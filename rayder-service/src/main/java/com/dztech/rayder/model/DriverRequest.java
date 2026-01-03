package com.dztech.rayder.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Random;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "driver_requests")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Column(name = "booking_type", nullable = false, length = 50)
    private String bookingType;

    @Column(name = "trip_option", nullable = false, length = 50)
    private String tripOption;

    @Column(name = "hours")
    private Integer hours;

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @Column(name = "end_time", nullable = false)
    private Instant endTime;

    @Column(name = "pickup_address", nullable = false, length = 255)
    private String pickupAddress;

    @Column(name = "pickup_latitude", nullable = false, precision = 10, scale = 6)
    private BigDecimal pickupLatitude;

    @Column(name = "pickup_longitude", nullable = false, precision = 10, scale = 6)
    private BigDecimal pickupLongitude;

    @Column(name = "drop_address", nullable = false, length = 255)
    private String dropAddress;

    @Column(name = "drop_latitude", nullable = false, precision = 10, scale = 6)
    private BigDecimal dropLatitude;

    @Column(name = "drop_longitude", nullable = false, precision = 10, scale = 6)
    private BigDecimal dropLongitude;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "accepted_driver_id")
    private Long acceptedDriverId;

    @Column(name = "accepted_at")
    private Instant acceptedAt;

    @Column(name = "departed_at")
    private Instant departedAt;

    @Column(name = "departed_latitude", precision = 10, scale = 6)
    private BigDecimal departedLatitude;

    @Column(name = "departed_longitude", precision = 10, scale = 6)
    private BigDecimal departedLongitude;

    @Column(name = "estimate", nullable = false, precision = 12, scale = 2)
    private BigDecimal estimate;

    @Column(name = "base_fare", nullable = false, precision = 12, scale = 2)
    private BigDecimal baseFare;

    @Column(name = "late_night_charges", nullable = false, precision = 12, scale = 2)
    private BigDecimal lateNightCharges;

    @Column(name = "extra_hour_charges", nullable = false, precision = 12, scale = 2)
    private BigDecimal extraHourCharges;

    @Column(name = "festival_charges", nullable = false, precision = 12, scale = 2)
    private BigDecimal festivalCharges;

    @Column(name = "trip_otp", length = 4)
    private String tripOtp;

    @PrePersist
    private void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (status == null) {
            status = "PENDING";
        }
        if (estimate == null) {
            estimate = BigDecimal.ZERO;
        }
        if (baseFare == null) {
            baseFare = BigDecimal.ZERO;
        }
        if (lateNightCharges == null) {
            lateNightCharges = BigDecimal.ZERO;
        }
        if (extraHourCharges == null) {
            extraHourCharges = BigDecimal.ZERO;
        }
        if (festivalCharges == null) {
            festivalCharges = BigDecimal.ZERO;
        }
        if (tripOtp == null) {
            tripOtp = generateOtp();
        }
    }

    private String generateOtp() {
        Random random = new Random();
        int otp = 1000 + random.nextInt(9000); // Generates 1000-9999
        return String.valueOf(otp);
    }
}
