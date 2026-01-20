package com.dztech.auth.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "district_price_settings")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DistrictPriceSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "district_id", nullable = false)
    private District district;

    @Enumerated(EnumType.STRING)
    @Column(name = "model_type", nullable = false)
    private ModelType modelType;

    @Column(name = "minimum_hours", nullable = false)
    private Integer minimumHours;

    @Column(name = "base_fare", nullable = false, precision = 10, scale = 2)
    private BigDecimal baseFare;

    @Column(name = "extra_fare_per_hour", nullable = false, precision = 10, scale = 2)
    private BigDecimal extraFarePerHour;

    @Column(name = "night_charges", nullable = false, precision = 10, scale = 2)
    private BigDecimal nightCharges;

    @Column(name = "festive_charges", nullable = false, precision = 10, scale = 2)
    private BigDecimal festiveCharges;

    @Column(name = "rain_charge", nullable = false, precision = 10, scale = 2)
    private BigDecimal rainCharge;

    @Column(name = "drop_charges_per_5km", nullable = false, precision = 10, scale = 2)
    private BigDecimal dropChargesPer5km;

    @Column(name = "drop_charges_per_100km", nullable = false, precision = 10, scale = 2)
    private BigDecimal dropChargesPer100km;

    @Column(name = "drop_limit_kms", nullable = false)
    private Integer dropLimitKms;

    @Column(name = "driver_cancellation_penalty", nullable = false, precision = 10, scale = 2)
    private BigDecimal driverCancellationPenalty;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public enum ModelType {
        LOCAL,
        OUTSTATION
    }
}
