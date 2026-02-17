package com.dztech.rayder.controller;

import com.dztech.rayder.dto.DriverTripActionResponse;
import com.dztech.rayder.dto.DriverTripCloseRequest;
import com.dztech.rayder.dto.DriverTripCloseResponse;
import com.dztech.rayder.dto.DriverTripDepartureResponse;
import com.dztech.rayder.dto.DriverTripDetailResponse;
import com.dztech.rayder.dto.DriverTripListResponse;
import com.dztech.rayder.dto.DriverDepartureRequest;
import com.dztech.rayder.dto.DriverReachedRequest;
import com.dztech.rayder.dto.DriverReachedResponse;
import com.dztech.rayder.dto.OtpVerificationRequest;
import com.dztech.rayder.dto.OtpVerificationResponse;
import com.dztech.rayder.dto.VehicleCompletionResponse;
import com.dztech.rayder.security.AuthenticatedUserProvider;
import com.dztech.rayder.service.DriverTripResponseService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/driver/trips")
public class DriverTripResponseController {

    private final DriverTripResponseService driverTripResponseService;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public DriverTripResponseController(
            DriverTripResponseService driverTripResponseService,
            AuthenticatedUserProvider authenticatedUserProvider) {
        this.driverTripResponseService = driverTripResponseService;
        this.authenticatedUserProvider = authenticatedUserProvider;
    }

    @GetMapping
    public ResponseEntity<DriverTripListResponse> getTripsByStatus(@RequestParam String status) {
        Long driverId = authenticatedUserProvider.getCurrentUserId();
        DriverTripListResponse response = driverTripResponseService.getTripsByStatus(driverId, status);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<DriverTripDetailResponse> getTrip(@PathVariable Long bookingId) {
        Long driverId = authenticatedUserProvider.getCurrentUserId();
        DriverTripDetailResponse response = driverTripResponseService.getTripForDriver(driverId, bookingId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{bookingId}/accept")
    public ResponseEntity<DriverTripActionResponse> acceptTrip(@PathVariable Long bookingId) {
        Long driverId = authenticatedUserProvider.getCurrentUserId();
        DriverTripActionResponse response = driverTripResponseService.acceptTrip(driverId, bookingId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{bookingId}/deny")
    public ResponseEntity<DriverTripActionResponse> denyTrip(@PathVariable Long bookingId) {
        Long driverId = authenticatedUserProvider.getCurrentUserId();
        DriverTripActionResponse response = driverTripResponseService.denyTrip(driverId, bookingId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{bookingId}/depart")
    public ResponseEntity<DriverTripDepartureResponse> markDeparted(
            @PathVariable Long bookingId, @RequestBody @Valid DriverDepartureRequest request) {
        Long driverId = authenticatedUserProvider.getCurrentUserId();
        System.out.println("driverId");
        System.out.println(driverId);
        DriverTripDepartureResponse response = driverTripResponseService.markDeparted(
                driverId, bookingId, request.latitude(), request.longitude());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{bookingId}/close")
    public ResponseEntity<DriverTripCloseResponse> closeTrip(
            @PathVariable Long bookingId, @RequestBody @Valid DriverTripCloseRequest request) {
        Long driverId = authenticatedUserProvider.getCurrentUserId();
        DriverTripCloseResponse response = driverTripResponseService.closeTrip(
                driverId, bookingId, request.latitude(), request.longitude());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{bookingId}/vehicle-completion")
    public ResponseEntity<VehicleCompletionResponse> checkVehicleCompletion(@PathVariable Long bookingId) {
        Long driverId = authenticatedUserProvider.getCurrentUserId();
        VehicleCompletionResponse response = driverTripResponseService.checkVehicleCompletionForDriver(driverId, bookingId);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/{bookingId}/verify-otp", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<OtpVerificationResponse> verifyOtp(
            @PathVariable Long bookingId, @ModelAttribute @Valid OtpVerificationRequest request) {
        Long driverId = authenticatedUserProvider.getCurrentUserId();
        OtpVerificationResponse response = driverTripResponseService.verifyOtpForDriver(driverId, bookingId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/{bookingId}/mark-reached", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DriverReachedResponse> markReached(
            @PathVariable Long bookingId, @ModelAttribute @Valid DriverReachedRequest request) {
        Long driverId = authenticatedUserProvider.getCurrentUserId();
        DriverReachedResponse response = driverTripResponseService.markDriverReached(driverId, bookingId, request);
        return ResponseEntity.ok(response);
    }
}
