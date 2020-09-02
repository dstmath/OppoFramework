package com.color.inner.content.pm;

import android.content.pm.ApplicationInfo;
import android.content.pm.OppoApplicationInfoEx;

public class ApplicationInfoWrapper {
    private static final String TAG = "ApplicationInfoWrapper";

    private ApplicationInfoWrapper() {
    }

    public static int getVersionCode(ApplicationInfo appInfo) {
        return appInfo.versionCode;
    }

    public static void setVersionCode(ApplicationInfo appInfo, int versionCode) {
        appInfo.versionCode = versionCode;
    }

    public static long getLongVersionCode(ApplicationInfo appInfo) {
        return appInfo.longVersionCode;
    }

    public static void setLongVersionCode(ApplicationInfo appInfo, long versionCode) {
        appInfo.setVersionCode(versionCode);
    }

    public static boolean isSystemApp(ApplicationInfo appInfo) {
        return appInfo.isSystemApp();
    }

    public static String getBaseCodePath(ApplicationInfo appInfo) {
        return appInfo.getBaseCodePath();
    }

    public static int getColorFreezeState(ApplicationInfo appInfo) {
        OppoApplicationInfoEx oppoAppInfoEx;
        if (appInfo == null || (oppoAppInfoEx = OppoApplicationInfoEx.getOppoAppInfoExFromAppInfoRef(appInfo)) == null) {
            return -1;
        }
        return oppoAppInfoEx.oppoFreezeState;
    }

    public static void setColorFreezeState(ApplicationInfo appInfo, int state) {
        OppoApplicationInfoEx oppoAppInfoEx = OppoApplicationInfoEx.getOppoAppInfoExFromAppInfoRef(appInfo);
        if (oppoAppInfoEx != null) {
            oppoAppInfoEx.oppoFreezeState = state;
        }
    }
}
