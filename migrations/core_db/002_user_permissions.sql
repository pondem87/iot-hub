CREATE TYPE users_permission_principal_type AS ENUM ('role', 'user');
CREATE TYPE users_permission_type AS ENUM ('collection', 'object');
CREATE TYPE users_permission_resource AS ENUM ('user', 'user_contact', 'user_profile', 'user_preferences', 'user_permission');
CREATE TYPE users_permission_action AS ENUM ('create', 'read', 'update', 'delete');

CREATE TABLE user_permissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    principal_type users_permission_principal_type NOT NULL,
    principal_id UUID NOT NULL,
    perm_type users_permission_type NOT NULL,
    resource users_permission_resource NOT NULL,
    resource_id UUID,
    action users_permission_action NOT NULL,
    attributes TEXT NOT NULL,
    CONSTRAINT user_permissions_scope_check CHECK (
        (perm_type = 'collection' AND resource_id IS NULL)
        OR (perm_type = 'object' AND resource_id IS NOT NULL)
    )
);

-- Principal and resource IDs are polymorphic. Their existence and authorization
-- are service/caller responsibilities; no shared role table exists yet.
CREATE INDEX user_permissions_principal_idx ON user_permissions (principal_type, principal_id);
