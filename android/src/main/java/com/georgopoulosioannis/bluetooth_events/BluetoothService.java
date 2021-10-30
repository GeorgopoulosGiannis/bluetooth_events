package com.georgopoulosioannis.bluetooth_events;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;


import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;



public class BluetoothService extends JobIntentService {
    private static final String TAG = "AlarmService";
    private static final int JOB_ID = 1984; // Random job ID.

    private static final List<Intent> bluetoothQueue = Collections.synchronizedList(new LinkedList<>());

    /** Background Dart execution context. */
    private static FlutterBackgroundExecutor flutterBackgroundExecutor;

    /** Schedule the bluetooth to be handled by the {@link BluetoothService}. */
    public static void enqueueBluetoothProcessing(Context context, Intent bluetoothContext) {
        enqueueWork(context, BluetoothService.class, JOB_ID, bluetoothContext);
    }

    /**
     * Starts the background isolate for the {@link BluetoothService}.
     *
     * <p>Preconditions:
     *
     * <ul>
     *   <li>The given {@code callbackHandle} must correspond to a registered Dart callback. If the
     *       handle does not resolve to a Dart callback then this method does nothing.
     * </ul>
     */
    public static void startBackgroundIsolate(Context context, long callbackHandle) {
        if (flutterBackgroundExecutor != null) {
            Log.w(TAG, "Attempted to start a duplicate background isolate. Returning...");
            return;
        }
        flutterBackgroundExecutor = new FlutterBackgroundExecutor();
        flutterBackgroundExecutor.startBackgroundIsolate(context, callbackHandle);
    }

    /**
     * Called once the Dart isolate ({@code flutterBackgroundExecutor}) has finished initializing.
     *
     * <p>Invoked by {@link BluetoothEventsPlugin} when it receives the {@code
     * BluetoothEvents.startInitialize} message. Processes all alarm events that came in while the isolate
     * was starting.
     */
    /* package */ static void onInitialized() {
        Log.i(TAG, "BluetoothService started!");
        synchronized (bluetoothQueue) {
            // Handle all the alarm events received before the Dart isolate was
            // initialized, then clear the queue.
            for (Intent intent : bluetoothQueue) {
                flutterBackgroundExecutor.executeDartCallbackInBackgroundIsolate(intent, null);
            }
            bluetoothQueue.clear();
        }
    }

    /**
     * Sets the Dart callback handle for the Dart method that is responsible for initializing the
     * background Dart isolate, preparing it to receive Dart callback tasks requests.
     */
    public static void setCallbackDispatcher(Context context, long callbackHandle) {
        FlutterBackgroundExecutor.setCallbackDispatcher(context, callbackHandle);
    }




    @Override
    public void onCreate() {
        super.onCreate();
        if (flutterBackgroundExecutor == null) {
            flutterBackgroundExecutor = new FlutterBackgroundExecutor();
        }
        Context context = getApplicationContext();
        flutterBackgroundExecutor.startBackgroundIsolate(context);
    }

    /**
     * Executes a Dart callback, as specified within the incoming {@code intent}.
     *
     * <p>Invoked by our {@link JobIntentService} superclass after a call to {@link
     * JobIntentService#enqueueWork(Context, Class, int, Intent);}.
     *
     * <p>If there are no pre-existing callback execution requests, other than the incoming {@code
     * intent}, then the desired Dart callback is invoked immediately.
     *
     * <p>If there are any pre-existing callback requests that have yet to be executed, the incoming
     * {@code intent} is added to the {@link #bluetoothQueue} to invoked later, after all pre-existing
     * callbacks have been executed.
     */
    @Override
    protected void onHandleWork(@NonNull final Intent intent) {
        // If we're in the middle of processing queued alarms, add the incoming
        // intent to the queue and return.
        synchronized (bluetoothQueue) {
            if (!flutterBackgroundExecutor.isRunning()) {
                Log.i(TAG, "BluetoothService has not yet started.");
                bluetoothQueue.add(intent);
                return;
            }
        }

        // There were no pre-existing callback requests. Execute the callback
        // specified by the incoming intent.
        final CountDownLatch latch = new CountDownLatch(1);
        new Handler(getMainLooper())
                .post(
                        () -> flutterBackgroundExecutor.executeDartCallbackInBackgroundIsolate(intent, latch));

        try {
            latch.await();
        } catch (InterruptedException ex) {
            Log.i(TAG, "Exception waiting to execute Dart callback", ex);
        }
    }

}
