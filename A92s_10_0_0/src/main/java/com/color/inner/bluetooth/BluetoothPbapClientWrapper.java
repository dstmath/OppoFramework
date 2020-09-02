package com.color.inner.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothPbapClient;
import android.bluetooth.BluetoothProfile;
import java.util.ArrayList;
import java.util.List;

public class BluetoothPbapClientWrapper {
    private static final String TAG = "PbapClientWrapper";
    private BluetoothPbapClient mService;

    public BluetoothPbapClientWrapper(BluetoothProfile proxy) {
        this.mService = (BluetoothPbapClient) proxy;
    }

    public List<BluetoothDevice> getConnectedDevices() {
        BluetoothPbapClient bluetoothPbapClient = this.mService;
        if (bluetoothPbapClient != null) {
            return bluetoothPbapClient.getConnectedDevices();
        }
        return new ArrayList();
    }

    public boolean disconnect(BluetoothDevice device) {
        BluetoothPbapClient bluetoothPbapClient = this.mService;
        if (bluetoothPbapClient != null) {
            return bluetoothPbapClient.disconnect(device);
        }
        return false;
    }

    public boolean connect(BluetoothDevice device) {
        BluetoothPbapClient bluetoothPbapClient = this.mService;
        if (bluetoothPbapClient != null) {
            return bluetoothPbapClient.connect(device);
        }
        return false;
    }

    public int getConnectionState(BluetoothDevice device) {
        BluetoothPbapClient bluetoothPbapClient = this.mService;
        if (bluetoothPbapClient != null) {
            return bluetoothPbapClient.getConnectionState(device);
        }
        return 0;
    }

    public int getPriority(BluetoothDevice device) {
        BluetoothPbapClient bluetoothPbapClient = this.mService;
        if (bluetoothPbapClient != null) {
            return bluetoothPbapClient.getPriority(device);
        }
        return 0;
    }

    public boolean setPriority(BluetoothDevice device, int priority) {
        BluetoothPbapClient bluetoothPbapClient = this.mService;
        if (bluetoothPbapClient != null) {
            return bluetoothPbapClient.setPriority(device, priority);
        }
        return false;
    }

    public BluetoothProfile getDefaultProfile() {
        BluetoothPbapClient bluetoothPbapClient = this.mService;
        if (bluetoothPbapClient != null) {
            return bluetoothPbapClient;
        }
        return null;
    }

    public List<BluetoothDevice> getDevicesMatchingConnectionStates(int[] states) {
        BluetoothPbapClient bluetoothPbapClient = this.mService;
        if (bluetoothPbapClient != null) {
            return bluetoothPbapClient.getDevicesMatchingConnectionStates(states);
        }
        return new ArrayList();
    }
}
