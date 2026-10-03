//! PostgreSQL adapters; raw rows stay within the users persistence boundary.
pub(super) mod rows;
mod user_account_repository;
pub use user_account_repository::UserRepository;
mod user_profile_repository;
pub use user_profile_repository::UserProfileRepository;
mod user_preferences_repository;
pub use user_preferences_repository::UserPreferencesRepository;
mod user_contact_repository;
pub use user_contact_repository::UserContactRepository;
