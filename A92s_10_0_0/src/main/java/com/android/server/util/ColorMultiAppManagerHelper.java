package com.android.server.util;

import android.common.OppoFeatureCache;
import com.android.server.am.IColorMultiAppManager;

public class ColorMultiAppManagerHelper {
    private static final String TAG = "ColorMultiAppManagerHelper";
    private static final ColorMultiAppManagerHelper instance = new ColorMultiAppManagerHelper();

    public static ColorMultiAppManagerHelper getInstance() {
        return instance;
    }

    public IColorMultiAppManager getMultiAppManager() {
        return OppoFeatureCache.get(IColorMultiAppManager.DEFAULT);
    }
}
