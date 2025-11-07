package com.dztech.auth.controller;

import com.dztech.auth.dto.DriverVehicleCreateRequest;
import com.dztech.auth.dto.DriverVehicleCreateResponse;
import com.dztech.auth.dto.DriverVehicleView;
import com.dztech.auth.exception.ResourceNotFoundException;
import com.dztech.auth.security.AuthenticatedUserProvider;
import com.dztech.auth.service.DriverVehicleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/driver/vehicles")
public class DriverVehicleController {

    private final DriverVehicleService driverVehicleService;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public DriverVehicleController(
            DriverVehicleService driverVehicleService, AuthenticatedUserProvider authenticatedUserProvider) {
        this.driverVehicleService = driverVehicleService;
        this.authenticatedUserProvider = authenticatedUserProvider;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DriverVehicleCreateResponse> addVehicle(
            @Valid @ModelAttribute DriverVehicleCreateRequest request) {
        Long userId = authenticatedUserProvider.getCurrentUserId();
        try {
            DriverVehicleView created = driverVehicleService.addVehicle(userId, request);
            return ResponseEntity.ok(new DriverVehicleCreateResponse(true, "Vehicle added successfully", created));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest()
                    .body(new DriverVehicleCreateResponse(false, ex.getMessage(), null));
        } catch (ResourceNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new DriverVehicleCreateResponse(false, ex.getMessage(), null));
        }
    }
}
