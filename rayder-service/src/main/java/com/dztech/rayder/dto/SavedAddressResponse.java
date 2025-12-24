package com.dztech.rayder.dto;

import java.math.BigDecimal;

public record SavedAddressResponse(
        Long id,
        String label,
        String receiverPhoneNumber,
        BigDecimal latitude,
        BigDecimal longitude,
        AddressDto address) {
}
