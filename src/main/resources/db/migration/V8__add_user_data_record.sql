CREATE TABLE IF NOT EXISTS user_data_record (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    entity_type VARCHAR(64) NOT NULL,
    client_id VARCHAR(128) NOT NULL,
    payload TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    deleted_at TIMESTAMP WITH TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_user_data_record_user FOREIGN KEY (user_id) REFERENCES app_user (id) ON DELETE CASCADE,
    CONSTRAINT uk_user_data_record_user_type_client UNIQUE (user_id, entity_type, client_id)
);

CREATE INDEX IF NOT EXISTS idx_user_data_record_user_updated ON user_data_record (user_id, updated_at);