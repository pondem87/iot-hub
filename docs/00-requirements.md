# IoT Hub – Requirements

## 1. Overview

The system shall provide cloud-based monitoring and remote control of physical assets commonly used in:
- Agriculture (farmer use case)
- Residential / real-estate management (multi-tenant infrastructure)

The platform shall support real-time visibility, historical analysis, alerting and control, accessible via a mobile application.

---

## 2. User Types

### 2.1 Farmer

A farmer requires visibility and control over infrastructure such as:
- Boreholes (water level, recovery rate, usage)
- Water pumps (state, running hours)
- Generators (state, running hours)
- Solar systems
- Irrigation systems
- Greenhouses

---

### 2.2 Real Estate Manager

A real estate manager oversees:
- Multiple housing units
- Shared infrastructure (e.g. borehole, water storage)
- Independent solar systems per unit

They require:
- Cluster-level monitoring
- Asset sharing visibility
- Tenant-independent operational control
- Traceability of asset usage across individual units

#### Liability & Accountability Requirements

- The system shall record asset usage per unit or tenant where applicable
- The system shall maintain a tamper-evident history of:
  - State changes
  - Control actions
  - Runtime metrics
- The system shall associate control actions and state changes with:
  - User identity
  - Device identity
  - Timestamp
- The system shall allow asset owners or managers to audit historical usage
- The system shall support dispute resolution by providing evidence of:
  - Misuse
  - Unauthorized control
  - Excessive or abnormal usage patterns
- The system shall retain audit and usage records for a configurable retention period

---

## 3. Assets Supported

The system shall support monitoring and control of the following asset types:
1. Boreholes
2. Water pumps
3. Water storage tanks
4. Power generators
5. Solar systems
6. Irrigation systems
7. Greenhouses

Each asset may consist of:
- One or more devices
- Multiple sensors
- Optional actuators

---

## 4. System Requirements

### 4.1 General System Requirements

- The system shall be cloud-hosted
- The system shall be accessible via a mobile application
- The system shall support multiple users and tenants
- The system shall allow real-time and historical data access

---

## 5. User Functional Requirements

### 5.1 Account & Access

- Users shall be able to create and manage accounts
- Users shall be able to manage assets they own or are assigned to
- The system shall support role-based access control

---

### 5.2 Asset Monitoring

- Users shall be able to view the real-time state of assets
- Users shall be able to view:
  - Sensor readings
  - Device health
  - Current operational state
- Users shall be able to group assets logically (e.g. by farm, property, cluster)

---

### 5.3 Remote Control

- Users shall be able to change the desired state of an asset remotely
- The system shall send control commands securely to field devices
- Users shall be able to see confirmation of executed commands

---

### 5.4 Historical Data & Insights

- Users shall be able to view historical states and state changes
- The system shall store time-series data for:
  - Sensor readings
  - State transitions
  - Runtime metrics
- Users shall be able to visualize trends over time
- The system shall support basic analytics (e.g. usage patterns, runtime totals)

---

### 5.5 Alerts & Notifications

- Users shall be able to define alert conditions
- The system shall notify users in real time when:
  - Undesirable states occur
  - Thresholds are exceeded
- Alerts shall be delivered via:
  - Mobile notifications (minimum requirement)

---

## 6. Device Functional Requirements

### 6.1 Connectivity

- Devices shall be able to connect to the cloud over IP networks
- Devices shall support secure, authenticated communication
- Devices shall be able to operate with intermittent connectivity

---

### 6.2 Telemetry

- Devices shall publish:
  - Sensor data
  - Device health status
  - Operational state
- Telemetry shall be timestamped
- Telemetry shall support configurable reporting intervals

---

### 6.3 Command & Control

- Devices shall receive control commands from the cloud
- Devices shall acknowledge command execution
- Devices shall report resulting state changes

---

### 6.4 Device Configuration

- Devices shall support remote configuration updates
- Devices shall persist configuration locally
- Devices shall recover gracefully after power or network loss

---

## 7. Non-Functional Requirements

### 7.1 Availability & Reliability

- The system shall be available 24/7
- The system shall tolerate intermittent device connectivity
- No data loss shall occur due to temporary network outages

---

### 7.2 Scalability

- The system shall scale to:
  - Thousands of devices
  - Millions of telemetry messages per day
- The system shall support multi-tenant isolation

---

### 7.3 Performance

- Real-time telemetry updates shall be visible within acceptable latency
- Control commands shall be delivered with low latency
- Historical queries shall be optimized for time-based access

---

### 7.4 Security

- All communication shall be encrypted
- Devices shall authenticate uniquely
- Users shall authenticate securely
- Access to assets shall be authorization-controlled
- All control actions shall be auditable

---

### 7.5 Maintainability

- The system shall be modular and extensible
- New asset types shall be addable without major redesign
- Monitoring and logging shall be available for operators
- Should have high observability

---

### 7.6 Usability

- The mobile application shall be intuitive and simple
- The system shall prioritize low cognitive load for non-technical users
- Critical states and alerts shall be clearly visible

---

## 8. Assumptions & Constraints

- Devices may operate in low-bandwidth environments
- Power availability in the field may be unreliable
- The system must be suitable for deployment in resource-constrained regions