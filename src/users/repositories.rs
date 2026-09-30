use crate::users::models;
use sqlx::types::Uuid;
use sqlx::{Pool, Postgres};

pub struct UserRepository<'a> {
    pool: &'a Pool<Postgres>,
}

impl<'a> UserRepository<'a> {
    pub fn new(pool: &'a Pool<Postgres>) -> Self {
        Self { pool }
    }
}

pub struct UserProfileRepository<'a> {
    pool: &'a Pool<Postgres>,
}

impl<'a> UserProfileRepository<'a> {
    pub fn new(pool: &'a Pool<Postgres>) -> Self {
        Self { pool }
    }
}

pub struct UserPreferencesRepository<'a> {
    pool: &'a Pool<Postgres>,
}

impl<'a> UserPreferencesRepository<'a> {
    pub fn new(pool: &'a Pool<Postgres>) -> Self {
        Self { pool }
    }
}