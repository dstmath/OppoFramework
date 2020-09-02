package com.android.server.wm;

import android.app.ActivityOptions;
import android.app.ActivityTaskManager;
import android.common.OppoFeatureCache;
import android.content.ComponentName;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Region;
import android.os.Binder;
import android.os.Bundle;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.DisplayMetrics;
import android.util.IntArray;
import android.util.MergedConfiguration;
import android.util.Slog;
import android.util.SparseArray;
import android.view.DisplayInfo;
import android.view.InputWindowHandle;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.Interpolator;
import android.view.animation.ScaleAnimation;
import com.android.server.LocalServices;
import com.android.server.am.ActivityManagerService;
import com.android.server.am.IColorActivityManagerServiceEx;
import com.android.server.inputmethod.InputMethodManagerInternal;
import com.android.server.wm.ActivityStack;
import com.color.util.ColorTypeCastingHelper;
import com.color.zoomwindow.ColorZoomWindowInfo;
import com.color.zoomwindow.ColorZoomWindowManager;
import com.color.zoomwindow.ColorZoomWindowRUSConfig;
import com.color.zoomwindow.IColorZoomWindowObserver;
import java.util.ArrayList;
import java.util.List;

public class ColorZoomWindowManagerService implements IColorZoomWindowManager {
    private static boolean DEBUG_ANIMATION = true;
    private static boolean DEBUG_CORNER_DIUS = true;
    private static boolean DEBUG_INPUT = true;
    private static boolean DEBUG_LIFE_CYCLE = true;
    private static final float DIM_AMOUNT = 0.4f;
    private static final int ERROR_DEFAULT = -1;
    private static final int ERROR_INPUT_METHOD_MANAGER_INTERNAL_NOTNULL = -2;
    private static final int ERROR_MINIMIZEDDOCKED_STACK = -3;
    private static final int ERROR_TASKSTACK_ISNULL = -4;
    private static final int FLAG_FOR_ZOOM_WINDOW = 268435456;
    static final int LAND_LEFT_OFFSET_IN_DP = 40;
    static final int RESIZE_HANDLE_HEIGHT_IN_DP = 250;
    static final int RESIZE_HANDLE_WIDTH_IN_DP = 12;
    private static final String TAG = "ColorZoomWindowManagerService";
    static final float ZOOM_CORNER_RADIUS = 40.0f;
    private static volatile ColorZoomWindowManagerService sInstance;
    private ActivityManagerService mAms;
    private ActivityTaskManagerService mAtms;
    private Rect mBoundsLandSpaceDefault = new Rect();
    private Rect mBoundsLandSpaceScale = new Rect();
    private Rect mBoundsLandSpaceScaleNavLeft = new Rect();
    private Rect mBoundsPortraitDefault = new Rect();
    private Rect mBoundsPortraitInputmethod = new Rect();
    private Rect mBoundsPortraitScale = new Rect();
    private ColorZoomStarter mColorZoomStarter;
    protected WindowState mCurrentInputMethodWindow = null;
    private DisplayContent mDefaultDisplay = null;
    private Dimmer mDimmer = null;
    private final SparseArray<IntArray> mDisplayZoomModeUIDs = new SparseArray<>();
    private final RemoteCallbackList<IColorZoomWindowObserver> mIColorZoomWindowObservers = new RemoteCallbackList<>();
    private boolean mInputMethodChanged = false;
    protected int mInputMethodHeightDefault = -1;
    private boolean mInputMethodInBottom = false;
    protected boolean mInputMethodVisibility;
    protected int mLandLeftOffset;
    private float mLandRatio = 1.5f;
    protected int mLandTopOffset;
    public int mLastExitZoomMethod = -1;
    protected String mLockPkg;
    private final Object mLockUid = new Object();
    protected int mLockUserId;
    private final Object mObserverLock = new Object();
    protected int mPortLeftOffset;
    protected int mPortTopOffset;
    private float mRatio = 1.7777778f;
    protected int mRotation;
    private float mScaleForInputmethodDefault = 0.65f;
    private float mScaleForLandSpace;
    private float mScaleForLandSpaceDefault = 0.65f;
    private float mScaleForPortrait;
    private float mScaleForPortraitDefault = 0.8f;
    private int mScreenHeight;
    private int mScreenWidth;
    final ArrayList<WindowState> mTapExcludedWindows = new ArrayList<>();
    private final Rect mTmpRect = new Rect();
    private final Region mTmpRegion = new Region();
    private WindowManagerService mWms;

    private ColorZoomWindowManagerService() {
    }

    public static ColorZoomWindowManagerService getInstance() {
        if (sInstance == null) {
            synchronized (ColorZoomWindowManagerService.class) {
                if (sInstance == null) {
                    sInstance = new ColorZoomWindowManagerService();
                }
            }
        }
        return sInstance;
    }

    public void init(IColorActivityManagerServiceEx amsEx, IColorActivityTaskManagerServiceEx atmsEx) {
        Slog.i(TAG, "init ams");
        this.mAms = amsEx.getActivityManagerService();
        this.mAtms = atmsEx.getActivityTaskManagerService();
        ColorZoomWindowConfig.getInstance().init();
        ActivityTaskManagerService activityTaskManagerService = this.mAtms;
        if (activityTaskManagerService != null) {
            this.mColorZoomStarter = new ColorZoomStarter(activityTaskManagerService);
        }
    }

    public void init(IColorWindowManagerServiceEx wmsEx) {
        Slog.i(TAG, "init wms");
        this.mWms = wmsEx.getWindowManagerService();
        WindowManagerService windowManagerService = this.mWms;
        if (windowManagerService != null) {
            this.mDefaultDisplay = windowManagerService.getDefaultDisplayContentLocked();
            DisplayContent displayContent = this.mDefaultDisplay;
            if (displayContent != null) {
                this.mDimmer = new Dimmer((WindowContainer) displayContent.mChildren.get(1));
                DisplayInfo displayInfo = this.mDefaultDisplay.getDisplayInfo();
                if (displayInfo != null) {
                    int tmpWidth = displayInfo.logicalWidth;
                    int tmpHeight = displayInfo.logicalHeight;
                    this.mScreenWidth = Math.min(tmpWidth, tmpHeight);
                    this.mScreenHeight = Math.max(tmpWidth, tmpHeight);
                    Slog.i(TAG, "init screen size : " + this.mScreenWidth + " x " + this.mScreenHeight);
                }
            }
        }
    }

    private boolean initialized() {
        return (this.mAtms == null || this.mWms == null) ? false : true;
    }

    private void initScreen() {
        if (this.mBoundsPortraitScale.isEmpty()) {
            int tmpWidth = this.mAtms.mWindowManager.getDefaultDisplayContentLocked().getDisplayInfo().logicalHeight;
            int tmpHeight = this.mAtms.mWindowManager.getDefaultDisplayContentLocked().getDisplayInfo().logicalWidth;
            Slog.i(TAG, "initScreen start: tmpWidth = " + tmpWidth + ", tmpHeight" + tmpHeight);
            this.mScreenWidth = Math.min(tmpWidth, tmpHeight);
            this.mScreenHeight = Math.max(tmpWidth, tmpHeight);
            this.mScaleForPortrait = this.mScaleForPortraitDefault;
            this.mScaleForLandSpace = this.mScaleForLandSpaceDefault;
            Rect rect = this.mBoundsPortraitDefault;
            int i = this.mScreenWidth;
            rect.set(0, 0, i, (int) (((float) i) * this.mRatio));
            int i2 = this.mScreenWidth;
            float f = this.mScaleForPortrait;
            int portraitScaleWidth = (int) (((float) i2) * f);
            float f2 = this.mRatio;
            int portraitScaleHeight = (int) (((float) i2) * f2 * f);
            this.mInputMethodHeightDefault = (int) (((float) i2) * f2);
            this.mPortLeftOffset = (i2 - portraitScaleWidth) / 2;
            this.mPortTopOffset = (this.mScreenHeight - portraitScaleHeight) / 2;
            Rect rect2 = this.mBoundsPortraitScale;
            int i3 = this.mPortLeftOffset;
            int i4 = this.mPortTopOffset;
            rect2.set(i3, i4, i3 + portraitScaleWidth, i4 + portraitScaleHeight);
            Rect rect3 = this.mBoundsLandSpaceDefault;
            int i5 = this.mScreenWidth;
            rect3.set(0, 0, i5, (int) (((float) i5) * this.mLandRatio));
            int i6 = this.mScreenWidth;
            float f3 = this.mScaleForLandSpace;
            int landSpaceScaleHeight = (int) (((float) i6) * this.mLandRatio * f3);
            this.mLandLeftOffset = WindowManagerService.dipToPixel((int) LAND_LEFT_OFFSET_IN_DP, this.mAtms.mWindowManager.getDefaultDisplayContentLocked().getDisplayMetrics());
            this.mLandTopOffset = (this.mScreenWidth - landSpaceScaleHeight) / 2;
            Rect rect4 = this.mBoundsLandSpaceScale;
            int i7 = this.mLandLeftOffset;
            int i8 = this.mLandTopOffset;
            rect4.set(i7, i8, ((int) (((float) i6) * f3)) + i7, landSpaceScaleHeight + i8);
            Slog.i(TAG, "mBoundsPortraitScale = " + this.mBoundsPortraitScale + ", mBoundsLandSpaceDefault = " + this.mBoundsLandSpaceDefault + "mBoundsLandSpaceScale = " + this.mBoundsLandSpaceScale);
        }
    }

