package com.georgopoulosioannis.bluetooth_events;

import static com.georgopoulosioannis.bluetooth_events.BluetoothEventsPlugin.CALLBACK_DISPATCHER_HANDLE_KEY;
import static com.georgopoulosioannis.bluetooth_events.BluetoothEventsPlugin.CALLBACK_HANDLE_KEY;
import static com.georgopoulosioannis.bluetooth_events.BluetoothEventsPlugin.DEVICE_ADDRESS;
import static com.georgopoulosioannis.bluetooth_events.BluetoothEventsPlugin.DEVICE_NAME;
import static com.georgopoulosioannis.bluetooth_events.BluetoothEventsPlugin.SHARED_PREFS_KEY;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.HashMap;
import java.util.Map;

import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.embedding.engine.dart.DartExecutor;
import io.flutter.embedding.engine.loader.FlutterLoader;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.view.FlutterCallbackInformation;

public class BluetoothWorker extends Worker {

    public BluetoothWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    private FlutterLoader flutterLoader = new FlutterLoader();

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        Data data = getInputData();

        SharedPreferences prefs = context.getSharedPreferences(
                SHARED_PREFS_KEY,
                Context.MODE_PRIVATE);
        long callbackDispatcherHandle = prefs.getLong(CALLBACK_DISPATCHER_HANDLE_KEY, 0);
        long callbackHandle = prefs.getLong(CALLBACK_HANDLE_KEY, 0);
        data.getLong(CALLBACK_HANDLE_KEY, 0);
        FlutterEngine engine = new FlutterEngine(context);
        if (!flutterLoader.initialized()) {
            flutterLoader.startInitialization(context);
        }

        flutterLoader.ensureInitializationCompleteAsync(context, null, new Handler(Looper.getMainLooper()), () -> {

            FlutterCallbackInformation flutterCallbackInformation =
                    FlutterCallbackInformation.lookupCallbackInformation(callbackDispatcherHandle);

            MethodChannel mBackgroundChannel = new MethodChannel(engine.getDartExecutor(), "bluetooth_events_background");

            engine.getDartExecutor().executeDartCallback(new DartExecutor
                    .DartCallback(context.getAssets(), flutterLoader.findAppBundlePath(), flutterCallbackInformation));
            final Map<String, Object> m = new HashMap<String, Object>();
            m.put("callbackHandle", callbackHandle);
            m.put(DEVICE_NAME, data.getString(DEVICE_NAME));
            m.put(DEVICE_ADDRESS, data.getString(DEVICE_ADDRESS));
            mBackgroundChannel.invokeMethod("", m);
        });

        return Result.success();

    }
}
