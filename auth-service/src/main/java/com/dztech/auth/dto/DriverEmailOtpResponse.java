package com.dztech.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DriverEmailOtpResponse(
        boolean success,
        String message,
        DriverEmailOtpResponse.Data data) {

    public record Data(String email, @JsonProperty("otp_expires_in") long otpExpiresIn) {
    }
}
