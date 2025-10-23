package com.dztech.rayder.service;

import com.dztech.rayder.dto.CreateVehicleRequest;
import com.dztech.rayder.dto.UpdateVehicleRequest;
import com.dztech.rayder.dto.VehicleResponse;
import com.dztech.rayder.exception.ResourceNotFoundException;
import com.dztech.rayder.model.CarBrand;
import com.dztech.rayder.model.CarModel;
import com.dztech.rayder.model.Vehicle;
import com.dztech.rayder.repository.VehicleRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final CarCatalogService carCatalogService;

    public VehicleService(VehicleRepository vehicleRepository, CarCatalogService carCatalogService) {
        this.vehicleRepository = vehicleRepository;
        this.carCatalogService = carCatalogService;
    }

    @Transactional
    public VehicleResponse addVehicle(Long userId, CreateVehicleRequest request) {
        validateDateOrder(request.startDate(), request.expiryDate());

        CarBrand brand = carCatalogService.getBrandById(request.brandId());
        CarModel model = carCatalogService.getModelById(request.modelId());

        if (!model.getBrand().getId().equals(brand.getId())) {
            throw new IllegalArgumentException("Selected model does not belong to the provided brand");
        }

        Vehicle vehicle = Vehicle.builder()
                .userId(userId)
                .brand(brand)
                .model(model)
                .ownershipType(request.ownershipType())
                .transmission(request.transmission())
                .fuelType(request.fuelType())
                .year(request.year().trim())
                .policyNo(request.policyNo().trim())
                .startDate(request.startDate())
                .expiryDate(request.expiryDate())
                .build();

        Vehicle saved = vehicleRepository.save(vehicle);
        return toResponse(saved);
    }

    @Transactional
    public VehicleResponse updateVehicle(Long userId, Long vehicleId, UpdateVehicleRequest request) {
        Vehicle vehicle = vehicleRepository.findByIdAndUserId(vehicleId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));

        CarBrand updatedBrand = null;
        CarModel updatedModel = null;

        if (request.brandId() != null) {
            updatedBrand = carCatalogService.getBrandById(request.brandId());
        }

        if (request.modelId() != null) {
            updatedModel = carCatalogService.getModelById(request.modelId());
        }

        if (updatedModel != null) {
            Long expectedBrandId = updatedBrand != null ? updatedBrand.getId() : updatedModel.getBrand().getId();
            if (!updatedModel.getBrand().getId().equals(expectedBrandId)) {
                throw new IllegalArgumentException("Selected model does not belong to the provided brand");
            }
            vehicle.setModel(updatedModel);
            vehicle.setBrand(updatedModel.getBrand());
        } else if (updatedBrand != null) {
            if (!vehicle.getModel().getBrand().getId().equals(updatedBrand.getId())) {
                throw new IllegalArgumentException("Update model to match the selected brand");
            }
            vehicle.setBrand(updatedBrand);
        }

        if (request.ownershipType() != null) {
            vehicle.setOwnershipType(request.ownershipType());
        }

        if (request.transmission() != null) {
            vehicle.setTransmission(request.transmission());
        }

        if (request.fuelType() != null) {
            vehicle.setFuelType(request.fuelType());
        }

        if (request.year() != null) {
            String year = request.year().trim();
            if (!year.matches("\\d{4}")) {
                throw new IllegalArgumentException("Year must be a valid 4-digit year");
            }
            vehicle.setYear(year);
        }

        if (request.policyNo() != null) {
            String policyNo = request.policyNo().trim();
            if (policyNo.isEmpty()) {
                throw new IllegalArgumentException("Policy number cannot be blank");
            }
            vehicle.setPolicyNo(policyNo);
        }

        if (request.startDate() != null) {
            vehicle.setStartDate(request.startDate());
        }

        if (request.expiryDate() != null) {
            vehicle.setExpiryDate(request.expiryDate());
        }

        validateDateOrder(vehicle.getStartDate(), vehicle.getExpiryDate());

        Vehicle updated = vehicleRepository.save(vehicle);
        return toResponse(updated);
    }

    @Transactional
    public void deleteVehicle(Long userId, Long vehicleId) {
        Vehicle vehicle = vehicleRepository.findByIdAndUserId(vehicleId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));
        vehicleRepository.delete(vehicle);
    }

    @Transactional(readOnly = true)
    public List<VehicleResponse> getVehicles(Long userId) {
        return vehicleRepository.findByUserIdOrderByIdAsc(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    private void validateDateOrder(LocalDate start, LocalDate expiry) {
        if (start != null && expiry != null && expiry.isBefore(start)) {
            throw new IllegalArgumentException("Expiry date cannot be before start date");
        }
    }

    private VehicleResponse toResponse(Vehicle vehicle) {
        return new VehicleResponse(
                vehicle.getId(),
                vehicle.getBrand().getId(),
                vehicle.getBrand().getName(),
                vehicle.getModel().getId(),
                vehicle.getModel().getName(),
                vehicle.getOwnershipType(),
                vehicle.getTransmission(),
                vehicle.getFuelType(),
                vehicle.getYear(),
                vehicle.getPolicyNo(),
                vehicle.getStartDate(),
                vehicle.getExpiryDate());
    }
}
