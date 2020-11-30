package com.color.inner.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothPan;
import android.bluetooth.BluetoothProfile;
import java.util.ArrayList;
import java.util.List;

public class BluetoothPanWrapper {
    private static final String TAG = "BluetoothPanWrapper";
    private BluetoothPan mService;

    public BluetoothPanWrapper(BluetoothProfile proxy) {
        this.mService = (BluetoothPan) proxy;
    }

    public List<BluetoothDevice> getConnectedDevices() {
        BluetoothPan bluetoothPan = this.mService;
        if (bluetoothPan != null) {
            return bluetoothPan.getConnectedDevices();
        }
        return new ArrayList();
    }

    public boolean disconnect(BluetoothDevice device) {
        BluetoothPan bluetoothPan = this.mService;
        if (bluetoothPan != null) {
            return bluetoothPan.disconnect(device);
        }
        return false;
    }

    public boolean connect(BluetoothDevice device) {
        BluetoothPan bluetoothPan = this.mService;
        if (bluetoothPan != null) {
            return bluetoothPan.connect(device);
        }
        return false;
    }

    public int getConnectionState(BluetoothDevice device) {
        BluetoothPan bluetoothPan = this.mService;
        if (bluetoothPan != null) {
            return bluetoothPan.getConnectionState(device);
        }
        return 0;
    }

    public boolean isTetheringOn() {
        BluetoothPan bluetoothPan = this.mService;
        if (bluetoothPan != null) {
            return bluetoothPan.isTetheringOn();
        }
        return false;
    }

    public BluetoothProfile getDefaultProfile() {
        BluetoothPan bluetoothPan = this.mService;
        if (bluetoothPan != null) {
            return bluetoothPan;
        }
        return null;
    }
}
