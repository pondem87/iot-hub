//! Existing user event wire contracts; no workflow is implied by these definitions.

use chrono::{DateTime, Utc};
use serde::{Deserialize, Serialize};

use crate::events::{
    errors::EventPayloadError,
    traits::{Event, EventPayload},
};

// user created event
/// Stable topic identifying this occurrence.
pub const TOPIC_USER_CREATED: &str = "users/user_created";

#[derive(Clone, Serialize, Deserialize)]
/// Existing wire representation for UserCreatedEvent; redact payloads from diagnostics.
pub struct UserCreatedEvent {
    topic: String,
    name: String,
    timestamp: DateTime<Utc>,
    payload: String,
}

#[derive(Clone, Serialize, Deserialize)]
/// Existing wire representation for UserCreatedPayload; redact payloads from diagnostics.
pub struct UserCreatedPayload {
    user_id: String,
    phone_number: String,
}

impl EventPayload for UserCreatedPayload {}

impl UserCreatedPayload {
    /// Constructs or reads the existing wire representation.
    pub fn new(user_id: String, phone_number: String) -> Self {
        Self {
            user_id,
            phone_number,
        }
    }

    /// Constructs or reads the existing wire representation.
    pub fn get_user_id(&self) -> &str {
        &self.user_id
    }

    /// Constructs or reads the existing wire representation.
    pub fn get_phone_number(&self) -> &str {
        &self.phone_number
    }
}

impl UserCreatedEvent {
    /// Constructs an event after serializing its payload.
    /// # Errors
    /// Returns a JSON serialization failure; callers must handle it before publishing.
    pub fn new(payload: &UserCreatedPayload) -> Result<Self, EventPayloadError> {
        Ok(Self {
            topic: String::from(TOPIC_USER_CREATED),
            name: String::from("UserCreatedEvent"),
            timestamp: chrono::Utc::now(),
            payload: payload.serialise()?,
        })
    }
}

impl Event for UserCreatedEvent {
    fn name(&self) -> &str {
        &self.name
    }

    fn timestamp(&self) -> DateTime<Utc> {
        self.timestamp
    }

    fn payload(&self) -> &str {
        &self.payload
    }
}

// user verified event
/// Stable topic identifying this occurrence.
pub const TOPIC_USER_VERIFIED: &str = "users/user_verified";

#[derive(Clone, Serialize, Deserialize)]
/// Existing wire representation for UserVerifiedEvent; redact payloads from diagnostics.
pub struct UserVerifiedEvent {
    topic: String,
    name: String,
    timestamp: DateTime<Utc>,
    payload: String,
}

#[derive(Clone, Serialize, Deserialize)]
/// Existing wire representation for UserVerifiedPayload; redact payloads from diagnostics.
pub struct UserVerifiedPayload {
    user_id: String,
    phone_number: String,
}

impl EventPayload for UserVerifiedPayload {}

impl UserVerifiedPayload {
    /// Constructs or reads the existing wire representation.
    pub fn new(user_id: String, phone_number: String) -> Self {
        Self {
            user_id,
            phone_number,
        }
    }

    /// Constructs or reads the existing wire representation.
    pub fn get_user_id(&self) -> &str {
        &self.user_id
    }

    /// Constructs or reads the existing wire representation.
    pub fn get_phone_number(&self) -> &str {
        &self.phone_number
    }
}

impl UserVerifiedEvent {
    /// Constructs an event after serializing its payload.
    /// # Errors
    /// Returns a JSON serialization failure; callers must handle it before publishing.
    pub fn new(payload: &UserVerifiedPayload) -> Result<Self, EventPayloadError> {
        Ok(Self {
            topic: String::from(TOPIC_USER_VERIFIED),
            name: String::from("UserVerifiedEvent"),
            timestamp: chrono::Utc::now(),
            payload: payload.serialise()?,
        })
    }
}

impl Event for UserVerifiedEvent {
    fn name(&self) -> &str {
        &self.name
    }

    fn timestamp(&self) -> DateTime<Utc> {
        self.timestamp
    }

    fn payload(&self) -> &str {
        &self.payload
    }
}

// verification code created event
/// Stable topic identifying this occurrence.
pub const TOPIC_VERIFICATION_CODE_CREATED: &str = "users/verification_code_created";

