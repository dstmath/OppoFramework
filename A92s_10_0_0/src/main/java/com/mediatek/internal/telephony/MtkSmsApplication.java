package com.mediatek.internal.telephony;

import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.telephony.Rlog;
import android.telephony.SmsManager;
import com.android.internal.telephony.SmsApplication;

public final class MtkSmsApplication {
    private static final boolean DBG = "eng".equals(Build.TYPE);
    private static final String LOG_TAG = "MtkSmsApplication";

    public static ComponentName getDefaultSmsApplication(Context context, boolean updateIfNeeded, int userId) {
        ComponentName component = null;
        try {
            SmsApplication.SmsApplicationData smsApplicationData = SmsApplication.getApplication(context, updateIfNeeded, userId);
            if (smsApplicationData != null) {
                component = new ComponentName(smsApplicationData.mPackageName, smsApplicationData.mSmsReceiverClass);
            }
            return component;
        } finally {
            if (DBG) {
                Rlog.d(LOG_TAG, "getDefaultSmsApplication for userId " + userId + " default component= " + component);
            }
        }
    }

    private static String getDefaultSmsApplicationPackageName(Context context, int userId) {
        ComponentName component = getDefaultSmsApplication(context, false, userId);
        if (component != null) {
            return component.getPackageName();
        }
        return null;
    }

    public static boolean isDefaultSmsApplication(Context context, String packageName, int userId) {
        if (packageName == null) {
            return false;
        }
        String defaultSmsPackage = getDefaultSmsApplicationPackageName(context, userId);
        if ((defaultSmsPackage == null || !defaultSmsPackage.equals(packageName)) && !"com.android.bluetooth".equals(packageName)) {
            return false;
        }
        return true;
    }

    public static boolean shouldWriteMessageForPackage(String packageName, Context context, int userId) {
        if (SmsManager.getDefault().getAutoPersisting()) {
            return true;
        }
        boolean result = !isDefaultSmsApplication(context, packageName, userId);
        if (DBG) {
            Rlog.d(LOG_TAG, "shouldWriteMessageForPackage for userId " + userId + ", shouldWrite=" + result);
        }
        return result;
    }
}
