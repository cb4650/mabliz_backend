package com.dztech.auth.dto;

import java.time.LocalDate;

public record DriverVehicleDetailView(
        Long vehicleId,
        String vehicleNumber,
        String vehicleType,
        String manufacturedYear,
        LocalDate insuranceExpiryDate,
        String brand,
        String model,
        DriverDocumentView rcDocument,
        DriverDocumentView insuranceDocument,
        DriverDocumentView pollutionDocument) {}
