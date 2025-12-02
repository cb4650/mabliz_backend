package com.dztech.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassPriceSettingsView {

    private String id;
    private String name;
    private PriceSettingsView priceSettings;
    private CommissionSettingsView commissionSettings;
}
