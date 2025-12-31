package com.dztech.rayder.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record DriverDepartureRequest(
        @NotNull(message = "Latitude is required") BigDecimal latitude,
        @NotNull(message = "Longitude is required") BigDecimal longitude) {}
