use iot_hub::http;
use iot_hub::db;
use std::sync::Arc;

#[tokio::main]
async fn main() {
    let conn_string = "mysql://user:password@localhost/dbname";
    let core_pool = db::db::get_conn_pool(conn_string).await.unwrap();

    sqlx::migrate!("./migrations/core_db").run(&core_pool).await.unwrap();

    let event_manager = iot_hub::events::event_manager_factory::get_in_memory_event_manager();

    let app_state = iot_hub::state::AppState::new(
        Arc::new(std::sync::Mutex::new(core_pool)),
        Arc::new(event_manager),
    );


    let handler = tokio::spawn(async {
        http::app::start_http_server("0.0.0.0:3000", app_state).await.unwrap();
    });

    handler.await.unwrap();
}
