package com.dztech.auth.service;

import com.dztech.auth.client.OtpProviderClient;
import com.dztech.auth.config.LoginProfileProperties;
import com.dztech.auth.config.LoginProfileProperties.ProfileType;
import com.dztech.auth.dto.LoginResponse;
import com.dztech.auth.dto.OtpRequest;
import com.dztech.auth.dto.OtpRequestResponse;
import com.dztech.auth.dto.OtpVerificationRequest;
import com.dztech.auth.dto.TokenRefreshRequest;
import com.dztech.auth.dto.TokenRefreshResponse;
import com.dztech.auth.exception.OtpBlockedException;
import com.dztech.auth.model.AppId;
import com.dztech.auth.model.DriverProfile;
import com.dztech.auth.model.DriverProfileStatus;
import com.dztech.auth.model.OtpFailureTracking;
import com.dztech.auth.model.User;
import com.dztech.auth.model.UserProfile;
import com.dztech.auth.repository.DriverProfileRepository;
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
    private final DriverProfileRepository driverProfileRepository;
    private final JwtTokenService jwtTokenService;
    private final OtpProviderClient otpProviderClient;
    private final LoginProfileProperties loginProfileProperties;
    private final OtpFailureTrackingService otpFailureTrackingService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthenticationService(
            UserRepository userRepository,
            UserProfileRepository userProfileRepository,
            DriverProfileRepository driverProfileRepository,
            JwtTokenService jwtTokenService,
            OtpProviderClient otpProviderClient,
            LoginProfileProperties loginProfileProperties,
            OtpFailureTrackingService otpFailureTrackingService) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.driverProfileRepository = driverProfileRepository;
        this.jwtTokenService = jwtTokenService;
        this.otpProviderClient = otpProviderClient;
        this.loginProfileProperties = loginProfileProperties;
        this.otpFailureTrackingService = otpFailureTrackingService;
    }

    @Transactional(readOnly = true)
    public OtpRequestResponse requestOtp(OtpRequest request, AppId appId) {
        String normalizedPhone = normalizePhone(request.phone());

        // Check if user is new (no existing profile)
        boolean newUser = isNewUser(normalizedPhone, appId);

        otpProviderClient.sendOtp(normalizedPhone, appId);
        return new OtpRequestResponse(true, "OTP sent successfully", newUser);
    }

    @Transactional
    public LoginResponse verifyOtp(OtpVerificationRequest request, AppId appId) {
        String normalizedPhone = normalizePhone(request.phone());
        String normalizedOtp = normalizeOtp(request.otp());
        OtpFailureTracking.RoleType roleType = resolveRoleType(appId);

        // Check if phone is blocked for this role
        if (otpFailureTrackingService.isOtpBlocked(normalizedPhone, roleType)) {
            var remainingTime = otpFailureTrackingService.getRemainingBlockTime(normalizedPhone, roleType);
            throw new OtpBlockedException(
                "Phone number is temporarily blocked due to too many failed OTP attempts",
                remainingTime.orElse(java.time.Duration.ZERO));
        }

        try {
            otpProviderClient.verifyOtp(normalizedPhone, normalizedOtp);

            // Reset failure count on successful verification
            otpFailureTrackingService.resetOtpFailures(normalizedPhone, roleType);

            ProfileResult profileResult = handleProfileForLogin(appId, normalizedPhone, request.phone());
            TokenPair tokens = issueTokens(profileResult.user(), profileResult.profileData(), appId);

            return new LoginResponse(
                    true,
                    profileResult.newUser(),
                    tokens.accessToken(),
                    tokens.refreshToken(),
                    profileResult.user().getId(),
                    profileResult.profileData().name(),
                    profileResult.profileData().phone(),
                    profileResult.profileData().email(),
                    profileResult.profileData().emailVerified());
        } catch (Exception e) {
            // Record failure for invalid OTP or other verification errors
            otpFailureTrackingService.recordOtpFailure(normalizedPhone, roleType);
            throw e;
        }
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

        AppId appId = resolveAppIdFromClaims(claims);
        ProfileData profileData = resolveProfileForRefresh(appId, user);
        TokenPair tokens = issueTokens(user, profileData, appId);

        return new TokenRefreshResponse(
                true,
                tokens.accessToken(),
                tokens.refreshToken(),
                user.getId(),
                profileData.name(),
                profileData.phone(),
                profileData.email(),
                profileData.emailVerified());
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

    private ProfileResult handleProfileForLogin(AppId appId, String normalizedPhone, String rawPhone) {
        ProfileType profileType = loginProfileProperties.resolve(appId);
        return switch (profileType) {
            case DRIVER -> handleDriverProfileLogin(normalizedPhone, rawPhone);
            case USER -> handleUserProfileLogin(normalizedPhone, rawPhone);
        };
    }

    private ProfileResult handleUserProfileLogin(String normalizedPhone, String rawPhone) {
        boolean newUser = false;
        UserProfile profile = userProfileRepository.findByPhone(normalizedPhone).orElse(null);
        if (profile == null) {
            String legacyPhone = legacyNormalizePhone(rawPhone);
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

        ProfileData profileData = new ProfileData(
                profile.getName(),
                profile.getPhone(),
                profile.getEmail(),
                profile.isEmailVerified());

        return new ProfileResult(user, profileData, newUser);
    }

    private ProfileResult handleDriverProfileLogin(String normalizedPhone, String rawPhone) {
        boolean newUser = false;
        DriverProfile profile = driverProfileRepository.findByPhone(normalizedPhone).orElse(null);
        if (profile == null) {
            String legacyPhone = legacyNormalizePhone(rawPhone);
            if (StringUtils.hasText(legacyPhone) && !legacyPhone.equals(normalizedPhone)) {
                profile = driverProfileRepository.findByPhone(legacyPhone).orElse(null);
            }
        }

        User user;
        if (profile != null) {
            user = userRepository.findById(profile.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("Driver account is unavailable for this phone number"));
            boolean profileUpdated = false;
            if (!StringUtils.hasText(profile.getPhone()) || !normalizedPhone.equals(profile.getPhone())) {
                profile.setPhone(normalizedPhone);
                profileUpdated = true;
            }
            if (!StringUtils.hasText(profile.getFullName())) {
                profile.setFullName(defaultNameFromPhone(normalizedPhone));
                profileUpdated = true;
            }
            if (!StringUtils.hasText(profile.getEmail())) {
                profile.setEmail(user.getEmail());
                profileUpdated = true;
            }
            if (profileUpdated) {
                profile = driverProfileRepository.save(profile);
            }
        } else {
            user = createUserForPhone(normalizedPhone);
            profile = createDriverProfileForUser(user, normalizedPhone);
            newUser = true;
        }

        boolean emailVerified = userProfileRepository.findByUserId(user.getId())
                .map(UserProfile::isEmailVerified)
                .orElse(false);

        String name = StringUtils.hasText(profile.getFullName())
                ? profile.getFullName()
                : defaultNameFromPhone(normalizedPhone);
        String phone = StringUtils.hasText(profile.getPhone()) ? profile.getPhone() : normalizedPhone;
        String email = StringUtils.hasText(profile.getEmail()) ? profile.getEmail() : user.getEmail();

        ProfileData profileData = new ProfileData(name, phone, email, emailVerified);
        return new ProfileResult(user, profileData, newUser);
    }

    private ProfileData resolveProfileForRefresh(AppId appId, User user) {
        ProfileType profileType = loginProfileProperties.resolve(appId);
        return switch (profileType) {
            case DRIVER -> resolveDriverProfileData(user);
            case USER -> resolveUserProfileData(user);
        };
    }

    private ProfileData resolveUserProfileData(User user) {
        UserProfile profile = userProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("User profile is unavailable for this refresh token"));
        return new ProfileData(
                profile.getName(), profile.getPhone(), profile.getEmail(), profile.isEmailVerified());
    }

    private ProfileData resolveDriverProfileData(User user) {
        DriverProfile profile = driverProfileRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Driver profile is unavailable for this refresh token"));
        boolean emailVerified = userProfileRepository.findByUserId(user.getId())
                .map(UserProfile::isEmailVerified)
                .orElse(false);
        String phone = StringUtils.hasText(profile.getPhone()) ? profile.getPhone() : "";
        String name = StringUtils.hasText(profile.getFullName())
                ? profile.getFullName()
                : (StringUtils.hasText(phone) ? defaultNameFromPhone(phone) : defaultNameFromPhone(user.getUsername()));
        String email = StringUtils.hasText(profile.getEmail()) ? profile.getEmail() : user.getEmail();
        return new ProfileData(name, phone, email, emailVerified);
    }

    private TokenPair issueTokens(User user, ProfileData profile, AppId appId) {
        Map<String, Object> additionalClaims = new HashMap<>();
        if (appId != null) {
            additionalClaims.put("appId", appId.value());
        }
        JwtTokenPayload payload = new JwtTokenPayload(
                user.getId(),
                user.getUsername(),
                profile.email(),
                profile.phone(),
                profile.name(),
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

    /**
     * Public method to normalize phone number for user checking.
     * This is exposed for controller use.
     */
    public String normalizePhoneForCheck(String rawPhone) {
        return normalizePhone(rawPhone);
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
                .emailVerified(false)
                .build();
        return userProfileRepository.save(profile);
    }

    private DriverProfile createDriverProfileForUser(User user, String phone) {
        DriverProfile profile = DriverProfile.builder()
                .userId(user.getId())
                .fullName(defaultNameFromPhone(phone))
                .phone(phone)
                .email(user.getEmail())
                .status(DriverProfileStatus.PENDING)
                .build();
        return driverProfileRepository.save(profile);
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

    private boolean isNewUser(String normalizedPhone, AppId appId) {
        ProfileType profileType = loginProfileProperties.resolve(appId);
        return switch (profileType) {
            case DRIVER -> driverProfileRepository.findByPhone(normalizedPhone).isEmpty();
            case USER -> userProfileRepository.findByPhone(normalizedPhone).isEmpty();
        };
    }

    /**
     * Checks if the user is new for driver registration.
     * Only checks the driver table, even if the phone number exists in the user table.
     */
    public boolean isNewDriverUser(String normalizedPhone, AppId appId) {
        ProfileType profileType = loginProfileProperties.resolve(appId);
        if (profileType == ProfileType.DRIVER) {
            return driverProfileRepository.findByPhone(normalizedPhone).isEmpty();
        }
        throw new IllegalArgumentException("This method is only for driver appIds");
    }

    private OtpFailureTracking.RoleType resolveRoleType(AppId appId) {
        ProfileType profileType = loginProfileProperties.resolve(appId);
        return switch (profileType) {
            case DRIVER -> OtpFailureTracking.RoleType.DRIVER;
            case USER -> OtpFailureTracking.RoleType.USER;
        };
    }

    private String defaultNameFromPhone(String phone) {
        String digits = phone.replaceAll("\\D", "");
        if (digits.length() >= 4) {
            return "Guest " + digits.substring(digits.length() - 4);
        }
        return "Guest " + phone;
    }

    private record ProfileResult(User user, ProfileData profileData, boolean newUser) {}

    private record ProfileData(String name, String phone, String email, boolean emailVerified) {}

    private record TokenPair(String accessToken, String refreshToken) {}
}
