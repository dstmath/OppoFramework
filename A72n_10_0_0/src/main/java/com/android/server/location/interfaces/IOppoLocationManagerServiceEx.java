package com.android.server.location.interfaces;

import android.os.Message;
import com.android.server.IOppoCommonManagerServiceEx;
import com.android.server.LocationManagerService;

public interface IOppoLocationManagerServiceEx extends IOppoCommonManagerServiceEx {
    LocationManagerService getLocationManagerService();

    void handleMessage(Message message, int i);
}
