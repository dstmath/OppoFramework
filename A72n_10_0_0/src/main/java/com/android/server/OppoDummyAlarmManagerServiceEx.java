package com.android.server;

import android.content.Context;

public class OppoDummyAlarmManagerServiceEx extends OppoDummyCommonManagerServiceEx implements IOppoAlarmManagerServiceEx {
    protected final AlarmManagerService mAms;

    public OppoDummyAlarmManagerServiceEx(Context context, AlarmManagerService alarmManagerService) {
        super(context);
        this.mAms = alarmManagerService;
    }

    @Override // com.android.server.IOppoAlarmManagerServiceEx
    public AlarmManagerService getAlarmManagerService() {
        return this.mAms;
    }
}
