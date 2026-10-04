# Architecture

This document defines component responsibilities and implementation boundaries for
IoT Hub. It describes the target architecture; the current code is incomplete.
Use [STYLE_GUIDE.md](STYLE_GUIDE.md) for Rust conventions and file organization,
and [CONTRIBUTING.md](CONTRIBUTING.md) for development and validation workflows.

## Domain design

IoT Hub collects device telemetry and sends commands through gateways. Organize
implementation around business domains, following existing modules such as
`users`, `v_codes`, and `events`.

Use the [general description](docs/001-general-description.md) and
[detailed requirements](docs/002-detailed-requirements.md) for intended behavior,
the [business architecture](docs/003-business-architecture.md) for capabilities,
information concepts, and lifecycle states, and the
[data architecture](docs/004-data-architecture.md) for persisted relationships.
Do not infer missing business rules from placeholders or incomplete code.

This root document provides implementation-level guidance. The
[application architecture](docs/005-application-architecture.md) and
[technology architecture](docs/006-technology-architecture.md) apply these rules
to project components and infrastructure, distinguishing required design from
implementation evidence and open decisions. Follow the
[documentation guide](docs/000-docs-guide.md) for structure and traceability.

## Components and dependencies

| Component | Responsibility |
| --- | --- |
| HTTP routes, filters, and handlers | Parse and validate transport input, invoke service contracts, and map results into HTTP responses. |
| Schemas | Represent request and response data where transport shapes differ from domain models. |
| Services | Implement business capabilities and coordinate domain operations, repositories, and event publication. |
| Domain models | Represent business concepts and enforce invariants and state-dependent operations. |
| Repositories | Own SQL, persistence mapping, and checked conversion from stored rows to domain objects. |
| Traits | External service contracts belong in the domain service entry point; local service and persistence contracts live with their owning implementations. |
| Events, publishers, and handlers | Describe domain occurrences and connect business workflows. |
| Application composition and shared state | Construct dependencies and provide services with the resources they need. |

The normal request path is HTTP adapter → service → repository → database, with
domain models carrying validated data back through the layers. Keep SQL in
repositories and business decisions in services or models. Domain models must not
depend on HTTP response types. HTTP handlers must not bypass service operations
by querying repositories directly.

Pass dependencies through constructors rather than opening hidden connections
inside business operations. A domain may have separate repositories for users,
profiles, preferences, and contacts. Keep each repository focused on the records
and persistence operations it owns, with one implementation per file as specified
in the style guide. Services coordinate operations spanning those repositories
and define transaction boundaries where atomicity is required.

### Service boundaries

Every publicly accessible service must implement a meaningful trait covering its
business operations. This includes services exposed to other domains through
crate-visible APIs. Constructors and private helpers may remain inherent methods.
Consumers should depend on the service contract where practical, and that contract
must expose the types and failure outcomes needed to use it correctly.

A domain's `app.rs` is reserved for established external service traits and
factory functions returning concrete implementations. External means consumers
outside that domain, including other domains and application adapters. Factories
compose supplied dependencies; consumers invoke operations through service traits.
The users domain's exported capabilities and factory signatures are not established
yet, so its root `app.rs` remains a documented placeholder.
See [ADR-005](docs/decisions/005-service-contract-entry-points.md) for the ownership
and compatibility trade-offs.

Keep state transitions and authorization decisions in the relevant domain or
service. A repository's state check guarantees that the returned type matches the
loaded data; it does not establish the caller's permission to perform an operation.

<a id="users-module-structure-and-trait-ownership"></a>

### Reference module structure and trait ownership

The users module layout below is the intended reference structure for all domain
modules. Apply the same separation of external service API, local services, and
persistence, adapting implementation files and contract names to each domain's
capabilities. Follow the style guide's file organization rules as modules grow.

The users-specific names and current implementation details illustrate this shared
structure; they do not prescribe other domains' capabilities or imply that every
module already follows it. The relevant files in `users` are:

```text
src/users/
    mod.rs
    app.rs
    services/
        mod.rs
        traits.rs
        user_account_service.rs
        user_profile_service.rs
        user_preferences_service.rs
        user_contact_service.rs
    repositories/
        mod.rs
        traits.rs
        storage_error.rs
        user_account_repository.rs
        user_profile_repository.rs
        user_preferences_repository.rs
        user_contact_repository.rs
    models.rs
    errors.rs
```

