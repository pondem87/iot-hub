//! Contracts for inspecting permissions and implementing permission providers.
//!
//! These contracts do not define role precedence, grant authority, or persistence
//! guarantees. Providers must establish those policies before implementation.

use std::collections::HashSet;
use std::future::Future;

use sqlx::types::Uuid;

use crate::permissions::errors::PermissionProviderError;
use crate::permissions::schemas::{DiscoveredPermission, Permission, PermissionActor, Principal};

/// Authorization denial; retained here for compatibility with existing imports.
pub use crate::permissions::errors::UnauthorizedError;

/// Read-only access to a permission independent of its domain vocabulary.
pub trait PermissionTrait {
    /// Returns the permission record identifier without modifying it.
    fn perm_id(&self) -> &Uuid;

    /// Returns the user or role receiving the permission.
    fn principal(&self) -> &Principal;

    /// Borrows the target and attribute selection covered by the permission.
    fn scope(&self) -> &dyn ScopeTrait;

    /// Borrows the operation granted by the permission.
    fn action(&self) -> &dyn ActionTrait;
}

/// Read-only access to a permission's target and attribute selection.
pub trait ScopeTrait {
    /// Borrows the collection or identified object addressed by the scope.
    fn object(&self) -> &dyn ScopeObjectTrait;

    /// Borrows the attribute selection without changing the scope.
    fn attributes(&self) -> &dyn AttrObjectTrait;
}

/// Supplies the stable resource name defined by the owning domain.
pub trait ResourceTrait {
    /// Returns the domain-defined resource name without allocating.
    fn resource_name(&self) -> &str;
}

/// Describes whether a scope targets a collection or one identified object.
pub trait ScopeObjectTrait {
    /// Returns `"collection"` or `"object"` to identify the target kind.
    fn object_type(&self) -> &str;

    /// Returns the target identifier for an object, or `None` for a collection.
    fn object_id(&self) -> Option<&Uuid>;
}

/// Describes unrestricted attributes or an explicitly selected set.
pub trait AttrObjectTrait {
    /// Returns `"all"` for unrestricted attributes or `"selected"` for a set.
    fn attributes_type(&self) -> &str;

    /// Returns selected names in unspecified order, or an empty vector for all.
    ///
    /// Use [`Self::attributes_type`] to distinguish all from an empty selection.
    fn attribute_names(&self) -> Vec<&str>;
}

/// Supplies the stable attribute name defined by the owning domain.
pub trait ResourceAttrTrait {
    /// Returns the domain-defined attribute name without allocating.
    fn attribute_name(&self) -> &str;
}

/// Supplies the stable operation name defined by the owning domain.
pub trait ActionTrait {
    /// Returns the domain-defined operation name without allocating.
    fn action_name(&self) -> &str;
}

/// Checks and manages grants using a domain's resources, attributes, and actions.
///
/// Check operations do not mutate grants. Creation and revocation change grants
/// only on success. Storage operations are asynchronous and report operational
/// failures separately from authorization denial. Implementations must document
/// their policy, caller authorization requirements, and consistency guarantees.
/// Management operations take an authenticated actor separately from the grant
/// recipient. Callers must establish the actor's user-to-role association.
pub trait PermissionProvider<
    Resource: ResourceTrait,
    ResourceAttr: ResourceAttrTrait,
    Action: ActionTrait,
