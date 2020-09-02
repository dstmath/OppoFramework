package com.android.server.wm;

import android.os.Message;
import com.android.server.IOppoCommonManagerServiceEx;

public interface IOppoWindowManagerServiceEx extends IOppoCommonManagerServiceEx {
    default WindowManagerService getWindowManagerService() {
        return null;
    }

    default void handleMessage(Message msg, int whichHandler) {
    }
}
