CREATE TABLE IF NOT EXISTS driver_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    vehicle_id BIGINT NOT NULL,
    booking_type VARCHAR(50) NOT NULL,
    trip_option VARCHAR(50) NOT NULL,
    hours INT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    pickup_address VARCHAR(255) NOT NULL,
    pickup_latitude DECIMAL(10, 6) NOT NULL,
    pickup_longitude DECIMAL(10, 6) NOT NULL,
    drop_address VARCHAR(255) NOT NULL,
    drop_latitude DECIMAL(10, 6) NOT NULL,
    drop_longitude DECIMAL(10, 6) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_driver_requests_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles (id) ON DELETE RESTRICT
);

CREATE INDEX idx_driver_requests_user_id ON driver_requests (user_id);
CREATE INDEX idx_driver_requests_vehicle_id ON driver_requests (vehicle_id);
