package com.mediatek.internal.telephony.dataconnection;

import android.os.Bundle;
import android.telephony.Rlog;
import android.telephony.data.ApnSetting;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.dataconnection.ApnSettingUtils;
import com.android.internal.telephony.uicc.IccRecords;
import com.mediatek.internal.telephony.OpTelephonyCustomizationUtils;

public class MtkApnSettingUtils extends ApnSettingUtils {
    private static final boolean DBG = true;
    private static final String LOG_TAG = "MtkApnSettingUtils";
    private static IDataConnectionExt sDataConnectionExt = null;

    private static boolean mvnoMatchesEx(IccRecords r, int mvnoType, String mvnoMatchData) {
        if (mvnoType != 4 || r.isOperatorMvnoForEfPnn() == null || !r.isOperatorMvnoForEfPnn().equalsIgnoreCase(mvnoMatchData)) {
            return false;
        }
        return true;
    }

    private static Bundle isMeteredApnTypeEx(int apnType, Phone phone) {
        boolean isRoaming = phone.getServiceState().getDataRoaming();
        boolean useEx = false;
        boolean result = false;
        if (sDataConnectionExt == null) {
            try {
                sDataConnectionExt = OpTelephonyCustomizationUtils.getOpFactory(phone.getContext()).makeDataConnectionExt(phone.getContext());
            } catch (Exception e) {
                Rlog.e(LOG_TAG, "sDataConnectionExt init fail. e: " + e);
                sDataConnectionExt = null;
            }
        }
        IDataConnectionExt iDataConnectionExt = sDataConnectionExt;
        if (iDataConnectionExt != null && iDataConnectionExt.isMeteredApnTypeByLoad()) {
            useEx = true;
            result = sDataConnectionExt.isMeteredApnType(ApnSetting.getApnTypeString(apnType), isRoaming);
        }
        Bundle b = new Bundle();
        b.putBoolean("useEx", useEx);
        b.putBoolean("result", result);
        return b;
    }
}
