//! User permission vocabulary and validation of decoded permission data.

use std::collections::HashSet;

use serde::Deserialize;
use sqlx::types::Uuid;

use crate::permissions::app::{ActionTrait, ResourceAttrTrait, ResourceTrait};
use crate::permissions::schemas::{AttrObject, Permission, Principal, Scope, ScopeObject};

/// Conversion errors; retained here for compatibility with existing imports.
pub use super::errors::UserPermissionDataError;

/// Attributes that can be selected by a user-domain permission.
#[derive(Debug, Clone, Copy, Deserialize, PartialEq, Eq, Hash)]
#[serde(rename_all = "snake_case")]
pub enum UsersAttribute {
    /// Account classification.
    UserType,
    /// Account lifecycle state.
    UserState,
    /// Credential field; stored values are encoded password hashes.
    Password,
    /// Contact channel classification.
    ContactType,
    /// Contact value.
    Value,
    /// Contact lifecycle state.
    State,
    /// Profile name.
    Name,
    /// Notification preference.
    AllowNotifications,
    /// Permission record identifier.
    Id,
    /// Kind of grant recipient.
    PrincipalType,
    /// Grant recipient identifier.
    PrincipalId,
    /// Collection or object scope discriminator.
    PermType,
    /// Resource addressed by a grant.
    Resource,
    /// Object identifier addressed by a grant.
    ResourceId,
    /// Operation granted by a permission.
    Action,
    /// Attribute selection stored on a grant.
    Attributes,
}

impl ResourceAttrTrait for UsersAttribute {
    fn attribute_name(&self) -> &str {
        match self {
            Self::UserType => "user_type",
            Self::UserState => "user_state",
            Self::Password => "password",
            Self::ContactType => "contact_type",
            Self::Value => "value",
            Self::State => "state",
            Self::Name => "name",
            Self::AllowNotifications => "allow_notifications",
            Self::Id => "id",
            Self::PrincipalType => "principal_type",
            Self::PrincipalId => "principal_id",
            Self::PermType => "perm_type",
            Self::Resource => "resource",
            Self::ResourceId => "resource_id",
            Self::Action => "action",
            Self::Attributes => "attributes",
        }
    }
}

/// Resources addressed by user-domain permissions.
#[derive(Debug, Clone, Copy, PartialEq, Eq, Hash, sqlx::Type)]
#[sqlx(type_name = "users_permission_resource", rename_all = "snake_case")]
pub enum UsersResource {
    /// User account resource.
    User,
    /// User contact resource.
    UserContact,
    /// User profile resource.
    UserProfile,
    /// User preferences resource.
    UserPreferences,
    /// Permission grant resource.
    UserPermission,
}

impl ResourceTrait for UsersResource {
    fn resource_name(&self) -> &str {
        match self {
            Self::User => "user",
            Self::UserContact => "user_contact",
            Self::UserProfile => "user_profile",
            Self::UserPreferences => "user_preferences",
            Self::UserPermission => "user_permission",
        }
    }
}

/// Operations granted by user-domain permissions.
#[derive(Debug, Clone, Copy, PartialEq, Eq, Hash, sqlx::Type)]
#[sqlx(type_name = "users_permission_action", rename_all = "snake_case")]
pub enum UsersAction {
    /// Create a resource.
    Create,
    /// Read a resource.
    Read,
    /// Update a resource.
    Update,
    /// Delete a resource.
    Delete,
}

impl ActionTrait for UsersAction {
    fn action_name(&self) -> &str {
        match self {
            Self::Create => "create",
            Self::Read => "read",
            Self::Update => "update",
            Self::Delete => "delete",
        }
    }
}

/// A permission over resources and attributes in the users domain.
pub type UserPermission = Permission<UsersResource, UsersAttribute, UsersAction>;
type UserScope = Scope<UsersResource, UsersAttribute>;

impl UserPermission {
    /// Decodes a permission without substituting defaults for invalid data.
    ///
    /// Attributes must be the JSON string `"all"` or a JSON array of snake_case
    /// attribute names. An empty array selects no attributes; duplicates collapse.
    /// Object permissions require a resource ID; collection permissions omit it.
    ///
    /// # Errors
    /// Returns a structured error when the resource ID conflicts with the scope
    /// or attributes contain malformed JSON, unknown names, or an invalid shape.
    pub fn from_data(data: UserPermissionData) -> Result<Self, UserPermissionDataError> {
        let object = match (data.perm_type, data.resource_id) {
            (PermissionType::Collection, None) => ScopeObject::Collection(data.resource),
            (PermissionType::Object, Some(id)) => ScopeObject::ObjectId(data.resource, id),
            (PermissionType::Object, None) => {
                return Err(UserPermissionDataError::MissingResourceId);
            }
            (PermissionType::Collection, Some(_)) => {
                return Err(UserPermissionDataError::UnexpectedResourceId);
            }
        };
        let attributes = match serde_json::from_str::<AttributeData>(&data.attributes)
            .map_err(|_| UserPermissionDataError::InvalidAttributes)?
        {
            AttributeData::All(AllAttributes::All) => AttrObject::All,
            AttributeData::Selected(attributes) => AttrObject::Selected(attributes),
        };
        let principal = match data.principal_type {
            PrincipalType::Role => Principal::Role(data.principal_id),
            PrincipalType::User => Principal::User(data.principal_id),
        };

        Ok(Self {
            perm_id: data.id,
            principal,
            scope: UserScope { object, attributes },
            action: data.action,
        })
    }
}

