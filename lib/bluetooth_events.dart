import 'dart:async';
import 'dart:ui';

import 'package:flutter/cupertino.dart';
import 'package:flutter/services.dart';

class BluetoothEvents {
  static const MethodChannel _channel = MethodChannel('bluetooth_events');

  static Future<void> initialize() async {
    final callbackDispatcherHandle =
        PluginUtilities.getCallbackHandle(callbackDispatcher);
    var rawDispatcherHandle = callbackDispatcherHandle!.toRawHandle();

    await _channel.invokeMethod(
      'BluetoothEvents.initializeService',
      <dynamic>[rawDispatcherHandle],
    );
  }

  static Future<void> setBluetoothEventCallback(
    void Function(Map<String, dynamic> args) callback,
  ) async {
    try {
      final callbackHandle = PluginUtilities.getCallbackHandle(callback);
      var raw = callbackHandle!.toRawHandle();
      await _channel.invokeMethod('setEventCallback', [raw]);
    } catch (e) {
      print(e);
    }
  }

  static Future<Map<String, dynamic>> getBondedDevices() async {
    final devices = await _channel.invokeMethod<Map>('getPairedDevices');
    return devices?.cast<String, dynamic>() ?? {};
  }

  static Future<Map<String, dynamic>> getConnectedDevices() async {
    final devices = await _channel.invokeMethod<Map>('getConnectedDevices');
    return devices?.cast<String, dynamic>() ?? {};
  }
}

void callbackDispatcher() {
  const MethodChannel _backgroundChannel =
      MethodChannel('bluetooth_events_background');

  WidgetsFlutterBinding.ensureInitialized();

  _backgroundChannel.setMethodCallHandler((MethodCall call) async {
    final args = call.arguments;

    final Function? callback = PluginUtilities.getCallbackFromHandle(
        CallbackHandle.fromRawHandle(args['callbackHandle']));

    assert(callback != null);
    args.remove('callbackHandle');

    callback!(args.cast<String, dynamic>());
    return 'success';
  });
  _backgroundChannel.invokeMethod<void>('BluetoothService.initialized');
}
