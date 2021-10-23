package com.georgopoulosioannis.bluetooth_events;






import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import static com.georgopoulosioannis.bluetooth_events.BluetoothEventsPlugin.CALLBACK_DISPATCHER_HANDLE_KEY;
import static com.georgopoulosioannis.bluetooth_events.BluetoothEventsPlugin.CALLBACK_HANDLE_KEY;

import java.util.ArrayList;

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

        long callbackDispatcherHandle = intent.getLongExtra(CALLBACK_DISPATCHER_HANDLE_KEY, 0);

        FlutterCallbackInformation flutterCallbackInformation =
                FlutterCallbackInformation.lookupCallbackInformation(callbackDispatcherHandle);



        FlutterEngine engine = new FlutterEngine(this);
        if(!flutterLoader.initialized()){
            flutterLoader.startInitialization(this);
        }

        flutterLoader.ensureInitializationCompleteAsync(this,null,new Handler(Looper.getMainLooper()),()->{
            MethodChannel mBackgroundChannel = new MethodChannel(engine.getDartExecutor(), "bluetooth_events_background");
            engine.getDartExecutor().executeDartCallback(new DartExecutor.DartCallback(this.getAssets(),flutterLoader.findAppBundlePath(),flutterCallbackInformation));
            long callbackHandle = intent.getLongExtra(CALLBACK_HANDLE_KEY, 0);
            final ArrayList<Object> l = new ArrayList<Object>();

            l.add(callbackHandle);
            l.add("Hello, I am transferred from java to dart world");
            mBackgroundChannel.invokeMethod("",l);
        });




        return START_STICKY;
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}