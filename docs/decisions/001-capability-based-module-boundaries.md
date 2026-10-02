# ADR 001 — Capability-based module boundaries

**Date:** 2026-10-02 (record date; original decision date unknown)  
**Status:** Accepted

## 1 Context

The project needs boundaries that keep related business behavior cohesive while
allowing requirements and capabilities to evolve. A capability map describes
business abilities; it does not prescribe a one-to-one mapping to code services.

## 2 Decision

Use business capabilities and domain ownership to inform module boundaries.
Group related domain behavior and data within cohesive modules. Do not assume
each capability requires its own service, module, or deployment unit.

## 3 Rationale

Capability-based boundaries keep implementation aligned with business meaning
while leaving room to combine closely related behavior and separate behavior
when ownership or change needs justify it.

## 4 Alternatives considered

- **One module per capability:** rejected as a default because it would imply a
  service boundary for every capability without evidence that such separation is
  useful.
- **Organizational or technical-layer boundaries alone:** not selected as the
  primary guide because they can split behavior that belongs to one domain.

## 5 Consequences

- **Benefits:** modules can express cohesive business responsibilities and have
  explicit ownership.
- **Costs and limitations:** boundaries require review as requirements change;
  the capability map alone does not determine every code boundary.

## 6 Related decisions

- [ADR-002 — Domain events for cross-module effects](002-domain-events-for-cross-module-effects.md)
- [ADR-004 — Typed user state machine](004-typed-user-state-machine.md)
- [Application architecture](../005-application-architecture.md)
