package com.dztech.rayder.service;

import com.dztech.rayder.dto.DriverAvailabilityResponse;
import com.dztech.rayder.dto.DriverAvailabilityUpdateRequest;
import com.dztech.rayder.model.DriverAvailability;
import com.dztech.rayder.repository.DriverAvailabilityRepository;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DriverAvailabilityService {

    private final DriverAvailabilityRepository driverAvailabilityRepository;

    public DriverAvailabilityService(DriverAvailabilityRepository driverAvailabilityRepository) {
        this.driverAvailabilityRepository = driverAvailabilityRepository;
    }

    @Transactional
    public DriverAvailabilityResponse updateAvailability(Long userId, DriverAvailabilityUpdateRequest request) {
        DriverAvailability availability = driverAvailabilityRepository
                .findById(userId)
                .orElseGet(() -> DriverAvailability.builder()
                        .userId(userId)
                        .available(false)
                        .latitude(BigDecimal.ZERO)
                        .longitude(BigDecimal.ZERO)
                        .build());

        availability.setAvailable(Boolean.TRUE.equals(request.online()));
        availability.setLatitude(request.latitude());
        availability.setLongitude(request.longitude());
        DriverAvailability saved = driverAvailabilityRepository.save(availability);

        DriverAvailabilityResponse.Data data = new DriverAvailabilityResponse.Data(
                saved.getUserId(),
                saved.isAvailable(),
                saved.getLatitude(),
                saved.getLongitude(),
                saved.getUpdatedAt());
        return new DriverAvailabilityResponse(true, "Availability updated", data);
    }
}
