use sqlx::types::Uuid;

pub trait CodeGenerator {
    fn generate_otp_code(&self, user_id: Uuid) -> String;

    fn generate_contact_verif_code(&self, user_id: Uuid, contact_id: Uuid) -> String;
}

pub trait CodeValidator {
    fn validate_otp_code(&self, user_id: Uuid, code: &str) -> bool;

    fn validate_contact_verif_code(&self, user_id: Uuid, contact_id: Uuid, code: &str) -> bool;
}