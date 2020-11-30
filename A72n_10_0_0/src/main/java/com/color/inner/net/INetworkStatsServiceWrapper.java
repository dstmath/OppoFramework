package com.color.inner.net;

import android.util.Log;

public class INetworkStatsServiceWrapper {
    private static final String TAG = "INetworkStatsServiceWrapper";

    public static INetworkStatsSessionWrapper openSession() {
        try {
            return new INetworkStatsSessionWrapper();
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }
}
