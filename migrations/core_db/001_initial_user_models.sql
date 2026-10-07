CREATE TYPE user_type AS ENUM ('superuser', 'staff', 'customer');
CREATE TYPE user_state AS ENUM ('unverified', 'active', 'inactive', 'barred', 'deleted');
CREATE TYPE user_contact_type AS ENUM ('phonenumber', 'emailaddress');
CREATE TYPE user_contact_state AS ENUM ('unverified', 'active', 'disabled');

CREATE TABLE user_profiles (
    id UUID PRIMARY KEY,
    name TEXT NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE user_preferences (
    id UUID PRIMARY KEY,
    allow_notifications BOOLEAN NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE users (
    id UUID PRIMARY KEY,
    phone_number TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,
    "user_type" user_type NOT NULL,
    state user_state NOT NULL,
    profile_id UUID NOT NULL UNIQUE REFERENCES user_profiles (id),
    preferences_id UUID NOT NULL UNIQUE REFERENCES user_preferences (id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE user_contacts (
    id UUID PRIMARY KEY,
    "user_contact_type" user_contact_type NOT NULL,
    value TEXT NOT NULL,
    states user_contact_state NOT NULL,
    user_id UUID NOT NULL REFERENCES users (id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX user_contacts_user_id_idx ON user_contacts (user_id);
