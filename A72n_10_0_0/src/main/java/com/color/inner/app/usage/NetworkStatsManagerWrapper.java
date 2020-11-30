package com.color.inner.app.usage;

import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.net.NetworkTemplate;
import android.os.RemoteException;

public class NetworkStatsManagerWrapper {
    private static final String TAG = "NetworkStatsManagerWrapper";

    private NetworkStatsManagerWrapper() {
    }

    public static long querySummaryForDeviceWithMobileAllTemplate(NetworkStatsManager networkStatsManager, String subscriberId, long startTime, long endTime) throws RemoteException {
        NetworkStats.Bucket bucket = networkStatsManager.querySummaryForDevice(NetworkTemplate.buildTemplateMobileAll(subscriberId), startTime, endTime);
        return bucket.getRxBytes() + bucket.getTxBytes();
    }

    public static long querySummaryForDeviceWithWifiTemplate(NetworkStatsManager networkStatsManager, long startTime, long endTime) throws RemoteException {
        NetworkStats.Bucket bucket = networkStatsManager.querySummaryForDevice(NetworkTemplate.buildTemplateWifiWildcard(), startTime, endTime);
        return bucket.getRxBytes() + bucket.getTxBytes();
    }
}
