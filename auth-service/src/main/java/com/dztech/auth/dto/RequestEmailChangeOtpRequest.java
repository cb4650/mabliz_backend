package com.dztech.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RequestEmailChangeOtpRequest(
        @NotBlank(message = "New email is required")
        @Email(message = "New email must be a valid email address")
        @Size(max = 150, message = "Email must be at most 150 characters")
        String newEmail) {
}