//! Read contracts; callers must establish authorization before exposing results.

use std::future::Future;

use sqlx::types::Uuid;

use super::{
    errors::{UserReadError, UserRepositoryError},
    models::{
        ActiveContact, ActiveUser, StoredContact, StoredUser, User, UserContact, UserPreferences,
        UserProfile,
    },
};

/// Persistence reads for account information within the documented identity scope.
pub trait UserStore: Send + Sync {
    /// Retrieves a user by `id` in any state, validating the returned enum variant.
    /// Returns `None` only when the user does not exist.
    /// # Errors
    /// Returns invalid stored data or a storage failure.
    fn get_user_by_id(
        &self,
        id: Uuid,
    ) -> impl Future<Output = Result<Option<StoredUser>, UserRepositoryError>> + Send;
    /// Retrieves the user by `id` and verifies it is active after decoding.
    /// Returns `None` only when the user does not exist.
    /// # Errors
    /// Returns state mismatch for an existing non-active user, invalid stored data, or storage failure.
    fn get_active_user(
        &self,
        id: Uuid,
    ) -> impl Future<Output = Result<Option<User<ActiveUser>>, UserRepositoryError>> + Send;
}
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
/// Persistence reads for profile information within the documented identity scope.
pub trait UserProfileStore: Send + Sync {
    /// Retrieves the profile referenced by `user_id`, in any account state.
    /// Returns `None` only when no profile is reachable through that user.
    /// # Errors
    /// Returns invalid stored data or a storage failure.
    fn get_profile(
        &self,
        user_id: Uuid,
    ) -> impl Future<Output = Result<Option<UserProfile>, UserRepositoryError>> + Send;
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
/// Persistence reads for preferences information within the documented identity scope.
pub trait UserPreferencesStore: Send + Sync {
    /// Retrieves preferences referenced by `user_id`, in any account state.
    /// Returns `None` only when no preferences are reachable through that user.
    /// # Errors
    /// Returns invalid stored data or a storage failure.
    fn get_preferences(
        &self,
        user_id: Uuid,
    ) -> impl Future<Output = Result<Option<UserPreferences>, UserRepositoryError>> + Send;
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
/// Persistence reads for contact information within the documented identity scope.
pub trait UserContactStore: Send + Sync {
    /// Retrieves `contact_id` belonging to `user_id`, validating any stored contact state.
    /// Returns `None` when the contact does not exist within that owner scope.
    /// # Errors
    /// Returns invalid stored data or a storage failure.
    fn get_contact(
        &self,
        user_id: Uuid,
        contact_id: Uuid,
    ) -> impl Future<Output = Result<Option<StoredContact>, UserRepositoryError>> + Send;
    /// Retrieves `contact_id` belonging to `user_id` and verifies the contact is active.
    /// The owning account state is not a prerequisite for this storage lookup.
    /// Returns `None` when the contact does not exist within that owner scope.
    /// # Errors
    /// Returns state mismatch for an existing non-active contact in scope, invalid data, or storage failure.
    fn get_active_contact(
        &self,
        user_id: Uuid,
        contact_id: Uuid,
    ) -> impl Future<Output = Result<Option<UserContact<ActiveContact>>, UserRepositoryError>> + Send;
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
