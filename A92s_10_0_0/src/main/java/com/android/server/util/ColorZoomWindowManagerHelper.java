package com.android.server.util;

import android.common.OppoFeatureCache;
import com.android.server.wm.IColorZoomWindowManager;

public class ColorZoomWindowManagerHelper {
    private static final String TAG = "ColorZoomWindowManagerHelper";
    private static volatile ColorZoomWindowManagerHelper sInstance;

    public static ColorZoomWindowManagerHelper getInstance() {
        if (sInstance == null) {
            synchronized (ColorZoomWindowManagerHelper.class) {
                if (sInstance == null) {
                    sInstance = new ColorZoomWindowManagerHelper();
                }
            }
        }
        return sInstance;
    }

    public static IColorZoomWindowManager getZoomWindowManager() {
        return OppoFeatureCache.get(IColorZoomWindowManager.DEFAULT);
    }
}
