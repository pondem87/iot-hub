# ADR 007 — User permission provider and storage

**Date:** 2026-10-06
**Status:** Accepted

## 1 Context

The permission foundation represented grants but had no provider or repository.
Its synchronous provider trait could not express SQLx I/O or distinguish storage
failure from authorization denial. The user requested a persistent provider and
resolved the grant-combination and permission-management rules during implementation.

## 2 Decision

Keep shared representations and provider contracts in `permissions`. Implement
`UserPermissionsProviderService` in `users/perm/services/` with an injected
`UserPermissionsStore`; implement PostgreSQL operations in `users/perm/repositories/user_permissions_repository.rs`.
The existing `UserPermissionData` becomes the single SQLx row representation.
Validate decoded data before returning grants, including insert results before commit.

Make provider I/O methods asynchronous and return `PermissionProviderError` for
structured denial, invalid request, invalid stored data, unavailable storage, and
unexpected operational failure. Discovery remains synchronous. Management methods
receive `PermissionActor` separately from the recipient; callers authenticate the
user and establish its role association.

Combine matching user and role grants. Collection grants cover object requests,
while object grants apply only to that object. Require every requested attribute
to be covered and at least one matching grant, even for an empty attribute request.
Grant management requires grants on `user_permission`: collection create for
creation, collection read for listing, and object delete for revocation.
Creation checks all written fields; listing checks every returned field. Initial
administrative grants are provisioned outside the permission service.

Store grants in `user_permissions` using explicit PostgreSQL enums, UUIDs, and the
existing JSON-in-text attribute encoding. Separate principal kind from its UUID.
Polymorphic principal/resource references have no foreign keys; existence checks
and lifecycle cleanup are not supplied by this repository. Keep duplicate grants
independent and revoke by grant UUID. Authorized revocation is idempotent.

## 3 Consequences and limits

- Existing provider callers must await I/O methods, handle operational errors, and
  supply an actor for management. No other provider implementations existed.
- Apply migration `002_user_permissions.sql` before using the provider. It does not
  seed administrator grants or alter existing user tables.
- No cache is used. Reads observe persisted grants, but evaluation and subsequent
  mutation/business actions are not serialized with concurrent revocation. Stronger
  freshness or transaction guarantees require further design.
- Creation permits delegation wherever an actor has permission-record create
  access; it does not additionally require possession of the delegated grant.
- Authentication, role membership, organisation policy, and transport mappings
  remain outside this implementation. The provider is not wired into HTTP routes
  or existing user read services by this change.

## 4 Alternatives considered

- Blocking on asynchronous SQLx inside the old synchronous trait would risk runtime
  blocking and still leave storage failures unrepresentable.
- Reporting storage failures as authorization denial or an empty grant list would
  hide operational failures and violate component error boundaries.
- Allowing callers to authorize all grant management was rejected in favor of
  checking grants on permission records themselves, as directed by the user.

## 5 Related decisions

- [ADR-001](001-capability-based-module-boundaries.md)
- [ADR-005](005-service-contract-entry-points.md)
- [Permissions architecture](../005-application-architecture.md#app-10)
