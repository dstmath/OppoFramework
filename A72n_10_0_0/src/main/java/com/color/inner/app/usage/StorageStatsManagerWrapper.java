package com.color.inner.app.usage;

import android.app.usage.StorageStats;
import android.app.usage.StorageStatsManager;
import android.os.UserHandle;
import android.util.Log;
import java.util.UUID;

public class StorageStatsManagerWrapper {
    private static final String TAG = "StorageStatsManagerWrapper";

    public static StorageStats queryStatsForPackage(StorageStatsManager manager, UUID uuid, String packageName, UserHandle user) {
        try {
            return manager.queryStatsForPackage(uuid, packageName, user);
        } catch (Exception e) {
            Log.e(TAG, "queryStatsForPackage exception:" + e);
            return null;
        }
    }
}
