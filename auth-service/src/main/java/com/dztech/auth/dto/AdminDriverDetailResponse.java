package com.dztech.auth.dto;

import com.dztech.auth.model.DriverProfileStatus;
import java.util.List;

public record AdminDriverDetailResponse(
        boolean success,
        Long driverId,
        DriverProfileStatus status,
        DriverProfileDetailView profile,
        List<DriverVehicleDetailView> vehicles,
        List<DriverFieldVerificationView> fieldVerifications) {}
