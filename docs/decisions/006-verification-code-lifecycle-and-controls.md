# ADR 006 — Verification code lifecycle and generation controls

**Date:** 2026-10-04
**Status:** Proposed

## 1 Context

The agreed requirements pair every verification code with a service-supplied purpose,
limit use to once within five minutes, invalidate the previous code on resend, and
remove all codes within 24 hours. Generation is throttled per purpose: five codes in
ten minutes trigger a one-hour block; a qualifying second block lasts 24 hours.
An active block can outlive the code whose generation triggered it.

The business and data maps previously listed codes and associated users/contacts
without a complete lifecycle or separate representation for retained block history.
The existing verification traits do not implement these rules. This proposal refines
the logical model; it does not choose physical tables, public APIs, or missing policy.

## 2 Proposed decision

- Model Verification Code as a verification-domain record with its own identity,
  required purpose, subject binding, generation time, protected proof representation,
  and lifecycle: ready, used, expired, or invalidated by resend.
- Treat purpose as a required value supplied by the caller. Treat subject binding as
  the association identifying the intended verification. The subject is not an
  additional throttle key: controls are shared by the supplied purpose.
- Represent Verification Code Generation Control separately, keyed by purpose, with
  generation and block history. Its allowed/blocked condition follows its deadline.
  It contains no code secrets and does not depend on any particular code surviving.
- Coordinate code generation/replacement, counting, and new blocks consistently.
  Consume a code atomically after current purpose, subject, proof, state, and time
  checks. Apply the existing typestate and repository-validation standards.
- Keep purpose vocabulary, subject encoding, window details, and subsequent-block
  policy open under the owning requirements. Do not introduce a table for each
  purpose or equate code cleanup with generation-control cleanup.

## 3 Rationale

Separating the records lets proof retention and block retention obey their different
rules. Explicit invalidation distinguishes resend from time expiry and consumption.
Purpose and subject checks prevent using a code outside its intended verification,
while per-purpose controls preserve the user's chosen throttle scope.

## 4 Alternatives considered

- **Store blocks only on code records:** code cleanup could erase a live block or
  escalation history, violating the agreed rules.
- **Keep code records while blocked:** would retain code material beyond its 24-hour
  cleanup deadline when a later block outlives it.
- **Throttle by user plus purpose:** changes the agreed per-purpose policy; it is not
  selected by this proposal.
- **Represent resend as expiry:** obscures why a still-young code stopped being usable.
  A separate invalidated condition makes the required lifecycle explicit.

## 5 Consequences

- **Benefits:** Independent retention, explicit lifecycle restrictions, and testable
  concurrency and time boundaries without prescribing a storage technology.
- **Costs and limitations:** Generation must coordinate code and control data. Physical
  persistence, secret protection, caller authorization, delivery guarantees, and the
  transaction boundary with the verified business action still require design.
- **Status:** This is a proposed representation of agreed business rules, not evidence
  of implementation or approval of the remaining policies.

## 6 Related decisions and records

- [ADR-001 — Capability-based module boundaries](001-capability-based-module-boundaries.md)
- [ADR-004 — Typed user state machine](004-typed-user-state-machine.md)
- [Verification requirements](../002-detailed-requirements.md#req-3.1.1)
- [Verification Code](../003-business-architecture.md#info-3)
- [Generation Control](../003-business-architecture.md#info-5)
- [Code data](../004-data-architecture.md#data-9)
- [Generation-control data](../004-data-architecture.md#data-11)
