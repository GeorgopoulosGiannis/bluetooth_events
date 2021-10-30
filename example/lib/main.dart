import 'package:flutter/material.dart';
import 'dart:async';

import 'package:bluetooth_events/bluetooth_events.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    await BluetoothEvents.initialize();
    await BluetoothEvents.setBluetoothEventCallback(bluetoothCallback);
  }
  static void bluetoothCallback(dynamic args){
    print(args);
    
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
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: <Widget>[
                    Center(
                      child: RaisedButton(
                        child: const Text('Run'),
                        onPressed: () {},
                      ),
                    ),
                  ]))),
    );
  }
}
