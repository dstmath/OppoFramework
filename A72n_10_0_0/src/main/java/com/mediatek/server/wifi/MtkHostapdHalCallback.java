package com.mediatek.server.wifi;

import android.util.Log;
import com.android.server.wifi.util.NativeUtil;
import vendor.mediatek.hardware.wifi.hostapd.V2_0.IHostapdCallback;

public class MtkHostapdHalCallback extends IHostapdCallback.Stub {
    private static final String TAG = "MtkHostapdHalCallback";

    @Override // vendor.mediatek.hardware.wifi.hostapd.V2_0.IHostapdCallback
    public void onStaAuthorized(byte[] staAddress) {
        try {
            String macString = NativeUtil.macAddressFromByteArray(staAddress);
            Log.e(TAG, "STA: " + macString + "is connected");
            MtkWifiApMonitor.broadcastApStaConnected(MtkHostapdHal.getIfaceName(), macString);
        } catch (Exception e) {
            Log.e(TAG, "Could not decode MAC address.", e);
        }
    }

    @Override // vendor.mediatek.hardware.wifi.hostapd.V2_0.IHostapdCallback
    public void onStaDeauthorized(byte[] staAddress) {
        try {
            String macString = NativeUtil.macAddressFromByteArray(staAddress);
            Log.e(TAG, "STA: " + macString + "is disconnected");
            MtkWifiApMonitor.broadcastApStaDisconnected(MtkHostapdHal.getIfaceName(), macString);
        } catch (Exception e) {
            Log.e(TAG, "Could not decode MAC address.", e);
        }
    }
}
