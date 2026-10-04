# Application architecture

## 1 Context and design status

This is an evidence-based draft derived from [requirements](002-detailed-requirements.md),
[business maps](003-business-architecture.md), [data architecture](004-data-architecture.md),
and the [root component rules](../ARCHITECTURE.md#components-and-dependencies).
It allocates responsibilities without asserting that every workflow exists.

Users access the service through intended mobile, web, and WhatsApp interfaces;
gateways exchange telemetry and commands. Database systems and communication
providers support these interactions. Required design below comes from adopted
project rules; proposed allocations are identified as such. Source files provide
implementation evidence only, not proof that the application builds or runs.

## 2 Components and contracts

| ID | Responsibility and contract | Business/data sources | Implementation evidence |
| --- | --- | --- | --- |
| <a id="app-1"></a>APP-1 | HTTP interface: parse requests, validate transport shapes, invoke service traits, and translate outcomes to safe responses. Public endpoints beyond health require design. | [REQ-2.5.1](002-detailed-requirements.md#req-2.5.1), [NFR-2.1](002-detailed-requirements.md#nfr-2.1) | [Router](../src/http/router.rs) defines GET / returning a health string; [server](../src/http/app.rs) uses Axum. User/auth handler scaffolds do not establish complete endpoints. |
| <a id="app-2"></a>APP-2 | User domain and services: coordinate accounts, profiles, preferences, and contacts through documented service traits; models enforce state-dependent behavior. | [CAP-1](003-business-architecture.md#cap-1), [DATA-1](004-data-architecture.md#data-1), [DATA-2](004-data-architecture.md#data-2), [DATA-3](004-data-architecture.md#data-3), [DATA-4](004-data-architecture.md#data-4) | [Models](../src/users/models.rs) enforce private lifecycle fields and checked construction; [local read services](../src/users/services/mod.rs) retain tested account, profile, preference, and contact reads. The [external service entry point](../src/users/app.rs) is reserved; exported capabilities remain undecided. Registration and lifecycle transitions remain deferred. |
| <a id="app-3"></a>APP-3 | Verification domain: generate, check, and clear codes for the requested proof purpose; return domain/use-case outcomes. | [CAP-3](003-business-architecture.md#cap-3), [DATA-9](004-data-architecture.md#data-9) | [Code traits](../src/v_codes/traits.rs) declare generation and validation; storage, expiry enforcement, and cleanup are not established by these declarations. |
| <a id="app-4"></a>APP-4 | Event component: expose contracts for occurrences, publication, subscription, and handling. Publish after committed changes; document delivery guarantees. | [CAP-5](003-business-architecture.md#cap-5), [VS-1](003-business-architecture.md#vs-1) | [Traits](../src/events/traits.rs), [in-memory manager](../src/events/models.rs), and [user publisher](../src/users/event_publishers.rs) exist; handler dispatch uses spawned tasks. |
| <a id="app-5"></a>APP-5 | Proposed notification/message services: determine permission and channel, prepare content, and coordinate dispatch through channel contracts. | [CAP-6](003-business-architecture.md#cap-6), [CAP-7](003-business-architecture.md#cap-7), [VS-2](003-business-architecture.md#vs-2) | Business maps establish responsibilities; the [messages module](../src/messages/mod.rs) is a scaffold, not evidence of an implemented workflow. |
| <a id="app-6"></a>APP-6 | Proposed channel adapters: convey messages, including WhatsApp verification, while translating provider failures into port outcomes. | [CAP-8](003-business-architecture.md#cap-8), [CAP-9](003-business-architecture.md#cap-9), [REQ-2.1.2](002-detailed-requirements.md#req-2.1.2) | Provider contracts and configured delivery are not established by the inspected source; [OPEN-005-2](005-application-architecture.md#open-005-2). |
| <a id="app-7"></a>APP-7 | Composition and shared dependencies: construct pools and adapters, run startup tasks, supply AppState, and handle top-level failures. | [TECH-1](006-technology-architecture.md#tech-1), [TECH-3](006-technology-architecture.md#tech-3) | [Entrypoint](../src/main.rs) wires pool, migrations, event manager, and HTTP; [AppState](../src/state.rs) shares pool/manager references. Startup correctness is not implied. |
| <a id="app-8"></a>APP-8 | Future organisation/device capabilities: allocate membership, invitation, gateway, telemetry, threshold, and command use cases after business mapping is complete. | [CAP-2](003-business-architecture.md#cap-2), [CAP-4](003-business-architecture.md#cap-4), [REQ-5.1.1](002-detailed-requirements.md#req-5.1.1), [REQ-5.4.2](002-detailed-requirements.md#req-5.4.2) | This is a coverage placeholder, not a single prescribed service or implemented component; [OPEN-003-4](003-business-architecture.md#open-003-4), [OPEN-005-3](005-application-architecture.md#open-005-3). |
| <a id="app-9"></a>APP-9 | Repositories: own SQL and mapping; return validated typestate objects and consumer-oriented persistence errors. | [DATA-1](004-data-architecture.md#data-1), [DATA-2](004-data-architecture.md#data-2), [DATA-3](004-data-architecture.md#data-3), [DATA-4](004-data-architecture.md#data-4) | [Repositories](../src/users/repositories/mod.rs) implement scoped reads for users, profiles, preferences, and contacts. Internal data structs are decoded before checked conversion; active identity lookups distinguish state mismatch from absence. |

Service and repository implementations follow the style guide's file-separation
rules. Services expose meaningful traits; the public operation signatures must
express expected states and recoverable outcomes. Dependencies are injected at
composition boundaries. No one-to-one mapping from every CAP to a service is
required, and [APP-8](005-application-architecture.md#app-8) does not license a catch-all implementation.

## 3 Workflows and events

### 3.1 Registration design trace

<a id="registration-flow"></a>The following trace connects
[REQ-2.1.1](002-detailed-requirements.md#req-2.1.1), [REQ-2.1.2](002-detailed-requirements.md#req-2.1.2), [VS-1](003-business-architecture.md#vs-1) to application responsibilities.
It preserves the business-stage order without inventing endpoint names or payloads.

| Step and business stage | Application responsibility | State/data outcome and limits |
| --- | --- | --- |
| 1. [STAGE-1.1](003-business-architecture.md#stage-1.1) | [APP-1](005-application-architecture.md#app-1), [APP-2](005-application-architecture.md#app-2) capture the registration request. | Phone details enter the use case; other mandatory fields are [OPEN-002-2](002-detailed-requirements.md#open-002-2). |
| 2. [STAGE-1.2](003-business-architecture.md#stage-1.2) | [APP-2](005-application-architecture.md#app-2) applies business validation after transport validation. | Invalid submissions produce typed outcomes; schema and normalization policy remain open. |
| 3. [STAGE-1.3](003-business-architecture.md#stage-1.3) | [APP-2](005-application-architecture.md#app-2), [APP-9](005-application-architecture.md#app-9) create the account and required associated data. | Persist [DATA-1](004-data-architecture.md#data-1) and agreed secondary records. Transaction and initial-state rules remain open; publish only after commit. |
| 4. [STAGE-1.4](003-business-architecture.md#stage-1.4) | [APP-3](005-application-architecture.md#app-3), [APP-4](005-application-architecture.md#app-4) initiate verification; [APP-5](005-application-architecture.md#app-5), [APP-6](005-application-architecture.md#app-6) are proposed delivery participants. | Generate the five-digit code for [DATA-9](004-data-architecture.md#data-9); WhatsApp delivery is required, but orchestration and resend guarantees are not decided. |
| 5. [STAGE-1.5](003-business-architecture.md#stage-1.5) | [APP-2](005-application-architecture.md#app-2), [APP-3](005-application-architecture.md#app-3), [APP-9](005-application-architecture.md#app-9) validate proof and persist the permitted transition. | Return a typed verified outcome only after checks succeed; exact user/contact transition is [OPEN-003-2](003-business-architecture.md#open-003-2). |

### 3.2 Notification design trace

[VS-2](003-business-architecture.md#vs-2) allocates receipt and dispatch determination to [APP-5](005-application-architecture.md#app-5),
content preparation to [APP-5](005-application-architecture.md#app-5), and communication to [APP-6](005-application-architecture.md#app-6). Preference reads come
through user-service contracts. The source does not decide whether verification
messages are subject to ordinary notification opt-out; do not apply that policy
implicitly. Provider acceptance, delivered status, retries, and deduplication are
[OPEN-005-2](005-application-architecture.md#open-005-2).

### 3.3 Event evidence and guarantees

The [user event definitions](../src/users/events.rs) contain topics
`users/user_created`, `users/user_verified`, and `users/verification_code_created`.
Payloads include user/contact information; the code-created event also contains
the verification code. This is implementation evidence, not approval to expose or
log those payloads. Review proof handling and retention before production use.

The in-memory manager dispatches to topic subscribers using spawned tasks. It
does not establish persistence, retry, ordering, transactional delivery, or a
completed registration handler chain. Required event guarantees must follow the
business outcome and be explicitly designed; event definitions alone do not prove
publication at the correct point in a workflow.

## 4 Persistence, state, and error boundaries

Use the [repository state contract](../ARCHITECTURE.md#repository-state-contract)
and [error boundaries](../ARCHITECTURE.md#error-boundaries). [APP-9](005-application-architecture.md#app-9) must load and
deserialize before verifying the state promised by its return type. Missing rows
and wrong-state rows are distinct outcomes. Services translate persistence errors
to use-case errors, and [APP-1](005-application-architecture.md#app-1) maps those to safe transport responses. Domain models
do not expose SQLx or HTTP errors.

Services define transaction scope for multi-repository changes. Transition
operations consume the prior typed value; database updates must detect stale
state when concurrency can violate a rule. Success events describe committed
changes. The mechanism bridging commit and reliable external publication remains
an open design decision rather than an assumed guarantee.

## 5 Security and quality responsibilities

| Requirement | Allocated responsibility | Evidence needed |
| --- | --- | --- |
| [REQ-2.5.1](002-detailed-requirements.md#req-2.5.1), [REQ-1.3.1](002-detailed-requirements.md#req-1.3.1) | [APP-1](005-application-architecture.md#app-1), [APP-2](005-application-architecture.md#app-2), [APP-8](005-application-architecture.md#app-8) establish identity and enforce organisation/object permissions. | Authorization policy and allow/deny tests; empty auth modules are not evidence of enforcement. |
| [REQ-3.1.1](002-detailed-requirements.md#req-3.1.1), [REQ-3.2.1](002-detailed-requirements.md#req-3.2.1) | [APP-3](005-application-architecture.md#app-3) owns secure generation and validity decisions. | Generator, proof scope, time-boundary, replay, and cleanup tests after policy decisions. |
| [NFR-1.1](002-detailed-requirements.md#nfr-1.1), [NFR-1.2](002-detailed-requirements.md#nfr-1.2), [NFR-1.3](002-detailed-requirements.md#nfr-1.3) | [APP-8](005-application-architecture.md#app-8) must allocate gateway identity and access responsibilities with [TECH-6](006-technology-architecture.md#tech-6). | Certificate lifecycle and topic-access scenarios. |
| [NFR-2.1](002-detailed-requirements.md#nfr-2.1), [NFR-4.1](002-detailed-requirements.md#nfr-4.1) | [APP-1](005-application-architecture.md#app-1), [APP-6](005-application-architecture.md#app-6) support channel-appropriate interfaces. | Agreed channel journey coverage and usability criteria. |
| [NFR-3.1](002-detailed-requirements.md#nfr-3.1) | [APP-4](005-application-architecture.md#app-4), [APP-7](005-application-architecture.md#app-7) must expose operational failure and lifecycle behavior. | Availability targets, task failure handling, shutdown, and recovery design. |

## 6 Traceability and open decisions

Components link to upstream records in section 2; workflows link to stages in
section 3; quality responsibilities link to NFRs in section 5. All component
records are new: the previous document was empty.

| ID | Decision needed and affected design |
| --- | --- |
| <a id="open-005-1"></a>OPEN-005-1 | Define service traits, HTTP schemas/endpoints, authentication, transaction boundaries, and verified-user/contact transitions once the linked business rules are resolved. Existing scaffolds are not approved public contracts. |
| <a id="open-005-2"></a>OPEN-005-2 | Define registration-to-message orchestration, WhatsApp/provider contracts, proof confidentiality, timeout/retry/idempotency, durable event needs, and delivery success semantics. Decide verification-message behavior under notification preferences. |
| <a id="open-005-3"></a>OPEN-005-3 | Complete component allocations for organisation, invitation, subscription, device, threshold, and audit capabilities. Maintain traceability rather than deriving requirements from module names. |

## 7 Implemented user read foundation

- **Contracts:** Account reads accept a user UUID. Profile and preference reads
  resolve the records referenced by that user UUID. Contact reads accept both the
  owner user UUID and contact UUID. These internal capabilities do not authorize
  callers or expose new HTTP endpoints.
- **Mapping:** SQLx decodes user/contact rows into internal `UserData` and
  `ContactData` structs. Repositories invoke checked construction and translate
  its domain errors; duplicate row structs are unnecessary. Profiles and
  preferences map directly because they have no checked-construction invariants.
- **State:** General user/contact reads return enums containing validated
  typestate objects. Active-only reads retrieve rows by identity within scope,
  then check the decoded state. An existing wrong-state row is an error;
  absence alone permits an optional repository result.
- **Errors:** Repositories translate technical failures into unavailable,
  invalid-data, state-mismatch, or unexpected outcomes. Local read services require a
  result and translate absence to not-found. Technical sources remain diagnostic
  information and must not be serialized or logged with sensitive payloads.
- **Construction:** `users/app.rs` is a documented placeholder with no
  external service traits, factories, or implementation exports. Existing read
  services and their local contracts remain normal application code under
  `services/`, loaded normally as `users::services`. Constructors accept repositories. Persistence contracts
  remain in `repositories/traits.rs`, exported through `users::repositories`.
  See [ADR-005](decisions/005-service-contract-entry-points.md).
- **Events:** Payload parsing/serialization and event constructors now return
  errors rather than panicking. Topic strings and JSON field shapes are retained;
  no registration or verification handler chain is introduced.
- **Rust compatibility:** `users::traits` has been removed. Store traits are
  available from `users::repositories`; local read contracts and concrete service
  implementations are available from `users::services` in normal
  builds. The future external API remains reserved in `users::app`.
  Repository reads still return validated `StoredUser` and `StoredContact`
  variants. Existing event JSON field shapes and the database schema are unchanged.
- **Deferred design:** Registration, lifecycle transitions, access policies,
  notification channels, and mutation transactions still depend on the existing
  open decisions. The read foundation does not complete their requirements.
- **Verification:** Colocated unit tests and Rustdoc compile-fail examples cover
  contracts and construction restrictions. The isolated Compose test target is
  documented in [database test setup](../docker-compose/README.md).
