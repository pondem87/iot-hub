use std::future::Future;
use std::sync::Arc;

use chrono::{DateTime, Utc};

use crate::events::errors::EventPayloadError;
use crate::state::AppState;

/// A completed domain occurrence; payloads may contain sensitive information.
pub trait Event: Send + Sync {
    /// Returns the stable occurrence name.
    fn name(&self) -> &str;
    /// Returns the occurrence timestamp in UTC.
    fn timestamp(&self) -> DateTime<Utc>;
    /// Returns serialized payload data; callers must avoid exposing sensitive values.
    fn payload(&self) -> &str;
}

/// Fallible JSON payload conversion; callers must handle malformed input.
pub trait EventPayload: serde::Serialize + serde::de::DeserializeOwned {
    /// Encodes this payload as JSON.
    /// # Errors
    /// Returns serialization errors rather than panicking.
    fn serialise(&self) -> Result<String, EventPayloadError> {
        serde_json::to_string(self).map_err(|cause| EventPayloadError::EncodingFailure {
            cause: Box::new(cause),
        })
    }

    /// Decodes JSON into this payload.
    /// # Errors
    /// Returns malformed JSON or incompatible payload data as an error.
    fn parse(serialized: &str) -> Result<Self, EventPayloadError> {
        serde_json::from_str(serialized).map_err(|cause| EventPayloadError::InvalidPayload {
            cause: Box::new(cause),
        })
    }
}

/// Coordinates reactions to occurrences through application capabilities.
pub trait EventHandler: Send + Sync {
    /// Reacts to the occurrence; the in-memory dispatcher does not monitor handler panics.
    fn handle(&self, event: &dyn Event);
    /// Returns shared dependencies for the reaction.
    fn get_app_state(&self) -> Arc<AppState>;
}

/// Publishes an occurrence through the configured manager.
pub trait EventPublisher {
    /// Forwards an occurrence; concrete managers define delivery guarantees.
    fn publish(&self, topic: &str, event: Box<dyn Event>) -> impl Future<Output = ()>;
}

/// Subscription and publication port; implementations define delivery guarantees.
pub trait EventManager: Send + Sync {
    /// Registers a handler for the supplied topic.
    fn subscribe(&mut self, topic: String, subscriber: Box<dyn EventHandler>);

    /// Forwards an occurrence; concrete managers define delivery guarantees.
    fn publish(&self, topic: &str, event: Arc<dyn Event>);
}
#[cfg(test)]
mod tests {
    //! Tests fallible payload conversion.
    //! # Test plan
    //! - `rejects_malformed_payload`: returns a decoding error instead of panicking.
    //! - `reports_encoding_failure`: returns a safe structured outcome for serialization failure.
    //! - `round_trips_payload`: preserves valid payloads.
    use super::*;
    /// Minimal event payload fixture.
    #[derive(serde::Serialize, serde::Deserialize, Debug, PartialEq)]
    struct Payload {
        value: bool,
    }
    impl EventPayload for Payload {}
    #[test]
    fn rejects_malformed_payload() {
        assert!(matches!(
            Payload::parse("invalid"),
            Err(EventPayloadError::InvalidPayload { .. })
        ));
    }
    /// Payload fixture simulating an adapter serialization failure.
    #[derive(serde::Deserialize)]
    struct FailingPayload;
    impl serde::Serialize for FailingPayload {
        fn serialize<S: serde::Serializer>(&self, _serializer: S) -> Result<S::Ok, S::Error> {
            Err(serde::ser::Error::custom("private encoding detail"))
        }
    }
    impl EventPayload for FailingPayload {}
    #[test]
    fn reports_encoding_failure() {
        let error = FailingPayload.serialise().unwrap_err();
        assert!(matches!(error, EventPayloadError::EncodingFailure { .. }));
        assert!(!error.to_string().contains("private encoding detail"));
    }
    #[test]
    fn round_trips_payload() {
        let value = Payload { value: true };
        assert_eq!(Payload::parse(&value.serialise().unwrap()).unwrap(), value);
    }
}
