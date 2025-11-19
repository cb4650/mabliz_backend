ALTER TABLE driver_profiles
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'PENDING';

UPDATE driver_profiles
SET status = 'PENDING'
WHERE status IS NULL;

CREATE TABLE driver_field_verifications (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    driver_id BIGINT NOT NULL,
    field_name VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL,
    notes VARCHAR(255),
    verified_by_admin_id BIGINT NULL,
    verified_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_driver_field_verifications_driver
        FOREIGN KEY (driver_id) REFERENCES driver_profiles (user_id),
    CONSTRAINT fk_driver_field_verifications_admin
        FOREIGN KEY (verified_by_admin_id) REFERENCES admins (id),
    CONSTRAINT uk_driver_field_verifications_driver_field
        UNIQUE (driver_id, field_name)
);
