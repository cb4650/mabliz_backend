package com.dztech.auth.dto;

import com.dztech.auth.dto.DriverProfileView;

public record DriverChangeMobileResponse(
        boolean success,
        String message,
        DriverProfileView profile) {
}