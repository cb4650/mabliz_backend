package com.dztech.rayder.dto;

import java.math.BigDecimal;

public record DriverLocationResponse(
        String address,
        BigDecimal latitude,
        BigDecimal longitude) {
}
