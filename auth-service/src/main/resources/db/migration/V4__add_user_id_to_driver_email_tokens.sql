ALTER TABLE driver_email_verification_tokens
    ADD COLUMN user_id BIGINT NULL;

ALTER TABLE driver_email_verification_tokens
    ADD CONSTRAINT fk_driver_email_verification_tokens_user
        FOREIGN KEY (user_id) REFERENCES user_profiles (user_id) ON DELETE CASCADE;

CREATE UNIQUE INDEX idx_driver_email_verification_tokens_user
    ON driver_email_verification_tokens (user_id);
