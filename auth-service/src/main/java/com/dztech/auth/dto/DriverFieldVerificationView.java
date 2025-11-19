package com.dztech.auth.dto;

import com.dztech.auth.model.DriverFieldVerificationStatus;
import java.time.Instant;

public record DriverFieldVerificationView(
        String fieldName,
        DriverFieldVerificationStatus status,
        String notes,
        Long verifiedByAdminId,
        Instant verifiedAt) {}
