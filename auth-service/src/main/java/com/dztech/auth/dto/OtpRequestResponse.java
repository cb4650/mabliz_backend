package com.dztech.auth.dto;

public record OtpRequestResponse(boolean success, String message, boolean newUser) {
}
