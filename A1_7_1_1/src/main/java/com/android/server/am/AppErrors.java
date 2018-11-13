package com.android.server.am;

import android.app.ActivityManager.ProcessErrorStateInfo;
import android.app.ActivityThread;
import android.app.ApplicationErrorReport;
import android.app.ApplicationErrorReport.AnrInfo;
import android.app.ApplicationErrorReport.CrashInfo;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Binder;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Secure;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.EventLog;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import android.util.TimeUtils;
import com.android.internal.app.ProcessMap;
import com.android.internal.logging.MetricsLogger;
import com.android.server.LocationManagerService;
import com.android.server.Watchdog;
import com.android.server.am.AppErrorDialog.Data;
import com.android.server.oppo.IElsaManager;
import com.mediatek.anrmanager.ANRManager;
import com.mediatek.anrmanager.ANRManager.AnrDumpRecord;
import com.mediatek.anrmanager.ANRManager.BinderDumpThread;
import com.mediatek.server.am.AMEventHook;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
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
class AppErrors {
    public static final String[] LIGHTWEIGHT_NATIVE_STACKS_OF_INTEREST = null;
    private static final String TAG = null;
    private AMEventHook mAMEventHook;
    private ArraySet<String> mAppsNotReportingCrashes;
    private final ProcessMap<BadProcessInfo> mBadProcesses;
    private final Context mContext;
    private ArrayList<String> mInterestAnrAppProcNames;
    private final ProcessMap<Long> mProcessCrashTimes;
    private final ProcessMap<Long> mProcessCrashTimesPersistent;
    private final ActivityManagerService mService;

    static final class BadProcessInfo {
        final String longMsg;
        final String shortMsg;
        final String stack;
        final long time;

        BadProcessInfo(long time, String shortMsg, String longMsg, String stack) {
            this.time = time;
            this.shortMsg = shortMsg;
            this.longMsg = longMsg;
            this.stack = stack;
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.am.AppErrors.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.am.AppErrors.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.am.AppErrors.<clinit>():void");
    }

    AppErrors(Context context, ActivityManagerService service) {
        this.mProcessCrashTimes = new ProcessMap();
        this.mProcessCrashTimesPersistent = new ProcessMap();
        this.mBadProcesses = new ProcessMap();
        this.mInterestAnrAppProcNames = null;
        this.mAMEventHook = AMEventHook.createInstance();
        this.mService = service;
        this.mContext = context;
    }

    boolean dumpLocked(FileDescriptor fd, PrintWriter pw, boolean needSep, String dumpPackage) {
        boolean printed;
        int processCount;
        int ip;
        String pname;
        int uidCount;
        int i;
        int puid;
        ProcessRecord r;
        if (!this.mProcessCrashTimes.getMap().isEmpty()) {
            printed = false;
            long now = SystemClock.uptimeMillis();
            ArrayMap<String, SparseArray<Long>> pmap = this.mProcessCrashTimes.getMap();
            processCount = pmap.size();
            for (ip = 0; ip < processCount; ip++) {
                pname = (String) pmap.keyAt(ip);
                SparseArray<Long> uids = (SparseArray) pmap.valueAt(ip);
                uidCount = uids.size();
                for (i = 0; i < uidCount; i++) {
                    puid = uids.keyAt(i);
                    r = (ProcessRecord) this.mService.mProcessNames.get(pname, puid);
                    if (dumpPackage == null || (r != null && r.pkgList.containsKey(dumpPackage))) {
                        if (!printed) {
                            if (needSep) {
                                pw.println();
                            }
                            needSep = true;
                            pw.println("  Time since processes crashed:");
                            printed = true;
                        }
                        pw.print("    Process ");
                        pw.print(pname);
                        pw.print(" uid ");
                        pw.print(puid);
                        pw.print(": last crashed ");
                        TimeUtils.formatDuration(now - ((Long) uids.valueAt(i)).longValue(), pw);
                        pw.println(" ago");
                    }
                }
            }
        }
        if (!this.mBadProcesses.getMap().isEmpty()) {
            printed = false;
            ArrayMap<String, SparseArray<BadProcessInfo>> pmap2 = this.mBadProcesses.getMap();
            processCount = pmap2.size();
            for (ip = 0; ip < processCount; ip++) {
                pname = (String) pmap2.keyAt(ip);
                SparseArray<BadProcessInfo> uids2 = (SparseArray) pmap2.valueAt(ip);
                uidCount = uids2.size();
                for (i = 0; i < uidCount; i++) {
                    puid = uids2.keyAt(i);
                    r = (ProcessRecord) this.mService.mProcessNames.get(pname, puid);
                    if (dumpPackage == null || (r != null && r.pkgList.containsKey(dumpPackage))) {
                        if (!printed) {
                            if (needSep) {
                                pw.println();
                            }
                            needSep = true;
                            pw.println("  Bad processes:");
                            printed = true;
                        }
                        BadProcessInfo info = (BadProcessInfo) uids2.valueAt(i);
                        pw.print("    Bad process ");
                        pw.print(pname);
                        pw.print(" uid ");
                        pw.print(puid);
                        pw.print(": crashed at time ");
                        pw.println(info.time);
                        if (info.shortMsg != null) {
                            pw.print("      Short msg: ");
                            pw.println(info.shortMsg);
                        }
                        if (info.longMsg != null) {
                            pw.print("      Long msg: ");
                            pw.println(info.longMsg);
                        }
                        if (info.stack != null) {
                            pw.println("      Stack:");
                            int lastPos = 0;
                            for (int pos = 0; pos < info.stack.length(); pos++) {
                                if (info.stack.charAt(pos) == 10) {
                                    pw.print("        ");
                                    pw.write(info.stack, lastPos, pos - lastPos);
                                    pw.println();
                                    lastPos = pos + 1;
                                }
                            }
                            if (lastPos < info.stack.length()) {
                                pw.print("        ");
                                pw.write(info.stack, lastPos, info.stack.length() - lastPos);
                                pw.println();
                            }
                        }
                    }
                }
            }
        }
        return needSep;
    }

    boolean isBadProcessLocked(ApplicationInfo info) {
        return this.mBadProcesses.get(info.processName, info.uid) != null;
    }

    void clearBadProcessLocked(ApplicationInfo info) {
        this.mBadProcesses.remove(info.processName, info.uid);
    }

