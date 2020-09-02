package com.android.server.wm;

import android.content.Intent;
import android.content.pm.ActivityInfo;

public interface IColorActivityStarterEx {
    int isProhibitInstallation(int i, Intent intent, int i2);

    void notifyToRemovePkgFromNotLaunchedList(ActivityInfo activityInfo, boolean z);

    void packageInstallInfoCollectForExp(WindowProcessController windowProcessController, String str, Intent intent);
}
