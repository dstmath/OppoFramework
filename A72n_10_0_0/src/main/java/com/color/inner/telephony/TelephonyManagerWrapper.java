package com.color.inner.telephony;

import android.telephony.TelephonyManager;
import android.util.Log;
import java.util.List;

public class TelephonyManagerWrapper {
    public static final int NETWORK_CLASS_2_G = 1;
    public static final int NETWORK_CLASS_3_G = 2;
    public static final int NETWORK_CLASS_4_G = 3;
    public static final int NETWORK_CLASS_UNKNOWN = 0;
    private static final String TAG = "TelephonyManagerWrapper";

    private TelephonyManagerWrapper() {
    }

    public static int getNetworkClass(TelephonyManager telephonyManager, int networkType) {
        try {
            return TelephonyManager.getNetworkClass(networkType);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return 0;
        }
    }

    public static String getTelephonyProperty(TelephonyManager telephonyManager, int phoneId, String property, String defaultVal) {
        try {
            return TelephonyManager.getTelephonyProperty(phoneId, property, defaultVal);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }

    public static boolean isMultiSimEnabled(TelephonyManager telephonyManager) {
        try {
            return telephonyManager.isMultiSimEnabled();
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return false;
        }
    }

    public static String getIccAuthentication(TelephonyManager telephonyManager, int subId, int appType, int authType, String data) {
        try {
            return telephonyManager.getIccAuthentication(subId, appType, authType, data);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }

    public static boolean setRoamingOverride(TelephonyManager telephonyManager, List<String> gsmRoamingList, List<String> gsmNonRoamingList, List<String> cdmaRoamingList, List<String> cdmaNonRoamingList) {
        try {
            return telephonyManager.setRoamingOverride(gsmRoamingList, gsmNonRoamingList, cdmaRoamingList, cdmaNonRoamingList);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return false;
        }
    }

    public static int getPreferredNetworkType(TelephonyManager telephonyManager, int subId) {
        try {
            return telephonyManager.getPreferredNetworkType(subId);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return -1;
        }
    }

    public static String getSimCountryIso(TelephonyManager telephonyManager, int subId) {
        return telephonyManager.getSimCountryIso(subId);
    }

    public static boolean hasIccCard(TelephonyManager telephonyManager, int slotIndex) {
        return telephonyManager.hasIccCard(slotIndex);
    }

    public static int getSlotIndex(TelephonyManager telephonyManager) {
        return telephonyManager.getSlotIndex();
    }
}
