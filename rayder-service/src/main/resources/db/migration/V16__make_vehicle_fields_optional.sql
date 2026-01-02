-- Make fuel_type, year, policy_no, start_date, expiry_date columns nullable in vehicles table
ALTER TABLE vehicles MODIFY COLUMN fuel_type VARCHAR(20);
ALTER TABLE vehicles MODIFY COLUMN year VARCHAR(4);
ALTER TABLE vehicles MODIFY COLUMN policy_no VARCHAR(100);
ALTER TABLE vehicles MODIFY COLUMN start_date DATE;
ALTER TABLE vehicles MODIFY COLUMN expiry_date DATE;
