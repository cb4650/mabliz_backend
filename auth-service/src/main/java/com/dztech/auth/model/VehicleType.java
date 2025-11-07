package com.dztech.auth.model;

public enum VehicleType {
    COMMERCIAL,
    PRIVATE;

    public static VehicleType fromString(String value) {
        if (value == null) {
            throw new IllegalArgumentException("vehicleType is required");
        }
        try {
            return VehicleType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("vehicleType must be either 'commercial' or 'private'");
        }
    }
}
