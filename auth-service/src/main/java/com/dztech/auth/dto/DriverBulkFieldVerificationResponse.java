package com.dztech.auth.dto;

import java.util.List;

public record DriverBulkFieldVerificationResponse(
        boolean success,
        String message,
        List<DriverFieldVerificationView> verifiedFields) {}
