package com.android.server.am;

import android.annotation.OppoHook;
import android.annotation.OppoHook.OppoHookType;
import android.annotation.OppoHook.OppoRomType;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.ActivityManager.StackId;
import android.app.ActivityManager.StackInfo;
import android.app.ActivityOptions;
import android.app.AppGlobals;
import android.app.AppOpsManager;
import android.app.IActivityContainer;
import android.app.IActivityContainer.Stub;
import android.app.IActivityContainerCallback;
import android.app.IActivityManager.WaitResult;
import android.app.IApplicationThread;
import android.app.ProfilerInfo;
import android.app.admin.IDevicePolicyManager;
import android.content.ComponentName;
import android.content.IIntentSender;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.hardware.display.DisplayManager;
import android.hardware.display.DisplayManager.DisplayListener;
import android.hardware.display.DisplayManagerGlobal;
import android.hardware.display.VirtualDisplay;
import android.hardware.input.InputManagerInternal;
import android.os.Binder;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.Trace;
import android.os.TransactionTooLargeException;
import android.os.UserManager;
import android.os.WorkSource;
import android.provider.Settings.Secure;
import android.provider.Settings.SettingNotFoundException;
import android.service.voice.IVoiceInteractionSession;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.EventLog;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.Display;
import android.view.DisplayInfo;
import android.view.InputEvent;
import android.view.Surface;
import com.android.internal.os.TransferPipe;
import com.android.internal.statusbar.IStatusBarService;
import com.android.internal.util.ArrayUtils;
import com.android.internal.widget.LockPatternUtils;
import com.android.server.LocalServices;
import com.android.server.display.OppoBrightUtils;
import com.android.server.oppo.IElsaManager;
import com.android.server.pm.OppoPackageManagerHelper;
import com.android.server.wm.WindowManagerService;
import com.mediatek.am.AMEventHookData.AfterActivityPaused;
import com.mediatek.multiwindow.MultiWindowManager;
import com.mediatek.perfservice.PerfServiceWrapper;
import com.mediatek.server.am.AMEventHook.Event;
import com.oppo.hypnus.Hypnus;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

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
public final class ActivityStackSupervisor implements DisplayListener {
    private static final ArrayMap<String, String> ACTION_TO_RUNTIME_PERMISSION = null;
    private static final int ACTIVITY_RESTRICTION_APPOP = 2;
    private static final int ACTIVITY_RESTRICTION_NONE = 0;
    private static final int ACTIVITY_RESTRICTION_PERMISSION = 1;
    static final int CONTAINER_CALLBACK_TASK_LIST_EMPTY = 111;
    static final int CONTAINER_CALLBACK_VISIBILITY = 108;
    static final boolean CREATE_IF_NEEDED = true;
    static final boolean DEFER_RESUME = true;
    private static final int FIT_WITHIN_BOUNDS_DIVIDER = 3;
    static final boolean FORCE_FOCUS = true;
    static final int HANDLE_DISPLAY_ADDED = 105;
    static final int HANDLE_DISPLAY_CHANGED = 106;
    static final int HANDLE_DISPLAY_REMOVED = 107;
    static final int IDLE_NOW_MSG = 101;
    static final int IDLE_TIMEOUT = 10000;
    static final int IDLE_TIMEOUT_MSG = 100;
    static final int LAUNCH_TASK_BEHIND_COMPLETE = 112;
    static final int LAUNCH_TIMEOUT = 10000;
    static final int LAUNCH_TIMEOUT_MSG = 104;
    static final int LOCK_TASK_END_MSG = 110;
    static final int LOCK_TASK_START_MSG = 109;
    private static final String LOCK_TASK_TAG = "Lock-to-App";
    private static final int MAX_TASK_IDS_PER_USER = 100000;
    static final boolean MOVING = true;
    static final boolean ON_TOP = true;
    protected static final String OPPO_SAFECENTER_PASSWORD = "com.coloros.safecenter/.privacy.view.password";
    static final boolean PRESERVE_WINDOWS = true;
    static final int REPORT_MULTI_WINDOW_MODE_CHANGED_MSG = 114;
    static final int REPORT_PIP_MODE_CHANGED_MSG = 115;
    static final boolean RESTORE_FROM_RECENTS = true;
    static final int RESUME_TOP_ACTIVITY_MSG = 102;
    static final int SHOW_LOCK_TASK_ESCAPE_MESSAGE_MSG = 113;
    static final int SLEEP_TIMEOUT = 5000;
    static final int SLEEP_TIMEOUT_MSG = 103;
    private static final String TAG = null;
    private static final String TAG_CONTAINERS = null;
    private static final String TAG_IDLE = null;
    private static final String TAG_LOCKTASK = null;
    private static final String TAG_PAUSE = null;
    private static final String TAG_RECENTS = null;
    private static final String TAG_RELEASE = null;
    private static final String TAG_STACK = null;
    private static final String TAG_STATES = null;
    private static final String TAG_SWITCH = null;
    static final String TAG_TASKS = null;
    private static final String TAG_VISIBLE_BEHIND = null;
    static final boolean VALIDATE_WAKE_LOCK_CALLER = false;
    private static final String VIRTUAL_DISPLAY_BASE_NAME = "ActivityViewVirtualDisplay";
    public static Hypnus mHyp;
    final String ACTION_OPPO_SAFE_COUNT_START_URL;
    boolean inResumeTopActivity;
    private SparseArray<ActivityContainer> mActivityContainers;
    private final SparseArray<ActivityDisplay> mActivityDisplays;
    final ActivityMetricsLogger mActivityMetricsLogger;
    private boolean mAllowDockedStackResize;
    boolean mAppVisibilitiesChangedSinceLastPause;
    private final SparseIntArray mCurTaskIdForUser;
    int mCurrentUser;
    int mDefaultMinSizeOfResizeableTask;
    private IDevicePolicyManager mDevicePolicyManager;
    DisplayManager mDisplayManager;
    final ArrayList<ActivityRecord> mFinishingActivities;
    ActivityStack mFocusedStack;
    WakeLock mGoingToSleep;
    final ArrayList<ActivityRecord> mGoingToSleepActivities;
    final ActivityStackSupervisorHandler mHandler;
    ActivityStack mHomeStack;
    InputManagerInternal mInputManagerInternal;
    boolean mIsDockMinimized;
    private ActivityStack mLastFocusedStack;
    SimpleActivityInfo mLastResumedActivity;
    WakeLock mLaunchingActivity;
    private int mLockTaskModeState;
    ArrayList<TaskRecord> mLockTaskModeTasks;
    private LockTaskNotify mLockTaskNotify;
    final ArrayList<ActivityRecord> mMultiWindowModeChangedActivities;
    private int mNextFreeStackId;
    int mOppoCustomMinSizeOfResizeableTask;
    @OppoHook(level = OppoHookType.NEW_FIELD, note = "ZhiYong.Lin@Plf.Framework, modify for secure protect", property = OppoRomType.ROM)
    OppoSecureProtectUtils mOppoSecureProtectUtils;
    private PerfServiceWrapper mPerfService;
    final ArrayList<ActivityRecord> mPipModeChangedActivities;
    private RecentTasks mRecentTasks;
    private final ResizeDockedStackTimeout mResizeDockedStackTimeout;
    private final ArraySet<Integer> mResizingTasksDuringAnimation;
    final ActivityManagerService mService;
    boolean mSleepTimeout;
    final ArrayList<UserState> mStartingUsers;
    private IStatusBarService mStatusBarService;
    final ArrayList<ActivityRecord> mStoppingActivities;
    private boolean mTaskLayersChanged;
    private final SparseArray<Rect> mTmpBounds;
    private final SparseArray<Configuration> mTmpConfigs;
    private final FindTaskResult mTmpFindTaskResult;
    private final SparseArray<Rect> mTmpInsetBounds;
    private IBinder mToken;
    boolean mUserLeaving;
    SparseIntArray mUserStackInFront;
    final ArrayList<WaitResult> mWaitingActivityLaunched;
    final ArrayList<WaitResult> mWaitingActivityVisible;
    final ArrayList<ActivityRecord> mWaitingVisibleActivities;
    WindowManagerService mWindowManager;
    private final Rect tempRect;
    private final Rect tempRect2;

    class ActivityContainer extends Stub {
        static final int CONTAINER_STATE_FINISHING = 2;
        static final int CONTAINER_STATE_HAS_SURFACE = 0;
        static final int CONTAINER_STATE_NO_SURFACE = 1;
        static final int FORCE_NEW_TASK_FLAGS = 402718720;
        ActivityDisplay mActivityDisplay;
        IActivityContainerCallback mCallback = null;
        int mContainerState = 0;
        String mIdString;
        ActivityRecord mParentActivity = null;
        final ActivityStack mStack;
        final int mStackId;
        boolean mVisible = true;

