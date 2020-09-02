package com.android.server.wm;

import android.annotation.OppoHook;
import android.app.ActivityOptions;
import android.app.AppOpsManager;
import android.app.ProfilerInfo;
import android.app.ResultInfo;
import android.app.WaitResult;
import android.app.servertransaction.ActivityLifecycleItem;
import android.app.servertransaction.ClientTransaction;
import android.app.servertransaction.LaunchActivityItem;
import android.app.servertransaction.PauseActivityItem;
import android.app.servertransaction.ResumeActivityItem;
import android.common.OppoFeatureCache;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Binder;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.Trace;
import android.os.UserManager;
import android.os.WorkSource;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.EventLog;
import android.util.MergedConfiguration;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseIntArray;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.content.ReferrerIntent;
import com.android.internal.os.TransferPipe;
import com.android.internal.os.logging.MetricsLoggerWrapper;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.function.pooled.PooledLambda;
import com.android.server.am.AppTimeTracker;
import com.android.server.am.EventLogTags;
import com.android.server.am.IColorAppCrashClearManager;
import com.android.server.am.IColorAppStartupManager;
import com.android.server.am.IColorMultiAppManager;
import com.android.server.am.UserState;
import com.android.server.pm.DumpState;
import com.android.server.wm.ActivityStack;
import com.android.server.wm.RecentTasks;
import com.oppo.hypnus.Hypnus;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ActivityStackSupervisor extends OppoBaseActivityStackSupervisor implements RecentTasks.Callbacks {
    private static final ArrayMap<String, String> ACTION_TO_RUNTIME_PERMISSION = new ArrayMap<>();
    private static final int ACTIVITY_RESTRICTION_APPOP = 2;
    private static final int ACTIVITY_RESTRICTION_NONE = 0;
    private static final int ACTIVITY_RESTRICTION_PERMISSION = 1;
    static final boolean DEFER_RESUME = true;
    static final int IDLE_NOW_MSG = 201;
    static final int IDLE_TIMEOUT = 10000;
    static final int IDLE_TIMEOUT_MSG = 200;
    static final int LAUNCH_TASK_BEHIND_COMPLETE = 212;
    static final int LAUNCH_TIMEOUT = 10000;
    static final int LAUNCH_TIMEOUT_MSG = 204;
    private static final int MAX_TASK_IDS_PER_USER = 100000;
    static final boolean ON_TOP = true;
    static final boolean PAUSE_IMMEDIATELY = true;
    static final boolean PRESERVE_WINDOWS = true;
    static final boolean REMOVE_FROM_RECENTS = true;
    static final int REPORT_HOME_CHANGED_MSG = 216;
    static final int REPORT_MULTI_WINDOW_MODE_CHANGED_MSG = 214;
    static final int REPORT_PIP_MODE_CHANGED_MSG = 215;
    static final int RESTART_ACTIVITY_PROCESS_TIMEOUT_MSG = 213;
    static final int RESUME_TOP_ACTIVITY_MSG = 202;
    static final int SLEEP_TIMEOUT = 5000;
    static final int SLEEP_TIMEOUT_MSG = 203;
    private static final String TAG = "ActivityTaskManager";
    /* access modifiers changed from: private */
    public static final String TAG_IDLE = (TAG + ActivityTaskManagerDebugConfig.POSTFIX_IDLE);
    private static final String TAG_PAUSE = (TAG + ActivityTaskManagerDebugConfig.POSTFIX_PAUSE);
    private static final String TAG_RECENTS = (TAG + ActivityTaskManagerDebugConfig.POSTFIX_RECENTS);
    private static final String TAG_STACK = (TAG + ActivityTaskManagerDebugConfig.POSTFIX_STACK);
    private static final String TAG_SWITCH = (TAG + ActivityTaskManagerDebugConfig.POSTFIX_SWITCH);
    static final String TAG_TASKS = (TAG + ActivityTaskManagerDebugConfig.POSTFIX_TASKS);
    static final int TOP_RESUMED_STATE_LOSS_TIMEOUT = 500;
    static final int TOP_RESUMED_STATE_LOSS_TIMEOUT_MSG = 217;
    static final boolean VALIDATE_WAKE_LOCK_CALLER = false;
    public static Hypnus mHyp = null;
    private ActivityMetricsLogger mActivityMetricsLogger;
    private boolean mAllowDockedStackResize = true;
    boolean mAppVisibilitiesChangedSinceLastPause;
    private final SparseIntArray mCurTaskIdForUser = new SparseIntArray(20);
    private int mDeferResumeCount;
    private boolean mDockedStackResizing;
    final ArrayList<ActivityRecord> mFinishingActivities = new ArrayList<>();
    final ArrayList<ActivityRecord> mGoingToSleepActivities = new ArrayList<>();
    PowerManager.WakeLock mGoingToSleepWakeLock;
    final ActivityStackSupervisorHandler mHandler;
    private boolean mHasPendingDockedBounds;
    private boolean mInitialized;
    private KeyguardController mKeyguardController;
    private LaunchParamsController mLaunchParamsController;
    LaunchParamsPersister mLaunchParamsPersister;
    PowerManager.WakeLock mLaunchingActivityWakeLock;
    final Looper mLooper;
    final ArrayList<ActivityRecord> mMultiWindowModeChangedActivities = new ArrayList<>();
    final ArrayList<ActivityRecord> mNoAnimActivities = new ArrayList<>();
    @OppoHook(level = OppoHook.OppoHookType.NEW_FIELD, note = "ZhiYong.Lin@Plf.Framework, modify for secure protect", property = OppoHook.OppoRomType.ROM)
    OppoSecureProtectUtils mOppoSecureProtectUtils = new OppoSecureProtectUtils();
    private Rect mPendingDockedBounds;
    private Rect mPendingTempDockedTaskBounds;
    private Rect mPendingTempDockedTaskInsetBounds;
    private Rect mPendingTempOtherTaskBounds;
    private Rect mPendingTempOtherTaskInsetBounds;
    PersisterQueue mPersisterQueue;
    final ArrayList<ActivityRecord> mPipModeChangedActivities = new ArrayList<>();
    Rect mPipModeChangedTargetStackBounds;
    private PowerManager mPowerManager;
    private ActivityRecord mPreTopActivity;
    RecentTasks mRecentTasks;
    private final ArraySet<Integer> mResizingTasksDuringAnimation = new ArraySet<>();
    RootActivityContainer mRootActivityContainer;
    RunningTasks mRunningTasks;
    final ActivityTaskManagerService mService;
    final ArrayList<UserState> mStartingUsers = new ArrayList<>();
    final ArrayList<ActivityRecord> mStoppingActivities = new ArrayList<>();
    private final ActivityOptions mTmpOptions = ActivityOptions.makeBasic();
    private ActivityRecord mTopResumedActivity;
    private boolean mTopResumedActivityWaitingForPrev;
    boolean mUserLeaving = false;
    final ArrayList<WaitResult> mWaitingActivityLaunched = new ArrayList<>();
    private final ArrayList<WaitInfo> mWaitingForActivityVisible = new ArrayList<>();
    WindowManagerService mWindowManager;
    private final Rect tempRect = new Rect();

    static {
        ACTION_TO_RUNTIME_PERMISSION.put("android.media.action.IMAGE_CAPTURE", "android.permission.CAMERA");
        ACTION_TO_RUNTIME_PERMISSION.put("android.media.action.VIDEO_CAPTURE", "android.permission.CAMERA");
        ACTION_TO_RUNTIME_PERMISSION.put("android.intent.action.CALL", "android.permission.CALL_PHONE");
    }

    /* access modifiers changed from: package-private */
    public boolean canPlaceEntityOnDisplay(int displayId, int callingPid, int callingUid, ActivityInfo activityInfo) {
        if (displayId == 0) {
            return true;
        }
        if (this.mService.mSupportsMultiDisplay && isCallerAllowedToLaunchOnDisplay(callingPid, callingUid, displayId, activityInfo)) {
            return true;
        }
        return false;
    }

    static class PendingActivityLaunch {
        final WindowProcessController callerApp;
        final ActivityRecord r;
        final ActivityRecord sourceRecord;
        final ActivityStack stack;
        final int startFlags;

        PendingActivityLaunch(ActivityRecord _r, ActivityRecord _sourceRecord, int _startFlags, ActivityStack _stack, WindowProcessController app) {
            this.r = _r;
            this.sourceRecord = _sourceRecord;
            this.startFlags = _startFlags;
            this.stack = _stack;
            this.callerApp = app;
        }

        /* access modifiers changed from: package-private */
        public void sendErrorResult(String message) {
            try {
                if (this.callerApp != null && this.callerApp.hasThread()) {
                    this.callerApp.getThread().scheduleCrash(message);
                }
            } catch (RemoteException e) {
                Slog.e(ActivityStackSupervisor.TAG, "Exception scheduling crash of failed activity launcher sourceRecord=" + this.sourceRecord, e);
            }
        }
    }

    public ActivityStackSupervisor(ActivityTaskManagerService service, Looper looper) {
        this.mService = service;
        this.mLooper = looper;
        this.mHandler = new ActivityStackSupervisorHandler(looper);
    }

    public void initialize() {
        if (!this.mInitialized) {
            this.mInitialized = true;
            this.mRunningTasks = createRunningTasks();
            this.mActivityMetricsLogger = new ActivityMetricsLogger(this, this.mService.mContext, this.mHandler.getLooper());
            this.mKeyguardController = new KeyguardController(this.mService, this);
            this.mPersisterQueue = new PersisterQueue();
            this.mLaunchParamsPersister = new LaunchParamsPersister(this.mPersisterQueue, this);
            this.mLaunchParamsController = new LaunchParamsController(this.mService, this.mLaunchParamsPersister);
            this.mLaunchParamsController.registerDefaultModifiers(this);
        }
    }

    /* access modifiers changed from: package-private */
    public void onSystemReady() {
        this.mLaunchParamsPersister.onSystemReady();
    }

    /* access modifiers changed from: package-private */
    public void onUserUnlocked(int userId) {
        this.mPersisterQueue.startPersisting();
        this.mLaunchParamsPersister.onUnlockUser(userId);
    }

    public ActivityMetricsLogger getActivityMetricsLogger() {
        return this.mActivityMetricsLogger;
    }

    public KeyguardController getKeyguardController() {
        return this.mKeyguardController;
    }

    /* access modifiers changed from: package-private */
    public void setRecentTasks(RecentTasks recentTasks) {
        this.mRecentTasks = recentTasks;
        this.mRecentTasks.registerCallback(this);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public RunningTasks createRunningTasks() {
        return new RunningTasks();
    }

    /* access modifiers changed from: package-private */
    public void initPowerManagement() {
        this.mPowerManager = (PowerManager) this.mService.mContext.getSystemService(PowerManager.class);
        this.mGoingToSleepWakeLock = this.mPowerManager.newWakeLock(1, "ActivityManager-Sleep");
        this.mLaunchingActivityWakeLock = this.mPowerManager.newWakeLock(1, "*launch*");
        this.mLaunchingActivityWakeLock.setReferenceCounted(false);
    }

    /* access modifiers changed from: package-private */
    public void setWindowManager(WindowManagerService wm) {
        this.mWindowManager = wm;
        getKeyguardController().setWindowManager(wm);
    }

    /* access modifiers changed from: package-private */
    public void moveRecentsStackToFront(String reason) {
        ActivityStack recentsStack = this.mRootActivityContainer.getDefaultDisplay().getStack(0, 3);
        if (recentsStack != null) {
            recentsStack.moveToFront(reason);
        }
    }

    /* access modifiers changed from: package-private */
    public void setNextTaskIdForUserLocked(int taskId, int userId) {
        if (taskId > this.mCurTaskIdForUser.get(userId, -1)) {
            this.mCurTaskIdForUser.put(userId, taskId);
        }
    }

    static int nextTaskIdForUser(int taskId, int userId) {
        int nextTaskId = taskId + 1;
        if (nextTaskId == (userId + 1) * MAX_TASK_IDS_PER_USER) {
            return nextTaskId - MAX_TASK_IDS_PER_USER;
        }
        return nextTaskId;
    }

    /* access modifiers changed from: package-private */
    public int getNextTaskIdForUserLocked(int userId) {
        int currentTaskId = this.mCurTaskIdForUser.get(userId, MAX_TASK_IDS_PER_USER * userId);
        int candidateTaskId = nextTaskIdForUser(currentTaskId, userId);
        do {
            if (this.mRecentTasks.containsTaskId(candidateTaskId, userId) || this.mRootActivityContainer.anyTaskForId(candidateTaskId, 1) != null) {
                candidateTaskId = nextTaskIdForUser(candidateTaskId, userId);
            } else {
                this.mCurTaskIdForUser.put(userId, candidateTaskId);
                return candidateTaskId;
            }
        } while (candidateTaskId != currentTaskId);
        throw new IllegalStateException("Cannot get an available task id. Reached limit of 100000 running tasks per user.");
    }

    /* access modifiers changed from: package-private */
    public void waitActivityVisible(ComponentName name, WaitResult result, long startTimeMs) {
        this.mWaitingForActivityVisible.add(new WaitInfo(name, result, startTimeMs));
    }

    /* access modifiers changed from: package-private */
    public void cleanupActivity(ActivityRecord r) {
        this.mFinishingActivities.remove(r);
        stopWaitingForActivityVisible(r);
    }

    /* access modifiers changed from: package-private */
    public void stopWaitingForActivityVisible(ActivityRecord r) {
        boolean changed = false;
        for (int i = this.mWaitingForActivityVisible.size() - 1; i >= 0; i--) {
            WaitInfo w = this.mWaitingForActivityVisible.get(i);
            if (w.matches(r.mActivityComponent)) {
                WaitResult result = w.getResult();
                changed = true;
                result.timeout = false;
                result.who = w.getComponent();
                result.totalTime = SystemClock.uptimeMillis() - w.getStartTime();
                this.mWaitingForActivityVisible.remove(w);
            }
        }
        if (changed) {
            this.mService.mGlobalLock.notifyAll();
        }
    }

    /* access modifiers changed from: package-private */
    public void reportWaitingActivityLaunchedIfNeeded(ActivityRecord r, int result) {
        if (!this.mWaitingActivityLaunched.isEmpty()) {
            if (result == 3 || result == 2) {
                boolean changed = false;
                for (int i = this.mWaitingActivityLaunched.size() - 1; i >= 0; i--) {
                    WaitResult w = this.mWaitingActivityLaunched.remove(i);
                    if (w.who == null) {
                        changed = true;
                        w.result = result;
                        if (result == 3) {
                            w.who = r.mActivityComponent;
                        }
                    }
                }
                if (changed) {
                    this.mService.mGlobalLock.notifyAll();
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void reportActivityLaunchedLocked(boolean timeout, ActivityRecord r, long totalTime, int launchState) {
        boolean changed = false;
        for (int i = this.mWaitingActivityLaunched.size() - 1; i >= 0; i--) {
            WaitResult w = this.mWaitingActivityLaunched.remove(i);
            if (w.who == null) {
                changed = true;
                w.timeout = timeout;
                if (r != null) {
                    w.who = new ComponentName(r.info.packageName, r.info.name);
                }
                w.totalTime = totalTime;
                w.launchState = launchState;
            }
        }
        if (changed) {
            this.mService.mGlobalLock.notifyAll();
        }
    }

    /* access modifiers changed from: package-private */
    public ActivityInfo resolveActivity(Intent intent, ResolveInfo rInfo, int startFlags, ProfilerInfo profilerInfo) {
        ActivityInfo aInfo = rInfo != null ? rInfo.activityInfo : null;
        if (aInfo != null) {
            intent.setComponent(new ComponentName(aInfo.applicationInfo.packageName, aInfo.name));
            if (!aInfo.processName.equals("system") && !((startFlags & 14) == 0 && profilerInfo == null)) {
                synchronized (this.mService.mGlobalLock) {
                    try {
                        WindowManagerService.boostPriorityForLockedSection();
                        this.mService.mH.sendMessage(PooledLambda.obtainMessage($$Lambda$8ew6SY_v_7ex9pwFGDswbkGWuXc.INSTANCE, this.mService.mAmInternal, aInfo, Integer.valueOf(startFlags), profilerInfo, this.mService.mGlobalLock));
                        try {
                            this.mService.mGlobalLock.wait();
                        } catch (InterruptedException e) {
                        }
                    } finally {
                        WindowManagerService.resetPriorityAfterLockedSection();
                    }
                }
            }
            String intentLaunchToken = intent.getLaunchToken();
            if (aInfo.launchToken == null && intentLaunchToken != null) {
                aInfo.launchToken = intentLaunchToken;
            }
        }
        return aInfo;
    }

    /* access modifiers changed from: package-private */
    public ResolveInfo resolveIntent(Intent intent, String resolvedType, int userId, int flags, int filterCallingUid) {
        int modifiedFlags;
        try {
            Trace.traceBegin(64, "resolveIntent");
            int modifiedFlags2 = flags | 65536 | 1024;
            if (!intent.isWebIntent()) {
                if ((intent.getFlags() & 2048) == 0) {
                    modifiedFlags = modifiedFlags2;
                    setOppoCallingUid(intent);
                    long token = Binder.clearCallingIdentity();
                    ResolveInfo resolveIntent = this.mService.getPackageManagerInternalLocked().resolveIntent(intent, resolvedType, modifiedFlags, userId, true, filterCallingUid);
                    Trace.traceEnd(64);
                    return resolveIntent;
                }
            }
            modifiedFlags = modifiedFlags2 | DumpState.DUMP_VOLUMES;
            setOppoCallingUid(intent);
            long token2 = Binder.clearCallingIdentity();
            try {
                ResolveInfo resolveIntent2 = this.mService.getPackageManagerInternalLocked().resolveIntent(intent, resolvedType, modifiedFlags, userId, true, filterCallingUid);
            } finally {
                Binder.restoreCallingIdentity(token2);
            }
            try {
                Trace.traceEnd(64);
                return resolveIntent2;
            } catch (Throwable th) {
                th = th;
                Trace.traceEnd(64);
                throw th;
            }
        } catch (Throwable th2) {
            th = th2;
            Trace.traceEnd(64);
            throw th;
        }
    }

    /* access modifiers changed from: package-private */
    public ActivityInfo resolveActivity(Intent intent, String resolvedType, int startFlags, ProfilerInfo profilerInfo, int userId, int filterCallingUid) {
        return resolveActivity(intent, resolveIntent(intent, resolvedType, userId, 0, filterCallingUid), startFlags, profilerInfo);
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:112:0x02de, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:114:0x02e3, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:115:0x02e4, code lost:
        r6 = com.android.server.wm.ActivityStackSupervisor.TAG;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:116:0x02e6, code lost:
        r4 = r12;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:138:0x0373, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:139:0x0374, code lost:
        r4 = r12;
        r6 = com.android.server.wm.ActivityStackSupervisor.TAG;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:140:0x0379, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:162:0x03b6, code lost:
        android.util.Slog.e(r6, "Second failure launching " + r39.intent.getComponent().flattenToShortString() + ", giving up", r0);
        r40.appDied();
        r4.requestFinishActivityLocked(r39.appToken, 0, null, "2nd-crash", false);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:163:0x03e7, code lost:
        endDeferResume();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:164:0x03ec, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:165:0x03ed, code lost:
        r39.launchFailed = true;
        r40.removeActivity(r39);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:166:0x03f4, code lost:
        throw r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:90:0x0271, code lost:
        r0 = th;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:120:0x02fc  */
    /* JADX WARNING: Removed duplicated region for block: B:127:0x0325  */
    /* JADX WARNING: Removed duplicated region for block: B:131:0x0358  */
    /* JADX WARNING: Removed duplicated region for block: B:135:0x0365 A[Catch:{ NullPointerException -> 0x036b }] */
    /* JADX WARNING: Removed duplicated region for block: B:140:0x0379 A[ExcHandler: all (th java.lang.Throwable), Splitter:B:82:0x023d] */
    /* JADX WARNING: Removed duplicated region for block: B:152:0x0399  */
    /* JADX WARNING: Removed duplicated region for block: B:162:0x03b6 A[Catch:{ all -> 0x03f5 }] */
    /* JADX WARNING: Removed duplicated region for block: B:165:0x03ed  */
    /* JADX WARNING: Removed duplicated region for block: B:176:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x006a A[SYNTHETIC, Splitter:B:24:0x006a] */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x0077 A[Catch:{ all -> 0x006e }] */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x008a  */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x0093  */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x009a  */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x00f5  */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x012f  */
    /* JADX WARNING: Removed duplicated region for block: B:90:0x0271 A[ExcHandler: all (th java.lang.Throwable), Splitter:B:88:0x0268] */
    public boolean realStartActivityLocked(ActivityRecord r, WindowProcessController proc, boolean andResume, boolean checkConfig) throws RemoteException {
        boolean andResume2;
        int applicationInfoUid;
        LockTaskController lockTaskController;
        String str;
        ActivityStack stack;
        List<ReferrerIntent> newIntents;
        ActivityLifecycleItem lifecycleItem;
        if (!this.mRootActivityContainer.allPausedActivitiesComplete()) {
            if (ActivityTaskManagerDebugConfig.DEBUG_SWITCH || ActivityTaskManagerDebugConfig.DEBUG_PAUSE || ActivityTaskManagerDebugConfig.DEBUG_STATES) {
                Slog.v(TAG_PAUSE, "realStartActivityLocked: Skipping start of r=" + r + " some activities pausing...");
            }
            return false;
        }
        TaskRecord task = r.getTaskRecord();
        ActivityStack stack2 = task.getStack();
        beginDeferResume();
        try {
            r.startFreezingScreenLocked(proc, 0);
            r.startLaunchTickingLocked();
            r.setProcess(proc);
            if (andResume) {
                try {
                    if (!r.canResumeByCompat()) {
                        andResume2 = false;
                        if (getKeyguardController().isKeyguardLocked()) {
                            try {
                                r.notifyUnknownVisibilityLaunched();
                            } catch (Throwable th) {
                                e = th;
                                endDeferResume();
                                throw e;
                            }
                        }
                        if (checkConfig) {
                            this.mRootActivityContainer.ensureVisibilityAndConfig(r, r.getDisplayId(), false, true);
                        }
                        if (r.getActivityStack().checkKeyguardVisibility(r, true, true)) {
                            r.setVisibility(true);
                        }
                        applicationInfoUid = r.info.applicationInfo == null ? r.info.applicationInfo.uid : -1;
                        if (!(r.mUserId == proc.mUserId && r.appInfo.uid == applicationInfoUid)) {
                            Slog.wtf(TAG, "User ID for activity changing for " + r + " appInfo.uid=" + r.appInfo.uid + " info.ai.uid=" + applicationInfoUid + " old=" + r.app + " new=" + proc);
                        }
                        r.launchCount++;
                        r.lastLaunchTime = SystemClock.uptimeMillis();
                        if (ActivityTaskManagerDebugConfig.DEBUG_ALL) {
                            Slog.v(TAG, "Launching: " + r);
                        }
                        proc.addActivityIfNeeded(r);
                        lockTaskController = this.mService.getLockTaskController();
                        if (task.mLockTaskAuth == 2 || task.mLockTaskAuth == 4 || (task.mLockTaskAuth == 3 && lockTaskController.getLockTaskModeState() == 1)) {
                            lockTaskController.startLockTaskMode(task, false, 0);
                        }
                        try {
                            if (!proc.hasThread()) {
                                List<ResultInfo> results = null;
                                if (andResume2) {
                                    try {
                                        results = r.results;
                                        newIntents = r.newIntents;
                                    } catch (RemoteException e) {
                                        e = e;
                                        str = TAG;
                                        stack = stack2;
                                        try {
                                            if (!r.launchFailed) {
                                            }
                                        } catch (Throwable th2) {
                                            e = th2;
                                            endDeferResume();
                                            throw e;
                                        }
                                    }
                                } else {
                                    newIntents = null;
                                }
                                if (ActivityTaskManagerDebugConfig.DEBUG_SWITCH) {
                                    Slog.v(TAG_SWITCH, "Launching: " + r + " icicle=" + r.icicle + " with results=" + results + " newIntents=" + newIntents + " andResume=" + andResume2);
                                }
                                EventLog.writeEvent((int) EventLogTags.AM_RESTART_ACTIVITY, Integer.valueOf(r.mUserId), Integer.valueOf(System.identityHashCode(r)), Integer.valueOf(task.taskId), r.shortComponentName);
                                if (r.isActivityTypeHome()) {
                                    updateHomeProcess(task.mActivities.get(0).app);
                                }
                                this.mService.getPackageManagerInternalLocked().notifyPackageUse(r.intent.getComponent().getPackageName(), 0);
                                r.sleeping = false;
                                r.forceNewConfig = false;
                                this.mService.getAppWarningsLocked().onStartActivity(r);
                                r.compat = this.mService.compatibilityInfoForPackageLocked(r.info.applicationInfo);
                                MergedConfiguration mergedConfiguration = new MergedConfiguration(proc.getConfiguration(), r.getMergedOverrideConfiguration());
                                r.setLastReportedConfiguration(mergedConfiguration);
                                logIfTransactionTooLarge(r.intent, r.icicle);
                                ClientTransaction clientTransaction = ClientTransaction.obtain(proc.getThread(), r.appToken);
                                DisplayContent dc = r.getDisplay().mDisplayContent;
                                try {
                                    try {
                                        try {
                                            try {
                                                clientTransaction.addCallback(LaunchActivityItem.obtain(new Intent(r.intent), System.identityHashCode(r), r.info, mergedConfiguration.getGlobalConfiguration(), mergedConfiguration.getOverrideConfiguration(), r.compat, r.launchedFromPackage, task.voiceInteractor, proc.getReportedProcState(), r.icicle, r.persistentState, results, newIntents, dc.isNextTransitionForward(), proc.createProfilerInfoIfNeeded(), r.assistToken));
                                                if (andResume2) {
                                                    try {
                                                        lifecycleItem = ResumeActivityItem.obtain(dc.isNextTransitionForward());
                                                    } catch (RemoteException e2) {
                                                        e = e2;
                                                        stack = stack2;
                                                        str = TAG;
                                                    } catch (Throwable th3) {
                                                    }
                                                } else {
                                                    lifecycleItem = PauseActivityItem.obtain();
                                                }
                                                clientTransaction.setLifecycleStateRequest(lifecycleItem);
                                                this.mService.getLifecycleManager().scheduleTransaction(clientTransaction);
                                                if ((proc.mInfo.privateFlags & 2) != 0) {
                                                    if (this.mService.mHasHeavyWeightFeature) {
                                                        if (proc.mName.equals(proc.mInfo.packageName)) {
                                                            if (this.mService.mHeavyWeightProcess == null || this.mService.mHeavyWeightProcess == proc) {
                                                                str = TAG;
                                                            } else {
                                                                String str2 = "Starting new heavy weight process " + proc + " when already running " + this.mService.mHeavyWeightProcess;
                                                                str = TAG;
                                                                Slog.w(str, str2);
                                                            }
                                                            this.mService.setHeavyWeightProcess(r);
                                                        } else {
                                                            str = TAG;
                                                        }
                                                        endDeferResume();
                                                        r.launchFailed = false;
                                                        if (stack2.updateLRUListLocked(r)) {
                                                            Slog.w(str, "Activity " + r + " being launched, but already in LRU list");
                                                        }
                                                        if (andResume2 || !readyToResume()) {
                                                            if (ActivityTaskManagerDebugConfig.DEBUG_STATES) {
                                                                Slog.v(RootActivityContainer.TAG_STATES, "Moving to PAUSED: " + r + " (starting in paused state)");
                                                            }
                                                            r.setState(ActivityStack.ActivityState.PAUSED, "realStartActivityLocked");
                                                        } else {
                                                            stack2.minimalResumeActivityLocked(r);
                                                        }
                                                        proc.onStartActivity(this.mService.mTopProcessState, r.info);
                                                        if (this.mRootActivityContainer.isTopDisplayFocusedStack(stack2)) {
                                                            this.mService.getActivityStartController().startSetupActivity();
                                                        }
                                                        if (r.app != null) {
                                                            return true;
                                                        }
                                                        r.app.updateServiceConnectionActivities();
                                                        return true;
                                                    }
                                                }
                                                str = TAG;
                                                endDeferResume();
                                                r.launchFailed = false;
                                                if (stack2.updateLRUListLocked(r)) {
                                                }
                                                if (andResume2) {
                                                }
                                                if (ActivityTaskManagerDebugConfig.DEBUG_STATES) {
                                                }
                                                r.setState(ActivityStack.ActivityState.PAUSED, "realStartActivityLocked");
                                                proc.onStartActivity(this.mService.mTopProcessState, r.info);
                                                if (this.mRootActivityContainer.isTopDisplayFocusedStack(stack2)) {
                                                }
                                                try {
                                                    if (r.app != null) {
                                                    }
                                                } catch (NullPointerException e3) {
                                                    Slog.w(str, "updateServiceConnectionActivities catch NullPointerException", e3);
                                                    return true;
                                                }
                                            } catch (RemoteException e4) {
                                                e = e4;
                                                str = TAG;
                                                stack = stack2;
                                                if (!r.launchFailed) {
                                                }
                                            } catch (Throwable th4) {
                                            }
                                        } catch (RemoteException e5) {
                                            e = e5;
                                            str = TAG;
                                            stack = stack2;
                                            if (!r.launchFailed) {
                                            }
                                        } catch (Throwable th5) {
                                            e = th5;
                                            endDeferResume();
                                            throw e;
                                        }
                                    } catch (RemoteException e6) {
                                        e = e6;
                                        str = TAG;
                                        stack = stack2;
                                        if (!r.launchFailed) {
                                        }
                                    }
                                } catch (RemoteException e7) {
                                    e = e7;
                                    str = TAG;
                                    stack = stack2;
                                    if (!r.launchFailed) {
                                    }
                                }
                            } else {
                                str = TAG;
                                stack = stack2;
                                try {
                                    throw new RemoteException();
                                } catch (RemoteException e8) {
                                    e = e8;
                                    if (!r.launchFailed) {
                                    }
                                }
                            }
                        } catch (RemoteException e9) {
                            e = e9;
                            str = TAG;
                            stack = stack2;
                            if (!r.launchFailed) {
                            }
                        }
                    }
                } catch (Throwable th6) {
                    e = th6;
                    endDeferResume();
                    throw e;
                }
            }
            andResume2 = andResume;
            try {
                if (getKeyguardController().isKeyguardLocked()) {
                }
                if (checkConfig) {
                }
                if (r.getActivityStack().checkKeyguardVisibility(r, true, true)) {
                }
                if (r.info.applicationInfo == null) {
                }
                Slog.wtf(TAG, "User ID for activity changing for " + r + " appInfo.uid=" + r.appInfo.uid + " info.ai.uid=" + applicationInfoUid + " old=" + r.app + " new=" + proc);
                r.launchCount++;
                r.lastLaunchTime = SystemClock.uptimeMillis();
                if (ActivityTaskManagerDebugConfig.DEBUG_ALL) {
                }
                proc.addActivityIfNeeded(r);
                lockTaskController = this.mService.getLockTaskController();
                lockTaskController.startLockTaskMode(task, false, 0);
                if (!proc.hasThread()) {
                }
            } catch (Throwable th7) {
                e = th7;
                endDeferResume();
                throw e;
            }
        } catch (Throwable th8) {
            e = th8;
            endDeferResume();
            throw e;
        }
    }

    /* access modifiers changed from: package-private */
    public void updateHomeProcess(WindowProcessController app) {
        if (app != null && this.mService.mHomeProcess != app) {
            if (!this.mHandler.hasMessages(REPORT_HOME_CHANGED_MSG)) {
                this.mHandler.sendEmptyMessage(REPORT_HOME_CHANGED_MSG);
            }
            this.mService.mHomeProcess = app;
        }
    }

    private void logIfTransactionTooLarge(Intent intent, Bundle icicle) {
        Bundle extras;
        int extrasSize = 0;
        if (!(intent == null || (extras = intent.getExtras()) == null)) {
            extrasSize = extras.getSize();
        }
        int icicleSize = icicle == null ? 0 : icicle.getSize();
        if (extrasSize + icicleSize > 200000) {
            Slog.e(TAG, "Transaction too large, intent: " + intent + ", extras size: " + extrasSize + ", icicle size: " + icicleSize);
        }
    }

    /* access modifiers changed from: package-private */
    public void startSpecificActivityLocked(ActivityRecord r, boolean andResume, boolean checkConfig) {
        WindowProcessController wpc = this.mService.getProcessController(r.processName, r.info.applicationInfo.uid);
        boolean knownToBeDead = false;
        r.setLaunchTimeStart();
        if (wpc != null && wpc.hasThread()) {
            try {
                realStartActivityLocked(r, wpc, andResume, checkConfig);
                return;
            } catch (RemoteException e) {
                Slog.w(TAG, "Exception when starting activity " + r.intent.getComponent().flattenToShortString(), e);
                knownToBeDead = true;
            }
        }
        acquireAppLaunch();
        if (getKeyguardController().isKeyguardLocked()) {
            r.notifyUnknownVisibilityLaunched();
        }
        try {
            if (Trace.isTagEnabled(64)) {
                Trace.traceBegin(64, "dispatchingStartProcess:" + r.processName);
            }
            this.mService.mH.sendMessage(PooledLambda.obtainMessage($$Lambda$3W4Y_XVQUddVKzLjibuHW7h0R1g.INSTANCE, this.mService.mAmInternal, r.processName, r.info.applicationInfo, Boolean.valueOf(knownToBeDead), IColorAppStartupManager.TYPE_ACTIVITY, r.intent.getComponent()));
            if (andResume) {
                OppoFeatureCache.get(IColorAppStartupManager.DEFAULT).handleResumeActivity(r.mColorArEx);
            }
            OppoFeatureCache.get(IColorAppCrashClearManager.DEFAULT).collectCrashInfo(r.mColorArEx);
        } finally {
            Trace.traceEnd(64);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean checkStartAnyActivityPermission(Intent intent, ActivityInfo aInfo, String resultWho, int requestCode, int callingPid, int callingUid, String callingPackage, boolean ignoreTargetSecurity, boolean launchingInTask, WindowProcessController callerApp, ActivityRecord resultRecord, ActivityStack resultStack) {
        String msg;
        boolean isCallerRecents = this.mService.getRecentTasks() != null && this.mService.getRecentTasks().isCallerRecents(callingUid);
        ActivityTaskManagerService activityTaskManagerService = this.mService;
        if (ActivityTaskManagerService.checkPermission("android.permission.START_ANY_ACTIVITY", callingPid, callingUid) == 0) {
            return true;
        }
        if (isCallerRecents && launchingInTask) {
            return true;
        }
        int componentRestriction = getComponentRestrictionForCallingPackage(aInfo, callingPackage, callingPid, callingUid, ignoreTargetSecurity);
        int actionRestriction = getActionRestrictionForCallingPackage(intent.getAction(), callingPackage, callingPid, callingUid);
        if (componentRestriction == 1 || actionRestriction == 1) {
            if (resultRecord != null) {
                resultStack.sendActivityResultLocked(-1, resultRecord, resultWho, requestCode, 0, null);
            }
            if (actionRestriction == 1) {
                msg = "Permission Denial: starting " + intent.toString() + " from " + callerApp + " (pid=" + callingPid + ", uid=" + callingUid + ") with revoked permission " + ACTION_TO_RUNTIME_PERMISSION.get(intent.getAction());
            } else if (!aInfo.exported) {
                msg = "Permission Denial: starting " + intent.toString() + " from " + callerApp + " (pid=" + callingPid + ", uid=" + callingUid + ") not exported from uid " + aInfo.applicationInfo.uid;
            } else {
                msg = "Permission Denial: starting " + intent.toString() + " from " + callerApp + " (pid=" + callingPid + ", uid=" + callingUid + ") requires " + aInfo.permission;
            }
            Slog.w(TAG, msg);
            throw new SecurityException(msg);
        } else if (actionRestriction == 2) {
            Slog.w(TAG, "Appop Denial: starting " + intent.toString() + " from " + callerApp + " (pid=" + callingPid + ", uid=" + callingUid + ") requires " + AppOpsManager.permissionToOp(ACTION_TO_RUNTIME_PERMISSION.get(intent.getAction())));
            return false;
        } else if (componentRestriction != 2) {
            return true;
        } else {
            Slog.w(TAG, "Appop Denial: starting " + intent.toString() + " from " + callerApp + " (pid=" + callingPid + ", uid=" + callingUid + ") requires appop " + AppOpsManager.permissionToOp(aInfo.permission));
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isCallerAllowedToLaunchOnDisplay(int callingPid, int callingUid, int launchDisplayId, ActivityInfo aInfo) {
        if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
            Slog.d(TAG, "Launch on display check: displayId=" + launchDisplayId + " callingPid=" + callingPid + " callingUid=" + callingUid);
        }
        if (callingPid == -1 && callingUid == -1) {
            if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                Slog.d(TAG, "Launch on display check: no caller info, skip check");
            }
            return true;
        }
        ActivityDisplay activityDisplay = this.mRootActivityContainer.getActivityDisplayOrCreate(launchDisplayId);
        if (activityDisplay == null || activityDisplay.isRemoved()) {
            Slog.w(TAG, "Launch on display check: display not found");
            return false;
        }
        ActivityTaskManagerService activityTaskManagerService = this.mService;
        if (ActivityTaskManagerService.checkPermission("android.permission.INTERNAL_SYSTEM_WINDOW", callingPid, callingUid) == 0) {
            if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                Slog.d(TAG, "Launch on display check: allow launch any on display");
            }
            return true;
        }
        boolean uidPresentOnDisplay = activityDisplay.isUidPresent(callingUid);
        int displayOwnerUid = activityDisplay.mDisplay.getOwnerUid();
        if (!(activityDisplay.mDisplay.getType() != 5 || displayOwnerUid == 1000 || displayOwnerUid == aInfo.applicationInfo.uid)) {
            if ((aInfo.flags & Integer.MIN_VALUE) == 0) {
                if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                    Slog.d(TAG, "Launch on display check: disallow launch on virtual display for not-embedded activity.");
                }
                return false;
            }
            ActivityTaskManagerService activityTaskManagerService2 = this.mService;
            if (ActivityTaskManagerService.checkPermission("android.permission.ACTIVITY_EMBEDDING", callingPid, callingUid) == -1 && !uidPresentOnDisplay) {
                if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                    Slog.d(TAG, "Launch on display check: disallow activity embedding without permission.");
                }
                return false;
            }
        }
        if (!activityDisplay.isPrivate()) {
            if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                Slog.d(TAG, "Launch on display check: allow launch on public display");
            }
            return true;
        } else if (displayOwnerUid == callingUid) {
            if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                Slog.d(TAG, "Launch on display check: allow launch for owner of the display");
            }
            return true;
        } else if (uidPresentOnDisplay) {
            if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                Slog.d(TAG, "Launch on display check: allow launch for caller present on the display");
            }
            return true;
        } else {
            Slog.w(TAG, "Launch on display check: denied");
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public UserInfo getUserInfo(int userId) {
        long identity = Binder.clearCallingIdentity();
        try {
            return UserManager.get(this.mService.mContext).getUserInfo(userId);
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    private int getComponentRestrictionForCallingPackage(ActivityInfo activityInfo, String callingPackage, int callingPid, int callingUid, boolean ignoreTargetSecurity) {
        int opCode;
        if (!ignoreTargetSecurity) {
            ActivityTaskManagerService activityTaskManagerService = this.mService;
            if (ActivityTaskManagerService.checkComponentPermission(activityInfo.permission, callingPid, callingUid, activityInfo.applicationInfo.uid, activityInfo.exported) == -1) {
                return 1;
            }
        }
        if (activityInfo.permission == null || (opCode = AppOpsManager.permissionToOpCode(activityInfo.permission)) == -1 || this.mService.getAppOpsService().noteOperation(opCode, callingUid, callingPackage) == 0 || ignoreTargetSecurity) {
            return 0;
        }
        return 2;
    }

    private int getActionRestrictionForCallingPackage(String action, String callingPackage, int callingPid, int callingUid) {
        String permission;
        if (action == null || (permission = ACTION_TO_RUNTIME_PERMISSION.get(action)) == null) {
            return 0;
        }
        try {
            if (!ArrayUtils.contains(this.mService.mContext.getPackageManager().getPackageInfo(callingPackage, 4096).requestedPermissions, permission)) {
                return 0;
            }
            ActivityTaskManagerService activityTaskManagerService = this.mService;
            if (ActivityTaskManagerService.checkPermission(permission, callingPid, callingUid) == -1) {
                return 1;
            }
            int opCode = AppOpsManager.permissionToOpCode(permission);
            if (opCode == -1 || this.mService.getAppOpsService().noteOperation(opCode, callingUid, callingPackage) == 0) {
                return 0;
            }
            return 2;
        } catch (PackageManager.NameNotFoundException e) {
            Slog.i(TAG, "Cannot find package info for " + callingPackage);
            return 0;
        }
    }

    /* access modifiers changed from: package-private */
    public void setLaunchSource(int uid) {
        this.mLaunchingActivityWakeLock.setWorkSource(new WorkSource(uid));
    }

    /* access modifiers changed from: package-private */
    public void acquireLaunchWakelock() {
        this.mLaunchingActivityWakeLock.acquire();
        if (!this.mHandler.hasMessages(LAUNCH_TIMEOUT_MSG)) {
            this.mHandler.sendEmptyMessageDelayed(LAUNCH_TIMEOUT_MSG, 10000);
        }
    }

    @GuardedBy({"mService"})
    private boolean checkFinishBootingLocked() {
        boolean booting = this.mService.isBooting();
        boolean enableScreen = false;
        this.mService.setBooting(false);
        if (!this.mService.isBooted()) {
            this.mService.setBooted(true);
            enableScreen = true;
        }
        if (booting || enableScreen) {
            this.mService.postFinishBooting(booting, enableScreen);
        }
        return booting;
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r11v5, resolved type: com.android.server.wm.ActivityStack} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v5, resolved type: com.android.server.wm.RootActivityContainer} */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v1, types: [int, boolean] */
    /* JADX WARN: Type inference failed for: r0v11 */
    /* JADX WARN: Type inference failed for: r0v16 */
    /* access modifiers changed from: package-private */
    @GuardedBy({"mService"})
    public final ActivityRecord activityIdleInternalLocked(IBinder token, boolean fromTimeout, boolean processPausingActivities, Configuration config) {
        boolean z;
        ? r0;
        if (ActivityTaskManagerDebugConfig.DEBUG_ALL) {
            Slog.v(TAG, "Activity idle: " + token);
        }
        ArrayList<ActivityRecord> finishes = null;
        ArrayList<UserState> startingUsers = null;
        boolean booting = false;
        booting = false;
        boolean activityRemoved = false;
        ActivityRecord r = ActivityRecord.forTokenLocked(token);
        if (r != null) {
            if (ActivityTaskManagerDebugConfig.DEBUG_IDLE) {
                Slog.d(TAG_IDLE, "activityIdleInternalLocked: Callers=" + Debug.getCallers(4));
            }
            this.mHandler.removeMessages(200, r);
            r.finishLaunchTickingLocked();
            if (fromTimeout) {
                z = true;
                reportActivityLaunchedLocked(fromTimeout, r, -1, -1);
            } else {
                z = true;
            }
            if (config != null) {
                r.setLastReportedGlobalConfiguration(config);
            }
            r.idle = z;
            if ((this.mService.isBooting() && this.mRootActivityContainer.allResumedActivitiesIdle()) || fromTimeout) {
                booting = checkFinishBootingLocked();
            }
            r0 = 0;
            r.mRelaunchReason = 0;
        } else {
            z = true;
            r0 = 0;
        }
        if (this.mRootActivityContainer.allResumedActivitiesIdle()) {
            if (r != null) {
                this.mService.scheduleAppGcsLocked();
            }
            if (this.mLaunchingActivityWakeLock.isHeld()) {
                this.mHandler.removeMessages(LAUNCH_TIMEOUT_MSG);
                this.mLaunchingActivityWakeLock.release();
            }
            this.mRootActivityContainer.ensureActivitiesVisible(null, r0, r0);
        }
        ArrayList<ActivityRecord> stops = processStoppingActivitiesLocked(r, z, processPausingActivities);
        int NS = stops != null ? stops.size() : r0;
        int NF = this.mFinishingActivities.size();
        if (NF > 0) {
            finishes = new ArrayList<>(this.mFinishingActivities);
            this.mFinishingActivities.clear();
        }
        if (this.mStartingUsers.size() > 0) {
            startingUsers = new ArrayList<>(this.mStartingUsers);
            this.mStartingUsers.clear();
        }
        for (int i = 0; i < NS; i++) {
            r = stops.get(i);
            ActivityStack stack = r.getActivityStack();
            if (stack != 0) {
                if (r.finishing) {
                    stack.finishCurrentActivityLocked(r, r0, r0, "activityIdleInternalLocked");
                } else {
                    stack.stopActivityLocked(r);
                }
            }
        }
        for (int i2 = 0; i2 < NF; i2++) {
            r = finishes.get(i2);
            ActivityStack stack2 = r.getActivityStack();
            if (stack2 != null) {
                activityRemoved = stack2.destroyActivityLocked(r, z, "finish-idle") | activityRemoved;
            }
        }
        if (!booting && startingUsers != null) {
            for (int i3 = 0; i3 < startingUsers.size(); i3++) {
                this.mService.mAmInternal.finishUserSwitch(startingUsers.get(i3));
            }
        }
        this.mService.mH.post(new Runnable() {
            /* class com.android.server.wm.$$Lambda$ActivityStackSupervisor$28Zuzbi6usdgbDcOi8hrJg6nZO0 */

            public final void run() {
                ActivityStackSupervisor.this.lambda$activityIdleInternalLocked$0$ActivityStackSupervisor();
            }
        });
        if (activityRemoved) {
            this.mRootActivityContainer.resumeFocusedStacksTopActivities();
        }
        return r;
    }

    public /* synthetic */ void lambda$activityIdleInternalLocked$0$ActivityStackSupervisor() {
        this.mService.mAmInternal.trimApplications();
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: com.android.server.wm.TaskRecord.reparent(com.android.server.wm.ActivityStack, boolean, int, boolean, boolean, java.lang.String):boolean
     arg types: [com.android.server.wm.ActivityStack, int, int, int, int, java.lang.String]
     candidates:
      com.android.server.wm.TaskRecord.reparent(com.android.server.wm.ActivityStack, int, int, boolean, boolean, java.lang.String):boolean
      com.android.server.wm.TaskRecord.reparent(com.android.server.wm.ActivityStack, boolean, int, boolean, boolean, java.lang.String):boolean */
    /* access modifiers changed from: package-private */
    public void findTaskToMoveToFront(TaskRecord task, int flags, ActivityOptions options, String reason, boolean forceNonResizeable) {
        AppTimeTracker appTimeTracker;
        Rect bounds;
        ActivityStack currentStack = task.getStack();
        if (currentStack == null) {
            Slog.e(TAG, "findTaskToMoveToFront: can't move task=" + task + " to front. Stack is null");
            return;
        }
        ActivityRecord currentStackTopActivity = currentStack.topRunningActivityLocked();
        if (currentStackTopActivity != null && currentStackTopActivity.isState(ActivityStack.ActivityState.DESTROYED)) {
            acquireAppLaunch();
        }
        if ((flags & 2) == 0) {
            this.mUserLeaving = true;
        }
        String reason2 = reason + " findTaskToMoveToFront";
        boolean reparented = false;
        if (!task.isResizeable() || !canUseActivityOptionsLaunchBounds(options)) {
            appTimeTracker = null;
        } else {
            Rect bounds2 = options.getLaunchBounds();
            task.updateOverrideConfiguration(bounds2);
            ActivityStack stack = this.mRootActivityContainer.getLaunchStack(null, options, task, true);
            if (stack != currentStack) {
                moveHomeStackToFrontIfNeeded(flags, stack.getDisplay(), reason2);
                bounds = bounds2;
                appTimeTracker = null;
                task.reparent(stack, true, 1, false, true, reason2);
                currentStack = stack;
                reparented = true;
            } else {
                bounds = bounds2;
                appTimeTracker = null;
            }
            if (stack.resizeStackWithLaunchBounds()) {
                this.mRootActivityContainer.resizeStack(stack, bounds, null, null, false, true, false);
            } else {
                task.resizeWindowContainer();
            }
        }
        if (!reparented) {
            moveHomeStackToFrontIfNeeded(flags, currentStack.getDisplay(), reason2);
        }
        ActivityRecord r = task.getTopActivity();
        if (r != null) {
            appTimeTracker = r.appTimeTracker;
        }
        currentStack.moveTaskToFrontLocked(task, false, options, appTimeTracker, reason2);
        if (ActivityTaskManagerDebugConfig.DEBUG_STACK) {
            Slog.d(TAG_STACK, "findTaskToMoveToFront: moved to front of stack=" + currentStack);
        }
        handleNonResizableTaskIfNeeded(task, 0, 0, currentStack, forceNonResizeable);
    }

    /* access modifiers changed from: package-private */
    public void acquireAppLaunch() {
        if (mHyp == null) {
            mHyp = Hypnus.getHypnus();
        }
        Hypnus hypnus = mHyp;
        if (hypnus != null) {
            hypnus.hypnusSetAction(13, 2000);
        }
    }

    /* access modifiers changed from: package-private */
    public void startActivityBoost(String className) {
        if (className != null) {
            if (className.equals("com.tencent.mm.plugin.voip.ui.VideoActivity") || className.equals("com.tencent.av.ui.AVLoadingDialogActivity") || className.equals("com.tencent.mm.plugin.exdevice.ui.ExdeviceRankInfoUI") || className.equals("com.tencent.mm.plugin.webview.ui.tools.WebViewUI") || className.equals("com.jingdong.app.mall.pay.CashierDeskActivity") || className.equals("com.tmall.wireless.pay.TMPayActivity") || className.equals("com.alipay.android.app.pay.MiniLaucherActivity") || className.equals("com.alipay.android.app.ui.quickpay.window.MiniPayActivity") || className.equals("com.taobao.ju.android.ui.main.TabMainActivity") || className.equals("com.taobao.tao.alipay.cashdesk.CashDeskActivity")) {
                if (mHyp == null) {
                    mHyp = Hypnus.getHypnus();
                }
                Hypnus hypnus = mHyp;
                if (hypnus != null) {
                    hypnus.hypnusSetAction(15, 3000);
                }
            }
        }
    }

    private void moveHomeStackToFrontIfNeeded(int flags, ActivityDisplay display, String reason) {
        ActivityStack focusedStack = display.getFocusedStack();
        if ((display.getWindowingMode() == 1 && (flags & 1) != 0) || (focusedStack != null && focusedStack.isActivityTypeRecents())) {
            display.moveHomeStackToFront(reason);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean canUseActivityOptionsLaunchBounds(ActivityOptions options) {
        if (options == null || options.getLaunchBounds() == null) {
            return false;
        }
        if ((!this.mService.mSupportsPictureInPicture || options.getLaunchWindowingMode() != 2) && !this.mService.mSupportsFreeformWindowManagement) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public LaunchParamsController getLaunchParamsController() {
        return this.mLaunchParamsController;
    }

    private void deferUpdateRecentsHomeStackBounds() {
        this.mRootActivityContainer.deferUpdateBounds(3);
        this.mRootActivityContainer.deferUpdateBounds(2);
    }

    private void continueUpdateRecentsHomeStackBounds() {
        this.mRootActivityContainer.continueUpdateBounds(3);
        this.mRootActivityContainer.continueUpdateBounds(2);
    }

    /* access modifiers changed from: package-private */
    public void notifyAppTransitionDone() {
        if (this.mResizingTasksDuringAnimation.size() > 0 && !this.mService.mWindowManager.getDefaultDisplayContentLocked().mAppTransition.isTransitionSet()) {
            this.mService.mWindowManager.startFreezingScreen(0, 0);
        }
        continueUpdateRecentsHomeStackBounds();
        for (int i = this.mResizingTasksDuringAnimation.size() - 1; i >= 0; i--) {
            TaskRecord task = this.mRootActivityContainer.anyTaskForId(this.mResizingTasksDuringAnimation.valueAt(i).intValue(), 0);
            if (task != null) {
                task.setTaskDockedResizing(false);
            }
        }
        if (this.mResizingTasksDuringAnimation.size() > 0 && this.mService.mInternal.startHomeActivity(this.mService.getCurrentUserId(), "startActivityFromRecents: startHomeActivity")) {
            this.mService.mWindowManager.stopFreezingScreen();
        }
        this.mResizingTasksDuringAnimation.clear();
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: com.android.server.wm.TaskRecord.reparent(com.android.server.wm.ActivityStack, boolean, int, boolean, boolean, boolean, java.lang.String):boolean
     arg types: [com.android.server.wm.ActivityStack, int, int, boolean, int, boolean, java.lang.String]
     candidates:
      com.android.server.wm.TaskRecord.reparent(com.android.server.wm.ActivityStack, int, int, boolean, boolean, boolean, java.lang.String):boolean
      com.android.server.wm.TaskRecord.reparent(com.android.server.wm.ActivityStack, boolean, int, boolean, boolean, boolean, java.lang.String):boolean */
    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: com.android.server.wm.TaskRecord.reparent(com.android.server.wm.ActivityStack, boolean, int, boolean, boolean, boolean, java.lang.String):boolean
     arg types: [com.android.server.wm.ActivityStack, int, int, int, int, boolean, java.lang.String]
     candidates:
      com.android.server.wm.TaskRecord.reparent(com.android.server.wm.ActivityStack, int, int, boolean, boolean, boolean, java.lang.String):boolean
      com.android.server.wm.TaskRecord.reparent(com.android.server.wm.ActivityStack, boolean, int, boolean, boolean, boolean, java.lang.String):boolean */
    /* access modifiers changed from: private */
    /* renamed from: moveTasksToFullscreenStackInSurfaceTransaction */
    public void lambda$moveTasksToFullscreenStackLocked$1$ActivityStackSupervisor(ActivityStack fromStack, int toDisplayId, boolean onTop) {
        ActivityDisplay toDisplay;
        ArrayList<TaskRecord> tasks;
        int size;
        int i;
        this.mWindowManager.deferSurfaceLayout();
        try {
            int windowingMode = fromStack.getWindowingMode();
            boolean inPinnedWindowingMode = windowingMode == 2;
            try {
                ActivityDisplay toDisplay2 = this.mRootActivityContainer.getActivityDisplay(toDisplayId);
                if (windowingMode == 3) {
                    toDisplay2.onExitingSplitScreenMode();
                    for (int i2 = toDisplay2.getChildCount() - 1; i2 >= 0; i2--) {
                        ActivityStack otherStack = toDisplay2.getChildAt(i2);
                        if (otherStack.inSplitScreenSecondaryWindowingMode()) {
                            otherStack.setWindowingMode(0);
                        }
                    }
                    this.mAllowDockedStackResize = false;
                }
                ArrayList<TaskRecord> tasks2 = fromStack.getAllTasks();
                if (!tasks2.isEmpty()) {
                    this.mTmpOptions.setLaunchWindowingMode(1);
                    int size2 = tasks2.size();
                    int i3 = 0;
                    while (i3 < size2) {
                        TaskRecord task = tasks2.get(i3);
                        ActivityStack toStack = toDisplay2.getOrCreateStack(null, this.mTmpOptions, task, task.getActivityType(), onTop);
                        if (onTop) {
                            i = i3;
                            size = size2;
                            tasks = tasks2;
                            toDisplay = toDisplay2;
                            task.reparent(toStack, true, 0, i3 == size2 + -1, true, inPinnedWindowingMode, "moveTasksToFullscreenStack - onTop");
                            MetricsLoggerWrapper.logPictureInPictureFullScreen(this.mService.mContext, task.effectiveUid, task.realActivity.flattenToString());
                        } else {
                            i = i3;
                            size = size2;
                            tasks = tasks2;
                            toDisplay = toDisplay2;
                            task.reparent(toStack, true, 2, false, true, inPinnedWindowingMode, "moveTasksToFullscreenStack - NOT_onTop");
                        }
                        i3 = i + 1;
                        size2 = size;
                        tasks2 = tasks;
                        toDisplay2 = toDisplay;
                    }
                }
                this.mRootActivityContainer.ensureActivitiesVisible(null, 0, true);
                this.mRootActivityContainer.resumeFocusedStacksTopActivities();
                this.mAllowDockedStackResize = true;
                this.mWindowManager.continueSurfaceLayout();
            } catch (Throwable th) {
                th = th;
                this.mAllowDockedStackResize = true;
                this.mWindowManager.continueSurfaceLayout();
                throw th;
            }
        } catch (Throwable th2) {
            th = th2;
            this.mAllowDockedStackResize = true;
            this.mWindowManager.continueSurfaceLayout();
            throw th;
        }
    }

    /* access modifiers changed from: package-private */
    public void moveTasksToFullscreenStackLocked(ActivityStack fromStack, boolean onTop) {
        moveTasksToFullscreenStackLocked(fromStack, 0, onTop);
    }

    /* access modifiers changed from: package-private */
    public void moveTasksToFullscreenStackLocked(ActivityStack fromStack, int toDisplayId, boolean onTop) {
        this.mWindowManager.inSurfaceTransaction(new Runnable(fromStack, toDisplayId, onTop) {
            /* class com.android.server.wm.$$Lambda$ActivityStackSupervisor$PHIj4FpzoLIwUTmMRMOYA9us0rc */
            private final /* synthetic */ ActivityStack f$1;
            private final /* synthetic */ int f$2;
            private final /* synthetic */ boolean f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            public final void run() {
                ActivityStackSupervisor.this.lambda$moveTasksToFullscreenStackLocked$1$ActivityStackSupervisor(this.f$1, this.f$2, this.f$3);
            }
        });
    }

    /* access modifiers changed from: package-private */
    public void setSplitScreenResizing(boolean resizing) {
        if (resizing != this.mDockedStackResizing) {
            this.mDockedStackResizing = resizing;
            this.mWindowManager.setDockedStackResizing(resizing);
            if (!resizing && this.mHasPendingDockedBounds) {
                resizeDockedStackLocked(this.mPendingDockedBounds, this.mPendingTempDockedTaskBounds, this.mPendingTempDockedTaskInsetBounds, this.mPendingTempOtherTaskBounds, this.mPendingTempOtherTaskInsetBounds, true);
                this.mHasPendingDockedBounds = false;
                this.mPendingDockedBounds = null;
                this.mPendingTempDockedTaskBounds = null;
                this.mPendingTempDockedTaskInsetBounds = null;
                this.mPendingTempOtherTaskBounds = null;
                this.mPendingTempOtherTaskInsetBounds = null;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void resizeDockedStackLocked(Rect dockedBounds, Rect tempDockedTaskBounds, Rect tempDockedTaskInsetBounds, Rect tempOtherTaskBounds, Rect tempOtherTaskInsetBounds, boolean preserveWindows) {
        resizeDockedStackLocked(dockedBounds, tempDockedTaskBounds, tempDockedTaskInsetBounds, tempOtherTaskBounds, tempOtherTaskInsetBounds, preserveWindows, false);
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x00d6  */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x00de  */
    public void resizeDockedStackLocked(Rect dockedBounds, Rect tempDockedTaskBounds, Rect tempDockedTaskInsetBounds, Rect tempOtherTaskBounds, Rect tempOtherTaskInsetBounds, boolean preserveWindows, boolean deferResume) {
        if (this.mAllowDockedStackResize) {
            ActivityStack stack = this.mRootActivityContainer.getDefaultDisplay().getSplitScreenPrimaryStack();
            if (stack == null) {
                Slog.w(TAG, "resizeDockedStackLocked: docked stack not found");
                return;
            }
            if (this.mDockedStackResizing) {
                this.mHasPendingDockedBounds = true;
                this.mPendingDockedBounds = Rect.copyOrNull(dockedBounds);
                this.mPendingTempDockedTaskBounds = Rect.copyOrNull(tempDockedTaskBounds);
                this.mPendingTempDockedTaskInsetBounds = Rect.copyOrNull(tempDockedTaskInsetBounds);
                this.mPendingTempOtherTaskBounds = Rect.copyOrNull(tempOtherTaskBounds);
                this.mPendingTempOtherTaskInsetBounds = Rect.copyOrNull(tempOtherTaskInsetBounds);
            }
            Trace.traceBegin(64, "am.resizeDockedStack");
            this.mWindowManager.deferSurfaceLayout();
            try {
                this.mAllowDockedStackResize = false;
                ActivityRecord r = stack.topRunningActivityLocked();
                try {
                    stack.resize(dockedBounds, tempDockedTaskBounds, tempDockedTaskInsetBounds);
                    if (stack.getWindowingMode() != 1) {
                        if (dockedBounds != null || stack.isAttached()) {
                            ActivityDisplay display = this.mRootActivityContainer.getDefaultDisplay();
                            Rect otherTaskRect = new Rect();
                            for (int i = display.getChildCount() - 1; i >= 0; i--) {
                                ActivityStack current = display.getChildAt(i);
                                if (current.inSplitScreenSecondaryWindowingMode()) {
                                    if (current.affectedBySplitScreenResize()) {
                                        if (!this.mDockedStackResizing || current.isTopActivityVisible()) {
                                            current.getStackDockedModeBounds(dockedBounds, tempOtherTaskBounds, this.tempRect, otherTaskRect);
                                            this.mRootActivityContainer.resizeStack(current, !this.tempRect.isEmpty() ? this.tempRect : null, !otherTaskRect.isEmpty() ? otherTaskRect : tempOtherTaskBounds, tempOtherTaskInsetBounds, preserveWindows, true, deferResume);
                                        }
                                    }
                                }
                            }
                            if (deferResume) {
                                try {
                                    stack.ensureVisibleActivitiesConfigurationLocked(r, preserveWindows);
                                } catch (Throwable th) {
                                    th = th;
                                }
                            }
                            this.mAllowDockedStackResize = true;
                            this.mWindowManager.continueSurfaceLayout();
                            Trace.traceEnd(64);
                        }
                    }
                    moveTasksToFullscreenStackLocked(stack, true);
                    r = null;
                    if (deferResume) {
                    }
                    this.mAllowDockedStackResize = true;
                    this.mWindowManager.continueSurfaceLayout();
                    Trace.traceEnd(64);
                } catch (Throwable th2) {
                    th = th2;
                    this.mAllowDockedStackResize = true;
                    this.mWindowManager.continueSurfaceLayout();
                    Trace.traceEnd(64);
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                this.mAllowDockedStackResize = true;
                this.mWindowManager.continueSurfaceLayout();
                Trace.traceEnd(64);
                throw th;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void resizePinnedStackLocked(Rect pinnedBounds, Rect tempPinnedTaskBounds) {
        ActivityStack stack = this.mRootActivityContainer.getDefaultDisplay().getPinnedStack();
        if (stack == null) {
            Slog.w(TAG, "resizePinnedStackLocked: pinned stack not found");
        } else if (!stack.getTaskStack().pinnedStackResizeDisallowed()) {
            Trace.traceBegin(64, "am.resizePinnedStack");
            this.mWindowManager.deferSurfaceLayout();
            try {
                ActivityRecord r = stack.topRunningActivityLocked();
                Rect insetBounds = null;
                if (tempPinnedTaskBounds != null && stack.isAnimatingBoundsToFullscreen()) {
                    insetBounds = this.tempRect;
                    insetBounds.top = 0;
                    insetBounds.left = 0;
                    insetBounds.right = tempPinnedTaskBounds.width();
                    insetBounds.bottom = tempPinnedTaskBounds.height();
                }
                if (pinnedBounds != null && tempPinnedTaskBounds == null) {
                    stack.onPipAnimationEndResize();
                }
                stack.resize(pinnedBounds, tempPinnedTaskBounds, insetBounds);
                stack.ensureVisibleActivitiesConfigurationLocked(r, false);
            } finally {
                this.mWindowManager.continueSurfaceLayout();
                Trace.traceEnd(64);
            }
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: removeStackInSurfaceTransaction */
    public void lambda$removeStack$2$ActivityStackSupervisor(ActivityStack stack) {
        ArrayList<TaskRecord> tasks = stack.getAllTasks();
        if (stack.getWindowingMode() == 2) {
            stack.mForceHidden = true;
            stack.ensureActivitiesVisibleLocked(null, 0, true);
            stack.mForceHidden = false;
            activityIdleInternalLocked(null, false, true, null);
            moveTasksToFullscreenStackLocked(stack, false);
            return;
        }
        for (int i = tasks.size() - 1; i >= 0; i--) {
            removeTaskByIdLocked(tasks.get(i).taskId, true, true, "remove-stack");
        }
    }

    /* access modifiers changed from: package-private */
    public void removeStack(ActivityStack stack) {
        this.mWindowManager.inSurfaceTransaction(new Runnable(stack) {
            /* class com.android.server.wm.$$Lambda$ActivityStackSupervisor$0u1RcpeZ6m0BHDGGv8EXroS3KyE */
            private final /* synthetic */ ActivityStack f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                ActivityStackSupervisor.this.lambda$removeStack$2$ActivityStackSupervisor(this.f$1);
            }
        });
    }

    /* access modifiers changed from: package-private */
    public boolean removeTaskByIdLocked(int taskId, boolean killProcess, boolean removeFromRecents, String reason) {
        return removeTaskByIdLocked(taskId, killProcess, removeFromRecents, false, reason);
    }

    /* access modifiers changed from: package-private */
    public boolean removeTaskByIdLocked(int taskId, boolean killProcess, boolean removeFromRecents, boolean pauseImmediately, String reason) {
        TaskRecord tr = this.mRootActivityContainer.anyTaskForId(taskId, 1);
        if (tr != null) {
            tr.removeTaskActivitiesLocked(pauseImmediately, reason);
            if (ActivityTaskManagerDebugConfig.DEBUG_TASKS && killProcess) {
                int callerPid = Binder.getCallingPid();
                int callerUid = Binder.getCallingUid();
                Slog.d(TAG, "kill proc from removeTask, callerPid:" + callerPid + ", callerUid:" + callerUid + Debug.getCallers(5));
            }
            cleanUpRemovedTaskLocked(tr, killProcess, removeFromRecents);
            this.mService.getLockTaskController().clearLockedTask(tr);
            if (tr.isPersistable) {
                this.mService.notifyTaskPersisterLocked(null, true);
            }
            return true;
        }
        Slog.w(TAG, "Request to remove task ignored for non-existent task " + taskId);
        return false;
    }

    /* access modifiers changed from: package-private */
    public void cleanUpRemovedTaskLocked(TaskRecord tr, boolean killProcess, boolean removeFromRecents) {
        int filterType;
        if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
            Slog.i(TAG, "cleanUpRemovedTaskLocked " + tr + " killProcess" + killProcess);
        }
        if (removeFromRecents) {
            this.mRecentTasks.remove(tr);
        }
        ComponentName component = tr.getBaseIntent().getComponent();
        if (component == null) {
            Slog.w(TAG, "No component for base intent of task: " + tr);
            return;
        }
        this.mService.mH.sendMessage(PooledLambda.obtainMessage($$Lambda$z5j5fiv3cZuY5AODkt3H3rhKimk.INSTANCE, this.mService.mAmInternal, Integer.valueOf(tr.userId), component, new Intent(tr.getBaseIntent())));
        if (killProcess) {
            String pkg = component.getPackageName();
            ArrayList<Object> procsToKill = new ArrayList<>();
            ArrayMap<String, SparseArray<WindowProcessController>> pmap = this.mService.mProcessNames.getMap();
            for (int i = 0; i < pmap.size(); i++) {
                SparseArray<WindowProcessController> uids = pmap.valueAt(i);
                for (int j = 0; j < uids.size(); j++) {
                    WindowProcessController proc = uids.valueAt(j);
                    if (proc.mUserId == tr.userId && proc != this.mService.mHomeProcess && proc.mPkgList.contains(pkg)) {
                        if (!proc.shouldKillProcessForRemovedTask(tr) || (filterType = OppoFeatureCache.get(IColorAthenaManager.DEFAULT).getRemoveTaskFilterType(proc)) == 1) {
                            return;
                        }
                        if (filterType == 2) {
                            continue;
                        } else if (filterType == 3) {
                            procsToKill.add(proc);
                        } else if (!proc.hasForegroundServices()) {
                            procsToKill.add(proc);
                        } else {
                            return;
                        }
                    }
                }
            }
            this.mService.mH.sendMessage(PooledLambda.obtainMessage($$Lambda$j9nJq2XXOKyN4f0dfDaTjqmQRvg.INSTANCE, this.mService.mAmInternal, procsToKill));
        }
    }

    /* access modifiers changed from: package-private */
    public boolean restoreRecentTaskLocked(TaskRecord task, ActivityOptions aOptions, boolean onTop) {
        ActivityStack stack = this.mRootActivityContainer.getLaunchStack(null, aOptions, task, onTop);
        ActivityStack currentStack = task.getStack();
        if (currentStack != null) {
            if (currentStack == stack) {
                return true;
            }
            currentStack.removeTask(task, "restoreRecentTaskLocked", 1);
        }
        stack.addTask(task, onTop, "restoreRecentTask");
        task.createTask(onTop, true);
        if (ActivityTaskManagerDebugConfig.DEBUG_RECENTS) {
            Slog.v(TAG_RECENTS, "Added restored task=" + task + " to stack=" + stack);
        }
        ArrayList<ActivityRecord> activities = task.mActivities;
        for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
            activities.get(activityNdx).createAppWindowToken();
        }
        return true;
    }

    @Override // com.android.server.wm.RecentTasks.Callbacks
    public void onRecentTaskAdded(TaskRecord task) {
        task.touchActiveTime();
    }

    @Override // com.android.server.wm.RecentTasks.Callbacks
    public void onRecentTaskRemoved(TaskRecord task, boolean wasTrimmed, boolean killProcess) {
        if (wasTrimmed) {
            removeTaskByIdLocked(task.taskId, killProcess, false, false, "recent-task-trimmed");
        }
        task.removedFromRecents();
    }

    /* access modifiers changed from: package-private */
    public ActivityStack getReparentTargetStack(TaskRecord task, ActivityStack stack, boolean toTop) {
        ActivityStack prevStack = task.getStack();
        int stackId = stack.mStackId;
        boolean inMultiWindowMode = stack.inMultiWindowMode();
        if (prevStack != null && prevStack.mStackId == stackId) {
            Slog.w(TAG, "Can not reparent to same stack, task=" + task + " already in stackId=" + stackId);
            return prevStack;
        } else if (inMultiWindowMode && !this.mService.mSupportsMultiWindow) {
            throw new IllegalArgumentException("Device doesn't support multi-window, can not reparent task=" + task + " to stack=" + stack);
        } else if (stack.mDisplayId != 0 && !this.mService.mSupportsMultiDisplay) {
            throw new IllegalArgumentException("Device doesn't support multi-display, can not reparent task=" + task + " to stackId=" + stackId);
        } else if (stack.getWindowingMode() == 5 && !this.mService.mSupportsFreeformWindowManagement) {
            throw new IllegalArgumentException("Device doesn't support freeform, can not reparent task=" + task);
        } else if (!inMultiWindowMode || task.isResizeable()) {
            return stack;
        } else {
            Slog.w(TAG, "Can not move unresizeable task=" + task + " to multi-window stack=" + stack + " Moving to a fullscreen stack instead.");
            if (prevStack != null) {
                return prevStack;
            }
            return stack.getDisplay().createStack(1, stack.getActivityType(), toTop);
        }
    }

    /* access modifiers changed from: package-private */
    public void goingToSleepLocked() {
        OppoFeatureCache.get(IColorAccessControlLocalManager.DEFAULT).removeAccessControlPassAsUser(null, 0, true);
        scheduleSleepTimeout();
        if (!this.mGoingToSleepWakeLock.isHeld()) {
            this.mGoingToSleepWakeLock.acquire();
            if (this.mLaunchingActivityWakeLock.isHeld()) {
                this.mLaunchingActivityWakeLock.release();
                this.mHandler.removeMessages(LAUNCH_TIMEOUT_MSG);
            }
        }
        this.mRootActivityContainer.applySleepTokens(false);
        synchronized (this.mService.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                checkReadyForSleepLocked(true);
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean shutdownLocked(int timeout) {
        goingToSleepLocked();
        boolean timedout = false;
        long endTime = System.currentTimeMillis() + ((long) timeout);
        while (true) {
            if (this.mRootActivityContainer.putStacksToSleep(true, true)) {
                break;
            }
            long timeRemaining = endTime - System.currentTimeMillis();
            if (timeRemaining <= 0) {
                Slog.w(TAG, "Activity manager shutdown timed out");
                timedout = true;
                break;
            }
            try {
                this.mService.mGlobalLock.wait(timeRemaining);
            } catch (InterruptedException e) {
            }
        }
        checkReadyForSleepLocked(false);
        return timedout;
    }

    /* access modifiers changed from: package-private */
    public void comeOutOfSleepIfNeededLocked() {
        removeSleepTimeouts();
        if (this.mGoingToSleepWakeLock.isHeld()) {
            this.mGoingToSleepWakeLock.release();
        }
    }

    /* access modifiers changed from: package-private */
    public void activitySleptLocked(ActivityRecord r) {
        this.mGoingToSleepActivities.remove(r);
        ActivityStack s = r.getActivityStack();
        if (s != null) {
            s.checkReadyForSleep();
        } else {
            checkReadyForSleepLocked(true);
        }
    }

    /* access modifiers changed from: package-private */
    public void checkReadyForSleepLocked(boolean allowDelay) {
        if (this.mService.isSleepingOrShuttingDownLocked() && this.mRootActivityContainer.putStacksToSleep(allowDelay, false)) {
            this.mRootActivityContainer.sendPowerHintForLaunchEndIfNeeded();
            removeSleepTimeouts();
            if (this.mGoingToSleepWakeLock.isHeld()) {
                this.mGoingToSleepWakeLock.release();
            }
            if (this.mService.mShuttingDown) {
                this.mService.mGlobalLock.notifyAll();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean reportResumedActivityLocked(ActivityRecord r) {
        this.mStoppingActivities.remove(r);
        if (!r.getActivityStack().getDisplay().allResumedActivitiesComplete()) {
            return false;
        }
        this.mRootActivityContainer.ensureActivitiesVisible(null, 0, false);
        this.mRootActivityContainer.executeAppTransitionForAllDisplay();
        return true;
    }

    /* access modifiers changed from: private */
    public void handleLaunchTaskBehindCompleteLocked(ActivityRecord r) {
        TaskRecord task = r.getTaskRecord();
        ActivityStack stack = task.getStack();
        r.mLaunchTaskBehind = false;
        this.mRecentTasks.add(task);
        this.mService.getTaskChangeNotificationController().notifyTaskStackChanged();
        r.setVisibility(false);
        ActivityRecord top = stack.getTopActivity();
        if (top != null) {
            top.getTaskRecord().touchActiveTime();
        }
    }

    /* access modifiers changed from: package-private */
    public void scheduleLaunchTaskBehindComplete(IBinder token) {
        this.mHandler.obtainMessage(212, token).sendToTarget();
    }

    /* access modifiers changed from: package-private */
    public boolean isCurrentProfileLocked(int userId) {
        if (userId != this.mRootActivityContainer.mCurrentUser && !OppoFeatureCache.get(IColorMultiAppManager.DEFAULT).isCurrentProfile(userId)) {
            return this.mService.mAmInternal.isCurrentProfile(userId);
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean isStoppingNoHistoryActivity() {
        Iterator<ActivityRecord> it = this.mStoppingActivities.iterator();
        while (it.hasNext()) {
            if (it.next().isNoHistory()) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public final ArrayList<ActivityRecord> processStoppingActivitiesLocked(ActivityRecord idleActivity, boolean remove, boolean processPausingActivities) {
        boolean shouldSleepOrShutDown;
        ArrayList<ActivityRecord> stops = null;
        boolean nowVisible = this.mRootActivityContainer.allResumedActivitiesVisible();
        for (int activityNdx = this.mStoppingActivities.size() - 1; activityNdx >= 0; activityNdx--) {
            ActivityRecord s = this.mStoppingActivities.get(activityNdx);
            boolean animating = s.mAppWindowToken.isSelfAnimating();
            if (ActivityTaskManagerDebugConfig.DEBUG_STATES) {
                Slog.v(TAG, "Stopping " + s + ": nowVisible=" + nowVisible + " animating=" + animating + " finishing=" + s.finishing);
            }
            if (nowVisible && s.finishing) {
                if (ActivityTaskManagerDebugConfig.DEBUG_STATES) {
                    Slog.v(TAG, "Before stopping, can hide: " + s);
                }
                s.setVisibility(false);
            }
            if (remove) {
                ActivityStack stack = s.getActivityStack();
                if (stack != null) {
                    shouldSleepOrShutDown = stack.shouldSleepOrShutDownActivities();
                } else {
                    shouldSleepOrShutDown = this.mService.isSleepingOrShuttingDownLocked();
                }
                if (!animating || shouldSleepOrShutDown) {
                    if (processPausingActivities || !s.isState(ActivityStack.ActivityState.PAUSING)) {
                        if (ActivityTaskManagerDebugConfig.DEBUG_STATES) {
                            Slog.v(TAG, "Ready to stop: " + s);
                        }
                        if (stops == null) {
                            stops = new ArrayList<>();
                        }
                        stops.add(s);
                        this.mStoppingActivities.remove(activityNdx);
                    } else {
                        removeTimeoutsForActivityLocked(idleActivity);
                        scheduleIdleTimeoutLocked(idleActivity);
                    }
                }
            }
        }
        return stops;
    }

    public void dump(PrintWriter pw, String prefix) {
        pw.println();
        pw.println("ActivityStackSupervisor state:");
        this.mRootActivityContainer.dump(pw, prefix);
        pw.print(prefix);
        pw.println("mCurTaskIdForUser=" + this.mCurTaskIdForUser);
        pw.println(prefix + "mUserStackInFront=" + this.mRootActivityContainer.mUserStackInFront);
        if (!this.mWaitingForActivityVisible.isEmpty()) {
            pw.println(prefix + "mWaitingForActivityVisible=");
            for (int i = 0; i < this.mWaitingForActivityVisible.size(); i++) {
                pw.print(prefix + prefix);
                this.mWaitingForActivityVisible.get(i).dump(pw, prefix);
            }
        }
        pw.print(prefix);
        pw.print("isHomeRecentsComponent=");
        pw.print(this.mRecentTasks.isRecentsComponentHomeActivity(this.mRootActivityContainer.mCurrentUser));
        getKeyguardController().dump(pw, prefix);
        this.mService.getLockTaskController().dump(pw, prefix);
    }

    static boolean printThisActivity(PrintWriter pw, ActivityRecord activity, String dumpPackage, boolean needSep, String prefix) {
        if (activity == null) {
            return false;
        }
        if (dumpPackage != null && !dumpPackage.equals(activity.packageName)) {
            return false;
        }
        if (needSep) {
            pw.println();
        }
        pw.print(prefix);
        pw.println(activity);
        return true;
    }

    /* JADX INFO: Multiple debug info for r4v9 'lastTask'  com.android.server.wm.TaskRecord: [D('lastTask' com.android.server.wm.TaskRecord), D('tp' com.android.internal.os.TransferPipe)] */
    static boolean dumpHistoryList(FileDescriptor fd, PrintWriter pw, List<ActivityRecord> list, String prefix, String label, boolean complete, boolean brief, boolean client, String dumpPackage, boolean needNL, String header, TaskRecord lastTask) {
        String header2;
        TaskRecord lastTask2;
        TaskRecord lastTask3;
        String str = prefix;
        String str2 = dumpPackage;
        boolean printed = false;
        boolean z = true;
        int i = list.size() - 1;
        boolean needNL2 = needNL;
        String innerPrefix = null;
        String[] args = null;
        String header3 = header;
        TaskRecord lastTask4 = lastTask;
        while (i >= 0) {
            ActivityRecord r = list.get(i);
            if (str2 == null || str2.equals(r.packageName)) {
                boolean full = false;
                full = false;
                if (innerPrefix == null) {
                    innerPrefix = str + "      ";
                    args = new String[0];
                }
                printed = true;
                printed = true;
                if (!brief && (complete || !r.isInHistory())) {
                    full = z;
                }
                if (needNL2) {
                    pw.println("");
                    needNL2 = false;
                }
                if (header3 != null) {
                    pw.println(header3);
                    header2 = null;
                } else {
                    header2 = header3;
                }
                if (lastTask4 != r.getTaskRecord()) {
                    lastTask4 = r.getTaskRecord();
                    pw.print(str);
                    pw.print(full ? "* " : "  ");
                    pw.println(lastTask4);
                    if (full) {
                        lastTask4.dump(pw, str + "  ");
                    } else if (complete && lastTask4.intent != null) {
                        pw.print(str);
                        pw.print("  ");
                        pw.println(lastTask4.intent.toInsecureStringWithClip());
                    }
                }
                pw.print(str);
                pw.print(full ? "  * " : "    ");
                pw.print(label);
                pw.print(" #");
                pw.print(i);
                pw.print(": ");
                pw.println(r);
                if (full) {
                    r.dump(pw, innerPrefix);
                } else if (complete) {
                    pw.print(innerPrefix);
                    pw.println(r.intent.toInsecureString());
                    if (r.app != null) {
                        pw.print(innerPrefix);
                        pw.println(r.app);
                    }
                }
                if (!client || !r.attachedToProcess()) {
                    lastTask4 = lastTask4;
                    header3 = header2;
                } else {
                    pw.flush();
                    try {
                        lastTask2 = new TransferPipe();
                        try {
                            r.app.getThread().dumpActivity(lastTask2.getWriteFd(), r.appToken, innerPrefix, args);
                            lastTask3 = lastTask2;
                            lastTask2 = lastTask4;
                            try {
                                lastTask3.go(fd, 2000);
                                lastTask4 = lastTask2;
                                needNL2 = true;
                                header3 = header2;
                            } catch (Throwable th) {
                                th = th;
                                lastTask3.kill();
                                throw th;
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            lastTask3 = lastTask2;
                            lastTask3.kill();
                            throw th;
                        }
                        try {
                            lastTask3.kill();
                        } catch (IOException e) {
                            e = e;
                            pw.println(innerPrefix + "Failure while dumping the activity: " + e);
                            lastTask4 = lastTask2;
                            needNL2 = true;
                            header3 = header2;
                            i--;
                            str = prefix;
                            str2 = dumpPackage;
                            z = true;
                        } catch (RemoteException e2) {
                            pw.println(innerPrefix + "Got a RemoteException while dumping the activity");
                            lastTask4 = lastTask2;
                            needNL2 = true;
                            header3 = header2;
                            i--;
                            str = prefix;
                            str2 = dumpPackage;
                            z = true;
                        }
                    } catch (IOException e3) {
                        e = e3;
                        lastTask2 = lastTask4;
                        pw.println(innerPrefix + "Failure while dumping the activity: " + e);
                        lastTask4 = lastTask2;
                        needNL2 = true;
                        header3 = header2;
                        i--;
                        str = prefix;
                        str2 = dumpPackage;
                        z = true;
                    } catch (RemoteException e4) {
                        lastTask2 = lastTask4;
                        pw.println(innerPrefix + "Got a RemoteException while dumping the activity");
                        lastTask4 = lastTask2;
                        needNL2 = true;
                        header3 = header2;
                        i--;
                        str = prefix;
                        str2 = dumpPackage;
                        z = true;
                    }
                }
            }
            i--;
            str = prefix;
            str2 = dumpPackage;
            z = true;
        }
        return printed;
    }

    /* access modifiers changed from: package-private */
    public void scheduleIdleTimeoutLocked(ActivityRecord next) {
        if (ActivityTaskManagerDebugConfig.DEBUG_IDLE) {
            String str = TAG_IDLE;
            Slog.d(str, "scheduleIdleTimeoutLocked: Callers=" + Debug.getCallers(4));
        }
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(200, next), 10000);
    }

    /* access modifiers changed from: package-private */
    public final void scheduleIdleLocked() {
        this.mHandler.sendEmptyMessage(IDLE_NOW_MSG);
    }

    /* access modifiers changed from: package-private */
    public void updateTopResumedActivityIfNeeded() {
        ActivityRecord prevTopActivity = this.mTopResumedActivity;
        ActivityStack topStack = this.mRootActivityContainer.getTopDisplayFocusedStack();
        if (topStack != null && topStack.mResumedActivity != prevTopActivity) {
            if ((prevTopActivity != null && !this.mTopResumedActivityWaitingForPrev) && prevTopActivity.scheduleTopResumedActivityChanged(false)) {
                scheduleTopResumedStateLossTimeout(prevTopActivity);
                this.mTopResumedActivityWaitingForPrev = true;
                this.mPreTopActivity = prevTopActivity;
            }
            this.mTopResumedActivity = topStack.mResumedActivity;
            scheduleTopResumedActivityStateIfNeeded();
        }
    }

    private void scheduleTopResumedActivityStateIfNeeded() {
        if (this.mTopResumedActivity != null && !this.mTopResumedActivityWaitingForPrev) {
            if (this.mPreTopActivity != null) {
                ColorAppSwitchManagerService.getInstance().handleActivityPaused(this.mPreTopActivity, this.mTopResumedActivity);
                OppoFeatureCache.get(IColorAppSwitchManager.DEFAULT).handleActivitySwitch(this.mService.mContext, this.mPreTopActivity, this.mTopResumedActivity, this.mUserLeaving);
            }
            ColorAppSwitchManagerService.getInstance().handleActivityResumed(this.mTopResumedActivity);
            ActivityRecord activityRecord = this.mTopResumedActivity;
            this.mPreTopActivity = activityRecord;
            activityRecord.scheduleTopResumedActivityChanged(true);
        }
    }

    private void scheduleTopResumedStateLossTimeout(ActivityRecord r) {
        Message msg = this.mHandler.obtainMessage(TOP_RESUMED_STATE_LOSS_TIMEOUT_MSG);
        msg.obj = r;
        r.topResumedStateLossTime = SystemClock.uptimeMillis();
        this.mHandler.sendMessageDelayed(msg, 500);
        if (ActivityTaskManagerDebugConfig.DEBUG_STATES) {
            String str = RootActivityContainer.TAG_STATES;
            Slog.v(str, "Waiting for top state to be released by " + r);
        }
    }

    /* access modifiers changed from: package-private */
    public void handleTopResumedStateReleased(boolean timeout) {
        if (ActivityTaskManagerDebugConfig.DEBUG_STATES) {
            String str = RootActivityContainer.TAG_STATES;
            StringBuilder sb = new StringBuilder();
            sb.append("Top resumed state released ");
            sb.append(timeout ? " (due to timeout)" : " (transition complete)");
            Slog.v(str, sb.toString());
        }
        this.mHandler.removeMessages(TOP_RESUMED_STATE_LOSS_TIMEOUT_MSG);
        if (this.mTopResumedActivityWaitingForPrev) {
            this.mTopResumedActivityWaitingForPrev = false;
            scheduleTopResumedActivityStateIfNeeded();
        }
    }

    /* access modifiers changed from: package-private */
    public void removeTimeoutsForActivityLocked(ActivityRecord r) {
        if (ActivityTaskManagerDebugConfig.DEBUG_IDLE) {
            String str = TAG_IDLE;
            Slog.d(str, "removeTimeoutsForActivity: Callers=" + Debug.getCallers(4));
        }
        this.mHandler.removeMessages(200, r);
    }

    /* access modifiers changed from: package-private */
    public final void scheduleResumeTopActivities() {
        if (!this.mHandler.hasMessages(RESUME_TOP_ACTIVITY_MSG)) {
            this.mHandler.sendEmptyMessage(RESUME_TOP_ACTIVITY_MSG);
        }
    }

    /* access modifiers changed from: package-private */
    public void removeSleepTimeouts() {
        this.mHandler.removeMessages(SLEEP_TIMEOUT_MSG);
    }

    /* access modifiers changed from: package-private */
    public final void scheduleSleepTimeout() {
        removeSleepTimeouts();
        this.mHandler.sendEmptyMessageDelayed(SLEEP_TIMEOUT_MSG, 5000);
    }

    /* access modifiers changed from: package-private */
    public void removeRestartTimeouts(ActivityRecord r) {
        this.mHandler.removeMessages(RESTART_ACTIVITY_PROCESS_TIMEOUT_MSG, r);
    }

    /* access modifiers changed from: package-private */
    public final void scheduleRestartTimeout(ActivityRecord r) {
        removeRestartTimeouts(r);
        ActivityStackSupervisorHandler activityStackSupervisorHandler = this.mHandler;
        activityStackSupervisorHandler.sendMessageDelayed(activityStackSupervisorHandler.obtainMessage(RESTART_ACTIVITY_PROCESS_TIMEOUT_MSG, r), 2000);
    }

    /* access modifiers changed from: package-private */
    public void handleNonResizableTaskIfNeeded(TaskRecord task, int preferredWindowingMode, int preferredDisplayId, ActivityStack actualStack) {
        handleNonResizableTaskIfNeeded(task, preferredWindowingMode, preferredDisplayId, actualStack, false);
    }

    /* access modifiers changed from: package-private */
    public void handleNonResizableTaskIfNeeded(TaskRecord task, int preferredWindowingMode, int preferredDisplayId, ActivityStack actualStack, boolean forceNonResizable) {
        boolean singleTaskInstance = false;
        boolean isSecondaryDisplayPreferred = (preferredDisplayId == 0 || preferredDisplayId == -1) ? false : true;
        if ((!(actualStack != null && actualStack.getDisplay().hasSplitScreenPrimaryStack()) && preferredWindowingMode != 3 && !isSecondaryDisplayPreferred) || !task.isActivityTypeStandardOrUndefined()) {
            return;
        }
        if (isSecondaryDisplayPreferred) {
            int actualDisplayId = task.getStack().mDisplayId;
            if (task.canBeLaunchedOnDisplay(actualDisplayId)) {
                ActivityDisplay preferredDisplay = this.mRootActivityContainer.getActivityDisplay(preferredDisplayId);
                if (preferredDisplay != null && preferredDisplay.isSingleTaskInstance()) {
                    singleTaskInstance = true;
                }
                if (preferredDisplayId != actualDisplayId) {
                    if (singleTaskInstance) {
                        this.mService.getTaskChangeNotificationController().notifyActivityLaunchOnSecondaryDisplayRerouted(task.getTaskInfo(), preferredDisplayId);
                        return;
                    }
                    Slog.w(TAG, "Failed to put " + task + " on display " + preferredDisplayId);
                    this.mService.getTaskChangeNotificationController().notifyActivityLaunchOnSecondaryDisplayFailed(task.getTaskInfo(), preferredDisplayId);
                } else if (!forceNonResizable) {
                    handleForcedResizableTaskIfNeeded(task, 2);
                }
            } else {
                throw new IllegalStateException("Task resolved to incompatible display");
            }
        } else if (!task.supportsSplitScreenWindowingMode() || forceNonResizable || OppoFeatureCache.get(IColorSplitWindowManager.DEFAULT).isInForbidActivityList(task)) {
            ActivityStack dockedStack = task.getStack().getDisplay().getSplitScreenPrimaryStack();
            if (dockedStack != null) {
                this.mService.getTaskChangeNotificationController().notifyActivityDismissingDockedStack();
                if (actualStack == dockedStack) {
                    singleTaskInstance = true;
                }
                moveTasksToFullscreenStackLocked(dockedStack, singleTaskInstance);
            }
        } else {
            handleForcedResizableTaskIfNeeded(task, 1);
        }
    }

    private void handleForcedResizableTaskIfNeeded(TaskRecord task, int reason) {
        ActivityRecord topActivity = task.getTopActivity();
        if (topActivity != null && !topActivity.noDisplay && topActivity.isNonResizableOrForcedResizable()) {
            this.mService.getTaskChangeNotificationController().notifyActivityForcedResizable(task.taskId, reason, topActivity.appInfo.packageName);
        }
    }

    /* access modifiers changed from: package-private */
    public void activityRelaunchedLocked(IBinder token) {
        this.mWindowManager.notifyAppRelaunchingFinished(token);
        ActivityRecord r = ActivityRecord.isInStackLocked(token);
        if (r != null && r.getActivityStack().shouldSleepOrShutDownActivities()) {
            r.setSleeping(true, true);
        }
    }

    /* access modifiers changed from: package-private */
    public void activityRelaunchingLocked(ActivityRecord r) {
        this.mWindowManager.notifyAppRelaunching(r.appToken);
    }

    /* access modifiers changed from: package-private */
    public void logStackState() {
        this.mActivityMetricsLogger.logWindowState();
    }

    /* access modifiers changed from: package-private */
    public void scheduleUpdateMultiWindowMode(TaskRecord task) {
        if (!task.getStack().deferScheduleMultiWindowModeChanged()) {
            for (int i = task.mActivities.size() - 1; i >= 0; i--) {
                ActivityRecord r = task.mActivities.get(i);
                if (r.attachedToProcess()) {
                    this.mMultiWindowModeChangedActivities.add(r);
                }
            }
            if (!this.mHandler.hasMessages(REPORT_MULTI_WINDOW_MODE_CHANGED_MSG)) {
                this.mHandler.sendEmptyMessage(REPORT_MULTI_WINDOW_MODE_CHANGED_MSG);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void scheduleUpdatePictureInPictureModeIfNeeded(TaskRecord task, ActivityStack prevStack) {
        ActivityStack stack = task.getStack();
        if (prevStack != null && prevStack != stack) {
            if (prevStack.inPinnedWindowingMode() || stack.inPinnedWindowingMode()) {
                scheduleUpdatePictureInPictureModeIfNeeded(task, stack.getRequestedOverrideBounds());
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void scheduleUpdatePictureInPictureModeIfNeeded(TaskRecord task, Rect targetStackBounds) {
        for (int i = task.mActivities.size() - 1; i >= 0; i--) {
            ActivityRecord r = task.mActivities.get(i);
            if (r.attachedToProcess()) {
                this.mPipModeChangedActivities.add(r);
                this.mMultiWindowModeChangedActivities.remove(r);
            }
        }
        this.mPipModeChangedTargetStackBounds = targetStackBounds;
        if (!this.mHandler.hasMessages(REPORT_PIP_MODE_CHANGED_MSG)) {
            this.mHandler.sendEmptyMessage(REPORT_PIP_MODE_CHANGED_MSG);
        }
    }

    /* access modifiers changed from: package-private */
    public void updatePictureInPictureMode(TaskRecord task, Rect targetStackBounds, boolean forceUpdate) {
        this.mHandler.removeMessages(REPORT_PIP_MODE_CHANGED_MSG);
        for (int i = task.mActivities.size() - 1; i >= 0; i--) {
            ActivityRecord r = task.mActivities.get(i);
            if (r.attachedToProcess()) {
                r.updatePictureInPictureMode(targetStackBounds, forceUpdate);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void wakeUp(String reason) {
        PowerManager powerManager = this.mPowerManager;
        long uptimeMillis = SystemClock.uptimeMillis();
        powerManager.wakeUp(uptimeMillis, 2, "android.server.am:TURN_ON:" + reason);
    }

    /* access modifiers changed from: package-private */
    public void beginDeferResume() {
        this.mDeferResumeCount++;
    }

    /* access modifiers changed from: package-private */
    public void endDeferResume() {
        this.mDeferResumeCount--;
    }

    /* access modifiers changed from: package-private */
    public boolean readyToResume() {
        return this.mDeferResumeCount == 0;
    }

    private final class ActivityStackSupervisorHandler extends Handler {
        public ActivityStackSupervisorHandler(Looper looper) {
            super(looper);
        }

        /* access modifiers changed from: package-private */
        public void activityIdleInternal(ActivityRecord r, boolean processPausingActivities) {
            synchronized (ActivityStackSupervisor.this.mService.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityStackSupervisor.this.activityIdleInternalLocked(r != null ? r.appToken : null, true, processPausingActivities, null);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        public void handleMessage(Message msg) {
            int i = msg.what;
            switch (i) {
                case 200:
                    if (ActivityTaskManagerDebugConfig.DEBUG_IDLE) {
                        Slog.d(ActivityStackSupervisor.TAG_IDLE, "handleMessage: IDLE_TIMEOUT_MSG: r=" + msg.obj);
                    }
                    activityIdleInternal((ActivityRecord) msg.obj, true);
                    return;
                case ActivityStackSupervisor.IDLE_NOW_MSG /*{ENCODED_INT: 201}*/:
                    if (ActivityTaskManagerDebugConfig.DEBUG_IDLE) {
                        Slog.d(ActivityStackSupervisor.TAG_IDLE, "handleMessage: IDLE_NOW_MSG: r=" + msg.obj);
                    }
                    activityIdleInternal((ActivityRecord) msg.obj, false);
                    return;
                case ActivityStackSupervisor.RESUME_TOP_ACTIVITY_MSG /*{ENCODED_INT: 202}*/:
                    synchronized (ActivityStackSupervisor.this.mService.mGlobalLock) {
                        try {
                            WindowManagerService.boostPriorityForLockedSection();
                            ActivityStackSupervisor.this.mRootActivityContainer.resumeFocusedStacksTopActivities();
                        } finally {
                            WindowManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    return;
                case ActivityStackSupervisor.SLEEP_TIMEOUT_MSG /*{ENCODED_INT: 203}*/:
                    synchronized (ActivityStackSupervisor.this.mService.mGlobalLock) {
                        try {
                            WindowManagerService.boostPriorityForLockedSection();
                            if (ActivityStackSupervisor.this.mService.isSleepingOrShuttingDownLocked()) {
                                Slog.w(ActivityStackSupervisor.TAG, "Sleep timeout!  Sleeping now.");
                                ActivityStackSupervisor.this.checkReadyForSleepLocked(false);
                            }
                        } finally {
                            WindowManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    return;
                case ActivityStackSupervisor.LAUNCH_TIMEOUT_MSG /*{ENCODED_INT: 204}*/:
                    synchronized (ActivityStackSupervisor.this.mService.mGlobalLock) {
                        try {
                            WindowManagerService.boostPriorityForLockedSection();
                            if (ActivityStackSupervisor.this.mLaunchingActivityWakeLock.isHeld()) {
                                Slog.w(ActivityStackSupervisor.TAG, "Launch timeout has expired, giving up wake lock!");
                                ActivityStackSupervisor.this.mLaunchingActivityWakeLock.release();
                            }
                        } finally {
                            WindowManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    return;
                default:
                    switch (i) {
                        case 212:
                            synchronized (ActivityStackSupervisor.this.mService.mGlobalLock) {
                                try {
                                    WindowManagerService.boostPriorityForLockedSection();
                                    ActivityRecord r = ActivityRecord.forTokenLocked((IBinder) msg.obj);
                                    if (r != null) {
                                        ActivityStackSupervisor.this.handleLaunchTaskBehindCompleteLocked(r);
                                    }
                                } finally {
                                    WindowManagerService.resetPriorityAfterLockedSection();
                                }
                            }
                            return;
                        case ActivityStackSupervisor.RESTART_ACTIVITY_PROCESS_TIMEOUT_MSG /*{ENCODED_INT: 213}*/:
                            ActivityRecord r2 = (ActivityRecord) msg.obj;
                            String processName = null;
                            int uid = 0;
                            synchronized (ActivityStackSupervisor.this.mService.mGlobalLock) {
                                try {
                                    WindowManagerService.boostPriorityForLockedSection();
                                    if (r2.attachedToProcess() && r2.isState(ActivityStack.ActivityState.RESTARTING_PROCESS)) {
                                        processName = r2.app.mName;
                                        uid = r2.app.mUid;
                                    }
                                } finally {
                                    WindowManagerService.resetPriorityAfterLockedSection();
                                }
                            }
                            if (processName != null) {
                                ActivityStackSupervisor.this.mService.mAmInternal.killProcess(processName, uid, "restartActivityProcessTimeout");
                                return;
                            }
                            return;
                        case ActivityStackSupervisor.REPORT_MULTI_WINDOW_MODE_CHANGED_MSG /*{ENCODED_INT: 214}*/:
                            synchronized (ActivityStackSupervisor.this.mService.mGlobalLock) {
                                try {
                                    WindowManagerService.boostPriorityForLockedSection();
                                    for (int i2 = ActivityStackSupervisor.this.mMultiWindowModeChangedActivities.size() - 1; i2 >= 0; i2--) {
                                        ActivityStackSupervisor.this.mMultiWindowModeChangedActivities.remove(i2).updateMultiWindowMode();
                                    }
                                } finally {
                                    WindowManagerService.resetPriorityAfterLockedSection();
                                }
                            }
                            return;
                        case ActivityStackSupervisor.REPORT_PIP_MODE_CHANGED_MSG /*{ENCODED_INT: 215}*/:
                            synchronized (ActivityStackSupervisor.this.mService.mGlobalLock) {
                                try {
                                    WindowManagerService.boostPriorityForLockedSection();
                                    for (int i3 = ActivityStackSupervisor.this.mPipModeChangedActivities.size() - 1; i3 >= 0; i3--) {
                                        ActivityStackSupervisor.this.mPipModeChangedActivities.remove(i3).updatePictureInPictureMode(ActivityStackSupervisor.this.mPipModeChangedTargetStackBounds, false);
                                    }
                                } finally {
                                    WindowManagerService.resetPriorityAfterLockedSection();
                                }
                            }
                            return;
                        case ActivityStackSupervisor.REPORT_HOME_CHANGED_MSG /*{ENCODED_INT: 216}*/:
                            synchronized (ActivityStackSupervisor.this.mService.mGlobalLock) {
                                try {
                                    WindowManagerService.boostPriorityForLockedSection();
                                    ActivityStackSupervisor.this.mHandler.removeMessages(ActivityStackSupervisor.REPORT_HOME_CHANGED_MSG);
                                    ActivityStackSupervisor.this.mRootActivityContainer.startHomeOnEmptyDisplays("homeChanged");
                                } finally {
                                    WindowManagerService.resetPriorityAfterLockedSection();
                                }
                            }
                            return;
                        case ActivityStackSupervisor.TOP_RESUMED_STATE_LOSS_TIMEOUT_MSG /*{ENCODED_INT: 217}*/:
                            ActivityRecord r3 = (ActivityRecord) msg.obj;
                            Slog.w(ActivityStackSupervisor.TAG, "Activity top resumed state loss timeout for " + r3);
                            synchronized (ActivityStackSupervisor.this.mService.mGlobalLock) {
                                try {
                                    WindowManagerService.boostPriorityForLockedSection();
                                    if (r3.hasProcess()) {
                                        ActivityStackSupervisor.this.mService.logAppTooSlow(r3.app, r3.topResumedStateLossTime, "top state loss for " + r3);
                                    }
                                    ActivityStackSupervisor.this.handleTopResumedStateReleased(true);
                                } finally {
                                    WindowManagerService.resetPriorityAfterLockedSection();
                                }
                            }
                            return;
                        default:
                            return;
                    }
            }
        }
    }

    @OppoHook(level = OppoHook.OppoHookType.NEW_METHOD, note = "ZhiYong.Lin@Plf.Framework add for BPM", property = OppoHook.OppoRomType.ROM)
    public ActivityRecord getTopRunningActivityLocked() {
        ActivityRecord activityRecord;
        synchronized (this.mService.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                activityRecord = this.mService.mRootActivityContainer.topRunningActivity();
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        return activityRecord;
    }

    /* access modifiers changed from: package-private */
    public void setResizingDuringAnimation(TaskRecord task) {
        this.mResizingTasksDuringAnimation.add(Integer.valueOf(task.taskId));
        task.setTaskDockedResizing(true);
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:141:0x0369  */
    public int startActivityFromRecents(int callingPid, int callingUid, int taskId, SafeActivityOptions options) {
        ActivityOptions activityOptions;
        int activityType;
        int windowingMode;
        boolean z;
        String str;
        int i;
        TaskRecord task;
        int i2;
        int windowingMode2;
        ActivityDisplay display;
        int activityType2;
        ActivityOptions activityOptions2;
        int i3;
        String str2;
        int activityType3;
        TaskRecord task2;
        boolean z2;
        int activityType4;
        ActivityOptions activityOptions3;
        ActivityRecord targetActivity;
        int i4;
        String str3;
        int activityType5;
        TaskRecord task3;
        boolean z3;
        IColorActivityRecordEx arEx = null;
        if (options != null) {
            activityOptions = options.getOptions(this);
        } else {
            activityOptions = null;
        }
        if (activityOptions != null) {
            int activityType6 = activityOptions.getLaunchActivityType();
            int windowingMode3 = activityOptions.getLaunchWindowingMode();
            if (activityOptions.freezeRecentTasksReordering()) {
                if (this.mRecentTasks.isCallerRecents(callingUid)) {
                    this.mRecentTasks.setFreezeTaskListReordering();
                }
            }
            activityType = activityType6;
            windowingMode = windowingMode3;
        } else {
            activityType = 0;
            windowingMode = 0;
        }
        if (activityType == 2 || activityType == 3) {
            throw new IllegalArgumentException("startActivityFromRecents: Task " + taskId + " can't be launch in the home/recents stack.");
        }
        this.mWindowManager.deferSurfaceLayout();
        if (windowingMode == 3) {
            try {
                this.mWindowManager.setDockedStackCreateState(activityOptions.getSplitScreenCreateMode(), null);
                deferUpdateRecentsHomeStackBounds();
                this.mWindowManager.prepareAppTransition(19, false);
            } catch (Throwable th) {
                th = th;
                i2 = 3;
                windowingMode2 = windowingMode;
                z = false;
                str = "startActivityFromRecents: homeVisibleInSplitScreen";
                i = 4;
                task = null;
            }
        }
        try {
            TaskRecord task4 = this.mRootActivityContainer.anyTaskForId(taskId, 2, activityOptions, true);
            if (task4 != null) {
                try {
                    if (task4.getTopActivity() != null) {
                        try {
                            arEx = task4.getTopActivity().mColorArEx;
                        } catch (Throwable th2) {
                            th = th2;
                            i2 = 3;
                            str = "startActivityFromRecents: homeVisibleInSplitScreen";
                            i = 4;
                        }
                    }
                    if (OppoFeatureCache.get(IColorAppStartupManager.DEFAULT).checkPreventIndulge(arEx)) {
                        continueUpdateRecentsHomeStackBounds();
                        this.mWindowManager.executeAppTransition();
                        if (windowingMode == 3) {
                            setResizingDuringAnimation(task4);
                            ActivityDisplay display2 = task4.getStack().getDisplay();
                            if (display2.getTopStackInWindowingMode(4).isActivityTypeHome()) {
                                display2.moveHomeStackToFront("startActivityFromRecents: homeVisibleInSplitScreen");
                                this.mWindowManager.checkSplitScreenMinimizedChanged(false);
                            }
                        }
                        this.mWindowManager.continueSurfaceLayout();
                        return 0;
                    }
                    ActivityRecord r = task4.getTopActivity();
                    if (r == null || !this.mService.mOppoArmyController.isRunningDisallowed(r.packageName)) {
                        if (windowingMode != 3) {
                            try {
                                this.mRootActivityContainer.getDefaultDisplay().moveHomeStackToFront("startActivityFromRecents");
                            } catch (Throwable th3) {
                                th = th3;
                                task = task4;
                                i2 = 3;
                                windowingMode2 = windowingMode;
                                z = false;
                                str = "startActivityFromRecents: homeVisibleInSplitScreen";
                                i = 4;
                                setResizingDuringAnimation(task);
                                display = task.getStack().getDisplay();
                                if (display.getTopStackInWindowingMode(i).isActivityTypeHome()) {
                                }
                                this.mWindowManager.continueSurfaceLayout();
                                throw th;
                            }
                        }
                        if (windowingMode == 3) {
                            OppoFeatureCache.get(IColorAccessControlLocalManager.DEFAULT).notifyInSplitScreenMode(task4.getStack());
                        }
                        try {
                            if (!this.mService.mAmInternal.shouldConfirmCredentials(task4.userId)) {
                                try {
                                    if (task4.getRootActivity() != null) {
                                        ActivityRecord targetActivity2 = task4.getTopActivity();
                                        this.mRootActivityContainer.sendPowerHintForLaunchStartIfNeeded(true, targetActivity2);
                                        this.mActivityMetricsLogger.notifyActivityLaunching(task4.intent);
                                        try {
                                            try {
                                                activityOptions3 = activityOptions;
                                                targetActivity = targetActivity2;
                                                str3 = "startActivityFromRecents: homeVisibleInSplitScreen";
                                                activityType4 = activityType;
                                                i4 = 3;
                                                activityType5 = 4;
                                                try {
                                                    this.mService.moveTaskToFrontLocked(null, null, task4.taskId, 0, options, true);
                                                    targetActivity.applyOptionsLocked();
                                                    try {
                                                        this.mActivityMetricsLogger.notifyActivityLaunched(2, targetActivity);
                                                        this.mService.getActivityStartController().postStartActivityProcessingForLastStarter(task4.getTopActivity(), 2, task4.getStack());
                                                        if (windowingMode == 3) {
                                                            setResizingDuringAnimation(task4);
                                                            ActivityDisplay display3 = task4.getStack().getDisplay();
                                                            if (display3.getTopStackInWindowingMode(4).isActivityTypeHome()) {
                                                                display3.moveHomeStackToFront(str3);
                                                                this.mWindowManager.checkSplitScreenMinimizedChanged(false);
                                                            }
                                                        }
                                                        this.mWindowManager.continueSurfaceLayout();
                                                        return 2;
                                                    } catch (Throwable th4) {
                                                        th = th4;
                                                        task = task4;
                                                        windowingMode2 = windowingMode;
                                                        i = 4;
                                                        str = str3;
                                                        i2 = 3;
                                                        z = false;
                                                    }
                                                } catch (Throwable th5) {
                                                    th = th5;
                                                    task3 = task4;
                                                    z3 = false;
                                                    try {
                                                        this.mActivityMetricsLogger.notifyActivityLaunched(2, targetActivity);
                                                        throw th;
                                                    } catch (Throwable th6) {
                                                        th = th6;
                                                        task = task3;
                                                        windowingMode2 = windowingMode;
                                                        str = str3;
                                                        i2 = i4;
                                                        z = z3;
                                                        i = activityType5;
                                                        setResizingDuringAnimation(task);
                                                        display = task.getStack().getDisplay();
                                                        if (display.getTopStackInWindowingMode(i).isActivityTypeHome()) {
                                                        }
                                                        this.mWindowManager.continueSurfaceLayout();
                                                        throw th;
                                                    }
                                                }
                                            } catch (Throwable th7) {
                                                th = th7;
                                                task3 = task4;
                                                str3 = "startActivityFromRecents: homeVisibleInSplitScreen";
                                                activityType4 = activityType;
                                                activityOptions3 = activityOptions;
                                                activityType5 = 4;
                                                z3 = false;
                                                i4 = 3;
                                                targetActivity = targetActivity2;
                                                this.mActivityMetricsLogger.notifyActivityLaunched(2, targetActivity);
                                                throw th;
                                            }
                                        } catch (Throwable th8) {
                                            th = th8;
                                            str3 = "startActivityFromRecents: homeVisibleInSplitScreen";
                                            activityType4 = activityType;
                                            activityOptions3 = activityOptions;
                                            activityType5 = 4;
                                            z3 = false;
                                            targetActivity = targetActivity2;
                                            i4 = 3;
                                            task3 = task4;
                                            this.mActivityMetricsLogger.notifyActivityLaunched(2, targetActivity);
                                            throw th;
                                        }
                                    } else {
                                        str2 = "startActivityFromRecents: homeVisibleInSplitScreen";
                                        activityType2 = activityType;
                                        activityOptions2 = activityOptions;
                                        activityType3 = 4;
                                        z2 = false;
                                        i3 = 3;
                                        task2 = task4;
                                    }
                                } catch (Throwable th9) {
                                    th = th9;
                                    task = task4;
                                    windowingMode2 = windowingMode;
                                    str = "startActivityFromRecents: homeVisibleInSplitScreen";
                                    i2 = 3;
                                    z = false;
                                    i = 4;
                                    if (windowingMode2 == i2 && task != null) {
                                        setResizingDuringAnimation(task);
                                        display = task.getStack().getDisplay();
                                        if (display.getTopStackInWindowingMode(i).isActivityTypeHome()) {
                                            display.moveHomeStackToFront(str);
                                            this.mWindowManager.checkSplitScreenMinimizedChanged(z);
                                        }
                                    }
                                    this.mWindowManager.continueSurfaceLayout();
                                    throw th;
                                }
                            } else {
                                str2 = "startActivityFromRecents: homeVisibleInSplitScreen";
                                activityType2 = activityType;
                                activityOptions2 = activityOptions;
                                activityType3 = 4;
                                z2 = false;
                                i3 = 3;
                                task2 = task4;
                            }
                        } catch (Throwable th10) {
                            th = th10;
                            task = task4;
                            i2 = 3;
                            windowingMode2 = windowingMode;
                            z = false;
                            str = "startActivityFromRecents: homeVisibleInSplitScreen";
                            i = 4;
                            setResizingDuringAnimation(task);
                            display = task.getStack().getDisplay();
                            if (display.getTopStackInWindowingMode(i).isActivityTypeHome()) {
                            }
                            this.mWindowManager.continueSurfaceLayout();
                            throw th;
                        }
                        try {
                            String callingPackage = task2.mCallingPackage;
                            Intent intent = task2.intent;
                            intent.addFlags(DumpState.DUMP_DEXOPT);
                            try {
                                int startActivityInPackage = this.mService.getActivityStartController().startActivityInPackage(task2.mCallingUid, callingPid, callingUid, callingPackage, intent, null, null, null, 0, 0, options, task2.userId, task2, "startActivityFromRecents", false, null, false);
                                if (windowingMode == 3) {
                                    setResizingDuringAnimation(task2);
                                    ActivityDisplay display4 = task2.getStack().getDisplay();
                                    if (display4.getTopStackInWindowingMode(4).isActivityTypeHome()) {
                                        display4.moveHomeStackToFront(str2);
                                        this.mWindowManager.checkSplitScreenMinimizedChanged(false);
                                    }
                                }
                                this.mWindowManager.continueSurfaceLayout();
                                return startActivityInPackage;
                            } catch (Throwable th11) {
                                th = th11;
                                task = task2;
                                windowingMode2 = windowingMode;
                                str = str2;
                                i2 = 3;
                                i = 4;
                                z = false;
                                setResizingDuringAnimation(task);
                                display = task.getStack().getDisplay();
                                if (display.getTopStackInWindowingMode(i).isActivityTypeHome()) {
                                }
                                this.mWindowManager.continueSurfaceLayout();
                                throw th;
                            }
                        } catch (Throwable th12) {
                            th = th12;
                            task = task2;
                            windowingMode2 = windowingMode;
                            str = str2;
                            i2 = i3;
                            z = z2;
                            i = activityType3;
                            setResizingDuringAnimation(task);
                            display = task.getStack().getDisplay();
                            if (display.getTopStackInWindowingMode(i).isActivityTypeHome()) {
                            }
                            this.mWindowManager.continueSurfaceLayout();
                            throw th;
                        }
                    } else {
                        continueUpdateRecentsHomeStackBounds();
                        this.mWindowManager.executeAppTransition();
                        this.mService.mOppoArmyController.showDisallowedRunningAppDialog();
                        if (windowingMode == 3) {
                            setResizingDuringAnimation(task4);
                            ActivityDisplay display5 = task4.getStack().getDisplay();
                            if (display5.getTopStackInWindowingMode(4).isActivityTypeHome()) {
                                display5.moveHomeStackToFront("startActivityFromRecents: homeVisibleInSplitScreen");
                                this.mWindowManager.checkSplitScreenMinimizedChanged(false);
                            }
                        }
                        this.mWindowManager.continueSurfaceLayout();
                        return 0;
                    }
                } catch (Throwable th13) {
                    th = th13;
                    i2 = 3;
                    str = "startActivityFromRecents: homeVisibleInSplitScreen";
                    i = 4;
                    task = task4;
                    windowingMode2 = windowingMode;
                    z = false;
                    setResizingDuringAnimation(task);
                    display = task.getStack().getDisplay();
                    if (display.getTopStackInWindowingMode(i).isActivityTypeHome()) {
                    }
                    this.mWindowManager.continueSurfaceLayout();
                    throw th;
                }
            } else {
                i2 = 3;
                str = "startActivityFromRecents: homeVisibleInSplitScreen";
                i = 4;
                task = task4;
                windowingMode2 = windowingMode;
                z = false;
                try {
                    continueUpdateRecentsHomeStackBounds();
                    this.mWindowManager.executeAppTransition();
                    throw new IllegalArgumentException("startActivityFromRecents: Task " + taskId + " not found.");
                } catch (Throwable th14) {
                    th = th14;
                    setResizingDuringAnimation(task);
                    display = task.getStack().getDisplay();
                    if (display.getTopStackInWindowingMode(i).isActivityTypeHome()) {
                    }
                    this.mWindowManager.continueSurfaceLayout();
                    throw th;
                }
            }
        } catch (Throwable th15) {
            th = th15;
            i2 = 3;
            windowingMode2 = windowingMode;
            z = false;
            str = "startActivityFromRecents: homeVisibleInSplitScreen";
            i = 4;
            task = null;
            setResizingDuringAnimation(task);
            display = task.getStack().getDisplay();
            if (display.getTopStackInWindowingMode(i).isActivityTypeHome()) {
            }
            this.mWindowManager.continueSurfaceLayout();
            throw th;
        }
    }

    static class WaitInfo {
        private final WaitResult mResult;
        private final long mStartTimeMs;
        private final ComponentName mTargetComponent;

        WaitInfo(ComponentName targetComponent, WaitResult result, long startTimeMs) {
            this.mTargetComponent = targetComponent;
            this.mResult = result;
            this.mStartTimeMs = startTimeMs;
        }

        public boolean matches(ComponentName targetComponent) {
            ComponentName componentName = this.mTargetComponent;
            return componentName == null || componentName.equals(targetComponent);
        }

        public WaitResult getResult() {
            return this.mResult;
        }

        public long getStartTime() {
            return this.mStartTimeMs;
        }

        public ComponentName getComponent() {
            return this.mTargetComponent;
        }

        public void dump(PrintWriter pw, String prefix) {
            pw.println(prefix + "WaitInfo:");
            pw.println(prefix + "  mTargetComponent=" + this.mTargetComponent);
            StringBuilder sb = new StringBuilder();
            sb.append(prefix);
            sb.append("  mResult=");
            pw.println(sb.toString());
            this.mResult.dump(pw, prefix);
        }
    }

    public class ColorActivityStackSupervisorInner implements IColorActivityStackSupervisorInner {
        public ColorActivityStackSupervisorInner() {
        }

        @Override // com.android.server.wm.IColorActivityStackSupervisorInner
        public <T extends ActivityStack> T getStack(int windowingMode, int activityType) {
            return null;
        }
    }

    @Override // com.android.server.wm.OppoBaseActivityStackSupervisor
    public IColorActivityStackSupervisorInner createColorActivityStackSupervisorInner() {
        return new ColorActivityStackSupervisorInner();
    }

    /* access modifiers changed from: package-private */
    public void dataCollectionInfoExp(String callerApp, String callingPackage, Intent intent) {
        ComponentName realActivity;
        if (intent != null && intent.getComponent() != null && (realActivity = intent.getComponent()) != null && "com.android.packageinstaller.PackageInstallerActivity".equals(realActivity.getClassName()) && callerApp != null && !"com.google.android.packageinstaller".equals(callerApp)) {
            Slog.d(TAG, "dataCollectionInfoExp: callAppName" + callerApp + " callingPackage " + callingPackage);
            intent.putExtra("android.intent.extra.INSTALLER_PACKAGE_NAME", callerApp);
        }
    }

    public ComponentName getTopAppName() {
        ActivityStack stack = this.mRootActivityContainer.getDefaultDisplay().getFocusedStack();
        if (stack != null) {
            return stack.getTopAppName();
        }
        return null;
    }
}
