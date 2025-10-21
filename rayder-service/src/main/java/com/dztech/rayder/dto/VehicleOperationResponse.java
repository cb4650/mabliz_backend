package com.dztech.rayder.dto;

public record VehicleOperationResponse(
        boolean success,
        String message,
        VehicleResponse data) {
}
