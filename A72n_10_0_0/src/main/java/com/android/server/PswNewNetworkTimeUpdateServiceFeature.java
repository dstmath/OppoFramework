package com.android.server;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Slog;
import java.lang.reflect.Method;
import java.util.Calendar;

public class PswNewNetworkTimeUpdateServiceFeature implements IPswNewNetworkTimeUpdateServiceFeature {
    private static final boolean DBG_FUN = true;
    private static final boolean DBG_METHOD = true;
    private static final String GAME_PACKAGE_NAME = "com.tencent.tmgp.sgame";
    private static final String TAG = "PswNewNetworkTimeUpdateServiceFeature";
    private ActivityManager mActivityManager;
    private Context mContext;
    private boolean mIsInited;

    private PswNewNetworkTimeUpdateServiceFeature() {
        this.mContext = null;
        this.mIsInited = false;
        this.mActivityManager = null;
    }

    private static class InstanceHolder {
        static final PswNewNetworkTimeUpdateServiceFeature INSTANCE = new PswNewNetworkTimeUpdateServiceFeature();

        private InstanceHolder() {
        }
    }

    public static PswNewNetworkTimeUpdateServiceFeature getInstance(Context context) {
        Slog.d(TAG, "getInstance.");
        PswNewNetworkTimeUpdateServiceFeature instance = InstanceHolder.INSTANCE;
        instance.init(context);
        return instance;
    }

    private void init(Context context) {
        if (!this.mIsInited) {
            if (context == null) {
                Slog.e(TAG, "failed to init for null context!");
                return;
            }
            this.mContext = context;
            this.mIsInited = true;
        }
    }

    public boolean isNeedSkipPollNetworkTime() {
        Slog.d(TAG, "isNeedSkipPollNetworkTime");
        String packageName = null;
        try {
            ComponentName topActComp = getTopActivity();
            if (topActComp != null) {
                packageName = topActComp.getPackageName();
            }
        } catch (Exception e) {
        }
        if (GAME_PACKAGE_NAME.equals(packageName)) {
            return true;
        }
        return false;
    }

    public void checkSystemTime() {
        Slog.d(TAG, "checkSystemTime");
        boolean isFirstBoot = "1".equals(SystemProperties.get("persist.sys.device_first_boot", "1"));
        long cur = System.currentTimeMillis();
        if (isFirstBoot || cur < 50000000) {
            SystemProperties.set("persist.sys.device_first_boot", "0");
            Calendar c = Calendar.getInstance();
            c.set(Integer.parseInt(SystemProperties.get("ro.build.date.YmdHM", "2020").substring(0, 4)), 0, 1, 0, 0, 0);
            long destinationTime = c.getTimeInMillis();
            Log.w(TAG, "cur [" + cur + "]  destinationTime [" + destinationTime + "]");
            SystemClock.setCurrentTimeMillis(SystemClock.elapsedRealtime() + destinationTime);
            StringBuilder sb = new StringBuilder();
            sb.append("reset system time here, isFirstBoot = [");
            sb.append(isFirstBoot);
            sb.append("]");
            Log.w(TAG, sb.toString());
        }
    }

    private ComponentName getTopActivity() {
        if (this.mActivityManager == null) {
            this.mActivityManager = (ActivityManager) this.mContext.getSystemService("activity");
        }
        try {
            Method getTopAppName = this.mActivityManager.getClass().getMethod("getTopAppName", new Class[0]);
            getTopAppName.setAccessible(true);
            return (ComponentName) getTopAppName.invoke(this.mActivityManager, new Object[0]);
        } catch (Exception e) {
            Log.w(TAG, "getTopAppName failed.", e);
            return null;
        }
    }
}
