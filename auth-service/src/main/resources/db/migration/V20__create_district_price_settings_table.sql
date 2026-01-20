CREATE TABLE IF NOT EXISTS district_price_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    district_id BIGINT NOT NULL,
    model_type ENUM('LOCAL', 'OUTSTATION') NOT NULL,
    minimum_hours INT NOT NULL DEFAULT 1,
    base_fare DECIMAL(10,2) NOT NULL,
    extra_fare_per_hour DECIMAL(10,2) NOT NULL,
    night_charges DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    festive_charges DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    rain_charge DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    drop_charges_per_5km DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    drop_charges_per_100km DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    drop_limit_kms INT NOT NULL DEFAULT 0,
    driver_cancellation_penalty DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (district_id) REFERENCES districts(id) ON DELETE CASCADE,
    UNIQUE KEY unique_district_model (district_id, model_type)
);

-- Create indexes for better performance
CREATE INDEX idx_district_price_settings_district_id ON district_price_settings (district_id);
CREATE INDEX idx_district_price_settings_model_type ON district_price_settings (model_type);

-- Create fallback price settings table (without district reference)
CREATE TABLE IF NOT EXISTS fallback_price_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    model_type ENUM('LOCAL', 'OUTSTATION') NOT NULL,
    minimum_hours INT NOT NULL DEFAULT 1,
    base_fare DECIMAL(10,2) NOT NULL,
    extra_fare_per_hour DECIMAL(10,2) NOT NULL,
    night_charges DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    festive_charges DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    rain_charge DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    drop_charges_per_5km DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    drop_charges_per_100km DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    drop_limit_kms INT NOT NULL DEFAULT 0,
    driver_cancellation_penalty DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    UNIQUE KEY unique_fallback_model (model_type)
);

-- Insert default fallback price settings
INSERT INTO fallback_price_settings (model_type, minimum_hours, base_fare, extra_fare_per_hour, night_charges, festive_charges, rain_charge, drop_charges_per_5km, drop_charges_per_100km, drop_limit_kms, driver_cancellation_penalty)
VALUES
('LOCAL', 2, 100.00, 50.00, 25.00, 50.00, 20.00, 15.00, 100.00, 10, 50.00),
('OUTSTATION', 4, 200.00, 75.00, 50.00, 100.00, 30.00, 25.00, 150.00, 50, 100.00);
