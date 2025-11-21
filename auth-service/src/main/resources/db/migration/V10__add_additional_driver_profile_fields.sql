-- Add additional fields to driver profile

ALTER TABLE driver_profiles ADD COLUMN batch_number VARCHAR(50) NULL;
ALTER TABLE driver_profiles ADD COLUMN batch_expiry_date VARCHAR(10) NULL;
ALTER TABLE driver_profiles ADD COLUMN father_name VARCHAR(150) NULL;
