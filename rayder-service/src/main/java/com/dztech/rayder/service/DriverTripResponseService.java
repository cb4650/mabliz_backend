package com.dztech.rayder.service;

import com.dztech.rayder.dto.DriverTripActionResponse;
import com.dztech.rayder.exception.ResourceNotFoundException;
import com.dztech.rayder.model.DriverRequest;
import com.dztech.rayder.model.DriverTripResponse;
import com.dztech.rayder.model.DriverTripResponseStatus;
import com.dztech.rayder.repository.DriverRequestRepository;
import com.dztech.rayder.repository.DriverTripResponseRepository;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DriverTripResponseService {

    private final DriverRequestRepository driverRequestRepository;
    private final DriverTripResponseRepository driverTripResponseRepository;

    public DriverTripResponseService(
            DriverRequestRepository driverRequestRepository,
            DriverTripResponseRepository driverTripResponseRepository) {
        this.driverRequestRepository = driverRequestRepository;
        this.driverTripResponseRepository = driverTripResponseRepository;
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
