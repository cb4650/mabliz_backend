package com.dztech.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HelpCategoryRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Category key is required")
    @Pattern(regexp = "^(trip|driver|payment)$", message = "Category key must be one of: trip, driver, payment")
    private String categoryKey;

    @NotNull(message = "App ID is required")
    @Pattern(regexp = "^(rydd|rydc)$", message = "App ID must be either 'rydd' or 'rydc'")
    private String appId;
}