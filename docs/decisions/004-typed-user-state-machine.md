# ADR 004 — Typed user state machine

**Date:** 2026-10-02 (record date; original decision date unknown)  
**Status:** Accepted

## 1 Context

Users have lifecycle states with state-dependent operations. A runtime-only state
field allows invalid operations to be expressed and requires repeated checks.
Persistence deserialization also means data must be checked before it is treated
as a trusted domain object.

## 2 Decision

Represent user lifecycle states with Rust typestate. Stateful user objects use a
state type parameter and `PhantomData`; constructors and transition methods
control which typed states can be created. Repository operations validate the
persisted state after deserialization and return the validated typestate in their
return type. If a requested row exists in an unexpected state, return a typed
repository error rather than `None`.

## 3 Rationale

Typestate makes valid operations visible in method signatures and prevents many
invalid transitions at compile time. Repository validation preserves that
guarantee when data crosses the persistence boundary.

## 4 Alternatives considered

- **A runtime state field with checks at every call site:** not selected as the
  primary model because callers could still express invalid operations and checks
  could be omitted.
- **Trust persisted state without validation:** rejected because deserialized
  values are not proof that the row matches the requested domain state.

## 5 Consequences

- **Benefits:** valid state transitions are constrained by types, and unexpected
  persisted states are distinguishable from ordinary absence.
- **Costs and limitations:** conversion at persistence boundaries must validate
  state, and typestate does not by itself prevent stale writes or concurrent
  transitions.

## 6 Related decisions

- [Data architecture — user state](../004-data-architecture.md#data-1)
- [Application architecture — user contracts](../005-application-architecture.md#app-2)
- [Repository state contract](../../ARCHITECTURE.md#repository-state-contract)
