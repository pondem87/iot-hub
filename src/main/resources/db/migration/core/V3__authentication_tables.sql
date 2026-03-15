create table sessions (
    session_id uuid primary key,
    user_id varchar(50) not null,
    user_agent varchar(255) not null,
    created_at timestamp without time zone not null,
    expires_at timestamp without time zone not null,
    revoked_at timestamp without time zone
);