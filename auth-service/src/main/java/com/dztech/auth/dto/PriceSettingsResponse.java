package com.dztech.auth.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceSettingsResponse {

    private String status;
    private PriceSettingsData data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PriceSettingsData {
        private List<ClassPriceSettingsView> classes;
    }
}
