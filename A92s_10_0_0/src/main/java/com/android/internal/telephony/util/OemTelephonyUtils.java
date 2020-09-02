package com.android.internal.telephony.util;

import android.content.Context;
import android.os.PersistableBundle;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.CarrierConfigManager;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
import com.android.internal.telephony.OemConstant;
import com.android.internal.telephony.OemFeature;
import com.android.internal.telephony.OemSimProtect;
import com.android.internal.telephony.OppoModemLogManager;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConfigurationManager;

public class OemTelephonyUtils {
    public static final int CDMA_1X_FIXED_ON_BOTTOM_ANT_BIT = 17;
    public static final String DEEP_SLEEP_URI = "oppoguaedelf_deep_sleep_status";
    public static final boolean FEATURE_SCREEN_SOLUTION = SystemProperties.get("ro.product.oppo.nw_scr_solution", OppoModemLogManager.DEFAULT_MODEMDUMP_POSTBACK).equals("1");
    public static final int FOUR_WAY_ASDIV_BIT = 5;
    public static final int NR_FIX_ANT_FEATRUE = 6;
    public static final int SAR_BATTERY_BIT = 11;
    public static final int SAR_CAM_BIT = 12;
    public static final int SAR_CE_BIT = 7;
    public static final int SAR_DISPLAY_BIT = 13;
    public static final int SAR_DISPLAY_EXP_BIT = 14;
    public static final int SAR_DP_BIT = 10;
    public static final int SAR_HEAD_BIT = 8;
    public static final int SAR_RECEIVER_BIT = 16;
    public static final int SAR_SENSOR_BIT = 0;
    public static final int SAR_USB_BIT = 9;
    public static final int SAR_WIFI_2P45G_BIT = 15;
    private static final String SYS_NW_RF_FEATURE = "sys.oppo.network.rffeature";
    private static final String TAG = "oem";
    public static final int THREE_WAY_ASDIV_BIT = 4;
    private static boolean mPowerBackOffEnable = true;
    private static String mRfFeature = OppoModemLogManager.DEFAULT_MODEMDUMP_POSTBACK;

    public static boolean isEuexOpenMarket() {
        String regionmark = SystemProperties.get("ro.oppo.regionmark", PhoneConfigurationManager.SSSS);
        String operator = SystemProperties.get("ro.oppo.operator", PhoneConfigurationManager.SSSS);
        String country = SystemProperties.get("ro.oppo.euex.country", PhoneConfigurationManager.SSSS);
        if (TextUtils.isEmpty(regionmark) || !"EUEX".equals(regionmark) || !TextUtils.isEmpty(operator) || !TextUtils.isEmpty(country)) {
            return false;
        }
        return true;
    }

    public static boolean isCTS(Context context) {
        return context.getPackageManager().hasSystemFeature("persist.oppo.ctsversion");
    }

    public static boolean isCTA(Context context) {
        return context.getPackageManager().hasSystemFeature("oppo.cta.support");
    }

    public static boolean isNwLabTest() {
        return SystemProperties.get("persist.sys.nw_lab_test", OppoModemLogManager.DEFAULT_MODEMDUMP_POSTBACK).equals("1");
    }

