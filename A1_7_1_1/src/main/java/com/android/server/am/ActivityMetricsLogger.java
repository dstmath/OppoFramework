package com.android.server.am;

import android.app.ActivityManager.StackId;
import android.content.Context;
import android.os.SystemClock;
import android.util.Slog;
import com.android.internal.logging.MetricsLogger;
import java.util.ArrayList;

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
class ActivityMetricsLogger {
    private static final long INVALID_START_TIME = -1;
    private static final String TAG = null;
    private static final String[] TRON_WINDOW_STATE_VARZ_STRINGS = null;
    private static final int WINDOW_STATE_FREEFORM = 2;
    private static final int WINDOW_STATE_INVALID = -1;
    private static final int WINDOW_STATE_SIDE_BY_SIDE = 1;
    private static final int WINDOW_STATE_STANDARD = 0;
    private final Context mContext;
    private long mCurrentTransitionStartTime;
    private long mLastLogTimeSecs;
    private boolean mLoggedStartingWindowDrawn;
    private boolean mLoggedTransitionStarting;
    private boolean mLoggedWindowsDrawn;
    private final ActivityStackSupervisor mSupervisor;
    private int mWindowState;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.am.ActivityMetricsLogger.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.am.ActivityMetricsLogger.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.am.ActivityMetricsLogger.<clinit>():void");
    }

    ActivityMetricsLogger(ActivityStackSupervisor supervisor, Context context) {
        this.mWindowState = 0;
        this.mCurrentTransitionStartTime = -1;
        this.mLastLogTimeSecs = SystemClock.elapsedRealtime() / 1000;
        this.mSupervisor = supervisor;
        this.mContext = context;
    }

    void logWindowState() {
        long now = SystemClock.elapsedRealtime() / 1000;
        if (this.mWindowState != -1) {
            MetricsLogger.count(this.mContext, TRON_WINDOW_STATE_VARZ_STRINGS[this.mWindowState], (int) (now - this.mLastLogTimeSecs));
        }
        this.mLastLogTimeSecs = now;
        ActivityStack stack = this.mSupervisor.getStack(3);
        if (stack == null || stack.getStackVisibilityLocked(null) == 0) {
            this.mWindowState = -1;
            stack = this.mSupervisor.getFocusedStack();
            if (stack.mStackId == 4) {
                stack = this.mSupervisor.findStackBehind(stack);
            }
            if (stack.mStackId == 0 || stack.mStackId == 1) {
                this.mWindowState = 0;
            } else if (stack.mStackId == 3) {
                Slog.wtf(TAG, "Docked stack shouldn't be the focused stack, because it reported not being visible.");
                this.mWindowState = -1;
            } else if (stack.mStackId == 2) {
                this.mWindowState = 2;
            } else if (StackId.isStaticStack(stack.mStackId)) {
                throw new IllegalStateException("Unknown stack=" + stack);
            }
            return;
        }
        this.mWindowState = 1;
    }

    void notifyActivityLaunching() {
        this.mCurrentTransitionStartTime = System.currentTimeMillis();
    }

    void notifyActivityLaunched(int resultCode, ActivityRecord launchedActivity) {
        String componentName;
        boolean processSwitch = true;
        ProcessRecord processRecord = null;
        if (launchedActivity != null) {
            processRecord = (ProcessRecord) this.mSupervisor.mService.mProcessNames.get(launchedActivity.processName, launchedActivity.appInfo.uid);
        }
        boolean processRunning = processRecord != null;
        if (launchedActivity != null) {
            componentName = launchedActivity.shortComponentName;
        } else {
            componentName = null;
        }
        if (processRecord != null && hasStartedActivity(processRecord, launchedActivity)) {
            processSwitch = false;
        }
        notifyActivityLaunched(resultCode, componentName, processRunning, processSwitch);
    }

    private boolean hasStartedActivity(ProcessRecord record, ActivityRecord launchedActivity) {
        ArrayList<ActivityRecord> activities = record.activities;
        for (int i = activities.size() - 1; i >= 0; i--) {
            ActivityRecord activity = (ActivityRecord) activities.get(i);
            if (launchedActivity != activity && !activity.stopped) {
                return true;
            }
        }
        return false;
    }

    private void notifyActivityLaunched(int resultCode, String componentName, boolean processRunning, boolean processSwitch) {
        if (resultCode < 0 || componentName == null || !processSwitch) {
            reset();
            return;
        }
        MetricsLogger.action(this.mContext, 323, componentName);
        MetricsLogger.action(this.mContext, 324, processRunning);
        MetricsLogger.action(this.mContext, 325, (int) (SystemClock.uptimeMillis() / 1000));
    }

    void notifyWindowsDrawn() {
        if (isTransitionActive() && !this.mLoggedWindowsDrawn) {
            MetricsLogger.action(this.mContext, 322, calculateCurrentDelay());
            this.mLoggedWindowsDrawn = true;
            if (this.mLoggedTransitionStarting) {
                reset();
            }
        }
    }

    void notifyStartingWindowDrawn() {
        if (isTransitionActive() && !this.mLoggedStartingWindowDrawn) {
            this.mLoggedStartingWindowDrawn = true;
            MetricsLogger.action(this.mContext, 321, calculateCurrentDelay());
        }
    }

    void notifyTransitionStarting(int reason) {
        if (isTransitionActive() && !this.mLoggedTransitionStarting) {
            MetricsLogger.action(this.mContext, OppoProcessManager.MSG_SUSPEND_PROCESS_DELAY, reason);
            MetricsLogger.action(this.mContext, OppoProcessManager.MSG_SUSPEND_PROCESS, calculateCurrentDelay());
            this.mLoggedTransitionStarting = true;
            if (this.mLoggedWindowsDrawn) {
                reset();
            }
        }
    }

    private boolean isTransitionActive() {
        return this.mCurrentTransitionStartTime != -1;
    }

    private void reset() {
        this.mCurrentTransitionStartTime = -1;
        this.mLoggedWindowsDrawn = false;
        this.mLoggedTransitionStarting = false;
        this.mLoggedStartingWindowDrawn = false;
    }

    private int calculateCurrentDelay() {
        return (int) (System.currentTimeMillis() - this.mCurrentTransitionStartTime);
    }
}
