//! Required profile read coordination.

use sqlx::types::Uuid;

use crate::users::{
    errors::UserReadError,
    models::UserProfile,
    traits::{UserProfileReads, UserProfileStore},
};

/// Coordinates profile reads through an injected persistence contract.
pub struct UserProfileService<R> {
    repository: R,
}
impl<R> UserProfileService<R> {
    /// Creates the service without opening connections or changing data.
    pub fn new(repository: R) -> Self {
        Self { repository }
    }
}
impl<R: UserProfileStore> UserProfileReads for UserProfileService<R> {
    async fn get_profile(&self, user_id: Uuid) -> Result<UserProfile, UserReadError> {
        self.repository
            .get_profile(user_id)
            .await?
            .ok_or(UserReadError::NotFound)
    }
}
#[cfg(test)]
mod tests {
    //! Tests profile reads with deterministic injected persistence outcomes.
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
    impl UserProfileStore for FakeStore {
        async fn get_profile(
            &self,
            user_id: Uuid,
        ) -> Result<Option<UserProfile>, UserRepositoryError> {
            assert_eq!(user_id, Uuid::nil());
            match self.outcome {
                Outcome::Success => Ok(Some(UserProfile {
                    id: Uuid::from_u128(2),
                    name: "name".into(),
                    updated_at: chrono::Utc::now(),
                })),
                Outcome::Absent => Ok(None),
                failure_outcome => Err(failure(failure_outcome)),
            }
        }
    }
    #[tokio::test]
    async fn reports_absence() {
        let service = UserProfileService::new(FakeStore {
            outcome: Outcome::Absent,
        });
        assert!(matches!(
            service.get_profile(Uuid::nil()).await,
            Err(UserReadError::NotFound)
        ));
    }
    #[tokio::test]
    async fn preserves_success() {
        let service = UserProfileService::new(FakeStore {
            outcome: Outcome::Success,
        });
        let value = service.get_profile(Uuid::nil()).await.unwrap();
        assert_eq!(value.name, "name");
        assert_eq!(value.id, Uuid::from_u128(2));
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
            let service = UserProfileService::new(FakeStore { outcome });
            let error = service.get_profile(Uuid::nil()).await.unwrap_err();
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
