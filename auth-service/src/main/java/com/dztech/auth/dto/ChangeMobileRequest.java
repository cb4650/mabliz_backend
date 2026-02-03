package com.dztech.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ChangeMobileRequest(
        @NotBlank(message = "New phone number is required")
        @Size(max = 25, message = "Phone number must be at most 25 characters")
        String newPhone,

        @NotBlank(message = "Email OTP is required")
        @Pattern(regexp = "\\d{4,8}", message = "Email OTP must be a 4 to 8 digit code")
        String emailOtp) {
}