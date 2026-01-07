package com.dztech.rayder.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record DriverTripListResponse(
        boolean success,
        String message,
        List<DriverTripListItem> data) {

    public record DriverTripListItem(
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
            String userName,
            String userPhone,
            String status,
            Instant createdAt,
            BigDecimal estimate) {}
}
