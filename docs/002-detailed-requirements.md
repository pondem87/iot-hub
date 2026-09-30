# Detailed user requirements

## 1 Organisation management
- 1.1 create an organisation
    1.1.2 organisation must have a unique name
    1.1.3 user creating organisation is superuser by default
    1.1.4 organisation must have 1 superuser
    1.1.5 check users permission to create more than one organisation
- 1.2 manage an organisation
    1.2.1 organisation can have other users whose max number depends on subscription tier
    1.2.2 superuser can change organisation name to a different but unique name
    1.2.3 superuser or allowed user can invite or revoke other users from organisation
- 1.3 manage organisation access
    1.3.1 determine users with access to organisation
    1.3.2 determine user with ability to invite or revoke other users
- 1.4 manage organisation limits
    1.4.1 determine and apply maximum organisation users
    1.4.2 determine and apply organisation service continuity
- 1.5 delete or disable organisation
    1.5.1 superuser can enable or disable an organisation
    1.5.2 superuser can delete an organisation 

## 2 User management
- 2.1 create user
    - 2.1.1 users should be able to register with phone number
    - 2.1.2 phone number verified with code sent via whatsapp
- 2.2 manage user attributes
    - 2.2.1 user should be able to change password
    - 2.2.2 should be able to verify phone number
    - 2.2.3 user should be able to reset password
    - 2.2.4 user should be able to request new verification code
    - 2.2.5 user should be able to set and verify email
    - 2.2.6 user should be able to set and change name
- 2.3 manage user preferences
    - 2.3.1 opt in and out to notifications
    - 2.3.2 choose notification channel
- 2.4 deactivate and delete user
    - should be able to deactivate and delete account

## 3 Verification code management
- 3.1 Create code
    - 3.1.1 generate cryptographically secure 5 digit numeric codes to verify accounts, contacts and password resets
- 3.2 Manage code validity
    - 3.1.2 confirm if code correct and within validity period
- 3.3 Clear out used codes

## 4 Subscription management

## Gateway