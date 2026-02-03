package com.dztech.auth.dto;

import com.dztech.auth.dto.DriverProfileView;

public record DriverChangeEmailResponse(
        boolean success,
        String message,
        DriverProfileView profile) {
}