package com.dztech.auth.service;

import com.dztech.auth.client.OtpProviderClient;
import com.dztech.auth.client.RayderVehicleClient;
import com.dztech.auth.dto.ChangeEmailRequest;
import com.dztech.auth.dto.ChangeEmailResponse;
import com.dztech.auth.dto.ChangeMobileRequest;
import com.dztech.auth.dto.ChangeMobileResponse;
import com.dztech.auth.dto.CustomerEmailOtpRequest;
import com.dztech.auth.dto.CustomerEmailOtpResponse;
import com.dztech.auth.dto.CustomerEmailVerificationRequest;
import com.dztech.auth.dto.CustomerEmailVerificationResponse;
import com.dztech.auth.dto.PreferredLanguageListResponse;
import com.dztech.auth.dto.PreferredLanguageView;
import com.dztech.auth.dto.RequestEmailChangeOtpRequest;
import com.dztech.auth.dto.RequestEmailChangeOtpResponse;
import com.dztech.auth.dto.RequestMobileChangeOtpRequest;
import com.dztech.auth.dto.RequestMobileChangeOtpResponse;
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
    private final OtpProviderClient otpProviderClient;

    public ProfileService(
            UserProfileRepository userProfileRepository,
            RayderVehicleClient rayderVehicleClient,
            PreferredLanguageRepository preferredLanguageRepository,
            EmailOtpService emailOtpService,
            OtpProviderClient otpProviderClient) {
        this.userProfileRepository = userProfileRepository;
        this.rayderVehicleClient = rayderVehicleClient;
        this.preferredLanguageRepository = preferredLanguageRepository;
        this.emailOtpService = emailOtpService;
        this.otpProviderClient = otpProviderClient;
    }

    @Transactional(readOnly = true)
    public UserProfileView getProfile(Long userId, String accessToken) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User profile not found"));

        return toView(profile, accessToken);
    }

    @Transactional
    public RequestEmailChangeOtpResponse requestEmailChangeOtp(Long userId, RequestEmailChangeOtpRequest request) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User profile not found"));

        String newEmail = request.newEmail().trim().toLowerCase();
        
        // Check if email is already in use by another user
        if (userProfileRepository.existsByEmailAndUserIdNot(newEmail, userId)) {
            throw new IllegalArgumentException("Email is already in use by another account");
        }

        // Send OTP to current phone number for verification
        try {
            otpProviderClient.sendOtp(profile.getPhone(), null);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send OTP to phone: " + e.getMessage());
        }

        return RequestEmailChangeOtpResponse.builder()
                .success(true)
                .message("OTP sent to your current phone number for email change verification")
                .newEmail(newEmail)
                .build();
    }

    @Transactional
    public RequestMobileChangeOtpResponse requestMobileChangeOtp(Long userId, RequestMobileChangeOtpRequest request) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User profile not found"));

        String newPhone = request.newPhone().trim();
        
        // Check if phone is already in use by another user
        if (userProfileRepository.existsByPhoneAndUserIdNot(newPhone, userId)) {
            throw new IllegalArgumentException("Phone number is already in use by another account");
        }

        // Send OTP to current email for verification
        try {
            emailOtpService.sendVerificationOtp(userId, profile.getEmail(), "Your OTP for mobile change is: {otp}");
        } catch (Exception e) {
            throw new RuntimeException("Failed to send OTP to email: " + e.getMessage());
        }

        return RequestMobileChangeOtpResponse.builder()
                .success(true)
                .message("OTP sent to your current email for mobile change verification")
                .newPhone(newPhone)
                .build();
    }

    @Transactional
    public CustomerEmailOtpResponse requestEmailOtp(Long userId, CustomerEmailOtpRequest request) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User profile not found"));

        String email = request.email().trim().toLowerCase();
        String name = request.name().trim();
        
        // Validate name length
        if (name.length() > 150) {
            throw new IllegalArgumentException("Name must be at most 150 characters");
        }
        
        // Update name if it's different from current profile name
        if (!name.equals(profile.getName())) {
            profile.setName(name);
        }
        
        // Check if email is already in use by another user
        if (userProfileRepository.existsByEmailAndUserIdNot(email, userId)) {
            throw new IllegalArgumentException("Email is already in use by another account");
        }

        // Send OTP to email for verification
        try {
            emailOtpService.sendVerificationOtp(userId, email, profile.getName());
        } catch (Exception e) {
            throw new RuntimeException("Failed to send OTP to email: " + e.getMessage());
        }

        return new CustomerEmailOtpResponse(
                true,
                "OTP sent to email successfully",
                new CustomerEmailOtpResponse.Data(email, 300));
    }

    @Transactional
    public CustomerEmailVerificationResponse verifyEmailOtp(Long userId, CustomerEmailVerificationRequest request) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User profile not found"));

        String email = request.email().trim().toLowerCase();
        String otp = request.otp();
        
        // Verify the OTP
        try {
            emailOtpService.verifyOtp(userId, email, otp);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid email OTP: " + e.getMessage());
        }

        // Update email and mark as verified
        profile.setEmail(email);
        profile.setEmailVerified(true);
        
        UserProfile updated = userProfileRepository.save(profile);
        
        return new CustomerEmailVerificationResponse(
                true,
                "Email verified successfully",
                new CustomerEmailVerificationResponse.Data(
                        new CustomerEmailVerificationResponse.User(updated.getName(), updated.getEmail()), 
                        null));
    }

    @Transactional
    public UserProfileView changeEmail(Long userId, ChangeEmailRequest request, String accessToken) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User profile not found"));

        String normalizedNewEmail = request.newEmail().trim().toLowerCase();
        String currentEmail = profile.getEmail();
        
        // Verify the phone OTP first (cross-verification)
        try {
            otpProviderClient.verifyOtp(profile.getPhone(), request.phoneOtp());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid phone OTP: " + e.getMessage());
        }

        // Check if email is already in use by another user
        if (userProfileRepository.existsByEmailAndUserIdNot(normalizedNewEmail, userId)) {
            throw new IllegalArgumentException("Email is already in use by another account");
        }

        // Update email and mark as unverified
        profile.setEmail(normalizedNewEmail);
        profile.setEmailVerified(false);
        
        UserProfile updated = userProfileRepository.save(profile);
        
        // Send verification OTP for the new email
        emailOtpService.sendVerificationOtp(userId, normalizedNewEmail, profile.getName());

        return toView(updated, accessToken);
    }

    @Transactional
    public UserProfileView changeMobile(Long userId, ChangeMobileRequest request, String accessToken) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User profile not found"));

        String newPhone = request.newPhone().trim();
        
        // Verify the email OTP first (cross-verification)
        try {
            emailOtpService.verifyOtp(userId, profile.getEmail(), request.emailOtp());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid email OTP: " + e.getMessage());
        }

        // Check if phone is already in use by another user
        if (userProfileRepository.existsByPhoneAndUserIdNot(newPhone, userId)) {
            throw new IllegalArgumentException("Phone number is already in use by another account");
        }

        // Update phone number
        profile.setPhone(newPhone);
        
        UserProfile updated = userProfileRepository.save(profile);
        
        return toView(updated, accessToken);
    }

    @Transactional
    public UserProfileView verifyAndChangeMobile(Long userId, ChangeMobileRequest request, String accessToken) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User profile not found"));

        String newPhone = request.newPhone().trim();
        
        // Verify the email OTP first (cross-verification)
        try {
            emailOtpService.verifyOtp(userId, profile.getEmail(), request.emailOtp());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid email OTP: " + e.getMessage());
        }

        // Check if phone is already in use by another user
        if (userProfileRepository.existsByPhoneAndUserIdNot(newPhone, userId)) {
            throw new IllegalArgumentException("Phone number is already in use by another account");
        }

        // Update phone number
        profile.setPhone(newPhone);
        
        UserProfile updated = userProfileRepository.save(profile);
        
        return toView(updated, accessToken);
    }

    @Transactional
    public UserProfileView verifyAndChangeEmail(Long userId, ChangeEmailRequest request, String accessToken) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User profile not found"));

        String normalizedNewEmail = request.newEmail().trim().toLowerCase();
        String currentEmail = profile.getEmail();
        
        // Verify the phone OTP first (cross-verification)
        try {
            otpProviderClient.verifyOtp(profile.getPhone(), request.phoneOtp());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid phone OTP: " + e.getMessage());
        }

        // Check if email is already in use by another user
        if (userProfileRepository.existsByEmailAndUserIdNot(normalizedNewEmail, userId)) {
            throw new IllegalArgumentException("Email is already in use by another account");
        }

        // Update email and mark as unverified
        profile.setEmail(normalizedNewEmail);
        profile.setEmailVerified(false);
        
        UserProfile updated = userProfileRepository.save(profile);
        
        // Send verification OTP for the new email
        emailOtpService.sendVerificationOtp(userId, normalizedNewEmail, profile.getName());

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

    private PreferredLanguageView toLanguageView(PreferredLanguage language) {
        if (language == null) {
            return null;
        }
        return new PreferredLanguageView(language.getId(), language.getCode(), language.getName());
    }

    @Transactional(readOnly = true)
    public PreferredLanguageListResponse getPreferredLanguages() {
        List<PreferredLanguage> languages = preferredLanguageRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
        
        List<PreferredLanguageView> languageViews = languages.stream()
                .map(language -> new PreferredLanguageView(language.getId(), language.getCode(), language.getName()))
                .collect(Collectors.toList());
        
        return new PreferredLanguageListResponse(true, languageViews);
    }

    @Transactional
    public UserProfileView updatePreferredLanguages(Long userId, UpdateUserPreferredLanguagesRequest request) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User profile not found"));

        // Validate preferred languages exist
        PreferredLanguage primaryLanguage = preferredLanguageRepository.findById(request.primaryPreferredLanguageId())
                .orElseThrow(() -> new IllegalArgumentException("Primary preferred language not found"));
        
        PreferredLanguage secondaryLanguage = preferredLanguageRepository.findById(request.secondaryPreferredLanguageId())
                .orElseThrow(() -> new IllegalArgumentException("Secondary preferred language not found"));

        // Update preferred languages
        profile.setPrimaryPreferredLanguage(primaryLanguage);
        profile.setSecondaryPreferredLanguage(secondaryLanguage);
        
        UserProfile updated = userProfileRepository.save(profile);
        
        return toView(updated, null); // accessToken not needed for preferred languages
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

}
