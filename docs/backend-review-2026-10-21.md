Jexis backend review — 21 October 2026

My assessment: a substantial, worthwhile learning project with a sensible modular structure, but important security and consistency work remains before I would trust it with real customer money. The main weaknesses are lifecycle rules and coordination between systems, rather than Java syntax.

This review examined the current backend/ working tree, including the ongoing repository relocation. Inventory: 488 production Java files and approximately 19,550 lines. I surveyed the modules, controllers, DTOs, persistence, configuration, deployment scripts and tests, and traced high-risk flows in detail. This is a broad source review with targeted verification, not exhaustive proof of every line or a live-service penetration test. Application source was not changed.

Verification: a fresh `./gradlew test --rerun-tasks` passed 176 tests across 39 suites; `bootJar` succeeded (up to date); all 6 Python launcher tests passed. Four additional isolated probes reproduced deleted-user refresh/token replay, administrator-to-owner promotion, acceptance of an empty reset password, and swallowed SMTP failures. These probes live under /tmp/jexis-review-probes and ran through a temporary Gradle init script; they assert the current problematic behavior, not the desired behavior. PostgreSQL-tagged migration tests and Redis-tagged integration tests were excluded by the ordinary test task and were not run. No live Stripe operations were performed.

Priority meanings: P1 = fix before real-money use; P2 = important reliability or correctness work. Items are source-confirmed unless explicitly described as conditional or a design risk.

1. **P1 — Account administrators can grant themselves owner powers.**

[MemberAuthorization](/Users/leo/Code/Projects/jexis-backend/backend/src/main/java/com/jexis/jexis_backend/member/application/security/MemberAuthorization.java:45) permits an ADMIN to edit any membership in the account. [EditMemberUseCase](/Users/leo/Code/Projects/jexis-backend/backend/src/main/java/com/jexis/jexis_backend/member/application/useCases/EditMemberUseCase.java:24) accepts OWNER without checking the actor, the previous role or an ownership-transfer policy. An administrator can PATCH their own membership to OWNER, then access owner-only wallet transfer operations. The add-member path similarly accepts OWNER. Administrators can also remove or demote an existing owner, and the last owner is not protected. The isolated probe confirmed the promotion path.

Fix: explicitly define role transitions. Reserve owner grants/removal for an ownership-transfer operation, check both the actor and target role, and protect the final owner in a transaction with account-level locking. Test the entire role-transition matrix.

2. **P1 — Deleted users can refresh sessions; refresh tokens are replayable.**

[RefreshTokensUseCase](/Users/leo/Code/Projects/jexis-backend/backend/src/main/java/com/jexis/jexis_backend/auth/application/useCases/RefreshTokensUseCase.java:63) uses `findById`, which includes soft-deleted users. Login correctly filters deleted users, but refresh bypasses that restriction. Deletion leaves memberships in place, and several authorization checks only inspect those memberships. Password changes do not revoke existing refresh tokens, and logout only clears browser cookies. A copied token remains usable until its expiration; refreshing does not consume the old token. Both deleted-user refresh and replay were reproduced.

Fix: reject deleted users consistently, store revocable session/refresh-token records (hash token values), rotate tokens atomically, and revoke sessions on password reset, deletion and logout. Define access-token revocation behavior too; a short expiration alone does not provide immediate revocation.

3. **P1 — Removing a member leaves their issued card usable.**

[RemoveMemberUseCase](/Users/leo/Code/Projects/jexis-backend/backend/src/main/java/com/jexis/jexis_backend/member/application/useCases/RemoveMemberUseCase.java:18) only deletes the membership row. [CardAuthorization](/Users/leo/Code/Projects/jexis-backend/backend/src/main/java/com/jexis/jexis_backend/card/application/security/CardAuthorization.java:43) checks the card's assigned user, with no active-membership check. [CheckAuthorizationEligibilityUseCase](/Users/leo/Code/Projects/jexis-backend/backend/src/main/java/com/jexis/jexis_backend/authorization/application/useCases/CheckAuthorizationEligibilityUseCase.java:17) checks card status and subscription eligibility but not whether the assigned person remains a member. Consequently, offboarding does not revoke card-detail access or cause this application's authorization decision to decline further spending on an otherwise active card.

Fix: make offboarding an explicit workflow that disables relevant cards at Stripe, immediately blocks local card access and authorization decisions, and records any pending external cleanup. Preserve historical transactions independently of current access. Test removal followed by card-detail access and a signed authorization request.

4. **P1 — Outbound transfers are created with the wrong direction, type and status.**

