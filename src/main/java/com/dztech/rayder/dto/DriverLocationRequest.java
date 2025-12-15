package com.dztech.rayder.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record DriverLocationRequest(
        @NotBlank(message = "Address is required")
        String address,

        @NotNull(message = "Latitude is required")
        @DecimalMin(value = "-90.0", message = "Latitude cannot be less than -90")
        @DecimalMax(value = "90.0", message = "Latitude cannot be greater than 90")
        @Digits(integer = 3, fraction = 6, message = "Latitude must have at most 6 decimal places")
        BigDecimal latitude,

        @NotNull(message = "Longitude is required")
        @DecimalMin(value = "-180.0", message = "Longitude cannot be less than -180")
        @DecimalMax(value = "180.0", message = "Longitude cannot be greater than 180")
        @Digits(integer = 3, fraction = 6, message = "Longitude must have at most 6 decimal places")
        BigDecimal longitude) {
}
