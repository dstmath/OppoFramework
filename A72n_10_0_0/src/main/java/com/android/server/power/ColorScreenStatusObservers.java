package com.android.server.power;

import android.app.ActivityManager;
import android.content.Context;
import android.os.RemoteCallbackList;
import com.android.server.am.IColorAppStartupManager;
import com.color.os.IColorScreenStatusListener;
import com.color.util.ColorLog;

public class ColorScreenStatusObservers extends RemoteCallbackList<IColorScreenStatusListener> {
    public static final boolean DBG = ColorPowerNotifierContext.DBG;
    public static final String TAG = "ColorPowerNotifierContext";
    private final ActivityManager mActivityManager;

    public ColorScreenStatusObservers(Context context) {
        this.mActivityManager = (ActivityManager) context.getSystemService(IColorAppStartupManager.TYPE_ACTIVITY);
    }

    public boolean register(IColorScreenStatusListener callback) {
        ColorScreenStatusObserver observer = new ColorScreenStatusObserver(callback, this.mActivityManager);
        boolean ret = super.register((ColorScreenStatusObservers) observer);
        printLog("register", observer, ret);
        return ret;
    }

    public boolean unregister(IColorScreenStatusListener callback) {
        ColorScreenStatusObserver observer = new ColorScreenStatusObserver(callback, this.mActivityManager);
        boolean ret = super.unregister((ColorScreenStatusObservers) observer);
        printLog("unregister", observer, ret);
        return ret;
    }

    public void onCallbackDied(IColorScreenStatusListener callback) {
        super.onCallbackDied((ColorScreenStatusObservers) callback);
        printLog("onCallbackDied", (ColorScreenStatusObserver) callback, true);
    }

    private void printLog(String msg, ColorScreenStatusObserver observer, boolean result) {
        boolean z = DBG;
        ColorLog.d(z, "ColorPowerNotifierContext", msg + " from " + observer.getCaller() + " : " + result);
    }
}
