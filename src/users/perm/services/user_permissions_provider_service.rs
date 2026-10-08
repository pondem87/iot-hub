//! Grant evaluation and authorized permission management for the users domain.

use std::collections::{HashMap, HashSet};

use sqlx::types::Uuid;

use crate::permissions::{
    app::PermissionProvider,
    errors::{PermissionProviderError, UnauthorizedError},
    schemas::{AttrObject, DiscoveredPermission, PermissionActor, Principal, Scope, ScopeObject},
};
use crate::users::perm::{
    errors::UserPermissionsRepositoryError,
    models::{UserPermission, UsersAction, UsersAttribute, UsersResource},
    repositories::UserPermissionsStore,
};

/// Evaluates additive user and role grants through an injected store.
///
/// Callers authenticate users and resolve role membership. Matching grants are
/// combined by resource, action and target; collection grants cover objects.
/// Every requested attribute must be covered, and no matching grant means denial,
/// including requests with no attributes. Checks observe current repository reads
/// without caching; concurrent grant changes are not serialized with later actions.
///
/// Management is authorized on `UserPermission`: create checks collection access
/// to written fields, list checks collection access to every returned field, and
/// delete checks the permission object. Missing grants are revoked idempotently
/// after authorization. Initial administrative grants require external provisioning.
pub struct UserPermissionsProviderService<R> {
    repository: R,
}

impl<R: UserPermissionsStore> UserPermissionsProviderService<R> {
    /// Creates the service with existing storage and no hidden connections.
    pub fn new(repository: R) -> Self {
        Self { repository }
    }

    async fn check(
        &self,
        actor: PermissionActor,
        resource: UsersResource,
        object_id: Option<Uuid>,
        action: UsersAction,
        attributes: HashSet<UsersAttribute>,
    ) -> Result<(), PermissionProviderError> {
        validate_request(resource, action, &attributes)?;
        let mut grants = self
            .repository
            .get_permissions(Principal::User(actor.user_id))
            .await
            .map_err(provider_error)?;
        grants.extend(
            self.repository
                .get_permissions(Principal::Role(actor.role_id))
                .await
                .map_err(provider_error)?,
        );
        let mut covered = HashSet::new();
        let mut matched = false;
        let mut all = false;
        for grant in grants {
            // Reject malformed domain combinations even if another grant would allow access.
            if let AttrObject::Selected(ref attrs) = grant.scope.attributes {
                validate_request(scope_resource(&grant.scope.object), grant.action, attrs)
                    .map_err(|_| PermissionProviderError::InvalidData)?;
            } else {
                validate_request(
                    scope_resource(&grant.scope.object),
                    grant.action,
                    &HashSet::new(),
                )
                .map_err(|_| PermissionProviderError::InvalidData)?;
            }
            let matching_principal = match grant.principal {
                Principal::User(id) => id == actor.user_id,
                Principal::Role(id) => id == actor.role_id,
            };
            if !matching_principal
                || grant.action != action
                || scope_resource(&grant.scope.object) != resource
            {
                continue;
            }
            if let ScopeObject::ObjectId(_, id) = grant.scope.object
                && object_id != Some(id)
            {
                continue;
            }
            matched = true;
            match grant.scope.attributes {
                AttrObject::All => all = true,
                AttrObject::Selected(attrs) => covered.extend(attrs),
            }
        }

        if matched && (all || attributes.is_subset(&covered)) {
            Ok(())
        } else {
            Err(UnauthorizedError.into())
        }
    }

    async fn create(
        &self,
        actor: PermissionActor,
        principal: Principal,
        scope: Scope<UsersResource, UsersAttribute>,
        action: UsersAction,
    ) -> Result<(), PermissionProviderError> {
        let mut written = resource_attributes(UsersResource::UserPermission);
        written.remove(&UsersAttribute::Id);
        self.check(
            actor,
            UsersResource::UserPermission,
            None,
            UsersAction::Create,
            written,
        )
        .await?;
        let AttrObject::Selected(ref attributes) = scope.attributes else {
            return Err(PermissionProviderError::InvalidRequest);
        };
        validate_request(scope_resource(&scope.object), action, attributes)?;
        self.repository
            .create_permission(principal, scope, action)
            .await
            .map_err(provider_error)?;

        Ok(())
    }
}

