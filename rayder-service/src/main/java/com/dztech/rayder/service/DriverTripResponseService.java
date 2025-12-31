package com.dztech.rayder.service;

import com.dztech.rayder.dto.DriverTripActionResponse;
import com.dztech.rayder.dto.DriverTripDepartureResponse;
import com.dztech.rayder.dto.DriverTripDetailResponse;
import com.dztech.rayder.exception.ResourceNotFoundException;
import com.dztech.rayder.model.DriverRequest;
import com.dztech.rayder.model.DriverTripResponse;
import com.dztech.rayder.model.DriverTripResponseStatus;
import com.dztech.rayder.model.UserProfile;
import com.dztech.rayder.repository.DriverRequestRepository;
import com.dztech.rayder.repository.DriverTripResponseRepository;
import com.dztech.rayder.repository.UserProfileRepository;
import java.util.Optional;
import java.time.Instant;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DriverTripResponseService {

    private final DriverRequestRepository driverRequestRepository;
    private final DriverTripResponseRepository driverTripResponseRepository;
    private final UserProfileRepository userProfileRepository;

    public DriverTripResponseService(
            DriverRequestRepository driverRequestRepository,
            DriverTripResponseRepository driverTripResponseRepository,
            UserProfileRepository userProfileRepository) {
        this.driverRequestRepository = driverRequestRepository;
        this.driverTripResponseRepository = driverTripResponseRepository;
        this.userProfileRepository = userProfileRepository;
    }

    @Transactional
    public DriverTripActionResponse acceptTrip(Long driverId, Long bookingId) {
        DriverRequest request = driverRequestRepository
                .findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        DriverTripResponse existingResponse = driverTripResponseRepository
                .findByBookingIdAndDriverId(bookingId, driverId)
                .orElse(null);

        if (existingResponse != null && existingResponse.getStatus() == DriverTripResponseStatus.DENIED) {
            throw new IllegalArgumentException("You have already denied this trip");
        }
        if (request.getAcceptedDriverId() != null && !request.getAcceptedDriverId().equals(driverId)) {
            throw new IllegalArgumentException("Trip already accepted by another driver");
        }

        Instant now = Instant.now();
        int updated = driverRequestRepository.assignDriverIfAvailable(bookingId, driverId, now);
        if (updated == 0) {
            throw new IllegalArgumentException("Trip already accepted by another driver");
        }

        DriverTripResponse response = existingResponse != null ? existingResponse : new DriverTripResponse();
        response.setBookingId(bookingId);
        response.setDriverId(driverId);
        response.setStatus(DriverTripResponseStatus.ACCEPTED);
        response.setRespondedAt(now);

        DriverTripResponse saved = driverTripResponseRepository.save(response);
        return toActionResponse(saved, driverId, "Trip accepted");
    }

    @Transactional
    public DriverTripActionResponse denyTrip(Long driverId, Long bookingId) {
        DriverRequest request = driverRequestRepository
                .findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        DriverTripResponse existingResponse = driverTripResponseRepository
                .findByBookingIdAndDriverId(bookingId, driverId)
                .orElse(null);

        if (existingResponse != null && existingResponse.getStatus() == DriverTripResponseStatus.DENIED) {
            return toActionResponse(existingResponse, request.getAcceptedDriverId(), "Trip already denied");
        }
        if (existingResponse != null && existingResponse.getStatus() == DriverTripResponseStatus.ACCEPTED) {
            throw new IllegalArgumentException("You have already accepted this trip");
        }

        DriverTripResponse response = existingResponse != null ? existingResponse : new DriverTripResponse();
        response.setBookingId(bookingId);
        response.setDriverId(driverId);
        response.setStatus(DriverTripResponseStatus.DENIED);
        response.setRespondedAt(Instant.now());

        DriverTripResponse saved = driverTripResponseRepository.save(response);
        return toActionResponse(saved, request.getAcceptedDriverId(), "Trip denied");
    }

    @Transactional
    public DriverTripDepartureResponse markDeparted(Long driverId, Long bookingId, BigDecimal latitude, BigDecimal longitude) {
        DriverRequest request = driverRequestRepository
                .findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (request.getAcceptedDriverId() == null || !request.getAcceptedDriverId().equals(driverId)) {
            throw new IllegalArgumentException("Trip is not assigned to this driver");
        }

        Instant now = Instant.now();
        request.setDepartedAt(now);
        request.setDepartedLatitude(latitude);
        request.setDepartedLongitude(longitude);
        request.setStatus("DEPARTED");
        driverRequestRepository.save(request);

        DriverTripDepartureResponse.Data data = new DriverTripDepartureResponse.Data(
                bookingId, driverId, now, latitude, longitude);
        return new DriverTripDepartureResponse(true, "Driver marked as departed", data);
    }

    @Transactional(readOnly = true)
    public DriverTripDetailResponse getTripForDriver(Long driverId, Long bookingId) {
        DriverRequest request = driverRequestRepository
                .findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (request.getAcceptedDriverId() == null || !request.getAcceptedDriverId().equals(driverId)) {
            throw new IllegalArgumentException("Trip is not assigned to this driver");
        }

        Optional<UserProfile> userProfile = userProfileRepository.findById(request.getUserId());

        DriverTripDetailResponse.Data data = new DriverTripDetailResponse.Data(
                request.getId(),
                request.getBookingType(),
                request.getTripOption(),
                request.getHours(),
                request.getStartTime(),
                request.getEndTime(),
                request.getPickupAddress(),
                request.getPickupLatitude(),
                request.getPickupLongitude(),
                request.getDropAddress(),
                request.getDropLatitude(),
                request.getDropLongitude(),
                userProfile.map(UserProfile::getName).orElse(null),
                userProfile.map(UserProfile::getPhone).orElse(null),
                userProfile.map(UserProfile::getEmail).orElse(null),
                userProfile.map(UserProfile::getAddress).orElse(null));

        return new DriverTripDetailResponse(true, "Trip details", data);
    }

    private DriverTripActionResponse toActionResponse(
            DriverTripResponse response, Long acceptedDriverId, String message) {
        DriverTripActionResponse.Data data = new DriverTripActionResponse.Data(
                response.getBookingId(),
                response.getDriverId(),
                response.getStatus().name(),
                acceptedDriverId,
                response.getRespondedAt());
        return new DriverTripActionResponse(true, message, data);
    }
}
