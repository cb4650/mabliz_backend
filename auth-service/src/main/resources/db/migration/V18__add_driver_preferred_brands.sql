-- Store preferred brand ids per driver profile

CREATE TABLE driver_preferred_brands (
    user_id BIGINT NOT NULL,
    brand_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, brand_id),
    FOREIGN KEY (user_id) REFERENCES driver_profiles(user_id) ON DELETE CASCADE
);
