CREATE TABLE IF NOT EXISTS force_update (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    app_id VARCHAR(10) NOT NULL,
    platform ENUM('ANDROID','IOS') NOT NULL,
    version VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_app_id_platform (app_id, platform)
);

-- Insert default versions for all appIds (rydd, rydc, admn) with both platforms
INSERT INTO force_update (app_id, platform, version) VALUES ('rydd', 'ANDROID', '1.0.0');
INSERT INTO force_update (app_id, platform, version) VALUES ('rydd', 'IOS', '1.0.0');
INSERT INTO force_update (app_id, platform, version) VALUES ('rydc', 'ANDROID', '1.0.0');
INSERT INTO force_update (app_id, platform, version) VALUES ('rydc', 'IOS', '1.0.0');
INSERT INTO force_update (app_id, platform, version) VALUES ('admn', 'ANDROID', '1.0.0');
INSERT INTO force_update (app_id, platform, version) VALUES ('admn', 'IOS', '1.0.0');
