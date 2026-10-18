# Wallet currency contract

Wallets currently hold USD only. Creating or editing a wallet does not accept a
currency selection. The backend assigns `USD`; changing a user's display
preference must not change the wallet's denomination or stored amount.

All wallet responses (including admin and nested wallet responses) expose:

```json
{
  "availableBalance": 10000,
  "currency": "USD"
}
```

`availableBalance` retains its existing API name and is an integer in minor units:
10000 means $100.00. Storage and Java types are now 64-bit (`BIGINT` / `Long`).
For JavaScript clients, exact numeric handling is limited to `Number.MAX_SAFE_INTEGER`;
do not assume the full 64-bit range can be safely represented by a JS number.

Transfer requests retain their required `currency` field. Codes are accepted
case-insensitively and sent to Stripe in lowercase. The request currency and the
destination bank account currency must match the wallet. Cross-currency requests
return HTTP 400 with code `WALLET_CURRENCY_MISMATCH`, before a Stripe transfer is
created. Amounts must be positive. Test funding applies the same currency rule.
New and replacement cards use their funding wallet's currency, validated against
the current USD-only integration policy.

Balance synchronization uses the wallet currency and preserves the 64-bit Stripe
amount. A missing currency balance is an error, not a zero balance; the existing
local value is retained for a later retry.

Flyway V3 backfills all existing wallets as USD and widens the balance column
without rescaling amounts. It also enforces the current USD-only policy in the
database. It cannot repair balances already truncated by the old integer sync;
those need a fresh Stripe synchronization. Run the migration before deploying
code that expects the currency column; older application versions cannot safely
read balances above the 32-bit range.

Display conversion, exchange-rate fetching, and user display preferences are
separate future work. UI clients can show the actual USD balance now, and later
add a clearly marked approximate equivalent without replacing that balance.
