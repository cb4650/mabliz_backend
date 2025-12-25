-- Add hill station flag to driver profiles

ALTER TABLE driver_profiles ADD COLUMN hill_station BOOLEAN NOT NULL DEFAULT FALSE;
