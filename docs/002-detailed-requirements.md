# Detailed requirements

## 1 Functional requirements

These records preserve the obligations in the original detailed requirements and
elaborate goals already stated in 001. Acceptance statements describe observable
checks without selecting unspecified policies. Source labels identify the original
document and section; they are historical references, not current heading numbers.

### 1.1 Organisation management

Actor and trigger: Users creating organisations; superusers and authorized users
managing existing organisations. Each record's operation specifies the trigger.
Permission details beyond the source remain open.

<a id="req-1.1.1"></a>

**REQ-1.1.1**

- **Required outcome and constraints:** Create an organisation with a unique name.
- **Acceptance criterion:** A creation request with a conflicting name is rejected; a
  distinct name can be used. Name equivalence rules are
  [OPEN-002-1](002-detailed-requirements.md#open-002-1).
- **Source:** 002 / Organisation / 1.1 create; 1.1.2 unique name

<a id="req-1.1.2"></a>

**REQ-1.1.2**

- **Required outcome and constraints:** Make the creating user the organisation superuser
  by default.
- **Acceptance criterion:** After creation, the creator is assigned the superuser role.
- **Source:** 002 / Organisation / 1.1.3

<a id="req-1.1.3"></a>

**REQ-1.1.3**

- **Required outcome and constraints:** An organisation must have one superuser.
- **Acceptance criterion:** An organisation has exactly one superuser; transfer and
  exceptional cases require [OPEN-002-1](002-detailed-requirements.md#open-002-1).
- **Source:** 002 / Organisation / 1.1.4

<a id="req-1.1.4"></a>

**REQ-1.1.4**

- **Required outcome and constraints:** Check the user’s permission and usage tier before
  allowing multiple organisations.
- **Acceptance criterion:** A request to create another organisation is evaluated against
  the applicable entitlement; tier limits require
  [OPEN-002-1](002-detailed-requirements.md#open-002-1).
- **Source:** 002 / Organisation / 1.1.5; 001 / Organisations

<a id="req-1.2.1"></a>

**REQ-1.2.1**

- **Required outcome and constraints:** Allow additional organisation users up to the
  subscription-tier maximum.
- **Acceptance criterion:** Adding a member respects the applicable maximum; tier values
  require [OPEN-002-1](002-detailed-requirements.md#open-002-1).
- **Source:** 002 / Organisation / 1.2.1

<a id="req-1.2.2"></a>

**REQ-1.2.2**

- **Required outcome and constraints:** Allow the superuser to rename the organisation to
  another unique name.
- **Acceptance criterion:** An authorized rename succeeds only if the new name meets
  uniqueness rules.
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

- **Required outcome and constraints:** Determine and enforce maximum organisation users.
- **Acceptance criterion:** Membership changes enforce the maximum determined for that
  organisation.
- **Source:** 002 / Organisation / 1.4.1

<a id="req-1.4.2"></a>

**REQ-1.4.2**

- **Required outcome and constraints:** Determine and apply organisation service
  continuity limits.
- **Acceptance criterion:** The continuity decision and its effects require
  [OPEN-002-1](002-detailed-requirements.md#open-002-1) before acceptance can be
  completed.
- **Source:** 002 / Organisation / 1.4.2

<a id="req-1.5.1"></a>

**REQ-1.5.1**

- **Required outcome and constraints:** Allow the superuser to enable or disable the
  organisation.
- **Acceptance criterion:** An authorized request changes availability; state and access
  effects require [OPEN-002-1](002-detailed-requirements.md#open-002-1).
- **Source:** 002 / Organisation / 1.5.1

<a id="req-1.5.2"></a>

**REQ-1.5.2**

- **Required outcome and constraints:** Allow the superuser to delete the organisation.
- **Acceptance criterion:** An authorized deletion request follows a defined deletion and
  retention policy; policy requires [OPEN-002-1](002-detailed-requirements.md#open-002-1).
- **Source:** 002 / Organisation / 1.5.2

### 1.2 User management

Actor and trigger: A person registers; a user requests changes to their account, contacts,
profile, or preferences. Authentication and reset preconditions require
[OPEN-002-2](002-detailed-requirements.md#open-002-2).

<a id="req-2.1.1"></a>

**REQ-2.1.1**

- **Required outcome and constraints:** Allow registration using a phone number.
- **Acceptance criterion:** A registration request can create an account identified by a
  phone number; normalization and duplicate handling require
  [OPEN-002-2](002-detailed-requirements.md#open-002-2).
- **Source:** 002 / User / 2.1.1

<a id="req-2.1.2"></a>

**REQ-2.1.2**

- **Required outcome and constraints:** Verify the phone number using a code sent through
  WhatsApp.
- **Acceptance criterion:** The workflow sends a verification code through WhatsApp and
  checks the submitted code; timing and retry rules require
  [OPEN-002-3](002-detailed-requirements.md#open-002-3).
- **Source:** 002 / User / 2.1.2

<a id="req-2.2.1"></a>

**REQ-2.2.1**

- **Required outcome and constraints:** Allow a user to change their password.
- **Acceptance criterion:** A permitted change replaces the credential; credential and
  authorization policy require [OPEN-002-2](002-detailed-requirements.md#open-002-2).
- **Source:** 002 / User / 2.2.1

<a id="req-2.2.2"></a>

**REQ-2.2.2**

- **Required outcome and constraints:** Allow a user to verify their phone number.
- **Acceptance criterion:** A valid verification result updates phone verification status;
  account/contact coupling requires [OPEN-002-2](002-detailed-requirements.md#open-002-2).
- **Source:** 002 / User / 2.2.2

<a id="req-2.2.3"></a>

**REQ-2.2.3**

- **Required outcome and constraints:** Allow a user to reset their password.
- **Acceptance criterion:** The reset flow establishes the required proof and changes the
  password; proof and expiry rules remain
  [OPEN-002-2](002-detailed-requirements.md#open-002-2).
- **Source:** 002 / User / 2.2.3

<a id="req-2.2.4"></a>

**REQ-2.2.4**

- **Required outcome and constraints:** Allow a user to request a new verification code.
- **Acceptance criterion:** A new-code request is supported; throttling and treatment of
  earlier codes require [OPEN-002-3](002-detailed-requirements.md#open-002-3).
- **Source:** 002 / User / 2.2.4

<a id="req-2.2.5"></a>

**REQ-2.2.5**

- **Required outcome and constraints:** Allow a user to set and verify an email address.
- **Acceptance criterion:** The user can set an email and establish its verification
  status; proof mechanism requires [OPEN-002-2](002-detailed-requirements.md#open-002-2).
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
- **Acceptance criterion:** A supported channel selection is stored and used for dispatch;
  available channels and fallback require
  [OPEN-002-2](002-detailed-requirements.md#open-002-2).
- **Source:** 002 / User / 2.3.2

<a id="req-2.4.1"></a>

**REQ-2.4.1**

- **Required outcome and constraints:** Allow a user to deactivate and delete their
  account.
- **Acceptance criterion:** Both operations are available under defined rules;
  reversibility and retention require
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
  numeric codes for accounts, contacts, and password resets.
- **Acceptance criterion:** Generated codes contain five numeric digits and use a
  cryptographically secure generator; review generation and format, including leading
  zeros.
- **Source:** 002 / Verification code / 3.1.1

<a id="req-3.2.1"></a>

**REQ-3.2.1**

- **Required outcome and constraints:** Check whether a submitted code matches the stored
  code and is within its validity period.
- **Acceptance criterion:** Matching, in-period codes pass the code check; mismatched or
  expired codes fail. Duration and boundary semantics require
  [OPEN-002-3](002-detailed-requirements.md#open-002-3).
- **Source:** 002 / Verification code / 3.2, incorrectly numbered 3.1.2

<a id="req-3.3.1"></a>

**REQ-3.3.1**

- **Required outcome and constraints:** Clear codes that have been used or are no longer
  valid.
- **Acceptance criterion:** Used and invalid codes are removed according to an agreed
  cleanup policy; timing and retention require
  [OPEN-002-3](002-detailed-requirements.md#open-002-3).
- **Source:** 002 / Verification code / 3.3; 003 / CAP 3.3

### 1.4 Subscription management

Actor and trigger: Subscription policy supplies entitlements to organisation operations.

The source contains a heading without detailed requirements. Organisation tier rules
remain in [REQ-1.1.4](002-detailed-requirements.md#req-1.1.4),
[REQ-1.2.1](002-detailed-requirements.md#req-1.2.1),
[REQ-1.4.1](002-detailed-requirements.md#req-1.4.1). Additional subscription behavior is
[OPEN-002-4](002-detailed-requirements.md#open-002-4).

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

- **Decision needed and affected work:** Define organisation name equivalence, superuser
  transfer, tier limits, continuity, membership permissions, and deletion/disable effects.
  These block complete organisation acceptance and lifecycle design.

<a id="open-002-2"></a>

**OPEN-002-2**

- **Decision needed and affected work:** Define phone/email normalization and
  verification, duplicate-account rules, passwords and authentication, role scope,
  notification channels/fallback, account/contact state coupling, and deletion retention.
  Do not derive these policies solely from current columns.

<a id="open-002-3"></a>

**OPEN-002-3**

- **Decision needed and affected work:** Define code lifetime, expiry boundary, proof
  scope, replay/use semantics, resend invalidation, throttling, and cleanup timing.
  Five-digit secure generation is already required; these policies are not specified.

<a id="open-002-4"></a>

**OPEN-002-4**

- **Decision needed and affected work:** Expand subscription requirements beyond the
  existing organisation entitlements; the original Subscription management section was
  empty.

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
