package com.android.server;

import android.content.Context;

public class ColorDummyAlarmManagerServiceEx extends OppoDummyAlarmManagerServiceEx implements IColorAlarmManagerServiceEx {
    public ColorDummyAlarmManagerServiceEx(Context context, AlarmManagerService alarmManagerService) {
        super(context, alarmManagerService);
    }
}
