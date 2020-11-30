package com.mediatek.ims.internal.ext;

import android.content.Context;

public class OpImsCustomizationFactoryBase {
    public IImsManagerExt makeImsManagerExt(Context context) {
        return new ImsManagerExt();
    }
}
