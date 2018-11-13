package com.android.server.am;

import android.annotation.OppoHook;
import android.annotation.OppoHook.OppoHookType;
import android.annotation.OppoHook.OppoRomType;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AppGlobals;
import android.app.AppOpsManager;
import android.app.IApplicationThread;
import android.app.IServiceConnection;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.IIntentSender;
import android.content.Intent;
import android.content.Intent.FilterComparison;
import android.content.IntentSender;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.DeadObjectException;
import android.os.Debug;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteCallback;
import android.os.RemoteCallback.OnResultListener;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.TransactionTooLargeException;
import android.os.UserHandle;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.EventLog;
import android.util.Slog;
import android.util.SparseArray;
import android.util.TimeUtils;
import com.android.internal.app.procstats.ProcessStats.ProcessStateHolder;
import com.android.internal.app.procstats.ServiceState;
import com.android.internal.os.BatteryStatsImpl;
import com.android.internal.os.BatteryStatsImpl.Uid.Pkg.Serv;
import com.android.internal.os.TransferPipe;
import com.android.internal.util.FastPrintWriter;
import com.android.server.LocationManagerService;
import com.android.server.coloros.OppoListManager;
import com.android.server.display.DisplayTransformManager;
import com.android.server.oppo.IElsaManager;
import com.mediatek.am.AMEventHookData.PackageStoppedStatusChanged;
import com.mediatek.am.AMEventHookData.ReadyToStartComponent;
import com.mediatek.am.AMEventHookData.ReadyToStartComponent.Index;
import com.mediatek.am.AMEventHookData.ReadyToStartService;
import com.mediatek.anrmanager.ANRManager;
import com.mediatek.server.am.AMEventHook.Event;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import oppo.util.OppoMultiLauncherUtil;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
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
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public final class ActiveServices {
    static final int BG_START_TIMEOUT = 15000;
    static final String BRING_UP_BIND_SERVICE = "bind service";
    static final String BRING_UP_DELAYED_SERVICE = "delayed service";
    static final String BRING_UP_RESTART_SERVICE = "restart service";
    static final String BRING_UP_START_SERVICE = "start service";
    static final boolean DEBUG_ACTIVE_SERVICES = false;
    static final boolean DEBUG_AGING_VERSION = false;
    private static boolean DEBUG_DELAYED_SERVICE = false;
    private static boolean DEBUG_DELAYED_STARTS = false;
    static final int LAST_ANR_LIFETIME_DURATION_MSECS = 7200000;
    private static boolean LOG_SERVICE_START_STOP = false;
    static final int MAX_SERVICE_INACTIVITY = 1800000;
    static final int SERVICE_BACKGROUND_TIMEOUT = 200000;
    static final int SERVICE_MIN_RESTART_TIME_BETWEEN = 10000;
    private static final boolean SERVICE_RESCHEDULE = false;
    static final int SERVICE_RESET_RUN_DURATION = 60000;
    static final int SERVICE_RESTART_DURATION = 1000;
    static final int SERVICE_RESTART_DURATION_FACTOR = 4;
    static final int SERVICE_TIMEOUT = 20000;
    private static final String TAG = null;
    private static final String TAG_MU = null;
    private static final String TAG_SERVICE = null;
    private static final String TAG_SERVICE_EXECUTING = null;
    final ActivityManagerService mAm;
    private String mBringUpReason;
    private final boolean mCtmFlag;
    private final String mCtmName;
    private String mCurRestartServiceName;
    private String mCurrentCallerPackage;
    private int mCurrentCallerUid;
    final ArrayList<ServiceRecord> mDestroyingServices;
    String mLastAnrDump;
    final Runnable mLastAnrDumpClearer;
    final int mMaxStartingBackground;
    final ArrayList<ServiceRecord> mPendingServices;
    final ArrayList<ServiceRecord> mRestartingServices;
    final ArrayMap<IBinder, ArrayList<ConnectionRecord>> mServiceConnections;
    final SparseArray<ServiceMap> mServiceMap;
    private ArrayList<ServiceRecord> mTmpCollectionResults;

    final class ServiceDumper {
        private final String[] args;
        private final boolean dumpAll;
        private final String dumpPackage;
        private final FileDescriptor fd;
        private final ItemMatcher matcher;
        private boolean needSep = false;
        private final long nowReal = SystemClock.elapsedRealtime();
        private final int opti;
        private boolean printed = false;
        private boolean printedAnything = false;
        private final PrintWriter pw;
        private final ArrayList<ServiceRecord> services = new ArrayList();

        ServiceDumper(FileDescriptor fd, PrintWriter pw, String[] args, int opti, boolean dumpAll, String dumpPackage) {
            this.fd = fd;
            this.pw = pw;
            this.args = args;
            this.opti = opti;
            this.dumpAll = dumpAll;
            this.dumpPackage = dumpPackage;
            this.matcher = new ItemMatcher();
            this.matcher.build(args, opti);
            for (int user : ActiveServices.this.mAm.mUserController.getUsers()) {
                ServiceMap smap = ActiveServices.this.getServiceMap(user);
                if (smap.mServicesByName.size() > 0) {
                    for (int si = 0; si < smap.mServicesByName.size(); si++) {
                        ServiceRecord r = (ServiceRecord) smap.mServicesByName.valueAt(si);
                        if (this.matcher.match(r, r.name)) {
                            if (dumpPackage != null) {
                                if (!dumpPackage.equals(r.appInfo.packageName)) {
                                }
                            }
                            this.services.add(r);
                        }
                    }
                }
            }
        }

        private void dumpHeaderLocked() {
            this.pw.println("ACTIVITY MANAGER SERVICES (dumpsys activity services)");
            if (ActiveServices.this.mLastAnrDump != null) {
                this.pw.println("  Last ANR service:");
                this.pw.print(ActiveServices.this.mLastAnrDump);
                this.pw.println();
            }
        }

        void dumpLocked() {
            dumpHeaderLocked();
            try {
                for (int user : ActiveServices.this.mAm.mUserController.getUsers()) {
                    int serviceIdx = 0;
                    while (serviceIdx < this.services.size() && ((ServiceRecord) this.services.get(serviceIdx)).userId != user) {
                        serviceIdx++;
                    }
                    this.printed = false;
                    if (serviceIdx < this.services.size()) {
                        this.needSep = false;
                        while (serviceIdx < this.services.size()) {
                            ServiceRecord r = (ServiceRecord) this.services.get(serviceIdx);
                            serviceIdx++;
                            if (r.userId != user) {
                                break;
                            }
                            dumpServiceLocalLocked(r);
                        }
                        this.needSep |= this.printed;
                    }
                    dumpUserRemainsLocked(user);
                }
            } catch (Exception e) {
                Slog.w(ActiveServices.TAG, "Exception in dumpServicesLocked", e);
            }
            dumpRemainsLocked();
        }

        void dumpWithClient() {
            synchronized (ActiveServices.this.mAm) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    dumpHeaderLocked();
                } finally {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
            try {
                for (int user : ActiveServices.this.mAm.mUserController.getUsers()) {
                    int serviceIdx = 0;
                    while (serviceIdx < this.services.size() && ((ServiceRecord) this.services.get(serviceIdx)).userId != user) {
                        serviceIdx++;
                    }
                    this.printed = false;
                    if (serviceIdx < this.services.size()) {
                        this.needSep = false;
                        while (serviceIdx < this.services.size()) {
                            ServiceRecord r = (ServiceRecord) this.services.get(serviceIdx);
                            serviceIdx++;
                            if (r.userId != user) {
                                break;
                            }
                            synchronized (ActiveServices.this.mAm) {
                                ActivityManagerService.boostPriorityForLockedSection();
                                dumpServiceLocalLocked(r);
                            }
                            ActivityManagerService.resetPriorityAfterLockedSection();
                            dumpServiceClient(r);
                        }
                        this.needSep |= this.printed;
                    }
                    synchronized (ActiveServices.this.mAm) {
                        ActivityManagerService.boostPriorityForLockedSection();
                        dumpUserRemainsLocked(user);
                    }
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            } catch (Exception e) {
                Slog.w(ActiveServices.TAG, "Exception in dumpServicesLocked", e);
            } catch (Throwable th) {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
            synchronized (ActiveServices.this.mAm) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    dumpRemainsLocked();
                } finally {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        private void dumpUserHeaderLocked(int user) {
            if (!this.printed) {
                if (this.printedAnything) {
                    this.pw.println();
                }
                this.pw.println("  User " + user + " active services:");
                this.printed = true;
            }
            this.printedAnything = true;
            if (this.needSep) {
                this.pw.println();
            }
        }

        private void dumpServiceLocalLocked(ServiceRecord r) {
            dumpUserHeaderLocked(r.userId);
            this.pw.print("  * ");
            this.pw.println(r);
            if (this.dumpAll) {
                r.dump(this.pw, "    ");
                this.needSep = true;
                return;
            }
            this.pw.print("    app=");
            this.pw.println(r.app);
            this.pw.print("    created=");
            TimeUtils.formatDuration(r.createTime, this.nowReal, this.pw);
            this.pw.print(" started=");
            this.pw.print(r.startRequested);
            this.pw.print(" connections=");
            this.pw.println(r.connections.size());
            if (r.connections.size() > 0) {
                this.pw.println("    Connections:");
                for (int conni = 0; conni < r.connections.size(); conni++) {
                    ArrayList<ConnectionRecord> clist = (ArrayList) r.connections.valueAt(conni);
                    for (int i = 0; i < clist.size(); i++) {
                        String toShortString;
                        ConnectionRecord conn = (ConnectionRecord) clist.get(i);
                        this.pw.print("      ");
                        this.pw.print(conn.binding.intent.intent.getIntent().toShortString(false, false, false, false));
                        this.pw.print(" -> ");
                        ProcessRecord proc = conn.binding.client;
                        PrintWriter printWriter = this.pw;
                        if (proc != null) {
                            toShortString = proc.toShortString();
                        } else {
                            toShortString = "null";
                        }
                        printWriter.println(toShortString);
                    }
                }
            }
        }

        private void dumpServiceClient(ServiceRecord r) {
            ProcessRecord proc = r.app;
            if (proc != null) {
                IApplicationThread thread = proc.thread;
                if (thread != null) {
                    this.pw.println("    Client:");
                    this.pw.flush();
                    TransferPipe tp;
                    try {
                        tp = new TransferPipe();
                        thread.dumpService(tp.getWriteFd().getFileDescriptor(), r, this.args);
                        tp.setBufferPrefix("      ");
                        tp.go(this.fd, 2000);
                        tp.kill();
                    } catch (IOException e) {
                        this.pw.println("      Failure while dumping the service: " + e);
                    } catch (RemoteException e2) {
                        this.pw.println("      Got a RemoteException while dumping the service");
                    } catch (Throwable th) {
                        tp.kill();
                    }
                    this.needSep = true;
                }
            }
        }

        private void dumpUserRemainsLocked(int user) {
            int si;
            ServiceRecord r;
            ServiceMap smap = ActiveServices.this.getServiceMap(user);
            this.printed = false;
            int SN = smap.mDelayedStartList.size();
            for (si = 0; si < SN; si++) {
                r = (ServiceRecord) smap.mDelayedStartList.get(si);
                if (this.matcher.match(r, r.name) && (this.dumpPackage == null || this.dumpPackage.equals(r.appInfo.packageName))) {
                    if (!this.printed) {
                        if (this.printedAnything) {
                            this.pw.println();
                        }
                        this.pw.println("  User " + user + " delayed start services:");
                        this.printed = true;
                    }
                    this.printedAnything = true;
                    this.pw.print("  * Delayed start ");
                    this.pw.println(r);
                }
            }
            this.printed = false;
            SN = smap.mStartingBackground.size();
            for (si = 0; si < SN; si++) {
                r = (ServiceRecord) smap.mStartingBackground.get(si);
                if (this.matcher.match(r, r.name) && (this.dumpPackage == null || this.dumpPackage.equals(r.appInfo.packageName))) {
                    if (!this.printed) {
                        if (this.printedAnything) {
                            this.pw.println();
                        }
                        this.pw.println("  User " + user + " starting in background:");
                        this.printed = true;
                    }
                    this.printedAnything = true;
                    this.pw.print("  * Starting bg ");
                    this.pw.println(r);
                }
            }
        }

        private void dumpRemainsLocked() {
            int i;
            ServiceRecord r;
            if (ActiveServices.this.mPendingServices.size() > 0) {
                this.printed = false;
                for (i = 0; i < ActiveServices.this.mPendingServices.size(); i++) {
                    r = (ServiceRecord) ActiveServices.this.mPendingServices.get(i);
                    if (this.matcher.match(r, r.name) && (this.dumpPackage == null || this.dumpPackage.equals(r.appInfo.packageName))) {
                        this.printedAnything = true;
                        if (!this.printed) {
                            if (this.needSep) {
                                this.pw.println();
                            }
                            this.needSep = true;
                            this.pw.println("  Pending services:");
                            this.printed = true;
                        }
                        this.pw.print("  * Pending ");
                        this.pw.println(r);
                        r.dump(this.pw, "    ");
                    }
                }
                this.needSep = true;
            }
            if (ActiveServices.this.mRestartingServices.size() > 0) {
                this.printed = false;
                for (i = 0; i < ActiveServices.this.mRestartingServices.size(); i++) {
                    r = (ServiceRecord) ActiveServices.this.mRestartingServices.get(i);
                    if (this.matcher.match(r, r.name) && (this.dumpPackage == null || this.dumpPackage.equals(r.appInfo.packageName))) {
                        this.printedAnything = true;
                        if (!this.printed) {
                            if (this.needSep) {
                                this.pw.println();
                            }
                            this.needSep = true;
                            this.pw.println("  Restarting services:");
                            this.printed = true;
                        }
                        this.pw.print("  * Restarting ");
                        this.pw.println(r);
                        r.dump(this.pw, "    ");
                    }
                }
                this.needSep = true;
            }
            if (ActiveServices.this.mDestroyingServices.size() > 0) {
                this.printed = false;
                for (i = 0; i < ActiveServices.this.mDestroyingServices.size(); i++) {
                    r = (ServiceRecord) ActiveServices.this.mDestroyingServices.get(i);
                    if (this.matcher.match(r, r.name) && (this.dumpPackage == null || this.dumpPackage.equals(r.appInfo.packageName))) {
                        this.printedAnything = true;
                        if (!this.printed) {
                            if (this.needSep) {
                                this.pw.println();
                            }
                            this.needSep = true;
                            this.pw.println("  Destroying services:");
                            this.printed = true;
                        }
                        this.pw.print("  * Destroy ");
                        this.pw.println(r);
                        r.dump(this.pw, "    ");
                    }
                }
                this.needSep = true;
            }
            if (this.dumpAll) {
                this.printed = false;
                for (int ic = 0; ic < ActiveServices.this.mServiceConnections.size(); ic++) {
                    ArrayList<ConnectionRecord> r2 = (ArrayList) ActiveServices.this.mServiceConnections.valueAt(ic);
                    for (i = 0; i < r2.size(); i++) {
                        ConnectionRecord cr = (ConnectionRecord) r2.get(i);
                        if (this.matcher.match(cr.binding.service, cr.binding.service.name) && (this.dumpPackage == null || (cr.binding.client != null && this.dumpPackage.equals(cr.binding.client.info.packageName)))) {
                            this.printedAnything = true;
                            if (!this.printed) {
                                if (this.needSep) {
                                    this.pw.println();
                                }
                                this.needSep = true;
                                this.pw.println("  Connection bindings to services:");
                                this.printed = true;
                            }
                            this.pw.print("  * ");
                            this.pw.println(cr);
                            cr.dump(this.pw, "    ");
                        }
                    }
                }
            }
            if (!this.printedAnything) {
                this.pw.println("  (nothing)");
            }
        }
    }

    private final class ServiceLookupResult {
        final String permission;
        final ServiceRecord record;

        ServiceLookupResult(ServiceRecord _record, String _permission) {
            this.record = _record;
            this.permission = _permission;
        }
    }

    class ServiceMap extends Handler {
        static final int MSG_BG_START_TIMEOUT = 1;
        final ArrayList<ServiceRecord> mDelayedStartList = new ArrayList();
        final ArrayMap<FilterComparison, ServiceRecord> mServicesByIntent = new ArrayMap();
        final ArrayMap<ComponentName, ServiceRecord> mServicesByName = new ArrayMap();
        final ArrayList<ServiceRecord> mStartingBackground = new ArrayList();
        final int mUserId;

        ServiceMap(Looper looper, int userId) {
            super(looper);
            this.mUserId = userId;
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    synchronized (ActiveServices.this.mAm) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            rescheduleDelayedStarts();
                        } finally {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    return;
                default:
                    return;
            }
        }

        void ensureNotStartingBackground(ServiceRecord r) {
            if (this.mStartingBackground.remove(r)) {
                if (ActiveServices.DEBUG_DELAYED_STARTS) {
                    Slog.v(ActiveServices.TAG_SERVICE, "No longer background starting: " + r);
                }
                rescheduleDelayedStarts();
            }
            if (this.mDelayedStartList.remove(r) && ActiveServices.DEBUG_DELAYED_STARTS) {
                Slog.v(ActiveServices.TAG_SERVICE, "No longer delaying start: " + r);
            }
        }

        void rescheduleDelayedStarts() {
            ServiceRecord r;
            removeMessages(1);
            long now = SystemClock.uptimeMillis();
            int i = 0;
            int N = this.mStartingBackground.size();
            while (i < N) {
                r = (ServiceRecord) this.mStartingBackground.get(i);
                if (r.startingBgTimeout <= now) {
                    if (ActivityManagerDebugConfig.DEBUG_AMS || ActivityManagerDebugConfig.DEBUG_SERVICE) {
                        Slog.i(ActiveServices.TAG, "Waited long enough for: " + r);
                    }
                    this.mStartingBackground.remove(i);
                    N--;
                    i--;
                }
                i++;
            }
            while (this.mDelayedStartList.size() > 0 && this.mStartingBackground.size() < ActiveServices.this.mMaxStartingBackground) {
                r = (ServiceRecord) this.mDelayedStartList.remove(0);
                if (ActiveServices.DEBUG_DELAYED_STARTS) {
                    Slog.v(ActiveServices.TAG_SERVICE, "REM FR DELAY LIST (exec next): " + r);
                }
                if (r.pendingStarts.size() <= 0) {
                    Slog.w(ActiveServices.TAG, "**** NO PENDING STARTS! " + r + " startReq=" + r.startRequested + " delayedStop=" + r.delayedStop);
                    r.delayed = false;
                } else {
                    if (ActiveServices.DEBUG_DELAYED_SERVICE && this.mDelayedStartList.size() > 0) {
                        Slog.v(ActiveServices.TAG_SERVICE, "Remaining delayed list:");
                        for (i = 0; i < this.mDelayedStartList.size(); i++) {
                            Slog.v(ActiveServices.TAG_SERVICE, "  #" + i + ": " + this.mDelayedStartList.get(i));
                        }
                    }
                    r.delayed = false;
                    try {
                        if (LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(SystemProperties.get("persist.runningbooster.support")) || LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(SystemProperties.get("ro.mtk_aws_support"))) {
                            ActiveServices.this.mBringUpReason = ActiveServices.BRING_UP_DELAYED_SERVICE;
                            ActiveServices.this.mCurrentCallerPackage = null;
                            ActiveServices.this.mCurrentCallerUid = -1;
                        }
                        ActiveServices.this.startServiceInnerLocked(this, ((StartItem) r.pendingStarts.get(0)).intent, r, false, true);
                        if (r.pendingStarts.size() > 0) {
                            ActiveServices.this.startServiceInnerLocked(this, ((StartItem) r.pendingStarts.get(0)).intent, r, false, true);
                        } else {
                            Slog.v(ActiveServices.TAG, "r.pendingStarts.size() is 0 ,r = " + r);
                        }
                    } catch (TransactionTooLargeException e) {
                    }
                }
            }
            if (this.mStartingBackground.size() > 0) {
                ServiceRecord next = (ServiceRecord) this.mStartingBackground.get(0);
                long when = next.startingBgTimeout > now ? next.startingBgTimeout : now;
                if (ActiveServices.DEBUG_DELAYED_SERVICE) {
                    Slog.v(ActiveServices.TAG_SERVICE, "Top bg start is " + next + ", can delay others up to " + when);
                }
                sendMessageAtTime(obtainMessage(1), when);
            }
            if (this.mStartingBackground.size() < ActiveServices.this.mMaxStartingBackground) {
                ActiveServices.this.mAm.backgroundServicesFinishedLocked(this.mUserId);
            }
        }
    }

    private class ServiceRestarter implements Runnable {
        private ServiceRecord mService;

        /* synthetic */ ServiceRestarter(ActiveServices this$0, ServiceRestarter serviceRestarter) {
            this();
        }

        private ServiceRestarter() {
        }

        void setService(ServiceRecord service) {
            this.mService = service;
        }

        public void run() {
            synchronized (ActiveServices.this.mAm) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    ActiveServices.this.performServiceRestartLocked(this.mService);
                } finally {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.am.ActiveServices.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.am.ActiveServices.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.am.ActiveServices.<clinit>():void");
    }

    public ActiveServices(ActivityManagerService service) {
        this.mCurRestartServiceName = null;
        this.mServiceMap = new SparseArray();
        this.mServiceConnections = new ArrayMap();
        this.mPendingServices = new ArrayList();
        this.mRestartingServices = new ArrayList();
        this.mDestroyingServices = new ArrayList();
        this.mTmpCollectionResults = null;
        this.mBringUpReason = IElsaManager.EMPTY_PACKAGE;
        this.mLastAnrDumpClearer = new Runnable() {
            public void run() {
                synchronized (ActiveServices.this.mAm) {
                    try {
                        ActivityManagerService.boostPriorityForLockedSection();
                        ActiveServices.this.mLastAnrDump = null;
                    } finally {
                        ActivityManagerService.resetPriorityAfterLockedSection();
                    }
                }
            }
        };
        this.mAm = service;
        int maxBg = 0;
        try {
            maxBg = Integer.parseInt(SystemProperties.get("ro.config.max_starting_bg", "0"));
        } catch (RuntimeException e) {
        }
        if (maxBg <= 0) {
            maxBg = ActivityManager.isLowRamDeviceStatic() ? 1 : 8;
        }
        this.mMaxStartingBackground = maxBg;
        this.mCtmFlag = LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(SystemProperties.get("ro.mtk_ctm_flag", "0"));
        this.mCtmName = SystemProperties.get("ro.ctm_name", "invalid");
    }

    ServiceRecord getServiceByName(ComponentName name, int callingUser) {
        if (ActivityManagerDebugConfig.DEBUG_MU) {
            Slog.v(TAG_MU, "getServiceByName(" + name + "), callingUser = " + callingUser);
        }
        return (ServiceRecord) getServiceMap(callingUser).mServicesByName.get(name);
    }

    boolean hasBackgroundServices(int callingUser) {
        ServiceMap smap = (ServiceMap) this.mServiceMap.get(callingUser);
        if (smap == null || smap.mStartingBackground.size() < this.mMaxStartingBackground) {
            return false;
        }
        return true;
    }

    private ServiceMap getServiceMap(int callingUser) {
        ServiceMap smap = (ServiceMap) this.mServiceMap.get(callingUser);
        if (smap != null) {
            return smap;
        }
        smap = new ServiceMap(this.mAm.mHandler.getLooper(), callingUser);
        this.mServiceMap.put(callingUser, smap);
        return smap;
    }

    ArrayMap<ComponentName, ServiceRecord> getServices(int callingUser) {
        return getServiceMap(callingUser).mServicesByName;
    }

    ComponentName startServiceLocked(IApplicationThread caller, Intent service, String resolvedType, int callingPid, int callingUid, String callingPackage, int userId) throws TransactionTooLargeException {
        boolean callerFg;
        if (DEBUG_DELAYED_STARTS) {
            Slog.v(TAG_SERVICE, "startService: " + service + " type=" + resolvedType + " args=" + service.getExtras());
        }
        ProcessRecord callerProcessRecord = null;
        if (caller != null) {
            ProcessRecord callerApp = this.mAm.getRecordForAppLocked(caller);
            if (!ActivityManagerService.IS_USER_BUILD || ActivityManagerDebugConfig.DEBUG_SERVICE) {
                Slog.d(TAG_SERVICE, "SVC-startService: " + service + " callerApp=" + callerApp);
            }
            if (callerApp == null) {
                throw new SecurityException("Unable to find app for caller " + caller + " (pid=" + Binder.getCallingPid() + ") when starting service " + service);
            }
            callerFg = callerApp.setSchedGroup != 0;
            if (OppoAppStartupManager.getInstance().handleStartOrBindService(service, callerApp)) {
                return null;
            }
            callerProcessRecord = callerApp;
        } else {
            callerFg = true;
        }
        ServiceLookupResult res = retrieveServiceLocked(service, resolvedType, callingPackage, callingPid, callingUid, userId, true, callerFg, false);
        if (res == null) {
            return null;
        }
        if (res.record == null) {
            return new ComponentName("!", res.permission != null ? res.permission : "private to package");
        }
        ServiceRecord r = res.record;
        if (r.appInfo != null && OppoAppStartupManager.getInstance().isAppStartForbidden(r.appInfo.packageName)) {
            return null;
        }
        if (r.app == null && service != null && r.processName != null && r.appInfo != null && this.mAm.getProcessRecordLocked(r.processName, r.appInfo.uid, false) == null && !OppoAppStartupManager.getInstance().isAllowStartFromStartService(callerProcessRecord, callingPid, callingUid, r, service)) {
            return r.name;
        }
        OppoAppStartupManager.getInstance().handleProcessStartupInfo(callingPid, callingUid, callerProcessRecord, service, r.appInfo, "startservice");
        if (this.mAm.mUserController.exists(r.userId)) {
            if (!r.startRequested) {
                long token = Binder.clearCallingIdentity();
                try {
                    if (this.mAm.checkAllowBackgroundLocked(r.appInfo.uid, r.packageName, callingPid, true) != 0) {
                        Slog.w(TAG, "Background start not allowed: service " + service + " to " + r.name.flattenToShortString() + " from pid=" + callingPid + " uid=" + callingUid + " pkg=" + callingPackage);
                        return null;
                    }
                    Binder.restoreCallingIdentity(token);
                } finally {
                    Binder.restoreCallingIdentity(token);
                }
            }
            NeededUriGrants neededGrants = this.mAm.checkGrantUriPermissionFromIntentLocked(callingUid, r.packageName, service, service.getFlags(), null, r.userId);
            if (Build.isPermissionReviewRequired() && !requestStartTargetPermissionsReviewIfNeededLocked(r, callingPackage, callingUid, service, callerFg, userId)) {
                return null;
            }
            if (unscheduleServiceRestartLocked(r, callingUid, false) && ActivityManagerDebugConfig.DEBUG_SERVICE) {
                Slog.v(TAG_SERVICE, "START SERVICE WHILE RESTART PENDING: " + r);
            }
            r.lastActivity = SystemClock.uptimeMillis();
            r.startRequested = true;
            r.delayedStop = false;
            r.pendingStarts.add(new StartItem(r, false, r.makeNextStartId(), service, neededGrants));
            ServiceMap smap = getServiceMap(r.userId);
            boolean addToStarting = false;
            if (!callerFg && r.app == null && this.mAm.mUserController.hasStartedUserState(r.userId)) {
                ProcessRecord proc = this.mAm.getProcessRecordLocked(r.processName, r.appInfo.uid, false);
                if (proc == null || proc.curProcState > 11) {
                    if (DEBUG_DELAYED_SERVICE) {
                        Slog.v(TAG_SERVICE, "Potential start delay of " + r + " in " + proc);
                    }
                    if (r.delayed) {
                        if (DEBUG_DELAYED_STARTS) {
                            Slog.v(TAG_SERVICE, "Continuing to delay: " + r);
                        }
                        return r.name;
                    } else if (smap.mStartingBackground.size() >= this.mMaxStartingBackground) {
                        if (ActivityManagerDebugConfig.DEBUG_AMS || ActivityManagerDebugConfig.DEBUG_SERVICE) {
                            Slog.i(TAG_SERVICE, "Delaying start of: " + r);
                        }
                        smap.mDelayedStartList.add(r);
                        r.delayed = true;
                        if (LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(SystemProperties.get("persist.runningbooster.support")) || LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(SystemProperties.get("ro.mtk_aws_support"))) {
                            if (r.delayedServiceCallerPkg == null) {
                                r.delayedServiceCallerPkg = new ArrayList();
                            }
                            r.delayedServiceCallerPkg.add(callingPackage);
                            if (r.delayedServiceCallerUid == null) {
                                r.delayedServiceCallerUid = new ArrayList();
                            }
                            r.delayedServiceCallerUid.add(Integer.valueOf(callingUid));
                        }
                        return r.name;
                    } else {
                        if (DEBUG_DELAYED_STARTS) {
                            Slog.v(TAG_SERVICE, "Not delaying: " + r);
                        }
                        addToStarting = true;
                    }
                } else if (proc.curProcState >= 10) {
                    addToStarting = true;
                    if (DEBUG_DELAYED_STARTS) {
                        Slog.v(TAG_SERVICE, "Not delaying, but counting as bg: " + r);
                    }
                } else if (DEBUG_DELAYED_STARTS) {
                    StringBuilder stringBuilder = new StringBuilder(128);
                    stringBuilder.append("Not potential delay (state=").append(proc.curProcState).append(' ').append(proc.adjType);
                    String reason = proc.makeAdjReason();
                    if (reason != null) {
                        stringBuilder.append(' ');
                        stringBuilder.append(reason);
                    }
                    stringBuilder.append("): ");
                    stringBuilder.append(r.toString());
                    Slog.v(TAG_SERVICE, stringBuilder.toString());
                }
            } else if (DEBUG_DELAYED_STARTS) {
                if (callerFg) {
                    Slog.v(TAG_SERVICE, "Not potential delay (callerFg=" + callerFg + " uid=" + callingUid + " pid=" + callingPid + "): " + r);
                } else if (r.app != null) {
                    Slog.v(TAG_SERVICE, "Not potential delay (cur app=" + r.app + "): " + r);
                } else {
                    Slog.v(TAG_SERVICE, "Not potential delay (user " + r.userId + " not started): " + r);
                }
            }
            if (LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(SystemProperties.get("persist.runningbooster.support")) || LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(SystemProperties.get("ro.mtk_aws_support"))) {
                this.mBringUpReason = BRING_UP_START_SERVICE;
                this.mCurrentCallerPackage = callingPackage;
                this.mCurrentCallerUid = callingUid;
            }
            return startServiceInnerLocked(smap, service, r, callerFg, addToStarting);
        }
        Slog.w(TAG, "Trying to start service with non-existent user! " + r.userId);
        return null;
    }

    private boolean requestStartTargetPermissionsReviewIfNeededLocked(ServiceRecord r, String callingPackage, int callingUid, Intent service, boolean callerFg, int userId) {
        if (!this.mAm.getPackageManagerInternalLocked().isPermissionsReviewRequired(r.packageName, r.userId)) {
            return true;
        }
        if (callerFg) {
            ActivityManagerService activityManagerService = this.mAm;
            Intent[] intentArr = new Intent[1];
            intentArr[0] = service;
            String[] strArr = new String[1];
            strArr[0] = service.resolveType(this.mAm.mContext.getContentResolver());
            IIntentSender target = activityManagerService.getIntentSenderLocked(4, callingPackage, callingUid, userId, null, null, 0, intentArr, strArr, 1409286144, null);
            final Intent intent = new Intent("android.intent.action.REVIEW_PERMISSIONS");
            intent.addFlags(276824064);
            intent.putExtra("android.intent.extra.PACKAGE_NAME", r.packageName);
            intent.putExtra("android.intent.extra.INTENT", new IntentSender(target));
            if (ActivityManagerDebugConfig.DEBUG_PERMISSIONS_REVIEW || !ActivityManagerService.IS_USER_BUILD) {
                Slog.i(TAG, "u" + r.userId + " Launching permission review for package " + r.packageName);
            }
            final int i = userId;
            this.mAm.mHandler.post(new Runnable() {
                public void run() {
                    ActiveServices.this.mAm.mContext.startActivityAsUser(intent, new UserHandle(i));
                }
            });
            return false;
        }
        Slog.w(TAG, "u" + r.userId + " Starting a service in package" + r.packageName + " requires a permissions review");
        return false;
    }

    ComponentName startServiceInnerLocked(ServiceMap smap, Intent service, ServiceRecord r, boolean callerFg, boolean addToStarting) throws TransactionTooLargeException {
        ServiceState stracker = r.getTracker();
        if (stracker != null) {
            stracker.setStarted(true, this.mAm.mProcessStats.getMemFactorLocked(), r.lastActivity);
        }
        r.callStart = false;
        synchronized (r.stats.getBatteryStats()) {
            r.stats.startRunningLocked();
        }
        String error = bringUpServiceLocked(r, service.getFlags(), callerFg, false, false);
        if (error != null) {
            return new ComponentName("!!", error);
        }
        if (r.startRequested && addToStarting) {
            boolean first = smap.mStartingBackground.size() == 0;
            smap.mStartingBackground.add(r);
            r.startingBgTimeout = SystemClock.uptimeMillis() + 15000;
            if (DEBUG_DELAYED_SERVICE) {
                RuntimeException here = new RuntimeException("here");
                here.fillInStackTrace();
                Slog.v(TAG_SERVICE, "Starting background (first=" + first + "): " + r, here);
            } else if (DEBUG_DELAYED_STARTS) {
                Slog.v(TAG_SERVICE, "Starting background (first=" + first + "): " + r);
            }
            if (first) {
                smap.rescheduleDelayedStarts();
            }
        } else if (callerFg) {
            smap.ensureNotStartingBackground(r);
        }
        return r.name;
    }

    private void stopServiceLocked(ServiceRecord service) {
        if (service.delayed) {
            if (DEBUG_DELAYED_STARTS) {
                Slog.v(TAG_SERVICE, "Delaying stop of pending: " + service);
            }
            service.delayedStop = true;
            return;
        }
        synchronized (service.stats.getBatteryStats()) {
            service.stats.stopRunningLocked();
        }
        service.startRequested = false;
        if (service.tracker != null) {
            service.tracker.setStarted(false, this.mAm.mProcessStats.getMemFactorLocked(), SystemClock.uptimeMillis());
        }
        service.callStart = false;
        bringDownServiceIfNeededLocked(service, false, false);
    }

    int stopServiceLocked(IApplicationThread caller, Intent service, String resolvedType, int userId) {
        if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
            Slog.v(TAG_SERVICE, "stopService: " + service + " type=" + resolvedType);
        }
        ProcessRecord callerApp = this.mAm.getRecordForAppLocked(caller);
        if (caller == null || callerApp != null) {
            ServiceLookupResult r = retrieveServiceLocked(service, resolvedType, null, Binder.getCallingPid(), Binder.getCallingUid(), userId, false, false, false);
            if (r == null) {
                return 0;
            }
            if (r.record == null) {
                return -1;
            }
            long origId = Binder.clearCallingIdentity();
            try {
                if (!ActivityManagerService.IS_USER_BUILD || ActivityManagerDebugConfig.DEBUG_SERVICE) {
                    Slog.d(TAG_SERVICE, "SVC-Stopping service: " + r.record + ", app=" + callerApp);
                }
                stopServiceLocked(r.record);
                return 1;
            } finally {
                Binder.restoreCallingIdentity(origId);
            }
        } else {
            throw new SecurityException("Unable to find app for caller " + caller + " (pid=" + Binder.getCallingPid() + ") when stopping service " + service);
        }
    }

    void stopInBackgroundLocked(int uid) {
        ServiceMap services = (ServiceMap) this.mServiceMap.get(UserHandle.getUserId(uid));
        ArrayList stopping = null;
        if (services != null) {
            int i;
            ServiceRecord service;
            for (i = services.mServicesByName.size() - 1; i >= 0; i--) {
                service = (ServiceRecord) services.mServicesByName.valueAt(i);
                if (service.appInfo.uid == uid && service.startRequested && this.mAm.mAppOpsService.noteOperation(63, uid, service.packageName) != 0 && stopping == null) {
                    stopping = new ArrayList();
                    stopping.add(service);
                }
            }
            if (stopping != null) {
                for (i = stopping.size() - 1; i >= 0; i--) {
                    service = (ServiceRecord) stopping.get(i);
                    service.delayed = false;
                    services.ensureNotStartingBackground(service);
                    stopServiceLocked(service);
                }
            }
        }
    }

    IBinder peekServiceLocked(Intent service, String resolvedType, String callingPackage) {
        ServiceLookupResult r = retrieveServiceLocked(service, resolvedType, callingPackage, Binder.getCallingPid(), Binder.getCallingUid(), UserHandle.getCallingUserId(), false, false, false);
        if (r == null) {
            return null;
        }
        if (r.record == null) {
            throw new SecurityException("Permission Denial: Accessing service from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " requires " + r.permission);
        }
        IntentBindRecord ib = (IntentBindRecord) r.record.bindings.get(r.record.intent);
        if (ib != null) {
            return ib.binder;
        }
        return null;
    }

    boolean stopServiceTokenLocked(ComponentName className, IBinder token, int startId) {
        if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
            Slog.v(TAG_SERVICE, "stopServiceToken: " + className + " " + token + " startId=" + startId);
        }
        ServiceRecord r = findServiceLocked(className, token, UserHandle.getCallingUserId());
        if (r == null) {
            return false;
        }
        if (startId >= 0) {
            StartItem si = r.findDeliveredStart(startId, false);
            if (si != null) {
                while (r.deliveredStarts.size() > 0) {
                    StartItem cur = (StartItem) r.deliveredStarts.remove(0);
                    cur.removeUriPermissionsLocked();
                    if (cur == si) {
                        break;
                    }
                }
            }
            if (r.getLastStartId() != startId) {
                return false;
            }
            if (r.deliveredStarts.size() > 0) {
                Slog.w(TAG, "stopServiceToken startId " + startId + " is last, but have " + r.deliveredStarts.size() + " remaining args");
            }
        }
        synchronized (r.stats.getBatteryStats()) {
            r.stats.stopRunningLocked();
        }
        r.startRequested = false;
        if (r.tracker != null) {
            r.tracker.setStarted(false, this.mAm.mProcessStats.getMemFactorLocked(), SystemClock.uptimeMillis());
        }
        r.callStart = false;
        long origId = Binder.clearCallingIdentity();
        bringDownServiceIfNeededLocked(r, false, false);
        Binder.restoreCallingIdentity(origId);
        return true;
    }

    public void setServiceForegroundLocked(ComponentName className, IBinder token, int id, Notification notification, int flags) {
        int userId = UserHandle.getCallingUserId();
        long origId = Binder.clearCallingIdentity();
        try {
            ServiceRecord r = findServiceLocked(className, token, userId);
            if (r != null) {
                if (id == 0) {
                    if (r.isForeground) {
                        r.isForeground = false;
                        if (r.app != null) {
                            ProcessRecord proc = r.app;
                            for (int i = proc.services.size() - 1; i >= 0; i--) {
                                ServiceRecord sr = (ServiceRecord) proc.services.valueAt(i);
                                if (sr.isForeground && sr.foregroundId == r.foregroundId) {
                                    sr.isForeground = false;
                                    String str = TAG;
                                    Object[] objArr = new Object[2];
                                    objArr[0] = sr;
                                    objArr[1] = Integer.valueOf(sr.foregroundId);
                                    Slog.d(str, String.format("%s(foregroundId=%d) not foreground anymore", objArr));
                                }
                            }
                            this.mAm.updateLruProcessLocked(r.app, false, null);
                            updateServiceForegroundLocked(r.app, true);
                        }
                    }
                    if ((flags & 1) != 0) {
                        r.cancelNotification();
                        r.foregroundId = 0;
                        r.foregroundNoti = null;
                    } else if (r.appInfo.targetSdkVersion >= 21) {
                        r.stripForegroundServiceFlagFromNotification();
                        if ((flags & 2) != 0) {
                            r.foregroundId = 0;
                            r.foregroundNoti = null;
                        }
                    }
                } else if (notification == null) {
                    throw new IllegalArgumentException("null notification");
                } else {
                    String pkg = IElsaManager.EMPTY_PACKAGE;
                    if (r.packageName != null) {
                        pkg = r.packageName;
                    }
                    if (notification.icon != 0 || OppoListManager.getInstance().getNotificationServiceList().contains(pkg)) {
                        if (r.foregroundId != id) {
                            cancelForegroudNotificationLocked(r);
                            r.foregroundId = id;
                        }
                        notification.flags |= 64;
                        r.foregroundNoti = notification;
                        r.isForeground = true;
                        r.postNotification();
                        if (r.app != null) {
                            updateServiceForegroundLocked(r.app, true);
                        }
                        getServiceMap(r.userId).ensureNotStartingBackground(r);
                        this.mAm.notifyPackageUse(r.serviceInfo.packageName, 2);
                    } else {
                        if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                            Slog.d(TAG, "forbid third app show useless foreground notification to upgrade service adj");
                        }
                        Binder.restoreCallingIdentity(origId);
                        return;
                    }
                }
            }
            Binder.restoreCallingIdentity(origId);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(origId);
        }
    }

    private void cancelForegroudNotificationLocked(ServiceRecord r) {
        if (r.foregroundId != 0) {
            ServiceMap sm = getServiceMap(r.userId);
            if (sm != null) {
                int i = sm.mServicesByName.size() - 1;
                while (i >= 0) {
                    ServiceRecord other = (ServiceRecord) sm.mServicesByName.valueAt(i);
                    if (other == r || other.foregroundId != r.foregroundId || !other.packageName.equals(r.packageName)) {
                        i--;
                    } else {
                        return;
                    }
                }
            }
            r.cancelNotification();
        }
    }

    private void updateServiceForegroundLocked(ProcessRecord proc, boolean oomAdj) {
        boolean anyForeground = false;
        for (int i = proc.services.size() - 1; i >= 0; i--) {
            if (((ServiceRecord) proc.services.valueAt(i)).isForeground) {
                anyForeground = true;
                break;
            }
        }
        this.mAm.updateProcessForegroundLocked(proc, anyForeground, oomAdj);
    }

    private void updateWhitelistManagerLocked(ProcessRecord proc) {
        proc.whitelistManager = false;
        for (int i = proc.services.size() - 1; i >= 0; i--) {
            if (((ServiceRecord) proc.services.valueAt(i)).whitelistManager) {
                proc.whitelistManager = true;
                return;
            }
        }
    }

    public void updateServiceConnectionActivitiesLocked(ProcessRecord clientProc) {
        ArraySet updatedProcesses = null;
        for (int i = 0; i < clientProc.connections.size(); i++) {
            ProcessRecord proc = ((ConnectionRecord) clientProc.connections.valueAt(i)).binding.service.app;
            if (!(proc == null || proc == clientProc)) {
                if (updatedProcesses == null) {
                    updatedProcesses = new ArraySet();
                } else if (updatedProcesses.contains(proc)) {
                }
                updatedProcesses.add(proc);
                updateServiceClientActivitiesLocked(proc, null, false);
            }
        }
    }

    private boolean updateServiceClientActivitiesLocked(ProcessRecord proc, ConnectionRecord modCr, boolean updateLru) {
        if (modCr != null && modCr.binding.client != null && modCr.binding.client.activities.size() <= 0) {
            return false;
        }
        boolean anyClientActivities = false;
        for (int i = proc.services.size() - 1; i >= 0 && !anyClientActivities; i--) {
            ServiceRecord sr = (ServiceRecord) proc.services.valueAt(i);
            for (int conni = sr.connections.size() - 1; conni >= 0 && !anyClientActivities; conni--) {
                ArrayList<ConnectionRecord> clist = (ArrayList) sr.connections.valueAt(conni);
                for (int cri = clist.size() - 1; cri >= 0; cri--) {
                    ConnectionRecord cr = (ConnectionRecord) clist.get(cri);
                    if (cr.binding.client != null && cr.binding.client != proc && cr.binding.client.activities.size() > 0) {
                        anyClientActivities = true;
                        break;
                    }
                }
            }
        }
        if (anyClientActivities == proc.hasClientActivities) {
            return false;
        }
        proc.hasClientActivities = anyClientActivities;
        if (updateLru) {
            this.mAm.updateLruProcessLocked(proc, anyClientActivities, null);
        }
        return true;
    }

    int bindServiceLocked(IApplicationThread caller, IBinder token, Intent service, String resolvedType, IServiceConnection connection, int flags, String callingPackage, int userId) throws TransactionTooLargeException {
        ProcessRecord callerApp = this.mAm.getRecordForAppLocked(caller);
        if (!ActivityManagerService.IS_USER_BUILD || ActivityManagerDebugConfig.DEBUG_SERVICE) {
            Slog.v(TAG_SERVICE, "bindService: " + service + " type=" + resolvedType + " conn=" + connection.asBinder() + " flags=0x" + Integer.toHexString(flags) + " callerApp=" + callerApp);
        }
        if (callerApp == null) {
            throw new SecurityException("Unable to find app for caller " + caller + " (pid=" + Binder.getCallingPid() + ") when binding service " + service);
        } else if (OppoAppStartupManager.getInstance().handleStartOrBindService(service, callerApp)) {
            return 0;
        } else {
            ActivityRecord activity = null;
            if (token != null) {
                activity = ActivityRecord.isInStackLocked(token);
                if (activity == null) {
                    Slog.w(TAG, "Binding with unknown activity: " + token);
                    return 0;
                }
            }
            int clientLabel = 0;
            PendingIntent clientIntent = null;
            boolean bindFromJob = false;
            boolean isCallerSystem = callerApp.info.uid == 1000;
            if (isCallerSystem) {
                service.setDefusable(true);
                clientIntent = (PendingIntent) service.getParcelableExtra("android.intent.extra.client_intent");
                bindFromJob = service.getBooleanExtra("BINDSERVICE_FROM_JOB", false);
                if (clientIntent != null) {
                    clientLabel = service.getIntExtra("android.intent.extra.client_label", 0);
                    if (clientLabel != 0) {
                        service = service.cloneFilter();
                    }
                }
            }
            if ((134217728 & flags) != 0) {
                this.mAm.enforceCallingPermission("android.permission.MANAGE_ACTIVITY_STACKS", "BIND_TREAT_LIKE_ACTIVITY");
            }
            if ((16777216 & flags) == 0 || isCallerSystem) {
                boolean callerFg = callerApp.setSchedGroup != 0;
                ServiceLookupResult res = retrieveServiceLocked(service, resolvedType, callingPackage, Binder.getCallingPid(), Binder.getCallingUid(), userId, true, callerFg, (Integer.MIN_VALUE & flags) != 0);
                if (res == null) {
                    return 0;
                }
                if (res.record == null) {
                    return -1;
                }
                ServiceRecord s = res.record;
                if (s.appInfo != null && OppoAppStartupManager.getInstance().isAppStartForbidden(s.appInfo.packageName)) {
                    return 0;
                }
                if (s.app == null && callerApp != null && s.processName != null && s.appInfo != null && (callerApp.info.flags & 1) == 0 && this.mAm.getProcessRecordLocked(s.processName, s.appInfo.uid, false) == null) {
                    if (!OppoAppStartupManager.getInstance().isAllowStartFromBindService(callerApp, null, callerApp.info.uid, s, service, "bs")) {
                        return 0;
                    }
                }
                if (s.app == null && callerApp.info.uid == 1000 && s.processName != null && s.appInfo != null && this.mAm.getProcessRecordLocked(s.processName, s.appInfo.uid, false) == null) {
                    if (bindFromJob) {
                        if (!OppoAppStartupManager.getInstance().isAllowStartFromBindService(null, "system[jobScheduler]", callerApp.info.uid, s, service, "bsfj")) {
                            return 0;
                        }
                    }
                    OppoAppStartupManager.getInstance().collectAppStartBySystemUI(callerApp, s);
                }
                OppoAppStartupManager.getInstance().handleProcessStartupInfo(0, callerApp.uid, callerApp, service, s.appInfo, "bindservice");
                boolean permissionsReviewRequired = false;
                if (Build.isPermissionReviewRequired() && this.mAm.getPackageManagerInternalLocked().isPermissionsReviewRequired(s.packageName, s.userId)) {
                    permissionsReviewRequired = true;
                    if (callerFg) {
                        ServiceRecord serviceRecord = s;
                        final Intent serviceIntent = service;
                        String currentCallingPackage = callingPackage;
                        final ServiceRecord serviceRecord2 = s;
                        final String str = callingPackage;
                        final ProcessRecord processRecord = callerApp;
                        final boolean z = callerFg;
                        final IServiceConnection iServiceConnection = connection;
                        RemoteCallback remoteCallback = new RemoteCallback(new OnResultListener() {
                            public void onResult(Bundle result) {
                                synchronized (ActiveServices.this.mAm) {
                                    long identity;
                                    try {
                                        ActivityManagerService.boostPriorityForLockedSection();
                                        identity = Binder.clearCallingIdentity();
                                        if (ActiveServices.this.mPendingServices.contains(serviceRecord2)) {
                                            if (ActiveServices.this.mAm.getPackageManagerInternalLocked().isPermissionsReviewRequired(serviceRecord2.packageName, serviceRecord2.userId)) {
                                                ActiveServices.this.unbindServiceLocked(iServiceConnection);
                                            } else {
                                                try {
                                                    if (LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(SystemProperties.get("persist.runningbooster.support")) || LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(SystemProperties.get("ro.mtk_aws_support"))) {
                                                        ActiveServices.this.mBringUpReason = ActiveServices.BRING_UP_BIND_SERVICE;
                                                        ActiveServices.this.mCurrentCallerPackage = str;
                                                        ActiveServices.this.mCurrentCallerUid = processRecord.info.uid;
                                                    }
                                                    ActiveServices.this.bringUpServiceLocked(serviceRecord2, serviceIntent.getFlags(), z, false, false);
                                                } catch (RemoteException e) {
                                                }
                                            }
                                            Binder.restoreCallingIdentity(identity);
                                            ActivityManagerService.resetPriorityAfterLockedSection();
                                            return;
                                        }
                                        Binder.restoreCallingIdentity(identity);
                                        ActivityManagerService.resetPriorityAfterLockedSection();
                                    } catch (Throwable th) {
                                        ActivityManagerService.resetPriorityAfterLockedSection();
                                    }
                                }
                            }
                        });
                        Intent intent = new Intent("android.intent.action.REVIEW_PERMISSIONS");
                        intent.addFlags(276824064);
                        intent.putExtra("android.intent.extra.PACKAGE_NAME", s.packageName);
                        intent.putExtra("android.intent.extra.REMOTE_CALLBACK", remoteCallback);
                        if (ActivityManagerDebugConfig.DEBUG_PERMISSIONS_REVIEW || !ActivityManagerService.IS_USER_BUILD) {
                            Slog.i(TAG, "u" + s.userId + " Launching permission review for package " + s.packageName);
                        }
                        final Intent intent2 = intent;
                        final int i = userId;
                        this.mAm.mHandler.post(new Runnable() {
                            public void run() {
                                ActiveServices.this.mAm.mContext.startActivityAsUser(intent2, new UserHandle(i));
                            }
                        });
                    } else {
                        Slog.w(TAG, "u" + s.userId + " Binding to a service in package" + s.packageName + " requires a permissions review");
                        return 0;
                    }
                }
                long origId = Binder.clearCallingIdentity();
                if (unscheduleServiceRestartLocked(s, callerApp.info.uid, false) && ActivityManagerDebugConfig.DEBUG_SERVICE) {
                    Slog.v(TAG_SERVICE, "BIND SERVICE WHILE RESTART PENDING: " + s);
                }
                if ((flags & 1) != 0) {
                    s.lastActivity = SystemClock.uptimeMillis();
                    if (!s.hasAutoCreateConnections()) {
                        ServiceState stracker = s.getTracker();
                        if (stracker != null) {
                            stracker.setBound(true, this.mAm.mProcessStats.getMemFactorLocked(), s.lastActivity);
                        }
                    }
                }
                this.mAm.startAssociationLocked(callerApp.uid, callerApp.processName, callerApp.curProcState, s.appInfo.uid, s.name, s.processName);
                AppBindRecord b = s.retrieveAppBindingLocked(service, callerApp);
                ConnectionRecord c = new ConnectionRecord(b, activity, connection, flags, clientLabel, clientIntent);
                IBinder binder = connection.asBinder();
                ArrayList<ConnectionRecord> clist = (ArrayList) s.connections.get(binder);
                if (clist == null) {
                    clist = new ArrayList();
                    s.connections.put(binder, clist);
                }
                clist.add(c);
                try {
                    b.connections.add(c);
                } catch (Throwable e) {
                    Slog.wtf(TAG, "WTF:add ConnectionRecord failed!", e);
                    b.logOutIntentBindWithTypeInfo();
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(origId);
                }
                if (activity != null) {
                    if (activity.connections == null) {
                        activity.connections = new HashSet();
                    }
                    activity.connections.add(c);
                }
                b.client.connections.add(c);
                if ((c.flags & 8) != 0) {
                    b.client.hasAboveClient = true;
                }
                if ((c.flags & 16777216) != 0) {
                    s.whitelistManager = true;
                }
                if (s.app != null) {
                    updateServiceClientActivitiesLocked(s.app, c, true);
                }
                clist = (ArrayList) this.mServiceConnections.get(binder);
                if (clist == null) {
                    clist = new ArrayList();
                    this.mServiceConnections.put(binder, clist);
                }
                clist.add(c);
                if ((flags & 1) != 0) {
                    s.lastActivity = SystemClock.uptimeMillis();
                    if (LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(SystemProperties.get("persist.runningbooster.support")) || LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(SystemProperties.get("ro.mtk_aws_support"))) {
                        this.mBringUpReason = BRING_UP_BIND_SERVICE;
                        this.mCurrentCallerPackage = callingPackage;
                        this.mCurrentCallerUid = callerApp.info.uid;
                    }
                    if (bringUpServiceLocked(s, service.getFlags(), callerFg, false, permissionsReviewRequired) != null) {
                        Binder.restoreCallingIdentity(origId);
                        return 0;
                    }
                }
                if (s.app != null) {
                    boolean z2;
                    if (this.mAm.mWallpaperClassName != null && s.name.equals(this.mAm.mWallpaperClassName)) {
                        this.mAm.mWallpaperProcess = s.app;
                    }
                    if ((134217728 & flags) != 0) {
                        s.app.treatLikeActivity = true;
                    }
                    if (s.whitelistManager) {
                        s.app.whitelistManager = true;
                    }
                    ActivityManagerService activityManagerService = this.mAm;
                    ProcessRecord processRecord2 = s.app;
                    if (s.app.hasClientActivities) {
                        z2 = true;
                    } else {
                        z2 = s.app.treatLikeActivity;
                    }
                    activityManagerService.updateLruProcessLocked(processRecord2, z2, b.client);
                    this.mAm.updateOomAdjLocked(s.app);
                }
                if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                    Slog.v(TAG_SERVICE, "Bind " + s + " with " + b + ": received=" + b.intent.received + " apps=" + b.intent.apps.size() + " doRebind=" + b.intent.doRebind);
                }
                if (!ActivityManagerService.IS_USER_BUILD || ActivityManagerDebugConfig.DEBUG_SERVICE) {
                    Slog.d(TAG_SERVICE, "SVC-Binding service: " + s + ", app=" + s.app + ", activity=" + activity);
                }
                if (s.app != null && b.intent.received) {
                    try {
                        c.conn.connected(s.name, b.intent.binder);
                    } catch (Throwable e2) {
                        Slog.w(TAG, "Failure sending service " + s.shortName + " to connection " + c.conn.asBinder() + " (in " + c.binding.client.processName + ")", e2);
                    }
                    if (b.intent.apps.size() == 1 && b.intent.doRebind) {
                        requestServiceBindingLocked(s, b.intent, callerFg, true);
                    }
                } else if (!b.intent.requested) {
                    requestServiceBindingLocked(s, b.intent, callerFg, false);
                }
                getServiceMap(s.userId).ensureNotStartingBackground(s);
                Binder.restoreCallingIdentity(origId);
                return 1;
            }
            throw new SecurityException("Non-system caller " + caller + " (pid=" + Binder.getCallingPid() + ") set BIND_ALLOW_WHITELIST_MANAGEMENT when binding service " + service);
        }
    }

    private void foo() {
    }

    void publishServiceLocked(ServiceRecord r, Intent intent, IBinder service) {
        long origId = Binder.clearCallingIdentity();
        ConnectionRecord c;
        try {
            if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                Slog.v(TAG_SERVICE, "PUBLISHING " + r + " " + intent + ": " + service);
            }
            if (r != null) {
                FilterComparison filter = new FilterComparison(intent);
                IntentBindRecord b = (IntentBindRecord) r.bindings.get(filter);
                if (!(b == null || b.received)) {
                    b.binder = service;
                    b.requested = true;
                    b.received = true;
                    for (int conni = r.connections.size() - 1; conni >= 0; conni--) {
                        ArrayList<ConnectionRecord> clist = (ArrayList) r.connections.valueAt(conni);
                        for (int i = 0; i < clist.size(); i++) {
                            c = (ConnectionRecord) clist.get(i);
                            if (filter.equals(c.binding.intent.intent)) {
                                if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                                    Slog.v(TAG_SERVICE, "Publishing to: " + c);
                                }
                                c.conn.connected(r.name, service);
                            } else {
                                if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                                    Slog.v(TAG_SERVICE, "Not publishing to: " + c);
                                }
                                if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                                    Slog.v(TAG_SERVICE, "Bound intent: " + c.binding.intent.intent);
                                }
                                if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                                    Slog.v(TAG_SERVICE, "Published intent: " + intent);
                                }
                            }
                        }
                    }
                }
                serviceDoneExecutingLocked(r, this.mDestroyingServices.contains(r), false);
            }
            Binder.restoreCallingIdentity(origId);
        } catch (Exception e) {
            Slog.w(TAG, "Failure sending service " + r.name + " to connection " + c.conn.asBinder() + " (in " + c.binding.client.processName + ")", e);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(origId);
        }
    }

    boolean unbindServiceLocked(IServiceConnection connection) {
        IBinder binder = connection.asBinder();
        if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
            Slog.v(TAG_SERVICE, "unbindService: conn=" + binder);
        }
        ArrayList<ConnectionRecord> clist = (ArrayList) this.mServiceConnections.get(binder);
        if (clist == null) {
            Slog.w(TAG, "Unbind failed: could not find connection for " + connection.asBinder());
            return false;
        }
        long origId = Binder.clearCallingIdentity();
        while (clist.size() > 0) {
            try {
                ConnectionRecord r = (ConnectionRecord) clist.get(0);
                if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                    Slog.d(TAG_SERVICE, "SVC-Unbinding service: " + r.binding.service + ", app=" + r.binding.service.app);
                }
                removeConnectionLocked(r, null, null);
                if (clist.size() > 0 && clist.get(0) == r) {
                    Slog.wtf(TAG, "Connection " + r + " not removed for binder " + binder);
                    clist.remove(0);
                }
                if (r.binding.service.app != null) {
                    if (r.binding.service.app.whitelistManager) {
                        updateWhitelistManagerLocked(r.binding.service.app);
                    }
                    if ((r.flags & 134217728) != 0) {
                        boolean z;
                        r.binding.service.app.treatLikeActivity = true;
                        ActivityManagerService activityManagerService = this.mAm;
                        ProcessRecord processRecord = r.binding.service.app;
                        if (r.binding.service.app.hasClientActivities) {
                            z = true;
                        } else {
                            z = r.binding.service.app.treatLikeActivity;
                        }
                        activityManagerService.updateLruProcessLocked(processRecord, z, null);
                    }
                    this.mAm.updateOomAdjLocked(r.binding.service.app);
                }
            } finally {
                Binder.restoreCallingIdentity(origId);
            }
        }
        return true;
    }

    void unbindFinishedLocked(ServiceRecord r, Intent intent, boolean doRebind) {
        long origId = Binder.clearCallingIdentity();
        if (r != null) {
            try {
                IntentBindRecord b = (IntentBindRecord) r.bindings.get(new FilterComparison(intent));
                if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                    Slog.v(TAG_SERVICE, "unbindFinished in " + r + " at " + b + ": apps=" + (b != null ? b.apps.size() : 0));
                }
                boolean inDestroying = this.mDestroyingServices.contains(r);
                if (b != null) {
                    if (b.apps.size() <= 0 || inDestroying) {
                        b.doRebind = true;
                    } else {
                        boolean inFg = false;
                        for (int i = b.apps.size() - 1; i >= 0; i--) {
                            ProcessRecord client = ((AppBindRecord) b.apps.valueAt(i)).client;
                            if (client != null && client.setSchedGroup != 0) {
                                inFg = true;
                                break;
                            }
                        }
                        try {
                            requestServiceBindingLocked(r, b, inFg, true);
                        } catch (TransactionTooLargeException e) {
                        }
                    }
                }
                serviceDoneExecutingLocked(r, inDestroying, false);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(origId);
            }
        }
        Binder.restoreCallingIdentity(origId);
    }

    private final ServiceRecord findServiceLocked(ComponentName name, IBinder token, int userId) {
        IBinder r = getServiceByName(name, userId);
        return r == token ? r : null;
    }

    private ServiceLookupResult retrieveServiceLocked(Intent service, String resolvedType, String callingPackage, int callingPid, int callingUid, int userId, boolean createIfNeeded, boolean callingFromFg, boolean isBindExternal) {
        ServiceRecord r;
        if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
            Slog.v(TAG_SERVICE, "retrieveServiceLocked: " + service + " type=" + resolvedType + " callingUid=" + callingUid);
        }
        userId = this.mAm.mUserController.handleIncomingUser(callingPid, callingUid, userId, false, 1, OppoProcessManager.RESUME_REASON_SERVICE_STR, null);
        if (userId == 999) {
            String pName = null;
            if (!(service == null || service.getPackage() == null)) {
                pName = service.getPackage();
            }
            if (!(pName != null || service == null || service.getComponent() == null)) {
                pName = service.getComponent().getPackageName();
            }
            if (!(pName == null || OppoMultiLauncherUtil.getInstance().isMultiApp(pName) || OppoMultiAppManagerUtil.getInstance().isGms(pName))) {
                userId = 0;
            }
        }
        ServiceMap smap = getServiceMap(userId);
        ComponentName comp = service.getComponent();
        if (comp != null) {
            r = (ServiceRecord) smap.mServicesByName.get(comp);
        } else {
            r = null;
        }
        if (r == null && !isBindExternal) {
            r = (ServiceRecord) smap.mServicesByIntent.get(new FilterComparison(service));
        }
        if (!(r == null || (r.serviceInfo.flags & 4) == 0)) {
            if (!callingPackage.equals(r.packageName)) {
                r = null;
            }
        }
        if (r == null) {
            try {
                ResolveInfo rInfo = AppGlobals.getPackageManager().resolveService(service, resolvedType, 268436480, userId);
                ServiceInfo sInfo = rInfo != null ? rInfo.serviceInfo : null;
                if (sInfo == null) {
                    if (ActivityManagerDebugConfig.DEBUG_AMS || ActivityManagerDebugConfig.DEBUG_SERVICE) {
                        Slog.w(TAG_SERVICE, "Unable to start service " + service + " U=" + userId + ": not found");
                    }
                    return null;
                }
                ServiceInfo sInfo2;
                ComponentName name = new ComponentName(sInfo.applicationInfo.packageName, sInfo.name);
                if ((sInfo.flags & 4) != 0) {
                    if (!isBindExternal) {
                        throw new SecurityException("BIND_EXTERNAL_SERVICE required for " + name);
                    } else if (!sInfo.exported) {
                        throw new SecurityException("BIND_EXTERNAL_SERVICE failed, " + name + " is not exported");
                    } else if ((sInfo.flags & 2) == 0) {
                        throw new SecurityException("BIND_EXTERNAL_SERVICE failed, " + name + " is not an isolatedProcess");
                    } else {
                        ApplicationInfo aInfo = AppGlobals.getPackageManager().getApplicationInfo(callingPackage, 1024, userId);
                        if (aInfo == null) {
                            throw new SecurityException("BIND_EXTERNAL_SERVICE failed, could not resolve client package " + callingPackage);
                        }
                        sInfo2 = new ServiceInfo(sInfo);
                        sInfo2.applicationInfo = new ApplicationInfo(sInfo2.applicationInfo);
                        sInfo2.applicationInfo.packageName = aInfo.packageName;
                        sInfo2.applicationInfo.uid = aInfo.uid;
                        ComponentName componentName = new ComponentName(aInfo.packageName, name.getClassName());
                        service.setComponent(componentName);
                        name = componentName;
                        sInfo = sInfo2;
                    }
                } else if (isBindExternal) {
                    throw new SecurityException("BIND_EXTERNAL_SERVICE failed, " + name + " is not an externalService");
                }
                if (userId > 0) {
                    if (this.mAm.isSingleton(sInfo.processName, sInfo.applicationInfo, sInfo.name, sInfo.flags)) {
                        if (this.mAm.isValidSingletonCall(callingUid, sInfo.applicationInfo.uid)) {
                            userId = 0;
                            smap = getServiceMap(0);
                        }
                    }
                    sInfo2 = new ServiceInfo(sInfo);
                    sInfo2.applicationInfo = this.mAm.getAppInfoForUser(sInfo2.applicationInfo, userId);
                } else {
                    sInfo2 = sInfo;
                }
                ServiceRecord r2 = (ServiceRecord) smap.mServicesByName.get(name);
                if (r2 != null) {
                    r = r2;
                } else if (createIfNeeded) {
                    try {
                        Serv ss;
                        FilterComparison filter = new FilterComparison(service.cloneFilter());
                        ServiceRestarter res = new ServiceRestarter(this, null);
                        BatteryStatsImpl stats = this.mAm.mBatteryStatsService.getActiveStatistics();
                        synchronized (stats) {
                            ss = stats.getServiceStatsLocked(sInfo2.applicationInfo.uid, sInfo2.packageName, sInfo2.name);
                        }
                        r = new ServiceRecord(this.mAm, ss, name, filter, sInfo2, callingFromFg, res);
                        res.setService(r);
                        smap.mServicesByName.put(name, r);
                        smap.mServicesByIntent.put(filter, r);
                        for (int i = this.mPendingServices.size() - 1; i >= 0; i--) {
                            ServiceRecord pr = (ServiceRecord) this.mPendingServices.get(i);
                            if (pr.serviceInfo.applicationInfo.uid == sInfo2.applicationInfo.uid && pr.name.equals(name)) {
                                this.mPendingServices.remove(i);
                            }
                        }
                    } catch (RemoteException e) {
                        r = r2;
                    }
                } else {
                    r = r2;
                }
            } catch (RemoteException e2) {
            }
        }
        if (r == null) {
            return null;
        }
        if (this.mAm.checkComponentPermission(r.permission, callingPid, callingUid, r.appInfo.uid, r.exported) == 0) {
            if (!(r.permission == null || callingPackage == null)) {
                int opCode = AppOpsManager.permissionToOpCode(r.permission);
                if (!(opCode == -1 || this.mAm.mAppOpsService.noteOperation(opCode, callingUid, callingPackage) == 0)) {
                    Slog.w(TAG, "Appop Denial: Accessing service " + r.name + " from pid=" + callingPid + ", uid=" + callingUid + " requires appop " + AppOpsManager.opToName(opCode));
                    return null;
                }
            }
            if (this.mAm.mIntentFirewall.checkService(r.name, service, callingUid, callingPid, resolvedType, r.appInfo)) {
                return new ServiceLookupResult(r, null);
            }
            return null;
        } else if (r.exported) {
            Slog.w(TAG, "Permission Denial: Accessing service " + r.name + " from pid=" + callingPid + ", uid=" + callingUid + " requires " + r.permission);
            return new ServiceLookupResult(null, r.permission);
        } else {
            Slog.w(TAG, "Permission Denial: Accessing service " + r.name + " from pid=" + callingPid + ", uid=" + callingUid + " that is not exported from uid " + r.appInfo.uid);
            return new ServiceLookupResult(null, "not exported from uid " + r.appInfo.uid);
        }
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "ZhiYong.Lin@Plf.Framework, modify for BPM", property = OppoRomType.ROM)
    private final void bumpServiceExecutingLocked(ServiceRecord r, boolean fg, String why) {
        if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
            Slog.v(TAG_SERVICE, ">>> EXECUTING " + why + " of " + r + " in app " + r.app);
        } else if (ActivityManagerDebugConfig.DEBUG_SERVICE_EXECUTING) {
            Slog.v(TAG_SERVICE_EXECUTING, ">>> EXECUTING " + why + " of " + r.shortName);
        }
        long now = SystemClock.uptimeMillis();
        if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
            Slog.v(TAG, "bumpServiceExecutingLocked r.executeNesting " + r.executeNesting);
        }
        if (r.executeNesting == 0) {
            r.executeFg = fg;
            ServiceState stracker = r.getTracker();
            if (stracker != null) {
                stracker.setExecuting(true, this.mAm.mProcessStats.getMemFactorLocked(), now);
            }
            if (r.app != null) {
                OppoProcessManagerHelper.resumeProcessForService(r, fg, why);
                r.app.executingServices.add(r);
                ProcessRecord processRecord = r.app;
                processRecord.execServicesFg |= fg;
                if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                    Slog.v(TAG, "bumpServiceExecutingLocked r.app.executingServices.size() " + r.app.executingServices.size());
                }
                if (r.app.executingServices.size() == 1) {
                    scheduleServiceTimeoutLocked(r.app);
                }
            }
        } else if (!(r.app == null || !fg || r.app.execServicesFg)) {
            r.app.execServicesFg = true;
            scheduleServiceTimeoutLocked(r.app);
        }
        r.executeFg |= fg;
        r.executeNesting++;
        r.executingStart = now;
    }

    private final boolean requestServiceBindingLocked(ServiceRecord r, IntentBindRecord i, boolean execInFg, boolean rebind) throws TransactionTooLargeException {
        boolean inDestroying;
        if (r.app == null || r.app.thread == null) {
            return false;
        }
        if ((!i.requested || rebind) && i.apps.size() > 0) {
            try {
                bumpServiceExecutingLocked(r, execInFg, "bind");
                r.app.forceProcessStateUpTo(10);
                r.app.thread.scheduleBindService(r, i.intent.getIntent(), rebind, r.app.repProcState);
                if (!rebind) {
                    i.requested = true;
                }
                i.hasBound = true;
                i.doRebind = false;
            } catch (TransactionTooLargeException e) {
                if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                    Slog.v(TAG_SERVICE, "Crashed while binding " + r, e);
                }
                inDestroying = this.mDestroyingServices.contains(r);
                serviceDoneExecutingLocked(r, inDestroying, inDestroying);
                throw e;
            } catch (RemoteException e2) {
                if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                    Slog.v(TAG_SERVICE, "Crashed while binding " + r);
                }
                inDestroying = this.mDestroyingServices.contains(r);
                serviceDoneExecutingLocked(r, inDestroying, inDestroying);
                return false;
            }
        }
        return true;
    }

    private final boolean scheduleServiceRestartLocked(ServiceRecord r, boolean allowCancel) {
        boolean canceled = false;
        if (this.mAm.isShuttingDownLocked()) {
            Slog.w(TAG, "Not scheduling restart of crashed service " + r.shortName + " - system is shutting down");
            return false;
        }
        ServiceMap smap = getServiceMap(r.userId);
        if (smap.mServicesByName.get(r.name) != r) {
            Slog.wtf(TAG, "Attempting to schedule restart of " + r + " when found in map: " + ((ServiceRecord) smap.mServicesByName.get(r.name)));
            return false;
        }
        int i;
        ServiceRecord r2;
        long now = SystemClock.uptimeMillis();
        if ((r.serviceInfo.applicationInfo.flags & 8) == 0) {
            long minDuration = 1000;
            long resetTime = 60000;
            int N = r.deliveredStarts.size();
            if (N > 0) {
                for (i = N - 1; i >= 0; i--) {
                    StartItem si = (StartItem) r.deliveredStarts.get(i);
                    si.removeUriPermissionsLocked();
                    if (si.intent != null) {
                        if (!allowCancel || (si.deliveryCount < 3 && si.doneExecutingCount < 6)) {
                            r.pendingStarts.add(0, si);
                            long dur = (SystemClock.uptimeMillis() - si.deliveredTime) * 2;
                            if (minDuration < dur) {
                                minDuration = dur;
                            }
                            if (resetTime < dur) {
                                resetTime = dur;
                            }
                        } else {
                            Slog.w(TAG, "Canceling start item " + si.intent + " in service " + r.name);
                            canceled = true;
                        }
                    }
                }
                r.deliveredStarts.clear();
            }
            r.totalRestartCount++;
            if (r.restartDelay == 0) {
                r.restartCount++;
                r.restartDelay = minDuration;
            } else {
                if (now > r.restartTime + resetTime) {
                    r.restartCount = 1;
                    r.restartDelay = minDuration;
                } else {
                    r.restartDelay *= 4;
                    if (r.restartDelay < minDuration) {
                        r.restartDelay = minDuration;
                    }
                }
                if (r.shortName != null && (r.shortName.contains("com.tencent.mm") || r.shortName.contains("com.tencent.mobileqq"))) {
                    r.restartDelay = minDuration;
                }
            }
            r.nextRestartTime = r.restartDelay + now;
            boolean repeat;
            do {
                repeat = false;
                for (i = this.mRestartingServices.size() - 1; i >= 0; i--) {
                    r2 = (ServiceRecord) this.mRestartingServices.get(i);
                    if (r2 != r && r.nextRestartTime >= r2.nextRestartTime - 10000 && r.nextRestartTime < r2.nextRestartTime + 10000) {
                        r.nextRestartTime = r2.nextRestartTime + 10000;
                        r.restartDelay = r.nextRestartTime - now;
                        repeat = true;
                        continue;
                        break;
                    }
                }
            } while (repeat);
            if (r.processName != null && r.processName.equals("com.tencent.mm:push") && r.restartDelay > 5000 + minDuration) {
                r.restartDelay = 5000 + minDuration;
                r.nextRestartTime = r.restartDelay + now;
                if (DEBUG_DELAYED_SERVICE) {
                    Slog.d(TAG, "adjust restart mm:push in " + r.restartDelay);
                }
            }
        } else {
            r.totalRestartCount++;
            r.restartCount = 0;
            r.restartDelay = 0;
            r.nextRestartTime = now;
        }
        if (!this.mRestartingServices.contains(r)) {
            r.createdFromFg = false;
            this.mRestartingServices.add(r);
            r.makeRestarting(this.mAm.mProcessStats.getMemFactorLocked(), now);
        }
        cancelForegroudNotificationLocked(r);
        this.mAm.mHandler.removeCallbacks(r.restarter);
        this.mAm.mHandler.postAtTime(r.restarter, r.nextRestartTime);
        r.nextRestartTime = SystemClock.uptimeMillis() + r.restartDelay;
        if (DEBUG_DELAYED_SERVICE) {
            Slog.w(TAG, "r " + r + " r.restartDelay " + r.restartDelay + " r.nextRestartTime " + r.nextRestartTime);
        }
        boolean shouldLogout = true;
        if (this.mCurRestartServiceName == null) {
            this.mCurRestartServiceName = r.shortName;
        } else if (!this.mCurRestartServiceName.equals(r.shortName)) {
            this.mCurRestartServiceName = r.shortName;
        } else if (r.totalRestartCount > 3) {
            shouldLogout = false;
        }
        if (shouldLogout) {
            if (DEBUG_DELAYED_SERVICE) {
                Slog.w(TAG, "r " + r + " r.restartDelay " + r.restartDelay + " r.nextRestartTime " + r.nextRestartTime);
            }
            if (ActivityManagerDebugConfig.DEBUG_AMS || DEBUG_DELAYED_SERVICE) {
                Slog.w(TAG, "Scheduling restart of crashed service " + r.shortName + " in " + r.restartDelay + "ms");
            }
            if (SERVICE_RESCHEDULE && DEBUG_DELAYED_SERVICE) {
                for (i = this.mRestartingServices.size() - 1; i >= 0; i--) {
                    r2 = (ServiceRecord) this.mRestartingServices.get(i);
                    Slog.w(TAG, "Restarting list - i " + i + " r2.nextRestartTime " + r2.nextRestartTime + " r2.name " + r2.name);
                }
            }
            Integer[] numArr = new Object[3];
            numArr[0] = Integer.valueOf(r.userId);
            numArr[1] = r.shortName;
            numArr[2] = Long.valueOf(r.restartDelay);
            EventLog.writeEvent(EventLogTags.AM_SCHEDULE_SERVICE_RESTART, numArr);
            if (DEBUG_DELAYED_SERVICE) {
                Slog.v(TAG, "scheduleServiceRestartLocked r " + r + " call by " + Debug.getCallers(8));
            }
        }
        return canceled;
    }

    final void performServiceRestartLocked(ServiceRecord r) {
        if (!this.mRestartingServices.contains(r)) {
            return;
        }
        if (isServiceNeeded(r, false, false)) {
            try {
                if (LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(SystemProperties.get("persist.runningbooster.support")) || LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(SystemProperties.get("ro.mtk_aws_support"))) {
                    this.mBringUpReason = BRING_UP_RESTART_SERVICE;
                    this.mCurrentCallerPackage = null;
                    this.mCurrentCallerUid = -1;
                }
                bringUpServiceLocked(r, r.intent.getIntent().getFlags(), r.createdFromFg, true, false);
            } catch (TransactionTooLargeException e) {
            }
            return;
        }
        Slog.e(TAG, "Restarting service that is not needed: " + r);
    }

    private final boolean unscheduleServiceRestartLocked(ServiceRecord r, int callingUid, boolean force) {
        if (!force && r.restartDelay == 0) {
            return false;
        }
        boolean removed = this.mRestartingServices.remove(r);
        if (removed || callingUid != r.appInfo.uid) {
            r.resetRestartCounter();
        }
        if (removed) {
            clearRestartingIfNeededLocked(r);
        }
        this.mAm.mHandler.removeCallbacks(r.restarter);
        return true;
    }

    private void clearRestartingIfNeededLocked(ServiceRecord r) {
        if (r.restartTracker != null) {
            boolean stillTracking = false;
            for (int i = this.mRestartingServices.size() - 1; i >= 0; i--) {
                if (((ServiceRecord) this.mRestartingServices.get(i)).restartTracker == r.restartTracker) {
                    stillTracking = true;
                    break;
                }
            }
            if (!stillTracking) {
                r.restartTracker.setRestarting(false, this.mAm.mProcessStats.getMemFactorLocked(), SystemClock.uptimeMillis());
                r.restartTracker = null;
            }
        }
    }

    /* JADX WARNING: Missing block: B:121:0x04ed, code:
            if (r42.equals("skipped") != false) goto L_0x04ef;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private String bringUpServiceLocked(ServiceRecord r, int intentFlags, boolean execInFg, boolean whileRestarting, boolean permissionsReviewRequired) throws TransactionTooLargeException {
        if (r.app != null && r.app.thread != null) {
            sendServiceArgsLocked(r, execInFg, false);
            return null;
        } else if (!whileRestarting && r.restartDelay > 0) {
            return null;
        } else {
            if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                Slog.v(TAG_SERVICE, "Bringing up " + r + " " + r.intent);
            }
            if (this.mRestartingServices.remove(r)) {
                r.resetRestartCounter();
                clearRestartingIfNeededLocked(r);
            }
            if (r.delayed) {
                if (DEBUG_DELAYED_STARTS) {
                    Slog.v(TAG_SERVICE, "REM FR DELAY LIST (bring up): " + r);
                }
                getServiceMap(r.userId).mDelayedStartList.remove(r);
                r.delayed = false;
            }
            String msg;
            if (!this.mAm.mUserController.hasStartedUserState(r.userId)) {
                msg = "Unable to launch app " + r.appInfo.packageName + "/" + r.appInfo.uid + " for service " + r.intent.getIntent() + ": user " + r.userId + " is stopped";
                Slog.w(TAG, msg);
                bringDownServiceLocked(r);
                return msg;
            } else if (OppoAbnormalAppManager.getInstance().validStartService(r.packageName)) {
                Slog.d(OppoAbnormalAppManager.TAG, "UL app " + r.appInfo.packageName + "/" + r.appInfo.uid + " for service " + r.intent.getIntent() + ": user " + r.userId + " is R");
                bringDownServiceLocked(r);
                return null;
            } else {
                ProcessRecord app;
                ArrayList<ConnectionRecord> clist;
                int i;
                int j;
                Object[] objArr;
                try {
                    AppGlobals.getPackageManager().setPackageStoppedState(r.packageName, false, r.userId);
                } catch (RemoteException e) {
                } catch (IllegalArgumentException e2) {
                    Slog.w(TAG, "Failed trying to unstop package " + r.packageName + ": " + e2);
                }
                boolean isolated = (r.serviceInfo.flags & 2) != 0;
                String procName = r.processName;
                boolean openTraceLog = false;
                if (procName != null && (procName.contains("clear_filter") || procName.contains("safecenter"))) {
                    openTraceLog = true;
                }
                if (openTraceLog) {
                    Slog.d(TAG, "bringUpService for proc:" + procName + ", isolated:" + isolated);
                }
                if (isolated) {
                    app = r.isolatedProc;
                    if (openTraceLog) {
                        Slog.d(TAG, "bringUpService for proc:" + procName + ", use isolatedProc:" + app);
                    }
                } else {
                    app = this.mAm.getProcessRecordLocked(procName, r.appInfo.uid, false);
                    if (ActivityManagerDebugConfig.DEBUG_MU) {
                        Slog.v(TAG_MU, "bringUpServiceLocked: appInfo.uid=" + r.appInfo.uid + " app=" + app);
                    }
                    if (!(app == null || app.thread == null)) {
                        try {
                            app.addPackage(r.appInfo.packageName, r.appInfo.versionCode, this.mAm.mProcessStats);
                            realStartServiceLocked(r, app, execInFg);
                            return null;
                        } catch (TransactionTooLargeException e3) {
                            throw e3;
                        } catch (Throwable e4) {
                            Slog.w(TAG, "Exception when starting service " + r.shortName, e4);
                        }
                    }
                }
                PackageStoppedStatusChanged eventData;
                if (app == null && !permissionsReviewRequired) {
                    String suppressAction = "allowed";
                    if (LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(SystemProperties.get("persist.runningbooster.support")) || LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(SystemProperties.get("ro.mtk_aws_support"))) {
                        List<String> callerList = new ArrayList();
                        List<Integer> callerUidList = new ArrayList();
                        List<String> clientList = new ArrayList();
                        List<Integer> clientUidList = new ArrayList();
                        if (this.mCurrentCallerPackage != null) {
                            callerList.add(this.mCurrentCallerPackage);
                            callerUidList.add(Integer.valueOf(this.mCurrentCallerUid));
                        }
                        for (int conni = 0; conni < r.connections.size(); conni++) {
                            ProcessRecord client = null;
                            clist = (ArrayList) r.connections.valueAt(conni);
                            if (clist != null) {
                                for (i = 0; i < clist.size(); i++) {
                                    ConnectionRecord cr = (ConnectionRecord) clist.get(i);
                                    if (!(cr == null || cr.binding == null)) {
                                        client = cr.binding.client;
                                    }
                                    if (client != null) {
                                        for (j = 0; j < client.pkgList.size(); j++) {
                                            clientList.add((String) client.pkgList.keyAt(j));
                                            clientUidList.add(Integer.valueOf(client.userId));
                                        }
                                    }
                                }
                            }
                        }
                        ReadyToStartComponent eventHookData = ReadyToStartComponent.createInstance();
                        objArr = new Object[10];
                        objArr[0] = r.appInfo.packageName;
                        objArr[1] = Integer.valueOf(r.appInfo.uid);
                        objArr[2] = callerList;
                        objArr[3] = callerUidList;
                        objArr[4] = r.delayedServiceCallerPkg;
                        objArr[5] = r.delayedServiceCallerUid;
                        objArr[6] = clientList;
                        objArr[7] = clientUidList;
                        objArr[8] = this.mBringUpReason;
                        objArr[9] = suppressAction;
                        eventHookData.set(objArr);
                        this.mAm.getAMEventHook().hook(Event.AM_ReadyToStartComponent, eventHookData);
                        suppressAction = eventHookData.getString(Index.suppressAction);
                        Slog.d(TAG, "[process suppression] suppressAction = " + suppressAction);
                    }
                    if (suppressAction != null) {
                        if (!suppressAction.equals("delayed")) {
                        }
                        Slog.d(TAG, "[process suppression] bringUpServiceLocked : suppress process to start for service!");
                        try {
                            AppGlobals.getPackageManager().setPackageStoppedState(r.packageName, true, r.userId);
                        } catch (RemoteException e5) {
                            Slog.w(TAG, "RemoteException: " + e5);
                        } catch (IllegalArgumentException e22) {
                            Slog.w(TAG, "Failed trying to stop package " + r.packageName + ": " + e22);
                        }
                        if (suppressAction.equals("delayed") && scheduleServiceRestartLocked(r, true)) {
                            bringDownServiceLocked(r);
                            Slog.d(TAG, "[process suppression] restart canceled!");
                        }
                    }
                    if (LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(SystemProperties.get("persist.runningbooster.support")) || LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(SystemProperties.get("ro.mtk_aws_support"))) {
                        eventData = PackageStoppedStatusChanged.createInstance();
                        objArr = new Object[3];
                        objArr[0] = r.packageName;
                        objArr[1] = Integer.valueOf(0);
                        objArr[2] = "bringUpServiceLocked";
                        eventData.set(objArr);
                        this.mAm.getAMEventHook().hook(Event.AM_PackageStoppedStatusChanged, eventData);
                    }
                    app = this.mAm.startProcessLocked(procName, r.appInfo, true, intentFlags, OppoProcessManager.RESUME_REASON_SERVICE_STR, r.name, false, isolated, false);
                    if (app == null) {
                        msg = "Unable to launch app " + r.appInfo.packageName + "/" + r.appInfo.uid + " for service " + r.intent.getIntent() + ": process is bad";
                        Slog.w(TAG, msg);
                        bringDownServiceLocked(r);
                        return msg;
                    } else if (isolated) {
                        r.isolatedProc = app;
                    }
                } else if (LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(SystemProperties.get("persist.runningbooster.support")) || LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(SystemProperties.get("ro.mtk_aws_support"))) {
                    eventData = PackageStoppedStatusChanged.createInstance();
                    objArr = new Object[3];
                    objArr[0] = r.packageName;
                    objArr[1] = Integer.valueOf(0);
                    objArr[2] = "bringUpServiceLocked";
                    eventData.set(objArr);
                    this.mAm.getAMEventHook().hook(Event.AM_PackageStoppedStatusChanged, eventData);
                }
                if ((LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(SystemProperties.get("persist.runningbooster.support")) || LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(SystemProperties.get("ro.mtk_aws_support"))) && app != null) {
                    ReadyToStartService eventData2 = ReadyToStartService.createInstance();
                    ArrayList<String> clientPkgList = null;
                    ArrayList<Integer> clientUidList2 = null;
                    if (this.mBringUpReason.equalsIgnoreCase(BRING_UP_RESTART_SERVICE)) {
                        clientPkgList = new ArrayList();
                        clientUidList2 = new ArrayList();
                        for (i = 0; i < r.connections.size(); i++) {
                            clist = (ArrayList) r.connections.valueAt(i);
                            for (j = 0; j < clist.size(); j++) {
                                ConnectionRecord conn = (ConnectionRecord) clist.get(j);
                                if (conn.binding != null) {
                                    ProcessRecord proc = conn.binding.client;
                                    if (proc != null) {
                                        int clientUid = proc.uid;
                                        for (Entry<String, ProcessStateHolder> entry : proc.pkgList.entrySet()) {
                                            clientPkgList.add((String) entry.getKey());
                                            clientUidList2.add(Integer.valueOf(clientUid));
                                        }
                                    }
                                }
                            }
                        }
                    }
                    objArr = new Object[8];
                    objArr[0] = r.appInfo.packageName;
                    objArr[1] = this.mCurrentCallerPackage;
                    objArr[2] = Integer.valueOf(this.mCurrentCallerUid);
                    objArr[3] = clientPkgList;
                    objArr[4] = clientUidList2;
                    objArr[5] = r.delayedServiceCallerPkg;
                    objArr[6] = r.delayedServiceCallerUid;
                    objArr[7] = this.mBringUpReason;
                    eventData2.set(objArr);
                    this.mAm.getAMEventHook().hook(Event.AM_ReadyToStartService, eventData2);
                    r.delayedServiceCallerPkg = null;
                    r.delayedServiceCallerUid = null;
                }
                if (!this.mPendingServices.contains(r)) {
                    this.mPendingServices.add(r);
                }
                if (r.delayedStop) {
                    r.delayedStop = false;
                    if (r.startRequested) {
                        if (DEBUG_DELAYED_STARTS) {
                            Slog.v(TAG_SERVICE, "Applying delayed stop (in bring up): " + r);
                        }
                        stopServiceLocked(r);
                    }
                }
                return null;
            }
        }
    }

    private final void requestServiceBindingsLocked(ServiceRecord r, boolean execInFg) throws TransactionTooLargeException {
        int i = r.bindings.size() - 1;
        while (i >= 0 && requestServiceBindingLocked(r, (IntentBindRecord) r.bindings.valueAt(i), execInFg, false)) {
            i--;
        }
    }

    private final void realStartServiceLocked(ServiceRecord r, ProcessRecord app, boolean execInFg) throws RemoteException {
        if (app.thread == null) {
            throw new RemoteException();
        }
        if (ActivityManagerDebugConfig.DEBUG_MU) {
            Slog.v(TAG_MU, "realStartServiceLocked, ServiceRecord.uid = " + r.appInfo.uid + ", ProcessRecord.uid = " + app.uid);
        }
        r.app = app;
        long uptimeMillis = SystemClock.uptimeMillis();
        r.lastActivity = uptimeMillis;
        r.restartTime = uptimeMillis;
        if (this.mAm.mWallpaperClassName != null && r.name.equals(this.mAm.mWallpaperClassName)) {
            this.mAm.mWallpaperProcess = app;
        }
        boolean newService = app.services.add(r);
        bumpServiceExecutingLocked(r, execInFg, "create");
        this.mAm.updateLruProcessLocked(app, false, null);
        this.mAm.updateOomAdjLocked();
        boolean inDestroying;
        try {
            if (LOG_SERVICE_START_STOP) {
                int lastPeriod = r.shortName.lastIndexOf(46);
                EventLogTags.writeAmCreateService(r.userId, System.identityHashCode(r), lastPeriod >= 0 ? r.shortName.substring(lastPeriod) : r.shortName, r.app.uid, r.app.pid);
            }
            synchronized (r.stats.getBatteryStats()) {
                r.stats.startLaunchedLocked();
            }
            this.mAm.notifyPackageUse(r.serviceInfo.packageName, 1);
            app.forceProcessStateUpTo(10);
            if (!ActivityManagerService.IS_USER_BUILD || ActivityManagerDebugConfig.DEBUG_SERVICE) {
                Slog.d(TAG_SERVICE, "AMS Creating service " + r);
            }
            app.thread.scheduleCreateService(r, r.serviceInfo, this.mAm.compatibilityInfoForPackageLocked(r.serviceInfo.applicationInfo), app.repProcState);
            r.postNotification();
            if (!true) {
                inDestroying = this.mDestroyingServices.contains(r);
                serviceDoneExecutingLocked(r, inDestroying, inDestroying);
                if (newService) {
                    app.services.remove(r);
                    r.app = null;
                }
                if (!inDestroying) {
                    scheduleServiceRestartLocked(r, false);
                }
            }
            if (r.whitelistManager) {
                app.whitelistManager = true;
            }
            requestServiceBindingsLocked(r, execInFg);
            updateServiceClientActivitiesLocked(app, null, true);
            if (r.startRequested && r.callStart && r.pendingStarts.size() == 0) {
                r.pendingStarts.add(new StartItem(r, false, r.makeNextStartId(), null, null));
            }
            sendServiceArgsLocked(r, execInFg, true);
            if (r.delayed) {
                if (DEBUG_DELAYED_STARTS) {
                    Slog.v(TAG_SERVICE, "REM FR DELAY LIST (new proc): " + r);
                }
                getServiceMap(r.userId).mDelayedStartList.remove(r);
                r.delayed = false;
            }
            if (r.delayedStop) {
                r.delayedStop = false;
                if (r.startRequested) {
                    if (DEBUG_DELAYED_STARTS) {
                        Slog.v(TAG_SERVICE, "Applying delayed stop (from start): " + r);
                    }
                    stopServiceLocked(r);
                }
            }
        } catch (DeadObjectException e) {
            try {
                Slog.w(TAG, "Application dead when creating service " + r);
                this.mAm.appDiedLocked(app);
                throw e;
            } catch (Throwable th) {
                if (!false) {
                    inDestroying = this.mDestroyingServices.contains(r);
                    serviceDoneExecutingLocked(r, inDestroying, inDestroying);
                    if (newService) {
                        app.services.remove(r);
                        r.app = null;
                    }
                    if (!inDestroying) {
                        scheduleServiceRestartLocked(r, false);
                    }
                }
            }
        }
    }

    private final void sendServiceArgsLocked(ServiceRecord r, boolean execInFg, boolean oomAdjusted) throws TransactionTooLargeException {
        int N = r.pendingStarts.size();
        if (N != 0) {
            while (r.pendingStarts.size() > 0) {
                Exception caughtException = null;
                try {
                    StartItem si = (StartItem) r.pendingStarts.remove(0);
                    if (!ActivityManagerService.IS_USER_BUILD || ActivityManagerDebugConfig.DEBUG_SERVICE) {
                        Slog.v(TAG_SERVICE, "Sending arguments to: " + r + " " + r.intent + " args=" + si.intent);
                    }
                    if (si.intent != null || N <= 1) {
                        si.deliveredTime = SystemClock.uptimeMillis();
                        r.deliveredStarts.add(si);
                        si.deliveryCount++;
                        if (si.neededGrants != null) {
                            this.mAm.grantUriPermissionUncheckedFromIntentLocked(si.neededGrants, si.getUriPermissionsLocked());
                        }
                        bumpServiceExecutingLocked(r, execInFg, "start");
                        if (!oomAdjusted) {
                            oomAdjusted = true;
                            this.mAm.updateOomAdjLocked(r.app);
                        }
                        int flags = 0;
                        if (si.deliveryCount > 1) {
                            flags = 2;
                        }
                        if (si.doneExecutingCount > 0) {
                            flags |= 1;
                        }
                        r.app.thread.scheduleServiceArgs(r, si.taskRemoved, si.id, flags, si.intent);
                        if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                            Slog.d(TAG_SERVICE, "SVC-Sent arguments: " + r + ", app=" + r.app + ", args=" + si.intent + ", flags=" + flags);
                        }
                        if (caughtException != null) {
                            boolean inDestroying = this.mDestroyingServices.contains(r);
                            serviceDoneExecutingLocked(r, inDestroying, inDestroying);
                            if (caughtException instanceof TransactionTooLargeException) {
                                throw ((TransactionTooLargeException) caughtException);
                            }
                        }
                    }
                } catch (Exception e) {
                    if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                        Slog.v(TAG_SERVICE, "Transaction too large: intent=" + (null != null ? null.intent : "si=null"));
                    }
                    caughtException = e;
                } catch (Exception e2) {
                    if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                        Slog.v(TAG_SERVICE, "Crashed while sending args: " + r);
                    }
                    caughtException = e2;
                } catch (Exception e3) {
                    Slog.w(TAG, "Unexpected exception", e3);
                    caughtException = e3;
                }
            }
        }
    }

    private final boolean isServiceNeeded(ServiceRecord r, boolean knowConn, boolean hasConn) {
        if (r.startRequested) {
            return true;
        }
        if (!knowConn) {
            hasConn = r.hasAutoCreateConnections();
        }
        if (hasConn) {
            return true;
        }
        return false;
    }

    private final void bringDownServiceIfNeededLocked(ServiceRecord r, boolean knowConn, boolean hasConn) {
        if (!isServiceNeeded(r, knowConn, hasConn) && !this.mPendingServices.contains(r)) {
            bringDownServiceLocked(r);
        }
    }

    private final void bringDownServiceLocked(ServiceRecord r) {
        int i;
        for (int conni = r.connections.size() - 1; conni >= 0; conni--) {
            ArrayList<ConnectionRecord> c = (ArrayList) r.connections.valueAt(conni);
            for (i = 0; i < c.size(); i++) {
                ConnectionRecord cr = (ConnectionRecord) c.get(i);
                cr.serviceDead = true;
                try {
                    cr.conn.connected(r.name, null);
                } catch (Exception e) {
                    Slog.w(TAG, "Failure disconnecting service " + r.name + " to connection " + ((ConnectionRecord) c.get(i)).conn.asBinder() + " (in " + ((ConnectionRecord) c.get(i)).binding.client.processName + ")", e);
                }
            }
        }
        if (!(r.app == null || r.app.thread == null)) {
            for (i = r.bindings.size() - 1; i >= 0; i--) {
                IntentBindRecord ibr = (IntentBindRecord) r.bindings.valueAt(i);
                if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                    Slog.v(TAG_SERVICE, "Bringing down binding " + ibr + ": hasBound=" + ibr.hasBound);
                }
                if (ibr.hasBound) {
                    try {
                        bumpServiceExecutingLocked(r, false, "bring down unbind");
                        this.mAm.updateOomAdjLocked(r.app);
                        ibr.hasBound = false;
                        r.app.thread.scheduleUnbindService(r, ibr.intent.getIntent());
                    } catch (Exception e2) {
                        Slog.w(TAG, "Exception when unbinding service " + r.shortName, e2);
                        serviceProcessGoneLocked(r);
                    }
                }
            }
        }
        if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
            Slog.v(TAG_SERVICE, "Bringing down " + r + " " + r.intent);
        }
        r.destroyTime = SystemClock.uptimeMillis();
        if (LOG_SERVICE_START_STOP) {
            EventLogTags.writeAmDestroyService(r.userId, System.identityHashCode(r), r.app != null ? r.app.pid : -1);
        }
        ServiceMap smap = getServiceMap(r.userId);
        smap.mServicesByName.remove(r.name);
        smap.mServicesByIntent.remove(r.intent);
        r.totalRestartCount = 0;
        unscheduleServiceRestartLocked(r, 0, true);
        for (i = this.mPendingServices.size() - 1; i >= 0; i--) {
            if (this.mPendingServices.get(i) == r) {
                this.mPendingServices.remove(i);
                if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                    Slog.v(TAG_SERVICE, "Removed pending: " + r);
                }
            }
        }
        cancelForegroudNotificationLocked(r);
        r.isForeground = false;
        r.foregroundId = 0;
        r.foregroundNoti = null;
        r.clearDeliveredStartsLocked();
        r.pendingStarts.clear();
        if (r.app != null) {
            synchronized (r.stats.getBatteryStats()) {
                r.stats.stopLaunchedLocked();
            }
            r.app.services.remove(r);
            if (r.whitelistManager) {
                updateWhitelistManagerLocked(r.app);
            }
            if (r.app.thread != null) {
                updateServiceForegroundLocked(r.app, false);
                try {
                    bumpServiceExecutingLocked(r, false, "destroy");
                    this.mDestroyingServices.add(r);
                    r.destroying = true;
                    this.mAm.updateOomAdjLocked(r.app);
                    r.app.thread.scheduleStopService(r);
                } catch (Exception e22) {
                    Slog.w(TAG, "Exception when destroying service " + r.shortName, e22);
                    serviceProcessGoneLocked(r);
                }
            } else if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                Slog.v(TAG_SERVICE, "Removed service that has no process: " + r);
            }
        } else if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
            Slog.v(TAG_SERVICE, "Removed service that is not running: " + r);
        }
        if (r.bindings.size() > 0) {
            r.bindings.clear();
        }
        if (r.restarter instanceof ServiceRestarter) {
            ((ServiceRestarter) r.restarter).setService(null);
        }
        int memFactor = this.mAm.mProcessStats.getMemFactorLocked();
        long now = SystemClock.uptimeMillis();
        if (r.tracker != null) {
            r.tracker.setStarted(false, memFactor, now);
            r.tracker.setBound(false, memFactor, now);
            if (r.executeNesting == 0) {
                r.tracker.clearCurrentOwner(r, false);
                r.tracker = null;
            }
        }
        smap.ensureNotStartingBackground(r);
    }

    void removeConnectionLocked(ConnectionRecord c, ProcessRecord skipApp, ActivityRecord skipAct) {
        IBinder binder = c.conn.asBinder();
        AppBindRecord b = c.binding;
        ServiceRecord s = b.service;
        ArrayList<ConnectionRecord> clist = (ArrayList) s.connections.get(binder);
        if (clist != null) {
            clist.remove(c);
            if (clist.size() == 0) {
                s.connections.remove(binder);
            }
        }
        b.connections.remove(c);
        if (!(c.activity == null || c.activity == skipAct || c.activity.connections == null)) {
            c.activity.connections.remove(c);
        }
        if (b.client != skipApp) {
            b.client.connections.remove(c);
            if ((c.flags & 8) != 0) {
                b.client.updateHasAboveClientLocked();
            }
            if ((c.flags & 16777216) != 0) {
                s.updateWhitelistManager();
                if (!(s.whitelistManager || s.app == null)) {
                    updateWhitelistManagerLocked(s.app);
                }
            }
            if (s.app != null) {
                updateServiceClientActivitiesLocked(s.app, c, true);
            }
        }
        clist = (ArrayList) this.mServiceConnections.get(binder);
        if (clist != null) {
            clist.remove(c);
            if (clist.size() == 0) {
                this.mServiceConnections.remove(binder);
            }
        }
        this.mAm.stopAssociationLocked(b.client.uid, b.client.processName, s.appInfo.uid, s.name);
        if (b.connections.size() == 0) {
            b.intent.apps.remove(b.client);
        }
        if (!c.serviceDead) {
            if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                Slog.v(TAG_SERVICE, "Disconnecting binding " + b.intent + ": shouldUnbind=" + b.intent.hasBound);
            }
            if (s.app != null && s.app.thread != null && b.intent.apps.size() == 0 && b.intent.hasBound) {
                try {
                    bumpServiceExecutingLocked(s, false, "unbind");
                    if (b.client != s.app && (c.flags & 32) == 0 && s.app.setProcState <= 11) {
                        this.mAm.updateLruProcessLocked(s.app, false, null);
                    }
                    this.mAm.updateOomAdjLocked(s.app);
                    b.intent.hasBound = false;
                    b.intent.doRebind = false;
                    s.app.thread.scheduleUnbindService(s, b.intent.intent.getIntent());
                } catch (Exception e) {
                    Slog.w(TAG, "Exception when unbinding service " + s.shortName, e);
                    serviceProcessGoneLocked(s);
                }
            }
            this.mPendingServices.remove(s);
            if ((c.flags & 1) != 0) {
                boolean hasAutoCreate = s.hasAutoCreateConnections();
                if (!(hasAutoCreate || s.tracker == null)) {
                    s.tracker.setBound(false, this.mAm.mProcessStats.getMemFactorLocked(), SystemClock.uptimeMillis());
                }
                bringDownServiceIfNeededLocked(s, true, hasAutoCreate);
            }
        }
    }

    void serviceDoneExecutingLocked(ServiceRecord r, int type, int startId, int res) {
        if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
            Slog.v(TAG, "serviceDoneExecutingLocked ServiceRecord= " + r + " type= " + type + " startId= " + startId + " res= " + res);
        }
        boolean inDestroying = this.mDestroyingServices.contains(r);
        if (r != null) {
            if (type == 1) {
                r.callStart = true;
                switch (res) {
                    case 0:
                    case 1:
                        r.findDeliveredStart(startId, true);
                        r.stopIfKilled = false;
                        break;
                    case 2:
                        r.findDeliveredStart(startId, true);
                        if (r.getLastStartId() == startId) {
                            r.stopIfKilled = true;
                            break;
                        }
                        break;
                    case 3:
                        StartItem si = r.findDeliveredStart(startId, false);
                        if (si != null) {
                            si.deliveryCount = 0;
                            si.doneExecutingCount++;
                            r.stopIfKilled = true;
                            break;
                        }
                        break;
                    case 1000:
                        r.findDeliveredStart(startId, true);
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown service start result: " + res);
                }
                if (res == 0) {
                    r.callStart = false;
                }
            } else if (type == 2) {
                if (inDestroying) {
                    if (r.executeNesting != 1) {
                        Slog.w(TAG, "Service done with onDestroy, but executeNesting=" + r.executeNesting + ": " + r);
                        r.executeNesting = 1;
                    }
                } else if (r.app != null) {
                    Slog.w(TAG, "Service done with onDestroy, but not inDestroying: " + r + ", app=" + r.app);
                }
            }
            long origId = Binder.clearCallingIdentity();
            serviceDoneExecutingLocked(r, inDestroying, inDestroying);
            Binder.restoreCallingIdentity(origId);
            return;
        }
        Slog.w(TAG, "Done executing unknown service from pid " + Binder.getCallingPid());
    }

    private void serviceProcessGoneLocked(ServiceRecord r) {
        if (r.tracker != null) {
            int memFactor = this.mAm.mProcessStats.getMemFactorLocked();
            long now = SystemClock.uptimeMillis();
            r.tracker.setExecuting(false, memFactor, now);
            r.tracker.setBound(false, memFactor, now);
            r.tracker.setStarted(false, memFactor, now);
        }
        serviceDoneExecutingLocked(r, true, true);
    }

    private void serviceDoneExecutingLocked(ServiceRecord r, boolean inDestroying, boolean finishing) {
        if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
            Slog.v(TAG_SERVICE, "<<< DONE EXECUTING " + r + ": nesting=" + r.executeNesting + ", inDestroying=" + inDestroying + ", app=" + r.app);
        } else if (ActivityManagerDebugConfig.DEBUG_SERVICE_EXECUTING) {
            Slog.v(TAG_SERVICE_EXECUTING, "<<< DONE EXECUTING " + r.shortName);
        }
        r.executeNesting--;
        if (r.executeNesting <= 0) {
            if (r.app != null) {
                if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                    Slog.v(TAG_SERVICE, "Nesting at 0 of " + r.shortName);
                }
                r.app.execServicesFg = false;
                r.app.executingServices.remove(r);
                if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                    Slog.v(TAG, "r.app.executingServices.size(): " + r.app.executingServices.size());
                }
                if (r.app.executingServices.size() == 0) {
                    if (ActivityManagerDebugConfig.DEBUG_SERVICE || ActivityManagerDebugConfig.DEBUG_SERVICE_EXECUTING) {
                        Slog.v(TAG_SERVICE_EXECUTING, "No more executingServices of " + r.shortName);
                    }
                    this.mAm.mHandler.removeMessages(12, r.app);
                    if (2 == ANRManager.enableANRDebuggingMechanism()) {
                        this.mAm.mAnrHandler.removeMessages(1002, r.app);
                    }
                } else if (r.executeFg) {
                    for (int i = r.app.executingServices.size() - 1; i >= 0; i--) {
                        if (((ServiceRecord) r.app.executingServices.valueAt(i)).executeFg) {
                            r.app.execServicesFg = true;
                            break;
                        }
                    }
                }
                if (inDestroying) {
                    if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                        Slog.v(TAG_SERVICE, "doneExecuting remove destroying " + r);
                    }
                    this.mDestroyingServices.remove(r);
                    r.bindings.clear();
                }
                this.mAm.updateOomAdjLocked(r.app);
            }
            r.executeFg = false;
            if (r.tracker != null) {
                r.tracker.setExecuting(false, this.mAm.mProcessStats.getMemFactorLocked(), SystemClock.uptimeMillis());
                if (finishing) {
                    r.tracker.clearCurrentOwner(r, false);
                    r.tracker = null;
                }
            }
            if (finishing) {
                if (!(r.app == null || r.app.persistent)) {
                    r.app.services.remove(r);
                    if (r.whitelistManager) {
                        updateWhitelistManagerLocked(r.app);
                    }
                }
                r.app = null;
            }
            if (r.executeNesting < 0) {
                r.executeNesting = 0;
            }
        }
    }

    boolean attachApplicationLocked(ProcessRecord proc, String processName) throws RemoteException {
        ServiceRecord sr;
        int i;
        boolean didSomething = false;
        if (this.mPendingServices.size() > 0) {
            sr = null;
            i = 0;
            while (i < this.mPendingServices.size()) {
                try {
                    sr = (ServiceRecord) this.mPendingServices.get(i);
                    if (proc == sr.isolatedProc || (proc.uid == sr.appInfo.uid && processName.equals(sr.processName))) {
                        this.mPendingServices.remove(i);
                        i--;
                        proc.addPackage(sr.appInfo.packageName, sr.appInfo.versionCode, this.mAm.mProcessStats);
                        realStartServiceLocked(sr, proc, sr.createdFromFg);
                        didSomething = true;
                        if (!isServiceNeeded(sr, false, false)) {
                            bringDownServiceLocked(sr);
                        }
                    }
                    i++;
                } catch (RemoteException e) {
                    Slog.w(TAG, "Exception in new application when starting service " + sr.shortName, e);
                    throw e;
                }
            }
        }
        if (this.mRestartingServices.size() > 0) {
            for (i = 0; i < this.mRestartingServices.size(); i++) {
                sr = (ServiceRecord) this.mRestartingServices.get(i);
                if (proc == sr.isolatedProc || (proc.uid == sr.appInfo.uid && processName.equals(sr.processName))) {
                    this.mAm.mHandler.removeCallbacks(sr.restarter);
                    this.mAm.mHandler.post(sr.restarter);
                }
            }
        }
        return didSomething;
    }

    void processStartTimedOutLocked(ProcessRecord proc) {
        int i = 0;
        while (i < this.mPendingServices.size()) {
            ServiceRecord sr = (ServiceRecord) this.mPendingServices.get(i);
            if ((proc.uid == sr.appInfo.uid && proc.processName.equals(sr.processName)) || sr.isolatedProc == proc) {
                Slog.w(TAG, "Forcing bringing down service: " + sr);
                sr.isolatedProc = null;
                this.mPendingServices.remove(i);
                i--;
                bringDownServiceLocked(sr);
            }
            i++;
        }
    }

    private boolean collectPackageServicesLocked(String packageName, Set<String> filterByClasses, boolean evenPersistent, boolean doit, boolean killProcess, ArrayMap<ComponentName, ServiceRecord> services) {
        boolean didSomething = false;
        for (int i = services.size() - 1; i >= 0; i--) {
            boolean sameComponent;
            ServiceRecord service = (ServiceRecord) services.valueAt(i);
            if (packageName == null) {
                sameComponent = true;
            } else if (!service.packageName.equals(packageName)) {
                sameComponent = false;
            } else if (filterByClasses != null) {
                sameComponent = filterByClasses.contains(service.name.getClassName());
            } else {
                sameComponent = true;
            }
            if (sameComponent && (service.app == null || evenPersistent || !service.app.persistent)) {
                if (!doit) {
                    return true;
                }
                didSomething = true;
                Slog.i(TAG, "  Force stopping service " + service);
                if (service.app != null) {
                    service.app.removed = killProcess;
                    if (!service.app.persistent) {
                        service.app.services.remove(service);
                        if (service.whitelistManager) {
                            updateWhitelistManagerLocked(service.app);
                        }
                    }
                }
                service.app = null;
                service.isolatedProc = null;
                if (this.mTmpCollectionResults == null) {
                    this.mTmpCollectionResults = new ArrayList();
                }
                this.mTmpCollectionResults.add(service);
            }
        }
        return didSomething;
    }

    boolean bringDownDisabledPackageServicesLocked(String packageName, Set<String> filterByClasses, int userId, boolean evenPersistent, boolean killProcess, boolean doit) {
        int i;
        boolean didSomething = false;
        if (this.mTmpCollectionResults != null) {
            this.mTmpCollectionResults.clear();
        }
        if (userId == -1) {
            for (i = this.mServiceMap.size() - 1; i >= 0; i--) {
                didSomething |= collectPackageServicesLocked(packageName, filterByClasses, evenPersistent, doit, killProcess, ((ServiceMap) this.mServiceMap.valueAt(i)).mServicesByName);
                if (!doit && didSomething) {
                    return true;
                }
            }
        } else {
            ServiceMap smap = (ServiceMap) this.mServiceMap.get(userId);
            if (smap != null) {
                didSomething = collectPackageServicesLocked(packageName, filterByClasses, evenPersistent, doit, killProcess, smap.mServicesByName);
            }
        }
        if (this.mTmpCollectionResults != null) {
            if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                Slog.i(TAG, "packageName " + packageName + " size " + this.mTmpCollectionResults.size() + " " + Debug.getCallers(8));
            }
            for (i = this.mTmpCollectionResults.size() - 1; i >= 0; i--) {
                if (i < this.mTmpCollectionResults.size()) {
                    bringDownServiceLocked((ServiceRecord) this.mTmpCollectionResults.get(i));
                } else if (DEBUG_AGING_VERSION || DEBUG_ACTIVE_SERVICES || ActivityManagerDebugConfig.DEBUG_SERVICE) {
                    Slog.w(TAG, "Index is error " + i + " " + Debug.getCallers(8));
                }
            }
            this.mTmpCollectionResults.clear();
        }
        return didSomething;
    }

    void cleanUpRemovedTaskLocked(TaskRecord tr, ComponentName component, Intent baseIntent) {
        int i;
        ServiceRecord sr;
        ArrayList<ServiceRecord> services = new ArrayList();
        ArrayMap<ComponentName, ServiceRecord> alls = getServices(tr.userId);
        for (i = alls.size() - 1; i >= 0; i--) {
            sr = (ServiceRecord) alls.valueAt(i);
            if (sr.packageName.equals(component.getPackageName())) {
                services.add(sr);
            }
        }
        for (i = services.size() - 1; i >= 0; i--) {
            sr = (ServiceRecord) services.get(i);
            if (sr.startRequested) {
                if ((sr.serviceInfo.flags & 1) != 0) {
                    Slog.i(TAG, "Stopping service " + sr.shortName + ": remove task");
                    stopServiceLocked(sr);
                } else {
                    sr.pendingStarts.add(new StartItem(sr, true, sr.makeNextStartId(), baseIntent, null));
                    if (!(sr.app == null || sr.app.thread == null)) {
                        try {
                            sendServiceArgsLocked(sr, true, false);
                        } catch (TransactionTooLargeException e) {
                        }
                    }
                }
            }
        }
    }

    final void killServicesLocked(ProcessRecord app, boolean allowRestart) {
        int i;
        ServiceRecord sr;
        for (i = app.connections.size() - 1; i >= 0; i--) {
            removeConnectionLocked((ConnectionRecord) app.connections.valueAt(i), app, null);
        }
        updateServiceConnectionActivitiesLocked(app);
        app.connections.clear();
        app.whitelistManager = false;
        for (i = app.services.size() - 1; i >= 0; i--) {
            sr = (ServiceRecord) app.services.valueAt(i);
            synchronized (sr.stats.getBatteryStats()) {
                sr.stats.stopLaunchedLocked();
            }
            if (!(sr.app == app || sr.app == null || sr.app.persistent)) {
                sr.app.services.remove(sr);
            }
            sr.app = null;
            sr.isolatedProc = null;
            sr.executeNesting = 0;
            sr.forceClearTracker();
            if (this.mDestroyingServices.remove(sr) && ActivityManagerDebugConfig.DEBUG_SERVICE) {
                Slog.v(TAG_SERVICE, "killServices remove destroying " + sr);
            }
            for (int bindingi = sr.bindings.size() - 1; bindingi >= 0; bindingi--) {
                IntentBindRecord b = (IntentBindRecord) sr.bindings.valueAt(bindingi);
                if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                    Slog.v(TAG_SERVICE, "Killing binding " + b + ": shouldUnbind=" + b.hasBound);
                }
                b.binder = null;
                b.hasBound = false;
                b.received = false;
                b.requested = false;
                for (int appi = b.apps.size() - 1; appi >= 0; appi--) {
                    ProcessRecord proc = (ProcessRecord) b.apps.keyAt(appi);
                    if (!(proc.killedByAm || proc.thread == null)) {
                        AppBindRecord abind = (AppBindRecord) b.apps.valueAt(appi);
                        boolean hasCreate = false;
                        for (int conni = abind.connections.size() - 1; conni >= 0; conni--) {
                            if ((((ConnectionRecord) abind.connections.valueAt(conni)).flags & 49) == 1) {
                                hasCreate = true;
                                break;
                            }
                        }
                        if (!hasCreate) {
                        }
                    }
                }
            }
        }
        ServiceMap smap = getServiceMap(app.userId);
        for (i = app.services.size() - 1; i >= 0; i--) {
            sr = (ServiceRecord) app.services.valueAt(i);
            if (!app.persistent) {
                app.services.removeAt(i);
            }
            ServiceRecord curRec = (ServiceRecord) smap.mServicesByName.get(sr.name);
            Integer[] numArr;
            if (curRec != sr) {
                if (curRec != null) {
                    Slog.e(TAG, "Service " + sr + " in process " + app + " not same as in map: " + curRec);
                }
            } else if (allowRestart && sr.crashCount >= 2 && (sr.serviceInfo.applicationInfo.flags & 8) == 0) {
                Slog.w(TAG, "Service crashed " + sr.crashCount + " times, stopping: " + sr);
                numArr = new Object[4];
                numArr[0] = Integer.valueOf(sr.userId);
                numArr[1] = Integer.valueOf(sr.crashCount);
                numArr[2] = sr.shortName;
                numArr[3] = Integer.valueOf(app.pid);
                EventLog.writeEvent(EventLogTags.AM_SERVICE_CRASHED_TOO_MUCH, numArr);
                bringDownServiceLocked(sr);
            } else if (!allowRestart || !this.mAm.mUserController.isUserRunningLocked(sr.userId, 0)) {
                bringDownServiceLocked(sr);
            } else if (isCoreApp(app)) {
                if (app != this.mAm.mWallpaperProcess || this.mAm.mIsWallpaperFg) {
                    boolean canceled = scheduleServiceRestartLocked(sr, true);
                    if (!ActivityManagerService.IS_USER_BUILD || ActivityManagerDebugConfig.DEBUG_SERVICE) {
                        Slog.d(TAG_SERVICE, "killServicesLocked sr.startRequested: " + sr.startRequested + " sr.stopIfKilled: " + sr.stopIfKilled + " canceled: " + canceled);
                    }
                    if (sr.startRequested && ((sr.stopIfKilled || canceled) && sr.pendingStarts.size() == 0)) {
                        sr.startRequested = false;
                        if (sr.tracker != null) {
                            sr.tracker.setStarted(false, this.mAm.mProcessStats.getMemFactorLocked(), SystemClock.uptimeMillis());
                        }
                        if (!sr.hasAutoCreateConnections()) {
                            if (!ActivityManagerService.IS_USER_BUILD || ActivityManagerDebugConfig.DEBUG_SERVICE) {
                                Slog.d(TAG_SERVICE, "killServicesLocked no reason to restart");
                            }
                            bringDownServiceLocked(sr);
                        }
                    }
                } else {
                    bringDownServiceLocked(sr);
                    this.mAm.mWallpaperProcess = null;
                }
            } else {
                Slog.w(TAG, "Service crashed " + sr.crashCount + " times, stopping: " + sr + "this is not core app" + app.processName);
                numArr = new Object[4];
                numArr[0] = Integer.valueOf(sr.crashCount);
                numArr[1] = sr.shortName;
                numArr[2] = Integer.valueOf(app.pid);
                numArr[3] = app.processName;
                EventLog.writeEvent(EventLogTags.AM_DESTROY_SERVICE, numArr);
                bringDownServiceLocked(sr);
            }
        }
        if (!allowRestart) {
            ServiceRecord r;
            app.services.clear();
            for (i = this.mRestartingServices.size() - 1; i >= 0; i--) {
                r = (ServiceRecord) this.mRestartingServices.get(i);
                if (r.processName.equals(app.processName) && r.serviceInfo.applicationInfo.uid == app.info.uid) {
                    this.mRestartingServices.remove(i);
                    clearRestartingIfNeededLocked(r);
                }
            }
            for (i = this.mPendingServices.size() - 1; i >= 0; i--) {
                r = (ServiceRecord) this.mPendingServices.get(i);
                if (r.processName.equals(app.processName) && r.serviceInfo.applicationInfo.uid == app.info.uid) {
                    this.mPendingServices.remove(i);
                }
            }
        }
        i = this.mDestroyingServices.size();
        while (i > 0) {
            i--;
            sr = (ServiceRecord) this.mDestroyingServices.get(i);
            if (sr.app == app) {
                sr.forceClearTracker();
                this.mDestroyingServices.remove(i);
                if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                    Slog.v(TAG_SERVICE, "killServices remove destroying " + sr);
                }
            }
        }
        app.executingServices.clear();
    }

    private boolean isCoreApp(ProcessRecord app) {
        if (!(app.processName == null || app.info == null || app.info.packageName == null)) {
            for (String str : OppoListManager.getInstance().getKillRestartServicePkgList(this.mAm.mContext)) {
                if (str != null && app.info.packageName.equals(str)) {
                    boolean bootAllow = OppoListManager.getInstance().isInAutoBootWhiteList(app.info.packageName);
                    boolean inRecent = this.mAm.isPkgInRecentTasks(app.info.packageName);
                    Slog.d(TAG, app.processName + " bA=" + bootAllow + ", iR=" + inRecent);
                    if (!bootAllow && !inRecent) {
                        return false;
                    }
                    Slog.d(TAG, "allow restart service in proc " + app.processName);
                    return true;
                }
            }
            if (OppoListManager.getInstance().getGlobalProcessWhiteList(this.mAm.mContext).contains(app.processName)) {
                return true;
            }
            if (OppoListManager.getInstance().isInstalledAppWidget(app.info.packageName)) {
                Slog.d(TAG, "allow restart service in widget proc " + app.processName);
                return true;
            }
        }
        return (app.info.flags & 1) != 0 || app.curAdj <= DisplayTransformManager.LEVEL_COLOR_MATRIX_GRAYSCALE;
    }

    RunningServiceInfo makeRunningServiceInfoLocked(ServiceRecord r) {
        RunningServiceInfo info = new RunningServiceInfo();
        info.service = r.name;
        if (r.app != null) {
            info.pid = r.app.pid;
        }
        info.uid = r.appInfo.uid;
        info.process = r.processName;
        info.foreground = r.isForeground;
        info.activeSince = r.createTime;
        info.started = r.startRequested;
        info.clientCount = r.connections.size();
        info.crashCount = r.crashCount;
        info.lastActivityTime = r.lastActivity;
        if (r.isForeground) {
            info.flags |= 2;
        }
        if (r.startRequested) {
            info.flags |= 1;
        }
        if (r.app != null && r.app.pid == ActivityManagerService.MY_PID) {
            info.flags |= 4;
        }
        if (r.app != null && r.app.persistent) {
            info.flags |= 8;
        }
        for (int conni = r.connections.size() - 1; conni >= 0; conni--) {
            ArrayList<ConnectionRecord> connl = (ArrayList) r.connections.valueAt(conni);
            for (int i = 0; i < connl.size(); i++) {
                ConnectionRecord conn = (ConnectionRecord) connl.get(i);
                if (conn.clientLabel != 0) {
                    info.clientPackage = conn.binding.client.info.packageName;
                    info.clientLabel = conn.clientLabel;
                    return info;
                }
            }
        }
        return info;
    }

    List<RunningServiceInfo> getRunningServiceInfoLocked(int maxNum, int flags) {
        ArrayList<RunningServiceInfo> res = new ArrayList();
        int uid = Binder.getCallingUid();
        long ident = Binder.clearCallingIdentity();
        try {
            ArrayMap<ComponentName, ServiceRecord> alls;
            int i;
            ServiceRecord sr;
            ServiceRecord r;
            RunningServiceInfo info;
            if (ActivityManager.checkUidPermission("android.permission.INTERACT_ACROSS_USERS_FULL", uid) == 0) {
                int[] users = this.mAm.mUserController.getUsers();
                for (int ui = 0; ui < users.length && res.size() < maxNum; ui++) {
                    alls = getServices(users[ui]);
                    for (i = 0; i < alls.size() && res.size() < maxNum; i++) {
                        sr = (ServiceRecord) alls.valueAt(i);
                        if (!this.mCtmFlag || !sr.packageName.equals(this.mCtmName)) {
                            res.add(makeRunningServiceInfoLocked(sr));
                        }
                    }
                }
                for (i = 0; i < this.mRestartingServices.size() && res.size() < maxNum; i++) {
                    r = (ServiceRecord) this.mRestartingServices.get(i);
                    if (!this.mCtmFlag || !r.packageName.equals(this.mCtmName)) {
                        info = makeRunningServiceInfoLocked(r);
                        info.restarting = r.nextRestartTime;
                        res.add(info);
                    }
                }
            } else {
                int userId = UserHandle.getUserId(uid);
                alls = getServices(userId);
                for (i = 0; i < alls.size() && res.size() < maxNum; i++) {
                    sr = (ServiceRecord) alls.valueAt(i);
                    if (!this.mCtmFlag || !sr.packageName.equals(this.mCtmName)) {
                        res.add(makeRunningServiceInfoLocked(sr));
                    }
                }
                for (i = 0; i < this.mRestartingServices.size() && res.size() < maxNum; i++) {
                    r = (ServiceRecord) this.mRestartingServices.get(i);
                    if (!(this.mCtmFlag && r.packageName.equals(this.mCtmName)) && r.userId == userId) {
                        info = makeRunningServiceInfoLocked(r);
                        info.restarting = r.nextRestartTime;
                        res.add(info);
                    }
                }
            }
            Binder.restoreCallingIdentity(ident);
            return res;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public PendingIntent getRunningServiceControlPanelLocked(ComponentName name) {
        ServiceRecord r = getServiceByName(name, UserHandle.getUserId(Binder.getCallingUid()));
        if (r != null) {
            for (int conni = r.connections.size() - 1; conni >= 0; conni--) {
                ArrayList<ConnectionRecord> conn = (ArrayList) r.connections.valueAt(conni);
                for (int i = 0; i < conn.size(); i++) {
                    if (((ConnectionRecord) conn.get(i)).clientIntent != null) {
                        return ((ConnectionRecord) conn.get(i)).clientIntent;
                    }
                }
            }
        }
        return null;
    }

    /* JADX WARNING: Missing block: B:23:0x00e1, code:
            com.android.server.am.ActivityManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Missing block: B:24:0x00e4, code:
            if (r7 == null) goto L_0x013e;
     */
    /* JADX WARNING: Missing block: B:26:0x00ea, code:
            if (com.android.server.am.OppoProcessManagerHelper.checkProcessWhileTimeout(r23) == false) goto L_0x0130;
     */
    /* JADX WARNING: Missing block: B:27:0x00ec, code:
            return;
     */
    /* JADX WARNING: Missing block: B:41:0x0130, code:
            r22.mAm.mAppErrors.appNotResponding(r23, null, null, false, r7);
     */
    /* JADX WARNING: Missing block: B:42:0x013e, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "ZhiYong.Lin@Plf.Framework, modify for BPM", property = OppoRomType.ROM)
    void serviceTimeout(ProcessRecord proc) {
        String anrMessage = null;
        synchronized (this.mAm) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                if (proc.executingServices.size() == 0 || proc.thread == null) {
                } else {
                    long maxTime = SystemClock.uptimeMillis() - ((long) (proc.execServicesFg ? SERVICE_TIMEOUT : SERVICE_BACKGROUND_TIMEOUT));
                    ServiceRecord timeout = null;
                    long nextTime = 0;
                    for (int i = proc.executingServices.size() - 1; i >= 0; i--) {
                        ServiceRecord sr = (ServiceRecord) proc.executingServices.valueAt(i);
                        if (sr.executingStart < maxTime) {
                            timeout = sr;
                            break;
                        }
                        if (sr.executingStart > nextTime) {
                            nextTime = sr.executingStart;
                        }
                    }
                    if (timeout == null || !this.mAm.mLruProcesses.contains(proc)) {
                        long j;
                        Message msg = this.mAm.mHandler.obtainMessage(12);
                        msg.obj = proc;
                        MainHandler mainHandler = this.mAm.mHandler;
                        if (proc.execServicesFg) {
                            j = 20000 + nextTime;
                        } else {
                            j = 200000 + nextTime;
                        }
                        mainHandler.sendMessageAtTime(msg, j);
                    } else {
                        Slog.w(TAG, "Timeout executing service: " + timeout);
                        Writer sw = new StringWriter();
                        PrintWriter fastPrintWriter = new FastPrintWriter(sw, false, 1024);
                        fastPrintWriter.println(timeout);
                        timeout.dump(fastPrintWriter, "    ");
                        fastPrintWriter.close();
                        this.mLastAnrDump = sw.toString();
                        this.mAm.mHandler.removeCallbacks(this.mLastAnrDumpClearer);
                        this.mAm.mHandler.postDelayed(this.mLastAnrDumpClearer, 7200000);
                        anrMessage = "executing service " + timeout.shortName;
                    }
                }
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    void scheduleServiceTimeoutLocked(ProcessRecord proc) {
        if (proc.executingServices.size() != 0 && proc.thread != null) {
            long now = SystemClock.uptimeMillis();
            Message msg = this.mAm.mHandler.obtainMessage(12);
            msg.obj = proc;
            this.mAm.mHandler.sendMessageAtTime(msg, proc.execServicesFg ? 20000 + now : 200000 + now);
            if (2 == ANRManager.enableANRDebuggingMechanism()) {
                this.mAm.mAnrHandler.sendMessageAtTime(this.mAm.mAnrHandler.obtainMessage(1002, proc), 13333 + now);
            }
        }
    }

    List<ServiceRecord> collectServicesToDumpLocked(ItemMatcher matcher, String dumpPackage) {
        ArrayList<ServiceRecord> services = new ArrayList();
        for (int user : this.mAm.mUserController.getUsers()) {
            ServiceMap smap = getServiceMap(user);
            if (smap.mServicesByName.size() > 0) {
                for (int si = 0; si < smap.mServicesByName.size(); si++) {
                    ServiceRecord r = (ServiceRecord) smap.mServicesByName.valueAt(si);
                    if (matcher.match(r, r.name) && (dumpPackage == null || dumpPackage.equals(r.appInfo.packageName))) {
                        services.add(r);
                    }
                }
            }
        }
        return services;
    }

    ServiceDumper newServiceDumperLocked(FileDescriptor fd, PrintWriter pw, String[] args, int opti, boolean dumpAll, String dumpPackage) {
        return new ServiceDumper(fd, pw, args, opti, dumpAll, dumpPackage);
    }

    protected boolean dumpService(FileDescriptor fd, PrintWriter pw, String name, String[] args, int opti, boolean dumpAll) {
        int i;
        ArrayList<ServiceRecord> services = new ArrayList();
        synchronized (this.mAm) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                int[] users = this.mAm.mUserController.getUsers();
                ServiceMap smap;
                ArrayMap<ComponentName, ServiceRecord> alls;
                if ("all".equals(name)) {
                    for (int user : users) {
                        smap = (ServiceMap) this.mServiceMap.get(user);
                        if (smap != null) {
                            alls = smap.mServicesByName;
                            for (i = 0; i < alls.size(); i++) {
                                services.add((ServiceRecord) alls.valueAt(i));
                            }
                        }
                    }
                } else {
                    Object componentName = name != null ? ComponentName.unflattenFromString(name) : null;
                    int objectId = 0;
                    if (componentName == null) {
                        objectId = Integer.parseInt(name, 16);
                        name = null;
                        componentName = null;
                    }
                    for (int user2 : users) {
                        smap = (ServiceMap) this.mServiceMap.get(user2);
                        if (smap != null) {
                            alls = smap.mServicesByName;
                            for (i = 0; i < alls.size(); i++) {
                                ServiceRecord r1 = (ServiceRecord) alls.valueAt(i);
                                if (componentName != null) {
                                    if (r1.name.equals(componentName)) {
                                        services.add(r1);
                                    }
                                } else if (name != null) {
                                    if (r1.name.flattenToString().contains(name)) {
                                        services.add(r1);
                                    }
                                } else if (System.identityHashCode(r1) == objectId) {
                                    services.add(r1);
                                }
                            }
                        }
                    }
                }
            } catch (RuntimeException e) {
            } catch (Throwable th) {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
        ActivityManagerService.resetPriorityAfterLockedSection();
        if (services.size() <= 0) {
            return false;
        }
        boolean needSep = false;
        for (i = 0; i < services.size(); i++) {
            if (needSep) {
                pw.println();
            }
            needSep = true;
            dumpService(IElsaManager.EMPTY_PACKAGE, fd, pw, (ServiceRecord) services.get(i), args, dumpAll);
        }
        return true;
    }

    private void dumpService(String prefix, FileDescriptor fd, PrintWriter pw, ServiceRecord r, String[] args, boolean dumpAll) {
        String innerPrefix = prefix + "  ";
        synchronized (this.mAm) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                pw.print(prefix);
                pw.print("SERVICE ");
                pw.print(r.shortName);
                pw.print(" ");
                pw.print(Integer.toHexString(System.identityHashCode(r)));
                pw.print(" pid=");
                if (r.app != null) {
                    pw.println(r.app.pid);
                } else {
                    pw.println("(not running)");
                }
                if (dumpAll) {
                    r.dump(pw, innerPrefix);
                }
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
        if (r.app != null && r.app.thread != null) {
            pw.print(prefix);
            pw.println("  Client:");
            pw.flush();
            TransferPipe tp;
            try {
                tp = new TransferPipe();
                r.app.thread.dumpService(tp.getWriteFd().getFileDescriptor(), r, args);
                tp.setBufferPrefix(prefix + "    ");
                tp.go(fd);
                tp.kill();
            } catch (IOException e) {
                pw.println(prefix + "    Failure while dumping the service: " + e);
            } catch (RemoteException e2) {
                pw.println(prefix + "    Got a RemoteException while dumping the service");
            } catch (Throwable th) {
                tp.kill();
            }
        }
    }

    public void dynaicallyUpdateLogTag(boolean on) {
        DEBUG_DELAYED_SERVICE = on;
        DEBUG_DELAYED_STARTS = on;
        LOG_SERVICE_START_STOP = on;
    }
}
