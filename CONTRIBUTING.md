# Contributing to IoT Hub

Use [STYLE_GUIDE.md](STYLE_GUIDE.md) for code organization, documentation, typestate,
and testing conventions, and [ARCHITECTURE.md](ARCHITECTURE.md) for component
responsibilities, service boundaries, and repository contracts. These documents
define the required project structure and practices. Contributions must follow
them and keep implementation, tests, and documentation aligned.

## Understand the intended behavior

Read the documents relevant to the capability you are changing:

- [Implementation architecture](ARCHITECTURE.md): component responsibilities,
  dependencies, repository state validation, and event guarantees.
- [General description](docs/001-general-description.md): product purpose and
  functional and non-functional requirements.
- [Detailed requirements](docs/002-detailed-requirements.md): specific user-facing
  behavior, including registration, contacts, and verification codes.
- [Business architecture](docs/003-business-architecture.md): capabilities,
  information concepts, lifecycle states, and value streams.
- [Data architecture](docs/004-data-architecture.md): persisted concepts and their
  relationships.
- [Application architecture](docs/005-application-architecture.md): application
  components, interactions, and workflows.
- [Technology architecture](docs/006-technology-architecture.md): technology
  choices, runtime infrastructure, and deployment design.

Use the established domain language and relationships. Resolve material ambiguity
before implementing behavior, and record decisions in the relevant documentation.
Maintain requirements, architecture, and lifecycle rules alongside the code that
implements them.

## Local development

The repository must pin a Rust toolchain supporting edition 2024 and the
dependencies in `Cargo.lock`, including `rustfmt` and Clippy. Local development
and CI must use the same toolchain and committed lockfile. Dependency updates must
include the corresponding manifest and lockfile changes.

Use Docker with Compose for local database services. Run Rust commands from the
repository root. Maintain setup instructions that let a fresh checkout build and
run using documented configuration and migration steps.

Start the local databases with:

```sh
docker compose -f docker-compose/compose.yaml up -d
```

The Compose file defines these development services:

| Service | Database | Host port | Container port |
| --- | --- | --- | --- |
| `core-db` | PostgreSQL | `5431` | `5432` |
| `telemetry-db` | TimescaleDB on PostgreSQL | `5433` | `5432` |

Both use the database name `mydb` and local development credentials configured in
the Compose file. Treat those credentials as local-only. The data directories are
mounted under `docker-compose/` and ignored by Git; do not commit their contents.

Application configuration must come from documented environment variables or
configuration files, with safe example values and clear descriptions of required
settings. Do not embed deployment credentials in application code. Validate
required settings at startup and report configuration errors without exposing
secrets. A connection to the core database from the host uses PostgreSQL settings
and port `5431`; a container on the Compose network uses the service name and port
`5432`.

Apply SQLx migrations from `migrations/core_db` before operations that depend on
the core schema. Document how migrations run during setup and deployment.
Database tests must use isolated test databases with migrations applied and clean
up their own data. Unit tests must run without live databases or external services.

Stop the development services when finished:

```sh
docker compose -f docker-compose/compose.yaml down
```

## Make a focused change

1. Inspect the relevant requirements, modules, and current Git changes. Preserve
   unrelated work already present in the checkout.
2. Identify the behavior and acceptance cases before implementation. Keep each
   change focused on one coherent capability or correction.
3. Follow the style and architecture guides: document structs and traits, separate
   services and repositories into individual files, expose business services
   through traits, and enforce lifecycle rules through typestate. Repository
   methods must validate deserialized states and return typed results, with errors
   for requested rows in an unexpected state. Translate errors at component
   boundaries and preserve structured outcomes without exposing implementation
   details to callers.
4. Add or update colocated tests and their module-level test plans. Cover relevant
   failures and invalid states as well as successful behavior.
5. Update affected requirements, architecture descriptions, public API examples,
   and setup instructions alongside the implementation.
6. Run the relevant checks and review the final diff for unrelated changes,
   accidental secrets, and generated artifacts.

Prefer the libraries already in use. Explain new dependencies, public contract
changes, and architectural departures in the review description. Do not introduce
repository-wide rewrites or unrelated formatting while changing one feature.

### Database and compatibility changes

Include SQL migrations for schema changes and update the corresponding models,
queries, enum mappings, and data architecture. Once a migration has been applied
to a shared environment, add a new migration rather than rewriting its history.
Describe data conversion, deployment ordering, and recovery steps when a change
requires them. Test migrations against an isolated database.

Identify consumers affected by HTTP, service trait, or event contract changes.
Document compatibility implications and update relevant tests and examples. Keep
credentials, private keys, local database contents, and sensitive user data out of
source control and review descriptions.

## Validate and report results

The following checks must pass for code changes before merge. CI must enforce
them using the pinned toolchain:

```sh
cargo fmt --check
cargo check --locked
cargo clippy --all-targets --locked -- -D warnings
cargo test --locked
cargo doc --no-deps --locked
```

`cargo test --locked` includes ordinary unit tests and library documentation tests.
Run any additional integration or compile-fail harness checks required by the
change, and describe their setup. Use `--offline` only when dependencies are
already cached. CI must provide isolated dependencies for integration tests and
report failures clearly.

Test successful behavior and meaningful failure outcomes. Cover repository
absence versus state mismatch, checked typestate construction and prohibited
operations, error translation across boundaries, and external responses that
omit technical diagnostics and sensitive data. Keep each test module's documented
test plan synchronized with its tests.

For documentation-only changes, validate links, examples, and consistency with
the repository. Follow the [Markdown source style](STYLE_GUIDE.md#markdown-source-readability):
use labeled records and numbered flows instead of tables in requirements, business
architecture, and use cases, including their templates. Review the raw Markdown
and diff for readable fields and preserved identifiers. A code refactor or a full
live-service test run is not required to
edit prose. Do not report checks as passing unless they were actually run and
passed.

Report unresolved failures with the command, relevant output, and impact on
validation. Required checks must pass before a change is ready to merge; an
existing failure is not an exemption. Resolve unrelated failures in focused
prerequisite changes rather than hiding them or mixing broad repairs into a
feature contribution.

## Prepare the review

Describe the concrete problem and resulting behavior, then provide:

- The requirements or capability addressed and any important design decisions.
- Public API, lifecycle, event, or database changes and their compatibility impact.
- Tests and checks run, their results, and any unresolved failures or untested cases.
- Documentation updates and any necessary deployment or migration steps.

Reviewers should be able to connect the implementation to the requirements and
verify its behavior from the tests. Check that test plans match actual tests,
service contracts cover exposed operations, and construction or persistence paths
cannot bypass state invariants. Verify that repository tests distinguish missing
rows from wrong-state rows and that state-specific return types match validated
data. Check that errors are translated at the correct boundaries, operational
failures are logged once with appropriate context, and external responses omit
sensitive and internal details. Keep the description proportional to the change.
