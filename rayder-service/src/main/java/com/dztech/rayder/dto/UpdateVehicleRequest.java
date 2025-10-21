package com.dztech.rayder.dto;

import com.dztech.rayder.model.VehicleFuelType;
import com.dztech.rayder.model.VehicleOwnershipType;
import com.dztech.rayder.model.VehicleTransmissionType;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record UpdateVehicleRequest(
        @Nullable
        @Size(max = 100, message = "Brand must be at most 100 characters")
        String brand,

        @Nullable
        @Size(max = 100, message = "Model must be at most 100 characters")
        String model,

        @Nullable
        VehicleOwnershipType ownershipType,

        @Nullable
        VehicleTransmissionType transmission,

        @Nullable
        VehicleFuelType fuelType,

        @Nullable
        @Pattern(regexp = "\\d{4}", message = "Year must be a valid 4-digit year")
        String year,

        @Nullable
        @Size(max = 100, message = "Policy number must be at most 100 characters")
        String policyNo,

        @Nullable
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate startDate,

        @Nullable
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate expiryDate) {
}
