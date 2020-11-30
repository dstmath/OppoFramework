package com.oppo.screenmode;

import android.common.PswFrameworkFactory;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.util.Log;
import android.view.View;

@Deprecated
public class OppoScreenModeInjector {
    public static final int DEFAULT_RATE = 0;
    private static final String FEATURE_SCREENMODE = "oppo.dynamicfpsswitch.feature.support";
    public static final int RATE_120 = 3;
    public static final int RATE_60 = 2;
    public static final int RATE_90 = 1;
    public static final int RATE_AUTO = 0;
    private static final String TAG = "ScreenMode";
    private static IPswScreenModeFeature mIPswScreenModeFeature = null;
    private static boolean sInitialized = false;
    private static boolean sScreenRateSupport = false;

    public static void init(Context context) {
        if (!sInitialized) {
            if (context == null) {
                Log.e(TAG, "failed to init for context null!");
                return;
            }
            PackageManager pkgMgr = context.getPackageManager();
            if (pkgMgr == null) {
                Log.e(TAG, "failed to init for pkgMgr uninit!");
                return;
            }
            if (pkgMgr.hasSystemFeature(FEATURE_SCREENMODE)) {
                sScreenRateSupport = true;
            } else {
                sScreenRateSupport = false;
            }
            mIPswScreenModeFeature = (IPswScreenModeFeature) PswFrameworkFactory.getInstance().getFeature(IPswScreenModeFeature.DEFAULT, context);
            Log.d(TAG, "init OppoScreenModeInjector, sScreenRateSupport:" + sScreenRateSupport);
            if (mIPswScreenModeFeature != null) {
                sInitialized = true;
            } else {
                sInitialized = false;
            }
        }
    }

    public static void setRefreshRate(IBinder token, int rate) {
        IPswScreenModeFeature iPswScreenModeFeature;
        if (!sInitialized) {
            Log.e(TAG, "setRefreshRate: failed for not init! Must call init(Context context) before.");
        } else if (sScreenRateSupport && (iPswScreenModeFeature = mIPswScreenModeFeature) != null) {
            iPswScreenModeFeature.setRefreshRate(token, rate);
        }
    }

    public static void setRefreshRate(View view, int rate) {
        IPswScreenModeFeature iPswScreenModeFeature;
        if (!sInitialized) {
            Log.e(TAG, "setRefreshRate: failed for not init! Must call init(Context context) before.");
        } else if (sScreenRateSupport && (iPswScreenModeFeature = mIPswScreenModeFeature) != null) {
            iPswScreenModeFeature.setRefreshRate(view, rate);
        }
    }

    public static boolean requestRefreshRate(boolean open, int rate) {
        IPswScreenModeFeature iPswScreenModeFeature;
        if (!sInitialized) {
            Log.e(TAG, "setRefreshRate: failed for not init! Must call init(Context context) before.");
            return false;
        } else if (sScreenRateSupport && (iPswScreenModeFeature = mIPswScreenModeFeature) != null) {
            return iPswScreenModeFeature.requestRefreshRate(open, rate);
        } else {
            return false;
        }
    }
}
