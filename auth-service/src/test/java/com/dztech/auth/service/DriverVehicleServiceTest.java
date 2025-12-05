package com.dztech.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dztech.auth.dto.DriverVehicleCreateRequest;
import com.dztech.auth.dto.DriverVehicleView;
import com.dztech.auth.exception.ResourceNotFoundException;
import com.dztech.auth.model.DriverVehicle;
import com.dztech.auth.repository.DriverProfileRepository;
import com.dztech.auth.repository.DriverVehicleRepository;
import com.dztech.auth.storage.DocumentStorageService;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class DriverVehicleServiceTest {

    @Mock
    private DriverVehicleRepository driverVehicleRepository;

    @Mock
    private DriverProfileRepository driverProfileRepository;

    @Mock
    private DocumentStorageService documentStorageService;

    private DriverVehicleService driverVehicleService;

    @BeforeEach
    void setUp() {
        driverVehicleService =
                new DriverVehicleService(driverVehicleRepository, driverProfileRepository, documentStorageService);
    }

    @Test
    void addVehiclePersistsData() {
        when(driverProfileRepository.existsById(42L)).thenReturn(true);
        when(driverVehicleRepository.existsByVehicleNumberIgnoreCase("TN09AB1234")).thenReturn(false);
        when(driverVehicleRepository.save(any(DriverVehicle.class))).thenAnswer(invocation -> {
            DriverVehicle vehicle = invocation.getArgument(0);
            vehicle.setId(100L);
            return vehicle;
        });

        DriverVehicleCreateRequest request = buildValidRequest();

        DriverVehicleView view = driverVehicleService.addVehicle(42L, request);

        assertThat(view.id()).isEqualTo(100L);
        assertThat(view.vehicleNumber()).isEqualTo("TN09AB1234");
        assertThat(view.vehicleType()).isEqualTo("private");
        assertThat(view.insuranceExpiryDate()).isEqualTo(LocalDate.of(2025, 12, 31));
        verify(documentStorageService)
                .upload(anyString(), any(), eq(request.getRcImage().getSize()), eq("image/jpeg"));
    }

    @Test
    void addVehicleRejectsDuplicateVehicleNumber() {
        when(driverProfileRepository.existsById(42L)).thenReturn(true);
        when(driverVehicleRepository.existsByVehicleNumberIgnoreCase("TN09AB1234")).thenReturn(true);

        DriverVehicleCreateRequest request = buildValidRequest();

        assertThatThrownBy(() -> driverVehicleService.addVehicle(42L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("vehicleNumber is already registered");
    }

    @Test
    void addVehicleRequiresDriverProfile() {
        when(driverProfileRepository.existsById(42L)).thenReturn(false);

        DriverVehicleCreateRequest request = buildValidRequest();

        assertThatThrownBy(() -> driverVehicleService.addVehicle(42L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Driver profile not found");
    }

    @Test
    void addVehicleValidatesInsuranceDate() {
        when(driverProfileRepository.existsById(42L)).thenReturn(true);
        when(driverVehicleRepository.existsByVehicleNumberIgnoreCase("TN09AB1234")).thenReturn(false);

        DriverVehicleCreateRequest request = buildValidRequest();
        request.setInsuranceExpiryDate("31/12/2025");

        assertThatThrownBy(() -> driverVehicleService.addVehicle(42L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("insuranceExpiryDate");
    }

    @Test
    void addVehicleRejectsNonImageUpload() {
        when(driverProfileRepository.existsById(42L)).thenReturn(true);
        when(driverVehicleRepository.existsByVehicleNumberIgnoreCase("TN09AB1234")).thenReturn(false);

        DriverVehicleCreateRequest request = buildValidRequest();
        request.setRcImage(new MockMultipartFile("rcImage", "doc.pdf", "application/pdf", new byte[] {1}));

        assertThatThrownBy(() -> driverVehicleService.addVehicle(42L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("rcImage must be an image file");
    }

    private DriverVehicleCreateRequest buildValidRequest() {
        DriverVehicleCreateRequest request = new DriverVehicleCreateRequest();
        request.setVehicleNumber("tn09ab1234");
        request.setVehicleType("private");
        request.setManufacturedYear("05/2021");
        request.setInsuranceExpiryDate("31-12-2025");
        request.setBrand("Toyota");
        request.setModel("Innova Crysta");
        request.setRcImage(new MockMultipartFile("rcImage", "rc.jpg", "image/jpeg", new byte[] {1, 2}));
        request.setInsuranceImage(
                new MockMultipartFile("insuranceImage", "ins.jpg", "image/jpeg", new byte[] {3, 4}));
        request.setPollutionCertificateImage(new MockMultipartFile(
                "pollutionCertificateImage", "poll.jpg", "image/jpeg", new byte[] {5, 6}));
        return request;
    }
}
