-- Remove rc_number column and add manufactured_year column to driver_vehicles table
ALTER TABLE driver_vehicles
    DROP COLUMN rc_number,
    ADD COLUMN manufactured_year VARCHAR(7) NOT NULL AFTER vehicle_type;
