# Retrying outbound transfers

`POST /wallets/{id}/create-outbound-transfer` now requires an `Idempotency-Key` header containing a UUID. Missing or malformed keys are rejected before a transfer is sent.

Generate this UUID once when the user confirms a new transfer. Persist it with that transfer's request body and reuse it after timeouts, network errors, or application restarts. A genuinely new transfer needs a new UUID. Do not generate a new key in a retry interceptor or after an uncertain error.

The backend commits a command before calling Stripe. It stores the amount, currency, description, destination, and Stripe account identifiers, and uses the command ID as Stripe's idempotency key. Concurrent requests for the same wallet/key share a command, protected by an account lock and a database uniqueness constraint. Stripe receives the same parameters and key on retries, including when its earlier response was lost.

A completed command returns the existing success response without another Stripe call. Reusing a key with different transfer details returns HTTP 409 `IDEMPOTENCY_KEY_REUSED`. Currency casing is normalized.

Stripe can discard idempotency keys after 24 hours. Unresolved commands older than 23 hours return HTTP 409 `TRANSFER_RECONCILIATION_REQUIRED` instead of automatically sending again. An operator must inspect Stripe and reconcile the stored command with the actual transfer. Do not bypass this by submitting a new key. This change does not add an automated reconciliation workflow.

Apply Flyway migration V6 before deploying this endpoint. Keep command records to preserve duplicate protection for completed transfers. Requests sent before this change are not covered retroactively.
