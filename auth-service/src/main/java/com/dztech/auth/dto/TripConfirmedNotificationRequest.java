package com.dztech.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record TripConfirmedNotificationRequest(
        @NotNull Long bookingId,
        @NotBlank String pickupAddress,
        @NotBlank String dropAddress,
        @NotNull Instant startTime,
        @NotNull Instant endTime) {}
