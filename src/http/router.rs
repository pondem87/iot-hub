//! HTTP composition; users endpoints remain unimplemented.

use axum::{Router, routing::get};

use crate::state::AppState;

/// Constructs the existing health route without opening network listeners.
pub fn get_router(state: AppState) -> Router {
    Router::new().route("/", get(http_health)).with_state(state)
}

async fn http_health() -> &'static str {
    "Server running!"
}

#[cfg(test)]
mod tests {
    //! Tests the existing health route composition.
    //! # Test plan
    //! - `preserves_health_response`: retains the existing health response text.
    //! - `constructs_router`: accepts injected state without starting infrastructure.
    use super::*;
    #[tokio::test]
    async fn preserves_health_response() {
        assert_eq!(http_health().await, "Server running!");
    }
    #[tokio::test]
    async fn constructs_router() {
        use std::sync::{Arc, Mutex};
        let pool = sqlx::postgres::PgPoolOptions::new()
            .connect_lazy("postgres://localhost/test")
            .unwrap();
        let state = AppState::new(
            Arc::new(Mutex::new(pool)),
            Arc::new(crate::events::models::InMemoryEventManager::default()),
        );
        let _: Router = get_router(state);
    }
}
