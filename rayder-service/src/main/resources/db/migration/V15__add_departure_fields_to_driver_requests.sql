ALTER TABLE driver_requests
    ADD COLUMN departed_at TIMESTAMP NULL,
    ADD COLUMN departed_latitude DECIMAL(10, 6) NULL,
    ADD COLUMN departed_longitude DECIMAL(10, 6) NULL;