impl<R: UserPermissionsStore> PermissionProvider<UsersResource, UsersAttribute, UsersAction>
    for UserPermissionsProviderService<R>
{
    async fn check_collection_perms(
        &self,
        user: Uuid,
        role: Uuid,
        resource: UsersResource,
        action: UsersAction,
        resource_attrs: HashSet<UsersAttribute>,
    ) -> Result<(), PermissionProviderError> {
        self.check(
            PermissionActor {
                user_id: user,
                role_id: role,
            },
            resource,
            None,
            action,
            resource_attrs,
        )
        .await
    }

    async fn check_object_perms(
        &self,
        user: Uuid,
        role: Uuid,
        resource: UsersResource,
        resource_id: Uuid,
        action: UsersAction,
        resource_attrs: HashSet<UsersAttribute>,
    ) -> Result<(), PermissionProviderError> {
        self.check(
            PermissionActor {
                user_id: user,
                role_id: role,
            },
            resource,
            Some(resource_id),
            action,
            resource_attrs,
        )
        .await
    }

    fn discover_perms() -> Vec<DiscoveredPermission<UsersResource, UsersAttribute, UsersAction>> {
        [
            UsersResource::User,
            UsersResource::UserContact,
            UsersResource::UserProfile,
            UsersResource::UserPreferences,
            UsersResource::UserPermission,
        ]
        .into_iter()
        .map(|resource| {
            let actions = [
                UsersAction::Create,
                UsersAction::Read,
                UsersAction::Update,
                UsersAction::Delete,
            ]
            .into_iter()
            .filter(|action| {
                !(resource == UsersResource::UserPermission && *action == UsersAction::Update)
            })
            .map(|action| (action, AttrObject::Selected(resource_attributes(resource))))
            .collect::<HashMap<_, _>>();
            DiscoveredPermission { resource, actions }
        })
        .collect()
    }

    async fn create_object_perm(
        &self,
        actor: PermissionActor,
        principal: Principal,
        resource: UsersResource,
        resource_id: Uuid,
        action: UsersAction,
        resource_attrs: HashSet<UsersAttribute>,
    ) -> Result<(), PermissionProviderError> {
        self.create(
            actor,
            principal,
            Scope {
                object: ScopeObject::ObjectId(resource, resource_id),
                attributes: AttrObject::Selected(resource_attrs),
            },
            action,
        )
        .await
    }

    async fn create_user_collection_perm(
        &self,
        actor: PermissionActor,
        principal: Uuid,
        resource: UsersResource,
        action: UsersAction,
        resource_attrs: HashSet<UsersAttribute>,
    ) -> Result<(), PermissionProviderError> {
        self.create(
            actor,
            Principal::User(principal),
            Scope {
                object: ScopeObject::Collection(resource),
                attributes: AttrObject::Selected(resource_attrs),
            },
            action,
        )
        .await
    }

    async fn create_role_object_perm(
        &self,
        actor: PermissionActor,
        principal: Uuid,
        resource: UsersResource,
        resource_id: Uuid,
        action: UsersAction,
        resource_attrs: HashSet<UsersAttribute>,
    ) -> Result<(), PermissionProviderError> {
        self.create_object_perm(
            actor,
            Principal::Role(principal),
            resource,
            resource_id,
            action,
            resource_attrs,
        )
        .await
    }

    async fn create_role_collection_perm(
        &self,
        actor: PermissionActor,
        principal: Uuid,
        resource: UsersResource,
        action: UsersAction,
        resource_attrs: HashSet<UsersAttribute>,
    ) -> Result<(), PermissionProviderError> {
        self.create(
            actor,
            Principal::Role(principal),
            Scope {
                object: ScopeObject::Collection(resource),
                attributes: AttrObject::Selected(resource_attrs),
            },
            action,
        )
        .await
    }

    async fn get_permissions(
        &self,
        actor: PermissionActor,
        principal: Principal,
    ) -> Result<Vec<UserPermission>, PermissionProviderError> {
        self.check(
            actor,
            UsersResource::UserPermission,
            None,
            UsersAction::Read,
            resource_attributes(UsersResource::UserPermission),
        )
        .await?;
        let permissions = self
            .repository
            .get_permissions(principal)
            .await
            .map_err(provider_error)?;
        for grant in &permissions {
            let attrs = match &grant.scope.attributes {
                AttrObject::All => HashSet::new(),
                AttrObject::Selected(attrs) => attrs.clone(),
            };
            validate_request(scope_resource(&grant.scope.object), grant.action, &attrs)
                .map_err(|_| PermissionProviderError::InvalidData)?;
        }

        Ok(permissions)
    }

    async fn revoke_permission(
        &self,
        actor: PermissionActor,
        perm_id: Uuid,
    ) -> Result<(), PermissionProviderError> {
        self.check(
            actor,
            UsersResource::UserPermission,
            Some(perm_id),
            UsersAction::Delete,
            HashSet::new(),
        )
        .await?;
        self.repository
            .delete_permission(perm_id)
            .await
            .map_err(provider_error)?;

        Ok(())
    }
}

