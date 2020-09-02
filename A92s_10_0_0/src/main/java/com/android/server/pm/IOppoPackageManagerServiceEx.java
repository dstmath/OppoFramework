package com.android.server.pm;

import android.os.Message;
import com.android.server.IOppoCommonManagerServiceEx;

public interface IOppoPackageManagerServiceEx extends IOppoCommonManagerServiceEx {
    default PackageManagerService getPackageManagerService() {
        return null;
    }

    default void handleMessage(Message msg, int whichHandler) {
    }
}
