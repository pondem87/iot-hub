# ADR 003 — Usage limits owned by service

**Date:** 2026-10-02 (record date; original decision date unknown)  
**Status:** Proposed

## 1 Context

Requirements describe organisation limits in relation to subscription tiers, but
do not fully specify how those limits are allocated across services or where
enforcement responsibility belongs. This is a design proposal, not an approved
business rule.

## 2 Decision

Propose that the service responsible for an operation owns enforcement of the
usage limit relevant to that operation. Subscription entitlements may inform the
limit, but this proposal does not define tier values, counting rules, exceptions,
or a complete subscription policy.

## 3 Rationale

The service performing an operation has the context to apply its own domain
constraints and can report an outcome in its domain language. Keeping that
responsibility near the operation avoids a generic limit mechanism becoming the
owner of unrelated business rules.

## 4 Alternatives considered

- **Central shared limit service owns all enforcement:** may centralize policy,
  but risks coupling unrelated capabilities and is not established by current
  requirements.
- **Each interface enforces limits independently:** risks inconsistent outcomes
  across callers and does not ensure enforcement for every entry point.

## 5 Consequences

- **Benefits:** proposed ownership follows the service that performs the
  constrained operation.
- **Costs and limitations:** requirements must resolve limit values, measurement
  periods, exceptions, and subscription semantics before implementation can claim
  approved business behavior.

## 6 Related decisions

- [Detailed requirements — organisation limits](../002-detailed-requirements.md#req-1.1.4)
- [Business architecture — organisation limit management](../003-business-architecture.md#cap-2.5)

## 7 Open questions

- Which operations consume each limit, and what counts as usage?
- How are limit values, periods, changes, and exceptions defined by subscription?
- What business outcome should occur when an operation reaches its limit?
