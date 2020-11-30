package com.android.server.wm;

import android.util.Slog;

public class ColorZoomWindowDebugConfig {
    public static boolean DEBUG_LIFE_CYCLE = false;
    private static final String TAG = "ColorZoomWindowDebugConfig";
    private static volatile ColorZoomWindowDebugConfig sInstance;

    public static ColorZoomWindowDebugConfig getInstance() {
        if (sInstance == null) {
            synchronized (ColorZoomWindowDebugConfig.class) {
                if (sInstance == null) {
                    sInstance = new ColorZoomWindowDebugConfig();
                }
            }
        }
        return sInstance;
    }

    public void enableDebugLifeCycle() {
        Slog.i(TAG, "enableDebugLifeCycle before: DEBUG_LIFE_CYCLE = " + DEBUG_LIFE_CYCLE);
        DEBUG_LIFE_CYCLE = true;
        Slog.i(TAG, "enableDebugLifeCycle after: DEBUG_LIFE_CYCLE = " + DEBUG_LIFE_CYCLE);
    }

    public void disableDebugLifeCycle() {
        Slog.i(TAG, "disableDebugLifeCycle before: DEBUG_LIFE_CYCLE = " + DEBUG_LIFE_CYCLE);
        DEBUG_LIFE_CYCLE = false;
        Slog.i(TAG, "disableDebugLifeCycle after: DEBUG_LIFE_CYCLE = " + DEBUG_LIFE_CYCLE);
    }
}
