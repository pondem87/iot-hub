use std::collections::HashSet;

use sqlx::types::Uuid;

use crate::permissions::schemas::DiscoveredPermission;
use crate::permissions::schemas::Permission;
use crate::permissions::schemas::AttrObject;
use crate::permissions::schemas::Principal;


/// Defines the core traits and structures for managing permissions, scopes, resources, and actions within the system.
/// Traits help in providing generic deserialization and interaction with different types of permissions, scopes, resources, and actions.
pub trait PermissionTrait {
    fn perm_id(&self) -> &Uuid;
    fn principal(&self) -> &Principal;
    fn scope(&self) -> &dyn ScopeTrait;
    fn action(&self) -> &dyn ActionTrait;
}

pub trait ScopeTrait {
    fn object(&self) -> &dyn ScopeObjectTrait;
    fn attributes(&self) -> &dyn AttrObjectTrait;
}

pub trait ResourceTrait {
    fn resource_name(&self) -> &str;
}

pub trait ScopeObjectTrait {
    fn object_type(&self) -> &str;
    fn object_id(&self) -> Option<&Uuid>;
}

pub trait AttrObjectTrait {
    fn attributes_type(&self) -> &str;
    fn attribute_names(&self) -> Vec<&str>;
}

pub trait ResourceAttrTrait {
    fn attribute_name(&self) -> &str;
}

pub trait ActionTrait {
    fn action_name(&self) -> &str;
}


/// Defines the core trait for a permission provider, which is responsible for checking, creating, and managing permissions for principals on resources.
/// This trait is generic over the principal type, allowing for flexible handling of different principal representations.
pub trait PermissionProvider<R: Resource<ResourceAttr> + ResourceTrait, ResourceAttr: ResourceAttrTrait, Action: ActionTrait> {
    fn check_collection_perms(
        &self,
        user: Uuid,
        role: Uuid,
        resource: R,
        action: Action,
        resource_attrs: HashSet<ResourceAttr>,
    ) -> Result<(), UnauthorizedError>;

    fn check_object_perms(
        &self,
        user: Uuid,
        role: Uuid,
        resource: R,
        resource_id: Uuid,
        action: Action,
        resource_attrs: HashSet<ResourceAttr>,
    ) -> Result<(), UnauthorizedError>;

    fn discover_perms() -> Vec<DiscoveredPermission<R, ResourceAttr, Action>>;

    fn create_object_perm(
        &self,
        principal: Principal,
        resource: R,
        resource_id: Uuid,
        action: Action,
        resource_attrs: HashSet<ResourceAttr>,
    ) -> Result<(), UnauthorizedError>;

    fn create_user_collection_perm(
        &self,
        principal: Uuid,
        resource: R,
        action: Action,
        resource_attrs: HashSet<ResourceAttr>,
    ) -> Result<(), UnauthorizedError>;

    fn create_role_object_perm(
        &self,
        principal: Uuid,
        resource: R,
        resource_id: Uuid,
        action: Action,
        resource_attrs: HashSet<ResourceAttr>,
    ) -> Result<(), UnauthorizedError>;

    fn create_role_collection_perm(
        &self,
        principal: Uuid,
        resource: R,
        action: Action,
        resource_attrs: HashSet<ResourceAttr>,
    ) -> Result<(), UnauthorizedError>;

    fn get_permissions(
        &self,
        principal: Principal,
    ) -> Vec<Permission<R, ResourceAttr, Action>>;

    fn revoke_permission(
        &self,
        perm_id: PermId,
    ) -> Result<(), UnauthorizedError>;
}

pub struct UnauthorizedError;   

    