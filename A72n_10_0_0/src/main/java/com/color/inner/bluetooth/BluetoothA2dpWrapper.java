package com.color.inner.bluetooth;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothDevice;

public class BluetoothA2dpWrapper {
    private static final String TAG = "BluetoothA2dpWrapper";

    public static int getPriority(BluetoothA2dp mService, BluetoothDevice device) {
        if (mService != null) {
            return mService.getPriority(device);
        }
        return 0;
    }

    public static boolean setPriority(BluetoothA2dp mService, BluetoothDevice device, int priority) {
        if (mService != null) {
            return mService.setPriority(device, priority);
        }
        return false;
    }

    public static boolean disconnect(BluetoothA2dp mService, BluetoothDevice device) {
        if (mService != null) {
            return mService.disconnect(device);
        }
        return false;
    }

    public static boolean connect(BluetoothA2dp mService, BluetoothDevice device) {
        if (mService != null) {
            return mService.connect(device);
        }
        return false;
    }

    public static boolean setActiveDevice(BluetoothA2dp mService, BluetoothDevice device) {
        if (mService != null) {
            return mService.setActiveDevice(device);
        }
        return false;
    }

    public static BluetoothDevice getActiveDevice(BluetoothA2dp mService) {
        if (mService != null) {
            return mService.getActiveDevice();
        }
        return null;
    }
}
