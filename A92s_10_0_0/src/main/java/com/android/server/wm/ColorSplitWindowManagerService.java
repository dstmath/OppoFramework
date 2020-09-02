package com.android.server.wm;

import android.app.ActivityManager;
import android.app.AppGlobals;
import android.app.OppoActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.OppoBaseActivityInfo;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Slog;
import android.view.IDockedStackListener;
import android.view.OppoScreenDragUtil;
import android.view.WindowManagerGlobal;
import com.android.server.ColorStrictModeManager;
import com.android.server.am.ActivityManagerService;
import com.android.server.am.IColorActivityManagerServiceEx;
import com.color.app.ColorAppInfo;
import com.color.screenshot.ColorLongshotUtils;
import com.color.screenshot.ColorScreenshotManager;
import com.color.splitscreen.ColorSplitScreenManager;
import com.color.util.ColorTypeCastingHelper;
import java.util.ArrayList;
import java.util.List;

public class ColorSplitWindowManagerService implements IColorSplitWindowManager {
    private static final long DELAY_SPLIT_SCREEN_NOTIFY_TIMEOUT = 4000;
    private static final long DELAY_SPLIT_TO_LAUNCH_ACTIVITY = 600;
    private static final String IS_CHILDRENMODE_SUPPORT_STRING = "oppo.childspace.support";
    private static final int MSG_ACTIVITY_VISIBLE_TO_SPLIT = 2;
    private static final int MSG_SPLIT_TO_LAUNCH_ACTIVITY = 3;
    private static final int MSG_TIMEOUT_WAITING_ACTIVITY_VISIBLE_TO_SPLIT = 1;
    private static final String PKG_LAUNCHER = "com.oppo.launcher";
    public static final String TAG = "ColorSplitWindowManagerService";
    private static ColorSplitWindowManagerService mInstance = null;
    private static final Object mLock = new Object();
    private static boolean sChildrenModeSupport = false;
    private ActivityManager mActivityManager;
    private ActivityManagerService mAms = null;
    /* access modifiers changed from: private */
    public AppVisibilityObserver mAppVisibilityObserver = new AppVisibilityObserver() {
        /* class com.android.server.wm.ColorSplitWindowManagerService.AnonymousClass1 */

        public void onAppVisible(ActivityRecord r) {
            Slog.d(ColorSplitWindowManagerService.TAG, "onAppVisible for :" + r);
            ColorSplitWindowManagerService.this.unregisterActivityVisibilityListener();
            if (ColorSplitWindowManagerService.this.mSplitLaunchState == SplitLaunchState.WAITING_ACTIVITY_VISIBLE_TO_SPLIT) {
                ColorSplitWindowManagerService.this.setSplitLaunchState(SplitLaunchState.NONE);
                ColorSplitWindowManagerService.this.mHandler.removeMessages(2);
                ColorSplitWindowManagerService.this.mHandler.sendMessageDelayed(ColorSplitWindowManagerService.this.mHandler.obtainMessage(2), ColorSplitWindowManagerService.DELAY_SPLIT_TO_LAUNCH_ACTIVITY);
            }
        }
    };
    private ActivityTaskManagerService mAtms = null;
    private Context mContext;
    private DockDividerVisibilityListener mDockDividerVisibilityListener = new DockDividerVisibilityListener();
    /* access modifiers changed from: private */
    public Handler mHandler;
    private OppoActivityManager mOppoAms = new OppoActivityManager();
    /* access modifiers changed from: private */
    public PendingAppLaunchInfo mPendingAppLaunchInfo;
    /* access modifiers changed from: private */
    public SplitLaunchState mSplitLaunchState = SplitLaunchState.NONE;

    public enum SplitLaunchState {
        NONE,
        WAITING_ACTIVITY_VISIBLE_TO_SPLIT,
        WAITING_FOR_SPLIT_TO_LAUNCH_ACTIVITY
    }

    public static class PendingAppLaunchInfo {
        public Intent intent;
        public int userId;

        public PendingAppLaunchInfo(Intent intent2, int userId2) {
            this.intent = intent2;
            this.userId = userId2;
        }

