package com.mediatek.internal.telephony;

import android.content.Context;
import com.android.internal.telephony.SmsUsageMonitor;

public class MtkSmsUsageMonitor extends SmsUsageMonitor {
    private static final String[] SKIP_SEND_LIMIT_PACKAGES = {"com.android.mms", "com.mediatek.autotest"};
    private static final String TAG = "MtkSmsUsageMonitor";

    public MtkSmsUsageMonitor(Context context) {
        super(context);
    }

    public boolean check(String appName, int smsWaiting) {
        for (String name : SKIP_SEND_LIMIT_PACKAGES) {
            if (appName.equals(name)) {
                return true;
            }
        }
        return MtkSmsUsageMonitor.super.check(appName, smsWaiting);
    }
}
