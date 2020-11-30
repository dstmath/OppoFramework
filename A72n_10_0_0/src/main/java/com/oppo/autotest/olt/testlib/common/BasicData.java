package com.oppo.autotest.olt.testlib.common;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.SystemProperties;
import androidx.test.platform.app.InstrumentationRegistry;

public class BasicData {
    public static final String ANDROID_VERSION = Build.VERSION.RELEASE;
    public static final String APP_VERSION = getAppVersion();
    public static final String CONFIG_PATH = (PROJECT_PATH + "/Config/");
    public static final String DEVICE_MODEL = Build.MODEL;
    public static final String DEVICE_VERSION = getDeviceVersion();
    public static final String IMEI1 = getIMEI(0);
    public static final String IMEI2 = getIMEI(1);
    public static final boolean IS_QCOM = SystemProperties.get("ro.hardware").contains("qcom");
    public static final String LOG_ALL_PATH = (PROJECT_PATH + "/Log/");
    public static boolean MultThreadRunSwitch = false;
    public static final String OS_VERSION = SystemProperties.get("ro.build.version.opporom");
    public static final String OTA_VERSION = SystemProperties.get("ro.build.version.ota");
    public static final String PACKAGE_NAME = getGlobalContext().getPackageName();
    public static final String PLATFORM = (IS_QCOM ? "高通" : "MTK");
    public static final String PLATFORM_DETAIL = SystemProperties.get("ro.board.platform");
    public static final String PROJECT_CODE = getProjectCode();
    public static final String PROJECT_PATH = (SDCARD_PATH + "/OLT/");
    public static final String REGION = (getLocale().contains("zh") ? "内销" : "外销");
    public static final String RESULT_PATH = (PROJECT_PATH + "/Result/");
    public static final String SDCARD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
    public static final String TARGET_PACKAGE_NAME = getTargetContext().getPackageName();
    public static final String TEST_PIC_PATH = (PROJECT_PATH + "/TestPic/");
    public static String sReportTime = "";
    public static String sTestCaseName = "unknown";

    public static void setTestCaseName(String testCaseName) {
        sTestCaseName = testCaseName;
    }

    public static void setReportTime(String reportTime) {
        sReportTime = reportTime;
    }

    private static String getAppVersion() {
        try {
            return getGlobalContext().getPackageManager().getPackageInfo(PACKAGE_NAME, 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "未知";
        }
    }

    public static Context getGlobalContext() {
        return InstrumentationRegistry.getInstrumentation().getContext();
    }

    public static Context getTargetContext() {
        return InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    private static String getProjectCode() {
        String originalVersion;
        String projectCode = SystemProperties.get("ro.separate.soft", "未知");
        if (projectCode.contains("未知")) {
            if (IS_QCOM) {
                originalVersion = SystemProperties.get("ro.build.display.full_id");
            } else {
                originalVersion = SystemProperties.get("ro.mediatek.version.release");
            }
            return originalVersion.split("_")[0];
        }
        String[] projectCodeStr = projectCode.split("_");
        return projectCodeStr[projectCodeStr.length - 1];
    }

    private static String getDeviceVersion() {
        String originalVersion;
        if (IS_QCOM) {
            originalVersion = SystemProperties.get("ro.build.display.full_id");
        } else {
            originalVersion = SystemProperties.get("ro.mediatek.version.release");
        }
        String[] versionStr = originalVersion.split("_");
        String version = "";
        if (versionStr != null) {
            for (int i = 1; i < versionStr.length; i++) {
                version = version + "_" + versionStr[i];
            }
        }
        if (OTA_VERSION.length() >= 4) {
            return PROJECT_CODE + version + OTA_VERSION.substring(OTA_VERSION.length() - 4);
        }
        return PROJECT_CODE + version;
    }

    private static String getLocale() {
        String str = SystemProperties.get("ro.product.locale");
        if ("".equals(str)) {
            return SystemProperties.get("ro.product.locale.language");
        }
        return str;
    }

    private static String getIMEI(int sim) {
        return "null";
    }
}
