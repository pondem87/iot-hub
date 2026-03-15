create table roles (
    id uuid primary key,
    account_id uuid not null,
    role_name varchar(255) not null,
    description text,
    default_role boolean not null,
    created_at timestamp without time zone not null
);

create table role_permissions (
    id uuid primary key,
    account_id uuid not null,
    role_id uuid not null,
    entity varchar(255) not null,
    action varchar(255) not null
);