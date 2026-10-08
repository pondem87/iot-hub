//! Isolated PostgreSQL integration target; enable `database-tests` to run.
#[cfg(test)]
mod tests {
    //! Isolated PostgreSQL tests for user persistence and service composition.
    //!
    //! Run with the dedicated Compose configuration documented in `docker-compose/README.md`.
    //! SQLx creates a migrated database per test and cleans it up after success.
    //!
    //! # Test plan
    //! - `loads_every_user_state`: loads validated users and their password hashes
    //!   for every SQL state and role, preserving diagnostic redaction.
    //! - `distinguishes_user_absence_and_mismatch`: active reads distinguish missing and wrong-state rows.
    //! - `loads_contacts_and_enforces_owner_scope`: maps both contact kinds and every lifecycle state.
    //! - `distinguishes_contact_absence_and_mismatch`: active contact reads preserve scope and mismatch.
    //! - `reads_owned_profile_and_preferences`: joins through the owning user and preserves values.
    //! - `reports_invalid_stored_data`: rejects unsupported user and contact enum values.
    //! - `reports_decoding_failures`: rejects a malformed profile column and missing preference column.
    //! - `reports_database_failures`: translates a permission failure into an operational outcome.
    //! - `services_read_through_postgres`: composes all four local read services over migrated adapters.
    use iot_hub::users::{
        errors::{UserReadError, UserRepositoryError},
        models::{
            StoredContact, StoredUser, UserContactState, UserContactType, UserState, UserType,
        },
        repositories::{
            UserContactRepository, UserContactStore, UserPreferencesRepository,
            UserPreferencesStore, UserProfileRepository, UserProfileStore, UserRepository,
            UserStore,
        },
        services::{
            UserAccountReads, UserAccountService, UserContactReads, UserContactService,
            UserPreferencesReads, UserPreferencesService, UserProfileReads, UserProfileService,
        },
    };
    use sqlx::{PgPool, types::Uuid};

    /// Identities for records owned by one isolated test user.
    struct Fixture {
        user_id: Uuid,
        profile_id: Uuid,
        preferences_id: Uuid,
        contact_id: Uuid,
    }
    impl Fixture {
        async fn create(pool: &PgPool, ordinal: u128, state: &str, role: &str) -> Self {
            let fixture = Self {
                user_id: Uuid::from_u128(ordinal * 4),
                profile_id: Uuid::from_u128(ordinal * 4 + 1),
                preferences_id: Uuid::from_u128(ordinal * 4 + 2),
                contact_id: Uuid::from_u128(ordinal * 4 + 3),
            };
            sqlx::query(
                "INSERT INTO user_profiles (id, name, updated_at) VALUES ($1, 'Example', now())",
            )
            .bind(fixture.profile_id)
            .execute(pool)
            .await
            .unwrap();
            sqlx::query("INSERT INTO user_preferences (id, allow_notifications, updated_at) VALUES ($1, true, now())").bind(fixture.preferences_id).execute(pool).await.unwrap();
            sqlx::query("INSERT INTO users (id, phone_number, password, user_type, state, profile_id, preferences_id, created_at, updated_at) VALUES ($1, $2, 'test-encoded-password-hash', $3::text::user_type, $4::text::user_state, $5, $6, now(), now())")
                .bind(fixture.user_id).bind(format!("test-phone-{ordinal}")).bind(role).bind(state).bind(fixture.profile_id).bind(fixture.preferences_id).execute(pool).await.unwrap();
            fixture
        }
        async fn add_contact(&self, pool: &PgPool, state: &str, kind: &str) {
            sqlx::query("INSERT INTO user_contacts (id, user_contact_type, value, states, user_id, created_at, updated_at) VALUES ($1, $2::text::user_contact_type, 'example', $3::text::user_contact_state, $4, now(), now())")
                .bind(self.contact_id).bind(kind).bind(state).bind(self.user_id).execute(pool).await.unwrap();
        }
    }

