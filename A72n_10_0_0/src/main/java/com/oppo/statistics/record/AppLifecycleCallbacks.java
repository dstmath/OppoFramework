package com.oppo.statistics.record;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

public class AppLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {
    private int mActivityCount;

    private static class Holder {
        private static final AppLifecycleCallbacks INSTANCE = new AppLifecycleCallbacks();

        private Holder() {
        }
    }

    private AppLifecycleCallbacks() {
        this.mActivityCount = 0;
    }

    public static AppLifecycleCallbacks getInstance() {
        return Holder.INSTANCE;
    }

    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    }

    public void onActivityStarted(Activity activity) {
        this.mActivityCount++;
        Log.d("DCS-", "test onActivityStarted mActivityCount=" + this.mActivityCount);
    }

    public void onActivityResumed(Activity activity) {
        Log.d("DCS-", "test onActivityResumed mActivityCount=" + this.mActivityCount);
        if (isAppBoot()) {
            StatIdManager.getInstance().refreshAppSessionIdIfNeed(activity.getApplicationContext());
        }
    }

    public void onActivityPaused(Activity activity) {
    }

    public void onActivityStopped(Activity activity) {
        this.mActivityCount--;
        Log.d("DCS-", "test onActivityStopped mActivityCount=" + this.mActivityCount);
        if (isAppExit()) {
            StatIdManager.getInstance().onAppExit(activity.getApplicationContext());
        }
    }

    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    public void onActivityDestroyed(Activity activity) {
    }

    private boolean isAppBoot() {
        return this.mActivityCount == 1;
    }

    private boolean isAppExit() {
        return this.mActivityCount == 0;
    }
}
