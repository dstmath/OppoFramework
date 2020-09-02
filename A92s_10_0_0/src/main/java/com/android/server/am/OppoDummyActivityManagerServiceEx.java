package com.android.server.am;

import android.content.Context;
import android.os.Message;
import com.android.server.OppoDummyCommonManagerServiceEx;

public class OppoDummyActivityManagerServiceEx extends OppoDummyCommonManagerServiceEx implements IOppoActivityManagerServiceEx {
    protected final ActivityManagerService mAms;

    public OppoDummyActivityManagerServiceEx(Context context, ActivityManagerService ams) {
        super(context);
        this.mAms = ams;
    }

    @Override // com.android.server.am.IOppoActivityManagerServiceEx
    public ActivityManagerService getActivityManagerService() {
        return this.mAms;
    }

    @Override // com.android.server.am.IOppoActivityManagerServiceEx
    public void handleMessage(Message msg, int whichHandler) {
    }
}
