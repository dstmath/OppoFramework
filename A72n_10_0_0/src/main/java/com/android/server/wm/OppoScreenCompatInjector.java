package com.android.server.wm;

import android.common.OppoFeatureCache;
import android.content.pm.ApplicationInfo;

public class OppoScreenCompatInjector {
    public static void overrideCompatInfoIfNeed(ApplicationInfo ai) {
        OppoFeatureCache.get(IOppoScreenModeManagerFeature.DEFAULT).overrideCompatInfoIfNeed(ai);
    }

    public static boolean isDisplayCompat(String packageName, int uid) {
        return OppoFeatureCache.get(IOppoScreenModeManagerFeature.DEFAULT).isDisplayCompat(packageName, uid);
    }

    public static float overrideScaleIfNeed(WindowState win) {
        return OppoFeatureCache.get(IOppoScreenModeManagerFeature.DEFAULT).overrideScaleIfNeed(win);
    }
}