    #[sqlx::test(migrations = "migrations/core_db")]
    async fn loads_every_user_state(pool: PgPool) {
        let repository = UserRepository::new(pool.clone());
        for (index, state) in ["unverified", "active", "inactive", "barred", "deleted"]
            .into_iter()
            .enumerate()
        {
            for (role_index, (role, expected_role)) in [
                ("superuser", UserType::Superuser),
                ("staff", UserType::Staff),
                ("customer", UserType::Customer),
            ]
            .into_iter()
            .enumerate()
            {
                let fixture =
                    Fixture::create(&pool, (index * 3 + role_index + 1) as u128, state, role).await;
                let user = repository
                    .get_user_by_id(fixture.user_id)
                    .await
                    .unwrap()
                    .unwrap();
                let (id, actual_role) = match (state, user) {
                    ("unverified", StoredUser::Unverified(user)) => {
                        assert_eq!(user.password(), "test-encoded-password-hash");
                        assert!(!format!("{user:?}").contains("test-encoded-password-hash"));
                        (user.id(), user.user_type())
                    }
                    ("active", StoredUser::Active(user)) => {
                        assert_eq!(user.password(), "test-encoded-password-hash");
                        assert!(!format!("{user:?}").contains("test-encoded-password-hash"));
                        (user.id(), user.user_type())
                    }
                    ("inactive", StoredUser::Inactive(user)) => {
                        assert_eq!(user.password(), "test-encoded-password-hash");
                        assert!(!format!("{user:?}").contains("test-encoded-password-hash"));
                        (user.id(), user.user_type())
                    }
                    ("barred", StoredUser::Barred(user)) => {
                        assert_eq!(user.password(), "test-encoded-password-hash");
                        assert!(!format!("{user:?}").contains("test-encoded-password-hash"));
                        (user.id(), user.user_type())
                    }
                    ("deleted", StoredUser::Deleted(user)) => {
                        assert_eq!(user.password(), "test-encoded-password-hash");
                        assert!(!format!("{user:?}").contains("test-encoded-password-hash"));
                        (user.id(), user.user_type())
                    }
                    _ => panic!("incorrect typestate returned"),
                };
                assert_eq!(id, fixture.user_id);
                assert_eq!(actual_role, expected_role);
            }
        }
    }

    #[sqlx::test(migrations = "migrations/core_db")]
    async fn distinguishes_user_absence_and_mismatch(pool: PgPool) {
        let repository = UserRepository::new(pool.clone());
        assert!(
            repository
                .get_user_by_id(Uuid::nil())
                .await
                .unwrap()
                .is_none()
        );
        assert!(
            repository
                .get_active_user(Uuid::nil())
                .await
                .unwrap()
                .is_none()
        );
        let active = Fixture::create(&pool, 1, "active", "customer").await;
        assert_eq!(
            repository
                .get_active_user(active.user_id)
                .await
                .unwrap()
                .unwrap()
                .state(),
            UserState::Active
        );
        for (index, (state, actual)) in [
            ("unverified", UserState::Unverified),
            ("inactive", UserState::Inactive),
            ("barred", UserState::Barred),
            ("deleted", UserState::Deleted),
        ]
        .into_iter()
        .enumerate()
        {
            let fixture = Fixture::create(&pool, index as u128 + 2, state, "customer").await;
            assert!(
                matches!(repository.get_active_user(fixture.user_id).await, Err(UserRepositoryError::UserStateMismatch { expected: UserState::Active, actual: a }) if a == actual)
            );
        }
    }

    #[sqlx::test(migrations = "migrations/core_db")]
    async fn loads_contacts_and_enforces_owner_scope(pool: PgPool) {
        let repository = UserContactRepository::new(pool.clone());
        for (index, state) in ["unverified", "active", "disabled"].into_iter().enumerate() {
            for (kind_index, (kind, expected_kind)) in [
                ("phonenumber", UserContactType::PhoneNumber),
                ("emailaddress", UserContactType::EmailAddress),
            ]
            .into_iter()
            .enumerate()
            {
                let fixture = Fixture::create(
                    &pool,
                    (index * 2 + kind_index + 1) as u128,
                    "active",
                    "customer",
                )
                .await;
                fixture.add_contact(&pool, state, kind).await;
                let contact = repository
                    .get_contact(fixture.user_id, fixture.contact_id)
                    .await
                    .unwrap()
                    .unwrap();
                let (id, owner, actual_kind) = match (state, contact) {
                    ("unverified", StoredContact::Unverified(contact)) => {
                        (contact.id(), contact.user_id(), contact.contact_type())
                    }
                    ("active", StoredContact::Active(contact)) => {
                        (contact.id(), contact.user_id(), contact.contact_type())
                    }
                    ("disabled", StoredContact::Disabled(contact)) => {
                        (contact.id(), contact.user_id(), contact.contact_type())
                    }
                    _ => panic!("incorrect contact typestate returned"),
                };
                assert_eq!(
                    (id, owner, actual_kind),
                    (fixture.contact_id, fixture.user_id, expected_kind)
                );
                assert!(
                    repository
                        .get_contact(Uuid::nil(), fixture.contact_id)
                        .await
                        .unwrap()
                        .is_none()
                );
            }
        }
    }

