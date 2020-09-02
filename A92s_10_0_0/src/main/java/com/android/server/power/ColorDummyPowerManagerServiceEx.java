package com.android.server.power;

import android.content.Context;

public class ColorDummyPowerManagerServiceEx extends OppoDummyPowerManagerServiceEx implements IColorPowerManagerServiceEx {
    public ColorDummyPowerManagerServiceEx(Context context, PowerManagerService pms) {
        super(context, pms);
    }

    @Override // com.android.server.IOppoCommonManagerServiceEx, com.android.server.OppoDummyCommonManagerServiceEx
    public void systemReady() {
    }

    @Override // com.android.server.power.IColorPowerManagerServiceEx
    public Context getContext() {
        return this.mContext;
    }
}
