package com.color.inner.content.om;

import android.content.om.ColorOverlayManager;
import android.content.om.IColorOverlayManager;
import android.util.Log;

public class OverlayManagerWrapper {
    private static final String TAG = "OverlayManagerWrapper";
    private final IColorOverlayManager mService = new ColorOverlayManager();

    public void setLanguageEnable(String overlayPath, int userId) {
        try {
            this.mService.setLanguageEnable(overlayPath, userId);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
        }
    }
}
