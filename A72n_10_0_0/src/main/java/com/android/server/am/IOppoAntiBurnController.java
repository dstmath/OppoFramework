package com.android.server.am;

import android.app.IApplicationThread;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import com.android.server.wm.ActivityTaskManagerService;

public interface IOppoAntiBurnController {
    void dispatchConfig(IApplicationThread iApplicationThread, ApplicationInfo applicationInfo);

    void init(ActivityManagerService activityManagerService, ActivityTaskManagerService activityTaskManagerService, IPackageManager iPackageManager);

    void notifyPackageChanged(String str, int i, String str2);

    void setAppThreadExtend(OppoAppThreadExtendCallback oppoAppThreadExtendCallback);
}
