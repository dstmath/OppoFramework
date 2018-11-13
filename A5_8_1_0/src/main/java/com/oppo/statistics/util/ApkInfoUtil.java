package com.oppo.statistics.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public class ApkInfoUtil {
    public static String getPackageName(Context context) {
        String packageName = AccountUtil.SSOID_DEFAULT;
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).packageName;
        } catch (Throwable e) {
            LogUtil.e("NearMeStatistics", e);
            return packageName;
        }
    }

    public static String getAppName(Context context) {
        String packageName = AccountUtil.SSOID_DEFAULT;
        try {
            PackageManager manager = context.getPackageManager();
            return manager.getPackageInfo(context.getPackageName(), 0).applicationInfo.loadLabel(manager).toString();
        } catch (Throwable e) {
            LogUtil.e("NearMeStatistics", e);
            return packageName;
        }
    }

    public static String getVersionName(Context context) {
        String versionName = AccountUtil.SSOID_DEFAULT;
        try {
            PackageInfo pkgInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            if (pkgInfo == null || pkgInfo.versionName == null) {
                return versionName;
            }
            versionName = pkgInfo.versionName;
            LogUtil.i("versionName=" + versionName);
            return versionName;
        } catch (Throwable e) {
            LogUtil.e("NearMeStatistics", e);
            return versionName;
        }
    }

    public static int getVersionCode(Context context) {
        int versionCode = 0;
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (Throwable e) {
            LogUtil.e("NearMeStatistics", e);
            return versionCode;
        }
    }

    public static int getVersionCode(Context context, String pkgName) {
        int versionCode = 0;
        try {
            return context.getPackageManager().getPackageInfo(pkgName, 0).versionCode;
        } catch (Throwable e) {
            LogUtil.e("NearMeStatistics", e);
            return versionCode;
        }
    }

    public static int getAppCode(Context context) {
        int appCode = 0;
        try {
            appCode = context.getPackageManager().getApplicationInfo(getPackageName(context), 128).metaData.getInt("AppCode");
            if (appCode == 0) {
                LogUtil.e("NearMeStatistics", "AppCode not set. please read the document of NearMeStatistics SDK.");
            }
        } catch (Throwable e) {
            LogUtil.e("NearMeStatistics", e);
        }
        return appCode;
    }
}
