//! Technical error translation for PostgreSQL adapters.

use crate::users::errors::UserRepositoryError;

/// Converts technical failures to storage outcomes without exposing SQLx in contracts.
pub(super) fn storage_error(error: sqlx::Error) -> UserRepositoryError {
    match error {
        cause @ (sqlx::Error::Io(_)
        | sqlx::Error::Tls(_)
        | sqlx::Error::PoolTimedOut
        | sqlx::Error::PoolClosed) => UserRepositoryError::Unavailable {
            cause: Box::new(cause),
        },
        cause @ (sqlx::Error::ColumnDecode { .. }
        | sqlx::Error::Decode(_)
        | sqlx::Error::ColumnNotFound(_)
        | sqlx::Error::TypeNotFound { .. }) => UserRepositoryError::InvalidData {
            cause: Box::new(cause),
        },
        cause => UserRepositoryError::Unexpected {
            cause: Box::new(cause),
        },
    }
}

#[cfg(test)]
mod tests {
    //! Tests structured SQL error translation.
    //! # Test plan
    //! - `translates_sql_failures`: distinguishes unavailable, malformed, and unexpected failures.
    use super::*;
    #[test]
    fn translates_sql_failures() {
        assert!(matches!(
            storage_error(sqlx::Error::PoolClosed),
            UserRepositoryError::Unavailable { .. }
        ));
        assert!(matches!(
            storage_error(sqlx::Error::Decode(Box::new(std::io::Error::other(
                "decode"
            )))),
            UserRepositoryError::InvalidData { .. }
        ));
        assert!(matches!(
            storage_error(sqlx::Error::RowNotFound),
            UserRepositoryError::Unexpected { .. }
        ));
    }
}