        public String toString() {
            return "PendingAppLaunchInfo{intent=" + this.intent + ", userId=" + this.userId + '}';
        }
    }

    public static ColorSplitWindowManagerService getInstance() {
        if (mInstance == null) {
            synchronized (mLock) {
                if (mInstance == null) {
                    mInstance = new ColorSplitWindowManagerService();
                }
            }
        }
        return mInstance;
    }

    private ColorSplitWindowManagerService() {
    }

    public void init(IColorActivityManagerServiceEx amsEx) {
        if (amsEx != null) {
            this.mAms = amsEx.getActivityManagerService();
            this.mAtms = amsEx.getActivityManagerService().mActivityTaskManager;
            this.mContext = this.mAtms.mContext;
            try {
                WindowManagerGlobal.getWindowManagerService().registerDockedStackListener(this.mDockDividerVisibilityListener);
            } catch (Exception e) {
                Slog.e(TAG, "Failed to register docked stack listener", e);
            }
            this.mActivityManager = (ActivityManager) this.mContext.getSystemService("activity");
            this.mHandler = new Handler(new Handler.Callback() {
                /* class com.android.server.wm.ColorSplitWindowManagerService.AnonymousClass2 */

                public boolean handleMessage(Message message) {
                    try {
                        int i = message.what;
                        if (i == 1) {
                            synchronized (ColorSplitWindowManagerService.this.mAppVisibilityObserver) {
                                Slog.e(ColorSplitWindowManagerService.TAG, "unregister activity visibility listener if activity launch timeout");
                                ColorSplitWindowManagerService.this.unregisterActivityVisibilityListener();
                            }
                        } else if (i == 2) {
                            boolean unused = ColorSplitWindowManagerService.this.startSplitScreen();
                        } else if (i == 3) {
                            PendingAppLaunchInfo pendingAppLaunchInfo = (PendingAppLaunchInfo) message.obj;
                            ColorSplitWindowManagerService.this.startActivity(pendingAppLaunchInfo.intent, pendingAppLaunchInfo.userId);
                        }
                    } catch (Exception e) {
                        Slog.v(ColorSplitWindowManagerService.TAG, "handleMessage Error..");
                        e.printStackTrace();
                    }
                    return true;
                }
            });
        }
    }

    public void swapDockedFullscreenStack() {
        OppoBaseActivityStack baseSecondStack = null;
        try {
            ActivityStack primaryStack = this.mAtms.mRootActivityContainer.getDefaultDisplay().getStack(3, 1);
            ActivityStack secondStack = this.mAtms.mRootActivityContainer.getDefaultDisplay().getStack(4, 1);
            baseSecondStack = typeCasting(secondStack);
            ArrayList<TaskRecord> primaryTasks = primaryStack != null ? primaryStack.getAllTasks() : null;
            TaskRecord secondTask = secondStack != null ? secondStack.topTask() : null;
            if (primaryStack != null && secondStack != null) {
                if (baseSecondStack != null) {
                    this.mAms.mWindowManager.startFreezingScreen(0, 0);
                    baseSecondStack.mIsSwapTask = true;
                    secondTask.reparent(primaryStack, true, 1, true, true, "swapToPrimaryStack");
                    int size = primaryTasks.size();
                    for (int i = 0; i < size; i++) {
                        if (primaryTasks.get(i).taskId != secondTask.taskId) {
                            if (secondStack == null) {
                                secondStack = primaryStack.getDisplay().getOrCreateStack(4, 1, true);
                            }
                            primaryTasks.get(i).reparent(secondStack, true, 1, true, true, "swapDockedFullscreenStack - SECOND_STACK");
                        }
                    }
                    this.mAtms.mRootActivityContainer.ensureActivitiesVisible((ActivityRecord) null, 0, false);
                    this.mAtms.mRootActivityContainer.resumeFocusedStacksTopActivities();
                    ((ActivityManagerService) this.mAms).mWindowManager.stopFreezingScreen();
                    baseSecondStack.mIsSwapTask = false;
                    return;
                }
            }
        } finally {
            this.mAms.mWindowManager.stopFreezingScreen();
            if (baseSecondStack != null) {
                baseSecondStack.mIsSwapTask = false;
            }
        }
    }

