package com.mediatek.server.display;

import android.os.SystemProperties;
import android.view.DisplayInfo;

public class MtkDisplayManagerServiceImpl {
    private static final boolean DEBUG = false;
    private static final String TAG = "MtkDisplayManagerServiceImpl";
    private DisplayInfo mDisplayInfo = new DisplayInfo();
    private boolean mSupportFullscreenSwitch = "1".equals(SystemProperties.get("ro.vendor.fullscreen_switch"));

    public void setDisplayInfoForFullscreenSwitch(DisplayInfo displayInfo) {
    }

    public DisplayInfo getDisplayInfoForFullscreenSwitch(DisplayInfo displayInfo, int callingUid) {
        return null;
    }
}
