# Warp

Java-based application for beaming applications between devices.

Instead of having to transfer files and other information with other devices via third-party services (and in any case using the internet), this solution provides a much easier way to share the same data, taking advantage of the proximity of the devices.

The core framework allows any Java powered device to exchange data with other visible devices in the local network. 
The discovery of such nearby devices is automatic. 
Currently only IEEE 802.11 is supported for this purpose.
Devices can create direct connections between them and exchange data at high speeds, without having to use external servers of any kind. The solution pushes towards a pure single-hop P2P system.
Ultimately, each endpoint will work as a small service-container, where services can be added and/or removed, providing different E2E functionalities, such as file transfers, media streaming and more.

Dynamic class loading will be supported and will allow endpoints to share existing services and install them at runtime, allowing to perform any kind of operation.
A multicast functionality is planned as well.

The application was developed having Andrid as a target platform in mind, since it provides enough flexibility to support all the offered features.
On Android, communication between devices can be achieved while being in the same WiFi network, or creating ad-hoc networks using WiFi Direct.
