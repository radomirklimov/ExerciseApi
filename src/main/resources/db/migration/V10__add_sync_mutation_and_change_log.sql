-- Idempotency receipts: a retry of an old mutation is recognized independently
-- of newer mutations for the same user/record.
CREATE TABLE IF NOT EXISTS sync_mutation_receipt (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    mutation_id VARCHAR(128) NOT NULL,
    request_hash VARCHAR(128) NOT NULL,
    response_json TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_sync_mutation_receipt_user FOREIGN KEY (user_id) REFERENCES app_user (id) ON DELETE CASCADE,
    CONSTRAINT uk_sync_mutation_receipt_user_mutation UNIQUE (user_id, mutation_id)
);

CREATE INDEX IF NOT EXISTS idx_sync_mutation_receipt_user ON sync_mutation_receipt (user_id, created_at);

-- Per-user monotonic sequence used for cursor-based incremental pulls.
CREATE TABLE IF NOT EXISTS user_sync_sequence (
    user_id UUID PRIMARY KEY,
    next_sequence BIGINT NOT NULL DEFAULT 1,
    CONSTRAINT fk_user_sync_sequence_user FOREIGN KEY (user_id) REFERENCES app_user (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS sync_change_log (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    entity_type VARCHAR(64) NOT NULL,
    sync_id VARCHAR(128) NOT NULL,
    record_version BIGINT NOT NULL,
    sequence BIGINT NOT NULL,
    changed_at TIMESTAMP WITH TIME ZONE NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_sync_change_log_user FOREIGN KEY (user_id) REFERENCES app_user (id) ON DELETE CASCADE,
    CONSTRAINT uk_sync_change_log_user_sequence UNIQUE (user_id, sequence)
);

CREATE INDEX IF NOT EXISTS idx_sync_change_log_user_sequence ON sync_change_log (user_id, sequence);
CREATE INDEX IF NOT EXISTS idx_sync_change_log_user_changed ON sync_change_log (user_id, changed_at);