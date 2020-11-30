package com.mediatek.server.wm;

import android.os.SystemProperties;
import android.util.Slog;
import android.view.DisplayInfo;
import android.view.WindowManager;
import com.android.server.wm.WindowState;
import com.mediatek.appresolutiontuner.ResolutionTunerAppList;

public class WmsExtImpl extends WmsExt {
    private static final String TAG = "WmsExtImpl";
    private static final String TAG_ART = "AppResolutionTuner";

    public boolean isAppResolutionTunerSupport() {
        if (!"1".equals(SystemProperties.get("ro.vendor.app_resolution_tuner")) || SystemProperties.getInt("persist.vendor.dbg.disable.art", 0) != 0) {
            return false;
        }
        return true;
    }

    public void loadResolutionTunerAppList() {
        getTunerList().loadTunerAppList();
    }

    public void setWindowScaleByWL(WindowState win, DisplayInfo displayInfo, WindowManager.LayoutParams attrs, int requestedWidth, int requestedHeight) {
        float scale = 1.0f;
        int width = displayInfo.logicalWidth;
        int height = displayInfo.logicalHeight;
        String windowName = null;
        String packageName = attrs != null ? attrs.packageName : null;
        if (!(attrs == null || attrs.getTitle() == null)) {
            windowName = attrs.getTitle().toString();
        }
        if (packageName != null && windowName != null && !windowName.contains("FastStarting") && !windowName.contains("Splash Screen") && !windowName.contains("PopupWindow") && (((height == requestedHeight && width == requestedWidth) || (attrs.width == -1 && attrs.height == -1 && attrs.x == 0 && attrs.y == 0)) && getTunerList().isScaledByWMS(packageName, windowName))) {
            scale = getTunerList().getScaleValue(packageName);
        }
        if (scale != 1.0f) {
            win.mHWScale = scale;
            win.mNeedHWResizer = true;
            Slog.v(TAG_ART, "setWindowScaleByWL - new scale = " + scale + " ,set mEnforceSizeCompat/mNeedHWResizer = true , win : " + win + " ,attrs=" + attrs.getTitle().toString());
        }
    }

    private ResolutionTunerAppList getTunerList() {
        return ResolutionTunerAppList.getInstance();
    }
}
