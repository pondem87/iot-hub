//! Local read service implementations and the contracts they use.
//! The external service entry point is the separate `users::app` module.

mod traits;
pub use traits::{UserAccountReads, UserContactReads, UserPreferencesReads, UserProfileReads};

mod user_account_service;
pub use user_account_service::UserAccountService;
mod user_contact_service;
pub use user_contact_service::UserContactService;
mod user_preferences_service;
pub use user_preferences_service::UserPreferencesService;
mod user_profile_service;
pub use user_profile_service::UserProfileService;
