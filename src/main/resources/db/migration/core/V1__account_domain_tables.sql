create table accounts (
    account_id uuid primary key,
    account_name varchar(255) not null unique,
    admin_id varchar(50),
    status varchar(50) not null,
    marked_for_deletion_at timestamp without time zone,
    created_at timestamp without time zone not null
);

create table users (
    user_id varchar(50) primary key,
    name varchar(50),
    created_at timestamp without time zone not null,
    verified boolean not null,
    password_hash varchar(255) not null,
    status varchar(50) not null
);

create table account_users (
    id uuid primary key,
    account_id uuid not null,
    user_id varchar(50) not null,
    status varchar(50) not null,
    joined_at timestamp without time zone not null,
    foreign key (account_id) references accounts(account_id),
    foreign key (user_id) references users(user_id)
);

create table account_user_roles (
    id uuid primary key,
    account_user_id uuid not null,
    role_id uuid not null,
    foreign key (account_user_id) references account_users(id)
);

create table verification_codes (
    id uuid primary key,
    user_id varchar(50) not null,
    code_hash varchar(255) not null,
    created_at timestamp without time zone not null
);

create table invitations (
    invitation_id uuid primary key,
    account_id uuid not null,
    user_id varchar(50) not null,
    invitation_status varchar(50) not null,
    created_at timestamp without time zone not null
);