    public boolean isSwapToPrimaryStack(String reason) {
        return !TextUtils.isEmpty(reason) && "swapToPrimaryStack".equals(reason);
    }

    public void adjustActivityResizeMode(ActivityInfo aInfo) {
        OppoBaseActivityInfo baseInfo = typeCasting(aInfo);
        if (aInfo != null && aInfo.applicationInfo != null && baseInfo != null) {
            String pName = aInfo.applicationInfo.packageName;
            if (baseInfo.hasResizeModeInit) {
                aInfo.resizeMode = baseInfo.resizeModeOriginal;
                if (ActivityTaskManagerDebugConfig.DEBUG_STACK) {
                    Slog.v(TAG, "split app: " + pName + " resizeMode: " + baseInfo.resizeModeOriginal);
                }
            } else {
                baseInfo.hasResizeModeInit = true;
                baseInfo.resizeModeOriginal = aInfo.resizeMode;
                if (ActivityTaskManagerDebugConfig.DEBUG_STACK) {
                    Slog.v(TAG, "split app2: " + pName + " resizeMode: " + baseInfo.resizeModeOriginal);
                }
            }
            if (isInBlackList(pName)) {
                if (ActivityTaskManagerDebugConfig.DEBUG_STACK) {
                    Slog.v(TAG, "split app: " + pName + " in blacklist");
                }
                aInfo.resizeMode = 0;
            } else if (!isInConfigList(pName)) {
            } else {
                if (aInfo.resizeMode != 2 && aInfo.resizeMode != 3 && aInfo.resizeMode != 1) {
                    if (ActivityTaskManagerDebugConfig.DEBUG_STACK) {
                        Slog.v(TAG, "split app: " + pName + " in configList");
                    }
                    aInfo.resizeMode = 4;
                } else if (ActivityTaskManagerDebugConfig.DEBUG_STACK) {
                    Slog.w(TAG, "split app: " + pName + "already resizable but in configList!");
                }
            }
        }
    }

    public boolean shouldHandleForPrimaryStack(ActivityStack currentStack, ActivityStack nextFocusableStack, String reason) {
        return false;
    }

    public boolean handleBackKeyForPrimaryStack(ActivityStack currentStack, ActivityStack nextFocusableStack, String reason) {
        return false;
    }

    public boolean moveTaskToBackForSplitWindowMode(ActivityStack currentStack) {
        return false;
    }

    public int getSplitScreenState(Intent intent) {
        if (!this.mAtms.mSupportsSplitScreenMultiWindow) {
            return ColorFreeformManagerService.FREEFORM_CALLER_UID;
        }
        if (isSnapshotEditing()) {
            Slog.d(TAG, "getSplitScreenState: snapshot editing page");
            return 1002;
        } else if (OppoScreenDragUtil.isDragState()) {
            Slog.d(TAG, "getSplitScreenState: single hand mode");
            return 1003;
        } else {
            String packageName = null;
            if (intent == null) {
                TaskRecord taskRecord = getRunningTask();
                if (taskRecord != null) {
                    packageName = taskRecord.realActivity.getPackageName();
                    if (isInBlackList(packageName)) {
                        Slog.d(TAG, "getSplitScreenState: black list");
                        return ColorStrictModeManager.WorkerHandler.MSG_SCREEN_ON;
                    } else if (isInForbidActivityListInner(taskRecord)) {
                        Slog.d(TAG, "getSplitScreenState: forbid special app");
                        return 1008;
                    } else if (taskRecord.supportsSplitScreenWindowingMode()) {
                        Slog.d(TAG, "getSplitScreenState: support from running TaskInfo");
                        return 1001;
                    }
                }
            } else {
                ActivityInfo activityInfo = resolveActivityInfo(intent, 0, this.mAtms.getCurrentUserId());
                if (activityInfo != null) {
                    packageName = activityInfo.applicationInfo.packageName;
                    if (isInBlackList(packageName)) {
                        Slog.d(TAG, "getSplitScreenState: black list");
                        return ColorStrictModeManager.WorkerHandler.MSG_SCREEN_ON;
                    } else if (ActivityInfo.isResizeableMode(activityInfo.resizeMode) && !ActivityInfo.isPreserveOrientationMode(activityInfo.resizeMode)) {
                        Slog.d(TAG, "getSplitScreenState: support from activity info");
                        return 1001;
                    }
                }
            }
            if (TextUtils.isEmpty(packageName)) {
                Slog.d(TAG, "getSplitScreenState: packageName = null");
                return ColorFreeformManagerService.FREEFORM_CALLER_UID;
            }
            Slog.d(TAG, "getSplitScreenState: packageName = " + packageName);
            if (!isInConfigList(packageName)) {
                return ColorStrictModeManager.WorkerHandler.STOP_STRICTMODE;
            }
            Slog.d(TAG, "getSplitScreenState: white list");
            return 1001;
        }
    }

