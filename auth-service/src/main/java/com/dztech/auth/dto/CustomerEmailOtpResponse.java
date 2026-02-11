package com.dztech.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CustomerEmailOtpResponse(
        boolean success,
        String message,
        CustomerEmailOtpResponse.Data data) {

    public record Data(String email, @JsonProperty("otp_expires_in") long otpExpiresIn) {
    }
}