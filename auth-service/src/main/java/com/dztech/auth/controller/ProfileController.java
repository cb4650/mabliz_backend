package com.dztech.auth.controller;

import com.dztech.auth.dto.ChangeEmailRequest;
import com.dztech.auth.dto.ChangeEmailResponse;
import com.dztech.auth.dto.ChangeMobileRequest;
import com.dztech.auth.dto.ChangeMobileResponse;
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

    @PutMapping("/email/change")
    public ResponseEntity<ChangeEmailResponse> changeEmail(
            Authentication authentication, @RequestBody @Valid ChangeEmailRequest request) {
        Long userId = authenticatedUserProvider.getCurrentUserId();
        String accessToken = resolveAccessToken(authentication);
        try {
            UserProfileView updated = profileService.changeEmail(userId, request, accessToken);
            return ResponseEntity.ok(new ChangeEmailResponse(true, "Email change request successful. Please verify the new email.", updated));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest()
                    .body(new ChangeEmailResponse(false, ex.getMessage(), null));
        }
    }

    @PutMapping("/phone/change")
    public ResponseEntity<ChangeMobileResponse> changeMobile(
            Authentication authentication, @RequestBody @Valid ChangeMobileRequest request) {
        Long userId = authenticatedUserProvider.getCurrentUserId();
        String accessToken = resolveAccessToken(authentication);
        try {
            UserProfileView updated = profileService.changeMobile(userId, request, accessToken);
            return ResponseEntity.ok(new ChangeMobileResponse(true, "Phone number changed successfully.", updated));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest()
                    .body(new ChangeMobileResponse(false, ex.getMessage(), null));
        }
    }

    private String resolveAccessToken(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtToken) {
            return jwtToken.getToken();
        }
        return null;
    }
}
