package com.dztech.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dztech.auth.dto.DriverProfileUpdateForm;
import com.dztech.auth.dto.DriverProfileView;
import com.dztech.auth.model.DriverProfile;
import com.dztech.auth.model.DriverProfileStatus;
import com.dztech.auth.model.UserProfile;
import com.dztech.auth.repository.DriverProfileRepository;
import com.dztech.auth.repository.UserProfileRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class DriverProfileServiceTest {

    @Mock
    private DriverProfileRepository driverProfileRepository;

    @Mock
    private DriverEmailOtpService driverEmailOtpService;

    @Mock
    private UserProfileRepository userProfileRepository;

    private DriverProfileService driverProfileService;

    @BeforeEach
    void setUp() {
        when(userProfileRepository.findByUserId(anyLong())).thenReturn(Optional.empty());
        driverProfileService =
                new DriverProfileService(driverProfileRepository, driverEmailOtpService, userProfileRepository);
    }

    @Test
    void getProfileReturnsView() {
        DriverProfile profile = DriverProfile.builder()
                .userId(10L)
                .fullName("John Doe")
                .email("john@example.com")
                .status(DriverProfileStatus.PENDING)
                .build();
        when(driverProfileRepository.findById(10L)).thenReturn(Optional.of(profile));
        when(userProfileRepository.findByUserId(10L))
                .thenReturn(Optional.of(UserProfile.builder()
                        .userId(10L)
                        .name("John Doe")
                        .phone("9876543210")
                        .email("john@example.com")
                        .emailVerified(true)
                        .build()));

        DriverProfileView view = driverProfileService.getProfile(10L);

        assertThat(view.fullName()).isEqualTo("John Doe");
        assertThat(view.email()).isEqualTo("john@example.com");
        assertThat(view.emailVerified()).isTrue();
    }

    @Test
    void updateProfileUpdatesEditableFields() throws Exception {
        DriverProfile profile = DriverProfile.builder()
                .userId(10L)
                .fullName("Old Name")
                .email("old@example.com")
                .status(DriverProfileStatus.PENDING)
                .build();

        when(driverProfileRepository.findById(10L)).thenReturn(Optional.of(profile));
        when(driverProfileRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(driverProfileRepository.save(any(DriverProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userProfileRepository.findByUserId(10L))
                .thenReturn(Optional.of(UserProfile.builder()
                        .userId(10L)
                        .name("New Name")
                        .phone("9876543210")
                        .email("new@example.com")
                        .emailVerified(true)
                        .build()));

        DriverProfileUpdateForm form = new DriverProfileUpdateForm();
        form.setFullName("New Name");
        form.setEmail("new@example.com");
        form.setDob("1990-05-20");
        form.setGender("Male");
        form.setPhone("9876543210");
        form.setLanguages(List.of("English", "Tamil"));
        form.setExperience("5");
        form.setProfilePhoto(new MockMultipartFile("profilePhoto", "photo.jpg", "image/jpeg", new byte[] {1, 2}));

        DriverProfileView view = driverProfileService.updateProfile(10L, form);

        assertThat(view.fullName()).isEqualTo("New Name");
        assertThat(view.email()).isEqualTo("new@example.com");
        assertThat(view.emailVerified()).isTrue();
        assertThat(profile.getProfilePhoto()).containsExactly(1, 2);
        assertThat(profile.getDob()).isEqualTo("1990-05-20");
        assertThat(profile.getExperience()).isEqualTo("5");
        verify(driverEmailOtpService).sendOtp(10L, "new@example.com", "New Name");
    }

    @Test
    void updateProfileRejectsNonImageUploads() {
        DriverProfile profile = DriverProfile.builder()
                .userId(10L)
                .fullName("Driver")
                .email("driver@example.com")
                .status(DriverProfileStatus.PENDING)
                .build();

        when(driverProfileRepository.findById(10L)).thenReturn(Optional.of(profile));

        DriverProfileUpdateForm form = new DriverProfileUpdateForm();
        form.setProfilePhoto(new MockMultipartFile(
                "profilePhoto", "document.pdf", "application/pdf", new byte[] {1, 2, 3}));

        assertThatThrownBy(() -> driverProfileService.updateProfile(10L, form))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("profilePhoto must be an image file");

        verify(driverProfileRepository, never()).save(any());
        verify(driverEmailOtpService, never()).sendOtp(any(), any(), any());
    }

    @Test
    void updateProfileRejectsOversizedImageUploads() {
        DriverProfile profile = DriverProfile.builder()
                .userId(10L)
                .fullName("Driver")
                .email("driver@example.com")
                .status(DriverProfileStatus.PENDING)
                .build();

        when(driverProfileRepository.findById(10L)).thenReturn(Optional.of(profile));

        byte[] large = new byte[5 * 1024 * 1024 + 1];
        DriverProfileUpdateForm form = new DriverProfileUpdateForm();
        form.setProfilePhoto(new MockMultipartFile("profilePhoto", "large.jpg", "image/jpeg", large));

        assertThatThrownBy(() -> driverProfileService.updateProfile(10L, form))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("profilePhoto must be 5 MB or smaller");

        verify(driverProfileRepository, never()).save(any());
        verify(driverEmailOtpService, never()).sendOtp(any(), any(), any());
    }

    @Test
    void updateProfileRejectsDuplicateEmail() {
        DriverProfile profile = DriverProfile.builder()
                .userId(10L)
                .fullName("Old Name")
                .email("old@example.com")
                .status(DriverProfileStatus.PENDING)
                .build();

        when(driverProfileRepository.findById(10L)).thenReturn(Optional.of(profile));
        when(driverProfileRepository.findByEmail("existing@example.com"))
                .thenReturn(Optional.of(DriverProfile.builder()
                        .userId(20L)
                        .email("existing@example.com")
                        .status(DriverProfileStatus.PENDING)
                        .build()));

        DriverProfileUpdateForm form = new DriverProfileUpdateForm();
        form.setEmail("existing@example.com");

        assertThatThrownBy(() -> driverProfileService.updateProfile(10L, form))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email already in use");
    }
}
