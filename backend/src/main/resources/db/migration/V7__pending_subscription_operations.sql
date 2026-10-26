CREATE TABLE pending_subscription_operations (
    id UUID PRIMARY KEY,
    account_id UUID NOT NULL REFERENCES accounts(id),
    user_id UUID NOT NULL REFERENCES users(id),
    plan_id UUID NOT NULL REFERENCES plans(id),
    type VARCHAR(255) NOT NULL CHECK (type IN ('CHECKOUT', 'DIRECT')),
    payment_method_id VARCHAR(255),
    checkout_url TEXT,
    stripe_subscription_id VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    UNIQUE (account_id)
);
