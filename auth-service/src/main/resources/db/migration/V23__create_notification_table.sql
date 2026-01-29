CREATE TABLE IF NOT EXISTS notification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    time TIMESTAMP NOT NULL,
    app_id VARCHAR(10) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_notification_app_id CHECK (app_id IN ('rydd', 'rydc'))
);

CREATE INDEX idx_notification_app_id ON notification (app_id);
CREATE INDEX idx_notification_time ON notification (time);
