package com.dztech.rayder.dto;

import java.math.BigDecimal;
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
        DriverLocationResponse drop,
        String bookingStatus,
        BigDecimal baseFare,
        BigDecimal lateNightCharges,
        BigDecimal extraHourCharges,
        BigDecimal festivalCharges,
        BigDecimal estimate) {
}
