package com.dztech.auth.service;

import com.dztech.auth.dto.CustomerEmailOtpRequest;
import com.dztech.auth.dto.CustomerEmailOtpResponse;
import com.dztech.auth.exception.ResourceNotFoundException;
import com.dztech.auth.model.UserProfile;
import com.dztech.auth.repository.UserProfileRepository;
import com.dztech.auth.service.EmailOtpService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceEmailOtpTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private EmailOtpService emailOtpService;

    @InjectMocks
    private ProfileService profileService;

    private UserProfile existingProfile;

    @BeforeEach
    void setUp() {
        existingProfile = new UserProfile();
        existingProfile.setUserId(1L);
        existingProfile.setName("Old Name");
        existingProfile.setPhone("1234567890");
        existingProfile.setEmail("old@example.com");
        existingProfile.setEmailVerified(false);
    }

    @Test
    void testRequestEmailOtp_NameUpdated() {
        // Arrange
        Long userId = 1L;
        String newEmail = "newemail@example.com";
        String newName = "New Name";

        CustomerEmailOtpRequest request = new CustomerEmailOtpRequest(newName, newEmail);

        when(userProfileRepository.findByUserId(userId)).thenReturn(java.util.Optional.of(existingProfile));
        when(userProfileRepository.existsByEmailAndUserIdNot(eq(newEmail), eq(userId))).thenReturn(false);

        // Act
        CustomerEmailOtpResponse response = profileService.requestEmailOtp(userId, request);

        // Assert
        assertNotNull(response);
        assertTrue(response.success());
        assertEquals("OTP sent to email successfully", response.message());
        assertEquals(newEmail, response.data().email());
        
        // Verify that the profile name was updated
        assertEquals(newName, existingProfile.getName());
        
        // Verify that OTP was sent with the updated name
        verify(emailOtpService).sendVerificationOtp(eq(userId), eq(newEmail), eq(newName));
    }

    @Test
    void testRequestEmailOtp_NameNotUpdatedWhenSame() {
        // Arrange
        Long userId = 1L;
        String newEmail = "newemail@example.com";
        String sameName = "Old Name"; // Same as existing profile name

        CustomerEmailOtpRequest request = new CustomerEmailOtpRequest(sameName, newEmail);

        when(userProfileRepository.findByUserId(userId)).thenReturn(java.util.Optional.of(existingProfile));
        when(userProfileRepository.existsByEmailAndUserIdNot(eq(newEmail), eq(userId))).thenReturn(false);

        // Act
        CustomerEmailOtpResponse response = profileService.requestEmailOtp(userId, request);

        // Assert
        assertNotNull(response);
        assertTrue(response.success());
        
        // Verify that the profile name was not changed (still the same)
        assertEquals("Old Name", existingProfile.getName());
        
        // Verify that OTP was sent with the existing name
        verify(emailOtpService).sendVerificationOtp(eq(userId), eq(newEmail), eq("Old Name"));
    }

    @Test
    void testRequestEmailOtp_EmailAlreadyInUse() {
        // Arrange
        Long userId = 1L;
        String newEmail = "existing@example.com";
        String newName = "New Name";

        CustomerEmailOtpRequest request = new CustomerEmailOtpRequest(newName, newEmail);

        when(userProfileRepository.findByUserId(userId)).thenReturn(java.util.Optional.of(existingProfile));
        when(userProfileRepository.existsByEmailAndUserIdNot(eq(newEmail), eq(userId))).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            profileService.requestEmailOtp(userId, request);
        });

        assertEquals("Email is already in use by another account", exception.getMessage());
        
        // Verify that OTP was not sent
        verify(emailOtpService, never()).sendVerificationOtp(anyLong(), anyString(), anyString());
    }

    @Test
    void testRequestEmailOtp_UserNotFound() {
        // Arrange
        Long userId = 1L;
        String newEmail = "newemail@example.com";
        String newName = "New Name";

        CustomerEmailOtpRequest request = new CustomerEmailOtpRequest(newName, newEmail);

        when(userProfileRepository.findByUserId(userId)).thenReturn(java.util.Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            profileService.requestEmailOtp(userId, request);
        });

        assertEquals("User profile not found", exception.getMessage());
        
        // Verify that OTP was not sent
        verify(emailOtpService, never()).sendVerificationOtp(anyLong(), anyString(), anyString());
    }
}