package com.georgopoulosioannis.bluetooth_events;

import static com.georgopoulosioannis.bluetooth_events.BluetoothEventsPlugin.CALLBACK_DISPATCHER_HANDLE_KEY;
import static com.georgopoulosioannis.bluetooth_events.BluetoothEventsPlugin.CALLBACK_HANDLE_KEY;
import static com.georgopoulosioannis.bluetooth_events.BluetoothEventsPlugin.DEVICE_ADDRESS;
import static com.georgopoulosioannis.bluetooth_events.BluetoothEventsPlugin.DEVICE_NAME;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import io.flutter.embedding.engine.loader.FlutterLoader;

public class BluetoothReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        String deviceName =device.getName();
        String deviceAddress =device.getAddress();
        if(action.equals(BluetoothDevice.ACTION_FOUND)) {
            Toast.makeText(context, "BT found", Toast.LENGTH_SHORT).show();
        } else if(action.equals(BluetoothDevice.ACTION_ACL_CONNECTED)) {
            Toast.makeText(context, String.format("Bluetooth connected: %s",deviceName), Toast.LENGTH_SHORT).show();
        } else if(action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
            Toast.makeText(context, "BT Disconnected", Toast.LENGTH_SHORT).show();
        } else{
            Toast.makeText(context, "BT Disconnect requested", Toast.LENGTH_SHORT).show();
        }



        Intent i = new Intent(context, BluetoothService.class);
        i.putExtra(DEVICE_NAME, deviceName);
        i.putExtra(DEVICE_ADDRESS, deviceAddress);
        context.startService(i);
    }
}
