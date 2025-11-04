package com.dztech.auth.dto;

public record DriverEmailVerificationResponse(
        boolean success,
        String message,
        DriverEmailVerificationResponse.Data data) {

    public record Data(DriverEmailVerificationResponse.User user, String token) {
    }

    public record User(String name, String email) {
    }
}
