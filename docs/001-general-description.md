# General description

## 1 Purpose

IoT Hub connects equipment to the cloud so authorized people can monitor and
control it remotely. It collects device telemetry and sends actuator commands
through IoT gateways. It supports coordinated systems of devices and associated
data, beyond the scope of an individual smart switch.

## 2 Stakeholders and scope

Users create profiles and access resources according to permissions. Organisation
superusers own organisations and delegate access to other users. Customers benefit
from remote visibility and control; physical gateways connect sensors and actuators.
The business map also names staff as a user type. Detailed stakeholder powers
require definition in the requirements.

The product includes organisation-scoped sites and assets, telemetry collection,
gateway configuration, and actuator control. Mobile, web, and WhatsApp interfaces
are intended access channels. Release boundaries and priorities are not yet defined;
see [OPEN-001-1](#open-001-1). No exclusions are inferred from missing implementation.

## 3 Domain and capability overview

**Area: Users**

- **Business meaning and desired behavior:** Create and manage profiles; authenticate and
  access resources securely.
- **Detailed requirements:** [REQ-2.1.1](002-detailed-requirements.md#req-2.1.1),
  [REQ-2.2.6](002-detailed-requirements.md#req-2.2.6),
  [REQ-2.5.1](002-detailed-requirements.md#req-2.5.1)

**Area: Organisations**

- **Business meaning and desired behavior:** Top-level grouping for assets, devices,
  sensors, and actuators; owned by a superuser who can invite users and customize access.
  A user may create one or more organisations according to usage tier.
- **Detailed requirements:** [REQ-1.1.1](002-detailed-requirements.md#req-1.1.1),
  [REQ-1.1.4](002-detailed-requirements.md#req-1.1.4),
  [REQ-1.2.3](002-detailed-requirements.md#req-1.2.3)

**Area: Sites and assets**

- **Business meaning and desired behavior:** Organisations support multiple sites; sites
  contain assets representing physical equipment, and assets contain sensors and
  actuators. Users create and link these according to organisation and object permissions.
  A water tank is an example asset with a water-level sensor.
- **Detailed requirements:** [REQ-5.1.1](002-detailed-requirements.md#req-5.1.1),
  [REQ-5.1.2](002-detailed-requirements.md#req-5.1.2)

**Area: Gateways**

- **Business meaning and desired behavior:** Represent physical IoT gateways and convey
  telemetry and commands. Every sensor and actuator is linked to a gateway. Users
  configure gateways and obtain device authentication certificates.
- **Detailed requirements:** [REQ-5.2.1](002-detailed-requirements.md#req-5.2.1),
  [REQ-5.2.2](002-detailed-requirements.md#req-5.2.2),
  [REQ-5.2.3](002-detailed-requirements.md#req-5.2.3)

**Area: Sensors and actuators**

- **Business meaning and desired behavior:** Sensors collect observations; actuators
  accept control commands.
- **Detailed requirements:** [REQ-5.3.1](002-detailed-requirements.md#req-5.3.1)

**Area: Thresholds and actions**

- **Business meaning and desired behavior:** Sensor thresholds trigger actions which
  change actuator states and send commands.
- **Detailed requirements:** [REQ-5.4.1](002-detailed-requirements.md#req-5.4.1),
  [REQ-5.4.2](002-detailed-requirements.md#req-5.4.2)

## 4 Quality goals

**Area: Security**

- **Goal:** Gateway mutual TLS, identity-based MQTT topic access, and certificate
  invalidation.
- **Detailed requirements:** [NFR-1.1](002-detailed-requirements.md#nfr-1.1),
  [NFR-1.2](002-detailed-requirements.md#nfr-1.2),
  [NFR-1.3](002-detailed-requirements.md#nfr-1.3)

**Area: Accessibility**

- **Goal:** Service access through mobile, web, and WhatsApp.
- **Detailed requirements:** [NFR-2.1](002-detailed-requirements.md#nfr-2.1)

**Area: Reliability**

- **Goal:** High availability; store-and-send capable gateways are preferred and
  recommended to users.
- **Detailed requirements:** [NFR-3.1](002-detailed-requirements.md#nfr-3.1),
  [NFR-3.2](002-detailed-requirements.md#nfr-3.2)

**Area: Usability**

- **Goal:** Simple language understandable by nontechnical users.
- **Detailed requirements:** [NFR-4.1](002-detailed-requirements.md#nfr-4.1)

## 5 Traceability

[002](002-detailed-requirements.md) owns detailed obligations and acceptance
criteria, including requirements elaborated from this overview. The
[business architecture](003-business-architecture.md) expresses the abilities,
information, and value needed to satisfy them. Scope in this overview does not
mean every corresponding capability, data entity, or endpoint is implemented.

## 6 Assumptions and legacy mapping

<a id="open-001-1"></a>**OPEN-001-1 — Release scope and stakeholder detail.**
The source defines product goals but no release boundaries, delivery priorities,
or complete stakeholder responsibilities. Agree these before treating coverage
gaps as exclusions or assigning unrecorded permissions.

**Original section: Overview**

- **New location:** Sections 1–2

**Original section: Functional requirements / Users**

- **New location:** Section 3, Users

**Original section: Functional requirements / Organisations**

- **New location:** Section 3, Organisations

**Original section: Functional requirements / Sites, gateway, assets, sensors and assets**

- **New location:** Section 3, Sites and assets

**Original section: Functional requirements / Gateway**

- **New location:** Section 3, Gateways

**Original section: Functional requirements / Assets**

- **New location:** Section 3, Sites and assets, including the tank example

**Original section: Functional requirements / Sensors and actuators**

- **New location:** Section 3, Sensors and actuators

**Original section: Functional requirements / Thresholds and actions**

- **New location:** Section 3, Thresholds and actions

**Original section: Non-functional requirements / Security, Accessibility, Reliability, Usability**

- **New location:** Section 4 and the corresponding NFR records in 002
