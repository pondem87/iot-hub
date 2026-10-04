//! Read contracts used by the local service implementations.

use std::future::Future;

use sqlx::types::Uuid;

use crate::users::{
    errors::UserReadError,
    models::{
        ActiveContact, ActiveUser, StoredContact, StoredUser, User, UserContact, UserPreferences,
        UserProfile,
    },
};

/// Required account reads; this contract does not establish caller authorization.
pub trait UserAccountReads: Send + Sync {
    /// Retrieves required account information in the supplied identity scope.
    /// # Errors
    /// Distinguishes absence, state mismatch, invalid data, and operational failures.
    fn get_user_by_id(
        &self,
        id: Uuid,
    ) -> impl Future<Output = Result<StoredUser, UserReadError>> + Send;

    /// Retrieves required account information in the supplied identity scope.
    /// # Errors
    /// Distinguishes absence, state mismatch, invalid data, and operational failures.
    fn get_active_user(
        &self,
        id: Uuid,
    ) -> impl Future<Output = Result<User<ActiveUser>, UserReadError>> + Send;
}

/// Required profile reads; this contract does not establish caller authorization.
pub trait UserProfileReads: Send + Sync {
    /// Retrieves the profile referenced by `user_id`; this does not validate caller permission.
    /// # Errors
    /// Returns not-found for an absent owning user/profile, invalid data, or operational failure.
    fn get_profile(
        &self,
        user_id: Uuid,
    ) -> impl Future<Output = Result<UserProfile, UserReadError>> + Send;
}

/// Required preferences reads; this contract does not establish caller authorization.
pub trait UserPreferencesReads: Send + Sync {
    /// Retrieves preferences referenced by `user_id`; this does not validate caller permission.
    /// # Errors
    /// Returns not-found for an absent owning user/preferences, invalid data, or operational failure.
    fn get_preferences(
        &self,
        user_id: Uuid,
    ) -> impl Future<Output = Result<UserPreferences, UserReadError>> + Send;
}

/// Required contact reads; this contract does not establish caller authorization.
pub trait UserContactReads: Send + Sync {
    /// Retrieves `contact_id` within `user_id` ownership; this does not validate caller permission.
    /// # Errors
    /// Distinguishes absence, state mismatch, invalid data, and operational failures.
    fn get_contact(
        &self,
        user_id: Uuid,
        contact_id: Uuid,
    ) -> impl Future<Output = Result<StoredContact, UserReadError>> + Send;

    /// Retrieves `contact_id` within `user_id` ownership; this does not validate caller permission.
    /// # Errors
    /// Distinguishes absence, state mismatch, invalid data, and operational failures.
    fn get_active_contact(
        &self,
        user_id: Uuid,
        contact_id: Uuid,
    ) -> impl Future<Output = Result<UserContact<ActiveContact>, UserReadError>> + Send;
}
