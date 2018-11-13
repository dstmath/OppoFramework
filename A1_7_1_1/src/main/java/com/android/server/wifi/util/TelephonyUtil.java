package com.android.server.wifi.util;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;

public class TelephonyUtil {
    public static final int GET_SUBID_NULL_ERROR = -1;

    public static String getSimIdentity(Context context, int eapMethod, int slotId) {
        TelephonyManager tm = TelephonyManager.from(context);
        int subId = getSubId(slotId);
        if (tm == null) {
            return null;
        }
        String imsi;
        String mccMnc = "";
        if (TelephonyManager.getDefault().getPhoneCount() < 2 || subId == -1) {
            imsi = tm.getSubscriberId();
            if (tm.getSimState() == 5) {
                mccMnc = tm.getSimOperator();
            }
        } else {
            imsi = tm.getSubscriberId(subId);
            if (tm.getSimState(slotId) == 5) {
                mccMnc = tm.getSimOperator(subId);
            }
        }
        return buildIdentity(eapMethod, imsi, mccMnc);
    }

    private static String buildIdentity(int eapMethod, String imsi, String mccMnc) {
        if (imsi == null || imsi.isEmpty()) {
            return null;
        }
        String prefix;
        String mcc;
        String mnc;
        if (eapMethod == 4) {
            prefix = "1";
        } else if (eapMethod == 5) {
            prefix = "0";
        } else if (eapMethod != 6) {
            return null;
        } else {
            prefix = "6";
        }
        if (mccMnc == null || mccMnc.isEmpty()) {
            mcc = imsi.substring(0, 3);
            mnc = imsi.substring(3, 6);
        } else {
            mcc = mccMnc.substring(0, 3);
            mnc = mccMnc.substring(3);
            if (mnc.length() == 2) {
                mnc = "0" + mnc;
            }
        }
        return prefix + imsi + "@wlan.mnc" + mnc + ".mcc" + mcc + ".3gppnetwork.org";
    }

    public static boolean isSimConfig(WifiConfiguration config) {
        if (config == null || config.enterpriseConfig == null) {
            return false;
        }
        return isSimEapMethod(config.enterpriseConfig.getEapMethod());
    }

    public static boolean isSimEapMethod(int eapMethod) {
        if (eapMethod == 4 || eapMethod == 5 || eapMethod == 6) {
            return true;
        }
        return false;
    }

    public static int getSubId(int simSlot) {
        int[] subIds = SubscriptionManager.getSubId(simSlot);
        if (subIds != null) {
            return subIds[0];
        }
        return -1;
    }
}
