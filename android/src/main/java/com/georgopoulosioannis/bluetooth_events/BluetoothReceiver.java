package com.georgopoulosioannis.bluetooth_events;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import io.flutter.embedding.engine.loader.FlutterLoader;

public class BluetoothReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        FlutterLoader loader = new FlutterLoader();
        loader.startInitialization(context);
        loader.ensureInitializationComplete(context,null);


        String action = intent.getAction();
        if(action.equals(BluetoothDevice.ACTION_FOUND)) {
            Toast.makeText(context, "BT found", Toast.LENGTH_SHORT).show();
        } else if(action.equals(BluetoothDevice.ACTION_ACL_CONNECTED)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            String deviceName =device.getName();

            Toast.makeText(context, String.format("Bluetooth connected: %s",deviceName), Toast.LENGTH_SHORT).show();

        } else if(action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
            Toast.makeText(context, "BT Disconnected", Toast.LENGTH_SHORT).show();
        } else
            Toast.makeText(context, "BT Disconnect requested", Toast.LENGTH_SHORT).show();

    }
}
