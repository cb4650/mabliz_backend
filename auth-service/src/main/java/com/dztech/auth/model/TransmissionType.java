package com.dztech.auth.model;

public enum TransmissionType {
    MANUAL,
    AUTOMATIC,
    SEMI_AUTOMATIC,
    ELECTRIC;

    public static TransmissionType fromString(String value) {
        if (value == null) {
            throw new IllegalArgumentException("transmissionType is required");
        }
        try {
            return TransmissionType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("transmissionType must be one of: MANUAL, AUTOMATIC, SEMI_AUTOMATIC, ELECTRIC");
        }
    }
}
