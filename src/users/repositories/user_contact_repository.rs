//! PostgreSQL contact reads within the owning identity scope.

use sqlx::{PgPool, types::Uuid};

use super::rows::{ContactRow, storage_error};
use crate::users::{
    errors::UserRepositoryError,
    models::{ActiveContact, StoredContact, UserContact},
    traits::UserContactStore,
};

/// Reads persisted contact records using an injected, cheaply cloned connection pool.
pub struct UserContactRepository {
    pool: PgPool,
}
impl UserContactRepository {
    /// Creates the adapter without establishing a new connection.
    pub fn new(pool: PgPool) -> Self {
        Self { pool }
    }
    async fn load(
        &self,
        user_id: Uuid,
        contact_id: Uuid,
    ) -> Result<Option<ContactRow>, UserRepositoryError> {
        sqlx::query_as::<_, ContactRow>("SELECT id, user_contact_type, value, states, user_id, created_at, updated_at FROM user_contacts WHERE user_id = $1 AND id = $2").bind(user_id).bind(contact_id).fetch_optional(&self.pool).await.map_err(storage_error)
    }
}
impl UserContactStore for UserContactRepository {
    async fn get_contact(
        &self,
        user_id: Uuid,
        contact_id: Uuid,
    ) -> Result<Option<StoredContact>, UserRepositoryError> {
        self.load(user_id, contact_id)
            .await?
            .map(ContactRow::validate)
            .transpose()
    }
    async fn get_active_contact(
        &self,
        user_id: Uuid,
        contact_id: Uuid,
    ) -> Result<Option<UserContact<ActiveContact>>, UserRepositoryError> {
        self.load(user_id, contact_id)
            .await?
            .map(|row| UserContact::<ActiveContact>::from_persisted(row.into()).map_err(Into::into))
            .transpose()
    }
}
#[cfg(test)]
mod tests {
    //! Tests contact persistence conversion and disconnected storage.
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
        let repository = UserContactRepository::new(pool);
        assert!(matches!(
            repository.get_contact(Uuid::nil(), Uuid::nil()).await,
            Err(UserRepositoryError::Unavailable { .. })
        ));
    }
    use super::super::rows::*;
    fn row(state: StoredContactState) -> ContactRow {
        ContactRow {
            id: Uuid::nil(),
            contact_type: StoredContactType::PhoneNumber,
            value: "test".into(),
            state,
            user_id: Uuid::nil(),
            created_at: chrono::Utc::now(),
            updated_at: chrono::Utc::now(),
        }
    }
    #[test]
    fn validates_every_state() {
        assert!(matches!(
            row(StoredContactState::Unverified).validate().unwrap(),
            StoredContact::Unverified(_)
        ));
        assert!(matches!(
            row(StoredContactState::Active).validate().unwrap(),
            StoredContact::Active(_)
        ));
        assert!(matches!(
            row(StoredContactState::Disabled).validate().unwrap(),
            StoredContact::Disabled(_)
        ));
    }
    #[test]
    fn rejects_wrong_active_state() {
        for state in [StoredContactState::Unverified, StoredContactState::Disabled] {
            assert!(matches!(
                UserContact::<ActiveContact>::from_persisted(row(state).into()),
                Err(crate::users::errors::UserError::ContactStateMismatch { .. })
            ));
        }
    }
}
