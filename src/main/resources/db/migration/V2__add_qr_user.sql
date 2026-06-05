-- Add nullable user_id column to qr_numbers and FK to app_users
ALTER TABLE qr_numbers
    ADD COLUMN user_id BIGINT;

-- Create index to speed up lookups by user
CREATE INDEX IF NOT EXISTS idx_qr_numbers_user_id ON qr_numbers(user_id);

-- Add foreign key constraint (nullable)
ALTER TABLE qr_numbers
    ADD CONSTRAINT fk_qr_numbers_user_id
    FOREIGN KEY (user_id) REFERENCES app_users(id);
