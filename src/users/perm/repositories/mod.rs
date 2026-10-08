//! Persistence contracts and PostgreSQL adapters for user permission grants.

pub mod traits;
mod user_permissions_repository;

pub use traits::UserPermissionsStore;
pub use user_permissions_repository::UserPermissionsRepository;
