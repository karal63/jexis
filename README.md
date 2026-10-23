# Jexis

Spring Boot 4 / Java 17 API with PostgreSQL, RabbitMQ, Redis, Stripe and JWT authentication.
The API keeps its `/api` prefix. Select the environment using the launch scripts.

| | DEV | PROD |
|---|---|---|
| Start | `./scripts/dev` | `./scripts/prod` |
| Private configuration | `.env.dev` | `.env.prod` |
| Java | Local Gradle process | Docker image |
| PostgreSQL / RabbitMQ / Redis | Docker | Docker, separate data |
| Entry point | `http://localhost:3000/api` | `http://localhost/api` or your HTTPS domain |
| Swagger / SQL logging | Enabled | Disabled |

## Repository layout

- `backend/`: Spring Boot application, Java tests, Gradle wrapper, and Docker build.
- `scripts/`, `compose.*.yml`, and `Caddyfile`: shared development/deployment tooling.
- `tests/`: launch-script tests; `docs/`: project documentation.

Run the commands below from the repository root. In an IDE, import `backend/` as
the Gradle project. Private environment files stay at the repository root.

## First-time setup

Install Docker with Compose **2.30+** (or newer major versions). DEV also needs Java
17 or newer; Gradle's wrapper selects/downloads the Java 17 toolchain. On a fresh
checkout, copy the templates and fill every `CHANGE_ME` value:

```bash
cp .env.dev.example .env.dev
cp .env.prod.example .env.prod
chmod 600 .env.dev .env.prod
```

Existing local `.env` values were copied into `.env.dev` during this change; the
original file was preserved. Neither launch mode reads the legacy `.env` anymore.
If you have existing database data, follow [the adoption guide](docs/environments.md#preserving-an-existing-database)
**before starting the new DEV stack**. A new Compose project uses a new volume;
it does not automatically contain your old records.

Use literal `KEY=value` lines, without wrapping quotes, `export`, substitutions,
inline comments, or multiline values. `$`, `#`, backslashes and spaces inside a
value are literal. `${...}` is unsupported. Put comments on their own lines.
Never `source` an environment file: the scripts safely parse and export its values.
The selected file takes precedence over same-named shell variables. Scripts select
the Spring profile; do not put Spring profile settings into the environment files.

The files contain database, RabbitMQ, JWT, Stripe, SMTP and URL settings. Keep them
private. Both Git and the Docker build context exclude actual environment files.
Use independent credentials for DEV and PROD. JWT secrets must be at least 32 bytes.
PROD mode can use Stripe test credentials for a local rehearsal.

## Daily use

```bash
./scripts/dev             # Infrastructure, then foreground Java on port 3000
./scripts/prod            # Build and start the entire stack, wait for readiness
./scripts/logs prod       # Follow container logs (DEV Java logs are in its terminal)
./scripts/stop dev        # Stop containers, retain data; Ctrl+C stops local Java
./scripts/stop prod       # Stop all PROD containers, retain data
```

DEV exposes PostgreSQL on `127.0.0.1:5434`, RabbitMQ on `127.0.0.1:5672`, and its
management UI on `http://localhost:15672`. Redis is available on `127.0.0.1:6379`. Swagger is at
`http://localhost:3000/api/swagger-ui/index.html`.

PROD only publishes Caddy's ports 80/443. Caddy forwards to the backend's internal
port 3000; the backend connects to `postgres:5432`, `rabbitmq:5672`, and `redis:6379` over its
private Docker network. Do not put localhost database URLs into `.env.prod`.
DEV and PROD use `jexis-dev` and `jexis-prod` project names and separate persistent
PostgreSQL/RabbitMQ/Redis volumes. The old default Compose file is intentionally disabled.

The readiness endpoint is `/api/actuator/health`. It returns only health status,
checks PostgreSQL and RabbitMQ, and does not require authentication. The older
`/api/health` route is unchanged and is not used for container readiness.

## Authentication sessions

Refresh tokens rotate once per use and are backed by revocable database sessions.
Logout revokes the current session; password changes and deletion revoke all of
that user's sessions. Access-token checks enforce revocation immediately. See
[session behavior, deployment and frontend requirements](docs/auth-sessions.md).

## Rate limiting

Authentication and password recovery use Bucket4j with shared Redis counters.
Redis starts automatically with either launch script. See [policies, tuning, and
verification](docs/rate-limiting.md) for the limits, `429` responses, and outage behavior.

## Server deployment

On a Docker-capable Linux server, copy/clone this repository, create `.env.prod`,
and set `CADDY_ADDRESS=api.example.com`, `PUBLIC_API_URL=https://api.example.com/api`,
and `APP_ORIGIN=https://app.example.com`. Point the domain at the server and allow
inbound ports 80 and 443. Then run:

```bash
./scripts/prod
```

Caddy obtains and renews HTTPS certificates. For a local production rehearsal, keep
`CADDY_ADDRESS=http://localhost` and `PUBLIC_API_URL=http://localhost/api`.
`APP_ORIGIN` is the frontend URL; the frontend is not included in this repository.
Activation emails use `PUBLIC_API_URL`, password-reset emails use `APP_ORIGIN`.

Containers restart when Docker restarts; enable the Docker service at server boot.
After updating source, rerun `./scripts/prod` to rebuild. Startup is not an atomic
or zero-downtime deployment. Back up before schema changes and follow the
[operations and migration guide](docs/environments.md).

## Verification

```bash
./backend/gradlew -p backend test bootJar
python3 -m unittest discover -s tests
```

Unit/context tests use isolated test settings and H2, without personal credentials
or running services. PostgreSQL migration tests require a disposable PostgreSQL
database and create/drop only uniquely named test schemas:

```bash
TEST_DATABASE_URL=jdbc:postgresql://localhost:5434/migration_tests \
TEST_DATABASE_USERNAME=jexis TEST_DATABASE_PASSWORD=your-test-password \
./backend/gradlew -p backend migrationTest
```

Run this check against PostgreSQL 17 (the Docker version) before deployment. See the
operations guide for schema export, adoption, backups and deployment smoke checks.
