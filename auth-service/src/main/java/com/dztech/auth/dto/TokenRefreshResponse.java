package com.dztech.auth.dto;

public record TokenRefreshResponse(
        boolean success,
        String token,
        String refreshToken,
        Long userId,
        String name,
        String phone,
        String email) {
}
