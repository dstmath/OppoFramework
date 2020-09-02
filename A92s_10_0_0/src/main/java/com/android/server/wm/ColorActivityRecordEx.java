package com.android.server.wm;

import android.content.res.Configuration;

public class ColorActivityRecordEx extends ColorDummyActivityRecordEx {
    private boolean mForceRelaunchByNavBarHide = false;

    public ColorActivityRecordEx(ActivityRecord ar) {
        super(ar);
    }

    public boolean forceRelaunchByNavBarHide() {
        return this.mForceRelaunchByNavBarHide;
    }

    public void setForceRelaunchByNavBarHide(boolean shouldRelaunch) {
        this.mForceRelaunchByNavBarHide = shouldRelaunch;
    }

    public boolean isUpdateFromNavbarHide(Configuration lastConfig, Configuration currentConfig, int height, String packageName) {
        boolean result = false;
        int desityDpi = currentConfig.densityDpi;
        int widthDiff = Math.round(((float) (Math.abs(lastConfig.screenWidthDp - currentConfig.screenWidthDp) * desityDpi)) / 160.0f);
        int heightDiff = Math.round(((float) (Math.abs(lastConfig.screenHeightDp - currentConfig.screenHeightDp) * desityDpi)) / 160.0f);
        if (widthDiff == height || heightDiff == height) {
            result = true;
        }
        if ("com.netease.my".equals(packageName) && result) {
            this.mForceRelaunchByNavBarHide = true;
        }
        return result;
    }
}
