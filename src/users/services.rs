use sqlx::{Pool, Postgres};

use crate::users::repositories::{UserPreferencesRepository, UserProfileRepository, UserRepository};

struct UserAccountService<'a> {
    repo: UserRepository<'a>,
}

impl<'a> UserAccountService<'a> {
    pub fn new(pool: &'a Pool<Postgres>) -> Self {
        let repo = UserRepository::new(pool);
        Self { repo }
    }
}


struct UserProfileService<'a> {
    repo: UserProfileRepository<'a>,
}

impl<'a> UserProfileService<'a> {
    pub fn new(pool: &'a Pool<Postgres>) -> Self {
        let repo = UserProfileRepository::new(pool);
        Self { repo }
    }
}

struct UserPreferencesService<'a> {
    repo: UserPreferencesRepository<'a>,
}

impl<'a> UserPreferencesService<'a> {
    pub fn new(pool: &'a Pool<Postgres>) -> Self {
        let repo = UserPreferencesRepository::new(pool);
        Self { repo }
    }
}