fn resource_attributes(resource: UsersResource) -> HashSet<UsersAttribute> {
    use UsersAttribute::*;

    match resource {
        UsersResource::User => HashSet::from([UserType, UserState, Password]),
        UsersResource::UserContact => HashSet::from([ContactType, Value, State]),
        UsersResource::UserProfile => HashSet::from([Name]),
        UsersResource::UserPreferences => HashSet::from([AllowNotifications]),
        UsersResource::UserPermission => HashSet::from([
            Id,
            PrincipalType,
            PrincipalId,
            PermType,
            Resource,
            ResourceId,
            Action,
            Attributes,
        ]),
    }
}

fn validate_request(
    resource: UsersResource,
    action: UsersAction,
    attributes: &HashSet<UsersAttribute>,
) -> Result<(), PermissionProviderError> {
    if (resource == UsersResource::UserPermission && action == UsersAction::Update)
        || !attributes.is_subset(&resource_attributes(resource))
    {
        return Err(PermissionProviderError::InvalidRequest);
    }

    Ok(())
}

fn scope_resource(object: &ScopeObject<UsersResource>) -> UsersResource {
    match object {
        ScopeObject::Collection(resource) | ScopeObject::ObjectId(resource, _) => *resource,
    }
}

fn provider_error(error: UserPermissionsRepositoryError) -> PermissionProviderError {
    match error {
        UserPermissionsRepositoryError::Unavailable => PermissionProviderError::Unavailable,
        UserPermissionsRepositoryError::InvalidData => PermissionProviderError::InvalidData,
        UserPermissionsRepositoryError::Unexpected => PermissionProviderError::Unexpected,
    }
}

#[cfg(test)]
mod tests {
    //! Tests additive grant checks and authorization of permission management.
    //!
    //! # Test plan
    //! - `combines_user_and_role_attributes`: checks combined coverage and missing attributes.
    //! - `enforces_target_action_and_identity`: rejects different principals, actions,
    //!   resources, object IDs, and object grants used for collections; collections cover objects.
    //! - `empty_requests_still_require_a_grant`: distinguishes empty selections and absent grants.
    //! - `management_requires_permission_grants`: covers denied create/list/revoke without effects.
    //! - `management_requires_all_record_fields`: rejects partial create/read coverage.
    //! - `authorized_management_preserves_targets`: exercises all creation methods, list, and
    //!   object-scoped revocation while preserving recipients and target scope.
    //! - `invalid_requests_and_stored_grants_are_rejected`: checks attribute applicability
    //!   and unsupported permission updates without writing invalid grants.
    //! - `storage_failures_are_not_denials`: checks every storage error on reads and writes.
    //! - `password_requires_user_attribute_coverage`: checks password discovery, allow/deny,
    //!   and rejection on non-user resources.
    //! - `discovery_matches_supported_combinations`: verifies resources, actions and attributes.

