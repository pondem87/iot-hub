//! Connection construction; domain operations receive an existing pool.
use sqlx::{Error, PgPool, Pool, Postgres};

/// Opens a PostgreSQL pool using deployment configuration supplied by the caller.
/// # Errors
/// Returns SQLx configuration or connection failures to the composition boundary.
pub async fn get_conn_pool(conn_string: &str) -> Result<Pool<Postgres>, Error> {
    PgPool::connect(conn_string).await
}
#[cfg(test)]
mod tests {
    //! Tests connection input validation without a live database.
    //! # Test plan
    //! - `rejects_invalid_connection_configuration`: fails before connecting for an invalid URL.
    use super::*;
    #[tokio::test]
    async fn rejects_invalid_connection_configuration() {
        assert!(matches!(
            get_conn_pool("invalid connection URL").await,
            Err(sqlx::Error::Configuration(_))
        ));
    }
}
