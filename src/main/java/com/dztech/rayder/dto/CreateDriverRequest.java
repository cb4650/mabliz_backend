package com.dztech.rayder.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.Instant;

public record CreateDriverRequest(
        @NotBlank(message = "Booking type is required")
        String bookingType,

        @NotBlank(message = "Trip option is required")
        String tripOption,

        @NotNull(message = "Vehicle id is required")
        @Positive(message = "Vehicle id must be positive")
        Long vehicleId,

        @Positive(message = "Hours must be a positive number")
        Integer hours,

        @NotNull(message = "Start time is required")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC")
        Instant startTime,

        @NotNull(message = "End time is required")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC")
        Instant endTime,

        @NotNull(message = "Pickup location is required")
        @Valid
        DriverLocationRequest pickup,

        @NotNull(message = "Drop location is required")
        @Valid
        DriverLocationRequest drop) {
}
