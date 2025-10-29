package com.dztech.auth.service;

import com.dztech.auth.client.OtpProviderClient;
import com.dztech.auth.dto.LoginResponse;
import com.dztech.auth.dto.OtpRequest;
import com.dztech.auth.dto.OtpRequestResponse;
import com.dztech.auth.dto.OtpVerificationRequest;
import com.dztech.auth.dto.TokenRefreshRequest;
import com.dztech.auth.dto.TokenRefreshResponse;
import com.dztech.auth.model.AppId;
import com.dztech.auth.model.User;
import com.dztech.auth.model.UserProfile;
import com.dztech.auth.repository.UserProfileRepository;
import com.dztech.auth.repository.UserRepository;
import com.dztech.auth.security.JwtTokenService;
import com.dztech.auth.security.JwtTokenService.JwtClaims;
import com.dztech.auth.security.JwtTokenService.JwtTokenPayload;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AuthenticationService {

    private static final String OTP_EMAIL_DOMAIN = "otp-auth.local";

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final JwtTokenService jwtTokenService;
    private final OtpProviderClient otpProviderClient;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthenticationService(
            UserRepository userRepository,
            UserProfileRepository userProfileRepository,
            JwtTokenService jwtTokenService,
            OtpProviderClient otpProviderClient) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.jwtTokenService = jwtTokenService;
        this.otpProviderClient = otpProviderClient;
    }

    @Transactional(readOnly = true)
    public OtpRequestResponse requestOtp(OtpRequest request, AppId appId) {
        String normalizedPhone = normalizePhone(request.phone());
        otpProviderClient.sendOtp(normalizedPhone, appId);
        return new OtpRequestResponse(true, "OTP sent successfully");
    }

    @Transactional
    public LoginResponse verifyOtp(OtpVerificationRequest request, AppId appId) {
        String normalizedPhone = normalizePhone(request.phone());
        String normalizedOtp = normalizeOtp(request.otp());

        otpProviderClient.verifyOtp(normalizedPhone, normalizedOtp);

        boolean newUser = false;
        UserProfile profile = userProfileRepository.findByPhone(normalizedPhone).orElse(null);
        if (profile == null) {
            String legacyPhone = legacyNormalizePhone(request.phone());
            if (StringUtils.hasText(legacyPhone) && !legacyPhone.equals(normalizedPhone)) {
                profile = userProfileRepository.findByPhone(legacyPhone).orElse(null);
            }
        }
        User user;

        if (profile != null) {
            user = userRepository.findById(profile.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("User account is unavailable for this phone number"));
            boolean profileUpdated = false;
            if (!normalizedPhone.equals(profile.getPhone())) {
                profile.setPhone(normalizedPhone);
                profileUpdated = true;
            }
            if (!StringUtils.hasText(profile.getEmail())) {
                profile.setEmail(user.getEmail());
                profileUpdated = true;
            }
            if (!StringUtils.hasText(profile.getName())) {
                profile.setName(defaultNameFromPhone(normalizedPhone));
                profileUpdated = true;
            }
            if (profileUpdated) {
                profile = userProfileRepository.save(profile);
            }
        } else {
            user = createUserForPhone(normalizedPhone);
            profile = createProfileForUser(user, normalizedPhone);
            newUser = true;
        }

        TokenPair tokens = issueTokens(user, profile, appId);

        return new LoginResponse(
                true,
                newUser,
                tokens.accessToken(),
                tokens.refreshToken(),
                user.getId(),
                profile.getName(),
                profile.getPhone(),
                profile.getEmail());
    }

    public TokenRefreshResponse refreshToken(TokenRefreshRequest request) {
        String normalizedRefreshToken = normalizeRefreshToken(request.refreshToken());
        JwtClaims claims = jwtTokenService.parseRefreshToken(normalizedRefreshToken);
        Long userId = claims.userId();
        if (userId == null) {
            throw new IllegalArgumentException("Refresh token is missing user id");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User account is unavailable for this refresh token"));
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("User profile is unavailable for this refresh token"));

        AppId appId = resolveAppIdFromClaims(claims);
        TokenPair tokens = issueTokens(user, profile, appId);

        return new TokenRefreshResponse(
                true,
                tokens.accessToken(),
                tokens.refreshToken(),
                user.getId(),
                profile.getName(),
                profile.getPhone(),
                profile.getEmail());
    }

    private AppId resolveAppIdFromClaims(JwtClaims claims) {
        Object rawAppId = claims.additionalClaims().get("appId");
        if (rawAppId instanceof String appIdString) {
            try {
                return AppId.fromHeader(appIdString);
            } catch (IllegalArgumentException ignored) {
                // fall back to default if token contained unexpected value
            }
        }
        return AppId.ALL;
    }

    private TokenPair issueTokens(User user, UserProfile profile, AppId appId) {
        Map<String, Object> additionalClaims = new HashMap<>();
        if (appId != null) {
            additionalClaims.put("appId", appId.value());
        }
        JwtTokenPayload payload = new JwtTokenPayload(
                user.getId(),
                user.getUsername(),
                profile.getEmail(),
                profile.getPhone(),
                profile.getName(),
                additionalClaims);

        String accessToken = jwtTokenService.generateAccessToken(payload);
        String refreshToken = jwtTokenService.generateRefreshToken(payload);
        return new TokenPair(accessToken, refreshToken);
    }

    private String normalizePhone(String rawPhone) {
        String trimmed = rawPhone == null ? "" : rawPhone.trim();
        if (!StringUtils.hasText(trimmed)) {
            throw new IllegalArgumentException("Phone number is required");
        }
        String digitsOnly = trimmed.replaceAll("\\D", "");
        if (!StringUtils.hasText(digitsOnly)) {
            throw new IllegalArgumentException("Phone number must contain digits");
        }
        return digitsOnly;
    }

    private String legacyNormalizePhone(String rawPhone) {
        String trimmed = rawPhone == null ? "" : rawPhone.trim();
        return StringUtils.hasText(trimmed) ? trimmed : null;
    }

    private String normalizeOtp(String rawOtp) {
        String trimmed = rawOtp == null ? "" : rawOtp.trim();
        if (!StringUtils.hasText(trimmed)) {
            throw new IllegalArgumentException("OTP is required");
        }
        return trimmed;
    }

    private String normalizeRefreshToken(String rawToken) {
        String trimmed = rawToken == null ? "" : rawToken.trim();
        if (!StringUtils.hasText(trimmed)) {
            throw new IllegalArgumentException("Refresh token is required");
        }
        return trimmed;
    }

    private User createUserForPhone(String phone) {
        String baseIdentifier = sanitizeForIdentifier(phone);
        String username = generateUniqueUsername("ryd_" + baseIdentifier);
        String email = generateUniqueEmail("ryd-" + baseIdentifier);
        String randomPassword = UUID.randomUUID().toString();

        User user = User.builder()
                .username(username)
                .email(email)
                .passwordHash(passwordEncoder.encode(randomPassword))
                .build();

        return userRepository.save(user);
    }

    private UserProfile createProfileForUser(User user, String phone) {
        UserProfile profile = UserProfile.builder()
                .userId(user.getId())
                .name(defaultNameFromPhone(phone))
                .phone(phone)
                .email(user.getEmail())
                .build();
        return userProfileRepository.save(profile);
    }

    private String sanitizeForIdentifier(String phone) {
        String digits = phone.replaceAll("\\D", "");
        if (StringUtils.hasText(digits)) {
            return digits;
        }
        String alphanumeric = phone.replaceAll("[^a-zA-Z0-9]", "");
        if (StringUtils.hasText(alphanumeric)) {
            return alphanumeric;
        }
        return "user";
    }

    private String generateUniqueUsername(String base) {
        String sanitized = base.replaceAll("[^a-zA-Z0-9._-]", "");
        if (!StringUtils.hasText(sanitized)) {
            sanitized = "user";
        }
        if (sanitized.length() > 100) {
            sanitized = sanitized.substring(0, 100);
        }
        String candidate = sanitized;
        int counter = 0;
        while (userRepository.existsByUsername(candidate)) {
            counter++;
            String suffix = "_" + counter;
            int maxBaseLength = Math.max(1, 100 - suffix.length());
            String truncatedBase = sanitized.length() > maxBaseLength ? sanitized.substring(0, maxBaseLength) : sanitized;
            candidate = truncatedBase + suffix;
        }
        return candidate;
    }

    private String generateUniqueEmail(String localPartBase) {
        String sanitized = localPartBase.replaceAll("[^a-zA-Z0-9._-]", "");
        if (!StringUtils.hasText(sanitized)) {
            sanitized = "user";
        }
        int maxLocalLength = Math.max(1, 150 - OTP_EMAIL_DOMAIN.length() - 1);
        if (sanitized.length() > maxLocalLength) {
            sanitized = sanitized.substring(0, maxLocalLength);
        }
        String candidate = sanitized + "@" + OTP_EMAIL_DOMAIN;
        int counter = 0;
        while (userRepository.existsByEmail(candidate)) {
            counter++;
            String suffix = "-" + counter;
            int dynamicMax = Math.max(1, 150 - OTP_EMAIL_DOMAIN.length() - 1 - suffix.length());
            String truncatedBase = sanitized.length() > dynamicMax ? sanitized.substring(0, dynamicMax) : sanitized;
            candidate = truncatedBase + suffix + "@" + OTP_EMAIL_DOMAIN;
        }
        return candidate;
    }

    private String defaultNameFromPhone(String phone) {
        String digits = phone.replaceAll("\\D", "");
        if (digits.length() >= 4) {
            return "Guest " + digits.substring(digits.length() - 4);
        }
        return "Guest " + phone;
    }

    private record TokenPair(String accessToken, String refreshToken) {
    }
}
