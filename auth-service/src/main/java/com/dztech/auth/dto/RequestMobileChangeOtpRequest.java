package com.dztech.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestMobileChangeOtpRequest {

    @NotBlank(message = "New mobile number is required")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Mobile number must be 10-15 digits")
    private String newPhone;
}