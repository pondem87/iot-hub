# Data architecture

## 1 Purpose and derivation

Derive data from the [information map](003-business-architecture.md#31-concepts-types-and-states)
and the capabilities that use or modify it. The four user entities below preserve
the original data architecture. Additional information concepts are recorded as
candidates, not approved tables. Business meaning, logical structure, and physical
storage are separate views; neither a UUID nor a SQL column defines a business rule.

Physical evidence comes from the [initial migration](../migrations/core_db/001_initial_user_models.sql)
and [user models](../src/users/models.rs). Their presence establishes schema and
type definitions, not a fully working repository or business workflow.

## 2 Conceptual relationships

| Source concept | Entity candidate | Relationship and cardinality | Scenario or open decision |
| --- | --- | --- | --- |
| [INFO-1](003-business-architecture.md#info-1) | [DATA-1](004-data-architecture.md#data-1) | One user references one profile and one preference record. Their exclusive ownership is enforced by unique foreign keys in the migration. | [CAP-1.2](003-business-architecture.md#cap-1.2), [CAP-1.3](003-business-architecture.md#cap-1.3); whether unowned secondary rows may exist is [OPEN-004-1](004-data-architecture.md#open-004-1). |
| [INFO-1.1](003-business-architecture.md#info-1.1) | [DATA-2](004-data-architecture.md#data-2) | Profile depends on user in the business map; SQL permits an unreferenced profile. | Profile creation/deletion atomicity is [OPEN-004-1](004-data-architecture.md#open-004-1). |
| [INFO-1.2](003-business-architecture.md#info-1.2) | [DATA-3](004-data-architecture.md#data-3) | Preferences depend on user in the business map; SQL permits an unreferenced preference record. | Preference ownership follows [CAP-1.3](003-business-architecture.md#cap-1.3); cleanup policy is [OPEN-004-1](004-data-architecture.md#open-004-1). |
| [INFO-1.3](003-business-architecture.md#info-1.3) | [DATA-4](004-data-architecture.md#data-4) | Each contact belongs to one user; SQL permits zero or more contacts per user. | [REQ-2.2.2](002-detailed-requirements.md#req-2.2.2), [REQ-2.2.5](002-detailed-requirements.md#req-2.2.5); mandatory contact and uniqueness rules are [OPEN-004-1](004-data-architecture.md#open-004-1). |
| [INFO-2](003-business-architecture.md#info-2) | [DATA-5](004-data-architecture.md#data-5) | Organisation relates to users, subscription, access constraints, limits, and invitations; exact cardinalities need scenario validation. | [REQ-1.1.3](002-detailed-requirements.md#req-1.1.3), [REQ-1.2.3](002-detailed-requirements.md#req-1.2.3), [OPEN-004-2](004-data-architecture.md#open-004-2) |
| [INFO-2.1](003-business-architecture.md#info-2.1) | [DATA-6](004-data-architecture.md#data-6) | Access constraint depends on an organisation. | [CAP-2.4](003-business-architecture.md#cap-2.4), [OPEN-004-2](004-data-architecture.md#open-004-2) |
| [INFO-2.2](003-business-architecture.md#info-2.2) | [DATA-7](004-data-architecture.md#data-7) | Limit depends on an organisation. | [CAP-2.5](003-business-architecture.md#cap-2.5), [OPEN-004-2](004-data-architecture.md#open-004-2) |
| [INFO-2.3](003-business-architecture.md#info-2.3) | [DATA-8](004-data-architecture.md#data-8) | Dependent invitation candidate retained pending reconciliation. | [OPEN-003-1](003-business-architecture.md#open-003-1), [OPEN-004-2](004-data-architecture.md#open-004-2) |
| [INFO-3](003-business-architecture.md#info-3) | [DATA-9](004-data-architecture.md#data-9) | Code associates with a user or contact according to purpose; multiplicities and ownership are not decided. | [CAP-3.4](003-business-architecture.md#cap-3.4), [OPEN-002-3](002-detailed-requirements.md#open-002-3) |
| [INFO-4](003-business-architecture.md#info-4) | [DATA-10](004-data-architecture.md#data-10) | Invitation associates with organisation and user; distinctness from membership invitation is unresolved. | [CAP-4.4](003-business-architecture.md#cap-4.4), [OPEN-003-1](003-business-architecture.md#open-003-1) |

## 3 Logical entities and attribute dictionary

### 3.1 User

<a id="data-1"></a>**DATA-1 — User.** A person using the service, derived from
[INFO-1](003-business-architecture.md#info-1), [CAP-1.1](003-business-architecture.md#cap-1.1), [REQ-2.1.1](002-detailed-requirements.md#req-2.1.1). The UUID is a technical surrogate
identifier. The phone number is a unique business identifier candidate in the
schema, with normalization and reassignment rules still unresolved.

| Attribute | Logical type | Constraints and business meaning | Derivation |
| --- | --- | --- | --- |
| id | UUID | Required primary key; technical identity. | Existing data model and migration. |
| phone_number | String | Required and unique in SQL; phone representation and equality need business rules. | [REQ-2.1.1](002-detailed-requirements.md#req-2.1.1), [OPEN-002-2](002-detailed-requirements.md#open-002-2) |
| type | User type | superuser, staff, customer; required. Physical name: user_type. | [INFO-1](003-business-architecture.md#info-1), [OPEN-003-2](003-business-architecture.md#open-003-2) |
| state | User state | unverified, active, inactive, barred, deleted; required. | [INFO-1](003-business-architecture.md#info-1), [CAP-1.4](003-business-architecture.md#cap-1.4) |
| profile_id | UUID reference | Required, unique reference to [DATA-2](004-data-architecture.md#data-2); one profile per user. | [CAP-1.2](003-business-architecture.md#cap-1.2) |
| preferences_id | UUID reference | Required, unique reference to [DATA-3](004-data-architecture.md#data-3); one preference record per user. | [CAP-1.3](003-business-architecture.md#cap-1.3) |
| created_at | UTC timestamp | Required creation time; recorded by the technical implementation. | Existing data model and migration. |
| updated_at | UTC timestamp | Required change time; update semantics need definition. | [OPEN-004-1](004-data-architecture.md#open-004-1) |

### 3.2 User Profile

<a id="data-2"></a>**DATA-2 — User Profile.** Characteristics describing a user,
derived from [INFO-1.1](003-business-architecture.md#info-1.1), [CAP-1.2](003-business-architecture.md#cap-1.2), [REQ-2.2.6](002-detailed-requirements.md#req-2.2.6). This is a dependent
concept with its own identity and retrieval needs; it has no recorded lifecycle.

| Attribute | Logical type | Constraints and business meaning | Derivation |
| --- | --- | --- | --- |
| id | UUID | Required primary key; technical identity. | Existing data model and migration. |
| name | String | Required in SQL; name validation rules are unresolved. | [REQ-2.2.6](002-detailed-requirements.md#req-2.2.6), [OPEN-002-2](002-detailed-requirements.md#open-002-2) |
| updated_at | UTC timestamp | Required; records changes to the profile. | Existing data model; [OPEN-004-1](004-data-architecture.md#open-004-1) for timestamp policy. |

### 3.3 User Preferences

<a id="data-3"></a>**DATA-3 — User Preferences.** Parameters expressing a user's
needs, derived from [INFO-1.2](003-business-architecture.md#info-1.2), [CAP-1.3](003-business-architecture.md#cap-1.3). Preferences are ordinary
mutable values, not a lifecycle requiring typestate.

| Attribute | Logical type | Constraints and business meaning | Derivation |
| --- | --- | --- | --- |
| id | UUID | Required primary key; technical identity. | Existing data model and migration. |
| allow_notifications | Boolean | Required; expresses notification opt-in. No default is defined in the SQL. | [REQ-2.3.1](002-detailed-requirements.md#req-2.3.1) |
| updated_at | UTC timestamp | Required; records changes to preferences. | Existing data model; [OPEN-004-1](004-data-architecture.md#open-004-1) for timestamp policy. |

The requirement to choose a notification channel is preserved in
[REQ-2.3.2](002-detailed-requirements.md#req-2.3.2); no corresponding attribute exists in the original data
model or migration. Define its representation after channel policy is settled,
rather than silently treating the opt-in flag as sufficient.

### 3.4 User Contact

<a id="data-4"></a>**DATA-4 — User Contact.** An identifier for a user in a
communication channel, derived from [INFO-1.3](003-business-architecture.md#info-1.3), [CAP-1.7](003-business-architecture.md#cap-1.7). The
contact has its own identity and lifecycle while depending on its user.

| Attribute | Logical type | Constraints and business meaning | Derivation |
| --- | --- | --- | --- |
| id | UUID | Required primary key; technical identity. | Existing data model and migration. |
| type | Contact type | Phone number or email address; SQL values phonenumber and emailaddress. | [INFO-1.3](003-business-architecture.md#info-1.3), [CAP-1.7.2](003-business-architecture.md#cap-1.7.2) |
| value | String | Required; the address or number. No uniqueness constraint exists in the migration. | [REQ-2.2.2](002-detailed-requirements.md#req-2.2.2), [REQ-2.2.5](002-detailed-requirements.md#req-2.2.5), [OPEN-004-1](004-data-architecture.md#open-004-1) |
| states | Contact state | One state value: unverified, active, disabled. The plural field name is retained from the source. | [INFO-1.3](003-business-architecture.md#info-1.3), [CAP-1.7.3](003-business-architecture.md#cap-1.7.3) |
| user_id | UUID reference | Required reference to [DATA-1](004-data-architecture.md#data-1); many contacts may refer to one user. | [CAP-1.7](003-business-architecture.md#cap-1.7) |
| created_at | UTC timestamp | Required creation time. | Existing data model and migration. |
| updated_at | UTC timestamp | Required change time. | Existing data model; [OPEN-004-1](004-data-architecture.md#open-004-1) for timestamp policy. |

### 3.5 Additional entity candidates

Every remaining mapped concept is considered below, including secondary concepts.
These candidates have no approved attributes or physical tables in this document.
Do not use the separate invitation candidates to justify duplicate storage before
resolving their meaning.

| ID | Business definition and source | Derivation status |
| --- | --- | --- |
| <a id="data-5"></a>DATA-5 | Organisation: a container for assets belonging to one entity. [INFO-2](003-business-architecture.md#info-2), [CAP-2](003-business-architecture.md#cap-2) | Candidate; derive membership, ownership, limits, and states from agreed scenarios. |
| <a id="data-6"></a>DATA-6 | Organisation Access Constraint: policy restricting resource access. [INFO-2.1](003-business-architecture.md#info-2.1), [CAP-2.4](003-business-architecture.md#cap-2.4) | Dependent candidate; object/attribute constraint representation is unresolved. |
| <a id="data-7"></a>DATA-7 | Organisation Limit: policy determining resource limits. [INFO-2.2](003-business-architecture.md#info-2.2), [CAP-2.5](003-business-architecture.md#cap-2.5) | Dependent candidate; entitlement and continuity representation is unresolved. |
| <a id="data-8"></a>DATA-8 | Membership Invitation: source definition is unfinished. [INFO-2.3](003-business-architecture.md#info-2.3) | Hold modeling decision pending [OPEN-003-1](003-business-architecture.md#open-003-1). |
| <a id="data-9"></a>DATA-9 | Verification Code: proof for request or contact verification. [INFO-3](003-business-architecture.md#info-3), [CAP-3](003-business-architecture.md#cap-3) | Candidate; validity, proof storage, purpose, and cleanup require [OPEN-002-3](002-detailed-requirements.md#open-002-3). |
| <a id="data-10"></a>DATA-10 | Invitation: a mechanism allowing users to join an organisation. [INFO-4](003-business-architecture.md#info-4), [CAP-4](003-business-architecture.md#cap-4) | Candidate; reconcile with [DATA-8](004-data-architecture.md#data-8) before choosing physical structure. |

## 4 Lifecycle and integrity

Runtime state enums store lifecycle values; trusted domain objects use typestate
according to the [root state contract](../ARCHITECTURE.md#repository-state-contract).
Repositories must deserialize, verify the state, and return the promised typed
object. Existing wrong-state rows produce state mismatch errors; optional absence
is reserved for genuinely missing rows. Persisted transitions require concurrency
control where stale reads could invalidate an operation.

The listed states are source vocabulary, not an approved transition matrix.
[OPEN-003-2](003-business-architecture.md#open-003-2) owns the missing transition rules, including the
relationship between phone verification and account activation. Contact kinds and
user roles are classifications, not lifecycle states. Profiles and preferences
have no recorded lifecycle.

The original user aggregate groups users, profiles, preferences, and contacts.
Transaction boundaries and secondary-row cleanup must be decided explicitly; the
grouping alone does not establish cascade deletion or atomic creation semantics.

## 5 Physical mappings and evidence

| Entity | SQL table and mapping | Constraints and evidence |
| --- | --- | --- |
| [DATA-1](004-data-architecture.md#data-1) | users; logical type → user_type (SQL enum user_type); state → user_state enum. | UUID primary key; unique phone_number; unique non-null profile/preferences foreign keys. |
| [DATA-2](004-data-architecture.md#data-2) | user_profiles; name → TEXT. | UUID primary key; non-null name and updated_at. |
| [DATA-3](004-data-architecture.md#data-3) | user_preferences; allow_notifications → BOOLEAN. | UUID primary key; non-null flag and updated_at; no channel field. |
| [DATA-4](004-data-architecture.md#data-4) | user_contacts; logical type → user_contact_type; states → user_contact_state. | UUID primary key; non-null user_id foreign key; user_contacts_user_id_idx supports user lookup. |

All timestamps use `TIMESTAMP WITH TIME ZONE` in SQL and `DateTime<Utc>` in
Rust. All columns in the migration are non-null, including primary keys. No
cascading deletion, timestamp defaults, automatic timestamp updates, or contact
value uniqueness is declared. These observations do not approve their absence
as business policy.

The Rust contact type is named `contact_type`, whereas the SQL column is
`user_contact_type`. SQL enum values `phonenumber` and `emailaddress` must be
mapped explicitly rather than assuming a particular case conversion. The
`states` column contains one enum, not a collection. Keep these naming differences
visible until a coordinated code/schema change resolves them.

## 6 Governance and quality

The [use/modify map](003-business-architecture.md#5-cross-mappings-and-downstream-derivation)
identifies which business capabilities produce and consume information; it does
not grant database access. Services apply authorization, repositories enforce
persistence invariants, and external interfaces expose only appropriate data.

Validate required values, enum mappings, uniqueness, reference integrity, and
typed-state conversion. User-entered contact and profile data need agreed
normalization rules. Identity, credentials, and verification proofs require
controlled access and redacted diagnostics. Retention, deletion, provenance,
timestamp authority, quality measures, and telemetry freshness remain open.

## 7 Open decisions and legacy mapping

| ID | Decision needed and impact |
| --- | --- |
| <a id="open-004-1"></a>OPEN-004-1 | Define user/contact identifier normalization and uniqueness, orphan secondary-row handling, required initial contacts, creation transactions, timestamp authority, and deletion/retention. Resolve global user_type versus organisation-scoped superuser semantics using the requirements. These govern [DATA-1](004-data-architecture.md#data-1) through [DATA-4](004-data-architecture.md#data-4). |
| <a id="open-004-2"></a>OPEN-004-2 | Validate candidate entity boundaries, relationships, cardinalities, and logical attributes for [DATA-5](004-data-architecture.md#data-5) through [DATA-10](004-data-architecture.md#data-10). Finish missing business concepts before designing subscription, telemetry, message, event, or audit schemas. |
| <a id="open-004-3"></a>OPEN-004-3 | Define quality measures, provenance, data access responsibilities, retention, backup/recovery expectations, and freshness with their business owners; no values are invented here. |

| Original section and label | New record |
| --- | --- |
| The User aggregate / 1 User | [DATA-1](004-data-architecture.md#data-1) |
| The User aggregate / 2 UserProfile | [DATA-2](004-data-architecture.md#data-2) |
| The User aggregate / 3 UserPreferences | [DATA-3](004-data-architecture.md#data-3) |
| The User aggregate / 4 UserContact | [DATA-4](004-data-architecture.md#data-4) |

All original attributes are retained in section 3. Names differing from SQL or
Rust are explained in section 5 rather than silently renamed.

## 8 User read persistence implementation

- **Mapping:** Internal `UserData` and `ContactData` structs in the users models
  derive SQLx `FromRow` and feed checked lifecycle construction without duplicate
  row structs. Profiles and preferences have no checked-construction invariants
  and derive `FromRow` directly. Runtime enums declare SQL type names and explicit
  stored spellings, including `phonenumber` and `emailaddress`; the
  physical columns `user_contact_type` and `states` map to the domain accessors
  `contact_type()` and `state()`. The existing schema is unchanged.
- **Validation:** SQL decoding rejects unsupported enum values. Checked
  construction validates user/contact state before returning trusted typestate
  objects, including the variants used by general reads.
- **Ownership:** Profile and preference reads join through the user foreign keys.
  Contact reads constrain both owner identity and contact identity. These query
  scopes do not establish authorization or lifecycle transition policy.
- **Compatibility:** No migrations or stored-data conversions are required.
  Lifecycle fields are private and direct SQLx mapping into generic domain
  objects is removed. Rust consumers use documented read accessors and contracts.
- **Tests:** Each integration test receives its own migrated PostgreSQL database
  through SQLx. Deliberately malformed schema/data fixtures are confined to these
  test databases; the Compose service uses temporary storage.
