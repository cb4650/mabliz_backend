package com.dztech.auth.controller;

import com.dztech.auth.dto.ChangeEmailRequest;
import com.dztech.auth.dto.ChangeEmailResponse;
import com.dztech.auth.dto.ChangeMobileRequest;
import com.dztech.auth.dto.ChangeMobileResponse;
import com.dztech.auth.dto.UserProfileView;
import com.dztech.auth.service.ProfileService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileControllerTest {

    @Mock
    private ProfileService profileService;


    @InjectMocks
    private ProfileController profileController;

    @Test
    void testChangeEmail_Success() {
        // Arrange
        Long userId = 1L;
        String newEmail = "newemail@example.com";
        String phoneOtp = "123456";
        String accessToken = "access_token";

        UserProfileView updatedProfile = new UserProfileView(
                userId, "Test User", "9876543210", newEmail, false, 
                "Address", null, null, 0L, 0L, 0L);

        ChangeEmailRequest request = new ChangeEmailRequest(newEmail, phoneOtp);

        when(profileService.changeEmail(eq(userId), any(ChangeEmailRequest.class), eq(accessToken)))
                .thenReturn(updatedProfile);

        // Act
        ResponseEntity<ChangeEmailResponse> response = profileController.changeEmail(
                mock(Authentication.class), request);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        ChangeEmailResponse responseBody = response.getBody();
        assertNotNull(responseBody);
        assertTrue(responseBody.success());
        assertEquals("Email change request successful. Please verify the new email.", responseBody.message());
        assertEquals(updatedProfile, responseBody.profile());
    }

    @Test
    void testChangeEmail_InvalidOtp() {
        // Arrange
        Long userId = 1L;
        String newEmail = "newemail@example.com";
        String phoneOtp = "123456";
        String accessToken = "access_token";

        ChangeEmailRequest request = new ChangeEmailRequest(newEmail, phoneOtp);

        when(profileService.changeEmail(eq(userId), any(ChangeEmailRequest.class), eq(accessToken)))
                .thenThrow(new IllegalArgumentException("Invalid phone OTP"));

        // Act
        ResponseEntity<ChangeEmailResponse> response = profileController.changeEmail(
                mock(Authentication.class), request);

        // Assert
        assertNotNull(response);
        assertEquals(400, response.getStatusCodeValue());
        ChangeEmailResponse responseBody = response.getBody();
        assertNotNull(responseBody);
        assertFalse(responseBody.success());
        assertEquals("Invalid phone OTP", responseBody.message());
        assertNull(responseBody.profile());
    }

    @Test
    void testChangeMobile_Success() {
        // Arrange
        Long userId = 1L;
        String newPhone = "1234567890";
        String emailOtp = "654321";
        String accessToken = "access_token";

        UserProfileView updatedProfile = new UserProfileView(
                userId, "Test User", newPhone, "user@example.com", true, 
                "Address", null, null, 0L, 0L, 0L);

        ChangeMobileRequest request = new ChangeMobileRequest(newPhone, emailOtp);

        when(profileService.changeMobile(eq(userId), any(ChangeMobileRequest.class), eq(accessToken)))
                .thenReturn(updatedProfile);

        // Act
        ResponseEntity<ChangeMobileResponse> response = profileController.changeMobile(
                mock(Authentication.class), request);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        ChangeMobileResponse responseBody = response.getBody();
        assertNotNull(responseBody);
        assertTrue(responseBody.success());
        assertEquals("Phone number changed successfully.", responseBody.message());
        assertEquals(updatedProfile, responseBody.profile());
    }
}