        ActivityContainer(int stackId) {
            synchronized (ActivityStackSupervisor.this.mService) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    this.mStackId = stackId;
                    this.mStack = new ActivityStack(this, ActivityStackSupervisor.this.mRecentTasks);
                    this.mIdString = "ActivtyContainer{" + this.mStackId + "}";
                    if (ActivityManagerDebugConfig.DEBUG_STACK) {
                        Slog.d(ActivityStackSupervisor.TAG_STACK, "Creating " + this);
                    }
                } finally {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        void attachToDisplayLocked(ActivityDisplay activityDisplay, boolean onTop) {
            if (ActivityManagerDebugConfig.DEBUG_STACK) {
                Slog.d(ActivityStackSupervisor.TAG_STACK, "attachToDisplayLocked: " + this + " to display=" + activityDisplay + " onTop=" + onTop);
            }
            this.mActivityDisplay = activityDisplay;
            this.mStack.attachDisplay(activityDisplay, onTop);
            activityDisplay.attachActivities(this.mStack, onTop);
        }

        public void attachToDisplay(int displayId) {
            synchronized (ActivityStackSupervisor.this.mService) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    ActivityDisplay activityDisplay = (ActivityDisplay) ActivityStackSupervisor.this.mActivityDisplays.get(displayId);
                    if (activityDisplay == null) {
                    } else {
                        attachToDisplayLocked(activityDisplay, true);
                        ActivityManagerService.resetPriorityAfterLockedSection();
                    }
                } finally {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        public int getDisplayId() {
            int i;
            synchronized (ActivityStackSupervisor.this.mService) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    if (this.mActivityDisplay != null) {
                        i = this.mActivityDisplay.mDisplayId;
                    } else {
                        ActivityManagerService.resetPriorityAfterLockedSection();
                        return -1;
                    }
                } finally {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
            return i;
        }

        public int getStackId() {
            int i;
            synchronized (ActivityStackSupervisor.this.mService) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    i = this.mStackId;
                } finally {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
            return i;
        }

        public boolean injectEvent(InputEvent event) {
            long origId = Binder.clearCallingIdentity();
            try {
                synchronized (ActivityStackSupervisor.this.mService) {
                    ActivityManagerService.boostPriorityForLockedSection();
                    if (this.mActivityDisplay != null) {
                        boolean injectInputEvent = ActivityStackSupervisor.this.mInputManagerInternal.injectInputEvent(event, this.mActivityDisplay.mDisplayId, 0);
                        ActivityManagerService.resetPriorityAfterLockedSection();
                        Binder.restoreCallingIdentity(origId);
                        return injectInputEvent;
                    }
                    ActivityManagerService.resetPriorityAfterLockedSection();
                    Binder.restoreCallingIdentity(origId);
                    return false;
                }
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(origId);
            }
        }

        public void release() {
            synchronized (ActivityStackSupervisor.this.mService) {
                long origId;
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    if (this.mContainerState == 2) {
                        ActivityManagerService.resetPriorityAfterLockedSection();
                        return;
                    }
                    this.mContainerState = 2;
                    origId = Binder.clearCallingIdentity();
                    this.mStack.finishAllActivitiesLocked(false);
                    ActivityStackSupervisor.this.mService.mActivityStarter.removePendingActivityLaunchesLocked(this.mStack);
                    Binder.restoreCallingIdentity(origId);
                    ActivityManagerService.resetPriorityAfterLockedSection();
                } catch (Throwable th) {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        protected void detachLocked() {
            if (ActivityManagerDebugConfig.DEBUG_STACK) {
                Slog.d(ActivityStackSupervisor.TAG_STACK, "detachLocked: " + this + " from display=" + this.mActivityDisplay + " Callers=" + Debug.getCallers(2));
            }
            if (this.mActivityDisplay != null) {
                this.mActivityDisplay.detachActivitiesLocked(this.mStack);
                this.mActivityDisplay = null;
                this.mStack.detachDisplay();
            }
        }

        public final int startActivity(Intent intent) {
            return ActivityStackSupervisor.this.mService.startActivity(intent, this);
        }

        public final int startActivityIntentSender(IIntentSender intentSender) throws TransactionTooLargeException {
            ActivityStackSupervisor.this.mService.enforceNotIsolatedCaller("ActivityContainer.startActivityIntentSender");
            if (intentSender instanceof PendingIntentRecord) {
                PendingIntentRecord pendingIntent = (PendingIntentRecord) intentSender;
                checkEmbeddedAllowedInner(ActivityStackSupervisor.this.mService.mUserController.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), ActivityStackSupervisor.this.mCurrentUser, false, 2, "ActivityContainer", null), pendingIntent.key.requestIntent, pendingIntent.key.requestResolvedType);
                return pendingIntent.sendInner(0, null, null, null, null, null, null, 0, FORCE_NEW_TASK_FLAGS, FORCE_NEW_TASK_FLAGS, null, this);
            }
            throw new IllegalArgumentException("Bad PendingIntent object");
        }

        void checkEmbeddedAllowedInner(int userId, Intent intent, String resolvedType) {
            ActivityInfo aInfo = ActivityStackSupervisor.this.resolveActivity(intent, resolvedType, 0, null, userId);
            if (aInfo != null && (aInfo.flags & Integer.MIN_VALUE) == 0) {
                throw new SecurityException("Attempt to embed activity that has not set allowEmbedded=\"true\"");
            }
        }

        public IBinder asBinder() {
            return this;
        }

        public void setSurface(Surface surface, int width, int height, int density) {
            ActivityStackSupervisor.this.mService.enforceNotIsolatedCaller("ActivityContainer.attachToSurface");
        }

        ActivityStackSupervisor getOuter() {
            return ActivityStackSupervisor.this;
        }

        boolean isAttachedLocked() {
            return this.mActivityDisplay != null;
        }

        void setVisible(boolean visible) {
            if (this.mVisible != visible) {
                this.mVisible = visible;
                if (this.mCallback != null) {
                    int i;
                    ActivityStackSupervisorHandler activityStackSupervisorHandler = ActivityStackSupervisor.this.mHandler;
                    if (visible) {
                        i = 1;
                    } else {
                        i = 0;
                    }
                    activityStackSupervisorHandler.obtainMessage(108, i, 0, this).sendToTarget();
                }
            }
        }

        void setDrawn() {
        }

        boolean isEligibleForNewTasks() {
            return true;
        }

        void onTaskListEmptyLocked() {
            detachLocked();
            ActivityStackSupervisor.this.deleteActivityContainer(this);
            ActivityStackSupervisor.this.mHandler.obtainMessage(111, this).sendToTarget();
        }

        public String toString() {
            return this.mIdString + (this.mActivityDisplay == null ? "N" : "A");
        }
    }

    class ActivityDisplay {
        Display mDisplay;
        int mDisplayId;
        DisplayInfo mDisplayInfo = new DisplayInfo();
        final ArrayList<ActivityStack> mStacks = new ArrayList();
        ActivityRecord mVisibleBehindActivity;

        ActivityDisplay() {
        }

        ActivityDisplay(int displayId) {
            Display display = ActivityStackSupervisor.this.mDisplayManager.getDisplay(displayId);
            if (display != null) {
                init(display);
            }
        }

        void init(Display display) {
            this.mDisplay = display;
            this.mDisplayId = display.getDisplayId();
            this.mDisplay.getDisplayInfo(this.mDisplayInfo);
        }

        void attachActivities(ActivityStack stack, boolean onTop) {
            if (ActivityManagerDebugConfig.DEBUG_STACK) {
                Slog.v(ActivityStackSupervisor.TAG_STACK, "attachActivities: attaching " + stack + " to displayId=" + this.mDisplayId + " onTop=" + onTop);
            }
            if (onTop) {
                this.mStacks.add(stack);
            } else {
                this.mStacks.add(0, stack);
            }
        }

        void detachActivitiesLocked(ActivityStack stack) {
            if (ActivityManagerDebugConfig.DEBUG_STACK) {
                Slog.v(ActivityStackSupervisor.TAG_STACK, "detachActivitiesLocked: detaching " + stack + " from displayId=" + this.mDisplayId);
            }
            this.mStacks.remove(stack);
        }

        void setVisibleBehindActivity(ActivityRecord r) {
            this.mVisibleBehindActivity = r;
        }

        boolean hasVisibleBehindActivity() {
            return this.mVisibleBehindActivity != null;
        }

        public String toString() {
            return "ActivityDisplay={" + this.mDisplayId + " numStacks=" + this.mStacks.size() + "}";
        }
    }

    private final class ActivityStackSupervisorHandler extends Handler {
        public ActivityStackSupervisorHandler(Looper looper) {
            super(looper);
        }

        void activityIdleInternal(ActivityRecord r) {
            IBinder iBinder = null;
            synchronized (ActivityStackSupervisor.this.mService) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    ActivityStackSupervisor activityStackSupervisor = ActivityStackSupervisor.this;
                    if (r != null) {
                        iBinder = r.appToken;
                    }
                    activityStackSupervisor.activityIdleInternalLocked(iBinder, true, null);
                } finally {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        public void handleMessage(Message msg) {
            ActivityContainer container;
            IActivityContainerCallback callback;
            int i;
            switch (msg.what) {
                case 100:
                    if (ActivityManagerDebugConfig.DEBUG_IDLE) {
                        Slog.d(ActivityStackSupervisor.TAG_IDLE, "handleMessage: IDLE_TIMEOUT_MSG: r=" + msg.obj);
                    }
                    if (!ActivityStackSupervisor.this.mService.mDidDexOpt) {
                        activityIdleInternal((ActivityRecord) msg.obj);
                        break;
                    }
                    ActivityStackSupervisor.this.mService.mDidDexOpt = false;
                    Message nmsg = ActivityStackSupervisor.this.mHandler.obtainMessage(100);
                    nmsg.obj = msg.obj;
                    ActivityStackSupervisor.this.mHandler.sendMessageDelayed(nmsg, 10000);
                    return;
                case 101:
                    if (ActivityManagerDebugConfig.DEBUG_IDLE) {
                        Slog.d(ActivityStackSupervisor.TAG_IDLE, "handleMessage: IDLE_NOW_MSG: r=" + msg.obj);
                    }
                    activityIdleInternal((ActivityRecord) msg.obj);
                    break;
                case 102:
                    synchronized (ActivityStackSupervisor.this.mService) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            ActivityStackSupervisor.this.resumeFocusedStackTopActivityLocked();
                        } finally {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    break;
                case 103:
                    synchronized (ActivityStackSupervisor.this.mService) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            if (ActivityStackSupervisor.this.mService.isSleepingOrShuttingDownLocked()) {
                                Slog.w(ActivityStackSupervisor.TAG, "Sleep timeout!  Sleeping now.");
                                ActivityStackSupervisor.this.mSleepTimeout = true;
                                ActivityStackSupervisor.this.checkReadyForSleepLocked();
                            } else if (ActivityManagerService.DEBUG_COLOROS_AMS && ActivityStackSupervisor.this.mGoingToSleep.isHeld()) {
                                Slog.w(ActivityStackSupervisor.TAG, "Sleep timeout but not sleeping, still hold wake lock.");
                            }
                        } finally {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    break;
                case 104:
                    if (!ActivityStackSupervisor.this.mService.mDidDexOpt) {
                        synchronized (ActivityStackSupervisor.this.mService) {
                            try {
                                ActivityManagerService.boostPriorityForLockedSection();
                                if (ActivityStackSupervisor.this.mLaunchingActivity.isHeld()) {
                                    Slog.w(ActivityStackSupervisor.TAG, "Launch timeout has expired, giving up wake lock!");
                                    Slog.d(ActivityStackSupervisor.TAG, "mLaunchingActivity.release() in LAUNCH_TIMEOUT_MSG");
                                    ActivityStackSupervisor.this.mLaunchingActivity.release();
                                    if (ActivityStackSupervisor.this.mPerfService != null) {
                                        ActivityStackSupervisor.this.mPerfService.amsBoostStop();
                                    }
                                }
                            } finally {
                                ActivityManagerService.resetPriorityAfterLockedSection();
                            }
                        }
                        break;
                    }
                    ActivityStackSupervisor.this.mService.mDidDexOpt = false;
                    ActivityStackSupervisor.this.mHandler.sendEmptyMessageDelayed(104, 10000);
                    return;
                case 105:
                    ActivityStackSupervisor.this.handleDisplayAdded(msg.arg1);
                    break;
                case 106:
                    ActivityStackSupervisor.this.handleDisplayChanged(msg.arg1);
                    break;
                case 107:
                    ActivityStackSupervisor.this.handleDisplayRemoved(msg.arg1);
                    break;
                case 108:
                    container = msg.obj;
                    callback = container.mCallback;
                    if (callback != null) {
                        try {
                            callback.setVisible(container.asBinder(), msg.arg1 == 1);
                            break;
                        } catch (RemoteException e) {
                            break;
                        }
                    }
                    break;
                case 109:
                    try {
                        if (ActivityStackSupervisor.this.mLockTaskNotify == null) {
                            ActivityStackSupervisor.this.mLockTaskNotify = new LockTaskNotify(ActivityStackSupervisor.this.mService.mContext);
                        }
                        ActivityStackSupervisor.this.mLockTaskNotify.show(true);
                        ActivityStackSupervisor.this.mLockTaskModeState = msg.arg2;
                        if (ActivityStackSupervisor.this.getStatusBarService() != null) {
                            int flags = 0;
                            if (ActivityStackSupervisor.this.mLockTaskModeState == 1) {
                                flags = 62849024;
                            } else if (ActivityStackSupervisor.this.mLockTaskModeState == 2) {
                                flags = 43974656;
                            }
                            ActivityStackSupervisor.this.getStatusBarService().disable(flags ^ 8388608, ActivityStackSupervisor.this.mToken, ActivityStackSupervisor.this.mService.mContext.getPackageName());
                        }
                        ActivityStackSupervisor.this.mWindowManager.disableKeyguard(ActivityStackSupervisor.this.mToken, ActivityStackSupervisor.LOCK_TASK_TAG);
                        if (ActivityStackSupervisor.this.getDevicePolicyManager() != null) {
                            ActivityStackSupervisor.this.getDevicePolicyManager().notifyLockTaskModeChanged(true, (String) msg.obj, msg.arg1);
                            break;
                        }
                    } catch (RemoteException ex) {
                        throw new RuntimeException(ex);
                    }
                    break;
                case 110:
                    try {
                        if (ActivityStackSupervisor.this.getStatusBarService() != null) {
                            ActivityStackSupervisor.this.getStatusBarService().disable(0, ActivityStackSupervisor.this.mToken, ActivityStackSupervisor.this.mService.mContext.getPackageName());
                        }
                        ActivityStackSupervisor.this.mWindowManager.reenableKeyguard(ActivityStackSupervisor.this.mToken);
                        if (ActivityStackSupervisor.this.getDevicePolicyManager() != null) {
                            ActivityStackSupervisor.this.getDevicePolicyManager().notifyLockTaskModeChanged(false, null, msg.arg1);
                        }
                        if (ActivityStackSupervisor.this.mLockTaskNotify == null) {
                            ActivityStackSupervisor.this.mLockTaskNotify = new LockTaskNotify(ActivityStackSupervisor.this.mService.mContext);
                        }
                        ActivityStackSupervisor.this.mLockTaskNotify.show(false);
                        try {
                            boolean shouldLockKeyguard = Secure.getInt(ActivityStackSupervisor.this.mService.mContext.getContentResolver(), "lock_to_app_exit_locked") != 0;
                            if (ActivityStackSupervisor.this.mLockTaskModeState == 2 && shouldLockKeyguard) {
                                ActivityStackSupervisor.this.mWindowManager.lockNow(null);
                                ActivityStackSupervisor.this.mWindowManager.dismissKeyguard();
                                new LockPatternUtils(ActivityStackSupervisor.this.mService.mContext).requireCredentialEntry(-1);
                            }
                        } catch (SettingNotFoundException e2) {
                        }
                        ActivityStackSupervisor.this.mLockTaskModeState = 0;
                        break;
                    } catch (RemoteException ex2) {
                        throw new RuntimeException(ex2);
                    } catch (Throwable th) {
                        ActivityStackSupervisor.this.mLockTaskModeState = 0;
                    }
                case 111:
                    container = (ActivityContainer) msg.obj;
                    callback = container.mCallback;
                    if (callback != null) {
                        try {
                            callback.onAllActivitiesComplete(container.asBinder());
                            break;
                        } catch (RemoteException e3) {
                            break;
                        }
                    }
                    break;
                case 112:
                    synchronized (ActivityStackSupervisor.this.mService) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            ActivityRecord r = ActivityRecord.forTokenLocked((IBinder) msg.obj);
                            if (r != null) {
                                ActivityStackSupervisor.this.handleLaunchTaskBehindCompleteLocked(r);
                            }
                        } finally {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    break;
                case 113:
                    if (ActivityStackSupervisor.this.mLockTaskNotify == null) {
                        ActivityStackSupervisor.this.mLockTaskNotify = new LockTaskNotify(ActivityStackSupervisor.this.mService.mContext);
                    }
                    ActivityStackSupervisor.this.mLockTaskNotify.showToast(2);
                    break;
                case 114:
                    synchronized (ActivityStackSupervisor.this.mService) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            for (i = ActivityStackSupervisor.this.mMultiWindowModeChangedActivities.size() - 1; i >= 0; i--) {
                                ((ActivityRecord) ActivityStackSupervisor.this.mMultiWindowModeChangedActivities.remove(i)).scheduleMultiWindowModeChanged();
                            }
                        } finally {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    break;
                case 115:
                    synchronized (ActivityStackSupervisor.this.mService) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            for (i = ActivityStackSupervisor.this.mPipModeChangedActivities.size() - 1; i >= 0; i--) {
                                ((ActivityRecord) ActivityStackSupervisor.this.mPipModeChangedActivities.remove(i)).schedulePictureInPictureModeChanged();
                            }
                        } finally {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    break;
            }
        }
    }

    static class FindTaskResult {
        boolean matchedByRootAffinity;
        ActivityRecord r;

        FindTaskResult() {
        }
    }

    static class PendingActivityLaunch {
        final ProcessRecord callerApp;
        final ActivityRecord r;
        final ActivityRecord sourceRecord;
        final ActivityStack stack;
        final int startFlags;

        PendingActivityLaunch(ActivityRecord _r, ActivityRecord _sourceRecord, int _startFlags, ActivityStack _stack, ProcessRecord _callerApp) {
            this.r = _r;
            this.sourceRecord = _sourceRecord;
            this.startFlags = _startFlags;
            this.stack = _stack;
            this.callerApp = _callerApp;
        }

        void sendErrorResult(String message) {
            try {
                if (this.callerApp.thread != null) {
                    this.callerApp.thread.scheduleCrash(message);
                }
            } catch (RemoteException e) {
                Slog.e(ActivityStackSupervisor.TAG, "Exception scheduling crash of failed activity launcher sourceRecord=" + this.sourceRecord, e);
            }
        }
    }

    class SimpleActivityInfo {
        String activityName = null;
        int activityType = 0;
        String packageName = null;

        SimpleActivityInfo() {
        }
    }

    private class VirtualActivityContainer extends ActivityContainer {
        boolean mDrawn = false;
        Surface mSurface;

        VirtualActivityContainer(ActivityRecord parent, IActivityContainerCallback callback) {
            super(ActivityStackSupervisor.this.getNextStackId());
            this.mParentActivity = parent;
            this.mCallback = callback;
            this.mContainerState = 1;
            this.mIdString = "VirtualActivityContainer{" + this.mStackId + ", parent=" + this.mParentActivity + "}";
        }

        public void setSurface(Surface surface, int width, int height, int density) {
            super.setSurface(surface, width, height, density);
            synchronized (ActivityStackSupervisor.this.mService) {
                long origId;
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    origId = Binder.clearCallingIdentity();
                    setSurfaceLocked(surface, width, height, density);
                    Binder.restoreCallingIdentity(origId);
                } catch (Throwable th) {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
            ActivityManagerService.resetPriorityAfterLockedSection();
        }

        private void setSurfaceLocked(Surface surface, int width, int height, int density) {
            if (this.mContainerState != 2) {
                VirtualActivityDisplay virtualActivityDisplay = this.mActivityDisplay;
                if (virtualActivityDisplay == null) {
                    virtualActivityDisplay = new VirtualActivityDisplay(width, height, density);
                    this.mActivityDisplay = virtualActivityDisplay;
                    ActivityStackSupervisor.this.mActivityDisplays.put(virtualActivityDisplay.mDisplayId, virtualActivityDisplay);
                    attachToDisplayLocked(virtualActivityDisplay, true);
                }
                if (this.mSurface != null) {
                    this.mSurface.release();
                }
                this.mSurface = surface;
                if (surface != null) {
                    ActivityStackSupervisor.this.resumeFocusedStackTopActivityLocked();
                } else {
                    this.mContainerState = 1;
                    ((VirtualActivityDisplay) this.mActivityDisplay).setSurface(null);
                    if (this.mStack.mPausingActivity == null && this.mStack.mResumedActivity != null) {
                        this.mStack.startPausingLocked(false, true, null, false, "set-surface");
                    }
                }
                setSurfaceIfReadyLocked();
                if (ActivityManagerDebugConfig.DEBUG_STACK) {
                    Slog.d(ActivityStackSupervisor.TAG_STACK, "setSurface: " + this + " to display=" + virtualActivityDisplay);
                }
            }
        }

        boolean isAttachedLocked() {
            return this.mSurface != null ? super.isAttachedLocked() : false;
        }

        void setDrawn() {
            synchronized (ActivityStackSupervisor.this.mService) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    this.mDrawn = true;
                    setSurfaceIfReadyLocked();
                } finally {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        boolean isEligibleForNewTasks() {
            return false;
        }

        private void setSurfaceIfReadyLocked() {
            if (ActivityManagerDebugConfig.DEBUG_STACK) {
                Slog.v(ActivityStackSupervisor.TAG_STACK, "setSurfaceIfReadyLocked: mDrawn=" + this.mDrawn + " mContainerState=" + this.mContainerState + " mSurface=" + this.mSurface);
            }
            if (this.mDrawn && this.mSurface != null && this.mContainerState == 1) {
                ((VirtualActivityDisplay) this.mActivityDisplay).setSurface(this.mSurface);
                this.mContainerState = 0;
            }
        }
    }

    class VirtualActivityDisplay extends ActivityDisplay {
        VirtualDisplay mVirtualDisplay;

        VirtualActivityDisplay(int width, int height, int density) {
            super();
            this.mVirtualDisplay = DisplayManagerGlobal.getInstance().createVirtualDisplay(ActivityStackSupervisor.this.mService.mContext, null, ActivityStackSupervisor.VIRTUAL_DISPLAY_BASE_NAME, width, height, density, null, 9, null, null);
            init(this.mVirtualDisplay.getDisplay());
            ActivityStackSupervisor.this.mWindowManager.handleDisplayAdded(this.mDisplayId);
        }

        void setSurface(Surface surface) {
            if (this.mVirtualDisplay != null) {
                this.mVirtualDisplay.setSurface(surface);
            }
        }

        void detachActivitiesLocked(ActivityStack stack) {
            super.detachActivitiesLocked(stack);
            if (this.mVirtualDisplay != null) {
                this.mVirtualDisplay.release();
                this.mVirtualDisplay = null;
            }
        }

        public String toString() {
            return "VirtualActivityDisplay={" + this.mDisplayId + "}";
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.am.ActivityStackSupervisor.<clinit>():void, dex: 
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
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.am.ActivityStackSupervisor.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.am.ActivityStackSupervisor.<clinit>():void");
    }

    public ActivityStackSupervisor(ActivityManagerService service) {
        this.mPerfService = new PerfServiceWrapper(null);
        this.mToken = new Binder();
        this.mNextFreeStackId = 5;
        this.mCurTaskIdForUser = new SparseIntArray(20);
        this.mWaitingVisibleActivities = new ArrayList();
        this.mWaitingActivityVisible = new ArrayList();
        this.mWaitingActivityLaunched = new ArrayList();
        this.mStoppingActivities = new ArrayList();
        this.mFinishingActivities = new ArrayList();
        this.mGoingToSleepActivities = new ArrayList();
        this.mMultiWindowModeChangedActivities = new ArrayList();
        this.mPipModeChangedActivities = new ArrayList();
        this.mStartingUsers = new ArrayList();
        this.mUserLeaving = false;
        this.mSleepTimeout = false;
        this.mUserStackInFront = new SparseIntArray(2);
        this.mActivityContainers = new SparseArray();
        this.mActivityDisplays = new SparseArray();
        this.mLockTaskModeTasks = new ArrayList();
        this.tempRect = new Rect();
        this.tempRect2 = new Rect();
        this.mTmpConfigs = new SparseArray();
        this.mTmpBounds = new SparseArray();
        this.mTmpInsetBounds = new SparseArray();
        this.mDefaultMinSizeOfResizeableTask = -1;
        this.mOppoCustomMinSizeOfResizeableTask = -1;
        this.mTaskLayersChanged = true;
        this.mTmpFindTaskResult = new FindTaskResult();
        this.mResizingTasksDuringAnimation = new ArraySet();
        this.mAllowDockedStackResize = true;
        this.mOppoSecureProtectUtils = new OppoSecureProtectUtils();
        this.mLastResumedActivity = new SimpleActivityInfo();
        this.ACTION_OPPO_SAFE_COUNT_START_URL = "android.intent.action.OPPO_SAFE_COUNT_START_URL";
        this.mService = service;
        this.mHandler = new ActivityStackSupervisorHandler(this.mService.mHandler.getLooper());
        this.mActivityMetricsLogger = new ActivityMetricsLogger(this, this.mService.mContext);
        this.mResizeDockedStackTimeout = new ResizeDockedStackTimeout(service, this, this.mHandler);
    }

    void setRecentTasks(RecentTasks recentTasks) {
        this.mRecentTasks = recentTasks;
    }

    void initPowerManagement() {
        PowerManager pm = (PowerManager) this.mService.mContext.getSystemService("power");
        this.mGoingToSleep = pm.newWakeLock(1, "ActivityManager-Sleep");
        this.mLaunchingActivity = pm.newWakeLock(1, "*launch*");
        this.mLaunchingActivity.setReferenceCounted(false);
    }

    private IStatusBarService getStatusBarService() {
        IStatusBarService iStatusBarService;
        synchronized (this.mService) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                if (this.mStatusBarService == null) {
                    this.mStatusBarService = IStatusBarService.Stub.asInterface(ServiceManager.checkService("statusbar"));
                    if (this.mStatusBarService == null) {
                        Slog.w("StatusBarManager", "warning: no STATUS_BAR_SERVICE");
                    }
                }
                iStatusBarService = this.mStatusBarService;
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
        return iStatusBarService;
    }

    private IDevicePolicyManager getDevicePolicyManager() {
        IDevicePolicyManager iDevicePolicyManager;
        synchronized (this.mService) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                if (this.mDevicePolicyManager == null) {
                    this.mDevicePolicyManager = IDevicePolicyManager.Stub.asInterface(ServiceManager.checkService("device_policy"));
                    if (this.mDevicePolicyManager == null) {
                        Slog.w(TAG, "warning: no DEVICE_POLICY_SERVICE");
                    }
                }
                iDevicePolicyManager = this.mDevicePolicyManager;
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
        return iDevicePolicyManager;
    }

    void setWindowManager(WindowManagerService wm) {
        synchronized (this.mService) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                this.mWindowManager = wm;
                this.mDisplayManager = (DisplayManager) this.mService.mContext.getSystemService("display");
                this.mDisplayManager.registerDisplayListener(this, null);
                Display[] displays = this.mDisplayManager.getDisplays();
                for (int displayNdx = displays.length - 1; displayNdx >= 0; displayNdx--) {
                    int displayId = displays[displayNdx].getDisplayId();
                    ActivityDisplay activityDisplay = new ActivityDisplay(displayId);
                    if (activityDisplay.mDisplay == null) {
                        throw new IllegalStateException("Default Display does not exist");
                    }
                    this.mActivityDisplays.put(displayId, activityDisplay);
                    calculateDefaultMinimalSizeOfResizeableTasks(activityDisplay);
                }
                ActivityStack stack = getStack(0, true, true);
                this.mLastFocusedStack = stack;
                this.mFocusedStack = stack;
                this.mHomeStack = stack;
                this.mInputManagerInternal = (InputManagerInternal) LocalServices.getService(InputManagerInternal.class);
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    void notifyActivityDrawnForKeyguard() {
        if (ActivityManagerDebugConfig.DEBUG_LOCKSCREEN) {
            this.mService.logLockScreen(IElsaManager.EMPTY_PACKAGE);
        }
        this.mWindowManager.notifyActivityDrawnForKeyguard();
    }

    ActivityStack getFocusedStack() {
        return this.mFocusedStack;
    }

    ActivityStack getLastStack() {
        return this.mLastFocusedStack;
    }

    boolean isFocusedStack(ActivityStack stack) {
        boolean z = false;
        if (stack == null) {
            return false;
        }
        ActivityRecord parent = stack.mActivityContainer.mParentActivity;
        if (parent != null) {
            stack = parent.task.stack;
        }
        if (stack == this.mFocusedStack) {
            z = true;
        }
        return z;
    }

    boolean isFrontStack(ActivityStack stack) {
        boolean z = false;
        if (stack == null) {
            return false;
        }
        ActivityRecord parent = stack.mActivityContainer.mParentActivity;
        if (parent != null) {
            stack = parent.task.stack;
        }
        if (stack == this.mHomeStack.mStacks.get(this.mHomeStack.mStacks.size() - 1)) {
            z = true;
        }
        return z;
    }

    void setFocusStackUnchecked(String reason, ActivityStack focusCandidate) {
        int i = -1;
        if (!focusCandidate.isFocusable()) {
            focusCandidate = focusCandidate.getNextFocusableStackLocked();
        }
        if (focusCandidate != this.mFocusedStack) {
            this.mLastFocusedStack = this.mFocusedStack;
            this.mFocusedStack = focusCandidate;
            int i2 = this.mCurrentUser;
            int stackId = this.mFocusedStack == null ? -1 : this.mFocusedStack.getStackId();
            if (this.mLastFocusedStack != null) {
                i = this.mLastFocusedStack.getStackId();
            }
            EventLogTags.writeAmFocusedStack(i2, stackId, i, reason);
        }
        ActivityRecord r = topRunningActivityLocked();
        if (!(this.mService.mDoingSetFocusedActivity || this.mService.mFocusedActivity == r)) {
            this.mService.setFocusedActivityLocked(r, reason + " setFocusStack");
        }
        if ((this.mService.mBooting || !this.mService.mBooted) && r != null && r.idle) {
            checkFinishBootingLocked();
        }
    }

    void moveHomeStackToFront(String reason) {
        this.mHomeStack.moveToFront(reason);
    }

    boolean moveHomeStackTaskToTop(int homeStackTaskType, String reason) {
        if (homeStackTaskType == 2) {
            this.mWindowManager.showRecentApps(false);
            return false;
        }
        this.mHomeStack.moveHomeStackTaskToTop(homeStackTaskType);
        ActivityRecord top = getHomeActivity();
        if (top == null) {
            return false;
        }
        this.mService.setFocusedActivityLocked(top, reason);
        return true;
    }

    boolean resumeHomeStackTask(int homeStackTaskType, ActivityRecord prev, String reason) {
        if (!this.mService.mBooting && !this.mService.mBooted) {
            return false;
        }
        if (homeStackTaskType == 2) {
            this.mWindowManager.showRecentApps(false);
            return false;
        }
        if (prev != null) {
            prev.task.setTaskToReturnTo(0);
        }
        this.mHomeStack.moveHomeStackTaskToTop(homeStackTaskType);
        ActivityRecord r = getHomeActivity();
        String myReason = reason + " resumeHomeStackTask";
        if (r == null || r.finishing) {
            return this.mService.startHomeActivityLocked(this.mCurrentUser, myReason);
        }
        this.mService.setFocusedActivityLocked(r, myReason);
        return resumeFocusedStackTopActivityLocked(this.mHomeStack, prev, null);
    }

    TaskRecord anyTaskForIdLocked(int id) {
        return anyTaskForIdLocked(id, true, -1);
    }

    TaskRecord anyTaskForIdLocked(int id, boolean restoreFromRecents, int stackId) {
        TaskRecord task;
        int numDisplays = this.mActivityDisplays.size();
        for (int displayNdx = 0; displayNdx < numDisplays; displayNdx++) {
            ArrayList<ActivityStack> stacks = ((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks;
            for (int stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
                task = ((ActivityStack) stacks.get(stackNdx)).taskForIdLocked(id);
                if (task != null) {
                    return task;
                }
            }
        }
        if (ActivityManagerDebugConfig.DEBUG_RECENTS) {
            Slog.v(TAG_RECENTS, "Looking for task id=" + id + " in recents");
        }
        task = this.mRecentTasks.taskForIdLocked(id);
        if (task == null) {
            if (ActivityManagerDebugConfig.DEBUG_RECENTS) {
                Slog.d(TAG_RECENTS, "\tDidn't find task id=" + id + " in recents");
            }
            return null;
        } else if (!restoreFromRecents) {
            return task;
        } else {
            if (restoreRecentTaskLocked(task, stackId)) {
                if (ActivityManagerDebugConfig.DEBUG_RECENTS) {
                    Slog.w(TAG_RECENTS, "Restored task id=" + id + " from in recents");
                }
                return task;
            }
            if (ActivityManagerDebugConfig.DEBUG_RECENTS) {
                Slog.w(TAG_RECENTS, "Couldn't restore task id=" + id + " found in recents");
            }
            return null;
        }
    }

    ActivityRecord isInAnyStackLocked(IBinder token) {
        int numDisplays = this.mActivityDisplays.size();
        for (int displayNdx = 0; displayNdx < numDisplays; displayNdx++) {
            ArrayList<ActivityStack> stacks = ((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks;
            for (int stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
                ActivityRecord r = ((ActivityStack) stacks.get(stackNdx)).isInStackLocked(token);
                if (r != null) {
                    return r;
                }
            }
        }
        return null;
    }

    boolean isUserLockedProfile(int userId) {
        if (!this.mService.mUserController.shouldConfirmCredentials(userId)) {
            return false;
        }
        ActivityStack[] activityStacks = new ActivityStack[3];
        activityStacks[0] = getStack(3);
        activityStacks[1] = getStack(2);
        activityStacks[2] = getStack(1);
        for (ActivityStack activityStack : activityStacks) {
            if (!(activityStack == null || activityStack.topRunningActivityLocked() == null || activityStack.getStackVisibilityLocked(null) == 0 || (activityStack.isDockedStack() && this.mIsDockMinimized))) {
                if (activityStack.mStackId == 2) {
                    List<TaskRecord> tasks = activityStack.getAllTasks();
                    int size = tasks.size();
                    for (int i = 0; i < size; i++) {
                        if (taskContainsActivityFromUser((TaskRecord) tasks.get(i), userId)) {
                            return true;
                        }
                    }
                    continue;
                } else {
                    TaskRecord topTask = activityStack.topTask();
                    if (topTask != null && taskContainsActivityFromUser(topTask, userId)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean taskContainsActivityFromUser(TaskRecord task, int userId) {
        for (int i = task.mActivities.size() - 1; i >= 0; i--) {
            if (((ActivityRecord) task.mActivities.get(i)).userId == userId) {
                return true;
            }
        }
        return false;
    }

    void setNextTaskIdForUserLocked(int taskId, int userId) {
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

    int getNextTaskIdForUserLocked(int userId) {
        int currentTaskId = this.mCurTaskIdForUser.get(userId, MAX_TASK_IDS_PER_USER * userId);
        int candidateTaskId = nextTaskIdForUser(currentTaskId, userId);
        do {
            if (this.mRecentTasks.taskIdTakenForUserLocked(candidateTaskId, userId) || anyTaskForIdLocked(candidateTaskId, false, -1) != null) {
                candidateTaskId = nextTaskIdForUser(candidateTaskId, userId);
            } else {
                this.mCurTaskIdForUser.put(userId, candidateTaskId);
                return candidateTaskId;
            }
        } while (candidateTaskId != currentTaskId);
        throw new IllegalStateException("Cannot get an available task id. Reached limit of 100000 running tasks per user.");
    }

    ActivityRecord resumedAppLocked() {
        ActivityStack stack = this.mFocusedStack;
        if (stack == null) {
            return null;
        }
        ActivityRecord resumedActivity = stack.mResumedActivity;
        if (resumedActivity == null || resumedActivity.app == null) {
            resumedActivity = stack.mPausingActivity;
            if (resumedActivity == null || resumedActivity.app == null) {
                resumedActivity = stack.topRunningActivityLocked();
            }
        }
        return resumedActivity;
    }

    boolean attachApplicationLocked(ProcessRecord app) throws RemoteException {
        String processName = app.processName;
        boolean didSomething = false;
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            ArrayList<ActivityStack> stacks = ((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks;
            for (int stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
                ActivityStack stack = (ActivityStack) stacks.get(stackNdx);
                if (isFocusedStack(stack)) {
                    ActivityRecord hr = stack.topRunningActivityLocked();
                    if (hr != null && hr.app == null && app.uid == hr.info.applicationInfo.uid && processName.equals(hr.processName)) {
                        try {
                            if (realStartActivityLocked(hr, app, true, true)) {
                                didSomething = true;
                            }
                        } catch (RemoteException e) {
                            Slog.w(TAG, "Exception in new application when starting activity " + hr.intent.getComponent().flattenToShortString(), e);
                            throw e;
                        }
                    }
                }
            }
        }
        if (!didSomething) {
            ensureActivitiesVisibleLocked(null, 0, false);
        }
        return didSomething;
    }

    boolean allResumedActivitiesIdle() {
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            ArrayList<ActivityStack> stacks = ((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks;
            for (int stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
                ActivityStack stack = (ActivityStack) stacks.get(stackNdx);
                if (isFocusedStack(stack) && stack.numActivities() != 0) {
                    ActivityRecord resumedActivity = stack.mResumedActivity;
                    if (resumedActivity == null || !resumedActivity.idle) {
                        if (ActivityManagerDebugConfig.DEBUG_STATES) {
                            Slog.d(TAG_STATES, "allResumedActivitiesIdle: stack=" + stack.mStackId + " " + resumedActivity + " not idle");
                        }
                        return false;
                    }
                }
            }
        }
        this.mService.mActivityStarter.sendPowerHintForLaunchEndIfNeeded();
        return true;
    }

    boolean allResumedActivitiesComplete() {
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            ArrayList<ActivityStack> stacks = ((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks;
            for (int stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
                ActivityStack stack = (ActivityStack) stacks.get(stackNdx);
                if (isFocusedStack(stack)) {
                    ActivityRecord r = stack.mResumedActivity;
                    if (!(r == null || r.state == ActivityState.RESUMED)) {
                        return false;
                    }
                }
            }
        }
        if (ActivityManagerDebugConfig.DEBUG_STACK) {
            Slog.d(TAG_STACK, "allResumedActivitiesComplete: mLastFocusedStack changing from=" + this.mLastFocusedStack + " to=" + this.mFocusedStack);
        }
        this.mLastFocusedStack = this.mFocusedStack;
        return true;
    }

    boolean allResumedActivitiesVisible() {
        boolean foundResumed = false;
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            ArrayList<ActivityStack> stacks = ((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks;
            for (int stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
                ActivityRecord r = ((ActivityStack) stacks.get(stackNdx)).mResumedActivity;
                if (r != null) {
                    if (!r.nowVisible || this.mWaitingVisibleActivities.contains(r)) {
                        return false;
                    }
                    foundResumed = true;
                }
            }
        }
        return foundResumed;
    }

    boolean pauseBackStacks(boolean userLeaving, ActivityRecord resuming, boolean dontWait) {
        boolean someActivityPaused = false;
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            ArrayList<ActivityStack> stacks = ((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks;
            for (int stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
                ActivityStack stack = (ActivityStack) stacks.get(stackNdx);
                if (!(isFocusedStack(stack) || stack.mResumedActivity == null)) {
                    if (ActivityManagerDebugConfig.DEBUG_STATES) {
                        Slog.d(TAG_STATES, "pauseBackStacks: stack=" + stack + " mResumedActivity=" + stack.mResumedActivity);
                    }
                    someActivityPaused |= stack.startPausingLocked(userLeaving, false, resuming, dontWait, "pause-stack");
                }
            }
        }
        return someActivityPaused;
    }

    boolean allPausedActivitiesComplete() {
        boolean pausing = true;
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            ArrayList<ActivityStack> stacks = ((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks;
            for (int stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
                ActivityRecord r = ((ActivityStack) stacks.get(stackNdx)).mPausingActivity;
                if (!(r == null || r.state == ActivityState.PAUSED || r.state == ActivityState.STOPPED || r.state == ActivityState.STOPPING)) {
                    if (!ActivityManagerDebugConfig.DEBUG_STATES) {
                        return false;
                    }
                    Slog.d(TAG_STATES, "allPausedActivitiesComplete: r=" + r + " state=" + r.state);
                    pausing = false;
                }
            }
        }
        return pausing;
    }

    void pauseChildStacks(ActivityRecord parent, boolean userLeaving, boolean uiSleeping, ActivityRecord resuming, boolean dontWait) {
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            ArrayList<ActivityStack> stacks = ((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks;
            for (int stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
                ActivityStack stack = (ActivityStack) stacks.get(stackNdx);
                if (stack.mResumedActivity != null && stack.mActivityContainer.mParentActivity == parent) {
                    stack.startPausingLocked(userLeaving, uiSleeping, resuming, dontWait, "pause-child");
                }
            }
        }
    }

    void cancelInitializingActivities() {
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            ArrayList<ActivityStack> stacks = ((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks;
            for (int stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
                ((ActivityStack) stacks.get(stackNdx)).cancelInitializingActivities();
            }
        }
    }

    void reportActivityVisibleLocked(ActivityRecord r) {
        sendWaitingVisibleReportLocked(r);
    }

    void sendWaitingVisibleReportLocked(ActivityRecord r) {
        boolean changed = false;
        for (int i = this.mWaitingActivityVisible.size() - 1; i >= 0; i--) {
            WaitResult w = (WaitResult) this.mWaitingActivityVisible.get(i);
            if (w.who == null) {
                changed = true;
                w.timeout = false;
                if (r != null) {
                    w.who = new ComponentName(r.info.packageName, r.info.name);
                }
                w.totalTime = SystemClock.uptimeMillis() - w.thisTime;
                w.thisTime = w.totalTime;
            }
        }
        if (changed) {
            this.mService.notifyAll();
        }
    }

    void reportTaskToFrontNoLaunch(ActivityRecord r) {
        boolean changed = false;
        for (int i = this.mWaitingActivityLaunched.size() - 1; i >= 0; i--) {
            WaitResult w = (WaitResult) this.mWaitingActivityLaunched.remove(i);
            if (w.who == null) {
                changed = true;
                w.result = 2;
            }
        }
        if (changed) {
            this.mService.notifyAll();
        }
    }

    void reportActivityLaunchedLocked(boolean timeout, ActivityRecord r, long thisTime, long totalTime) {
        boolean changed = false;
        for (int i = this.mWaitingActivityLaunched.size() - 1; i >= 0; i--) {
            WaitResult w = (WaitResult) this.mWaitingActivityLaunched.remove(i);
            if (w.who == null) {
                changed = true;
                w.timeout = timeout;
                if (r != null) {
                    w.who = new ComponentName(r.info.packageName, r.info.name);
                }
                w.thisTime = thisTime;
                w.totalTime = totalTime;
            }
        }
        if (changed) {
            this.mService.notifyAll();
        }
    }

    ActivityRecord topRunningActivityLocked() {
        ActivityStack focusedStack = this.mFocusedStack;
        ActivityRecord r = focusedStack.topRunningActivityLocked();
        if (r != null) {
            return r;
        }
        ArrayList<ActivityStack> stacks = this.mHomeStack.mStacks;
        for (int stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
            ActivityStack stack = (ActivityStack) stacks.get(stackNdx);
            if (stack != focusedStack && isFrontStack(stack) && stack.isFocusable()) {
                r = stack.topRunningActivityLocked();
                if (r != null) {
                    return r;
                }
            }
        }
        return null;
    }

    void getTasksLocked(int maxNum, List<RunningTaskInfo> list, int callingUid, boolean allowed) {
        int stackNdx;
        ArrayList<RunningTaskInfo> stackTaskList;
        if (ActivityManagerDebugConfig.DEBUG_TASKS) {
            Slog.v(TAG, "getTasksLocked maxNum " + maxNum + " callingUid " + callingUid + " allowed " + allowed);
        }
        ArrayList<ArrayList<RunningTaskInfo>> runningTaskLists = new ArrayList();
        int numDisplays = this.mActivityDisplays.size();
        for (int displayNdx = 0; displayNdx < numDisplays; displayNdx++) {
            ArrayList<ActivityStack> stacks = ((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks;
            for (stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
                ActivityStack stack = (ActivityStack) stacks.get(stackNdx);
                if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                    Slog.v(TAG, "stack " + stack);
                }
                stackTaskList = new ArrayList();
                runningTaskLists.add(stackTaskList);
                stack.getTasksLocked(stackTaskList, callingUid, allowed);
            }
        }
        while (maxNum > 0) {
            long mostRecentActiveTime = Long.MIN_VALUE;
            ArrayList selectedStackList = null;
            int numTaskLists = runningTaskLists.size();
            for (stackNdx = 0; stackNdx < numTaskLists; stackNdx++) {
                stackTaskList = (ArrayList) runningTaskLists.get(stackNdx);
                if (!stackTaskList.isEmpty()) {
                    long lastActiveTime = ((RunningTaskInfo) stackTaskList.get(0)).lastActiveTime;
                    if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                        String str = TAG;
                        String str2 = str;
                        Slog.v(str2, "topActivity " + ((RunningTaskInfo) stackTaskList.get(0)).topActivity + " lastActiveTime " + lastActiveTime);
                    }
                    if (lastActiveTime > mostRecentActiveTime) {
                        mostRecentActiveTime = lastActiveTime;
                        selectedStackList = stackTaskList;
                    }
                }
            }
            if (selectedStackList != null) {
                list.add((RunningTaskInfo) selectedStackList.remove(0));
                maxNum--;
            } else {
                return;
            }
        }
    }

    ActivityInfo resolveActivity(Intent intent, ResolveInfo rInfo, int startFlags, ProfilerInfo profilerInfo) {
        ActivityInfo aInfo = null;
        if (rInfo != null) {
            aInfo = rInfo.activityInfo;
        }
        if (aInfo != null) {
            intent.setComponent(new ComponentName(aInfo.applicationInfo.packageName, aInfo.name));
            if (!aInfo.processName.equals("system")) {
                if ((startFlags & 2) != 0) {
                    this.mService.setDebugApp(aInfo.processName, true, false);
                }
                if ((startFlags & 8) != 0) {
                    this.mService.setNativeDebuggingAppLocked(aInfo.applicationInfo, aInfo.processName);
                }
                if ((startFlags & 4) != 0) {
                    this.mService.setTrackAllocationApp(aInfo.applicationInfo, aInfo.processName);
                }
                if (profilerInfo != null) {
                    this.mService.setProfileApp(aInfo.applicationInfo, aInfo.processName, profilerInfo);
                }
            }
        }
        return aInfo;
    }

    ResolveInfo resolveIntent(Intent intent, String resolvedType, int userId) {
        return resolveIntent(intent, resolvedType, userId, 0);
    }

    ResolveInfo resolveIntent(Intent intent, String resolvedType, int userId, int flags) {
        try {
            return AppGlobals.getPackageManager().resolveIntent(intent, resolvedType, (DumpState.DUMP_INSTALLS | flags) | 1024, userId);
        } catch (RemoteException e) {
            return null;
        }
    }

    ActivityInfo resolveActivity(Intent intent, String resolvedType, int startFlags, ProfilerInfo profilerInfo, int userId) {
        return resolveActivity(intent, resolveIntent(intent, resolvedType, userId), startFlags, profilerInfo);
    }

    final boolean realStartActivityLocked(ActivityRecord r, ProcessRecord app, boolean andResume, boolean checkConfig) throws RemoteException {
        if (allPausedActivitiesComplete()) {
            if (andResume) {
                r.startFreezingScreenLocked(app, 0);
                this.mWindowManager.setAppVisibility(r.appToken, true);
                r.startLaunchTickingLocked();
            }
            if (checkConfig) {
                this.mService.updateConfigurationLocked(this.mWindowManager.updateOrientationFromAppTokens(this.mService.mConfiguration, r.mayFreezeScreenLocked(app) ? r.appToken : null), r, false, true);
            }
            r.app = app;
            app.waitingToKill = null;
            r.launchCount++;
            r.lastLaunchTime = SystemClock.uptimeMillis();
            if (ActivityManagerDebugConfig.DEBUG_ALL) {
                Slog.v(TAG, "ACT-Launching: " + r);
            }
            if (app.activities.indexOf(r) < 0) {
                app.activities.add(r);
            }
            this.mService.updateLruProcessLocked(app, true, null);
            this.mService.updateOomAdjLocked();
            TaskRecord task = r.task;
            if (task.mLockTaskAuth == 2 || task.mLockTaskAuth == 4) {
                setLockTaskModeLocked(task, 1, "mLockTaskAuth==LAUNCHABLE", false);
            }
            ActivityStack stack = task.stack;
            try {
                if (app.thread == null) {
                    throw new RemoteException();
                }
                Object[] objArr;
                List results = null;
                List newIntents = null;
                if (andResume) {
                    results = r.results;
                    newIntents = r.newIntents;
                }
                if (ActivityManagerDebugConfig.DEBUG_SWITCH) {
                    Slog.v(TAG_SWITCH, "Launching: " + r + " icicle=" + r.icicle + " with results=" + results + " newIntents=" + newIntents + " andResume=" + andResume);
                }
                if (andResume) {
                    objArr = new Object[4];
                    objArr[0] = Integer.valueOf(r.userId);
                    objArr[1] = Integer.valueOf(System.identityHashCode(r));
                    objArr[2] = Integer.valueOf(task.taskId);
                    objArr[3] = r.shortComponentName;
                    EventLog.writeEvent(EventLogTags.AM_RESTART_ACTIVITY, objArr);
                    if (!ActivityManagerService.IS_USER_BUILD || ActivityManagerDebugConfig.DEBUG_STACK) {
                        Slog.d(TAG, "ACT-AM_RESTART_ACTIVITY " + r + " Task:" + r.task.taskId);
                    }
                }
                if (r.isHomeActivity()) {
                    this.mService.mHomeProcess = ((ActivityRecord) task.mActivities.get(0)).app;
                }
                this.mService.notifyPackageUse(r.intent.getComponent().getPackageName(), 0);
                r.sleeping = false;
                r.forceNewConfig = false;
                this.mService.showUnsupportedZoomDialogIfNeededLocked(r);
                this.mService.showAskCompatModeDialogLocked(r);
                r.compat = this.mService.compatibilityInfoForPackageLocked(r.info.applicationInfo);
                ProfilerInfo profilerInfo = null;
                if (this.mService.mProfileApp != null && this.mService.mProfileApp.equals(app.processName) && (this.mService.mProfileProc == null || this.mService.mProfileProc == app)) {
                    this.mService.mProfileProc = app;
                    String profileFile = this.mService.mProfileFile;
                    if (profileFile != null) {
                        ParcelFileDescriptor profileFd = this.mService.mProfileFd;
                        if (profileFd != null) {
                            try {
                                profileFd = profileFd.dup();
                            } catch (IOException e) {
                                if (profileFd != null) {
                                    try {
                                        profileFd.close();
                                    } catch (IOException e2) {
                                    }
                                    profileFd = null;
                                }
                            }
                        }
                        ProfilerInfo profilerInfo2 = new ProfilerInfo(profileFile, profileFd, this.mService.mSamplingInterval, this.mService.mAutoStopProfiler);
                    }
                }
                if (andResume) {
                    app.hasShownUi = true;
                    app.pendingUiClean = true;
                }
                app.forceProcessStateUpTo(this.mService.mTopProcessState);
                if (!(r.intent == null || r.intent.getComponent() == null)) {
                    startActivityBoost(r.intent.getComponent().getClassName());
                }
                app.thread.scheduleLaunchActivity(new Intent(r.intent), r.appToken, System.identityHashCode(r), r.info, new Configuration(this.mService.mConfiguration), new Configuration(task.mOverrideConfig), r.compat, r.launchedFromPackage, task.voiceInteractor, app.repProcState, r.icicle, r.persistentState, results, newIntents, !andResume, this.mService.isNextTransitionForward(), profilerInfo);
                if ((app.info.privateFlags & 2) != 0 && app.processName.equals(app.info.packageName)) {
                    if (!(this.mService.mHeavyWeightProcess == null || this.mService.mHeavyWeightProcess == app)) {
                        Slog.w(TAG, "Starting new heavy weight process " + app + " when already running " + this.mService.mHeavyWeightProcess);
                    }
                    this.mService.mHeavyWeightProcess = app;
                    Message msg = this.mService.mHandler.obtainMessage(24);
                    msg.obj = r;
                    this.mService.mHandler.sendMessage(msg);
                }
                r.launchFailed = false;
                if (stack.updateLRUListLocked(r)) {
                    Slog.w(TAG, "Activity " + r + " being launched, but already in LRU list");
                }
                if (andResume) {
                    stack.minimalResumeActivityLocked(r);
                } else {
                    if (ActivityManagerDebugConfig.DEBUG_STATES) {
                        Slog.v(TAG_STATES, "Moving to PAUSED: " + r + " (starting in paused state)");
                    }
                    r.state = ActivityState.PAUSED;
                    ProcessRecord appProc = r.app;
                    if (appProc != null) {
                        AfterActivityPaused eventData = AfterActivityPaused.createInstance();
                        objArr = new Object[3];
                        objArr[0] = Integer.valueOf(appProc.pid);
                        objArr[1] = r.info.name;
                        objArr[2] = r.info.packageName;
                        eventData.set(objArr);
                        this.mService.getAMEventHook().hook(Event.AM_AfterActivityPaused, eventData);
                    }
                }
                if (isFocusedStack(stack)) {
                    this.mService.startSetupActivityLocked();
                }
                try {
                    if (r.app != null) {
                        this.mService.mServices.updateServiceConnectionActivitiesLocked(r.app);
                    }
                } catch (Throwable e3) {
                    Slog.w(TAG, "updateServiceConnectionActivitiesLocked catch NullPointerException", e3);
                }
                return true;
            } catch (Throwable e4) {
                if (r.launchFailed) {
                    Slog.e(TAG, "Second failure launching " + r.intent.getComponent().flattenToShortString() + ", giving up", e4);
                    this.mService.appDiedLocked(app);
                    stack.requestFinishActivityLocked(r.appToken, 0, null, "2nd-crash", false);
                    return false;
                }
                app.activities.remove(r);
                throw e4;
            }
        }
        if (ActivityManagerDebugConfig.DEBUG_SWITCH || ActivityManagerDebugConfig.DEBUG_PAUSE || ActivityManagerDebugConfig.DEBUG_STATES) {
            Slog.v(TAG_PAUSE, "realStartActivityLocked: Skipping start of r=" + r + " some activities pausing...");
        }
        return false;
    }

    void startSpecificActivityLocked(ActivityRecord r, boolean andResume, boolean checkConfig) {
        ProcessRecord app = this.mService.getProcessRecordLocked(r.processName, r.info.applicationInfo.uid, true);
        r.task.stack.setLaunchTime(r);
        if (!(app == null || app.thread == null)) {
            try {
                if ((r.info.flags & 1) == 0 || !"android".equals(r.info.packageName)) {
                    app.addPackage(r.info.packageName, r.info.applicationInfo.versionCode, this.mService.mProcessStats);
                }
                realStartActivityLocked(r, app, andResume, checkConfig);
                return;
            } catch (Throwable e) {
                Slog.w(TAG, "Exception when starting activity " + r.intent.getComponent().flattenToShortString(), e);
            }
        }
        this.mService.startProcessLocked(r.processName, r.info.applicationInfo, true, 0, "activity", r.intent.getComponent(), false, false, true, r);
    }

    boolean checkStartAnyActivityPermission(Intent intent, ActivityInfo aInfo, String resultWho, int requestCode, int callingPid, int callingUid, String callingPackage, boolean ignoreTargetSecurity, ProcessRecord callerApp, ActivityRecord resultRecord, ActivityStack resultStack, ActivityOptions options) {
        if (this.mService.checkPermission("android.permission.START_ANY_ACTIVITY", callingPid, callingUid) == 0) {
            return true;
        }
        int componentRestriction = getComponentRestrictionForCallingPackage(aInfo, callingPackage, callingPid, callingUid, ignoreTargetSecurity);
        int actionRestriction = getActionRestrictionForCallingPackage(intent.getAction(), callingPackage, callingPid, callingUid);
        String msg;
        if (componentRestriction == 1 || actionRestriction == 1) {
            if (resultRecord != null) {
                resultStack.sendActivityResultLocked(-1, resultRecord, resultWho, requestCode, 0, null);
            }
            if (actionRestriction == 1) {
                msg = "Permission Denial: starting " + intent.toString() + " from " + callerApp + " (pid=" + callingPid + ", uid=" + callingUid + ")" + " with revoked permission " + ((String) ACTION_TO_RUNTIME_PERMISSION.get(intent.getAction()));
            } else if (aInfo.exported) {
                msg = "Permission Denial: starting " + intent.toString() + " from " + callerApp + " (pid=" + callingPid + ", uid=" + callingUid + ")" + " requires " + aInfo.permission;
            } else {
                msg = "Permission Denial: starting " + intent.toString() + " from " + callerApp + " (pid=" + callingPid + ", uid=" + callingUid + ")" + " not exported from uid " + aInfo.applicationInfo.uid;
            }
            Slog.w(TAG, msg);
            throw new SecurityException(msg);
        } else if (actionRestriction == 2) {
            Slog.w(TAG, "Appop Denial: starting " + intent.toString() + " from " + callerApp + " (pid=" + callingPid + ", uid=" + callingUid + ")" + " requires " + AppOpsManager.permissionToOp((String) ACTION_TO_RUNTIME_PERMISSION.get(intent.getAction())));
            return false;
        } else if (componentRestriction == 2) {
            Slog.w(TAG, "Appop Denial: starting " + intent.toString() + " from " + callerApp + " (pid=" + callingPid + ", uid=" + callingUid + ")" + " requires appop " + AppOpsManager.permissionToOp(aInfo.permission));
            return false;
        } else if (options == null || options.getLaunchTaskId() == -1 || this.mService.checkPermission("android.permission.START_TASKS_FROM_RECENTS", callingPid, callingUid) == 0) {
            return true;
        } else {
            msg = "Permission Denial: starting " + intent.toString() + " from " + callerApp + " (pid=" + callingPid + ", uid=" + callingUid + ") with launchTaskId=" + options.getLaunchTaskId();
            Slog.w(TAG, msg);
            throw new SecurityException(msg);
        }
    }

    UserInfo getUserInfo(int userId) {
        long identity = Binder.clearCallingIdentity();
        try {
            UserInfo userInfo = UserManager.get(this.mService.mContext).getUserInfo(userId);
            return userInfo;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    private int getComponentRestrictionForCallingPackage(ActivityInfo activityInfo, String callingPackage, int callingPid, int callingUid, boolean ignoreTargetSecurity) {
        if (!ignoreTargetSecurity) {
            if (this.mService.checkComponentPermission(activityInfo.permission, callingPid, callingUid, activityInfo.applicationInfo.uid, activityInfo.exported) == -1) {
                return 1;
            }
        }
        if (activityInfo.permission == null) {
            return 0;
        }
        int opCode = AppOpsManager.permissionToOpCode(activityInfo.permission);
        if (opCode == -1 || this.mService.mAppOpsService.noteOperation(opCode, callingUid, callingPackage) == 0 || ignoreTargetSecurity) {
            return 0;
        }
        return 2;
    }

    void dataCollectionInfo(IApplicationThread caller, ProcessRecord callerApp, int pid, int uid, String callingPackage, Intent intent) {
        if (intent != null && intent.getComponent() != null) {
            ComponentName realActivity = intent.getComponent();
            String packageInstall = "com.android.packageinstaller.PackageInstallerActivity";
            String packageUninstall = "com.android.packageinstaller.UninstallerActivity";
            if (realActivity == null) {
                return;
            }
            if (packageInstall.equals(realActivity.getClassName()) || packageUninstall.equals(realActivity.getClassName())) {
                int i;
                String str = OppoPackageManagerHelper.OPPO_EXTRA_PID;
                if (callerApp != null) {
                    i = callerApp.pid;
                } else {
                    i = pid;
                }
                intent.putExtra(str, i);
                str = OppoPackageManagerHelper.OPPO_EXTRA_UID;
                if (callerApp != null) {
                    i = callerApp.info.uid;
                } else {
                    i = uid;
                }
                intent.putExtra(str, i);
                intent.putExtra("oppo_extra_pkg_name", callingPackage != null ? callingPackage : IElsaManager.EMPTY_PACKAGE);
                StringBuffer sb = new StringBuffer("dataCollection debug info ");
                if (caller == null) {
                    sb.append(" caller is null,");
                }
                if (callerApp == null) {
                    sb.append(" callerApp is null");
                }
                sb.append(" pid ");
                sb.append(pid);
                sb.append(" uid ");
                sb.append(uid);
                sb.append(" callingPackage ");
                sb.append(callingPackage);
                intent.putExtra(OppoPackageManagerHelper.OPPO_EXTRA_DEBUG_INFO, sb.toString());
            }
        }
    }

    private int getActionRestrictionForCallingPackage(String action, String callingPackage, int callingPid, int callingUid) {
        if (action == null) {
            return 0;
        }
        String permission = (String) ACTION_TO_RUNTIME_PERMISSION.get(action);
        if (permission == null) {
            return 0;
        }
        try {
            if (!ArrayUtils.contains(this.mService.mContext.getPackageManager().getPackageInfo(callingPackage, 4096).requestedPermissions, permission)) {
                return 0;
            }
            if (this.mService.checkPermission(permission, callingPid, callingUid) == -1) {
                return 1;
            }
            int opCode = AppOpsManager.permissionToOpCode(permission);
            if (opCode == -1 || this.mService.mAppOpsService.noteOperation(opCode, callingUid, callingPackage) == 0) {
                return 0;
            }
            return 2;
        } catch (NameNotFoundException e) {
            Slog.i(TAG, "Cannot find package info for " + callingPackage);
            return 0;
        }
    }

    boolean moveActivityStackToFront(ActivityRecord r, String reason) {
        if (r == null) {
            return false;
        }
        TaskRecord task = r.task;
        if (task == null || task.stack == null) {
            Slog.w(TAG, "Can't move stack to front for r=" + r + " task=" + task);
            return false;
        }
        task.stack.moveToFront(reason, task);
        return true;
    }

    void setLaunchSource(int uid) {
        this.mLaunchingActivity.setWorkSource(new WorkSource(uid));
    }

    void acquireLaunchWakelock() {
        this.mLaunchingActivity.acquire();
        if (!this.mHandler.hasMessages(104)) {
            this.mHandler.sendEmptyMessageDelayed(104, 10000);
        }
    }

    private boolean checkFinishBootingLocked() {
        boolean booting = this.mService.mBooting;
        boolean enableScreen = false;
        this.mService.mBooting = false;
        if (!this.mService.mBooted) {
            this.mService.mBooted = true;
            enableScreen = true;
        }
        if (booting || enableScreen) {
            this.mService.postFinishBooting(booting, enableScreen);
        }
        return booting;
    }

    final ActivityRecord activityIdleInternalLocked(IBinder token, boolean fromTimeout, Configuration config) {
        int i;
        ActivityStack stack;
        if (ActivityManagerDebugConfig.DEBUG_ALL) {
            Slog.v(TAG, "Activity idle: " + token);
        }
        ArrayList finishes = null;
        ArrayList startingUsers = null;
        boolean booting = false;
        int activityRemoved = 0;
        ActivityRecord r = ActivityRecord.forTokenLocked(token);
        if (r != null) {
            if (ActivityManagerDebugConfig.DEBUG_IDLE) {
                Slog.d(TAG_IDLE, "activityIdleInternalLocked: Callers=" + Debug.getCallers(4));
            }
            this.mHandler.removeMessages(100, r);
            r.finishLaunchTickingLocked();
            if (fromTimeout) {
                reportActivityLaunchedLocked(fromTimeout, r, -1, -1);
            }
            if (config != null) {
                r.configuration = config;
            }
            r.idle = true;
            if (isFocusedStack(r.task.stack) || fromTimeout) {
                booting = checkFinishBootingLocked();
            }
        }
        if (allResumedActivitiesIdle()) {
            if (r != null) {
                this.mService.scheduleAppGcsLocked();
            }
            if (this.mLaunchingActivity.isHeld()) {
                this.mHandler.removeMessages(104);
                Slog.d(TAG, "mLaunchingActivity.release() in activityIdleInternalLocked");
                this.mLaunchingActivity.release();
                if (this.mPerfService != null) {
                    this.mPerfService.amsBoostStop();
                }
            }
            ensureActivitiesVisibleLocked(null, 0, false);
        }
        ArrayList<ActivityRecord> stops = processStoppingActivitiesLocked(true);
        int NS = stops != null ? stops.size() : 0;
        int NF = this.mFinishingActivities.size();
        if (NF > 0) {
            finishes = new ArrayList(this.mFinishingActivities);
            this.mFinishingActivities.clear();
        }
        if (this.mStartingUsers.size() > 0) {
            ArrayList arrayList = new ArrayList(this.mStartingUsers);
            this.mStartingUsers.clear();
        }
        for (i = 0; i < NS; i++) {
            r = (ActivityRecord) stops.get(i);
            stack = r.task.stack;
            if (stack != null) {
                if (r.finishing) {
                    stack.finishCurrentActivityLocked(r, 0, false);
                } else {
                    stack.stopActivityLocked(r);
                }
            }
        }
        for (i = 0; i < NF; i++) {
            r = (ActivityRecord) finishes.get(i);
            stack = r.task.stack;
            if (stack != null) {
                activityRemoved |= stack.destroyActivityLocked(r, true, "finish-idle");
            }
        }
        if (!(booting || startingUsers == null)) {
            for (i = 0; i < startingUsers.size(); i++) {
                this.mService.mUserController.finishUserSwitch((UserState) startingUsers.get(i));
            }
        }
        this.mService.trimApplications();
        if (activityRemoved != 0) {
            resumeFocusedStackTopActivityLocked();
        }
        return r;
    }

    boolean handleAppDiedLocked(ProcessRecord app) {
        boolean hasVisibleActivities = false;
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            ArrayList<ActivityStack> stacks = ((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks;
            for (int stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
                hasVisibleActivities |= ((ActivityStack) stacks.get(stackNdx)).handleAppDiedLocked(app);
            }
        }
        return hasVisibleActivities;
    }

    void closeSystemDialogsLocked() {
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            ArrayList<ActivityStack> stacks = ((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks;
            for (int stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
                ((ActivityStack) stacks.get(stackNdx)).closeSystemDialogsLocked();
            }
        }
    }

    void removeUserLocked(int userId) {
        this.mUserStackInFront.delete(userId);
    }

    void updateUserStackLocked(int userId, ActivityStack stack) {
        if (userId != this.mCurrentUser) {
            this.mUserStackInFront.put(userId, stack != null ? stack.getStackId() : 0);
        }
    }

    boolean finishDisabledPackageActivitiesLocked(String packageName, Set<String> filterByClasses, boolean doit, boolean evenPersistent, int userId) {
        boolean didSomething = false;
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            ArrayList<ActivityStack> stacks = ((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks;
            for (int stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
                if (((ActivityStack) stacks.get(stackNdx)).finishDisabledPackageActivitiesLocked(packageName, filterByClasses, doit, evenPersistent, userId)) {
                    didSomething = true;
                }
            }
        }
        return didSomething;
    }

    void updatePreviousProcessLocked(ActivityRecord r) {
        ProcessRecord fgApp = null;
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            ArrayList<ActivityStack> stacks = ((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks;
            int stackNdx = stacks.size() - 1;
            while (stackNdx >= 0) {
                ActivityStack stack = (ActivityStack) stacks.get(stackNdx);
                if (isFocusedStack(stack)) {
                    if (stack.mResumedActivity != null) {
                        fgApp = stack.mResumedActivity.app;
                    } else if (stack.mPausingActivity != null) {
                        fgApp = stack.mPausingActivity.app;
                    }
                } else {
                    stackNdx--;
                }
            }
        }
        if (r.app != null && fgApp != null && r.app != fgApp && r.lastVisibleTime > this.mService.mPreviousProcessVisibleTime && r.app != this.mService.mHomeProcess) {
            this.mService.mPreviousProcess = r.app;
            this.mService.mPreviousProcessVisibleTime = r.lastVisibleTime;
        }
    }

    boolean resumeFocusedStackTopActivityLocked() {
        return resumeFocusedStackTopActivityLocked(null, null, null);
    }

    boolean resumeFocusedStackTopActivityLocked(ActivityStack targetStack, ActivityRecord target, ActivityOptions targetOptions) {
        if (targetStack != null && isFocusedStack(targetStack)) {
            return targetStack.resumeTopActivityUncheckedLocked(target, targetOptions);
        }
        ActivityRecord r = this.mFocusedStack.topRunningActivityLocked();
        if (r == null || r.state != ActivityState.RESUMED) {
            return this.mFocusedStack.resumeTopActivityUncheckedLocked(null, null);
        }
        return false;
    }

    void updateActivityApplicationInfoLocked(ApplicationInfo aInfo) {
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            ArrayList<ActivityStack> stacks = ((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks;
            for (int stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
                ((ActivityStack) stacks.get(stackNdx)).updateActivityApplicationInfoLocked(aInfo);
            }
        }
    }

    TaskRecord finishTopRunningActivityLocked(ProcessRecord app, String reason) {
        TaskRecord finishedTask = null;
        ActivityStack focusedStack = getFocusedStack();
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            ArrayList<ActivityStack> stacks = ((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks;
            int numStacks = stacks.size();
            for (int stackNdx = 0; stackNdx < numStacks; stackNdx++) {
                ActivityStack stack = (ActivityStack) stacks.get(stackNdx);
                TaskRecord t = stack.finishTopRunningActivityLocked(app, reason);
                if (stack == focusedStack || finishedTask == null) {
                    finishedTask = t;
                }
            }
        }
        return finishedTask;
    }

    void finishVoiceTask(IVoiceInteractionSession session) {
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            ArrayList<ActivityStack> stacks = ((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks;
            int numStacks = stacks.size();
            for (int stackNdx = 0; stackNdx < numStacks; stackNdx++) {
                ((ActivityStack) stacks.get(stackNdx)).finishVoiceTask(session);
            }
        }
    }

    void findTaskToMoveToFrontLocked(TaskRecord task, int flags, ActivityOptions options, String reason, boolean forceNonResizeable) {
        ActivityRecord top_activity = task.stack.topRunningActivityLocked();
        if (top_activity != null && top_activity.state == ActivityState.DESTROYED) {
            acquireAppLaunch();
        }
        if ((flags & 2) == 0) {
            this.mUserLeaving = true;
        }
        if ((flags & 1) != 0) {
            task.setTaskToReturnTo(1);
        }
        if (task.stack == null) {
            Slog.e(TAG, "findTaskToMoveToFrontLocked: can't move task=" + task + " to front. Stack is null");
            return;
        }
        if (task.isResizeable() && options != null) {
            int stackId = options.getLaunchStackId();
            if (canUseActivityOptionsLaunchBounds(options, stackId)) {
                Rect bounds = TaskRecord.validateBounds(options.getLaunchBounds());
                task.updateOverrideConfiguration(bounds);
                if (stackId == -1) {
                    stackId = task.getLaunchStackId();
                }
                if (stackId != task.stack.mStackId) {
                    stackId = moveTaskToStackUncheckedLocked(task, stackId, true, false, reason).mStackId;
                }
                if (StackId.resizeStackWithLaunchBounds(stackId)) {
                    resizeStackLocked(stackId, bounds, null, null, false, true, false);
                } else {
                    this.mWindowManager.resizeTask(task.taskId, task.mBounds, task.mOverrideConfig, false, false);
                }
            }
        }
        ActivityRecord r = task.getTopActivity();
        task.stack.moveTaskToFrontLocked(task, false, options, r == null ? null : r.appTimeTracker, reason);
        if (ActivityManagerDebugConfig.DEBUG_STACK) {
            Slog.d(TAG_STACK, "findTaskToMoveToFront: moved to front of stack=" + task.stack);
        }
        handleNonResizableTaskIfNeeded(task, -1, task.stack.mStackId, forceNonResizeable);
    }

    boolean canUseActivityOptionsLaunchBounds(ActivityOptions options, int launchStackId) {
        if (options.getLaunchBounds() == null) {
            return false;
        }
        boolean z;
        if (this.mService.mSupportsPictureInPicture && launchStackId == 4) {
            z = true;
        } else {
            z = this.mService.mSupportsFreeformWindowManagement;
        }
        return z;
    }

    ActivityStack getStack(int stackId) {
        return getStack(stackId, false, false);
    }

    ActivityStack getStack(int stackId, boolean createStaticStackIfNeeded, boolean createOnTop) {
        ActivityContainer activityContainer = (ActivityContainer) this.mActivityContainers.get(stackId);
        if (activityContainer != null) {
            return activityContainer.mStack;
        }
        if (createStaticStackIfNeeded && StackId.isStaticStack(stackId)) {
            return createStackOnDisplay(stackId, 0, createOnTop);
        }
        return null;
    }

    ArrayList<ActivityStack> getStacks() {
        ArrayList<ActivityStack> allStacks = new ArrayList();
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            allStacks.addAll(((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks);
        }
        return allStacks;
    }

    IBinder getHomeActivityToken() {
        ActivityRecord homeActivity = getHomeActivity();
        if (homeActivity != null) {
            return homeActivity.appToken;
        }
        return null;
    }

    ActivityRecord getHomeActivity() {
        return getHomeActivityForUser(this.mCurrentUser);
    }

    ActivityRecord getHomeActivityForUser(int userId) {
        ArrayList<TaskRecord> tasks = this.mHomeStack.getAllTasks();
        for (int taskNdx = tasks.size() - 1; taskNdx >= 0; taskNdx--) {
            TaskRecord task = (TaskRecord) tasks.get(taskNdx);
            if (task.isHomeTask()) {
                ArrayList<ActivityRecord> activities = task.mActivities;
                for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                    ActivityRecord r = (ActivityRecord) activities.get(activityNdx);
                    if (r.isHomeActivity() && (userId == -1 || r.userId == userId)) {
                        return r;
                    }
                }
                continue;
            }
        }
        return null;
    }

    boolean isStackDockedInEffect(int stackId) {
        if (stackId != 3) {
            return StackId.isResizeableByDockedStack(stackId) && getStack(3) != null;
        } else {
            return true;
        }
    }

    ActivityContainer createVirtualActivityContainer(ActivityRecord parentActivity, IActivityContainerCallback callback) {
        ActivityContainer activityContainer = new VirtualActivityContainer(parentActivity, callback);
        this.mActivityContainers.put(activityContainer.mStackId, activityContainer);
        if (ActivityManagerDebugConfig.DEBUG_CONTAINERS) {
            Slog.d(TAG_CONTAINERS, "createActivityContainer: " + activityContainer);
        }
        parentActivity.mChildContainers.add(activityContainer);
        return activityContainer;
    }

    void removeChildActivityContainers(ActivityRecord parentActivity) {
        ArrayList<ActivityContainer> childStacks = parentActivity.mChildContainers;
        for (int containerNdx = childStacks.size() - 1; containerNdx >= 0; containerNdx--) {
            ActivityContainer container = (ActivityContainer) childStacks.remove(containerNdx);
            if (ActivityManagerDebugConfig.DEBUG_CONTAINERS) {
                Slog.d(TAG_CONTAINERS, "removeChildActivityContainers: removing " + container);
            }
            container.release();
        }
    }

    void deleteActivityContainer(IActivityContainer container) {
        ActivityContainer activityContainer = (ActivityContainer) container;
        if (activityContainer != null) {
            if (ActivityManagerDebugConfig.DEBUG_CONTAINERS) {
                Slog.d(TAG_CONTAINERS, "deleteActivityContainer: callers=" + Debug.getCallers(4));
            }
            int stackId = activityContainer.mStackId;
            this.mActivityContainers.remove(stackId);
            this.mWindowManager.removeStack(stackId);
        }
    }

    void resizeStackLocked(int stackId, Rect bounds, Rect tempTaskBounds, Rect tempTaskInsetBounds, boolean preserveWindows, boolean allowResizeInDockedMode, boolean deferResume) {
        if (stackId == 3) {
            resizeDockedStackLocked(bounds, tempTaskBounds, tempTaskInsetBounds, null, null, preserveWindows, deferResume);
            return;
        }
        ActivityStack stack = getStack(stackId);
        if (stack == null) {
            Slog.w(TAG, "resizeStack: stackId " + stackId + " not found.");
        } else if (allowResizeInDockedMode || getStack(3) == null) {
            Trace.traceBegin(64, "am.resizeStack_" + stackId);
            this.mWindowManager.deferSurfaceLayout();
            try {
                resizeStackUncheckedLocked(stack, bounds, tempTaskBounds, tempTaskInsetBounds);
                if (!deferResume) {
                    stack.ensureVisibleActivitiesConfigurationLocked(stack.topRunningActivityLocked(), preserveWindows);
                }
                this.mWindowManager.continueSurfaceLayout();
                Trace.traceEnd(64);
            } catch (Throwable th) {
                this.mWindowManager.continueSurfaceLayout();
                Trace.traceEnd(64);
            }
        }
    }

    void deferUpdateBounds(int stackId) {
        ActivityStack stack = getStack(stackId);
        if (stack != null) {
            stack.deferUpdateBounds();
        }
    }

    void continueUpdateBounds(int stackId) {
        ActivityStack stack = getStack(stackId);
        if (stack != null) {
            stack.continueUpdateBounds();
        }
    }

    void notifyAppTransitionDone() {
        continueUpdateBounds(0);
        for (int i = this.mResizingTasksDuringAnimation.size() - 1; i >= 0; i--) {
            int taskId = ((Integer) this.mResizingTasksDuringAnimation.valueAt(i)).intValue();
            if (anyTaskForIdLocked(taskId, false, -1) != null) {
                this.mWindowManager.setTaskDockedResizing(taskId, false);
            }
        }
        this.mResizingTasksDuringAnimation.clear();
    }

    void resizeStackUncheckedLocked(ActivityStack stack, Rect bounds, Rect tempTaskBounds, Rect tempTaskInsetBounds) {
        bounds = TaskRecord.validateBounds(bounds);
        if (stack.updateBoundsAllowed(bounds, tempTaskBounds, tempTaskInsetBounds)) {
            this.mTmpBounds.clear();
            this.mTmpConfigs.clear();
            this.mTmpInsetBounds.clear();
            ArrayList<TaskRecord> tasks = stack.getAllTasks();
            Rect taskBounds = tempTaskBounds != null ? tempTaskBounds : bounds;
            Rect insetBounds = tempTaskInsetBounds != null ? tempTaskInsetBounds : taskBounds;
            for (int i = tasks.size() - 1; i >= 0; i--) {
                TaskRecord task = (TaskRecord) tasks.get(i);
                if (task.isResizeable()) {
                    if (stack.mStackId == 2) {
                        this.tempRect2.set(task.mBounds);
                        fitWithinBounds(this.tempRect2, bounds);
                        task.updateOverrideConfiguration(this.tempRect2);
                    } else {
                        task.updateOverrideConfiguration(taskBounds, insetBounds);
                    }
                }
                this.mTmpConfigs.put(task.taskId, task.mOverrideConfig);
                this.mTmpBounds.put(task.taskId, task.mBounds);
                if (tempTaskInsetBounds != null) {
                    this.mTmpInsetBounds.put(task.taskId, tempTaskInsetBounds);
                }
            }
            this.mWindowManager.prepareFreezingTaskBounds(stack.mStackId);
            stack.mFullscreen = this.mWindowManager.resizeStack(stack.mStackId, bounds, this.mTmpConfigs, this.mTmpBounds, this.mTmpInsetBounds);
            stack.setBounds(bounds);
        }
    }

    void moveTasksToFullscreenStackLocked(int fromStackId, boolean onTop) {
        ActivityStack stack = getStack(fromStackId);
        if (stack != null) {
            int i;
            this.mWindowManager.deferSurfaceLayout();
            if (fromStackId == 3) {
                i = 0;
                while (i <= 4) {
                    try {
                        if (StackId.isResizeableByDockedStack(i) && getStack(i) != null) {
                            resizeStackLocked(i, null, null, null, true, true, true);
                        }
                        i++;
                    } catch (Throwable th) {
                        this.mAllowDockedStackResize = true;
                        this.mWindowManager.continueSurfaceLayout();
                    }
                }
                this.mAllowDockedStackResize = false;
            }
            ArrayList<TaskRecord> tasks = stack.getAllTasks();
            int size = tasks.size();
            if (onTop) {
                for (i = 0; i < size; i++) {
                    moveTaskToStackLocked(((TaskRecord) tasks.get(i)).taskId, 1, onTop, onTop, "moveTasksToFullscreenStack", true, true);
                }
                ensureActivitiesVisibleLocked(null, 0, true);
                resumeFocusedStackTopActivityLocked();
            } else {
                i = size - 1;
                while (i >= 0) {
                    if (!MultiWindowManager.isSupported() || tasks.get(i) == null || !((TaskRecord) tasks.get(i)).mLastTaskInFreeformStack) {
                        positionTaskInStackLocked(((TaskRecord) tasks.get(i)).taskId, 1, 0);
                    }
                    i--;
                }
            }
            this.mAllowDockedStackResize = true;
            this.mWindowManager.continueSurfaceLayout();
        }
    }

    void moveProfileTasksFromFreeformToFullscreenStackLocked(int userId) {
        ActivityStack stack = getStack(2);
        if (stack != null) {
            this.mWindowManager.deferSurfaceLayout();
            try {
                ArrayList<TaskRecord> tasks = stack.getAllTasks();
                for (int i = tasks.size() - 1; i >= 0; i--) {
                    if (taskContainsActivityFromUser((TaskRecord) tasks.get(i), userId)) {
                        positionTaskInStackLocked(((TaskRecord) tasks.get(i)).taskId, 1, 0);
                    }
                }
            } finally {
                this.mWindowManager.continueSurfaceLayout();
            }
        }
    }

    void resizeDockedStackLocked(Rect dockedBounds, Rect tempDockedTaskBounds, Rect tempDockedTaskInsetBounds, Rect tempOtherTaskBounds, Rect tempOtherTaskInsetBounds, boolean preserveWindows) {
        resizeDockedStackLocked(dockedBounds, tempDockedTaskBounds, tempDockedTaskInsetBounds, tempOtherTaskBounds, tempOtherTaskInsetBounds, preserveWindows, false);
    }

    void resizeDockedStackLocked(Rect dockedBounds, Rect tempDockedTaskBounds, Rect tempDockedTaskInsetBounds, Rect tempOtherTaskBounds, Rect tempOtherTaskInsetBounds, boolean preserveWindows, boolean deferResume) {
        if (this.mAllowDockedStackResize) {
            ActivityStack stack = getStack(3);
            if (stack == null) {
                Slog.w(TAG, "resizeDockedStackLocked: docked stack not found");
                return;
            }
            Trace.traceBegin(64, "am.resizeDockedStack");
            this.mWindowManager.deferSurfaceLayout();
            try {
                this.mAllowDockedStackResize = false;
                ActivityRecord r = stack.topRunningActivityLocked();
                resizeStackUncheckedLocked(stack, dockedBounds, tempDockedTaskBounds, tempDockedTaskInsetBounds);
                if (stack.mFullscreen || (dockedBounds == null && !stack.isAttached())) {
                    moveTasksToFullscreenStackLocked(3, true);
                    r = null;
                } else {
                    this.mWindowManager.getStackDockedModeBounds(0, this.tempRect, true);
                    int i = 0;
                    while (i <= 4) {
                        if (StackId.isResizeableByDockedStack(i) && getStack(i) != null) {
                            resizeStackLocked(i, this.tempRect, tempOtherTaskBounds, tempOtherTaskInsetBounds, preserveWindows, true, deferResume);
                        }
                        i++;
                    }
                }
                if (!deferResume) {
                    stack.ensureVisibleActivitiesConfigurationLocked(r, preserveWindows);
                }
                this.mAllowDockedStackResize = true;
                this.mWindowManager.continueSurfaceLayout();
                Trace.traceEnd(64);
                ResizeDockedStackTimeout resizeDockedStackTimeout = this.mResizeDockedStackTimeout;
                boolean z = (tempDockedTaskBounds == null && tempDockedTaskInsetBounds == null && tempOtherTaskBounds == null) ? tempOtherTaskInsetBounds != null : true;
                resizeDockedStackTimeout.notifyResizing(dockedBounds, z);
            } catch (Throwable th) {
                this.mAllowDockedStackResize = true;
                this.mWindowManager.continueSurfaceLayout();
                Trace.traceEnd(64);
            }
        }
    }

    void resizePinnedStackLocked(Rect pinnedBounds, Rect tempPinnedTaskBounds) {
        ActivityStack stack = getStack(4);
        if (stack == null) {
            Slog.w(TAG, "resizePinnedStackLocked: pinned stack not found");
            return;
        }
        Trace.traceBegin(64, "am.resizePinnedStack");
        this.mWindowManager.deferSurfaceLayout();
        try {
            ActivityRecord r = stack.topRunningActivityLocked();
            resizeStackUncheckedLocked(stack, pinnedBounds, tempPinnedTaskBounds, null);
            stack.ensureVisibleActivitiesConfigurationLocked(r, false);
        } finally {
            this.mWindowManager.continueSurfaceLayout();
            Trace.traceEnd(64);
        }
    }

    boolean resizeTaskLocked(TaskRecord task, Rect bounds, int resizeMode, boolean preserveWindow, boolean deferResume) {
        if (task.isResizeable()) {
            boolean needRelaunch;
            if (!MultiWindowManager.isSupported() || task.stack == null || task.stack.mStackId != 1 || preserveWindow) {
                needRelaunch = false;
            } else {
                needRelaunch = Objects.equals(task.mBounds, bounds);
            }
            if (MultiWindowManager.isSupported() && needRelaunch) {
                Slog.d(TAG, "[BMW] move task from freeform stack to fullscreen stack, need to restart");
            }
            boolean forced = (resizeMode & 2) != 0;
            if (Objects.equals(task.mBounds, bounds) && !forced && !needRelaunch) {
                return true;
            }
            bounds = TaskRecord.validateBounds(bounds);
            if (this.mWindowManager.isValidTaskId(task.taskId)) {
                Trace.traceBegin(64, "am.resizeTask_" + task.taskId);
                boolean kept = true;
                if (task.updateOverrideConfiguration(bounds) != null || needRelaunch) {
                    ActivityRecord r = task.topRunningActivityLocked();
                    if (r != null) {
                        if (needRelaunch) {
                            r.forceNewConfig = true;
                        }
                        kept = task.stack.ensureActivityConfigurationLocked(r, 0, preserveWindow);
                        if (!deferResume) {
                            ensureActivitiesVisibleLocked(r, 0, false);
                            if (!kept) {
                                resumeFocusedStackTopActivityLocked();
                            }
                        }
                    }
                }
                this.mWindowManager.resizeTask(task.taskId, task.mBounds, task.mOverrideConfig, kept, forced);
                Trace.traceEnd(64);
                return kept;
            }
            task.updateOverrideConfiguration(bounds);
            if (!(task.stack == null || task.stack.mStackId == 2)) {
                restoreRecentTaskLocked(task, 2);
            }
            return true;
        }
        Slog.w(TAG, "resizeTask: task " + task + " not resizeable.");
        return true;
    }

    ActivityStack createStackOnDisplay(int stackId, int displayId, boolean onTop) {
        ActivityDisplay activityDisplay = (ActivityDisplay) this.mActivityDisplays.get(displayId);
        if (activityDisplay == null) {
            return null;
        }
        ActivityContainer activityContainer = new ActivityContainer(stackId);
        this.mActivityContainers.put(stackId, activityContainer);
        activityContainer.attachToDisplayLocked(activityDisplay, onTop);
        if (activityContainer.mStack.mStackId == 3 && getFocusedStack() != null) {
            activityContainer.mStack.mDockComponentName = getFocusedStack().mComponentName;
        }
        return activityContainer.mStack;
    }

    int getNextStackId() {
        while (true) {
            if (this.mNextFreeStackId >= 5 && getStack(this.mNextFreeStackId) == null) {
                return this.mNextFreeStackId;
            }
            this.mNextFreeStackId++;
        }
    }

    private boolean restoreRecentTaskLocked(TaskRecord task, int stackId) {
        if (stackId == -1) {
            stackId = task.getLaunchStackId();
            if (MultiWindowManager.isSupported() && stackId == 2 && findStack(3) != null) {
                stackId = 1;
            }
        } else if (stackId == 3 && !task.canGoInDockedStack()) {
            stackId = 1;
        } else if (stackId == 2 && this.mService.mUserController.shouldConfirmCredentials(task.userId)) {
            stackId = 1;
        }
        if (task.stack != null) {
            if (task.stack.mStackId == stackId) {
                return true;
            }
            task.stack.removeTask(task, "restoreRecentTaskLocked", 1);
        }
        ActivityStack stack = getStack(stackId, true, false);
        if (stack == null) {
            if (ActivityManagerDebugConfig.DEBUG_RECENTS) {
                Slog.v(TAG_RECENTS, "Unable to find/create stack to restore recent task=" + task);
            }
            return false;
        }
        stack.addTask(task, false, "restoreRecentTask");
        if (ActivityManagerDebugConfig.DEBUG_RECENTS) {
            Slog.v(TAG_RECENTS, "Added restored task=" + task + " to stack=" + stack);
        }
        ArrayList<ActivityRecord> activities = task.mActivities;
        for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
            stack.addConfigOverride((ActivityRecord) activities.get(activityNdx), task);
        }
        return true;
    }

    ActivityStack moveTaskToStackUncheckedLocked(TaskRecord task, int stackId, boolean toTop, boolean forceFocus, String reason) {
        if (!StackId.isMultiWindowStack(stackId) || this.mService.mSupportsMultiWindow) {
            if (ActivityManagerDebugConfig.DEBUG_MULTIWINDOW) {
                Slog.d(TAG, "moveTaskToStackUncheckedLocked: " + task + ", t.uid=" + task.mCallingUid + ", t.pkg=" + task.mCallingPackage + ", t.userid=" + task.userId + ", t.intent=" + task.intent + ", stackid=" + stackId + ", toTop=" + toTop + ", focus= " + forceFocus + ", reason=" + reason, new Throwable());
            }
            ActivityRecord r = task.topRunningActivityLocked();
            ActivityStack prevStack = task.stack;
            boolean wasFocused = isFocusedStack(prevStack) && topRunningActivityLocked() == r;
            boolean wasResumed = prevStack.mResumedActivity == r;
            boolean wasFront = isFrontStack(prevStack) ? prevStack.topRunningActivityLocked() == r : false;
            if (stackId == 3 && !task.isResizeable()) {
                stackId = prevStack != null ? prevStack.mStackId : 1;
                Slog.w(TAG, "Can not move unresizeable task=" + task + " to docked stack. Moving to stackId=" + stackId + " instead.");
            }
            if (stackId == 2 && this.mService.mUserController.shouldConfirmCredentials(task.userId)) {
                stackId = prevStack != null ? prevStack.mStackId : 1;
                Slog.w(TAG, "Can not move locked profile task=" + task + " to freeform stack. Moving to stackId=" + stackId + " instead.");
            }
            task.mTemporarilyUnresizable = true;
            ActivityStack stack = getStack(stackId, true, toTop);
            task.mTemporarilyUnresizable = false;
            this.mWindowManager.moveTaskToStack(task.taskId, stack.mStackId, toTop);
            stack.addTask(task, toTop, reason);
            if (forceFocus || wasFocused) {
                wasFront = true;
            }
            stack.moveToFrontAndResumeStateIfNeeded(r, wasFront, wasResumed, reason);
            return stack;
        }
        throw new IllegalStateException("moveTaskToStackUncheckedLocked: Device doesn't support multi-window task=" + task + " to stackId=" + stackId);
    }

    boolean moveTaskToStackLocked(int taskId, int stackId, boolean toTop, boolean forceFocus, String reason, boolean animate) {
        return moveTaskToStackLocked(taskId, stackId, toTop, forceFocus, reason, animate, false);
    }

    boolean moveTaskToStackLocked(int taskId, int stackId, boolean toTop, boolean forceFocus, String reason, boolean animate, boolean deferResume) {
        TaskRecord task = anyTaskForIdLocked(taskId);
        if (task == null) {
            Slog.w(TAG, "moveTaskToStack: no task for id=" + taskId);
            return false;
        } else if (task.stack != null && task.stack.mStackId == stackId) {
            Slog.i(TAG, "moveTaskToStack: taskId=" + taskId + " already in stackId=" + stackId);
            return true;
        } else if (stackId != 2 || this.mService.mSupportsFreeformWindowManagement) {
            if (MultiWindowManager.isSupported() && stackId == 3) {
                Slog.d(TAG, "moveTaskToStack: moveTasksToFullscreenStack");
                if (task != null && task.stack.mStackId == 2) {
                    task.mLastTaskInFreeformStack = true;
                }
                this.mService.moveTasksToFullscreenStack(2, false);
                if (task.mLastTaskInFreeformStack) {
                    task.mLastTaskInFreeformStack = false;
                }
            }
            if (MultiWindowManager.isSupported() && stackId != 2 && task.mSticky) {
                this.mService.stickWindow(task, false);
            }
            ActivityRecord topActivity = task.getTopActivity();
            boolean mightReplaceWindow = StackId.replaceWindowsOnTaskMove(task.stack != null ? task.stack.mStackId : -1, stackId) && topActivity != null;
            if (mightReplaceWindow) {
                this.mWindowManager.setReplacingWindow(topActivity.appToken, animate);
            }
            this.mWindowManager.deferSurfaceLayout();
            int preferredLaunchStackId = stackId;
            boolean kept = true;
            try {
                boolean z;
                ActivityStack stack = moveTaskToStackUncheckedLocked(task, stackId, toTop, forceFocus, reason + " moveTaskToStack");
                stackId = stack.mStackId;
                if (!animate) {
                    stack.mNoAnimActivities.add(topActivity);
                }
                this.mWindowManager.prepareFreezingTaskBounds(stack.mStackId);
                if (stackId == 1 && task.mBounds != null) {
                    kept = resizeTaskLocked(task, stack.mBounds, 0, !mightReplaceWindow, deferResume);
                } else if (stackId == 2) {
                    Rect bounds = task.getLaunchBounds();
                    if (bounds == null) {
                        stack.layoutTaskInStack(task, null);
                        bounds = task.mBounds;
                    }
                    kept = resizeTaskLocked(task, bounds, 2, !mightReplaceWindow, deferResume);
                } else if (stackId == 3 || stackId == 4) {
                    kept = resizeTaskLocked(task, stack.mBounds, 0, !mightReplaceWindow, deferResume);
                }
                this.mWindowManager.continueSurfaceLayout();
                if (mightReplaceWindow) {
                    this.mWindowManager.scheduleClearReplacingWindowIfNeeded(topActivity.appToken, !kept);
                }
                if (!deferResume) {
                    ensureActivitiesVisibleLocked(null, 0, !mightReplaceWindow);
                    resumeFocusedStackTopActivityLocked();
                }
                handleNonResizableTaskIfNeeded(task, preferredLaunchStackId, stackId);
                if (preferredLaunchStackId == stackId) {
                    z = true;
                } else {
                    z = false;
                }
                return z;
            } catch (Throwable th) {
                this.mWindowManager.continueSurfaceLayout();
            }
        } else {
            throw new IllegalArgumentException("moveTaskToStack:Attempt to move task " + taskId + " to unsupported freeform stack");
        }
    }

    boolean moveTopStackActivityToPinnedStackLocked(int stackId, Rect bounds) {
        ActivityStack stack = getStack(stackId, false, false);
        if (stack == null) {
            throw new IllegalArgumentException("moveTopStackActivityToPinnedStackLocked: Unknown stackId=" + stackId);
        }
        ActivityRecord r = stack.topRunningActivityLocked();
        if (r == null) {
            Slog.w(TAG, "moveTopStackActivityToPinnedStackLocked: No top running activity in stack=" + stack);
            return false;
        } else if (this.mService.mForceResizableActivities || r.supportsPictureInPicture()) {
            moveActivityToPinnedStackLocked(r, "moveTopActivityToPinnedStack", bounds);
            return true;
        } else {
            Slog.w(TAG, "moveTopStackActivityToPinnedStackLocked: Picture-In-Picture not supported for  r=" + r);
            return false;
        }
    }

    void moveActivityToPinnedStackLocked(ActivityRecord r, String reason, Rect bounds) {
        this.mWindowManager.deferSurfaceLayout();
        try {
            TaskRecord task = r.task;
            if (r == task.stack.getVisibleBehindActivity()) {
                requestVisibleBehindLocked(r, false);
            }
            ActivityStack stack = getStack(4, true, true);
            resizeStackLocked(4, task.mBounds, null, null, false, true, false);
            if (task.mActivities.size() == 1) {
                if (task.getTaskToReturnTo() == 1) {
                    moveHomeStackToFront(reason);
                }
                moveTaskToStackLocked(task.taskId, 4, true, true, reason, false);
            } else {
                stack.moveActivityToStack(r);
            }
            this.mWindowManager.continueSurfaceLayout();
            ensureActivitiesVisibleLocked(null, 0, false);
            resumeFocusedStackTopActivityLocked();
            this.mWindowManager.animateResizePinnedStack(bounds, -1);
            this.mService.notifyActivityPinnedLocked();
        } catch (Throwable th) {
            this.mWindowManager.continueSurfaceLayout();
        }
    }

    void positionTaskInStackLocked(int taskId, int stackId, int position) {
        TaskRecord task = anyTaskForIdLocked(taskId);
        if (task == null) {
            Slog.w(TAG, "positionTaskInStackLocked: no task for id=" + taskId);
            return;
        }
        ActivityStack stack = getStack(stackId, true, false);
        task.updateOverrideConfigurationForStack(stack);
        this.mWindowManager.positionTaskInStack(taskId, stackId, position, task.mBounds, task.mOverrideConfig);
        stack.positionTask(task, position);
        stack.ensureActivitiesVisibleLocked(null, 0, false);
        resumeFocusedStackTopActivityLocked();
    }

    void acquireAppLaunch() {
        if (mHyp == null) {
            mHyp = new Hypnus();
        }
        if (mHyp != null) {
            mHyp.hypnusSetAction(13, SLEEP_TIMEOUT);
        }
    }

    void startActivityBoost(String className) {
        if (className != null) {
            if (className.equals("com.tencent.mm.plugin.voip.ui.VideoActivity") || className.equals("com.tencent.av.ui.AVLoadingDialogActivity") || className.equals("com.tencent.mm.plugin.exdevice.ui.ExdeviceRankInfoUI") || className.equals("com.tencent.mm.plugin.webview.ui.tools.WebViewUI") || className.equals("com.jingdong.app.mall.pay.CashierDeskActivity") || className.equals("com.tmall.wireless.pay.TMPayActivity") || className.equals("com.alipay.android.app.pay.MiniLaucherActivity") || className.equals("com.alipay.android.app.ui.quickpay.window.MiniPayActivity") || className.equals("com.taobao.tao.alipay.cashdesk.CashDeskActivity")) {
                if (mHyp == null) {
                    mHyp = new Hypnus();
                }
                if (mHyp != null) {
                    mHyp.hypnusSetAction(12, OppoBrightUtils.HIGH_BRIGHTNESS_LUX_STEP);
                }
            }
        }
    }

    ActivityRecord findTaskLocked(ActivityRecord r) {
        this.mTmpFindTaskResult.r = null;
        this.mTmpFindTaskResult.matchedByRootAffinity = false;
        if (ActivityManagerDebugConfig.DEBUG_TASKS) {
            Slog.d(TAG_TASKS, "Looking for task of " + r);
        }
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            ArrayList<ActivityStack> stacks = ((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks;
            for (int stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
                ActivityStack stack = (ActivityStack) stacks.get(stackNdx);
                if (r.isApplicationActivity() || stack.isHomeStack()) {
                    if (stack.mActivityContainer.isEligibleForNewTasks()) {
                        stack.findTaskLocked(r, this.mTmpFindTaskResult);
                        if (!(this.mTmpFindTaskResult.r == null || this.mTmpFindTaskResult.matchedByRootAffinity)) {
                            if (this.mTmpFindTaskResult.r.state == ActivityState.DESTROYED) {
                                acquireAppLaunch();
                            }
                            return this.mTmpFindTaskResult.r;
                        }
                    } else if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                        Slog.d(TAG_TASKS, "Skipping stack: (new task not allowed) " + stack);
                    }
                } else if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                    Slog.d(TAG_TASKS, "Skipping stack: (home activity) " + stack);
                }
            }
        }
        acquireAppLaunch();
        if (ActivityManagerDebugConfig.DEBUG_TASKS && this.mTmpFindTaskResult.r == null) {
            Slog.d(TAG_TASKS, "No task found");
        }
        return this.mTmpFindTaskResult.r;
    }

    ActivityRecord findActivityLocked(Intent intent, ActivityInfo info, boolean compareIntentFilters) {
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            ArrayList<ActivityStack> stacks = ((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks;
            for (int stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
                ActivityRecord ar = ((ActivityStack) stacks.get(stackNdx)).findActivityLocked(intent, info, compareIntentFilters);
                if (ar != null) {
                    return ar;
                }
            }
        }
        return null;
    }

    void goingToSleepLocked() {
        scheduleSleepTimeout();
        if (!this.mGoingToSleep.isHeld()) {
            this.mGoingToSleep.acquire();
            if (ActivityManagerService.DEBUG_COLOROS_AMS) {
                Slog.d(TAG, "acquire wake lock for goingToSleepLocked.");
            }
            if (this.mLaunchingActivity.isHeld()) {
                Slog.d(TAG, "mLaunchingActivity.release() in goingToSleepLocked");
                this.mLaunchingActivity.release();
                if (this.mPerfService != null) {
                    this.mPerfService.amsBoostStop();
                }
                this.mService.mHandler.removeMessages(104);
            }
        }
        checkReadyForSleepLocked();
    }

    boolean shutdownLocked(int timeout) {
        goingToSleepLocked();
        boolean timedout = false;
        long endTime = System.currentTimeMillis() + ((long) timeout);
        while (true) {
            boolean cantShutdown = false;
            for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
                ArrayList<ActivityStack> stacks = ((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks;
                for (int stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
                    cantShutdown |= ((ActivityStack) stacks.get(stackNdx)).checkReadyForSleepLocked();
                }
            }
            if (!cantShutdown) {
                break;
            }
            long timeRemaining = endTime - System.currentTimeMillis();
            if (timeRemaining <= 0) {
                Slog.w(TAG, "Activity manager shutdown timed out");
                timedout = true;
                break;
            }
            try {
                this.mService.wait(timeRemaining);
            } catch (InterruptedException e) {
            }
        }
        this.mSleepTimeout = true;
        checkReadyForSleepLocked();
        return timedout;
    }

    void comeOutOfSleepIfNeededLocked() {
        removeSleepTimeouts();
        if (this.mGoingToSleep.isHeld()) {
            this.mGoingToSleep.release();
        }
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            ArrayList<ActivityStack> stacks = ((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks;
            for (int stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
                ActivityStack stack = (ActivityStack) stacks.get(stackNdx);
                stack.awakeFromSleepingLocked();
                if (isFocusedStack(stack)) {
                    resumeFocusedStackTopActivityLocked();
                }
            }
        }
        this.mGoingToSleepActivities.clear();
    }

    void activitySleptLocked(ActivityRecord r) {
        this.mGoingToSleepActivities.remove(r);
        if (this.mService.mIgnoreSleepCheckLater) {
            Slog.d(TAG, "activitySleptLocked, mIgnoreSleepCheckLater, do not care.");
        } else {
            checkReadyForSleepLocked();
        }
    }

    void checkReadyForSleepWhenResumeLocked(ComponentName currResumeActCompName) {
        if (this.mService.mIgnoreSleepCheckLater) {
            Slog.d(TAG, "checkReadyForSleepWhenResumeLocked, mIgnoreSleepCheckLater, do not care. " + Debug.getCallers(4));
        } else {
            checkReadyForSleepLocked();
        }
    }

    void checkReadyForSleepLocked() {
        if (this.mService.isSleepingOrShuttingDownLocked()) {
            int displayNdx;
            ArrayList<ActivityStack> stacks;
            int stackNdx;
            if (!this.mSleepTimeout) {
                boolean dontSleep = false;
                for (displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
                    stacks = ((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks;
                    for (stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
                        dontSleep |= ((ActivityStack) stacks.get(stackNdx)).checkReadyForSleepLocked();
                    }
                }
                if (this.mStoppingActivities.size() > 0) {
                    if (ActivityManagerDebugConfig.DEBUG_PAUSE || ActivityManagerService.DEBUG_COLOROS_AMS) {
                        Slog.v(TAG_PAUSE, "Sleep still need to stop " + this.mStoppingActivities.size() + " activities");
                    }
                    scheduleIdleLocked();
                    if (!ActivityManagerService.IS_USER_BUILD || ActivityManagerDebugConfig.DEBUG_STACK) {
                        Slog.d(TAG, "ACT-IDLE_NOW_MSG from checkReadyForSleepLocked size > 0");
                    }
                    dontSleep = true;
                }
                if (this.mGoingToSleepActivities.size() > 0) {
                    if (ActivityManagerDebugConfig.DEBUG_PAUSE || ActivityManagerService.DEBUG_COLOROS_AMS) {
                        Slog.v(TAG_PAUSE, "Sleep still need to sleep " + this.mGoingToSleepActivities.size() + " activities");
                    }
                    dontSleep = true;
                }
                if (dontSleep) {
                    if (ActivityManagerService.DEBUG_COLOROS_AMS && this.mGoingToSleep.isHeld()) {
                        Slog.d(TAG, "checkReadyForSleepLocked:hold wakelock but not sleep.");
                    }
                    return;
                }
            }
            this.mService.mActivityStarter.sendPowerHintForLaunchEndIfNeeded();
            for (displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
                stacks = ((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks;
                for (stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
                    ((ActivityStack) stacks.get(stackNdx)).goToSleep();
                }
            }
            removeSleepTimeouts();
            if (this.mGoingToSleep.isHeld()) {
                this.mGoingToSleep.release();
                if (ActivityManagerService.DEBUG_COLOROS_AMS) {
                    Slog.d(TAG, "checkReadyForSleepLocked:release wakelock.");
                }
            }
            if (this.mService.mShuttingDown) {
                this.mService.notifyAll();
            }
        }
    }

    boolean reportResumedActivityLocked(ActivityRecord r) {
        if (isFocusedStack(r.task.stack)) {
            this.mService.updateUsageStats(r, true);
        }
        if (!allResumedActivitiesComplete()) {
            return false;
        }
        ensureActivitiesVisibleLocked(null, 0, false);
        this.mWindowManager.executeAppTransition();
        return true;
    }

    void handleAppCrashLocked(ProcessRecord app) {
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            ArrayList<ActivityStack> stacks = ((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks;
            for (int stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
                ((ActivityStack) stacks.get(stackNdx)).handleAppCrashLocked(app);
            }
        }
    }

    boolean requestVisibleBehindLocked(ActivityRecord r, boolean visible) {
        ActivityRecord activityRecord = null;
        ActivityStack stack = r.task.stack;
        if (stack == null) {
            if (ActivityManagerDebugConfig.DEBUG_VISIBLE_BEHIND) {
                Slog.d(TAG_VISIBLE_BEHIND, "requestVisibleBehind: r=" + r + " visible=" + visible + " stack is null");
            }
            return false;
        } else if (!visible || StackId.activitiesCanRequestVisibleBehind(stack.mStackId)) {
            boolean isVisible = stack.hasVisibleBehindActivity();
            if (ActivityManagerDebugConfig.DEBUG_VISIBLE_BEHIND) {
                Slog.d(TAG_VISIBLE_BEHIND, "requestVisibleBehind r=" + r + " visible=" + visible + " isVisible=" + isVisible);
            }
            ActivityRecord top = topRunningActivityLocked();
            if (top == null || top == r || visible == isVisible) {
                if (ActivityManagerDebugConfig.DEBUG_VISIBLE_BEHIND) {
                    Slog.d(TAG_VISIBLE_BEHIND, "requestVisibleBehind: quick return");
                }
                if (!visible) {
                    r = null;
                }
                stack.setVisibleBehindActivity(r);
                return true;
            } else if (visible && top.fullscreen) {
                if (ActivityManagerDebugConfig.DEBUG_VISIBLE_BEHIND) {
                    Slog.d(TAG_VISIBLE_BEHIND, "requestVisibleBehind: returning top.fullscreen=" + top.fullscreen + " top.state=" + top.state + " top.app=" + top.app + " top.app.thread=" + top.app.thread);
                }
                return false;
            } else if (visible || stack.getVisibleBehindActivity() == r) {
                if (visible) {
                    activityRecord = r;
                }
                stack.setVisibleBehindActivity(activityRecord);
                if (!visible) {
                    ActivityRecord next = stack.findNextTranslucentActivity(r);
                    if (next != null && next.isHomeActivity()) {
                        this.mService.convertFromTranslucent(next.appToken);
                    }
                }
                if (!(top.app == null || top.app.thread == null)) {
                    try {
                        top.app.thread.scheduleBackgroundVisibleBehindChanged(top.appToken, visible);
                    } catch (RemoteException e) {
                    }
                }
                return true;
            } else {
                if (ActivityManagerDebugConfig.DEBUG_VISIBLE_BEHIND) {
                    Slog.d(TAG_VISIBLE_BEHIND, "requestVisibleBehind: returning visible=" + visible + " stack.getVisibleBehindActivity()=" + stack.getVisibleBehindActivity() + " r=" + r);
                }
                return false;
            }
        } else {
            if (ActivityManagerDebugConfig.DEBUG_VISIBLE_BEHIND) {
                Slog.d(TAG_VISIBLE_BEHIND, "requestVisibleBehind: r=" + r + " visible=" + visible + " stackId=" + stack.mStackId + " can't contain visible behind activities");
            }
            return false;
        }
    }

    void handleLaunchTaskBehindCompleteLocked(ActivityRecord r) {
        TaskRecord task = r.task;
        ActivityStack stack = task.stack;
        r.mLaunchTaskBehind = false;
        task.setLastThumbnailLocked(stack.screenshotActivitiesLocked(r));
        this.mRecentTasks.addLocked(task);
        this.mService.notifyTaskStackChangedLocked();
        this.mWindowManager.setAppVisibility(r.appToken, false);
        ActivityRecord top = stack.topActivity();
        if (top != null) {
            top.task.touchActiveTime();
        }
    }

    void scheduleLaunchTaskBehindComplete(IBinder token) {
        this.mHandler.obtainMessage(112, token).sendToTarget();
    }

    void ensureActivitiesVisibleLocked(ActivityRecord starting, int configChanges, boolean preserveWindows) {
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            ArrayList<ActivityStack> stacks = ((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks;
            for (int stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
                ((ActivityStack) stacks.get(stackNdx)).ensureActivitiesVisibleLocked(starting, configChanges, preserveWindows);
            }
        }
    }

    void invalidateTaskLayers() {
        this.mTaskLayersChanged = true;
    }

    void rankTaskLayersIfNeeded() {
        if (this.mTaskLayersChanged) {
            this.mTaskLayersChanged = false;
            for (int displayNdx = 0; displayNdx < this.mActivityDisplays.size(); displayNdx++) {
                ArrayList<ActivityStack> stacks = ((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks;
                int baseLayer = 0;
                for (int stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
                    baseLayer += ((ActivityStack) stacks.get(stackNdx)).rankTaskLayers(baseLayer);
                }
            }
        }
    }

    void clearOtherAppTimeTrackers(AppTimeTracker except) {
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            ArrayList<ActivityStack> stacks = ((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks;
            for (int stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
                ((ActivityStack) stacks.get(stackNdx)).clearOtherAppTimeTrackers(except);
            }
        }
    }

    void scheduleDestroyAllActivities(ProcessRecord app, String reason) {
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            ArrayList<ActivityStack> stacks = ((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks;
            int numStacks = stacks.size();
            for (int stackNdx = 0; stackNdx < numStacks; stackNdx++) {
                ((ActivityStack) stacks.get(stackNdx)).scheduleDestroyActivities(app, reason);
            }
        }
    }

    void releaseSomeActivitiesLocked(ProcessRecord app, String reason) {
        TaskRecord firstTask = null;
        ArraySet tasks = null;
        if (ActivityManagerDebugConfig.DEBUG_RELEASE) {
            Slog.d(TAG_RELEASE, "Trying to release some activities in " + app);
        }
        for (int i = 0; i < app.activities.size(); i++) {
            ActivityRecord r = (ActivityRecord) app.activities.get(i);
            if (r.finishing || r.state == ActivityState.DESTROYING || r.state == ActivityState.DESTROYED) {
                if (ActivityManagerDebugConfig.DEBUG_RELEASE) {
                    Slog.d(TAG_RELEASE, "Abort release; already destroying: " + r);
                }
                return;
            }
            if (r.visible || !r.stopped || !r.haveState || r.state == ActivityState.RESUMED || r.state == ActivityState.PAUSING || r.state == ActivityState.PAUSED || r.state == ActivityState.STOPPING) {
                if (ActivityManagerDebugConfig.DEBUG_RELEASE) {
                    Slog.d(TAG_RELEASE, "Not releasing in-use activity: " + r);
                }
            } else if (r.task != null) {
                if (ActivityManagerDebugConfig.DEBUG_RELEASE) {
                    Slog.d(TAG_RELEASE, "Collecting release task " + r.task + " from " + r);
                }
                if (firstTask == null) {
                    firstTask = r.task;
                } else if (firstTask != r.task) {
                    if (tasks == null) {
                        tasks = new ArraySet();
                        tasks.add(firstTask);
                    }
                    tasks.add(r.task);
                }
            }
        }
        if (tasks == null) {
            if (ActivityManagerDebugConfig.DEBUG_RELEASE) {
                Slog.d(TAG_RELEASE, "Didn't find two or more tasks to release");
            }
            return;
        }
        int numDisplays = this.mActivityDisplays.size();
        for (int displayNdx = 0; displayNdx < numDisplays; displayNdx++) {
            ArrayList<ActivityStack> stacks = ((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks;
            int stackNdx = 0;
            while (stackNdx < stacks.size()) {
                if (((ActivityStack) stacks.get(stackNdx)).releaseSomeActivitiesLocked(app, tasks, reason) <= 0) {
                    stackNdx++;
                } else {
                    return;
                }
            }
        }
    }

    boolean switchUserLocked(int userId, UserState uss) {
        boolean z;
        ActivityStack stack;
        int focusStackId = this.mFocusedStack.getStackId();
        if (focusStackId == 3) {
            z = true;
        } else {
            z = false;
        }
        moveTasksToFullscreenStackLocked(3, z);
        this.mUserStackInFront.put(this.mCurrentUser, focusStackId);
        int restoreStackId = this.mUserStackInFront.get(userId, 0);
        this.mCurrentUser = userId;
        this.mStartingUsers.add(uss);
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            ArrayList<ActivityStack> stacks = ((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks;
            for (int stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
                stack = (ActivityStack) stacks.get(stackNdx);
                stack.switchUserLocked(userId);
                TaskRecord task = stack.topTask();
                if (task != null) {
                    this.mWindowManager.moveTaskToTop(task.taskId);
                }
            }
        }
        stack = getStack(restoreStackId);
        if (stack == null) {
            stack = this.mHomeStack;
        }
        boolean homeInFront = stack.isHomeStack();
        if (stack.isOnHomeDisplay()) {
            stack.moveToFront("switchUserOnHomeDisplay");
        } else {
            resumeHomeStackTask(1, null, "switchUserOnOtherDisplay");
        }
        return homeInFront;
    }

    boolean isCurrentProfileLocked(int userId) {
        if (userId == this.mCurrentUser || OppoMultiAppManager.getInstance().isCurrentProfile(userId)) {
            return true;
        }
        return this.mService.mUserController.isCurrentProfileLocked(userId);
    }

    boolean okToShowLocked(ActivityRecord r) {
        if (r == null) {
            return false;
        }
        if ((r.info.flags & 1024) != 0) {
            return true;
        }
        if (!isCurrentProfileLocked(r.userId)) {
            return false;
        }
        if (this.mService.mUserController.isUserStoppingOrShuttingDownLocked(r.userId)) {
            return false;
        }
        return true;
    }

    final ArrayList<ActivityRecord> processStoppingActivitiesLocked(boolean remove) {
        ArrayList<ActivityRecord> stops = null;
        boolean nowVisible = allResumedActivitiesVisible();
        for (int activityNdx = this.mStoppingActivities.size() - 1; activityNdx >= 0; activityNdx--) {
            ActivityRecord s = (ActivityRecord) this.mStoppingActivities.get(activityNdx);
            boolean waitingVisible = this.mWaitingVisibleActivities.contains(s);
            if (ActivityManagerDebugConfig.DEBUG_STATES) {
                Slog.v(TAG, "Stopping " + s + ": nowVisible=" + nowVisible + " waitingVisible=" + waitingVisible + " finishing=" + s.finishing);
            }
            if (waitingVisible && nowVisible) {
                this.mWaitingVisibleActivities.remove(s);
                waitingVisible = false;
                if (s.finishing) {
                    if (ActivityManagerDebugConfig.DEBUG_STATES) {
                        Slog.v(TAG, "Before stopping, can hide: " + s);
                    }
                    this.mWindowManager.setAppVisibility(s.appToken, false);
                }
            }
            if ((!waitingVisible || this.mService.isSleepingOrShuttingDownLocked()) && remove) {
                if (ActivityManagerDebugConfig.DEBUG_STATES) {
                    Slog.v(TAG, "Ready to stop: " + s);
                }
                if (stops == null) {
                    stops = new ArrayList();
                }
                stops.add(s);
                this.mStoppingActivities.remove(activityNdx);
            }
        }
        return stops;
    }

    void validateTopActivitiesLocked() {
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            ArrayList<ActivityStack> stacks = ((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks;
            for (int stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
                ActivityStack stack = (ActivityStack) stacks.get(stackNdx);
                ActivityRecord r = stack.topRunningActivityLocked();
                ActivityState state = r == null ? ActivityState.DESTROYED : r.state;
                if (!isFocusedStack(stack)) {
                    ActivityRecord resumed = stack.mResumedActivity;
                    if (resumed != null && resumed == r) {
                        Slog.e(TAG, "validateTop...: back stack has resumed activity r=" + r + " state=" + state);
                    }
                    if (r != null && (state == ActivityState.INITIALIZING || state == ActivityState.RESUMED)) {
                        Slog.e(TAG, "validateTop...: activity in back resumed r=" + r + " state=" + state);
                    }
                } else if (r == null) {
                    Slog.e(TAG, "validateTop...: null top activity, stack=" + stack);
                } else {
                    ActivityRecord pausing = stack.mPausingActivity;
                    if (pausing != null && pausing == r) {
                        Slog.e(TAG, "validateTop...: top stack has pausing activity r=" + r + " state=" + state);
                    }
                    if (!(state == ActivityState.INITIALIZING || state == ActivityState.RESUMED)) {
                        Slog.e(TAG, "validateTop...: activity in front not resumed r=" + r + " state=" + state);
                    }
                }
            }
        }
    }

    private String lockTaskModeToString() {
        switch (this.mLockTaskModeState) {
            case 0:
                return "NONE";
            case 1:
                return "LOCKED";
            case 2:
                return "PINNED";
            default:
                return "unknown=" + this.mLockTaskModeState;
        }
    }

    public void dump(PrintWriter pw, String prefix) {
        pw.print(prefix);
        pw.print("mFocusedStack=" + this.mFocusedStack);
        pw.print(" mLastFocusedStack=");
        pw.println(this.mLastFocusedStack);
        pw.print(prefix);
        pw.println("mSleepTimeout=" + this.mSleepTimeout);
        pw.print(prefix);
        pw.println("mCurTaskIdForUser=" + this.mCurTaskIdForUser);
        pw.print(prefix);
        pw.println("mUserStackInFront=" + this.mUserStackInFront);
        pw.print(prefix);
        pw.println("mActivityContainers=" + this.mActivityContainers);
        pw.print(prefix);
        pw.print("mLockTaskModeState=" + lockTaskModeToString());
        SparseArray<String[]> packages = this.mService.mLockTaskPackages;
        if (packages.size() > 0) {
            pw.println(" mLockTaskPackages (userId:packages)=");
            for (int i = 0; i < packages.size(); i++) {
                pw.print(prefix);
                pw.print(prefix);
                pw.print(packages.keyAt(i));
                pw.print(":");
                pw.println(Arrays.toString((Object[]) packages.valueAt(i)));
            }
        }
        pw.println(" mLockTaskModeTasks" + this.mLockTaskModeTasks);
    }

    ArrayList<ActivityRecord> getDumpActivitiesLocked(String name) {
        return this.mFocusedStack.getDumpActivitiesLocked(name);
    }

    static boolean printThisActivity(PrintWriter pw, ActivityRecord activity, String dumpPackage, boolean needSep, String prefix) {
        if (activity == null || (dumpPackage != null && !dumpPackage.equals(activity.packageName))) {
            return false;
        }
        if (needSep) {
            pw.println();
        }
        pw.print(prefix);
        pw.println(activity);
        return true;
    }

    boolean dumpActivitiesLocked(FileDescriptor fd, PrintWriter pw, boolean dumpAll, boolean dumpClient, String dumpPackage) {
        int printed = 0;
        boolean needSep = false;
        for (int displayNdx = 0; displayNdx < this.mActivityDisplays.size(); displayNdx++) {
            ActivityDisplay activityDisplay = (ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx);
            pw.print("Display #");
            pw.print(activityDisplay.mDisplayId);
            pw.println(" (activities from top to bottom):");
            ArrayList<ActivityStack> stacks = activityDisplay.mStacks;
            for (int stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
                ActivityStack stack = (ActivityStack) stacks.get(stackNdx);
                StringBuilder stringBuilder = new StringBuilder(128);
                stringBuilder.append("  Stack #");
                stringBuilder.append(stack.mStackId);
                stringBuilder.append(":");
                stringBuilder.append("\n");
                stringBuilder.append("  mFullscreen=").append(stack.mFullscreen);
                stringBuilder.append("\n");
                stringBuilder.append("  mBounds=").append(stack.mBounds);
                printed = (printed | stack.dumpActivitiesLocked(fd, pw, dumpAll, dumpClient, dumpPackage, needSep, stringBuilder.toString())) | dumpHistoryList(fd, pw, stack.mLRUActivities, "    ", "Run", false, !dumpAll, false, dumpPackage, true, "    Running activities (most recent first):", null);
                needSep = printed;
                if (printThisActivity(pw, stack.mPausingActivity, dumpPackage, printed, "    mPausingActivity: ")) {
                    printed = 1;
                    needSep = false;
                }
                if (printThisActivity(pw, stack.mResumedActivity, dumpPackage, needSep, "    mResumedActivity: ")) {
                    printed = 1;
                    needSep = false;
                }
                if (dumpAll) {
                    if (printThisActivity(pw, stack.mLastPausedActivity, dumpPackage, needSep, "    mLastPausedActivity: ")) {
                        printed = 1;
                        needSep = true;
                    }
                    printed |= printThisActivity(pw, stack.mLastNoHistoryActivity, dumpPackage, needSep, "    mLastNoHistoryActivity: ");
                }
                needSep = printed;
            }
        }
        return ((((printed | dumpHistoryList(fd, pw, this.mFinishingActivities, "  ", "Fin", false, !dumpAll, false, dumpPackage, true, "  Activities waiting to finish:", null)) | dumpHistoryList(fd, pw, this.mStoppingActivities, "  ", "Stop", false, !dumpAll, false, dumpPackage, true, "  Activities waiting to stop:", null)) | dumpHistoryList(fd, pw, this.mWaitingVisibleActivities, "  ", "Wait", false, !dumpAll, false, dumpPackage, true, "  Activities waiting for another to become visible:", null)) | dumpHistoryList(fd, pw, this.mGoingToSleepActivities, "  ", "Sleep", false, !dumpAll, false, dumpPackage, true, "  Activities waiting to sleep:", null)) | dumpHistoryList(fd, pw, this.mGoingToSleepActivities, "  ", "Sleep", false, !dumpAll, false, dumpPackage, true, "  Activities waiting to sleep:", null);
    }

    static boolean dumpHistoryList(FileDescriptor fd, PrintWriter pw, List<ActivityRecord> list, String prefix, String label, boolean complete, boolean brief, boolean client, String dumpPackage, boolean needNL, String header1, String header2) {
        TaskRecord lastTask = null;
        String innerPrefix = null;
        String[] args = null;
        boolean printed = false;
        for (int i = list.size() - 1; i >= 0; i--) {
            ActivityRecord r = (ActivityRecord) list.get(i);
            if (dumpPackage != null) {
                if (!dumpPackage.equals(r.packageName)) {
                    continue;
                }
            }
            if (innerPrefix == null) {
                innerPrefix = prefix + "      ";
                args = new String[0];
            }
            printed = true;
            boolean full = !brief && (complete || !r.isInHistory());
            if (needNL) {
                pw.println(IElsaManager.EMPTY_PACKAGE);
                needNL = false;
            }
            if (header1 != null) {
                pw.println(header1);
                header1 = null;
            }
            if (header2 != null) {
                pw.println(header2);
                header2 = null;
            }
            if (lastTask != r.task) {
                lastTask = r.task;
                pw.print(prefix);
                pw.print(full ? "* " : "  ");
                pw.println(lastTask);
                if (full) {
                    lastTask.dump(pw, prefix + "  ");
                } else if (complete && lastTask.intent != null) {
                    pw.print(prefix);
                    pw.print("  ");
                    pw.println(lastTask.intent.toInsecureStringWithClip());
                }
            }
            pw.print(prefix);
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
            if (!(!client || r.app == null || r.app.thread == null)) {
                pw.flush();
                TransferPipe tp;
                try {
                    tp = new TransferPipe();
                    r.app.thread.dumpActivity(tp.getWriteFd().getFileDescriptor(), r.appToken, innerPrefix, args);
                    tp.go(fd, 2000);
                    tp.kill();
                } catch (IOException e) {
                    pw.println(innerPrefix + "Failure while dumping the activity: " + e);
                } catch (RemoteException e2) {
                    pw.println(innerPrefix + "Got a RemoteException while dumping the activity");
                } catch (Throwable th) {
                    tp.kill();
                }
                needNL = true;
            }
        }
        return printed;
    }

    void scheduleIdleTimeoutLocked(ActivityRecord next) {
        if (ActivityManagerDebugConfig.DEBUG_IDLE) {
            Slog.d(TAG_IDLE, "scheduleIdleTimeoutLocked: Callers=" + Debug.getCallers(4));
        }
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(100, next), 10000);
    }

    final void scheduleIdleLocked() {
        this.mHandler.sendEmptyMessage(101);
    }

    void removeTimeoutsForActivityLocked(ActivityRecord r) {
        if (ActivityManagerDebugConfig.DEBUG_IDLE) {
            Slog.d(TAG_IDLE, "removeTimeoutsForActivity: Callers=" + Debug.getCallers(4));
        }
        this.mHandler.removeMessages(100, r);
    }

    final void scheduleResumeTopActivities() {
        if (!this.mHandler.hasMessages(102)) {
            this.mHandler.sendEmptyMessage(102);
        }
    }

    void removeSleepTimeouts() {
        this.mSleepTimeout = false;
        this.mHandler.removeMessages(103);
    }

    final void scheduleSleepTimeout() {
        removeSleepTimeouts();
        this.mHandler.sendEmptyMessageDelayed(103, 5000);
    }

    public void onDisplayAdded(int displayId) {
        if (ActivityManagerDebugConfig.DEBUG_STACK) {
            Slog.v(TAG, "Display added displayId=" + displayId);
        }
        this.mHandler.sendMessage(this.mHandler.obtainMessage(105, displayId, 0));
    }

    public void onDisplayRemoved(int displayId) {
        if (ActivityManagerDebugConfig.DEBUG_STACK) {
            Slog.v(TAG, "Display removed displayId=" + displayId);
        }
        this.mHandler.sendMessage(this.mHandler.obtainMessage(107, displayId, 0));
    }

    public void onDisplayChanged(int displayId) {
        if (ActivityManagerDebugConfig.DEBUG_STACK) {
            Slog.v(TAG, "Display changed displayId=" + displayId);
        }
        this.mHandler.sendMessage(this.mHandler.obtainMessage(106, displayId, 0));
    }

    /* JADX WARNING: Missing block: B:16:0x004a, code:
            com.android.server.am.ActivityManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Missing block: B:17:0x004d, code:
            if (r1 == false) goto L_0x0054;
     */
    /* JADX WARNING: Missing block: B:18:0x004f, code:
            r6.mWindowManager.onDisplayAdded(r7);
     */
    /* JADX WARNING: Missing block: B:19:0x0054, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void handleDisplayAdded(int displayId) {
        synchronized (this.mService) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                boolean newDisplay = this.mActivityDisplays.get(displayId) == null;
                if (newDisplay) {
                    ActivityDisplay activityDisplay = new ActivityDisplay(displayId);
                    if (activityDisplay.mDisplay == null) {
                        Slog.w(TAG, "Display " + displayId + " gone before initialization complete");
                    } else {
                        this.mActivityDisplays.put(displayId, activityDisplay);
                        calculateDefaultMinimalSizeOfResizeableTasks(activityDisplay);
                    }
                }
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    private void calculateDefaultMinimalSizeOfResizeableTasks(ActivityDisplay display) {
        this.mDefaultMinSizeOfResizeableTask = this.mService.mContext.getResources().getDimensionPixelSize(17105089);
        this.mOppoCustomMinSizeOfResizeableTask = this.mService.mContext.getResources().getDimensionPixelSize(201655553);
    }

    private void handleDisplayRemoved(int displayId) {
        synchronized (this.mService) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                ActivityDisplay activityDisplay = (ActivityDisplay) this.mActivityDisplays.get(displayId);
                if (activityDisplay != null) {
                    ArrayList<ActivityStack> stacks = activityDisplay.mStacks;
                    for (int stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
                        ((ActivityStack) stacks.get(stackNdx)).mActivityContainer.detachLocked();
                    }
                    this.mActivityDisplays.remove(displayId);
                }
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
        this.mWindowManager.onDisplayRemoved(displayId);
    }

    private void handleDisplayChanged(int displayId) {
        synchronized (this.mService) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                if (((ActivityDisplay) this.mActivityDisplays.get(displayId)) != null) {
                }
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
        this.mWindowManager.onDisplayChanged(displayId);
    }

    private StackInfo getStackInfoLocked(ActivityStack stack) {
        int indexOf;
        ActivityDisplay display = (ActivityDisplay) this.mActivityDisplays.get(0);
        StackInfo info = new StackInfo();
        this.mWindowManager.getStackBounds(stack.mStackId, info.bounds);
        info.displayId = 0;
        info.stackId = stack.mStackId;
        info.userId = stack.mCurrentUser;
        info.visible = stack.getStackVisibilityLocked(null) == 1;
        if (display != null) {
            indexOf = display.mStacks.indexOf(stack);
        } else {
            indexOf = 0;
        }
        info.position = indexOf;
        ArrayList<TaskRecord> tasks = stack.getAllTasks();
        int numTasks = tasks.size();
        int[] taskIds = new int[numTasks];
        String[] taskNames = new String[numTasks];
        Rect[] taskBounds = new Rect[numTasks];
        int[] taskUserIds = new int[numTasks];
        for (int i = 0; i < numTasks; i++) {
            String flattenToString;
            TaskRecord task = (TaskRecord) tasks.get(i);
            taskIds[i] = task.taskId;
            if (task.origActivity != null) {
                flattenToString = task.origActivity.flattenToString();
            } else if (task.realActivity != null) {
                flattenToString = task.realActivity.flattenToString();
            } else if (task.getTopActivity() != null) {
                flattenToString = task.getTopActivity().packageName;
            } else {
                flattenToString = "unknown";
            }
            taskNames[i] = flattenToString;
            taskBounds[i] = new Rect();
            this.mWindowManager.getTaskBounds(task.taskId, taskBounds[i]);
            taskUserIds[i] = task.userId;
        }
        info.taskIds = taskIds;
        info.taskNames = taskNames;
        info.taskBounds = taskBounds;
        info.taskUserIds = taskUserIds;
        ActivityRecord top = stack.topRunningActivityLocked();
        info.topActivity = top != null ? top.intent.getComponent() : null;
        return info;
    }

    StackInfo getStackInfoLocked(int stackId) {
        ActivityStack stack = getStack(stackId);
        if (stack != null) {
            return getStackInfoLocked(stack);
        }
        return null;
    }

    ArrayList<StackInfo> getAllStackInfosLocked() {
        ArrayList<StackInfo> list = new ArrayList();
        for (int displayNdx = 0; displayNdx < this.mActivityDisplays.size(); displayNdx++) {
            ArrayList<ActivityStack> stacks = ((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks;
            for (int ndx = stacks.size() - 1; ndx >= 0; ndx--) {
                list.add(getStackInfoLocked((ActivityStack) stacks.get(ndx)));
            }
        }
        return list;
    }

    TaskRecord getLockedTaskLocked() {
        int top = this.mLockTaskModeTasks.size() - 1;
        if (top >= 0) {
            return (TaskRecord) this.mLockTaskModeTasks.get(top);
        }
        return null;
    }

    boolean isLockedTask(TaskRecord task) {
        return this.mLockTaskModeTasks.contains(task);
    }

    boolean isLastLockedTask(TaskRecord task) {
        return this.mLockTaskModeTasks.size() == 1 ? this.mLockTaskModeTasks.contains(task) : false;
    }

    void removeLockedTaskLocked(TaskRecord task) {
        if (this.mLockTaskModeTasks.remove(task)) {
            if (ActivityManagerDebugConfig.DEBUG_LOCKTASK) {
                Slog.w(TAG_LOCKTASK, "removeLockedTaskLocked: removed " + task);
            }
            if (this.mLockTaskModeTasks.isEmpty()) {
                if (ActivityManagerDebugConfig.DEBUG_LOCKTASK) {
                    Slog.d(TAG_LOCKTASK, "removeLockedTask: task=" + task + " last task, reverting locktask mode. Callers=" + Debug.getCallers(3));
                }
                Message lockTaskMsg = Message.obtain();
                lockTaskMsg.arg1 = task.userId;
                lockTaskMsg.what = 110;
                this.mHandler.sendMessage(lockTaskMsg);
            }
        }
    }

    void handleNonResizableTaskIfNeeded(TaskRecord task, int preferredStackId, int actualStackId) {
        handleNonResizableTaskIfNeeded(task, preferredStackId, actualStackId, false);
    }

    void handleNonResizableTaskIfNeeded(TaskRecord task, int preferredStackId, int actualStackId, boolean forceNonResizable) {
        if (!isStackDockedInEffect(actualStackId) && preferredStackId != 3) {
            return;
        }
        if (this.mService.mFocusedActivity != null && this.mService.mFocusedActivity.shortComponentName != null && this.mService.mFocusedActivity.shortComponentName.contains(OPPO_SAFECENTER_PASSWORD)) {
            resizeStackLocked(actualStackId, null, null, null, true, true, true);
            moveTasksToFullscreenStackLocked(actualStackId, true);
        } else if (task.canGoInDockedStack() && actualStackId == 1 && task != null) {
            ActivityStack stack = getStack(3, false, false);
            if (!(stack == null || stack.mFullscreen || !task.mFullscreen)) {
                Rect resetRect = new Rect();
                this.mWindowManager.getStackDockedModeBounds(0, resetRect, true);
                resizeStackLocked(actualStackId, resetRect, null, null, true, true, false);
            }
        } else if (!task.canGoInDockedStack() || this.mLastFocusedStack == null || this.mLastFocusedStack.getTopAppName() == null || !this.mLastFocusedStack.getTopAppName().toString().contains(OPPO_SAFECENTER_PASSWORD)) {
            ActivityRecord topActivity = task.getTopActivity();
            if (!task.canGoInDockedStack() || forceNonResizable) {
                this.mService.mHandler.sendEmptyMessage(68);
                this.mWindowManager.notifyDismissDockedStatckFromHome();
                moveTasksToFullscreenStackLocked(3, actualStackId == 3);
                if (this.mService.mConfiguration != null && this.mService.mConfiguration.orientation == 1) {
                    this.mWindowManager.setFreeingChange(true);
                    this.mWindowManager.startFreezingScreen(0, 0);
                }
            } else if (!(topActivity == null || !topActivity.isNonResizableOrForced() || topActivity.noDisplay)) {
                this.mService.mHandler.obtainMessage(67, task.taskId, 0, topActivity.appInfo.packageName).sendToTarget();
            }
        } else {
            resizeDockedStackLocked(null, null, null, null, null, true, true);
        }
    }

    void showLockTaskToast() {
        if (this.mLockTaskNotify != null) {
            this.mLockTaskNotify.showToast(this.mLockTaskModeState);
        }
    }

    void showLockTaskEscapeMessageLocked(TaskRecord task) {
        if (this.mLockTaskModeTasks.contains(task)) {
            this.mHandler.sendEmptyMessage(113);
        }
    }

    void setLockTaskModeLocked(TaskRecord task, int lockTaskModeState, String reason, boolean andResume) {
        boolean z = true;
        if (task == null) {
            TaskRecord lockedTask = getLockedTaskLocked();
            if (lockedTask != null) {
                removeLockedTaskLocked(lockedTask);
                if (!this.mLockTaskModeTasks.isEmpty()) {
                    if (ActivityManagerDebugConfig.DEBUG_LOCKTASK) {
                        Slog.w(TAG_LOCKTASK, "setLockTaskModeLocked: Tasks remaining, can't unlock");
                    }
                    lockedTask.performClearTaskLocked();
                    resumeFocusedStackTopActivityLocked();
                    return;
                }
            }
            if (ActivityManagerDebugConfig.DEBUG_LOCKTASK) {
                Slog.w(TAG_LOCKTASK, "setLockTaskModeLocked: No tasks to unlock. Callers=" + Debug.getCallers(4));
            }
        } else if (task.mLockTaskAuth == 0) {
            if (ActivityManagerDebugConfig.DEBUG_LOCKTASK) {
                Slog.w(TAG_LOCKTASK, "setLockTaskModeLocked: Can't lock due to auth");
            }
        } else if (isLockTaskModeViolation(task)) {
            Slog.e(TAG_LOCKTASK, "setLockTaskMode: Attempt to start an unauthorized lock task.");
        } else {
            if (this.mLockTaskModeTasks.isEmpty()) {
                Message lockTaskMsg = Message.obtain();
                lockTaskMsg.obj = task.intent.getComponent().getPackageName();
                lockTaskMsg.arg1 = task.userId;
                lockTaskMsg.what = 109;
                lockTaskMsg.arg2 = lockTaskModeState;
                this.mHandler.sendMessage(lockTaskMsg);
            }
            if (ActivityManagerDebugConfig.DEBUG_LOCKTASK) {
                Slog.w(TAG_LOCKTASK, "setLockTaskModeLocked: Locking to " + task + " Callers=" + Debug.getCallers(4));
            }
            this.mLockTaskModeTasks.remove(task);
            this.mLockTaskModeTasks.add(task);
            if (task.mLockTaskUid == -1) {
                task.mLockTaskUid = task.effectiveUid;
            }
            if (andResume) {
                if (lockTaskModeState == 0) {
                    z = false;
                }
                findTaskToMoveToFrontLocked(task, 0, null, reason, z);
                resumeFocusedStackTopActivityLocked();
            } else if (lockTaskModeState != 0) {
                handleNonResizableTaskIfNeeded(task, -1, task.stack.mStackId, true);
            }
        }
    }

    boolean isLockTaskModeViolation(TaskRecord task) {
        return isLockTaskModeViolation(task, false);
    }

    boolean isLockTaskModeViolation(TaskRecord task, boolean isNewClearTask) {
        boolean z = false;
        if (getLockedTaskLocked() == task && !isNewClearTask) {
            return false;
        }
        int lockTaskAuth = task.mLockTaskAuth;
        if (lockTaskAuth == 1 && task.intent != null && "android.intent.action.CHOOSER".equals(task.intent.getAction()) && "coloros_multiapp_chooser".equals(task.affinity)) {
            return false;
        }
        switch (lockTaskAuth) {
            case 0:
                if (!this.mLockTaskModeTasks.isEmpty()) {
                    z = true;
                }
                return z;
            case 1:
                if (!this.mLockTaskModeTasks.isEmpty()) {
                    z = true;
                }
                return z;
            case 2:
            case 3:
            case 4:
                return false;
            default:
                Slog.w(TAG, "isLockTaskModeViolation: invalid lockTaskAuth value=" + lockTaskAuth);
                return true;
        }
    }

    void onLockTaskPackagesUpdatedLocked() {
        boolean didSomething = false;
        for (int taskNdx = this.mLockTaskModeTasks.size() - 1; taskNdx >= 0; taskNdx--) {
            TaskRecord lockedTask = (TaskRecord) this.mLockTaskModeTasks.get(taskNdx);
            boolean wasWhitelisted = lockedTask.mLockTaskAuth != 2 ? lockedTask.mLockTaskAuth == 3 : true;
            lockedTask.setLockTaskAuth();
            boolean isWhitelisted = lockedTask.mLockTaskAuth != 2 ? lockedTask.mLockTaskAuth == 3 : true;
            if (wasWhitelisted && !isWhitelisted) {
                if (ActivityManagerDebugConfig.DEBUG_LOCKTASK) {
                    Slog.d(TAG_LOCKTASK, "onLockTaskPackagesUpdated: removing " + lockedTask + " mLockTaskAuth=" + lockedTask.lockTaskAuthToString());
                }
                removeLockedTaskLocked(lockedTask);
                lockedTask.performClearTaskLocked();
                didSomething = true;
            }
        }
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            ArrayList<ActivityStack> stacks = ((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks;
            for (int stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
                ((ActivityStack) stacks.get(stackNdx)).onLockTaskPackagesUpdatedLocked();
            }
        }
        ActivityRecord r = topRunningActivityLocked();
        TaskRecord task = r != null ? r.task : null;
        if (this.mLockTaskModeTasks.isEmpty() && task != null && task.mLockTaskAuth == 2) {
            if (ActivityManagerDebugConfig.DEBUG_LOCKTASK) {
                Slog.d(TAG_LOCKTASK, "onLockTaskPackagesUpdated: starting new locktask task=" + task);
            }
            setLockTaskModeLocked(task, 1, "package updated", false);
            didSomething = true;
        }
        if (didSomething) {
            resumeFocusedStackTopActivityLocked();
        }
    }

    int getLockTaskModeState() {
        return this.mLockTaskModeState;
    }

    void activityRelaunchedLocked(IBinder token) {
        this.mWindowManager.notifyAppRelaunchingFinished(token);
        if (this.mService.isSleepingOrShuttingDownLocked()) {
            ActivityRecord r = ActivityRecord.isInStackLocked(token);
            if (r != null && this.mService.mIgnoreSleepCheckLater && this.mService.mFocusedActivity == r) {
                Slog.d(TAG, "activityRelaunchedLocked focus activity not need sleeping");
            } else if (r != null) {
                r.setSleeping(true, true);
            }
        }
    }

    void activityRelaunchingLocked(ActivityRecord r) {
        this.mWindowManager.notifyAppRelaunching(r.appToken);
    }

    void logStackState() {
        this.mActivityMetricsLogger.logWindowState();
    }

    void scheduleReportMultiWindowModeChanged(TaskRecord task) {
        for (int i = task.mActivities.size() - 1; i >= 0; i--) {
            ActivityRecord r = (ActivityRecord) task.mActivities.get(i);
            if (!(r.app == null || r.app.thread == null)) {
                this.mMultiWindowModeChangedActivities.add(r);
            }
        }
        if (!this.mHandler.hasMessages(114)) {
            this.mHandler.sendEmptyMessage(114);
        }
    }

    void scheduleReportPictureInPictureModeChangedIfNeeded(TaskRecord task, ActivityStack prevStack) {
        ActivityStack stack = task.stack;
        if (prevStack != null && prevStack != stack && (prevStack.mStackId == 4 || stack.mStackId == 4)) {
            for (int i = task.mActivities.size() - 1; i >= 0; i--) {
                ActivityRecord r = (ActivityRecord) task.mActivities.get(i);
                if (!(r.app == null || r.app.thread == null)) {
                    this.mPipModeChangedActivities.add(r);
                }
            }
            if (!this.mHandler.hasMessages(115)) {
                this.mHandler.sendEmptyMessage(115);
            }
        }
    }

    void setDockedStackMinimized(boolean minimized) {
        this.mIsDockMinimized = minimized;
        if (!minimized) {
            ActivityStack dockedStack = getStack(3);
            if (dockedStack != null) {
                ActivityRecord top = dockedStack.topRunningActivityLocked();
                if (top != null && this.mService.mUserController.shouldConfirmCredentials(top.userId)) {
                    this.mService.mActivityStarter.showConfirmDeviceCredential(top.userId);
                }
            }
        }
    }

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "ZhiYong.Lin@Plf.Framework modify for BPM", property = OppoRomType.ROM)
    final ActivityRecord getTopRunningActivityLocked() {
        ActivityRecord topRunningActivityLocked;
        synchronized (this.mService) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                topRunningActivityLocked = topRunningActivityLocked();
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
        return topRunningActivityLocked;
    }

    public ComponentName getTopAppName() {
        return getFocusedStack().getTopAppName();
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

    ActivityStack findStackBehind(ActivityStack stack) {
        ActivityDisplay display = (ActivityDisplay) this.mActivityDisplays.get(0);
        if (display == null) {
            return null;
        }
        ArrayList<ActivityStack> stacks = display.mStacks;
        int i = stacks.size() - 1;
        while (i >= 0) {
            if (stacks.get(i) == stack && i > 0) {
                return (ActivityStack) stacks.get(i - 1);
            }
            i--;
        }
        throw new IllegalStateException("Failed to find a stack behind stack=" + stack + " in=" + stacks);
    }

    private void setResizingDuringAnimation(int taskId) {
        this.mResizingTasksDuringAnimation.add(Integer.valueOf(taskId));
        this.mWindowManager.setTaskDockedResizing(taskId, true);
    }

    final int startActivityFromRecentsInner(int taskId, Bundle bOptions) {
        ActivityOptions activityOptions;
        if (bOptions != null) {
            ActivityOptions activityOptions2 = new ActivityOptions(bOptions);
        } else {
            activityOptions = null;
        }
        int launchStackId = activityOptions != null ? activityOptions.getLaunchStackId() : -1;
        if (!ActivityManagerService.IS_USER_BUILD || ActivityManagerDebugConfig.DEBUG_MULTIWINDOW) {
            Slog.d(TAG, "startActivityFromRecentsInner: launchStackId=" + launchStackId + " taskId=" + taskId);
        }
        if (launchStackId == 0) {
            throw new IllegalArgumentException("startActivityFromRecentsInner: Task " + taskId + " can't be launch in the home stack.");
        }
        if (launchStackId == 3) {
            this.mWindowManager.setDockedStackCreateState(activityOptions.getDockCreateMode(), null);
            deferUpdateBounds(0);
            this.mWindowManager.prepareAppTransition(19, false);
        }
        TaskRecord task = anyTaskForIdLocked(taskId, true, launchStackId);
        if (task == null) {
            continueUpdateBounds(0);
            this.mWindowManager.executeAppTransition();
            throw new IllegalArgumentException("startActivityFromRecentsInner: Task " + taskId + " not found.");
        }
        if (!this.mService.getDisallowedRunningList().isEmpty()) {
            ActivityRecord r = task.getTopActivity();
            if (!(r == null || this.mService == null || !this.mService.isInDisallowedRunningAppList(r.packageName))) {
                continueUpdateBounds(0);
                this.mWindowManager.executeAppTransition();
                this.mService.showDisallowedRunningAppDialog();
                return 0;
            }
        }
        ActivityStack focusedStack = getFocusedStack();
        ActivityRecord sourceRecord = focusedStack != null ? focusedStack.topActivity() : null;
        if (!(launchStackId == -1 || task.stack.mStackId == launchStackId)) {
            moveTaskToStackLocked(taskId, launchStackId, true, true, "startActivityFromRecents", true);
        }
        if (this.mService.mUserController.shouldConfirmCredentials(task.userId) || task.getRootActivity() == null) {
            int callingUid = task.mCallingUid;
            String callingPackage = task.mCallingPackage;
            Intent intent = task.intent;
            intent.addFlags(DumpState.DUMP_DEXOPT);
            int result = this.mService.startActivityInPackage(callingUid, callingPackage, intent, null, null, null, 0, 0, bOptions, task.userId, null, task);
            if (launchStackId == 3) {
                setResizingDuringAnimation(task.taskId);
            }
            if (ActivityManagerDebugConfig.DEBUG_MULTIWINDOW) {
                Slog.d(TAG, "startActivityFromRecentsInner: result=" + result + ", t.uid=" + task.mCallingUid + ", t.pkg=" + task.mCallingPackage + ", t.userid=" + task.userId + ", t.intent=" + task.intent, new Throwable());
            }
            return result;
        }
        this.mService.mActivityStarter.sendPowerHintForLaunchStartIfNeeded(true);
        this.mActivityMetricsLogger.notifyActivityLaunching();
        this.mService.moveTaskToFrontLocked(task.taskId, 0, bOptions);
        this.mActivityMetricsLogger.notifyActivityLaunched(2, task.getTopActivity());
        if (launchStackId == 3) {
            setResizingDuringAnimation(taskId);
        }
        this.mService.mActivityStarter.postStartActivityUncheckedProcessing(task.getTopActivity(), 2, sourceRecord != null ? sourceRecord.task.stack.mStackId : -1, sourceRecord, task.stack);
        if (ActivityManagerDebugConfig.DEBUG_MULTIWINDOW) {
            Slog.d(TAG, "startActivityFromRecentsInner: START_TASK_TO_FRONT , t.uid=" + task.mCallingUid + ", t.pkg=" + task.mCallingPackage + ", t.userid=" + task.userId + ", t.intent=" + task.intent, new Throwable());
        }
        return 2;
    }

    public List<IBinder> getTopVisibleActivities() {
        ActivityDisplay display = (ActivityDisplay) this.mActivityDisplays.get(0);
        if (display == null) {
            return Collections.EMPTY_LIST;
        }
        ArrayList<IBinder> topActivityTokens = new ArrayList();
        ArrayList<ActivityStack> stacks = display.mStacks;
        for (int i = stacks.size() - 1; i >= 0; i--) {
            ActivityStack stack = (ActivityStack) stacks.get(i);
            if (stack.getStackVisibilityLocked(null) == 1) {
                ActivityRecord top = stack.topActivity();
                if (top != null) {
                    if (stack == this.mFocusedStack) {
                        topActivityTokens.add(0, top.appToken);
                    } else {
                        topActivityTokens.add(top.appToken);
                    }
                }
            }
        }
        return topActivityTokens;
    }

    public boolean isUpdatedLastActivityWhenStartHome(String packageName, String activityName) {
        if (this.mLastResumedActivity.activityType != 1 || packageName == null || activityName == null) {
            return false;
        }
        return (packageName.equals(this.mLastResumedActivity.packageName) && activityName.equals(this.mLastResumedActivity.activityName)) ? false : true;
    }

    public ActivityStack findStack(int stackId) {
        ActivityDisplay display = (ActivityDisplay) this.mActivityDisplays.get(0);
        if (display == null) {
            return null;
        }
        ArrayList<ActivityStack> stacks = display.mStacks;
        if (MultiWindowManager.DEBUG) {
            Slog.d(TAG_STACK, "findStack, stacks = " + stacks);
        }
        for (ActivityStack stack : stacks) {
            if (stack.mStackId == stackId) {
                return stack;
            }
        }
        return null;
    }

    private void forceStopWaitingVisibleActivitiesLocked(int stopIndex, ActivityRecord s) {
        if (this.mStoppingActivities.size() > ActivityStack.getMaxStoppingToForce()) {
            int forceIndex = (this.mStoppingActivities.size() - 1) - ActivityStack.getMaxStoppingToForce();
            if (ActivityManagerDebugConfig.DEBUG_STATES) {
                Slog.d(TAG, "Force stop waitingVisible activity? [" + stopIndex + "," + forceIndex + "]");
            }
            if (stopIndex <= forceIndex && this.mWaitingVisibleActivities.remove(s) && ActivityManagerDebugConfig.DEBUG_STATES) {
                Slog.d(TAG, "Force stop waitingVisible activity: " + s);
            }
        }
    }

    void collectionStartUrlInfo(IApplicationThread caller, ProcessRecord callerApp, int pid, int uid, String callingPackage, Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            String url = intent.getDataString();
            if (action != null && action.equals("android.intent.action.VIEW") && url != null && url.contains("http")) {
                boolean isFore = false;
                String cpnPkgName = IElsaManager.EMPTY_PACKAGE;
                String cpnClassName = IElsaManager.EMPTY_PACKAGE;
                ComponentName cpn = intent.getComponent();
                if (cpn != null) {
                    cpnPkgName = cpn.getPackageName();
                    cpnClassName = cpn.getClassName();
                }
                if (callingPackage != null && callingPackage.equals(getTopPackageName())) {
                    isFore = true;
                }
                if (callingPackage != null && !callingPackage.equals(cpnPkgName)) {
                    sendBroadcastForStartUrlInfo(callingPackage, url, cpnPkgName, cpnClassName, isFore);
                }
            }
        }
    }

    private void sendBroadcastForStartUrlInfo(String callingPkg, String url, String pkgName, String className, boolean isFore) {
        Slog.d(TAG, "sendBroadcastForStartUrlInfo callingPkg = " + callingPkg + "  url = " + url + "  pkgName = " + pkgName + "  className= " + className + "  isFore = " + isFore);
        Intent intent = new Intent("android.intent.action.OPPO_SAFE_COUNT_START_URL");
        intent.putExtra("caller", callingPkg);
        intent.putExtra("url", url);
        intent.putExtra("pkgName", pkgName);
        intent.putExtra("className", className);
        intent.putExtra("isFore", isFore);
        this.mService.mContext.sendBroadcast(intent);
    }

    private String getTopPackageName() {
        String topPkgName = IElsaManager.EMPTY_PACKAGE;
        ComponentName topCpn = getFocusedStack().mComponentName;
        if (topCpn != null) {
            return topCpn.getPackageName();
        }
        return topPkgName;
    }

    void dataCollectionInfoExp(ProcessRecord callerApp, String callingPackage, Intent intent) {
        String pkgInstallerExp = "com.google.android.packageinstaller";
        String pkgInstallerActExp = "com.android.packageinstaller.PackageInstallerActivity";
        if (intent != null && intent.getComponent() != null) {
            ComponentName realActivity = intent.getComponent();
            if (realActivity != null && pkgInstallerActExp.equals(realActivity.getClassName()) && callerApp != null && callerApp.info.processName != null) {
                String callAppName = callerApp.info.processName;
                if (!pkgInstallerExp.equals(callAppName)) {
                    Slog.d(TAG, "dataCollectionInfoExp: callAppName" + callAppName + " callingPackage " + callingPackage);
                    intent.putExtra("android.intent.extra.INSTALLER_PACKAGE_NAME", callAppName);
                }
            }
        }
    }

    public ComponentName getDockTopAppName() {
        ActivityContainer activityContainer = (ActivityContainer) this.mActivityContainers.get(3);
        ActivityStack stack = null;
        if (activityContainer != null) {
            stack = activityContainer.mStack;
        }
        if (stack != null) {
            return stack.getDockTopAppName();
        }
        return null;
    }

    protected boolean getAllowDockedStackResize() {
        return this.mAllowDockedStackResize;
    }
}
