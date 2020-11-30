package com.android.server.power;

import android.content.Context;
import com.android.server.OppoDummyCommonManagerServiceEx;

public class OppoDummyPowerManagerServiceEx extends OppoDummyCommonManagerServiceEx implements IOppoPowerManagerServiceEx {
    protected final PowerManagerService mPowerMS;

    public OppoDummyPowerManagerServiceEx(Context context, PowerManagerService pms) {
        super(context);
        this.mPowerMS = pms;
    }

    @Override // com.android.server.power.IOppoPowerManagerServiceEx
    public PowerManagerService getPowerManagerService() {
        return this.mPowerMS;
    }
}
