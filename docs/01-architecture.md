# High level architecture

We use a layered architecture.
The layers:
1. Ingestion layer (lower MVC): responsible for ingesting health data, status data, sensor data and publishing control signals to device
2. Device management layer: logic for devices, sensors, actuators
3. Business layer: business logic for assets, sites and account management
4. User experience layer (upper MVC)


# Domain models

## Ingestion
- account (tenant)
    - account_id
    - status (enum = ACTIVE, DISABLED)
- subscription
    - account_id
    - status (enum = VALID, EXPIRED)
- device
    - device_id
    - account_id
    - status (enum = ACTIVE, DISABLED)

## Device domain
- device
    - account_id
    - device_id
    - device_name
    - device_state
    - device_status
    - last_seen
    - added_on
    - status (enum = ACTIVE, DISABLED)
- component
    - account_id
    - device_id
    - component_id
    - component_type (enum = SENSOR, ACTUATOR)
    - metric
    - unit
    - value
    - hardware_tag
    - name
    - description
    - added_on
    - status (enum = ACTIVE, DISABLED)
- timeseriesdata
    - time
    - account_id
    - component_id
    - value

## Management domain
- asset
    - account_id
    - asset_id
    - asset_name
    - components -> one to many
- asset subclasses: borehole, pump, generator, tank, power inverter, battery, solar panel

## Automation and control domain
- command
    - account_id
    - command_id
    - user_id
    - device_id
    - hardware_tag
    - value
    - issued_at
    - response_at
    - success
    - response_info
- action
- schedule

## Notification domain
- master_config
    - account_id
    - config_id
    - allow_nofications

- user_config
    - account_id
    - config_id
    - allow_notifications

## User access domain
- session
    - session_id (PK)
    - user_id
    - created_at
    - expires_at
    - revoked_at
- role
    - account_id
    - role_id (pk)
    - role_name
    - description
    - default  (boolean)
- role_permission
    - role_id (uk)
    - account_id (uk)
    - model (uk)
    - action (enum = READ, READ_WRITE, ALL)

## Audit domain
- activity
    - activity_id
    - account_id
    - user_id
    - description
    - timestamp
    - object_type
    - object_id
    - values (json)
    - ip_address
    - source  (whatsapp, web, mobile)

## Account (tenants) domain
- account
    - account_id
    - admin_id
    - account_name (uk)
    - created_at
    - account_status (enum = ACTIVE, DISABLED)
    - marked_for_delete_on
- account_users
    - account_id
    - user_id
    - role_id
    - joined_at
    - status (enum = ACTIVE, SUSPENDED)
- user
    - user_id (phone number)
    - name
    - created_at (timestamp)
    - user_status (enum = ACTIVE, DISABLED)
    - password_hash
    - verified
- verification_code
    - user_id
    - code
    - created_at
#### Value objects
    1. phone_number
    2. password

## Invite domain
- invitation
    - invitation_id
    - account_id
    - user_id
    - created_at
    - status (enum = PENDING, ACCEPTED, DECLINED)

## Subscriptions domain
- subscription
    - subscription_id
    - account_id
    - start_date
    - end_date
    - payment_id
- payment
    - payment_id
    - account_id
    - user_id
    - subscription_type_id
    - status
    - amount
    - channel
    - created_at
    - updated_at
- subscription_type
    - subscription_type_id
    - subscription_name
    - description
    - price
    - subscription_status (enum = ACTIVE, DISABLED, DISCONTINUED)
    - discontinue_date
    - duration
- subscription_limits
    - subscription_limits_id
    - subscription_type_id
    - max_active_users
    - max_active_sites
    - max_active_devices
    - max_components