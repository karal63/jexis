# Account resource limits

The backend checks the account's current ACTIVE or TRIALING subscription and merges plan entitlements with subscription overrides. The period start is inclusive and the end is exclusive. Scheduled cancellation keeps access through the current period. No eligible subscription, multiple eligible subscriptions, or a missing/invalid limit denies additions.

## Configuration and deployment

Before deploying enforcement:

1. Create entitlement definitions with keys `max_cards` and `max_members` through `POST /admin/entitlements`. The type is a business category (for example, `limit`), not a variable type; the checker validates the numeric value independently of that category. Reuse existing definitions with those keys.
2. Set each plan's actual business limits through `POST /admin/plans/{id}/entitlements`. Preserve the full existing list: this API replaces assignments omitted from its payload. Values must be strings of nonnegative decimal integers within Java's signed 64-bit range. Zero means none; unlimited is unsupported.
3. Where necessary, configure subscription overrides through `POST /admin/subscriptions/{id}/entitlements`, also preserving existing assignments.
4. Apply `docs/sql/account-resource-limits.sql` to PostgreSQL before deploying the new application, or use the repository's existing Hibernate schema-update mechanism. The SQL only adds replacement-tracking columns; it does not choose or overwrite business limits.
5. Deploy all backend instances together (or drain old instances first). Older instances do not enforce limits or acquire the account lock.

No production data or plan values are automatically modified by this change. Existing resources remain after a downgrade, but additions are denied until capacity is available. Account onboarding alone can initialize its first owner without a subscription.

## API

`GET /accounts/{accountId}/limits/cards?additionalQuantity=2`

Use `members` for the member limit or `cardholders` for the cardholder limit. Both use the effective `max_members` entitlement, with separate usage counts: a value of 30 permits up to 30 members and up to 30 non-deleted cardholders. Active and inactive cardholders both count. Quantity defaults to 1; 0 asks whether current usage fits. Account-view authorization is required. The endpoint is advisory: creation repeats the check under a database lock before calling Stripe.

Example response:

```json
{
  "allowed": true,
  "used": 8,
  "limit": 10,
  "remaining": 2,
  "additionalQuantity": 2,
  "reason": null
}
```

Denials return HTTP 200 with `allowed: false` and a reason: `LIMIT_EXCEEDED`, `NO_ELIGIBLE_SUBSCRIPTION`, `AMBIGUOUS_SUBSCRIPTION`, `LIMIT_NOT_CONFIGURED`, or `INVALID_LIMIT_CONFIGURATION`. Unknown/unusable limits return `limit: null` and `remaining: 0`. Invalid resource/quantity returns 400. Creation guards return 403 with the corresponding domain error code. Existing role authorization remains required.

Internally, use `CheckAccountResourceLimitUseCase.execute(accountId, AccountResource.CARDS, quantity)` for a decision, or `requireCapacity(...)` for enforcement. Call enforcement inside a write transaction after `LockAccountUseCase.execute(accountId)`. New resources must define an explicit entitlement key and a database usage counter.

## Counting and replacements

Wallets (Stripe Treasury financial accounts) use the `max_wallets` entitlement, with subscription overrides taking
precedence over plan values. Configure a nonnegative integer value on the plan or subscription before creating wallets.
`GET /accounts/{accountId}/limits/wallets?additionalQuantity=1` checks capacity. All non-deleted wallets belonging to
that account count. Wallet creation checks capacity before calling Stripe and holds the account lock through saving
the wallet, so concurrent requests cannot claim the same slot. Missing limits deny creation. External bank accounts
are separate resources and are not subject to this wallet limit.

Cards count across all account wallets through the cardholder's account: active and inactive cards count; deleted and canceled cards do not. All members count, including owners. Card creation validates both the wallet's and cardholder's account.

Pending replacements reserve one slot, which is included in `used`. Preparation cancels the original in Stripe and commits the canceled status, original reason, and reservation. Issuance then runs in a separate transaction under the same account lock. Failure keeps the reservation; retry the same replacement endpoint with the same reason.

The Stripe request uses `replace-card-{originalCardId}` as its idempotency key. Before issuance it also searches the cardholder's Stripe cards for an existing replacement, recovering issuance whose local commit failed. A successful replacement is linked to the original in the database; subsequent retries return that card. Multiple Stripe replacements require operator reconciliation rather than issuing another card.

A pending replacement cannot be edited or deleted. Completed replacements may proceed even if a downgrade left usage over the limit, provided the subscription is eligible. Canceled/deleted cards cannot be reactivated. Stripe and database operations are not a distributed transaction: monitor pending replacements and reconcile external failures. The account lock spans Stripe calls, so long Stripe latency delays other mutations for that account.

## Verification

`./gradlew test` covers API method authorization, limits and overrides, status/date boundaries, bootstrap owner creation, account isolation, blocked issuance, replacement failures/retries, and concurrent card/member creation. Database concurrency tests use isolated H2 in PostgreSQL compatibility mode; repeat deployment smoke tests against PostgreSQL before release.

## Existing card spending

`issuing_authorization.request` resolves the issued card locally and requires exactly one eligible subscription
for its owning account using `requireEligibleSubscription`. ACTIVE/TRIALING and the current period boundaries
are the same as creation checks and are evaluated in UTC, matching stored Stripe periods. Past-due, unpaid, paused, canceled, missing, expired, or ambiguous subscriptions
do not allow spending. Scheduled cancellation retains access until the current period ends.

Authorization does not consume resource capacity: an eligible account can continue using existing cards after a
downgrade even when it cannot create more cards. Deleted, inactive, canceled, replacing, or unknown cards and
cards whose wallet belongs to a different account are declined. Signed requests with malformed authorization
data or lookup failures receive HTTP 200 with `approved: false`, the Stripe version header, and JSON content type.
Invalid signatures still return HTTP 400. Created/updated events continue recording authorization history,
including declined authorizations, independently of subscription eligibility.

This uses locally synchronized subscription status; subscription webhook delivery must remain healthy.
Configure Stripe Issuing timeout/error fallback to decline as well: requests that cannot reach this application
or exceed Stripe's response deadline cannot be protected by this check alone. See
[Stripe real-time authorizations](https://docs.stripe.com/issuing/controls/real-time-authorizations).

Related flows currently without a subscription eligibility guard:

- `CreateOutboundTransferUseCase` sends money from an existing wallet. Decide whether withdrawals should remain
  available after subscription expiry before adding enforcement.
- `EditCardUseCase` and `EditCardHolderUseCase` allow activation and spending-control changes. The authorization
  guard now blocks card spending when the subscription is ineligible, but these edits remain available.
- `SubscriptionRepository.findActiveSubscription` excludes only CANCELED, unlike the strict ACTIVE/TRIALING
  resource eligibility query. Keep that distinction in mind for subscription display and purchase/recovery flows.
