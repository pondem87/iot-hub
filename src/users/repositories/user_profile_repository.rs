//! PostgreSQL profile reads within the owning identity scope.

use sqlx::{PgPool, types::Uuid};

use super::rows::{ProfileRow, storage_error};
use crate::users::{errors::UserRepositoryError, models::UserProfile, traits::UserProfileStore};

/// Reads persisted profile records using an injected, cheaply cloned connection pool.
pub struct UserProfileRepository {
    pool: PgPool,
}
impl UserProfileRepository {
    /// Creates the adapter without establishing a new connection.
    pub fn new(pool: PgPool) -> Self {
        Self { pool }
    }
    async fn load(&self, user_id: Uuid) -> Result<Option<ProfileRow>, UserRepositoryError> {
        sqlx::query_as::<_, ProfileRow>("SELECT p.id, p.name, p.updated_at FROM user_profiles p JOIN users u ON u.profile_id = p.id WHERE u.id = $1").bind(user_id).fetch_optional(&self.pool).await.map_err(storage_error)
    }
}
impl UserProfileStore for UserProfileRepository {
    async fn get_profile(&self, user_id: Uuid) -> Result<Option<UserProfile>, UserRepositoryError> {
        Ok(self.load(user_id).await?.map(Into::into))
    }
}
#[cfg(test)]
mod tests {
    //! Tests profile persistence conversion and disconnected storage.
    //! # Test plan
    //! - `reports_unavailable_storage`: reports a closed pool without using a live service.
    //! - `preserves_stored_fields`: maps stored values without inventing validation rules.
    //!
    //! Isolated database coverage is in `tests/users_persistence.rs`.
    use super::*;
    use crate::users::models::*;
    #[tokio::test]
    async fn reports_unavailable_storage() {
        let pool = sqlx::postgres::PgPoolOptions::new()
            .connect_lazy("postgres://localhost/test")
            .unwrap();
        pool.close().await;
        let repository = UserProfileRepository::new(pool);
        assert!(matches!(
            repository.get_profile(Uuid::nil()).await,
            Err(UserRepositoryError::Unavailable { .. })
        ));
    }
    #[test]
    fn preserves_stored_fields() {
        let row = ProfileRow {
            id: Uuid::nil(),
            name: "example".into(),
            updated_at: chrono::Utc::now(),
        };
        let value: UserProfile = row.into();
        assert_eq!(value.id, Uuid::nil());
        assert_eq!(value.name, "example");
    }
}
