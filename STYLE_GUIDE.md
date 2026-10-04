# Style guide

This guide defines the target coding conventions for IoT Hub. Follow it for new
and modified code. The repository is still being developed: existing code does
not yet comply with every rule. See [CONTRIBUTING.md](CONTRIBUTING.md) for workflow,
validation, and known gaps.

## File organization

Follow [ARCHITECTURE.md](ARCHITECTURE.md) for component responsibilities, service
boundaries, and repository contracts. Organize code around domains such as
`users`, `v_codes`, and `events`.

### One service or repository per file

Each service and each repository implementation belongs in its own descriptively
named file. When a domain has multiple services or repositories, use separate
`services/` and `repositories/` directories for those groups. For example:

```text
users/
    app.rs
    services/
        mod.rs
        traits.rs
        user_account_service.rs
        user_profile_service.rs
        user_preferences_service.rs
    repositories/
        mod.rs
        traits.rs
        user_repository.rs
        user_profile_repository.rs
        user_preferences_repository.rs
        user_contact_repository.rs
    models.rs
```

Keep the external entry point `app.rs` separate from local implementations under
`services/`. Declare `app` and `services` normally in the domain's `mod.rs`;
`app.rs` and `services/mod.rs` resolve without custom module paths. Keep local
read contracts in `services/traits.rs` and repository contracts in
`repositories/traits.rs`.
Keep repository declarations and exports in `repositories/mod.rs`. Implementation
files may contain private supporting types, constructors, trait implementations,
and tests. Do not combine multiple service or repository implementations in one
file, or place a repository implementation in its service's file. Keep service
implementations under `services/` even when there is only one, reserving
`app.rs` for the contracts and factories described below. A domain with one
repository may retain `repositories.rs` until a directory is needed. This rule
does not require one model, event, or trait per file. Each file containing behavior
must also follow the test-module requirements below.

### Service contracts

