package com.android.internal.telephony.dataconnection;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.os.SystemProperties;
import android.telephony.CarrierConfigManager;
import android.telephony.Rlog;
import android.telephony.data.ApnSetting;
import android.util.Log;
import com.android.internal.telephony.OppoModemLogManager;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.uicc.IccRecords;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;

public class ApnSettingUtils {
    private static final boolean DBG = false;
    static final String LOG_TAG = "ApnSetting";
    private static Method sMethodIsMeteredApnTypeEx;
    private static Method sMethodMvnoMatchesEx;

    static {
        if (SystemProperties.get("ro.vendor.mtk_telephony_add_on_policy", OppoModemLogManager.DEFAULT_MODEMDUMP_POSTBACK).equals(OppoModemLogManager.DEFAULT_MODEMDUMP_POSTBACK)) {
            Class<?> clz = null;
            try {
                clz = Class.forName("com.mediatek.internal.telephony.dataconnection.MtkApnSettingUtils");
            } catch (Exception e) {
                Rlog.d(LOG_TAG, e.toString());
            }
            if (clz != null) {
                try {
                    sMethodMvnoMatchesEx = clz.getDeclaredMethod("mvnoMatchesEx", IccRecords.class, Integer.TYPE, String.class);
                    sMethodMvnoMatchesEx.setAccessible(true);
                } catch (Exception e2) {
                    Rlog.d(LOG_TAG, e2.toString());
                }
                try {
                    sMethodIsMeteredApnTypeEx = clz.getDeclaredMethod("isMeteredApnTypeEx", Integer.TYPE, Phone.class);
                    sMethodIsMeteredApnTypeEx.setAccessible(true);
                } catch (Exception e3) {
                    Rlog.d(LOG_TAG, e3.toString());
                }
            }
        }
    }

    public static boolean iccidMatches(String mvnoData, String iccId) {
        for (String mvnoIccid : mvnoData.split(",")) {
            if (iccId.startsWith(mvnoIccid)) {
                Log.d(LOG_TAG, "mvno icc id match found");
                return true;
            }
        }
        return false;
    }

    public static boolean imsiMatches(String imsiDB, String imsiSIM) {
        int len = imsiDB.length();
        if (len <= 0 || len > imsiSIM.length()) {
            return false;
        }
        for (int idx = 0; idx < len; idx++) {
            char c = imsiDB.charAt(idx);
            if (c != 'x' && c != 'X' && c != imsiSIM.charAt(idx)) {
                return false;
            }
        }
        return true;
    }

    public static boolean mvnoMatches(IccRecords r, int mvnoType, String mvnoMatchData) {
        String iccId;
        if (mvnoType == 0) {
            if (r.getServiceProviderName() != null && r.getServiceProviderName().equalsIgnoreCase(mvnoMatchData)) {
                return true;
            }
        } else if (mvnoType == 1) {
            String imsiSIM = r.getIMSI();
            if (imsiSIM != null && imsiMatches(mvnoMatchData, imsiSIM)) {
                return true;
            }
        } else if (mvnoType == 2) {
            String gid1 = r.getGid1();
            int mvno_match_data_length = mvnoMatchData.length();
            if (gid1 != null && gid1.length() >= mvno_match_data_length && gid1.substring(0, mvno_match_data_length).equalsIgnoreCase(mvnoMatchData)) {
                return true;
            }
        } else if (mvnoType == 3 && (iccId = r.getIccId()) != null && iccidMatches(mvnoMatchData, iccId)) {
            return true;
        }
        try {
            if (sMethodMvnoMatchesEx != null) {
                return ((Boolean) sMethodMvnoMatchesEx.invoke(null, r, Integer.valueOf(mvnoType), mvnoMatchData)).booleanValue();
            }
        } catch (Exception e) {
            Rlog.d(LOG_TAG, e.toString());
        }
        return false;
    }

    public static boolean isMeteredApnType(int apnType, Phone phone) {
        String carrierConfig;
        if (phone == null) {
            return true;
        }
        try {
            if (sMethodIsMeteredApnTypeEx != null) {
                Bundle b = (Bundle) sMethodIsMeteredApnTypeEx.invoke(null, Integer.valueOf(apnType), phone);
                if (b.getBoolean("useEx")) {
                    return b.getBoolean("result");
                }
            }
        } catch (Exception e) {
            Rlog.d(LOG_TAG, e.toString());
        }
        boolean isRoaming = phone.getServiceState().getDataRoaming();
        int subId = phone.getSubId();
        if (isRoaming) {
            carrierConfig = "carrier_metered_roaming_apn_types_strings";
        } else {
            carrierConfig = "carrier_metered_apn_types_strings";
        }
        CarrierConfigManager configManager = (CarrierConfigManager) phone.getContext().getSystemService("carrier_config");
        if (configManager == null) {
            Rlog.e(LOG_TAG, "Carrier config service is not available");
            return true;
        }
        PersistableBundle b2 = configManager.getConfigForSubId(subId);
        if (b2 == null) {
            Rlog.e(LOG_TAG, "Can't get the config. subId = " + subId);
            return true;
        }
        String[] meteredApnTypes = b2.getStringArray(carrierConfig);
        if (meteredApnTypes == null) {
            Rlog.e(LOG_TAG, carrierConfig + " is not available. subId = " + subId);
            return true;
        }
        HashSet<String> meteredApnSet = new HashSet<>(Arrays.asList(meteredApnTypes));
        if (meteredApnSet.contains(ApnSetting.getApnTypeString(apnType))) {
            return true;
        }
        if (apnType != 255 || meteredApnSet.size() <= 0) {
            return false;
        }
        return true;
    }

    public static boolean isMetered(ApnSetting apn, Phone phone) {
        if (phone == null || apn == null) {
            return true;
        }
        for (Integer num : apn.getApnTypes()) {
            if (isMeteredApnType(num.intValue(), phone)) {
                return true;
            }
        }
        return false;
    }
}
