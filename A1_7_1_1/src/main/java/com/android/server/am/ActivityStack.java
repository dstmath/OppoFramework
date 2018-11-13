package com.android.server.am;

import android.annotation.OppoHook;
import android.annotation.OppoHook.OppoHookType;
import android.annotation.OppoHook.OppoRomType;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.ActivityManager.StackId;
import android.app.ActivityOptions;
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
import android.graphics.Bitmap;
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
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.os.UserHandle;
import android.service.voice.IVoiceInteractionSession;
import android.text.format.Time;
import android.util.ArraySet;
import android.util.EventLog;
import android.util.Log;
import android.util.Slog;
import com.android.internal.app.IVoiceInteractor;
import com.android.internal.os.BatteryStatsImpl;
import com.android.internal.os.BatteryStatsImpl.Uid.Proc;
import com.android.server.LocationManagerService;
import com.android.server.Watchdog;
import com.android.server.coloros.OppoListManager;
import com.android.server.oppo.IElsaManager;
import com.android.server.wm.TaskGroup;
import com.android.server.wm.WindowManagerService;
import com.mediatek.am.AMEventHookAction;
import com.mediatek.am.AMEventHookData.AfterActivityDestroyed;
import com.mediatek.am.AMEventHookData.AfterActivityPaused;
import com.mediatek.am.AMEventHookData.AfterActivityResumed;
import com.mediatek.am.AMEventHookData.AfterActivityStopped;
import com.mediatek.am.AMEventHookData.BeforeActivitySwitch;
import com.mediatek.am.AMEventHookData.BeforeGoHomeWhenNoActivities;
import com.mediatek.am.AMEventHookData.PackageStoppedStatusChanged;
import com.mediatek.am.AMEventHookData.SkipStartActivity;
import com.mediatek.am.AMEventHookResult;
import com.mediatek.am.IAWSProcessRecord;
import com.mediatek.multiwindow.MultiWindowManager;
import com.mediatek.server.am.AMEventHook.Event;
import com.oppo.hypnus.Hypnus;
import com.oppo.oiface.OifaceUtil;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import oppo.util.OppoMultiLauncherUtil;

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
final class ActivityStack {
    /* renamed from: -com-android-server-am-ActivityStack$ActivityStateSwitchesValues */
    private static final /* synthetic */ int[] f6-com-android-server-am-ActivityStack$ActivityStateSwitchesValues = null;
    static final long ACTIVITY_INACTIVE_RESET_TIME = 0;
    static final int DESTROY_ACTIVITIES_MSG = 105;
    static final int DESTROY_TIMEOUT = 10000;
    static final int DESTROY_TIMEOUT_MSG = 102;
    static final int FINISH_AFTER_PAUSE = 1;
    static final int FINISH_AFTER_VISIBLE = 2;
    static final int FINISH_IMMEDIATELY = 0;
    static final int LAUNCH_TICK = 500;
    static final int LAUNCH_TICK_MSG = 103;
    private static final int MAX_STOPPING_TO_FORCE = 3;
    private static final String MM_WEB_UI = "com.tencent.mm.plugin.webview.ui.tools.WebViewUI";
    private static final String OPPO_SCREENSHOT_DISABLE = "oppo.recents.smallicon";
    private static final String OPPO_SECURITYPAY_FEATURE = "oppo.securitypay.support";
    static final int PAUSE_TIMEOUT = 500;
    static final int PAUSE_TIMEOUT_MSG = 101;
    static final int RELEASE_BACKGROUND_RESOURCES_TIMEOUT_MSG = 107;
    static final int REMOVE_TASK_MODE_DESTROYING = 0;
    static final int REMOVE_TASK_MODE_MOVING = 1;
    static final int REMOVE_TASK_MODE_MOVING_TO_TOP = 2;
    static final boolean SHOW_APP_STARTING_PREVIEW = true;
    static final int STACK_INVISIBLE = 0;
    static final int STACK_VISIBLE = 1;
    static final int STACK_VISIBLE_ACTIVITY_BEHIND = 2;
    static final long START_WARN_TIME = 5000;
    static final int STOP_TIMEOUT = 10000;
    static final int STOP_TIMEOUT_MSG = 104;
    private static final String SWAP_DOCKED_STACK = "swapDockedAndFullscreenStack";
    private static final String TAG = null;
    private static final String TAG_ADD_REMOVE = null;
    private static final String TAG_APP = null;
    private static final String TAG_CLEANUP = null;
    private static final String TAG_CONFIGURATION = null;
    private static final String TAG_CONTAINERS = null;
    private static final String TAG_PAUSE = null;
    private static final String TAG_RELEASE = null;
    private static final String TAG_RESULTS = null;
    private static final String TAG_SAVED_STATE = null;
    private static final String TAG_SCREENSHOTS = null;
    private static final String TAG_STACK = null;
    private static final String TAG_STATES = null;
    private static final String TAG_SWITCH = null;
    private static final String TAG_TASKS = null;
    private static final String TAG_TRANSITION = null;
    private static final String TAG_USER_LEAVING = null;
    private static final String TAG_VISIBILITY = null;
    static final long TRANSLUCENT_CONVERSION_TIMEOUT = 2000;
    static final int TRANSLUCENT_TIMEOUT_MSG = 106;
    private static final boolean VALIDATE_TOKENS = false;
    private static final int VISIABLE_ACTIVITY_LAUNCH_MAX = 10;
    public static Hypnus mHyp;
    final ActivityContainer mActivityContainer;
    Rect mBounds;
    ComponentName mComponentName;
    boolean mConfigWillChange;
    int mCurrentUser;
    final Rect mDeferredBounds;
    final Rect mDeferredTaskBounds;
    final Rect mDeferredTaskInsetBounds;
    int mDisplayId;
    ComponentName mDockComponentName;
    boolean mFullscreen;
    long mFullyDrawnStartTime;
    final Handler mHandler;
    final ArrayList<ActivityRecord> mLRUActivities;
    ActivityRecord mLastNoHistoryActivity;
    ActivityRecord mLastPausedActivity;
    private ComponentName mLastRecordCmpName;
    private String mLastRecordPkgName;
    ActivityRecord mLastStartedActivity;
    long mLaunchStartTime;
    final ArrayList<ActivityRecord> mNoAnimActivities;
    ActivityRecord mPausingActivity;
    private final RecentTasks mRecentTasks;
    ActivityRecord mResumedActivity;
    private boolean mScreenShotDisabled;
    final ActivityManagerService mService;
    final int mStackId;
    final ActivityStackSupervisor mStackSupervisor;
    ArrayList<ActivityStack> mStacks;
    private final ArrayList<TaskRecord> mTaskHistory;
    private final LaunchingTaskPositioner mTaskPositioner;
    ActivityRecord mTranslucentActivityWaiting;
    private ArrayList<ActivityRecord> mUndrawnActivitiesBelowTopTranslucent;
    boolean mUpdateBoundsDeferred;
    boolean mUpdateBoundsDeferredCalled;
    final ArrayList<TaskGroup> mValidateAppTokens;
    final WindowManagerService mWindowManager;

    final class ActivityStackHandler extends Handler {
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
                                ActivityStack.this.activityStoppedLocked(r, null, null, null);
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
                case 107:
                    synchronized (ActivityStack.this.mService) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            r = ActivityStack.this.getVisibleBehindActivity();
                            Slog.e(ActivityStack.TAG, "Timeout waiting for cancelVisibleBehind player=" + r);
                            if (r != null) {
                                ActivityStack.this.mService.killAppAtUsersRequest(r.app, null);
                            }
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

    /*  JADX ERROR: NullPointerException in pass: EnumVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.EnumVisitor.visit(EnumVisitor.java:102)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    enum ActivityState {
        ;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.am.ActivityStack.ActivityState.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 10 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.am.ActivityStack.ActivityState.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.am.ActivityStack.ActivityState.<clinit>():void");
        }
    }

    static class ScheduleDestroyArgs {
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
    private static /* synthetic */ int[] m17x775af271() {
        if (f6-com-android-server-am-ActivityStack$ActivityStateSwitchesValues != null) {
            return f6-com-android-server-am-ActivityStack$ActivityStateSwitchesValues;
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
        f6-com-android-server-am-ActivityStack$ActivityStateSwitchesValues = iArr;
        return iArr;
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.am.ActivityStack.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.am.ActivityStack.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.am.ActivityStack.<clinit>():void");
    }

    int numActivities() {
        int count = 0;
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            count += ((TaskRecord) this.mTaskHistory.get(taskNdx)).mActivities.size();
        }
        return count;
    }

    ActivityStack(ActivityContainer activityContainer, RecentTasks recentTasks) {
        LaunchingTaskPositioner launchingTaskPositioner = null;
        this.mLastRecordCmpName = null;
        this.mLastRecordPkgName = null;
        this.mTaskHistory = new ArrayList();
        this.mValidateAppTokens = new ArrayList();
        this.mLRUActivities = new ArrayList();
        this.mNoAnimActivities = new ArrayList();
        this.mPausingActivity = null;
        this.mLastPausedActivity = null;
        this.mLastNoHistoryActivity = null;
        this.mResumedActivity = null;
        this.mLastStartedActivity = null;
        this.mTranslucentActivityWaiting = null;
        this.mUndrawnActivitiesBelowTopTranslucent = new ArrayList();
        this.mFullscreen = true;
        this.mBounds = null;
        this.mDeferredBounds = new Rect();
        this.mDeferredTaskBounds = new Rect();
        this.mDeferredTaskInsetBounds = new Rect();
        this.mLaunchStartTime = 0;
        this.mFullyDrawnStartTime = 0;
        this.mScreenShotDisabled = false;
        this.mActivityContainer = activityContainer;
        this.mStackSupervisor = activityContainer.getOuter();
        this.mService = this.mStackSupervisor.mService;
        this.mScreenShotDisabled = this.mService.mContext.getPackageManager().hasSystemFeature(OPPO_SCREENSHOT_DISABLE);
        this.mHandler = new ActivityStackHandler(this.mService.mHandler.getLooper());
        this.mWindowManager = this.mService.mWindowManager;
        this.mStackId = activityContainer.mStackId;
        this.mCurrentUser = this.mService.mUserController.getCurrentUserIdLocked();
        this.mRecentTasks = recentTasks;
        if (this.mStackId == 2) {
            launchingTaskPositioner = new LaunchingTaskPositioner();
        }
        this.mTaskPositioner = launchingTaskPositioner;
    }

    void attachDisplay(ActivityDisplay activityDisplay, boolean onTop) {
        this.mDisplayId = activityDisplay.mDisplayId;
        this.mStacks = activityDisplay.mStacks;
        this.mBounds = this.mWindowManager.attachStack(this.mStackId, activityDisplay.mDisplayId, onTop);
        if (this.mFullscreen && this.mBounds != null && this.mStackId == 1 && !this.mStackSupervisor.getAllowDockedStackResize()) {
            this.mStackSupervisor.resizeStackLocked(this.mStackId, null, null, null, true, true, true);
            Slog.v(TAG, "attachDisplay set the full screen status");
        }
        this.mFullscreen = this.mBounds == null;
        if (this.mTaskPositioner != null) {
            this.mTaskPositioner.setDisplay(activityDisplay.mDisplay);
            this.mTaskPositioner.configure(this.mBounds);
        }
        if (this.mStackId == 3) {
            this.mStackSupervisor.resizeDockedStackLocked(this.mBounds, null, null, null, null, true);
        }
    }

    void detachDisplay() {
        this.mDisplayId = -1;
        this.mStacks = null;
        if (this.mTaskPositioner != null) {
            this.mTaskPositioner.reset();
        }
        this.mWindowManager.detachStack(this.mStackId);
        if (this.mStackId == 3) {
            this.mStackSupervisor.resizeDockedStackLocked(null, null, null, null, null, true);
        }
    }

    public void getDisplaySize(Point out) {
        this.mActivityContainer.mActivityDisplay.mDisplay.getSize(out);
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
            ActivityStackSupervisor activityStackSupervisor = this.mStackSupervisor;
            Rect rect2 = this.mDeferredBounds.isEmpty() ? null : this.mDeferredBounds;
            Rect rect3 = this.mDeferredTaskBounds.isEmpty() ? null : this.mDeferredTaskBounds;
            if (!this.mDeferredTaskInsetBounds.isEmpty()) {
                rect = this.mDeferredTaskInsetBounds;
            }
            activityStackSupervisor.resizeStackUncheckedLocked(this, rect2, rect3, rect);
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

    boolean okToShowLocked(ActivityRecord r) {
        return this.mStackSupervisor.okToShowLocked(r);
    }

    final ActivityRecord topRunningActivityLocked() {
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            if (MultiWindowManager.isSupported()) {
                TaskRecord task = (TaskRecord) this.mTaskHistory.get(taskNdx);
                if (!(!task.mSticky || this.mService.mFocusedActivity == null || task.taskId == this.mService.mFocusedActivity.task.taskId)) {
                }
            }
            ActivityRecord r = ((TaskRecord) this.mTaskHistory.get(taskNdx)).topRunningActivityLocked();
            if (r != null) {
                return r;
            }
        }
        return null;
    }

    public TaskRecord topRunningTask() {
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            if (((TaskRecord) this.mTaskHistory.get(taskNdx)).topRunningActivityLocked() != null) {
                return (TaskRecord) this.mTaskHistory.get(taskNdx);
            }
        }
        return null;
    }

