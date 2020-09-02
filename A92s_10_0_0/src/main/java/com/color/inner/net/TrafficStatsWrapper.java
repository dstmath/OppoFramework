package com.color.inner.net;

import android.net.INetworkStatsService;
import android.os.ServiceManager;
import android.util.Log;

public class TrafficStatsWrapper {
    private static final String TAG = "TrafficStatsWrapper";

    public static Object getStatsService() {
        try {
            return INetworkStatsService.Stub.asInterface(ServiceManager.getService("netstats"));
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }
}
