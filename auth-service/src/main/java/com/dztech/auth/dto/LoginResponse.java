package com.dztech.auth.dto;

public record LoginResponse(
        boolean success,
        boolean newUser,
        String token,
        Long userId,
        String name,
        String phone,
        String email) {
}
