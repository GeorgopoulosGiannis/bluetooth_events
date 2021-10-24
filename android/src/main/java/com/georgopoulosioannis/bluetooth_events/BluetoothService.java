package com.georgopoulosioannis.bluetooth_events;






import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import static com.georgopoulosioannis.bluetooth_events.BluetoothEventsPlugin.CALLBACK_DISPATCHER_HANDLE_KEY;
import static com.georgopoulosioannis.bluetooth_events.BluetoothEventsPlugin.CALLBACK_HANDLE_KEY;
import static com.georgopoulosioannis.bluetooth_events.BluetoothEventsPlugin.DEVICE_ADDRESS;
import static com.georgopoulosioannis.bluetooth_events.BluetoothEventsPlugin.DEVICE_NAME;
import static com.georgopoulosioannis.bluetooth_events.BluetoothEventsPlugin.SHARED_PREFS_KEY;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.flutter.embedding.android.FlutterView;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.embedding.engine.dart.DartExecutor;
import io.flutter.embedding.engine.loader.FlutterLoader;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.view.FlutterCallbackInformation;
import io.flutter.view.FlutterMain;
import io.flutter.view.FlutterNativeView;
import io.flutter.view.FlutterRunArguments;

public class BluetoothService extends Service {

    private FlutterLoader flutterLoader = new FlutterLoader();
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        SharedPreferences prefs = this.getSharedPreferences(
                SHARED_PREFS_KEY,
                Context.MODE_PRIVATE);
        long callbackDispatcherHandle = prefs.getLong(CALLBACK_DISPATCHER_HANDLE_KEY,0);
        long callbackHandle = prefs.getLong(CALLBACK_HANDLE_KEY,0); intent.getLongExtra(CALLBACK_HANDLE_KEY, 0);
        if(!flutterLoader.initialized()){
            flutterLoader.startInitialization(this);
        }

        flutterLoader.ensureInitializationCompleteAsync(this,null,new Handler(Looper.getMainLooper()),()->{
            FlutterEngine engine = new FlutterEngine(this);
            FlutterCallbackInformation flutterCallbackInformation =
                    FlutterCallbackInformation.lookupCallbackInformation(callbackDispatcherHandle);
            MethodChannel mBackgroundChannel = new MethodChannel(engine.getDartExecutor(), "bluetooth_events_background");

            engine.getDartExecutor().executeDartCallback(new DartExecutor.DartCallback(this.getAssets(),flutterLoader.findAppBundlePath(),flutterCallbackInformation));

            final Map<String,Object> m = new HashMap<String,Object>();
            m.put("callbackHandle",callbackHandle);
            m.put(DEVICE_NAME,intent.getStringExtra(DEVICE_NAME));
            m.put(DEVICE_ADDRESS,intent.getStringExtra(DEVICE_ADDRESS));
           /* final ArrayList<Object> l = new ArrayList<Object>();

            l.add(callbackHandle);
            l.add(intent.getStringExtra(DEVICE_NAME));
            l.add(intent.getStringExtra(DEVICE_ADDRESS));*/
            mBackgroundChannel.invokeMethod("",m);
        });




        return START_STICKY;
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}