package com.android.server.am;

import android.os.IColorKeyEventObserver;
import android.text.TextUtils;
import android.util.Slog;

public class ColorKeyEventManagerService implements IColorKeyEventManager {
    private static final String TAG = "ColorKeyEventManagerService";
    private static volatile ColorKeyEventManagerService sInstance = null;
    private boolean mInitialized;

    public static ColorKeyEventManagerService getInstance() {
        if (sInstance == null) {
            synchronized (ColorKeyEventManagerService.class) {
                if (sInstance == null) {
                    sInstance = new ColorKeyEventManagerService();
                }
            }
        }
        return sInstance;
    }

    private ColorKeyEventManagerService() {
        this.mInitialized = false;
        this.mInitialized = true;
    }

    public boolean registerKeyEventObserver(String observerFingerPrint, IColorKeyEventObserver observer, int listenFlag) {
        if (initialized() && !TextUtils.isEmpty(observerFingerPrint) && observer != null && listenFlag >= -1) {
            return ColorKeyEventUtil.getInstance().registerKeyEventObserver(observerFingerPrint, observer, listenFlag);
        }
        Slog.e(TAG, "registerKeyEventObserver failed, observerFingerPrint: " + observerFingerPrint + ", listenFlag: " + listenFlag);
        return false;
    }

    public boolean unregisterKeyEventObserver(String observerFingerPrint) {
        if (initialized() && !TextUtils.isEmpty(observerFingerPrint)) {
            return ColorKeyEventUtil.getInstance().unregisterKeyEventObserver(observerFingerPrint, false);
        }
        Slog.e(TAG, "unregisterKeyEventObserver failed, observerFingerPrint: " + observerFingerPrint);
        return false;
    }

    private boolean initialized() {
        return this.mInitialized;
    }
}
