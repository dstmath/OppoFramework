package com.android.server.wm;

import android.app.ActivityManager;
import android.app.ActivityThread;
import android.app.LoadedApk;
import android.app.ResourcesManager;
import android.common.OppoFeatureCache;
import android.content.Context;
import android.content.Intent;
import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Insets;
import android.graphics.Rect;
import android.graphics.Region;
import android.hardware.input.InputManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.ArraySet;
import android.util.BoostFramework;
import android.util.Pair;
import android.util.PrintWriterPrinter;
import android.util.Slog;
import android.view.DisplayCutout;
import android.view.IApplicationToken;
import android.view.InputChannel;
import android.view.InputEvent;
import android.view.InputEventReceiver;
import android.view.MotionEvent;
import android.view.ViewRootImpl;
import android.view.WindowManager;
import android.view.WindowManagerPolicyConstants;
import android.view.accessibility.AccessibilityManager;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.policy.ScreenDecorationsUtils;
import com.android.internal.util.ScreenShapeHelper;
import com.android.internal.util.ScreenshotHelper;
import com.android.internal.util.ToBooleanFunction;
import com.android.internal.util.function.TriConsumer;
import com.android.internal.widget.PointerLocationView;
import com.android.server.LocalServices;
import com.android.server.UiThread;
import com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils;
import com.android.server.display.OppoBrightUtils;
import com.android.server.pm.DumpState;
import com.android.server.policy.WindowManagerPolicy;
import com.android.server.policy.WindowOrientationListener;
import com.android.server.statusbar.StatusBarManagerInternal;
import com.android.server.usb.descriptors.UsbACInterface;
import com.android.server.wallpaper.WallpaperManagerInternal;
import com.android.server.wm.ActivityTaskManagerInternal;
import com.android.server.wm.BarController;
import com.android.server.wm.SystemGesturesPointerEventListener;
import com.android.server.wm.utils.InsetUtils;
import com.color.screenshot.IColorScreenShotEuclidManager;
import com.color.screenshot.IColorScreenshotHelper;
import java.io.PrintWriter;

public class DisplayPolicy extends OppoBaseDisplayPolicy {
    private static final boolean ALTERNATE_CAR_MODE_NAV_SIZE = false;
    private static final boolean DEBUG = false;
    private static final int MSG_DISABLE_POINTER_LOCATION = 5;
    private static final int MSG_DISPOSE_INPUT_CONSUMER = 3;
    private static final int MSG_ENABLE_POINTER_LOCATION = 4;
    private static final int MSG_REQUEST_TRANSIENT_BARS = 2;
    private static final int MSG_REQUEST_TRANSIENT_BARS_ARG_NAVIGATION = 1;
    private static final int MSG_REQUEST_TRANSIENT_BARS_ARG_STATUS = 0;
    private static final int MSG_UPDATE_DREAMING_SLEEP_TOKEN = 1;
    private static final int NAV_BAR_FORCE_TRANSPARENT = 2;
    private static final int NAV_BAR_OPAQUE_WHEN_FREEFORM_OR_DOCKED = 0;
    private static final int NAV_BAR_TRANSLUCENT_WHEN_FREEFORM_OPAQUE_OTHERWISE = 1;
    private static final long PANIC_GESTURE_EXPIRATION = 30000;
    private static boolean SCROLL_BOOST_SS_ENABLE = false;
    private static final int SYSTEM_UI_CHANGING_LAYOUT = -1073709042;
    private static final String TAG = "WindowManager";
    private static boolean isLowRAM = false;
    private static final Rect sTmpDisplayCutoutSafeExceptMaybeBarsRect = new Rect();
    private static final Rect sTmpDockedFrame = new Rect();
    private static final Rect sTmpLastParentFrame = new Rect();
    private static final Rect sTmpNavFrame = new Rect();
    private static final Rect sTmpRect = new Rect();
    /* access modifiers changed from: private */
    public final AccessibilityManager mAccessibilityManager;
    private final Runnable mAcquireSleepTokenRunnable;
    private boolean mAllowLockscreenWhenOn;
    private volatile boolean mAllowSeamlessRotationDespiteNavBarMoving;
    private volatile boolean mAwake;
    private int mBottomGestureAdditionalInset;
    private final boolean mCarDockEnablesAccelerometer;
    /* access modifiers changed from: private */
    public final Runnable mClearHideNavigationFlag = new Runnable() {
        /* class com.android.server.wm.DisplayPolicy.AnonymousClass3 */

        public void run() {
            synchronized (DisplayPolicy.this.mLock) {
                DisplayPolicy.access$1472(DisplayPolicy.this, -3);
                DisplayPolicy.this.mDisplayContent.reevaluateStatusBarVisibility();
            }
        }
    };
    final Context mContext;
    private Resources mCurrentUserResources;
    private final boolean mDeskDockEnablesAccelerometer;
    /* access modifiers changed from: private */
    public final DisplayContent mDisplayContent;
    private volatile int mDockMode = 0;
    private final Rect mDockedStackBounds = new Rect();
    private boolean mDreamingLockscreen;
    @GuardedBy({"mHandler"})
    private ActivityTaskManagerInternal.SleepToken mDreamingSleepToken;
    private boolean mDreamingSleepTokenNeeded;
    IApplicationToken mFocusedApp;
    private WindowState mFocusedWindow;
    /* access modifiers changed from: private */
    public int mForceClearedSystemUiFlags = 0;
    private boolean mForceShowSystemBars;
    private boolean mForceShowSystemBarsFromExternal;
    private boolean mForceStatusBar;
    private boolean mForceStatusBarFromKeyguard;
    private boolean mForceStatusBarTransparent;
    private boolean mForceUpdateSystemUiVisInSpecialWindow = false;
    private boolean mForcingShowNavBar;
    private int mForcingShowNavBarLayer;
    private Insets mForwardedInsets = Insets.NONE;
    /* access modifiers changed from: private */
    public final Handler mHandler;
    private volatile boolean mHasNavigationBar;
    private volatile boolean mHasStatusBar;
    private volatile boolean mHdmiPlugged;
    private final Runnable mHiddenNavPanic = new Runnable() {
        /* class com.android.server.wm.DisplayPolicy.AnonymousClass4 */

        /* JADX WARNING: Code restructure failed: missing block: B:11:0x0032, code lost:
            return;
         */
        public void run() {
            synchronized (DisplayPolicy.this.mLock) {
                if (DisplayPolicy.this.mService.mPolicy.isUserSetupComplete()) {
                    long unused = DisplayPolicy.this.mPendingPanicGestureUptime = SystemClock.uptimeMillis();
                    if (!DisplayPolicy.isNavBarEmpty(DisplayPolicy.this.mLastSystemUiFlags)) {
                        DisplayPolicy.this.mNavigationBarController.showTransient();
                    }
                }
            }
        }
    };
    private final ImmersiveModeConfirmation mImmersiveModeConfirmation;
    /* access modifiers changed from: private */
    public WindowManagerPolicy.InputConsumer mInputConsumer = null;
    private boolean mIsPerfBoostFlingAcquired;
    private volatile boolean mKeyguardDrawComplete;
    private final Rect mLastDockedStackBounds = new Rect();
    private int mLastDockedStackSysUiFlags;
    private boolean mLastFocusNeedsMenu = false;
    private WindowState mLastFocusedWindow;
    private int mLastFullscreenStackSysUiFlags;
    private int mLastNavBarVis;
    private final Rect mLastNonDockedStackBounds = new Rect();
    private boolean mLastShowingDream;
    int mLastSystemUiFlags;
    private boolean mLastWindowSleepTokenNeeded;
    private volatile int mLidState = -1;
    /* access modifiers changed from: private */
    public final Object mLock;
    private int mNavBarOpacityMode = 0;
    private final BarController.OnBarVisibilityChangedListener mNavBarVisibilityListener = new BarController.OnBarVisibilityChangedListener() {
        /* class com.android.server.wm.DisplayPolicy.AnonymousClass1 */

        @Override // com.android.server.wm.BarController.OnBarVisibilityChangedListener
        public void onBarVisibilityChanged(boolean visible) {
            if (DisplayPolicy.this.mAccessibilityManager != null) {
                DisplayPolicy.this.mAccessibilityManager.notifyAccessibilityButtonVisibilityChanged(visible);
            }
        }
    };
    /* access modifiers changed from: private */
    public WindowState mNavigationBar = null;
    /* access modifiers changed from: private */
    public volatile boolean mNavigationBarAlwaysShowOnSideGesture;
    private volatile boolean mNavigationBarCanMove;
    /* access modifiers changed from: private */
    public final BarController mNavigationBarController;
    private int[] mNavigationBarFrameHeightForRotationDefault = new int[4];
    /* access modifiers changed from: private */
    public int[] mNavigationBarHeightForRotationDefault = new int[4];
    private int[] mNavigationBarHeightForRotationInCarMode = new int[4];
    private volatile boolean mNavigationBarLetsThroughTaps;
    /* access modifiers changed from: private */
    public int mNavigationBarPosition = 4;
    private int[] mNavigationBarWidthForRotationDefault = new int[4];
    private int[] mNavigationBarWidthForRotationInCarMode = new int[4];
    private final Rect mNonDockedStackBounds = new Rect();
    /* access modifiers changed from: private */
    public long mPendingPanicGestureUptime;
    BoostFramework mPerf = new BoostFramework();
    BoostFramework mPerfBoostDrag = null;
    BoostFramework mPerfBoostFling = null;
    BoostFramework mPerfBoostPrefling = null;
    private volatile boolean mPersistentVrModeEnabled;
    private PointerLocationView mPointerLocationView;
    private RefreshRatePolicy mRefreshRatePolicy;
    private final Runnable mReleaseSleepTokenRunnable;
    /* access modifiers changed from: private */
    public int mResettingSystemUiFlags = 0;
    private final ArraySet<WindowState> mScreenDecorWindows = new ArraySet<>();
    private volatile boolean mScreenOnEarly;
    private volatile boolean mScreenOnFully;
    private volatile WindowManagerPolicy.ScreenOnListener mScreenOnListener;
    /* access modifiers changed from: private */
    public final ScreenshotHelper mScreenshotHelper;
    final WindowManagerService mService;
    final Object mServiceAcquireLock = new Object();
    private boolean mShowingDream;
    private int mSideGestureInset;
    /* access modifiers changed from: private */
    public WindowState mStatusBar = null;
    private final StatusBarController mStatusBarController;
    private final int[] mStatusBarHeightForRotation = new int[4];
    private StatusBarManagerInternal mStatusBarManagerInternal;
    /* access modifiers changed from: private */
    public final SystemGesturesPointerEventListener mSystemGestures;
    private WindowState mTopDockedOpaqueOrDimmingWindowState;
    private WindowState mTopDockedOpaqueWindowState;
    private WindowState mTopFullscreenOpaqueOrDimmingWindowState;
    WindowState mTopFullscreenOpaqueWindowState;
    private boolean mTopIsFullscreen;
    private volatile boolean mWindowManagerDrawComplete;
    private int mWindowOutsetBottom;
    @GuardedBy({"mHandler"})
    private ActivityTaskManagerInternal.SleepToken mWindowSleepToken;
    private boolean mWindowSleepTokenNeeded;

    static /* synthetic */ int access$1472(DisplayPolicy x0, int x1) {
        int i = x0.mForceClearedSystemUiFlags & x1;
        x0.mForceClearedSystemUiFlags = i;
        return i;
    }

    /* access modifiers changed from: package-private */
    public StatusBarManagerInternal getStatusBarManagerInternal() {
        StatusBarManagerInternal statusBarManagerInternal;
        synchronized (this.mServiceAcquireLock) {
            if (this.mStatusBarManagerInternal == null) {
                this.mStatusBarManagerInternal = (StatusBarManagerInternal) LocalServices.getService(StatusBarManagerInternal.class);
            }
            statusBarManagerInternal = this.mStatusBarManagerInternal;
        }
        return statusBarManagerInternal;
    }