    void resetProcessCrashTimeLocked(ApplicationInfo info) {
        this.mProcessCrashTimes.remove(info.processName, info.uid);
    }

    void resetProcessCrashTimeLocked(boolean resetEntireUser, int appId, int userId) {
        ArrayMap<String, SparseArray<Long>> pmap = this.mProcessCrashTimes.getMap();
        for (int ip = pmap.size() - 1; ip >= 0; ip--) {
            SparseArray<Long> ba = (SparseArray) pmap.valueAt(ip);
            for (int i = ba.size() - 1; i >= 0; i--) {
                boolean remove = false;
                int entUid = ba.keyAt(i);
                if (resetEntireUser) {
                    if (UserHandle.getUserId(entUid) == userId) {
                        remove = true;
                    }
                } else if (userId == -1) {
                    if (UserHandle.getAppId(entUid) == appId) {
                        remove = true;
                    }
                } else if (entUid == UserHandle.getUid(userId, appId)) {
                    remove = true;
                }
                if (remove) {
                    ba.removeAt(i);
                }
            }
            if (ba.size() == 0) {
                pmap.removeAt(ip);
            }
        }
    }

    void loadAppsNotReportingCrashesFromConfigLocked(String appsNotReportingCrashesConfig) {
        if (appsNotReportingCrashesConfig != null) {
            String[] split = appsNotReportingCrashesConfig.split(",");
            if (split.length > 0) {
                this.mAppsNotReportingCrashes = new ArraySet();
                Collections.addAll(this.mAppsNotReportingCrashes, split);
            }
        }
    }

    void killAppAtUserRequestLocked(ProcessRecord app, Dialog fromDialog) {
        app.crashing = false;
        app.crashingReport = null;
        app.notResponding = false;
        app.notRespondingReport = null;
        if (app.anrDialog == fromDialog) {
            app.anrDialog = null;
        }
        if (app.waitDialog == fromDialog) {
            app.waitDialog = null;
        }
        if (app.pid > 0 && app.pid != ActivityManagerService.MY_PID) {
            handleAppCrashLocked(app, "user-terminated", null, null, null, null);
            app.kill("user request after error", true);
        }
    }

    void scheduleAppCrashLocked(int uid, int initialPid, String packageName, String message) {
        ProcessRecord proc = null;
        synchronized (this.mService.mPidsSelfLocked) {
            for (int i = 0; i < this.mService.mPidsSelfLocked.size(); i++) {
                ProcessRecord p = (ProcessRecord) this.mService.mPidsSelfLocked.valueAt(i);
                if (p.uid == uid) {
                    if (p.pid == initialPid) {
                        proc = p;
                        break;
                    } else if (p.pkgList.containsKey(packageName)) {
                        proc = p;
                    }
                }
            }
        }
        if (proc == null) {
            Slog.w(TAG, "crashApplication: nothing for uid=" + uid + " initialPid=" + initialPid + " packageName=" + packageName);
        } else {
            proc.scheduleCrash(message);
        }
    }

    void crashApplication(ProcessRecord r, CrashInfo crashInfo) {
        long origId = Binder.clearCallingIdentity();
        try {
            crashApplicationInner(r, crashInfo);
        } finally {
            Binder.restoreCallingIdentity(origId);
        }
    }