As required by the [service boundaries](ARCHITECTURE.md#service-boundaries), every
publicly accessible service must implement a trait covering its business
operations. Implementing only a utility trait such as `Clone` does not satisfy
this rule.

Put service contracts offered to consumers outside the domain in its `app.rs`.
Reserve this module for those traits, factory functions returning concrete service
implementations, any types required by those contracts, and factory tests.
Here, external consumers means other domains or application adapters, not only
remote systems. Add exports and factories only after the domain's external
capabilities have been established. Until then, keep `app.rs` as a documented
placeholder; existing implementation code does not establish an approved external
API. Local services remain normal application code with their own contracts and
tests, independently of the external entry point.

Place persistence contracts such as `UserStore` in `repositories/traits.rs` and
re-export them through the repository module. Other internal contracts belong
with their owning component; do not collect unrelated contracts in a domain-wide
`traits.rs`. Use the visibility required by consumers and test adapters.

Factories accept existing dependencies and wire concrete implementations without
opening hidden connections. Select their inputs and concrete return types when
the exported capabilities are established, rather than inferring them from
existing implementations. Retain dependency-injecting constructors for unit tests
and alternative adapters. Constructors and private
helpers may remain inherent methods. Keep business operations on the trait rather
than adding a parallel public inherent API. Consumers should depend on the contract
where practical; choose generics or trait objects according to the interface's
needs. Do not assume every async trait supports dynamic dispatch.

For example, this self-contained illustration shows a small preference service;
it is a target pattern, not the current persistence-backed implementation:

```rust
/// Answers whether the user's preferences permit notifications.
pub trait NotificationPreferences {
    /// Returns the user's notification opt-in setting.
    fn notifications_allowed(&self) -> bool;
}

/// Provides access to a user's notification preference.
pub struct UserPreferencesService {
    allow_notifications: bool,
}

impl UserPreferencesService {
    /// Creates a service for the supplied notification preference.
    pub fn new(allow_notifications: bool) -> Self {
        Self { allow_notifications }
    }
}

impl NotificationPreferences for UserPreferencesService {
    fn notifications_allowed(&self) -> bool {
        self.allow_notifications
    }
}
```

In the project, place the trait and service in their respective modules and add
the tests shown below to the service file.

## Rust source spacing

Use explicit blank lines to make logical boundaries visible in handwritten and
agent-generated Rust code. Apply these rules when creating or editing code:

- Put one blank line between adjacent structs, enums, traits, functions, and
  `impl` blocks, including between a type definition and its implementation.
- Put one blank line between methods in an `impl` or trait, and between tests
  and test helpers. Separate the test module from the preceding production code.
- Put one blank line after module documentation and after the final import group
  before declarations or other code. Keep related imports together; do not add a
  blank line between every `use`, `mod`, or re-export declaration.
- Keep Rustdoc comments and attributes attached to the item they describe. Place
  the separating blank line before the documentation or attributes, never between
  them and the item.
- Inside functions, separate meaningful steps with one blank line, such as input
  validation, data retrieval, and result construction. Keep closely related
  statements together; do not separate every statement or field.
- Avoid multiple consecutive blank lines and empty padding immediately inside
  opening or closing braces.

For example, this excerpt separates a type, its implementation, and its methods:

```rust
/// A recorded numeric reading.
pub struct Reading {
    value: u64,
}

impl Reading {
    /// Creates a reading with the supplied value.
    pub fn new(value: u64) -> Self {
        Self { value }
    }

    /// Returns the recorded value.
    pub fn value(&self) -> u64 {
        self.value
    }
}
```

Authors and coding agents must insert this spacing before running `cargo fmt`.
Format on save and `cargo fmt` do not insert all of these logical separators;
a passing `cargo fmt --check` alone does not establish compliance. Review source
spacing alongside the [required validation](CONTRIBUTING.md#validate-and-report-results).

## Rust documentation

Use Rustdoc comments (`///`), rather than ordinary comments, on **every struct and
trait**, regardless of visibility. This includes state markers, private helpers,
test fixtures, and error structs. Describe the purpose and relevant invariants;
do not merely repeat the name.

Document public methods, functions, enums, and other public API items. Trait
methods must explain their contract, including inputs, results, side effects, and
state requirements where relevant. An implementation can rely on the trait's
documentation, adding implementation-specific constraints when needed.

Use `# Errors`, `# Panics`, and examples when applicable. Document intentional
panics; return errors for expected failures. Explain lifecycle meaning on each
state marker. Use `//!` for module documentation and `//` for local explanations
of why an implementation takes a particular approach.

## State-dependent objects

Domain objects with lifecycle states must use a generic state parameter and a
private `std::marker::PhantomData<State>` field. Define documented marker types
and put state-dependent operations in specialized `impl Object<SpecificState>`
blocks. Keep only operations valid in every state in a generic implementation.

- Keep state-bearing fields private so callers cannot bypass transitions.
- Transitions consume the old value and return the value with its new state type.
  Use `Result` for transitions that can fail, and document ownership on failure.
- Restrict construction to valid states. Do not expose arbitrary generic
  constructors or unchecked conversions between state parameters.
- Validate authorization and business preconditions as well as type constraints.
  Typestate alone does not establish permission or database freshness.
- Do not derive `Clone` automatically for lifecycle objects when stale copies
  would undermine transition rules. Keep persisted transitions consistent with
  the runtime and type-level state, using transactions or conditional updates
  where required.

The following example illustrates ownership and method availability. It does not
define the application's complete user lifecycle or authorization rules:

```rust
use std::marker::PhantomData;

/// Marks an account whose active-only operations are available.
pub struct Active;

/// Marks an account whose active-only operations are unavailable.
pub struct Inactive;

/// An account whose lifecycle state controls its available operations.
pub struct Account<State> {
    id: u64,
    state_marker: PhantomData<State>,
}

impl Account<Active> {
    /// Consumes the active account and returns an inactive account.
    pub fn deactivate(self) -> Account<Inactive> {
        Account {
            id: self.id,
            state_marker: PhantomData,
        }
    }
}
```

An `Account<Inactive>` has no `deactivate` method, and the consumed active value
cannot be reused. Tests for real implementations must verify both properties.

### Repository return types and state validation

Follow the [repository state contract](ARCHITECTURE.md#repository-state-contract):
repository functions must verify state after deserialization and return validated
typestate objects in their return types. A lookup expecting an active user must
return `User<ActiveUser>` within its result, not leave state checking to its caller.
For a requested row that exists in a different state, return `Err` with a state
mismatch error; never return `None` or panic. Reserve `None` for a genuinely absent
row when the lookup's contract allows absence. Document these outcomes in the
repository method's Rustdoc, including its `# Errors` section.

Use one internal `<Model>Data` struct with `#[derive(sqlx::FromRow)]` to decode
persisted fields for a lifecycle model. Retain the stored runtime state in this
struct, then consume it through checked construction of the trusted typestate.
Do not introduce a duplicate `<Model>Row` struct merely to copy fields into
`<Model>Data`. For example, user reads follow `SQL row → UserData → User<State>`;
repository results expose only the validated object or an enum of validated states.
Keep data structs and checked constructors at the narrowest useful visibility.

Keep column and enum mappings explicit on the data fields and runtime enums,
including PostgreSQL type names and stored spellings. `FromRow` decodes data; it
does not establish lifecycle validity. Do not derive `FromRow` or unchecked
serialization-based construction on trusted typestate models. Marker fields belong
only to validated objects and must stay out of persisted and serialized data.

Plain models without lifecycle or other checked-construction invariants, such as
`UserProfile` and `UserPreferences`, may derive `FromRow` directly when their shape
matches the query. Do not add a separate data struct solely to copy identical
fields. SQL execution and technical error translation remain in repositories;
checked model construction returns domain errors for repositories to translate.

Use typestate for lifecycle concepts such as users, contacts, verification codes,
and invitations as their behavior is implemented. User roles and contact kinds
are classifications, not lifecycle states. Profiles, preferences, and `AppState`
do not require typestate simply because they contain mutable values or have
“state” in their name.

## Error handling

Errors must speak the language of the component exposing them. Follow the
[error boundaries](ARCHITECTURE.md#error-boundaries) when translating failures
between repositories, services, and transport adapters.

- Use `Result<T, E>` for recoverable failures. Reserve `Option` for normal absence,
  never for hiding a failure or a repository state mismatch.
- Define small, typed errors near their owning domain concept or operation. Use
  names such as `UserError`, `ActivateUserError`, and `UserRepositoryError`; avoid
  global enums that accumulate unrelated failures. Variants and structured fields
  must let callers match outcomes without parsing human-readable messages.
- Use `thiserror` for explicit programmatic error contracts. Allow `anyhow` at
  executable edges such as startup, CLI commands, migrations, and maintenance
  utilities, where callers do not need a typed contract. Do not use it as the
  primary error type for domain operations or service and repository interfaces.
  Neither crate is currently declared in the manifest; add the relevant dependency
  when implementing these conventions.
- Preserve original technical causes internally where useful for diagnostics.
  Add context describing the attempted operation, such as “failed to persist
  activated user,” rather than repeating “database error.” Match structured
  library error codes or variants when translating failures, not message text.
- Log operational failures once at the boundary with enough context to explain
  the failed operation. Domain code must not log merely because it returns an
  error. Expected business validation failures are normally outcomes, not system
  incidents; avoid logging and propagating the same failure at every layer.
- Return safe messages to external consumers. Do not expose SQL, database names,
  credentials, internal paths or service addresses, stack traces, tokens, or
  sensitive user data. Keep technical diagnostics in controlled logs and telemetry,
  redacting secrets and sensitive values there too.
- Do not use `panic!`, `unwrap`, or `expect` for failures caused by runtime input,
  user actions, infrastructure, or external services. Reserve them for programmer
  defects or invariants whose validity is established; document the invariant.
  Tests may use `unwrap` and `expect` when failure should fail the test.

## Tests and test plans

Every Rust source file containing behavior must have a colocated
`#[cfg(test)] mod tests`, normally at the end of the file. Behavior includes
constructors, conversions, trait default methods, routing, and application
composition. Files containing only declarations, re-exports, or data definitions
without behavior are exempt; do not add empty modules just to satisfy a count.
The same distinction applies to binary entrypoints and build scripts: extract
testable behavior when necessary, and avoid tests that start production services.

The **first content inside each test module** must be `//!` documentation naming
the subject and a `# Test plan` section. List every test by its actual name and
describe the behavior it verifies. Update the plan whenever tests change. For a
parameterized test, also describe the cases it covers.

For the preference service above:

```rust
#[cfg(test)]
mod tests {
    //! Tests notification preference access through the service contract.
    //!
    //! # Test plan
    //! - `allows_notifications_when_opted_in`: preserves an enabled preference.
    //! - `rejects_notifications_when_opted_out`: preserves a disabled preference.

    use super::*;

    #[test]
    fn allows_notifications_when_opted_in() {
        let service = UserPreferencesService::new(true);
        assert!(NotificationPreferences::notifications_allowed(&service));
    }

    #[test]
    fn rejects_notifications_when_opted_out() {
        let service = UserPreferencesService::new(false);
        assert!(!NotificationPreferences::notifications_allowed(&service));
    }
}
```

Test observable behavior: successful operations, error paths, boundaries, and
state transitions relevant to the file. Use `#[test]` for synchronous behavior and
`#[tokio::test]` when a runtime is needed. Keep unit tests deterministic and avoid
live external dependencies; inject dependencies and control time where relevant.

Database integration tests must use an isolated test database with migrations
applied and clean up their own data. Document required setup. They supplement the
file's unit tests rather than replacing them.

Test typestate restrictions with Rustdoc `compile_fail` examples or a compile-fail
test harness. Include a valid companion case so a broken import cannot masquerade
as enforcement. Cover prohibited operations and reuse after consuming transitions,
as well as runtime rejection of mismatched persisted states. Repository tests
must distinguish an absent row from an existing row in the wrong state, verify
the returned typestate for a matching row, and cover deserialization and database
failures. Describe these additional checks in the relevant module's test plan and
identify their location.

Test error translation at the relevant boundaries: repository failures become
structured persistence outcomes, services distinguish required-object absence
from operational failure, and HTTP responses preserve the intended outcome while
omitting technical diagnostics and sensitive data. Assert variants and fields for
programmatic contracts rather than matching formatted error strings.

## Markdown source readability

Write Markdown for people reading and editing the source as well as the rendered
document. Requirements, business architecture, and detailed use cases must use
numbered sections, short paragraphs, and labeled lists rather than Markdown or
HTML tables. This includes their indexes, cross-mappings, open decisions, legacy
references, and templates. Present each record separately, with one field per
list item, so changing a rule or outcome produces a small, readable diff.

- Preserve required heading numbers, stable record IDs, and explicit anchors.
  Put a record's anchor on its own line before its title; use a bold record label
  inside a numbered section when another heading level would add noise.
- Keep field order consistent. Separate records with a blank line. Use numbered
  lists for flow steps and labeled bullets for actor actions, outcomes, and rules.
- Wrap prose and list continuations at roughly 80–100 characters, indenting
  continuations under the list item. Keep links, inline code, and identifiers
  intact even when they exceed that width. Avoid unrelated reflow when editing.
- Use short, descriptive link labels. Do not align text with spaces or embed
  multiple independently editable fields on one long line.
- Reserve tables in other documents for compact, stable comparisons or technical
  lookup data. Prefer lists when cells contain sentences, rules, or changing
  requirements. Do not use a table merely to make rendered Markdown look tidy.

Follow the [documentation guide](docs/000-docs-guide.md) and
[scoped agent instructions](docs/AGENTS.md) for the record formats and templates.

## Formatting, types, and operational behavior

- Use Rust 2024 and standard `rustfmt` formatting. Use `snake_case` for modules,
  files, functions, and fields; `UpperCamelCase` for types and traits; and
  `SCREAMING_SNAKE_CASE` for constants.
- Group imports clearly: standard library, external crates, then crate-local
  modules. Prefer explicit imports in production code; `use super::*` is suitable
  for small test modules. Use the narrowest useful visibility.
- Follow the stack described in [ARCHITECTURE.md](ARCHITECTURE.md#technology-and-composition).
  Use `sqlx::types::Uuid` with the current dependencies and `DateTime<Utc>` for
  timestamps; introduce a direct dependency only when needed and declared.
- Keep Rust models, SQL types, enum values, and serialized values aligned through
  explicit mappings. Do not assume automatic case conversion matches existing
  database spellings. Prefer typed identifiers internally over string conversion.
- Follow the [error-handling conventions](#error-handling) for fallible operations,
  error types, diagnostic context, logging, and panics.
- Avoid blocking work and synchronous lock guards across `.await`. Use shared
  ownership and synchronization only when needed; document background task error
  handling rather than silently losing failures.
- Keep event topics in named constants, following existing domain-prefixed names
  such as `users/user_created`. Preserve event schema compatibility or document
  changes with affected producers and consumers.
- Read deployment configuration from configuration sources rather than embedding
  credentials. Keep secrets and sensitive user data out of logs and errors.
