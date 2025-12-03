package com.dztech.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePriceSettingsResponse {

    private String status;
    private String message;
    private PriceSettingsData data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PriceSettingsData {
        private String classId;
        private String className;
    }
}
