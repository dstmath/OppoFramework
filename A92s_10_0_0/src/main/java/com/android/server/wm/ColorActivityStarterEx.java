package com.android.server.wm;

import android.common.OppoFeatureCache;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import com.android.server.pm.IColorChildrenModeInstallManager;
import com.android.server.pm.IColorPackageInstallStatisticManager;
import com.android.server.pm.IColorPkgStartInfoManager;

public class ColorActivityStarterEx extends ColorDummyActivityStarterEx {
    public ColorActivityStarterEx(ActivityStarter starter) {
        super(starter);
    }

    public int isProhibitInstallation(int err, Intent intent, int userId) {
        String className = "";
        ComponentName componentName = intent.getComponent();
        if (componentName != null) {
            className = componentName.getClassName();
        }
        if ("com.android.packageinstaller.PackageInstallerActivity".equals(className) && OppoFeatureCache.get(IColorChildrenModeInstallManager.DEFAULT).prohibitChildInstallation(userId, true)) {
            return 100;
        }
        if (!"com.android.packageinstaller.UninstallerActivity".equals(className) || !OppoFeatureCache.get(IColorChildrenModeInstallManager.DEFAULT).prohibitChildInstallation(userId, false)) {
            return err;
        }
        return 100;
    }

    public void packageInstallInfoCollectForExp(WindowProcessController callerApp, String callingPackage, Intent intent) {
        String callAppName = callerApp != null ? callerApp.mInfo.processName : null;
        if (callAppName != null) {
            OppoFeatureCache.get(IColorPackageInstallStatisticManager.DEFAULT).packageInstallInfoCollectForExp(callAppName, callingPackage, intent);
        }
    }

    public void notifyToRemovePkgFromNotLaunchedList(ActivityInfo aInfo, boolean notify) {
        if (aInfo != null && aInfo.packageName != null) {
            OppoFeatureCache.get(IColorPkgStartInfoManager.DEFAULT).removePkgFromNotLaunchedList(aInfo.packageName, true);
        }
    }
}
