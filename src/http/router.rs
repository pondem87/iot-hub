use axum::{ Router, routing::get };
use crate::state::AppState;

use crate::http::handlers::{auth, users};

pub fn get_router(state: AppState) -> Router<> {
    let router = Router::new()
        .route("/", get(http_health))
        .with_state(state);

    router
}

async fn http_health() -> &'static str {
    "Server running!"
}

