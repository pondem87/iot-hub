use crate::events::traits::{EventPublisher, EventManager};
use crate::events::traits::Event;
use std::sync::Arc;

pub struct UserEventPublisher<'a> {
    event_manager: &'a Box<dyn EventManager>,
}

impl<'a> UserEventPublisher<'a> {
    pub fn new(event_manager: &'a Box<dyn EventManager>) -> Self {
        Self { event_manager }
    }
}

impl<'a> EventPublisher for UserEventPublisher<'a> {
    async fn publish(&self, topic: &str, event: Box<dyn Event>) {
        self.event_manager.publish(topic, Arc::from(event));
    }
}
