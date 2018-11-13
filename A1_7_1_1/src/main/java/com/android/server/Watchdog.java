package com.android.server;

import android.app.IActivityController;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
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
import android.util.EventLog;
import android.util.Log;
import android.util.Slog;
import com.android.server.am.ActivityManagerService;
import com.android.server.oppo.IElsaManager;
import com.mediatek.aee.ExceptionLog;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

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
public class Watchdog extends Thread {
    static final long CHECK_INTERVAL = 30000;
    static final int COMPLETED = 0;
    static final boolean DB = false;
    static final long DEFAULT_TIMEOUT = 60000;
    public static final String[] NATIVE_STACKS_OF_INTEREST = null;
    static final int OVERDUE = 3;
    static final boolean RECORD_KERNEL_THREADS = true;
    static final String TAG = "Watchdog";
    static final int TIME_SF_WAIT = 20000;
    static final int WAITED_HALF = 2;
    static final int WAITING = 1;
    static Watchdog sWatchdog;
    ExceptionLog exceptionHWT;
    ActivityManagerService mActivity;
    boolean mAllowRestart;
    Context mContext;
    IActivityController mController;
    final ArrayList<HandlerChecker> mHandlerCheckers;
    private boolean mLastTimeWatchdogHappen;
    final HandlerChecker mMonitorChecker;
    int mPhonePid;
    ContentResolver mResolver;
    boolean mSFHang;

    public interface Monitor {
        void monitor();
    }

    private static final class BinderThreadMonitor implements Monitor {
        /* synthetic */ BinderThreadMonitor(BinderThreadMonitor binderThreadMonitor) {
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
            if (monitor == null) {
                Slog.w(Watchdog.TAG, "HandlerChecker trying to add null monitor, stack trace:");
                for (StackTraceElement element : getThread().getStackTrace()) {
                    Slog.w(Watchdog.TAG, "    at " + element);
                }
            }
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
                if (this.mCurrentMonitor != null) {
                    this.mCurrentMonitor.monitor();
                }
            }
            synchronized (Watchdog.this) {
                this.mCompleted = true;
                this.mCurrentMonitor = null;
            }
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

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.Watchdog.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.Watchdog.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.Watchdog.<clinit>():void");
    }

    private native void native_dumpKernelStacks(String str);

