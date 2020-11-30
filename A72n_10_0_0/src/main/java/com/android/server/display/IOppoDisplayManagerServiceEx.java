package com.android.server.display;

import com.android.server.IOppoCommonManagerServiceEx;

public interface IOppoDisplayManagerServiceEx extends IOppoCommonManagerServiceEx {
    default DisplayManagerService getDisplayManagerService() {
        return null;
    }
}