    use std::sync::Mutex;

    use super::*;

    /// In-memory store with controllable read/write failures and observable writes.
    #[derive(Default)]
    struct Store {
        grants: Mutex<Vec<UserPermission>>,
        read_error: Option<UserPermissionsRepositoryError>,
        write_error: Option<UserPermissionsRepositoryError>,
    }

    fn copy_error(error: &UserPermissionsRepositoryError) -> UserPermissionsRepositoryError {
        match error {
            UserPermissionsRepositoryError::Unavailable => {
                UserPermissionsRepositoryError::Unavailable
            }
            UserPermissionsRepositoryError::InvalidData => {
                UserPermissionsRepositoryError::InvalidData
            }
            UserPermissionsRepositoryError::Unexpected => {
                UserPermissionsRepositoryError::Unexpected
            }
        }
    }

    fn copy_grant(grant: &UserPermission) -> UserPermission {
        UserPermission {
            perm_id: grant.perm_id,
            principal: match grant.principal {
                Principal::User(id) => Principal::User(id),
                Principal::Role(id) => Principal::Role(id),
            },
            scope: Scope {
                object: match grant.scope.object {
                    ScopeObject::Collection(r) => ScopeObject::Collection(r),
                    ScopeObject::ObjectId(r, id) => ScopeObject::ObjectId(r, id),
                },
                attributes: match &grant.scope.attributes {
                    AttrObject::All => AttrObject::All,
                    AttrObject::Selected(attrs) => AttrObject::Selected(attrs.clone()),
                },
            },
            action: grant.action,
        }
    }

    impl UserPermissionsStore for Store {
        async fn create_permission(
            &self,
            principal: Principal,
            scope: Scope<UsersResource, UsersAttribute>,
            action: UsersAction,
        ) -> Result<UserPermission, UserPermissionsRepositoryError> {
            if let Some(error) = &self.write_error {
                return Err(copy_error(error));
            }
            let mut grants = self.grants.lock().unwrap();
            let grant = UserPermission {
                perm_id: Uuid::from_u128(grants.len() as u128 + 10),
                principal,
                scope,
                action,
            };
            grants.push(copy_grant(&grant));

            Ok(grant)
        }

        async fn get_permissions(
            &self,
            principal: Principal,
        ) -> Result<Vec<UserPermission>, UserPermissionsRepositoryError> {
            if let Some(error) = &self.read_error {
                return Err(copy_error(error));
            }

            Ok(self
                .grants
                .lock()
                .unwrap()
                .iter()
                .filter(|grant| match (&grant.principal, &principal) {
                    (Principal::User(a), Principal::User(b))
                    | (Principal::Role(a), Principal::Role(b)) => a == b,
                    _ => false,
                })
                .map(copy_grant)
                .collect())
        }

        async fn delete_permission(
            &self,
            id: Uuid,
        ) -> Result<bool, UserPermissionsRepositoryError> {
            if let Some(error) = &self.write_error {
                return Err(copy_error(error));
            }
            let mut grants = self.grants.lock().unwrap();
            let before = grants.len();
            grants.retain(|g| g.perm_id != id);

            Ok(grants.len() != before)
        }
    }

    fn actor() -> PermissionActor {
        PermissionActor {
            user_id: Uuid::from_u128(1),
            role_id: Uuid::from_u128(2),
        }
    }

    fn grant(
        principal: Principal,
        object: ScopeObject<UsersResource>,
        action: UsersAction,
        attributes: AttrObject<UsersAttribute>,
    ) -> UserPermission {
        UserPermission {
            perm_id: Uuid::from_u128(9),
            principal,
            scope: Scope { object, attributes },
            action,
        }
    }

