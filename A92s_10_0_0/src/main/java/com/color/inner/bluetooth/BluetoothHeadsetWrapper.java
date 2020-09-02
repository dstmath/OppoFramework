package com.color.inner.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;

public class BluetoothHeadsetWrapper {
    public static boolean setPriority(BluetoothHeadset mService, BluetoothDevice device, int priority) {
        return mService.setPriority(device, priority);
    }

    public static int getPriority(BluetoothHeadset mService, BluetoothDevice device) {
        return mService.getPriority(device);
    }

    public static boolean connect(BluetoothHeadset mService, BluetoothDevice device) {
        return mService.connect(device);
    }

    public static boolean disconnect(BluetoothHeadset mService, BluetoothDevice device) {
        return mService.disconnect(device);
    }

    public static BluetoothDevice getActiveDevice(BluetoothHeadset mService) {
        return mService.getActiveDevice();
    }
}
