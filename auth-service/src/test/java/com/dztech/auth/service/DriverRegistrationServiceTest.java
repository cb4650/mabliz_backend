package com.dztech.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dztech.auth.dto.DriverEmailOtpRequest;
import com.dztech.auth.dto.DriverEmailOtpResponse;
import com.dztech.auth.dto.DriverEmailVerificationRequest;
import com.dztech.auth.dto.DriverEmailVerificationResponse;
import com.dztech.auth.model.DriverEmailVerificationToken;
import com.dztech.auth.model.DriverProfile;
import com.dztech.auth.model.DriverProfileStatus;
import com.dztech.auth.model.User;
import com.dztech.auth.model.UserProfile;
import com.dztech.auth.repository.DriverProfileRepository;
import com.dztech.auth.repository.UserProfileRepository;
import com.dztech.auth.repository.UserRepository;
import com.dztech.auth.security.JwtTokenService;
import com.dztech.auth.security.JwtTokenService.JwtTokenPayload;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DriverRegistrationServiceTest {

    @Mock
    private DriverEmailOtpService driverEmailOtpService;

    @Mock
    private DriverProfileRepository driverProfileRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private JwtTokenService jwtTokenService;

    private DriverRegistrationService driverRegistrationService;

    @BeforeEach
    void setUp() {
        driverRegistrationService = new DriverRegistrationService(
                driverEmailOtpService, driverProfileRepository, userRepository, userProfileRepository, jwtTokenService);
    }

    @Test
    void requestEmailOtpSendsOtpWhenEmailNotRegistered() {
        Long userId = 5L;
        DriverEmailOtpRequest request = new DriverEmailOtpRequest("Mugil", "mugil@example.com", null, null, null);
        when(driverProfileRepository.findByEmail("mugil@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("mugil@example.com")).thenReturn(Optional.empty());
        when(driverEmailOtpService.getExpirySeconds()).thenReturn(300L);

        DriverEmailOtpResponse response = driverRegistrationService.requestEmailOtp(userId, request);

        verify(driverEmailOtpService).sendOtp(userId, "mugil@example.com", "Mugil");
        assertThat(response.success()).isTrue();
        assertThat(response.data().otpExpiresIn()).isEqualTo(300L);
    }

    @Test
    void requestEmailOtpRejectsAlreadyRegisteredEmail() {
        Long userId = 5L;
        DriverEmailOtpRequest request = new DriverEmailOtpRequest("Mugil", "mugil@example.com", null, null, null);
        when(driverProfileRepository.findByEmail("mugil@example.com"))
                .thenReturn(Optional.of(
                        DriverProfile.builder().userId(1L).status(DriverProfileStatus.PENDING).build()));

        assertThatThrownBy(() -> driverRegistrationService.requestEmailOtp(userId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid email address or email already registered");
    }

    @Test
    void verifyEmailOtpReturnsTokenForExistingDriver() {
        Long userId = 5L;
        DriverEmailVerificationRequest request = new DriverEmailVerificationRequest("mugil@example.com", "123456");
        DriverEmailVerificationToken token = DriverEmailVerificationToken.builder()
                .userId(userId)
                .email("mugil@example.com")
                .name("Mugil M")
                .otpCode("123456")
                .expiresAt(Instant.now().plusSeconds(300))
                .build();
        User user = User.builder().id(5L).username("driver1").email("mugil@example.com").passwordHash("pw").build();
        DriverProfile profile = DriverProfile.builder()
                .userId(5L)
                .email("mugil@example.com")
                .fullName("Mugil M")
                .status(DriverProfileStatus.PENDING)
                .build();

        when(driverEmailOtpService.verifyOtp(userId, "mugil@example.com", "123456")).thenReturn(token);
        when(userRepository.findByEmail("mugil@example.com")).thenReturn(Optional.of(user));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(driverProfileRepository.findById(5L)).thenReturn(Optional.of(profile));
        when(driverProfileRepository.save(any(DriverProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userProfileRepository.findByUserId(5L)).thenReturn(Optional.of(UserProfile.builder()
                .userId(5L)
                .name("Mugil M")
                .phone("9876543210")
                .email("mugil@example.com")
                .emailVerified(true)
                .build()));
        when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtTokenService.generateAccessToken(any(JwtTokenPayload.class))).thenReturn("token-123");

        DriverEmailVerificationResponse response = driverRegistrationService.verifyEmailOtp(userId, request);

        assertThat(response.success()).isTrue();
        assertThat(response.data().token()).isEqualTo("token-123");
        assertThat(response.data().user().email()).isEqualTo("mugil@example.com");
    }
}