/// Whether a permission addresses a collection or an identified object.
#[derive(Debug, Clone, Copy, PartialEq, Eq, Hash, sqlx::Type)]
#[sqlx(type_name = "users_permission_type", rename_all = "snake_case")]
pub enum PermissionType {
    /// The resource collection as a whole.
    Collection,
    /// One resource identified by its UUID.
    Object,
}

/// The kind of principal receiving a permission.
#[derive(Debug, Clone, Copy, PartialEq, Eq, Hash, sqlx::Type)]
#[sqlx(
    type_name = "users_permission_principal_type",
    rename_all = "snake_case"
)]
pub enum PrincipalType {
    /// A role receiving the permission.
    Role,
    /// A user receiving the permission.
    User,
}

/// Unvalidated permission data consumed by checked construction.
#[derive(sqlx::FromRow)]
pub struct UserPermissionData {
    /// Identity of the permission record.
    pub id: Uuid,
    /// Whether the principal is a role or a user.
    pub principal_type: PrincipalType,
    /// Identity of the principal receiving the permission.
    pub principal_id: Uuid,
    /// Whether the scope is a collection or an object.
    pub perm_type: PermissionType,
    /// Resource addressed by the permission.
    pub resource: UsersResource,
    /// Required for object permissions and absent for collection permissions.
    pub resource_id: Option<Uuid>,
    /// Operation granted by the permission.
    pub action: UsersAction,
    /// JSON `"all"` or an array of snake_case attribute names.
    pub attributes: String,
}

/// Accepted JSON representations of an attribute selection.
#[derive(Deserialize)]
#[serde(untagged)]
enum AttributeData {
    All(AllAttributes),
    Selected(HashSet<UsersAttribute>),
}

/// The explicit JSON marker granting access to all attributes.
#[derive(Deserialize)]
enum AllAttributes {
    #[serde(rename = "all")]
    All,
}

#[cfg(test)]
mod tests {
    //! Tests checked conversion of user permission data and permission vocabulary.
    //!
    //! # Test plan
    //! - `converts_role_collection_permission`: preserves IDs, resource, action and all attributes.
    //! - `converts_user_object_permission`: preserves the target ID and deduplicates selected attributes.
    //! - `preserves_empty_selection`: an empty selection never becomes all attributes.
    //! - `rejects_inconsistent_resource_id`: rejects missing object IDs and collection IDs.
    //! - `rejects_invalid_attributes`: rejects malformed JSON, unknown names and invalid shapes.
    //! - `maps_permission_vocabulary`: covers every resource, action and attribute name.
    //! - `retains_permission_sql_type_names`: verifies explicit PostgreSQL enum names.

    use super::*;

    fn data() -> UserPermissionData {
        UserPermissionData {
            id: Uuid::from_u128(1),
            principal_type: PrincipalType::Role,
            principal_id: Uuid::from_u128(2),
            perm_type: PermissionType::Collection,
            resource: UsersResource::UserProfile,
            resource_id: None,
            action: UsersAction::Update,
            attributes: r#""all""#.into(),
        }
    }

    #[test]
    fn converts_role_collection_permission() {
        let permission = UserPermission::from_data(data()).unwrap();

        assert_eq!(permission.perm_id, Uuid::from_u128(1));
        assert!(matches!(permission.principal, Principal::Role(id) if id == Uuid::from_u128(2)));
        assert!(matches!(
            permission.scope.object,
            ScopeObject::Collection(UsersResource::UserProfile)
        ));
        assert!(matches!(permission.scope.attributes, AttrObject::All));
        assert!(matches!(permission.action, UsersAction::Update));
    }

