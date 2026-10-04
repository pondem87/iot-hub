//! Lifecycle types validated by persistence adapters; transitions await business policy.
use std::marker::PhantomData;

use chrono::{DateTime, Utc};
use sqlx::FromRow;
use sqlx::types::Uuid;

use super::errors::UserError;

/// Persisted role classifications; organisation role semantics remain unresolved.
#[derive(Debug, Clone, Copy, PartialEq, Eq, sqlx::Type)]
#[sqlx(type_name = "user_type")]
pub enum UserType {
    /// Represents the superuser classification.
    #[sqlx(rename = "superuser")]
    Superuser,
    /// Represents the staff classification.
    #[sqlx(rename = "staff")]
    Staff,
    /// Represents the customer classification.
    #[sqlx(rename = "customer")]
    Customer,
}

/// Persisted account lifecycle values; transition policy remains unresolved.
#[derive(Debug, Clone, Copy, PartialEq, Eq, sqlx::Type)]
#[sqlx(type_name = "user_state")]
pub enum UserState {
    /// Represents the unverified classification.
    #[sqlx(rename = "unverified")]
    Unverified,
    /// Represents the active classification.
    #[sqlx(rename = "active")]
    Active,
    /// Represents the inactive classification.
    #[sqlx(rename = "inactive")]
    Inactive,
    /// Represents the barred classification.
    #[sqlx(rename = "barred")]
    Barred,
    /// Represents the deleted classification.
    #[sqlx(rename = "deleted")]
    Deleted,
}

/// Classifies the communication identifier independently of its lifecycle.
#[derive(Debug, Clone, Copy, PartialEq, Eq, sqlx::Type)]
#[sqlx(type_name = "user_contact_type")]
pub enum UserContactType {
    /// Represents the phonenumber classification.
    #[sqlx(rename = "phonenumber")]
    PhoneNumber,
    /// Represents the emailaddress classification.
    #[sqlx(rename = "emailaddress")]
    EmailAddress,
}

/// Persisted contact lifecycle values; transition policy remains unresolved.
#[derive(Debug, Clone, Copy, PartialEq, Eq, sqlx::Type)]
#[sqlx(type_name = "user_contact_state")]
pub enum UserContactState {
    /// Represents the unverified classification.
    #[sqlx(rename = "unverified")]
    Unverified,
    /// Represents the active classification.
    #[sqlx(rename = "active")]
    Active,
    /// Represents the disabled classification.
    #[sqlx(rename = "disabled")]
    Disabled,
}

/// Marks a validated unverified user; no transitions are yet defined.
#[derive(Debug)]
pub struct UnverifiedUser;

/// Marks a validated active user; no transitions are yet defined.
#[derive(Debug)]
pub struct ActiveUser;

/// Marks a validated inactive user; no transitions are yet defined.
#[derive(Debug)]
pub struct InactiveUser;

/// Marks a validated barred user; no transitions are yet defined.
#[derive(Debug)]
pub struct BarredUser;

/// Marks a validated deleted user; no transitions are yet defined.
#[derive(Debug)]
pub struct DeletedUser;

/// Marks a validated unverified contact; no transitions are yet defined.
#[derive(Debug)]
pub struct UnverifiedContact;

/// Marks a validated active contact; no transitions are yet defined.
#[derive(Debug)]
pub struct ActiveContact;

/// Marks a validated disabled contact; no transitions are yet defined.
#[derive(Debug)]
pub struct DisabledContact;

