package com.dztech.rayder.service;

import com.dztech.rayder.client.InternalNotificationClient;
import com.dztech.rayder.dto.CreateDriverRequest;
import com.dztech.rayder.dto.DriverLocationResponse;
import com.dztech.rayder.dto.DriverRequestDetails;
import com.dztech.rayder.dto.TripConfirmedNotificationRequest;
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

    public DriverRequestService(
            DriverRequestRepository driverRequestRepository,
            VehicleRepository vehicleRepository,
            InternalNotificationClient internalNotificationClient) {
        this.driverRequestRepository = driverRequestRepository;
        this.vehicleRepository = vehicleRepository;
        this.internalNotificationClient = internalNotificationClient;
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
