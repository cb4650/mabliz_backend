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
import com.dztech.rayder.model.CarBrand;
import com.dztech.rayder.model.CarModel;
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

    @Mock
    private CarCatalogService carCatalogService;

    private VehicleService vehicleService;

    @BeforeEach
    void setUp() {
        vehicleService = new VehicleService(vehicleRepository, carCatalogService);
    }

    @Test
    void addVehicle_persistsAndReturnsResponse() {
        CarBrand brand = createBrand(6L, "Toyota");
        CarModel model = createModel(21L, "Innova", brand);

        CreateVehicleRequest request = new CreateVehicleRequest(
                6L,
                21L,
                VehicleOwnershipType.OWN_VEHICLE,
                VehicleTransmissionType.MANUAL,
                VehicleFuelType.DIESEL,
                "2021",
                " POL123 ",
                LocalDate.parse("2024-01-10"),
                LocalDate.parse("2025-01-10"));

        when(carCatalogService.getBrandById(6L)).thenReturn(brand);
        when(carCatalogService.getModelById(21L)).thenReturn(model);
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> {
            Vehicle vehicle = invocation.getArgument(0);
            vehicle.setId(12L);
            return vehicle;
        });

        VehicleResponse response = vehicleService.addVehicle(5L, request);

        ArgumentCaptor<Vehicle> captor = ArgumentCaptor.forClass(Vehicle.class);
        verify(vehicleRepository).save(captor.capture());
        Vehicle persisted = captor.getValue();
        assertThat(persisted.getBrand()).isEqualTo(brand);
        assertThat(persisted.getModel()).isEqualTo(model);
        assertThat(persisted.getPolicyNo()).isEqualTo("POL123");
        assertThat(persisted.getUserId()).isEqualTo(5L);

        assertThat(response.id()).isEqualTo(12L);
        assertThat(response.brandId()).isEqualTo(6L);
        assertThat(response.brandName()).isEqualTo("Toyota");
        assertThat(response.modelId()).isEqualTo(21L);
        assertThat(response.modelName()).isEqualTo("Innova");
        assertThat(response.policyNo()).isEqualTo("POL123");
    }

    @Test
    void addVehicle_withInvalidDates_throwsIllegalArgumentException() {
        CreateVehicleRequest request = new CreateVehicleRequest(
                6L,
                21L,
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
        CarBrand toyota = createBrand(6L, "Toyota");
        CarModel innova = createModel(21L, "Innova", toyota);
        CarBrand hyundai = createBrand(2L, "Hyundai");
        CarModel i20 = createModel(6L, "i20", hyundai);

        Vehicle first = Vehicle.builder()
                .id(1L)
                .userId(3L)
                .brand(toyota)
                .model(innova)
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
                .brand(hyundai)
                .model(i20)
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
        assertThat(responses.get(0).brandName()).isEqualTo("Toyota");
        assertThat(responses.get(0).modelName()).isEqualTo("Innova");
        assertThat(responses.get(1).fuelType()).isEqualTo(VehicleFuelType.PETROL);
    }

    @Test
    void updateVehicle_updatesProvidedFieldsOnly() {
        CarBrand toyota = createBrand(6L, "Toyota");
        CarModel innova = createModel(21L, "Innova", toyota);
        CarBrand hyundai = createBrand(2L, "Hyundai");
        CarModel creta = createModel(7L, "Creta", hyundai);

        Vehicle existing = Vehicle.builder()
                .id(10L)
                .userId(2L)
                .brand(toyota)
                .model(innova)
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
        when(carCatalogService.getBrandById(2L)).thenReturn(hyundai);
        when(carCatalogService.getModelById(7L)).thenReturn(creta);

        UpdateVehicleRequest request = new UpdateVehicleRequest(
                2L,
                7L,
                VehicleOwnershipType.COMMERCIAL,
                VehicleTransmissionType.AUTOMATIC,
                null,
                "2023",
                "HYN99999",
                null,
                LocalDate.parse("2025-02-01"));

        VehicleResponse response = vehicleService.updateVehicle(2L, 10L, request);

        assertThat(response.brandId()).isEqualTo(2L);
        assertThat(response.brandName()).isEqualTo("Hyundai");
        assertThat(response.modelId()).isEqualTo(7L);
        assertThat(response.modelName()).isEqualTo("Creta");
        assertThat(response.ownershipType()).isEqualTo(VehicleOwnershipType.COMMERCIAL);
        assertThat(response.transmission()).isEqualTo(VehicleTransmissionType.AUTOMATIC);
        assertThat(response.year()).isEqualTo("2023");
        assertThat(response.policyNo()).isEqualTo("HYN99999");
        assertThat(response.expiryDate()).isEqualTo(LocalDate.parse("2025-02-01"));
    }

    @Test
    void updateVehicle_whenNotFound_throwsResourceNotFoundException() {
        when(vehicleRepository.findByIdAndUserId(1L, 99L)).thenReturn(Optional.empty());

        UpdateVehicleRequest request = new UpdateVehicleRequest(
                6L,
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
        CarBrand brand = createBrand(6L, "Toyota");
        CarModel model = createModel(21L, "Innova", brand);
        Vehicle existing = Vehicle.builder()
                .id(5L)
                .userId(3L)
                .brand(brand)
                .model(model)
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

    private CarBrand createBrand(long id, String name) {
        CarBrand brand = new CarBrand();
        brand.setId(id);
        brand.setName(name);
        brand.setCountry("Country");
        brand.setCategory("Category");
        return brand;
    }

    private CarModel createModel(long id, String name, CarBrand brand) {
        CarModel model = new CarModel();
        model.setId(id);
        model.setName(name);
        model.setBrand(brand);
        return model;
    }
}
