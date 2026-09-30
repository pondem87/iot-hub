use std::io::Error;

use crate::http::router::get_router;
use crate::state::AppState;

pub async fn start_http_server(port: &str, state: AppState) -> Result<(), Error> {
    let listener = tokio::net::TcpListener::bind(port).await?;
    axum::serve(listener, get_router(state)).await
}