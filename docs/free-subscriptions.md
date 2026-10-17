# Free subscriptions

Creating a business account for an activated user also creates an ACTIVE local
subscription in the same database transaction. Email activation itself does not
create a business account or subscription. Provisioning is idempotent per account.

Flyway V2 seeds a published plan with code `free` and a zero-cost local price.
An existing plan with that code and its default price are preserved; its price
must be active and zero-cost for account creation to succeed. The seeded USD/month
price is catalog metadata only: free subscriptions do not renew or bill.

Local subscriptions have no Stripe subscription or payment method ID, and
`currentPeriodEnd: null` means no expiration. Existing account onboarding still
uses Stripe Connect; provisioning the subscription makes no Stripe calls.

Configure the free plan's resource limits through the existing plan-entitlement
admin API. No entitlement limits are guessed by this migration; missing limits
continue to deny the corresponding resource creation.

Upgrading from free opens the normal paid checkout (or direct paid creation).
The free subscription remains active until the Stripe sync receives ACTIVE or
TRIALING, then is canceled locally in the same transaction as the paid record.
Billing operations on local subscriptions are rejected before calling Stripe.

This change provisions newly created accounts. It does not backfill existing
accounts or automatically restore free subscriptions after paid cancellation.

The admin API reserves the `free` code: it cannot be created manually, renamed,
or assigned to another plan. The free plan cannot be deleted, given new prices,
or have its default price replaced; its prices cannot be deactivated. These
protections also apply to legacy free plans with Stripe IDs. Violations return
HTTP 400 with code `PROTECTED_FREE_PLAN` before mutations or Stripe calls.
Names, descriptions, and entitlements remain editable. The plan update API does
not expose active/status changes, and publishing only sets PUBLISHED and active.
