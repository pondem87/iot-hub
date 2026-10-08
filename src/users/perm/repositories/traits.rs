//! Storage contract for user-domain permission grants.

use std::future::Future;

use sqlx::types::Uuid;

use crate::permissions::schemas::{Principal, Scope};
use crate::users::perm::{
    errors::UserPermissionsRepositoryError,
    models::{UserPermission, UsersAction, UsersAttribute, UsersResource},
};

/// Stores and retrieves checked grants without deciding who may grant access.
pub trait UserPermissionsStore: Send + Sync {
    /// Persists a grant with a generated ID and returns its checked representation.
    /// The caller must authorize grant creation before invoking this operation.
    ///
    /// # Errors
    /// Returns invalid data or an operational storage failure. No successful grant
    /// is returned before the insert transaction commits.
    fn create_permission(
        &self,
        principal: Principal,
        scope: Scope<UsersResource, UsersAttribute>,
        action: UsersAction,
    ) -> impl Future<Output = Result<UserPermission, UserPermissionsRepositoryError>> + Send;

    /// Retrieves grants for exactly the supplied principal kind and UUID.
    /// An empty vector means no grants exist. Every returned row is validated.
    ///
    /// # Errors
    /// Returns invalid data if any matching row cannot be decoded or validated,
    /// or an operational failure. Invalid rows are never silently omitted.
    fn get_permissions(
        &self,
        principal: Principal,
    ) -> impl Future<Output = Result<Vec<UserPermission>, UserPermissionsRepositoryError>> + Send;

    /// Deletes a grant by ID, returning whether a row was removed.
    /// The caller must authorize revocation before invoking this operation.
    ///
    /// # Errors
    /// Returns an operational storage failure; absence alone returns `false`.
    fn delete_permission(
        &self,
        id: Uuid,
    ) -> impl Future<Output = Result<bool, UserPermissionsRepositoryError>> + Send;
}
