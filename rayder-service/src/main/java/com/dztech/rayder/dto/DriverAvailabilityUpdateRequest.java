package com.dztech.rayder.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.AssertTrue;
import java.math.BigDecimal;

public record DriverAvailabilityUpdateRequest(
        @NotNull(message = "online is required")
        Boolean online,

        @DecimalMin(value = "-90.0", message = "Latitude cannot be less than -90")
        @DecimalMax(value = "90.0", message = "Latitude cannot be greater than 90")
        @Digits(integer = 3, fraction = 6, message = "Latitude must have at most 6 decimal places")
        BigDecimal latitude,

        @DecimalMin(value = "-180.0", message = "Longitude cannot be less than -180")
        @DecimalMax(value = "180.0", message = "Longitude cannot be greater than 180")
        @Digits(integer = 3, fraction = 6, message = "Longitude must have at most 6 decimal places")
        BigDecimal longitude) {

    @AssertTrue(message = "latitude and longitude are required")
    public boolean isLocationRequiredWhenOnline() {
        if (Boolean.TRUE.equals(online)) {
            return latitude != null && longitude != null;
        }
        return true;
    }
}
