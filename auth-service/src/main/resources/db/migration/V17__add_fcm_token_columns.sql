-- =========================
-- admins
-- =========================
SET @col_exists := (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'admins'
    AND COLUMN_NAME = 'fcm_token'
);

SET @sql := IF(
  @col_exists = 0,
  'ALTER TABLE admins ADD COLUMN fcm_token VARCHAR(512) NULL AFTER phone',
  'SELECT 1'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- =========================
-- user_profiles
-- =========================
SET @col_exists := (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'user_profiles'
    AND COLUMN_NAME = 'fcm_token'
);

SET @sql := IF(
  @col_exists = 0,
  'ALTER TABLE user_profiles ADD COLUMN fcm_token VARCHAR(512) NULL AFTER phone',
  'SELECT 1'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- =========================
-- driver_profiles
-- =========================
SET @col_exists := (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'driver_profiles'
    AND COLUMN_NAME = 'fcm_token'
);

SET @sql := IF(
  @col_exists = 0,
  'ALTER TABLE driver_profiles ADD COLUMN fcm_token VARCHAR(512) NULL AFTER phone',
  'SELECT 1'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
