package com.android.server.am;

import android.content.ComponentName;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Slog;
import java.util.HashMap;

public class OppoProcessManagerHelper {
    private static final int SUPEND_TIME_INTERVAL = 20000;
    private static final String TAG = "OppoProcessManager";
    private static final int TIMEOUT_LIST_LENGTH = 50;
    private static ActivityManagerService sActivityManagerService = null;
    private static HashMap<Integer, Long> sAppTimeOutMap = new HashMap();
    private static boolean sDebugDetail = OppoProcessManager.sDebugDetail;
    public static OppoProcessManager sOppoProcessManager = null;

    public static void init(ActivityManagerService ams) {
        OppoProcessManager.getInstance().init(ams);
        sOppoProcessManager = OppoProcessManager.getInstance();
        sActivityManagerService = ams;
    }

    static final boolean checkProcessCanRestart(ProcessRecord app) {
        return sOppoProcessManager.checkProcessCanRestart(app);
    }

    static final void checkAppInLaunchingProviders(ProcessRecord app) {
        ContentProviderRecord cpr;
        Slog.i("OppoProcessManager", app + " died but not restart......");
        if (!app.pubProviders.isEmpty()) {
            for (ContentProviderRecord cpr2 : app.pubProviders.values()) {
                sActivityManagerService.removeDyingProviderLocked(app, cpr2, true);
                cpr2.provider = null;
                cpr2.proc = null;
            }
            app.pubProviders.clear();
        }
        int length = sActivityManagerService.mLaunchingProviders.size();
        for (int i = 0; i < length; i++) {
            cpr2 = (ContentProviderRecord) sActivityManagerService.mLaunchingProviders.get(i);
            if (cpr2.launchingApp == app) {
                sActivityManagerService.removeDyingProviderLocked(app, cpr2, true);
                length = sActivityManagerService.mLaunchingProviders.size();
            }
        }
    }

    static final boolean checkProcessWhileTimeout(ProcessRecord proc) {
        if (proc == null) {
            return false;
        }
        if (proc != null && proc.uid < 10000) {
            return false;
        }
        Slog.i("OppoProcessManager", "checkProcessWhileTimeout !  " + proc.processName);
        long time = SystemClock.elapsedRealtime();
        if (isFrozingByUid(proc.uid)) {
            if (sAppTimeOutMap.containsKey(Integer.valueOf(proc.uid))) {
                sAppTimeOutMap.remove(Integer.valueOf(proc.uid));
                sAppTimeOutMap.put(Integer.valueOf(proc.uid), Long.valueOf(time));
            } else {
                if (sAppTimeOutMap.size() > 50) {
                    sAppTimeOutMap.clear();
                }
                sAppTimeOutMap.put(Integer.valueOf(proc.uid), Long.valueOf(time));
            }
            if (isInStrictMode()) {
                Slog.i("OppoProcessManager", "service timeout for suspend, kill it in bg!  " + proc.processName);
                proc.killedByAm = true;
                Process.killProcessQuiet(proc.pid);
                return true;
            }
            setPackageResume(proc.uid, proc.processName, OppoProcessManager.RESUME_REASON_SERVICE_TIMEOUT_STR);
            return true;
        } else if (!sAppTimeOutMap.containsKey(Integer.valueOf(proc.uid)) || Math.abs(time - ((Long) sAppTimeOutMap.get(Integer.valueOf(proc.uid))).longValue()) >= 20000) {
            return false;
        } else {
            Slog.i("OppoProcessManager", "checkProcessWhileTimeout maybe proc is suspend!  " + proc.processName);
            return true;
        }
    }

    static final boolean checkProcessWhileBroadcastTimeout(ProcessRecord proc) {
        if (proc == null) {
            return false;
        }
        if (proc != null && proc.uid < 10000) {
            return false;
        }
        Slog.i("OppoProcessManager", "checkProcessWhileBroadcastTimeout !  " + proc);
        long time = SystemClock.elapsedRealtime();
        String procName = proc.processName;
        if (isFrozingByUid(proc.uid)) {
            Slog.i("OppoProcessManager", "broadcast proc is suspend, resume it in background!  " + procName);
            if (sAppTimeOutMap.containsKey(Integer.valueOf(proc.uid))) {
                sAppTimeOutMap.remove(Integer.valueOf(proc.uid));
                sAppTimeOutMap.put(Integer.valueOf(proc.uid), Long.valueOf(time));
            } else {
                if (sAppTimeOutMap.size() > 50) {
                    sAppTimeOutMap.clear();
                }
                sAppTimeOutMap.put(Integer.valueOf(proc.uid), Long.valueOf(time));
            }
            if (!isInStrictMode()) {
                setPackageResume(proc.uid, procName, OppoProcessManager.RESUME_REASON_BROADCAST_TIMEOUT_STR);
            }
            return true;
        } else if (!sAppTimeOutMap.containsKey(Integer.valueOf(proc.uid)) || Math.abs(time - ((Long) sAppTimeOutMap.get(Integer.valueOf(proc.uid))).longValue()) >= 20000) {
            return false;
        } else {
            Slog.i("OppoProcessManager", "checkProcessWhileBroadcastTimeout maybe proc is suspend!  " + proc.processName);
            return true;
        }
    }

