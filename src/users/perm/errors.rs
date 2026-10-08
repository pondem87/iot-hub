//! Errors produced while decoding user-domain permissions.

use thiserror::Error;

/// Invalid data that cannot be converted into a user permission.
#[derive(Debug, Error, PartialEq, Eq)]
pub enum UserPermissionDataError {
    /// An object scope has no target identifier.
    #[error("object permission requires a resource ID")]
    MissingResourceId,
    /// A collection scope incorrectly specifies a target identifier.
    #[error("collection permission must not contain a resource ID")]
    UnexpectedResourceId,
    /// The attribute selection does not match the documented JSON representation.
    #[error("invalid permission attributes")]
    InvalidAttributes,
}

/// Storage outcomes exposed without coupling callers to SQLx.
#[derive(Debug, Error, PartialEq, Eq)]
pub enum UserPermissionsRepositoryError {
    /// The database could not be reached or the connection pool is closed.
    #[error("permission storage unavailable")]
    Unavailable,
    /// A matching row could not be decoded or validated.
    #[error("invalid stored permission")]
    InvalidData,
    /// The requested storage operation failed for another reason.
    #[error("permission storage operation failed")]
    Unexpected,
}

#[cfg(test)]
mod tests {
    //! Tests errors returned by checked user permission construction.
    //!
    //! # Test plan
    //! - `conversion_errors_have_safe_messages`: checks every variant's safe
    //!   display and absence of infrastructure error sources.
    //! - `repository_errors_have_safe_messages`: covers each storage outcome.
    //! - `legacy_import_preserves_variants`: verifies the model re-export
    //!   preserves structured matching. Conversion behavior is tested in models.

    use std::error::Error;

    use super::*;

    #[test]
    fn conversion_errors_have_safe_messages() {
        for (error, expected) in [
            (
                UserPermissionDataError::MissingResourceId,
                "object permission requires a resource ID",
            ),
            (
                UserPermissionDataError::UnexpectedResourceId,
                "collection permission must not contain a resource ID",
            ),
            (
                UserPermissionDataError::InvalidAttributes,
                "invalid permission attributes",
            ),
        ] {
            assert_eq!(error.to_string(), expected);
            assert!(error.source().is_none());
        }
    }

    #[test]
    fn legacy_import_preserves_variants() {
        let error: UserPermissionDataError =
            super::super::models::UserPermissionDataError::InvalidAttributes;

        assert!(matches!(error, UserPermissionDataError::InvalidAttributes));
    }

    #[test]
    fn repository_errors_have_safe_messages() {
        for (error, message) in [
            (
                UserPermissionsRepositoryError::Unavailable,
                "permission storage unavailable",
            ),
            (
                UserPermissionsRepositoryError::InvalidData,
                "invalid stored permission",
            ),
            (
                UserPermissionsRepositoryError::Unexpected,
                "permission storage operation failed",
            ),
        ] {
            assert_eq!(error.to_string(), message);
            assert!(error.source().is_none());
        }
    }
}
