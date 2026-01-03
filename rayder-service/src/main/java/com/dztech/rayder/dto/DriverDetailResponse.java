package com.dztech.rayder.dto;

public record DriverDetailResponse(
        Long userId,
        String fullName,
        String email,
        String phone) {
}
