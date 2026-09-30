use serde::Deserialize;

#[derive(Debug, Clone, Deserialize)]
pub struct CreateUserInput {
    pub phone_number: String,
    pub name: String,
    pub password: String,
}

#[derive(Debug, Clone, Deserialize)]
pub struct CreateUserOutput {
    pub id: String,
    pub phone_number: String,
    pub name: String,
}