    private class PolicyHandler extends Handler {
        PolicyHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            int i = msg.what;
            boolean z = true;
            if (i == 1) {
                DisplayPolicy displayPolicy = DisplayPolicy.this;
                if (msg.arg1 == 0) {
                    z = false;
                }
                displayPolicy.updateDreamingSleepToken(z);
            } else if (i == 2) {
                WindowState targetBar = msg.arg1 == 0 ? DisplayPolicy.this.mStatusBar : DisplayPolicy.this.mNavigationBar;
                if (targetBar != null) {
                    DisplayPolicy.this.requestTransientBars(targetBar);
                }
            } else if (i == 3) {
                DisplayPolicy.this.disposeInputConsumer((WindowManagerPolicy.InputConsumer) msg.obj);
            } else if (i == 4) {
                DisplayPolicy.this.enablePointerLocation();
            } else if (i == 5) {
                DisplayPolicy.this.disablePointerLocation();
            }
        }
    }

    DisplayPolicy(WindowManagerService service, DisplayContent displayContent) {
        Context context;
        ScreenshotHelper screenshotHelper = null;
        this.mService = service;
        if (displayContent.isDefaultDisplay) {
            context = service.mContext;
        } else {
            context = service.mContext.createDisplayContext(displayContent.getDisplay());
        }
        this.mContext = context;
        this.mDisplayContent = displayContent;
        this.mLock = service.getWindowManagerLock();
        int displayId = displayContent.getDisplayId();
        this.mStatusBarController = new StatusBarController(displayId);
        this.mNavigationBarController = new BarController("NavigationBar", displayId, 134217728, 536870912, Integer.MIN_VALUE, 2, 134217728, 32768);
        Resources r = this.mContext.getResources();
        this.mCarDockEnablesAccelerometer = r.getBoolean(17891386);
        this.mDeskDockEnablesAccelerometer = r.getBoolean(17891401);
        this.mForceShowSystemBarsFromExternal = r.getBoolean(17891458);
        this.mAccessibilityManager = (AccessibilityManager) this.mContext.getSystemService("accessibility");
        if (!displayContent.isDefaultDisplay) {
            this.mAwake = true;
            this.mScreenOnEarly = true;
            this.mScreenOnFully = true;
        }
        Looper looper = UiThread.getHandler().getLooper();
        this.mHandler = createPolicyHandler(looper);
        this.mColorDpInner = new ColorDisplayPolicyInner();
        this.mColorDpEx = service.getColorDisplayPolicyEx(this);
        this.mSystemGestures = new SystemGesturesPointerEventListener(this.mContext, this.mHandler, new SystemGesturesPointerEventListener.Callbacks() {
            /* class com.android.server.wm.DisplayPolicy.AnonymousClass2 */

            @Override // com.android.server.wm.SystemGesturesPointerEventListener.Callbacks
            public void onSwipeFromTop() {
                if (!DisplayPolicy.this.requestGameDockIfNecessary() && DisplayPolicy.this.mStatusBar != null) {
                    DisplayPolicy displayPolicy = DisplayPolicy.this;
                    displayPolicy.requestTransientBars(displayPolicy.mStatusBar);
                }
            }

            @Override // com.android.server.wm.SystemGesturesPointerEventListener.Callbacks
            public void onSwipeFromBottom() {
                if (DisplayPolicy.this.mNavigationBar != null && DisplayPolicy.this.mNavigationBarPosition == 4) {
                    DisplayPolicy displayPolicy = DisplayPolicy.this;
                    displayPolicy.requestTransientBars(displayPolicy.mNavigationBar);
                }
                OppoFeatureCache.get(IColorZoomWindowManager.DEFAULT).gestureSwipeFromBottom();
            }

            @Override // com.android.server.wm.SystemGesturesPointerEventListener.Callbacks
            public void onSwipeFromRight() {
                Region excludedRegion;
                synchronized (DisplayPolicy.this.mLock) {
                    excludedRegion = DisplayPolicy.this.mDisplayContent.calculateSystemGestureExclusion();
                }
                boolean sideAllowed = DisplayPolicy.this.mNavigationBarAlwaysShowOnSideGesture || DisplayPolicy.this.mNavigationBarPosition == 2;
                if (DisplayPolicy.this.mNavigationBar != null && sideAllowed && !DisplayPolicy.this.mSystemGestures.currentGestureStartedInRegion(excludedRegion)) {
                    DisplayPolicy displayPolicy = DisplayPolicy.this;
                    displayPolicy.requestTransientBars(displayPolicy.mNavigationBar);
                }
            }

            @Override // com.android.server.wm.SystemGesturesPointerEventListener.Callbacks
            public void onSwipeFromLeft() {
                Region excludedRegion;
                synchronized (DisplayPolicy.this.mLock) {
                    excludedRegion = DisplayPolicy.this.mDisplayContent.calculateSystemGestureExclusion();
                }
                boolean sideAllowed = true;
                if (!DisplayPolicy.this.mNavigationBarAlwaysShowOnSideGesture && DisplayPolicy.this.mNavigationBarPosition != 1) {
                    sideAllowed = false;
                }
                if (DisplayPolicy.this.mNavigationBar != null && sideAllowed && !DisplayPolicy.this.mSystemGestures.currentGestureStartedInRegion(excludedRegion)) {
                    DisplayPolicy displayPolicy = DisplayPolicy.this;
                    displayPolicy.requestTransientBars(displayPolicy.mNavigationBar);
                }
            }

            @Override // com.android.server.wm.SystemGesturesPointerEventListener.Callbacks
            public void onFling(int duration) {
                if (DisplayPolicy.this.mService.mPowerManagerInternal != null) {
                    DisplayPolicy.this.mService.mPowerManagerInternal.powerHint(2, duration);
                }
            }

            @Override // com.android.server.wm.SystemGesturesPointerEventListener.Callbacks
            public void onDebug() {
            }

            private WindowOrientationListener getOrientationListener() {
                DisplayRotation rotation = DisplayPolicy.this.mDisplayContent.getDisplayRotation();
                if (rotation != null) {
                    return rotation.getOrientationListener();
                }
                return null;
            }

            @Override // com.android.server.wm.SystemGesturesPointerEventListener.Callbacks
            public void onDown() {
                WindowOrientationListener listener = getOrientationListener();
                if (listener != null) {
                    listener.onTouchStart();
                }
            }

            @Override // com.android.server.wm.SystemGesturesPointerEventListener.Callbacks
            public void onUpOrCancel() {
                WindowOrientationListener listener = getOrientationListener();
                if (listener != null) {
                    listener.onTouchEnd();
                }
            }

            @Override // com.android.server.wm.SystemGesturesPointerEventListener.Callbacks
            public void onMouseHoverAtTop() {
                DisplayPolicy.this.mHandler.removeMessages(2);
                Message msg = DisplayPolicy.this.mHandler.obtainMessage(2);
                msg.arg1 = 0;
                DisplayPolicy.this.mHandler.sendMessageDelayed(msg, 500);
            }

            @Override // com.android.server.wm.SystemGesturesPointerEventListener.Callbacks
            public void onMouseHoverAtBottom() {
                DisplayPolicy.this.mHandler.removeMessages(2);
                Message msg = DisplayPolicy.this.mHandler.obtainMessage(2);
                msg.arg1 = 1;
                DisplayPolicy.this.mHandler.sendMessageDelayed(msg, 500);
            }

            @Override // com.android.server.wm.SystemGesturesPointerEventListener.Callbacks
            public void onMouseLeaveFromEdge() {
                DisplayPolicy.this.mHandler.removeMessages(2);
            }
        });
        displayContent.registerPointerEventListener(this.mSystemGestures);
        displayContent.mAppTransition.registerListenerLocked(this.mStatusBarController.getAppTransitionListener());
        this.mImmersiveModeConfirmation = new ImmersiveModeConfirmation(this.mContext, looper, this.mService.mVrModeEnabled);
        this.mAcquireSleepTokenRunnable = new Runnable(service, displayId) {
            /* class com.android.server.wm.$$Lambda$DisplayPolicy$j3sY1jb4WFF_F3wOT9D2fB2mOts */
            private final /* synthetic */ WindowManagerService f$1;
            private final /* synthetic */ int f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                DisplayPolicy.this.lambda$new$0$DisplayPolicy(this.f$1, this.f$2);
            }
        };
        this.mReleaseSleepTokenRunnable = new Runnable() {
            /* class com.android.server.wm.$$Lambda$DisplayPolicy$_FsvHpVUigbWmSpT009cJNNmgM */

            public final void run() {
                DisplayPolicy.this.lambda$new$1$DisplayPolicy();
            }
        };
        this.mScreenshotHelper = displayContent.isDefaultDisplay ? new ScreenshotHelper(this.mContext) : screenshotHelper;
        if (this.mDisplayContent.isDefaultDisplay) {
            this.mHasStatusBar = true;
            this.mHasNavigationBar = this.mContext.getResources().getBoolean(17891514);
            String navBarOverride = SystemProperties.get("qemu.hw.mainkeys");
            if ("1".equals(navBarOverride)) {
                this.mHasNavigationBar = false;
            } else if ("0".equals(navBarOverride)) {
                this.mHasNavigationBar = true;
            }
        } else {
            this.mHasStatusBar = false;
            this.mHasNavigationBar = this.mDisplayContent.supportsSystemDecorations();
        }
        this.mRefreshRatePolicy = new RefreshRatePolicy(this.mService, this.mDisplayContent.getDisplayInfo(), this.mService.mHighRefreshRateBlacklist);
    }

    public /* synthetic */ void lambda$new$0$DisplayPolicy(WindowManagerService service, int displayId) {
        if (this.mWindowSleepToken == null) {
            ActivityTaskManagerInternal activityTaskManagerInternal = service.mAtmInternal;
            this.mWindowSleepToken = activityTaskManagerInternal.acquireSleepToken("WindowSleepTokenOnDisplay" + displayId, displayId);
        }
    }

    public /* synthetic */ void lambda$new$1$DisplayPolicy() {
        ActivityTaskManagerInternal.SleepToken sleepToken = this.mWindowSleepToken;
        if (sleepToken != null) {
            sleepToken.release();
            this.mWindowSleepToken = null;
        }
    }

    /* access modifiers changed from: package-private */
    public void systemReady() {
        this.mSystemGestures.systemReady();
        if (this.mService.mPointerLocationEnabled) {
            setPointerLocationEnabled(true);
        }
    }

    private int getDisplayId() {
        return this.mDisplayContent.getDisplayId();
    }

    public void setHdmiPlugged(boolean plugged) {
        setHdmiPlugged(plugged, false);
    }

    public void setHdmiPlugged(boolean plugged, boolean force) {
        if (force || this.mHdmiPlugged != plugged) {
            this.mHdmiPlugged = plugged;
            this.mService.updateRotation(true, true);
            Intent intent = new Intent("android.intent.action.HDMI_PLUGGED");
            intent.addFlags(67108864);
            intent.putExtra("state", plugged);
            this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isHdmiPlugged() {
        return this.mHdmiPlugged;
    }

    /* access modifiers changed from: package-private */
    public boolean isCarDockEnablesAccelerometer() {
        return this.mCarDockEnablesAccelerometer;
    }

    /* access modifiers changed from: package-private */
    public boolean isDeskDockEnablesAccelerometer() {
        return this.mDeskDockEnablesAccelerometer;
    }

    public void setPersistentVrModeEnabled(boolean persistentVrModeEnabled) {
        this.mPersistentVrModeEnabled = persistentVrModeEnabled;
    }

    public boolean isPersistentVrModeEnabled() {
        return this.mPersistentVrModeEnabled;
    }

    public void setDockMode(int dockMode) {
        this.mDockMode = dockMode;
    }

    public int getDockMode() {
        return this.mDockMode;
    }

    /* access modifiers changed from: package-private */
    public void setForceShowSystemBars(boolean forceShowSystemBars) {
        this.mForceShowSystemBarsFromExternal = forceShowSystemBars;
    }

    public boolean hasNavigationBar() {
        return this.mHasNavigationBar;
    }

    public boolean hasStatusBar() {
        return this.mHasStatusBar;
    }

    public boolean navigationBarCanMove() {
        return this.mNavigationBarCanMove;
    }

    public void setLidState(int lidState) {
        this.mLidState = lidState;
    }

    public int getLidState() {
        return this.mLidState;
    }

    public void setAwake(boolean awake) {
        this.mAwake = awake;
    }

    public boolean isAwake() {
        return this.mAwake;
    }

    public boolean isScreenOnEarly() {
        return this.mScreenOnEarly;
    }

    public boolean isScreenOnFully() {
        return this.mScreenOnFully;
    }

    public boolean isKeyguardDrawComplete() {
        return this.mKeyguardDrawComplete;
    }

    public boolean isWindowManagerDrawComplete() {
        return this.mWindowManagerDrawComplete;
    }

    public WindowManagerPolicy.ScreenOnListener getScreenOnListener() {
        return this.mScreenOnListener;
    }

    public void screenTurnedOn(WindowManagerPolicy.ScreenOnListener screenOnListener) {
        synchronized (this.mLock) {
            this.mScreenOnEarly = true;
            this.mScreenOnFully = false;
            this.mKeyguardDrawComplete = false;
            this.mWindowManagerDrawComplete = false;
            this.mScreenOnListener = screenOnListener;
        }
    }

    public void screenTurnedOff() {
        synchronized (this.mLock) {
            this.mScreenOnEarly = false;
            this.mScreenOnFully = false;
            this.mKeyguardDrawComplete = false;
            this.mWindowManagerDrawComplete = false;
            this.mScreenOnListener = null;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0015, code lost:
        return false;
     */
    public boolean finishKeyguardDrawn() {
        synchronized (this.mLock) {
            if (this.mScreenOnEarly) {
                if (!this.mKeyguardDrawComplete) {
                    this.mKeyguardDrawComplete = true;
                    this.mWindowManagerDrawComplete = false;
                    return true;
                }
            }
        }
    }

    public boolean finishWindowsDrawn() {
        synchronized (this.mLock) {
            if (this.mScreenOnEarly) {
                if (!this.mWindowManagerDrawComplete) {
                    this.mWindowManagerDrawComplete = true;
                    return true;
                }
            }
            return false;
        }
    }

    public boolean finishScreenTurningOn() {
        synchronized (this.mLock) {
            if (WindowManagerDebugConfig.DEBUG_SCREEN_ON) {
                Slog.d("WindowManager", "finishScreenTurningOn: mAwake=" + this.mAwake + ", mScreenOnEarly=" + this.mScreenOnEarly + ", mScreenOnFully=" + this.mScreenOnFully + ", mKeyguardDrawComplete=" + this.mKeyguardDrawComplete + ", mWindowManagerDrawComplete=" + this.mWindowManagerDrawComplete);
            }
            if (!this.mScreenOnFully && this.mScreenOnEarly && this.mWindowManagerDrawComplete) {
                if (!this.mAwake || this.mKeyguardDrawComplete) {
                    if (WindowManagerDebugConfig.DEBUG_SCREEN_ON) {
                        Slog.i("WindowManager", "Finished screen turning on...");
                    }
                    this.mScreenOnListener = null;
                    this.mScreenOnFully = true;
                    return true;
                }
            }
            return false;
        }
    }

    private boolean hasStatusBarServicePermission(int pid, int uid) {
        return this.mContext.checkPermission("android.permission.STATUS_BAR_SERVICE", pid, uid) == 0;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0044, code lost:
        if (r2 != 2006) goto L_0x00b0;
     */
    public void adjustWindowParamsLw(WindowState win, WindowManager.LayoutParams attrs, int callingPid, int callingUid) {
        boolean isScreenDecor = (attrs.privateFlags & DumpState.DUMP_CHANGES) != 0;
        if (this.mScreenDecorWindows.contains(win)) {
            if (!isScreenDecor) {
                this.mScreenDecorWindows.remove(win);
            }
        } else if (isScreenDecor && hasStatusBarServicePermission(callingPid, callingUid)) {
            this.mScreenDecorWindows.add(win);
        }
        int i = attrs.type;
        if (i != 2000) {
            if (i != 2013) {
                if (i != 2015) {
                    if (i != 2023) {
                        if (i == 2036) {
                            attrs.flags |= 8;
                        } else if (i == 2005) {
                            if (attrs.hideTimeoutMilliseconds < 0 || attrs.hideTimeoutMilliseconds > 3500) {
                                attrs.hideTimeoutMilliseconds = 3500;
                            }
                            attrs.hideTimeoutMilliseconds = (long) this.mAccessibilityManager.getRecommendedTimeoutMillis((int) attrs.hideTimeoutMilliseconds, 2);
                            attrs.windowAnimations = 16973828;
                            if (canToastShowWhenLocked(callingPid)) {
                                attrs.flags |= DumpState.DUMP_FROZEN;
                            }
                            attrs.flags |= 16;
                        }
                    }
                }
                attrs.flags |= 24;
                attrs.flags &= -262145;
            }
            attrs.layoutInDisplayCutoutMode = 1;
        } else if (this.mService.mPolicy.isKeyguardOccluded()) {
            attrs.flags &= -1048577;
            attrs.privateFlags &= -1025;
        }
        if (attrs.type != 2000) {
            attrs.privateFlags &= -1025;
        }
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: com.android.server.wm.DisplayContent.forAllWindows(com.android.internal.util.ToBooleanFunction<com.android.server.wm.WindowState>, boolean):boolean
     arg types: [com.android.server.wm.-$$Lambda$DisplayPolicy$pqtzqy0ti-csynvTP9P1eQUE-gE, int]
     candidates:
      com.android.server.wm.WindowContainer.forAllWindows(java.util.function.Consumer<com.android.server.wm.WindowState>, boolean):void
      com.android.server.wm.DisplayContent.forAllWindows(com.android.internal.util.ToBooleanFunction<com.android.server.wm.WindowState>, boolean):boolean */
    /* access modifiers changed from: package-private */
    public boolean canToastShowWhenLocked(int callingPid) {
        return this.mDisplayContent.forAllWindows((ToBooleanFunction<WindowState>) new ToBooleanFunction(callingPid) {
            /* class com.android.server.wm.$$Lambda$DisplayPolicy$pqtzqy0ticsynvTP9P1eQUEgE */
            private final /* synthetic */ int f$0;

            {
                this.f$0 = r1;
            }

            public final boolean apply(Object obj) {
                return DisplayPolicy.lambda$canToastShowWhenLocked$2(this.f$0, (WindowState) obj);
            }
        }, true);
    }

    static /* synthetic */ boolean lambda$canToastShowWhenLocked$2(int callingPid, WindowState w) {
        return callingPid == w.mSession.mPid && w.isVisible() && w.canShowWhenLocked();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x002f, code lost:
        if (r0 != 2033) goto L_0x00e4;
     */
    public int prepareAddWindowLw(WindowState win, WindowManager.LayoutParams attrs) {
        if ((attrs.privateFlags & DumpState.DUMP_CHANGES) != 0) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.STATUS_BAR_SERVICE", "DisplayPolicy");
            this.mScreenDecorWindows.add(win);
        }
        int i = attrs.type;
        if (i != 2000) {
            if (!(i == 2014 || i == 2017)) {
                if (i == 2019) {
                    this.mContext.enforceCallingOrSelfPermission("android.permission.STATUS_BAR_SERVICE", "DisplayPolicy");
                    WindowState windowState = this.mNavigationBar;
                    if (windowState != null && windowState.isAlive()) {
                        return -7;
                    }
                    this.mNavigationBar = win;
                    this.mNavigationBarController.setWindow(win);
                    this.mNavigationBarController.setOnBarVisibilityChangedListener(this.mNavBarVisibilityListener, true);
                    this.mDisplayContent.setInsetProvider(1, win, null);
                    this.mDisplayContent.setInsetProvider(5, win, new TriConsumer() {
                        /* class com.android.server.wm.$$Lambda$DisplayPolicy$52bg3qYmo5Unt8Q07j9d6hFQG2o */

                        public final void accept(Object obj, Object obj2, Object obj3) {
                            DisplayPolicy.this.lambda$prepareAddWindowLw$4$DisplayPolicy((DisplayFrames) obj, (WindowState) obj2, (Rect) obj3);
                        }
                    });
                    this.mDisplayContent.setInsetProvider(6, win, new TriConsumer() {
                        /* class com.android.server.wm.$$Lambda$DisplayPolicy$XeqRJzc7ac4NU1zAF74Hsb20Oyg */

                        public final void accept(Object obj, Object obj2, Object obj3) {
                            DisplayPolicy.this.lambda$prepareAddWindowLw$5$DisplayPolicy((DisplayFrames) obj, (WindowState) obj2, (Rect) obj3);
                        }
                    });
                    this.mDisplayContent.setInsetProvider(7, win, new TriConsumer() {
                        /* class com.android.server.wm.$$Lambda$DisplayPolicy$2VfPB7jRHi3x9grU1pG8ihi_Ga4 */

                        public final void accept(Object obj, Object obj2, Object obj3) {
                            DisplayPolicy.this.lambda$prepareAddWindowLw$6$DisplayPolicy((DisplayFrames) obj, (WindowState) obj2, (Rect) obj3);
                        }
                    });
                    this.mDisplayContent.setInsetProvider(9, win, new TriConsumer() {
                        /* class com.android.server.wm.$$Lambda$DisplayPolicy$LmU9vcWscAr5f4KqPLDYJTaZBVU */

                        public final void accept(Object obj, Object obj2, Object obj3) {
                            DisplayPolicy.this.lambda$prepareAddWindowLw$7$DisplayPolicy((DisplayFrames) obj, (WindowState) obj2, (Rect) obj3);
                        }
                    });
                    if (WindowManagerDebugConfig.DEBUG_LAYOUT) {
                        Slog.i("WindowManager", "NAVIGATION BAR: " + this.mNavigationBar);
                    }
                } else if (i != 2024) {
                }
            }
            this.mContext.enforceCallingOrSelfPermission("android.permission.STATUS_BAR_SERVICE", "DisplayPolicy");
        } else {
            this.mContext.enforceCallingOrSelfPermission("android.permission.STATUS_BAR_SERVICE", "DisplayPolicy");
            WindowState windowState2 = this.mStatusBar;
            if (windowState2 != null && windowState2.isAlive()) {
                return -7;
            }
            this.mStatusBar = win;
            this.mStatusBarController.setWindow(win);
            if (this.mDisplayContent.isDefaultDisplay) {
                this.mService.mPolicy.setKeyguardCandidateLw(win);
            }
            TriConsumer<DisplayFrames, WindowState, Rect> frameProvider = new TriConsumer() {
                /* class com.android.server.wm.$$Lambda$DisplayPolicy$sDsfACJdM5Dc_VvZ4b6PthimRJY */

                public final void accept(Object obj, Object obj2, Object obj3) {
                    DisplayPolicy.this.lambda$prepareAddWindowLw$3$DisplayPolicy((DisplayFrames) obj, (WindowState) obj2, (Rect) obj3);
                }
            };
            this.mDisplayContent.setInsetProvider(0, win, frameProvider);
            this.mDisplayContent.setInsetProvider(4, win, frameProvider);
            this.mDisplayContent.setInsetProvider(8, win, frameProvider);
        }
        return 0;
    }

    public /* synthetic */ void lambda$prepareAddWindowLw$3$DisplayPolicy(DisplayFrames displayFrames, WindowState windowState, Rect rect) {
        rect.top = 0;
        rect.bottom = getStatusBarHeight(displayFrames);
    }

    public /* synthetic */ void lambda$prepareAddWindowLw$4$DisplayPolicy(DisplayFrames displayFrames, WindowState windowState, Rect inOutFrame) {
        inOutFrame.top -= this.mBottomGestureAdditionalInset;
    }

    public /* synthetic */ void lambda$prepareAddWindowLw$5$DisplayPolicy(DisplayFrames displayFrames, WindowState windowState, Rect inOutFrame) {
        inOutFrame.left = 0;
        inOutFrame.top = 0;
        inOutFrame.bottom = displayFrames.mDisplayHeight;
        inOutFrame.right = displayFrames.mUnrestricted.left + this.mSideGestureInset;
    }

    public /* synthetic */ void lambda$prepareAddWindowLw$6$DisplayPolicy(DisplayFrames displayFrames, WindowState windowState, Rect inOutFrame) {
        inOutFrame.left = displayFrames.mUnrestricted.right - this.mSideGestureInset;
        inOutFrame.top = 0;
        inOutFrame.bottom = displayFrames.mDisplayHeight;
        inOutFrame.right = displayFrames.mDisplayWidth;
    }

    public /* synthetic */ void lambda$prepareAddWindowLw$7$DisplayPolicy(DisplayFrames displayFrames, WindowState windowState, Rect inOutFrame) {
        if ((windowState.getAttrs().flags & 16) != 0 || this.mNavigationBarLetsThroughTaps) {
            inOutFrame.setEmpty();
        }
    }

    public void removeWindowLw(WindowState win) {
        if (this.mStatusBar == win) {
            this.mStatusBar = null;
            this.mStatusBarController.setWindow(null);
            if (this.mDisplayContent.isDefaultDisplay) {
                this.mService.mPolicy.setKeyguardCandidateLw(null);
            }
            this.mDisplayContent.setInsetProvider(0, null, null);
        } else if (this.mNavigationBar == win) {
            this.mNavigationBar = null;
            this.mNavigationBarController.setWindow(null);
            this.mDisplayContent.setInsetProvider(1, null, null);
        }
        if (this.mLastFocusedWindow == win) {
            this.mLastFocusedWindow = null;
        }
        this.mScreenDecorWindows.remove(win);
    }

    private int getStatusBarHeight(DisplayFrames displayFrames) {
        return Math.max(this.mStatusBarHeightForRotation[displayFrames.mRotation], displayFrames.mDisplayCutoutSafe.top);
    }

    public int selectAnimationLw(WindowState win, int transit) {
        if (WindowManagerDebugConfig.DEBUG_ANIM) {
            Slog.i("WindowManager", "selectAnimation in " + win + ": transit=" + transit);
        }
        if (win == this.mStatusBar) {
            boolean isKeyguard = (win.getAttrs().privateFlags & 1024) != 0;
            boolean expanded = win.getAttrs().height == -1 && win.getAttrs().width == -1;
            if (isKeyguard || expanded) {
                return -1;
            }
            if (transit == 2 || transit == 4) {
                return 17432757;
            }
            if (transit == 1 || transit == 3) {
                return 17432756;
            }
        } else if (win == this.mNavigationBar) {
            if (win.getAttrs().windowAnimations != 0) {
                return 0;
            }
            this.mColorDpEx.updateNavigationBarHideState();
            if (this.mColorDpEx.isNavBarHidden() && this.mColorDpEx.isNavGestureMode()) {
                return 0;
            }
            int i = this.mNavigationBarPosition;
            if (i == 4) {
                if (transit == 2 || transit == 4) {
                    if (this.mService.mPolicy.isKeyguardShowingAndNotOccluded()) {
                        return 0;
                    }
                    return 17432750;
                } else if (transit == 1 || transit == 3) {
                    return 17432749;
                }
            } else if (i == 2) {
                if (transit == 2 || transit == 4) {
                    return 17432755;
                }
                if (transit == 1 || transit == 3) {
                    return 17432754;
                }
            } else if (i == 1) {
                if (transit == 2 || transit == 4) {
                    return 17432753;
                }
                if (transit == 1 || transit == 3) {
                    return 17432752;
                }
            }
        } else if (win.getAttrs().type == 2034) {
            return selectDockedDividerAnimationLw(win, transit);
        }
        if (transit != 5) {
            return (win.getAttrs().type == 2023 && this.mDreamingLockscreen && transit == 1) ? -1 : 0;
        }
        if (win.hasAppShownWindows()) {
            if (!WindowManagerDebugConfig.DEBUG_ANIM) {
                return 17432730;
            }
            Slog.i("WindowManager", "**** STARTING EXIT");
            return 17432730;
        }
    }

    private int selectDockedDividerAnimationLw(WindowState win, int transit) {
        int insets = this.mDisplayContent.getDockedDividerController().getContentInsets();
        Rect frame = win.getFrameLw();
        boolean behindNavBar = this.mNavigationBar != null && ((this.mNavigationBarPosition == 4 && frame.top + insets >= this.mNavigationBar.getFrameLw().top) || ((this.mNavigationBarPosition == 2 && frame.left + insets >= this.mNavigationBar.getFrameLw().left) || (this.mNavigationBarPosition == 1 && frame.right - insets <= this.mNavigationBar.getFrameLw().right)));
        boolean landscape = frame.height() > frame.width();
        boolean offscreen = (landscape && (frame.right - insets <= 0 || frame.left + insets >= win.getDisplayFrameLw().right)) || (!landscape && (frame.top - insets <= 0 || frame.bottom + insets >= win.getDisplayFrameLw().bottom));
        if (behindNavBar || offscreen) {
            return 0;
        }
        if (transit == 1 || transit == 3) {
            return 17432576;
        }
        return transit == 2 ? 0 : 0;
    }

    public void selectRotationAnimationLw(int[] anim) {
        boolean forceJumpcut = !this.mScreenOnFully || !this.mService.mPolicy.okToAnimate();
        if (WindowManagerDebugConfig.DEBUG_ANIM) {
            StringBuilder sb = new StringBuilder();
            sb.append("selectRotationAnimation mTopFullscreen=");
            sb.append(this.mTopFullscreenOpaqueWindowState);
            sb.append(" rotationAnimation=");
            WindowState windowState = this.mTopFullscreenOpaqueWindowState;
            sb.append(windowState == null ? "0" : Integer.valueOf(windowState.getAttrs().rotationAnimation));
            sb.append(" forceJumpcut=");
            sb.append(forceJumpcut);
            Slog.i("WindowManager", sb.toString());
        }
        if (forceJumpcut) {
            anim[0] = 17432847;
            anim[1] = 17432846;
            return;
        }
        WindowState windowState2 = this.mTopFullscreenOpaqueWindowState;
        if (windowState2 != null) {
            int animationHint = windowState2.getRotationAnimationHint();
            if (animationHint < 0 && this.mTopIsFullscreen) {
                animationHint = this.mTopFullscreenOpaqueWindowState.getAttrs().rotationAnimation;
            }
            if (animationHint != 1) {
                if (animationHint == 2) {
                    anim[0] = 17432847;
                    anim[1] = 17432846;
                    return;
                } else if (animationHint != 3) {
                    anim[1] = 0;
                    anim[0] = 0;
                    return;
                }
            }
            anim[0] = 17432848;
            anim[1] = 17432846;
            return;
        }
        anim[1] = 0;
        anim[0] = 0;
    }

    public boolean validateRotationAnimationLw(int exitAnimId, int enterAnimId, boolean forceDefault) {
        switch (exitAnimId) {
            case 17432847:
            case 17432848:
                if (forceDefault) {
                    return false;
                }
                int[] anim = new int[2];
                selectRotationAnimationLw(anim);
                if (exitAnimId == anim[0] && enterAnimId == anim[1]) {
                    return true;
                }
                return false;
            default:
                return true;
        }
    }

    public int adjustSystemUiVisibilityLw(int visibility) {
        this.mStatusBarController.adjustSystemUiVisibilityLw(this.mLastSystemUiFlags, visibility);
        this.mNavigationBarController.adjustSystemUiVisibilityLw(this.mLastSystemUiFlags, visibility);
        this.mResettingSystemUiFlags &= visibility;
        return (~this.mResettingSystemUiFlags) & visibility & (~this.mForceClearedSystemUiFlags);
    }

    public boolean areSystemBarsForcedShownLw(WindowState windowState) {
        return this.mForceShowSystemBars;
    }

    public boolean getLayoutHintLw(WindowManager.LayoutParams attrs, Rect taskBounds, DisplayFrames displayFrames, boolean floatingStack, Rect outFrame, Rect outContentInsets, Rect outStableInsets, Rect outOutsets, DisplayCutout.ParcelableWrapper outDisplayCutout) {
        Rect sf;
        Rect cf;
        int outset;
        int fl = PolicyControl.getWindowFlags(null, attrs);
        int pfl = attrs.privateFlags;
        int sysUiVis = getImpliedSysUiFlagsForLayout(attrs) | PolicyControl.getSystemUiVisibility(null, attrs);
        int displayRotation = displayFrames.mRotation;
        if ((outOutsets != null && shouldUseOutsets(attrs, fl)) && (outset = this.mWindowOutsetBottom) > 0) {
            if (displayRotation == 0) {
                outOutsets.bottom += outset;
            } else if (displayRotation == 1) {
                outOutsets.right += outset;
            } else if (displayRotation == 2) {
                outOutsets.top += outset;
            } else if (displayRotation == 3) {
                outOutsets.left += outset;
            }
        }
        boolean layoutInScreen = (fl & 256) != 0;
        boolean layoutInScreenAndInsetDecor = layoutInScreen && (65536 & fl) != 0;
        boolean screenDecor = (pfl & DumpState.DUMP_CHANGES) != 0;
        if (!layoutInScreenAndInsetDecor || screenDecor) {
            if (layoutInScreen) {
                outFrame.set(displayFrames.mUnrestricted);
            } else {
                outFrame.set(displayFrames.mStable);
            }
            if (taskBounds != null) {
                outFrame.intersect(taskBounds);
            }
            outContentInsets.setEmpty();
            outStableInsets.setEmpty();
            outDisplayCutout.set(DisplayCutout.NO_CUTOUT);
            return this.mForceShowSystemBars && !this.mColorDpEx.isNavBarImmersive();
        }
        if ((sysUiVis & 512) != 0) {
            outFrame.set(displayFrames.mUnrestricted);
        } else {
            outFrame.set(displayFrames.mRestricted);
        }
        if (floatingStack) {
            sf = null;
        } else {
            sf = displayFrames.mStable;
        }
        if (floatingStack) {
            cf = null;
        } else if ((sysUiVis & 256) != 0) {
            if ((fl & 1024) != 0) {
                cf = displayFrames.mStableFullscreen;
            } else {
                cf = displayFrames.mStable;
            }
        } else if ((fl & 1024) == 0 && (33554432 & fl) == 0) {
            cf = displayFrames.mCurrent;
        } else {
            cf = displayFrames.mOverscan;
        }
        if (taskBounds != null) {
            outFrame.intersect(taskBounds);
        }
        InsetUtils.insetsBetweenFrames(outFrame, cf, outContentInsets);
        InsetUtils.insetsBetweenFrames(outFrame, sf, outStableInsets);
        outDisplayCutout.set(displayFrames.mDisplayCutout.calculateRelativeTo(outFrame).getDisplayCutout());
        return this.mForceShowSystemBars && !this.mColorDpEx.isNavBarImmersive();
    }

    private static int getImpliedSysUiFlagsForLayout(WindowManager.LayoutParams attrs) {
        boolean forceWindowDrawsBarBackgrounds = (attrs.privateFlags & DumpState.DUMP_INTENT_FILTER_VERIFIERS) != 0 && attrs.height == -1 && attrs.width == -1;
        if ((attrs.flags & Integer.MIN_VALUE) != 0 || forceWindowDrawsBarBackgrounds) {
            return 0 | 512 | 1024;
        }
        return 0;
    }

    private static boolean shouldUseOutsets(WindowManager.LayoutParams attrs, int fl) {
        return attrs.type == 2013 || (33555456 & fl) != 0;
    }

    private final class HideNavInputEventReceiver extends InputEventReceiver {
        HideNavInputEventReceiver(InputChannel inputChannel, Looper looper) {
            super(inputChannel, looper);
        }

        public void onInputEvent(InputEvent event) {
            try {
                if ((event instanceof MotionEvent) && (event.getSource() & 2) != 0 && ((MotionEvent) event).getAction() == 0) {
                    boolean changed = false;
                    synchronized (DisplayPolicy.this.mLock) {
                        if (DisplayPolicy.this.mInputConsumer != null) {
                            int newVal = DisplayPolicy.this.mResettingSystemUiFlags | 2 | 1 | 4;
                            if (DisplayPolicy.this.mResettingSystemUiFlags != newVal) {
                                int unused = DisplayPolicy.this.mResettingSystemUiFlags = newVal;
                                changed = true;
                            }
                            int newVal2 = DisplayPolicy.this.mForceClearedSystemUiFlags | 2;
                            if (DisplayPolicy.this.mForceClearedSystemUiFlags != newVal2) {
                                int unused2 = DisplayPolicy.this.mForceClearedSystemUiFlags = newVal2;
                                changed = true;
                                DisplayPolicy.this.mHandler.postDelayed(DisplayPolicy.this.mClearHideNavigationFlag, 1000);
                            }
                            if (changed) {
                                DisplayPolicy.this.mDisplayContent.reevaluateStatusBarVisibility();
                            }
                        } else {
                            return;
                        }
                    }
                }
                finishInputEvent(event, false);
            } finally {
                finishInputEvent(event, false);
            }
        }
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: com.android.server.wm.WindowManagerService.createInputConsumer(android.os.Looper, java.lang.String, android.view.InputEventReceiver$Factory, int):com.android.server.policy.WindowManagerPolicy$InputConsumer
     arg types: [android.os.Looper, java.lang.String, com.android.server.wm.-$$Lambda$DisplayPolicy$FpQuLkFb2EnHvk4Uzhr9G5Rn_xI, int]
     candidates:
      com.android.server.wm.WindowManagerService.createInputConsumer(android.os.IBinder, java.lang.String, int, android.view.InputChannel):void
      com.android.server.wm.WindowManagerService.createInputConsumer(android.os.Looper, java.lang.String, android.view.InputEventReceiver$Factory, int):com.android.server.policy.WindowManagerPolicy$InputConsumer */
    public void beginLayoutLw(DisplayFrames displayFrames, int uiMode) {
        WindowState windowState;
        displayFrames.onBeginLayout();
        this.mSystemGestures.screenWidth = displayFrames.mUnrestricted.width();
        this.mSystemGestures.screenHeight = displayFrames.mUnrestricted.height();
        int sysui = getSystemUIFlagAfterGesture(this.mLastSystemUiFlags);
        boolean navVisible = (sysui & 2) == 0;
        boolean navTranslucent = (-2147450880 & sysui) != 0;
        boolean immersive = (sysui & 2048) != 0;
        boolean immersiveSticky = (sysui & 4096) != 0;
        boolean navAllowedHidden = immersive || immersiveSticky;
        boolean navTranslucent2 = navTranslucent & (!immersiveSticky);
        boolean isKeyguardShowing = isStatusBarKeyguard() && !this.mService.mPolicy.isKeyguardOccluded();
        boolean statusBarForcesShowingNavigation = (isKeyguardShowing || (windowState = this.mStatusBar) == null || (windowState.getAttrs().privateFlags & DumpState.DUMP_VOLUMES) == 0) ? false : true;
        if (navVisible || navAllowedHidden) {
            WindowManagerPolicy.InputConsumer inputConsumer = this.mInputConsumer;
            if (inputConsumer != null) {
                Handler handler = this.mHandler;
                handler.sendMessage(handler.obtainMessage(3, inputConsumer));
                this.mInputConsumer = null;
            }
        } else if (this.mInputConsumer == null && this.mStatusBar != null && canHideNavigationBar()) {
            this.mInputConsumer = this.mService.createInputConsumer(this.mHandler.getLooper(), "nav_input_consumer", (InputEventReceiver.Factory) new InputEventReceiver.Factory() {
                /* class com.android.server.wm.$$Lambda$DisplayPolicy$FpQuLkFb2EnHvk4Uzhr9G5Rn_xI */

                public final InputEventReceiver createInputEventReceiver(InputChannel inputChannel, Looper looper) {
                    return DisplayPolicy.this.lambda$beginLayoutLw$8$DisplayPolicy(inputChannel, looper);
                }
            }, displayFrames.mDisplayId);
            InputManager.getInstance().setPointerIconType(0);
        }
        boolean updateSysUiVisibility = layoutNavigationBar(displayFrames, uiMode, navVisible | (!canHideNavigationBar()), navTranslucent2, navAllowedHidden, statusBarForcesShowingNavigation);
        if (WindowManagerDebugConfig.DEBUG_LAYOUT) {
            Slog.i("WindowManager", "mDock rect:" + displayFrames.mDock);
        }
        if (updateSysUiVisibility || layoutStatusBar(displayFrames, sysui, isKeyguardShowing)) {
            updateSystemUiVisibilityLw();
        }
        layoutScreenDecorWindows(displayFrames);
        if (displayFrames.mDisplayCutoutSafe.top > displayFrames.mUnrestricted.top) {
            displayFrames.mDisplayCutoutSafe.top = Math.max(displayFrames.mDisplayCutoutSafe.top, displayFrames.mStable.top);
        }
        displayFrames.mCurrent.inset(this.mForwardedInsets);
        displayFrames.mContent.inset(this.mForwardedInsets);
    }

    public /* synthetic */ InputEventReceiver lambda$beginLayoutLw$8$DisplayPolicy(InputChannel x$0, Looper x$1) {
        return new HideNavInputEventReceiver(x$0, x$1);
    }

    private void layoutScreenDecorWindows(DisplayFrames displayFrames) {
        DisplayPolicy displayPolicy = this;
        if (!displayPolicy.mScreenDecorWindows.isEmpty()) {
            sTmpRect.setEmpty();
            int displayId = displayFrames.mDisplayId;
            Rect dockFrame = displayFrames.mDock;
            int displayHeight = displayFrames.mDisplayHeight;
            int displayWidth = displayFrames.mDisplayWidth;
            int i = displayPolicy.mScreenDecorWindows.size() - 1;
            while (i >= 0) {
                WindowState w = displayPolicy.mScreenDecorWindows.valueAt(i);
                if (w.getDisplayId() == displayId && w.isVisibleLw()) {
                    w.getWindowFrames().setFrames(displayFrames.mUnrestricted, displayFrames.mUnrestricted, displayFrames.mUnrestricted, displayFrames.mUnrestricted, displayFrames.mUnrestricted, sTmpRect, displayFrames.mUnrestricted, displayFrames.mUnrestricted);
                    w.getWindowFrames().setDisplayCutout(displayFrames.mDisplayCutout);
                    w.computeFrameLw();
                    Rect frame = w.getFrameLw();
                    if (frame.left > 0 || frame.top > 0) {
                        if (frame.right < displayWidth || frame.bottom < displayHeight) {
                            Slog.w("WindowManager", "layoutScreenDecorWindows: Ignoring decor win=" + w + " not docked on one of the sides of the display. frame=" + frame + " displayWidth=" + displayWidth + " displayHeight=" + displayHeight);
                        } else if (frame.top <= 0) {
                            dockFrame.right = Math.min(frame.left, dockFrame.right);
                        } else if (frame.left <= 0) {
                            dockFrame.bottom = Math.min(frame.top, dockFrame.bottom);
                        } else {
                            Slog.w("WindowManager", "layoutScreenDecorWindows: Ignoring decor win=" + w + " not docked on right or bottom of display. frame=" + frame + " displayWidth=" + displayWidth + " displayHeight=" + displayHeight);
                        }
                    } else if (frame.bottom >= displayHeight) {
                        dockFrame.left = Math.max(frame.right, dockFrame.left);
                    } else if (frame.right >= displayWidth) {
                        dockFrame.top = Math.max(frame.bottom, dockFrame.top);
                    } else {
                        Slog.w("WindowManager", "layoutScreenDecorWindows: Ignoring decor win=" + w + " not docked on left or top of display. frame=" + frame + " displayWidth=" + displayWidth + " displayHeight=" + displayHeight);
                    }
                }
                i--;
                displayPolicy = this;
            }
            displayFrames.mRestricted.set(dockFrame);
            displayFrames.mCurrent.set(dockFrame);
            displayFrames.mVoiceContent.set(dockFrame);
            displayFrames.mSystem.set(dockFrame);
            displayFrames.mContent.set(dockFrame);
            displayFrames.mRestrictedOverscan.set(dockFrame);
        }
    }

    private boolean layoutStatusBar(DisplayFrames displayFrames, int sysui, boolean isKeyguardShowing) {
        if (this.mStatusBar == null) {
            return false;
        }
        sTmpRect.setEmpty();
        WindowFrames windowFrames = this.mStatusBar.getWindowFrames();
        windowFrames.setFrames(displayFrames.mUnrestricted, displayFrames.mUnrestricted, displayFrames.mStable, displayFrames.mStable, displayFrames.mStable, sTmpRect, displayFrames.mStable, displayFrames.mStable);
        windowFrames.setDisplayCutout(displayFrames.mDisplayCutout);
        this.mStatusBar.computeFrameLw();
        displayFrames.mStable.top = displayFrames.mUnrestricted.top + this.mStatusBarHeightForRotation[displayFrames.mRotation];
        displayFrames.mStable.top = Math.max(displayFrames.mStable.top, displayFrames.mDisplayCutoutSafe.top);
        sTmpRect.set(this.mStatusBar.getContentFrameLw());
        sTmpRect.intersect(displayFrames.mDisplayCutoutSafe);
        sTmpRect.top = this.mStatusBar.getContentFrameLw().top;
        sTmpRect.bottom = displayFrames.mStable.top;
        this.mStatusBarController.setContentFrame(sTmpRect);
        boolean statusBarTransient = (67108864 & sysui) != 0;
        boolean statusBarTranslucent = (1073741832 & sysui) != 0;
        if (this.mStatusBar.isVisibleLw() && !statusBarTransient) {
            Rect dockFrame = displayFrames.mDock;
            dockFrame.top = displayFrames.mStable.top;
            displayFrames.mContent.set(dockFrame);
            displayFrames.mVoiceContent.set(dockFrame);
            displayFrames.mCurrent.set(dockFrame);
            if (WindowManagerDebugConfig.DEBUG_LAYOUT) {
                Slog.v("WindowManager", "Status bar: " + String.format("dock=%s content=%s cur=%s", dockFrame.toString(), displayFrames.mContent.toString(), displayFrames.mCurrent.toString()));
            }
            if (!statusBarTranslucent && !this.mStatusBarController.wasRecentlyTranslucent() && !this.mStatusBar.isAnimatingLw()) {
                displayFrames.mSystem.top = displayFrames.mStable.top;
            }
        }
        return this.mStatusBarController.checkHiddenLw();
    }

    private boolean layoutNavigationBar(DisplayFrames displayFrames, int uiMode, boolean navVisible, boolean navTranslucent, boolean navAllowedHidden, boolean statusBarForcesShowingNavigation) {
        if (this.mNavigationBar == null) {
            return false;
        }
        Rect navigationFrame = sTmpNavFrame;
        boolean transientNavBarShowing = this.mNavigationBarController.isTransientShowing();
        int rotation = displayFrames.mRotation;
        int displayHeight = displayFrames.mDisplayHeight;
        int displayWidth = displayFrames.mDisplayWidth;
        Rect dockFrame = displayFrames.mDock;
        this.mColorDpEx.updateNavigationBarHideState();
        this.mNavigationBarPosition = navigationBarPosition(displayWidth, displayHeight, rotation);
        Rect cutoutSafeUnrestricted = sTmpRect;
        cutoutSafeUnrestricted.set(displayFrames.mUnrestricted);
        cutoutSafeUnrestricted.intersectUnchecked(displayFrames.mDisplayCutoutSafe);
        int i = this.mNavigationBarPosition;
        if (i == 4) {
            int top = this.mColorDpEx.caculateDisplayFrame(this.mNavigationBarPosition, cutoutSafeUnrestricted, cutoutSafeUnrestricted.bottom - getNavigationBarHeight(rotation, uiMode));
            navigationFrame.set(0, cutoutSafeUnrestricted.bottom - getNavigationBarFrameHeight(rotation, uiMode), displayWidth, displayFrames.mUnrestricted.bottom);
            this.mColorDpEx.updateNavigationFrame(this.mNavigationBarPosition, rotation, navigationFrame, cutoutSafeUnrestricted);
            Rect rect = displayFrames.mStable;
            displayFrames.mStableFullscreen.bottom = top;
            rect.bottom = top;
            if (transientNavBarShowing) {
                this.mNavigationBarController.setBarShowingLw(true);
            } else if (navVisible) {
                this.mNavigationBarController.setBarShowingLw(true);
                Rect rect2 = displayFrames.mRestricted;
                displayFrames.mRestrictedOverscan.bottom = top;
                rect2.bottom = top;
                dockFrame.bottom = top;
            } else {
                this.mNavigationBarController.setBarShowingLw(statusBarForcesShowingNavigation);
            }
            if (navVisible && !navTranslucent && !navAllowedHidden && !this.mNavigationBar.isAnimatingLw() && !this.mNavigationBarController.wasRecentlyTranslucent()) {
                displayFrames.mSystem.bottom = top;
            }
        } else if (i == 2) {
            int def = cutoutSafeUnrestricted.right - getNavigationBarWidth(rotation, uiMode);
            int left = this.mColorDpEx.caculateDisplayFrame(this.mNavigationBarPosition, cutoutSafeUnrestricted, def);
            navigationFrame.set(def, 0, displayFrames.mUnrestricted.right, displayHeight);
            this.mColorDpEx.updateNavigationFrame(this.mNavigationBarPosition, rotation, navigationFrame, cutoutSafeUnrestricted);
            Rect rect3 = displayFrames.mStable;
            displayFrames.mStableFullscreen.right = left;
            rect3.right = left;
            if (transientNavBarShowing) {
                this.mNavigationBarController.setBarShowingLw(true);
            } else if (navVisible) {
                this.mNavigationBarController.setBarShowingLw(true);
                Rect rect4 = displayFrames.mRestricted;
                displayFrames.mRestrictedOverscan.right = left;
                rect4.right = left;
                dockFrame.right = left;
            } else {
                this.mNavigationBarController.setBarShowingLw(statusBarForcesShowingNavigation);
            }
            if (navVisible && !navTranslucent && !navAllowedHidden && !this.mNavigationBar.isAnimatingLw() && !this.mNavigationBarController.wasRecentlyTranslucent()) {
                displayFrames.mSystem.right = left;
            }
        } else if (i == 1) {
            int def2 = cutoutSafeUnrestricted.left + getNavigationBarWidth(rotation, uiMode);
            int right = this.mColorDpEx.caculateDisplayFrame(this.mNavigationBarPosition, cutoutSafeUnrestricted, def2);
            navigationFrame.set(displayFrames.mUnrestricted.left, 0, def2, displayHeight);
            this.mColorDpEx.updateNavigationFrame(this.mNavigationBarPosition, rotation, navigationFrame, cutoutSafeUnrestricted);
            Rect rect5 = displayFrames.mStable;
            displayFrames.mStableFullscreen.left = right;
            rect5.left = right;
            if (transientNavBarShowing) {
                this.mNavigationBarController.setBarShowingLw(true);
            } else if (navVisible) {
                this.mNavigationBarController.setBarShowingLw(true);
                Rect rect6 = displayFrames.mRestricted;
                displayFrames.mRestrictedOverscan.left = right;
                rect6.left = right;
                dockFrame.left = right;
            } else {
                this.mNavigationBarController.setBarShowingLw(statusBarForcesShowingNavigation);
            }
            if (navVisible && !navTranslucent && !navAllowedHidden && !this.mNavigationBar.isAnimatingLw() && !this.mNavigationBarController.wasRecentlyTranslucent()) {
                displayFrames.mSystem.left = right;
            }
        }
        displayFrames.mCurrent.set(dockFrame);
        displayFrames.mVoiceContent.set(dockFrame);
        displayFrames.mContent.set(dockFrame);
        sTmpRect.setEmpty();
        this.mNavigationBar.getWindowFrames().setFrames(navigationFrame, navigationFrame, navigationFrame, displayFrames.mDisplayCutoutSafe, navigationFrame, sTmpRect, navigationFrame, displayFrames.mDisplayCutoutSafe);
        this.mNavigationBar.getWindowFrames().setDisplayCutout(displayFrames.mDisplayCutout);
        this.mNavigationBar.computeFrameLw();
        this.mNavigationBarController.setContentFrame(this.mNavigationBar.getContentFrameLw());
        if (WindowManagerDebugConfig.DEBUG_LAYOUT) {
            Slog.i("WindowManager", "mNavigationBar frame: " + navigationFrame);
        }
        return this.mNavigationBarController.checkHiddenLw();
    }

    private void setAttachedWindowFrames(WindowState win, int fl, int adjust, WindowState attached, boolean insetDecors, Rect pf, Rect df, Rect of, Rect cf, Rect vf, DisplayFrames displayFrames) {
        if (win.isInputMethodTarget() || !attached.isInputMethodTarget()) {
            Rect parentDisplayFrame = attached.getDisplayFrameLw();
            Rect parentOverscan = attached.getOverscanFrameLw();
            WindowManager.LayoutParams attachedAttrs = attached.mAttrs;
            if ((attachedAttrs.privateFlags & DumpState.DUMP_INTENT_FILTER_VERIFIERS) != 0 && (attachedAttrs.flags & Integer.MIN_VALUE) == 0 && (attachedAttrs.systemUiVisibility & 512) == 0) {
                parentOverscan = new Rect(parentOverscan);
                parentOverscan.intersect(displayFrames.mRestrictedOverscan);
                parentDisplayFrame = new Rect(parentDisplayFrame);
                parentDisplayFrame.intersect(displayFrames.mRestrictedOverscan);
            }
            if (adjust != 16) {
                cf.set((1073741824 & fl) != 0 ? attached.getContentFrameLw() : parentOverscan);
            } else {
                cf.set(attached.getContentFrameLw());
                if (attached.isVoiceInteraction()) {
                    cf.intersectUnchecked(displayFrames.mVoiceContent);
                } else if (win.isInputMethodTarget() || attached.isInputMethodTarget()) {
                    cf.intersectUnchecked(displayFrames.mContent);
                }
            }
            df.set(insetDecors ? parentDisplayFrame : cf);
            of.set(insetDecors ? parentOverscan : cf);
            vf.set(attached.getVisibleFrameLw());
        } else {
            vf.set(displayFrames.mDock);
            cf.set(displayFrames.mDock);
            of.set(displayFrames.mDock);
            df.set(displayFrames.mDock);
        }
        pf.set((fl & 256) == 0 ? attached.getFrameLw() : df);
    }

    private void applyStableConstraints(int sysui, int fl, Rect r, DisplayFrames displayFrames) {
        if ((sysui & 256) != 0) {
            if ((fl & 1024) != 0) {
                r.intersectUnchecked(displayFrames.mStableFullscreen);
            } else {
                r.intersectUnchecked(displayFrames.mStable);
            }
        }
    }

    private boolean canReceiveInput(WindowState win) {
        return !(((win.getAttrs().flags & 8) != 0) ^ ((win.getAttrs().flags & DumpState.DUMP_INTENT_FILTER_VERIFIERS) != 0));
    }

    /* JADX INFO: Multiple debug info for r6v1 android.graphics.Rect: [D('tempSysUiFl' int), D('vf' android.graphics.Rect)] */
    /* JADX INFO: Multiple debug info for r8v1 android.graphics.Rect: [D('pfl' int), D('sf' android.graphics.Rect)] */
    /* JADX WARNING: Removed duplicated region for block: B:173:0x0503  */
    /* JADX WARNING: Removed duplicated region for block: B:183:0x052e  */
    /* JADX WARNING: Removed duplicated region for block: B:186:0x053e  */
    /* JADX WARNING: Removed duplicated region for block: B:187:0x0558  */
    /* JADX WARNING: Removed duplicated region for block: B:298:0x08ea  */
    /* JADX WARNING: Removed duplicated region for block: B:299:0x08f0  */
    public void layoutWindowLw(WindowState win, WindowState attached, DisplayFrames displayFrames) {
        boolean layoutInsetDecor;
        WindowFrames windowFrames;
        Rect dcf;
        WindowManager.LayoutParams attrs;
        Rect sf;
        int sim;
        int i;
        String str;
        Rect df;
        Rect of;
        Rect cf;
        Rect sf2;
        int adjust;
        int type;
        int fl;
        int i2;
        int sysUiFl;
        int sysUiFl2;
        WindowManager.LayoutParams attrs2;
        WindowFrames windowFrames2;
        String str2;
        WindowFrames windowFrames3;
        WindowState windowState;
        int sysUiFl3;
        int sysUiFl4;
        int fl2;
        int sysUiFl5;
        int type2;
        int type3;
        int sysUiFl6;
        int fl3;
        int i3;
        int adjust2;
        Rect vf;
        int adjust3;
        int sysUiFl7;
        String str3;
        int fl4;
        int i4;
        Rect of2;
        Rect df2;
        Rect pf;
        int sysUiFl8;
        String str4;
        Rect cf2;
        int adjust4;
        int i5;
        int i6;
        Rect pf2;
        Rect of3;
        Rect df3;
        WindowState windowState2;
        if (win == this.mStatusBar && !canReceiveInput(win)) {
            return;
        }
        if (win == this.mNavigationBar) {
            return;
        }
        if (!this.mScreenDecorWindows.contains(win)) {
            WindowManager.LayoutParams attrs3 = win.getAttrs();
            boolean isDefaultDisplay = win.isDefaultDisplay();
            int type4 = attrs3.type;
            int fl5 = PolicyControl.getWindowFlags(win, attrs3);
            int pfl = attrs3.privateFlags;
            int sim2 = attrs3.softInputMode;
            int requestedSysUiFl = PolicyControl.getSystemUiVisibility(null, attrs3);
            int sysUiFl9 = this.mColorDpEx.getSysUiFlagsForSplitScreen(win, requestedSysUiFl | getImpliedSysUiFlagsForLayout(attrs3));
            WindowFrames windowFrames4 = win.getWindowFrames();
            windowFrames4.setHasOutsets(false);
            sTmpLastParentFrame.set(windowFrames4.mParentFrame);
            Rect pf3 = windowFrames4.mParentFrame;
            Rect df4 = windowFrames4.mDisplayFrame;
            Rect of4 = windowFrames4.mOverscanFrame;
            Rect cf3 = windowFrames4.mContentFrame;
            Rect vf2 = windowFrames4.mVisibleFrame;
            Rect dcf2 = windowFrames4.mDecorFrame;
            Rect sf3 = windowFrames4.mStableFrame;
            dcf2.setEmpty();
            windowFrames4.setParentFrameWasClippedByDisplayCutout(false);
            windowFrames4.setDisplayCutout(displayFrames.mDisplayCutout);
            boolean hasNavBar = hasNavigationBar() && (windowState2 = this.mNavigationBar) != null && windowState2.isVisibleLw();
            int adjust5 = sim2 & 240;
            boolean requestedFullscreen = ((fl5 & 1024) == 0 && (requestedSysUiFl & 4) == 0) ? false : true;
            boolean layoutInScreen = (fl5 & 256) == 256;
            boolean layoutInsetDecor2 = (65536 & fl5) == 65536;
            sf3.set(displayFrames.mStable);
            if (type4 == 2011) {
                vf2.set(displayFrames.mDock);
                cf3.set(displayFrames.mDock);
                of4.set(displayFrames.mDock);
                df4.set(displayFrames.mDock);
                windowFrames4.mParentFrame.set(displayFrames.mDock);
                int i7 = displayFrames.mUnrestricted.bottom;
                of4.bottom = i7;
                df4.bottom = i7;
                pf3.bottom = i7;
                int i8 = displayFrames.mStable.bottom;
                vf2.bottom = i8;
                cf3.bottom = i8;
                WindowState windowState3 = this.mStatusBar;
                if (windowState3 != null && this.mFocusedWindow == windowState3 && canReceiveInput(windowState3)) {
                    int i9 = this.mNavigationBarPosition;
                    if (i9 == 2) {
                        int i10 = displayFrames.mStable.right;
                        vf2.right = i10;
                        cf3.right = i10;
                        of4.right = i10;
                        df4.right = i10;
                        pf3.right = i10;
                    } else if (i9 == 1) {
                        int i11 = displayFrames.mStable.left;
                        vf2.left = i11;
                        cf3.left = i11;
                        of4.left = i11;
                        df4.left = i11;
                        pf3.left = i11;
                    }
                }
                if (this.mNavigationBarPosition == 4) {
                    int rotation = displayFrames.mRotation;
                    int uimode = this.mService.mPolicy.getUiMode();
                    int navHeightOffset = getNavigationBarFrameHeight(rotation, uimode) - getNavigationBarHeight(rotation, uimode);
                    if (navHeightOffset > 0) {
                        cf3.bottom -= navHeightOffset;
                        sf3.bottom -= navHeightOffset;
                        vf2.bottom -= navHeightOffset;
                        dcf2.bottom -= navHeightOffset;
                    }
                }
                attrs3.gravity = 80;
                adjust = adjust5;
                fl = fl5;
                type = type4;
                attrs = attrs3;
                layoutInsetDecor = layoutInsetDecor2;
                sim = sim2;
                windowFrames = windowFrames4;
                i = 0;
                of = of4;
                df = df4;
                cf = cf3;
                sf = sf3;
                i2 = 1;
                sf2 = pf3;
                sysUiFl = sysUiFl9;
                dcf = dcf2;
                str = "WindowManager";
            } else if (type4 == 2031) {
                of4.set(displayFrames.mUnrestricted);
                df4.set(displayFrames.mUnrestricted);
                pf3.set(displayFrames.mUnrestricted);
                if (adjust5 != 16) {
                    cf3.set(displayFrames.mDock);
                } else {
                    cf3.set(displayFrames.mContent);
                }
                if (adjust5 != 48) {
                    vf2.set(displayFrames.mCurrent);
                    adjust = adjust5;
                    fl = fl5;
                    type = type4;
                    attrs = attrs3;
                    layoutInsetDecor = layoutInsetDecor2;
                    sim = sim2;
                    windowFrames = windowFrames4;
                    i = 0;
                    of = of4;
                    df = df4;
                    cf = cf3;
                    sf = sf3;
                    i2 = 1;
                    sf2 = pf3;
                    sysUiFl = sysUiFl9;
                    dcf = dcf2;
                    str = "WindowManager";
                } else {
                    vf2.set(cf3);
                    adjust = adjust5;
                    fl = fl5;
                    type = type4;
                    attrs = attrs3;
                    layoutInsetDecor = layoutInsetDecor2;
                    sim = sim2;
                    windowFrames = windowFrames4;
                    i = 0;
                    of = of4;
                    df = df4;
                    cf = cf3;
                    sf = sf3;
                    i2 = 1;
                    sf2 = pf3;
                    sysUiFl = sysUiFl9;
                    dcf = dcf2;
                    str = "WindowManager";
                }
            } else if (type4 == 2013) {
                i = 0;
                windowFrames = windowFrames4;
                adjust = adjust5;
                layoutWallpaper(displayFrames, pf3, df4, of4, cf3);
                fl = fl5;
                type = type4;
                attrs = attrs3;
                layoutInsetDecor = layoutInsetDecor2;
                of = of4;
                sim = sim2;
                sf = sf3;
                cf = cf3;
                sysUiFl = sysUiFl9;
                i2 = 1;
                df = df4;
                sf2 = pf3;
                dcf = dcf2;
                str = "WindowManager";
            } else {
                windowFrames = windowFrames4;
                i = 0;
                adjust = adjust5;
                if (win == this.mStatusBar) {
                    of4.set(displayFrames.mUnrestricted);
                    df4.set(displayFrames.mUnrestricted);
                    pf3.set(displayFrames.mUnrestricted);
                    cf3.set(displayFrames.mStable);
                    vf2.set(displayFrames.mStable);
                    if (adjust == 16) {
                        cf3.bottom = displayFrames.mContent.bottom;
                        attrs = attrs3;
                        layoutInsetDecor = layoutInsetDecor2;
                        sim = sim2;
                        sf = sf3;
                        df = df4;
                        fl = fl5;
                        sf2 = pf3;
                        cf = cf3;
                        sysUiFl = sysUiFl9;
                        i2 = 1;
                        of = of4;
                        type = type4;
                        dcf = dcf2;
                        str = "WindowManager";
                    } else {
                        cf3.bottom = displayFrames.mDock.bottom;
                        vf2.bottom = displayFrames.mContent.bottom;
                        attrs = attrs3;
                        layoutInsetDecor = layoutInsetDecor2;
                        sim = sim2;
                        sf = sf3;
                        df = df4;
                        fl = fl5;
                        sf2 = pf3;
                        cf = cf3;
                        sysUiFl = sysUiFl9;
                        i2 = 1;
                        of = of4;
                        type = type4;
                        dcf = dcf2;
                        str = "WindowManager";
                    }
                } else if (type4 != 2023 || (fl5 & 1024) == 0) {
                    dcf2.set(displayFrames.mSystem);
                    boolean inheritTranslucentDecor = (attrs3.privateFlags & 512) != 0;
                    boolean isAppWindow = type4 >= 1 && type4 <= 99;
                    boolean topAtRest = win == this.mTopFullscreenOpaqueWindowState && !win.isAnimatingLw();
                    if (!isAppWindow || inheritTranslucentDecor || topAtRest) {
                        sysUiFl4 = sysUiFl9;
                    } else {
                        sysUiFl4 = sysUiFl9;
                        if ((sysUiFl4 & 4) == 0 && (fl5 & 1024) == 0 && (67108864 & fl5) == 0 && (fl5 & Integer.MIN_VALUE) == 0 && (pfl & DumpState.DUMP_INTENT_FILTER_VERIFIERS) == 0) {
                            dcf2.top = displayFrames.mStable.top;
                        }
                        if ((134217728 & fl5) == 0 && (sysUiFl4 & 2) == 0) {
                            if ((fl5 & Integer.MIN_VALUE) == 0 && (pfl & DumpState.DUMP_INTENT_FILTER_VERIFIERS) == 0) {
                                dcf2.bottom = displayFrames.mStable.bottom;
                                dcf2.right = displayFrames.mStable.right;
                            }
                        }
                    }
                    if (!layoutInScreen || !layoutInsetDecor2) {
                        attrs = attrs3;
                        layoutInsetDecor = layoutInsetDecor2;
                        sim = sim2;
                        sf = sf3;
                        dcf = dcf2;
                        if (layoutInScreen) {
                            cf = cf3;
                            str = "WindowManager";
                            sysUiFl5 = sysUiFl4;
                            fl2 = fl5;
                            type2 = type4;
                            df = df4;
                            of = of4;
                            adjust2 = adjust;
                            sf2 = pf3;
                            vf = vf2;
                        } else if ((sysUiFl4 & 1536) != 0) {
                            cf = cf3;
                            str = "WindowManager";
                            sysUiFl5 = sysUiFl4;
                            fl2 = fl5;
                            type2 = type4;
                            df = df4;
                            of = of4;
                            adjust2 = adjust;
                            sf2 = pf3;
                            vf = vf2;
                        } else if (attached != null) {
                            if (WindowManagerDebugConfig.DEBUG_LAYOUT) {
                                Slog.v("WindowManager", "layoutWindowLw(" + ((Object) attrs.getTitle()) + "): attached to " + attached);
                            }
                            str = "WindowManager";
                            cf = cf3;
                            setAttachedWindowFrames(win, fl5, adjust, attached, false, pf3, df4, of4, cf, vf2, displayFrames);
                            type = type4;
                            of = of4;
                            vf2 = vf2;
                            adjust = adjust;
                            df = df4;
                            sysUiFl = sysUiFl4;
                            sf2 = pf3;
                            fl = fl5;
                            i2 = 1;
                        } else {
                            str = "WindowManager";
                            if (WindowManagerDebugConfig.DEBUG_LAYOUT) {
                                Slog.v(str, "layoutWindowLw(" + ((Object) attrs.getTitle()) + "): normal window");
                            }
                            if (type4 == 2014) {
                                cf = cf3;
                                cf.set(displayFrames.mRestricted);
                                of = of4;
                                of.set(displayFrames.mRestricted);
                                df = df4;
                                df.set(displayFrames.mRestricted);
                                sf2 = pf3;
                                sf2.set(displayFrames.mRestricted);
                                type = type4;
                                vf2 = vf2;
                                adjust = adjust;
                                sysUiFl = sysUiFl4;
                                fl = fl5;
                                i2 = 1;
                            } else {
                                cf = cf3;
                                of = of4;
                                df = df4;
                                sf2 = pf3;
                                if (type4 == 2005) {
                                    vf2 = vf2;
                                    adjust3 = adjust;
                                } else if (type4 == 2003) {
                                    vf2 = vf2;
                                    adjust3 = adjust;
                                } else {
                                    sf2.set(displayFrames.mContent);
                                    if (win.isVoiceInteraction()) {
                                        cf.set(displayFrames.mVoiceContent);
                                        of.set(displayFrames.mVoiceContent);
                                        df.set(displayFrames.mVoiceContent);
                                        adjust = adjust;
                                    } else {
                                        adjust = adjust;
                                        if (adjust != 16) {
                                            cf.set(displayFrames.mDock);
                                            of.set(displayFrames.mDock);
                                            df.set(displayFrames.mDock);
                                        } else {
                                            cf.set(displayFrames.mContent);
                                            of.set(displayFrames.mContent);
                                            df.set(displayFrames.mContent);
                                        }
                                    }
                                    if (adjust != 48) {
                                        vf2 = vf2;
                                        vf2.set(displayFrames.mCurrent);
                                        type = type4;
                                        sysUiFl = sysUiFl4;
                                        fl = fl5;
                                        i2 = 1;
                                    } else {
                                        vf2 = vf2;
                                        vf2.set(cf);
                                        type = type4;
                                        sysUiFl = sysUiFl4;
                                        fl = fl5;
                                        i2 = 1;
                                    }
                                }
                                cf.set(displayFrames.mStable);
                                of.set(displayFrames.mStable);
                                df.set(displayFrames.mStable);
                                sf2.set(displayFrames.mStable);
                                type = type4;
                                sysUiFl = sysUiFl4;
                                fl = fl5;
                                i2 = 1;
                            }
                        }
                        if (WindowManagerDebugConfig.DEBUG_LAYOUT) {
                            Slog.v(str, "layoutWindowLw(" + ((Object) attrs.getTitle()) + "): IN_SCREEN");
                        }
                        if (type2 == 2014) {
                            type3 = type2;
                            sysUiFl6 = sysUiFl5;
                            fl3 = fl2;
                            i3 = 1;
                        } else if (type2 == 2017) {
                            type3 = type2;
                            sysUiFl6 = sysUiFl5;
                            fl3 = fl2;
                            i3 = 1;
                        } else {
                            if (type2 == 2019) {
                                type = type2;
                                sysUiFl = sysUiFl5;
                                fl = fl2;
                                i2 = 1;
                            } else if (type2 == 2024) {
                                type = type2;
                                sysUiFl = sysUiFl5;
                                fl = fl2;
                                i2 = 1;
                            } else {
                                type = type2;
                                if (this.mColorDpEx.isWindowTitleEquals(win, "BreenoSmartPanel")) {
                                    cf.set(displayFrames.mOverscan);
                                    of.set(displayFrames.mOverscan);
                                    df.set(displayFrames.mOverscan);
                                    sf2.set(displayFrames.mOverscan);
                                    sysUiFl = sysUiFl5;
                                    fl = fl2;
                                    i2 = 1;
                                } else {
                                    if (type == 2015 || type == 2036 || type == 2314 || type == 2315) {
                                        fl = fl2;
                                        if ((fl & 1024) != 0) {
                                            cf.set(displayFrames.mOverscan);
                                            of.set(displayFrames.mOverscan);
                                            df.set(displayFrames.mOverscan);
                                            sf2.set(displayFrames.mOverscan);
                                            sysUiFl = sysUiFl5;
                                            i2 = 1;
                                        }
                                    } else {
                                        fl = fl2;
                                    }
                                    if (type == 2021) {
                                        cf.set(displayFrames.mOverscan);
                                        of.set(displayFrames.mOverscan);
                                        df.set(displayFrames.mOverscan);
                                        sf2.set(displayFrames.mOverscan);
                                        sysUiFl = sysUiFl5;
                                        i2 = 1;
                                    } else {
                                        if ((33554432 & fl) != 0) {
                                            i2 = 1;
                                            if (type >= 1 && type <= 1999) {
                                                cf.set(displayFrames.mOverscan);
                                                of.set(displayFrames.mOverscan);
                                                df.set(displayFrames.mOverscan);
                                                sf2.set(displayFrames.mOverscan);
                                                sysUiFl = sysUiFl5;
                                            }
                                        } else {
                                            i2 = 1;
                                        }
                                        sysUiFl = sysUiFl5;
                                        if ((sysUiFl & 512) != 0 && (type == 2000 || type == 2005 || type == 2034 || type == 2033 || (type >= i2 && type <= 1999))) {
                                            cf.set(displayFrames.mUnrestricted);
                                            of.set(displayFrames.mUnrestricted);
                                            df.set(displayFrames.mUnrestricted);
                                            sf2.set(displayFrames.mUnrestricted);
                                        } else if ((sysUiFl & 1024) != 0) {
                                            of.set(displayFrames.mRestricted);
                                            df.set(displayFrames.mRestricted);
                                            sf2.set(displayFrames.mRestricted);
                                            if (ViewRootImpl.sNewInsetsMode == 0 && adjust == 16) {
                                                cf.set(displayFrames.mContent);
                                            } else {
                                                cf.set(displayFrames.mDock);
                                            }
                                        } else {
                                            cf.set(displayFrames.mRestricted);
                                            of.set(displayFrames.mRestricted);
                                            df.set(displayFrames.mRestricted);
                                            sf2.set(displayFrames.mRestricted);
                                        }
                                    }
                                }
                                applyStableConstraints(sysUiFl, fl, cf, displayFrames);
                                if (adjust != 48) {
                                    vf2.set(displayFrames.mCurrent);
                                } else {
                                    vf2.set(cf);
                                }
                            }
                            of.set(displayFrames.mUnrestricted);
                            df.set(displayFrames.mUnrestricted);
                            sf2.set(displayFrames.mUnrestricted);
                            if (WindowManagerDebugConfig.DEBUG_LAYOUT) {
                                Slog.v(str, "Laying out navigation bar window: " + sf2);
                            }
                            applyStableConstraints(sysUiFl, fl, cf, displayFrames);
                            if (adjust != 48) {
                            }
                        }
                        cf.set(displayFrames.mUnrestricted);
                        of.set(displayFrames.mUnrestricted);
                        df.set(displayFrames.mUnrestricted);
                        sf2.set(displayFrames.mUnrestricted);
                        if (hasNavBar) {
                            int i12 = displayFrames.mDock.left;
                            cf.left = i12;
                            of.left = i12;
                            df.left = i12;
                            sf2.left = i12;
                            int i13 = displayFrames.mRestricted.right;
                            cf.right = i13;
                            of.right = i13;
                            df.right = i13;
                            sf2.right = i13;
                            int i14 = displayFrames.mRestricted.bottom;
                            cf.bottom = i14;
                            of.bottom = i14;
                            df.bottom = i14;
                            sf2.bottom = i14;
                        }
                        if (WindowManagerDebugConfig.DEBUG_LAYOUT) {
                            Slog.v(str, "Laying out IN_SCREEN status bar window: " + sf2);
                        }
                        applyStableConstraints(sysUiFl, fl, cf, displayFrames);
                        if (adjust != 48) {
                        }
                    } else {
                        if (WindowManagerDebugConfig.DEBUG_LAYOUT) {
                            sysUiFl7 = sysUiFl4;
                            str3 = "WindowManager";
                            Slog.v(str3, "layoutWindowLw(" + ((Object) attrs3.getTitle()) + "): IN_SCREEN, INSET_DECOR");
                        } else {
                            sysUiFl7 = sysUiFl4;
                            str3 = "WindowManager";
                        }
                        dcf = dcf2;
                        if (attached != null) {
                            layoutInsetDecor = layoutInsetDecor2;
                            sim = sim2;
                            sf = sf3;
                            cf = cf3;
                            attrs = attrs3;
                            setAttachedWindowFrames(win, fl5, adjust, attached, true, pf3, df4, of4, cf, vf2, displayFrames);
                            sf2 = pf3;
                            sysUiFl = sysUiFl7;
                            of = of4;
                            vf2 = vf2;
                            adjust = adjust;
                            str = str3;
                            df = df4;
                            fl = fl5;
                            type = type4;
                            i2 = 1;
                        } else {
                            attrs = attrs3;
                            layoutInsetDecor = layoutInsetDecor2;
                            sim = sim2;
                            sf = sf3;
                            if (type4 == 2014) {
                                pf2 = pf3;
                                sysUiFl8 = sysUiFl7;
                                of3 = of4;
                                df3 = df4;
                                fl4 = fl5;
                                i4 = 1;
                            } else if (type4 == 2017) {
                                pf2 = pf3;
                                sysUiFl8 = sysUiFl7;
                                of3 = of4;
                                df3 = df4;
                                fl4 = fl5;
                                i4 = 1;
                            } else {
                                fl4 = fl5;
                                if ((33554432 & fl4) != 0) {
                                    i4 = 1;
                                    if (type4 < 1 || type4 > 1999) {
                                        pf = pf3;
                                        of2 = of4;
                                        df2 = df4;
                                    } else {
                                        of2 = of4;
                                        of2.set(displayFrames.mOverscan);
                                        df2 = df4;
                                        df2.set(displayFrames.mOverscan);
                                        pf = pf3;
                                        pf.set(displayFrames.mOverscan);
                                        sysUiFl8 = sysUiFl7;
                                        str4 = str3;
                                        if ((fl4 & 1024) != 0) {
                                            cf2 = cf3;
                                            adjust4 = adjust;
                                            cf2.set(displayFrames.mRestricted);
                                        } else if (win.isVoiceInteraction()) {
                                            cf2 = cf3;
                                            cf2.set(displayFrames.mVoiceContent);
                                            adjust4 = adjust;
                                        } else {
                                            cf2 = cf3;
                                            if (ViewRootImpl.sNewInsetsMode == 0) {
                                                adjust4 = adjust;
                                                if (adjust4 == 16) {
                                                    cf2.set(displayFrames.mContent);
                                                }
                                            } else {
                                                adjust4 = adjust;
                                            }
                                            cf2.set(displayFrames.mDock);
                                        }
                                        applyStableConstraints(sysUiFl8, fl4, cf2, displayFrames);
                                        if (adjust4 != 48) {
                                            vf2.set(displayFrames.mCurrent);
                                            str = str4;
                                            fl = fl4;
                                            of = of2;
                                            sf2 = pf;
                                            vf2 = vf2;
                                            adjust = adjust4;
                                            sysUiFl = sysUiFl8;
                                            type = type4;
                                            df = df2;
                                            cf = cf2;
                                            i2 = i4;
                                        } else {
                                            vf2.set(cf2);
                                            str = str4;
                                            fl = fl4;
                                            of = of2;
                                            sf2 = pf;
                                            vf2 = vf2;
                                            adjust = adjust4;
                                            sysUiFl = sysUiFl8;
                                            type = type4;
                                            df = df2;
                                            cf = cf2;
                                            i2 = i4;
                                        }
                                    }
                                } else {
                                    pf = pf3;
                                    of2 = of4;
                                    df2 = df4;
                                    i4 = 1;
                                }
                                sysUiFl8 = sysUiFl7;
                                if ((sysUiFl8 & 512) == 0 || !((type4 >= i4 && type4 <= 1999) || type4 == 2020 || type4 == 2009)) {
                                    df2.set(displayFrames.mRestrictedOverscan);
                                    pf.set(displayFrames.mRestrictedOverscan);
                                    of2.set(displayFrames.mUnrestricted);
                                    str4 = str3;
                                } else {
                                    df2.set(displayFrames.mOverscan);
                                    pf.set(displayFrames.mOverscan);
                                    of2.set(displayFrames.mUnrestricted);
                                    str4 = str3;
                                }
                                if ((fl4 & 1024) != 0) {
                                }
                                applyStableConstraints(sysUiFl8, fl4, cf2, displayFrames);
                                if (adjust4 != 48) {
                                }
                            }
                            int i15 = (hasNavBar ? displayFrames.mDock : displayFrames.mUnrestricted).left;
                            of2.left = i15;
                            df2.left = i15;
                            pf.left = i15;
                            int i16 = displayFrames.mUnrestricted.top;
                            of2.top = i16;
                            df2.top = i16;
                            pf.top = i16;
                            if (hasNavBar) {
                                i5 = displayFrames.mRestricted.right;
                            } else {
                                i5 = displayFrames.mUnrestricted.right;
                            }
                            of2.right = i5;
                            df2.right = i5;
                            pf.right = i5;
                            if (hasNavBar) {
                                i6 = displayFrames.mRestricted.bottom;
                            } else {
                                i6 = displayFrames.mUnrestricted.bottom;
                            }
                            of2.bottom = i6;
                            df2.bottom = i6;
                            pf.bottom = i6;
                            if (WindowManagerDebugConfig.DEBUG_LAYOUT) {
                                str4 = str3;
                                Slog.v(str4, "Laying out status bar window: " + pf);
                            } else {
                                str4 = str3;
                            }
                            if ((fl4 & 1024) != 0) {
                            }
                            applyStableConstraints(sysUiFl8, fl4, cf2, displayFrames);
                            if (adjust4 != 48) {
                            }
                        }
                    }
                } else {
                    cf3.set(displayFrames.mOverscan);
                    of4.set(displayFrames.mOverscan);
                    df4.set(displayFrames.mOverscan);
                    pf3.set(displayFrames.mOverscan);
                    attrs = attrs3;
                    layoutInsetDecor = layoutInsetDecor2;
                    sim = sim2;
                    sf = sf3;
                    df = df4;
                    fl = fl5;
                    sf2 = pf3;
                    cf = cf3;
                    sysUiFl = sysUiFl9;
                    i2 = 1;
                    of = of4;
                    type = type4;
                    dcf = dcf2;
                    str = "WindowManager";
                }
            }
            int cutoutMode = attrs.layoutInDisplayCutoutMode;
            int i17 = (attached == null || layoutInScreen) ? i : i2;
            int i18 = (requestedSysUiFl & 2) != 0 ? i2 : i;
            int i19 = (attrs.isFullscreen() || !layoutInScreen || type == i2) ? i : i2;
            boolean isOppoAlwaysMode = OppoFeatureCache.get(IColorFullScreenDisplayManager.DEFAULT).injectLayoutWindowLwDisplayPolicy(cutoutMode);
            if (cutoutMode == 1 || !isOppoAlwaysMode) {
                attrs2 = attrs;
                sysUiFl2 = sysUiFl;
                windowFrames2 = windowFrames;
            } else {
                Rect displayCutoutSafeExceptMaybeBars = sTmpDisplayCutoutSafeExceptMaybeBarsRect;
                attrs2 = attrs;
                displayCutoutSafeExceptMaybeBars.set(displayFrames.mDisplayCutoutSafe);
                if (layoutInScreen && layoutInsetDecor && !requestedFullscreen && cutoutMode == 0) {
                    displayCutoutSafeExceptMaybeBars.top = Integer.MIN_VALUE;
                }
                if (!layoutInScreen || !layoutInsetDecor || i18 != 0 || cutoutMode != 0) {
                    sysUiFl2 = sysUiFl;
                    sysUiFl3 = 4;
                } else {
                    int i20 = this.mNavigationBarPosition;
                    sysUiFl2 = sysUiFl;
                    if (i20 == 1) {
                        sysUiFl3 = 4;
                        displayCutoutSafeExceptMaybeBars.left = Integer.MIN_VALUE;
                    } else if (i20 != 2) {
                        sysUiFl3 = 4;
                        if (i20 == 4) {
                            displayCutoutSafeExceptMaybeBars.bottom = Integer.MAX_VALUE;
                        }
                    } else {
                        sysUiFl3 = 4;
                        displayCutoutSafeExceptMaybeBars.right = Integer.MAX_VALUE;
                    }
                }
                if (type == 2011 && this.mNavigationBarPosition == sysUiFl3) {
                    displayCutoutSafeExceptMaybeBars.bottom = Integer.MAX_VALUE;
                }
                if (i17 == 0 && i19 == 0) {
                    sTmpRect.set(sf2);
                    sf2.intersectUnchecked(displayCutoutSafeExceptMaybeBars);
                    windowFrames2 = windowFrames;
                    windowFrames2.setParentFrameWasClippedByDisplayCutout(!sTmpRect.equals(sf2));
                } else {
                    windowFrames2 = windowFrames;
                }
                df.intersectUnchecked(displayCutoutSafeExceptMaybeBars);
            }
            if (cutoutMode != 1 && OppoFeatureCache.get(IColorFullScreenDisplayManager.DEFAULT).injectLayoutWindowLwDisplayPolicy(cutoutMode)) {
                cf.intersectUnchecked(displayFrames.mDisplayCutoutSafe);
            }
            if (!((fl & 512) == 0 || type == 2010 || win.inMultiWindowMode())) {
                df.top = -10000;
                df.left = -10000;
                df.bottom = 10000;
                df.right = 10000;
                if (type != 2013) {
                    vf2.top = -10000;
                    vf2.left = -10000;
                    cf.top = -10000;
                    cf.left = -10000;
                    of.top = -10000;
                    of.left = -10000;
                    vf2.bottom = 10000;
                    vf2.right = 10000;
                    cf.bottom = 10000;
                    cf.right = 10000;
                    of.bottom = 10000;
                    of.right = 10000;
                }
            }
            adjustOppoWindowFrame(sf2, df, of, cf, dcf, vf2, attrs2, displayFrames);
            int i21 = (!shouldUseOutsets(attrs2, fl) || win.inZoomWindowingMode()) ? i : 1;
            if (!isDefaultDisplay || i21 == 0) {
                str2 = str;
            } else {
                Rect osf = windowFrames2.mOutsetFrame;
                osf.set(cf.left, cf.top, cf.right, cf.bottom);
                windowFrames2.setHasOutsets(true);
                int outset = this.mWindowOutsetBottom;
                if (outset > 0) {
                    int rotation2 = displayFrames.mRotation;
                    if (rotation2 == 0) {
                        osf.bottom += outset;
                    } else if (rotation2 == 1) {
                        osf.right += outset;
                    } else if (rotation2 == 2) {
                        osf.top -= outset;
                    } else if (rotation2 == 3) {
                        osf.left -= outset;
                    }
                    if (WindowManagerDebugConfig.DEBUG_LAYOUT) {
                        str2 = str;
                        Slog.v(str2, "applying bottom outset of " + outset + " with rotation " + rotation2 + ", result: " + osf);
                    } else {
                        str2 = str;
                    }
                } else {
                    str2 = str;
                }
            }
            if (WindowManagerDebugConfig.DEBUG_LAYOUT) {
                StringBuilder sb = new StringBuilder();
                sb.append("Compute frame ");
                sb.append((Object) attrs2.getTitle());
                sb.append(": sim=#");
                sb.append(Integer.toHexString(sim));
                sb.append(" attach=");
                sb.append(attached);
                sb.append(" type=");
                sb.append(type);
                Object[] objArr = new Object[1];
                objArr[i] = Integer.valueOf(fl);
                sb.append(String.format(" flags=0x%08x", objArr));
                sb.append(" pf=");
                sb.append(sf2.toShortString());
                sb.append(" df=");
                sb.append(df.toShortString());
                sb.append(" of=");
                sb.append(of.toShortString());
                sb.append(" cf=");
                sb.append(cf.toShortString());
                sb.append(" vf=");
                sb.append(vf2.toShortString());
                sb.append(" dcf=");
                sb.append(dcf.toShortString());
                sb.append(" sf=");
                sb.append(sf.toShortString());
                sb.append(" osf=");
                sb.append(windowFrames2.mOutsetFrame.toShortString());
                sb.append(StringUtils.SPACE);
                windowState = win;
                windowFrames3 = windowFrames2;
                sb.append(windowState);
                Slog.v(str2, sb.toString());
            } else {
                windowState = win;
                windowFrames3 = windowFrames2;
            }
            if (!sTmpLastParentFrame.equals(sf2)) {
                windowFrames3.setContentChanged(true);
            }
            win.computeFrameLw();
            if (type == 2011 && win.isVisibleLw() && !win.getGivenInsetsPendingLw()) {
                offsetInputMethodWindowLw(windowState, displayFrames);
            }
            if (type == 2031 && win.isVisibleLw() && !win.getGivenInsetsPendingLw()) {
                offsetVoiceInputWindowLw(windowState, displayFrames);
            }
        }
    }

    private void layoutWallpaper(DisplayFrames displayFrames, Rect pf, Rect df, Rect of, Rect cf) {
        df.set(displayFrames.mOverscan);
        pf.set(displayFrames.mOverscan);
        cf.set(displayFrames.mUnrestricted);
        of.set(displayFrames.mUnrestricted);
    }

    private void offsetInputMethodWindowLw(WindowState win, DisplayFrames displayFrames) {
        int top = Math.max(win.getDisplayFrameLw().top, win.getContentFrameLw().top) + win.getGivenContentInsetsLw().top;
        displayFrames.mContent.bottom = Math.min(displayFrames.mContent.bottom, top);
        displayFrames.mVoiceContent.bottom = Math.min(displayFrames.mVoiceContent.bottom, top);
        int top2 = win.getVisibleFrameLw().top + win.getGivenVisibleInsetsLw().top;
        displayFrames.mCurrent.bottom = Math.min(displayFrames.mCurrent.bottom, top2);
        if (WindowManagerDebugConfig.DEBUG_LAYOUT) {
            Slog.v("WindowManager", "Input method: mDockBottom=" + displayFrames.mDock.bottom + " mContentBottom=" + displayFrames.mContent.bottom + " mCurBottom=" + displayFrames.mCurrent.bottom);
        }
    }

    private void offsetVoiceInputWindowLw(WindowState win, DisplayFrames displayFrames) {
        int top = Math.max(win.getDisplayFrameLw().top, win.getContentFrameLw().top) + win.getGivenContentInsetsLw().top;
        displayFrames.mVoiceContent.bottom = Math.min(displayFrames.mVoiceContent.bottom, top);
    }

    public void beginPostLayoutPolicyLw() {
        this.mTopFullscreenOpaqueWindowState = null;
        this.mTopFullscreenOpaqueOrDimmingWindowState = null;
        this.mTopDockedOpaqueWindowState = null;
        this.mColorDpEx.setTopDockedWindowState(null);
        this.mTopDockedOpaqueOrDimmingWindowState = null;
        this.mForceStatusBar = false;
        this.mForceStatusBarFromKeyguard = false;
        this.mForceStatusBarTransparent = false;
        this.mForcingShowNavBar = false;
        this.mForcingShowNavBarLayer = -1;
        this.mAllowLockscreenWhenOn = false;
        this.mShowingDream = false;
        this.mWindowSleepTokenNeeded = false;
    }

    public void applyPostLayoutPolicyLw(WindowState win, WindowManager.LayoutParams attrs, WindowState attached, WindowState imeTarget) {
        boolean affectsSystemUi = win.canAffectSystemUiFlags();
        if (WindowManagerDebugConfig.DEBUG_LAYOUT) {
            Slog.i("WindowManager", "Win " + win + ": affectsSystemUi=" + affectsSystemUi);
        }
        this.mService.mPolicy.applyKeyguardPolicyLw(win, imeTarget);
        int fl = PolicyControl.getWindowFlags(win, attrs);
        if (this.mTopFullscreenOpaqueWindowState == null && affectsSystemUi && attrs.type == 2011) {
            this.mForcingShowNavBar = true;
            this.mForcingShowNavBarLayer = win.getSurfaceLayer();
        }
        if (attrs.type == 2000) {
            if ((attrs.privateFlags & 1024) != 0) {
                this.mForceStatusBarFromKeyguard = true;
            }
            if ((attrs.privateFlags & 4096) != 0) {
                this.mForceStatusBarTransparent = true;
            }
        }
        boolean inFullScreenOrSplitScreenSecondaryWindowingMode = false;
        boolean appWindow = OppoFeatureCache.getOrCreate(IColorScreenShotEuclidManager.DEFAULT, new Object[0]).isSpecialAppWindow(attrs.type >= 1 && attrs.type < 2000, attrs);
        int windowingMode = win.getWindowingMode();
        if (windowingMode == 1 || windowingMode == 4) {
            inFullScreenOrSplitScreenSecondaryWindowingMode = true;
        }
        if (this.mTopFullscreenOpaqueWindowState == null && affectsSystemUi) {
            if ((fl & 2048) != 0) {
                this.mForceStatusBar = true;
            }
            if (attrs.type == 2023 && (!this.mDreamingLockscreen || (win.isVisibleLw() && win.hasDrawnLw()))) {
                this.mShowingDream = true;
                appWindow = true;
            }
            if (appWindow && attached == null && attrs.isFullscreen() && inFullScreenOrSplitScreenSecondaryWindowingMode) {
                if (WindowManagerDebugConfig.DEBUG_LAYOUT) {
                    Slog.v("WindowManager", "Fullscreen window: " + win);
                }
                this.mTopFullscreenOpaqueWindowState = win;
                if (this.mTopFullscreenOpaqueOrDimmingWindowState == null) {
                    this.mTopFullscreenOpaqueOrDimmingWindowState = win;
                }
                if ((fl & 1) != 0) {
                    this.mAllowLockscreenWhenOn = true;
                }
            }
        }
        if (affectsSystemUi && attrs.type == 2031) {
            if (this.mTopFullscreenOpaqueWindowState == null) {
                this.mTopFullscreenOpaqueWindowState = win;
                if (this.mTopFullscreenOpaqueOrDimmingWindowState == null) {
                    this.mTopFullscreenOpaqueOrDimmingWindowState = win;
                }
            }
            if (this.mTopDockedOpaqueWindowState == null) {
                this.mTopDockedOpaqueWindowState = win;
                if (this.mTopDockedOpaqueOrDimmingWindowState == null) {
                    this.mTopDockedOpaqueOrDimmingWindowState = win;
                }
            }
            if (this.mColorDpEx.getTopDockedWindowState() == null) {
                this.mColorDpEx.setTopDockedWindowState(win);
            }
        }
        if (this.mTopFullscreenOpaqueOrDimmingWindowState == null && affectsSystemUi && win.isDimming() && inFullScreenOrSplitScreenSecondaryWindowingMode) {
            this.mTopFullscreenOpaqueOrDimmingWindowState = win;
        }
        if (this.mTopDockedOpaqueWindowState == null && affectsSystemUi && appWindow && attached == null && attrs.isFullscreen() && windowingMode == 3) {
            this.mTopDockedOpaqueWindowState = win;
            if (this.mTopDockedOpaqueOrDimmingWindowState == null) {
                this.mTopDockedOpaqueOrDimmingWindowState = win;
            }
        }
        if (this.mColorDpEx.getTopDockedWindowState() == null && affectsSystemUi && appWindow && attached == null && windowingMode == 3) {
            this.mColorDpEx.setTopDockedWindowState(win);
        }
        if (this.mTopDockedOpaqueOrDimmingWindowState == null && affectsSystemUi && win.isDimming() && windowingMode == 3) {
            this.mTopDockedOpaqueOrDimmingWindowState = win;
        }
    }

    public int finishPostLayoutPolicyLw() {
        boolean topActivityIsFullscreen;
        int changes = 0;
        boolean topIsFullscreen = false;
        boolean z = true;
        if (!this.mShowingDream) {
            this.mDreamingLockscreen = this.mService.mPolicy.isKeyguardShowingAndNotOccluded();
            if (this.mDreamingSleepTokenNeeded) {
                this.mDreamingSleepTokenNeeded = false;
                this.mHandler.obtainMessage(1, 0, 1).sendToTarget();
            }
        } else if (!this.mDreamingSleepTokenNeeded) {
            this.mDreamingSleepTokenNeeded = true;
            this.mHandler.obtainMessage(1, 1, 1).sendToTarget();
        }
        if (this.mStatusBar != null) {
            if (WindowManagerDebugConfig.DEBUG_LAYOUT) {
                Slog.i("WindowManager", "force=" + this.mForceStatusBar + " forcefkg=" + this.mForceStatusBarFromKeyguard + " top=" + this.mTopFullscreenOpaqueWindowState);
            }
            if (!(this.mForceStatusBarTransparent && !this.mForceStatusBar && !this.mForceStatusBarFromKeyguard)) {
                this.mStatusBarController.setShowTransparent(false);
            } else if (!this.mStatusBar.isVisibleLw()) {
                this.mStatusBarController.setShowTransparent(true);
            }
            boolean statusBarForcesShowingNavigation = (this.mStatusBar.getAttrs().privateFlags & DumpState.DUMP_VOLUMES) != 0;
            boolean topAppHidesStatusBar = topAppHidesStatusBar();
            if (this.mForceStatusBar || this.mForceStatusBarFromKeyguard || this.mForceStatusBarTransparent || statusBarForcesShowingNavigation) {
                if (WindowManagerDebugConfig.DEBUG_LAYOUT) {
                    Slog.v("WindowManager", "Showing status bar: forced");
                }
                if (this.mStatusBarController.setBarShowingLw(true)) {
                    changes = 0 | 1;
                }
                if (!this.mTopIsFullscreen || !this.mStatusBar.isAnimatingLw()) {
                    z = false;
                }
                topIsFullscreen = z;
                if ((this.mForceStatusBarFromKeyguard || statusBarForcesShowingNavigation) && this.mStatusBarController.isTransientShowing()) {
                    StatusBarController statusBarController = this.mStatusBarController;
                    int i = this.mLastSystemUiFlags;
                    statusBarController.updateVisibilityLw(false, i, i);
                }
            } else if (this.mTopFullscreenOpaqueWindowState != null) {
                topIsFullscreen = topAppHidesStatusBar;
                if (this.mStatusBarController.isTransientShowing()) {
                    if (this.mStatusBarController.setBarShowingLw(true)) {
                        changes = 0 | 1;
                    }
                } else if (!topIsFullscreen || this.mDisplayContent.isStackVisible(5) || this.mDisplayContent.isStackVisible(3)) {
                    if (WindowManagerDebugConfig.DEBUG_LAYOUT) {
                        Slog.v("WindowManager", "** SHOWING status bar: top is not fullscreen");
                    }
                    if (this.mStatusBarController.setBarShowingLw(true)) {
                        changes = 0 | 1;
                    }
                    topAppHidesStatusBar = false;
                } else {
                    if (WindowManagerDebugConfig.DEBUG_LAYOUT) {
                        Slog.v("WindowManager", "** HIDING status bar");
                    }
                    if (this.mStatusBarController.setBarShowingLw(false)) {
                        changes = 0 | 1;
                    } else if (WindowManagerDebugConfig.DEBUG_LAYOUT) {
                        Slog.v("WindowManager", "Status bar already hiding");
                    }
                }
            }
            this.mStatusBarController.setTopAppHidesStatusBar(topAppHidesStatusBar);
        }
        if (this.mColorDpEx.isStatusBarExpanded(this.mStatusBar)) {
            topActivityIsFullscreen = topAppHidesStatusBar();
        } else {
            topActivityIsFullscreen = topIsFullscreen;
        }
        if (this.mColorDpEx.getTopActivityIsFullscreen() != topActivityIsFullscreen) {
            this.mColorDpEx.topActivityStatusChanged(topActivityIsFullscreen);
        }
        if (this.mTopIsFullscreen != topIsFullscreen) {
            this.mColorDpEx.topActivityStatusChanged(topActivityIsFullscreen);
            if (!topIsFullscreen) {
                changes |= 1;
            }
            this.mTopIsFullscreen = topIsFullscreen;
        }
        if ((updateSystemUiVisibilityLw() & SYSTEM_UI_CHANGING_LAYOUT) != 0) {
            changes |= 1;
        }
        boolean z2 = this.mShowingDream;
        if (z2 != this.mLastShowingDream) {
            this.mLastShowingDream = z2;
            this.mService.notifyShowingDreamChanged();
        }
        updateWindowSleepToken();
        this.mService.mPolicy.setAllowLockscreenWhenOn(getDisplayId(), this.mAllowLockscreenWhenOn);
        return changes;
    }

    private void updateWindowSleepToken() {
        if (this.mWindowSleepTokenNeeded && !this.mLastWindowSleepTokenNeeded) {
            this.mHandler.removeCallbacks(this.mReleaseSleepTokenRunnable);
            this.mHandler.post(this.mAcquireSleepTokenRunnable);
        } else if (!this.mWindowSleepTokenNeeded && this.mLastWindowSleepTokenNeeded) {
            this.mHandler.removeCallbacks(this.mAcquireSleepTokenRunnable);
            this.mHandler.post(this.mReleaseSleepTokenRunnable);
        }
        this.mLastWindowSleepTokenNeeded = this.mWindowSleepTokenNeeded;
    }

    private boolean topAppHidesStatusBar() {
        if (this.mTopFullscreenOpaqueWindowState == null) {
            return false;
        }
        if (OppoFeatureCache.getOrCreate(IColorScreenShotEuclidManager.DEFAULT, new Object[0]).updateSpecialSystemBar(this.mTopFullscreenOpaqueWindowState.getAttrs())) {
            return this.mTopIsFullscreen;
        }
        if (this.mTopFullscreenOpaqueWindowState.getAttrs().type == 2023) {
            return this.mTopIsFullscreen;
        }
        int fl = PolicyControl.getWindowFlags(null, this.mTopFullscreenOpaqueWindowState.getAttrs());
        if (WindowManagerService.localLOGV) {
            Slog.d("WindowManager", "frame: " + this.mTopFullscreenOpaqueWindowState.getFrameLw());
            Slog.d("WindowManager", "attr: " + this.mTopFullscreenOpaqueWindowState.getAttrs() + " lp.flags=0x" + Integer.toHexString(fl));
        }
        if ((fl & 1024) == 0 && (this.mLastSystemUiFlags & 4) == 0) {
            return false;
        }
        return true;
    }

    public void switchUser() {
        updateCurrentUserResources();
    }

    public void onOverlayChangedLw() {
        updateCurrentUserResources();
        onConfigurationChanged();
        this.mSystemGestures.onConfigurationChanged();
    }

    public void onConfigurationChanged() {
        DisplayRotation displayRotation = this.mDisplayContent.getDisplayRotation();
        Resources res = getCurrentUserResources();
        int portraitRotation = displayRotation.getPortraitRotation();
        int upsideDownRotation = displayRotation.getUpsideDownRotation();
        int landscapeRotation = displayRotation.getLandscapeRotation();
        int seascapeRotation = displayRotation.getSeascapeRotation();
        int uiMode = this.mService.mPolicy.getUiMode();
        if (hasStatusBar()) {
            int[] iArr = this.mStatusBarHeightForRotation;
            int dimensionPixelSize = res.getDimensionPixelSize(17105424);
            iArr[upsideDownRotation] = dimensionPixelSize;
            iArr[portraitRotation] = dimensionPixelSize;
            int[] iArr2 = this.mStatusBarHeightForRotation;
            int dimensionPixelSize2 = res.getDimensionPixelSize(17105423);
            iArr2[seascapeRotation] = dimensionPixelSize2;
            iArr2[landscapeRotation] = dimensionPixelSize2;
        } else {
            int[] iArr3 = this.mStatusBarHeightForRotation;
            iArr3[seascapeRotation] = 0;
            iArr3[landscapeRotation] = 0;
            iArr3[upsideDownRotation] = 0;
            iArr3[portraitRotation] = 0;
        }
        int[] iArr4 = this.mNavigationBarHeightForRotationDefault;
        int dimensionPixelSize3 = res.getDimensionPixelSize(17105288);
        iArr4[upsideDownRotation] = dimensionPixelSize3;
        iArr4[portraitRotation] = dimensionPixelSize3;
        int[] iArr5 = this.mNavigationBarHeightForRotationDefault;
        int dimensionPixelSize4 = res.getDimensionPixelSize(17105290);
        iArr5[seascapeRotation] = dimensionPixelSize4;
        iArr5[landscapeRotation] = dimensionPixelSize4;
        int[] iArr6 = this.mNavigationBarFrameHeightForRotationDefault;
        int dimensionPixelSize5 = res.getDimensionPixelSize(17105285);
        iArr6[upsideDownRotation] = dimensionPixelSize5;
        iArr6[portraitRotation] = dimensionPixelSize5;
        int[] iArr7 = this.mNavigationBarFrameHeightForRotationDefault;
        int dimensionPixelSize6 = res.getDimensionPixelSize(17105286);
        iArr7[seascapeRotation] = dimensionPixelSize6;
        iArr7[landscapeRotation] = dimensionPixelSize6;
        int[] iArr8 = this.mNavigationBarWidthForRotationDefault;
        int dimensionPixelSize7 = res.getDimensionPixelSize(17105293);
        iArr8[seascapeRotation] = dimensionPixelSize7;
        iArr8[landscapeRotation] = dimensionPixelSize7;
        iArr8[upsideDownRotation] = dimensionPixelSize7;
        iArr8[portraitRotation] = dimensionPixelSize7;
        this.mColorDpEx.loadGestureBarHeight(res, portraitRotation, upsideDownRotation, landscapeRotation, seascapeRotation);
        this.mNavBarOpacityMode = res.getInteger(17694850);
        this.mSideGestureInset = res.getDimensionPixelSize(17105049);
        this.mNavigationBarLetsThroughTaps = res.getBoolean(17891485);
        this.mNavigationBarAlwaysShowOnSideGesture = res.getBoolean(17891482);
        this.mBottomGestureAdditionalInset = res.getDimensionPixelSize(17105287) - getNavigationBarFrameHeight(portraitRotation, uiMode);
        updateConfigurationAndScreenSizeDependentBehaviors();
        this.mWindowOutsetBottom = ScreenShapeHelper.getWindowOutsetBottomPx(this.mContext.getResources());
    }

    /* access modifiers changed from: package-private */
    public void updateConfigurationAndScreenSizeDependentBehaviors() {
        Resources res = getCurrentUserResources();
        this.mNavigationBarCanMove = this.mDisplayContent.mBaseDisplayWidth != this.mDisplayContent.mBaseDisplayHeight && !this.mColorDpEx.isNavGestureMode();
        this.mAllowSeamlessRotationDespiteNavBarMoving = res.getBoolean(17891346);
    }

    private void updateCurrentUserResources() {
        int userId = this.mService.mAmInternal.getCurrentUserId();
        Context uiContext = getSystemUiContext();
        if (userId == 0) {
            this.mCurrentUserResources = uiContext.getResources();
            return;
        }
        LoadedApk pi = ActivityThread.currentActivityThread().getPackageInfo(uiContext.getPackageName(), (CompatibilityInfo) null, 0, userId);
        this.mCurrentUserResources = ResourcesManager.getInstance().getResources((IBinder) null, pi.getResDir(), (String[]) null, pi.getOverlayDirs(), pi.getApplicationInfo().sharedLibraryFiles, this.mDisplayContent.getDisplayId(), (Configuration) null, uiContext.getResources().getCompatibilityInfo(), (ClassLoader) null);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public Resources getCurrentUserResources() {
        if (this.mCurrentUserResources == null) {
            updateCurrentUserResources();
        }
        return this.mCurrentUserResources;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public Context getContext() {
        return this.mContext;
    }

    private Context getSystemUiContext() {
        Context uiContext = ActivityThread.currentActivityThread().getSystemUiContext();
        if (this.mDisplayContent.isDefaultDisplay) {
            return uiContext;
        }
        return uiContext.createDisplayContext(this.mDisplayContent.getDisplay());
    }

    private int getNavigationBarWidth(int rotation, int uiMode) {
        return this.mNavigationBarWidthForRotationDefault[rotation];
    }

    /* access modifiers changed from: package-private */
    public void notifyDisplayReady() {
        this.mHandler.post(new Runnable() {
            /* class com.android.server.wm.$$Lambda$DisplayPolicy$mUPXUZKrPpeFUjrauzoJMNbYjM */

            public final void run() {
                DisplayPolicy.this.lambda$notifyDisplayReady$9$DisplayPolicy();
            }
        });
    }

    public /* synthetic */ void lambda$notifyDisplayReady$9$DisplayPolicy() {
        int displayId = getDisplayId();
        getStatusBarManagerInternal().onDisplayReady(displayId);
        ((WallpaperManagerInternal) LocalServices.getService(WallpaperManagerInternal.class)).onDisplayReady(displayId);
    }

    public int getNonDecorDisplayWidth(int fullWidth, int fullHeight, int rotation, int uiMode, DisplayCutout displayCutout) {
        int navBarPosition;
        int width = fullWidth;
        if (hasNavigationBar() && !this.mColorDpEx.isNavGestureMode() && ((navBarPosition = navigationBarPosition(fullWidth, fullHeight, rotation)) == 1 || navBarPosition == 2)) {
            width -= getNavigationBarWidth(rotation, uiMode);
        }
        if (displayCutout != null) {
            return width - (displayCutout.getSafeInsetLeft() + displayCutout.getSafeInsetRight());
        }
        return width;
    }

    public int getNavigationBarHeight(int rotation, int uiMode) {
        return this.mNavigationBarHeightForRotationDefault[rotation];
    }

    private int getNavigationBarFrameHeight(int rotation, int uiMode) {
        return this.mNavigationBarFrameHeightForRotationDefault[rotation];
    }

    public int getNonDecorDisplayHeight(int fullWidth, int fullHeight, int rotation, int uiMode, DisplayCutout displayCutout) {
        int height = fullHeight;
        if (hasNavigationBar() && !this.mColorDpEx.isNavGestureMode() && navigationBarPosition(fullWidth, fullHeight, rotation) == 4) {
            height -= getNavigationBarHeight(rotation, uiMode);
        }
        if (displayCutout != null) {
            return height - (displayCutout.getSafeInsetTop() + displayCutout.getSafeInsetBottom());
        }
        return height;
    }

    public int getConfigDisplayWidth(int fullWidth, int fullHeight, int rotation, int uiMode, DisplayCutout displayCutout) {
        return getNonDecorDisplayWidth(fullWidth, fullHeight, rotation, uiMode, displayCutout);
    }

    public int getConfigDisplayHeight(int fullWidth, int fullHeight, int rotation, int uiMode, DisplayCutout displayCutout) {
        int statusBarHeight = this.mStatusBarHeightForRotation[rotation];
        if (displayCutout != null) {
            statusBarHeight = Math.max(0, statusBarHeight - displayCutout.getSafeInsetTop());
        }
        return getNonDecorDisplayHeight(fullWidth, fullHeight, rotation, uiMode, displayCutout) - statusBarHeight;
    }

    /* access modifiers changed from: package-private */
    public float getWindowCornerRadius() {
        return this.mDisplayContent.getDisplay().getType() == 1 ? ScreenDecorationsUtils.getWindowCornerRadius(this.mContext.getResources()) : OppoBrightUtils.MIN_LUX_LIMITI;
    }

    /* access modifiers changed from: package-private */
    public boolean isShowingDreamLw() {
        return this.mShowingDream;
    }

    /* access modifiers changed from: package-private */
    public void convertNonDecorInsetsToStableInsets(Rect inOutInsets, int rotation) {
        inOutInsets.top = Math.max(inOutInsets.top, this.mStatusBarHeightForRotation[rotation]);
    }

    public void getStableInsetsLw(int displayRotation, int displayWidth, int displayHeight, DisplayCutout displayCutout, Rect outInsets) {
        outInsets.setEmpty();
        getNonDecorInsetsLw(displayRotation, displayWidth, displayHeight, displayCutout, outInsets);
        convertNonDecorInsetsToStableInsets(outInsets, displayRotation);
    }

    public void getNonDecorInsetsLw(int displayRotation, int displayWidth, int displayHeight, DisplayCutout displayCutout, Rect outInsets) {
        outInsets.setEmpty();
        if (hasNavigationBar() && !this.mColorDpEx.isNavGestureMode()) {
            int uiMode = this.mService.mPolicy.getUiMode();
            int position = navigationBarPosition(displayWidth, displayHeight, displayRotation);
            if (position == 4) {
                outInsets.bottom = getNavigationBarHeight(displayRotation, uiMode);
            } else if (position == 2) {
                outInsets.right = getNavigationBarWidth(displayRotation, uiMode);
            } else if (position == 1) {
                outInsets.left = getNavigationBarWidth(displayRotation, uiMode);
            }
        }
        if (displayCutout != null) {
            outInsets.left += displayCutout.getSafeInsetLeft();
            outInsets.top += displayCutout.getSafeInsetTop();
            outInsets.right += displayCutout.getSafeInsetRight();
            outInsets.bottom += displayCutout.getSafeInsetBottom();
        }
    }

    public void setForwardedInsets(Insets forwardedInsets) {
        this.mForwardedInsets = forwardedInsets;
    }

    public Insets getForwardedInsets() {
        return this.mForwardedInsets;
    }

    /* access modifiers changed from: package-private */
    public int navigationBarPosition(int displayWidth, int displayHeight, int displayRotation) {
        if (!this.mColorDpEx.isNavGestureMode() && navigationBarCanMove() && displayWidth > displayHeight) {
            if (displayRotation == 3) {
                return 1;
            }
            if (displayRotation == 1) {
                return 2;
            }
        }
        return 4;
    }

    public int getNavBarPosition() {
        return this.mNavigationBarPosition;
    }

    public int focusChangedLw(WindowState lastFocus, WindowState newFocus) {
        this.mFocusedWindow = newFocus;
        this.mLastFocusedWindow = lastFocus;
        if (this.mDisplayContent.isDefaultDisplay) {
            this.mService.mPolicy.onDefaultDisplayFocusChangedLw(newFocus);
        }
        if ((updateSystemUiVisibilityLw() & SYSTEM_UI_CHANGING_LAYOUT) != 0) {
            return 1;
        }
        return 0;
    }

    public boolean allowAppAnimationsLw() {
        return !this.mShowingDream;
    }

    /* access modifiers changed from: private */
    public void updateDreamingSleepToken(boolean acquire) {
        if (acquire) {
            int displayId = getDisplayId();
            if (this.mDreamingSleepToken == null) {
                ActivityTaskManagerInternal activityTaskManagerInternal = this.mService.mAtmInternal;
                this.mDreamingSleepToken = activityTaskManagerInternal.acquireSleepToken("DreamOnDisplay" + displayId, displayId);
                return;
            }
            return;
        }
        ActivityTaskManagerInternal.SleepToken sleepToken = this.mDreamingSleepToken;
        if (sleepToken != null) {
            sleepToken.release();
            this.mDreamingSleepToken = null;
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0067, code lost:
        return;
     */
    public void requestTransientBars(WindowState swipeTarget) {
        synchronized (this.mLock) {
            if (this.mService.mPolicy.isUserSetupComplete()) {
                if (swipeTarget != this.mNavigationBar || !this.mColorDpEx.isNavGestureMode()) {
                    boolean sb = this.mStatusBarController.checkShowTransientBarLw();
                    boolean nb = this.mNavigationBarController.checkShowTransientBarLw() && !isNavBarEmpty(this.mLastSystemUiFlags);
                    if (sb || nb) {
                        if (OppoWindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
                            Slog.d("WindowManager", "requestTransientBars, force update systemui in special window");
                        }
                        this.mForceUpdateSystemUiVisInSpecialWindow = true;
                        if (nb || swipeTarget != this.mNavigationBar) {
                            if (sb) {
                                this.mStatusBarController.showTransient();
                            }
                            if (nb) {
                                this.mNavigationBarController.showTransient();
                            }
                            this.mImmersiveModeConfirmation.confirmCurrentPrompt();
                            updateSystemUiVisibilityLw();
                        }
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void disposeInputConsumer(WindowManagerPolicy.InputConsumer inputConsumer) {
        if (inputConsumer != null) {
            inputConsumer.dismiss();
        }
    }

    private boolean isStatusBarKeyguard() {
        WindowState windowState = this.mStatusBar;
        return (windowState == null || (windowState.getAttrs().privateFlags & 1024) == 0) ? false : true;
    }

    private boolean isKeyguardOccluded() {
        return this.mService.mPolicy.isKeyguardOccluded();
    }

    /* access modifiers changed from: package-private */
    public void resetSystemUiVisibilityLw() {
        this.mLastSystemUiFlags = 0;
        updateSystemUiVisibilityLw();
    }

    private int updateSystemUiVisibilityLw() {
        WindowState winCandidate;
        int tmpVisibility;
        WindowState winCandidate2;
        WindowState winCandidate3 = this.mFocusedWindow;
        if (winCandidate3 == null) {
            winCandidate3 = this.mTopFullscreenOpaqueWindowState;
        }
        if (winCandidate3 == null) {
            return 0;
        }
        boolean isKeyguard = true;
        if (winCandidate3.getAttrs().token == this.mImmersiveModeConfirmation.getWindowToken()) {
            WindowState windowState = this.mLastFocusedWindow;
            boolean lastFocusCanReceiveKeys = windowState != null && windowState.canReceiveKeys();
            if (isStatusBarKeyguard()) {
                winCandidate2 = this.mStatusBar;
            } else if (lastFocusCanReceiveKeys) {
                winCandidate2 = this.mLastFocusedWindow;
            } else {
                winCandidate2 = this.mTopFullscreenOpaqueWindowState;
            }
            if (winCandidate2 == null) {
                return 0;
            }
            winCandidate = winCandidate2;
        } else {
            winCandidate = winCandidate3;
        }
        if (((winCandidate.getAttrs().privateFlags & 1024) != 0 && isKeyguardOccluded()) || OppoFeatureCache.getOrCreate(IColorScreenShotEuclidManager.DEFAULT, new Object[0]).skipSystemUiVisibility(winCandidate.getAttrs()) || winCandidate.getAttrs().type == 2023) {
            return 0;
        }
        if (!(winCandidate.getAttrs() == null || winCandidate.getAttrs().getTitle() == null || !this.mColorDpEx.willSkipSystemUI(winCandidate.getAttrs().getTitle().toString()))) {
            if (OppoWindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
                Slog.d("WindowManager", "updateSystemUiVisibilityLw will Skip SystemUI. forceUpdate:" + this.mForceUpdateSystemUiVisInSpecialWindow);
            }
            if (!this.mForceUpdateSystemUiVisInSpecialWindow) {
                return 0;
            }
            this.mForceUpdateSystemUiVisInSpecialWindow = false;
        }
        this.mDisplayContent.getInsetsStateController().onBarControllingWindowChanged(this.mTopFullscreenOpaqueWindowState);
        int tmpVisibility2 = PolicyControl.getSystemUiVisibility(winCandidate, null) & (~this.mResettingSystemUiFlags) & (~this.mForceClearedSystemUiFlags);
        if (this.mForcingShowNavBar && winCandidate.getSurfaceLayer() < this.mForcingShowNavBarLayer) {
            tmpVisibility2 &= ~PolicyControl.adjustClearableFlags(winCandidate, 7);
        }
        boolean isAppRequestImmersive = ((tmpVisibility2 & 2) == 0 || (tmpVisibility2 & 4096) == 0) ? false : true;
        if (!this.mColorDpEx.isNavBarImmersive() || isAppRequestImmersive) {
            tmpVisibility = tmpVisibility2;
        } else {
            tmpVisibility = tmpVisibility2 | UsbACInterface.FORMAT_II_AC3;
        }
        int fullscreenVisibility = updateLightStatusBarLw(0, this.mTopFullscreenOpaqueWindowState, this.mTopFullscreenOpaqueOrDimmingWindowState);
        int dockedVisibility = updateLightStatusBarLw(0, this.mTopDockedOpaqueWindowState, this.mTopDockedOpaqueOrDimmingWindowState);
        this.mService.getStackBounds(0, 2, this.mNonDockedStackBounds);
        this.mService.getStackBounds(3, 1, this.mDockedStackBounds);
        Pair<Integer, Boolean> result = updateSystemBarsLw(winCandidate, this.mLastSystemUiFlags, tmpVisibility);
        int visibility = ((Integer) result.first).intValue();
        int diff = visibility ^ this.mLastSystemUiFlags;
        int fullscreenDiff = fullscreenVisibility ^ this.mLastFullscreenStackSysUiFlags;
        int dockedDiff = dockedVisibility ^ this.mLastDockedStackSysUiFlags;
        boolean needsMenu = winCandidate.getNeedsMenuLw(this.mTopFullscreenOpaqueWindowState);
        if (!this.mService.mPolicy.isKeyguardLocked() || winCandidate != this.mStatusBar) {
            isKeyguard = false;
        }
        int navBarVis = this.mColorDpEx.getNavBarVisibility(isKeyguard, this.mForcingShowNavBar, this.mScreenOnEarly);
        this.mColorDpEx.notifyWindowStateChange(visibility);
        if (diff == 0 && fullscreenDiff == 0 && dockedDiff == 0 && this.mLastFocusNeedsMenu == needsMenu && this.mFocusedApp == winCandidate.getAppToken() && this.mLastNonDockedStackBounds.equals(this.mNonDockedStackBounds) && this.mLastDockedStackBounds.equals(this.mDockedStackBounds) && this.mLastNavBarVis == navBarVis) {
            return 0;
        }
        this.mLastNavBarVis = navBarVis;
        this.mLastSystemUiFlags = visibility;
        this.mLastFullscreenStackSysUiFlags = fullscreenVisibility;
        this.mLastDockedStackSysUiFlags = dockedVisibility;
        this.mLastFocusNeedsMenu = needsMenu;
        this.mFocusedApp = winCandidate.getAppToken();
        this.mLastNonDockedStackBounds.set(this.mNonDockedStackBounds);
        this.mLastDockedStackBounds.set(this.mDockedStackBounds);
        Rect fullscreenStackBounds = new Rect(this.mNonDockedStackBounds);
        Rect dockedStackBounds = new Rect(this.mDockedStackBounds);
        boolean isNavbarColorManagedByIme = ((Boolean) result.second).booleanValue();
        this.mColorDpEx.handleMultiWindowFocusChanged(this.mFocusedWindow, this.mFocusedApp, this.mHandler);
        this.mHandler.post(new Runnable(visibility, fullscreenVisibility, dockedVisibility, fullscreenStackBounds, dockedStackBounds, isNavbarColorManagedByIme, winCandidate, navBarVis, needsMenu) {
            /* class com.android.server.wm.$$Lambda$DisplayPolicy$pAyx7kKcPbvythOoKC3uHDTsjg */
            private final /* synthetic */ int f$1;
            private final /* synthetic */ int f$2;
            private final /* synthetic */ int f$3;
            private final /* synthetic */ Rect f$4;
            private final /* synthetic */ Rect f$5;
            private final /* synthetic */ boolean f$6;
            private final /* synthetic */ WindowState f$7;
            private final /* synthetic */ int f$8;
            private final /* synthetic */ boolean f$9;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
                this.f$5 = r6;
                this.f$6 = r7;
                this.f$7 = r8;
                this.f$8 = r9;
                this.f$9 = r10;
            }

            public final void run() {
                DisplayPolicy.this.lambda$updateSystemUiVisibilityLw$10$DisplayPolicy(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7, this.f$8, this.f$9);
            }
        });
        return diff;
    }

    public /* synthetic */ void lambda$updateSystemUiVisibilityLw$10$DisplayPolicy(int visibility, int fullscreenVisibility, int dockedVisibility, Rect fullscreenStackBounds, Rect dockedStackBounds, boolean isNavbarColorManagedByIme, WindowState win, int navBarVis, boolean needsMenu) {
        StatusBarManagerInternal statusBar = getStatusBarManagerInternal();
        if (statusBar != null) {
            int displayId = getDisplayId();
            this.mColorDpEx.setSystemUiVisibility(statusBar, displayId, visibility, fullscreenVisibility, dockedVisibility, -1, fullscreenStackBounds, dockedStackBounds, isNavbarColorManagedByIme, win, navBarVis);
            statusBar.topAppWindowChanged(displayId, needsMenu);
        }
    }

    private int updateLightStatusBarLw(int vis, WindowState opaque, WindowState opaqueOrDimming) {
        boolean onKeyguard = isStatusBarKeyguard() && !isKeyguardOccluded();
        WindowState statusColorWin = onKeyguard ? this.mStatusBar : opaqueOrDimming;
        if (statusColorWin != null && (statusColorWin == opaque || onKeyguard)) {
            return (vis & -8193) | (PolicyControl.getSystemUiVisibility(statusColorWin, null) & 8192);
        }
        if (statusColorWin == null || !statusColorWin.isDimming()) {
            return vis;
        }
        return vis & -8193;
    }

    @VisibleForTesting
    static WindowState chooseNavigationColorWindowLw(WindowState opaque, WindowState opaqueOrDimming, WindowState imeWindow, int navBarPosition) {
        boolean imeWindowCanNavColorWindow = imeWindow != null && imeWindow.isVisibleLw() && navBarPosition == 4 && (PolicyControl.getWindowFlags(imeWindow, null) & Integer.MIN_VALUE) != 0;
        if (opaque != null && opaqueOrDimming == opaque) {
            return imeWindowCanNavColorWindow ? imeWindow : opaque;
        }
        if (opaqueOrDimming == null || !opaqueOrDimming.isDimming()) {
            if (imeWindowCanNavColorWindow) {
                return imeWindow;
            }
            return null;
        } else if (imeWindowCanNavColorWindow && WindowManager.LayoutParams.mayUseInputMethod(PolicyControl.getWindowFlags(opaqueOrDimming, null))) {
            return imeWindow;
        } else {
            return opaqueOrDimming;
        }
    }

    @VisibleForTesting
    static int updateLightNavigationBarLw(int vis, WindowState opaque, WindowState opaqueOrDimming, WindowState imeWindow, WindowState navColorWin) {
        if (navColorWin == null) {
            return vis;
        }
        if (navColorWin == imeWindow || navColorWin == opaque) {
            return (vis & -17) | (PolicyControl.getSystemUiVisibility(navColorWin, null) & 16);
        }
        if (navColorWin != opaqueOrDimming || !navColorWin.isDimming()) {
            return vis;
        }
        return vis & -17;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:104:0x0186, code lost:
        if (r36.mForceShowSystemBars != false) goto L_0x018b;
     */
    private Pair<Integer, Boolean> updateSystemBarsLw(WindowState win, int oldVis, int vis) {
        WindowState fullscreenTransWin;
        int vis2;
        boolean dockedStackVisible = this.mDisplayContent.isStackVisible(3);
        boolean freeformStackVisible = this.mDisplayContent.isStackVisible(5);
        boolean resizing = this.mDisplayContent.getDockedDividerController().isResizing();
        this.mForceShowSystemBars = dockedStackVisible || freeformStackVisible || resizing || this.mForceShowSystemBarsFromExternal;
        boolean forceOpaqueStatusBar = this.mForceShowSystemBars && !this.mForceStatusBarFromKeyguard;
        if (!isStatusBarKeyguard() || isKeyguardOccluded()) {
            fullscreenTransWin = this.mTopFullscreenOpaqueWindowState;
        } else {
            fullscreenTransWin = this.mStatusBar;
        }
        int vis3 = this.mNavigationBarController.applyTranslucentFlagLw(fullscreenTransWin, this.mStatusBarController.applyTranslucentFlagLw(fullscreenTransWin, vis, oldVis), oldVis);
        int dockedVis = this.mNavigationBarController.applyTranslucentFlagLw(this.mTopDockedOpaqueWindowState, this.mStatusBarController.applyTranslucentFlagLw(this.mTopDockedOpaqueWindowState, 0, 0), 0);
        boolean fullscreenDrawsStatusBarBackground = drawsStatusBarBackground(vis3, this.mTopFullscreenOpaqueWindowState);
        boolean dockedDrawsStatusBarBackground = drawsStatusBarBackground(dockedVis, this.mTopDockedOpaqueWindowState);
        boolean fullscreenDrawsNavBarBackground = drawsNavigationBarBackground(vis3, this.mTopFullscreenOpaqueWindowState);
        boolean dockedDrawsNavigationBarBackground = drawsNavigationBarBackground(dockedVis, this.mTopDockedOpaqueWindowState);
        boolean statusBarHasFocus = win.getAttrs().type == 2000;
        if (statusBarHasFocus && !isStatusBarKeyguard()) {
            int flags = 14342;
            if (isKeyguardOccluded()) {
                flags = 14342 | -1073741824;
            }
            vis3 = ((~flags) & vis3) | (oldVis & flags);
        }
        if (fullscreenDrawsStatusBarBackground && dockedDrawsStatusBarBackground) {
            vis2 = (vis3 | 8) & -1073741825;
        } else if (forceOpaqueStatusBar) {
            vis2 = vis3 & -1073741833;
        } else {
            vis2 = vis3;
        }
        int vis4 = configureNavBarOpacity(vis2, dockedStackVisible, freeformStackVisible, resizing, fullscreenDrawsNavBarBackground, dockedDrawsNavigationBarBackground);
        boolean immersiveSticky = (vis4 & 4096) != 0;
        WindowState windowState = this.mTopFullscreenOpaqueWindowState;
        boolean hideStatusBarWM = (windowState == null || (PolicyControl.getWindowFlags(windowState, null) & 1024) == 0) ? false : true;
        boolean hideStatusBarSysui = (vis4 & 4) != 0;
        boolean hideNavBarSysui = (vis4 & 2) != 0;
        boolean transientStatusBarAllowed = this.mStatusBar != null && (statusBarHasFocus || (!this.mForceShowSystemBars && (hideStatusBarWM || (hideStatusBarSysui && immersiveSticky))));
        boolean transientNavBarAllowed = this.mNavigationBar != null && !this.mForceShowSystemBars && hideNavBarSysui && immersiveSticky;
        long now = SystemClock.uptimeMillis();
        long j = this.mPendingPanicGestureUptime;
        boolean pendingPanic = j != 0 && now - j <= 30000;
        DisplayPolicy defaultDisplayPolicy = this.mService.getDefaultDisplayContentLocked().getDisplayPolicy();
        if (pendingPanic && hideNavBarSysui && !isStatusBarKeyguard() && defaultDisplayPolicy.isKeyguardDrawComplete()) {
            this.mPendingPanicGestureUptime = 0;
            this.mStatusBarController.showTransient();
            if (!isNavBarEmpty(vis4)) {
                this.mNavigationBarController.showTransient();
            }
        }
        boolean denyTransientStatus = this.mStatusBarController.isTransientShowRequested() && !transientStatusBarAllowed && hideStatusBarSysui;
        boolean denyTransientNav = this.mNavigationBarController.isTransientShowRequested() && !transientNavBarAllowed;
        if (denyTransientStatus || denyTransientNav) {
        }
        clearClearableFlagsLw();
        vis4 &= -8;
        boolean navAllowedHidden = ((vis4 & 2048) != 0) || (vis4 & true);
        if (hideNavBarSysui && !navAllowedHidden) {
            if (this.mService.mPolicy.getWindowLayerLw(win) > this.mService.mPolicy.getWindowLayerFromTypeLw(2022)) {
                vis4 &= -3;
            }
        }
        int vis5 = this.mStatusBarController.updateVisibilityLw(transientStatusBarAllowed, oldVis, vis4);
        boolean oldImmersiveMode = isImmersiveMode(oldVis);
        boolean newImmersiveMode = isImmersiveMode(vis5);
        if (oldImmersiveMode != newImmersiveMode) {
            this.mImmersiveModeConfirmation.immersiveModeChangedLw(win.getOwningPackage(), newImmersiveMode, this.mService.mPolicy.isUserSetupComplete(), isNavBarEmpty(win.getSystemUiVisibility()));
        }
        int vis6 = this.mNavigationBarController.updateVisibilityLw(transientNavBarAllowed, oldVis, vis5);
        WindowState navColorWin = chooseNavigationColorWindowLw(this.mTopFullscreenOpaqueWindowState, this.mTopFullscreenOpaqueOrDimmingWindowState, this.mDisplayContent.mInputMethodWindow, this.mNavigationBarPosition);
        return Pair.create(Integer.valueOf(updateLightNavigationBarLw(vis6, this.mTopFullscreenOpaqueWindowState, this.mTopFullscreenOpaqueOrDimmingWindowState, this.mDisplayContent.mInputMethodWindow, navColorWin)), Boolean.valueOf(navColorWin != null && navColorWin == this.mDisplayContent.mInputMethodWindow));
    }

    private boolean drawsBarBackground(int vis, WindowState win, BarController controller, int translucentFlag) {
        if (!controller.isTransparentAllowed(win)) {
            return false;
        }
        if (win == null) {
            return true;
        }
        boolean drawsSystemBars = (win.getAttrs().flags & Integer.MIN_VALUE) != 0;
        if ((win.getAttrs().privateFlags & DumpState.DUMP_INTENT_FILTER_VERIFIERS) != 0) {
            return true;
        }
        if (!drawsSystemBars || (vis & translucentFlag) != 0) {
            return false;
        }
        return true;
    }

    private boolean drawsStatusBarBackground(int vis, WindowState win) {
        return drawsBarBackground(vis, win, this.mStatusBarController, 67108864);
    }

    private boolean drawsNavigationBarBackground(int vis, WindowState win) {
        return drawsBarBackground(vis, win, this.mNavigationBarController, 134217728);
    }

    private int configureNavBarOpacity(int visibility, boolean dockedStackVisible, boolean freeformStackVisible, boolean isDockedDividerResizing, boolean fullscreenDrawsBackground, boolean dockedDrawsNavigationBarBackground) {
        int i = this.mNavBarOpacityMode;
        if (i == 2) {
            if (fullscreenDrawsBackground && dockedDrawsNavigationBarBackground) {
                return setNavBarTransparentFlag(visibility);
            }
            if (dockedStackVisible) {
                return setNavBarOpaqueFlag(visibility);
            }
            return visibility;
        } else if (i == 0) {
            if (dockedStackVisible || freeformStackVisible || isDockedDividerResizing) {
                return setNavBarOpaqueFlag(visibility);
            }
            if (fullscreenDrawsBackground) {
                return setNavBarTransparentFlag(visibility);
            }
            return visibility;
        } else if (i != 1) {
            return visibility;
        } else {
            if (isDockedDividerResizing) {
                return setNavBarOpaqueFlag(visibility);
            }
            if (freeformStackVisible) {
                return setNavBarTranslucentFlag(visibility);
            }
            return setNavBarOpaqueFlag(visibility);
        }
    }

    private int setNavBarOpaqueFlag(int visibility) {
        return 2147450879 & visibility;
    }

    private int setNavBarTranslucentFlag(int visibility) {
        return Integer.MIN_VALUE | (visibility & -32769);
    }

    private int setNavBarTransparentFlag(int visibility) {
        return 32768 | (visibility & Integer.MAX_VALUE);
    }

    private void clearClearableFlagsLw() {
        int i = this.mResettingSystemUiFlags;
        int newVal = i | 7;
        if (newVal != i) {
            this.mResettingSystemUiFlags = newVal;
            this.mDisplayContent.reevaluateStatusBarVisibility();
        }
    }

    private boolean isImmersiveMode(int vis) {
        return (this.mNavigationBar == null || (vis & 2) == 0 || (vis & 6144) == 0 || !canHideNavigationBar()) ? false : true;
    }

    private boolean canHideNavigationBar() {
        return hasNavigationBar();
    }

    /* access modifiers changed from: private */
    public static boolean isNavBarEmpty(int systemUiFlags) {
        return (systemUiFlags & 23068672) == 23068672;
    }

    /* access modifiers changed from: package-private */
    public boolean shouldRotateSeamlessly(DisplayRotation displayRotation, int oldRotation, int newRotation) {
        WindowState w;
        if (oldRotation == displayRotation.getUpsideDownRotation() || newRotation == displayRotation.getUpsideDownRotation()) {
            return false;
        }
        if ((!navigationBarCanMove() && !this.mAllowSeamlessRotationDespiteNavBarMoving) || (w = this.mTopFullscreenOpaqueWindowState) == null || w != this.mFocusedWindow) {
            return false;
        }
        if ((w.mAppToken == null || w.mAppToken.matchParentBounds()) && !w.isAnimatingLw() && w.getAttrs().rotationAnimation == 3) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void onPowerKeyDown(boolean isScreenOn) {
        if (this.mImmersiveModeConfirmation.onPowerKeyDown(isScreenOn, SystemClock.elapsedRealtime(), isImmersiveMode(this.mLastSystemUiFlags), isNavBarEmpty(this.mLastSystemUiFlags))) {
            this.mHandler.post(this.mHiddenNavPanic);
        }
    }

    /* access modifiers changed from: package-private */
    public void onVrStateChangedLw(boolean enabled) {
        this.mImmersiveModeConfirmation.onVrStateChangedLw(enabled);
    }

    public void onLockTaskStateChangedLw(int lockTaskState) {
        this.mImmersiveModeConfirmation.onLockTaskModeChangedLw(lockTaskState);
    }

    public void takeScreenshot(int screenshotType) {
        ScreenshotHelper screenshotHelper = this.mScreenshotHelper;
        if (screenshotHelper != null) {
            WindowState windowState = this.mStatusBar;
            boolean z = true;
            boolean z2 = windowState != null && windowState.isVisibleLw();
            WindowState windowState2 = this.mNavigationBar;
            if (windowState2 == null || !windowState2.isVisibleLw()) {
                z = false;
            }
            screenshotHelper.takeScreenshot(screenshotType, z2, z, this.mHandler);
        }
    }

    /* access modifiers changed from: package-private */
    public RefreshRatePolicy getRefreshRatePolicy() {
        return this.mRefreshRatePolicy;
    }

    /* access modifiers changed from: package-private */
    public void dump(String prefix, PrintWriter pw) {
        pw.print(prefix);
        pw.print("DisplayPolicy");
        String prefix2 = prefix + "  ";
        pw.print(prefix2);
        pw.print("mCarDockEnablesAccelerometer=");
        pw.print(this.mCarDockEnablesAccelerometer);
        pw.print(" mDeskDockEnablesAccelerometer=");
        pw.println(this.mDeskDockEnablesAccelerometer);
        pw.print(prefix2);
        pw.print("mDockMode=");
        pw.print(Intent.dockStateToString(this.mDockMode));
        pw.print(" mLidState=");
        pw.println(WindowManagerPolicy.WindowManagerFuncs.lidStateToString(this.mLidState));
        pw.print(prefix2);
        pw.print("mAwake=");
        pw.print(this.mAwake);
        pw.print(" mScreenOnEarly=");
        pw.print(this.mScreenOnEarly);
        pw.print(" mScreenOnFully=");
        pw.println(this.mScreenOnFully);
        pw.print(prefix2);
        pw.print("mKeyguardDrawComplete=");
        pw.print(this.mKeyguardDrawComplete);
        pw.print(" mWindowManagerDrawComplete=");
        pw.println(this.mWindowManagerDrawComplete);
        pw.print(prefix2);
        pw.print("mHdmiPlugged=");
        pw.println(this.mHdmiPlugged);
        if (!(this.mLastSystemUiFlags == 0 && this.mResettingSystemUiFlags == 0 && this.mForceClearedSystemUiFlags == 0)) {
            pw.print(prefix2);
            pw.print("mLastSystemUiFlags=0x");
            pw.print(Integer.toHexString(this.mLastSystemUiFlags));
            pw.print(" mResettingSystemUiFlags=0x");
            pw.print(Integer.toHexString(this.mResettingSystemUiFlags));
            pw.print(" mForceClearedSystemUiFlags=0x");
            pw.println(Integer.toHexString(this.mForceClearedSystemUiFlags));
        }
        if (this.mLastFocusNeedsMenu) {
            pw.print(prefix2);
            pw.print("mLastFocusNeedsMenu=");
            pw.println(this.mLastFocusNeedsMenu);
        }
        pw.print(prefix2);
        pw.print("mShowingDream=");
        pw.print(this.mShowingDream);
        pw.print(" mDreamingLockscreen=");
        pw.print(this.mDreamingLockscreen);
        pw.print(" mDreamingSleepToken=");
        pw.println(this.mDreamingSleepToken);
        if (this.mStatusBar != null) {
            pw.print(prefix2);
            pw.print("mStatusBar=");
            pw.print(this.mStatusBar);
            pw.print(" isStatusBarKeyguard=");
            pw.println(isStatusBarKeyguard());
        }
        if (this.mNavigationBar != null) {
            pw.print(prefix2);
            pw.print("mNavigationBar=");
            pw.println(this.mNavigationBar);
            pw.print(prefix2);
            pw.print("mNavBarOpacityMode=");
            pw.println(this.mNavBarOpacityMode);
            pw.print(prefix2);
            pw.print("mNavigationBarCanMove=");
            pw.println(this.mNavigationBarCanMove);
            pw.print(prefix2);
            pw.print("mNavigationBarPosition=");
            pw.println(this.mNavigationBarPosition);
        }
        if (this.mFocusedWindow != null) {
            pw.print(prefix2);
            pw.print("mFocusedWindow=");
            pw.println(this.mFocusedWindow);
        }
        if (this.mFocusedApp != null) {
            pw.print(prefix2);
            pw.print("mFocusedApp=");
            pw.println(this.mFocusedApp);
        }
        if (this.mTopFullscreenOpaqueWindowState != null) {
            pw.print(prefix2);
            pw.print("mTopFullscreenOpaqueWindowState=");
            pw.println(this.mTopFullscreenOpaqueWindowState);
        }
        if (this.mTopFullscreenOpaqueOrDimmingWindowState != null) {
            pw.print(prefix2);
            pw.print("mTopFullscreenOpaqueOrDimmingWindowState=");
            pw.println(this.mTopFullscreenOpaqueOrDimmingWindowState);
        }
        if (this.mForcingShowNavBar) {
            pw.print(prefix2);
            pw.print("mForcingShowNavBar=");
            pw.println(this.mForcingShowNavBar);
            pw.print(prefix2);
            pw.print("mForcingShowNavBarLayer=");
            pw.println(this.mForcingShowNavBarLayer);
        }
        pw.print(prefix2);
        pw.print("mTopIsFullscreen=");
        pw.print(this.mTopIsFullscreen);
        pw.print(prefix2);
        pw.print("mForceStatusBar=");
        pw.print(this.mForceStatusBar);
        pw.print(" mForceStatusBarFromKeyguard=");
        pw.println(this.mForceStatusBarFromKeyguard);
        pw.print(" mForceShowSystemBarsFromExternal=");
        pw.println(this.mForceShowSystemBarsFromExternal);
        pw.print(prefix2);
        pw.print("mAllowLockscreenWhenOn=");
        pw.println(this.mAllowLockscreenWhenOn);
        this.mStatusBarController.dump(pw, prefix2);
        this.mNavigationBarController.dump(pw, prefix2);
        pw.print(prefix2);
        pw.println("Looper state:");
        this.mHandler.getLooper().dump(new PrintWriterPrinter(pw), prefix2 + "  ");
    }

    private boolean supportsPointerLocation() {
        return this.mDisplayContent.isDefaultDisplay;
    }

    /* access modifiers changed from: package-private */
    public void setPointerLocationEnabled(boolean pointerLocationEnabled) {
        if (supportsPointerLocation()) {
            this.mHandler.sendEmptyMessage(pointerLocationEnabled ? 4 : 5);
        }
    }

    /* access modifiers changed from: private */
    public void enablePointerLocation() {
        if (this.mPointerLocationView == null) {
            this.mPointerLocationView = new PointerLocationView(this.mContext);
            this.mPointerLocationView.setPrintCoords(false);
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams(-1, -1);
            lp.type = 2015;
            lp.flags = IColorFullScreenDisplayManager.CREATE_DISPLAY_FULL_SCREEN_WINDOW;
            lp.layoutInDisplayCutoutMode = 1;
            if (ActivityManager.isHighEndGfx()) {
                lp.flags |= DumpState.DUMP_SERVICE_PERMISSIONS;
                lp.privateFlags |= 2;
            }
            lp.format = -3;
            lp.setTitle("PointerLocation - display " + getDisplayId());
            lp.inputFeatures = lp.inputFeatures | 2;
            ((WindowManager) this.mContext.getSystemService(WindowManager.class)).addView(this.mPointerLocationView, lp);
            this.mDisplayContent.registerPointerEventListener(this.mPointerLocationView);
        }
    }

    /* access modifiers changed from: private */
    public void disablePointerLocation() {
        WindowManagerPolicyConstants.PointerEventListener pointerEventListener = this.mPointerLocationView;
        if (pointerEventListener != null) {
            this.mDisplayContent.unregisterPointerEventListener(pointerEventListener);
            ((WindowManager) this.mContext.getSystemService(WindowManager.class)).removeView(this.mPointerLocationView);
            this.mPointerLocationView = null;
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.OppoBaseDisplayPolicy
    public void adjustOppoWindowFrame(Rect pf, Rect df, Rect of, Rect cf, Rect dcf, Rect vf, WindowManager.LayoutParams attrs, DisplayFrames displayFrames) {
    }

    /* access modifiers changed from: package-private */
    public Handler createPolicyHandler(Looper looper) {
        return new PolicyHandler(looper);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.wm.OppoBaseDisplayPolicy
    public Handler createPolicyHandlerWrapper(Looper looper) {
        return new ColorScreenShotHandler(looper);
    }

    class ColorScreenShotHandler extends PolicyHandler implements IColorScreenshotHelper {
        ColorScreenShotHandler(Looper looper) {
            super(looper);
        }

        @Override // com.android.server.wm.DisplayPolicy.PolicyHandler
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }

        public String getSource() {
            return "KeyPress";
        }

        public boolean isGlobalAction() {
            return DisplayPolicy.this.isGlobalActionVisible();
        }
    }

    class ColorDisplayPolicyInner implements IColorDisplayPolicyInner {
        ColorDisplayPolicyInner() {
        }

        @Override // com.android.server.wm.IColorDisplayPolicyInner
        public Context getContext() {
            return DisplayPolicy.this.mContext;
        }

        @Override // com.android.server.wm.IColorDisplayPolicyInner
        public ScreenshotHelper getScreenshotHelper() {
            return DisplayPolicy.this.mScreenshotHelper;
        }

        @Override // com.android.server.wm.IColorDisplayPolicyInner
        public Object getServiceAcquireLock() {
            return DisplayPolicy.this.mServiceAcquireLock;
        }

        @Override // com.android.server.wm.IColorDisplayPolicyInner
        public WindowState getStatusBar() {
            return DisplayPolicy.this.mStatusBar;
        }

        @Override // com.android.server.wm.IColorDisplayPolicyInner
        public WindowState getNavigationBar() {
            return DisplayPolicy.this.mNavigationBar;
        }

        @Override // com.android.server.wm.IColorDisplayPolicyInner
        public int[] getNavigationBarHeightForRotationDefault() {
            return DisplayPolicy.this.mNavigationBarHeightForRotationDefault;
        }

        @Override // com.android.server.wm.IColorDisplayPolicyInner
        public WindowState getTopFullscreenOpaqueWindowState() {
            return DisplayPolicy.this.mTopFullscreenOpaqueWindowState;
        }

        @Override // com.android.server.wm.IColorDisplayPolicyInner
        public StatusBarManagerInternal getStatusBarManagerInternal() {
            return DisplayPolicy.this.getStatusBarManagerInternal();
        }

        @Override // com.android.server.wm.IColorDisplayPolicyInner
        public Handler getHandler() {
            return DisplayPolicy.this.mHandler;
        }

        @Override // com.android.server.wm.IColorDisplayPolicyInner
        public WindowManagerService getWindowManagerService() {
            return DisplayPolicy.this.mService;
        }
    }
}
