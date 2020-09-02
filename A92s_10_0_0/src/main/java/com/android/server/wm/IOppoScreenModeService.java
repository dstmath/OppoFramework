package com.android.server.wm;

import android.content.Context;
import android.os.IBinder;

public interface IOppoScreenModeService {
    int adjustDensityForUser(int i, int i2);

    int getPreferredModeId(WindowState windowState, int i);

    void init(WindowManagerService windowManagerService, Context context, boolean z, boolean z2, boolean z3);

    void onSetDensityForUser(int i, int i2);

    void setRefreshRate(IBinder iBinder, int i);

    void setRefreshRate(AppWindowToken appWindowToken, int i);

    void startAnimation(boolean z);

    int updateGlobalModeId(int i);

    void updateScreenSplitMode(boolean z);
}
