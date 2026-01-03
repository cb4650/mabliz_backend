package com.dztech.rayder.controller;

import com.dztech.rayder.dto.TripDetailResponse;
import com.dztech.rayder.security.AuthenticatedUserProvider;
import com.dztech.rayder.service.DriverRequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/driver/trips")
public class TripController {

    private final DriverRequestService driverRequestService;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public TripController(
            DriverRequestService driverRequestService,
            AuthenticatedUserProvider authenticatedUserProvider) {
        this.driverRequestService = driverRequestService;
        this.authenticatedUserProvider = authenticatedUserProvider;
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<TripDetailResponse> getTripDetails(@PathVariable Long bookingId) {
        Long userId = authenticatedUserProvider.getCurrentUserId();
        TripDetailResponse details = driverRequestService.getTripDetails(userId, bookingId);
        return ResponseEntity.ok(details);
    }
}