[SynchronizeOutboundTransfer](/Users/leo/Code/Projects/jexis-backend/backend/src/main/java/com/jexis/jexis_backend/transaction/application/useCases/SynchronizeOutboundTransfer.java:56) creates a new outbound-transfer record as `INBOUND_TRANSFER`, `CREDIT`, `COMPLETED`, regardless of the incoming status argument. A `treasury.outbound_transfer.created` event therefore becomes a completed incoming transfer. [CancelOutboundTransferUseCase](/Users/leo/Code/Projects/jexis-backend/backend/src/main/java/com/jexis/jexis_backend/wallet/application/useCases/CancelOutboundTransferUseCase.java:22) then refuses cancellation because it requires PROCESSING. A later PROCESSING event cannot repair the incorrect completed status because of the priority comparison.

Fix: use OUTBOUND_TRANSFER, DEBIT and the actual status, establish an explicit signed-amount convention, and test created/posted/canceled/failed events both first and in different orders. Also refresh the wallet balance on relevant state changes: the existing-record branch currently updates only status, so cancellation/failure can leave the balance stale until another sync.

5. **P1 — Retrying an outbound-transfer request can send money twice.**

[StripeOutboundTransferUseCase](/Users/leo/Code/Projects/jexis-backend/backend/src/main/java/com/jexis/jexis_backend/stripe/application/useCases/StripeOutboundTransferUseCase.java:32) sends the create request without a stable idempotency key, and no persisted local transfer-command identity surrounds it. If Stripe accepts a request but the response is lost, retrying the HTTP operation can create another transfer.

