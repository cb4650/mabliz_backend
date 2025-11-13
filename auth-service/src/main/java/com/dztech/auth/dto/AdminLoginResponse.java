package com.dztech.auth.dto;

public record AdminLoginResponse(
        boolean success,
        String accessToken,
        String refreshToken,
        Long adminId,
        String name,
        String phone) {
}
