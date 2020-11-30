package com.android.server.om;

import android.content.Context;
import com.android.server.ColorServiceRegistry;

public class ColorOverlayManagerServiceEx extends ColorDummyOverlayManagerServiceEx {
    public ColorOverlayManagerServiceEx(Context context) {
        super(context);
    }

    public void init() {
        ColorServiceRegistry.getInstance().serviceInit(9, this);
    }
}