Fix: accept or generate a stable operation ID, persist its parameters/status, reject parameter changes for that ID, and use the same Stripe idempotency key on every retry. Return an operation/transfer identifier to the client. This is distinct from webhook deduplication. Stripe documents this mechanism in [Idempotent requests](https://docs.stripe.com/api/idempotent_requests).

6. **P1 — Duplicate transaction webhooks fail instead of completing idempotently.**

[CreateBankTransactionUseCase](/Users/leo/Code/Projects/jexis-backend/backend/src/main/java/com/jexis/jexis_backend/transaction/application/useCases/CreateBankTransactionUseCase.java:43) and [CreateCardTransactionUseCase](/Users/leo/Code/Projects/jexis-backend/backend/src/main/java/com/jexis/jexis_backend/transaction/application/useCases/CreateCardTransactionUseCase.java:47) construct and insert a fresh transaction every time. The unique `stripeObjectId` constraint prevents duplicate rows, which is good, but repeated deliveries hit that constraint and return an error rather than acknowledging an already-processed event. Work before the insert, including balance retrieval, is repeated as well.

Fix: persist received event IDs with processing state and make object updates idempotent under concurrency. Mark an event processed only after its local changes commit; a simple pre-insert existence check is insufficient under parallel requests. Stripe explicitly documents [duplicate delivery and unordered events](https://docs.stripe.com/webhooks).

7. **P1 — An older subscription event can overwrite newer state.**

[SyncSubscriptionUseCase](/Users/leo/Code/Projects/jexis-backend/backend/src/main/java/com/jexis/jexis_backend/subscription/application/useCases/SyncSubscriptionUseCase.java:91) locks by subscription ID but then blindly copies status and period fields from the supplied event snapshot. The webhook controller sends created/updated snapshots directly to it. A delayed ACTIVE update following cancellation can restore stale active state locally. Locks serialize processing; they do not establish event freshness.

Fix: reconcile against current provider state with a serialization strategy that avoids racing fetches and commits, or use a provider-supported version/state mechanism. Add canceled-then-old-active and upgrade-then-old-plan regression cases. Apply equivalent scrutiny to invoice and external-account state.

8. **P1 — Concurrent checkout requests can create multiple paid subscriptions.**

[CreateCheckoutUseCase](/Users/leo/Code/Projects/jexis-backend/backend/src/main/java/com/jexis/jexis_backend/subscription/application/useCases/CreateCheckoutUseCase.java:45) checks the local active subscription and then creates a checkout session, without reserving an in-progress operation per account. Two requests before either webhook arrives can both create usable checkout sessions. The direct subscription-create path has the same check-then-create window. Completing both sessions can create duplicate billing and ambiguous resource eligibility.

Fix: persist and reuse a pending checkout/subscription operation per account, serialize its creation, and explicitly reconcile duplicate provider subscriptions. A lock held only during the HTTP request is insufficient if the local paid subscription appears later through a webhook.

9. **P2 — Password-reset validation and token consumption are incomplete.**

[ConfirmPasswordResetDto](/Users/leo/Code/Projects/jexis-backend/backend/src/main/java/com/jexis/jexis_backend/user/application/dto/ConfirmPasswordResetDto.java:7) accepts an empty password because it uses only `@NotNull`; signup and the administrator password endpoint enforce 8–128 characters. The isolated validator probe confirmed that an empty reset password passes. [ConfirmPasswordResetUseCase](/Users/leo/Code/Projects/jexis-backend/backend/src/main/java/com/jexis/jexis_backend/user/application/useCases/ConfirmPasswordResetUseCase.java:17) validates a token, changes the password, and marks the token used in separate operations. Concurrent requests can both pass the unused check; failure after the password write leaves the token usable.

Fix: share one password policy across all entry points. Consume the token and change the password atomically using a conditional update or locking, and revoke sessions as part of the completed reset. Test simultaneous redemption and rollback behavior.

10. **P2 — Failed email deliveries are acknowledged and lost.**

[EmailConsumer](/Users/leo/Code/Projects/jexis-backend/backend/src/main/java/com/jexis/jexis_backend/emailService/infrastructure/messaging/EmailConsumer.java:22) catches mail exceptions and returns normally. With the configured listener defaults, RabbitMQ sees successful processing; logging the failure does not schedule another delivery. This affects activation and password recovery. The probe reproduced normal return on a simulated SMTP failure.

Fix: configure bounded retries and a dead-letter queue, and allow failures to reach that mechanism. Use an outbox for database changes that require email publication; signup currently saves a user before publishing activation mail, so publication failure can leave a registered but unactivated account without its email.

11. **P2 — The external-account advisory lock has no encompassing transaction.**

[SynchronizeExternalAccountUseCase](/Users/leo/Code/Projects/jexis-backend/backend/src/main/java/com/jexis/jexis_backend/externalAccount/application/useCases/SynchronizeExternalAccountUseCase.java:23) calls `pg_advisory_xact_lock` without a transaction on the service or its controller caller. A transaction-scoped lock cannot protect the later read/save across separate transactions. Parallel deliveries can both observe an absent external account and attempt creation.

Fix: wrap the lock, lookup and write in one transaction and retain database uniqueness as a second defense. Verify this using two PostgreSQL connections; an H2 or Mockito test cannot establish the advisory-lock guarantee.

12. **P2 — Transfer cancellation retrieves from the wrong Stripe account context.**

[StripeCancelOutboundTransferUseCase](/Users/leo/Code/Projects/jexis-backend/backend/src/main/java/com/jexis/jexis_backend/stripe/application/useCases/StripeCancelOutboundTransferUseCase.java:18) retrieves the transfer without connected-account RequestOptions, then supplies those options only to cancellation. Transfers are created under the connected account. With the current platform StripeClient, retrieval can fail before cancellation runs.

Fix: supply the same account context to retrieval and cancellation, or call the account-scoped cancel service directly. Verify account options in an adapter test. Fix item 4 first so valid processing transfers can reach this path.

13. **P2 — An early dispute webhook is discarded permanently.**

[SynchronizeDisputeUseCase](/Users/leo/Code/Projects/jexis-backend/backend/src/main/java/com/jexis/jexis_backend/dispute/application/useCases/SynchronizeDisputeUseCase.java:29) returns successfully when the local dispute does not yet exist. Dispute creation calls Stripe before saving the local entity, so a webhook can arrive during that gap. The webhook handler acknowledges success, losing the update; creation then persists NEEDS_RESPONSE even if the provider has moved on.

Fix: retain unmatched events for retry/reconciliation, or create the local operation before the external call and correlate it. Do not acknowledge an unrecorded required state change as successfully applied.

**Architecture assessment**

Keep the modular monolith. Feature packages make the code navigable, controllers generally delegate work, and important routes validate resource ownership. Separate DTOs, explicit currency checks, integer money values, Flyway-owned schema changes, resource-limit locking, and the card replacement recovery workflow are all useful decisions. Replacement even has an idempotency key and provider recovery logic; apply that level of care to transfers and other creation flows.

The layers are organizational rather than strict clean architecture: application services import concrete repositories and Stripe models. That is acceptable for this project's size. Introduce ports around the few external operations that need failure simulation or replacement; avoid creating interfaces solely to satisfy a diagram.

The next architectural investment should be explicit workflows. A database `@Transactional` annotation cannot undo a successful Stripe call. For example, [CreateAccountUseCase](/Users/leo/Code/Projects/jexis-backend/backend/src/main/java/com/jexis/jexis_backend/account/application/useCases/CreateAccountUseCase.java:60) creates the remote account before committing the local account/member/subscription. A subsequent rollback leaves a remote resource behind. Persist operation states, use stable idempotency keys, keep lock durations short, and add reconciliation. Distinguish the provider's authoritative balance from your local cached view. If the product eventually owns accounting obligations beyond displaying provider data, define ledger invariants separately rather than treating a mutable balance as an accounting ledger.

Some low-value complexity can be removed. Many tiny Get/UseCase wrappers, repeated pagination mapping and sorting code, verbose boilerplate comments and inconsistent names obscure important policies. Consolidate simple queries where it improves readability; retain separate classes for substantial workflows. Prefer records for immutable request/response DTOs and private final dependencies. Replace the catch-and-wrap pattern that drops exception causes with consistent typed integration failures. Move business transitions toward named methods such as `transferOwnership` or `beginReplacement` instead of unrestricted setters.

**Additional engineering improvements and conditional risks**

- Browser integration: no CORS configuration was found although the documented frontend and API use different origins. If the browser calls the API directly, configure the exact allowed origin and credentials, and test a real preflight/login flow. A same-origin frontend proxy changes this requirement. Cookie authentication also needs an explicit CSRF policy; SameSite=Strict mitigates many cross-site cases but is not a general substitute for evaluating same-site origins and unsafe GET operations. Logout should be POST. Set an intentional session policy for the JWT API.
- Bound page size and validate sorting/direction. Current query services commonly pass caller-supplied pageSize into PageRequest without a maximum, and malformed query values can become generic 500s. Use a shared validated pagination request and deterministic tie-break ordering.
- Measure database query counts. Many associations use eager defaults and the central DTO helper walks entity relationships. No explicit CREATE INDEX statements were found in the migrations beyond indexes implicitly provided by constraints. Start with tenant foreign keys and common tenant/date filters, then confirm plans with EXPLAIN on representative data.
- Replace [AsyncLogger](/Users/leo/Code/Projects/jexis-backend/backend/src/main/java/com/jexis/jexis_backend/common/logging/AsyncLogger.java:19) with standard SLF4J/Logback logging. Its unbounded queue can grow if output stalls, and its daemon thread is not integrated into Spring shutdown. Distinguish ordinary logs from a durable audit history for role changes, transfers, card access and administrative operations.
- Dependency alignment: build.gradle combines Boot 4.0.5 and springdoc 2.7.0. The [official springdoc compatibility matrix](https://springdoc.org/#what-is-the-compatibility-matrix-of-springdoc-openapi-with-spring-boot) assigns springdoc 3.x to Boot 4.x. The existing Swagger HTTP test passed here, so this is an unsupported pairing to correct, not a demonstrated current outage.
- Keep simulator endpoints behind an explicit development/test setting. The current /test routes are authorized but not profile-gated. Their presence does not prove live-money creation is possible; Stripe test helpers are a separate concern.
- Add CI for the ordinary suite plus disposable PostgreSQL and Redis checks. The repository has useful tests but no .github workflow was found. Existing HTTP, tenant-isolation, currency, entitlement and deployment checks are good foundations. Focus new tests on the scenarios above rather than raw coverage percentage.
- Preserve the current deployment strengths: private service ports, non-root app container, health checks, explicit profiles, sanitized proxy headers, and migration documentation. Add recovery drills and monitoring for webhook lag, failed operations, email dead letters and reconciliation mismatches. A single-host deployment can be appropriate at this stage; recovery evidence matters more than adding infrastructure.

**Product and personal feedback**

The backend suggests a team spending product: company wallets, member cards, spending controls, transaction history, disputes and paid access tiers. That is a coherent use case. I cannot assess visual design, frontend usability or demand from this repository. I also cannot infer commercial differentiation from the number of implemented endpoints.

My subjective ratings are:

| Dimension | Rating | Reason |
|---|---:|---|
| Learning/portfolio value | 8/10 | Substantial integration and domain work beyond CRUD |
| Code organization | 7/10 | Navigable feature structure with some repetitive scaffolding |
| Correctness and resilience | 4/10 | Material role, session, transfer and event-processing gaps |
| Production readiness for real money | 3/10 | Critical scenarios remain unprotected or unverified |
| Overall current backend | 6/10 | A useful foundation needing a focused hardening phase |

These numbers describe the observed artifact, not your ability as a developer. Choosing Java was not the mistake. Most important findings would also occur in a JavaScript implementation: authorization boundaries, concurrency, state transitions and partial failures are language-independent.

My product judgment is “worth continuing, demand unproven.” Clarify one target user's recurring problem and test the smallest complete workflow with that audience. For example, invite a team member, issue a limited card, inspect a purchase and revoke access safely. Explain why that workflow is easier for this audience than their current process. The README currently explains infrastructure much better than the customer's problem. Add a short product statement, target user, workflow and demo. These are product-development suggestions, not conclusions from market research.

I would postpone feature expansion and large structural rewrites. First fix role escalation, session revocation and offboarding. Then fix transfer mapping/account scope and add command idempotency. Next make webhooks durable and order-safe, close subscription-creation races, and harden reset/email workflows. Run the resulting regression suite against real PostgreSQL/Redis and a controlled Stripe test environment before treating the application as ready for customer funds.
