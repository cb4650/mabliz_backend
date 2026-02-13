package com.dztech.rayder.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "vehicles")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "brand_id", nullable = false)
    private CarBrand brand;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "model_id", nullable = false)
    private CarModel model;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "ownership_type", nullable = true, length = 30)
    private VehicleOwnershipType ownershipType;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "transmission", nullable = false, length = 30)
    private VehicleTransmissionType transmission;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "fuel_type", nullable = true, length = 20)
    private VehicleFuelType fuelType;

    @Column(nullable = true, length = 4)
    private String year;

    @Column(name = "policy_no", nullable = true, length = 100)
    private String policyNo;

    @Column(name = "start_date", nullable = true)
    private LocalDate startDate;

    @Column(name = "expiry_date", nullable = true)
    private LocalDate expiryDate;

    @Column(name = "vehicle_no", nullable = true, length = 20)
    private String vehicleNo;

    @Column(name = "insurance_no", nullable = true, length = 100)
    private String insuranceNo;

    @Column(name = "insurance_expiry", nullable = true)
    private LocalDate insuranceExpiry;

    @Column(name = "insurance_photo", nullable = true, length = 255)
    private String insurancePhoto;
}
