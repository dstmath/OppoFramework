package com.mediatek.ims.internal.ext;

import android.content.Context;
import android.util.Log;

public class ImsManagerExt implements IImsManagerExt {
    private static final String TAG = "ImsManagerExt";

    @Override // com.mediatek.ims.internal.ext.IImsManagerExt
    public boolean isFeatureEnabledByPlatform(Context context, int feature, int phoneId) {
        return true;
    }

    @Override // com.mediatek.ims.internal.ext.IImsManagerExt
    public int getImsPhoneId(Context context, int phoneId) {
        Log.d(TAG, "phoneId = " + phoneId);
        return phoneId;
    }
}
