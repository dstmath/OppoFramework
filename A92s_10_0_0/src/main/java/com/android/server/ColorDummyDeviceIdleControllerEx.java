package com.android.server;

import android.content.Context;

public class ColorDummyDeviceIdleControllerEx extends OppoDummyDeviceIdleControllerEx implements IColorDeviceIdleControllerEx {
    private static final String TAG = "ColorDummyDeviceIdleControllerEx";

    public ColorDummyDeviceIdleControllerEx(Context context, DeviceIdleController dic) {
        super(context, dic);
    }

    @Override // com.android.server.IOppoCommonManagerServiceEx, com.android.server.OppoDummyCommonManagerServiceEx
    public void systemReady() {
    }

    private void registerColorDeviceIdleController() {
    }
}
