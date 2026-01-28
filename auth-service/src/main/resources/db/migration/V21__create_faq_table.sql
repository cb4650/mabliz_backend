CREATE TABLE IF NOT EXISTS faq (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    question TEXT NOT NULL,
    answer TEXT NOT NULL,
    category VARCHAR(255) NOT NULL,
    sub_category VARCHAR(255),
    app_id VARCHAR(10) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_app_id CHECK (app_id IN ('rydd', 'rydc'))
);

CREATE INDEX idx_faq_app_id ON faq (app_id);
CREATE INDEX idx_faq_category ON faq (category);
CREATE INDEX idx_faq_sub_category ON faq (sub_category);
