# ADR 002 — Domain events for cross-module effects

**Date:** 2026-10-02 (record date; original decision date unknown)  
**Status:** Accepted

## 1 Context

One business operation may produce effects owned by another module. Directly
changing another domain's state couples its implementation to the initiating
module and obscures ownership.

## 2 Decision

Prefer domain events for cross-module reactions to a completed business change.
Use a direct service contract when the initiating operation needs an immediate
result or query. Publish an event only after the originating state change has
succeeded, and document the actual delivery guarantees of its implementation.

## 3 Rationale

Events let the owning module react to a fact without transferring ownership of
its state. Direct calls remain appropriate when synchronous results are part of
the operation's contract.

## 4 Alternatives considered

- **Direct cross-module state mutation:** rejected because it bypasses the owning
  module's rules and couples internal representations.
- **Direct service calls for every reaction:** not selected as the default because
  it creates unnecessary synchronous dependencies for reactions to completed
  changes.

## 5 Consequences

- **Benefits:** the initiating module can announce outcomes while consumers keep
  ownership of their behavior and state.
- **Costs and limitations:** event delivery, failure, ordering, and durability
  must be designed and documented. An event abstraction alone provides no such
  guarantees.

## 6 Related decisions

- [ADR-001 — Capability-based module boundaries](001-capability-based-module-boundaries.md)
- [Application architecture](../005-application-architecture.md)
- [Architecture event and workflow boundaries](../../ARCHITECTURE.md#events-and-workflows)
