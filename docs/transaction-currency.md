# Transaction amounts and currencies

Transaction responses (user and admin, including lists) expose two amount pairs:

- `amount` / `currency`: the existing wallet-side amount and currency.
- `merchantAmount` / `merchantCurrency`: the original merchant-side amount and
  currency reported by Stripe Issuing, without conversion or rescaling.

Each amount is a signed 64-bit integer in the smallest unit of its own currency.
For example, a capture might have `amount: -2200, currency: "usd"` and
`merchantAmount: -2000, merchantCurrency: "eur"`: $22 debited for a €20 purchase.
Do not assume every currency has two decimal places. Preserve provider signs;
these new fields do not change existing transaction direction/type behavior.

Created and updated card webhooks populate these fields. An update without a
complete merchant amount/currency pair preserves the last known pair. A zero
merchant amount is a known value, not missing data.

Flyway V4 adds nullable columns. Existing transactions and bank transactions have
unknown/not-applicable merchant values (`null`); the migration does not invent
values from the wallet amount. Historical card data can be populated by a future
provider reconciliation or a subsequent Stripe update containing the pair. No
historical Stripe backfill is performed by this migration.