>
{
    /// Checks the requested collection action and attributes for a user and role.
    ///
    /// # Errors
    /// Returns [`PermissionProviderError::Denied`] when policy denies the request,
    /// invalid-request errors for unsupported inputs, or operational storage errors.
    fn check_collection_perms(
        &self,
        user: Uuid,
        role: Uuid,
        resource: Resource,
        action: Action,
        resource_attrs: HashSet<ResourceAttr>,
    ) -> impl Future<Output = Result<(), PermissionProviderError>> + Send;

    /// Checks the action and attributes on `resource_id` for a user and role.
    ///
    /// # Errors
    /// Returns [`PermissionProviderError::Denied`] when policy denies the request,
    /// invalid-request errors for unsupported inputs, or operational storage errors.
    fn check_object_perms(
        &self,
        user: Uuid,
        role: Uuid,
        resource: Resource,
        resource_id: Uuid,
        action: Action,
        resource_attrs: HashSet<ResourceAttr>,
    ) -> impl Future<Output = Result<(), PermissionProviderError>> + Send;

    /// Returns the resources, actions, and attribute selections supported by this provider.
    /// This describes capabilities and does not grant access or change stored permissions.
    fn discover_perms() -> Vec<DiscoveredPermission<Resource, ResourceAttr, Action>>;

    /// Creates an object grant for the supplied user or role principal.
    /// The grant covers `action` and `resource_attrs` on `resource_id`.
    ///
    /// # Errors
    /// Returns denial, invalid-request, or operational storage errors as appropriate
    /// to the provider's documented grant-management contract.
    fn create_object_perm(
        &self,
        actor: PermissionActor,
        principal: Principal,
        resource: Resource,
        resource_id: Uuid,
        action: Action,
        resource_attrs: HashSet<ResourceAttr>,
    ) -> impl Future<Output = Result<(), PermissionProviderError>> + Send;

    /// Creates a collection grant for the user identified by `principal`.
    /// The grant covers `action` and `resource_attrs` on the resource collection.
    ///
    /// # Errors
    /// Returns denial, invalid-request, or operational storage errors as appropriate
    /// to the provider's documented grant-management contract.
    fn create_user_collection_perm(
        &self,
        actor: PermissionActor,
        principal: Uuid,
        resource: Resource,
        action: Action,
        resource_attrs: HashSet<ResourceAttr>,
    ) -> impl Future<Output = Result<(), PermissionProviderError>> + Send;

    /// Creates an object grant for the role identified by `principal`.
    /// The grant covers `action` and `resource_attrs` on `resource_id`.
    ///
    /// # Errors
    /// Returns denial, invalid-request, or operational storage errors as appropriate
    /// to the provider's documented grant-management contract.
    fn create_role_object_perm(
        &self,
        actor: PermissionActor,
        principal: Uuid,
        resource: Resource,
        resource_id: Uuid,
        action: Action,
        resource_attrs: HashSet<ResourceAttr>,
    ) -> impl Future<Output = Result<(), PermissionProviderError>> + Send;

    /// Creates a collection grant for the role identified by `principal`.
    /// The grant covers `action` and `resource_attrs` on the resource collection.
    ///
    /// # Errors
    /// Returns denial, invalid-request, or operational storage errors as appropriate
    /// to the provider's documented grant-management contract.
    fn create_role_collection_perm(
        &self,
        actor: PermissionActor,
        principal: Uuid,
        resource: Resource,
        action: Action,
        resource_attrs: HashSet<ResourceAttr>,
    ) -> impl Future<Output = Result<(), PermissionProviderError>> + Send;

    /// Returns the supplied principal's grants without modifying them.
    /// An empty list means no grants exist for that principal.
    ///
    /// # Errors
    /// Returns invalid stored data or operational storage errors, never an empty
    /// list to conceal a failed read.
    fn get_permissions(
        &self,
        actor: PermissionActor,
        principal: Principal,
    ) -> impl Future<
        Output = Result<Vec<Permission<Resource, ResourceAttr, Action>>, PermissionProviderError>,
    > + Send;

    /// Revokes the grant identified by `perm_id` when allowed by provider policy.
    /// Providers must document how already-absent grants are handled.
    ///
    /// # Errors
    /// Returns denial or operational storage errors as appropriate to the
    /// provider's documented grant-management contract.
    fn revoke_permission(
        &self,
        actor: PermissionActor,
        perm_id: Uuid,
    ) -> impl Future<Output = Result<(), PermissionProviderError>> + Send;
}
