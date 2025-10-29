package com.dztech.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dztech.auth.client.RayderVehicleClient;
import com.dztech.auth.dto.UpdateUserProfileRequest;
import com.dztech.auth.dto.UserProfileView;
import com.dztech.auth.dto.VerifyEmailOtpRequest;
import com.dztech.auth.model.UserProfile;
import com.dztech.auth.repository.PreferredLanguageRepository;
import com.dztech.auth.repository.UserProfileRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private RayderVehicleClient rayderVehicleClient;

    @Mock
    private PreferredLanguageRepository preferredLanguageRepository;

    @Mock
    private EmailOtpService emailOtpService;

    private ProfileService profileService;

    @BeforeEach
    void setUp() {
        profileService = new ProfileService(
                userProfileRepository, rayderVehicleClient, preferredLanguageRepository, emailOtpService);
    }

    @Test
    void getProfileReturnsAggregatedView() {
        UserProfile profile = UserProfile.builder()
                .userId(1L)
                .name("Elamugil M")
                .phone("+919876543210")
                .email("elamugil@example.com")
                .emailVerified(true)
                .address("123, MG Road, Chennai")
                .build();

        when(userProfileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(rayderVehicleClient.fetchVehicleCount("token")).thenReturn(2L);

        UserProfileView view = profileService.getProfile(1L, "token");

        assertThat(view.id()).isEqualTo(1L);
        assertThat(view.name()).isEqualTo("Elamugil M");
        assertThat(view.phone()).isEqualTo("+919876543210");
        assertThat(view.email()).isEqualTo("elamugil@example.com");
        assertThat(view.emailVerified()).isTrue();
        assertThat(view.address()).isEqualTo("123, MG Road, Chennai");
        assertThat(view.vehicleCount()).isEqualTo(2L);
        assertThat(view.completedBookings()).isZero();
        assertThat(view.activeBookings()).isZero();
    }

    @Test
    void updateProfileUpdatesMutableFieldsOnly() {
        UserProfile profile = UserProfile.builder()
                .userId(1L)
                .name("Elamugil M")
                .phone("+919876543210")
                .email("elamugil@example.com")
                .address("Old Address")
                .build();

        when(userProfileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(rayderVehicleClient.fetchVehicleCount("token")).thenReturn(3L);

        UpdateUserProfileRequest request = new UpdateUserProfileRequest(
                "Elamugil M", "UPDATED@example.com", "New Address", null, null);

        UserProfileView view = profileService.updateProfile(1L, request, "token");

        verify(userProfileRepository).save(profile);
        verify(emailOtpService).sendVerificationOtp(eq(1L), eq("updated@example.com"), eq("Elamugil M"));
        assertThat(profile.getPhone()).isEqualTo("+919876543210");
        assertThat(view.email()).isEqualTo("updated@example.com");
        assertThat(view.address()).isEqualTo("New Address");
        assertThat(view.emailVerified()).isFalse();
        assertThat(view.vehicleCount()).isEqualTo(3L);
        assertThat(view.completedBookings()).isZero();
        assertThat(view.activeBookings()).isZero();
    }

    @Test
    void updateProfileRejectsBlankName() {
        UserProfile profile = UserProfile.builder()
                .userId(1L)
                .name("Existing")
                .phone("+919876543210")
                .email("existing@example.com")
                .address(null)
                .build();

        when(userProfileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));

        UpdateUserProfileRequest request = new UpdateUserProfileRequest("  ", null, null, null, null);

        assertThatThrownBy(() -> profileService.updateProfile(1L, request, "token"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Name cannot be blank");
    }

    @Test
    void verifyEmailMarksProfileVerified() {
        UserProfile profile = UserProfile.builder()
                .userId(1L)
                .name("Existing")
                .phone("+919876543210")
                .email("existing@example.com")
                .emailVerified(false)
                .build();

        when(userProfileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(rayderVehicleClient.fetchVehicleCount("token")).thenReturn(0L);

        VerifyEmailOtpRequest request = new VerifyEmailOtpRequest("existing@example.com", "123456");

        UserProfileView view = profileService.verifyEmail(1L, request, "token");

        verify(emailOtpService).verifyOtp(1L, "existing@example.com", "123456");
        verify(userProfileRepository).save(profile);
        assertThat(profile.isEmailVerified()).isTrue();
        assertThat(view.emailVerified()).isTrue();
    }
}
