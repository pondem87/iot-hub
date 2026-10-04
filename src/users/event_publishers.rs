//! User occurrence forwarding; delivery is best effort and managed in memory.

use std::sync::Arc;

use crate::events::traits::{Event, EventManager, EventPublisher};

/// Forwards committed occurrences without claiming retries, ordering, or durability.
pub struct UserEventPublisher {
    event_manager: Arc<dyn EventManager>,
}

impl UserEventPublisher {
    /// Injects the application's shared event manager.
    pub fn new(event_manager: Arc<dyn EventManager>) -> Self {
        Self { event_manager }
    }
}

impl EventPublisher for UserEventPublisher {
    async fn publish(&self, topic: &str, event: Box<dyn Event>) {
        self.event_manager.publish(topic, Arc::from(event));
    }
}

#[cfg(test)]
mod tests {
    //! Tests publisher forwarding without dispatching real workflows.
    //! # Test plan
    //! - `forwards_exact_occurrence`: retains topic and event identity.
    use super::*;
    use crate::events::traits::EventHandler;
    use std::sync::Mutex;

    /// Captures publication for verification.
    #[derive(Default)]
    struct Manager {
        received: Mutex<Option<(String, Arc<dyn Event>)>>,
    }

    impl EventManager for Manager {
        fn subscribe(&mut self, _topic: String, _subscriber: Box<dyn EventHandler>) {}
        fn publish(&self, topic: &str, event: Arc<dyn Event>) {
            *self.received.lock().unwrap() = Some((topic.into(), event));
        }
    }

    #[tokio::test]
    async fn forwards_exact_occurrence() {
        use crate::users::events::*;
        let manager = Arc::new(Manager::default());
        let publisher = UserEventPublisher::new(manager.clone());
        let event =
            UserCreatedEvent::new(&UserCreatedPayload::new("id".into(), "phone".into())).unwrap();
        let timestamp = event.timestamp();
        let payload = event.payload().to_owned();
        publisher.publish(TOPIC_USER_CREATED, Box::new(event)).await;
        let guard = manager.received.lock().unwrap();
        let (topic, event) = guard.as_ref().unwrap();
        assert_eq!(topic, TOPIC_USER_CREATED);
        assert_eq!(event.name(), "UserCreatedEvent");
        assert_eq!(event.timestamp(), timestamp);
        assert_eq!(event.payload(), payload);
    }
}