    fn service(grants: Vec<UserPermission>) -> UserPermissionsProviderService<Store> {
        UserPermissionsProviderService::new(Store {
            grants: Mutex::new(grants),
            ..Store::default()
        })
    }

    fn admin_grants() -> Vec<UserPermission> {
        [UsersAction::Create, UsersAction::Read, UsersAction::Delete]
            .into_iter()
            .map(|action| {
                grant(
                    Principal::Role(actor().role_id),
                    ScopeObject::Collection(UsersResource::UserPermission),
                    action,
                    AttrObject::All,
                )
            })
            .collect()
    }

    #[tokio::test]
    async fn combines_user_and_role_attributes() {
        let service = service(vec![
            grant(
                Principal::User(actor().user_id),
                ScopeObject::Collection(UsersResource::UserContact),
                UsersAction::Read,
                AttrObject::Selected(HashSet::from([UsersAttribute::Value])),
            ),
            grant(
                Principal::Role(actor().role_id),
                ScopeObject::Collection(UsersResource::UserContact),
                UsersAction::Read,
                AttrObject::Selected(HashSet::from([UsersAttribute::State])),
            ),
        ]);
        assert_eq!(
            service
                .check_collection_perms(
                    actor().user_id,
                    actor().role_id,
                    UsersResource::UserContact,
                    UsersAction::Read,
                    HashSet::from([UsersAttribute::Value, UsersAttribute::State])
                )
                .await,
            Ok(())
        );
        assert_eq!(
            service
                .check_collection_perms(
                    actor().user_id,
                    actor().role_id,
                    UsersResource::UserContact,
                    UsersAction::Read,
                    HashSet::from([UsersAttribute::Value, UsersAttribute::ContactType])
                )
                .await,
            Err(UnauthorizedError.into())
        );
    }

    #[tokio::test]
    async fn enforces_target_action_and_identity() {
        let target = Uuid::from_u128(99);
        let service = service(vec![grant(
            Principal::User(actor().user_id),
            ScopeObject::ObjectId(UsersResource::UserProfile, target),
            UsersAction::Read,
            AttrObject::All,
        )]);
        assert_eq!(
            service
                .check_object_perms(
                    actor().user_id,
                    actor().role_id,
                    UsersResource::UserProfile,
                    target,
                    UsersAction::Read,
                    HashSet::from([UsersAttribute::Name])
                )
                .await,
            Ok(())
        );
        for (user, role, resource, id, action) in [
            (
                Uuid::nil(),
                actor().role_id,
                UsersResource::UserProfile,
                target,
                UsersAction::Read,
            ),
            (
                Uuid::nil(),
                actor().user_id,
                UsersResource::UserProfile,
                target,
                UsersAction::Read,
            ),
            (
                actor().user_id,
                actor().role_id,
                UsersResource::User,
                target,
                UsersAction::Read,
            ),
            (
                actor().user_id,
                actor().role_id,
                UsersResource::UserProfile,
                Uuid::nil(),
                UsersAction::Read,
            ),
            (
                actor().user_id,
                actor().role_id,
                UsersResource::UserProfile,
                target,
                UsersAction::Update,
            ),
        ] {
            assert_eq!(
                service
                    .check_object_perms(user, role, resource, id, action, HashSet::new())
                    .await,
                Err(UnauthorizedError.into())
            );
        }
        assert_eq!(
            service
                .check_collection_perms(
                    actor().user_id,
                    actor().role_id,
                    UsersResource::UserProfile,
                    UsersAction::Read,
                    HashSet::new()
                )
                .await,
            Err(UnauthorizedError.into())
        );
        let service = super::tests::service(vec![grant(
            Principal::Role(actor().role_id),
            ScopeObject::Collection(UsersResource::UserProfile),
            UsersAction::Read,
            AttrObject::All,
        )]);
        assert_eq!(
            service
                .check_object_perms(
                    actor().user_id,
                    actor().role_id,
                    UsersResource::UserProfile,
                    target,
                    UsersAction::Read,
                    HashSet::from([UsersAttribute::Name])
                )
                .await,
            Ok(())
        );
    }

