package com.dztech.auth.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "driver_vehicles")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverVehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @Column(name = "vehicle_number", nullable = false, length = 25)
    private String vehicleNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type", nullable = false, length = 20)
    private VehicleType vehicleType;

    @Column(name = "manufactured_year", nullable = false, length = 7)
    private String manufacturedYear;

    @Lob
    @Column(name = "rc_image", columnDefinition = "LONGBLOB")
    private byte[] rcImage;

    @Column(name = "rc_image_object", length = 255)
    private String rcImageObject;

    @Column(name = "rc_image_content_type", nullable = false, length = 100)
    private String rcImageContentType;

    @Column(name = "insurance_expiry_date", nullable = false)
    private LocalDate insuranceExpiryDate;

    @Lob
    @Column(name = "insurance_image", columnDefinition = "LONGBLOB")
    private byte[] insuranceImage;

    @Column(name = "insurance_image_object", length = 255)
    private String insuranceImageObject;

    @Column(name = "insurance_image_content_type", nullable = false, length = 100)
    private String insuranceImageContentType;

    @Lob
    @Column(name = "pollution_certificate_image", columnDefinition = "LONGBLOB")
    private byte[] pollutionCertificateImage;

    @Column(name = "pollution_certificate_image_object", length = 255)
    private String pollutionCertificateImageObject;

    @Column(name = "pollution_certificate_image_content_type", nullable = false, length = 100)
    private String pollutionCertificateImageContentType;

    @Column(name = "brand", nullable = false, length = 100)
    private String brand;

    @Column(name = "model", nullable = false, length = 100)
    private String model;

    @Enumerated(EnumType.STRING)
    @Column(name = "transmission_type", length = 50)
    private TransmissionType transmissionType;

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
