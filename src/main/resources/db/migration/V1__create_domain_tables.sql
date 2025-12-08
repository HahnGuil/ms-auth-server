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

CREATE UNIQUE INDEX IF NOT EXISTS ux_users_username ON users(user_name);
CREATE UNIQUE INDEX IF NOT EXISTS ux_users_email ON users(user_email);
CREATE INDEX IF NOT EXISTS ix_users_role ON users(user_role);

-- Tabela application
CREATE TABLE IF NOT EXISTS application (
                                           id BIGSERIAL PRIMARY KEY,
                                           name_application VARCHAR(150) NOT NULL
    );

CREATE UNIQUE INDEX IF NOT EXISTS ux_application_name ON application(name_application);

-- Tabela token_log (LoginLog)
CREATE TABLE IF NOT EXISTS token_log (
                                         id_login_log UUID PRIMARY KEY,
                                         scope_token VARCHAR(50),
    date_login TIMESTAMP,
    active_token BOOLEAN,
    user_id UUID NOT NULL,
    CONSTRAINT fk_token_log_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
    );

CREATE INDEX IF NOT EXISTS ix_token_log_user_id ON token_log(user_id);

-- Tabela invalidated_token
CREATE TABLE IF NOT EXISTS invalidated_token (
                                                 id UUID PRIMARY KEY,
                                                 user_id UUID NOT NULL,
                                                 login_log_id UUID NOT NULL,
                                                 date_invalidate TIMESTAMP,
                                                 type_invalidation VARCHAR(50),
    CONSTRAINT fk_invalidated_token_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_invalidated_token_login_log FOREIGN KEY (login_log_id) REFERENCES token_log(id_login_log) ON DELETE CASCADE
    );

CREATE INDEX IF NOT EXISTS ix_invalidated_token_user_id ON invalidated_token(user_id);
CREATE INDEX IF NOT EXISTS ix_invalidated_token_login_log_id ON invalidated_token(login_log_id);

-- Tabela logged_npw (LoggedNow)
CREATE TABLE IF NOT EXISTS logged_npw (
                                          id UUID PRIMARY KEY,
                                          user_id UUID NOT NULL,
                                          login_log_id UUID NOT NULL,
                                          date_login TIMESTAMP,
                                          is_use_refresh BOOLEAN,
                                          date_refresh TIMESTAMP,
                                          CONSTRAINT fk_logged_npw_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_logged_npw_login_log FOREIGN KEY (login_log_id) REFERENCES token_log(id_login_log) ON DELETE CASCADE
    );

CREATE INDEX IF NOT EXISTS ix_logged_npw_user_id ON logged_npw(user_id);
CREATE INDEX IF NOT EXISTS ix_logged_npw_login_log_id ON logged_npw(login_log_id);

-- Tabela recover_token (ResetPassword)
CREATE TABLE IF NOT EXISTS recover_token (
                                             id BIGSERIAL PRIMARY KEY,
                                             recover_code VARCHAR(200) NOT NULL,
    user_email VARCHAR(255) NOT NULL,
    expiration_date TIMESTAMP
    );

CREATE UNIQUE INDEX IF NOT EXISTS ux_recover_token_code ON recover_token(recover_code);
CREATE INDEX IF NOT EXISTS ix_recover_token_email ON recover_token(user_email);

-- Tabela de relacionamento many-to-many user_application
CREATE TABLE IF NOT EXISTS user_application (
                                                user_id UUID NOT NULL,
                                                application_id BIGINT NOT NULL,
                                                PRIMARY KEY (user_id, application_id),
    CONSTRAINT fk_user_application_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_user_application_application FOREIGN KEY (application_id) REFERENCES application(id) ON DELETE CASCADE
    );

CREATE INDEX IF NOT EXISTS ix_user_application_user_id ON user_application(user_id);
CREATE INDEX IF NOT EXISTS ix_user_application_application_id ON user_application(application_id);