    public long GetSFStatus() {
        if (this.exceptionHWT != null) {
            return this.exceptionHWT.SFMatterJava(0, 0);
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

    public static Watchdog getInstance() {
        if (sWatchdog == null) {
            sWatchdog = new Watchdog();
        }
        return sWatchdog;
    }

    private Watchdog() {
        super("watchdog");
        this.mLastTimeWatchdogHappen = false;
        this.mHandlerCheckers = new ArrayList();
        this.mAllowRestart = true;
        this.mSFHang = false;
        this.mMonitorChecker = new HandlerChecker(FgThread.getHandler(), "foreground thread", 60000);
        this.mHandlerCheckers.add(this.mMonitorChecker);
        this.mHandlerCheckers.add(new HandlerChecker(new Handler(Looper.getMainLooper()), "main thread", 60000));
        this.mHandlerCheckers.add(new HandlerChecker(UiThread.getHandler(), "ui thread", 60000));
        this.mHandlerCheckers.add(new HandlerChecker(IoThread.getHandler(), "i/o thread", 60000));
        this.mHandlerCheckers.add(new HandlerChecker(DisplayThread.getHandler(), "display thread", 60000));
        addMonitor(new BinderThreadMonitor());
        this.exceptionHWT = new ExceptionLog();
        enableFutexwaitCheckIfNeeded();
    }

    public void init(Context context, ActivityManagerService activity) {
        this.mContext = context;
        this.mResolver = context.getContentResolver();
        this.mActivity = activity;
        context.registerReceiver(new RebootRequestReceiver(), new IntentFilter("android.intent.action.REBOOT"), "android.permission.REBOOT", null);
        if (this.exceptionHWT != null) {
            this.exceptionHWT.WDTMatterJava(0);
        }
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

    private String describeCheckersLocked(ArrayList<HandlerChecker> checkers) {
        StringBuilder builder = new StringBuilder(128);
        for (int i = 0; i < checkers.size(); i++) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(((HandlerChecker) checkers.get(i)).describeBlockedStateLocked());
        }
        return builder.toString();
    }

    public void run() {
        boolean waitedHalf = false;
        while (true) {
            CheckBlockedException.getInstance().setContext(this.mContext);
            CheckBlockedException.getInstance().triggerDetect();
            this.mSFHang = false;
            if (!(this.exceptionHWT == null || waitedHalf)) {
                this.exceptionHWT.WDTMatterJava(300);
            }
            int debuggerWasConnected = 0;
            synchronized (this) {
                int i;
                IActivityController controller;
                for (i = 0; i < this.mHandlerCheckers.size(); i++) {
                    ((HandlerChecker) this.mHandlerCheckers.get(i)).scheduleCheckLocked();
                }
                long start = SystemClock.uptimeMillis();
                for (long timeout = CHECK_INTERVAL; timeout > 0; timeout = CHECK_INTERVAL - (SystemClock.uptimeMillis() - start)) {
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
                }
                long SFHangTime = GetSFStatus();
                if (SFHangTime > 40000) {
                    Slog.v(TAG, "**SF hang Time **" + SFHangTime);
                    this.mSFHang = true;
                } else {
                    int waitState = evaluateCheckerCompletionLocked();
                    if (waitState == 0) {
                        this.mLastTimeWatchdogHappen = false;
                        waitedHalf = false;
                    } else if (waitState != 1) {
                        if (waitState == 2) {
                            if (!waitedHalf) {
                                this.mLastTimeWatchdogHappen = true;
                                if (this.exceptionHWT != null) {
                                    this.exceptionHWT.WDTMatterJava(360);
                                }
                                ArrayList<Integer> pids = new ArrayList();
                                pids.add(Integer.valueOf(Process.myPid()));
                                ActivityManagerService.dumpStackTraces(true, (ArrayList) pids, null, null, NATIVE_STACKS_OF_INTEREST);
                                waitedHalf = true;
                            }
                        }
                    }
                }
                ArrayList<HandlerChecker> blockedCheckers = getBlockedCheckersLocked();
                String subject = describeCheckersLocked(blockedCheckers);
                boolean allowRestart = this.mAllowRestart;
                Slog.e(TAG, "**SWT happen **" + subject);
                DumpStackAndAddDropbox(!waitedHalf, subject);
                synchronized (this) {
                    controller = this.mController;
                }
                if (!(this.mSFHang || controller == null)) {
                    Slog.i(TAG, "Reporting stuck state to activity controller");
                    try {
                        Binder.setDumpDisabled("Service dumps disabled due to hung system process.");
                        Slog.i(TAG, "Binder.setDumpDisabled");
                        if (controller.systemNotResponding(subject) >= 0) {
                            Slog.i(TAG, "Activity controller requested to coninue to wait");
                            waitedHalf = false;
                        } else {
                            Slog.i(TAG, "Activity controller requested to reboot");
                        }
                    } catch (RemoteException e2) {
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
                    AgingCriticalEvent instance = AgingCriticalEvent.getInstance();
                    String str = AgingCriticalEvent.EVENT_SYSTEMSERVER_WATCHDOG;
                    String[] strArr = new String[1];
                    strArr[0] = subject;
                    instance.writeEvent(str, strArr);
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
                    }
                    if (this.mSFHang) {
                        Slog.v(TAG, "killing surfaceflinger for surfaceflinger hang");
                        String[] sf = new String[1];
                        sf[0] = "/system/bin/surfaceflinger";
                        int[] pid_sf = Process.getPidsForCommands(sf);
                        if (pid_sf[0] > 0) {
                            Process.killProcess(pid_sf[0]);
                        }
                        Slog.v(TAG, "killing surfaceflinger end");
                    } else {
                        Slog.d(TAG, "systemserver SWT to NE");
                        Process.sendSignal(Process.myPid(), 31);
                    }
                    this.exceptionHWT.WDTMatterJava(330);
                    SystemProperties.set("ctl.restart", "zygote_secondary");
                    try {
                        Thread.sleep(5000);
                    } catch (Exception e3) {
                    }
                    SystemProperties.set("ctl.restart", "zygote");
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

    private File dumpKernelStackTraces() {
        String tracesPath = SystemProperties.get("dalvik.vm.stack-trace-file", null);
        if (tracesPath == null || tracesPath.length() == 0) {
            return null;
        }
        native_dumpKernelStacks(tracesPath);
        return new File(tracesPath);
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

    public void DumpStackAndAddDropbox(boolean clearTrace, String subject) {
        String str;
        final String name = (this.mSFHang && subject.isEmpty()) ? "surfaceflinger  hang." : IElsaManager.EMPTY_PACKAGE;
        if (name.isEmpty()) {
            str = subject;
        } else {
            str = name;
        }
        EventLog.writeEvent(EventLogTags.WATCHDOG, str);
        if (this.exceptionHWT != null) {
            this.exceptionHWT.WDTMatterJava(420);
        }
        ArrayList pids = new ArrayList();
        pids.add(Integer.valueOf(Process.myPid()));
        if (this.mPhonePid > 0) {
            pids.add(Integer.valueOf(this.mPhonePid));
        }
        final File stack = ActivityManagerService.dumpStackTraces(clearTrace, pids, null, null, NATIVE_STACKS_OF_INTEREST);
        SystemClock.sleep(2000);
        dumpKernelStackTraces();
        doSysRq('w');
        doSysRq('l');
        Slog.v(TAG, "** save all info before killnig system server **");
        final String str2 = subject;
        Thread dropboxThread = new Thread("watchdogWriteToDropbox") {
            public void run() {
                Slog.v(Watchdog.TAG, "** start addErrorToDropBox **");
                Watchdog.this.mActivity.addErrorToDropBox("watchdog", null, "system_server", null, null, name.isEmpty() ? str2 : name, null, stack, null);
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

    private void enableFutexwaitCheckIfNeeded() {
        if (SystemProperties.getBoolean("ro.build.release_type", false)) {
            SystemProperties.set("persist.sys.oppo.checkfutexwait", "true");
        }
    }
}
