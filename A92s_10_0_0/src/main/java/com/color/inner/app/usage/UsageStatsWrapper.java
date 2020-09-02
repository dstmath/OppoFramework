package com.color.inner.app.usage;

import android.app.usage.UsageStats;

public class UsageStatsWrapper {
    public static int getAppLaunchCount(UsageStats usageStats) {
        return usageStats.getAppLaunchCount();
    }
}
