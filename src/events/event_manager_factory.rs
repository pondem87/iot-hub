use crate::events::models::InMemoryEventManager;

pub fn get_in_memory_event_manager() -> InMemoryEventManager {
    InMemoryEventManager::new()
}