    public void displayChanged(DisplayContent dc) {
        if (dc == null) {
            Slog.e(TAG, "DisplayContent is null");
            return;
        }
        DisplayInfo displayInfo = dc.getDisplayInfo();
        if (displayInfo != null) {
            int tmpWidth = displayInfo.logicalWidth;
            int tmpHeight = displayInfo.logicalHeight;
            int currentWidth = Math.min(tmpWidth, tmpHeight);
            int currentHeight = Math.max(tmpWidth, tmpHeight);
            Slog.d(TAG, "current size = " + currentWidth + " x " + currentHeight);
            if (currentWidth != this.mScreenWidth || currentHeight != this.mScreenHeight) {
                Slog.i(TAG, "The display changed : old = " + this.mScreenWidth + " x " + this.mScreenHeight + ", new = " + currentWidth + " x " + currentHeight);
                this.mBoundsPortraitScale.setEmpty();
                initScreen();
            }
        }
    }

    public int startZoomWindow(Intent intent, Bundle bOptions, int userId, String callPkg) {
        if (this.mColorZoomStarter == null) {
            return -1;
        }
        if (bOptions.getInt("extra_window_mode") == ColorZoomWindowManager.WINDOWING_MODE_ZOOM_TO_FULLSCREEN) {
            this.mLastExitZoomMethod = 7;
        }
        return this.mColorZoomStarter.startZoomWindow(intent, bOptions, userId, callPkg);
    }

    public boolean registerZoomWindowObserver(IColorZoomWindowObserver observer) {
        if (!initialized() || observer == null) {
            return false;
        }
        return this.mIColorZoomWindowObservers.register(observer);
    }

    public boolean unregisterZoomWindowObserver(IColorZoomWindowObserver observer) {
        return this.mIColorZoomWindowObservers.unregister(observer);
    }

