CREATE TABLE IF NOT EXISTS price_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    class_name VARCHAR(100) NOT NULL UNIQUE,
    base_fare DECIMAL(10,2) NOT NULL,
    per_hour DECIMAL(10,2) NOT NULL,
    late_night_charges DECIMAL(10,2) NOT NULL,
    extra_hour_charges DECIMAL(10,2) NOT NULL,
    food_charges DECIMAL(10,2) NOT NULL,
    festival_charges DECIMAL(10,2) NOT NULL,
    platform_commission DECIMAL(5,2) NOT NULL,
    festival_commission DECIMAL(5,2) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE UNIQUE INDEX idx_price_settings_class_name
    ON price_settings (class_name);

-- Insert default data
INSERT INTO price_settings (class_name, base_fare, per_hour, late_night_charges, extra_hour_charges, food_charges, festival_charges, platform_commission, festival_commission, created_at, updated_at)
VALUES
('Economy', 50.00, 200.00, 100.00, 50.00, 25.00, 75.00, 15.00, 5.00, NOW(), NOW()),
('Business', 100.00, 350.00, 150.00, 75.00, 50.00, 100.00, 20.00, 7.00, NOW(), NOW());
