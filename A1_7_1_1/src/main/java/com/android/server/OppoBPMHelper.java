package com.android.server;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.ServiceManager;
import android.util.Slog;
import android.view.inputmethod.InputMethodInfo;
import com.android.server.am.OppoProcessManagerHelper;
import com.android.server.job.JobSchedulerService;
import com.android.server.notification.NotificationManagerInternal;
import com.android.server.wallpaper.WallpaperManagerService;
import com.android.server.wm.WindowManagerService;
import java.util.List;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class OppoBPMHelper {
    static final String TAG = "OppoProcessManager";
    public static AlarmManagerService mAlarmManagerService;
    public static JobSchedulerService mJobSchedulerService;
    private static boolean sDebugDefault;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.OppoBPMHelper.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.OppoBPMHelper.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.OppoBPMHelper.<clinit>():void");
    }

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
        return wms.getLiveComponent();
    }

    public static final int[] getTouchedWindowPids(WindowManagerService wm) {
        return wm.getInputManagerService().getTouchedWindowPids();
    }

    public static final int[] getLocationListenersUid() {
        return ((LocationManagerService) ServiceManager.getService("location")).getLocationListenersUid();
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
        ResolveInfo defaultLaucher = context.getPackageManager().resolveActivity(intent, DumpState.DUMP_INSTALLS);
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

    public static final void recordResumeLog(int pid, String reason) {
        OppoProcessManagerHelper.recordResumeLog(pid, reason);
    }

    public static final void recordResumeLog(String processName, String reason) {
        OppoProcessManagerHelper.recordResumeLog(processName, reason);
    }

    public static final void resumeForMedia(int uid) {
        OppoProcessManagerHelper.resumeForMedia(uid);
    }

    public static final void setAlarmService(AlarmManagerService alarmManagerService) {
        mAlarmManagerService = alarmManagerService;
    }

    public static final void setJobSchedulerService(JobSchedulerService jobSchedulerService) {
        mJobSchedulerService = jobSchedulerService;
    }

    public static final void notifyStopStrictMode() {
        if (mAlarmManagerService != null) {
            mAlarmManagerService.stopStrictMode();
        }
        if (mJobSchedulerService != null) {
            mJobSchedulerService.stopStrictMode();
        }
    }

    public void resumeProcessByUID(int uid, String reason) {
        OppoProcessManagerHelper.resumeProcessByUID(uid, reason);
    }
}
