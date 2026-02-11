package com.dztech.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CustomerEmailVerificationRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be a valid email address")
        @Size(max = 150, message = "Email must be at most 150 characters")
        String email,

        @NotBlank(message = "OTP is required")
        @Pattern(regexp = "\\d{4,8}", message = "OTP must be a 4 to 8 digit code")
        String otp) {
}