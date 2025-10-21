package com.dztech.auth.dto;

public record UserProfileUpdateResponse(boolean success, String message, UserProfileView data) {
}
