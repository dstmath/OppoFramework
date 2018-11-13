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
import com.android.server.RescueParty;
import com.android.server.Watchdog;
import com.android.server.oppo.ElsaManager;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

class AppErrors {
    public static final String[] LIGHTWEIGHT_NATIVE_STACKS_OF_INTEREST = new String[]{"/system/bin/mediaserver", "/system/bin/surfaceflinger", "/system/bin/audioserver", "media.codec"};
    private static final String TAG = "ActivityManager";
    private ArraySet<String> mAppsNotReportingCrashes;
    private final ProcessMap<BadProcessInfo> mBadProcesses = new ProcessMap();
    private final Context mContext;
    private ArrayList<String> mInterestAnrAppProcNames = null;
    private final ProcessMap<Long> mProcessCrashTimes = new ProcessMap();
    private final ProcessMap<Long> mProcessCrashTimesPersistent = new ProcessMap();
    private final ActivityManagerService mService;
    SimpleDateFormat mTraceDateFormat = new SimpleDateFormat("dd_MMM_HH_mm_ss.SSS");

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

    AppErrors(Context context, ActivityManagerService service) {
        context.assertRuntimeOverlayThemable();
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
                    if (dumpPackage == null || (r != null && (r.pkgList.containsKey(dumpPackage) ^ 1) == 0)) {
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
                    if (dumpPackage == null || (r != null && (r.pkgList.containsKey(dumpPackage) ^ 1) == 0)) {
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

    void scheduleAppCrashLocked(int uid, int initialPid, String packageName, int userId, String message) {
        ProcessRecord proc = null;
        synchronized (this.mService.mPidsSelfLocked) {
            for (int i = 0; i < this.mService.mPidsSelfLocked.size(); i++) {
                ProcessRecord p = (ProcessRecord) this.mService.mPidsSelfLocked.valueAt(i);
                if (uid < 0 || p.uid == uid) {
                    if (p.pid == initialPid) {
                        proc = p;
                        break;
                    } else if (p.pkgList.containsKey(packageName) && (userId < 0 || p.userId == userId)) {
                        proc = p;
                    }
                }
            }
        }
        if (proc == null) {
            Slog.w(TAG, "crashApplication: nothing for uid=" + uid + " initialPid=" + initialPid + " packageName=" + packageName + " userId=" + userId);
        } else {
            proc.scheduleCrash(message);
        }
    }

    void crashApplication(ProcessRecord r, CrashInfo crashInfo) {
        int callingPid = Binder.getCallingPid();
        int callingUid = Binder.getCallingUid();
        long origId = Binder.clearCallingIdentity();
        try {
            crashApplicationInner(r, crashInfo, callingPid, callingUid);
        } finally {
            Binder.restoreCallingIdentity(origId);
        }
    }

    /* JADX WARNING: Missing block: B:33:0x00ab, code:
            com.android.server.am.ActivityManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Missing block: B:34:0x00ae, code:
            return;
     */
    /* JADX WARNING: Missing block: B:38:0x00d0, code:
            com.android.server.am.ActivityManagerService.resetPriorityAfterLockedSection();
            r38 = r39.get();
            r31 = null;
            com.android.internal.logging.MetricsLogger.action(r41.mContext, 316, r38);
     */
    /* JADX WARNING: Missing block: B:39:0x00e7, code:
            if (r38 == 6) goto L_0x00ee;
     */
    /* JADX WARNING: Missing block: B:41:0x00ec, code:
            if (r38 != 7) goto L_0x00f0;
     */
    /* JADX WARNING: Missing block: B:42:0x00ee, code:
            r38 = 1;
     */
    /* JADX WARNING: Missing block: B:43:0x00f0, code:
            r5 = r41.mService;
     */
    /* JADX WARNING: Missing block: B:44:0x00f4, code:
            monitor-enter(r5);
     */
    /* JADX WARNING: Missing block: B:46:?, code:
            com.android.server.am.ActivityManagerService.boostPriorityForLockedSection();
     */
    /* JADX WARNING: Missing block: B:47:0x00fb, code:
            if (r38 != 5) goto L_0x0100;
     */
    /* JADX WARNING: Missing block: B:48:0x00fd, code:
            stopReportingCrashesLocked(r42);
     */
    /* JADX WARNING: Missing block: B:50:0x0103, code:
            if (r38 != 3) goto L_0x0128;
     */
    /* JADX WARNING: Missing block: B:51:0x0105, code:
            r41.mService.removeProcessLocked(r42, false, true, "crash");
     */
    /* JADX WARNING: Missing block: B:52:0x0113, code:
            if (r40 == null) goto L_0x0128;
     */
    /* JADX WARNING: Missing block: B:54:?, code:
            r41.mService.startActivityFromRecents(r40.taskId, android.app.ActivityOptions.makeBasic().toBundle());
     */
    /* JADX WARNING: Missing block: B:56:0x012b, code:
            if (r38 != 1) goto L_0x015c;
     */
    /* JADX WARNING: Missing block: B:58:?, code:
            r36 = android.os.Binder.clearCallingIdentity();
     */
    /* JADX WARNING: Missing block: B:60:?, code:
            r41.mService.mStackSupervisor.handleAppCrashLocked(r42);
     */
    /* JADX WARNING: Missing block: B:61:0x0140, code:
            if (r42.persistent != false) goto L_0x0159;
     */
    /* JADX WARNING: Missing block: B:62:0x0142, code:
            r41.mService.removeProcessLocked(r42, false, false, "crash");
            r41.mService.mStackSupervisor.resumeFocusedStackTopActivityLocked();
     */
    /* JADX WARNING: Missing block: B:64:?, code:
            android.os.Binder.restoreCallingIdentity(r36);
     */
    /* JADX WARNING: Missing block: B:66:0x015f, code:
            if (r38 != 2) goto L_0x016b;
     */
    /* JADX WARNING: Missing block: B:67:0x0161, code:
            r31 = createAppErrorIntentLocked(r42, r10, r43);
     */
    /* JADX WARNING: Missing block: B:68:0x016b, code:
            if (r42 == null) goto L_0x0193;
     */
    /* JADX WARNING: Missing block: B:70:0x0173, code:
            if ((r42.isolated ^ 1) == 0) goto L_0x0193;
     */
    /* JADX WARNING: Missing block: B:72:0x0178, code:
            if (r38 == 3) goto L_0x0193;
     */
    /* JADX WARNING: Missing block: B:73:0x017a, code:
            r41.mProcessCrashTimes.put(r42.info.processName, r42.uid, java.lang.Long.valueOf(android.os.SystemClock.uptimeMillis()));
     */
    /* JADX WARNING: Missing block: B:74:0x0193, code:
            monitor-exit(r5);
     */
    /* JADX WARNING: Missing block: B:75:0x0194, code:
            com.android.server.am.ActivityManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Missing block: B:76:0x0197, code:
            if (r31 == null) goto L_0x01ab;
     */
    /* JADX WARNING: Missing block: B:78:?, code:
            r41.mContext.startActivityAsUser(r31, new android.os.UserHandle(r42.userId));
     */
    /* JADX WARNING: Missing block: B:85:?, code:
            r32 = r40.intent.getCategories();
     */
    /* JADX WARNING: Missing block: B:86:0x01c6, code:
            if (r32 != null) goto L_0x01c8;
     */
    /* JADX WARNING: Missing block: B:88:0x01d1, code:
            if (r32.contains("android.intent.category.LAUNCHER") != false) goto L_0x01d3;
     */
    /* JADX WARNING: Missing block: B:89:0x01d3, code:
            r41.mService.startActivityInPackage(r40.mCallingUid, r40.mCallingPackage, r40.intent, null, null, null, 0, 0, android.app.ActivityOptions.makeBasic().toBundle(), r40.userId, null, "AppErrors");
     */
    /* JADX WARNING: Missing block: B:91:0x020f, code:
            com.android.server.am.ActivityManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Missing block: B:94:?, code:
            android.os.Binder.restoreCallingIdentity(r36);
     */
    /* JADX WARNING: Missing block: B:96:0x0218, code:
            r33 = move-exception;
     */
    /* JADX WARNING: Missing block: B:97:0x0219, code:
            android.util.Slog.w(TAG, "bug report receiver dissappeared", r33);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void crashApplicationInner(ProcessRecord r, CrashInfo crashInfo, int callingPid, int callingUid) {
        long timeMillis = System.currentTimeMillis();
        String shortMsg = crashInfo.exceptionClassName;
        String longMsg = crashInfo.exceptionMessage;
        String stackTrace = crashInfo.stackTrace;
        if (shortMsg != null && longMsg != null) {
            longMsg = shortMsg + ": " + longMsg;
        } else if (shortMsg != null) {
            longMsg = shortMsg;
        }
        if (r != null && r.persistent) {
            RescueParty.notePersistentAppCrash(this.mContext, r.uid);
        }
        AppErrorResult result = new AppErrorResult();
        synchronized (this.mService) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                if (handleAppCrashInActivityController(r, crashInfo, shortMsg, longMsg, stackTrace, timeMillis, callingPid, callingUid)) {
                } else {
                    if (r != null) {
                        if (r.instr != null) {
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
                    if (r == null || (makeAppCrashingLocked(r, shortMsg, longMsg, stackTrace, data) ^ 1) != 0) {
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
        return;
        OppoAppErrorsStatistics.doErrorsStatistics(this.mContext, r, crashInfo);
    }

    private boolean handleAppCrashInActivityController(ProcessRecord r, CrashInfo crashInfo, String shortMsg, String longMsg, String stackTrace, long timeMillis, int callingPid, int callingUid) {
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
        int pid = r != null ? r.pid : callingPid;
        int uid = r != null ? r.info.uid : callingUid;
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
        if (!r.crashing && (r.notResponding ^ 1) != 0 && (r.forceCrashReport ^ 1) != 0) {
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
        boolean procIsBoundForeground = app.curProcState == 3;
        OppoCrashClearManager.getInstance().clearAppUserData(app);
        boolean tryAgain = false;
        Long crashTimePersistent;
        if (app.isolated) {
            crashTimePersistent = null;
            crashTime = null;
        } else {
            crashTime = (Long) this.mProcessCrashTimes.get(app.info.processName, app.uid);
            crashTimePersistent = (Long) this.mProcessCrashTimesPersistent.get(app.info.processName, app.uid);
        }
        for (int i = app.services.size() - 1; i >= 0; i--) {
            ServiceRecord sr = (ServiceRecord) app.services.valueAt(i);
            if (now > sr.restartTime + 60000) {
                sr.crashCount = 1;
            } else {
                sr.crashCount++;
            }
            if (((long) sr.crashCount) < this.mService.mConstants.BOUND_SERVICE_MAX_CRASH_RETRY && (sr.isForeground || procIsBoundForeground)) {
                tryAgain = true;
            }
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
            EventLog.writeEvent(EventLogTags.AM_PROCESS_CRASHED_TOO_MUCH, new Object[]{Integer.valueOf(app.userId), app.info.processName, Integer.valueOf(app.uid)});
            this.mService.mStackSupervisor.handleAppCrashLocked(app);
            if (!app.persistent) {
                EventLog.writeEvent(EventLogTags.AM_PROC_BAD, new Object[]{Integer.valueOf(app.userId), Integer.valueOf(app.uid), app.info.processName});
                if (!app.isolated) {
                    this.mBadProcesses.put(app.info.processName, app.uid, new BadProcessInfo(now, shortMsg, longMsg, stackTrace));
                    this.mProcessCrashTimes.remove(app.info.processName, app.uid);
                }
                app.bad = true;
                app.removed = true;
                this.mService.removeProcessLocked(app, false, tryAgain, "crash");
                this.mService.mStackSupervisor.resumeFocusedStackTopActivityLocked();
                if (!showBackground) {
                    return false;
                }
            }
            this.mService.mStackSupervisor.resumeFocusedStackTopActivityLocked();
        }
        if (data != null && tryAgain) {
            data.isRestartableForService = true;
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

    /* JADX WARNING: Missing block: B:38:0x00aa, code:
            com.android.server.am.ActivityManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Missing block: B:39:0x00ad, code:
            return;
     */
    /* JADX WARNING: Missing block: B:54:0x00e6, code:
            com.android.server.am.ActivityManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Missing block: B:55:0x00ed, code:
            if (r1.proc.crashDialog == null) goto L_0x0124;
     */
    /* JADX WARNING: Missing block: B:56:0x00ef, code:
            android.util.Slog.i(TAG, "Showing crash dialog for package " + r1.proc.info.packageName + " u" + r1.proc.userId);
     */
    /* JADX WARNING: Missing block: B:58:?, code:
            r1.proc.crashDialog.show();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void handleShowAppErrorUi(Message msg) {
        Data data = msg.obj;
        boolean showBackground = Secure.getInt(this.mContext.getContentResolver(), "anr_show_background", 0) != 0;
        synchronized (this.mService) {
            ActivityManagerService.boostPriorityForLockedSection();
            ProcessRecord proc = data.proc;
            AppErrorResult res = data.result;
            if (proc == null || proc.crashDialog == null) {
                try {
                    int isBackground = UserHandle.getAppId(proc.uid) >= 10000 ? proc.pid != ActivityManagerService.MY_PID ? 1 : 0 : 0;
                    for (int userId : this.mService.mUserController.getCurrentProfileIdsLocked()) {
                        isBackground &= proc.userId != userId ? 1 : 0;
                    }
                    if (isBackground == 0 || (showBackground ^ 1) == 0) {
                        int crashSilenced;
                        if (this.mAppsNotReportingCrashes != null) {
                            crashSilenced = this.mAppsNotReportingCrashes.contains(proc.info.packageName);
                        } else {
                            crashSilenced = 0;
                        }
                        if ((this.mService.canShowErrorDialogs() || showBackground) && (crashSilenced ^ 1) != 0) {
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
                } finally {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            } else {
                Slog.e(TAG, "App already has crash dialog: " + proc);
                if (res != null) {
                    res.set(AppErrorDialog.ALREADY_SHOWING);
                }
            }
        }
    }

    void stopReportingCrashesLocked(ProcessRecord proc) {
        if (this.mAppsNotReportingCrashes == null) {
            this.mAppsNotReportingCrashes = new ArraySet();
        }
        this.mAppsNotReportingCrashes.add(proc.info.packageName);
    }

    static boolean isInterestingForBackgroundTraces(ProcessRecord app) {
        boolean z = true;
        if (app.pid == ActivityManagerService.MY_PID) {
            return true;
        }
        if (!app.isInterestingToUserLocked() && ((app.info == null || !OppoFreeFormManagerService.FREEFORM_CALLER_PKG.equals(app.info.packageName)) && !app.hasTopUi)) {
            z = app.hasOverlayUi;
        }
        return z;
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

    /* JADX WARNING: Missing block: B:136:0x03b6, code:
            com.android.server.am.ActivityManagerService.resetPriorityAfterLockedSection();
            r25 = new java.lang.StringBuilder();
            r25.setLength(0);
            r25.append("ANR in ").append(r50.processName);
     */
    /* JADX WARNING: Missing block: B:137:0x03d4, code:
            if (r51 == null) goto L_0x03f3;
     */
    /* JADX WARNING: Missing block: B:139:0x03da, code:
            if (r51.shortComponentName == null) goto L_0x03f3;
     */
    /* JADX WARNING: Missing block: B:140:0x03dc, code:
            r25.append(" (").append(r51.shortComponentName).append(")");
     */
    /* JADX WARNING: Missing block: B:141:0x03f3, code:
            r25.append("\n");
            r25.append("PID: ").append(r50.pid).append("\n");
     */
    /* JADX WARNING: Missing block: B:142:0x0412, code:
            if (r54 == null) goto L_0x0429;
     */
    /* JADX WARNING: Missing block: B:143:0x0414, code:
            r25.append("Reason: ").append(r54).append("\n");
     */
    /* JADX WARNING: Missing block: B:144:0x0429, code:
            if (r52 == null) goto L_0x0448;
     */
    /* JADX WARNING: Missing block: B:146:0x042f, code:
            if (r52 == r51) goto L_0x0448;
     */
    /* JADX WARNING: Missing block: B:147:0x0431, code:
            r25.append("Parent: ").append(r52.shortComponentName).append("\n");
     */
    /* JADX WARNING: Missing block: B:148:0x0448, code:
            r0 = new com.android.internal.os.ProcessCpuTracker(true);
            r34 = null;
     */
    /* JADX WARNING: Missing block: B:149:0x0452, code:
            if (r27 == 0) goto L_0x04a6;
     */
    /* JADX WARNING: Missing block: B:150:0x0454, code:
            r23 = 0;
     */
    /* JADX WARNING: Missing block: B:152:0x045b, code:
            if (r23 >= com.android.server.Watchdog.NATIVE_STACKS_OF_INTEREST.length) goto L_0x0477;
     */
    /* JADX WARNING: Missing block: B:154:0x0469, code:
            if (com.android.server.Watchdog.NATIVE_STACKS_OF_INTEREST[r23].equals(r50.processName) == false) goto L_0x04a3;
     */
    /* JADX WARNING: Missing block: B:155:0x046b, code:
            r34 = new java.lang.String[]{r50.processName};
     */
    /* JADX WARNING: Missing block: B:156:0x0477, code:
            if (r34 != null) goto L_0x04b6;
     */
    /* JADX WARNING: Missing block: B:157:0x0479, code:
            r38 = null;
     */
    /* JADX WARNING: Missing block: B:158:0x047b, code:
            r33 = null;
     */
    /* JADX WARNING: Missing block: B:159:0x047d, code:
            if (r38 == null) goto L_0x04bb;
     */
    /* JADX WARNING: Missing block: B:160:0x047f, code:
            r0 = new java.util.ArrayList(r38.length);
            r4 = 0;
            r5 = r38.length;
     */
    /* JADX WARNING: Missing block: B:161:0x048d, code:
            if (r4 >= r5) goto L_0x04bb;
     */
    /* JADX WARNING: Missing block: B:162:0x048f, code:
            r0.add(java.lang.Integer.valueOf(r38[r4]));
            r4 = r4 + 1;
     */
    /* JADX WARNING: Missing block: B:165:0x04a3, code:
            r23 = r23 + 1;
     */
    /* JADX WARNING: Missing block: B:167:0x04ae, code:
            if (android.os.SystemProperties.getBoolean("persist.sys.assert.nativestack", false) == false) goto L_0x04b3;
     */
    /* JADX WARNING: Missing block: B:168:0x04b0, code:
            r34 = com.android.server.Watchdog.NATIVE_STACKS_OF_INTEREST;
     */
    /* JADX WARNING: Missing block: B:169:0x04b3, code:
            r34 = LIGHTWEIGHT_NATIVE_STACKS_OF_INTEREST;
     */
    /* JADX WARNING: Missing block: B:170:0x04b6, code:
            r38 = android.os.Process.getPidsForCommands(r34);
     */
    /* JADX WARNING: Missing block: B:171:0x04bb, code:
            if (r27 == 0) goto L_0x05aa;
     */
    /* JADX WARNING: Missing block: B:172:0x04bd, code:
            r4 = null;
     */
    /* JADX WARNING: Missing block: B:173:0x04be, code:
            if (r27 == 0) goto L_0x04c2;
     */
    /* JADX WARNING: Missing block: B:174:0x04c0, code:
            r28 = null;
     */
    /* JADX WARNING: Missing block: B:175:0x04c2, code:
            r12 = com.android.server.am.ActivityManagerService.dumpStackTraces(true, (java.util.ArrayList) r0, r4, (android.util.SparseArray) r28, r33);
            r49.mService.updateCpuStatsNow();
            r5 = r49.mService.mProcessCpuTracker;
     */
    /* JADX WARNING: Missing block: B:176:0x04db, code:
            monitor-enter(r5);
     */
    /* JADX WARNING: Missing block: B:178:?, code:
            r11 = r49.mService.mProcessCpuTracker.printCurrentState(r14);
     */
    /* JADX WARNING: Missing block: B:179:0x04e6, code:
            monitor-exit(r5);
     */
    /* JADX WARNING: Missing block: B:180:0x04e7, code:
            r25.append(r0.printCurrentLoad());
            r25.append(r11);
            r25.append(r0.printCurrentState(r14));
            android.util.Slog.e(TAG, r25.toString());
     */
    /* JADX WARNING: Missing block: B:181:0x0509, code:
            if (r12 != null) goto L_0x0513;
     */
    /* JADX WARNING: Missing block: B:182:0x050b, code:
            android.os.Process.sendSignal(r50.pid, 3);
     */
    /* JADX WARNING: Missing block: B:183:0x0513, code:
            if (r12 == null) goto L_0x055f;
     */
    /* JADX WARNING: Missing block: B:184:0x0515, code:
            com.oppo.debug.ASSERT.copyAnr(r12.getPath(), "traces_" + r50.pid + com.android.server.LocationManagerService.OPPO_FAKE_LOCATION_SPLIT + new java.text.SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS").format(new java.util.Date()) + ".txt");
     */
    /* JADX WARNING: Missing block: B:185:0x055f, code:
            r49.mService.addErrorToDropBox("anr", r50, r50.processName, r51, r52, r54, r11, r12, null);
     */
    /* JADX WARNING: Missing block: B:186:0x057c, code:
            if (r49.mService.mController == null) goto L_0x05f1;
     */
    /* JADX WARNING: Missing block: B:188:?, code:
            r43 = r49.mService.mController.appNotResponding(r50.processName, r50.pid, r25.toString());
     */
    /* JADX WARNING: Missing block: B:189:0x0594, code:
            if (r43 == 0) goto L_0x05f1;
     */
    /* JADX WARNING: Missing block: B:190:0x0596, code:
            if (r43 >= 0) goto L_0x05b1;
     */
    /* JADX WARNING: Missing block: B:192:0x059e, code:
            if (r50.pid == com.android.server.am.ActivityManagerService.MY_PID) goto L_0x05b1;
     */
    /* JADX WARNING: Missing block: B:193:0x05a0, code:
            r50.kill("anr", true);
     */
    /* JADX WARNING: Missing block: B:194:0x05a9, code:
            return;
     */
    /* JADX WARNING: Missing block: B:195:0x05aa, code:
            r4 = r0;
     */
    /* JADX WARNING: Missing block: B:200:?, code:
            r5 = r49.mService;
     */
    /* JADX WARNING: Missing block: B:201:0x05b5, code:
            monitor-enter(r5);
     */
    /* JADX WARNING: Missing block: B:203:?, code:
            com.android.server.am.ActivityManagerService.boostPriorityForLockedSection();
            r49.mService.mServices.scheduleServiceTimeoutLocked(r50);
     */
    /* JADX WARNING: Missing block: B:205:?, code:
            monitor-exit(r5);
     */
    /* JADX WARNING: Missing block: B:206:0x05c5, code:
            com.android.server.am.ActivityManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Missing block: B:208:0x05ca, code:
            r49.mService.mController = null;
            com.android.server.Watchdog.getInstance().setActivityController(null);
     */
    /* JADX WARNING: Missing block: B:209:0x05df, code:
            if (r49.mService.mOppoActivityControlerScheduler != null) goto L_0x05e1;
     */
    /* JADX WARNING: Missing block: B:210:0x05e1, code:
            r49.mService.mOppoActivityControlerScheduler.exitRunningScheduler();
            r49.mService.mOppoActivityControlerScheduler = null;
     */
    /* JADX WARNING: Missing block: B:211:0x05f1, code:
            r6 = r49.mService;
     */
    /* JADX WARNING: Missing block: B:212:0x05f5, code:
            monitor-enter(r6);
     */
    /* JADX WARNING: Missing block: B:214:?, code:
            com.android.server.am.ActivityManagerService.boostPriorityForLockedSection();
            r49.mService.mBatteryStatsService.noteProcessAnr(r50.processName, r50.uid);
     */
    /* JADX WARNING: Missing block: B:215:0x060a, code:
            if (r27 == 0) goto L_0x0620;
     */
    /* JADX WARNING: Missing block: B:216:0x060c, code:
            r50.kill("bg anr", true);
     */
    /* JADX WARNING: Missing block: B:217:0x0615, code:
            monitor-exit(r6);
     */
    /* JADX WARNING: Missing block: B:222:0x061c, code:
            com.android.server.am.ActivityManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Missing block: B:224:0x0620, code:
            if (r51 == null) goto L_0x0716;
     */
    /* JADX WARNING: Missing block: B:226:?, code:
            r5 = r51.shortComponentName;
     */
    /* JADX WARNING: Missing block: B:227:0x0627, code:
            if (r54 == null) goto L_0x071a;
     */
    /* JADX WARNING: Missing block: B:228:0x0629, code:
            r4 = "ANR " + r54;
     */
    /* JADX WARNING: Missing block: B:229:0x063f, code:
            makeAppNotRespondingLocked(r50, r5, r4, r25.toString());
     */
    /* JADX WARNING: Missing block: B:230:0x0652, code:
            if (android.os.SystemProperties.getBoolean("persist.sys.enableTraceRename", false) == false) goto L_0x06d5;
     */
    /* JADX WARNING: Missing block: B:231:0x0654, code:
            r48 = android.os.SystemProperties.get("dalvik.vm.stack-trace-file", null);
     */
    /* JADX WARNING: Missing block: B:232:0x065c, code:
            if (r48 == null) goto L_0x06d5;
     */
    /* JADX WARNING: Missing block: B:234:0x0662, code:
            if (r48.length() == 0) goto L_0x06d5;
     */
    /* JADX WARNING: Missing block: B:235:0x0664, code:
            r0 = new java.io.File(r48);
            r29 = r48.lastIndexOf(".");
     */
    /* JADX WARNING: Missing block: B:236:0x0679, code:
            if (-1 == r29) goto L_0x071f;
     */
    /* JADX WARNING: Missing block: B:237:0x067b, code:
            r35 = r48.substring(0, r29) + com.android.server.LocationManagerService.OPPO_FAKE_LOCATION_SPLIT + r50.processName + com.android.server.LocationManagerService.OPPO_FAKE_LOCATION_SPLIT + r49.mTraceDateFormat.format(new java.util.Date()) + r48.substring(r29);
     */
    /* JADX WARNING: Missing block: B:238:0x06c4, code:
            r0.renameTo(new java.io.File(r35));
            android.os.SystemClock.sleep(1000);
     */
    /* JADX WARNING: Missing block: B:239:0x06d5, code:
            r32 = android.os.Message.obtain();
            r30 = new java.util.HashMap();
            r32.what = 2;
            r32.obj = r30;
     */
    /* JADX WARNING: Missing block: B:240:0x06e9, code:
            if (r53 == false) goto L_0x073e;
     */
    /* JADX WARNING: Missing block: B:241:0x06eb, code:
            r4 = 1;
     */
    /* JADX WARNING: Missing block: B:242:0x06ec, code:
            r32.arg1 = r4;
            r30.put("app", r50);
     */
    /* JADX WARNING: Missing block: B:243:0x06fa, code:
            if (r51 == null) goto L_0x0706;
     */
    /* JADX WARNING: Missing block: B:244:0x06fc, code:
            r30.put(com.android.server.am.OppoAppStartupManager.TYPE_ACTIVITY, r51);
     */
    /* JADX WARNING: Missing block: B:245:0x0706, code:
            r49.mService.mUiHandler.sendMessage(r32);
     */
    /* JADX WARNING: Missing block: B:246:0x0711, code:
            monitor-exit(r6);
     */
    /* JADX WARNING: Missing block: B:247:0x0712, code:
            com.android.server.am.ActivityManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Missing block: B:248:0x0715, code:
            return;
     */
    /* JADX WARNING: Missing block: B:249:0x0716, code:
            r5 = null;
     */
    /* JADX WARNING: Missing block: B:251:?, code:
            r4 = "ANR";
     */
    /* JADX WARNING: Missing block: B:252:0x071f, code:
            r35 = r48 + com.android.server.LocationManagerService.OPPO_FAKE_LOCATION_SPLIT + r50.processName;
     */
    /* JADX WARNING: Missing block: B:253:0x073e, code:
            r4 = 0;
     */
    /* JADX WARNING: Missing block: B:255:0x0742, code:
            com.android.server.am.ActivityManagerService.resetPriorityAfterLockedSection();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    final void appNotRespondingInner(ProcessRecord app, ActivityRecord activity, ActivityRecord parent, boolean aboveSystem, String annotation) {
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
        boolean showBackground = Secure.getInt(this.mContext.getContentResolver(), "anr_show_background", 0) != 0;
        synchronized (this.mService) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                if (this.mService.mShuttingDown) {
                    Slog.i(TAG, "During shutdown skipping ANR: " + app + " " + annotation);
                } else if (app.notResponding) {
                    Slog.i(TAG, "Skipping duplicate ANR: " + app + " " + annotation);
                    ActivityManagerService.resetPriorityAfterLockedSection();
                } else if (app.crashing) {
                    Slog.i(TAG, "Crashing app skipping ANR: " + app + " " + annotation);
                    ActivityManagerService.resetPriorityAfterLockedSection();
                } else if (app.killedByAm) {
                    Slog.i(TAG, "App already killed by AM skipping ANR: " + app + " " + annotation);
                    ActivityManagerService.resetPriorityAfterLockedSection();
                } else if (app.killed) {
                    Slog.i(TAG, "Skipping died app ANR: " + app + " " + annotation);
                    ActivityManagerService.resetPriorityAfterLockedSection();
                } else {
                    if (app != null) {
                        if (ElsaManager.isFrozingByPid(app.pid)) {
                            Slog.i(TAG, "Frozing app skipping ANR: " + app + " " + annotation);
                            ActivityManagerService.resetPriorityAfterLockedSection();
                            return;
                        }
                    }
                    app.notResponding = true;
                    EventLog.writeEvent(EventLogTags.AM_ANR, new Object[]{Integer.valueOf(app.userId), Integer.valueOf(app.pid), app.processName, Integer.valueOf(app.info.flags), annotation});
                    arrayList.add(Integer.valueOf(app.pid));
                    boolean dumpAllStacks = SystemProperties.getBoolean("persist.sys.assert.panic", false);
                    int isSilentANR = !showBackground ? isInterestingForBackgroundTraces(app) ^ 1 : 0;
                    if (isSilentANR == 0) {
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
                        for (int i = this.mService.mLruProcesses.size() - 1; i >= 0; i--) {
                            ProcessRecord r = (ProcessRecord) this.mService.mLruProcesses.get(i);
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
                                    } else if (r.treatLikeActivity) {
                                        if (procNumToDumpStackLastPids < maxProcForStackDump) {
                                            arrayList.add(Integer.valueOf(pid));
                                            if (ActivityManagerDebugConfig.DEBUG_ANR) {
                                                Slog.i(TAG, "Adding likely IME: " + r);
                                            }
                                            procNumToDumpStackLastPids++;
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

    private void makeAppNotRespondingLocked(ProcessRecord app, String activity, String shortMsg, String longMsg) {
        app.notResponding = true;
        app.notRespondingReport = generateProcessError(app, 2, activity, shortMsg, longMsg, null);
        startAppProblemLocked(app);
        app.stopFreezingAllLocked();
    }

    /* JADX WARNING: Missing block: B:30:0x00ea, code:
            com.android.server.am.ActivityManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Missing block: B:31:0x00ed, code:
            if (r18 == null) goto L_0x00f2;
     */
    /* JADX WARNING: Missing block: B:32:0x00ef, code:
            r18.show();
     */
    /* JADX WARNING: Missing block: B:33:0x00f2, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void handleShowAnrUi(Message msg) {
        synchronized (this.mService) {
            ActivityManagerService.boostPriorityForLockedSection();
            HashMap<String, Object> data = msg.obj;
            ProcessRecord proc = (ProcessRecord) data.get("app");
            if (proc == null || proc.anrDialog == null) {
                try {
                    Intent intent = new Intent("android.intent.action.ANR");
                    if (!this.mService.mProcessesReady) {
                        intent.addFlags(1342177280);
                    }
                    this.mService.broadcastIntentLocked(null, null, intent, null, null, 0, null, null, null, -1, null, false, false, ActivityManagerService.MY_PID, 1000, 0);
                    boolean showBackground = Secure.getInt(this.mContext.getContentResolver(), "anr_show_background", 0) != 0;
                    if (this.mService.canShowErrorDialogs() || showBackground) {
                        boolean isDebugVersion = SystemProperties.getBoolean("persist.sys.assert.panic", false);
                        boolean isAgingVersion = LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(SystemProperties.get("SPECIAL_OPPO_CONFIG", "0"));
                        if (!isDebugVersion || (isAgingVersion ^ 1) == 0) {
                            if (proc.anrDialog != null) {
                                Slog.w(TAG, " Dismiss app ANR dialog : " + proc.processName);
                                proc.anrDialog = null;
                            }
                            this.mService.killAppAtUsersRequest(proc, null);
                        } else {
                            proc.anrDialog = new AppNotRespondingDialog(this.mService, this.mContext, proc, (ActivityRecord) data.get(OppoAppStartupManager.TYPE_ACTIVITY), msg.arg1 != 0);
                        }
                    } else {
                        MetricsLogger.action(this.mContext, 317, -1);
                        this.mService.killAppAtUsersRequest(proc, null);
                    }
                    Dialog d = proc.anrDialog;
                } finally {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            } else {
                Slog.e(TAG, "App already has anr dialog: " + proc);
                MetricsLogger.action(this.mContext, 317, -2);
            }
        }
    }
}
