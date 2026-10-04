//! Technology-independent payload conversion outcomes.
use std::error::Error;

use thiserror::Error;

/// Failure to encode or decode a domain occurrence payload.
#[derive(Debug, Error)]
pub enum EventPayloadError {
    /// The payload could not be encoded for publication.
    #[error("failed to encode event payload")]
    EncodingFailure {
        /// Internal diagnostic source; never serialize the source chain.
        #[source]
        cause: Box<dyn Error + Send + Sync>,
    },
    /// Input is malformed or does not match the requested payload contract.
    #[error("invalid event payload")]
    InvalidPayload {
        /// Internal diagnostic source; never serialize the source chain.
        #[source]
        cause: Box<dyn Error + Send + Sync>,
    },
}
