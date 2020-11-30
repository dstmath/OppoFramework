package com.color.inner.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHearingAid;
import java.util.List;

public class BluetoothHearingAidWrapper {
    private static final String TAG = "BluetoothHearingAid";

    public static boolean connect(BluetoothHearingAid service, BluetoothDevice bluetoothDevice) {
        return service.connect(bluetoothDevice);
    }

    public static int getPriority(BluetoothHearingAid mService, BluetoothDevice device) {
        return mService.getPriority(device);
    }

    public static boolean setPriority(BluetoothHearingAid mService, BluetoothDevice device, int priority) {
        return mService.setPriority(device, priority);
    }

    public static boolean disconnect(BluetoothHearingAid mService, BluetoothDevice device) {
        return mService.disconnect(device);
    }

    public static List<BluetoothDevice> getActiveDevices(BluetoothHearingAid mService) {
        return mService.getActiveDevices();
    }

    public static int getVolume(BluetoothHearingAid mService) {
        return mService.getVolume();
    }

    public static void setVolume(BluetoothHearingAid mService, int volume) {
        mService.setVolume(volume);
    }

    public static long getHiSyncId(BluetoothHearingAid mService, BluetoothDevice device) {
        return mService.getHiSyncId(device);
    }
}
