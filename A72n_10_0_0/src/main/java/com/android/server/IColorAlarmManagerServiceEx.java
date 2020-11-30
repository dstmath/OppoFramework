package com.android.server;

public interface IColorAlarmManagerServiceEx extends IOppoAlarmManagerServiceEx {
    @Override // com.android.server.IOppoAlarmManagerServiceEx
    AlarmManagerService getAlarmManagerService();
}
