-- sql
-- Database migration script V1: Creates domain tables and indexes for the authentication system

-- Creating the users table
-- Stores user account information including credentials, profile data and access control
CREATE TABLE IF NOT EXISTS users (
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
                                     user_role VARCHAR(50)
);

-- Remove unique constraint on user_name to allow duplicate usernames.
-- Keep a non-unique index for lookup performance.
CREATE INDEX IF NOT EXISTS ix_users_username ON users(user_name);
CREATE UNIQUE INDEX IF NOT EXISTS ux_users_email ON users(user_email);
CREATE INDEX IF NOT EXISTS ix_users_role ON users(user_role);

-- Creating the application table
-- Stores application information for multi-application support
CREATE TABLE IF NOT EXISTS application (
                                           id BIGSERIAL PRIMARY KEY,
                                           name_application VARCHAR(150) NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_application_name ON application(name_application);

-- Creating the token_log table
-- Records token generation history for audit and security purposes
CREATE TABLE IF NOT EXISTS token_log (
                                         id_token_log UUID PRIMARY KEY,
                                         scope_token VARCHAR(50),
                                         create_date TIMESTAMP,
                                         active_token BOOLEAN,
                                         user_id UUID NOT NULL,
                                         CONSTRAINT fk_token_log_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS ix_token_log_user_id ON token_log(user_id);

-- Creating the invalidated_token table
-- Tracks tokens that have been explicitly invalidated before expiration
CREATE TABLE IF NOT EXISTS invalidated_token (
                                                 id UUID PRIMARY KEY,
                                                 user_id UUID NOT NULL,
                                                 login_log_id UUID NOT NULL,
                                                 date_invalidate TIMESTAMP,
                                                 type_invalidation VARCHAR(50),
                                                 CONSTRAINT fk_invalidated_token_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                                                 CONSTRAINT fk_invalidated_token_token_log FOREIGN KEY (login_log_id) REFERENCES token_log(id_token_log) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS ix_invalidated_token_user_id ON invalidated_token(user_id);
CREATE INDEX IF NOT EXISTS ix_invalidated_token_login_log_id ON invalidated_token(login_log_id);

-- Creating the logged_now table
-- Maintains current active sessions and refresh token usage
CREATE TABLE IF NOT EXISTS logged_now (
                                          id UUID PRIMARY KEY,
                                          user_id UUID NOT NULL,
                                          token_log_id UUID NOT NULL,
                                          date_login TIMESTAMP,
                                          is_use_refresh BOOLEAN,
                                          date_refresh TIMESTAMP,
                                          CONSTRAINT fk_logged_now_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                                          CONSTRAINT fk_logged_now_token_log FOREIGN KEY (token_log_id) REFERENCES token_log(id_token_log) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS ix_logged_now_user_id ON logged_now(user_id);
CREATE INDEX IF NOT EXISTS ix_logged_now_token_log_id ON logged_now(token_log_id);

-- Creating the reset_password table
-- Stores password recovery codes with expiration tracking
CREATE TABLE IF NOT EXISTS reset_password (
                                              id BIGSERIAL PRIMARY KEY,
                                              recover_code VARCHAR(200) NOT NULL,
                                              user_email VARCHAR(255) NOT NULL,
                                              expiration_date TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_reset_password_code ON reset_password(recover_code);
CREATE INDEX IF NOT EXISTS ix_reset_password_email ON reset_password(user_email);

-- Creating the user_application junction table
-- Manages many-to-many relationship between users and applications
CREATE TABLE IF NOT EXISTS user_application (
                                                user_id UUID NOT NULL,
                                                application_id BIGINT NOT NULL,
                                                PRIMARY KEY (user_id, application_id),
                                                CONSTRAINT fk_user_application_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                                                CONSTRAINT fk_user_application_application FOREIGN KEY (application_id) REFERENCES application(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS ix_user_application_user_id ON user_application(user_id);
CREATE INDEX IF NOT EXISTS ix_user_application_application_id ON user_application(application_id);
