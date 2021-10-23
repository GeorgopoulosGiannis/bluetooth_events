package com.georgopoulosioannis.bluetooth_events;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

/** BluetoothEventsPlugin */
public class BluetoothEventsPlugin implements FlutterPlugin, MethodCallHandler {
  public static final String CALLBACK_HANDLE_KEY = "CALLBACK_HANDLE_KEY";
  public static final String CALLBACK_DISPATCHER_HANDLE_KEY = "CALLBACK_DISPATCH_HANDLE_KEY";
  private static final String TAG = "TAG";
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity

  private MethodChannel channel;
  private Context mContext;


  private long mCallbackDispatcherHandle;

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    mContext = flutterPluginBinding.getApplicationContext();
    channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "bluetooth_events");
    channel.setMethodCallHandler(this);
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {

    if (call.method.equals("initializeService")) {
      ArrayList args = call.arguments();
      long callBackHandle = (long) args.get(0);
      mCallbackDispatcherHandle = callBackHandle;

      result.success(null);
      return;
    } else if(call.method.equals("run")){

      ArrayList args = call.arguments();
      long callbackHandle = (long) args.get(0);

      Intent i = new Intent(mContext, BluetoothService.class);
      i.putExtra(CALLBACK_HANDLE_KEY, callbackHandle);
      i.putExtra(CALLBACK_DISPATCHER_HANDLE_KEY, mCallbackDispatcherHandle);
      mContext.startService(i);

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
