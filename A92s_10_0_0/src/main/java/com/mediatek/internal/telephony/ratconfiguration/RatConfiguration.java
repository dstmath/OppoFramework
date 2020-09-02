package com.mediatek.internal.telephony.ratconfiguration;

import android.os.SystemProperties;
import android.telephony.Rlog;

public class RatConfiguration {
    static final String CDMA = "C";
    static final String DELIMITER = "/";
    static final String GSM = "G";
    private static final String LOG_TAG = "RatConfig";
    static final String LteFdd = "Lf";
    static final String LteTdd = "Lt";
    public static final int MASK_CDMA = 32;
    public static final int MASK_GSM = 1;
    public static final int MASK_LteFdd = 16;
    public static final int MASK_LteTdd = 8;
    public static final int MASK_NR = 64;
    public static final int MASK_TDSCDMA = 2;
    public static final int MASK_WCDMA = 4;
    protected static final int MD_MODE_LCTG = 16;
    protected static final int MD_MODE_LFWCG = 15;
    protected static final int MD_MODE_LFWG = 14;
    protected static final int MD_MODE_LTCTG = 17;
    protected static final int MD_MODE_LTG = 8;
    protected static final int MD_MODE_LTTG = 13;
    protected static final int MD_MODE_LWCG = 11;
    protected static final int MD_MODE_LWCTG = 12;
    protected static final int MD_MODE_LWG = 9;
    protected static final int MD_MODE_LWTG = 10;
    protected static final int MD_MODE_UNKNOWN = 0;
    static final String NR = "N";
    static final String PROPERTY_BUILD_RAT_CONFIG = "ro.vendor.mtk_protocol1_rat_config";
    static final String PROPERTY_IS_USING_DEFAULT_CONFIG = "ro.boot.opt_using_default";
    static final String PROPERTY_RAT_CONFIG = "ro.vendor.mtk_ps1_rat";
    static final String TDSCDMA = "T";
    static final String WCDMA = "W";
    private static int actived_rat = 0;
    private static boolean is_default_config = true;
    private static int max_rat = 0;
    private static boolean max_rat_initialized = false;

    protected static int ratToBitmask(String rat) {
        int iRat = 0;
        if (rat.contains(CDMA)) {
            iRat = 0 | 32;
        }
        if (rat.contains(LteFdd)) {
            iRat |= 16;
        }
        if (rat.contains(LteTdd)) {
            iRat |= 8;
        }
        if (rat.contains(WCDMA)) {
            iRat |= 4;
        }
        if (rat.contains(TDSCDMA)) {
            iRat |= 2;
        }
        if (rat.contains(GSM)) {
            iRat |= 1;
        }
        if (rat.contains(NR)) {
            return iRat | 64;
        }
        return iRat;
    }

    protected static synchronized int getMaxRat() {
        int i;
        synchronized (RatConfiguration.class) {
            if (!max_rat_initialized) {
                String sMaxRat = SystemProperties.get(PROPERTY_BUILD_RAT_CONFIG, "");
                max_rat = ratToBitmask(sMaxRat);
                is_default_config = SystemProperties.getInt(PROPERTY_IS_USING_DEFAULT_CONFIG, 1) != 0;
                max_rat_initialized = true;
                logd("getMaxRat: initial " + sMaxRat + " " + max_rat);
            }
            i = max_rat;
        }
        return i;
    }

    protected static boolean checkRatConfig(int iRat) {
        int maxrat = getMaxRat();
        if ((iRat | maxrat) == maxrat) {
            return true;
        }
        logd("checkRatConfig: FAIL with " + String.valueOf(iRat));
        return false;
    }

    protected static int getRatConfig() {
        int default_rat_config = getMaxRat();
        if (default_rat_config == 0) {
            actived_rat = 0;
            return actived_rat;
        } else if (is_default_config) {
            actived_rat = default_rat_config;
            return default_rat_config;
        } else {
            String rat = SystemProperties.get(PROPERTY_RAT_CONFIG, "");
            if (rat.length() > 0) {
                actived_rat = ratToBitmask(rat);
                if (!checkRatConfig(actived_rat)) {
                    logd("getRatConfig: invalid PROPERTY_RAT_CONFIG, set to max_rat");
                    actived_rat = getMaxRat();
                }
            } else {
                logd("getRatConfig: ger property PROPERTY_RAT_CONFIG fail, initialize");
                actived_rat = getMaxRat();
            }
            return actived_rat;
        }
    }

    protected static String ratToString(int iRat) {
        String rat = "";
        if ((iRat & 32) == 32) {
            rat = rat + "/C";
        }
        if ((iRat & 16) == 16) {
            rat = rat + "/Lf";
        }
        if ((iRat & 8) == 8) {
            rat = rat + "/Lt";
        }
        if ((iRat & 4) == 4) {
            rat = rat + "/W";
        }
        if ((iRat & 2) == 2) {
            rat = rat + "/T";
        }
        if ((iRat & 1) == 1) {
            rat = rat + "/G";
        }
        if ((iRat & 64) == 64) {
            rat = rat + "/N";
        }
        if (rat.length() > 0) {
            return rat.substring(1);
        }
        return rat;
    }

    public static boolean isC2kSupported() {
        return ((getMaxRat() & getRatConfig()) & 32) == 32;
    }

    public static boolean isLteFddSupported() {
        return ((getMaxRat() & getRatConfig()) & 16) == 16;
    }

    public static boolean isLteTddSupported() {
        return ((getMaxRat() & getRatConfig()) & 8) == 8;
    }

    public static boolean isWcdmaSupported() {
        return ((getMaxRat() & getRatConfig()) & 4) == 4;
    }

    public static boolean isTdscdmaSupported() {
        return ((getMaxRat() & getRatConfig()) & 2) == 2;
    }

    public static boolean isGsmSupported() {
        return ((getMaxRat() & getRatConfig()) & 1) == 1;
    }

    public static boolean isNrSupported() {
        return ((getMaxRat() & getRatConfig()) & 64) == 64;
    }

    public static String getActiveRatConfig() {
        String rat = ratToString(getRatConfig());
        logd("getActiveRatConfig: " + rat);
        return rat;
    }

    private static void logd(String msg) {
        Rlog.d(LOG_TAG, msg);
    }
}
