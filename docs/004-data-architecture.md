# Data Architecture

## The User aggregrate

### User

- 1 User
    - id: uuid pk
    - phone_number: str unique
    - type: enum {superuser, staff, customer}
    - state: enum {unverified, active, inactive, barred, deleted}
    - profile_id: uuid foreign-key one2one
    - preferences_id: uuid foreign-key one2one
    - created_at: datetime
    - updated_at: datetime

- 2 UserProfile
    - id: uuid pk
    - name: str
    - updated_at: datetime

- 3 UserPreferences
    - id: uuid pk
    - allow_notifications: bool
    - updated_at: datetime

- 4 UserContact
    - id: uuid pk
    - type: enum {phonenumber, emailaddress}
    - value: str
    - states: enum {unverified, active, disabled}
    - user_id: uuid foreign-key many2one
    - created_at: datetime
    - updated_at: datetime