    #[sqlx::test(migrations = "migrations/core_db")]
    async fn distinguishes_contact_absence_and_mismatch(pool: PgPool) {
        let repository = UserContactRepository::new(pool.clone());
        assert!(
            repository
                .get_contact(Uuid::nil(), Uuid::nil())
                .await
                .unwrap()
                .is_none()
        );
        assert!(
            repository
                .get_active_contact(Uuid::nil(), Uuid::nil())
                .await
                .unwrap()
                .is_none()
        );
        for (index, (state, actual)) in [
            ("unverified", UserContactState::Unverified),
            ("active", UserContactState::Active),
            ("disabled", UserContactState::Disabled),
        ]
        .into_iter()
        .enumerate()
        {
            let fixture = Fixture::create(&pool, index as u128 + 1, "active", "customer").await;
            fixture.add_contact(&pool, state, "phonenumber").await;
            let result = repository
                .get_active_contact(fixture.user_id, fixture.contact_id)
                .await;
            if actual == UserContactState::Active {
                assert_eq!(result.unwrap().unwrap().state(), actual);
            } else {
                assert!(
                    matches!(result, Err(UserRepositoryError::ContactStateMismatch { expected: UserContactState::Active, actual: a }) if a == actual)
                );
            }
            assert!(
                repository
                    .get_active_contact(Uuid::nil(), fixture.contact_id)
                    .await
                    .unwrap()
                    .is_none()
            );
        }
    }

    #[sqlx::test(migrations = "migrations/core_db")]
    async fn reads_owned_profile_and_preferences(pool: PgPool) {
        let fixture = Fixture::create(&pool, 1, "active", "customer").await;
        let profiles = UserProfileRepository::new(pool.clone());
        let preferences = UserPreferencesRepository::new(pool.clone());
        let profile = profiles
            .get_profile(fixture.user_id)
            .await
            .unwrap()
            .unwrap();
        assert_eq!(profile.id, fixture.profile_id);
        assert_eq!(profile.name, "Example");
        let settings = preferences
            .get_preferences(fixture.user_id)
            .await
            .unwrap()
            .unwrap();
        assert_eq!(settings.id, fixture.preferences_id);
        assert!(settings.allow_notifications);
        assert!(profiles.get_profile(Uuid::nil()).await.unwrap().is_none());
        assert!(
            preferences
                .get_preferences(Uuid::nil())
                .await
                .unwrap()
                .is_none()
        );
        // Secondary identities cannot be used as owning user identities.
        assert!(
            profiles
                .get_profile(fixture.profile_id)
                .await
                .unwrap()
                .is_none()
        );
        assert!(
            preferences
                .get_preferences(fixture.preferences_id)
                .await
                .unwrap()
                .is_none()
        );
    }

    #[sqlx::test(migrations = "migrations/core_db")]
    async fn reports_invalid_stored_data(pool: PgPool) {
        let fixture = Fixture::create(&pool, 1, "active", "customer").await;
        fixture.add_contact(&pool, "active", "phonenumber").await;
        sqlx::query("ALTER TYPE user_state ADD VALUE 'unsupported'")
            .execute(&pool)
            .await
            .unwrap();
        sqlx::query("UPDATE users SET state = 'unsupported' WHERE id = $1")
            .bind(fixture.user_id)
            .execute(&pool)
            .await
            .unwrap();
        assert!(matches!(
            UserRepository::new(pool.clone())
                .get_user_by_id(fixture.user_id)
                .await,
            Err(UserRepositoryError::InvalidData { .. })
        ));
        sqlx::query("ALTER TYPE user_contact_state ADD VALUE 'unsupported'")
            .execute(&pool)
            .await
            .unwrap();
        sqlx::query("UPDATE user_contacts SET states = 'unsupported' WHERE id = $1")
            .bind(fixture.contact_id)
            .execute(&pool)
            .await
            .unwrap();
        assert!(matches!(
            UserContactRepository::new(pool.clone())
                .get_contact(fixture.user_id, fixture.contact_id)
                .await,
            Err(UserRepositoryError::InvalidData { .. })
        ));
    }

