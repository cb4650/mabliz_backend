package com.dztech.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateUserProfileRequest(
        @Size(max = 150, message = "Name must be at most 150 characters")
        String name,

        @Email(message = "Email must be a valid email address")
        @Size(max = 150, message = "Email must be at most 150 characters")
        String email,

        @Size(max = 255, message = "Address must be at most 255 characters")
        String address) {
}