    final ActivityRecord topRunningNonDelayedActivityLocked(ActivityRecord notTop) {
        if (ActivityManagerDebugConfig.DEBUG_STATES) {
            Slog.i(TAG, "topRunningActivityLocked: mTaskHistory.size() " + this.mTaskHistory.size() + " mTaskHistory " + this.mTaskHistory + " " + " call by " + Debug.getCallers(8));
        }
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ArrayList<ActivityRecord> activities = ((TaskRecord) this.mTaskHistory.get(taskNdx)).mActivities;
            for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                ActivityRecord r = (ActivityRecord) activities.get(activityNdx);
                if (ActivityManagerDebugConfig.DEBUG_STATES) {
                    Slog.d(TAG, "topRunningActivityLocked: taskNdx " + taskNdx + " mTaskHistory.get(taskNdx) " + this.mTaskHistory.get(taskNdx) + " r " + r);
                }
                if (!r.finishing && !r.delayedResume && r != notTop && okToShowLocked(r)) {
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
                    if (!r.finishing && token != r.appToken && okToShowLocked(r)) {
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
        TaskRecord task = r.task;
        if (task == null || task.stack == null || !task.mActivities.contains(r) || !this.mTaskHistory.contains(task)) {
            return null;
        }
        if (task.stack != this) {
            Slog.w(TAG, "Illegal state! task does not point to stack it is in.");
        }
        return r;
    }

    final boolean updateLRUListLocked(ActivityRecord r) {
        boolean hadit = this.mLRUActivities.remove(r);
        this.mLRUActivities.add(r);
        return hadit;
    }

    final boolean isHomeStack() {
        return this.mStackId == 0;
    }

    final boolean isDockedStack() {
        return this.mStackId == 3;
    }

    final boolean isPinnedStack() {
        return this.mStackId == 4;
    }

    final boolean isOnHomeDisplay() {
        if (isAttached() && this.mActivityContainer.mActivityDisplay.mDisplayId == 0) {
            return true;
        }
        return false;
    }

    void moveToFront(String reason) {
        moveToFront(reason, null);
    }

    void moveToFront(String reason, TaskRecord task) {
        if (isAttached()) {
            this.mStacks.remove(this);
            int addIndex = this.mStacks.size();
            if (addIndex > 0) {
                ActivityStack topStack = (ActivityStack) this.mStacks.get(addIndex - 1);
                if (StackId.isAlwaysOnTop(topStack.mStackId) && topStack != this) {
                    addIndex--;
                }
            }
            this.mStacks.add(addIndex, this);
            if (isOnHomeDisplay()) {
                this.mStackSupervisor.setFocusStackUnchecked(reason, this);
            }
            if (task != null) {
                insertTaskAtTop(task, null);
            } else {
                task = topTask();
            }
            if (task != null) {
                this.mWindowManager.moveTaskToTop(task.taskId);
            }
            if (MultiWindowManager.isSupported()) {
                restoreStickyTaskLocked(task);
                keepStickyTaskLocked();
            }
        }
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
                ActivityRecord r = task.getTopActivity();
                boolean isSkip = true;
                if (r != null) {
                    isSkip = r.userId != userId;
                }
                if ((task != null && OppoMultiLauncherUtil.getInstance().isMultiApp(task.affinity)) || (r != null && OppoMultiLauncherUtil.getInstance().isMultiApp(r.packageName))) {
                    isSkip = false;
                }
                if (r != null && !r.finishing && !isSkip && r.launchMode != 3) {
                    if (r.mActivityType == target.mActivityType || target.shortComponentName.equals("com.oppo.launcher/.Launcher")) {
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
                            Slog.d(TAG_TASKS, "Comparing existing cls=" + taskIntent.getComponent().flattenToShortString() + "/aff=" + r.task.rootAffinity + " to new cls=" + intent.getComponent().flattenToShortString() + "/aff=" + info.taskAffinity);
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
                        } else if (!isDocument && !taskIsDocument && result.r == null && task.canMatchRootAffinity()) {
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
            TaskRecord task = (TaskRecord) this.mTaskHistory.get(taskNdx);
            boolean notCurrentUserTask = !this.mStackSupervisor.isCurrentProfileLocked(task.userId);
            ArrayList<ActivityRecord> activities = task.mActivities;
            for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                ActivityRecord r = (ActivityRecord) activities.get(activityNdx);
                if (!((notCurrentUserTask && (r.info.flags & 1024) == 0) || r.finishing || r.userId != userId)) {
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

    final void switchUserLocked(int userId) {
        if (this.mCurrentUser != userId) {
            this.mCurrentUser = userId;
            int index = this.mTaskHistory.size();
            int i = 0;
            while (i < index) {
                TaskRecord task = (TaskRecord) this.mTaskHistory.get(i);
                if (this.mStackSupervisor.isCurrentProfileLocked(task.userId) || task.topRunningActivityLocked() != null) {
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

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "Jianhua.Lin@Plf.SDK : Modify for mResumedActivity", property = OppoRomType.ROM)
    void minimalResumeActivityLocked(ActivityRecord r) {
        r.state = ActivityState.RESUMED;
        ProcessRecord appProc = r.app;
        if (appProc != null) {
            AfterActivityResumed eventData = AfterActivityResumed.createInstance();
            Object[] objArr = new Object[6];
            objArr[0] = Integer.valueOf(appProc.pid);
            objArr[1] = r.info.name;
            objArr[2] = r.info.packageName;
            objArr[3] = Integer.valueOf(r.mActivityType);
            objArr[4] = Integer.valueOf(appProc.uid);
            objArr[5] = Integer.valueOf(r.launchedFromUid);
            eventData.set(objArr);
            this.mService.getAMEventHook().hook(Event.AM_AfterActivityResumed, eventData);
        }
        if (ActivityManagerDebugConfig.DEBUG_STATES) {
            Slog.v(TAG_STATES, "Moving to RESUMED: " + r + " (starting new instance)" + " callers=" + Debug.getCallers(5));
        }
        this.mResumedActivity = r;
        if (!(this.mResumedActivity == null || this.mResumedActivity.app == null)) {
            String value = this.mResumedActivity.processName;
            if (value != null && value.length() > 50) {
                value = value.substring(0, 50);
            }
            SystemProperties.set("debug.junk.process.name", value);
            SystemProperties.set("debug.junk.process.pid", Integer.toString(this.mResumedActivity.app.pid));
        }
        r.task.touchActiveTime();
        this.mRecentTasks.addLocked(r.task);
        completeResumeLocked(r);
        this.mStackSupervisor.checkReadyForSleepWhenResumeLocked(r.realActivity);
        setLaunchTime(r);
        if (ActivityManagerDebugConfig.DEBUG_SAVED_STATE) {
            Slog.i(TAG_SAVED_STATE, "Launch completed; removing icicle of " + r.icicle);
        }
    }

    void addRecentActivityLocked(ActivityRecord r) {
        if (r != null) {
            this.mRecentTasks.addLocked(r.task);
            r.task.touchActiveTime();
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

    void clearLaunchTime(ActivityRecord r) {
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
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            List<ActivityRecord> activities = ((TaskRecord) this.mTaskHistory.get(taskNdx)).mActivities;
            for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                if (packageName.equals(((ActivityRecord) activities.get(activityNdx)).packageName)) {
                    ((ActivityRecord) activities.get(activityNdx)).info.applicationInfo = aInfo;
                }
            }
        }
    }

    boolean checkReadyForSleepLocked() {
        if (this.mResumedActivity != null) {
            if (ActivityManagerDebugConfig.DEBUG_PAUSE || ActivityManagerService.DEBUG_COLOROS_AMS) {
                Slog.v(TAG_PAUSE, "Sleep needs to pause " + this.mResumedActivity);
            }
            if (ActivityManagerDebugConfig.DEBUG_USER_LEAVING) {
                Slog.v(TAG_USER_LEAVING, "Sleep => pause with userLeaving=false");
            }
            startPausingLocked(false, true, null, false, "sleep-request");
            return true;
        } else if (this.mPausingActivity != null) {
            if (ActivityManagerDebugConfig.DEBUG_PAUSE || ActivityManagerService.DEBUG_COLOROS_AMS) {
                Slog.v(TAG_PAUSE, "Sleep still waiting to pause " + this.mPausingActivity);
            }
            return true;
        } else if (!hasVisibleBehindActivity()) {
            return false;
        } else {
            ActivityRecord r = getVisibleBehindActivity();
            this.mStackSupervisor.mStoppingActivities.add(r);
            if (ActivityManagerDebugConfig.DEBUG_STATES || ActivityManagerService.DEBUG_COLOROS_AMS) {
                Slog.v(TAG_STATES, "Sleep still waiting to stop visible behind " + r);
            }
            return true;
        }
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

    public final Bitmap screenshotActivitiesLocked(ActivityRecord who) {
        if (ActivityManagerDebugConfig.DEBUG_SCREENSHOTS) {
            Slog.d(TAG_SCREENSHOTS, "screenshotActivitiesLocked: " + who);
        }
        if (who.noDisplay) {
            if (ActivityManagerDebugConfig.DEBUG_SCREENSHOTS) {
                Slog.d(TAG_SCREENSHOTS, "\tNo display");
            }
            return null;
        } else if (isHomeStack()) {
            if (ActivityManagerDebugConfig.DEBUG_SCREENSHOTS) {
                Slog.d(TAG_SCREENSHOTS, "\tHome stack");
            }
            return null;
        } else {
            int w = this.mService.mThumbnailWidth;
            int h = this.mService.mThumbnailHeight;
            if (w <= 0) {
                Slog.e(TAG, "\tInvalid thumbnail dimensions: " + w + "x" + h);
                return null;
            } else if (this.mStackId == 3 && this.mStackSupervisor.mIsDockMinimized) {
                if (ActivityManagerDebugConfig.DEBUG_SCREENSHOTS) {
                    Slog.e(TAG, "\tIn minimized docked stack");
                }
                return null;
            } else if (this.mScreenShotDisabled) {
                Slog.e(TAG, "\tmScreenShotDisabled is true");
                return null;
            } else {
                if (ActivityManagerDebugConfig.DEBUG_SCREENSHOTS) {
                    Slog.d(TAG_SCREENSHOTS, "\tTaking screenshot");
                }
                return this.mWindowManager.screenshotApplications(who.appToken, 0, -1, -1, this.mService.mFullscreenThumbnailScale);
            }
        }
    }

    final boolean startPausingLocked(boolean userLeaving, boolean uiSleeping, ActivityRecord resuming, boolean dontWait) {
        return startPausingLocked(userLeaving, uiSleeping, resuming, dontWait, "ohter-request");
    }

    final boolean startPausingLocked(boolean userLeaving, boolean uiSleeping, ActivityRecord resuming, boolean dontWait, String reason) {
        if (this.mPausingActivity != null) {
            Slog.e(TAG, "Going to pause when pause is already pending for " + this.mPausingActivity + " state=" + this.mPausingActivity.state, new RuntimeException("here").fillInStackTrace());
            completePauseLocked(false, resuming);
        }
        ActivityRecord prev = this.mResumedActivity;
        if (prev == null) {
            if (resuming == null) {
                Slog.e(TAG, "Trying to pause when nothing is resumed", new RuntimeException("here").fillInStackTrace());
                this.mStackSupervisor.resumeFocusedStackTopActivityLocked();
            }
            return false;
        }
        if (this.mActivityContainer.mParentActivity == null) {
            this.mStackSupervisor.pauseChildStacks(prev, userLeaving, uiSleeping, resuming, dontWait);
        }
        if (ActivityManagerDebugConfig.DEBUG_STATES) {
            Slog.v(TAG_STATES, "Moving to PAUSING: " + prev);
        } else if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
            Slog.v(TAG_PAUSE, "Start pausing: " + prev);
        }
        this.mResumedActivity = null;
        this.mPausingActivity = prev;
        this.mLastPausedActivity = prev;
        ActivityRecord activityRecord = ((prev.intent.getFlags() & 1073741824) == 0 && (prev.info.flags & 128) == 0) ? null : prev;
        this.mLastNoHistoryActivity = activityRecord;
        prev.state = ActivityState.PAUSING;
        prev.task.touchActiveTime();
        clearLaunchTime(prev);
        ActivityRecord next = this.mStackSupervisor.topRunningActivityLocked();
        if (this.mService.mHasRecents && (next == null || next.noDisplay || next.task != prev.task || uiSleeping)) {
            prev.mUpdateTaskThumbnailWhenHidden = true;
            if (!(!this.mFullscreen || prev == null || prev.configuration == null || prev.mSplitBackState == prev.SPLIT_BACE_SHOTED || prev.configuration.screenWidthDp <= prev.configuration.screenHeightDp)) {
                prev.updateThumbnailLocked(screenshotActivitiesLocked(prev), null);
                prev.mUpdateTaskThumbnailWhenHidden = false;
            }
            this.mWindowManager.setSplitFromBack(false);
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
                Object[] objArr = new Object[4];
                objArr[0] = Integer.valueOf(prev.userId);
                objArr[1] = Integer.valueOf(System.identityHashCode(prev));
                objArr[2] = prev.shortComponentName;
                objArr[3] = reason;
                EventLog.writeEvent(EventLogTags.AM_PAUSE_ACTIVITY, objArr);
                if (!ActivityManagerService.IS_USER_BUILD || ActivityManagerDebugConfig.DEBUG_TASKS) {
                    Slog.d(TAG, "ACT-AM_PAUSE_ACTIVITY " + prev);
                }
                this.mService.updateUsageStats(prev, false);
                prev.app.thread.schedulePauseActivity(prev.appToken, prev.finishing, userLeaving, prev.configChangeFlags, dontWait);
            } catch (Exception e) {
                Slog.w(TAG, "Exception thrown during pause", e);
                this.mPausingActivity = null;
                this.mLastPausedActivity = null;
                this.mLastNoHistoryActivity = null;
            }
        }
        if (!(uiSleeping || this.mService.isSleepingOrShuttingDownLocked())) {
            this.mStackSupervisor.acquireLaunchWakelock();
        }
        if (this.mPausingActivity != null) {
            if (!uiSleeping) {
                prev.pauseKeyDispatchingLocked();
            } else if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
                Slog.v(TAG_PAUSE, "Key dispatch not paused for screen off");
            }
            if (dontWait) {
                completePauseLocked(false, resuming);
                return false;
            }
            Message msg = this.mHandler.obtainMessage(101);
            msg.obj = prev;
            prev.pauseTime = SystemClock.uptimeMillis();
            this.mHandler.sendMessageDelayed(msg, 500);
            if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
                Slog.v(TAG_PAUSE, "Waiting for pause to complete...");
            }
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
        if (!ActivityManagerService.IS_USER_BUILD || ActivityManagerDebugConfig.DEBUG_TASKS) {
            Slog.d(TAG, "ACT-paused: token=" + token + ", timeout=" + timeout);
        }
        ActivityRecord r = isInStackLocked(token);
        if (r != null) {
            this.mHandler.removeMessages(101, r);
            if (this.mPausingActivity == r) {
                if (ActivityManagerDebugConfig.DEBUG_STATES) {
                    Slog.v(TAG_STATES, "Moving to PAUSED: " + r + (timeout ? " (due to timeout)" : " (pause complete)"));
                }
                completePauseLocked(true, null);
                return;
            }
            Object[] objArr = new Object[4];
            objArr[0] = Integer.valueOf(r.userId);
            objArr[1] = Integer.valueOf(System.identityHashCode(r));
            objArr[2] = r.shortComponentName;
            objArr[3] = this.mPausingActivity != null ? this.mPausingActivity.shortComponentName : "(none)";
            EventLog.writeEvent(EventLogTags.AM_FAILED_TO_PAUSE, objArr);
            if (r.state == ActivityState.PAUSING) {
                r.state = ActivityState.PAUSED;
                ProcessRecord appProc = r.app;
                if (appProc != null) {
                    AfterActivityPaused eventData = AfterActivityPaused.createInstance();
                    Object[] objArr2 = new Object[3];
                    objArr2[0] = Integer.valueOf(appProc.pid);
                    objArr2[1] = r.info.name;
                    objArr2[2] = r.info.packageName;
                    eventData.set(objArr2);
                    this.mService.getAMEventHook().hook(Event.AM_AfterActivityPaused, eventData);
                }
                if (r.finishing) {
                    if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
                        Slog.v(TAG, "Executing finish of failed to pause activity: " + r);
                    }
                    finishCurrentActivityLocked(r, 2, false);
                }
            }
            if (!ActivityManagerService.IS_USER_BUILD || ActivityManagerDebugConfig.DEBUG_TASKS) {
                Slog.d(TAG, "ACT-AM_FAILED_TO_PAUSE " + r + " PausingActivity:" + this.mPausingActivity);
            }
        } else if (timeout && this.mPausingActivity != null) {
            if (ActivityManagerDebugConfig.DEBUG_STATES) {
                Slog.v(TAG_STATES, "mPausingActivity is not null when timeout,call completePauseLocked");
            }
            completePauseLocked(false, null);
        }
        this.mStackSupervisor.ensureActivitiesVisibleLocked(null, 0, false);
    }

    final void activityResumedLocked(IBinder token) {
        ActivityRecord r = ActivityRecord.forTokenLocked(token);
        if (ActivityManagerDebugConfig.DEBUG_SAVED_STATE) {
            Slog.i(TAG_STATES, "Resumed activity; dropping state of: " + r);
        }
        r.icicle = null;
        r.haveState = false;
    }

    final void activityStoppedLocked(ActivityRecord r, Bundle icicle, PersistableBundle persistentState, CharSequence description) {
        if (r.state != ActivityState.STOPPING) {
            Slog.i(TAG, "Activity reported stop, but no longer stopping: " + r);
            this.mHandler.removeMessages(104, r);
            return;
        }
        if (persistentState != null) {
            r.persistentState = persistentState;
            this.mService.notifyTaskPersisterLocked(r.task, false);
        }
        if (ActivityManagerDebugConfig.DEBUG_SAVED_STATE) {
            Slog.i(TAG_SAVED_STATE, "Saving icicle of " + r + ": " + icicle);
        }
        if (icicle != null) {
            r.icicle = icicle;
            r.haveState = true;
            r.launchCount = 0;
            r.updateThumbnailLocked(null, description);
        }
        if (!r.stopped) {
            if (ActivityManagerDebugConfig.DEBUG_STATES) {
                Slog.v(TAG_STATES, "Moving to STOPPED: " + r + " (stop complete)");
            }
            this.mHandler.removeMessages(104, r);
            r.stopped = true;
            r.state = ActivityState.STOPPED;
            ProcessRecord appProc = r.app;
            if (appProc != null) {
                AfterActivityStopped eventData = AfterActivityStopped.createInstance();
                Object[] objArr = new Object[3];
                objArr[0] = Integer.valueOf(appProc.pid);
                objArr[1] = r.info.name;
                objArr[2] = r.info.packageName;
                eventData.set(objArr);
                this.mService.getAMEventHook().hook(Event.AM_AfterActivityStopped, eventData);
            }
            this.mWindowManager.notifyAppStopped(r.appToken);
            if (getVisibleBehindActivity() == r) {
                this.mStackSupervisor.requestVisibleBehindLocked(r, false);
            }
            if (r.finishing) {
                r.clearOptionsLocked();
            } else if (r.deferRelaunchUntilPaused) {
                destroyActivityLocked(r, true, "stop-config");
                this.mStackSupervisor.resumeFocusedStackTopActivityLocked();
            } else {
                this.mStackSupervisor.updatePreviousProcessLocked(r);
            }
        }
    }

    private void completePauseLocked(boolean resumeNext, ActivityRecord resuming) {
        ActivityStack topStack;
        ActivityRecord prev = this.mPausingActivity;
        if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
            Slog.v(TAG_PAUSE, "Complete pause: " + prev);
        }
        if (prev != null) {
            boolean wasStopping = prev.state == ActivityState.STOPPING;
            prev.mSplitBackState = prev.SPLIT_BACE_INVALID;
            prev.state = ActivityState.PAUSED;
            ProcessRecord appProc = prev.app;
            if (appProc != null) {
                AfterActivityPaused eventData = AfterActivityPaused.createInstance();
                Object[] objArr = new Object[3];
                objArr[0] = Integer.valueOf(appProc.pid);
                objArr[1] = prev.info.name;
                objArr[2] = prev.info.packageName;
                eventData.set(objArr);
                this.mService.getAMEventHook().hook(Event.AM_AfterActivityPaused, eventData);
            }
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
                if (this.mStackSupervisor.mWaitingVisibleActivities.remove(prev) && (ActivityManagerDebugConfig.DEBUG_SWITCH || ActivityManagerDebugConfig.DEBUG_PAUSE)) {
                    Slog.v(TAG_PAUSE, "Complete pause, no longer waiting: " + prev);
                }
                if (prev.deferRelaunchUntilPaused) {
                    topStack = this.mStackSupervisor.getFocusedStack();
                    ActivityRecord next = topStack != null ? topStack.topRunningActivityLocked() : null;
                    if (next == null || !ActivityManagerService.OPPO_LAUNCHER.equals(next.packageName)) {
                        if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
                            Slog.v(TAG_PAUSE, "Re-launching after pause: " + prev);
                        }
                        relaunchActivityLocked(prev, prev.configChangeFlags, false, prev.preserveWindowOnDeferredRelaunch);
                    } else {
                        Slog.v(TAG_PAUSE, "topRunningActivity is launcher,don't relaunch prev=" + prev);
                    }
                } else if (wasStopping) {
                    prev.state = ActivityState.STOPPING;
                } else if (!(prev.visible || hasVisibleBehindActivity()) || this.mService.isSleepingOrShuttingDownLocked()) {
                    addToStopping(prev, true);
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
            topStack = this.mStackSupervisor.getFocusedStack();
            if (this.mService.isSleepingOrShuttingDownLocked()) {
                if (!this.mService.mIgnoreSleepCheckLater) {
                    this.mStackSupervisor.checkReadyForSleepLocked();
                } else if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
                    Slog.v(TAG, "resumeNext, mIgnoreSleepCheckLater, do not care.");
                }
                ActivityRecord top = topStack.topRunningActivityLocked();
                if (top == null || !(prev == null || top == prev)) {
                    this.mStackSupervisor.resumeFocusedStackTopActivityLocked();
                }
            } else {
                this.mStackSupervisor.resumeFocusedStackTopActivityLocked(topStack, prev, null);
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
            this.mService.notifyTaskStackChangedLocked();
            this.mStackSupervisor.mAppVisibilitiesChangedSinceLastPause = false;
        }
        this.mStackSupervisor.ensureActivitiesVisibleLocked(resuming, 0, false);
    }

    private void addToStopping(ActivityRecord r, boolean immediate) {
        if (!this.mStackSupervisor.mStoppingActivities.contains(r)) {
            this.mStackSupervisor.mStoppingActivities.add(r);
        }
        boolean forceIdle = this.mStackSupervisor.mStoppingActivities.size() <= 3 ? r.frontOfTask && this.mTaskHistory.size() <= 1 : true;
        if (immediate || forceIdle) {
            if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
                Slog.v(TAG_PAUSE, "Scheduling idle now: forceIdle=" + forceIdle + "immediate=" + immediate);
            }
            this.mStackSupervisor.scheduleIdleLocked();
        } else if (!this.mService.mIgnoreSleepCheckLater) {
            this.mStackSupervisor.checkReadyForSleepLocked();
        } else if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
            Slog.v(TAG, "resumeNext, mIgnoreSleepCheckLater, do not care.");
        }
    }

    private void completeResumeLocked(ActivityRecord next) {
        next.visible = true;
        next.idle = false;
        next.results = null;
        next.newIntents = null;
        next.stopped = false;
        if (next.isHomeActivity()) {
            ProcessRecord app = ((ActivityRecord) next.task.mActivities.get(0)).app;
            if (!(app == null || app == this.mService.mHomeProcess)) {
                this.mService.mHomeProcess = app;
            }
        }
        if (next.nowVisible) {
            this.mStackSupervisor.reportActivityVisibleLocked(next);
            this.mStackSupervisor.notifyActivityDrawnForKeyguard();
        }
        this.mStackSupervisor.scheduleIdleTimeoutLocked(next);
        this.mStackSupervisor.reportResumedActivityLocked(next);
        this.mService.setFocusedActivityLocked(next, "completeResume");
        if (!(this.mService.mOppoKinectController == null || next == null)) {
            try {
                if (next.realActivity == null) {
                    this.mService.mOppoKinectController.activityResuming(null);
                } else {
                    this.mService.mOppoKinectController.activityResuming(next.realActivity.getClassName());
                }
            } catch (Exception e) {
                this.mService.mOppoKinectController = null;
                Slog.v(TAG, "mOppoKinectController exception e = " + e.toString());
            }
        }
        next.resumeKeyDispatchingLocked();
        this.mNoAnimActivities.clear();
        if (next.app != null) {
            next.cpuTimeAtResume = this.mService.mProcessCpuTracker.getCpuTimeForPid(next.app.pid);
        } else {
            next.cpuTimeAtResume = 0;
        }
        next.returningOptions = null;
        if (getVisibleBehindActivity() == next) {
            setVisibleBehindActivity(null);
        }
        if (!OifaceUtil.isEnable() || next.realActivity == null || next.app == null) {
            Slog.i(TAG, "OifaceUtil cann't get currentPackage");
        } else {
            OifaceUtil.getInstance().currentPackage(next.realActivity.getPackageName(), next.app.uid, next.app.pid);
        }
    }

    private void setVisible(ActivityRecord r, boolean visible) {
        if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
            Slog.v(TAG, "setVisible " + r + " " + visible);
        }
        r.visible = visible;
        if (!visible && r.mUpdateTaskThumbnailWhenHidden) {
            r.updateThumbnailLocked(r.task.stack.screenshotActivitiesLocked(r), null);
            r.mUpdateTaskThumbnailWhenHidden = false;
        }
        this.mWindowManager.setAppVisibility(r.appToken, visible);
        ArrayList<ActivityContainer> containers = r.mChildContainers;
        for (int containerNdx = containers.size() - 1; containerNdx >= 0; containerNdx--) {
            ((ActivityContainer) containers.get(containerNdx)).setVisible(visible);
        }
        this.mStackSupervisor.mAppVisibilitiesChangedSinceLastPause = true;
    }

    ActivityRecord findNextTranslucentActivity(ActivityRecord r) {
        TaskRecord task = r.task;
        if (task == null) {
            return null;
        }
        ActivityStack stack = task.stack;
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
jadx.core.utils.exceptions.CodegenException: Error generate insn: PHI: (r11_1 'taskNdx' int) = (r11_0 'taskNdx' int), (r11_4 'taskNdx' int) binds: {(r11_0 'taskNdx' int)=B:8:0x000c, (r11_4 'taskNdx' int)=B:26:0x0065} in method: com.android.server.am.ActivityStack.findNextTranslucentActivity(com.android.server.am.ActivityRecord):com.android.server.am.ActivityRecord, dex: 
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

    ActivityStack getNextFocusableStackLocked() {
        ArrayList<ActivityStack> stacks = this.mStacks;
        ActivityRecord parent = this.mActivityContainer.mParentActivity;
        if (parent != null) {
            stacks = parent.task.stack.mStacks;
        }
        if (stacks != null) {
            for (int i = stacks.size() - 1; i >= 0; i--) {
                ActivityStack stack = (ActivityStack) stacks.get(i);
                if (stack != this && stack.isFocusable() && stack.getStackVisibilityLocked(null) != 0) {
                    return stack;
                }
            }
        }
        return null;
    }

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
                    if (!isHomeStack() && r.frontOfTask && task.isOverHomeStack() && stackBehindId != 0) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    int getStackVisibilityLocked(ActivityRecord starting) {
        TaskRecord task = null;
        if (!isAttached()) {
            return 0;
        }
        if (this.mStackSupervisor.isFrontStack(this) || this.mStackSupervisor.isFocusedStack(this)) {
            return 1;
        }
        int stackIndex = this.mStacks.indexOf(this);
        if (stackIndex == this.mStacks.size() - 1) {
            Slog.wtf(TAG, "Stack=" + this + " isn't front stack but is at the top of the stack list");
            return 0;
        }
        ActivityStack focusedStack = this.mStackSupervisor.getFocusedStack();
        int focusedStackId = focusedStack.mStackId;
        if (this.mStackId == 1 && hasVisibleBehindActivity() && focusedStackId == 0 && !focusedStack.topActivity().fullscreen) {
            return 2;
        }
        if (this.mStackId == 3) {
            ActivityRecord r = focusedStack.topRunningActivityLocked();
            if (r != null) {
                task = r.task;
            }
            int i = (task == null || task.canGoInDockedStack() || task.isHomeTask()) ? 1 : 0;
            return i;
        }
        int stackBehindFocusedIndex = this.mStacks.indexOf(focusedStack) - 1;
        while (stackBehindFocusedIndex >= 0 && ((ActivityStack) this.mStacks.get(stackBehindFocusedIndex)).topRunningActivityLocked() == null) {
            stackBehindFocusedIndex--;
        }
        if ((focusedStackId == 3 || focusedStackId == 4) && stackIndex == stackBehindFocusedIndex) {
            return 1;
        }
        int stackBehindFocusedId = stackBehindFocusedIndex >= 0 ? ((ActivityStack) this.mStacks.get(stackBehindFocusedIndex)).mStackId : -1;
        if (focusedStackId == 1 && focusedStack.isStackTranslucent(starting, stackBehindFocusedId)) {
            if (stackIndex == stackBehindFocusedIndex) {
                return 1;
            }
            if (stackBehindFocusedIndex >= 0 && ((stackBehindFocusedId == 3 || stackBehindFocusedId == 4) && stackIndex == stackBehindFocusedIndex - 1)) {
                return 1;
            }
        }
        if (StackId.isStaticStack(this.mStackId)) {
            return 0;
        }
        for (int i2 = stackIndex + 1; i2 < this.mStacks.size(); i2++) {
            ActivityStack stack = (ActivityStack) this.mStacks.get(i2);
            if ((stack.mFullscreen || stack.hasFullscreenTask()) && (!StackId.isDynamicStacksVisibleBehindAllowed(stack.mStackId) || !stack.isStackTranslucent(starting, -1))) {
                return 0;
            }
        }
        return 1;
    }

    final int rankTaskLayers(int baseLayer) {
        int taskNdx = this.mTaskHistory.size() - 1;
        int layer = 0;
        while (taskNdx >= 0) {
            int layer2;
            TaskRecord task = (TaskRecord) this.mTaskHistory.get(taskNdx);
            ActivityRecord r = task.topRunningActivityLocked();
            if (r == null || r.finishing || !r.visible) {
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
        ActivityRecord top = topRunningActivityLocked();
        if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
            Slog.v(TAG_VISIBILITY, "ensureActivitiesVisible behind " + top + " starting " + starting + " configChanges=0x" + Integer.toHexString(configChanges));
        }
        if (top != null) {
            checkTranslucentActivityWaiting(top);
        }
        boolean aboveTop = top != null;
        int stackVisibility = getStackVisibilityLocked(starting);
        boolean stackInvisible = stackVisibility != 1;
        boolean stackVisibleBehind = stackVisibility == 2;
        boolean behindFullscreenActivity = stackInvisible;
        boolean resumeNextActivity = this.mStackSupervisor.isFocusedStack(this) ? isInStackLocked(starting) == null : false;
        boolean behindTranslucentActivity = false;
        ActivityRecord visibleBehind = getVisibleBehindActivity();
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            TaskRecord task = (TaskRecord) this.mTaskHistory.get(taskNdx);
            ArrayList<ActivityRecord> activities = task.mActivities;
            int activityNdx = activities.size() - 1;
            while (activityNdx >= 0) {
                ActivityRecord r = (ActivityRecord) activities.get(activityNdx);
                if (!r.finishing) {
                    boolean isTop = r == top;
                    if (!aboveTop || isTop) {
                        aboveTop = false;
                        if (shouldBeVisible(r, behindTranslucentActivity, stackVisibleBehind, visibleBehind, behindFullscreenActivity)) {
                            if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
                                Slog.v(TAG_VISIBILITY, "Make visible? " + r + " finishing=" + r.finishing + " state=" + r.state);
                            }
                            if (r != starting) {
                                ensureActivityConfigurationLocked(r, 0, preserveWindows);
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
                                if (handleAlreadyVisible(r)) {
                                    resumeNextActivity = false;
                                }
                            } else {
                                makeVisibleIfNeeded(starting, r);
                            }
                            configChanges |= r.configChangeFlags;
                            behindFullscreenActivity = updateBehindFullscreen(stackInvisible, behindFullscreenActivity, task, r);
                            if (behindFullscreenActivity && !r.fullscreen) {
                                behindTranslucentActivity = true;
                            }
                        } else {
                            if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
                                Slog.v(TAG_VISIBILITY, "Make invisible? " + r + " finishing=" + r.finishing + " state=" + r.state + " stackInvisible=" + stackInvisible + " behindFullscreenActivity=" + behindFullscreenActivity + " mLaunchTaskBehind=" + r.mLaunchTaskBehind);
                            }
                            makeInvisible(r, visibleBehind);
                        }
                    }
                } else if (r.mUpdateTaskThumbnailWhenHidden && r.mSplitBackState != r.SPLIT_BACE_SHOTED) {
                    r.updateThumbnailLocked(screenshotActivitiesLocked(r), null);
                    r.mUpdateTaskThumbnailWhenHidden = false;
                    r.mSplitBackState = r.SPLIT_BACE_SHOTED;
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
            }
        }
        if (this.mTranslucentActivityWaiting != null && this.mUndrawnActivitiesBelowTopTranslucent.isEmpty()) {
            notifyActivityDrawnLocked(null);
        }
    }

    private boolean shouldBeVisible(ActivityRecord r, boolean behindTranslucentActivity, boolean stackVisibleBehind, ActivityRecord visibleBehind, boolean behindFullscreenActivity) {
        if (!okToShowLocked(r)) {
            return false;
        }
        boolean activityVisibleBehind = (behindTranslucentActivity || stackVisibleBehind) && visibleBehind == r;
        boolean isVisible = (!behindFullscreenActivity || r.mLaunchTaskBehind) ? true : activityVisibleBehind;
        if (this.mService.mSupportsLeanbackOnly && isVisible && r.isRecentsActivity()) {
            isVisible = this.mStackSupervisor.getStack(3) == null ? this.mStackSupervisor.isFocusedStack(this) : true;
        }
        return isVisible;
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
        if (skipStartActivityIfNeeded()) {
            return false;
        }
        if (isTop || !r.visible) {
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
                setVisible(r, true);
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
    private void makeInvisible(ActivityRecord r, ActivityRecord visibleBehind) {
        if (r.visible) {
            if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
                Slog.v(TAG_VISIBILITY, "Making invisible: " + r + " " + r.state);
            }
            try {
                setVisible(r, false);
                switch (m17x775af271()[r.state.ordinal()]) {
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                        if (visibleBehind != r) {
                            ActivityRecord next = this.mStackSupervisor.topRunningActivityLocked();
                            if ((!"com.android.contacts/.activities.ContactEditorActivity".equals(r.shortComponentName) && !"com.google.android.googlequicksearchbox/com.google.android.apps.gsa.searchnow.SearchNowActivity".equals(r.shortComponentName)) || !r.nowVisible || next == null || next.nowVisible) {
                                addToStopping(r, true);
                                break;
                            }
                            if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
                                Slog.v(TAG_VISIBILITY, "Add stop: " + r);
                            }
                            addToStopping(r, false);
                            break;
                        }
                        releaseBackgroundResources(r);
                        break;
                        break;
                    case 5:
                    case 6:
                        if (!(r.app == null || r.app.thread == null)) {
                            if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
                                Slog.v(TAG_VISIBILITY, "Scheduling invisibility: " + r);
                            }
                            r.app.thread.scheduleWindowVisibility(r.appToken, false);
                            break;
                        }
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
        } else if (isHomeStack() || !r.frontOfTask || !task.isOverHomeStack()) {
            return behindFullscreenActivity;
        } else {
            if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
                Slog.v(TAG_VISIBILITY, "Showing home: at " + r + " stackInvisible=" + stackInvisible + " behindFullscreenActivity=" + behindFullscreenActivity);
            }
            return true;
        }
    }

    private void makeVisibleIfNeeded(ActivityRecord starting, ActivityRecord r) {
        if (r.state == ActivityState.RESUMED || r == starting) {
            if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
                Slog.d(TAG_VISIBILITY, "Not making visible, r=" + r + " state=" + r.state + " starting=" + starting);
            }
            return;
        }
        if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
            Slog.v(TAG_VISIBILITY, "Making visible and scheduling visibility: " + r);
        }
        try {
            if (this.mTranslucentActivityWaiting != null) {
                r.updateOptionsLocked(r.returningOptions);
                this.mUndrawnActivitiesBelowTopTranslucent.add(r);
            }
            setVisible(r, true);
            r.sleeping = false;
            r.app.pendingUiClean = true;
            r.app.thread.scheduleWindowVisibility(r.appToken, true);
            this.mStackSupervisor.mStoppingActivities.remove(r);
            this.mStackSupervisor.mGoingToSleepActivities.remove(r);
        } catch (Exception e) {
            Slog.w(TAG, "Exception thrown making visibile: " + r.intent.getComponent(), e);
        }
        handleAlreadyVisible(r);
    }

