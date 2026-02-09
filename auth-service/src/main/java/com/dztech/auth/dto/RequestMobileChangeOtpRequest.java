package com.dztech.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RequestMobileChangeOtpRequest(
        @NotBlank(message = "New phone number is required")
        @Size(max = 25, message = "Phone number must be at most 25 characters")
        String newPhone) {
}