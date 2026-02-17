-- Add driver_reached_at field to track when driver reaches customer location
ALTER TABLE driver_requests ADD COLUMN driver_reached_at TIMESTAMP;

-- Add index for better query performance on driver_reached_at
CREATE INDEX idx_driver_requests_driver_reached_at ON driver_requests(driver_reached_at);