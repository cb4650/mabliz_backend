CREATE TABLE IF NOT EXISTS driver_vehicles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    vehicle_number VARCHAR(25) NOT NULL,
    vehicle_type VARCHAR(20) NOT NULL,
    rc_number VARCHAR(50) NOT NULL,
    rc_image LONGBLOB NOT NULL,
    rc_image_content_type VARCHAR(100) NOT NULL,
    insurance_expiry_date DATE NOT NULL,
    insurance_image LONGBLOB NOT NULL,
    insurance_image_content_type VARCHAR(100) NOT NULL,
    pollution_certificate_image LONGBLOB NOT NULL,
    pollution_certificate_image_content_type VARCHAR(100) NOT NULL,
    brand VARCHAR(100) NOT NULL,
    model VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_driver_vehicles_user
        FOREIGN KEY (user_id) REFERENCES driver_profiles(user_id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX idx_driver_vehicles_vehicle_number
    ON driver_vehicles (vehicle_number);

CREATE INDEX idx_driver_vehicles_user_id
    ON driver_vehicles (user_id);
