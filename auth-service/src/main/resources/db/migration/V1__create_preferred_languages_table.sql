CREATE TABLE IF NOT EXISTS preferred_languages (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(20) NOT NULL,
    name VARCHAR(100) NOT NULL,
    CONSTRAINT uk_preferred_languages_code UNIQUE (code),
    CONSTRAINT uk_preferred_languages_name UNIQUE (name)
);

INSERT INTO preferred_languages (code, name)
VALUES
    ('en', 'English'),
    ('as', 'Assamese'),
    ('bn', 'Bengali'),
    ('brx', 'Bodo'),
    ('doi', 'Dogri'),
    ('gu', 'Gujarati'),
    ('hi', 'Hindi'),
    ('kn', 'Kannada'),
    ('ks', 'Kashmiri'),
    ('kok', 'Konkani'),
    ('mai', 'Maithili'),
    ('ml', 'Malayalam'),
    ('mni', 'Manipuri (Meitei)'),
    ('mr', 'Marathi'),
    ('ne', 'Nepali'),
    ('or', 'Odia'),
    ('pa', 'Punjabi'),
    ('sa', 'Sanskrit'),
    ('sat', 'Santali'),
    ('sd', 'Sindhi'),
    ('ta', 'Tamil'),
    ('te', 'Telugu'),
    ('ur', 'Urdu')
ON DUPLICATE KEY UPDATE name = VALUES(name);

CREATE TABLE IF NOT EXISTS user_profiles (
    user_id BIGINT NOT NULL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    phone VARCHAR(25) NOT NULL,
    email VARCHAR(150) NOT NULL,
    address VARCHAR(255) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

ALTER TABLE user_profiles
    ADD COLUMN primary_preferred_language_id BIGINT NULL AFTER address,
    ADD COLUMN secondary_preferred_language_id BIGINT NULL AFTER primary_preferred_language_id,
    ADD CONSTRAINT fk_user_profiles_primary_language FOREIGN KEY (primary_preferred_language_id) REFERENCES preferred_languages (id),
    ADD CONSTRAINT fk_user_profiles_secondary_language FOREIGN KEY (secondary_preferred_language_id) REFERENCES preferred_languages (id);

SET @has_primary_column := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'user_profiles'
      AND COLUMN_NAME = 'primary_preferred_language'
);

SET @has_secondary_column := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'user_profiles'
      AND COLUMN_NAME = 'secondary_preferred_language'
);

SET @migrate_primary_sql := IF(
    @has_primary_column > 0,
    'UPDATE user_profiles up\n     LEFT JOIN preferred_languages pl ON LOWER(TRIM(up.primary_preferred_language)) = LOWER(pl.name)\n     SET up.primary_preferred_language_id = pl.id\n     WHERE pl.id IS NOT NULL',
    'SELECT 1'
);

PREPARE migrate_primary_stmt FROM @migrate_primary_sql;
EXECUTE migrate_primary_stmt;
DEALLOCATE PREPARE migrate_primary_stmt;

SET @migrate_secondary_sql := IF(
    @has_secondary_column > 0,
    'UPDATE user_profiles up\n     LEFT JOIN preferred_languages pl ON LOWER(TRIM(up.secondary_preferred_language)) = LOWER(pl.name)\n     SET up.secondary_preferred_language_id = pl.id\n     WHERE pl.id IS NOT NULL',
    'SELECT 1'
);

PREPARE migrate_secondary_stmt FROM @migrate_secondary_sql;
EXECUTE migrate_secondary_stmt;
DEALLOCATE PREPARE migrate_secondary_stmt;

SET @drop_primary_sql := IF(
    @has_primary_column > 0,
    'ALTER TABLE user_profiles DROP COLUMN primary_preferred_language',
    'SELECT 1'
);

PREPARE drop_primary_stmt FROM @drop_primary_sql;
EXECUTE drop_primary_stmt;
DEALLOCATE PREPARE drop_primary_stmt;

SET @drop_secondary_sql := IF(
    @has_secondary_column > 0,
    'ALTER TABLE user_profiles DROP COLUMN secondary_preferred_language',
    'SELECT 1'
);

PREPARE drop_secondary_stmt FROM @drop_secondary_sql;
EXECUTE drop_secondary_stmt;
DEALLOCATE PREPARE drop_secondary_stmt;
