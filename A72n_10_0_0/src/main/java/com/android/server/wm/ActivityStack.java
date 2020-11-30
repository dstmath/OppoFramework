package com.android.server.wm;

import android.app.ActivityOptions;
import android.app.AppGlobals;
import android.app.IActivityController;
import android.app.IApplicationThread;
import android.app.RemoteAction;
import android.app.ResultInfo;
import android.app.WindowConfiguration;
import android.app.servertransaction.ActivityLifecycleItem;
import android.app.servertransaction.ActivityResultItem;
import android.app.servertransaction.ClientTransaction;
import android.app.servertransaction.ClientTransactionItem;
import android.app.servertransaction.DestroyActivityItem;
import android.app.servertransaction.NewIntentItem;
import android.app.servertransaction.PauseActivityItem;
import android.app.servertransaction.ResumeActivityItem;
import android.app.servertransaction.StopActivityItem;
import android.app.servertransaction.WindowVisibilityItem;
import android.common.OppoFeatureCache;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Binder;
import android.os.Debug;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.OppoUsageManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.service.voice.IVoiceInteractionSession;
import android.text.format.Time;
import android.util.ArraySet;
import android.util.EventLog;
import android.util.IntArray;
import android.util.Log;
import android.util.Slog;
import android.util.proto.ProtoOutputStream;
import android.view.IApplicationToken;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.app.IVoiceInteractor;
import com.android.internal.util.function.pooled.PooledLambda;
import com.android.server.SystemService;
import com.android.server.Watchdog;
import com.android.server.am.ActivityManagerService;
import com.android.server.am.AppTimeTracker;
import com.android.server.am.EventLogTags;
import com.android.server.am.IColorHansManager;
import com.android.server.am.IColorMultiAppManager;
import com.android.server.am.PendingIntentRecord;
import com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils;
import com.android.server.pm.DumpState;
import com.android.server.theia.NoFocusWindow;
import com.android.server.util.ColorZoomWindowManagerHelper;
import com.android.server.wm.RootActivityContainer;
import com.color.antivirus.IColorAntiVirusBehaviorManager;
import com.oppo.hypnus.Hypnus;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ActivityStack extends OppoBaseActivityStack {
    static final int DESTROY_ACTIVITIES_MSG = 105;
    private static final int DESTROY_TIMEOUT = 10000;
    static final int DESTROY_TIMEOUT_MSG = 102;
    static final int FINISH_AFTER_PAUSE = 1;
    static final int FINISH_AFTER_VISIBLE = 2;
    static final int FINISH_IMMEDIATELY = 0;
    static final int LAUNCH_TICK = 500;
    static final int LAUNCH_TICK_MSG = 103;
    private static final int MAX_ENSURE_VISIBILE_DEPTH = 16;
    private static final int MAX_STOPPING_TO_FORCE = 3;
    private static final int PAUSE_TIMEOUT = 500;
    static final int PAUSE_TIMEOUT_MSG = 101;
    @VisibleForTesting
    protected static final int REMOVE_TASK_MODE_DESTROYING = 0;
    static final int REMOVE_TASK_MODE_MOVING = 1;
    static final int REMOVE_TASK_MODE_MOVING_TO_TOP = 2;
    private static final boolean SHOW_APP_STARTING_PREVIEW = true;
    static final int STACK_VISIBILITY_INVISIBLE = 2;
    static final int STACK_VISIBILITY_VISIBLE = 0;
    static final int STACK_VISIBILITY_VISIBLE_BEHIND_TRANSLUCENT = 1;
    private static final int STOP_TIMEOUT = 11000;
    static final int STOP_TIMEOUT_MSG = 104;
    private static final String TAG = "ActivityTaskManager";
    private static final String TAG_ADD_REMOVE = (TAG + ActivityTaskManagerDebugConfig.POSTFIX_ADD_REMOVE);
    private static final String TAG_APP = (TAG + ActivityTaskManagerDebugConfig.POSTFIX_APP);
    private static final String TAG_CLEANUP = (TAG + ActivityTaskManagerDebugConfig.POSTFIX_CLEANUP);
    private static final String TAG_CONTAINERS = (TAG + ActivityTaskManagerDebugConfig.POSTFIX_CONTAINERS);
    private static final String TAG_PAUSE = (TAG + ActivityTaskManagerDebugConfig.POSTFIX_PAUSE);
    private static final String TAG_RELEASE = (TAG + ActivityTaskManagerDebugConfig.POSTFIX_RELEASE);
    private static final String TAG_RESULTS = (TAG + ActivityTaskManagerDebugConfig.POSTFIX_RESULTS);
    private static final String TAG_SAVED_STATE = (TAG + ActivityTaskManagerDebugConfig.POSTFIX_SAVED_STATE);
    private static final String TAG_STACK = (TAG + ActivityTaskManagerDebugConfig.POSTFIX_STACK);
    private static final String TAG_STATES = (TAG + ActivityTaskManagerDebugConfig.POSTFIX_STATES);
    private static final String TAG_SWITCH = (TAG + ActivityTaskManagerDebugConfig.POSTFIX_SWITCH);
    private static final String TAG_TASKS = (TAG + ActivityTaskManagerDebugConfig.POSTFIX_TASKS);
    private static final String TAG_TRANSITION = (TAG + ActivityTaskManagerDebugConfig.POSTFIX_TRANSITION);
    private static final String TAG_USER_LEAVING = (TAG + ActivityTaskManagerDebugConfig.POSTFIX_USER_LEAVING);
    private static final String TAG_VISIBILITY = (TAG + ActivityTaskManagerDebugConfig.POSTFIX_VISIBILITY);
    private static final long TRANSLUCENT_CONVERSION_TIMEOUT = 2000;
    static final int TRANSLUCENT_TIMEOUT_MSG = 106;
    private static final List<String> mDefaultAlbumBrowserList = Arrays.asList("com.tencent.mobileqq/.filemanager.activity.FMActivity", "com.sina.weibo/.photoalbum.PicAlbumNewActivity", "com.taobao.taobao/com.taobao.message.ui.biz.mediapick.view.MultiPickGalleryActivity");
    public static Hypnus mHyp = null;
    boolean mConfigWillChange;
    int mCurrentUser;
    private final Rect mDeferredBounds = new Rect();
    private final Rect mDeferredDisplayedBounds = new Rect();
    int mDisplayId;
    private int mEnsureActivityVisibilityDepth = 0;
    boolean mForceHidden = false;
    final Handler mHandler;
    boolean mInResumeTopActivity = false;
    private final ArrayList<ActivityRecord> mLRUActivities = new ArrayList<>();
    ActivityRecord mLastNoHistoryActivity = null;
    ActivityRecord mLastPausedActivity = null;
    private ComponentName mLastRecordCmpName = null;
    private String mLastRecordPkgName = null;
    ActivityRecord mPausingActivity = null;
    private int mRestoreOverrideWindowingMode = 0;
    ActivityRecord mResumedActivity = null;
    protected final RootActivityContainer mRootActivityContainer;
    final ActivityTaskManagerService mService;
    final int mStackId;
    protected final ActivityStackSupervisor mStackSupervisor;
    private final ArrayList<TaskRecord> mTaskHistory = new ArrayList<>();
    TaskStack mTaskStack;
    private final ArrayList<ActivityRecord> mTmpActivities = new ArrayList<>();
    private final ActivityOptions mTmpOptions = ActivityOptions.makeBasic();
    private final Rect mTmpRect = new Rect();
    private final Rect mTmpRect2 = new Rect();
    private boolean mTopActivityOccludesKeyguard;
    private ActivityRecord mTopDismissingKeyguardActivity;
    ActivityRecord mTranslucentActivityWaiting = null;
    ArrayList<ActivityRecord> mUndrawnActivitiesBelowTopTranslucent = new ArrayList<>();
    private boolean mUpdateBoundsDeferred;
    private boolean mUpdateBoundsDeferredCalled;
    private boolean mUpdateDisplayedBoundsDeferredCalled;
    final WindowManagerService mWindowManager;

    /* access modifiers changed from: package-private */
    public enum ActivityState {
        INITIALIZING,
        RESUMED,
        PAUSING,
        PAUSED,
        STOPPING,
        STOPPED,
        FINISHING,
        DESTROYING,
        DESTROYED,
        RESTARTING_PROCESS
    }

    @interface StackVisibility {
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.wm.ConfigurationContainer
    public int getChildCount() {
        return this.mTaskHistory.size();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.wm.ConfigurationContainer
    public TaskRecord getChildAt(int index) {
        return this.mTaskHistory.get(index);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.wm.ConfigurationContainer
    public ActivityDisplay getParent() {
        return getDisplay();
    }

    /* access modifiers changed from: package-private */
    public void setParent(ActivityDisplay parent) {
        if (getParent() != parent) {
            this.mDisplayId = parent.mDisplayId;
            onParentChanged();
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.wm.ConfigurationContainer
    public void onParentChanged() {
        ActivityDisplay display = getParent();
        if (display != null) {
            getConfiguration().windowConfiguration.setRotation(display.getWindowConfiguration().getRotation());
        }
        super.onParentChanged();
        if (display != null && inSplitScreenPrimaryWindowingMode()) {
            getStackDockedModeBounds(null, null, this.mTmpRect, this.mTmpRect2);
            this.mStackSupervisor.resizeDockedStackLocked(getRequestedOverrideBounds(), this.mTmpRect, this.mTmpRect2, null, null, true);
        }
        this.mRootActivityContainer.updateUIDsPresentOnDisplay();
    }

    private static class ScheduleDestroyArgs {
        final WindowProcessController mOwner;
        final String mReason;

        ScheduleDestroyArgs(WindowProcessController owner, String reason) {
            this.mOwner = owner;
            this.mReason = reason;
        }
    }

    private class ActivityStackHandler extends Handler {
        ActivityStackHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            IApplicationToken.Stub stub = null;
            switch (msg.what) {
                case 101:
                    ActivityRecord r = (ActivityRecord) msg.obj;
                    Slog.w(ActivityStack.TAG, "Activity pause timeout for " + r);
                    synchronized (ActivityStack.this.mService.mGlobalLock) {
                        try {
                            WindowManagerService.boostPriorityForLockedSection();
                            if (r.hasProcess()) {
                                ActivityTaskManagerService activityTaskManagerService = ActivityStack.this.mService;
                                WindowProcessController windowProcessController = r.app;
                                long j = r.pauseTime;
                                activityTaskManagerService.logAppTooSlow(windowProcessController, j, "pausing " + r);
                            }
                            ActivityStack.this.activityPausedLocked(r.appToken, true);
                        } finally {
                            WindowManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    return;
                case 102:
                    ActivityRecord r2 = (ActivityRecord) msg.obj;
                    Slog.w(ActivityStack.TAG, "Activity destroy timeout for " + r2);
                    synchronized (ActivityStack.this.mService.mGlobalLock) {
                        try {
                            WindowManagerService.boostPriorityForLockedSection();
                            ActivityStack activityStack = ActivityStack.this;
                            if (r2 != null) {
                                stub = r2.appToken;
                            }
                            activityStack.activityDestroyedLocked((IBinder) stub, "destroyTimeout");
                        } finally {
                            WindowManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    return;
                case 103:
                    ActivityRecord r3 = (ActivityRecord) msg.obj;
                    synchronized (ActivityStack.this.mService.mGlobalLock) {
                        try {
                            WindowManagerService.boostPriorityForLockedSection();
                            if (r3.continueLaunchTickingLocked()) {
                                ActivityTaskManagerService activityTaskManagerService2 = ActivityStack.this.mService;
                                WindowProcessController windowProcessController2 = r3.app;
                                long j2 = r3.launchTickTime;
                                activityTaskManagerService2.logAppTooSlow(windowProcessController2, j2, "launching " + r3);
                            }
                        } finally {
                            WindowManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    return;
                case 104:
                    ActivityRecord r4 = (ActivityRecord) msg.obj;
                    Slog.w(ActivityStack.TAG, "Activity stop timeout for " + r4);
                    synchronized (ActivityStack.this.mService.mGlobalLock) {
                        try {
                            WindowManagerService.boostPriorityForLockedSection();
                            if (r4.isInHistory()) {
                                r4.activityStoppedLocked(null, null, null);
                            }
                        } finally {
                            WindowManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    return;
                case 105:
                    ScheduleDestroyArgs args = (ScheduleDestroyArgs) msg.obj;
                    synchronized (ActivityStack.this.mService.mGlobalLock) {
                        try {
                            WindowManagerService.boostPriorityForLockedSection();
                            ActivityStack.this.destroyActivitiesLocked(args.mOwner, args.mReason);
                        } finally {
                            WindowManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    return;
                case 106:
                    synchronized (ActivityStack.this.mService.mGlobalLock) {
                        try {
                            WindowManagerService.boostPriorityForLockedSection();
                            ActivityStack.this.notifyActivityDrawnLocked(null);
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

    /* access modifiers changed from: package-private */
    public int numActivities() {
        int count = 0;
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            count += this.mTaskHistory.get(taskNdx).mActivities.size();
        }
        return count;
    }

    ActivityStack(ActivityDisplay display, int stackId, ActivityStackSupervisor supervisor, int windowingMode, int activityType, boolean onTop) {
        this.mStackSupervisor = supervisor;
        this.mService = supervisor.mService;
        this.mRootActivityContainer = this.mService.mRootActivityContainer;
        this.mHandler = new ActivityStackHandler(supervisor.mLooper);
        this.mWindowManager = this.mService.mWindowManager;
        this.mStackId = stackId;
        this.mCurrentUser = this.mService.mAmInternal.getCurrentUserId();
        this.mDisplayId = display.mDisplayId;
        setActivityType(activityType);
        createTaskStack(display.mDisplayId, onTop, this.mTmpRect2);
        setWindowingMode(windowingMode, false, false, false, false, true);
        display.addChild(this, onTop ? Integer.MAX_VALUE : Integer.MIN_VALUE);
    }

    /* access modifiers changed from: package-private */
    public void createTaskStack(int displayId, boolean onTop, Rect outBounds) {
        DisplayContent dc = this.mWindowManager.mRoot.getDisplayContent(displayId);
        if (dc != null) {
            WindowManagerService windowManagerService = this.mWindowManager;
            this.mTaskStack = windowManagerService.createTaskStack(windowManagerService, this.mStackId, this);
            dc.setStackOnDisplay(this.mStackId, onTop, this.mTaskStack);
            if (this.mTaskStack.matchParentBounds()) {
                outBounds.setEmpty();
            } else {
                this.mTaskStack.getRawBounds(outBounds);
            }
        } else {
            throw new IllegalArgumentException("Trying to add stackId=" + this.mStackId + " to unknown displayId=" + displayId);
        }
    }

    /* access modifiers changed from: package-private */
    public TaskStack getTaskStack() {
        return this.mTaskStack;
    }

    /* access modifiers changed from: package-private */
    public void onActivityStateChanged(ActivityRecord record, ActivityState state, String reason) {
        if (record == this.mResumedActivity && state != ActivityState.RESUMED) {
            setResumedActivity(null, reason + " - onActivityStateChanged");
        }
        if (state == ActivityState.RESUMED) {
            if (ActivityTaskManagerDebugConfig.DEBUG_STACK) {
                String str = TAG_STACK;
                Slog.v(str, "set resumed activity to:" + record + " reason:" + reason);
            }
            setResumedActivity(record, reason + " - onActivityStateChanged");
            if (record == this.mRootActivityContainer.getTopResumedActivity()) {
                this.mService.setResumedActivityUncheckLocked(record, reason);
            }
            this.mStackSupervisor.mRecentTasks.add(record.getTaskRecord());
        }
    }

    @Override // com.android.server.wm.ConfigurationContainer
    public void onConfigurationChanged(Configuration newParentConfig) {
        boolean hasNewOverrideBounds;
        ActivityDisplay display;
        TaskRecord topTask;
        int prevWindowingMode = getWindowingMode();
        boolean prevIsAlwaysOnTop = isAlwaysOnTop();
        int prevRotation = getWindowConfiguration().getRotation();
        int prevDensity = getConfiguration().densityDpi;
        int prevScreenW = getConfiguration().screenWidthDp;
        int prevScreenH = getConfiguration().screenHeightDp;
        Rect newBounds = this.mTmpRect;
        getBounds(newBounds);
        super.onConfigurationChanged(newParentConfig);
        ActivityDisplay display2 = getDisplay();
        if (display2 == null) {
            return;
        }
        if (getTaskStack() != null) {
            boolean windowingModeChanged = prevWindowingMode != getWindowingMode();
            int overrideWindowingMode = getRequestedOverrideWindowingMode();
            boolean hasNewOverrideBounds2 = false;
            if (overrideWindowingMode == 2) {
                hasNewOverrideBounds2 = getTaskStack().calculatePinnedBoundsForConfigChange(newBounds);
            } else if (!matchParentBounds()) {
                int newRotation = getWindowConfiguration().getRotation();
                boolean rotationChanged = prevRotation != newRotation;
                if (rotationChanged) {
                    display2.mDisplayContent.rotateBounds(newParentConfig.windowConfiguration.getBounds(), prevRotation, newRotation, newBounds);
                    hasNewOverrideBounds2 = true;
                }
                if ((overrideWindowingMode == 3 || overrideWindowingMode == 4) && !(!rotationChanged && !windowingModeChanged && prevDensity == getConfiguration().densityDpi && prevScreenW == getConfiguration().screenWidthDp && prevScreenH == getConfiguration().screenHeightDp)) {
                    getTaskStack().calculateDockedBoundsForConfigChange(newParentConfig, newBounds);
                    hasNewOverrideBounds2 = true;
                }
            }
            if (windowingModeChanged) {
                if (overrideWindowingMode == 3) {
                    getStackDockedModeBounds(null, null, newBounds, this.mTmpRect2);
                    setTaskDisplayedBounds(null);
                    setTaskBounds(newBounds);
                    setBounds(newBounds);
                    newBounds.set(newBounds);
                } else if (overrideWindowingMode == 4) {
                    Rect dockedBounds = display2.getSplitScreenPrimaryStack().getBounds();
                    if (display2.mDisplayContent.getDockedDividerController().isMinimizedDock() && (topTask = display2.getSplitScreenPrimaryStack().topTask()) != null) {
                        dockedBounds = topTask.getBounds();
                    }
                    getStackDockedModeBounds(dockedBounds, null, newBounds, this.mTmpRect2);
                    hasNewOverrideBounds2 = true;
                }
                display2.onStackWindowingModeChanged(this);
            }
            if (OppoFeatureCache.get(IColorZoomWindowManager.DEFAULT).updateZoonWindowTaskBound(getConfiguration(), this)) {
                hasNewOverrideBounds = false;
            } else {
                hasNewOverrideBounds = hasNewOverrideBounds2;
            }
            if (hasNewOverrideBounds) {
                display = display2;
                this.mRootActivityContainer.resizeStack(this, new Rect(newBounds), null, null, true, true, true);
            } else {
                display = display2;
            }
            if (prevIsAlwaysOnTop != isAlwaysOnTop()) {
                display.positionChildAtTop(this, false);
            }
        }
    }

    @Override // com.android.server.wm.OppoBaseActivityStack, com.android.server.wm.ConfigurationContainer
    public void setWindowingMode(int windowingMode) {
        setWindowingMode(windowingMode, false, false, false, false, false);
    }

    private static boolean isTransientWindowingMode(int windowingMode) {
        return windowingMode == 3 || windowingMode == 4;
    }

    /* access modifiers changed from: package-private */
    public void setWindowingMode(int preferredWindowingMode, boolean animate, boolean showRecents, boolean enteringSplitScreenMode, boolean deferEnsuringVisibility, boolean creating) {
        this.mWindowManager.inSurfaceTransaction(new Runnable(preferredWindowingMode, animate, showRecents, enteringSplitScreenMode, deferEnsuringVisibility, creating) {
            /* class com.android.server.wm.$$Lambda$ActivityStack$7heVv97BezfdSlHS0oo3lugbypI */
            private final /* synthetic */ int f$1;
            private final /* synthetic */ boolean f$2;
            private final /* synthetic */ boolean f$3;
            private final /* synthetic */ boolean f$4;
            private final /* synthetic */ boolean f$5;
            private final /* synthetic */ boolean f$6;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
                this.f$5 = r6;
                this.f$6 = r7;
            }

            public final void run() {
                ActivityStack.this.lambda$setWindowingMode$0$ActivityStack(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: setWindowingModeInSurfaceTransaction */
    public void lambda$setWindowingMode$0$ActivityStack(int preferredWindowingMode, boolean animate, boolean showRecents, boolean enteringSplitScreenMode, boolean deferEnsuringVisibility, boolean creating) {
        int windowingMode;
        int likelyResolvedMode;
        Throwable th;
        int likelyResolvedMode2;
        int currentMode = getWindowingMode();
        int currentOverrideMode = getRequestedOverrideWindowingMode();
        ActivityDisplay display = getDisplay();
        TaskRecord topTask = topTask();
        ActivityStack splitScreenStack = display.getSplitScreenPrimaryStack();
        int windowingMode2 = preferredWindowingMode;
        if (preferredWindowingMode == 0 && isTransientWindowingMode(currentMode)) {
            windowingMode2 = this.mRestoreOverrideWindowingMode;
        }
        this.mTmpOptions.setLaunchWindowingMode(windowingMode2);
        if (!creating) {
            windowingMode2 = display.validateWindowingMode(windowingMode2, null, topTask, getActivityType());
        }
        if (splitScreenStack == this && windowingMode2 == 4) {
            windowingMode = this.mRestoreOverrideWindowingMode;
        } else {
            windowingMode = windowingMode2;
        }
        boolean alreadyInSplitScreenMode = display.hasSplitScreenPrimaryStack();
        boolean sendNonResizeableNotification = !enteringSplitScreenMode;
        if (alreadyInSplitScreenMode && windowingMode == 1 && sendNonResizeableNotification && isActivityTypeStandardOrUndefined()) {
            if ((preferredWindowingMode == 3 || preferredWindowingMode == 4) || creating) {
                this.mService.getTaskChangeNotificationController().notifyActivityDismissingDockedStack();
                ActivityStack primarySplitStack = display.getSplitScreenPrimaryStack();
                primarySplitStack.lambda$setWindowingMode$0$ActivityStack(0, false, false, false, true, primarySplitStack == this ? creating : false);
            }
        }
        if (currentMode == windowingMode) {
            getRequestedOverrideConfiguration().windowConfiguration.setWindowingMode(windowingMode);
            return;
        }
        WindowManagerService wm = this.mService.mWindowManager;
        ActivityRecord topActivity = getTopActivity();
        if (windowingMode == 0) {
            ConfigurationContainer parent = getParent();
            if (parent != null) {
                likelyResolvedMode2 = parent.getWindowingMode();
            } else {
                likelyResolvedMode2 = 1;
            }
            likelyResolvedMode = likelyResolvedMode2;
        } else {
            likelyResolvedMode = windowingMode;
        }
        if (sendNonResizeableNotification && likelyResolvedMode != 1 && topActivity != null && topActivity.isNonResizableOrForcedResizable() && preferredWindowingMode != WindowConfiguration.WINDOWING_MODE_ZOOM && !topActivity.noDisplay) {
            this.mService.getTaskChangeNotificationController().notifyActivityForcedResizable(topTask.taskId, 1, topActivity.appInfo.packageName);
        }
        wm.deferSurfaceLayout();
        if (!animate && topActivity != null) {
            try {
                this.mStackSupervisor.mNoAnimActivities.add(topActivity);
            } catch (Throwable th2) {
                th = th2;
            }
        }
        try {
            super.setWindowingMode(windowingMode);
            windowingMode = getWindowingMode();
            if (creating) {
                if (showRecents && !alreadyInSplitScreenMode && this.mDisplayId == 0 && windowingMode == 3) {
                    display.getOrCreateStack(4, 3, true).moveToFront("setWindowingMode");
                    this.mService.mH.post(new Runnable() {
                        /* class com.android.server.wm.$$Lambda$ActivityStack$3dUQUxLPwVLC7t9Mqjl5VgILT4 */

                        public final void run() {
                            ActivityStack.this.lambda$setWindowingModeInSurfaceTransaction$1$ActivityStack();
                        }
                    });
                }
                wm.continueSurfaceLayout();
            } else if (windowingMode == 2 || currentMode == 2) {
                throw new IllegalArgumentException("Changing pinned windowing mode not currently supported");
            } else if (windowingMode != 3 || splitScreenStack == null) {
                if (isTransientWindowingMode(windowingMode) && !isTransientWindowingMode(currentMode)) {
                    this.mRestoreOverrideWindowingMode = currentOverrideMode;
                }
                this.mTmpRect2.setEmpty();
                if (windowingMode != 1) {
                    if (this.mTaskStack.matchParentBounds()) {
                        this.mTmpRect2.setEmpty();
                    } else {
                        this.mTaskStack.getRawBounds(this.mTmpRect2);
                    }
                }
                if (!Objects.equals(getRequestedOverrideBounds(), this.mTmpRect2)) {
                    resize(this.mTmpRect2, null, null);
                }
                if (windowingMode == 1 && currentMode == 5) {
                    resize(this.mTmpRect2, null, null);
                }
                if (showRecents && !alreadyInSplitScreenMode && this.mDisplayId == 0 && windowingMode == 3) {
                    display.getOrCreateStack(4, 3, true).moveToFront("setWindowingMode");
                    this.mService.mH.post(new Runnable() {
                        /* class com.android.server.wm.$$Lambda$ActivityStack$3dUQUxLPwVLC7t9Mqjl5VgILT4 */

                        public final void run() {
                            ActivityStack.this.lambda$setWindowingModeInSurfaceTransaction$1$ActivityStack();
                        }
                    });
                }
                wm.continueSurfaceLayout();
                if (!deferEnsuringVisibility) {
                    this.mRootActivityContainer.ensureActivitiesVisible(null, 0, true);
                    this.mRootActivityContainer.resumeFocusedStacksTopActivities();
                }
            } else {
                try {
                    throw new IllegalArgumentException("Setting primary split-screen windowing mode while there is already one isn't currently supported");
                } catch (Throwable th3) {
                    th = th3;
                    if (showRecents && !alreadyInSplitScreenMode && this.mDisplayId == 0 && windowingMode == 3) {
                        display.getOrCreateStack(4, 3, true).moveToFront("setWindowingMode");
                        this.mService.mH.post(new Runnable() {
                            /* class com.android.server.wm.$$Lambda$ActivityStack$3dUQUxLPwVLC7t9Mqjl5VgILT4 */

                            public final void run() {
                                ActivityStack.this.lambda$setWindowingModeInSurfaceTransaction$1$ActivityStack();
                            }
                        });
                    }
                    wm.continueSurfaceLayout();
                    throw th;
                }
            }
        } catch (Throwable th4) {
            th = th4;
            display.getOrCreateStack(4, 3, true).moveToFront("setWindowingMode");
            this.mService.mH.post(new Runnable() {
                /* class com.android.server.wm.$$Lambda$ActivityStack$3dUQUxLPwVLC7t9Mqjl5VgILT4 */

                public final void run() {
                    ActivityStack.this.lambda$setWindowingModeInSurfaceTransaction$1$ActivityStack();
                }
            });
            wm.continueSurfaceLayout();
            throw th;
        }
    }

    public /* synthetic */ void lambda$setWindowingModeInSurfaceTransaction$1$ActivityStack() {
        this.mService.mInternal.startHomeActivity(this.mCurrentUser, "setWindowingMode");
    }

    @Override // com.android.server.wm.ConfigurationContainer
    public boolean isCompatible(int windowingMode, int activityType) {
        if (activityType == 0) {
            activityType = 1;
        }
        return super.isCompatible(windowingMode, activityType);
    }

    /* access modifiers changed from: package-private */
    public void reparent(ActivityDisplay activityDisplay, boolean onTop, boolean displayRemoved) {
        removeFromDisplay();
        this.mTmpRect2.setEmpty();
        TaskStack taskStack = this.mTaskStack;
        if (taskStack == null) {
            Log.w(TAG, "Task stack is not valid when reparenting.");
        } else {
            taskStack.reparent(activityDisplay.mDisplayId, this.mTmpRect2, onTop);
        }
        setBounds(this.mTmpRect2.isEmpty() ? null : this.mTmpRect2);
        activityDisplay.addChild(this, onTop ? Integer.MAX_VALUE : Integer.MIN_VALUE);
        if (!displayRemoved) {
            postReparent();
        }
    }

    /* access modifiers changed from: package-private */
    public void postReparent() {
        adjustFocusToNextFocusableStack("reparent", true);
        this.mRootActivityContainer.resumeFocusedStacksTopActivities();
        this.mRootActivityContainer.ensureActivitiesVisible(null, 0, false);
    }

    private void removeFromDisplay() {
        ActivityDisplay display = getDisplay();
        if (display != null) {
            display.removeChild(this);
        }
        this.mDisplayId = -1;
    }

    /* access modifiers changed from: package-private */
    public void remove() {
        if (!this.mIsSwapTask) {
            removeFromDisplay();
            TaskStack taskStack = this.mTaskStack;
            if (taskStack != null) {
                taskStack.removeIfPossible();
                this.mTaskStack = null;
            }
            onParentChanged();
        }
    }

    /* access modifiers changed from: package-private */
    public ActivityDisplay getDisplay() {
        return this.mRootActivityContainer.getActivityDisplay(this.mDisplayId);
    }

    /* access modifiers changed from: package-private */
    public void getStackDockedModeBounds(Rect dockedBounds, Rect currentTempTaskBounds, Rect outStackBounds, Rect outTempTaskBounds) {
        TaskStack taskStack = this.mTaskStack;
        if (taskStack != null) {
            taskStack.getStackDockedModeBoundsLocked(getParent().getConfiguration(), dockedBounds, currentTempTaskBounds, outStackBounds, outTempTaskBounds);
            return;
        }
        outStackBounds.setEmpty();
        outTempTaskBounds.setEmpty();
    }

    /* access modifiers changed from: package-private */
    public void prepareFreezingTaskBounds() {
        TaskStack taskStack = this.mTaskStack;
        if (taskStack != null) {
            taskStack.prepareFreezingTaskBounds();
        }
    }

    /* access modifiers changed from: package-private */
    public void getWindowContainerBounds(Rect outBounds) {
        TaskStack taskStack = this.mTaskStack;
        if (taskStack != null) {
            taskStack.getBounds(outBounds);
        } else {
            outBounds.setEmpty();
        }
    }

    /* access modifiers changed from: package-private */
    public void positionChildWindowContainerAtTop(TaskRecord child) {
        TaskStack taskStack = this.mTaskStack;
        if (taskStack != null) {
            taskStack.positionChildAtTop(child.getTask(), true);
        }
    }

    /* access modifiers changed from: package-private */
    public void positionChildWindowContainerAtBottom(TaskRecord child) {
        boolean z = true;
        ActivityStack nextFocusableStack = getDisplay().getNextFocusableStack(child.getStack(), true);
        TaskStack taskStack = this.mTaskStack;
        if (taskStack != null) {
            Task task = child.getTask();
            if (nextFocusableStack != null) {
                z = false;
            }
            taskStack.positionChildAtBottom(task, z);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean deferScheduleMultiWindowModeChanged() {
        if (!inPinnedWindowingMode() || getTaskStack() == null) {
            return false;
        }
        return getTaskStack().deferScheduleMultiWindowModeChanged();
    }

    /* access modifiers changed from: package-private */
    public void deferUpdateBounds() {
        if (!this.mUpdateBoundsDeferred) {
            this.mUpdateBoundsDeferred = true;
            this.mUpdateBoundsDeferredCalled = false;
        }
    }

    /* access modifiers changed from: package-private */
    public void continueUpdateBounds() {
        if (this.mUpdateBoundsDeferred) {
            this.mUpdateBoundsDeferred = false;
            if (this.mUpdateBoundsDeferredCalled) {
                setTaskBounds(this.mDeferredBounds);
                setBounds(this.mDeferredBounds);
            }
            if (this.mUpdateDisplayedBoundsDeferredCalled) {
                setTaskDisplayedBounds(this.mDeferredDisplayedBounds);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean updateBoundsAllowed(Rect bounds) {
        if (!this.mUpdateBoundsDeferred) {
            return true;
        }
        if (bounds != null) {
            this.mDeferredBounds.set(bounds);
        } else {
            this.mDeferredBounds.setEmpty();
        }
        this.mUpdateBoundsDeferredCalled = true;
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean updateDisplayedBoundsAllowed(Rect bounds) {
        if (!this.mUpdateBoundsDeferred) {
            return true;
        }
        if (bounds != null) {
            this.mDeferredDisplayedBounds.set(bounds);
        } else {
            this.mDeferredDisplayedBounds.setEmpty();
        }
        this.mUpdateDisplayedBoundsDeferredCalled = true;
        return false;
    }

    @Override // com.android.server.wm.ConfigurationContainer
    public int setBounds(Rect bounds) {
        if (getWindowingMode() == WindowConfiguration.WINDOWING_MODE_ZOOM) {
            return super.setBounds(bounds);
        }
        return super.setBounds(!inMultiWindowMode() ? null : bounds);
    }

    /* access modifiers changed from: package-private */
    public ActivityRecord topRunningActivityLocked() {
        return topRunningActivityLocked(false);
    }

    /* access modifiers changed from: package-private */
    public void getAllRunningVisibleActivitiesLocked(ArrayList<ActivityRecord> outActivities) {
        outActivities.clear();
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            this.mTaskHistory.get(taskNdx).getAllRunningVisibleActivitiesLocked(outActivities);
        }
    }

    /* access modifiers changed from: package-private */
    public ActivityRecord topRunningActivityLocked(boolean focusableOnly) {
        try {
            for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
                ActivityRecord r = this.mTaskHistory.get(taskNdx).topRunningActivityLocked();
                if (r != null && (!focusableOnly || r.isFocusable())) {
                    return r;
                }
            }
            return null;
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    public ActivityRecord topRunningNonOverlayTaskActivity() {
        if (ActivityTaskManagerDebugConfig.DEBUG_STATES) {
            Slog.i(TAG, "topRunningActivityLocked: mTaskHistory.size() " + this.mTaskHistory.size() + " mTaskHistory " + this.mTaskHistory + "  call by " + Debug.getCallers(8));
        }
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ArrayList<ActivityRecord> activities = this.mTaskHistory.get(taskNdx).mActivities;
            for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                ActivityRecord r = activities.get(activityNdx);
                if (!(r.finishing || r.mTaskOverlay)) {
                    return r;
                }
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public ActivityRecord topRunningNonDelayedActivityLocked(ActivityRecord notTop) {
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ArrayList<ActivityRecord> activities = this.mTaskHistory.get(taskNdx).mActivities;
            for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                ActivityRecord r = activities.get(activityNdx);
                if (ActivityTaskManagerDebugConfig.DEBUG_STATES) {
                    Slog.d(TAG, "topRunningActivityLocked: taskNdx " + taskNdx + " mTaskHistory.get(taskNdx) " + this.mTaskHistory.get(taskNdx) + " r " + r);
                }
                if (!(r.finishing || r.delayedResume || r == notTop || !r.okToShowLocked())) {
                    return r;
                }
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public final ActivityRecord topRunningActivityLocked(IBinder token, int taskId) {
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            TaskRecord task = this.mTaskHistory.get(taskNdx);
            if (task.taskId != taskId) {
                ArrayList<ActivityRecord> activities = task.mActivities;
                for (int i = activities.size() - 1; i >= 0; i--) {
                    ActivityRecord r = activities.get(i);
                    if (!(r.finishing || token == r.appToken || !r.okToShowLocked())) {
                        return r;
                    }
                }
                continue;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public ActivityRecord getTopActivity() {
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ActivityRecord r = this.mTaskHistory.get(taskNdx).getTopActivity();
            if (r != null) {
                return r;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public final TaskRecord topTask() {
        int size = this.mTaskHistory.size();
        if (size > 0) {
            return this.mTaskHistory.get(size - 1);
        }
        return null;
    }

    private TaskRecord bottomTask() {
        if (this.mTaskHistory.isEmpty()) {
            return null;
        }
        return this.mTaskHistory.get(0);
    }

    /* access modifiers changed from: package-private */
    public TaskRecord taskForIdLocked(int id) {
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            TaskRecord task = this.mTaskHistory.get(taskNdx);
            if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                Slog.d(TAG_TASKS, "task.taskId " + task.taskId + " task " + task);
            }
            if (task.taskId == id) {
                return task;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public ActivityRecord isInStackLocked(IBinder token) {
        return isInStackLocked(ActivityRecord.forTokenLocked(token));
    }

    /* access modifiers changed from: package-private */
    public ActivityRecord isInStackLocked(ActivityRecord r) {
        if (r == null) {
            return null;
        }
        TaskRecord task = r.getTaskRecord();
        ActivityStack stack = r.getActivityStack();
        if (stack == null || !task.mActivities.contains(r) || !this.mTaskHistory.contains(task)) {
            return null;
        }
        if (stack != this) {
            Slog.w(TAG, "Illegal state! task does not point to stack it is in.");
        }
        return r;
    }

    /* access modifiers changed from: package-private */
    public boolean isInStackLocked(TaskRecord task) {
        return this.mTaskHistory.contains(task);
    }

    /* access modifiers changed from: package-private */
    public boolean isUidPresent(int uid) {
        Iterator<TaskRecord> it = this.mTaskHistory.iterator();
        while (it.hasNext()) {
            Iterator<ActivityRecord> it2 = it.next().mActivities.iterator();
            while (true) {
                if (it2.hasNext()) {
                    if (it2.next().getUid() == uid) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void getPresentUIDs(IntArray presentUIDs) {
        Iterator<TaskRecord> it = this.mTaskHistory.iterator();
        while (it.hasNext()) {
            Iterator<ActivityRecord> it2 = it.next().mActivities.iterator();
            while (it2.hasNext()) {
                presentUIDs.add(it2.next().getUid());
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isSingleTaskInstance() {
        ActivityDisplay display = getDisplay();
        return display != null && display.isSingleTaskInstance();
    }

    /* access modifiers changed from: package-private */
    public final void removeActivitiesFromLRUListLocked(TaskRecord task) {
        Iterator<ActivityRecord> it = task.mActivities.iterator();
        while (it.hasNext()) {
            this.mLRUActivities.remove(it.next());
        }
    }

    /* access modifiers changed from: package-private */
    public final boolean updateLRUListLocked(ActivityRecord r) {
        boolean hadit = this.mLRUActivities.remove(r);
        this.mLRUActivities.add(r);
        return hadit;
    }

    /* access modifiers changed from: package-private */
    public final boolean isHomeOrRecentsStack() {
        return isActivityTypeHome() || isActivityTypeRecents();
    }

    /* access modifiers changed from: package-private */
    public final boolean isOnHomeDisplay() {
        return this.mDisplayId == 0;
    }

    private boolean returnsToHomeStack() {
        if (inMultiWindowMode() || this.mTaskHistory.isEmpty() || !this.mTaskHistory.get(0).returnsToHomeStack()) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public void moveToFront(String reason) {
        moveToFront(reason, null);
    }

    /* access modifiers changed from: package-private */
    public void moveToFront(String reason, TaskRecord task) {
        ActivityStack topFullScreenStack;
        ActivityStack primarySplitScreenStack;
        if (isAttached()) {
            ActivityDisplay display = getDisplay();
            if (inSplitScreenSecondaryWindowingMode() && (topFullScreenStack = display.getTopStackInWindowingMode(1)) != null && (primarySplitScreenStack = display.getSplitScreenPrimaryStack()) != null && display.getIndexOf(topFullScreenStack) > display.getIndexOf(primarySplitScreenStack)) {
                primarySplitScreenStack.moveToFront(reason + " splitScreenToTop");
            }
            if (!isActivityTypeHome() && returnsToHomeStack()) {
                display.moveHomeStackToFront(reason + " returnToHome");
            }
            boolean z = false;
            boolean movingTask = task != null;
            if (!movingTask) {
                z = true;
            }
            display.positionChildAtTop(this, z, reason);
            if (movingTask) {
                insertTaskAtTop(task, null);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void moveToBack(String reason, TaskRecord task) {
        if (isAttached()) {
            if (getWindowingMode() == 3) {
                setWindowingMode(0);
            }
            getDisplay().positionChildAtBottom(this, reason);
            if (task != null) {
                insertTaskAtBottom(task);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isFocusable() {
        ActivityRecord r = topRunningActivityLocked();
        return this.mRootActivityContainer.isFocusable(this, r != null && r.isFocusable());
    }

    /* access modifiers changed from: package-private */
    public boolean isFocusableAndVisible() {
        return isFocusable() && shouldBeVisible(null);
    }

    /* access modifiers changed from: package-private */
    public final boolean isAttached() {
        ActivityDisplay display = getDisplay();
        return display != null && !display.isRemoved();
    }

    /* access modifiers changed from: package-private */
    public void findTaskLocked(ActivityRecord target, RootActivityContainer.FindTaskResult result) {
        int taskNdx;
        int userId;
        int userId2;
        boolean taskIsDocument;
        Uri taskDocumentData;
        ActivityStack activityStack = this;
        Intent intent = target.intent;
        ActivityInfo info = target.info;
        ComponentName cls = intent.getComponent();
        if (info.targetActivity != null) {
            cls = new ComponentName(info.packageName, info.targetActivity);
        }
        int userId3 = UserHandle.getUserId(info.applicationInfo.uid);
        int i = 1;
        boolean isDocument = intent.isDocument() & true;
        Uri documentData = isDocument ? intent.getData() : null;
        if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
            Slog.d(TAG_TASKS, "Looking for task of " + target + " in " + activityStack);
        }
        int taskNdx2 = activityStack.mTaskHistory.size() - 1;
        while (taskNdx2 >= 0) {
            TaskRecord task = activityStack.mTaskHistory.get(taskNdx2);
            if (task.voiceSession != null) {
                if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                    Slog.d(TAG_TASKS, "Skipping " + task + ": voice session");
                }
                userId = userId3;
                userId2 = i;
                taskNdx = taskNdx2;
            } else if (task.userId != userId3) {
                if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                    Slog.d(TAG_TASKS, "Skipping " + task + ": different user");
                }
                userId = userId3;
                userId2 = i;
                taskNdx = taskNdx2;
            } else {
                ActivityRecord r = task.getTopActivity(false);
                boolean isSkip = activityStack.shouldSkipMultiAppUser(task, r, userId3);
                if (r == null || r.finishing || isSkip) {
                    userId = userId3;
                    userId2 = i;
                    taskNdx = taskNdx2;
                } else if (r.launchMode == 3) {
                    userId = userId3;
                    taskNdx = taskNdx2;
                    userId2 = 1;
                } else if (!r.hasCompatibleActivityType(target)) {
                    if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                        Slog.d(TAG_TASKS, "Skipping " + task + ": mismatch activity type");
                    }
                    userId = userId3;
                    taskNdx = taskNdx2;
                    userId2 = 1;
                } else {
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
                    if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                        userId = userId3;
                        Slog.d(TAG_TASKS, "affinityIntent " + affinityIntent + " taskIsDocument " + taskIsDocument + " taskDocumentData " + taskDocumentData + " documentData " + documentData + " isDocument " + isDocument);
                    } else {
                        userId = userId3;
                    }
                    if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                        String str = TAG_TASKS;
                        StringBuilder sb = new StringBuilder();
                        sb.append("Comparing existing cls=");
                        sb.append(task.realActivity != null ? task.realActivity.flattenToShortString() : "");
                        sb.append("/aff=");
                        taskNdx = taskNdx2;
                        sb.append(r.getTaskRecord().rootAffinity);
                        sb.append(" to new cls=");
                        sb.append(intent.getComponent().flattenToShortString());
                        sb.append("/aff=");
                        sb.append(info.taskAffinity);
                        Slog.d(str, sb.toString());
                    } else {
                        taskNdx = taskNdx2;
                    }
                    if (task.realActivity == null || task.realActivity.compareTo(cls) != 0 || !Objects.equals(documentData, taskDocumentData)) {
                        if (affinityIntent == null || affinityIntent.getComponent() == null) {
                            userId2 = 1;
                        } else if (affinityIntent.getComponent().compareTo(cls) != 0) {
                            userId2 = 1;
                        } else if (Objects.equals(documentData, taskDocumentData)) {
                            if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                                Slog.d(TAG_TASKS, "Found matching class!");
                            }
                            if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                                Slog.d(TAG_TASKS, "For Intent " + intent + " bringing to top: " + r.intent);
                            }
                            result.mRecord = r;
                            result.mIdealMatch = true;
                            return;
                        } else {
                            userId2 = 1;
                        }
                        if (!isDocument && !taskIsDocument && result.mRecord == null && task.rootAffinity != null) {
                            if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                                Slog.d(TAG_TASKS, "task.rootAffinity " + task.rootAffinity + " target.taskAffinity " + target.taskAffinity);
                            }
                            if (task.rootAffinity.equals(target.taskAffinity)) {
                                if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                                    Slog.d(TAG_TASKS, "Found matching affinity candidate!");
                                }
                                result.mRecord = r;
                                result.mIdealMatch = false;
                            }
                        } else if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                            Slog.d(TAG_TASKS, "Not a match: " + task);
                        }
                    } else {
                        if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                            Slog.d(TAG_TASKS, "Found matching class!");
                        }
                        if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                            Slog.d(TAG_TASKS, "For Intent " + intent + " bringing to top: " + r.intent);
                        }
                        result.mRecord = r;
                        result.mIdealMatch = true;
                        return;
                    }
                }
                if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                    Slog.d(TAG_TASKS, "Skipping " + task + ": mismatch root " + r);
                }
            }
            taskNdx2 = taskNdx - 1;
            activityStack = this;
            i = userId2;
            userId3 = userId;
        }
    }

    /* access modifiers changed from: package-private */
    public ActivityRecord findActivityLocked(Intent intent, ActivityInfo info, boolean compareIntentFilters) {
        ComponentName cls = intent.getComponent();
        if (info.targetActivity != null) {
            cls = new ComponentName(info.packageName, info.targetActivity);
        }
        int userId = UserHandle.getUserId(info.applicationInfo.uid);
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ArrayList<ActivityRecord> activities = this.mTaskHistory.get(taskNdx).mActivities;
            for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                ActivityRecord r = activities.get(activityNdx);
                if (r.okToShowLocked() && !r.finishing && r.mUserId == userId) {
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

    /* access modifiers changed from: package-private */
    public final void switchUserLocked(int userId) {
        if (this.mCurrentUser != userId) {
            this.mCurrentUser = userId;
            int index = this.mTaskHistory.size();
            int i = 0;
            while (i < index) {
                TaskRecord task = this.mTaskHistory.get(i);
                if (task.okToShowLocked()) {
                    if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                        Slog.d(TAG_TASKS, "switchUser: stack=" + getStackId() + " moving " + task + " to top");
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

    /* access modifiers changed from: package-private */
    public void minimalResumeActivityLocked(ActivityRecord r) {
        if (ActivityTaskManagerDebugConfig.DEBUG_STATES) {
            String str = TAG_STATES;
            Slog.v(str, "Moving to RESUMED: " + r + " (starting new instance) callers=" + Debug.getCallers(5));
        }
        r.setState(ActivityState.RESUMED, "minimalResumeActivityLocked");
        r.completeResumeLocked();
        OppoFeatureCache.getOrCreate(IColorAntiVirusBehaviorManager.DEFAULT, new Object[0]).setForegroundApp(r.packageName);
        if (ActivityTaskManagerDebugConfig.DEBUG_SAVED_STATE) {
            String str2 = TAG_SAVED_STATE;
            Slog.i(str2, "Launch completed; removing icicle of " + r.icicle);
        }
        this.mService.mAmsExt.onAfterActivityResumed(r);
    }

    private void clearLaunchTime(ActivityRecord r) {
        if (!this.mStackSupervisor.mWaitingActivityLaunched.isEmpty()) {
            this.mStackSupervisor.removeTimeoutsForActivityLocked(r);
            this.mStackSupervisor.scheduleIdleTimeoutLocked(r);
        }
    }

    /* access modifiers changed from: package-private */
    public void awakeFromSleepingLocked() {
        if (ActivityTaskManagerDebugConfig.DEBUG_PAUSE) {
            Slog.d(TAG, "awakeFromSleep");
        }
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ArrayList<ActivityRecord> activities = this.mTaskHistory.get(taskNdx).mActivities;
            for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                if (ActivityTaskManagerDebugConfig.DEBUG_PAUSE) {
                    Slog.d(TAG, "awakeFromSleep, act:" + activities.get(activityNdx));
                }
                activities.get(activityNdx).setSleeping(false);
            }
        }
        if (this.mPausingActivity != null) {
            Slog.d(TAG, "awakeFromSleepingLocked: previously pausing activity didn't pause");
            activityPausedLocked(this.mPausingActivity.appToken, true);
        }
    }

    /* access modifiers changed from: package-private */
    public void updateActivityApplicationInfoLocked(ApplicationInfo aInfo) {
        String packageName = aInfo.packageName;
        int userId = UserHandle.getUserId(aInfo.uid);
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            List<ActivityRecord> activities = this.mTaskHistory.get(taskNdx).mActivities;
            for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                ActivityRecord ar = activities.get(activityNdx);
                if (userId == ar.mUserId && packageName.equals(ar.packageName)) {
                    ar.updateApplicationInfo(aInfo);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void checkReadyForSleep() {
        if (shouldSleepActivities() && goToSleepIfPossible(false)) {
            this.mStackSupervisor.checkReadyForSleepLocked(true);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean goToSleepIfPossible(boolean shuttingDown) {
        boolean shouldSleep = true;
        if (this.mResumedActivity != null) {
            ActivityRecord activityRecord = this.mResumedActivity;
            OppoFeatureCache.get(IColorAccessControlLocalManager.DEFAULT).checkGoToSleep(activityRecord, activityRecord.mUserId);
            if (ActivityTaskManagerDebugConfig.DEBUG_PAUSE) {
                String str = TAG_PAUSE;
                Slog.v(str, "Sleep needs to pause " + this.mResumedActivity);
            }
            if (ActivityTaskManagerDebugConfig.DEBUG_USER_LEAVING) {
                Slog.v(TAG_USER_LEAVING, "Sleep => pause with userLeaving=false");
            }
            startPausingLocked(false, true, null, false, "sleep-request");
            shouldSleep = false;
        } else if (this.mPausingActivity != null) {
            if (ActivityTaskManagerDebugConfig.DEBUG_PAUSE) {
                String str2 = TAG_PAUSE;
                Slog.v(str2, "Sleep still waiting to pause " + this.mPausingActivity);
            }
            shouldSleep = false;
        }
        if (!shuttingDown) {
            if (containsActivityFromStack(this.mStackSupervisor.mStoppingActivities)) {
                if (ActivityTaskManagerDebugConfig.DEBUG_PAUSE) {
                    String str3 = TAG_PAUSE;
                    Slog.v(str3, "Sleep still need to stop " + this.mStackSupervisor.mStoppingActivities.size() + " activities");
                }
                this.mStackSupervisor.scheduleIdleLocked();
                shouldSleep = false;
            }
            if (containsActivityFromStack(this.mStackSupervisor.mGoingToSleepActivities)) {
                if (ActivityTaskManagerDebugConfig.DEBUG_PAUSE) {
                    String str4 = TAG_PAUSE;
                    Slog.v(str4, "Sleep still need to sleep " + this.mStackSupervisor.mGoingToSleepActivities.size() + " activities");
                }
                shouldSleep = false;
            }
        }
        if (shouldSleep) {
            goToSleep();
        }
        return shouldSleep;
    }

    /* access modifiers changed from: package-private */
    public void goToSleep() {
        ensureActivitiesVisibleLocked(null, 0, false);
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ArrayList<ActivityRecord> activities = this.mTaskHistory.get(taskNdx).mActivities;
            for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                ActivityRecord r = activities.get(activityNdx);
                if (r.isState(ActivityState.STOPPING, ActivityState.STOPPED, ActivityState.PAUSED, ActivityState.PAUSING)) {
                    r.setSleeping(true);
                }
            }
        }
    }

    private boolean containsActivityFromStack(List<ActivityRecord> rs) {
        for (ActivityRecord r : rs) {
            if (r.getActivityStack() == this) {
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
        if (ActivityTaskManagerDebugConfig.DEBUG_PAUSE) {
            Slog.v(TAG_PAUSE, "Waiting for pause to complete...");
        }
    }

    /* access modifiers changed from: package-private */
    public final boolean startPausingLocked(boolean userLeaving, boolean uiSleeping, ActivityRecord resuming, boolean pauseImmediately) {
        return startPausingLocked(userLeaving, uiSleeping, resuming, pauseImmediately, "other-request");
    }

    /* access modifiers changed from: package-private */
    public final boolean startPausingLocked(boolean userLeaving, boolean uiSleeping, ActivityRecord resuming, boolean pauseImmediately, String reason) {
        if (ActivityTaskManagerDebugConfig.DEBUG_PAUSE) {
            RuntimeException trace = new RuntimeException();
            trace.fillInStackTrace();
            Slog.d(TAG, "startPausingLocked, reason:" + reason, trace);
        }
        if (this.mPausingActivity != null) {
            Slog.e(TAG, "Going to pause when pause is already pending for " + this.mPausingActivity + " state=" + this.mPausingActivity.getState(), new RuntimeException("here").fillInStackTrace());
            if (!shouldSleepActivities()) {
                completePauseLocked(false, resuming);
            }
        }
        ActivityRecord prev = this.mResumedActivity;
        if (prev == null) {
            if (resuming == null) {
                Slog.wtf(TAG, "Trying to pause when nothing is resumed");
                this.mRootActivityContainer.resumeFocusedStacksTopActivities();
            }
            return false;
        } else if (prev == resuming) {
            Slog.wtf(TAG, "Trying to pause activity that is in process of being resumed");
            return false;
        } else {
            if (ActivityTaskManagerDebugConfig.DEBUG_STATES) {
                String str = TAG_STATES;
                Slog.v(str, "Moving to PAUSING: " + prev);
            } else if (ActivityTaskManagerDebugConfig.DEBUG_PAUSE) {
                String str2 = TAG_PAUSE;
                Slog.v(str2, "Start pausing: " + prev);
            }
            this.mPausingActivity = prev;
            this.mLastPausedActivity = prev;
            this.mLastNoHistoryActivity = ((prev.intent.getFlags() & 1073741824) == 0 && (prev.info.flags & 128) == 0) ? null : prev;
            prev.setState(ActivityState.PAUSING, "startPausingLocked");
            prev.getTaskRecord().touchActiveTime();
            clearLaunchTime(prev);
            this.mService.updateCpuStats();
            if (prev.attachedToProcess()) {
                if (ActivityTaskManagerDebugConfig.DEBUG_PAUSE) {
                    String str3 = TAG_PAUSE;
                    Slog.v(str3, "Enqueueing pending pause: " + prev);
                }
                try {
                    int i = prev.mUserId;
                    int identityHashCode = System.identityHashCode(prev);
                    String str4 = prev.shortComponentName;
                    EventLogTags.writeAmPauseActivity(i, identityHashCode, str4, "userLeaving=" + userLeaving, reason);
                    if (prev.shortComponentName != null && mDefaultAlbumBrowserList.contains(prev.shortComponentName)) {
                        SystemProperties.set("debug.jpegfull.opt.switch", NoFocusWindow.HUNG_CONFIG_ENABLE);
                        Slog.d(TAG, "yac: NOW PAUSING " + prev.shortComponentName + " set jpegfull 1");
                    }
                    this.mService.getLifecycleManager().scheduleTransaction(prev.app.getThread(), (IBinder) prev.appToken, (ActivityLifecycleItem) PauseActivityItem.obtain(prev.finishing, userLeaving, prev.configChangeFlags, pauseImmediately));
                } catch (Exception e) {
                    Slog.w(TAG, "Exception thrown during pause", e);
                    this.mPausingActivity = null;
                    this.mLastPausedActivity = null;
                    this.mLastNoHistoryActivity = null;
                }
            } else {
                this.mPausingActivity = null;
                this.mLastPausedActivity = null;
                this.mLastNoHistoryActivity = null;
            }
            if (!uiSleeping && !this.mService.isSleepingOrShuttingDownLocked()) {
                this.mStackSupervisor.acquireLaunchWakelock();
            }
            if (this.mPausingActivity != null) {
                if (!uiSleeping) {
                    prev.pauseKeyDispatchingLocked();
                } else if (ActivityTaskManagerDebugConfig.DEBUG_PAUSE) {
                    Slog.v(TAG_PAUSE, "Key dispatch not paused for screen off");
                }
                if (pauseImmediately) {
                    completePauseLocked(false, resuming);
                    return false;
                }
                schedulePauseTimeout(prev);
                return true;
            }
            if (ActivityTaskManagerDebugConfig.DEBUG_PAUSE) {
                Slog.v(TAG_PAUSE, "Activity not running, resuming next.");
            }
            if (resuming == null) {
                this.mRootActivityContainer.resumeFocusedStacksTopActivities();
            }
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public final void activityPausedLocked(IBinder token, boolean timeout) {
        if (ActivityTaskManagerDebugConfig.DEBUG_PAUSE) {
            String str = TAG_PAUSE;
            Slog.v(str, "Activity paused: token=" + token + ", timeout=" + timeout);
        }
        ActivityRecord r = isInStackLocked(token);
        if (r != null) {
            this.mHandler.removeMessages(101, r);
            if (this.mPausingActivity == r) {
                if (ActivityTaskManagerDebugConfig.DEBUG_STATES) {
                    String str2 = TAG_STATES;
                    StringBuilder sb = new StringBuilder();
                    sb.append("Moving to PAUSED: ");
                    sb.append(r);
                    sb.append(timeout ? " (due to timeout)" : " (pause complete)");
                    Slog.v(str2, sb.toString());
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
                objArr[0] = Integer.valueOf(r.mUserId);
                objArr[1] = Integer.valueOf(System.identityHashCode(r));
                objArr[2] = r.shortComponentName;
                ActivityRecord activityRecord = this.mPausingActivity;
                objArr[3] = activityRecord != null ? activityRecord.shortComponentName : "(none)";
                EventLog.writeEvent((int) EventLogTags.AM_FAILED_TO_PAUSE, objArr);
                if (r.isState(ActivityState.PAUSING)) {
                    r.setState(ActivityState.PAUSED, "activityPausedLocked");
                    if (r.finishing) {
                        if (ActivityTaskManagerDebugConfig.DEBUG_PAUSE) {
                            Slog.v(TAG, "Executing finish of failed to pause activity: " + r);
                        }
                        finishCurrentActivityLocked(r, 2, false, "activityPausedLocked");
                    }
                    if (r.deferRelaunchUntilPaused && r.hasProcess() && inSplitScreenPrimaryWindowingMode() && this.mRootActivityContainer.mIsDockMinimized) {
                        r.relaunchActivityLocked(false, r.preserveWindowOnDeferredRelaunch);
                    }
                }
            }
        }
        this.mRootActivityContainer.ensureActivitiesVisible(null, 0, false);
    }

    private void completePauseLocked(boolean resumeNext, ActivityRecord resuming) {
        ActivityRecord prev = this.mPausingActivity;
        if (ActivityTaskManagerDebugConfig.DEBUG_PAUSE) {
            String str = TAG_PAUSE;
            Slog.v(str, "Complete pause: " + prev);
        }
        if (prev != null) {
            prev.setWillCloseOrEnterPip(false);
            boolean wasStopping = prev.isState(ActivityState.STOPPING);
            prev.setState(ActivityState.PAUSED, "completePausedLocked");
            if (prev.finishing) {
                if (ActivityTaskManagerDebugConfig.DEBUG_PAUSE) {
                    String str2 = TAG_PAUSE;
                    Slog.v(str2, "Executing finish of activity: " + prev);
                }
                prev = finishCurrentActivityLocked(prev, 2, false, "completePausedLocked");
            } else if (prev.hasProcess()) {
                if (ActivityTaskManagerDebugConfig.DEBUG_PAUSE) {
                    String str3 = TAG_PAUSE;
                    Slog.v(str3, "Enqueue pending stop if needed: " + prev + " wasStopping=" + wasStopping + " visible=" + prev.visible);
                }
                if (prev.deferRelaunchUntilPaused) {
                    if (ActivityTaskManagerDebugConfig.DEBUG_PAUSE) {
                        String str4 = TAG_PAUSE;
                        Slog.v(str4, "Re-launching after pause: " + prev);
                    }
                    prev.relaunchActivityLocked(false, prev.preserveWindowOnDeferredRelaunch);
                } else if (wasStopping) {
                    prev.setState(ActivityState.STOPPING, "completePausedLocked");
                } else if (!prev.visible || shouldSleepOrShutDownActivities()) {
                    prev.setDeferHidingClient(false);
                    addToStopping(prev, true, false, "completePauseLocked");
                }
            } else {
                if (ActivityTaskManagerDebugConfig.DEBUG_PAUSE) {
                    String str5 = TAG_PAUSE;
                    Slog.v(str5, "App died during pause, not stopping: " + prev);
                }
                prev = null;
            }
            if (prev != null) {
                prev.stopFreezingScreenLocked(true);
            }
            this.mPausingActivity = null;
        }
        if (resumeNext) {
            ActivityStack topStack = this.mRootActivityContainer.getTopDisplayFocusedStack();
            if (!topStack.shouldSleepOrShutDownActivities()) {
                this.mRootActivityContainer.resumeFocusedStacksTopActivities(topStack, prev, null);
            } else {
                checkReadyForSleep();
                ActivityRecord top = topStack.topRunningActivityLocked();
                if (top == null || !(prev == null || top == prev)) {
                    this.mRootActivityContainer.resumeFocusedStacksTopActivities();
                }
            }
        }
        if (prev != null) {
            prev.resumeKeyDispatchingLocked();
            if (prev.hasProcess() && prev.cpuTimeAtResume > 0) {
                long diff = prev.app.getCpuTime() - prev.cpuTimeAtResume;
                if (diff > 0) {
                    this.mService.mH.post(PooledLambda.obtainRunnable($$Lambda$1636dquQO0UvkFayOGf_gceB4iw.INSTANCE, this.mService.mAmInternal, prev.info.packageName, Integer.valueOf(prev.info.applicationInfo.uid), Long.valueOf(diff)));
                }
            }
            prev.cpuTimeAtResume = 0;
        }
        if (this.mStackSupervisor.mAppVisibilitiesChangedSinceLastPause || (getDisplay() != null && getDisplay().hasPinnedStack())) {
            this.mService.getTaskChangeNotificationController().notifyTaskStackChanged();
            this.mStackSupervisor.mAppVisibilitiesChangedSinceLastPause = false;
        }
        this.mRootActivityContainer.ensureActivitiesVisible(resuming, 0, false);
    }

    private void addToStopping(ActivityRecord r, boolean scheduleIdle, boolean idleDelayed, String reason) {
        boolean forceIdle = false;
        if (!this.mStackSupervisor.mStoppingActivities.contains(r)) {
            EventLog.writeEvent((int) EventLogTags.AM_ADD_TO_STOPPING, Integer.valueOf(r.mUserId), Integer.valueOf(System.identityHashCode(r)), r.shortComponentName, reason);
            this.mStackSupervisor.mStoppingActivities.add(r);
        }
        if (this.mStackSupervisor.mStoppingActivities.size() > 3 || (r.frontOfTask && this.mTaskHistory.size() <= 1)) {
            forceIdle = true;
        }
        if (scheduleIdle || forceIdle) {
            if (ActivityTaskManagerDebugConfig.DEBUG_PAUSE) {
                String str = TAG_PAUSE;
                StringBuilder sb = new StringBuilder();
                sb.append("Scheduling idle now: forceIdle=");
                sb.append(forceIdle);
                sb.append("immediate=");
                sb.append(!idleDelayed);
                Slog.v(str, sb.toString());
            }
            if (!idleDelayed) {
                this.mStackSupervisor.scheduleIdleLocked();
            } else {
                this.mStackSupervisor.scheduleIdleTimeoutLocked(r);
            }
        } else {
            checkReadyForSleep();
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public boolean isStackTranslucent(ActivityRecord starting) {
        if (!isAttached() || this.mForceHidden) {
            return true;
        }
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ArrayList<ActivityRecord> activities = this.mTaskHistory.get(taskNdx).mActivities;
            for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                ActivityRecord r = activities.get(activityNdx);
                if (!r.finishing && ((r.visibleIgnoringKeyguard || r == starting) && (r.fullscreen || r.hasWallpaper))) {
                    return false;
                }
            }
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean isTopStackOnDisplay() {
        ActivityDisplay display = getDisplay();
        return display != null && display.isTopStack(this);
    }

    /* access modifiers changed from: package-private */
    public boolean isFocusedStackOnDisplay() {
        ActivityDisplay display = getDisplay();
        return display != null && this == display.getFocusedStack();
    }

    /* access modifiers changed from: package-private */
    public boolean isTopActivityVisible() {
        ActivityRecord topActivity = getTopActivity();
        return topActivity != null && topActivity.visible;
    }

    /* access modifiers changed from: package-private */
    public boolean shouldBeVisible(ActivityRecord starting) {
        return getVisibility(starting) != 2;
    }

    /* access modifiers changed from: package-private */
    @StackVisibility
    public int getVisibility(ActivityRecord starting) {
        ActivityDisplay display;
        if (!isAttached() || this.mForceHidden) {
            return 2;
        }
        ActivityDisplay display2 = getDisplay();
        boolean gotSplitScreenStack = false;
        boolean gotOpaqueSplitScreenPrimary = false;
        boolean gotOpaqueSplitScreenSecondary = false;
        boolean gotTranslucentFullscreen = false;
        boolean gotTranslucentSplitScreenPrimary = false;
        boolean gotTranslucentSplitScreenSecondary = false;
        boolean shouldBeVisible = true;
        int windowingMode = getWindowingMode();
        boolean isAssistantType = isActivityTypeAssistant();
        boolean z = true;
        int i = display2.getChildCount() - 1;
        while (true) {
            if (i < 0) {
                break;
            }
            ActivityStack other = display2.getChildAt(i);
            boolean hasRunningActivities = other.topRunningActivityLocked() != null ? z : false;
            if (other == this) {
                shouldBeVisible = (hasRunningActivities || isInStackLocked(starting) != null || isActivityTypeHome()) ? z : false;
            } else {
                if (!hasRunningActivities) {
                    display = display2;
                } else {
                    int otherWindowingMode = other.getWindowingMode();
                    if (otherWindowingMode == z) {
                        int activityType = other.getActivityType();
                        display = display2;
                        if (windowingMode == 3 && (activityType == 2 || (activityType == 4 && this.mWindowManager.getRecentsAnimationController() != null))) {
                            break;
                        } else if (!other.isStackTranslucent(starting)) {
                            return 2;
                        } else {
                            gotTranslucentFullscreen = true;
                        }
                    } else {
                        display = display2;
                        if (otherWindowingMode == 3 && !gotOpaqueSplitScreenPrimary) {
                            gotSplitScreenStack = true;
                            gotTranslucentSplitScreenPrimary = other.isStackTranslucent(starting);
                            gotOpaqueSplitScreenPrimary = !gotTranslucentSplitScreenPrimary;
                            if (windowingMode == 3 && gotOpaqueSplitScreenPrimary) {
                                return 2;
                            }
                        } else if (otherWindowingMode == 4 && !gotOpaqueSplitScreenSecondary) {
                            gotSplitScreenStack = true;
                            gotTranslucentSplitScreenSecondary = other.isStackTranslucent(starting);
                            gotOpaqueSplitScreenSecondary = !gotTranslucentSplitScreenSecondary;
                            if (windowingMode == 4 && gotOpaqueSplitScreenSecondary) {
                                return 2;
                            }
                        }
                        if (gotOpaqueSplitScreenPrimary && gotOpaqueSplitScreenSecondary) {
                            return 2;
                        }
                        if (isAssistantType && gotSplitScreenStack) {
                            return 2;
                        }
                        if (windowingMode == WindowConfiguration.WINDOWING_MODE_ZOOM && other != this && other.getWindowingMode() == WindowConfiguration.WINDOWING_MODE_ZOOM) {
                            return 2;
                        }
                    }
                }
                i--;
                display2 = display;
                z = true;
            }
        }
        if (!shouldBeVisible) {
            return 2;
        }
        if (windowingMode != 1) {
            if (windowingMode != 3) {
                if (windowingMode == 4 && gotTranslucentSplitScreenSecondary) {
                    return 1;
                }
            } else if (gotTranslucentSplitScreenPrimary) {
                return 1;
            }
        } else if (gotSplitScreenStack && !gotTranslucentSplitScreenSecondary && !supportsSplitScreenWindowingMode()) {
            return 2;
        } else {
            if (gotTranslucentSplitScreenPrimary || gotTranslucentSplitScreenSecondary) {
                return 1;
            }
        }
        if (gotTranslucentFullscreen) {
            return 1;
        }
        return 0;
    }

    /* access modifiers changed from: package-private */
    public final int rankTaskLayers(int baseLayer) {
        int layer = 0;
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            TaskRecord task = this.mTaskHistory.get(taskNdx);
            ActivityRecord r = task.topRunningActivityLocked();
            if (r == null || r.finishing || !r.visible) {
                task.mLayerRank = -1;
            } else {
                task.mLayerRank = layer + baseLayer;
                layer++;
            }
        }
        return layer;
    }

    /* access modifiers changed from: package-private */
    public final void ensureActivitiesVisibleLocked(ActivityRecord starting, int configChanges, boolean preserveWindows) {
        ensureActivitiesVisibleLocked(starting, configChanges, preserveWindows, true);
    }

    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:125:0x0203 */
    /* JADX DEBUG: Multi-variable search result rejected for r11v0, resolved type: int */
    /* JADX DEBUG: Multi-variable search result rejected for r11v3, resolved type: int */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX INFO: Multiple debug info for r0v27 'activityNdx'  int: [D('activityNdx' int), D('isTop' boolean)] */
    /* JADX WARN: Type inference failed for: r11v1 */
    /* access modifiers changed from: package-private */
    public final void ensureActivitiesVisibleLocked(ActivityRecord starting, int configChanges, boolean preserveWindows, boolean notifyClients) {
        Throwable th;
        ArrayList<ActivityRecord> activities;
        boolean z;
        ActivityRecord top;
        boolean behindFullscreenActivity;
        boolean z2;
        boolean behindFullscreenActivity2;
        int activityNdx;
        boolean z3;
        if (this.mEnsureActivityVisibilityDepth > 16) {
            Slog.d(TAG_VISIBILITY, "ensureActivitiesVisibleLocked quit for depth over 16");
            return;
        }
        boolean z4 = false;
        this.mTopActivityOccludesKeyguard = false;
        this.mTopDismissingKeyguardActivity = null;
        this.mStackSupervisor.getKeyguardController().beginActivityVisibilityUpdate();
        int i = 1;
        try {
            this.mEnsureActivityVisibilityDepth++;
            ActivityRecord top2 = topRunningActivityLocked();
            if (ActivityTaskManagerDebugConfig.DEBUG_VISIBILITY) {
                Slog.v(TAG_VISIBILITY, "ensureActivitiesVisible behind " + top2 + " configChanges=0x" + Integer.toHexString(configChanges));
            }
            if (top2 != null) {
                checkTranslucentActivityWaiting(top2);
            }
            boolean aboveTop = top2 != null;
            boolean stackShouldBeVisible = shouldBeVisible(starting);
            OppoFeatureCache.get(IColorZoomWindowManager.DEFAULT).shouldBeVisible(stackShouldBeVisible, this, top2);
            boolean behindFullscreenActivity3 = !stackShouldBeVisible;
            boolean resumeNextActivity = isFocusable() && isInStackLocked(starting) == null;
            int taskNdx = this.mTaskHistory.size() - 1;
            int configChanges2 = configChanges;
            boolean aboveTop2 = aboveTop;
            while (taskNdx >= 0) {
                try {
                    TaskRecord task = this.mTaskHistory.get(taskNdx);
                    ArrayList<ActivityRecord> activities2 = task.mActivities;
                    int activityNdx2 = activities2.size() - (i == true ? 1 : 0);
                    boolean resumeNextActivity2 = resumeNextActivity;
                    int configChanges3 = configChanges2;
                    boolean aboveTop3 = aboveTop2;
                    boolean z5 = i;
                    while (activityNdx2 >= 0) {
                        try {
                            ActivityRecord r = activities2.get(activityNdx2);
                            if (!r.finishing) {
                                boolean isTop = r == top2 ? z5 : false;
                                if (!aboveTop3 || isTop) {
                                    boolean visibleIgnoringKeyguard = r.shouldBeVisibleIgnoringKeyguard(behindFullscreenActivity3);
                                    boolean reallyVisible = r.shouldBeVisible(behindFullscreenActivity3);
                                    if (visibleIgnoringKeyguard) {
                                        if (!stackShouldBeVisible) {
                                            boolean z6 = z5 ? 1 : 0;
                                            boolean z7 = z5 ? 1 : 0;
                                            z3 = z6;
                                        } else {
                                            z3 = false;
                                        }
                                        behindFullscreenActivity3 = updateBehindFullscreen(z3, behindFullscreenActivity3, r);
                                    }
                                    if (reallyVisible) {
                                        try {
                                            if (ActivityTaskManagerDebugConfig.DEBUG_VISIBILITY) {
                                                String str = TAG_VISIBILITY;
                                                StringBuilder sb = new StringBuilder();
                                                behindFullscreenActivity = behindFullscreenActivity3;
                                                sb.append("Make visible? ");
                                                sb.append(r);
                                                sb.append(" finishing=");
                                                sb.append(r.finishing);
                                                sb.append(" state=");
                                                sb.append(r.getState());
                                                Slog.v(str, sb.toString());
                                            } else {
                                                behindFullscreenActivity = behindFullscreenActivity3;
                                            }
                                            if (r == starting || !notifyClients) {
                                                z2 = false;
                                            } else {
                                                z2 = false;
                                                r.ensureActivityConfiguration(0, preserveWindows, true);
                                            }
                                            if (!r.attachedToProcess()) {
                                                top = top2;
                                                behindFullscreenActivity2 = behindFullscreenActivity;
                                                z = z2;
                                                activityNdx = activityNdx2;
                                                activities = activities2;
                                                if (makeVisibleAndRestartIfNeeded(starting, configChanges3, isTop, resumeNextActivity2, r)) {
                                                    if (activityNdx >= activities.size()) {
                                                        activityNdx2 = activities.size() - 1;
                                                    } else {
                                                        activityNdx2 = activityNdx;
                                                        resumeNextActivity2 = false;
                                                    }
                                                    configChanges3 |= r.configChangeFlags;
                                                    behindFullscreenActivity3 = behindFullscreenActivity2;
                                                    aboveTop3 = false;
                                                }
                                            } else {
                                                z = z2;
                                                activities = activities2;
                                                top = top2;
                                                behindFullscreenActivity2 = behindFullscreenActivity;
                                                activityNdx = activityNdx2;
                                                if (r.visible) {
                                                    if (ActivityTaskManagerDebugConfig.DEBUG_VISIBILITY) {
                                                        Slog.v(TAG_VISIBILITY, "Skipping: already visible at " + r);
                                                    }
                                                    if (r.mClientVisibilityDeferred && notifyClients) {
                                                        r.makeClientVisible();
                                                    }
                                                    if (r.handleAlreadyVisible()) {
                                                        resumeNextActivity2 = false;
                                                    }
                                                    if (notifyClients) {
                                                        r.makeActiveIfNeeded(starting);
                                                    }
                                                } else {
                                                    r.makeVisibleIfNeeded(starting, notifyClients);
                                                }
                                            }
                                            activityNdx2 = activityNdx;
                                            configChanges3 |= r.configChangeFlags;
                                            behindFullscreenActivity3 = behindFullscreenActivity2;
                                            aboveTop3 = false;
                                        } catch (Throwable th2) {
                                            th = th2;
                                            this.mStackSupervisor.getKeyguardController().endActivityVisibilityUpdate();
                                            this.mEnsureActivityVisibilityDepth--;
                                            throw th;
                                        }
                                    } else {
                                        activities = activities2;
                                        top = top2;
                                        z = false;
                                        if (ActivityTaskManagerDebugConfig.DEBUG_VISIBILITY) {
                                            Slog.v(TAG_VISIBILITY, "Make invisible? " + r + " finishing=" + r.finishing + " state=" + r.getState() + " stackShouldBeVisible=" + stackShouldBeVisible + " behindFullscreenActivity=" + behindFullscreenActivity3 + " mLaunchTaskBehind=" + r.mLaunchTaskBehind);
                                        }
                                        makeInvisible(r);
                                        activityNdx2 = activityNdx2;
                                        behindFullscreenActivity3 = behindFullscreenActivity3;
                                        aboveTop3 = false;
                                    }
                                    activityNdx2--;
                                    top2 = top;
                                    z4 = z;
                                    activities2 = activities;
                                    z5 = true;
                                    aboveTop3 = aboveTop3;
                                }
                            }
                            activities = activities2;
                            top = top2;
                            z = false;
                            activityNdx2--;
                            top2 = top;
                            z4 = z;
                            activities2 = activities;
                            z5 = true;
                            aboveTop3 = aboveTop3;
                        } catch (Throwable th3) {
                            th = th3;
                            this.mStackSupervisor.getKeyguardController().endActivityVisibilityUpdate();
                            this.mEnsureActivityVisibilityDepth--;
                            throw th;
                        }
                    }
                    if (getWindowingMode() == 5) {
                        behindFullscreenActivity3 = !stackShouldBeVisible ? true : z4;
                    } else if (isActivityTypeHome()) {
                        if (ActivityTaskManagerDebugConfig.DEBUG_VISIBILITY) {
                            Slog.v(TAG_VISIBILITY, "Home task: at " + task + " stackShouldBeVisible=" + stackShouldBeVisible + " behindFullscreenActivity=" + behindFullscreenActivity3);
                        }
                        behindFullscreenActivity3 = true;
                    }
                    taskNdx--;
                    configChanges2 = configChanges3;
                    resumeNextActivity = resumeNextActivity2;
                    top2 = top2;
                    z4 = z4;
                    i = 1;
                    aboveTop2 = aboveTop3;
                } catch (Throwable th4) {
                    th = th4;
                    this.mStackSupervisor.getKeyguardController().endActivityVisibilityUpdate();
                    this.mEnsureActivityVisibilityDepth--;
                    throw th;
                }
            }
            if (this.mTranslucentActivityWaiting != null && this.mUndrawnActivitiesBelowTopTranslucent.isEmpty()) {
                notifyActivityDrawnLocked(null);
            }
            this.mStackSupervisor.getKeyguardController().endActivityVisibilityUpdate();
            this.mEnsureActivityVisibilityDepth--;
        } catch (Throwable th5) {
            th = th5;
            this.mStackSupervisor.getKeyguardController().endActivityVisibilityUpdate();
            this.mEnsureActivityVisibilityDepth--;
            throw th;
        }
    }

    /* access modifiers changed from: package-private */
    public void addStartingWindowsForVisibleActivities(boolean taskSwitch) {
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            this.mTaskHistory.get(taskNdx).addStartingWindowsForVisibleActivities(taskSwitch);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean topActivityOccludesKeyguard() {
        return this.mTopActivityOccludesKeyguard;
    }

    /* access modifiers changed from: package-private */
    public boolean resizeStackWithLaunchBounds() {
        return inPinnedWindowingMode();
    }

    @Override // com.android.server.wm.ConfigurationContainer
    public boolean supportsSplitScreenWindowingMode() {
        TaskRecord topTask = topTask();
        return super.supportsSplitScreenWindowingMode() && (topTask == null || topTask.supportsSplitScreenWindowingMode());
    }

    /* access modifiers changed from: package-private */
    public boolean affectedBySplitScreenResize() {
        int windowingMode;
        if (!supportsSplitScreenWindowingMode() || (windowingMode = getWindowingMode()) == 5 || windowingMode == 2) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public ActivityRecord getTopDismissingKeyguardActivity() {
        return this.mTopDismissingKeyguardActivity;
    }

    /* access modifiers changed from: package-private */
    public boolean checkKeyguardVisibility(ActivityRecord r, boolean shouldBeVisible, boolean isTop) {
        boolean dismissKeyguard;
        int displayId = this.mDisplayId;
        if (displayId == -1) {
            displayId = 0;
        }
        boolean keyguardOrAodShowing = this.mStackSupervisor.getKeyguardController().isKeyguardOrAodShowing(displayId);
        boolean keyguardLocked = this.mStackSupervisor.getKeyguardController().isKeyguardLocked();
        boolean showWhenLocked = r.canShowWhenLocked();
        boolean dismissKeyguard2 = r.mAppWindowToken != null && r.mAppWindowToken.containsDismissKeyguardWindow();
        if (this.mService.mColorAtmsEx.execInterceptWindow(this.mWindowManager.mContext, r, keyguardLocked, showWhenLocked, dismissKeyguard2, true)) {
            showWhenLocked = false;
            dismissKeyguard = false;
        } else {
            dismissKeyguard = dismissKeyguard2;
        }
        if (ActivityTaskManagerDebugConfig.DEBUG_VISIBILITY) {
            Slog.d(TAG, "checkKeyguardVisibility,keyguardOrAodShowing:" + keyguardOrAodShowing + ",keyguardLocked:" + keyguardLocked + ",showWhenLocked:" + showWhenLocked + ",dismissKeyguard:" + dismissKeyguard + ",shouldBeVisible:" + shouldBeVisible);
        }
        if (shouldBeVisible) {
            if (dismissKeyguard && this.mTopDismissingKeyguardActivity == null) {
                this.mTopDismissingKeyguardActivity = r;
            }
            if (isTop) {
                this.mTopActivityOccludesKeyguard |= showWhenLocked;
            }
            if (canShowWithInsecureKeyguard() && this.mStackSupervisor.getKeyguardController().canDismissKeyguard()) {
                return true;
            }
        }
        if (keyguardOrAodShowing) {
            if (!shouldBeVisible || !this.mStackSupervisor.getKeyguardController().canShowActivityWhileKeyguardShowing(r, dismissKeyguard)) {
                return false;
            }
            return true;
        } else if (!keyguardLocked) {
            return shouldBeVisible;
        } else {
            if (!shouldBeVisible || !this.mStackSupervisor.getKeyguardController().canShowWhileOccluded(dismissKeyguard, showWhenLocked)) {
                return false;
            }
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean canShowWithInsecureKeyguard() {
        ActivityDisplay activityDisplay = getDisplay();
        if (activityDisplay != null) {
            return (activityDisplay.mDisplay.getFlags() & 32) != 0;
        }
        throw new IllegalStateException("Stack is not attached to any display, stackId=" + this.mStackId);
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
        boolean z = false;
        if (isTop || !r.visible) {
            if (ActivityTaskManagerDebugConfig.DEBUG_VISIBILITY) {
                Slog.v(TAG_VISIBILITY, "Start and freeze screen for " + r);
            }
            if (r != starting) {
                r.startFreezingScreenLocked(r.app, configChanges);
            }
            if (!r.visible || r.mLaunchTaskBehind) {
                if (ActivityTaskManagerDebugConfig.DEBUG_VISIBILITY) {
                    Slog.v(TAG_VISIBILITY, "Starting and making visible: " + r);
                }
                r.setVisible(true);
            }
            if (r != starting) {
                ActivityStackSupervisor activityStackSupervisor = this.mStackSupervisor;
                if (andResume && !r.mLaunchTaskBehind) {
                    z = true;
                }
                activityStackSupervisor.startSpecificActivityLocked(r, z, true);
                return true;
            } else if (inSplitScreenPrimaryWindowingMode() && r == starting && !r.attachedToProcess() && OppoFeatureCache.get(IColorAccessControlLocalManager.DEFAULT).isAppUnlockPasswordActivity(r)) {
                this.mStackSupervisor.startSpecificActivityLocked(r, false, true);
                return true;
            }
        }
        return false;
    }

    private void makeInvisible(ActivityRecord r) {
        if (r.visible) {
            if (ActivityTaskManagerDebugConfig.DEBUG_VISIBILITY) {
                String str = TAG_VISIBILITY;
                Slog.v(str, "Making invisible: " + r + StringUtils.SPACE + r.getState());
            }
            try {
                boolean canEnterPictureInPicture = r.checkEnterPictureInPictureState("makeInvisible", true);
                r.setDeferHidingClient(canEnterPictureInPicture && !r.isState(ActivityState.STOPPING, ActivityState.STOPPED, ActivityState.PAUSED));
                r.setVisible(false);
                switch (r.getState()) {
                    case STOPPING:
                    case STOPPED:
                        if (r.attachedToProcess()) {
                            if (ActivityTaskManagerDebugConfig.DEBUG_VISIBILITY) {
                                String str2 = TAG_VISIBILITY;
                                Slog.v(str2, "Scheduling invisibility: " + r);
                            }
                            this.mService.getLifecycleManager().scheduleTransaction(r.app.getThread(), (IBinder) r.appToken, (ClientTransactionItem) WindowVisibilityItem.obtain(false));
                        }
                        r.supportsEnterPipOnTaskSwitch = false;
                        return;
                    case INITIALIZING:
                    case RESUMED:
                    case PAUSING:
                    case PAUSED:
                        addToStopping(r, true, canEnterPictureInPicture, "makeInvisible");
                        return;
                    default:
                        return;
                }
            } catch (Exception e) {
                Slog.w(TAG, "Exception thrown making hidden: " + r.intent.getComponent(), e);
            }
        } else if (ActivityTaskManagerDebugConfig.DEBUG_VISIBILITY) {
            String str3 = TAG_VISIBILITY;
            Slog.v(str3, "Already invisible: " + r);
        }
    }

    private boolean updateBehindFullscreen(boolean stackInvisible, boolean behindFullscreenActivity, ActivityRecord r) {
        if (!r.fullscreen) {
            return behindFullscreenActivity;
        }
        if (ActivityTaskManagerDebugConfig.DEBUG_VISIBILITY) {
            String str = TAG_VISIBILITY;
            Slog.v(str, "Fullscreen: at " + r + " stackInvisible=" + stackInvisible + " behindFullscreenActivity=" + behindFullscreenActivity);
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public void convertActivityToTranslucent(ActivityRecord r) {
        this.mTranslucentActivityWaiting = r;
        this.mUndrawnActivitiesBelowTopTranslucent.clear();
        this.mHandler.sendEmptyMessageDelayed(106, TRANSLUCENT_CONVERSION_TIMEOUT);
    }

    /* access modifiers changed from: package-private */
    public void clearOtherAppTimeTrackers(AppTimeTracker except) {
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ArrayList<ActivityRecord> activities = this.mTaskHistory.get(taskNdx).mActivities;
            for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                ActivityRecord r = activities.get(activityNdx);
                if (r.appTimeTracker != except) {
                    r.appTimeTracker = null;
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void notifyActivityDrawnLocked(ActivityRecord r) {
        if (r == null || (this.mUndrawnActivitiesBelowTopTranslucent.remove(r) && this.mUndrawnActivitiesBelowTopTranslucent.isEmpty())) {
            ActivityRecord waitingActivity = this.mTranslucentActivityWaiting;
            this.mTranslucentActivityWaiting = null;
            this.mUndrawnActivitiesBelowTopTranslucent.clear();
            this.mHandler.removeMessages(106);
            if (waitingActivity != null) {
                boolean z = false;
                this.mWindowManager.setWindowOpaque(waitingActivity.appToken, false);
                if (waitingActivity.attachedToProcess()) {
                    try {
                        IApplicationThread thread = waitingActivity.app.getThread();
                        IApplicationToken.Stub stub = waitingActivity.appToken;
                        if (r != null) {
                            z = true;
                        }
                        thread.scheduleTranslucentConversionComplete(stub, z);
                    } catch (RemoteException e) {
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void cancelInitializingActivities() {
        boolean z;
        ActivityRecord topActivity = topRunningActivityLocked();
        boolean aboveTop = true;
        boolean behindFullscreenActivity = false;
        if (!shouldBeVisible(null)) {
            aboveTop = false;
            behindFullscreenActivity = true;
        }
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ArrayList<ActivityRecord> activities = this.mTaskHistory.get(taskNdx).mActivities;
            for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                ActivityRecord r = activities.get(activityNdx);
                if (aboveTop) {
                    if (r == topActivity) {
                        aboveTop = false;
                    }
                    z = r.fullscreen;
                } else {
                    r.removeOrphanedStartingWindow(behindFullscreenActivity);
                    z = r.fullscreen;
                }
                behindFullscreenActivity |= z;
            }
        }
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: package-private */
    @GuardedBy({"mService"})
    public boolean resumeTopActivityUncheckedLocked(ActivityRecord prev, ActivityOptions options) {
        if (this.mInResumeTopActivity) {
            return false;
        }
        try {
            this.mInResumeTopActivity = true;
            boolean result = resumeTopActivityInnerLocked(prev, options);
            ActivityRecord next = topRunningActivityLocked(true);
            if (next == null || !next.canTurnScreenOn()) {
                checkReadyForSleep();
            }
            this.mInResumeTopActivity = false;
            return result;
        } catch (Throwable th) {
            this.mInResumeTopActivity = false;
            throw th;
        }
    }

    /* access modifiers changed from: protected */
    public ActivityRecord getResumedActivity() {
        return this.mResumedActivity;
    }

    private void setResumedActivity(ActivityRecord r, String reason) {
        if (this.mResumedActivity != r) {
            if (ActivityTaskManagerDebugConfig.DEBUG_STACK) {
                String str = TAG_STACK;
                Slog.d(str, "setResumedActivity stack:" + this + " + from: " + this.mResumedActivity + " to:" + r + " reason:" + reason);
            }
            if (!(r == null || r.app == null)) {
                String value = r.processName;
                if (value != null && value.length() > 50) {
                    value = value.substring(0, 50);
                }
                SystemProperties.set("debug.junk.process.name", value);
                SystemProperties.set("debug.junk.process.pid", Integer.toString(r.app.getPid()));
            }
            if (!(r == null || r.app == null || inFreeformWindowingMode())) {
                try {
                    if (mHyp == null) {
                        mHyp = Hypnus.getHypnus();
                    }
                    if (mHyp != null) {
                        mHyp.hypnusSetScene(r.app.getPid(), r.app.mName);
                        if (ActivityTaskManagerDebugConfig.DEBUG_STACK) {
                            Slog.d(TAG, "setResumedActivityLocked hypnusSetScene: " + r.app.getPid() + " :" + r.app.mName);
                        }
                    }
                } catch (Exception e) {
                    Slog.w(TAG, "setResumedActivityLocked hypnusSetScene has exception ", e);
                }
            }
            this.mResumedActivity = r;
            this.mStackSupervisor.updateTopResumedActivityIfNeeded();
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:239:0x04e4  */
    /* JADX WARNING: Removed duplicated region for block: B:240:0x04e8  */
    /* JADX WARNING: Removed duplicated region for block: B:247:0x0508  */
    /* JADX WARNING: Removed duplicated region for block: B:248:0x050d  */
    /* JADX WARNING: Removed duplicated region for block: B:256:0x0543  */
    /* JADX WARNING: Removed duplicated region for block: B:257:0x0546  */
    /* JADX WARNING: Removed duplicated region for block: B:260:0x0551  */
    /* JADX WARNING: Removed duplicated region for block: B:266:0x05b4  */
    /* JADX WARNING: Removed duplicated region for block: B:271:0x05c8  */
    /* JADX WARNING: Removed duplicated region for block: B:272:0x05cd  */
    /* JADX WARNING: Removed duplicated region for block: B:274:0x05d0  */
    /* JADX WARNING: Removed duplicated region for block: B:280:0x0605  */
    /* JADX WARNING: Removed duplicated region for block: B:366:0x0868  */
    /* JADX WARNING: Removed duplicated region for block: B:369:0x088b  */
    /* JADX WARNING: Removed duplicated region for block: B:372:0x08a8  */
    /* JADX WARNING: Removed duplicated region for block: B:373:0x08ad  */
    /* JADX WARNING: Removed duplicated region for block: B:381:0x08c5  */
    @GuardedBy({"mService"})
    private boolean resumeTopActivityInnerLocked(ActivityRecord prev, ActivityOptions options) {
        boolean lastResumedCanPip;
        ActivityRecord lastResumed;
        boolean userLeaving;
        ActivityRecord lastResumed2;
        ActivityRecord lastResumedBeforeActivitySwitch;
        String str;
        ActivityStack lastFocusedStack;
        boolean pausing;
        boolean anim;
        ActivityStack lastStack;
        ComponentName componentName;
        ActivityStack lastStack2;
        ActivityState lastState;
        boolean notUpdated;
        ActivityRecord lastResumedActivity;
        ActivityState lastState2;
        boolean z;
        ActivityRecord lastResumedActivity2;
        boolean z2;
        ActivityRecord activityRecord;
        String pkgName;
        String pkgName2;
        int i;
        Hypnus hypnus;
        int i2;
        Hypnus hypnus2;
        ActivityRecord activityRecord2;
        boolean z3;
        if (!this.mService.isBooting() && !this.mService.isBooted()) {
            return false;
        }
        ActivityRecord next = topRunningActivityLocked(true);
        boolean hasRunningActivity = next != null;
        if (hasRunningActivity && !isAttached()) {
            return false;
        }
        if (ActivityTaskManagerDebugConfig.DEBUG_STACK || ActivityTaskManagerDebugConfig.DEBUG_STATES || ActivityTaskManagerDebugConfig.DEBUG_AMS) {
            Slog.d(TAG, "resumeTopActivityLocked, prev:" + prev + " call by " + Debug.getCallers(6));
        }
        this.mRootActivityContainer.cancelInitializingActivities();
        if (ActivityTaskManagerDebugConfig.DEBUG_STATES || ActivityTaskManagerDebugConfig.DEBUG_AMS) {
            Slog.d(TAG, "resumeTopActivityLocked: next " + next);
        }
        if (!(next == null || next.shortComponentName == null || !mDefaultAlbumBrowserList.contains(next.shortComponentName))) {
            SystemProperties.set("debug.jpegfull.opt.switch", "0");
            Slog.d(TAG, "yac: NOW RESUME " + next.shortComponentName + "set jpegfull 0");
        }
        boolean userLeaving2 = this.mStackSupervisor.mUserLeaving;
        this.mStackSupervisor.mUserLeaving = false;
        if (!hasRunningActivity) {
            return resumeNextFocusableActivityWhenStackIsEmpty(prev, options);
        }
        next.delayedResume = false;
        ActivityDisplay display = getDisplay();
        if (this.mResumedActivity == next && next.isState(ActivityState.RESUMED) && display.allResumedActivitiesComplete()) {
            executeAppTransition(options);
            if (ActivityTaskManagerDebugConfig.DEBUG_STATES) {
                Slog.d(TAG_STATES, "resumeTopActivityLocked: Top activity resumed " + next);
            }
            return false;
        } else if (!next.canResumeByCompat()) {
            return false;
        } else {
            if (shouldSleepOrShutDownActivities() && this.mLastPausedActivity == next && this.mRootActivityContainer.allPausedActivitiesComplete()) {
                boolean nothingToResume = true;
                if (!this.mService.mShuttingDown) {
                    boolean canShowWhenLocked = !this.mTopActivityOccludesKeyguard && next.canShowWhenLocked();
                    boolean mayDismissKeyguard = (this.mTopDismissingKeyguardActivity == next || next.mAppWindowToken == null || !next.mAppWindowToken.containsDismissKeyguardWindow()) ? false : true;
                    if (canShowWhenLocked || mayDismissKeyguard) {
                        ensureActivitiesVisibleLocked(null, 0, false);
                        nothingToResume = shouldSleepActivities();
                    }
                }
                if (nothingToResume) {
                    executeAppTransition(options);
                    if (ActivityTaskManagerDebugConfig.DEBUG_STATES) {
                        Slog.d(TAG_STATES, "resumeTopActivityLocked: Going to sleep and all paused");
                    }
                    return false;
                }
            }
            if (!this.mService.mAmInternal.hasStartedUserState(next.mUserId)) {
                Slog.w(TAG, "Skipping resume of top activity " + next + ": user " + next.mUserId + " is stopped");
                return false;
            }
            this.mStackSupervisor.mStoppingActivities.remove(next);
            this.mStackSupervisor.mGoingToSleepActivities.remove(next);
            next.sleeping = false;
            if (ActivityTaskManagerDebugConfig.DEBUG_SWITCH) {
                Slog.v(TAG_SWITCH, "Resuming " + next);
            }
            if (!this.mRootActivityContainer.allPausedActivitiesComplete()) {
                if (ActivityTaskManagerDebugConfig.DEBUG_SWITCH || ActivityTaskManagerDebugConfig.DEBUG_PAUSE || ActivityTaskManagerDebugConfig.DEBUG_STATES) {
                    Slog.v(TAG_PAUSE, "resumeTopActivityLocked: Skip resume: some activity pausing.");
                }
                return false;
            }
            this.mStackSupervisor.setLaunchSource(next.info.applicationInfo.uid);
            ActivityStack lastFocusedStack2 = display.getLastFocusedStack();
            if (lastFocusedStack2 == null || lastFocusedStack2 == this) {
                userLeaving = userLeaving2;
                lastResumedCanPip = false;
                lastResumed = null;
            } else {
                ActivityRecord lastResumed3 = lastFocusedStack2.mResumedActivity;
                if (userLeaving2 && inMultiWindowMode() && lastFocusedStack2.shouldBeVisible(next)) {
                    if (ActivityTaskManagerDebugConfig.DEBUG_USER_LEAVING) {
                        Slog.i(TAG_USER_LEAVING, "Overriding userLeaving to false next=" + next + " lastResumed=" + lastResumed3);
                    }
                    userLeaving2 = false;
                }
                userLeaving = userLeaving2;
                lastResumedCanPip = lastResumed3 != null && lastResumed3.checkEnterPictureInPictureState("resumeTopActivity", userLeaving2);
                lastResumed = lastResumed3;
            }
            boolean resumeWhilePausing = (next.info.flags & 16384) != 0 && !lastResumedCanPip;
            ActivityRecord lastResumedBeforeActivitySwitch2 = lastResumed != null ? lastResumed : this.mResumedActivity;
            boolean pausing2 = getDisplay().pauseBackStacks(userLeaving, next, false);
            if (this.mResumedActivity != null) {
                if (ActivityTaskManagerDebugConfig.DEBUG_STATES) {
                    Slog.d(TAG_STATES, "resumeTopActivityLocked: Pausing " + this.mResumedActivity);
                }
                lastResumedBeforeActivitySwitch = lastResumedBeforeActivitySwitch2;
                lastFocusedStack = lastFocusedStack2;
                lastResumed2 = lastResumed;
                str = TAG;
                pausing = pausing2 | startPausingLocked(userLeaving, false, next, false, "resume-request");
            } else {
                lastResumedBeforeActivitySwitch = lastResumedBeforeActivitySwitch2;
                lastFocusedStack = lastFocusedStack2;
                lastResumed2 = lastResumed;
                str = TAG;
                pausing = pausing2;
            }
            this.mService.mAmsExt.onBeforeActivitySwitch(lastResumedBeforeActivitySwitch, next, pausing, next.getActivityType());
            if (pausing && !resumeWhilePausing) {
                if (ActivityTaskManagerDebugConfig.DEBUG_SWITCH || ActivityTaskManagerDebugConfig.DEBUG_STATES) {
                    Slog.v(TAG_STATES, "resumeTopActivityLocked: Skip resume: need to start pausing");
                }
                if (next.attachedToProcess()) {
                    z3 = true;
                    next.app.updateProcessInfo(false, true, false);
                } else {
                    z3 = true;
                }
                if (lastResumed2 != null) {
                    lastResumed2.setWillCloseOrEnterPip(z3);
                }
                return z3;
            } else if (this.mResumedActivity == next && next.isState(ActivityState.RESUMED) && display.allResumedActivitiesComplete()) {
                executeAppTransition(options);
                if (!ActivityTaskManagerDebugConfig.DEBUG_STATES) {
                    return true;
                }
                Slog.d(TAG_STATES, "resumeTopActivityLocked: Top activity resumed (dontWaitForPause) " + next);
                return true;
            } else if (!OppoFeatureCache.get(IColorAccessControlLocalManager.DEFAULT).interceptResumeActivity(next, next.mUserId)) {
                if (shouldSleepActivities() && (activityRecord2 = this.mLastNoHistoryActivity) != null && !activityRecord2.finishing) {
                    if (ActivityTaskManagerDebugConfig.DEBUG_STATES) {
                        Slog.d(TAG_STATES, "no-history finish of " + this.mLastNoHistoryActivity + " on new resume");
                    }
                    requestFinishActivityLocked(this.mLastNoHistoryActivity.appToken, 0, null, "resume-no-history", false);
                    this.mLastNoHistoryActivity = null;
                }
                if (!(prev == null || prev == next || !next.nowVisible)) {
                    if (prev.finishing) {
                        prev.setVisibility(false);
                        if (ActivityTaskManagerDebugConfig.DEBUG_SWITCH) {
                            Slog.v(TAG_SWITCH, "Not waiting for visible to hide: " + prev + ", nowVisible=" + next.nowVisible);
                        }
                    } else if (ActivityTaskManagerDebugConfig.DEBUG_SWITCH) {
                        Slog.v(TAG_SWITCH, "Previous already visible but still waiting to hide: " + prev + ", nowVisible=" + next.nowVisible);
                    }
                }
                try {
                    AppGlobals.getPackageManager().setPackageStoppedState(next.packageName, false, next.mUserId);
                } catch (RemoteException e) {
                } catch (IllegalArgumentException e2) {
                    Slog.w(str, "Failed trying to unstop package " + next.packageName + ": " + e2);
                }
                boolean anim2 = true;
                DisplayContent dc = getDisplay().mDisplayContent;
                if (mHyp == null) {
                    mHyp = Hypnus.getHypnus();
                }
                if (prev != null) {
                    if (prev.finishing) {
                        if (ActivityTaskManagerDebugConfig.DEBUG_TRANSITION) {
                            Slog.v(TAG_TRANSITION, "Prepare close transition: prev=" + prev);
                        }
                        if (this.mStackSupervisor.mNoAnimActivities.contains(prev)) {
                            anim2 = false;
                            dc.prepareAppTransition(0, false);
                        } else {
                            if (prev.getTaskRecord() == next.getTaskRecord()) {
                                i2 = 7;
                            } else {
                                i2 = 9;
                            }
                            dc.prepareAppTransition(i2, false);
                            if (!(prev.getTaskRecord() == next.getTaskRecord() || (hypnus2 = mHyp) == null)) {
                                hypnus2.hypnusSetAction(38, (int) SystemService.PHASE_THIRD_PARTY_APPS_CAN_START);
                            }
                        }
                        prev.setVisibility(false);
                        anim = anim2;
                    } else {
                        if (ActivityTaskManagerDebugConfig.DEBUG_TRANSITION) {
                            Slog.v(TAG_TRANSITION, "Prepare open transition: prev=" + prev);
                        }
                        if (this.mStackSupervisor.mNoAnimActivities.contains(next)) {
                            dc.prepareAppTransition(0, false);
                            anim = false;
                        } else {
                            if (prev.getTaskRecord() == next.getTaskRecord()) {
                                i = 6;
                            } else if (next.mLaunchTaskBehind) {
                                i = 16;
                            } else {
                                i = 8;
                            }
                            dc.prepareAppTransition(i, false);
                            if (!(prev.getTaskRecord() == next.getTaskRecord() || (hypnus = mHyp) == null)) {
                                hypnus.hypnusSetAction(38, (int) SystemService.PHASE_THIRD_PARTY_APPS_CAN_START);
                            }
                        }
                    }
                    if (anim) {
                        next.applyOptionsLocked();
                    } else {
                        next.clearOptionsLocked();
                    }
                    this.mStackSupervisor.mNoAnimActivities.clear();
                    ComponentName componentName2 = this.mLastRecordCmpName;
                    if (componentName2 == null || !componentName2.equals(this.mComponentName)) {
                        this.mLastRecordCmpName = this.mComponentName;
                        ComponentName componentName3 = this.mLastRecordCmpName;
                        pkgName2 = componentName3 != null ? componentName3.getPackageName() : null;
                        if (pkgName2 != null && !pkgName2.equals(this.mLastRecordPkgName)) {
                            this.mLastRecordPkgName = pkgName2;
                            Time tobj = new Time();
                            tobj.set(System.currentTimeMillis());
                            this.mHandler.postDelayed(new UsageRecorderRunnable(pkgName2, tobj.format("%Y-%m-%d %H:%M:%S")), 400);
                        }
                    }
                    lastStack = getDisplay().getLastFocusedStack();
                    ComponentName componentName4 = lastStack != null ? lastStack.mComponentName : null;
                    if (next.getTaskRecord() == null && next.getTaskRecord().mLastNextActivity != null) {
                        Slog.v(str, "lastNextActivity: " + next.getTaskRecord().mLastNextActivity);
                        next.getTaskRecord().mLastNextActivity = null;
                    }
                    OppoFeatureCache.get(IColorHansManager.DEFAULT).hansTopActivityIfNeeded(next.appInfo.uid, next.appInfo.packageName, getWindowModeForHans(next));
                    OppoFeatureCache.get(IColorHansManager.DEFAULT).hansTopActivityIfNeeded(next.appInfo.uid, next.appInfo.packageName);
                    this.mService.startSecurityPayService(prev, next);
                    componentName = this.mLastRecordCmpName;
                    if (componentName != null || !componentName.equals(this.mComponentName)) {
                        this.mLastRecordCmpName = this.mComponentName;
                        ComponentName componentName5 = this.mLastRecordCmpName;
                        pkgName = componentName5 != null ? componentName5.getPackageName() : null;
                        if (pkgName != null || pkgName.equals(this.mLastRecordPkgName)) {
                            lastStack2 = lastStack;
                        } else {
                            this.mLastRecordPkgName = pkgName;
                            Time tobj2 = new Time();
                            lastStack2 = lastStack;
                            tobj2.set(System.currentTimeMillis());
                            this.mHandler.postDelayed(new UsageRecorderRunnable(pkgName, tobj2.format("%Y-%m-%d %H:%M:%S")), 400);
                        }
                    } else {
                        lastStack2 = lastStack;
                    }
                    this.mComponentName = next.mActivityComponent;
                    if (next.attachedToProcess()) {
                        if (ActivityTaskManagerDebugConfig.DEBUG_SWITCH) {
                            Slog.v(TAG_SWITCH, "Resume running: " + next + " stopped=" + next.stopped + " visible=" + next.visible);
                        }
                        boolean lastActivityTranslucent = lastFocusedStack != null && (lastFocusedStack.inMultiWindowMode() || ((activityRecord = lastFocusedStack.mLastPausedActivity) != null && !activityRecord.fullscreen));
                        if (!next.visible || next.stopped || lastActivityTranslucent) {
                            next.setVisibility(true);
                        }
                        next.startLaunchTickingLocked();
                        ActivityRecord lastResumedActivity3 = lastFocusedStack == null ? null : lastFocusedStack.mResumedActivity;
                        ActivityState lastState3 = next.getState();
                        this.mService.updateCpuStats();
                        if (ActivityTaskManagerDebugConfig.DEBUG_STATES) {
                            Slog.v(TAG_STATES, "Moving to RESUMED: " + next + " (in existing) call by " + Debug.getCallers(8));
                        }
                        next.setState(ActivityState.RESUMED, "resumeTopActivityInnerLocked");
                        this.mService.mAmsExt.onAfterActivityResumed(next);
                        next.app.updateProcessInfo(false, true, true);
                        updateLRUListLocked(next);
                        if (shouldBeVisible(next)) {
                            lastState = lastState3;
                            notUpdated = !this.mRootActivityContainer.ensureVisibilityAndConfig(next, this.mDisplayId, true, false);
                        } else {
                            notUpdated = true;
                            lastState = lastState3;
                        }
                        if (notUpdated) {
                            ActivityRecord nextNext = topRunningActivityLocked();
                            if (ActivityTaskManagerDebugConfig.DEBUG_SWITCH || ActivityTaskManagerDebugConfig.DEBUG_STATES) {
                                Slog.i(TAG_STATES, "Activity config changed during resume: " + next + ", new next: " + nextNext);
                            }
                            if (nextNext != next) {
                                this.mStackSupervisor.scheduleResumeTopActivities();
                            }
                            if (!next.visible || next.stopped) {
                                z2 = true;
                                next.setVisibility(true);
                            } else {
                                z2 = true;
                            }
                            next.completeResumeLocked();
                            return z2;
                        }
                        try {
                            ClientTransaction transaction = ClientTransaction.obtain(next.app.getThread(), next.appToken);
                            ArrayList<ResultInfo> a = next.results;
                            if (a != null) {
                                try {
                                    int N = a.size();
                                    if (next.finishing || N <= 0) {
                                        lastResumedActivity2 = lastResumedActivity3;
                                    } else {
                                        if (ActivityTaskManagerDebugConfig.DEBUG_RESULTS) {
                                            String str2 = TAG_RESULTS;
                                            StringBuilder sb = new StringBuilder();
                                            lastResumedActivity2 = lastResumedActivity3;
                                            try {
                                                sb.append("Delivering results to ");
                                                sb.append(next);
                                                sb.append(": ");
                                                sb.append(a);
                                                Slog.v(str2, sb.toString());
                                            } catch (Exception e3) {
                                                lastState2 = lastState;
                                                lastResumedActivity = lastResumedActivity2;
                                                if (ActivityTaskManagerDebugConfig.DEBUG_STATES) {
                                                }
                                                next.setState(lastState2, "resumeTopActivityInnerLocked");
                                                if (lastResumedActivity != null) {
                                                }
                                                Slog.i(str, "Restarting because process died: " + next);
                                                if (next.hasBeenLaunched) {
                                                }
                                                this.mStackSupervisor.startSpecificActivityLocked(next, true, z);
                                                return true;
                                            }
                                        } else {
                                            lastResumedActivity2 = lastResumedActivity3;
                                        }
                                        transaction.addCallback(ActivityResultItem.obtain(a));
                                    }
                                } catch (Exception e4) {
                                    lastResumedActivity = lastResumedActivity3;
                                    lastState2 = lastState;
                                    if (ActivityTaskManagerDebugConfig.DEBUG_STATES) {
                                    }
                                    next.setState(lastState2, "resumeTopActivityInnerLocked");
                                    if (lastResumedActivity != null) {
                                    }
                                    Slog.i(str, "Restarting because process died: " + next);
                                    if (next.hasBeenLaunched) {
                                    }
                                    this.mStackSupervisor.startSpecificActivityLocked(next, true, z);
                                    return true;
                                }
                            } else {
                                lastResumedActivity2 = lastResumedActivity3;
                            }
                            try {
                                if (next.newIntents != null) {
                                    transaction.addCallback(NewIntentItem.obtain(next.newIntents, true));
                                }
                                next.notifyAppResumed(next.stopped);
                                EventLog.writeEvent((int) EventLogTags.AM_RESUME_ACTIVITY, Integer.valueOf(next.mUserId), Integer.valueOf(System.identityHashCode(next)), Integer.valueOf(next.getTaskRecord().taskId), next.shortComponentName);
                                next.sleeping = false;
                                this.mService.getAppWarningsLocked().onResumeActivity(next);
                                next.app.setPendingUiCleanAndForceProcessStateUpTo(this.mService.mTopProcessState);
                                next.clearOptionsLocked();
                                transaction.setLifecycleStateRequest(ResumeActivityItem.obtain(next.app.getReportedProcState(), getDisplay().mDisplayContent.isNextTransitionForward()));
                                this.mService.getLifecycleManager().scheduleTransaction(transaction);
                                if (ActivityTaskManagerDebugConfig.DEBUG_STATES) {
                                    Slog.d(TAG_STATES, "resumeTopActivityLocked: Resumed " + next);
                                }
                                OppoFeatureCache.getOrCreate(IColorAntiVirusBehaviorManager.DEFAULT, new Object[0]).setForegroundApp(next.packageName);
                                try {
                                    next.completeResumeLocked();
                                    return true;
                                } catch (Exception e5) {
                                    Slog.w(str, "Exception thrown during resume of " + next, e5);
                                    requestFinishActivityLocked(next.appToken, 0, null, "resume-exception", true);
                                    return true;
                                }
                            } catch (Exception e6) {
                                lastState2 = lastState;
                                lastResumedActivity = lastResumedActivity2;
                                if (ActivityTaskManagerDebugConfig.DEBUG_STATES) {
                                    Slog.v(TAG_STATES, "Resume failed; resetting state to " + lastState2 + ": " + next);
                                }
                                next.setState(lastState2, "resumeTopActivityInnerLocked");
                                if (lastResumedActivity != null) {
                                    lastResumedActivity.setState(ActivityState.RESUMED, "resumeTopActivityInnerLocked");
                                }
                                Slog.i(str, "Restarting because process died: " + next);
                                if (next.hasBeenLaunched) {
                                    next.hasBeenLaunched = true;
                                    z = false;
                                } else if (lastFocusedStack == null) {
                                    z = false;
                                } else if (lastFocusedStack.isTopStackOnDisplay()) {
                                    z = false;
                                    next.showStartingWindow(null, false, false);
                                } else {
                                    z = false;
                                }
                                this.mStackSupervisor.startSpecificActivityLocked(next, true, z);
                                return true;
                            }
                        } catch (Exception e7) {
                            lastResumedActivity = lastResumedActivity3;
                            lastState2 = lastState;
                            if (ActivityTaskManagerDebugConfig.DEBUG_STATES) {
                            }
                            next.setState(lastState2, "resumeTopActivityInnerLocked");
                            if (lastResumedActivity != null) {
                            }
                            Slog.i(str, "Restarting because process died: " + next);
                            if (next.hasBeenLaunched) {
                            }
                            this.mStackSupervisor.startSpecificActivityLocked(next, true, z);
                            return true;
                        }
                    } else {
                        if (!next.hasBeenLaunched) {
                            next.hasBeenLaunched = true;
                        } else {
                            next.showStartingWindow(null, false, false);
                            if (ActivityTaskManagerDebugConfig.DEBUG_SWITCH) {
                                Slog.v(TAG_SWITCH, "Restarting: " + next);
                            }
                        }
                        if (ActivityTaskManagerDebugConfig.DEBUG_STATES) {
                            Slog.d(TAG_STATES, "resumeTopActivityLocked: Restarting " + next);
                        }
                        this.mStackSupervisor.startSpecificActivityLocked(next, true, true);
                        return true;
                    }
                } else {
                    if (ActivityTaskManagerDebugConfig.DEBUG_TRANSITION) {
                        Slog.v(TAG_TRANSITION, "Prepare open transition: no previous");
                    }
                    if (this.mStackSupervisor.mNoAnimActivities.contains(next)) {
                        dc.prepareAppTransition(0, false);
                        anim = false;
                        if (anim) {
                        }
                        this.mStackSupervisor.mNoAnimActivities.clear();
                        ComponentName componentName22 = this.mLastRecordCmpName;
                        this.mLastRecordCmpName = this.mComponentName;
                        ComponentName componentName32 = this.mLastRecordCmpName;
                        if (componentName32 != null) {
                        }
                        this.mLastRecordPkgName = pkgName2;
                        Time tobj3 = new Time();
                        tobj3.set(System.currentTimeMillis());
                        this.mHandler.postDelayed(new UsageRecorderRunnable(pkgName2, tobj3.format("%Y-%m-%d %H:%M:%S")), 400);
                        lastStack = getDisplay().getLastFocusedStack();
                        if (lastStack != null) {
                        }
                        if (next.getTaskRecord() == null) {
                        }
                        OppoFeatureCache.get(IColorHansManager.DEFAULT).hansTopActivityIfNeeded(next.appInfo.uid, next.appInfo.packageName, getWindowModeForHans(next));
                        OppoFeatureCache.get(IColorHansManager.DEFAULT).hansTopActivityIfNeeded(next.appInfo.uid, next.appInfo.packageName);
                        this.mService.startSecurityPayService(prev, next);
                        componentName = this.mLastRecordCmpName;
                        if (componentName != null) {
                        }
                        this.mLastRecordCmpName = this.mComponentName;
                        ComponentName componentName52 = this.mLastRecordCmpName;
                        if (componentName52 != null) {
                        }
                        if (pkgName != null) {
                        }
                        lastStack2 = lastStack;
                        this.mComponentName = next.mActivityComponent;
                        if (next.attachedToProcess()) {
                        }
                    } else {
                        dc.prepareAppTransition(6, false);
                    }
                }
                anim = true;
                if (anim) {
                }
                this.mStackSupervisor.mNoAnimActivities.clear();
                ComponentName componentName222 = this.mLastRecordCmpName;
                this.mLastRecordCmpName = this.mComponentName;
                ComponentName componentName322 = this.mLastRecordCmpName;
                if (componentName322 != null) {
                }
                this.mLastRecordPkgName = pkgName2;
                Time tobj32 = new Time();
                tobj32.set(System.currentTimeMillis());
                this.mHandler.postDelayed(new UsageRecorderRunnable(pkgName2, tobj32.format("%Y-%m-%d %H:%M:%S")), 400);
                lastStack = getDisplay().getLastFocusedStack();
                if (lastStack != null) {
                }
                if (next.getTaskRecord() == null) {
                }
                OppoFeatureCache.get(IColorHansManager.DEFAULT).hansTopActivityIfNeeded(next.appInfo.uid, next.appInfo.packageName, getWindowModeForHans(next));
                OppoFeatureCache.get(IColorHansManager.DEFAULT).hansTopActivityIfNeeded(next.appInfo.uid, next.appInfo.packageName);
                this.mService.startSecurityPayService(prev, next);
                componentName = this.mLastRecordCmpName;
                if (componentName != null) {
                }
                this.mLastRecordCmpName = this.mComponentName;
                ComponentName componentName522 = this.mLastRecordCmpName;
                if (componentName522 != null) {
                }
                if (pkgName != null) {
                }
                lastStack2 = lastStack;
                this.mComponentName = next.mActivityComponent;
                if (next.attachedToProcess()) {
                }
            } else if (!ActivityTaskManagerDebugConfig.DEBUG_STACK) {
                return true;
            } else {
                Slog.v(str, "intercept resume activity: " + next);
                return true;
            }
        }
    }

    private boolean resumeNextFocusableActivityWhenStackIsEmpty(ActivityRecord prev, ActivityOptions options) {
        ActivityStack nextFocusedStack;
        if (!isActivityTypeHome() && (nextFocusedStack = adjustFocusToNextFocusableStack("noMoreActivities")) != null) {
            return this.mRootActivityContainer.resumeFocusedStacksTopActivities(nextFocusedStack, prev, null);
        }
        ActivityOptions.abort(options);
        if (ActivityTaskManagerDebugConfig.DEBUG_STATES) {
            Slog.d(TAG_STATES, "resumeNextFocusableActivityWhenStackIsEmpty: noMoreActivities, go home");
        }
        return this.mRootActivityContainer.resumeHomeActivity(prev, "noMoreActivities", this.mDisplayId);
    }

    /* access modifiers changed from: package-private */
    public int getAdjustedPositionForTask(TaskRecord task, int suggestedPosition, ActivityRecord starting) {
        int maxPosition = this.mTaskHistory.size();
        if ((starting != null && starting.okToShowLocked()) || (starting == null && task.okToShowLocked())) {
            return Math.min(suggestedPosition, maxPosition);
        }
        while (maxPosition > 0) {
            TaskRecord tmpTask = this.mTaskHistory.get(maxPosition - 1);
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
        } else if (position <= 0) {
            insertTaskAtBottom(task);
        } else {
            int position2 = getAdjustedPositionForTask(task, position, null);
            this.mTaskHistory.remove(task);
            this.mTaskHistory.add(position2, task);
            TaskStack taskStack = this.mTaskStack;
            if (taskStack != null) {
                taskStack.positionChildAt(task.getTask(), position2);
            }
            updateTaskMovement(task, true);
        }
    }

    private void insertTaskAtTop(TaskRecord task, ActivityRecord starting) {
        if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
            Slog.d(TAG, "before insertTaskAtTop:mTaskHistory." + this.mTaskHistory);
        }
        this.mTaskHistory.remove(task);
        this.mTaskHistory.add(getAdjustedPositionForTask(task, this.mTaskHistory.size(), starting), task);
        if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
            Slog.d(TAG, "after insertTaskAtTop:mTaskHistory." + this.mTaskHistory);
        }
        updateTaskMovement(task, true);
        positionChildWindowContainerAtTop(task);
    }

    private void insertTaskAtBottom(TaskRecord task) {
        this.mTaskHistory.remove(task);
        this.mTaskHistory.add(getAdjustedPositionForTask(task, 0, null), task);
        updateTaskMovement(task, true);
        positionChildWindowContainerAtBottom(task);
    }

    /* access modifiers changed from: package-private */
    public void startActivityLocked(ActivityRecord r, ActivityRecord focusedTopActivity, boolean newTask, boolean keepCurTransition, ActivityOptions options) {
        TaskRecord rTask = r.getTaskRecord();
        int taskId = rTask.taskId;
        if (!r.mLaunchTaskBehind && (taskForIdLocked(taskId) == null || newTask)) {
            insertTaskAtTop(rTask, r);
        }
        if (r.shortComponentName != null && mDefaultAlbumBrowserList.contains(r.shortComponentName)) {
            SystemProperties.set("debug.jpegfull.opt.switch", "0");
            Slog.d(TAG, "yac: NOW START " + r.shortComponentName + " set jpegfull 0");
        }
        TaskRecord task = null;
        if (!newTask) {
            boolean startIt = true;
            int taskNdx = this.mTaskHistory.size() - 1;
            while (true) {
                if (taskNdx < 0) {
                    break;
                }
                task = this.mTaskHistory.get(taskNdx);
                if (task.getTopActivity() != null) {
                    if (task == rTask) {
                        if (!startIt) {
                            if (ActivityTaskManagerDebugConfig.DEBUG_ADD_REMOVE) {
                                Slog.i(TAG, "Adding activity " + r + " to task " + task, new RuntimeException("here").fillInStackTrace());
                            }
                            r.createAppWindowToken();
                            ActivityOptions.abort(options);
                            return;
                        }
                    } else if (task.numFullscreen > 0) {
                        startIt = false;
                    }
                }
                taskNdx--;
            }
        }
        TaskRecord activityTask = r.getTaskRecord();
        if (task == activityTask && this.mTaskHistory.indexOf(task) != this.mTaskHistory.size() - 1) {
            this.mStackSupervisor.mUserLeaving = false;
            if (ActivityTaskManagerDebugConfig.DEBUG_USER_LEAVING) {
                Slog.v(TAG_USER_LEAVING, "startActivity() behind front, mUserLeaving=false");
            }
        }
        if (ActivityTaskManagerDebugConfig.DEBUG_ADD_REMOVE) {
            Slog.i(TAG, "Adding activity " + r + " to stack to task " + activityTask, new RuntimeException("here").fillInStackTrace());
        }
        if (r.mAppWindowToken == null) {
            r.createAppWindowToken();
        }
        activityTask.setFrontOfTask();
        if (!isHomeOrRecentsStack() || numActivities() > 0) {
            DisplayContent dc = getDisplay().mDisplayContent;
            if (ActivityTaskManagerDebugConfig.DEBUG_TRANSITION) {
                Slog.v(TAG_TRANSITION, "Prepare open transition: starting " + r);
            }
            if ((r.intent.getFlags() & 65536) != 0) {
                dc.prepareAppTransition(0, keepCurTransition);
                this.mStackSupervisor.mNoAnimActivities.add(r);
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
                dc.prepareAppTransition(transit, keepCurTransition);
                this.mStackSupervisor.mNoAnimActivities.remove(r);
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
            } else if (doShow && !r.inFreeformWindowingMode()) {
                TaskRecord prevTask = r.getTaskRecord();
                ActivityRecord prev = prevTask.topRunningActivityWithStartingWindowLocked();
                if (prev != null) {
                    if (prev.getTaskRecord() != prevTask) {
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

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean canEnterPipOnTaskSwitch(ActivityRecord pipCandidate, TaskRecord toFrontTask, ActivityRecord toFrontActivity, ActivityOptions opts) {
        if ((opts != null && opts.disallowEnterPictureInPictureWhileLaunching()) || pipCandidate == null || pipCandidate.inPinnedWindowingMode()) {
            return false;
        }
        ActivityStack targetStack = toFrontTask != null ? toFrontTask.getStack() : toFrontActivity.getActivityStack();
        if (targetStack == null || !targetStack.isActivityTypeAssistant()) {
            return true;
        }
        return false;
    }

    private boolean isTaskSwitch(ActivityRecord r, ActivityRecord topFocusedActivity) {
        return (topFocusedActivity == null || r.getTaskRecord() == topFocusedActivity.getTaskRecord()) ? false : true;
    }

    /* JADX INFO: Multiple debug info for r10v6 'target'  com.android.server.wm.ActivityRecord: [D('numActivities' int), D('target' com.android.server.wm.ActivityRecord)] */
    private ActivityOptions resetTargetTaskIfNeededLocked(TaskRecord task, boolean forceReset) {
        int numActivities;
        int end;
        boolean noOptions;
        ActivityOptions topOptions;
        boolean z;
        ActivityRecord target;
        TaskRecord targetTask;
        boolean noOptions2;
        ArrayList<ActivityRecord> activities = task.mActivities;
        int numActivities2 = activities.size();
        int rootActivityNdx = task.findEffectiveRootIndex();
        ActivityOptions topOptions2 = null;
        int replyChainEnd = -1;
        boolean canMoveOptions = true;
        int i = numActivities2 - 1;
        while (true) {
            if (i <= rootActivityNdx) {
                break;
            }
            ActivityRecord target2 = activities.get(i);
            if (target2.frontOfTask) {
                break;
            }
            int flags = target2.info.flags;
            boolean finishOnTaskLaunch = (flags & 2) != 0;
            boolean allowTaskReparenting = (flags & 64) != 0;
            boolean clearWhenTaskReset = (target2.intent.getFlags() & DumpState.DUMP_FROZEN) != 0;
            if (finishOnTaskLaunch || clearWhenTaskReset || target2.resultTo == null) {
                if (finishOnTaskLaunch || clearWhenTaskReset || !allowTaskReparenting || target2.taskAffinity == null) {
                    numActivities = numActivities2;
                } else if (!target2.taskAffinity.equals(task.affinity)) {
                    ActivityRecord bottom = (this.mTaskHistory.isEmpty() || this.mTaskHistory.get(0).mActivities.isEmpty()) ? null : this.mTaskHistory.get(0).mActivities.get(0);
                    if (bottom == null || target2.taskAffinity == null || !target2.taskAffinity.equals(bottom.getTaskRecord().affinity)) {
                        numActivities = numActivities2;
                        target = target2;
                        targetTask = createTaskRecord(this.mStackSupervisor.getNextTaskIdForUserLocked(target2.mUserId), target2.info, null, null, null, false);
                        targetTask.affinityIntent = target.intent;
                        if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                            Slog.v(TAG_TASKS, "Start pushing activity " + target + " out to new task " + targetTask);
                        }
                    } else {
                        targetTask = bottom.getTaskRecord();
                        if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                            Slog.v(TAG_TASKS, "Start pushing activity " + target2 + " out to bottom task " + targetTask);
                        }
                        numActivities = numActivities2;
                        target = target2;
                    }
                    boolean noOptions3 = canMoveOptions;
                    for (int srcPos = replyChainEnd < 0 ? i : replyChainEnd; srcPos >= i; srcPos--) {
                        ActivityRecord p = activities.get(srcPos);
                        if (!p.finishing) {
                            if (noOptions3 && topOptions2 == null) {
                                topOptions2 = p.takeOptionsLocked(false);
                                if (topOptions2 != null) {
                                    noOptions3 = false;
                                }
                            }
                            if (ActivityTaskManagerDebugConfig.DEBUG_ADD_REMOVE) {
                                String str = TAG_ADD_REMOVE;
                                StringBuilder sb = new StringBuilder();
                                noOptions2 = noOptions3;
                                sb.append("Removing activity ");
                                sb.append(p);
                                sb.append(" from task=");
                                sb.append(task);
                                sb.append(" adding to task=");
                                sb.append(targetTask);
                                sb.append(" Callers=");
                                sb.append(Debug.getCallers(4));
                                Slog.i(str, sb.toString());
                            } else {
                                noOptions2 = noOptions3;
                            }
                            if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                                Slog.v(TAG_TASKS, "Pushing next activity " + p + " out to target's task " + target);
                            }
                            p.reparent(targetTask, 0, "resetTargetTaskIfNeeded");
                            canMoveOptions = false;
                            noOptions3 = noOptions2;
                        }
                    }
                    positionChildWindowContainerAtBottom(targetTask);
                    replyChainEnd = -1;
                } else {
                    numActivities = numActivities2;
                }
                if (forceReset || finishOnTaskLaunch || clearWhenTaskReset) {
                    if (clearWhenTaskReset) {
                        end = activities.size() - 1;
                    } else if (replyChainEnd < 0) {
                        end = i;
                    } else {
                        end = replyChainEnd;
                    }
                    boolean noOptions4 = canMoveOptions;
                    int srcPos2 = i;
                    int end2 = end;
                    ActivityOptions topOptions3 = topOptions2;
                    while (srcPos2 <= end2) {
                        ActivityRecord p2 = activities.get(srcPos2);
                        if (!p2.finishing) {
                            canMoveOptions = false;
                            if (!noOptions4 || topOptions3 != null) {
                                z = false;
                                topOptions = topOptions3;
                                noOptions = noOptions4;
                            } else {
                                z = false;
                                ActivityOptions topOptions4 = p2.takeOptionsLocked(false);
                                if (topOptions4 != null) {
                                    topOptions = topOptions4;
                                    noOptions = false;
                                } else {
                                    topOptions = topOptions4;
                                    noOptions = noOptions4;
                                }
                            }
                            if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                                Slog.w(TAG_TASKS, "resetTaskIntendedTask: calling finishActivity on " + p2);
                            }
                            if (finishActivityLocked(p2, 0, null, "reset-task", false)) {
                                end2--;
                                srcPos2--;
                                topOptions3 = topOptions;
                                noOptions4 = noOptions;
                            } else {
                                topOptions3 = topOptions;
                                noOptions4 = noOptions;
                            }
                        }
                        srcPos2++;
                    }
                    topOptions2 = topOptions3;
                    replyChainEnd = -1;
                } else {
                    replyChainEnd = -1;
                }
            } else if (replyChainEnd < 0) {
                replyChainEnd = i;
                numActivities = numActivities2;
            } else {
                numActivities = numActivities2;
            }
            i--;
            numActivities2 = numActivities;
        }
        return topOptions2;
    }

    private int resetAffinityTaskIfNeededLocked(TaskRecord affinityTask, TaskRecord task, boolean topTaskIsHigher, boolean forceReset, int taskInsertionPoint) {
        String taskAffinity;
        int taskId;
        ArrayList<ActivityRecord> taskActivities;
        int targetNdx;
        int taskInsertionPoint2;
        TaskRecord taskRecord = affinityTask;
        int taskId2 = task.taskId;
        String taskAffinity2 = task.affinity;
        ArrayList<ActivityRecord> activities = taskRecord.mActivities;
        int numActivities = activities.size();
        int rootActivityNdx = affinityTask.findEffectiveRootIndex();
        int i = numActivities - 1;
        int replyChainEnd = -1;
        int taskInsertionPoint3 = taskInsertionPoint;
        while (true) {
            if (i <= rootActivityNdx) {
                break;
            }
            ActivityRecord target = activities.get(i);
            if (target.frontOfTask) {
                break;
            }
            int flags = target.info.flags;
            boolean allowTaskReparenting = false;
            boolean finishOnTaskLaunch = (flags & 2) != 0;
            if ((flags & 64) != 0) {
                allowTaskReparenting = true;
            }
            if (target.resultTo != null) {
                if (replyChainEnd < 0) {
                    replyChainEnd = i;
                    taskId = taskId2;
                    taskAffinity = taskAffinity2;
                } else {
                    taskId = taskId2;
                    taskAffinity = taskAffinity2;
                }
            } else if (!topTaskIsHigher || !allowTaskReparenting || taskAffinity2 == null) {
                taskId = taskId2;
                taskAffinity = taskAffinity2;
            } else if (taskAffinity2.equals(target.taskAffinity)) {
                if (forceReset) {
                    taskId = taskId2;
                    taskAffinity = taskAffinity2;
                } else if (finishOnTaskLaunch) {
                    taskId = taskId2;
                    taskAffinity = taskAffinity2;
                } else {
                    if (taskInsertionPoint3 < 0) {
                        taskInsertionPoint3 = task.mActivities.size();
                    }
                    int start = replyChainEnd >= 0 ? replyChainEnd : i;
                    if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                        String str = TAG_TASKS;
                        taskId = taskId2;
                        StringBuilder sb = new StringBuilder();
                        taskAffinity = taskAffinity2;
                        sb.append("Reparenting from task=");
                        sb.append(taskRecord);
                        sb.append(":");
                        sb.append(start);
                        sb.append("-");
                        sb.append(i);
                        sb.append(" to task=");
                        sb.append(task);
                        sb.append(":");
                        sb.append(taskInsertionPoint3);
                        Slog.v(str, sb.toString());
                    } else {
                        taskId = taskId2;
                        taskAffinity = taskAffinity2;
                    }
                    int srcPos = start;
                    while (srcPos >= i) {
                        ActivityRecord p = activities.get(srcPos);
                        p.reparent(task, taskInsertionPoint3, "resetAffinityTaskIfNeededLocked");
                        if (ActivityTaskManagerDebugConfig.DEBUG_ADD_REMOVE) {
                            String str2 = TAG_ADD_REMOVE;
                            StringBuilder sb2 = new StringBuilder();
                            taskInsertionPoint2 = taskInsertionPoint3;
                            sb2.append("Removing and adding activity ");
                            sb2.append(p);
                            sb2.append(" to stack at ");
                            sb2.append(task);
                            sb2.append(" callers=");
                            sb2.append(Debug.getCallers(3));
                            Slog.i(str2, sb2.toString());
                        } else {
                            taskInsertionPoint2 = taskInsertionPoint3;
                        }
                        if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                            Slog.v(TAG_TASKS, "Pulling activity " + p + " from " + srcPos + " in to resetting task " + task);
                        }
                        srcPos--;
                        taskInsertionPoint3 = taskInsertionPoint2;
                    }
                    positionChildWindowContainerAtTop(task);
                    if (target.info.launchMode == 1 && (targetNdx = (taskActivities = task.mActivities).indexOf(target)) > 0) {
                        ActivityRecord p2 = taskActivities.get(targetNdx - 1);
                        if (p2.intent.getComponent().equals(target.intent.getComponent())) {
                            finishActivityLocked(p2, 0, null, "replace", false);
                        }
                    }
                    taskInsertionPoint3 = taskInsertionPoint3;
                    replyChainEnd = -1;
                }
                int start2 = replyChainEnd >= 0 ? replyChainEnd : i;
                if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                    Slog.v(TAG_TASKS, "Finishing task at index " + start2 + " to " + i);
                }
                for (int srcPos2 = start2; srcPos2 >= i; srcPos2--) {
                    ActivityRecord p3 = activities.get(srcPos2);
                    if (!p3.finishing) {
                        finishActivityLocked(p3, 0, null, "move-affinity", false);
                    }
                }
                replyChainEnd = -1;
            } else {
                taskId = taskId2;
                taskAffinity = taskAffinity2;
            }
            i--;
            taskRecord = affinityTask;
            taskId2 = taskId;
            taskAffinity2 = taskAffinity;
        }
        return taskInsertionPoint3;
    }

    /* access modifiers changed from: package-private */
    public final ActivityRecord resetTaskIfNeededLocked(ActivityRecord taskTop, ActivityRecord newActivity) {
        boolean forceReset = (newActivity.info.flags & 4) != 0;
        TaskRecord task = taskTop.getTaskRecord();
        boolean taskFound = false;
        ActivityOptions topOptions = null;
        int reparentInsertionPoint = -1;
        for (int i = this.mTaskHistory.size() - 1; i >= 0; i--) {
            TaskRecord targetTask = this.mTaskHistory.get(i);
            if (!shouldResetTask(targetTask, task)) {
                if (targetTask == task) {
                    taskFound = true;
                    topOptions = resetTargetTaskIfNeededLocked(task, forceReset);
                } else {
                    reparentInsertionPoint = resetAffinityTaskIfNeededLocked(targetTask, task, taskFound, forceReset, reparentInsertionPoint);
                }
            }
        }
        int taskNdx = this.mTaskHistory.indexOf(task);
        if (taskNdx >= 0) {
            while (true) {
                int taskNdx2 = taskNdx - 1;
                taskTop = this.mTaskHistory.get(taskNdx).getTopActivity();
                if (taskTop == null && taskNdx2 >= 0) {
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

    /* access modifiers changed from: package-private */
    public void sendActivityResultLocked(int callingUid, ActivityRecord r, String resultWho, int requestCode, int resultCode, Intent data) {
        if (callingUid > 0) {
            this.mService.mUgmInternal.grantUriPermissionFromIntent(callingUid, r.packageName, data, r.getUriPermissionsLocked(), r.mUserId);
        }
        if (ActivityTaskManagerDebugConfig.DEBUG_RESULTS) {
            Slog.v(TAG, "Send activity result to " + r + " : who=" + resultWho + " req=" + requestCode + " res=" + resultCode + " data=" + data);
        }
        if (this.mResumedActivity == r && r.attachedToProcess()) {
            try {
                ArrayList<ResultInfo> list = new ArrayList<>();
                list.add(new ResultInfo(resultWho, requestCode, resultCode, data));
                this.mService.getLifecycleManager().scheduleTransaction(r.app.getThread(), (IBinder) r.appToken, (ClientTransactionItem) ActivityResultItem.obtain(list));
                return;
            } catch (Exception e) {
                Slog.w(TAG, "Exception thrown sending result to " + r, e);
            }
        }
        r.addResultLocked(null, resultWho, requestCode, resultCode, data);
    }

    private boolean isATopFinishingTask(TaskRecord task) {
        for (int i = this.mTaskHistory.size() - 1; i >= 0; i--) {
            TaskRecord current = this.mTaskHistory.get(i);
            if (current.topRunningActivityLocked() != null) {
                return false;
            }
            if (current == task) {
                return true;
            }
        }
        return false;
    }

    private void adjustFocusedActivityStack(ActivityRecord r, String reason) {
        if (ActivityTaskManagerDebugConfig.DEBUG_FOCUS) {
            Slog.v(TAG, "adjustFocusedActivityLocked: r= " + r + " mResumedActivity " + this.mResumedActivity + " reason " + reason);
        }
        if (this.mRootActivityContainer.isTopDisplayFocusedStack(this)) {
            ActivityRecord activityRecord = this.mResumedActivity;
            if (activityRecord == r || activityRecord == null) {
                ActivityRecord next = topRunningActivityLocked();
                String myReason = reason + " adjustFocus";
                if (ActivityTaskManagerDebugConfig.DEBUG_FOCUS) {
                    Slog.v(TAG, "next: = " + next);
                }
                if (next == r) {
                    ActivityRecord top = this.mRootActivityContainer.topRunningActivity();
                    if (top != null) {
                        top.moveFocusableActivityToTop(myReason);
                    }
                } else if (next != null && isFocusable()) {
                } else {
                    if (r.getTaskRecord() != null) {
                        ActivityStack nextFocusableStack = adjustFocusToNextFocusableStack(myReason);
                        if (nextFocusableStack != null) {
                            ActivityRecord top2 = nextFocusableStack.topRunningActivityLocked();
                            if (top2 != null && top2 == this.mRootActivityContainer.getTopResumedActivity()) {
                                this.mService.setResumedActivityUncheckLocked(top2, reason);
                                return;
                            }
                            return;
                        }
                        getDisplay().moveHomeActivityToTop(myReason);
                        return;
                    }
                    throw new IllegalStateException("activity no longer associated with task:" + r);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public ActivityStack adjustFocusToNextFocusableStack(String reason) {
        return adjustFocusToNextFocusableStack(reason, false);
    }

    private ActivityStack adjustFocusToNextFocusableStack(String reason, boolean allowFocusSelf) {
        ActivityRecord r;
        ActivityStack stack = this.mRootActivityContainer.getNextFocusableStack(this, !allowFocusSelf);
        String myReason = reason + " adjustFocusToNextFocusableStack";
        if (stack == null) {
            return null;
        }
        ActivityRecord top = stack.topRunningActivityLocked();
        if (!stack.isActivityTypeHome() || (top != null && top.visible)) {
            stack.moveToFront(myReason);
            if (inFreeformWindowingMode() && !stack.inFreeformWindowingMode() && (r = stack.topRunningActivityLocked()) != null) {
                stack.getDisplay().setFocusedApp(r, true);
                Slog.v(TAG, "oppo freeform setFocusedApp: " + r);
            }
            return stack;
        }
        stack.getDisplay().moveHomeActivityToTop(reason);
        return stack;
    }

    /* access modifiers changed from: package-private */
    public final void stopActivityLocked(ActivityRecord r) {
        if (ActivityTaskManagerDebugConfig.DEBUG_SWITCH) {
            String str = TAG_SWITCH;
            Slog.d(str, "Stopping: " + r);
        }
        if (!((r.intent.getFlags() & 1073741824) == 0 && (r.info.flags & 128) == 0) && !r.finishing) {
            if (!shouldSleepActivities()) {
                if (ActivityTaskManagerDebugConfig.DEBUG_STATES) {
                    String str2 = TAG_STATES;
                    Slog.d(str2, "no-history finish of " + r);
                }
                if (requestFinishActivityLocked(r.appToken, 0, null, "stop-no-history", false)) {
                    r.resumeKeyDispatchingLocked();
                    return;
                }
            } else if (ActivityTaskManagerDebugConfig.DEBUG_STATES) {
                String str3 = TAG_STATES;
                Slog.d(str3, "Not finishing noHistory " + r + " on stop because we're just sleeping");
            }
        }
        if (r.attachedToProcess()) {
            adjustFocusedActivityStack(r, "stopActivity");
            r.resumeKeyDispatchingLocked();
            try {
                r.stopped = false;
                if (ActivityTaskManagerDebugConfig.DEBUG_STATES) {
                    String str4 = TAG_STATES;
                    Slog.v(str4, "Moving to STOPPING: " + r + " (stop requested)");
                }
                r.setState(ActivityState.STOPPING, "stopActivityLocked");
                if (ActivityTaskManagerDebugConfig.DEBUG_VISIBILITY) {
                    String str5 = TAG_VISIBILITY;
                    Slog.v(str5, "Stopping visible=" + r.visible + " for " + r);
                }
                if (!r.visible) {
                    r.setVisible(false);
                }
                EventLogTags.writeAmStopActivity(r.mUserId, System.identityHashCode(r), r.shortComponentName);
                this.mService.getLifecycleManager().scheduleTransaction(r.app.getThread(), (IBinder) r.appToken, (ActivityLifecycleItem) StopActivityItem.obtain(r.visible, r.configChangeFlags));
                if (shouldSleepOrShutDownActivities()) {
                    r.setSleeping(true);
                }
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(104, r), 11000);
            } catch (Exception e) {
                Slog.w(TAG, "Exception thrown during pause", e);
                r.stopped = true;
                if (ActivityTaskManagerDebugConfig.DEBUG_STATES) {
                    String str6 = TAG_STATES;
                    Slog.v(str6, "Stop failed; moving to STOPPED: " + r);
                }
                r.setState(ActivityState.STOPPED, "stopActivityLocked");
                if (r.deferRelaunchUntilPaused) {
                    destroyActivityLocked(r, true, "stop-except");
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public final boolean requestFinishActivityLocked(IBinder token, int resultCode, Intent resultData, String reason, boolean oomAdj) {
        ActivityRecord r = isInStackLocked(token);
        if (ActivityTaskManagerDebugConfig.DEBUG_RESULTS || ActivityTaskManagerDebugConfig.DEBUG_STATES) {
            String str = TAG_STATES;
            Slog.v(str, "Finishing activity token=" + token + " r=, result=" + resultCode + ", data=" + resultData + ", reason=" + reason);
        }
        if (r == null) {
            return false;
        }
        finishActivityLocked(r, resultCode, resultData, reason, oomAdj);
        return true;
    }

    /* access modifiers changed from: package-private */
    public final void finishSubActivityLocked(ActivityRecord self, String resultWho, int requestCode) {
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ArrayList<ActivityRecord> activities = this.mTaskHistory.get(taskNdx).mActivities;
            for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                ActivityRecord r = activities.get(activityNdx);
                if (r.resultTo == self && r.requestCode == requestCode && ((r.resultWho == null && resultWho == null) || (r.resultWho != null && r.resultWho.equals(resultWho)))) {
                    finishActivityLocked(r, 0, null, "request-sub", false);
                }
            }
        }
        this.mService.updateOomAdj();
    }

    /* access modifiers changed from: package-private */
    public final TaskRecord finishTopCrashedActivityLocked(WindowProcessController app, String reason) {
        ActivityRecord r = topRunningActivityLocked();
        if (r == null) {
            return null;
        }
        if (r.app != app) {
            return null;
        }
        Slog.w(TAG, "  Force finishing activity " + r.intent.getComponent().flattenToShortString());
        TaskRecord finishedTask = r.getTaskRecord();
        int taskNdx = this.mTaskHistory.indexOf(finishedTask);
        int activityNdx = finishedTask.mActivities.indexOf(r);
        getDisplay().mDisplayContent.prepareAppTransition(26, false);
        finishActivityLocked(r, 0, null, reason, false);
        int activityNdx2 = activityNdx - 1;
        if (activityNdx2 < 0) {
            do {
                taskNdx--;
                if (taskNdx < 0) {
                    break;
                }
                activityNdx2 = this.mTaskHistory.get(taskNdx).mActivities.size() - 1;
            } while (activityNdx2 < 0);
        }
        if (activityNdx2 >= 0 && taskNdx < this.mTaskHistory.size() && activityNdx2 < this.mTaskHistory.get(taskNdx).mActivities.size()) {
            ActivityRecord r2 = this.mTaskHistory.get(taskNdx).mActivities.get(activityNdx2);
            if (r2.isState(ActivityState.RESUMED, ActivityState.PAUSING, ActivityState.PAUSED) && (!r2.isActivityTypeHome() || this.mService.mHomeProcess != r2.app)) {
                Slog.w(TAG, "  Force finishing activity " + r2.intent.getComponent().flattenToShortString());
                finishActivityLocked(r2, 0, null, reason, false);
            }
        }
        return finishedTask;
    }

    /* access modifiers changed from: package-private */
    public final void finishVoiceTask(IVoiceInteractionSession session) {
        IBinder sessionBinder = session.asBinder();
        boolean didOne = false;
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            TaskRecord tr = this.mTaskHistory.get(taskNdx);
            if (tr.voiceSession == null || tr.voiceSession.asBinder() != sessionBinder) {
                int activityNdx = tr.mActivities.size() - 1;
                while (true) {
                    if (activityNdx < 0) {
                        break;
                    }
                    ActivityRecord r = tr.mActivities.get(activityNdx);
                    if (r.voiceSession != null && r.voiceSession.asBinder() == sessionBinder) {
                        r.clearVoiceSessionLocked();
                        try {
                            r.app.getThread().scheduleLocalVoiceInteractionStarted(r.appToken, (IVoiceInteractor) null);
                        } catch (RemoteException e) {
                        }
                        this.mService.finishRunningVoiceLocked();
                        break;
                    }
                    activityNdx--;
                }
            } else {
                for (int activityNdx2 = tr.mActivities.size() - 1; activityNdx2 >= 0; activityNdx2--) {
                    ActivityRecord r2 = tr.mActivities.get(activityNdx2);
                    if (!r2.finishing) {
                        finishActivityLocked(r2, 0, null, "finish-voice", false);
                        didOne = true;
                    }
                }
            }
        }
        if (didOne) {
            this.mService.updateOomAdj();
        }
    }

    /* access modifiers changed from: package-private */
    public final boolean finishActivityAffinityLocked(ActivityRecord r) {
        ArrayList<ActivityRecord> activities = r.getTaskRecord().mActivities;
        for (int index = activities.indexOf(r); index >= 0; index--) {
            ActivityRecord cur = activities.get(index);
            if (!Objects.equals(cur.taskAffinity, r.taskAffinity)) {
                return true;
            }
            finishActivityLocked(cur, 0, null, "request-affinity", true);
        }
        return true;
    }

    private void finishActivityResultsLocked(ActivityRecord r, int resultCode, Intent resultData) {
        ActivityRecord resultTo = r.resultTo;
        if (resultTo != null) {
            if (ActivityTaskManagerDebugConfig.DEBUG_RESULTS) {
                String str = TAG_RESULTS;
                Slog.v(str, "Adding result to " + resultTo + " who=" + r.resultWho + " req=" + r.requestCode + " res=" + resultCode + " data=" + resultData);
            }
            if (!(resultTo.mUserId == r.mUserId || resultData == null || OppoFeatureCache.get(IColorMultiAppManager.DEFAULT).shouldSkipLeaveUser(resultData, r.mColorArEx))) {
                resultData.prepareToLeaveUser(r.mUserId);
            }
            if (r.info.applicationInfo.uid > 0) {
                this.mService.mUgmInternal.grantUriPermissionFromIntent(r.info.applicationInfo.uid, resultTo.packageName, resultData, resultTo.getUriPermissionsLocked(), resultTo.mUserId);
            }
            resultTo.addResultLocked(r, r.resultWho, r.requestCode, resultCode, resultData);
            r.resultTo = null;
        } else if (ActivityTaskManagerDebugConfig.DEBUG_RESULTS) {
            String str2 = TAG_RESULTS;
            Slog.v(str2, "No result destination from " + r);
        }
        r.results = null;
        r.pendingResults = null;
        r.newIntents = null;
        r.icicle = null;
    }

    /* access modifiers changed from: package-private */
    public final boolean finishActivityLocked(ActivityRecord r, int resultCode, Intent resultData, String reason, boolean oomAdj) {
        String componentName = "";
        if (r != null) {
            componentName = r.shortComponentName;
        }
        if (!(r == null || r.intent == null || ((componentName == null || componentName.endsWith("ChooserActivity")) && (r.intent.getFlags() & 1024) == 0))) {
            this.mStackSupervisor.mOppoSecureProtectUtils.handleFinishActivityLocked(this);
        }
        return finishActivityLocked(r, resultCode, resultData, reason, oomAdj, false);
    }

    /* access modifiers changed from: package-private */
    public final boolean finishActivityLocked(ActivityRecord r, int resultCode, Intent resultData, String reason, boolean oomAdj, boolean pauseImmediately) {
        Throwable th;
        ActivityRecord nextResumedActivity;
        boolean removedActivity = false;
        if (r.finishing) {
            Slog.w(TAG, "Duplicate finish request for " + r);
            return false;
        }
        this.mWindowManager.deferSurfaceLayout();
        try {
            r.makeFinishingLocked();
            TaskRecord task = r.getTaskRecord();
            int finishMode = 2;
            EventLog.writeEvent((int) EventLogTags.AM_FINISH_ACTIVITY, Integer.valueOf(r.mUserId), Integer.valueOf(System.identityHashCode(r)), Integer.valueOf(task.taskId), r.shortComponentName, reason);
            ArrayList<ActivityRecord> activities = task.mActivities;
            int index = activities.indexOf(r);
            if (index < activities.size() - 1) {
                task.setFrontOfTask();
                if ((r.intent.getFlags() & DumpState.DUMP_FROZEN) != 0) {
                    activities.get(index + 1).intent.addFlags(DumpState.DUMP_FROZEN);
                }
            }
            r.pauseKeyDispatchingLocked();
            adjustFocusedActivityStack(r, "finishActivity");
            finishActivityResultsLocked(r, resultCode, resultData);
            boolean endTask = index <= 0 && !task.isClearingToReuseTask();
            int transit = endTask ? 9 : 7;
            if (this.mResumedActivity == r) {
                if (ActivityTaskManagerDebugConfig.DEBUG_VISIBILITY || ActivityTaskManagerDebugConfig.DEBUG_TRANSITION) {
                    Slog.v(TAG_TRANSITION, "Prepare close transition: finishing " + r);
                }
                if (endTask) {
                    this.mService.getTaskChangeNotificationController().notifyTaskRemovalStarted(task.getTaskInfo());
                }
                getDisplay().mDisplayContent.prepareAppTransition(transit, false);
                OppoFeatureCache.get(IColorZoomWindowManager.DEFAULT).prepareZoomTransition(this, this.mService.mRootActivityContainer.getTopDisplayFocusedStack());
                r.setVisibility(false);
                if (this.mPausingActivity == null) {
                    if (ActivityTaskManagerDebugConfig.DEBUG_PAUSE) {
                        Slog.v(TAG_PAUSE, "Finish needs to pause: " + r);
                    }
                    if (ActivityTaskManagerDebugConfig.DEBUG_USER_LEAVING) {
                        Slog.v(TAG_USER_LEAVING, "finish() => pause with userLeaving=false");
                    }
                    startPausingLocked(false, false, null, pauseImmediately, "finish-request");
                    if (!(getDisplay() == null || getDisplay().getFocusedStack() == null || (nextResumedActivity = getDisplay().getFocusedStack().topRunningActivityLocked()) == null)) {
                        this.mService.mAmsExt.onBeforeActivitySwitch(r, nextResumedActivity, true, nextResumedActivity.getActivityType());
                    }
                }
                if (endTask) {
                    this.mService.getLockTaskController().clearLockedTask(task);
                }
            } else if (!r.isState(ActivityState.PAUSING)) {
                if (ActivityTaskManagerDebugConfig.DEBUG_PAUSE) {
                    Slog.v(TAG_PAUSE, "Finish not pausing: " + r);
                }
                if (r.visible) {
                    prepareActivityHideTransitionAnimation(r, transit);
                }
                if (!r.visible) {
                    if (!r.nowVisible) {
                        finishMode = 1;
                    }
                }
                try {
                    if (finishCurrentActivityLocked(r, finishMode, oomAdj, "finishActivityLocked") == null) {
                        removedActivity = true;
                    }
                    if (task.onlyHasTaskOverlayActivities(true)) {
                        Iterator<ActivityRecord> it = task.mActivities.iterator();
                        while (it.hasNext()) {
                            ActivityRecord taskOverlay = it.next();
                            if (taskOverlay.mTaskOverlay) {
                                prepareActivityHideTransitionAnimation(taskOverlay, transit);
                            }
                        }
                    }
                    this.mWindowManager.continueSurfaceLayout();
                    return removedActivity;
                } catch (Throwable th2) {
                    th = th2;
                    this.mWindowManager.continueSurfaceLayout();
                    throw th;
                }
            } else if (ActivityTaskManagerDebugConfig.DEBUG_PAUSE) {
                Slog.v(TAG_PAUSE, "Finish waiting for pause of: " + r);
            }
            this.mWindowManager.continueSurfaceLayout();
            return false;
        } catch (Throwable th3) {
            th = th3;
            this.mWindowManager.continueSurfaceLayout();
            throw th;
        }
    }

    private void prepareActivityHideTransitionAnimation(ActivityRecord r, int transit) {
        DisplayContent dc = getDisplay().mDisplayContent;
        dc.prepareAppTransition(transit, false);
        r.setVisibility(false);
        dc.executeAppTransition();
    }

    /* access modifiers changed from: package-private */
    public final ActivityRecord finishCurrentActivityLocked(ActivityRecord r, int mode, boolean oomAdj, String reason) {
        ActivityDisplay display = getDisplay();
        ActivityRecord next = display.topRunningActivity(true);
        boolean isFloating = r.getConfiguration().windowConfiguration.tasksAreFloating();
        if (mode != 2 || ((!r.visible && !r.nowVisible) || next == null || next.nowVisible || isFloating)) {
            this.mStackSupervisor.mStoppingActivities.remove(r);
            this.mStackSupervisor.mGoingToSleepActivities.remove(r);
            ActivityState prevState = r.getState();
            if (ActivityTaskManagerDebugConfig.DEBUG_STATES) {
                String str = TAG_STATES;
                Slog.v(str, "Moving to FINISHING: " + r);
            }
            r.setState(ActivityState.FINISHING, "finishCurrentActivityLocked");
            boolean finishingInNonFocusedStackOrNoRunning = mode == 2 && prevState == ActivityState.PAUSED && ((r.getActivityStack() != display.getFocusedStack()) || (next == null && display.topRunningActivity() == null && display.getHomeStack() == null));
            if (mode == 0 || ((prevState == ActivityState.PAUSED && (mode == 1 || inPinnedWindowingMode())) || finishingInNonFocusedStackOrNoRunning || prevState == ActivityState.STOPPING || prevState == ActivityState.STOPPED || prevState == ActivityState.INITIALIZING)) {
                r.makeFinishingLocked();
                boolean activityRemoved = destroyActivityLocked(r, true, "finish-imm:" + reason);
                if (finishingInNonFocusedStackOrNoRunning) {
                    this.mRootActivityContainer.ensureVisibilityAndConfig(next, this.mDisplayId, false, true);
                }
                if (activityRemoved) {
                    this.mRootActivityContainer.resumeFocusedStacksTopActivities();
                }
                if (ActivityTaskManagerDebugConfig.DEBUG_CONTAINERS) {
                    String str2 = TAG_CONTAINERS;
                    Slog.d(str2, "destroyActivityLocked: finishCurrentActivityLocked r=" + r + " destroy returned removed=" + activityRemoved);
                }
                if (activityRemoved) {
                    return null;
                }
                return r;
            }
            if (ActivityTaskManagerDebugConfig.DEBUG_ALL) {
                Slog.v(TAG, "Enqueueing pending finish: " + r);
            }
            this.mStackSupervisor.mFinishingActivities.add(r);
            r.resumeKeyDispatchingLocked();
            this.mRootActivityContainer.resumeFocusedStacksTopActivities();
            if (r.isState(ActivityState.RESUMED) && this.mPausingActivity != null) {
                startPausingLocked(false, false, next, false);
            }
            return r;
        }
        if (!this.mStackSupervisor.mStoppingActivities.contains(r)) {
            addToStopping(r, false, false, "finishCurrentActivityLocked");
        }
        if (ActivityTaskManagerDebugConfig.DEBUG_STATES) {
            String str3 = TAG_STATES;
            Slog.v(str3, "Moving to STOPPING: " + r + " (finish requested)");
        }
        r.setState(ActivityState.STOPPING, "finishCurrentActivityLocked");
        if (oomAdj) {
            this.mService.updateOomAdj();
        }
        return r;
    }

    /* access modifiers changed from: package-private */
    public void finishAllActivitiesLocked(boolean immediately) {
        boolean noActivitiesInStack = true;
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ArrayList<ActivityRecord> activities = this.mTaskHistory.get(taskNdx).mActivities;
            for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                ActivityRecord r = activities.get(activityNdx);
                noActivitiesInStack = false;
                if (!r.finishing || immediately) {
                    Slog.d(TAG, "finishAllActivitiesLocked: finishing " + r + " immediately");
                    finishCurrentActivityLocked(r, 0, false, "finishAllActivitiesLocked");
                }
            }
        }
        if (noActivitiesInStack) {
            remove();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean inFrontOfStandardStack() {
        int index;
        ActivityDisplay display = getDisplay();
        if (display == null || (index = display.getIndexOf(this)) == 0) {
            return false;
        }
        return display.getChildAt(index - 1).isActivityTypeStandard();
    }

    /* access modifiers changed from: package-private */
    public boolean shouldUpRecreateTaskLocked(ActivityRecord srec, String destAffinity) {
        if (srec == null || srec.getTaskRecord().affinity == null || !srec.getTaskRecord().affinity.equals(destAffinity)) {
            return true;
        }
        TaskRecord task = srec.getTaskRecord();
        if (srec.frontOfTask && task.getBaseIntent() != null && task.getBaseIntent().isDocument()) {
            if (!inFrontOfStandardStack()) {
                return true;
            }
            int taskIdx = this.mTaskHistory.indexOf(task);
            if (taskIdx <= 0) {
                Slog.w(TAG, "shouldUpRecreateTask: task not in history for " + srec);
                return false;
            } else if (!task.affinity.equals(this.mTaskHistory.get(taskIdx).affinity)) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x0093 A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x009f A[LOOP:1: B:35:0x009d->B:36:0x009f, LOOP_END] */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x00e9  */
    public final boolean navigateUpToLocked(ActivityRecord srec, Intent destIntent, int resultCode, Intent resultData) {
        boolean foundParentInTask;
        ActivityRecord parent;
        int finishTo;
        IActivityController controller;
        int i;
        int parentLaunchMode;
        int callingUid;
        ActivityRecord next;
        boolean resumeOK;
        if (!srec.attachedToProcess()) {
            return false;
        }
        TaskRecord task = srec.getTaskRecord();
        ArrayList<ActivityRecord> activities = task.mActivities;
        int start = activities.indexOf(srec);
        if (!this.mTaskHistory.contains(task) || start < 0) {
            return false;
        }
        int finishTo2 = start - 1;
        ActivityRecord parent2 = finishTo2 < 0 ? null : activities.get(finishTo2);
        ComponentName dest = destIntent.getComponent();
        if (start > 0 && dest != null) {
            int i2 = finishTo2;
            while (true) {
                if (i2 < 0) {
                    break;
                }
                ActivityRecord r = activities.get(i2);
                if (r.info.packageName.equals(dest.getPackageName()) && r.info.name.equals(dest.getClassName())) {
                    finishTo = i2;
                    parent = r;
                    foundParentInTask = true;
                    break;
                }
                i2--;
            }
            controller = this.mService.mController;
            if (!(controller == null || (next = topRunningActivityLocked(srec.appToken, 0)) == null)) {
                resumeOK = true;
                resumeOK = controller.activityResuming(next.packageName);
                if (!resumeOK) {
                    return false;
                }
            }
            long origId = Binder.clearCallingIdentity();
            i = start;
            int resultCode2 = resultCode;
            Intent resultData2 = resultData;
            while (i > finishTo) {
                requestFinishActivityLocked(activities.get(i).appToken, resultCode2, resultData2, "navigate-up", true);
                resultCode2 = 0;
                resultData2 = null;
                i--;
                finishTo = finishTo;
                controller = controller;
            }
            if (parent != null && foundParentInTask) {
                int callingUid2 = srec.info.applicationInfo.uid;
                parentLaunchMode = parent.info.launchMode;
                int destIntentFlags = destIntent.getFlags();
                if (!(parentLaunchMode == 3 || parentLaunchMode == 2)) {
                    boolean foundParentInTask2 = true;
                    if (parentLaunchMode != 1) {
                        if ((destIntentFlags & 67108864) != 0) {
                            callingUid = callingUid2;
                            parent.deliverNewIntentLocked(callingUid, destIntent, srec.packageName);
                        } else {
                            try {
                                if (this.mService.getActivityStartController().obtainStarter(destIntent, "navigateUpTo").setCaller(srec.app.getThread()).setActivityInfo(AppGlobals.getPackageManager().getActivityInfo(destIntent.getComponent(), 1024, srec.mUserId)).setResultTo(parent.appToken).setCallingPid(-1).setCallingUid(callingUid2).setCallingPackage(srec.packageName).setRealCallingPid(-1).setRealCallingUid(callingUid2).setComponentSpecified(true).execute() != 0) {
                                    foundParentInTask2 = false;
                                }
                                foundParentInTask = foundParentInTask2;
                            } catch (RemoteException e) {
                                foundParentInTask = false;
                            }
                            requestFinishActivityLocked(parent.appToken, resultCode2, resultData2, "navigate-top", true);
                        }
                    }
                }
                callingUid = callingUid2;
                parent.deliverNewIntentLocked(callingUid, destIntent, srec.packageName);
            }
            Binder.restoreCallingIdentity(origId);
            return foundParentInTask;
        }
        finishTo = finishTo2;
        parent = parent2;
        foundParentInTask = false;
        controller = this.mService.mController;
        resumeOK = true;
        try {
            resumeOK = controller.activityResuming(next.packageName);
        } catch (RemoteException e2) {
            this.mService.mController = null;
            Watchdog.getInstance().setActivityController(null);
        }
        if (!resumeOK) {
        }
        long origId2 = Binder.clearCallingIdentity();
        i = start;
        int resultCode22 = resultCode;
        Intent resultData22 = resultData;
        while (i > finishTo) {
        }
        int callingUid22 = srec.info.applicationInfo.uid;
        parentLaunchMode = parent.info.launchMode;
        int destIntentFlags2 = destIntent.getFlags();
        boolean foundParentInTask22 = true;
        if (parentLaunchMode != 1) {
        }
        callingUid = callingUid22;
        parent.deliverNewIntentLocked(callingUid, destIntent, srec.packageName);
        Binder.restoreCallingIdentity(origId2);
        return foundParentInTask;
    }

    /* access modifiers changed from: package-private */
    public void onActivityRemovedFromStack(ActivityRecord r) {
        removeTimeoutsForActivityLocked(r);
        ActivityRecord activityRecord = this.mResumedActivity;
        if (activityRecord != null && activityRecord == r) {
            setResumedActivity(null, "onActivityRemovedFromStack");
        }
        ActivityRecord activityRecord2 = this.mPausingActivity;
        if (activityRecord2 != null && activityRecord2 == r) {
            this.mPausingActivity = null;
        }
    }

    /* access modifiers changed from: package-private */
    public void onActivityAddedToStack(ActivityRecord r) {
        if (r.getState() == ActivityState.RESUMED) {
            setResumedActivity(r, "onActivityAddedToStack");
        }
    }

    private void cleanUpActivityLocked(ActivityRecord r, boolean cleanServices, boolean setState) {
        onActivityRemovedFromStack(r);
        r.deferRelaunchUntilPaused = false;
        r.frozenBeforeDestroy = false;
        if (setState) {
            if (ActivityTaskManagerDebugConfig.DEBUG_STATES) {
                String str = TAG_STATES;
                Slog.v(str, "Moving to DESTROYED: " + r + " (cleaning up)");
            }
            r.setState(ActivityState.DESTROYED, "cleanupActivityLocked");
            if (ActivityTaskManagerDebugConfig.DEBUG_APP) {
                String str2 = TAG_APP;
                Slog.v(str2, "Clearing app during cleanUp for activity " + r);
            }
            r.app = null;
        }
        this.mStackSupervisor.cleanupActivity(r);
        if (r.finishing && r.pendingResults != null) {
            Iterator<WeakReference<PendingIntentRecord>> it = r.pendingResults.iterator();
            while (it.hasNext()) {
                PendingIntentRecord rec = it.next().get();
                if (rec != null) {
                    this.mService.mPendingIntentController.cancelIntentSender(rec, false);
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

    private void removeTimeoutsForActivityLocked(ActivityRecord r) {
        this.mStackSupervisor.removeTimeoutsForActivityLocked(r);
        this.mHandler.removeMessages(101, r);
        this.mHandler.removeMessages(104, r);
        this.mHandler.removeMessages(102, r);
        r.finishLaunchTickingLocked();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void removeActivityFromHistoryLocked(ActivityRecord r, String reason) {
        finishActivityResultsLocked(r, 0, null);
        r.makeFinishingLocked();
        if (ActivityTaskManagerDebugConfig.DEBUG_ADD_REMOVE) {
            String str = TAG_ADD_REMOVE;
            Slog.i(str, "Removing activity " + r + " from stack callers=" + Debug.getCallers(5));
        }
        r.takeFromHistory();
        removeTimeoutsForActivityLocked(r);
        if (ActivityTaskManagerDebugConfig.DEBUG_STATES) {
            String str2 = TAG_STATES;
            Slog.v(str2, "Moving to DESTROYED: " + r + " (removed from history)");
        }
        r.setState(ActivityState.DESTROYED, "removeActivityFromHistoryLocked");
        if (ActivityTaskManagerDebugConfig.DEBUG_APP) {
            String str3 = TAG_APP;
            Slog.v(str3, "Clearing app during remove for activity " + r);
        }
        r.app = null;
        r.removeWindowContainer();
        TaskRecord task = r.getTaskRecord();
        boolean lastActivity = task != null ? task.removeActivity(r) : false;
        boolean onlyHasTaskOverlays = task != null ? task.onlyHasTaskOverlayActivities(false) : false;
        if (lastActivity || onlyHasTaskOverlays) {
            if (ActivityTaskManagerDebugConfig.DEBUG_STACK) {
                String str4 = TAG_STACK;
                Slog.i(str4, "removeActivityFromHistoryLocked: last activity removed from " + this + " onlyHasTaskOverlays=" + onlyHasTaskOverlays);
            }
            if (onlyHasTaskOverlays) {
                this.mStackSupervisor.removeTaskByIdLocked(task.taskId, false, false, true, reason);
            }
            if (lastActivity) {
                removeTask(task, reason, 0);
            }
        }
        cleanUpActivityServicesLocked(r);
        r.removeUriPermissionsLocked();
    }

    private void cleanUpActivityServicesLocked(ActivityRecord r) {
        if (r.mServiceConnectionsHolder != null) {
            r.mServiceConnectionsHolder.disconnectActivityFromServices();
        }
    }

    /* access modifiers changed from: package-private */
    public final void scheduleDestroyActivities(WindowProcessController owner, String reason) {
        Message msg = this.mHandler.obtainMessage(105);
        msg.obj = new ScheduleDestroyArgs(owner, reason);
        this.mHandler.sendMessage(msg);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void destroyActivitiesLocked(WindowProcessController owner, String reason) {
        boolean lastIsOpaque = false;
        boolean activityRemoved = false;
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ArrayList<ActivityRecord> activities = this.mTaskHistory.get(taskNdx).mActivities;
            for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                ActivityRecord r = activities.get(activityNdx);
                if (!r.finishing) {
                    if (r.fullscreen) {
                        lastIsOpaque = true;
                    }
                    if ((owner == null || r.app == owner) && lastIsOpaque && r.isDestroyable()) {
                        if (ActivityTaskManagerDebugConfig.DEBUG_SWITCH) {
                            Slog.v(TAG_SWITCH, "Destroying " + r + " in state " + r.getState() + " resumed=" + this.mResumedActivity + " pausing=" + this.mPausingActivity + " for reason " + reason);
                        }
                        if (destroyActivityLocked(r, true, reason)) {
                            activityRemoved = true;
                        }
                    }
                }
            }
        }
        if (activityRemoved) {
            this.mRootActivityContainer.resumeFocusedStacksTopActivities();
        }
    }

    /* access modifiers changed from: package-private */
    public final boolean safelyDestroyActivityLocked(ActivityRecord r, String reason) {
        if (!r.isDestroyable()) {
            return false;
        }
        if (ActivityTaskManagerDebugConfig.DEBUG_SWITCH) {
            String str = TAG_SWITCH;
            Slog.v(str, "Destroying " + r + " in state " + r.getState() + " resumed=" + this.mResumedActivity + " pausing=" + this.mPausingActivity + " for reason " + reason);
        }
        return destroyActivityLocked(r, true, reason);
    }

    /* access modifiers changed from: package-private */
    public final int releaseSomeActivitiesLocked(WindowProcessController app, ArraySet<TaskRecord> tasks, String reason) {
        if (ActivityTaskManagerDebugConfig.DEBUG_RELEASE) {
            Slog.d(TAG_RELEASE, "Trying to release some activities in " + app);
        }
        int maxTasks = tasks.size() / 4;
        if (maxTasks < 1) {
            maxTasks = 1;
        }
        int numReleased = 0;
        int taskNdx = 0;
        while (taskNdx < this.mTaskHistory.size() && maxTasks > 0) {
            TaskRecord task = this.mTaskHistory.get(taskNdx);
            if (tasks.contains(task)) {
                if (ActivityTaskManagerDebugConfig.DEBUG_RELEASE) {
                    Slog.d(TAG_RELEASE, "Looking for activities to release in " + task);
                }
                int curNum = 0;
                ArrayList<ActivityRecord> activities = task.mActivities;
                int actNdx = 0;
                while (actNdx < activities.size()) {
                    ActivityRecord activity = activities.get(actNdx);
                    if (activity.app == app && activity.isDestroyable()) {
                        if (ActivityTaskManagerDebugConfig.DEBUG_RELEASE) {
                            Slog.v(TAG_RELEASE, "Destroying " + activity + " in state " + activity.getState() + " resumed=" + this.mResumedActivity + " pausing=" + this.mPausingActivity + " for reason " + reason);
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
        if (ActivityTaskManagerDebugConfig.DEBUG_RELEASE) {
            Slog.d(TAG_RELEASE, "Done releasing: did " + numReleased + " activities");
        }
        return numReleased;
    }

    /* access modifiers changed from: package-private */
    public final boolean destroyActivityLocked(ActivityRecord r, boolean removeFromApp, String reason) {
        if (ActivityTaskManagerDebugConfig.DEBUG_SWITCH || ActivityTaskManagerDebugConfig.DEBUG_CLEANUP) {
            String str = TAG_SWITCH;
            StringBuilder sb = new StringBuilder();
            sb.append("Removing activity from ");
            sb.append(reason);
            sb.append(": token=");
            sb.append(r);
            sb.append(", app=");
            sb.append(r.hasProcess() ? r.app.mName : "(null)");
            Slog.v(str, sb.toString());
        }
        if (r.isState(ActivityState.DESTROYING, ActivityState.DESTROYED)) {
            if (ActivityTaskManagerDebugConfig.DEBUG_STATES) {
                String str2 = TAG_STATES;
                Slog.v(str2, "activity " + r + " already destroying.skipping request with reason:" + reason);
            }
            return false;
        }
        EventLog.writeEvent((int) EventLogTags.AM_DESTROY_ACTIVITY, Integer.valueOf(r.mUserId), Integer.valueOf(System.identityHashCode(r)), Integer.valueOf(r.getTaskRecord().taskId), r.shortComponentName, reason);
        boolean removedFromHistory = false;
        cleanUpActivityLocked(r, false, false);
        boolean hadApp = r.hasProcess();
        if (hadApp) {
            if (removeFromApp) {
                r.app.removeActivity(r);
                if (!r.app.hasActivities()) {
                    this.mService.clearHeavyWeightProcessIfEquals(r.app);
                }
                if (!r.app.hasActivities()) {
                    r.app.updateProcessInfo(true, false, true);
                }
            }
            boolean skipDestroy = false;
            try {
                if (ActivityTaskManagerDebugConfig.DEBUG_SWITCH) {
                    String str3 = TAG_SWITCH;
                    Slog.i(str3, "Destroying: " + r);
                }
                this.mService.getLifecycleManager().scheduleTransaction(r.app.getThread(), (IBinder) r.appToken, (ActivityLifecycleItem) DestroyActivityItem.obtain(r.finishing, r.configChangeFlags));
            } catch (Exception e) {
                if (r.finishing) {
                    removeActivityFromHistoryLocked(r, reason + " exceptionInScheduleDestroy");
                    removedFromHistory = true;
                    skipDestroy = true;
                }
            }
            r.nowVisible = false;
            if (!r.finishing || skipDestroy) {
                if (ActivityTaskManagerDebugConfig.DEBUG_STATES) {
                    String str4 = TAG_STATES;
                    Slog.v(str4, "Moving to DESTROYED: " + r + " (destroy skipped)");
                }
                r.setState(ActivityState.DESTROYED, "destroyActivityLocked. not finishing or skipping destroy");
                if (ActivityTaskManagerDebugConfig.DEBUG_APP) {
                    String str5 = TAG_APP;
                    Slog.v(str5, "Clearing app during destroy for activity " + r);
                }
                r.app = null;
            } else {
                if (ActivityTaskManagerDebugConfig.DEBUG_STATES) {
                    String str6 = TAG_STATES;
                    Slog.v(str6, "Moving to DESTROYING: " + r + " (destroy requested)");
                }
                r.setState(ActivityState.DESTROYING, "destroyActivityLocked. finishing and not skipping destroy");
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(102, r), 10000);
            }
        } else if (r.finishing) {
            removeActivityFromHistoryLocked(r, reason + " hadNoApp");
            removedFromHistory = true;
        } else {
            if (ActivityTaskManagerDebugConfig.DEBUG_STATES) {
                String str7 = TAG_STATES;
                Slog.v(str7, "Moving to DESTROYED: " + r + " (no app)");
            }
            r.setState(ActivityState.DESTROYED, "destroyActivityLocked. not finishing and had no app");
            if (ActivityTaskManagerDebugConfig.DEBUG_APP) {
                String str8 = TAG_APP;
                Slog.v(str8, "Clearing app during destroy for activity " + r);
            }
            r.app = null;
        }
        r.configChangeFlags = 0;
        if (!this.mLRUActivities.remove(r) && hadApp) {
            Slog.w(TAG, "Activity " + r + " being finished, but not in LRU list");
        }
        return removedFromHistory;
    }

    /* access modifiers changed from: package-private */
    public final void activityDestroyedLocked(IBinder token, String reason) {
        long origId = Binder.clearCallingIdentity();
        try {
            activityDestroyedLocked(ActivityRecord.forTokenLocked(token), reason);
        } finally {
            Binder.restoreCallingIdentity(origId);
        }
    }

    /* access modifiers changed from: package-private */
    public final void activityDestroyedLocked(ActivityRecord record, String reason) {
        if (record != null) {
            this.mHandler.removeMessages(102, record);
        }
        if (ActivityTaskManagerDebugConfig.DEBUG_CONTAINERS) {
            String str = TAG_CONTAINERS;
            Slog.d(str, "activityDestroyedLocked: r=" + record);
        }
        if (isInStackLocked(record) != null && record.isState(ActivityState.DESTROYING, ActivityState.DESTROYED)) {
            cleanUpActivityLocked(record, true, false);
            removeActivityFromHistoryLocked(record, reason);
        }
        this.mRootActivityContainer.resumeFocusedStacksTopActivities();
    }

    private void removeHistoryRecordsForAppLocked(ArrayList<ActivityRecord> list, WindowProcessController app, String listName) {
        int i = list.size();
        if (ActivityTaskManagerDebugConfig.DEBUG_CLEANUP) {
            Slog.v(TAG_CLEANUP, "Removing app " + app + " from list " + listName + " with " + i + " entries");
        }
        while (i > 0) {
            i--;
            ActivityRecord r = list.get(i);
            if (ActivityTaskManagerDebugConfig.DEBUG_CLEANUP) {
                Slog.v(TAG_CLEANUP, "Record #" + i + StringUtils.SPACE + r);
            }
            if (r.app == app) {
                if (ActivityTaskManagerDebugConfig.DEBUG_CLEANUP) {
                    Slog.v(TAG_CLEANUP, "---> REMOVING this entry!");
                }
                list.remove(i);
                removeTimeoutsForActivityLocked(r);
            }
        }
    }

    private boolean removeHistoryRecordsForAppLocked(WindowProcessController app) {
        boolean remove;
        removeHistoryRecordsForAppLocked(this.mLRUActivities, app, "mLRUActivities");
        removeHistoryRecordsForAppLocked(this.mStackSupervisor.mStoppingActivities, app, "mStoppingActivities");
        removeHistoryRecordsForAppLocked(this.mStackSupervisor.mGoingToSleepActivities, app, "mGoingToSleepActivities");
        removeHistoryRecordsForAppLocked(this.mStackSupervisor.mFinishingActivities, app, "mFinishingActivities");
        boolean isProcessRemoved = app.isRemoved();
        if (isProcessRemoved) {
            app.makeFinishingForProcessRemoved();
        }
        boolean hasVisibleActivities = false;
        int i = numActivities();
        if (ActivityTaskManagerDebugConfig.DEBUG_CLEANUP) {
            Slog.v(TAG_CLEANUP, "Removing app " + app + " from history with " + i + " entries");
        }
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ArrayList<ActivityRecord> activities = this.mTaskHistory.get(taskNdx).mActivities;
            this.mTmpActivities.clear();
            this.mTmpActivities.addAll(activities);
            while (!this.mTmpActivities.isEmpty()) {
                int targetIndex = this.mTmpActivities.size() - 1;
                ActivityRecord r = this.mTmpActivities.remove(targetIndex);
                if (ActivityTaskManagerDebugConfig.DEBUG_CLEANUP) {
                    Slog.v(TAG_CLEANUP, "Record #" + targetIndex + StringUtils.SPACE + r + ": app=" + r.app);
                }
                if (r.app == app) {
                    if (r.visible) {
                        hasVisibleActivities = true;
                    }
                    if ((r.mRelaunchReason == 1 || r.mRelaunchReason == 2) && r.launchCount < 3 && !r.finishing) {
                        remove = false;
                    } else if (!(r.haveState || r.stateNotNeeded || r.isState(ActivityState.RESTARTING_PROCESS)) || r.finishing) {
                        remove = true;
                    } else if (r.visible || r.launchCount <= 2 || r.lastLaunchTime <= SystemClock.uptimeMillis() - 60000) {
                        remove = false;
                    } else {
                        remove = true;
                    }
                    if (remove) {
                        if (ActivityTaskManagerDebugConfig.DEBUG_ADD_REMOVE || ActivityTaskManagerDebugConfig.DEBUG_CLEANUP) {
                            Slog.i(TAG_ADD_REMOVE, "Removing activity " + r + " from stack at " + i + ": haveState=" + r.haveState + " stateNotNeeded=" + r.stateNotNeeded + " finishing=" + r.finishing + " state=" + r.getState() + " callers=" + Debug.getCallers(5));
                        }
                        if (!r.finishing || isProcessRemoved) {
                            Slog.w(TAG, "Force removing " + r + ": app died, no saved state");
                            EventLog.writeEvent((int) EventLogTags.AM_FINISH_ACTIVITY, Integer.valueOf(r.mUserId), Integer.valueOf(System.identityHashCode(r)), Integer.valueOf(r.getTaskRecord().taskId), r.shortComponentName, "proc died without state saved");
                        }
                    } else {
                        if (ActivityTaskManagerDebugConfig.DEBUG_ALL) {
                            Slog.v(TAG, "Keeping entry, setting app to null");
                        }
                        if (ActivityTaskManagerDebugConfig.DEBUG_APP) {
                            Slog.v(TAG_APP, "Clearing app during removeHistory for activity " + r);
                        }
                        r.app = null;
                        r.nowVisible = r.visible;
                        if (!r.haveState) {
                            if (ActivityTaskManagerDebugConfig.DEBUG_SAVED_STATE) {
                                Slog.i(TAG_SAVED_STATE, "App died, clearing saved state of " + r);
                            }
                            r.icicle = null;
                        }
                    }
                    cleanUpActivityLocked(r, true, true);
                    if (this.mService.getColorFreeformManager() != null) {
                        this.mService.getColorFreeformManager().handleFreeformDied(inFreeformWindowingMode(), r);
                    }
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
            if (r == null || r.isState(ActivityState.RESUMED)) {
                ActivityOptions.abort(options);
            } else {
                r.updateOptionsLocked(options);
            }
        }
        getDisplay().mDisplayContent.prepareAppTransition(transit, false);
    }

    private void updateTaskMovement(TaskRecord task, boolean toFront) {
        if (task.isPersistable) {
            task.mLastTimeMoved = System.currentTimeMillis();
            if (!toFront) {
                task.mLastTimeMoved *= -1;
            }
        }
        this.mRootActivityContainer.invalidateTaskLayers();
    }

    /* access modifiers changed from: package-private */
    public final void moveTaskToFrontLocked(TaskRecord tr, boolean noAnimation, ActivityOptions options, AppTimeTracker timeTracker, String reason) {
        Throwable th;
        if (ActivityTaskManagerDebugConfig.DEBUG_SWITCH) {
            Slog.v(TAG_SWITCH, "moveTaskToFront: " + tr);
        }
        if (getWindowingMode() == WindowConfiguration.WINDOWING_MODE_ZOOM) {
            ColorZoomWindowManagerHelper.getInstance();
            ColorZoomWindowManagerHelper.getZoomWindowManager().updateZoomStack(this, null, topRunningActivityLocked(), null);
        }
        ActivityStack topStack = getDisplay().getTopStack();
        ActivityRecord topActivity = topStack != null ? topStack.getTopActivity() : null;
        int numTasks = this.mTaskHistory.size();
        int index = this.mTaskHistory.indexOf(tr);
        if (numTasks != 0) {
            if (index >= 0) {
                if (timeTracker != null) {
                    for (int i = tr.mActivities.size() - 1; i >= 0; i--) {
                        tr.mActivities.get(i).appTimeTracker = timeTracker;
                    }
                }
                try {
                    getDisplay().deferUpdateImeTarget();
                    insertTaskAtTop(tr, null);
                    ActivityRecord top = tr.getTopActivity();
                    if (top != null) {
                        if (top.okToShowLocked()) {
                            ActivityRecord r = topRunningActivityLocked();
                            if (r != null) {
                                try {
                                    r.moveFocusableActivityToTop(reason);
                                } catch (Throwable th2) {
                                    th = th2;
                                    getDisplay().continueUpdateImeTarget();
                                    throw th;
                                }
                            }
                            if (ActivityTaskManagerDebugConfig.DEBUG_TRANSITION) {
                                Slog.v(TAG_TRANSITION, "Prepare to front transition: task=" + tr);
                            }
                            if (noAnimation) {
                                getDisplay().mDisplayContent.prepareAppTransition(0, false);
                                if (r != null) {
                                    this.mStackSupervisor.mNoAnimActivities.add(r);
                                }
                                ActivityOptions.abort(options);
                            } else {
                                updateTransitLocked(10, options);
                                OppoFeatureCache.get(IColorZoomWindowManager.DEFAULT).prepareZoomTransition(topStack, this);
                            }
                            if (canEnterPipOnTaskSwitch(topActivity, tr, null, options)) {
                                topActivity.supportsEnterPipOnTaskSwitch = true;
                            }
                            this.mRootActivityContainer.resumeFocusedStacksTopActivities();
                            EventLog.writeEvent((int) EventLogTags.AM_TASK_TO_FRONT, Integer.valueOf(tr.userId), Integer.valueOf(tr.taskId));
                            this.mService.getTaskChangeNotificationController().notifyTaskMovedToFront(tr.getTaskInfo());
                            getDisplay().continueUpdateImeTarget();
                            return;
                        }
                    }
                    if (top != null) {
                        this.mStackSupervisor.mRecentTasks.add(top.getTaskRecord());
                    }
                    ActivityOptions.abort(options);
                    getDisplay().continueUpdateImeTarget();
                    return;
                } catch (Throwable th3) {
                    th = th3;
                    getDisplay().continueUpdateImeTarget();
                    throw th;
                }
            }
        }
        if (noAnimation) {
            ActivityOptions.abort(options);
        } else {
            updateTransitLocked(10, options);
        }
    }

    /* access modifiers changed from: package-private */
    public final boolean moveTaskToBackLocked(int taskId) {
        TaskRecord tr = taskForIdLocked(taskId);
        if (tr == null) {
            Slog.i(TAG, "moveTaskToBack: bad taskId=" + taskId);
            return false;
        }
        Slog.i(TAG, "moveTaskToBack: " + tr);
        if (!this.mService.getLockTaskController().canMoveTaskToBack(tr)) {
            return false;
        }
        if (isTopStackOnDisplay() && this.mService.mController != null) {
            ActivityRecord next = topRunningActivityLocked(null, taskId);
            if (ActivityTaskManagerDebugConfig.DEBUG_TASKS || ActivityTaskManagerDebugConfig.DEBUG_STACK) {
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
        if (ActivityTaskManagerDebugConfig.DEBUG_TRANSITION) {
            Slog.v(TAG_TRANSITION, "Prepare to back transition: task=" + taskId);
        }
        this.mTaskHistory.remove(tr);
        this.mTaskHistory.add(0, tr);
        updateTaskMovement(tr, false);
        if (moveTaskToBackForSplitScreenMode()) {
            return true;
        }
        getDisplay().mDisplayContent.prepareAppTransition(11, false);
        moveToBack("moveTaskToBackLocked", tr);
        OppoFeatureCache.get(IColorZoomWindowManager.DEFAULT).prepareZoomTransition(this, getDisplay().getFocusedStack());
        if (this.mColorStackEx != null) {
            this.mColorStackEx.moveFreeformToBackLocked(inFreeformWindowingMode());
            if (inFreeformWindowingMode()) {
                for (int i = tr.mActivities.size() - 1; i >= 0; i--) {
                    ActivityRecord tmp = tr.mActivities.get(i);
                    if (tmp != null && tmp.frontOfTask) {
                        tmp.forceNewConfig = true;
                    }
                }
            }
        }
        if (inPinnedWindowingMode()) {
            this.mStackSupervisor.removeStack(this);
            return true;
        }
        if (this.mRootActivityContainer.getDefaultDisplay().getStack(5, 1) != null && !inFreeformWindowingMode()) {
            adjustFocusToNextFocusableStack("moveTaskToBackHasfreeform");
        }
        if (getWindowingMode() == WindowConfiguration.WINDOWING_MODE_ZOOM) {
            startPausingLocked(false, true, null, false, "moveTaskToBackLocked");
        }
        ActivityRecord topActivity = getDisplay().topRunningActivity();
        ActivityStack topStack = topActivity.getActivityStack();
        if (!(topStack == null || topStack == this || !topActivity.isState(ActivityState.RESUMED))) {
            this.mRootActivityContainer.ensureVisibilityAndConfig(null, getDisplay().mDisplayId, false, false);
        }
        this.mRootActivityContainer.resumeFocusedStacksTopActivities();
        return true;
    }

    static void logStartActivity(int tag, ActivityRecord r, TaskRecord task) {
        Uri data = r.intent.getData();
        EventLog.writeEvent(tag, Integer.valueOf(r.mUserId), Integer.valueOf(System.identityHashCode(r)), Integer.valueOf(task.taskId), r.shortComponentName, r.intent.getAction(), r.intent.getType(), data != null ? data.toSafeString() : null, Integer.valueOf(r.intent.getFlags()));
    }

    /* access modifiers changed from: package-private */
    public void ensureVisibleActivitiesConfigurationLocked(ActivityRecord start, boolean preserveWindow) {
        if (start != null && start.visible) {
            boolean behindFullscreen = false;
            boolean updatedConfig = false;
            for (int taskIndex = this.mTaskHistory.indexOf(start.getTaskRecord()); taskIndex >= 0; taskIndex--) {
                TaskRecord task = this.mTaskHistory.get(taskIndex);
                ArrayList<ActivityRecord> activities = task.mActivities;
                int activityIndex = start.getTaskRecord() == task ? activities.indexOf(start) : activities.size() - 1;
                while (true) {
                    if (activityIndex < 0) {
                        break;
                    }
                    ActivityRecord r = activities.get(activityIndex);
                    updatedConfig |= r.ensureActivityConfiguration(0, preserveWindow);
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
                this.mRootActivityContainer.resumeFocusedStacksTopActivities();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void requestResize(Rect bounds) {
        this.mService.resizeStack(this.mStackId, bounds, true, false, false, -1);
    }

    /* access modifiers changed from: package-private */
    public void resize(Rect bounds, Rect tempTaskBounds, Rect tempTaskInsetBounds) {
        if (updateBoundsAllowed(bounds)) {
            Rect taskBounds = tempTaskBounds != null ? tempTaskBounds : bounds;
            for (int i = this.mTaskHistory.size() - 1; i >= 0; i--) {
                TaskRecord task = this.mTaskHistory.get(i);
                if (task.isResizeable()) {
                    task.updateOverrideConfiguration(taskBounds, tempTaskInsetBounds);
                } else if (getWindowingMode() == WindowConfiguration.WINDOWING_MODE_ZOOM) {
                    task.updateOverrideConfiguration(taskBounds, tempTaskInsetBounds);
                }
            }
            if (!inSplitScreenWindowingMode()) {
                getTaskStack().setBounds(bounds);
            }
            setBounds(bounds);
        }
    }

    /* access modifiers changed from: package-private */
    public void onPipAnimationEndResize() {
        TaskStack taskStack = this.mTaskStack;
        if (taskStack != null) {
            taskStack.onPipAnimationEndResize();
        }
    }

    /* access modifiers changed from: package-private */
    public void setTaskBounds(Rect bounds) {
        if (updateBoundsAllowed(bounds)) {
            for (int i = this.mTaskHistory.size() - 1; i >= 0; i--) {
                TaskRecord task = this.mTaskHistory.get(i);
                if (task.isResizeable()) {
                    task.setBounds(bounds);
                } else {
                    task.setBounds(null);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setTaskDisplayedBounds(Rect bounds) {
        if (updateDisplayedBoundsAllowed(bounds)) {
            for (int i = this.mTaskHistory.size() - 1; i >= 0; i--) {
                TaskRecord task = this.mTaskHistory.get(i);
                if (bounds == null || bounds.isEmpty()) {
                    task.setDisplayedBounds(null);
                } else if (task.isResizeable()) {
                    task.setDisplayedBounds(bounds);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean willActivityBeVisibleLocked(IBinder token) {
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ArrayList<ActivityRecord> activities = this.mTaskHistory.get(taskNdx).mActivities;
            for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                ActivityRecord r = activities.get(activityNdx);
                if (r.appToken == token) {
                    return true;
                }
                if (r.fullscreen && !r.finishing) {
                    return false;
                }
            }
        }
        ActivityRecord r2 = ActivityRecord.forTokenLocked(token);
        if (r2 == null) {
            return false;
        }
        if (r2.finishing) {
            Slog.e(TAG, "willActivityBeVisibleLocked: Returning false, would have returned true for r=" + r2);
        }
        return true ^ r2.finishing;
    }

    /* access modifiers changed from: package-private */
    public void closeSystemDialogsLocked() {
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ArrayList<ActivityRecord> activities = this.mTaskHistory.get(taskNdx).mActivities;
            for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                ActivityRecord r = activities.get(activityNdx);
                if ((r.info.flags & 256) != 0) {
                    finishActivityLocked(r, 0, null, "close-sys", true);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean finishDisabledPackageActivitiesLocked(String packageName, Set<String> filterByClasses, boolean doit, boolean evenPersistent, int userId) {
        boolean didSomething = false;
        TaskRecord lastTask = null;
        ComponentName homeActivity = null;
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ArrayList<ActivityRecord> activities = this.mTaskHistory.get(taskNdx).mActivities;
            this.mTmpActivities.clear();
            this.mTmpActivities.addAll(activities);
            while (!this.mTmpActivities.isEmpty()) {
                boolean sameComponent = false;
                ActivityRecord r = this.mTmpActivities.remove(0);
                if ((r.packageName.equals(packageName) && (filterByClasses == null || filterByClasses.contains(r.mActivityComponent.getClassName()))) || (packageName == null && r.mUserId == userId)) {
                    sameComponent = true;
                }
                if ((userId == -1 || r.mUserId == userId) && ((sameComponent || r.getTaskRecord() == lastTask) && (r.app == null || evenPersistent || !r.app.isPersistent()))) {
                    if (doit) {
                        if (r.isActivityTypeHome()) {
                            if (homeActivity == null || !homeActivity.equals(r.mActivityComponent)) {
                                homeActivity = r.mActivityComponent;
                            } else {
                                Slog.i(TAG, "Skip force-stop again " + r);
                            }
                        }
                        Slog.i(TAG, "  Force finishing activity " + r);
                        TaskRecord lastTask2 = r.getTaskRecord();
                        finishActivityLocked(r, 0, null, "force-stop", true);
                        homeActivity = homeActivity;
                        didSomething = true;
                        lastTask = lastTask2;
                    } else if (!r.finishing) {
                        return true;
                    }
                }
            }
        }
        return didSomething;
    }

    /* access modifiers changed from: package-private */
    public void getRunningTasks(List<TaskRecord> tasksOut, @WindowConfiguration.ActivityType int ignoreActivityType, @WindowConfiguration.WindowingMode int ignoreWindowingMode, int callingUid, boolean allowed) {
        boolean focusedStack = this.mRootActivityContainer.getTopDisplayFocusedStack() == this;
        boolean topTask = true;
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            TaskRecord task = this.mTaskHistory.get(taskNdx);
            if (task.getTopActivity() != null && ((allowed || task.isActivityTypeHome() || task.effectiveUid == callingUid) && ((ignoreActivityType == 0 || task.getActivityType() != ignoreActivityType) && (ignoreWindowingMode == 0 || task.getWindowingMode() != ignoreWindowingMode)))) {
                if (focusedStack && topTask) {
                    task.lastActiveTime = SystemClock.elapsedRealtime();
                    topTask = false;
                }
                tasksOut.add(task);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void unhandledBackLocked() {
        int top = this.mTaskHistory.size() - 1;
        if (ActivityTaskManagerDebugConfig.DEBUG_SWITCH) {
            Slog.d(TAG_SWITCH, "Performing unhandledBack(): top activity at " + top);
        }
        if (top >= 0) {
            ArrayList<ActivityRecord> activities = this.mTaskHistory.get(top).mActivities;
            int activityTop = activities.size() - 1;
            if (activityTop >= 0) {
                finishActivityLocked(activities.get(activityTop), 0, null, "unhandled-back", true);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean handleAppDiedLocked(WindowProcessController app) {
        ActivityRecord activityRecord = this.mPausingActivity;
        if (activityRecord != null && activityRecord.app == app) {
            if (ActivityTaskManagerDebugConfig.DEBUG_PAUSE || ActivityTaskManagerDebugConfig.DEBUG_CLEANUP) {
                String str = TAG_PAUSE;
                Slog.v(str, "App died while pausing: " + this.mPausingActivity);
            }
            this.mPausingActivity = null;
        }
        ActivityRecord activityRecord2 = this.mLastPausedActivity;
        if (activityRecord2 != null && activityRecord2.app == app) {
            this.mLastPausedActivity = null;
            this.mLastNoHistoryActivity = null;
        }
        return removeHistoryRecordsForAppLocked(app);
    }

    /* access modifiers changed from: package-private */
    public void handleAppCrash(WindowProcessController app) {
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ArrayList<ActivityRecord> activities = this.mTaskHistory.get(taskNdx).mActivities;
            for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                if (activityNdx < activities.size()) {
                    ActivityRecord r = activities.get(activityNdx);
                    if (r.app == app) {
                        Slog.w(TAG, "  Force finishing activity " + r.intent.getComponent().flattenToShortString());
                        r.app = null;
                        getDisplay().mDisplayContent.prepareAppTransition(26, false);
                        finishCurrentActivityLocked(r, 0, false, "handleAppCrashedLocked");
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean dump(FileDescriptor fd, PrintWriter pw, boolean dumpAll, boolean dumpClient, String dumpPackage, boolean needSep) {
        pw.println("  Stack #" + this.mStackId + ": type=" + WindowConfiguration.activityTypeToString(getActivityType()) + " mode=" + WindowConfiguration.windowingModeToString(getWindowingMode()));
        StringBuilder sb = new StringBuilder();
        sb.append("  isSleeping=");
        sb.append(shouldSleepActivities());
        pw.println(sb.toString());
        pw.println("  mBounds=" + getRequestedOverrideBounds());
        boolean printed = ActivityStackSupervisor.dumpHistoryList(fd, pw, this.mLRUActivities, "    ", "Run", false, dumpAll ^ true, false, dumpPackage, true, "    Running activities (most recent first):", null) | dumpActivitiesLocked(fd, pw, dumpAll, dumpClient, dumpPackage, needSep);
        boolean needSep2 = printed;
        if (ActivityStackSupervisor.printThisActivity(pw, this.mPausingActivity, dumpPackage, needSep2, "    mPausingActivity: ")) {
            printed = true;
            needSep2 = false;
        }
        if (ActivityStackSupervisor.printThisActivity(pw, getResumedActivity(), dumpPackage, needSep2, "    mResumedActivity: ")) {
            printed = true;
            needSep2 = false;
        }
        if (!dumpAll) {
            return printed;
        }
        if (ActivityStackSupervisor.printThisActivity(pw, this.mLastPausedActivity, dumpPackage, needSep2, "    mLastPausedActivity: ")) {
            printed = true;
            needSep2 = true;
        }
        return printed | ActivityStackSupervisor.printThisActivity(pw, this.mLastNoHistoryActivity, dumpPackage, needSep2, "    mLastNoHistoryActivity: ");
    }

    /* access modifiers changed from: package-private */
    public boolean dumpActivitiesLocked(FileDescriptor fd, PrintWriter pw, boolean dumpAll, boolean dumpClient, String dumpPackage, boolean needSep) {
        if (this.mTaskHistory.isEmpty()) {
            return false;
        }
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx += -1) {
            TaskRecord task = this.mTaskHistory.get(taskNdx);
            if (needSep) {
                pw.println("");
            }
            pw.println("    Task id #" + task.taskId);
            pw.println("    mBounds=" + task.getRequestedOverrideBounds());
            pw.println("    mMinWidth=" + task.mMinWidth);
            pw.println("    mMinHeight=" + task.mMinHeight);
            pw.println("    mLastNonFullscreenBounds=" + task.mLastNonFullscreenBounds);
            pw.println("    * " + task);
            task.dump(pw, "      ");
            ActivityStackSupervisor.dumpHistoryList(fd, pw, this.mTaskHistory.get(taskNdx).mActivities, "    ", "Hist", true, dumpAll ^ true, dumpClient, dumpPackage, false, null, task);
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public ArrayList<ActivityRecord> getDumpActivitiesLocked(String name) {
        ArrayList<ActivityRecord> activities = new ArrayList<>();
        if ("all".equals(name)) {
            for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
                activities.addAll(this.mTaskHistory.get(taskNdx).mActivities);
            }
        } else if ("top".equals(name)) {
            int top = this.mTaskHistory.size() - 1;
            if (top >= 0) {
                ArrayList<ActivityRecord> list = this.mTaskHistory.get(top).mActivities;
                int listTop = list.size() - 1;
                if (listTop >= 0) {
                    activities.add(list.get(listTop));
                }
            }
        } else {
            ActivityManagerService.ItemMatcher matcher = new ActivityManagerService.ItemMatcher();
            matcher.build(name);
            for (int taskNdx2 = this.mTaskHistory.size() - 1; taskNdx2 >= 0; taskNdx2--) {
                Iterator<ActivityRecord> it = this.mTaskHistory.get(taskNdx2).mActivities.iterator();
                while (it.hasNext()) {
                    ActivityRecord r1 = it.next();
                    if (matcher.match(r1, r1.intent.getComponent())) {
                        activities.add(r1);
                    }
                }
            }
        }
        return activities;
    }

    /* access modifiers changed from: package-private */
    public ActivityRecord restartPackage(String packageName) {
        ActivityRecord starting = topRunningActivityLocked();
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ArrayList<ActivityRecord> activities = this.mTaskHistory.get(taskNdx).mActivities;
            for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                ActivityRecord a = activities.get(activityNdx);
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

    /* access modifiers changed from: package-private */
    public void removeTask(TaskRecord task, String reason, int mode) {
        if (this.mTaskHistory.remove(task)) {
            EventLog.writeEvent((int) EventLogTags.AM_REMOVE_TASK, Integer.valueOf(task.taskId), Integer.valueOf(getStackId()));
        }
        if (ActivityTaskManagerDebugConfig.DEBUG_STACK) {
            Slog.i(TAG_STACK, "removeTask: " + task + StringUtils.SPACE + Debug.getCallers(8));
        }
        removeActivitiesFromLRUListLocked(task);
        updateTaskMovement(task, true);
        if (mode == 0) {
            task.cleanUpResourcesForDestroy();
        }
        if (this.mTaskHistory.isEmpty()) {
            if (ActivityTaskManagerDebugConfig.DEBUG_STACK) {
                Slog.i(TAG_STACK, "removeTask: removing stack=" + this);
            }
            if (mode != 2 && this.mRootActivityContainer.isTopDisplayFocusedStack(this)) {
                String myReason = reason + " leftTaskHistoryEmpty";
                if (!inMultiWindowMode() || adjustFocusToNextFocusableStack(myReason) == null) {
                    getDisplay().moveHomeStackToFront(myReason);
                }
            }
            if (isAttached()) {
                getDisplay().positionChildAtBottom(this);
            }
            if (!isActivityTypeHome() || !isAttached()) {
                remove();
            }
        }
        task.setStack(null);
        if (inPinnedWindowingMode()) {
            this.mService.getTaskChangeNotificationController().notifyActivityUnpinned();
        }
    }

    /* access modifiers changed from: package-private */
    public TaskRecord createTaskRecord(int taskId, ActivityInfo info, Intent intent, IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor, boolean toTop) {
        return createTaskRecord(taskId, info, intent, voiceSession, voiceInteractor, toTop, null, null, null);
    }

    /* access modifiers changed from: package-private */
    public TaskRecord createTaskRecord(int taskId, ActivityInfo info, Intent intent, IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor, boolean toTop, ActivityRecord activity, ActivityRecord source, ActivityOptions options) {
        TaskRecord task = TaskRecord.create(this.mService, taskId, info, intent, voiceSession, voiceInteractor);
        addTask(task, toTop, "createTaskRecord");
        int displayId = this.mDisplayId;
        boolean z = false;
        if (displayId == -1) {
            displayId = 0;
        }
        boolean isLockscreenShown = this.mService.mStackSupervisor.getKeyguardController().isKeyguardOrAodShowing(displayId);
        if (!this.mStackSupervisor.getLaunchParamsController().layoutTask(task, info.windowLayout, activity, source, options) && !matchParentBounds() && task.isResizeable() && !isLockscreenShown) {
            task.updateOverrideConfiguration(getRequestedOverrideBounds());
        }
        if ((info.flags & 1024) != 0) {
            z = true;
        }
        task.createTask(toTop, z);
        return task;
    }

    /* access modifiers changed from: package-private */
    public ArrayList<TaskRecord> getAllTasks() {
        return new ArrayList<>(this.mTaskHistory);
    }

    /* access modifiers changed from: package-private */
    public void addTask(TaskRecord task, boolean toTop, String reason) {
        addTask(task, toTop ? Integer.MAX_VALUE : 0, true, reason);
        if (toTop) {
            positionChildWindowContainerAtTop(task);
        }
    }

    /* access modifiers changed from: package-private */
    public void addTask(TaskRecord task, int position, boolean schedulePictureInPictureModeChange, String reason) {
        this.mTaskHistory.remove(task);
        if (!isSingleTaskInstance() || this.mTaskHistory.isEmpty()) {
            int position2 = getAdjustedPositionForTask(task, position, null);
            boolean toTop = position2 >= this.mTaskHistory.size();
            ActivityStack prevStack = preAddTask(task, reason, toTop);
            this.mTaskHistory.add(position2, task);
            task.setStack(this);
            updateTaskMovement(task, toTop);
            postAddTask(task, prevStack, schedulePictureInPictureModeChange);
            return;
        }
        throw new IllegalStateException("Can only have one child on stack=" + this);
    }

    /* access modifiers changed from: package-private */
    public void positionChildAt(TaskRecord task, int index) {
        if (task.getStack() == this) {
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
                topRunningActivity.setState(ActivityState.RESUMED, "positionChildAt");
            }
            ensureActivitiesVisibleLocked(null, 0, false);
            this.mRootActivityContainer.resumeFocusedStacksTopActivities();
            return;
        }
        throw new IllegalArgumentException("AS.positionChildAt: task=" + task + " is not a child of stack=" + this + " current parent=" + task.getStack());
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

    @Override // com.android.server.wm.ConfigurationContainer
    public void setAlwaysOnTop(boolean alwaysOnTop) {
        if (isAlwaysOnTop() != alwaysOnTop) {
            super.setAlwaysOnTop(alwaysOnTop);
            getDisplay().positionChildAtTop(this, false);
        }
    }

    /* access modifiers changed from: package-private */
    public void moveToFrontAndResumeStateIfNeeded(ActivityRecord r, boolean moveToFront, boolean setResume, boolean setPause, String reason) {
        if (moveToFront) {
            ActivityState origState = r.getState();
            if (setResume) {
                r.setState(ActivityState.RESUMED, "moveToFrontAndResumeStateIfNeeded");
                updateLRUListLocked(r);
            }
            if (setPause) {
                this.mPausingActivity = r;
                schedulePauseTimeout(r);
            }
            moveToFront(reason);
            if (origState == ActivityState.RESUMED && r == this.mRootActivityContainer.getTopResumedActivity()) {
                this.mService.setResumedActivityUncheckLocked(r, reason);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public Rect getDefaultPictureInPictureBounds(float aspectRatio) {
        if (getTaskStack() == null) {
            return null;
        }
        return getTaskStack().getPictureInPictureBounds(aspectRatio, null);
    }

    /* access modifiers changed from: package-private */
    public void animateResizePinnedStack(Rect sourceHintBounds, Rect toBounds, int animationDuration, boolean fromFullscreen) {
        if (inPinnedWindowingMode()) {
            if (skipResizeAnimation(toBounds == null)) {
                this.mService.moveTasksToFullscreenStack(this.mStackId, true);
            } else if (getTaskStack() != null) {
                getTaskStack().animateResizePinnedStack(toBounds, sourceHintBounds, animationDuration, fromFullscreen);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void getAnimationOrCurrentBounds(Rect outBounds) {
        TaskStack stack = getTaskStack();
        if (stack == null) {
            outBounds.setEmpty();
        } else {
            stack.getAnimationOrCurrentBounds(outBounds);
        }
    }

    private boolean skipResizeAnimation(boolean toFullscreen) {
        if (!toFullscreen) {
            return false;
        }
        Configuration parentConfig = getParent().getConfiguration();
        ActivityRecord top = topRunningNonOverlayTaskActivity();
        if (top == null || top.isConfigurationCompatible(parentConfig)) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public void setPictureInPictureAspectRatio(float aspectRatio) {
        if (getTaskStack() != null) {
            getTaskStack().setPictureInPictureAspectRatio(aspectRatio);
        }
    }

    /* access modifiers changed from: package-private */
    public void setPictureInPictureActions(List<RemoteAction> actions) {
        if (getTaskStack() != null) {
            getTaskStack().setPictureInPictureActions(actions);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isAnimatingBoundsToFullscreen() {
        if (getTaskStack() == null) {
            return false;
        }
        return getTaskStack().isAnimatingBoundsToFullscreen();
    }

    public void updatePictureInPictureModeForPinnedStackAnimation(Rect targetStackBounds, boolean forceUpdate) {
        synchronized (this.mService.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (isAttached()) {
                    ArrayList<TaskRecord> tasks = getAllTasks();
                    for (int i = 0; i < tasks.size(); i++) {
                        this.mStackSupervisor.updatePictureInPictureMode(tasks.get(i), targetStackBounds, forceUpdate);
                    }
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public int getStackId() {
        return this.mStackId;
    }

    public String toString() {
        return "ActivityStack{" + Integer.toHexString(System.identityHashCode(this)) + " stackId=" + this.mStackId + " type=" + WindowConfiguration.activityTypeToString(getActivityType()) + " mode=" + WindowConfiguration.windowingModeToString(getWindowingMode()) + " visible=" + shouldBeVisible(null) + " translucent=" + isStackTranslucent(null) + ", " + this.mTaskHistory.size() + " tasks}";
    }

    /* access modifiers changed from: package-private */
    public void onLockTaskPackagesUpdated() {
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            this.mTaskHistory.get(taskNdx).setLockTaskAuth();
        }
    }

    /* access modifiers changed from: private */
    public class UsageRecorderRunnable implements Runnable {
        private String mProcName = null;
        private String mTimeStr = null;

        public UsageRecorderRunnable(String procName, String timeStr) {
            this.mProcName = procName;
            this.mTimeStr = timeStr;
        }

        public void run() {
            String str;
            String str2 = this.mProcName;
            if (str2 != null && str2.length() > 0 && (str = this.mTimeStr) != null && str.length() > 0) {
                OppoUsageManager.getOppoUsageManager().writeAppUsageHistoryRecord(this.mProcName, this.mTimeStr);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void executeAppTransition(ActivityOptions options) {
        getDisplay().mDisplayContent.executeAppTransition();
        ActivityOptions.abort(options);
    }

    /* access modifiers changed from: package-private */
    public boolean shouldSleepActivities() {
        ActivityDisplay display = getDisplay();
        if (!isFocusedStackOnDisplay() || !this.mStackSupervisor.getKeyguardController().isKeyguardGoingAway()) {
            return display != null ? display.isSleeping() : this.mService.isSleepingLocked();
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean shouldSleepOrShutDownActivities() {
        return shouldSleepActivities() || this.mService.mShuttingDown;
    }

    @Override // com.android.server.wm.ConfigurationContainer
    public void writeToProto(ProtoOutputStream proto, long fieldId, int logLevel) {
        long token = proto.start(fieldId);
        super.writeToProto(proto, 1146756268033L, logLevel);
        proto.write(1120986464258L, this.mStackId);
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            this.mTaskHistory.get(taskNdx).writeToProto(proto, 2246267895811L, logLevel);
        }
        ActivityRecord activityRecord = this.mResumedActivity;
        if (activityRecord != null) {
            activityRecord.writeIdentifierToProto(proto, 1146756268036L);
        }
        proto.write(1120986464261L, this.mDisplayId);
        if (!matchParentBounds()) {
            getRequestedOverrideBounds().writeToProto(proto, 1146756268039L);
        }
        proto.write(1133871366150L, matchParentBounds());
        proto.end(token);
    }

    public final class ColorActivityStackInner implements IColorActivityStackInner {
        public ColorActivityStackInner() {
        }

        @Override // com.android.server.wm.IColorActivityStackInner
        public boolean canEnterPipOnTaskSwitch(ActivityRecord pipCandidate, TaskRecord toFrontTask, ActivityRecord toFrontActivity, ActivityOptions opts) {
            return ActivityStack.this.canEnterPipOnTaskSwitch(pipCandidate, toFrontTask, toFrontActivity, opts);
        }

        @Override // com.android.server.wm.IColorActivityStackInner
        public void removeActivityFromHistoryLocked(ActivityRecord r, String reason) {
            ActivityStack.this.removeActivityFromHistoryLocked(r, reason);
        }

        @Override // com.android.server.wm.IColorActivityStackInner
        public ArrayList<ActivityRecord> getLRUActivities() {
            return ActivityStack.this.mLRUActivities;
        }
    }

    @Override // com.android.server.wm.OppoBaseActivityStack
    public IColorActivityStackInner createColorActivityStackInner() {
        return new ColorActivityStackInner();
    }
}
