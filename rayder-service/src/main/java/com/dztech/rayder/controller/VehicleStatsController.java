package com.dztech.rayder.controller;

import com.dztech.rayder.repository.VehicleRepository;
import com.dztech.rayder.security.AuthenticatedUserProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/vehicles")
public class VehicleStatsController {

    private final VehicleRepository vehicleRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public VehicleStatsController(
            VehicleRepository vehicleRepository, AuthenticatedUserProvider authenticatedUserProvider) {
        this.vehicleRepository = vehicleRepository;
        this.authenticatedUserProvider = authenticatedUserProvider;
    }

    @GetMapping("/count")
    public ResponseEntity<VehicleCountResponse> countVehicles() {
        Long userId = authenticatedUserProvider.getCurrentUserId();
        long count = vehicleRepository.countByUserId(userId);
        return ResponseEntity.ok(new VehicleCountResponse(true, count));
    }

    private record VehicleCountResponse(boolean success, long count) {
    }
}
