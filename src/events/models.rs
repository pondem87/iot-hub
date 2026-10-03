//! Best-effort in-memory dispatch; publication does not establish durable delivery.

use std::{collections::HashMap, sync::Arc};

use crate::events::traits::{Event, EventHandler, EventManager};

/// Best-effort in-memory dispatcher with spawned subscriber tasks.
pub struct InMemoryEventManager {
    subscriptions: HashMap<String, SubscriberList>,
}

impl InMemoryEventManager {
    /// Creates an empty best-effort dispatcher without spawning tasks.
    pub fn new() -> Self {
        Self {
            subscriptions: HashMap::new(),
        }
    }
}

impl EventManager for InMemoryEventManager {
    fn subscribe(&mut self, topic: String, subscriber: Box<dyn EventHandler>) {
        self.subscriptions
            .entry(topic)
            .or_insert_with(SubscriberList::new)
            .add_subscriber(subscriber);
    }

    fn publish(&self, topic: &str, event: Arc<dyn Event>) {
        if let Some(subscribers) = self.subscriptions.get(topic) {
            for subscriber in &subscribers.subscribers {
                let subscriber = Arc::clone(subscriber);
                let event = Arc::clone(&event);

                tokio::spawn(async move {
                    subscriber.handle(event.as_ref());
                });
            }
        }
    }
}

/// Shared handlers registered for one topic.
struct SubscriberList {
    subscribers: Vec<Arc<dyn EventHandler>>,
}

impl SubscriberList {
    /// Creates an empty topic subscriber list.
    pub fn new() -> Self {
        Self {
            subscribers: Vec::new(),
        }
    }

    /// Retains a handler through shared ownership for asynchronous dispatch.
    pub fn add_subscriber(&mut self, subscriber: Box<dyn EventHandler>) {
        self.subscribers.push(Arc::from(subscriber));
    }
}
impl Default for InMemoryEventManager {
    fn default() -> Self {
        Self::new()
    }
}

#[cfg(test)]
mod tests {
    //! Tests best-effort event manager construction and dispatch.
    //! # Test plan
    //! - `default_has_no_subscriptions`: constructs an empty manager through Default.
    //! - `dispatches_registered_occurrence`: dispatches to a matching handler, ignoring other topics.
    use super::*;
    use std::sync::atomic::{AtomicUsize, Ordering};
    use tokio::sync::Notify;
    /// Records a dispatched event and retains injected handler state.
    struct Handler {
        state: Arc<crate::state::AppState>,
        notified: Arc<Notify>,
        calls: Arc<AtomicUsize>,
    }
    impl EventHandler for Handler {
        fn handle(&self, event: &dyn Event) {
            assert_eq!(event.name(), "UserCreatedEvent");
            self.calls.fetch_add(1, Ordering::SeqCst);
            self.notified.notify_one();
        }
        fn get_app_state(&self) -> Arc<crate::state::AppState> {
            Arc::clone(&self.state)
        }
    }
    #[test]
    fn default_has_no_subscriptions() {
        assert!(InMemoryEventManager::default().subscriptions.is_empty());
    }
    #[tokio::test]
    async fn dispatches_registered_occurrence() {
        use crate::users::events::{TOPIC_USER_CREATED, UserCreatedEvent, UserCreatedPayload};
        let pool = sqlx::postgres::PgPoolOptions::new()
            .connect_lazy("postgres://localhost/test")
            .unwrap();
        let state = Arc::new(crate::state::AppState::new(
            Arc::new(std::sync::Mutex::new(pool)),
            Arc::new(InMemoryEventManager::default()),
        ));
        let notified = Arc::new(Notify::new());
        let calls = Arc::new(AtomicUsize::new(0));
        let mut manager = InMemoryEventManager::default();
        manager.subscribe(
            TOPIC_USER_CREATED.into(),
            Box::new(Handler {
                state,
                notified: Arc::clone(&notified),
                calls: Arc::clone(&calls),
            }),
        );
        let event: Arc<dyn Event> = Arc::new(
            UserCreatedEvent::new(&UserCreatedPayload::new("id".into(), "phone".into())).unwrap(),
        );
        manager.publish("unknown", Arc::clone(&event));
        tokio::task::yield_now().await;
        assert_eq!(calls.load(Ordering::SeqCst), 0);
        manager.publish(TOPIC_USER_CREATED, event);
        tokio::time::timeout(std::time::Duration::from_secs(1), notified.notified())
            .await
            .unwrap();
        assert_eq!(calls.load(Ordering::SeqCst), 1);
    }
}
