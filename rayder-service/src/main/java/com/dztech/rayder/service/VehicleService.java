package com.dztech.rayder.service;

import com.dztech.rayder.dto.CreateVehicleRequest;
import com.dztech.rayder.dto.UpdateVehicleRequest;
import com.dztech.rayder.dto.VehicleResponse;
import com.dztech.rayder.exception.ResourceNotFoundException;
import com.dztech.rayder.model.Vehicle;
import com.dztech.rayder.repository.VehicleRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    public VehicleService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    @Transactional
    public VehicleResponse addVehicle(Long userId, CreateVehicleRequest request) {
        validateDateOrder(request.startDate(), request.expiryDate());

        Vehicle vehicle = Vehicle.builder()
                .userId(userId)
                .brand(request.brand().trim())
                .model(request.model().trim())
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

        if (request.brand() != null) {
            String brand = request.brand().trim();
            if (brand.isEmpty()) {
                throw new IllegalArgumentException("Brand cannot be blank");
            }
            vehicle.setBrand(brand);
        }

        if (request.model() != null) {
            String model = request.model().trim();
            if (model.isEmpty()) {
                throw new IllegalArgumentException("Model cannot be blank");
            }
            vehicle.setModel(model);
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
                vehicle.getBrand(),
                vehicle.getModel(),
                vehicle.getOwnershipType(),
                vehicle.getTransmission(),
                vehicle.getFuelType(),
                vehicle.getYear(),
                vehicle.getPolicyNo(),
                vehicle.getStartDate(),
                vehicle.getExpiryDate());
    }
}
