package com.mediatek.internal.telephony.datasub;

import android.os.SystemProperties;
import android.telephony.Rlog;
import android.text.TextUtils;
import com.mediatek.internal.telephony.ratconfiguration.RatConfiguration;

public class DataSubSelectorUtil {
    private static boolean DBG = true;
    private static final String LOG_TAG = "DSSelectorUtil";
    public static String[] PROPERTY_ICCID = {"vendor.ril.iccid.sim1", "vendor.ril.iccid.sim2", "vendor.ril.iccid.sim3", "vendor.ril.iccid.sim4"};
    private static DataSubSelector mDataSubSelector = null;
    private static DataSubSelectorUtil mInstance = null;

    public static String getIccidFromProp(int phoneId) {
        return SystemProperties.get(PROPERTY_ICCID[phoneId]);
    }

    public static int getIccidNum() {
        return PROPERTY_ICCID.length;
    }

    public boolean isSimInserted(int phoneId) {
        String iccid = SystemProperties.get(PROPERTY_ICCID[phoneId], "");
        return !TextUtils.isEmpty(iccid) && !DataSubConstants.NO_SIM_VALUE.equals(iccid);
    }

    public static boolean isC2kProject() {
        return RatConfiguration.isC2kSupported();
    }

    public static int getMaxIccIdCount() {
        return PROPERTY_ICCID.length;
    }

    private static void log(String txt) {
        if (DBG) {
            Rlog.d(LOG_TAG, txt);
        }
    }

    private static void loge(String txt) {
        if (DBG) {
            Rlog.e(LOG_TAG, txt);
        }
    }
}
