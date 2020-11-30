package com.android.server.display.ai.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.text.TextUtils;
import com.color.app.ColorAppEnterInfo;
import com.color.app.ColorAppExitInfo;
import com.color.app.ColorAppSwitchConfig;
import com.color.app.ColorAppSwitchManager;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class AppSwitchDetectorUtil {
    private static String TAG = "AppSwitchDetectorUtil";
    private static volatile AppSwitchDetectorUtil sAppSwitchDetectorUtil;
    private List<IAppSwitchListener> mAppSwitchListeners = new CopyOnWriteArrayList();
    private final Context mContext;
    private final ColorAppSwitchManager.OnAppSwitchObserver mDynamicObserver = new ColorAppSwitchManager.OnAppSwitchObserver() {
        /* class com.android.server.display.ai.utils.AppSwitchDetectorUtil.AnonymousClass1 */

        public void onAppEnter(ColorAppEnterInfo info) {
            String packageName = info.targetName;
            if (!(AppSwitchDetectorUtil.this.mContext == null || TextUtils.isEmpty(packageName))) {
                String str = AppSwitchDetectorUtil.TAG;
                ColorAILog.i(str, "Detect " + packageName + " switch to foreground");
                for (IAppSwitchListener appSwitchListener : AppSwitchDetectorUtil.this.mAppSwitchListeners) {
                    appSwitchListener.onAppSwitchToForeground(packageName);
                }
            }
        }

        public void onAppExit(ColorAppExitInfo info) {
            if (!(info == null || AppSwitchDetectorUtil.this.pkgList == null || AppSwitchDetectorUtil.this.pkgList.isEmpty())) {
                for (String pkgName : AppSwitchDetectorUtil.this.pkgList) {
                    if (!TextUtils.isEmpty(pkgName) && pkgName.equals(info.resumingPackageName)) {
                        ColorAILog.i(AppSwitchDetectorUtil.TAG, "The resuming app has a specified spline. Do nothing.");
                        return;
                    }
                }
            }
            if (AppSwitchDetectorUtil.this.mContext != null) {
                ColorAILog.i(AppSwitchDetectorUtil.TAG, "Switch to default spline");
                for (IAppSwitchListener appSwitchListener : AppSwitchDetectorUtil.this.mAppSwitchListeners) {
                    appSwitchListener.onAppSwitchToForeground(BrightnessConstants.DEFAULT_SPLINE);
                }
            }
        }

        public void onActivityEnter(ColorAppEnterInfo info) {
        }

        public void onActivityExit(ColorAppExitInfo info) {
        }
    };
    private List<String> pkgList;

    public interface IAppSwitchListener {
        void onAppSwitchToForeground(String str);
    }

    private AppSwitchDetectorUtil(Context context) {
        this.mContext = context;
    }

    public static AppSwitchDetectorUtil getInstance(Context context) {
        if (sAppSwitchDetectorUtil == null) {
            synchronized (AppSwitchDetectorUtil.class) {
                if (sAppSwitchDetectorUtil == null) {
                    sAppSwitchDetectorUtil = new AppSwitchDetectorUtil(context);
                }
            }
        }
        return sAppSwitchDetectorUtil;
    }

    public synchronized void register(List<String> pkgList2) {
        if (pkgList2 != null) {
            if (!pkgList2.isEmpty()) {
                this.pkgList = pkgList2;
                String str = TAG;
                ColorAILog.i(str, "Start detecting app switching to foreground event." + pkgList2);
                ColorAppSwitchConfig config = new ColorAppSwitchConfig();
                config.addAppConfig(2, pkgList2);
                try {
                    ColorAppSwitchManager.getInstance().registerAppSwitchObserver(this.mContext, this.mDynamicObserver, config);
                } catch (Exception e) {
                    String str2 = TAG;
                    ColorAILog.e(str2, "Oops! Exception on register: " + e.getMessage());
                }
                return;
            }
        }
        ColorAILog.w(TAG, "No pkgs to listen.");
    }

    public synchronized void unregister() {
        ColorAILog.i(TAG, "Stop detecting app switching to foreground event.");
        this.mAppSwitchListeners.clear();
        try {
            ColorAppSwitchManager.getInstance().unregisterAppSwitchObserver(this.mContext, this.mDynamicObserver);
        } catch (Exception e) {
            String str = TAG;
            ColorAILog.e(str, "Oops! Exception on unregister: " + e.getMessage());
        }
    }

    public void addListener(IAppSwitchListener listener) {
        if (listener != null && !this.mAppSwitchListeners.contains(listener)) {
            this.mAppSwitchListeners.add(listener);
        }
    }

    public List<String> getLauncherAppList(Context context) {
        List<String> launcherAppList = new ArrayList<>();
        PackageManager packageManager = context.getPackageManager();
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        intent.addCategory("android.intent.category.DEFAULT");
        for (ResolveInfo info : packageManager.queryIntentActivities(intent, 65536)) {
            if (!TextUtils.isEmpty(info.activityInfo.packageName)) {
                launcherAppList.add(info.activityInfo.packageName);
            }
        }
        String str = TAG;
        ColorAILog.i(str, "Installed launcher app list : " + launcherAppList.toString());
        if (!launcherAppList.contains("com.oppo.launcher")) {
            launcherAppList.add("com.oppo.launcher");
        }
        return launcherAppList;
    }
}
