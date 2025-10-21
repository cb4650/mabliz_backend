package com.dztech.auth.service;

import com.dztech.auth.dto.LoginRequest;
import com.dztech.auth.dto.LoginResponse;
import com.dztech.auth.dto.OtpVerificationRequest;
import com.dztech.auth.model.User;
import com.dztech.auth.model.UserProfile;
import com.dztech.auth.repository.UserProfileRepository;
import com.dztech.auth.repository.UserRepository;
import com.dztech.auth.security.JwtTokenService;
import com.dztech.auth.security.JwtTokenService.JwtTokenPayload;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AuthenticationService {

    private static final String STATIC_OTP_CODE = "123456";
    private static final String OTP_EMAIL_DOMAIN = "otp-auth.local";

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final JwtTokenService jwtTokenService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthenticationService(
            UserRepository userRepository,
            UserProfileRepository userProfileRepository,
            JwtTokenService jwtTokenService) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.jwtTokenService = jwtTokenService;
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = resolveUser(request.usernameOrEmail());
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid username/email or password");
        }

        String normalizedName = normalizeName(request.name());
        String normalizedPhone = normalizePhone(request.phone());
        String normalizedEmail = normalizeEmail(user.getEmail());

        boolean profileCreated = false;
        UserProfile profile = userProfileRepository.findByUserId(user.getId()).orElse(null);
        if (profile == null) {
            profileCreated = true;
            profile = UserProfile.builder()
                    .userId(user.getId())
                    .name(normalizedName)
                    .phone(normalizedPhone)
                    .email(normalizedEmail)
                    .build();
        } else {
            profile.setName(normalizedName);
            profile.setPhone(normalizedPhone);
            profile.setEmail(normalizedEmail);
        }

        UserProfile savedProfile = userProfileRepository.save(profile);

        String token = jwtTokenService.generateToken(new JwtTokenPayload(
                user.getId(),
                user.getUsername(),
                savedProfile.getEmail(),
                savedProfile.getPhone(),
                savedProfile.getName(),
                Map.of()));

        return new LoginResponse(
                true,
                profileCreated,
                token,
                user.getId(),
                savedProfile.getName(),
                savedProfile.getPhone(),
                savedProfile.getEmail());
    }

    @Transactional
    public LoginResponse verifyOtp(OtpVerificationRequest request) {
        String normalizedPhone = normalizePhone(request.phone());
        String normalizedOtp = normalizeOtp(request.otp());

        if (!STATIC_OTP_CODE.equals(normalizedOtp)) {
            throw new IllegalArgumentException("Invalid OTP");
        }

        boolean newUser = false;
        UserProfile profile = userProfileRepository.findByPhone(normalizedPhone).orElse(null);
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

        String token = jwtTokenService.generateToken(new JwtTokenPayload(
                user.getId(),
                user.getUsername(),
                profile.getEmail(),
                profile.getPhone(),
                profile.getName(),
                Map.of()));

        return new LoginResponse(
                true,
                newUser,
                token,
                user.getId(),
                profile.getName(),
                profile.getPhone(),
                profile.getEmail());
    }

    private User resolveUser(String identifier) {
        String normalized = normalizeIdentifier(identifier);
        Optional<User> byUsername = userRepository.findByUsername(normalized);
        if (byUsername.isPresent()) {
            return byUsername.get();
        }

        Optional<User> byEmail = userRepository.findByEmail(normalized.toLowerCase());
        if (byEmail.isPresent()) {
            return byEmail.get();
        }

        throw new IllegalArgumentException("Invalid username/email or password");
    }

    private String normalizeIdentifier(String raw) {
        String trimmed = raw == null ? "" : raw.trim();
        if (!StringUtils.hasText(trimmed)) {
            throw new IllegalArgumentException("Username or email is required");
        }
        return trimmed;
    }

    private String normalizeName(String rawName) {
        String trimmed = rawName == null ? "" : rawName.trim();
        if (!StringUtils.hasText(trimmed)) {
            throw new IllegalArgumentException("Name is required");
        }
        return trimmed;
    }

    private String normalizePhone(String rawPhone) {
        String trimmed = rawPhone == null ? "" : rawPhone.trim();
        if (!StringUtils.hasText(trimmed)) {
            throw new IllegalArgumentException("Phone number is required");
        }
        return trimmed;
    }

    private String normalizeEmail(String rawEmail) {
        if (!StringUtils.hasText(rawEmail)) {
            throw new IllegalArgumentException("Email address is not configured for this user");
        }
        return rawEmail.trim().toLowerCase();
    }

    private String normalizeOtp(String rawOtp) {
        String trimmed = rawOtp == null ? "" : rawOtp.trim();
        if (!StringUtils.hasText(trimmed)) {
            throw new IllegalArgumentException("OTP is required");
        }
        return trimmed;
    }

    private User createUserForPhone(String phone) {
        String baseIdentifier = sanitizeForIdentifier(phone);
        String username = generateUniqueUsername("otp_" + baseIdentifier);
        String email = generateUniqueEmail("otp-" + baseIdentifier);
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
}
