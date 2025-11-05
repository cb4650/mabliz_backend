CREATE TABLE IF NOT EXISTS driver_profiles (
    user_id BIGINT PRIMARY KEY,
    full_name VARCHAR(150) NOT NULL,
    dob DATE NULL,
    gender VARCHAR(20) NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    phone VARCHAR(25) NULL,
    emergency_contact_name VARCHAR(150) NULL,
    emergency_contact_number VARCHAR(25) NULL,
    permanent_address VARCHAR(255) NULL,
    languages VARCHAR(255) NULL,
    license_number VARCHAR(50) NULL,
    license_type VARCHAR(50) NULL,
    experience_years INT NULL,
    gov_id_type VARCHAR(50) NULL,
    gov_id_number VARCHAR(100) NULL,
    profile_photo LONGBLOB NULL,
    profile_photo_content_type VARCHAR(100) NULL,
    license_front LONGBLOB NULL,
    license_front_content_type VARCHAR(100) NULL,
    license_back LONGBLOB NULL,
    license_back_content_type VARCHAR(100) NULL,
    gov_id_front LONGBLOB NULL,
    gov_id_front_content_type VARCHAR(100) NULL,
    gov_id_back LONGBLOB NULL,
    gov_id_back_content_type VARCHAR(100) NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_driver_profiles_user FOREIGN KEY (user_id) REFERENCES user_profiles (user_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS driver_email_verification_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(150) NOT NULL,
    name VARCHAR(150) NOT NULL,
    otp_code VARCHAR(12) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    verified_at TIMESTAMP NULL
);

CREATE UNIQUE INDEX idx_driver_email_verification_tokens_email
    ON driver_email_verification_tokens (email);
