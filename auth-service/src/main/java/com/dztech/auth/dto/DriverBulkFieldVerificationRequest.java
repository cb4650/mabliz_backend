package com.dztech.auth.dto;

import com.dztech.auth.model.DriverFieldVerificationStatus;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record DriverBulkFieldVerificationRequest(
        @NotNull Long driverId,
        @NotEmpty List<FieldVerification> verifications) {

    public record FieldVerification(
            @NotNull String fieldName,
            @NotNull DriverFieldVerificationStatus status,
            String notes) {}
}
