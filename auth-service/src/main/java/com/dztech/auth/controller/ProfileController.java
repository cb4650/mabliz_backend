package com.dztech.auth.controller;

import com.dztech.auth.dto.ChangeEmailRequest;
import com.dztech.auth.dto.ChangeEmailResponse;
import com.dztech.auth.dto.ChangeMobileRequest;
import com.dztech.auth.dto.ChangeMobileResponse;
import com.dztech.auth.dto.CustomerEmailOtpRequest;
import com.dztech.auth.dto.CustomerEmailOtpResponse;
import com.dztech.auth.dto.CustomerEmailVerificationRequest;
import com.dztech.auth.dto.CustomerEmailVerificationResponse;
import com.dztech.auth.dto.RequestEmailChangeOtpRequest;
import com.dztech.auth.dto.RequestEmailChangeOtpResponse;
import com.dztech.auth.dto.RequestMobileChangeOtpRequest;
import com.dztech.auth.dto.RequestMobileChangeOtpResponse;
import com.dztech.auth.dto.UpdateUserProfileRequest;
import com.dztech.auth.dto.PreferredLanguageListResponse;
import com.dztech.auth.dto.UpdateUserPreferredLanguagesRequest;
import com.dztech.auth.dto.UserProfileResponse;
import com.dztech.auth.dto.UserProfileUpdateResponse;
import com.dztech.auth.dto.UserProfileView;
import com.dztech.auth.dto.VerifyEmailOtpRequest;
import com.dztech.auth.security.AuthenticatedUserProvider;
import com.dztech.auth.security.JwtAuthenticationToken;
import com.dztech.auth.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public ProfileController(ProfileService profileService, AuthenticatedUserProvider authenticatedUserProvider) {
        this.profileService = profileService;
        this.authenticatedUserProvider = authenticatedUserProvider;
    }

    @GetMapping
    public ResponseEntity<UserProfileResponse> getProfile(Authentication authentication) {
        Long userId = authenticatedUserProvider.getCurrentUserId();
        String accessToken = resolveAccessToken(authentication);
        UserProfileView profile = profileService.getProfile(userId, accessToken);
        return ResponseEntity.ok(new UserProfileResponse(true, profile));
    }

    @GetMapping("/preferred-languages")
    public ResponseEntity<PreferredLanguageListResponse> getPreferredLanguages() {
        PreferredLanguageListResponse response = profileService.getPreferredLanguages();
        return ResponseEntity.ok(response);
    }


    @PostMapping("/request-email-change-otp")
    public ResponseEntity<RequestEmailChangeOtpResponse> requestEmailChangeOtp(
            Authentication authentication, @RequestBody @Valid RequestEmailChangeOtpRequest request) {
        Long userId = authenticatedUserProvider.getCurrentUserId();
        RequestEmailChangeOtpResponse response = profileService.requestEmailChangeOtp(userId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/request-mobile-change-otp")
    public ResponseEntity<RequestMobileChangeOtpResponse> requestMobileChangeOtp(
            Authentication authentication, @RequestBody @Valid RequestMobileChangeOtpRequest request) {
        Long userId = authenticatedUserProvider.getCurrentUserId();
        RequestMobileChangeOtpResponse response = profileService.requestMobileChangeOtp(userId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-mobile")
    public ResponseEntity<UserProfileResponse> verifyAndChangeMobile(
            Authentication authentication, @RequestBody @Valid ChangeMobileRequest request) {
        Long userId = authenticatedUserProvider.getCurrentUserId();
        String accessToken = resolveAccessToken(authentication);
        UserProfileView updatedProfile = profileService.verifyAndChangeMobile(userId, request, accessToken);
        return ResponseEntity.ok(new UserProfileResponse(true, updatedProfile));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<UserProfileResponse> verifyAndChangeEmail(
            Authentication authentication, @RequestBody @Valid ChangeEmailRequest request) {
        Long userId = authenticatedUserProvider.getCurrentUserId();
        String accessToken = resolveAccessToken(authentication);
        UserProfileView updatedProfile = profileService.verifyAndChangeEmail(userId, request, accessToken);
        return ResponseEntity.ok(new UserProfileResponse(true, updatedProfile));
    }

    @PostMapping("/email/otp")
    public ResponseEntity<CustomerEmailOtpResponse> requestEmailOtp(
            Authentication authentication, @RequestBody @Valid CustomerEmailOtpRequest request) {
        Long userId = authenticatedUserProvider.getCurrentUserId();
        CustomerEmailOtpResponse response = profileService.requestEmailOtp(userId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/email/verify")
    public ResponseEntity<CustomerEmailVerificationResponse> verifyEmailOtp(
            Authentication authentication, @RequestBody @Valid CustomerEmailVerificationRequest request) {
        Long userId = authenticatedUserProvider.getCurrentUserId();
        CustomerEmailVerificationResponse response = profileService.verifyEmailOtp(userId, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/preferred-languages")
    public ResponseEntity<UserProfileUpdateResponse> updatePreferredLanguages(
            Authentication authentication, @RequestBody @Valid UpdateUserPreferredLanguagesRequest request) {
        Long userId = authenticatedUserProvider.getCurrentUserId();
        UserProfileView updatedProfile = profileService.updatePreferredLanguages(userId, request);
        return ResponseEntity.ok(new UserProfileUpdateResponse(true, "Preferred languages updated successfully", updatedProfile));
    }

    private String resolveAccessToken(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtToken) {
            return jwtToken.getToken();
        }
        return null;
    }


    @PutMapping("/update")
    public ResponseEntity<UserProfileUpdateResponse> updateProfile(
            Authentication authentication, @RequestBody @Valid UpdateUserProfileRequest request) {
        Long userId = authenticatedUserProvider.getCurrentUserId();
        String accessToken = resolveAccessToken(authentication);
        UserProfileView updated = profileService.updateProfile(userId, request, accessToken);
        return ResponseEntity.ok(new UserProfileUpdateResponse(true, "Profile updated successfully", updated));
    }
}
