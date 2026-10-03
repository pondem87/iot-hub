//! Required contact read coordination.

use sqlx::types::Uuid;

use crate::users::{
    errors::UserReadError,
    models::{ActiveContact, StoredContact, UserContact},
    traits::{UserContactReads, UserContactStore},
};

/// Coordinates contact reads through an injected persistence contract.
pub struct UserContactService<R> {
    repository: R,
}
impl<R> UserContactService<R> {
    /// Creates the service without opening connections or changing data.
    pub fn new(repository: R) -> Self {
        Self { repository }
    }
}
impl<R: UserContactStore> UserContactReads for UserContactService<R> {
    async fn get_contact(
        &self,
        user_id: Uuid,
        contact_id: Uuid,
    ) -> Result<StoredContact, UserReadError> {
        self.repository
            .get_contact(user_id, contact_id)
            .await?
            .ok_or(UserReadError::NotFound)
    }
    async fn get_active_contact(
        &self,
        user_id: Uuid,
        contact_id: Uuid,
    ) -> Result<UserContact<ActiveContact>, UserReadError> {
        self.repository
            .get_active_contact(user_id, contact_id)
            .await?
            .ok_or(UserReadError::NotFound)
    }
}
#[cfg(test)]
mod tests {
    //! Tests contact reads with deterministic injected persistence outcomes.
    //! # Test plan
    //! - `reports_absence`: translates normal absence to required-object failure.
    //! - `preserves_success`: returns stored information and forwards identity scope.
    //! - `translates_failures`: preserves every structured failure category.
    //! - `active_lookup_preserves_outcomes`: preserves success, absence and mismatch for active reads.
    use super::*;
    use crate::users::errors::UserRepositoryError;
    use crate::users::models::*;
    /// One outcome generated deterministically for each lookup.
    #[derive(Clone, Copy)]
    enum Outcome {
        Success,
        Absent,
        Unavailable,
        InvalidData,
        Unexpected,
        UserMismatch,
        ContactMismatch,
    }
    /// Repository double that asserts identity arguments and supplies controlled outcomes.
    struct FakeStore {
        outcome: Outcome,
    }
    fn failure(outcome: Outcome) -> UserRepositoryError {
        let cause = || {
            Box::new(std::io::Error::other("private diagnostic"))
                as Box<dyn std::error::Error + Send + Sync>
        };
        match outcome {
            Outcome::Unavailable => UserRepositoryError::Unavailable { cause: cause() },
            Outcome::InvalidData => UserRepositoryError::InvalidData { cause: cause() },
            Outcome::Unexpected => UserRepositoryError::Unexpected { cause: cause() },
            Outcome::UserMismatch => UserRepositoryError::UserStateMismatch {
                expected: UserState::Active,
                actual: UserState::Barred,
            },
            Outcome::ContactMismatch => UserRepositoryError::ContactStateMismatch {
                expected: UserContactState::Active,
                actual: UserContactState::Disabled,
            },
            _ => unreachable!("only failure outcomes reach the failure fixture"),
        }
    }
    fn active() -> UserContact<ActiveContact> {
        UserContact::<ActiveContact>::from_persisted(crate::users::models::ContactData {
            id: Uuid::from_u128(1),
            contact_type: UserContactType::PhoneNumber,
            value: "phone".into(),
            state: UserContactState::Active,
            user_id: Uuid::nil(),
            created_at: chrono::Utc::now(),
            updated_at: chrono::Utc::now(),
        })
        .unwrap()
    }
    impl UserContactStore for FakeStore {
        async fn get_contact(
            &self,
            user_id: Uuid,
            contact_id: Uuid,
        ) -> Result<Option<StoredContact>, UserRepositoryError> {
            assert_eq!(user_id, Uuid::nil());
            assert_eq!(contact_id, Uuid::from_u128(1));
            match self.outcome {
                Outcome::Success => Ok(Some(StoredContact::Active(active()))),
                Outcome::Absent => Ok(None),
                failure_outcome => Err(failure(failure_outcome)),
            }
        }
        async fn get_active_contact(
            &self,
            user_id: Uuid,
            contact_id: Uuid,
        ) -> Result<Option<UserContact<ActiveContact>>, UserRepositoryError> {
            assert_eq!(user_id, Uuid::nil());
            assert_eq!(contact_id, Uuid::from_u128(1));
            match self.outcome {
                Outcome::Success => Ok(Some(active())),
                Outcome::Absent => Ok(None),
                failure_outcome => Err(failure(failure_outcome)),
            }
        }
    }
    #[tokio::test]
    async fn reports_absence() {
        let service = UserContactService::new(FakeStore {
            outcome: Outcome::Absent,
        });
        assert!(matches!(
            service.get_contact(Uuid::nil(), Uuid::from_u128(1)).await,
            Err(UserReadError::NotFound)
        ));
    }
    #[tokio::test]
    async fn preserves_success() {
        let service = UserContactService::new(FakeStore {
            outcome: Outcome::Success,
        });
        let value = service
            .get_contact(Uuid::nil(), Uuid::from_u128(1))
            .await
            .unwrap();
        assert!(
            matches!(value, StoredContact::Active(contact) if contact.id() == Uuid::from_u128(1) && contact.user_id() == Uuid::nil() && contact.value() == "phone")
        );
    }
    #[tokio::test]
    async fn translates_failures() {
        for outcome in [
            Outcome::Unavailable,
            Outcome::InvalidData,
            Outcome::Unexpected,
            Outcome::UserMismatch,
            Outcome::ContactMismatch,
        ] {
            let service = UserContactService::new(FakeStore { outcome });
            let error = service
                .get_contact(Uuid::nil(), Uuid::from_u128(1))
                .await
                .unwrap_err();
            match outcome {
                Outcome::Unavailable => assert!(matches!(error, UserReadError::Unavailable { .. })),
                Outcome::InvalidData => assert!(matches!(error, UserReadError::InvalidData { .. })),
                Outcome::Unexpected => {
                    assert!(matches!(error, UserReadError::PersistenceFailure { .. }))
                }
                Outcome::UserMismatch => assert!(matches!(
                    error,
                    UserReadError::UserStateMismatch {
                        expected: UserState::Active,
                        actual: UserState::Barred
                    }
                )),
                Outcome::ContactMismatch => assert!(matches!(
                    error,
                    UserReadError::ContactStateMismatch {
                        expected: UserContactState::Active,
                        actual: UserContactState::Disabled
                    }
                )),
                _ => unreachable!("only failure outcomes are exercised"),
            }
            assert!(!error.to_string().contains("private diagnostic"));
        }
    }
    #[tokio::test]
    async fn active_lookup_preserves_outcomes() {
        let success = UserContactService::new(FakeStore {
            outcome: Outcome::Success,
        })
        .get_active_contact(Uuid::nil(), Uuid::from_u128(1))
        .await
        .unwrap();
        assert_eq!(success.state(), UserContactState::Active);
        assert!(matches!(
            UserContactService::new(FakeStore {
                outcome: Outcome::Absent
            })
            .get_active_contact(Uuid::nil(), Uuid::from_u128(1))
            .await,
            Err(UserReadError::NotFound)
        ));
        assert!(matches!(
            UserContactService::new(FakeStore {
                outcome: Outcome::ContactMismatch
            })
            .get_active_contact(Uuid::nil(), Uuid::from_u128(1))
            .await,
            Err(UserReadError::ContactStateMismatch { .. })
        ));
    }
}
