package com.georgopoulosioannis.bluetooth_events;

import static android.bluetooth.BluetoothProfile.GATT;
import static android.bluetooth.BluetoothProfile.GATT_SERVER;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Build;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BluetoothManagerWrapper {
    private Context mContext;
    private BluetoothAdapter adapter;
    private BluetoothManager manager;

    public BluetoothManagerWrapper(Context context){
        mContext =context;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            manager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
            adapter = manager.getAdapter();
        }else{
            adapter = BluetoothAdapter.getDefaultAdapter();
        }
    }
    public Map<String,Map<String,Object>> getPairedDevices(){
        Set<BluetoothDevice> devices = adapter.getBondedDevices();
        return mapListOfDevices(devices);
    }

    public Map<String,Map<String,Object>> getConnectedDevices(){
        List<BluetoothDevice> devices = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            devices = manager.getConnectedDevices(GATT);
        }
        return mapListOfDevices(devices);
    }

    private Map<String,Map<String,Object>> mapListOfDevices(Collection<BluetoothDevice> devices){
        Map<String,Map<String,Object>> result = new HashMap<>();

        for(BluetoothDevice d : devices) {
            Map<String,Object> val = new HashMap<>();
            val.put("name",d.getName());
            val.put("bondState",d.getBondState());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val.put("alias",d.getAlias());
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                val.put("type",d.getType());
            }
            result.put(d.getAddress(),val);
        }
        return result;
    }
}
