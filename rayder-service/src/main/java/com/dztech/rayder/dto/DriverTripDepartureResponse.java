package com.dztech.rayder.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record DriverTripDepartureResponse(
        boolean success,
        String message,
        DriverTripDepartureResponse.Data data) {

    public record Data(
            Long bookingId,
            Long driverId,
            Instant departedAt,
            BigDecimal latitude,
            BigDecimal longitude) {}
}
