package com.dztech.rayder.dto;

public record InternalDriverProfileResponse(
        Long userId,
        String fullName,
        String email,
        String phone) {
}