/// A user whose lifecycle is validated before construction.
/// Reads a typed user identity without granting access permissions.
/// ```
/// use iot_hub::users::models::{User, ActiveUser};
/// fn identity(user: &User<ActiveUser>) -> sqlx::types::Uuid { user.id() }
/// ```
/// ```compile_fail
/// use iot_hub::users::models::{User, ActiveUser, UserState};
/// fn bypass(user: &mut User<ActiveUser>) { user.state = UserState::Deleted; }
/// ```
/// ```compile_fail
/// use iot_hub::users::models::{User, ActiveUser};
/// fn bypass() { let _: User<ActiveUser> = User::new(); }
/// ```
/// ```compile_fail
/// use iot_hub::users::models::{UserContact, ActiveContact, UserContactState};
/// fn bypass(contact: &mut UserContact<ActiveContact>) { contact.state = UserContactState::Disabled; }
/// ```
/// ```compile_fail
/// use iot_hub::users::models::{UserContact, ActiveContact};
/// fn bypass() { let _: UserContact<ActiveContact> = UserContact::new(); }
/// ```
pub struct User<State> {
    id: Uuid,
    phone_number: String,
    user_type: UserType,
    state: UserState,
    profile_id: Uuid,
    preferences_id: Uuid,
    created_at: DateTime<Utc>,
    updated_at: DateTime<Utc>,
    state_marker: PhantomData<State>,
}

impl<State> std::fmt::Debug for User<State> {
    fn fmt(&self, formatter: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        formatter
            .debug_struct("User")
            .field("id", &self.id)
            .field("state", &self.state)
            .finish_non_exhaustive()
    }
}

impl<State> User<State> {
    /// Returns the stored id value; this is not an authorization decision.
    pub fn id(&self) -> Uuid {
        self.id
    }

    /// Returns the stored phone_number value; this is not an authorization decision.
    pub fn phone_number(&self) -> &str {
        &self.phone_number
    }

    /// Returns the stored user_type value; this is not an authorization decision.
    pub fn user_type(&self) -> UserType {
        self.user_type
    }

    /// Returns the stored state value; this is not an authorization decision.
    pub fn state(&self) -> UserState {
        self.state
    }

    /// Returns the stored profile_id value; this is not an authorization decision.
    pub fn profile_id(&self) -> Uuid {
        self.profile_id
    }

    /// Returns the stored preferences_id value; this is not an authorization decision.
    pub fn preferences_id(&self) -> Uuid {
        self.preferences_id
    }

    /// Returns the stored created_at value; this is not an authorization decision.
    pub fn created_at(&self) -> DateTime<Utc> {
        self.created_at
    }
    /// Returns the stored updated_at value; this is not an authorization decision.
    pub fn updated_at(&self) -> DateTime<Utc> {
        self.updated_at
    }
}

/// A contact whose lifecycle is validated before construction.
/// ```
/// use iot_hub::users::models::{UserContact, ActiveContact};
/// fn identity(contact: &UserContact<ActiveContact>) -> sqlx::types::Uuid { contact.id() }
/// ```
pub struct UserContact<State> {
    id: Uuid,
    contact_type: UserContactType,
    value: String,
    state: UserContactState,
    user_id: Uuid,
    created_at: DateTime<Utc>,
    updated_at: DateTime<Utc>,
    state_marker: PhantomData<State>,
}

impl<State> std::fmt::Debug for UserContact<State> {
    fn fmt(&self, formatter: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        formatter
            .debug_struct("UserContact")
            .field("id", &self.id)
            .field("state", &self.state)
            .finish_non_exhaustive()
    }
}

impl<State> UserContact<State> {
    /// Returns the stored id value; this is not an authorization decision.
    pub fn id(&self) -> Uuid {
        self.id
    }

    /// Returns the stored contact_type value; this is not an authorization decision.
    pub fn contact_type(&self) -> UserContactType {
        self.contact_type
    }

    /// Returns the stored value value; this is not an authorization decision.
    pub fn value(&self) -> &str {
        &self.value
    }

    /// Returns the stored state value; this is not an authorization decision.
    pub fn state(&self) -> UserContactState {
        self.state
    }

    /// Returns the stored user_id value; this is not an authorization decision.
    pub fn user_id(&self) -> Uuid {
        self.user_id
    }

    /// Returns the stored created_at value; this is not an authorization decision.
    pub fn created_at(&self) -> DateTime<Utc> {
        self.created_at
    }

    /// Returns the stored updated_at value; this is not an authorization decision.
    pub fn updated_at(&self) -> DateTime<Utc> {
        self.updated_at
    }
}

