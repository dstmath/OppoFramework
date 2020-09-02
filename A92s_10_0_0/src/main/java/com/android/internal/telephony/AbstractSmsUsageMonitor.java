package com.android.internal.telephony;

import android.content.Context;

public class AbstractSmsUsageMonitor {
    private static final String LOG_TAG = "AbstractSmsUsageMonitor";

    public boolean oemCheck(String appName, Context contex) {
        IOppoSmsManager manager = (IOppoSmsManager) OppoTelephonyFactory.getInstance().getFeature(IOppoSmsManager.DEFAULT, new Object[0]);
        if (manager != null) {
            return manager.oemCheck(appName, contex);
        }
        return false;
    }
}
