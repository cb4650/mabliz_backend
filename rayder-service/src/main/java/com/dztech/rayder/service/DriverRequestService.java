package com.dztech.rayder.service;

import com.dztech.rayder.client.InternalDriverClient;
import com.dztech.rayder.client.InternalNotificationClient;
import com.dztech.rayder.dto.CreateDriverRequest;
import com.dztech.rayder.dto.DriverDetailResponse;
import com.dztech.rayder.dto.DriverLocationResponse;
import com.dztech.rayder.dto.DriverRequestDetails;
import com.dztech.rayder.dto.InternalDriverProfileResponse;
import com.dztech.rayder.dto.OtpVerificationRequest;
import com.dztech.rayder.dto.OtpVerificationResponse;
import com.dztech.rayder.dto.TripConfirmedNotificationRequest;
import com.dztech.rayder.dto.TripDetailResponse;
import com.dztech.rayder.dto.VehicleCompletionResponse;
import com.dztech.rayder.exception.ResourceNotFoundException;
import com.dztech.rayder.model.DriverRequest;
import com.dztech.rayder.model.Vehicle;
import com.dztech.rayder.repository.DriverRequestRepository;
import com.dztech.rayder.repository.VehicleRepository;
import java.math.BigDecimal;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DriverRequestService {

    private static final Logger log = LoggerFactory.getLogger(DriverRequestService.class);
    private static final BigDecimal DEFAULT_CHARGE = new BigDecimal("250.00");
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_CONFIRMED = "CONFIRMED";

    private final DriverRequestRepository driverRequestRepository;
    private final VehicleRepository vehicleRepository;
    private final InternalNotificationClient internalNotificationClient;
    private final InternalDriverClient internalDriverClient;

    public DriverRequestService(
            DriverRequestRepository driverRequestRepository,
            VehicleRepository vehicleRepository,
            InternalNotificationClient internalNotificationClient,
            InternalDriverClient internalDriverClient) {
        this.driverRequestRepository = driverRequestRepository;
        this.vehicleRepository = vehicleRepository;
        this.internalNotificationClient = internalNotificationClient;
        this.internalDriverClient = internalDriverClient;
    }

    @Transactional
    public DriverRequestDetails createDriverRequest(Long userId, CreateDriverRequest request) {
        log.info("Creating driver request for userId: {}, pickup: {}, drop: {}",
                userId, request.pickup().address(), request.drop().address());

        validateTimeRange(request.startTime(), request.endTime());
        FareBreakup breakup = calculateFareBreakup();

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
                .status(STATUS_PENDING)
                .estimate(breakup.estimate())
                .baseFare(breakup.baseFare())
                .lateNightCharges(breakup.lateNightCharges())
                .extraHourCharges(breakup.extraHourCharges())
                .festivalCharges(breakup.festivalCharges())
                .build();

        DriverRequest saved = driverRequestRepository.save(entity);
        log.info("Driver request created successfully with bookingId: {}", saved.getId());
        return toDetails(saved);
    }

    @Transactional
    public DriverRequestDetails confirmDriverRequest(Long userId, Long bookingId) {
        log.info("Confirming driver request for userId: {}, bookingId: {}", userId, bookingId);

        DriverRequest request = driverRequestRepository
                .findByIdAndUserId(bookingId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found for current user"));

        request.setStatus(STATUS_CONFIRMED);
        DriverRequest saved = driverRequestRepository.save(request);
        log.info("Driver request confirmed, now notifying drivers for bookingId: {}", bookingId);
        notifyDrivers(saved);
        return toDetails(saved);
    }

    @Transactional(readOnly = true)
    public TripDetailResponse getTripDetails(Long userId, Long bookingId) {
        log.info("Getting trip details for userId: {}, bookingId: {}", userId, bookingId);

        DriverRequest request = driverRequestRepository
                .findByIdAndUserId(bookingId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found for current user"));

        DriverDetailResponse driver = null;
        if (request.getAcceptedDriverId() != null) {
            InternalDriverProfileResponse driverProfile = internalDriverClient.getDriverProfile(request.getAcceptedDriverId());
            if (driverProfile != null) {
                driver = new DriverDetailResponse(
                        driverProfile.userId(),
                        driverProfile.fullName(),
                        driverProfile.email(),
                        driverProfile.phone());
            }
        }

        DriverLocationResponse pickup = new DriverLocationResponse(
                request.getPickupAddress(),
                request.getPickupLatitude(),
                request.getPickupLongitude());

        DriverLocationResponse drop = new DriverLocationResponse(
                request.getDropAddress(),
                request.getDropLatitude(),
                request.getDropLongitude());

        return new TripDetailResponse(
                request.getId(),
                request.getBookingType(),
                request.getTripOption(),
                request.getVehicle().getId(),
                request.getHours(),
                request.getStartTime(),
                request.getEndTime(),
                pickup,
                drop,
                request.getStatus(),
                request.getBaseFare(),
                request.getLateNightCharges(),
                request.getExtraHourCharges(),
                request.getFestivalCharges(),
                request.getEstimate(),
                driver,
                request.getTripOtp());
    }

    @Transactional(readOnly = true)
    public VehicleCompletionResponse checkVehicleCompletion(Long userId, Long bookingId) {
        log.info("Checking vehicle completion for userId: {}, bookingId: {}", userId, bookingId);

        DriverRequest request = driverRequestRepository
                .findByIdAndUserId(bookingId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found for current user"));

        Vehicle vehicle = request.getVehicle();
        boolean isCompleted = vehicle.getFuelType() != null
                && vehicle.getYear() != null && !vehicle.getYear().trim().isEmpty()
                && vehicle.getPolicyNo() != null && !vehicle.getPolicyNo().trim().isEmpty()
                && vehicle.getStartDate() != null
                && vehicle.getExpiryDate() != null;

        log.info("Vehicle completion check for bookingId: {}, vehicleId: {}, isCompleted: {}",
                bookingId, vehicle.getId(), isCompleted);
        return new VehicleCompletionResponse(isCompleted);
    }

    @Transactional
    public OtpVerificationResponse verifyOtp(Long userId, Long bookingId, OtpVerificationRequest request) {
        log.info("Verifying OTP for userId: {}, bookingId: {}", userId, bookingId);

        DriverRequest driverRequest = driverRequestRepository
                .findByIdAndUserId(bookingId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found for current user"));

        // Check if vehicle details are required
        VehicleCompletionResponse vehicleCompletion = checkVehicleCompletion(userId, bookingId);
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
            vehicleRepository.save(vehicle);
            log.info("Updated vehicle details for vehicleId: {}", vehicle.getId());
        }

        return new OtpVerificationResponse(true, "OTP verified successfully, trip started");
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
                drop,
                request.getStatus(),
                request.getBaseFare(),
                request.getLateNightCharges(),
                request.getExtraHourCharges(),
                request.getFestivalCharges(),
                request.getEstimate());
    }

    private FareBreakup calculateFareBreakup() {
        BigDecimal baseFare = DEFAULT_CHARGE;
        BigDecimal lateNightCharges = DEFAULT_CHARGE;
        BigDecimal extraHourCharges = DEFAULT_CHARGE;
        BigDecimal festivalCharges = DEFAULT_CHARGE;
        BigDecimal estimate = baseFare
                .add(lateNightCharges)
                .add(extraHourCharges)
                .add(festivalCharges);
        return new FareBreakup(baseFare, lateNightCharges, extraHourCharges, festivalCharges, estimate);
    }

    private record FareBreakup(
            BigDecimal baseFare,
            BigDecimal lateNightCharges,
            BigDecimal extraHourCharges,
            BigDecimal festivalCharges,
            BigDecimal estimate) {
    }

    private void notifyDrivers(DriverRequest request) {
        log.info("Sending trip confirmation notification to all drivers for bookingId: {}", request.getId());
        TripConfirmedNotificationRequest payload = new TripConfirmedNotificationRequest(
                request.getId(),
                request.getPickupAddress(),
                request.getDropAddress(),
                request.getStartTime(),
                request.getEndTime());
        internalNotificationClient.sendTripConfirmed(payload);
    }
}
