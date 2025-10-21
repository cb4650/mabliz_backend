package com.dztech.auth.controller;

import com.dztech.auth.dto.UpdateUserProfileRequest;
import com.dztech.auth.dto.UserProfileResponse;
import com.dztech.auth.dto.UserProfileUpdateResponse;
import com.dztech.auth.dto.UserProfileView;
import com.dztech.auth.security.AuthenticatedUserProvider;
import com.dztech.auth.security.JwtAuthenticationToken;
import com.dztech.auth.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
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

    @PutMapping("/update")
    public ResponseEntity<UserProfileUpdateResponse> updateProfile(
            Authentication authentication, @RequestBody @Valid UpdateUserProfileRequest request) {
        Long userId = authenticatedUserProvider.getCurrentUserId();
        String accessToken = resolveAccessToken(authentication);
        UserProfileView updated = profileService.updateProfile(userId, request, accessToken);
        return ResponseEntity.ok(new UserProfileUpdateResponse(true, "Profile updated successfully", updated));
    }

    private String resolveAccessToken(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtToken) {
            return jwtToken.getToken();
        }
        return null;
    }
}
