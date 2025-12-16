package com.dztech.rayder.controller;

import com.dztech.rayder.dto.DriverAvailabilityResponse;
import com.dztech.rayder.dto.DriverAvailabilityUpdateRequest;
import com.dztech.rayder.security.AuthenticatedUserProvider;
import com.dztech.rayder.service.DriverAvailabilityService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/driver/availability")
public class DriverAvailabilityController {

    private final DriverAvailabilityService driverAvailabilityService;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public DriverAvailabilityController(
            DriverAvailabilityService driverAvailabilityService,
            AuthenticatedUserProvider authenticatedUserProvider) {
        this.driverAvailabilityService = driverAvailabilityService;
        this.authenticatedUserProvider = authenticatedUserProvider;
    }

    @PutMapping
    public ResponseEntity<DriverAvailabilityResponse> updateAvailability(
            @RequestBody @Valid DriverAvailabilityUpdateRequest request) {
        Long userId = authenticatedUserProvider.getCurrentUserId();
        DriverAvailabilityResponse response = driverAvailabilityService.updateAvailability(userId, request);
        return ResponseEntity.ok(response);
    }
}
