package com.dztech.rayder.dto;

import java.time.Instant;

public record TripConfirmedNotificationRequest(
        Long bookingId, String pickupAddress, String dropAddress, Instant startTime, Instant endTime) {}
