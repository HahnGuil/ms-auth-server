-- Password, expiration and block status per (user, application). users.user_email stays UNIQUE:
-- one identity per email, linked to several applications through user_application.
-- users.password / users.password_create_date / users.block_user become legacy columns.
CREATE TABLE IF NOT EXISTS auth_service.password_parameters (
                                                             id UUID PRIMARY KEY,
                                                             user_id UUID NOT NULL,
                                                             application_id BIGINT NOT NULL,
                                                             password VARCHAR(255) NOT NULL,
                                                             password_create_date TIMESTAMP NOT NULL,
                                                             block_user BOOLEAN NOT NULL DEFAULT FALSE,
                                                             CONSTRAINT uk_password_parameters_user_application UNIQUE (user_id, application_id),
                                                             CONSTRAINT fk_password_parameters_user
                                                                 FOREIGN KEY (user_id) REFERENCES auth_service.users(user_id) ON DELETE CASCADE,
                                                             CONSTRAINT fk_password_parameters_application
                                                                 FOREIGN KEY (application_id) REFERENCES auth_service.application(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS index_password_parameters_user_id ON auth_service.password_parameters(user_id);
CREATE INDEX IF NOT EXISTS index_password_parameters_application_id ON auth_service.password_parameters(application_id);
CREATE INDEX IF NOT EXISTS index_password_parameters_create_date ON auth_service.password_parameters(password_create_date);

-- Backfill existing passwords for every application the user is already registered in
INSERT INTO auth_service.password_parameters (id, user_id, application_id, password, password_create_date, block_user)
SELECT gen_random_uuid(), u.user_id, ua.application_id, u.password, COALESCE(u.password_create_date, NOW()), COALESCE(u.block_user, FALSE)
FROM auth_service.users u
         JOIN auth_service.user_application ua ON ua.user_id = u.user_id
WHERE u.password IS NOT NULL AND u.password <> ''
ON CONFLICT (user_id, application_id) DO NOTHING;

-- Recovery codes are now requested per application
-- Resend API key of each application, used (with application_email as sender) to send its emails
ALTER TABLE auth_service.application ADD COLUMN IF NOT EXISTS resend_api_key VARCHAR(255);

ALTER TABLE auth_service.reset_password ADD COLUMN IF NOT EXISTS application_id BIGINT;
CREATE INDEX IF NOT EXISTS index_reset_password_application_id ON auth_service.reset_password(application_id);

-- Password policy per application
CREATE TABLE IF NOT EXISTS auth_service.password_policy (
                                                         id UUID PRIMARY KEY,
                                                         application_id BIGINT NOT NULL UNIQUE,
                                                         expiration_enabled BOOLEAN NOT NULL,
                                                         default_policy BOOLEAN NOT NULL,
                                                         expiration_months INTEGER,
                                                         CONSTRAINT fk_password_policy_application
                                                             FOREIGN KEY (application_id) REFERENCES auth_service.application(id) ON DELETE CASCADE,
                                                         CONSTRAINT ck_password_policy_months
                                                             CHECK (expiration_enabled = FALSE OR default_policy = TRUE OR expiration_months >= 1)
);

-- Existing applications keep the previous behavior (default policy: password expires in 90 days)
INSERT INTO auth_service.password_policy (id, application_id, expiration_enabled, default_policy, expiration_months)
SELECT gen_random_uuid(), a.id, TRUE, TRUE, NULL
FROM auth_service.application a
ON CONFLICT (application_id) DO NOTHING;
