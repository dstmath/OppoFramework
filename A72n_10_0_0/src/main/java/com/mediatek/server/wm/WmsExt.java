package com.mediatek.server.wm;

import android.view.DisplayInfo;
import android.view.WindowManager;
import com.android.server.wm.WindowState;

public class WmsExt {
    public static final String TAG = "WindowManager";

    public boolean isAppResolutionTunerSupport() {
        return false;
    }

    public void loadResolutionTunerAppList() {
    }

    public void setWindowScaleByWL(WindowState win, DisplayInfo displayInfo, WindowManager.LayoutParams attrs, int requestedWidth, int requestedHeight) {
    }
}
