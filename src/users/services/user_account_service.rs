//! Required account read coordination.

use sqlx::types::Uuid;

use super::UserAccountReads;

use crate::users::{
    errors::UserReadError,
    models::{ActiveUser, StoredUser, User},
    repositories::UserStore,
};

/// Coordinates account reads through an injected persistence contract.
pub struct UserAccountService<R> {
    repository: R,
}

impl<R> UserAccountService<R> {
    /// Creates the service without opening connections or changing data.
    pub fn new(repository: R) -> Self {
        Self { repository }
    }
}

impl<R: UserStore> UserAccountReads for UserAccountService<R> {
    async fn get_user_by_id(&self, id: Uuid) -> Result<StoredUser, UserReadError> {
        self.repository
            .get_user_by_id(id)
            .await?
            .ok_or(UserReadError::NotFound)
    }

    async fn get_active_user(&self, id: Uuid) -> Result<User<ActiveUser>, UserReadError> {
        self.repository
            .get_active_user(id)
            .await?
            .ok_or(UserReadError::NotFound)
    }
}

#[cfg(test)]
mod tests {
    //! Tests account reads with deterministic injected persistence outcomes.
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

    fn active() -> User<ActiveUser> {
        User::<ActiveUser>::from_persisted(crate::users::models::UserData {
            id: Uuid::nil(),
            phone_number: "phone".into(),
            user_type: UserType::Customer,
            state: UserState::Active,
            profile_id: Uuid::nil(),
            preferences_id: Uuid::nil(),
            created_at: chrono::Utc::now(),
            updated_at: chrono::Utc::now(),
        })
        .unwrap()
    }

    impl UserStore for FakeStore {
        async fn get_user_by_id(
            &self,
            id: Uuid,
        ) -> Result<Option<StoredUser>, UserRepositoryError> {
            assert_eq!(id, Uuid::nil());
            match self.outcome {
                Outcome::Success => Ok(Some(StoredUser::Active(active()))),
                Outcome::Absent => Ok(None),
                failure_outcome => Err(failure(failure_outcome)),
            }
        }

        async fn get_active_user(
            &self,
            id: Uuid,
        ) -> Result<Option<User<ActiveUser>>, UserRepositoryError> {
            assert_eq!(id, Uuid::nil());
            match self.outcome {
                Outcome::Success => Ok(Some(active())),
                Outcome::Absent => Ok(None),
                failure_outcome => Err(failure(failure_outcome)),
            }
        }
    }

    #[tokio::test]
    async fn reports_absence() {
        let service = UserAccountService::new(FakeStore {
            outcome: Outcome::Absent,
        });
        assert!(matches!(
            service.get_user_by_id(Uuid::nil()).await,
            Err(UserReadError::NotFound)
        ));
    }

    #[tokio::test]
    async fn preserves_success() {
        let service = UserAccountService::new(FakeStore {
            outcome: Outcome::Success,
        });
        let value = service.get_user_by_id(Uuid::nil()).await.unwrap();
        assert!(
            matches!(value, StoredUser::Active(user) if user.id() == Uuid::nil() && user.phone_number() == "phone")
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
            let service = UserAccountService::new(FakeStore { outcome });
            let error = service.get_user_by_id(Uuid::nil()).await.unwrap_err();
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
        let success = UserAccountService::new(FakeStore {
            outcome: Outcome::Success,
        })
        .get_active_user(Uuid::nil())
        .await
        .unwrap();
        assert_eq!(success.state(), UserState::Active);
        assert!(matches!(
            UserAccountService::new(FakeStore {
                outcome: Outcome::Absent
            })
            .get_active_user(Uuid::nil())
            .await,
            Err(UserReadError::NotFound)
        ));
        assert!(matches!(
            UserAccountService::new(FakeStore {
                outcome: Outcome::UserMismatch
            })
            .get_active_user(Uuid::nil())
            .await,
            Err(UserReadError::UserStateMismatch { .. })
        ));
    }
}