    public boolean isInForbidActivityList(TaskRecord task) {
        if (!isInForbidActivityListInner(task)) {
            return false;
        }
        this.mAtms.getTaskChangeNotificationController().notifyActivityDismissingDockedStack();
        return true;
    }

    private boolean isInForbidActivityListInner(TaskRecord task) {
        if (task == null) {
            return false;
        }
        ActivityRecord topRunningActivity = task.getTopActivity();
        ApplicationInfo info = topRunningActivity != null ? topRunningActivity.appInfo : null;
        if (topRunningActivity != null && topRunningActivity.mActivityComponent != null && ColorSplitWindowAppReader.getInstance().isInForbidActivityList(topRunningActivity.mActivityComponent.getClassName())) {
            return true;
        }
        if (!isSystemApp(info) || supportsSplitScreenWindowingMode(task)) {
            return false;
        }
        return true;
    }

    private ActivityInfo resolveActivityInfo(Intent intent, int flags, int userId) {
        ComponentName comp = intent.getComponent();
        if (comp != null) {
            try {
                return AppGlobals.getPackageManager().getActivityInfo(comp, flags, userId);
            } catch (RemoteException e) {
                Slog.e(TAG, "resolveActivityInfo failed");
                e.printStackTrace();
                return null;
            }
        } else {
            ResolveInfo info = AppGlobals.getPackageManager().resolveIntent(intent, intent.resolveTypeIfNeeded(this.mContext.getContentResolver()), flags, userId);
            if (info != null) {
                return info.activityInfo;
            }
            return null;
        }
    }

    private TaskRecord getRunningTask() {
        TaskRecord task;
        List<ActivityManager.RunningTaskInfo> tasks = this.mAtms.getFilteredTasks(1, 3, 2);
        if (!tasks.isEmpty() && (task = this.mAtms.mRootActivityContainer.anyTaskForId(tasks.get(0).taskId)) != null) {
            return task;
        }
        return null;
    }

    private boolean isSnapshotEditing() {
        try {
            ColorScreenshotManager sm = ColorLongshotUtils.getScreenshotManager(this.mContext);
            if (sm == null || !sm.isScreenshotEdit()) {
                return false;
            }
            return true;
        } catch (NoSuchMethodError e) {
            Slog.e(TAG, "ColorScreenshotManager no method isScreenshotEdit!!");
            return false;
        }
    }

    private boolean isInConfigList(String packageName) {
        return ColorSplitWindowAppReader.getInstance().isInConfigList(packageName);
    }

    private boolean isInBlackList(String packageName) {
        return ColorSplitWindowAppReader.getInstance().isInBlackList(packageName);
    }

    private OppoBaseActivityInfo typeCasting(ActivityInfo info) {
        if (info != null) {
            return (OppoBaseActivityInfo) ColorTypeCastingHelper.typeCasting(OppoBaseActivityInfo.class, info);
        }
        return null;
    }

    private OppoBaseActivityStack typeCasting(ActivityStack stack) {
        if (stack != null) {
            return (OppoBaseActivityStack) ColorTypeCastingHelper.typeCasting(OppoBaseActivityStack.class, stack);
        }
        return null;
    }

