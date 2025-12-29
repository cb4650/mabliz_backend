ALTER TABLE driver_requests
    ADD COLUMN accepted_driver_id BIGINT NULL,
    ADD COLUMN accepted_at TIMESTAMP NULL,
    ADD INDEX idx_driver_requests_accepted_driver (accepted_driver_id);

CREATE TABLE driver_trip_responses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    driver_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    responded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_driver_trip_responses_booking_driver (booking_id, driver_id),
    INDEX idx_driver_trip_responses_booking (booking_id),
    INDEX idx_driver_trip_responses_driver (driver_id),
    CONSTRAINT fk_driver_trip_responses_booking FOREIGN KEY (booking_id) REFERENCES driver_requests (id) ON DELETE CASCADE
);
