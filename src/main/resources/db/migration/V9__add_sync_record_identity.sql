-- Add identity/timestamp columns needed by the sync contract. Existing rows are kept.
ALTER TABLE user_data_record ADD COLUMN IF NOT EXISTS sync_id VARCHAR(128);
ALTER TABLE user_data_record ADD COLUMN IF NOT EXISTS client_updated_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE user_data_record ADD COLUMN IF NOT EXISTS server_updated_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE user_data_record ADD COLUMN IF NOT EXISTS last_mutation_id VARCHAR(128);

-- Backfill from existing values. client_id was the only stable identity available.
UPDATE user_data_record
SET sync_id = client_id
WHERE sync_id IS NULL;

UPDATE user_data_record
SET client_updated_at = updated_at,
    server_updated_at = updated_at
WHERE client_updated_at IS NULL OR server_updated_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_user_data_record_sync_id ON user_data_record (user_id, sync_id);
CREATE INDEX IF NOT EXISTS idx_user_data_record_server_updated ON user_data_record (user_id, server_updated_at, id);