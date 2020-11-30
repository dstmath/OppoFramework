package com.oppo.enterprise.mdmcoreservice.utils.defaultapp;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import com.oppo.enterprise.mdmcoreservice.utils.AppTypeUtil;
import com.oppo.enterprise.mdmcoreservice.utils.defaultapp.entity.DefaultAppActivityInfo;
import com.oppo.enterprise.mdmcoreservice.utils.permission.DataBaseUtil;
import java.util.ArrayList;
import java.util.List;

public abstract class DefaultApp {
    protected static final String TAG = "DefaultApp";
    private static final Uri sBackUpAuthorityUri = Uri.parse("content://com.oppo.permissioncontroller.defaultapp.BackupProvider");
    private static final Uri sBackUpUri = Uri.withAppendedPath(Uri.parse("content://com.color.provider.SafeProvider"), DataBaseUtil.Settings.TABLE_SETTINGS);
    private static Uri sBackUpUriQ = Uri.withAppendedPath(sBackUpAuthorityUri, "backup");
    private static final int sVersionCodeQ = 29;
    private List<DefaultAppActivityInfo> mAppActivityInfoList;
    protected Context mContext;

    public abstract String getAppTypeKey();

    public abstract String getDefaultPackage(PackageManager packageManager);

    /* access modifiers changed from: protected */
    public abstract List<IntentFilter> getFilterList();

    /* access modifiers changed from: protected */
    public abstract List<Intent> getIntentList();

    /* access modifiers changed from: protected */
    public abstract List<Integer> getMatchList();

    public DefaultApp(Context context) {
        this.mContext = context;
    }

    public List<DefaultAppActivityInfo> getAppInfoList() {
        if (this.mAppActivityInfoList != null) {
            return this.mAppActivityInfoList;
        }
        this.mAppActivityInfoList = new ArrayList();
        PackageManager pm = this.mContext.getPackageManager();
        for (Intent intent : getIntentList()) {
            DefaultAppActivityInfo appActivityInfo = new DefaultAppActivityInfo();
            for (ResolveInfo ri : getResolveInfoList(intent, pm)) {
                appActivityInfo.addActivityInfo(ri.activityInfo);
                appActivityInfo.addPriority(ri.priority);
            }
            this.mAppActivityInfoList.add(appActivityInfo);
        }
        return this.mAppActivityInfoList;
    }

    /* access modifiers changed from: protected */
    public List<ResolveInfo> getResolveInfoList(Intent intent, PackageManager pm) {
        try {
            return pm.queryIntentActivities(intent, 65536);
        } catch (Exception e) {
            List<ResolveInfo> resolveInfoList = new ArrayList<>();
            e.printStackTrace();
            return resolveInfoList;
        }
    }

    /* JADX INFO: Multiple debug info for r4v6 java.lang.String: [D('activityInfoList' java.util.List<android.content.pm.ActivityInfo>), D('packageName' java.lang.String)] */
    /* JADX INFO: Multiple debug info for r14v7 java.lang.String: [D('changed' boolean), D('className' java.lang.String)] */
    /* JADX INFO: Multiple debug info for r5v7 android.content.ComponentName: [D('activityInfo' android.content.pm.ActivityInfo), D('componentName' android.content.ComponentName)] */
    public boolean setDefaultApp(String targetPackage) {
        ComponentName[] componentNames;
        PackageManager pm = this.mContext.getPackageManager();
        String currentDefault = getDefaultPackage(pm);
        Log.d(TAG, "setDefaultApp, currentDefault=" + currentDefault + ", targetPackage=" + targetPackage);
        if (currentDefault != null && currentDefault.equals(targetPackage)) {
            return true;
        }
        List<IntentFilter> filterList = getFilterList();
        List<Integer> matchList = getMatchList();
        List<DefaultAppActivityInfo> appInfoList = getAppInfoList();
        boolean hasCleared = false;
        boolean changed = false;
        int i = 0;
        while (i < filterList.size()) {
            DefaultAppActivityInfo appActivityInfo = appInfoList.get(i);
            List<ActivityInfo> activityInfoList = appActivityInfo.getActivityInfo();
            List<Integer> priorityList = appActivityInfo.getPriorityList();
            ComponentName[] componentNames2 = new ComponentName[activityInfoList.size()];
            ComponentName bestActivity = null;
            int maxPrioity = Integer.MIN_VALUE;
            int j = 0;
            while (j < activityInfoList.size()) {
                ActivityInfo activityInfo = activityInfoList.get(j);
                String packageName = activityInfo.packageName;
                String className = activityInfo.name;
                ComponentName componentName = new ComponentName(packageName, className);
                componentNames2[j] = componentName;
                if (targetPackage.equals(packageName)) {
                    componentNames = componentNames2;
                    if (priorityList.get(j).intValue() > maxPrioity) {
                        bestActivity = componentName;
                        maxPrioity = priorityList.get(j).intValue();
                    }
                } else {
                    componentNames = componentNames2;
                }
                Log.d(TAG, "setDefaultApp, index=" + j + ",packageName=" + packageName + ", className=" + className + ", priority=" + priorityList.get(j));
                j++;
                appActivityInfo = appActivityInfo;
                activityInfoList = activityInfoList;
                changed = changed;
                componentNames2 = componentNames;
                maxPrioity = maxPrioity;
            }
            Log.d(TAG, "setDefaultApp, bestActivity=" + bestActivity + ", maxPrioity=" + maxPrioity);
            if (bestActivity != null) {
                if (!hasCleared) {
                    clearPreferredActivity(pm, currentDefault);
                    hasCleared = true;
                }
                setPreferredActivity(pm, filterList.get(i), matchList.get(i).intValue(), componentNames2, bestActivity);
                setDefaultAppPackageName(this.mContext, getAppTypeKey(), targetPackage);
                hasCleared = hasCleared;
                changed = true;
            } else {
                changed = changed;
            }
            i++;
            appInfoList = appInfoList;
        }
        return changed;
    }

