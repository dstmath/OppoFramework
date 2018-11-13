package com.android.server.am;

import android.annotation.OppoHook;
import android.annotation.OppoHook.OppoHookType;
import android.annotation.OppoHook.OppoRomType;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.ActivityManager.StackId;
import android.app.ActivityOptions;
import android.app.ActivityThread;
import android.app.AppGlobals;
import android.app.IActivityController;
import android.app.IApplicationThread;
import android.app.ResultInfo;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ActivityInfo.WindowLayout;
import android.content.pm.ApplicationInfo;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.OppoUsageManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.os.UserHandle;
import android.service.voice.IVoiceInteractionSession;
import android.text.format.Time;
import android.util.ArraySet;
import android.util.BoostFramework;
import android.util.EventLog;
import android.util.IntArray;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import com.android.internal.app.ActivityTrigger;
import com.android.internal.app.IVoiceInteractor;
import com.android.internal.os.BatteryStatsImpl;
import com.android.internal.os.BatteryStatsImpl.Uid.Proc;
import com.android.server.Watchdog;
import com.android.server.coloros.OppoListManager;
import com.android.server.pm.CompatibilityHelper;
import com.android.server.wm.StackWindowController;
import com.android.server.wm.StackWindowListener;
import com.android.server.wm.WindowManagerService;
import com.oppo.hypnus.Hypnus;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

class ActivityStack<T extends StackWindowController> extends ConfigurationContainer implements StackWindowListener {
    /* renamed from: -com-android-server-am-ActivityStack$ActivityStateSwitchesValues */
    private static final /* synthetic */ int[] f28-com-android-server-am-ActivityStack$ActivityStateSwitchesValues = null;
    private static final long ACTIVITY_INACTIVE_RESET_TIME = 0;
    static final int DESTROY_ACTIVITIES_MSG = 105;
    private static final int DESTROY_TIMEOUT = 10000;
    static final int DESTROY_TIMEOUT_MSG = 102;
    static final int FINISH_AFTER_PAUSE = 1;
    static final int FINISH_AFTER_VISIBLE = 2;
    static final int FINISH_IMMEDIATELY = 0;
    private static final int FIT_WITHIN_BOUNDS_DIVIDER = 3;
    static final int LAUNCH_TICK = 500;
    static final int LAUNCH_TICK_MSG = 103;
    private static final int MAX_STOPPING_TO_FORCE = 3;
    private static final String MM_WEB_UI = "com.tencent.mm.plugin.webview.ui.tools.WebViewUI";
    private static final String OPPO_SECURITYPAY_FEATURE = "oppo.securitypay.support";
    private static final int PAUSE_TIMEOUT = 500;
    static final int PAUSE_TIMEOUT_MSG = 101;
    protected static final int REMOVE_TASK_MODE_DESTROYING = 0;
    static final int REMOVE_TASK_MODE_MOVING = 1;
    static final int REMOVE_TASK_MODE_MOVING_TO_TOP = 2;
    private static final boolean SHOW_APP_STARTING_PREVIEW = true;
    protected static final int SPLIT_TIMEOUT = 500;
    static final int STACK_INVISIBLE = 0;
    static final int STACK_VISIBLE = 1;
    private static final int STOP_TIMEOUT = 10000;
    static final int STOP_TIMEOUT_MSG = 104;
    private static final String SWAP_DOCKED_STACK = "swapDockedAndFullscreenStack";
    private static final String TAG = "ActivityManager";
    private static final String TAG_ADD_REMOVE = (TAG + ActivityManagerDebugConfig.POSTFIX_ADD_REMOVE);
    private static final String TAG_APP = (TAG + ActivityManagerDebugConfig.POSTFIX_APP);
    private static final String TAG_CLEANUP = (TAG + ActivityManagerDebugConfig.POSTFIX_CLEANUP);
    private static final String TAG_CONTAINERS = (TAG + ActivityManagerDebugConfig.POSTFIX_CONTAINERS);
    private static final String TAG_PAUSE = (TAG + ActivityManagerDebugConfig.POSTFIX_PAUSE);
    private static final String TAG_RELEASE = (TAG + ActivityManagerDebugConfig.POSTFIX_RELEASE);
    private static final String TAG_RESULTS = (TAG + ActivityManagerDebugConfig.POSTFIX_RESULTS);
    private static final String TAG_SAVED_STATE = (TAG + ActivityManagerDebugConfig.POSTFIX_SAVED_STATE);
    private static final String TAG_STACK = (TAG + ActivityManagerDebugConfig.POSTFIX_STACK);
    private static final String TAG_STATES = (TAG + ActivityManagerDebugConfig.POSTFIX_STATES);
    private static final String TAG_SWITCH = (TAG + ActivityManagerDebugConfig.POSTFIX_SWITCH);
    private static final String TAG_TASKS = (TAG + ActivityManagerDebugConfig.POSTFIX_TASKS);
    private static final String TAG_TRANSITION = (TAG + ActivityManagerDebugConfig.POSTFIX_TRANSITION);
    private static final String TAG_USER_LEAVING = (TAG + ActivityManagerDebugConfig.POSTFIX_USER_LEAVING);
    private static final String TAG_VISIBILITY = (TAG + ActivityManagerDebugConfig.POSTFIX_VISIBILITY);
    private static final long TRANSLUCENT_CONVERSION_TIMEOUT = 2000;
    static final int TRANSLUCENT_TIMEOUT_MSG = 106;
    private static final int VISIABLE_ACTIVITY_LAUNCH_MAX = 10;
    static final ActivityTrigger mActivityTrigger = new ActivityTrigger();
    public static Hypnus mHyp = null;
    Rect mBounds = null;
    private boolean mClosedSuperFirewall = false;
    ComponentName mComponentName;
    boolean mConfigWillChange;
    int mCurrentUser;
    private final Rect mDeferredBounds = new Rect();
    private final Rect mDeferredTaskBounds = new Rect();
    private final Rect mDeferredTaskInsetBounds = new Rect();
    int mDisplayId;
    ComponentName mDockComponentName;
    boolean mForceHidden = false;
    boolean mFullscreen = true;
    long mFullyDrawnStartTime = 0;
    final Handler mHandler;
    protected boolean mHasRunningActivity = false;
    boolean mIsClearTask = false;
    final ArrayList<ActivityRecord> mLRUActivities = new ArrayList();
    ActivityRecord mLastNoHistoryActivity = null;
    ActivityRecord mLastPausedActivity = null;
    private ComponentName mLastRecordCmpName = null;
    private String mLastRecordPkgName = null;
    long mLaunchStartTime = 0;
    final ArrayList<ActivityRecord> mNoAnimActivities = new ArrayList();
    ActivityRecord mPausingActivity = null;
    public BoostFramework mPerf = null;
    private final RecentTasks mRecentTasks;
    ActivityRecord mResumedActivity = null;
    final ActivityManagerService mService;
    final int mStackId;
    protected final ActivityStackSupervisor mStackSupervisor;
    ArrayList<ActivityStack> mStacks;
    private final ArrayList<TaskRecord> mTaskHistory = new ArrayList();
    private final LaunchingTaskPositioner mTaskPositioner;
    private final SparseArray<Rect> mTmpBounds = new SparseArray();
    private final SparseArray<Configuration> mTmpConfigs = new SparseArray();
    private final SparseArray<Rect> mTmpInsetBounds = new SparseArray();
    private final Rect mTmpRect2 = new Rect();
    private boolean mTopActivityOccludesKeyguard;
    private ActivityRecord mTopDismissingKeyguardActivity;
    ActivityRecord mTranslucentActivityWaiting = null;
    ArrayList<ActivityRecord> mUndrawnActivitiesBelowTopTranslucent = new ArrayList();
    private boolean mUpdateBoundsDeferred;
    private boolean mUpdateBoundsDeferredCalled;
    T mWindowContainerController;
    private final WindowManagerService mWindowManager;

