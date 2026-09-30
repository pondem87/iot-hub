use serde::{Deserialize, Serialize};
use chrono::{DateTime, Utc};

use crate::events::traits::{Event, EventPayload};


// user created event
pub const TOPIC_USER_CREATED: &str = "users/user_created";

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct UserCreatedEvent {
    topic: String,
    name: String,
    timestamp: DateTime<Utc>,
    payload: String,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct UserCreatedPayload {
    user_id: String,
    phone_number: String,
}

impl EventPayload for UserCreatedPayload {}

impl UserCreatedPayload {
    pub fn new(user_id: String, phone_number: String) -> Self {
        Self {
            user_id,
            phone_number,
        }
    }

    pub fn get_user_id(&self) -> &str {
        &self.user_id
    }

    pub fn get_phone_number(&self) -> &str {
        &self.phone_number
    }
}

impl UserCreatedEvent {
    pub fn new(payload: &UserCreatedPayload) -> Self {
        Self {
            topic: String::from(TOPIC_USER_CREATED),
            name: String::from("UserCreatedEvent"),
            timestamp: chrono::Utc::now(),
            payload: payload.serialise(),
        }
    }
}

impl Event for UserCreatedEvent {
    fn name(&self) -> &str {
        &self.name
    }

    fn timestamp(&self) -> DateTime<Utc> {
        self.timestamp
    }

    fn payload(&self) -> &str {
        &self.payload
    }
}

// user verified event
pub const TOPIC_USER_VERIFIED: &str = "users/user_verified";

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct UserVerifiedEvent {
    topic: String,
    name: String,
    timestamp: DateTime<Utc>,
    payload: String,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct UserVerifiedPayload {
    user_id: String,
    phone_number: String,
}

impl EventPayload for UserVerifiedPayload {}
    
impl UserVerifiedPayload {
    pub fn new(user_id: String, phone_number: String) -> Self {
        Self {
            user_id,
            phone_number,
        }
    }

    pub fn get_user_id(&self) -> &str {
        &self.user_id
    }

    pub fn get_phone_number(&self) -> &str {
        &self.phone_number
    }
}

impl UserVerifiedEvent {
    pub fn new(payload: &UserVerifiedPayload) -> Self {
        Self {
            topic: String::from(TOPIC_USER_VERIFIED),
            name: String::from("UserVerifiedEvent"),
            timestamp: chrono::Utc::now(),
            payload: payload.serialise(),
        }
    }
}

impl Event for UserVerifiedEvent {
    fn name(&self) -> &str {
        &self.name
    }

    fn timestamp(&self) -> DateTime<Utc> {
        self.timestamp
    }

    fn payload(&self) -> &str {
        &self.payload
    }
}

// verification code created event
pub const TOPIC_VERIFICATION_CODE_CREATED: &str = "users/verification_code_created";

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct VerificationCodeCreatedEvent {
    topic: String,
    name: String,
    timestamp: DateTime<Utc>,
    payload: String,
}

#[derive(Debug, Clone, Serialize, Deserialize)] 
pub struct VerificationCodeCreatedPayload {
    user_id: String,
    phone_number: String,
    verification_code: String,
}

impl EventPayload for VerificationCodeCreatedPayload {}

impl VerificationCodeCreatedPayload {
    pub fn new(user_id: String, phone_number: String, verification_code: String) -> Self {
        Self {
            user_id,
            phone_number,
            verification_code,
        }
    }

    pub fn get_user_id(&self) -> &str {
        &self.user_id
    }

    pub fn get_phone_number(&self) -> &str {
        &self.phone_number
    }

    pub fn get_verification_code(&self) -> &str {
        &self.verification_code
    }
}

impl VerificationCodeCreatedEvent {
    pub fn new(payload: &VerificationCodeCreatedPayload) -> Self {
        Self {
            topic: String::from(TOPIC_VERIFICATION_CODE_CREATED),
            name: String::from("VerificationCodeCreatedEvent"),
            timestamp: chrono::Utc::now(),
            payload: payload.serialise(),
        }
    }
}

impl Event for VerificationCodeCreatedEvent {
    fn name(&self) -> &str {
        &self.name
    }

    fn timestamp(&self) -> DateTime<Utc> {
        self.timestamp
    }

    fn payload(&self) -> &str{
        &self.payload
    }
}

