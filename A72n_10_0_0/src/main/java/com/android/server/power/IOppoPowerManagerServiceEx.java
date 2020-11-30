package com.android.server.power;

import com.android.server.IOppoCommonManagerServiceEx;

public interface IOppoPowerManagerServiceEx extends IOppoCommonManagerServiceEx {
    default PowerManagerService getPowerManagerService() {
        return null;
    }
}
