CREATE TABLE IF NOT EXISTS money_movement_transactions (
    id UUID PRIMARY KEY,
    idempotency_key VARCHAR(255) NOT NULL UNIQUE,
    source_account_id VARCHAR(255) NOT NULL,
    destination_account_id VARCHAR(255) NOT NULL,
    amount NUMERIC(19, 4) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    status VARCHAR(50) NOT NULL,
    blockchain_tx_hash VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_money_movement_status
    ON money_movement_transactions(status);

CREATE INDEX IF NOT EXISTS idx_money_movement_created_at
    ON money_movement_transactions(created_at);