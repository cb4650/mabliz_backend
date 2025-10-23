ALTER TABLE vehicles
    ADD COLUMN brand_id BIGINT NULL,
    ADD COLUMN model_id BIGINT NULL;

UPDATE vehicles v
SET brand_id = (
    SELECT b.id
    FROM car_brands b
    WHERE b.name = v.brand
    LIMIT 1
)
WHERE v.brand_id IS NULL;

UPDATE vehicles v
SET model_id = (
    SELECT m.id
    FROM car_models m
    WHERE m.name = v.model
    LIMIT 1
)
WHERE v.model_id IS NULL;

ALTER TABLE vehicles
    MODIFY COLUMN brand_id BIGINT NOT NULL,
    MODIFY COLUMN model_id BIGINT NOT NULL;

ALTER TABLE vehicles
    ADD CONSTRAINT fk_vehicles_brand FOREIGN KEY (brand_id) REFERENCES car_brands (id) ON DELETE RESTRICT,
    ADD CONSTRAINT fk_vehicles_model FOREIGN KEY (model_id) REFERENCES car_models (id) ON DELETE RESTRICT;

CREATE INDEX idx_vehicles_brand_id ON vehicles (brand_id);
CREATE INDEX idx_vehicles_model_id ON vehicles (model_id);

ALTER TABLE vehicles
    DROP COLUMN brand,
    DROP COLUMN model;
