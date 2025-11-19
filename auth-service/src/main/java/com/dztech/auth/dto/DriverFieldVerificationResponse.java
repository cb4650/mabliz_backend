package com.dztech.auth.dto;

public record DriverFieldVerificationResponse(
        boolean success, String message, DriverFieldVerificationView verification) {}
