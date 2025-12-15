package com.dztech.rayder.dto;

public record DriverRequestAcknowledgement(
        boolean success,
        String message,
        DriverRequestDetails data) {
}