    #[test]
    fn converts_user_object_permission() {
        let data = UserPermissionData {
            principal_type: PrincipalType::User,
            perm_type: PermissionType::Object,
            resource_id: Some(Uuid::from_u128(3)),
            attributes: r#"["name", "user_state", "name"]"#.into(),
            ..data()
        };
        let permission = UserPermission::from_data(data).unwrap();

        assert_eq!(permission.perm_id, Uuid::from_u128(1));
        assert!(matches!(permission.principal, Principal::User(id) if id == Uuid::from_u128(2)));
        assert!(
            matches!(permission.scope.object, ScopeObject::ObjectId(UsersResource::UserProfile, id) if id == Uuid::from_u128(3))
        );
        assert!(matches!(permission.action, UsersAction::Update));
        let AttrObject::Selected(attributes) = permission.scope.attributes else {
            panic!("expected selected attributes");
        };
        assert_eq!(
            attributes,
            HashSet::from([UsersAttribute::Name, UsersAttribute::UserState])
        );
    }

    #[test]
    fn preserves_empty_selection() {
        let permission = UserPermission::from_data(UserPermissionData {
            attributes: "[]".into(),
            ..data()
        })
        .unwrap();

        assert!(
            matches!(permission.scope.attributes, AttrObject::Selected(attrs) if attrs.is_empty())
        );
    }

    #[test]
    fn rejects_inconsistent_resource_id() {
        let cases = [
            (
                PermissionType::Object,
                None,
                UserPermissionDataError::MissingResourceId,
            ),
            (
                PermissionType::Collection,
                Some(Uuid::from_u128(3)),
                UserPermissionDataError::UnexpectedResourceId,
            ),
        ];
        for (perm_type, resource_id, expected) in cases {
            let result = UserPermission::from_data(UserPermissionData {
                perm_type,
                resource_id,
                ..data()
            });
            assert_eq!(result.err(), Some(expected));
        }
    }

    #[test]
    fn rejects_invalid_attributes() {
        for attributes in [
            "",
            "all",
            "[",
            "null",
            "{}",
            "true",
            "42",
            r#""unknown""#,
            r#"["unknown"]"#,
            r#"["Name"]"#,
            r#"["all"]"#,
            r#"["name", 1]"#,
        ] {
            let result = UserPermission::from_data(UserPermissionData {
                attributes: attributes.into(),
                ..data()
            });
            assert_eq!(
                result.err(),
                Some(UserPermissionDataError::InvalidAttributes)
            );
        }
    }

    #[test]
    fn maps_permission_vocabulary() {
        for (resource, expected) in [
            (UsersResource::User, "user"),
            (UsersResource::UserContact, "user_contact"),
            (UsersResource::UserProfile, "user_profile"),
            (UsersResource::UserPreferences, "user_preferences"),
            (UsersResource::UserPermission, "user_permission"),
        ] {
            assert_eq!(resource.resource_name(), expected);
        }
        for (action, expected) in [
            (UsersAction::Create, "create"),
            (UsersAction::Read, "read"),
            (UsersAction::Update, "update"),
            (UsersAction::Delete, "delete"),
        ] {
            assert_eq!(action.action_name(), expected);
        }
        for (attribute, expected) in [
            (UsersAttribute::UserType, "user_type"),
            (UsersAttribute::UserState, "user_state"),
            (UsersAttribute::Password, "password"),
            (UsersAttribute::ContactType, "contact_type"),
            (UsersAttribute::Value, "value"),
            (UsersAttribute::State, "state"),
            (UsersAttribute::Name, "name"),
            (UsersAttribute::AllowNotifications, "allow_notifications"),
            (UsersAttribute::Id, "id"),
            (UsersAttribute::PrincipalType, "principal_type"),
            (UsersAttribute::PrincipalId, "principal_id"),
            (UsersAttribute::PermType, "perm_type"),
            (UsersAttribute::Resource, "resource"),
            (UsersAttribute::ResourceId, "resource_id"),
            (UsersAttribute::Action, "action"),
            (UsersAttribute::Attributes, "attributes"),
        ] {
            assert_eq!(attribute.attribute_name(), expected);
            let permission = UserPermission::from_data(UserPermissionData {
                attributes: serde_json::to_string(&[expected]).unwrap(),
                ..data()
            })
            .unwrap();
            assert!(
                matches!(permission.scope.attributes, AttrObject::Selected(attrs) if attrs == HashSet::from([attribute]))
            );
        }
    }

    #[test]
    fn retains_permission_sql_type_names() {
        use sqlx::{Postgres, Type, TypeInfo};

        assert_eq!(
            <UsersResource as Type<Postgres>>::type_info().name(),
            "users_permission_resource"
        );
        assert_eq!(
            <UsersAction as Type<Postgres>>::type_info().name(),
            "users_permission_action"
        );
        assert_eq!(
            <PermissionType as Type<Postgres>>::type_info().name(),
            "users_permission_type"
        );
        assert_eq!(
            <PrincipalType as Type<Postgres>>::type_info().name(),
            "users_permission_principal_type"
        );
    }
}
