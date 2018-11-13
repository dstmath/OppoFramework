package com.android.server.am;

import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;
import android.util.Slog;
import java.util.HashMap;

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
public class OppoProcessManagerHelper {
    private static final int BROADCAST_TIMEOUT_LIST_LENGTH = 30;
    private static final int SIGNAL_CONT = 18;
    private static final int SIGNAL_STOP = 19;
    private static final int SUPEND_TIME_INTERVAL = 20000;
    private static final String TAG = "OppoProcessManager";
    private static ActivityManagerService mActivityManagerService;
    private static HashMap<String, Long> mBroadcastTimeOutMap;
    private static boolean mDebugDetail;
    public static OppoProcessManager mOppoProcessManager;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.am.OppoProcessManagerHelper.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.am.OppoProcessManagerHelper.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.am.OppoProcessManagerHelper.<clinit>():void");
    }

    public static void init(ActivityManagerService ams) {
        OppoProcessManager.getInstance().init(ams);
        mOppoProcessManager = OppoProcessManager.getInstance();
        mActivityManagerService = ams;
    }

    static final void resumeProvider(ContentProviderRecord cpr) {
        if (mOppoProcessManager.isEnable() && cpr.proc != null) {
            mOppoProcessManager.resumeProcess(cpr.proc, 3);
        }
    }

    static final boolean checkProcessCanRestart(ProcessRecord app) {
        return mOppoProcessManager.checkProcessCanRestart(app);
    }

    static final void checkAppInLaunchingProviders(ProcessRecord app) {
        ContentProviderRecord cpr;
        Slog.i("OppoProcessManager", app + " died but not restart......");
        if (!app.pubProviders.isEmpty()) {
            for (ContentProviderRecord cpr2 : app.pubProviders.values()) {
                mActivityManagerService.removeDyingProviderLocked(app, cpr2, true);
                cpr2.provider = null;
                cpr2.proc = null;
            }
            app.pubProviders.clear();
        }
        int length = mActivityManagerService.mLaunchingProviders.size();
        for (int i = 0; i < length; i++) {
            cpr2 = (ContentProviderRecord) mActivityManagerService.mLaunchingProviders.get(i);
            if (cpr2.launchingApp == app) {
                mActivityManagerService.removeDyingProviderLocked(app, cpr2, true);
                length = mActivityManagerService.mLaunchingProviders.size();
            }
        }
    }

    /* JADX WARNING: Missing block: B:6:0x0010, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static final void resumeProcessForService(ServiceRecord r, boolean fg, String why) {
        if (mOppoProcessManager.isEnable() && r != null && r.app != null && !mOppoProcessManager.checkWhiteProcessRecord(r.app)) {
            if (isInStrictMode()) {
                if (mOppoProcessManager.mDynamicDebug) {
                    Slog.i("OppoProcessManager", "resumeProcessForService uid = " + r.app.uid + "  r = " + r + "  fg = " + fg + "  why = " + why);
                    Slog.i("OppoProcessManager", Log.getStackTraceString(new Throwable()));
                }
                mOppoProcessManager.resumeProcess(r.app, 2);
            } else {
                mOppoProcessManager.resumeProcessByUID(r.app.uid, 2);
            }
        }
    }

    static final void resumeProcessForSystemCall(ProcessRecord proc) {
        if (proc != null && mOppoProcessManager.isProcessSuspended(proc)) {
            Process.sendSignal(proc.pid, 18);
            recordResumeLog(proc.pid, OppoProcessManager.RESUME_REASON_SYSTEM_CALL_STR);
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
        if (mOppoProcessManager.isProcessSuspended(proc)) {
            if (isInStrictMode()) {
                Slog.i("OppoProcessManager", "service timeout for suspend, kill it in bg!  " + proc.processName);
                proc.killedByAm = true;
                Process.killProcessQuiet(proc.pid);
                return true;
            }
            Slog.i("OppoProcessManager", "service is suspend, resume it in background!  " + proc.processName);
            Process.sendSignal(proc.pid, 18);
            recordResumeLog(proc.processName, OppoProcessManager.RESUME_REASON_SERVICE_TIMEOUT_STR);
            return true;
        } else if (!mOppoProcessManager.isUidGroupHasSuspended(proc)) {
            return false;
        } else {
            if (isInStrictMode()) {
                Slog.i("OppoProcessManager", "service timeout for the same uid's proc suspend, kill it in bg!  " + proc.processName);
                proc.killedByAm = true;
                Process.killProcessQuiet(proc.pid);
                return true;
            }
            Slog.i("OppoProcessManager", "checkProcessWhileTimeout the same uid's proc has suspend!  " + proc.processName);
            mOppoProcessManager.resumeProcessByUID(proc.uid, 11);
            return true;
        }
    }

    static final boolean checkProcessWhileBroadcastTimeout(ProcessRecord proc) {
        Slog.i("OppoProcessManager", "checkProcessWhileBroadcastTimeout proc = " + proc);
        if (proc == null) {
            return false;
        }
        if (proc != null && proc.uid < 10000) {
            return false;
        }
        Slog.i("OppoProcessManager", "checkProcessWhileBroadcastTimeout !  " + proc.processName);
        long time = SystemClock.elapsedRealtime();
        String procName = proc.processName;
        if (mOppoProcessManager.isProcessSuspended(proc)) {
            Slog.i("OppoProcessManager", "broadcast proc is suspend, resume it in background!  " + procName);
            if (mBroadcastTimeOutMap.containsKey(procName)) {
                mBroadcastTimeOutMap.remove(procName);
                mBroadcastTimeOutMap.put(procName, Long.valueOf(time));
            } else {
                if (mBroadcastTimeOutMap.size() > 30) {
                    mBroadcastTimeOutMap.clear();
                }
                mBroadcastTimeOutMap.put(procName, Long.valueOf(time));
            }
            if (!isInStrictMode()) {
                Process.sendSignal(proc.pid, 18);
                recordResumeLog(procName, OppoProcessManager.RESUME_REASON_BROADCAST_TIMEOUT_STR);
            }
            return true;
        } else if (mBroadcastTimeOutMap.containsKey(procName) && Math.abs(time - ((Long) mBroadcastTimeOutMap.get(procName)).longValue()) < 20000) {
            Slog.i("OppoProcessManager", "checkProcessWhileBroadcastTimeout maybe proc is suspend!  " + proc.processName);
            return true;
        } else if (!mOppoProcessManager.isUidGroupHasSuspended(proc)) {
            return false;
        } else {
            Slog.i("OppoProcessManager", "checkProcessWhileBroadcastTimeout the same uid's proc has suspend!  " + proc.processName);
            if (!isInStrictMode()) {
                mOppoProcessManager.resumeProcessByUID(proc.uid, 10);
            }
            return true;
        }
    }

    static final void updateProcessState(ProcessRecord app) {
        if (mActivityManagerService.mSystemReady) {
            mOppoProcessManager.updateProcessState(app);
        }
    }

    static final boolean checkBroadcast(BroadcastQueue queue, ProcessRecord app, BroadcastRecord r) throws RemoteException {
        if (!mOppoProcessManager.skipBroadcast(app, r, r.ordered)) {
            return true;
        }
        if (mOppoProcessManager.mDynamicDebug) {
            Slog.i("OppoProcessManager", "BPM skip: receiving " + r.intent.toString() + " to " + app.processName + " (pid=" + app.pid + " due to sender " + r.callerPackage + " (uid " + r.callingUid + ")" + " is ordered " + r.ordered);
        }
        queue.skipCurrentReceiverLocked(app);
        return false;
    }

    static final boolean skipBroadcast(BroadcastFilter filter, BroadcastRecord r, boolean ordered) {
        if (!mOppoProcessManager.skipBroadcast(filter.receiverList.app, r, ordered)) {
            return false;
        }
        if (mOppoProcessManager.mDynamicDebug) {
            Slog.i("OppoProcessManager", "BPM Denial: receiving " + r.intent.toString() + " to " + filter.receiverList.app + " (pid=" + filter.receiverList.pid + ", uid=" + filter.receiverList.uid + ")" + " due to sender " + r.callerPackage + " (uid " + r.callingUid + ")" + " is ordered " + r.ordered + "   ordered " + ordered);
        }
        return true;
    }

    public static final void recordResumeLog(int pid, String reason) {
        if (mDebugDetail) {
            Slog.i("OppoProcessManager", "bpmhandle: resume " + pid + " reason is " + reason);
        }
        mOppoProcessManager.sendBpmMessage(pid, 140, 10000, mOppoProcessManager.strToReasonCode(reason));
    }

    public static final void recordResumeLog(String processName, String reason) {
        if (mDebugDetail) {
            Slog.i("OppoProcessManager", "bpmhandle: resume " + processName + " reason is " + reason);
        }
        mOppoProcessManager.increaseResumeInfo(processName, mOppoProcessManager.strToReasonCode(reason));
    }

    public static final void resumeForMedia(int uid) {
        if (mDebugDetail) {
            Slog.i("OppoProcessManager", "bpmhandle: resume " + uid + " reason is media");
        }
        mOppoProcessManager.resumeProcessByUID(uid, 7);
    }

    public static final boolean isInStrictMode() {
        return mOppoProcessManager.isStrictMode();
    }

    public static final boolean isDelayAppAlarm(int callerPid, int callerUid, int calledUid, String calledPkg) {
        if (mOppoProcessManager.mDynamicDebug) {
            Slog.i("OppoProcessManager", "isDelayAppAlarm callerPid = " + callerPid + "  callerUid = " + callerUid + "  calledUid = " + calledUid + "  calledPkg = " + calledPkg);
        }
        return mOppoProcessManager.isDelayAppAlarm(callerPid, callerUid, calledUid, calledPkg);
    }

    public static final boolean isDelayAppSync(int uid, String pkg) {
        if (mOppoProcessManager.mDebugSwitch) {
            Slog.i("OppoProcessManager", "isDelayAppSync  Uid = " + uid + "  Pkg = " + pkg);
        }
        return mOppoProcessManager.isDelayAppSync(uid, pkg);
    }

    public static final boolean isDelayAppJob(int uid, String pkg) {
        if (mOppoProcessManager.mDebugSwitch) {
            Slog.i("OppoProcessManager", "isDelayAppJob  Uid = " + uid + "  Pkg = " + pkg);
        }
        return mOppoProcessManager.isDelayAppJob(uid, pkg);
    }

    public static final void enterStrictMode() {
        mOppoProcessManager.enterStrictMode();
    }

    public static final void stopStrictMode() {
        mOppoProcessManager.stopStrictMode();
    }

    public static final void resumeProcessByUID(int uid, String reason) {
        mOppoProcessManager.resumeProcessByUID(uid, mOppoProcessManager.strToReasonCode(reason));
    }

    public static final void handleAppDied(ProcessRecord app) {
        mOppoProcessManager.handleAppDied(app);
    }
}
