-- Add transmission_type column to driver_vehicles table
ALTER TABLE driver_vehicles
    ADD COLUMN transmission_type VARCHAR(50) AFTER model;