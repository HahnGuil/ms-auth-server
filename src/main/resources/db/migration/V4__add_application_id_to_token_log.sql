-- sql
-- Database migration script V4: Adds application reference to token_log table

ALTER TABLE token_log
    ADD COLUMN IF NOT EXISTS application_id BIGINT;

CREATE INDEX IF NOT EXISTS ix_token_log_application_id
    ON token_log(application_id);

DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1
            FROM information_schema.table_constraints
            WHERE constraint_name = 'fk_token_log_application'
              AND table_name = 'token_log'
        ) THEN
            ALTER TABLE token_log
                ADD CONSTRAINT fk_token_log_application
                    FOREIGN KEY (application_id)
                        REFERENCES application(id)
                        ON DELETE SET NULL;
        END IF;
    END $$;