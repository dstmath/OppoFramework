package com.android.server.wm;

public class ColorWmsFreeformHelp {
    private static final long OPPO_FREEZE_TIMEOUT_DEFAULE = 3000;
    private static final String TAG = "ColorWmsFreeformHelp";
    private static ColorWmsFreeformHelp mHelp = null;
    private final Object mOppoSplitLock = new Object();
    private WindowManagerService mWms = null;

    private ColorWmsFreeformHelp() {
    }

    public static ColorWmsFreeformHelp getInstance() {
        if (mHelp == null) {
            synchronized (ColorWmsFreeformHelp.class) {
                if (mHelp == null) {
                    mHelp = new ColorWmsFreeformHelp();
                }
            }
        }
        return mHelp;
    }

    public void init(IColorWindowManagerServiceEx wmsEx) {
        this.mWms = wmsEx.getWindowManagerService();
    }

    public void oppoStartFreezingDisplayLocked() {
    }

    public void oppoStopFreezingDisplayLocked() {
    }
}
