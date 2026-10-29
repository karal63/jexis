CREATE TABLE email_outbox (
    id UUID PRIMARY KEY,
    recipient TEXT NOT NULL,
    subject TEXT NOT NULL,
    body TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    next_attempt_at TIMESTAMP WITH TIME ZONE NOT NULL,
    attempts INTEGER NOT NULL DEFAULT 0
);
CREATE INDEX idx_email_outbox_pending ON email_outbox (next_attempt_at, created_at);
