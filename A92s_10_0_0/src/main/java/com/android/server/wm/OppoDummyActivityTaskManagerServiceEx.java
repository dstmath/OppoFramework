package com.android.server.wm;

import android.content.Context;
import com.android.server.OppoDummyCommonManagerServiceEx;

public class OppoDummyActivityTaskManagerServiceEx extends OppoDummyCommonManagerServiceEx implements IOppoActivityTaskManagerServiceEx {
    protected final ActivityTaskManagerService mAtms;

    public OppoDummyActivityTaskManagerServiceEx(Context context, ActivityTaskManagerService atms) {
        super(context);
        this.mAtms = atms;
    }

    @Override // com.android.server.wm.IOppoActivityTaskManagerServiceEx
    public ActivityTaskManagerService getActivityTaskManagerService() {
        return this.mAtms;
    }
}
