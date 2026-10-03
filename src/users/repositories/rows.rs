//! Internal SQL rows and technical error translation.

use chrono::{DateTime, Utc};
use sqlx::types::Uuid;

use crate::users::{
    errors::UserRepositoryError,
    models::{
        ActiveContact, ActiveUser, BarredUser, ContactData, DeletedUser, DisabledContact,
        InactiveUser, StoredContact, StoredUser, UnverifiedContact, UnverifiedUser, User,
        UserContact, UserContactState, UserContactType, UserData, UserPreferences, UserProfile,
        UserState, UserType,
    },
};

/// Converts technical failures to storage outcomes without exposing SQLx in contracts.
pub(super) fn storage_error(error: sqlx::Error) -> UserRepositoryError {
    match error {
        cause @ (sqlx::Error::Io(_)
        | sqlx::Error::Tls(_)
        | sqlx::Error::PoolTimedOut
        | sqlx::Error::PoolClosed) => UserRepositoryError::Unavailable {
            cause: Box::new(cause),
        },
        cause @ (sqlx::Error::ColumnDecode { .. }
        | sqlx::Error::Decode(_)
        | sqlx::Error::ColumnNotFound(_)
        | sqlx::Error::TypeNotFound { .. }) => UserRepositoryError::InvalidData {
            cause: Box::new(cause),
        },
        cause => UserRepositoryError::Unexpected {
            cause: Box::new(cause),
        },
    }
}
/// Explicit SQL spelling for user_type.
#[derive(Debug, Clone, Copy, sqlx::Type)]
#[sqlx(type_name = "user_type")]
pub(super) enum StoredUserType {
    #[sqlx(rename = "superuser")]
    Superuser,
    #[sqlx(rename = "staff")]
    Staff,
    #[sqlx(rename = "customer")]
    Customer,
}
impl From<StoredUserType> for UserType {
    fn from(value: StoredUserType) -> Self {
        match value {
            StoredUserType::Superuser => Self::Superuser,
            StoredUserType::Staff => Self::Staff,
            StoredUserType::Customer => Self::Customer,
        }
    }
}
/// Explicit SQL spelling for user_state.
#[derive(Debug, Clone, Copy, sqlx::Type)]
#[sqlx(type_name = "user_state")]
pub(super) enum StoredUserState {
    #[sqlx(rename = "unverified")]
    Unverified,
    #[sqlx(rename = "active")]
    Active,
    #[sqlx(rename = "inactive")]
    Inactive,
    #[sqlx(rename = "barred")]
    Barred,
    #[sqlx(rename = "deleted")]
    Deleted,
}
impl From<StoredUserState> for UserState {
    fn from(value: StoredUserState) -> Self {
        match value {
            StoredUserState::Unverified => Self::Unverified,
            StoredUserState::Active => Self::Active,
            StoredUserState::Inactive => Self::Inactive,
            StoredUserState::Barred => Self::Barred,
            StoredUserState::Deleted => Self::Deleted,
        }
    }
}
/// Explicit SQL spelling for user_contact_type.
#[derive(Debug, Clone, Copy, sqlx::Type)]
#[sqlx(type_name = "user_contact_type")]
pub(super) enum StoredContactType {
    #[sqlx(rename = "phonenumber")]
    PhoneNumber,
    #[sqlx(rename = "emailaddress")]
    EmailAddress,
}
impl From<StoredContactType> for UserContactType {
    fn from(value: StoredContactType) -> Self {
        match value {
            StoredContactType::PhoneNumber => Self::PhoneNumber,
            StoredContactType::EmailAddress => Self::EmailAddress,
        }
    }
}
/// Explicit SQL spelling for user_contact_state.
#[derive(Debug, Clone, Copy, sqlx::Type)]
#[sqlx(type_name = "user_contact_state")]
pub(super) enum StoredContactState {
    #[sqlx(rename = "unverified")]
    Unverified,
    #[sqlx(rename = "active")]
    Active,
    #[sqlx(rename = "disabled")]
    Disabled,
}
impl From<StoredContactState> for UserContactState {
    fn from(value: StoredContactState) -> Self {
        match value {
            StoredContactState::Unverified => Self::Unverified,
            StoredContactState::Active => Self::Active,
            StoredContactState::Disabled => Self::Disabled,
        }
    }
}
/// Untrusted persisted UserRow retaining its original stored state.
#[derive(sqlx::FromRow)]
pub(super) struct UserRow {
    pub(super) id: Uuid,
    pub(super) phone_number: String,
    pub(super) user_type: StoredUserType,
    pub(super) state: StoredUserState,
    pub(super) profile_id: Uuid,
    pub(super) preferences_id: Uuid,
    pub(super) created_at: DateTime<Utc>,
    pub(super) updated_at: DateTime<Utc>,
}
/// Untrusted persisted ContactRow retaining its original stored state.
#[derive(sqlx::FromRow)]
pub(super) struct ContactRow {
    pub(super) id: Uuid,
    #[sqlx(rename = "user_contact_type")]
    pub(super) contact_type: StoredContactType,
    pub(super) value: String,
    #[sqlx(rename = "states")]
    pub(super) state: StoredContactState,
    pub(super) user_id: Uuid,
    pub(super) created_at: DateTime<Utc>,
    pub(super) updated_at: DateTime<Utc>,
}
/// Untrusted persisted ProfileRow retaining its original stored state.
#[derive(sqlx::FromRow)]
pub(super) struct ProfileRow {
    pub(super) id: Uuid,
    pub(super) name: String,
    pub(super) updated_at: DateTime<Utc>,
}
/// Untrusted persisted PreferencesRow retaining its original stored state.
#[derive(sqlx::FromRow)]
pub(super) struct PreferencesRow {
    pub(super) id: Uuid,
    pub(super) allow_notifications: bool,
    pub(super) updated_at: DateTime<Utc>,
}
impl UserRow {
    pub(super) fn validate(self) -> Result<StoredUser, UserRepositoryError> {
        match self.state {
            StoredUserState::Unverified => Ok(StoredUser::Unverified(
                User::<UnverifiedUser>::from_persisted(self.into())?,
            )),
            StoredUserState::Active => Ok(StoredUser::Active(User::<ActiveUser>::from_persisted(
                self.into(),
            )?)),
            StoredUserState::Inactive => Ok(StoredUser::Inactive(
                User::<InactiveUser>::from_persisted(self.into())?,
            )),
            StoredUserState::Barred => Ok(StoredUser::Barred(User::<BarredUser>::from_persisted(
                self.into(),
            )?)),
            StoredUserState::Deleted => Ok(StoredUser::Deleted(
                User::<DeletedUser>::from_persisted(self.into())?,
            )),
        }
    }
}
impl ContactRow {
    pub(super) fn validate(self) -> Result<StoredContact, UserRepositoryError> {
        match self.state {
            StoredContactState::Unverified => {
                Ok(StoredContact::Unverified(
                    UserContact::<UnverifiedContact>::from_persisted(self.into())?,
                ))
            }
            StoredContactState::Active => Ok(StoredContact::Active(
                UserContact::<ActiveContact>::from_persisted(self.into())?,
            )),
            StoredContactState::Disabled => {
                Ok(StoredContact::Disabled(
                    UserContact::<DisabledContact>::from_persisted(self.into())?,
                ))
            }
        }
    }
}
impl From<ProfileRow> for UserProfile {
    fn from(row: ProfileRow) -> Self {
        Self {
            id: row.id,
            name: row.name,
            updated_at: row.updated_at,
        }
    }
}
impl From<PreferencesRow> for UserPreferences {
    fn from(row: PreferencesRow) -> Self {
        Self {
            id: row.id,
            allow_notifications: row.allow_notifications,
            updated_at: row.updated_at,
        }
    }
}

