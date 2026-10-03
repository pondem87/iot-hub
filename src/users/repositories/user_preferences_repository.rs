//! PostgreSQL preferences reads within the owning identity scope.

use sqlx::{PgPool, types::Uuid};

use super::rows::{PreferencesRow, storage_error};
use crate::users::{
    errors::UserRepositoryError, models::UserPreferences, traits::UserPreferencesStore,
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
    async fn load(&self, user_id: Uuid) -> Result<Option<PreferencesRow>, UserRepositoryError> {
        sqlx::query_as::<_, PreferencesRow>("SELECT p.id, p.allow_notifications, p.updated_at FROM user_preferences p JOIN users u ON u.preferences_id = p.id WHERE u.id = $1").bind(user_id).fetch_optional(&self.pool).await.map_err(storage_error)
    }
}
impl UserPreferencesStore for UserPreferencesRepository {
    async fn get_preferences(
        &self,
        user_id: Uuid,
    ) -> Result<Option<UserPreferences>, UserRepositoryError> {
        Ok(self.load(user_id).await?.map(Into::into))
    }
}
#[cfg(test)]
mod tests {
    //! Tests preferences persistence conversion and disconnected storage.
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
        let repository = UserPreferencesRepository::new(pool);
        assert!(matches!(
            repository.get_preferences(Uuid::nil()).await,
            Err(UserRepositoryError::Unavailable { .. })
        ));
    }
    #[test]
    fn preserves_stored_fields() {
        let row = PreferencesRow {
            id: Uuid::nil(),
            allow_notifications: true,
            updated_at: chrono::Utc::now(),
        };
        let value: UserPreferences = row.into();
        assert_eq!(value.id, Uuid::nil());
        assert!(value.allow_notifications);
    }
}
