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
                userProfileRepository, rayderVehicleClient, preferredLanguageRepository, emailOtpService, null);
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

}