impl From<UserRow> for UserData {
    fn from(row: UserRow) -> Self {
        Self {
            id: row.id,
            phone_number: row.phone_number,
            user_type: row.user_type.into(),
            state: row.state.into(),
            profile_id: row.profile_id,
            preferences_id: row.preferences_id,
            created_at: row.created_at,
            updated_at: row.updated_at,
        }
    }
}
impl From<ContactRow> for ContactData {
    fn from(row: ContactRow) -> Self {
        Self {
            id: row.id,
            contact_type: row.contact_type.into(),
            value: row.value,
            state: row.state.into(),
            user_id: row.user_id,
            created_at: row.created_at,
            updated_at: row.updated_at,
        }
    }
}

#[cfg(test)]
mod tests {
    //! Tests structured SQL error translation and enum spelling.
    //! # Test plan
    //! - `translates_sql_failures`: distinguishes unavailable, malformed, and unexpected failures.
    //! - `retains_sql_type_names`: preserves PostgreSQL enum identities.
    use super::*;
    use sqlx::Type;
    #[test]
    fn translates_sql_failures() {
        assert!(matches!(
            storage_error(sqlx::Error::PoolClosed),
            UserRepositoryError::Unavailable { .. }
        ));
        assert!(matches!(
            storage_error(sqlx::Error::Decode(Box::new(std::io::Error::other(
                "decode"
            )))),
            UserRepositoryError::InvalidData { .. }
        ));
        assert!(matches!(
            storage_error(sqlx::Error::RowNotFound),
            UserRepositoryError::Unexpected { .. }
        ));
    }
    #[test]
    fn retains_sql_type_names() {
        use sqlx::TypeInfo;
        assert_eq!(StoredContactType::type_info().name(), "user_contact_type");
        assert_eq!(StoredUserState::type_info().name(), "user_state");
    }
}
