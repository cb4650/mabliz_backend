package com.dztech.rayder.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record DriverTripCloseResponse(
        boolean success,
        String message,
        DriverTripCloseResponse.Data data) {

    public record Data(
            Long bookingId,
            Long driverId,
            Instant closedAt,
            BigDecimal latitude,
            BigDecimal longitude) {}
}
