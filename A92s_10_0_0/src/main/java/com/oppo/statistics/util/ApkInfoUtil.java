package com.oppo.statistics.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public class ApkInfoUtil {
    public static String getPackageName(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).packageName;
        } catch (Exception e) {
            LogUtil.e("NearMeStatistics", e);
            return AccountUtil.SSOID_DEFAULT;
        }
    }

    public static String getAppName(Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            return manager.getPackageInfo(context.getPackageName(), 0).applicationInfo.loadLabel(manager).toString();
        } catch (Exception e) {
            LogUtil.e("NearMeStatistics", e);
            return AccountUtil.SSOID_DEFAULT;
        }
    }

    public static String getVersionName(Context context) {
        try {
            PackageInfo pkgInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            if (pkgInfo == null || pkgInfo.versionName == null) {
                return AccountUtil.SSOID_DEFAULT;
            }
            String versionName = pkgInfo.versionName;
            LogUtil.i("versionName=" + versionName);
            return versionName;
        } catch (Exception e) {
            LogUtil.e("NearMeStatistics", e);
            return AccountUtil.SSOID_DEFAULT;
        }
    }

    public static int getVersionCode(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (Exception e) {
            LogUtil.e("NearMeStatistics", e);
            return 0;
        }
    }

    public static int getVersionCode(Context context, String pkgName) {
        try {
            return context.getPackageManager().getPackageInfo(pkgName, 0).versionCode;
        } catch (Exception e) {
            LogUtil.e("NearMeStatistics", e);
            return 0;
        }
    }

    public static int getAppCode(Context context) {
        int appCode = 0;
        try {
            appCode = context.getPackageManager().getApplicationInfo(getPackageName(context), 128).metaData.getInt("AppCode");
            if (appCode == 0) {
                LogUtil.e("NearMeStatistics", "AppCode not set. please read the document of NearMeStatistics SDK.");
            }
        } catch (Exception e) {
            LogUtil.e("NearMeStatistics", e);
        }
        return appCode;
    }
}
