CREATE TABLE IF NOT EXISTS otp_failure_tracking (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    phone VARCHAR(25) NOT NULL,
    role_type VARCHAR(20) NOT NULL COMMENT 'USER, DRIVER, ADMIN',
    failure_count INT NOT NULL DEFAULT 0,
    last_failure_at TIMESTAMP NOT NULL,
    blocked_until TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_otp_failure_phone_role (phone, role_type),
    INDEX idx_otp_failure_blocked_until (blocked_until),
    INDEX idx_otp_failure_last_failure (last_failure_at)
);
