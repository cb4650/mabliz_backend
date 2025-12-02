package com.dztech.auth.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "price_settings")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "class_name", nullable = false, unique = true, length = 100)
    private String className;

    @Column(name = "base_fare", nullable = false, precision = 10, scale = 2)
    private BigDecimal baseFare;

    @Column(name = "per_hour", nullable = false, precision = 10, scale = 2)
    private BigDecimal perHour;

    @Column(name = "late_night_charges", nullable = false, precision = 10, scale = 2)
    private BigDecimal lateNightCharges;

    @Column(name = "extra_hour_charges", nullable = false, precision = 10, scale = 2)
    private BigDecimal extraHourCharges;

    @Column(name = "food_charges", nullable = false, precision = 10, scale = 2)
    private BigDecimal foodCharges;

    @Column(name = "festival_charges", nullable = false, precision = 10, scale = 2)
    private BigDecimal festivalCharges;

    @Column(name = "platform_commission", nullable = false, precision = 5, scale = 2)
    private BigDecimal platformCommission;

    @Column(name = "festival_commission", nullable = false, precision = 5, scale = 2)
    private BigDecimal festivalCommission;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