    public ColorZoomWindowInfo getCurrentZoomWindowState() {
        ColorZoomWindowInfo zoomWindowInfo;
        synchronized (this.mAtms.mGlobalLock) {
            long ident = Binder.clearCallingIdentity();
            try {
                zoomWindowInfo = getZoomWindowInfo();
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
        return zoomWindowInfo;
    }

    public void setBubbleMode(boolean inBubbleMode) {
        synchronized (this.mAtms.mGlobalLock) {
            ActivityStack zoomStack = getBubbleOrZoomStack();
            OppoBaseActivityStack baseZoomStack = typeCasting(zoomStack);
            if (baseZoomStack != null) {
                baseZoomStack.mZoomBubble = inBubbleMode;
            }
            if (!inBubbleMode) {
                this.mLockPkg = "";
                this.mLockUserId = 0;
            } else if (zoomStack != null) {
                setLockState(zoomStack);
            }
        }
        Slog.v(TAG, "setBubbleMode mZoomBubble: " + inBubbleMode + " ,mLockPkg : " + this.mLockPkg);
    }

    private String getRootPackage(ActivityStack zoomStack) {
        TaskRecord taskRecord = zoomStack.getChildAt(0);
        if (taskRecord == null) {
            return "";
        }
        ActivityRecord activityRecord = taskRecord.getChildAt(0);
        if (activityRecord != null) {
            return activityRecord.packageName;
        }
        Slog.v(TAG, " taskRecord: " + taskRecord + " ,activityRecord : " + activityRecord + " ,package : " + activityRecord.packageName);
        return "";
    }

    public void hideZoomWindow(int flag) {
        ActivityRecord r;
        synchronized (this.mAtms.mGlobalLock) {
            long ident = Binder.clearCallingIdentity();
            try {
                ActivityStack zoomStack = this.mAtms.mRootActivityContainer.getDefaultDisplay().getStack(ColorZoomWindowManager.WINDOWING_MODE_ZOOM, 1);
                OppoBaseActivityStack baseZoomStack = typeCasting(zoomStack);
                if (zoomStack != null) {
                    if (baseZoomStack != null) {
                        this.mLastExitZoomMethod = flag;
                        Slog.v(TAG, "hideZoomWindow : mLastExitZoomMethod = " + this.mLastExitZoomMethod);
                        if ((flag & 4) == 4) {
                            setLockState(zoomStack);
                            baseZoomStack.mZoomBubble = true;
                        } else {
                            baseZoomStack.mZoomBubble = false;
                        }
                        ActivityDisplay preferredDisplay = zoomStack.getDisplay();
                        ActivityStack nextStack = null;
                        int i = preferredDisplay.getChildCount() - 1;
                        while (true) {
                            if (i < 0) {
                                break;
                            }
                            ActivityStack stack = preferredDisplay.getChildAt(i);
                            if (stack != zoomStack) {
                                if (stack.isFocusableAndVisible()) {
                                    if (stack.getWindowingMode() == 1) {
                                        nextStack = stack;
                                        break;
                                    }
                                }
                            }
                            i--;
                        }
                        Slog.v(TAG, "hideZoomWindow nextStack: " + nextStack + " mZoomBubble: " + baseZoomStack.mZoomBubble);
                        if (!(nextStack == null || (r = nextStack.topRunningActivityLocked()) == null || !r.moveFocusableActivityToTop("hideZoomWindow"))) {
                            this.mAtms.mRootActivityContainer.resumeFocusedStacksTopActivities();
                        }
                        Binder.restoreCallingIdentity(ident);
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    public List<String> getZoomAppConfigList(int type) {
        return ColorZoomWindowConfig.getInstance().getConfigList(type);
    }

    public ColorZoomWindowRUSConfig getZoomWindowConfig() {
        return ColorZoomWindowConfig.getInstance().getZoomWindowConfig();
    }

    public void setZoomWindowConfig(ColorZoomWindowRUSConfig config) {
        ColorZoomWindowConfig.getInstance().setZoomWindowConfig(config);
    }

    public boolean isSupportZoomWindowMode() {
        return ColorZoomWindowConfig.getInstance().isZoomWindowEnabled();
    }

    private void setLockState(ActivityStack zoomStack) {
        this.mLockPkg = getRootPackage(zoomStack);
        ActivityRecord resume = zoomStack.getResumedActivity();
        if (resume != null) {
            this.mLockUserId = resume.mUserId;
            Slog.v(TAG, "mLockUserId: " + this.mLockUserId);
        }
    }

    public void notifyZoomWindowShow(ColorZoomWindowInfo info) {
        ColorZoomWindowDebugConfig.getInstance().enableDebugLifeCycle();
        try {
            int size = this.mIColorZoomWindowObservers.beginBroadcast();
            Slog.v(TAG, "notifyZoomWindowShow size: " + size);
            if (info != null) {
                Slog.v(TAG, "notifyZoomWindowShow info: " + info);
            }
            for (int i = 0; i < size; i++) {
                try {
                    this.mIColorZoomWindowObservers.getBroadcastItem(i).onZoomWindowShow(info);
                } catch (RemoteException e) {
                    Slog.e(TAG, "Error notifyZoomWindowShow changed event.", e);
                }
            }
            this.mIColorZoomWindowObservers.finishBroadcast();
        } catch (Exception e2) {
            Slog.e(TAG, "Exception notifyZoomWindowShow changed event.", e2);
        }
    }

    public void notifyZoomWindowHide(ColorZoomWindowInfo info, ActivityStack stack, boolean toFullScreen) {
        ColorZoomWindowDebugConfig.getInstance().disableDebugLifeCycle();
        if (stack != null) {
            try {
                if (!(stack.getTaskStack() == null || stack.getTaskStack().getSurfaceControl() == null)) {
                    stack.getTaskStack().getSurfaceControl().setCornerRadius(0.0f);
                }
            } catch (Exception e) {
                Slog.e(TAG, "Exception notifyZoomWindowHide changed event.", e);
                return;
            }
        }
        if (stack != null) {
            updateZoomUIDsOnDisplay(stack, false);
            ActivityRecord top = stack.getTopActivity();
            if (this.mAtms.isSleepingLocked() && top != null && stack.getWindowingMode() == ColorZoomWindowManager.WINDOWING_MODE_ZOOM && isUnReusedActivityInZoomMode(top.mActivityComponent)) {
                stack.finishActivityLocked(top, 0, (Intent) null, "zoom-sleep", false);
                Slog.i(TAG, "notifyZoomWindowHide: finish top = " + top + " when going to sleep");
            }
        }
        OppoFeatureCache.get(IColorAccessControlLocalManager.DEFAULT).notifyZoomWindowExit(stack, toFullScreen);
        int size = this.mIColorZoomWindowObservers.beginBroadcast();
        for (int i = 0; i < size; i++) {
            IColorZoomWindowObserver listener = this.mIColorZoomWindowObservers.getBroadcastItem(i);
            try {
                Slog.v(TAG, "notifyZoomWindowHide: ");
                listener.onZoomWindowHide(info);
            } catch (RemoteException e2) {
                Slog.e(TAG, "Error notifyZoomWindowHide changed event.", e2);
            }
        }
        this.mIColorZoomWindowObservers.finishBroadcast();
        this.mCurrentInputMethodWindow = null;
    }

    private void nodifyControlViewVisible(ColorZoomWindowInfo info, boolean visible) {
        int size = this.mIColorZoomWindowObservers.beginBroadcast();
        for (int i = 0; i < size; i++) {
            IColorZoomWindowObserver listener = this.mIColorZoomWindowObservers.getBroadcastItem(i);
            if (visible) {
                try {
                    listener.onZoomWindowShow(info);
                } catch (RemoteException e) {
                    Slog.e(TAG, "Error nodifyControlViewVisible changed event.", e);
                }
            } else {
                listener.onZoomWindowHide(info);
            }
        }
        this.mIColorZoomWindowObservers.finishBroadcast();
    }

    public void notifyZoomWindowDied(String appName, ActivityStack stack) {
        ColorZoomWindowDebugConfig.getInstance().disableDebugLifeCycle();
        if (stack != null) {
            try {
                if (!(stack.getTaskStack() == null || stack.getTaskStack().getSurfaceControl() == null)) {
                    stack.getTaskStack().getSurfaceControl().setCornerRadius(0.0f);
                }
            } catch (Exception e) {
                Slog.e(TAG, "Exception notifyZoomWindowDied changed event.", e);
                return;
            }
        }
        OppoFeatureCache.get(IColorAccessControlLocalManager.DEFAULT).notifyZoomWindowExit(stack, false);
        int size = this.mIColorZoomWindowObservers.beginBroadcast();
        Slog.v(TAG, "notifyZoomWindowDied appName: " + appName);
        for (int i = 0; i < size; i++) {
            try {
                this.mIColorZoomWindowObservers.getBroadcastItem(i).onZoomWindowDied(appName);
            } catch (RemoteException e2) {
                Slog.e(TAG, "Error notifyZoomWindowDied changed event.", e2);
            }
        }
        this.mIColorZoomWindowObservers.finishBroadcast();
        this.mCurrentInputMethodWindow = null;
    }

    public void notifyInputMethodChanged(boolean isShown, boolean inBottom) {
        if (this.mInputMethodChanged != isShown || this.mInputMethodInBottom != inBottom) {
            this.mInputMethodChanged = isShown;
            this.mInputMethodInBottom = inBottom;
            try {
                int size = this.mIColorZoomWindowObservers.beginBroadcast();
                for (int i = 0; i < size; i++) {
                    IColorZoomWindowObserver listener = this.mIColorZoomWindowObservers.getBroadcastItem(i);
                    try {
                        Slog.v(TAG, "notifyInputMethodChanged: ");
                        listener.onInputMethodChanged(this.mInputMethodChanged && this.mInputMethodInBottom);
                    } catch (RemoteException e) {
                        Slog.e(TAG, "Error notifyInputMethodChanged changed event.", e);
                    }
                }
                this.mIColorZoomWindowObservers.finishBroadcast();
            } catch (Exception e2) {
                Slog.e(TAG, "Exception notifyInputMethodChanged changed event.", e2);
            }
        }
    }

    public void updateZoomStack(ActivityStack stack, ActivityOptions options, ActivityRecord startActivity, ActivityRecord sourceRecord) {
        ActivityStack sourceStack;
        ActivityStack sourceStack2;
        InputMethodManagerInternal inputMethodManagerInternal;
        ActivityRecord top;
        if (options != null) {
            int windowMode = options.getLaunchWindowingMode();
            Slog.v(TAG, "windowMode: " + windowMode + "=stack=" + stack);
            OppoBaseActivityStack baseActivityStack = typeCasting(stack);
            if (stack != null && baseActivityStack != null) {
                if (windowMode == ColorZoomWindowManager.WINDOWING_MODE_ZOOM) {
                    if (!stack.isFocusedStackOnDisplay() || stack.getWindowingMode() != 1 || startActivity == null || (top = stack.topRunningActivityLocked()) == null || top.getTaskRecord() == null || startActivity.getTaskRecord() == null || startActivity.getTaskRecord() != top.getTaskRecord() || !isUnSupportZoomMode(top.mActivityComponent)) {
                        DisplayContent dc = ((ActivityDisplay) stack.getDisplay()).mDisplayContent;
                        WindowState inputMethodWindow = dc.mInputMethodWindow;
                        if (!(inputMethodWindow == null || !inputMethodWindow.isVisible() || (inputMethodManagerInternal = (InputMethodManagerInternal) LocalServices.getService(InputMethodManagerInternal.class)) == null)) {
                            inputMethodManagerInternal.hideCurrentInputMethod();
                            this.mInputMethodVisibility = false;
                            Slog.v(TAG, "updateZoomStack: hide inputMethod Window when start in zoom mode");
                        }
                        dc.prepareAppTransition(100, true, 0, true);
                        stack.setWindowingMode(ColorZoomWindowManager.WINDOWING_MODE_ZOOM, false, false, false, true, false);
                        stack.resize(getScaledTaskBound(), getTaskBound(), (Rect) null);
                        return;
                    }
                    Slog.v(TAG, "updateZoomStack: if the un Support ZoomMode top=" + top + "  and startActivity =" + startActivity + "  is same task, not start in zoom");
                } else if (windowMode == 1 && stack.getWindowingMode() == ColorZoomWindowManager.WINDOWING_MODE_ZOOM) {
                    baseActivityStack.mZoomBubble = false;
                    baseActivityStack.mStackVisibleChange = false;
                    baseActivityStack.mStackShown = false;
                    Slog.v(TAG, "mStackShown false: " + stack);
                    notifyZoomWindowHide(getZoomWindowInfo(), stack, true);
                    ActivityStack topFullStack = this.mAtms.mRootActivityContainer.getDefaultDisplay().getStack(1, 1);
                    Slog.v(TAG, "updateZoomStack: topFullStack=" + topFullStack);
                    boolean needScheduleIdle = false;
                    if (!(topFullStack == null || topFullStack.mResumedActivity == null || !topFullStack.mResumedActivity.checkEnterPictureInPictureState("makeInvisible", true))) {
                        needScheduleIdle = true;
                        Slog.v(TAG, "updateZoomStack: topFullStack.mResumedActivity=" + topFullStack.mResumedActivity);
                    }
                    stack.setWindowingMode(options.getLaunchWindowingMode());
                    if (needScheduleIdle) {
                        this.mAtms.mStackSupervisor.scheduleIdleLocked();
                    }
                }
            }
        } else if (startActivity != null) {
            if (sourceRecord != null) {
                sourceStack = sourceRecord.getActivityStack();
            } else {
                sourceStack = null;
            }
            ColorZoomWindowConfig.getInstance().getUnSupportCpnList();
            boolean unSupportZoom = isUnSupportZoomMode(startActivity.mActivityComponent);
            if (sourceStack != null && sourceStack.getWindowingMode() == ColorZoomWindowManager.WINDOWING_MODE_ZOOM && sourceStack != stack && startActivity.packageName.equals(sourceRecord.packageName) && !unSupportZoom) {
                stack.setWindowingMode(ColorZoomWindowManager.WINDOWING_MODE_ZOOM, false, false, false, true, false);
                stack.resize(getScaledTaskBound(), getTaskBound(), (Rect) null);
            } else if (unSupportZoom) {
                this.mLastExitZoomMethod = 6;
                stack.setWindowingMode(1, false, false, false, true, false);
                if (sourceStack == null || sourceStack.getWindowingMode() != ColorZoomWindowManager.WINDOWING_MODE_ZOOM || sourceStack == stack) {
                    sourceStack2 = sourceStack;
                } else {
                    sourceStack2 = sourceStack;
                    sourceStack.setWindowingMode(1, false, false, false, true, false);
                }
                notifyZoomWindowHide(getZoomWindowInfo(), sourceStack2, true);
            }
        }
    }

    private boolean isUnSupportZoomMode(ComponentName componentName) {
        List<String> unSupportCpn = ColorZoomWindowConfig.getInstance().getUnSupportCpnList();
        if (unSupportCpn == null || unSupportCpn.isEmpty() || componentName == null) {
            return false;
        }
        return unSupportCpn.contains(componentName.flattenToShortString());
    }

    private boolean isUnReusedActivityInZoomMode(ComponentName componentName) {
        List<String> unActivityCpn = ColorZoomWindowConfig.getInstance().getUnReusedActivityInZoomModeList();
        if (unActivityCpn == null || unActivityCpn.isEmpty() || componentName == null) {
            return false;
        }
        return unActivityCpn.contains(componentName.flattenToShortString());
    }

    public void pauseZoomWindowActivity() {
        ActivityStack zoomStack = getVisibleZoomStack();
        if (zoomStack != null) {
            ActivityStack focusStack = this.mAtms.mRootActivityContainer.getDefaultDisplay().getFocusedStack();
            ActivityRecord r = focusStack.topRunningActivityLocked();
            zoomStack.topRunningActivityLocked();
            ActivityRecord resume = zoomStack.mResumedActivity;
            if (focusStack != zoomStack && resume != null && r != null && r.getState() == ActivityStack.ActivityState.RESUMED) {
                zoomStack.startPausingLocked(false, true, (ActivityRecord) null, false);
            }
        }
    }

    private ActivityStack getVisibleZoomStack() {
        ActivityDisplay activityDisplay = this.mAtms.mRootActivityContainer.getDefaultDisplay();
        for (int stackNdx = activityDisplay.getChildCount() - 1; stackNdx >= 0; stackNdx--) {
            ActivityStack tmpStack = activityDisplay.getChildAt(stackNdx);
            if (tmpStack.getWindowingMode() == ColorZoomWindowManager.WINDOWING_MODE_ZOOM) {
                ArrayList<TaskRecord> allTasks = tmpStack.getAllTasks();
                for (int taskNdx = allTasks.size() - 1; taskNdx >= 0; taskNdx--) {
                    ArrayList<ActivityRecord> activities = allTasks.get(taskNdx).mActivities;
                    for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                        ActivityRecord r = activities.get(activityNdx);
                        if (r.visible || r.getState() == ActivityStack.ActivityState.RESUMED) {
                            return tmpStack;
                        }
                    }
                }
                continue;
            }
        }
        return null;
    }

    public ColorZoomWindowInfo getZoomWindowInfo() {
        ColorZoomWindowInfo info = new ColorZoomWindowInfo();
        info.zoomRect.set(getScaledTaskBound());
        int i = this.mRotation;
        info.rotation = i;
        info.zoomPkg = "";
        boolean z = false;
        info.zoomUserId = 0;
        info.lockPkg = this.mLockPkg;
        info.lockUserId = this.mLockUserId;
        info.lastExitMethod = this.mLastExitZoomMethod;
        if (this.mInputMethodVisibility && this.mScaleForPortrait == this.mScaleForPortraitDefault && i == 0) {
            Slog.i(TAG, "the current keyboard is a floating keyboard and the floating window is not raised, so set to false to notify the app to prevent the two buttons from being hidden on the app side");
            info.inputShow = this.mInputMethodChanged && this.mInputMethodInBottom;
        } else {
            info.inputShow = this.mInputMethodVisibility;
        }
        ActivityStack zoomStack = getBubbleOrZoomStack();
        OppoBaseActivityStack baseZoomStack = typeCasting(zoomStack);
        if (!(zoomStack == null || baseZoomStack == null)) {
            info.zoomPkg = baseZoomStack.mZoomPkg;
            if (baseZoomStack.mStackShown && inZoomWindowMode(zoomStack)) {
                z = true;
            }
            info.windowShown = z;
            info.zoomUserId = baseZoomStack.mZoomUserId;
            Slog.v(TAG, "getZoomWindowInfo bubbleLocked: " + baseZoomStack.mZoomPkg + " mRotation: " + this.mRotation + " userId: " + info.lockUserId + " zoomStack: " + zoomStack);
        }
        return info;
    }

    private ActivityStack getBubbleOrZoomStack() {
        ActivityDisplay activityDisplay = this.mAtms.mRootActivityContainer.getDefaultDisplay();
        for (int i = activityDisplay.getChildCount() - 1; i >= 0; i--) {
            ActivityStack stack = activityDisplay.getChildAt(i);
            OppoBaseActivityStack baseStack = typeCasting(stack);
            if ((baseStack != null && baseStack.mZoomBubble) || stack.isCompatible(ColorZoomWindowManager.WINDOWING_MODE_ZOOM, 1)) {
                return stack;
            }
        }
        return null;
    }

    public void shouldBeVisible(boolean stackShouldBeVisible, ActivityStack stack, ActivityRecord top) {
        OppoBaseActivityStack baseStack = typeCasting(stack);
        if (baseStack != null) {
            if (stackShouldBeVisible != baseStack.mStackShown) {
                baseStack.mStackVisibleChange = true;
            }
            baseStack.mStackShown = stackShouldBeVisible;
            if (stack.getWindowingMode() != ColorZoomWindowManager.WINDOWING_MODE_ZOOM) {
                return;
            }
            if (!stackShouldBeVisible) {
                baseStack.mStackVisibleChange = false;
                ActivityDisplay display2 = stack.getDisplay();
                ActivityStack topStack = display2.getChildAt(display2.getChildCount() - 1);
                Slog.v(TAG, "topStack: " + topStack);
                if (topStack != null && topStack != stack && topStack.getWindowingMode() != ColorZoomWindowManager.WINDOWING_MODE_ZOOM) {
                    baseStack.mStackShown = false;
                    baseStack.mWindowModeHide = true;
                    notifyZoomWindowHide(getZoomWindowInfo(), stack, false);
                    if (!(stack.getTaskStack() == null || stack.getTaskStack().isAnimating() || stack.getTaskStack().getDisplayContent().mAppTransition.getAppTransition() == 101)) {
                        this.mInputMethodVisibility = false;
                        this.mScaleForPortrait = this.mScaleForPortraitDefault;
                        Slog.i(TAG, "shouldBeVisible mScaleForPortrait = " + this.mScaleForPortrait);
                        stack.setWindowingMode(1, false, false, false, true, false);
                        Slog.i(TAG, "push zoomStack to back");
                        stack.moveToBack("push zoomStack to back", (TaskRecord) null);
                    }
                    Slog.v(TAG, "notifyZoomWindowHide: " + stack);
                    return;
                }
                return;
            }
            ActivityDisplay display3 = stack.getDisplay();
            ActivityStack topStack2 = display3.getChildAt(display3.getChildCount() - 1);
            if (topStack2 != null && topStack2 != stack && topStack2.getWindowingMode() == 1) {
                this.mLastExitZoomMethod = 8;
                nodifyControlViewVisible(getZoomWindowInfo(), false);
                Slog.v(TAG, "notifyZoomWindowHide: because topStack=" + topStack2 + " not zoom stack");
            } else if (topStack2 != null && topStack2 == stack) {
                nodifyControlViewVisible(getZoomWindowInfo(), true);
            }
        }
    }

    public String getRealActivityPkgName(ActivityRecord activityRecord) {
        TaskRecord taskRecord = activityRecord.getTaskRecord();
        if (taskRecord == null) {
            Slog.e(TAG, "taskRecord is null");
            return null;
        }
        ComponentName realActivity = taskRecord.realActivity;
        if (realActivity != null) {
            return realActivity.getPackageName();
        }
        Slog.e(TAG, "realActivity is null");
        return null;
    }

    public void notifyZoomActivityShown(ActivityRecord showActivity) {
        if (showActivity != null && showActivity.getWindowingMode() == ColorZoomWindowManager.WINDOWING_MODE_ZOOM) {
            ActivityStack showStack = showActivity.getActivityStack();
            OppoBaseActivityStack baseShowStack = typeCasting(showStack);
            Slog.v(TAG, "mStackVisibleChange showStack: " + showStack);
            if (baseShowStack != null && baseShowStack.mStackVisibleChange) {
                Slog.v(TAG, "notifyZoomWindowShow " + showActivity);
                baseShowStack.mStackVisibleChange = false;
                baseShowStack.mZoomPkg = getRealActivityPkgName(showActivity);
                baseShowStack.mZoomUserId = showActivity.mUserId;
                updateZoomUIDsOnDisplay(showStack, true);
                notifyZoomWindowShow(getZoomWindowInfo());
                Slog.v(TAG, "notifyZoomActivityShown " + showActivity);
            }
        }
    }

    public void notifyWindowDied(ActivityStack stack) {
        OppoBaseActivityStack baseStack = typeCasting(stack);
        if (baseStack != null && stack != null) {
            if (stack.getWindowingMode() == ColorZoomWindowManager.WINDOWING_MODE_ZOOM || baseStack.mZoomBubble) {
                updateZoomUIDsOnDisplay(stack, false);
                clearZoomWindow();
                Slog.v(TAG, "notifyZoomWindowDied " + stack);
                notifyZoomWindowDied(baseStack.mZoomPkg, stack);
            }
        }
    }

    public void clearZoomWindow() {
    }

    public boolean updateZoonWindowTaskBound(Configuration config, ActivityStack stack) {
        if (stack.getWindowingMode() != ColorZoomWindowManager.WINDOWING_MODE_ZOOM) {
            return false;
        }
        stack.resize(getScaledTaskBound(), getTaskBound(), (Rect) null);
        return true;
    }

    public Rect getTaskBound() {
        initScreen();
        this.mRotation = this.mAtms.mWindowManager.getDefaultDisplayRotation();
        int i = this.mRotation;
        if (i == 1 || i == 3) {
            return new Rect(this.mBoundsLandSpaceDefault);
        }
        return new Rect(this.mBoundsPortraitDefault);
    }

    public void updateInputWindowByZoom(boolean inputMethodVisibility) {
        int stackCount;
        WindowState inputMethodWindow;
        synchronized (this.mAtms.mGlobalLock) {
            boolean inBottom = true;
            WindowState inputMethodWindow2 = this.mWms.getDefaultDisplayContentLocked().mInputMethodWindow;
            if (!isKeyBoardFixedInBottom(inputMethodWindow2) && inputMethodVisibility) {
                Slog.i(TAG, "now not KeyBoardFixedInBottom");
                inBottom = false;
            }
            int stackCount2 = this.mAtms.mRootActivityContainer.getDefaultDisplay().getChildCount();
            int i = stackCount2 - 1;
            while (i >= 0) {
                ActivityStack stack = this.mAtms.mRootActivityContainer.getDefaultDisplay().getChildAt(i);
                if (stack == null || !stack.isCompatible(ColorZoomWindowManager.WINDOWING_MODE_ZOOM, 1)) {
                    inputMethodWindow = inputMethodWindow2;
                    stackCount = stackCount2;
                } else {
                    DisplayContent displayContent = this.mWms.getDefaultDisplayContentLocked();
                    DisplayInfo displayInfo = displayContent.getDisplayInfo();
                    int logicalWidth = Math.min(displayInfo.logicalWidth, displayInfo.logicalHeight);
                    Math.max(displayInfo.logicalWidth, displayInfo.logicalHeight);
                    int statusBar = displayContent.mDisplayFrames.mStable.top;
                    int inputMethodTop = displayContent.mDisplayFrames.mCurrent.bottom;
                    int halfBar = statusBar / 2;
                    inputMethodWindow = inputMethodWindow2;
                    StringBuilder sb = new StringBuilder();
                    stackCount = stackCount2;
                    sb.append("logicalWidth = ");
                    sb.append(logicalWidth);
                    sb.append(", statusBar = ");
                    sb.append(statusBar);
                    sb.append(", inputMethodTop = ");
                    sb.append(inputMethodTop);
                    sb.append(", halfBar = ");
                    sb.append(halfBar);
                    Slog.i(TAG, sb.toString());
                    int delta = (inputMethodTop - statusBar) - halfBar;
                    Slog.i(TAG, "delta = " + delta + ", mInputMethodHeightDefault = " + this.mInputMethodHeightDefault);
                    if (delta <= this.mInputMethodHeightDefault || !inputMethodVisibility || !inBottom) {
                        this.mScaleForInputmethodDefault = ((float) delta) / (((float) logicalWidth) * this.mRatio);
                        int scaleWidth = (int) (((float) logicalWidth) * this.mScaleForInputmethodDefault);
                        int scaleHeight = (int) (((float) logicalWidth) * this.mRatio * this.mScaleForInputmethodDefault);
                        int leftOffset = (logicalWidth - scaleWidth) / 2;
                        Slog.i(TAG, "mScaleForInputmethodDefault = " + this.mScaleForInputmethodDefault + ", scaleWidth = " + scaleWidth + ", scaleHeight = " + scaleHeight + ", leftOffset = " + leftOffset);
                        this.mBoundsPortraitInputmethod.set(leftOffset, statusBar, leftOffset + scaleWidth, statusBar + scaleHeight);
                        Rect tmp = (!inputMethodVisibility || !inBottom) ? this.mBoundsPortraitScale : this.mBoundsPortraitInputmethod;
                        this.mScaleForPortrait = (!inputMethodVisibility || !inBottom) ? this.mScaleForPortraitDefault : this.mScaleForInputmethodDefault;
                        Slog.i(TAG, "mBoundsPortraitInputmethod(Rect) = " + this.mBoundsPortraitInputmethod + ", tmp(Rect) = " + tmp + ", mScaleForPortrait = " + this.mScaleForPortrait);
                        notifyInputMethodChanged(inputMethodVisibility, inBottom);
                        stack.resize(tmp, getTaskBound(), (Rect) null);
                    } else {
                        this.mBoundsPortraitInputmethod.set(this.mBoundsPortraitScale);
                        return;
                    }
                }
                i--;
                inputMethodWindow2 = inputMethodWindow;
                stackCount2 = stackCount;
            }
        }
    }

    public void windowZoomFrame(Rect pf, Rect df, Rect cf, Rect vf, WindowState win) {
        if (!(win.mToken == null || win.mToken.windowType == 2013)) {
            win.mWinAnimator.mXOffset = 0;
            win.mWinAnimator.mYOffset = 0;
        }
        if (win.getWindowingMode() == ColorZoomWindowManager.WINDOWING_MODE_ZOOM) {
            int rotation = win.getDisplayContent().getRotation();
            if (win.getStack() != null) {
                Rect scaledTaskBound = getScaledTaskBound();
                if (rotation == 0) {
                    float f = this.mScaleForPortrait;
                    win.mGlobalScale = f;
                    win.mInvGlobalScale = f;
                } else {
                    float f2 = this.mScaleForLandSpace;
                    win.mGlobalScale = f2;
                    win.mInvGlobalScale = f2;
                }
                win.mWinAnimator.mXOffset = scaledTaskBound.left + ((int) (((float) win.mAttrs.surfaceInsets.left) * (1.0f - win.mGlobalScale)));
                win.mWinAnimator.mYOffset = scaledTaskBound.top + ((int) (((float) win.mAttrs.surfaceInsets.top) * (1.0f - win.mGlobalScale)));
            }
            win.getContainingFrame().set(pf);
            if (win.getAttrs() != null && (win.getAttrs().flags & 512) == 0) {
                if (win.getContentFrameLw().bottom != pf.bottom) {
                    win.getContentFrameLw().bottom = pf.bottom;
                }
                if (win.getVisibleFrameLw().bottom != pf.bottom) {
                    win.getVisibleFrameLw().bottom = pf.bottom;
                }
            }
            if (rotation == 1 || rotation == 3) {
                WindowFrames windowFrames = win.getWindowFrames();
                windowFrames.mDecorFrame.set(win.getDisplayedBounds());
            }
        }
    }

    public void updateInputVisibility(WindowState win, boolean surfaceShown) {
        if (win == null) {
            Slog.e(TAG, "WindowState is null");
        } else if (win.mAttrs.type == 2011) {
            if (surfaceShown) {
                Slog.i(TAG, "mCurrentInputMethodWindow before = " + this.mCurrentInputMethodWindow);
                this.mCurrentInputMethodWindow = win;
                Slog.i(TAG, "mCurrentInputMethodWindow after = " + this.mCurrentInputMethodWindow);
            }
            Slog.i(TAG, "mInputMethodVisibility = " + this.mInputMethodVisibility + ", surfaceShown = " + surfaceShown);
            if (this.mInputMethodVisibility != surfaceShown && win.equals(this.mCurrentInputMethodWindow)) {
                this.mInputMethodVisibility = surfaceShown;
                int rotation = this.mWms.getDefaultDisplayRotation();
                if (rotation != 0 && rotation != 2) {
                    this.mScaleForPortrait = this.mScaleForPortraitDefault;
                    Slog.i(TAG, "updateInputVisibility mScaleForPortrait = " + this.mScaleForPortrait);
                    notifyInputMethodChanged(this.mInputMethodVisibility, true);
                } else if (this.mAtms.mRootActivityContainer.getDefaultDisplay().getStack(ColorZoomWindowManager.WINDOWING_MODE_ZOOM, 1) != null) {
                    this.mWms.mH.post(new Runnable() {
                        /* class com.android.server.wm.$$Lambda$ColorZoomWindowManagerService$Mnhypskkoanq_RTSnWbKWkoEpeI */

                        public final void run() {
                            ColorZoomWindowManagerService.this.lambda$updateInputVisibility$0$ColorZoomWindowManagerService();
                        }
                    });
                }
            }
        } else if (win.mAttrs.type == 2019 && surfaceShown) {
            int rotation2 = this.mWms.getDefaultDisplayRotation();
            if (rotation2 == 0 || rotation2 == 2) {
                Slog.i(TAG, "Navigation bar is show and current screen orientation is portrait");
                this.mWms.mH.postDelayed(new Runnable() {
                    /* class com.android.server.wm.$$Lambda$ColorZoomWindowManagerService$w_lIO6Wc5EbdXXKAjAisb_cwXz8 */

                    public final void run() {
                        ColorZoomWindowManagerService.this.lambda$updateInputVisibility$1$ColorZoomWindowManagerService();
                    }
                }, 100);
            }
        }
    }

    public /* synthetic */ void lambda$updateInputVisibility$0$ColorZoomWindowManagerService() {
        updateInputWindowByZoom(this.mInputMethodVisibility);
    }

    public /* synthetic */ void lambda$updateInputVisibility$1$ColorZoomWindowManagerService() {
        updateInputWindowByZoom(this.mInputMethodVisibility);
    }

    public Rect getScaledTaskBound() {
        initScreen();
        this.mRotation = this.mAtms.mWindowManager.getDefaultDisplayRotation();
        int i = this.mRotation;
        if (i == 1) {
            return this.mBoundsLandSpaceScale;
        }
        if (i == 3) {
            if (!this.mAtms.mWindowManager.hasNavigationBar(0) || this.mAtms.mWindowManager.getNavBarPosition(0) != 1) {
                return this.mBoundsLandSpaceScale;
            }
            if (this.mBoundsLandSpaceScaleNavLeft.isEmpty()) {
                DisplayInfo displayInfo = this.mAtms.mWindowManager.getDefaultDisplayContentLocked().getDisplayInfo();
                int navWidth = displayInfo.logicalWidth - displayInfo.appWidth;
                this.mBoundsLandSpaceScaleNavLeft.set(this.mBoundsLandSpaceScale.left + navWidth, this.mBoundsLandSpaceScale.top, this.mBoundsLandSpaceScale.right + navWidth, this.mBoundsLandSpaceScale.bottom);
            }
            return this.mBoundsLandSpaceScaleNavLeft;
        } else if (!this.mInputMethodVisibility || this.mScaleForPortrait == this.mScaleForPortraitDefault) {
            return this.mBoundsPortraitScale;
        } else {
            return this.mBoundsPortraitInputmethod;
        }
    }

    public int clearWindowFlagsIfNeed(WindowState win, int curFlags) {
        if (win == null || win.getWindowingMode() != ColorZoomWindowManager.WINDOWING_MODE_ZOOM || (curFlags & 32) == 0) {
            return curFlags;
        }
        Slog.i("WindowState", " before child = " + this + " flags = 0x" + Integer.toHexString(curFlags));
        return curFlags & -33;
    }

    public void handleTapOutsideTask(DisplayContent displayContent, Task task, int x, int y) {
        debugLogUtil("ColorZoomWindowManagerService: handleTapOutsideTask", Integer.valueOf(x), Integer.valueOf(y));
        if (task == null) {
            int taskId = getTaskId(displayContent, x, y);
            Slog.i(TAG, "handleTapOutsideTask setFocusedTask taskId = " + taskId);
            if (taskId > 0) {
                this.mLastExitZoomMethod = 5;
                if (displayContent != null) {
                    try {
                        displayContent.prepareAppTransition(101, true, 0, true);
                        ActivityStack zoomStack = this.mAtms.mRootActivityContainer.getDefaultDisplay().getStack(ColorZoomWindowManager.WINDOWING_MODE_ZOOM, 1);
                        if (!(zoomStack == null || zoomStack.getTopActivity() == null)) {
                            zoomStack.getTopActivity().setVisibility(false);
                            displayContent.executeAppTransition();
                        }
                    } catch (RemoteException e) {
                        return;
                    }
                }
                ActivityTaskManager.getService().setFocusedTask(taskId);
            }
        }
    }

    public AnimationAdapter getZoomAnimationAdapter(AnimationAdapter adapter, AppWindowToken appToken, Animation animation, int appStackClipMode, Rect transitStartRect, int transit, boolean enter) {
        Animation animation2;
        if (transit == 100 || transit == 101) {
            animation2 = loadZoomAnimation(transit, enter, appToken, null, null);
            if (animation2 != null && animation2.getZAdjustment() == 1) {
                appToken.mNeedsZBoost = true;
            }
        } else {
            animation2 = animation;
        }
        if (appToken != null && animation2 != null) {
            if (appToken.getWindowingMode() == ColorZoomWindowManager.WINDOWING_MODE_ZOOM || transit == 100 || transit == 101) {
                Rect taskRect = new Rect();
                Rect stackRect = new Rect();
                Point tmpPoint = new Point();
                taskRect.set(appToken.getAnimationBounds(appStackClipMode));
                tmpPoint.set(taskRect.left, taskRect.top);
                taskRect.offsetTo(0, 0);
                stackRect.set(appToken.getStack().getBounds());
                return new LocalAnimationAdapter(new ColorZoomWindowAnimationSpec(animation2, tmpPoint, stackRect, taskRect, appToken.getDisplayContent().mAppTransition.canSkipFirstFrame(), appStackClipMode, true, ZOOM_CORNER_RADIUS), appToken.mWmService.mSurfaceAnimationRunner);
            }
        }
        return adapter;
    }

    public void adjustWindowCropForLeash(AppWindowToken appToken, Rect windowCrop) {
        if (appToken != null && appToken.getWindowingMode() == ColorZoomWindowManager.WINDOWING_MODE_ZOOM) {
            AppTransition appTransition = appToken.getDisplayContent().mAppTransition;
            Rect stackBounds = appToken.getStack().getBounds();
            windowCrop.set(stackBounds);
            Slog.i(TAG, "adjustWindowCropForLeash stackBounds = " + stackBounds + " enter = " + appToken.mEnteringAnimation + "  appTransition =" + appTransition.getAppTransition());
            if (appToken.mAnimationBoundsLayer != null && appTransition.getAppTransition() != 100 && appTransition.getAppTransition() != 101) {
                appToken.mAnimationBoundsLayer.setCornerRadius(ZOOM_CORNER_RADIUS);
            }
        }
    }

    public void adjustWindowFrame(WindowState win, WindowFrames windowFrames) {
        if (win != null && windowFrames != null && inZoomWindowMode(win)) {
            Rect displayBounds = win.getDisplayedBounds();
            windowFrames.mParentFrame.set(displayBounds);
            windowFrames.mDisplayFrame.set(displayBounds);
            windowFrames.mOverscanFrame.set(displayBounds);
            windowFrames.mDecorFrame.set(displayBounds);
            windowFrames.mStableFrame.bottom = displayBounds.bottom;
            windowFrames.mStableFrame.left = 0;
            windowFrames.mStableFrame.right = displayBounds.right;
            windowFrames.mContentFrame.bottom = displayBounds.bottom;
            windowFrames.mContentFrame.left = 0;
            windowFrames.mContentFrame.right = displayBounds.right;
            windowFrames.mVisibleFrame.bottom = displayBounds.bottom;
            windowFrames.mVisibleFrame.left = 0;
            windowFrames.mVisibleFrame.right = displayBounds.right;
        }
    }

    public void amendWindowTapExcludeRegion(DisplayContent dc, Region region) {
        if (dc != null && region != null) {
            DisplayMetrics dm = dc.getDisplayMetrics();
            int deltawidth = WindowManagerService.dipToPixel((int) RESIZE_HANDLE_WIDTH_IN_DP, dm);
            int deltaheight = (int) (((double) dm.heightPixels) * 0.3d);
            Rect left = new Rect(0, deltaheight, deltawidth, dm.heightPixels);
            Rect right = new Rect(dm.widthPixels - deltawidth, deltaheight, dm.widthPixels, dm.heightPixels);
            region.op(left, Region.Op.UNION);
            region.op(right, Region.Op.UNION);
            for (int i = this.mTapExcludedWindows.size() - 1; i >= 0; i--) {
                WindowState win = this.mTapExcludedWindows.get(i);
                if (win.isVisible()) {
                    win.getTouchableRegion(this.mTmpRegion);
                    region.op(this.mTmpRegion, Region.Op.UNION);
                }
            }
        }
    }

    public boolean isKeyBoardFixedInBottom(WindowState inputMethodWindow) {
        if (inputMethodWindow == null) {
            Slog.e(TAG, "isKeyBoardFixedInBottom: inputMethodWindow is null");
            return false;
        } else if (this.mWms.getDefaultDisplayContentLocked().mDisplayFrames.mCurrent.bottom > (this.mScreenHeight * 3) / 4) {
            Slog.w(TAG, "isNotKeyBoardFixedInBottom - will not adjust");
            return false;
        } else {
            Region region = new Region();
            inputMethodWindow.getTouchableRegion(region);
            Rect rect = region.getBounds();
            if (rect.left == 0 && rect.right == this.mScreenWidth && rect.bottom == this.mScreenHeight) {
                return true;
            }
            return false;
        }
    }

    public void prepareSurfaceFromDim(DisplayContent dc) {
        Dimmer dimmer;
        if (dc == null || dc.getDisplayId() == 0) {
            if (!initialized()) {
                Slog.i(TAG, "prepareSurfaceFromDim ignore systemserver not ready");
            }
            if (dc != null && dc.getDisplayId() == 0 && (dimmer = this.mDimmer) != null) {
                dimmer.resetDimStates();
                TaskStack zoomTaskStack = dc.getTopStackInWindowingMode(ColorZoomWindowManager.WINDOWING_MODE_ZOOM);
                TaskStack topStack = dc.getTopStack();
                if (zoomTaskStack == null || !zoomTaskStack.isVisible() || topStack == null || !(zoomTaskStack == topStack || topStack.getWindowingMode() == 2)) {
                    this.mDimmer.stopDim(dc.getPendingTransaction());
                    this.mDimmer.updateDims(dc.getPendingTransaction(), (Rect) null);
                    dc.scheduleAnimation();
                    return;
                }
                Slog.i("DisplayContent", "prepareSurfaces zoomTaskStack = " + zoomTaskStack);
                Rect mTmpDimBoundsRect = new Rect();
                dc.getBounds(mTmpDimBoundsRect);
                this.mDimmer.dimBelow(dc.getPendingTransaction(), zoomTaskStack, (float) DIM_AMOUNT);
                if (this.mDimmer.updateDims(dc.getPendingTransaction(), mTmpDimBoundsRect)) {
                    dc.scheduleAnimation();
                }
                zoomTaskStack.getSurfaceControl().setCornerRadius(ZOOM_CORNER_RADIUS);
                WindowState inputMethodWindow = dc.mInputMethodWindow;
                if (inputMethodWindow != null && inputMethodWindow.isVisible()) {
                    int rotation = this.mWms.getDefaultDisplayRotation();
                    if (rotation == 0 || rotation == 2) {
                        this.mWms.mH.post(new Runnable() {
                            /* class com.android.server.wm.$$Lambda$ColorZoomWindowManagerService$Ygf8Yq82V2lvuuuWKTjgh55Mcxk */

                            public final void run() {
                                ColorZoomWindowManagerService.this.lambda$prepareSurfaceFromDim$2$ColorZoomWindowManagerService();
                            }
                        });
                        return;
                    }
                    return;
                }
                return;
            }
            return;
        }
        Slog.i(TAG, "prepareSurfaceFromDim ignore not default display =" + dc);
    }

    public /* synthetic */ void lambda$prepareSurfaceFromDim$2$ColorZoomWindowManagerService() {
        updateInputWindowByZoom(true);
    }

    public int getWindowState(ActivityStack stack) {
        if (stack == null || stack.getWindowingMode() != ColorZoomWindowManager.WINDOWING_MODE_ZOOM) {
            return -1;
        }
        return 2;
    }

    public boolean validateWindowingMode(int windowingMode, ActivityRecord r, TaskRecord task, int activityType) {
        if (windowingMode == ColorZoomWindowManager.WINDOWING_MODE_ZOOM) {
            return true;
        }
        return false;
    }

    public void adjustInputWindowHandle(InputMonitor monitor, WindowState child, InputWindowHandle inputWindowHandle) {
        if (child != null && inputWindowHandle != null && inZoomWindowMode(child)) {
            inputWindowHandle.frameLeft += child.mWinAnimator.mXOffset;
            inputWindowHandle.frameTop += child.mWinAnimator.mYOffset;
            inputWindowHandle.frameRight += child.mWinAnimator.mXOffset;
            inputWindowHandle.frameBottom += child.mWinAnimator.mYOffset;
            if (child.getParentWindow() == null || !inZoomWindowMode(child.getParentWindow())) {
                inputWindowHandle.inputFeatures |= FLAG_FOR_ZOOM_WINDOW;
            }
        }
    }

    public void addTapExcluedWindow(WindowState win) {
        if (win == null) {
            return;
        }
        if (win.mIsImWindow || (win.mAttrs != null && IColorZoomWindowManager.excludeWindowTypeFromTapOutTask(win.mAttrs.type))) {
            if (DEBUG_INPUT) {
                Slog.i(TAG, "addTapExcluedWindow win = " + win);
            }
            this.mTapExcludedWindows.add(win);
        }
    }

    public void removeTapExcluedWindow(WindowState win) {
        if (win == null) {
            return;
        }
        if (win.mIsImWindow || (win.mAttrs != null && IColorZoomWindowManager.excludeWindowTypeFromTapOutTask(win.mAttrs.type))) {
            if (DEBUG_INPUT) {
                Slog.i(TAG, "removeTapExcluedWindow win = " + win);
            }
            this.mTapExcludedWindows.remove(win);
        }
    }

    public void gestureSwipeFromBottom() {
    }

    private int getTaskId(DisplayContent displayContent, int x, int y) {
        int taskId = -1;
        if (displayContent != null) {
            WindowState currentFocus = displayContent.mCurrentFocus;
            WindowState inputMethodTarget = displayContent.mInputMethodTarget;
            WindowState inputMethodWindow = displayContent.mInputMethodWindow;
            WindowList<TaskStack> taskStackList = displayContent.getStacks();
            if (currentFocus != null && currentFocus.getWindowingMode() == ColorZoomWindowManager.WINDOWING_MODE_ZOOM) {
                if (!(inputMethodTarget == null || inputMethodWindow == null || !inputMethodWindow.isVisible())) {
                    Slog.i("DisplayContent", "taskIdFromPoint skip method show");
                    InputMethodManagerInternal inputMethodManagerInternal = (InputMethodManagerInternal) LocalServices.getService(InputMethodManagerInternal.class);
                    if (inputMethodManagerInternal != null) {
                        inputMethodManagerInternal.hideCurrentInputMethod();
                        return ERROR_INPUT_METHOD_MANAGER_INTERNAL_NOTNULL;
                    }
                }
                int i = taskStackList.size() - 1;
                while (true) {
                    if (i < 0) {
                        break;
                    }
                    TaskStack taskStack = (TaskStack) taskStackList.get(i);
                    if (taskStack.getWindowingMode() != ColorZoomWindowManager.WINDOWING_MODE_ZOOM || !taskStack.getBounds().contains(x, y)) {
                        if (taskStack.getWindowingMode() == 2 && taskStack.getBounds().contains(x, y)) {
                            Slog.i(TAG, "found pip task ignore.");
                            break;
                        }
                        taskId = getTaskIdByTaskStack(taskStack, x, y);
                        if (taskId > 0) {
                            Slog.i(TAG, "getTaskId found target task = " + taskId);
                            break;
                        }
                        i--;
                    } else {
                        Slog.i(TAG, "found zoom task ignore.");
                        break;
                    }
                }
            }
        }
        return taskId;
    }

    private int getTaskIdByTaskStack(TaskStack taskStack, int x, int y) {
        if (taskStack == null) {
            return ERROR_TASKSTACK_ISNULL;
        }
        Rect tmpRect = new Rect();
        taskStack.getBounds(tmpRect);
        if (!tmpRect.contains(x, y) || taskStack.isAdjustedForMinimizedDockedStack()) {
            return ERROR_MINIMIZEDDOCKED_STACK;
        }
        for (int taskNdx = taskStack.mChildren.size() - 1; taskNdx >= 0; taskNdx--) {
            Task task = (Task) taskStack.mChildren.get(taskNdx);
            if (task.getTopVisibleAppMainWindow() != null) {
                task.getDimBounds(tmpRect);
                if (tmpRect.contains(x, y)) {
                    return task.mTaskId;
                }
            }
        }
        return ERROR_TASKSTACK_ISNULL;
    }

    public Animation loadZoomAnimation(int transit, boolean enter, AppWindowToken appToken, Rect targetPositionRect, Rect startRect) {
        Rect appFrame = new Rect();
        if (appToken != null) {
            appFrame.set(appToken.getBounds());
        }
        Interpolator CUBIC_EASE_OUT_INTERPOLATOR = new CubicEaseOutInterpolator();
        DisplayInfo displayInfo = this.mWms.getDefaultDisplayContentLocked().getDisplayInfo();
        int width = displayInfo.appWidth;
        int height = displayInfo.appHeight;
        int appWidth = appFrame.width();
        int appHeight = appFrame.height();
        Animation anim = null;
        if (enter) {
            if (transit == 100) {
                AnimationSet set = new AnimationSet(true);
                Animation scaleAnimation = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, ((float) appWidth) / 2.0f, ((float) appHeight) / 2.0f);
                Animation alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
                set.addAnimation(scaleAnimation);
                set.addAnimation(alphaAnimation);
                set.setDuration(300);
                set.setInterpolator(CUBIC_EASE_OUT_INTERPOLATOR);
                set.setZAdjustment(1);
                anim = set;
            }
        } else if (transit == 101) {
            AnimationSet set2 = new AnimationSet(true);
            Animation scaleAnimation2 = new ScaleAnimation(1.0f, 0.0f, 1.0f, 0.0f, ((float) appWidth) / 2.0f, ((float) appHeight) / 2.0f);
            Animation alphaAnimation2 = new AlphaAnimation(1.0f, 0.0f);
            set2.addAnimation(scaleAnimation2);
            set2.addAnimation(alphaAnimation2);
            set2.setDuration(300);
            set2.setInterpolator(CUBIC_EASE_OUT_INTERPOLATOR);
            set2.setZAdjustment(1);
            anim = set2;
        }
        if (anim != null) {
            Slog.v(TAG, "Loaded zoom animation " + anim + " for appToken=" + appToken + "   appFrame=" + appFrame + "   enter=" + enter);
            anim.initialize(appWidth, appHeight, width, height);
            anim.scaleCurrentDuration(this.mWms.getTransitionAnimationScaleLocked());
        }
        return anim;
    }

    public void prepareZoomTransition(ActivityStack curStack, ActivityStack nextStack) {
        Slog.v(TAG, "prepareZoomTransition: curStack=" + curStack + "   nextStack" + nextStack);
        if (curStack != null && nextStack != null && curStack.getWindowingMode() == ColorZoomWindowManager.WINDOWING_MODE_ZOOM && nextStack.getWindowingMode() == 1) {
            nextStack.getDisplay().mDisplayContent.prepareAppTransition(101, true, 0, true);
            ActivityRecord curTop = curStack.getTopActivity();
            if (curTop != null) {
                curTop.setVisibility(false);
            }
        }
    }

    public void onAnimationFinished(ActivityRecord r) {
        ActivityStack focuStack;
        Slog.v(TAG, "onAnimationFinished:  r=" + r);
        if (r != null && !r.visible && r.getActivityStack() != null && r.getActivityStack().getWindowingMode() == ColorZoomWindowManager.WINDOWING_MODE_ZOOM && (focuStack = r.getActivityStack().getDisplay().getFocusedStack()) != null && focuStack != r.getActivityStack() && focuStack.getWindowingMode() == 1) {
            r.getActivityStack().setWindowingMode(1, false, false, false, true, false);
            this.mLastExitZoomMethod = 8;
            notifyZoomWindowHide(getZoomWindowInfo(), r.getActivityStack(), true);
            Slog.v(TAG, "onAnimationFinished: exit zoom mode  curStack=" + r.getActivityStack() + "   r=" + r);
        }
    }

    public void topResumedActivityChanged(ActivityRecord r) {
        if (r != null && r.visible && r.getActivityStack() != null && r.getActivityStack().isFocusedStackOnDisplay()) {
            Slog.i(TAG, "topResumedActivityChanged start r = " + r);
            if (r.getWindowingMode() == ColorZoomWindowManager.WINDOWING_MODE_ZOOM) {
                notifyZoomActivityShown(r);
            } else if (r.getWindowingMode() == 1) {
                Slog.i(TAG, "WINDOWING_MODE_FULLSCREEN : will notifyZoomWindowHide");
                notifyZoomWindowHide(getZoomWindowInfo(), r.getActivityStack(), true);
            }
            Slog.i(TAG, "topResumedActivityChanged end");
        }
    }

    public void debugLogUtil(String key, Object... vars) {
        ColorZoomWindowDebugUtil.debugLogUtil(key, vars);
    }

    public DisplayInfo getZoomModeDisplayInfo(DisplayInfo info, int displayId, int callingUid) {
        DisplayInfo newInfo;
        synchronized (this.mLockUid) {
            newInfo = new DisplayInfo(info);
            if (isZoomUidOnDisplay(callingUid, displayId) && UserHandle.getAppId(callingUid) >= 10000) {
                if (newInfo.appWidth > newInfo.appHeight) {
                    int width = this.mBoundsLandSpaceDefault.width();
                    newInfo.logicalWidth = width;
                    newInfo.appWidth = width;
                    int height = this.mBoundsLandSpaceDefault.height();
                    newInfo.logicalHeight = height;
                    newInfo.appHeight = height;
                    newInfo.rotation = 0;
                } else {
                    int width2 = this.mBoundsPortraitDefault.width();
                    newInfo.logicalWidth = width2;
                    newInfo.appWidth = width2;
                    int height2 = this.mBoundsPortraitDefault.height();
                    newInfo.logicalHeight = height2;
                    newInfo.appHeight = height2;
                }
                Slog.w(TAG, "getZoomModeDisplayInfo: old info =" + info + "   new info=" + newInfo + "   displayId=" + displayId + "   callingUid=" + callingUid);
            }
        }
        return newInfo;
    }

    public boolean isZoomUidOnDisplay(int uid, int displayId) {
        IntArray zoomUIDs = this.mDisplayZoomModeUIDs.get(displayId);
        return (zoomUIDs == null || zoomUIDs.indexOf(uid) == -1) ? false : true;
    }

    private void updateZoomUIDsOnDisplay(ActivityStack stack, boolean visible) {
        if (stack != null) {
            synchronized (this.mLockUid) {
                IntArray zoomModeUIDs = new IntArray();
                stack.getPresentUIDs(zoomModeUIDs);
                if (visible) {
                    this.mDisplayZoomModeUIDs.append(stack.mDisplayId, zoomModeUIDs);
                } else {
                    this.mDisplayZoomModeUIDs.delete(stack.mDisplayId);
                }
                if (this.mDisplayZoomModeUIDs.get(stack.mDisplayId) != null) {
                    Slog.i(TAG, "updateZoomUIDsOnDisplay: stack=" + stack + "  visible=" + visible);
                    IntArray curZoomUIDs = this.mDisplayZoomModeUIDs.get(stack.mDisplayId);
                    for (int i = 0; i < curZoomUIDs.size(); i++) {
                        Slog.i(TAG, "updateZoomUIDsOnDisplay: index=" + i + "  uid=" + curZoomUIDs.get(i));
                    }
                }
            }
        }
    }

    public boolean shouldClearReusedActivity(ActivityRecord reusedActivity, ActivityOptions options, ActivityRecord startActivity, MergedConfiguration lastReportedConfiguration) {
        int windowMode;
        if (reusedActivity != null && lastReportedConfiguration != null) {
            boolean unReused = isUnReusedActivityInZoomMode(reusedActivity.mActivityComponent);
            boolean reusedIsZoom = lastReportedConfiguration.getOverrideConfiguration().windowConfiguration.getWindowingMode() == ColorZoomWindowManager.WINDOWING_MODE_ZOOM;
            if (options != null) {
                windowMode = options.getLaunchWindowingMode();
            } else {
                windowMode = -1;
            }
            boolean shouldLaunchInZoom = windowMode == ColorZoomWindowManager.WINDOWING_MODE_ZOOM;
            if (!(startActivity.launchedFromUid == startActivity.info.applicationInfo.uid) && unReused && ((reusedIsZoom && !shouldLaunchInZoom) || (shouldLaunchInZoom && !reusedIsZoom))) {
                Slog.i(TAG, "shouldClearReusedActivity: not reused reusedActivity=" + reusedActivity + "  when launch startActivity=" + startActivity + "  reusedIsZoom=" + reusedIsZoom + "  shouldLaunchInZoom=" + shouldLaunchInZoom);
                ActivityStack stack = reusedActivity.getActivityStack();
                if (stack != null) {
                    stack.finishActivityLocked(reusedActivity, 0, (Intent) null, "finish-reused", false);
                }
                return true;
            }
        }
        return false;
    }

    public boolean shouldIgnoreInputShownForResult(int uid, int displayId) {
        ActivityStack topStack;
        if (!isZoomUidOnDisplay(uid, displayId) || (topStack = this.mAtms.getTopDisplayFocusedStack()) == null || topStack.getTopActivity() == null) {
            return false;
        }
        ActivityRecord curTop = topStack.getTopActivity();
        if (curTop.getWindowingMode() != ColorZoomWindowManager.WINDOWING_MODE_ZOOM || curTop.appInfo.uid != uid || !isIgnoreInputShownForResult(curTop.mActivityComponent)) {
            return false;
        }
        Slog.i(TAG, "ignoreInputShownForResult: curTop=" + curTop);
        return true;
    }

    private boolean isIgnoreInputShownForResult(ComponentName componentName) {
        List<String> list = ColorZoomWindowConfig.getInstance().getIgnoreInputShownForResultList();
        if (list == null || list.isEmpty() || componentName == null) {
            return false;
        }
        return list.contains(componentName.flattenToShortString());
    }

    class CubicEaseOutInterpolator implements Interpolator {
        CubicEaseOutInterpolator() {
        }

        public float getInterpolation(float t) {
            float t2 = t - 1.0f;
            return (t2 * t2 * t2) + 1.0f;
        }
    }

    private static OppoBaseActivityStack typeCasting(ActivityStack stack) {
        if (stack != null) {
            return (OppoBaseActivityStack) ColorTypeCastingHelper.typeCasting(OppoBaseActivityStack.class, stack);
        }
        return null;
    }

    private boolean inZoomWindowMode(WindowState win) {
        if (win == null || win.getWindowingMode() != ColorZoomWindowManager.WINDOWING_MODE_ZOOM) {
            return false;
        }
        return true;
    }

    private boolean inZoomWindowMode(ActivityStack stack) {
        if (stack == null || stack.getWindowingMode() != ColorZoomWindowManager.WINDOWING_MODE_ZOOM) {
            return false;
        }
        return true;
    }
}
