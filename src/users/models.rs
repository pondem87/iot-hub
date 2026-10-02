use sqlx::types::Uuid;
use chrono::{DateTime, Utc};

/// Represents the different types of users in the system.
#[derive(Debug, Clone, Copy, PartialEq, Eq, sqlx::Type)]
#[sqlx(type_name = "user_type", rename_all = "lowercase")]
pub enum UserType {
    Superuser,
    Staff,
    Customer,
}

/// Represents the different states a user can be in.
#[derive(Debug, Clone, Copy, PartialEq, Eq, sqlx::Type)]
#[sqlx(type_name = "user_state", rename_all = "lowercase")]
pub enum UserState {
    Unverified,
    Active,
    Inactive,
    Barred,
    Deleted,
}

/// Represents the different types of user contacts (e.g., phone number, email address).
#[derive(Debug, Clone, Copy, PartialEq, Eq, sqlx::Type)]
#[sqlx(type_name = "user_contact_type", rename_all = "lowercase")]
pub enum UserContactType {
    PhoneNumber,
    EmailAddress,
}

/// Represents the different states a user contact can be in.
#[derive(Debug, Clone, Copy, PartialEq, Eq, sqlx::Type)]
#[sqlx(type_name = "user_contact_state", rename_all = "lowercase")]
pub enum UserContactState {
    Unverified,
    Active,
    Disabled,
}


/// User states for type state pattern
pub struct UnverifiedUser;

pub struct ActiveUser;

pub struct InactiveUser;

pub struct BarredUser;

pub struct DeletedUser;

pub struct AnyUser;

/// Represents a user in the system.
#[derive(Debug, Clone, sqlx::FromRow)]
pub struct User<State> {
    pub id: Uuid,
    pub phone_number: String,
    pub user_type: UserType,
    pub state: UserState,
    pub profile_id: Uuid,
    pub preferences_id: Uuid,
    pub created_at: DateTime<Utc>,
    pub updated_at: DateTime<Utc>,
    /// Marker for the state type of the user.
    state_marker: std::marker::PhantomData<State>,
}


impl User<UnverifiedUser> {}

impl User<ActiveUser> {}

impl User<InactiveUser> {}

impl User<BarredUser> {}

impl User<DeletedUser> {}

#[derive(Debug, Clone, sqlx::FromRow)]
pub struct UserProfile {
    pub id: Uuid,
    pub name: String,
    pub updated_at: DateTime<Utc>,
}

#[derive(Debug, Clone, sqlx::FromRow)]
pub struct UserPreferences {
    pub id: Uuid,
    pub allow_notifications: bool,
    pub updated_at: DateTime<Utc>,
}


/// User contact states for type state pattern
pub struct UnverifiedContact;

pub struct ActiveContact;

pub struct DisabledContact;

/// Represents a contact method for a user.
#[derive(Debug, Clone, sqlx::FromRow)]
pub struct UserContact<State> {
    pub id: Uuid,
    pub contact_type: UserContactType,
    pub value: String,
    pub states: UserContactState,
    pub user_id: Uuid,
    pub created_at: DateTime<Utc>,
    pub updated_at: DateTime<Utc>,
    /// Marker for the state type of the user contact.
    #[sqlx(skip)]
    state_marker: std::marker::PhantomData<State>,
}

impl UserContact<UnverifiedContact> {}

impl UserContact<ActiveContact> {}

impl UserContact<DisabledContact> {}
