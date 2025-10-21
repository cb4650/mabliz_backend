package com.dztech.rayder.dto;

import com.dztech.rayder.model.VehicleFuelType;
import com.dztech.rayder.model.VehicleOwnershipType;
import com.dztech.rayder.model.VehicleTransmissionType;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record CreateVehicleRequest(
        @NotBlank(message = "Brand is required")
        @Size(max = 100, message = "Brand must be at most 100 characters")
        String brand,

        @NotBlank(message = "Model is required")
        @Size(max = 100, message = "Model must be at most 100 characters")
        String model,

        @NotNull(message = "Ownership type is required")
        VehicleOwnershipType ownershipType,

        @NotNull(message = "Transmission is required")
        VehicleTransmissionType transmission,

        @NotNull(message = "Fuel type is required")
        VehicleFuelType fuelType,

        @NotBlank(message = "Year is required")
        @Pattern(regexp = "\\d{4}", message = "Year must be a valid 4-digit year")
        String year,

        @NotBlank(message = "Policy number is required")
        @Size(max = 100, message = "Policy number must be at most 100 characters")
        String policyNo,

        @NotNull(message = "Start date is required")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate startDate,

        @NotNull(message = "Expiry date is required")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate expiryDate) {
}
