package com.dztech.rayder.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;

public enum VehicleTransmissionType {
    MANUAL("Manual"),
    AUTOMATIC("Automatic"),
    SEMI_AUTOMATIC_IMT("Semi-Automatic IMT");

    private final String value;

    VehicleTransmissionType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static VehicleTransmissionType fromValue(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Transmission type is required");
        }
        return Arrays.stream(values())
                .filter(type -> type.value.equalsIgnoreCase(value.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid transmission type: " + value));
    }
}
