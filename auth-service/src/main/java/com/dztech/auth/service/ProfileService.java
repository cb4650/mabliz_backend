package com.dztech.auth.service;

import com.dztech.auth.client.RayderVehicleClient;
import com.dztech.auth.dto.PreferredLanguageView;
import com.dztech.auth.dto.UpdateUserProfileRequest;
import com.dztech.auth.dto.UpdateUserPreferredLanguagesRequest;
import com.dztech.auth.dto.UserProfileView;
import com.dztech.auth.dto.VerifyEmailOtpRequest;
import com.dztech.auth.exception.ResourceNotFoundException;
import com.dztech.auth.model.PreferredLanguage;
import com.dztech.auth.model.UserProfile;
import com.dztech.auth.repository.PreferredLanguageRepository;
import com.dztech.auth.repository.UserProfileRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class ProfileService {

    private final UserProfileRepository userProfileRepository;
    private final RayderVehicleClient rayderVehicleClient;
    private final PreferredLanguageRepository preferredLanguageRepository;
    private final EmailOtpService emailOtpService;

    public ProfileService(
            UserProfileRepository userProfileRepository,
            RayderVehicleClient rayderVehicleClient,
            PreferredLanguageRepository preferredLanguageRepository,
            EmailOtpService emailOtpService) {
        this.userProfileRepository = userProfileRepository;
        this.rayderVehicleClient = rayderVehicleClient;
        this.preferredLanguageRepository = preferredLanguageRepository;
        this.emailOtpService = emailOtpService;
    }

    @Transactional(readOnly = true)
    public UserProfileView getProfile(Long userId, String accessToken) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User profile not found"));

        return toView(profile, accessToken);
    }

    @Transactional
    public UserProfileView updateProfile(Long userId, UpdateUserProfileRequest request, String accessToken) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User profile not found"));

        if (request.name() != null) {
            String name = request.name().trim();
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Name cannot be blank");
            }
            profile.setName(name);
        }

        boolean emailChanged = false;
        if (request.email() != null) {
            String email = request.email().trim();
            if (email.isEmpty()) {
                throw new IllegalArgumentException("Email cannot be blank");
            }
            String normalizedEmail = email.toLowerCase();
            String currentEmail = profile.getEmail();
            if (currentEmail == null || !currentEmail.equalsIgnoreCase(normalizedEmail)) {
                profile.setEmail(normalizedEmail);
                profile.setEmailVerified(false);
                emailChanged = true;
            }
        }

        if (request.address() != null) {
            String address = request.address().trim();
            profile.setAddress(StringUtils.hasText(address) ? address : null);
        }

        if (request.primaryPreferredLanguageId() != null) {
            profile.setPrimaryPreferredLanguage(resolvePreferredLanguage(request.primaryPreferredLanguageId()));
        }

        if (request.secondaryPreferredLanguageId() != null) {
            profile.setSecondaryPreferredLanguage(resolvePreferredLanguage(request.secondaryPreferredLanguageId()));
        }

        UserProfile updated = userProfileRepository.save(profile);

        if (emailChanged) {
            emailOtpService.sendVerificationOtp(
                    userId, updated.getEmail(), updated.getName());
        }

        return toView(updated, accessToken);
    }

    @Transactional(readOnly = true)
    public List<PreferredLanguageView> listPreferredLanguages() {
        return preferredLanguageRepository.findAll(Sort.by("name").ascending()).stream()
                .map(this::toLanguageView)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserProfileView updatePreferredLanguages(
            Long userId, UpdateUserPreferredLanguagesRequest request, String accessToken) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User profile not found"));

        if (request.primaryPreferredLanguageId() != null) {
            profile.setPrimaryPreferredLanguage(resolvePreferredLanguage(request.primaryPreferredLanguageId()));
        }

        if (request.secondaryPreferredLanguageId() != null) {
            profile.setSecondaryPreferredLanguage(resolvePreferredLanguage(request.secondaryPreferredLanguageId()));
        }

        UserProfile updated = userProfileRepository.save(profile);
        return toView(updated, accessToken);
    }

    @Transactional
    public UserProfileView verifyEmail(Long userId, VerifyEmailOtpRequest request, String accessToken) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User profile not found"));

        String normalizedEmail = request.email().trim().toLowerCase();
        String currentEmail = profile.getEmail();
        if (currentEmail == null || !currentEmail.equalsIgnoreCase(normalizedEmail)) {
            throw new IllegalArgumentException("Email does not match the current profile email");
        }

        emailOtpService.verifyOtp(userId, normalizedEmail, request.otp());

        if (!normalizedEmail.equals(currentEmail)) {
            profile.setEmail(normalizedEmail);
        }
        profile.setEmailVerified(true);
        UserProfile updated = userProfileRepository.save(profile);
        return toView(updated, accessToken);
    }

    private UserProfileView toView(UserProfile profile, String accessToken) {
        long vehicleCount = rayderVehicleClient.fetchVehicleCount(accessToken);
        long completedBookings = 0L;
        long activeBookings = 0L;

        return new UserProfileView(
                profile.getUserId(),
                profile.getName(),
                profile.getPhone(),
                profile.getEmail(),
                profile.isEmailVerified(),
                profile.getAddress(),
                toLanguageView(profile.getPrimaryPreferredLanguage()),
                toLanguageView(profile.getSecondaryPreferredLanguage()),
                vehicleCount,
                completedBookings,
                activeBookings);
    }

    private PreferredLanguage resolvePreferredLanguage(Long id) {
        if (id == null) {
            return null;
        }

        if (id <= 0) {
            return null;
        }

        return preferredLanguageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Preferred language not found for id: " + id));
    }

    private PreferredLanguageView toLanguageView(PreferredLanguage language) {
        if (language == null) {
            return null;
        }
        return new PreferredLanguageView(language.getId(), language.getCode(), language.getName());
    }
}
