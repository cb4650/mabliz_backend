package com.dztech.auth.dto;

public record AdminRegistrationResponse(
        boolean success,
        String message,
        Long adminId,
        String name,
        String phone) {
}
