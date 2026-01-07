ALTER TABLE driver_requests
ADD COLUMN trip_closed_at TIMESTAMP,
ADD COLUMN trip_closed_latitude DECIMAL(10,6),
ADD COLUMN trip_closed_longitude DECIMAL(10,6);
