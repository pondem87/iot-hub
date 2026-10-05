# Data architecture

## 1 Purpose and status

This document maps [information concepts](003-business-architecture.md#31-concepts-types-and-states)
to tables, columns, keys, constraints, and indexes. Business definitions and lifecycle
rules belong in [003](003-business-architecture.md); workflows belong in
[005](005-application-architecture.md).

**Existing** means declared by the
[core migration](../migrations/core_db/001_initial_user_models.sql).
**Proposed** means a schema design requiring a new migration and implementation;
no proposed column or table is present in that migration.

## 2 Existing core tables

All existing columns are `NOT NULL`. UUID primary keys, timestamps, and other values
have no SQL defaults. Timestamps use `TIMESTAMPTZ` / Rust `DateTime<Utc>`.
There are no `ON DELETE CASCADE` clauses or automatic timestamp updates.

### 2.1 Users

<a id="data-1"></a>

**DATA-1 — `users`** · [INFO-1](003-business-architecture.md#info-1) · Existing

| Column | PostgreSQL type | Key or constraint |
| --- | --- | --- |
| id | UUID | Primary key |
| phone_number | TEXT | Unique |
| user_type | user_type | Enum: superuser, staff, customer |
| state | user_state | Enum: unverified, active, inactive, barred, deleted |
| profile_id | UUID | Unique FK → user_profiles.id |
| preferences_id | UUID | Unique FK → user_preferences.id |
| created_at | TIMESTAMPTZ | Required |
| updated_at | TIMESTAMPTZ | Required |

### 2.2 Profiles

<a id="data-2"></a>

**DATA-2 — `user_profiles`** · [INFO-1.1](003-business-architecture.md#info-1.1) · Existing

| Column | PostgreSQL type | Key or constraint |
| --- | --- | --- |
| id | UUID | Primary key |
| name | TEXT | Required |
| updated_at | TIMESTAMPTZ | Required |

### 2.3 Preferences

<a id="data-3"></a>

**DATA-3 — `user_preferences`** · [INFO-1.2](003-business-architecture.md#info-1.2) · Existing

| Column | PostgreSQL type | Key or constraint |
| --- | --- | --- |
| id | UUID | Primary key |
| allow_notifications | BOOLEAN | No default |
| updated_at | TIMESTAMPTZ | Required |

### 2.4 Contacts

<a id="data-4"></a>

**DATA-4 — `user_contacts`** · [INFO-1.3](003-business-architecture.md#info-1.3) · Existing

| Column | PostgreSQL type | Key or constraint |
| --- | --- | --- |
| id | UUID | Primary key |
| user_contact_type | user_contact_type | Enum: phonenumber, emailaddress |
| value | TEXT | No uniqueness constraint |
| states | user_contact_state | Single enum: unverified, active, disabled |
| user_id | UUID | FK → users.id |
| created_at | TIMESTAMPTZ | Required |
| updated_at | TIMESTAMPTZ | Required |

Index: `user_contacts_user_id_idx(user_id)`. Rust maps `user_contact_type` to
`contact_type()` and the singular state in `states` to `state()`.
[Read repositories](../src/users/repositories/mod.rs) decode `UserData` / `ContactData`
and validate lifecycle state. Profiles and preferences map directly; their SQL
foreign keys allow unreferenced secondary rows. See
[the repository contract](../ARCHITECTURE.md#repository-state-contract).

## 3 Proposed schema changes

### 3.1 User additions

- `users.deleted_at TIMESTAMPTZ NULL`: deletion timestamp; index for cleanup selection.
- `user_preferences.communication_channel TEXT NOT NULL DEFAULT 'whatsapp'`: proposed
  channel representation; supported non-default values still need definition.
- Add phone-format and lowercase-email validation consistent with
  [REQ-2.1.1](002-detailed-requirements.md#req-2.1.1) and
  [REQ-2.2.5](002-detailed-requirements.md#req-2.2.5). Existing phone uniqueness already
  prevents duplicate stored phone values; contact/email uniqueness is undecided.
- Credential storage, existing-row backfills, and profile/preference cleanup remain
  [OPEN-004-1](#open-004-1). Do not store plaintext passwords.

### 3.2 Organisations

<a id="data-5"></a>

**DATA-5 — `organisations`** · [INFO-2](003-business-architecture.md#info-2) · Proposed

| Column | Proposed type | Key or constraint |
| --- | --- | --- |
| id | UUID | Primary key |
| name | TEXT | NOT NULL; display name |
| name_key | TEXT | NOT NULL UNIQUE; case-insensitive, trimmed, collapsed-whitespace comparison |
| superuser_id | UUID | NOT NULL FK → users.id; not unique across organisations |
| state | Organisation state | NOT NULL; stored enum spelling pending |
| deleted_at | TIMESTAMPTZ | Nullable; set for deleted state |

Index `superuser_id` for ownership/count lookups and `deleted_at` for cleanup.
Maintain `name_key` consistently with `name`; select database expression/collation
before migration. Membership needs an organisation/user association with a unique
pair, not ownership of user rows. Subscription storage and its links remain pending.
Sources: [REQ-1.1.1](002-detailed-requirements.md#req-1.1.1),
[REQ-1.1.3](002-detailed-requirements.md#req-1.1.3),
[REQ-1.5.2](002-detailed-requirements.md#req-1.5.2).

### 3.3 Verification codes

<a id="data-9"></a>

**DATA-9 — `verification_codes`** · [INFO-3](003-business-architecture.md#info-3) · Proposed

| Column | Proposed type | Key or constraint |
| --- | --- | --- |
| id | UUID | Primary key; distinct from the five-digit secret |
| purpose | TEXT | NOT NULL; service-supplied value |
| subject_key | TEXT | NOT NULL; verification binding, encoding pending |
| proof_verifier | BYTEA | NOT NULL; protected proof representation, algorithm pending |
| state | Verification code state | NOT NULL; ready, used, expired, invalidated |
| generated_at | TIMESTAMPTZ | NOT NULL |
| consumed_at | TIMESTAMPTZ | Nullable; present for used state |
| invalidated_at | TIMESTAMPTZ | Nullable; present for resend-invalidated state |

- **Indexes:** `(purpose, subject_key)` for verification lookup; `generated_at` for cleanup.
  Candidate partial unique index on `(purpose, subject_key) WHERE state = 'ready'`;
  stale ready rows must be expired/invalidated transactionally before replacement.
- **Checks:** Used state requires `consumed_at`; invalidated state requires
  `invalidated_at`; timestamps must not precede generation. Final subject representation
  must preserve user/contact references; a generic text key supplies no foreign key.
- **Derived deadlines:** Expiry = `generated_at + 5 minutes`; cleanup =
  `generated_at + 24 hours`. Do not rely on a stored ready label for time validity.
- **Storage protection:** No plaintext proof in general reads, logs, or control history.
  Choose verifier construction and key handling before implementing `proof_verifier`.
- **Sources:** [Code requirements](002-detailed-requirements.md#req-3.1.1),
  [replacement](002-detailed-requirements.md#req-2.2.4), and proposed
  [ADR-006](decisions/006-verification-code-lifecycle-and-controls.md).

### 3.4 Verification generation controls

<a id="data-11"></a>

**DATA-11 — `verification_generation_controls` and `verification_generations`** ·
[INFO-5](003-business-architecture.md#info-5) · Proposed

| Control column | Proposed type | Key or constraint |
| --- | --- | --- |
| purpose | TEXT | Primary key; no user/contact key component |
| block_started_at | TIMESTAMPTZ | Nullable; latest block start |
| previous_block_started_at | TIMESTAMPTZ | Nullable; preceding block for escalation |
| blocked_until | TIMESTAMPTZ | Nullable; latest block deadline |

| Generation-history column | Proposed type | Key or constraint |
| --- | --- | --- |
| id | UUID | Primary key; identifies a successful generation |
| purpose | TEXT | NOT NULL FK → verification_generation_controls.purpose |
| generated_at | TIMESTAMPTZ | NOT NULL |

Index history on `(purpose, generated_at)`. The block start and deadline must be set
or absent together; a set deadline must follow its start. History has no foreign key
to deletable code rows and contains no proof material. Code generation, replacement,
history insertion, and block updates must commit consistently under a lock or
conditional update keyed by purpose. These tables are proposed storage for
[REQ-3.1.2](002-detailed-requirements.md#req-3.1.2); window and subsequent-block rules
must be settled before finalizing history pruning or escalation queries.

### 3.5 Deferred mappings

These stable DATA identifiers have no selected physical tables yet.

<a id="data-6"></a>

- **DATA-6 — Access constraints:** [INFO-2.1](003-business-architecture.md#info-2.1);
  organisation reference and policy representation pending.

<a id="data-7"></a>

- **DATA-7 — Organisation limits:** [INFO-2.2](003-business-architecture.md#info-2.2);
  subscription/entitlement storage pending. Defaults belong to
  [the subscription requirements](002-detailed-requirements.md#open-002-4).

<a id="data-8"></a>

- **DATA-8 — Membership invitations:** [INFO-2.3](003-business-architecture.md#info-2.3);
  reconcile with DATA-10 before selecting storage.

<a id="data-10"></a>

- **DATA-10 — Invitations:** [INFO-4](003-business-architecture.md#info-4);
  duplicate-concept decision is [OPEN-003-1](003-business-architecture.md#open-003-1).

## 4 Integrity and retention

- User deletion: select `deleted_at + 30 days` for cleanup; dependent-row deletion
  requires the account cleanup policy.
- Organisation deletion: remove all owned assets after 30 days; user references must
  never cascade into deleting users. Disabled organisations have no deletion timer.
- Codes: remove records by generation plus 24 hours. Preserve control/history rows
  while required by an active block or counting/escalation window.
- Atomic updates must protect sole-superuser replacement, code consumption/resend,
  and generation counts. Cleanup must not delete a successfully reactivated organisation.
  See [application operations](005-application-architecture.md#36-verification-persistence-operations)
  for verification sequencing and the [root contract](../ARCHITECTURE.md#repository-state-contract)
  for checked repository returns.

## 5 Migration and implementation gaps

Only the four tables in section 2 exist in the migration. Verification
[traits](../src/v_codes/traits.rs) declare string/boolean operations without purpose
or persistence; [models.rs](../src/v_codes/models.rs) is empty.

Add new migrations for proposed tables, fields, constraints, and indexes. Plan
backfills and check existing rows before introducing validation or uniqueness
constraints. Do not rewrite a migration applied to shared environments. Coordinate
SQL enum/column mappings and Rust consumers; validate on isolated migrated databases.
No migration or source-code change is included in this documentation update.

## 6 Remaining storage decisions

<a id="open-004-1"></a>

- **OPEN-004-1 — User schema:** Credential storage, email uniqueness, timestamp authority,
  channel representation/backfill, orphan profile/preference cleanup, and reconciliation
  of global `user_type` with organisation superuser references. Remaining business rules:
  [OPEN-002-2](002-detailed-requirements.md#open-002-2).

<a id="open-004-2"></a>

- **OPEN-004-2 — New schemas:** Finalize organisation state/name comparison, memberships,
  subscription/invitation tables, code subject references, proof protection, and control
  history queries. Use proposed [ADR-006](decisions/006-verification-code-lifecycle-and-controls.md)
  and remaining [code policy](002-detailed-requirements.md#open-002-3).

<a id="open-004-3"></a>

- **OPEN-004-3 — Storage operations:** Cleanup scheduling, control-history pruning,
  backup/recovery and retention, and cleanup across core/telemetry stores.

## 7 Legacy mapping

The original User aggregate records retain their identifiers: User → [DATA-1](#data-1),
UserProfile → [DATA-2](#data-2), UserPreferences → [DATA-3](#data-3), and
UserContact → [DATA-4](#data-4). All original columns remain documented in section 2.
