package com.color.inner.net.wifi.p2p;

import android.net.wifi.p2p.WifiP2pGroup;
import android.util.Log;

public class WifiP2pGroupWrapper {
    private static final String TAG = "WifiP2pGroupWrapper";

    public static int getNetworkId(WifiP2pGroup wifiP2pGroup) {
        try {
            return wifiP2pGroup.getNetworkId();
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return -1;
        }
    }
}
