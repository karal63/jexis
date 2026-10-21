# Authentication rate limiting

Bucket4j 8.16.0 uses Redis through Lettuce to share atomic token buckets across
backend instances. This first stage protects authentication and password recovery.
Stripe operations, ordinary reads, and account-wide business-operation limits are
not implemented yet. Webhooks, logout, and health checks are outside these policies.

## Policies

Paths below are relative to the `/api` context path. All limits count attempts,
including requests that later fail validation or authentication.

| Request | Bucket identity | Capacity / refill period |
| --- | --- | --- |
| POST /auth/login | Client IP | 30 / 1 minute |
| POST /auth/login | Client IP + normalized email | 5 / 1 minute |
| POST /auth/signup | Client IP | 5 / 1 hour |
| POST /auth/forgot-password and POST /users/change-password | Shared client IP | 10 / 1 hour |
| Both password reset entry points | Shared normalized recipient email | 3 / 1 hour |
| POST /users/reset-password/confirm | Client IP | 10 / 1 minute |
| GET /user/activate | Client IP | 10 / 1 minute |
| POST /auth/refresh | Client IP | 60 / 1 minute |
| POST /auth/refresh | Verified refresh-token user ID | 30 / 1 minute |

These are token buckets, not strict rolling-window counters. A bucket starts full
and refills continuously: 5/minute permits a burst of 5 followed by one request
per 12 seconds. The refresh IP policy is intentionally broader than the per-user
policy to accommodate shared networks while rejecting invalid-token floods early.

An early Spring Security filter checks the IP allowance before JWT processing or
controller work. Login checks the IP/email pair before password verification.
Refresh checks the user bucket only after verifying the refresh token. Reset
recipient limiting lives in the shared token-creation use case, before lookup,
token creation, or email enqueueing. Email normalization only affects bucket keys;
it does not change existing account lookup semantics.

Rejected requests return HTTP 429, a JSON error with code `RATE_LIMIT_EXCEEDED`,
`Cache-Control: no-store`, and `Retry-After` in rounded-up seconds. The frontend
should respect that delay instead of automatically retrying in a tight loop.
Recipient-limited reset requests silently skip delivery and retain the normal
success response, regardless of whether the account exists. They still spend IP
allowance. Recipient keys also count requests for nonexistent accounts.

## Redis and Docker

`./scripts/dev` starts Redis with the other DEV services, exposed only at
`127.0.0.1:6379`. The local backend defaults to `redis://localhost:6379`; optional
`REDIS_URI` in `.env.dev` overrides this. `./scripts/prod` starts an internal Redis
service and fixes the backend URI to `redis://redis:6379`. Redis has no published
PROD port. Existing environment files need no edits.

DEV and PROD have separate `redis_data` volumes. Redis uses AOF persistence and
128 MB with `noeviction`: it must not evict live limits to make room for new keys.
Bucket4j expires idle keys after the time needed to refill plus one minute.
AOF's default every-second fsync can lose about a second of updates on a crash;
this is not a durable financial ledger. Never expose these unauthenticated Redis
services outside the private Docker network/loopback interface.

Redis must be reachable at backend startup. Runtime storage failures return HTTP
503 with `RATE_LIMIT_UNAVAILABLE` and `Retry-After: 5`, preventing protected work
from proceeding without its limiter. The Redis command and bucket-operation
timeouts are one second. Webhooks and unrelated requests do not consult Redis.
The existing application readiness endpoint still checks PostgreSQL/RabbitMQ;
Redis itself has a Compose health check. Monitor Redis availability and memory as
well as 429/503 rates.

Caddy overwrites `X-Forwarded-For` with the peer address and removes `Forwarded`.
PROD Spring trusts forwarded headers, so the backend must remain reachable only
through this trusted ingress. If adding a CDN/load balancer, update and test the
trusted-proxy arrangement; otherwise every request will use that proxy's IP.
DEV does not enable forwarded-header processing.

## Tuning

Defaults live in `RateLimitProperties`. Override individual policies in Spring
configuration, for example:

```properties
application.rate-limit.policies.login-ip.capacity=40
application.rate-limit.policies.login-ip.period=1m
application.rate-limit.timeout=1s
```

Changing a policy's capacity or period creates a fresh bucket namespace, granting
a fresh allowance. Old keys expire naturally. All backend instances should use
the same policy configuration. Keys include policy names and SHA-256 identity
hashes, not raw email addresses, IP addresses, or tokens. Hashing is pseudonymization,
not encryption; protect Redis access.

Tests disable the limiter through `application.rate-limit.enabled=false` so the
normal offline suite needs no Redis. Keep it enabled in DEV/PROD. Test changes
under realistic shared-IP traffic before tightening production limits. IP/email
limits alone do not stop distributed attacks using many source addresses.

## Verification

```bash
./backend/gradlew -p backend test bootJar
python3 -m unittest discover -s tests
```

Run the Redis integration suite against a disposable instance, not production:

```bash
docker run --rm -d --name jexis-rate-limit-test -p 127.0.0.1:16379:6379 redis:7.4-alpine
TEST_REDIS_URI=redis://127.0.0.1:16379 ./backend/gradlew -p backend rateLimitIntegrationTest
docker stop jexis-rate-limit-test
```

The suite tests concurrent requests through independent Redis connections,
shared recipient limits, refill, expiration, and failure behavior. HTTP tests
exercise the `/api` context path, early rejection, response headers, and exclusions.