    private boolean isSystemApp(ApplicationInfo info) {
        if (info == null || (info.flags & 1) == 0) {
            return false;
        }
        return true;
    }

    private boolean supportsSplitScreenWindowingMode(TaskRecord record) {
        boolean taskSupportSplitScreen = record.supportsSplitScreenWindowingMode();
        ActivityRecord topRunningActivity = record.getTopActivity();
        if (topRunningActivity != null) {
            return taskSupportSplitScreen && topRunningActivity.isResizeable();
        }
        return taskSupportSplitScreen;
    }

    public int splitScreenForEdgePanel(Intent intent, int userId) {
        Slog.e(TAG, "splitScreenForEdgePanel intent = " + intent + " userId = " + userId);
        if (!isReadyForSplitScreen()) {
            return -1;
        }
        resetSplitAssistentState();
        if (isHomeVisible()) {
            setSplitLaunchState(SplitLaunchState.WAITING_ACTIVITY_VISIBLE_TO_SPLIT);
            if (registerActivityVisibilityListener()) {
                startActivity(intent, userId);
                return 1;
            }
            resetSplitAssistentState();
            return -1;
        }
        setSplitLaunchState(SplitLaunchState.WAITING_FOR_SPLIT_TO_LAUNCH_ACTIVITY);
        if (startSplitScreen()) {
            savePengdingLaunchInfo(intent, userId);
            return 1;
        }
        resetSplitAssistentState();
        return -1;
    }

    public void resetSplitAssistentState() {
        setSplitLaunchState(SplitLaunchState.NONE);
        clearPengdingLaunchInfo();
    }

