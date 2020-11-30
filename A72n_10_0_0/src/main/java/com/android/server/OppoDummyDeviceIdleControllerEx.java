package com.android.server;

import android.content.Context;

public class OppoDummyDeviceIdleControllerEx extends OppoDummyCommonManagerServiceEx implements IOppoDeviceIdleControllerEx {
    protected final DeviceIdleController mDic;

    public OppoDummyDeviceIdleControllerEx(Context context, DeviceIdleController dic) {
        super(context);
        this.mDic = dic;
    }

    @Override // com.android.server.IOppoDeviceIdleControllerEx
    public DeviceIdleController getDeviceIdleController() {
        return this.mDic;
    }
}
