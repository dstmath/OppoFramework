package com.android.server.power.batterysaver;

import android.database.ContentObserver;
import com.android.internal.os.BackgroundThread;
import com.android.server.power.IColorPowerManagerServiceEx;

public abstract class OppoBaseBatterySaverPolicy extends ContentObserver {
    private static final String TAG = "OppoBaseBatterySaverPolicy";
    IColorPowerManagerServiceEx mColorPowerEx = null;

    public OppoBaseBatterySaverPolicy() {
        super(BackgroundThread.getHandler());
    }
}
