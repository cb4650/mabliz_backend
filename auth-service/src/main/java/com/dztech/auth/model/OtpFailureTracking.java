package com.dztech.auth.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "otp_failure_tracking", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"phone", "role_type"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpFailureTracking {

    public enum RoleType {
        USER, DRIVER, ADMIN
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "phone", nullable = false, length = 25)
    private String phone;

    @Column(name = "role_type", nullable = false, length = 20)
    private RoleType roleType;

    @Column(name = "failure_count", nullable = false)
    private int failureCount;

    @Column(name = "last_failure_at", nullable = false)
    private Instant lastFailureAt;

    @Column(name = "blocked_until")
    private Instant blockedUntil;

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

    public boolean isBlocked() {
        return blockedUntil != null && blockedUntil.isAfter(Instant.now());
    }

    public boolean isBlocked(Instant now) {
        return blockedUntil != null && blockedUntil.isAfter(now);
    }
}
