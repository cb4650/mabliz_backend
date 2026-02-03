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
                documentTokenService,
                null);
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

}
