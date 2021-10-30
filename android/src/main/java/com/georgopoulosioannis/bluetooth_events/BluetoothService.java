package com.georgopoulosioannis.bluetooth_events;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.JobIntentService;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class BluetoothService extends Service {
    private static String CHANNEL_ID = "channel_id";
    private static String CHANNEL_NAME = "CHANNEL_NAME";


    private static final String TAG = "AlarmService";
    private static final int JOB_ID = 1984; // Random job ID.

    private static final List<Intent> bluetoothQueue = Collections.synchronizedList(new LinkedList<>());

    /** Background Dart execution context. */
    private static FlutterBackgroundExecutor flutterBackgroundExecutor;


    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Test descritpion");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

            Intent notificationIntent = new Intent(this, BluetoothService.class);
            PendingIntent pendingIntent =
                    PendingIntent.getActivity(this, 0, notificationIntent, 0);
        Notification notification = new Notification.Builder(this,CHANNEL_ID )
                    .setContentTitle("titltitle")
                    .setContentText("content text content text")
                    .setContentIntent(pendingIntent)
                    .setTicker("ticker ticker ticker")
                    .build();
            startForeground(1,notification);

        }
        doWork(intent);
        return super.onStartCommand(intent, flags, startId);
    }
    private void doWork( Intent intent){
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
        /*new Handler(getMainLooper())
                .post(
                        () -> flutterBackgroundExecutor.executeDartCallbackInBackgroundIsolate(intent, latch)); */
        flutterBackgroundExecutor.executeDartCallbackInBackgroundIsolate(intent, latch);
        try {
            stopSelf();
            latch.await();

        } catch (InterruptedException ex) {
            Log.i(TAG, "Exception waiting to execute Dart callback", ex);
        }


    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
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

}
