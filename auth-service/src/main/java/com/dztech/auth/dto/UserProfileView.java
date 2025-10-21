package com.dztech.auth.dto;

public record UserProfileView(
        Long id,
        String name,
        String phone,
        String email,
        String address,
        long vehicleCount,
        long completedBookings,
        long activeBookings) {
}
