use std::collections::HashSet;
use std::collections::HashMap;
use sqlx::types::Uuid;

/// Represents a permission granted to a principal for a specific action on a resource, potentially scoped by resource attributes.
/// This struct is generic over the resource, resource attribute, and action types, allowing for flexible permission definitions.
/// Allows a generic structure for permission while allowing domain specific typing and enforcement.
pub struct Permission<R: Resource<ResourceAttr> + ResourceTrait, ResourceAttr: ResourceAttrTrait, Action: ActionTrait> {
    pub perm_id: Uuid,
    pub principal: Principal,
    pub scope: Scope<R, ResourceAttr>,
    pub action: Action,
}

impl<R: Resource<ResourceAttr> + ResourceTrait, ResourceAttr: ResourceAttrTrait, Action: ActionTrait> PermissionTrait for Permission<R, ResourceAttr, Action> {
    fn perm_id(&self) -> &Uuid {
        &self.perm_id
    }

    fn principal(&self) -> &Principal {
        &self.principal
    }

    fn scope(&self) -> &dyn ScopeTrait {
        &self.scope
    }

    fn action(&self) -> &dyn ActionTrait {
        &self.action
    }
}

pub struct Scope<R: Resource<ResourceAttr> + ResourceTrait, ResourceAttr: ResourceAttrTrait> {
    pub object: ScopeObject<R, ResourceAttr>,
    pub attributes: AttrObject<ResourceAttr>,
}

impl<R: Resource<ResourceAttr> + ResourceTrait, ResourceAttr: ResourceAttrTrait> ScopeTrait for Scope<R, ResourceAttr> {
    fn object(&self) -> &dyn ScopeObjectTrait {
        &self.object
    }

    fn attributes(&self) -> &dyn AttrObjectTrait {
        &self.attributes
    }
}

pub enum Principal {
    User(Uuid),
    Role(Uuid),
}

pub enum ScopeObject<R: Resource<ResourceAttr> + ResourceTrait, ResourceAttr: ResourceAttrTrait> {
    Collection(R),
    ObjectId(R, Uuid)
}

impl<R: Resource<ResourceAttr> + ResourceTrait, ResourceAttr: ResourceAttrTrait> ScopeObjectTrait for ScopeObject<R, ResourceAttr> {
    fn object_type(&self) -> &str {
        match self {
            ScopeObject::Collection(_) => "collection",
            ScopeObject::ObjectId(_, _) => "object",
        }
    }

    fn object_id(&self) -> Option<&Uuid> {
        match self {
            ScopeObject::Collection(_) => None,
            ScopeObject::ObjectId(_, id) => Some(id),
        }
    }
}

pub enum AttrObject<ResourceAttr: ResourceAttrTrait> {
    All,
    Selected(HashSet<ResourceAttr>),
}

impl<ResourceAttr: ResourceAttrTrait> AttrObjectTrait for AttrObject<ResourceAttr> {
    fn attributes_type(&self) -> &str {
        match self {
            AttrObject::All => "all",
            AttrObject::Selected(_) => "selected",
        }
    }

    fn attribute_names(&self) -> Vec<&str> {
        match self {
            AttrObject::All => vec![],
            AttrObject::Selected(attrs) => attrs.iter().map(|a| a.attribute_name()).collect(),
        }
    }
}

/// Represents the available permission resources and actions for the domain.
pub struct DiscoveredPermission<R:Resource<ResourceAttr> + ResourceTrait, ResourceAttr: ResourceAttrTrait, Action: ActionTrait> {
    pub resource: R,
    pub actions: HashMap<Action, AttrObject<ResourceAttr>>,
}