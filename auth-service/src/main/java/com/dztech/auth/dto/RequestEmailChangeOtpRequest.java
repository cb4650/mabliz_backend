package com.dztech.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestEmailChangeOtpRequest {

    @NotBlank(message = "New email is required")
    @Email(message = "New email must be valid")
    private String newEmail;
}