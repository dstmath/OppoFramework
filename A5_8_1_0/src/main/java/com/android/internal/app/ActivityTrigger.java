package com.android.internal.app;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.util.Log;

public class ActivityTrigger {
    public static final int ANIMATION_SCALE = 3;
    private static final int FLAG_HARDWARE_ACCELERATED = 512;
    private static final int FLAG_OVERRIDE_RESOLUTION = 1;
    public static final int NETWORK_OPTS = 2;
    public static final int START_PROCESS = 1;
    private static final String TAG = "ActivityTrigger";

    private native void native_at_deinit();

    private native float native_at_miscActivity(int i, String str, int i2, int i3);

    private native void native_at_pauseActivity(String str);

    private native void native_at_resumeActivity(String str);

    private native int native_at_startActivity(String str, int i);

    private native void native_at_stopActivity(String str);

    protected void finalize() {
        native_at_deinit();
    }

    public void activityStartTrigger(Intent intent, ActivityInfo acInfo, ApplicationInfo appInfo, boolean IsInFullScreen) {
        ComponentName cn = intent.getComponent();
        String activity = null;
        if (cn != null) {
            activity = cn.flattenToString() + "/" + appInfo.versionCode;
        }
        int overrideFlags = native_at_startActivity(activity, 0);
        if ((overrideFlags & 512) != 0) {
            acInfo.flags |= 512;
        }
        if (IsInFullScreen) {
            Log.d(TAG, "activityStartTrigger: Activity is Triggerred in full screen " + appInfo);
            if ((overrideFlags & 1) != 0) {
                Log.e(TAG, "activityStartTrigger: whiteListed " + activity + " appInfo.flags - " + Integer.toHexString(appInfo.flags));
                appInfo.setAppOverrideDensity();
                appInfo.setAppWhiteListed(1);
                return;
            }
            appInfo.setOverrideDensity(0);
            appInfo.setAppWhiteListed(0);
            Log.e(TAG, "activityStartTrigger: not whiteListed" + activity);
            return;
        }
        Log.d(TAG, "activityStartTrigger: Activity is not Triggerred in full screen " + appInfo);
        appInfo.setOverrideDensity(0);
    }

    public void activityResumeTrigger(Intent intent, ActivityInfo acInfo, ApplicationInfo appInfo, boolean IsInFullScreen) {
        ComponentName cn = intent.getComponent();
        String activity = null;
        if (cn != null) {
            activity = cn.flattenToString() + "/" + appInfo.versionCode;
        }
        native_at_resumeActivity(activity);
        if (IsInFullScreen) {
            Log.d(TAG, "activityResumeTrigger: The activity in " + appInfo + " is now in focus and seems to be in full-screen mode");
            if (appInfo.isAppWhiteListed()) {
                Log.d(TAG, "activityResumeTrigger: whiteListed " + activity + " appInfo.flags - " + Integer.toHexString(appInfo.flags));
                appInfo.setAppOverrideDensity();
                return;
            }
            appInfo.setOverrideDensity(0);
            Log.e(TAG, "activityResumeTrigger: not whiteListed" + activity);
            return;
        }
        Log.d(TAG, "activityResumeTrigger: Activity is not Triggerred in full screen " + appInfo);
        appInfo.setOverrideDensity(0);
    }

    public void activityPauseTrigger(Intent intent, ActivityInfo acInfo, ApplicationInfo appInfo) {
        ComponentName cn = intent.getComponent();
        String activity = null;
        if (!(cn == null || appInfo == null)) {
            activity = cn.flattenToString() + "/" + appInfo.versionCode;
        }
        native_at_pauseActivity(activity);
    }

    public void activityStopTrigger(Intent intent, ActivityInfo acInfo, ApplicationInfo appInfo) {
        ComponentName cn = intent.getComponent();
        String activity = null;
        if (!(cn == null || appInfo == null)) {
            activity = cn.flattenToString() + "/" + appInfo.versionCode;
        }
        native_at_stopActivity(activity);
    }

    public float activityMiscTrigger(int func, String activity, int flag, int type) {
        return native_at_miscActivity(func, activity, flag, type);
    }
}
