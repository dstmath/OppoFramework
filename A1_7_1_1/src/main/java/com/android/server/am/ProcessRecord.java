package com.android.server.am;

import android.annotation.OppoHook;
import android.annotation.OppoHook.OppoHookType;
import android.annotation.OppoHook.OppoRomType;
import android.app.ActivityManager.ProcessErrorStateInfo;
import android.app.Dialog;
import android.app.IApplicationThread;
import android.app.IInstrumentationWatcher;
import android.app.IUiAutomationConnection;
import android.content.ComponentName;
import android.content.pm.ApplicationInfo;
import android.content.res.CompatibilityInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.Trace;
import android.os.UserHandle;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.DebugUtils;
import android.util.EventLog;
import android.util.PrintWriterPrinter;
import android.util.Slog;
import android.util.TimeUtils;
import com.android.internal.app.procstats.ProcessState;
import com.android.internal.app.procstats.ProcessStats.ProcessStateHolder;
import com.android.internal.os.BatteryStatsImpl;
import com.android.internal.os.BatteryStatsImpl.Uid.Proc;
import com.android.server.display.DisplayTransformManager;
import com.android.server.oppo.IElsaManager;
import java.io.PrintWriter;
import java.util.ArrayList;

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
final class ProcessRecord {
    private static final String TAG = null;
    final ArrayList<ActivityRecord> activities;
    int adjSeq;
    Object adjSource;
    int adjSourceProcState;
    Object adjTarget;
    String adjType;
    int adjTypeCode;
    Dialog anrDialog;
    BroadcastRecord backCurReceiver;
    boolean bad;
    ProcessState baseProcessTracker;
    boolean cached;
    CompatibilityInfo compat;
    final ArrayList<ContentProviderConnection> conProviders;
    final ArraySet<ConnectionRecord> connections;
    Dialog crashDialog;
    Runnable crashHandler;
    boolean crashing;
    ProcessErrorStateInfo crashingReport;
    int curAdj;
    long curCpuTime;
    Proc curProcBatteryStats;
    int curProcState;
    int curRawAdj;
    BroadcastRecord curReceiver;
    int curSchedGroup;
    DeathRecipient deathRecipient;
    boolean debugging;
    boolean empty;
    ComponentName errorReportReceiver;
    boolean execServicesFg;
    final ArraySet<ServiceRecord> executingServices;
    long fgInteractionTime;
    boolean forceCrashReport;
    IBinder forcingToForeground;
    BroadcastRecord foreCurReceiver;
    boolean foregroundActivities;
    boolean foregroundServices;
    int[] gids;
    boolean hasAboveClient;
    boolean hasClientActivities;
    boolean hasShownUi;
    boolean hasStartedServices;
    boolean hasTopUi;
    public boolean inFullBackup;
    final ApplicationInfo info;
    long initialIdlePss;
    String instructionSet;
    Bundle instrumentationArguments;
    ComponentName instrumentationClass;
    ApplicationInfo instrumentationInfo;
    String instrumentationProfileFile;
    ComponentName instrumentationResultClass;
    IUiAutomationConnection instrumentationUiAutomationConnection;
    IInstrumentationWatcher instrumentationWatcher;
    long interactionEventTime;
    boolean isANR;
    boolean isLaunchingProvider;
    @OppoHook(level = OppoHookType.NEW_FIELD, note = "WangLan@Plf.Framework, modify for permission intercept", property = OppoRomType.ROM)
    int isSelected;
    @OppoHook(level = OppoHookType.NEW_FIELD, note = "WangLan@Plf.Framework, modify for permission intercept", property = OppoRomType.ROM)
    volatile boolean isWaitingPermissionChoice;
    final boolean isolated;
    boolean killed;
    boolean killedByAm;
    long lastActivityTime;
    long lastCachedPss;
    long lastCachedSwapPss;
    long lastCpuTime;
    long lastLowMemory;
    long lastProviderTime;
    long lastPss;
    long lastPssTime;
    long lastRequestedGc;
    long lastStateTime;
    long lastSwapPss;
    long lastWakeTime;
    int lruSeq;
    private final BatteryStatsImpl mBatteryStats;
    @OppoHook(level = OppoHookType.NEW_FIELD, note = "WangLan@Plf.Framework, modify for permission intercept", property = OppoRomType.ROM)
    PackagePermission mPackagePermission;
    @OppoHook(level = OppoHookType.NEW_FIELD, note = "WangLan@Plf.Framework, modify for permission intercept", property = OppoRomType.ROM)
    PackagePermission mPersistPackagePermission;
    int maxAdj;
    long nextPssTime;
    boolean notCachedSinceIdle;
    boolean notResponding;
    ProcessErrorStateInfo notRespondingReport;
    BroadcastRecord oppobackCurReceiver;
    BroadcastRecord oppoforeCurReceiver;
    boolean pendingUiClean;
    boolean persistent;
    int pid;
    ArraySet<String> pkgDeps;
    final ArrayMap<String, ProcessStateHolder> pkgList;
    String procStatFile;
    boolean procStateChanged;
    final String processName;
    int pssProcState;
    final ArrayMap<String, ContentProviderRecord> pubProviders;
    final ArrayList<ReceiverRecord> receiverRecords;
    final ArraySet<ReceiverList> receivers;
    boolean removed;
    int renderThreadTid;
    boolean repForegroundActivities;
    int repProcState;
    boolean reportLowMemory;
    boolean reportedInteraction;
    String requiredAbi;
    int savedPriority;
    boolean serviceHighRam;
    boolean serviceb;
    final ArraySet<ServiceRecord> services;
    int setAdj;
    boolean setIsForeground;
    int setProcState;
    int setRawAdj;
    int setSchedGroup;
    String shortStringName;
    boolean starting;
    String stringName;
    boolean systemNoUi;
    IApplicationThread thread;
    boolean treatLikeActivity;
    int trimMemoryLevel;
    final int uid;
    UidRecord uidRecord;
    boolean unlocked;
    final int userId;
    boolean usingWrapper;
    int verifiedAdj;
    int vrThreadTid;
    Dialog waitDialog;
    boolean waitedForDebugger;
    String waitingToKill;
    boolean whitelistManager;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.am.ProcessRecord.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.am.ProcessRecord.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.am.ProcessRecord.<clinit>():void");
    }

    void dump(PrintWriter pw, String prefix) {
        int i;
        long now = SystemClock.uptimeMillis();
        pw.print(prefix);
        pw.print("user #");
        pw.print(this.userId);
        pw.print(" uid=");
        pw.print(this.info.uid);
        if (this.uid != this.info.uid) {
            pw.print(" ISOLATED uid=");
            pw.print(this.uid);
        }
        pw.print(" gids={");
        if (this.gids != null) {
            for (int gi = 0; gi < this.gids.length; gi++) {
                if (gi != 0) {
                    pw.print(", ");
                }
                pw.print(this.gids[gi]);
            }
        }
        pw.println("}");
        pw.print(prefix);
        pw.print("requiredAbi=");
        pw.print(this.requiredAbi);
        pw.print(" instructionSet=");
        pw.println(this.instructionSet);
        if (this.info.className != null) {
            pw.print(prefix);
            pw.print("class=");
            pw.println(this.info.className);
        }
        if (this.info.manageSpaceActivityName != null) {
            pw.print(prefix);
            pw.print("manageSpaceActivityName=");
            pw.println(this.info.manageSpaceActivityName);
        }
        pw.print(prefix);
        pw.print("dir=");
        pw.print(this.info.sourceDir);
        pw.print(" publicDir=");
        pw.print(this.info.publicSourceDir);
        pw.print(" data=");
        pw.println(this.info.dataDir);
        pw.print(prefix);
        pw.print("packageList={");
        for (i = 0; i < this.pkgList.size(); i++) {
            if (i > 0) {
                pw.print(", ");
            }
            pw.print((String) this.pkgList.keyAt(i));
        }
        pw.println("}");
        if (this.pkgDeps != null) {
            pw.print(prefix);
            pw.print("packageDependencies={");
            for (i = 0; i < this.pkgDeps.size(); i++) {
                if (i > 0) {
                    pw.print(", ");
                }
                pw.print((String) this.pkgDeps.valueAt(i));
            }
            pw.println("}");
        }
        pw.print(prefix);
        pw.print("compat=");
        pw.println(this.compat);
        if (!(this.instrumentationClass == null && this.instrumentationProfileFile == null && this.instrumentationArguments == null)) {
            pw.print(prefix);
            pw.print("instrumentationClass=");
            pw.print(this.instrumentationClass);
            pw.print(" instrumentationProfileFile=");
            pw.println(this.instrumentationProfileFile);
            pw.print(prefix);
            pw.print("instrumentationArguments=");
            pw.println(this.instrumentationArguments);
            pw.print(prefix);
            pw.print("instrumentationInfo=");
            pw.println(this.instrumentationInfo);
            if (this.instrumentationInfo != null) {
                this.instrumentationInfo.dump(new PrintWriterPrinter(pw), prefix + "  ");
            }
        }
        pw.print(prefix);
        pw.print("thread=");
        pw.println(this.thread);
        pw.print(prefix);
        pw.print("pid=");
        pw.print(this.pid);
        pw.print(" starting=");
        pw.println(this.starting);
        pw.print(prefix);
        pw.print("lastActivityTime=");
        TimeUtils.formatDuration(this.lastActivityTime, now, pw);
        pw.print(" lastPssTime=");
        TimeUtils.formatDuration(this.lastPssTime, now, pw);
        pw.print(" nextPssTime=");
        TimeUtils.formatDuration(this.nextPssTime, now, pw);
        pw.println();
        pw.print(prefix);
        pw.print("adjSeq=");
        pw.print(this.adjSeq);
        pw.print(" lruSeq=");
        pw.print(this.lruSeq);
        pw.print(" lastPss=");
        DebugUtils.printSizeValue(pw, this.lastPss * 1024);
        pw.print(" lastSwapPss=");
        DebugUtils.printSizeValue(pw, this.lastSwapPss * 1024);
        pw.print(" lastCachedPss=");
        DebugUtils.printSizeValue(pw, this.lastCachedPss * 1024);
        pw.print(" lastCachedSwapPss=");
        DebugUtils.printSizeValue(pw, this.lastCachedSwapPss * 1024);
        pw.println();
        pw.print(prefix);
        pw.print("cached=");
        pw.print(this.cached);
        pw.print(" empty=");
        pw.println(this.empty);
        if (this.serviceb) {
            pw.print(prefix);
            pw.print("serviceb=");
            pw.print(this.serviceb);
            pw.print(" serviceHighRam=");
            pw.println(this.serviceHighRam);
        }
        if (this.notCachedSinceIdle) {
            pw.print(prefix);
            pw.print("notCachedSinceIdle=");
            pw.print(this.notCachedSinceIdle);
            pw.print(" initialIdlePss=");
            pw.println(this.initialIdlePss);
        }
        pw.print(prefix);
        pw.print("oom: max=");
        pw.print(this.maxAdj);
        pw.print(" curRaw=");
        pw.print(this.curRawAdj);
        pw.print(" setRaw=");
        pw.print(this.setRawAdj);
        pw.print(" cur=");
        pw.print(this.curAdj);
        pw.print(" set=");
        pw.println(this.setAdj);
        pw.print(prefix);
        pw.print("curSchedGroup=");
        pw.print(this.curSchedGroup);
        pw.print(" setSchedGroup=");
        pw.print(this.setSchedGroup);
        pw.print(" systemNoUi=");
        pw.print(this.systemNoUi);
        pw.print(" trimMemoryLevel=");
        pw.println(this.trimMemoryLevel);
        pw.print(prefix);
        pw.print("vrThreadTid=");
        pw.print(this.vrThreadTid);
        pw.print(prefix);
        pw.print("curProcState=");
        pw.print(this.curProcState);
        pw.print(" repProcState=");
        pw.print(this.repProcState);
        pw.print(" pssProcState=");
        pw.print(this.pssProcState);
        pw.print(" setProcState=");
        pw.print(this.setProcState);
        pw.print(" lastStateTime=");
        TimeUtils.formatDuration(this.lastStateTime, now, pw);
        pw.println();
        if (this.hasShownUi || this.pendingUiClean || this.hasAboveClient || this.treatLikeActivity) {
            pw.print(prefix);
            pw.print("hasShownUi=");
            pw.print(this.hasShownUi);
            pw.print(" pendingUiClean=");
            pw.print(this.pendingUiClean);
            pw.print(" hasAboveClient=");
            pw.print(this.hasAboveClient);
            pw.print(" treatLikeActivity=");
            pw.println(this.treatLikeActivity);
        }
        if (this.setIsForeground || this.foregroundServices || this.forcingToForeground != null) {
            pw.print(prefix);
            pw.print("setIsForeground=");
            pw.print(this.setIsForeground);
            pw.print(" foregroundServices=");
            pw.print(this.foregroundServices);
            pw.print(" forcingToForeground=");
            pw.println(this.forcingToForeground);
        }
        if (this.reportedInteraction || this.fgInteractionTime != 0) {
            pw.print(prefix);
            pw.print("reportedInteraction=");
            pw.print(this.reportedInteraction);
            if (this.interactionEventTime != 0) {
                pw.print(" time=");
                TimeUtils.formatDuration(this.interactionEventTime, SystemClock.elapsedRealtime(), pw);
            }
            if (this.fgInteractionTime != 0) {
                pw.print(" fgInteractionTime=");
                TimeUtils.formatDuration(this.fgInteractionTime, SystemClock.elapsedRealtime(), pw);
            }
            pw.println();
        }
        if (this.persistent || this.removed) {
            pw.print(prefix);
            pw.print("persistent=");
            pw.print(this.persistent);
            pw.print(" removed=");
            pw.println(this.removed);
        }
        if (this.hasClientActivities || this.foregroundActivities || this.repForegroundActivities) {
            pw.print(prefix);
            pw.print("hasClientActivities=");
            pw.print(this.hasClientActivities);
            pw.print(" foregroundActivities=");
            pw.print(this.foregroundActivities);
            pw.print(" (rep=");
            pw.print(this.repForegroundActivities);
            pw.println(")");
        }
        if (this.lastProviderTime > 0) {
            pw.print(prefix);
            pw.print("lastProviderTime=");
            TimeUtils.formatDuration(this.lastProviderTime, now, pw);
            pw.println();
        }
        if (this.hasStartedServices) {
            pw.print(prefix);
            pw.print("hasStartedServices=");
            pw.println(this.hasStartedServices);
        }
        if (this.setProcState >= 10) {
            long wtime;
            synchronized (this.mBatteryStats) {
                wtime = this.mBatteryStats.getProcessWakeTime(this.info.uid, this.pid, SystemClock.elapsedRealtime());
            }
            pw.print(prefix);
            pw.print("lastWakeTime=");
            pw.print(this.lastWakeTime);
            pw.print(" timeUsed=");
            TimeUtils.formatDuration(wtime - this.lastWakeTime, pw);
            pw.println(IElsaManager.EMPTY_PACKAGE);
            pw.print(prefix);
            pw.print("lastCpuTime=");
            pw.print(this.lastCpuTime);
            pw.print(" timeUsed=");
            TimeUtils.formatDuration(this.curCpuTime - this.lastCpuTime, pw);
            pw.println(IElsaManager.EMPTY_PACKAGE);
        }
        pw.print(prefix);
        pw.print("lastRequestedGc=");
        TimeUtils.formatDuration(this.lastRequestedGc, now, pw);
        pw.print(" lastLowMemory=");
        TimeUtils.formatDuration(this.lastLowMemory, now, pw);
        pw.print(" reportLowMemory=");
        pw.println(this.reportLowMemory);
        if (this.killed || this.killedByAm || this.waitingToKill != null) {
            pw.print(prefix);
            pw.print("killed=");
            pw.print(this.killed);
            pw.print(" killedByAm=");
            pw.print(this.killedByAm);
            pw.print(" waitingToKill=");
            pw.println(this.waitingToKill);
        }
        if (this.debugging || this.crashing || this.crashDialog != null || this.notResponding || this.anrDialog != null || this.bad) {
            pw.print(prefix);
            pw.print("debugging=");
            pw.print(this.debugging);
            pw.print(" crashing=");
            pw.print(this.crashing);
            pw.print(" ");
            pw.print(this.crashDialog);
            pw.print(" notResponding=");
            pw.print(this.notResponding);
            pw.print(" ");
            pw.print(this.anrDialog);
            pw.print(" bad=");
            pw.print(this.bad);
            if (this.errorReportReceiver != null) {
                pw.print(" errorReportReceiver=");
                pw.print(this.errorReportReceiver.flattenToShortString());
            }
            pw.println();
        }
        if (this.whitelistManager) {
            pw.print(prefix);
            pw.print("whitelistManager=");
            pw.println(this.whitelistManager);
        }
        if (this.activities.size() > 0) {
            pw.print(prefix);
            pw.println("Activities:");
            for (i = 0; i < this.activities.size(); i++) {
                pw.print(prefix);
                pw.print("  - ");
                pw.println(this.activities.get(i));
            }
        }
        if (this.services.size() > 0) {
            pw.print(prefix);
            pw.println("Services:");
            for (i = 0; i < this.services.size(); i++) {
                pw.print(prefix);
                pw.print("  - ");
                pw.println(this.services.valueAt(i));
            }
        }
        if (this.executingServices.size() > 0) {
            pw.print(prefix);
            pw.print("Executing Services (fg=");
            pw.print(this.execServicesFg);
            pw.println(")");
            for (i = 0; i < this.executingServices.size(); i++) {
                pw.print(prefix);
                pw.print("  - ");
                pw.println(this.executingServices.valueAt(i));
            }
        }
        if (this.connections.size() > 0) {
            pw.print(prefix);
            pw.println("Connections:");
            for (i = 0; i < this.connections.size(); i++) {
                pw.print(prefix);
                pw.print("  - ");
                pw.println(this.connections.valueAt(i));
            }
        }
        if (this.pubProviders.size() > 0) {
            pw.print(prefix);
            pw.println("Published Providers:");
            for (i = 0; i < this.pubProviders.size(); i++) {
                pw.print(prefix);
                pw.print("  - ");
                pw.println((String) this.pubProviders.keyAt(i));
                pw.print(prefix);
                pw.print("    -> ");
                pw.println(this.pubProviders.valueAt(i));
            }
        }
        if (this.conProviders.size() > 0) {
            pw.print(prefix);
            pw.println("Connected Providers:");
            for (i = 0; i < this.conProviders.size(); i++) {
                pw.print(prefix);
                pw.print("  - ");
                pw.println(((ContentProviderConnection) this.conProviders.get(i)).toShortString());
            }
        }
        if (this.curReceiver != null) {
            pw.print(prefix);
            pw.print("curReceiver=");
            pw.println(this.curReceiver);
        }
        if (this.receivers.size() > 0) {
            pw.print(prefix);
            pw.println("Receivers:");
            for (i = 0; i < this.receivers.size(); i++) {
                pw.print(prefix);
                pw.print("  - ");
                pw.println(this.receivers.valueAt(i));
            }
        }
        if (this.hasTopUi) {
            pw.print(prefix);
            pw.print("hasTopUi=");
            pw.print(this.hasTopUi);
        }
    }

    ProcessRecord(BatteryStatsImpl _batteryStats, ApplicationInfo _info, String _processName, int _uid) {
        boolean z;
        this.pkgList = new ArrayMap();
        this.curProcState = -1;
        this.repProcState = -1;
        this.setProcState = -1;
        this.pssProcState = -1;
        this.receiverRecords = new ArrayList();
        this.isLaunchingProvider = false;
        this.activities = new ArrayList();
        this.services = new ArraySet();
        this.executingServices = new ArraySet();
        this.connections = new ArraySet();
        this.receivers = new ArraySet();
        this.pubProviders = new ArrayMap();
        this.conProviders = new ArrayList();
        this.mBatteryStats = _batteryStats;
        this.info = _info;
        if (_info.uid != _uid) {
            z = true;
        } else {
            z = false;
        }
        this.isolated = z;
        this.uid = _uid;
        this.userId = UserHandle.getUserId(_uid);
        this.processName = _processName;
        this.pkgList.put(_info.packageName, new ProcessStateHolder(_info.versionCode));
        this.maxAdj = 1001;
        this.setRawAdj = -10000;
        this.curRawAdj = -10000;
        this.verifiedAdj = -10000;
        this.setAdj = -10000;
        this.curAdj = -10000;
        this.persistent = false;
        this.removed = false;
        long uptimeMillis = SystemClock.uptimeMillis();
        this.nextPssTime = uptimeMillis;
        this.lastPssTime = uptimeMillis;
        this.lastStateTime = uptimeMillis;
    }

    public void setPid(int _pid) {
        this.pid = _pid;
        this.procStatFile = null;
        this.shortStringName = null;
        this.stringName = null;
    }

    public void makeActive(IApplicationThread _thread, ProcessStatsService tracker) {
        if (this.thread == null) {
            ProcessState origBase = this.baseProcessTracker;
            if (origBase != null) {
                origBase.setState(-1, tracker.getMemFactorLocked(), SystemClock.uptimeMillis(), this.pkgList);
                origBase.makeInactive();
            }
            this.baseProcessTracker = tracker.getProcessStateLocked(this.info.packageName, this.uid, this.info.versionCode, this.processName);
            this.baseProcessTracker.makeActive();
            for (int i = 0; i < this.pkgList.size(); i++) {
                ProcessStateHolder holder = (ProcessStateHolder) this.pkgList.valueAt(i);
                if (!(holder.state == null || holder.state == origBase)) {
                    holder.state.makeInactive();
                }
                holder.state = tracker.getProcessStateLocked((String) this.pkgList.keyAt(i), this.uid, this.info.versionCode, this.processName);
                if (holder.state != this.baseProcessTracker) {
                    holder.state.makeActive();
                }
            }
        }
        this.thread = _thread;
    }

    public void makeInactive(ProcessStatsService tracker) {
        this.thread = null;
        ProcessState origBase = this.baseProcessTracker;
        if (origBase != null) {
            if (origBase != null) {
                origBase.setState(-1, tracker.getMemFactorLocked(), SystemClock.uptimeMillis(), this.pkgList);
                origBase.makeInactive();
            }
            this.baseProcessTracker = null;
            for (int i = 0; i < this.pkgList.size(); i++) {
                ProcessStateHolder holder = (ProcessStateHolder) this.pkgList.valueAt(i);
                if (!(holder.state == null || holder.state == origBase)) {
                    holder.state.makeInactive();
                }
                holder.state = null;
            }
        }
    }

    public boolean isInterestingToUserLocked() {
        int size = this.activities.size();
        for (int i = 0; i < size; i++) {
            if (((ActivityRecord) this.activities.get(i)).isInterestingToUserLocked()) {
                return true;
            }
        }
        return false;
    }

    public void stopFreezingAllLocked() {
        int i = this.activities.size();
        while (i > 0) {
            i--;
            ((ActivityRecord) this.activities.get(i)).stopFreezingScreenLocked(true);
        }
    }

    public void unlinkDeathRecipient() {
        if (!(this.deathRecipient == null || this.thread == null)) {
            this.thread.asBinder().unlinkToDeath(this.deathRecipient, 0);
        }
        this.deathRecipient = null;
    }

    void updateHasAboveClientLocked() {
        this.hasAboveClient = false;
        for (int i = this.connections.size() - 1; i >= 0; i--) {
            if ((((ConnectionRecord) this.connections.valueAt(i)).flags & 8) != 0) {
                this.hasAboveClient = true;
                return;
            }
        }
    }

    int modifyRawOomAdj(int adj) {
        if (!this.hasAboveClient || adj < 0) {
            return adj;
        }
        if (adj < 100) {
            return 100;
        }
        if (adj < DisplayTransformManager.LEVEL_COLOR_MATRIX_GRAYSCALE) {
            return DisplayTransformManager.LEVEL_COLOR_MATRIX_GRAYSCALE;
        }
        if (adj < 900) {
            return 900;
        }
        if (adj < 906) {
            return adj + 1;
        }
        return adj;
    }

    void scheduleCrash(String message) {
        if (!(this.killedByAm || this.thread == null)) {
            if (this.pid == Process.myPid()) {
                Slog.w(TAG, "scheduleCrash: trying to crash system process!");
                return;
            }
            long ident = Binder.clearCallingIdentity();
            try {
                this.thread.scheduleCrash(message);
            } catch (RemoteException e) {
                kill("scheduleCrash for '" + message + "' failed", true);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    void kill(String reason, boolean noisy) {
        if (!this.killedByAm) {
            Trace.traceBegin(64, "kill");
            if (noisy) {
                Slog.i(TAG, "Killing " + toShortString() + " (adj " + this.setAdj + "): " + reason);
            }
            Object[] objArr = new Object[5];
            objArr[0] = Integer.valueOf(this.userId);
            objArr[1] = Integer.valueOf(this.pid);
            objArr[2] = this.processName;
            objArr[3] = Integer.valueOf(this.setAdj);
            objArr[4] = reason;
            EventLog.writeEvent(EventLogTags.AM_KILL, objArr);
            Process.killProcessQuiet(this.pid);
            ActivityManagerService.killProcessGroup(this.uid, this.pid);
            if (!this.persistent) {
                this.killed = true;
                this.killedByAm = true;
            }
            Trace.traceEnd(64);
        }
    }

    void oppoClearProcess(String reason, boolean noisy) {
        if (!this.killedByAm) {
            if (noisy) {
                Slog.i(TAG, "Killing " + toShortString() + " (adj " + this.setAdj + "): " + reason);
            }
            Object[] objArr = new Object[5];
            objArr[0] = Integer.valueOf(this.userId);
            objArr[1] = Integer.valueOf(this.pid);
            objArr[2] = this.processName;
            objArr[3] = Integer.valueOf(this.setAdj);
            objArr[4] = reason;
            EventLog.writeEvent(EventLogTags.AM_KILL, objArr);
            Process.killProcessQuiet(this.pid);
            if (!this.persistent) {
                this.killed = true;
                this.killedByAm = true;
            }
        }
    }

    public String toShortString() {
        if (this.shortStringName != null) {
            return this.shortStringName;
        }
        StringBuilder sb = new StringBuilder(128);
        toShortString(sb);
        String stringBuilder = sb.toString();
        this.shortStringName = stringBuilder;
        return stringBuilder;
    }

    void toShortString(StringBuilder sb) {
        sb.append(this.pid);
        sb.append(':');
        sb.append(this.processName);
        sb.append('/');
        if (this.info.uid < 10000) {
            sb.append(this.uid);
            return;
        }
        sb.append('u');
        sb.append(this.userId);
        int appId = UserHandle.getAppId(this.info.uid);
        if (appId >= 10000) {
            sb.append('a');
            sb.append(appId - 10000);
        } else {
            sb.append('s');
            sb.append(appId);
        }
        if (this.uid != this.info.uid) {
            sb.append('i');
            sb.append(UserHandle.getAppId(this.uid) - 99000);
        }
    }

    public String toString() {
        if (this.stringName != null) {
            return this.stringName;
        }
        StringBuilder sb = new StringBuilder(128);
        sb.append("ProcessRecord{");
        sb.append(Integer.toHexString(System.identityHashCode(this)));
        sb.append(' ');
        toShortString(sb);
        sb.append('}');
        String stringBuilder = sb.toString();
        this.stringName = stringBuilder;
        return stringBuilder;
    }

    public String makeAdjReason() {
        if (this.adjSource == null && this.adjTarget == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder(128);
        sb.append(' ');
        if (this.adjTarget instanceof ComponentName) {
            sb.append(((ComponentName) this.adjTarget).flattenToShortString());
        } else if (this.adjTarget != null) {
            sb.append(this.adjTarget.toString());
        } else {
            sb.append("{null}");
        }
        sb.append("<=");
        if (this.adjSource instanceof ProcessRecord) {
            sb.append("Proc{");
            sb.append(((ProcessRecord) this.adjSource).toShortString());
            sb.append("}");
        } else if (this.adjSource != null) {
            sb.append(this.adjSource.toString());
        } else {
            sb.append("{null}");
        }
        return sb.toString();
    }

    public boolean addPackage(String pkg, int versionCode, ProcessStatsService tracker) {
        if (this.pkgList.containsKey(pkg)) {
            return false;
        }
        ProcessStateHolder holder = new ProcessStateHolder(versionCode);
        if (this.baseProcessTracker != null) {
            holder.state = tracker.getProcessStateLocked(pkg, this.uid, versionCode, this.processName);
            this.pkgList.put(pkg, holder);
            if (holder.state != this.baseProcessTracker) {
                holder.state.makeActive();
            }
        } else {
            this.pkgList.put(pkg, holder);
        }
        return true;
    }

    public int getSetAdjWithServices() {
        if (this.setAdj < 900 || !this.hasStartedServices) {
            return this.setAdj;
        }
        return 800;
    }

    public void forceProcessStateUpTo(int newState) {
        if (this.repProcState > newState) {
            this.repProcState = newState;
            this.curProcState = newState;
        }
    }

    public void resetPackageList(ProcessStatsService tracker) {
        int N = this.pkgList.size();
        if (this.baseProcessTracker != null) {
            this.baseProcessTracker.setState(-1, tracker.getMemFactorLocked(), SystemClock.uptimeMillis(), this.pkgList);
            if (N != 1) {
                ProcessStateHolder holder;
                for (int i = 0; i < N; i++) {
                    holder = (ProcessStateHolder) this.pkgList.valueAt(i);
                    if (!(holder.state == null || holder.state == this.baseProcessTracker)) {
                        holder.state.makeInactive();
                    }
                }
                this.pkgList.clear();
                ProcessState ps = tracker.getProcessStateLocked(this.info.packageName, this.uid, this.info.versionCode, this.processName);
                holder = new ProcessStateHolder(this.info.versionCode);
                holder.state = ps;
                this.pkgList.put(this.info.packageName, holder);
                if (ps != this.baseProcessTracker) {
                    ps.makeActive();
                }
            }
        } else if (N != 1) {
            this.pkgList.clear();
            this.pkgList.put(this.info.packageName, new ProcessStateHolder(this.info.versionCode));
        }
    }

    public String[] getPackageList() {
        int size = this.pkgList.size();
        if (size == 0) {
            return null;
        }
        String[] list = new String[size];
        for (int i = 0; i < this.pkgList.size(); i++) {
            list[i] = (String) this.pkgList.keyAt(i);
        }
        return list;
    }
}
