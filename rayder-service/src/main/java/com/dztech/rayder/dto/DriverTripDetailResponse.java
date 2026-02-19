package com.dztech.rayder.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record DriverTripDetailResponse(
        boolean success,
        String message,
        DriverTripDetailResponse.Data data) {

    public record Data(
            Long bookingId,
            String bookingType,
            String tripOption,
            Integer hours,
            Instant startTime,
            Instant endTime,
            String pickupAddress,
            BigDecimal pickupLatitude,
            BigDecimal pickupLongitude,
            String dropAddress,
            BigDecimal dropLatitude,
            BigDecimal dropLongitude,
            String customerName,
            String customerPhone,
            String customerEmail,
            String customerAddress,
            Instant driverReachedAt) {}
}
