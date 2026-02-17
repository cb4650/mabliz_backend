package com.dztech.auth.dto;

import java.time.LocalDate;

public record DriverVehicleView(
        Long id,
        String vehicleNumber,
        String vehicleType,
        String manufacturedYear,
        LocalDate insuranceExpiryDate,
        String brand,
        String model,
        String transmissionType) {
}
