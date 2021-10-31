import 'package:flutter/material.dart';
import 'dart:async';

import 'package:bluetooth_events/bluetooth_events.dart';

import 'local_notification_srv.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  Map<String, dynamic> bondedDevices = {};
  Map<String, dynamic> connectedDevices = {};
  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    await LocalNotificationSrv.initialize();
    await BluetoothEvents.initialize();
    await BluetoothEvents.setBluetoothEventCallback(bluetoothCallback);
  }

  static void bluetoothCallback(dynamic args) {
    Future.delayed(Duration(seconds: 2)).then(
      (value) => LocalNotificationSrv.showNotification(),
    );
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Flutter Example'),
        ),
        body: Container(
          padding: const EdgeInsets.all(20.0),
          child: Column(
            children: <Widget>[
              RaisedButton(
                child: const Text('bonded devices'),
                onPressed: () async {
                  try {
                    bondedDevices = await BluetoothEvents.getBondedDevices();
                    setState(() {});
                  } catch (e) {}

                  print(bondedDevices);
                },
              ),
              Flexible(
                child: ListView.builder(
                  itemCount: bondedDevices.length,
                  itemBuilder: (context, index) {
                    final curDev = Map<String, dynamic>.from(
                        bondedDevices.values.elementAt(index));
                    final sub = curDev.entries.fold(
                        ' address : ${bondedDevices.keys.elementAt(index)}',
                        (previousValue, element) =>
                            '$previousValue \n ${element.key} : ${element.value}');
                    return ListTile(
                      title: Text(curDev['name']),
                      subtitle: Text(sub),
                    );
                  },
                ),
              ),
              RaisedButton(
                child: const Text('connected devices'),
                onPressed: () async {
                  connectedDevices =
                      await BluetoothEvents.getConnectedDevices();
                  print(connectedDevices);
                },
              ),
              if (connectedDevices.isNotEmpty)
                Flexible(
                  child: ListView.builder(
                    itemCount: connectedDevices.length,
                    itemBuilder: (context, index) {
                      final curDev = Map<String, dynamic>.from(
                          connectedDevices.values.elementAt(index));
                      final sub = curDev.entries.fold(
                          ' address : ${connectedDevices.keys.elementAt(index)}',
                          (previousValue, element) =>
                              '$previousValue \n ${element.key} : ${element.value}');
                      return ListTile(
                        title: Text(curDev['name']),
                        subtitle: Text(sub),
                      );
                    },
                  ),
                ),
            ],
          ),
        ),
      ),
    );
  }
}
