package com.dztech.auth.exception;

import java.time.Duration;

public class OtpBlockedException extends RuntimeException {

    private final Duration remainingTime;

    public OtpBlockedException(String message, Duration remainingTime) {
        super(message);
        this.remainingTime = remainingTime;
    }

    public Duration getRemainingTime() {
        return remainingTime;
    }

    public long getRemainingSeconds() {
        return remainingTime != null ? remainingTime.getSeconds() : 0;
    }
}
