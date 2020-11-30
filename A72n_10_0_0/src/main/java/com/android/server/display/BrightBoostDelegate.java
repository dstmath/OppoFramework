package com.android.server.display;

import android.util.Slog;

/* access modifiers changed from: package-private */
public class BrightBoostDelegate {
    private static final long SCREENON_BRIGHTNESS_BOOST_TIMEOUT = 4000;
    private static int mBrightnessBoost = 0;
    private static boolean mPocketRingingState = false;
    private short mSkipCount = 0;

    BrightBoostDelegate() {
    }

    /* access modifiers changed from: package-private */
    public void turnOnBoost() {
        if (ColorAutomaticBrightnessController.DEBUG) {
            Slog.e("ColorAutomaticBrightnessController", "setLightSensorEnabled     mBrightnessBoost = " + mBrightnessBoost);
        }
        int i = mBrightnessBoost;
        if (i == 4 || i == 0 || i == 2) {
            mBrightnessBoost = 3;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isScreenOn(long time, long lightSensorEnableTime) {
        if (time - lightSensorEnableTime <= SCREENON_BRIGHTNESS_BOOST_TIMEOUT) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean isPocketRingingState() {
        return mPocketRingingState;
    }

    /* access modifiers changed from: package-private */
    public void setPocketRingingState(boolean state) {
        mPocketRingingState = state;
    }

    /* access modifiers changed from: package-private */
    public int getBrightnessBoost() {
        return mBrightnessBoost;
    }

    /* access modifiers changed from: package-private */
    public void setBrightnessBoost(int mode) {
        mBrightnessBoost = mode;
    }
}
