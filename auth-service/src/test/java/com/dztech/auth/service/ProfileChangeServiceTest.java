package com.dztech.auth.service;

import com.dztech.auth.client.OtpProviderClient;
import com.dztech.auth.client.RayderVehicleClient;
import com.dztech.auth.dto.ChangeEmailRequest;
import com.dztech.auth.dto.ChangeMobileRequest;
import com.dztech.auth.dto.UserProfileView;
import com.dztech.auth.model.UserProfile;
import com.dztech.auth.repository.PreferredLanguageRepository;
import com.dztech.auth.repository.UserProfileRepository;
import com.dztech.auth.service.EmailOtpService;
import com.dztech.auth.service.ProfileService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileChangeServiceTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private RayderVehicleClient rayderVehicleClient;

    @Mock
    private PreferredLanguageRepository preferredLanguageRepository;

    @Mock
    private EmailOtpService emailOtpService;

    @Mock
    private OtpProviderClient otpProviderClient;

    @InjectMocks
    private ProfileService profileService;

    @Test
    void testChangeEmail_Success() {
        // Arrange
        Long userId = 1L;
        String newEmail = "newemail@example.com";
        String phoneOtp = "123456";
        String currentPhone = "9876543210";

        UserProfile existingProfile = new UserProfile();
        existingProfile.setUserId(userId);
        existingProfile.setEmail("oldemail@example.com");
        existingProfile.setPhone(currentPhone);

        when(userProfileRepository.findByUserId(userId)).thenReturn(java.util.Optional.of(existingProfile));
        when(userProfileRepository.existsByEmailAndUserIdNot(anyString(), anyLong())).thenReturn(false);
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(existingProfile);

        // Act
        UserProfileView result = profileService.changeEmail(userId, 
            new ChangeEmailRequest(newEmail, phoneOtp), "access_token");

        // Assert
        assertNotNull(result);
        assertEquals(newEmail, result.email());
        verify(otpProviderClient).verifyOtp(currentPhone, phoneOtp);
        verify(emailOtpService).sendVerificationOtp(userId, newEmail, null);
    }

    @Test
    void testChangeMobile_Success() {
        // Arrange
        Long userId = 1L;
        String newPhone = "1234567890";
        String emailOtp = "654321";
        String currentEmail = "user@example.com";

        UserProfile existingProfile = new UserProfile();
        existingProfile.setUserId(userId);
        existingProfile.setEmail(currentEmail);
        existingProfile.setPhone("9876543210");

        when(userProfileRepository.findByUserId(userId)).thenReturn(java.util.Optional.of(existingProfile));
        when(userProfileRepository.existsByPhoneAndUserIdNot(anyString(), anyLong())).thenReturn(false);
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(existingProfile);

        // Act
        UserProfileView result = profileService.changeMobile(userId, 
            new ChangeMobileRequest(newPhone, emailOtp), "access_token");

        // Assert
        assertNotNull(result);
        assertEquals(newPhone, result.phone());
        verify(emailOtpService).verifyOtp(userId, currentEmail, emailOtp);
    }

    @Test
    void testChangeEmail_EmailAlreadyInUse() {
        // Arrange
        Long userId = 1L;
        String newEmail = "newemail@example.com";
        String phoneOtp = "123456";

        UserProfile existingProfile = new UserProfile();
        existingProfile.setUserId(userId);
        existingProfile.setEmail("oldemail@example.com");
        existingProfile.setPhone("9876543210");

        when(userProfileRepository.findByUserId(userId)).thenReturn(java.util.Optional.of(existingProfile));
        when(userProfileRepository.existsByEmailAndUserIdNot(newEmail, userId)).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            profileService.changeEmail(userId, new ChangeEmailRequest(newEmail, phoneOtp), "access_token");
        });

        assertEquals("Email is already in use by another account", exception.getMessage());
    }
}