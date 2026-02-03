package com.dztech.auth.dto;

import com.dztech.auth.dto.UserProfileView;

public record ChangeMobileResponse(
        boolean success,
        String message,
        UserProfileView profile) {
}