//! Permission storage integration tests against isolated, migrated PostgreSQL databases.

#[cfg(test)]
mod tests {
    //! Tests grant persistence and checked SQLx decoding.
    //!
    //! # Test plan
    //! - `round_trips_grants_and_isolates_principals`: covers both principal kinds,
    //!   both scopes, all/selected/empty attributes, generated IDs, absence, and deletion.
    //! - `round_trips_domain_vocabulary`: verifies every supported SQL resource/action
    //!   and attribute spelling through a real insert and decode.
    //! - `rejects_invalid_rows`: distinguishes malformed JSON, enum decoding, and
    //!   inconsistent scope data from absence.
    //! - `failed_validation_rolls_back_creation`: corrupts an inserted row through
    //!   a test trigger and verifies no invalid grant commits.
    //! - `service_authorizes_management_through_postgres`: checks administrative grants,
    //!   creation, listing, additive user/role coverage, and persisted revocation.
    //! - `reports_database_failures`: translates an unavailable relation into a
    //!   storage error instead of absence.
    //!
    //! SQLx creates and migrates one disposable database per test. Run using the
    //! isolated Compose setup in `docker-compose/README.md`.

    use std::collections::HashSet;

    use iot_hub::{
        permissions::{
            app::ResourceAttrTrait,
            schemas::{AttrObject, Principal, Scope, ScopeObject},
        },
        users::perm::{
            errors::UserPermissionsRepositoryError,
            models::{UsersAction, UsersAttribute, UsersResource},
            repositories::{UserPermissionsRepository, UserPermissionsStore},
        },
    };
    use sqlx::{PgPool, types::Uuid};

    #[sqlx::test(migrations = "migrations/core_db")]
    async fn round_trips_grants_and_isolates_principals(pool: PgPool) {
        let repository = UserPermissionsRepository::new(pool);
        let id = Uuid::from_u128(1);
        assert!(
            repository
                .get_permissions(Principal::User(id))
                .await
                .unwrap()
                .is_empty()
        );
        for role in [false, true] {
            for object in [false, true] {
                for selection in [None, Some(vec![]), Some(vec![UsersAttribute::Name])] {
                    let principal = if role {
                        Principal::Role(id)
                    } else {
                        Principal::User(id)
                    };
                    let scope = Scope {
                        object: if object {
                            ScopeObject::ObjectId(UsersResource::UserProfile, id)
                        } else {
                            ScopeObject::Collection(UsersResource::UserProfile)
                        },
                        attributes: match selection {
                            None => AttrObject::All,
                            Some(attrs) => AttrObject::Selected(attrs.into_iter().collect()),
                        },
                    };
                    let permission = repository
                        .create_permission(principal, scope, UsersAction::Read)
                        .await
                        .unwrap();
                    assert!(!permission.perm_id.is_nil());
                    assert!(matches!(permission.action, UsersAction::Read));
                }
            }
        }
        for principal in [Principal::User(id), Principal::Role(id)] {
            let role = matches!(principal, Principal::Role(_));
            let grants = repository.get_permissions(principal).await.unwrap();
            assert_eq!(grants.len(), 6);
            assert_eq!(
                grants
                    .iter()
                    .map(|grant| grant.perm_id)
                    .collect::<HashSet<_>>()
                    .len(),
                6
            );
            let mut all = 0;
            let mut empty = 0;
            let mut selected = 0;
            let mut objects = 0;
            for grant in grants {
                assert!(matches!(grant.action, UsersAction::Read));
                match grant.principal {
                    Principal::Role(actual) => {
                        assert!(role);
                        assert_eq!(actual, id);
                    }
                    Principal::User(actual) => {
                        assert!(!role);
                        assert_eq!(actual, id);
                    }
                }
                match grant.scope.object {
                    ScopeObject::ObjectId(UsersResource::UserProfile, actual) => {
                        objects += 1;
                        assert_eq!(actual, id);
                    }
                    ScopeObject::Collection(UsersResource::UserProfile) => {}
                    _ => panic!("wrong resource"),
                }
                match grant.scope.attributes {
                    AttrObject::All => all += 1,
                    AttrObject::Selected(attrs) if attrs.is_empty() => empty += 1,
                    AttrObject::Selected(attrs) => {
                        selected += 1;
                        assert_eq!(
                            attrs.iter().map(|a| a.attribute_name()).collect::<Vec<_>>(),
                            vec!["name"]
                        );
                    }
                }
                assert!(repository.delete_permission(grant.perm_id).await.unwrap());
                assert!(!repository.delete_permission(grant.perm_id).await.unwrap());
            }
            assert_eq!((all, empty, selected, objects), (2, 2, 2, 3));
        }
        assert!(
            repository
                .get_permissions(Principal::User(id))
                .await
                .unwrap()
                .is_empty()
        );
        assert!(
            repository
                .get_permissions(Principal::Role(id))
                .await
                .unwrap()
                .is_empty()
        );
    }

