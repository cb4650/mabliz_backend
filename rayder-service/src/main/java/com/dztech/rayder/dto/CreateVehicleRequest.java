package com.dztech.rayder.dto;

import com.dztech.rayder.model.VehicleFuelType;
import com.dztech.rayder.model.VehicleOwnershipType;
import com.dztech.rayder.model.VehicleTransmissionType;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record CreateVehicleRequest(
        @NotNull(message = "Brand is required")
        @Positive(message = "Brand must be a positive id")
        Long brandId,

        @NotNull(message = "Model is required")
        @Positive(message = "Model must be a positive id")
        Long modelId,

        VehicleOwnershipType ownershipType,

        @NotNull(message = "Transmission is required")
        VehicleTransmissionType transmission,

        VehicleFuelType fuelType,

        String year,

        String policyNo,

        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate startDate,

        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate expiryDate) {
}
