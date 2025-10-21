package com.dztech.rayder.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;

public enum VehicleOwnershipType {
    OWN_VEHICLE("Own Vehicle"),
    COMMERCIAL("Commercial");

    private final String value;

    VehicleOwnershipType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static VehicleOwnershipType fromValue(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Ownership type is required");
        }
        return Arrays.stream(values())
                .filter(type -> type.value.equalsIgnoreCase(value.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid ownership type: " + value));
    }
}
