package com.dztech.rayder.dto;

import java.time.Instant;

public record DriverTripActionResponse(
        boolean success,
        String message,
        DriverTripActionResponse.Data data) {

    public record Data(
            Long bookingId, Long driverId, String decision, Long acceptedDriverId, Instant respondedAt) {}
}
