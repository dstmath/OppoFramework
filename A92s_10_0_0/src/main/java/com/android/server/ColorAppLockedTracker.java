package com.android.server;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.ArrayMap;
import android.util.Slog;
import com.android.server.display.ai.utils.ColorAILog;
import com.color.util.ColorCommonConfig;
import java.util.ArrayList;
import java.util.Iterator;

public class ColorAppLockedTracker extends ColorAppActionTracker {
    private static final boolean DEBUG = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, (boolean) DEBUG);
    private static final String RECENT_LOCK_LIST = "recent_lock_list";
    private static final String TAG = "ColorAppLockedTracker";
    private static volatile ColorAppLockedTracker mInstance = null;
    ArrayList<Integer> mLockAppIds = new ArrayList<>();

    public static ColorAppLockedTracker getInstance(Context context) {
        if (mInstance == null) {
            synchronized (ColorAppLockedTracker.class) {
                if (mInstance == null) {
                    mInstance = new ColorAppLockedTracker(context);
                }
            }
        }
        return mInstance;
    }

    public ColorAppLockedTracker(Context context) {
        super(context);
    }

    public boolean isWhiteList(int appid) {
        boolean contains;
        synchronized (this.mLockAppIds) {
            contains = this.mLockAppIds.contains(Integer.valueOf(appid));
        }
        return contains;
    }

    public ArrayMap<Integer, Integer> getTrackWhiteList(boolean startFromScreenOn) {
        ArrayMap<Integer, Integer> result = new ArrayMap<>();
        synchronized (this.mLockAppIds) {
            for (int i = 0; i < this.mLockAppIds.size(); i++) {
                int appid = this.mLockAppIds.get(i).intValue();
                result.put(Integer.valueOf(appid), 1);
                if (DEBUG) {
                    Slog.d(TAG, "getLockWhiteList appid = " + appid);
                }
            }
        }
        return result;
    }

    public void handleScreenOn() {
        if (DEBUG) {
            Slog.d(TAG, "handleScreenOn");
        }
    }

    public void handleScreenOff() {
        ArrayList<String> list;
        if (this.mStart) {
            Bundle data = ColorCommonConfig.getInstance().getConfigInfo(RECENT_LOCK_LIST, 1);
            if (!(data == null || (list = data.getStringArrayList(RECENT_LOCK_LIST)) == null)) {
                PackageManager pm = this.mContext.getPackageManager();
                synchronized (this.mLockAppIds) {
                    this.mLockAppIds.clear();
                    Iterator<String> it = list.iterator();
                    while (it.hasNext()) {
                        String pkg = it.next();
                        try {
                            String realPkg = pkg.split("#")[0];
                            if (DEBUG) {
                                Slog.d(TAG, "update lockWhiteList pkg = " + pkg + ", realPkg = " + realPkg);
                            }
                            int appid = UserHandle.getAppId(pm.getApplicationInfo(realPkg, 0).uid);
                            if (!this.mLockAppIds.contains(Integer.valueOf(appid))) {
                                this.mLockAppIds.add(Integer.valueOf(appid));
                            }
                        } catch (Exception e) {
                        }
                    }
                }
            }
            if (DEBUG) {
                Slog.d(TAG, "handleScreenOff");
            }
        }
    }

    public void onStart() {
        if (DEBUG) {
            Slog.d(TAG, "app locked tracker start");
        }
    }

    public void onStop() {
        synchronized (this.mLockAppIds) {
            this.mLockAppIds.clear();
        }
        if (DEBUG) {
            Slog.d(TAG, "app locked tracker stop");
        }
    }
}
