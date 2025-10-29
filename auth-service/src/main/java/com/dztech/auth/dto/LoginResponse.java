package com.dztech.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LoginResponse(
        boolean success,
        boolean newUser,
        String token,
        String refreshToken,
        Long userId,
        String name,
        String phone,
        String email,
        @JsonProperty("isEmailVerified")
        boolean emailVerified) {
}
