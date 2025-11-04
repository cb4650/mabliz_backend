package com.dztech.auth.dto;

public record DriverProfileUpdateResponse(boolean success, String message, DriverProfileView data) {
}
