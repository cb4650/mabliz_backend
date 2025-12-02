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
public class PriceSettingsView {

    private BigDecimal baseFare;
    private BigDecimal perHour;
    private BigDecimal lateNightCharges;
    private BigDecimal extraHourCharges;
    private BigDecimal foodCharges;
    private BigDecimal festivalCharges;
}
