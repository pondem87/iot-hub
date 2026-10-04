//! Unused registration transport scaffolding; mandatory input rules remain unresolved.
use serde::{Deserialize, Serialize};

/// Proposed registration input; this does not establish required business fields.
#[derive(Clone, Deserialize)]
pub struct CreateUserInput {
    /// Phone number in the existing scaffold transport representation.
    pub phone_number: String,
    /// Profile name in the existing scaffold transport representation.
    pub name: String,
    /// Unvalidated password input; do not include it in diagnostics.
    pub password: String,
}

/// Proposed registration response, separate from trusted domain objects.
#[derive(Debug, Clone, Deserialize, Serialize)]
pub struct CreateUserOutput {
    /// Account identifier in the existing scaffold response representation.
    pub id: String,
    /// Phone number in the existing scaffold transport representation.
    pub phone_number: String,
    /// Profile name in the existing scaffold transport representation.
    pub name: String,
}
#[cfg(test)]
mod tests {
    //! Tests registration scaffold response encoding.
    //! # Test plan
    //! - `serializes_response`: preserves the proposed response keys.
    use super::*;
    #[test]
    fn serializes_response() {
        let value = CreateUserOutput {
            id: "id".into(),
            phone_number: "phone".into(),
            name: "name".into(),
        };
        assert_eq!(
            serde_json::to_value(value).unwrap(),
            serde_json::json!({"id":"id", "phone_number":"phone", "name":"name"})
        );
    }
}