#[derive(Clone, Serialize, Deserialize)]
/// Existing wire representation for VerificationCodeCreatedEvent; redact payloads from diagnostics.
pub struct VerificationCodeCreatedEvent {
    topic: String,
    name: String,
    timestamp: DateTime<Utc>,
    payload: String,
}

#[derive(Clone, Serialize, Deserialize)]
/// Existing wire representation for VerificationCodeCreatedPayload; redact payloads from diagnostics.
pub struct VerificationCodeCreatedPayload {
    user_id: String,
    phone_number: String,
    verification_code: String,
}

impl EventPayload for VerificationCodeCreatedPayload {}

impl VerificationCodeCreatedPayload {
    /// Constructs or reads the existing wire representation.
    pub fn new(user_id: String, phone_number: String, verification_code: String) -> Self {
        Self {
            user_id,
            phone_number,
            verification_code,
        }
    }

    /// Constructs or reads the existing wire representation.
    pub fn get_user_id(&self) -> &str {
        &self.user_id
    }

    /// Constructs or reads the existing wire representation.
    pub fn get_phone_number(&self) -> &str {
        &self.phone_number
    }

    /// Constructs or reads the existing wire representation.
    pub fn get_verification_code(&self) -> &str {
        &self.verification_code
    }
}

impl VerificationCodeCreatedEvent {
    /// Constructs an event after serializing its payload.
    /// # Errors
    /// Returns a JSON serialization failure; callers must handle it before publishing.
    pub fn new(payload: &VerificationCodeCreatedPayload) -> Result<Self, EventPayloadError> {
        Ok(Self {
            topic: String::from(TOPIC_VERIFICATION_CODE_CREATED),
            name: String::from("VerificationCodeCreatedEvent"),
            timestamp: chrono::Utc::now(),
            payload: payload.serialise()?,
        })
    }
}

impl Event for VerificationCodeCreatedEvent {
    fn name(&self) -> &str {
        &self.name
    }

    fn timestamp(&self) -> DateTime<Utc> {
        self.timestamp
    }

    fn payload(&self) -> &str {
        &self.payload
    }
}

#[cfg(test)]
mod tests {
    //! Tests existing user event schemas.
    //! # Test plan
    //! - `preserves_event_contracts`: retains names, topics and JSON fields for all events.
    //! - `rejects_malformed_payloads`: malformed input returns errors for every payload.
    use super::*;
    #[test]
    fn preserves_event_contracts() {
        let created =
            UserCreatedEvent::new(&UserCreatedPayload::new("id".into(), "phone".into())).unwrap();
        let verified =
            UserVerifiedEvent::new(&UserVerifiedPayload::new("id".into(), "phone".into())).unwrap();
        let code = VerificationCodeCreatedEvent::new(&VerificationCodeCreatedPayload::new(
            "id".into(),
            "phone".into(),
            "code".into(),
        ))
        .unwrap();
        for (event, topic, name, expected) in [
            (
                &created as &dyn Event,
                TOPIC_USER_CREATED,
                "UserCreatedEvent",
                serde_json::json!({"user_id":"id", "phone_number":"phone"}),
            ),
            (
                &verified as &dyn Event,
                TOPIC_USER_VERIFIED,
                "UserVerifiedEvent",
                serde_json::json!({"user_id":"id", "phone_number":"phone"}),
            ),
            (
                &code as &dyn Event,
                TOPIC_VERIFICATION_CODE_CREATED,
                "VerificationCodeCreatedEvent",
                serde_json::json!({"user_id":"id", "phone_number":"phone", "verification_code":"code"}),
            ),
        ] {
            assert_eq!(event.name(), name);
            assert_eq!(
                serde_json::from_str::<serde_json::Value>(event.payload()).unwrap(),
                expected
            );
            assert!(topic.starts_with("users/"));
        }
        assert_eq!(
            serde_json::to_value(&created).unwrap()["topic"],
            TOPIC_USER_CREATED
        );
        assert_eq!(
            serde_json::to_value(&verified).unwrap()["topic"],
            TOPIC_USER_VERIFIED
        );
        assert_eq!(
            serde_json::to_value(&code).unwrap()["topic"],
            TOPIC_VERIFICATION_CODE_CREATED
        );
    }
    #[test]
    fn rejects_malformed_payloads() {
        assert!(UserCreatedPayload::parse("invalid").is_err());
        assert!(UserVerifiedPayload::parse("invalid").is_err());
        assert!(VerificationCodeCreatedPayload::parse("invalid").is_err());
    }
}