    #[tokio::test]
    async fn empty_requests_still_require_a_grant() {
        let service = service(vec![]);
        assert_eq!(
            service
                .check_collection_perms(
                    actor().user_id,
                    actor().role_id,
                    UsersResource::UserProfile,
                    UsersAction::Read,
                    HashSet::new()
                )
                .await,
            Err(UnauthorizedError.into())
        );
        let service = super::tests::service(vec![grant(
            Principal::User(actor().user_id),
            ScopeObject::Collection(UsersResource::UserProfile),
            UsersAction::Read,
            AttrObject::Selected(HashSet::new()),
        )]);
        assert_eq!(
            service
                .check_collection_perms(
                    actor().user_id,
                    actor().role_id,
                    UsersResource::UserProfile,
                    UsersAction::Read,
                    HashSet::new()
                )
                .await,
            Ok(())
        );
        assert_eq!(
            service
                .check_collection_perms(
                    actor().user_id,
                    actor().role_id,
                    UsersResource::UserProfile,
                    UsersAction::Read,
                    HashSet::from([UsersAttribute::Name])
                )
                .await,
            Err(UnauthorizedError.into())
        );
    }

    #[tokio::test]
    async fn management_requires_permission_grants() {
        let service = service(vec![]);
        assert_eq!(
            service
                .create_user_collection_perm(
                    actor(),
                    actor().user_id,
                    UsersResource::UserProfile,
                    UsersAction::Read,
                    HashSet::from([UsersAttribute::Name])
                )
                .await,
            Err(UnauthorizedError.into())
        );
        assert!(matches!(
            service
                .get_permissions(actor(), Principal::User(actor().user_id))
                .await,
            Err(PermissionProviderError::Denied(_))
        ));
        assert_eq!(
            service.revoke_permission(actor(), Uuid::nil()).await,
            Err(UnauthorizedError.into())
        );
        assert!(service.repository.grants.lock().unwrap().is_empty());
    }

    #[tokio::test]
    async fn authorized_management_preserves_targets() {
        let service = service(admin_grants());
        let user = Uuid::from_u128(50);
        let role = Uuid::from_u128(51);
        let target = Uuid::from_u128(52);
        service
            .create_user_collection_perm(
                actor(),
                user,
                UsersResource::UserProfile,
                UsersAction::Read,
                HashSet::from([UsersAttribute::Name]),
            )
            .await
            .unwrap();
        service
            .create_role_collection_perm(
                actor(),
                role,
                UsersResource::UserProfile,
                UsersAction::Read,
                HashSet::new(),
            )
            .await
            .unwrap();
        service
            .create_object_perm(
                actor(),
                Principal::User(user),
                UsersResource::UserProfile,
                target,
                UsersAction::Update,
                HashSet::from([UsersAttribute::Name]),
            )
            .await
            .unwrap();
        service
            .create_role_object_perm(
                actor(),
                role,
                UsersResource::UserProfile,
                target,
                UsersAction::Delete,
                HashSet::new(),
            )
            .await
            .unwrap();
        for (principal, expected_object_action) in [
            (Principal::User(user), UsersAction::Update),
            (Principal::Role(role), UsersAction::Delete),
        ] {
            let grants = service.get_permissions(actor(), principal).await.unwrap();
            assert_eq!(grants.len(), 2);
            assert!(grants.iter().any(|g| matches!(
                g.scope.object,
                ScopeObject::Collection(UsersResource::UserProfile)
            )));
            let object = grants
                .iter()
                .find(|g| matches!(g.scope.object, ScopeObject::ObjectId(_, id) if id == target))
                .unwrap();
            assert_eq!(object.action, expected_object_action);
            service
                .revoke_permission(actor(), object.perm_id)
                .await
                .unwrap();
            service
                .revoke_permission(actor(), object.perm_id)
                .await
                .unwrap();
        }
        let victim = service
            .repository
            .grants
            .lock()
            .unwrap()
            .iter()
            .find(|g| matches!(g.principal, Principal::User(id) if id == user))
            .map(copy_grant)
            .unwrap();
        let scoped = super::tests::service(vec![
            copy_grant(&victim),
            grant(
                Principal::User(actor().user_id),
                ScopeObject::ObjectId(UsersResource::UserPermission, victim.perm_id),
                UsersAction::Delete,
                AttrObject::All,
            ),
        ]);
        assert_eq!(
            scoped.revoke_permission(actor(), Uuid::nil()).await,
            Err(UnauthorizedError.into())
        );
        scoped
            .revoke_permission(actor(), victim.perm_id)
            .await
            .unwrap();
        assert!(
            !scoped
                .repository
                .grants
                .lock()
                .unwrap()
                .iter()
                .any(|g| g.perm_id == victim.perm_id)
        );
    }

