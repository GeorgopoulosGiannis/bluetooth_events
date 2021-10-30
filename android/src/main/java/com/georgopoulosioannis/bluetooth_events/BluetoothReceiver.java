package com.georgopoulosioannis.bluetooth_events;

import static com.georgopoulosioannis.bluetooth_events.BluetoothEventsPlugin.DEVICE_ADDRESS;
import static com.georgopoulosioannis.bluetooth_events.BluetoothEventsPlugin.DEVICE_NAME;
import static com.georgopoulosioannis.bluetooth_events.BluetoothEventsPlugin.SHARED_PREFS_KEY;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class BluetoothReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        String deviceName = device.getName();
        String deviceAddress = device.getAddress();


        if (action.equals(BluetoothDevice.ACTION_FOUND)) {

            Toast.makeText(context, "BT found", Toast.LENGTH_SHORT).show();
        } else if (action.equals(BluetoothDevice.ACTION_ACL_CONNECTED)) {
            Toast.makeText(context, String.format("Bluetooth connected: %s", deviceName), Toast.LENGTH_SHORT).show();
        } else if (action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
            Toast.makeText(context, "BT Disconnected", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "BT Disconnect requested", Toast.LENGTH_SHORT).show();
        }

  /*      Data inputData = new Data.Builder()
                .putString("ACTION", action)
                .putString(DEVICE_NAME, deviceName)
                .putString(DEVICE_ADDRESS, deviceAddress)
                .build();
        OneTimeWorkRequest workRq = new OneTimeWorkRequest
                .Builder(BluetoothWorker.class)
                .setInputData(inputData)
                .build();

        WorkManager.getInstance(context).enqueue(workRq); */

    /*    Intent i = new Intent(context, BluetoothService.class);
        i.putExtra("ACTION",action);
        i.putExtra(DEVICE_NAME, deviceName);
        i.putExtra(DEVICE_ADDRESS, deviceAddress);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(i);

        } else {
            context.startService(i);
        } */

        Intent i = new Intent(context, BluetoothService.class);
        i.putExtra("ACTION",action);
        i.putExtra(DEVICE_NAME, deviceName);
        i.putExtra(DEVICE_ADDRESS, deviceAddress);
        i.putExtra("callbackHandle",context.getSharedPreferences(SHARED_PREFS_KEY, 0).getLong(BluetoothEventsPlugin.CALLBACK_HANDLE_KEY, 0));
        BluetoothService.enqueueBluetoothProcessing(context,i);


    }
}
