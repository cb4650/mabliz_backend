ALTER TABLE driver_profiles
    MODIFY profile_photo LONGBLOB NULL,
    MODIFY license_front LONGBLOB NULL,
    MODIFY license_back LONGBLOB NULL,
    MODIFY gov_id_front LONGBLOB NULL,
    MODIFY gov_id_back LONGBLOB NULL;
