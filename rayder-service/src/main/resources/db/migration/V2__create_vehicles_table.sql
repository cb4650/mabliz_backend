CREATE TABLE IF NOT EXISTS vehicles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    brand VARCHAR(100) NOT NULL,
    model VARCHAR(100) NOT NULL,
    ownership_type VARCHAR(30) NOT NULL,
    transmission VARCHAR(30) NOT NULL,
    fuel_type VARCHAR(20) NOT NULL,
    year VARCHAR(4) NOT NULL,
    policy_no VARCHAR(100) NOT NULL,
    start_date DATE NOT NULL,
    expiry_date DATE NOT NULL
);

CREATE INDEX idx_vehicles_user_id ON vehicles (user_id);
