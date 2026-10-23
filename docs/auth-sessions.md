# Authentication sessions

Login and signup create one database-backed session per device/login. Both JWTs
contain its `sid`, a random `jti`, and an explicit `token_use`. Only a SHA-256 hash
of the current refresh token is stored in `auth_sessions`; raw tokens are never
persisted or logged. Access tokens last 15 minutes. Refresh tokens last 30 days,
with a new 30-day expiration after each successful rotation.

`POST /api/auth/refresh` verifies the JWT signature/expiration and applies the
existing refresh rate limits before database work. It locks the active user's
row, verifies the session and current token hash, and commits the replacement
hash before returning new cookies. Concurrent refreshes, login, logout, password
changes and deletion use the same user lock to serialize session changes.

A repeated, previously valid refresh token returns `401 INVALID_RT` and revokes
that session, including its latest token pair. Other devices remain signed in.
The revocation commits even though the request returns an authentication error.
Missing, malformed, expired and unregistered refresh tokens also return 401.

Every authenticated request checks the session and non-deleted user in the
database. Roles and activation state come from the current database state.
Revocation therefore invalidates access tokens on subsequent requests immediately,
without waiting for their 15-minute expiry. Requests already executing are not
canceled. Authentication requires database availability; it does not fall back to
trusting an unchecked JWT or an HTTP session.

`POST /api/auth/logout` revokes the current device's session and expires both
cookies. The existing GET route remains available for client compatibility;
new clients should use POST. Logout can use a valid access cookie if the refresh
cookie is missing or invalid, and is harmless when both are absent.

Password changes, including the public password-reset flow, revoke all of the
user's sessions. User deletion atomically revokes all sessions, soft-deletes the
user and removes memberships by **user ID**. This fixes the former call that
passed a user ID to a method expecting a membership ID.

## Deployment and client behavior

Flyway applies `V5__auth_sessions.sql` automatically. Earlier tokens have no
registered session and are rejected, so all users must log in again after this
change is deployed. No token backfill is needed.

The frontend must serialize refresh requests for a session, including requests
from multiple tabs. Do not automatically replay an old refresh request after an
uncertain network failure: token reuse intentionally signs that session out.
On a refresh 401, clear client authentication state and return to login.
Cookies retain their existing Secure, HttpOnly, SameSite=Strict settings and paths.

Expired/revoked session rows are retained. They may be purged periodically once
their `expires_at` has passed; removing an unexpired session also revokes it. No
cleanup worker is enabled by this change.

## Verification

The ordinary test suite exercises session HTTP behavior and concurrent refreshes
with H2:

```bash
./backend/gradlew -p backend test bootJar
```

Run the same session tests against a dedicated **disposable PostgreSQL database**
with the following command. This suite uses Hibernate create-drop and must never
point at application data:

```bash
TEST_AUTH_DATABASE_URL=jdbc:postgresql://localhost:5432/disposable_auth_tests \
TEST_AUTH_DATABASE_USERNAME=test_user TEST_AUTH_DATABASE_PASSWORD=test_password \
./backend/gradlew -p backend test --tests '*AuthSessionIntegrationTest' --rerun-tasks
```

The existing `migrationTest` task checks Flyway upgrades, preservation of existing
users, session persistence and entity mapping against PostgreSQL in isolated schemas.
