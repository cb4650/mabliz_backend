package com.dztech.auth.dto;

import com.dztech.auth.dto.UserProfileView;

public record ChangeEmailResponse(
        boolean success,
        String message,
        UserProfileView profile) {
}