    #[tokio::test]
    async fn invalid_requests_and_stored_grants_are_rejected() {
        let service = service(admin_grants());
        assert_eq!(
            service
                .create_user_collection_perm(
                    actor(),
                    actor().user_id,
                    UsersResource::UserProfile,
                    UsersAction::Read,
                    HashSet::from([UsersAttribute::UserState])
                )
                .await,
            Err(PermissionProviderError::InvalidRequest)
        );
        assert_eq!(
            service
                .create_user_collection_perm(
                    actor(),
                    actor().user_id,
                    UsersResource::UserPermission,
                    UsersAction::Update,
                    HashSet::new()
                )
                .await,
            Err(PermissionProviderError::InvalidRequest)
        );
        assert_eq!(service.repository.grants.lock().unwrap().len(), 3);
        service.repository.grants.lock().unwrap().push(grant(
            Principal::User(actor().user_id),
            ScopeObject::Collection(UsersResource::UserProfile),
            UsersAction::Read,
            AttrObject::Selected(HashSet::from([UsersAttribute::UserState])),
        ));
        assert_eq!(
            service
                .check_collection_perms(
                    actor().user_id,
                    actor().role_id,
                    UsersResource::UserProfile,
                    UsersAction::Read,
                    HashSet::new()
                )
                .await,
            Err(PermissionProviderError::InvalidData)
        );
    }

    #[tokio::test]
    async fn storage_failures_are_not_denials() {
        for (error, expected) in [
            (
                UserPermissionsRepositoryError::Unavailable,
                PermissionProviderError::Unavailable,
            ),
            (
                UserPermissionsRepositoryError::InvalidData,
                PermissionProviderError::InvalidData,
            ),
            (
                UserPermissionsRepositoryError::Unexpected,
                PermissionProviderError::Unexpected,
            ),
        ] {
            let service = UserPermissionsProviderService::new(Store {
                read_error: Some(copy_error(&error)),
                ..Store::default()
            });
            assert_eq!(
                service
                    .check_collection_perms(
                        actor().user_id,
                        actor().role_id,
                        UsersResource::UserProfile,
                        UsersAction::Read,
                        HashSet::new()
                    )
                    .await,
                Err(expected)
            );
            let service = UserPermissionsProviderService::new(Store {
                grants: Mutex::new(admin_grants()),
                write_error: Some(copy_error(&error)),
                ..Store::default()
            });
            assert_eq!(
                service
                    .create_user_collection_perm(
                        actor(),
                        actor().user_id,
                        UsersResource::UserProfile,
                        UsersAction::Read,
                        HashSet::new()
                    )
                    .await,
                Err(provider_error(copy_error(&error)))
            );
            assert_eq!(
                service.revoke_permission(actor(), Uuid::nil()).await,
                Err(provider_error(error))
            );
        }
    }

