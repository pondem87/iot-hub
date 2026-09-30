use sqlx::{
    Pool,
    PgPool,
    Postgres,
    Error
};

pub async fn get_conn_pool(conn_string: &str) -> Result<Pool<Postgres>, Error> {
    PgPool::connect(conn_string).await
}