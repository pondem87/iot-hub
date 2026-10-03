//! Structured persistence and application read outcomes.

use std::error::Error;

use thiserror::Error;

use super::models::{UserContactState, UserState};

/// Lifecycle validation failure, independent of persistence and transport.
#[derive(Debug, Error)]
pub enum UserError {
    /// Loaded user data does not establish the requested typestate.
    #[error("unexpected user state")]
    UserStateMismatch {
        /// Lifecycle required by the returned typestate.
        expected: UserState,
        /// Lifecycle found in the requested row.
        actual: UserState,
    },
    /// Loaded contact data does not establish the requested typestate.
    #[error("unexpected contact state")]
    ContactStateMismatch {
        /// Contact lifecycle required by the returned typestate.
        expected: UserContactState,
        /// Contact lifecycle found in the requested row.
        actual: UserContactState,
    },
}

/// Adapter-independent failure while retrieving user information.
#[derive(Debug, Error)]
pub enum UserRepositoryError {
    /// Storage could not be reached.
    #[error("user storage unavailable")]
    Unavailable {
        /// Internal diagnostic source; never serialize it for external consumers.
        #[source]
        cause: Box<dyn Error + Send + Sync>,
    },
    /// Stored data could not be decoded.
    #[error("invalid stored user information")]
    InvalidData {
        /// Internal diagnostic source; never serialize it for external consumers.
        #[source]
        cause: Box<dyn Error + Send + Sync>,
    },
    /// A user exists in a different lifecycle state.
    #[error("unexpected user state")]
    UserStateMismatch {
        /// Lifecycle required by the returned typestate.
        expected: UserState,
        /// Lifecycle found in the requested row.
        actual: UserState,
    },
    /// A contact exists in a different lifecycle state.
    #[error("unexpected contact state")]
    ContactStateMismatch {
        /// Contact lifecycle required by the returned typestate.
        expected: UserContactState,
        /// Contact lifecycle found in the requested row.
        actual: UserContactState,
    },
    /// An unexpected storage operation failed.
    #[error("failed to retrieve user information")]
    Unexpected {
        /// Internal diagnostic source; never serialize it for external consumers.
        #[source]
        cause: Box<dyn Error + Send + Sync>,
    },
}

/// Failure to retrieve required user information through a service.
#[derive(Debug, Error)]
pub enum UserReadError {
    /// No object exists within the requested lookup scope.
    #[error("user information not found")]
    NotFound,
    /// A user exists in a different lifecycle state.
    #[error("unexpected user state")]
    UserStateMismatch {
        /// Lifecycle required by the returned typestate.
        expected: UserState,
        /// Lifecycle found in the requested row.
        actual: UserState,
    },
    /// A contact exists in a different lifecycle state.
    #[error("unexpected contact state")]
    ContactStateMismatch {
        /// Contact lifecycle required by the returned typestate.
        expected: UserContactState,
        /// Contact lifecycle found in the requested row.
        actual: UserContactState,
    },
    /// Storage is unavailable.
    #[error("user information temporarily unavailable")]
    Unavailable {
        /// Internal persistence context; never serialize its source chain.
        #[source]
        cause: UserRepositoryError,
    },
    /// Stored data is invalid.
    #[error("invalid stored user information")]
    InvalidData {
        /// Internal persistence context; never serialize its source chain.
        #[source]
        cause: UserRepositoryError,
    },
    /// An unexpected persistence failure occurred.
    #[error("failed to retrieve user information")]
    PersistenceFailure {
        /// Internal persistence context; never serialize its source chain.
        #[source]
        cause: UserRepositoryError,
    },
}
impl From<UserRepositoryError> for UserReadError {
    fn from(error: UserRepositoryError) -> Self {
        match error {
            UserRepositoryError::UserStateMismatch { expected, actual } => {
                Self::UserStateMismatch { expected, actual }
            }
            UserRepositoryError::ContactStateMismatch { expected, actual } => {
                Self::ContactStateMismatch { expected, actual }
            }
            cause @ UserRepositoryError::Unavailable { .. } => Self::Unavailable { cause },
            cause @ UserRepositoryError::InvalidData { .. } => Self::InvalidData { cause },
            cause => Self::PersistenceFailure { cause },
        }
    }
}
impl From<UserError> for UserRepositoryError {
    fn from(error: UserError) -> Self {
        match error {
            UserError::UserStateMismatch { expected, actual } => {
                Self::UserStateMismatch { expected, actual }
            }
            UserError::ContactStateMismatch { expected, actual } => {
                Self::ContactStateMismatch { expected, actual }
            }
        }
    }
}
#[cfg(test)]
mod tests {
    //! Tests read error translation.
    //! # Test plan
    //! - `translates_domain_mismatch`: preserves domain outcomes at the repository boundary.
    //! - `preserves_state_mismatch`: retains expected and actual states.
    //! - `translates_storage_failures`: retains distinct operational outcomes.
    use super::*;
    #[test]
    fn translates_domain_mismatch() {
        assert!(matches!(
            UserRepositoryError::from(UserError::UserStateMismatch {
                expected: UserState::Active,
                actual: UserState::Inactive
            }),
            UserRepositoryError::UserStateMismatch {
                expected: UserState::Active,
                actual: UserState::Inactive
            }
        ));
        assert!(matches!(
            UserRepositoryError::from(UserError::ContactStateMismatch {
                expected: UserContactState::Active,
                actual: UserContactState::Disabled
            }),
            UserRepositoryError::ContactStateMismatch {
                expected: UserContactState::Active,
                actual: UserContactState::Disabled
            }
        ));
    }
    #[test]
    fn preserves_state_mismatch() {
        assert!(matches!(
            UserReadError::from(UserRepositoryError::UserStateMismatch {
                expected: UserState::Active,
                actual: UserState::Deleted
            }),
            UserReadError::UserStateMismatch {
                expected: UserState::Active,
                actual: UserState::Deleted
            }
        ));
    }
    #[test]
    fn translates_storage_failures() {
        let cause = || {
            Box::new(std::io::Error::other("internal diagnostic")) as Box<dyn Error + Send + Sync>
        };
        assert!(matches!(
            UserReadError::from(UserRepositoryError::Unavailable { cause: cause() }),
            UserReadError::Unavailable { .. }
        ));
        assert!(matches!(
            UserReadError::from(UserRepositoryError::InvalidData { cause: cause() }),
            UserReadError::InvalidData { .. }
        ));
        let error = UserReadError::from(UserRepositoryError::Unexpected { cause: cause() });
        assert!(matches!(error, UserReadError::PersistenceFailure { .. }));
        assert!(!error.to_string().contains("internal diagnostic"));
    }
}
