package com.color.inner.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothMap;
import android.bluetooth.BluetoothProfile;
import java.util.ArrayList;
import java.util.List;

public class BluetoothMapWrapper {
    private static final String TAG = "BluetoothMapWrapper";
    private BluetoothMap mService;

    public BluetoothMapWrapper(BluetoothProfile proxy) {
        this.mService = (BluetoothMap) proxy;
    }

    public List<BluetoothDevice> getConnectedDevices() {
        BluetoothMap bluetoothMap = this.mService;
        if (bluetoothMap != null) {
            return bluetoothMap.getConnectedDevices();
        }
        return new ArrayList();
    }

    public boolean disconnect(BluetoothDevice device) {
        BluetoothMap bluetoothMap = this.mService;
        if (bluetoothMap != null) {
            return bluetoothMap.disconnect(device);
        }
        return false;
    }

    public boolean connect(BluetoothDevice device) {
        BluetoothMap bluetoothMap = this.mService;
        if (bluetoothMap != null) {
            return bluetoothMap.connect(device);
        }
        return false;
    }

    public int getConnectionState(BluetoothDevice device) {
        BluetoothMap bluetoothMap = this.mService;
        if (bluetoothMap != null) {
            return bluetoothMap.getConnectionState(device);
        }
        return 0;
    }

    public int getPriority(BluetoothDevice device) {
        BluetoothMap bluetoothMap = this.mService;
        if (bluetoothMap != null) {
            return bluetoothMap.getPriority(device);
        }
        return 0;
    }

    public boolean setPriority(BluetoothDevice device, int priority) {
        BluetoothMap bluetoothMap = this.mService;
        if (bluetoothMap != null) {
            return bluetoothMap.setPriority(device, priority);
        }
        return false;
    }

    public BluetoothProfile getDefaultProfile() {
        BluetoothMap bluetoothMap = this.mService;
        if (bluetoothMap != null) {
            return bluetoothMap;
        }
        return null;
    }

    public List<BluetoothDevice> getDevicesMatchingConnectionStates(int[] states) {
        BluetoothMap bluetoothMap = this.mService;
        if (bluetoothMap != null) {
            return bluetoothMap.getDevicesMatchingConnectionStates(states);
        }
        return new ArrayList();
    }
}
