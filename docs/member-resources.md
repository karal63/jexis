# Member financial records

These authenticated GET endpoints return a member's records within one company (`accountId`). `memberId` is the membership ID, not the user ID.

| Endpoint | Optional resource filters |
| --- | --- |
| `/accounts/{accountId}/members/{memberId}/cards` | `status`, `brand`, `type` |
| `/accounts/{accountId}/members/{memberId}/transactions` | `type`, `status`, `direction` |
| `/accounts/{accountId}/members/{memberId}/authorizations` | `approved`, `status` |
| `/accounts/{accountId}/members/{memberId}/disputes` | `reason`, `status` |

All four support `page` (starts at 1, default 1), `pageSize` (1–100, default 20), `search`, `sortBy`, and `sortDirection` (`asc` or `desc`). Responses use the existing public DTOs and contain `items`, `page`, `pageSize`, `totalItems`, and `totalPages`. Filters and pagination run in the database after applying member and company scope.

Access is granted to the member's own user, a global user with `UserRole.ADMIN`, or a member of the same company with `Role.ADMIN` or `Role.OWNER`. Ordinary users cannot read another member's financial records. An administrator of another company receives 403. Missing members and membership/company mismatches return 404, including for global administrators. Invalid pagination, enum filters, or sort direction returns 400.

Lists include records across the member's company wallets and exclude the same user's records in other companies. Cards exclude soft-deleted cards. Transactions, authorizations, and disputes retain history for deleted cards. Member transactions are identified through the card owner; company bank transfers without a card are excluded.

`sortBy` supports the following values (unknown values default to `createdAt`; default direction is descending):

- Cards: `last4`, `status`, `brand`, `type`, `cardholder`, `wallet`, `treasuryaccount`.
- Transactions: `amount`, `status`, `type`, `direction`, `bankname`, `merchantname`.
- Authorizations: `amount`, `status`, `merchantname`, `merchantcategory`, `merchantcity`, `merchantcountry`.
- Disputes: `id`, `amount`, `currency`, `status`, `reason`, `wallet`, `walletname`, `resolvedat`.

Single-record reads remain available at `/cards/{cardId}`, `/transactions/{id}`, `/authorizations/{id}`, and `/disputes/{id}`. Global administrators can access these existing routes as well as the existing wallet and card scoped read routes. Card owners can read their card payments, refunds, and reversals.

The wallet authorization list also now enforces its wallet ID in the database query.
