//! PostgreSQL preferences reads within the owning identity scope.

use sqlx::{PgPool, types::Uuid};

use super::storage_error::storage_error;
use crate::users::{
    errors::UserRepositoryError, models::UserPreferences, repositories::UserPreferencesStore,
};

/// Reads persisted preferences records using an injected, cheaply cloned connection pool.
pub struct UserPreferencesRepository {
    pool: PgPool,
}
impl UserPreferencesRepository {
    /// Creates the adapter without establishing a new connection.
    pub fn new(pool: PgPool) -> Self {
        Self { pool }
    }
    async fn load(&self, user_id: Uuid) -> Result<Option<UserPreferences>, UserRepositoryError> {
        sqlx::query_as::<_, UserPreferences>("SELECT p.id, p.allow_notifications, p.updated_at FROM user_preferences p JOIN users u ON u.preferences_id = p.id WHERE u.id = $1").bind(user_id).fetch_optional(&self.pool).await.map_err(storage_error)
    }
}
impl UserPreferencesStore for UserPreferencesRepository {
    async fn get_preferences(
        &self,
        user_id: Uuid,
    ) -> Result<Option<UserPreferences>, UserRepositoryError> {
        self.load(user_id).await
    }
}
#[cfg(test)]
mod tests {
    //! Tests preferences reads with disconnected storage.
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
        let repository = UserPreferencesRepository::new(pool);
        assert!(matches!(
            repository.get_preferences(Uuid::nil()).await,
            Err(UserRepositoryError::Unavailable { .. })
        ));
    }
}
