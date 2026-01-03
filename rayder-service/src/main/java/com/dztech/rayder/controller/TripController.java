package com.dztech.rayder.controller;

import com.dztech.rayder.dto.OtpVerificationRequest;
import com.dztech.rayder.dto.OtpVerificationResponse;
import com.dztech.rayder.dto.TripDetailResponse;
import com.dztech.rayder.dto.VehicleCompletionResponse;
import com.dztech.rayder.security.AuthenticatedUserProvider;
import com.dztech.rayder.service.DriverRequestService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trips")
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

    @GetMapping("/{bookingId}/vehicle-completion")
    public ResponseEntity<VehicleCompletionResponse> checkVehicleCompletion(@PathVariable Long bookingId) {
        Long userId = authenticatedUserProvider.getCurrentUserId();
        VehicleCompletionResponse response = driverRequestService.checkVehicleCompletion(userId, bookingId);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/{bookingId}/verify-otp", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<OtpVerificationResponse> verifyOtp(
            @PathVariable Long bookingId,
            @ModelAttribute OtpVerificationRequest request) {
        Long userId = authenticatedUserProvider.getCurrentUserId();
        OtpVerificationResponse response = driverRequestService.verifyOtp(userId, bookingId, request);
        return ResponseEntity.ok(response);
    }
}
