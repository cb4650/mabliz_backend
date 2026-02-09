package com.dztech.auth.controller;

import com.dztech.auth.dto.ChangeEmailRequest;
import com.dztech.auth.dto.ChangeEmailResponse;
import com.dztech.auth.dto.ChangeMobileRequest;
import com.dztech.auth.dto.ChangeMobileResponse;
import com.dztech.auth.dto.RequestEmailChangeOtpRequest;
import com.dztech.auth.dto.RequestMobileChangeOtpRequest;
import com.dztech.auth.dto.DriverChangeEmailResponse;
import com.dztech.auth.dto.DriverChangeMobileResponse;
import com.dztech.auth.dto.DriverEmailOtpRequest;
import com.dztech.auth.dto.DriverEmailOtpResponse;
import com.dztech.auth.dto.DriverEmailVerificationRequest;
import com.dztech.auth.dto.DriverEmailVerificationResponse;
import com.dztech.auth.dto.DriverProfileResponse;
import com.dztech.auth.dto.DriverProfileUpdateForm;
import com.dztech.auth.dto.DriverProfileUpdateResponse;
import com.dztech.auth.dto.DriverProfileView;
import com.dztech.auth.exception.EmailOtpException;
import com.dztech.auth.security.AuthenticatedUserProvider;
import com.dztech.auth.service.DriverProfileService;
import com.dztech.auth.service.DriverRegistrationService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/driver")
public class DriverProfileController {

    private final DriverRegistrationService driverRegistrationService;
    private final DriverProfileService driverProfileService;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public DriverProfileController(
            DriverRegistrationService driverRegistrationService,
            DriverProfileService driverProfileService,
            AuthenticatedUserProvider authenticatedUserProvider) {
        this.driverRegistrationService = driverRegistrationService;
        this.driverProfileService = driverProfileService;
        this.authenticatedUserProvider = authenticatedUserProvider;
    }

    @PostMapping("/email/otp")
    public ResponseEntity<DriverEmailOtpResponse> requestEmailOtp(
            Authentication authentication, @RequestBody @Valid DriverEmailOtpRequest request) {
        try {
            Long userId = authenticatedUserProvider.getCurrentUserId();
            DriverEmailOtpResponse response = driverRegistrationService.requestEmailOtp(userId, request);
            return ResponseEntity.ok(response);
        } catch (IllegalStateException ex) {
            DriverEmailOtpResponse error =
                    new DriverEmailOtpResponse(false, "Authentication is required", null);
            return ResponseEntity.status(401).body(error);
        } catch (IllegalArgumentException | EmailOtpException ex) {
            DriverEmailOtpResponse error = new DriverEmailOtpResponse(false, ex.getMessage(), null);
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/email/verify")
    public ResponseEntity<DriverEmailVerificationResponse> verifyEmailOtp(
            Authentication authentication, @RequestBody @Valid DriverEmailVerificationRequest request) {
        try {
            Long userId = authenticatedUserProvider.getCurrentUserId();
            DriverEmailVerificationResponse response = driverRegistrationService.verifyEmailOtp(userId, request);
            return ResponseEntity.ok(response);
        } catch (IllegalStateException ex) {
            DriverEmailVerificationResponse error =
                    new DriverEmailVerificationResponse(false, "Authentication is required", null);
            return ResponseEntity.status(401).body(error);
        } catch (IllegalArgumentException | EmailOtpException ex) {
            DriverEmailVerificationResponse error = new DriverEmailVerificationResponse(false, ex.getMessage(), null);
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<DriverProfileResponse> getProfile(Authentication authentication) {
        Long userId = authenticatedUserProvider.getCurrentUserId();
        DriverProfileView profile = driverProfileService.getProfile(userId);
        return ResponseEntity.ok(new DriverProfileResponse(true, profile));
    }

    @PutMapping(
            value = "/profile",
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<DriverProfileUpdateResponse> updateProfile(
            Authentication authentication, @Valid @ModelAttribute DriverProfileUpdateForm form) {
        Long userId = authenticatedUserProvider.getCurrentUserId();
        try {
            DriverProfileView updated = driverProfileService.updateProfile(userId, form);
            return ResponseEntity.ok(new DriverProfileUpdateResponse(true, "Profile updated successfully", updated));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest()
                    .body(new DriverProfileUpdateResponse(false, ex.getMessage(), null));
        }
    }

    @PostMapping("/request-email-change-otp")
    public ResponseEntity<DriverChangeEmailResponse> requestEmailChangeOtp(
            Authentication authentication, @RequestBody @Valid RequestEmailChangeOtpRequest request) {
        Long userId = authenticatedUserProvider.getCurrentUserId();
        try {
            DriverProfileView profile = driverProfileService.requestEmailChangeOtp(userId, request);
            return ResponseEntity.ok(new DriverChangeEmailResponse(true, "OTP sent to your current phone number for email change verification", profile));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest()
                    .body(new DriverChangeEmailResponse(false, ex.getMessage(), null));
        }
    }

    @PostMapping("/request-mobile-change-otp")
    public ResponseEntity<DriverChangeMobileResponse> requestMobileChangeOtp(
            Authentication authentication, @RequestBody @Valid RequestMobileChangeOtpRequest request) {
        Long userId = authenticatedUserProvider.getCurrentUserId();
        try {
            DriverProfileView profile = driverProfileService.requestMobileChangeOtp(userId, request);
            return ResponseEntity.ok(new DriverChangeMobileResponse(true, "OTP sent to your current email for mobile change verification", profile));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest()
                    .body(new DriverChangeMobileResponse(false, ex.getMessage(), null));
        }
    }

    @PostMapping("/verify-mobile")
    public ResponseEntity<DriverChangeMobileResponse> verifyAndChangeMobile(
            Authentication authentication, @RequestBody @Valid ChangeMobileRequest request) {
        Long userId = authenticatedUserProvider.getCurrentUserId();
        try {
            DriverProfileView updated = driverProfileService.verifyAndChangeMobile(userId, request);
            return ResponseEntity.ok(new DriverChangeMobileResponse(true, "Phone number changed successfully.", updated));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest()
                    .body(new DriverChangeMobileResponse(false, ex.getMessage(), null));
        }
    }

    @PostMapping("/verify-email")
    public ResponseEntity<DriverChangeEmailResponse> verifyAndChangeEmail(
            Authentication authentication, @RequestBody @Valid ChangeEmailRequest request) {
        Long userId = authenticatedUserProvider.getCurrentUserId();
        try {
            DriverProfileView updated = driverProfileService.verifyAndChangeEmail(userId, request);
            return ResponseEntity.ok(new DriverChangeEmailResponse(true, "Email change request successful. Please verify the new email.", updated));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest()
                    .body(new DriverChangeEmailResponse(false, ex.getMessage(), null));
        }
    }

}
