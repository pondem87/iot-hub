//! PostgreSQL storage for user-domain permission grants.

use sqlx::{PgPool, types::Uuid};

use crate::permissions::{
    app::ResourceAttrTrait,
    schemas::{AttrObject, Principal, Scope, ScopeObject},
};

use crate::users::perm::{
    errors::UserPermissionsRepositoryError,
    models::{
        PermissionType, PrincipalType, UserPermission, UserPermissionData, UsersAction,
        UsersAttribute, UsersResource,
    },
};

use super::UserPermissionsStore;

/// Persists grants using an injected PostgreSQL connection pool.
pub struct UserPermissionsRepository {
    pool: PgPool,
}

impl UserPermissionsRepository {
    /// Creates the adapter without opening a new connection.
    pub fn new(pool: PgPool) -> Self {
        Self { pool }
    }
}

impl UserPermissionsStore for UserPermissionsRepository {
    async fn create_permission(
        &self,
        principal: Principal,
        scope: Scope<UsersResource, UsersAttribute>,
        action: UsersAction,
    ) -> Result<UserPermission, UserPermissionsRepositoryError> {
        let (principal_type, principal_id) = principal_parts(principal);
        let (perm_type, resource, resource_id) = match scope.object {
            ScopeObject::Collection(resource) => (PermissionType::Collection, resource, None),
            ScopeObject::ObjectId(resource, id) => (PermissionType::Object, resource, Some(id)),
        };
        let attributes = match scope.attributes {
            AttrObject::All => r#""all""#.to_owned(),
            AttrObject::Selected(attributes) => {
                let mut names: Vec<_> = attributes
                    .iter()
                    .map(|attr| attr.attribute_name())
                    .collect();
                names.sort_unstable();
                serde_json::to_string(&names)
                    .map_err(|_| UserPermissionsRepositoryError::Unexpected)?
            }
        };

        let mut transaction = self.pool.begin().await.map_err(storage_error)?;
        let data = sqlx::query_as::<_, UserPermissionData>(
            "INSERT INTO user_permissions
             (principal_type, principal_id, perm_type, resource, resource_id, action, attributes)
             VALUES ($1, $2, $3, $4, $5, $6, $7)
             RETURNING id, principal_type, principal_id, perm_type, resource, resource_id, action, attributes",
        )
        .bind(principal_type)
        .bind(principal_id)
        .bind(perm_type)
        .bind(resource)
        .bind(resource_id)
        .bind(action)
        .bind(attributes)
        .fetch_one(&mut *transaction)
        .await
        .map_err(storage_error)?;
        let permission = UserPermission::from_data(data)
            .map_err(|_| UserPermissionsRepositoryError::InvalidData)?;
        transaction.commit().await.map_err(storage_error)?;

        Ok(permission)
    }

    async fn get_permissions(
        &self,
        principal: Principal,
    ) -> Result<Vec<UserPermission>, UserPermissionsRepositoryError> {
        let (principal_type, principal_id) = principal_parts(principal);
        let rows = sqlx::query_as::<_, UserPermissionData>(
            "SELECT id, principal_type, principal_id, perm_type, resource, resource_id, action, attributes
             FROM user_permissions WHERE principal_type = $1 AND principal_id = $2 ORDER BY id",
        )
        .bind(principal_type)
        .bind(principal_id)
        .fetch_all(&self.pool)
        .await
        .map_err(storage_error)?;

        rows.into_iter()
            .map(|data| {
                UserPermission::from_data(data)
                    .map_err(|_| UserPermissionsRepositoryError::InvalidData)
            })
            .collect()
    }

    async fn delete_permission(&self, id: Uuid) -> Result<bool, UserPermissionsRepositoryError> {
        let result = sqlx::query("DELETE FROM user_permissions WHERE id = $1")
            .bind(id)
            .execute(&self.pool)
            .await
            .map_err(storage_error)?;

        Ok(result.rows_affected() != 0)
    }
}

fn principal_parts(principal: Principal) -> (PrincipalType, Uuid) {
    match principal {
        Principal::User(id) => (PrincipalType::User, id),
        Principal::Role(id) => (PrincipalType::Role, id),
    }
}

fn storage_error(error: sqlx::Error) -> UserPermissionsRepositoryError {
    match error {
        sqlx::Error::Io(_)
        | sqlx::Error::Tls(_)
        | sqlx::Error::PoolTimedOut
        | sqlx::Error::PoolClosed => UserPermissionsRepositoryError::Unavailable,
        sqlx::Error::ColumnDecode { .. }
        | sqlx::Error::Decode(_)
        | sqlx::Error::ColumnNotFound(_)
        | sqlx::Error::TypeNotFound { .. } => UserPermissionsRepositoryError::InvalidData,
        _ => UserPermissionsRepositoryError::Unexpected,
    }
}

#[cfg(test)]
mod tests {
    //! Tests permission storage failure translation without a live database.
    //!
    //! # Test plan
    //! - `closed_pool_reports_unavailable`: covers creation, retrieval, and deletion.
    //! - `translates_storage_failures`: distinguishes connectivity, decoding, and other failures.
    //! - `principal_mapping_preserves_kind_and_identity`: distinguishes user and role IDs.

    use super::*;

    #[tokio::test]
    async fn closed_pool_reports_unavailable() {
        let pool = sqlx::postgres::PgPoolOptions::new()
            .connect_lazy("postgres://localhost/test")
            .unwrap();
        pool.close().await;
        let repository = UserPermissionsRepository::new(pool);
        let scope = Scope {
            object: ScopeObject::Collection(UsersResource::User),
            attributes: AttrObject::All,
        };

        assert!(matches!(
            repository
                .create_permission(Principal::User(Uuid::nil()), scope, UsersAction::Read)
                .await,
            Err(UserPermissionsRepositoryError::Unavailable)
        ));
        assert!(matches!(
            repository
                .get_permissions(Principal::User(Uuid::nil()))
                .await,
            Err(UserPermissionsRepositoryError::Unavailable)
        ));
        assert_eq!(
            repository.delete_permission(Uuid::nil()).await,
            Err(UserPermissionsRepositoryError::Unavailable)
        );
    }

    #[test]
    fn translates_storage_failures() {
        assert_eq!(
            storage_error(sqlx::Error::PoolClosed),
            UserPermissionsRepositoryError::Unavailable
        );
        assert_eq!(
            storage_error(sqlx::Error::ColumnNotFound("attributes".into())),
            UserPermissionsRepositoryError::InvalidData
        );
        assert_eq!(
            storage_error(sqlx::Error::RowNotFound),
            UserPermissionsRepositoryError::Unexpected
        );
    }

    #[test]
    fn principal_mapping_preserves_kind_and_identity() {
        let id = Uuid::from_u128(8);

        assert_eq!(
            principal_parts(Principal::User(id)),
            (PrincipalType::User, id)
        );
        assert_eq!(
            principal_parts(Principal::Role(id)),
            (PrincipalType::Role, id)
        );
    }
}
