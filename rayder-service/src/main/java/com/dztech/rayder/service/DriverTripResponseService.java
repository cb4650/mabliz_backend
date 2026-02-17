package com.dztech.rayder.service;

import com.dztech.rayder.dto.DriverTripActionResponse;
import com.dztech.rayder.dto.DriverTripCloseRequest;
import com.dztech.rayder.dto.DriverTripCloseResponse;
import com.dztech.rayder.dto.DriverTripDepartureResponse;
import com.dztech.rayder.dto.DriverTripDetailResponse;
import com.dztech.rayder.dto.DriverTripListResponse;
import com.dztech.rayder.dto.DriverReachedRequest;
import com.dztech.rayder.dto.DriverReachedResponse;
import com.dztech.rayder.dto.OtpVerificationRequest;
import com.dztech.rayder.dto.OtpVerificationResponse;
import com.dztech.rayder.dto.VehicleCompletionResponse;
import com.dztech.rayder.model.Vehicle;
import com.dztech.rayder.exception.ResourceNotFoundException;
import com.dztech.rayder.model.DriverRequest;
import com.dztech.rayder.model.DriverTripResponse;
import com.dztech.rayder.model.DriverTripResponseStatus;
import com.dztech.rayder.model.UserProfile;
import com.dztech.rayder.repository.DriverRequestRepository;
import com.dztech.rayder.repository.DriverTripResponseRepository;
import com.dztech.rayder.repository.UserProfileRepository;
import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;
import java.time.Instant;
import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DriverTripResponseService {

    private static final Logger log = LoggerFactory.getLogger(DriverTripResponseService.class);

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

        // Update the driver request status to ACCEPTED
        request.setStatus("ACCEPTED");
        driverRequestRepository.save(request);

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

    @Transactional
    public DriverTripCloseResponse closeTrip(Long driverId, Long bookingId, BigDecimal latitude, BigDecimal longitude) {
        DriverRequest request = driverRequestRepository
                .findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (request.getAcceptedDriverId() == null || !request.getAcceptedDriverId().equals(driverId)) {
            throw new IllegalArgumentException("Trip is not assigned to this driver");
        }

        if (!"STARTED".equals(request.getStatus())) {
            throw new IllegalArgumentException("Trip must be started before it can be closed");
        }

        Instant now = Instant.now();
        request.setTripClosedAt(now);
        request.setTripClosedLatitude(latitude);
        request.setTripClosedLongitude(longitude);
        request.setStatus("COMPLETED");
        driverRequestRepository.save(request);

        DriverTripCloseResponse.Data data = new DriverTripCloseResponse.Data(
                bookingId, driverId, now, latitude, longitude);
        return new DriverTripCloseResponse(true, "Trip closed successfully", data);
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

    @Transactional(readOnly = true)
    public VehicleCompletionResponse checkVehicleCompletionForDriver(Long driverId, Long bookingId) {
        DriverRequest request = driverRequestRepository
                .findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (request.getAcceptedDriverId() == null || !request.getAcceptedDriverId().equals(driverId)) {
            throw new IllegalArgumentException("Trip is not assigned to this driver");
        }

        Vehicle vehicle = request.getVehicle();
        boolean isCompleted = vehicle.getFuelType() != null
                && vehicle.getYear() != null && !vehicle.getYear().trim().isEmpty()
                && vehicle.getPolicyNo() != null && !vehicle.getPolicyNo().trim().isEmpty()
                && vehicle.getStartDate() != null
                && vehicle.getExpiryDate() != null;

        return new VehicleCompletionResponse(isCompleted);
    }

    @Transactional
    public OtpVerificationResponse verifyOtpForDriver(Long driverId, Long bookingId, OtpVerificationRequest request) {
        DriverRequest driverRequest = driverRequestRepository
                .findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (driverRequest.getAcceptedDriverId() == null || !driverRequest.getAcceptedDriverId().equals(driverId)) {
            throw new IllegalArgumentException("Trip is not assigned to this driver");
        }

        // Check if vehicle details are required
        VehicleCompletionResponse vehicleCompletion = checkVehicleCompletionForDriver(driverId, bookingId);
        if (!vehicleCompletion.isVehicleCompleted()) {
            // Vehicle details are mandatory when vehicle is not completed
            if (request.vehicleNo() == null || request.vehicleNo().trim().isEmpty()
                    || request.insuranceExpiry() == null || request.insurancePhoto() == null) {
                return new OtpVerificationResponse(false, "Vehicle details are required since vehicle is not completed");
            }
        }

        // Verify OTP
        if (!request.otp().equals(driverRequest.getTripOtp())) {
            return new OtpVerificationResponse(false, "Invalid OTP");
        }

        // Mark trip as started
        Instant now = Instant.now();
        driverRequest.setStatus("STARTED");
        driverRequest.setTripStartedAt(now);
        driverRequestRepository.save(driverRequest);

        // TODO: Handle image uploads here
        log.info("OTP verified successfully for bookingId: {}, trip marked as STARTED", bookingId);

        // Update vehicle details if provided
        if (request.vehicleNo() != null || request.insuranceNo() != null
                || request.insuranceExpiry() != null || request.insurancePhoto() != null) {
            Vehicle vehicle = driverRequest.getVehicle();
            if (request.vehicleNo() != null) vehicle.setVehicleNo(request.vehicleNo());
            if (request.insuranceNo() != null) vehicle.setInsuranceNo(request.insuranceNo());
            if (request.insuranceExpiry() != null) vehicle.setInsuranceExpiry(request.insuranceExpiry());
            // TODO: Handle insurance photo upload and set the path
            if (request.insurancePhoto() != null) {
                // vehicle.setInsurancePhoto(saveInsurancePhoto(request.insurancePhoto()));
            }
            // Assuming vehicleRepository is accessible or needs to be injected
            // vehicleRepository.save(vehicle);
            log.info("Updated vehicle details for vehicleId: {}", vehicle.getId());
        }

        return new OtpVerificationResponse(true, "OTP verified successfully, trip started");
    }

    @Transactional
    public DriverReachedResponse markDriverReached(Long driverId, Long bookingId, com.dztech.rayder.dto.DriverReachedRequest request) {
        DriverRequest driverRequest = driverRequestRepository
                .findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (driverRequest.getAcceptedDriverId() == null || !driverRequest.getAcceptedDriverId().equals(driverId)) {
            throw new IllegalArgumentException("Trip is not assigned to this driver");
        }

        // Mark driver as reached
        Instant now = Instant.now();
        driverRequest.setDriverReachedAt(now);
        driverRequestRepository.save(driverRequest);

        // TODO: Handle selfie image upload here
        log.info("Driver marked as reached for bookingId: {}, selfie image received", bookingId);

        return new DriverReachedResponse(true, "Driver marked as reached successfully", now);
    }

    @Transactional(readOnly = true)
    public DriverTripListResponse getTripsByStatus(Long driverId, String status) {
        List<String> statuses;

        switch (status.toUpperCase()) {
            case "ACTIVE":
                statuses = List.of("STARTED");
                break;
            case "UPCOMING":
                statuses = List.of("ACCEPTED", "DEPARTED");
                break;
            case "CLOSED":
                statuses = List.of("COMPLETED");
                break;
            default:
                throw new IllegalArgumentException("Invalid status: " + status + ". Valid values are: ACTIVE, UPCOMING, CLOSED");
        }

        List<DriverRequest> requests = driverRequestRepository.findByAcceptedDriverIdAndStatusIn(driverId, statuses);

        List<DriverTripListResponse.DriverTripListItem> items = requests.stream()
                .map(request -> {
                    Optional<UserProfile> userProfile = userProfileRepository.findById(request.getUserId());
                    return new DriverTripListResponse.DriverTripListItem(
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
                            request.getStatus(),
                            request.getCreatedAt(),
                            request.getEstimate());
                })
                .collect(Collectors.toList());

        String message = String.format("Found %d %s trips", items.size(), status.toLowerCase());
        return new DriverTripListResponse(true, message, items);
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
