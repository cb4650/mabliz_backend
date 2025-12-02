package com.dztech.auth.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePriceSettingsRequest {

    @NotNull
    private PriceSettingsUpdate priceSettings;

    @NotNull
    private CommissionSettingsUpdate commissionSettings;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PriceSettingsUpdate {
        @NotNull
        private BigDecimal baseFare;
        @NotNull
        private BigDecimal perHour;
        @NotNull
        private BigDecimal lateNightCharges;
        @NotNull
        private BigDecimal extraHourCharges;
        @NotNull
        private BigDecimal foodCharges;
        @NotNull
        private BigDecimal festivalCharges;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommissionSettingsUpdate {
        @NotNull
        private BigDecimal platformCommission;
        @NotNull
        private BigDecimal festivalCommission;
    }
}
