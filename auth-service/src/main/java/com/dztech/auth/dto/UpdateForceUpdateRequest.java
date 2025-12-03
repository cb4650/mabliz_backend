package com.dztech.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateForceUpdateRequest(
        @NotBlank(message = "Version is required")
        @Size(max = 20, message = "Version must be at most 20 characters")
        @Pattern(regexp = "^\\d+\\.\\d+\\.\\d+$", message = "Version must be in format x.x.x")
        String version) {
}
