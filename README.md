# Detect Anomalies in Heart Rate
This application can communicate with Mi-band smart watch and collect sensor data. Then App can identify anomalies like sudden increse of heart beat while sleeping, very low heart beat etc ang give alert to user or related persons.

This application uses Mi band's following UUIDs for Services and Characteristic to communicate via BLE. Since there is no proper documentation on these UUIDs it needs some deep dig into BLE communication. Here are some of UUIDs used

**Basic Service:** 

UUID of Service: 0000fee0-0000-1000-8000-00805f9b34fb

Battery Info Characteristic: 00000006-0000-3512-2118-0009af100700

**Alert Service** 

UUID of Service: 00001802-0000-1000-8000-00805f9b34fb

New Alert Characteristic: 00002a06-0000-1000-8000-00805f9b34fb
 
**Heart Rate Service**

UUID of Service: 0000180d-0000-1000-8000-00805f9b34fb

Measurement Characteristic: 00002a37-0000-1000-8000-00805f9b34fb

Control Characteristic: 00002a39-0000-1000-8000-00805f9b34fb

Descriptor: 00002902-0000-1000-8000-00805f9b34fb

There's so many things you can explore about how MI Band device connect and communicate with your Android device using Bluetooth connection
