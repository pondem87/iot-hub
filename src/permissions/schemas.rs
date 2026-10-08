//! Generic permission representations and their read-only trait adapters.

use std::collections::{HashMap, HashSet};

use sqlx::types::Uuid;

use crate::permissions::app::{
    ActionTrait, AttrObjectTrait, PermissionTrait, ResourceAttrTrait, ResourceTrait,
    ScopeObjectTrait, ScopeTrait,
};

/// A grant to a principal for an action within a resource and attribute scope.
///
/// This representation does not itself authorize requests or validate stored data.
/// Domain-specific constructors establish any additional validation rules.
pub struct Permission<Resource: ResourceTrait, ResourceAttr: ResourceAttrTrait, Action: ActionTrait>
{
    /// Identifier of the grant.
    pub perm_id: Uuid,
    /// User or role receiving the grant.
    pub principal: Principal,
    /// Target and attribute selection covered by the grant.
    pub scope: Scope<Resource, ResourceAttr>,
    /// Operation granted within the scope.
    pub action: Action,
}

impl<Resource: ResourceTrait, ResourceAttr: ResourceAttrTrait, Action: ActionTrait> PermissionTrait
    for Permission<Resource, ResourceAttr, Action>
{
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

/// A resource target paired with its permitted attributes.
pub struct Scope<Resource: ResourceTrait, ResourceAttr: ResourceAttrTrait> {
    /// Resource collection or identified object addressed by this scope.
    pub object: ScopeObject<Resource>,
    /// All attributes or an explicitly selected set.
    pub attributes: AttrObject<ResourceAttr>,
}

impl<Resource: ResourceTrait, ResourceAttr: ResourceAttrTrait> ScopeTrait
    for Scope<Resource, ResourceAttr>
{
    fn object(&self) -> &dyn ScopeObjectTrait {
        &self.object
    }

    fn attributes(&self) -> &dyn AttrObjectTrait {
        &self.attributes
    }
}

/// Authenticated actor whose role association has been established by the caller.
///
/// Permission providers use these identities to retrieve grants; constructing this
/// value does not authenticate a user or verify membership in the supplied role.
#[derive(Debug, Clone, Copy)]
pub struct PermissionActor {
    /// Authenticated user identifier.
    pub user_id: Uuid,
    /// Role identifier resolved by the trusted calling service.
    pub role_id: Uuid,
}

/// Identity receiving a permission; user and role UUIDs remain distinct kinds.
pub enum Principal {
    /// A grant assigned directly to a user.
    User(Uuid),
    /// A grant assigned to a role.
    Role(Uuid),
}

/// A complete resource collection or a specific object within that resource.
pub enum ScopeObject<Resource: ResourceTrait> {
    /// The collection represented by the resource, without an object ID.
    Collection(Resource),
    /// An object identified by its resource kind and UUID.
    ObjectId(Resource, Uuid),
}

impl<Resource: ResourceTrait> ScopeObjectTrait for ScopeObject<Resource> {
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

/// Attribute coverage for a grant, independent of its target object.
pub enum AttrObject<ResourceAttr: ResourceAttrTrait> {
    /// Every attribute; represented by the `"all"` discriminator.
    All,
    /// Explicit attributes, possibly empty; iteration order is unspecified.
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
pub struct DiscoveredPermission<
    Resource: ResourceTrait,
    ResourceAttr: ResourceAttrTrait,
    Action: ActionTrait,
> {
    /// Resource whose supported operations are described.
    pub resource: Resource,
    /// Supported actions and their available attribute selections.
    pub actions: HashMap<Action, AttrObject<ResourceAttr>>,
}

#[cfg(test)]
mod tests {
    //! Tests permission inspection through the shared trait contracts.
    //!
    //! # Test plan
    //! - `permission_accessors_preserve_grants`: checks both principal kinds,
    //!   permission IDs, actions, and nested scope inspection through trait objects.
    //! - `scope_objects_distinguish_collection_and_object`: verifies discriminators
    //!   and optional IDs for both target kinds.
    //! - `attribute_selections_distinguish_all_empty_and_selected`: checks all,
    //!   empty and populated selections without relying on hash iteration order.

    use super::*;

    /// Resource vocabulary for exercising the generic adapters.
    struct TestResource;

    impl ResourceTrait for TestResource {
        fn resource_name(&self) -> &str {
            "test_resource"
        }
    }

    /// Attribute vocabulary with two distinct selectable names.
    #[derive(PartialEq, Eq, Hash)]
    enum TestAttribute {
        Name,
        State,
    }

    impl ResourceAttrTrait for TestAttribute {
        fn attribute_name(&self) -> &str {
            match self {
                Self::Name => "name",
                Self::State => "state",
            }
        }
    }

    /// Operation used to exercise action inspection through a trait object.
    struct TestAction;

    impl ActionTrait for TestAction {
        fn action_name(&self) -> &str {
            "read"
        }
    }

    #[test]
    fn permission_accessors_preserve_grants() {
        let principal_id = Uuid::from_u128(1);
        let perm_id = Uuid::from_u128(2);
        let object_id = Uuid::from_u128(3);
        for principal in [Principal::User(principal_id), Principal::Role(principal_id)] {
            let is_user = matches!(principal, Principal::User(_));
            let permission = Permission {
                perm_id,
                principal,
                scope: Scope {
                    object: ScopeObject::ObjectId(TestResource, object_id),
                    attributes: AttrObject::Selected(HashSet::from([TestAttribute::Name])),
                },
                action: TestAction,
            };
            let view: &dyn PermissionTrait = &permission;

            assert_eq!(view.perm_id(), &perm_id);
            match view.principal() {
                Principal::User(id) => {
                    assert!(is_user);
                    assert_eq!(id, &principal_id);
                }
                Principal::Role(id) => {
                    assert!(!is_user);
                    assert_eq!(id, &principal_id);
                }
            }
            assert_eq!(view.action().action_name(), "read");
            assert_eq!(view.scope().object().object_type(), "object");
            assert_eq!(view.scope().object().object_id(), Some(&object_id));
            assert_eq!(view.scope().attributes().attributes_type(), "selected");
            assert_eq!(view.scope().attributes().attribute_names(), vec!["name"]);
        }
    }

    #[test]
    fn scope_objects_distinguish_collection_and_object() {
        let id = Uuid::from_u128(7);
        let collection = ScopeObject::Collection(TestResource);
        let object = ScopeObject::ObjectId(TestResource, id);
        for (scope, kind, expected_id) in [
            (&collection as &dyn ScopeObjectTrait, "collection", None),
            (&object as &dyn ScopeObjectTrait, "object", Some(&id)),
        ] {
            assert_eq!(scope.object_type(), kind);
            assert_eq!(scope.object_id(), expected_id);
        }
    }

    #[test]
    fn attribute_selections_distinguish_all_empty_and_selected() {
        let all = AttrObject::<TestAttribute>::All;
        let empty = AttrObject::Selected(HashSet::<TestAttribute>::new());
        let selected = AttrObject::Selected(HashSet::from([
            TestAttribute::State,
            TestAttribute::Name,
            TestAttribute::Name,
        ]));
        for (attributes, kind, expected) in [
            (&all as &dyn AttrObjectTrait, "all", HashSet::new()),
            (&empty as &dyn AttrObjectTrait, "selected", HashSet::new()),
            (
                &selected as &dyn AttrObjectTrait,
                "selected",
                HashSet::from(["name", "state"]),
            ),
        ] {
            assert_eq!(attributes.attributes_type(), kind);
            let names = attributes.attribute_names();
            assert_eq!(names.len(), expected.len());
            assert_eq!(names.into_iter().collect::<HashSet<_>>(), expected);
        }
    }
}
