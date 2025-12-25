package com.dztech.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dztech.auth.dto.DriverProfileUpdateForm;
import com.dztech.auth.dto.DriverProfileView;
import com.dztech.auth.model.DriverProfile;
import com.dztech.auth.model.DriverProfileStatus;
import com.dztech.auth.model.UserProfile;
import com.dztech.auth.repository.DriverFieldVerificationRepository;
import com.dztech.auth.repository.DriverProfileRepository;
import com.dztech.auth.repository.UserProfileRepository;
import com.dztech.auth.security.DocumentTokenService;
import com.dztech.auth.service.DriverDocumentUrlBuilder;
import com.dztech.auth.storage.DocumentPathBuilder;
import com.dztech.auth.storage.DocumentStorageService;
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

    @Mock
    private DriverFieldVerificationRepository driverFieldVerificationRepository;

    @Mock
    private DocumentStorageService documentStorageService;

    @Mock
    private DriverDocumentUrlBuilder driverDocumentUrlBuilder;

    @Mock
    private DocumentTokenService documentTokenService;

    private DriverProfileService driverProfileService;

    @BeforeEach
    void setUp() {
        when(userProfileRepository.findByUserId(anyLong())).thenReturn(Optional.empty());
        when(driverFieldVerificationRepository.findByDriverId(anyLong())).thenReturn(List.of());
        when(documentStorageService.getPresignedUrl(any())).thenReturn(Optional.empty());
        when(documentTokenService.issueProfileDocumentToken(anyLong(), any()))
                .thenReturn("doc-token");
        when(driverDocumentUrlBuilder.profileDocument(anyLong(), any(), any())).thenAnswer(invocation -> {
            Long driverId = invocation.getArgument(0);
            String label = invocation.getArgument(1);
            String token = invocation.getArgument(2);
            return "/api/driver/profile/documents/%d/%s?token=%s".formatted(driverId, label, token);
        });
        driverProfileService = new DriverProfileService(
                driverProfileRepository,
                driverEmailOtpService,
                userProfileRepository,
                driverFieldVerificationRepository,
                documentStorageService,
                driverDocumentUrlBuilder,
                documentTokenService);
    }

    @Test
    void getProfileReturnsView() {
        DriverProfile profile = DriverProfile.builder()
                .userId(10L)
                .fullName("John Doe")
                .email("john@example.com")
                .hillStation(true)
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
        assertThat(view.hillStation()).isTrue();
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
        when(documentTokenService.issueProfileDocumentToken(10L, "profilePhoto")).thenReturn("doc-token");
        when(driverDocumentUrlBuilder.profileDocument(10L, "profilePhoto", "doc-token"))
                .thenReturn("/api/driver/profile/documents/10/profilePhoto?token=doc-token");

        DriverProfileUpdateForm form = new DriverProfileUpdateForm();
        form.setFullName("New Name");
        form.setEmail("new@example.com");
        form.setDob("1990-05-20");
        form.setGender("Male");
        form.setPhone("9876543210");
        form.setLanguages(List.of("English", "Tamil"));
        form.setExperience("5");
        form.setHillStation(true);
        form.setProfilePhoto(new MockMultipartFile("profilePhoto", "photo.jpg", "image/jpeg", new byte[] {1, 2}));

        DriverProfileView view = driverProfileService.updateProfile(10L, form);

        assertThat(view.fullName()).isEqualTo("New Name");
        assertThat(view.email()).isEqualTo("new@example.com");
        assertThat(view.emailVerified()).isTrue();
        assertThat(profile.getProfilePhotoObject())
                .isEqualTo(DocumentPathBuilder.profileDocument(10L, "profile-photo"));
        assertThat(profile.getProfilePhoto()).isNull();
        assertThat(profile.getDob()).isEqualTo("1990-05-20");
        assertThat(profile.getExperience()).isEqualTo("5");
        assertThat(profile.isHillStation()).isTrue();
        assertThat(view.profilePhoto().url())
                .isEqualTo("/api/driver/profile/documents/10/profilePhoto?token=doc-token");
        assertThat(view.profilePhoto().contentType()).isEqualTo("image/jpeg");
        verify(driverEmailOtpService).sendOtp(10L, "new@example.com", "New Name");
        verify(documentStorageService)
                .upload(
                        DocumentPathBuilder.profileDocument(10L, "profile-photo"),
                        any(),
                        eq(form.getProfilePhoto().getSize()),
                        "image/jpeg");
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