- **External service API:** [app.rs](src/users/app.rs) is reserved for
  the traits and factories selected for external use. It currently contains only
  module documentation; the presence of a local service does not select it for
  this API.
- **Local services:** [services/](src/users/services/mod.rs) contains the existing
  application implementations, compiled in normal builds. Its `traits.rs` owns
  `UserAccountReads`, `UserProfileReads`, `UserPreferencesReads`, and
  `UserContactReads`. The directory's module exports these contracts alongside
  their concrete implementations as `users::services`.
- **Persistence:** [repositories/traits.rs](src/users/repositories/traits.rs) owns
  `UserStore`, `UserProfileStore`, `UserPreferencesStore`, and `UserContactStore`.
  The repository module exports these contracts and their PostgreSQL adapters as
  `users::repositories`. SQL and technical error translation stay in this layer.
- **Module wiring:** [users/mod.rs](src/users/mod.rs) declares `app` and `services`
  using standard Rust module resolution: `app.rs` provides `users::app`, and
  `services/mod.rs` provides `users::services`. The implementation exports are
  available to local composition and tests; the external service API remains a
  separate design decision.

Each local service implements its read trait and receives a repository through
its constructor. For example, `UserAccountService<R>` implements
`UserAccountReads` when `R: UserStore`; `UserRepository` implements `UserStore`
using PostgreSQL. Profile, preference, and contact services follow the same
pattern. Tests supply repository doubles through these same contracts. The async
traits return `impl Future + Send`, and current consumers use generic dispatch;
the interfaces do not promise trait-object support.

An account read calls the `UserStore` contract, whose implementation decodes
`UserData` and validates the lifecycle before returning `StoredUser` or a specific
`User<State>`. The service translates optional absence into `UserReadError::NotFound`
and maps repository failures into its service error contract. These checks retain
their responsibilities regardless of how the future external API is composed.

## Repository state contract

Repository functions that return stateful domain objects must verify the stored
state **after deserialization**, before constructing and returning a trusted
typestate object. State verification belongs inside the repository boundary;
callers must not receive an unchecked object and be responsible for validating it.
A repository may use a shared checked conversion internally, but must propagate
any conversion failure in its own result.

For a lookup of a specific row in an expected state:

1. Retrieve the row by its identity within the lookup's applicable access scope.
2. Deserialize it into a persistence representation that retains the stored state.
3. Compare that state with the state promised by the return type.
4. Return the corresponding typed domain object only when the states match.
   Otherwise return a state mismatch error containing the expected and actual
   states.

For example, an optional active-user lookup returns
`Result<Option<User<ActiveUser>>, UserRepositoryError>`. Its outcomes are:

| Database outcome | Repository result |
| --- | --- |
| Requested row exists and is active | `Ok(Some(user))`, where `user` is `User<ActiveUser>` |
| Requested row exists in another state | `Err(UserRepositoryError::StateMismatch { expected, actual })` |
| Requested row does not exist in the lookup's scope | `Ok(None)` |
| Row cannot be deserialized or its stored state is invalid | A repository invalid-data error, with decoding details retained internally |
| Database operation fails | A translated repository failure, such as unavailable or unexpected failure |

These error names illustrate the required distinctions, rather than an existing
error type. In Rust, “throwing an error” here means returning `Err` through
`Result`, not panicking. A required-row lookup may instead return
`Result<User<ActiveUser>, UserRepositoryError>` and report absence as `NotFound`.
Both contracts must report an existing row in the wrong state as a state mismatch.

Do not use an expected-state SQL filter alone for an identity lookup, such as
`WHERE id = ... AND state = 'active'`, and interpret no matching result as absence.
That hides the difference between a missing row and a wrong-state row. Preserve
the distinction and perform the state check on the deserialized row. A collection
query explicitly listing active objects may filter by state, but must still
validate each returned row before constructing its typed value.

When an operation intentionally retrieves objects in multiple possible states,
return an enum whose variants contain validated typestate objects, such as active
and inactive users. Keep raw rows and unchecked `AnyUser`-style representations
internal to persistence conversion; they must not substitute for validated
typestate results at the repository boundary. Apply the same state verification
to objects returned from inserts or updates.

