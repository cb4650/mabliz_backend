package com.dztech.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;

public record DriverProfileView(
        Long userId,
        String fullName,
        LocalDate dob,
        String gender,
        String email,
        @JsonProperty("isEmailVerified")
        boolean emailVerified,
        String phone,
        String emergencyContactName,
        String emergencyContactNumber,
        String permanentAddress,
        String languages,
        String licenseNumber,
        String licenseType,
        Integer experienceYears,
        String govIdType,
        String govIdNumber) {
}
