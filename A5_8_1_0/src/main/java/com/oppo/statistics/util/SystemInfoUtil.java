package com.oppo.statistics.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Build.VERSION;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.color.os.ColorBuild;
import java.util.regex.Pattern;

@SuppressLint({"DefaultLocale"})
public class SystemInfoUtil {
    private static final Pattern MTK_PATTERN = Pattern.compile("^[MT]{2}[a-zA-Z0-9]{0,10}$");
    private static int STATISTICS_PLATFORM_MTK = 1;
    private static int STATISTICS_PLATFORM_QUALCOMM = 2;
    public static final String SYSTEM_NAME = "Android";

    public static String getModel() {
        String model = AccountUtil.SSOID_DEFAULT;
        if (!isEmpty(Build.MODEL)) {
            return Build.MODEL.toUpperCase();
        }
        LogUtil.w("NearMeStatistics", "No MODEL.");
        return model;
    }

    public static int getPlatForm() {
        if (getHardware().equals("QCOM")) {
            return STATISTICS_PLATFORM_QUALCOMM;
        }
        if (MTK_PATTERN.matcher(getHardware()).find()) {
            return STATISTICS_PLATFORM_MTK;
        }
        return 0;
    }

    public static String getHardware() {
        if (!isEmpty(Build.HARDWARE)) {
            return Build.HARDWARE.toUpperCase();
        }
        LogUtil.w("NearMeStatistics", "No HARDWARE INFO.");
        return AccountUtil.SSOID_DEFAULT;
    }

    public static int getSDKVersion() {
        return VERSION.SDK_INT;
    }

    public static String getRomVersion() {
        return "" + ColorBuild.getColorOSVERSION();
    }

    public static String getLocalPhoneNO(Context context) {
        String phoneNo = AccountUtil.SSOID_DEFAULT;
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService("phone");
            if (isEmpty(tm.getLine1Number())) {
                return phoneNo;
            }
            return tm.getLine1Number();
        } catch (Throwable e) {
            LogUtil.e("NearMeStatistics", e);
            return phoneNo;
        }
    }

    public static String getOperator(Context context) {
        String operator = "";
        try {
            return ((TelephonyManager) context.getSystemService("phone")).getNetworkOperatorName();
        } catch (Throwable e) {
            LogUtil.e("NearMeStatistics", e);
            return operator;
        }
    }

    public static int getOperatorId(Context context) {
        String operator = getOperator(context).toLowerCase();
        if (operator.equals("中国移动") || operator.equals("china mobile") || operator.equals("chinamobile")) {
            return 0;
        }
        if (operator.equals("中国联通") || operator.equals("china unicom") || operator.equals("chinaunicom")) {
            return 1;
        }
        if (operator.equals("中国电信") || operator.equals("china net") || operator.equals("chinanet")) {
            return 2;
        }
        return 99;
    }

    public static String getMacAddress(Context context) {
        String macAddress = AccountUtil.SSOID_DEFAULT;
        try {
            WifiInfo info = ((WifiManager) context.getSystemService("wifi")).getConnectionInfo();
            if (!isEmpty(info.getMacAddress())) {
                return info.getMacAddress();
            }
            LogUtil.w("NearMeStatistics", "NO MAC ADDRESS.");
            return macAddress;
        } catch (Throwable e) {
            LogUtil.e("NearMeStatistics", e);
            return macAddress;
        }
    }

    public static String getMobile(Context context) {
        String result = AccountUtil.SSOID_DEFAULT;
        TelephonyManager tm = (TelephonyManager) context.getSystemService("phone");
        if (!tm.hasIccCard()) {
            return result;
        }
        result = tm.getLine1Number();
        if (TextUtils.isEmpty(result) || result.equals("null")) {
            return AccountUtil.SSOID_DEFAULT;
        }
        return result;
    }

    private static boolean isEmpty(String str) {
        if (TextUtils.isEmpty(str) || "null".equals(str)) {
            return true;
        }
        return false;
    }
}
