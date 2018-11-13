package com.android.server;

import android.app.IActivityController;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hidl.manager.V1_0.IServiceManager;
import android.hidl.manager.V1_0.IServiceManager.InstanceDebugInfo;
import android.os.Binder;
import android.os.Build;
import android.os.Debug;
import android.os.FileUtils;
import android.os.Handler;
import android.os.IPowerManager;
import android.os.Looper;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.util.EventLog;
import android.util.Log;
import android.util.Slog;
import com.android.server.am.ActivityManagerService;
import com.android.server.face.FaceDaemonWrapper;
import com.android.server.job.controllers.JobStatus;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.Thread.State;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

public class Watchdog extends Thread {
    /* renamed from: -assertionsDisabled */
    static final /* synthetic */ boolean f26-assertionsDisabled = (Watchdog.class.desiredAssertionStatus() ^ 1);
    static final long CHECK_INTERVAL = 30000;
    static final int COMPLETED = 0;
    static final boolean DB = false;
    static final long DEFAULT_TIMEOUT = 60000;
    public static final List<String> HAL_INTERFACES_OF_INTEREST = Arrays.asList(new String[]{"android.hardware.audio@2.0::IDevicesFactory", "android.hardware.bluetooth@1.0::IBluetoothHci", "android.hardware.camera.provider@2.4::ICameraProvider", "android.hardware.graphics.composer@2.1::IComposer", "android.hardware.media.omx@1.0::IOmx", "android.hardware.sensors@1.0::ISensors", "android.hardware.vr@1.0::IVr"});
    public static final String[] NATIVE_STACKS_OF_INTEREST = new String[]{"/system/bin/audioserver", "/system/bin/cameraserver", "/system/bin/drmserver", "/system/bin/mediadrmserver", "/system/bin/mediaserver", "/system/bin/sdcard", "/system/bin/surfaceflinger", "media.extractor", "media.codec", "com.android.bluetooth", "zygote64", "zygote", "media.metrics"};
    static final int OVERDUE = 3;
    static final boolean RECORD_KERNEL_THREADS = true;
    static final String TAG = "Watchdog";
    static final int WAITED_HALF = 2;
    static final int WAITING = 1;
    static Watchdog sWatchdog;
    ActivityManagerService mActivity;
    boolean mAllowRestart = true;
    Context mContext;
    IActivityController mController;
    final ArrayList<HandlerChecker> mHandlerCheckers = new ArrayList();
    private boolean mLastTimeWatchdogHappen = false;
    final HandlerChecker mMonitorChecker = new HandlerChecker(FgThread.getHandler(), "foreground thread", 60000);
    final OpenFdMonitor mOpenFdMonitor;
    int mPhonePid;
    ContentResolver mResolver;
    SimpleDateFormat mTraceDateFormat = new SimpleDateFormat("dd_MMM_HH_mm_ss.SSS");

    public interface Monitor {
        void monitor();
    }

    private static final class BinderThreadMonitor implements Monitor {
        /* synthetic */ BinderThreadMonitor(BinderThreadMonitor -this0) {
            this();
        }

        private BinderThreadMonitor() {
        }

        public void monitor() {
            Binder.blockUntilThreadAvailable();
        }
    }

    public final class HandlerChecker implements Runnable {
        private boolean mCompleted;
        private Monitor mCurrentMonitor;
        private final Handler mHandler;
        private final ArrayList<Monitor> mMonitors = new ArrayList();
        private final String mName;
        private long mStartTime;
        private final long mWaitMax;

        HandlerChecker(Handler handler, String name, long waitMaxMillis) {
            this.mHandler = handler;
            this.mName = name;
            this.mWaitMax = waitMaxMillis;
            this.mCompleted = true;
        }

        public void addMonitor(Monitor monitor) {
            this.mMonitors.add(monitor);
        }

        public void scheduleCheckLocked() {
            if (this.mMonitors.size() == 0 && this.mHandler.getLooper().getQueue().isPolling()) {
                this.mCompleted = true;
            } else if (this.mCompleted) {
                this.mCompleted = false;
                this.mCurrentMonitor = null;
                this.mStartTime = SystemClock.uptimeMillis();
                this.mHandler.postAtFrontOfQueue(this);
            }
        }

