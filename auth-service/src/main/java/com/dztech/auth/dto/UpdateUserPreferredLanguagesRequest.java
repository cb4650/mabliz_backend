package com.dztech.auth.dto;

import jakarta.validation.constraints.Positive;

public record UpdateUserPreferredLanguagesRequest(
        @Positive(message = "Primary preferred language id must be positive")
        Long primaryPreferredLanguageId,

        @Positive(message = "Secondary preferred language id must be positive")
        Long secondaryPreferredLanguageId) {
}
