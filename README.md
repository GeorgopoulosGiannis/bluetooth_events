# bluetooth_events

A Flutter plugin that allows you to execute dart callbacks even when your app is not running, when a bluetooth event is received
(e.g BluetoothDevice.ACTION_ACL_CONNECTED, BluetoothDevice.ACTION_ACL_DISCONNECTED).

A method to retrieve all bonded devices is also provided.

## Getting Started

Include package in pubspec.yaml and then call : 
```dart
    await BluetoothEvents.initialize();
    await BluetoothEvents.setBluetoothEventCallback(bluetoothCallback);
``` 
where bluetoothCallback is the callback which will be invoked whenever a bluetooth event is received.

TO retrieve all bonded devices :

```dart
    await BluetoothEvents.getBondedDevices();
```


### Example
The example provided in example/ folder uses LocalNotification package to trigger a notification when a device gets connected or disconnected via bluetooth.