    /* JADX WARNING: Missing block: B:29:0x00b6, code:
            com.android.server.am.ActivityManagerService.resetPriorityAfterLockedSection();
            r38 = r39.get();
            r31 = null;
            com.android.internal.logging.MetricsLogger.action(r41.mContext, 316, r38);
     */
    /* JADX WARNING: Missing block: B:30:0x00cd, code:
            if (r38 == 6) goto L_0x00d4;
     */
    /* JADX WARNING: Missing block: B:32:0x00d2, code:
            if (r38 != 7) goto L_0x00d6;
     */
    /* JADX WARNING: Missing block: B:33:0x00d4, code:
            r38 = 1;
     */
    /* JADX WARNING: Missing block: B:34:0x00d6, code:
            r5 = r41.mService;
     */
    /* JADX WARNING: Missing block: B:35:0x00da, code:
            monitor-enter(r5);
     */
    /* JADX WARNING: Missing block: B:37:?, code:
            com.android.server.am.ActivityManagerService.boostPriorityForLockedSection();
     */
    /* JADX WARNING: Missing block: B:38:0x00e1, code:
            if (r38 != 5) goto L_0x00e6;
     */
    /* JADX WARNING: Missing block: B:39:0x00e3, code:
            stopReportingCrashesLocked(r42);
     */
    /* JADX WARNING: Missing block: B:41:0x00e9, code:
            if (r38 != 3) goto L_0x010e;
     */
    /* JADX WARNING: Missing block: B:42:0x00eb, code:
            r41.mService.removeProcessLocked(r42, false, true, "crash");
     */
    /* JADX WARNING: Missing block: B:43:0x00f9, code:
            if (r40 == null) goto L_0x010e;
     */
    /* JADX WARNING: Missing block: B:45:?, code:
            r41.mService.startActivityFromRecents(r40.taskId, android.app.ActivityOptions.makeBasic().toBundle());
     */
    /* JADX WARNING: Missing block: B:47:0x0111, code:
            if (r38 != 1) goto L_0x0142;
     */
    /* JADX WARNING: Missing block: B:49:?, code:
            r36 = android.os.Binder.clearCallingIdentity();
     */
    /* JADX WARNING: Missing block: B:51:?, code:
            r41.mService.mStackSupervisor.handleAppCrashLocked(r42);
     */
    /* JADX WARNING: Missing block: B:52:0x0126, code:
            if (r42.persistent != false) goto L_0x013f;
     */
    /* JADX WARNING: Missing block: B:53:0x0128, code:
            r41.mService.removeProcessLocked(r42, false, false, "crash");
            r41.mService.mStackSupervisor.resumeFocusedStackTopActivityLocked();
     */
    /* JADX WARNING: Missing block: B:55:?, code:
            android.os.Binder.restoreCallingIdentity(r36);
     */
    /* JADX WARNING: Missing block: B:57:0x0145, code:
            if (r38 != 2) goto L_0x0151;
     */
    /* JADX WARNING: Missing block: B:58:0x0147, code:
            r31 = createAppErrorIntentLocked(r42, r10, r43);
     */
    /* JADX WARNING: Missing block: B:59:0x0151, code:
            if (r42 == null) goto L_0x0159;
     */
    /* JADX WARNING: Missing block: B:61:0x0157, code:
            if (r42.isolated == false) goto L_0x01d7;
     */
    /* JADX WARNING: Missing block: B:62:0x0159, code:
            monitor-exit(r5);
     */
    /* JADX WARNING: Missing block: B:63:0x015a, code:
            com.android.server.am.ActivityManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Missing block: B:64:0x015d, code:
            if (r31 == null) goto L_0x0171;
     */
    /* JADX WARNING: Missing block: B:66:?, code:
            r41.mContext.startActivityAsUser(r31, new android.os.UserHandle(r42.userId));
     */
    /* JADX WARNING: Missing block: B:69:0x0173, code:
            com.android.server.am.ActivityManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Missing block: B:70:0x0176, code:
            return;
     */
    /* JADX WARNING: Missing block: B:75:?, code:
            r32 = r40.intent.getCategories();
     */
    /* JADX WARNING: Missing block: B:76:0x0186, code:
            if (r32 != null) goto L_0x0188;
     */
    /* JADX WARNING: Missing block: B:78:0x0191, code:
            if (r32.contains("android.intent.category.LAUNCHER") != false) goto L_0x0193;
     */
    /* JADX WARNING: Missing block: B:79:0x0193, code:
            r41.mService.startActivityInPackage(r40.mCallingUid, r40.mCallingPackage, r40.intent, null, null, null, 0, 0, android.app.ActivityOptions.makeBasic().toBundle(), r40.userId, null, null);
     */
    /* JADX WARNING: Missing block: B:81:0x01ce, code:
            com.android.server.am.ActivityManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Missing block: B:84:?, code:
            android.os.Binder.restoreCallingIdentity(r36);
     */
    /* JADX WARNING: Missing block: B:87:0x01da, code:
            if (r38 == 3) goto L_0x0159;
     */
    /* JADX WARNING: Missing block: B:88:0x01dc, code:
            r41.mProcessCrashTimes.put(r42.info.processName, r42.uid, java.lang.Long.valueOf(android.os.SystemClock.uptimeMillis()));
     */
    /* JADX WARNING: Missing block: B:89:0x01f7, code:
            r33 = move-exception;
     */
    /* JADX WARNING: Missing block: B:90:0x01f8, code:
            android.util.Slog.w(TAG, "bug report receiver dissappeared", r33);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void crashApplicationInner(ProcessRecord r, CrashInfo crashInfo) {
        long timeMillis = System.currentTimeMillis();
        String shortMsg = crashInfo.exceptionClassName;
        String longMsg = crashInfo.exceptionMessage;
        String stackTrace = crashInfo.stackTrace;
        if (shortMsg != null && longMsg != null) {
            longMsg = shortMsg + ": " + longMsg;
        } else if (shortMsg != null) {
            longMsg = shortMsg;
        }
        AppErrorResult result = new AppErrorResult();
        synchronized (this.mService) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                if (handleAppCrashInActivityController(r, crashInfo, shortMsg, longMsg, stackTrace, timeMillis)) {
                } else {
                    if (r != null) {
                        if (r.instrumentationClass != null) {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                            return;
                        }
                    }
                    if (r != null) {
                        this.mService.mBatteryStatsService.noteProcessCrash(r.processName, r.uid);
                    }
                    Data data = new Data();
                    data.result = result;
                    data.proc = r;
                    data.exceptionMsg = longMsg;
                    if (r == null || !makeAppCrashingLocked(r, shortMsg, longMsg, stackTrace, data)) {
                    } else {
                        Message msg = Message.obtain();
                        msg.what = 1;
                        TaskRecord task = data.task;
                        msg.obj = data;
                        this.mService.mUiHandler.sendMessage(msg);
                    }
                }
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    private boolean handleAppCrashInActivityController(ProcessRecord r, CrashInfo crashInfo, String shortMsg, String longMsg, String stackTrace, long timeMillis) {
        if (this.mService.mController == null) {
            return false;
        }
        String name;
        if (r != null) {
            try {
                name = r.processName;
            } catch (RemoteException e) {
                this.mService.mController = null;
                Watchdog.getInstance().setActivityController(null);
                if (this.mService.mOppoActivityControlerScheduler != null) {
                    this.mService.mOppoActivityControlerScheduler.exitRunningScheduler();
                    this.mService.mOppoActivityControlerScheduler = null;
                }
            }
        } else {
            name = null;
        }
        int pid = r != null ? r.pid : Binder.getCallingPid();
        int uid = r != null ? r.info.uid : Binder.getCallingUid();
        if (!this.mService.mController.appCrashed(name, pid, shortMsg, longMsg, timeMillis, crashInfo.stackTrace)) {
            if (LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(SystemProperties.get("ro.debuggable", "0")) && "Native crash".equals(crashInfo.exceptionClassName)) {
                Slog.w(TAG, "Skip killing native crashed app " + name + "(" + pid + ") during testing");
            } else {
                Slog.w(TAG, "Force-killing crashed app " + name + " at watcher's request");
                if (r == null) {
                    Process.killProcess(pid);
                    ActivityManagerService.killProcessGroup(uid, pid);
                } else if (!makeAppCrashingLocked(r, shortMsg, longMsg, stackTrace, null)) {
                    r.kill("crash", true);
                }
            }
            return true;
        }
        return false;
    }

    private boolean makeAppCrashingLocked(ProcessRecord app, String shortMsg, String longMsg, String stackTrace, Data data) {
        app.crashing = true;
        app.crashingReport = generateProcessError(app, 1, null, shortMsg, longMsg, stackTrace);
        startAppProblemLocked(app);
        app.stopFreezingAllLocked();
        return handleAppCrashLocked(app, "force-crash", shortMsg, longMsg, stackTrace, data);
    }

    void startAppProblemLocked(ProcessRecord app) {
        app.errorReportReceiver = null;
        for (int userId : this.mService.mUserController.getCurrentProfileIdsLocked()) {
            if (app.userId == userId) {
                app.errorReportReceiver = ApplicationErrorReport.getErrorReportReceiver(this.mContext, app.info.packageName, app.info.flags);
            }
        }
        this.mService.skipCurrentReceiverLocked(app);
    }

    private ProcessErrorStateInfo generateProcessError(ProcessRecord app, int condition, String activity, String shortMsg, String longMsg, String stackTrace) {
        ProcessErrorStateInfo report = new ProcessErrorStateInfo();
        report.condition = condition;
        report.processName = app.processName;
        report.pid = app.pid;
        report.uid = app.info.uid;
        report.tag = activity;
        report.shortMsg = shortMsg;
        report.longMsg = longMsg;
        report.stackTrace = stackTrace;
        return report;
    }

    Intent createAppErrorIntentLocked(ProcessRecord r, long timeMillis, CrashInfo crashInfo) {
        ApplicationErrorReport report = createAppErrorReportLocked(r, timeMillis, crashInfo);
        if (report == null) {
            return null;
        }
        Intent result = new Intent("android.intent.action.APP_ERROR");
        result.setComponent(r.errorReportReceiver);
        result.putExtra("android.intent.extra.BUG_REPORT", report);
        result.addFlags(268435456);
        return result;
    }

    private ApplicationErrorReport createAppErrorReportLocked(ProcessRecord r, long timeMillis, CrashInfo crashInfo) {
        boolean z = false;
        if (r.errorReportReceiver == null) {
            return null;
        }
        if (!r.crashing && !r.notResponding && !r.forceCrashReport) {
            return null;
        }
        ApplicationErrorReport report = new ApplicationErrorReport();
        report.packageName = r.info.packageName;
        report.installerPackageName = r.errorReportReceiver.getPackageName();
        report.processName = r.processName;
        report.time = timeMillis;
        if ((r.info.flags & 1) != 0) {
            z = true;
        }
        report.systemApp = z;
        if (r.crashing || r.forceCrashReport) {
            report.type = 1;
            report.crashInfo = crashInfo;
        } else if (r.notResponding) {
            report.type = 2;
            report.anrInfo = new AnrInfo();
            report.anrInfo.activity = r.notRespondingReport.tag;
            report.anrInfo.cause = r.notRespondingReport.shortMsg;
            report.anrInfo.info = r.notRespondingReport.longMsg;
        }
        return report;
    }

    boolean handleAppCrashLocked(ProcessRecord app, String reason, String shortMsg, String longMsg, String stackTrace, Data data) {
        Long crashTime;
        long now = SystemClock.uptimeMillis();
        boolean showBackground = Secure.getInt(this.mContext.getContentResolver(), "anr_show_background", 0) != 0;
        OppoCrashClearManager.getInstance().clearAppUserData(app);
        Long crashTimePersistent;
        if (app.isolated) {
            crashTimePersistent = null;
            crashTime = null;
        } else {
            crashTime = (Long) this.mProcessCrashTimes.get(app.info.processName, app.uid);
            crashTimePersistent = (Long) this.mProcessCrashTimesPersistent.get(app.info.processName, app.uid);
        }
        if (crashTime == null || now >= crashTime.longValue() + 60000) {
            TaskRecord affectedTask = this.mService.mStackSupervisor.finishTopRunningActivityLocked(app, reason);
            if (data != null) {
                data.task = affectedTask;
            }
            if (!(data == null || crashTimePersistent == null || now >= crashTimePersistent.longValue() + 60000)) {
                data.repeating = true;
            }
        } else {
            Slog.w(TAG, "Process " + app.info.processName + " has crashed too many times: killing!");
            Object[] objArr = new Object[3];
            objArr[0] = Integer.valueOf(app.userId);
            objArr[1] = app.info.processName;
            objArr[2] = Integer.valueOf(app.uid);
            EventLog.writeEvent(EventLogTags.AM_PROCESS_CRASHED_TOO_MUCH, objArr);
            this.mService.mStackSupervisor.handleAppCrashLocked(app);
            if (!app.persistent) {
                objArr = new Object[3];
                objArr[0] = Integer.valueOf(app.userId);
                objArr[1] = Integer.valueOf(app.uid);
                objArr[2] = app.info.processName;
                EventLog.writeEvent(EventLogTags.AM_PROC_BAD, objArr);
                if (!app.isolated) {
                    this.mBadProcesses.put(app.info.processName, app.uid, new BadProcessInfo(now, shortMsg, longMsg, stackTrace));
                    this.mProcessCrashTimes.remove(app.info.processName, app.uid);
                }
                app.bad = true;
                app.removed = true;
                this.mService.removeProcessLocked(app, false, false, "crash");
                this.mService.mStackSupervisor.resumeFocusedStackTopActivityLocked();
                if (!showBackground) {
                    return false;
                }
            }
            this.mService.mStackSupervisor.resumeFocusedStackTopActivityLocked();
        }
        boolean procIsBoundForeground = app.curProcState == 3;
        for (int i = app.services.size() - 1; i >= 0; i--) {
            ServiceRecord sr = (ServiceRecord) app.services.valueAt(i);
            sr.crashCount++;
            if (data != null && sr.crashCount <= 1 && (sr.isForeground || procIsBoundForeground)) {
                data.isRestartableForService = true;
            }
        }
        ArrayList<ActivityRecord> activities = app.activities;
        if (app == this.mService.mHomeProcess && activities.size() > 0 && (this.mService.mHomeProcess.info.flags & 1) == 0) {
            for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                ActivityRecord r = (ActivityRecord) activities.get(activityNdx);
                if (r.isHomeActivity()) {
                    Log.i(TAG, "Clearing package preferred activities from " + r.packageName);
                    try {
                        ActivityThread.getPackageManager().clearPackagePreferredActivities(r.packageName);
                    } catch (RemoteException e) {
                    }
                }
            }
        }
        if (!app.isolated) {
            this.mProcessCrashTimes.put(app.info.processName, app.uid, Long.valueOf(now));
            this.mProcessCrashTimesPersistent.put(app.info.processName, app.uid, Long.valueOf(now));
        }
        if (app.crashHandler != null) {
            this.mService.mHandler.post(app.crashHandler);
        }
        return true;
    }

    /* JADX WARNING: Missing block: B:44:0x00a7, code:
            com.android.server.am.ActivityManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Missing block: B:45:0x00ae, code:
            if (r1.proc.crashDialog == null) goto L_0x00b7;
     */
    /* JADX WARNING: Missing block: B:46:0x00b0, code:
            r1.proc.crashDialog.show();
     */
    /* JADX WARNING: Missing block: B:47:0x00b7, code:
            return;
     */
    /* JADX WARNING: Missing block: B:53:0x00e0, code:
            com.android.server.am.ActivityManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Missing block: B:54:0x00e3, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void handleShowAppErrorUi(Message msg) {
        Data data = msg.obj;
        boolean showBackground = Secure.getInt(this.mContext.getContentResolver(), "anr_show_background", 0) != 0;
        synchronized (this.mService) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                ProcessRecord proc = data.proc;
                AppErrorResult res = data.result;
                if (proc == null || proc.crashDialog == null) {
                    int isBackground = UserHandle.getAppId(proc.uid) >= 10000 ? proc.pid != ActivityManagerService.MY_PID ? 1 : 0 : 0;
                    for (int userId : this.mService.mUserController.getCurrentProfileIdsLocked()) {
                        int i;
                        if (proc.userId != userId) {
                            i = 1;
                        } else {
                            i = 0;
                        }
                        isBackground &= i;
                    }
                    if (isBackground == 0 || showBackground) {
                        boolean crashSilenced;
                        if (this.mAppsNotReportingCrashes != null) {
                            crashSilenced = this.mAppsNotReportingCrashes.contains(proc.info.packageName);
                        } else {
                            crashSilenced = false;
                        }
                        if ((this.mService.canShowErrorDialogs() || showBackground) && !crashSilenced) {
                            if (SystemProperties.getBoolean("persist.sys.assert.panic", false)) {
                                proc.crashDialog = new AppErrorDialog(this.mContext, this.mService, data);
                            } else {
                                if (proc.crashDialog != null) {
                                    Slog.w(TAG, " Dismiss app error dialog : " + proc.processName);
                                    proc.crashDialog = null;
                                }
                                if (res != null) {
                                    res.set(0);
                                }
                            }
                        } else if (res != null) {
                            res.set(AppErrorDialog.CANT_SHOW);
                        }
                        data.proc.crashDialog = proc.crashDialog;
                    } else {
                        Slog.w(TAG, "Skipping crash dialog of " + proc + ": background");
                        if (res != null) {
                            res.set(AppErrorDialog.BACKGROUND_USER);
                        }
                    }
                } else {
                    Slog.e(TAG, "App already has crash dialog: " + proc);
                    if (res != null) {
                        res.set(AppErrorDialog.ALREADY_SHOWING);
                    }
                }
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    void stopReportingCrashesLocked(ProcessRecord proc) {
        if (this.mAppsNotReportingCrashes == null) {
            this.mAppsNotReportingCrashes = new ArraySet();
        }
        this.mAppsNotReportingCrashes.add(proc.info.packageName);
    }

    final void appNotResponding(ProcessRecord app, ActivityRecord activity, ActivityRecord parent, boolean aboveSystem, String annotation) {
        ProcessRecord fApp = app;
        ActivityRecord fActivity = activity;
        ActivityRecord fParent = parent;
        boolean fAboveSystem = aboveSystem;
        final ProcessRecord processRecord = app;
        final ActivityRecord activityRecord = activity;
        final ActivityRecord activityRecord2 = parent;
        final boolean z = aboveSystem;
        final String str = annotation;
        new Thread() {
            public void run() {
                AppErrors.this.appNotRespondingInner(processRecord, activityRecord, activityRecord2, z, str);
            }
        }.start();
    }

    /* JADX WARNING: Missing block: B:82:0x033b, code:
            com.android.server.am.ActivityManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Missing block: B:83:0x033e, code:
            if (r4 == null) goto L_0x034c;
     */
    /* JADX WARNING: Missing block: B:84:0x0340, code:
            monitor-enter(r4);
     */
    /* JADX WARNING: Missing block: B:86:?, code:
            r53.mService.mAnrDumpMgr.dumpAnrDebugInfo(r4, false);
     */
    /* JADX WARNING: Missing block: B:87:0x034b, code:
            monitor-exit(r4);
     */
    /* JADX WARNING: Missing block: B:88:0x034c, code:
            r53.mService.mAnrDumpMgr.removeDumpRecord(r4);
            r5 = r53.mService;
            r36 = com.android.server.am.ActivityManagerService.mANRManager.isProcDoCoredump(r54.pid);
            r6 = new java.lang.StringBuilder().append(r4.mCpuInfo);
            r5 = r53.mService;
            r5 = com.android.server.am.ActivityManagerService.mANRManager;
            r4.mCpuInfo = r6.append((java.lang.String) com.mediatek.anrmanager.ANRManager.mMessageMap.get(java.lang.Integer.valueOf(r54.pid))).toString();
     */
    /* JADX WARNING: Missing block: B:89:0x0392, code:
            if (r36.booleanValue() != false) goto L_0x03b8;
     */
    /* JADX WARNING: Missing block: B:90:0x0394, code:
            r16 = r53.mService;
            r17 = "anr";
            r19 = r54.processName;
     */
    /* JADX WARNING: Missing block: B:91:0x03a3, code:
            if (r4 == null) goto L_0x0415;
     */
    /* JADX WARNING: Missing block: B:92:0x03a5, code:
            r23 = r4.mCpuInfo;
     */
    /* JADX WARNING: Missing block: B:93:0x03a9, code:
            r16.addErrorToDropBox(r17, r54, r19, r55, r56, r58, r23, null, null);
     */
    /* JADX WARNING: Missing block: B:119:0x0415, code:
            r23 = com.android.server.oppo.IElsaManager.EMPTY_PACKAGE;
     */
    /* JADX WARNING: Missing block: B:216:0x067d, code:
            com.android.server.am.ActivityManagerService.resetPriorityAfterLockedSection();
            r35.setLength(0);
            r35.append("ANR in ").append(r54.processName);
     */
    /* JADX WARNING: Missing block: B:217:0x0696, code:
            if (r55 == null) goto L_0x06b5;
     */
    /* JADX WARNING: Missing block: B:219:0x069c, code:
            if (r55.shortComponentName == null) goto L_0x06b5;
     */
    /* JADX WARNING: Missing block: B:220:0x069e, code:
            r35.append(" (").append(r55.shortComponentName).append(")");
     */
    /* JADX WARNING: Missing block: B:221:0x06b5, code:
            r35.append("\n");
            r35.append("PID: ").append(r54.pid).append("\n");
     */
    /* JADX WARNING: Missing block: B:222:0x06d4, code:
            if (r58 == null) goto L_0x06eb;
     */
    /* JADX WARNING: Missing block: B:223:0x06d6, code:
            r35.append("Reason: ").append(r58).append("\n");
     */
    /* JADX WARNING: Missing block: B:224:0x06eb, code:
            if (r56 == null) goto L_0x070a;
     */
    /* JADX WARNING: Missing block: B:226:0x06f1, code:
            if (r56 == r55) goto L_0x070a;
     */
    /* JADX WARNING: Missing block: B:227:0x06f3, code:
            r35.append("Parent: ").append(r56.shortComponentName).append("\n");
     */
    /* JADX WARNING: Missing block: B:228:0x070a, code:
            r0 = new com.android.internal.os.ProcessCpuTracker(true);
            r43 = com.android.server.Watchdog.NATIVE_STACKS_OF_INTEREST;
     */
    /* JADX WARNING: Missing block: B:229:0x0716, code:
            if (r38 == false) goto L_0x071a;
     */
    /* JADX WARNING: Missing block: B:230:0x0718, code:
            if (r28 == false) goto L_0x07c3;
     */
    /* JADX WARNING: Missing block: B:232:0x0722, code:
            if (android.os.SystemProperties.getBoolean("persist.sys.assert.nativestack", false) == false) goto L_0x07d4;
     */
    /* JADX WARNING: Missing block: B:233:0x0724, code:
            r5 = r53.mService;
            r24 = com.android.server.am.ActivityManagerService.dumpStackTraces(true, (java.util.ArrayList) r0, r0, (android.util.SparseArray) r0, r43);
     */
    /* JADX WARNING: Missing block: B:234:0x0735, code:
            r53.mService.updateCpuStatsNow();
            r6 = r53.mService.mProcessCpuTracker;
     */
    /* JADX WARNING: Missing block: B:235:0x0744, code:
            monitor-enter(r6);
     */
    /* JADX WARNING: Missing block: B:237:?, code:
            r23 = r53.mService.mProcessCpuTracker.printCurrentState(r14);
     */
    /* JADX WARNING: Missing block: B:238:0x074f, code:
            monitor-exit(r6);
     */
    /* JADX WARNING: Missing block: B:239:0x0750, code:
            r35.append(r0.printCurrentLoad());
            r35.append(r23);
            r35.append(r0.printCurrentState(r14));
            android.util.Slog.e(TAG, r35.toString());
     */
    /* JADX WARNING: Missing block: B:240:0x0774, code:
            if (r24 != null) goto L_0x077e;
     */
    /* JADX WARNING: Missing block: B:241:0x0776, code:
            android.os.Process.sendSignal(r54.pid, 3);
     */
    /* JADX WARNING: Missing block: B:242:0x077e, code:
            if (r24 == null) goto L_0x079f;
     */
    /* JADX WARNING: Missing block: B:243:0x0780, code:
            r52 = "traces_" + r54.pid + ".txt";
     */
    /* JADX WARNING: Missing block: B:244:0x079f, code:
            r53.mService.addErrorToDropBox("anr", r54, r54.processName, r55, r56, r58, r23, r24, null);
     */
    /* JADX WARNING: Missing block: B:247:0x07c3, code:
            r5 = r53.mService;
            r24 = com.android.server.am.ActivityManagerService.dumpStackTraces(true, (java.util.ArrayList) r0, null, (android.util.SparseArray) r0, null);
     */
    /* JADX WARNING: Missing block: B:248:0x07d4, code:
            r5 = r53.mService;
            r24 = com.android.server.am.ActivityManagerService.dumpStackTraces(true, (java.util.ArrayList) r0, r0, (android.util.SparseArray) r0, LIGHTWEIGHT_NATIVE_STACKS_OF_INTEREST);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    final void appNotRespondingInner(ProcessRecord app, ActivityRecord activity, ActivityRecord parent, boolean aboveSystem, String annotation) {
        ActivityManagerService activityManagerService = this.mService;
        if (!ActivityManagerService.mANRManager.isANRFlowSkipped(app.pid, app.processName, annotation, this.mService.mShuttingDown, app.notResponding, app.crashing)) {
            boolean isSilentANR;
            ArrayList<Integer> arrayList = new ArrayList(5);
            SparseArray<Boolean> sparseArray = new SparseArray(20);
            if (this.mService.mController != null) {
                try {
                    if (this.mService.mController.appEarlyNotResponding(app.processName, app.pid, annotation) < 0 && app.pid != ActivityManagerService.MY_PID) {
                        app.kill("anr", true);
                    }
                } catch (RemoteException e) {
                    this.mService.mController = null;
                    Watchdog.getInstance().setActivityController(null);
                    if (this.mService.mOppoActivityControlerScheduler != null) {
                        this.mService.mOppoActivityControlerScheduler.exitRunningScheduler();
                        this.mService.mOppoActivityControlerScheduler = null;
                    }
                }
            }
            long anrTime = SystemClock.uptimeMillis();
            this.mService.updateCpuStatsNow();
            activityManagerService = this.mService;
            ActivityManagerService.mANRManager.enableTraceLog(false);
            activityManagerService = this.mService;
            ActivityManagerService.mANRManager.enableBinderLog(false);
            AnrDumpRecord anrDumpRecord = null;
            StringBuilder info = new StringBuilder();
            activityManagerService = this.mService;
            ANRManager aNRManager = ActivityManagerService.mANRManager;
            if (2 == ANRManager.enableANRDebuggingMechanism()) {
                try {
                    if (app.pid == Process.myPid()) {
                        app.thread.dumpAllMessageHistory();
                    } else {
                        app.thread.dumpMessageHistory();
                    }
                } catch (Throwable e2) {
                    Slog.e(TAG, "Error happens when dumping message history", e2);
                }
            }
            boolean showBackground = Secure.getInt(this.mContext.getContentResolver(), "anr_show_background", 0) != 0;
            boolean dumpAllStacks = SystemProperties.getBoolean("persist.sys.assert.panic", false);
            activityManagerService = this.mService;
            aNRManager = ActivityManagerService.mANRManager;
            Object[] objArr;
            if (ANRManager.enableANRDebuggingMechanism() != 0) {
                activityManagerService = this.mService;
                aNRManager = ActivityManagerService.mANRManager;
                aNRManager.getClass();
                new BinderDumpThread(app.pid).start();
                if (!this.mService.mAnrDumpMgr.mDumpList.containsKey(app)) {
                    activityManagerService = this.mService;
                    aNRManager = ActivityManagerService.mANRManager;
                    aNRManager.getClass();
                    int i = app.pid;
                    boolean z = app.crashing;
                    String str = app.processName;
                    String processRecord = app.toString();
                    String str2 = activity != null ? activity.shortComponentName : null;
                    int i2 = (parent == null || parent.app == null) ? -1 : parent.app.pid;
                    anrDumpRecord = new AnrDumpRecord(i, z, str, processRecord, str2, i2, parent != null ? parent.shortComponentName : null, annotation, anrTime);
                    if (2 == ANRManager.enableANRDebuggingMechanism()) {
                        activityManagerService = this.mService;
                        ActivityManagerService.mANRManager.updateProcessStats();
                        StringBuilder stringBuilder = new StringBuilder();
                        ActivityManagerService activityManagerService2 = this.mService;
                        stringBuilder = stringBuilder.append(ActivityManagerService.mANRManager.getAndroidTime());
                        activityManagerService2 = this.mService;
                        String cpuInfo = stringBuilder.append(ActivityManagerService.mANRManager.getProcessState()).append("\n").toString();
                        if (anrDumpRecord != null) {
                            anrDumpRecord.mCpuInfo = cpuInfo;
                        }
                        Slog.i(TAG, cpuInfo.toString());
                    }
                    this.mService.mAnrDumpMgr.startAsyncDump(anrDumpRecord);
                }
                synchronized (this.mService) {
                    try {
                        ActivityManagerService.boostPriorityForLockedSection();
                        Slog.i(TAG, "appNotResponding-got this lock: " + app + " " + annotation);
                        if (this.mService.mShuttingDown) {
                            activityManagerService = this.mService;
                            ActivityManagerService.mANRManager.enableTraceLog(true);
                            activityManagerService = this.mService;
                            ActivityManagerService.mANRManager.enableBinderLog(true);
                            this.mService.mAnrDumpMgr.cancelDump(anrDumpRecord);
                            Slog.i(TAG, "During shutdown skipping ANR: " + app + " " + annotation);
                        } else if (app.notResponding) {
                            activityManagerService = this.mService;
                            ActivityManagerService.mANRManager.enableTraceLog(true);
                            activityManagerService = this.mService;
                            ActivityManagerService.mANRManager.enableBinderLog(true);
                            this.mService.mAnrDumpMgr.cancelDump(anrDumpRecord);
                            Slog.i(TAG, "Skipping duplicate ANR: " + app + " " + annotation);
                            ActivityManagerService.resetPriorityAfterLockedSection();
                            return;
                        } else if (app.crashing) {
                            activityManagerService = this.mService;
                            ActivityManagerService.mANRManager.enableTraceLog(true);
                            activityManagerService = this.mService;
                            ActivityManagerService.mANRManager.enableBinderLog(true);
                            this.mService.mAnrDumpMgr.cancelDump(anrDumpRecord);
                            Slog.i(TAG, "Crashing app skipping ANR: " + app + " " + annotation);
                            ActivityManagerService.resetPriorityAfterLockedSection();
                            return;
                        } else {
                            app.notResponding = true;
                            objArr = new Object[5];
                            objArr[0] = Integer.valueOf(app.userId);
                            objArr[1] = Integer.valueOf(app.pid);
                            objArr[2] = app.processName;
                            objArr[3] = Integer.valueOf(app.info.flags);
                            objArr[4] = annotation;
                            EventLog.writeEvent(EventLogTags.AM_ANR, objArr);
                            isSilentANR = (showBackground || app.isInterestingToUserLocked()) ? false : app.pid != ActivityManagerService.MY_PID;
                        }
                    } finally {
                        ActivityManagerService.resetPriorityAfterLockedSection();
                    }
                }
            } else {
                Slog.i(TAG, "ANR_DEBUGGING_MECHANISM is disabled");
                synchronized (this.mService) {
                    try {
                        ActivityManagerService.boostPriorityForLockedSection();
                        if (this.mService.mShuttingDown) {
                            Slog.i(TAG, "During shutdown skipping ANR: " + app + " " + annotation);
                        } else if (app.notResponding) {
                            Slog.i(TAG, "Skipping duplicate ANR: " + app + " " + annotation);
                            ActivityManagerService.resetPriorityAfterLockedSection();
                            return;
                        } else if (app.crashing) {
                            Slog.i(TAG, "Crashing app skipping ANR: " + app + " " + annotation);
                            ActivityManagerService.resetPriorityAfterLockedSection();
                            return;
                        } else {
                            app.notResponding = true;
                            objArr = new Object[5];
                            objArr[0] = Integer.valueOf(app.userId);
                            objArr[1] = Integer.valueOf(app.pid);
                            objArr[2] = app.processName;
                            objArr[3] = Integer.valueOf(app.info.flags);
                            objArr[4] = annotation;
                            EventLog.writeEvent(EventLogTags.AM_ANR, objArr);
                            arrayList.add(Integer.valueOf(app.pid));
                            isSilentANR = (showBackground || app.isInterestingToUserLocked()) ? false : app.pid != ActivityManagerService.MY_PID;
                            if (!isSilentANR || dumpAllStacks) {
                                int parentPid = app.pid;
                                if (!(parent == null || parent.app == null || parent.app.pid <= 0)) {
                                    parentPid = parent.app.pid;
                                }
                                if (parentPid != app.pid) {
                                    arrayList.add(Integer.valueOf(parentPid));
                                }
                                int maxProcForStackDump = SystemProperties.getInt("persist.sys.assert.stackdump", dumpAllStacks ? 4 : 2);
                                int procNumToDumpStackFisrtPids = 0;
                                int procNumToDumpStackLastPids = 0;
                                if (!(!SystemProperties.getBoolean("persist.sys.assert.dumpsys", dumpAllStacks) || ActivityManagerService.MY_PID == app.pid || ActivityManagerService.MY_PID == parentPid)) {
                                    arrayList.add(Integer.valueOf(ActivityManagerService.MY_PID));
                                }
                                if (this.mInterestAnrAppProcNames == null) {
                                    this.mInterestAnrAppProcNames = new ArrayList();
                                    this.mInterestAnrAppProcNames.add("android.process.media");
                                    this.mInterestAnrAppProcNames.add("com.android.phone");
                                }
                                for (int i3 = this.mService.mLruProcesses.size() - 1; i3 >= 0; i3--) {
                                    ProcessRecord r = (ProcessRecord) this.mService.mLruProcesses.get(i3);
                                    if (!(r == null || r.thread == null)) {
                                        int pid = r.pid;
                                        if (!(pid <= 0 || pid == app.pid || pid == parentPid || pid == ActivityManagerService.MY_PID)) {
                                            boolean isInterestProc = r.processName != null ? this.mInterestAnrAppProcNames.contains(r.processName) : false;
                                            if (r.persistent) {
                                                if (procNumToDumpStackFisrtPids < maxProcForStackDump || isInterestProc) {
                                                    arrayList.add(Integer.valueOf(pid));
                                                    if (ActivityManagerDebugConfig.DEBUG_ANR) {
                                                        Slog.i(TAG, "Adding persistent proc: " + r);
                                                    }
                                                    if (!isInterestProc) {
                                                        procNumToDumpStackFisrtPids++;
                                                    }
                                                }
                                            } else if (procNumToDumpStackLastPids < maxProcForStackDump || isInterestProc) {
                                                sparseArray.put(pid, Boolean.TRUE);
                                                if (ActivityManagerDebugConfig.DEBUG_ANR) {
                                                    Slog.i(TAG, "Adding ANR proc: " + r);
                                                }
                                                if (!isInterestProc) {
                                                    procNumToDumpStackLastPids++;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } finally {
                        ActivityManagerService.resetPriorityAfterLockedSection();
                    }
                }
            }
            if (this.mService.mController != null) {
                try {
                    int res;
                    activityManagerService = this.mService;
                    aNRManager = ActivityManagerService.mANRManager;
                    if (ANRManager.enableANRDebuggingMechanism() != 0) {
                        res = this.mService.mController.appNotResponding(app.processName, app.pid, anrDumpRecord != null ? anrDumpRecord.mInfo.toString() : IElsaManager.EMPTY_PACKAGE);
                    } else {
                        res = this.mService.mController.appNotResponding(app.processName, app.pid, info.toString());
                    }
                    if (res != 0) {
                        if (res >= 0 || app.pid == ActivityManagerService.MY_PID) {
                            synchronized (this.mService) {
                                ActivityManagerService.boostPriorityForLockedSection();
                                this.mService.mServices.scheduleServiceTimeoutLocked(app);
                            }
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        } else {
                            app.kill("anr", true);
                        }
                        return;
                    }
                } catch (RemoteException e3) {
                    this.mService.mController = null;
                    Watchdog.getInstance().setActivityController(null);
                    if (this.mService.mOppoActivityControlerScheduler != null) {
                        this.mService.mOppoActivityControlerScheduler.exitRunningScheduler();
                        this.mService.mOppoActivityControlerScheduler = null;
                    }
                } catch (Throwable th) {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
            synchronized (this.mService) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    this.mService.mBatteryStatsService.noteProcessAnr(app.processName, app.uid);
                    if (isSilentANR) {
                        app.kill("bg anr", true);
                    } else {
                        activityManagerService = this.mService;
                        aNRManager = ActivityManagerService.mANRManager;
                        if (ANRManager.enableANRDebuggingMechanism() != 0) {
                            makeAppNotRespondingLocked(app, activity != null ? activity.shortComponentName : null, annotation != null ? "ANR " + annotation : "ANR", anrDumpRecord != null ? anrDumpRecord.mInfo.toString() : IElsaManager.EMPTY_PACKAGE);
                        } else {
                            makeAppNotRespondingLocked(app, activity != null ? activity.shortComponentName : null, annotation != null ? "ANR " + annotation : "ANR", info.toString());
                        }
                        Message msg = Message.obtain();
                        HashMap<String, Object> map = new HashMap();
                        msg.what = 2;
                        msg.obj = map;
                        msg.arg1 = aboveSystem ? 1 : 0;
                        map.put("app", app);
                        if (activity != null) {
                            map.put("activity", activity);
                        }
                        this.mService.mUiHandler.sendMessage(msg);
                        ActivityManagerService.resetPriorityAfterLockedSection();
                    }
                } finally {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
        }
    }

    private void makeAppNotRespondingLocked(ProcessRecord app, String activity, String shortMsg, String longMsg) {
        app.notResponding = true;
        app.notRespondingReport = generateProcessError(app, 2, activity, shortMsg, longMsg, null);
        startAppProblemLocked(app);
        app.stopFreezingAllLocked();
    }

    /* JADX WARNING: Missing block: B:30:0x00e1, code:
            com.android.server.am.ActivityManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Missing block: B:31:0x00e4, code:
            if (r18 == null) goto L_0x00e9;
     */
    /* JADX WARNING: Missing block: B:32:0x00e6, code:
            r18.show();
     */
    /* JADX WARNING: Missing block: B:33:0x00e9, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void handleShowAnrUi(Message msg) {
        synchronized (this.mService) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                HashMap<String, Object> data = msg.obj;
                ProcessRecord proc = (ProcessRecord) data.get("app");
                if (proc == null || proc.anrDialog == null) {
                    Dialog d;
                    Intent intent = new Intent("android.intent.action.ANR");
                    if (!this.mService.mProcessesReady) {
                        intent.addFlags(1342177280);
                    }
                    this.mService.broadcastIntentLocked(null, null, intent, null, null, 0, null, null, null, -1, null, false, false, ActivityManagerService.MY_PID, 1000, 0);
                    boolean showBackground = Secure.getInt(this.mContext.getContentResolver(), "anr_show_background", 0) != 0;
                    if (!(this.mService.canShowErrorDialogs() || showBackground)) {
                        ActivityManagerService activityManagerService = this.mService;
                        ANRManager aNRManager = ActivityManagerService.mANRManager;
                        if (ANRManager.enableANRDebuggingMechanism() == 0) {
                            MetricsLogger.action(this.mContext, 317, -1);
                            this.mService.killAppAtUsersRequest(proc, null);
                            d = proc.anrDialog;
                        }
                    }
                    if (SystemProperties.getBoolean("persist.sys.assert.panic", false)) {
                        proc.anrDialog = new AppNotRespondingDialog(this.mService, this.mContext, proc, (ActivityRecord) data.get("activity"), msg.arg1 != 0);
                    } else {
                        if (proc.anrDialog != null) {
                            Slog.w(TAG, " Dismiss app ANR dialog : " + proc.processName);
                            proc.anrDialog = null;
                        }
                        this.mService.killAppAtUsersRequest(proc, null);
                    }
                    d = proc.anrDialog;
                } else {
                    Slog.e(TAG, "App already has anr dialog: " + proc);
                    MetricsLogger.action(this.mContext, 317, -2);
                }
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
    }
}