    public static boolean isVersionMOVISTAR() {
        try {
            return "MOVISTAR".equals(SystemProperties.get("ro.oppo.operator", PhoneConfigurationManager.SSSS));
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public static boolean isTestCard(Context context, byte b) {
        return b == 128 || b == 129 || ((b == 2 || b == 4) && isNwLabTest());
    }

    public static boolean isTestCard(String mImsi) {
        return (mImsi != null && (mImsi.startsWith("00101") || mImsi.startsWith("001001") || mImsi.equals("111223333333333"))) || OemConstant.FEATURE_TEST_SIM;
    }

    public static void printStack(String msg) {
        if (OemFeature.FEATURE_COMM_STACK) {
            Rlog.e(TAG, "oem stack", new Throwable(msg));
        }
    }

    public static void printStack(boolean isPrint, String msg) {
        if (isPrint || OemFeature.FEATURE_COMM_STACK) {
            Rlog.e(TAG, "oem stack", new Throwable(msg));
        }
    }

    public static boolean isDumpOff() {
        try {
            return SystemProperties.get("persist.sys.dump", "1").equals(OppoModemLogManager.DEFAULT_MODEMDUMP_POSTBACK);
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isVsimIgnoreUserDataSetting(Context context) {
        return context.getPackageManager().hasSystemFeature("oppo.softsim.ignore_data_setting");
    }

    public static boolean needUseMotor() {
        return false;
    }

    public static boolean needSetSarForCeTest() {
        return hasRfFeature(7);
    }

    public static boolean needSetSarForHeadSet() {
        return hasRfFeature(8);
    }

    public static boolean needSetSarForUSB() {
        return hasRfFeature(9);
    }

    public static boolean needSetSarForDP() {
        return hasRfFeature(10);
    }

    public static boolean needSetSarForBattery() {
        return hasRfFeature(11);
    }

    public static boolean needSetSarForCam() {
        return hasRfFeature(12);
    }

    public static boolean needSetWifiSar() {
        if (SystemProperties.get("ro.separate.soft", "oppo").equals("18397") || SystemProperties.get("ro.separate.soft", "oppo").equals("18383") || SystemProperties.get("ro.separate.soft", "oppo").equals("18539")) {
            return true;
        }
        return false;
    }

    public static boolean needSetSarForDisplay() {
        Rlog.d(TAG, "FEATURE_SCREEN_SOLUTION: " + FEATURE_SCREEN_SOLUTION);
        return hasRfFeature(13);
    }

    public static boolean needSetSarForDisplayExp() {
        Rlog.d(TAG, "FEATURE_SCREEN_SOLUTION: " + FEATURE_SCREEN_SOLUTION);
        return hasRfFeature(14);
    }

    public static boolean needSetSarForDistinguishWifi2P4G_5G() {
        return hasRfFeature(15);
    }

    public static boolean isSimProtectContact(Context context, String packageName) {
        return OemSimProtect.getInstance().isSimProtectContact(context, packageName);
    }

    public static boolean isSimProtectSms(Context context, String packageName) {
        return OemSimProtect.getInstance().isSimProtectSms(context, packageName);
    }

    public static String getOemRes(Context context, String resName, String defValue) {
        try {
            return context.getString(context.getResources().getIdentifier(resName, "string", "oppo"));
        } catch (Exception e) {
            return defValue;
        }
    }

    public static String[] getOemResStrings(Context context, String resName, String[] defValue) {
        try {
            return context.getResources().getStringArray(context.getResources().getIdentifier(resName, "array", "oppo"));
        } catch (Exception e) {
            return defValue;
        }
    }

    public static boolean getPowerBackOffEnable() {
        return mPowerBackOffEnable;
    }

    public static void setPowerBackOffEnable(boolean enable) {
        mPowerBackOffEnable = enable;
    }

    public static <T> T typeCasting(Class<T> type, Object object) {
        if (object == null || !type.isInstance(object)) {
            return null;
        }
        return object;
    }

    public static boolean isInCnList(Context context, String spn) {
        int i = 0;
        if (TextUtils.isEmpty(spn)) {
            return false;
        }
        boolean isCnList = false;
        String[] plmn_list = getOemResStrings(context, "oppo_cn_operator_list", null);
        if (plmn_list != null) {
            int length = plmn_list.length;
            while (true) {
                if (i >= length) {
                    break;
                } else if (spn.equalsIgnoreCase(plmn_list[i])) {
                    isCnList = true;
                    break;
                } else {
                    i++;
                }
            }
        }
        logd("isInCnList " + isCnList + "  spn: " + spn);
        return isCnList;
    }

    public static boolean isInCmccList(Context context, String spn) {
        int i = 0;
        if (TextUtils.isEmpty(spn)) {
            return false;
        }
        boolean isCmccList = false;
        String[] plmn_list = getOemResStrings(context, "oppo_cmcc_operator_list", null);
        if (plmn_list != null) {
            int length = plmn_list.length;
            while (true) {
                if (i >= length) {
                    break;
                } else if (spn.equalsIgnoreCase(plmn_list[i])) {
                    isCmccList = true;
                    break;
                } else {
                    i++;
                }
            }
        }
        logd("isInCmccList " + isCmccList + "  spn: " + spn);
        return isCmccList;
    }

    static void logd(String s) {
        Rlog.d(TAG, s);
    }

    public static boolean hasRfFeature(int bitMask) {
        mRfFeature = SystemProperties.get(SYS_NW_RF_FEATURE, PhoneConfigurationManager.SSSS);
        String str = mRfFeature;
        if (str == null || PhoneConfigurationManager.SSSS.equals(str) || OppoModemLogManager.DEFAULT_MODEMDUMP_POSTBACK == mRfFeature) {
            return false;
        }
        Rlog.e(TAG, "mRfFeature : " + mRfFeature);
        if ((Long.rotateLeft(1, bitMask) & Long.parseLong(mRfFeature)) != 0) {
            return true;
        }
        return false;
    }

    public static void disableRfFeature(int bitMask) {
        mRfFeature = SystemProperties.get(SYS_NW_RF_FEATURE, PhoneConfigurationManager.SSSS);
        String str = mRfFeature;
        if (str != null && !PhoneConfigurationManager.SSSS.equals(str) && OppoModemLogManager.DEFAULT_MODEMDUMP_POSTBACK != mRfFeature) {
            Rlog.e(TAG, "mRfFeature : " + mRfFeature);
            mRfFeature = Long.toString(Long.parseLong(mRfFeature) & (~Long.rotateLeft(1, bitMask)));
            SystemProperties.set(SYS_NW_RF_FEATURE, mRfFeature);
            Rlog.e(TAG, "After disable bit " + bitMask + " mRfFeature : " + mRfFeature);
        }
    }

    public static void enableRfFeature(int bitMask) {
        mRfFeature = SystemProperties.get(SYS_NW_RF_FEATURE, PhoneConfigurationManager.SSSS);
        String str = mRfFeature;
        if (str != null && !PhoneConfigurationManager.SSSS.equals(str)) {
            Rlog.e(TAG, "mRfFeature : " + mRfFeature);
            mRfFeature = Long.toString(Long.parseLong(mRfFeature) | Long.rotateLeft(1, bitMask));
            SystemProperties.set(SYS_NW_RF_FEATURE, mRfFeature);
            Rlog.e(TAG, "After enable bit " + bitMask + " mRfFeature : " + mRfFeature);
        }
    }

    public static String getReadTeaServiceProviderName(Context context, String providerName) {
        if (!TextUtils.isEmpty(providerName)) {
            return getOemRes(context, "redtea_virtul_card", PhoneConfigurationManager.SSSS);
        }
        return PhoneConfigurationManager.SSSS;
    }

    public static boolean isCTcardAsDefaultDataSubscription(Phone phone) {
        if (!OemConstant.isCtCard(phone) || phone.getSubId() == SubscriptionManager.getDefaultDataSubscriptionId()) {
            return false;
        }
        return true;
    }

    public static boolean getBooleanCarrierConfig(Context context, String key, int subId) {
        if (context == null || TextUtils.isEmpty(key)) {
            Rlog.e(TAG, "getBooleanCarrierConfig return false for context is null or key is null!");
            return false;
        }
        CarrierConfigManager configManager = (CarrierConfigManager) context.getSystemService("carrier_config");
        Rlog.d(TAG, "getBooleanCarrierConfig: subId=" + subId + " key = " + key);
        PersistableBundle b = null;
        if (configManager != null) {
            b = configManager.getConfigForSubId(subId);
        }
        if (b != null) {
            return b.getBoolean(key);
        }
        return CarrierConfigManager.getDefaultConfig().getBoolean(key);
    }

    public static boolean isInDeepSleepStatus(Context context) {
        int deepSleepStatus = 0;
        boolean result = false;
        if (context != null) {
            deepSleepStatus = Settings.System.getInt(context.getContentResolver(), DEEP_SLEEP_URI, 0);
        }
        if (1 == deepSleepStatus) {
            result = true;
        }
        logd("isInDeepSleepStatus, deepSleepStatus: " + deepSleepStatus + " ,  result: " + result);
        return result;
    }
}
