package android.bluetooth;

import android.os.ParcelFileDescriptor;
import android.util.Log;

@Deprecated
public abstract class BluetoothHealthCallback {
    private static final String TAG = "BluetoothHealthCallback";

    @Deprecated
    public void onHealthAppConfigurationStatusChange(BluetoothHealthAppConfiguration config, int status) {
        Log.d(TAG, "onHealthAppConfigurationStatusChange: " + config + "Status: " + status);
    }

    @Deprecated
    public void onHealthChannelStateChange(BluetoothHealthAppConfiguration config, BluetoothDevice device, int prevState, int newState, ParcelFileDescriptor fd, int channelId) {
        Log.d(TAG, "onHealthChannelStateChange: " + config + "Device: " + device + "prevState:" + prevState + "newState:" + newState + "ParcelFd:" + fd + "ChannelId:" + channelId);
    }
}