    /* JADX INFO: Multiple debug info for r4v5 java.lang.String: [D('activityInfoList' java.util.List<android.content.pm.ActivityInfo>), D('packageName' java.lang.String)] */
    /* JADX INFO: Multiple debug info for r1v12 java.lang.String: [D('changed' boolean), D('className' java.lang.String)] */
    /* JADX INFO: Multiple debug info for r5v7 android.content.ComponentName: [D('activityInfo' android.content.pm.ActivityInfo), D('componentName' android.content.ComponentName)] */
    public boolean setDefaultApp(ComponentName home) {
        int i = 0;
        if (home == null) {
            return false;
        }
        String targetPackage = home.getPackageName();
        PackageManager pm = this.mContext.getPackageManager();
        String currentDefault = getDefaultPackage(pm);
        Log.d(TAG, "setDefaultApp, currentDefault=" + currentDefault + ", home=" + home);
        List<IntentFilter> filterList = getFilterList();
        List<Integer> matchList = getMatchList();
        List<DefaultAppActivityInfo> appInfoList = getAppInfoList();
        boolean changed = false;
        boolean hasCleared = false;
        int i2 = 0;
        while (i2 < filterList.size()) {
            DefaultAppActivityInfo appActivityInfo = appInfoList.get(i2);
            List<ActivityInfo> activityInfoList = appActivityInfo.getActivityInfo();
            List<Integer> priorityList = appActivityInfo.getPriorityList();
            ComponentName[] componentNames = new ComponentName[activityInfoList.size()];
            int j = i;
            boolean componentNameMatched = false;
            while (j < activityInfoList.size()) {
                ActivityInfo activityInfo = activityInfoList.get(j);
                String packageName = activityInfo.packageName;
                String className = activityInfo.name;
                ComponentName componentName = new ComponentName(packageName, className);
                componentNames[j] = componentName;
                if (home.equals(componentName)) {
                    componentNameMatched = true;
                }
                Log.d(TAG, "setDefaultApp, index=" + j + ",packageName=" + packageName + ", className=" + className + ", priority=" + priorityList.get(j));
                j++;
                appActivityInfo = appActivityInfo;
                appInfoList = appInfoList;
                activityInfoList = activityInfoList;
                changed = changed;
                componentNames = componentNames;
                componentNameMatched = componentNameMatched;
            }
            Log.d(TAG, "setDefaultApp, componentNameMatched=" + componentNameMatched);
            if (componentNameMatched) {
                if (!hasCleared) {
                    clearPreferredActivity(pm, currentDefault);
                    hasCleared = true;
                }
                setPreferredActivity(pm, filterList.get(i2), matchList.get(i2).intValue(), componentNames, home);
                setDefaultAppPackageName(this.mContext, getAppTypeKey(), targetPackage);
                changed = true;
                hasCleared = hasCleared;
            } else {
                changed = changed;
            }
            i2++;
            appInfoList = appInfoList;
            i = 0;
        }
        return changed;
    }

    /* access modifiers changed from: protected */
    public void setPreferredActivity(PackageManager pm, IntentFilter filter, int match, ComponentName[] componentNames, ComponentName activity) {
        pm.addPreferredActivity(filter, match, componentNames, activity);
    }

    /* access modifiers changed from: protected */
    public void clearPreferredActivity(PackageManager pm, String currentDefault) {
        pm.clearPackagePreferredActivities(currentDefault);
        Log.d(TAG, "clearPreferredActivity, pkg=" + currentDefault);
    }

    public static void setDefaultAppPackageName(Context context, String appType, String pkgName) {
        if (AppTypeUtil.KEY_DEFAULT_APP_DESKTOP.equals(appType)) {
            saveChangeToSafeCenter(context, AppTypeUtil.PP_DEFAULT_DESKTOP, pkgName);
        } else if (AppTypeUtil.KEY_DEFAULT_APP_MESSAGE.equals(appType)) {
            saveChangeToSafeCenter(context, AppTypeUtil.PP_DEFAULT_MESSAGE, pkgName);
        } else if (AppTypeUtil.KEY_DEFAULT_APP_DIAL.equals(appType)) {
            saveChangeToSafeCenter(context, AppTypeUtil.PP_DEFAULT_DIAL, pkgName);
        } else if (AppTypeUtil.KEY_DEFAULT_APP_BROWSER.equals(appType)) {
            saveChangeToSafeCenter(context, AppTypeUtil.PP_DEFAULT_BROWSER, pkgName);
        } else if (AppTypeUtil.KEY_DEFAULT_APP_EMAIL.equals(appType)) {
            saveChangeToSafeCenter(context, AppTypeUtil.PP_DEFAULT_EMAIL, pkgName);
        }
    }

    private static void saveChangeToSafeCenter(Context context, String key, String value) {
        Uri uri;
        ContentValues cv = new ContentValues();
        cv.put(DataBaseUtil.Settings.COLUMN_MAIN_VALUE, value);
        if (isSdkVersionQ()) {
            uri = sBackUpUriQ;
        } else {
            uri = sBackUpUri;
        }
        try {
            if (context.getContentResolver().update(uri, cv, "key= ?", new String[]{key}) == 0) {
                cv.put(DataBaseUtil.Settings.COLUMN_MAIN_KEY, key);
                context.getContentResolver().insert(uri, cv);
            }
            cv.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean isSdkVersionQ() {
        return Build.VERSION.SDK_INT == 29;
    }
}
