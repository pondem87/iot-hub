# Overview

- The IoT Hub is a cloud service meant to meant to collect device telemetry data and send commands to devices via an IoT gateway.
- Rather that using a simple smart switch, which doesnt scale well when you have a number of devices working together in a system, IoT Hub allows you to manage a system of devices and associated data.

# Functional requirements
So what is it that the users must be able to accomplish with this system?

### Users
- users must be able to create and manage their profiles
- user must be able to authenticate and securely access resources based on resource access controls

### Organisations
- Assets are scoped to organisations.
- An organisation is the top level grouping of devices, sensors and actuators.
- An organisation will have multiple sites and within each site there will be assets which in turn contain sensors and actuators.
- Data and commands will be transmitted via gateways (IoT Gateways).
- An organisation will be owned by a superuser who in turn can invite other users to have access to the organisation and its assets with customisable access controls.
- A single user will be able to create one or more organisations depending on usage tier.

### Sites, gateway, assets, sensors and assets
- organisations decompose into sites, assets, sensors and actuators.
- gateways will manage transmission of data to and from sensors and actuators.
- users must be able to create and link these objects according to access level at organisation and object level.

### Gateway
- a gateway represents the physical iot gateway and manages transmission of sensor data and actuator commands.
- every sensor and actuator must be linked to a gateway to be able to transmit and receive data.
- the user must be able to configure the gateway and obtain device certificates for authenticating the physical iot gateway.

### Assets
- An asset represents a physical piece of equipment being monitored and controlled.
- for example a water tank is an asset which may have a water level sensor.

### Sensors and actuators
- sensors and actuators collect data and accept control commands respectively.

### Thresholds and actions
- sensors can have threshold values which will trigger actions when reached by the sensor data.
- actions trigger changes in actuator states resulting in command sent to the actuator.


# Non-functional requirements

### Security
- gateways must securely connect to the mqtt server through mutual TLS
- topic access policy must be determined after confirming the gateway identity
- a mechanism of invalidating certificates must be provided

### Accessibility
- there will be three platforms available to access the service: mobile app, web application and whatsapp chatbot

### Reliability
- the cloud service should have high availability
- iot gateways with store and send are preferred and advised to end users

### Usability
- the interface should use simple language understandable by non-technical people