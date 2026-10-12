# Environments, migrations and operations

## How startup works

`./scripts/dev` starts only PostgreSQL and RabbitMQ, waits for their health checks,
and runs Java locally with the `dev` Spring profile. Ctrl+C stops Java, not Docker.
For IDE debugging, load `.env.dev` as literal process environment variables and
select the `dev` profile; never use `.env.prod` to run the development process.

`./scripts/prod` builds a JAR in a Java 17 build container and copies it into a
separate runtime image. The runtime runs as a non-root user. Compose injects
`.env.prod` at runtime and selects the `prod` profile. It waits for PostgreSQL and
RabbitMQ, then for the backend health endpoint, then starts Caddy. Normal commands
never remove volumes. A failed health check causes the startup command to fail;
inspect `./scripts/logs prod` (containers are retained for diagnosis).

Spring shared properties contain variable references; profile files contain
behavior differences. The scripts load the selected private file once into process
environment variables instead of parsing it separately as Java properties. This
keeps special-character credentials consistent between local Java and Docker.
Compose's raw `env_file` format requires version 2.30 or newer. Compose also receives
`--env-file` for infrastructure settings; exported literal values take precedence
for interpolation. Use the scripts, not bare `docker compose up`.

The application rejects missing/conflicting runtime profiles and missing settings
before connecting. The `test` profile and its fake credentials exist only in test
resources. SQL logging and Swagger are disabled in PROD. SMTP is excluded from
readiness; a mail provider outage should not prevent the backend from starting.

## Changing the database schema

Flyway runs versioned SQL from `src/main/resources/db/migration` before Hibernate.
Hibernate validates mappings and never creates/updates DEV or PROD tables.

- A new database runs `V1__initial_schema.sql` automatically.
- Add later changes as `V2__description.sql`, `V3__description.sql`, etc.
- Never edit an already-applied migration; Flyway checks its checksum.
- Commit migrations alongside Java changes and test them in DEV and PostgreSQL 17.
- V1 includes card replacement fields and the unique replacement-card relationship
  previously documented in `docs/sql/account-resource-limits.sql`; do not apply that
  legacy script to a fresh migrated database.
- Migrations do not seed business entitlements, Stripe plans, or customer data.
- Baseline-on-migrate and Flyway clean are disabled. Backups are separate from Flyway.

For a reference schema generated from the current Java mappings without connecting
to any database:

```bash
./gradlew test --tests '*SchemaExportTest' --rerun-tasks
```

The result is `build/reference-schema.sql`. Review it when preparing migrations;
do not blindly replace an applied V1 with new generated output.

## Preserving an existing database

This is an explicit, one-time operator process. No database, volume or container
from the old setup is automatically modified. Do not run new application instances
against the restored database until the steps below are complete.

1. Stop the old Java/backend process to prevent writes. Identify the old PostgreSQL
   container and volume with `docker ps -a` and `docker inspect`. Keep the container
   and volume; do not run `down -v`. Save the old connection settings privately.
2. Create a logical backup. Substitute the actual old container, user and database:

   ```bash
   mkdir -p backups
   chmod 700 backups
   docker exec OLD_POSTGRES_CONTAINER pg_dump -U OLD_USER -d OLD_DB \
     --format=custom --no-owner --no-acl > backups/before-flyway.dump
   test -s backups/before-flyway.dump
   ```

   Verify the dump with `pg_restore --list` (use PostgreSQL 17 tools for a PostgreSQL
   17 source). Keep an additional backup off the server. Stop old containers that
   occupy DEV ports; stopping them preserves their volumes.
3. Fill `.env.dev` and start only the new DEV database, not Java:

   ```bash
   bash -c 'source scripts/common.sh; load_environment dev; require_docker; compose up -d --wait postgres'
   ```

   Restore into its empty database (never restore over populated data):

   ```bash
   bash -c 'source scripts/common.sh; load_environment dev; compose exec -T postgres pg_restore --exit-on-error --no-owner --no-acl -U "$DB_USERNAME" -d "$DB_NAME"' < backups/before-flyway.dump
   ```
4. Compare the restored schema with V1, including columns/types, nullability,
   foreign keys, unique/check constraints and indexes. `pg_dump --schema-only`
   helps inspect it; Hibernate validation alone does not compare all constraints.
   If it differs, reconcile the **restored copy** with reviewed SQL and verify
   record counts/relationships before baselining. Never baseline merely to silence
   a schema mismatch. The exact reconciliation depends on the old database state.
5. After verifying equivalence, explicitly record baseline version 1 with the
   Flyway CLI (11.14.1, matching this build). For local DEV:

   ```bash
   bash -c 'source scripts/common.sh; load_environment dev; export FLYWAY_URL="$DB_URL" FLYWAY_USER="$DB_USERNAME" FLYWAY_PASSWORD="$DB_PASSWORD"; flyway -baselineVersion=1 baseline'
   ```

   This records V1 as already present; it does not create missing tables. Do not
   baseline an empty database. Future migrations will execute normally.
6. Run `./scripts/dev`; Hibernate must validate successfully. Verify existing users,
   accounts and relationships. Keep the original database and backup until satisfied.
   Production uses its own fresh database unless you deliberately import data.

Existing RabbitMQ messages remain in the old broker. Drain pending jobs before
retiring it; new stacks have separate persistent broker volumes and do not copy
queued messages automatically.

## Backups and updates

Create a PROD database backup before deploying migrations:

```bash
mkdir -p backups
chmod 700 backups
bash -c 'source scripts/common.sh; load_environment prod; compose exec -T postgres pg_dump -U "$DB_USERNAME" -d "$DB_NAME" --format=custom --no-owner --no-acl' > backups/prod-before-update.dump
```

Keep backups off-server and periodically test restoration into an isolated database.
Caddy certificate state and RabbitMQ data also persist in named volumes. Changing
PostgreSQL/RabbitMQ credentials in an env file does not rotate credentials already
stored in an initialized service; rotate them deliberately before changing settings.

Run the tests before deployment, update source, then `./scripts/prod`. Keep migrations
backward-compatible when possible. Reverting Java code does not undo database
changes; rollback requires a compatible old image or a verified backup restoration.
There is no automatic destructive rollback or volume reset command.

## Deployment smoke checks

On a machine with Docker running:

1. Start DEV and confirm `/api/actuator/health` and Swagger work on port 3000.
2. Start PROD with independent test credentials and confirm health through Caddy;
   Swagger/OpenAPI should be unavailable. Real Stripe/SMTP calls require valid
   credentials even during a local PROD rehearsal.
3. Stop/restart each environment, confirming records persist and migrations do not
   rerun. Confirm one environment cannot see the other's records.
4. Stop PROD RabbitMQ temporarily: backend readiness should become unavailable.
   Restart it and confirm recovery. Check this only in a rehearsal environment.
5. Check that only Caddy publishes PROD ports and that images contain no `.env`
   files. For a server deployment, verify HTTPS and environment-specific email URLs.
