# ADR 005 — Service contract entry points

**Date:** 2026-10-04
**Status:** Accepted

## 1 Context

The users domain mixed service and persistence contracts in `traits.rs`.
The requested organization separates outward-facing capabilities from persistence
contracts. The users capabilities to expose have not been established; existing
read implementations are not approval of an external service API.

## 2 Decision

Use a domain's `app.rs` for service traits offered to external consumers,
factory functions returning concrete implementations, types required by those
contracts, and factory tests. Keep local business implementations and read
contracts under `services/`, with their own `mod.rs` and `traits.rs`. Use normal
Rust module declarations: `app.rs` provides `users::app`, and `services/mod.rs`
provides `users::services`.

Place persistence traits in `repositories/traits.rs`, exported through the
repository module. Other internal traits belong with their owning components.
External consumers here means other domains and application adapters.

Keep `users/app.rs` as a documented placeholder until exported capabilities
are established. Do not export the existing read traits, implementations, or
factories from it. Factory dependencies and concrete return types remain undecided.

Retain the existing local read implementations as normal application code under
`services/`. Their constructors and local contracts remain available through
`users::services` in normal builds. Their use does not select the
future external API. Preserve dependency injection and behavioral tests.

## 3 Rationale and alternatives

- **Selected:** group contracts by responsibility and reserve an entry point for
  future service construction without prematurely choosing exported capabilities.
- **Mixed domain-wide traits:** rejected because persistence contracts obscure
  the service API presented to consumers.
- **Export existing reads immediately:** rejected because implementation evidence
  does not establish which capabilities the domain should offer externally.

## 4 Consequences

- **Compatibility:** store traits move to `users::repositories`. Remove
  `users::traits`; local read services and their contracts remain usable through
  `users::services` in normal builds.
- **Trade-off:** local service contracts and the future external API have separate
  owners; establishing an external capability requires an explicit contract in
  `app.rs` rather than automatically exposing every local service.
- **Visibility:** moving internally used contracts does not make them private;
  repository traits remain accessible to alternative adapters and integration tests.
- **Limits:** this change does not alter authorization, lifecycle validation,
  error outcomes, or the database schema. Other domains migrate separately.

## 5 Related decisions

- [ADR-001 — Capability-based module boundaries](001-capability-based-module-boundaries.md)
- [Service boundaries](../../ARCHITECTURE.md#service-boundaries)
- [Service contracts](../../STYLE_GUIDE.md#service-contracts)