    private boolean handleAlreadyVisible(ActivityRecord r) {
        r.stopFreezingScreenLocked(false);
        try {
            if (r.returningOptions != null) {
                r.app.thread.scheduleOnNewActivityOptions(r.appToken, r.returningOptions);
            }
        } catch (RemoteException e) {
        }
        if (r.state == ActivityState.RESUMED) {
            return true;
        }
        return false;
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
        this.mActivityContainer.setDrawn();
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
        if (getStackVisibilityLocked(null) == 0) {
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
                    if (r.state == ActivityState.INITIALIZING && r.mStartingWindowState == 1 && behindFullscreenActivity) {
                        if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
                            Slog.w(TAG_VISIBILITY, "Found orphaned starting window " + r);
                        }
                        r.mStartingWindowState = 2;
                        this.mWindowManager.removeAppStartingWindow(r.appToken);
                    }
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
            if (this.mService.mLockScreenShown == 1) {
                this.mService.mLockScreenShown = 0;
                this.mService.updateSleepIfNeededLocked();
            }
            result = resumeTopActivityInnerLocked(prev, options);
            return result;
        } finally {
            this.mStackSupervisor.inResumeTopActivity = false;
        }
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "Jianhua.Lin@Plf.SDK : Modify for mResumedActivity", property = OppoRomType.ROM)
    private boolean resumeTopActivityInnerLocked(ActivityRecord prev, ActivityOptions options) {
        Throwable th;
        if (ActivityManagerDebugConfig.DEBUG_LOCKSCREEN) {
            this.mService.logLockScreen(IElsaManager.EMPTY_PACKAGE);
        }
        if (!this.mService.mBooting && !this.mService.mBooted) {
            return false;
        }
        ActivityRecord parent = this.mActivityContainer.mParentActivity;
        if ((parent != null && parent.state != ActivityState.RESUMED) || !this.mActivityContainer.isAttachedLocked()) {
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
        ActivityRecord next = topRunningActivityLocked();
        if (ActivityManagerDebugConfig.DEBUG_STATES || ActivityManagerDebugConfig.DEBUG_AMS) {
            Slog.d(TAG, "resumeTopActivityLocked: next " + next);
        }
        boolean userLeaving = this.mStackSupervisor.mUserLeaving;
        this.mStackSupervisor.mUserLeaving = false;
        TaskRecord prevTask = prev != null ? prev.task : null;
        int returnTaskType;
        boolean resumeHomeStackTask;
        if (next == null) {
            String reason = "noMoreActivities";
            returnTaskType = (prevTask == null || !prevTask.isOverHomeStack()) ? 1 : prevTask.getTaskToReturnTo();
            if (!this.mFullscreen) {
                if (adjustFocusToNextFocusableStackLocked(returnTaskType, "noMoreActivities")) {
                    return this.mStackSupervisor.resumeFocusedStackTopActivityLocked(this.mStackSupervisor.getFocusedStack(), prev, null);
                }
            }
            ActivityOptions.abort(options);
            if (AMEventHookResult.hasAction(this.mService.getAMEventHook().hook(Event.AM_BeforeGoHomeWhenNoActivities, BeforeGoHomeWhenNoActivities.createInstance()), AMEventHookAction.AM_SkipHomeActivityLaunching)) {
                Slog.v(TAG, "Skip to resume home activity!!");
                return false;
            }
            if (ActivityManagerDebugConfig.DEBUG_STATES) {
                Slog.d(TAG_STATES, "resumeTopActivityLocked: No more activities go home");
            }
            if (ActivityManagerDebugConfig.DEBUG_STACK) {
                this.mStackSupervisor.validateTopActivitiesLocked();
            }
            if (isOnHomeDisplay()) {
                resumeHomeStackTask = this.mStackSupervisor.resumeHomeStackTask(returnTaskType, prev, "noMoreActivities");
            } else {
                resumeHomeStackTask = false;
            }
            return resumeHomeStackTask;
        }
        next.delayedResume = false;
        if (this.mResumedActivity == next && next.state == ActivityState.RESUMED && this.mStackSupervisor.allResumedActivitiesComplete()) {
            this.mWindowManager.executeAppTransition();
            this.mNoAnimActivities.clear();
            ActivityOptions.abort(options);
            if (ActivityManagerDebugConfig.DEBUG_STATES) {
                Slog.d(TAG_STATES, "resumeTopActivityLocked: Top activity resumed " + next);
            }
            if (ActivityManagerDebugConfig.DEBUG_STACK) {
                this.mStackSupervisor.validateTopActivitiesLocked();
            }
            return false;
        }
        TaskRecord nextTask = next.task;
        if (prevTask != null && prevTask.stack == this && prevTask.isOverHomeStack() && prev.finishing && prev.frontOfTask) {
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
                    if (ActivityManagerDebugConfig.DEBUG_STATES) {
                        Slog.d(TAG_STATES, "resumeTopActivityLocked: Launching home next");
                    }
                    returnTaskType = (prevTask == null || !prevTask.isOverHomeStack()) ? 1 : prevTask.getTaskToReturnTo();
                    if (isOnHomeDisplay()) {
                        resumeHomeStackTask = this.mStackSupervisor.resumeHomeStackTask(returnTaskType, prev, "prevFinished");
                    } else {
                        resumeHomeStackTask = false;
                    }
                    return resumeHomeStackTask;
                }
            }
        }
        ActivityStack focusStack = this.mStackSupervisor.getFocusedStack();
        ActivityStack lastStackBeforeResume = this.mStackSupervisor.getLastStack();
        boolean keepResume = false;
        if (focusStack != null && lastStackBeforeResume != null && isHomeStack() && focusStack == this && focusStack != lastStackBeforeResume && this.mLastPausedActivity == next) {
            if (ActivityManagerDebugConfig.DEBUG_TASKS || ActivityManagerDebugConfig.DEBUG_STACK) {
                Slog.d(TAG, "resumeTopActivityLocked: Going to sleep, and resume home from other stack, try resume one time.");
            }
            keepResume = true;
        }
        if (this.mService.isSleepingOrShuttingDownLocked() && this.mLastPausedActivity == next && this.mStackSupervisor.allPausedActivitiesComplete() && !keepResume) {
            this.mWindowManager.executeAppTransition();
            this.mNoAnimActivities.clear();
            ActivityOptions.abort(options);
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
            this.mStackSupervisor.mWaitingVisibleActivities.remove(next);
            if (ActivityManagerDebugConfig.DEBUG_SWITCH) {
                Slog.v(TAG_SWITCH, "Resuming " + next);
            }
            if (this.mStackSupervisor.allPausedActivitiesComplete()) {
                int i;
                Object[] objArr;
                this.mStackSupervisor.setLaunchSource(next.info.applicationInfo.uid);
                boolean dontWaitForPause = (next.info.flags & 16384) != 0;
                boolean pausing = this.mStackSupervisor.pauseBackStacks(userLeaving, next, dontWaitForPause);
                if (this.mResumedActivity != null) {
                    if (ActivityManagerDebugConfig.DEBUG_STATES) {
                        Slog.d(TAG_STATES, "resumeTopActivityLocked: Pausing " + this.mResumedActivity);
                    }
                    pausing |= startPausingLocked(userLeaving, false, next, dontWaitForPause, "resume-request");
                }
                if (!(next.info.packageName == this.mStackSupervisor.mLastResumedActivity.packageName && next.info.name == this.mStackSupervisor.mLastResumedActivity.activityName)) {
                    BeforeActivitySwitch eventData = BeforeActivitySwitch.createInstance();
                    ArrayList arrayList = null;
                    if (!((!LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(SystemProperties.get("persist.runningbooster.support")) && !LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(SystemProperties.get("ro.mtk_aws_support"))) || pausing || nextTask == null)) {
                        arrayList = new ArrayList();
                        for (i = 0; i < nextTask.mActivities.size(); i++) {
                            ActivityRecord taskActivity = (ActivityRecord) nextTask.mActivities.get(i);
                            if (taskActivity.packageName != null) {
                                arrayList.add(taskActivity.packageName);
                            }
                        }
                    }
                    int waitProcessPid = -1;
                    ArrayList<IAWSProcessRecord> runningProcRecords = null;
                    if (SystemProperties.get("ro.mtk_aws_support").equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON)) {
                        if (!(next.resultTo == null || next.resultTo.app == null)) {
                            waitProcessPid = next.resultTo.app.pid;
                        }
                        synchronized (this.mService.mPidsSelfLocked) {
                            try {
                                int size = this.mService.mPidsSelfLocked.size();
                                if (size != 0) {
                                    i = 0;
                                    while (true) {
                                        ArrayList<IAWSProcessRecord> runningProcRecords2 = runningProcRecords;
                                        if (i >= size) {
                                            runningProcRecords = runningProcRecords2;
                                            break;
                                        }
                                        if (runningProcRecords2 == null) {
                                            try {
                                                runningProcRecords = new ArrayList();
                                            } catch (Throwable th2) {
                                                th = th2;
                                                runningProcRecords = runningProcRecords2;
                                            }
                                        } else {
                                            runningProcRecords = runningProcRecords2;
                                        }
                                        ProcessRecord proc = (ProcessRecord) this.mService.mPidsSelfLocked.valueAt(i);
                                        if (proc != null) {
                                            runningProcRecords.add(ActivityManagerService.convertProcessRecord(proc));
                                        }
                                        i++;
                                    }
                                }
                            } catch (Throwable th3) {
                                th = th3;
                            }
                        }
                    }
                    objArr = new Object[10];
                    objArr[0] = this.mStackSupervisor.mLastResumedActivity.activityName;
                    objArr[1] = next.info.name;
                    objArr[2] = this.mStackSupervisor.mLastResumedActivity.packageName;
                    objArr[3] = next.info.packageName;
                    objArr[4] = Integer.valueOf(this.mStackSupervisor.mLastResumedActivity.activityType);
                    objArr[5] = Integer.valueOf(next.mActivityType);
                    objArr[6] = Boolean.valueOf(pausing);
                    objArr[7] = arrayList;
                    objArr[8] = Integer.valueOf(waitProcessPid);
                    objArr[9] = runningProcRecords;
                    eventData.set(objArr);
                    this.mService.getAMEventHook().hook(Event.AM_BeforeActivitySwitch, eventData);
                    if (!pausing) {
                        this.mStackSupervisor.mLastResumedActivity.packageName = next.info.packageName;
                        this.mStackSupervisor.mLastResumedActivity.activityName = next.info.name;
                        this.mStackSupervisor.mLastResumedActivity.activityType = next.mActivityType;
                    }
                }
                if (pausing) {
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
                    this.mWindowManager.executeAppTransition();
                    this.mNoAnimActivities.clear();
                    ActivityOptions.abort(options);
                    if (ActivityManagerDebugConfig.DEBUG_STATES) {
                        Slog.d(TAG_STATES, "resumeTopActivityLocked: Top activity resumed (dontWaitForPause) " + next);
                    }
                    if (ActivityManagerDebugConfig.DEBUG_STACK) {
                        this.mStackSupervisor.validateTopActivitiesLocked();
                    }
                    return true;
                } else {
                    if (!(!this.mService.isSleepingLocked() || this.mLastNoHistoryActivity == null || this.mLastNoHistoryActivity.finishing)) {
                        if (ActivityManagerDebugConfig.DEBUG_STATES) {
                            Slog.d(TAG_STATES, "no-history finish of " + this.mLastNoHistoryActivity + " on new resume");
                        }
                        requestFinishActivityLocked(this.mLastNoHistoryActivity.appToken, 0, null, "resume-no-history", false);
                        this.mLastNoHistoryActivity = null;
                    }
                    if (!(prev == null || prev == next)) {
                        if (!this.mStackSupervisor.mWaitingVisibleActivities.contains(prev) && next != null && !next.nowVisible) {
                            this.mStackSupervisor.mWaitingVisibleActivities.add(prev);
                            if (ActivityManagerDebugConfig.DEBUG_SWITCH) {
                                Slog.v(TAG_SWITCH, "Resuming top, waiting visible to hide: " + prev);
                            }
                        } else if (prev.finishing) {
                            this.mWindowManager.setAppVisibility(prev.appToken, false);
                            if (ActivityManagerDebugConfig.DEBUG_SWITCH) {
                                Slog.v(TAG_SWITCH, "Not waiting for visible to hide: " + prev + ", waitingVisible=" + this.mStackSupervisor.mWaitingVisibleActivities.contains(prev) + ", nowVisible=" + next.nowVisible);
                            }
                        } else if (ActivityManagerDebugConfig.DEBUG_SWITCH) {
                            Slog.v(TAG_SWITCH, "Previous already visible but still waiting to hide: " + prev + ", waitingVisible=" + this.mStackSupervisor.mWaitingVisibleActivities.contains(prev) + ", nowVisible=" + next.nowVisible);
                        }
                    }
                    try {
                        AppGlobals.getPackageManager().setPackageStoppedState(next.packageName, false, next.userId);
                    } catch (RemoteException e) {
                    } catch (IllegalArgumentException e2) {
                        Slog.w(TAG, "Failed trying to unstop package " + next.packageName + ": " + e2);
                    }
                    if (LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(SystemProperties.get("persist.runningbooster.support")) || LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(SystemProperties.get("ro.mtk_aws_support"))) {
                        PackageStoppedStatusChanged eventData1 = PackageStoppedStatusChanged.createInstance();
                        objArr = new Object[3];
                        objArr[0] = next.packageName;
                        objArr[1] = Integer.valueOf(0);
                        objArr[2] = "resumeTopActivityInnerLocked";
                        eventData1.set(objArr);
                        this.mService.getAMEventHook().hook(Event.AM_PackageStoppedStatusChanged, eventData1);
                    }
                    boolean anim = true;
                    if (mHyp == null) {
                        mHyp = new Hypnus();
                    }
                    WindowManagerService windowManagerService;
                    int i2;
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
                            if (prev.task == next.task) {
                                i2 = 7;
                            } else {
                                i2 = 9;
                            }
                            windowManagerService.prepareAppTransition(i2, false);
                            if (!(prev.task == next.task || mHyp == null)) {
                                mHyp.hypnusSetAction(11, 600);
                            }
                        }
                        this.mWindowManager.setAppVisibility(prev.appToken, false);
                    } else {
                        if (ActivityManagerDebugConfig.DEBUG_TRANSITION) {
                            Slog.v(TAG_TRANSITION, "Prepare open transition: prev=" + prev);
                        }
                        if (this.mNoAnimActivities.contains(next)) {
                            anim = false;
                            this.mWindowManager.prepareAppTransition(0, false);
                        } else {
                            windowManagerService = this.mWindowManager;
                            if (prev.task == next.task) {
                                i2 = 6;
                            } else if (next.mLaunchTaskBehind) {
                                i2 = 16;
                            } else {
                                i2 = 8;
                            }
                            windowManagerService.prepareAppTransition(i2, false);
                            if (!(prev.task == next.task || mHyp == null)) {
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
                    this.mComponentName = next.realActivity;
                    if (this.mStackId == 3) {
                        this.mDockComponentName = next.realActivity;
                    }
                    if (this.mLastRecordCmpName == null || !this.mLastRecordCmpName.equals(this.mComponentName)) {
                        this.mLastRecordCmpName = this.mComponentName;
                        String pkgName = this.mLastRecordCmpName != null ? this.mLastRecordCmpName.getPackageName() : null;
                        if (pkgName != null) {
                            if (!pkgName.equals(this.mLastRecordPkgName)) {
                                this.mLastRecordPkgName = pkgName;
                                Time tobj = new Time();
                                tobj.set(System.currentTimeMillis());
                                this.mHandler.postDelayed(new UsageRecorderRunnable(pkgName, tobj.format("%Y-%m-%d %H:%M:%S")), 400);
                            }
                        }
                    }
                    OppoProtectEyeManagerService.getInstance().handleProtectEyeMode(this.mService.mContext, prev, next);
                    try {
                        if (AppGlobals.getPackageManager().hasSystemFeature(OPPO_SECURITYPAY_FEATURE, 0)) {
                            startSecurityPayService(prev, next);
                        }
                    } catch (RemoteException e3) {
                    }
                    if (next.app == null || next.app.thread == null) {
                        if (next.hasBeenLaunched) {
                            next.showStartingWindow(null, true);
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
                        boolean lastActivityTranslucent = lastStack != null ? lastStack.mFullscreen ? lastStack.mLastPausedActivity != null ? !lastStack.mLastPausedActivity.fullscreen : false : true : false;
                        if (!next.visible || next.stopped || lastActivityTranslucent) {
                            this.mWindowManager.setAppVisibility(next.appToken, true);
                        }
                        next.startLaunchTickingLocked();
                        ActivityRecord lastResumedActivity = lastStack == null ? null : lastStack.mResumedActivity;
                        ActivityState lastState = next.state;
                        this.mService.updateCpuStats();
                        if (ActivityManagerDebugConfig.DEBUG_STATES) {
                            Slog.v(TAG_STATES, "Moving to RESUMED: " + next + " (in existing)" + " call by " + Debug.getCallers(8));
                        }
                        next.state = ActivityState.RESUMED;
                        ProcessRecord appProc = next.app;
                        if (appProc != null) {
                            AfterActivityResumed aarEventData = AfterActivityResumed.createInstance();
                            objArr = new Object[6];
                            objArr[0] = Integer.valueOf(appProc.pid);
                            objArr[1] = next.info.name;
                            objArr[2] = next.info.packageName;
                            objArr[3] = Integer.valueOf(next.mActivityType);
                            objArr[4] = Integer.valueOf(appProc.uid);
                            objArr[5] = Integer.valueOf(next.launchedFromUid);
                            aarEventData.set(objArr);
                            this.mService.getAMEventHook().hook(Event.AM_AfterActivityResumed, aarEventData);
                        }
                        this.mResumedActivity = next;
                        if (!(this.mResumedActivity == null || this.mResumedActivity.app == null)) {
                            String value = this.mResumedActivity.processName;
                            if (value != null && value.length() > 50) {
                                value = value.substring(0, 50);
                            }
                            SystemProperties.set("debug.junk.process.name", value);
                            SystemProperties.set("debug.junk.process.pid", Integer.toString(this.mResumedActivity.app.pid));
                        }
                        next.task.touchActiveTime();
                        this.mRecentTasks.addLocked(next.task);
                        this.mService.updateLruProcessLocked(next.app, true, null);
                        updateLRUListLocked(next);
                        this.mService.updateOomAdjLocked();
                        boolean notUpdated = true;
                        if (this.mStackSupervisor.isFocusedStack(this)) {
                            Configuration config = this.mWindowManager.updateOrientationFromAppTokens(this.mService.mConfiguration, next.mayFreezeScreenLocked(next.app) ? next.appToken : null);
                            if (config != null) {
                                next.frozenBeforeDestroy = true;
                            }
                            notUpdated = !this.mService.updateConfigurationLocked(config, next, false);
                        }
                        if (notUpdated) {
                            ActivityRecord nextNext = topRunningActivityLocked();
                            if (ActivityManagerDebugConfig.DEBUG_SWITCH || ActivityManagerDebugConfig.DEBUG_STATES) {
                                Slog.i(TAG_STATES, "Activity config changed during resume: " + next + ", new next: " + nextNext);
                            }
                            if (nextNext != next) {
                                this.mStackSupervisor.scheduleResumeTopActivities();
                            }
                            if (this.mStackSupervisor.reportResumedActivityLocked(next)) {
                                this.mNoAnimActivities.clear();
                                if (ActivityManagerDebugConfig.DEBUG_STACK) {
                                    this.mStackSupervisor.validateTopActivitiesLocked();
                                }
                                return true;
                            }
                            if (ActivityManagerDebugConfig.DEBUG_STACK) {
                                this.mStackSupervisor.validateTopActivitiesLocked();
                            }
                            return false;
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
                            boolean allowSavedSurface = true;
                            if (next.newIntents != null) {
                                for (i = next.newIntents.size() - 1; i >= 0; i--) {
                                    Intent intent = (Intent) next.newIntents.get(i);
                                    if (intent != null && !ActivityRecord.isMainIntent(intent)) {
                                        allowSavedSurface = false;
                                        break;
                                    }
                                }
                                next.app.thread.scheduleNewIntent(next.newIntents, next.appToken, false);
                            }
                            this.mWindowManager.notifyAppResumed(next.appToken, next.stopped, allowSavedSurface);
                            objArr = new Object[4];
                            objArr[0] = Integer.valueOf(next.userId);
                            objArr[1] = Integer.valueOf(System.identityHashCode(next));
                            objArr[2] = Integer.valueOf(next.task.taskId);
                            objArr[3] = next.shortComponentName;
                            EventLog.writeEvent(EventLogTags.AM_RESUME_ACTIVITY, objArr);
                            if (!ActivityManagerService.IS_USER_BUILD || ActivityManagerDebugConfig.DEBUG_TASKS) {
                                Slog.d(TAG, "ACT-AM_RESUME_ACTIVITY " + next + " task:" + next.task.taskId);
                            }
                            next.sleeping = false;
                            this.mService.showUnsupportedZoomDialogIfNeededLocked(next);
                            this.mService.showAskCompatModeDialogLocked(next);
                            next.app.pendingUiClean = true;
                            next.app.forceProcessStateUpTo(this.mService.mTopProcessState);
                            next.clearOptionsLocked();
                            next.app.thread.scheduleResumeActivity(next.appToken, next.app.repProcState, this.mService.isNextTransitionForward(), resumeAnimOptions);
                            this.mStackSupervisor.checkReadyForSleepWhenResumeLocked(next.realActivity);
                            if (ActivityManagerDebugConfig.DEBUG_STATES) {
                                Slog.d(TAG_STATES, "resumeTopActivityLocked: Resumed " + next);
                            }
                            try {
                                completeResumeLocked(next);
                            } catch (Throwable e4) {
                                Slog.w(TAG, "Exception thrown during resume of " + next, e4);
                                requestFinishActivityLocked(next.appToken, 0, null, "resume-exception", true);
                                if (ActivityManagerDebugConfig.DEBUG_STACK) {
                                    this.mStackSupervisor.validateTopActivitiesLocked();
                                }
                                return true;
                            }
                        } catch (Exception e5) {
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
                            } else if (lastStack != null && this.mStackSupervisor.isFrontStack(lastStack)) {
                                next.showStartingWindow(null, true);
                            }
                            this.mStackSupervisor.startSpecificActivityLocked(next, true, false);
                            if (ActivityManagerDebugConfig.DEBUG_STACK) {
                                this.mStackSupervisor.validateTopActivitiesLocked();
                            }
                            return true;
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
        throw th;
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

    private void insertTaskAtPosition(TaskRecord task, int position) {
        if (position >= this.mTaskHistory.size()) {
            insertTaskAtTop(task, null);
            return;
        }
        int maxPosition = this.mTaskHistory.size();
        if (!this.mStackSupervisor.isCurrentProfileLocked(task.userId) && task.topRunningActivityLocked() == null) {
            while (maxPosition > 0) {
                TaskRecord tmpTask = (TaskRecord) this.mTaskHistory.get(maxPosition - 1);
                if (!this.mStackSupervisor.isCurrentProfileLocked(tmpTask.userId) || tmpTask.topRunningActivityLocked() == null) {
                    break;
                }
                maxPosition--;
            }
        }
        position = Math.min(position, maxPosition);
        this.mTaskHistory.remove(task);
        this.mTaskHistory.add(position, task);
        updateTaskMovement(task, true);
    }

    private void insertTaskAtTop(TaskRecord task, ActivityRecord newActivity) {
        boolean isLastTaskOverHome = false;
        if (task.isOverHomeStack()) {
            TaskRecord nextTask = getNextTask(task);
            if (nextTask != null) {
                nextTask.setTaskToReturnTo(task.getTaskToReturnTo());
            } else {
                isLastTaskOverHome = true;
            }
        }
        if (isOnHomeDisplay()) {
            ActivityStack lastStack = this.mStackSupervisor.getLastStack();
            boolean fromHome = lastStack.isHomeStack();
            if (ActivityManagerDebugConfig.DEBUG_TASK_RETURNTO) {
                Slog.d(TAG, "insertTaskAtTop() task " + task + " fromHome=" + fromHome + " isHomeStack=" + isHomeStack() + " topTask=" + topTask());
                if (lastStack != null) {
                    TaskRecord top = lastStack.topTask();
                    Slog.d(TAG, "lastStack=" + lastStack + " lastTop=" + top);
                    if (top != null) {
                        Slog.d(TAG, "lastTopType=" + top.taskType);
                    }
                }
            }
            if (!isHomeStack() && (fromHome || topTask() != task)) {
                int returnToType = isLastTaskOverHome ? task.getTaskToReturnTo() : 0;
                if (fromHome && StackId.allowTopTaskToReturnHome(this.mStackId)) {
                    returnToType = lastStack.topTask() == null ? 1 : lastStack.topTask().taskType;
                }
                task.setTaskToReturnTo(returnToType);
            }
        } else {
            task.setTaskToReturnTo(0);
        }
        if (ActivityManagerDebugConfig.DEBUG_TASKS) {
            Slog.d(TAG, "befor insertTaskAtTop:mTaskHistory." + this.mTaskHistory);
        }
        this.mTaskHistory.remove(task);
        int taskNdx = this.mTaskHistory.size();
        boolean notShownWhenLocked = (newActivity == null || (newActivity.info.flags & 1024) != 0) ? newActivity == null && task.topRunningActivityLocked() == null : true;
        if (!this.mStackSupervisor.isCurrentProfileLocked(task.userId) && notShownWhenLocked) {
            TaskRecord tmpTask;
            do {
                taskNdx--;
                if (taskNdx < 0) {
                    break;
                }
                tmpTask = (TaskRecord) this.mTaskHistory.get(taskNdx);
                if (!this.mStackSupervisor.isCurrentProfileLocked(tmpTask.userId)) {
                    break;
                }
            } while (tmpTask.topRunningActivityLocked() != null);
            taskNdx++;
        }
        this.mTaskHistory.add(taskNdx, task);
        if (ActivityManagerDebugConfig.DEBUG_TASKS) {
            Slog.d(TAG, "after insertTaskAtTop:mTaskHistory." + this.mTaskHistory);
        }
        updateTaskMovement(task, true);
    }

    final void startActivityLocked(ActivityRecord r, boolean newTask, boolean keepCurTransition, ActivityOptions options) {
        TaskRecord rTask = r.task;
        int taskId = rTask.taskId;
        if (!(r == null || r.info == null || r.realActivity == null || !MM_WEB_UI.equals(r.realActivity.getClassName()))) {
            r.info.screenOrientation = 3;
        }
        if (!r.mLaunchTaskBehind && (taskForIdLocked(taskId) == null || newTask)) {
            insertTaskAtTop(rTask, r);
            this.mWindowManager.moveTaskToTop(taskId);
        }
        TaskRecord task = null;
        if (!newTask) {
            boolean startIt = true;
            for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
                task = (TaskRecord) this.mTaskHistory.get(taskNdx);
                if (task.getTopActivity() != null) {
                    if (task == r.task) {
                        if (!startIt) {
                            if (ActivityManagerDebugConfig.DEBUG_ADD_REMOVE) {
                                Slog.i(TAG, "Adding activity " + r + " to task " + task, new RuntimeException("here").fillInStackTrace());
                            }
                            task.addActivityToTop(r);
                            r.putInHistory();
                            addConfigOverride(r, task);
                            ActivityOptions.abort(options);
                            return;
                        }
                    } else if (task.numFullscreen > 0) {
                        startIt = false;
                    }
                }
            }
        }
        if (task == r.task && this.mTaskHistory.indexOf(task) != this.mTaskHistory.size() - 1) {
            this.mStackSupervisor.mUserLeaving = false;
            if (ActivityManagerDebugConfig.DEBUG_USER_LEAVING) {
                Slog.v(TAG_USER_LEAVING, "startActivity() behind front, mUserLeaving=false");
            }
        }
        task = r.task;
        if (ActivityManagerDebugConfig.DEBUG_ADD_REMOVE) {
            Slog.i(TAG, "Adding activity " + r + " to stack to task " + task, new RuntimeException("here").fillInStackTrace());
        }
        task.addActivityToTop(r);
        task.setFrontOfTask();
        r.putInHistory();
        if (!isHomeStack() || numActivities() > 0) {
            boolean showStartingIcon = newTask;
            ProcessRecord proc = r.app;
            if (proc == null) {
                proc = (ProcessRecord) this.mService.mProcessNames.get(r.processName, r.info.applicationInfo.uid);
            }
            if (proc == null || proc.thread == null) {
                showStartingIcon = true;
            }
            if (ActivityManagerDebugConfig.DEBUG_TRANSITION) {
                Slog.v(TAG_TRANSITION, "Prepare open transition: starting " + r);
            }
            if ((r.intent.getFlags() & DumpState.DUMP_INSTALLS) != 0) {
                this.mWindowManager.prepareAppTransition(0, keepCurTransition);
                this.mNoAnimActivities.add(r);
            } else {
                int i;
                WindowManagerService windowManagerService = this.mWindowManager;
                if (!newTask) {
                    i = 6;
                } else if (r.mLaunchTaskBehind) {
                    i = 16;
                } else {
                    i = 8;
                }
                windowManagerService.prepareAppTransition(i, keepCurTransition);
                this.mNoAnimActivities.remove(r);
            }
            addConfigOverride(r, task);
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
                this.mWindowManager.setAppVisibility(r.appToken, true);
                ensureActivitiesVisibleLocked(null, 0, false);
            } else if (!(!doShow || r == null || r.toString().contains("AppUnlock") || r.toString().contains("WhiteBgActivity") || r.toString().contains("com.sina.weibo/.composerinde.ComposerDispatchActivity") || r.toString().contains("com.sina.weibo/.composerinde.OriginalComposerActivity") || ((this.mResumedActivity != null && this.mResumedActivity.toString().contains("com.oppo.camera/.Camera") && r.toString().contains("com.coloros.gallery3d/com.oppo.gallery3d.app.Gallery2")) || r.toString().contains("com.tencent.mm/.plugin.mmsight.ui.SightCaptureUI")))) {
                ActivityRecord prev = r.task.topRunningActivityWithStartingWindowLocked();
                if (prev != null) {
                    if (prev.task != r.task) {
                        prev = null;
                    } else if (prev.nowVisible) {
                        prev = null;
                    }
                }
                r.showStartingWindow(prev, showStartingIcon);
            }
        } else {
            addConfigOverride(r, task);
            ActivityOptions.abort(options);
        }
    }

    final void validateAppTokensLocked() {
        this.mValidateAppTokens.clear();
        this.mValidateAppTokens.ensureCapacity(numActivities());
        int numTasks = this.mTaskHistory.size();
        for (int taskNdx = 0; taskNdx < numTasks; taskNdx++) {
            TaskRecord task = (TaskRecord) this.mTaskHistory.get(taskNdx);
            ArrayList<ActivityRecord> activities = task.mActivities;
            if (!activities.isEmpty()) {
                TaskGroup group = new TaskGroup();
                group.taskId = task.taskId;
                this.mValidateAppTokens.add(group);
                int numActivities = activities.size();
                for (int activityNdx = 0; activityNdx < numActivities; activityNdx++) {
                    group.tokens.add(((ActivityRecord) activities.get(activityNdx)).appToken);
                }
            }
        }
        this.mWindowManager.validateAppTokens(this.mStackId, this.mValidateAppTokens);
    }

    final ActivityOptions resetTargetTaskIfNeededLocked(TaskRecord task, boolean forceReset) {
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
            if (finishOnTaskLaunch || clearWhenTaskReset || target.resultTo == null) {
                boolean noOptions;
                int srcPos;
                ActivityRecord p;
                if (!finishOnTaskLaunch && !clearWhenTaskReset && allowTaskReparenting && target.taskAffinity != null && !target.taskAffinity.equals(task.affinity)) {
                    ActivityRecord bottom;
                    TaskRecord targetTask;
                    int start;
                    if (this.mTaskHistory.isEmpty() || ((TaskRecord) this.mTaskHistory.get(0)).mActivities.isEmpty()) {
                        bottom = null;
                    } else {
                        bottom = (ActivityRecord) ((TaskRecord) this.mTaskHistory.get(0)).mActivities.get(0);
                    }
                    if (bottom == null || target.taskAffinity == null || !target.taskAffinity.equals(bottom.task.affinity)) {
                        targetTask = createTaskRecord(this.mStackSupervisor.getNextTaskIdForUserLocked(target.userId), target.info, null, null, null, false);
                        targetTask.affinityIntent = target.intent;
                        if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                            Slog.v(TAG_TASKS, "Start pushing activity " + target + " out to new task " + target.task);
                        }
                    } else {
                        targetTask = bottom.task;
                        if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                            Slog.v(TAG_TASKS, "Start pushing activity " + target + " out to bottom task " + bottom.task);
                        }
                    }
                    setAppTask(target, targetTask);
                    noOptions = canMoveOptions;
                    if (replyChainEnd < 0) {
                        start = i;
                    } else {
                        start = replyChainEnd;
                    }
                    for (srcPos = start; srcPos >= i; srcPos--) {
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
                                Slog.v(TAG_TASKS, "Pushing next activity " + p + " out to target's task " + target.task);
                            }
                            p.setTask(targetTask, null);
                            targetTask.addActivityAtBottom(p);
                            targetTask.setFrontOfTask();
                            setAppTask(p, targetTask);
                        }
                    }
                    this.mWindowManager.moveTaskToBottom(targetTask.taskId);
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
                            p.setTask(task, null);
                            task.addActivityAtIndex(taskInsertionPoint, p);
                            if (ActivityManagerDebugConfig.DEBUG_ADD_REMOVE) {
                                Slog.i(TAG_ADD_REMOVE, "Removing and adding activity " + p + " to stack at " + task + " callers=" + Debug.getCallers(3));
                            }
                            if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                                Slog.v(TAG_TASKS, "Pulling activity " + p + " from " + srcPos + " in to resetting task " + task);
                            }
                            setAppTask(p, task);
                        }
                        this.mWindowManager.moveTaskToTop(taskId);
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
        TaskRecord task = taskTop.task;
        boolean taskFound = false;
        ActivityOptions topOptions = null;
        int reparentInsertionPoint = -1;
        for (int i = this.mTaskHistory.size() - 1; i >= 0; i--) {
            TaskRecord targetTask = (TaskRecord) this.mTaskHistory.get(i);
            if (targetTask == null || task == null || targetTask.userId == task.userId || targetTask.realActivity == null || !OppoMultiLauncherUtil.getInstance().isMultiApp(targetTask.realActivity.getPackageName())) {
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

    private void adjustFocusedActivityLocked(ActivityRecord r, String reason) {
        if (ActivityManagerDebugConfig.DEBUG_FOCUS) {
            Slog.v(TAG, "adjustFocusedActivityLocked: r= " + r + " mFocusedActivity " + this.mService.mFocusedActivity + " reason " + reason);
        }
        if (this.mStackSupervisor.isFocusedStack(this) && (this.mService.mFocusedActivity == r || this.mService.mFocusedActivity == null)) {
            ActivityRecord next = topRunningActivityLocked();
            String myReason = reason + " adjustFocus";
            if (ActivityManagerDebugConfig.DEBUG_FOCUS) {
                Slog.v(TAG, "next: = " + next);
            }
            if (!(r == null || r.shortComponentName == null || !r.shortComponentName.contains("com.coloros.safecenter/.privacy.view.password"))) {
                boolean isFinish = false;
                if (myReason != null && myReason.contains("finishActivity")) {
                    isFinish = true;
                }
                if (this.mStackSupervisor.getStack(3, false, false) != null && isFinish) {
                    this.mStackSupervisor.moveTasksToFullscreenStackLocked(3, true);
                    return;
                }
            }
            if (next != r) {
                if (MultiWindowManager.isSupported()) {
                    if (MultiWindowManager.DEBUG) {
                        Slog.d(TAG, "adjustFocusedActivityLocked, r = " + r + ", r.task = " + r.task + ", topTask() = " + topTask() + ", r.frontOfTask" + r.frontOfTask);
                    }
                    if (r.task.mSticky && r.frontOfTask && r.task == topTask()) {
                        this.mService.stickWindow(r.task, false);
                    }
                }
                if (next != null && StackId.keepFocusInStackIfPossible(this.mStackId) && isFocusable()) {
                    this.mService.setFocusedActivityLocked(next, myReason);
                    return;
                }
                TaskRecord task = r.task;
                if (r.frontOfTask && task == topTask() && task.isOverHomeStack()) {
                    int taskToReturnTo = task.getTaskToReturnTo();
                    if ((!this.mFullscreen && adjustFocusToNextFocusableStackLocked(taskToReturnTo, myReason)) || this.mStackSupervisor.moveHomeStackTaskToTop(taskToReturnTo, myReason)) {
                        return;
                    }
                }
            }
            this.mService.setFocusedActivityLocked(this.mStackSupervisor.topRunningActivityLocked(), myReason);
        }
    }

    private boolean adjustFocusToNextFocusableStackLocked(int taskToReturnTo, String reason) {
        ActivityStack stack = getNextFocusableStackLocked();
        String myReason = reason + " adjustFocusToNextFocusableStack";
        if (stack == null) {
            return false;
        }
        ActivityRecord top = stack.topRunningActivityLocked();
        boolean isSwapDocked = false;
        if (reason != null && reason.contains(SWAP_DOCKED_STACK)) {
            isSwapDocked = true;
        }
        if (!this.mFullscreen && !isSwapDocked) {
            boolean result;
            if (stack.isHomeStack() && (top == null || !top.visible)) {
                ActivityStack tmpStack;
                if (isDockedStack()) {
                    tmpStack = this.mStackSupervisor.getStack(1, false, false);
                } else {
                    tmpStack = this.mStackSupervisor.getStack(3, false, false);
                }
                if (tmpStack != null) {
                    ActivityRecord tmpTop = tmpStack.topRunningActivityLocked();
                    if (tmpTop != null && tmpTop.visible) {
                        result = this.mService.setFocusedActivityLocked(tmpTop, myReason);
                        tmpTop.mSplitBackState = top.SPLIT_BACEING;
                        this.mWindowManager.setSplitFromBack(true);
                        this.mStackSupervisor.moveTasksToFullscreenStackLocked(3, true);
                        this.mWindowManager.setFreeingChange(true);
                        this.mWindowManager.startFreezingScreen(0, 0);
                        return result;
                    }
                }
            } else if (stack.isHomeStack() && top != null && top.visible) {
                if (!isHomeStack() && this.mStackId == 1) {
                    return this.mService.setFocusedActivityLocked(top, myReason);
                }
                if (this.mService.mConfiguration != null && this.mService.mConfiguration.orientation == 1) {
                    this.mWindowManager.setFreeingChange(true);
                    this.mWindowManager.startFreezingScreen(0, 0);
                }
                return this.mStackSupervisor.moveHomeStackTaskToTop(taskToReturnTo, reason);
            }
            if (this.mService.mFocusedActivity != null) {
                this.mService.mFocusedActivity.mSplitBackState = top.SPLIT_BACEING;
            }
            result = this.mService.setFocusedActivityLocked(top, myReason);
            this.mWindowManager.setSplitFromBack(true);
            if (!isDockedStack()) {
                this.mStackSupervisor.moveTasksToFullscreenStackLocked(3, true);
            }
            this.mWindowManager.setFreeingChange(true);
            this.mWindowManager.startFreezingScreen(0, 0);
            return result;
        } else if (!stack.isHomeStack() || (top != null && top.visible)) {
            return this.mService.setFocusedActivityLocked(top, myReason);
        } else {
            return this.mStackSupervisor.moveHomeStackTaskToTop(taskToReturnTo, reason);
        }
    }

    final void stopActivityLocked(ActivityRecord r) {
        if (ActivityManagerDebugConfig.DEBUG_SWITCH) {
            Slog.d(TAG_SWITCH, "Stopping: " + r);
        }
        if (!(((r.intent.getFlags() & 1073741824) == 0 && (r.info.flags & 128) == 0) || r.finishing)) {
            if (!this.mService.isSleepingLocked()) {
                if (ActivityManagerDebugConfig.DEBUG_STATES) {
                    Slog.d(TAG_STATES, "no-history finish of " + r);
                }
                if (requestFinishActivityLocked(r.appToken, 0, null, "stop-no-history", false)) {
                    adjustFocusedActivityLocked(r, "stopActivityFinished");
                    r.resumeKeyDispatchingLocked();
                    return;
                }
            } else if (ActivityManagerDebugConfig.DEBUG_STATES) {
                Slog.d(TAG_STATES, "Not finishing noHistory " + r + " on stop because we're just sleeping");
            }
        }
        if (!(r.app == null || r.app.thread == null)) {
            adjustFocusedActivityLocked(r, "stopActivity");
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
                if (!r.visible) {
                    this.mWindowManager.setAppVisibility(r.appToken, false);
                }
                EventLogTags.writeAmStopActivity(r.userId, System.identityHashCode(r), r.shortComponentName);
                r.app.thread.scheduleStopActivity(r.appToken, r.visible, r.configChangeFlags);
                if (this.mService.isSleepingOrShuttingDownLocked()) {
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
                ProcessRecord appProc = r.app;
                if (appProc != null) {
                    AfterActivityStopped eventData = AfterActivityStopped.createInstance();
                    Object[] objArr = new Object[3];
                    objArr[0] = Integer.valueOf(appProc.pid);
                    objArr[1] = r.info.name;
                    objArr[2] = r.info.packageName;
                    eventData.set(objArr);
                    this.mService.getAMEventHook().hook(Event.AM_AfterActivityStopped, eventData);
                }
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

    /* JADX WARNING: Missing block: B:30:0x00dd, code:
            if (r2.state == com.android.server.am.ActivityStack.ActivityState.PAUSED) goto L_0x0089;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    final TaskRecord finishTopRunningActivityLocked(ProcessRecord app, String reason) {
        ActivityRecord r = topRunningActivityLocked();
        if (r == null || r.app != app) {
            return null;
        }
        Slog.w(TAG, "  Force finishing activity " + r.intent.getComponent().flattenToShortString());
        int taskNdx = this.mTaskHistory.indexOf(r.task);
        int activityNdx = r.task.mActivities.indexOf(r);
        finishActivityLocked(r, 0, null, reason, false);
        TaskRecord finishedTask = r.task;
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
        ArrayList<ActivityRecord> activities = r.task.mActivities;
        for (int index = activities.indexOf(r); index >= 0; index--) {
            ActivityRecord cur = (ActivityRecord) activities.get(index);
            if (!Objects.equals(cur.taskAffinity, r.taskAffinity)) {
                break;
            }
            finishActivityLocked(cur, 0, null, "request-affinity", true);
        }
        return true;
    }

    final void finishActivityResultsLocked(ActivityRecord r, int resultCode, Intent resultData) {
        boolean z = false;
        ActivityRecord resultTo = r.resultTo;
        if (resultTo != null) {
            if (ActivityManagerDebugConfig.DEBUG_RESULTS) {
                Slog.v(TAG_RESULTS, "Adding result to " + resultTo + " who=" + r.resultWho + " req=" + r.requestCode + " res=" + resultCode + " data=" + resultData);
            }
            if (!(resultTo.userId == r.userId || resultData == null)) {
                if (OppoMultiAppManager.USER_ID == resultTo.userId && r.packageName != null && "com.android.documentsui".equals(r.packageName) && resultTo.packageName != null && "com.imo.android.imoim".equals(resultTo.packageName) && resultData.getDataString() != null) {
                    z = resultData.getDataString().contains("com.android.providers.media.documents");
                }
                if (!z) {
                    resultData.prepareToLeaveUser(r.userId);
                }
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
        String componentName = IElsaManager.EMPTY_PACKAGE;
        if (r != null) {
            componentName = r.shortComponentName;
        }
        if (!(r == null || r.intent == null || (componentName.endsWith("ChooserActivity") && (r.intent.getFlags() & 1024) == 0))) {
            this.mStackSupervisor.mOppoSecureProtectUtils.handleFinishActivityLocked(this);
        }
        if (r.finishing) {
            Slog.w(TAG, "Duplicate finish request for " + r);
            return false;
        }
        r.makeFinishingLocked();
        TaskRecord task = r.task;
        Object[] objArr = new Object[5];
        objArr[0] = Integer.valueOf(r.userId);
        objArr[1] = Integer.valueOf(System.identityHashCode(r));
        objArr[2] = Integer.valueOf(task.taskId);
        objArr[3] = r.shortComponentName;
        objArr[4] = reason;
        EventLog.writeEvent(EventLogTags.AM_FINISH_ACTIVITY, objArr);
        if (!ActivityManagerService.IS_USER_BUILD || ActivityManagerDebugConfig.DEBUG_TASKS) {
            Slog.d(TAG, "ACT-AM_FINISH_ACTIVITY " + r + " task:" + r.task + " " + reason);
        }
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
        adjustFocusedActivityLocked(r, "finishActivity");
        finishActivityResultsLocked(r, resultCode, resultData);
        boolean endTask = index <= 0;
        int transit = endTask ? 9 : 7;
        ActivityRecord next;
        if (this.mResumedActivity == r) {
            if (ActivityManagerDebugConfig.DEBUG_VISIBILITY || ActivityManagerDebugConfig.DEBUG_TRANSITION) {
                Slog.v(TAG_TRANSITION, "Prepare close transition: finishing " + r);
            }
            this.mWindowManager.prepareAppTransition(transit, false);
            this.mWindowManager.setAppVisibility(r.appToken, false);
            if (this.mPausingActivity == null) {
                if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
                    Slog.v(TAG_PAUSE, "Finish needs to pause: " + r);
                }
                if (ActivityManagerDebugConfig.DEBUG_USER_LEAVING) {
                    Slog.v(TAG_USER_LEAVING, "finish() => pause with userLeaving=false");
                }
                startPausingLocked(false, false, null, false, "finish-request");
                next = this.mStackSupervisor.getFocusedStack().topRunningActivityLocked();
                if (!(next == null || next.info == null || (next.info.packageName == this.mStackSupervisor.mLastResumedActivity.packageName && next.info.name == this.mStackSupervisor.mLastResumedActivity.activityName))) {
                    BeforeActivitySwitch eventData = BeforeActivitySwitch.createInstance();
                    objArr = new Object[10];
                    objArr[0] = this.mStackSupervisor.mLastResumedActivity.activityName;
                    objArr[1] = next.info.name;
                    objArr[2] = this.mStackSupervisor.mLastResumedActivity.packageName;
                    objArr[3] = next.info.packageName;
                    objArr[4] = Integer.valueOf(this.mStackSupervisor.mLastResumedActivity.activityType);
                    objArr[5] = Integer.valueOf(next.mActivityType);
                    objArr[6] = Boolean.valueOf(true);
                    objArr[7] = null;
                    objArr[8] = null;
                    objArr[9] = null;
                    eventData.set(objArr);
                    this.mService.getAMEventHook().hook(Event.AM_BeforeActivitySwitch, eventData);
                }
            }
            if (endTask) {
                this.mStackSupervisor.removeLockedTaskLocked(task);
            }
        } else if (r.state != ActivityState.PAUSING) {
            boolean z;
            if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
                Slog.v(TAG_PAUSE, "Finish not pausing: " + r);
            }
            next = this.mStackSupervisor.topRunningActivityLocked();
            if (!(next == null || !"com.android.stk/.StkMenuActivity".equals(next.shortComponentName) || (next.nowVisible && next.fullscreen))) {
                this.mStackSupervisor.ensureActivitiesVisibleLocked(null, 0, false);
            }
            if (r.visible) {
                this.mWindowManager.prepareAppTransition(transit, false);
                this.mWindowManager.setAppVisibility(r.appToken, false);
                this.mWindowManager.executeAppTransition();
                if (!this.mStackSupervisor.mWaitingVisibleActivities.contains(r)) {
                    this.mStackSupervisor.mWaitingVisibleActivities.add(r);
                }
            }
            int i = (r.visible || r.nowVisible) ? 2 : 1;
            if (finishCurrentActivityLocked(r, i, oomAdj) == null) {
                z = true;
            } else {
                z = false;
            }
            return z;
        } else if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
            Slog.v(TAG_PAUSE, "Finish waiting for pause of: " + r);
        }
        return false;
    }

    final ActivityRecord finishCurrentActivityLocked(ActivityRecord r, int mode, boolean oomAdj) {
        ActivityRecord next = this.mStackSupervisor.topRunningActivityLocked();
        if (mode != 2 || (!(r.visible || r.nowVisible) || next == null || next.nowVisible)) {
            this.mStackSupervisor.mStoppingActivities.remove(r);
            this.mStackSupervisor.mGoingToSleepActivities.remove(r);
            this.mStackSupervisor.mWaitingVisibleActivities.remove(r);
            if (this.mResumedActivity == r) {
                this.mResumedActivity = null;
            }
            ActivityState prevState = r.state;
            if (ActivityManagerDebugConfig.DEBUG_STATES) {
                Slog.v(TAG_STATES, "Moving to FINISHING: " + r);
            }
            r.state = ActivityState.FINISHING;
            boolean finishingActivityInNonFocusedStack = (r.task.stack == this.mStackSupervisor.getFocusedStack() || prevState != ActivityState.PAUSED) ? false : mode == 2;
            if (mode == 0 || ((prevState == ActivityState.PAUSED && (mode == 1 || this.mStackId == 4)) || finishingActivityInNonFocusedStack || prevState == ActivityState.STOPPED || prevState == ActivityState.INITIALIZING)) {
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
            if (ActivityManagerDebugConfig.DEBUG_ALL) {
                Slog.v(TAG, "Enqueueing pending finish: " + r);
            }
            this.mStackSupervisor.mFinishingActivities.add(r);
            r.resumeKeyDispatchingLocked();
            this.mStackSupervisor.resumeFocusedStackTopActivityLocked();
            return r;
        }
        if (!this.mStackSupervisor.mStoppingActivities.contains(r)) {
            if (r.realActivity.flattenToString().equals("com.oppo.camera/com.oppo.camera.Camera")) {
                addToStopping(r, true);
            } else {
                addToStopping(r, false);
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
                if (!r.finishing || immediately) {
                    Slog.d(TAG, "finishAllActivitiesLocked: finishing " + r + " immediately");
                    finishCurrentActivityLocked(r, 0, false);
                }
            }
        }
        if (noActivitiesInStack) {
            this.mActivityContainer.onTaskListEmptyLocked();
        }
    }

    final boolean shouldUpRecreateTaskLocked(ActivityRecord srec, String destAffinity) {
        if (srec == null || srec.task.affinity == null || !srec.task.affinity.equals(destAffinity)) {
            return true;
        }
        if (srec.frontOfTask && srec.task != null && srec.task.getBaseIntent() != null && srec.task.getBaseIntent().isDocument()) {
            if (srec.task.getTaskToReturnTo() != 0) {
                return true;
            }
            int taskIdx = this.mTaskHistory.indexOf(srec.task);
            if (taskIdx <= 0) {
                Slog.w(TAG, "shouldUpRecreateTask: task not in history for " + srec);
                return false;
            } else if (taskIdx == 0) {
                return true;
            } else {
                if (!srec.task.affinity.equals(((TaskRecord) this.mTaskHistory.get(taskIdx)).affinity)) {
                    return true;
                }
            }
        }
        return false;
    }

    final boolean navigateUpToLocked(ActivityRecord srec, Intent destIntent, int resultCode, Intent resultData) {
        TaskRecord task = srec.task;
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
                    foundParentInTask = this.mService.mActivityStarter.startActivityLocked(srec.app.thread, destIntent, null, null, AppGlobals.getPackageManager().getActivityInfo(destIntent.getComponent(), 0, srec.userId), null, null, null, parent.appToken, null, 0, -1, parent.launchedFromUid, parent.launchedFromPackage, -1, parent.launchedFromUid, 0, null, false, true, null, null, null) == 0;
                } catch (RemoteException e2) {
                    foundParentInTask = false;
                }
                requestFinishActivityLocked(parent.appToken, resultCode, resultData, "navigate-top", true);
            }
        }
        Binder.restoreCallingIdentity(origId);
        return foundParentInTask;
    }

    final void cleanUpActivityLocked(ActivityRecord r, boolean cleanServices, boolean setState) {
        if (this.mResumedActivity == r) {
            this.mResumedActivity = null;
        }
        if (this.mPausingActivity == r) {
            this.mPausingActivity = null;
        }
        this.mService.resetFocusedActivityIfNeededLocked(r);
        r.deferRelaunchUntilPaused = false;
        r.frozenBeforeDestroy = false;
        if (setState) {
            if (ActivityManagerDebugConfig.DEBUG_STATES) {
                Slog.v(TAG_STATES, "Moving to DESTROYED: " + r + " (cleaning up)");
            }
            r.state = ActivityState.DESTROYED;
            ProcessRecord appProc = r.app;
            if (appProc != null) {
                AfterActivityDestroyed eventData = AfterActivityDestroyed.createInstance();
                Object[] objArr = new Object[3];
                objArr[0] = Integer.valueOf(appProc.pid);
                objArr[1] = r.info.name;
                objArr[2] = r.info.packageName;
                eventData.set(objArr);
                this.mService.getAMEventHook().hook(Event.AM_AfterActivityDestroyed, eventData);
            }
            if (ActivityManagerDebugConfig.DEBUG_APP) {
                Slog.v(TAG_APP, "Clearing app during cleanUp for activity " + r);
            }
            r.app = null;
        }
        this.mStackSupervisor.mFinishingActivities.remove(r);
        this.mStackSupervisor.mWaitingVisibleActivities.remove(r);
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
        if (getVisibleBehindActivity() == r) {
            this.mStackSupervisor.requestVisibleBehindLocked(r, false);
        }
        this.mWindowManager.notifyAppRelaunchesCleared(r.appToken);
    }

    private void removeTimeoutsForActivityLocked(ActivityRecord r) {
        this.mStackSupervisor.removeTimeoutsForActivityLocked(r);
        this.mHandler.removeMessages(101, r);
        this.mHandler.removeMessages(104, r);
        this.mHandler.removeMessages(102, r);
        r.finishLaunchTickingLocked();
    }

    void removeActivityFromHistoryLocked(ActivityRecord r, TaskRecord oldTop, String reason) {
        this.mStackSupervisor.removeChildActivityContainers(r);
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
        ProcessRecord appProc = r.app;
        if (appProc != null) {
            AfterActivityDestroyed eventData = AfterActivityDestroyed.createInstance();
            Object[] objArr = new Object[3];
            objArr[0] = Integer.valueOf(appProc.pid);
            objArr[1] = r.info.name;
            objArr[2] = r.info.packageName;
            eventData.set(objArr);
            this.mService.getAMEventHook().hook(Event.AM_AfterActivityDestroyed, eventData);
        }
        if (ActivityManagerDebugConfig.DEBUG_APP) {
            Slog.v(TAG_APP, "Clearing app during remove for activity " + r);
        }
        r.app = null;
        this.mWindowManager.removeAppToken(r.appToken);
        TaskRecord task = r.task;
        TaskRecord topTask = oldTop != null ? oldTop : topTask();
        if (task != null && task.removeActivity(r)) {
            if (ActivityManagerDebugConfig.DEBUG_STACK) {
                Slog.i(TAG_STACK, "removeActivityFromHistoryLocked: last activity removed from " + this);
            }
            if (this.mStackSupervisor.isFocusedStack(this) && task == topTask && task.isOverHomeStack()) {
                if (MultiWindowManager.isSupported() && task.mSticky) {
                    this.mService.stickWindow(task, false);
                }
                if (this.mFullscreen && !isDockedStack()) {
                    this.mStackSupervisor.moveHomeStackTaskToTop(task.getTaskToReturnTo(), reason);
                }
            }
            removeTask(task, reason);
        }
        cleanUpActivityServicesLocked(r);
        r.removeUriPermissionsLocked();
    }

    final void cleanUpActivityServicesLocked(ActivityRecord r) {
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

    final void destroyActivitiesLocked(ProcessRecord owner, String reason) {
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

    /* JADX WARNING: Removed duplicated region for block: B:49:0x01ba  */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x01e8  */
    /* JADX WARNING: Removed duplicated region for block: B:55:0x021f  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    final boolean destroyActivityLocked(ActivityRecord r, boolean removeFromApp, String reason) {
        if (ActivityManagerDebugConfig.DEBUG_SWITCH || ActivityManagerDebugConfig.DEBUG_CLEANUP) {
            Slog.v(TAG_SWITCH, "Removing activity from " + reason + ": token=" + r + ", app=" + (r.app != null ? r.app.processName : "(null)"));
        }
        Object[] objArr = new Object[5];
        objArr[0] = Integer.valueOf(r.userId);
        objArr[1] = Integer.valueOf(System.identityHashCode(r));
        objArr[2] = Integer.valueOf(r.task.taskId);
        objArr[3] = r.shortComponentName;
        objArr[4] = reason;
        EventLog.writeEvent(EventLogTags.AM_DESTROY_ACTIVITY, objArr);
        if (!ActivityManagerService.IS_USER_BUILD || ActivityManagerDebugConfig.DEBUG_TASKS) {
            Slog.d(TAG, "ACT-Removing activity from " + reason + ": token=" + r + ", app=" + (r.app != null ? r.app.processName : "(null)"));
        }
        boolean removedFromHistory = false;
        TaskRecord topTask = topTask();
        cleanUpActivityLocked(r, false, false);
        boolean hadApp = r.app != null;
        ProcessRecord appProc;
        AfterActivityDestroyed eventData;
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
                if (top == null || p == null || !ActivityManagerService.OPPO_LAUNCHER.equals(top.packageName) || p.equals(top.packageName)) {
                    SystemProperties.set("debug.sys.oppo.keytime", "0");
                    r.nowVisible = false;
                    if (r.finishing || skipDestroy) {
                        if (ActivityManagerDebugConfig.DEBUG_STATES) {
                            Slog.v(TAG_STATES, "Moving to DESTROYED: " + r + " (destroy skipped)");
                        }
                        r.state = ActivityState.DESTROYED;
                        appProc = r.app;
                        if (appProc != null) {
                            eventData = AfterActivityDestroyed.createInstance();
                            objArr = new Object[3];
                            objArr[0] = Integer.valueOf(appProc.pid);
                            objArr[1] = r.info.name;
                            objArr[2] = r.info.packageName;
                            eventData.set(objArr);
                            this.mService.getAMEventHook().hook(Event.AM_AfterActivityDestroyed, eventData);
                        }
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
                        this.mService.mHandler.sendMessageDelayed(this.mService.mHandler.obtainMessage(511, top.userId, -1, p), 1000);
                    }
                    r.nowVisible = false;
                    if (r.finishing) {
                    }
                    if (ActivityManagerDebugConfig.DEBUG_STATES) {
                    }
                    r.state = ActivityState.DESTROYED;
                    appProc = r.app;
                    if (appProc != null) {
                    }
                    if (ActivityManagerDebugConfig.DEBUG_APP) {
                    }
                    r.app = null;
                }
            } catch (Exception e) {
                if (ActivityManagerDebugConfig.DEBUG_AMS) {
                    Slog.w(TAG, "Exception thrown during finish", e);
                }
                if (r.finishing) {
                    removeActivityFromHistoryLocked(r, topTask, reason + " exceptionInScheduleDestroy");
                    removedFromHistory = true;
                    skipDestroy = true;
                }
            }
        } else if (r.finishing) {
            removeActivityFromHistoryLocked(r, topTask, reason + " hadNoApp");
            removedFromHistory = true;
        } else {
            if (ActivityManagerDebugConfig.DEBUG_STATES) {
                Slog.v(TAG_STATES, "Moving to DESTROYED: " + r + " (no app)");
            }
            r.state = ActivityState.DESTROYED;
            appProc = r.app;
            if (appProc != null) {
                eventData = AfterActivityDestroyed.createInstance();
                objArr = new Object[3];
                objArr[0] = Integer.valueOf(appProc.pid);
                objArr[1] = r.info.name;
                objArr[2] = r.info.packageName;
                eventData.set(objArr);
                this.mService.getAMEventHook().hook(Event.AM_AfterActivityDestroyed, eventData);
            }
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
                removeActivityFromHistoryLocked(r, null, reason);
            }
            this.mStackSupervisor.resumeFocusedStackTopActivityLocked();
        } finally {
            Binder.restoreCallingIdentity(origId);
        }
    }

    void releaseBackgroundResources(ActivityRecord r) {
        if (hasVisibleBehindActivity() && !this.mHandler.hasMessages(107) && (r != topRunningActivityLocked() || getStackVisibilityLocked(null) != 1)) {
            if (ActivityManagerDebugConfig.DEBUG_STATES) {
                Slog.d(TAG_STATES, "releaseBackgroundResources activtyDisplay=" + this.mActivityContainer.mActivityDisplay + " visibleBehind=" + r + " app=" + r.app + " thread=" + r.app.thread);
            }
            if (r == null || r.app == null || r.app.thread == null) {
                Slog.e(TAG, "releaseBackgroundResources: activity " + r + " no longer running");
                backgroundResourcesReleased();
            }
            try {
                r.app.thread.scheduleCancelVisibleBehind(r.appToken);
            } catch (RemoteException e) {
            }
            this.mHandler.sendEmptyMessageDelayed(107, 500);
        }
    }

    final void backgroundResourcesReleased() {
        this.mHandler.removeMessages(107);
        ActivityRecord r = getVisibleBehindActivity();
        if (r != null) {
            this.mStackSupervisor.mStoppingActivities.add(r);
            setVisibleBehindActivity(null);
            this.mStackSupervisor.scheduleIdleTimeoutLocked(null);
        }
        this.mStackSupervisor.resumeFocusedStackTopActivityLocked();
    }

    boolean hasVisibleBehindActivity() {
        return isAttached() ? this.mActivityContainer.mActivityDisplay.hasVisibleBehindActivity() : false;
    }

    void setVisibleBehindActivity(ActivityRecord r) {
        if (isAttached()) {
            this.mActivityContainer.mActivityDisplay.setVisibleBehindActivity(r);
        }
    }

    ActivityRecord getVisibleBehindActivity() {
        return isAttached() ? this.mActivityContainer.mActivityDisplay.mVisibleBehindActivity : null;
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
                this.mWindowManager.notifyAppRelaunchesCleared(r.appToken);
                removeTimeoutsForActivityLocked(r);
            }
        }
    }

    boolean removeHistoryRecordsForAppLocked(ProcessRecord app) {
        removeHistoryRecordsForAppLocked(this.mLRUActivities, app, "mLRUActivities");
        removeHistoryRecordsForAppLocked(this.mStackSupervisor.mStoppingActivities, app, "mStoppingActivities");
        removeHistoryRecordsForAppLocked(this.mStackSupervisor.mGoingToSleepActivities, app, "mGoingToSleepActivities");
        removeHistoryRecordsForAppLocked(this.mStackSupervisor.mWaitingVisibleActivities, app, "mWaitingVisibleActivities");
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
                    if ((!r.haveState && !r.stateNotNeeded) || r.finishing) {
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
                            Object[] objArr = new Object[5];
                            objArr[0] = Integer.valueOf(r.userId);
                            objArr[1] = Integer.valueOf(System.identityHashCode(r));
                            objArr[2] = Integer.valueOf(r.task.taskId);
                            objArr[3] = r.shortComponentName;
                            objArr[4] = "proc died without state saved";
                            EventLog.writeEvent(EventLogTags.AM_FINISH_ACTIVITY, objArr);
                            if (r.state == ActivityState.RESUMED) {
                                this.mService.updateUsageStats(r, false);
                            }
                        }
                    } else {
                        if (ActivityManagerDebugConfig.DEBUG_ALL) {
                            Slog.v(TAG, "Keeping entry, setting app to null");
                        }
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
                        removeActivityFromHistoryLocked(r, null, "appDied");
                    }
                }
            }
        }
        return hasVisibleActivities;
    }

    final void updateTransitLocked(int transit, ActivityOptions options) {
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

    void updateTaskMovement(TaskRecord task, boolean toFront) {
        if (task.isPersistable) {
            task.mLastTimeMoved = System.currentTimeMillis();
            if (!toFront) {
                task.mLastTimeMoved *= -1;
            }
        }
        this.mStackSupervisor.invalidateTaskLayers();
    }

    void moveHomeStackTaskToTop(int homeStackTaskType) {
        int top = this.mTaskHistory.size() - 1;
        for (int taskNdx = top; taskNdx >= 0; taskNdx--) {
            TaskRecord task = (TaskRecord) this.mTaskHistory.get(taskNdx);
            if (task.taskType == homeStackTaskType) {
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
        if (!ActivityManagerService.IS_USER_BUILD || ActivityManagerDebugConfig.DEBUG_TASKS || ActivityManagerDebugConfig.DEBUG_MULTIWINDOW) {
            Slog.d(TAG, "ACT-moveTaskToFront: " + tr + " mStackId=" + this.mStackId + " reason=" + reason + " " + Debug.getCallers(5));
        }
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
        if (timeTracker != null) {
            for (int i = tr.mActivities.size() - 1; i >= 0; i--) {
                ((ActivityRecord) tr.mActivities.get(i)).appTimeTracker = timeTracker;
            }
        }
        insertTaskAtTop(tr, null);
        ActivityRecord top = tr.getTopActivity();
        if (okToShowLocked(top)) {
            ActivityRecord r = topRunningActivityLocked();
            if (r != null) {
                this.mService.setFocusedActivityLocked(r, reason);
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
            this.mStackSupervisor.resumeFocusedStackTopActivityLocked();
            Object[] objArr = new Object[2];
            objArr[0] = Integer.valueOf(tr.userId);
            objArr[1] = Integer.valueOf(tr.taskId);
            EventLog.writeEvent(EventLogTags.AM_TASK_TO_FRONT, objArr);
            if (!ActivityManagerService.IS_USER_BUILD || ActivityManagerDebugConfig.DEBUG_TASKS) {
                Slog.d(TAG, "ACT-AM_TASK_TO_FRONT: " + tr);
            }
            return;
        }
        addRecentActivityLocked(top);
        ActivityOptions.abort(options);
    }

    /* JADX WARNING: Removed duplicated region for block: B:134:0x04b1 A:{SKIP} */
    /* JADX WARNING: Removed duplicated region for block: B:81:0x02dd  */
    /* JADX WARNING: Removed duplicated region for block: B:89:0x0336  */
    /* JADX WARNING: Removed duplicated region for block: B:111:0x03c3  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    final boolean moveTaskToBackLocked(int taskId) {
        TaskRecord tr = taskForIdLocked(taskId);
        if (tr == null) {
            Slog.i(TAG, "moveTaskToBack: bad taskId=" + taskId);
            return false;
        }
        TaskRecord task;
        int taskToReturnTo;
        if (ActivityManagerDebugConfig.DEBUG_TASKS || ActivityManagerDebugConfig.DEBUG_STACK) {
            Slog.i(TAG, "moveTaskToBack: " + tr);
        }
        this.mStackSupervisor.removeLockedTaskLocked(tr);
        ActivityRecord next = null;
        if (this.mStackSupervisor.isFrontStack(this) && this.mService.mController != null) {
            next = topRunningActivityLocked(null, taskId);
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
        if (this.mStackId == 0 && topTask().isHomeTask()) {
            ActivityStack fullscreenStack = this.mStackSupervisor.getStack(1);
            if (fullscreenStack != null && fullscreenStack.hasVisibleBehindActivity()) {
                this.mService.setFocusedActivityLocked(fullscreenStack.getVisibleBehindActivity(), "moveHomeTaskToBack");
                this.mStackSupervisor.resumeFocusedStackTopActivityLocked();
                return true;
            }
        }
        boolean moveTopTaskInFrontStackToBack = false;
        if (this.mStackSupervisor.isFrontStack(this) && tr == topTask()) {
            if (ActivityManagerDebugConfig.DEBUG_TASKS || ActivityManagerDebugConfig.DEBUG_STACK) {
                Slog.d(TAG, "moveTaskToBackLocked:current stack is front, task prepare to move back is top task:" + tr);
            }
            moveTopTaskInFrontStackToBack = true;
        }
        boolean prevIsHome = false;
        boolean hasOverHome = false;
        boolean canGoHome = !tr.isHomeTask() ? tr.isOverHomeStack() : false;
        if (ActivityManagerDebugConfig.DEBUG_TASKS) {
            Slog.w(TAG, "tr " + tr + " canGoHome " + canGoHome);
        }
        if (canGoHome) {
            TaskRecord nextTask = getNextTask(tr);
            if (ActivityManagerDebugConfig.DEBUG_AMS) {
                Slog.w(TAG, "nextTask " + nextTask);
            }
            if (nextTask != null) {
                nextTask.setTaskToReturnTo(tr.getTaskToReturnTo());
            } else {
                prevIsHome = true;
            }
        }
        this.mTaskHistory.remove(tr);
        this.mTaskHistory.add(0, tr);
        updateTaskMovement(tr, false);
        int numTasks = this.mTaskHistory.size();
        for (int taskNdx = numTasks - 1; taskNdx >= 1; taskNdx--) {
            task = (TaskRecord) this.mTaskHistory.get(taskNdx);
            if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                Slog.w(TAG, "taskNdx " + taskNdx + " task " + task);
            }
            if (task.isOverHomeStack()) {
                if (task.topRunningActivityLocked() != null) {
                    hasOverHome = true;
                    if (ActivityManagerDebugConfig.DEBUG_TASKS || ActivityManagerDebugConfig.DEBUG_AMS) {
                        Slog.w(TAG, "Find valid task " + task + " over home");
                    }
                    this.mWindowManager.prepareAppTransition(11, false);
                    this.mWindowManager.moveTaskToBottom(taskId);
                    task = null;
                    if (this.mResumedActivity == null) {
                        task = this.mResumedActivity.task;
                    } else if (next != null && next.task == topTask()) {
                        task = next.task;
                        if (ActivityManagerDebugConfig.DEBUG_TASKS || ActivityManagerDebugConfig.DEBUG_STACK) {
                            Slog.i(TAG, "moveTaskToBackLocked next : " + next + " next.task " + task + " tr.isOverHomeStack() " + tr.isOverHomeStack() + " " + Debug.getCallers(8));
                        }
                    }
                    if (ActivityManagerDebugConfig.DEBUG_TASKS || ActivityManagerDebugConfig.DEBUG_STACK) {
                        Slog.i(TAG, "moveTaskToBackLocked mResumedActivity : " + this.mResumedActivity + " task " + task + " tr.isOverHomeStack() " + tr.isOverHomeStack());
                    }
                    if (this.mResumedActivity == null) {
                        Slog.i(TAG, "moveTaskToBack:no resumed activity, check the current move back task instead. task:" + task);
                        if (moveTopTaskInFrontStackToBack) {
                            if (ActivityManagerDebugConfig.DEBUG_TASKS || ActivityManagerDebugConfig.DEBUG_STACK) {
                                Slog.i(TAG, "moveTaskToBack: move top task in the front stack to back, and the task has no resumed activity:" + tr);
                            }
                            task = tr;
                        }
                    }
                    if (prevIsHome && ((task != tr || !canGoHome) && (numTasks > 1 || !isOnHomeDisplay()))) {
                        if (!(hasOverHome || task == tr || !tr.isOverHomeStack() || task == null)) {
                            Slog.i(TAG, "moveTaskToBack: the current task activity:" + tr + "is not equal the mResumedActivity activity:" + task);
                            tr.setTaskToReturnTo(0);
                            task.setTaskToReturnTo(1);
                        }
                        adjustFocusedActivityLocked(this.mService.mFocusedActivity, "moveTaskToBack");
                        if (!(this.mFullscreen || isDockedStack() || this.mStackSupervisor.mFocusedStack == null)) {
                            ActivityRecord topRecord = this.mStackSupervisor.mFocusedStack.topRunningActivityLocked();
                            if (!(topRecord == null || topRecord.task == null || topRecord.task.canGoInDockedStack())) {
                                this.mStackSupervisor.moveTasksToFullscreenStackLocked(3, false);
                                if (this.mService.mConfiguration != null && this.mService.mConfiguration.orientation == 1) {
                                    this.mWindowManager.setFreeingChange(true);
                                    this.mWindowManager.startFreezingScreen(0, 0);
                                }
                                return true;
                            }
                        }
                        if (!this.mStackSupervisor.resumeFocusedStackTopActivityLocked()) {
                            this.mWindowManager.executeAppTransition();
                        }
                        return true;
                    } else if (this.mService.mBooting && !this.mService.mBooted) {
                        return false;
                    } else {
                        taskToReturnTo = tr.getTaskToReturnTo();
                        tr.setTaskToReturnTo(0);
                        if (!this.mFullscreen) {
                            ActivityStack stack = this.mStackSupervisor.getStack(0, false, false);
                            if (stack != null) {
                                ActivityRecord top = stack.topRunningActivityLocked();
                                if (top == null || !top.visible) {
                                    if (this.mService.mFocusedActivity != null) {
                                        this.mService.mFocusedActivity.mSplitBackState = top.SPLIT_BACEING;
                                    }
                                    this.mWindowManager.setSplitFromBack(true);
                                    if (isDockedStack()) {
                                        this.mStackSupervisor.moveTasksToFullscreenStackLocked(3, false);
                                    } else {
                                        this.mStackSupervisor.moveTasksToFullscreenStackLocked(3, true);
                                    }
                                    this.mWindowManager.setFreeingChange(true);
                                    this.mWindowManager.startFreezingScreen(0, 0);
                                    return true;
                                }
                                if (this.mService.mFocusedActivity != null) {
                                    this.mService.mFocusedActivity.mSplitBackState = top.SPLIT_BACEING;
                                }
                                this.mWindowManager.setSplitFromBack(true);
                                this.mStackSupervisor.moveTasksToFullscreenStackLocked(3, true);
                                if (this.mService.mConfiguration != null && this.mService.mConfiguration.orientation == 1) {
                                    this.mWindowManager.setFreeingChange(true);
                                    this.mWindowManager.startFreezingScreen(0, 0);
                                }
                                return this.mStackSupervisor.resumeHomeStackTask(taskToReturnTo, null, "moveTaskToBackAndShowHome");
                            }
                        }
                        return this.mStackSupervisor.resumeHomeStackTask(taskToReturnTo, null, "moveTaskToBackAndShowHome");
                    }
                }
                Slog.w(TAG, "invalid task " + task);
            }
            if (taskNdx == 1) {
                task.setTaskToReturnTo(1);
            }
        }
        this.mWindowManager.prepareAppTransition(11, false);
        this.mWindowManager.moveTaskToBottom(taskId);
        task = null;
        if (this.mResumedActivity == null) {
        }
        Slog.i(TAG, "moveTaskToBackLocked mResumedActivity : " + this.mResumedActivity + " task " + task + " tr.isOverHomeStack() " + tr.isOverHomeStack());
        if (this.mResumedActivity == null) {
        }
        if (prevIsHome) {
        }
        if (this.mService.mBooting) {
        }
        taskToReturnTo = tr.getTaskToReturnTo();
        tr.setTaskToReturnTo(0);
        if (this.mFullscreen) {
        }
        return this.mStackSupervisor.resumeHomeStackTask(taskToReturnTo, null, "moveTaskToBackAndShowHome");
    }

    static final void logStartActivity(int tag, ActivityRecord r, TaskRecord task) {
        Uri data = r.intent.getData();
        String strData = data != null ? data.toSafeString() : null;
        Object[] objArr = new Object[8];
        objArr[0] = Integer.valueOf(r.userId);
        objArr[1] = Integer.valueOf(System.identityHashCode(r));
        objArr[2] = Integer.valueOf(task.taskId);
        objArr[3] = r.shortComponentName;
        objArr[4] = r.intent.getAction();
        objArr[5] = r.intent.getType();
        objArr[6] = strData;
        objArr[7] = Integer.valueOf(r.intent.getFlags());
        EventLog.writeEvent(tag, objArr);
    }

    void ensureVisibleActivitiesConfigurationLocked(ActivityRecord start, boolean preserveWindow) {
        if (start != null && start.visible) {
            boolean behindFullscreen = false;
            boolean updatedConfig = false;
            for (int taskIndex = this.mTaskHistory.indexOf(start.task); taskIndex >= 0; taskIndex--) {
                TaskRecord task = (TaskRecord) this.mTaskHistory.get(taskIndex);
                ArrayList<ActivityRecord> activities = task.mActivities;
                int activityIndex = start.task == task ? activities.indexOf(start) : activities.size() - 1;
                while (activityIndex >= 0) {
                    ActivityRecord r = (ActivityRecord) activities.get(activityIndex);
                    if (r.visible || !r.stopped || r.state != ActivityState.STOPPED) {
                        updatedConfig |= ensureActivityConfigurationLocked(r, 0, preserveWindow);
                        if (ActivityManagerDebugConfig.DEBUG_CONFIGURATION) {
                            Slog.v(TAG_CONFIGURATION, "ensureVisibleActivitiesConfigurationLocked: updatedConfig=" + updatedConfig + " fullscreen=" + r.fullscreen + " visible=" + r.visible + " state=" + r.state + " stopped=" + r.stopped + " r=" + r + " task=" + task + " start=" + start + " preserveWindow=" + preserveWindow + " called by " + Debug.getCallers(4));
                        }
                        if (r.fullscreen) {
                            behindFullscreen = true;
                            break;
                        }
                    } else if (ActivityManagerDebugConfig.DEBUG_CONFIGURATION || !ActivityManagerService.IS_USER_BUILD) {
                        Slog.v(TAG_CONFIGURATION, "ensureVisibleActivitiesConfigurationLocked: skip r=" + r + " task=" + task + " start=" + start + " called by " + Debug.getCallers(4));
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

    boolean ensureActivityConfigurationLocked(ActivityRecord r, int globalChanges, boolean preserveWindow) {
        if (this.mConfigWillChange) {
            if (ActivityManagerDebugConfig.DEBUG_SWITCH || ActivityManagerDebugConfig.DEBUG_CONFIGURATION) {
                Slog.v(TAG_CONFIGURATION, "Skipping config check (will change): " + r);
            }
            return true;
        }
        if (ActivityManagerDebugConfig.DEBUG_SWITCH || ActivityManagerDebugConfig.DEBUG_CONFIGURATION) {
            Slog.v(TAG_CONFIGURATION, "Ensuring correct configuration: " + r);
        }
        Configuration newConfig = this.mService.mConfiguration;
        r.task.sanitizeOverrideConfiguration(newConfig);
        Configuration taskConfig = r.task.mOverrideConfig;
        if (r.configuration.equals(newConfig) && r.taskConfigOverride.equals(taskConfig) && !r.forceNewConfig) {
            if (ActivityManagerDebugConfig.DEBUG_SWITCH || ActivityManagerDebugConfig.DEBUG_CONFIGURATION) {
                Slog.v(TAG_CONFIGURATION, "Configuration unchanged in " + r);
            }
            return true;
        } else if (r.finishing) {
            if (ActivityManagerDebugConfig.DEBUG_SWITCH || ActivityManagerDebugConfig.DEBUG_CONFIGURATION) {
                Slog.v(TAG_CONFIGURATION, "Configuration doesn't matter in finishing " + r);
            }
            r.stopFreezingScreenLocked(false);
            return true;
        } else {
            Configuration oldConfig = r.configuration;
            Configuration oldTaskOverride = r.taskConfigOverride;
            r.configuration = newConfig;
            r.taskConfigOverride = taskConfig;
            if (ActivityManagerDebugConfig.DEBUG_AMS) {
                Slog.v(TAG_CONFIGURATION, "ensureActivityConfigurationLocked: r=" + r + ", oldConfig " + oldConfig + " newConfig " + newConfig + " call by " + Debug.getCallers(8));
            }
            int taskChanges = getTaskConfigurationChanges(r, taskConfig, oldTaskOverride);
            int changes = oldConfig.diff(newConfig) | taskChanges;
            if (changes != 0 || r.forceNewConfig) {
                if (ActivityManagerDebugConfig.DEBUG_SWITCH || ActivityManagerDebugConfig.DEBUG_CONFIGURATION) {
                    Slog.v(TAG_CONFIGURATION, "Configuration changes for " + r + " ; taskChanges=" + Configuration.configurationDiffToString(taskChanges) + ", allChanges=" + Configuration.configurationDiffToString(changes));
                }
                if (r.app == null || r.app.thread == null) {
                    if (ActivityManagerDebugConfig.DEBUG_SWITCH || ActivityManagerDebugConfig.DEBUG_CONFIGURATION) {
                        Slog.v(TAG_CONFIGURATION, "Configuration doesn't matter not running " + r);
                    }
                    r.stopFreezingScreenLocked(false);
                    r.forceNewConfig = false;
                    return true;
                }
                if (ActivityManagerDebugConfig.DEBUG_SWITCH || ActivityManagerDebugConfig.DEBUG_CONFIGURATION) {
                    Slog.v(TAG_CONFIGURATION, "Checking to restart " + r.info.name + ": changed=0x" + Integer.toHexString(changes) + ", handles=0x" + Integer.toHexString(r.info.getRealConfigChanged()) + ", newConfig=" + newConfig + ", taskConfig=" + taskConfig);
                }
                if (((~r.info.getRealConfigChanged()) & changes) != 0 || r.forceNewConfig) {
                    r.configChangeFlags |= changes;
                    r.startFreezingScreenLocked(r.app, globalChanges);
                    r.forceNewConfig = false;
                    preserveWindow &= isResizeOnlyChange(changes);
                    if (r.app == null || r.app.thread == null) {
                        if (ActivityManagerDebugConfig.DEBUG_SWITCH || ActivityManagerDebugConfig.DEBUG_CONFIGURATION) {
                            Slog.v(TAG_CONFIGURATION, "Config is destroying non-running " + r);
                        }
                        destroyActivityLocked(r, true, "config");
                    } else if (r.state == ActivityState.PAUSING) {
                        if (ActivityManagerDebugConfig.DEBUG_SWITCH || ActivityManagerDebugConfig.DEBUG_CONFIGURATION) {
                            Slog.v(TAG_CONFIGURATION, "Config is skipping already pausing " + r);
                        }
                        r.deferRelaunchUntilPaused = true;
                        r.preserveWindowOnDeferredRelaunch = preserveWindow;
                        return true;
                    } else if (r.state == ActivityState.RESUMED) {
                        if (ActivityManagerDebugConfig.DEBUG_SWITCH || ActivityManagerDebugConfig.DEBUG_CONFIGURATION) {
                            Slog.v(TAG_CONFIGURATION, "Config is relaunching resumed " + r);
                        }
                        if (ActivityManagerDebugConfig.DEBUG_STATES && !r.visible) {
                            Slog.v(TAG_STATES, "Config is relaunching resumed invisible activity " + r + " called by " + Debug.getCallers(4));
                        }
                        relaunchActivityLocked(r, r.configChangeFlags, true, preserveWindow);
                    } else {
                        if (ActivityManagerDebugConfig.DEBUG_SWITCH || ActivityManagerDebugConfig.DEBUG_CONFIGURATION) {
                            Slog.v(TAG_CONFIGURATION, "Config is relaunching non-resumed " + r);
                        }
                        relaunchActivityLocked(r, r.configChangeFlags, false, preserveWindow);
                    }
                    return false;
                }
                r.scheduleConfigurationChanged(taskConfig, true);
                r.stopFreezingScreenLocked(false);
                return true;
            }
            if (ActivityManagerDebugConfig.DEBUG_SWITCH || ActivityManagerDebugConfig.DEBUG_CONFIGURATION) {
                Slog.v(TAG_CONFIGURATION, "Configuration no differences in " + r);
            }
            r.scheduleConfigurationChanged(taskConfig, true);
            return true;
        }
    }

    private int getTaskConfigurationChanges(ActivityRecord record, Configuration taskConfig, Configuration oldTaskOverride) {
        if (Configuration.EMPTY.equals(oldTaskOverride) && !Configuration.EMPTY.equals(taskConfig)) {
            oldTaskOverride = record.task.extractOverrideConfig(record.configuration);
        }
        if (Configuration.EMPTY.equals(taskConfig) && !Configuration.EMPTY.equals(oldTaskOverride)) {
            taskConfig = record.task.extractOverrideConfig(record.configuration);
        }
        int taskChanges = oldTaskOverride.diff(taskConfig);
        if ((taskChanges & 1024) != 0) {
            boolean crosses;
            if (record.crossesHorizontalSizeThreshold(oldTaskOverride.screenWidthDp, taskConfig.screenWidthDp)) {
                crosses = true;
            } else {
                crosses = record.crossesVerticalSizeThreshold(oldTaskOverride.screenHeightDp, taskConfig.screenHeightDp);
            }
            if (!crosses) {
                taskChanges &= -1025;
            }
        }
        if (!((taskChanges & 2048) == 0 || record.crossesSmallestSizeThreshold(oldTaskOverride.smallestScreenWidthDp, taskConfig.smallestScreenWidthDp))) {
            taskChanges &= -2049;
        }
        return catchConfigChangesFromUnset(taskConfig, oldTaskOverride, taskChanges);
    }

    private static int catchConfigChangesFromUnset(Configuration taskConfig, Configuration oldTaskOverride, int taskChanges) {
        if (taskChanges != 0) {
            return taskChanges;
        }
        if (oldTaskOverride.orientation != taskConfig.orientation) {
            taskChanges |= 128;
        }
        int oldHeight = oldTaskOverride.screenHeightDp;
        int newHeight = taskConfig.screenHeightDp;
        if ((oldHeight == 0 && newHeight != 0) || (oldHeight != 0 && newHeight == 0)) {
            taskChanges |= 1024;
        }
        int oldWidth = oldTaskOverride.screenWidthDp;
        int newWidth = taskConfig.screenWidthDp;
        if ((oldWidth == 0 && newWidth != 0) || (oldWidth != 0 && newWidth == 0)) {
            taskChanges |= 1024;
        }
        int oldSmallest = oldTaskOverride.smallestScreenWidthDp;
        int newSmallest = taskConfig.smallestScreenWidthDp;
        if ((oldSmallest == 0 && newSmallest != 0) || (oldSmallest != 0 && newSmallest == 0)) {
            taskChanges |= 2048;
        }
        int oldLayout = oldTaskOverride.screenLayout;
        int newLayout = taskConfig.screenLayout;
        if ((oldLayout != 0 || newLayout == 0) && (oldLayout == 0 || newLayout != 0)) {
            return taskChanges;
        }
        return taskChanges | 256;
    }

    private static boolean isResizeOnlyChange(int change) {
        return (change & -3457) == 0;
    }

    private void relaunchActivityLocked(ActivityRecord r, int changes, boolean andResume, boolean preserveWindow) {
        if (this.mService.mSuppressResizeConfigChanges && preserveWindow) {
            r.configChangeFlags = 0;
            return;
        }
        int i;
        List results = null;
        List newIntents = null;
        if (andResume) {
            results = r.results;
            newIntents = r.newIntents;
        }
        if (ActivityManagerDebugConfig.DEBUG_SWITCH || !ActivityManagerService.IS_USER_BUILD) {
            Slog.v(TAG_SWITCH, "ACT-Relaunching: " + r + " with results=" + results + " newIntents=" + newIntents + " andResume=" + andResume + " preserveWindow=" + preserveWindow);
        }
        if (andResume) {
            i = EventLogTags.AM_RELAUNCH_RESUME_ACTIVITY;
        } else {
            i = EventLogTags.AM_RELAUNCH_ACTIVITY;
        }
        Object[] objArr = new Object[4];
        objArr[0] = Integer.valueOf(r.userId);
        objArr[1] = Integer.valueOf(System.identityHashCode(r));
        objArr[2] = Integer.valueOf(r.task.taskId);
        objArr[3] = r.shortComponentName;
        EventLog.writeEvent(i, objArr);
        r.startFreezingScreenLocked(r.app, 0);
        this.mStackSupervisor.removeChildActivityContainers(r);
        try {
            if (ActivityManagerDebugConfig.DEBUG_SWITCH || ActivityManagerDebugConfig.DEBUG_STATES) {
                Slog.i(TAG_SWITCH, "Moving to " + (andResume ? "RESUMED" : "PAUSED") + " Relaunching " + r + " callers=" + Debug.getCallers(6));
            }
            r.forceNewConfig = false;
            this.mStackSupervisor.activityRelaunchingLocked(r);
            r.app.thread.scheduleRelaunchActivity(r.appToken, results, newIntents, changes, !andResume, new Configuration(this.mService.mConfiguration), new Configuration(r.task.mOverrideConfig), preserveWindow);
        } catch (RemoteException e) {
            if (ActivityManagerDebugConfig.DEBUG_SWITCH || ActivityManagerDebugConfig.DEBUG_STATES) {
                Slog.i(TAG_SWITCH, "Relaunch failed", e);
            }
        }
        ProcessRecord appProc;
        Object[] objArr2;
        if (andResume) {
            if (ActivityManagerDebugConfig.DEBUG_STATES) {
                Slog.d(TAG_STATES, "Resumed after relaunch " + r);
            }
            r.state = ActivityState.RESUMED;
            appProc = r.app;
            if (appProc != null) {
                AfterActivityResumed eventData = AfterActivityResumed.createInstance();
                objArr2 = new Object[6];
                objArr2[0] = Integer.valueOf(appProc.pid);
                objArr2[1] = r.info.name;
                objArr2[2] = r.info.packageName;
                objArr2[3] = Integer.valueOf(r.mActivityType);
                objArr2[4] = Integer.valueOf(appProc.uid);
                objArr2[5] = Integer.valueOf(r.launchedFromUid);
                eventData.set(objArr2);
                this.mService.getAMEventHook().hook(Event.AM_AfterActivityResumed, eventData);
            }
            if (!r.visible || r.stopped) {
                this.mWindowManager.setAppVisibility(r.appToken, true);
                completeResumeLocked(r);
            } else {
                r.results = null;
                r.newIntents = null;
            }
            this.mService.showUnsupportedZoomDialogIfNeededLocked(r);
            this.mService.showAskCompatModeDialogLocked(r);
        } else {
            this.mHandler.removeMessages(101, r);
            r.state = ActivityState.PAUSED;
            appProc = r.app;
            if (appProc != null) {
                AfterActivityPaused eventData2 = AfterActivityPaused.createInstance();
                objArr2 = new Object[3];
                objArr2[0] = Integer.valueOf(appProc.pid);
                objArr2[1] = r.info.name;
                objArr2[2] = r.info.packageName;
                eventData2.set(objArr2);
                this.mService.getAMEventHook().hook(Event.AM_AfterActivityPaused, eventData2);
            }
        }
        r.configChangeFlags = 0;
        r.deferRelaunchUntilPaused = false;
        r.preserveWindowOnDeferredRelaunch = false;
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
                if (r.fullscreen && !r.finishing) {
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
        return !r.finishing;
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

    /* JADX WARNING: Removed duplicated region for block: B:35:0x0079  */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x0073  */
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
                    if ((userId == -1 || r.userId == userId) && ((sameComponent || r.task == lastTask) && (r.app == null || evenPersistent || !r.app.persistent))) {
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
                            lastTask = r.task;
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
                if (allowed || task.isHomeTask() || task.effectiveUid == callingUid) {
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
                            if (ActivityManagerDebugConfig.DEBUG_ALL) {
                                Slog.v(TAG, tmp.intent.getComponent().flattenToShortString() + ": task=" + tmp.task);
                            }
                        }
                    }
                    RunningTaskInfo ci = new RunningTaskInfo();
                    ci.id = task.taskId;
                    ci.stackId = this.mStackId;
                    ci.baseActivity = r.intent.getComponent();
                    ci.topActivity = top.intent.getComponent();
                    ci.isTopDockable = top.canGoInDockedStack();
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
                    if (top.task != null) {
                        ci.description = top.task.lastDescription;
                    }
                    ci.numActivities = numActivities;
                    ci.numRunning = numRunning;
                    ci.isDockable = task.canGoInDockedStack();
                    ci.resizeMode = task.mResizeMode;
                    if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                        Slog.v(TAG, "ci.topActivity " + ci.topActivity + " lastActiveTime " + ci.lastActiveTime);
                    }
                    list.add(ci);
                }
            }
        }
    }

    public void unhandledBackLocked() {
        int top = this.mTaskHistory.size() - 1;
        if (ActivityManagerDebugConfig.DEBUG_SWITCH) {
            Slog.d(TAG_SWITCH, "Performing unhandledBack(): top activity at " + top);
        }
        if (top >= 0) {
            ArrayList<ActivityRecord> activities = ((TaskRecord) this.mTaskHistory.get(top)).mActivities;
            int activityTop = activities.size() - 1;
            if (activityTop > 0) {
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
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            TaskRecord task = (TaskRecord) this.mTaskHistory.get(taskNdx);
            printed |= ActivityStackSupervisor.dumpHistoryList(fd, pw, ((TaskRecord) this.mTaskHistory.get(taskNdx)).mActivities, "    ", "Hist", true, !dumpAll, dumpClient, dumpPackage, needSep, header, "    Task id #" + task.taskId + "\n" + "    mFullscreen=" + task.mFullscreen + "\n" + "    mBounds=" + task.mBounds + "\n" + "    mMinWidth=" + task.mMinWidth + "\n" + "    mMinHeight=" + task.mMinHeight + "\n" + "    mLastNonFullscreenBounds=" + task.mLastNonFullscreenBounds);
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
        boolean isVoiceSession = true;
        if (mode == 0) {
            this.mStackSupervisor.removeLockedTaskLocked(task);
            this.mWindowManager.removeTask(task.taskId);
            if (!StackId.persistTaskBounds(this.mStackId)) {
                task.updateOverrideConfiguration(null);
            }
        }
        ActivityRecord r = this.mResumedActivity;
        if (r != null && r.task == task) {
            this.mResumedActivity = null;
        }
        int taskNdx = this.mTaskHistory.indexOf(task);
        int topTaskNdx = this.mTaskHistory.size() - 1;
        if (task.isOverHomeStack() && taskNdx < topTaskNdx) {
            TaskRecord nextTask = (TaskRecord) this.mTaskHistory.get(taskNdx + 1);
            if (!nextTask.isOverHomeStack()) {
                nextTask.setTaskToReturnTo(1);
            }
        }
        if (ActivityManagerDebugConfig.DEBUG_STACK) {
            Slog.i(TAG_STACK, "removeTask: " + task + " " + Debug.getCallers(8));
        }
        this.mTaskHistory.remove(task);
        updateTaskMovement(task, true);
        if (mode == 0 && task.mActivities.isEmpty()) {
            if (task.voiceSession == null) {
                isVoiceSession = false;
            }
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
        }
        if (this.mTaskHistory.isEmpty()) {
            if (ActivityManagerDebugConfig.DEBUG_STACK) {
                Slog.i(TAG_STACK, "removeTask: removing stack=" + this);
            }
            if (isOnHomeDisplay() && mode != 2 && this.mStackSupervisor.isFocusedStack(this)) {
                String myReason = reason + " leftTaskHistoryEmpty";
                if (this.mFullscreen || !adjustFocusToNextFocusableStackLocked(task.getTaskToReturnTo(), myReason)) {
                    this.mStackSupervisor.moveHomeStackToFront(myReason);
                }
            }
            if (this.mStacks != null) {
                this.mStacks.remove(this);
                this.mStacks.add(0, this);
            }
            if (!isHomeStack()) {
                this.mActivityContainer.onTaskListEmptyLocked();
            }
        }
        task.stack = null;
    }

    TaskRecord createTaskRecord(int taskId, ActivityInfo info, Intent intent, IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor, boolean toTop) {
        TaskRecord task = new TaskRecord(this.mService, taskId, info, intent, voiceSession, voiceInteractor);
        addTask(task, toTop, "createTaskRecord");
        boolean isLockscreenShown = this.mService.mLockScreenShown == 2;
        boolean showForAllUsers = (info.flags & 1024) != 0;
        if (!layoutTaskInStack(task, info.windowLayout) && this.mBounds != null && task.isResizeable() && (!isLockscreenShown || (isLockscreenShown && !showForAllUsers))) {
            task.updateOverrideConfiguration(this.mBounds);
        } else if (ActivityManagerDebugConfig.DEBUG_TASKS || !ActivityManagerService.IS_USER_BUILD) {
            Slog.d(TAG, "createTaskRecord: skip updateOverrideConfiguration mBounds=" + this.mBounds + " taskId=" + taskId + " " + task + " " + info + " isLockscreenShown=" + isLockscreenShown + " showForAllUsers=" + showForAllUsers + " callers=" + Debug.getCallers(4));
        }
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
        ActivityStack prevStack = preAddTask(task, reason, toTop);
        task.stack = this;
        if (toTop) {
            insertTaskAtTop(task, null);
        } else {
            this.mTaskHistory.add(0, task);
            updateTaskMovement(task, false);
        }
        postAddTask(task, prevStack);
    }

    void positionTask(TaskRecord task, int position) {
        ActivityRecord topRunningActivity = task.topRunningActivityLocked();
        boolean wasResumed = topRunningActivity == task.stack.mResumedActivity;
        ActivityStack prevStack = preAddTask(task, "positionTask", false);
        task.stack = this;
        insertTaskAtPosition(task, position);
        postAddTask(task, prevStack);
        if (wasResumed) {
            if (this.mResumedActivity != null) {
                Log.wtf(TAG, "mResumedActivity was already set when moving mResumedActivity from other stack to this stack mResumedActivity=" + this.mResumedActivity + " other mResumedActivity=" + topRunningActivity);
            }
            this.mResumedActivity = topRunningActivity;
        }
    }

    private ActivityStack preAddTask(TaskRecord task, String reason, boolean toTop) {
        ActivityStack prevStack = task.stack;
        if (!(prevStack == null || prevStack == this)) {
            prevStack.removeTask(task, reason, toTop ? 2 : 1);
        }
        return prevStack;
    }

    private void postAddTask(TaskRecord task, ActivityStack prevStack) {
        if (prevStack != null) {
            this.mStackSupervisor.scheduleReportPictureInPictureModeChangedIfNeeded(task, prevStack);
        } else if (task.voiceSession != null) {
            try {
                task.voiceSession.taskStarted(task.intent, task.taskId);
            } catch (RemoteException e) {
            }
        }
    }

    void addConfigOverride(ActivityRecord r, TaskRecord task) {
        this.mWindowManager.addAppToken(task.mActivities.indexOf(r), r.appToken, r.task.taskId, this.mStackId, r.info.screenOrientation, r.fullscreen, (r.info.flags & 1024) != 0, r.userId, r.info.configChanges, task.voiceSession != null, r.mLaunchTaskBehind, task.updateOverrideConfigurationFromLaunchBounds(), task.mOverrideConfig, task.mResizeMode, r.isAlwaysFocusable(), task.isHomeTask(), r.appInfo.targetSdkVersion, r.mRotationAnimationHint);
        r.taskConfigOverride = task.mOverrideConfig;
    }

    void moveToFrontAndResumeStateIfNeeded(ActivityRecord r, boolean moveToFront, boolean setResume, String reason) {
        if (moveToFront) {
            if (setResume) {
                this.mResumedActivity = r;
            }
            moveToFront(reason);
        }
    }

    void moveActivityToStack(ActivityRecord r) {
        boolean wasFocused = false;
        ActivityStack prevStack = r.task.stack;
        if (prevStack.mStackId != this.mStackId) {
            if (this.mStackSupervisor.isFocusedStack(prevStack) && this.mStackSupervisor.topRunningActivityLocked() == r) {
                wasFocused = true;
            }
            boolean wasResumed = wasFocused && prevStack.mResumedActivity == r;
            TaskRecord task = createTaskRecord(this.mStackSupervisor.getNextTaskIdForUserLocked(r.userId), r.info, r.intent, null, null, true);
            r.setTask(task, null);
            task.addActivityToTop(r);
            setAppTask(r, task);
            this.mStackSupervisor.scheduleReportPictureInPictureModeChangedIfNeeded(task, prevStack);
            moveToFrontAndResumeStateIfNeeded(r, wasFocused, wasResumed, "moveActivityToStack");
            if (wasResumed) {
                prevStack.mResumedActivity = null;
            }
        }
    }

    private void setAppTask(ActivityRecord r, TaskRecord task) {
        this.mWindowManager.setAppTask(r.appToken, task.taskId, this.mStackId, task.updateOverrideConfigurationFromLaunchBounds(), task.mOverrideConfig, task.mResizeMode, task.isHomeTask());
        r.taskConfigOverride = task.mOverrideConfig;
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

    public ComponentName getTopAppName() {
        return this.mComponentName;
    }

    private boolean skipStartActivityIfNeeded() {
        if (AMEventHookResult.hasAction(this.mService.getAMEventHook().hook(Event.AM_SkipStartActivity, SkipStartActivity.createInstance()), AMEventHookAction.AM_SkipStartActivity)) {
            return true;
        }
        return false;
    }

    void keepStickyTaskLocked() {
        ActivityStack stack = this.mStackSupervisor.findStack(2);
        if (MultiWindowManager.DEBUG) {
            Slog.d(TAG_STACK, "keepStickyTaskLocked, stack = " + stack);
        }
        if (stack != null && this == stack) {
            ArrayList<TaskRecord> tasks = stack.getAllTasks();
            for (int i = 0; i < tasks.size(); i++) {
                TaskRecord task = (TaskRecord) tasks.get(i);
                if (MultiWindowManager.DEBUG) {
                    Slog.d(TAG_STACK, "keepStickyTaskLocked, task = " + task);
                }
                if (task != null && task.mSticky) {
                    insertTaskAtTop(task, null);
                    this.mWindowManager.moveTaskToTop(task.taskId);
                }
            }
        }
    }

    void restoreStickyTaskLocked(TaskRecord topTask) {
        ActivityStack stack = this.mStackSupervisor.findStack(2);
        if (MultiWindowManager.DEBUG) {
            Slog.d(TAG_STACK, "restoreStickyTaskLocked, topTask = " + topTask + ", stack = " + stack);
        }
        if (topTask != null && stack != null && topTask.stack != stack) {
            ArrayList<TaskRecord> tasks = stack.getAllTasks();
            for (int i = tasks.size() - 1; i >= 0; i--) {
                TaskRecord task = (TaskRecord) tasks.get(i);
                if (MultiWindowManager.DEBUG) {
                    Slog.d(TAG_STACK, "restoreStickyTaskLocked, task = " + task);
                }
                if (task != null && task.mSticky) {
                    this.mService.stickWindow(task, false);
                }
            }
        }
    }

    static int getMaxStoppingToForce() {
        return 3;
    }

    private void startSecurityPayService(ActivityRecord prev, ActivityRecord next) {
        String prevPkg = IElsaManager.EMPTY_PACKAGE;
        String nextPkg = IElsaManager.EMPTY_PACKAGE;
        boolean isWechatPay = false;
        boolean isExitWechatPay = false;
        if (next != null) {
            if (!(prev == null || prev.packageName == null)) {
                prevPkg = prev.packageName;
            }
            if (!(next == null || next.packageName == null)) {
                nextPkg = next.packageName;
            }
            if (!prevPkg.equals(nextPkg) && next.packageName != null && !"com.tencent.mm".equals(next.packageName)) {
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

    protected ArrayList<TaskRecord> getTaskHistory() {
        return this.mTaskHistory;
    }

    public ComponentName getDockTopAppName() {
        return this.mDockComponentName;
    }
}