        public boolean isOverdueLocked() {
            return !this.mCompleted && SystemClock.uptimeMillis() > this.mStartTime + this.mWaitMax;
        }

        public int getCompletionStateLocked() {
            if (this.mCompleted) {
                return 0;
            }
            long latency = SystemClock.uptimeMillis() - this.mStartTime;
            if (latency < this.mWaitMax / 2) {
                return 1;
            }
            if (latency < this.mWaitMax) {
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
                    this.mCurrentMonitor = (Monitor) this.mMonitors.get(i);
                }
                this.mCurrentMonitor.monitor();
            }
            synchronized (Watchdog.this) {
                this.mCompleted = true;
                this.mCurrentMonitor = null;
            }
        }
    }

    public static final class OpenFdMonitor {
        private static final int FD_HIGH_WATER_MARK = 12;
        private final File mDumpDir;
        private final File mFdHighWaterMark;

        public static OpenFdMonitor create() {
            if (!Build.IS_DEBUGGABLE) {
                return null;
            }
            String dumpDirStr = SystemProperties.get("dalvik.vm.stack-trace-dir", "");
            if (dumpDirStr.isEmpty()) {
                return null;
            }
            try {
                return new OpenFdMonitor(new File(dumpDirStr), new File("/proc/self/fd/" + (Os.getrlimit(OsConstants.RLIMIT_NOFILE).rlim_cur - 12)));
            } catch (ErrnoException errno) {
                Slog.w(Watchdog.TAG, "Error thrown from getrlimit(RLIMIT_NOFILE)", errno);
                return null;
            }
        }

        OpenFdMonitor(File dumpDir, File fdThreshold) {
            this.mDumpDir = dumpDir;
            this.mFdHighWaterMark = fdThreshold;
        }

        /* JADX WARNING: Removed duplicated region for block: B:4:0x0063 A:{Splitter: B:0:0x0000, ExcHandler: java.io.IOException (r1_0 'ex' java.lang.Exception)} */
        /* JADX WARNING: Missing block: B:4:0x0063, code:
            r1 = move-exception;
     */
        /* JADX WARNING: Missing block: B:5:0x0064, code:
            android.util.Slog.w(com.android.server.Watchdog.TAG, "Unable to dump open descriptors: " + r1);
     */
        /* JADX WARNING: Missing block: B:8:?, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private void dumpOpenDescriptors() {
            try {
                File dumpFile = File.createTempFile("anr_fd_", "", this.mDumpDir);
                int returnCode = new ProcessBuilder(new String[0]).command(new String[]{"/system/bin/lsof", "-p", String.valueOf(Process.myPid())}).redirectErrorStream(true).redirectOutput(dumpFile).start().waitFor();
                if (returnCode != 0) {
                    Slog.w(Watchdog.TAG, "Unable to dump open descriptors, lsof return code: " + returnCode);
                    dumpFile.delete();
                }
            } catch (Exception ex) {
            }
        }

        public boolean monitor() {
            if (!this.mFdHighWaterMark.exists()) {
                return false;
            }
            dumpOpenDescriptors();
            return true;
        }
    }

    final class RebootRequestReceiver extends BroadcastReceiver {
        RebootRequestReceiver() {
        }

        public void onReceive(Context c, Intent intent) {
            if (intent.getIntExtra("nowait", 0) != 0) {
                Watchdog.this.rebootSystem("Received ACTION_REBOOT broadcast");
            } else {
                Slog.w(Watchdog.TAG, "Unsupported ACTION_REBOOT broadcast: " + intent);
            }
        }
    }

    private native void native_dumpKernelStacks(String str);

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
        addMonitor(new BinderThreadMonitor());
        this.mOpenFdMonitor = OpenFdMonitor.create();
        boolean z = f26-assertionsDisabled;
        enableFutexwaitCheckIfNeeded();
    }

    public void init(Context context, ActivityManagerService activity) {
        this.mContext = context;
        this.mResolver = context.getContentResolver();
        this.mActivity = activity;
        context.registerReceiver(new RebootRequestReceiver(), new IntentFilter("android.intent.action.REBOOT"), "android.permission.REBOOT", null);
    }

    public void processStarted(String name, int pid) {
        synchronized (this) {
            if ("com.android.phone".equals(name)) {
                this.mPhonePid = pid;
            }
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

    public boolean getLastTimeWatchdogHappen() {
        return this.mLastTimeWatchdogHappen;
    }

    public void addMonitor(Monitor monitor) {
        synchronized (this) {
            if (isAlive()) {
                throw new RuntimeException("Monitors can't be added once the Watchdog is running");
            }
            this.mMonitorChecker.addMonitor(monitor);
        }
    }

    public void addThread(Handler thread) {
        addThread(thread, 60000);
    }

    public void addThread(Handler thread, long timeoutMillis) {
        synchronized (this) {
            if (isAlive()) {
                throw new RuntimeException("Threads can't be added once the Watchdog is running");
            }
            this.mHandlerCheckers.add(new HandlerChecker(thread, thread.getLooper().getThread().getName(), timeoutMillis));
        }
    }

    void rebootSystem(String reason) {
        Slog.i(TAG, "Rebooting system because: " + reason);
        try {
            ((IPowerManager) ServiceManager.getService("power")).reboot(false, reason, false);
        } catch (RemoteException e) {
        }
    }

    private int evaluateCheckerCompletionLocked() {
        int state = 0;
        for (int i = 0; i < this.mHandlerCheckers.size(); i++) {
            state = Math.max(state, ((HandlerChecker) this.mHandlerCheckers.get(i)).getCompletionStateLocked());
        }
        return state;
    }

    private ArrayList<HandlerChecker> getBlockedCheckersLocked() {
        ArrayList<HandlerChecker> checkers = new ArrayList();
        for (int i = 0; i < this.mHandlerCheckers.size(); i++) {
            HandlerChecker hc = (HandlerChecker) this.mHandlerCheckers.get(i);
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
            builder.append(((HandlerChecker) checkers.get(i)).describeBlockedStateLocked());
        }
        return builder.toString();
    }

    private ArrayList<Integer> getInterestingHalPids() {
        try {
            ArrayList<InstanceDebugInfo> dump = IServiceManager.getService().debugDump();
            HashSet<Integer> pids = new HashSet();
            for (InstanceDebugInfo info : dump) {
                if (info.pid != -1 && HAL_INTERFACES_OF_INTEREST.contains(info.interfaceName)) {
                    pids.add(Integer.valueOf(info.pid));
                }
            }
            return new ArrayList(pids);
        } catch (RemoteException e) {
            return new ArrayList();
        }
    }

    private ArrayList<Integer> getInterestingNativePids() {
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

    public void run() {
        boolean waitedHalf = false;
        File initialStack = null;
        boolean agingTestVersion = LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(SystemProperties.get("SPECIAL_OPPO_CONFIG"));
        boolean releaseVersion = SystemProperties.getBoolean("ro.build.release_type", false);
        if (agingTestVersion || (releaseVersion ^ 1) != 0) {
            SystemProperties.set("persist.sys.oppo.perfrecord", "true");
        }
        while (true) {
            CheckBlockedException.getInstance().setContext(this.mContext);
            CheckBlockedException.getInstance().triggerDetect();
            int debuggerWasConnected = 0;
            synchronized (this) {
                int i;
                List<HandlerChecker> blockedCheckers;
                String subject;
                ArrayList<Integer> pids;
                IActivityController controller;
                FDMonitor.monitor();
                for (i = 0; i < this.mHandlerCheckers.size(); i++) {
                    ((HandlerChecker) this.mHandlerCheckers.get(i)).scheduleCheckLocked();
                }
                long start = SystemClock.uptimeMillis();
                for (long timeout = 30000; timeout > 0; timeout = 30000 - (SystemClock.uptimeMillis() - start)) {
                    if (Debug.isDebuggerConnected()) {
                        debuggerWasConnected = 2;
                    }
                    try {
                        wait(timeout);
                    } catch (Throwable e) {
                        Log.wtf(TAG, e);
                    }
                    if (Debug.isDebuggerConnected()) {
                        debuggerWasConnected = 2;
                    }
                }
                boolean fdLimitTriggered = false;
                if (this.mOpenFdMonitor != null) {
                    fdLimitTriggered = this.mOpenFdMonitor.monitor();
                }
                if (fdLimitTriggered) {
                    blockedCheckers = Collections.emptyList();
                    subject = "Open FD high water mark reached";
                } else {
                    int waitState = evaluateCheckerCompletionLocked();
                    if (waitState == 0) {
                        waitedHalf = false;
                    } else if (waitState != 1) {
                        if (waitState != 2) {
                            blockedCheckers = getBlockedCheckersLocked();
                            subject = describeCheckersLocked(blockedCheckers);
                        } else if (!waitedHalf) {
                            pids = new ArrayList();
                            pids.add(Integer.valueOf(Process.myPid()));
                            initialStack = ActivityManagerService.dumpStackTraces(true, (ArrayList) pids, null, null, getInterestingNativePids());
                            waitedHalf = true;
                        }
                    }
                }
                boolean allowRestart = this.mAllowRestart;
                EventLog.writeEvent(EventLogTags.WATCHDOG, subject);
                pids = new ArrayList();
                pids.add(Integer.valueOf(Process.myPid()));
                if (this.mPhonePid > 0) {
                    pids.add(Integer.valueOf(this.mPhonePid));
                }
                File finalStack = ActivityManagerService.dumpStackTraces(waitedHalf ^ 1, (ArrayList) pids, null, null, getInterestingNativePids());
                if (LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(SystemProperties.get("ro.debuggable"))) {
                    binderStateRead();
                }
                SystemClock.sleep(2000);
                dumpKernelStackTraces();
                String tracesDirProp = SystemProperties.get("dalvik.vm.stack-trace-dir", "");
                File stackFd = finalStack;
                File file;
                if (tracesDirProp.isEmpty()) {
                    String tracesPath = SystemProperties.get("dalvik.vm.stack-trace-file", null);
                    String traceFileNameAmendment = "_SystemServer_WDT" + this.mTraceDateFormat.format(new Date());
                    if (tracesPath == null || tracesPath.length() == 0) {
                        Slog.w(TAG, "dump WDT Traces: no trace path configured");
                    } else {
                        String newTracesPath;
                        file = new File(tracesPath);
                        int lpos = tracesPath.lastIndexOf(".");
                        if (-1 != lpos) {
                            newTracesPath = tracesPath.substring(0, lpos) + traceFileNameAmendment + tracesPath.substring(lpos);
                        } else {
                            newTracesPath = tracesPath + traceFileNameAmendment;
                        }
                        file.renameTo(new File(newTracesPath));
                        file = new File(newTracesPath);
                    }
                } else {
                    file = new File(new File(tracesDirProp), "traces_SystemServer_WDT" + this.mTraceDateFormat.format(new Date()) + "_pid" + String.valueOf(Process.myPid()));
                    try {
                        if (file.createNewFile()) {
                            FileUtils.setPermissions(file.getAbsolutePath(), 384, -1, -1);
                            if (initialStack == null) {
                                Slog.e(TAG, "First set of traces are empty!");
                            } else if (System.currentTimeMillis() - initialStack.lastModified() < 60000) {
                                Slog.e(TAG, "First set of traces taken from " + initialStack.getAbsolutePath());
                                appendFile(file, initialStack);
                            }
                            if (finalStack != null) {
                                Slog.e(TAG, "Second set of traces taken from " + finalStack.getAbsolutePath());
                                appendFile(file, finalStack);
                            } else {
                                Slog.e(TAG, "Second set of traces are empty!");
                            }
                        } else {
                            Slog.w(TAG, "Unable to create Watchdog dump file: createNewFile failed");
                        }
                    } catch (Throwable ioe) {
                        Slog.e(TAG, "Exception creating Watchdog dump file:", ioe);
                    }
                }
                final String str = subject;
                final File file2 = stackFd;
                Thread anonymousClass1 = new Thread("watchdogWriteToDropbox") {
                    public void run() {
                        Watchdog.this.mActivity.addErrorToDropBox("watchdog", null, "system_server", null, null, str, null, file2, null);
                    }
                };
                anonymousClass1.start();
                try {
                    anonymousClass1.join(2000);
                } catch (InterruptedException e2) {
                }
                checkGotoDumpAfterWriteEventlog();
                DumpStackAndAddDropbox(waitedHalf ^ 1, subject);
                if (SystemProperties.getBoolean("persist.sys.crashOnWatchdog", false) || shouldGotoDump()) {
                    Slog.e(TAG, "Triggering SysRq for system_server watchdog");
                    doSysRq('w');
                    doSysRq('l');
                    SystemClock.sleep(3000);
                    doSysRq('c');
                }
                synchronized (this) {
                    controller = this.mController;
                }
                if (controller != null) {
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
                } else if (allowRestart) {
                    Slog.w(TAG, "*** WATCHDOG KILLING SYSTEM PROCESS: " + subject);
                    for (i = 0; i < blockedCheckers.size(); i++) {
                        Slog.w(TAG, ((HandlerChecker) blockedCheckers.get(i)).getName() + " stack trace:");
                        for (StackTraceElement element : ((HandlerChecker) blockedCheckers.get(i)).getThread().getStackTrace()) {
                            Slog.w(TAG, "    at " + element);
                        }
                    }
                    outputCurrentProcessTrace();
                    AgingCriticalEvent.getInstance().writeEvent(AgingCriticalEvent.EVENT_SYSTEMSERVER_WATCHDOG, subject);
                    if (SystemProperties.getBoolean("persist.sys.warnOnWatchdog", false)) {
                        Slog.wtf(TAG, "*** GOODBYE  !, warnOnWatchdog have set, only warning, not reboot");
                        try {
                            Thread.sleep(JobStatus.DEFAULT_TRIGGER_MAX_DELAY);
                        } catch (Exception e4) {
                        }
                    } else {
                        Slog.w(TAG, "*** GOODBYE  !");
                        SystemProperties.set("ctl.restart", "zygote_secondary");
                        try {
                            Thread.sleep(FaceDaemonWrapper.TIMEOUT_FACED_BINDERCALL_CHECK);
                        } catch (Exception e5) {
                        }
                        SystemProperties.set("ctl.restart", "zygote");
                    }
                } else {
                    Slog.w(TAG, "Restart not allowed: Watchdog is *not* killing the system process");
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
            Slog.i(TAG, "Collect Binder Transaction Status Information");
            FileReader binder_state_in = new FileReader("/sys/kernel/debug/binder/state");
            FileWriter binder_state_out = new FileWriter("/data/anr/BinderTraces.txt");
            while (true) {
                int c = binder_state_in.read();
                if (c != -1) {
                    binder_state_out.write(c);
                } else {
                    binder_state_in.close();
                    binder_state_out.close();
                    return;
                }
            }
        } catch (IOException e) {
            Slog.w(TAG, "Failed to collect state file", e);
        }
    }

    private File dumpKernelStackTraces() {
        String tracesPath = SystemProperties.get("dalvik.vm.stack-trace-file", null);
        if (tracesPath == null || tracesPath.length() == 0) {
            return null;
        }
        native_dumpKernelStacks(tracesPath);
        return new File(tracesPath);
    }

    private boolean isReleaseVersion() {
        return SystemProperties.getInt("ro.secure", 1) == 1;
    }

    private boolean isThreadStatusBlock(State state) {
        return state == State.BLOCKED || state == State.WAITING || state == State.TIMED_WAITING;
    }

    private boolean isProcessWaitForZygoteSocket() {
        ThreadGroup currentGroup = Thread.currentThread().getThreadGroup();
        int count = currentGroup.activeCount();
        Thread[] threads = new Thread[((count / 2) + count)];
        count = currentGroup.enumerate(threads);
        int i = 0;
        while (i < count) {
            StackTraceElement[] stackArray = threads[i].getStackTrace();
            for (StackTraceElement element : stackArray) {
                if (element.toString().contains("zygoteSendArgsAndGetResult") && isThreadStatusBlock(threads[i].getState())) {
                    Log.i(TAG, "isProcessWaitForZygoteSocket return true! thread name:" + threads[i].getName() + " status:" + threads[i].getState());
                    return true;
                }
            }
            i++;
        }
        return false;
    }

    private boolean shouldGotoDump() {
        if (isReleaseVersion() || !isProcessWaitForZygoteSocket()) {
            return false;
        }
        SystemClock.sleep(10000);
        return true;
    }

    public void DumpStackAndAddDropbox(boolean clearTrace, String subject) {
        ArrayList pids = new ArrayList();
        pids.add(Integer.valueOf(Process.myPid()));
        if (this.mPhonePid > 0) {
            pids.add(Integer.valueOf(this.mPhonePid));
        }
        File stack = ActivityManagerService.dumpStackTraces(clearTrace, pids, null, null, getInterestingNativePids());
        this.mLastTimeWatchdogHappen = true;
        if (LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(SystemProperties.get("ro.debuggable"))) {
            binderStateRead();
        }
        SystemClock.sleep(2000);
        dumpKernelStackTraces();
        doSysRq('w');
        doSysRq('l');
        File stackFd = stack;
        if (SystemProperties.get("dalvik.vm.stack-trace-dir", "").isEmpty()) {
            String tracesPath = SystemProperties.get("dalvik.vm.stack-trace-file", null);
            String traceFileNameAmendment = "_SystemServer_WDT" + this.mTraceDateFormat.format(new Date());
            if (tracesPath == null || tracesPath.length() == 0) {
                Slog.w(TAG, "dump WDT Traces: no trace path configured");
            } else {
                String newTracesPath;
                File traceRenameFile = new File(tracesPath);
                int lpos = tracesPath.lastIndexOf(".");
                if (-1 != lpos) {
                    newTracesPath = tracesPath.substring(0, lpos) + traceFileNameAmendment + tracesPath.substring(lpos);
                } else {
                    newTracesPath = tracesPath + traceFileNameAmendment;
                }
                traceRenameFile.renameTo(new File(newTracesPath));
                stackFd = new File(newTracesPath);
            }
        }
        final File newFd = stackFd;
        final String str = subject;
        Thread dropboxThread = new Thread("watchdogWriteToDropbox") {
            public void run() {
                Watchdog.this.mActivity.addErrorToDropBox("watchdog", null, "system_server", null, null, str, null, newFd, null);
            }
        };
        dropboxThread.start();
        try {
            Slog.i(TAG, "call dropboxThread join");
            dropboxThread.join(10000);
            Slog.i(TAG, "dropboxThread join finish");
        } catch (InterruptedException e) {
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:30:0x0115 A:{SYNTHETIC, Splitter: B:30:0x0115} */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x011a A:{Catch:{ Exception -> 0x0129 }} */
    /* JADX WARNING: Removed duplicated region for block: B:64:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x011f A:{Catch:{ Exception -> 0x0129 }} */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x0131 A:{SYNTHETIC, Splitter: B:40:0x0131} */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x0136 A:{Catch:{ Exception -> 0x0145 }} */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x013b A:{Catch:{ Exception -> 0x0145 }} */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x0115 A:{SYNTHETIC, Splitter: B:30:0x0115} */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x011a A:{Catch:{ Exception -> 0x0129 }} */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x011f A:{Catch:{ Exception -> 0x0129 }} */
    /* JADX WARNING: Removed duplicated region for block: B:64:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x0131 A:{SYNTHETIC, Splitter: B:40:0x0131} */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x0136 A:{Catch:{ Exception -> 0x0145 }} */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x013b A:{Catch:{ Exception -> 0x0145 }} */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x0115 A:{SYNTHETIC, Splitter: B:30:0x0115} */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x011a A:{Catch:{ Exception -> 0x0129 }} */
    /* JADX WARNING: Removed duplicated region for block: B:64:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x011f A:{Catch:{ Exception -> 0x0129 }} */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x0131 A:{SYNTHETIC, Splitter: B:40:0x0131} */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x0136 A:{Catch:{ Exception -> 0x0145 }} */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x013b A:{Catch:{ Exception -> 0x0145 }} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void outputCurrentProcessTrace() {
        Exception e;
        Throwable th;
        BufferedWriter out = null;
        FileOutputStream filestream = null;
        OutputStreamWriter writer = null;
        try {
            Writer outputStreamWriter;
            FileOutputStream filestream2 = new FileOutputStream("/data/system/dropbox/WDT_java_trace_" + new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date(System.currentTimeMillis())) + ".txt");
            try {
                outputStreamWriter = new OutputStreamWriter(filestream2);
            } catch (Exception e2) {
                e = e2;
                filestream = filestream2;
                try {
                    e.printStackTrace();
                    if (out != null) {
                    }
                    if (writer != null) {
                    }
                    if (filestream == null) {
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (out != null) {
                        try {
                            out.close();
                        } catch (Exception e3) {
                            e3.printStackTrace();
                            throw th;
                        }
                    }
                    if (writer != null) {
                        writer.close();
                    }
                    if (filestream != null) {
                        filestream.flush();
                        FileUtils.sync(filestream);
                        filestream.close();
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                filestream = filestream2;
                if (out != null) {
                }
                if (writer != null) {
                }
                if (filestream != null) {
                }
                throw th;
            }
            Writer writer2;
            try {
                BufferedWriter out2 = new BufferedWriter(outputStreamWriter);
                try {
                    ThreadGroup currentGroup = Thread.currentThread().getThreadGroup();
                    int count = currentGroup.activeCount();
                    Thread[] threads = new Thread[((count / 2) + count)];
                    count = currentGroup.enumerate(threads);
                    for (int i = 0; i < count; i++) {
                        out2.write("Thread Name:" + threads[i].getName() + "\nThread id:" + threads[i].getId() + "\nThread State:" + threads[i].getState() + "\n");
                        StackTraceElement[] stackArray = threads[i].getStackTrace();
                        for (StackTraceElement element : stackArray) {
                            out2.write(element.toString() + "\n");
                        }
                    }
                    out2.write("\n");
                    out2.flush();
                    if (out2 != null) {
                        try {
                            out2.close();
                        } catch (Exception e32) {
                            e32.printStackTrace();
                        }
                    }
                    if (outputStreamWriter != null) {
                        outputStreamWriter.close();
                    }
                    if (filestream2 != null) {
                        filestream2.flush();
                        FileUtils.sync(filestream2);
                        filestream2.close();
                    }
                    writer2 = outputStreamWriter;
                    out = out2;
                } catch (Exception e4) {
                    e32 = e4;
                    writer2 = outputStreamWriter;
                    filestream = filestream2;
                    out = out2;
                    e32.printStackTrace();
                    if (out != null) {
                    }
                    if (writer != null) {
                    }
                    if (filestream == null) {
                    }
                } catch (Throwable th4) {
                    th = th4;
                    writer2 = outputStreamWriter;
                    filestream = filestream2;
                    out = out2;
                    if (out != null) {
                    }
                    if (writer != null) {
                    }
                    if (filestream != null) {
                    }
                    throw th;
                }
            } catch (Exception e5) {
                e32 = e5;
                writer2 = outputStreamWriter;
                filestream = filestream2;
                e32.printStackTrace();
                if (out != null) {
                }
                if (writer != null) {
                }
                if (filestream == null) {
                }
            } catch (Throwable th5) {
                th = th5;
                writer2 = outputStreamWriter;
                filestream = filestream2;
                if (out != null) {
                }
                if (writer != null) {
                }
                if (filestream != null) {
                }
                throw th;
            }
        } catch (Exception e6) {
            e32 = e6;
            e32.printStackTrace();
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e322) {
                    e322.printStackTrace();
                    return;
                }
            }
            if (writer != null) {
                writer.close();
            }
            if (filestream == null) {
                filestream.flush();
                FileUtils.sync(filestream);
                filestream.close();
            }
        }
    }

    private void enableFutexwaitCheckIfNeeded() {
        if (SystemProperties.getBoolean("ro.build.release_type", false)) {
            SystemProperties.set("persist.sys.oppo.checkfutexwait", "true");
        }
    }

    private void checkGotoDumpAfterWriteEventlog() {
        if (SystemProperties.getBoolean("persist.sys.dumpAfterWdEvent", false)) {
            doSysRq('c');
        }
    }
}
