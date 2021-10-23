import 'dart:async';
import 'dart:ui';

import 'package:flutter/cupertino.dart';
import 'package:flutter/services.dart';

class BluetoothEvents {
  static const MethodChannel _channel = MethodChannel('bluetooth_events');

  static Future<void> initialize() async {
    final callback = PluginUtilities.getCallbackHandle(callbackDispatcher);
    var raw = callback!.toRawHandle();
    await _channel.invokeMethod(
      'initializeService',
      <dynamic>[raw],
    );
  }

  static void test(void Function(String s) callback) async {
    try {
      var raw = PluginUtilities.getCallbackHandle(callback)!.toRawHandle();
      await _channel.invokeMethod('run', [raw]);
    } catch (e) {
      print(e);
    }
  }
}

void callbackDispatcher() {
  const MethodChannel _backgroundChannel =
      MethodChannel('bluetooth_events_background');

  WidgetsFlutterBinding.ensureInitialized();

  _backgroundChannel.setMethodCallHandler((MethodCall call) async {
    final args = call.arguments;

    final Function? callback = PluginUtilities.getCallbackFromHandle(
        CallbackHandle.fromRawHandle(args[0]));

    assert(callback != null);
    String s = args[1] as String;
    callback!(s);
  });
}
