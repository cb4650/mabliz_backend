package com.dztech.auth.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "driver_profiles")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverProfile {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(name = "dob")
    private String dob;

    @Column(length = 20)
    private String gender;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(length = 25)
    private String phone;

    @Column(name = "fcm_token", length = 512)
    private String fcmToken;

    @Column(name = "emergency_contact_name", length = 150)
    private String emergencyContactName;

    @Column(name = "emergency_contact_number", length = 25)
    private String emergencyContactNumber;

    @Column(name = "permanent_address", length = 255)
    private String permanentAddress;

    @ElementCollection
    @CollectionTable(name = "driver_languages", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "language")
    private List<String> languages;

    @Column(name = "license_number", length = 50)
    private String licenseNumber;

    @ElementCollection
    @CollectionTable(name = "driver_license_types", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "license_type")
    private List<String> licenseType;

    @ElementCollection
    @CollectionTable(name = "driver_preferred_brands", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "brand_id")
    private List<Long> preferredBrands;

    @Column(name = "experience")
    private String experience;

    @Column(name = "gov_id_type", length = 50)
    private String govIdType;

    @Column(name = "gov_id_number", length = 100)
    private String govIdNumber;

    @Column(name = "current_address", length = 255)
    private String currentAddress;

    @Column(name = "mother_tongue", length = 50)
    private String motherTongue;

    @Column(name = "relationship", length = 50)
    private String relationship;

    @Column(name = "batch", length = 50)
    private String batch;

    @Column(name = "expiry_date")
    private String expiryDate;

    @ElementCollection
    @CollectionTable(name = "driver_transmissions", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "transmission")
    private List<String> transmission;

    @Column(name = "expiry_date_kyc")
    private String expiryDateKyc;

    @Column(name = "blood_group", length = 10)
    private String bloodGroup;

    @Column(name = "qualification", length = 50)
    private String qualification;

    @Column(name = "batch_number", length = 50)
    private String batchNumber;

    @Column(name = "batch_expiry_date")
    private String batchExpiryDate;

    @Column(name = "father_name", length = 150)
    private String fatherName;

    @Column(name = "hill_station", nullable = false)
    private boolean hillStation;

    @Lob
    @Column(name = "profile_photo", columnDefinition = "LONGBLOB")
    private byte[] profilePhoto;

    @Column(name = "profile_photo_object", length = 255)
    private String profilePhotoObject;

    @Column(name = "profile_photo_content_type", length = 100)
    private String profilePhotoContentType;

    @Lob
    @Column(name = "license_front", columnDefinition = "LONGBLOB")
    private byte[] licenseFront;

    @Column(name = "license_front_object", length = 255)
    private String licenseFrontObject;

    @Column(name = "license_front_content_type", length = 100)
    private String licenseFrontContentType;

    @Lob
    @Column(name = "license_back", columnDefinition = "LONGBLOB")
    private byte[] licenseBack;

    @Column(name = "license_back_object", length = 255)
    private String licenseBackObject;

    @Column(name = "license_back_content_type", length = 100)
    private String licenseBackContentType;

    @Lob
    @Column(name = "gov_id_front", columnDefinition = "LONGBLOB")
    private byte[] govIdFront;

    @Column(name = "gov_id_front_object", length = 255)
    private String govIdFrontObject;

    @Column(name = "gov_id_front_content_type", length = 100)
    private String govIdFrontContentType;

    @Lob
    @Column(name = "gov_id_back", columnDefinition = "LONGBLOB")
    private byte[] govIdBack;

    @Column(name = "gov_id_back_object", length = 255)
    private String govIdBackObject;

    @Column(name = "gov_id_back_content_type", length = 100)
    private String govIdBackContentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private DriverProfileStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.status == null) {
            this.status = DriverProfileStatus.PENDING;
        }
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