    #[sqlx::test(migrations = "migrations/core_db")]
    async fn rejects_invalid_rows(pool: PgPool) {
        let repository = UserPermissionsRepository::new(pool.clone());
        let id = Uuid::from_u128(1);
        let grant = repository
            .create_permission(
                Principal::User(id),
                Scope {
                    object: ScopeObject::Collection(UsersResource::UserProfile),
                    attributes: AttrObject::All,
                },
                UsersAction::Read,
            )
            .await
            .unwrap();
        sqlx::query("UPDATE user_permissions SET attributes = '[\"unknown\"]'")
            .execute(&pool)
            .await
            .unwrap();
        assert!(matches!(
            repository.get_permissions(Principal::User(id)).await,
            Err(UserPermissionsRepositoryError::InvalidData)
        ));
        assert!(
            repository
                .get_permissions(Principal::Role(id))
                .await
                .unwrap()
                .is_empty()
        );
        sqlx::query("ALTER TABLE user_permissions DROP CONSTRAINT user_permissions_scope_check")
            .execute(&pool)
            .await
            .unwrap();
        sqlx::query("UPDATE user_permissions SET attributes = '\"all\"', perm_type = 'object'")
            .execute(&pool)
            .await
            .unwrap();
        assert!(matches!(
            repository.get_permissions(Principal::User(id)).await,
            Err(UserPermissionsRepositoryError::InvalidData)
        ));
        sqlx::query("ALTER TYPE users_permission_action ADD VALUE 'unknown'")
            .execute(&pool)
            .await
            .unwrap();
        sqlx::query("UPDATE user_permissions SET perm_type = 'collection', action = 'unknown'")
            .execute(&pool)
            .await
            .unwrap();
        assert!(matches!(
            repository.get_permissions(Principal::User(id)).await,
            Err(UserPermissionsRepositoryError::InvalidData)
        ));
        assert!(repository.delete_permission(grant.perm_id).await.unwrap());
    }

