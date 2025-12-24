package com.dztech.rayder.dto;

public record SavedAddressOperationResponse(
        boolean success,
        String message,
        SavedAddressResponse address) {
}
