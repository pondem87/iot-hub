//! Errors exposed by permission contracts.

use thiserror::Error;

/// The requested permission operation was denied by the provider's policy.
///
/// This is a business outcome, not a representation of an infrastructure failure.
/// It carries no principal identifiers, policy internals, or technical causes.
#[derive(Debug, Error, PartialEq, Eq)]
#[error("permission denied")]
pub struct UnauthorizedError;

/// Outcomes of a permission provider operation, independent of its storage technology.
#[derive(Debug, Error, PartialEq, Eq)]
pub enum PermissionProviderError {
    /// No matching grant authorizes the requested operation and attributes.
    #[error(transparent)]
    Denied(#[from] UnauthorizedError),
    /// The requested attributes do not belong to the selected resource.
    #[error("invalid permission request")]
    InvalidRequest,
    /// Permission storage is temporarily unavailable.
    #[error("permission service unavailable")]
    Unavailable,
    /// Stored permissions could not be interpreted safely.
    #[error("invalid stored permission")]
    InvalidData,
    /// The permission operation failed for another operational reason.
    #[error("permission operation failed")]
    Unexpected,
}

#[cfg(test)]
mod tests {
    //! Tests the authorization denial error exposed to permission consumers.
    //!
    //! # Test plan
    //! - `denial_has_safe_error_representation`: verifies the standard error
    //!   contract and the absence of technical diagnostics in its message/source.
    //! - `provider_errors_remain_distinct`: verifies denial conversion and safe operational errors.
    //! - `legacy_import_preserves_error_identity`: verifies the app re-export
    //!   names the same matchable error type.

    use std::error::Error;

    use super::*;

    #[test]
    fn denial_has_safe_error_representation() {
        let error: &dyn Error = &UnauthorizedError;

        assert_eq!(error.to_string(), "permission denied");
        assert!(error.source().is_none());
    }

    #[test]
    fn legacy_import_preserves_error_identity() {
        let error: UnauthorizedError = crate::permissions::app::UnauthorizedError;

        assert!(matches!(error, UnauthorizedError));
    }

    #[test]
    fn provider_errors_remain_distinct() {
        assert_eq!(
            PermissionProviderError::from(UnauthorizedError),
            PermissionProviderError::Denied(UnauthorizedError)
        );
        for (error, message) in [
            (
                PermissionProviderError::InvalidRequest,
                "invalid permission request",
            ),
            (
                PermissionProviderError::Unavailable,
                "permission service unavailable",
            ),
            (
                PermissionProviderError::InvalidData,
                "invalid stored permission",
            ),
            (
                PermissionProviderError::Unexpected,
                "permission operation failed",
            ),
        ] {
            assert_eq!(error.to_string(), message);
            assert!(error.source().is_none());
        }
    }
}
