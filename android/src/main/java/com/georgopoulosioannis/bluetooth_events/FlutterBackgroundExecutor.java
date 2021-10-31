package com.georgopoulosioannis.bluetooth_events;

import static com.georgopoulosioannis.bluetooth_events.BluetoothEventsPlugin.DEVICE_ADDRESS;
import static com.georgopoulosioannis.bluetooth_events.BluetoothEventsPlugin.DEVICE_NAME;
import static com.georgopoulosioannis.bluetooth_events.BluetoothEventsPlugin.SHARED_PREFS_KEY;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import io.flutter.FlutterInjector;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.embedding.engine.dart.DartExecutor;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.view.FlutterCallbackInformation;

/**
 * An background execution abstraction which handles initializing a background isolate running a
 * callback dispatcher, used to invoke Dart callbacks while backgrounded.
 */

public class FlutterBackgroundExecutor implements MethodCallHandler {

    private static final String TAG = "FlutterBackgroundExecutor";

    /**
     * The {@link MethodChannel} that connects the Android side of this plugin with the background
     * Dart isolate that was created by this plugin.
     */
    private MethodChannel backgroundChannel;

    private FlutterEngine backgroundFlutterEngine;

    private final AtomicBoolean isCallbackDispatcherReady = new AtomicBoolean(false);


    /**
     * Sets the Dart callback handle for the Dart method that is responsible for initializing the
     * background Dart isolate, preparing it to receive Dart callback tasks requests.
     */
    public static void setCallbackDispatcher(Context context, long callbackDispatcherHandle) {
        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFS_KEY, 0);
        prefs.edit().putLong(BluetoothEventsPlugin.CALLBACK_DISPATCHER_HANDLE_KEY, callbackDispatcherHandle).apply();
    }

    /** Returns true when the background isolate has started and is ready to handle bluetooth events. */
    public boolean isRunning() {
        return isCallbackDispatcherReady.get();
    }

    private void onInitialized() {
        isCallbackDispatcherReady.set(true);
        BluetoothService.onInitialized();
    }

    @Override
    public void onMethodCall(MethodCall call, MethodChannel.Result result) {
        String method = call.method;
        Object arguments = call.arguments;
        try {
            if (method.equals("BluetoothService.initialized")) {
                // This message is sent by the background method channel as soon as the background isolate
                // is running. From this point forward, the Android side of this plugin can send
                // callback handles through the background method channel, and the Dart side will execute
                // the Dart methods corresponding to those callback handles.
                onInitialized();
                result.success(true);
            } else if(method.equals("BluetoothService.done")){

            }else {
                result.notImplemented();
            }
        } catch (Exception e ) {
            result.error("error", "BluetoothEvents error: " + e.getMessage(), null);
        }
    }

    /**
     * Starts running a background Dart isolate within a new {@link FlutterEngine} using a previously
     * used entrypoint.
     *
     * <p>The isolate is configured as follows:
     *
     * <ul>
     *   <li>Bundle Path: {@code FlutterMain.findAppBundlePath(context)}.
     *   <li>Entrypoint: The Dart method used the last time this plugin was initialized in the
     *       foreground.
     *   <li>Run args: none.
     * </ul>
     *
     * <p>Preconditions:
     *
     * <ul>
     *   <li>The given callback must correspond to a registered Dart callback. If the handle does not
     *       resolve to a Dart callback then this method does nothing.
     * </ul>
     */
    public void startBackgroundIsolate(Context context) {
        if (!isRunning()) {
            SharedPreferences p = context.getSharedPreferences(SHARED_PREFS_KEY, 0);
            long callbackHandle = p.getLong(BluetoothEventsPlugin.CALLBACK_DISPATCHER_HANDLE_KEY, 0);
            startBackgroundIsolate(context, callbackHandle);
        }
    }

    /**
     * Starts running a background Dart isolate within a new {@link FlutterEngine}.
     *
     * <p>The isolate is configured as follows:
     *
     * <ul>
     *   <li>Bundle Path: {@code FlutterMain.findAppBundlePath(context)}.
     *   <li>Entrypoint: The Dart method represented by {@code callbackHandle}.
     *   <li>Run args: none.
     * </ul>
     *
     * <p>Preconditions:
     *
     * <ul>
     *   <li>The given {@code callbackHandle} must correspond to a registered Dart callback. If the
     *       handle does not resolve to a Dart callback then this method does nothing.
     */
    @SuppressLint("LongLogTag")
    public void startBackgroundIsolate(Context context, long callbackDispatcherHandle) {
        if (backgroundFlutterEngine != null) {
            Log.e(TAG, "Background isolate already started");
            return;
        }

        Log.i(TAG, "Starting BluetoothService...");
        String appBundlePath = FlutterInjector.instance().flutterLoader().findAppBundlePath();
        AssetManager assets = context.getAssets();
        if (appBundlePath != null && !isRunning()) {
            backgroundFlutterEngine = new FlutterEngine(context);

            // We need to create an instance of `FlutterEngine` before looking up the
            // callback. If we don't, the callback cache won't be initialized and the
            // lookup will fail.
            FlutterCallbackInformation flutterCallback =
                    FlutterCallbackInformation.lookupCallbackInformation(callbackDispatcherHandle);
            if (flutterCallback == null) {
                Log.e(TAG, "Fatal: failed to find callback");
                return;
            }

            DartExecutor executor = backgroundFlutterEngine.getDartExecutor();
            initializeMethodChannel(executor);
            DartExecutor.DartCallback dartCallback = new DartExecutor.DartCallback(assets, appBundlePath, flutterCallback);

            executor.executeDartCallback(dartCallback);
        }
    }

    /**
     * Executes the desired Dart callback in a background Dart isolate.
     *
     * <p>The given {@code intent} should contain a {@code long} extra called "callbackHandle", which
     * corresponds to a callback registered with the Dart VM.
     */
    public void executeDartCallbackInBackgroundIsolate(Intent intent, final CountDownLatch latch) {
        // Grab the handle for the callback. Pay close
        // attention to the type of the callback handle as storing this value in a
        // variable of the wrong size will cause the callback lookup to fail.

        long callbackHandle = intent.getLongExtra("callbackHandle", 0);

        // If another thread is waiting, then wake that thread when the callback returns a result.
        MethodChannel.Result result = null;

       if (latch != null) {
            result =
                    new MethodChannel.Result() {
                        @Override
                        public void success(Object result) {
                            latch.countDown();
                        }

                        @Override
                        public void error(String errorCode, String errorMessage, Object errorDetails) {
                            latch.countDown();
                        }

                        @Override
                        public void notImplemented() {
                            latch.countDown();
                        }
                    };
        }


        final Map<String,Object> m = new HashMap<String,Object>();
        m.put("callbackHandle",callbackHandle);
        m.put(DEVICE_NAME,intent.getStringExtra(DEVICE_NAME));
        m.put(DEVICE_ADDRESS,intent.getStringExtra(DEVICE_ADDRESS));
        backgroundChannel.invokeMethod("",m,result);
    }

    private void initializeMethodChannel(BinaryMessenger isolate) {
        // backgroundChannel is the channel responsible for receiving the following messages from
        // the background isolate that was setup by this plugin:
        // - "BluetoothService.initialized"
        //
        // This channel is also responsible for sending requests from Android to Dart to execute Dart
        // callbacks in the background isolate.
        backgroundChannel =
                new MethodChannel(
                        isolate,
                        "bluetooth_events_background");
        backgroundChannel.setMethodCallHandler(this);
    }
}
