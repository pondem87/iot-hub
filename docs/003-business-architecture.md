# Business architecture

## 1 Purpose and sources

This architecture translates [detailed requirements](002-detailed-requirements.md)
into business abilities, information, and stakeholder value using the
[documentation methodology](000-docs-guide.md#31-capabilities-and-information).
It preserves the original maps and their intent while normalizing identifiers.
Definitions inferred from existing capability names are derived design, not new
permissions or lifecycle policy. Capabilities previously named only in value
stages are now explicitly registered so cross-mappings have identifiable targets.

The maps describe the business rather than implementation completeness. Missing
coverage and unresolved classifications are listed in section 6.

## 2 Capability map

Each dotted CAP identifier belongs to the parent obtained by removing its final
segment. Top-level records have no parent. Sources cite requirements, value streams,
or open decisions; section 7 preserves original labels for all records.

### 2.1 User Management

<a id="cap-1"></a>

**CAP-1**

- **Name:** User Management
- **Business ability or outcome:** Identify people using the service and maintain their
  accounts, attributes, contacts, and associations.
- **Source:** [REQ-2.1.1](002-detailed-requirements.md#req-2.1.1),
  [REQ-2.2.6](002-detailed-requirements.md#req-2.2.6),
  [REQ-2.4.1](002-detailed-requirements.md#req-2.4.1)

<a id="cap-1.1"></a>

**CAP-1.1**

- **Name:** User Definition
- **Business ability or outcome:** Identify a user by an international phone number,
  create and retrieve their record, and reject duplicate accounts.
- **Source:** [REQ-2.1.1](002-detailed-requirements.md#req-2.1.1)

<a id="cap-1.2"></a>

**CAP-1.2**

- **Name:** User Profile Management
- **Business ability or outcome:** Obtain, maintain, and set attributes describing a user.
- **Source:** [REQ-2.2.6](002-detailed-requirements.md#req-2.2.6)

<a id="cap-1.3"></a>

**CAP-1.3**

- **Name:** User Preference Management
- **Business ability or outcome:** Obtain, maintain, and enforce a user’s expressed needs,
  with WhatsApp as the default communication channel.
- **Source:** [REQ-2.3.1](002-detailed-requirements.md#req-2.3.1),
  [REQ-2.3.2](002-detailed-requirements.md#req-2.3.2)

<a id="cap-1.4"></a>

**CAP-1.4**

- **Name:** User State Management
- **Business ability or outcome:** Determine and update a user’s lifecycle condition.
- **Source:** [REQ-2.2.2](002-detailed-requirements.md#req-2.2.2),
  [REQ-2.4.1](002-detailed-requirements.md#req-2.4.1)

<a id="cap-1.5"></a>

**CAP-1.5**

- **Name:** User Account Management
- **Business ability or outcome:** Activate, disable, and delete an account under the
  applicable business rules, retaining deleted accounts for 30 days before cleanup.
- **Source:** [REQ-2.4.1](002-detailed-requirements.md#req-2.4.1),
  [OPEN-003-2](003-business-architecture.md#open-003-2)

<a id="cap-1.6"></a>

**CAP-1.6**

- **Name:** User Matching
- **Business ability or outcome:** Associate a user with other business objects.
- **Source:** [OPEN-003-4](003-business-architecture.md#open-003-4)

<a id="cap-1.6.1"></a>

**CAP-1.6.1**

- **Name:** User/Subscription Matching
- **Business ability or outcome:** Associate a user with the entitlement arrangement that
  applies to them.
- **Entitlement:** Subscription sets the user's maximum superuser-account count,
  defaulting to one organisation where they hold the superuser role, under
  [REQ-1.1.4](002-detailed-requirements.md#req-1.1.4).
- **Source:** [REQ-1.1.4](002-detailed-requirements.md#req-1.1.4),
  [OPEN-002-4](002-detailed-requirements.md#open-002-4)

<a id="cap-1.7"></a>

**CAP-1.7**

- **Name:** User Contact Management
- **Business ability or outcome:** Add, retrieve, verify, and delete ways to reach a user.
- **Source:** [REQ-2.2.2](002-detailed-requirements.md#req-2.2.2),
  [REQ-2.2.5](002-detailed-requirements.md#req-2.2.5)

<a id="cap-1.7.1"></a>

**CAP-1.7.1**

- **Name:** User Contact Definition
- **Business ability or outcome:** Identify, create, and retrieve a way to reach a user.
- **Validation rules:** Phone numbers use international form such as `+26774178111`;
  email addresses are lowercase, following
  [REQ-2.1.1](002-detailed-requirements.md#req-2.1.1) and
  [REQ-2.2.5](002-detailed-requirements.md#req-2.2.5).
- **Source:** [REQ-2.2.5](002-detailed-requirements.md#req-2.2.5)

<a id="cap-1.7.2"></a>

**CAP-1.7.2**

- **Name:** User Contact Type Management
- **Business ability or outcome:** Determine and set the kind of a user’s contact method.
- **Source:** [REQ-2.2.2](002-detailed-requirements.md#req-2.2.2),
  [REQ-2.2.5](002-detailed-requirements.md#req-2.2.5)

<a id="cap-1.7.3"></a>

**CAP-1.7.3**

- **Name:** User Contact State Management
- **Business ability or outcome:** Determine and change the lifecycle condition of a
  contact method.
- **Source:** [REQ-2.2.2](002-detailed-requirements.md#req-2.2.2),
  [REQ-2.2.5](002-detailed-requirements.md#req-2.2.5)

**CAP-1.8**

- **Name:** User Access Management
- **Business ability or outcome:** Determine who can access and manipulate user information
- **Source:** [REQ-1.2](002-detailed-requirements.md#req-1.2)

**CAP-1.8**

- **Name:** User Access Constraint Definition
- **Business ability or outcome:** Determine who can access and manipulate user information
- **Source:** [REQ-1.2](002-detailed-requirements.md#req-1.2)

**CAP-1.8.1**

- **Name:** User Access Constraints Determination
- **Business ability or outcome:** Determine who can access and manipulate user information
- **Source:** [REQ-1.2](002-detailed-requirements.md#req-1.2)

**CAP-1.8**

- **Name:** User Access Contraints Enforcement
- **Business ability or outcome:** Determine who can access and manipulate user information
- **Source:** [REQ-1.2](002-detailed-requirements.md#req-1.2)

### 2.2 Organisation Management

<a id="cap-2"></a>

**CAP-2**

- **Name:** Organisation Management
- **Business ability or outcome:** Identify and maintain an organisation, its membership,
  access, limits, and associations.
- **Source:** [REQ-1.1.1](002-detailed-requirements.md#req-1.1.1),
  [REQ-1.2.3](002-detailed-requirements.md#req-1.2.3)

<a id="cap-2.1"></a>

**CAP-2.1**

- **Name:** Organisation Definition
- **Business ability or outcome:** Identify, create, rename, and retrieve an organisation,
  enforcing name uniqueness while ignoring case and trimming and collapsing whitespace.
- **Source:** [REQ-1.1.1](002-detailed-requirements.md#req-1.1.1)

<a id="cap-2.2"></a>

**CAP-2.2**

- **Name:** Organisation Superuser Management
- **Business ability or outcome:** Set, retrieve, and transfer the organisation's sole
  superuser role following verification by the current superuser; immediately remove
  the former superuser's status on irreversible completion.
- **Source:** [REQ-1.1.2](002-detailed-requirements.md#req-1.1.2),
  [REQ-1.1.3](002-detailed-requirements.md#req-1.1.3)

<a id="cap-2.3"></a>

**CAP-2.3**

- **Name:** Organisation Membership Management
- **Business ability or outcome:** Add, remove, and list people belonging to an
  organisation.
- **Source:** [REQ-1.2.3](002-detailed-requirements.md#req-1.2.3)

<a id="cap-2.3.1"></a>

**CAP-2.3.1**

- **Name:** Organisation Member List Management
- **Business ability or outcome:** Maintain and list the people belonging to an
  organisation.
- **Source:** [REQ-1.2.3](002-detailed-requirements.md#req-1.2.3)

<a id="cap-2.3.2"></a>

**CAP-2.3.2**

- **Name:** Organisation Member Access Management
- **Business ability or outcome:** Maintain the access constraints applying to an
  organisation member.
- **Source:** [REQ-1.3.1](002-detailed-requirements.md#req-1.3.1)

<a id="cap-2.4"></a>

**CAP-2.4**

- **Name:** Organisation Access Management
- **Business ability or outcome:** Determine and control access to organisation resources.
- **Source:** [REQ-1.3.1](002-detailed-requirements.md#req-1.3.1),
  [REQ-1.3.2](002-detailed-requirements.md#req-1.3.2)

<a id="cap-2.4.1"></a>

**CAP-2.4.1**

- **Name:** Organisation Access Constraint Definition
- **Business ability or outcome:** Create, retrieve, and delete a policy restricting
  organisation resource access.
- **Source:** [REQ-1.3.1](002-detailed-requirements.md#req-1.3.1)

<a id="cap-2.4.2"></a>

**CAP-2.4.2**

- **Name:** Organisation Access Constraint Interpretation
- **Business ability or outcome:** Interpret the limits imposed by an organisation access
  policy.
- **Source:** [REQ-1.3.1](002-detailed-requirements.md#req-1.3.1)

<a id="cap-2.4.3"></a>

**CAP-2.4.3**

- **Name:** Organisation Access Constraint Enforcement
- **Business ability or outcome:** Apply organisation access restrictions to an attempted
  operation.
- **Source:** [REQ-1.3.1](002-detailed-requirements.md#req-1.3.1),
  [REQ-1.3.2](002-detailed-requirements.md#req-1.3.2)

<a id="cap-2.5"></a>

**CAP-2.5**

- **Name:** Organisation Limit Management
- **Business ability or outcome:** Apply the subscription maximum for organisation
  users and enforce service continuity when a user's subscription tier expires.
- **Source:** [REQ-1.4.1](002-detailed-requirements.md#req-1.4.1),
  [REQ-1.4.2](002-detailed-requirements.md#req-1.4.2)

<a id="cap-2.5.1"></a>

**CAP-2.5.1**

- **Name:** Organisation Limit Definition
- **Business ability or outcome:** Obtain and maintain the subscription-defined maximum
  number of users for an organisation, defaulting to three including the superuser.
- **Source:** [REQ-1.4.1](002-detailed-requirements.md#req-1.4.1)

<a id="cap-2.5.2"></a>

**CAP-2.5.2**

- **Name:** Organisation Limit Interpretation
- **Business ability or outcome:** Interpret the subscription-defined user maximum and
  the organisation allowance applicable to a superuser after tier expiry.
- **Source:** [REQ-1.4.1](002-detailed-requirements.md#req-1.4.1),
  [REQ-1.4.2](002-detailed-requirements.md#req-1.4.2)

<a id="cap-2.5.3"></a>

**CAP-2.5.3**

- **Name:** Organisation Limit Enforcement
- **Business ability or outcome:** Enforce the subscription maximum on membership
  additions; on a superuser's tier expiry, randomly disable excess organisations
  until one remains enabled under the default allowance, preserving data.
- **Source:** [REQ-1.4.1](002-detailed-requirements.md#req-1.4.1),
  [REQ-1.4.2](002-detailed-requirements.md#req-1.4.2)

<a id="cap-2.6"></a>

**CAP-2.6**

- **Name:** Organisation Matching
- **Business ability or outcome:** Associate an organisation with other business objects.
- **Source:** [REQ-1.1.2](002-detailed-requirements.md#req-1.1.2),
  [REQ-1.2.1](002-detailed-requirements.md#req-1.2.1)

<a id="cap-2.6.1"></a>

**CAP-2.6.1**

- **Name:** Organisation/User Matching
- **Business ability or outcome:** Associate an organisation with a person belonging to
  it.
- **Source:** [REQ-1.2.3](002-detailed-requirements.md#req-1.2.3)

<a id="cap-2.6.2"></a>

**CAP-2.6.2**

- **Name:** Organisation/Subscription Matching
- **Business ability or outcome:** Associate an organisation with its applicable
  entitlement arrangement.
- **Source:** [REQ-1.2.1](002-detailed-requirements.md#req-1.2.1),
  [OPEN-002-4](002-detailed-requirements.md#open-002-4)

<a id="cap-2.7"></a>

**CAP-2.7**

- **Name:** Organisation Lifecycle Management
- **Business ability or outcome:** Disable all organisation activity until superuser
  reactivation while retaining data indefinitely; permit only reactivation while
  deleted, before cleanup after 30 days of the organisation and all its owned assets.
  User accounts survive organisation cleanup because organisations do not own users.
- **Source:** [REQ-1.5.1](002-detailed-requirements.md#req-1.5.1),
  [REQ-1.5.2](002-detailed-requirements.md#req-1.5.2)

### 2.3 Verification Code Management

<a id="cap-3"></a>

**CAP-3**

- **Name:** Verification Code Management
- **Business ability or outcome:** Generate, validate, clear, and associate proofs used
  for account and contact operations.
- **Source:** [REQ-3.1.1](002-detailed-requirements.md#req-3.1.1),
  [REQ-3.2.1](002-detailed-requirements.md#req-3.2.1),
  [REQ-3.3.1](002-detailed-requirements.md#req-3.3.1)

<a id="cap-3.1"></a>

**CAP-3.1**

- **Name:** Verification Code Definition
- **Business ability or outcome:** Generate, store, and retrieve a code used to verify a
  request, paired with a purpose supplied by the requesting service; replace the
  prior code for that verification and purpose on resend, subject to
  [CAP-3.5](003-business-architecture.md#cap-3.5).
- **Replacement guarantee:** A successful resend makes the previous code unusable
  and starts a fresh five-minute lifetime. If generation is blocked, no replacement
  is created. A failed replacement must not report success.
- **Source:** [REQ-2.2.4](002-detailed-requirements.md#req-2.2.4),
  [REQ-3.1.2](002-detailed-requirements.md#req-3.1.2), [REQ-3.1.1](002-detailed-requirements.md#req-3.1.1)

<a id="cap-3.2"></a>

**CAP-3.2**

- **Name:** Verification Code Validation
- **Business ability or outcome:** Determine whether a submitted code matches and remains
  within its five-minute lifetime, has not been invalidated, and has not already
  been consumed; require the matching service-supplied purpose and accept each code
  only once.
- **Source:** [REQ-3.2.1](002-detailed-requirements.md#req-3.2.1)

<a id="cap-3.3"></a>

**CAP-3.3**

- **Name:** Verification Code Cleanup
- **Business ability or outcome:** Clear proofs that have been used or are no longer
  valid, and discard every code within 24 hours of generation while preserving
  still-required block history.
- **Source:** [REQ-3.3.1](002-detailed-requirements.md#req-3.3.1)

<a id="cap-3.4"></a>

**CAP-3.4**

- **Name:** Verification Code Matching
- **Business ability or outcome:** Associate a verification code with the business object
  to which it applies and the purpose supplied by the requesting service.
- **Source:** [REQ-3.1.1](002-detailed-requirements.md#req-3.1.1)

<a id="cap-3.4.1"></a>

**CAP-3.4.1**

- **Name:** Verification Code/User Matching
- **Business ability or outcome:** Associate a verification code with its user.
- **Source:** [REQ-2.1.2](002-detailed-requirements.md#req-2.1.2)

<a id="cap-3.4.2"></a>

**CAP-3.4.2**

- **Name:** Verification Code/Contact Matching
- **Business ability or outcome:** Associate a verification code with its contact method.
- **Source:** [REQ-2.2.2](002-detailed-requirements.md#req-2.2.2),
  [REQ-2.2.5](002-detailed-requirements.md#req-2.2.5)

<a id="cap-3.5"></a>

**CAP-3.5**

- **Name:** Verification Code Generation Control
- **Business ability or outcome:** Count generation and resends per service-supplied
  purpose, enforce timed blocks, and preserve block history independently of codes.
- **Rules:** The fifth code generated within ten minutes triggers a one-hour block;
  a second block for the same purpose within 24 hours of the first block's start
  lasts 24 hours. Blocked requests generate no codes and do not count as generation.
  Other purposes are unaffected. Window details and subsequent-block policy remain
  [OPEN-002-3](002-detailed-requirements.md#open-002-3).
- **Source:** [REQ-3.1.2](002-detailed-requirements.md#req-3.1.2),
  [REQ-3.3.1](002-detailed-requirements.md#req-3.3.1)

### 2.4 Organisation Invitation Management

<a id="cap-4"></a>

**CAP-4**

- **Name:** Organisation Invitation Management
- **Business ability or outcome:** Issue, track, and revoke mechanisms for users to join
  an organisation.
- **Source:** [REQ-1.2.3](002-detailed-requirements.md#req-1.2.3)

<a id="cap-4.1"></a>

**CAP-4.1**

- **Name:** Invitation Definition
- **Business ability or outcome:** Create and retrieve a mechanism for joining an
  organisation.
- **Source:** [REQ-1.2.3](002-detailed-requirements.md#req-1.2.3)

<a id="cap-4.2"></a>

**CAP-4.2**

- **Name:** Invitation Revocation
- **Business ability or outcome:** Withdraw an issued invitation for a specified user.
- **Source:** [REQ-1.2.3](002-detailed-requirements.md#req-1.2.3)

<a id="cap-4.3"></a>

**CAP-4.3**

- **Name:** Invitation Access Management
- **Business ability or outcome:** Control access to invitations.
- **Source:** [REQ-1.3.2](002-detailed-requirements.md#req-1.3.2)

<a id="cap-4.3.1"></a>

**CAP-4.3.1**

- **Name:** Invitation Access Constraint Definition
- **Business ability or outcome:** Create, retrieve, and delete an invitation access
  restriction.
- **Source:** [REQ-1.3.2](002-detailed-requirements.md#req-1.3.2)

<a id="cap-4.3.2"></a>

**CAP-4.3.2**

- **Name:** Invitation Access Constraint Interpretation
- **Business ability or outcome:** Interpret an invitation access restriction.
- **Source:** [REQ-1.3.2](002-detailed-requirements.md#req-1.3.2)

<a id="cap-4.3.3"></a>

**CAP-4.3.3**

- **Name:** Invitation Access Constraint Enforcement
- **Business ability or outcome:** Apply an invitation access restriction.
- **Source:** [REQ-1.3.2](002-detailed-requirements.md#req-1.3.2)

<a id="cap-4.4"></a>

**CAP-4.4**

- **Name:** Invitation Matching
- **Business ability or outcome:** Associate an invitation with the business objects
  involved in joining.
- **Source:** [REQ-1.2.3](002-detailed-requirements.md#req-1.2.3)

<a id="cap-4.4.1"></a>

**CAP-4.4.1**

- **Name:** Invitation/Organisation Matching
- **Business ability or outcome:** Associate an invitation with the organisation to be
  joined.
- **Source:** [REQ-1.2.3](002-detailed-requirements.md#req-1.2.3)

<a id="cap-4.4.2"></a>

**CAP-4.4.2**

- **Name:** Invitation/User Matching
- **Business ability or outcome:** Associate an invitation with the intended user.
- **Source:** [REQ-1.2.3](002-detailed-requirements.md#req-1.2.3)

### 2.5 Event Management

<a id="cap-5"></a>

**CAP-5**

- **Name:** Event Management
- **Business ability or outcome:** Create, capture, interpret, distribute, and route
  occurrences and register their publishers and subscribers.
- **Source:** [OPEN-003-4](003-business-architecture.md#open-003-4)

<a id="cap-5.1"></a>

**CAP-5.1**

- **Name:** Event Definition
- **Business ability or outcome:** Create a representation of a business occurrence.
- **Source:** [VS-1](003-business-architecture.md#vs-1)

<a id="cap-5.2"></a>

**CAP-5.2**

- **Name:** Event Capture
- **Business ability or outcome:** Receive an occurrence and forward it to subscribers.
- **Source:** [VS-1](003-business-architecture.md#vs-1)

<a id="cap-5.3"></a>

**CAP-5.3**

- **Name:** Event Distribution
- **Business ability or outcome:** Distribute occurrences from publishers to subscribers.
- **Source:** [VS-1](003-business-architecture.md#vs-1)

<a id="cap-5.4"></a>

**CAP-5.4**

- **Name:** Event Publisher Registration
- **Business ability or outcome:** Register an originator of business occurrences.
- **Source:** [OPEN-003-4](003-business-architecture.md#open-003-4)

<a id="cap-5.5"></a>

**CAP-5.5**

- **Name:** Event Subscriber Registration
- **Business ability or outcome:** Register a recipient of business occurrences.
- **Source:** [OPEN-003-4](003-business-architecture.md#open-003-4)

### 2.6 Notification Management

<a id="cap-6"></a>

**CAP-6**

- **Name:** Notification Management
- **Business ability or outcome:** Determine what to tell a user and through which
  channel, subject to preferences and permissions.
- **Source:** [REQ-2.3.1](002-detailed-requirements.md#req-2.3.1),
  [REQ-2.3.2](002-detailed-requirements.md#req-2.3.2)

<a id="cap-6.1"></a>

**CAP-6.1**

- **Name:** Notification Definition
- **Business ability or outcome:** Create a notice intended for a user.
- **Source:** [VS-2](003-business-architecture.md#vs-2)

<a id="cap-6.2"></a>

**CAP-6.2**

- **Name:** Notification Dispatch Determination
- **Business ability or outcome:** Decide whether to send a notice under the applicable
  preferences and permissions.
- **Source:** [REQ-2.3.1](002-detailed-requirements.md#req-2.3.1)

<a id="cap-6.3"></a>

**CAP-6.3**

- **Name:** Notification Channel Determination
- **Business ability or outcome:** Choose the communication medium for a notice.
- **Source:** [REQ-2.3.2](002-detailed-requirements.md#req-2.3.2)

<a id="cap-6.4"></a>

**CAP-6.4**

- **Name:** Notification Content Construction
- **Business ability or outcome:** Prepare the information to communicate in a notice.
- **Source:** [VS-2](003-business-architecture.md#vs-2)

<a id="cap-6.5"></a>

**CAP-6.5**

- **Name:** Notification Type Determination
- **Business ability or outcome:** Determine the kind of notice being requested.
- **Source:** [VS-2](003-business-architecture.md#vs-2)

### 2.7 Message Management

<a id="cap-7"></a>

**CAP-7**

- **Name:** Message Management
- **Business ability or outcome:** Create, structure, route, and interpret communications
  to and from users.
- **Source:** [VS-2](003-business-architecture.md#vs-2)

<a id="cap-7.1"></a>

**CAP-7.1**

- **Name:** Message Definition
- **Business ability or outcome:** Create a communication for a recipient.
- **Source:** [VS-2](003-business-architecture.md#vs-2)

<a id="cap-7.2"></a>

**CAP-7.2**

- **Name:** Message Capture
- **Business ability or outcome:** Receive a communication from a sender.
- **Source:** [VS-2](003-business-architecture.md#vs-2)

<a id="cap-7.3"></a>

**CAP-7.3**

- **Name:** Message Structuring
- **Business ability or outcome:** Determine the form and type of a communication.
- **Source:** [VS-2](003-business-architecture.md#vs-2)

<a id="cap-7.4"></a>

**CAP-7.4**

- **Name:** Message Dispatch
- **Business ability or outcome:** Send a prepared communication.
- **Source:** [VS-2](003-business-architecture.md#vs-2)

<a id="cap-7.5"></a>

**CAP-7.5**

- **Name:** Message Channel Determination
- **Business ability or outcome:** Determine the medium through which to send a
  communication.
- **Source:** [VS-2](003-business-architecture.md#vs-2)

<a id="cap-7.6"></a>

**CAP-7.6**

- **Name:** Message Status Tracking
- **Business ability or outcome:** Track the progress of a communication toward its
  delivery outcome.
- **Source:** [VS-2](003-business-architecture.md#vs-2)

### 2.8 Channel Management

<a id="cap-8"></a>

**CAP-8**

- **Name:** Channel Management
- **Business ability or outcome:** Send and receive communications through a chosen
  medium.
- **Source:** [VS-2](003-business-architecture.md#vs-2)

<a id="cap-8.1"></a>

**CAP-8.1**

- **Name:** Channel Identification
- **Business ability or outcome:** Identify the medium needed for communication.
- **Source:** [VS-2](003-business-architecture.md#vs-2)

<a id="cap-8.2"></a>

**CAP-8.2**

- **Name:** Channel Configuration
- **Business ability or outcome:** Establish the settings needed to communicate through a
  medium.
- **Source:** [VS-2](003-business-architecture.md#vs-2)

<a id="cap-8.3"></a>

**CAP-8.3**

- **Name:** Channel Dispatch
- **Business ability or outcome:** Convey a communication through its chosen medium.
- **Source:** [VS-2](003-business-architecture.md#vs-2)

### 2.9 WhatsApp Communication Management

<a id="cap-9"></a>

**CAP-9**

- **Name:** WhatsApp Communication Management
- **Business ability or outcome:** Convey messages to and from users through WhatsApp.
- **Source:** [REQ-2.1.2](002-detailed-requirements.md#req-2.1.2),
  [NFR-2.1](002-detailed-requirements.md#nfr-2.1)

<a id="cap-9.1"></a>

**CAP-9.1**

- **Name:** WhatsApp Communication Configuration
- **Business ability or outcome:** Establish the settings needed for WhatsApp
  communication.
- **Source:** [REQ-2.1.2](002-detailed-requirements.md#req-2.1.2)

<a id="cap-9.2"></a>

**CAP-9.2**

- **Name:** WhatsApp Message Dispatch
- **Business ability or outcome:** Send a communication through WhatsApp.
- **Source:** [REQ-2.1.2](002-detailed-requirements.md#req-2.1.2)

### 2.10 Audit Log Management

<a id="cap-10"></a>

**CAP-10**

- **Name:** Audit Log Management
- **Business ability or outcome:** Create, store, and retrieve records of relevant
  activity.
- **Source:** [OPEN-003-4](003-business-architecture.md#open-003-4)

### 2.11 Submission Management

<a id="cap-11"></a>

**CAP-11**

- **Name:** Submission Management
- **Business ability or outcome:** Capture and validate information submitted to request a
  business operation.
- **Source:** [REQ-2.1.1](002-detailed-requirements.md#req-2.1.1)

<a id="cap-11.1"></a>

**CAP-11.1**

- **Name:** Input Data Capture
- **Business ability or outcome:** Receive details supplied by a person.
- **Source:** [REQ-2.1.1](002-detailed-requirements.md#req-2.1.1)

<a id="cap-11.2"></a>

**CAP-11.2**

- **Name:** Form Submission
- **Business ability or outcome:** Accept a submitted set of details for processing.
- **Source:** [REQ-2.1.1](002-detailed-requirements.md#req-2.1.1)

<a id="cap-11.3"></a>

**CAP-11.3**

- **Name:** Submission Schema Determination
- **Business ability or outcome:** Determine the expected structure of a submission.
- **Source:** [REQ-2.1.1](002-detailed-requirements.md#req-2.1.1)

<a id="cap-11.4"></a>

**CAP-11.4**

- **Name:** Submission Validation
- **Business ability or outcome:** Check submission structure and phone, name, and
  password values against the applicable rules, including international phone form,
  lowercase email, and passwords of at least eight characters containing lowercase,
  uppercase, and digits.
- **Resolved rules:** [REQ-2.2.1](002-detailed-requirements.md#req-2.2.1),
  [REQ-2.2.5](002-detailed-requirements.md#req-2.2.5).
- **Source:** [REQ-2.1.1](002-detailed-requirements.md#req-2.1.1),
  [OPEN-002-2](002-detailed-requirements.md#open-002-2)

## 3 Information map

### 3.1 Concepts, types, and states

State lists and “None recorded” values preserve the original map. Their complete
definitions, permissible transitions, and the sufficiency of type lists require
[OPEN-003-2](003-business-architecture.md#open-003-2). The short definitions below are
business vocabulary,
not physical table definitions. Secondary-parent references denote existence
dependencies, not a decided database deletion policy.

<a id="info-1"></a>

**INFO-1**

- **Concept and definition:** User — a person using the service.
- **Category and parent:** Primary
- **Types:** superuser, staff, customer
- **States:** unverified, active, inactive, barred, deleted
- **Identity and retention:** Phone identity uses international form such as
  `+26774178111`; duplicate accounts fail registration. Deleted accounts are retained
  for 30 days before cleanup, under [REQ-2.1.1](002-detailed-requirements.md#req-2.1.1)
  and [REQ-2.4.1](002-detailed-requirements.md#req-2.4.1).
- **Organisation roles and limits:** Users exist independently of organisations and
  survive organisation cleanup. Subscription limits the number of organisations in
  which a user holds the superuser role; verified transfer removes that role from
  the former superuser immediately. See
  [REQ-1.1.3](002-detailed-requirements.md#req-1.1.3),
  [REQ-1.1.4](002-detailed-requirements.md#req-1.1.4), and
  [REQ-1.5.2](002-detailed-requirements.md#req-1.5.2).
- **Source:** [CAP-1.1](003-business-architecture.md#cap-1.1),
  [CAP-1.4](003-business-architecture.md#cap-1.4)

<a id="info-1.1"></a>

**INFO-1.1**

- **Concept and definition:** User Profile — characteristics describing a user.
- **Category and parent:** Secondary; [INFO-1](003-business-architecture.md#info-1)
- **Types:** None recorded
- **States:** None recorded
- **Source:** [CAP-1.2](003-business-architecture.md#cap-1.2)

<a id="info-1.2"></a>

**INFO-1.2**

- **Concept and definition:** User Preferences — parameters expressing a user’s needs.
- **Category and parent:** Secondary; [INFO-1](003-business-architecture.md#info-1)
- **Types:** None recorded
- **States:** None recorded
- **Default:** WhatsApp is the default communication channel under
  [REQ-2.3.2](002-detailed-requirements.md#req-2.3.2); channel choice and notification
  opt-in are distinct preferences.
- **Source:** [CAP-1.3](003-business-architecture.md#cap-1.3)

<a id="info-1.3"></a>

**INFO-1.3**

- **Concept and definition:** User Contact — an identifier for a user in a communication
  channel.
- **Category and parent:** Secondary; [INFO-1](003-business-architecture.md#info-1)
- **Types:** phone number, email address
- **States:** unverified, active, disabled
- **Representation:** International phone form, for example `+26774178111`, or a
  lowercase email address; see [REQ-2.1.1](002-detailed-requirements.md#req-2.1.1)
  and [REQ-2.2.5](002-detailed-requirements.md#req-2.2.5).
- **Source:** [CAP-1.7](003-business-architecture.md#cap-1.7)

<a id="info-1.4"></a>

**INFO-1.4**

- **Concept and definition:** User access constraints — parameters restricting access to a user's data
- **Category and parent:** Secondary; [INFO-1](003-business-architecture.md#info-1)
- **Types:** 
- **States:** 
- **Source:** [CAP-1.8](003-business-architecture.md#cap-1.8)


<a id="info-2"></a>

**INFO-2**

- **Concept and definition:** Organisation — a container for assets belonging to one
  entity.
- **Category and parent:** Primary
- **Types:** None recorded
- **States:** active, inactive, barred, deleted
- **Lifecycle and retention:** Disabled organisations halt all activity until
  superuser reactivation and keep data indefinitely. Deleted organisations retain
  data for 30 days before cleanup; reactivation is possible only before that
  deadline and cancels cleanup. These rules derive from
  [CAP-2.7](003-business-architecture.md#cap-2.7). Mapping the disabled condition to
  the existing inactive/barred vocabulary remains in
  [OPEN-003-2](003-business-architecture.md#open-003-2).
- **Name identity:** Ignore case, trim surrounding whitespace, and collapse internal
  whitespace when checking uniqueness under
  [REQ-1.1.1](002-detailed-requirements.md#req-1.1.1).
- **Superuser:** Exactly one user holds the role for this organisation. Transfer
  requires the current superuser's verification, is irreversible, and immediately
  replaces their superuser status with the recipient's, under
  [REQ-1.1.3](002-detailed-requirements.md#req-1.1.3).
- **Ownership and cleanup:** Owns its assets, not its users. Deletion cleanup includes
  all owned assets and preserves user accounts. While deleted, only reactivation is
  allowed before the cleanup deadline, under
  [REQ-1.5.2](002-detailed-requirements.md#req-1.5.2).
- **Subscription expiry:** Randomly selected excess organisations become disabled,
  preserving their data, under [REQ-1.4.2](002-detailed-requirements.md#req-1.4.2).
- **Source:** [CAP-2](003-business-architecture.md#cap-2)

<a id="info-2.1"></a>

**INFO-2.1**

- **Concept and definition:** Organisation Access Constraint — a policy limiting access to
  organisation resources.
- **Category and parent:** Secondary; [INFO-2](003-business-architecture.md#info-2)
- **Types:** object, attribute
- **States:** None recorded
- **Source:** [CAP-2.4](003-business-architecture.md#cap-2.4)

<a id="info-2.2"></a>

**INFO-2.2**

- **Concept and definition:** Organisation Limit — the maximum number of users allowed
  in an organisation by its applicable subscription.
- **Category and parent:** Secondary; [INFO-2](003-business-architecture.md#info-2)
- **Types:** None recorded
- **States:** None recorded
- **Scope:** Default maximum is three users total, including the superuser. Distinct
  from the user's default limit of one organisation where they are superuser. See
  [REQ-1.4.1](002-detailed-requirements.md#req-1.4.1) and
  [REQ-1.1.4](002-detailed-requirements.md#req-1.1.4).
- **Source:** [CAP-2.5](003-business-architecture.md#cap-2.5)

<a id="info-2.3"></a>

**INFO-2.3**

- **Concept and definition:** Membership Invitation — definition incomplete in the source.
- **Category and parent:** Secondary; [INFO-2](003-business-architecture.md#info-2);
  classification unresolved
- **Types:** Not specified
- **States:** Not specified
- **Source:** [OPEN-003-1](003-business-architecture.md#open-003-1)

<a id="info-3"></a>

**INFO-3**

- **Concept and definition:** Verification Code — a code used to verify authenticity of a
  request or contact verification.
- **Category and parent:** Primary verification-domain record with its own identity
  and retention; associated with a subject rather than owned by an organisation.
  This is the proposed model in
  [ADR-006](decisions/006-verification-code-lifecycle-and-controls.md).
- **Types:** otp; the existing requirement specifies five numeric digits.
- **States:** ready, used, expired, invalidated. `invalidated` represents resend
  replacement; it is derived from [REQ-2.2.4](002-detailed-requirements.md#req-2.2.4).
- **Lifecycle:** Generation creates a ready code. Successful validation consumes it
  as used. A ready code becomes expired at its five-minute deadline or invalidated
  by successful resend. Used, expired, and invalidated codes cannot become ready
  again. Cleanup removes records; removed is not a usable lifecycle state.
- **Purpose:** Each code is paired with the purpose supplied by its requesting
  service. Purpose is a required attribute, distinct from the `otp` type and
  lifecycle state; validation must match it. The purpose vocabulary remains in
  [OPEN-002-3](002-detailed-requirements.md#open-002-3).
- **Validity and retention:** Codes last five minutes, succeed once, and are
  invalidated by resend; all codes are discarded within 24 hours of generation.
  Expiry takes effect at the deadline even if no background task has yet changed a
  stored state. Generation blocks restrict generation, not an otherwise valid code's
  use; no separate validation block has been specified.
- **Generation control:** Independently for each purpose, track five generations
  within ten minutes, a one-hour first block, and a 24-hour second block triggered
  within 24 hours of the first block's start. Other purposes are unaffected.
  Block history belongs to [INFO-5](003-business-architecture.md#info-5) and survives
  code cleanup while still needed. These rules follow [REQ-3.1.2](002-detailed-requirements.md#req-3.1.2),
  [REQ-3.2.1](002-detailed-requirements.md#req-3.2.1),
  [REQ-2.2.4](002-detailed-requirements.md#req-2.2.4), and
  [REQ-3.3.1](002-detailed-requirements.md#req-3.3.1).
- **Source:** [CAP-3](003-business-architecture.md#cap-3)

<a id="info-4"></a>

**INFO-4**

- **Concept and definition:** Invitation — a mechanism allowing users to join an
  organisation.
- **Category and parent:** Primary in source; relationship to
  [INFO-2.3](003-business-architecture.md#info-2.3) unresolved
- **Types:** None recorded
- **States:** active, used, revoked
- **Source:** [CAP-4](003-business-architecture.md#cap-4),
  [OPEN-003-1](003-business-architecture.md#open-003-1)

<a id="info-5"></a>

**INFO-5**

- **Concept and definition:** Verification Code Generation Control — generation history
  and temporary restriction for one service-supplied purpose.
- **Category and parent:** Primary control record, independent of any individual
  code's existence; proposed separation in
  [ADR-006](decisions/006-verification-code-lifecycle-and-controls.md).
- **Types:** None required; one-hour and 24-hour blocks are durations, not code types.
- **States:** allowed, blocked; blocked until its deadline, then allowed to generate
  again. Prior block history remains while needed for escalation.
- **Scope:** The supplied purpose identifies the control. The subject identifies
  which verification a code serves; it does not add another throttle dimension.
- **Retention:** Do not retain code secrets here. Keep only the generation and block
  metadata needed for counting, escalation, and the active block. Removing a code
  must not delete or reset its purpose's control.
- **Source:** [CAP-3.5](003-business-architecture.md#cap-3.5),
  [REQ-3.1.2](002-detailed-requirements.md#req-3.1.2),
  [REQ-3.3.1](002-detailed-requirements.md#req-3.3.1)

### 3.2 Business relationships

Relationships are defined once from their controlling concept or parent. These
associations derive from the original concept relationships and matching
capabilities; cardinalities require scenario validation in 004.

**Controlling concept: [INFO-1](003-business-architecture.md#info-1)**

- **Related concept:** [INFO-1.1](003-business-architecture.md#info-1.1)
- **Business relationship:** Owns dependent profile information.
- **Establishing capability:** [CAP-1.2](003-business-architecture.md#cap-1.2)

**Controlling concept: [INFO-1](003-business-architecture.md#info-1)**

- **Related concept:** [INFO-1.2](003-business-architecture.md#info-1.2)
- **Business relationship:** Owns dependent preference information.
- **Establishing capability:** [CAP-1.3](003-business-architecture.md#cap-1.3)

**Controlling concept: [INFO-1](003-business-architecture.md#info-1)**

- **Related concept:** [INFO-1.3](003-business-architecture.md#info-1.3)
- **Business relationship:** Owns contact methods.
- **Establishing capability:** [CAP-1.7](003-business-architecture.md#cap-1.7)

**Controlling concept: [INFO-1](003-business-architecture.md#info-1)**

- **Related concept:** [OPEN-003-4](003-business-architecture.md#open-003-4)
- **Business relationship:** Associated with a subscription; the subscription concept is
  not yet defined.
- **Establishing capability:** [CAP-1.6.1](003-business-architecture.md#cap-1.6.1)

**Controlling concept: [INFO-1.3](003-business-architecture.md#info-1.3)**

- **Related concept:** [OPEN-003-4](003-business-architecture.md#open-003-4)
- **Business relationship:** Uses a communication channel; channel concept definition is
  missing.
- **Establishing capability:** Association-setting capability not specified;
  [OPEN-003-4](003-business-architecture.md#open-003-4). Channel communication itself is
  [CAP-8](003-business-architecture.md#cap-8).

**Controlling concept: [INFO-2](003-business-architecture.md#info-2)**

- **Related concept:** [INFO-2.1](003-business-architecture.md#info-2.1)
- **Business relationship:** Owns organisation access constraints.
- **Establishing capability:** [CAP-2.4](003-business-architecture.md#cap-2.4)

**Controlling concept: [INFO-2](003-business-architecture.md#info-2)**

- **Related concept:** [INFO-2.2](003-business-architecture.md#info-2.2)
- **Business relationship:** Owns organisation limits.
- **Establishing capability:** [CAP-2.5](003-business-architecture.md#cap-2.5)

**Controlling concept: [INFO-2](003-business-architecture.md#info-2)**

- **Related concept:** [INFO-2.3](003-business-architecture.md#info-2.3)
- **Business relationship:** Source places a dependent membership invitation here;
  reconcile with independent Invitation.
- **Establishing capability:** [CAP-4.4.1](003-business-architecture.md#cap-4.4.1)

**Controlling concept: [INFO-2](003-business-architecture.md#info-2)**

- **Related concept:** [INFO-1](003-business-architecture.md#info-1)
- **Business relationship:** Associates independent users through membership; users
  are not owned by the organisation. Exactly one is superuser, with verified,
  irreversible transfer managed by [CAP-2.2](003-business-architecture.md#cap-2.2).
- **Establishing capability:** [CAP-2.6.1](003-business-architecture.md#cap-2.6.1)

**Controlling concept: [INFO-2](003-business-architecture.md#info-2)**

- **Related concept:** [OPEN-003-4](003-business-architecture.md#open-003-4)
- **Business relationship:** Associated with a subscription; concept not yet defined.
- **Establishing capability:** [CAP-2.6.2](003-business-architecture.md#cap-2.6.2)

**Controlling concept: [INFO-3](003-business-architecture.md#info-3)**

- **Related concept:** [INFO-1](003-business-architecture.md#info-1)
- **Business relationship:** Associates a verification code with its user.
- **Establishing capability:** [CAP-3.4.1](003-business-architecture.md#cap-3.4.1)

**Controlling concept: [INFO-3](003-business-architecture.md#info-3)**

- **Related concept:** [INFO-1.3](003-business-architecture.md#info-1.3)
- **Business relationship:** Associates a verification code with a contact.
- **Establishing capability:** [CAP-3.4.2](003-business-architecture.md#cap-3.4.2)

**Controlling concept: [INFO-4](003-business-architecture.md#info-4)**

- **Related concept:** [INFO-2](003-business-architecture.md#info-2)
- **Business relationship:** Associates an invitation with the organisation to be joined.
- **Establishing capability:** [CAP-4.4.1](003-business-architecture.md#cap-4.4.1)

**Controlling concept: [INFO-4](003-business-architecture.md#info-4)**

- **Related concept:** [INFO-1](003-business-architecture.md#info-1)
- **Business relationship:** Associates an invitation with its intended user.
- **Establishing capability:** [CAP-4.4.2](003-business-architecture.md#cap-4.4.2)

**Controlling concept: [INFO-5](003-business-architecture.md#info-5)**

- **Related concept:** [INFO-3](003-business-architecture.md#info-3)
- **Business relationship:** Controls zero or more code generations sharing one purpose.
  Every code contributes to the history for exactly that purpose. Codes may be cleaned
  up while the control remains; this is an association, not lifecycle ownership.
- **Establishing capability:** [CAP-3.5](003-business-architecture.md#cap-3.5)

## 4 Value streams

### 4.1 Register User

<a id="vs-1"></a>**VS-1 — Register User.** A customer submits details and establishes
a verified account. Triggering stakeholder: customer/user. Value proposition: the
customer has a verified account. Sources:
[REQ-2.1.1](002-detailed-requirements.md#req-2.1.1),
[REQ-2.1.2](002-detailed-requirements.md#req-2.1.2)
and the original Register user map.

The stage names and order are preserved. Entry/exit conditions and value items are
derived descriptions, subject to the linked open policies; they do not establish
new mandatory fields or equate contact verification with account activation.

<a id="stage-1.1"></a>

**STAGE-1.1 — Submit User Details**

- **Entry:** Customer seeks an account.
- **Exit:** Details are submitted.
- **Value item:** A registration request is captured.
- **Participants:** Customer; service
- **Enabling capabilities:** [CAP-11.1](003-business-architecture.md#cap-11.1),
  [CAP-11.2](003-business-architecture.md#cap-11.2),
  [CAP-11.3](003-business-architecture.md#cap-11.3)

<a id="stage-1.2"></a>

**STAGE-1.2 — Accept User Details**

- **Entry:** Submitted details are available.
- **Exit:** Details satisfy the applicable schema and validation rules;
  [REQ-2.1.1](002-detailed-requirements.md#req-2.1.1) defines phone form and
  [REQ-2.2.1](002-detailed-requirements.md#req-2.2.1) defines password rules.
  Remaining submission rules are [OPEN-002-2](002-detailed-requirements.md#open-002-2).
- **Value item:** The request is fit for account creation.
- **Participants:** Customer; service
- **Enabling capabilities:** [CAP-11.4](003-business-architecture.md#cap-11.4)

<a id="stage-1.3"></a>

**STAGE-1.3 — Create User Account**

- **Entry:** Accepted details are available.
- **Exit:** A new, nonduplicate user record exists; initial lifecycle state requires
  [OPEN-003-2](003-business-architecture.md#open-003-2).
- **Value item:** The customer is represented by an account.
- **Participants:** Customer; service
- **Enabling capabilities:** [CAP-1.1](003-business-architecture.md#cap-1.1),
  [CAP-5.1](003-business-architecture.md#cap-5.1),
  [CAP-5.2](003-business-architecture.md#cap-5.2),
  [CAP-5.3](003-business-architecture.md#cap-5.3)

<a id="stage-1.4"></a>

**STAGE-1.4 — Initialise Verification**

- **Entry:** The account and phone details exist.
- **Exit:** A code is generated within the generation limits and verification is initiated;
  delivery responsibility is [OPEN-003-3](003-business-architecture.md#open-003-3).
- **Value item:** The customer has a path to proving phone ownership.
- **Participants:** Customer; service; messaging participant
- **Enabling capabilities:** [CAP-3.1](003-business-architecture.md#cap-3.1),
  [CAP-3.5](003-business-architecture.md#cap-3.5),
  [CAP-5.1](003-business-architecture.md#cap-5.1),
  [CAP-5.2](003-business-architecture.md#cap-5.2),
  [CAP-5.3](003-business-architecture.md#cap-5.3)

<a id="stage-1.5"></a>

**STAGE-1.5 — Verify User**

- **Entry:** The user submits proof for the initiated verification.
- **Exit:** Valid proof produces the defined verified outcome; account/contact transitions
  require [OPEN-003-2](003-business-architecture.md#open-003-2).
- **Value item:** The customer has a verified account.
- **Participants:** Customer; service
- **Enabling capabilities:** [CAP-11.1](003-business-architecture.md#cap-11.1),
  [CAP-11.4](003-business-architecture.md#cap-11.4),
  [CAP-3.2](003-business-architecture.md#cap-3.2),
  [CAP-1.4](003-business-architecture.md#cap-1.4),
  [CAP-5.1](003-business-architecture.md#cap-5.1),
  [CAP-5.2](003-business-architecture.md#cap-5.2),
  [CAP-5.3](003-business-architecture.md#cap-5.3)

### 4.2 Notify User

<a id="vs-2"></a>**VS-2 — Notify User.** Receive a notification request, prepare a
message, and dispatch it through a channel. The source provides these stages;
the intended value is that a user receives relevant information under their
preferences and permissions. Triggering stakeholder and delivery-success criteria
require [OPEN-003-3](003-business-architecture.md#open-003-3). Sources:
[REQ-2.3.1](002-detailed-requirements.md#req-2.3.1),
[REQ-2.3.2](002-detailed-requirements.md#req-2.3.2).

<a id="stage-2.1"></a>

**STAGE-2.1 — Receive Notification Request**

- **Entry:** A notice is requested.
- **Exit:** Notice type, preferences, and permissions determine whether dispatch is
  appropriate.
- **Value item:** A relevant, permitted communication is selected.
- **Participants:** Requesting participant unresolved; recipient user
- **Enabling capabilities:** [CAP-5.1](003-business-architecture.md#cap-5.1),
  [CAP-5.2](003-business-architecture.md#cap-5.2),
  [CAP-5.3](003-business-architecture.md#cap-5.3),
  [CAP-6.2](003-business-architecture.md#cap-6.2),
  [CAP-6.5](003-business-architecture.md#cap-6.5),
  [CAP-1.3](003-business-architecture.md#cap-1.3)

<a id="stage-2.2"></a>

**STAGE-2.2 — Prepare Message**

- **Entry:** Dispatch is permitted.
- **Exit:** Content, message form, channel choice, and status tracking are prepared.
- **Value item:** The notice is ready for the selected medium.
- **Participants:** Service; recipient preferences apply
- **Enabling capabilities:** [CAP-6.3](003-business-architecture.md#cap-6.3),
  [CAP-6.4](003-business-architecture.md#cap-6.4),
  [CAP-7.1](003-business-architecture.md#cap-7.1),
  [CAP-7.3](003-business-architecture.md#cap-7.3),
  [CAP-7.5](003-business-architecture.md#cap-7.5),
  [CAP-7.6](003-business-architecture.md#cap-7.6)

<a id="stage-2.3"></a>

**STAGE-2.3 — Dispatch Message**

- **Entry:** A prepared communication and channel configuration are available.
- **Exit:** The message is dispatched; acceptance versus delivery remains
  [OPEN-003-3](003-business-architecture.md#open-003-3).
- **Value item:** Information is conveyed toward the recipient.
- **Participants:** Channel provider; recipient
- **Enabling capabilities:** [CAP-7.4](003-business-architecture.md#cap-7.4),
  [CAP-8.1](003-business-architecture.md#cap-8.1),
  [CAP-8.2](003-business-architecture.md#cap-8.2),
  [CAP-8.3](003-business-architecture.md#cap-8.3),
  [CAP-9.1](003-business-architecture.md#cap-9.1),
  [CAP-9.2](003-business-architecture.md#cap-9.2)

The original preparation stage listed message dispatch; the dispatch capability
is mapped to Dispatch Message, where its outcome occurs. Event definition/capture/
distribution are retained from the original receipt stage; whether all three
perform work there, rather than upstream, remains
[OPEN-003-3](003-business-architecture.md#open-003-3).

### 4.3 Onboard User

<a id="vs-3"></a>**VS-3 — Onboard User.** The source contains only this title.
Triggering stakeholder, distinct value proposition, stages, and relationship to
Register User require [OPEN-003-3](003-business-architecture.md#open-003-3). No stage IDs
are allocated yet.

## 5 Cross-mappings and downstream derivation

Use means consuming business information; modify means creating, changing, or
removing it. These are derived responsibility mappings, not database permissions.

**Concept: [INFO-1](003-business-architecture.md#info-1)**

- **Capabilities that use it:** [CAP-3.4.1](003-business-architecture.md#cap-3.4.1),
  [CAP-2.6.1](003-business-architecture.md#cap-2.6.1),
  [CAP-2.2](003-business-architecture.md#cap-2.2),
  [CAP-2.5.3](003-business-architecture.md#cap-2.5.3)
- **Capabilities that modify it:** [CAP-1.1](003-business-architecture.md#cap-1.1),
  [CAP-1.4](003-business-architecture.md#cap-1.4),
  [CAP-1.5](003-business-architecture.md#cap-1.5)
- **Data/application references:** [DATA-1](004-data-architecture.md#data-1),
  [APP-2](005-application-architecture.md#app-2)

**Concept: [INFO-1.1](003-business-architecture.md#info-1.1)**

- **Capabilities that use it:** [CAP-1.2](003-business-architecture.md#cap-1.2)
- **Capabilities that modify it:** [CAP-1.2](003-business-architecture.md#cap-1.2)
- **Data/application references:** [DATA-2](004-data-architecture.md#data-2),
  [APP-2](005-application-architecture.md#app-2)

**Concept: [INFO-1.2](003-business-architecture.md#info-1.2)**

- **Capabilities that use it:** [CAP-6.2](003-business-architecture.md#cap-6.2),
  [CAP-6.3](003-business-architecture.md#cap-6.3)
- **Capabilities that modify it:** [CAP-1.3](003-business-architecture.md#cap-1.3)
- **Data/application references:** [DATA-3](004-data-architecture.md#data-3),
  [APP-2](005-application-architecture.md#app-2),
  [APP-5](005-application-architecture.md#app-5)

**Concept: [INFO-1.3](003-business-architecture.md#info-1.3)**

- **Capabilities that use it:** [CAP-3.4.2](003-business-architecture.md#cap-3.4.2),
  [CAP-8](003-business-architecture.md#cap-8)
- **Capabilities that modify it:** [CAP-1.7.1](003-business-architecture.md#cap-1.7.1),
  [CAP-1.7.2](003-business-architecture.md#cap-1.7.2),
  [CAP-1.7.3](003-business-architecture.md#cap-1.7.3)
- **Data/application references:** [DATA-4](004-data-architecture.md#data-4),
  [APP-2](005-application-architecture.md#app-2),
  [APP-3](005-application-architecture.md#app-3)

**Concept: [INFO-2](003-business-architecture.md#info-2)**

- **Capabilities that use it:** [CAP-4.4.1](003-business-architecture.md#cap-4.4.1)
- **Capabilities that modify it:** [CAP-2.1](003-business-architecture.md#cap-2.1),
  [CAP-2.2](003-business-architecture.md#cap-2.2),
  [CAP-2.3](003-business-architecture.md#cap-2.3),
  [CAP-2.5.3](003-business-architecture.md#cap-2.5.3),
  [CAP-2.7](003-business-architecture.md#cap-2.7)
- **Data/application references:** [DATA-5](004-data-architecture.md#data-5),
  [APP-8](005-application-architecture.md#app-8)

**Concept: [INFO-2.1](003-business-architecture.md#info-2.1)**

- **Capabilities that use it:** [CAP-2.4.2](003-business-architecture.md#cap-2.4.2),
  [CAP-2.4.3](003-business-architecture.md#cap-2.4.3)
- **Capabilities that modify it:** [CAP-2.4.1](003-business-architecture.md#cap-2.4.1)
- **Data/application references:** [DATA-6](004-data-architecture.md#data-6),
  [APP-8](005-application-architecture.md#app-8)

**Concept: [INFO-2.2](003-business-architecture.md#info-2.2)**

- **Capabilities that use it:** [CAP-2.5.2](003-business-architecture.md#cap-2.5.2),
  [CAP-2.5.3](003-business-architecture.md#cap-2.5.3)
- **Capabilities that modify it:** [CAP-2.5.1](003-business-architecture.md#cap-2.5.1)
- **Data/application references:** [DATA-7](004-data-architecture.md#data-7),
  [APP-8](005-application-architecture.md#app-8)

**Concept: [INFO-2.3](003-business-architecture.md#info-2.3)**

- **Capabilities that use it:** [OPEN-003-1](003-business-architecture.md#open-003-1)
- **Capabilities that modify it:** [OPEN-003-1](003-business-architecture.md#open-003-1)
- **Data/application references:** [DATA-8](004-data-architecture.md#data-8)

**Concept: [INFO-3](003-business-architecture.md#info-3)**

- **Capabilities that use it:** [CAP-3.2](003-business-architecture.md#cap-3.2)
- **Capabilities that modify it:** [CAP-3.2](003-business-architecture.md#cap-3.2),
  [CAP-3.1](003-business-architecture.md#cap-3.1),
  [CAP-3.3](003-business-architecture.md#cap-3.3),
  [CAP-3.4](003-business-architecture.md#cap-3.4)
- **Data/application references:** [DATA-9](004-data-architecture.md#data-9),
  [APP-3](005-application-architecture.md#app-3)

**Concept: [INFO-4](003-business-architecture.md#info-4)**

- **Capabilities that use it:** [CAP-4.3.2](003-business-architecture.md#cap-4.3.2),
  [CAP-4.3.3](003-business-architecture.md#cap-4.3.3)
- **Capabilities that modify it:** [CAP-4.1](003-business-architecture.md#cap-4.1),
  [CAP-4.2](003-business-architecture.md#cap-4.2),
  [CAP-4.4](003-business-architecture.md#cap-4.4)
- **Data/application references:** [DATA-10](004-data-architecture.md#data-10),
  [APP-8](005-application-architecture.md#app-8)

**Concept: [INFO-5](003-business-architecture.md#info-5)**

- **Capabilities that use it:** [CAP-3.1](003-business-architecture.md#cap-3.1),
  [CAP-3.5](003-business-architecture.md#cap-3.5)
- **Capabilities that modify it:** [CAP-3.5](003-business-architecture.md#cap-3.5)
- **Data/application references:** [DATA-11](004-data-architecture.md#data-11),
  [APP-3](005-application-architecture.md#app-3)

The registration trace joins [REQ-2.1.1](002-detailed-requirements.md#req-2.1.1),
[REQ-2.1.2](002-detailed-requirements.md#req-2.1.2) →
[VS-1](003-business-architecture.md#vs-1),
[CAP-1.1](003-business-architecture.md#cap-1.1),
[CAP-3.2](003-business-architecture.md#cap-3.2) →
[INFO-1](003-business-architecture.md#info-1),
[INFO-1.3](003-business-architecture.md#info-1.3),
[INFO-3](003-business-architecture.md#info-3) → [DATA-1](004-data-architecture.md#data-1),
[DATA-4](004-data-architecture.md#data-4), [DATA-9](004-data-architecture.md#data-9) →
[APP-2](005-application-architecture.md#app-2),
[APP-3](005-application-architecture.md#app-3),
[TECH-3](006-technology-architecture.md#tech-3). It records design intent, not an
implemented end-to-end feature.

## 6 Open decisions

<a id="open-003-1"></a>

**OPEN-003-1**

- **Question and impact:** The source defines both dependent Membership Invitation under
  Organisation and independent Invitation. Decide whether these are one concept and its
  ownership, or distinct concepts. Preserve both until resolved; data candidates must not
  create duplicate invitation tables by default.

<a id="open-003-2"></a>

**OPEN-003-2**

- **Question and impact:** Complete state meanings, transitions, prerequisites, and
  effects for users, contacts, organisations, codes, and invitations around the
  resolved retention, disable, reactivation, code expiry/use, and resend rules in
  [OPEN-002-1](002-detailed-requirements.md#open-002-1),
  [OPEN-002-2](002-detailed-requirements.md#open-002-2), and
  [OPEN-002-3](002-detailed-requirements.md#open-002-3). Map the disabled organisation
  condition to the recorded state vocabulary. Code lifecycle, subject associations,
  and separate generation control are refined in
  [INFO-3](003-business-architecture.md#info-3), [INFO-5](003-business-architecture.md#info-5),
  and proposed [ADR-006](decisions/006-verification-code-lifecycle-and-controls.md).
  Confirm remaining role/type semantics and whether account activation follows
  contact verification. These decisions constrain data and typestate APIs.

<a id="open-003-3"></a>

**OPEN-003-3**

- **Question and impact:** Define Onboard User and its relationship to registration;
  notification requester, preferences for verification messages, rejected-dispatch exits,
  delivery guarantees, and registration-to-message handoff. Validate the event
  capabilities active at each stage.

<a id="open-003-4"></a>

**OPEN-003-4**

- **Question and impact:** Complete business coverage for subscriptions, sites, assets,
  gateways, sensors, actuators, thresholds, and actions. Define referenced channel, event,
  notification, message, and audit information concepts; establish audit and
  publisher-registration requirements. Existing capability names are retained without
  inventing missing requirements.

## 7 Legacy reference mapping

### 7.1 Capabilities

Original labels refer to the former Capability Mapping section unless a value
stream is explicitly named. Duplicate numbers are disambiguated by map and name.
Message and later roots are renumbered to remove the repeated 6; submission and
stage-only subcapabilities are promoted from existing value-map descriptions.
WhatsApp API Management is expressed as a communication ability; API mechanics
belong in application/technology design.

**Original map, label, or stage reference: User map / 1**

- **New identifier:** [CAP-1](003-business-architecture.md#cap-1)

**Original map, label, or stage reference: User map / 1.1**

- **New identifier:** [CAP-1.1](003-business-architecture.md#cap-1.1)

**Original map, label, or stage reference: User map / 1.2**

- **New identifier:** [CAP-1.2](003-business-architecture.md#cap-1.2)

**Original map, label, or stage reference: User map / 1.3**

- **New identifier:** [CAP-1.3](003-business-architecture.md#cap-1.3)

**Original map, label, or stage reference: User map / 1.4**

- **New identifier:** [CAP-1.4](003-business-architecture.md#cap-1.4)

**Original map, label, or stage reference: User map / 1.5**

- **New identifier:** [CAP-1.5](003-business-architecture.md#cap-1.5)

**Original map, label, or stage reference: User map / 1.6**

- **New identifier:** [CAP-1.6](003-business-architecture.md#cap-1.6)

**Original map, label, or stage reference: User map / 1.6.1**

- **New identifier:** [CAP-1.6.1](003-business-architecture.md#cap-1.6.1)

**Original map, label, or stage reference: User map / 1.7**

- **New identifier:** [CAP-1.7](003-business-architecture.md#cap-1.7)

**Original map, label, or stage reference: User map / 1.7.1**

- **New identifier:** [CAP-1.7.1](003-business-architecture.md#cap-1.7.1)

**Original map, label, or stage reference: User map / 1.7.2**

- **New identifier:** [CAP-1.7.2](003-business-architecture.md#cap-1.7.2)

**Original map, label, or stage reference: User map / 1.7.3**

- **New identifier:** [CAP-1.7.3](003-business-architecture.md#cap-1.7.3)

**Original map, label, or stage reference: Organisation map / 2**

- **New identifier:** [CAP-2](003-business-architecture.md#cap-2)

**Original map, label, or stage reference: Organisation map / 1.1**

- **New identifier:** [CAP-2.1](003-business-architecture.md#cap-2.1)

**Original map, label, or stage reference: Organisation map / 1.2**

- **New identifier:** [CAP-2.2](003-business-architecture.md#cap-2.2)

**Original map, label, or stage reference: Organisation map / 1.3**

- **New identifier:** [CAP-2.3](003-business-architecture.md#cap-2.3)

**Original map, label, or stage reference: Organisation map / 1.3.2.1 member list**

- **New identifier:** [CAP-2.3.1](003-business-architecture.md#cap-2.3.1)

**Original map, label, or stage reference: Organisation map / 1.3.2.1 member access**

- **New identifier:** [CAP-2.3.2](003-business-architecture.md#cap-2.3.2)

**Original map, label, or stage reference: Organisation map / 1.4**

- **New identifier:** [CAP-2.4](003-business-architecture.md#cap-2.4)

**Original map, label, or stage reference: Organisation map / 1.4.1**

- **New identifier:** [CAP-2.4.1](003-business-architecture.md#cap-2.4.1)

**Original map, label, or stage reference: Organisation map / 1.4.2**

- **New identifier:** [CAP-2.4.2](003-business-architecture.md#cap-2.4.2)

**Original map, label, or stage reference: Organisation map / 1.4.3**

- **New identifier:** [CAP-2.4.3](003-business-architecture.md#cap-2.4.3)

**Original map, label, or stage reference: Organisation map / 1.5**

- **New identifier:** [CAP-2.5](003-business-architecture.md#cap-2.5)

**Original map, label, or stage reference: Organisation map / 1.5.1**

- **New identifier:** [CAP-2.5.1](003-business-architecture.md#cap-2.5.1)

**Original map, label, or stage reference: Organisation map / 1.5.2**

- **New identifier:** [CAP-2.5.2](003-business-architecture.md#cap-2.5.2)

**Original map, label, or stage reference: Organisation map / 1.5.3**

- **New identifier:** [CAP-2.5.3](003-business-architecture.md#cap-2.5.3)

**Original map, label, or stage reference: Organisation map / 1.6**

- **New identifier:** [CAP-2.6](003-business-architecture.md#cap-2.6)

**Original map, label, or stage reference: Organisation map / 1.6 unnumbered organisation/user**

- **New identifier:** [CAP-2.6.1](003-business-architecture.md#cap-2.6.1)

**Original map, label, or stage reference: Organisation map / 1.6 unnumbered
organisation/subscription**

- **New identifier:** [CAP-2.6.2](003-business-architecture.md#cap-2.6.2)

**Original map, label, or stage reference: Verification map / 3**

- **New identifier:** [CAP-3](003-business-architecture.md#cap-3)

**Original map, label, or stage reference: Verification map / 3.1**

- **New identifier:** [CAP-3.1](003-business-architecture.md#cap-3.1)

**Original map, label, or stage reference: Verification map / 3.2**

- **New identifier:** [CAP-3.2](003-business-architecture.md#cap-3.2)

**Original map, label, or stage reference: Verification map / 3.3**

- **New identifier:** [CAP-3.3](003-business-architecture.md#cap-3.3)

**Original map, label, or stage reference: Verification map / 3.4**

- **New identifier:** [CAP-3.4](003-business-architecture.md#cap-3.4)

**Original map, label, or stage reference: Verification map / 3.4.1**

- **New identifier:** [CAP-3.4.1](003-business-architecture.md#cap-3.4.1)

**Original map, label, or stage reference: Verification map / 3.4.2**

- **New identifier:** [CAP-3.4.2](003-business-architecture.md#cap-3.4.2)

**Original map, label, or stage reference: Invitation map / 4**

- **New identifier:** [CAP-4](003-business-architecture.md#cap-4)

**Original map, label, or stage reference: Invitation map / 4.1**

- **New identifier:** [CAP-4.1](003-business-architecture.md#cap-4.1)

**Original map, label, or stage reference: Invitation map / 4.2**

- **New identifier:** [CAP-4.2](003-business-architecture.md#cap-4.2)

**Original map, label, or stage reference: Invitation map / 4.3**

- **New identifier:** [CAP-4.3](003-business-architecture.md#cap-4.3)

**Original map, label, or stage reference: Invitation map / 4.3.1**

- **New identifier:** [CAP-4.3.1](003-business-architecture.md#cap-4.3.1)

**Original map, label, or stage reference: Invitation map / 4.3.2**

- **New identifier:** [CAP-4.3.2](003-business-architecture.md#cap-4.3.2)

**Original map, label, or stage reference: Invitation map / 4.3.3**

- **New identifier:** [CAP-4.3.3](003-business-architecture.md#cap-4.3.3)

**Original map, label, or stage reference: Invitation map / 4.4**

- **New identifier:** [CAP-4.4](003-business-architecture.md#cap-4.4)

**Original map, label, or stage reference: Invitation map / 4.4.1**

- **New identifier:** [CAP-4.4.1](003-business-architecture.md#cap-4.4.1)

**Original map, label, or stage reference: Invitation map / 4.4.2**

- **New identifier:** [CAP-4.4.2](003-business-architecture.md#cap-4.4.2)

**Original map, label, or stage reference: Event map / 5**

- **New identifier:** [CAP-5](003-business-architecture.md#cap-5)

**Original map, label, or stage reference: Event map / 5.1**

- **New identifier:** [CAP-5.1](003-business-architecture.md#cap-5.1)

**Original map, label, or stage reference: Event map / 5.2**

- **New identifier:** [CAP-5.2](003-business-architecture.md#cap-5.2)

**Original map, label, or stage reference: Event map / 5.3**

- **New identifier:** [CAP-5.3](003-business-architecture.md#cap-5.3)

**Original map, label, or stage reference: Event map / 5.4**

- **New identifier:** [CAP-5.4](003-business-architecture.md#cap-5.4)

**Original map, label, or stage reference: Event map / 5.5**

- **New identifier:** [CAP-5.5](003-business-architecture.md#cap-5.5)

**Original map, label, or stage reference: Notification map / 6**

- **New identifier:** [CAP-6](003-business-architecture.md#cap-6)

**Original map, label, or stage reference: Notification map / 6.1**

- **New identifier:** [CAP-6.1](003-business-architecture.md#cap-6.1)

**Original map, label, or stage reference: Notification map / 6.2; Notify user / receive
notification request**

- **New identifier:** [CAP-6.2](003-business-architecture.md#cap-6.2)

**Original map, label, or stage reference: Notification map / 6.3**

- **New identifier:** [CAP-6.3](003-business-architecture.md#cap-6.3)

**Original map, label, or stage reference: Notification map / 6.4**

- **New identifier:** [CAP-6.4](003-business-architecture.md#cap-6.4)

**Original map, label, or stage reference: Notify user / receive notification request /
notification type determination**

- **New identifier:** [CAP-6.5](003-business-architecture.md#cap-6.5)

**Original map, label, or stage reference: Message map / second 6**

- **New identifier:** [CAP-7](003-business-architecture.md#cap-7)

**Original map, label, or stage reference: Message map / 6.1**

- **New identifier:** [CAP-7.1](003-business-architecture.md#cap-7.1)

**Original map, label, or stage reference: Message map / 6.2**

- **New identifier:** [CAP-7.2](003-business-architecture.md#cap-7.2)

**Original map, label, or stage reference: Message map / 6.3; Notify user / prepare /
message type determination**

- **New identifier:** [CAP-7.3](003-business-architecture.md#cap-7.3)

**Original map, label, or stage reference: Message map / 6.4**

- **New identifier:** [CAP-7.4](003-business-architecture.md#cap-7.4)

**Original map, label, or stage reference: Message map / 6.5**

- **New identifier:** [CAP-7.5](003-business-architecture.md#cap-7.5)

**Original map, label, or stage reference: Notify user / prepare / message status tracking**

- **New identifier:** [CAP-7.6](003-business-architecture.md#cap-7.6)

**Original map, label, or stage reference: Channel map / 7**

- **New identifier:** [CAP-8](003-business-architecture.md#cap-8)

**Original map, label, or stage reference: Notify user / dispatch message / channel identification**

- **New identifier:** [CAP-8.1](003-business-architecture.md#cap-8.1)

**Original map, label, or stage reference: Notify user / dispatch message / channel configuration**

- **New identifier:** [CAP-8.2](003-business-architecture.md#cap-8.2)

**Original map, label, or stage reference: Notify user / dispatch message / message dispatch**

- **New identifier:** [CAP-8.3](003-business-architecture.md#cap-8.3)

**Original map, label, or stage reference: WhatsApp API map / 8**

- **New identifier:** [CAP-9](003-business-architecture.md#cap-9)

**Original map, label, or stage reference: Notify user / dispatch message / API configuration**

- **New identifier:** [CAP-9.1](003-business-architecture.md#cap-9.1)

**Original map, label, or stage reference: Notify user / dispatch message / message dispatch**

- **New identifier:** [CAP-9.2](003-business-architecture.md#cap-9.2)

**Original map, label, or stage reference: Audit log map / 9**

- **New identifier:** [CAP-10](003-business-architecture.md#cap-10)

**Original map, label, or stage reference: Register user / submit and accept user details**

- **New identifier:** [CAP-11](003-business-architecture.md#cap-11)

**Original map, label, or stage reference: Register user / submit user details / input
data capture**

- **New identifier:** [CAP-11.1](003-business-architecture.md#cap-11.1)

**Original map, label, or stage reference: Register user / submit user details / form submission**

- **New identifier:** [CAP-11.2](003-business-architecture.md#cap-11.2)

**Original map, label, or stage reference: Register user / submit user details / data
schema determination**

- **New identifier:** [CAP-11.3](003-business-architecture.md#cap-11.3)

**Original map, label, or stage reference: Register user / submit and accept / schema,
phone number, name, password validation**

- **New identifier:** [CAP-11.4](003-business-architecture.md#cap-11.4)

### 7.2 Information and value maps

**Original section and label: Information Map / 1 User**

- **New identifier:** [INFO-1](003-business-architecture.md#info-1)

**Original section and label: Information Map / User / 1.1 Profile**

- **New identifier:** [INFO-1.1](003-business-architecture.md#info-1.1)

**Original section and label: Information Map / User / 1.2 Preferences**

- **New identifier:** [INFO-1.2](003-business-architecture.md#info-1.2)

**Original section and label: Information Map / User / 1.3 Contact**

- **New identifier:** [INFO-1.3](003-business-architecture.md#info-1.3)

**Original section and label: Information Map / 2 Organisation**

- **New identifier:** [INFO-2](003-business-architecture.md#info-2)

**Original section and label: Information Map / Organisation / 1.1 Access constraint**

- **New identifier:** [INFO-2.1](003-business-architecture.md#info-2.1)

**Original section and label: Information Map / Organisation / 1.2 Limit**

- **New identifier:** [INFO-2.2](003-business-architecture.md#info-2.2)

**Original section and label: Information Map / Organisation / 1.3 Membership invitation
(unfinished)**

- **New identifier:** [INFO-2.3](003-business-architecture.md#info-2.3),
  [OPEN-003-1](003-business-architecture.md#open-003-1)

**Original section and label: Information Map / 3 Verification code**

- **New identifier:** [INFO-3](003-business-architecture.md#info-3)

**Original section and label: Information Map / 4 Invitation**

- **New identifier:** [INFO-4](003-business-architecture.md#info-4)

**Original section and label: Value Map / 1 Register user; five stages in original order**

- **New identifier:** [VS-1](003-business-architecture.md#vs-1),
  [STAGE-1.1](003-business-architecture.md#stage-1.1),
  [STAGE-1.2](003-business-architecture.md#stage-1.2),
  [STAGE-1.3](003-business-architecture.md#stage-1.3),
  [STAGE-1.4](003-business-architecture.md#stage-1.4),
  [STAGE-1.5](003-business-architecture.md#stage-1.5)

**Original section and label: Value Map / 2 Notify user; three stages in original order**

- **New identifier:** [VS-2](003-business-architecture.md#vs-2),
  [STAGE-2.1](003-business-architecture.md#stage-2.1),
  [STAGE-2.2](003-business-architecture.md#stage-2.2),
  [STAGE-2.3](003-business-architecture.md#stage-2.3)

**Original section and label: Value Map / 3 Onboard user (title only)**

- **New identifier:** [VS-3](003-business-architecture.md#vs-3),
  [OPEN-003-3](003-business-architecture.md#open-003-3)
