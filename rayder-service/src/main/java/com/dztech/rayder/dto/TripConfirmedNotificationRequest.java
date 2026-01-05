package com.dztech.rayder.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;

public record TripConfirmedNotificationRequest(
        @NotNull Long bookingId,
        @NotBlank String pickupAddress,
        @NotBlank String dropAddress,
        @NotNull Instant startTime,
        @NotNull Instant endTime,
        @NotBlank String customerName,
        @NotNull BigDecimal estimatedFare,
        @NotBlank String vehicleBrand,
        @NotBlank String vehicleModel,
        String vehicleNumber) {}
