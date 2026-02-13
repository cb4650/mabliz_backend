CREATE TABLE IF NOT EXISTS help_category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    category_key VARCHAR(50) NOT NULL UNIQUE,
    app_id VARCHAR(10) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_app_id CHECK (app_id IN ('rydd', 'rydc')),
    CONSTRAINT chk_category_key CHECK (category_key IN ('trip', 'driver', 'payment'))
);

CREATE TABLE IF NOT EXISTS help_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    email VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES help_category(id) ON DELETE CASCADE
);

CREATE INDEX idx_help_category_app_id ON help_category (app_id);
CREATE INDEX idx_help_category_key ON help_category (category_key);
CREATE INDEX idx_help_item_category_id ON help_item (category_id);