# Technology architecture

## 1 Scope and evidence

This draft connects [application responsibilities](005-application-architecture.md)
and [data needs](004-data-architecture.md) to the inspected Rust manifest, source,
migrations, and local Compose configuration. Those artifacts establish technology
choices and local wiring; they do not establish a production deployment or achieved
availability/security guarantees. Required engineering practices come from
[CONTRIBUTING.md](../CONTRIBUTING.md); unselected infrastructure remains open.

## 2 Technology elements

| ID | Element and purpose | Application/data/quality sources | Evidence and status |
| --- | --- | --- | --- |
| <a id="tech-1"></a>TECH-1 | Rust 2024 with Tokio for application execution and async tasks. | [APP-4](005-application-architecture.md#app-4), [APP-7](005-application-architecture.md#app-7) | [Cargo.toml](../Cargo.toml) declares the edition and runtime; [Cargo.lock](../Cargo.lock) records resolved dependencies. Toolchain pinning is required by contribution standards. |
| <a id="tech-2"></a>TECH-2 | Axum for HTTP and Serde/JSON for data serialization; Chrono for UTC timestamps and SQLx UUID types for identity. | [APP-1](005-application-architecture.md#app-1), [APP-4](005-application-architecture.md#app-4), [DATA-1](004-data-architecture.md#data-1) | [Manifest](../Cargo.toml), [HTTP server](../src/http/app.rs), and [user models](../src/users/models.rs) show usage. This does not define external API schemas. |
| <a id="tech-3"></a>TECH-3 | PostgreSQL accessed through SQLx for core persistence and migrations. | [APP-9](005-application-architecture.md#app-9), [DATA-1](004-data-architecture.md#data-1), [DATA-2](004-data-architecture.md#data-2), [DATA-3](004-data-architecture.md#data-3), [DATA-4](004-data-architecture.md#data-4) | [Pool creation](../src/db/db.rs), [core migration](../migrations/core_db/001_initial_user_models.sql), and [Compose](../docker-compose/compose.yaml). Local image: postgres:18.3-alpine3.23. |
| <a id="tech-4"></a>TECH-4 | TimescaleDB on PostgreSQL for the intended telemetry store. | [REQ-5.2.1](002-detailed-requirements.md#req-5.2.1), [APP-8](005-application-architecture.md#app-8) | [Compose](../docker-compose/compose.yaml) defines timescale/timescaledb:latest-pg18. A service declaration does not establish telemetry schemas, retention, or ingestion. |
| <a id="tech-5"></a>TECH-5 | Reproducible development, CI, and operational configuration as required engineering infrastructure. | [APP-7](005-application-architecture.md#app-7), [NFR-3.1](002-detailed-requirements.md#nfr-3.1) | [Contribution standards](../CONTRIBUTING.md) require pinned tools, checks, isolated tests, and external configuration. Production hosting and CI provider are not selected here. |
| <a id="tech-6"></a>TECH-6 | Gateway/MQTT security and connectivity required by the product. | [NFR-1.1](002-detailed-requirements.md#nfr-1.1), [NFR-1.2](002-detailed-requirements.md#nfr-1.2), [NFR-1.3](002-detailed-requirements.md#nfr-1.3), [APP-8](005-application-architecture.md#app-8) | Business requirements mandate mutual TLS and topic authorization; broker, certificate authority, and revocation design remain open. |
| <a id="tech-7"></a>TECH-7 | WhatsApp communication integration for verification and access. | [REQ-2.1.2](002-detailed-requirements.md#req-2.1.2), [NFR-2.1](002-detailed-requirements.md#nfr-2.1), [APP-6](005-application-architecture.md#app-6) | Required by product documents; concrete provider configuration, client, and delivery guarantees remain open. |

## 3 Connectivity and deployment

### 3.1 Local topology

The [Compose file](../docker-compose/compose.yaml) defines these development
database endpoints. They are local configuration facts, not approved public
production endpoints.

| Service | Database | Host port | Container port | Local storage |
| --- | --- | --- | --- | --- |
| core-db | PostgreSQL / mydb | 5431 | 5432 | docker-compose/core-db-data mounted at /pgdata |
| telemetry-db | TimescaleDB / mydb | 5433 | 5432 | docker-compose/telemetry-db-data mounted at /pgdata |

The binary requests an HTTP listener at `0.0.0.0:3000`. No application container
is defined in this Compose file. A host process uses published database ports;
containers on the Compose network use service names and container ports. Keep
local database credentials restricted to development and out of deployment code.

### 3.2 External and production boundaries

Gateway communication requires MQTT with mutual TLS and identity-based topic
access. User-facing mobile, web, and WhatsApp access requires application-facing
contracts and appropriate security boundaries. Production routing, HTTP TLS
termination, private networks, scaling, hosting, and broker/provider selection
are [OPEN-006-1](006-technology-architecture.md#open-006-1). No cloud vendor or high-availability topology is
selected by this document.

## 4 Configuration and data lifecycle

Required design: load documented settings from external configuration, validate
them at startup, and protect secrets. Apply core migrations before dependent
operations. Preserve migration history after shared deployment. Define backups,
restore procedures, retention, and recovery objectives from business requirements.

Implementation evidence: [main.rs](../src/main.rs) attempts to create a PostgreSQL
pool but supplies a hard-coded MySQL connection string; this is a configuration
defect, not the intended storage design. It invokes SQLx migrations and starts the
event manager and HTTP server. The [build script](../build.rs) watches migration
changes. No runnable deployment or successful migration is claimed by this review.

The local telemetry image uses a moving tag. Reproducible deployment image/version
policy, upgrade procedures, secret distribution, and recovery implementation are
[OPEN-006-2](006-technology-architecture.md#open-006-2). Local bind mounts provide persistence, not backups or
replication.

## 5 Operations and verification

CI must execute the [required checks](../CONTRIBUTING.md#validate-and-report-results)
using the pinned toolchain. Database integration tests need isolated databases
and migrations. Unit tests must not require external services. Documentation
checks verify references and agreement between business requirements and design.

Operational failures should be observable at boundaries with useful context and
without secret payloads. Choose health/readiness signals, metrics, log retention,
alerting, and shutdown behavior against concrete requirements. The existing GET /
response is a server response, not evidence of database readiness or downstream
availability. Tests must exercise state mismatches, dependency failures, safe
error translation, and any selected recovery or delivery guarantees.

## 6 Traceability and open decisions

| Requirement or constraint | Technology allocation | Verification or unresolved work |
| --- | --- | --- |
| [REQ-2.1.1](002-detailed-requirements.md#req-2.1.1), [REQ-2.1.2](002-detailed-requirements.md#req-2.1.2) | [TECH-1](006-technology-architecture.md#tech-1), [TECH-2](006-technology-architecture.md#tech-2), [TECH-3](006-technology-architecture.md#tech-3), [TECH-7](006-technology-architecture.md#tech-7) | Complete registration and proof workflow; source files alone do not establish acceptance. |
| [NFR-1.1](002-detailed-requirements.md#nfr-1.1), [NFR-1.2](002-detailed-requirements.md#nfr-1.2), [NFR-1.3](002-detailed-requirements.md#nfr-1.3) | [TECH-6](006-technology-architecture.md#tech-6) | Test gateway authentication, topic policies, and certificate invalidation once designed. |
| [NFR-2.1](002-detailed-requirements.md#nfr-2.1) | [TECH-2](006-technology-architecture.md#tech-2), [TECH-7](006-technology-architecture.md#tech-7) | Agree feature coverage for mobile, web, and WhatsApp. |
| [NFR-3.1](002-detailed-requirements.md#nfr-3.1), [NFR-3.2](002-detailed-requirements.md#nfr-3.2) | [TECH-4](006-technology-architecture.md#tech-4), [TECH-5](006-technology-architecture.md#tech-5), [TECH-6](006-technology-architecture.md#tech-6) | Set availability/recovery targets and store-and-send recommendations; benchmark only against approved measures. |
| [NFR-4.1](002-detailed-requirements.md#nfr-4.1) | [APP-1](005-application-architecture.md#app-1) | Usability is an interface responsibility, not a framework guarantee. |

| ID | Decision needed and impact |
| --- | --- |
| <a id="open-006-1"></a>OPEN-006-1 | Choose production deployment/network/TLS topology, MQTT broker and certificate infrastructure, and WhatsApp integration after access and delivery requirements are settled. |
| <a id="open-006-2"></a>OPEN-006-2 | Define reproducible image/tool versions, configuration names and sources, secrets, migration operation, backups/restores, retention, and upgrade procedures. Follow contribution standards without assuming their tooling is already implemented. |
| <a id="open-006-3"></a>OPEN-006-3 | Define measurable availability, capacity, recovery, telemetry freshness, observability, alerting, and shutdown requirements with the application and data owners. |

All technology records are new; the previous document was empty. Sources and
status appear in each record so future changes can reconcile evidence with the
required design.
