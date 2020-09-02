package com.android.server.am;

import android.os.Message;
import com.android.server.IOppoCommonManagerServiceEx;

public interface IOppoActivityManagerServiceEx extends IOppoCommonManagerServiceEx {
    default ActivityManagerService getActivityManagerService() {
        return null;
    }

    default void handleMessage(Message msg, int whichHandler) {
    }
}
