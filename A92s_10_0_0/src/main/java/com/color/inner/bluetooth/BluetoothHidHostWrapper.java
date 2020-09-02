package com.color.inner.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHidHost;
import android.bluetooth.BluetoothProfile;
import java.util.ArrayList;
import java.util.List;

public class BluetoothHidHostWrapper {
    private static final String TAG = "BluetoothHidHostWrapper";
    private BluetoothHidHost mService;

    public BluetoothHidHostWrapper(BluetoothProfile proxy) {
        this.mService = (BluetoothHidHost) proxy;
    }

    public List<BluetoothDevice> getConnectedDevices() {
        BluetoothHidHost bluetoothHidHost = this.mService;
        if (bluetoothHidHost != null) {
            return bluetoothHidHost.getConnectedDevices();
        }
        return new ArrayList();
    }

    public boolean disconnect(BluetoothDevice device) {
        BluetoothHidHost bluetoothHidHost = this.mService;
        if (bluetoothHidHost != null) {
            return bluetoothHidHost.disconnect(device);
        }
        return false;
    }

    public boolean connect(BluetoothDevice device) {
        BluetoothHidHost bluetoothHidHost = this.mService;
        if (bluetoothHidHost != null) {
            return bluetoothHidHost.connect(device);
        }
        return false;
    }

    public int getConnectionState(BluetoothDevice device) {
        BluetoothHidHost bluetoothHidHost = this.mService;
        if (bluetoothHidHost != null) {
            return bluetoothHidHost.getConnectionState(device);
        }
        return 0;
    }

    public int getPriority(BluetoothDevice device) {
        BluetoothHidHost bluetoothHidHost = this.mService;
        if (bluetoothHidHost != null) {
            return bluetoothHidHost.getPriority(device);
        }
        return 0;
    }

    public boolean setPriority(BluetoothDevice device, int priority) {
        BluetoothHidHost bluetoothHidHost = this.mService;
        if (bluetoothHidHost != null) {
            return bluetoothHidHost.setPriority(device, priority);
        }
        return false;
    }

    public BluetoothProfile getDefaultProfile() {
        BluetoothHidHost bluetoothHidHost = this.mService;
        if (bluetoothHidHost != null) {
            return bluetoothHidHost;
        }
        return null;
    }
}
