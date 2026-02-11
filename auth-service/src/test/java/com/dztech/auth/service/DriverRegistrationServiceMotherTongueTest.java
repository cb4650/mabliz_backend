package com.dztech.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dztech.auth.dto.DriverEmailOtpRequest;
import com.dztech.auth.dto.DriverEmailOtpResponse;
import com.dztech.auth.model.DriverProfile;
import com.dztech.auth.model.DriverProfileStatus;
import com.dztech.auth.repository.DriverProfileRepository;
import com.dztech.auth.repository.UserRepository;
import com.dztech.auth.security.JwtTokenService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DriverRegistrationServiceMotherTongueTest {

    @Mock
    private DriverEmailOtpService driverEmailOtpService;

    @Mock
    private DriverProfileRepository driverProfileRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtTokenService jwtTokenService;

    private DriverRegistrationService driverRegistrationService;

    @BeforeEach
    void setUp() {
        driverRegistrationService = new DriverRegistrationService(
                driverEmailOtpService, driverProfileRepository, userRepository, null, jwtTokenService);
    }

    @Test
    void requestEmailOtpUpdatesMotherTongueWhenProvidedAndNotAlreadySet() {
        Long userId = 5L;
        DriverEmailOtpRequest request = new DriverEmailOtpRequest(
                "Mugil", "mugil@example.com", null, null, null, "Tamil");
        
        DriverProfile profile = DriverProfile.builder()
                .userId(userId)
                .fullName("Mugil")
                .email("old@example.com")
                .motherTongue(null) // Mother tongue not set
                .status(DriverProfileStatus.PENDING)
                .build();

        when(driverProfileRepository.findByEmail("mugil@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("mugil@example.com")).thenReturn(Optional.empty());
        when(driverProfileRepository.findById(userId)).thenReturn(Optional.of(profile));
        when(driverProfileRepository.save(any(DriverProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(driverEmailOtpService.getExpirySeconds()).thenReturn(300L);

        DriverEmailOtpResponse response = driverRegistrationService.requestEmailOtp(userId, request);

        verify(driverEmailOtpService).sendOtp(userId, "mugil@example.com", "Mugil");
        assertThat(response.success()).isTrue();
        assertThat(profile.getMotherTongue()).isEqualTo("Tamil");
    }

    @Test
    void requestEmailOtpDoesNotUpdateMotherTongueWhenAlreadySet() {
        Long userId = 5L;
        DriverEmailOtpRequest request = new DriverEmailOtpRequest(
                "Mugil", "mugil@example.com", null, null, null, "Tamil");
        
        DriverProfile profile = DriverProfile.builder()
                .userId(userId)
                .fullName("Mugil")
                .email("old@example.com")
                .motherTongue("Hindi") // Mother tongue already set
                .status(DriverProfileStatus.PENDING)
                .build();

        when(driverProfileRepository.findByEmail("mugil@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("mugil@example.com")).thenReturn(Optional.empty());
        when(driverProfileRepository.findById(userId)).thenReturn(Optional.of(profile));
        when(driverProfileRepository.save(any(DriverProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(driverEmailOtpService.getExpirySeconds()).thenReturn(300L);

        DriverEmailOtpResponse response = driverRegistrationService.requestEmailOtp(userId, request);

        verify(driverEmailOtpService).sendOtp(userId, "mugil@example.com", "Mugil");
        assertThat(response.success()).isTrue();
        assertThat(profile.getMotherTongue()).isEqualTo("Hindi"); // Should remain unchanged
    }

    @Test
    void requestEmailOtpDoesNotUpdateMotherTongueWhenNotProvided() {
        Long userId = 5L;
        DriverEmailOtpRequest request = new DriverEmailOtpRequest(
                "Mugil", "mugil@example.com", null, null, null, null);
        
        DriverProfile profile = DriverProfile.builder()
                .userId(userId)
                .fullName("Mugil")
                .email("old@example.com")
                .motherTongue("Hindi") // Mother tongue already set
                .status(DriverProfileStatus.PENDING)
                .build();

        when(driverProfileRepository.findByEmail("mugil@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("mugil@example.com")).thenReturn(Optional.empty());
        when(driverProfileRepository.findById(userId)).thenReturn(Optional.of(profile));
        when(driverProfileRepository.save(any(DriverProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(driverEmailOtpService.getExpirySeconds()).thenReturn(300L);

        DriverEmailOtpResponse response = driverRegistrationService.requestEmailOtp(userId, request);

        verify(driverEmailOtpService).sendOtp(userId, "mugil@example.com", "Mugil");
        assertThat(response.success()).isTrue();
        assertThat(profile.getMotherTongue()).isEqualTo("Hindi"); // Should remain unchanged
    }
}