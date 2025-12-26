package com.dztech.auth.controller;

import com.dztech.auth.dto.TripConfirmedNotificationRequest;
import com.dztech.auth.service.DriverNotificationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/internal/notifications")
public class InternalNotificationController {

    private static final String INTERNAL_API_KEY_HEADER = "X-Internal-Api-Key";

    private final DriverNotificationService driverNotificationService;
    private final String internalApiKey;

    public InternalNotificationController(
            DriverNotificationService driverNotificationService,
            @Value("${internal.api.key:}") String internalApiKey) {
        this.driverNotificationService = driverNotificationService;
        this.internalApiKey = internalApiKey;
    }

    @PostMapping("/trip-confirmed")
    public ResponseEntity<Void> handleTripConfirmation(
            @RequestHeader(value = INTERNAL_API_KEY_HEADER, required = false) String apiKey,
            @Valid @RequestBody TripConfirmedNotificationRequest request) {
        if (!StringUtils.hasText(internalApiKey) || !internalApiKey.equals(apiKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        driverNotificationService.sendTripConfirmation(request);
        return ResponseEntity.accepted().build();
    }
}
