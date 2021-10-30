package com.georgopoulosioannis.bluetooth_events;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

/** BluetoothEventsPlugin */
public class BluetoothEventsPlugin implements FlutterPlugin, MethodCallHandler {
  public static final String SHARED_PREFS_KEY="SHARED_PREFS_KEY";
  public static final String CALLBACK_HANDLE_KEY = "CALLBACK_HANDLE_KEY";
  public static final String CALLBACK_DISPATCHER_HANDLE_KEY = "CALLBACK_DISPATCH_HANDLE_KEY";
  public static final String DEVICE_NAME="DEVICE_NAME";
  public static final String DEVICE_ADDRESS="DEVICE_ADDRESS";

  private static final String TAG = "TAG";
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity

  private MethodChannel channel;
  private Context mContext;


  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    mContext = flutterPluginBinding.getApplicationContext();
    channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "bluetooth_events");
    channel.setMethodCallHandler(this);
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {

    SharedPreferences prefs = mContext.getSharedPreferences(
            SHARED_PREFS_KEY,
            Context.MODE_PRIVATE);
    if (call.method.equals("BluetoothEvents.initializeService")) {
      ArrayList args = call.arguments();
      long callBackDispatcherHandle = (long) args.get(0);
      BluetoothService.setCallbackDispatcher(mContext, callBackDispatcherHandle);
      BluetoothService.startBackgroundIsolate(mContext, callBackDispatcherHandle);
      result.success(null);
      return;
    } else if(call.method.equals("setEventCallback")){
      ArrayList args = call.arguments();
      long callbackHandle = (long) args.get(0);
      prefs.edit().putLong(CALLBACK_HANDLE_KEY,callbackHandle).apply();
      result.success(null);
      return;
    }else{
      result.notImplemented();
    }
  }


  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    channel.setMethodCallHandler(null);
  }
}
