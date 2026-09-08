-- BaseEntity requires audit timestamps on refresh_tokens.
ALTER TABLE refresh_tokens
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT now();