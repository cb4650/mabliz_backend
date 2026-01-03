ALTER TABLE vehicles
    ADD COLUMN vehicle_no VARCHAR(20) NULL,
    ADD COLUMN insurance_no VARCHAR(100) NULL,
    ADD COLUMN insurance_expiry DATE NULL,
    ADD COLUMN insurance_photo VARCHAR(255) NULL;
