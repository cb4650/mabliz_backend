package com.dztech.auth.dto;

/**
 * Response for checking if a user is new for driver registration.
 * Only checks the driver table, ignoring the user table.
 */
public record NewUserCheckResponse(boolean success, String message, boolean isNewUser) {
}
