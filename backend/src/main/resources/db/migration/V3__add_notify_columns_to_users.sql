ALTER TABLE users
    ADD COLUMN notify_sign_request TINYINT(1) NOT NULL DEFAULT 1,
    ADD COLUMN notify_sign_done    TINYINT(1) NOT NULL DEFAULT 1,
    ADD COLUMN notify_weekly       TINYINT(1) NOT NULL DEFAULT 0,
    ADD COLUMN notify_marketing    TINYINT(1) NOT NULL DEFAULT 0;
