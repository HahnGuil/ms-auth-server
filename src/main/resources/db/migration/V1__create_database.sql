CREATE SCHEMA IF NOT EXISTS auth_service;
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Creating the user's table
CREATE TABLE IF NOT EXISTS auth_service.users (
                                               user_id UUID PRIMARY KEY,
                                               user_name VARCHAR(150) NOT NULL UNIQUE,
                                               password VARCHAR(255) NOT NULL,
                                               password_create_date TIMESTAMP,
                                               user_email VARCHAR(255) NOT NULL UNIQUE,
                                               first_name VARCHAR(100),
                                               last_name VARCHAR(100),
                                               picture_url TEXT,
                                               block_user BOOLEAN,
                                               type_user VARCHAR(50),
                                               user_role VARCHAR(50),
                                               user_application_role VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS index_users_role ON auth_service.users(user_role);
CREATE INDEX IF NOT EXISTS index_users_user_application_role ON auth_service.users(user_application_role);

-- Creating the application table
CREATE TABLE IF NOT EXISTS auth_service.application (
                                                     id BIGSERIAL PRIMARY KEY,
                                                     name_application VARCHAR(150) NOT NULL UNIQUE,
                                                    application_domain VARCHAR(150) NOT NULL UNIQUE,
                                                     application_email VARCHAR(255) NOT NULL UNIQUE,
                                                     public_id UUID NOT NULL
);

CREATE INDEX IF NOT EXISTS index_application_public_id ON auth_service.application(public_id);
CREATE INDEX IF NOT EXISTS index_application_domain ON auth_service.application(application_domain);
CREATE INDEX IF NOT EXISTS index_application_email ON auth_service.application(application_email);

-- Creating the token_log table
CREATE TABLE IF NOT EXISTS auth_service.token_log (
                                                   id_token_log UUID PRIMARY KEY,
                                                   scope_token VARCHAR(50),
                                                   create_date TIMESTAMP,
                                                   active_token BOOLEAN,
                                                   user_id UUID,
                                                   application_id BIGINT,
                                                   CONSTRAINT fk_token_log_user
                                                       FOREIGN KEY (user_id) REFERENCES auth_service.users(user_id) ON DELETE CASCADE,
                                                   CONSTRAINT fk_token_log_application
                                                       FOREIGN KEY (application_id) REFERENCES auth_service.application(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS index_token_log_user_id ON auth_service.token_log(user_id);
CREATE INDEX IF NOT EXISTS index_token_log_application_id ON auth_service.token_log(application_id);

-- Creating the invalidated_token table
CREATE TABLE IF NOT EXISTS auth_service.invalidated_token (
                                                           id UUID PRIMARY KEY,
                                                           user_id UUID,
                                                           application_public_id UUID,
                                                           login_log_id UUID NOT NULL,
                                                           date_invalidate TIMESTAMP,
                                                           type_invalidation VARCHAR(50),
                                                           CONSTRAINT fk_invalidated_token_user
                                                               FOREIGN KEY (user_id) REFERENCES auth_service.users(user_id) ON DELETE CASCADE,
                                                           CONSTRAINT fk_invalidated_token_token_log
                                                               FOREIGN KEY (login_log_id) REFERENCES auth_service.token_log(id_token_log) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS index_invalidated_token_user_id ON auth_service.invalidated_token(user_id);
CREATE INDEX IF NOT EXISTS index_invalidated_token_application_public_id ON auth_service.invalidated_token(application_public_id);
CREATE INDEX IF NOT EXISTS index_invalidated_token_login_log_id ON auth_service.invalidated_token(login_log_id);

-- Creating the logged_now table
CREATE TABLE IF NOT EXISTS auth_service.logged_now (
                                                    id UUID PRIMARY KEY,
                                                    user_id UUID,
                                                    application_public_id UUID,
                                                    token_log_id UUID NOT NULL,
                                                    date_login TIMESTAMP,
                                                    is_use_refresh BOOLEAN,
                                                    date_refresh TIMESTAMP,
                                                    CONSTRAINT fk_logged_now_user
                                                        FOREIGN KEY (user_id) REFERENCES auth_service.users(user_id) ON DELETE CASCADE,
                                                    CONSTRAINT fk_logged_now_token_log
                                                        FOREIGN KEY (token_log_id) REFERENCES auth_service.token_log(id_token_log) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS index_logged_now_user_id ON auth_service.logged_now(user_id);
CREATE INDEX IF NOT EXISTS index_logged_now_application_public_id ON auth_service.logged_now(application_public_id);
CREATE INDEX IF NOT EXISTS index_logged_now_token_log_id ON auth_service.logged_now(token_log_id);

-- Creating the reset_password table
CREATE TABLE IF NOT EXISTS auth_service.reset_password (
                                                        id BIGSERIAL PRIMARY KEY,
                                                        recover_code VARCHAR(200) NOT NULL,
                                                        user_email VARCHAR(255) NOT NULL,
                                                        expiration_date TIMESTAMP
);

CREATE INDEX IF NOT EXISTS index_reset_password_email ON auth_service.reset_password(user_email);

-- Creating the user_application junction table
CREATE TABLE IF NOT EXISTS auth_service.user_application (
                                                          user_id UUID NOT NULL,
                                                          application_id BIGINT NOT NULL,
                                                          PRIMARY KEY (user_id, application_id),
                                                          CONSTRAINT fk_user_application_user
                                                              FOREIGN KEY (user_id) REFERENCES auth_service.users(user_id) ON DELETE CASCADE,
                                                          CONSTRAINT fk_user_application_application
                                                              FOREIGN KEY (application_id) REFERENCES auth_service.application(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS index_user_application_user_id ON auth_service.user_application(user_id);
CREATE INDEX IF NOT EXISTS index_user_application_application_id ON auth_service.user_application(application_id);