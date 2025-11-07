package com.dztech.auth.dto;

import java.time.LocalDate;

public record DriverVehicleView(
        Long id,
        String vehicleNumber,
        String vehicleType,
        String rcNumber,
        LocalDate insuranceExpiryDate,
        String brand,
        String model) {
}
