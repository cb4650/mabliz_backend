package com.dztech.auth.service;

import com.dztech.auth.dto.DriverEmailOtpRequest;
import com.dztech.auth.dto.DriverEmailOtpResponse;
import com.dztech.auth.dto.DriverEmailVerificationRequest;
import com.dztech.auth.dto.DriverEmailVerificationResponse;
import com.dztech.auth.model.DriverEmailVerificationToken;
import com.dztech.auth.model.DriverProfile;
import com.dztech.auth.model.DriverProfileStatus;
import com.dztech.auth.model.User;
import com.dztech.auth.model.UserProfile;
import com.dztech.auth.repository.DriverProfileRepository;
import com.dztech.auth.repository.UserProfileRepository;
import com.dztech.auth.repository.UserRepository;
import com.dztech.auth.security.JwtTokenService;
import com.dztech.auth.security.JwtTokenService.JwtTokenPayload;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class DriverRegistrationService {

    private final DriverEmailOtpService driverEmailOtpService;
    private final DriverProfileRepository driverProfileRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final JwtTokenService jwtTokenService;

    public DriverRegistrationService(
            DriverEmailOtpService driverEmailOtpService,
            DriverProfileRepository driverProfileRepository,
            UserRepository userRepository,
            UserProfileRepository userProfileRepository,
            JwtTokenService jwtTokenService) {
        this.driverEmailOtpService = driverEmailOtpService;
        this.driverProfileRepository = driverProfileRepository;
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.jwtTokenService = jwtTokenService;
    }

    @Transactional
    public DriverEmailOtpResponse requestEmailOtp(Long userId, DriverEmailOtpRequest request) {
        String normalizedEmail = normalizeEmail(request.email());

        driverProfileRepository.findByEmail(normalizedEmail)
                .filter(existing -> !existing.getUserId().equals(userId))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Invalid email address or email already registered");
                });

        userRepository.findByEmail(normalizedEmail)
                .filter(existing -> !existing.getId().equals(userId))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Invalid email address or email already registered");
                });

        // Update profile with additional fields if provided
        DriverProfile profile = driverProfileRepository.findById(userId)
                .orElseGet(() -> createDriverProfile(userId, request.name(), normalizedEmail));
        
        boolean updated = false;
        if (StringUtils.hasText(request.gender()) && !StringUtils.hasText(profile.getGender())) {
            profile.setGender(request.gender());
            updated = true;
        }
        if (StringUtils.hasText(request.dob()) && !StringUtils.hasText(profile.getDob())) {
            profile.setDob(request.dob());
            updated = true;
        }
        if (request.languages() != null && !request.languages().isEmpty() &&
            (profile.getLanguages() == null || profile.getLanguages().isEmpty())) {
            profile.setLanguages(request.languages());
            updated = true;
        }
        // Allow Mother Tongue to be updated even if already set
        if (StringUtils.hasText(request.motherTongue())) {
            profile.setMotherTongue(request.motherTongue());
            updated = true;
        }
        
        if (updated) {
            profile = driverProfileRepository.save(profile);
        }

        driverEmailOtpService.sendOtp(userId, normalizedEmail, request.name());
        long expiresIn = driverEmailOtpService.getExpirySeconds();

        DriverEmailOtpResponse.Data data = new DriverEmailOtpResponse.Data(normalizedEmail, expiresIn);
        return new DriverEmailOtpResponse(true, "OTP sent successfully to " + normalizedEmail, data);
    }

    @Transactional
    public DriverEmailVerificationResponse verifyEmailOtp(Long userId, DriverEmailVerificationRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        DriverEmailVerificationToken token = driverEmailOtpService.verifyOtp(userId, normalizedEmail, request.otp());

        userRepository.findByEmail(normalizedEmail)
                .filter(existing -> !existing.getId().equals(userId))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Invalid email address or email already registered");
                });

        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Driver account not found"));
        user.setEmail(normalizedEmail);
        userRepository.save(user);

        DriverProfile profile = driverProfileRepository
                .findById(userId)
                .orElseGet(() -> createDriverProfile(userId, token.getName(), normalizedEmail));

        String tokenName = token.getName();
        if (StringUtils.hasText(tokenName)) {
            profile.setFullName(tokenName);
        } else if (!StringUtils.hasText(profile.getFullName())) {
            profile.setFullName(defaultNameFromEmail(normalizedEmail));
        }
        profile.setEmail(normalizedEmail);
        driverProfileRepository.save(profile);

        ensureUserProfile(user, tokenName, normalizedEmail);

        String jwt = issueDriverToken(user, profile);

        DriverEmailVerificationResponse.User userData =
                new DriverEmailVerificationResponse.User(profile.getFullName(), normalizedEmail);
        DriverEmailVerificationResponse.Data data = new DriverEmailVerificationResponse.Data(userData, jwt);
        return new DriverEmailVerificationResponse(true, "Email verified successfully", data);
    }

    private void ensureUserProfile(User user, String name, String email) {
        userProfileRepository
                .findByUserId(user.getId())
                .ifPresentOrElse(profile -> {
                    profile.setEmail(email);
                    profile.setEmailVerified(true);
                    if (StringUtils.hasText(name)) {
                        profile.setName(name);
                    } else if (!StringUtils.hasText(profile.getName())) {
                        profile.setName(defaultNameFromEmail(email));
                    }
                    if (!StringUtils.hasText(profile.getPhone())) {
                        profile.setPhone("");
                    }
                    userProfileRepository.save(profile);
                }, () -> userProfileRepository.save(UserProfile.builder()
                        .userId(user.getId())
                        .name(StringUtils.hasText(name) ? name : defaultNameFromEmail(email))
                        .phone("")
                        .email(email)
                        .emailVerified(true)
                        .build()));
    }

    private DriverProfile createDriverProfile(Long userId, String name, String email) {
        return DriverProfile.builder()
                .userId(userId)
                .fullName(StringUtils.hasText(name) ? name : defaultNameFromEmail(email))
                .email(email)
                .status(DriverProfileStatus.PENDING)
                .build();
    }

    private String issueDriverToken(User user, DriverProfile profile) {
        Map<String, Object> additional = new HashMap<>();
        additional.put("role", "DRIVER");
        JwtTokenPayload payload = new JwtTokenPayload(
                user.getId(),
                user.getUsername(),
                profile.getEmail(),
                profile.getPhone(),
                profile.getFullName(),
                additional);
        return jwtTokenService.generateAccessToken(payload);
    }

    private String normalizeEmail(String email) {
        if (!StringUtils.hasText(email)) {
            throw new IllegalArgumentException("Email is required");
        }
        return email.trim().toLowerCase();
    }

    private String defaultNameFromEmail(String email) {
        String local = email.split("@")[0];
        if (!StringUtils.hasText(local)) {
            return "Driver";
        }
        return local.substring(0, 1).toUpperCase() + local.substring(1);
    }
}
