package com.dztech.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record OtpVerificationRequest(
        @NotBlank(message = "Phone number is required")
        @Size(max = 25, message = "Phone number must be at most 25 characters")
        String phone,

        @NotBlank(message = "OTP is required")
        @Pattern(regexp = "\\d{6}", message = "OTP must be a 6 digit code")
        String otp) {
}
