package com.dztech.auth.controller;

import com.dztech.auth.dto.DriverProfileView;
import com.dztech.auth.dto.InternalDriverProfileResponse;
import com.dztech.auth.dto.TripConfirmedNotificationRequest;
import com.dztech.auth.dto.UserProfileView;
import com.dztech.auth.service.DriverNotificationService;
import com.dztech.auth.service.DriverProfileService;
import com.dztech.auth.service.ProfileService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/internal/notifications")
public class InternalNotificationController {

    private static final Logger log = LoggerFactory.getLogger(InternalNotificationController.class);
    private static final String INTERNAL_API_KEY_HEADER = "X-Internal-Api-Key";

    private final DriverNotificationService driverNotificationService;
    private final DriverProfileService driverProfileService;
    private final ProfileService profileService;
    private final String internalApiKey;

    public InternalNotificationController(
            DriverNotificationService driverNotificationService,
            DriverProfileService driverProfileService,
            ProfileService profileService,
            @Value("${internal.api.key:}") String internalApiKey) {
        this.driverNotificationService = driverNotificationService;
        this.driverProfileService = driverProfileService;
        this.profileService = profileService;
        this.internalApiKey = internalApiKey;
    }

    @PostMapping("/trip-confirmed")
    public ResponseEntity<Void> handleTripConfirmation(
            @RequestHeader(value = INTERNAL_API_KEY_HEADER, required = false) String apiKey,
            @Valid @RequestBody TripConfirmedNotificationRequest request) {
        log.info("Received trip confirmation notification request for bookingId: {}", request.bookingId());

        if (!StringUtils.hasText(internalApiKey) || !internalApiKey.equals(apiKey)) {
            log.warn("Unauthorized trip confirmation notification request - invalid API key");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.debug("API key validated, proceeding with trip confirmation notification");
        driverNotificationService.sendTripConfirmation(request);
        log.info("Trip confirmation notification processing completed for bookingId: {}", request.bookingId());
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/drivers/{driverId}")
    public ResponseEntity<InternalDriverProfileResponse> getDriverProfile(
            @RequestHeader(value = INTERNAL_API_KEY_HEADER, required = false) String apiKey,
            @PathVariable Long driverId) {
        log.info("Received request to get driver profile for driverId: {}", driverId);

        if (!StringUtils.hasText(internalApiKey) || !internalApiKey.equals(apiKey)) {
            log.warn("Unauthorized request to get driver profile - invalid API key");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            DriverProfileView profile = driverProfileService.getProfile(driverId);
            InternalDriverProfileResponse response = new InternalDriverProfileResponse(
                    profile.userId(),
                    profile.fullName(),
                    profile.email(),
                    profile.phone());
            log.info("Driver profile retrieved successfully for driverId: {}", driverId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving driver profile for driverId: {}", driverId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<InternalDriverProfileResponse> getUserProfile(
            @RequestHeader(value = INTERNAL_API_KEY_HEADER, required = false) String apiKey,
            @PathVariable Long userId) {
        log.info("Received request to get user profile for userId: {}", userId);

        if (!StringUtils.hasText(internalApiKey) || !internalApiKey.equals(apiKey)) {
            log.warn("Unauthorized request to get user profile - invalid API key");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            UserProfileView profile = profileService.getProfile(userId, null);
            InternalDriverProfileResponse response = new InternalDriverProfileResponse(
                    profile.id(),
                    profile.name(),
                    profile.email(),
                    profile.phone());
            log.info("User profile retrieved successfully for userId: {}", userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving user profile for userId: {}", userId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
