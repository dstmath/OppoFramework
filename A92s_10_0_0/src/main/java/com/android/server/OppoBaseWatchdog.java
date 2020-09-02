package com.android.server;

import android.content.Context;
import android.hidl.manager.V1_0.IServiceManager;
import android.os.Debug;
import android.os.FileUtils;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.Slog;
import android.util.SparseArray;
import android.util.StatsLog;
import com.android.internal.os.ProcessCpuTracker;
import com.android.server.am.ActivityManagerService;
import com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils;
import com.android.server.job.controllers.JobStatus;
import com.android.server.oppo.TemperatureProvider;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.Thread;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public abstract class OppoBaseWatchdog extends Thread {
    public static int HPROF_COUNTER = 0;
    public static final List<String> OPPO_HAL_INTERFACES_OF_INTEREST = Arrays.asList("android.hardware.camera.provider@2.4::ICameraProvider");
    private static final String TAG = "OppoBaseWatchdog";
    ActivityManagerService mActivityService;
    protected int mAndroidProcessAcorePid;
    protected Context mContext;
    protected OppoWatchdogDcsUploader mDcsUploader = null;
    protected boolean mLastTimeWatchdogHappen = false;
    protected int mLoopCount = 0;

    /* access modifiers changed from: protected */
    public abstract int getPhonePid();

    /* access modifiers changed from: protected */
    public abstract void onBinderStateRead();

    /* access modifiers changed from: protected */
    public abstract void onDoSysRq(char c);

    public abstract ArrayList<Integer> onGetInterestingNativePids();

    public OppoBaseWatchdog(String name) {
        super(name);
    }

    /* access modifiers changed from: protected */
    public void onInit(Context context, ActivityManagerService activity) {
        this.mContext = context;
        this.mActivityService = activity;
        enableFutexwaitCheckIfNeeded();
        this.mDcsUploader = new OppoWatchdogDcsUploader(this.mContext);
    }

    public boolean getLastTimeWatchdogHappen() {
        return this.mLastTimeWatchdogHappen;
    }

    private boolean isReleaseVersion() {
        return SystemProperties.getInt("ro.secure", 1) == 1;
    }

    private boolean isThreadStatusBlock(Thread.State state) {
        return state == Thread.State.BLOCKED || state == Thread.State.WAITING || state == Thread.State.TIMED_WAITING;
    }

    private boolean isProcessWaitForZygoteSocket() {
        ThreadGroup currentGroup = Thread.currentThread().getThreadGroup();
        int count = currentGroup.activeCount();
        Thread[] threads = new Thread[((count / 2) + count)];
        int count2 = currentGroup.enumerate(threads);
        for (int i = 0; i < count2; i++) {
            StackTraceElement[] stackArray = threads[i].getStackTrace();
            int j = 0;
            while (j < stackArray.length) {
                if (!stackArray[j].toString().contains("zygoteSendArgsAndGetResult") || !isThreadStatusBlock(threads[i].getState())) {
                    j++;
                } else {
                    Slog.i(TAG, "isProcessWaitForZygoteSocket return true! thread name:" + threads[i].getName() + " status:" + threads[i].getState());
                    return true;
                }
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean shouldGotoDump() {
        if (isReleaseVersion() || !isProcessWaitForZygoteSocket()) {
            return false;
        }
        SystemClock.sleep(10000);
        return true;
    }

    public void dumpStackAndAddDropbox(final String subject) {
        String newTracesPath;
        onDumpStackForSurfaceFlingerHang(subject);
        ArrayList<Integer> pids = new ArrayList<>();
        pids.add(Integer.valueOf(Process.myPid()));
        int phonePid = getPhonePid();
        if (phonePid > 0) {
            pids.add(Integer.valueOf(phonePid));
        }
        File stack = ActivityManagerService.dumpStackTraces(pids, (ProcessCpuTracker) null, (SparseArray<Boolean>) null, onGetInterestingNativePids());
        this.mLastTimeWatchdogHappen = true;
        if ("1".equals(SystemProperties.get("ro.debuggable"))) {
            onBinderStateRead();
        }
        SystemClock.sleep(5000);
        onDoSysRq('w');
        onDoSysRq('l');
        final File stackFd = stack;
        if (SystemProperties.get("dalvik.vm.stack-trace-dir", "").isEmpty()) {
            String tracesPath = SystemProperties.get("dalvik.vm.stack-trace-file", (String) null);
            SimpleDateFormat traceDateFormat = new SimpleDateFormat("dd_MM_HH_mm_ss.SSS");
            String traceFileNameAmendment = "_SystemServer_WDT" + traceDateFormat.format(new Date());
            if (tracesPath == null || tracesPath.length() == 0) {
                Slog.w(TAG, "dump WDT Traces: no trace path configured");
            } else {
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
        Slog.v(TAG, "** save all info before killnig system server **");
        Thread dropboxThread = new Thread("watchdogWriteToDropbox") {
            /* class com.android.server.OppoBaseWatchdog.AnonymousClass1 */

            public void run() {
                if (OppoBaseWatchdog.this.mActivityService != null) {
                    OppoBaseWatchdog.this.mActivityService.addErrorToDropBox("watchdog", null, "system_server", null, null, null, subject, null, stackFd, null);
                }
                StatsLog.write(185, subject);
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

    /* access modifiers changed from: protected */
    public void outputCurrentProcessTrace() {
        BufferedWriter out = null;
        FileOutputStream filestream = null;
        OutputStreamWriter writer = null;
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
            Date dayNow = new Date(System.currentTimeMillis());
            filestream = new FileOutputStream("/data/system/dropbox/WDT_java_trace_" + dateFormat.format(dayNow) + ".txt");
            writer = new OutputStreamWriter(filestream);
            out = new BufferedWriter(writer);
            ThreadGroup currentGroup = Thread.currentThread().getThreadGroup();
            int count = currentGroup.activeCount();
            Thread[] threads = new Thread[((count / 2) + count)];
            int count2 = currentGroup.enumerate(threads);
            for (int i = 0; i < count2; i++) {
                out.write("Thread Name:" + threads[i].getName() + "\nThread id:" + threads[i].getId() + "\nThread State:" + threads[i].getState() + StringUtils.LF);
                StackTraceElement[] stackArray = threads[i].getStackTrace();
                for (StackTraceElement element : stackArray) {
                    out.write(element.toString() + StringUtils.LF);
                }
            }
            out.write(StringUtils.LF);
            out.flush();
            try {
                out.close();
                writer.close();
                filestream.flush();
                FileUtils.sync(filestream);
                filestream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e2) {
            e2.printStackTrace();
            if (out != null) {
                out.close();
            }
            if (writer != null) {
                writer.close();
            }
            if (filestream != null) {
                filestream.flush();
                FileUtils.sync(filestream);
                filestream.close();
            }
        } catch (Throwable th) {
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
    }

    /* access modifiers changed from: protected */
    public void enableFutexwaitCheckIfNeeded() {
        if (SystemProperties.getBoolean("ro.build.release_type", false)) {
            SystemProperties.set("persist.sys.oppo.checkfutexwait", TemperatureProvider.SWITCH_ON);
        }
    }

    /* access modifiers changed from: protected */
    public void checkGotoDumpAfterWriteEventlog() {
        if (SystemProperties.getBoolean("persist.sys.dumpAfterWdEvent", false)) {
            onDoSysRq('c');
        }
    }

    /* access modifiers changed from: protected */
    public void maybeRecordPerfInfo() {
        boolean agingTestVersion = "1".equals(SystemProperties.get("SPECIAL_OPPO_CONFIG"));
        boolean releaseVersion = SystemProperties.getBoolean("ro.build.release_type", false);
        if (agingTestVersion || !releaseVersion) {
            SystemProperties.set("persist.sys.oppo.perfrecord", TemperatureProvider.SWITCH_ON);
        }
    }

    /* access modifiers changed from: protected */
    public void warnOrRebootWhenWatchdog() {
        if (SystemProperties.getBoolean("persist.sys.warnOnWatchdog", false)) {
            Slog.wtf(TAG, "*** GOODBYE  !, warnOnWatchdog have set, only warning, not reboot");
            try {
                Thread.sleep(JobStatus.DEFAULT_TRIGGER_MAX_DELAY);
            } catch (Exception e) {
            }
        } else {
            Slog.w(TAG, "*** GOODBYE  !");
            SystemProperties.set("ctl.restart", "zygote_secondary");
            try {
                Thread.sleep(5000);
            } catch (Exception e2) {
            }
            SystemProperties.set("ctl.restart", "zygote");
        }
    }

    public static ArrayList<Integer> getOppoInterestingHalPids() {
        try {
            ArrayList<IServiceManager.InstanceDebugInfo> dump = IServiceManager.getService().debugDump();
            HashSet<Integer> pids = new HashSet<>();
            Iterator<IServiceManager.InstanceDebugInfo> it = dump.iterator();
            while (it.hasNext()) {
                IServiceManager.InstanceDebugInfo info = it.next();
                if (info.pid != -1) {
                    if (OPPO_HAL_INTERFACES_OF_INTEREST.contains(info.interfaceName)) {
                        pids.add(Integer.valueOf(info.pid));
                    }
                }
            }
            return new ArrayList<>(pids);
        } catch (RemoteException e) {
            return new ArrayList<>();
        }
    }

    public void checkSystemHeapMem() {
        if (SystemProperties.getBoolean("persist.sys.assert.panic", false)) {
            new Thread("checkSystemServerMemHealth") {
                /* class com.android.server.OppoBaseWatchdog.AnonymousClass2 */

                public void run() {
                    Runtime runtime = Runtime.getRuntime();
                    long dalvikAllocated = (runtime.totalMemory() / 1024) - (runtime.freeMemory() / 1024);
                    int totalRef = Debug.getBinderLocalObjectCount() + Debug.getBinderProxyObjectCount() + Debug.getBinderDeathObjectCount();
                    if ((dalvikAllocated > 480000 || totalRef > 42000) && OppoBaseWatchdog.HPROF_COUNTER < 6) {
                        try {
                            SimpleDateFormat traceDateFormat = new SimpleDateFormat("dd_MM_HH_mm_ss.SSS");
                            Slog.w(OppoBaseWatchdog.TAG, "about to dump system hprof dalvikAllocated: " + dalvikAllocated + " totalRef: " + totalRef);
                            StringBuilder sb = new StringBuilder();
                            sb.append("/data/oppo_log/system_server_heap_");
                            sb.append(traceDateFormat.format(new Date()));
                            sb.append(".hprof");
                            Debug.dumpHprofData(sb.toString());
                            OppoBaseWatchdog.HPROF_COUNTER++;
                        } catch (IOException e) {
                            Slog.w(OppoBaseWatchdog.TAG, "system server heap dump failed ");
                        }
                    }
                }
            }.start();
        }
    }

    public static void dumpHprof() {
        String profName = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()).replaceAll(StringUtils.SPACE, "_").replaceAll(":", "-");
        try {
            Debug.dumpHprofData("/data/anr/" + profName + ".hprof");
        } catch (IOException e) {
            Slog.w(TAG, "system server heap dump failed ");
        }
    }

    /* access modifiers changed from: protected */
    public void syncAppAndKernelTime() {
        writeCounterState((long) this.mLoopCount);
        this.mLoopCount++;
    }

    private void writeCounterState(long count) {
        try {
            FileOutputStream mFos = new FileOutputStream(new File("/d/wakeup_sources"));
            mFos.write(String.format("%d", Long.valueOf(count)).getBytes("utf-8"));
            mFos.close();
            Slog.w(TAG, "!@WatchDog_" + count);
        } catch (Exception e) {
            Slog.v(TAG, "@WatchDog_ error e: " + e.toString());
        }
    }

    /* access modifiers changed from: protected */
    public void onAddMonitorCheck(Thread thread) {
        Slog.w(TAG, "HandlerChecker trying to add null monitor, stack trace:");
        StackTraceElement[] stackTrace = thread.getStackTrace();
        for (StackTraceElement element : stackTrace) {
            Slog.w(TAG, "    at " + element);
        }
    }

    /* access modifiers changed from: protected */
    public void onProcessStarted(String name, int pid) {
        if ("android.process.acore".equals(name)) {
            this.mAndroidProcessAcorePid = pid;
        }
    }

    /* access modifiers changed from: protected */
    public void onDumpStackForSurfaceFlingerHang(String subject) {
    }
}
