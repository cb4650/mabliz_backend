package com.dztech.rayder.dto;

import java.util.List;

public record SavedAddressListResponse(
        boolean success,
        List<SavedAddressResponse> addresses) {
}