/// Validated alternatives for a lookup accepting every stored lifecycle state.
#[derive(Debug)]
pub enum StoredUser {
    /// Contains a validated unverified object.
    Unverified(User<UnverifiedUser>),
    /// Contains a validated active object.
    Active(User<ActiveUser>),
    /// Contains a validated inactive object.
    Inactive(User<InactiveUser>),
    /// Contains a validated barred object.
    Barred(User<BarredUser>),
    /// Contains a validated deleted object.
    Deleted(User<DeletedUser>),
}

/// Validated alternatives for a lookup accepting every stored lifecycle state.
#[derive(Debug)]
pub enum StoredContact {
    /// Contains a validated unverified object.
    Unverified(UserContact<UnverifiedContact>),
    /// Contains a validated active object.
    Active(UserContact<ActiveContact>),
    /// Contains a validated disabled object.
    Disabled(UserContact<DisabledContact>),
}

/// Persisted UserProfile information with no lifecycle state.
#[derive(Debug, FromRow)]
pub struct UserProfile {
    /// Stored id value.
    pub id: Uuid,
    /// Stored name value.
    pub name: String,
    /// Stored updated_at value.
    pub updated_at: DateTime<Utc>,
}

/// Persisted UserPreferences information with no lifecycle state.
#[derive(Debug, FromRow)]
pub struct UserPreferences {
    /// Stored id value.
    pub id: Uuid,
    /// Stored allow_notifications value.
    pub allow_notifications: bool,
    /// Stored updated_at value.
    pub updated_at: DateTime<Utc>,
}

impl User<UnverifiedUser> {
    /// Constructs this typestate only after checking the persisted lifecycle value.
    /// # Errors
    /// Returns a domain state mismatch if the supplied lifecycle differs.
    pub(super) fn from_persisted(row: UserData) -> Result<Self, super::errors::UserError> {
        let actual: UserState = row.state;
        if actual != UserState::Unverified {
            return Err(super::errors::UserError::UserStateMismatch {
                expected: UserState::Unverified,
                actual,
            });
        }
        Ok(Self {
            id: row.id,
            phone_number: row.phone_number,
            user_type: row.user_type,
            state: row.state,
            profile_id: row.profile_id,
            preferences_id: row.preferences_id,
            created_at: row.created_at,
            updated_at: row.updated_at,
            state_marker: PhantomData,
        })
    }
}

impl User<ActiveUser> {
    /// Constructs this typestate only after checking the persisted lifecycle value.
    /// # Errors
    /// Returns a domain state mismatch if the supplied lifecycle differs.
    pub(super) fn from_persisted(row: UserData) -> Result<Self, super::errors::UserError> {
        let actual: UserState = row.state;
        if actual != UserState::Active {
            return Err(super::errors::UserError::UserStateMismatch {
                expected: UserState::Active,
                actual,
            });
        }
        Ok(Self {
            id: row.id,
            phone_number: row.phone_number,
            user_type: row.user_type,
            state: row.state,
            profile_id: row.profile_id,
            preferences_id: row.preferences_id,
            created_at: row.created_at,
            updated_at: row.updated_at,
            state_marker: PhantomData,
        })
    }
}

impl User<InactiveUser> {
    /// Constructs this typestate only after checking the persisted lifecycle value.
    /// # Errors
    /// Returns a domain state mismatch if the supplied lifecycle differs.
    pub(super) fn from_persisted(row: UserData) -> Result<Self, super::errors::UserError> {
        let actual: UserState = row.state;
        if actual != UserState::Inactive {
            return Err(super::errors::UserError::UserStateMismatch {
                expected: UserState::Inactive,
                actual,
            });
        }
        Ok(Self {
            id: row.id,
            phone_number: row.phone_number,
            user_type: row.user_type,
            state: row.state,
            profile_id: row.profile_id,
            preferences_id: row.preferences_id,
            created_at: row.created_at,
            updated_at: row.updated_at,
            state_marker: PhantomData,
        })
    }
}