Use a single internal `<Model>Data` type deriving `sqlx::FromRow` as the decoded
representation for a lifecycle model, followed by checked construction. Avoid a
redundant row-to-data copy layer. Plain models with no checked-construction
invariants may map directly. See the
[style guide](STYLE_GUIDE.md#repository-return-types-and-state-validation) for
mapping conventions. Repositories still own queries, invoke validation, and
translate domain and SQLx failures into repository outcomes.

Runtime state enums remain useful in persistence and transport representations.
Generic markers and `PhantomData` do not establish that a database row has the
promised state. Direct SQLx mapping or deserialization into arbitrary trusted
typestates must not bypass validation.

A loaded typestate reflects the validated state at read time. Enforce persisted
transitions with transactions or conditional updates when concurrent changes
could invalidate an operation. A stale-state conflict must become an error rather
than a success or an absent result; typestate alone does not provide database
concurrency control.

## Error boundaries

An error crossing a component boundary must express outcomes meaningful to the
receiving layer. Lower-level implementation errors must not leak through domain
or application contracts. Use structured variants and fields so callers can make
decisions without knowing the adapter technology or parsing messages.

| Layer | Error contract |
| --- | --- |
| Domain | Business-rule violations and domain outcomes, such as `UserError::InvalidPhoneNumber`. No SQL, HTTP, filesystem, messaging, or framework error types or concepts. |
| Infrastructure adapters and repositories | Translate technical failures into consumer-facing persistence or port outcomes, such as unavailable, invalid data, or state mismatch. Technology-specific errors remain internal. |
| Application services | Describe use-case failures, such as `ActivateUserError::UserNotFound` or `PersistenceFailure`, and include domain failures where meaningful. Do not expose concrete adapter error types. |
| HTTP and other transport adapters | Translate application outcomes into safe transport representations. HTTP status codes and response implementations belong here, not in domain or application error definitions. |

The translation paths are:

```text
Infrastructure error → Repository/port error → Application error → Transport response
Domain rule violation → Domain error ────────↗
```

For example, a SQLx connection failure becomes a repository-unavailable outcome,
which an activation service translates into its persistence or service-unavailable
outcome. An optional lookup's `Ok(None)` becomes `UserNotFound` when activation
requires an existing user. A wrong-state row remains a state mismatch and must
never be translated into absence; retain that distinction in the use-case outcome.
The [repository state contract](#repository-state-contract) continues to require
validated typestate return values.

Retain original infrastructure errors as internal diagnostic sources where useful,
using private adapter errors or opaque diagnostic storage. Public variants and
fields must not require consumers to depend on, match, or downcast SQLx or other
technology-specific types. Domain errors must not carry infrastructure causes.
Transport adapters must not serialize internal errors or their source chains.

The boundary handling an operational failure owns contextual logging; intermediate
layers should preserve context without repeatedly logging it. Domain failures are
business outcomes, and the caller decides whether they merit logging, translation,
or another action. Apply the [style guide's error conventions](STYLE_GUIDE.md#error-handling)
for diagnostic context and redaction. HTTP mappings must be explicit for each
use case; no universal status mapping is implied by a variant's name.

## Events and workflows

Events describe domain occurrences. Publishers distribute them through event
contracts, and handlers invoke the capabilities needed by the next workflow step.
Keep event handlers focused on coordinating those calls rather than duplicating
business rules or repository queries.

Publish events only after the changes they describe have succeeded. For a
transactional change, success means the transaction has committed. Document the
delivery and transaction guarantees actually provided. The current in-memory
event manager spawns handler tasks; it does not establish durable delivery,
retries, ordering, or atomicity between database changes and event publication.
Any workflow requiring those guarantees needs an explicit design and tests.

## Technology and composition

The current Rust stack uses Tokio for async execution, Axum for HTTP, SQLx with
PostgreSQL for persistence, and Serde for serialization. Compose defines a core
PostgreSQL database and a TimescaleDB telemetry database; their presence does not
mean all telemetry functionality is implemented.

The binary entrypoint composes the database pool, event manager, and HTTP server;
`AppState` carries shared dependencies. Keep resource construction and deployment
configuration at this boundary. Domain services should receive dependencies and
remain testable without starting the complete application. See the contribution
guide for the current build and startup limitations.
