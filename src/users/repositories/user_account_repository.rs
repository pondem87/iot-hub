//! PostgreSQL account reads within the owning identity scope.

use sqlx::{PgPool, types::Uuid};

use super::rows::{UserRow, storage_error};
use crate::users::{
    errors::UserRepositoryError,
    models::{ActiveUser, StoredUser, User},
    traits::UserStore,
};

/// Reads persisted account records using an injected, cheaply cloned connection pool.
pub struct UserRepository {
    pool: PgPool,
}
impl UserRepository {
    /// Creates the adapter without establishing a new connection.
    pub fn new(pool: PgPool) -> Self {
        Self { pool }
    }
    async fn load(&self, id: Uuid) -> Result<Option<UserRow>, UserRepositoryError> {
        sqlx::query_as::<_, UserRow>("SELECT id, phone_number, user_type, state, profile_id, preferences_id, created_at, updated_at FROM users WHERE id = $1").bind(id).fetch_optional(&self.pool).await.map_err(storage_error)
    }
}
impl UserStore for UserRepository {
    async fn get_user_by_id(&self, id: Uuid) -> Result<Option<StoredUser>, UserRepositoryError> {
        self.load(id).await?.map(UserRow::validate).transpose()
    }
    async fn get_active_user(
        &self,
        id: Uuid,
    ) -> Result<Option<User<ActiveUser>>, UserRepositoryError> {
        self.load(id)
            .await?
            .map(|row| User::<ActiveUser>::from_persisted(row.into()).map_err(Into::into))
            .transpose()
    }
}
#[cfg(test)]
mod tests {
    //! Tests account persistence conversion and disconnected storage.
    //! # Test plan
    //! - `reports_unavailable_storage`: reports a closed pool without using a live service.
    //! - `validates_every_state`: converts each stored state to its corresponding typestate.
    //! - `rejects_wrong_active_state`: distinguishes an existing wrong-state row from absence.
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
        let repository = UserRepository::new(pool);
        assert!(matches!(
            repository.get_user_by_id(Uuid::nil()).await,
            Err(UserRepositoryError::Unavailable { .. })
        ));
    }
    use super::super::rows::*;
    fn row(state: StoredUserState) -> UserRow {
        UserRow {
            id: Uuid::nil(),
            phone_number: "test".into(),
            user_type: StoredUserType::Customer,
            state,
            profile_id: Uuid::nil(),
            preferences_id: Uuid::nil(),
            created_at: chrono::Utc::now(),
            updated_at: chrono::Utc::now(),
        }
    }
    #[test]
    fn validates_every_state() {
        assert!(matches!(
            row(StoredUserState::Unverified).validate().unwrap(),
            StoredUser::Unverified(_)
        ));
        assert!(matches!(
            row(StoredUserState::Active).validate().unwrap(),
            StoredUser::Active(_)
        ));
        assert!(matches!(
            row(StoredUserState::Inactive).validate().unwrap(),
            StoredUser::Inactive(_)
        ));
        assert!(matches!(
            row(StoredUserState::Barred).validate().unwrap(),
            StoredUser::Barred(_)
        ));
        assert!(matches!(
            row(StoredUserState::Deleted).validate().unwrap(),
            StoredUser::Deleted(_)
        ));
    }
    #[test]
    fn rejects_wrong_active_state() {
        for state in [
            StoredUserState::Unverified,
            StoredUserState::Inactive,
            StoredUserState::Barred,
            StoredUserState::Deleted,
        ] {
            assert!(matches!(
                User::<ActiveUser>::from_persisted(row(state).into()),
                Err(crate::users::errors::UserError::UserStateMismatch { .. })
            ));
        }
    }
}