impl User<BarredUser> {
    /// Constructs this typestate only after checking the persisted lifecycle value.
    /// # Errors
    /// Returns a domain state mismatch if the supplied lifecycle differs.
    pub(super) fn from_persisted(row: UserData) -> Result<Self, super::errors::UserError> {
        let actual: UserState = row.state;
        if actual != UserState::Barred {
            return Err(super::errors::UserError::UserStateMismatch {
                expected: UserState::Barred,
                actual,
            });
        }
        Ok(Self {
            id: row.id,
            phone_number: row.phone_number,
            user_type: row.user_type,
            state: row.state,
            profile_id: row.profile_id,
            preferences_id: row.preferences_id,
            created_at: row.created_at,
            updated_at: row.updated_at,
            state_marker: PhantomData,
        })
    }
}

impl User<DeletedUser> {
    /// Constructs this typestate only after checking the persisted lifecycle value.
    /// # Errors
    /// Returns a domain state mismatch if the supplied lifecycle differs.
    pub(super) fn from_persisted(row: UserData) -> Result<Self, super::errors::UserError> {
        let actual: UserState = row.state;
        if actual != UserState::Deleted {
            return Err(super::errors::UserError::UserStateMismatch {
                expected: UserState::Deleted,
                actual,
            });
        }
        Ok(Self {
            id: row.id,
            phone_number: row.phone_number,
            user_type: row.user_type,
            state: row.state,
            profile_id: row.profile_id,
            preferences_id: row.preferences_id,
            created_at: row.created_at,
            updated_at: row.updated_at,
            state_marker: PhantomData,
        })
    }
}

impl UserContact<UnverifiedContact> {
    /// Constructs this typestate only after checking the persisted lifecycle value.
    /// # Errors
    /// Returns a domain state mismatch if the supplied lifecycle differs.
    pub(super) fn from_persisted(row: ContactData) -> Result<Self, super::errors::UserError> {
        let actual: UserContactState = row.state;
        if actual != UserContactState::Unverified {
            return Err(super::errors::UserError::ContactStateMismatch {
                expected: UserContactState::Unverified,
                actual,
            });
        }
        Ok(Self {
            id: row.id,
            contact_type: row.contact_type,
            value: row.value,
            state: row.state,
            user_id: row.user_id,
            created_at: row.created_at,
            updated_at: row.updated_at,
            state_marker: PhantomData,
        })
    }
}

impl UserContact<ActiveContact> {
    /// Constructs this typestate only after checking the persisted lifecycle value.
    /// # Errors
    /// Returns a domain state mismatch if the supplied lifecycle differs.
    pub(super) fn from_persisted(row: ContactData) -> Result<Self, super::errors::UserError> {
        let actual: UserContactState = row.state;
        if actual != UserContactState::Active {
            return Err(super::errors::UserError::ContactStateMismatch {
                expected: UserContactState::Active,
                actual,
            });
        }
        Ok(Self {
            id: row.id,
            contact_type: row.contact_type,
            value: row.value,
            state: row.state,
            user_id: row.user_id,
            created_at: row.created_at,
            updated_at: row.updated_at,
            state_marker: PhantomData,
        })
    }
}

impl UserContact<DisabledContact> {
    /// Constructs this typestate only after checking the persisted lifecycle value.
    /// # Errors
    /// Returns a domain state mismatch if the supplied lifecycle differs.
    pub(super) fn from_persisted(row: ContactData) -> Result<Self, super::errors::UserError> {
        let actual: UserContactState = row.state;
        if actual != UserContactState::Disabled {
            return Err(super::errors::UserError::ContactStateMismatch {
                expected: UserContactState::Disabled,
                actual,
            });
        }
        Ok(Self {
            id: row.id,
            contact_type: row.contact_type,
            value: row.value,
            state: row.state,
            user_id: row.user_id,
            created_at: row.created_at,
            updated_at: row.updated_at,
            state_marker: PhantomData,
        })
    }
}

