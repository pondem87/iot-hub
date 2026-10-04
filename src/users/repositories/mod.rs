//! PostgreSQL adapters; decoded data is validated within the repository boundary.
mod storage_error;
mod traits;
pub use traits::{UserContactStore, UserPreferencesStore, UserProfileStore, UserStore};
mod user_account_repository;
pub use user_account_repository::UserRepository;
mod user_profile_repository;
pub use user_profile_repository::UserProfileRepository;
mod user_preferences_repository;
pub use user_preferences_repository::UserPreferencesRepository;
mod user_contact_repository;
pub use user_contact_repository::UserContactRepository;
