package com.dztech.rayder.dto;

import jakarta.validation.constraints.NotBlank;

public record AddressDto(
        @NotBlank(message = "House number or name is required")
        String houseName,

        @NotBlank(message = "Address line 1 is required")
        String addressLine1,

        String addressLine2) {
}
