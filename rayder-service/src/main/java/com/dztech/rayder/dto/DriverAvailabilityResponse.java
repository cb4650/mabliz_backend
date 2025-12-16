package com.dztech.rayder.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record DriverAvailabilityResponse(
        boolean success,
        String message,
        DriverAvailabilityResponse.Data data) {

    public record Data(
            Long userId, boolean online, BigDecimal latitude, BigDecimal longitude, Instant updatedAt) {
    }
}
