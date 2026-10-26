CREATE TABLE outbound_transfer_commands (
    id UUID PRIMARY KEY,
    wallet_id UUID NOT NULL REFERENCES wallets(id),
    request_key UUID NOT NULL,
    external_account_id UUID NOT NULL,
    amount BIGINT NOT NULL,
    currency VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    stripe_account_id VARCHAR(255) NOT NULL,
    financial_account_id VARCHAR(255) NOT NULL,
    destination_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    stripe_transfer_id VARCHAR(255),
    UNIQUE (wallet_id, request_key)
);
