package com.android.server.wm;

import android.content.Intent;
import android.content.pm.ActivityInfo;

public class ColorDummyActivityStarterEx implements IColorActivityStarterEx {
    final ActivityStarter mStarter;

    public ColorDummyActivityStarterEx(ActivityStarter starter) {
        this.mStarter = starter;
    }

    @Override // com.android.server.wm.IColorActivityStarterEx
    public int isProhibitInstallation(int err, Intent intent, int userId) {
        return err;
    }

    @Override // com.android.server.wm.IColorActivityStarterEx
    public void packageInstallInfoCollectForExp(WindowProcessController callerApp, String callingPackage, Intent intent) {
    }

    @Override // com.android.server.wm.IColorActivityStarterEx
    public void notifyToRemovePkgFromNotLaunchedList(ActivityInfo aInfo, boolean notify) {
    }
}
