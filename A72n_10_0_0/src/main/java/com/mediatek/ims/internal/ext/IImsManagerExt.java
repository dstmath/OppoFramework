package com.mediatek.ims.internal.ext;

import android.content.Context;

public interface IImsManagerExt {
    int getImsPhoneId(Context context, int i);

    boolean isFeatureEnabledByPlatform(Context context, int i, int i2);
}
