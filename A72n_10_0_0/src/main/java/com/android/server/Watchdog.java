package com.android.server;

import android.app.IActivityController;
import android.common.OppoFeatureCache;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.biometrics.face.V1_0.IBiometricsFace;
import android.hardware.health.V2_0.IHealth;
import android.hidl.manager.V1_0.IServiceManager;
import android.os.Binder;
import android.os.Build;
import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import android.os.OppoBinderRecorder;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.system.StructRlimit;
import android.util.EventLog;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import com.android.internal.os.ProcessCpuTracker;
import com.android.server.am.ActivityManagerService;
import com.android.server.am.IColorHansManager;
import com.android.server.biometrics.face.health.HealthMonitor;
import com.android.server.wm.SurfaceAnimationThread;
import com.mediatek.aee.ExceptionLog;
import com.oppo.phoenix.Phoenix;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Watchdog extends OppoBaseWatchdog {
    static final long CHECK_INTERVAL = 30000;
    static final int COMPLETED = 0;
    static final boolean DB = false;
    public static final boolean DEBUG = false;
    static final long DEFAULT_TIMEOUT = 60000;
    public static final List<String> HAL_INTERFACES_OF_INTEREST = Arrays.asList("android.hardware.audio@2.0::IDevicesFactory", "android.hardware.audio@4.0::IDevicesFactory", "android.hardware.bluetooth@1.0::IBluetoothHci", "android.hardware.camera.provider@2.4::ICameraProvider", "android.hardware.graphics.allocator@2.0::IAllocator", "android.hardware.graphics.composer@2.1::IComposer", IHealth.kInterfaceName, "android.hardware.media.c2@1.0::IComponentStore", "android.hardware.media.omx@1.0::IOmx", "android.hardware.media.omx@1.0::IOmxStore", "android.hardware.sensors@1.0::ISensors", "android.hardware.vr@1.0::IVr", IBiometricsFace.kInterfaceName);
    public static final String[] NATIVE_STACKS_OF_INTEREST = {"/system/bin/audioserver", HealthMonitor.CAMERA_NATIVE_NAME, "/system/bin/drmserver", "/system/bin/mediadrmserver", "/system/bin/mediaserver", "/system/bin/sdcard", "/system/bin/surfaceflinger", "/system/bin/vold", "media.extractor", "media.metrics", "media.codec", "media.swcodec", "com.android.bluetooth", "/system/bin/statsd", "zygote64", "zygote", "media.metrics", "/system/bin/neo"};
    static final int OVERDUE = 3;
    static final String TAG = "Watchdog";
    static final int TIME_SF_WAIT = 20000;
    static final int WAITED_HALF = 2;
    static final int WAITING = 1;
    protected static final ProcessCpuTracker mProcessStats = new ProcessCpuTracker(true);
    static Watchdog sWatchdog;
    ExceptionLog exceptionHWT;
    ActivityManagerService mActivity;
    boolean mAllowRestart = true;
    IActivityController mController;
    final ArrayList<HandlerChecker> mHandlerCheckers = new ArrayList<>();
    final HandlerChecker mMonitorChecker = new HandlerChecker(FgThread.getHandler(), "foreground thread", 60000);
    final OpenFdMonitor mOpenFdMonitor;
    int mPhonePid;
    boolean mSFHang = false;

    public interface Monitor {
        void monitor();
    }

    public long GetSFStatus() {
        ExceptionLog exceptionLog = this.exceptionHWT;
        if (exceptionLog != null) {
            return exceptionLog.SFMatterJava(0, 0);
        }
        return 0;
    }

    public static int GetSFReboot() {
        return SystemProperties.getInt("service.sf.reboot", 0);
    }

    public static void SetSFReboot() {
        int OldTime = SystemProperties.getInt("service.sf.reboot", 0) + 1;
        if (OldTime > 9) {
            OldTime = 9;
        }
        SystemProperties.set("service.sf.reboot", String.valueOf(OldTime));
    }

    public final class HandlerChecker implements Runnable {
        private boolean mCompleted;
        private Monitor mCurrentMonitor;
        private final Handler mHandler;
        private final ArrayList<Monitor> mMonitorQueue = new ArrayList<>();
        private final ArrayList<Monitor> mMonitors = new ArrayList<>();
        private final String mName;
        private int mPauseCount;
        private long mStartTime;
        private final long mWaitMax;

        HandlerChecker(Handler handler, String name, long waitMaxMillis) {
            this.mHandler = handler;
            this.mName = name;
            this.mWaitMax = waitMaxMillis;
            this.mCompleted = true;
        }

        /* access modifiers changed from: package-private */
        public void addMonitorLocked(Monitor monitor) {
            if (monitor == null) {
                Watchdog.this.onAddMonitorCheck(getThread());
            }
            this.mMonitorQueue.add(monitor);
        }

        public void scheduleCheckLocked() {
            if (this.mCompleted) {
                this.mMonitors.addAll(this.mMonitorQueue);
                this.mMonitorQueue.clear();
            }
            if ((this.mMonitors.size() == 0 && this.mHandler.getLooper().getQueue().isPolling()) || this.mPauseCount > 0) {
                this.mCompleted = true;
            } else if (this.mCompleted) {
                this.mCompleted = false;
                this.mCurrentMonitor = null;
                this.mStartTime = SystemClock.uptimeMillis();
                this.mHandler.postAtFrontOfQueue(this);
            }
        }

        /* access modifiers changed from: package-private */
        public boolean isOverdueLocked() {
            return !this.mCompleted && SystemClock.uptimeMillis() > this.mStartTime + this.mWaitMax;
        }

        public int getCompletionStateLocked() {
            if (this.mCompleted) {
                return 0;
            }
            long latency = SystemClock.uptimeMillis() - this.mStartTime;
            long j = this.mWaitMax;
            if (latency < j / 2) {
                return 1;
            }
            if (latency < j) {
                return 2;
            }
            return 3;
        }

        public Thread getThread() {
            return this.mHandler.getLooper().getThread();
        }

        public String getName() {
            return this.mName;
        }

        /* access modifiers changed from: package-private */
        public String describeBlockedStateLocked() {
            if (this.mCurrentMonitor == null) {
                return "Blocked in handler on " + this.mName + " (" + getThread().getName() + ")";
            }
            return "Blocked in monitor " + this.mCurrentMonitor.getClass().getName() + " on " + this.mName + " (" + getThread().getName() + ")";
        }

        public void run() {
            int size = this.mMonitors.size();
            for (int i = 0; i < size; i++) {
                synchronized (Watchdog.this) {
                    this.mCurrentMonitor = this.mMonitors.get(i);
                }
                Monitor monitor = this.mCurrentMonitor;
                if (monitor != null) {
                    monitor.monitor();
                }
            }
            synchronized (Watchdog.this) {
                this.mCompleted = true;
                this.mCurrentMonitor = null;
            }
        }

        public void pauseLocked(String reason) {
            this.mPauseCount++;
            this.mCompleted = true;
            Slog.i(Watchdog.TAG, "Pausing HandlerChecker: " + this.mName + " for reason: " + reason + ". Pause count: " + this.mPauseCount);
        }

        public void resumeLocked(String reason) {
            int i = this.mPauseCount;
            if (i > 0) {
                this.mPauseCount = i - 1;
                Slog.i(Watchdog.TAG, "Resuming HandlerChecker: " + this.mName + " for reason: " + reason + ". Pause count: " + this.mPauseCount);
                return;
            }
            Slog.wtf(Watchdog.TAG, "Already resumed HandlerChecker: " + this.mName);
        }
    }

    /* access modifiers changed from: package-private */
    public final class RebootRequestReceiver extends BroadcastReceiver {
        RebootRequestReceiver() {
        }

        public void onReceive(Context c, Intent intent) {
            if (intent.getIntExtra("nowait", 0) != 0) {
                Watchdog.this.rebootSystem("Received ACTION_REBOOT broadcast");
                return;
            }
            Slog.w(Watchdog.TAG, "Unsupported ACTION_REBOOT broadcast: " + intent);
        }
    }

    private static final class BinderThreadMonitor implements Monitor {
        private BinderThreadMonitor() {
        }

        @Override // com.android.server.Watchdog.Monitor
        public void monitor() {
            Binder.blockUntilThreadAvailable();
        }
    }

    public static Watchdog getInstance() {
        if (sWatchdog == null) {
            sWatchdog = new Watchdog();
        }
        return sWatchdog;
    }

    private Watchdog() {
        super("watchdog");
        this.mHandlerCheckers.add(this.mMonitorChecker);
        this.mHandlerCheckers.add(new HandlerChecker(new Handler(Looper.getMainLooper()), "main thread", 60000));
        this.mHandlerCheckers.add(new HandlerChecker(UiThread.getHandler(), "ui thread", 60000));
        this.mHandlerCheckers.add(new HandlerChecker(IoThread.getHandler(), "i/o thread", 60000));
        this.mHandlerCheckers.add(new HandlerChecker(DisplayThread.getHandler(), "display thread", 60000));
        this.mHandlerCheckers.add(new HandlerChecker(AnimationThread.getHandler(), "animation thread", 60000));
        this.mHandlerCheckers.add(new HandlerChecker(SurfaceAnimationThread.getHandler(), "surface animation thread", 60000));
        addMonitor(new BinderThreadMonitor());
        this.mOpenFdMonitor = OpenFdMonitor.create();
        this.exceptionHWT = ExceptionLog.getInstance();
    }

    public void init(Context context, ActivityManagerService activity) {
        this.mActivity = activity;
        context.registerReceiver(new RebootRequestReceiver(), new IntentFilter("android.intent.action.REBOOT"), "android.permission.REBOOT", null);
        ExceptionLog exceptionLog = this.exceptionHWT;
        if (exceptionLog != null) {
            exceptionLog.WDTMatterJava(0);
        }
        onInit(context, activity);
    }

    public void processStarted(String name, int pid) {
        synchronized (this) {
            if ("com.android.phone".equals(name)) {
                this.mPhonePid = pid;
            }
            onProcessStarted(name, pid);
        }
    }

    public void setActivityController(IActivityController controller) {
        synchronized (this) {
            this.mController = controller;
        }
    }

    public void setAllowRestart(boolean allowRestart) {
        synchronized (this) {
            this.mAllowRestart = allowRestart;
        }
    }

    @Override // com.android.server.OppoBaseWatchdog
    public boolean getLastTimeWatchdogHappen() {
        boolean z;
        synchronized (this) {
            z = this.mLastTimeWatchdogHappen;
        }
        return z;
    }

    public void addMonitor(Monitor monitor) {
        synchronized (this) {
            this.mMonitorChecker.addMonitorLocked(monitor);
        }
    }

    public void addThread(Handler thread) {
        addThread(thread, 60000);
    }

    public void addThread(Handler thread, long timeoutMillis) {
        synchronized (this) {
            this.mHandlerCheckers.add(new HandlerChecker(thread, thread.getLooper().getThread().getName(), timeoutMillis));
        }
    }

    public void pauseWatchingCurrentThread(String reason) {
        synchronized (this) {
            Iterator<HandlerChecker> it = this.mHandlerCheckers.iterator();
            while (it.hasNext()) {
                HandlerChecker hc = it.next();
                if (Thread.currentThread().equals(hc.getThread())) {
                    hc.pauseLocked(reason);
                }
            }
        }
    }

    public void resumeWatchingCurrentThread(String reason) {
        synchronized (this) {
            Iterator<HandlerChecker> it = this.mHandlerCheckers.iterator();
            while (it.hasNext()) {
                HandlerChecker hc = it.next();
                if (Thread.currentThread().equals(hc.getThread())) {
                    hc.resumeLocked(reason);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void rebootSystem(String reason) {
        Slog.i(TAG, "Rebooting system because: " + reason);
        try {
            ServiceManager.getService("power").reboot(false, reason, false);
        } catch (RemoteException e) {
        }
    }

    private int evaluateCheckerCompletionLocked() {
        int state = 0;
        for (int i = 0; i < this.mHandlerCheckers.size(); i++) {
            state = Math.max(state, this.mHandlerCheckers.get(i).getCompletionStateLocked());
        }
        return state;
    }

    private ArrayList<HandlerChecker> getBlockedCheckersLocked() {
        ArrayList<HandlerChecker> checkers = new ArrayList<>();
        for (int i = 0; i < this.mHandlerCheckers.size(); i++) {
            HandlerChecker hc = this.mHandlerCheckers.get(i);
            if (hc.isOverdueLocked()) {
                checkers.add(hc);
            }
        }
        return checkers;
    }

    private String describeCheckersLocked(List<HandlerChecker> checkers) {
        StringBuilder builder = new StringBuilder(128);
        for (int i = 0; i < checkers.size(); i++) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(checkers.get(i).describeBlockedStateLocked());
        }
        return builder.toString();
    }

    private static ArrayList<Integer> getInterestingHalPids() {
        try {
            ArrayList<IServiceManager.InstanceDebugInfo> dump = IServiceManager.getService().debugDump();
            HashSet<Integer> pids = new HashSet<>();
            Iterator<IServiceManager.InstanceDebugInfo> it = dump.iterator();
            while (it.hasNext()) {
                IServiceManager.InstanceDebugInfo info = it.next();
                if (info.pid != -1) {
                    if (HAL_INTERFACES_OF_INTEREST.contains(info.interfaceName)) {
                        pids.add(Integer.valueOf(info.pid));
                    }
                }
            }
            return new ArrayList<>(pids);
        } catch (RemoteException e) {
            return new ArrayList<>();
        }
    }

    static ArrayList<Integer> getInterestingNativePids() {
        ArrayList<Integer> pids = getInterestingHalPids();
        int[] nativePids = Process.getPidsForCommands(NATIVE_STACKS_OF_INTEREST);
        if (nativePids != null) {
            pids.ensureCapacity(pids.size() + nativePids.length);
            for (int i : nativePids) {
                pids.add(Integer.valueOf(i));
            }
        }
        return pids;
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX INFO: Multiple debug info for r0v9 boolean: [D('fdLimitTriggered' boolean), D('allowRestart' boolean)] */
    public void run() {
        List<HandlerChecker> blockedCheckers;
        String subject;
        IActivityController controller;
        maybeRecordPerfInfo();
        this.mSFHang = false;
        mProcessStats.init();
        boolean waitedHalf = false;
        while (true) {
            OppoCheckBlockedException.getInstance().setContext(this.mContext);
            OppoCheckBlockedException.getInstance().triggerDetect();
            this.mSFHang = false;
            ExceptionLog exceptionLog = this.exceptionHWT;
            if (exceptionLog != null && !waitedHalf) {
                exceptionLog.WDTMatterJava(300);
            }
            int debuggerWasConnected = 0;
            synchronized (this) {
                long timeout = 30000;
                syncAppAndKernelTime();
                for (int i = 0; i < this.mHandlerCheckers.size(); i++) {
                    this.mHandlerCheckers.get(i).scheduleCheckLocked();
                }
                if (0 > 0) {
                    debuggerWasConnected = 0 - 1;
                }
                long start = SystemClock.uptimeMillis();
                while (timeout > 0) {
                    if (Debug.isDebuggerConnected()) {
                        debuggerWasConnected = 2;
                    }
                    try {
                        wait(timeout);
                    } catch (InterruptedException e) {
                        Log.wtf(TAG, e);
                    }
                    if (Debug.isDebuggerConnected()) {
                        debuggerWasConnected = 2;
                    }
                    long end = SystemClock.uptimeMillis();
                    timeout = 30000 - (end - start);
                    if (timeout > 30000) {
                        Slog.e(TAG, "timeout=" + timeout + ", end=" + end + ",start=" + start + " are abnormal, set timeout to default 30s");
                        StringBuilder sb = new StringBuilder();
                        sb.append("try again to get uptimeMillis:");
                        sb.append(SystemClock.uptimeMillis());
                        Slog.e(TAG, sb.toString());
                        timeout = 30000;
                    }
                }
                long SFHangTime = GetSFStatus();
                if (SFHangTime > 40000) {
                    Slog.v(TAG, "**SF hang Time **" + SFHangTime);
                    this.mSFHang = true;
                    subject = "";
                    blockedCheckers = getBlockedCheckersLocked();
                } else {
                    boolean fdLimitTriggered = false;
                    if (this.mOpenFdMonitor != null) {
                        fdLimitTriggered = this.mOpenFdMonitor.monitor();
                    }
                    checkSystemHeapMem();
                    if (!fdLimitTriggered) {
                        int waitState = evaluateCheckerCompletionLocked();
                        if (waitState == 0) {
                            waitedHalf = false;
                        } else if (waitState != 1) {
                            if (waitState != 2) {
                                blockedCheckers = getBlockedCheckersLocked();
                                subject = describeCheckersLocked(blockedCheckers);
                            } else if (!waitedHalf) {
                                Slog.i(TAG, "WAITED_HALF");
                                if (this.exceptionHWT != null) {
                                    this.exceptionHWT.WDTMatterJava(360);
                                }
                                ArrayList<Integer> pids = new ArrayList<>();
                                pids.add(Integer.valueOf(Process.myPid()));
                                ActivityManagerService.dumpStackTraces(pids, (ProcessCpuTracker) null, (SparseArray<Boolean>) null, getInterestingNativePids());
                                mProcessStats.update();
                                waitedHalf = true;
                                Log.p("Quality", "01 11 ");
                            }
                        }
                    } else {
                        blockedCheckers = Collections.emptyList();
                        subject = "Open FD high water mark reached";
                    }
                }
                boolean allowRestart = this.mAllowRestart;
                Slog.e(TAG, "**SWT happen **" + subject);
                ExceptionLog exceptionLog2 = this.exceptionHWT;
                if (exceptionLog2 != null) {
                    exceptionLog2.switchFtrace(2);
                }
                dumpStackAndAddDropbox(subject);
                if (!Phoenix.isBootCompleted() && !Phoenix.isSwtHappened) {
                    Phoenix.isSwtHappened = true;
                    Phoenix.setBooterror("ERROR_SYSTEM_SERVER_WATCHDOG");
                    try {
                        Thread.sleep(10000);
                    } catch (Exception e2) {
                    }
                }
                Map<String, String> logMap = new HashMap<>();
                logMap.put("Subject", subject);
                if (this.mDcsUploader != null) {
                    this.mDcsUploader.storeLog("WatchdogSystemServer", logMap);
                }
                if (subject.indexOf("BinderThreadMonitor") >= 0) {
                    Map<String, String> logMap2 = OppoBinderRecorder.getInstance().getBinderUsageDscLogMap();
                    if (!(logMap2 == null || this.mDcsUploader == null)) {
                        this.mDcsUploader.storeLog("WatchdogBinderThread", logMap2);
                    }
                    Slog.w(TAG, "*** WatchdogBinderThread thread leak  !");
                }
                synchronized (this) {
                    controller = this.mController;
                }
                if (!this.mSFHang && controller != null) {
                    Slog.i(TAG, "Reporting stuck state to activity controller");
                    try {
                        Binder.setDumpDisabled("Service dumps disabled due to hung system process.");
                        if (controller.systemNotResponding(subject) >= 0) {
                            Slog.i(TAG, "Activity controller requested to coninue to wait");
                            waitedHalf = false;
                        }
                    } catch (RemoteException e3) {
                    }
                }
                if (Debug.isDebuggerConnected()) {
                    debuggerWasConnected = 2;
                }
                if (debuggerWasConnected >= 2) {
                    Slog.w(TAG, "Debugger connected: Watchdog is *not* killing the system process");
                } else if (debuggerWasConnected > 0) {
                    Slog.w(TAG, "Debugger was connected: Watchdog is *not* killing the system process");
                } else if (!allowRestart) {
                    Slog.w(TAG, "Restart not allowed: Watchdog is *not* killing the system process");
                } else {
                    Slog.w(TAG, "*** WATCHDOG KILLING SYSTEM PROCESS: " + subject);
                    WatchdogDiagnostics.diagnoseCheckers(blockedCheckers);
                    OppoFeatureCache.get(IColorHansManager.DEFAULT).unfreezeForWatchdog();
                    outputCurrentProcessTrace();
                    AgingCriticalEvent.getInstance().writeEvent(AgingCriticalEvent.EVENT_SYSTEMSERVER_WATCHDOG, subject);
                    Slog.w(TAG, "*** GOODBYE!");
                    this.exceptionHWT.WDTMatterJava(330);
                    if (this.mSFHang) {
                        Slog.w(TAG, "SF hang!");
                        if (GetSFReboot() > 3) {
                            Slog.w(TAG, "SF hang reboot time larger than 3 time, reboot device!");
                            rebootSystem("Maybe SF driver hang,reboot device.");
                        } else {
                            SetSFReboot();
                        }
                        Slog.v(TAG, "killing surfaceflinger for surfaceflinger hang");
                        int[] pid_sf = Process.getPidsForCommands(new String[]{"/system/bin/surfaceflinger"});
                        if (pid_sf[0] > 0) {
                            Process.killProcess(pid_sf[0]);
                        }
                        Slog.v(TAG, "killing surfaceflinger end");
                    } else {
                        Process.killProcess(Process.myPid());
                    }
                    SystemProperties.set("ctl.restart", "zygote_secondary");
                    try {
                        Thread.sleep(5000);
                    } catch (Exception e4) {
                    }
                    SystemProperties.set("ctl.restart", "zygote");
                }
                waitedHalf = false;
            }
        }
    }

    private void doSysRq(char c) {
        try {
            FileWriter sysrq_trigger = new FileWriter("/proc/sysrq-trigger");
            sysrq_trigger.write(c);
            sysrq_trigger.close();
        } catch (IOException e) {
            Slog.w(TAG, "Failed to write to /proc/sysrq-trigger", e);
        }
    }

    private void appendFile(File writeTo, File copyFrom) {
        try {
            BufferedReader in = new BufferedReader(new FileReader(copyFrom));
            FileWriter out = new FileWriter(writeTo, true);
            while (true) {
                String line = in.readLine();
                if (line != null) {
                    out.write(line);
                    out.write(10);
                } else {
                    in.close();
                    out.close();
                    return;
                }
            }
        } catch (IOException e) {
            Slog.e(TAG, "Exception while writing watchdog traces to new file!");
            e.printStackTrace();
        }
    }

    private void binderStateRead() {
        try {
            Slog.i(TAG, "Collecting Binder Transaction Status Information");
            BufferedReader in = new BufferedReader(new FileReader("/sys/kernel/debug/binder/state"));
            FileWriter out = new FileWriter("/data/anr/BinderTraces_pid" + String.valueOf(Process.myPid()) + ".txt");
            while (true) {
                String line = in.readLine();
                if (line != null) {
                    out.write(line);
                    out.write(10);
                } else {
                    in.close();
                    out.close();
                    return;
                }
            }
        } catch (IOException e) {
            Slog.w(TAG, "Failed to collect state file", e);
        }
    }

    public static final class OpenFdMonitor {
        private static final int FD_HIGH_WATER_MARK = 600;
        private final File mDumpDir;
        private final File mFdHighWaterMark;

        public static OpenFdMonitor create() {
            if (!Build.IS_DEBUGGABLE) {
                return null;
            }
            try {
                StructRlimit rlimit = Os.getrlimit(OsConstants.RLIMIT_NOFILE);
                return new OpenFdMonitor(new File(ActivityManagerService.ANR_TRACE_DIR), new File("/proc/self/fd/" + (rlimit.rlim_cur - 600)));
            } catch (ErrnoException errno) {
                Slog.w(Watchdog.TAG, "Error thrown from getrlimit(RLIMIT_NOFILE)", errno);
                return null;
            }
        }

        OpenFdMonitor(File dumpDir, File fdThreshold) {
            this.mDumpDir = dumpDir;
            this.mFdHighWaterMark = fdThreshold;
        }

        private void dumpOpenDescriptors() {
            String resolvedPath;
            List<String> dumpInfo = new ArrayList<>();
            String fdDirPath = String.format("/proc/%d/fd/", Integer.valueOf(Process.myPid()));
            File[] fds = new File(fdDirPath).listFiles();
            if (fds == null) {
                dumpInfo.add("Unable to list " + fdDirPath);
            } else {
                for (File f : fds) {
                    String fdSymLink = f.getAbsolutePath();
                    try {
                        resolvedPath = Os.readlink(fdSymLink);
                    } catch (ErrnoException ex) {
                        resolvedPath = ex.getMessage();
                    }
                    dumpInfo.add(fdSymLink + "\t" + resolvedPath);
                }
            }
            try {
                Files.write(Paths.get(File.createTempFile("anr_fd_", "", this.mDumpDir).getAbsolutePath(), new String[0]), dumpInfo, StandardCharsets.UTF_8, new OpenOption[0]);
            } catch (IOException ex2) {
                Slog.w(Watchdog.TAG, "Unable to write open descriptors to file: " + ex2);
            }
        }

        public boolean monitor() {
            if (!this.mFdHighWaterMark.exists()) {
                return false;
            }
            dumpOpenDescriptors();
            OppoBaseWatchdog.dumpHprof();
            return true;
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.OppoBaseWatchdog
    public int getPhonePid() {
        return this.mPhonePid;
    }

    @Override // com.android.server.OppoBaseWatchdog
    public ArrayList<Integer> onGetInterestingNativePids() {
        return getInterestingNativePids();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.OppoBaseWatchdog
    public void onBinderStateRead() {
        binderStateRead();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.OppoBaseWatchdog
    public void onDoSysRq(char c) {
        doSysRq(c);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.OppoBaseWatchdog
    public void onDumpStackForSurfaceFlingerHang(String subject) {
        super.onDumpStackForSurfaceFlingerHang(subject);
        String name = (!this.mSFHang || !subject.isEmpty()) ? "" : "surfaceflinger  hang.";
        EventLog.writeEvent((int) EventLogTags.WATCHDOG, name.isEmpty() ? subject : name);
        ExceptionLog exceptionLog = this.exceptionHWT;
        if (exceptionLog != null) {
            exceptionLog.WDTMatterJava(420);
        }
        mProcessStats.update();
        mProcessStats.printCurrentState(SystemClock.uptimeMillis());
        Slog.d(TAG, mProcessStats.printCurrentLoad());
    }
}
