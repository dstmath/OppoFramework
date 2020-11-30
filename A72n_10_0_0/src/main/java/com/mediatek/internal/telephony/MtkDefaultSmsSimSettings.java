package com.mediatek.internal.telephony;

import android.content.Context;
import android.os.SystemProperties;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.util.Log;
import java.util.List;
import mediatek.telephony.MtkSignalStrength;

public class MtkDefaultSmsSimSettings {
    public static final int ASK_USER_SUB_ID = -2;
    private static final String TAG = "MTKDefaultSmsSimSettings";

    public static void setSmsTalkDefaultSim(List<SubscriptionInfo> subInfos, Context context) {
        int oldSmsDefaultSIM = SubscriptionManager.getDefaultSmsSubscriptionId();
        Log.i(TAG, "oldSmsDefaultSIM" + oldSmsDefaultSIM);
        if (subInfos == null) {
            Log.i(TAG, "subInfos == null, return");
            return;
        }
        Log.i(TAG, "subInfos size = " + subInfos.size());
        if (subInfos.size() > 1) {
            if (isoldDefaultSMSSubIdActive(subInfos)) {
                Log.i(TAG, "subInfos size > 1 & old available, set to :" + oldSmsDefaultSIM);
                return;
            }
            String optr = SystemProperties.get(MtkSignalStrength.PROPERTY_OPERATOR_OPTR);
            Log.d(TAG, "optr = " + optr);
            if ("OP01".equals(optr)) {
                Log.i(TAG, "subInfos size > 1, set to : ASK_USER_SUB_ID");
                SubscriptionManager.from(context).setDefaultSmsSubId(-2);
            } else if ("OP09".equals(optr) && "SEGDEFAULT".equals(SystemProperties.get("persist.vendor.operator.seg"))) {
                SubscriptionInfo subInfo = SubscriptionManager.from(context).getActiveSubscriptionInfoForSimSlotIndex(0);
                int firstSubId = -1;
                if (subInfo != null) {
                    firstSubId = subInfo.getSubscriptionId();
                }
                SubscriptionManager.from(context).setDefaultSmsSubId(firstSubId);
                Log.i(TAG, "subInfos size > 1, set to " + firstSubId);
            } else if ("OP07".equals(optr)) {
                int mainPhoneId = getMainCapabilityPhoneId();
                Log.d(TAG, "Main slot = " + mainPhoneId);
                SubscriptionInfo subInfo2 = SubscriptionManager.from(context).getActiveSubscriptionInfoForSimSlotIndex(mainPhoneId);
                int mainSubId = -1;
                if (subInfo2 != null) {
                    mainSubId = subInfo2.getSubscriptionId();
                    Log.d(TAG, "Get subId from subInfo = " + mainSubId);
                }
                Log.d(TAG, "subInfos size > 1, set to " + mainSubId);
                SubscriptionManager.from(context).setDefaultSmsSubId(mainSubId);
            } else {
                Log.i(TAG, "subInfos size > 1, set to : ASK_USER_SUB_ID");
            }
        } else if (subInfos.size() == 1) {
            Log.i(TAG, "sub size = 1,segment = " + SystemProperties.get("persist.vendor.operator.seg"));
            if ("OP09".equals(SystemProperties.get(MtkSignalStrength.PROPERTY_OPERATOR_OPTR)) && "SEGDEFAULT".equals(SystemProperties.get("persist.vendor.operator.seg"))) {
                int defaultSubId = subInfos.get(0).getSubscriptionId();
                SubscriptionManager.from(context).setDefaultSmsSubId(defaultSubId);
                Log.i(TAG, "subInfos size = 1, set to " + defaultSubId);
            } else if ("OP01".equals(SystemProperties.get(MtkSignalStrength.PROPERTY_OPERATOR_OPTR))) {
                int defaultSubId2 = subInfos.get(0).getSubscriptionId();
                SubscriptionManager.from(context).setDefaultSmsSubId(defaultSubId2);
                Log.i(TAG, "subInfos size = 1, set to " + defaultSubId2);
            }
        } else {
            Log.i(TAG, "setSmsTalkDefaultSim SIM not insert");
        }
    }

    private static boolean isoldDefaultSMSSubIdActive(List<SubscriptionInfo> subInfos) {
        int oldSmsDefaultSIM = SubscriptionManager.getDefaultSmsSubscriptionId();
        for (SubscriptionInfo subInfo : subInfos) {
            if (subInfo.getSubscriptionId() == oldSmsDefaultSIM) {
                return true;
            }
        }
        if (!"OP01".equals(SystemProperties.get(MtkSignalStrength.PROPERTY_OPERATOR_OPTR)) || oldSmsDefaultSIM != -2) {
            return false;
        }
        return true;
    }

    private static int getMainCapabilityPhoneId() {
        int phoneId = SystemProperties.getInt(MtkPhoneConstants.PROPERTY_CAPABILITY_SWITCH, 1) - 1;
        Log.d(TAG, "getMainCapabilityPhoneId " + phoneId);
        return phoneId;
    }
}
