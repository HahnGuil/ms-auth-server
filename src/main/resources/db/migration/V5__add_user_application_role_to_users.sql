ALTER TABLE users
    ADD COLUMN IF NOT EXISTS user_application_role VARCHAR(100);

CREATE INDEX IF NOT EXISTS ix_users_user_application_role
    ON users(user_application_role);