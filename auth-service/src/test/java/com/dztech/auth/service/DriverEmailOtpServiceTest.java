package com.dztech.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dztech.auth.client.EmailOtpSender;
import com.dztech.auth.exception.EmailOtpException;
import com.dztech.auth.model.DriverEmailVerificationToken;
import com.dztech.auth.repository.DriverEmailVerificationTokenRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DriverEmailOtpServiceTest {

    @Mock
    private DriverEmailVerificationTokenRepository tokenRepository;

    @Mock
    private EmailOtpSender emailOtpSender;

    private DriverEmailOtpService service;

    @BeforeEach
    void setUp() {
        service = new DriverEmailOtpService(tokenRepository, emailOtpSender, 300L, 6);
    }

    @Test
    void sendOtpThrowsWhenEmailBelongsToOtherDriver() {
        DriverEmailVerificationToken existing = DriverEmailVerificationToken.builder()
                .id(10L)
                .userId(3L)
                .email("claimed@example.com")
                .name("Claimed User")
                .build();
        existing.stampCreation(Instant.now().minusSeconds(600));

        when(tokenRepository.findByEmail("claimed@example.com")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.sendOtp(5L, "claimed@example.com", "Driver"))
                .isInstanceOf(EmailOtpException.class)
                .hasMessageContaining("Email already in use by another driver");

        verify(tokenRepository, never()).findByUserId(any());
        verify(tokenRepository, never()).save(any());
        verify(emailOtpSender, never()).sendOtp(any(), any(), any());
    }

    @Test
    void sendOtpRefreshesTokenWhenEmailAlreadyAssignedToSameDriver() {
        Instant originalCreatedAt = Instant.now().minusSeconds(1200);
        DriverEmailVerificationToken existing = DriverEmailVerificationToken.builder()
                .id(14L)
                .userId(5L)
                .email("driver@example.com")
                .name("Old Name")
                .otpCode("111111")
                .expiresAt(Instant.now().minusSeconds(60))
                .verifiedAt(Instant.now().minusSeconds(30))
                .build();
        existing.stampCreation(originalCreatedAt);

        when(tokenRepository.findByEmail("driver@example.com")).thenReturn(Optional.of(existing));
        when(tokenRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.sendOtp(5L, "driver@example.com", "Driver Name");

        ArgumentCaptor<DriverEmailVerificationToken> tokenCaptor =
                ArgumentCaptor.forClass(DriverEmailVerificationToken.class);
        verify(tokenRepository).save(tokenCaptor.capture());
        DriverEmailVerificationToken saved = tokenCaptor.getValue();

        assertThat(saved.getId()).isEqualTo(14L);
        assertThat(saved.getUserId()).isEqualTo(5L);
        assertThat(saved.getEmail()).isEqualTo("driver@example.com");
        assertThat(saved.getName()).isEqualTo("Driver Name");
        assertThat(saved.getOtpCode()).matches("\\d{6}");
        assertThat(saved.getExpiresAt()).isAfter(Instant.now().minusSeconds(5));
        assertThat(saved.getVerifiedAt()).isNull();
        assertThat(saved.getCreatedAt()).isEqualTo(originalCreatedAt);
        assertThat(saved.getUpdatedAt()).isNotNull();

        verify(emailOtpSender).sendOtp("driver@example.com", "Driver Name", saved.getOtpCode());
    }

    @Test
    void sendOtpUpdatesExistingTokenWhenDriverChangesEmailBeforeVerification() {
        DriverEmailVerificationToken existing = DriverEmailVerificationToken.builder()
                .id(20L)
                .userId(5L)
                .email("old@example.com")
                .name("Driver Old")
                .otpCode("222222")
                .expiresAt(Instant.now().minusSeconds(30))
                .build();
        existing.stampCreation(Instant.now().minusSeconds(1800));

        when(tokenRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(tokenRepository.findByUserId(5L)).thenReturn(Optional.of(existing));
        when(tokenRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.sendOtp(5L, "new@example.com", "Driver New");

        ArgumentCaptor<DriverEmailVerificationToken> tokenCaptor =
                ArgumentCaptor.forClass(DriverEmailVerificationToken.class);
        verify(tokenRepository).save(tokenCaptor.capture());
        DriverEmailVerificationToken saved = tokenCaptor.getValue();

        assertThat(saved.getId()).isEqualTo(20L);
        assertThat(saved.getUserId()).isEqualTo(5L);
        assertThat(saved.getEmail()).isEqualTo("new@example.com");
        assertThat(saved.getName()).isEqualTo("Driver New");
        assertThat(saved.getOtpCode()).matches("\\d{6}");
        assertThat(saved.getVerifiedAt()).isNull();

        verify(emailOtpSender).sendOtp("new@example.com", "Driver New", saved.getOtpCode());
    }
}
