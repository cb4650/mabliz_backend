-- Make ownership_type column nullable to align with business requirements
-- Only brandId, modelId, and transmission should be required

ALTER TABLE vehicles MODIFY ownership_type VARCHAR(30) NULL;