    #[test]
    fn discovery_matches_supported_combinations() {
        let discovered = UserPermissionsProviderService::<Store>::discover_perms();
        assert_eq!(discovered.len(), 5);
        for item in discovered {
            assert_eq!(
                item.actions.len(),
                if item.resource == UsersResource::UserPermission {
                    3
                } else {
                    4
                }
            );
            for (action, attrs) in item.actions {
                let AttrObject::Selected(attrs) = attrs else {
                    panic!("expected explicit discovery");
                };
                assert_eq!(validate_request(item.resource, action, &attrs), Ok(()));
            }
        }
    }

    #[tokio::test]
    async fn management_requires_all_record_fields() {
        let service = service(
            [UsersAction::Create, UsersAction::Read]
                .into_iter()
                .map(|action| {
                    grant(
                        Principal::User(actor().user_id),
                        ScopeObject::Collection(UsersResource::UserPermission),
                        action,
                        AttrObject::Selected(HashSet::from([UsersAttribute::PrincipalId])),
                    )
                })
                .collect(),
        );
        assert_eq!(
            service
                .create_user_collection_perm(
                    actor(),
                    actor().user_id,
                    UsersResource::UserProfile,
                    UsersAction::Read,
                    HashSet::from([UsersAttribute::Name])
                )
                .await,
            Err(UnauthorizedError.into())
        );
        assert!(matches!(
            service
                .get_permissions(actor(), Principal::User(actor().user_id))
                .await,
            Err(PermissionProviderError::Denied(_))
        ));
        assert_eq!(service.repository.grants.lock().unwrap().len(), 2);
    }

    #[tokio::test]
    async fn password_requires_user_attribute_coverage() {
        let service = service(vec![grant(
            Principal::User(actor().user_id),
            ScopeObject::Collection(UsersResource::User),
            UsersAction::Update,
            AttrObject::Selected(HashSet::from([UsersAttribute::Password])),
        )]);
        assert_eq!(
            service
                .check_collection_perms(
                    actor().user_id,
                    actor().role_id,
                    UsersResource::User,
                    UsersAction::Update,
                    HashSet::from([UsersAttribute::Password])
                )
                .await,
            Ok(())
        );
        assert_eq!(
            service
                .check_collection_perms(
                    actor().user_id,
                    actor().role_id,
                    UsersResource::User,
                    UsersAction::Read,
                    HashSet::from([UsersAttribute::Password])
                )
                .await,
            Err(UnauthorizedError.into())
        );
        assert_eq!(
            service
                .check_collection_perms(
                    actor().user_id,
                    actor().role_id,
                    UsersResource::UserProfile,
                    UsersAction::Update,
                    HashSet::from([UsersAttribute::Password])
                )
                .await,
            Err(PermissionProviderError::InvalidRequest)
        );
        service.repository.grants.lock().unwrap()[0]
            .scope
            .attributes = AttrObject::Selected(HashSet::from([UsersAttribute::UserType]));
        assert_eq!(
            service
                .check_collection_perms(
                    actor().user_id,
                    actor().role_id,
                    UsersResource::User,
                    UsersAction::Update,
                    HashSet::from([UsersAttribute::Password])
                )
                .await,
            Err(UnauthorizedError.into())
        );
        service.repository.grants.lock().unwrap()[0]
            .scope
            .attributes = AttrObject::All;
        assert_eq!(
            service
                .check_collection_perms(
                    actor().user_id,
                    actor().role_id,
                    UsersResource::User,
                    UsersAction::Update,
                    HashSet::from([UsersAttribute::Password])
                )
                .await,
            Ok(())
        );
        let user = UserPermissionsProviderService::<Store>::discover_perms()
            .into_iter()
            .find(|entry| entry.resource == UsersResource::User)
            .unwrap();
        for attributes in user.actions.values() {
            assert!(
                matches!(attributes, AttrObject::Selected(attrs) if attrs.contains(&UsersAttribute::Password))
            );
        }
    }
}
