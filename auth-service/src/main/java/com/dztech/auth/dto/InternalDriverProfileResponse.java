package com.dztech.auth.dto;

public record InternalDriverProfileResponse(
        Long userId,
        String fullName,
        String email,
        String phone) {
}
