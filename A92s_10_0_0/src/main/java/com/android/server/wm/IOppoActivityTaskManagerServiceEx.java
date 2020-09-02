package com.android.server.wm;

import com.android.server.IOppoCommonManagerServiceEx;

public interface IOppoActivityTaskManagerServiceEx extends IOppoCommonManagerServiceEx {
    default ActivityTaskManagerService getActivityTaskManagerService() {
        return null;
    }
}
