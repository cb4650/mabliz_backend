-- Add app_id column to force_update table
ALTER TABLE force_update ADD COLUMN app_id VARCHAR(10) NOT NULL DEFAULT 'DEFAULT';

-- Update existing records with a default app_id
UPDATE force_update SET app_id = 'DEFAULT' WHERE app_id IS NULL;

-- Drop the old unique constraint on platform only
ALTER TABLE force_update DROP KEY platform;

-- Add the new unique constraint on app_id and platform
ALTER TABLE force_update ADD CONSTRAINT uk_force_update_app_id_platform UNIQUE (app_id, platform);
