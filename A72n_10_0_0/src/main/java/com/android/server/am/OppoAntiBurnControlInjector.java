package com.android.server.am;

import android.app.IApplicationThread;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import com.android.server.wm.ActivityTaskManagerService;

public class OppoAntiBurnControlInjector {
    private static IOppoAntiBurnController sController = null;
    private static boolean sFeatureEnable = true;

    private static void initInstance() {
        if (sFeatureEnable && sController == null) {
            sController = new OppoAntiBurnController();
        }
    }

    public static void init(ActivityManagerService ams, ActivityTaskManagerService atms, IPackageManager pm) {
        initInstance();
        IOppoAntiBurnController iOppoAntiBurnController = sController;
        if (iOppoAntiBurnController != null) {
            iOppoAntiBurnController.init(ams, atms, pm);
        }
    }

    public static void dispatchConfig(IApplicationThread appThread, ApplicationInfo targetAppInfo) {
        initInstance();
        IOppoAntiBurnController iOppoAntiBurnController = sController;
        if (iOppoAntiBurnController != null) {
            iOppoAntiBurnController.dispatchConfig(appThread, targetAppInfo);
        }
    }

    public static void notifyPackageChanged(String pkgName, int uid, String changeType) {
        initInstance();
        IOppoAntiBurnController iOppoAntiBurnController = sController;
        if (iOppoAntiBurnController != null) {
            iOppoAntiBurnController.notifyPackageChanged(pkgName, uid, changeType);
        }
    }

    public static void setAppThreadExtend(OppoAppThreadExtendCallback callback) {
        initInstance();
        IOppoAntiBurnController iOppoAntiBurnController = sController;
        if (iOppoAntiBurnController != null) {
            iOppoAntiBurnController.setAppThreadExtend(callback);
        }
    }
}
