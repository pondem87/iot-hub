use sqlx::{Pool, Postgres};

use crate::users::repositories::{UserPreferencesRepository, UserProfileRepository, UserRepository};
use crate::users::models::User;
use uuid::Uuid;

struct UserAccountService<'a> {
    repo: UserRepository<'a>,
}

impl<'a> UserAccountService<'a> {
    pub fn new(pool: &'a Pool<Postgres>) -> Self {
        let repo = UserRepository::new(pool);
        Self { repo }
    }

    fn add_user(&self, user: User) {
        self.repo.add_user(user);
    }

    pub fn get_user_by_id(&self, user_id: Uuid) -> Option<User<AnyUser>> {
        self.repo.get_any_user_by_id(user_id)
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

