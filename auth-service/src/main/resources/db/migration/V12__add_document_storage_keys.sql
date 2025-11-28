ALTER TABLE driver_profiles
    ADD COLUMN profile_photo_object VARCHAR(255) NULL AFTER profile_photo,
    ADD COLUMN license_front_object VARCHAR(255) NULL AFTER license_front,
    ADD COLUMN license_back_object VARCHAR(255) NULL AFTER license_back,
    ADD COLUMN gov_id_front_object VARCHAR(255) NULL AFTER gov_id_front,
    ADD COLUMN gov_id_back_object VARCHAR(255) NULL AFTER gov_id_back;

ALTER TABLE driver_vehicles
    MODIFY rc_image LONGBLOB NULL,
    MODIFY insurance_image LONGBLOB NULL,
    MODIFY pollution_certificate_image LONGBLOB NULL,
    ADD COLUMN rc_image_object VARCHAR(255) NULL AFTER rc_image,
    ADD COLUMN insurance_image_object VARCHAR(255) NULL AFTER insurance_image,
    ADD COLUMN pollution_certificate_image_object VARCHAR(255) NULL AFTER pollution_certificate_image;
