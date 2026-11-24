-- V9 backfilled sync_id from client_id. Rows written after V9 but before V11
-- may still have a NULL sync_id, so backfill defensively before enforcing NOT NULL.
UPDATE user_data_record
SET sync_id = client_id
WHERE sync_id IS NULL;

ALTER TABLE user_data_record ALTER COLUMN sync_id SET NOT NULL;

-- Enforce logical record identity on sync_id so a client cannot create two
-- records with the same stable UUID for one user.

ALTER TABLE user_data_record
    ADD CONSTRAINT uk_user_data_record_user_type_sync UNIQUE (user_id, entity_type, sync_id);

CREATE INDEX IF NOT EXISTS idx_user_data_record_user_sync ON user_data_record (user_id, entity_type, sync_id);

-- Ensure every existing application user has a change-sequence row. New users
-- receive one in UserService.getOrCreate.
INSERT INTO user_sync_sequence (user_id, next_sequence)
SELECT id, 1 FROM app_user
WHERE NOT EXISTS (SELECT 1 FROM user_sync_sequence s WHERE s.user_id = app_user.id);