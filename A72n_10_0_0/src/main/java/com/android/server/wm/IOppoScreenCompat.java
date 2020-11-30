package com.android.server.wm;

import android.content.Context;
import android.content.pm.ApplicationInfo;

public interface IOppoScreenCompat {
    void init(WindowManagerService windowManagerService, Context context, boolean z, int i);

    boolean isDisplayCompat(String str, int i);

    void overrideCompatInfoIfNeed(ApplicationInfo applicationInfo);

    float overrideScaleIfNeed(WindowState windowState);
}
