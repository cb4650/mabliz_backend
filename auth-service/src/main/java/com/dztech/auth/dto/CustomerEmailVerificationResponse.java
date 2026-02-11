package com.dztech.auth.dto;

public record CustomerEmailVerificationResponse(
        boolean success,
        String message,
        CustomerEmailVerificationResponse.Data data) {

    public record Data(CustomerEmailVerificationResponse.User user, String token) {
    }

    public record User(String name, String email) {
    }
}