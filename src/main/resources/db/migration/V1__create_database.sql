CREATE SCHEMA IF NOT EXISTS toxic_bet;
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Creating the users table
CREATE TABLE IF NOT EXISTS toxic_bet.users (
                                               user_id UUID PRIMARY KEY,
                                               user_name VARCHAR(150) NOT NULL,
                                               password VARCHAR(255) NOT NULL,
                                               password_create_date TIMESTAMP,
                                               user_email VARCHAR(255) NOT NULL,
                                               first_name VARCHAR(100),
                                               last_name VARCHAR(100),
                                               picture_url TEXT,
                                               block_user BOOLEAN,
                                               type_user VARCHAR(50),
                                               user_role VARCHAR(50),
                                               user_application_role VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS ix_users_username ON toxic_bet.users(user_name);
CREATE UNIQUE INDEX IF NOT EXISTS ux_users_email ON toxic_bet.users(user_email);
CREATE INDEX IF NOT EXISTS ix_users_role ON toxic_bet.users(user_role);
CREATE INDEX IF NOT EXISTS ix_users_user_application_role ON toxic_bet.users(user_application_role);

-- Creating the application table
CREATE TABLE IF NOT EXISTS toxic_bet.application (
                                                     id BIGSERIAL PRIMARY KEY,
                                                     name_application VARCHAR(150) NOT NULL,
                                                     public_id UUID NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_application_name ON toxic_bet.application(name_application);
CREATE INDEX IF NOT EXISTS ix_application_public_id ON toxic_bet.application(public_id);

-- Seed applications
INSERT INTO toxic_bet.application (name_application, public_id)
VALUES ('toxic-bet', gen_random_uuid())
ON CONFLICT (name_application) DO NOTHING;

INSERT INTO toxic_bet.application (name_application, public_id)
VALUES ('pata-amiga', gen_random_uuid())
ON CONFLICT (name_application) DO NOTHING;

INSERT INTO toxic_bet.application (name_application, public_id)
VALUES ('smbuilder', gen_random_uuid())
ON CONFLICT (name_application) DO NOTHING;

UPDATE toxic_bet.application
SET public_id = gen_random_uuid()
WHERE public_id IS NULL;

-- Creating the token_log table
CREATE TABLE IF NOT EXISTS toxic_bet.token_log (
                                                   id_token_log UUID PRIMARY KEY,
                                                   scope_token VARCHAR(50),
                                                   create_date TIMESTAMP,
                                                   active_token BOOLEAN,
                                                   user_id UUID,
                                                   application_id BIGINT,
                                                   CONSTRAINT fk_token_log_user
                                                       FOREIGN KEY (user_id) REFERENCES toxic_bet.users(user_id) ON DELETE CASCADE,
                                                   CONSTRAINT fk_token_log_application
                                                       FOREIGN KEY (application_id) REFERENCES toxic_bet.application(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS ix_token_log_user_id ON toxic_bet.token_log(user_id);
CREATE INDEX IF NOT EXISTS ix_token_log_application_id ON toxic_bet.token_log(application_id);

-- Creating the invalidated_token table
CREATE TABLE IF NOT EXISTS toxic_bet.invalidated_token (
                                                           id UUID PRIMARY KEY,
                                                           user_id UUID,
                                                           application_public_id UUID,
                                                           login_log_id UUID NOT NULL,
                                                           date_invalidate TIMESTAMP,
                                                           type_invalidation VARCHAR(50),
                                                           CONSTRAINT fk_invalidated_token_user
                                                               FOREIGN KEY (user_id) REFERENCES toxic_bet.users(user_id) ON DELETE CASCADE,
                                                           CONSTRAINT fk_invalidated_token_token_log
                                                               FOREIGN KEY (login_log_id) REFERENCES toxic_bet.token_log(id_token_log) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS ix_invalidated_token_user_id ON toxic_bet.invalidated_token(user_id);
CREATE INDEX IF NOT EXISTS ix_invalidated_token_application_public_id ON toxic_bet.invalidated_token(application_public_id);
CREATE INDEX IF NOT EXISTS ix_invalidated_token_login_log_id ON toxic_bet.invalidated_token(login_log_id);

-- Creating the logged_now table
CREATE TABLE IF NOT EXISTS toxic_bet.logged_now (
                                                    id UUID PRIMARY KEY,
                                                    user_id UUID,
                                                    application_public_id UUID,
                                                    token_log_id UUID NOT NULL,
                                                    date_login TIMESTAMP,
                                                    is_use_refresh BOOLEAN,
                                                    date_refresh TIMESTAMP,
                                                    CONSTRAINT fk_logged_now_user
                                                        FOREIGN KEY (user_id) REFERENCES toxic_bet.users(user_id) ON DELETE CASCADE,
                                                    CONSTRAINT fk_logged_now_token_log
                                                        FOREIGN KEY (token_log_id) REFERENCES toxic_bet.token_log(id_token_log) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS ix_logged_now_user_id ON toxic_bet.logged_now(user_id);
CREATE INDEX IF NOT EXISTS ix_logged_now_application_public_id ON toxic_bet.logged_now(application_public_id);
CREATE INDEX IF NOT EXISTS ix_logged_now_token_log_id ON toxic_bet.logged_now(token_log_id);

-- Creating the reset_password table
CREATE TABLE IF NOT EXISTS toxic_bet.reset_password (
                                                        id BIGSERIAL PRIMARY KEY,
                                                        recover_code VARCHAR(200) NOT NULL,
                                                        user_email VARCHAR(255) NOT NULL,
                                                        expiration_date TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_reset_password_code ON toxic_bet.reset_password(recover_code);
CREATE INDEX IF NOT EXISTS ix_reset_password_email ON toxic_bet.reset_password(user_email);

-- Creating the user_application junction table
CREATE TABLE IF NOT EXISTS toxic_bet.user_application (
                                                          user_id UUID NOT NULL,
                                                          application_id BIGINT NOT NULL,
                                                          PRIMARY KEY (user_id, application_id),
                                                          CONSTRAINT fk_user_application_user
                                                              FOREIGN KEY (user_id) REFERENCES toxic_bet.users(user_id) ON DELETE CASCADE,
                                                          CONSTRAINT fk_user_application_application
                                                              FOREIGN KEY (application_id) REFERENCES toxic_bet.application(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS ix_user_application_user_id ON toxic_bet.user_application(user_id);
CREATE INDEX IF NOT EXISTS ix_user_application_application_id ON toxic_bet.user_application(application_id);