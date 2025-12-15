package com.dztech.rayder.service;

import com.dztech.rayder.dto.CreateDriverRequest;
import com.dztech.rayder.dto.DriverLocationResponse;
import com.dztech.rayder.dto.DriverRequestDetails;
import com.dztech.rayder.exception.ResourceNotFoundException;
import com.dztech.rayder.model.DriverRequest;
import com.dztech.rayder.model.Vehicle;
import com.dztech.rayder.repository.DriverRequestRepository;
import com.dztech.rayder.repository.VehicleRepository;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DriverRequestService {

    private final DriverRequestRepository driverRequestRepository;
    private final VehicleRepository vehicleRepository;

    public DriverRequestService(
            DriverRequestRepository driverRequestRepository,
            VehicleRepository vehicleRepository) {
        this.driverRequestRepository = driverRequestRepository;
        this.vehicleRepository = vehicleRepository;
    }

    @Transactional
    public DriverRequestDetails createDriverRequest(Long userId, CreateDriverRequest request) {
        validateTimeRange(request.startTime(), request.endTime());

        Vehicle vehicle = vehicleRepository.findByIdAndUserId(request.vehicleId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found for current user"));

        DriverRequest entity = DriverRequest.builder()
                .userId(userId)
                .vehicle(vehicle)
                .bookingType(request.bookingType().trim())
                .tripOption(request.tripOption().trim())
                .hours(request.hours())
                .startTime(request.startTime())
                .endTime(request.endTime())
                .pickupAddress(request.pickup().address().trim())
                .pickupLatitude(request.pickup().latitude())
                .pickupLongitude(request.pickup().longitude())
                .dropAddress(request.drop().address().trim())
                .dropLatitude(request.drop().latitude())
                .dropLongitude(request.drop().longitude())
                .build();

        DriverRequest saved = driverRequestRepository.save(entity);
        return toDetails(saved);
    }

    private void validateTimeRange(Instant startTime, Instant endTime) {
        if (startTime != null && endTime != null && !endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("End time must be after start time");
        }
    }

    private DriverRequestDetails toDetails(DriverRequest request) {
        DriverLocationResponse pickup = new DriverLocationResponse(
                request.getPickupAddress(),
                request.getPickupLatitude(),
                request.getPickupLongitude());

        DriverLocationResponse drop = new DriverLocationResponse(
                request.getDropAddress(),
                request.getDropLatitude(),
                request.getDropLongitude());

        return new DriverRequestDetails(
                request.getId(),
                request.getBookingType(),
                request.getTripOption(),
                request.getVehicle().getId(),
                request.getHours(),
                request.getStartTime(),
                request.getEndTime(),
                pickup,
                drop);
    }
}
