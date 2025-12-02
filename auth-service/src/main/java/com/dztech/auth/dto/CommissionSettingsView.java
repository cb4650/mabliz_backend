package com.dztech.auth.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommissionSettingsView {

    private BigDecimal platformCommission;
    private BigDecimal festivalCommission;
}