/// Internal untrusted UserData awaiting lifecycle validation.
#[derive(FromRow)]
pub(super) struct UserData {
    pub(super) id: Uuid,
    pub(super) phone_number: String,
    pub(super) user_type: UserType,
    pub(super) state: UserState,
    pub(super) profile_id: Uuid,
    pub(super) preferences_id: Uuid,
    pub(super) created_at: DateTime<Utc>,
    pub(super) updated_at: DateTime<Utc>,
}

/// Internal untrusted ContactData awaiting lifecycle validation.
#[derive(FromRow)]
pub(super) struct ContactData {
    pub(super) id: Uuid,
    #[sqlx(rename = "user_contact_type")]
    pub(super) contact_type: UserContactType,
    pub(super) value: String,
    #[sqlx(rename = "states")]
    pub(super) state: UserContactState,
    pub(super) user_id: Uuid,
    pub(super) created_at: DateTime<Utc>,
    pub(super) updated_at: DateTime<Utc>,
}

impl UserData {
    /// Consumes decoded data and checks construction of its stored lifecycle variant.
    /// # Errors
    /// Returns a state mismatch if checked construction rejects the stored state.
    pub(super) fn validate(self) -> Result<StoredUser, UserError> {
        match self.state {
            UserState::Unverified => Ok(StoredUser::Unverified(
                User::<UnverifiedUser>::from_persisted(self)?,
            )),
            UserState::Active => Ok(StoredUser::Active(User::<ActiveUser>::from_persisted(
                self,
            )?)),
            UserState::Inactive => Ok(StoredUser::Inactive(User::<InactiveUser>::from_persisted(
                self,
            )?)),
            UserState::Barred => Ok(StoredUser::Barred(User::<BarredUser>::from_persisted(
                self,
            )?)),
            UserState::Deleted => Ok(StoredUser::Deleted(User::<DeletedUser>::from_persisted(
                self,
            )?)),
        }
    }
}

impl ContactData {
    /// Consumes decoded data and checks construction of its stored lifecycle variant.
    /// # Errors
    /// Returns a state mismatch if checked construction rejects the stored state.
    pub(super) fn validate(self) -> Result<StoredContact, UserError> {
        match self.state {
            UserContactState::Unverified => {
                Ok(StoredContact::Unverified(
                    UserContact::<UnverifiedContact>::from_persisted(self)?,
                ))
            }
            UserContactState::Active => Ok(StoredContact::Active(
                UserContact::<ActiveContact>::from_persisted(self)?,
            )),
            UserContactState::Disabled => {
                Ok(StoredContact::Disabled(
                    UserContact::<DisabledContact>::from_persisted(self)?,
                ))
            }
        }
    }
}

#[cfg(test)]
mod tests {
    //! Tests checked lifecycle construction and immutable information.
    //! # Test plan
    //! - `retains_sql_type_names`: preserves all four PostgreSQL enum identities.
    //! - `checks_user_states`: checks every constructor against every user state.
    //! - `checks_contact_states`: checks every constructor against every contact state.
    //! - `redacts_contact_details`: excludes phone and address values from domain Debug output.
    //! - `reads_stored_fields`: retains user and contact data through checked construction.
    //!
    //! Rustdoc examples cover inaccessible construction and lifecycle mutation with valid companions.
    use super::*;

    #[test]
    fn retains_sql_type_names() {
        use sqlx::{Type, TypeInfo};
        assert_eq!(UserType::type_info().name(), "user_type");
        assert_eq!(UserState::type_info().name(), "user_state");
        assert_eq!(UserContactType::type_info().name(), "user_contact_type");
        assert_eq!(UserContactState::type_info().name(), "user_contact_state");
    }

    fn user_data(state: UserState) -> UserData {
        UserData {
            id: Uuid::from_u128(1),
            phone_number: "phone".into(),
            user_type: UserType::Staff,
            state,
            profile_id: Uuid::from_u128(2),
            preferences_id: Uuid::from_u128(3),
            created_at: DateTime::from_timestamp(0, 0).unwrap(),
            updated_at: DateTime::from_timestamp(1, 0).unwrap(),
        }
    }

