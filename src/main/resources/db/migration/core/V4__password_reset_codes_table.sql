create table password_reset_codes (
    id uuid primary key,
    user_id varchar(50) not null,
    code_hash varchar(255) not null,
    created_at timestamp without time zone not null,
    expires_at timestamp without time zone not null
);