    static final boolean checkBroadcast(BroadcastQueue queue, ProcessRecord app, BroadcastRecord r) throws RemoteException {
        if (!sOppoProcessManager.skipBroadcast(app, r, r.ordered)) {
            return true;
        }
        if (sOppoProcessManager.mDynamicDebug) {
            Slog.i("OppoProcessManager", "BPM skip: receiving " + r.intent.toString() + " to " + app.processName + " (pid=" + app.pid + " due to sender " + r.callerPackage + " (uid " + r.callingUid + ")" + " is ordered " + r.ordered);
        }
        queue.skipCurrentReceiverLocked(app);
        return false;
    }

    static final boolean skipBroadcast(BroadcastFilter filter, BroadcastRecord r, boolean ordered) {
        if (!sOppoProcessManager.skipBroadcast(filter.receiverList.app, r, ordered)) {
            return false;
        }
        if (sOppoProcessManager.mDynamicDebug) {
            Slog.i("OppoProcessManager", "BPM Denial: receiving " + r.intent.toString() + " to " + filter.receiverList.app + " (pid=" + filter.receiverList.pid + ", uid=" + filter.receiverList.uid + ")" + " due to sender " + r.callerPackage + " (uid " + r.callingUid + ")" + " is ordered " + r.ordered + "   ordered " + ordered);
        }
        return true;
    }

    public static final boolean isInStrictMode() {
        return sOppoProcessManager.isStrictMode();
    }

    public static final boolean isDelayAppAlarm(int callerPid, int callerUid, int calledUid, String calledPkg) {
        if (sOppoProcessManager.mDynamicDebug) {
            Slog.i("OppoProcessManager", "isDelayAppAlarm callerPid = " + callerPid + "  callerUid = " + callerUid + "  calledUid = " + calledUid + "  calledPkg = " + calledPkg);
        }
        return sOppoProcessManager.isDelayAppAlarm(callerPid, callerUid, calledUid, calledPkg);
    }

    public static final boolean isDelayAppSync(int uid, String pkg) {
        if (sOppoProcessManager.mDebugSwitch) {
            Slog.i("OppoProcessManager", "isDelayAppSync  Uid = " + uid + "  Pkg = " + pkg);
        }
        return sOppoProcessManager.isDelayAppSync(uid, pkg);
    }

    public static final boolean isDelayAppJob(int uid, String pkg) {
        if (sOppoProcessManager.mDebugSwitch) {
            Slog.i("OppoProcessManager", "isDelayAppJob  Uid = " + uid + "  Pkg = " + pkg);
        }
        return sOppoProcessManager.isDelayAppJob(uid, pkg);
    }

    public static final void enterStrictMode() {
        sOppoProcessManager.enterStrictMode();
    }

    public static final void stopStrictMode() {
        sOppoProcessManager.stopStrictMode();
    }

    public static final boolean isFrozingByUid(int uid) {
        return sOppoProcessManager.isFrozingByUid(uid);
    }

    public static final void setPackageResume(int uid, String packageName, String reason) {
        sOppoProcessManager.setPackageResume(uid, packageName, reason);
    }

    public static final void setPackageResume(int uid, String packageName, int timeout, int isTargetFreeze, String reason) {
        sOppoProcessManager.setPackageResume(uid, packageName, timeout, isTargetFreeze, reason);
    }

    public static final void noteWindowStateChange(int uid, int pid, int windowId, int windowType, boolean isVisible, boolean shown) {
        sOppoProcessManager.noteWindowStateChange(uid, pid, windowId, windowType, isVisible, shown);
    }

    public static final void resumeTopApp(ProcessRecord app) {
        sOppoProcessManager.resumeTopApp(app);
    }

    public static final void resumeAppAssociated(int calledUid, ComponentName calledName) {
        sOppoProcessManager.resumeAppAssociated(calledUid, calledName);
    }
}
