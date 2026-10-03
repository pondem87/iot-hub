use sqlx::PgPool;
use std::sync::{Arc, Mutex};

use crate::events::traits::EventManager;

#[derive(Clone)]
pub struct AppState {
    db_pool: Arc<Mutex<PgPool>>,
    event_manager: Arc<dyn EventManager>,
}

impl AppState {
    pub fn new(db_pool: Arc<Mutex<PgPool>>, event_manager: Arc<dyn EventManager>) -> Self {
        Self {
            db_pool,
            event_manager,
        }
    }

    pub fn db_pool(&self) -> Arc<Mutex<PgPool>> {
        Arc::clone(&self.db_pool)
    }

    pub fn event_manager(&self) -> Arc<dyn EventManager> {
        Arc::clone(&self.event_manager)
    }
}