    public boolean registerActivityVisibilityListener() {
        synchronized (this.mAppVisibilityObserver) {
            if (this.mHandler == null) {
                Slog.e(TAG, "registerActivityVisibilityListener failed");
                return false;
            }
            ColorAppSwitchManagerService.getInstance().registerActivityVisibilityListener(this.mAppVisibilityObserver);
            this.mHandler.removeMessages(1);
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1), DELAY_SPLIT_SCREEN_NOTIFY_TIMEOUT);
            return true;
        }
    }

    public void unregisterActivityVisibilityListener() {
        synchronized (this.mAppVisibilityObserver) {
            ColorAppSwitchManagerService.getInstance().unregisterActivityVisibilityListener(this.mAppVisibilityObserver);
        }
    }

    private void savePengdingLaunchInfo(Intent intent, int userId) {
        synchronized (this.mAppVisibilityObserver) {
            this.mPendingAppLaunchInfo = new PendingAppLaunchInfo(intent, userId);
        }
    }

    private void clearPengdingLaunchInfo() {
        synchronized (this.mAppVisibilityObserver) {
            this.mPendingAppLaunchInfo = null;
        }
    }

    /* access modifiers changed from: private */
    public void startActivity(Intent intent, int userId) {
        this.mContext.startActivityAsUser(intent, UserHandle.of(userId));
    }

    public boolean isReadyForSplitScreen() {
        if (!isActivityPinned()) {
            return true;
        }
        Slog.d(TAG, "Activity Pinned");
        return false;
    }

    /* access modifiers changed from: private */
    public void setSplitLaunchState(SplitLaunchState state) {
        synchronized (this.mAppVisibilityObserver) {
            this.mSplitLaunchState = state;
        }
    }

    private boolean isHomeVisible() {
        boolean result = false;
        try {
            List<ColorAppInfo> topAppInfo = this.mOppoAms.getAllTopAppInfos();
            if (topAppInfo != null) {
                result = topAppInfo.stream().anyMatch($$Lambda$ColorSplitWindowManagerService$04NqSS2mUHNbEWem9Wf6OTiV2WY.INSTANCE);
            }
            Slog.d(TAG, "isHomeVisible: " + result);
            return result;
        } catch (Exception t) {
            t.printStackTrace();
            Slog.e(TAG, "getAllTopAppInfo error , t:" + t);
            return false;
        }
    }

    static /* synthetic */ boolean lambda$isHomeVisible$0(ColorAppInfo appInfo) {
        if (appInfo.windowingMode != 1 || appInfo.appInfo == null || !TextUtils.equals(appInfo.appInfo.packageName, "com.oppo.launcher")) {
            return false;
        }
        return true;
    }

    private boolean isActivityPinned() {
        ActivityManager activityManager = this.mActivityManager;
        if (activityManager == null || activityManager.getLockTaskModeState() != 2) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    public boolean startSplitScreen() {
        Slog.e(TAG, "startSplitScreen state = " + this.mSplitLaunchState);
        if (this.mAtms == null || this.mContext == null) {
            Slog.e(TAG, "startSplitScreen failed , mAtms = " + this.mAtms);
            return false;
        }
        try {
            return ColorSplitScreenManager.getInstance().splitScreenForTopApp(4);
        } catch (Exception e) {
            e.printStackTrace();
            Slog.e(TAG, "startSplitScreen failed");
            return false;
        }
    }

    class DockDividerVisibilityListener extends IDockedStackListener.Stub {
        DockDividerVisibilityListener() {
        }

        public void onDividerVisibilityChanged(boolean visible) throws RemoteException {
            Slog.d(ColorSplitWindowManagerService.TAG, "onDividerVisibilityChanged: " + visible);
        }

        public void onDockedStackExistsChanged(boolean exists) throws RemoteException {
            Slog.d(ColorSplitWindowManagerService.TAG, "onDockedStackExistsChanged: " + exists);
            if (!exists && ColorSplitWindowManagerService.this.mSplitLaunchState == SplitLaunchState.WAITING_FOR_SPLIT_TO_LAUNCH_ACTIVITY) {
                ColorSplitWindowManagerService.this.resetSplitAssistentState();
            }
        }

        public void onDockedStackMinimizedChanged(boolean minimized, long animDuration, boolean isHomeStackResizable) throws RemoteException {
            Slog.d(ColorSplitWindowManagerService.TAG, "onDockedStackMinimizedChanged: minimized = " + minimized + " mSplitLaunchState: " + ColorSplitWindowManagerService.this.mSplitLaunchState + " animDuration: " + animDuration + " isHomeStackResizable: " + isHomeStackResizable + " mPendingAppLaunchInfo =" + ColorSplitWindowManagerService.this.mPendingAppLaunchInfo);
            synchronized (ColorSplitWindowManagerService.this.mAppVisibilityObserver) {
                if (minimized) {
                    if (!(ColorSplitWindowManagerService.this.mPendingAppLaunchInfo == null || ColorSplitWindowManagerService.this.mPendingAppLaunchInfo.intent == null || ColorSplitWindowManagerService.this.mSplitLaunchState != SplitLaunchState.WAITING_FOR_SPLIT_TO_LAUNCH_ACTIVITY)) {
                        Slog.d(ColorSplitWindowManagerService.TAG, "onDockedStackExistsChanged: start ........");
                        if (ColorSplitWindowManagerService.this.mHandler != null) {
                            Message msg = ColorSplitWindowManagerService.this.mHandler.obtainMessage(3);
                            msg.obj = new PendingAppLaunchInfo(ColorSplitWindowManagerService.this.mPendingAppLaunchInfo.intent, ColorSplitWindowManagerService.this.mPendingAppLaunchInfo.userId);
                            ColorSplitWindowManagerService.this.mHandler.sendMessageDelayed(msg, ColorSplitWindowManagerService.DELAY_SPLIT_TO_LAUNCH_ACTIVITY);
                        } else {
                            Slog.e(ColorSplitWindowManagerService.TAG, "onDockedStackExistsChanged: mHandler is null");
                        }
                        ColorSplitWindowManagerService.this.resetSplitAssistentState();
                    }
                }
            }
        }

        public void onAdjustedForImeChanged(boolean adjustedForIme, long animDuration) throws RemoteException {
            Slog.d(ColorSplitWindowManagerService.TAG, "onAdjustedForImeChanged: adjustedForIme = " + adjustedForIme + " animDuration: " + animDuration);
        }

        public void onDockSideChanged(int newDockSide) throws RemoteException {
            Slog.d(ColorSplitWindowManagerService.TAG, "onDockSideChanged: newDockSide = " + newDockSide);
        }
    }
}
