package com.android.server;

import android.content.Context;

public class ColorDeviceIdleControllerEx extends ColorDummyDeviceIdleControllerEx {
    private static final String TAG = "ColorDeviceIdleControllerEx";

    public ColorDeviceIdleControllerEx(Context context, DeviceIdleController dic) {
        super(context, dic);
        init(context, dic);
    }

    public void systemReady() {
        ColorServiceRegistry.getInstance().serviceReady(27);
    }

    private void init(Context context, DeviceIdleController dic) {
        ColorServiceRegistry.getInstance().serviceInit(7, this);
    }
}
