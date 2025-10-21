package com.dztech.rayder.dto;

import com.dztech.rayder.model.VehicleFuelType;
import com.dztech.rayder.model.VehicleOwnershipType;
import com.dztech.rayder.model.VehicleTransmissionType;
import java.time.LocalDate;

public record VehicleResponse(
        Long id,
        String brand,
        String model,
        VehicleOwnershipType ownershipType,
        VehicleTransmissionType transmission,
        VehicleFuelType fuelType,
        String year,
        String policyNo,
        LocalDate startDate,
        LocalDate expiryDate) {
}
