package com.dztech.rayder.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dztech.rayder.dto.CreateVehicleRequest;
import com.dztech.rayder.dto.UpdateVehicleRequest;
import com.dztech.rayder.dto.VehicleResponse;
import com.dztech.rayder.exception.ResourceNotFoundException;
import com.dztech.rayder.model.Vehicle;
import com.dztech.rayder.model.VehicleFuelType;
import com.dztech.rayder.model.VehicleOwnershipType;
import com.dztech.rayder.model.VehicleTransmissionType;
import com.dztech.rayder.repository.VehicleRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    private VehicleService vehicleService;

    @BeforeEach
    void setUp() {
        vehicleService = new VehicleService(vehicleRepository);
    }

    @Test
    void addVehicle_persistsAndReturnsResponse() {
        CreateVehicleRequest request = new CreateVehicleRequest(
                " Toyota ",
                " Innova ",
                VehicleOwnershipType.OWN_VEHICLE,
                VehicleTransmissionType.MANUAL,
                VehicleFuelType.DIESEL,
                "2021",
                " POL123 ",
                LocalDate.parse("2024-01-10"),
                LocalDate.parse("2025-01-10"));

        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> {
            Vehicle vehicle = invocation.getArgument(0);
            vehicle.setId(12L);
            return vehicle;
        });

        VehicleResponse response = vehicleService.addVehicle(5L, request);

        ArgumentCaptor<Vehicle> captor = ArgumentCaptor.forClass(Vehicle.class);
        verify(vehicleRepository).save(captor.capture());
        Vehicle persisted = captor.getValue();
        assertThat(persisted.getBrand()).isEqualTo("Toyota");
        assertThat(persisted.getModel()).isEqualTo("Innova");
        assertThat(persisted.getPolicyNo()).isEqualTo("POL123");
        assertThat(persisted.getUserId()).isEqualTo(5L);

        assertThat(response.id()).isEqualTo(12L);
        assertThat(response.brand()).isEqualTo("Toyota");
        assertThat(response.policyNo()).isEqualTo("POL123");
    }

    @Test
    void addVehicle_withInvalidDates_throwsIllegalArgumentException() {
        CreateVehicleRequest request = new CreateVehicleRequest(
                "Toyota",
                "Innova",
                VehicleOwnershipType.OWN_VEHICLE,
                VehicleTransmissionType.MANUAL,
                VehicleFuelType.DIESEL,
                "2021",
                "POL123",
                LocalDate.parse("2024-01-10"),
                LocalDate.parse("2023-12-31"));

        assertThatThrownBy(() -> vehicleService.addVehicle(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Expiry date cannot be before start date");
    }

    @Test
    void getVehicles_returnsMappedResponses() {
        Vehicle first = Vehicle.builder()
                .id(1L)
                .userId(3L)
                .brand("Toyota")
                .model("Innova")
                .ownershipType(VehicleOwnershipType.OWN_VEHICLE)
                .transmission(VehicleTransmissionType.MANUAL)
                .fuelType(VehicleFuelType.DIESEL)
                .year("2021")
                .policyNo("POL1")
                .startDate(LocalDate.parse("2024-01-01"))
                .expiryDate(LocalDate.parse("2025-01-01"))
                .build();
        Vehicle second = Vehicle.builder()
                .id(2L)
                .userId(3L)
                .brand("Hyundai")
                .model("i20")
                .ownershipType(VehicleOwnershipType.COMMERCIAL)
                .transmission(VehicleTransmissionType.AUTOMATIC)
                .fuelType(VehicleFuelType.PETROL)
                .year("2022")
                .policyNo("POL2")
                .startDate(LocalDate.parse("2023-07-15"))
                .expiryDate(LocalDate.parse("2024-07-15"))
                .build();

        when(vehicleRepository.findByUserIdOrderByIdAsc(3L)).thenReturn(List.of(first, second));

        List<VehicleResponse> responses = vehicleService.getVehicles(3L);

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).brand()).isEqualTo("Toyota");
        assertThat(responses.get(1).fuelType()).isEqualTo(VehicleFuelType.PETROL);
    }

    @Test
    void updateVehicle_updatesProvidedFieldsOnly() {
        Vehicle existing = Vehicle.builder()
                .id(10L)
                .userId(2L)
                .brand("Toyota")
                .model("Innova")
                .ownershipType(VehicleOwnershipType.OWN_VEHICLE)
                .transmission(VehicleTransmissionType.MANUAL)
                .fuelType(VehicleFuelType.DIESEL)
                .year("2021")
                .policyNo("POL123")
                .startDate(LocalDate.parse("2024-01-01"))
                .expiryDate(LocalDate.parse("2025-01-01"))
                .build();

        when(vehicleRepository.findByIdAndUserId(10L, 2L)).thenReturn(Optional.of(existing));
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateVehicleRequest request = new UpdateVehicleRequest(
                "Hyundai",
                null,
                VehicleOwnershipType.COMMERCIAL,
                VehicleTransmissionType.AUTOMATIC,
                null,
                "2023",
                "HYN99999",
                null,
                LocalDate.parse("2025-02-01"));

        VehicleResponse response = vehicleService.updateVehicle(2L, 10L, request);

        assertThat(response.brand()).isEqualTo("Hyundai");
        assertThat(response.ownershipType()).isEqualTo(VehicleOwnershipType.COMMERCIAL);
        assertThat(response.transmission()).isEqualTo(VehicleTransmissionType.AUTOMATIC);
        assertThat(response.year()).isEqualTo("2023");
        assertThat(response.policyNo()).isEqualTo("HYN99999");
        assertThat(response.expiryDate()).isEqualTo(LocalDate.parse("2025-02-01"));
        assertThat(response.model()).isEqualTo("Innova");
    }

    @Test
    void updateVehicle_whenNotFound_throwsResourceNotFoundException() {
        when(vehicleRepository.findByIdAndUserId(1L, 99L)).thenReturn(Optional.empty());

        UpdateVehicleRequest request = new UpdateVehicleRequest(
                "Toyota",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);

        assertThatThrownBy(() -> vehicleService.updateVehicle(99L, 1L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Vehicle not found");
    }

    @Test
    void deleteVehicle_removesEntity() {
        Vehicle existing = Vehicle.builder()
                .id(5L)
                .userId(3L)
                .brand("Toyota")
                .model("Innova")
                .ownershipType(VehicleOwnershipType.OWN_VEHICLE)
                .transmission(VehicleTransmissionType.MANUAL)
                .fuelType(VehicleFuelType.DIESEL)
                .year("2021")
                .policyNo("POL123")
                .startDate(LocalDate.parse("2024-01-01"))
                .expiryDate(LocalDate.parse("2025-01-01"))
                .build();

        when(vehicleRepository.findByIdAndUserId(5L, 3L)).thenReturn(Optional.of(existing));

        vehicleService.deleteVehicle(3L, 5L);

        verify(vehicleRepository).delete(existing);
    }
}
