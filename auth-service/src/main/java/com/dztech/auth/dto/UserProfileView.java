package com.dztech.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UserProfileView(
        Long id,
        String name,
        String phone,
        String email,
        @JsonProperty("isEmailVerified")
        boolean emailVerified,
        String address,
        PreferredLanguageView primaryPreferredLanguage,
        PreferredLanguageView secondaryPreferredLanguage,
        long vehicleCount,
        long completedBookings,
        long activeBookings) {
}