    fn contact_data(state: UserContactState) -> ContactData {
        ContactData {
            id: Uuid::from_u128(4),
            contact_type: UserContactType::EmailAddress,
            value: "example@example.test".into(),
            state,
            user_id: Uuid::from_u128(1),
            created_at: DateTime::from_timestamp(0, 0).unwrap(),
            updated_at: DateTime::from_timestamp(1, 0).unwrap(),
        }
    }

    #[test]
    fn checks_user_states() {
        let states = [
            UserState::Unverified,
            UserState::Active,
            UserState::Inactive,
            UserState::Barred,
            UserState::Deleted,
        ];

        for expected in states {
            for actual in states {
                let result = match expected {
                    UserState::Unverified => {
                        User::<UnverifiedUser>::from_persisted(user_data(actual))
                            .map(|user| user.state())
                    }
                    UserState::Active => User::<ActiveUser>::from_persisted(user_data(actual))
                        .map(|user| user.state()),
                    UserState::Inactive => User::<InactiveUser>::from_persisted(user_data(actual))
                        .map(|user| user.state()),
                    UserState::Barred => User::<BarredUser>::from_persisted(user_data(actual))
                        .map(|user| user.state()),
                    UserState::Deleted => User::<DeletedUser>::from_persisted(user_data(actual))
                        .map(|user| user.state()),
                };

                if expected == actual {
                    assert_eq!(result.unwrap(), actual);
                } else {
                    assert!(
                        matches!(result, Err(UserError::UserStateMismatch { expected: e, actual: a }) if e == expected && a == actual)
                    );
                }
            }
        }
    }

    #[test]
    fn checks_contact_states() {
        let states = [
            UserContactState::Unverified,
            UserContactState::Active,
            UserContactState::Disabled,
        ];
        for expected in states {
            for actual in states {
                let result = match expected {
                    UserContactState::Unverified => {
                        UserContact::<UnverifiedContact>::from_persisted(contact_data(actual))
                            .map(|contact| contact.state())
                    }
                    UserContactState::Active => {
                        UserContact::<ActiveContact>::from_persisted(contact_data(actual))
                            .map(|contact| contact.state())
                    }
                    UserContactState::Disabled => {
                        UserContact::<DisabledContact>::from_persisted(contact_data(actual))
                            .map(|contact| contact.state())
                    }
                };
                if expected == actual {
                    assert_eq!(result.unwrap(), actual);
                } else {
                    assert!(
                        matches!(result, Err(UserError::ContactStateMismatch { expected: e, actual: a }) if e == expected && a == actual)
                    );
                }
            }
        }
    }

    #[test]
    fn redacts_contact_details() {
        let user = User::<ActiveUser>::from_persisted(user_data(UserState::Active)).unwrap();
        let contact =
            UserContact::<ActiveContact>::from_persisted(contact_data(UserContactState::Active))
                .unwrap();
        assert!(!format!("{user:?}").contains("phone"));
        assert!(!format!("{contact:?}").contains("example@example.test"));
    }

    #[test]
    fn reads_stored_fields() {
        let user = User::<ActiveUser>::from_persisted(user_data(UserState::Active)).unwrap();
        assert_eq!(user.id(), Uuid::from_u128(1));
        assert_eq!(user.phone_number(), "phone");
        assert_eq!(user.user_type(), UserType::Staff);
        assert_eq!(user.profile_id(), Uuid::from_u128(2));
        assert_eq!(user.preferences_id(), Uuid::from_u128(3));
        assert_eq!(user.created_at(), DateTime::from_timestamp(0, 0).unwrap());
        assert_eq!(user.updated_at(), DateTime::from_timestamp(1, 0).unwrap());
        let contact =
            UserContact::<ActiveContact>::from_persisted(contact_data(UserContactState::Active))
                .unwrap();
        assert_eq!(contact.id(), Uuid::from_u128(4));
        assert_eq!(contact.contact_type(), UserContactType::EmailAddress);
        assert_eq!(contact.value(), "example@example.test");
        assert_eq!(contact.user_id(), user.id());
        assert_eq!(contact.created_at(), user.created_at());
        assert_eq!(contact.updated_at(), user.updated_at());
    }
}