    #[sqlx::test(migrations = "migrations/core_db")]
    async fn failed_validation_rolls_back_creation(pool: PgPool) {
        sqlx::raw_sql("CREATE FUNCTION corrupt_attributes() RETURNS trigger LANGUAGE plpgsql AS $$ BEGIN NEW.attributes := 'invalid'; RETURN NEW; END; $$;
            CREATE TRIGGER corrupt_permission BEFORE INSERT ON user_permissions FOR EACH ROW EXECUTE FUNCTION corrupt_attributes();")
            .execute(&pool).await.unwrap();
        let repository = UserPermissionsRepository::new(pool.clone());
        let result = repository
            .create_permission(
                Principal::User(Uuid::nil()),
                Scope {
                    object: ScopeObject::Collection(UsersResource::User),
                    attributes: AttrObject::All,
                },
                UsersAction::Read,
            )
            .await;
        assert!(matches!(
            result,
            Err(UserPermissionsRepositoryError::InvalidData)
        ));
        let count: i64 = sqlx::query_scalar("SELECT count(*) FROM user_permissions")
            .fetch_one(&pool)
            .await
            .unwrap();
        assert_eq!(count, 0);
    }

    #[sqlx::test(migrations = "migrations/core_db")]
    async fn reports_database_failures(pool: PgPool) {
        sqlx::query("DROP TABLE user_permissions")
            .execute(&pool)
            .await
            .unwrap();
        let repository = UserPermissionsRepository::new(pool);
        assert!(matches!(
            repository
                .get_permissions(Principal::User(Uuid::nil()))
                .await,
            Err(UserPermissionsRepositoryError::Unexpected)
        ));
    }

    #[sqlx::test(migrations = "migrations/core_db")]
    async fn service_authorizes_management_through_postgres(pool: PgPool) {
        use iot_hub::permissions::{
            app::PermissionProvider, errors::PermissionProviderError, schemas::PermissionActor,
        };
        use iot_hub::users::perm::services::UserPermissionsProviderService;

        let repository = UserPermissionsRepository::new(pool.clone());
        let actor = PermissionActor {
            user_id: Uuid::from_u128(1),
            role_id: Uuid::from_u128(2),
        };
        let recipient = PermissionActor {
            user_id: Uuid::from_u128(3),
            role_id: Uuid::from_u128(4),
        };
        let service = UserPermissionsProviderService::new(UserPermissionsRepository::new(pool));
        assert!(matches!(
            service
                .create_user_collection_perm(
                    actor,
                    recipient.user_id,
                    UsersResource::UserContact,
                    UsersAction::Read,
                    HashSet::from([UsersAttribute::Value])
                )
                .await,
            Err(PermissionProviderError::Denied(_))
        ));
        for action in [UsersAction::Create, UsersAction::Read, UsersAction::Delete] {
            repository
                .create_permission(
                    Principal::Role(actor.role_id),
                    Scope {
                        object: ScopeObject::Collection(UsersResource::UserPermission),
                        attributes: AttrObject::All,
                    },
                    action,
                )
                .await
                .unwrap();
        }
        service
            .create_user_collection_perm(
                actor,
                recipient.user_id,
                UsersResource::UserContact,
                UsersAction::Read,
                HashSet::from([UsersAttribute::Value]),
            )
            .await
            .unwrap();
        let object_id = Uuid::from_u128(5);
        service
            .create_role_object_perm(
                actor,
                recipient.role_id,
                UsersResource::UserContact,
                object_id,
                UsersAction::Read,
                HashSet::from([UsersAttribute::State]),
            )
            .await
            .unwrap();
        assert_eq!(
            service
                .check_object_perms(
                    recipient.user_id,
                    recipient.role_id,
                    UsersResource::UserContact,
                    object_id,
                    UsersAction::Read,
                    HashSet::from([UsersAttribute::Value, UsersAttribute::State])
                )
                .await,
            Ok(())
        );
        assert!(matches!(
            service
                .get_permissions(recipient, Principal::Role(recipient.role_id))
                .await,
            Err(PermissionProviderError::Denied(_))
        ));
        let grants = service
            .get_permissions(actor, Principal::Role(recipient.role_id))
            .await
            .unwrap();
        assert_eq!(grants.len(), 1);
        service
            .revoke_permission(actor, grants[0].perm_id)
            .await
            .unwrap();
        assert!(matches!(
            service
                .check_object_perms(
                    recipient.user_id,
                    recipient.role_id,
                    UsersResource::UserContact,
                    object_id,
                    UsersAction::Read,
                    HashSet::from([UsersAttribute::Value, UsersAttribute::State])
                )
                .await,
            Err(PermissionProviderError::Denied(_))
        ));
        assert!(
            service
                .get_permissions(actor, Principal::Role(recipient.role_id))
                .await
                .unwrap()
                .is_empty()
        );
    }

    #[sqlx::test(migrations = "migrations/core_db")]
    async fn round_trips_domain_vocabulary(pool: PgPool) {
        use iot_hub::permissions::app::{AttrObjectTrait, PermissionProvider};
        use iot_hub::users::perm::services::UserPermissionsProviderService;

        let repository = UserPermissionsRepository::new(pool);
        for supported in
            UserPermissionsProviderService::<UserPermissionsRepository>::discover_perms()
        {
            for (action, attrs) in supported.actions {
                let expected_attrs = attrs
                    .attribute_names()
                    .into_iter()
                    .map(str::to_owned)
                    .collect::<HashSet<_>>();
                let grant = repository
                    .create_permission(
                        Principal::User(Uuid::nil()),
                        Scope {
                            object: ScopeObject::Collection(supported.resource),
                            attributes: attrs,
                        },
                        action,
                    )
                    .await
                    .unwrap();
                assert_eq!(grant.action, action);
                assert!(
                    matches!(grant.scope.object, ScopeObject::Collection(resource) if resource == supported.resource)
                );
                assert_eq!(
                    grant
                        .scope
                        .attributes
                        .attribute_names()
                        .into_iter()
                        .map(str::to_owned)
                        .collect::<HashSet<_>>(),
                    expected_attrs
                );
            }
        }
        assert_eq!(
            repository
                .get_permissions(Principal::User(Uuid::nil()))
                .await
                .unwrap()
                .len(),
            19
        );
    }
}
