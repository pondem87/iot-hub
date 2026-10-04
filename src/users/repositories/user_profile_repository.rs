//! PostgreSQL profile reads within the owning identity scope.

use sqlx::{PgPool, types::Uuid};

use super::storage_error::storage_error;
use crate::users::{
    errors::UserRepositoryError, models::UserProfile, repositories::UserProfileStore,
};

/// Reads persisted profile records using an injected, cheaply cloned connection pool.
pub struct UserProfileRepository {
    pool: PgPool,
}
impl UserProfileRepository {
    /// Creates the adapter without establishing a new connection.
    pub fn new(pool: PgPool) -> Self {
        Self { pool }
    }
    async fn load(&self, user_id: Uuid) -> Result<Option<UserProfile>, UserRepositoryError> {
        sqlx::query_as::<_, UserProfile>("SELECT p.id, p.name, p.updated_at FROM user_profiles p JOIN users u ON u.profile_id = p.id WHERE u.id = $1").bind(user_id).fetch_optional(&self.pool).await.map_err(storage_error)
    }
}
impl UserProfileStore for UserProfileRepository {
    async fn get_profile(&self, user_id: Uuid) -> Result<Option<UserProfile>, UserRepositoryError> {
        self.load(user_id).await
    }
}
#[cfg(test)]
mod tests {
    //! Tests profile reads with disconnected storage.
    //! # Test plan
    //! - `reports_unavailable_storage`: reports a closed pool without using a live service.
    //!
    //! SQL decoding, field preservation, and ownership coverage are in `tests/users_persistence.rs`.
    use super::*;
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
}
