package com.dztech.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DistrictPriceSettingsResponse {

    private Long id;
    private Long districtId;
    private String districtName;
    private ModelType modelType;
    private Integer minimumHours;
    private BigDecimal baseFare;
    private BigDecimal extraFarePerHour;
    private BigDecimal nightCharges;
    private BigDecimal festiveCharges;
    private BigDecimal rainCharge;
    private BigDecimal dropChargesPer5km;
    private BigDecimal dropChargesPer100km;
    private Integer dropLimitKms;
    private BigDecimal driverCancellationPenalty;
    private Instant createdAt;
    private Instant updatedAt;

    public enum ModelType {
        LOCAL,
        OUTSTATION
    }
}
