package com.color.inner.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.os.ParcelUuid;
import java.util.ArrayList;
import java.util.List;

public class BluetoothAdapterWrapper {
    private static final String TAG = "BluetoothAdapterWrapper";

    private BluetoothAdapterWrapper() {
    }

    public static int getConnectionState(BluetoothAdapter bluetoothAdapter) {
        if (bluetoothAdapter != null) {
            return bluetoothAdapter.getConnectionState();
        }
        return 0;
    }

    public static ParcelUuid[] getUuids(BluetoothAdapter mAdapter) {
        if (mAdapter != null) {
            return mAdapter.getUuids();
        }
        return null;
    }

    public static boolean setScanMode(BluetoothAdapter mAdapter, int mode) {
        if (mAdapter != null) {
            return mAdapter.setScanMode(mode);
        }
        return false;
    }

    public static int getMaxConnectedAudioDevices(BluetoothAdapter mAdapter) {
        if (mAdapter != null) {
            return mAdapter.getMaxConnectedAudioDevices();
        }
        return 1;
    }

    public static List<Integer> getSupportedProfiles(BluetoothAdapter mAdapter) {
        if (mAdapter != null) {
            return mAdapter.getSupportedProfiles();
        }
        return new ArrayList();
    }
}
