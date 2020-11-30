package com.android.server.wm;

import android.animation.ValueAnimator;
import android.annotation.OppoHook;
import android.app.ActivityManager;
import android.app.ActivityManagerInternal;
import android.app.ActivityTaskManager;
import android.app.ActivityThread;
import android.app.AppOpsManager;
import android.app.IActivityManager;
import android.app.IActivityTaskManager;
import android.app.IAssistDataReceiver;
import android.app.OppoActivityManager;
import android.app.StatusBarManager;
import android.app.WindowConfiguration;
import android.app.admin.DevicePolicyCache;
import android.common.OppoFeatureCache;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManagerInternal;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.Insets;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.hardware.configstore.V1_0.ISurfaceFlingerConfigs;
import android.hardware.configstore.V1_0.OptionalBool;
import android.hardware.display.DisplayManager;
import android.hardware.display.DisplayManagerInternal;
import android.hardware.input.InputManager;
import android.hardware.input.InputManagerInternal;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.HandlerExecutor;
import android.os.IBinder;
import android.os.IRemoteCallback;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.PowerManager;
import android.os.PowerManagerInternal;
import android.os.PowerSaveState;
import android.os.Process;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ServiceManager;
import android.os.ShellCallback;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.SystemService;
import android.os.Trace;
import android.os.UserHandle;
import android.os.WorkSource;
import android.provider.DeviceConfig;
import android.provider.Settings;
import android.service.vr.IVrManager;
import android.service.vr.IVrStateCallbacks;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.DisplayMetrics;
import android.util.EventLog;
import android.util.Log;
import android.util.MergedConfiguration;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.TimeUtils;
import android.util.TypedValue;
import android.util.proto.ProtoOutputStream;
import android.view.Choreographer;
import android.view.Display;
import android.view.DisplayCutout;
import android.view.DisplayInfo;
import android.view.IAppTransitionAnimationSpecsFuture;
import android.view.IDisplayFoldListener;
import android.view.IDockedStackListener;
import android.view.IInputFilter;
import android.view.IOnKeyguardExitResult;
import android.view.IOppoWindowStateObserver;
import android.view.IPinnedStackListener;
import android.view.IRecentsAnimationRunner;
import android.view.IRotationWatcher;
import android.view.ISystemGestureExclusionListener;
import android.view.IWallpaperVisibilityListener;
import android.view.IWindow;
import android.view.IWindowId;
import android.view.IWindowSession;
import android.view.IWindowSessionCallback;
import android.view.InputChannel;
import android.view.InputEvent;
import android.view.InputEventReceiver;
import android.view.InsetsState;
import android.view.KeyEvent;
import android.view.MagnificationSpec;
import android.view.MotionEvent;
import android.view.RemoteAnimationAdapter;
import android.view.SurfaceControl;
import android.view.SurfaceSession;
import android.view.WindowContentFrameStats;
import android.view.WindowManager;
import android.view.WindowManagerPolicyConstants;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.os.BackgroundThread;
import com.android.internal.os.IResultReceiver;
import com.android.internal.policy.IKeyguardDismissCallback;
import com.android.internal.policy.IShortcutService;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.FastPrintWriter;
import com.android.internal.util.LatencyTracker;
import com.android.internal.util.Preconditions;
import com.android.internal.util.function.pooled.PooledLambda;
import com.android.internal.view.WindowManagerPolicyThread;
import com.android.server.AnimationThread;
import com.android.server.ColorServiceFactory;
import com.android.server.DisplayThread;
import com.android.server.EventLogTags;
import com.android.server.FgThread;
import com.android.server.LocalServices;
import com.android.server.LockGuard;
import com.android.server.OppoCheckBlockedException;
import com.android.server.OppoCommonServiceFactory;
import com.android.server.PswServiceFactory;
import com.android.server.UiThread;
import com.android.server.Watchdog;
import com.android.server.am.IColorMultiAppManager;
import com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils;
import com.android.server.display.OppoBrightUtils;
import com.android.server.input.InputManagerService;
import com.android.server.lights.OppoLightsService;
import com.android.server.oppo.OppoAppScaleHelper;
import com.android.server.oppo.OppoCustomizeNotificationHelper;
import com.android.server.pm.DumpState;
import com.android.server.policy.WindowManagerPolicy;
import com.android.server.power.ShutdownThread;
import com.android.server.theia.NoFocusWindow;
import com.android.server.usage.AppStandbyController;
import com.android.server.usb.descriptors.UsbACInterface;
import com.android.server.usb.descriptors.UsbTerminalTypes;
import com.android.server.utils.PriorityDump;
import com.android.server.wm.RecentsAnimationController;
import com.android.server.wm.WindowManagerInternal;
import com.android.server.wm.WindowManagerService;
import com.android.server.wm.WindowState;
import com.color.util.ColorNavigationBarUtil;
import com.mediatek.server.MtkSystemServiceFactory;
import com.mediatek.server.powerhal.PowerHalManager;
import com.mediatek.server.wm.WindowManagerDebugger;
import com.mediatek.server.wm.WmsExt;
import com.oppo.hypnus.Hypnus;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class WindowManagerService extends OppoBaseWindowManagerService implements Watchdog.Monitor, WindowManagerPolicy.WindowManagerFuncs {
    private static final boolean ALWAYS_KEEP_CURRENT = true;
    private static final int ANIMATION_COMPLETED_TIMEOUT_MS = 5000;
    private static final int ANIMATION_DURATION_SCALE = 2;
    static final String APP_FROZEN_TIMEOUT_PROP = "sys.app_freeze_timeout";
    private static final int BOOT_ANIMATION_POLL_INTERVAL = 200;
    private static final String BOOT_ANIMATION_SERVICE = "bootanim";
    static final boolean CUSTOM_SCREEN_ROTATION = true;
    public static boolean DEBUG_COMPAT = false;
    public static final String DEBUG_HIGH_REFRESH_BALCK_LIST = "debug.wms.high_refresh_rate_blacklist";
    static final long DEFAULT_INPUT_DISPATCHING_TIMEOUT_NANOS = 5000000000L;
    private static final String DENSITY_OVERRIDE = "ro.config.density_override";
    private static final int INPUT_DEVICES_READY_FOR_SAFE_MODE_DETECTION_TIMEOUT_MILLIS = 1000;
    static final int LAST_ANR_LIFETIME_DURATION_MSECS = 7200000;
    static final int LAYER_OFFSET_DIM = 1;
    static final int LAYER_OFFSET_THUMBNAIL = 4;
    static final int LAYOUT_REPEAT_THRESHOLD = 4;
    static final int MAX_ANIMATION_DURATION = 10000;
    private static final int MAX_SCREENSHOT_RETRIES = 3;
    private static final int MIN_GESTURE_EXCLUSION_LIMIT_DP = 200;
    private static final long OPPO_FREEZE_TIMEOUT_DEFAULE = 3000;
    static boolean PROFILE_ORIENTATION = false;
    private static final String PROPERTY_EMULATOR_CIRCULAR = "ro.emulator.circular";
    static final int SEAMLESS_ROTATION_TIMEOUT_DURATION = 2000;
    private static final String SIZE_OVERRIDE = "ro.config.size_override";
    private static final String SYSTEM_DEBUGGABLE = "ro.debuggable";
    private static final String SYSTEM_SECURE = "ro.secure";
    static final String TAG = "WindowManager";
    private static final int TRANSITION_ANIMATION_SCALE = 1;
    static final int TYPE_LAYER_MULTIPLIER = 10000;
    static final int TYPE_LAYER_OFFSET = 1000;
    static final int UPDATE_FOCUS_NORMAL = 0;
    static final int UPDATE_FOCUS_PLACING_SURFACES = 2;
    static final int UPDATE_FOCUS_REMOVING_FOCUS = 4;
    static final int UPDATE_FOCUS_WILL_ASSIGN_LAYERS = 1;
    static final int UPDATE_FOCUS_WILL_PLACE_SURFACES = 3;
    static final int WINDOWS_FREEZING_SCREENS_ACTIVE = 1;
    static final int WINDOWS_FREEZING_SCREENS_NONE = 0;
    static final int WINDOWS_FREEZING_SCREENS_TIMEOUT = 2;
    private static final int WINDOW_ANIMATION_SCALE = 0;
    static final int WINDOW_FREEZE_TIMEOUT_DURATION = 2000;
    static final int WINDOW_LAYER_MULTIPLIER = 5;
    static final int WINDOW_REPLACEMENT_TIMEOUT_DURATION = 2000;
    public static boolean localLOGV = WindowManagerDebugConfig.DEBUG;
    static int mDisplayAutoResolutionMode = 0;
    static OppoAppScaleHelper mOppoAppScaleHelper = null;
    private static WindowManagerService sInstance;
    static WindowManagerThreadPriorityBooster sThreadPriorityBooster = new WindowManagerThreadPriorityBooster();
    public boolean isHighRefreshBlackListOn;
    AccessibilityController mAccessibilityController;
    final IActivityManager mActivityManager;
    final WindowManagerInternal.AppTransitionListener mActivityManagerAppTransitionNotifier;
    final IActivityTaskManager mActivityTaskManager;
    final boolean mAllowAnimationsInLowPowerMode;
    final boolean mAllowBootMessages;
    boolean mAllowTheaterModeWakeFromLayout;
    final ActivityManagerInternal mAmInternal;
    final Handler mAnimationHandler;
    final ArrayMap<AnimationAdapter, SurfaceAnimator> mAnimationTransferMap;
    private boolean mAnimationsDisabled;
    final WindowAnimator mAnimator;
    private float mAnimatorDurationScaleSetting;
    final ArrayList<AppFreezeListener> mAppFreezeListeners;
    final AppOpsManager mAppOps;
    int mAppsFreezingScreen;
    final ActivityTaskManagerInternal mAtmInternal;
    final ActivityTaskManagerService mAtmService;
    boolean mBootAnimationStopped;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        /* class com.android.server.wm.WindowManagerService.AnonymousClass2 */

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (((action.hashCode() == 988075300 && action.equals("android.app.action.DEVICE_POLICY_MANAGER_STATE_CHANGED")) ? (char) 0 : 65535) == 0) {
                WindowManagerService.this.mKeyguardDisableHandler.updateKeyguardEnabled(getSendingUserId());
            }
        }
    };
    CircularDisplayMask mCircularDisplayMask;
    boolean mClientFreezingScreen;
    final Context mContext;
    int[] mCurrentProfileIds = new int[0];
    int mCurrentUserId;
    final ArrayList<WindowState> mDestroyPreservedSurface;
    final ArrayList<WindowState> mDestroySurface;
    public boolean mDisableStatusBar;
    boolean mDisableTransitionAnimation;
    boolean mDisplayEnabled;
    long mDisplayFreezeTime;
    boolean mDisplayFrozen;
    final DisplayManager mDisplayManager;
    final DisplayManagerInternal mDisplayManagerInternal;
    boolean mDisplayReady;
    final DisplayWindowSettings mDisplayWindowSettings;
    Rect mDockedStackCreateBounds;
    int mDockedStackCreateMode;
    final DragDropController mDragDropController;
    final long mDrawLockTimeoutMillis;
    EmulatorDisplayOverlay mEmulatorDisplayOverlay;
    private int mEnterAnimId;
    private boolean mEventDispatchingEnabled;
    private int mExitAnimId;
    boolean mFocusMayChange;
    String mFocusingActivity;
    boolean mForceDesktopModeOnExternalDisplays;
    boolean mForceDisplayEnabled;
    final ArrayList<WindowState> mForceRemoves;
    boolean mForceResizableTasks;
    private int mFrozenDisplayId;
    final WindowManagerGlobalLock mGlobalLock;
    final H mH;
    boolean mHardKeyboardAvailable;
    WindowManagerInternal.OnHardKeyboardStatusChangeListener mHardKeyboardStatusChangeListener;
    private boolean mHasHdrSupport;
    final boolean mHasPermanentDpad;
    private boolean mHasWideColorGamutSupport;
    boolean mHideFakeAppLayer;
    private Runnable mHideKeyguardTimeoutRunnable;
    private ArrayList<WindowState> mHidingNonSystemOverlayWindows;
    final HighRefreshRateBlacklist mHighRefreshRateBlacklist;
    private Session mHoldingScreenOn;
    private PowerManager.WakeLock mHoldingScreenWakeLock;
    private Hypnus mHyp = null;
    boolean mInTouchMode;
    final InputManagerService mInputManager;
    final InputManagerCallback mInputManagerCallback;
    private boolean mIsKeyguardWindowHide;
    boolean mIsPc;
    boolean mIsTouchDevice;
    private final KeyguardDisableHandler mKeyguardDisableHandler;
    boolean mKeyguardGoingAway;
    boolean mKeyguardOrAodShowingOnDefaultDisplay;
    String mLastANRState;
    int mLastDisplayFreezeDuration;
    Object mLastFinishedFreezeSource;
    WindowState mLastWakeLockHoldingWindow;
    WindowState mLastWakeLockObscuringWindow;
    private final LatencyTracker mLatencyTracker;
    final boolean mLimitedAlphaCompositing;
    final boolean mLowRamTaskSnapshotsAndRecents;
    final int mMaxUiWidth;
    MousePositionTracker mMousePositionTracker;
    NoFocusWindow mNoFocusWindow;
    final boolean mOnlyCore;
    private final Object mOppoSplitLock = new Object();
    final ArrayList<WindowState> mPendingRemove;
    WindowState[] mPendingRemoveTmp;
    @VisibleForTesting
    boolean mPerDisplayFocusEnabled;
    final PackageManagerInternal mPmInternal;
    boolean mPointerLocationEnabled;
    @VisibleForTesting
    WindowManagerPolicy mPolicy;
    public PowerHalManager mPowerHalManager = MtkSystemServiceFactory.getInstance().makePowerHalManager();
    PowerManager mPowerManager;
    PowerManagerInternal mPowerManagerInternal;
    private final PriorityDump.PriorityDumper mPriorityDumper = new PriorityDump.PriorityDumper() {
        /* class com.android.server.wm.WindowManagerService.AnonymousClass3 */

        @Override // com.android.server.utils.PriorityDump.PriorityDumper
        public void dumpCritical(FileDescriptor fd, PrintWriter pw, String[] args, boolean asProto) {
            if (asProto && WindowManagerService.this.mWindowTracing.isEnabled()) {
                WindowManagerService.this.mWindowTracing.stopTrace(null, false);
                BackgroundThread.getHandler().post(new Runnable() {
                    /* class com.android.server.wm.$$Lambda$WindowManagerService$3$FRNc42I1SE4lD0XFYgIp8RCUXng */

                    public final void run() {
                        WindowManagerService.AnonymousClass3.this.lambda$dumpCritical$0$WindowManagerService$3();
                    }
                });
            }
            WindowManagerService.this.doDump(fd, pw, new String[]{"-a"}, asProto);
        }

        public /* synthetic */ void lambda$dumpCritical$0$WindowManagerService$3() {
            WindowManagerService.this.mWindowTracing.writeTraceToFile();
            WindowManagerService.this.mWindowTracing.startTrace(null);
        }

        @Override // com.android.server.utils.PriorityDump.PriorityDumper
        public void dump(FileDescriptor fd, PrintWriter pw, String[] args, boolean asProto) {
            WindowManagerService.this.doDump(fd, pw, args, asProto);
        }
    };
    final SparseArray<Configuration> mProcessConfigurations;
    private RecentsAnimationController mRecentsAnimationController;
    final ArrayList<WindowState> mResizingWindows;
    RootWindowContainer mRoot;
    private boolean mRotatingSeamlessly;
    ArrayList<RotationWatcher> mRotationWatchers;
    boolean mSafeMode;
    private final PowerManager.WakeLock mScreenFrozenLock;
    IOppoScreenModeManagerFeature mScreenModeFeature;
    private int mSeamlessRotationCount;
    final ArraySet<Session> mSessions;
    SettingsObserver mSettingsObserver;
    boolean mShowAlertWindowNotifications;
    boolean mShowingBootMessages;
    boolean mSimulateWindowFreezing;
    StrictModeFlash mStrictModeFlash;
    boolean mSupportsFreeformWindowManagement;
    boolean mSupportsPictureInPicture;
    final SurfaceAnimationRunner mSurfaceAnimationRunner;
    SurfaceBuilderFactory mSurfaceBuilderFactory;
    SurfaceFactory mSurfaceFactory;
    boolean mSwitchingUser;
    boolean mSystemBooted;
    boolean mSystemGestureExcludedByPreQStickyImmersive;
    int mSystemGestureExclusionLimitDp;
    boolean mSystemReady;
    final TaskPositioningController mTaskPositioningController;
    final TaskSnapshotController mTaskSnapshotController;
    final Configuration mTempConfiguration;
    private WindowContentFrameStats mTempWindowRenderStats;
    final float[] mTmpFloats;
    final Rect mTmpRect;
    final Rect mTmpRect2;
    final Rect mTmpRect3;
    final RectF mTmpRectF;
    final Matrix mTmpTransform;
    private final SurfaceControl.Transaction mTransaction;
    TransactionFactory mTransactionFactory;
    int mTransactionSequence;
    float mTransitionAnimationScaleSetting;
    private ViewServer mViewServer;
    int mVr2dDisplayId = -1;
    boolean mVrModeEnabled = false;
    private final IVrStateCallbacks mVrStateCallbacks = new IVrStateCallbacks.Stub() {
        /* class com.android.server.wm.WindowManagerService.AnonymousClass1 */

        public void onVrStateChanged(boolean enabled) {
            synchronized (WindowManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    WindowManagerService.this.mVrModeEnabled = enabled;
                    WindowManagerService.this.mRoot.forAllDisplayPolicies(PooledLambda.obtainConsumer($$Lambda$h9zRxk6xP2dliCTsIiNVg_lH9kA.INSTANCE, PooledLambda.__(), Boolean.valueOf(enabled)));
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }
    };
    ArrayList<WindowState> mWaitingForDrawn;
    Runnable mWaitingForDrawnCallback;
    final WallpaperVisibilityListeners mWallpaperVisibilityListeners;
    Watermark mWatermark;
    float mWindowAnimationScaleSetting;
    final ArrayList<WindowChangeListener> mWindowChangeListeners;
    public WindowManagerDebugger mWindowManagerDebugger = MtkSystemServiceFactory.getInstance().makeWindowManagerDebugger();
    final WindowHashMap mWindowMap;
    final WindowSurfacePlacer mWindowPlacerLocked;
    final ArrayList<AppWindowToken> mWindowReplacementTimeouts;
    final WindowTracing mWindowTracing;
    boolean mWindowsChanged;
    int mWindowsFreezingScreen;
    private WmsExt mWmsExt;

    interface AppFreezeListener {
        void onAppFreezeTimeout();
    }

    @Retention(RetentionPolicy.SOURCE)
    private @interface UpdateAnimationScaleMode {
    }

    public interface WindowChangeListener {
        void focusChanged();

        void windowsChanged();
    }

    /* access modifiers changed from: package-private */
    public int getDragLayerLocked() {
        return (this.mPolicy.getWindowLayerFromTypeLw(2016) * 10000) + 1000;
    }

    class RotationWatcher {
        final IBinder.DeathRecipient mDeathRecipient;
        final int mDisplayId;
        final IRotationWatcher mWatcher;

        RotationWatcher(IRotationWatcher watcher, IBinder.DeathRecipient deathRecipient, int displayId) {
            this.mWatcher = watcher;
            this.mDeathRecipient = deathRecipient;
            this.mDisplayId = displayId;
        }
    }

    /* access modifiers changed from: private */
    public final class SettingsObserver extends ContentObserver {
        private final Uri mAnimationDurationScaleUri = Settings.Global.getUriFor("animator_duration_scale");
        private final Uri mDisplayAutoResolutionUri = Settings.Secure.getUriFor("app_auto_resolution");
        private final Uri mDisplayInversionEnabledUri = Settings.Secure.getUriFor("accessibility_display_inversion_enabled");
        private final Uri mImmersiveModeConfirmationsUri = Settings.Secure.getUriFor("immersive_mode_confirmations");
        private final Uri mPointerLocationUri = Settings.System.getUriFor("pointer_location");
        private final Uri mPolicyControlUri = Settings.Global.getUriFor("policy_control");
        private final Uri mTransitionAnimationScaleUri = Settings.Global.getUriFor("transition_animation_scale");
        private final Uri mWindowAnimationScaleUri = Settings.Global.getUriFor("window_animation_scale");

        public SettingsObserver() {
            super(new Handler());
            ContentResolver resolver = WindowManagerService.this.mContext.getContentResolver();
            resolver.registerContentObserver(this.mDisplayInversionEnabledUri, false, this, -1);
            resolver.registerContentObserver(this.mWindowAnimationScaleUri, false, this, -1);
            resolver.registerContentObserver(this.mTransitionAnimationScaleUri, false, this, -1);
            resolver.registerContentObserver(this.mAnimationDurationScaleUri, false, this, -1);
            resolver.registerContentObserver(this.mImmersiveModeConfirmationsUri, false, this, -1);
            resolver.registerContentObserver(this.mPolicyControlUri, false, this, -1);
            resolver.registerContentObserver(this.mPointerLocationUri, false, this, -1);
            resolver.registerContentObserver(this.mDisplayAutoResolutionUri, false, this, -1);
        }

        public void onChange(boolean selfChange, Uri uri) {
            int mode;
            if (uri != null) {
                if (this.mImmersiveModeConfirmationsUri.equals(uri) || this.mPolicyControlUri.equals(uri)) {
                    updateSystemUiSettings();
                } else if (this.mDisplayInversionEnabledUri.equals(uri)) {
                    WindowManagerService.this.updateCircularDisplayMaskIfNeeded();
                } else if (this.mPointerLocationUri.equals(uri)) {
                    updatePointerLocation();
                } else {
                    if (this.mDisplayAutoResolutionUri.equals(uri)) {
                        WindowManagerService.mDisplayAutoResolutionMode = Settings.Secure.getIntForUser(WindowManagerService.this.mContext.getContentResolver(), "app_auto_resolution", 0, -2);
                        WindowManagerService.this.mH.removeMessages(66);
                        WindowManagerService.this.mH.sendMessage(WindowManagerService.this.mH.obtainMessage(66));
                        Log.d("WindowManager", "DisplayCompat mDisplayAutoResolutionMode:" + WindowManagerService.mDisplayAutoResolutionMode);
                    }
                    if (this.mWindowAnimationScaleUri.equals(uri)) {
                        mode = 0;
                    } else if (this.mTransitionAnimationScaleUri.equals(uri)) {
                        mode = 1;
                    } else if (this.mAnimationDurationScaleUri.equals(uri)) {
                        mode = 2;
                    } else {
                        return;
                    }
                    WindowManagerService.this.mH.sendMessage(WindowManagerService.this.mH.obtainMessage(51, mode, 0));
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void updateSystemUiSettings() {
            boolean changed;
            synchronized (WindowManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    if (!ImmersiveModeConfirmation.loadSetting(WindowManagerService.this.mCurrentUserId, WindowManagerService.this.mContext)) {
                        if (!PolicyControl.reloadFromSetting(WindowManagerService.this.mContext)) {
                            changed = false;
                        }
                    }
                    changed = true;
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
            if (changed) {
                WindowManagerService.this.updateRotation(false, false);
            }
        }

        /* access modifiers changed from: package-private */
        public void updatePointerLocation() {
            boolean enablePointerLocation = false;
            if (Settings.System.getIntForUser(WindowManagerService.this.mContext.getContentResolver(), "pointer_location", 0, -2) != 0) {
                enablePointerLocation = true;
            }
            if (WindowManagerService.this.mPointerLocationEnabled != enablePointerLocation) {
                WindowManagerService windowManagerService = WindowManagerService.this;
                windowManagerService.mPointerLocationEnabled = enablePointerLocation;
                synchronized (windowManagerService.mGlobalLock) {
                    try {
                        WindowManagerService.boostPriorityForLockedSection();
                        WindowManagerService.this.mRoot.forAllDisplayPolicies(PooledLambda.obtainConsumer($$Lambda$1z_bkwouqOBIC89HKBNNqb1FoaY.INSTANCE, PooledLambda.__(), Boolean.valueOf(WindowManagerService.this.mPointerLocationEnabled)));
                    } finally {
                        WindowManagerService.resetPriorityAfterLockedSection();
                    }
                }
            }
        }
    }

    @Override // com.android.server.policy.WindowManagerPolicy.WindowManagerFuncs
    public void hideKeyguardByFingerprint(boolean isHide) {
        synchronized (this.mWindowMap) {
            Log.d("WindowManager", "hideKeyguardByFingerprint  begin");
            hideKeyguardLocked(isHide, true);
            Log.d("WindowManager", "hideKeyguardByFingerprint  end");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void hideKeyguardLocked(boolean hide, boolean hideAnimator) {
        this.mH.removeCallbacks(this.mHideKeyguardTimeoutRunnable);
    }

    static void boostPriorityForLockedSection() {
        sThreadPriorityBooster.boost();
    }

    static void resetPriorityAfterLockedSection() {
        sThreadPriorityBooster.reset();
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: package-private */
    public void openSurfaceTransaction() {
        try {
            Trace.traceBegin(32, "openSurfaceTransaction");
            synchronized (this.mGlobalLock) {
                try {
                    boostPriorityForLockedSection();
                    SurfaceControl.openTransaction();
                } catch (Throwable th) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
            resetPriorityAfterLockedSection();
        } finally {
            Trace.traceEnd(32);
        }
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: package-private */
    public void closeSurfaceTransaction(String where) {
        try {
            Trace.traceBegin(32, "closeSurfaceTransaction");
            synchronized (this.mGlobalLock) {
                try {
                    boostPriorityForLockedSection();
                    SurfaceControl.closeTransaction();
                    this.mWindowTracing.logState(where);
                } catch (Throwable th) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
            resetPriorityAfterLockedSection();
        } finally {
            Trace.traceEnd(32);
        }
    }

    static WindowManagerService getInstance() {
        return sInstance;
    }

    public static WindowManagerService main(Context context, InputManagerService im, boolean showBootMsgs, boolean onlyCore, WindowManagerPolicy policy, ActivityTaskManagerService atm) {
        return main(context, im, showBootMsgs, onlyCore, policy, atm, $$Lambda$hBnABSAsqXWvQ0zKwHWE4BZ3Mc0.INSTANCE);
    }

    @VisibleForTesting
    public static WindowManagerService main(Context context, InputManagerService im, boolean showBootMsgs, boolean onlyCore, WindowManagerPolicy policy, ActivityTaskManagerService atm, TransactionFactory transactionFactory) {
        DisplayThread.getHandler().runWithScissors(new Runnable(context, im, showBootMsgs, onlyCore, policy, atm, transactionFactory) {
            /* class com.android.server.wm.$$Lambda$WindowManagerService$wGh8jzmWqrd_7ruovSXZoiIk1s0 */
            private final /* synthetic */ Context f$0;
            private final /* synthetic */ InputManagerService f$1;
            private final /* synthetic */ boolean f$2;
            private final /* synthetic */ boolean f$3;
            private final /* synthetic */ WindowManagerPolicy f$4;
            private final /* synthetic */ ActivityTaskManagerService f$5;
            private final /* synthetic */ TransactionFactory f$6;

            {
                this.f$0 = r1;
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
                this.f$5 = r6;
                this.f$6 = r7;
            }

            public final void run() {
                WindowManagerService.sInstance = OppoCommonServiceFactory.getWindowManagerService(this.f$0, this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, new OppoTransactionFactory(this.f$6));
            }
        }, 0);
        return sInstance;
    }

    private void initPolicy() {
        UiThread.getHandler().runWithScissors(new Runnable() {
            /* class com.android.server.wm.WindowManagerService.AnonymousClass6 */

            public void run() {
                WindowManagerPolicyThread.set(Thread.currentThread(), Looper.myLooper());
                WindowManagerPolicy windowManagerPolicy = WindowManagerService.this.mPolicy;
                Context context = WindowManagerService.this.mContext;
                WindowManagerService windowManagerService = WindowManagerService.this;
                windowManagerPolicy.init(context, windowManagerService, windowManagerService);
            }
        }, 0);
    }

    /* JADX DEBUG: Multi-variable search result rejected for r8v0, resolved type: com.android.server.wm.WindowManagerService */
    /* JADX WARN: Multi-variable type inference failed */
    public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ShellCallback callback, ResultReceiver result) {
        new WindowManagerShellCommand(this).exec(this, in, out, err, args, callback, result);
    }

    /* JADX DEBUG: Multi-variable search result rejected for r1v120, resolved type: android.app.AppOpsManager */
    /* JADX DEBUG: Multi-variable search result rejected for r1v121, resolved type: android.app.AppOpsManager */
    /* JADX WARN: Multi-variable type inference failed */
    @OppoHook(level = OppoHook.OppoHookType.CHANGE_ACCESS, note = "JianHui.Yu@ROM.SDK, 2016-12-25 : [-private] Modify for ColorOS Service", property = OppoHook.OppoRomType.ROM)
    WindowManagerService(Context context, InputManagerService inputManager, boolean showBootMsgs, boolean onlyCore, WindowManagerPolicy policy, ActivityTaskManagerService atm, TransactionFactory transactionFactory) {
        boolean z = true;
        this.mShowAlertWindowNotifications = true;
        this.mSessions = new ArraySet<>();
        this.mWindowMap = new WindowHashMap();
        this.mWindowReplacementTimeouts = new ArrayList<>();
        this.mResizingWindows = new ArrayList<>();
        this.mPendingRemove = new ArrayList<>();
        this.mPendingRemoveTmp = new WindowState[20];
        this.mProcessConfigurations = new SparseArray<>();
        this.mDestroySurface = new ArrayList<>();
        this.mDestroyPreservedSurface = new ArrayList<>();
        this.mForceRemoves = new ArrayList<>();
        this.mWaitingForDrawn = new ArrayList<>();
        this.mHidingNonSystemOverlayWindows = new ArrayList<>();
        this.mTmpFloats = new float[9];
        this.mTmpRect = new Rect();
        this.mTmpRect2 = new Rect();
        this.mTmpRect3 = new Rect();
        this.mTmpRectF = new RectF();
        this.mTmpTransform = new Matrix();
        this.mDisplayEnabled = false;
        this.mSystemBooted = false;
        this.mForceDisplayEnabled = false;
        this.mShowingBootMessages = false;
        this.mBootAnimationStopped = false;
        this.mSystemReady = false;
        this.mLastWakeLockHoldingWindow = null;
        this.mLastWakeLockObscuringWindow = null;
        this.mDockedStackCreateMode = 0;
        this.mNoFocusWindow = null;
        this.mRotationWatchers = new ArrayList<>();
        this.mWallpaperVisibilityListeners = new WallpaperVisibilityListeners();
        this.mDisplayFrozen = false;
        this.mDisplayFreezeTime = 0;
        this.mLastDisplayFreezeDuration = 0;
        this.mLastFinishedFreezeSource = null;
        this.mSwitchingUser = false;
        this.mWindowsFreezingScreen = 0;
        this.mClientFreezingScreen = false;
        this.mAppsFreezingScreen = 0;
        this.mH = new H();
        this.mAnimationHandler = new Handler(AnimationThread.getHandler().getLooper());
        this.mSeamlessRotationCount = 0;
        this.mRotatingSeamlessly = false;
        this.mWindowAnimationScaleSetting = 1.0f;
        this.mTransitionAnimationScaleSetting = 1.0f;
        this.mAnimatorDurationScaleSetting = 1.0f;
        this.mAnimationsDisabled = false;
        this.mPointerLocationEnabled = false;
        this.mAnimationTransferMap = new ArrayMap<>();
        this.mWindowChangeListeners = new ArrayList<>();
        this.mWindowsChanged = false;
        this.mTempConfiguration = new Configuration();
        this.mHighRefreshRateBlacklist = HighRefreshRateBlacklist.create();
        this.mIsKeyguardWindowHide = false;
        this.mHideKeyguardTimeoutRunnable = new Runnable() {
            /* class com.android.server.wm.WindowManagerService.AnonymousClass4 */

            public void run() {
                synchronized (WindowManagerService.this.mWindowMap) {
                    Log.d("WindowManager", "run");
                    WindowManagerService.this.hideKeyguardLocked(false, false);
                }
            }
        };
        this.mSurfaceBuilderFactory = $$Lambda$XZU3HlCFtHp_gydNmNMeRmQMCI.INSTANCE;
        this.mTransactionFactory = $$Lambda$hBnABSAsqXWvQ0zKwHWE4BZ3Mc0.INSTANCE;
        this.mSurfaceFactory = $$Lambda$6DEhn1zqxqV5_Ytb_NyzMW23Ano.INSTANCE;
        this.mSimulateWindowFreezing = SystemProperties.getBoolean("persist.simulatewmsfrozen", false);
        this.mActivityManagerAppTransitionNotifier = new WindowManagerInternal.AppTransitionListener() {
            /* class com.android.server.wm.WindowManagerService.AnonymousClass5 */

            @Override // com.android.server.wm.WindowManagerInternal.AppTransitionListener
            public void onAppTransitionCancelledLocked(int transit) {
                WindowManagerService.this.mAtmInternal.notifyAppTransitionCancelled();
            }

            @Override // com.android.server.wm.WindowManagerInternal.AppTransitionListener
            public void onAppTransitionFinishedLocked(IBinder token) {
                WindowManagerService.this.mAtmInternal.notifyAppTransitionFinished();
                AppWindowToken atoken = WindowManagerService.this.mRoot.getAppWindowToken(token);
                if (atoken != null) {
                    if (atoken.mLaunchTaskBehind) {
                        try {
                            WindowManagerService.this.mActivityTaskManager.notifyLaunchTaskBehindComplete(atoken.token);
                        } catch (RemoteException e) {
                        }
                        atoken.mLaunchTaskBehind = false;
                        return;
                    }
                    atoken.updateReportedVisibilityLocked();
                    if (!atoken.mEnteringAnimation) {
                        return;
                    }
                    if (WindowManagerService.this.getRecentsAnimationController() == null || !WindowManagerService.this.getRecentsAnimationController().isTargetApp(atoken)) {
                        atoken.mEnteringAnimation = false;
                        try {
                            ColorAppSwitchManagerService.getInstance().handleAppVisible(atoken.mActivityRecord);
                            WindowManagerService.this.mActivityTaskManager.notifyEnterAnimationComplete(atoken.token);
                        } catch (RemoteException e2) {
                        }
                    }
                }
            }
        };
        this.mAppFreezeListeners = new ArrayList<>();
        this.mInputManagerCallback = new InputManagerCallback(this);
        this.isHighRefreshBlackListOn = false;
        this.mMousePositionTracker = new MousePositionTracker();
        this.mDisableStatusBar = false;
        this.mWmsExt = MtkSystemServiceFactory.getInstance().makeWmsExt();
        this.mScreenModeFeature = null;
        LockGuard.installLock(this, 5);
        this.mGlobalLock = atm.getGlobalLock();
        this.mAtmService = atm;
        this.mContext = context;
        this.mAllowBootMessages = showBootMsgs;
        this.mOnlyCore = onlyCore;
        this.mLimitedAlphaCompositing = context.getResources().getBoolean(17891509);
        this.mHasPermanentDpad = context.getResources().getBoolean(17891464);
        this.mInTouchMode = context.getResources().getBoolean(17891397);
        this.mDrawLockTimeoutMillis = (long) context.getResources().getInteger(17694801);
        this.mAllowAnimationsInLowPowerMode = context.getResources().getBoolean(17891341);
        this.mMaxUiWidth = context.getResources().getInteger(17694837);
        this.mDisableTransitionAnimation = context.getResources().getBoolean(17891408);
        this.mPerDisplayFocusEnabled = context.getResources().getBoolean(17891332);
        this.mLowRamTaskSnapshotsAndRecents = context.getResources().getBoolean(17891477);
        this.mInputManager = inputManager;
        this.mDisplayManagerInternal = (DisplayManagerInternal) LocalServices.getService(DisplayManagerInternal.class);
        this.mDisplayWindowSettings = new DisplayWindowSettings(this);
        this.mTransactionFactory = transactionFactory;
        this.mTransaction = this.mTransactionFactory.make();
        this.mPolicy = policy;
        this.mAnimator = new WindowAnimator(this);
        this.mRoot = new RootWindowContainer(this);
        this.mColorWmsEx = ColorServiceFactory.getInstance().getFeature(IColorWindowManagerServiceEx.DEFAULT, new Object[]{context, this});
        this.mPswWmsEx = PswServiceFactory.getInstance().getFeature(IPswWindowManagerServiceEx.DEFAULT, new Object[]{context, this});
        this.mColorWmsInner = new ColorWindowManagerServiceInner();
        resgisterOppoWindowManagerInternal();
        this.mWindowPlacerLocked = new WindowSurfacePlacer(this);
        this.mTaskSnapshotController = new TaskSnapshotController(this);
        this.mWindowTracing = WindowTracing.createDefaultAndStartLooper(this, Choreographer.getInstance());
        LocalServices.addService(WindowManagerPolicy.class, this.mPolicy);
        this.mDisplayManager = (DisplayManager) context.getSystemService("display");
        this.mKeyguardDisableHandler = KeyguardDisableHandler.create(this.mContext, this.mPolicy, this.mH);
        this.mPowerManager = (PowerManager) context.getSystemService("power");
        this.mPowerManagerInternal = (PowerManagerInternal) LocalServices.getService(PowerManagerInternal.class);
        PowerManagerInternal powerManagerInternal = this.mPowerManagerInternal;
        if (powerManagerInternal != null) {
            powerManagerInternal.registerLowPowerModeObserver(new PowerManagerInternal.LowPowerModeListener() {
                /* class com.android.server.wm.WindowManagerService.AnonymousClass7 */

                public int getServiceType() {
                    return 3;
                }

                public void onLowPowerModeChanged(PowerSaveState result) {
                    synchronized (WindowManagerService.this.mGlobalLock) {
                        try {
                            WindowManagerService.boostPriorityForLockedSection();
                            boolean enabled = result.batterySaverEnabled;
                            if (WindowManagerService.this.mAnimationsDisabled != enabled && !WindowManagerService.this.mAllowAnimationsInLowPowerMode) {
                                WindowManagerService.this.mAnimationsDisabled = enabled;
                                WindowManagerService.this.dispatchNewAnimatorScaleLocked(null);
                            }
                        } finally {
                            WindowManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                }
            });
            this.mAnimationsDisabled = this.mPowerManagerInternal.getLowPowerState(3).batterySaverEnabled;
        }
        this.mScreenFrozenLock = this.mPowerManager.newWakeLock(1, "SCREEN_FROZEN");
        this.mScreenFrozenLock.setReferenceCounted(false);
        this.mActivityManager = ActivityManager.getService();
        this.mActivityTaskManager = ActivityTaskManager.getService();
        this.mAmInternal = (ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class);
        this.mAtmInternal = (ActivityTaskManagerInternal) LocalServices.getService(ActivityTaskManagerInternal.class);
        this.mAppOps = (AppOpsManager) context.getSystemService("appops");
        AppOpsManager.OnOpChangedInternalListener opListener = new AppOpsManager.OnOpChangedInternalListener() {
            /* class com.android.server.wm.WindowManagerService.AnonymousClass8 */

            public void onOpChanged(int op, String packageName) {
                WindowManagerService.this.updateAppOpsState();
            }
        };
        this.mAppOps.startWatchingMode(24, null, opListener);
        this.mAppOps.startWatchingMode(45, null, opListener);
        this.mPmInternal = (PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class);
        IntentFilter suspendPackagesFilter = new IntentFilter();
        suspendPackagesFilter.addAction("android.intent.action.PACKAGES_SUSPENDED");
        suspendPackagesFilter.addAction("android.intent.action.PACKAGES_UNSUSPENDED");
        context.registerReceiverAsUser(new BroadcastReceiver() {
            /* class com.android.server.wm.WindowManagerService.AnonymousClass9 */

            public void onReceive(Context context, Intent intent) {
                String[] affectedPackages = intent.getStringArrayExtra("android.intent.extra.changed_package_list");
                WindowManagerService.this.updateHiddenWhileSuspendedState(new ArraySet(Arrays.asList(affectedPackages)), "android.intent.action.PACKAGES_SUSPENDED".equals(intent.getAction()));
            }
        }, UserHandle.ALL, suspendPackagesFilter, null, null);
        ContentResolver resolver = context.getContentResolver();
        this.mWindowAnimationScaleSetting = Settings.Global.getFloat(resolver, "window_animation_scale", this.mWindowAnimationScaleSetting);
        this.mTransitionAnimationScaleSetting = Settings.Global.getFloat(resolver, "transition_animation_scale", context.getResources().getFloat(17105048));
        setAnimatorDurationScale(Settings.Global.getFloat(resolver, "animator_duration_scale", this.mAnimatorDurationScaleSetting));
        this.mForceDesktopModeOnExternalDisplays = Settings.Global.getInt(resolver, "force_desktop_mode_on_external_displays", 0) == 0 ? false : z;
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.app.action.DEVICE_POLICY_MANAGER_STATE_CHANGED");
        this.mContext.registerReceiverAsUser(this.mBroadcastReceiver, UserHandle.ALL, filter, null, null);
        this.mLatencyTracker = LatencyTracker.getInstance(context);
        this.mSettingsObserver = new SettingsObserver();
        this.mHoldingScreenWakeLock = this.mPowerManager.newWakeLock(536870922, "WindowManager");
        this.mHoldingScreenWakeLock.setReferenceCounted(false);
        this.mSurfaceAnimationRunner = new SurfaceAnimationRunner(this.mPowerManagerInternal);
        this.mAllowTheaterModeWakeFromLayout = context.getResources().getBoolean(17891357);
        this.mTaskPositioningController = new TaskPositioningController(this, this.mInputManager, this.mActivityTaskManager, this.mH.getLooper());
        this.mDragDropController = new DragDropController(this, this.mH.getLooper());
        this.mSystemGestureExclusionLimitDp = Math.max(200, DeviceConfig.getInt("android:window_manager", "system_gesture_exclusion_limit_dp", 0));
        this.mSystemGestureExcludedByPreQStickyImmersive = DeviceConfig.getBoolean("android:window_manager", "system_gestures_excluded_by_pre_q_sticky_immersive", false);
        DeviceConfig.addOnPropertiesChangedListener("android:window_manager", new HandlerExecutor(this.mH), new DeviceConfig.OnPropertiesChangedListener() {
            /* class com.android.server.wm.$$Lambda$WindowManagerService$vZ2iP62NKu_V2Wh0abrxnOgoI */

            public final void onPropertiesChanged(DeviceConfig.Properties properties) {
                WindowManagerService.this.lambda$new$1$WindowManagerService(properties);
            }
        });
        LocalServices.addService(WindowManagerInternal.class, new LocalService());
        OppoWmsFrozenStateWatch wmsFrozenWatch = new OppoWmsFrozenStateWatch();
        wmsFrozenWatch.setWmsInstance(this);
        OppoCheckBlockedException.getInstance().addStateWatch(wmsFrozenWatch);
        mOppoAppScaleHelper = new OppoAppScaleHelper(this.mContext);
        WindowManagerDynamicLogConfig.enableDefaultLogIfNeed();
        this.mNoFocusWindow = NoFocusWindow.getInstance(this.mContext);
    }

    public /* synthetic */ void lambda$new$1$WindowManagerService(DeviceConfig.Properties properties) {
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                int exclusionLimitDp = Math.max(200, properties.getInt("system_gesture_exclusion_limit_dp", 0));
                boolean excludedByPreQSticky = DeviceConfig.getBoolean("android:window_manager", "system_gestures_excluded_by_pre_q_sticky_immersive", false);
                if (!(this.mSystemGestureExcludedByPreQStickyImmersive == excludedByPreQSticky && this.mSystemGestureExclusionLimitDp == exclusionLimitDp)) {
                    this.mSystemGestureExclusionLimitDp = exclusionLimitDp;
                    this.mSystemGestureExcludedByPreQStickyImmersive = excludedByPreQSticky;
                    this.mRoot.forAllDisplays($$Lambda$JQG7CszycLV40zONwvdlvplb1TI.INSTANCE);
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    /* JADX INFO: finally extract failed */
    public void onInitReady() {
        initPolicy();
        Watchdog.getInstance().addMonitor(this);
        openSurfaceTransaction();
        try {
            createWatermarkInTransaction();
            OppoFeatureCache.get(IColorWatermarkManager.DEFAULT).createTalkbackWatermark();
            closeSurfaceTransaction("createWatermarkInTransaction");
            showEmulatorDisplayOverlayIfNeeded();
            mOppoAppScaleHelper.initUpdateBroadcastReceiver();
            mDisplayAutoResolutionMode = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "app_auto_resolution", -1, -2);
            if (this.mContext.getPackageManager().hasSystemFeature("oppo.appautoresolution.support") && mDisplayAutoResolutionMode == -1 && SystemProperties.getInt("ro.oppo.appautoresolution.default", 0) == 1) {
                mDisplayAutoResolutionMode = 1;
                Settings.Secure.putIntForUser(this.mContext.getContentResolver(), "app_auto_resolution", 1, -2);
            }
            if (this.mWmsExt.isAppResolutionTunerSupport()) {
                this.mWmsExt.loadResolutionTunerAppList();
            }
        } catch (Throwable th) {
            closeSurfaceTransaction("createWatermarkInTransaction");
            throw th;
        }
    }

    public InputManagerCallback getInputManagerCallback() {
        return this.mInputManagerCallback;
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        try {
            return super.onTransact(code, data, reply, flags);
        } catch (RuntimeException e) {
            if (!(e instanceof SecurityException)) {
                Slog.wtf("WindowManager", "Window Manager Crash", e);
            }
            throw e;
        }
    }

    static boolean excludeWindowTypeFromTapOutTask(int windowType) {
        if (windowType == 2000 || windowType == 2012 || windowType == 2019) {
            return true;
        }
        return false;
    }

    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:144:0x02eb */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r11v5 */
    /* JADX WARN: Type inference failed for: r11v6 */
    /* JADX WARNING: Code restructure failed: missing block: B:263:0x05ce, code lost:
        if (com.android.server.wm.WindowManagerDebugConfig.DEBUG_ADD_REMOVE != false) goto L_0x05db;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:326:0x06dd, code lost:
        if (r8.mCurrentFocus.mOwnerUid == r11) goto L_0x0709;
     */
    public int addWindow(Session session, IWindow client, int seq, WindowManager.LayoutParams attrs, int viewVisibility, int displayId, Rect outFrame, Rect outContentInsets, Rect outStableInsets, Rect outOutsets, DisplayCutout.ParcelableWrapper outDisplayCutout, InputChannel outInputChannel, InsetsState outInsetsState) {
        WindowManagerGlobalLock windowManagerGlobalLock;
        Throwable th;
        IBinder iBinder;
        DisplayContent displayContent;
        boolean z;
        WindowToken token;
        int callingUid;
        int rootType;
        AppWindowToken atoken;
        DisplayContent displayContent2;
        int i;
        AppWindowToken atoken2;
        Rect taskBounds;
        boolean floatingStack;
        boolean focusChanged;
        char c;
        int type = displayId;
        InputChannel inputChannel = outInputChannel;
        int[] appOp = new int[1];
        int res = this.mPolicy.checkAddPermission(attrs, appOp);
        if (res != 0) {
            return res;
        }
        if (!OppoFeatureCache.get(IColorLockTaskController.DEFAULT).canShowInLockDeviceMode(attrs.type)) {
            return -6;
        }
        boolean reportNewConfig = false;
        WindowState parentWindow = null;
        int callingUid2 = Binder.getCallingUid();
        int type2 = attrs.type;
        WindowManagerGlobalLock windowManagerGlobalLock2 = this.mGlobalLock;
        synchronized (windowManagerGlobalLock2) {
            try {
                boostPriorityForLockedSection();
                if (this.mDisplayReady) {
                    DisplayContent displayContent3 = getDisplayContentOrCreate(type, attrs.token);
                    if (displayContent3 == null) {
                        try {
                            Slog.w("WindowManager", "Attempted to add window to a display that does not exist: " + type + ".  Aborting.");
                            resetPriorityAfterLockedSection();
                            return -9;
                        } catch (Throwable th2) {
                            windowManagerGlobalLock = windowManagerGlobalLock2;
                            th = th2;
                            while (true) {
                                try {
                                    break;
                                } catch (Throwable th3) {
                                    th = th3;
                                }
                            }
                            resetPriorityAfterLockedSection();
                            throw th;
                        }
                    } else if (!displayContent3.hasAccess(session.mUid)) {
                        Slog.w("WindowManager", "Attempted to add window to a display for which the application does not have access: " + type + ".  Aborting.");
                        resetPriorityAfterLockedSection();
                        return -9;
                    } else if (this.mWindowMap.containsKey(client.asBinder())) {
                        try {
                            StringBuilder sb = new StringBuilder();
                            sb.append("Window ");
                            sb.append(client);
                            sb.append(" is already added");
                            Slog.w("WindowManager", sb.toString());
                            resetPriorityAfterLockedSection();
                            return -5;
                        } catch (Throwable th4) {
                            windowManagerGlobalLock = windowManagerGlobalLock2;
                            th = th4;
                            while (true) {
                                break;
                            }
                            resetPriorityAfterLockedSection();
                            throw th;
                        }
                    } else {
                        if (type2 >= 1000 && type2 <= 1999) {
                            parentWindow = windowForClientLocked((Session) null, attrs.token, false);
                            if (parentWindow == null) {
                                Slog.w("WindowManager", "Attempted to add window with token that is not a window: " + attrs.token + ".  Aborting.");
                                resetPriorityAfterLockedSection();
                                return -2;
                            } else if (parentWindow.mAttrs.type >= 1000 && parentWindow.mAttrs.type <= 1999) {
                                Slog.w("WindowManager", "Attempted to add window with token that is a sub-window: " + attrs.token + ".  Aborting.");
                                resetPriorityAfterLockedSection();
                                return -2;
                            }
                        }
                        if (type2 == 2030) {
                            try {
                                if (!displayContent3.isPrivate()) {
                                    Slog.w("WindowManager", "Attempted to add private presentation window to a non-private display.  Aborting.");
                                    resetPriorityAfterLockedSection();
                                    return -8;
                                }
                            } catch (Throwable th5) {
                                th = th5;
                                windowManagerGlobalLock = windowManagerGlobalLock2;
                                while (true) {
                                    break;
                                }
                                resetPriorityAfterLockedSection();
                                throw th;
                            }
                        }
                        if (type2 != 2037 || displayContent3.getDisplay().isPublicPresentation()) {
                            boolean hasParent = parentWindow != null;
                            if (hasParent) {
                                iBinder = parentWindow.mAttrs.token;
                            } else {
                                try {
                                    iBinder = attrs.token;
                                } catch (Throwable th6) {
                                    windowManagerGlobalLock = windowManagerGlobalLock2;
                                    th = th6;
                                    while (true) {
                                        break;
                                    }
                                    resetPriorityAfterLockedSection();
                                    throw th;
                                }
                            }
                            WindowToken token2 = displayContent3.getWindowToken(iBinder);
                            int rootType2 = hasParent ? parentWindow.mAttrs.type : type2;
                            boolean addToastWindowRequiresToken = false;
                            if (token2 != null) {
                                displayContent = displayContent3;
                                windowManagerGlobalLock = windowManagerGlobalLock2;
                                type = type2;
                                int callingUid3 = callingUid2;
                                rootType = rootType2;
                                if (rootType < 1 || rootType > 99) {
                                    if (rootType == 2011) {
                                        if (token2.windowType != 2011) {
                                            Slog.w("WindowManager", "Attempted to add input method window with bad token " + attrs.token + ".  Aborting.");
                                            resetPriorityAfterLockedSection();
                                            return -1;
                                        }
                                        token = token2;
                                        z = true;
                                    } else if (rootType == 2031) {
                                        if (token2.windowType != 2031) {
                                            Slog.w("WindowManager", "Attempted to add voice interaction window with bad token " + attrs.token + ".  Aborting.");
                                            resetPriorityAfterLockedSection();
                                            return -1;
                                        }
                                        token = token2;
                                        z = true;
                                    } else if (rootType == 2013) {
                                        if (token2.windowType != 2013) {
                                            Slog.w("WindowManager", "Attempted to add wallpaper window with bad token " + attrs.token + ".  Aborting.");
                                            resetPriorityAfterLockedSection();
                                            return -1;
                                        }
                                        token = token2;
                                        z = true;
                                    } else if (rootType == 2023) {
                                        if (token2.windowType != 2023) {
                                            Slog.w("WindowManager", "Attempted to add Dream window with bad token " + attrs.token + ".  Aborting.");
                                            resetPriorityAfterLockedSection();
                                            return -1;
                                        }
                                        token = token2;
                                        z = true;
                                    } else if (rootType == 2032) {
                                        if (token2.windowType != 2032) {
                                            Slog.w("WindowManager", "Attempted to add Accessibility overlay window with bad token " + attrs.token + ".  Aborting.");
                                            resetPriorityAfterLockedSection();
                                            return -1;
                                        }
                                        token = token2;
                                        z = true;
                                    } else if (type == 2005) {
                                        addToastWindowRequiresToken = doesAddToastWindowRequireToken(attrs.packageName, callingUid3, parentWindow);
                                        if (addToastWindowRequiresToken) {
                                            c = 2005;
                                            if (token2.windowType != 2005) {
                                                Slog.w("WindowManager", "Attempted to add a toast window with bad token " + attrs.token + ".  Aborting.");
                                                resetPriorityAfterLockedSection();
                                                return -1;
                                            }
                                        } else {
                                            c = 2005;
                                        }
                                        token = token2;
                                        z = true;
                                        atoken = null;
                                        callingUid = callingUid3;
                                    } else if (type != 2035) {
                                        try {
                                            if (token2.asAppWindowToken() != null) {
                                                Slog.w("WindowManager", "Non-null appWindowToken for system window of rootType=" + rootType);
                                                attrs.token = null;
                                                z = true;
                                                token = new WindowToken(this, client.asBinder(), type, false, displayContent, session.mCanAddInternalSystemWindow);
                                                atoken = null;
                                                callingUid = callingUid3;
                                            } else {
                                                token = token2;
                                                z = true;
                                            }
                                        } catch (Throwable th7) {
                                            th = th7;
                                            while (true) {
                                                break;
                                            }
                                            resetPriorityAfterLockedSection();
                                            throw th;
                                        }
                                    } else if (token2.windowType != 2035) {
                                        Slog.w("WindowManager", "Attempted to add QS dialog window with bad token " + attrs.token + ".  Aborting.");
                                        resetPriorityAfterLockedSection();
                                        return -1;
                                    } else {
                                        token = token2;
                                        z = true;
                                    }
                                    atoken = null;
                                    callingUid = callingUid3;
                                } else {
                                    AppWindowToken atoken3 = token2.asAppWindowToken();
                                    if (atoken3 == null) {
                                        Slog.w("WindowManager", "Attempted to add window with non-application token " + token2 + ".  Aborting.");
                                        resetPriorityAfterLockedSection();
                                        return -3;
                                    } else if (atoken3.removed) {
                                        Slog.w("WindowManager", "Attempted to add window with exiting application token " + token2 + ".  Aborting.");
                                        resetPriorityAfterLockedSection();
                                        return -4;
                                    } else if (type != 3 || atoken3.startingWindow == null) {
                                        token = token2;
                                        z = true;
                                        atoken = atoken3;
                                        callingUid = callingUid3;
                                    } else {
                                        Slog.w("WindowManager", "Attempted to add starting window to token with already existing starting window");
                                        resetPriorityAfterLockedSection();
                                        return -5;
                                    }
                                }
                            } else if (rootType2 >= 1 && rootType2 <= 99) {
                                Slog.w("WindowManager", "Attempted to add application window with unknown token " + attrs.token + ".  Aborting.");
                                resetPriorityAfterLockedSection();
                                return -1;
                            } else if (rootType2 == 2011) {
                                Slog.w("WindowManager", "Attempted to add input method window with unknown token " + attrs.token + ".  Aborting.");
                                resetPriorityAfterLockedSection();
                                return -1;
                            } else if (rootType2 == 2031) {
                                Slog.w("WindowManager", "Attempted to add voice interaction window with unknown token " + attrs.token + ".  Aborting.");
                                resetPriorityAfterLockedSection();
                                return -1;
                            } else if (rootType2 == 2013) {
                                Slog.w("WindowManager", "Attempted to add wallpaper window with unknown token " + attrs.token + ".  Aborting.");
                                resetPriorityAfterLockedSection();
                                return -1;
                            } else if (rootType2 == 2023) {
                                Slog.w("WindowManager", "Attempted to add Dream window with unknown token " + attrs.token + ".  Aborting.");
                                resetPriorityAfterLockedSection();
                                return -1;
                            } else if (rootType2 == 2035) {
                                Slog.w("WindowManager", "Attempted to add QS dialog window with unknown token " + attrs.token + ".  Aborting.");
                                resetPriorityAfterLockedSection();
                                return -1;
                            } else if (rootType2 == 2032) {
                                Slog.w("WindowManager", "Attempted to add Accessibility overlay window with unknown token " + attrs.token + ".  Aborting.");
                                resetPriorityAfterLockedSection();
                                return -1;
                            } else if (type2 != 2005 || !doesAddToastWindowRequireToken(attrs.packageName, callingUid2, parentWindow)) {
                                try {
                                    IBinder binder = attrs.token != null ? attrs.token : client.asBinder();
                                    boolean isRoundedCornerOverlay = (attrs.privateFlags & DumpState.DUMP_DEXOPT) != 0;
                                    OppoFeatureCache.get(IColorBreenoManager.DEFAULT).setBreenoState(this.mFocusingActivity);
                                    displayContent = displayContent3;
                                    windowManagerGlobalLock = windowManagerGlobalLock2;
                                    type = type2;
                                    inputChannel = callingUid2;
                                    token = new WindowToken(this, binder, type2, false, displayContent, session.mCanAddInternalSystemWindow, isRoundedCornerOverlay);
                                    atoken = null;
                                    rootType = rootType2;
                                    z = true;
                                    callingUid = inputChannel;
                                } catch (Throwable th8) {
                                    th = th8;
                                    while (true) {
                                        break;
                                    }
                                    resetPriorityAfterLockedSection();
                                    throw th;
                                }
                            } else {
                                Slog.w("WindowManager", "Attempted to add a toast window with unknown token " + attrs.token + ".  Aborting.");
                                resetPriorityAfterLockedSection();
                                return -1;
                            }
                            try {
                                WindowState win = createWindowState(this, session, client, token, parentWindow, appOp[0], seq, attrs, viewVisibility, session.mUid, session.mCanAddInternalSystemWindow);
                                if (!WindowManagerDebugConfig.DEBUG_VISIBILITY) {
                                    try {
                                    } catch (Throwable th9) {
                                        th = th9;
                                        while (true) {
                                            break;
                                        }
                                        resetPriorityAfterLockedSection();
                                        throw th;
                                    }
                                }
                                Slog.v("WindowManager", "addWindow: callingPid " + Binder.getCallingPid() + " callingUid " + callingUid + " win " + win);
                                if (win.mDeathRecipient == null) {
                                    Slog.w("WindowManager", "Adding window client " + client.asBinder() + " that is dead, aborting.");
                                    resetPriorityAfterLockedSection();
                                    return -4;
                                } else if (win.getDisplayContent() == null) {
                                    Slog.w("WindowManager", "Adding window to Display that has been removed.");
                                    resetPriorityAfterLockedSection();
                                    return -9;
                                } else {
                                    DisplayPolicy displayPolicy = displayContent.getDisplayPolicy();
                                    displayPolicy.adjustWindowParamsLw(win, win.mAttrs, Binder.getCallingPid(), Binder.getCallingUid());
                                    win.setShowToOwnerOnlyLocked(this.mPolicy.checkShowToOwnerOnly(attrs));
                                    displayContent.getOppoDisplayModeManager().setVendorPreferredModeId(win);
                                    int res2 = displayPolicy.prepareAddWindowLw(win, attrs);
                                    if (res2 != 0) {
                                        resetPriorityAfterLockedSection();
                                        return res2;
                                    }
                                    if (outInputChannel != null && (attrs.inputFeatures & 2) == 0) {
                                        win.openInputChannel(outInputChannel);
                                    }
                                    if (type == 2005) {
                                        try {
                                            ApplicationInfo aInfo = getApplicationInfo(attrs.packageName, callingUid);
                                            boolean nonSystemAppBeforeNougat = aInfo != null && aInfo.targetSdkVersion < 24 && !aInfo.isSystemApp();
                                            if (!((aInfo == null || (aInfo.oppoPrivateFlags & 2) == 0) ? false : true)) {
                                                if (!getDefaultDisplayContentLocked().canAddToastWindowForUid(callingUid, !nonSystemAppBeforeNougat)) {
                                                    Slog.w("WindowManager", "Adding more than one toast window for UID at a time.");
                                                    resetPriorityAfterLockedSection();
                                                    return -5;
                                                }
                                                if (addToastWindowRequiresToken || (attrs.flags & 8) == 0) {
                                                    displayContent2 = displayContent;
                                                } else {
                                                    displayContent2 = displayContent;
                                                    if (displayContent2.mCurrentFocus != null) {
                                                    }
                                                }
                                                this.mH.sendMessageDelayed(this.mH.obtainMessage(52, win), win.mAttrs.hideTimeoutMilliseconds);
                                            } else {
                                                displayContent2 = displayContent;
                                                Slog.w("WindowManager", "Skip toast check for application in whitelist.");
                                            }
                                        } catch (Throwable th10) {
                                            th = th10;
                                            while (true) {
                                                break;
                                            }
                                            resetPriorityAfterLockedSection();
                                            throw th;
                                        }
                                    } else {
                                        displayContent2 = displayContent;
                                    }
                                    int res3 = 0;
                                    try {
                                        if (displayContent2.mCurrentFocus == null) {
                                            displayContent2.mWinAddedSinceNullFocus.add(win);
                                        }
                                        if (excludeWindowTypeFromTapOutTask(type)) {
                                            displayContent2.mTapExcludedWindows.add(win);
                                        }
                                        long origId = Binder.clearCallingIdentity();
                                        win.attach();
                                        if (!win.isAttachSuccess()) {
                                            Slog.e("WindowManager", "This win has not finish attach, should not Add for map, aborting!");
                                            resetPriorityAfterLockedSection();
                                            return -4;
                                        }
                                        this.mWindowMap.put(client.asBinder(), win);
                                        this.mColorWmsEx.handleWindow(this, this.mAppOps, this.mActivityManager, this.mContext, win, session, client, type);
                                        this.mAtmService.mColorAtmsEx.execInterceptFloatWindow(this, this.mContext, win, this.mAtmService.isKeyguardLocked(), true);
                                        win.setHiddenWhileSuspended(this.mPmInternal.isPackageSuspended(win.getOwningPackage(), UserHandle.getUserId(win.getOwningUid())));
                                        win.setForceHideNonSystemOverlayWindowIfNeeded(!this.mHidingNonSystemOverlayWindows.isEmpty());
                                        AppWindowToken aToken = token.asAppWindowToken();
                                        if (type == 3 && aToken != null) {
                                            try {
                                                aToken.startingWindow = win;
                                                if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
                                                    Slog.v("WindowManager", "addWindow: " + aToken + " startingWindow=" + win);
                                                }
                                            } catch (Throwable th11) {
                                                th = th11;
                                                while (true) {
                                                    break;
                                                }
                                                resetPriorityAfterLockedSection();
                                                throw th;
                                            }
                                        }
                                        boolean imMayMove = true;
                                        win.mToken.addWindow(win);
                                        if (type == 2011) {
                                            displayContent2.setInputMethodWindowLocked(win);
                                            imMayMove = false;
                                        } else if (type == 2012) {
                                            displayContent2.computeImeTarget(true);
                                            imMayMove = false;
                                        } else if (type == 2013) {
                                            displayContent2.mWallpaperController.clearLastWallpaperTimeoutTime();
                                            displayContent2.pendingLayoutChanges |= 4;
                                        } else if ((attrs.flags & DumpState.DUMP_DEXOPT) != 0) {
                                            displayContent2.pendingLayoutChanges |= 4;
                                        } else if (displayContent2.mWallpaperController.isBelowWallpaperTarget(win)) {
                                            displayContent2.pendingLayoutChanges |= 4;
                                        }
                                        win.applyAdjustForImeIfNeeded();
                                        if (type == 2034) {
                                            i = displayId;
                                            try {
                                                this.mRoot.getDisplayContent(i).getDockedDividerController().setWindow(win);
                                            } catch (Throwable th12) {
                                                th = th12;
                                                while (true) {
                                                    break;
                                                }
                                                resetPriorityAfterLockedSection();
                                                throw th;
                                            }
                                        } else {
                                            i = displayId;
                                        }
                                        WindowStateAnimator winAnimator = win.mWinAnimator;
                                        winAnimator.mEnterAnimationPending = true;
                                        winAnimator.mEnteringAnimation = true;
                                        if (atoken == null || !atoken.isVisible()) {
                                            atoken2 = atoken;
                                        } else {
                                            atoken2 = atoken;
                                            if (!prepareWindowReplacementTransition(atoken2)) {
                                                prepareNoneTransitionForRelaunching(atoken2);
                                            }
                                        }
                                        DisplayFrames displayFrames = displayContent2.mDisplayFrames;
                                        DisplayInfo displayInfo = displayContent2.getDisplayInfo();
                                        displayFrames.onDisplayInfoUpdated(displayInfo, displayContent2.calculateDisplayCutoutForRotation(displayInfo.rotation));
                                        if (atoken2 == null || atoken2.getTask() == null) {
                                            floatingStack = false;
                                            taskBounds = null;
                                        } else {
                                            taskBounds = this.mTmpRect;
                                            atoken2.getTask().getBounds(this.mTmpRect);
                                            floatingStack = atoken2.getTask().isFloating();
                                        }
                                        if (displayPolicy.getLayoutHintLw(win.mAttrs, taskBounds, displayFrames, floatingStack, outFrame, outContentInsets, outStableInsets, outOutsets, outDisplayCutout)) {
                                            res3 = 0 | 4;
                                        }
                                        outInsetsState.set(displayContent2.getInsetsStateController().getInsetsForDispatch(win));
                                        if (this.mInTouchMode) {
                                            res3 |= 1;
                                        }
                                        if (win.mAppToken == null || !win.mAppToken.isClientHidden()) {
                                            res3 |= 2;
                                        }
                                        displayContent2.getInputMonitor().setUpdateInputWindowsNeededLw();
                                        if (win.canReceiveKeys()) {
                                            focusChanged = updateFocusedWindowLocked(1, false);
                                            if (focusChanged) {
                                                imMayMove = false;
                                            }
                                        } else {
                                            focusChanged = false;
                                        }
                                        if (imMayMove) {
                                            displayContent2.computeImeTarget(true);
                                        }
                                        win.getParent().assignChildLayers();
                                        if (focusChanged) {
                                            displayContent2.getInputMonitor().setInputFocusLw(displayContent2.mCurrentFocus, false);
                                        }
                                        displayContent2.getInputMonitor().updateInputWindowsLw(false);
                                        if (localLOGV || WindowManagerDebugConfig.DEBUG_ADD_REMOVE) {
                                            Slog.v("WindowManager", "addWindow: New client " + client.asBinder() + ": window=" + win + " Callers=" + Debug.getCallers(5));
                                        }
                                        if (win.isVisibleOrAdding() && displayContent2.updateOrientationFromAppTokens()) {
                                            reportNewConfig = true;
                                        }
                                        resetPriorityAfterLockedSection();
                                        if (reportNewConfig) {
                                            sendNewConfiguration(i);
                                        }
                                        Binder.restoreCallingIdentity(origId);
                                        return res3;
                                    } catch (Throwable th13) {
                                        th = th13;
                                        while (true) {
                                            break;
                                        }
                                        resetPriorityAfterLockedSection();
                                        throw th;
                                    }
                                }
                            } catch (Throwable th14) {
                                th = th14;
                                while (true) {
                                    break;
                                }
                                resetPriorityAfterLockedSection();
                                throw th;
                            }
                        } else {
                            Slog.w("WindowManager", "Attempted to add presentation window to a non-suitable display.  Aborting.");
                            resetPriorityAfterLockedSection();
                            return -9;
                        }
                    }
                } else {
                    windowManagerGlobalLock = windowManagerGlobalLock2;
                    try {
                        throw new IllegalStateException("Display has not been initialialized");
                    } catch (Throwable th15) {
                        th = th15;
                        while (true) {
                            break;
                        }
                        resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
            } catch (Throwable th16) {
                windowManagerGlobalLock = windowManagerGlobalLock2;
                th = th16;
                while (true) {
                    break;
                }
                resetPriorityAfterLockedSection();
                throw th;
            }
        }
    }

    private DisplayContent getDisplayContentOrCreate(int displayId, IBinder token) {
        Display display;
        WindowToken wToken;
        if (token != null && (wToken = this.mRoot.getWindowToken(token)) != null) {
            return wToken.getDisplayContent();
        }
        DisplayContent displayContent = this.mRoot.getDisplayContent(displayId);
        if (displayContent != null || (display = this.mDisplayManager.getDisplay(displayId)) == null) {
            return displayContent;
        }
        return this.mRoot.createDisplayContent(display, null);
    }

    private boolean doesAddToastWindowRequireToken(String packageName, int callingUid, WindowState attachedWindow) {
        if (attachedWindow != null) {
            return attachedWindow.mAppToken != null && attachedWindow.mAppToken.mTargetSdk >= 26;
        }
        try {
            ApplicationInfo appInfo = this.mContext.getPackageManager().getApplicationInfoAsUser(packageName, 0, UserHandle.getUserId(callingUid));
            if (appInfo.uid == callingUid) {
                return appInfo.targetSdkVersion >= 26;
            }
            throw new SecurityException("Package " + packageName + " not in UID " + callingUid);
        } catch (PackageManager.NameNotFoundException e) {
        }
    }

    /* access modifiers changed from: package-private */
    public ApplicationInfo getApplicationInfo(String packageName, int callingUid) {
        try {
            return this.mContext.getPackageManager().getApplicationInfoAsUser(packageName, 0, UserHandle.getUserId(callingUid));
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    private boolean prepareWindowReplacementTransition(AppWindowToken atoken) {
        atoken.clearAllDrawn();
        WindowState replacedWindow = atoken.getReplacingWindow();
        if (replacedWindow == null) {
            return false;
        }
        Rect frame = replacedWindow.getVisibleFrameLw();
        DisplayContent dc = atoken.getDisplayContent();
        dc.mOpeningApps.add(atoken);
        dc.prepareAppTransition(18, true, 0, false);
        dc.mAppTransition.overridePendingAppTransitionClipReveal(frame.left, frame.top, frame.width(), frame.height());
        dc.executeAppTransition();
        return true;
    }

    private void prepareNoneTransitionForRelaunching(AppWindowToken atoken) {
        DisplayContent dc = atoken.getDisplayContent();
        if (this.mDisplayFrozen && !dc.mOpeningApps.contains(atoken) && atoken.isRelaunching()) {
            dc.mOpeningApps.add(atoken);
            dc.prepareAppTransition(0, false, 0, false);
            dc.executeAppTransition();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isSecureLocked(WindowState w) {
        if ((w.mAttrs.flags & 8192) == 0 && !DevicePolicyCache.getInstance().getScreenCaptureDisabled(UserHandle.getUserId(w.mOwnerUid))) {
            return false;
        }
        return true;
    }

    public void refreshScreenCaptureDisabled(int userId) {
        if (Binder.getCallingUid() == 1000) {
            synchronized (this.mGlobalLock) {
                try {
                    boostPriorityForLockedSection();
                    this.mRoot.setSecureSurfaceState(userId, DevicePolicyCache.getInstance().getScreenCaptureDisabled(userId));
                } finally {
                    resetPriorityAfterLockedSection();
                }
            }
            return;
        }
        throw new SecurityException("Only system can call refreshScreenCaptureDisabled.");
    }

    /* access modifiers changed from: package-private */
    public void removeWindow(Session session, IWindow client) {
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                WindowState win = windowForClientLocked(session, client, false);
                if (win != null) {
                    win.removeIfPossible();
                    resetPriorityAfterLockedSection();
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void postWindowRemoveCleanupLocked(WindowState win) {
        if (WindowManagerDebugConfig.DEBUG_ADD_REMOVE) {
            Slog.v("WindowManager", "postWindowRemoveCleanupLocked: " + win);
        }
        this.mWindowMap.remove(win.mClient.asBinder());
        this.mColorWmsEx.sendPopUpNotifyMessage(this, this.mAppOps, this.mActivityManager, this.mContext, win);
        markForSeamlessRotation(win, false);
        win.resetAppOpsState();
        DisplayContent dc = win.getDisplayContent();
        if (dc.mCurrentFocus == null) {
            dc.mWinRemovedSinceNullFocus.add(win);
        }
        this.mPendingRemove.remove(win);
        this.mResizingWindows.remove(win);
        updateNonSystemOverlayWindowsVisibilityIfNeeded(win, false);
        this.mWindowsChanged = true;
        if (WindowManagerDebugConfig.DEBUG_WINDOW_MOVEMENT) {
            Slog.v("WindowManager", "Final remove of window: " + win);
        }
        DisplayContent displayContent = win.getDisplayContent();
        if (displayContent.mInputMethodWindow == win) {
            displayContent.setInputMethodWindowLocked(null);
        }
        WindowToken token = win.mToken;
        AppWindowToken atoken = win.mAppToken;
        if (WindowManagerDebugConfig.DEBUG_ADD_REMOVE) {
            Slog.v("WindowManager", "Removing " + win + " from " + token);
        }
        if (token.isEmpty()) {
            if (!token.mPersistOnEmpty) {
                token.removeImmediately();
            } else if (atoken != null) {
                atoken.firstWindowDrawn = false;
                atoken.clearAllDrawn();
                TaskStack stack = atoken.getStack();
                if (stack != null) {
                    stack.mExitingAppTokens.remove(atoken);
                }
            }
        }
        if (atoken != null) {
            atoken.postWindowRemoveStartingWindowCleanup(win);
        }
        if (win.mAttrs.type == 2013) {
            dc.mWallpaperController.clearLastWallpaperTimeoutTime();
            dc.pendingLayoutChanges |= 4;
        } else if ((win.mAttrs.flags & DumpState.DUMP_DEXOPT) != 0) {
            dc.pendingLayoutChanges |= 4;
        }
        if (!this.mWindowPlacerLocked.isInLayout()) {
            dc.assignWindowLayers(true);
            if (getFocusedWindow() == win) {
                this.mFocusMayChange = true;
            }
            this.mWindowPlacerLocked.performSurfacePlacement();
            if (win.mAppToken != null) {
                win.mAppToken.updateReportedVisibilityLocked();
            }
        }
        dc.getInputMonitor().updateInputWindowsLw(true);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateHiddenWhileSuspendedState(ArraySet<String> packages, boolean suspended) {
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                this.mRoot.updateHiddenWhileSuspendedState(packages, suspended);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateAppOpsState() {
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                this.mRoot.updateAppOpsState();
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    static void logSurface(WindowState w, String msg, boolean withStackTrace) {
        String str = "  SURFACE " + msg + ": " + w;
        if (withStackTrace) {
            logWithStack("WindowManager", str);
        } else {
            Slog.i("WindowManager", str);
        }
    }

    static void logSurface(SurfaceControl s, String title, String msg) {
        Slog.i("WindowManager", "  SURFACE " + s + ": " + msg + " / " + title);
    }

    static void logWithStack(String tag, String s) {
        RuntimeException e = null;
        if (WindowManagerDebugConfig.SHOW_STACK_CRAWLS) {
            e = new RuntimeException();
            e.fillInStackTrace();
        }
        Slog.i(tag, s, e);
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: package-private */
    public void setTransparentRegionWindow(Session session, IWindow client, Region region) {
        long origId = Binder.clearCallingIdentity();
        try {
            synchronized (this.mGlobalLock) {
                try {
                    boostPriorityForLockedSection();
                    WindowState w = windowForClientLocked(session, client, false);
                    if (WindowManagerDebugConfig.SHOW_TRANSACTIONS) {
                        logSurface(w, "transparentRegionHint=" + region, false);
                    }
                    if (w != null && w.mHasSurface) {
                        w.mWinAnimator.setTransparentRegionHintLocked(region);
                    }
                } catch (Throwable th) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
            resetPriorityAfterLockedSection();
        } finally {
            Binder.restoreCallingIdentity(origId);
        }
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: package-private */
    public void setInsetsWindow(Session session, IWindow client, int touchableInsets, Rect contentInsets, Rect visibleInsets, Region touchableRegion) {
        long origId = Binder.clearCallingIdentity();
        try {
            synchronized (this.mGlobalLock) {
                try {
                    boostPriorityForLockedSection();
                    WindowState w = windowForClientLocked(session, client, false);
                    if (WindowManagerDebugConfig.DEBUG_LAYOUT) {
                        Slog.d("WindowManager", "setInsetsWindow " + w + ", contentInsets=" + w.mGivenContentInsets + " -> " + contentInsets + ", visibleInsets=" + w.mGivenVisibleInsets + " -> " + visibleInsets + ", touchableRegion=" + w.mGivenTouchableRegion + " -> " + touchableRegion + ", touchableInsets " + w.mTouchableInsets + " -> " + touchableInsets);
                    }
                    if (w != null) {
                        w.mGivenInsetsPending = false;
                        w.mGivenContentInsets.set(contentInsets);
                        w.mGivenVisibleInsets.set(visibleInsets);
                        w.mGivenTouchableRegion.set(touchableRegion);
                        w.mTouchableInsets = touchableInsets;
                        if (!(w.mGlobalScale == 1.0f || w.getWindowingMode() == WindowConfiguration.WINDOWING_MODE_ZOOM)) {
                            w.mGivenContentInsets.scale(w.mGlobalScale);
                            w.mGivenVisibleInsets.scale(w.mGlobalScale);
                            w.mGivenTouchableRegion.scale(w.mGlobalScale);
                        }
                        w.setDisplayLayoutNeeded();
                        this.mWindowPlacerLocked.performSurfacePlacement();
                        if (this.mAccessibilityController != null && (w.getDisplayContent().getDisplayId() == 0 || w.getDisplayContent().getParentWindow() != null)) {
                            this.mAccessibilityController.onSomeWindowResizedOrMovedLocked();
                        }
                    }
                } catch (Throwable th) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
            resetPriorityAfterLockedSection();
        } finally {
            Binder.restoreCallingIdentity(origId);
        }
    }

    public void getWindowDisplayFrame(Session session, IWindow client, Rect outDisplayFrame) {
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                WindowState win = windowForClientLocked(session, client, false);
                if (win == null) {
                    outDisplayFrame.setEmpty();
                    return;
                }
                outDisplayFrame.set(win.getDisplayFrameLw());
                if (win.inSizeCompatMode()) {
                    outDisplayFrame.scale(win.mInvGlobalScale);
                }
                resetPriorityAfterLockedSection();
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void onRectangleOnScreenRequested(IBinder token, Rect rectangle) {
        WindowState window;
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                if (!(this.mAccessibilityController == null || (window = (WindowState) this.mWindowMap.get(token)) == null)) {
                    this.mAccessibilityController.onRectangleOnScreenRequestedLocked(window.getDisplayId(), rectangle);
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public IWindowId getWindowId(IBinder token) {
        WindowState.WindowId windowId;
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                WindowState window = (WindowState) this.mWindowMap.get(token);
                windowId = window != null ? window.mWindowId : null;
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        return windowId;
    }

    public void pokeDrawLock(Session session, IBinder token) {
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                WindowState window = windowForClientLocked(session, token, false);
                if (window != null) {
                    window.pokeDrawLockLw(this.mDrawLockTimeoutMillis);
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    private boolean hasStatusBarPermission(int pid, int uid) {
        return this.mContext.checkPermission("android.permission.STATUS_BAR", pid, uid) == 0;
    }

    /* JADX WARNING: Removed duplicated region for block: B:169:0x02c3 A[Catch:{ all -> 0x024e, all -> 0x0697 }] */
    /* JADX WARNING: Removed duplicated region for block: B:170:0x02c4 A[Catch:{ all -> 0x024e, all -> 0x0697 }] */
    /* JADX WARNING: Removed duplicated region for block: B:180:0x02db A[Catch:{ all -> 0x024e, all -> 0x0697 }] */
    /* JADX WARNING: Removed duplicated region for block: B:181:0x02dd A[Catch:{ all -> 0x024e, all -> 0x0697 }] */
    /* JADX WARNING: Removed duplicated region for block: B:189:0x02fc A[Catch:{ all -> 0x024e, all -> 0x0697 }] */
    /* JADX WARNING: Removed duplicated region for block: B:194:0x0337 A[Catch:{ all -> 0x024e, all -> 0x0697 }] */
    /* JADX WARNING: Removed duplicated region for block: B:195:0x0339 A[Catch:{ all -> 0x024e, all -> 0x0697 }] */
    /* JADX WARNING: Removed duplicated region for block: B:198:0x0342 A[Catch:{ all -> 0x024e, all -> 0x0697 }] */
    /* JADX WARNING: Removed duplicated region for block: B:199:0x0355 A[Catch:{ all -> 0x024e, all -> 0x0697 }] */
    /* JADX WARNING: Removed duplicated region for block: B:226:0x03c2 A[Catch:{ all -> 0x0691 }] */
    /* JADX WARNING: Removed duplicated region for block: B:253:0x0464  */
    /* JADX WARNING: Removed duplicated region for block: B:272:0x04e1  */
    /* JADX WARNING: Removed duplicated region for block: B:273:0x04e3  */
    /* JADX WARNING: Removed duplicated region for block: B:275:0x04e6  */
    /* JADX WARNING: Removed duplicated region for block: B:278:0x04f1  */
    /* JADX WARNING: Removed duplicated region for block: B:280:0x04f4  */
    /* JADX WARNING: Removed duplicated region for block: B:283:0x0500  */
    /* JADX WARNING: Removed duplicated region for block: B:294:0x0534 A[Catch:{ all -> 0x067a }] */
    /* JADX WARNING: Removed duplicated region for block: B:297:0x053d A[Catch:{ all -> 0x067a }] */
    /* JADX WARNING: Removed duplicated region for block: B:300:0x054a A[Catch:{ all -> 0x067a }] */
    /* JADX WARNING: Removed duplicated region for block: B:303:0x0554 A[Catch:{ all -> 0x067a }] */
    /* JADX WARNING: Removed duplicated region for block: B:304:0x0558 A[Catch:{ all -> 0x067a }] */
    /* JADX WARNING: Removed duplicated region for block: B:306:0x055b A[Catch:{ all -> 0x067a }] */
    /* JADX WARNING: Removed duplicated region for block: B:307:0x0561 A[Catch:{ all -> 0x067a }] */
    /* JADX WARNING: Removed duplicated region for block: B:313:0x05ad  */
    /* JADX WARNING: Removed duplicated region for block: B:319:0x05fa  */
    /* JADX WARNING: Removed duplicated region for block: B:329:0x062a A[Catch:{ all -> 0x06c3 }] */
    /* JADX WARNING: Removed duplicated region for block: B:330:0x062d A[Catch:{ all -> 0x06c3 }] */
    /* JADX WARNING: Removed duplicated region for block: B:333:0x0635 A[Catch:{ all -> 0x06c3 }] */
    /* JADX WARNING: Removed duplicated region for block: B:338:0x0660  */
    /* JADX WARNING: Removed duplicated region for block: B:339:0x0670  */
    public int relayoutWindow(Session session, IWindow client, int seq, WindowManager.LayoutParams attrs, int requestedWidth, int requestedHeight, int viewVisibility, int flags, long frameNumber, Rect outFrame, Rect outOverscanInsets, Rect outContentInsets, Rect outVisibleInsets, Rect outStableInsets, Rect outOutsets, Rect outBackdropFrame, DisplayCutout.ParcelableWrapper outCutout, MergedConfiguration mergedConfiguration, SurfaceControl outSurfaceControl, InsetsState outInsetsState) {
        WindowManagerGlobalLock windowManagerGlobalLock;
        Exception e;
        DisplayPolicy displayPolicy;
        int uid;
        WindowStateAnimator winAnimator;
        int flagChanges;
        int flagChanges2;
        WindowState win;
        WindowStateAnimator winAnimator2;
        int attrChanges;
        boolean z;
        boolean focusMayChange;
        boolean wallpaperMayMove;
        WindowState win2;
        boolean focusMayChange2;
        boolean shouldRelayout;
        boolean focusMayChange3;
        int result;
        int result2;
        DisplayContent displayContent;
        boolean imMayMove;
        boolean toBeDisplayed;
        boolean configChanged;
        MergedConfiguration mergedConfiguration2;
        long j;
        int pid = Binder.getCallingPid();
        int uid2 = Binder.getCallingUid();
        long origId = Binder.clearCallingIdentity();
        WindowManagerGlobalLock windowManagerGlobalLock2 = this.mGlobalLock;
        synchronized (windowManagerGlobalLock2) {
            try {
                boostPriorityForLockedSection();
                WindowState win3 = windowForClientLocked(session, client, false);
                if (win3 == null) {
                    try {
                    } catch (Throwable th) {
                        e = th;
                        windowManagerGlobalLock = windowManagerGlobalLock2;
                        resetPriorityAfterLockedSection();
                        throw e;
                    }
                } else {
                    int displayId = win3.getDisplayId();
                    DisplayContent displayContent2 = win3.getDisplayContent();
                    DisplayPolicy displayPolicy2 = displayContent2.getDisplayPolicy();
                    WindowStateAnimator winAnimator3 = win3.mWinAnimator;
                    if (viewVisibility != 8 || !win3.mRelayoutCalled) {
                        win3.setRequestedSize(requestedWidth, requestedHeight);
                    }
                    try {
                        win3.setFrameNumber(frameNumber);
                        try {
                            if (!win3.getDisplayContent().mWaitingForConfig) {
                                try {
                                    win3.finishSeamlessRotation(false);
                                } catch (Throwable th2) {
                                    e = th2;
                                    windowManagerGlobalLock = windowManagerGlobalLock2;
                                }
                            }
                            if (win3.getWindowingMode() == WindowConfiguration.WINDOWING_MODE_ZOOM && viewVisibility == 0 && win3.mAppToken != null) {
                                if (attrs != null) {
                                    attrs.format = -3;
                                }
                                win3.mAttrs.format = -3;
                            }
                            int flagChanges3 = 0;
                            if (attrs != null) {
                                displayPolicy = displayPolicy2;
                                try {
                                    displayPolicy.adjustWindowParamsLw(win3, attrs, pid, uid2);
                                    if (seq == win3.mSeq) {
                                        int systemUiVisibility = attrs.subtreeSystemUiVisibility | attrs.systemUiVisibility;
                                        if ((67043328 & systemUiVisibility) != 0 && !hasStatusBarPermission(pid, uid2)) {
                                            systemUiVisibility &= -67043329;
                                        }
                                        win3.mSystemUiVisibility = systemUiVisibility;
                                    }
                                    try {
                                        if (win3.mAttrs.type == attrs.type) {
                                            if ((attrs.privateFlags & 8192) != 0) {
                                                attrs.x = win3.mAttrs.x;
                                                attrs.y = win3.mAttrs.y;
                                                attrs.width = win3.mAttrs.width;
                                                attrs.height = win3.mAttrs.height;
                                            }
                                            WindowManager.LayoutParams layoutParams = win3.mAttrs;
                                            int i = attrs.flags ^ layoutParams.flags;
                                            layoutParams.flags = i;
                                            flagChanges3 = i;
                                            int attrChanges2 = win3.mAttrs.copyFrom(attrs);
                                            if ((attrChanges2 & OppoBrightUtils.ADJUSTMENT_GALLERY_IN) != 0) {
                                                win3.mLayoutNeeded = true;
                                            }
                                            if (!(win3.mAppToken == null || ((flagChanges3 & DumpState.DUMP_FROZEN) == 0 && (4194304 & flagChanges3) == 0))) {
                                                win3.mAppToken.checkKeyguardFlagsChanged();
                                            }
                                            if (!((33554432 & attrChanges2) == 0 || this.mAccessibilityController == null || (win3.getDisplayId() != 0 && win3.getDisplayContent().getParentWindow() == null))) {
                                                this.mAccessibilityController.onSomeWindowResizedOrMovedLocked();
                                            }
                                            if ((flagChanges3 & DumpState.DUMP_FROZEN) != 0) {
                                                updateNonSystemOverlayWindowsVisibilityIfNeeded(win3, win3.mWinAnimator.getShown());
                                            }
                                            if ((131072 & attrChanges2) != 0) {
                                                winAnimator = winAnimator3;
                                                winAnimator.setColorSpaceAgnosticLocked((win3.mAttrs.privateFlags & DumpState.DUMP_SERVICE_PERMISSIONS) != 0);
                                            } else {
                                                winAnimator = winAnimator3;
                                            }
                                            uid = attrChanges2;
                                        } else {
                                            try {
                                                this.mWindowManagerDebugger.debugRelayoutWindow("WindowManager", win3, win3.mAttrs.type, attrs.type);
                                                throw new IllegalArgumentException("Window type can not be changed after the window is added.");
                                            } catch (Throwable th3) {
                                                e = th3;
                                                windowManagerGlobalLock = windowManagerGlobalLock2;
                                                resetPriorityAfterLockedSection();
                                                throw e;
                                            }
                                        }
                                    } catch (Throwable th4) {
                                        e = th4;
                                        windowManagerGlobalLock = windowManagerGlobalLock2;
                                        resetPriorityAfterLockedSection();
                                        throw e;
                                    }
                                } catch (Throwable th5) {
                                    e = th5;
                                    windowManagerGlobalLock = windowManagerGlobalLock2;
                                    resetPriorityAfterLockedSection();
                                    throw e;
                                }
                            } else {
                                displayPolicy = displayPolicy2;
                                winAnimator = winAnimator3;
                                uid = 0;
                            }
                            try {
                                if (WindowManagerDebugConfig.DEBUG_LAYOUT || WindowManagerDebugConfig.DEBUG_WMS || WindowManagerDebugConfig.DEBUG_ADD_REMOVE) {
                                    StringBuilder sb = new StringBuilder();
                                    flagChanges = flagChanges3;
                                    sb.append("Relayout ");
                                    sb.append(win3);
                                    sb.append(": viewVisibility=");
                                    sb.append(viewVisibility);
                                    sb.append(" req=");
                                    sb.append(requestedWidth);
                                    sb.append("x");
                                    sb.append(requestedHeight);
                                    sb.append(StringUtils.SPACE);
                                    sb.append(win3.mAttrs);
                                    Slog.v("WindowManager", sb.toString());
                                } else {
                                    flagChanges = flagChanges3;
                                }
                                if (WindowManagerDebugConfig.DEBUG_LAYOUT) {
                                    this.mWindowManagerDebugger.debugInputAttr("WindowManager", attrs);
                                }
                                winAnimator.mSurfaceDestroyDeferred = (flags & 2) != 0;
                                if (!(mDisplayAutoResolutionMode != 1 || win3.mAttrs.packageName == null || mOppoAppScaleHelper == null)) {
                                    float scale = mOppoAppScaleHelper.GetNewScale(win3.mAttrs.packageName);
                                    if (scale > OppoBrightUtils.MIN_LUX_LIMITI && scale != 1.0f) {
                                        win3.mScale = scale;
                                    }
                                }
                                if (this.mWmsExt.isAppResolutionTunerSupport()) {
                                    try {
                                        win = win3;
                                        winAnimator2 = winAnimator;
                                        windowManagerGlobalLock = windowManagerGlobalLock2;
                                        flagChanges2 = flagChanges;
                                        attrChanges = uid;
                                        this.mWmsExt.setWindowScaleByWL(win, win3.getDisplayInfo(), win3.mAttrs, requestedWidth, requestedHeight);
                                    } catch (Throwable th6) {
                                        e = th6;
                                        resetPriorityAfterLockedSection();
                                        throw e;
                                    }
                                } else {
                                    win = win3;
                                    winAnimator2 = winAnimator;
                                    attrChanges = uid;
                                    windowManagerGlobalLock = windowManagerGlobalLock2;
                                    flagChanges2 = flagChanges;
                                }
                                if ((attrChanges & 128) != 0) {
                                    winAnimator2.mAlpha = attrs.alpha;
                                }
                                win.setWindowScale(win.mRequestedWidth, win.mRequestedHeight);
                                if (!(win.mAttrs.surfaceInsets.left == 0 && win.mAttrs.surfaceInsets.top == 0 && win.mAttrs.surfaceInsets.right == 0 && win.mAttrs.surfaceInsets.bottom == 0)) {
                                    winAnimator2.setOpaqueLocked(false);
                                }
                                int oldVisibility = win.mViewVisibility;
                                boolean becameVisible = (oldVisibility == 4 || oldVisibility == 8) && viewVisibility == 0;
                                if ((131080 & flagChanges2) == 0) {
                                    if (!becameVisible) {
                                        z = false;
                                        boolean imMayMove2 = z;
                                        if (win.mViewVisibility == viewVisibility && (flagChanges2 & 8) == 0) {
                                            if (!win.mRelayoutCalled) {
                                                focusMayChange = false;
                                                wallpaperMayMove = (win.mViewVisibility == viewVisibility && (win.mAttrs.flags & DumpState.DUMP_DEXOPT) != 0) | ((1048576 & flagChanges2) == 0);
                                                if (!((flagChanges2 & 8192) == 0 || winAnimator2.mSurfaceController == null)) {
                                                    winAnimator2.mSurfaceController.setSecure(isSecureLocked(win));
                                                }
                                                win.mRelayoutCalled = true;
                                                win.mInRelayout = true;
                                                win.mViewVisibility = viewVisibility;
                                                if (WindowManagerDebugConfig.DEBUG_SCREEN_ON) {
                                                    WindowManagerDebugger windowManagerDebugger = this.mWindowManagerDebugger;
                                                    if (WindowManagerDebugger.WMS_DEBUG_LOG_OFF) {
                                                        RuntimeException stack = new RuntimeException();
                                                        stack.fillInStackTrace();
                                                        Slog.i("WindowManager", "Relayout " + win + ": oldVis=" + oldVisibility + " newVis=" + viewVisibility, stack);
                                                    }
                                                }
                                                win.setDisplayLayoutNeeded();
                                                win.mGivenInsetsPending = (flags & 1) == 0;
                                                WindowManagerDebugger windowManagerDebugger2 = this.mWindowManagerDebugger;
                                                if (!WindowManagerDebugger.WMS_DEBUG_USER) {
                                                    focusMayChange2 = focusMayChange;
                                                    win2 = win;
                                                    this.mWindowManagerDebugger.debugViewVisibility("WindowManager", win, viewVisibility, oldVisibility, focusMayChange2);
                                                } else {
                                                    focusMayChange2 = focusMayChange;
                                                    win2 = win;
                                                }
                                                shouldRelayout = viewVisibility != 0 && (win2.mAppToken == null || win2.mAttrs.type == 3 || !win2.mAppToken.isClientHidden());
                                                if (!shouldRelayout || !winAnimator2.hasSurface() || win2.mAnimatingExit) {
                                                    focusMayChange3 = focusMayChange2;
                                                    result = 0;
                                                } else {
                                                    if (WindowManagerDebugConfig.DEBUG_VISIBILITY) {
                                                        Slog.i("WindowManager", "Relayout invis " + win2 + ": mAnimatingExit=" + win2.mAnimatingExit);
                                                    }
                                                    int result3 = 0 | 4;
                                                    if (!win2.mWillReplaceWindow) {
                                                        focusMayChange3 = tryStartExitingAnimation(win2, winAnimator2, focusMayChange2);
                                                        result = result3;
                                                    } else {
                                                        focusMayChange3 = focusMayChange2;
                                                        result = result3;
                                                    }
                                                }
                                                this.mWindowPlacerLocked.performSurfacePlacement(true);
                                                if (!shouldRelayout) {
                                                    Trace.traceBegin(32, "relayoutWindow: viewVisibility_1");
                                                    try {
                                                        result2 = createSurfaceControl(outSurfaceControl, win2.relayoutVisibleWindow(result, attrChanges), win2, winAnimator2);
                                                        if (WindowManagerDebugConfig.DEBUG_WMS || WindowManagerDebugConfig.DEBUG_FOCUS) {
                                                            Slog.v("WindowManager", "window " + win2 + " need drawFinish: " + (result2 & 2));
                                                        }
                                                        if ((result2 & 2) != 0) {
                                                            focusMayChange3 = true;
                                                        }
                                                        if (win2.mAttrs.type == 2011) {
                                                            displayContent = displayContent2;
                                                            displayContent.setInputMethodWindowLocked(win2);
                                                            imMayMove2 = true;
                                                        } else {
                                                            displayContent = displayContent2;
                                                        }
                                                        win2.adjustStartingWindowFlags();
                                                        Trace.traceEnd(32);
                                                    } catch (Exception e2) {
                                                        displayContent2.getInputMonitor().updateInputWindowsLw(true);
                                                        StringBuilder sb2 = new StringBuilder();
                                                        sb2.append("Exception thrown when creating surface for client ");
                                                        sb2.append(client);
                                                        sb2.append(" (");
                                                        sb2.append((Object) win2.mAttrs.getTitle());
                                                        sb2.append(")");
                                                        Slog.w("WindowManager", sb2.toString(), e2);
                                                        Binder.restoreCallingIdentity(origId);
                                                        resetPriorityAfterLockedSection();
                                                        return 0;
                                                    } catch (Throwable th7) {
                                                        e = th7;
                                                        resetPriorityAfterLockedSection();
                                                        throw e;
                                                    }
                                                } else {
                                                    displayContent = displayContent2;
                                                    Trace.traceBegin(32, "relayoutWindow: viewVisibility_2");
                                                    winAnimator2.mEnterAnimationPending = false;
                                                    winAnimator2.mEnteringAnimation = false;
                                                    if (viewVisibility != 0 || !winAnimator2.hasSurface()) {
                                                        if (WindowManagerDebugConfig.DEBUG_VISIBILITY) {
                                                            Slog.i("WindowManager", "Releasing surface in: " + win2);
                                                        }
                                                        try {
                                                            j = 32;
                                                            Trace.traceBegin(32, "wmReleaseOutSurface_" + ((Object) win2.mAttrs.getTitle()));
                                                            outSurfaceControl.release();
                                                            Trace.traceEnd(32);
                                                        } catch (Throwable th8) {
                                                            e = th8;
                                                            resetPriorityAfterLockedSection();
                                                            throw e;
                                                        }
                                                    } else {
                                                        Trace.traceBegin(32, "relayoutWindow: getSurface");
                                                        winAnimator2.mSurfaceController.getSurfaceControl(outSurfaceControl);
                                                        Trace.traceEnd(32);
                                                        j = 32;
                                                    }
                                                    Trace.traceEnd(j);
                                                    result2 = result;
                                                }
                                                if (focusMayChange3 || !updateFocusedWindowLocked(0, true)) {
                                                    imMayMove = imMayMove2;
                                                } else {
                                                    imMayMove = false;
                                                }
                                                toBeDisplayed = (result2 & 2) == 0;
                                                if (!imMayMove) {
                                                    displayContent.computeImeTarget(true);
                                                    if (toBeDisplayed) {
                                                        displayContent.assignWindowLayers(false);
                                                    }
                                                }
                                                if (wallpaperMayMove) {
                                                    displayContent.pendingLayoutChanges |= 4;
                                                }
                                                if (win2.mAppToken != null) {
                                                    displayContent.mUnknownAppVisibilityController.notifyRelayouted(win2.mAppToken);
                                                }
                                                Trace.traceBegin(32, "relayoutWindow: updateOrientationFromAppTokens");
                                                configChanged = displayContent.updateOrientationFromAppTokens();
                                                Trace.traceEnd(32);
                                                if (!toBeDisplayed && win2.mIsWallpaper) {
                                                    DisplayInfo displayInfo = displayContent.getDisplayInfo();
                                                    displayContent.mWallpaperController.updateWallpaperOffset(win2, displayInfo.logicalWidth, displayInfo.logicalHeight, false);
                                                }
                                                if (win2.mAppToken != null) {
                                                    win2.mAppToken.updateReportedVisibilityLocked();
                                                }
                                                if (winAnimator2.mReportSurfaceResized) {
                                                    winAnimator2.mReportSurfaceResized = false;
                                                    result2 |= 32;
                                                }
                                                if (displayPolicy.areSystemBarsForcedShownLw(win2)) {
                                                    result2 |= 64;
                                                }
                                                if (win2.isGoneForLayoutLw()) {
                                                    win2.mResizedWhileGone = false;
                                                }
                                                if (!shouldRelayout) {
                                                    mergedConfiguration2 = mergedConfiguration;
                                                    win2.getMergedConfiguration(mergedConfiguration2);
                                                } else {
                                                    mergedConfiguration2 = mergedConfiguration;
                                                    win2.getLastReportedMergedConfiguration(mergedConfiguration2);
                                                }
                                                win2.setLastReportedMergedConfiguration(mergedConfiguration2);
                                                win2.updateLastInsetValues();
                                                try {
                                                    win2.getCompatFrame(outFrame);
                                                    win2.getInsetsForRelayout(outOverscanInsets, outContentInsets, outVisibleInsets, outStableInsets, outOutsets);
                                                    outCutout.set(win2.getWmDisplayCutout().getDisplayCutout());
                                                    outBackdropFrame.set(win2.getBackdropFrame(win2.getFrameLw()));
                                                    outInsetsState.set(displayContent.getInsetsStateController().getInsetsForDispatch(win2));
                                                    if (!localLOGV) {
                                                        try {
                                                            StringBuilder sb3 = new StringBuilder();
                                                            sb3.append("Relayout given client ");
                                                            sb3.append(client.asBinder());
                                                            sb3.append(", requestedWidth=");
                                                            sb3.append(requestedWidth);
                                                            sb3.append(", requestedHeight=");
                                                            sb3.append(requestedHeight);
                                                            sb3.append(", viewVisibility=");
                                                            sb3.append(viewVisibility);
                                                            sb3.append("\nRelayout returning frame=");
                                                            sb3.append(outFrame);
                                                            sb3.append(", surface=");
                                                            sb3.append(outSurfaceControl);
                                                            Slog.v("WindowManager", sb3.toString());
                                                        } catch (Throwable th9) {
                                                            e = th9;
                                                            resetPriorityAfterLockedSection();
                                                            throw e;
                                                        }
                                                    }
                                                    if (localLOGV || WindowManagerDebugConfig.DEBUG_FOCUS) {
                                                        Slog.v("WindowManager", "Relayout of " + win2 + ": focusMayChange=" + focusMayChange3);
                                                    }
                                                    int result4 = result2 | (!this.mInTouchMode ? 1 : 0);
                                                    if (WindowManagerDebugConfig.DEBUG_LAYOUT) {
                                                        Slog.v("WindowManager", "Relayout complete " + win2 + ": outFrame=" + outFrame.toShortString());
                                                    }
                                                    win2.mInRelayout = false;
                                                    resetPriorityAfterLockedSection();
                                                    if (!configChanged) {
                                                        Trace.traceBegin(32, "relayoutWindow: sendNewConfiguration");
                                                        sendNewConfiguration(displayId);
                                                        Trace.traceEnd(32);
                                                    }
                                                    Binder.restoreCallingIdentity(origId);
                                                    return result4;
                                                } catch (Throwable th10) {
                                                    e = th10;
                                                    resetPriorityAfterLockedSection();
                                                    throw e;
                                                }
                                            }
                                        }
                                        focusMayChange = true;
                                        wallpaperMayMove = (win.mViewVisibility == viewVisibility && (win.mAttrs.flags & DumpState.DUMP_DEXOPT) != 0) | ((1048576 & flagChanges2) == 0);
                                        winAnimator2.mSurfaceController.setSecure(isSecureLocked(win));
                                        win.mRelayoutCalled = true;
                                        win.mInRelayout = true;
                                        win.mViewVisibility = viewVisibility;
                                        if (WindowManagerDebugConfig.DEBUG_SCREEN_ON) {
                                        }
                                        win.setDisplayLayoutNeeded();
                                        win.mGivenInsetsPending = (flags & 1) == 0;
                                        WindowManagerDebugger windowManagerDebugger22 = this.mWindowManagerDebugger;
                                        if (!WindowManagerDebugger.WMS_DEBUG_USER) {
                                        }
                                        if (viewVisibility != 0) {
                                        }
                                        if (!shouldRelayout) {
                                        }
                                        focusMayChange3 = focusMayChange2;
                                        result = 0;
                                        try {
                                            this.mWindowPlacerLocked.performSurfacePlacement(true);
                                            if (!shouldRelayout) {
                                            }
                                            if (focusMayChange3) {
                                            }
                                            imMayMove = imMayMove2;
                                            if ((result2 & 2) == 0) {
                                            }
                                            if (!imMayMove) {
                                            }
                                            if (wallpaperMayMove) {
                                            }
                                            if (win2.mAppToken != null) {
                                            }
                                        } catch (Throwable th11) {
                                            e = th11;
                                            resetPriorityAfterLockedSection();
                                            throw e;
                                        }
                                        try {
                                            Trace.traceBegin(32, "relayoutWindow: updateOrientationFromAppTokens");
                                            configChanged = displayContent.updateOrientationFromAppTokens();
                                            Trace.traceEnd(32);
                                            if (!toBeDisplayed) {
                                            }
                                            if (win2.mAppToken != null) {
                                            }
                                            if (winAnimator2.mReportSurfaceResized) {
                                            }
                                            if (displayPolicy.areSystemBarsForcedShownLw(win2)) {
                                            }
                                            if (win2.isGoneForLayoutLw()) {
                                            }
                                            if (!shouldRelayout) {
                                            }
                                            win2.setLastReportedMergedConfiguration(mergedConfiguration2);
                                            win2.updateLastInsetValues();
                                            win2.getCompatFrame(outFrame);
                                            win2.getInsetsForRelayout(outOverscanInsets, outContentInsets, outVisibleInsets, outStableInsets, outOutsets);
                                            outCutout.set(win2.getWmDisplayCutout().getDisplayCutout());
                                            outBackdropFrame.set(win2.getBackdropFrame(win2.getFrameLw()));
                                            outInsetsState.set(displayContent.getInsetsStateController().getInsetsForDispatch(win2));
                                            if (!localLOGV) {
                                            }
                                            Slog.v("WindowManager", "Relayout of " + win2 + ": focusMayChange=" + focusMayChange3);
                                        } catch (Throwable th12) {
                                            e = th12;
                                            resetPriorityAfterLockedSection();
                                            throw e;
                                        }
                                        try {
                                            int result42 = result2 | (!this.mInTouchMode ? 1 : 0);
                                            if (WindowManagerDebugConfig.DEBUG_LAYOUT) {
                                            }
                                            win2.mInRelayout = false;
                                            resetPriorityAfterLockedSection();
                                            if (!configChanged) {
                                            }
                                            Binder.restoreCallingIdentity(origId);
                                            return result42;
                                        } catch (Throwable th13) {
                                            e = th13;
                                            resetPriorityAfterLockedSection();
                                            throw e;
                                        }
                                    }
                                }
                                z = true;
                                boolean imMayMove22 = z;
                                if (!win.mRelayoutCalled) {
                                }
                            } catch (Throwable th14) {
                                e = th14;
                                windowManagerGlobalLock = windowManagerGlobalLock2;
                                resetPriorityAfterLockedSection();
                                throw e;
                            }
                        } catch (Throwable th15) {
                            e = th15;
                            windowManagerGlobalLock = windowManagerGlobalLock2;
                            resetPriorityAfterLockedSection();
                            throw e;
                        }
                    } catch (Throwable th16) {
                        e = th16;
                        windowManagerGlobalLock = windowManagerGlobalLock2;
                        resetPriorityAfterLockedSection();
                        throw e;
                    }
                }
            } catch (Throwable th17) {
                e = th17;
                windowManagerGlobalLock = windowManagerGlobalLock2;
                resetPriorityAfterLockedSection();
                throw e;
            }
        }
        resetPriorityAfterLockedSection();
        return 0;
    }

    private boolean tryStartExitingAnimation(WindowState win, WindowStateAnimator winAnimator, boolean focusMayChange) {
        int transit = 2;
        if (win.mAttrs.type == 3) {
            transit = 5;
        }
        if (win.isWinVisibleLw() && winAnimator.applyAnimationLocked(transit, false)) {
            focusMayChange = true;
            win.mAnimatingExit = true;
        } else if (win.isAnimating()) {
            win.mAnimatingExit = true;
        } else if (win.getDisplayContent().mWallpaperController.isWallpaperTarget(win)) {
            win.mAnimatingExit = true;
        } else {
            DisplayContent displayContent = win.getDisplayContent();
            if (displayContent.mInputMethodWindow == win) {
                displayContent.setInputMethodWindowLocked(null);
            }
            boolean stopped = win.mAppToken != null ? win.mAppToken.mAppStopped : true;
            win.mDestroying = true;
            win.destroySurface(false, stopped);
        }
        AccessibilityController accessibilityController = this.mAccessibilityController;
        if (accessibilityController != null) {
            accessibilityController.onWindowTransitionLocked(win, transit);
        }
        SurfaceControl.openTransaction();
        winAnimator.detachChildren();
        SurfaceControl.closeTransaction();
        return focusMayChange;
    }

    /* JADX INFO: finally extract failed */
    private int createSurfaceControl(SurfaceControl outSurfaceControl, int result, WindowState win, WindowStateAnimator winAnimator) {
        if (!win.mHasSurface) {
            result |= 4;
        }
        try {
            Trace.traceBegin(32, "createSurfaceControl");
            WindowSurfaceController surfaceController = winAnimator.createSurfaceLocked(win.mAttrs.type, win.mOwnerUid);
            Trace.traceEnd(32);
            if (surfaceController != null) {
                surfaceController.getSurfaceControl(outSurfaceControl);
                if (WindowManagerDebugConfig.SHOW_TRANSACTIONS) {
                    Slog.i("WindowManager", "  OUT SURFACE " + outSurfaceControl + ": copied");
                }
            } else {
                Slog.w("WindowManager", "Failed to create surface control for " + win);
                outSurfaceControl.release();
            }
            return result;
        } catch (Throwable th) {
            Trace.traceEnd(32);
            throw th;
        }
    }

    /* JADX INFO: finally extract failed */
    public boolean outOfMemoryWindow(Session session, IWindow client) {
        long origId = Binder.clearCallingIdentity();
        try {
            synchronized (this.mGlobalLock) {
                try {
                    boostPriorityForLockedSection();
                    WindowState win = windowForClientLocked(session, client, false);
                    if (win == null) {
                        resetPriorityAfterLockedSection();
                        return false;
                    }
                    boolean reclaimSomeSurfaceMemory = this.mRoot.reclaimSomeSurfaceMemory(win.mWinAnimator, "from-client", false);
                    resetPriorityAfterLockedSection();
                    Binder.restoreCallingIdentity(origId);
                    return reclaimSomeSurfaceMemory;
                } catch (Throwable th) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        } finally {
            Binder.restoreCallingIdentity(origId);
        }
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: package-private */
    public void finishDrawingWindow(Session session, IWindow client) {
        long origId = Binder.clearCallingIdentity();
        try {
            synchronized (this.mGlobalLock) {
                try {
                    boostPriorityForLockedSection();
                    WindowState win = windowForClientLocked(session, client, false);
                    if (WindowManagerDebugConfig.DEBUG_ADD_REMOVE || WindowManagerDebugConfig.DEBUG_WMS) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("finishDrawingWindow: ");
                        sb.append(win);
                        sb.append(" mDrawState=");
                        sb.append(win != null ? win.mWinAnimator.drawStateToString() : "null");
                        Slog.d("WindowManager", sb.toString());
                    }
                    if (win != null && win.mWinAnimator.finishDrawingLocked()) {
                        if ((win.mAttrs.flags & DumpState.DUMP_DEXOPT) != 0) {
                            win.getDisplayContent().pendingLayoutChanges |= 4;
                        }
                        win.setDisplayLayoutNeeded();
                        this.mWindowPlacerLocked.requestTraversal();
                    }
                } catch (Throwable th) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
            resetPriorityAfterLockedSection();
        } finally {
            Binder.restoreCallingIdentity(origId);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean checkCallingPermission(String permission, String func) {
        if (Binder.getCallingPid() == Process.myPid() || this.mContext.checkCallingPermission(permission) == 0) {
            return true;
        }
        Slog.w("WindowManager", "Permission Denial: " + func + " from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " requires " + permission);
        return false;
    }

    public void addWindowToken(IBinder binder, int type, int displayId) {
        if (checkCallingPermission("android.permission.MANAGE_APP_TOKENS", "addWindowToken()")) {
            synchronized (this.mGlobalLock) {
                try {
                    boostPriorityForLockedSection();
                    DisplayContent dc = getDisplayContentOrCreate(displayId, null);
                    if (dc == null) {
                        Slog.w("WindowManager", "addWindowToken: Attempted to add token: " + binder + " for non-exiting displayId=" + displayId);
                        return;
                    }
                    WindowToken token = dc.getWindowToken(binder);
                    if (token != null) {
                        Slog.w("WindowManager", "addWindowToken: Attempted to add binder token: " + binder + " for already created window token: " + token + " displayId=" + displayId);
                        resetPriorityAfterLockedSection();
                        return;
                    }
                    if (type == 2013) {
                        new WallpaperWindowToken(this, binder, true, dc, true);
                    } else {
                        new WindowToken(this, binder, type, true, dc, true);
                    }
                    resetPriorityAfterLockedSection();
                } finally {
                    resetPriorityAfterLockedSection();
                }
            }
        } else {
            throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
        }
    }

    /* JADX INFO: finally extract failed */
    public void removeWindowToken(IBinder binder, int displayId) {
        if (checkCallingPermission("android.permission.MANAGE_APP_TOKENS", "removeWindowToken()")) {
            long origId = Binder.clearCallingIdentity();
            try {
                synchronized (this.mGlobalLock) {
                    try {
                        boostPriorityForLockedSection();
                        DisplayContent dc = this.mRoot.getDisplayContent(displayId);
                        if (dc == null) {
                            Slog.w("WindowManager", "removeWindowToken: Attempted to remove token: " + binder + " for non-exiting displayId=" + displayId);
                            resetPriorityAfterLockedSection();
                        } else if (dc.removeWindowToken(binder) == null) {
                            Slog.w("WindowManager", "removeWindowToken: Attempted to remove non-existing token: " + binder);
                            resetPriorityAfterLockedSection();
                            Binder.restoreCallingIdentity(origId);
                        } else {
                            dc.getInputMonitor().updateInputWindowsLw(true);
                            resetPriorityAfterLockedSection();
                            Binder.restoreCallingIdentity(origId);
                        }
                    } catch (Throwable th) {
                        resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(origId);
            }
        } else {
            throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
        }
    }

    /* access modifiers changed from: package-private */
    public void setNewDisplayOverrideConfiguration(Configuration overrideConfig, DisplayContent dc) {
        this.mColorWmsEx.updataeAccidentPreventionState(this.mContext, false, getDefaultDisplayRotation());
        if (dc.mWaitingForConfig) {
            dc.mWaitingForConfig = false;
            this.mLastFinishedFreezeSource = "new-config";
        }
        this.mRoot.setDisplayOverrideConfigurationIfNeeded(overrideConfig, dc);
    }

    public static float isDisplayCompat(String packageName, int uid) {
        OppoAppScaleHelper oppoAppScaleHelper;
        if (mDisplayAutoResolutionMode != 1 || packageName == null || (oppoAppScaleHelper = mOppoAppScaleHelper) == null) {
            return 1.0f;
        }
        return oppoAppScaleHelper.GetNewScale(packageName);
    }

    public void prepareAppTransition(int transit, boolean alwaysKeepCurrent) {
        if (checkCallingPermission("android.permission.MANAGE_APP_TOKENS", "prepareAppTransition()")) {
            getDefaultDisplayContentLocked().prepareAppTransition(transit, alwaysKeepCurrent, 0, false);
            return;
        }
        throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
    }

    public void overridePendingAppTransitionMultiThumbFuture(IAppTransitionAnimationSpecsFuture specsFuture, IRemoteCallback callback, boolean scaleUp, int displayId) {
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                DisplayContent displayContent = this.mRoot.getDisplayContent(displayId);
                if (displayContent == null) {
                    Slog.w("WindowManager", "Attempted to call overridePendingAppTransitionMultiThumbFuture for the display " + displayId + " that does not exist.");
                    return;
                }
                displayContent.mAppTransition.overridePendingAppTransitionMultiThumbFuture(specsFuture, callback, scaleUp);
                resetPriorityAfterLockedSection();
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void overridePendingAppTransitionRemote(RemoteAnimationAdapter remoteAnimationAdapter, int displayId) {
        if (checkCallingPermission("android.permission.CONTROL_REMOTE_APP_TRANSITION_ANIMATIONS", "overridePendingAppTransitionRemote()")) {
            synchronized (this.mGlobalLock) {
                try {
                    boostPriorityForLockedSection();
                    DisplayContent displayContent = this.mRoot.getDisplayContent(displayId);
                    if (displayContent == null) {
                        Slog.w("WindowManager", "Attempted to call overridePendingAppTransitionRemote for the display " + displayId + " that does not exist.");
                        return;
                    }
                    displayContent.mAppTransition.overridePendingAppTransitionRemote(remoteAnimationAdapter);
                    resetPriorityAfterLockedSection();
                } finally {
                    resetPriorityAfterLockedSection();
                }
            }
        } else {
            throw new SecurityException("Requires CONTROL_REMOTE_APP_TRANSITION_ANIMATIONS permission");
        }
    }

    public void endProlongedAnimations() {
    }

    public void executeAppTransition() {
        if (checkCallingPermission("android.permission.MANAGE_APP_TOKENS", "executeAppTransition()")) {
            getDefaultDisplayContentLocked().executeAppTransition();
            return;
        }
        throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
    }

    public void initializeRecentsAnimation(int targetActivityType, IRecentsAnimationRunner recentsAnimationRunner, RecentsAnimationController.RecentsAnimationCallbacks callbacks, int displayId, SparseBooleanArray recentTaskIds) {
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                this.mRecentsAnimationController = new RecentsAnimationController(this, recentsAnimationRunner, callbacks, displayId);
                this.mRoot.getDisplayContent(displayId).mAppTransition.updateBooster();
                this.mRecentsAnimationController.initialize(targetActivityType, recentTaskIds);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void setRecentsAnimationController(RecentsAnimationController controller) {
        this.mRecentsAnimationController = controller;
    }

    public RecentsAnimationController getRecentsAnimationController() {
        return this.mRecentsAnimationController;
    }

    public boolean canStartRecentsAnimation() {
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                if (getDefaultDisplayContentLocked().mAppTransition.isTransitionSet()) {
                    return false;
                }
                resetPriorityAfterLockedSection();
                return true;
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void cancelRecentsAnimationSynchronously(@RecentsAnimationController.ReorderMode int reorderMode, String reason) {
        RecentsAnimationController recentsAnimationController = this.mRecentsAnimationController;
        if (recentsAnimationController != null) {
            recentsAnimationController.cancelAnimationSynchronously(reorderMode, reason);
        }
    }

    public void cleanupRecentsAnimation(@RecentsAnimationController.ReorderMode int reorderMode) {
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                if (this.mRecentsAnimationController != null) {
                    RecentsAnimationController controller = this.mRecentsAnimationController;
                    this.mRecentsAnimationController = null;
                    controller.cleanupAnimation(reorderMode);
                    getDefaultDisplayContentLocked().mAppTransition.updateBooster();
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void setAppFullscreen(IBinder token, boolean toOpaque) {
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                AppWindowToken atoken = this.mRoot.getAppWindowToken(token);
                if (atoken != null) {
                    atoken.setFillsParent(toOpaque);
                    setWindowOpaqueLocked(token, toOpaque);
                    this.mWindowPlacerLocked.requestTraversal();
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void setWindowOpaque(IBinder token, boolean isOpaque) {
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                setWindowOpaqueLocked(token, isOpaque);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    private void setWindowOpaqueLocked(IBinder token, boolean isOpaque) {
        WindowState win;
        AppWindowToken wtoken = this.mRoot.getAppWindowToken(token);
        if (wtoken != null && (win = wtoken.findMainWindow()) != null) {
            win.mWinAnimator.setOpaqueLocked(isOpaque & (!PixelFormat.formatHasAlpha(win.getAttrs().format)));
        }
    }

    public void setDockedStackCreateState(int mode, Rect bounds) {
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                setDockedStackCreateStateLocked(mode, bounds);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setDockedStackCreateStateLocked(int mode, Rect bounds) {
        this.mDockedStackCreateMode = mode;
        this.mDockedStackCreateBounds = bounds;
    }

    public void checkSplitScreenMinimizedChanged(boolean animate) {
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                getDefaultDisplayContentLocked().getDockedDividerController().checkMinimizeChanged(animate);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public boolean isValidPictureInPictureAspectRatio(int displayId, float aspectRatio) {
        return this.mRoot.getDisplayContent(displayId).getPinnedStackController().isValidPictureInPictureAspectRatio(aspectRatio);
    }

    @Override // com.android.server.policy.WindowManagerPolicy.WindowManagerFuncs
    public void getStackBounds(int windowingMode, int activityType, Rect bounds) {
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                TaskStack stack = this.mRoot.getStack(windowingMode, activityType);
                if (stack != null) {
                    stack.getBounds(bounds);
                    return;
                }
                bounds.setEmpty();
                resetPriorityAfterLockedSection();
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void notifyShowingDreamChanged() {
        notifyKeyguardFlagsChanged(null, 0);
    }

    @Override // com.android.server.policy.WindowManagerPolicy.WindowManagerFuncs
    public WindowManagerPolicy.WindowState getInputMethodWindowLw() {
        return this.mRoot.getCurrentInputMethodWindow();
    }

    @Override // com.android.server.policy.WindowManagerPolicy.WindowManagerFuncs
    public void notifyKeyguardTrustedChanged() {
        this.mAtmInternal.notifyKeyguardTrustedChanged();
    }

    @Override // com.android.server.policy.WindowManagerPolicy.WindowManagerFuncs
    public void screenTurningOff(WindowManagerPolicy.ScreenOffListener listener) {
        OppoFeatureCache.get(IColorFullScreenDisplayManager.DEFAULT).sendMessageToWmService(IColorFullScreenDisplayManager.RESET_DISPLAY_FULL_SCREEN_WINDOW, -1);
        this.mTaskSnapshotController.screenTurningOff(listener);
    }

    @Override // com.android.server.policy.WindowManagerPolicy.WindowManagerFuncs
    public void triggerAnimationFailsafe() {
        this.mH.sendEmptyMessage(60);
    }

    @Override // com.android.server.policy.WindowManagerPolicy.WindowManagerFuncs
    public void onKeyguardShowingAndNotOccludedChanged() {
        this.mH.sendEmptyMessage(61);
    }

    @Override // com.android.server.policy.WindowManagerPolicy.WindowManagerFuncs
    public void onPowerKeyDown(boolean isScreenOn) {
        this.mRoot.forAllDisplayPolicies(PooledLambda.obtainConsumer($$Lambda$99XNq73vh8e4HVH9BuxFhbLxKVY.INSTANCE, PooledLambda.__(), Boolean.valueOf(isScreenOn)));
    }

    @Override // com.android.server.policy.WindowManagerPolicy.WindowManagerFuncs
    public void onUserSwitched() {
        this.mSettingsObserver.updateSystemUiSettings();
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                this.mRoot.forAllDisplayPolicies($$Lambda$_jL5KNK44AQYPj1d8Hd3FYO0WM.INSTANCE);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    @Override // com.android.server.policy.WindowManagerPolicy.WindowManagerFuncs
    public void moveDisplayToTop(int displayId) {
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                DisplayContent displayContent = this.mRoot.getDisplayContent(displayId);
                if (!(displayContent == null || this.mRoot.getTopChild() == displayContent)) {
                    this.mRoot.positionChildAt(Integer.MAX_VALUE, displayContent, true);
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void deferSurfaceLayout() {
        this.mWindowPlacerLocked.deferLayout();
    }

    /* access modifiers changed from: package-private */
    public void continueSurfaceLayout() {
        this.mWindowPlacerLocked.continueLayout();
    }

    /* access modifiers changed from: package-private */
    public void notifyKeyguardFlagsChanged(Runnable callback, int displayId) {
        this.mAtmInternal.notifyKeyguardFlagsChanged(callback, displayId);
    }

    public boolean isKeyguardTrusted() {
        boolean isKeyguardTrustedLw;
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                isKeyguardTrustedLw = this.mPolicy.isKeyguardTrustedLw();
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        return isKeyguardTrustedLw;
    }

    /* JADX INFO: finally extract failed */
    public void setKeyguardGoingAway(boolean keyguardGoingAway) {
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                this.mKeyguardGoingAway = keyguardGoingAway;
            } catch (Throwable th) {
                resetPriorityAfterLockedSection();
                throw th;
            }
        }
        resetPriorityAfterLockedSection();
        this.mAtmService.mColorAtmsEx.execHandleKeyguardGoingAway(keyguardGoingAway);
    }

    public void setKeyguardOrAodShowingOnDefaultDisplay(boolean showing) {
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                this.mKeyguardOrAodShowingOnDefaultDisplay = showing;
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void startFreezingScreen(int exitAnim, int enterAnim) {
        if (checkCallingPermission("android.permission.FREEZE_SCREEN", "startFreezingScreen()")) {
            synchronized (this.mGlobalLock) {
                try {
                    boostPriorityForLockedSection();
                    if (!this.mClientFreezingScreen) {
                        this.mClientFreezingScreen = true;
                        long origId = Binder.clearCallingIdentity();
                        try {
                            startFreezingDisplayLocked(exitAnim, enterAnim);
                            this.mH.removeMessages(30);
                            this.mH.sendEmptyMessageDelayed(30, 5000);
                        } finally {
                            Binder.restoreCallingIdentity(origId);
                        }
                    }
                } finally {
                    resetPriorityAfterLockedSection();
                }
            }
            return;
        }
        throw new SecurityException("Requires FREEZE_SCREEN permission");
    }

    public void stopFreezingScreen() {
        if (checkCallingPermission("android.permission.FREEZE_SCREEN", "stopFreezingScreen()")) {
            synchronized (this.mGlobalLock) {
                try {
                    boostPriorityForLockedSection();
                    if (this.mClientFreezingScreen) {
                        this.mClientFreezingScreen = false;
                        this.mLastFinishedFreezeSource = "client";
                        long origId = Binder.clearCallingIdentity();
                        try {
                            stopFreezingDisplayLocked();
                        } finally {
                            Binder.restoreCallingIdentity(origId);
                        }
                    }
                } finally {
                    resetPriorityAfterLockedSection();
                }
            }
            return;
        }
        throw new SecurityException("Requires FREEZE_SCREEN permission");
    }

    public void disableKeyguard(IBinder token, String tag, int userId) {
        int userId2 = this.mAmInternal.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, false, 2, "disableKeyguard", (String) null);
        if (this.mContext.checkCallingOrSelfPermission("android.permission.DISABLE_KEYGUARD") == 0) {
            int callingUid = Binder.getCallingUid();
            if (!this.mAtmService.mColorAtmsEx.execInterceptDisableKeyguard(this.mContext, callingUid)) {
                long origIdentity = Binder.clearCallingIdentity();
                try {
                    this.mKeyguardDisableHandler.disableKeyguard(token, tag, callingUid, userId2);
                } finally {
                    Binder.restoreCallingIdentity(origIdentity);
                }
            }
        } else {
            throw new SecurityException("Requires DISABLE_KEYGUARD permission");
        }
    }

    public void reenableKeyguard(IBinder token, int userId) {
        int userId2 = this.mAmInternal.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, false, 2, "reenableKeyguard", (String) null);
        if (this.mContext.checkCallingOrSelfPermission("android.permission.DISABLE_KEYGUARD") == 0) {
            Preconditions.checkNotNull(token, "token is null");
            int callingUid = Binder.getCallingUid();
            long origIdentity = Binder.clearCallingIdentity();
            try {
                this.mKeyguardDisableHandler.reenableKeyguard(token, callingUid, userId2);
            } finally {
                Binder.restoreCallingIdentity(origIdentity);
            }
        } else {
            throw new SecurityException("Requires DISABLE_KEYGUARD permission");
        }
    }

    public void exitKeyguardSecurely(final IOnKeyguardExitResult callback) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.DISABLE_KEYGUARD") != 0) {
            throw new SecurityException("Requires DISABLE_KEYGUARD permission");
        } else if (callback != null) {
            this.mPolicy.exitKeyguardSecurely(new WindowManagerPolicy.OnKeyguardExitResult() {
                /* class com.android.server.wm.WindowManagerService.AnonymousClass10 */

                @Override // com.android.server.policy.WindowManagerPolicy.OnKeyguardExitResult
                public void onKeyguardExitResult(boolean success) {
                    try {
                        callback.onKeyguardExitResult(success);
                    } catch (RemoteException e) {
                    }
                }
            });
        } else {
            throw new IllegalArgumentException("callback == null");
        }
    }

    public boolean isKeyguardLocked() {
        return this.mPolicy.isKeyguardLocked();
    }

    public boolean isKeyguardShowingAndNotOccluded() {
        return this.mPolicy.isKeyguardShowingAndNotOccluded();
    }

    public boolean isKeyguardSecure(int userId) {
        if (userId == UserHandle.getCallingUserId() || checkCallingPermission("android.permission.INTERACT_ACROSS_USERS", "isKeyguardSecure")) {
            long origId = Binder.clearCallingIdentity();
            try {
                return this.mPolicy.isKeyguardSecure(userId);
            } finally {
                Binder.restoreCallingIdentity(origId);
            }
        } else {
            throw new SecurityException("Requires INTERACT_ACROSS_USERS permission");
        }
    }

    public boolean isShowingDream() {
        boolean isShowingDreamLw;
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                isShowingDreamLw = getDefaultDisplayContentLocked().getDisplayPolicy().isShowingDreamLw();
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        return isShowingDreamLw;
    }

    public void dismissKeyguard(IKeyguardDismissCallback callback, CharSequence message) {
        if (checkCallingPermission("android.permission.CONTROL_KEYGUARD", "dismissKeyguard")) {
            synchronized (this.mGlobalLock) {
                try {
                    boostPriorityForLockedSection();
                    this.mPolicy.dismissKeyguardLw(callback, message);
                } finally {
                    resetPriorityAfterLockedSection();
                }
            }
            return;
        }
        throw new SecurityException("Requires CONTROL_KEYGUARD permission");
    }

    public void onKeyguardOccludedChanged(boolean occluded) {
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                this.mPolicy.onKeyguardOccludedChangedLw(occluded);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void setSwitchingUser(boolean switching) {
        if (checkCallingPermission("android.permission.INTERACT_ACROSS_USERS_FULL", "setSwitchingUser()")) {
            this.mPolicy.setSwitchingUser(switching);
            synchronized (this.mGlobalLock) {
                try {
                    boostPriorityForLockedSection();
                    this.mSwitchingUser = switching;
                } finally {
                    resetPriorityAfterLockedSection();
                }
            }
            return;
        }
        throw new SecurityException("Requires INTERACT_ACROSS_USERS_FULL permission");
    }

    /* access modifiers changed from: package-private */
    public void showGlobalActions() {
        this.mPolicy.showGlobalActions();
    }

    public void closeSystemDialogs(String reason) {
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                this.mRoot.closeSystemDialogs(reason);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    static float fixScale(float scale) {
        if (scale < OppoBrightUtils.MIN_LUX_LIMITI) {
            scale = OppoBrightUtils.MIN_LUX_LIMITI;
        } else if (scale > 20.0f) {
            scale = 20.0f;
        }
        return Math.abs(scale);
    }

    public void setAnimationScale(int which, float scale) {
        if (checkCallingPermission("android.permission.SET_ANIMATION_SCALE", "setAnimationScale()")) {
            float scale2 = fixScale(scale);
            if (which == 0) {
                this.mWindowAnimationScaleSetting = scale2;
            } else if (which == 1) {
                this.mTransitionAnimationScaleSetting = scale2;
            } else if (which == 2) {
                this.mAnimatorDurationScaleSetting = scale2;
            }
            this.mH.sendEmptyMessage(14);
            return;
        }
        throw new SecurityException("Requires SET_ANIMATION_SCALE permission");
    }

    public void setAnimationScales(float[] scales) {
        if (checkCallingPermission("android.permission.SET_ANIMATION_SCALE", "setAnimationScale()")) {
            if (scales != null) {
                if (scales.length >= 1) {
                    this.mWindowAnimationScaleSetting = fixScale(scales[0]);
                }
                if (scales.length >= 2) {
                    this.mTransitionAnimationScaleSetting = fixScale(scales[1]);
                }
                if (scales.length >= 3) {
                    this.mAnimatorDurationScaleSetting = fixScale(scales[2]);
                    dispatchNewAnimatorScaleLocked(null);
                }
            }
            this.mH.sendEmptyMessage(14);
            return;
        }
        throw new SecurityException("Requires SET_ANIMATION_SCALE permission");
    }

    private void setAnimatorDurationScale(float scale) {
        this.mAnimatorDurationScaleSetting = scale;
        ValueAnimator.setDurationScale(scale);
    }

    public float getWindowAnimationScaleLocked() {
        return this.mAnimationsDisabled ? OppoBrightUtils.MIN_LUX_LIMITI : this.mWindowAnimationScaleSetting;
    }

    public float getTransitionAnimationScaleLocked() {
        return this.mAnimationsDisabled ? OppoBrightUtils.MIN_LUX_LIMITI : this.mTransitionAnimationScaleSetting;
    }

    public float getAnimationScale(int which) {
        if (which == 0) {
            return this.mWindowAnimationScaleSetting;
        }
        if (which == 1) {
            return this.mTransitionAnimationScaleSetting;
        }
        if (which != 2) {
            return OppoBrightUtils.MIN_LUX_LIMITI;
        }
        return this.mAnimatorDurationScaleSetting;
    }

    public float[] getAnimationScales() {
        return new float[]{this.mWindowAnimationScaleSetting, this.mTransitionAnimationScaleSetting, this.mAnimatorDurationScaleSetting};
    }

    public float getCurrentAnimatorScale() {
        float f;
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                f = this.mAnimationsDisabled ? OppoBrightUtils.MIN_LUX_LIMITI : this.mAnimatorDurationScaleSetting;
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        return f;
    }

    /* access modifiers changed from: package-private */
    public void dispatchNewAnimatorScaleLocked(Session session) {
        this.mH.obtainMessage(34, session).sendToTarget();
    }

    @Override // com.android.server.policy.WindowManagerPolicy.WindowManagerFuncs
    public void registerPointerEventListener(WindowManagerPolicyConstants.PointerEventListener listener, int displayId) {
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                DisplayContent displayContent = this.mRoot.getDisplayContent(displayId);
                if (displayContent != null) {
                    displayContent.registerPointerEventListener(listener);
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    @Override // com.android.server.policy.WindowManagerPolicy.WindowManagerFuncs
    public void unregisterPointerEventListener(WindowManagerPolicyConstants.PointerEventListener listener, int displayId) {
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                DisplayContent displayContent = this.mRoot.getDisplayContent(displayId);
                if (displayContent != null) {
                    displayContent.unregisterPointerEventListener(listener);
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    @Override // com.android.server.policy.WindowManagerPolicy.WindowManagerFuncs
    public int getLidState() {
        int sw = this.mInputManager.getSwitchState(-1, -256, 0);
        if (sw > 0) {
            return 0;
        }
        if (sw == 0) {
            return 1;
        }
        return -1;
    }

    @Override // com.android.server.policy.WindowManagerPolicy.WindowManagerFuncs
    public void lockDeviceNow() {
        lockNow(null);
    }

    @Override // com.android.server.policy.WindowManagerPolicy.WindowManagerFuncs
    public int getCameraLensCoverState() {
        int sw = this.mInputManager.getSwitchState(-1, -256, 9);
        if (sw > 0) {
            return 1;
        }
        if (sw == 0) {
            return 0;
        }
        return -1;
    }

    @Override // com.android.server.policy.WindowManagerPolicy.WindowManagerFuncs
    public void switchKeyboardLayout(int deviceId, int direction) {
        this.mInputManager.switchKeyboardLayout(deviceId, direction);
    }

    @Override // com.android.server.policy.WindowManagerPolicy.WindowManagerFuncs
    public void shutdown(boolean confirm) {
        ShutdownThread.shutdown(ActivityThread.currentActivityThread().getSystemUiContext(), "userrequested", confirm);
    }

    @Override // com.android.server.policy.WindowManagerPolicy.WindowManagerFuncs
    public void reboot(boolean confirm) {
        ShutdownThread.reboot(ActivityThread.currentActivityThread().getSystemUiContext(), "userrequested", confirm);
    }

    @Override // com.android.server.policy.WindowManagerPolicy.WindowManagerFuncs
    public void rebootSafeMode(boolean confirm) {
        ShutdownThread.rebootSafeMode(ActivityThread.currentActivityThread().getSystemUiContext(), confirm);
    }

    public void setCurrentProfileIds(int[] currentProfileIds) {
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                this.mCurrentProfileIds = currentProfileIds;
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void setCurrentUser(int newUserId, int[] currentProfileIds) {
        int targetDensity;
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                this.mCurrentUserId = newUserId;
                this.mCurrentProfileIds = currentProfileIds;
                this.mPolicy.setCurrentUserLw(newUserId);
                this.mKeyguardDisableHandler.setCurrentUser(newUserId);
                this.mRoot.switchUser();
                this.mWindowPlacerLocked.performSurfacePlacement();
                DisplayContent displayContent = getDefaultDisplayContentLocked();
                TaskStack stack = displayContent.getSplitScreenPrimaryStackIgnoringVisibility();
                displayContent.mDividerControllerLocked.notifyDockedStackExistsChanged(stack != null && stack.hasTaskForUser(newUserId));
                this.mRoot.forAllDisplays(new Consumer(newUserId) {
                    /* class com.android.server.wm.$$Lambda$WindowManagerService$05fsn8aS3Yh8PJChNK4X3zTgx6M */
                    private final /* synthetic */ int f$0;

                    {
                        this.f$0 = r1;
                    }

                    @Override // java.util.function.Consumer
                    public final void accept(Object obj) {
                        ((DisplayContent) obj).mAppTransition.setCurrentUser(this.f$0);
                    }
                });
                if (this.mDisplayReady) {
                    int forcedDensity = getForcedDisplayDensityForUserLocked(newUserId);
                    if (forcedDensity != 0) {
                        targetDensity = forcedDensity;
                    } else {
                        targetDensity = displayContent.mInitialDisplayDensity;
                    }
                    displayContent.setForcedDensity(targetDensity, -2);
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isCurrentProfileLocked(int userId) {
        if (userId == this.mCurrentUserId || OppoFeatureCache.get(IColorMultiAppManager.DEFAULT).isCurrentProfile(userId)) {
            return true;
        }
        int i = 0;
        while (true) {
            int[] iArr = this.mCurrentProfileIds;
            if (i >= iArr.length) {
                return false;
            }
            if (iArr[i] == userId) {
                return true;
            }
            i++;
        }
    }

    public void enableScreenAfterBoot() {
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                if (WindowManagerDebugConfig.DEBUG_BOOT) {
                    RuntimeException here = new RuntimeException("here");
                    here.fillInStackTrace();
                    Slog.i("WindowManager", "enableScreenAfterBoot: mDisplayEnabled=" + this.mDisplayEnabled + " mForceDisplayEnabled=" + this.mForceDisplayEnabled + " mShowingBootMessages=" + this.mShowingBootMessages + " mSystemBooted=" + this.mSystemBooted, here);
                }
                if (!this.mSystemBooted) {
                    this.mSystemBooted = true;
                    hideBootMessagesLocked();
                    this.mH.sendEmptyMessageDelayed(23, 30000);
                    resetPriorityAfterLockedSection();
                    this.mPolicy.systemBooted();
                    performEnableScreen();
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void enableScreenIfNeeded() {
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                enableScreenIfNeededLocked();
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void enableScreenIfNeededLocked() {
        if (WindowManagerDebugConfig.DEBUG_BOOT) {
            WindowManagerDebugger windowManagerDebugger = this.mWindowManagerDebugger;
            if (WindowManagerDebugger.WMS_DEBUG_LOG_OFF) {
                RuntimeException here = new RuntimeException("here");
                here.fillInStackTrace();
                Slog.i("WindowManager", "enableScreenIfNeededLocked: mDisplayEnabled=" + this.mDisplayEnabled + " mForceDisplayEnabled=" + this.mForceDisplayEnabled + " mShowingBootMessages=" + this.mShowingBootMessages + " mSystemBooted=" + this.mSystemBooted, here);
            }
        }
        if (!this.mDisplayEnabled) {
            if (this.mSystemBooted || this.mShowingBootMessages) {
                this.mH.sendEmptyMessage(16);
            }
        }
    }

    public void performBootTimeout() {
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                if (!this.mDisplayEnabled) {
                    Slog.w("WindowManager", "***** BOOT TIMEOUT: forcing display enabled");
                    this.mForceDisplayEnabled = true;
                    resetPriorityAfterLockedSection();
                    performEnableScreen();
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void onSystemUiStarted() {
        this.mPolicy.onSystemUiStarted();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Code restructure failed: missing block: B:6:0x000e, code lost:
        if (com.mediatek.server.wm.WindowManagerDebugger.WMS_DEBUG_ENG != false) goto L_0x0010;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:72:0x0142, code lost:
        resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:74:?, code lost:
        r9.mActivityManager.bootAnimationComplete();
     */
    private void performEnableScreen() {
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                if (!WindowManagerDebugConfig.DEBUG_BOOT) {
                    WindowManagerDebugger windowManagerDebugger = this.mWindowManagerDebugger;
                }
                Slog.i("WindowManager", "performEnableScreen: mDisplayEnabled=" + this.mDisplayEnabled + " mForceDisplayEnabled=" + this.mForceDisplayEnabled + " mShowingBootMessages=" + this.mShowingBootMessages + " mSystemBooted=" + this.mSystemBooted + " mOnlyCore=" + this.mOnlyCore, new RuntimeException("here").fillInStackTrace());
                if (!this.mDisplayEnabled) {
                    if (!this.mSystemBooted && !this.mShowingBootMessages) {
                        resetPriorityAfterLockedSection();
                        return;
                    } else if (!this.mShowingBootMessages && !this.mPolicy.canDismissBootAnimation()) {
                        if (WindowManagerDebugConfig.DEBUG_BOOT) {
                            Slog.i("WindowManager", " Waiting for mKeyguardDrawComplete");
                        }
                        resetPriorityAfterLockedSection();
                        return;
                    } else if (this.mForceDisplayEnabled || !getDefaultDisplayContentLocked().checkWaitingForWindows()) {
                        if (!this.mBootAnimationStopped) {
                            Trace.asyncTraceBegin(32, "Stop bootanim", 0);
                            SystemProperties.set("service.bootanim.exit", NoFocusWindow.HUNG_CONFIG_ENABLE);
                            if (WindowManagerDebugConfig.DEBUG_BOOT) {
                                Slog.i("WindowManager", " Set prop to stop bootanim");
                            }
                            this.mBootAnimationStopped = true;
                        }
                        if (this.mForceDisplayEnabled || checkBootAnimationCompleteLocked()) {
                            try {
                                IBinder surfaceFlinger = ServiceManager.getService(OppoLightsService.SURFACE_FLINGER);
                                if (surfaceFlinger != null) {
                                    Slog.i("WindowManager", "******* TELLING SURFACE FLINGER WE ARE BOOTED!");
                                    Parcel data = Parcel.obtain();
                                    data.writeInterfaceToken("android.ui.ISurfaceComposer");
                                    surfaceFlinger.transact(1, data, null, 0);
                                    data.recycle();
                                    WindowManagerDebugger windowManagerDebugger2 = this.mWindowManagerDebugger;
                                    if (WindowManagerDebugger.WMS_DEBUG_ENG) {
                                        Slog.d("WindowManager", "Tell SurfaceFlinger finish boot animation");
                                    }
                                }
                            } catch (RemoteException e) {
                                Slog.e("WindowManager", "Boot completed: SurfaceFlinger is dead!");
                            }
                            EventLog.writeEvent((int) EventLogTags.WM_BOOT_ANIMATION_DONE, SystemClock.uptimeMillis());
                            Trace.asyncTraceEnd(32, "Stop bootanim", 0);
                            this.mDisplayEnabled = true;
                            if (WindowManagerDebugConfig.DEBUG_SCREEN_ON || WindowManagerDebugConfig.DEBUG_BOOT) {
                                Slog.i("WindowManager", "******************** ENABLING SCREEN!");
                            }
                            this.mInputManagerCallback.setEventDispatchingLw(this.mEventDispatchingEnabled);
                        } else {
                            if (WindowManagerDebugConfig.DEBUG_BOOT) {
                                Slog.i("WindowManager", "performEnableScreen: Waiting for anim complete");
                            }
                            resetPriorityAfterLockedSection();
                            return;
                        }
                    } else {
                        if (WindowManagerDebugConfig.DEBUG_BOOT) {
                            Slog.i("WindowManager", " Waiting all existing windows have been drawn");
                        }
                        resetPriorityAfterLockedSection();
                        return;
                    }
                } else {
                    return;
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        this.mPolicy.enableScreenAfterBoot();
        updateRotationUnchecked(false, false);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean checkBootAnimationCompleteLocked() {
        if (SystemService.isRunning(BOOT_ANIMATION_SERVICE)) {
            this.mH.removeMessages(37);
            this.mH.sendEmptyMessageDelayed(37, 200);
            if (!WindowManagerDebugConfig.DEBUG_BOOT) {
                return false;
            }
            Slog.i("WindowManager", "checkBootAnimationComplete: Waiting for anim complete");
            return false;
        } else if (!WindowManagerDebugConfig.DEBUG_BOOT) {
            return true;
        } else {
            Slog.i("WindowManager", "checkBootAnimationComplete: Animation complete!");
            return true;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0078, code lost:
        resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x007b, code lost:
        if (r0 == false) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x007d, code lost:
        performEnableScreen();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:?, code lost:
        return;
     */
    public void showBootMessage(CharSequence msg, boolean always) {
        boolean first = false;
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                if (WindowManagerDebugConfig.DEBUG_BOOT) {
                    RuntimeException here = new RuntimeException("here");
                    here.fillInStackTrace();
                    Slog.i("WindowManager", "showBootMessage: msg=" + ((Object) msg) + " always=" + always + " mAllowBootMessages=" + this.mAllowBootMessages + " mShowingBootMessages=" + this.mShowingBootMessages + " mSystemBooted=" + this.mSystemBooted, here);
                }
                if (this.mAllowBootMessages) {
                    if (!this.mShowingBootMessages) {
                        if (!always) {
                            resetPriorityAfterLockedSection();
                            return;
                        }
                        first = true;
                    }
                    if (this.mSystemBooted) {
                        resetPriorityAfterLockedSection();
                    } else {
                        this.mShowingBootMessages = true;
                        this.mPolicy.showBootMessage(msg, always);
                    }
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void hideBootMessagesLocked() {
        if (WindowManagerDebugConfig.DEBUG_BOOT) {
            RuntimeException here = new RuntimeException("here");
            here.fillInStackTrace();
            Slog.i("WindowManager", "hideBootMessagesLocked: mDisplayEnabled=" + this.mDisplayEnabled + " mForceDisplayEnabled=" + this.mForceDisplayEnabled + " mShowingBootMessages=" + this.mShowingBootMessages + " mSystemBooted=" + this.mSystemBooted, here);
        }
        if (this.mShowingBootMessages) {
            this.mShowingBootMessages = false;
            this.mPolicy.hideBootMessages();
        }
    }

    public void setInTouchMode(boolean mode) {
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                this.mInTouchMode = mode;
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateCircularDisplayMaskIfNeeded() {
        int currentUserId;
        if (this.mContext.getResources().getConfiguration().isScreenRound() && this.mContext.getResources().getBoolean(17891601)) {
            synchronized (this.mGlobalLock) {
                try {
                    boostPriorityForLockedSection();
                    currentUserId = this.mCurrentUserId;
                } catch (Throwable th) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
            resetPriorityAfterLockedSection();
            int showMask = 0;
            if (Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "accessibility_display_inversion_enabled", 0, currentUserId) != 1) {
                showMask = 1;
            }
            Message m = this.mH.obtainMessage(35);
            m.arg1 = showMask;
            this.mH.sendMessage(m);
        }
    }

    public void showEmulatorDisplayOverlayIfNeeded() {
        if (this.mContext.getResources().getBoolean(17891597) && SystemProperties.getBoolean(PROPERTY_EMULATOR_CIRCULAR, false) && Build.IS_EMULATOR) {
            H h = this.mH;
            h.sendMessage(h.obtainMessage(36));
        }
    }

    public void showCircularMask(boolean visible) {
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                if (WindowManagerDebugConfig.SHOW_LIGHT_TRANSACTIONS) {
                    Slog.i("WindowManager", ">>> OPEN TRANSACTION showCircularMask(visible=" + visible + ")");
                }
                openSurfaceTransaction();
                if (visible) {
                    try {
                        if (this.mCircularDisplayMask == null) {
                            this.mCircularDisplayMask = new CircularDisplayMask(getDefaultDisplayContentLocked(), (this.mPolicy.getWindowLayerFromTypeLw(2018) * 10000) + 10, this.mContext.getResources().getInteger(17694963), this.mContext.getResources().getDimensionPixelSize(17105046));
                        }
                        this.mCircularDisplayMask.setVisibility(true);
                    } catch (Throwable th) {
                        closeSurfaceTransaction("showCircularMask");
                        if (WindowManagerDebugConfig.SHOW_LIGHT_TRANSACTIONS) {
                            Slog.i("WindowManager", "<<< CLOSE TRANSACTION showCircularMask(visible=" + visible + ")");
                        }
                        throw th;
                    }
                } else if (this.mCircularDisplayMask != null) {
                    this.mCircularDisplayMask.setVisibility(false);
                    this.mCircularDisplayMask = null;
                }
                closeSurfaceTransaction("showCircularMask");
                if (WindowManagerDebugConfig.SHOW_LIGHT_TRANSACTIONS) {
                    Slog.i("WindowManager", "<<< CLOSE TRANSACTION showCircularMask(visible=" + visible + ")");
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void showEmulatorDisplayOverlay() {
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                if (WindowManagerDebugConfig.SHOW_LIGHT_TRANSACTIONS) {
                    Slog.i("WindowManager", ">>> OPEN TRANSACTION showEmulatorDisplayOverlay");
                }
                openSurfaceTransaction();
                try {
                    if (this.mEmulatorDisplayOverlay == null) {
                        this.mEmulatorDisplayOverlay = new EmulatorDisplayOverlay(this.mContext, getDefaultDisplayContentLocked(), (this.mPolicy.getWindowLayerFromTypeLw(2018) * 10000) + 10);
                    }
                    this.mEmulatorDisplayOverlay.setVisibility(true);
                } finally {
                    closeSurfaceTransaction("showEmulatorDisplayOverlay");
                    if (WindowManagerDebugConfig.SHOW_LIGHT_TRANSACTIONS) {
                        Slog.i("WindowManager", "<<< CLOSE TRANSACTION showEmulatorDisplayOverlay");
                    }
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void showStrictModeViolation(boolean on) {
        int pid = Binder.getCallingPid();
        if (on) {
            H h = this.mH;
            h.sendMessage(h.obtainMessage(25, 1, pid));
            H h2 = this.mH;
            h2.sendMessageDelayed(h2.obtainMessage(25, 0, pid), 1000);
            return;
        }
        H h3 = this.mH;
        h3.sendMessage(h3.obtainMessage(25, 0, pid));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void showStrictModeViolation(int arg, int pid) {
        boolean on = arg != 0;
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                if (!on || this.mRoot.canShowStrictModeViolation(pid)) {
                    if (WindowManagerDebugConfig.SHOW_VERBOSE_TRANSACTIONS) {
                        Slog.i("WindowManager", ">>> OPEN TRANSACTION showStrictModeViolation");
                    }
                    SurfaceControl.openTransaction();
                    try {
                        if (this.mStrictModeFlash == null) {
                            this.mStrictModeFlash = new StrictModeFlash(getDefaultDisplayContentLocked());
                        }
                        this.mStrictModeFlash.setVisibility(on);
                        resetPriorityAfterLockedSection();
                    } finally {
                        SurfaceControl.closeTransaction();
                        if (WindowManagerDebugConfig.SHOW_VERBOSE_TRANSACTIONS) {
                            Slog.i("WindowManager", "<<< CLOSE TRANSACTION showStrictModeViolation");
                        }
                    }
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void setStrictModeVisualIndicatorPreference(String value) {
        SystemProperties.set("persist.sys.strictmode.visual", value);
    }

    /* JADX INFO: finally extract failed */
    public Bitmap screenshotWallpaper() {
        Bitmap screenshotWallpaperLocked;
        if (checkCallingPermission("android.permission.READ_FRAME_BUFFER", "screenshotWallpaper()")) {
            try {
                Trace.traceBegin(32, "screenshotWallpaper");
                synchronized (this.mGlobalLock) {
                    try {
                        boostPriorityForLockedSection();
                        screenshotWallpaperLocked = this.mRoot.getDisplayContent(0).mWallpaperController.screenshotWallpaperLocked();
                    } catch (Throwable th) {
                        resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
                resetPriorityAfterLockedSection();
                return screenshotWallpaperLocked;
            } finally {
                Trace.traceEnd(32);
            }
        } else {
            throw new SecurityException("Requires READ_FRAME_BUFFER permission");
        }
    }

    /* JADX INFO: finally extract failed */
    public boolean requestAssistScreenshot(IAssistDataReceiver receiver) {
        Bitmap bm;
        if (checkCallingPermission("android.permission.READ_FRAME_BUFFER", "requestAssistScreenshot()")) {
            synchronized (this.mGlobalLock) {
                try {
                    boostPriorityForLockedSection();
                    DisplayContent displayContent = this.mRoot.getDisplayContent(0);
                    if (displayContent == null) {
                        if (WindowManagerDebugConfig.DEBUG_SCREENSHOT) {
                            Slog.i("WindowManager", "Screenshot returning null. No Display for displayId=0");
                        }
                        bm = null;
                    } else {
                        bm = displayContent.screenshotDisplayLocked(Bitmap.Config.ARGB_8888);
                    }
                } catch (Throwable th) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
            resetPriorityAfterLockedSection();
            FgThread.getHandler().post(new Runnable(receiver, bm) {
                /* class com.android.server.wm.$$Lambda$WindowManagerService$Zv37mcLTUXyG89YznyHzluaKNE0 */
                private final /* synthetic */ IAssistDataReceiver f$0;
                private final /* synthetic */ Bitmap f$1;

                {
                    this.f$0 = r1;
                    this.f$1 = r2;
                }

                public final void run() {
                    WindowManagerService.lambda$requestAssistScreenshot$3(this.f$0, this.f$1);
                }
            });
            return true;
        }
        throw new SecurityException("Requires READ_FRAME_BUFFER permission");
    }

    static /* synthetic */ void lambda$requestAssistScreenshot$3(IAssistDataReceiver receiver, Bitmap bm) {
        try {
            receiver.onHandleAssistScreenshot(bm);
        } catch (RemoteException e) {
        }
    }

    public ActivityManager.TaskSnapshot getTaskSnapshot(int taskId, int userId, boolean reducedResolution, boolean restoreFromDisk) {
        return this.mTaskSnapshotController.getSnapshot(taskId, userId, restoreFromDisk, reducedResolution);
    }

    public void removeObsoleteTaskFiles(ArraySet<Integer> persistentTaskIds, int[] runningUserIds) {
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                this.mTaskSnapshotController.removeObsoleteTaskFiles(persistentTaskIds, runningUserIds);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setRotateForApp(int displayId, int fixedToUserRotation) {
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                DisplayContent display = this.mRoot.getDisplayContent(displayId);
                if (display == null) {
                    Slog.w("WindowManager", "Trying to set rotate for app for a missing display.");
                    return;
                }
                display.getDisplayRotation().setFixedToUserRotation(fixedToUserRotation);
                resetPriorityAfterLockedSection();
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void freezeRotation(int rotation) {
        freezeDisplayRotation(0, rotation);
    }

    /* JADX INFO: finally extract failed */
    public void freezeDisplayRotation(int displayId, int rotation) {
        if (!checkCallingPermission("android.permission.SET_ORIENTATION", "freezeRotation()")) {
            throw new SecurityException("Requires SET_ORIENTATION permission");
        } else if (rotation < -1 || rotation > 3) {
            throw new IllegalArgumentException("Rotation argument must be -1 or a valid rotation constant.");
        } else {
            long origId = Binder.clearCallingIdentity();
            try {
                synchronized (this.mGlobalLock) {
                    try {
                        boostPriorityForLockedSection();
                        DisplayContent display = this.mRoot.getDisplayContent(displayId);
                        if (display == null) {
                            Slog.w("WindowManager", "Trying to freeze rotation for a missing display.");
                            resetPriorityAfterLockedSection();
                            return;
                        }
                        display.getDisplayRotation().freezeRotation(rotation);
                        resetPriorityAfterLockedSection();
                        Binder.restoreCallingIdentity(origId);
                        updateRotationUnchecked(false, false);
                    } catch (Throwable th) {
                        resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(origId);
            }
        }
    }

    public void thawRotation() {
        thawDisplayRotation(0);
    }

    /* JADX INFO: finally extract failed */
    public void thawDisplayRotation(int displayId) {
        if (checkCallingPermission("android.permission.SET_ORIENTATION", "thawRotation()")) {
            if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                Slog.v("WindowManager", "thawRotation: mRotation=" + getDefaultDisplayRotation());
            }
            long origId = Binder.clearCallingIdentity();
            try {
                synchronized (this.mGlobalLock) {
                    try {
                        boostPriorityForLockedSection();
                        DisplayContent display = this.mRoot.getDisplayContent(displayId);
                        if (display == null) {
                            Slog.w("WindowManager", "Trying to thaw rotation for a missing display.");
                            resetPriorityAfterLockedSection();
                            return;
                        }
                        display.getDisplayRotation().thawRotation();
                        resetPriorityAfterLockedSection();
                        Binder.restoreCallingIdentity(origId);
                        updateRotationUnchecked(false, false);
                    } catch (Throwable th) {
                        resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(origId);
            }
        } else {
            throw new SecurityException("Requires SET_ORIENTATION permission");
        }
    }

    public boolean isRotationFrozen() {
        return isDisplayRotationFrozen(0);
    }

    public boolean isDisplayRotationFrozen(int displayId) {
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                DisplayContent display = this.mRoot.getDisplayContent(displayId);
                if (display == null) {
                    Slog.w("WindowManager", "Trying to thaw rotation for a missing display.");
                    return false;
                }
                boolean isRotationFrozen = display.getDisplayRotation().isRotationFrozen();
                resetPriorityAfterLockedSection();
                return isRotationFrozen;
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void updateRotation(boolean alwaysSendConfiguration, boolean forceRelayout) {
        updateRotationUnchecked(alwaysSendConfiguration, forceRelayout);
    }

    /* JADX INFO: finally extract failed */
    private void updateRotationUnchecked(boolean alwaysSendConfiguration, boolean forceRelayout) {
        if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
            Slog.v("WindowManager", "updateRotationUnchecked: alwaysSendConfiguration=" + alwaysSendConfiguration + " forceRelayout=" + forceRelayout);
        }
        Trace.traceBegin(32, "updateRotation");
        long origId = Binder.clearCallingIdentity();
        try {
            synchronized (this.mGlobalLock) {
                try {
                    boostPriorityForLockedSection();
                    boolean layoutNeeded = false;
                    int displayCount = this.mRoot.mChildren.size();
                    for (int i = 0; i < displayCount; i++) {
                        DisplayContent displayContent = (DisplayContent) this.mRoot.mChildren.get(i);
                        Trace.traceBegin(32, "updateRotation: display");
                        boolean rotationChanged = displayContent.updateRotationUnchecked();
                        Trace.traceEnd(32);
                        if (!rotationChanged || forceRelayout) {
                            displayContent.setLayoutNeeded();
                            layoutNeeded = true;
                        }
                        if (rotationChanged || alwaysSendConfiguration) {
                            displayContent.sendNewConfiguration();
                        }
                    }
                    if (layoutNeeded) {
                        Trace.traceBegin(32, "updateRotation: performSurfacePlacement");
                        this.mWindowPlacerLocked.performSurfacePlacement();
                        Trace.traceEnd(32);
                    }
                } catch (Throwable th) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
            resetPriorityAfterLockedSection();
        } finally {
            Binder.restoreCallingIdentity(origId);
            Trace.traceEnd(32);
        }
    }

    public int getDefaultDisplayRotation() {
        int rotation;
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                rotation = getDefaultDisplayContentLocked().getRotation();
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        return rotation;
    }

    public int watchRotation(IRotationWatcher watcher, int displayId) {
        DisplayContent displayContent;
        int rotation;
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                displayContent = this.mRoot.getDisplayContent(displayId);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        if (displayContent != null) {
            final IBinder watcherBinder = watcher.asBinder();
            IBinder.DeathRecipient dr = new IBinder.DeathRecipient() {
                /* class com.android.server.wm.WindowManagerService.AnonymousClass11 */

                public void binderDied() {
                    synchronized (WindowManagerService.this.mGlobalLock) {
                        try {
                            WindowManagerService.boostPriorityForLockedSection();
                            int i = 0;
                            while (i < WindowManagerService.this.mRotationWatchers.size()) {
                                if (watcherBinder == WindowManagerService.this.mRotationWatchers.get(i).mWatcher.asBinder()) {
                                    IBinder binder = WindowManagerService.this.mRotationWatchers.remove(i).mWatcher.asBinder();
                                    if (binder != null) {
                                        binder.unlinkToDeath(this, 0);
                                    }
                                    i--;
                                }
                                i++;
                            }
                        } finally {
                            WindowManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                }
            };
            synchronized (this.mGlobalLock) {
                try {
                    boostPriorityForLockedSection();
                    watcher.asBinder().linkToDeath(dr, 0);
                    this.mRotationWatchers.add(new RotationWatcher(watcher, dr, displayId));
                } catch (RemoteException e) {
                }
                try {
                    rotation = displayContent.getRotation();
                } finally {
                    resetPriorityAfterLockedSection();
                }
            }
            return rotation;
        }
        throw new IllegalArgumentException("Trying to register rotation event for invalid display: " + displayId);
    }

    public void removeRotationWatcher(IRotationWatcher watcher) {
        IBinder watcherBinder = watcher.asBinder();
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                int i = 0;
                while (i < this.mRotationWatchers.size()) {
                    if (watcherBinder == this.mRotationWatchers.get(i).mWatcher.asBinder()) {
                        RotationWatcher removed = this.mRotationWatchers.remove(i);
                        IBinder binder = removed.mWatcher.asBinder();
                        if (binder != null) {
                            binder.unlinkToDeath(removed.mDeathRecipient, 0);
                        }
                        i--;
                    }
                    i++;
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public boolean registerWallpaperVisibilityListener(IWallpaperVisibilityListener listener, int displayId) {
        boolean isWallpaperVisible;
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                DisplayContent displayContent = this.mRoot.getDisplayContent(displayId);
                if (displayContent != null) {
                    this.mWallpaperVisibilityListeners.registerWallpaperVisibilityListener(listener, displayId);
                    isWallpaperVisible = displayContent.mWallpaperController.isWallpaperVisible();
                } else {
                    throw new IllegalArgumentException("Trying to register visibility event for invalid display: " + displayId);
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        return isWallpaperVisible;
    }

    public void unregisterWallpaperVisibilityListener(IWallpaperVisibilityListener listener, int displayId) {
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                this.mWallpaperVisibilityListeners.unregisterWallpaperVisibilityListener(listener, displayId);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void registerSystemGestureExclusionListener(ISystemGestureExclusionListener listener, int displayId) {
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                DisplayContent displayContent = this.mRoot.getDisplayContent(displayId);
                if (displayContent != null) {
                    displayContent.registerSystemGestureExclusionListener(listener);
                } else {
                    throw new IllegalArgumentException("Trying to register visibility event for invalid display: " + displayId);
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void unregisterSystemGestureExclusionListener(ISystemGestureExclusionListener listener, int displayId) {
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                DisplayContent displayContent = this.mRoot.getDisplayContent(displayId);
                if (displayContent != null) {
                    displayContent.unregisterSystemGestureExclusionListener(listener);
                } else {
                    throw new IllegalArgumentException("Trying to register visibility event for invalid display: " + displayId);
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void reportSystemGestureExclusionChanged(Session session, IWindow window, List<Rect> exclusionRects) {
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                WindowState win = windowForClientLocked(session, window, true);
                if (this.mColorWmsEx.checkEnableSetGestureExclusion(this.mContext, win)) {
                    if (win.setSystemGestureExclusion(exclusionRects)) {
                        win.getDisplayContent().updateSystemGestureExclusion();
                    }
                    resetPriorityAfterLockedSection();
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void registerDisplayFoldListener(IDisplayFoldListener listener) {
        this.mPolicy.registerDisplayFoldListener(listener);
    }

    public void unregisterDisplayFoldListener(IDisplayFoldListener listener) {
        this.mPolicy.unregisterDisplayFoldListener(listener);
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: package-private */
    public void setOverrideFoldedArea(Rect area) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.WRITE_SECURE_SETTINGS") == 0) {
            long origId = Binder.clearCallingIdentity();
            try {
                synchronized (this.mGlobalLock) {
                    try {
                        boostPriorityForLockedSection();
                        this.mPolicy.setOverrideFoldedArea(area);
                    } catch (Throwable th) {
                        resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
                resetPriorityAfterLockedSection();
            } finally {
                Binder.restoreCallingIdentity(origId);
            }
        } else {
            throw new SecurityException("Must hold permission android.permission.WRITE_SECURE_SETTINGS");
        }
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: package-private */
    public Rect getFoldedArea() {
        Rect foldedArea;
        long origId = Binder.clearCallingIdentity();
        try {
            synchronized (this.mGlobalLock) {
                try {
                    boostPriorityForLockedSection();
                    foldedArea = this.mPolicy.getFoldedArea();
                } catch (Throwable th) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
            resetPriorityAfterLockedSection();
            return foldedArea;
        } finally {
            Binder.restoreCallingIdentity(origId);
        }
    }

    public int getPreferredOptionsPanelGravity(int displayId) {
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                DisplayContent displayContent = this.mRoot.getDisplayContent(displayId);
                if (displayContent == null) {
                    return 81;
                }
                int preferredOptionsPanelGravity = displayContent.getPreferredOptionsPanelGravity();
                resetPriorityAfterLockedSection();
                return preferredOptionsPanelGravity;
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public boolean startViewServer(int port) {
        if (isSystemSecure() || !checkCallingPermission("android.permission.DUMP", "startViewServer") || port < 1024) {
            return false;
        }
        ViewServer viewServer = this.mViewServer;
        if (viewServer != null) {
            if (!viewServer.isRunning()) {
                try {
                    return this.mViewServer.start();
                } catch (IOException e) {
                    Slog.w("WindowManager", "View server did not start");
                }
            }
            return false;
        }
        try {
            this.mViewServer = new ViewServer(this, port);
            return this.mViewServer.start();
        } catch (IOException e2) {
            Slog.w("WindowManager", "View server did not start");
            return false;
        }
    }

    private boolean isSystemSecure() {
        return NoFocusWindow.HUNG_CONFIG_ENABLE.equals(SystemProperties.get(SYSTEM_SECURE, NoFocusWindow.HUNG_CONFIG_ENABLE)) && "0".equals(SystemProperties.get(SYSTEM_DEBUGGABLE, "0"));
    }

    public boolean stopViewServer() {
        ViewServer viewServer;
        if (!isSystemSecure() && checkCallingPermission("android.permission.DUMP", "stopViewServer") && (viewServer = this.mViewServer) != null) {
            return viewServer.stop();
        }
        return false;
    }

    public boolean isViewServerRunning() {
        ViewServer viewServer;
        if (!isSystemSecure() && checkCallingPermission("android.permission.DUMP", "isViewServerRunning") && (viewServer = this.mViewServer) != null && viewServer.isRunning()) {
            return true;
        }
        return false;
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: package-private */
    public boolean viewServerListWindows(Socket client) {
        if (isSystemSecure()) {
            return false;
        }
        ArrayList<WindowState> windows = new ArrayList<>();
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                this.mRoot.forAllWindows((Consumer<WindowState>) new Consumer(windows) {
                    /* class com.android.server.wm.$$Lambda$WindowManagerService$Yf21B7QM1fRVFGIQy6MImYjka28 */
                    private final /* synthetic */ ArrayList f$0;

                    {
                        this.f$0 = r1;
                    }

                    @Override // java.util.function.Consumer
                    public final void accept(Object obj) {
                        this.f$0.add((WindowState) obj);
                    }
                }, false);
            } catch (Throwable th) {
                resetPriorityAfterLockedSection();
                throw th;
            }
        }
        resetPriorityAfterLockedSection();
        BufferedWriter out = null;
        try {
            BufferedWriter out2 = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()), 8192);
            int count = windows.size();
            for (int i = 0; i < count; i++) {
                WindowState w = windows.get(i);
                out2.write(Integer.toHexString(System.identityHashCode(w)));
                out2.write(32);
                out2.append(w.mAttrs.getTitle());
                out2.write(10);
            }
            out2.write("DONE.\n");
            out2.flush();
            try {
                out2.close();
                return true;
            } catch (IOException e) {
                return false;
            }
        } catch (Exception e2) {
            if (0 == 0) {
                return false;
            }
            out.close();
            return false;
        } catch (Throwable th2) {
            if (0 != 0) {
                try {
                    out.close();
                } catch (IOException e3) {
                }
            }
            throw th2;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean viewServerGetFocusedWindow(Socket client) {
        if (isSystemSecure()) {
            return false;
        }
        WindowState focusedWindow = getFocusedWindow();
        BufferedWriter out = null;
        try {
            BufferedWriter out2 = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()), 8192);
            if (focusedWindow != null) {
                out2.write(Integer.toHexString(System.identityHashCode(focusedWindow)));
                out2.write(32);
                out2.append(focusedWindow.mAttrs.getTitle());
            }
            out2.write(10);
            out2.flush();
            try {
                out2.close();
                return true;
            } catch (IOException e) {
                return false;
            }
        } catch (Exception e2) {
            if (0 == 0) {
                return false;
            }
            out.close();
            return false;
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    out.close();
                } catch (IOException e3) {
                }
            }
            throw th;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean viewServerWindowCommand(Socket client, String command, String parameters) {
        String parameters2;
        if (isSystemSecure()) {
            return false;
        }
        boolean success = true;
        Parcel data = null;
        Parcel reply = null;
        BufferedWriter out = null;
        try {
            int index = parameters.indexOf(32);
            if (index == -1) {
                index = parameters.length();
            }
            int hashCode = (int) Long.parseLong(parameters.substring(0, index), 16);
            if (index < parameters.length()) {
                parameters2 = parameters.substring(index + 1);
            } else {
                parameters2 = "";
            }
            WindowState window = findWindow(hashCode);
            if (window == null) {
                if (0 != 0) {
                    data.recycle();
                }
                if (0 != 0) {
                    reply.recycle();
                }
                if (0 != 0) {
                    try {
                        out.close();
                    } catch (IOException e) {
                    }
                }
                return false;
            }
            Parcel data2 = Parcel.obtain();
            data2.writeInterfaceToken("android.view.IWindow");
            data2.writeString(command);
            data2.writeString(parameters2);
            data2.writeInt(1);
            ParcelFileDescriptor.fromSocket(client).writeToParcel(data2, 0);
            Parcel reply2 = Parcel.obtain();
            window.mClient.asBinder().transact(1, data2, reply2, 0);
            reply2.readException();
            if (!client.isOutputShutdown()) {
                out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
                out.write("DONE\n");
                out.flush();
            }
            data2.recycle();
            reply2.recycle();
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e2) {
                }
            }
            return success;
        } catch (Exception e3) {
            Slog.w("WindowManager", "Could not send command " + command + " with parameters " + parameters, e3);
            success = false;
            if (0 != 0) {
                data.recycle();
            }
            if (0 != 0) {
                reply.recycle();
            }
            if (0 != 0) {
                out.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                data.recycle();
            }
            if (0 != 0) {
                reply.recycle();
            }
            if (0 != 0) {
                try {
                    out.close();
                } catch (IOException e4) {
                }
            }
            throw th;
        }
    }

    public void addWindowChangeListener(WindowChangeListener listener) {
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                this.mWindowChangeListeners.add(listener);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void removeWindowChangeListener(WindowChangeListener listener) {
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                this.mWindowChangeListeners.remove(listener);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0025, code lost:
        resetPriorityAfterLockedSection();
        r0 = r2.length;
        r2 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x002a, code lost:
        if (r2 >= r0) goto L_0x0034;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x002c, code lost:
        r2[r2].windowsChanged();
        r2 = r2 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0034, code lost:
        return;
     */
    private void notifyWindowsChanged() {
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                if (!this.mWindowChangeListeners.isEmpty()) {
                    WindowChangeListener[] windowChangeListeners = (WindowChangeListener[]) this.mWindowChangeListeners.toArray(new WindowChangeListener[this.mWindowChangeListeners.size()]);
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0025, code lost:
        resetPriorityAfterLockedSection();
        r0 = r2.length;
        r2 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x002a, code lost:
        if (r2 >= r0) goto L_0x0034;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x002c, code lost:
        r2[r2].focusChanged();
        r2 = r2 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0034, code lost:
        return;
     */
    private void notifyFocusChanged() {
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                if (!this.mWindowChangeListeners.isEmpty()) {
                    WindowChangeListener[] windowChangeListeners = (WindowChangeListener[]) this.mWindowChangeListeners.toArray(new WindowChangeListener[this.mWindowChangeListeners.size()]);
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    private WindowState findWindow(int hashCode) {
        WindowState window;
        if (hashCode == -1) {
            return getFocusedWindow();
        }
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                window = this.mRoot.getWindow(new Predicate(hashCode) {
                    /* class com.android.server.wm.$$Lambda$WindowManagerService$tOeHm8ndyhv8iLNQ_GHuZ7HhJdw */
                    private final /* synthetic */ int f$0;

                    {
                        this.f$0 = r1;
                    }

                    @Override // java.util.function.Predicate
                    public final boolean test(Object obj) {
                        return WindowManagerService.lambda$findWindow$5(this.f$0, (WindowState) obj);
                    }
                });
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        return window;
    }

    static /* synthetic */ boolean lambda$findWindow$5(int hashCode, WindowState w) {
        return System.identityHashCode(w) == hashCode;
    }

    /* access modifiers changed from: package-private */
    public void sendNewConfiguration(int displayId) {
        try {
            if (!this.mActivityTaskManager.updateDisplayOverrideConfiguration((Configuration) null, displayId)) {
                synchronized (this.mGlobalLock) {
                    try {
                        boostPriorityForLockedSection();
                        DisplayContent dc = this.mRoot.getDisplayContent(displayId);
                        if (dc != null && dc.mWaitingForConfig) {
                            dc.mWaitingForConfig = false;
                            this.mLastFinishedFreezeSource = "config-unchanged";
                            dc.setLayoutNeeded();
                            this.mWindowPlacerLocked.performSurfacePlacement();
                        }
                    } finally {
                        resetPriorityAfterLockedSection();
                    }
                }
            }
        } catch (RemoteException e) {
        }
    }

    public Configuration computeNewConfiguration(int displayId) {
        Configuration computeNewConfigurationLocked;
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                computeNewConfigurationLocked = computeNewConfigurationLocked(displayId);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        return computeNewConfigurationLocked;
    }

    private Configuration computeNewConfigurationLocked(int displayId) {
        if (!this.mDisplayReady) {
            return null;
        }
        Configuration config = new Configuration();
        this.mRoot.getDisplayContent(displayId).computeScreenConfiguration(config);
        return config;
    }

    /* access modifiers changed from: package-private */
    public void notifyHardKeyboardStatusChange() {
        WindowManagerInternal.OnHardKeyboardStatusChangeListener listener;
        boolean available;
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                listener = this.mHardKeyboardStatusChangeListener;
                available = this.mHardKeyboardAvailable;
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        if (listener != null) {
            listener.onHardKeyboardStatusChange(available);
        }
    }

    public void setEventDispatching(boolean enabled) {
        if (checkCallingPermission("android.permission.MANAGE_APP_TOKENS", "setEventDispatching()")) {
            synchronized (this.mGlobalLock) {
                try {
                    boostPriorityForLockedSection();
                    this.mEventDispatchingEnabled = enabled;
                    if (this.mDisplayEnabled) {
                        this.mInputManagerCallback.setEventDispatchingLw(enabled);
                    }
                } finally {
                    resetPriorityAfterLockedSection();
                }
            }
            return;
        }
        throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private WindowState getFocusedWindow() {
        WindowState focusedWindowLocked;
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                focusedWindowLocked = getFocusedWindowLocked();
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        return focusedWindowLocked;
    }

    public WindowState getFocusedWindowLocked() {
        return this.mRoot.getTopFocusedDisplayContent().mCurrentFocus;
    }

    /* access modifiers changed from: package-private */
    public TaskStack getImeFocusStackLocked() {
        AppWindowToken focusedApp = this.mRoot.getTopFocusedDisplayContent().mFocusedApp;
        if (focusedApp == null || focusedApp.getTask() == null) {
            return null;
        }
        return focusedApp.getTask().mStack;
    }

    private static int readIntFromFile(String logTag, String path, int defaultValue) {
        StringBuilder sb;
        BufferedReader reader = null;
        int result = defaultValue;
        String tempString = null;
        try {
            reader = new BufferedReader(new FileReader(new File(path)));
            tempString = reader.readLine();
            try {
                reader.close();
            } catch (IOException e) {
                e1 = e;
                sb = new StringBuilder();
            }
        } catch (Exception e2) {
            Log.e(logTag, "readIntFromFile io exception:" + e2.getMessage());
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e3) {
                    e1 = e3;
                    sb = new StringBuilder();
                }
            }
        } catch (Throwable th) {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                    Log.e(logTag, "readIntFromFile io close exception :" + e1.getMessage());
                }
            }
            throw th;
        }
        if (tempString != null && !tempString.isEmpty()) {
            try {
                result = Integer.valueOf(tempString).intValue();
            } catch (NumberFormatException e4) {
                Log.e(logTag, "readIntFromFile NumberFormatException:" + e4.getMessage());
            }
        }
        Log.i(logTag, "readIntFromFile path:" + path + ", result:" + result + ", defaultValue:" + defaultValue);
        return result;
        sb.append("readIntFromFile io close exception :");
        sb.append(e1.getMessage());
        Log.e(logTag, sb.toString());
        result = Integer.valueOf(tempString).intValue();
        Log.i(logTag, "readIntFromFile path:" + path + ", result:" + result + ", defaultValue:" + defaultValue);
        return result;
    }

    public boolean detectSafeMode() {
        if (!this.mInputManagerCallback.waitForInputDevicesReady(1000)) {
            Slog.w("WindowManager", "Devices still not ready after waiting 1000 milliseconds before attempting to detect safe mode.");
        }
        if (Settings.Global.getInt(this.mContext.getContentResolver(), "safe_boot_disallowed", 0) != 0) {
            return false;
        }
        int menuState = this.mInputManager.getKeyCodeState(-1, -256, 82);
        int sState = this.mInputManager.getKeyCodeState(-1, -256, 47);
        int dpadState = this.mInputManager.getKeyCodeState(-1, UsbTerminalTypes.TERMINAL_IN_MIC, 23);
        int trackballState = this.mInputManager.getScanCodeState(-1, 65540, 272);
        this.mInputManager.getKeyCodeState(-1, -256, 25);
        if (this.mContext.getPackageManager().hasSystemFeature(OppoCustomizeNotificationHelper.Constants.FEATURE_BUSINESS_CUSTOM) && SystemProperties.getInt("persist.sys.disable.safemode", 0) == 0) {
            try {
                if (!(SystemProperties.getInt(ShutdownThread.REBOOT_SAFEMODE_PROPERTY, 0) == 0 && SystemProperties.getInt(ShutdownThread.RO_SAFEMODE_PROPERTY, 0) == 0 && readIntFromFile("Safemode", "sys/systeminfo/ftmmode", 0) != 999)) {
                    this.mSafeMode = true;
                    SystemProperties.set(ShutdownThread.REBOOT_SAFEMODE_PROPERTY, "");
                }
            } catch (IllegalArgumentException e) {
            }
        }
        if (this.mSafeMode) {
            Log.i("WindowManager", "SAFE MODE ENABLED (menu=" + menuState + " s=" + sState + " dpad=" + dpadState + " trackball=" + trackballState + ")");
            if (SystemProperties.getInt(ShutdownThread.RO_SAFEMODE_PROPERTY, 0) == 0) {
                SystemProperties.set(ShutdownThread.RO_SAFEMODE_PROPERTY, NoFocusWindow.HUNG_CONFIG_ENABLE);
            }
        } else {
            Log.i("WindowManager", "SAFE MODE not enabled");
        }
        this.mPolicy.setSafeMode(this.mSafeMode);
        return this.mSafeMode;
    }

    public void displayReady() {
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                if (this.mMaxUiWidth > 0) {
                    this.mRoot.forAllDisplays(new Consumer() {
                        /* class com.android.server.wm.$$Lambda$WindowManagerService$_tfpDlf3MkHSDi8MNIOlvGgvLS8 */

                        @Override // java.util.function.Consumer
                        public final void accept(Object obj) {
                            WindowManagerService.this.lambda$displayReady$6$WindowManagerService((DisplayContent) obj);
                        }
                    });
                }
                boolean changed = applyForcedPropertiesForDefaultDisplay();
                this.mAnimator.ready();
                this.mDisplayReady = true;
                if (changed) {
                    reconfigureDisplayLocked(getDefaultDisplayContentLocked());
                }
                this.mIsTouchDevice = this.mContext.getPackageManager().hasSystemFeature("android.hardware.touchscreen");
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        try {
            this.mActivityTaskManager.updateConfiguration((Configuration) null);
        } catch (RemoteException e) {
        }
        updateCircularDisplayMaskIfNeeded();
    }

    public /* synthetic */ void lambda$displayReady$6$WindowManagerService(DisplayContent displayContent) {
        displayContent.setMaxUiWidth(this.mMaxUiWidth);
    }

    public void systemReady() {
        this.mSystemReady = true;
        this.mPolicy.systemReady();
        this.mRoot.forAllDisplayPolicies($$Lambda$cJEiQ28RvThCcuht9wXeFzPgo.INSTANCE);
        this.mTaskSnapshotController.systemReady();
        this.mHasWideColorGamutSupport = queryWideColorGamutSupport();
        ColorNavigationBarUtil.getInstance().init(this.mContext);
        this.mHasHdrSupport = queryHdrSupport();
        Handler handler = UiThread.getHandler();
        SettingsObserver settingsObserver = this.mSettingsObserver;
        Objects.requireNonNull(settingsObserver);
        handler.post(new Runnable() {
            /* class com.android.server.wm.$$Lambda$iQxeP_PsHHArcPSFabJ3FXyPKNc */

            public final void run() {
                WindowManagerService.SettingsObserver.this.updateSystemUiSettings();
            }
        });
        Handler handler2 = UiThread.getHandler();
        SettingsObserver settingsObserver2 = this.mSettingsObserver;
        Objects.requireNonNull(settingsObserver2);
        handler2.post(new Runnable() {
            /* class com.android.server.wm.$$Lambda$B58NKEOrr2mhFWeS3bqpaZnd11o */

            public final void run() {
                WindowManagerService.SettingsObserver.this.updatePointerLocation();
            }
        });
        IVrManager vrManager = IVrManager.Stub.asInterface(ServiceManager.getService("vrmanager"));
        if (vrManager != null) {
            try {
                boolean vrModeEnabled = vrManager.getVrModeState();
                synchronized (this.mGlobalLock) {
                    try {
                        boostPriorityForLockedSection();
                        vrManager.registerListener(this.mVrStateCallbacks);
                        if (vrModeEnabled) {
                            this.mVrModeEnabled = vrModeEnabled;
                            this.mVrStateCallbacks.onVrStateChanged(vrModeEnabled);
                        }
                    } finally {
                        resetPriorityAfterLockedSection();
                    }
                }
            } catch (RemoteException e) {
            }
        }
    }

    private static boolean queryWideColorGamutSupport() {
        try {
            OptionalBool hasWideColor = ISurfaceFlingerConfigs.getService().hasWideColorDisplay();
            if (hasWideColor != null) {
                return hasWideColor.value;
            }
            return false;
        } catch (RemoteException e) {
            return false;
        }
    }

    private static boolean queryHdrSupport() {
        try {
            OptionalBool hasHdr = ISurfaceFlingerConfigs.getService().hasHDRDisplay();
            if (hasHdr != null) {
                return hasHdr.value;
            }
            return false;
        } catch (RemoteException e) {
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public final class H extends Handler {
        public static final int ALL_WINDOWS_DRAWN = 33;
        public static final int ANIMATION_FAILSAFE = 60;
        public static final int APP_FREEZE_TIMEOUT = 17;
        public static final int BLOCK_SURFACE_FLINGER = 63;
        public static final int BOOT_TIMEOUT = 23;
        public static final int CHECK_IF_BOOT_ANIMATION_FINISHED = 37;
        public static final int CLIENT_FREEZE_TIMEOUT = 30;
        public static final int ENABLE_SCREEN = 16;
        public static final int FORCE_GC = 15;
        public static final int NEW_ANIMATOR_SCALE = 34;
        public static final int NOTIFY_ACTIVITY_DRAWN = 32;
        public static final int NOTIFY_RESOLUTION_MODE_CHANGE = 66;
        public static final int ON_POINTER_DOWN_OUTSIDE_FOCUS = 62;
        public static final int OPPO_FREEZE_TIMEOUT = 100400;
        public static final int PERSIST_ANIMATION_SCALE = 14;
        public static final int RECOMPUTE_FOCUS = 61;
        public static final int REPORT_FOCUS_CHANGE = 2;
        public static final int REPORT_HARD_KEYBOARD_STATUS_CHANGE = 22;
        public static final int REPORT_LOSING_FOCUS = 3;
        public static final int REPORT_WINDOWS_CHANGE = 19;
        public static final int RESET_ANR_MESSAGE = 38;
        public static final int RESTORE_POINTER_ICON = 55;
        public static final int SEAMLESS_ROTATION_TIMEOUT = 54;
        public static final int SEND_NEW_CONFIGURATION = 18;
        public static final int SET_HAS_OVERLAY_UI = 58;
        public static final int SET_RUNNING_REMOTE_ANIMATION = 59;
        public static final int SHOW_CIRCULAR_DISPLAY_MASK = 35;
        public static final int SHOW_EMULATOR_DISPLAY_OVERLAY = 36;
        public static final int SHOW_STRICT_MODE_VIOLATION = 25;
        public static final int UNBLOCK_SURFACE_FLINGER = 64;
        public static final int UNUSED = 0;
        public static final int UPDATE_ANIMATION_SCALE = 51;
        public static final int UPDATE_DOCKED_STACK_DIVIDER = 41;
        public static final int WAITING_FOR_DRAWN_TIMEOUT = 24;
        public static final int WALLPAPER_DRAW_PENDING_TIMEOUT = 39;
        public static final int WINDOW_FREEZE_TIMEOUT = 11;
        public static final int WINDOW_HIDE_TIMEOUT = 52;
        public static final int WINDOW_REPLACEMENT_TIMEOUT = 46;

        H() {
        }

        /* JADX INFO: finally extract failed */
        /* JADX INFO: Multiple debug info for r0v17 int: [D('window' com.android.server.wm.WindowState), D('mode' int)] */
        /* JADX WARNING: Code restructure failed: missing block: B:240:0x042f, code lost:
            com.android.server.wm.WindowManagerService.resetPriorityAfterLockedSection();
            java.lang.Runtime.getRuntime().gc();
         */
        public void handleMessage(Message msg) {
            WindowState lastFocus;
            WindowState newFocus;
            ArrayList<WindowState> losers;
            Runnable callback;
            Runnable dockedCallback;
            Runnable callback2;
            Runnable dockedCallback2;
            boolean bootAnimationComplete;
            if (WindowManagerDebugConfig.DEBUG_WINDOW_TRACE) {
                Slog.v("WindowManager", "handleMessage: entry what=" + msg.what);
            }
            int i = msg.what;
            boolean z = false;
            boolean z2 = false;
            boolean z3 = false;
            ignoreCall = false;
            boolean ignoreCall = false;
            if (i == 2) {
                DisplayContent displayContent = (DisplayContent) msg.obj;
                AccessibilityController accessibilityController = null;
                synchronized (WindowManagerService.this.mGlobalLock) {
                    try {
                        WindowManagerService.boostPriorityForLockedSection();
                        if (WindowManagerService.this.mAccessibilityController != null && displayContent.isDefaultDisplay) {
                            accessibilityController = WindowManagerService.this.mAccessibilityController;
                        }
                        lastFocus = displayContent.mLastFocus;
                        newFocus = displayContent.mCurrentFocus;
                    } finally {
                        WindowManagerService.resetPriorityAfterLockedSection();
                    }
                }
                if (lastFocus != newFocus) {
                    WindowManagerService.this.mColorWmsEx.updateFloatWindowState(WindowManagerService.sInstance, WindowManagerService.this.mAppOps, WindowManagerService.this.mActivityManager, WindowManagerService.this.mContext, lastFocus, newFocus);
                    synchronized (WindowManagerService.this.mGlobalLock) {
                        try {
                            WindowManagerService.boostPriorityForLockedSection();
                            displayContent.mLastFocus = newFocus;
                            if (WindowManagerDebugConfig.DEBUG_FOCUS_LIGHT) {
                                Slog.i("WindowManager", "Focus moving from " + lastFocus + " to " + newFocus + " displayId=" + displayContent.getDisplayId());
                            }
                            if (!(newFocus == null || lastFocus == null || newFocus.isDisplayedLw())) {
                                if (WindowManagerDebugConfig.DEBUG_FOCUS_LIGHT) {
                                    Slog.i("WindowManager", "Delaying loss of focus...");
                                }
                                displayContent.mLosingFocus.add(lastFocus);
                                lastFocus = null;
                            }
                            if (newFocus != null && displayContent.mLosingFocus.contains(newFocus) && newFocus.getName().contains("com.oppo.launcher.Launcher")) {
                                if (WindowManagerDebugConfig.DEBUG_FOCUS_LIGHT) {
                                    Slog.i("WindowManager", "Remove " + newFocus + " from delaying losing focus. it gaining focus.");
                                }
                                displayContent.mLosingFocus.remove(newFocus);
                            }
                        } finally {
                            WindowManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    if (accessibilityController != null) {
                        accessibilityController.onWindowFocusChangedNotLocked();
                    }
                    if (newFocus != null) {
                        if (WindowManagerDebugConfig.DEBUG_FOCUS_LIGHT) {
                            Slog.i("WindowManager", "Gaining focus: " + newFocus);
                        }
                        newFocus.reportFocusChangedSerialized(true, WindowManagerService.this.mInTouchMode);
                        WindowManagerService.this.notifyFocusChanged();
                    }
                    if (lastFocus != null) {
                        if (WindowManagerDebugConfig.DEBUG_FOCUS_LIGHT) {
                            Slog.i("WindowManager", "Losing focus: " + lastFocus);
                        }
                        lastFocus.reportFocusChangedSerialized(false, WindowManagerService.this.mInTouchMode);
                    }
                } else {
                    return;
                }
            } else if (i == 3) {
                DisplayContent displayContent2 = (DisplayContent) msg.obj;
                synchronized (WindowManagerService.this.mGlobalLock) {
                    try {
                        WindowManagerService.boostPriorityForLockedSection();
                        losers = displayContent2.mLosingFocus;
                        displayContent2.mLosingFocus = new ArrayList<>();
                    } catch (Throwable th) {
                        WindowManagerService.resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
                WindowManagerService.resetPriorityAfterLockedSection();
                int N = losers.size();
                for (int i2 = 0; i2 < N; i2++) {
                    if (WindowManagerDebugConfig.DEBUG_FOCUS_LIGHT) {
                        Slog.i("WindowManager", "Losing delayed focus: " + losers.get(i2));
                    }
                    losers.get(i2).reportFocusChangedSerialized(false, WindowManagerService.this.mInTouchMode);
                }
            } else if (i == 11) {
                DisplayContent displayContent3 = (DisplayContent) msg.obj;
                synchronized (WindowManagerService.this.mGlobalLock) {
                    try {
                        WindowManagerService.boostPriorityForLockedSection();
                        displayContent3.onWindowFreezeTimeout();
                    } finally {
                        WindowManagerService.resetPriorityAfterLockedSection();
                    }
                }
            } else if (i == 30) {
                synchronized (WindowManagerService.this.mGlobalLock) {
                    try {
                        WindowManagerService.boostPriorityForLockedSection();
                        if (WindowManagerService.this.mClientFreezingScreen) {
                            WindowManagerService.this.mClientFreezingScreen = false;
                            WindowManagerService.this.mLastFinishedFreezeSource = "client-timeout";
                            WindowManagerService.this.stopFreezingDisplayLocked();
                        }
                    } finally {
                        WindowManagerService.resetPriorityAfterLockedSection();
                    }
                }
            } else if (i == 41) {
                synchronized (WindowManagerService.this.mGlobalLock) {
                    try {
                        WindowManagerService.boostPriorityForLockedSection();
                        DisplayContent displayContent4 = WindowManagerService.this.getDefaultDisplayContentLocked();
                        displayContent4.getDockedDividerController().reevaluateVisibility(false);
                        displayContent4.adjustForImeIfNeeded();
                    } finally {
                        WindowManagerService.resetPriorityAfterLockedSection();
                    }
                }
            } else if (i == 46) {
                synchronized (WindowManagerService.this.mGlobalLock) {
                    try {
                        WindowManagerService.boostPriorityForLockedSection();
                        for (int i3 = WindowManagerService.this.mWindowReplacementTimeouts.size() - 1; i3 >= 0; i3--) {
                            WindowManagerService.this.mWindowReplacementTimeouts.get(i3).onWindowReplacementTimeout();
                        }
                        WindowManagerService.this.mWindowReplacementTimeouts.clear();
                    } finally {
                        WindowManagerService.resetPriorityAfterLockedSection();
                    }
                }
            } else if (i == 66) {
                WindowManagerService.this.killAutoResolutionAPP();
            } else if (i == 51) {
                int mode = msg.arg1;
                if (mode == 0) {
                    WindowManagerService windowManagerService = WindowManagerService.this;
                    windowManagerService.mWindowAnimationScaleSetting = Settings.Global.getFloat(windowManagerService.mContext.getContentResolver(), "window_animation_scale", WindowManagerService.this.mWindowAnimationScaleSetting);
                } else if (mode == 1) {
                    WindowManagerService windowManagerService2 = WindowManagerService.this;
                    windowManagerService2.mTransitionAnimationScaleSetting = Settings.Global.getFloat(windowManagerService2.mContext.getContentResolver(), "transition_animation_scale", WindowManagerService.this.mTransitionAnimationScaleSetting);
                } else if (mode == 2) {
                    WindowManagerService windowManagerService3 = WindowManagerService.this;
                    windowManagerService3.mAnimatorDurationScaleSetting = Settings.Global.getFloat(windowManagerService3.mContext.getContentResolver(), "animator_duration_scale", WindowManagerService.this.mAnimatorDurationScaleSetting);
                    WindowManagerService.this.dispatchNewAnimatorScaleLocked(null);
                }
            } else if (i == 52) {
                WindowState window = (WindowState) msg.obj;
                synchronized (WindowManagerService.this.mGlobalLock) {
                    try {
                        WindowManagerService.boostPriorityForLockedSection();
                        window.mAttrs.flags &= -129;
                        window.hidePermanentlyLw();
                        window.setDisplayLayoutNeeded();
                        WindowManagerService.this.mWindowPlacerLocked.performSurfacePlacement();
                    } finally {
                        WindowManagerService.resetPriorityAfterLockedSection();
                    }
                }
            } else if (i == 54) {
                DisplayContent displayContent5 = (DisplayContent) msg.obj;
                synchronized (WindowManagerService.this.mGlobalLock) {
                    try {
                        WindowManagerService.boostPriorityForLockedSection();
                        displayContent5.onSeamlessRotationTimeout();
                    } finally {
                        WindowManagerService.resetPriorityAfterLockedSection();
                    }
                }
            } else if (i != 55) {
                switch (i) {
                    case 14:
                        Settings.Global.putFloat(WindowManagerService.this.mContext.getContentResolver(), "window_animation_scale", WindowManagerService.this.mWindowAnimationScaleSetting);
                        Settings.Global.putFloat(WindowManagerService.this.mContext.getContentResolver(), "transition_animation_scale", WindowManagerService.this.mTransitionAnimationScaleSetting);
                        Settings.Global.putFloat(WindowManagerService.this.mContext.getContentResolver(), "animator_duration_scale", WindowManagerService.this.mAnimatorDurationScaleSetting);
                        break;
                    case 15:
                        synchronized (WindowManagerService.this.mGlobalLock) {
                            try {
                                WindowManagerService.boostPriorityForLockedSection();
                                if (!WindowManagerService.this.mAnimator.isAnimating()) {
                                    if (!WindowManagerService.this.mAnimator.isAnimationScheduled()) {
                                        if (WindowManagerService.this.mDisplayFrozen) {
                                            WindowManagerService.resetPriorityAfterLockedSection();
                                            return;
                                        }
                                    }
                                }
                                sendEmptyMessageDelayed(15, 2000);
                                return;
                            } finally {
                                WindowManagerService.resetPriorityAfterLockedSection();
                            }
                        }
                    case 16:
                        WindowManagerService.this.performEnableScreen();
                        break;
                    case 17:
                        synchronized (WindowManagerService.this.mGlobalLock) {
                            try {
                                WindowManagerService.boostPriorityForLockedSection();
                                Slog.w("WindowManager", "App freeze timeout expired.");
                                if (!WindowManagerService.this.mSimulateWindowFreezing) {
                                    WindowManagerService.this.mWindowsFreezingScreen = 2;
                                }
                                for (int i4 = WindowManagerService.this.mAppFreezeListeners.size() - 1; i4 >= 0; i4--) {
                                    WindowManagerService.this.mAppFreezeListeners.get(i4).onAppFreezeTimeout();
                                }
                            } finally {
                                WindowManagerService.resetPriorityAfterLockedSection();
                            }
                        }
                        break;
                    case 18:
                        DisplayContent displayContent6 = (DisplayContent) msg.obj;
                        removeMessages(18, displayContent6);
                        if (!displayContent6.isReady()) {
                            if (WindowManagerDebugConfig.DEBUG_CONFIGURATION) {
                                Slog.w("WindowManager", "Trying to send configuration to " + (displayContent6.getParent() == null ? "detached" : "unready") + " display=" + displayContent6);
                                break;
                            }
                        } else {
                            WindowManagerService.this.sendNewConfiguration(displayContent6.getDisplayId());
                            break;
                        }
                        break;
                    case 19:
                        if (WindowManagerService.this.mWindowsChanged) {
                            synchronized (WindowManagerService.this.mGlobalLock) {
                                try {
                                    WindowManagerService.boostPriorityForLockedSection();
                                    WindowManagerService.this.mWindowsChanged = false;
                                } catch (Throwable th2) {
                                    WindowManagerService.resetPriorityAfterLockedSection();
                                    throw th2;
                                }
                            }
                            WindowManagerService.resetPriorityAfterLockedSection();
                            WindowManagerService.this.notifyWindowsChanged();
                            break;
                        }
                        break;
                    default:
                        switch (i) {
                            case 22:
                                WindowManagerService.this.notifyHardKeyboardStatusChange();
                                break;
                            case 23:
                                WindowManagerService.this.performBootTimeout();
                                break;
                            case 24:
                                synchronized (WindowManagerService.this.mGlobalLock) {
                                    try {
                                        WindowManagerService.boostPriorityForLockedSection();
                                        Slog.w("WindowManager", "Timeout waiting for drawn: undrawn=" + WindowManagerService.this.mWaitingForDrawn);
                                        WindowManagerService.this.mWaitingForDrawn.clear();
                                        callback = WindowManagerService.this.mWaitingForDrawnCallback;
                                        dockedCallback = WindowManagerService.this.mDockedForDrawnCallback;
                                        WindowManagerService.this.mDockedForDrawnCallback = null;
                                        WindowManagerService.this.mWaitingForDrawnCallback = null;
                                    } finally {
                                        WindowManagerService.resetPriorityAfterLockedSection();
                                    }
                                }
                                if (callback != null) {
                                    callback.run();
                                }
                                if (dockedCallback != null) {
                                    dockedCallback.run();
                                    break;
                                }
                                break;
                            case 25:
                                WindowManagerService.this.showStrictModeViolation(msg.arg1, msg.arg2);
                                break;
                            default:
                                switch (i) {
                                    case 32:
                                        try {
                                            WindowManagerService.this.mActivityTaskManager.notifyActivityDrawn((IBinder) msg.obj);
                                            break;
                                        } catch (RemoteException e) {
                                            break;
                                        }
                                    case 33:
                                        synchronized (WindowManagerService.this.mGlobalLock) {
                                            try {
                                                WindowManagerService.boostPriorityForLockedSection();
                                                callback2 = WindowManagerService.this.mWaitingForDrawnCallback;
                                                WindowManagerService.this.mWaitingForDrawnCallback = null;
                                                dockedCallback2 = WindowManagerService.this.mDockedForDrawnCallback;
                                                WindowManagerService.this.mDockedForDrawnCallback = null;
                                            } finally {
                                                WindowManagerService.resetPriorityAfterLockedSection();
                                            }
                                        }
                                        if (callback2 != null) {
                                            callback2.run();
                                        }
                                        if (dockedCallback2 != null) {
                                            dockedCallback2.run();
                                            break;
                                        }
                                        break;
                                    case 34:
                                        float scale = WindowManagerService.this.getCurrentAnimatorScale();
                                        ValueAnimator.setDurationScale(scale);
                                        Session session = (Session) msg.obj;
                                        if (session != null) {
                                            int procState = Process.getProcessState(session.mPid);
                                            if (!(3 == procState || 2 == procState)) {
                                                ignoreCall = true;
                                            }
                                            if (ignoreCall) {
                                                if (WindowManagerDebugConfig.DEBUG_BINDER) {
                                                    Slog.i("WindowManager", "process " + session.mPid + " dead or suspend, ignore onAnimatorScaleChanged 01 call. " + procState);
                                                    break;
                                                }
                                            } else {
                                                try {
                                                    session.mCallback.onAnimatorScaleChanged(scale);
                                                    break;
                                                } catch (RemoteException e2) {
                                                    Slog.e("WindowManager", "call onAnimatorScaleChanged failed 01. session.uid=" + session.mUid + ", session.mPid=" + session.mPid + ", session.name=" + session.toString());
                                                    break;
                                                }
                                            }
                                        } else {
                                            ArrayList<IWindowSessionCallback> callbacks = new ArrayList<>();
                                            ArrayList<ProcessStates> procSuspendStates = new ArrayList<>();
                                            synchronized (WindowManagerService.this.mGlobalLock) {
                                                try {
                                                    WindowManagerService.boostPriorityForLockedSection();
                                                    for (int i5 = 0; i5 < WindowManagerService.this.mSessions.size(); i5++) {
                                                        Session tmpSession = WindowManagerService.this.mSessions.valueAt(i5);
                                                        callbacks.add(tmpSession.mCallback);
                                                        procSuspendStates.add(new ProcessStates(Process.getProcessState(tmpSession.mPid), tmpSession.mPid, tmpSession.mUid));
                                                    }
                                                } catch (Throwable th3) {
                                                    WindowManagerService.resetPriorityAfterLockedSection();
                                                    throw th3;
                                                }
                                            }
                                            WindowManagerService.resetPriorityAfterLockedSection();
                                            for (int i6 = 0; i6 < callbacks.size(); i6++) {
                                                if (!procSuspendStates.get(i6).mIgnoreCall) {
                                                    try {
                                                        callbacks.get(i6).onAnimatorScaleChanged(scale);
                                                    } catch (RemoteException e3) {
                                                        Slog.e("WindowManager", "call onAnimatorScaleChanged failed 02. session.uid=" + procSuspendStates.get(i6).mUid + ", session.mPid=" + procSuspendStates.get(i6).mPid);
                                                    }
                                                } else if (WindowManagerDebugConfig.DEBUG_BINDER) {
                                                    Slog.i("WindowManager", "process " + procSuspendStates.get(i6).mPid + " ignore onAnimatorScaleChanged 02 call. procState:" + procSuspendStates.get(i6).mProcState);
                                                }
                                            }
                                            break;
                                        }
                                        break;
                                    case 35:
                                        WindowManagerService windowManagerService4 = WindowManagerService.this;
                                        if (msg.arg1 == 1) {
                                            z3 = true;
                                        }
                                        windowManagerService4.showCircularMask(z3);
                                        break;
                                    case 36:
                                        WindowManagerService.this.showEmulatorDisplayOverlay();
                                        break;
                                    case 37:
                                        synchronized (WindowManagerService.this.mGlobalLock) {
                                            try {
                                                WindowManagerService.boostPriorityForLockedSection();
                                                if (WindowManagerDebugConfig.DEBUG_BOOT) {
                                                    Slog.i("WindowManager", "CHECK_IF_BOOT_ANIMATION_FINISHED:");
                                                }
                                                bootAnimationComplete = WindowManagerService.this.checkBootAnimationCompleteLocked();
                                            } finally {
                                                WindowManagerService.resetPriorityAfterLockedSection();
                                            }
                                        }
                                        if (bootAnimationComplete) {
                                            WindowManagerService.this.performEnableScreen();
                                            break;
                                        }
                                        break;
                                    case 38:
                                        synchronized (WindowManagerService.this.mGlobalLock) {
                                            try {
                                                WindowManagerService.boostPriorityForLockedSection();
                                                WindowManagerService.this.mLastANRState = null;
                                            } catch (Throwable th4) {
                                                WindowManagerService.resetPriorityAfterLockedSection();
                                                throw th4;
                                            }
                                        }
                                        WindowManagerService.resetPriorityAfterLockedSection();
                                        WindowManagerService.this.mAtmInternal.clearSavedANRState();
                                        break;
                                    case 39:
                                        synchronized (WindowManagerService.this.mGlobalLock) {
                                            try {
                                                WindowManagerService.boostPriorityForLockedSection();
                                                WallpaperController wallpaperController = (WallpaperController) msg.obj;
                                                if (wallpaperController != null && wallpaperController.processWallpaperDrawPendingTimeout()) {
                                                    WindowManagerService.this.mWindowPlacerLocked.performSurfacePlacement();
                                                }
                                            } finally {
                                                WindowManagerService.resetPriorityAfterLockedSection();
                                            }
                                        }
                                        break;
                                    default:
                                        switch (i) {
                                            case SET_HAS_OVERLAY_UI /* 58 */:
                                                ActivityManagerInternal activityManagerInternal = WindowManagerService.this.mAmInternal;
                                                int i7 = msg.arg1;
                                                if (msg.arg2 == 1) {
                                                    z2 = true;
                                                }
                                                activityManagerInternal.setHasOverlayUi(i7, z2);
                                                break;
                                            case SET_RUNNING_REMOTE_ANIMATION /* 59 */:
                                                ActivityManagerInternal activityManagerInternal2 = WindowManagerService.this.mAmInternal;
                                                int i8 = msg.arg1;
                                                if (msg.arg2 == 1) {
                                                    z = true;
                                                }
                                                activityManagerInternal2.setRunningRemoteAnimation(i8, z);
                                                break;
                                            case ANIMATION_FAILSAFE /* 60 */:
                                                synchronized (WindowManagerService.this.mGlobalLock) {
                                                    try {
                                                        WindowManagerService.boostPriorityForLockedSection();
                                                        if (WindowManagerService.this.mRecentsAnimationController != null) {
                                                            WindowManagerService.this.mRecentsAnimationController.scheduleFailsafe();
                                                        }
                                                    } finally {
                                                        WindowManagerService.resetPriorityAfterLockedSection();
                                                    }
                                                }
                                                break;
                                            case RECOMPUTE_FOCUS /* 61 */:
                                                synchronized (WindowManagerService.this.mGlobalLock) {
                                                    try {
                                                        WindowManagerService.boostPriorityForLockedSection();
                                                        WindowManagerService.this.updateFocusedWindowLocked(0, true);
                                                    } finally {
                                                        WindowManagerService.resetPriorityAfterLockedSection();
                                                    }
                                                }
                                                break;
                                            case ON_POINTER_DOWN_OUTSIDE_FOCUS /* 62 */:
                                                synchronized (WindowManagerService.this.mGlobalLock) {
                                                    try {
                                                        WindowManagerService.boostPriorityForLockedSection();
                                                        WindowManagerService.this.onPointerDownOutsideFocusLocked((IBinder) msg.obj);
                                                    } finally {
                                                        WindowManagerService.resetPriorityAfterLockedSection();
                                                    }
                                                }
                                                break;
                                            default:
                                                WindowManagerService.this.handleOppoMessage(msg, 1);
                                                break;
                                        }
                                }
                        }
                }
            } else {
                synchronized (WindowManagerService.this.mGlobalLock) {
                    try {
                        WindowManagerService.boostPriorityForLockedSection();
                        WindowManagerService.this.restorePointerIconLocked((DisplayContent) msg.obj, (float) msg.arg1, (float) msg.arg2);
                    } finally {
                        WindowManagerService.resetPriorityAfterLockedSection();
                    }
                }
            }
            if (WindowManagerDebugConfig.DEBUG_WINDOW_TRACE) {
                Slog.v("WindowManager", "handleMessage: exit");
            }
        }

        /* access modifiers changed from: package-private */
        public void sendNewMessageDelayed(int what, Object obj, long delayMillis) {
            removeMessages(what, obj);
            sendMessageDelayed(obtainMessage(what, obj), delayMillis);
        }
    }

    /* access modifiers changed from: package-private */
    public void destroyPreservedSurfaceLocked() {
        for (int i = this.mDestroyPreservedSurface.size() - 1; i >= 0; i--) {
            this.mDestroyPreservedSurface.get(i).mWinAnimator.destroyPreservedSurfaceLocked();
        }
        this.mDestroyPreservedSurface.clear();
    }

    public IWindowSession openSession(IWindowSessionCallback callback) {
        return new Session(this, callback);
    }

    public void getInitialDisplaySize(int displayId, Point size) {
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                DisplayContent displayContent = this.mRoot.getDisplayContent(displayId);
                if (displayContent != null && displayContent.hasAccess(Binder.getCallingUid())) {
                    size.x = displayContent.mInitialDisplayWidth;
                    size.y = displayContent.mInitialDisplayHeight;
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void getBaseDisplaySize(int displayId, Point size) {
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                DisplayContent displayContent = this.mRoot.getDisplayContent(displayId);
                if (displayContent != null && displayContent.hasAccess(Binder.getCallingUid())) {
                    size.x = displayContent.mBaseDisplayWidth;
                    size.y = displayContent.mBaseDisplayHeight;
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    /* JADX INFO: finally extract failed */
    public void setForcedDisplaySize(int displayId, int width, int height) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.WRITE_SECURE_SETTINGS") == 0) {
            long ident = Binder.clearCallingIdentity();
            try {
                synchronized (this.mGlobalLock) {
                    try {
                        boostPriorityForLockedSection();
                        DisplayContent displayContent = this.mRoot.getDisplayContent(displayId);
                        if (displayContent != null) {
                            displayContent.setForcedSize(width, height);
                        }
                    } catch (Throwable th) {
                        resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
                resetPriorityAfterLockedSection();
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        } else {
            throw new SecurityException("Must hold permission android.permission.WRITE_SECURE_SETTINGS");
        }
    }

    /* JADX INFO: finally extract failed */
    public void setForcedDisplayScalingMode(int displayId, int mode) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.WRITE_SECURE_SETTINGS") == 0) {
            long ident = Binder.clearCallingIdentity();
            try {
                synchronized (this.mGlobalLock) {
                    try {
                        boostPriorityForLockedSection();
                        DisplayContent displayContent = this.mRoot.getDisplayContent(displayId);
                        if (displayContent != null) {
                            displayContent.setForcedScalingMode(mode);
                        }
                    } catch (Throwable th) {
                        resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
                resetPriorityAfterLockedSection();
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        } else {
            throw new SecurityException("Must hold permission android.permission.WRITE_SECURE_SETTINGS");
        }
    }

    private boolean applyForcedPropertiesForDefaultDisplay() {
        int pos;
        boolean changed = false;
        DisplayContent displayContent = getDefaultDisplayContentLocked();
        String sizeStr = Settings.Global.getString(this.mContext.getContentResolver(), "display_size_forced");
        if (sizeStr == null || sizeStr.length() == 0) {
            sizeStr = SystemProperties.get(SIZE_OVERRIDE, (String) null);
        }
        boolean z = false;
        if (sizeStr != null && sizeStr.length() > 0 && (pos = sizeStr.indexOf(44)) > 0 && sizeStr.lastIndexOf(44) == pos) {
            try {
                int width = Integer.parseInt(sizeStr.substring(0, pos));
                int height = Integer.parseInt(sizeStr.substring(pos + 1));
                if (!(displayContent.mBaseDisplayWidth == width && displayContent.mBaseDisplayHeight == height)) {
                    Slog.i("WindowManager", "FORCED DISPLAY SIZE: " + width + "x" + height);
                    displayContent.updateBaseDisplayMetrics(width, height, displayContent.mBaseDisplayDensity);
                    changed = true;
                }
            } catch (NumberFormatException e) {
            }
        }
        int density = getForcedDisplayDensityForUserLocked(this.mCurrentUserId);
        if (!(density == 0 || density == displayContent.mBaseDisplayDensity)) {
            displayContent.mBaseDisplayDensity = density;
            changed = true;
        }
        int mode = Settings.Global.getInt(this.mContext.getContentResolver(), "display_scaling_force", 0);
        boolean z2 = displayContent.mDisplayScalingDisabled;
        if (mode != 0) {
            z = true;
        }
        if (z2 == z) {
            return changed;
        }
        Slog.i("WindowManager", "FORCED DISPLAY SCALING DISABLED");
        displayContent.mDisplayScalingDisabled = true;
        return true;
    }

    /* JADX INFO: finally extract failed */
    public void clearForcedDisplaySize(int displayId) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.WRITE_SECURE_SETTINGS") == 0) {
            long ident = Binder.clearCallingIdentity();
            try {
                synchronized (this.mGlobalLock) {
                    try {
                        boostPriorityForLockedSection();
                        DisplayContent displayContent = this.mRoot.getDisplayContent(displayId);
                        if (displayContent != null) {
                            displayContent.setForcedSize(displayContent.mInitialDisplayWidth, displayContent.mInitialDisplayHeight);
                        }
                    } catch (Throwable th) {
                        resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
                resetPriorityAfterLockedSection();
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        } else {
            throw new SecurityException("Must hold permission android.permission.WRITE_SECURE_SETTINGS");
        }
    }

    public int getInitialDisplayDensity(int displayId) {
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                DisplayContent displayContent = this.mRoot.getDisplayContent(displayId);
                if (displayContent == null || !displayContent.hasAccess(Binder.getCallingUid())) {
                    resetPriorityAfterLockedSection();
                    return -1;
                }
                return displayContent.mInitialDisplayDensity;
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public int getBaseDisplayDensity(int displayId) {
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                DisplayContent displayContent = this.mRoot.getDisplayContent(displayId);
                if (displayContent == null || !displayContent.hasAccess(Binder.getCallingUid())) {
                    resetPriorityAfterLockedSection();
                    return -1;
                }
                return displayContent.mBaseDisplayDensity;
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    /* JADX INFO: finally extract failed */
    public void setForcedDisplayDensityForUser(int displayId, int density, int userId) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.WRITE_SECURE_SETTINGS") == 0) {
            int targetUserId = ActivityManager.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, false, true, "setForcedDisplayDensityForUser", null);
            long ident = Binder.clearCallingIdentity();
            try {
                synchronized (this.mGlobalLock) {
                    try {
                        boostPriorityForLockedSection();
                        DisplayContent displayContent = this.mRoot.getDisplayContent(displayId);
                        if (displayContent != null) {
                            displayContent.setForcedDensity(density, targetUserId);
                        }
                    } catch (Throwable th) {
                        resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
                resetPriorityAfterLockedSection();
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        } else {
            throw new SecurityException("Must hold permission android.permission.WRITE_SECURE_SETTINGS");
        }
    }

    /* JADX INFO: finally extract failed */
    public void clearForcedDisplayDensityForUser(int displayId, int userId) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.WRITE_SECURE_SETTINGS") == 0) {
            int callingUserId = ActivityManager.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, false, true, "clearForcedDisplayDensityForUser", null);
            long ident = Binder.clearCallingIdentity();
            try {
                synchronized (this.mGlobalLock) {
                    try {
                        boostPriorityForLockedSection();
                        DisplayContent displayContent = this.mRoot.getDisplayContent(displayId);
                        if (displayContent != null) {
                            displayContent.setForcedDensity(displayContent.mInitialDisplayDensity, callingUserId);
                        }
                    } catch (Throwable th) {
                        resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
                resetPriorityAfterLockedSection();
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        } else {
            throw new SecurityException("Must hold permission android.permission.WRITE_SECURE_SETTINGS");
        }
    }

    private int getForcedDisplayDensityForUserLocked(int userId) {
        String densityStr = Settings.Secure.getStringForUser(this.mContext.getContentResolver(), "display_density_forced", userId);
        if (densityStr == null || densityStr.length() == 0) {
            densityStr = SystemProperties.get(DENSITY_OVERRIDE, (String) null);
        }
        if (densityStr == null || densityStr.length() <= 0) {
            return 0;
        }
        try {
            return Integer.parseInt(densityStr);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /* access modifiers changed from: package-private */
    public void reconfigureDisplayLocked(DisplayContent displayContent) {
        if (displayContent.isReady()) {
            displayContent.configureDisplayPolicy();
            displayContent.setLayoutNeeded();
            boolean configChanged = displayContent.updateOrientationFromAppTokens();
            Configuration currentDisplayConfig = displayContent.getConfiguration();
            this.mTempConfiguration.setTo(currentDisplayConfig);
            displayContent.computeScreenConfiguration(this.mTempConfiguration);
            if (configChanged || (currentDisplayConfig.diff(this.mTempConfiguration) != 0)) {
                displayContent.mWaitingForConfig = true;
                startFreezingDisplayLocked(0, 0, displayContent);
                displayContent.sendNewConfiguration();
            }
            this.mWindowPlacerLocked.performSurfacePlacement();
        }
    }

    /* JADX INFO: finally extract failed */
    public void setOverscan(int displayId, int left, int top, int right, int bottom) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.WRITE_SECURE_SETTINGS") == 0) {
            long ident = Binder.clearCallingIdentity();
            try {
                synchronized (this.mGlobalLock) {
                    try {
                        boostPriorityForLockedSection();
                        DisplayContent displayContent = this.mRoot.getDisplayContent(displayId);
                        if (displayContent != null) {
                            setOverscanLocked(displayContent, left, top, right, bottom);
                        }
                    } catch (Throwable th) {
                        resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
                resetPriorityAfterLockedSection();
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        } else {
            throw new SecurityException("Must hold permission android.permission.WRITE_SECURE_SETTINGS");
        }
    }

    private void setOverscanLocked(DisplayContent displayContent, int left, int top, int right, int bottom) {
        DisplayInfo displayInfo = displayContent.getDisplayInfo();
        displayInfo.overscanLeft = left;
        displayInfo.overscanTop = top;
        displayInfo.overscanRight = right;
        displayInfo.overscanBottom = bottom;
        this.mDisplayWindowSettings.setOverscanLocked(displayInfo, left, top, right, bottom);
        reconfigureDisplayLocked(displayContent);
    }

    public void startWindowTrace() {
        this.mWindowTracing.startTrace(null);
    }

    public void stopWindowTrace() {
        this.mWindowTracing.stopTrace(null);
    }

    public boolean isWindowTraceEnabled() {
        return this.mWindowTracing.isEnabled();
    }

    /* access modifiers changed from: package-private */
    public final WindowState windowForClientLocked(Session session, IWindow client, boolean throwOnError) {
        return windowForClientLocked(session, client.asBinder(), throwOnError);
    }

    /* access modifiers changed from: package-private */
    public final WindowState windowForClientLocked(Session session, IBinder client, boolean throwOnError) {
        WindowState win = (WindowState) this.mWindowMap.get(client);
        if (localLOGV) {
            Slog.v("WindowManager", "Looking up client " + client + ": " + win);
        }
        if (win == null) {
            if (!throwOnError) {
                Slog.w("WindowManager", "Failed looking up window callers=" + Debug.getCallers(3));
                return null;
            }
            throw new IllegalArgumentException("Requested window " + client + " does not exist");
        } else if (session == null || win.mSession == session) {
            return win;
        } else {
            if (!throwOnError) {
                Slog.w("WindowManager", "Failed looking up window callers=" + Debug.getCallers(3));
                return null;
            }
            throw new IllegalArgumentException("Requested window " + client + " is in session " + win.mSession + ", not " + session);
        }
    }

    /* access modifiers changed from: package-private */
    public void makeWindowFreezingScreenIfNeededLocked(WindowState w) {
        if (!w.mToken.okToDisplay() && this.mWindowsFreezingScreen != 2) {
            if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                Slog.v("WindowManager", "Changing surface while display frozen: " + w);
            }
            w.setOrientationChanging(true);
            w.mLastFreezeDuration = 0;
            this.mRoot.mOrientationChangeComplete = false;
            if (this.mWindowsFreezingScreen == 0) {
                this.mWindowsFreezingScreen = 1;
                this.mH.sendNewMessageDelayed(11, w.getDisplayContent(), 2000);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void checkDrawnWindowsLocked() {
        if (this.mWaitingForDrawn.isEmpty()) {
            return;
        }
        if (this.mWaitingForDrawnCallback != null || this.mDockedForDrawnCallback != null) {
            int j = this.mWaitingForDrawn.size();
            while (true) {
                j--;
                if (j < 0) {
                    break;
                }
                WindowState win = this.mWaitingForDrawn.get(j);
                if (WindowManagerDebugConfig.DEBUG_SCREEN_ON) {
                    Slog.i("WindowManager", "Waiting for drawn " + win + ": removed=" + win.mRemoved + " visible=" + win.isVisibleLw() + " mHasSurface=" + win.mHasSurface + " drawState=" + win.mWinAnimator.mDrawState);
                }
                if (win.mRemoved || !win.mHasSurface || !win.isVisibleByPolicy()) {
                    if (WindowManagerDebugConfig.DEBUG_SCREEN_ON) {
                        Slog.w("WindowManager", "Aborted waiting for drawn: " + win);
                    }
                    this.mWaitingForDrawn.remove(win);
                } else if (win.hasDrawnLw()) {
                    if (WindowManagerDebugConfig.DEBUG_SCREEN_ON) {
                        Slog.d("WindowManager", "Window drawn win=" + win);
                    }
                    this.mWaitingForDrawn.remove(win);
                }
            }
            if (this.mWaitingForDrawn.isEmpty()) {
                if (WindowManagerDebugConfig.DEBUG_SCREEN_ON) {
                    Slog.d("WindowManager", "All windows drawn!");
                }
                this.mH.removeMessages(24);
                this.mH.sendEmptyMessage(33);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setHoldScreenLocked(Session newHoldScreen) {
        boolean hold = newHoldScreen != null;
        if (hold && this.mHoldingScreenOn != newHoldScreen) {
            this.mHoldingScreenWakeLock.setWorkSource(new WorkSource(newHoldScreen.mUid));
        }
        this.mHoldingScreenOn = newHoldScreen;
        if (hold == this.mHoldingScreenWakeLock.isHeld()) {
            return;
        }
        if (hold) {
            if (WindowManagerDebugConfig.DEBUG_KEEP_SCREEN_ON) {
                Slog.d("DebugKeepScreenOn", "Acquiring screen wakelock due to " + this.mRoot.mHoldScreenWindow);
            }
            this.mLastWakeLockHoldingWindow = this.mRoot.mHoldScreenWindow;
            this.mLastWakeLockObscuringWindow = null;
            this.mHoldingScreenWakeLock.acquire();
            this.mPolicy.keepScreenOnStartedLw();
            return;
        }
        if (WindowManagerDebugConfig.DEBUG_KEEP_SCREEN_ON) {
            Slog.d("DebugKeepScreenOn", "Releasing screen wakelock, obscured by " + this.mRoot.mObscuringWindow);
        }
        this.mLastWakeLockHoldingWindow = null;
        this.mLastWakeLockObscuringWindow = this.mRoot.mObscuringWindow;
        this.mPolicy.keepScreenOnStoppedLw();
        this.mHoldingScreenWakeLock.release();
    }

    /* access modifiers changed from: package-private */
    public void requestTraversal() {
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                this.mWindowPlacerLocked.requestTraversal();
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void scheduleAnimationLocked() {
        WindowAnimator windowAnimator = this.mAnimator;
        if (windowAnimator != null) {
            windowAnimator.scheduleAnimation();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean updateFocusedWindowLocked(int mode, boolean updateInputWindows) {
        Trace.traceBegin(32, "wmUpdateFocus");
        boolean changed = this.mRoot.updateFocusedWindowLocked(mode, updateInputWindows);
        Trace.traceEnd(32);
        return changed;
    }

    /* access modifiers changed from: package-private */
    public void startFreezingDisplayLocked(int exitAnim, int enterAnim) {
        startFreezingDisplayLocked(exitAnim, enterAnim, getDefaultDisplayContentLocked());
    }

    /* access modifiers changed from: package-private */
    public void startFreezingDisplayLocked(int exitAnim, int enterAnim, DisplayContent displayContent) {
        if (!this.mDisplayFrozen && !this.mRotatingSeamlessly && displayContent.isReady() && this.mPolicy.isScreenOn() && displayContent.okToAnimate()) {
            if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                Slog.d("WindowManager", "startFreezingDisplayLocked: exitAnim=" + exitAnim + " enterAnim=" + enterAnim + " called by " + Debug.getCallers(8));
            }
            this.mScreenFrozenLock.acquire();
            this.mDisplayFrozen = true;
            this.mDisplayFreezeTime = SystemClock.elapsedRealtime();
            this.mLastFinishedFreezeSource = null;
            this.mFrozenDisplayId = displayContent.getDisplayId();
            this.mInputManagerCallback.freezeInputDispatchingLw();
            if (displayContent.mAppTransition.isTransitionSet()) {
                displayContent.mAppTransition.freeze();
            }
            if (PROFILE_ORIENTATION) {
                Debug.startMethodTracing(new File("/data/system/frozen").toString(), DumpState.DUMP_VOLUMES);
            }
            this.mLatencyTracker.onActionStart(6);
            this.mExitAnimId = exitAnim;
            this.mEnterAnimId = enterAnim;
            ScreenRotationAnimation screenRotationAnimation = this.mAnimator.getScreenRotationAnimationLocked(this.mFrozenDisplayId);
            if (screenRotationAnimation != null) {
                screenRotationAnimation.kill();
            }
            boolean isSecure = displayContent.hasSecureWindowOnScreen();
            displayContent.updateDisplayInfo();
            this.mAnimator.setScreenRotationAnimationLocked(this.mFrozenDisplayId, new ScreenRotationAnimation(this.mContext, displayContent, displayContent.getDisplayRotation().isFixedToUserRotation(), isSecure, this));
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x011b  */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x014d  */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x0159  */
    public void stopFreezingDisplayLocked() {
        boolean configChanged;
        ScreenRotationAnimation screenRotationAnimation;
        if (this.mDisplayFrozen) {
            DisplayContent displayContent = this.mRoot.getDisplayContent(this.mFrozenDisplayId);
            boolean waitingForConfig = displayContent != null && displayContent.mWaitingForConfig;
            int numOpeningApps = displayContent != null ? displayContent.mOpeningApps.size() : 0;
            if (!waitingForConfig && this.mAppsFreezingScreen <= 0 && this.mWindowsFreezingScreen != 1 && !this.mClientFreezingScreen && numOpeningApps <= 0) {
                if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                    Slog.d("WindowManager", "stopFreezingDisplayLocked: Unfreezing now");
                }
                int displayId = this.mFrozenDisplayId;
                this.mFrozenDisplayId = -1;
                this.mDisplayFrozen = false;
                this.mInputManagerCallback.thawInputDispatchingLw();
                this.mLastDisplayFreezeDuration = (int) (SystemClock.elapsedRealtime() - this.mDisplayFreezeTime);
                StringBuilder sb = new StringBuilder(128);
                sb.append("Screen frozen for ");
                TimeUtils.formatDuration((long) this.mLastDisplayFreezeDuration, sb);
                if (this.mLastFinishedFreezeSource != null) {
                    sb.append(" due to ");
                    sb.append(this.mLastFinishedFreezeSource);
                }
                Slog.i("WindowManager", sb.toString());
                this.mH.removeMessages(17);
                this.mH.removeMessages(30);
                if (PROFILE_ORIENTATION) {
                    Debug.stopMethodTracing();
                }
                boolean updateRotation = false;
                ScreenRotationAnimation screenRotationAnimation2 = this.mAnimator.getScreenRotationAnimationLocked(displayId);
                if (screenRotationAnimation2 == null) {
                    screenRotationAnimation = null;
                } else if (screenRotationAnimation2.hasScreenshot()) {
                    if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                        Slog.i("WindowManager", "**** Dismissing screen rotation animation");
                    }
                    DisplayInfo displayInfo = displayContent.getDisplayInfo();
                    if (!displayContent.getDisplayPolicy().validateRotationAnimationLw(this.mExitAnimId, this.mEnterAnimId, false)) {
                        this.mEnterAnimId = 0;
                        this.mExitAnimId = 0;
                    }
                    if (screenRotationAnimation2.dismiss(this.mTransaction, 10000, getTransitionAnimationScaleLocked(), displayInfo.logicalWidth, displayInfo.logicalHeight, this.mExitAnimId, this.mEnterAnimId)) {
                        this.mTransaction.apply();
                        scheduleAnimationLocked();
                    } else {
                        screenRotationAnimation2.kill();
                        this.mAnimator.setScreenRotationAnimationLocked(displayId, null);
                        updateRotation = true;
                    }
                    if (!"0".equals(SystemProperties.get(APP_FROZEN_TIMEOUT_PROP))) {
                        SystemProperties.set(APP_FROZEN_TIMEOUT_PROP, "0");
                    }
                    configChanged = displayContent == null && displayContent.updateOrientationFromAppTokens();
                    handleUiModeChanged();
                    this.mH.removeMessages(15);
                    this.mH.sendEmptyMessageDelayed(15, 2000);
                    this.mScreenFrozenLock.release();
                    if (updateRotation && displayContent != null && updateRotation) {
                        if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                            Slog.d("WindowManager", "Performing post-rotate rotation");
                        }
                        configChanged |= displayContent.updateRotationUnchecked();
                    }
                    if (configChanged) {
                        displayContent.sendNewConfiguration();
                    }
                    this.mLatencyTracker.onActionEnd(6);
                } else {
                    screenRotationAnimation = null;
                }
                if (screenRotationAnimation2 != null) {
                    screenRotationAnimation2.kill();
                    this.mAnimator.setScreenRotationAnimationLocked(displayId, screenRotationAnimation);
                }
                updateRotation = true;
                if (!"0".equals(SystemProperties.get(APP_FROZEN_TIMEOUT_PROP))) {
                }
                configChanged = displayContent == null && displayContent.updateOrientationFromAppTokens();
                handleUiModeChanged();
                this.mH.removeMessages(15);
                this.mH.sendEmptyMessageDelayed(15, 2000);
                this.mScreenFrozenLock.release();
                if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                }
                configChanged |= displayContent.updateRotationUnchecked();
                if (configChanged) {
                }
                this.mLatencyTracker.onActionEnd(6);
            } else if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                Slog.d("WindowManager", "stopFreezingDisplayLocked: Returning mWaitingForConfig=" + waitingForConfig + ", mAppsFreezingScreen=" + this.mAppsFreezingScreen + ", mWindowsFreezingScreen=" + this.mWindowsFreezingScreen + ", mClientFreezingScreen=" + this.mClientFreezingScreen + ", mOpeningApps.size()=" + numOpeningApps);
            }
        }
    }

    static int getPropertyInt(String[] tokens, int index, int defUnits, int defDps, DisplayMetrics dm) {
        String str;
        if (index < tokens.length && (str = tokens[index]) != null && str.length() > 0) {
            try {
                return Integer.parseInt(str);
            } catch (Exception e) {
            }
        }
        if (defUnits == 0) {
            return defDps;
        }
        return (int) TypedValue.applyDimension(defUnits, (float) defDps, dm);
    }

    /* access modifiers changed from: package-private */
    public void createWatermarkInTransaction() {
        String[] toks;
        if (this.mWatermark == null) {
            FileInputStream in = null;
            DataInputStream ind = null;
            try {
                DataInputStream ind2 = new DataInputStream(new FileInputStream(new File("/system/etc/setup.conf")));
                String line = ind2.readLine();
                if (!(line == null || (toks = line.split("%")) == null || toks.length <= 0)) {
                    DisplayContent displayContent = getDefaultDisplayContentLocked();
                    this.mWatermark = new Watermark(displayContent, displayContent.mRealDisplayMetrics, toks);
                }
                try {
                    ind2.close();
                } catch (IOException e) {
                }
            } catch (FileNotFoundException e2) {
                if (0 != 0) {
                    ind.close();
                } else if (0 != 0) {
                    in.close();
                }
            } catch (IOException e3) {
                if (0 != 0) {
                    ind.close();
                } else if (0 != 0) {
                    try {
                        in.close();
                    } catch (IOException e4) {
                    }
                }
            } catch (Throwable th) {
                if (0 != 0) {
                    try {
                        ind.close();
                    } catch (IOException e5) {
                    }
                } else if (0 != 0) {
                    try {
                        in.close();
                    } catch (IOException e6) {
                    }
                }
                throw th;
            }
        }
    }

    public void setRecentsVisibility(boolean visible) {
        this.mAtmInternal.enforceCallerIsRecentsOrHasPermission("android.permission.STATUS_BAR", "setRecentsVisibility()");
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                this.mPolicy.setRecentsVisibilityLw(visible);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void setPipVisibility(boolean visible) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.STATUS_BAR") == 0) {
            synchronized (this.mGlobalLock) {
                try {
                    boostPriorityForLockedSection();
                    this.mPolicy.setPipVisibilityLw(visible);
                } finally {
                    resetPriorityAfterLockedSection();
                }
            }
            return;
        }
        throw new SecurityException("Caller does not hold permission android.permission.STATUS_BAR");
    }

    public void setShelfHeight(boolean visible, int shelfHeight) {
        this.mAtmInternal.enforceCallerIsRecentsOrHasPermission("android.permission.STATUS_BAR", "setShelfHeight()");
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                getDefaultDisplayContentLocked().getPinnedStackController().setAdjustedForShelf(visible, shelfHeight);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void statusBarVisibilityChanged(int displayId, int visibility) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.STATUS_BAR") == 0) {
            synchronized (this.mGlobalLock) {
                try {
                    boostPriorityForLockedSection();
                    DisplayContent displayContent = this.mRoot.getDisplayContent(displayId);
                    if (displayContent != null) {
                        displayContent.statusBarVisibilityChanged(visibility);
                    } else {
                        Slog.w("WindowManager", "statusBarVisibilityChanged with invalid displayId=" + displayId);
                    }
                } finally {
                    resetPriorityAfterLockedSection();
                }
            }
            return;
        }
        throw new SecurityException("Caller does not hold permission android.permission.STATUS_BAR");
    }

    public void setForceShowSystemBars(boolean show) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.STATUS_BAR") == 0) {
            synchronized (this.mGlobalLock) {
                try {
                    boostPriorityForLockedSection();
                    this.mRoot.forAllDisplayPolicies(PooledLambda.obtainConsumer($$Lambda$XcHmyRxMY5ULhjLiVsIKnPtvOM.INSTANCE, PooledLambda.__(), Boolean.valueOf(show)));
                } finally {
                    resetPriorityAfterLockedSection();
                }
            }
            return;
        }
        throw new SecurityException("Caller does not hold permission android.permission.STATUS_BAR");
    }

    public void setNavBarVirtualKeyHapticFeedbackEnabled(boolean enabled) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.STATUS_BAR") == 0) {
            synchronized (this.mGlobalLock) {
                try {
                    boostPriorityForLockedSection();
                    this.mPolicy.setNavBarVirtualKeyHapticFeedbackEnabledLw(enabled);
                } finally {
                    resetPriorityAfterLockedSection();
                }
            }
            return;
        }
        throw new SecurityException("Caller does not hold permission android.permission.STATUS_BAR");
    }

    public int getNavBarPosition(int displayId) {
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                DisplayContent displayContent = this.mRoot.getDisplayContent(displayId);
                if (displayContent == null) {
                    Slog.w("WindowManager", "getNavBarPosition with invalid displayId=" + displayId + " callers=" + Debug.getCallers(3));
                    return -1;
                }
                displayContent.performLayout(false, false);
                int navBarPosition = displayContent.getDisplayPolicy().getNavBarPosition();
                resetPriorityAfterLockedSection();
                return navBarPosition;
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    @Override // com.android.server.policy.WindowManagerPolicy.WindowManagerFuncs
    public WindowManagerPolicy.InputConsumer createInputConsumer(Looper looper, String name, InputEventReceiver.Factory inputEventReceiverFactory, int displayId) {
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                DisplayContent displayContent = this.mRoot.getDisplayContent(displayId);
                if (displayContent != null) {
                    return displayContent.getInputMonitor().createInputConsumer(looper, name, inputEventReceiverFactory);
                }
                resetPriorityAfterLockedSection();
                return null;
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void createInputConsumer(IBinder token, String name, int displayId, InputChannel inputChannel) {
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                DisplayContent display = this.mRoot.getDisplayContent(displayId);
                if (display != null) {
                    display.getInputMonitor().createInputConsumer(token, name, inputChannel, Binder.getCallingPid(), Binder.getCallingUserHandle());
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public boolean destroyInputConsumer(String name, int displayId) {
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                DisplayContent display = this.mRoot.getDisplayContent(displayId);
                if (display != null) {
                    return display.getInputMonitor().destroyInputConsumer(name);
                }
                resetPriorityAfterLockedSection();
                return false;
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public Region getCurrentImeTouchRegion() {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.RESTRICTED_VR_ACCESS") == 0) {
            synchronized (this.mGlobalLock) {
                try {
                    boostPriorityForLockedSection();
                    Region r = new Region();
                    for (int i = this.mRoot.mChildren.size() - 1; i >= 0; i--) {
                        DisplayContent displayContent = (DisplayContent) this.mRoot.mChildren.get(i);
                        if (displayContent.mInputMethodWindow != null) {
                            displayContent.mInputMethodWindow.getTouchableRegion(r);
                            return r;
                        }
                    }
                    resetPriorityAfterLockedSection();
                    return r;
                } finally {
                    resetPriorityAfterLockedSection();
                }
            }
        } else {
            throw new SecurityException("getCurrentImeTouchRegion is restricted to VR services");
        }
    }

    public boolean hasNavigationBar(int displayId) {
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                DisplayContent dc = this.mRoot.getDisplayContent(displayId);
                if (dc == null) {
                    return false;
                }
                boolean hasNavigationBar = dc.getDisplayPolicy().hasNavigationBar();
                resetPriorityAfterLockedSection();
                return hasNavigationBar;
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void lockNow(Bundle options) {
        this.mPolicy.lockNow(options);
    }

    public void showRecentApps() {
        this.mPolicy.showRecentApps();
    }

    public boolean isSafeModeEnabled() {
        return this.mSafeMode;
    }

    public boolean clearWindowContentFrameStats(IBinder token) {
        if (checkCallingPermission("android.permission.FRAME_STATS", "clearWindowContentFrameStats()")) {
            synchronized (this.mGlobalLock) {
                try {
                    boostPriorityForLockedSection();
                    WindowState windowState = (WindowState) this.mWindowMap.get(token);
                    if (windowState == null) {
                        return false;
                    }
                    WindowSurfaceController surfaceController = windowState.mWinAnimator.mSurfaceController;
                    if (surfaceController == null) {
                        resetPriorityAfterLockedSection();
                        return false;
                    }
                    boolean clearWindowContentFrameStats = surfaceController.clearWindowContentFrameStats();
                    resetPriorityAfterLockedSection();
                    return clearWindowContentFrameStats;
                } finally {
                    resetPriorityAfterLockedSection();
                }
            }
        } else {
            throw new SecurityException("Requires FRAME_STATS permission");
        }
    }

    public WindowContentFrameStats getWindowContentFrameStats(IBinder token) {
        if (checkCallingPermission("android.permission.FRAME_STATS", "getWindowContentFrameStats()")) {
            synchronized (this.mGlobalLock) {
                try {
                    boostPriorityForLockedSection();
                    WindowState windowState = (WindowState) this.mWindowMap.get(token);
                    if (windowState == null) {
                        return null;
                    }
                    WindowSurfaceController surfaceController = windowState.mWinAnimator.mSurfaceController;
                    if (surfaceController == null) {
                        resetPriorityAfterLockedSection();
                        return null;
                    }
                    if (this.mTempWindowRenderStats == null) {
                        this.mTempWindowRenderStats = new WindowContentFrameStats();
                    }
                    WindowContentFrameStats stats = this.mTempWindowRenderStats;
                    if (!surfaceController.getWindowContentFrameStats(stats)) {
                        resetPriorityAfterLockedSection();
                        return null;
                    }
                    resetPriorityAfterLockedSection();
                    return stats;
                } finally {
                    resetPriorityAfterLockedSection();
                }
            }
        } else {
            throw new SecurityException("Requires FRAME_STATS permission");
        }
    }

    public void notifyAppRelaunching(IBinder token) {
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                AppWindowToken appWindow = this.mRoot.getAppWindowToken(token);
                if (appWindow != null) {
                    appWindow.startRelaunching();
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void notifyAppRelaunchingFinished(IBinder token) {
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                AppWindowToken appWindow = this.mRoot.getAppWindowToken(token);
                if (appWindow != null) {
                    appWindow.finishRelaunching();
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void notifyAppRelaunchesCleared(IBinder token) {
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                AppWindowToken appWindow = this.mRoot.getAppWindowToken(token);
                if (appWindow != null) {
                    appWindow.clearRelaunching();
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void notifyAppResumedFinished(IBinder token) {
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                AppWindowToken appWindow = this.mRoot.getAppWindowToken(token);
                if (appWindow != null) {
                    appWindow.getDisplayContent().mUnknownAppVisibilityController.notifyAppResumedFinished(appWindow);
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void notifyTaskRemovedFromRecents(int taskId, int userId) {
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                this.mTaskSnapshotController.notifyTaskRemovedFromRecents(taskId, userId);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    private void dumpPolicyLocked(PrintWriter pw, String[] args, boolean dumpAll) {
        pw.println("WINDOW MANAGER POLICY STATE (dumpsys window policy)");
        this.mPolicy.dump("    ", pw, args);
    }

    private void dumpAnimatorLocked(PrintWriter pw, String[] args, boolean dumpAll) {
        pw.println("WINDOW MANAGER ANIMATOR STATE (dumpsys window animator)");
        this.mAnimator.dumpLocked(pw, "    ", dumpAll);
    }

    private void dumpTokensLocked(PrintWriter pw, boolean dumpAll) {
        pw.println("WINDOW MANAGER TOKENS (dumpsys window tokens)");
        this.mRoot.dumpTokens(pw, dumpAll);
    }

    private void dumpTraceStatus(PrintWriter pw) {
        pw.println("WINDOW MANAGER TRACE (dumpsys window trace)");
        pw.print(this.mWindowTracing.getStatus() + StringUtils.LF);
    }

    private void dumpSessionsLocked(PrintWriter pw, boolean dumpAll) {
        pw.println("WINDOW MANAGER SESSIONS (dumpsys window sessions)");
        for (int i = 0; i < this.mSessions.size(); i++) {
            Session s = this.mSessions.valueAt(i);
            pw.print("  Session ");
            pw.print(s);
            pw.println(':');
            s.dump(pw, "    ");
        }
    }

    /* access modifiers changed from: package-private */
    public void writeToProtoLocked(ProtoOutputStream proto, int logLevel) {
        this.mPolicy.writeToProto(proto, 1146756268033L);
        this.mRoot.writeToProto(proto, 1146756268034L, logLevel);
        DisplayContent topFocusedDisplayContent = this.mRoot.getTopFocusedDisplayContent();
        if (topFocusedDisplayContent.mCurrentFocus != null) {
            topFocusedDisplayContent.mCurrentFocus.writeIdentifierToProto(proto, 1146756268035L);
        }
        if (topFocusedDisplayContent.mFocusedApp != null) {
            topFocusedDisplayContent.mFocusedApp.writeNameToProto(proto, 1138166333444L);
        }
        WindowState imeWindow = this.mRoot.getCurrentInputMethodWindow();
        if (imeWindow != null) {
            imeWindow.writeIdentifierToProto(proto, 1146756268037L);
        }
        proto.write(1133871366150L, this.mDisplayFrozen);
        DisplayContent defaultDisplayContent = getDefaultDisplayContentLocked();
        proto.write(1120986464263L, defaultDisplayContent.getRotation());
        proto.write(1120986464264L, defaultDisplayContent.getLastOrientation());
    }

    private void dumpWindowsLocked(PrintWriter pw, boolean dumpAll, ArrayList<WindowState> windows) {
        pw.println("WINDOW MANAGER WINDOWS (dumpsys window windows)");
        dumpWindowsNoHeaderLocked(pw, dumpAll, windows);
    }

    private void dumpWindowsNoHeaderLocked(PrintWriter pw, boolean dumpAll, ArrayList<WindowState> windows) {
        this.mRoot.dumpWindowsNoHeader(pw, dumpAll, windows);
        if (!this.mHidingNonSystemOverlayWindows.isEmpty()) {
            pw.println();
            pw.println("  Hiding System Alert Windows:");
            for (int i = this.mHidingNonSystemOverlayWindows.size() - 1; i >= 0; i--) {
                WindowState w = this.mHidingNonSystemOverlayWindows.get(i);
                pw.print("  #");
                pw.print(i);
                pw.print(' ');
                pw.print(w);
                if (dumpAll) {
                    pw.println(":");
                    w.dump(pw, "    ", true);
                } else {
                    pw.println();
                }
            }
        }
        if (this.mPendingRemove.size() > 0) {
            pw.println();
            pw.println("  Remove pending for:");
            for (int i2 = this.mPendingRemove.size() - 1; i2 >= 0; i2--) {
                WindowState w2 = this.mPendingRemove.get(i2);
                if (windows == null || windows.contains(w2)) {
                    pw.print("  Remove #");
                    pw.print(i2);
                    pw.print(' ');
                    pw.print(w2);
                    if (dumpAll) {
                        pw.println(":");
                        w2.dump(pw, "    ", true);
                    } else {
                        pw.println();
                    }
                }
            }
        }
        ArrayList<WindowState> arrayList = this.mForceRemoves;
        if (arrayList != null && arrayList.size() > 0) {
            pw.println();
            pw.println("  Windows force removing:");
            for (int i3 = this.mForceRemoves.size() - 1; i3 >= 0; i3--) {
                WindowState w3 = this.mForceRemoves.get(i3);
                pw.print("  Removing #");
                pw.print(i3);
                pw.print(' ');
                pw.print(w3);
                if (dumpAll) {
                    pw.println(":");
                    w3.dump(pw, "    ", true);
                } else {
                    pw.println();
                }
            }
        }
        if (this.mDestroySurface.size() > 0) {
            pw.println();
            pw.println("  Windows waiting to destroy their surface:");
            for (int i4 = this.mDestroySurface.size() - 1; i4 >= 0; i4--) {
                WindowState w4 = this.mDestroySurface.get(i4);
                if (windows == null || windows.contains(w4)) {
                    pw.print("  Destroy #");
                    pw.print(i4);
                    pw.print(' ');
                    pw.print(w4);
                    if (dumpAll) {
                        pw.println(":");
                        w4.dump(pw, "    ", true);
                    } else {
                        pw.println();
                    }
                }
            }
        }
        if (this.mResizingWindows.size() > 0) {
            pw.println();
            pw.println("  Windows waiting to resize:");
            for (int i5 = this.mResizingWindows.size() - 1; i5 >= 0; i5--) {
                WindowState w5 = this.mResizingWindows.get(i5);
                if (windows == null || windows.contains(w5)) {
                    pw.print("  Resizing #");
                    pw.print(i5);
                    pw.print(' ');
                    pw.print(w5);
                    if (dumpAll) {
                        pw.println(":");
                        w5.dump(pw, "    ", true);
                    } else {
                        pw.println();
                    }
                }
            }
        }
        if (this.mWaitingForDrawn.size() > 0) {
            pw.println();
            pw.println("  Clients waiting for these windows to be drawn:");
            for (int i6 = this.mWaitingForDrawn.size() - 1; i6 >= 0; i6--) {
                pw.print("  Waiting #");
                pw.print(i6);
                pw.print(' ');
                pw.print(this.mWaitingForDrawn.get(i6));
            }
        }
        pw.println();
        pw.print("  mGlobalConfiguration=");
        pw.println(this.mRoot.getConfiguration());
        pw.print("  mHasPermanentDpad=");
        pw.println(this.mHasPermanentDpad);
        this.mRoot.dumpTopFocusedDisplayId(pw);
        this.mRoot.forAllDisplays(new Consumer(pw) {
            /* class com.android.server.wm.$$Lambda$WindowManagerService$pgbw_FPqeLJMP83kqiaVcOeiDs */
            private final /* synthetic */ PrintWriter f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                WindowManagerService.lambda$dumpWindowsNoHeaderLocked$7(this.f$0, (DisplayContent) obj);
            }
        });
        pw.print("  mInTouchMode=");
        pw.println(this.mInTouchMode);
        pw.print("  mLastDisplayFreezeDuration=");
        TimeUtils.formatDuration((long) this.mLastDisplayFreezeDuration, pw);
        if (this.mLastFinishedFreezeSource != null) {
            pw.print(" due to ");
            pw.print(this.mLastFinishedFreezeSource);
        }
        pw.println();
        pw.print("  mLastWakeLockHoldingWindow=");
        pw.print(this.mLastWakeLockHoldingWindow);
        pw.print(" mLastWakeLockObscuringWindow=");
        pw.print(this.mLastWakeLockObscuringWindow);
        pw.println();
        this.mInputManagerCallback.dump(pw, "  ");
        this.mTaskSnapshotController.dump(pw, "  ");
        if (dumpAll) {
            WindowState imeWindow = this.mRoot.getCurrentInputMethodWindow();
            if (imeWindow != null) {
                pw.print("  mInputMethodWindow=");
                pw.println(imeWindow);
            }
            this.mWindowPlacerLocked.dump(pw, "  ");
            pw.print("  mSystemBooted=");
            pw.print(this.mSystemBooted);
            pw.print(" mDisplayEnabled=");
            pw.println(this.mDisplayEnabled);
            this.mRoot.dumpLayoutNeededDisplayIds(pw);
            pw.print("  mTransactionSequence=");
            pw.println(this.mTransactionSequence);
            pw.print("  mDisplayFrozen=");
            pw.print(this.mDisplayFrozen);
            pw.print(" windows=");
            pw.print(this.mWindowsFreezingScreen);
            pw.print(" client=");
            pw.print(this.mClientFreezingScreen);
            pw.print(" apps=");
            pw.print(this.mAppsFreezingScreen);
            DisplayContent defaultDisplayContent = getDefaultDisplayContentLocked();
            pw.print("  mRotation=");
            pw.print(defaultDisplayContent.getRotation());
            pw.print("  mLastWindowForcedOrientation=");
            pw.print(defaultDisplayContent.getLastWindowForcedOrientation());
            pw.print(" mLastOrientation=");
            pw.println(defaultDisplayContent.getLastOrientation());
            pw.print(" waitingForConfig=");
            pw.println(defaultDisplayContent.mWaitingForConfig);
            pw.print("  Animation settings: disabled=");
            pw.print(this.mAnimationsDisabled);
            pw.print(" window=");
            pw.print(this.mWindowAnimationScaleSetting);
            pw.print(" transition=");
            pw.print(this.mTransitionAnimationScaleSetting);
            pw.print(" animator=");
            pw.println(this.mAnimatorDurationScaleSetting);
            if (this.mRecentsAnimationController != null) {
                pw.print("  mRecentsAnimationController=");
                pw.println(this.mRecentsAnimationController);
                this.mRecentsAnimationController.dump(pw, "    ");
            }
            PolicyControl.dump("  ", pw);
        }
    }

    static /* synthetic */ void lambda$dumpWindowsNoHeaderLocked$7(PrintWriter pw, DisplayContent dc) {
        WindowState inputMethodTarget = dc.mInputMethodTarget;
        if (inputMethodTarget != null) {
            pw.print("  mInputMethodTarget in display# ");
            pw.print(dc.getDisplayId());
            pw.print(' ');
            pw.println(inputMethodTarget);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean dumpWindows(PrintWriter pw, String name, String[] args, int opti, boolean dumpAll) {
        ArrayList<WindowState> windows = new ArrayList<>();
        if ("apps".equals(name) || "visible".equals(name) || "visible-apps".equals(name)) {
            boolean appsOnly = name.contains("apps");
            boolean visibleOnly = name.contains("visible");
            synchronized (this.mGlobalLock) {
                try {
                    boostPriorityForLockedSection();
                    if (appsOnly) {
                        this.mRoot.dumpDisplayContents(pw);
                    }
                    this.mRoot.forAllWindows((Consumer<WindowState>) new Consumer(visibleOnly, appsOnly, windows) {
                        /* class com.android.server.wm.$$Lambda$WindowManagerService$C4RecYWtrllidEGWyvVvRsY6lno */
                        private final /* synthetic */ boolean f$0;
                        private final /* synthetic */ boolean f$1;
                        private final /* synthetic */ ArrayList f$2;

                        {
                            this.f$0 = r1;
                            this.f$1 = r2;
                            this.f$2 = r3;
                        }

                        @Override // java.util.function.Consumer
                        public final void accept(Object obj) {
                            WindowManagerService.lambda$dumpWindows$8(this.f$0, this.f$1, this.f$2, (WindowState) obj);
                        }
                    }, true);
                } finally {
                    resetPriorityAfterLockedSection();
                }
            }
        } else {
            synchronized (this.mGlobalLock) {
                try {
                    boostPriorityForLockedSection();
                    this.mRoot.getWindowsByName(windows, name);
                } finally {
                    resetPriorityAfterLockedSection();
                }
            }
        }
        if (windows.size() <= 0) {
            return false;
        }
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                dumpWindowsLocked(pw, dumpAll, windows);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        return true;
    }

    static /* synthetic */ void lambda$dumpWindows$8(boolean visibleOnly, boolean appsOnly, ArrayList windows, WindowState w) {
        if (visibleOnly && !w.mWinAnimator.getShown()) {
            return;
        }
        if (!appsOnly || w.mAppToken != null) {
            windows.add(w);
        }
    }

    private void dumpLastANRLocked(PrintWriter pw) {
        pw.println("WINDOW MANAGER LAST ANR (dumpsys window lastanr)");
        String str = this.mLastANRState;
        if (str == null) {
            pw.println("  <no ANR has occurred since boot>");
        } else {
            pw.println(str);
        }
    }

    /* access modifiers changed from: package-private */
    public void saveANRStateLocked(AppWindowToken appWindowToken, WindowState windowState, String reason) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new FastPrintWriter(sw, false, 1024);
        pw.println("  ANR time: " + DateFormat.getDateTimeInstance().format(new Date()));
        if (appWindowToken != null) {
            pw.println("  Application at fault: " + appWindowToken.stringName);
        }
        if (windowState != null) {
            pw.println("  Window at fault: " + ((Object) windowState.mAttrs.getTitle()));
        }
        if (reason != null) {
            pw.println("  Reason: " + reason);
        }
        for (int i = this.mRoot.getChildCount() - 1; i >= 0; i--) {
            DisplayContent dc = (DisplayContent) this.mRoot.getChildAt(i);
            int displayId = dc.getDisplayId();
            if (!dc.mWinAddedSinceNullFocus.isEmpty()) {
                pw.println("  Windows added in display #" + displayId + " since null focus: " + dc.mWinAddedSinceNullFocus);
            }
            if (!dc.mWinRemovedSinceNullFocus.isEmpty()) {
                pw.println("  Windows removed in display #" + displayId + " since null focus: " + dc.mWinRemovedSinceNullFocus);
            }
        }
        pw.println();
        dumpWindowsNoHeaderLocked(pw, true, null);
        pw.println();
        pw.println("Last ANR continued");
        this.mRoot.dumpDisplayContents(pw);
        pw.close();
        this.mLastANRState = sw.toString();
        this.mH.removeMessages(38);
        this.mH.sendEmptyMessageDelayed(38, AppStandbyController.SettingsObserver.DEFAULT_SYSTEM_UPDATE_TIMEOUT);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        PriorityDump.dump(this.mPriorityDumper, fd, pw, args);
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void doDump(FileDescriptor fd, PrintWriter pw, String[] args, boolean useProto) {
        int opti;
        String opt;
        if (!SystemProperties.getBoolean(DEBUG_HIGH_REFRESH_BALCK_LIST, false)) {
            this.isHighRefreshBlackListOn = false;
        } else {
            this.isHighRefreshBlackListOn = true;
        }
        if (DumpUtils.checkDumpPermission(this.mContext, "WindowManager", pw)) {
            int opti2 = 0;
            boolean dumpAll = false;
            while (opti2 < args.length && (opt = args[opti2]) != null && opt.length() > 0 && opt.charAt(0) == '-') {
                opti2++;
                if ("-a".equals(opt)) {
                    dumpAll = true;
                } else if ("-h".equals(opt)) {
                    pw.println("Window manager dump options:");
                    pw.println("  [-a] [-h] [cmd] ...");
                    pw.println("  cmd may be one of:");
                    pw.println("    l[astanr]: last ANR information");
                    pw.println("    p[policy]: policy state");
                    pw.println("    a[animator]: animator state");
                    pw.println("    s[essions]: active sessions");
                    pw.println("    surfaces: active surfaces (debugging enabled only)");
                    pw.println("    d[isplays]: active display contents");
                    pw.println("    t[okens]: token list");
                    pw.println("    w[indows]: window list");
                    pw.println("    trace: print trace status and write Winscope trace to file");
                    pw.println("  cmd may also be a NAME to dump windows.  NAME may");
                    pw.println("    be a partial substring in a window name, a");
                    pw.println("    Window hex object identifier, or");
                    pw.println("    \"all\" for all windows, or");
                    pw.println("    \"visible\" for the visible windows.");
                    pw.println("    \"visible-apps\" for the visible app windows.");
                    pw.println("  -a: include all available server state.");
                    pw.println("  --proto: output dump in protocol buffer format.");
                    return;
                } else if ("-d".equals(opt)) {
                    this.mWindowManagerDebugger.runDebug(pw, args, opti2);
                    return;
                } else {
                    pw.println("Unknown argument: " + opt + "; use -h for help");
                }
            }
            if (useProto) {
                ProtoOutputStream proto = new ProtoOutputStream(fd);
                synchronized (this.mGlobalLock) {
                    try {
                        boostPriorityForLockedSection();
                        writeToProtoLocked(proto, 0);
                    } catch (Throwable th) {
                        resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
                resetPriorityAfterLockedSection();
                proto.flush();
                return;
            }
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
            pw.println("Dump time : " + df.format(new Date()));
            if (opti2 < args.length) {
                String cmd = args[opti2];
                int opti3 = opti2 + 1;
                if (!WindowManagerDynamicLogConfig.doDump(this, cmd, fd, pw, args, opti3)) {
                    if (!ActivityTaskManagerService.DUMP_LASTANR_CMD.equals(cmd)) {
                        if (!"l".equals(cmd)) {
                            if (!"policy".equals(cmd)) {
                                if (!"p".equals(cmd)) {
                                    if (!"animator".equals(cmd)) {
                                        if (!ActivityTaskManagerService.DUMP_ACTIVITIES_SHORT_CMD.equals(cmd)) {
                                            if (!"sessions".equals(cmd)) {
                                                if (!"s".equals(cmd)) {
                                                    if (!"displays".equals(cmd)) {
                                                        if (!"d".equals(cmd)) {
                                                            if (!"tokens".equals(cmd)) {
                                                                if (!"t".equals(cmd)) {
                                                                    if (!"windows".equals(cmd)) {
                                                                        if (!"w".equals(cmd)) {
                                                                            if ("all".equals(cmd)) {
                                                                                synchronized (this.mGlobalLock) {
                                                                                    try {
                                                                                        boostPriorityForLockedSection();
                                                                                        dumpWindowsLocked(pw, true, null);
                                                                                    } finally {
                                                                                        resetPriorityAfterLockedSection();
                                                                                    }
                                                                                }
                                                                                return;
                                                                            } else if (ActivityTaskManagerService.DUMP_CONTAINERS_CMD.equals(cmd)) {
                                                                                synchronized (this.mGlobalLock) {
                                                                                    try {
                                                                                        boostPriorityForLockedSection();
                                                                                        this.mRoot.dumpChildrenNames(pw, StringUtils.SPACE);
                                                                                        pw.println(StringUtils.SPACE);
                                                                                        this.mRoot.forAllWindows((Consumer<WindowState>) new Consumer(pw) {
                                                                                            /* class com.android.server.wm.$$Lambda$WindowManagerService$LbJzcX6LZWc_oRhCOhY74zWzL7Y */
                                                                                            private final /* synthetic */ PrintWriter f$0;

                                                                                            {
                                                                                                this.f$0 = r1;
                                                                                            }

                                                                                            @Override // java.util.function.Consumer
                                                                                            public final void accept(Object obj) {
                                                                                                this.f$0.println((PrintWriter) ((WindowState) obj));
                                                                                            }
                                                                                        }, true);
                                                                                    } finally {
                                                                                        resetPriorityAfterLockedSection();
                                                                                    }
                                                                                }
                                                                                return;
                                                                            } else {
                                                                                if ("log".equals(cmd)) {
                                                                                    opti = opti3;
                                                                                } else if ("oppo-log".equals(cmd)) {
                                                                                    opti = opti3;
                                                                                } else if ("debug_switch".equals(cmd) || "get_value".equals(cmd)) {
                                                                                    OppoWMSDynamicLogConfig.dumpDynamicallyLogSwitch(pw, args, opti3);
                                                                                    return;
                                                                                } else if ("trace".equals(cmd)) {
                                                                                    dumpTraceStatus(pw);
                                                                                    return;
                                                                                } else if (!dumpWindows(pw, cmd, args, opti3, dumpAll)) {
                                                                                    pw.println("Bad window command, or no windows match: " + cmd);
                                                                                    pw.println("Use -h for help.");
                                                                                    return;
                                                                                } else {
                                                                                    return;
                                                                                }
                                                                                OppoWMSDynamicLogConfig.dynamicallyConfigLogTag(pw, args, opti);
                                                                                return;
                                                                            }
                                                                        }
                                                                    }
                                                                    synchronized (this.mGlobalLock) {
                                                                        try {
                                                                            boostPriorityForLockedSection();
                                                                            dumpWindowsLocked(pw, true, null);
                                                                        } finally {
                                                                            resetPriorityAfterLockedSection();
                                                                        }
                                                                    }
                                                                    return;
                                                                }
                                                            }
                                                            synchronized (this.mGlobalLock) {
                                                                try {
                                                                    boostPriorityForLockedSection();
                                                                    dumpTokensLocked(pw, true);
                                                                } finally {
                                                                    resetPriorityAfterLockedSection();
                                                                }
                                                            }
                                                            return;
                                                        }
                                                    }
                                                    synchronized (this.mGlobalLock) {
                                                        try {
                                                            boostPriorityForLockedSection();
                                                            this.mRoot.dumpDisplayContents(pw);
                                                        } finally {
                                                            resetPriorityAfterLockedSection();
                                                        }
                                                    }
                                                    return;
                                                }
                                            }
                                            synchronized (this.mGlobalLock) {
                                                try {
                                                    boostPriorityForLockedSection();
                                                    dumpSessionsLocked(pw, true);
                                                } finally {
                                                    resetPriorityAfterLockedSection();
                                                }
                                            }
                                            return;
                                        }
                                    }
                                    synchronized (this.mGlobalLock) {
                                        try {
                                            boostPriorityForLockedSection();
                                            dumpAnimatorLocked(pw, args, true);
                                        } finally {
                                            resetPriorityAfterLockedSection();
                                        }
                                    }
                                    return;
                                }
                            }
                            synchronized (this.mGlobalLock) {
                                try {
                                    boostPriorityForLockedSection();
                                    dumpPolicyLocked(pw, args, true);
                                } finally {
                                    resetPriorityAfterLockedSection();
                                }
                            }
                            return;
                        }
                    }
                    synchronized (this.mGlobalLock) {
                        try {
                            boostPriorityForLockedSection();
                            dumpLastANRLocked(pw);
                        } finally {
                            resetPriorityAfterLockedSection();
                        }
                    }
                    return;
                }
                return;
            }
            synchronized (this.mGlobalLock) {
                try {
                    boostPriorityForLockedSection();
                    pw.println();
                    if (dumpAll) {
                        pw.println("-------------------------------------------------------------------------------");
                    }
                    dumpLastANRLocked(pw);
                    pw.println();
                    if (dumpAll) {
                        pw.println("-------------------------------------------------------------------------------");
                    }
                    dumpPolicyLocked(pw, args, dumpAll);
                    pw.println();
                    if (dumpAll) {
                        pw.println("-------------------------------------------------------------------------------");
                    }
                    dumpAnimatorLocked(pw, args, dumpAll);
                    pw.println();
                    if (dumpAll) {
                        pw.println("-------------------------------------------------------------------------------");
                    }
                    dumpSessionsLocked(pw, dumpAll);
                    pw.println();
                    if (dumpAll) {
                        pw.println("-------------------------------------------------------------------------------");
                    }
                    if (dumpAll) {
                        pw.println("-------------------------------------------------------------------------------");
                    }
                    this.mRoot.dumpDisplayContents(pw);
                    pw.println();
                    if (dumpAll) {
                        pw.println("-------------------------------------------------------------------------------");
                    }
                    dumpTokensLocked(pw, dumpAll);
                    pw.println();
                    if (dumpAll) {
                        pw.println("-------------------------------------------------------------------------------");
                    }
                    dumpWindowsLocked(pw, dumpAll, null);
                    if (dumpAll) {
                        pw.println("-------------------------------------------------------------------------------");
                    }
                    dumpTraceStatus(pw);
                } finally {
                    resetPriorityAfterLockedSection();
                }
            }
        }
    }

    @Override // com.android.server.Watchdog.Monitor
    public void monitor() {
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public DisplayContent getDefaultDisplayContentLocked() {
        return this.mRoot.getDisplayContent(0);
    }

    public void onOverlayChanged() {
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                this.mRoot.forAllDisplays($$Lambda$WindowManagerService$oXZopye9ykF6MR6QjHAIi3bGRc.INSTANCE);
                requestTraversal();
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    static /* synthetic */ void lambda$onOverlayChanged$10(DisplayContent displayContent) {
        displayContent.getDisplayPolicy().onOverlayChangedLw();
        displayContent.updateDisplayInfo();
    }

    public void onDisplayChanged(int displayId) {
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                DisplayContent displayContent = this.mRoot.getDisplayContent(displayId);
                if (displayContent != null) {
                    displayContent.updateDisplayInfo();
                }
                this.mWindowPlacerLocked.requestTraversal();
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    @Override // com.android.server.policy.WindowManagerPolicy.WindowManagerFuncs
    public Object getWindowManagerLock() {
        return this.mGlobalLock;
    }

    public void setWillReplaceWindow(IBinder token, boolean animate) {
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                AppWindowToken appWindowToken = this.mRoot.getAppWindowToken(token);
                if (appWindowToken == null) {
                    Slog.w("WindowManager", "Attempted to set replacing window on non-existing app token " + token);
                } else if (!appWindowToken.hasContentToDisplay()) {
                    Slog.w("WindowManager", "Attempted to set replacing window on app token with no content" + token);
                    resetPriorityAfterLockedSection();
                } else {
                    appWindowToken.setWillReplaceWindows(animate);
                    resetPriorityAfterLockedSection();
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setWillReplaceWindows(IBinder token, boolean childrenOnly) {
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                AppWindowToken appWindowToken = this.mRoot.getAppWindowToken(token);
                if (appWindowToken == null) {
                    Slog.w("WindowManager", "Attempted to set replacing window on non-existing app token " + token);
                } else if (!appWindowToken.hasContentToDisplay()) {
                    Slog.w("WindowManager", "Attempted to set replacing window on app token with no content" + token);
                    resetPriorityAfterLockedSection();
                } else {
                    if (childrenOnly) {
                        appWindowToken.setWillReplaceChildWindows();
                    } else {
                        appWindowToken.setWillReplaceWindows(false);
                    }
                    scheduleClearWillReplaceWindows(token, true);
                    resetPriorityAfterLockedSection();
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void scheduleClearWillReplaceWindows(IBinder token, boolean replacing) {
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                AppWindowToken appWindowToken = this.mRoot.getAppWindowToken(token);
                if (appWindowToken == null) {
                    Slog.w("WindowManager", "Attempted to reset replacing window on non-existing app token " + token);
                    return;
                }
                if (replacing) {
                    scheduleWindowReplacementTimeouts(appWindowToken);
                } else {
                    appWindowToken.clearWillReplaceWindows();
                }
                resetPriorityAfterLockedSection();
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void scheduleWindowReplacementTimeouts(AppWindowToken appWindowToken) {
        if (!this.mWindowReplacementTimeouts.contains(appWindowToken)) {
            this.mWindowReplacementTimeouts.add(appWindowToken);
        }
        this.mH.removeMessages(46);
        this.mH.sendEmptyMessageDelayed(46, 2000);
    }

    public int getDockedStackSide() {
        int dockSide;
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                TaskStack dockedStack = getDefaultDisplayContentLocked().getSplitScreenPrimaryStackIgnoringVisibility();
                dockSide = dockedStack == null ? -1 : dockedStack.getDockSide();
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        return dockSide;
    }

    public void setDockedStackResizing(boolean resizing) {
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                getDefaultDisplayContentLocked().getDockedDividerController().setResizing(resizing);
                requestTraversal();
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void setDockedStackDividerTouchRegion(Rect touchRegion) {
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                DisplayContent dc = getDefaultDisplayContentLocked();
                dc.getDockedDividerController().setTouchRegion(touchRegion);
                dc.updateTouchExcludeRegion();
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void setResizeDimLayer(boolean visible, int targetWindowingMode, float alpha) {
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                getDefaultDisplayContentLocked().getDockedDividerController().setResizeDimLayer(visible, targetWindowingMode, alpha);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void setForceResizableTasks(boolean forceResizableTasks) {
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                this.mForceResizableTasks = forceResizableTasks;
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void setSupportsPictureInPicture(boolean supportsPictureInPicture) {
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                this.mSupportsPictureInPicture = supportsPictureInPicture;
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void setSupportsFreeformWindowManagement(boolean supportsFreeformWindowManagement) {
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                this.mSupportsFreeformWindowManagement = supportsFreeformWindowManagement;
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setForceDesktopModeOnExternalDisplays(boolean forceDesktopModeOnExternalDisplays) {
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                this.mForceDesktopModeOnExternalDisplays = forceDesktopModeOnExternalDisplays;
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void setIsPc(boolean isPc) {
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                this.mIsPc = isPc;
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    static int dipToPixel(int dip, DisplayMetrics displayMetrics) {
        return (int) TypedValue.applyDimension(1, (float) dip, displayMetrics);
    }

    public void registerDockedStackListener(IDockedStackListener listener) {
        this.mAtmInternal.enforceCallerIsRecentsOrHasPermission("android.permission.REGISTER_WINDOW_MANAGER_LISTENERS", "registerDockedStackListener()");
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                getDefaultDisplayContentLocked().mDividerControllerLocked.registerDockedStackListener(listener);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void registerPinnedStackListener(int displayId, IPinnedStackListener listener) {
        if (checkCallingPermission("android.permission.REGISTER_WINDOW_MANAGER_LISTENERS", "registerPinnedStackListener()") && this.mSupportsPictureInPicture) {
            synchronized (this.mGlobalLock) {
                try {
                    boostPriorityForLockedSection();
                    this.mRoot.getDisplayContent(displayId).getPinnedStackController().registerPinnedStackListener(listener);
                } finally {
                    resetPriorityAfterLockedSection();
                }
            }
        }
    }

    public void requestAppKeyboardShortcuts(IResultReceiver receiver, int deviceId) {
        try {
            WindowState focusedWindow = getFocusedWindow();
            if (focusedWindow != null && focusedWindow.mClient != null) {
                getFocusedWindow().mClient.requestAppKeyboardShortcuts(receiver, deviceId);
            }
        } catch (RemoteException e) {
        }
    }

    public void getStableInsets(int displayId, Rect outInsets) throws RemoteException {
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                getStableInsetsLocked(displayId, outInsets);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void getStableInsetsLocked(int displayId, Rect outInsets) {
        outInsets.setEmpty();
        DisplayContent dc = this.mRoot.getDisplayContent(displayId);
        if (dc != null) {
            DisplayInfo di = dc.getDisplayInfo();
            dc.getDisplayPolicy().getStableInsetsLw(di.rotation, di.logicalWidth, di.logicalHeight, di.displayCutout, outInsets);
        }
    }

    public void setForwardedInsets(int displayId, Insets insets) throws RemoteException {
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                DisplayContent dc = this.mRoot.getDisplayContent(displayId);
                if (dc != null) {
                    if (Binder.getCallingUid() == dc.getDisplay().getOwnerUid()) {
                        dc.setForwardedInsets(insets);
                        resetPriorityAfterLockedSection();
                        return;
                    }
                    throw new SecurityException("Only owner of the display can set ForwardedInsets to it.");
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void intersectDisplayInsetBounds(Rect display, Rect insets, Rect inOutBounds) {
        this.mTmpRect3.set(display);
        this.mTmpRect3.inset(insets);
        inOutBounds.intersect(this.mTmpRect3);
    }

    /* access modifiers changed from: private */
    public static class MousePositionTracker implements WindowManagerPolicyConstants.PointerEventListener {
        private boolean mLatestEventWasMouse;
        private float mLatestMouseX;
        private float mLatestMouseY;

        private MousePositionTracker() {
        }

        /* access modifiers changed from: package-private */
        public void updatePosition(float x, float y) {
            synchronized (this) {
                this.mLatestEventWasMouse = true;
                this.mLatestMouseX = x;
                this.mLatestMouseY = y;
            }
        }

        public void onPointerEvent(MotionEvent motionEvent) {
            if (motionEvent.isFromSource(UsbACInterface.FORMAT_III_IEC1937_MPEG1_Layer1)) {
                updatePosition(motionEvent.getRawX(), motionEvent.getRawY());
                return;
            }
            synchronized (this) {
                this.mLatestEventWasMouse = false;
            }
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:10:0x001c, code lost:
        monitor-enter(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:?, code lost:
        boostPriorityForLockedSection();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0026, code lost:
        if (r9.mDragDropController.dragDropActiveLocked() == false) goto L_0x002d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0028, code lost:
        monitor-exit(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x002c, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x002d, code lost:
        r0 = windowForClientLocked((com.android.server.wm.Session) null, r10, false);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0033, code lost:
        if (r0 != null) goto L_0x0050;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0035, code lost:
        android.util.Slog.w("WindowManager", "Bad requesting window " + r10);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x004b, code lost:
        monitor-exit(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x004c, code lost:
        resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x004f, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0050, code lost:
        r4 = r0.getDisplayContent();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0054, code lost:
        if (r4 != null) goto L_0x005b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0056, code lost:
        monitor-exit(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0057, code lost:
        resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x005a, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x005b, code lost:
        r5 = r4.getTouchableWinAtPointLocked(r1, r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0060, code lost:
        if (r5 == r0) goto L_0x0067;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0062, code lost:
        monitor-exit(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0063, code lost:
        resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0066, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:?, code lost:
        r5.mClient.updatePointerIcon(r5.translateToWindowX(r1), r5.translateToWindowY(r2));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0076, code lost:
        android.util.Slog.w("WindowManager", "unable to update pointer icon");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x0082, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x0084, code lost:
        resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x0087, code lost:
        throw r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x001a, code lost:
        r3 = r9.mGlobalLock;
     */
    public void updatePointerIcon(IWindow client) {
        WindowManagerGlobalLock windowManagerGlobalLock;
        synchronized (this.mMousePositionTracker) {
            if (this.mMousePositionTracker.mLatestEventWasMouse) {
                float mouseX = this.mMousePositionTracker.mLatestMouseX;
                float mouseY = this.mMousePositionTracker.mLatestMouseY;
            } else {
                return;
            }
        }
        resetPriorityAfterLockedSection();
    }

    /* access modifiers changed from: package-private */
    public void restorePointerIconLocked(DisplayContent displayContent, float latestX, float latestY) {
        this.mMousePositionTracker.updatePosition(latestX, latestY);
        WindowState windowUnderPointer = displayContent.getTouchableWinAtPointLocked(latestX, latestY);
        if (windowUnderPointer != null) {
            try {
                windowUnderPointer.mClient.updatePointerIcon(windowUnderPointer.translateToWindowX(latestX), windowUnderPointer.translateToWindowY(latestY));
            } catch (RemoteException e) {
                Slog.w("WindowManager", "unable to restore pointer icon");
            }
        } else {
            InputManager.getInstance().setPointerIconType(1000);
        }
    }

    private void checkCallerOwnsDisplay(int displayId) {
        Display display = this.mDisplayManager.getDisplay(displayId);
        if (display == null) {
            throw new IllegalArgumentException("Cannot find display for non-existent displayId: " + displayId);
        } else if (Binder.getCallingUid() != display.getOwnerUid()) {
            throw new SecurityException("The caller doesn't own the display.");
        }
    }

    /* access modifiers changed from: package-private */
    public void reparentDisplayContent(IWindow client, SurfaceControl sc, int displayId) {
        checkCallerOwnsDisplay(displayId);
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                long token = Binder.clearCallingIdentity();
                try {
                    WindowState win = windowForClientLocked((Session) null, client, false);
                    if (win == null) {
                        Slog.w("WindowManager", "Bad requesting window " + client);
                        return;
                    }
                    getDisplayContentOrCreate(displayId, null).reparentDisplayContent(win, sc);
                    Binder.restoreCallingIdentity(token);
                    resetPriorityAfterLockedSection();
                } finally {
                    Binder.restoreCallingIdentity(token);
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void updateDisplayContentLocation(IWindow client, int x, int y, int displayId) {
        checkCallerOwnsDisplay(displayId);
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                long token = Binder.clearCallingIdentity();
                try {
                    WindowState win = windowForClientLocked((Session) null, client, false);
                    if (win == null) {
                        Slog.w("WindowManager", "Bad requesting window " + client);
                        return;
                    }
                    DisplayContent displayContent = this.mRoot.getDisplayContent(displayId);
                    if (displayContent != null) {
                        displayContent.updateLocation(win, x, y);
                    }
                    Binder.restoreCallingIdentity(token);
                    resetPriorityAfterLockedSection();
                } finally {
                    Binder.restoreCallingIdentity(token);
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void updateTapExcludeRegion(IWindow client, int regionId, Region region) {
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                WindowState callingWin = windowForClientLocked((Session) null, client, false);
                if (callingWin == null) {
                    Slog.w("WindowManager", "Bad requesting window " + client);
                    return;
                }
                callingWin.updateTapExcludeRegion(regionId, region);
                resetPriorityAfterLockedSection();
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    /* JADX INFO: finally extract failed */
    public void dontOverrideDisplayInfo(int displayId) {
        long token = Binder.clearCallingIdentity();
        try {
            synchronized (this.mGlobalLock) {
                try {
                    boostPriorityForLockedSection();
                    DisplayContent dc = getDisplayContentOrCreate(displayId, null);
                    if (dc != null) {
                        dc.mShouldOverrideDisplayConfiguration = false;
                        this.mDisplayManagerInternal.setDisplayInfoOverrideFromWindowManager(displayId, (DisplayInfo) null);
                    } else {
                        throw new IllegalArgumentException("Trying to configure a non existent display.");
                    }
                } catch (Throwable th) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
            resetPriorityAfterLockedSection();
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public int getWindowingMode(int displayId) {
        if (checkCallingPermission("android.permission.INTERNAL_SYSTEM_WINDOW", "getWindowingMode()")) {
            synchronized (this.mGlobalLock) {
                try {
                    boostPriorityForLockedSection();
                    DisplayContent displayContent = this.mRoot.getDisplayContent(displayId);
                    if (displayContent == null) {
                        Slog.w("WindowManager", "Attempted to get windowing mode of a display that does not exist: " + displayId);
                        return 0;
                    }
                    int windowingModeLocked = this.mDisplayWindowSettings.getWindowingModeLocked(displayContent);
                    resetPriorityAfterLockedSection();
                    return windowingModeLocked;
                } finally {
                    resetPriorityAfterLockedSection();
                }
            }
        } else {
            throw new SecurityException("Requires INTERNAL_SYSTEM_WINDOW permission");
        }
    }

    /* JADX INFO: finally extract failed */
    public void setWindowingMode(int displayId, int mode) {
        if (checkCallingPermission("android.permission.INTERNAL_SYSTEM_WINDOW", "setWindowingMode()")) {
            synchronized (this.mGlobalLock) {
                try {
                    boostPriorityForLockedSection();
                    DisplayContent displayContent = getDisplayContentOrCreate(displayId, null);
                    if (displayContent == null) {
                        Slog.w("WindowManager", "Attempted to set windowing mode to a display that does not exist: " + displayId);
                        return;
                    }
                    int lastWindowingMode = displayContent.getWindowingMode();
                    this.mDisplayWindowSettings.setWindowingModeLocked(displayContent, mode);
                    reconfigureDisplayLocked(displayContent);
                    if (lastWindowingMode != displayContent.getWindowingMode()) {
                        this.mH.removeMessages(18);
                        long origId = Binder.clearCallingIdentity();
                        try {
                            sendNewConfiguration(displayId);
                            Binder.restoreCallingIdentity(origId);
                            displayContent.executeAppTransition();
                        } catch (Throwable th) {
                            Binder.restoreCallingIdentity(origId);
                            throw th;
                        }
                    }
                    resetPriorityAfterLockedSection();
                } finally {
                    resetPriorityAfterLockedSection();
                }
            }
        } else {
            throw new SecurityException("Requires INTERNAL_SYSTEM_WINDOW permission");
        }
    }

    @WindowManager.RemoveContentMode
    public int getRemoveContentMode(int displayId) {
        if (checkCallingPermission("android.permission.INTERNAL_SYSTEM_WINDOW", "getRemoveContentMode()")) {
            synchronized (this.mGlobalLock) {
                try {
                    boostPriorityForLockedSection();
                    DisplayContent displayContent = this.mRoot.getDisplayContent(displayId);
                    if (displayContent == null) {
                        Slog.w("WindowManager", "Attempted to get remove mode of a display that does not exist: " + displayId);
                        return 0;
                    }
                    int removeContentModeLocked = this.mDisplayWindowSettings.getRemoveContentModeLocked(displayContent);
                    resetPriorityAfterLockedSection();
                    return removeContentModeLocked;
                } finally {
                    resetPriorityAfterLockedSection();
                }
            }
        } else {
            throw new SecurityException("Requires INTERNAL_SYSTEM_WINDOW permission");
        }
    }

    public void setRemoveContentMode(int displayId, @WindowManager.RemoveContentMode int mode) {
        if (checkCallingPermission("android.permission.INTERNAL_SYSTEM_WINDOW", "setRemoveContentMode()")) {
            synchronized (this.mGlobalLock) {
                try {
                    boostPriorityForLockedSection();
                    DisplayContent displayContent = getDisplayContentOrCreate(displayId, null);
                    if (displayContent == null) {
                        Slog.w("WindowManager", "Attempted to set remove mode to a display that does not exist: " + displayId);
                        return;
                    }
                    this.mDisplayWindowSettings.setRemoveContentModeLocked(displayContent, mode);
                    reconfigureDisplayLocked(displayContent);
                    resetPriorityAfterLockedSection();
                } finally {
                    resetPriorityAfterLockedSection();
                }
            }
        } else {
            throw new SecurityException("Requires INTERNAL_SYSTEM_WINDOW permission");
        }
    }

    public boolean shouldShowWithInsecureKeyguard(int displayId) {
        if (checkCallingPermission("android.permission.INTERNAL_SYSTEM_WINDOW", "shouldShowWithInsecureKeyguard()")) {
            synchronized (this.mGlobalLock) {
                try {
                    boostPriorityForLockedSection();
                    DisplayContent displayContent = this.mRoot.getDisplayContent(displayId);
                    if (displayContent == null) {
                        Slog.w("WindowManager", "Attempted to get flag of a display that does not exist: " + displayId);
                        return false;
                    }
                    boolean shouldShowWithInsecureKeyguardLocked = this.mDisplayWindowSettings.shouldShowWithInsecureKeyguardLocked(displayContent);
                    resetPriorityAfterLockedSection();
                    return shouldShowWithInsecureKeyguardLocked;
                } finally {
                    resetPriorityAfterLockedSection();
                }
            }
        } else {
            throw new SecurityException("Requires INTERNAL_SYSTEM_WINDOW permission");
        }
    }

    public void setShouldShowWithInsecureKeyguard(int displayId, boolean shouldShow) {
        if (checkCallingPermission("android.permission.INTERNAL_SYSTEM_WINDOW", "setShouldShowWithInsecureKeyguard()")) {
            synchronized (this.mGlobalLock) {
                try {
                    boostPriorityForLockedSection();
                    DisplayContent displayContent = getDisplayContentOrCreate(displayId, null);
                    if (displayContent == null) {
                        Slog.w("WindowManager", "Attempted to set flag to a display that does not exist: " + displayId);
                        return;
                    }
                    this.mDisplayWindowSettings.setShouldShowWithInsecureKeyguardLocked(displayContent, shouldShow);
                    reconfigureDisplayLocked(displayContent);
                    resetPriorityAfterLockedSection();
                } finally {
                    resetPriorityAfterLockedSection();
                }
            }
        } else {
            throw new SecurityException("Requires INTERNAL_SYSTEM_WINDOW permission");
        }
    }

    public boolean shouldShowSystemDecors(int displayId) {
        if (checkCallingPermission("android.permission.INTERNAL_SYSTEM_WINDOW", "shouldShowSystemDecors()")) {
            synchronized (this.mGlobalLock) {
                try {
                    boostPriorityForLockedSection();
                    DisplayContent displayContent = this.mRoot.getDisplayContent(displayId);
                    if (displayContent == null) {
                        Slog.w("WindowManager", "Attempted to get system decors flag of a display that does not exist: " + displayId);
                        return false;
                    } else if (displayContent.isUntrustedVirtualDisplay()) {
                        resetPriorityAfterLockedSection();
                        return false;
                    } else {
                        boolean supportsSystemDecorations = displayContent.supportsSystemDecorations();
                        resetPriorityAfterLockedSection();
                        return supportsSystemDecorations;
                    }
                } finally {
                    resetPriorityAfterLockedSection();
                }
            }
        } else {
            throw new SecurityException("Requires INTERNAL_SYSTEM_WINDOW permission");
        }
    }

    public void setShouldShowSystemDecors(int displayId, boolean shouldShow) {
        if (checkCallingPermission("android.permission.INTERNAL_SYSTEM_WINDOW", "setShouldShowSystemDecors()")) {
            synchronized (this.mGlobalLock) {
                try {
                    boostPriorityForLockedSection();
                    DisplayContent displayContent = getDisplayContentOrCreate(displayId, null);
                    if (displayContent == null) {
                        Slog.w("WindowManager", "Attempted to set system decors flag to a display that does not exist: " + displayId);
                    } else if (!displayContent.isUntrustedVirtualDisplay()) {
                        this.mDisplayWindowSettings.setShouldShowSystemDecorsLocked(displayContent, shouldShow);
                        reconfigureDisplayLocked(displayContent);
                        resetPriorityAfterLockedSection();
                    } else {
                        throw new SecurityException("Attempted to set system decors flag to an untrusted virtual display: " + displayId);
                    }
                } finally {
                    resetPriorityAfterLockedSection();
                }
            }
        } else {
            throw new SecurityException("Requires INTERNAL_SYSTEM_WINDOW permission");
        }
    }

    public boolean shouldShowIme(int displayId) {
        if (checkCallingPermission("android.permission.INTERNAL_SYSTEM_WINDOW", "shouldShowIme()")) {
            synchronized (this.mGlobalLock) {
                try {
                    boostPriorityForLockedSection();
                    DisplayContent displayContent = this.mRoot.getDisplayContent(displayId);
                    boolean z = false;
                    if (displayContent == null) {
                        Slog.w("WindowManager", "Attempted to get IME flag of a display that does not exist: " + displayId);
                        return false;
                    } else if (displayContent.isUntrustedVirtualDisplay()) {
                        resetPriorityAfterLockedSection();
                        return false;
                    } else {
                        if (this.mDisplayWindowSettings.shouldShowImeLocked(displayContent) || this.mForceDesktopModeOnExternalDisplays) {
                            z = true;
                        }
                        resetPriorityAfterLockedSection();
                        return z;
                    }
                } finally {
                    resetPriorityAfterLockedSection();
                }
            }
        } else {
            throw new SecurityException("Requires INTERNAL_SYSTEM_WINDOW permission");
        }
    }

    public void setShouldShowIme(int displayId, boolean shouldShow) {
        if (checkCallingPermission("android.permission.INTERNAL_SYSTEM_WINDOW", "setShouldShowIme()")) {
            synchronized (this.mGlobalLock) {
                try {
                    boostPriorityForLockedSection();
                    DisplayContent displayContent = getDisplayContentOrCreate(displayId, null);
                    if (displayContent == null) {
                        Slog.w("WindowManager", "Attempted to set IME flag to a display that does not exist: " + displayId);
                    } else if (!displayContent.isUntrustedVirtualDisplay()) {
                        this.mDisplayWindowSettings.setShouldShowImeLocked(displayContent, shouldShow);
                        reconfigureDisplayLocked(displayContent);
                        resetPriorityAfterLockedSection();
                    } else {
                        throw new SecurityException("Attempted to set IME flag to an untrusted virtual display: " + displayId);
                    }
                } finally {
                    resetPriorityAfterLockedSection();
                }
            }
        } else {
            throw new SecurityException("Requires INTERNAL_SYSTEM_WINDOW permission");
        }
    }

    public void registerShortcutKey(long shortcutCode, IShortcutService shortcutKeyReceiver) throws RemoteException {
        if (checkCallingPermission("android.permission.REGISTER_WINDOW_MANAGER_LISTENERS", "registerShortcutKey")) {
            this.mPolicy.registerShortcutKey(shortcutCode, shortcutKeyReceiver);
            return;
        }
        throw new SecurityException("Requires REGISTER_WINDOW_MANAGER_LISTENERS permission");
    }

    public void requestUserActivityNotification() {
        if (checkCallingPermission("android.permission.USER_ACTIVITY", "requestUserActivityNotification()")) {
            this.mPolicy.requestUserActivityNotification();
            return;
        }
        throw new SecurityException("Requires USER_ACTIVITY permission");
    }

    /* access modifiers changed from: package-private */
    public void markForSeamlessRotation(WindowState w, boolean seamlesslyRotated) {
        if (seamlesslyRotated != w.mSeamlesslyRotated && !w.mForceSeamlesslyRotate) {
            w.mSeamlesslyRotated = seamlesslyRotated;
            if (seamlesslyRotated) {
                this.mSeamlessRotationCount++;
            } else {
                this.mSeamlessRotationCount--;
            }
            if (this.mSeamlessRotationCount == 0) {
                if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                    Slog.i("WindowManager", "Performing post-rotate rotation after seamless rotation");
                }
                finishSeamlessRotation();
                w.getDisplayContent().updateRotationAndSendNewConfigIfNeeded();
            }
        }
    }

    /* access modifiers changed from: private */
    public final class LocalService extends WindowManagerInternal {
        private LocalService() {
        }

        @Override // com.android.server.wm.WindowManagerInternal
        public void requestTraversalFromDisplayManager() {
            WindowManagerService.this.requestTraversal();
        }

        /* JADX INFO: finally extract failed */
        @Override // com.android.server.wm.WindowManagerInternal
        public void setMagnificationSpec(int displayId, MagnificationSpec spec) {
            synchronized (WindowManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    if (WindowManagerService.this.mAccessibilityController != null) {
                        WindowManagerService.this.mAccessibilityController.setMagnificationSpecLocked(displayId, spec);
                    } else {
                        throw new IllegalStateException("Magnification callbacks not set!");
                    }
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
            WindowManagerService.resetPriorityAfterLockedSection();
            if (Binder.getCallingPid() != Process.myPid()) {
                spec.recycle();
            }
        }

        @Override // com.android.server.wm.WindowManagerInternal
        public void setForceShowMagnifiableBounds(int displayId, boolean show) {
            synchronized (WindowManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    if (WindowManagerService.this.mAccessibilityController != null) {
                        WindowManagerService.this.mAccessibilityController.setForceShowMagnifiableBoundsLocked(displayId, show);
                    } else {
                        throw new IllegalStateException("Magnification callbacks not set!");
                    }
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.WindowManagerInternal
        public void getMagnificationRegion(int displayId, Region magnificationRegion) {
            synchronized (WindowManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    if (WindowManagerService.this.mAccessibilityController != null) {
                        WindowManagerService.this.mAccessibilityController.getMagnificationRegionLocked(displayId, magnificationRegion);
                    } else {
                        throw new IllegalStateException("Magnification callbacks not set!");
                    }
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.WindowManagerInternal
        public MagnificationSpec getCompatibleMagnificationSpecForWindow(IBinder windowToken) {
            synchronized (WindowManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    WindowState windowState = (WindowState) WindowManagerService.this.mWindowMap.get(windowToken);
                    if (windowState == null) {
                        return null;
                    }
                    MagnificationSpec spec = null;
                    if (WindowManagerService.this.mAccessibilityController != null) {
                        spec = WindowManagerService.this.mAccessibilityController.getMagnificationSpecForWindowLocked(windowState);
                    }
                    if ((spec == null || spec.isNop()) && windowState.mGlobalScale == 1.0f) {
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return null;
                    }
                    MagnificationSpec spec2 = spec == null ? MagnificationSpec.obtain() : MagnificationSpec.obtain(spec);
                    spec2.scale *= windowState.mGlobalScale;
                    WindowManagerService.resetPriorityAfterLockedSection();
                    return spec2;
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.WindowManagerInternal
        public boolean setMagnificationCallbacks(int displayId, WindowManagerInternal.MagnificationCallbacks callbacks) {
            boolean result;
            synchronized (WindowManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    if (WindowManagerService.this.mAccessibilityController == null) {
                        WindowManagerService.this.mAccessibilityController = new AccessibilityController(WindowManagerService.this);
                    }
                    result = WindowManagerService.this.mAccessibilityController.setMagnificationCallbacksLocked(displayId, callbacks);
                    if (!WindowManagerService.this.mAccessibilityController.hasCallbacksLocked()) {
                        WindowManagerService.this.mAccessibilityController = null;
                    }
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
            return result;
        }

        @Override // com.android.server.wm.WindowManagerInternal
        public void setWindowsForAccessibilityCallback(WindowManagerInternal.WindowsForAccessibilityCallback callback) {
            synchronized (WindowManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    if (WindowManagerService.this.mAccessibilityController == null) {
                        WindowManagerService.this.mAccessibilityController = new AccessibilityController(WindowManagerService.this);
                    }
                    WindowManagerService.this.mAccessibilityController.setWindowsForAccessibilityCallback(callback);
                    if (!WindowManagerService.this.mAccessibilityController.hasCallbacksLocked()) {
                        WindowManagerService.this.mAccessibilityController = null;
                    }
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.WindowManagerInternal
        public void setInputFilter(IInputFilter filter) {
            WindowManagerService.this.mInputManager.setInputFilter(filter);
        }

        @Override // com.android.server.wm.WindowManagerInternal
        public IBinder getFocusedWindowToken() {
            synchronized (WindowManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    WindowState windowState = WindowManagerService.this.getFocusedWindowLocked();
                    if (windowState != null) {
                        return windowState.mClient.asBinder();
                    }
                    WindowManagerService.resetPriorityAfterLockedSection();
                    return null;
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.WindowManagerInternal
        public boolean isKeyguardLocked() {
            return WindowManagerService.this.isKeyguardLocked();
        }

        @Override // com.android.server.wm.WindowManagerInternal
        public boolean isKeyguardShowingAndNotOccluded() {
            return WindowManagerService.this.isKeyguardShowingAndNotOccluded();
        }

        @Override // com.android.server.wm.WindowManagerInternal
        public void showGlobalActions() {
            WindowManagerService.this.showGlobalActions();
        }

        @Override // com.android.server.wm.WindowManagerInternal
        public void getWindowFrame(IBinder token, Rect outBounds) {
            synchronized (WindowManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    WindowState windowState = (WindowState) WindowManagerService.this.mWindowMap.get(token);
                    if (windowState != null) {
                        outBounds.set(windowState.getFrameLw());
                    } else {
                        outBounds.setEmpty();
                    }
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.WindowManagerInternal
        public void waitForAllWindowsDrawn(Runnable callback, long timeout) {
            boolean allWindowsDrawn = false;
            synchronized (WindowManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    WindowManagerService.this.mWaitingForDrawnCallback = callback;
                    WindowManagerService.this.getDefaultDisplayContentLocked().waitForAllWindowsDrawn();
                    WindowManagerService.this.mWindowPlacerLocked.requestTraversal();
                    WindowManagerService.this.mH.removeMessages(24);
                    if (WindowManagerService.this.mWaitingForDrawn.isEmpty()) {
                        allWindowsDrawn = true;
                    } else {
                        WindowManagerService.this.mH.sendEmptyMessageDelayed(24, timeout);
                        WindowManagerService.this.checkDrawnWindowsLocked();
                    }
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
            if (allWindowsDrawn) {
                callback.run();
            }
        }

        @Override // com.android.server.wm.WindowManagerInternal
        public void setForcedDisplaySize(int displayId, int width, int height) {
            WindowManagerService.this.setForcedDisplaySize(displayId, width, height);
        }

        @Override // com.android.server.wm.WindowManagerInternal
        public void clearForcedDisplaySize(int displayId) {
            WindowManagerService.this.clearForcedDisplaySize(displayId);
        }

        @Override // com.android.server.wm.WindowManagerInternal
        public void addWindowToken(IBinder token, int type, int displayId) {
            WindowManagerService.this.addWindowToken(token, type, displayId);
        }

        @Override // com.android.server.wm.WindowManagerInternal
        public void removeWindowToken(IBinder binder, boolean removeWindows, int displayId) {
            synchronized (WindowManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    if (removeWindows) {
                        DisplayContent dc = WindowManagerService.this.mRoot.getDisplayContent(displayId);
                        if (dc == null) {
                            Slog.w("WindowManager", "removeWindowToken: Attempted to remove token: " + binder + " for non-exiting displayId=" + displayId);
                            return;
                        }
                        WindowToken token = dc.removeWindowToken(binder);
                        if (token == null) {
                            Slog.w("WindowManager", "removeWindowToken: Attempted to remove non-existing token: " + binder);
                            WindowManagerService.resetPriorityAfterLockedSection();
                            return;
                        }
                        token.removeAllWindowsIfPossible();
                    }
                    WindowManagerService.this.removeWindowToken(binder, displayId);
                    WindowManagerService.resetPriorityAfterLockedSection();
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.WindowManagerInternal
        public void registerAppTransitionListener(WindowManagerInternal.AppTransitionListener listener) {
            synchronized (WindowManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    WindowManagerService.this.getDefaultDisplayContentLocked().mAppTransition.registerListenerLocked(listener);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.WindowManagerInternal
        public void reportPasswordChanged(int userId) {
            WindowManagerService.this.mKeyguardDisableHandler.updateKeyguardEnabled(userId);
        }

        @Override // com.android.server.wm.WindowManagerInternal
        public int getInputMethodWindowVisibleHeight(int displayId) {
            int inputMethodWindowVisibleHeight;
            synchronized (WindowManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    inputMethodWindowVisibleHeight = WindowManagerService.this.mRoot.getDisplayContent(displayId).mDisplayFrames.getInputMethodWindowVisibleHeight();
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
            return inputMethodWindowVisibleHeight;
        }

        @Override // com.android.server.wm.WindowManagerInternal
        public void updateInputMethodWindowStatus(IBinder imeToken, boolean imeWindowVisible, boolean dismissImeOnBackKeyPressed) {
            WindowManagerService.this.mPolicy.setDismissImeOnBackKeyPressed(dismissImeOnBackKeyPressed);
        }

        @Override // com.android.server.wm.WindowManagerInternal
        public void updateInputMethodTargetWindow(IBinder imeToken, IBinder imeTargetWindowToken) {
            if (WindowManagerDebugConfig.DEBUG_INPUT_METHOD) {
                Slog.w("WindowManager", "updateInputMethodTargetWindow: imeToken=" + imeToken + " imeTargetWindowToken=" + imeTargetWindowToken);
            }
        }

        @Override // com.android.server.wm.WindowManagerInternal
        public boolean isHardKeyboardAvailable() {
            boolean z;
            synchronized (WindowManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    z = WindowManagerService.this.mHardKeyboardAvailable;
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
            return z;
        }

        @Override // com.android.server.wm.WindowManagerInternal
        public void setOnHardKeyboardStatusChangeListener(WindowManagerInternal.OnHardKeyboardStatusChangeListener listener) {
            synchronized (WindowManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    WindowManagerService.this.mHardKeyboardStatusChangeListener = listener;
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.WindowManagerInternal
        public boolean isStackVisibleLw(int windowingMode) {
            return WindowManagerService.this.getDefaultDisplayContentLocked().isStackVisible(windowingMode);
        }

        @Override // com.android.server.wm.WindowManagerInternal
        public void computeWindowsForAccessibility() {
            AccessibilityController accessibilityController;
            synchronized (WindowManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    accessibilityController = WindowManagerService.this.mAccessibilityController;
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
            if (accessibilityController != null) {
                accessibilityController.performComputeChangedWindowsNotLocked(true);
            }
        }

        @Override // com.android.server.wm.WindowManagerInternal
        public void setVr2dDisplayId(int vr2dDisplayId) {
            if (WindowManagerDebugConfig.DEBUG_DISPLAY) {
                Slog.d("WindowManager", "setVr2dDisplayId called for: " + vr2dDisplayId);
            }
            synchronized (WindowManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    WindowManagerService.this.mVr2dDisplayId = vr2dDisplayId;
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.WindowManagerInternal
        public void registerDragDropControllerCallback(WindowManagerInternal.IDragDropCallback callback) {
            WindowManagerService.this.mDragDropController.registerCallback(callback);
        }

        @Override // com.android.server.wm.WindowManagerInternal
        public void lockNow() {
            WindowManagerService.this.lockNow(null);
        }

        @Override // com.android.server.wm.WindowManagerInternal
        public int getWindowOwnerUserId(IBinder token) {
            synchronized (WindowManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    WindowState window = (WindowState) WindowManagerService.this.mWindowMap.get(token);
                    if (window != null) {
                        return UserHandle.getUserId(window.mOwnerUid);
                    }
                    WindowManagerService.resetPriorityAfterLockedSection();
                    return -10000;
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.WindowManagerInternal
        public boolean isUidFocused(int uid) {
            synchronized (WindowManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    for (int i = WindowManagerService.this.mRoot.getChildCount() - 1; i >= 0; i--) {
                        DisplayContent displayContent = (DisplayContent) WindowManagerService.this.mRoot.getChildAt(i);
                        if (displayContent.mCurrentFocus != null && uid == displayContent.mCurrentFocus.getOwningUid()) {
                            return true;
                        }
                    }
                    WindowManagerService.resetPriorityAfterLockedSection();
                    return false;
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.WindowManagerInternal
        public boolean isInputMethodClientFocus(int uid, int pid, int displayId) {
            if (displayId == -1) {
                return false;
            }
            synchronized (WindowManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    DisplayContent displayContent = WindowManagerService.this.mRoot.getTopFocusedDisplayContent();
                    if (displayContent != null && displayContent.getDisplayId() == displayId) {
                        if (displayContent.hasAccess(uid)) {
                            if (displayContent.isInputMethodClientFocus(uid, pid)) {
                                WindowManagerService.resetPriorityAfterLockedSection();
                                return true;
                            }
                            WindowState currentFocus = displayContent.mCurrentFocus;
                            if (currentFocus != null && currentFocus.mSession.mUid == uid && currentFocus.mSession.mPid == pid) {
                                WindowManagerService.resetPriorityAfterLockedSection();
                                return true;
                            }
                            WindowManagerService.resetPriorityAfterLockedSection();
                            return false;
                        }
                    }
                    return false;
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.WindowManagerInternal
        public boolean isUidAllowedOnDisplay(int displayId, int uid) {
            boolean z = true;
            if (displayId == 0) {
                return true;
            }
            if (displayId == -1) {
                return false;
            }
            synchronized (WindowManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    DisplayContent displayContent = WindowManagerService.this.mRoot.getDisplayContent(displayId);
                    if (displayContent == null || !displayContent.hasAccess(uid)) {
                        z = false;
                    }
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
            return z;
        }

        @Override // com.android.server.wm.WindowManagerInternal
        public int getDisplayIdForWindow(IBinder windowToken) {
            synchronized (WindowManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    WindowState window = (WindowState) WindowManagerService.this.mWindowMap.get(windowToken);
                    if (window != null) {
                        return window.getDisplayContent().getDisplayId();
                    }
                    WindowManagerService.resetPriorityAfterLockedSection();
                    return -1;
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.WindowManagerInternal
        public int getTopFocusedDisplayId() {
            int displayId;
            synchronized (WindowManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    displayId = WindowManagerService.this.mRoot.getTopFocusedDisplayContent().getDisplayId();
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
            return displayId;
        }

        @Override // com.android.server.wm.WindowManagerInternal
        public boolean shouldShowSystemDecorOnDisplay(int displayId) {
            boolean shouldShowSystemDecors;
            synchronized (WindowManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    shouldShowSystemDecors = WindowManagerService.this.shouldShowSystemDecors(displayId);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
            return shouldShowSystemDecors;
        }

        @Override // com.android.server.wm.WindowManagerInternal
        public boolean shouldShowIme(int displayId) {
            boolean shouldShowIme;
            synchronized (WindowManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    shouldShowIme = WindowManagerService.this.shouldShowIme(displayId);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
            return shouldShowIme;
        }

        @Override // com.android.server.wm.WindowManagerInternal
        public void addNonHighRefreshRatePackage(String packageName) {
            synchronized (WindowManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    WindowManagerService.this.mRoot.forAllDisplays(new Consumer(packageName) {
                        /* class com.android.server.wm.$$Lambda$WindowManagerService$LocalService$_nYJRiVOgbON7mI191FIzNAk4Xs */
                        private final /* synthetic */ String f$0;

                        {
                            this.f$0 = r1;
                        }

                        @Override // java.util.function.Consumer
                        public final void accept(Object obj) {
                            ((DisplayContent) obj).getDisplayPolicy().getRefreshRatePolicy().addNonHighRefreshRatePackage(this.f$0);
                        }
                    });
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.WindowManagerInternal
        public void removeNonHighRefreshRatePackage(String packageName) {
            synchronized (WindowManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    WindowManagerService.this.mRoot.forAllDisplays(new Consumer(packageName) {
                        /* class com.android.server.wm.$$Lambda$WindowManagerService$LocalService$rEGrcIRCgYp4kzr5xA12LKQX0E */
                        private final /* synthetic */ String f$0;

                        {
                            this.f$0 = r1;
                        }

                        @Override // java.util.function.Consumer
                        public final void accept(Object obj) {
                            ((DisplayContent) obj).getDisplayPolicy().getRefreshRatePolicy().removeNonHighRefreshRatePackage(this.f$0);
                        }
                    });
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void registerAppFreezeListener(AppFreezeListener listener) {
        if (!this.mAppFreezeListeners.contains(listener)) {
            this.mAppFreezeListeners.add(listener);
        }
    }

    /* access modifiers changed from: package-private */
    public void unregisterAppFreezeListener(AppFreezeListener listener) {
        this.mAppFreezeListeners.remove(listener);
    }

    /* access modifiers changed from: package-private */
    public void inSurfaceTransaction(Runnable exec) {
        SurfaceControl.openTransaction();
        try {
            exec.run();
        } finally {
            SurfaceControl.closeTransaction();
        }
    }

    public void disableNonVrUi(boolean disable) {
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                boolean showAlertWindowNotifications = !disable;
                if (showAlertWindowNotifications != this.mShowAlertWindowNotifications) {
                    this.mShowAlertWindowNotifications = showAlertWindowNotifications;
                    for (int i = this.mSessions.size() - 1; i >= 0; i--) {
                        this.mSessions.valueAt(i).setShowingAlertWindowNotificationAllowed(this.mShowAlertWindowNotifications);
                    }
                    resetPriorityAfterLockedSection();
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean hasWideColorGamutSupport() {
        return this.mHasWideColorGamutSupport && SystemProperties.getInt("persist.sys.sf.native_mode", 0) != 1;
    }

    /* access modifiers changed from: package-private */
    public boolean hasHdrSupport() {
        return this.mHasHdrSupport && hasWideColorGamutSupport();
    }

    /* access modifiers changed from: package-private */
    public void updateNonSystemOverlayWindowsVisibilityIfNeeded(WindowState win, boolean surfaceShown) {
        if (win.hideNonSystemOverlayWindowsWhenVisible() || this.mHidingNonSystemOverlayWindows.contains(win)) {
            boolean systemAlertWindowsHidden = !this.mHidingNonSystemOverlayWindows.isEmpty();
            if (!surfaceShown) {
                this.mHidingNonSystemOverlayWindows.remove(win);
            } else if (!this.mHidingNonSystemOverlayWindows.contains(win)) {
                this.mHidingNonSystemOverlayWindows.add(win);
            }
            boolean hideSystemAlertWindows = !this.mHidingNonSystemOverlayWindows.isEmpty();
            if (systemAlertWindowsHidden != hideSystemAlertWindows) {
                this.mRoot.forAllWindows((Consumer<WindowState>) new Consumer(hideSystemAlertWindows) {
                    /* class com.android.server.wm.$$Lambda$WindowManagerService$nQHccAXNqWhpUTYdUQi4f3vYirA */
                    private final /* synthetic */ boolean f$0;

                    {
                        this.f$0 = r1;
                    }

                    @Override // java.util.function.Consumer
                    public final void accept(Object obj) {
                        ((WindowState) obj).setForceHideNonSystemOverlayWindowIfNeeded(this.f$0);
                    }
                }, false);
            }
        }
    }

    public void applyMagnificationSpecLocked(int displayId, MagnificationSpec spec) {
        DisplayContent displayContent = this.mRoot.getDisplayContent(displayId);
        if (displayContent != null) {
            displayContent.applyMagnificationSpec(spec);
        }
    }

    public String getFocusedWindowPkg() {
        WindowState window = getFocusedWindow();
        if (window != null) {
            return window.getOwningPackage();
        }
        return "";
    }

    public void disableStatusBar(boolean disable) {
        Context context = this.mContext;
        if (context != null) {
            StatusBarManager mStatusBar = (StatusBarManager) context.getSystemService("statusbar");
            int state = 0;
            if (disable) {
                state = 0 | 65536;
            }
            Slog.v("WindowManager", "disableStatusBar state: " + state + " , disable: " + disable);
            mStatusBar.disable(state);
        }
    }

    public void registerOppoWindowStateObserver(IOppoWindowStateObserver observer) {
        this.mColorWmsEx.registerOppoWindowStateObserver(observer);
    }

    public void unregisterOppoWindowStateObserver(IOppoWindowStateObserver observer) {
        this.mColorWmsEx.registerOppoWindowStateObserver(observer);
    }

    public boolean isInFreeformMode() {
        synchronized (this.mWindowMap) {
            TaskStack stack = this.mRoot.getStack(5, 1);
            if (stack == null) {
                return false;
            }
            Slog.v("WindowManager", "isInFreeformMode stack: " + stack);
            return true;
        }
    }

    public void getFreeformStackBounds(Rect outBounds) {
        synchronized (this.mWindowMap) {
            if (outBounds != null) {
                if (this.mRoot.getStack(5, 1) == null) {
                    outBounds.setEmpty();
                }
            }
        }
    }

    public InputManagerService getInputManagerService() {
        return this.mInputManager;
    }

    /* access modifiers changed from: package-private */
    public SurfaceControl.Builder makeSurfaceBuilder(SurfaceSession s) {
        return this.mSurfaceBuilderFactory.make(s);
    }

    /* access modifiers changed from: package-private */
    public void sendSetRunningRemoteAnimation(int pid, boolean runningRemoteAnimation) {
        this.mH.obtainMessage(59, pid, runningRemoteAnimation ? 1 : 0).sendToTarget();
    }

    /* access modifiers changed from: package-private */
    public void startSeamlessRotation() {
        this.mSeamlessRotationCount = 0;
        this.mRotatingSeamlessly = true;
    }

    /* access modifiers changed from: package-private */
    public boolean isRotatingSeamlessly() {
        return this.mRotatingSeamlessly;
    }

    /* access modifiers changed from: package-private */
    public void finishSeamlessRotation() {
        this.mRotatingSeamlessly = false;
    }

    private class ProcessStates {
        private static final int PROC_STATE_DEAD = 0;
        private static final int PROC_STATE_INTERRUPTIBLE_SLEEPING = 2;
        private static final int PROC_STATE_RUNNING = 3;
        private static final int PROC_STATE_SUSPEND = 1;
        private static final int PROC_STATE_UNDEFINED = -1;
        boolean mIgnoreCall;
        boolean mIsSuspend;
        int mPid;
        int mProcState;
        int mUid;

        public ProcessStates(boolean isSuspend, int pid, int uid) {
            this.mIsSuspend = isSuspend;
            this.mPid = pid;
            this.mUid = uid;
        }

        public ProcessStates(int procState, int pid, int uid) {
            this.mProcState = procState;
            this.mIgnoreCall = ignoreCall(procState);
            this.mPid = pid;
            this.mUid = uid;
        }

        private boolean ignoreCall(int procState) {
            return (3 == procState || 2 == procState) ? false : true;
        }
    }

    /* access modifiers changed from: package-private */
    public void onLockTaskStateChanged(int lockTaskState) {
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                this.mRoot.forAllDisplayPolicies(PooledLambda.obtainConsumer($$Lambda$5zz5Ugt4wxIXoNE3lZS6NA9z_Jk.INSTANCE, PooledLambda.__(), Integer.valueOf(lockTaskState)));
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void setAodShowing(boolean aodShowing) {
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                if (this.mPolicy.setAodShowing(aodShowing)) {
                    this.mWindowPlacerLocked.performSurfacePlacement();
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public int getTypedWindowLayer(int type) {
        return 0;
    }

    public int getFocusedWindowIgnoreHomeMenuKey() {
        return 0;
    }

    public boolean injectInputAfterTransactionsApplied(InputEvent ev, int mode) {
        boolean isDown;
        KeyEvent keyEvent;
        boolean isMouseEvent = false;
        if (ev instanceof KeyEvent) {
            KeyEvent keyEvent2 = (KeyEvent) ev;
            isDown = keyEvent2.getAction() == 0;
            keyEvent = keyEvent2.getAction() == 1 ? 1 : null;
        } else {
            MotionEvent motionEvent = (MotionEvent) ev;
            isDown = motionEvent.getAction() == 0;
            keyEvent = motionEvent.getAction() == 1 ? 1 : null;
        }
        if (ev.getSource() == 8194) {
            isMouseEvent = true;
        }
        if (isDown || isMouseEvent) {
            syncInputTransactions();
        }
        boolean result = ((InputManagerInternal) LocalServices.getService(InputManagerInternal.class)).injectInputEvent(ev, mode);
        if (keyEvent != null) {
            syncInputTransactions();
        }
        return result;
    }

    /* JADX INFO: finally extract failed */
    public void syncInputTransactions() {
        long token = Binder.clearCallingIdentity();
        try {
            waitForAnimationsToComplete();
            synchronized (this.mGlobalLock) {
                try {
                    boostPriorityForLockedSection();
                    this.mWindowPlacerLocked.performSurfacePlacementIfScheduled();
                    this.mRoot.forAllDisplays($$Lambda$WindowManagerService$QGTApvQkj7JVfTvOVrLJ6s24v8.INSTANCE);
                } catch (Throwable th) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
            resetPriorityAfterLockedSection();
            new SurfaceControl.Transaction().syncInputWindows().apply(true);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    private void waitForAnimationsToComplete() {
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                long timeoutRemaining = 5000;
                while (this.mRoot.isSelfOrChildAnimating() && timeoutRemaining > 0) {
                    long startTime = System.currentTimeMillis();
                    try {
                        this.mGlobalLock.wait(timeoutRemaining);
                    } catch (InterruptedException e) {
                    }
                    timeoutRemaining -= System.currentTimeMillis() - startTime;
                }
                if (this.mRoot.isSelfOrChildAnimating()) {
                    Log.w("WindowManager", "Timed out waiting for animations to complete.");
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onAnimationFinished() {
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                this.mGlobalLock.notifyAll();
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onPointerDownOutsideFocusLocked(IBinder touchedToken) {
        WindowState touchedWindow = windowForClientLocked((Session) null, touchedToken, false);
        if (touchedWindow != null && touchedWindow.canReceiveKeys()) {
            handleTaskFocusChange(touchedWindow.getTask());
            handleDisplayFocusChange(touchedWindow);
        }
    }

    private void handleTaskFocusChange(Task task) {
        if (task != null && !task.mStack.isActivityTypeHome()) {
            try {
                this.mActivityTaskManager.setFocusedTask(task.mTaskId);
            } catch (RemoteException e) {
            }
        }
    }

    private void handleDisplayFocusChange(WindowState window) {
        WindowContainer parent;
        DisplayContent displayContent = window.getDisplayContent();
        if (displayContent != null && window.canReceiveKeys() && (parent = displayContent.getParent()) != null && parent.getTopChild() != displayContent) {
            parent.positionChildAt(Integer.MAX_VALUE, displayContent, true);
            displayContent.mAcitvityDisplay.ensureActivitiesVisible(null, 0, false, true);
        }
    }

    public boolean isActivityNeedPalette(String pkg, String activityName) {
        if (this.mSystemReady) {
            return ColorNavigationBarUtil.getInstance().isActivityNeedPalette(pkg, activityName);
        }
        return false;
    }

    public int getNavBarColorFromAdaptation(String pkg, String activityName) {
        if (this.mSystemReady) {
            return ColorNavigationBarUtil.getInstance().getNavBarColorFromAdaptation(pkg, activityName);
        }
        return 0;
    }

    public int getStatusBarColorFromAdaptation(String pkg, String activityName) {
        if (this.mSystemReady) {
            return ColorNavigationBarUtil.getInstance().getStatusBarColorFromAdaptation(pkg, activityName);
        }
        return 0;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void killAutoResolutionAPP() {
        try {
            for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : this.mActivityManager.getRunningAppProcesses()) {
                if (mOppoAppScaleHelper.GetNewScale(runningAppProcessInfo.processName) != 1.0f) {
                    Process.killProcess(runningAppProcessInfo.pid);
                    Log.d("WindowManager", "DisplayCompat killProcess:" + runningAppProcessInfo.processName);
                }
            }
        } catch (RemoteException e) {
            Slog.w("WindowManager", "am.getRunningAppProcesses() failed");
        }
    }

    public int getImeBgColorFromAdaptation(String pkg) {
        if (this.mSystemReady) {
            return ColorNavigationBarUtil.getInstance().getImeBgColorFromAdaptation(pkg);
        }
        return 0;
    }

    public int getNavBarHeight(int rotation, int uiMode) {
        return this.mPolicy.getNavBarHeight(rotation, uiMode);
    }

    public class ColorWindowManagerServiceInner implements IColorWindowManagerServiceInner {
        public ColorWindowManagerServiceInner() {
        }

        @Override // com.android.server.wm.IColorWindowManagerServiceInner
        public void updateAppOpsState() {
            WindowManagerService.this.updateAppOpsState();
        }

        @Override // com.android.server.wm.IColorWindowManagerServiceInner
        public void updateAppOpsState(String packageName, Boolean state) {
            WindowManagerService.this.updateAppOpsState(packageName, state);
        }

        @Override // com.android.server.wm.IColorWindowManagerServiceInner
        public WindowState getFocusedWindow() {
            return WindowManagerService.this.getFocusedWindow();
        }

        @Override // com.android.server.wm.IColorWindowManagerServiceInner
        public String getFocusWindowPkgName() {
            synchronized (WindowManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    WindowState focus = WindowManagerService.this.getFocusedWindow();
                    if (focus != null) {
                        return focus.getOwningPackage();
                    }
                    WindowManagerService.resetPriorityAfterLockedSection();
                    return "";
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.IColorWindowManagerServiceInner
        public void resetAnimationSetting() {
            WindowManagerService windowManagerService = WindowManagerService.this;
            windowManagerService.mWindowAnimationScaleSetting = OppoBrightUtils.MIN_LUX_LIMITI;
            windowManagerService.mTransitionAnimationScaleSetting = OppoBrightUtils.MIN_LUX_LIMITI;
        }
    }

    @Override // com.android.server.wm.OppoBaseWindowManagerService
    public IColorWindowManagerServiceInner createColorWindowManagerServiceInner() {
        return new ColorWindowManagerServiceInner();
    }

    @Override // com.android.server.wm.OppoBaseWindowManagerService
    public void updateAppOpsState(String packageName, Boolean state) {
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                int numDisplays = this.mRoot.getDisplayContents().size();
                for (int displayNdx = 0; displayNdx < numDisplays; displayNdx++) {
                    this.mRoot.getDisplayContents().get(displayNdx).forAllWindows((Consumer<WindowState>) new Consumer(packageName, state) {
                        /* class com.android.server.wm.$$Lambda$WindowManagerService$uRY35RJfT2AvURYxz9bruMxOdpY */
                        private final /* synthetic */ String f$1;
                        private final /* synthetic */ Boolean f$2;

                        {
                            this.f$1 = r2;
                            this.f$2 = r3;
                        }

                        @Override // java.util.function.Consumer
                        public final void accept(Object obj) {
                            WindowManagerService.this.lambda$updateAppOpsState$13$WindowManagerService(this.f$1, this.f$2, (WindowState) obj);
                        }
                    }, false);
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public /* synthetic */ void lambda$updateAppOpsState$13$WindowManagerService(String packageName, Boolean state, WindowState w) {
        if (w.getOwningPackage() != null && packageName != null && w.getOwningPackage().equals(packageName) && w.mAppOp != -1) {
            if (24 == w.mAppOp) {
                w.setAppOpVisibilityLw(state.booleanValue());
                Slog.d("WindowManager", "updateAppOpsState, packageName : " + packageName + " , state : " + state);
            } else if (45 == w.mAppOp) {
                w.setAppOpVisibilityLw(state.booleanValue());
                Slog.d("WindowManager", "updateAppOpsState, packageName : " + packageName + " , state : " + state);
            }
            updateChildWindowState(w, state.booleanValue());
        }
    }

    private void updateChildWindowState(WindowState win, boolean state) {
        if (!(win.mChildren == null || win.mChildren.isEmpty())) {
            WindowList childWindows = win.mChildren;
            int numChildWindows = childWindows.size();
            for (int i = 0; i < numChildWindows; i++) {
                try {
                    ((WindowState) childWindows.get(i)).setAppOpVisibilityLw(state);
                } catch (Exception e) {
                    e.printStackTrace();
                    Slog.e("WindowManager", "updateAppOpsState error!");
                    return;
                }
            }
            String packageName = win.getOwningPackage();
            this.mColorWmsEx.sendBroadcastForFloatWindow(this, this.mAppOps, this.mActivityManager, this.mContext, state, packageName);
            Slog.d("WindowManager", "child win of : " + packageName + " , state " + state);
        }
    }

    private String getForegroundPackage() {
        ComponentName cn;
        try {
            cn = new OppoActivityManager().getTopActivityComponentName();
        } catch (Exception e) {
            Log.w("WindowManager", "getTopActivityComponentName exception");
            cn = null;
        }
        if (cn != null) {
            return cn.getPackageName();
        }
        return null;
    }

    public void updateAppOpsStateForFloatWindow() {
        String topAppName = "";
        try {
            if (this.mActivityManager.getTopAppName() != null) {
                topAppName = this.mActivityManager.getTopAppName().getPackageName();
            }
        } catch (RemoteException e) {
        }
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                int numDisplays = this.mRoot.getDisplayContents().size();
                for (int displayNdx = 0; displayNdx < numDisplays; displayNdx++) {
                    this.mRoot.getDisplayContents().get(displayNdx).forAllWindows((Consumer<WindowState>) new Consumer(topAppName) {
                        /* class com.android.server.wm.$$Lambda$WindowManagerService$YCQk4h3m_yFMR96IaNUQxgMkMVk */
                        private final /* synthetic */ String f$1;

                        {
                            this.f$1 = r2;
                        }

                        @Override // java.util.function.Consumer
                        public final void accept(Object obj) {
                            WindowManagerService.this.lambda$updateAppOpsStateForFloatWindow$14$WindowManagerService(this.f$1, (WindowState) obj);
                        }
                    }, false);
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public /* synthetic */ void lambda$updateAppOpsStateForFloatWindow$14$WindowManagerService(String topAppPkgName, WindowState w) {
        if (w.mAppOp != -1) {
            int mode = this.mAppOps.checkOpNoThrow(w.mAppOp, w.getOwningUid(), w.getOwningPackage());
            boolean state = false;
            if (24 == w.mAppOp) {
                if (mode == 0 || mode == 3 || topAppPkgName.equals(w.getOwningPackage())) {
                    state = true;
                }
                w.setAppOpVisibilityLw(state);
                updateChildWindowState(w, state);
                if ((mode == 0 || mode == 3) && this.mColorWmsEx.checkFloatWindowSet(sInstance, this.mAppOps, this.mActivityManager, this.mContext, w.getOwningPackage())) {
                    this.mColorWmsEx.removeFromFloatWindowSet(sInstance, this.mAppOps, this.mActivityManager, this.mContext, w.getOwningPackage());
                } else if (mode != 0 && mode != 3 && !this.mColorWmsEx.checkFloatWindowSet(sInstance, this.mAppOps, this.mActivityManager, this.mContext, w.getOwningPackage())) {
                    this.mColorWmsEx.addToFloatWindowSet(sInstance, this.mAppOps, this.mActivityManager, this.mContext, w.getOwningPackage());
                }
            } else if (45 == w.mAppOp) {
                boolean isPkgToastClosed = this.mColorWmsEx.isPackageToastClosed(sInstance, this.mAppOps, this.mActivityManager, this.mContext, w.getOwningPackage());
                if ((mode == 0 || mode == 3) && (!isPkgToastClosed || topAppPkgName.equals(w.getOwningPackage()))) {
                    state = true;
                }
                w.setAppOpVisibilityLw(state);
                updateChildWindowState(w, state);
                if (!isPkgToastClosed && this.mColorWmsEx.checkFloatWindowSet(sInstance, this.mAppOps, this.mActivityManager, this.mContext, w.getOwningPackage())) {
                    this.mColorWmsEx.removeFromFloatWindowSet(sInstance, this.mAppOps, this.mActivityManager, this.mContext, w.getOwningPackage());
                } else if (isPkgToastClosed && !this.mColorWmsEx.checkFloatWindowSet(sInstance, this.mAppOps, this.mActivityManager, this.mContext, w.getOwningPackage())) {
                    this.mColorWmsEx.addToFloatWindowSet(sInstance, this.mAppOps, this.mActivityManager, this.mContext, w.getOwningPackage());
                }
            } else {
                if (mode == 0 || mode == 3) {
                    state = true;
                }
                w.setAppOpVisibilityLw(state);
            }
        }
    }

    public Bitmap captureLayersForKgd() {
        WindowState w;
        this.mHideFakeAppLayer = true;
        DisplayContent dc = getDefaultDisplayContentLocked();
        if (dc == null || dc.mFocusedApp == null || this.mTaskSnapshotController == null || (w = dc.mFocusedApp.findMainWindow()) == null || isSecureLocked(w)) {
            return null;
        }
        return this.mTaskSnapshotController.takeSnapshotForKgd(dc.mFocusedApp);
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.OppoBaseWindowManagerService
    public boolean killNotDrawnAppsWhenFrozen() {
        if (!this.mDisplayFrozen) {
            return false;
        }
        synchronized (this.mWindowMap) {
            this.mRoot.forAllWindows((Consumer<WindowState>) $$Lambda$WindowManagerService$mxnGu6wgKmGTdVUX30qyNqEQwj8.INSTANCE, false);
        }
        return true;
    }

    static /* synthetic */ void lambda$killNotDrawnAppsWhenFrozen$15(WindowState w) {
        if (!w.hasDrawnLw() && w.mSession != null && Process.myPid() != w.mSession.mPid) {
            Process.killProcess(w.mSession.mPid);
            Log.i("WindowManager", "killNotDrawnAppsWhenFrozen kill w.mSession.mPid:" + w.mSession.mPid);
        }
    }

    /* access modifiers changed from: package-private */
    public void setWindowOverrideRefreshRate(Session session, IWindow window, int refreshRateId) {
        WindowState win;
        synchronized (this.mGlobalLock) {
            try {
                boostPriorityForLockedSection();
                win = windowForClientLocked(session, window, false);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        if (win != null && win.getDisplayContent() != null) {
            win.getDisplayContent().getOppoDisplayModeManager().overrideVendorPreferredModeIdIfNeed(win, refreshRateId);
        }
    }
}
