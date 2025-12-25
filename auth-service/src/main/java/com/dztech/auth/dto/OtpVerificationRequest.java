package com.dztech.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record OtpVerificationRequest(
        @NotBlank(message = "Phone number is required")
        @Size(max = 25, message = "Phone number must be at most 25 characters")
        String phone,

        @NotBlank(message = "OTP is required")
        @Pattern(regexp = "\\d{4,8}", message = "OTP must be a 4 to 8 digit code")
        String otp,

        @Size(max = 512, message = "FCM token must be at most 512 characters")
        String fcmToken) {
}
