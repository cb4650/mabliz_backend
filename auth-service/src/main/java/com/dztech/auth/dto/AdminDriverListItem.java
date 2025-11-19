package com.dztech.auth.dto;

import com.dztech.auth.model.DriverProfileStatus;
import java.time.Instant;
import java.util.List;

public record AdminDriverListItem(
        Long driverId,
        String fullName,
        String email,
        String phone,
        DriverProfileStatus status,
        Instant updatedAt,
        List<DriverFieldVerificationView> fieldVerifications) {}
