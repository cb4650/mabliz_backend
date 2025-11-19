package com.dztech.auth.dto;

import com.dztech.auth.model.DriverFieldVerificationStatus;
import com.dztech.auth.model.DriverProfileStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DriverFieldVerificationRequest(
        @NotBlank String fieldName,
        @NotNull DriverFieldVerificationStatus status,
        String notes,
        DriverProfileStatus overallStatus) {}
