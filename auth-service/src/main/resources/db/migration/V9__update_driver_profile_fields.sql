-- Update driver profile fields to match new requirements

-- Change dob to VARCHAR
ALTER TABLE driver_profiles MODIFY dob VARCHAR(10) NULL;

-- Change experience_years to experience VARCHAR
ALTER TABLE driver_profiles CHANGE experience_years experience VARCHAR(10) NULL;

-- Drop old languages column
ALTER TABLE driver_profiles DROP COLUMN languages;

-- Drop old license_type column
ALTER TABLE driver_profiles DROP COLUMN license_type;

-- Add new columns
ALTER TABLE driver_profiles ADD COLUMN current_address VARCHAR(255) NULL;
ALTER TABLE driver_profiles ADD COLUMN mother_tongue VARCHAR(50) NULL;
ALTER TABLE driver_profiles ADD COLUMN relationship VARCHAR(50) NULL;
ALTER TABLE driver_profiles ADD COLUMN batch VARCHAR(50) NULL;
ALTER TABLE driver_profiles ADD COLUMN expiry_date VARCHAR(10) NULL;
ALTER TABLE driver_profiles ADD COLUMN expiry_date_kyc VARCHAR(10) NULL;
ALTER TABLE driver_profiles ADD COLUMN blood_group VARCHAR(10) NULL;
ALTER TABLE driver_profiles ADD COLUMN qualification VARCHAR(50) NULL;

-- Create table for languages (ElementCollection)
CREATE TABLE driver_languages (
    user_id BIGINT NOT NULL,
    language VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, language),
    FOREIGN KEY (user_id) REFERENCES driver_profiles(user_id) ON DELETE CASCADE
);

-- Create table for license types (ElementCollection)
CREATE TABLE driver_license_types (
    user_id BIGINT NOT NULL,
    license_type VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, license_type),
    FOREIGN KEY (user_id) REFERENCES driver_profiles(user_id) ON DELETE CASCADE
);

-- Create table for transmissions (ElementCollection)
CREATE TABLE driver_transmissions (
    user_id BIGINT NOT NULL,
    transmission VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, transmission),
    FOREIGN KEY (user_id) REFERENCES driver_profiles(user_id) ON DELETE CASCADE
);
