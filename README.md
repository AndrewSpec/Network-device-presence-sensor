# Network device presence sensor for Hubitat
A virtual presence sensor for Hubitat that checks if an network device is present on the network.

- If this sensor shows "present", your device is returning http 200 status or a selected phrase.
- If this sensor shows "not present", the device is returing http status code other than 200 or it doesn't match your defined phrase

## Installation

Manual installation:

1. Open your Hubitat web page
2. Go to the "Drivers Code" page
3. Click "+ New Driver"
4. Paste in the contents of networkDevicePresenceSensor.groovy
5. Click "Save"
6. Go to the "Devices" page
7. Click "+ Add Virtual Device"
8. Set "Device Name" and "Device Network Id" to anything you like.  Set "Type" to "Network device presence sensor".
9. Click "Save Device"
10. On the device list, click the name of your new sensor
11. Set "IP Address" to the local static IP address of your device
12. Set protocol if other that http (like https)
13. Optionally set your search phrase that will be matched (the URL address returns that phrase)
14. Click "Save Preferences"
