# Local database setup

Development databases are defined in [compose.yaml](compose.yaml). Run them from
repository root with `docker compose -f docker-compose/compose.yaml up -d`.
The core database listens on host port 5431; telemetry listens on 5433.
Apply core migrations before using persistence operations.

## Isolated user database tests

Use [compose.test.yaml](compose.test.yaml) for tests. It uses the same pinned
PostgreSQL image as development, a separate Compose project, host port 15431,
and temporary storage. It never mounts the development data directories.
Docker Engine and the Docker Compose plugin are required.

From repository root:

```sh
docker compose -p iot-hub-tests -f docker-compose/compose.test.yaml up -d --wait core-db
DATABASE_URL=postgres://postgres:postgres@127.0.0.1:15431/iot_hub_tests \
  cargo test --locked --features database-tests --test users_persistence --test user_permissions_persistence
docker compose -p iot-hub-tests -f docker-compose/compose.test.yaml down
```

These credentials are only for the local, disposable test service. Do not point
this command at a shared database. SQLx requires permission to create databases,
creates a separate database per test, and applies `migrations/core_db` automatically.
Successful tests clean up their databases. Failed test databases are retained for
diagnostics until the disposable Compose service is removed. Always run the final
`down` command after failures too; temporary storage is removed with the container.
No standalone PostgreSQL installation or SQLx CLI is needed.

Ordinary `cargo test --locked` runs unit and documentation tests without live
services. The `database-tests` feature adds both integration targets; CI runs it
explicitly and lints it with `--all-targets --features database-tests`.

## Local PostgreSQL alternative

If the environment cannot run containers, an installed PostgreSQL server can run
an isolated temporary cluster instead. The following example uses Debian's
PostgreSQL 15 binary path; substitute the installed version's path as needed.
Compose and CI use PostgreSQL 18.3, so a local run on another major version does
not establish validation against that container image.

Run as an ordinary user, never as root. These commands create only disposable test
storage and bind only to loopback. Trust authentication is restricted to this
short-lived local test instance. The installation's default cluster is untouched.

```sh
iot_hub_pg_dir=$(mktemp -d /tmp/iot-hub-postgres.XXXXXX)
/usr/lib/postgresql/15/bin/initdb -D "$iot_hub_pg_dir" -U postgres --auth=trust
/usr/lib/postgresql/15/bin/pg_ctl -D "$iot_hub_pg_dir" \
  -l "$iot_hub_pg_dir/server.log" \
  -o "-h 127.0.0.1 -p 15431 -k $iot_hub_pg_dir" start
/usr/lib/postgresql/15/bin/createdb -h 127.0.0.1 -p 15431 -U postgres iot_hub_tests
DATABASE_URL=postgres://postgres@127.0.0.1:15431/iot_hub_tests \
  cargo test --locked --features database-tests --test users_persistence --test user_permissions_persistence
/usr/lib/postgresql/15/bin/pg_ctl -D "$iot_hub_pg_dir" stop
```

Run the final stop command even if tests fail. Inspect any failed test databases
while the temporary instance is running; discard its temporary directory after
shutdown when diagnostics are no longer needed. Do not reuse this instance for
application or shared data.

## Permission storage tests

The permission target applies migration 002 in each isolated SQLx test database.
It verifies SQL enum mappings, principal isolation, collection/object grants,
attribute decoding, rollback of invalid insert results, and provider authorization
through PostgreSQL. It provisions administrative grants directly through the
repository only inside those disposable tests. Production bootstrap provisioning
is a separate trusted operation; the provider never grants initial authority itself.

## Password column in initial user migration

Migration 001 now requires `users.password TEXT NOT NULL` for encoded password
hashes. Fresh isolated tests insert a synthetic placeholder solely to verify
storage mapping; they do not exercise hashing or authenticate with that value.
The initial migration was edited as requested, so an existing database with its
previous SQLx checksum needs an explicit upgrade plan. Do not rerun or reset a
shared database to bypass that mismatch. Only disposable databases may be recreated
using the fresh migration.
