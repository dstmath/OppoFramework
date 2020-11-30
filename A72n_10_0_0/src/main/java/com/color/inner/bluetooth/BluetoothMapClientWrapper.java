package com.color.inner.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothMapClient;
import android.bluetooth.BluetoothProfile;
import java.util.ArrayList;
import java.util.List;

public class BluetoothMapClientWrapper {
    private static final String TAG = "MapClientWrapper";
    private BluetoothMapClient mService;

    public BluetoothMapClientWrapper(BluetoothProfile proxy) {
        this.mService = (BluetoothMapClient) proxy;
    }

    public List<BluetoothDevice> getConnectedDevices() {
        BluetoothMapClient bluetoothMapClient = this.mService;
        if (bluetoothMapClient != null) {
            return bluetoothMapClient.getConnectedDevices();
        }
        return new ArrayList();
    }

    public boolean disconnect(BluetoothDevice device) {
        BluetoothMapClient bluetoothMapClient = this.mService;
        if (bluetoothMapClient != null) {
            return bluetoothMapClient.disconnect(device);
        }
        return false;
    }

    public boolean connect(BluetoothDevice device) {
        BluetoothMapClient bluetoothMapClient = this.mService;
        if (bluetoothMapClient != null) {
            return bluetoothMapClient.connect(device);
        }
        return false;
    }

    public int getConnectionState(BluetoothDevice device) {
        BluetoothMapClient bluetoothMapClient = this.mService;
        if (bluetoothMapClient != null) {
            return bluetoothMapClient.getConnectionState(device);
        }
        return 0;
    }

    public int getPriority(BluetoothDevice device) {
        BluetoothMapClient bluetoothMapClient = this.mService;
        if (bluetoothMapClient != null) {
            return bluetoothMapClient.getPriority(device);
        }
        return 0;
    }

    public boolean setPriority(BluetoothDevice device, int priority) {
        BluetoothMapClient bluetoothMapClient = this.mService;
        if (bluetoothMapClient != null) {
            return bluetoothMapClient.setPriority(device, priority);
        }
        return false;
    }

    public BluetoothProfile getDefaultProfile() {
        BluetoothMapClient bluetoothMapClient = this.mService;
        if (bluetoothMapClient != null) {
            return bluetoothMapClient;
        }
        return null;
    }

    public List<BluetoothDevice> getDevicesMatchingConnectionStates(int[] states) {
        BluetoothMapClient bluetoothMapClient = this.mService;
        if (bluetoothMapClient != null) {
            return bluetoothMapClient.getDevicesMatchingConnectionStates(states);
        }
        return new ArrayList();
    }
}
