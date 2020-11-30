package com.color.inner.telephony;

import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.util.Log;

public class SubscriptionManagerWrapper {
    private static final String TAG = "SubscriptionManagerWrapper";

    private SubscriptionManagerWrapper() {
    }

    public static int getDefaultVoicePhoneId() {
        try {
            return SubscriptionManager.getDefaultVoicePhoneId();
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return -1;
        }
    }

    public static SubscriptionInfo getDefaultVoiceSubscriptionInfo(SubscriptionManager subscriptionManager) {
        try {
            return subscriptionManager.getDefaultVoiceSubscriptionInfo();
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }

    public static int setDisplayName(SubscriptionManager subscriptionManager, String displayName, int subId, int nameSource) {
        try {
            return subscriptionManager.setDisplayName(displayName, subId, nameSource);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return 0;
        }
    }

    public static int getActiveDataSubscriptionId() {
        try {
            Class<?> subscriptionManager = Class.forName("android.telephony.SubscriptionManager");
            return ((Integer) subscriptionManager.getMethod("getActiveDataSubscriptionId", new Class[0]).invoke(subscriptionManager, new Object[0])).intValue();
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return -1;
        }
    }

    public static int[] getSubId(int slotIndex) {
        return SubscriptionManager.getSubId(slotIndex);
    }
}
