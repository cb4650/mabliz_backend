package com.dztech.rayder.dto;

import java.time.Instant;

public record DriverReachedResponse(
        boolean success,
        String message,
        Instant reachedAt) {
}