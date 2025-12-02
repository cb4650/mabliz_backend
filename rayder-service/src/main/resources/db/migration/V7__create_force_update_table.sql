CREATE TABLE IF NOT EXISTS force_update (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    platform ENUM('ANDROID','IOS') NOT NULL UNIQUE,
    version VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Insert default versions for Android and iOS platforms
INSERT INTO force_update (platform, version) VALUES ('ANDROID', '1.0.0');
INSERT INTO force_update (platform, version) VALUES ('IOS', '1.0.0');
