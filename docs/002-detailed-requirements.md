# Detailed requirements

## 1 Functional requirements

These records preserve the obligations in the original detailed requirements and
elaborate goals already stated in 001. Acceptance statements describe observable
checks without selecting unspecified policies. Source labels identify the original
document and section; they are historical references, not current heading numbers.
The resolved portions of [OPEN-002-1](#open-002-1), [OPEN-002-2](#open-002-2), and
[OPEN-002-3](#open-002-3) record the subsequent user decisions reflected in the
requirements and acceptance criteria below.

### 1.1 Organisation management

Actor and trigger: Users creating organisations; superusers and authorized users
managing existing organisations. Each record's operation specifies the trigger.
Permission details beyond the source remain open.

<a id="req-1.1.1"></a>

**REQ-1.1.1**

- **Required outcome and constraints:** Create an organisation with a unique name.
  Compare names ignoring case, trimming leading/trailing whitespace, and collapsing
  consecutive internal whitespace to a single space. Apply the same comparison on
  creation and rename.
- **Acceptance criterion:** `Acme Ltd`, `ACME LTD`, and ` Acme  Ltd ` conflict.
  A creation or rename that conflicts with another organisation is rejected,
  including concurrent conflicting requests. Distinct comparison values are allowed.
- **Source:** 002 / Organisation / 1.1 create; 1.1.2 unique name

<a id="req-1.1.2"></a>

**REQ-1.1.2**

- **Required outcome and constraints:** Make the creating user the organisation superuser
  by default.
- **Acceptance criterion:** After creation, the creator is assigned the superuser role.
- **Source:** 002 / Organisation / 1.1.3

<a id="req-1.1.3"></a>

**REQ-1.1.3**

- **Required outcome and constraints:** An organisation must have exactly one superuser.
  Transfer to another user is allowed only following verification of the transfer
  by the current superuser. A completed transfer is irreversible: the previous
  superuser immediately loses that status and the recipient becomes the superuser.
- **Acceptance criterion:** Before successful verification, the current superuser
  retains the role. Failed verification leaves ownership unchanged. On successful
  transfer, the recipient is the sole superuser, the previous user's superuser
  permissions cease immediately, and the previous user cannot undo the transfer.
  Verification method and recipient eligibility remain in
  [OPEN-002-1](002-detailed-requirements.md#open-002-1).
- **Source:** 002 / Organisation / 1.1.4

<a id="req-1.1.4"></a>

**REQ-1.1.4**

- **Required outcome and constraints:** Check the user's permission and subscription
  limit before allowing another organisation. The user limit is the number of
  organisations in which the user holds a superuser account/role. The default
  allowance is one organisation per superuser.
- **Acceptance criterion:** Creation succeeds only within the subscription's allowed
  superuser-account count. A request that would exceed the applicable limit fails.
  Non-default tier values and lifecycle counting cases remain in
  [OPEN-002-4](002-detailed-requirements.md#open-002-4).
- **Source:** 002 / Organisation / 1.1.5; 001 / Organisations

<a id="req-1.2.1"></a>

**REQ-1.2.1**

- **Required outcome and constraints:** Allow additional organisation users up to the
  maximum number of users set by the applicable subscription. The default is three
  users total per organisation, including its superuser.
- **Acceptance criterion:** Adding a user within the applicable subscription maximum
  is permitted; an addition exceeding it fails. Under the default, a superuser and
  two other members fill the allowance; a fourth user is rejected. Non-default tier
  values and remaining counting details remain in [OPEN-002-4](002-detailed-requirements.md#open-002-4).
- **Source:** 002 / Organisation / 1.2.1

<a id="req-1.2.2"></a>

**REQ-1.2.2**

- **Required outcome and constraints:** Allow the superuser to rename the organisation
  to another unique name, using the case-insensitive, trimmed, collapsed-whitespace
  comparison in [REQ-1.1.1](002-detailed-requirements.md#req-1.1.1).
- **Acceptance criterion:** An authorized rename succeeds only if its comparison
  value does not conflict with another organisation's name.
- **Source:** 002 / Organisation / 1.2.2

<a id="req-1.2.3"></a>

**REQ-1.2.3**

- **Required outcome and constraints:** Allow the superuser or an allowed user to invite
  or revoke organisation users.
- **Acceptance criterion:** Authorized invitations and revocations affect membership;
  eligibility and revocation effects require
  [OPEN-002-1](002-detailed-requirements.md#open-002-1).
- **Source:** 002 / Organisation / 1.2.3

<a id="req-1.3.1"></a>

**REQ-1.3.1**

- **Required outcome and constraints:** Determine which users may access an organisation.
- **Acceptance criterion:** Access decisions use an explicit organisation
  membership/access policy; policy cases require
  [OPEN-002-1](002-detailed-requirements.md#open-002-1).
- **Source:** 002 / Organisation / 1.3.1

<a id="req-1.3.2"></a>

**REQ-1.3.2**

- **Required outcome and constraints:** Determine which users may invite or revoke
  organisation users.
- **Acceptance criterion:** An invitation or revocation checks the caller’s corresponding
  permission.
- **Source:** 002 / Organisation / 1.3.2

<a id="req-1.4.1"></a>

**REQ-1.4.1**

- **Required outcome and constraints:** The organisation limit is its number of users,
  with the maximum supplied by subscription; the default is three users total, including
  the superuser.
  Enforce that maximum on membership
  additions.
- **Acceptance criterion:** Membership additions cannot exceed the subscription's
  maximum, including concurrent additions. Behaviour for an existing membership
  exceeding a reduced maximum remains in
  [OPEN-002-4](002-detailed-requirements.md#open-002-4).
- **Source:** 002 / Organisation / 1.4.1

<a id="req-1.4.2"></a>

**REQ-1.4.2**

- **Required outcome and constraints:** When a user's subscription tier expires,
  randomly select and disable excess organisations for which that user is superuser
  until the number remaining enabled meets the default allowance of one organisation
  per superuser. Disable rather than delete them; retain their data indefinitely under
  [REQ-1.5.1](002-detailed-requirements.md#req-1.5.1).
- **Acceptance criterion:** Given five enabled organisations when the tier expires,
  four distinct organisations are selected randomly and disabled, leaving one enabled. At or below the allowance, none need disabling. Existing
  users, superuser assignments, and organisation data are preserved. Reactivation
  conditions remain in
  [OPEN-002-4](002-detailed-requirements.md#open-002-4).
- **Source:** 002 / Organisation / 1.4.2

<a id="req-1.5.1"></a>

**REQ-1.5.1**

- **Required outcome and constraints:** Allow the organisation superuser to enable or
  disable the organisation. Disabling halts all organisation activity until the
  superuser reactivates it; its data is retained indefinitely while disabled.
- **Acceptance criterion:** After disabling, organisation activity cannot proceed.
  Superuser reactivation restores availability; another user cannot reactivate it.
  Disabled data remains retained beyond 30 days without deletion cleanup.
- **Source:** 002 / Organisation / 1.5.1

<a id="req-1.5.2"></a>

**REQ-1.5.2**

- **Required outcome and constraints:** Allow the superuser to delete the organisation.
  In the deleted state, it is completely disabled: reactivation is the only allowed
  organisation activity before the 30-day deadline. Retain it for 30 days after
  deletion, then clean up the organisation and all assets it owns. Users are not
  owned by organisations and must not be deleted by this cleanup. Reactivation
  before the deadline cancels pending cleanup.
- **Acceptance criterion:** While deleted, organisation operations other than
  reactivation fail, including asset operations and superuser transfer. Reactivation
  before deletion time plus 30 days cancels cleanup; at or after that deadline it
  fails and cleanup removes the organisation and all its owned assets, preserving
  user accounts and assets owned by other organisations. Scheduled cleanup is the
  required retention action, not an available user activity. Deleted-organisation
  reactivation authorization remains in
  [OPEN-002-1](002-detailed-requirements.md#open-002-1).
- **Source:** 002 / Organisation / 1.5.2

### 1.2 User management

Actor and trigger: A person registers; a user requests changes to their account, contacts,
profile, or preferences. Authentication and reset preconditions require
[OPEN-002-2](002-detailed-requirements.md#open-002-2).

<a id="req-2.1.1"></a>

**REQ-2.1.1**

- **Required outcome and constraints:** Allow registration using a phone number in
  international form: a leading `+`, country code, and subscriber digits, without
  spaces or punctuation; for example, `+26774178111` (Botswana). Duplicate accounts
  are not allowed; registration with an existing account phone number must fail.
- **Acceptance criterion:** An unused phone number in the required form can identify
  a new account. A number without `+` or containing spaces or separators is rejected.
  A duplicate registration fails without creating another account, including when
  requests race. Additional duplicate-identity rules remain in
  [OPEN-002-2](002-detailed-requirements.md#open-002-2).
- **Source:** 002 / User / 2.1.1

<a id="req-2.1.2"></a>

**REQ-2.1.2**

- **Required outcome and constraints:** Verify the phone number using a code sent through
  WhatsApp.
- **Acceptance criterion:** The workflow sends a verification code through WhatsApp and
  checks it using the lifetime and single-use rules in
  [REQ-3.2.1](002-detailed-requirements.md#req-3.2.1) and generation limits in
  [REQ-3.1.2](002-detailed-requirements.md#req-3.1.2).
- **Source:** 002 / User / 2.1.2

<a id="req-2.2.1"></a>

**REQ-2.2.1**

- **Required outcome and constraints:** Allow a user to change their password. Every
  password set during registration, change, or reset must have at least eight
  characters, including at least one lowercase letter, one uppercase letter, and
  one digit.
- **Acceptance criterion:** An eight-character password containing all three required
  classes satisfies this rule. A shorter password or one missing any required class
  is rejected without replacing the credential. Authorization remains in
  [OPEN-002-2](002-detailed-requirements.md#open-002-2).
- **Source:** 002 / User / 2.2.1

<a id="req-2.2.2"></a>

**REQ-2.2.2**

- **Required outcome and constraints:** Allow a user to verify their phone number.
- **Acceptance criterion:** A valid verification result updates phone verification status;
  account/contact coupling requires [OPEN-002-2](002-detailed-requirements.md#open-002-2).
- **Source:** 002 / User / 2.2.2

<a id="req-2.2.3"></a>

**REQ-2.2.3**

- **Required outcome and constraints:** Allow a user to reset their password, enforcing
  [REQ-2.2.1](002-detailed-requirements.md#req-2.2.1) for the replacement password.
- **Acceptance criterion:** The reset flow establishes the required proof and changes
  the password only if it meets the password rule. Reset verification codes follow
  [REQ-3.2.1](002-detailed-requirements.md#req-3.2.1). Reset authorization and proof
  scope remain in [OPEN-002-2](002-detailed-requirements.md#open-002-2).
- **Source:** 002 / User / 2.2.3

<a id="req-2.2.4"></a>

**REQ-2.2.4**

- **Required outcome and constraints:** Allow a user to request a new verification code.
  A successful resend creates a new code and invalidates the previous code for the
  same verification and service-supplied purpose. Codes for other purposes are
  unaffected. Resends count toward
  [REQ-3.1.2](002-detailed-requirements.md#req-3.1.2).
- **Acceptance criterion:** After a successful resend, the old code fails validation
  and the new code has its own five-minute lifetime. A blocked resend generates
  no new code.
- **Source:** 002 / User / 2.2.4

<a id="req-2.2.5"></a>

**REQ-2.2.5**

- **Required outcome and constraints:** Allow a user to set and verify an email address.
  Email addresses must be represented in lowercase.
- **Acceptance criterion:** A saved email address contains no uppercase letters and
  the user can establish its verification status. Whether mixed-case input is
  converted or rejected, and the proof mechanism, remain in
  [OPEN-002-2](002-detailed-requirements.md#open-002-2).
- **Source:** 002 / User / 2.2.5

<a id="req-2.2.6"></a>

**REQ-2.2.6**

- **Required outcome and constraints:** Allow a user to set and change their name and
  manage their profile.
- **Acceptance criterion:** The user can save and retrieve a changed name; validation
  constraints require [OPEN-002-2](002-detailed-requirements.md#open-002-2).
- **Source:** 002 / User / 2.2.6; 001 / Users

<a id="req-2.3.1"></a>

**REQ-2.3.1**

- **Required outcome and constraints:** Allow a user to opt in or out of notifications.
- **Acceptance criterion:** Changing the preference affects notification dispatch
  decisions.
- **Source:** 002 / User / 2.3.1

<a id="req-2.3.2"></a>

**REQ-2.3.2**

- **Required outcome and constraints:** Allow a user to choose a notification channel.
  WhatsApp is the default communication channel.
- **Acceptance criterion:** A user without an explicit channel choice defaults to
  WhatsApp. A supported choice is stored and used for dispatch, subject to
  [REQ-2.3.1](002-detailed-requirements.md#req-2.3.1). Other supported channels and
  fallback remain in [OPEN-002-2](002-detailed-requirements.md#open-002-2).
- **Source:** 002 / User / 2.3.2

<a id="req-2.4.1"></a>

**REQ-2.4.1**

- **Required outcome and constraints:** Allow a user to deactivate and delete their
  account. Deleted user accounts are retained for 30 days after deletion and then
  cleaned up.
- **Acceptance criterion:** Deletion records the deletion time, retains the account
  for 30 days, and makes it due for cleanup when that period ends. Deactivation
  rules, account restoration, and related-data cleanup scope remain in
  [OPEN-002-2](002-detailed-requirements.md#open-002-2).
- **Source:** 002 / User / 2.4 unnumbered item

<a id="req-2.5.1"></a>

**REQ-2.5.1**

- **Required outcome and constraints:** Authenticate users and restrict resource access
  according to access controls.
- **Acceptance criterion:** An authenticated identity is evaluated against organisation
  and object permissions; policy and authentication details require
  [OPEN-002-2](002-detailed-requirements.md#open-002-2).
- **Source:** 001 / Users; Organisations; Sites, gateway, assets, sensors and assets

### 1.3 Verification code management

Actor and trigger: Registration, contact verification, and password reset initiate code
generation or validation; cleanup removes consumed or invalid codes.

<a id="req-3.1.1"></a>

**REQ-3.1.1**

- **Required outcome and constraints:** Generate cryptographically secure five-digit
  numeric codes for accounts, contacts, and password resets. Each requesting service
  must supply a purpose, and every generated code is paired with that purpose.
- **Acceptance criterion:** Generated codes contain five numeric digits and use a
  cryptographically secure generator; review generation and format, including leading
  zeros. Generation without a purpose fails; a successful generation retains the
  supplied purpose with the code.
- **Source:** 002 / Verification code / 3.1.1

<a id="req-3.1.2"></a>

**REQ-3.1.2**

- **Required outcome and constraints:** Generating five codes within ten minutes
  for the same service-supplied purpose triggers a one-hour block on further code
  generation for that purpose. Counters and blocks are independent per purpose.
  A second block for that purpose triggered within 24 hours of the first block's
  start lasts 24 hours. Resends count as generation; blocked requests do not
  generate codes.
- **Acceptance criterion:** The fifth generated code in a ten-minute window triggers
  a block for that purpose; further generation for it fails until the block expires.
  Other purposes remain unaffected. If the threshold for the same purpose
  is reached again within 24 hours of the first block's start, the second block
  lasts 24 hours from its own start. Verify first-block and second-block expiry,
  resend counting, concurrent generation, and independence between purposes.
  Remaining window and escalation cases require
  [OPEN-002-3](002-detailed-requirements.md#open-002-3).
- **Source:** User decision resolving generation limits in
  [OPEN-002-3](002-detailed-requirements.md#open-002-3).

<a id="req-3.2.1"></a>

**REQ-3.2.1**

- **Required outcome and constraints:** A code is valid for five minutes from generation
  and can be used successfully only once. Check the submitted value, proof scope,
  service-supplied purpose, expiry, and whether the code has been used or invalidated
  by resend. A code issued for one purpose cannot authorize another purpose.
- **Acceptance criterion:** A matching, unused, non-invalidated code for the applicable
  proof can succeed before generation time plus five minutes. At or after that
  deadline it fails. Mismatched, used, or invalidated codes fail even before expiry;
  a code submitted for a different purpose also fails. Concurrent submissions
  cannot both consume the same code successfully.
- **Source:** 002 / Verification code / 3.2, incorrectly numbered 3.1.2

<a id="req-3.3.1"></a>

**REQ-3.3.1**

- **Required outcome and constraints:** Discard all verification codes within 24 hours
  of generation, including used, expired, and resend-invalidated codes. Physical
  retention must never extend a code's validity.
- **Acceptance criterion:** Cleanup removes codes no later than generation time plus
  24 hours. Used, expired, or invalidated codes cannot validate while awaiting
  cleanup. Removing code records does not erase an unexpired generation block or
  the history still needed for [REQ-3.1.2](002-detailed-requirements.md#req-3.1.2).
- **Source:** 002 / Verification code / 3.3; 003 / CAP 3.3

### 1.4 Subscription management

Actor and trigger: Subscription policy supplies entitlements to organisation operations.

Subscription sets the maximum users per organisation (default: three including the
superuser) and the maximum organisations per superuser (default: one), under
[REQ-1.1.4](002-detailed-requirements.md#req-1.1.4) and
[REQ-1.4.1](002-detailed-requirements.md#req-1.4.1). On tier expiry, excess
organisations are disabled randomly under
[REQ-1.4.2](002-detailed-requirements.md#req-1.4.2). Non-default tier values and
remaining subscription behavior are [OPEN-002-4](002-detailed-requirements.md#open-002-4).

### 1.5 Sites, assets, and gateways

Actor and trigger: Authorized users configure the system; gateways transmit observations
and commands; sensors and actuators operate within their assignments.

<a id="req-5.1.1"></a>

**REQ-5.1.1**

- **Required outcome and constraints:** Scope assets to organisations, with sites
  containing assets and assets containing sensors and actuators.
- **Acceptance criterion:** Configuration represents the organisation/site/asset/device
  hierarchy; ownership and reassignment cases require
  [OPEN-002-5](002-detailed-requirements.md#open-002-5).
- **Source:** 001 / Organisations; Sites, gateway, assets, sensors and assets; Assets

<a id="req-5.1.2"></a>

**REQ-5.1.2**

- **Required outcome and constraints:** Allow users to create and link these objects
  according to organisation and object access levels.
- **Acceptance criterion:** Creation and linking evaluate the relevant permissions;
  detailed policy requires [OPEN-002-5](002-detailed-requirements.md#open-002-5).
- **Source:** 001 / Sites, gateway, assets, sensors and assets

<a id="req-5.2.1"></a>

**REQ-5.2.1**

- **Required outcome and constraints:** Use gateways to transmit sensor telemetry and
  actuator commands.
- **Acceptance criterion:** A supported gateway conveys observations upstream and commands
  downstream; protocols and delivery criteria require
  [OPEN-002-5](002-detailed-requirements.md#open-002-5).
- **Source:** 001 / Overview; Organisations; Gateway

<a id="req-5.2.2"></a>

**REQ-5.2.2**

- **Required outcome and constraints:** Link every sensor and actuator to a gateway for
  transmission and reception.
- **Acceptance criterion:** A configured sensor or actuator has a gateway association used
  for its communication; reassignment rules require
  [OPEN-002-5](002-detailed-requirements.md#open-002-5).
- **Source:** 001 / Gateway

<a id="req-5.2.3"></a>

**REQ-5.2.3**

- **Required outcome and constraints:** Allow gateway configuration and obtaining
  certificates to authenticate the physical gateway.
- **Acceptance criterion:** An authorized user can configure a gateway and obtain its
  authentication certificate; issuance policy requires
  [OPEN-002-5](002-detailed-requirements.md#open-002-5).
- **Source:** 001 / Gateway

<a id="req-5.3.1"></a>

**REQ-5.3.1**

- **Required outcome and constraints:** Collect sensor observations and accept actuator
  control commands.
- **Acceptance criterion:** The system represents sensor readings and actuator commands;
  supported formats and command outcomes require
  [OPEN-002-5](002-detailed-requirements.md#open-002-5).
- **Source:** 001 / Sensors and actuators

<a id="req-5.4.1"></a>

**REQ-5.4.1**

- **Required outcome and constraints:** Allow sensor threshold values to trigger actions
  when reached.
- **Acceptance criterion:** A threshold condition can initiate its associated action;
  crossing, equality, and repeat behavior require
  [OPEN-002-5](002-detailed-requirements.md#open-002-5).
- **Source:** 001 / Thresholds and actions

<a id="req-5.4.2"></a>

**REQ-5.4.2**

- **Required outcome and constraints:** Use actions to change actuator states and send
  commands to actuators.
- **Acceptance criterion:** An action results in the corresponding command and intended
  state change; acknowledgement and failure rules require
  [OPEN-002-5](002-detailed-requirements.md#open-002-5).
- **Source:** 001 / Thresholds and actions

## 2 Quality requirements

<a id="nfr-1.1"></a>

**NFR-1.1**

- **Context and obligation:** Gateway connections to the MQTT server must use mutual TLS.
- **Acceptance evidence:** Verify client and server authentication and rejection of
  invalid credentials; certificate policy is
  [OPEN-002-5](002-detailed-requirements.md#open-002-5).
- **Source:** 001 / Security, first item

<a id="nfr-1.2"></a>

**NFR-1.2**

- **Context and obligation:** Determine MQTT topic access policy only after confirming
  gateway identity.
- **Acceptance evidence:** An unidentified gateway cannot receive an authenticated topic
  policy; topic rules are [OPEN-002-5](002-detailed-requirements.md#open-002-5).
- **Source:** 001 / Security, second item

<a id="nfr-1.3"></a>

**NFR-1.3**

- **Context and obligation:** Provide a mechanism to invalidate gateway certificates.
- **Acceptance evidence:** An invalidated certificate loses access under the agreed
  propagation policy; mechanism and timing are
  [OPEN-002-5](002-detailed-requirements.md#open-002-5).
- **Source:** 001 / Security, third item

<a id="nfr-2.1"></a>

**NFR-2.1**

- **Context and obligation:** Provide mobile, web, and WhatsApp chatbot access.
- **Acceptance evidence:** Each channel supports an agreed set of user journeys; feature
  parity and accessibility criteria are
  [OPEN-002-6](002-detailed-requirements.md#open-002-6).
- **Source:** 001 / Accessibility

<a id="nfr-3.1"></a>

**NFR-3.1**

- **Context and obligation:** The cloud service should have high availability.
- **Acceptance evidence:** Availability target, measurement window, recovery objectives,
  and load assumptions are [OPEN-002-6](002-detailed-requirements.md#open-002-6).
- **Source:** 001 / Reliability, first item

<a id="nfr-3.2"></a>

**NFR-3.2**

- **Context and obligation:** Prefer and recommend gateways with store-and-send behavior.
- **Acceptance evidence:** Gateway selection and user guidance identify store-and-send
  support; this recommendation is not a mandatory device admission rule.
- **Source:** 001 / Reliability, second item

<a id="nfr-4.1"></a>

**NFR-4.1**

- **Context and obligation:** Use simple interface language understandable by nontechnical
  people.
- **Acceptance evidence:** Evaluate agreed journeys with representative users; usability
  acceptance measures are [OPEN-002-6](002-detailed-requirements.md#open-002-6).
- **Source:** 001 / Usability

## 3 Downstream traceability

**Requirement area: Organisation rules**

- **Business mapping:** [CAP-2](003-business-architecture.md#cap-2),
  [CAP-4](003-business-architecture.md#cap-4),
  [INFO-2](003-business-architecture.md#info-2)
- **Downstream design:** [APP-8](005-application-architecture.md#app-8); detailed data
  design remains open.

**Requirement area: User registration and verification**

- **Business mapping:** [VS-1](003-business-architecture.md#vs-1),
  [CAP-1](003-business-architecture.md#cap-1),
  [CAP-3](003-business-architecture.md#cap-3),
  [INFO-1](003-business-architecture.md#info-1),
  [INFO-3](003-business-architecture.md#info-3)
- **Downstream design:** [DATA-1](004-data-architecture.md#data-1),
  [DATA-4](004-data-architecture.md#data-4),
  [APP-2](005-application-architecture.md#app-2),
  [APP-3](005-application-architecture.md#app-3),
  [TECH-3](006-technology-architecture.md#tech-3)

**Requirement area: Preferences and notifications**

- **Business mapping:** [VS-2](003-business-architecture.md#vs-2),
  [CAP-6](003-business-architecture.md#cap-6),
  [CAP-7](003-business-architecture.md#cap-7),
  [CAP-8](003-business-architecture.md#cap-8), [CAP-9](003-business-architecture.md#cap-9)
- **Downstream design:** [DATA-3](004-data-architecture.md#data-3),
  [APP-5](005-application-architecture.md#app-5),
  [APP-6](005-application-architecture.md#app-6)

**Requirement area: Telemetry, gateways, thresholds**

- **Business mapping:** [OPEN-003-4](003-business-architecture.md#open-003-4) identifies
  incomplete business mapping.
- **Downstream design:** [APP-8](005-application-architecture.md#app-8),
  [TECH-4](006-technology-architecture.md#tech-4),
  [TECH-6](006-technology-architecture.md#tech-6)

**Requirement area: Security, availability, access channels**

- **Business mapping:** [OPEN-002-6](002-detailed-requirements.md#open-002-6) tracks
  acceptance targets.
- **Downstream design:** [APP-1](005-application-architecture.md#app-1),
  [APP-7](005-application-architecture.md#app-7),
  [TECH-5](006-technology-architecture.md#tech-5),
  [TECH-6](006-technology-architecture.md#tech-6)

## 4 Open decisions

<a id="open-002-1"></a>

**OPEN-002-1**

- **Status:** Partially resolved by the user decisions on organisation rules.
- **Resolved:** Name uniqueness ignores case, trims, and collapses whitespace
  ([REQ-1.1.1](002-detailed-requirements.md#req-1.1.1)). Verified superuser transfer
  is irreversible and immediately removes the former superuser's status
  ([REQ-1.1.3](002-detailed-requirements.md#req-1.1.3)). Subscription sets both limit
  dimensions; tier expiry randomly disables excess organisations
  ([REQ-1.1.4](002-detailed-requirements.md#req-1.1.4),
  [REQ-1.4.1](002-detailed-requirements.md#req-1.4.1),
  [REQ-1.4.2](002-detailed-requirements.md#req-1.4.2)). Disable retains data
  indefinitely; deletion permits only reactivation before 30-day cleanup of all
  organisation-owned assets, excluding users
  ([REQ-1.5.1](002-detailed-requirements.md#req-1.5.1),
  [REQ-1.5.2](002-detailed-requirements.md#req-1.5.2)).
- **Decision needed and affected work:** Define membership permissions, verification
  method and recipient eligibility for transfer, the former superuser's remaining
  membership/role, and who may reactivate a deleted organisation. Subscription
  values, transfer-limit interactions, and expiry/reactivation cases are tracked in
  [OPEN-002-4](002-detailed-requirements.md#open-002-4).

<a id="open-002-2"></a>

**OPEN-002-2**

- **Status:** Partially resolved by the user decision on account rules.
- **Resolved:** International phone representation and rejection of duplicate accounts
  ([REQ-2.1.1](002-detailed-requirements.md#req-2.1.1)); lowercase email
  ([REQ-2.2.5](002-detailed-requirements.md#req-2.2.5)); passwords of at least eight
  characters with lowercase, uppercase, and digits
  ([REQ-2.2.1](002-detailed-requirements.md#req-2.2.1)); WhatsApp by default
  ([REQ-2.3.2](002-detailed-requirements.md#req-2.3.2)); and 30-day retention after
  account deletion ([REQ-2.4.1](002-detailed-requirements.md#req-2.4.1)).
- **Decision needed and affected work:** Define supported phone numbering ranges,
  conversion versus rejection of mixed-case email input, email uniqueness and other
  duplicate-identity criteria, and identifier reuse after deletion. Name validation,
  mandatory registration fields, authentication/reset authorization and proof,
  role scope, other channels/fallback, account/contact state coupling, account
  restoration, and related-data cleanup scope remain open.

<a id="open-002-3"></a>

**OPEN-002-3**

- **Status:** Partially resolved by the user decisions on verification codes and
  service-supplied purposes.
- **Resolved:** Every code is paired with a purpose supplied by the requesting
  service ([REQ-3.1.1](002-detailed-requirements.md#req-3.1.1)); generation counts and
  blocks apply independently per purpose. Five-minute lifetime and single use
  ([REQ-3.2.1](002-detailed-requirements.md#req-3.2.1)); resend replacement
  ([REQ-2.2.4](002-detailed-requirements.md#req-2.2.4)); five codes in ten minutes,
  a one-hour block, and a second block within 24 hours lasting 24 hours
  ([REQ-3.1.2](002-detailed-requirements.md#req-3.1.2)); all codes discarded within
  24 hours ([REQ-3.3.1](002-detailed-requirements.md#req-3.3.1)).
- **Decision needed and affected work:** Define the purpose vocabulary and how each
  service binds a purpose to the intended user/contact/request; rolling versus
  fixed ten-minute windows and exact window boundaries; subsequent-block
  escalation/reset behavior; and any failed-validation attempt limit. These
  constrain complete throttle and proof acceptance without changing the agreed
  per-purpose scope, durations, and thresholds.

<a id="open-002-4"></a>

**OPEN-002-4**

- **Resolved:** Subscription controls organisation user counts and each user's
  superuser-account count. Tier expiry randomly disables excess organisations;
  see [REQ-1.1.4](002-detailed-requirements.md#req-1.1.4),
  [REQ-1.4.1](002-detailed-requirements.md#req-1.4.1), and
  [REQ-1.4.2](002-detailed-requirements.md#req-1.4.2).
- **Defaults:** One organisation per superuser and three users total per organisation,
  including its superuser (up to two other members). Expiry restores the default
  organisation allowance.
- **Decision needed and affected work:** Define non-default tier values; how
  disabled/deleted organisations count for creation and transfer; transfer to a recipient at their limit; whether
  pending invitations count toward organisation membership; handling of existing members
  above a reduced limit; and reactivation after expiry or renewal. Complete the
  subscription lifecycle and user/organisation entitlement relationships.

<a id="open-002-5"></a>

**OPEN-002-5**

- **Decision needed and affected work:** Elaborate gateway/site/asset/sensor/actuator and
  threshold rules, protocols, certificate issuance/invalidation, topic policies,
  reassignment, and command delivery outcomes. The original Gateway heading was empty;
  current records derive only from 001.

<a id="open-002-6"></a>

**OPEN-002-6**

- **Decision needed and affected work:** Define measurable availability, recovery,
  usability, and access-channel acceptance targets and coverage. Avoid inventing
  percentages, latency targets, or deployment topology.

## 5 Legacy reference mapping

Each source item below is retained. Domain subsections group the corresponding
records; the second numeric segment identifies the original operation group.
Record IDs do not need to match presentation headings.

**Original section and label: 002 / Organisation / 1.1 create; 1.1.2 unique name**

- **New identifier or location:** [REQ-1.1.1](002-detailed-requirements.md#req-1.1.1)

**Original section and label: 002 / Organisation / 1.1.3**

- **New identifier or location:** [REQ-1.1.2](002-detailed-requirements.md#req-1.1.2)

**Original section and label: 002 / Organisation / 1.1.4**

- **New identifier or location:** [REQ-1.1.3](002-detailed-requirements.md#req-1.1.3)

**Original section and label: 002 / Organisation / 1.1.5; 001 / Organisations**

- **New identifier or location:** [REQ-1.1.4](002-detailed-requirements.md#req-1.1.4)

**Original section and label: 002 / Organisation / 1.2.1**

- **New identifier or location:** [REQ-1.2.1](002-detailed-requirements.md#req-1.2.1)

**Original section and label: 002 / Organisation / 1.2.2**

- **New identifier or location:** [REQ-1.2.2](002-detailed-requirements.md#req-1.2.2)

**Original section and label: 002 / Organisation / 1.2.3**

- **New identifier or location:** [REQ-1.2.3](002-detailed-requirements.md#req-1.2.3)

**Original section and label: 002 / Organisation / 1.3.1**

- **New identifier or location:** [REQ-1.3.1](002-detailed-requirements.md#req-1.3.1)

**Original section and label: 002 / Organisation / 1.3.2**

- **New identifier or location:** [REQ-1.3.2](002-detailed-requirements.md#req-1.3.2)

**Original section and label: 002 / Organisation / 1.4.1**

- **New identifier or location:** [REQ-1.4.1](002-detailed-requirements.md#req-1.4.1)

**Original section and label: 002 / Organisation / 1.4.2**

- **New identifier or location:** [REQ-1.4.2](002-detailed-requirements.md#req-1.4.2)

**Original section and label: 002 / Organisation / 1.5.1**

- **New identifier or location:** [REQ-1.5.1](002-detailed-requirements.md#req-1.5.1)

**Original section and label: 002 / Organisation / 1.5.2**

- **New identifier or location:** [REQ-1.5.2](002-detailed-requirements.md#req-1.5.2)

**Original section and label: 002 / User / 2.1.1**

- **New identifier or location:** [REQ-2.1.1](002-detailed-requirements.md#req-2.1.1)

**Original section and label: 002 / User / 2.1.2**

- **New identifier or location:** [REQ-2.1.2](002-detailed-requirements.md#req-2.1.2)

**Original section and label: 002 / User / 2.2.1**

- **New identifier or location:** [REQ-2.2.1](002-detailed-requirements.md#req-2.2.1)

**Original section and label: 002 / User / 2.2.2**

- **New identifier or location:** [REQ-2.2.2](002-detailed-requirements.md#req-2.2.2)

**Original section and label: 002 / User / 2.2.3**

- **New identifier or location:** [REQ-2.2.3](002-detailed-requirements.md#req-2.2.3)

**Original section and label: 002 / User / 2.2.4**

- **New identifier or location:** [REQ-2.2.4](002-detailed-requirements.md#req-2.2.4)

**Original section and label: 002 / User / 2.2.5**

- **New identifier or location:** [REQ-2.2.5](002-detailed-requirements.md#req-2.2.5)

**Original section and label: 002 / User / 2.2.6; 001 / Users**

- **New identifier or location:** [REQ-2.2.6](002-detailed-requirements.md#req-2.2.6)

**Original section and label: 002 / User / 2.3.1**

- **New identifier or location:** [REQ-2.3.1](002-detailed-requirements.md#req-2.3.1)

**Original section and label: 002 / User / 2.3.2**

- **New identifier or location:** [REQ-2.3.2](002-detailed-requirements.md#req-2.3.2)

**Original section and label: 002 / User / 2.4 unnumbered item**

- **New identifier or location:** [REQ-2.4.1](002-detailed-requirements.md#req-2.4.1)

**Original section and label: 001 / Users; Organisations; Sites, gateway, assets, sensors
and assets**

- **New identifier or location:** [REQ-2.5.1](002-detailed-requirements.md#req-2.5.1)

**Original section and label: 002 / Verification code / 3.1.1**

- **New identifier or location:** [REQ-3.1.1](002-detailed-requirements.md#req-3.1.1)

**Original section and label: 002 / Verification code / 3.2, incorrectly numbered 3.1.2**

- **New identifier or location:** [REQ-3.2.1](002-detailed-requirements.md#req-3.2.1)

**Original section and label: 002 / Verification code / 3.3; 003 / CAP 3.3**

- **New identifier or location:** [REQ-3.3.1](002-detailed-requirements.md#req-3.3.1)

**Original section and label: 001 / Organisations; Sites, gateway, assets, sensors and
assets; Assets**

- **New identifier or location:** [REQ-5.1.1](002-detailed-requirements.md#req-5.1.1)

**Original section and label: 001 / Sites, gateway, assets, sensors and assets**

- **New identifier or location:** [REQ-5.1.2](002-detailed-requirements.md#req-5.1.2)

**Original section and label: 001 / Overview; Organisations; Gateway**

- **New identifier or location:** [REQ-5.2.1](002-detailed-requirements.md#req-5.2.1)

**Original section and label: 001 / Gateway**

- **New identifier or location:** [REQ-5.2.2](002-detailed-requirements.md#req-5.2.2)

**Original section and label: 001 / Gateway**

- **New identifier or location:** [REQ-5.2.3](002-detailed-requirements.md#req-5.2.3)

**Original section and label: 001 / Sensors and actuators**

- **New identifier or location:** [REQ-5.3.1](002-detailed-requirements.md#req-5.3.1)

**Original section and label: 001 / Thresholds and actions**

- **New identifier or location:** [REQ-5.4.1](002-detailed-requirements.md#req-5.4.1)

**Original section and label: 001 / Thresholds and actions**

- **New identifier or location:** [REQ-5.4.2](002-detailed-requirements.md#req-5.4.2)

**Original section and label: 001 / Security, first item**

- **New identifier or location:** [NFR-1.1](002-detailed-requirements.md#nfr-1.1)

**Original section and label: 001 / Security, second item**

- **New identifier or location:** [NFR-1.2](002-detailed-requirements.md#nfr-1.2)

**Original section and label: 001 / Security, third item**

- **New identifier or location:** [NFR-1.3](002-detailed-requirements.md#nfr-1.3)

**Original section and label: 001 / Accessibility**

- **New identifier or location:** [NFR-2.1](002-detailed-requirements.md#nfr-2.1)

**Original section and label: 001 / Reliability, first item**

- **New identifier or location:** [NFR-3.1](002-detailed-requirements.md#nfr-3.1)

**Original section and label: 001 / Reliability, second item**

- **New identifier or location:** [NFR-3.2](002-detailed-requirements.md#nfr-3.2)

**Original section and label: 001 / Usability**

- **New identifier or location:** [NFR-4.1](002-detailed-requirements.md#nfr-4.1)

**Original section and label: 002 / 4 Subscription management (empty)**

- **New identifier or location:** [OPEN-002-4](002-detailed-requirements.md#open-002-4)

**Original section and label: 002 / Gateway (empty)**

- **New identifier or location:** [OPEN-002-5](002-detailed-requirements.md#open-002-5)
