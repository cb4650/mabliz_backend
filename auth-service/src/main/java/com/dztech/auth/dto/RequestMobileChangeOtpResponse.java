package com.dztech.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestMobileChangeOtpResponse {

    private boolean success;
    private String message;
    private String newPhone;
}