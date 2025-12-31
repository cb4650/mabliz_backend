package com.dztech.rayder.controller;

import com.dztech.rayder.dto.DriverTripActionResponse;
import com.dztech.rayder.dto.DriverTripDepartureResponse;
import com.dztech.rayder.dto.DriverTripDetailResponse;
import com.dztech.rayder.dto.DriverDepartureRequest;
import com.dztech.rayder.security.AuthenticatedUserProvider;
import com.dztech.rayder.service.DriverTripResponseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
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
        DriverTripDepartureResponse response = driverTripResponseService.markDeparted(
                driverId, bookingId, request.latitude(), request.longitude());
        return ResponseEntity.ok(response);
    }
}
