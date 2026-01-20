package com.dztech.auth.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FallbackPriceSettingsRequest {

    @NotNull(message = "Model type is required")
    private ModelType modelType;

    @NotNull(message = "Minimum hours is required")
    @Positive(message = "Minimum hours must be positive")
    private Integer minimumHours;

    @NotNull(message = "Base fare is required")
    @PositiveOrZero(message = "Base fare cannot be negative")
    private BigDecimal baseFare;

    @NotNull(message = "Extra fare per hour is required")
    @PositiveOrZero(message = "Extra fare per hour cannot be negative")
    private BigDecimal extraFarePerHour;

    @NotNull(message = "Night charges is required")
    @PositiveOrZero(message = "Night charges cannot be negative")
    private BigDecimal nightCharges;

    @NotNull(message = "Festive charges is required")
    @PositiveOrZero(message = "Festive charges cannot be negative")
    private BigDecimal festiveCharges;

    @NotNull(message = "Rain charge is required")
    @PositiveOrZero(message = "Rain charge cannot be negative")
    private BigDecimal rainCharge;

    @NotNull(message = "Drop charges per 5km is required")
    @PositiveOrZero(message = "Drop charges per 5km cannot be negative")
    private BigDecimal dropChargesPer5km;

    @NotNull(message = "Drop charges per 100km is required")
    @PositiveOrZero(message = "Drop charges per 100km cannot be negative")
    private BigDecimal dropChargesPer100km;

    @NotNull(message = "Drop limit kms is required")
    @PositiveOrZero(message = "Drop limit kms cannot be negative")
    private Integer dropLimitKms;

    @NotNull(message = "Driver cancellation penalty is required")
    @PositiveOrZero(message = "Driver cancellation penalty cannot be negative")
    private BigDecimal driverCancellationPenalty;

    public enum ModelType {
        LOCAL,
        OUTSTATION
    }
}
