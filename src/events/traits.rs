use std::sync::Arc;
use std::future::Future;
use crate::state::AppState;

use chrono::{DateTime, Utc};

pub trait Event: Send + Sync {
    fn name(&self) -> &str;
    fn timestamp(&self) -> DateTime<Utc>;
    fn payload(&self) -> &str;
}

pub trait EventPayload: serde::Serialize + serde::de::DeserializeOwned {
    fn serialise(&self) -> String {
        serde_json::to_string(self).unwrap()
    }

    fn parse(serialized: &str) -> Self {
        serde_json::from_str(serialized).unwrap()
    }
}

pub trait EventHandler: Send + Sync {
    fn handle(&self, event: &dyn Event);
    fn get_app_state(&self) -> Arc<AppState>;
}

pub trait EventPublisher {
    fn publish(&self, topic: &str, event: Box<dyn Event>) -> impl Future<Output = ()>;
}

pub trait EventManager: Send + Sync {
    fn subscribe(&mut self, topic: String, subscriber: Box<dyn EventHandler>);

    fn publish(&self, topic: &str, event: Arc<dyn Event>);
}