# Business Architecture

# Capability Mapping

- 1 User management
    - 1.1 user definition - ability to identify a user and create and retrieve user records
    - 1.2 user profile management - ability to obtain, maintain and set user attributes
    - 1.3 user preference management - abiltiy to obtain, maintain and enforce users needs
    - 1.4 user state management - ability to determine and update user state
    - 1.5 user account management - ability to activate/disable and deleter user account
    - 1.6 user matching - ability to associate a user with other business objects
        - 1.6.1 user/subscription matching
    - 1.7 user contacts management - ability to add, retrieve, verify and delete contacts
        - 1.7.1 contact definition - identify, create and retrieve contacts
        - 1.7.2 contact type management - ability to determine or set contact type
        - 1.7.3 contact state management - ability to determine, set and change contact state

- 2 Organisation management
    - 1.1 organisation definition - ability to identify an organisation, create and retrieve organisation
    - 1.2 organisation superuser management - ability to set and retrieve organisation superuser
    - 1.3 organisation membership management - ability to add, remove and list organisation users
        - 1.3.2.1 organisation member list management - ability to add and remove members from organisation
        - 1.3.2.1 member access management - ability to add and remove member's organisation access constraints
    - 1.4 organisation access management
        - 1.4.1 organisation access constraints definition - ability to create, retrieve and delete an access constraint
        - 1.4.2 organisation access constraints interpretation - ability to understand access limits
        - 1.4.3 organisation access constraints enforcement - ability to enforce access limits
    - 1.5 organisation limits management
        - 1.5.1 organisation limit definition - ability to create, retrieve and delete a limit
        - 1.5.2 organisation limit interpretation - ability to understand a limit
        - 1.5.3 organisation limit enforcement - ability to enforce a limit
    - 1.6 organisation matching - ability to associate to other business objects
        - organisation/user matching
        - organisation/subscription matching

- 3 Verification code management
    - 3.1 verification code definition - ability to generate, store and retrieve verification codes
    - 3.2 verification code validation - ability to determine if a sent code matches the stored code and that it is still valid
    - 3.3 verification code cleanup - ability to clean up codes that have already been used or are not valid
    - 3.4 verification code matching - ability to associate a code with other business objects
        - 3.4.1 verification code/user matching
        - 3.4.2 verification code/contact matching

- 4 Organisation invitation management - ability to provide users with a mechanism allowing them to join the organisation and to track and revoke the issuance of such mechanism to users
    - 4.1 invitation definition - ability to create and retrieve an invitation
    - 4.2 invitation revocation - ability to revoke an invitation to a specified user
    - 4.3 invitation access management - ability to control access to invitations
        - 4.3.1 invitation access constraints definition - ability to create, retrieve and delete an access constraint
        - 4.3.2 invitation access constraints interpretation - ability to understand access constraint
        - 4.3.3 invitation access constraints enforcement - ability to enforce access constraint
    - 4.4 invitation matching - ability to associate an invitation with other business objects
        - 4.4.1 invitation/organisation matching
        - 4.4.2 invitation/user matching

- 5 Event management - ability to create, capture, interpret and distribute and route events and register publishers and subscribers emitted by business processes
    - 5.1 event definition - ability to create events
    - 5.2 event capture - ability to capture events and forward to subscriber
    - 5.3 event distribution - ability to distribute events from publishers to subscribers
    - 5.4 event publisher registration
    - 5.5 event subscriber registration

- 6 Notification management - ability to create and determine which notifications to send to user and which messaging channel to use based on user preferences and permissions
    - 6.1 notification definition - ability to create notification
    - 6.2 notification dispatch determination - ability to determine whether to dispatch notification
    - 6.3 notification channel determination - ability to determine the channel to use to send notification
    - 6.4 notification content construction - ability to make the contents of the notification message

- 6 Message management - ability to create, structure, route and intepret communications media to and from users
    - 6.1 message definition
    - 6.2 message capture
    - 6.3 message structuring
    - 6.4 message dispatch
    - 6.5 message channel determination

- 7 Channel management - ability to send and recieve messages from a communication medium

- 8 Whatsapp api management - ability to convey messages to and from users via whatsapp messages api

- 9 Audit log management - ability create, store and retrieve audit logs


# Informatoion Map

- key:
    - [Number] Information concept
        - Information concept category - primary or secondary
        - Information concept definition
        - Information concept types
        - Related Information concepts
        - Information concept states

- 1 User
    - category: primary
    - definition: a person using the service
    - types: superuser, staff, customer
    - related: subscription
    - states: unverified, active, inactive, barred, deleted

    - 1.1 User Profile
        - category: secondary
        - definition: characteristics describing a user
        - types: none
        - related: none
        - states: none

    - 1.2 User Preferences
        - category: secondary
        - definition: set of parameters representing user needs
        - types: none
        - related: none
        - states: none

    - 1.3 User Contact
        - category: secondary
        - definition: identifier for user in a given communication channel
        - types: phonenumber, emailaddress
        - related: channel
        - states: unverified, active, disabled

- 2 Organisation
    - category: primary
    - definition: a container for assets belonging to the one entity
    - types: none
    - related: subscription
    - states: active, inactive, barred, deleted

    - 1.1 Organisation access constraint
        - category: secondary
        - definition: a policy determining who can access organisation resources
        - types: object, attribute
        - related: none
        - states: none

    - 1.2 Organisation limit
        - category: secondary
        - definition: a policy determining limits on organisation resources
        - types: none
        - related: none
        - states: none

    - 1.3 Membership invitation
        - category: secondary
        - definition: an 

- 3 Verification code
    - category: primary
    - definition: a code used to verify authenticity of a request or contact verification
    - types: otp
    - related: user, contact
    - states: ready, used, expired

- 4 Invitation
    - category: primary
    - definition: mechanism for allowing users to join an organisation
    - types: none
    - related: user, organisation
    - states: active, used, revoked


# Value Map

## 1. Register user value stream

triggering stakeholder: customer (user)
value proposition: customer has verified user account

### value stream stages

submit user details ---> accept user details ---> create user account ---> initialise verification ---> verify user

### Value stream/capability cross-mapping

submit user details
- submission management <= input data capture, form submission, data schema determination and validation

accept user details
- submission management <= data schema validation, phone number validation, name validation, password validation

create user account
- user management <= user definition <= user creation
- event management <= event definition <= event creation, event dispatch, event capture

initialise verification
- verification code generation
- event management <= event definition <= event creation, event dispatch, event capture

verify user
- submission management
- verification code validation
- user management <= user state management
- event management <= event definition <= event creation, event dispatch, event capture


## 2. Notify user

### value stream stages

receive notification request ---> prepare message ---> dispatch message

### Value stream/capability cross-mapping

receive notification request
- event management <= event definition <= event creation, event dispatch, event capture
- notification management <= notification type determination, notification preference determination, notification preference enforcement

prepare
- message management <= message creation, message type determination, channel determination, message dispatch, message status tracking

dispatch message
- channel management <= channel identification, channel configuration, message dispatch
- whatsapp api management <= api configuration, message dispatch


## 3. Onboard user