    #[sqlx::test(migrations = "migrations/core_db")]
    async fn reports_decoding_failures(pool: PgPool) {
        let fixture = Fixture::create(&pool, 1, "active", "customer").await;
        sqlx::query(
            "ALTER TABLE user_profiles ALTER COLUMN name TYPE BYTEA USING convert_to(name, 'UTF8')",
        )
        .execute(&pool)
        .await
        .unwrap();
        assert!(matches!(
            UserProfileRepository::new(pool.clone())
                .get_profile(fixture.user_id)
                .await,
            Err(UserRepositoryError::InvalidData { .. })
        ));
        sqlx::query(
            "ALTER TABLE user_preferences RENAME COLUMN allow_notifications TO unsupported",
        )
        .execute(&pool)
        .await
        .unwrap();
        assert!(matches!(
            UserPreferencesRepository::new(pool.clone())
                .get_preferences(fixture.user_id)
                .await,
            Err(UserRepositoryError::Unexpected { .. })
        ));
    }

    #[sqlx::test(migrations = "migrations/core_db")]
    async fn reports_database_failures(pool: PgPool) {
        let fixture = Fixture::create(&pool, 1, "active", "customer").await;
        // Session-local restricted role; no cluster-wide roles or shared databases are changed.
        let options = pool.connect_options();
        let restricted = sqlx::postgres::PgPoolOptions::new()
            .max_connections(1)
            .after_connect(|connection, _| {
                Box::pin(async move {
                    sqlx::query("SET ROLE pg_read_all_settings")
                        .execute(connection)
                        .await?;
                    Ok(())
                })
            })
            .connect_with((*options).clone())
            .await
            .unwrap();
        assert!(matches!(
            UserRepository::new(restricted.clone())
                .get_user_by_id(fixture.user_id)
                .await,
            Err(UserRepositoryError::Unexpected { .. })
        ));
        restricted.close().await;
    }

    #[sqlx::test(migrations = "migrations/core_db")]
    async fn services_read_through_postgres(pool: PgPool) {
        let fixture = Fixture::create(&pool, 1, "active", "customer").await;
        fixture.add_contact(&pool, "active", "emailaddress").await;
        let accounts = UserAccountService::new(UserRepository::new(pool.clone()));
        let profiles = UserProfileService::new(UserProfileRepository::new(pool.clone()));
        let preferences = UserPreferencesService::new(UserPreferencesRepository::new(pool.clone()));
        let contacts = UserContactService::new(UserContactRepository::new(pool.clone()));
        assert_eq!(
            accounts
                .get_active_user(fixture.user_id)
                .await
                .unwrap()
                .id(),
            fixture.user_id
        );
        assert_eq!(
            profiles.get_profile(fixture.user_id).await.unwrap().name,
            "Example"
        );
        assert!(
            preferences
                .get_preferences(fixture.user_id)
                .await
                .unwrap()
                .allow_notifications
        );
        assert_eq!(
            contacts
                .get_active_contact(fixture.user_id, fixture.contact_id)
                .await
                .unwrap()
                .contact_type(),
            UserContactType::EmailAddress
        );
        assert!(matches!(
            accounts.get_active_user(Uuid::nil()).await,
            Err(UserReadError::NotFound)
        ));
        sqlx::query("UPDATE users SET state = 'barred' WHERE id = $1")
            .bind(fixture.user_id)
            .execute(&pool)
            .await
            .unwrap();
        assert!(matches!(
            accounts.get_active_user(fixture.user_id).await,
            Err(UserReadError::UserStateMismatch {
                expected: UserState::Active,
                actual: UserState::Barred
            })
        ));
    }
}
