package com.dztech.auth.dto;

public record DriverVehicleCreateResponse(boolean success, String message, DriverVehicleView data) {
}
