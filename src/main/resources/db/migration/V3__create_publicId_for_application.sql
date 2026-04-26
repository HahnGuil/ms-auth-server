ALTER TABLE application
    ADD COLUMN IF NOT EXISTS public_id UUID;

CREATE EXTENSION IF NOT EXISTS pgcrypto;

UPDATE application
SET public_id = gen_random_uuid()
WHERE public_id IS NULL;

ALTER TABLE application
    ALTER COLUMN public_id SET NOT NULL;

CREATE INDEX IF NOT EXISTS ix_application_public_id ON application(public_id);