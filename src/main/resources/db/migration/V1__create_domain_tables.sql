-- Database migration script V1: Creates domain tables and indexes for the authentication system

-- Creating the users table
-- Stores user account information including credentials, profile data and access control
CREATE TABLE IF NOT EXISTS users (
                                     user_id UUID PRIMARY KEY, -- Unique identifier for the user
                                     user_name VARCHAR(150) NOT NULL, -- Username for authentication (must be unique)
    password VARCHAR(255) NOT NULL, -- Hashed password
    password_create_date TIMESTAMP, -- Timestamp when the password was created/last changed
    user_email VARCHAR(255) NOT NULL, -- User's email address (must be unique)
    first_name VARCHAR(100), -- User's first name
    last_name VARCHAR(100), -- User's last name
    picture_url TEXT, -- URL to user's profile picture
    block_user BOOLEAN, -- Flag indicating if the user account is blocked
    type_user VARCHAR(50), -- Type/category of user
    user_role VARCHAR(50) -- User's role for authorization purposes
    );

-- Unique index to ensure username uniqueness
CREATE UNIQUE INDEX IF NOT EXISTS ux_users_username ON users(user_name);
-- Unique index to ensure email uniqueness
CREATE UNIQUE INDEX IF NOT EXISTS ux_users_email ON users(user_email);
-- Index to optimize queries filtering by user role
CREATE INDEX IF NOT EXISTS ix_users_role ON users(user_role);

-- Creating the application table
-- Stores registered applications that users can access
CREATE TABLE IF NOT EXISTS application (
                                           id BIGSERIAL PRIMARY KEY, -- Auto-incrementing unique identifier
                                           name_application VARCHAR(150) NOT NULL -- Application name (must be unique)
    );

-- Unique index to ensure application name uniqueness
CREATE UNIQUE INDEX IF NOT EXISTS ux_application_name ON application(name_application);

-- Creating the TokenLog table
-- Tracks token generation and login sessions
CREATE TABLE IF NOT EXISTS token_log (
                                         id_login_log UUID PRIMARY KEY, -- Unique identifier for the login session
                                         scope_token VARCHAR(50), -- Token scope/permissions
    date_login TIMESTAMP, -- Timestamp when the login occurred
    active_token BOOLEAN, -- Flag indicating if the token is currently active
    user_id UUID NOT NULL, -- Reference to the user who owns this token
    CONSTRAINT fk_token_log_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE -- Foreign key with cascade delete
    );

-- Index to optimize queries filtering by user_id
CREATE INDEX IF NOT EXISTS ix_token_log_user_id ON token_log(user_id);

-- Creating the Invalidated Token table
-- Stores tokens that have been explicitly invalidated (logout, security events, etc.)
CREATE TABLE IF NOT EXISTS invalidated_token (
                                                 id UUID PRIMARY KEY, -- Unique identifier for the invalidation record
                                                 user_id UUID NOT NULL, -- Reference to the user who owned the token
                                                 login_log_id UUID NOT NULL, -- Reference to the original login session
                                                 date_invalidate TIMESTAMP, -- Timestamp when the token was invalidated
                                                 type_invalidation VARCHAR(50), -- Reason/type of invalidation (logout, force logout, etc.)
    CONSTRAINT fk_invalidated_token_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE, -- Foreign key with cascade delete
    CONSTRAINT fk_invalidated_token_login_log FOREIGN KEY (login_log_id) REFERENCES token_log(id_login_log) ON DELETE CASCADE -- Foreign key with cascade delete
    );

-- Index to optimize queries filtering by user_id
CREATE INDEX IF NOT EXISTS ix_invalidated_token_user_id ON invalidated_token(user_id);
-- Index to optimize queries filtering by login_log_id
CREATE INDEX IF NOT EXISTS ix_invalidated_token_login_log_id ON invalidated_token(login_log_id);

-- Creating the Logged Now table
-- Tracks currently active user sessions
CREATE TABLE IF NOT EXISTS logged_now (
                                          id UUID PRIMARY KEY, -- Unique identifier for the active session record
                                          user_id UUID NOT NULL, -- Reference to the logged-in user
                                          login_log_id UUID NOT NULL, -- Reference to the login session
                                          date_login TIMESTAMP, -- Timestamp of the login
                                          is_use_refresh BOOLEAN, -- Flag indicating if refresh token has been used
                                          date_refresh TIMESTAMP, -- Timestamp of the last token refresh
                                          CONSTRAINT fk_logged_now_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE, -- Foreign key with cascade delete
    CONSTRAINT fk_logged_now_login_log FOREIGN KEY (login_log_id) REFERENCES token_log(id_login_log) ON DELETE CASCADE -- Foreign key with cascade delete
    );

-- Index to optimize queries filtering by user_id
CREATE INDEX IF NOT EXISTS ix_logged_now_user_id ON logged_now(user_id);
-- Index to optimize queries filtering by login_log_id
CREATE INDEX IF NOT EXISTS ix_logged_now_login_log_id ON logged_now(login_log_id);

-- Creating the Reset password table
-- Stores password reset requests with recovery codes
CREATE TABLE IF NOT EXISTS reset_password (
                                              id BIGSERIAL PRIMARY KEY, -- Auto-incrementing unique identifier
                                              recover_code VARCHAR(200) NOT NULL, -- Unique recovery code sent to user (must be unique)
    user_email VARCHAR(255) NOT NULL, -- Email address associated with the reset request
    expiration_date TIMESTAMP -- Timestamp when the recovery code expires
    );

-- Unique index to ensure recovery code uniqueness
CREATE UNIQUE INDEX IF NOT EXISTS ux_reset_password_code ON reset_password(recover_code);
-- Index to optimize queries filtering by email
CREATE INDEX IF NOT EXISTS ix_reset_password_email ON reset_password(user_email);

-- Many-to-many user application relationship table
-- Links users to the applications they have access to
CREATE TABLE IF NOT EXISTS user_application (
                                                user_id UUID NOT NULL, -- Reference to the user
                                                application_id BIGINT NOT NULL, -- Reference to the application
                                                PRIMARY KEY (user_id, application_id), -- Composite primary key prevents duplicate relationships
    CONSTRAINT fk_user_application_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE, -- Foreign key with cascade delete
    CONSTRAINT fk_user_application_application FOREIGN KEY (application_id) REFERENCES application(id) ON DELETE CASCADE -- Foreign key with cascade delete
    );

-- Index to optimize queries filtering by user_id
CREATE INDEX IF NOT EXISTS ix_user_application_user_id ON user_application(user_id);
-- Index to optimize queries filtering by application_id
CREATE INDEX IF NOT EXISTS ix_user_application_application_id ON user_application(application_id);
