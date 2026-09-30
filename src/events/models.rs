use std::{collections::HashMap, sync::Arc};
use crate::events::traits::{Event, EventHandler, EventManager};

pub struct InMemoryEventManager {
    subscriptions: HashMap<String, SubscriberList>
}

impl InMemoryEventManager {
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

struct SubscriberList {
    subscribers: Vec<Arc<dyn EventHandler>>,
}

impl SubscriberList {
    pub fn new() -> Self {
        Self {
            subscribers: Vec::new(),
        }
    }

    pub fn add_subscriber(&mut self, subscriber: Box<dyn EventHandler>) {
        self.subscribers.push(Arc::from(subscriber));
    }
}