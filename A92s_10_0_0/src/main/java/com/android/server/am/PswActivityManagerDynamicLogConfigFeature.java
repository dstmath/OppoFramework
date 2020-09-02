package com.android.server.am;

import android.app.IApplicationThread;
import android.common.OppoFeatureCache;
import android.os.Build;
import android.os.SystemProperties;
import android.util.Slog;
import com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils;
import com.android.server.oppo.DumpObject;
import com.android.server.wm.ActivityTaskManagerDynamicLogConfig;
import com.color.util.ColorTypeCastingHelper;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class PswActivityManagerDynamicLogConfigFeature implements IPswActivityManagerDynamicLogConfigFeature {
    private static final String TAG = "ActivityManager";
    private static PswActivityManagerDynamicLogConfigFeature mInstance = null;
    private static final Object mLock = new Object();
    private IColorActivityManagerServiceInner mAmsInner = null;

    private PswActivityManagerDynamicLogConfigFeature() {
        init();
    }

    public static PswActivityManagerDynamicLogConfigFeature getInstance() {
        PswActivityManagerDynamicLogConfigFeature pswActivityManagerDynamicLogConfigFeature;
        synchronized (mLock) {
            if (mInstance == null) {
                mInstance = new PswActivityManagerDynamicLogConfigFeature();
            }
            pswActivityManagerDynamicLogConfigFeature = mInstance;
        }
        return pswActivityManagerDynamicLogConfigFeature;
    }

    private void init() {
        Slog.d(TAG, "PswActivityManagerDynamicLogConfigFeature init.");
    }

    public boolean doDump(ActivityManagerService ams, String cmd, FileDescriptor fd, PrintWriter pw, String[] args, int opti) {
        if ((Build.HARDWARE.startsWith("mt") ? "oppo-log" : "log").equals(cmd)) {
            dynamicallyConfigLogTag(ams, pw, args, opti);
            return true;
        } else if (!"get_value".equals(cmd)) {
            return false;
        } else {
            dynamicGetValue(ams, pw, args);
            return true;
        }
    }

    private void dynamicallyConfigLogTag(ActivityManagerService ams, PrintWriter pw, String[] args, int opti) {
        pw.println("dynamicallyConfigLogTag, opti:" + opti + ", args.length:" + args.length);
        for (int index = 0; index < args.length; index++) {
            pw.println("dynamicallyConfigLogTag, args[" + index + "]:" + args[index]);
        }
        if (args.length != 3) {
            pw.println("********** Invalid argument! Get detail help as bellow: **********");
            logoutTagConfigHelp(pw);
            return;
        }
        String tag = args[1];
        boolean on = "1".equals(args[2]);
        pw.println("dynamicallyConfigLogTag, tag:" + tag + ", on:" + on);
        if ("broadcast".equals(tag)) {
            pw.println("dl Ams bc, A:" + ActivityManagerDebugConfig.DEBUG_BROADCAST + ", " + ActivityManagerDebugConfig.DEBUG_BROADCAST_BACKGROUND + ", " + ActivityManagerDebugConfig.DEBUG_BROADCAST_LIGHT);
            OppoMirrorActivityManagerDebugConfig.setBooleanValue(OppoMirrorActivityManagerDebugConfig.DEBUG_BROADCAST, on);
            OppoMirrorActivityManagerDebugConfig.setBooleanValue(OppoMirrorActivityManagerDebugConfig.DEBUG_BROADCAST_BACKGROUND, on);
            OppoMirrorActivityManagerDebugConfig.setBooleanValue(OppoMirrorActivityManagerDebugConfig.DEBUG_BROADCAST_LIGHT, on);
            pw.println("dl Ams bc, B:" + ActivityManagerDebugConfig.DEBUG_BROADCAST + ", " + ActivityManagerDebugConfig.DEBUG_BROADCAST_BACKGROUND + ", " + ActivityManagerDebugConfig.DEBUG_BROADCAST_LIGHT);
        } else if ("oppobroadcast".equals(tag)) {
            OppoFeatureCache.get(IColorBroadcastManager.DEFAULT).handleDynamicLog(on);
        } else if ("service".equals(tag)) {
            OppoMirrorActivityManagerDebugConfig.setBooleanValue(OppoMirrorActivityManagerDebugConfig.DEBUG_SERVICE, on);
            OppoMirrorActivityManagerDebugConfig.setBooleanValue(OppoMirrorActivityManagerDebugConfig.DEBUG_SERVICE_EXECUTING, on);
            OppoMirrorActivityManagerDebugConfig.setBooleanValue(OppoMirrorActivityManagerDebugConfig.DEBUG_MU, on);
            OppoMirrorActivityManagerDebugConfig.setBooleanValue(OppoMirrorActivityManagerDebugConfig.DEBUG_BACKGROUND_CHECK, on);
            OppoMirrorActivityManagerDebugConfig.setBooleanValue(OppoMirrorActivityManagerDebugConfig.DEBUG_FOREGROUND_SERVICE, on);
            if (getColorActivityManagerServiceInner(ams) != null) {
                this.mAmsInner.dynamicalConfigLog("ActiveServices", (IApplicationThread) null, on);
            }
        } else if ("provider".equals(tag)) {
            OppoMirrorActivityManagerDebugConfig.setBooleanValue(OppoMirrorActivityManagerDebugConfig.DEBUG_PROVIDER, on);
            OppoMirrorActivityManagerDebugConfig.setBooleanValue(OppoMirrorActivityManagerDebugConfig.DEBUG_MU, on);
        } else if ("backup".equals(tag)) {
            OppoMirrorActivityManagerDebugConfig.setBooleanValue(OppoMirrorActivityManagerDebugConfig.DEBUG_BACKUP, on);
        } else if ("process".equals(tag)) {
            OppoMirrorActivityManagerDebugConfig.setBooleanValue(OppoMirrorActivityManagerDebugConfig.DEBUG_PROCESSES, on);
            OppoMirrorActivityManagerDebugConfig.setBooleanValue(OppoMirrorActivityManagerDebugConfig.DEBUG_PROCESS_OBSERVERS, on);
        } else if ("other".equals(tag)) {
            OppoMirrorActivityManagerDebugConfig.setBooleanValue(OppoMirrorActivityManagerDebugConfig.DEBUG_POWER, on);
            OppoMirrorActivityManagerDebugConfig.setBooleanValue(OppoMirrorActivityManagerDebugConfig.DEBUG_OOM_ADJ, on);
            OppoMirrorActivityManagerDebugConfig.setBooleanValue(OppoMirrorActivityManagerDebugConfig.DEBUG_LRU, on);
            SystemProperties.set("sys.activity.thread.log", on ? "true" : "false");
            for (int i = ams.mProcessList.mLruProcesses.size() - 1; i >= 0; i--) {
                ProcessRecord app = (ProcessRecord) ams.mProcessList.mLruProcesses.get(i);
                if (app.thread != null) {
                    Slog.v(TAG, "app.thread.setDynamicalLogEnable app " + app + " on " + on);
                    if (getColorActivityManagerServiceInner(ams) != null) {
                        this.mAmsInner.dynamicalConfigLog("ActivityThread", app.thread, on);
                    }
                }
            }
        } else if (!ActivityTaskManagerDynamicLogConfig.dynamicallyConfigLogTag(pw, args, opti)) {
            pw.println("Failed! Invalid argument! Type cmd for help: dumpsys activity log");
        }
    }

    private void dynamicGetValue(ActivityManagerService ams, PrintWriter pw, String[] args) {
        if (args.length == 1 || args.length == 2) {
            new DumpObject().dumpValue(pw, ams, args.length == 2 ? args[1] : StringUtils.EMPTY);
            return;
        }
        pw.println("get_value usage:");
        pw.println("dumpsys activity get_value");
        pw.println("or");
        pw.println("dumpsys activity get_value variable");
    }

    private void logoutTagConfigHelp(PrintWriter pw) {
        pw.println("********************** Help begin:**********************");
        pw.println("1 Activity life circle:DEBUG_SWITCH | DEBUG_PAUSE DEBUG_RESULTS | DEBUG_CLEANUP | DEBUG_STATES");
        pw.println("cmd: dumpsys activity log life 0/1");
        pw.println("----------------------------------");
        pw.println("2 App visibility:DEBUG_VISIBILITY | DEBUG_TRANSITION | DEBUG_FOCUS");
        pw.println("cmd: dumpsys activity log visibility 0/1");
        pw.println("----------------------------------");
        pw.println("3 Config process:DEBUG_CONFIGURATION");
        pw.println("cmd: dumpsys activity log config 0/1");
        pw.println("----------------------------------");
        pw.println("4 Task manage:DEBUG_TASKS | DEBUG_CLEANUP | DEBUG_ADD_REMOVE | DEBUG_SAVED_STATE");
        pw.println("cmd: dumpsys activity log task 0/1");
        pw.println("----------------------------------");
        pw.println("5 Broadcast manage:DEBUG_BROADCAST | DEBUG_BROADCAST_BACKGROUND | DEBUG_BROADCAST_LIGHT");
        pw.println("cmd: dumpsys activity log broadcast 0/1");
        pw.println("----------------------------------");
        pw.println("6 Service manage:DEBUG_SERVICE | DEBUG_SERVICE_EXECUTING | DEBUG_MU | ActiveServices's Log");
        pw.println("cmd: dumpsys activity log service 0/1");
        pw.println("----------------------------------");
        pw.println("7 Provider manage:DEBUG_PROVIDER | DEBUG_URI_PERMISSION | DEBUG_MU");
        pw.println("cmd: dumpsys activity log provider 0/1");
        pw.println("----------------------------------");
        pw.println("8 backup manage:DEBUG_BACKUP");
        pw.println("cmd: dumpsys activity log backup 0/1");
        pw.println("----------------------------------");
        pw.println("9 stack manager log:Ams stack & ActivityStackSupervisor & ActivityStack");
        pw.println("cmd: dumpsys activity log stack 0/1");
        pw.println("----------------------------------");
        pw.println("10 process manage:DEBUG_PROCESSES | DEBUG_PROCESS_OBSERVERS");
        pw.println("cmd: dumpsys activity log process 0/1");
        pw.println("----------------------------------");
        pw.println("11 oppoBroadcast manage:DEBUG_JUMP_QUEUE | DEBUG_ADJUST_PB_REC_QUE | DEBUG_ADJUST_OB_REC_QUE | DEBUG_BROADCAST_FIREWALL ");
        pw.println("cmd: dumpsys activity log oppobroadcast 0/1");
        pw.println("----------------------------------");
        pw.println("12 other not in common use:DEBUG_POWER | DEBUG_THUMBNAILS | DEBUG_OOM_ADJ | DEBUG_LOCKSCREEN | DEBUG_LRU | DEBUG_PSS ");
        pw.println("cmd: dumpsys activity log other 0/1");
        pw.println("----------------------------------");
        pw.println("13 fstrim:DEBUG_FSTRIM");
        pw.println("cmd: dumpsys activity log fstrim 0/1");
        pw.println("----------------------------------");
        pw.println("14 fstrim:DEBUG_PERMISSION");
        pw.println("cmd: dumpsys activity log permission 0/1");
        pw.println("----------------------------------");
        pw.println("15 junk:DEBUG_JUNK");
        pw.println("cmd: dumpsys activity log junk 0/1");
        pw.println("----------------------------------");
        pw.println("********************** Help end.  **********************");
    }

    private OppoBaseActivityManagerService typeCasting(ActivityManagerService ams) {
        if (ams != null) {
            return (OppoBaseActivityManagerService) ColorTypeCastingHelper.typeCasting(OppoBaseActivityManagerService.class, ams);
        }
        return null;
    }

    public IColorActivityManagerServiceInner getColorActivityManagerServiceInner(ActivityManagerService ams) {
        IColorActivityManagerServiceInner iColorActivityManagerServiceInner = this.mAmsInner;
        if (iColorActivityManagerServiceInner != null) {
            return iColorActivityManagerServiceInner;
        }
        OppoBaseActivityManagerService baseAms = typeCasting(ams);
        if (baseAms != null) {
            this.mAmsInner = baseAms.mColorAmsInner;
        }
        return this.mAmsInner;
    }
}
