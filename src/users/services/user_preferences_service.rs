//! Required preferences read coordination.

use sqlx::types::Uuid;

use super::UserPreferencesReads;

use crate::users::{
    errors::UserReadError, models::UserPreferences, repositories::UserPreferencesStore,
};

/// Coordinates preferences reads through an injected persistence contract.
pub struct UserPreferencesService<R> {
    repository: R,
}

impl<R> UserPreferencesService<R> {
    /// Creates the service without opening connections or changing data.
    pub fn new(repository: R) -> Self {
        Self { repository }
    }
}

impl<R: UserPreferencesStore> UserPreferencesReads for UserPreferencesService<R> {
    async fn get_preferences(&self, user_id: Uuid) -> Result<UserPreferences, UserReadError> {
        self.repository
            .get_preferences(user_id)
            .await?
            .ok_or(UserReadError::NotFound)
    }
}

#[cfg(test)]
mod tests {
    //! Tests preferences reads with deterministic injected persistence outcomes.
    //! # Test plan
    //! - `reports_absence`: translates normal absence to required-object failure.
    //! - `preserves_success`: returns stored information and forwards identity scope.
    //! - `translates_failures`: preserves every structured failure category.

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

    impl UserPreferencesStore for FakeStore {
        async fn get_preferences(
            &self,
            user_id: Uuid,
        ) -> Result<Option<UserPreferences>, UserRepositoryError> {
            assert_eq!(user_id, Uuid::nil());
            match self.outcome {
                Outcome::Success => Ok(Some(UserPreferences {
                    id: Uuid::from_u128(3),
                    allow_notifications: true,
                    updated_at: chrono::Utc::now(),
                })),
                Outcome::Absent => Ok(None),
                failure_outcome => Err(failure(failure_outcome)),
            }
        }
    }

    #[tokio::test]
    async fn reports_absence() {
        let service = UserPreferencesService::new(FakeStore {
            outcome: Outcome::Absent,
        });
        assert!(matches!(
            service.get_preferences(Uuid::nil()).await,
            Err(UserReadError::NotFound)
        ));
    }

    #[tokio::test]
    async fn preserves_success() {
        let service = UserPreferencesService::new(FakeStore {
            outcome: Outcome::Success,
        });
        let value = service.get_preferences(Uuid::nil()).await.unwrap();
        assert!(value.allow_notifications);
        assert_eq!(value.id, Uuid::from_u128(3));
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
            let service = UserPreferencesService::new(FakeStore { outcome });
            let error = service.get_preferences(Uuid::nil()).await.unwrap_err();
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
}
