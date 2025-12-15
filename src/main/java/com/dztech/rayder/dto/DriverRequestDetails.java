package com.dztech.rayder.dto;

import java.time.Instant;

public record DriverRequestDetails(
        Long id,
        String bookingType,
        String tripOption,
        Long vehicleId,
        Integer hours,
        Instant startTime,
        Instant endTime,
        DriverLocationResponse pickup,
        DriverLocationResponse drop) {
}
