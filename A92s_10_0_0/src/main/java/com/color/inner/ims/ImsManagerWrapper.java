package com.color.inner.ims;

import android.content.Context;
import com.android.ims.ImsManager;

public class ImsManagerWrapper {
    private static final String TAG = "ImsManagerWrapper";
    private ImsManager mImsManager = null;

    public ImsManagerWrapper(Context context, int phoneId) {
        this.mImsManager = new ImsManager(context, phoneId);
    }

    private ImsManagerWrapper(ImsManager imsManager) {
        this.mImsManager = imsManager;
    }

    public static ImsManagerWrapper getInstance(Context context, int phoneId) {
        return new ImsManagerWrapper(ImsManager.getInstance(context, phoneId));
    }

    public boolean isVtEnabledByUser() {
        return this.mImsManager.isVtEnabledByUser();
    }

    public boolean isVtEnabledByPlatform() {
        return this.mImsManager.isVtEnabledByPlatform();
    }

    public boolean isWfcEnabledByUser() {
        return this.mImsManager.isWfcEnabledByUser();
    }

    public boolean isWfcEnabledByPlatform() {
        return this.mImsManager.isWfcEnabledByPlatform();
    }

    public boolean isEnhanced4gLteModeSettingEnabledByUser() {
        return this.mImsManager.isEnhanced4gLteModeSettingEnabledByUser();
    }

    public boolean isVolteEnabledByPlatform() {
        return this.mImsManager.isVolteEnabledByPlatform();
    }

    public void setEnhanced4gLteModeSetting(boolean enabled) {
        this.mImsManager.setEnhanced4gLteModeSetting(enabled);
    }
}