    private class ActivityStackHandler extends Handler {
        ActivityStackHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            IBinder iBinder = null;
            ActivityRecord r;
            switch (msg.what) {
                case 101:
                    r = msg.obj;
                    Slog.w(ActivityStack.TAG, "Activity pause timeout for " + r);
                    synchronized (ActivityStack.this.mService) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            if (r.app != null) {
                                ActivityStack.this.mService.logAppTooSlow(r.app, r.pauseTime, "pausing " + r);
                            }
                            ActivityStack.this.activityPausedLocked(r.appToken, true);
                        } finally {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    return;
                case 102:
                    r = (ActivityRecord) msg.obj;
                    Slog.w(ActivityStack.TAG, "Activity destroy timeout for " + r);
                    synchronized (ActivityStack.this.mService) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            ActivityStack activityStack = ActivityStack.this;
                            if (r != null) {
                                iBinder = r.appToken;
                            }
                            activityStack.activityDestroyedLocked(iBinder, "destroyTimeout");
                        } finally {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    return;
                case 103:
                    r = (ActivityRecord) msg.obj;
                    synchronized (ActivityStack.this.mService) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            if (r.continueLaunchTickingLocked()) {
                                ActivityStack.this.mService.logAppTooSlow(r.app, r.launchTickTime, "launching " + r);
                            }
                        } finally {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    return;
                case 104:
                    r = (ActivityRecord) msg.obj;
                    Slog.w(ActivityStack.TAG, "Activity stop timeout for " + r);
                    synchronized (ActivityStack.this.mService) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            if (r.isInHistory()) {
                                r.activityStoppedLocked(null, null, null);
                            }
                        } finally {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    return;
                case 105:
                    ScheduleDestroyArgs args = msg.obj;
                    synchronized (ActivityStack.this.mService) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            ActivityStack.this.destroyActivitiesLocked(args.mOwner, args.mReason);
                        } finally {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    return;
                case 106:
                    synchronized (ActivityStack.this.mService) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            ActivityStack.this.notifyActivityDrawnLocked(null);
                        } finally {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    return;
                default:
                    return;
            }
        }
    }

    enum ActivityState {
        INITIALIZING,
        RESUMED,
        PAUSING,
        PAUSED,
        STOPPING,
        STOPPED,
        FINISHING,
        DESTROYING,
        DESTROYED
    }

    private static class ScheduleDestroyArgs {
        final ProcessRecord mOwner;
        final String mReason;

        ScheduleDestroyArgs(ProcessRecord owner, String reason) {
            this.mOwner = owner;
            this.mReason = reason;
        }
    }

    private class UsageRecorderRunnable implements Runnable {
        private String mProcName = null;
        private String mTimeStr = null;

        public UsageRecorderRunnable(String procName, String timeStr) {
            this.mProcName = procName;
            this.mTimeStr = timeStr;
        }

        public void run() {
            if (this.mProcName != null && this.mProcName.length() > 0 && this.mTimeStr != null && this.mTimeStr.length() > 0) {
                OppoUsageManager.getOppoUsageManager().writeAppUsageHistoryRecord(this.mProcName, this.mTimeStr);
            }
        }
    }

    /* renamed from: -getcom-android-server-am-ActivityStack$ActivityStateSwitchesValues */
    private static /* synthetic */ int[] m33x775af271() {
        if (f28-com-android-server-am-ActivityStack$ActivityStateSwitchesValues != null) {
            return f28-com-android-server-am-ActivityStack$ActivityStateSwitchesValues;
        }
        int[] iArr = new int[ActivityState.values().length];
        try {
            iArr[ActivityState.DESTROYED.ordinal()] = 7;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[ActivityState.DESTROYING.ordinal()] = 8;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[ActivityState.FINISHING.ordinal()] = 9;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[ActivityState.INITIALIZING.ordinal()] = 1;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[ActivityState.PAUSED.ordinal()] = 2;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[ActivityState.PAUSING.ordinal()] = 3;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[ActivityState.RESUMED.ordinal()] = 4;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[ActivityState.STOPPED.ordinal()] = 5;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[ActivityState.STOPPING.ordinal()] = 6;
        } catch (NoSuchFieldError e9) {
        }
        f28-com-android-server-am-ActivityStack$ActivityStateSwitchesValues = iArr;
        return iArr;
    }

    protected int getChildCount() {
        return this.mTaskHistory.size();
    }

    protected ConfigurationContainer getChildAt(int index) {
        return (ConfigurationContainer) this.mTaskHistory.get(index);
    }

    protected ConfigurationContainer getParent() {
        return getDisplay();
    }

    void onParentChanged() {
        super.onParentChanged();
        this.mStackSupervisor.updateUIDsPresentOnDisplay();
    }

    int numActivities() {
        int count = 0;
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            count += ((TaskRecord) this.mTaskHistory.get(taskNdx)).mActivities.size();
        }
        return count;
    }

    ActivityStack(ActivityDisplay display, int stackId, ActivityStackSupervisor supervisor, RecentTasks recentTasks, boolean onTop) {
        Rect rect = null;
        this.mStackSupervisor = supervisor;
        this.mService = supervisor.mService;
        this.mHandler = new ActivityStackHandler(this.mService.mHandler.getLooper());
        this.mWindowManager = this.mService.mWindowManager;
        this.mStackId = stackId;
        this.mCurrentUser = this.mService.mUserController.getCurrentUserIdLocked();
        this.mRecentTasks = recentTasks;
        this.mTaskPositioner = this.mStackId == 2 ? new LaunchingTaskPositioner() : null;
        this.mTmpRect2.setEmpty();
        this.mWindowContainerController = createStackWindowController(display.mDisplayId, onTop, this.mTmpRect2);
        this.mStackSupervisor.mStacks.put(this.mStackId, this);
        if (!this.mTmpRect2.isEmpty()) {
            rect = this.mTmpRect2;
        }
        postAddToDisplay(display, rect, onTop);
    }

    T createStackWindowController(int displayId, boolean onTop, Rect outBounds) {
        return new StackWindowController(this.mStackId, this, displayId, onTop, outBounds);
    }

    T getWindowContainerController() {
        return this.mWindowContainerController;
    }

    void reparent(ActivityDisplay activityDisplay, boolean onTop) {
        removeFromDisplay();
        this.mTmpRect2.setEmpty();
        postAddToDisplay(activityDisplay, this.mTmpRect2.isEmpty() ? null : this.mTmpRect2, onTop);
        adjustFocusToNextFocusableStackLocked("reparent", true);
        this.mStackSupervisor.resumeFocusedStackTopActivityLocked();
        this.mStackSupervisor.ensureActivitiesVisibleLocked(null, 0, false);
        this.mWindowContainerController.reparent(activityDisplay.mDisplayId, this.mTmpRect2, onTop);
    }

    private void postAddToDisplay(ActivityDisplay activityDisplay, Rect bounds, boolean onTop) {
        this.mDisplayId = activityDisplay.mDisplayId;
        this.mStacks = activityDisplay.mStacks;
        this.mBounds = bounds != null ? new Rect(bounds) : null;
        this.mFullscreen = this.mBounds == null;
        if (this.mTaskPositioner != null) {
            this.mTaskPositioner.setDisplay(activityDisplay.mDisplay);
            this.mTaskPositioner.configure(this.mBounds);
        }
        onParentChanged();
        activityDisplay.attachStack(this, findStackInsertIndex(onTop));
        if (this.mStackId == 3) {
            this.mStackSupervisor.resizeDockedStackLocked(this.mBounds, null, null, null, null, true);
        }
    }

    private void removeFromDisplay() {
        ActivityDisplay display = getDisplay();
        if (display != null) {
            display.detachStack(this);
        }
        this.mDisplayId = -1;
        this.mStacks = null;
        if (this.mTaskPositioner != null) {
            this.mTaskPositioner.reset();
        }
        if (this.mStackId == 3) {
            this.mStackSupervisor.resizeDockedStackLocked(null, null, null, null, null, true);
        }
    }

    void remove() {
        removeFromDisplay();
        this.mStackSupervisor.mStacks.remove(this.mStackId);
        if (this.mWindowContainerController != null) {
            this.mWindowContainerController.removeContainer();
        }
        this.mWindowContainerController = null;
        onParentChanged();
    }

    ActivityDisplay getDisplay() {
        return this.mStackSupervisor.getActivityDisplay(this.mDisplayId);
    }

    void getDisplaySize(Point out) {
        getDisplay().mDisplay.getSize(out);
    }

    void getStackDockedModeBounds(Rect currentTempTaskBounds, Rect outStackBounds, Rect outTempTaskBounds, boolean ignoreVisibility) {
        this.mWindowContainerController.getStackDockedModeBounds(currentTempTaskBounds, outStackBounds, outTempTaskBounds, ignoreVisibility);
    }

    void prepareFreezingTaskBounds() {
        this.mWindowContainerController.prepareFreezingTaskBounds();
    }

    void getWindowContainerBounds(Rect outBounds) {
        if (this.mWindowContainerController != null) {
            this.mWindowContainerController.getBounds(outBounds);
        } else {
            outBounds.setEmpty();
        }
    }

    void getBoundsForNewConfiguration(Rect outBounds) {
        this.mWindowContainerController.getBoundsForNewConfiguration(outBounds);
    }

    void positionChildWindowContainerAtTop(TaskRecord child) {
        this.mWindowContainerController.positionChildAtTop(child.getWindowContainerController(), true);
    }

    boolean deferScheduleMultiWindowModeChanged() {
        return false;
    }

    void deferUpdateBounds() {
        if (!this.mUpdateBoundsDeferred) {
            this.mUpdateBoundsDeferred = true;
            this.mUpdateBoundsDeferredCalled = false;
        }
    }

    void continueUpdateBounds() {
        Rect rect = null;
        boolean wasDeferred = this.mUpdateBoundsDeferred;
        this.mUpdateBoundsDeferred = false;
        if (wasDeferred && this.mUpdateBoundsDeferredCalled) {
            Rect rect2 = this.mDeferredBounds.isEmpty() ? null : this.mDeferredBounds;
            Rect rect3 = this.mDeferredTaskBounds.isEmpty() ? null : this.mDeferredTaskBounds;
            if (!this.mDeferredTaskInsetBounds.isEmpty()) {
                rect = this.mDeferredTaskInsetBounds;
            }
            resize(rect2, rect3, rect);
        }
    }

    boolean updateBoundsAllowed(Rect bounds, Rect tempTaskBounds, Rect tempTaskInsetBounds) {
        if (!this.mUpdateBoundsDeferred) {
            return true;
        }
        if (bounds != null) {
            this.mDeferredBounds.set(bounds);
        } else {
            this.mDeferredBounds.setEmpty();
        }
        if (tempTaskBounds != null) {
            this.mDeferredTaskBounds.set(tempTaskBounds);
        } else {
            this.mDeferredTaskBounds.setEmpty();
        }
        if (tempTaskInsetBounds != null) {
            this.mDeferredTaskInsetBounds.set(tempTaskInsetBounds);
        } else {
            this.mDeferredTaskInsetBounds.setEmpty();
        }
        this.mUpdateBoundsDeferredCalled = true;
        return false;
    }

    void setBounds(Rect bounds) {
        Rect rect = null;
        if (!this.mFullscreen) {
            rect = new Rect(bounds);
        }
        this.mBounds = rect;
        if (this.mTaskPositioner != null) {
            this.mTaskPositioner.configure(bounds);
        }
    }

    ActivityRecord topRunningActivityLocked() {
        return topRunningActivityLocked(false);
    }

    void getAllRunningVisibleActivitiesLocked(ArrayList<ActivityRecord> outActivities) {
        outActivities.clear();
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ((TaskRecord) this.mTaskHistory.get(taskNdx)).getAllRunningVisibleActivitiesLocked(outActivities);
        }
    }

    private ActivityRecord topRunningActivityLocked(boolean focusableOnly) {
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ActivityRecord r = ((TaskRecord) this.mTaskHistory.get(taskNdx)).topRunningActivityLocked();
            if (r != null && (!focusableOnly || r.isFocusable())) {
                return r;
            }
        }
        return null;
    }

    ActivityRecord topRunningNonOverlayTaskActivity() {
        if (ActivityManagerDebugConfig.DEBUG_STATES) {
            Slog.i(TAG, "topRunningActivityLocked: mTaskHistory.size() " + this.mTaskHistory.size() + " mTaskHistory " + this.mTaskHistory + " " + " call by " + Debug.getCallers(8));
        }
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ArrayList<ActivityRecord> activities = ((TaskRecord) this.mTaskHistory.get(taskNdx)).mActivities;
            for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                ActivityRecord r = (ActivityRecord) activities.get(activityNdx);
                if (!r.finishing && (r.mTaskOverlay ^ 1) != 0) {
                    return r;
                }
            }
        }
        return null;
    }

    ActivityRecord topRunningNonDelayedActivityLocked(ActivityRecord notTop) {
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ArrayList<ActivityRecord> activities = ((TaskRecord) this.mTaskHistory.get(taskNdx)).mActivities;
            for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                ActivityRecord r = (ActivityRecord) activities.get(activityNdx);
                if (ActivityManagerDebugConfig.DEBUG_STATES) {
                    Slog.d(TAG, "topRunningActivityLocked: taskNdx " + taskNdx + " mTaskHistory.get(taskNdx) " + this.mTaskHistory.get(taskNdx) + " r " + r);
                }
                if (!r.finishing && (r.delayedResume ^ 1) != 0 && r != notTop && r.okToShowLocked()) {
                    return r;
                }
            }
        }
        return null;
    }

    final ActivityRecord topRunningActivityLocked(IBinder token, int taskId) {
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            TaskRecord task = (TaskRecord) this.mTaskHistory.get(taskNdx);
            if (task.taskId != taskId) {
                ArrayList<ActivityRecord> activities = task.mActivities;
                for (int i = activities.size() - 1; i >= 0; i--) {
                    ActivityRecord r = (ActivityRecord) activities.get(i);
                    if (!r.finishing && token != r.appToken && r.okToShowLocked()) {
                        return r;
                    }
                }
                continue;
            }
        }
        return null;
    }

    final ActivityRecord topActivity() {
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ArrayList<ActivityRecord> activities = ((TaskRecord) this.mTaskHistory.get(taskNdx)).mActivities;
            for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                ActivityRecord r = (ActivityRecord) activities.get(activityNdx);
                if (!r.finishing) {
                    return r;
                }
            }
        }
        return null;
    }

    final TaskRecord topTask() {
        int size = this.mTaskHistory.size();
        if (size > 0) {
            return (TaskRecord) this.mTaskHistory.get(size - 1);
        }
        return null;
    }

    final TaskRecord bottomTask() {
        if (this.mTaskHistory.isEmpty()) {
            return null;
        }
        return (TaskRecord) this.mTaskHistory.get(0);
    }

    TaskRecord taskForIdLocked(int id) {
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            TaskRecord task = (TaskRecord) this.mTaskHistory.get(taskNdx);
            if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                Slog.d(TAG_TASKS, "task.taskId " + task.taskId + " task " + task);
            }
            if (task.taskId == id) {
                return task;
            }
        }
        return null;
    }

    ActivityRecord isInStackLocked(IBinder token) {
        return isInStackLocked(ActivityRecord.forTokenLocked(token));
    }

    ActivityRecord isInStackLocked(ActivityRecord r) {
        if (r == null) {
            return null;
        }
        TaskRecord task = r.getTask();
        ActivityStack stack = r.getStack();
        if (stack == null || !task.mActivities.contains(r) || !this.mTaskHistory.contains(task)) {
            return null;
        }
        if (stack != this) {
            Slog.w(TAG, "Illegal state! task does not point to stack it is in.");
        }
        return r;
    }

    boolean isInStackLocked(TaskRecord task) {
        return this.mTaskHistory.contains(task);
    }

    boolean isUidPresent(int uid) {
        for (TaskRecord task : this.mTaskHistory) {
            for (ActivityRecord r : task.mActivities) {
                if (r.getUid() == uid) {
                    return true;
                }
            }
        }
        return false;
    }

    void getPresentUIDs(IntArray presentUIDs) {
        for (TaskRecord task : this.mTaskHistory) {
            for (ActivityRecord r : task.mActivities) {
                presentUIDs.add(r.getUid());
            }
        }
    }

    final void removeActivitiesFromLRUListLocked(TaskRecord task) {
        for (ActivityRecord r : task.mActivities) {
            this.mLRUActivities.remove(r);
        }
    }

    final boolean updateLRUListLocked(ActivityRecord r) {
        boolean hadit = this.mLRUActivities.remove(r);
        this.mLRUActivities.add(r);
        return hadit;
    }

    final boolean isHomeStack() {
        return this.mStackId == 0;
    }

    final boolean isRecentsStack() {
        return this.mStackId == 5;
    }

    final boolean isHomeOrRecentsStack() {
        return StackId.isHomeOrRecentsStack(this.mStackId);
    }

    final boolean isDockedStack() {
        return this.mStackId == 3;
    }

    final boolean isPinnedStack() {
        return this.mStackId == 4;
    }

    final boolean isAssistantStack() {
        return this.mStackId == 6;
    }

    final boolean isOnHomeDisplay() {
        return isAttached() && this.mDisplayId == 0;
    }

    void moveToFront(String reason) {
        moveToFront(reason, null);
    }

    void moveToFront(String reason, TaskRecord task) {
        if (isAttached()) {
            this.mStacks.remove(this);
            this.mStacks.add(findStackInsertIndex(true), this);
            this.mStackSupervisor.setFocusStackUnchecked(reason, this);
            if (task != null) {
                insertTaskAtTop(task, null);
                return;
            }
            task = topTask();
            if (task != null) {
                this.mWindowContainerController.positionChildAtTop(task.getWindowContainerController(), true);
            }
        }
    }

    private void moveToBack(TaskRecord task) {
        if (isAttached()) {
            this.mStacks.remove(this);
            this.mStacks.add(0, this);
            if (task != null) {
                this.mTaskHistory.remove(task);
                this.mTaskHistory.add(0, task);
                updateTaskMovement(task, false);
                this.mWindowContainerController.positionChildAtBottom(task.getWindowContainerController());
            }
        }
    }

    private int findStackInsertIndex(boolean onTop) {
        if (!onTop) {
            return 0;
        }
        int addIndex = this.mStacks.size();
        if (addIndex > 0) {
            ActivityStack topStack = (ActivityStack) this.mStacks.get(addIndex - 1);
            if ((StackId.isAlwaysOnTop(topStack.mStackId) || (topStack.mStackId == 2 && this.mStackId == 1)) && topStack != this) {
                addIndex--;
            }
        }
        return addIndex;
    }

    boolean isFocusable() {
        if (StackId.canReceiveKeys(this.mStackId)) {
            return true;
        }
        ActivityRecord r = topRunningActivityLocked();
        return r != null ? r.isFocusable() : false;
    }

    final boolean isAttached() {
        return this.mStacks != null;
    }

    void findTaskLocked(ActivityRecord target, FindTaskResult result) {
        Intent intent = target.intent;
        ActivityInfo info = target.info;
        ComponentName cls = intent.getComponent();
        if (info.targetActivity != null) {
            cls = new ComponentName(info.packageName, info.targetActivity);
        }
        int userId = UserHandle.getUserId(info.applicationInfo.uid);
        boolean isDocument = (intent != null ? 1 : 0) & intent.isDocument();
        Object documentData = isDocument ? intent.getData() : null;
        if (intent.getComponent() != null && intent.getComponent().flattenToShortString().equals("com.tencent.mm/.ui.transmit.MsgRetransmitUI")) {
            documentData = null;
            isDocument = false;
        }
        if (ActivityManagerDebugConfig.DEBUG_TASKS) {
            Slog.d(TAG_TASKS, "Looking for task of " + target + " in " + this);
        }
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            TaskRecord task = (TaskRecord) this.mTaskHistory.get(taskNdx);
            if (task.voiceSession != null) {
                if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                    Slog.d(TAG_TASKS, "Skipping " + task + ": voice session");
                }
            } else if (task.userId == userId) {
                ActivityRecord r = task.getTopActivity(false);
                boolean isSkip = true;
                if (r != null) {
                    isSkip = r.userId != userId;
                }
                if (OppoMultiAppManagerUtil.getInstance().isMultiApp(task.affinity) || (r != null && OppoMultiAppManagerUtil.getInstance().isMultiApp(r.packageName))) {
                    isSkip = false;
                }
                if (r != null && !r.finishing && !isSkip && r.launchMode != 3) {
                    if (r.mActivityType == target.mActivityType || (target.shortComponentName.equals("com.oppo.launcher/.Launcher") ^ 1) == 0) {
                        boolean taskIsDocument;
                        Object taskDocumentData;
                        Intent taskIntent = task.intent;
                        Intent affinityIntent = task.affinityIntent;
                        if (taskIntent != null && taskIntent.isDocument()) {
                            taskIsDocument = true;
                            taskDocumentData = taskIntent.getData();
                        } else if (affinityIntent == null || !affinityIntent.isDocument()) {
                            taskIsDocument = false;
                            taskDocumentData = null;
                        } else {
                            taskIsDocument = true;
                            taskDocumentData = affinityIntent.getData();
                        }
                        if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                            Slog.d(TAG_TASKS, "affinityIntent " + affinityIntent + " taskIsDocument " + taskIsDocument + " taskDocumentData " + taskDocumentData + " documentData " + documentData + " isDocument " + isDocument);
                        }
                        if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                            Slog.d(TAG_TASKS, "Comparing existing cls=" + taskIntent.getComponent().flattenToShortString() + "/aff=" + r.getTask().rootAffinity + " to new cls=" + intent.getComponent().flattenToShortString() + "/aff=" + info.taskAffinity);
                        }
                        if (taskIntent != null && taskIntent.getComponent() != null && taskIntent.getComponent().compareTo(cls) == 0 && Objects.equals(documentData, taskDocumentData)) {
                            if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                                Slog.d(TAG_TASKS, "Found matching class!");
                            }
                            if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                                Slog.d(TAG_TASKS, "For Intent " + intent + " bringing to top: " + r.intent);
                            }
                            result.r = r;
                            result.matchedByRootAffinity = false;
                            return;
                        } else if (affinityIntent != null && affinityIntent.getComponent() != null && affinityIntent.getComponent().compareTo(cls) == 0 && Objects.equals(documentData, taskDocumentData)) {
                            if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                                Slog.d(TAG_TASKS, "Found matching class!");
                            }
                            if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                                Slog.d(TAG_TASKS, "For Intent " + intent + " bringing to top: " + r.intent);
                            }
                            result.r = r;
                            result.matchedByRootAffinity = false;
                            return;
                        } else if (!isDocument && (taskIsDocument ^ 1) != 0 && result.r == null && task.rootAffinity != null) {
                            if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                                Slog.d(TAG_TASKS, "task.rootAffinity " + task.rootAffinity + " target.taskAffinity " + target.taskAffinity);
                            }
                            if (task.rootAffinity.equals(target.taskAffinity)) {
                                if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                                    Slog.d(TAG_TASKS, "Found matching affinity candidate!");
                                }
                                result.r = r;
                                result.matchedByRootAffinity = true;
                            }
                        } else if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                            Slog.d(TAG_TASKS, "Not a match: " + task);
                        }
                    } else if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                        Slog.d(TAG_TASKS, "Skipping " + task + ": mismatch activity type");
                    }
                } else if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                    Slog.d(TAG_TASKS, "Skipping " + task + ": mismatch root " + r);
                }
            } else if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                Slog.d(TAG_TASKS, "Skipping " + task + ": different user");
            }
        }
    }

    ActivityRecord findActivityLocked(Intent intent, ActivityInfo info, boolean compareIntentFilters) {
        ComponentName cls = intent.getComponent();
        if (info.targetActivity != null) {
            cls = new ComponentName(info.packageName, info.targetActivity);
        }
        int userId = UserHandle.getUserId(info.applicationInfo.uid);
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ArrayList<ActivityRecord> activities = ((TaskRecord) this.mTaskHistory.get(taskNdx)).mActivities;
            for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                ActivityRecord r = (ActivityRecord) activities.get(activityNdx);
                if (r.okToShowLocked() && !r.finishing && r.userId == userId) {
                    if (compareIntentFilters) {
                        if (r.intent.filterEquals(intent)) {
                            return r;
                        }
                    } else if (r.intent.getComponent().equals(cls)) {
                        return r;
                    }
                }
            }
        }
        return null;
    }

    ActivityRecord findActivityForFreeformLocked(Intent intent, int userId, boolean compareIntentFilters) {
        ComponentName cls = intent.getComponent();
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ArrayList<ActivityRecord> activities = ((TaskRecord) this.mTaskHistory.get(taskNdx)).mActivities;
            for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                ActivityRecord r = (ActivityRecord) activities.get(activityNdx);
                if (!r.finishing && r.userId == userId) {
                    if (!compareIntentFilters) {
                        if (ActivityManagerDebugConfig.DEBUG_STACK) {
                            Slog.v(TAG_STACK, "oppo freeform findActivityForFreeformLocked: r " + r);
                        }
                        if (cls != null && r.intent.getComponent().equals(cls)) {
                            if (ActivityManagerDebugConfig.DEBUG_STACK) {
                                Slog.v(TAG_STACK, "oppo freeform findActivityForFreeformLocked: cls " + cls);
                            }
                            return r;
                        }
                    } else if (r.intent.filterEquals(intent)) {
                        return r;
                    }
                }
            }
        }
        return null;
    }

    TaskRecord findTaskForFreeformLocked(Intent intent, int userId) {
        if (ActivityManagerDebugConfig.DEBUG_STACK) {
            Slog.v(TAG_STACK, "oppo freeform findTaskForFreeformLocked: userId " + userId + " intent = " + intent);
        }
        ComponentName cls = intent.getComponent();
        String action = intent.getAction();
        String pkgName = intent.getPackage();
        Set<String> categories = intent.getCategories();
        if (OppoFreeFormManagerService.getInstance().mDynamicDebug) {
            Slog.v(TAG_STACK, "oppo freeform findTaskForFreeformLocked:  mTaskHistory = " + this.mTaskHistory);
            Slog.v(TAG_STACK, "oppo freeform findTaskForFreeformLocked:  action = " + action);
            Slog.v(TAG_STACK, "oppo freeform findTaskForFreeformLocked:  pkgName = " + pkgName);
            Slog.v(TAG_STACK, "oppo freeform findTaskForFreeformLocked:  categories = " + categories);
        }
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            TaskRecord task = (TaskRecord) this.mTaskHistory.get(taskNdx);
            if (!(task == null || task.intent == null || userId != task.userId)) {
                if (OppoFreeFormManagerService.getInstance().mDynamicDebug) {
                    Slog.v(TAG_STACK, "oppo freeform findTaskForFreeformLocked:  task = " + task);
                }
                ComponentName taskIntentCpn = task.intent.getComponent();
                if (cls != null && taskIntentCpn != null && taskIntentCpn.equals(cls)) {
                    if (ActivityManagerDebugConfig.DEBUG_STACK) {
                        Slog.v(TAG_STACK, "oppo freeform findTaskForFreeformLocked: cls " + cls);
                    }
                    return task;
                } else if (!(action == null || !"android.intent.action.MAIN".equals(action) || categories == null || !categories.contains("android.intent.category.LAUNCHER") || pkgName == null)) {
                    if (OppoFreeFormManagerService.getInstance().mDynamicDebug) {
                        Slog.v(TAG_STACK, "oppo freeform findTaskForFreeformLocked: MAIN task.intent = " + task.intent);
                    }
                    String taskAction = task.intent.getAction();
                    String taskPackage = task.intent.getPackage();
                    String taskAffinity = task.affinity;
                    Set<String> taskCategories = task.intent.getCategories();
                    if (OppoFreeFormManagerService.getInstance().mDynamicDebug) {
                        Slog.v(TAG_STACK, "oppo freeform findTaskForFreeformLocked:  taskAction = " + taskAction);
                        Slog.v(TAG_STACK, "oppo freeform findTaskForFreeformLocked:  taskPackage = " + taskPackage);
                        Slog.v(TAG_STACK, "oppo freeform findTaskForFreeformLocked:  taskAffinity = " + taskAffinity);
                        Slog.v(TAG_STACK, "oppo freeform findTaskForFreeformLocked:  taskCategories = " + taskCategories);
                    }
                    if (taskAction != null && taskAction.equals(action) && taskCategories != null && taskCategories.equals(categories) && ((taskPackage != null && taskPackage.equals(pkgName)) || (taskAffinity != null && taskAffinity.equals(pkgName)))) {
                        Slog.v(TAG_STACK, "oppo freeform findTaskForFreeformLocked: find intent = " + intent + "  task.intent = " + task.intent);
                        return task;
                    }
                }
            }
        }
        return null;
    }

    final void switchUserLocked(int userId) {
        if (this.mCurrentUser != userId) {
            this.mCurrentUser = userId;
            int index = this.mTaskHistory.size();
            int i = 0;
            while (i < index) {
                TaskRecord task = (TaskRecord) this.mTaskHistory.get(i);
                if (task.okToShowLocked()) {
                    if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                        Slog.d(TAG_TASKS, "switchUserLocked: stack=" + getStackId() + " moving " + task + " to top");
                    }
                    this.mTaskHistory.remove(i);
                    this.mTaskHistory.add(task);
                    index--;
                } else {
                    i++;
                }
            }
        }
    }

    void minimalResumeActivityLocked(ActivityRecord r) {
        if (ActivityManagerDebugConfig.DEBUG_STATES) {
            Slog.v(TAG_STATES, "Moving to RESUMED: " + r + " (starting new instance)" + " callers=" + Debug.getCallers(5));
        }
        setResumedActivityLocked(r, "minimalResumeActivityLocked");
        r.completeResumeLocked();
        setLaunchTime(r);
        if (ActivityManagerDebugConfig.DEBUG_SAVED_STATE) {
            Slog.i(TAG_SAVED_STATE, "Launch completed; removing icicle of " + r.icicle);
        }
    }

    void addRecentActivityLocked(ActivityRecord r) {
        if (r != null) {
            TaskRecord task = r.getTask();
            this.mRecentTasks.addLocked(task);
            if (task != null) {
                task.touchActiveTime();
            }
        }
    }

    private void startLaunchTraces(String packageName) {
        if (this.mFullyDrawnStartTime != 0) {
            Trace.asyncTraceEnd(64, "drawing", 0);
        }
        Trace.asyncTraceBegin(64, "launching: " + packageName, 0);
        Trace.asyncTraceBegin(64, "drawing", 0);
    }

    private void stopFullyDrawnTraceIfNeeded() {
        if (this.mFullyDrawnStartTime != 0 && this.mLaunchStartTime == 0) {
            Trace.asyncTraceEnd(64, "drawing", 0);
            this.mFullyDrawnStartTime = 0;
        }
    }

    void setLaunchTime(ActivityRecord r) {
        long uptimeMillis;
        if (r.displayStartTime == 0) {
            uptimeMillis = SystemClock.uptimeMillis();
            r.displayStartTime = uptimeMillis;
            r.fullyDrawnStartTime = uptimeMillis;
            if (this.mLaunchStartTime == 0) {
                startLaunchTraces(r.packageName);
                uptimeMillis = r.displayStartTime;
                this.mFullyDrawnStartTime = uptimeMillis;
                this.mLaunchStartTime = uptimeMillis;
            }
        } else if (this.mLaunchStartTime == 0) {
            startLaunchTraces(r.packageName);
            uptimeMillis = SystemClock.uptimeMillis();
            this.mFullyDrawnStartTime = uptimeMillis;
            this.mLaunchStartTime = uptimeMillis;
        }
    }

    private void clearLaunchTime(ActivityRecord r) {
        if (this.mStackSupervisor.mWaitingActivityLaunched.isEmpty()) {
            r.fullyDrawnStartTime = 0;
            r.displayStartTime = 0;
            return;
        }
        this.mStackSupervisor.removeTimeoutsForActivityLocked(r);
        this.mStackSupervisor.scheduleIdleTimeoutLocked(r);
    }

    void awakeFromSleepingLocked() {
        if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
            Slog.d(TAG, "awakeFromSleep");
        }
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ArrayList<ActivityRecord> activities = ((TaskRecord) this.mTaskHistory.get(taskNdx)).mActivities;
            for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
                    Slog.d(TAG, "awakeFromSleep, act:" + activities.get(activityNdx));
                }
                ((ActivityRecord) activities.get(activityNdx)).setSleeping(false);
            }
        }
        if (this.mPausingActivity != null) {
            Slog.d(TAG, "awakeFromSleepingLocked: previously pausing activity didn't pause");
            activityPausedLocked(this.mPausingActivity.appToken, true);
        }
    }

    void updateActivityApplicationInfoLocked(ApplicationInfo aInfo) {
        String packageName = aInfo.packageName;
        int userId = UserHandle.getUserId(aInfo.uid);
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            List<ActivityRecord> activities = ((TaskRecord) this.mTaskHistory.get(taskNdx)).mActivities;
            for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                ActivityRecord ar = (ActivityRecord) activities.get(activityNdx);
                if (userId == ar.userId && packageName.equals(ar.packageName)) {
                    ar.info.applicationInfo = aInfo;
                }
            }
        }
    }

    void checkReadyForSleep() {
        if (shouldSleepActivities() && goToSleepIfPossible(false)) {
            this.mStackSupervisor.checkReadyForSleepLocked(true);
        }
    }

    boolean goToSleepIfPossible(boolean shuttingDown) {
        boolean shouldSleep = true;
        if (this.mResumedActivity != null) {
            if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
                Slog.v(TAG_PAUSE, "Sleep needs to pause " + this.mResumedActivity);
            }
            if (ActivityManagerDebugConfig.DEBUG_USER_LEAVING) {
                Slog.v(TAG_USER_LEAVING, "Sleep => pause with userLeaving=false");
            }
            if (!this.mStackSupervisor.inResumeTopActivity) {
                startPausingLocked(false, true, null, false, "sleep-request");
            } else if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
                Slog.v(TAG_PAUSE, "In the middle of resuming top activity " + this.mResumedActivity);
            }
            shouldSleep = false;
        } else if (this.mPausingActivity != null) {
            if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
                Slog.v(TAG_PAUSE, "Sleep still waiting to pause " + this.mPausingActivity);
            }
            shouldSleep = false;
        }
        if (!shuttingDown) {
            if (containsActivityFromStack(this.mStackSupervisor.mStoppingActivities)) {
                if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
                    Slog.v(TAG_PAUSE, "Sleep still need to stop " + this.mStackSupervisor.mStoppingActivities.size() + " activities");
                }
                this.mStackSupervisor.scheduleIdleLocked();
                shouldSleep = false;
            }
            if (containsActivityFromStack(this.mStackSupervisor.mGoingToSleepActivities)) {
                if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
                    Slog.v(TAG_PAUSE, "Sleep still need to sleep " + this.mStackSupervisor.mGoingToSleepActivities.size() + " activities");
                }
                shouldSleep = false;
            }
        }
        if (shouldSleep) {
            goToSleep();
        }
        return shouldSleep;
    }

    void goToSleep() {
        ensureActivitiesVisibleLocked(null, 0, false);
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ArrayList<ActivityRecord> activities = ((TaskRecord) this.mTaskHistory.get(taskNdx)).mActivities;
            for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                ActivityRecord r = (ActivityRecord) activities.get(activityNdx);
                if (r.state == ActivityState.STOPPING || r.state == ActivityState.STOPPED || r.state == ActivityState.PAUSED || r.state == ActivityState.PAUSING) {
                    r.setSleeping(true);
                }
            }
        }
    }

    private boolean containsActivityFromStack(List<ActivityRecord> rs) {
        for (ActivityRecord r : rs) {
            if (r.getStack() == this) {
                return true;
            }
        }
        return false;
    }

    private void schedulePauseTimeout(ActivityRecord r) {
        Message msg = this.mHandler.obtainMessage(101);
        msg.obj = r;
        r.pauseTime = SystemClock.uptimeMillis();
        this.mHandler.sendMessageDelayed(msg, 500);
        if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
            Slog.v(TAG_PAUSE, "Waiting for pause to complete...");
        }
    }

    final boolean startPausingLocked(boolean userLeaving, boolean uiSleeping, ActivityRecord resuming, boolean pauseImmediately) {
        return startPausingLocked(userLeaving, uiSleeping, resuming, pauseImmediately, "ohter-request");
    }

    final boolean startPausingLocked(boolean userLeaving, boolean uiSleeping, ActivityRecord resuming, boolean pauseImmediately, String reason) {
        if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
            RuntimeException trace = new RuntimeException();
            trace.fillInStackTrace();
            Slog.d(TAG, "startPausingLocked, reason:" + reason, trace);
        }
        if (this.mPausingActivity != null) {
            Slog.e(TAG, "Going to pause when pause is already pending for " + this.mPausingActivity + " state=" + this.mPausingActivity.state, new RuntimeException("here").fillInStackTrace());
            completePauseLocked(false, resuming);
        }
        ActivityRecord prev = this.mResumedActivity;
        if (prev == null) {
            if (resuming == null) {
                Slog.wtf(TAG, "Trying to pause when nothing is resumed");
                this.mStackSupervisor.resumeFocusedStackTopActivityLocked();
            }
            return false;
        }
        if (ActivityManagerDebugConfig.DEBUG_STATES) {
            Slog.v(TAG_STATES, "Moving to PAUSING: " + prev);
        } else if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
            Slog.v(TAG_PAUSE, "Start pausing: " + prev);
        }
        if (mActivityTrigger != null) {
            mActivityTrigger.activityPauseTrigger(prev.intent, prev.info, prev.appInfo);
        }
        this.mResumedActivity = null;
        this.mPausingActivity = prev;
        this.mLastPausedActivity = prev;
        ActivityRecord activityRecord = ((prev.intent.getFlags() & 1073741824) == 0 && (prev.info.flags & 128) == 0) ? null : prev;
        this.mLastNoHistoryActivity = activityRecord;
        prev.state = ActivityState.PAUSING;
        prev.getTask().touchActiveTime();
        clearLaunchTime(prev);
        ActivityRecord next = this.mStackSupervisor.topRunningActivityLocked();
        if (this.mService.mHasRecents && (next == null || next.noDisplay || next.getTask() != prev.getTask() || uiSleeping)) {
            prev.mUpdateTaskThumbnailWhenHidden = true;
            if (this.mFullscreen && prev != null && prev.getConfiguration() != null && prev.getConfiguration().screenWidthDp > prev.getConfiguration().screenHeightDp) {
                prev.oppoSnapShot();
            }
        }
        stopFullyDrawnTraceIfNeeded();
        this.mService.updateCpuStats();
        if (prev.app == null || prev.app.thread == null) {
            this.mPausingActivity = null;
            this.mLastPausedActivity = null;
            this.mLastNoHistoryActivity = null;
        } else {
            if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
                Slog.v(TAG_PAUSE, "Enqueueing pending pause: " + prev);
            }
            try {
                EventLog.writeEvent(EventLogTags.AM_PAUSE_ACTIVITY, new Object[]{Integer.valueOf(prev.userId), Integer.valueOf(System.identityHashCode(prev)), prev.shortComponentName, reason});
                this.mService.updateUsageStats(prev, false);
                prev.app.thread.schedulePauseActivity(prev.appToken, prev.finishing, userLeaving, prev.configChangeFlags, pauseImmediately);
            } catch (Exception e) {
                Slog.w(TAG, "Exception thrown during pause", e);
                this.mPausingActivity = null;
                this.mLastPausedActivity = null;
                this.mLastNoHistoryActivity = null;
            }
        }
        if (!(uiSleeping || (this.mService.isSleepingOrShuttingDownLocked() ^ 1) == 0)) {
            this.mStackSupervisor.acquireLaunchWakelock();
        }
        if (this.mPausingActivity != null) {
            if (!uiSleeping) {
                prev.pauseKeyDispatchingLocked();
            } else if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
                Slog.v(TAG_PAUSE, "Key dispatch not paused for screen off");
            }
            if (this.mStackId == 2 && resuming != null) {
                int resumeId = resuming.getStackId();
                if (resumeId == 0 || resumeId == 5) {
                    this.mPausingActivity.mShouldPause = true;
                }
            }
            if (pauseImmediately) {
                completePauseLocked(false, resuming);
                return false;
            }
            schedulePauseTimeout(prev);
            return true;
        }
        if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
            Slog.v(TAG_PAUSE, "Activity not running, resuming next.");
        }
        if (resuming == null) {
            this.mStackSupervisor.resumeFocusedStackTopActivityLocked();
        }
        return false;
    }

    final void activityPausedLocked(IBinder token, boolean timeout) {
        if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
            Slog.v(TAG_PAUSE, "Activity paused: token=" + token + ", timeout=" + timeout);
        }
        ActivityRecord r = isInStackLocked(token);
        if (r != null) {
            this.mHandler.removeMessages(101, r);
            if (this.mPausingActivity == r) {
                if (ActivityManagerDebugConfig.DEBUG_STATES) {
                    Slog.v(TAG_STATES, "Moving to PAUSED: " + r + (timeout ? " (due to timeout)" : " (pause complete)"));
                }
                this.mService.mWindowManager.deferSurfaceLayout();
                try {
                    completePauseLocked(true, null);
                    return;
                } finally {
                    this.mService.mWindowManager.continueSurfaceLayout();
                }
            } else {
                Object[] objArr = new Object[4];
                objArr[0] = Integer.valueOf(r.userId);
                objArr[1] = Integer.valueOf(System.identityHashCode(r));
                objArr[2] = r.shortComponentName;
                objArr[3] = this.mPausingActivity != null ? this.mPausingActivity.shortComponentName : "(none)";
                EventLog.writeEvent(EventLogTags.AM_FAILED_TO_PAUSE, objArr);
                if (r.state == ActivityState.PAUSING) {
                    r.state = ActivityState.PAUSED;
                    if (r.finishing) {
                        if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
                            Slog.v(TAG, "Executing finish of failed to pause activity: " + r);
                        }
                        finishCurrentActivityLocked(r, 2, false);
                    }
                }
            }
        } else if (timeout && this.mPausingActivity != null) {
            if (ActivityManagerDebugConfig.DEBUG_STATES) {
                Slog.v(TAG_STATES, "mPausingActivity is not null when timeout,call completePauseLocked");
            }
            completePauseLocked(false, null);
        }
        this.mStackSupervisor.ensureActivitiesVisibleLocked(null, 0, false);
    }

    private void completePauseLocked(boolean resumeNext, ActivityRecord resuming) {
        ActivityRecord prev = this.mPausingActivity;
        if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
            Slog.v(TAG_PAUSE, "Complete pause: " + prev);
        }
        if (prev != null) {
            boolean wasStopping = prev.state == ActivityState.STOPPING;
            prev.state = ActivityState.PAUSED;
            if (prev.finishing) {
                if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
                    Slog.v(TAG_PAUSE, "Executing finish of activity: " + prev);
                }
                prev = finishCurrentActivityLocked(prev, 2, false);
            } else if (prev.app != null) {
                this.mPausingActivity = null;
                if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
                    Slog.v(TAG_PAUSE, "Enqueue pending stop if needed: " + prev + " wasStopping=" + wasStopping + " visible=" + prev.visible);
                }
                if (this.mStackSupervisor.mActivitiesWaitingForVisibleActivity.remove(prev) && (ActivityManagerDebugConfig.DEBUG_SWITCH || ActivityManagerDebugConfig.DEBUG_PAUSE)) {
                    Slog.v(TAG_PAUSE, "Complete pause, no longer waiting: " + prev);
                }
                if (prev.deferRelaunchUntilPaused) {
                    if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
                        Slog.v(TAG_PAUSE, "Re-launching after pause: " + prev);
                    }
                    prev.relaunchActivityLocked(false, prev.preserveWindowOnDeferredRelaunch);
                } else if (wasStopping) {
                    prev.state = ActivityState.STOPPING;
                } else if (!prev.visible || shouldSleepOrShutDownActivities()) {
                    prev.setDeferHidingClient(false);
                    addToStopping(prev, true, false);
                }
            } else {
                if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
                    Slog.v(TAG_PAUSE, "App died during pause, not stopping: " + prev);
                }
                prev = null;
            }
            if (prev != null) {
                prev.stopFreezingScreenLocked(true);
            }
            if (this.mPausingActivity != null) {
                this.mPausingActivity = null;
            }
        }
        if (resumeNext) {
            ActivityStack topStack = this.mStackSupervisor.getFocusedStack();
            if (topStack.shouldSleepOrShutDownActivities()) {
                if (!this.mService.mIgnoreSleepCheckLater) {
                    checkReadyForSleep();
                } else if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
                    Slog.v(TAG, "resumeNext, mIgnoreSleepCheckLater, do not care.");
                }
                ActivityRecord top = topStack.topRunningActivityLocked();
                if (top == null || !(prev == null || top == prev)) {
                    this.mStackSupervisor.resumeFocusedStackTopActivityLocked();
                }
            } else {
                if (this.mStackId == 2 && topStack != null && topStack.mStackId == 1 && prev != null && prev.finishing && (prev.nowVisible || !(prev.nowVisible || (this.mHasRunningActivity ^ 1) == 0))) {
                    this.mStackSupervisor.moveTasksToFullscreenStackLocked(this.mStackId, false);
                    if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
                        Slog.v(TAG, "oppo freeform moveTaskToFullscreen topStack: " + topStack);
                    }
                }
                this.mStackSupervisor.resumeFocusedStackTopActivityLocked(topStack, prev, null);
                if (!(topStack == null || topStack.mStackId != 3 || prev == null || prev.shortComponentName == null || !prev.shortComponentName.contains("com.coloros.safecenter/.privacy.view.password"))) {
                    this.mWindowManager.showRecentApps(true);
                }
            }
        }
        if (prev != null) {
            prev.resumeKeyDispatchingLocked();
            if (prev.app != null && prev.cpuTimeAtResume > 0 && this.mService.mBatteryStatsService.isOnBattery()) {
                long diff = this.mService.mProcessCpuTracker.getCpuTimeForPid(prev.app.pid) - prev.cpuTimeAtResume;
                if (diff > 0) {
                    BatteryStatsImpl bsi = this.mService.mBatteryStatsService.getActiveStatistics();
                    synchronized (bsi) {
                        Proc ps = bsi.getProcessStatsLocked(prev.info.applicationInfo.uid, prev.info.packageName);
                        if (ps != null) {
                            ps.addForegroundTimeLocked(diff);
                        }
                    }
                }
            }
            prev.cpuTimeAtResume = 0;
        }
        if (this.mStackSupervisor.mAppVisibilitiesChangedSinceLastPause || this.mService.mStackSupervisor.getStack(4) != null) {
            this.mService.mTaskChangeNotificationController.notifyTaskStackChanged();
            this.mStackSupervisor.mAppVisibilitiesChangedSinceLastPause = false;
        }
        this.mStackSupervisor.ensureActivitiesVisibleLocked(resuming, 0, false);
    }

    void addToStopping(ActivityRecord r, boolean scheduleIdle, boolean idleDelayed) {
        if (!this.mStackSupervisor.mStoppingActivities.contains(r)) {
            this.mStackSupervisor.mStoppingActivities.add(r);
        }
        boolean forceIdle = this.mStackSupervisor.mStoppingActivities.size() <= 3 ? r.frontOfTask && this.mTaskHistory.size() <= 1 : true;
        if (scheduleIdle || forceIdle) {
            if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
                Slog.v(TAG_PAUSE, "Scheduling idle now: forceIdle=" + forceIdle + "immediate=" + (idleDelayed ^ 1));
            }
            if (idleDelayed) {
                this.mStackSupervisor.scheduleIdleTimeoutLocked(r);
            } else {
                this.mStackSupervisor.scheduleIdleLocked();
            }
        } else if (!this.mService.mIgnoreSleepCheckLater) {
            checkReadyForSleep();
        } else if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
            Slog.v(TAG, "resumeNext, mIgnoreSleepCheckLater, do not care.");
        }
    }

    ActivityRecord findNextTranslucentActivity(ActivityRecord r) {
        TaskRecord task = r.getTask();
        if (task == null) {
            return null;
        }
        ActivityStack stack = task.getStack();
        if (stack == null) {
            return null;
        }
        int taskNdx = stack.mTaskHistory.indexOf(task);
        int activityNdx = task.mActivities.indexOf(r) + 1;
        int numStacks = this.mStacks.size();
        for (int stackNdx = this.mStacks.indexOf(stack); stackNdx < numStacks; stackNdx++) {
            ActivityStack historyStack = (ActivityStack) this.mStacks.get(stackNdx);
            ArrayList<TaskRecord> tasks = historyStack.mTaskHistory;
            int numTasks = tasks.size();
            for (taskNdx = 
/*
Method generation error in method: com.android.server.am.ActivityStack.findNextTranslucentActivity(com.android.server.am.ActivityRecord):com.android.server.am.ActivityRecord, dex: 
jadx.core.utils.exceptions.CodegenException: Error generate insn: PHI: (r11_1 'taskNdx' int) = (r11_0 'taskNdx' int), (r11_4 'taskNdx' int) binds: {(r11_0 'taskNdx' int)=B:8:0x0010, (r11_4 'taskNdx' int)=B:26:0x0069} in method: com.android.server.am.ActivityStack.findNextTranslucentActivity(com.android.server.am.ActivityRecord):com.android.server.am.ActivityRecord, dex: 
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:228)
	at jadx.core.codegen.RegionGen.makeLoop(RegionGen.java:183)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:61)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:93)
	at jadx.core.codegen.RegionGen.makeLoop(RegionGen.java:189)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:61)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:173)
	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:321)
	at jadx.core.codegen.ClassGen.addMethods(ClassGen.java:259)
	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:221)
	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:111)
	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:77)
	at jadx.core.codegen.CodeGen.visit(CodeGen.java:10)
	at jadx.core.ProcessClass.process(ProcessClass.java:38)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
Caused by: jadx.core.utils.exceptions.CodegenException: PHI can be used only in fallback mode
	at jadx.core.codegen.InsnGen.fallbackOnlyInsn(InsnGen.java:539)
	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:511)
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:222)
	... 28 more

*/

    private boolean hasFullscreenTask() {
        for (int i = this.mTaskHistory.size() - 1; i >= 0; i--) {
            if (((TaskRecord) this.mTaskHistory.get(i)).mFullscreen) {
                return true;
            }
        }
        return false;
    }

    private boolean isStackTranslucent(ActivityRecord starting, int stackBehindId) {
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            TaskRecord task = (TaskRecord) this.mTaskHistory.get(taskNdx);
            ArrayList<ActivityRecord> activities = task.mActivities;
            for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                ActivityRecord r = (ActivityRecord) activities.get(activityNdx);
                if (!r.finishing && (r.visible || r == starting)) {
                    if (r.fullscreen) {
                        return false;
                    }
                    if (!(isHomeOrRecentsStack() || !r.frontOfTask || !task.isOverHomeStack() || (StackId.isHomeOrRecentsStack(stackBehindId) ^ 1) == 0 || (isAssistantStack() ^ 1) == 0)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    boolean isVisible() {
        if (this.mWindowContainerController == null || !this.mWindowContainerController.isVisible()) {
            return false;
        }
        return this.mForceHidden ^ 1;
    }

    int shouldBeVisible(ActivityRecord starting) {
        int i = 1;
        if (!isAttached() || this.mForceHidden) {
            return 0;
        }
        if (this.mStackSupervisor.isFrontStackOnDisplay(this) || this.mStackSupervisor.isFocusedStack(this)) {
            return 1;
        }
        int stackIndex = this.mStacks.indexOf(this);
        if (stackIndex == this.mStacks.size() - 1) {
            Slog.wtf(TAG, "Stack=" + this + " isn't front stack but is at the top of the stack list");
            return 0;
        }
        ActivityStack topStack = getTopStackOnDisplay();
        int topStackId = topStack.mStackId;
        if (this.mStackId == 3) {
            if (!topStack.isAssistantStack()) {
                return (topStack.mStackId == 1 && topStack.mFullscreen && topStack.mBounds == null) ? 0 : 1;
            } else {
                return topStack.isStackTranslucent(starting, 3) ? 1 : 0;
            }
        } else if (this.mStackId == 2 && topStack.mStackId == 5) {
            return 0;
        } else {
            if (this.mStackId == 2 && topStack.mStackId != 0) {
                return 1;
            }
            if (this.mStackId == 0) {
                int dockedStackIndex = this.mStacks.indexOf(this.mStackSupervisor.getStack(3));
                if (dockedStackIndex > stackIndex && stackIndex != dockedStackIndex - 1) {
                    return 0;
                }
            }
            int stackBehindTopIndex = this.mStacks.indexOf(topStack) - 1;
            while (stackBehindTopIndex >= 0 && topStackId != 3 && ((ActivityStack) this.mStacks.get(stackBehindTopIndex)).topRunningActivityLocked() == null) {
                stackBehindTopIndex--;
            }
            int stackBehindTopId = stackBehindTopIndex >= 0 ? ((ActivityStack) this.mStacks.get(stackBehindTopIndex)).mStackId : -1;
            if (topStackId == 3 || StackId.isAlwaysOnTop(topStackId) || topStackId == 2) {
                if (stackIndex == stackBehindTopIndex) {
                    return 1;
                }
                if (StackId.isAlwaysOnTop(topStackId) && stackIndex == stackBehindTopIndex - 1) {
                    if (stackBehindTopId == 3) {
                        return 1;
                    }
                    if (stackBehindTopId == 6) {
                        if (!((ActivityStack) this.mStacks.get(stackBehindTopIndex)).isStackTranslucent(starting, this.mStackId)) {
                            i = 0;
                        }
                        return i;
                    }
                }
            }
            if (StackId.isBackdropToTranslucentActivity(topStackId) && topStack.isStackTranslucent(starting, stackBehindTopId)) {
                if (stackIndex == stackBehindTopIndex) {
                    return 1;
                }
                if (stackBehindTopIndex >= 0 && ((stackBehindTopId == 3 || stackBehindTopId == 4) && stackIndex == stackBehindTopIndex - 1)) {
                    return 1;
                }
            }
            if (StackId.isStaticStack(this.mStackId)) {
                return 0;
            }
            for (int i2 = stackIndex + 1; i2 < this.mStacks.size(); i2++) {
                ActivityStack stack = (ActivityStack) this.mStacks.get(i2);
                if ((stack.mFullscreen || (stack.hasFullscreenTask() ^ 1) == 0) && (!StackId.isDynamicStacksVisibleBehindAllowed(stack.mStackId) || !stack.isStackTranslucent(starting, -1))) {
                    return 0;
                }
            }
            return 1;
        }
    }

    final int rankTaskLayers(int baseLayer) {
        int taskNdx = this.mTaskHistory.size() - 1;
        int layer = 0;
        while (taskNdx >= 0) {
            int layer2;
            TaskRecord task = (TaskRecord) this.mTaskHistory.get(taskNdx);
            ActivityRecord r = task.topRunningActivityLocked();
            if (r == null || r.finishing || (r.visible ^ 1) != 0) {
                task.mLayerRank = -1;
                layer2 = layer;
            } else {
                layer2 = layer + 1;
                task.mLayerRank = baseLayer + layer;
            }
            taskNdx--;
            layer = layer2;
        }
        return layer;
    }

    final void ensureActivitiesVisibleLocked(ActivityRecord starting, int configChanges, boolean preserveWindows) {
        this.mTopActivityOccludesKeyguard = false;
        this.mTopDismissingKeyguardActivity = null;
        this.mStackSupervisor.mKeyguardController.beginActivityVisibilityUpdate();
        try {
            this.mClosedSuperFirewall = AppGlobals.getPackageManager().isClosedSuperFirewall();
        } catch (RemoteException e) {
        }
        try {
            ActivityRecord top = topRunningActivityLocked();
            if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
                Slog.v(TAG_VISIBILITY, "ensureActivitiesVisible behind " + top + " starting " + starting + " configChanges=0x" + Integer.toHexString(configChanges));
            }
            if (top != null) {
                checkTranslucentActivityWaiting(top);
            }
            boolean aboveTop = top != null;
            int stackVisibility = shouldBeVisible(starting);
            boolean stackInvisible = stackVisibility != 1;
            boolean behindFullscreenActivity = stackInvisible;
            boolean resumeNextActivity = this.mStackSupervisor.isFocusedStack(this) ? isInStackLocked(starting) == null : false;
            for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
                TaskRecord task = (TaskRecord) this.mTaskHistory.get(taskNdx);
                ArrayList<ActivityRecord> activities = task.mActivities;
                int activityNdx = activities.size() - 1;
                while (activityNdx >= 0) {
                    ActivityRecord r = (ActivityRecord) activities.get(activityNdx);
                    if (!r.finishing) {
                        boolean isTop = r == top;
                        if (!aboveTop || (isTop ^ 1) == 0) {
                            aboveTop = false;
                            boolean visibleIgnoringKeyguard = r.shouldBeVisibleIgnoringKeyguard(behindFullscreenActivity);
                            r.visibleIgnoringKeyguard = visibleIgnoringKeyguard;
                            boolean reallyVisible = checkKeyguardVisibility(r, visibleIgnoringKeyguard, isTop);
                            if (visibleIgnoringKeyguard) {
                                behindFullscreenActivity = updateBehindFullscreen(stackInvisible, behindFullscreenActivity, task, r);
                                if (behindFullscreenActivity && (r.fullscreen ^ 1) != 0) {
                                }
                            }
                            if (reallyVisible) {
                                if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
                                    Slog.v(TAG_VISIBILITY, "Make visible? " + r + " finishing=" + r.finishing + " state=" + r.state);
                                }
                                if (r != starting) {
                                    r.ensureActivityConfigurationLocked(0, preserveWindows);
                                }
                                if (r.app == null || r.app.thread == null) {
                                    if (makeVisibleAndRestartIfNeeded(starting, configChanges, isTop, resumeNextActivity, r)) {
                                        if (activityNdx >= activities.size()) {
                                            activityNdx = activities.size() - 1;
                                        } else {
                                            resumeNextActivity = false;
                                        }
                                    }
                                } else if (r.visible) {
                                    if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
                                        Slog.v(TAG_VISIBILITY, "Skipping: already visible at " + r);
                                    }
                                    if (r.handleAlreadyVisible()) {
                                        resumeNextActivity = false;
                                    }
                                } else {
                                    r.makeVisibleIfNeeded(starting);
                                }
                                configChanges |= r.configChangeFlags;
                            } else {
                                if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
                                    Slog.v(TAG_VISIBILITY, "Make invisible? " + r + " finishing=" + r.finishing + " state=" + r.state + " stackInvisible=" + stackInvisible + " behindFullscreenActivity=" + behindFullscreenActivity + " mLaunchTaskBehind=" + r.mLaunchTaskBehind);
                                }
                                makeInvisible(r);
                            }
                        }
                    } else if (r.mUpdateTaskThumbnailWhenHidden) {
                        r.updateThumbnailLocked(r.screenshotActivityLocked(), null);
                        r.mUpdateTaskThumbnailWhenHidden = false;
                    }
                    activityNdx--;
                }
                if (this.mStackId == 2) {
                    if (stackVisibility == 0) {
                        behindFullscreenActivity = true;
                    } else {
                        behindFullscreenActivity = false;
                    }
                } else if (this.mStackId == 0) {
                    if (task.isHomeTask()) {
                        if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
                            Slog.v(TAG_VISIBILITY, "Home task: at " + task + " stackInvisible=" + stackInvisible + " behindFullscreenActivity=" + behindFullscreenActivity);
                        }
                        behindFullscreenActivity = true;
                    } else if (task.isRecentsTask() && task.getTaskToReturnTo() == 0) {
                        if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
                            Slog.v(TAG_VISIBILITY, "Recents task returning to app: at " + task + " stackInvisible=" + stackInvisible + " behindFullscreenActivity=" + behindFullscreenActivity);
                        }
                        behindFullscreenActivity = true;
                    }
                } else if (this.mStackId == 1) {
                    if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
                        Slog.v(TAG_VISIBILITY, "Skipping after task=" + task + " returning to non-application type=" + task.getTaskToReturnTo());
                    }
                    if (!(task.topRunningActivityLocked() == null || task.getTaskToReturnTo() == 0)) {
                        behindFullscreenActivity = true;
                    }
                }
            }
            if (this.mTranslucentActivityWaiting != null && this.mUndrawnActivitiesBelowTopTranslucent.isEmpty()) {
                notifyActivityDrawnLocked(null);
            }
            this.mStackSupervisor.mKeyguardController.endActivityVisibilityUpdate();
        } catch (Throwable th) {
            this.mStackSupervisor.mKeyguardController.endActivityVisibilityUpdate();
        }
    }

    void addStartingWindowsForVisibleActivities(boolean taskSwitch) {
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ((TaskRecord) this.mTaskHistory.get(taskNdx)).addStartingWindowsForVisibleActivities(taskSwitch);
        }
    }

    boolean topActivityOccludesKeyguard() {
        return this.mTopActivityOccludesKeyguard;
    }

    ActivityRecord getTopDismissingKeyguardActivity() {
        return this.mTopDismissingKeyguardActivity;
    }

    boolean checkKeyguardVisibility(ActivityRecord r, boolean shouldBeVisible, boolean isTop) {
        int i;
        boolean z = false;
        boolean isInPinnedStack = r.getStack().getStackId() == 4;
        KeyguardController keyguardController = this.mStackSupervisor.mKeyguardController;
        if (this.mDisplayId != -1) {
            i = this.mDisplayId;
        } else {
            i = 0;
        }
        boolean keyguardShowing = keyguardController.isKeyguardShowing(i);
        boolean keyguardLocked = this.mStackSupervisor.mKeyguardController.isKeyguardLocked();
        boolean showWhenLocked = r.canShowWhenLocked() ? isInPinnedStack ^ 1 : false;
        boolean dismissKeyguard = r.hasDismissKeyguardWindows();
        if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
            Slog.d(TAG, "checkKeyguardVisibility r:" + r + " shouldBeVisible:" + shouldBeVisible + " isTop:" + isTop + " isInPinnedStack:" + isInPinnedStack + " keyguardShowing:" + keyguardShowing + " keyguardLocked:" + keyguardLocked + " showWhenLocked:" + showWhenLocked + " dismissKeyguard:" + dismissKeyguard);
        }
        if (!this.mClosedSuperFirewall && this.mService.mFinishWhenLockedState == 1 && keyguardLocked && !ActivityThread.inCptWhiteList(CompatibilityHelper.FORCE_VISIBLE_WHEN_BACK_TO_KEYGUARD, r.shortComponentName)) {
            if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
                Slog.d(TAG, "checkKeyguardVisibility return for state 1");
            }
            return false;
        } else if (this.mClosedSuperFirewall || this.mService.mFinishWhenLockedState != 2) {
            if (shouldBeVisible) {
                boolean canShowWithKeyguard;
                if (dismissKeyguard && this.mTopDismissingKeyguardActivity == null) {
                    this.mTopDismissingKeyguardActivity = r;
                }
                if (isTop) {
                    this.mTopActivityOccludesKeyguard |= showWhenLocked;
                }
                if (canShowWithInsecureKeyguard()) {
                    canShowWithKeyguard = this.mStackSupervisor.mKeyguardController.canDismissKeyguard();
                } else {
                    canShowWithKeyguard = false;
                }
                if (canShowWithKeyguard) {
                    if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
                        Slog.d(TAG, "checkKeyguardVisibility return for state 3");
                    }
                    return true;
                }
            }
            if (keyguardShowing) {
                if (!(this.mClosedSuperFirewall || !r.visible || r.realActivity == null || r.realActivity.getClassName() == null || (r.realActivity.getClassName().contains(ActivityRecord.COLOROS_RECENTS_PACKAGE_NAME) ^ 1) == 0 || r.packageName == null)) {
                    if (r.packageName.equals("com.oppo.reader") || r.packageName.equals("com.et.market") || r.packageName.equals("com.appon.worldofcricket") || r.packageName.equals("jp.shopping7net.shopapp_7net") || r.packageName.equals("com.balaji.alt") || r.packageName.equals("com.tencent.ig") || r.packageName.equals("com.tencent.tmgp.sgame")) {
                        i = 1;
                    } else {
                        i = r.packageName.equals("com.mediocre.smashhit");
                    }
                    if ((i ^ 1) != 0) {
                        if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
                            Slog.d(TAG, "checkKeyguardVisibility return for state 4");
                        }
                        return true;
                    }
                }
                if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
                    Slog.d(TAG, "checkKeyguardVisibility return for state 5");
                }
                if (shouldBeVisible) {
                    z = this.mStackSupervisor.mKeyguardController.canShowActivityWhileKeyguardShowing(r, dismissKeyguard);
                }
                return z;
            } else if (!keyguardLocked) {
                if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
                    Slog.d(TAG, "checkKeyguardVisibility return for state 8");
                }
                return shouldBeVisible;
            } else if (!this.mClosedSuperFirewall && isTop && r.visible && (r.shortComponentName.equals("com.coloros.alarmclock/.AlarmClock") || r.shortComponentName.equals("com.android.calculator2/.Calculator") || r.shortComponentName.equals("com.oppo.camera/.Camera"))) {
                if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
                    Slog.d(TAG, "checkKeyguardVisibility return for state 6");
                }
                return true;
            } else {
                if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
                    Slog.d(TAG, "checkKeyguardVisibility return for state 7");
                }
                if (shouldBeVisible) {
                    z = this.mStackSupervisor.mKeyguardController.canShowWhileOccluded(dismissKeyguard, showWhenLocked);
                }
                return z;
            }
        } else {
            if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
                Slog.d(TAG, "checkKeyguardVisibility return for state 2");
            }
            return shouldBeVisible;
        }
    }

    private boolean canShowWithInsecureKeyguard() {
        ActivityDisplay activityDisplay = getDisplay();
        if (activityDisplay == null) {
            throw new IllegalStateException("Stack is not attached to any display, stackId=" + this.mStackId);
        } else if ((activityDisplay.mDisplay.getFlags() & 32) != 0) {
            return true;
        } else {
            return false;
        }
    }

    private void checkTranslucentActivityWaiting(ActivityRecord top) {
        if (this.mTranslucentActivityWaiting != top) {
            this.mUndrawnActivitiesBelowTopTranslucent.clear();
            if (this.mTranslucentActivityWaiting != null) {
                notifyActivityDrawnLocked(null);
                this.mTranslucentActivityWaiting = null;
            }
            this.mHandler.removeMessages(106);
        }
    }

    private boolean makeVisibleAndRestartIfNeeded(ActivityRecord starting, int configChanges, boolean isTop, boolean andResume, ActivityRecord r) {
        if (isTop || (r.visible ^ 1) != 0) {
            if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
                Slog.v(TAG_VISIBILITY, "Start and freeze screen for " + r);
            }
            if (r != starting) {
                r.startFreezingScreenLocked(r.app, configChanges);
            }
            if (!(r.visible && !r.mLaunchTaskBehind && r == starting)) {
                if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
                    Slog.v(TAG_VISIBILITY, "Starting and making visible: " + r);
                }
                r.setVisible(true);
            }
            if (r != starting) {
                if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
                    Slog.v(TAG_VISIBILITY, "Starting activity again " + r);
                }
                this.mStackSupervisor.startSpecificActivityLocked(r, andResume, false);
                return true;
            }
        }
        return false;
    }

    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void makeInvisible(ActivityRecord r) {
        if (r.visible) {
            if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
                Slog.v(TAG_VISIBILITY, "Making invisible: " + r + " " + r.state);
            }
            try {
                boolean canEnterPictureInPicture = r.checkEnterPictureInPictureState("makeInvisible", true);
                boolean deferHidingClient = (!canEnterPictureInPicture || r.state == ActivityState.STOPPING || r.state == ActivityState.STOPPED) ? false : r.state != ActivityState.PAUSED;
                r.setDeferHidingClient(deferHidingClient);
                r.setVisible(false);
                switch (m33x775af271()[r.state.ordinal()]) {
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                        addToStopping(r, true, canEnterPictureInPicture);
                        break;
                    case 5:
                    case 6:
                        if (!(r.app == null || r.app.thread == null)) {
                            if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
                                Slog.v(TAG_VISIBILITY, "Scheduling invisibility: " + r);
                            }
                            r.app.thread.scheduleWindowVisibility(r.appToken, false);
                        }
                        r.supportsEnterPipOnTaskSwitch = false;
                        break;
                }
            } catch (Exception e) {
                Slog.w(TAG, "Exception thrown making hidden: " + r.intent.getComponent(), e);
            }
            return;
        }
        if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
            Slog.v(TAG_VISIBILITY, "Already invisible: " + r);
        }
    }

    private boolean updateBehindFullscreen(boolean stackInvisible, boolean behindFullscreenActivity, TaskRecord task, ActivityRecord r) {
        if (r.fullscreen) {
            if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
                Slog.v(TAG_VISIBILITY, "Fullscreen: at " + r + " stackInvisible=" + stackInvisible + " behindFullscreenActivity=" + behindFullscreenActivity);
            }
            return true;
        } else if (isHomeOrRecentsStack() || !r.frontOfTask || !task.isOverHomeStack()) {
            return behindFullscreenActivity;
        } else {
            if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
                Slog.v(TAG_VISIBILITY, "Showing home: at " + r + " stackInvisible=" + stackInvisible + " behindFullscreenActivity=" + behindFullscreenActivity);
            }
            return true;
        }
    }

    void convertActivityToTranslucent(ActivityRecord r) {
        this.mTranslucentActivityWaiting = r;
        this.mUndrawnActivitiesBelowTopTranslucent.clear();
        this.mHandler.sendEmptyMessageDelayed(106, TRANSLUCENT_CONVERSION_TIMEOUT);
    }

    void clearOtherAppTimeTrackers(AppTimeTracker except) {
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ArrayList<ActivityRecord> activities = ((TaskRecord) this.mTaskHistory.get(taskNdx)).mActivities;
            for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                ActivityRecord r = (ActivityRecord) activities.get(activityNdx);
                if (r.appTimeTracker != except) {
                    r.appTimeTracker = null;
                }
            }
        }
    }

    void notifyActivityDrawnLocked(ActivityRecord r) {
        boolean z = false;
        if (r == null || (this.mUndrawnActivitiesBelowTopTranslucent.remove(r) && this.mUndrawnActivitiesBelowTopTranslucent.isEmpty())) {
            ActivityRecord waitingActivity = this.mTranslucentActivityWaiting;
            this.mTranslucentActivityWaiting = null;
            this.mUndrawnActivitiesBelowTopTranslucent.clear();
            this.mHandler.removeMessages(106);
            if (waitingActivity != null) {
                this.mWindowManager.setWindowOpaque(waitingActivity.appToken, false);
                if (waitingActivity.app != null && waitingActivity.app.thread != null) {
                    try {
                        IApplicationThread iApplicationThread = waitingActivity.app.thread;
                        IBinder iBinder = waitingActivity.appToken;
                        if (r != null) {
                            z = true;
                        }
                        iApplicationThread.scheduleTranslucentConversionComplete(iBinder, z);
                    } catch (RemoteException e) {
                    }
                }
            }
        }
    }

    void cancelInitializingActivities() {
        ActivityRecord topActivity = topRunningActivityLocked();
        boolean aboveTop = true;
        boolean behindFullscreenActivity = false;
        if (shouldBeVisible(null) == 0) {
            aboveTop = false;
            behindFullscreenActivity = true;
        }
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ArrayList<ActivityRecord> activities = ((TaskRecord) this.mTaskHistory.get(taskNdx)).mActivities;
            for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                int i;
                ActivityRecord r = (ActivityRecord) activities.get(activityNdx);
                if (aboveTop) {
                    if (r == topActivity) {
                        aboveTop = false;
                    }
                    i = r.fullscreen;
                } else {
                    r.removeOrphanedStartingWindow(behindFullscreenActivity);
                    i = r.fullscreen;
                }
                behindFullscreenActivity |= i;
            }
        }
    }

    boolean resumeTopActivityUncheckedLocked(ActivityRecord prev, ActivityOptions options) {
        if (this.mStackSupervisor.inResumeTopActivity) {
            return false;
        }
        boolean result = false;
        try {
            this.mStackSupervisor.inResumeTopActivity = true;
            result = resumeTopActivityInnerLocked(prev, options);
            ActivityRecord next = topRunningActivityLocked(true);
            if (next == null || (next.canTurnScreenOn() ^ 1) != 0) {
                checkReadyForSleep();
            }
            if (!(next == null || !next.shortComponentName.equals("com.oppo.camera/.Camera") || (this.mStackSupervisor.mKeyguardController.isKeyguardLocked() ^ 1) == 0)) {
                next.setShowWhenLocked(false);
            }
            return result;
        } finally {
            this.mStackSupervisor.inResumeTopActivity = false;
        }
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "Jianhua.Lin@Plf.SDK : Modify for mResumedActivity", property = OppoRomType.ROM)
    void setResumedActivityLocked(ActivityRecord r, String reason) {
        if (!(r == null || r.app == null)) {
            String value = r.processName;
            if (value != null && value.length() > 50) {
                value = value.substring(0, 50);
            }
            SystemProperties.set("debug.junk.process.name", value);
            SystemProperties.set("debug.junk.process.pid", Integer.toString(r.app.pid));
        }
        this.mResumedActivity = r;
        r.state = ActivityState.RESUMED;
        this.mService.setResumedActivityUncheckLocked(r, reason);
        TaskRecord task = r.getTask();
        task.touchActiveTime();
        this.mRecentTasks.addLocked(task);
    }

    /* JADX WARNING: Missing block: B:398:?, code:
            r7.completeResumeLocked();
     */
    /* JADX WARNING: Missing block: B:429:0x0aa7, code:
            r24 = move-exception;
     */
    /* JADX WARNING: Missing block: B:430:0x0aa8, code:
            android.util.Slog.w(TAG, "Exception thrown during resume of " + r7, r24);
            requestFinishActivityLocked(r7.appToken, 0, null, "resume-exception", true);
     */
    /* JADX WARNING: Missing block: B:431:0x0ad5, code:
            if (com.android.server.am.ActivityManagerDebugConfig.DEBUG_STACK != false) goto L_0x0ad7;
     */
    /* JADX WARNING: Missing block: B:432:0x0ad7, code:
            r51.mStackSupervisor.validateTopActivitiesLocked();
     */
    /* JADX WARNING: Missing block: B:434:0x0adf, code:
            return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean resumeTopActivityInnerLocked(ActivityRecord prev, ActivityOptions options) {
        if (!this.mService.mBooting && (this.mService.mBooted ^ 1) != 0) {
            return false;
        }
        ActivityRecord next = topRunningActivityLocked(true);
        boolean hasRunningActivity = next != null;
        if (hasRunningActivity && getDisplay() == null) {
            return false;
        }
        if (ActivityManagerDebugConfig.DEBUG_STACK || ActivityManagerDebugConfig.DEBUG_STATES) {
            Throwable trace = new RuntimeException();
            trace.fillInStackTrace();
            Slog.d(TAG, "resumeTopActivityLocked, prev:" + prev, trace);
        } else if (ActivityManagerDebugConfig.DEBUG_AMS) {
            Slog.d(TAG, "resumeTopActivityLocked, prev:" + prev + " call by " + Debug.getCallers(6));
        }
        this.mStackSupervisor.cancelInitializingActivities();
        if (ActivityManagerDebugConfig.DEBUG_STATES || ActivityManagerDebugConfig.DEBUG_AMS) {
            Slog.d(TAG, "resumeTopActivityLocked: next " + next);
        }
        boolean userLeaving = this.mStackSupervisor.mUserLeaving;
        this.mStackSupervisor.mUserLeaving = false;
        this.mHasRunningActivity = hasRunningActivity;
        if (hasRunningActivity) {
            next.delayedResume = false;
            if (this.mResumedActivity == next && next.state == ActivityState.RESUMED && this.mStackSupervisor.allResumedActivitiesComplete()) {
                executeAppTransition(options);
                if (ActivityManagerDebugConfig.DEBUG_STATES) {
                    Slog.d(TAG_STATES, "resumeTopActivityLocked: Top activity resumed " + next);
                }
                if (ActivityManagerDebugConfig.DEBUG_STACK) {
                    this.mStackSupervisor.validateTopActivitiesLocked();
                }
                return false;
            }
            TaskRecord nextTask = next.getTask();
            TaskRecord prevTask = prev != null ? prev.getTask() : null;
            if (prevTask != null && prevTask.getStack() == this && prevTask.isOverHomeStack() && prev.finishing && prev.frontOfTask) {
                if (ActivityManagerDebugConfig.DEBUG_STACK) {
                    this.mStackSupervisor.validateTopActivitiesLocked();
                }
                if (prevTask == nextTask) {
                    prevTask.setFrontOfTask();
                } else if (prevTask != topTask()) {
                    ((TaskRecord) this.mTaskHistory.get(this.mTaskHistory.indexOf(prevTask) + 1)).setTaskToReturnTo(1);
                } else if (!isOnHomeDisplay()) {
                    return false;
                } else {
                    if (!isHomeStack()) {
                        boolean resumeHomeStackTask;
                        if (ActivityManagerDebugConfig.DEBUG_STATES) {
                            Slog.d(TAG_STATES, "resumeTopActivityLocked: Launching home next");
                        }
                        if (isOnHomeDisplay()) {
                            resumeHomeStackTask = this.mStackSupervisor.resumeHomeStackTask(prev, "prevFinished");
                        } else {
                            resumeHomeStackTask = false;
                        }
                        return resumeHomeStackTask;
                    }
                }
            }
            ActivityStack focusStack = this.mStackSupervisor.getFocusedStack();
            ActivityStack lastStackBeforeResume = this.mStackSupervisor.getLastStack();
            if (focusStack != null && lastStackBeforeResume != null && isHomeStack() && focusStack == this && focusStack != lastStackBeforeResume && this.mLastPausedActivity == next) {
                if (ActivityManagerDebugConfig.DEBUG_TASKS || ActivityManagerDebugConfig.DEBUG_STACK) {
                    Slog.d(TAG, "resumeTopActivityLocked: Going to sleep, and resume home from other stack, try resume one time.");
                }
            }
            if (shouldSleepOrShutDownActivities() && this.mLastPausedActivity == next && this.mStackSupervisor.allPausedActivitiesComplete()) {
                executeAppTransition(options);
                if (ActivityManagerDebugConfig.DEBUG_STATES) {
                    Slog.d(TAG_STATES, "resumeTopActivityLocked: Going to sleep and all paused");
                }
                if (ActivityManagerDebugConfig.DEBUG_STACK) {
                    this.mStackSupervisor.validateTopActivitiesLocked();
                }
                return false;
            } else if (this.mService.mUserController.hasStartedUserState(next.userId)) {
                this.mStackSupervisor.mStoppingActivities.remove(next);
                this.mStackSupervisor.mGoingToSleepActivities.remove(next);
                next.sleeping = false;
                this.mStackSupervisor.mActivitiesWaitingForVisibleActivity.remove(next);
                next.launching = true;
                if (ActivityManagerDebugConfig.DEBUG_SWITCH) {
                    Slog.v(TAG_SWITCH, "Resuming " + next);
                }
                if (mActivityTrigger != null) {
                    mActivityTrigger.activityResumeTrigger(next.intent, next.info, next.appInfo, next.getTask().mFullscreen);
                }
                if (this.mStackSupervisor.allPausedActivitiesComplete()) {
                    boolean resumeWhilePausing;
                    this.mStackSupervisor.setLaunchSource(next.info.applicationInfo.uid);
                    int lastResumedCanPip = 0;
                    ActivityStack lastFocusedStack = this.mStackSupervisor.getLastStack();
                    if (!(lastFocusedStack == null || lastFocusedStack == this)) {
                        ActivityRecord lastResumed = lastFocusedStack.mResumedActivity;
                        lastResumedCanPip = lastResumed != null ? lastResumed.checkEnterPictureInPictureState("resumeTopActivity", userLeaving) : 0;
                    }
                    if ((next.info.flags & 16384) != 0) {
                        resumeWhilePausing = lastResumedCanPip ^ 1;
                    } else {
                        resumeWhilePausing = false;
                    }
                    boolean pausing = this.mStackSupervisor.pauseBackStacks(userLeaving, next, false);
                    if (this.mResumedActivity != null) {
                        if (ActivityManagerDebugConfig.DEBUG_STATES) {
                            Slog.d(TAG_STATES, "resumeTopActivityLocked: Pausing " + this.mResumedActivity);
                        }
                        pausing |= startPausingLocked(userLeaving, false, next, false, "resume-request");
                    }
                    if (pausing && (resumeWhilePausing ^ 1) != 0) {
                        if (ActivityManagerDebugConfig.DEBUG_SWITCH || ActivityManagerDebugConfig.DEBUG_STATES) {
                            Slog.v(TAG_STATES, "resumeTopActivityLocked: Skip resume: need to start pausing");
                        }
                        if (!(next.app == null || next.app.thread == null)) {
                            this.mService.updateLruProcessLocked(next.app, true, null);
                        }
                        if (ActivityManagerDebugConfig.DEBUG_STACK) {
                            this.mStackSupervisor.validateTopActivitiesLocked();
                        }
                        return true;
                    } else if (this.mResumedActivity == next && next.state == ActivityState.RESUMED && this.mStackSupervisor.allResumedActivitiesComplete()) {
                        executeAppTransition(options);
                        if (ActivityManagerDebugConfig.DEBUG_STATES) {
                            Slog.d(TAG_STATES, "resumeTopActivityLocked: Top activity resumed (dontWaitForPause) " + next);
                        }
                        if (ActivityManagerDebugConfig.DEBUG_STACK) {
                            this.mStackSupervisor.validateTopActivitiesLocked();
                        }
                        return true;
                    } else {
                        if (!(!shouldSleepActivities() || this.mLastNoHistoryActivity == null || (this.mLastNoHistoryActivity.finishing ^ 1) == 0)) {
                            if (ActivityManagerDebugConfig.DEBUG_STATES) {
                                Slog.d(TAG_STATES, "no-history finish of " + this.mLastNoHistoryActivity + " on new resume");
                            }
                            requestFinishActivityLocked(this.mLastNoHistoryActivity.appToken, 0, null, "resume-no-history", false);
                            this.mLastNoHistoryActivity = null;
                        }
                        if (!(prev == null || prev == next)) {
                            if (!this.mStackSupervisor.mActivitiesWaitingForVisibleActivity.contains(prev) && next != null && (next.nowVisible ^ 1) != 0) {
                                this.mStackSupervisor.mActivitiesWaitingForVisibleActivity.add(prev);
                                if (ActivityManagerDebugConfig.DEBUG_SWITCH) {
                                    Slog.v(TAG_SWITCH, "Resuming top, waiting visible to hide: " + prev);
                                }
                            } else if (prev.finishing) {
                                prev.setVisibility(false);
                                if (ActivityManagerDebugConfig.DEBUG_SWITCH) {
                                    Slog.v(TAG_SWITCH, "Not waiting for visible to hide: " + prev + ", waitingVisible=" + this.mStackSupervisor.mActivitiesWaitingForVisibleActivity.contains(prev) + ", nowVisible=" + next.nowVisible);
                                }
                            } else if (ActivityManagerDebugConfig.DEBUG_SWITCH) {
                                Slog.v(TAG_SWITCH, "Previous already visible but still waiting to hide: " + prev + ", waitingVisible=" + this.mStackSupervisor.mActivitiesWaitingForVisibleActivity.contains(prev) + ", nowVisible=" + next.nowVisible);
                            }
                        }
                        try {
                            AppGlobals.getPackageManager().setPackageStoppedState(next.packageName, false, next.userId);
                        } catch (RemoteException e) {
                        } catch (IllegalArgumentException e2) {
                            Slog.w(TAG, "Failed trying to unstop package " + next.packageName + ": " + e2);
                        }
                        boolean anim = true;
                        if (this.mPerf == null) {
                            this.mPerf = new BoostFramework();
                        }
                        if (mHyp == null) {
                            mHyp = Hypnus.getHypnus();
                        }
                        WindowManagerService windowManagerService;
                        int i;
                        if (prev == null) {
                            if (ActivityManagerDebugConfig.DEBUG_TRANSITION) {
                                Slog.v(TAG_TRANSITION, "Prepare open transition: no previous");
                            }
                            if (this.mNoAnimActivities.contains(next)) {
                                anim = false;
                                this.mWindowManager.prepareAppTransition(0, false);
                            } else {
                                this.mWindowManager.prepareAppTransition(6, false);
                            }
                        } else if (prev.finishing) {
                            if (ActivityManagerDebugConfig.DEBUG_TRANSITION) {
                                Slog.v(TAG_TRANSITION, "Prepare close transition: prev=" + prev);
                            }
                            if (this.mNoAnimActivities.contains(prev)) {
                                anim = false;
                                this.mWindowManager.prepareAppTransition(0, false);
                            } else {
                                windowManagerService = this.mWindowManager;
                                if (prev.getTask() == next.getTask()) {
                                    i = 7;
                                } else {
                                    i = 9;
                                }
                                windowManagerService.prepareAppTransition(i, false);
                                if (!(prev.getTask() == next.getTask() || this.mPerf == null)) {
                                    this.mPerf.perfHint(4227, next.packageName);
                                }
                                if (!(prev.getTask() == next.getTask() || mHyp == null)) {
                                    mHyp.hypnusSetAction(11, 600);
                                }
                            }
                            prev.setVisibility(false);
                        } else {
                            if (ActivityManagerDebugConfig.DEBUG_TRANSITION) {
                                Slog.v(TAG_TRANSITION, "Prepare open transition: prev=" + prev);
                            }
                            if (this.mNoAnimActivities.contains(next)) {
                                anim = false;
                                this.mWindowManager.prepareAppTransition(0, false);
                            } else {
                                windowManagerService = this.mWindowManager;
                                if (prev.getTask() == next.getTask()) {
                                    i = 6;
                                } else if (next.mLaunchTaskBehind) {
                                    i = 16;
                                } else {
                                    i = 8;
                                }
                                windowManagerService.prepareAppTransition(i, false);
                                if (!(prev.getTask() == next.getTask() || this.mPerf == null)) {
                                    this.mPerf.perfHint(4227, next.packageName);
                                }
                                if (!(prev.getTask() == next.getTask() || mHyp == null)) {
                                    mHyp.hypnusSetAction(11, 600);
                                }
                            }
                        }
                        Bundle resumeAnimOptions = null;
                        if (anim) {
                            ActivityOptions opts = next.getOptionsForTargetActivityLocked();
                            if (opts != null) {
                                resumeAnimOptions = opts.toBundle();
                            }
                            next.applyOptionsLocked();
                        } else {
                            next.clearOptionsLocked();
                        }
                        ActivityStack lastStack = this.mStackSupervisor.getLastStack();
                        ComponentName lastCpn = lastStack != null ? lastStack.mComponentName : null;
                        this.mComponentName = next.realActivity;
                        OppoDockedManagerService.getInstance().handleActivityToFullscreen(prev, next);
                        if (this.mLastRecordCmpName == null || (this.mLastRecordCmpName.equals(this.mComponentName) ^ 1) != 0) {
                            this.mLastRecordCmpName = this.mComponentName;
                            String pkgName = this.mLastRecordCmpName != null ? this.mLastRecordCmpName.getPackageName() : null;
                            if (pkgName != null) {
                                if ((pkgName.equals(this.mLastRecordPkgName) ^ 1) != 0) {
                                    this.mLastRecordPkgName = pkgName;
                                    Time tobj = new Time();
                                    tobj.set(System.currentTimeMillis());
                                    this.mHandler.postDelayed(new UsageRecorderRunnable(pkgName, tobj.format("%Y-%m-%d %H:%M:%S")), 400);
                                }
                            }
                        }
                        OppoAppSwitchManager.getInstance().handleActivitySwitch(this.mService.mContext, prev, next, resumeWhilePausing, lastCpn);
                        try {
                            if (AppGlobals.getPackageManager().hasSystemFeature(OPPO_SECURITYPAY_FEATURE, 0)) {
                                startSecurityPayService(prev, next);
                            }
                        } catch (RemoteException e3) {
                        }
                        if (this.mStackId == 3) {
                            this.mDockComponentName = next.realActivity;
                        }
                        if (next.app == null || next.app.thread == null) {
                            if (next.hasBeenLaunched) {
                                next.showStartingWindow(null, false, false);
                                if (ActivityManagerDebugConfig.DEBUG_SWITCH) {
                                    Slog.v(TAG_SWITCH, "Restarting: " + next);
                                }
                            } else {
                                next.hasBeenLaunched = true;
                            }
                            if (ActivityManagerDebugConfig.DEBUG_STATES) {
                                Slog.d(TAG_STATES, "resumeTopActivityLocked: Restarting " + next);
                            }
                            this.mStackSupervisor.startSpecificActivityLocked(next, true, true);
                        } else {
                            if (ActivityManagerDebugConfig.DEBUG_SWITCH) {
                                Slog.v(TAG_SWITCH, "Resume running: " + next + " stopped=" + next.stopped + " visible=" + next.visible);
                            }
                            int lastActivityTranslucent;
                            if (lastStack == null) {
                                lastActivityTranslucent = 0;
                            } else if (!lastStack.mFullscreen) {
                                lastActivityTranslucent = 1;
                            } else if (lastStack.mLastPausedActivity != null) {
                                lastActivityTranslucent = lastStack.mLastPausedActivity.fullscreen ^ 1;
                            } else {
                                lastActivityTranslucent = 0;
                            }
                            synchronized (this.mWindowManager.getWindowManagerLock()) {
                                if (!(next.visible && !next.stopped && lastActivityTranslucent == 0)) {
                                    next.setVisibility(true);
                                }
                                next.startLaunchTickingLocked();
                                ActivityRecord lastResumedActivity = lastStack == null ? null : lastStack.mResumedActivity;
                                ActivityState lastState = next.state;
                                this.mService.updateCpuStats();
                                if (ActivityManagerDebugConfig.DEBUG_STATES) {
                                    Slog.v(TAG_STATES, "Moving to RESUMED: " + next + " (in existing)" + " call by " + Debug.getCallers(8));
                                }
                                setResumedActivityLocked(next, "resumeTopActivityInnerLocked");
                                this.mService.updateLruProcessLocked(next.app, true, null);
                                updateLRUListLocked(next);
                                this.mService.updateOomAdjLocked();
                                boolean notUpdated = true;
                                if (this.mStackSupervisor.isFocusedStack(this)) {
                                    if (this.mStackSupervisor.mKeyguardController.isKeyguardLocked()) {
                                        this.mStackSupervisor.ensureActivitiesVisibleLocked(null, 0, false);
                                    }
                                    Configuration config = this.mWindowManager.updateOrientationFromAppTokens(this.mStackSupervisor.getDisplayOverrideConfiguration(this.mDisplayId), next.mayFreezeScreenLocked(next.app) ? next.appToken : null, this.mDisplayId);
                                    if (config != null) {
                                        next.frozenBeforeDestroy = true;
                                    }
                                    notUpdated = this.mService.updateDisplayOverrideConfigurationLocked(config, next, false, this.mDisplayId) ^ 1;
                                }
                                if (notUpdated) {
                                    ActivityRecord nextNext = topRunningActivityLocked();
                                    if (ActivityManagerDebugConfig.DEBUG_SWITCH || ActivityManagerDebugConfig.DEBUG_STATES) {
                                        Slog.i(TAG_STATES, "Activity config changed during resume: " + next + ", new next: " + nextNext);
                                    }
                                    if (nextNext != next) {
                                        this.mStackSupervisor.scheduleResumeTopActivities();
                                    }
                                    if (!next.visible || next.stopped) {
                                        next.setVisibility(true);
                                    }
                                    next.completeResumeLocked();
                                    if (ActivityManagerDebugConfig.DEBUG_STACK) {
                                        this.mStackSupervisor.validateTopActivitiesLocked();
                                    }
                                    return true;
                                }
                                try {
                                    ArrayList<ResultInfo> a = next.results;
                                    if (a != null) {
                                        int N = a.size();
                                        if (!next.finishing && N > 0) {
                                            if (ActivityManagerDebugConfig.DEBUG_RESULTS) {
                                                Slog.v(TAG_RESULTS, "Delivering results to " + next + ": " + a);
                                            }
                                            next.app.thread.scheduleSendResult(next.appToken, a);
                                        }
                                    }
                                    if (next.newIntents != null) {
                                        next.app.thread.scheduleNewIntent(next.newIntents, next.appToken, false);
                                    }
                                    next.notifyAppResumed(next.stopped);
                                    EventLog.writeEvent(EventLogTags.AM_RESUME_ACTIVITY, new Object[]{Integer.valueOf(next.userId), Integer.valueOf(System.identityHashCode(next)), Integer.valueOf(next.getTask().taskId), next.shortComponentName});
                                    next.sleeping = false;
                                    this.mService.showUnsupportedZoomDialogIfNeededLocked(next);
                                    this.mService.showAskCompatModeDialogLocked(next);
                                    next.app.pendingUiClean = true;
                                    next.app.forceProcessStateUpTo(this.mService.mTopProcessState);
                                    next.clearOptionsLocked();
                                    next.app.thread.scheduleResumeActivity(next.appToken, next.app.repProcState, this.mService.isNextTransitionForward(), resumeAnimOptions);
                                    if (ActivityManagerDebugConfig.DEBUG_STATES) {
                                        Slog.d(TAG_STATES, "resumeTopActivityLocked: Resumed " + next);
                                    }
                                } catch (Exception e4) {
                                    if (ActivityManagerDebugConfig.DEBUG_STATES) {
                                        Slog.v(TAG_STATES, "Resume failed; resetting state to " + lastState + ": " + next);
                                    }
                                    next.state = lastState;
                                    if (lastStack != null) {
                                        lastStack.mResumedActivity = lastResumedActivity;
                                    }
                                    Slog.i(TAG, "Restarting because process died: " + next);
                                    if (!next.hasBeenLaunched) {
                                        next.hasBeenLaunched = true;
                                    } else if (lastStack != null) {
                                        if (this.mStackSupervisor.isFrontStackOnDisplay(lastStack)) {
                                            next.showStartingWindow(null, false, false);
                                        }
                                    }
                                    this.mStackSupervisor.startSpecificActivityLocked(next, true, false);
                                    if (ActivityManagerDebugConfig.DEBUG_STACK) {
                                        this.mStackSupervisor.validateTopActivitiesLocked();
                                    }
                                    return true;
                                }
                            }
                        }
                        if (ActivityManagerDebugConfig.DEBUG_STACK) {
                            this.mStackSupervisor.validateTopActivitiesLocked();
                        }
                        return true;
                    }
                }
                if (ActivityManagerDebugConfig.DEBUG_SWITCH || ActivityManagerDebugConfig.DEBUG_PAUSE || ActivityManagerDebugConfig.DEBUG_STATES) {
                    Slog.v(TAG_PAUSE, "resumeTopActivityLocked: Skip resume: some activity pausing.");
                }
                if (ActivityManagerDebugConfig.DEBUG_STACK) {
                    this.mStackSupervisor.validateTopActivitiesLocked();
                }
                return false;
            } else {
                Slog.w(TAG, "Skipping resume of top activity " + next + ": user " + next.userId + " is stopped");
                if (ActivityManagerDebugConfig.DEBUG_STACK) {
                    this.mStackSupervisor.validateTopActivitiesLocked();
                }
                return false;
            }
        }
        return resumeTopActivityInNextFocusableStack(prev, options, "noMoreActivities");
    }

    private boolean resumeTopActivityInNextFocusableStack(ActivityRecord prev, ActivityOptions options, String reason) {
        if ((!this.mFullscreen || (isOnHomeDisplay() ^ 1) != 0 || this.mStackId == 2) && adjustFocusToNextFocusableStackLocked(reason)) {
            return this.mStackSupervisor.resumeFocusedStackTopActivityLocked(this.mStackSupervisor.getFocusedStack(), prev, null);
        }
        boolean resumeHomeStackTask;
        ActivityOptions.abort(options);
        if (ActivityManagerDebugConfig.DEBUG_STATES) {
            Slog.d(TAG_STATES, "resumeTopActivityInNextFocusableStack: " + reason + ", go home");
        }
        if (ActivityManagerDebugConfig.DEBUG_STACK) {
            this.mStackSupervisor.validateTopActivitiesLocked();
        }
        if (isOnHomeDisplay()) {
            resumeHomeStackTask = this.mStackSupervisor.resumeHomeStackTask(prev, reason);
        } else {
            resumeHomeStackTask = false;
        }
        return resumeHomeStackTask;
    }

    private TaskRecord getNextTask(TaskRecord targetTask) {
        int index = this.mTaskHistory.indexOf(targetTask);
        if (index >= 0) {
            int numTasks = this.mTaskHistory.size();
            for (int i = index + 1; i < numTasks; i++) {
                TaskRecord task = (TaskRecord) this.mTaskHistory.get(i);
                ActivityRecord r = task.topRunningActivityLocked();
                if (r == null) {
                    Slog.w(TAG, "invalid task " + task);
                }
                if (r != null && task.userId == targetTask.userId) {
                    return task;
                }
            }
        }
        return null;
    }

    int getAdjustedPositionForTask(TaskRecord task, int suggestedPosition, ActivityRecord starting) {
        int maxPosition = this.mTaskHistory.size();
        if ((starting != null && starting.okToShowLocked()) || (starting == null && task.okToShowLocked())) {
            return Math.min(suggestedPosition, maxPosition);
        }
        while (maxPosition > 0) {
            TaskRecord tmpTask = (TaskRecord) this.mTaskHistory.get(maxPosition - 1);
            if (!this.mStackSupervisor.isCurrentProfileLocked(tmpTask.userId) || tmpTask.topRunningActivityLocked() == null) {
                break;
            }
            maxPosition--;
        }
        return Math.min(suggestedPosition, maxPosition);
    }

    private void insertTaskAtPosition(TaskRecord task, int position) {
        if (position >= this.mTaskHistory.size()) {
            insertTaskAtTop(task, null);
            return;
        }
        position = getAdjustedPositionForTask(task, position, null);
        this.mTaskHistory.remove(task);
        this.mTaskHistory.add(position, task);
        this.mWindowContainerController.positionChildAt(task.getWindowContainerController(), position, task.mBounds, task.getOverrideConfiguration());
        updateTaskMovement(task, true);
    }

    private void insertTaskAtTop(TaskRecord task, ActivityRecord starting) {
        updateTaskReturnToForTopInsertion(task);
        if (ActivityManagerDebugConfig.DEBUG_TASKS) {
            Slog.d(TAG, "befor insertTaskAtTop:mTaskHistory." + this.mTaskHistory);
        }
        this.mTaskHistory.remove(task);
        this.mTaskHistory.add(getAdjustedPositionForTask(task, this.mTaskHistory.size(), starting), task);
        if (ActivityManagerDebugConfig.DEBUG_TASKS) {
            Slog.d(TAG, "after insertTaskAtTop:mTaskHistory." + this.mTaskHistory);
        }
        updateTaskMovement(task, true);
        this.mWindowContainerController.positionChildAtTop(task.getWindowContainerController(), true);
    }

    private void updateTaskReturnToForTopInsertion(TaskRecord task) {
        boolean isLastTaskOverHome = false;
        if (task.isOverHomeStack() || task.isOverAssistantStack()) {
            TaskRecord nextTask = getNextTask(task);
            if (nextTask != null) {
                nextTask.setTaskToReturnTo(task.getTaskToReturnTo());
            } else {
                isLastTaskOverHome = true;
            }
        }
        if (isOnHomeDisplay()) {
            ActivityStack lastStack = this.mStackSupervisor.getLastStack();
            if (lastStack != null) {
                if (lastStack.isAssistantStack()) {
                    task.setTaskToReturnTo(3);
                    return;
                }
                boolean fromHomeOrRecents = lastStack.isHomeOrRecentsStack();
                TaskRecord topTask = lastStack.topTask();
                if (!isHomeOrRecentsStack() && (fromHomeOrRecents || topTask() != task)) {
                    int returnToType = isLastTaskOverHome ? task.getTaskToReturnTo() : 0;
                    if (fromHomeOrRecents && StackId.allowTopTaskToReturnHome(this.mStackId)) {
                        returnToType = topTask == null ? 1 : topTask.taskType;
                    }
                    if (!(task.mStack == null || task.mStack.mStackId == 3)) {
                        task.setTaskToReturnTo(returnToType);
                    }
                }
                return;
            }
            return;
        }
        task.setTaskToReturnTo(0);
    }

    final void startActivityLocked(ActivityRecord r, ActivityRecord focusedTopActivity, boolean newTask, boolean keepCurTransition, ActivityOptions options) {
        TaskRecord rTask = r.getTask();
        int taskId = rTask.taskId;
        if (!(r.info == null || r.realActivity == null || !MM_WEB_UI.equals(r.realActivity.getClassName()))) {
            r.info.screenOrientation = 3;
        }
        if (!r.mLaunchTaskBehind && (taskForIdLocked(taskId) == null || newTask)) {
            insertTaskAtTop(rTask, r);
        }
        TaskRecord task = null;
        if (!newTask) {
            boolean startIt = true;
            for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
                task = (TaskRecord) this.mTaskHistory.get(taskNdx);
                if (task.getTopActivity() != null) {
                    if (task == rTask) {
                        if (!startIt) {
                            if (ActivityManagerDebugConfig.DEBUG_ADD_REMOVE) {
                                Slog.i(TAG, "Adding activity " + r + " to task " + task, new RuntimeException("here").fillInStackTrace());
                            }
                            r.createWindowContainer();
                            ActivityOptions.abort(options);
                            return;
                        }
                    } else if (task.numFullscreen > 0) {
                        startIt = false;
                    }
                }
            }
        }
        TaskRecord activityTask = r.getTask();
        if (task == activityTask && this.mTaskHistory.indexOf(task) != this.mTaskHistory.size() - 1) {
            this.mStackSupervisor.mUserLeaving = false;
            if (ActivityManagerDebugConfig.DEBUG_USER_LEAVING) {
                Slog.v(TAG_USER_LEAVING, "startActivity() behind front, mUserLeaving=false");
            }
        }
        task = activityTask;
        if (ActivityManagerDebugConfig.DEBUG_ADD_REMOVE) {
            Slog.i(TAG, "Adding activity " + r + " to stack to task " + activityTask, new RuntimeException("here").fillInStackTrace());
        }
        if (r.getWindowContainerController() == null) {
            r.createWindowContainer();
        }
        activityTask.setFrontOfTask();
        if (mActivityTrigger != null) {
            mActivityTrigger.activityStartTrigger(r.intent, r.info, r.appInfo, r.getTask().mFullscreen);
        }
        if (!isHomeStack() || numActivities() > 0) {
            if (ActivityManagerDebugConfig.DEBUG_TRANSITION) {
                Slog.v(TAG_TRANSITION, "Prepare open transition: starting " + r);
            }
            if ((r.intent.getFlags() & 65536) != 0) {
                this.mWindowManager.prepareAppTransition(0, keepCurTransition);
                this.mNoAnimActivities.add(r);
            } else {
                int transit = 6;
                if (newTask) {
                    if (r.mLaunchTaskBehind) {
                        transit = 16;
                    } else {
                        if (canEnterPipOnTaskSwitch(focusedTopActivity, null, r, options)) {
                            focusedTopActivity.supportsEnterPipOnTaskSwitch = true;
                        }
                        transit = 8;
                    }
                }
                this.mWindowManager.prepareAppTransition(transit, keepCurTransition);
                this.mNoAnimActivities.remove(r);
            }
            boolean doShow = true;
            if (newTask) {
                if ((r.intent.getFlags() & DumpState.DUMP_COMPILER_STATS) != 0) {
                    resetTaskIfNeededLocked(r, r);
                    doShow = topRunningNonDelayedActivityLocked(null) == r;
                }
            } else if (options != null && options.getAnimationType() == 5) {
                doShow = false;
            }
            if (r.mLaunchTaskBehind) {
                r.setVisibility(true);
                ensureActivitiesVisibleLocked(null, 0, false);
            } else if (doShow && (r.isFreeform() ^ 1) != 0) {
                TaskRecord prevTask = r.getTask();
                ActivityRecord prev = prevTask.topRunningActivityWithStartingWindowLocked();
                if (prev != null) {
                    if (prev.getTask() != prevTask) {
                        prev = null;
                    } else if (prev.nowVisible) {
                        prev = null;
                    }
                }
                r.showStartingWindow(prev, newTask, isTaskSwitch(r, focusedTopActivity));
            }
        } else {
            ActivityOptions.abort(options);
        }
    }

    private boolean canEnterPipOnTaskSwitch(ActivityRecord pipCandidate, TaskRecord toFrontTask, ActivityRecord toFrontActivity, ActivityOptions opts) {
        if ((opts != null && opts.disallowEnterPictureInPictureWhileLaunching()) || pipCandidate == null || pipCandidate.getStackId() == 4) {
            return false;
        }
        int targetStackId;
        if (toFrontTask != null) {
            targetStackId = toFrontTask.getStackId();
        } else {
            targetStackId = toFrontActivity.getStackId();
        }
        if (targetStackId == 6) {
            return false;
        }
        return true;
    }

    private boolean isTaskSwitch(ActivityRecord r, ActivityRecord topFocusedActivity) {
        return (topFocusedActivity == null || r.getTask() == topFocusedActivity.getTask()) ? false : true;
    }

    private ActivityOptions resetTargetTaskIfNeededLocked(TaskRecord task, boolean forceReset) {
        ActivityOptions topOptions = null;
        int replyChainEnd = -1;
        boolean canMoveOptions = true;
        ArrayList<ActivityRecord> activities = task.mActivities;
        int numActivities = activities.size();
        int rootActivityNdx = task.findEffectiveRootIndex();
        for (int i = numActivities - 1; i > rootActivityNdx; i--) {
            ActivityRecord target = (ActivityRecord) activities.get(i);
            if (target.frontOfTask) {
                break;
            }
            int flags = target.info.flags;
            boolean finishOnTaskLaunch = (flags & 2) != 0;
            boolean allowTaskReparenting = (flags & 64) != 0;
            boolean clearWhenTaskReset = (target.intent.getFlags() & DumpState.DUMP_FROZEN) != 0;
            if (finishOnTaskLaunch || (clearWhenTaskReset ^ 1) == 0 || target.resultTo == null) {
                boolean noOptions;
                int srcPos;
                ActivityRecord p;
                if (!finishOnTaskLaunch && (clearWhenTaskReset ^ 1) != 0 && allowTaskReparenting && target.taskAffinity != null && (target.taskAffinity.equals(task.affinity) ^ 1) != 0) {
                    TaskRecord targetTask;
                    ActivityRecord bottom = (this.mTaskHistory.isEmpty() || (((TaskRecord) this.mTaskHistory.get(0)).mActivities.isEmpty() ^ 1) == 0) ? null : (ActivityRecord) ((TaskRecord) this.mTaskHistory.get(0)).mActivities.get(0);
                    if (bottom == null || target.taskAffinity == null || !target.taskAffinity.equals(bottom.getTask().affinity)) {
                        targetTask = createTaskRecord(this.mStackSupervisor.getNextTaskIdForUserLocked(target.userId), target.info, null, null, null, false, target.mActivityType);
                        targetTask.affinityIntent = target.intent;
                        if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                            Slog.v(TAG_TASKS, "Start pushing activity " + target + " out to new task " + targetTask);
                        }
                    } else {
                        targetTask = bottom.getTask();
                        if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                            Slog.v(TAG_TASKS, "Start pushing activity " + target + " out to bottom task " + targetTask);
                        }
                    }
                    noOptions = canMoveOptions;
                    for (srcPos = replyChainEnd < 0 ? i : replyChainEnd; srcPos >= i; srcPos--) {
                        p = (ActivityRecord) activities.get(srcPos);
                        if (!p.finishing) {
                            canMoveOptions = false;
                            if (noOptions && topOptions == null) {
                                topOptions = p.takeOptionsLocked();
                                if (topOptions != null) {
                                    noOptions = false;
                                }
                            }
                            if (ActivityManagerDebugConfig.DEBUG_ADD_REMOVE) {
                                Slog.i(TAG_ADD_REMOVE, "Removing activity " + p + " from task=" + task + " adding to task=" + targetTask + " Callers=" + Debug.getCallers(4));
                            }
                            if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                                Slog.v(TAG_TASKS, "Pushing next activity " + p + " out to target's task " + target);
                            }
                            p.reparent(targetTask, 0, "resetTargetTaskIfNeeded");
                        }
                    }
                    this.mWindowContainerController.positionChildAtBottom(targetTask.getWindowContainerController());
                    replyChainEnd = -1;
                } else if (forceReset || finishOnTaskLaunch || clearWhenTaskReset) {
                    int end;
                    if (clearWhenTaskReset) {
                        end = activities.size() - 1;
                    } else if (replyChainEnd < 0) {
                        end = i;
                    } else {
                        end = replyChainEnd;
                    }
                    noOptions = canMoveOptions;
                    srcPos = i;
                    while (srcPos <= end) {
                        p = (ActivityRecord) activities.get(srcPos);
                        if (!p.finishing) {
                            canMoveOptions = false;
                            if (noOptions && topOptions == null) {
                                topOptions = p.takeOptionsLocked();
                                if (topOptions != null) {
                                    noOptions = false;
                                }
                            }
                            if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                                Slog.w(TAG_TASKS, "resetTaskIntendedTask: calling finishActivity on " + p);
                            }
                            if (finishActivityLocked(p, 0, null, "reset-task", false)) {
                                end--;
                                srcPos--;
                            }
                        }
                        srcPos++;
                    }
                    replyChainEnd = -1;
                } else {
                    replyChainEnd = -1;
                }
            } else if (replyChainEnd < 0) {
                replyChainEnd = i;
            }
        }
        return topOptions;
    }

    private int resetAffinityTaskIfNeededLocked(TaskRecord affinityTask, TaskRecord task, boolean topTaskIsHigher, boolean forceReset, int taskInsertionPoint) {
        int replyChainEnd = -1;
        int taskId = task.taskId;
        String taskAffinity = task.affinity;
        ArrayList<ActivityRecord> activities = affinityTask.mActivities;
        int numActivities = activities.size();
        int rootActivityNdx = affinityTask.findEffectiveRootIndex();
        for (int i = numActivities - 1; i > rootActivityNdx; i--) {
            ActivityRecord target = (ActivityRecord) activities.get(i);
            if (target.frontOfTask) {
                break;
            }
            int flags = target.info.flags;
            boolean finishOnTaskLaunch = (flags & 2) != 0;
            boolean allowTaskReparenting = (flags & 64) != 0;
            if (target.resultTo != null) {
                if (replyChainEnd < 0) {
                    replyChainEnd = i;
                }
            } else if (topTaskIsHigher && allowTaskReparenting && taskAffinity != null) {
                if (taskAffinity.equals(target.taskAffinity)) {
                    int start;
                    int srcPos;
                    ActivityRecord p;
                    if (forceReset || finishOnTaskLaunch) {
                        start = replyChainEnd >= 0 ? replyChainEnd : i;
                        if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                            Slog.v(TAG_TASKS, "Finishing task at index " + start + " to " + i);
                        }
                        for (srcPos = start; srcPos >= i; srcPos--) {
                            p = (ActivityRecord) activities.get(srcPos);
                            if (!p.finishing) {
                                finishActivityLocked(p, 0, null, "move-affinity", false);
                            }
                        }
                    } else {
                        if (taskInsertionPoint < 0) {
                            taskInsertionPoint = task.mActivities.size();
                        }
                        start = replyChainEnd >= 0 ? replyChainEnd : i;
                        if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                            Slog.v(TAG_TASKS, "Reparenting from task=" + affinityTask + ":" + start + "-" + i + " to task=" + task + ":" + taskInsertionPoint);
                        }
                        for (srcPos = start; srcPos >= i; srcPos--) {
                            p = (ActivityRecord) activities.get(srcPos);
                            p.reparent(task, taskInsertionPoint, "resetAffinityTaskIfNeededLocked");
                            if (ActivityManagerDebugConfig.DEBUG_ADD_REMOVE) {
                                Slog.i(TAG_ADD_REMOVE, "Removing and adding activity " + p + " to stack at " + task + " callers=" + Debug.getCallers(3));
                            }
                            if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                                Slog.v(TAG_TASKS, "Pulling activity " + p + " from " + srcPos + " in to resetting task " + task);
                            }
                        }
                        this.mWindowContainerController.positionChildAtTop(task.getWindowContainerController(), true);
                        if (target.info.launchMode == 1) {
                            ArrayList<ActivityRecord> taskActivities = task.mActivities;
                            int targetNdx = taskActivities.indexOf(target);
                            if (targetNdx > 0) {
                                p = (ActivityRecord) taskActivities.get(targetNdx - 1);
                                if (p.intent.getComponent().equals(target.intent.getComponent())) {
                                    finishActivityLocked(p, 0, null, "replace", false);
                                }
                            }
                        }
                    }
                    replyChainEnd = -1;
                }
            }
        }
        return taskInsertionPoint;
    }

    final ActivityRecord resetTaskIfNeededLocked(ActivityRecord taskTop, ActivityRecord newActivity) {
        boolean forceReset = (newActivity.info.flags & 4) != 0;
        TaskRecord task = taskTop.getTask();
        boolean taskFound = false;
        ActivityOptions topOptions = null;
        int reparentInsertionPoint = -1;
        for (int i = this.mTaskHistory.size() - 1; i >= 0; i--) {
            TaskRecord targetTask = (TaskRecord) this.mTaskHistory.get(i);
            if (targetTask == null || task == null || targetTask.userId == task.userId || targetTask.realActivity == null || !OppoMultiAppManagerUtil.getInstance().isMultiApp(targetTask.realActivity.getPackageName())) {
                if (targetTask == task) {
                    topOptions = resetTargetTaskIfNeededLocked(task, forceReset);
                    taskFound = true;
                } else {
                    reparentInsertionPoint = resetAffinityTaskIfNeededLocked(targetTask, task, taskFound, forceReset, reparentInsertionPoint);
                }
            }
        }
        int taskNdx = this.mTaskHistory.indexOf(task);
        if (taskNdx >= 0) {
            while (true) {
                int taskNdx2 = taskNdx - 1;
                taskTop = ((TaskRecord) this.mTaskHistory.get(taskNdx)).getTopActivity();
                if (taskTop != null || taskNdx2 < 0) {
                } else {
                    taskNdx = taskNdx2;
                }
            }
        }
        if (topOptions != null) {
            if (taskTop != null) {
                taskTop.updateOptionsLocked(topOptions);
            } else {
                topOptions.abort();
            }
        }
        return taskTop;
    }

    void sendActivityResultLocked(int callingUid, ActivityRecord r, String resultWho, int requestCode, int resultCode, Intent data) {
        if (callingUid > 0) {
            this.mService.grantUriPermissionFromIntentLocked(callingUid, r.packageName, data, r.getUriPermissionsLocked(), r.userId);
        }
        if (ActivityManagerDebugConfig.DEBUG_RESULTS) {
            Slog.v(TAG, "Send activity result to " + r + " : who=" + resultWho + " req=" + requestCode + " res=" + resultCode + " data=" + data);
        }
        if (!(this.mResumedActivity != r || r.app == null || r.app.thread == null)) {
            try {
                ArrayList<ResultInfo> list = new ArrayList();
                list.add(new ResultInfo(resultWho, requestCode, resultCode, data));
                r.app.thread.scheduleSendResult(r.appToken, list);
                return;
            } catch (Exception e) {
                Slog.w(TAG, "Exception thrown sending result to " + r, e);
            }
        }
        r.addResultLocked(null, resultWho, requestCode, resultCode, data);
    }

    boolean isATopFinishingTask(TaskRecord task) {
        for (int i = this.mTaskHistory.size() - 1; i >= 0; i--) {
            TaskRecord current = (TaskRecord) this.mTaskHistory.get(i);
            if (current.topRunningActivityLocked() != null) {
                return false;
            }
            if (current == task) {
                return true;
            }
        }
        return false;
    }

    private void adjustFocusedActivityStackLocked(ActivityRecord r, String reason) {
        if (ActivityManagerDebugConfig.DEBUG_FOCUS) {
            Slog.v(TAG, "adjustFocusedActivityLocked: r= " + r + " mResumedActivity " + this.mResumedActivity + " reason " + reason);
        }
        if (this.mStackSupervisor.isFocusedStack(this) && (this.mResumedActivity == r || this.mResumedActivity == null)) {
            ActivityRecord next = topRunningActivityLocked();
            String myReason = reason + " adjustFocus";
            if (ActivityManagerDebugConfig.DEBUG_FOCUS) {
                Slog.v(TAG, "next: = " + next);
            }
            if (this.mStackId == 2 && r != null && r.mFinishFromClose) {
                r.mFinishFromClose = false;
                this.mStackSupervisor.moveTasksToFullscreenStackLocked(2, false);
                Slog.v(TAG, "oppo freeform close freeform: " + r);
                if (r.finishing) {
                    Slog.v(TAG, "oppo freeform finish " + r);
                    destroyActivityLocked(r, true, "finish-exit-freeform");
                }
                return;
            }
            if (!(r == null || r.shortComponentName == null || !r.shortComponentName.contains("com.coloros.safecenter/.privacy.view.password"))) {
                boolean isFinish = false;
                if (myReason != null && myReason.contains("finishActivity")) {
                    isFinish = true;
                }
                if (this.mStackSupervisor.getStack(3, false, false) != null && isFinish && this.mFullscreen && this.mResumedActivity != null) {
                    this.mStackSupervisor.moveTasksToFullscreenStackLocked(3, true);
                    return;
                }
            }
            if (next != r) {
                if (next == null || !StackId.keepFocusInStackIfPossible(this.mStackId) || !isFocusable()) {
                    TaskRecord task = r.getTask();
                    if (task == null) {
                        throw new IllegalStateException("activity no longer associated with task:" + r);
                    }
                    boolean isAssistantOrOverAssistant;
                    if (task.getStack().isAssistantStack()) {
                        isAssistantOrOverAssistant = true;
                    } else {
                        isAssistantOrOverAssistant = task.isOverAssistantStack();
                    }
                    if (r.frontOfTask && isATopFinishingTask(task) && (task.isOverHomeStack() || isAssistantOrOverAssistant)) {
                        if ((this.mFullscreen && !isAssistantOrOverAssistant) || !adjustFocusToNextFocusableStackLocked(myReason)) {
                            if (task.isOverHomeStack() && this.mStackSupervisor.moveHomeStackTaskToTop(myReason)) {
                                return;
                            }
                        }
                        return;
                    }
                }
                return;
            }
            this.mStackSupervisor.moveFocusableActivityStackToFrontLocked(this.mStackSupervisor.topRunningActivityLocked(), myReason);
        }
    }

    private boolean adjustFocusToNextFocusableStackLocked(String reason) {
        return adjustFocusToNextFocusableStackLocked(reason, false);
    }

    private boolean adjustFocusToNextFocusableStackLocked(String reason, boolean allowFocusSelf) {
        ActivityStack activityStack = null;
        if (isAssistantStack() && bottomTask() != null && bottomTask().getTaskToReturnTo() == 1) {
            return this.mStackSupervisor.moveHomeStackTaskToTop(reason);
        }
        ActivityStackSupervisor activityStackSupervisor = this.mStackSupervisor;
        if (!allowFocusSelf) {
            activityStack = this;
        }
        ActivityStack stack = activityStackSupervisor.getNextFocusableStackLocked(activityStack);
        String myReason = reason + " adjustFocusToNextFocusableStack";
        if (stack == null) {
            return false;
        }
        ActivityRecord top = stack.topRunningActivityLocked();
        boolean isSwapDocked = false;
        if (reason != null && reason.contains(SWAP_DOCKED_STACK)) {
            isSwapDocked = true;
        }
        if (!(this.mFullscreen || (isSwapDocked ^ 1) == 0 || (this.mIsClearTask ^ 1) == 0 || stack.mStackId == 6)) {
            if (stack.isRecentsStack() && top != null && top.visible) {
                if (this.mService.getGlobalConfiguration() != null && this.mService.getGlobalConfiguration().orientation == 1) {
                    this.mWindowManager.setSplitTimeout(500);
                    this.mWindowManager.startFreezingScreen(0, 0);
                }
                Slog.v(TAG, "split screen move home stack: " + this);
                return this.mStackSupervisor.moveHomeStackTaskToTop(reason);
            } else if (this.mStackId != 5 || (this.mStackId == 5 && this.mResumedActivity != null)) {
                stack.moveToFront(myReason);
                if (isDockedStack()) {
                    this.mStackSupervisor.moveTasksToFullscreenStackLocked(3, false);
                } else {
                    this.mStackSupervisor.moveTasksToFullscreenStackLocked(3, true);
                }
                if (this.mService.getGlobalConfiguration() != null && this.mService.getGlobalConfiguration().orientation == 1) {
                    this.mWindowManager.setSplitTimeout(500);
                    this.mWindowManager.startFreezingScreen(0, 0);
                }
                Slog.v(TAG, "split screen move full stack: " + this);
                return true;
            }
        }
        if (stack.isHomeOrRecentsStack() && (top == null || (top.visible ^ 1) != 0)) {
            return this.mStackSupervisor.moveHomeStackTaskToTop(reason);
        }
        if (stack.isAssistantStack() && top != null && top.getTask().getTaskToReturnTo() == 1) {
            this.mStackSupervisor.moveHomeStackTaskToTop("adjustAssistantReturnToHome");
        }
        stack.moveToFront(myReason);
        if (this.mStackId == 2 && stack.mStackId != 2) {
            ActivityRecord r = stack.topRunningActivityLocked();
            if (r != null) {
                this.mWindowManager.setFocusedApp(r.appToken, true);
                Slog.v(TAG, "split screen setFocusedApp: " + r);
            }
        }
        return true;
    }

    final void stopActivityLocked(ActivityRecord r) {
        if (ActivityManagerDebugConfig.DEBUG_SWITCH) {
            Slog.d(TAG_SWITCH, "Stopping: " + r);
        }
        r.launching = false;
        if (!(((r.intent.getFlags() & 1073741824) == 0 && (r.info.flags & 128) == 0) || r.finishing)) {
            if (!shouldSleepActivities()) {
                if (ActivityManagerDebugConfig.DEBUG_STATES) {
                    Slog.d(TAG_STATES, "no-history finish of " + r);
                }
                if (requestFinishActivityLocked(r.appToken, 0, null, "stop-no-history", false)) {
                    r.resumeKeyDispatchingLocked();
                    return;
                }
            } else if (ActivityManagerDebugConfig.DEBUG_STATES) {
                Slog.d(TAG_STATES, "Not finishing noHistory " + r + " on stop because we're just sleeping");
            }
        }
        if (!(r.app == null || r.app.thread == null)) {
            adjustFocusedActivityStackLocked(r, "stopActivity");
            r.resumeKeyDispatchingLocked();
            try {
                r.stopped = false;
                if (ActivityManagerDebugConfig.DEBUG_STATES) {
                    Slog.v(TAG_STATES, "Moving to STOPPING: " + r + " (stop requested)");
                }
                r.state = ActivityState.STOPPING;
                if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
                    Slog.v(TAG_VISIBILITY, "Stopping visible=" + r.visible + " for " + r);
                }
                if (mActivityTrigger != null) {
                    mActivityTrigger.activityStopTrigger(r.intent, r.info, r.appInfo);
                }
                if (!r.visible) {
                    r.setVisible(false);
                }
                EventLogTags.writeAmStopActivity(r.userId, System.identityHashCode(r), r.shortComponentName);
                r.app.thread.scheduleStopActivity(r.appToken, r.visible, r.configChangeFlags);
                if (shouldSleepOrShutDownActivities()) {
                    r.setSleeping(true);
                }
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(104, r), 10000);
            } catch (Exception e) {
                Slog.w(TAG, "Exception thrown during pause", e);
                r.stopped = true;
                if (ActivityManagerDebugConfig.DEBUG_STATES) {
                    Slog.v(TAG_STATES, "Stop failed; moving to STOPPED: " + r);
                }
                r.state = ActivityState.STOPPED;
                if (r.deferRelaunchUntilPaused) {
                    destroyActivityLocked(r, true, "stop-except");
                }
            }
        }
    }

    final boolean requestFinishActivityLocked(IBinder token, int resultCode, Intent resultData, String reason, boolean oomAdj) {
        ActivityRecord r = isInStackLocked(token);
        if (ActivityManagerDebugConfig.DEBUG_RESULTS || ActivityManagerDebugConfig.DEBUG_STATES) {
            Slog.v(TAG_STATES, "Finishing activity token=" + token + " r=" + ", result=" + resultCode + ", data=" + resultData + ", reason=" + reason);
        }
        if (r == null) {
            return false;
        }
        finishActivityLocked(r, resultCode, resultData, reason, oomAdj);
        return true;
    }

    final void finishSubActivityLocked(ActivityRecord self, String resultWho, int requestCode) {
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ArrayList<ActivityRecord> activities = ((TaskRecord) this.mTaskHistory.get(taskNdx)).mActivities;
            for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                ActivityRecord r = (ActivityRecord) activities.get(activityNdx);
                if (r.resultTo == self && r.requestCode == requestCode && ((r.resultWho == null && resultWho == null) || (r.resultWho != null && r.resultWho.equals(resultWho)))) {
                    finishActivityLocked(r, 0, null, "request-sub", false);
                }
            }
        }
        this.mService.updateOomAdjLocked();
    }

    /* JADX WARNING: Missing block: B:30:0x00dc, code:
            if (r2.state == com.android.server.am.ActivityStack.ActivityState.PAUSED) goto L_0x0088;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    final TaskRecord finishTopRunningActivityLocked(ProcessRecord app, String reason) {
        ActivityRecord r = topRunningActivityLocked();
        if (r == null || r.app != app) {
            return null;
        }
        Slog.w(TAG, "  Force finishing activity " + r.intent.getComponent().flattenToShortString());
        TaskRecord finishedTask = r.getTask();
        int taskNdx = this.mTaskHistory.indexOf(finishedTask);
        TaskRecord task = finishedTask;
        int activityNdx = finishedTask.mActivities.indexOf(r);
        finishActivityLocked(r, 0, null, reason, false);
        activityNdx--;
        if (activityNdx < 0) {
            while (true) {
                taskNdx--;
                if (taskNdx >= 0) {
                    activityNdx = ((TaskRecord) this.mTaskHistory.get(taskNdx)).mActivities.size() - 1;
                    if (activityNdx >= 0) {
                        break;
                    }
                } else {
                    break;
                }
            }
        }
        if (activityNdx >= 0 && taskNdx < this.mTaskHistory.size() && activityNdx < ((TaskRecord) this.mTaskHistory.get(taskNdx)).mActivities.size()) {
            try {
                r = (ActivityRecord) ((TaskRecord) this.mTaskHistory.get(taskNdx)).mActivities.get(activityNdx);
                if (!(r.state == ActivityState.RESUMED || r.state == ActivityState.PAUSING)) {
                }
                if (!(r.isHomeActivity() && this.mService.mHomeProcess == r.app)) {
                    Slog.w(TAG, "  Force finishing activity 2 " + r.intent.getComponent().flattenToShortString());
                    finishActivityLocked(r, 0, null, "crashed", false);
                }
            } catch (IndexOutOfBoundsException exce) {
                Slog.wtf(TAG, "finishTopRunningActivityLocked error:", exce);
                exce.printStackTrace();
            } catch (Exception e) {
                Slog.w(TAG, "Exception in finishTopRunningActivityLocked: r = " + r);
                e.printStackTrace();
            }
        }
        return finishedTask;
    }

    final void finishVoiceTask(IVoiceInteractionSession session) {
        IBinder sessionBinder = session.asBinder();
        boolean didOne = false;
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            TaskRecord tr = (TaskRecord) this.mTaskHistory.get(taskNdx);
            int activityNdx;
            ActivityRecord r;
            if (tr.voiceSession == null || tr.voiceSession.asBinder() != sessionBinder) {
                for (activityNdx = tr.mActivities.size() - 1; activityNdx >= 0; activityNdx--) {
                    r = (ActivityRecord) tr.mActivities.get(activityNdx);
                    if (r.voiceSession != null && r.voiceSession.asBinder() == sessionBinder) {
                        r.clearVoiceSessionLocked();
                        try {
                            r.app.thread.scheduleLocalVoiceInteractionStarted(r.appToken, null);
                        } catch (RemoteException e) {
                        }
                        this.mService.finishRunningVoiceLocked();
                        break;
                    }
                }
            } else {
                for (activityNdx = tr.mActivities.size() - 1; activityNdx >= 0; activityNdx--) {
                    r = (ActivityRecord) tr.mActivities.get(activityNdx);
                    if (!r.finishing) {
                        finishActivityLocked(r, 0, null, "finish-voice", false);
                        didOne = true;
                    }
                }
            }
        }
        if (didOne) {
            this.mService.updateOomAdjLocked();
        }
    }

    final boolean finishActivityAffinityLocked(ActivityRecord r) {
        ArrayList<ActivityRecord> activities = r.getTask().mActivities;
        for (int index = activities.indexOf(r); index >= 0; index--) {
            ActivityRecord cur = (ActivityRecord) activities.get(index);
            if (!Objects.equals(cur.taskAffinity, r.taskAffinity)) {
                break;
            }
            finishActivityLocked(cur, 0, null, "request-affinity", true);
        }
        return true;
    }

    private void finishActivityResultsLocked(ActivityRecord r, int resultCode, Intent resultData) {
        ActivityRecord resultTo = r.resultTo;
        if (resultTo != null) {
            if (ActivityManagerDebugConfig.DEBUG_RESULTS) {
                Slog.v(TAG_RESULTS, "Adding result to " + resultTo + " who=" + r.resultWho + " req=" + r.requestCode + " res=" + resultCode + " data=" + resultData);
            }
            if (!(resultTo.userId == r.userId || resultData == null || (OppoMultiAppManager.USER_ID == resultTo.userId && r.packageName != null && "com.android.documentsui".equals(r.packageName) && resultTo.packageName != null && "com.imo.android.imoim".equals(resultTo.packageName) && resultData.getDataString() != null && resultData.getDataString().contains("com.android.providers.media.documents")))) {
                resultData.prepareToLeaveUser(r.userId);
            }
            if (r.info.applicationInfo.uid > 0) {
                this.mService.grantUriPermissionFromIntentLocked(r.info.applicationInfo.uid, resultTo.packageName, resultData, resultTo.getUriPermissionsLocked(), resultTo.userId);
            }
            resultTo.addResultLocked(r, r.resultWho, r.requestCode, resultCode, resultData);
            r.resultTo = null;
        } else if (ActivityManagerDebugConfig.DEBUG_RESULTS) {
            Slog.v(TAG_RESULTS, "No result destination from " + r);
        }
        r.results = null;
        r.pendingResults = null;
        r.newIntents = null;
        r.icicle = null;
    }

    final boolean finishActivityLocked(ActivityRecord r, int resultCode, Intent resultData, String reason, boolean oomAdj) {
        String componentName = "";
        if (r != null) {
            componentName = r.shortComponentName;
        }
        if (!(r == null || r.intent == null || (componentName.endsWith("ChooserActivity") && (r.intent.getFlags() & 1024) == 0))) {
            this.mStackSupervisor.mOppoSecureProtectUtils.handleFinishActivityLocked(this);
        }
        return finishActivityLocked(r, resultCode, resultData, reason, oomAdj, false);
    }

    /* JADX WARNING: Missing block: B:22:0x009a, code:
            if ((r4.isKeyguardShowing(r3) ^ 1) == 0) goto L_0x009c;
     */
    /* JADX WARNING: Missing block: B:31:0x00d2, code:
            if (r24.shortComponentName.equals("com.android.settings/com.oppo.settings.fingerprint.FingerprintResetPassword") == false) goto L_0x00d4;
     */
    /* JADX WARNING: Missing block: B:44:0x0124, code:
            if (r24.shortComponentName.equals("com.android.contacts/.dialpad.EmergencyCall") == false) goto L_0x0126;
     */
    /* JADX WARNING: Missing block: B:46:0x0131, code:
            if (r24.shortComponentName.startsWith("com.nearme.instant.platform/com.nearme.instant.LauncherActivity") == false) goto L_0x0136;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    final boolean finishActivityLocked(ActivityRecord r, int resultCode, Intent resultData, String reason, boolean oomAdj, boolean pauseImmediately) {
        if (r.finishing) {
            Slog.w(TAG, "Duplicate finish request for " + r);
            return false;
        }
        ActivityRecord top = topRunningActivityLocked();
        if (!(r.shortComponentName.equals("com.oppo.camera/.Camera") || r.shortComponentName.equals("com.android.settings/com.oppo.settings.fingerprint.ResetGenericActivity") || r.shortComponentName.equals("com.android.calculator2/.Calculator") || r.shortComponentName.equals("com.tencent.mobileqq/.activity.QQLSActivity") || r.shortComponentName.equals("com.tencent.mm/.plugin.voip.ui.VideoActivity"))) {
            if (r.shortComponentName.contains("com.android.settings/.ChooseLock") && top.toString().equals(r.toString())) {
                KeyguardController keyguardController = this.mStackSupervisor.mKeyguardController;
                int i;
                if (this.mDisplayId != -1) {
                    i = this.mDisplayId;
                } else {
                    i = 0;
                }
            }
            if (!r.shortComponentName.equals("com.coloros.alarmclock/.AlarmClock")) {
                if (!r.shortComponentName.equals("com.coloros.speechassist/.Main")) {
                    if (top != null) {
                        if ((top.shortComponentName.contains("com.android.settings/.ChooseLock") ^ 1) != 0) {
                        }
                    }
                    if (!r.shortComponentName.equals("com.coloros.alarmclock/.timer.TimerAlertFullScreen")) {
                        if (!r.shortComponentName.equals("com.coloros.pictorial/.ui.PictorialDetailsActivity")) {
                            if (!r.shortComponentName.equals("com.coloros.alarmclock/.alert.AlarmAlertFullScreen")) {
                                if (!r.shortComponentName.equals("com.oppo.daydreamvideo/.play.SaleModeActivity")) {
                                    if (top != null) {
                                        if ((top.shortComponentName.equals("com.android.incallui/.OppoInCallActivity") ^ 1) != 0) {
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        hideAppSurfaceUntilKeyguardAppears();
        this.mWindowManager.deferSurfaceLayout();
        try {
            r.makeFinishingLocked();
            this.mWindowManager.setFinishing(r.appToken, true, r.isFreeform());
            TaskRecord task = r.getTask();
            EventLog.writeEvent(EventLogTags.AM_FINISH_ACTIVITY, new Object[]{Integer.valueOf(r.userId), Integer.valueOf(System.identityHashCode(r)), Integer.valueOf(task.taskId), r.shortComponentName, reason});
            ArrayList<ActivityRecord> activities = task.mActivities;
            int index = activities.indexOf(r);
            if (index < activities.size() - 1) {
                task.setFrontOfTask();
                if ((r.intent.getFlags() & DumpState.DUMP_FROZEN) != 0) {
                    ((ActivityRecord) activities.get(index + 1)).intent.addFlags(DumpState.DUMP_FROZEN);
                }
            }
            int taskNdx = this.mTaskHistory.indexOf(task);
            int topTaskNdx = this.mTaskHistory.size() - 1;
            if (r.frontOfTask && task.isOverHomeStack() && taskNdx < topTaskNdx) {
                TaskRecord nextTask = (TaskRecord) this.mTaskHistory.get(taskNdx + 1);
                if (!nextTask.isOverHomeStack()) {
                    nextTask.setTaskToReturnTo(1);
                    Slog.w(TAG, "Next up task set to return home " + nextTask);
                }
            }
            r.pauseKeyDispatchingLocked();
            OppoDockedManagerService.getInstance().revertStack(r, null);
            adjustFocusedActivityStackLocked(r, "finishActivity");
            finishActivityResultsLocked(r, resultCode, resultData);
            int endTask = index <= 0 ? task.isClearingToReuseTask() ^ 1 : 0;
            int transit = endTask != 0 ? 9 : 7;
            if (this.mResumedActivity == r) {
                if (ActivityManagerDebugConfig.DEBUG_VISIBILITY || ActivityManagerDebugConfig.DEBUG_TRANSITION) {
                    Slog.v(TAG_TRANSITION, "Prepare close transition: finishing " + r);
                }
                if (endTask != 0) {
                    this.mService.mTaskChangeNotificationController.notifyTaskRemovalStarted(task.taskId);
                }
                this.mWindowManager.prepareAppTransition(transit, false);
                r.setVisibility(false);
                if (this.mPausingActivity == null) {
                    if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
                        Slog.v(TAG_PAUSE, "Finish needs to pause: " + r);
                    }
                    if (ActivityManagerDebugConfig.DEBUG_USER_LEAVING) {
                        Slog.v(TAG_USER_LEAVING, "finish() => pause with userLeaving=false");
                    }
                    startPausingLocked(false, false, null, pauseImmediately, "finish-request");
                }
                if (endTask != 0) {
                    this.mStackSupervisor.removeLockedTaskLocked(task);
                }
            } else if (r.state != ActivityState.PAUSING) {
                int finishMode;
                if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
                    Slog.v(TAG_PAUSE, "Finish not pausing: " + r);
                }
                if (r.visible) {
                    prepareActivityHideTransitionAnimation(r, transit);
                }
                if (r.visible || r.nowVisible) {
                    finishMode = 2;
                } else {
                    finishMode = 1;
                }
                boolean removedActivity = finishCurrentActivityLocked(r, finishMode, oomAdj) == null;
                if (task.onlyHasTaskOverlayActivities(true)) {
                    for (ActivityRecord taskOverlay : task.mActivities) {
                        if (taskOverlay.mTaskOverlay) {
                            prepareActivityHideTransitionAnimation(taskOverlay, transit);
                        }
                    }
                }
                this.mWindowManager.continueSurfaceLayout();
                return removedActivity;
            } else if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
                Slog.v(TAG_PAUSE, "Finish waiting for pause of: " + r);
            }
            this.mWindowManager.continueSurfaceLayout();
            return false;
        } catch (Throwable th) {
            this.mWindowManager.continueSurfaceLayout();
        }
    }

    private void prepareActivityHideTransitionAnimation(ActivityRecord r, int transit) {
        this.mWindowManager.prepareAppTransition(transit, false);
        r.setVisibility(false);
        this.mWindowManager.executeAppTransition();
        if (!this.mStackSupervisor.mActivitiesWaitingForVisibleActivity.contains(r)) {
            this.mStackSupervisor.mActivitiesWaitingForVisibleActivity.add(r);
        }
    }

    final ActivityRecord finishCurrentActivityLocked(ActivityRecord r, int mode, boolean oomAdj) {
        ActivityRecord next = this.mStackSupervisor.topRunningActivityLocked();
        if (!(((r.intent.getFlags() & 1073741824) == 0 && (r.info.flags & 128) == 0) || next == null || !next.shortComponentName.equals("com.oppo.launcher/.Launcher"))) {
            mode = 2;
        }
        if (mode != 2 || (!(r.visible || r.nowVisible) || next == null || (next.nowVisible && (next.fullscreen ^ 1) == 0))) {
            this.mStackSupervisor.mStoppingActivities.remove(r);
            this.mStackSupervisor.mGoingToSleepActivities.remove(r);
            this.mStackSupervisor.mActivitiesWaitingForVisibleActivity.remove(r);
            if (this.mResumedActivity == r) {
                this.mResumedActivity = null;
            }
            ActivityState prevState = r.state;
            if (ActivityManagerDebugConfig.DEBUG_STATES) {
                Slog.v(TAG_STATES, "Moving to FINISHING: " + r);
            }
            r.state = ActivityState.FINISHING;
            boolean finishingActivityInNonFocusedStack = (r.getStack() == this.mStackSupervisor.getFocusedStack() || prevState != ActivityState.PAUSED) ? false : mode == 2;
            if (mode == 0 || ((prevState == ActivityState.PAUSED && (mode == 1 || this.mStackId == 4)) || finishingActivityInNonFocusedStack || prevState == ActivityState.STOPPING || prevState == ActivityState.STOPPED || prevState == ActivityState.INITIALIZING)) {
                r.makeFinishingLocked();
                boolean activityRemoved = destroyActivityLocked(r, true, "finish-imm");
                if (finishingActivityInNonFocusedStack) {
                    this.mStackSupervisor.ensureActivitiesVisibleLocked(null, 0, false);
                }
                if (activityRemoved) {
                    this.mStackSupervisor.resumeFocusedStackTopActivityLocked();
                }
                if (ActivityManagerDebugConfig.DEBUG_CONTAINERS) {
                    Slog.d(TAG_CONTAINERS, "destroyActivityLocked: finishCurrentActivityLocked r=" + r + " destroy returned removed=" + activityRemoved);
                }
                if (activityRemoved) {
                    r = null;
                }
                return r;
            }
            this.mStackSupervisor.mFinishingActivities.add(r);
            r.resumeKeyDispatchingLocked();
            this.mStackSupervisor.resumeFocusedStackTopActivityLocked();
            return r;
        }
        if (!this.mStackSupervisor.mStoppingActivities.contains(r)) {
            if (r.realActivity.flattenToString().equals("com.oppo.camera/com.oppo.camera.Camera")) {
                addToStopping(r, true, false);
            } else {
                addToStopping(r, false, false);
            }
        }
        if (ActivityManagerDebugConfig.DEBUG_STATES) {
            Slog.v(TAG_STATES, "Moving to STOPPING: " + r + " (finish requested)");
        }
        r.state = ActivityState.STOPPING;
        if (oomAdj) {
            this.mService.updateOomAdjLocked();
        }
        return r;
    }

    void finishAllActivitiesLocked(boolean immediately) {
        boolean noActivitiesInStack = true;
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ArrayList<ActivityRecord> activities = ((TaskRecord) this.mTaskHistory.get(taskNdx)).mActivities;
            for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                ActivityRecord r = (ActivityRecord) activities.get(activityNdx);
                noActivitiesInStack = false;
                if (!r.finishing || (immediately ^ 1) == 0) {
                    Slog.d(TAG, "finishAllActivitiesLocked: finishing " + r + " immediately");
                    finishCurrentActivityLocked(r, 0, false);
                }
            }
        }
        if (noActivitiesInStack) {
            remove();
        }
    }

    final boolean shouldUpRecreateTaskLocked(ActivityRecord srec, String destAffinity) {
        if (srec == null || srec.getTask().affinity == null || (srec.getTask().affinity.equals(destAffinity) ^ 1) != 0) {
            return true;
        }
        TaskRecord task = srec.getTask();
        if (srec.frontOfTask && task != null && task.getBaseIntent() != null && task.getBaseIntent().isDocument()) {
            if (task.getTaskToReturnTo() != 0) {
                return true;
            }
            int taskIdx = this.mTaskHistory.indexOf(task);
            if (taskIdx <= 0) {
                Slog.w(TAG, "shouldUpRecreateTask: task not in history for " + srec);
                return false;
            } else if (taskIdx == 0) {
                return true;
            } else {
                if (!task.affinity.equals(((TaskRecord) this.mTaskHistory.get(taskIdx)).affinity)) {
                    return true;
                }
            }
        }
        return false;
    }

    final boolean navigateUpToLocked(ActivityRecord srec, Intent destIntent, int resultCode, Intent resultData) {
        TaskRecord task = srec.getTask();
        ArrayList<ActivityRecord> activities = task.mActivities;
        int start = activities.indexOf(srec);
        if (!this.mTaskHistory.contains(task) || start < 0) {
            return false;
        }
        int i;
        int finishTo = start - 1;
        ActivityRecord parent = finishTo < 0 ? null : (ActivityRecord) activities.get(finishTo);
        boolean foundParentInTask = false;
        ComponentName dest = destIntent.getComponent();
        if (start > 0 && dest != null) {
            for (i = finishTo; i >= 0; i--) {
                ActivityRecord r = (ActivityRecord) activities.get(i);
                if (r.info.packageName.equals(dest.getPackageName()) && r.info.name.equals(dest.getClassName())) {
                    finishTo = i;
                    parent = r;
                    foundParentInTask = true;
                    break;
                }
            }
        }
        IActivityController controller = this.mService.mController;
        if (controller != null) {
            ActivityRecord next = topRunningActivityLocked(srec.appToken, 0);
            if (next != null) {
                boolean resumeOK = true;
                try {
                    resumeOK = controller.activityResuming(next.packageName);
                } catch (RemoteException e) {
                    this.mService.mController = null;
                    Watchdog.getInstance().setActivityController(null);
                }
                if (!resumeOK) {
                    return false;
                }
            }
        }
        long origId = Binder.clearCallingIdentity();
        for (i = start; i > finishTo; i--) {
            requestFinishActivityLocked(((ActivityRecord) activities.get(i)).appToken, resultCode, resultData, "navigate-up", true);
            resultCode = 0;
            resultData = null;
        }
        if (parent != null && foundParentInTask) {
            int parentLaunchMode = parent.info.launchMode;
            int destIntentFlags = destIntent.getFlags();
            if (parentLaunchMode == 3 || parentLaunchMode == 2 || parentLaunchMode == 1 || (67108864 & destIntentFlags) != 0) {
                parent.deliverNewIntentLocked(srec.info.applicationInfo.uid, destIntent, srec.packageName);
            } else {
                try {
                    foundParentInTask = this.mService.mActivityStarter.startActivityLocked(srec.app.thread, destIntent, null, null, AppGlobals.getPackageManager().getActivityInfo(destIntent.getComponent(), 0, srec.userId), null, null, null, parent.appToken, null, 0, -1, parent.launchedFromUid, parent.launchedFromPackage, -1, parent.launchedFromUid, 0, null, false, true, null, null, "navigateUpTo") == 0;
                } catch (RemoteException e2) {
                    foundParentInTask = false;
                }
                requestFinishActivityLocked(parent.appToken, resultCode, resultData, "navigate-top", true);
            }
        }
        Binder.restoreCallingIdentity(origId);
        return foundParentInTask;
    }

    void onActivityRemovedFromStack(ActivityRecord r) {
        if (this.mResumedActivity == r) {
            this.mResumedActivity = null;
        }
        if (this.mPausingActivity == r) {
            this.mPausingActivity = null;
        }
        removeTimeoutsForActivityLocked(r);
    }

    private void cleanUpActivityLocked(ActivityRecord r, boolean cleanServices, boolean setState) {
        onActivityRemovedFromStack(r);
        r.deferRelaunchUntilPaused = false;
        r.frozenBeforeDestroy = false;
        if (setState) {
            if (ActivityManagerDebugConfig.DEBUG_STATES) {
                Slog.v(TAG_STATES, "Moving to DESTROYED: " + r + " (cleaning up)");
            }
            r.state = ActivityState.DESTROYED;
            if (ActivityManagerDebugConfig.DEBUG_APP) {
                Slog.v(TAG_APP, "Clearing app during cleanUp for activity " + r);
            }
            r.app = null;
        }
        this.mStackSupervisor.cleanupActivity(r);
        if (r.finishing && r.pendingResults != null) {
            for (WeakReference<PendingIntentRecord> apr : r.pendingResults) {
                PendingIntentRecord rec = (PendingIntentRecord) apr.get();
                if (rec != null) {
                    this.mService.cancelIntentSenderLocked(rec, false);
                }
            }
            r.pendingResults = null;
        }
        if (cleanServices) {
            cleanUpActivityServicesLocked(r);
        }
        removeTimeoutsForActivityLocked(r);
        this.mWindowManager.notifyAppRelaunchesCleared(r.appToken);
    }

    void removeTimeoutsForActivityLocked(ActivityRecord r) {
        this.mStackSupervisor.removeTimeoutsForActivityLocked(r);
        this.mHandler.removeMessages(101, r);
        this.mHandler.removeMessages(104, r);
        this.mHandler.removeMessages(102, r);
        r.finishLaunchTickingLocked();
    }

    void removeActivityFromHistoryLocked(ActivityRecord r, String reason) {
        finishActivityResultsLocked(r, 0, null);
        r.makeFinishingLocked();
        if (ActivityManagerDebugConfig.DEBUG_ADD_REMOVE) {
            Slog.i(TAG_ADD_REMOVE, "Removing activity " + r + " from stack callers=" + Debug.getCallers(5));
        }
        r.takeFromHistory();
        removeTimeoutsForActivityLocked(r);
        if (ActivityManagerDebugConfig.DEBUG_STATES) {
            Slog.v(TAG_STATES, "Moving to DESTROYED: " + r + " (removed from history)");
        }
        r.state = ActivityState.DESTROYED;
        if (ActivityManagerDebugConfig.DEBUG_APP) {
            Slog.v(TAG_APP, "Clearing app during remove for activity " + r);
        }
        r.app = null;
        r.removeWindowContainer();
        TaskRecord task = r.getTask();
        boolean lastActivity = task != null ? task.removeActivity(r) : false;
        boolean onlyHasTaskOverlays = task != null ? task.onlyHasTaskOverlayActivities(false) : false;
        if (lastActivity || onlyHasTaskOverlays) {
            if (ActivityManagerDebugConfig.DEBUG_STACK) {
                Slog.i(TAG_STACK, "removeActivityFromHistoryLocked: last activity removed from " + this + " onlyHasTaskOverlays=" + onlyHasTaskOverlays);
            }
            if (this.mStackSupervisor.isFocusedStack(this) && task == topTask() && task.isOverHomeStack()) {
                this.mStackSupervisor.moveHomeStackTaskToTop(reason);
            }
            if (onlyHasTaskOverlays) {
                this.mStackSupervisor.removeTaskByIdLocked(task.taskId, false, false, true);
            }
            if (lastActivity) {
                removeTask(task, reason, 0);
            }
        }
        cleanUpActivityServicesLocked(r);
        r.removeUriPermissionsLocked();
    }

    private void cleanUpActivityServicesLocked(ActivityRecord r) {
        if (r.connections != null) {
            Iterator<ConnectionRecord> it = r.connections.iterator();
            while (it.hasNext()) {
                this.mService.mServices.removeConnectionLocked((ConnectionRecord) it.next(), null, r);
            }
            r.connections = null;
        }
    }

    final void scheduleDestroyActivities(ProcessRecord owner, String reason) {
        Message msg = this.mHandler.obtainMessage(105);
        msg.obj = new ScheduleDestroyArgs(owner, reason);
        this.mHandler.sendMessage(msg);
    }

    private void destroyActivitiesLocked(ProcessRecord owner, String reason) {
        boolean lastIsOpaque = false;
        boolean activityRemoved = false;
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ArrayList<ActivityRecord> activities = ((TaskRecord) this.mTaskHistory.get(taskNdx)).mActivities;
            for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                ActivityRecord r = (ActivityRecord) activities.get(activityNdx);
                if (!r.finishing) {
                    if (r.fullscreen) {
                        lastIsOpaque = true;
                    }
                    if ((owner == null || r.app == owner) && lastIsOpaque && r.isDestroyable()) {
                        if (ActivityManagerDebugConfig.DEBUG_SWITCH) {
                            Slog.v(TAG_SWITCH, "Destroying " + r + " in state " + r.state + " resumed=" + this.mResumedActivity + " pausing=" + this.mPausingActivity + " for reason " + reason);
                        }
                        if (destroyActivityLocked(r, true, reason)) {
                            activityRemoved = true;
                        }
                    }
                }
            }
        }
        if (activityRemoved) {
            this.mStackSupervisor.resumeFocusedStackTopActivityLocked();
        }
    }

    final boolean safelyDestroyActivityLocked(ActivityRecord r, String reason) {
        if (!r.isDestroyable()) {
            return false;
        }
        if (ActivityManagerDebugConfig.DEBUG_SWITCH) {
            Slog.v(TAG_SWITCH, "Destroying " + r + " in state " + r.state + " resumed=" + this.mResumedActivity + " pausing=" + this.mPausingActivity + " for reason " + reason);
        }
        return destroyActivityLocked(r, true, reason);
    }

    final int releaseSomeActivitiesLocked(ProcessRecord app, ArraySet<TaskRecord> tasks, String reason) {
        if (ActivityManagerDebugConfig.DEBUG_RELEASE) {
            Slog.d(TAG_RELEASE, "Trying to release some activities in " + app);
        }
        int maxTasks = tasks.size() / 4;
        if (maxTasks < 1) {
            maxTasks = 1;
        }
        int numReleased = 0;
        int taskNdx = 0;
        while (taskNdx < this.mTaskHistory.size() && maxTasks > 0) {
            TaskRecord task = (TaskRecord) this.mTaskHistory.get(taskNdx);
            if (tasks.contains(task)) {
                if (ActivityManagerDebugConfig.DEBUG_RELEASE) {
                    Slog.d(TAG_RELEASE, "Looking for activities to release in " + task);
                }
                int curNum = 0;
                ArrayList<ActivityRecord> activities = task.mActivities;
                int actNdx = 0;
                while (actNdx < activities.size()) {
                    ActivityRecord activity = (ActivityRecord) activities.get(actNdx);
                    if (activity.app == app && activity.isDestroyable()) {
                        if (ActivityManagerDebugConfig.DEBUG_RELEASE) {
                            Slog.v(TAG_RELEASE, "Destroying " + activity + " in state " + activity.state + " resumed=" + this.mResumedActivity + " pausing=" + this.mPausingActivity + " for reason " + reason);
                        }
                        destroyActivityLocked(activity, true, reason);
                        if (activities.get(actNdx) != activity) {
                            actNdx--;
                        }
                        curNum++;
                    }
                    actNdx++;
                }
                if (curNum > 0) {
                    numReleased += curNum;
                    maxTasks--;
                    if (this.mTaskHistory.get(taskNdx) != task) {
                        taskNdx--;
                    }
                }
            }
            taskNdx++;
        }
        if (ActivityManagerDebugConfig.DEBUG_RELEASE) {
            Slog.d(TAG_RELEASE, "Done releasing: did " + numReleased + " activities");
        }
        return numReleased;
    }

    /* JADX WARNING: Removed duplicated region for block: B:64:0x01f2  */
    /* JADX WARNING: Removed duplicated region for block: B:67:0x021a  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    final boolean destroyActivityLocked(ActivityRecord r, boolean removeFromApp, String reason) {
        if (ActivityManagerDebugConfig.DEBUG_SWITCH || ActivityManagerDebugConfig.DEBUG_CLEANUP) {
            Slog.v(TAG_SWITCH, "Removing activity from " + reason + ": token=" + r + ", app=" + (r.app != null ? r.app.processName : "(null)"));
        }
        EventLog.writeEvent(EventLogTags.AM_DESTROY_ACTIVITY, new Object[]{Integer.valueOf(r.userId), Integer.valueOf(System.identityHashCode(r)), Integer.valueOf(r.getTask().taskId), r.shortComponentName, reason});
        boolean removedFromHistory = false;
        cleanUpActivityLocked(r, false, false);
        boolean hadApp = r.app != null;
        if (hadApp) {
            if (removeFromApp) {
                r.app.activities.remove(r);
                if (this.mService.mHeavyWeightProcess == r.app && r.app.activities.size() <= 0) {
                    this.mService.mHeavyWeightProcess = null;
                    this.mService.mHandler.sendEmptyMessage(25);
                }
                if (r.app.activities.isEmpty()) {
                    this.mService.mServices.updateServiceConnectionActivitiesLocked(r.app);
                    this.mService.updateLruProcessLocked(r.app, false, null);
                    this.mService.updateOomAdjLocked();
                }
            }
            boolean skipDestroy = false;
            try {
                if (ActivityManagerDebugConfig.DEBUG_SWITCH) {
                    Slog.i(TAG_SWITCH, "Destroying: " + r);
                }
                r.app.thread.scheduleDestroyActivity(r.appToken, r.finishing, r.configChangeFlags);
                String p = r.packageName;
                ActivityRecord top = null;
                ActivityStack topStack = this.mStackSupervisor.getFocusedStack();
                if (topStack != null) {
                    top = topStack.topRunningActivityLocked();
                }
                if (top == null || p == null || !ActivityManagerService.OPPO_LAUNCHER.equals(top.packageName) || (p.equals(top.packageName) ^ 1) == 0) {
                    SystemProperties.set("debug.sys.oppo.keytime", "0");
                    r.nowVisible = false;
                    if (r.finishing || (skipDestroy ^ 1) == 0) {
                        if (ActivityManagerDebugConfig.DEBUG_STATES) {
                            Slog.v(TAG_STATES, "Moving to DESTROYED: " + r + " (destroy skipped)");
                        }
                        r.state = ActivityState.DESTROYED;
                        if (ActivityManagerDebugConfig.DEBUG_APP) {
                            Slog.v(TAG_APP, "Clearing app during destroy for activity " + r);
                        }
                        r.app = null;
                    } else {
                        if (ActivityManagerDebugConfig.DEBUG_STATES) {
                            Slog.v(TAG_STATES, "Moving to DESTROYING: " + r + " (destroy requested)");
                        }
                        r.state = ActivityState.DESTROYING;
                        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(102, r), 10000);
                    }
                } else {
                    if (OppoSplitWindowAppReader.isInTwoSecond()) {
                        this.mService.mHandler.removeMessages(511, p);
                        this.mService.mHandler.sendMessageDelayed(this.mService.mHandler.obtainMessage(511, top.userId, -1, p), 1000);
                    }
                    r.nowVisible = false;
                    if (r.finishing) {
                    }
                    if (ActivityManagerDebugConfig.DEBUG_STATES) {
                    }
                    r.state = ActivityState.DESTROYED;
                    if (ActivityManagerDebugConfig.DEBUG_APP) {
                    }
                    r.app = null;
                }
            } catch (Exception e) {
                if (ActivityManagerDebugConfig.DEBUG_AMS) {
                    Slog.w(TAG, "Exception thrown during finish", e);
                }
                if (r.finishing) {
                    removeActivityFromHistoryLocked(r, reason + " exceptionInScheduleDestroy");
                    removedFromHistory = true;
                    skipDestroy = true;
                }
            }
        } else if (r.finishing) {
            removeActivityFromHistoryLocked(r, reason + " hadNoApp");
            removedFromHistory = true;
        } else {
            if (ActivityManagerDebugConfig.DEBUG_STATES) {
                Slog.v(TAG_STATES, "Moving to DESTROYED: " + r + " (no app)");
            }
            r.state = ActivityState.DESTROYED;
            if (ActivityManagerDebugConfig.DEBUG_APP) {
                Slog.v(TAG_APP, "Clearing app during destroy for activity " + r);
            }
            r.app = null;
        }
        r.configChangeFlags = 0;
        if (!this.mLRUActivities.remove(r) && hadApp) {
            Slog.w(TAG, "Activity " + r + " being finished, but not in LRU list");
        }
        return removedFromHistory;
    }

    final void activityDestroyedLocked(IBinder token, String reason) {
        long origId = Binder.clearCallingIdentity();
        try {
            ActivityRecord r = ActivityRecord.forTokenLocked(token);
            if (r != null) {
                this.mHandler.removeMessages(102, r);
            }
            if (ActivityManagerDebugConfig.DEBUG_CONTAINERS) {
                Slog.d(TAG_CONTAINERS, "activityDestroyedLocked: r=" + r);
            }
            if (isInStackLocked(r) != null && r.state == ActivityState.DESTROYING) {
                cleanUpActivityLocked(r, true, false);
                removeActivityFromHistoryLocked(r, reason);
            }
            this.mStackSupervisor.resumeFocusedStackTopActivityLocked();
        } finally {
            Binder.restoreCallingIdentity(origId);
        }
    }

    private void removeHistoryRecordsForAppLocked(ArrayList<ActivityRecord> list, ProcessRecord app, String listName) {
        int i = list.size();
        if (ActivityManagerDebugConfig.DEBUG_CLEANUP) {
            Slog.v(TAG_CLEANUP, "Removing app " + app + " from list " + listName + " with " + i + " entries");
        }
        while (i > 0) {
            i--;
            ActivityRecord r = (ActivityRecord) list.get(i);
            if (ActivityManagerDebugConfig.DEBUG_CLEANUP) {
                Slog.v(TAG_CLEANUP, "Record #" + i + " " + r);
            }
            if (r.app == app) {
                if (ActivityManagerDebugConfig.DEBUG_CLEANUP) {
                    Slog.v(TAG_CLEANUP, "---> REMOVING this entry!");
                }
                list.remove(i);
                removeTimeoutsForActivityLocked(r);
            }
        }
    }

    private boolean removeHistoryRecordsForAppLocked(ProcessRecord app) {
        removeHistoryRecordsForAppLocked(this.mLRUActivities, app, "mLRUActivities");
        removeHistoryRecordsForAppLocked(this.mStackSupervisor.mStoppingActivities, app, "mStoppingActivities");
        removeHistoryRecordsForAppLocked(this.mStackSupervisor.mGoingToSleepActivities, app, "mGoingToSleepActivities");
        removeHistoryRecordsForAppLocked(this.mStackSupervisor.mActivitiesWaitingForVisibleActivity, app, "mActivitiesWaitingForVisibleActivity");
        removeHistoryRecordsForAppLocked(this.mStackSupervisor.mFinishingActivities, app, "mFinishingActivities");
        boolean hasVisibleActivities = false;
        int i = numActivities();
        if (ActivityManagerDebugConfig.DEBUG_CLEANUP) {
            Slog.v(TAG_CLEANUP, "Removing app " + app + " from history with " + i + " entries");
        }
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ArrayList<ActivityRecord> activities = ((TaskRecord) this.mTaskHistory.get(taskNdx)).mActivities;
            for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                ActivityRecord r = (ActivityRecord) activities.get(activityNdx);
                i--;
                if (ActivityManagerDebugConfig.DEBUG_CLEANUP) {
                    Slog.v(TAG_CLEANUP, "Record #" + i + " " + r + ": app=" + r.app);
                }
                if (r.app == app) {
                    boolean remove;
                    if (r.visible) {
                        hasVisibleActivities = true;
                    }
                    if ((!r.haveState && (r.stateNotNeeded ^ 1) != 0) || r.finishing) {
                        remove = true;
                    } else if (!r.visible && r.launchCount > 2 && r.lastLaunchTime > SystemClock.uptimeMillis() - 60000) {
                        remove = true;
                    } else if ("com.tencent.mm/.plugin.webview.ui.tools.WebViewUI".equals(r.shortComponentName) && app != null && "com.tencent.mm:tools".equals(app.processName)) {
                        Slog.v(TAG, "process com.tencent.mm:tools died,remove the WebViewUI activity");
                        remove = true;
                    } else if (!r.visible || r.launchCount <= 10) {
                        remove = false;
                    } else {
                        Slog.v(TAG, "launched activity too many times, we remove it");
                        remove = true;
                    }
                    if (remove) {
                        if (ActivityManagerDebugConfig.DEBUG_ADD_REMOVE || ActivityManagerDebugConfig.DEBUG_CLEANUP) {
                            Slog.i(TAG_ADD_REMOVE, "Removing activity " + r + " from stack at " + i + ": haveState=" + r.haveState + " stateNotNeeded=" + r.stateNotNeeded + " finishing=" + r.finishing + " state=" + r.state + " callers=" + Debug.getCallers(5));
                        }
                        if (!r.finishing) {
                            Slog.w(TAG, "Force removing " + r + ": app died, no saved state");
                            EventLog.writeEvent(EventLogTags.AM_FINISH_ACTIVITY, new Object[]{Integer.valueOf(r.userId), Integer.valueOf(System.identityHashCode(r)), Integer.valueOf(r.getTask().taskId), r.shortComponentName, "proc died without state saved"});
                            if (r.state == ActivityState.RESUMED) {
                                this.mService.updateUsageStats(r, false);
                            }
                        }
                    } else {
                        if (ActivityManagerDebugConfig.DEBUG_APP) {
                            Slog.v(TAG_APP, "Clearing app during removeHistory for activity " + r);
                        }
                        r.app = null;
                        r.nowVisible = r.visible;
                        if (!r.haveState) {
                            if (ActivityManagerDebugConfig.DEBUG_SAVED_STATE) {
                                Slog.i(TAG_SAVED_STATE, "App died, clearing saved state of " + r);
                            }
                            r.icicle = null;
                        }
                    }
                    cleanUpActivityLocked(r, true, true);
                    if (remove) {
                        removeActivityFromHistoryLocked(r, "appDied");
                    }
                }
            }
        }
        return hasVisibleActivities;
    }

    private void updateTransitLocked(int transit, ActivityOptions options) {
        if (options != null) {
            ActivityRecord r = topRunningActivityLocked();
            if (r == null || r.state == ActivityState.RESUMED) {
                ActivityOptions.abort(options);
            } else {
                r.updateOptionsLocked(options);
            }
        }
        this.mWindowManager.prepareAppTransition(transit, false);
    }

    private void updateTaskMovement(TaskRecord task, boolean toFront) {
        if (task.isPersistable) {
            task.mLastTimeMoved = System.currentTimeMillis();
            if (!toFront) {
                task.mLastTimeMoved *= -1;
            }
        }
        this.mStackSupervisor.invalidateTaskLayers();
    }

    void moveHomeStackTaskToTop() {
        int top = this.mTaskHistory.size() - 1;
        for (int taskNdx = top; taskNdx >= 0; taskNdx--) {
            TaskRecord task = (TaskRecord) this.mTaskHistory.get(taskNdx);
            if (task.taskType == 1) {
                if (ActivityManagerDebugConfig.DEBUG_TASKS || ActivityManagerDebugConfig.DEBUG_STACK) {
                    Slog.d(TAG_STACK, "moveHomeStackTaskToTop: moving " + task);
                }
                this.mTaskHistory.remove(taskNdx);
                this.mTaskHistory.add(top, task);
                updateTaskMovement(task, true);
                return;
            }
        }
    }

    final void moveTaskToFrontLocked(TaskRecord tr, boolean noAnimation, ActivityOptions options, AppTimeTracker timeTracker, String reason) {
        if (ActivityManagerDebugConfig.DEBUG_SWITCH) {
            Slog.v(TAG_SWITCH, "moveTaskToFront: " + tr);
        }
        if (this.mStackSupervisor.getStack(3, false, false) == null || !OppoSplitWindowAppReader.isInTwoSecond() || tr == null || tr.getStackId() != 0) {
            ActivityStack topStack = getTopStackOnDisplay();
            ActivityRecord topActivity = topStack != null ? topStack.topActivity() : null;
            int numTasks = this.mTaskHistory.size();
            int index = this.mTaskHistory.indexOf(tr);
            if (numTasks == 0 || index < 0) {
                if (noAnimation) {
                    ActivityOptions.abort(options);
                } else {
                    updateTransitLocked(10, options);
                }
                return;
            }
            if (tr.affinity != null && tr.affinity.equals(ActivityManagerService.OPPO_LAUNCHER)) {
                hideAppSurfaceUntilKeyguardAppears();
            }
            if (timeTracker != null) {
                for (int i = tr.mActivities.size() - 1; i >= 0; i--) {
                    ((ActivityRecord) tr.mActivities.get(i)).appTimeTracker = timeTracker;
                }
            }
            insertTaskAtTop(tr, null);
            ActivityRecord top = tr.getTopActivity();
            if (top == null || (top.okToShowLocked() ^ 1) != 0) {
                addRecentActivityLocked(top);
                ActivityOptions.abort(options);
                return;
            }
            ActivityRecord r = topRunningActivityLocked();
            if (r != null) {
                this.mStackSupervisor.moveFocusableActivityStackToFrontLocked(r, reason);
            } else {
                Slog.v(TAG_SWITCH, "no activity in tr,move to front,tr " + tr);
                moveToFront(reason);
            }
            if (ActivityManagerDebugConfig.DEBUG_TRANSITION) {
                Slog.v(TAG_TRANSITION, "Prepare to front transition: task=" + tr);
            }
            if (noAnimation) {
                this.mWindowManager.prepareAppTransition(0, false);
                if (r != null) {
                    this.mNoAnimActivities.add(r);
                }
                ActivityOptions.abort(options);
            } else {
                updateTransitLocked(10, options);
            }
            if (canEnterPipOnTaskSwitch(topActivity, tr, null, options)) {
                topActivity.supportsEnterPipOnTaskSwitch = true;
            }
            this.mStackSupervisor.resumeFocusedStackTopActivityLocked();
            EventLog.writeEvent(EventLogTags.AM_TASK_TO_FRONT, new Object[]{Integer.valueOf(tr.userId), Integer.valueOf(tr.taskId)});
            this.mService.mTaskChangeNotificationController.notifyTaskMovedToFront(tr.taskId);
            return;
        }
        if (this.mService.getGlobalConfiguration() != null && this.mService.getGlobalConfiguration().orientation == 1) {
            this.mWindowManager.setSplitTimeout(500);
            this.mWindowManager.startFreezingScreen(0, 0);
        }
        this.mStackSupervisor.moveTasksToFullscreenStackLocked(3, true);
    }

    /* JADX WARNING: Removed duplicated region for block: B:127:0x032f  */
    /* JADX WARNING: Removed duplicated region for block: B:102:0x0295  */
    /* JADX WARNING: Removed duplicated region for block: B:135:0x038f  */
    /* JADX WARNING: Removed duplicated region for block: B:130:0x0340  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    final boolean moveTaskToBackLocked(int taskId) {
        TaskRecord tr = taskForIdLocked(taskId);
        if (tr == null) {
            Slog.i(TAG, "moveTaskToBack: bad taskId=" + taskId);
            return false;
        }
        if (ActivityManagerDebugConfig.DEBUG_TASKS || ActivityManagerDebugConfig.DEBUG_STACK) {
            Slog.i(TAG, "moveTaskToBack: " + tr);
        }
        if (this.mStackSupervisor.isLockedTask(tr)) {
            this.mStackSupervisor.showLockTaskToast();
            return false;
        }
        if (this.mStackSupervisor.isFrontStackOnDisplay(this) && this.mService.mController != null) {
            ActivityRecord next = topRunningActivityLocked(null, taskId);
            if (ActivityManagerDebugConfig.DEBUG_TASKS || ActivityManagerDebugConfig.DEBUG_STACK) {
                Slog.i(TAG, "moveTaskToBackLocked next : " + next);
            }
            if (next == null) {
                next = topRunningActivityLocked(null, 0);
            }
            if (next != null) {
                boolean moveOK = true;
                try {
                    moveOK = this.mService.mController.activityResuming(next.packageName);
                } catch (RemoteException e) {
                    this.mService.mController = null;
                    Watchdog.getInstance().setActivityController(null);
                }
                if (!moveOK) {
                    return false;
                }
            }
        }
        if (ActivityManagerDebugConfig.DEBUG_TRANSITION) {
            Slog.v(TAG_TRANSITION, "Prepare to back transition: task=" + taskId);
        }
        ComponentName topapp = this.mService.getTopAppName();
        String packagename = "";
        if (topapp != null) {
            packagename = topapp.getPackageName();
        }
        if (tr.affinity != null && ((tr.affinity.equals("com.android.incallui") && packagename.equals("com.android.incallui")) || (tr.affinity.equals("com.coloros.speechassist") && packagename.equals("com.coloros.speechassist")))) {
            hideAppSurfaceUntilKeyguardAppears();
        }
        boolean prevIsHome = false;
        boolean canGoHome = !tr.isHomeTask() ? tr.isOverHomeStack() : false;
        if (ActivityManagerDebugConfig.DEBUG_TASKS) {
            Slog.w(TAG, "tr " + tr + " canGoHome " + canGoHome);
        }
        if (canGoHome) {
            TaskRecord nextTask = getNextTask(tr);
            if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                Slog.w(TAG, "nextTask " + nextTask);
            }
            if (nextTask != null) {
                nextTask.setTaskToReturnTo(tr.getTaskToReturnTo());
            } else {
                prevIsHome = true;
            }
        }
        if (this.mTaskHistory.indexOf(tr) != 0) {
            this.mTaskHistory.remove(tr);
            this.mTaskHistory.add(0, tr);
            updateTaskMovement(tr, false);
            if (canGoHome && this.mService.mBooted && this.mFullscreen && this.mStackId != 2 && this.mStackId != 4) {
                this.mWindowManager.setLockOrientation(true);
            }
            this.mWindowManager.prepareAppTransition(11, false);
            this.mWindowContainerController.positionChildAtBottom(tr.getWindowContainerController());
        }
        if (this.mStackId == 4) {
            this.mStackSupervisor.removeStackLocked(4);
            return true;
        }
        TaskRecord task;
        int numTasks = this.mTaskHistory.size();
        for (int taskNdx = numTasks - 1; taskNdx >= 1; taskNdx--) {
            task = (TaskRecord) this.mTaskHistory.get(taskNdx);
            if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                Slog.w(TAG, "taskNdx " + taskNdx + " task " + task);
            }
            if (task.isOverHomeStack()) {
                if (task.topRunningActivityLocked() != null) {
                    if (ActivityManagerDebugConfig.DEBUG_TASKS || ActivityManagerDebugConfig.DEBUG_AMS) {
                        Slog.w(TAG, "Find valid task " + task + " over home");
                    }
                    task = this.mResumedActivity == null ? this.mResumedActivity.getTask() : null;
                    if (ActivityManagerDebugConfig.DEBUG_TASKS || ActivityManagerDebugConfig.DEBUG_STACK) {
                        Slog.i(TAG, "moveTaskToBackLocked mResumedActivity : " + this.mResumedActivity + " task " + task + " tr.isOverHomeStack() " + tr.isOverHomeStack());
                    }
                    if (prevIsHome && ((task != tr || !canGoHome) && (numTasks > 1 || !isOnHomeDisplay()))) {
                        if (!(this.mFullscreen || this.mStackId != 1 || this.mStackSupervisor.getStack(3, false, false) == null || this.mStackSupervisor.mFocusedStack == null || !OppoSplitWindowAppReader.isInTwoSecond())) {
                            ActivityRecord topRecord = this.mStackSupervisor.mFocusedStack.topRunningActivityLocked();
                            if (!(topRecord == null || topRecord.task == null || (topRecord.task.supportsSplitScreen() ^ 1) == 0)) {
                                if (this.mService.getGlobalConfiguration() != null && this.mService.getGlobalConfiguration().orientation == 1) {
                                    this.mWindowManager.setSplitTimeout(500);
                                    this.mWindowManager.startFreezingScreen(0, 0);
                                }
                                this.mStackSupervisor.moveTasksToFullscreenStackLocked(3, true);
                                return true;
                            }
                        }
                        if (!this.mStackSupervisor.resumeFocusedStackTopActivityLocked()) {
                            this.mWindowManager.executeAppTransition();
                        }
                        this.mWindowManager.setLockOrientation(false);
                        return true;
                    } else if (this.mService.mBooting && (this.mService.mBooted ^ 1) != 0) {
                        return false;
                    } else {
                        tr.setTaskToReturnTo(0);
                        if (this.mStackId != 2) {
                            startPausingLocked(false, false, null, true, "exit-freeform");
                            this.mStackSupervisor.moveTasksToFullscreenStackLocked(2, false);
                            ActivityStack focusCandidate = this.mStackSupervisor.getNextFocusableStackLocked(this);
                            this.mStackSupervisor.setFocusStackUnchecked("moveTaskToFullLocked", focusCandidate);
                            this.mStackSupervisor.resumeFocusedStackTopActivityLocked();
                            if (ActivityManagerDebugConfig.DEBUG_FOCUS) {
                                Slog.v(TAG, "oppo freeform: focusCandidate==" + focusCandidate);
                            }
                            return true;
                        } else if (this.mFullscreen || !OppoSplitWindowAppReader.isInTwoSecond() || this.mStackSupervisor.getStack(3, false, false) == null) {
                            boolean resumedHome = this.mStackSupervisor.resumeHomeStackTask(null, "moveTaskToBack");
                            this.mWindowManager.setLockOrientation(false);
                            return resumedHome;
                        } else {
                            if (this.mService.getGlobalConfiguration() != null && this.mService.getGlobalConfiguration().orientation == 1) {
                                this.mWindowManager.setSplitTimeout(500);
                                this.mWindowManager.startFreezingScreen(0, 0);
                            }
                            if (isDockedStack()) {
                                this.mStackSupervisor.moveTasksToFullscreenStackLocked(3, false);
                            } else {
                                this.mStackSupervisor.moveTasksToFullscreenStackLocked(3, true);
                            }
                            Slog.v(TAG, "split screen moveTaskToBackLocked full screen: " + this);
                            return true;
                        }
                    }
                }
                Slog.w(TAG, "invalid task " + task);
            }
            if (taskNdx == 1) {
                task.setTaskToReturnTo(1);
            }
        }
        if (this.mResumedActivity == null) {
        }
        Slog.i(TAG, "moveTaskToBackLocked mResumedActivity : " + this.mResumedActivity + " task " + task + " tr.isOverHomeStack() " + tr.isOverHomeStack());
        if (prevIsHome) {
        }
        if (this.mService.mBooting) {
        }
        tr.setTaskToReturnTo(0);
        if (this.mStackId != 2) {
        }
    }

    private ActivityStack getTopStackOnDisplay() {
        ArrayList<ActivityStack> stacks = getDisplay().mStacks;
        return stacks.isEmpty() ? null : (ActivityStack) stacks.get(stacks.size() - 1);
    }

    static void logStartActivity(int tag, ActivityRecord r, TaskRecord task) {
        Uri data = r.intent.getData();
        String strData = data != null ? data.toSafeString() : null;
        EventLog.writeEvent(tag, new Object[]{Integer.valueOf(r.userId), Integer.valueOf(System.identityHashCode(r)), Integer.valueOf(task.taskId), r.shortComponentName, r.intent.getAction(), r.intent.getType(), strData, Integer.valueOf(r.intent.getFlags())});
    }

    void ensureVisibleActivitiesConfigurationLocked(ActivityRecord start, boolean preserveWindow) {
        if (start != null && (start.visible ^ 1) == 0) {
            boolean behindFullscreen = false;
            boolean updatedConfig = false;
            for (int taskIndex = this.mTaskHistory.indexOf(start.getTask()); taskIndex >= 0; taskIndex--) {
                TaskRecord task = (TaskRecord) this.mTaskHistory.get(taskIndex);
                ArrayList<ActivityRecord> activities = task.mActivities;
                int activityIndex = start.getTask() == task ? activities.indexOf(start) : activities.size() - 1;
                while (activityIndex >= 0) {
                    ActivityRecord r = (ActivityRecord) activities.get(activityIndex);
                    updatedConfig |= r.ensureActivityConfigurationLocked(0, preserveWindow);
                    if (r.fullscreen) {
                        behindFullscreen = true;
                        break;
                    }
                    activityIndex--;
                }
                if (behindFullscreen) {
                    break;
                }
            }
            if (updatedConfig) {
                this.mStackSupervisor.resumeFocusedStackTopActivityLocked();
            }
        }
    }

    public void requestResize(Rect bounds) {
        this.mService.resizeStack(this.mStackId, bounds, true, false, false, -1);
    }

    void resize(Rect bounds, Rect tempTaskBounds, Rect tempTaskInsetBounds) {
        bounds = TaskRecord.validateBounds(bounds);
        if (updateBoundsAllowed(bounds, tempTaskBounds, tempTaskInsetBounds)) {
            Rect taskBounds = tempTaskBounds != null ? tempTaskBounds : bounds;
            Rect insetBounds = tempTaskInsetBounds != null ? tempTaskInsetBounds : taskBounds;
            this.mTmpBounds.clear();
            this.mTmpConfigs.clear();
            this.mTmpInsetBounds.clear();
            for (int i = this.mTaskHistory.size() - 1; i >= 0; i--) {
                TaskRecord task = (TaskRecord) this.mTaskHistory.get(i);
                if (task.isResizeable()) {
                    if (this.mStackId == 2) {
                        this.mTmpRect2.set(task.mBounds);
                        fitWithinBounds(this.mTmpRect2, bounds);
                        task.updateOverrideConfiguration(this.mTmpRect2);
                    } else {
                        task.updateOverrideConfiguration(taskBounds, insetBounds);
                    }
                }
                this.mTmpConfigs.put(task.taskId, task.getOverrideConfiguration());
                this.mTmpBounds.put(task.taskId, task.mBounds);
                if (tempTaskInsetBounds != null) {
                    this.mTmpInsetBounds.put(task.taskId, tempTaskInsetBounds);
                }
            }
            this.mFullscreen = this.mWindowContainerController.resize(bounds, this.mTmpConfigs, this.mTmpBounds, this.mTmpInsetBounds);
            setBounds(bounds);
        }
    }

    private static void fitWithinBounds(Rect bounds, Rect stackBounds) {
        if (stackBounds != null && !stackBounds.contains(bounds)) {
            if (bounds.left < stackBounds.left || bounds.right > stackBounds.right) {
                int maxRight = stackBounds.right - (stackBounds.width() / 3);
                int horizontalDiff = stackBounds.left - bounds.left;
                if ((horizontalDiff < 0 && bounds.left >= maxRight) || bounds.left + horizontalDiff >= maxRight) {
                    horizontalDiff = maxRight - bounds.left;
                }
                bounds.left += horizontalDiff;
                bounds.right += horizontalDiff;
            }
            if (bounds.top < stackBounds.top || bounds.bottom > stackBounds.bottom) {
                int maxBottom = stackBounds.bottom - (stackBounds.height() / 3);
                int verticalDiff = stackBounds.top - bounds.top;
                if ((verticalDiff < 0 && bounds.top >= maxBottom) || bounds.top + verticalDiff >= maxBottom) {
                    verticalDiff = maxBottom - bounds.top;
                }
                bounds.top += verticalDiff;
                bounds.bottom += verticalDiff;
            }
        }
    }

    boolean willActivityBeVisibleLocked(IBinder token) {
        ActivityRecord r;
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ArrayList<ActivityRecord> activities = ((TaskRecord) this.mTaskHistory.get(taskNdx)).mActivities;
            for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                r = (ActivityRecord) activities.get(activityNdx);
                if (r.appToken == token) {
                    return true;
                }
                if (r.fullscreen && (r.finishing ^ 1) != 0) {
                    return false;
                }
            }
        }
        r = ActivityRecord.forTokenLocked(token);
        if (r == null) {
            return false;
        }
        if (r.finishing) {
            Slog.e(TAG, "willActivityBeVisibleLocked: Returning false, would have returned true for r=" + r);
        }
        return r.finishing ^ 1;
    }

    void closeSystemDialogsLocked() {
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ArrayList<ActivityRecord> activities = ((TaskRecord) this.mTaskHistory.get(taskNdx)).mActivities;
            for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                ActivityRecord r = (ActivityRecord) activities.get(activityNdx);
                if ((r.info.flags & 256) != 0) {
                    finishActivityLocked(r, 0, null, "close-sys", true);
                }
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:35:0x007d  */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x0070  */
    /* JADX WARNING: Missing block: B:10:0x0042, code:
            if (r17.contains(r2.realActivity.getClassName()) == false) goto L_0x0044;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    boolean finishDisabledPackageActivitiesLocked(String packageName, Set<String> filterByClasses, boolean doit, boolean evenPersistent, int userId) {
        boolean didSomething = false;
        TaskRecord lastTask = null;
        ComponentName homeActivity = null;
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ArrayList<ActivityRecord> activities = ((TaskRecord) this.mTaskHistory.get(taskNdx)).mActivities;
            int numActivities = activities.size();
            int activityNdx = 0;
            while (activityNdx < numActivities && activityNdx < activities.size()) {
                boolean sameComponent;
                ActivityRecord r = (ActivityRecord) activities.get(activityNdx);
                if (r.packageName.equals(packageName)) {
                    if (filterByClasses != null) {
                    }
                    sameComponent = true;
                    if ((userId == -1 || r.userId == userId) && ((sameComponent || r.getTask() == lastTask) && (r.app == null || evenPersistent || (r.app.persistent ^ 1) != 0))) {
                        if (!doit) {
                            if (r.isHomeActivity()) {
                                if (homeActivity == null || !homeActivity.equals(r.realActivity)) {
                                    homeActivity = r.realActivity;
                                } else {
                                    Slog.i(TAG, "Skip force-stop again " + r);
                                }
                            }
                            didSomething = true;
                            Slog.i(TAG, "  Force finishing activity " + r);
                            if (sameComponent) {
                                if (r.app != null) {
                                    r.app.removed = true;
                                }
                                r.app = null;
                            }
                            lastTask = r.getTask();
                            if (finishActivityLocked(r, 0, null, "force-stop", true)) {
                                numActivities--;
                                activityNdx--;
                            }
                        } else if (!r.finishing) {
                            return true;
                        }
                    }
                    activityNdx++;
                }
                sameComponent = packageName == null && r.userId == userId;
                if (!doit) {
                }
                activityNdx++;
            }
        }
        return didSomething;
    }

    void getTasksLocked(List<RunningTaskInfo> list, int callingUid, boolean allowed) {
        boolean focusedStack = this.mStackSupervisor.getFocusedStack() == this;
        boolean topTask = true;
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            TaskRecord task = (TaskRecord) this.mTaskHistory.get(taskNdx);
            if (task.getTopActivity() != null) {
                ActivityRecord r = null;
                ActivityRecord top = null;
                int numActivities = 0;
                int numRunning = 0;
                ArrayList<ActivityRecord> activities = task.mActivities;
                if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                    Slog.v(TAG, "task " + task + " activities " + activities + " isEmpty " + activities.isEmpty());
                }
                if (allowed || (task.isHomeTask() ^ 1) == 0 || task.effectiveUid == callingUid) {
                    for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                        ActivityRecord tmp = (ActivityRecord) activities.get(activityNdx);
                        if (!tmp.finishing) {
                            r = tmp;
                            if (top == null || top.state == ActivityState.INITIALIZING) {
                                top = tmp;
                                numRunning = 0;
                                numActivities = 0;
                                if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                                    Slog.v(TAG, "top " + tmp);
                                }
                            }
                            numActivities++;
                            if (!(tmp.app == null || tmp.app.thread == null)) {
                                numRunning++;
                            }
                        }
                    }
                    RunningTaskInfo ci = new RunningTaskInfo();
                    ci.id = task.taskId;
                    ci.stackId = this.mStackId;
                    ci.baseActivity = r.intent.getComponent();
                    ci.topActivity = top.intent.getComponent();
                    ci.isTopDockable = top.supportsSplitScreen();
                    if (!(ci.isTopDockable || ci.topActivity == null || !OppoSplitWindowAppReader.getInstance().isInConfigList(ci.topActivity.getPackageName()))) {
                        ci.isTopDockable = true;
                    }
                    ci.lastActiveTime = task.lastActiveTime;
                    if (focusedStack && topTask) {
                        long currentTime = System.currentTimeMillis();
                        if (currentTime > ci.lastActiveTime) {
                            ci.lastActiveTime = currentTime;
                        }
                        topTask = false;
                    }
                    if (top.getTask() != null) {
                        ci.description = top.getTask().lastDescription;
                    }
                    ci.numActivities = numActivities;
                    ci.numRunning = numRunning;
                    ci.supportsSplitScreenMultiWindow = task.supportsSplitScreen();
                    ci.resizeMode = task.mResizeMode;
                    if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                        Slog.v(TAG, "ci.topActivity " + ci.topActivity + " lastActiveTime " + ci.lastActiveTime);
                    }
                    list.add(ci);
                }
            }
        }
    }

    void unhandledBackLocked() {
        int top = this.mTaskHistory.size() - 1;
        if (ActivityManagerDebugConfig.DEBUG_SWITCH) {
            Slog.d(TAG_SWITCH, "Performing unhandledBack(): top activity at " + top);
        }
        if (top >= 0) {
            ArrayList<ActivityRecord> activities = ((TaskRecord) this.mTaskHistory.get(top)).mActivities;
            int activityTop = activities.size() - 1;
            if (activityTop >= 0) {
                finishActivityLocked((ActivityRecord) activities.get(activityTop), 0, null, "unhandled-back", true);
            }
        }
    }

    boolean handleAppDiedLocked(ProcessRecord app) {
        if (this.mPausingActivity != null && this.mPausingActivity.app == app) {
            if (ActivityManagerDebugConfig.DEBUG_PAUSE || ActivityManagerDebugConfig.DEBUG_CLEANUP) {
                Slog.v(TAG_PAUSE, "App died while pausing: " + this.mPausingActivity);
            }
            this.mPausingActivity = null;
        }
        if (this.mLastPausedActivity != null && this.mLastPausedActivity.app == app) {
            this.mLastPausedActivity = null;
            this.mLastNoHistoryActivity = null;
        }
        return removeHistoryRecordsForAppLocked(app);
    }

    void handleAppCrashLocked(ProcessRecord app) {
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ArrayList<ActivityRecord> activities = ((TaskRecord) this.mTaskHistory.get(taskNdx)).mActivities;
            int activityNdx = activities.size() - 1;
            while (activityNdx >= 0 && activityNdx < activities.size()) {
                try {
                    ActivityRecord r = (ActivityRecord) activities.get(activityNdx);
                    if (r.app == app) {
                        Slog.w(TAG, "  Force finishing activity 4 " + r.intent.getComponent().flattenToShortString());
                        r.app = null;
                        finishCurrentActivityLocked(r, 0, false);
                    }
                } catch (IndexOutOfBoundsException exce) {
                    Slog.wtf(TAG, "handleAppCrashLocked error:", exce);
                }
                activityNdx--;
            }
        }
    }

    boolean dumpActivitiesLocked(FileDescriptor fd, PrintWriter pw, boolean dumpAll, boolean dumpClient, String dumpPackage, boolean needSep, String header) {
        boolean printed = false;
        if (OppoFreeFormManagerService.getInstance().mDynamicDebug) {
            Slog.d(TAG, "dumpActivitiesLocked mTaskHistory = " + this.mTaskHistory);
        }
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            TaskRecord task = (TaskRecord) this.mTaskHistory.get(taskNdx);
            if (OppoFreeFormManagerService.getInstance().mDynamicDebug) {
                Slog.d(TAG, "dumpActivitiesLocked task = " + task);
            }
            boolean z = dumpAll ^ 1;
            String str = "    Task id #" + task.taskId + "\n" + "    mFullscreen=" + task.mFullscreen + "\n" + "    mBounds=" + task.mBounds + "\n" + "    mMinWidth=" + task.mMinWidth + "\n" + "    mMinHeight=" + task.mMinHeight + "\n" + "    mLastNonFullscreenBounds=" + task.mLastNonFullscreenBounds;
            printed |= ActivityStackSupervisor.dumpHistoryList(fd, pw, ((TaskRecord) this.mTaskHistory.get(taskNdx)).mActivities, "    ", "Hist", true, z, dumpClient, dumpPackage, needSep, header, str);
            if (printed) {
                header = null;
            }
        }
        return printed;
    }

    ArrayList<ActivityRecord> getDumpActivitiesLocked(String name) {
        ArrayList<ActivityRecord> activities = new ArrayList();
        int taskNdx;
        if ("all".equals(name)) {
            for (taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
                activities.addAll(((TaskRecord) this.mTaskHistory.get(taskNdx)).mActivities);
            }
        } else if ("top".equals(name)) {
            int top = this.mTaskHistory.size() - 1;
            if (top >= 0) {
                ArrayList<ActivityRecord> list = ((TaskRecord) this.mTaskHistory.get(top)).mActivities;
                int listTop = list.size() - 1;
                if (listTop >= 0) {
                    activities.add((ActivityRecord) list.get(listTop));
                }
            }
        } else {
            ItemMatcher matcher = new ItemMatcher();
            matcher.build(name);
            for (taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
                for (ActivityRecord r1 : ((TaskRecord) this.mTaskHistory.get(taskNdx)).mActivities) {
                    if (matcher.match(r1, r1.intent.getComponent())) {
                        activities.add(r1);
                    }
                }
            }
        }
        return activities;
    }

    ActivityRecord restartPackage(String packageName) {
        ActivityRecord starting = topRunningActivityLocked();
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ArrayList<ActivityRecord> activities = ((TaskRecord) this.mTaskHistory.get(taskNdx)).mActivities;
            for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                ActivityRecord a = (ActivityRecord) activities.get(activityNdx);
                if (a.info.packageName.equals(packageName)) {
                    a.forceNewConfig = true;
                    if (starting != null && a == starting && a.visible) {
                        a.startFreezingScreenLocked(starting.app, 256);
                    }
                }
            }
        }
        return starting;
    }

    void removeTask(TaskRecord task, String reason) {
        removeTask(task, reason, 0);
    }

    void removeTask(TaskRecord task, String reason, int mode) {
        for (ActivityRecord record : task.mActivities) {
            onActivityRemovedFromStack(record);
        }
        int taskNdx = this.mTaskHistory.indexOf(task);
        int topTaskNdx = this.mTaskHistory.size() - 1;
        if (task.isOverHomeStack() && taskNdx < topTaskNdx) {
            TaskRecord nextTask = (TaskRecord) this.mTaskHistory.get(taskNdx + 1);
            if (!(nextTask.isOverHomeStack() || (nextTask.isOverAssistantStack() ^ 1) == 0)) {
                nextTask.setTaskToReturnTo(1);
            }
        }
        if (ActivityManagerDebugConfig.DEBUG_STACK) {
            Slog.i(TAG_STACK, "removeTask: " + task + " " + Debug.getCallers(8));
        }
        this.mTaskHistory.remove(task);
        removeActivitiesFromLRUListLocked(task);
        updateTaskMovement(task, true);
        if (mode == 0 && task.mActivities.isEmpty()) {
            boolean isVoiceSession = task.voiceSession != null;
            if (isVoiceSession) {
                try {
                    task.voiceSession.taskFinished(task.intent, task.taskId);
                } catch (RemoteException e) {
                }
            }
            if (task.autoRemoveFromRecents() || isVoiceSession) {
                this.mRecentTasks.remove(task);
                task.removedFromRecents();
            }
            task.removeWindowContainer();
        }
        if (this.mTaskHistory.isEmpty()) {
            if (ActivityManagerDebugConfig.DEBUG_STACK) {
                Slog.i(TAG_STACK, "removeTask: removing stack=" + this);
            }
            if (isOnHomeDisplay() && mode != 2 && this.mStackSupervisor.isFocusedStack(this)) {
                String myReason = reason + " leftTaskHistoryEmpty";
                if (this.mFullscreen || (adjustFocusToNextFocusableStackLocked(myReason) ^ 1) != 0) {
                    boolean isMoveHome = true;
                    if (this.mStackId == 2) {
                        ActivityStack focusCandidate = this.mStackSupervisor.getNextFocusableStackLocked(this);
                        if (focusCandidate != null && focusCandidate.mStackId == 1) {
                            isMoveHome = false;
                        }
                    }
                    if (isMoveHome) {
                        this.mStackSupervisor.moveHomeStackToFront(myReason);
                    }
                }
            }
            if (this.mStacks != null) {
                this.mStacks.remove(this);
                this.mStacks.add(0, this);
            }
            if (!isHomeOrRecentsStack()) {
                remove();
            }
        }
        task.setStack(null);
        if (this.mStackId == 4) {
            this.mService.mTaskChangeNotificationController.notifyActivityUnpinned();
        }
    }

    TaskRecord createTaskRecord(int taskId, ActivityInfo info, Intent intent, IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor, boolean toTop, int type) {
        TaskRecord task = new TaskRecord(this.mService, taskId, info, intent, voiceSession, voiceInteractor, type);
        addTask(task, toTop, "createTaskRecord");
        boolean isLockscreenShown = this.mService.mStackSupervisor.mKeyguardController.isKeyguardShowing(this.mDisplayId != -1 ? this.mDisplayId : 0);
        if (!(layoutTaskInStack(task, info.windowLayout) || this.mBounds == null || !task.isResizeable() || (isLockscreenShown ^ 1) == 0)) {
            task.updateOverrideConfiguration(this.mBounds);
        }
        task.createWindowContainer(toTop, (info.flags & 1024) != 0);
        return task;
    }

    boolean layoutTaskInStack(TaskRecord task, WindowLayout windowLayout) {
        if (this.mTaskPositioner == null) {
            return false;
        }
        this.mTaskPositioner.updateDefaultBounds(task, this.mTaskHistory, windowLayout);
        return true;
    }

    ArrayList<TaskRecord> getAllTasks() {
        return new ArrayList(this.mTaskHistory);
    }

    void addTask(TaskRecord task, boolean toTop, String reason) {
        addTask(task, toTop ? Integer.MAX_VALUE : 0, true, reason);
        if (toTop) {
            this.mWindowContainerController.positionChildAtTop(task.getWindowContainerController(), true);
        }
    }

    void addTask(TaskRecord task, int position, boolean schedulePictureInPictureModeChange, String reason) {
        this.mTaskHistory.remove(task);
        position = getAdjustedPositionForTask(task, position, null);
        boolean toTop = position >= this.mTaskHistory.size();
        ActivityStack prevStack = preAddTask(task, reason, toTop);
        this.mTaskHistory.add(position, task);
        task.setStack(this);
        if (toTop) {
            updateTaskReturnToForTopInsertion(task);
        }
        updateTaskMovement(task, toTop);
        postAddTask(task, prevStack, schedulePictureInPictureModeChange);
    }

    void positionChildAt(TaskRecord task, int index) {
        if (task.getStack() != this) {
            throw new IllegalArgumentException("AS.positionChildAt: task=" + task + " is not a child of stack=" + this + " current parent=" + task.getStack());
        }
        task.updateOverrideConfigurationForStack(this);
        ActivityRecord topRunningActivity = task.topRunningActivityLocked();
        boolean wasResumed = topRunningActivity == task.getStack().mResumedActivity;
        insertTaskAtPosition(task, index);
        task.setStack(this);
        postAddTask(task, null, true);
        if (wasResumed) {
            if (this.mResumedActivity != null) {
                Log.wtf(TAG, "mResumedActivity was already set when moving mResumedActivity from other stack to this stack mResumedActivity=" + this.mResumedActivity + " other mResumedActivity=" + topRunningActivity);
            }
            this.mResumedActivity = topRunningActivity;
        }
        ensureActivitiesVisibleLocked(null, 0, false);
        this.mStackSupervisor.resumeFocusedStackTopActivityLocked();
    }

    private ActivityStack preAddTask(TaskRecord task, String reason, boolean toTop) {
        ActivityStack prevStack = task.getStack();
        if (!(prevStack == null || prevStack == this)) {
            prevStack.removeTask(task, reason, toTop ? 2 : 1);
        }
        return prevStack;
    }

    private void postAddTask(TaskRecord task, ActivityStack prevStack, boolean schedulePictureInPictureModeChange) {
        if (schedulePictureInPictureModeChange && prevStack != null) {
            this.mStackSupervisor.scheduleUpdatePictureInPictureModeIfNeeded(task, prevStack);
        } else if (task.voiceSession != null) {
            try {
                task.voiceSession.taskStarted(task.intent, task.taskId);
            } catch (RemoteException e) {
            }
        }
    }

    void moveToFrontAndResumeStateIfNeeded(ActivityRecord r, boolean moveToFront, boolean setResume, boolean setPause, String reason) {
        if (moveToFront) {
            boolean needNotifyAppSwitch = false;
            ActivityRecord lastActivity = null;
            if (ActivityManagerDebugConfig.DEBUG_STATES) {
                Slog.i(TAG, "moveToFrontAndResumeStateIfNeeded  r = " + r + "  reason = " + reason);
            }
            if (!(this.mResumedActivity == null || reason == null || (!"exitFreeformMode".equals(reason) && !"moveTasksToFullscreenStack - onTop".equals(reason)))) {
                if (ActivityManagerDebugConfig.DEBUG_STATES) {
                    Slog.i(TAG, "exitFreeformMode or moveTasksToFullscreenStack - onTop r = " + r);
                }
                lastActivity = this.mResumedActivity;
                needNotifyAppSwitch = true;
            }
            if (setResume) {
                this.mResumedActivity = r;
                updateLRUListLocked(r);
            }
            if (setPause) {
                this.mPausingActivity = r;
                schedulePauseTimeout(r);
            }
            moveToFront(reason);
            if (needNotifyAppSwitch) {
                OppoAppSwitchManager.getInstance().handleActivitySwitch(this.mService.mContext, lastActivity, r, false, null);
            }
        }
    }

    public int getStackId() {
        return this.mStackId;
    }

    public String toString() {
        return "ActivityStack{" + Integer.toHexString(System.identityHashCode(this)) + " stackId=" + this.mStackId + ", " + this.mTaskHistory.size() + " tasks}";
    }

    void onLockTaskPackagesUpdatedLocked() {
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ((TaskRecord) this.mTaskHistory.get(taskNdx)).setLockTaskAuth();
        }
    }

    private void startSecurityPayService(ActivityRecord prev, ActivityRecord next) {
        String prevPkg = "";
        String nextPkg = "";
        boolean isWechatPay = false;
        boolean isExitWechatPay = false;
        if (next != null) {
            if (!(prev == null || prev.packageName == null)) {
                prevPkg = prev.packageName;
            }
            if (!(next == null || next.packageName == null)) {
                nextPkg = next.packageName;
            }
            if (!prevPkg.equals(nextPkg) && next.packageName != null && ("com.tencent.mm".equals(next.packageName) ^ 1) != 0) {
                try {
                    if (AppGlobals.getPackageManager().isSecurePayApp(next.packageName)) {
                        Slog.d(TAG, "remuse secure pay app : " + next.packageName);
                        startSecurePayIntent(next.packageName, prevPkg);
                        return;
                    }
                } catch (RemoteException e) {
                    Slog.i(TAG, "Cannot find remote package");
                }
            } else if (next.packageName != null && "com.tencent.mm".equals(next.packageName)) {
                String clsName = next.realActivity.getClassName();
                if (clsName != null && OppoListManager.getInstance().getSecurePayActivityList().contains(clsName)) {
                    isWechatPay = true;
                }
                if (!(prevPkg == null || !"com.tencent.mm".equals(prevPkg) || prev == null)) {
                    String preClsName = prev.realActivity.getClassName();
                    if (preClsName != null && OppoListManager.getInstance().getSecurePayActivityList().contains(preClsName)) {
                        isExitWechatPay = true;
                    }
                }
            }
            if (isWechatPay) {
                Slog.d(TAG, "remuse wechat pay app : " + next.packageName);
                try {
                    if (AppGlobals.getPackageManager().isSecurePayApp(next.packageName)) {
                        Slog.d(TAG, "remuse secure pay app : " + next.packageName);
                        startSecurePayIntent(next.packageName, prevPkg);
                        return;
                    }
                } catch (RemoteException e2) {
                    Slog.i(TAG, "Cannot find remote package");
                }
            }
            if (isExitWechatPay) {
                try {
                    if (AppGlobals.getPackageManager().isSecurePayApp(prevPkg)) {
                        Slog.d(TAG, "exitWeChatPay");
                        exitWeChatPay(prevPkg);
                    }
                } catch (RemoteException e3) {
                    Slog.i(TAG, "Cannot find remote package");
                }
            }
        }
    }

    private void startSecurePayIntent(String pkgName, String prevPkgName) {
        Intent it = new Intent("oppo.intent.action.SECURE_PAY_SCAN_RISK");
        it.setPackage("com.coloros.securepay");
        it.putExtra("extra_key_app_pkg", pkgName);
        it.putExtra("extra_key_pre_pkg", prevPkgName);
        this.mService.mContext.startService(it);
    }

    private void exitWeChatPay(String pkgName) {
        Intent intent = new Intent("oppo.intent.action.SECURE_PAY_SCAN_RISK");
        intent.setPackage("com.coloros.securepay");
        intent.putExtra("extra_key_app_pkg", pkgName);
        intent.putExtra("extra_key_exit", true);
        this.mService.mContext.startService(intent);
    }

    void executeAppTransition(ActivityOptions options) {
        this.mWindowManager.executeAppTransition();
        this.mNoAnimActivities.clear();
        ActivityOptions.abort(options);
    }

    public ComponentName getTopAppName() {
        return this.mComponentName;
    }

    boolean shouldSleepActivities() {
        ActivityDisplay display = getDisplay();
        return display != null ? display.isSleeping() : this.mService.isSleepingLocked();
    }

    boolean shouldSleepOrShutDownActivities() {
        return !shouldSleepActivities() ? this.mService.isShuttingDownLocked() : true;
    }

    protected ArrayList<TaskRecord> getTaskHistory() {
        return this.mTaskHistory;
    }

    public ComponentName getDockTopAppName() {
        return this.mDockComponentName;
    }

    public ComponentName getResumedCpn() {
        if (this.mResumedActivity != null) {
            return this.mResumedActivity.realActivity;
        }
        return null;
    }

    public ApplicationInfo getResumedAppInfo() {
        if (this.mResumedActivity != null) {
            return this.mResumedActivity.appInfo;
        }
        return null;
    }

    void hideAppSurfaceUntilKeyguardAppears() {
        if (this.mStackSupervisor.mKeyguardController.isKeyguardLocked()) {
            if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
                Slog.d(TAG, "mFinishWhenLockedState:FINISH_WHEN_LOCKED_BEGIN");
            }
            this.mService.mFinishWhenLockedState = 1;
            this.mService.mHandler.sendMessageDelayed(this.mService.mHandler.obtainMessage(420), 500);
        }
    }
}
