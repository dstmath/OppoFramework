package com.android.server;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.ServiceManager;
import android.util.Slog;
import android.view.inputmethod.InputMethodInfo;
import com.android.server.am.OppoProcessManager;
import com.android.server.am.OppoProcessManagerHelper;
import com.android.server.job.JobSchedulerService;
import com.android.server.notification.NotificationManagerInternal;
import com.android.server.wallpaper.WallpaperManagerService;
import com.android.server.wm.WindowManagerService;
import java.util.List;

public class OppoBPMHelper {
    static final String TAG = "OppoProcessManager";
    public static AlarmManagerService sAlarmManagerService = null;
    private static boolean sDebugDefault = OppoProcessManager.sDebugDetail;
    public static JobSchedulerService sJobSchedulerService = null;

    public static final void updateProviders(List<String> appwidgetList) {
        for (String pkg : appwidgetList) {
            updateProvider(pkg);
        }
    }

    public static final void updateProvider(String pkg) {
        if (sDebugDefault) {
            Slog.i("OppoProcessManager", "updateProvider pkg is " + pkg);
        }
        AppWidgetBackupBridge.oppoUpdateProvidersForPackage(pkg, 0);
    }

    public static final String getLivePackageForLiveWallPaper() {
        ComponentName cn = getLiveComponent((WallpaperManagerService) ServiceManager.getService("wallpaper"));
        if (cn != null) {
            return cn.getPackageName();
        }
        return null;
    }

    static final ComponentName getLiveComponent(WallpaperManagerService wms) {
        if (wms != null) {
            return wms.getLiveComponent();
        }
        return null;
    }

    public static final int[] getTouchedWindowPids(WindowManagerService wm) {
        return wm.getInputManagerService().getTouchedWindowPids();
    }

    public static final int[] getLocationListenersUid() {
        LocationManagerService lm = (LocationManagerService) ServiceManager.getService("location");
        if (lm != null) {
            return lm.getLocationListenersUid();
        }
        return null;
    }

    public static final void cancelNotificationsWithPkg(String pkgName, int userId) {
        NotificationManagerInternal nm = (NotificationManagerInternal) LocalServices.getService(NotificationManagerInternal.class);
        if (nm != null) {
            nm.cancelAllNotificationsFromBMP(pkgName, userId);
        }
    }

    public static final boolean checkProcessToast(int pid) {
        NotificationManagerInternal nm = (NotificationManagerInternal) LocalServices.getService(NotificationManagerInternal.class);
        if (nm == null) {
            return false;
        }
        return nm.checkProcessToast(pid);
    }

    public static final List<InputMethodInfo> getInputMethodList() {
        InputMethodManagerService imManager = (InputMethodManagerService) ServiceManager.getService("input_method");
        if (imManager != null) {
            return imManager.getInputMethodList();
        }
        return null;
    }

    public static final boolean isHomeProcess(Context context, String pkgName) {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        ResolveInfo defaultLaucher = context.getPackageManager().resolveActivity(intent, 65536);
        if (defaultLaucher == null || defaultLaucher.activityInfo == null) {
            List<ResolveInfo> homeList = context.getPackageManager().queryIntentActivities(intent, 270532608);
            if (homeList == null || homeList.isEmpty()) {
                return false;
            }
            for (ResolveInfo ri : homeList) {
                if (pkgName.equals(ri.activityInfo.packageName)) {
                    return true;
                }
            }
            return false;
        } else if (!pkgName.equals(defaultLaucher.activityInfo.packageName)) {
            return false;
        } else {
            if (sDebugDefault) {
                Slog.i("OppoProcessManager", "defaultLaucher= " + defaultLaucher.activityInfo.packageName);
            }
            return true;
        }
    }

    public static final void addPkgToAppWidgetList(String pkgName) {
        if (sDebugDefault) {
            Slog.i("OppoProcessManager", "addPkgToAppWidgetList pkg is " + pkgName);
        }
        OppoBPMUtils.getInstance().addPkgToAppWidgetList(pkgName);
    }

    public static final void removePkgFromAppWidgetList(String pkgName) {
        if (sDebugDefault) {
            Slog.i("OppoProcessManager", "removePkgFromAppWidgetList pkg is " + pkgName);
        }
        OppoBPMUtils.getInstance().removePkgFromAppWidgetList(pkgName);
    }

    public static final void addPkgToDisplayDeviceList(String pkgName) {
        if (sDebugDefault) {
            Slog.i("OppoProcessManager", "addPkgToDisplayDeviceList pkg is " + pkgName);
        }
        OppoBPMUtils.getInstance().addPkgToDisplayDeviceList(pkgName);
    }

    public static final void removePkgFromDisplayDeviceList(String pkgName) {
        if (sDebugDefault) {
            Slog.i("OppoProcessManager", "removePkgFromDisplayDeviceList pkg is " + pkgName);
        }
        OppoBPMUtils.getInstance().removePkgFromDisplayDeviceList(pkgName);
    }

    public static final List<String> getAppWidgetList() {
        return OppoBPMUtils.getInstance().getAppWidgetList();
    }

    public static final void setAlarmService(AlarmManagerService alarmManagerService) {
        sAlarmManagerService = alarmManagerService;
    }

    public static final void setJobSchedulerService(JobSchedulerService jobSchedulerService) {
        sJobSchedulerService = jobSchedulerService;
    }

    public static final void notifyStopStrictMode() {
        if (sAlarmManagerService != null) {
            sAlarmManagerService.stopStrictMode();
        }
        if (sJobSchedulerService != null) {
            sJobSchedulerService.stopStrictMode();
        }
    }

    public static final boolean isFrozingByUid(int uid) {
        return OppoProcessManagerHelper.isFrozingByUid(uid);
    }

    public static final void setPackageResume(int uid, String packageName, String reason) {
        OppoProcessManagerHelper.setPackageResume(uid, packageName, reason);
    }

    public static final void setPackageResume(int uid, String packageName, int timeout, int isTargetFreeze, String reason) {
        OppoProcessManagerHelper.setPackageResume(uid, packageName, timeout, isTargetFreeze, reason);
    }

    public static final void noteWindowStateChange(int uid, int pid, int windowId, int windowType, boolean isVisible, boolean shown) {
        OppoProcessManagerHelper.noteWindowStateChange(uid, pid, windowId, windowType, isVisible, shown);
    }
}
