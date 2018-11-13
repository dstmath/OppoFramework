package com.android.server.wm;

import android.animation.ValueAnimator;
import android.annotation.IntDef;
import android.annotation.OppoHook;
import android.annotation.OppoHook.OppoHookType;
import android.annotation.OppoHook.OppoRomType;
import android.app.ActivityManager;
import android.app.ActivityManagerInternal;
import android.app.ActivityManagerNative;
import android.app.AppOpsManager;
import android.app.AppOpsManager.OnOpChangedInternalListener;
import android.app.IActivityManager;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.OppoActivityManager;
import android.app.PendingIntent;
import android.app.StatusBarManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.hardware.display.DisplayManager;
import android.hardware.display.DisplayManagerInternal;
import android.hardware.input.InputManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.IRemoteCallback;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.PowerManagerInternal;
import android.os.PowerManagerInternal.LowPowerModeListener;
import android.os.Process;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.SystemService;
import android.os.Trace;
import android.os.UserHandle;
import android.os.WorkSource;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.util.ArraySet;
import android.util.DisplayMetrics;
import android.util.EventLog;
import android.util.Log;
import android.util.LruCache;
import android.util.Pair;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.util.TimeUtils;
import android.util.TypedValue;
import android.view.AppTransitionAnimationSpec;
import android.view.Choreographer;
import android.view.Display;
import android.view.DisplayInfo;
import android.view.IAppTransitionAnimationSpecsFuture;
import android.view.IApplicationToken;
import android.view.IDockedStackListener;
import android.view.IInputFilter;
import android.view.IOnKeyguardExitResult;
import android.view.IRotationWatcher;
import android.view.IWindow;
import android.view.IWindowId;
import android.view.IWindowManager.Stub;
import android.view.IWindowSession;
import android.view.IWindowSessionCallback;
import android.view.InputChannel;
import android.view.InputDevice;
import android.view.InputEvent;
import android.view.InputEventReceiver;
import android.view.InputEventReceiver.Factory;
import android.view.MagnificationSpec;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.Surface.OutOfResourcesException;
import android.view.SurfaceControl;
import android.view.SurfaceSession;
import android.view.View;
import android.view.WindowContentFrameStats;
import android.view.WindowManager.LayoutParams;
import android.view.WindowManagerInternal;
import android.view.WindowManagerInternal.AppTransitionListener;
import android.view.WindowManagerInternal.MagnificationCallbacks;
import android.view.WindowManagerInternal.OnHardKeyboardStatusChangeListener;
import android.view.WindowManagerInternal.WindowsForAccessibilityCallback;
import android.view.WindowManagerPolicy;
import android.view.WindowManagerPolicy.InputConsumer;
import android.view.WindowManagerPolicy.OnKeyguardExitResult;
import android.view.WindowManagerPolicy.PointerEventListener;
import android.view.WindowManagerPolicy.WindowManagerFuncs;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManagerInternal;
import com.android.internal.R;
import com.android.internal.app.IAssistScreenshotReceiver;
import com.android.internal.os.IResultReceiver;
import com.android.internal.policy.IShortcutService;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.FastPrintWriter;
import com.android.internal.view.IInputContext;
import com.android.internal.view.IInputMethodClient;
import com.android.internal.view.IInputMethodManager;
import com.android.internal.view.WindowManagerPolicyThread;
import com.android.server.AttributeCache;
import com.android.server.AttributeCache.Entry;
import com.android.server.CheckBlockedException;
import com.android.server.DisplayThread;
import com.android.server.EventLogTags;
import com.android.server.FgThread;
import com.android.server.LocalServices;
import com.android.server.LocationManagerService;
import com.android.server.UiThread;
import com.android.server.Watchdog;
import com.android.server.Watchdog.Monitor;
import com.android.server.WmsFrozenStateWatch;
import com.android.server.am.ActivityManagerService;
import com.android.server.am.OppoCrashClearManager;
import com.android.server.am.OppoMultiAppManager;
import com.android.server.am.OppoProtectEyeManagerService;
import com.android.server.am.OppoProtectEyeManagerService.ActivityChangedListener;
import com.android.server.display.OppoBrightUtils;
import com.android.server.input.InputManagerService;
import com.android.server.location.LocationFudger;
import com.android.server.notification.NotificationManagerService;
import com.android.server.oppo.DumpObject;
import com.android.server.oppo.IElsaManager;
import com.android.server.policy.OppoPhoneWindowManager;
import com.android.server.policy.PhoneWindowManager;
import com.android.server.power.ShutdownThread;
import com.android.server.voiceinteraction.DatabaseHelper.SoundModelContract;
import com.mediatek.anrappframeworks.ANRAppFrameworks;
import com.mediatek.anrappmanager.ANRAppManager;
import com.mediatek.anrmanager.ANRManager;
import com.mediatek.appworkingset.AWSDBHelper.PackageProcessList;
import com.mediatek.multiwindow.IFreeformStackListener;
import com.mediatek.multiwindow.MultiWindowManager;
import com.mediatek.perfservice.IPerfServiceWrapper;
import com.mediatek.perfservice.PerfServiceWrapper;
import com.oppo.hypnus.Hypnus;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

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
public class WindowManagerService extends Stub implements Monitor, WindowManagerFuncs {
    private static final int ACTION_POP_ADD = 1;
    private static final int ACTION_POP_REMOVE = 0;
    public static final String ALARM_BOOT_DONE = "android.intent.action.normal.boot.done";
    private static final boolean ALWAYS_KEEP_CURRENT = true;
    private static final int ANIMATION_DURATION_SCALE = 2;
    private static final String APP_FROZEN_TIMEOUT_PROP = "sys.app_freeze_timeout";
    private static final int BOOT_ANIMATION_POLL_INTERVAL = 200;
    private static final String BOOT_ANIMATION_SERVICE = "bootanim";
    static final int CACHE_CONFIG_FIRST_FRAME = 1;
    static final int CACHE_CONFIG_LAST_FRAME = 2;
    static final int CACHE_CONFIG_STARTING_WINDOW = 0;
    private static final String COLOROS_FLOAT = "com.coloros.floatassistant";
    static final boolean CUSTOM_SCREEN_ROTATION = true;
    static boolean DEBUG_DETAIL = false;
    static boolean DEBUG_POLICY = false;
    static boolean DEBUG_WMS = false;
    static final long DEFAULT_INPUT_DISPATCHING_TIMEOUT_NANOS = 8000000000L;
    private static final String DENSITY_OVERRIDE = "ro.config.density_override";
    static final int DISABLE_FROZEN_BY_SECURITY_TIMEOUT = 5000;
    private static final float DRAG_SHADOW_ALPHA_TRANSPARENT = 0.7071f;
    private static final String EYEPROTECT_ENABLE = "color_eyeprotect_enable";
    private static final String EYEPROTECT_INVERSE_ENABLE = "inverse_on";
    private static final int FREEZE_TIMEOUT_DEFAULE = 5000;
    private static final int FREEZE_TIMEOUT_SPLIT = 500;
    private static final int INPUT_DEVICES_READY_FOR_SAFE_MODE_DETECTION_TIMEOUT_MILLIS = 1000;
    public static final String IPO_DISABLE = "android.intent.action.ACTION_BOOT_IPO";
    public static final String IPO_ENABLE = "android.intent.action.ACTION_SHUTDOWN_IPO";
    static final boolean IS_USER_BUILD = false;
    static final int LAST_ANR_LIFETIME_DURATION_MSECS = 7200000;
    static final int LAYER_OFFSET_DIM = 1;
    static final int LAYER_OFFSET_THUMBNAIL = 4;
    static final int LAYOUT_REPEAT_THRESHOLD = 4;
    static final int MAX_ANIMATION_DURATION = 10000;
    private static final int MAX_SCREENSHOT_RETRIES = 3;
    static final boolean NOTIFY_POPUP = false;
    public static final String PREBOOT_IPO = "android.intent.action.ACTION_PREBOOT_IPO";
    static boolean PROFILE_ORIENTATION = false;
    private static final String PROPERTY_BUILD_DATE_UTC = "ro.build.date.utc";
    private static final String PROPERTY_EMULATOR_CIRCULAR = "ro.emulator.circular";
    private static final String RECENT_ACTVITIY_NAME = "com.coloros.recents/.RecentsActivity";
    static final int SEAMLESS_ROTATION_TIMEOUT_DURATION = 2000;
    private static final String SIZE_OVERRIDE = "ro.config.size_override";
    private static final String SYSTEM_DEBUGGABLE = "ro.debuggable";
    private static final String SYSTEM_SECURE = "ro.secure";
    static final String TAG = null;
    private static final String TICKER_PANEL = "TickerPanel";
    private static final int TRANSITION_ANIMATION_SCALE = 1;
    static final int TYPE_LAYER_MULTIPLIER = 10000;
    static final int TYPE_LAYER_OFFSET = 1000;
    static final int UPDATE_FOCUS_NORMAL = 0;
    static final int UPDATE_FOCUS_PLACING_SURFACES = 2;
    static final int UPDATE_FOCUS_WILL_ASSIGN_LAYERS = 1;
    static final int UPDATE_FOCUS_WILL_PLACE_SURFACES = 3;
    static final int WINDOWS_FREEZING_SCREENS_ACTIVE = 1;
    static final int WINDOWS_FREEZING_SCREENS_NONE = 0;
    static final int WINDOWS_FREEZING_SCREENS_TIMEOUT = 2;
    private static final int WINDOW_ANIMATION_SCALE = 0;
    static final int WINDOW_FREEZE_TIMEOUT_DURATION = 2000;
    static final int WINDOW_LAYER_MULTIPLIER = 5;
    static final int WINDOW_REPLACEMENT_TIMEOUT_DURATION = 2000;
    static boolean localLOGV;
    static boolean mEnableSaveSurface;
    final int MAGNIFICATION_DISAPPEAR_CNT_LIMIT;
    public boolean blockSurfaceFlinger;
    boolean dismissDockedStackFromHome;
    AccessibilityController mAccessibilityController;
    private ActivityChangedListener mActivityChangedListener;
    final IActivityManager mActivityManager;
    private final AppTransitionListener mActivityManagerAppTransitionNotifier;
    final boolean mAllowAnimationsInLowPowerMode;
    final boolean mAllowBootMessages;
    boolean mAllowTheaterModeWakeFromLayout;
    boolean mAltOrientation;
    final ActivityManagerInternal mAmInternal;
    boolean mAnimateWallpaperWithTarget;
    boolean mAnimationScheduled;
    boolean mAnimationsDisabled;
    final WindowAnimator mAnimator;
    float mAnimatorDurationScaleSetting;
    final AppOpsManager mAppOps;
    final AppTransition mAppTransition;
    int mAppsFreezingScreen;
    private final LruCache<String, Bitmap> mBitmaps;
    boolean mBootAnimationStopped;
    private final BoundsAnimationController mBoundsAnimationController;
    final BroadcastReceiver mBroadcastReceiver;
    private final int mCacheBehavior;
    private final ArrayList<Integer> mChangedStackList;
    private HashSet<String> mCheckedFloatWindowSet;
    final Choreographer mChoreographer;
    CircularDisplayMask mCircularDisplayMask;
    boolean mClientFreezingScreen;
    final ArraySet<AppWindowToken> mClosingApps;
    ColorNavigationBarUtil mColorNavigationBarUtil;
    final DisplayMetrics mCompatDisplayMetrics;
    float mCompatibleScreenScale;
    final Context mContext;
    Configuration mCurConfiguration;
    WindowState mCurrentFocus;
    int[] mCurrentProfileIds;
    int mCurrentUserId;
    int mDeferredRotationPauseCount;
    final ArrayList<WindowState> mDestroyPreservedSurface;
    final ArrayList<WindowState> mDestroySurface;
    private final boolean mDisableFastStartingWindow;
    private final Runnable mDisableFrozenBySecurityTimeoutRunnable;
    boolean mDisableFrozenBySecuritypermission;
    private boolean mDisableStatusBar;
    SparseArray<DisplayContent> mDisplayContents;
    boolean mDisplayEnabled;
    long mDisplayFreezeTime;
    boolean mDisplayFrozen;
    boolean mDisplayMagnificationEnabled;
    final DisplayManager mDisplayManager;
    final DisplayManagerInternal mDisplayManagerInternal;
    final DisplayMetrics mDisplayMetrics;
    boolean mDisplayReady;
    final DisplaySettings mDisplaySettings;
    final Display[] mDisplays;
    Runnable mDockedForDrawnCallback;
    Rect mDockedStackCreateBounds;
    int mDockedStackCreateMode;
    DragState mDragState;
    final long mDrawLockTimeoutMillis;
    EmulatorDisplayOverlay mEmulatorDisplayOverlay;
    int mEnterAnimId;
    private boolean mEventDispatchingEnabled;
    int mExitAnimId;
    private final boolean mFastStartingWindowSupport;
    final ArrayList<AppWindowToken> mFinishedEarlyAnim;
    final ArrayList<AppWindowToken> mFinishedStarting;
    boolean mFocusMayChange;
    AppWindowToken mFocusedApp;
    boolean mForceDisplayEnabled;
    final ArrayList<WindowState> mForceRemoves;
    boolean mForceResizableTasks;
    private final RemoteCallbackList<IFreeformStackListener> mFreeformStackListeners;
    private boolean mFreeingChange;
    final SurfaceSession mFxSession;
    final H mH;
    private final HandlerFloatWindow mHandlerFloatWindow;
    boolean mHardKeyboardAvailable;
    OnHardKeyboardStatusChangeListener mHardKeyboardStatusChangeListener;
    final boolean mHasPermanentDpad;
    boolean mHasReceiveIPO;
    final boolean mHaveInputMethods;
    private Runnable mHideKeyguardTimeoutRunnable;
    private ArrayList<WindowState> mHidingNonSystemOverlayWindows;
    Session mHoldingScreenOn;
    WakeLock mHoldingScreenWakeLock;
    private Hypnus mHyp;
    boolean mInTouchMode;
    InputConsumerImpl mInputConsumer;
    final InputManagerService mInputManager;
    final ArrayList<WindowState> mInputMethodDialogs;
    IInputMethodManager mInputMethodManager;
    WindowState mInputMethodTarget;
    boolean mInputMethodTargetWaitingAnim;
    WindowState mInputMethodWindow;
    final InputMonitor mInputMonitor;
    int mIpoRotation;
    private boolean mIsAlarmBooting;
    private boolean mIsKeyguardWindowHide;
    private boolean mIsPerfBoostEnable;
    private boolean mIsRestoreButtonVisible;
    boolean mIsTouchDevice;
    private boolean mIsUpdateAlarmBootRotation;
    boolean mIsUpdateIpoRotation;
    WindowState mKeyguard;
    private final KeyguardDisableHandler mKeyguardDisableHandler;
    private boolean mKeyguardWaitingForActivityDrawn;
    String mLastANRState;
    int mLastDispatchedSystemUiVisibility;
    int mLastDisplayFreezeDuration;
    Object mLastFinishedFreezeSource;
    WindowState mLastFocus;
    int mLastKeyguardForcedOrientation;
    int mLastOrientation;
    int mLastStatusBarVisibility;
    WindowState mLastWakeLockHoldingWindow;
    WindowState mLastWakeLockObscuringWindow;
    int mLastWindowForcedOrientation;
    final WindowLayersController mLayersController;
    int mLayoutSeq;
    final boolean mLimitedAlphaCompositing;
    ArrayList<WindowState> mLosingFocus;
    int mMagnificationBorderDisappearCnt;
    private MousePositionTracker mMousePositionTracker;
    final List<IBinder> mNoAnimationNotifyOnTransitionFinished;
    final boolean mOnlyCore;
    final ArraySet<AppWindowToken> mOpeningApps;
    private final boolean mOppoEnableFastStartingWin;
    final ArrayList<WindowState> mPendingRemove;
    WindowState[] mPendingRemoveTmp;
    private IPerfServiceWrapper mPerfService;
    private final PointerEventDispatcher mPointerEventDispatcher;
    final WindowManagerPolicy mPolicy;
    PowerManager mPowerManager;
    PowerManagerInternal mPowerManagerInternal;
    final DisplayMetrics mRealDisplayMetrics;
    WindowState[] mRebuildTmp;
    private final DisplayContentList mReconfigureOnConfigurationChanged;
    final ArrayList<AppWindowToken> mReplacingWindowTimeouts;
    final ArrayList<WindowState> mResizingWindows;
    int mRotation;
    private boolean mRotationChanged;
    ArrayList<RotationWatcher> mRotationWatchers;
    boolean mSafeMode;
    SparseArray<Boolean> mScreenCaptureDisabled;
    private final WakeLock mScreenFrozenLock;
    final Rect mScreenRect;
    int mSeamlessRotationCount;
    final ArraySet<Session> mSessions;
    SettingsObserver mSettingsObserver;
    boolean mShowingBootMessages;
    private boolean mSimulateWindowFreezing;
    boolean mSkipAppTransitionAnimation;
    protected boolean mSplitFormBack;
    SparseArray<TaskStack> mStackIdToStack;
    int mStatusBarHeight;
    StrictModeFlash mStrictModeFlash;
    boolean mSystemBooted;
    int mSystemDecorLayer;
    boolean mSystemReady;
    SparseArray<Task> mTaskIdToTask;
    TaskPositioner mTaskPositioner;
    final Configuration mTempConfiguration;
    private WindowContentFrameStats mTempWindowRenderStats;
    private final HandlerThread mThreadFloatWindow;
    final DisplayMetrics mTmpDisplayMetrics;
    final float[] mTmpFloats;
    final Rect mTmpRect;
    final Rect mTmpRect2;
    final Rect mTmpRect3;
    final RectF mTmpRectF;
    private final SparseIntArray mTmpTaskIds;
    final Matrix mTmpTransform;
    final ArrayList<WindowState> mTmpWindows;
    final HashMap<IBinder, WindowToken> mTokenMap;
    int mTransactionSequence;
    float mTransitionAnimationScaleSetting;
    boolean mTurnOnScreen;
    private ViewServer mViewServer;
    boolean mWaitingForConfig;
    ArrayList<WindowState> mWaitingForDrawn;
    Runnable mWaitingForDrawnCallback;
    WallpaperController mWallpaperControllerLocked;
    InputConsumerImpl mWallpaperInputConsumer;
    Watermark mWatermark;
    WindowState mWinRequestTurnScreenOnWhenRelayout;
    float mWindowAnimationScaleSetting;
    final ArrayList<WindowChangeListener> mWindowChangeListeners;
    final HashMap<IBinder, WindowState> mWindowMap;
    final WindowSurfacePlacer mWindowPlacerLocked;
    boolean mWindowsChanged;
    int mWindowsFreezingScreen;

    public interface WindowChangeListener {
        void focusChanged();

        void windowsChanged();
    }

    final class DragInputEventReceiver extends InputEventReceiver {
        private boolean mIsStartEvent = true;
        private boolean mStylusButtonDownAtStart;

        public DragInputEventReceiver(InputChannel inputChannel, Looper looper) {
            super(inputChannel, looper);
        }

        /* JADX WARNING: Removed duplicated region for block: B:19:0x003e A:{Catch:{ Exception -> 0x0073, all -> 0x0095 }} */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onInputEvent(InputEvent event) {
            boolean handled = false;
            try {
                if (!(!(event instanceof MotionEvent) || (event.getSource() & 2) == 0 || WindowManagerService.this.mDragState == null)) {
                    MotionEvent motionEvent = (MotionEvent) event;
                    boolean endDrag = false;
                    float newX = motionEvent.getRawX();
                    float newY = motionEvent.getRawY();
                    boolean isStylusButtonDown = (motionEvent.getButtonState() & 32) != 0;
                    if (this.mIsStartEvent) {
                        if (isStylusButtonDown) {
                            this.mStylusButtonDownAtStart = true;
                        }
                        this.mIsStartEvent = false;
                    }
                    switch (motionEvent.getAction()) {
                        case 0:
                            if (WindowManagerDebugConfig.DEBUG_DRAG) {
                                Slog.w("WindowManager", "Unexpected ACTION_DOWN in drag layer");
                            }
                        case 1:
                            if (WindowManagerDebugConfig.DEBUG_DRAG) {
                                Slog.d("WindowManager", "Got UP on move channel; dropping at " + newX + "," + newY);
                            }
                            synchronized (WindowManagerService.this.mWindowMap) {
                                endDrag = WindowManagerService.this.mDragState.notifyDropLw(newX, newY);
                            }
                        case 2:
                            if (!this.mStylusButtonDownAtStart || isStylusButtonDown) {
                                synchronized (WindowManagerService.this.mWindowMap) {
                                    WindowManagerService.this.mDragState.notifyMoveLw(newX, newY);
                                }
                            } else {
                                if (WindowManagerDebugConfig.DEBUG_DRAG) {
                                    Slog.d("WindowManager", "Button no longer pressed; dropping at " + newX + "," + newY);
                                }
                                synchronized (WindowManagerService.this.mWindowMap) {
                                    endDrag = WindowManagerService.this.mDragState.notifyDropLw(newX, newY);
                                }
                            }
                            break;
                        case 3:
                            if (WindowManagerDebugConfig.DEBUG_DRAG) {
                                Slog.d("WindowManager", "Drag cancelled!");
                            }
                            endDrag = true;
                            if (endDrag) {
                                if (WindowManagerDebugConfig.DEBUG_DRAG) {
                                    Slog.d("WindowManager", "Drag ended; tearing down state");
                                }
                                synchronized (WindowManagerService.this.mWindowMap) {
                                    WindowManagerService.this.mDragState.endDragLw();
                                }
                                this.mStylusButtonDownAtStart = false;
                                this.mIsStartEvent = true;
                            }
                            handled = true;
                            break;
                    }
                    if (endDrag) {
                    }
                    handled = true;
                }
                finishInputEvent(event, handled);
            } catch (Exception e) {
                Slog.e("WindowManager", "Exception caught by drag handleMotion", e);
                finishInputEvent(event, false);
            } catch (Throwable th) {
                finishInputEvent(event, false);
            }
        }
    }

    final class H extends Handler {
        public static final int ADD_STARTING = 5;
        public static final int ALL_WINDOWS_DRAWN = 33;
        public static final int APP_FREEZE_TIMEOUT = 17;
        public static final int APP_TRANSITION_ANIMATION_SPECS_FUTURE_TIMEOUT = 1002;
        public static final int APP_TRANSITION_TIMEOUT = 13;
        public static final int BLOCK_SURFACE_FLINGER = 1000;
        public static final int BOOT_TIMEOUT = 23;
        public static final int CACHE_STARTING_WINDOW = 56;
        public static final int CHECK_IF_BOOT_ANIMATION_FINISHED = 37;
        public static final int CLIENT_FREEZE_TIMEOUT = 30;
        public static final int DO_ANIMATION_CALLBACK = 26;
        public static final int DO_DISPLAY_ADDED = 27;
        public static final int DO_DISPLAY_CHANGED = 29;
        public static final int DO_DISPLAY_REMOVED = 28;
        public static final int DO_TRAVERSAL = 4;
        public static final int DRAG_END_TIMEOUT = 21;
        public static final int DRAG_START_TIMEOUT = 20;
        public static final int ENABLE_SCREEN = 16;
        public static final int FINISHED_STARTING = 7;
        public static final int FINISH_TASK_POSITIONING = 40;
        public static final int FORCE_GC = 15;
        public static final int NEW_ANIMATOR_SCALE = 34;
        public static final int NOTIFY_ACTIVITY_DRAWN = 32;
        public static final int NOTIFY_APP_TRANSITION_CANCELLED = 48;
        public static final int NOTIFY_APP_TRANSITION_FINISHED = 49;
        public static final int NOTIFY_APP_TRANSITION_STARTING = 47;
        public static final int NOTIFY_DOCKED_STACK_MINIMIZED_CHANGED = 53;
        public static final int NOTIFY_STARTING_WINDOW_DRAWN = 50;
        public static final int PERSIST_ANIMATION_SCALE = 14;
        public static final int REMOVE_STARTING = 6;
        public static final int REPORT_APPLICATION_TOKEN_DRAWN = 9;
        public static final int REPORT_APPLICATION_TOKEN_WINDOWS = 8;
        public static final int REPORT_FOCUS_CHANGE = 2;
        public static final int REPORT_HARD_KEYBOARD_STATUS_CHANGE = 22;
        public static final int REPORT_LOSING_FOCUS = 3;
        public static final int REPORT_WINDOWS_CHANGE = 19;
        public static final int RESET_ANR_MESSAGE = 38;
        public static final int RESIZE_STACK = 42;
        public static final int RESIZE_TASK = 43;
        public static final int SEAMLESS_ROTATION_TIMEOUT = 54;
        public static final int SEND_NEW_CONFIGURATION = 18;
        public static final int SHOW_CIRCULAR_DISPLAY_MASK = 35;
        public static final int SHOW_EMULATOR_DISPLAY_OVERLAY = 36;
        public static final int SHOW_STRICT_MODE_VIOLATION = 25;
        public static final int TAP_OUTSIDE_TASK = 31;
        public static final int TWO_FINGER_SCROLL_START = 44;
        public static final int UNBLOCK_SURFACE_FLINGER = 1001;
        public static final int UNUSED = 0;
        public static final int UPDATE_ANIMATION_SCALE = 51;
        public static final int UPDATE_DOCKED_STACK_DIVIDER = 41;
        public static final int UPDATE_IPO_ROTATION = 55;
        public static final int WAITING_FOR_DRAWN_TIMEOUT = 24;
        public static final int WALLPAPER_DRAW_PENDING_TIMEOUT = 39;
        public static final int WINDOW_FREEZE_TIMEOUT = 11;
        public static final int WINDOW_HIDE_TIMEOUT = 52;
        public static final int WINDOW_REPLACEMENT_TIMEOUT = 46;

        H() {
        }

        /* JADX WARNING: Removed duplicated region for block: B:99:0x0272  */
        /* JADX WARNING: Removed duplicated region for block: B:99:0x0272  */
        /* JADX WARNING: Removed duplicated region for block: B:7:0x002d  */
        /* JADX WARNING: Missing block: B:31:0x00b3, code:
            if (r15 == null) goto L_0x00b8;
     */
        /* JADX WARNING: Missing block: B:32:0x00b5, code:
            r15.onWindowFocusChangedNotLocked();
     */
        /* JADX WARNING: Missing block: B:33:0x00b8, code:
            if (r31 == null) goto L_0x0102;
     */
        /* JADX WARNING: Missing block: B:35:0x00bc, code:
            if (com.android.server.wm.WindowManagerDebugConfig.DEBUG_FOCUS == false) goto L_0x00da;
     */
        /* JADX WARNING: Missing block: B:36:0x00be, code:
            android.util.Slog.i("WindowManager", "Gaining focus: " + r31);
     */
        /* JADX WARNING: Missing block: B:38:0x00de, code:
            if (com.mediatek.multiwindow.MultiWindowManager.isSupported() == false) goto L_0x00ef;
     */
        /* JADX WARNING: Missing block: B:40:0x00e4, code:
            if (r31.getTask() == null) goto L_0x00ef;
     */
        /* JADX WARNING: Missing block: B:41:0x00e6, code:
            r59.this$0.showOrHideRestoreButton(r31);
     */
        /* JADX WARNING: Missing block: B:42:0x00ef, code:
            r31.reportFocusChangedSerialized(true, r59.this$0.mInTouchMode);
            com.android.server.wm.WindowManagerService.-wrap9(r59.this$0);
     */
        /* JADX WARNING: Missing block: B:43:0x0102, code:
            if (r27 == null) goto L_0x0029;
     */
        /* JADX WARNING: Missing block: B:45:0x0106, code:
            if (com.android.server.wm.WindowManagerDebugConfig.DEBUG_FOCUS == false) goto L_0x0124;
     */
        /* JADX WARNING: Missing block: B:46:0x0108, code:
            android.util.Slog.i("WindowManager", "Losing focus: " + r27);
     */
        /* JADX WARNING: Missing block: B:47:0x0124, code:
            r27.reportFocusChangedSerialized(false, r59.this$0.mInTouchMode);
     */
        /* JADX WARNING: Missing block: B:285:0x083d, code:
            java.lang.Runtime.getRuntime().gc();
     */
        /* JADX WARNING: Missing block: B:415:0x0ba0, code:
            r39 = r59.this$0.getCurrentAnimatorScale();
            android.animation.ValueAnimator.setDurationScale(r39);
            r41 = r60.obj;
     */
        /* JADX WARNING: Missing block: B:416:0x0bb3, code:
            if (r41 == null) goto L_0x0c4d;
     */
        /* JADX WARNING: Missing block: B:417:0x0bb5, code:
            r36 = android.os.Process.getProcessState(r41.mPid);
     */
        /* JADX WARNING: Missing block: B:418:0x0bc0, code:
            if (3 == r36) goto L_0x0c1a;
     */
        /* JADX WARNING: Missing block: B:420:0x0bc5, code:
            if (2 == r36) goto L_0x0c17;
     */
        /* JADX WARNING: Missing block: B:421:0x0bc7, code:
            r26 = true;
     */
        /* JADX WARNING: Missing block: B:422:0x0bc9, code:
            if (r26 != false) goto L_0x0c1d;
     */
        /* JADX WARNING: Missing block: B:424:?, code:
            r41.mCallback.onAnimatorScaleChanged(r39);
     */
        /* JADX WARNING: Missing block: B:426:0x0bd7, code:
            android.util.Slog.e(com.android.server.wm.WindowManagerService.TAG, "call onAnimatorScaleChanged failed 01. session.uid=" + r41.mUid + ", session.mPid=" + r41.mPid + ", session.name=" + r41.mStringName);
     */
        /* JADX WARNING: Missing block: B:430:0x0c17, code:
            r26 = false;
     */
        /* JADX WARNING: Missing block: B:431:0x0c1a, code:
            r26 = false;
     */
        /* JADX WARNING: Missing block: B:433:0x0c1f, code:
            if (com.android.server.wm.WindowManagerDebugConfig.DEBUG_BINDER == false) goto L_0x0029;
     */
        /* JADX WARNING: Missing block: B:434:0x0c21, code:
            android.util.Slog.i(com.android.server.wm.WindowManagerService.TAG, "process " + r41.mPid + " dead or suspend, ignore onAnimatorScaleChanged 01 call. " + r36);
     */
        /* JADX WARNING: Missing block: B:435:0x0c4d, code:
            r19 = new java.util.ArrayList();
            r37 = new java.util.ArrayList();
            r3 = r59.this$0.mWindowMap;
     */
        /* JADX WARNING: Missing block: B:436:0x0c5d, code:
            monitor-enter(r3);
     */
        /* JADX WARNING: Missing block: B:437:0x0c5e, code:
            r25 = 0;
     */
        /* JADX WARNING: Missing block: B:440:0x0c6c, code:
            if (r25 >= r59.this$0.mSessions.size()) goto L_0x0ca8;
     */
        /* JADX WARNING: Missing block: B:441:0x0c6e, code:
            r47 = (com.android.server.wm.Session) r59.this$0.mSessions.valueAt(r25);
            r19.add(r47.mCallback);
            r37.add(new com.android.server.wm.WindowManagerService.ProcessStates(r59.this$0, android.os.Process.getProcessState(r47.mPid), r47.mPid, r47.mUid));
     */
        /* JADX WARNING: Missing block: B:442:0x0ca5, code:
            r25 = r25 + 1;
     */
        /* JADX WARNING: Missing block: B:443:0x0ca8, code:
            monitor-exit(r3);
     */
        /* JADX WARNING: Missing block: B:444:0x0ca9, code:
            r25 = 0;
     */
        /* JADX WARNING: Missing block: B:446:0x0cb1, code:
            if (r25 >= r19.size()) goto L_0x0029;
     */
        /* JADX WARNING: Missing block: B:448:0x0cc1, code:
            if (((com.android.server.wm.WindowManagerService.ProcessStates) r37.get(r25)).mIgnoreCall != false) goto L_0x0d16;
     */
        /* JADX WARNING: Missing block: B:450:?, code:
            ((android.view.IWindowSessionCallback) r19.get(r25)).onAnimatorScaleChanged(r39);
     */
        /* JADX WARNING: Missing block: B:456:0x0cd9, code:
            android.util.Slog.e(com.android.server.wm.WindowManagerService.TAG, "call onAnimatorScaleChanged failed 02. session.uid=" + ((com.android.server.wm.WindowManagerService.ProcessStates) r37.get(r25)).mUid + ", session.mPid=" + ((com.android.server.wm.WindowManagerService.ProcessStates) r37.get(r25)).mPid);
     */
        /* JADX WARNING: Missing block: B:458:0x0d18, code:
            if (com.android.server.wm.WindowManagerDebugConfig.DEBUG_BINDER == false) goto L_0x0cd2;
     */
        /* JADX WARNING: Missing block: B:459:0x0d1a, code:
            android.util.Slog.i(com.android.server.wm.WindowManagerService.TAG, "process " + ((com.android.server.wm.WindowManagerService.ProcessStates) r37.get(r25)).mPid + " ignore onAnimatorScaleChanged 02 call. procState:" + ((com.android.server.wm.WindowManagerService.ProcessStates) r37.get(r25)).mProcState);
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void handleMessage(Message msg) {
            int i;
            if (WindowManagerDebugConfig.DEBUG_WINDOW_TRACE) {
                Slog.v("WindowManager", "handleMessage: entry what=" + msg.what);
            }
            int N;
            HashMap hashMap;
            AppWindowToken wtoken;
            View view;
            IBinder token;
            WindowList windows;
            WindowState w;
            IBinder win;
            Runnable callback;
            Runnable dockedCallback;
            switch (msg.what) {
                case 2:
                    AccessibilityController accessibilityController = null;
                    synchronized (WindowManagerService.this.mWindowMap) {
                        if (WindowManagerService.this.mAccessibilityController != null && WindowManagerService.this.getDefaultDisplayContentLocked().getDisplayId() == 0) {
                            accessibilityController = WindowManagerService.this.mAccessibilityController;
                        }
                        WindowState lastFocus = WindowManagerService.this.mLastFocus;
                        WindowState newFocus = WindowManagerService.this.mCurrentFocus;
                        if (lastFocus != newFocus) {
                            WindowManagerService.this.mLastFocus = newFocus;
                            if (WindowManagerDebugConfig.DEBUG_FOCUS) {
                                Slog.i("WindowManager", "Focus moving from " + lastFocus + " to " + newFocus);
                            }
                            if (!(newFocus == null || lastFocus == null || newFocus.isDisplayedLw())) {
                                if (WindowManagerDebugConfig.DEBUG_FOCUS) {
                                    Slog.i("WindowManager", "Delaying loss of focus...");
                                }
                                WindowManagerService.this.mLosingFocus.add(lastFocus);
                                lastFocus = null;
                                break;
                            }
                        }
                        return;
                    }
                case 3:
                    ArrayList<WindowState> losers;
                    synchronized (WindowManagerService.this.mWindowMap) {
                        losers = WindowManagerService.this.mLosingFocus;
                        WindowManagerService.this.mLosingFocus = new ArrayList();
                    }
                    N = losers.size();
                    for (i = 0; i < N; i++) {
                        if (WindowManagerDebugConfig.DEBUG_FOCUS_LIGHT) {
                            Slog.i("WindowManager", "Losing delayed focus: " + losers.get(i));
                        }
                        ((WindowState) losers.get(i)).reportFocusChangedSerialized(false, WindowManagerService.this.mInTouchMode);
                    }
                    break;
                case 4:
                    hashMap = WindowManagerService.this.mWindowMap;
                    synchronized (hashMap) {
                        WindowManagerService.this.mWindowPlacerLocked.performSurfacePlacement();
                        break;
                    }
                case 5:
                    wtoken = msg.obj;
                    StartingData sd = wtoken.startingData;
                    if (sd != null) {
                        Configuration overrideConfig;
                        if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
                            Slog.v("WindowManager", "Add starting " + wtoken + ": pkg=" + sd.pkg);
                        }
                        Trace.traceBegin(4128, "wmAddStarting");
                        view = null;
                        if (wtoken != null) {
                            try {
                                if (wtoken.mTask != null) {
                                    overrideConfig = wtoken.mTask.mOverrideConfig;
                                    if (WindowManagerService.this.isFastStartingWindowSupport() || !WindowManagerService.this.hasBitmapByToken(wtoken.token)) {
                                        view = WindowManagerService.this.mPolicy.addStartingWindow(wtoken.token, sd.pkg, sd.theme, sd.compatInfo, sd.nonLocalizedLabel, sd.labelRes, sd.icon, sd.logo, sd.windowFlags, overrideConfig);
                                        Trace.traceEnd(4128);
                                        if (view != null) {
                                            boolean abort = false;
                                            synchronized (WindowManagerService.this.mWindowMap) {
                                                if (wtoken.removed || wtoken.startingData == null) {
                                                    if (wtoken.startingWindow != null) {
                                                        if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
                                                            Slog.v("WindowManager", "Aborted starting " + wtoken + ": removed=" + wtoken.removed + " startingData=" + wtoken.startingData);
                                                        }
                                                        wtoken.startingWindow = null;
                                                        wtoken.startingData = null;
                                                    }
                                                    abort = true;
                                                } else {
                                                    wtoken.startingView = view;
                                                }
                                                if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW && !abort) {
                                                    Slog.v("WindowManager", "Added starting " + wtoken + ": startingWindow=" + wtoken.startingWindow + " startingView=" + wtoken.startingView);
                                                }
                                            }
                                            if (abort) {
                                                Trace.traceBegin(32, "wmAbortStarting");
                                                try {
                                                    WindowManagerService.this.mPolicy.removeStartingWindow(wtoken.token, view);
                                                } catch (Throwable e) {
                                                    Slog.w("WindowManager", "Exception when removing starting window", e);
                                                }
                                                Trace.traceEnd(32);
                                                break;
                                            }
                                        }
                                    }
                                    view = WindowManagerService.this.mPolicy.addFastStartingWindow(wtoken.token, sd.pkg, sd.theme, sd.compatInfo, sd.nonLocalizedLabel, sd.labelRes, sd.icon, sd.logo, sd.windowFlags, null);
                                    Trace.traceEnd(4128);
                                    if (view != null) {
                                    }
                                }
                            } catch (Throwable e2) {
                                Slog.w("WindowManager", "Exception when adding starting window", e2);
                            }
                        }
                        overrideConfig = null;
                        if (WindowManagerService.this.isFastStartingWindowSupport()) {
                            break;
                        }
                        view = WindowManagerService.this.mPolicy.addStartingWindow(wtoken.token, sd.pkg, sd.theme, sd.compatInfo, sd.nonLocalizedLabel, sd.labelRes, sd.icon, sd.logo, sd.windowFlags, overrideConfig);
                        Trace.traceEnd(4128);
                        if (view != null) {
                        }
                    } else {
                        return;
                    }
                    break;
                case 6:
                    wtoken = (AppWindowToken) msg.obj;
                    token = null;
                    view = null;
                    synchronized (WindowManagerService.this.mWindowMap) {
                        if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
                            Slog.v("WindowManager", "Remove starting " + wtoken + ": startingWindow=" + wtoken.startingWindow + " startingView=" + wtoken.startingView);
                        }
                        if (wtoken.startingWindow != null) {
                            view = wtoken.startingView;
                            token = wtoken.token;
                            wtoken.startingData = null;
                            wtoken.startingView = null;
                            wtoken.startingWindow = null;
                            wtoken.startingDisplayed = false;
                        }
                    }
                    if (view != null) {
                        Trace.traceBegin(32, "wmRemoveStarting");
                        try {
                            WindowManagerService.this.mPolicy.removeStartingWindow(token, view);
                        } catch (Throwable e22) {
                            Slog.w("WindowManager", "Exception when removing starting window", e22);
                        }
                        Trace.traceEnd(32);
                        break;
                    }
                    break;
                case 7:
                    while (true) {
                        hashMap = WindowManagerService.this.mWindowMap;
                        synchronized (hashMap) {
                            N = WindowManagerService.this.mFinishedStarting.size();
                            if (N <= 0) {
                                break;
                            }
                            wtoken = (AppWindowToken) WindowManagerService.this.mFinishedStarting.remove(N - 1);
                            if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
                                Slog.v("WindowManager", "Finished starting " + wtoken + ": startingWindow=" + wtoken.startingWindow + " startingView=" + wtoken.startingView);
                            }
                            if (wtoken.startingWindow == null) {
                            } else {
                                view = wtoken.startingView;
                                token = wtoken.token;
                                wtoken.startingData = null;
                                wtoken.startingView = null;
                                wtoken.startingWindow = null;
                                wtoken.startingDisplayed = false;
                                Trace.traceBegin(4128, "wmFinishStarting");
                                try {
                                    WindowManagerService.this.mPolicy.removeStartingWindow(token, view);
                                } catch (Throwable e222) {
                                    Slog.w("WindowManager", "Exception when removing starting window", e222);
                                }
                                Trace.traceEnd(4128);
                            }
                        }
                    }
                case 8:
                    wtoken = (AppWindowToken) msg.obj;
                    boolean nowVisible = msg.arg1 != 0;
                    boolean nowGone = msg.arg2 != 0;
                    try {
                        if (WindowManagerDebugConfig.DEBUG_VISIBILITY) {
                            Slog.v("WindowManager", "Reporting visible in " + wtoken + " visible=" + nowVisible + " gone=" + nowGone);
                        }
                        if (!nowVisible) {
                            wtoken.appToken.windowsGone();
                            break;
                        } else {
                            wtoken.appToken.windowsVisible();
                            break;
                        }
                    } catch (RemoteException e3) {
                        break;
                    }
                case 9:
                    wtoken = (AppWindowToken) msg.obj;
                    try {
                        if (WindowManagerDebugConfig.DEBUG_VISIBILITY) {
                            Slog.v("WindowManager", "Reporting drawn in " + wtoken);
                        }
                        wtoken.appToken.windowsDrawn();
                        break;
                    } catch (RemoteException e4) {
                        break;
                    }
                case 11:
                    hashMap = WindowManagerService.this.mWindowMap;
                    synchronized (hashMap) {
                        Slog.w("WindowManager", "Window freeze timeout expired.");
                        if (!WindowManagerService.this.mSimulateWindowFreezing) {
                            WindowManagerService.this.mWindowsFreezingScreen = 2;
                        }
                        windows = WindowManagerService.this.getDefaultWindowListLocked();
                        i = windows.size();
                        while (i > 0) {
                            i--;
                            w = (WindowState) windows.get(i);
                            if (w.mOrientationChanging) {
                                w.mOrientationChanging = false;
                                w.mLastFreezeDuration = (int) (SystemClock.elapsedRealtime() - WindowManagerService.this.mDisplayFreezeTime);
                                Slog.w("WindowManager", "Force clearing orientation change: " + w);
                            }
                        }
                        WindowManagerService.this.mWindowPlacerLocked.performSurfacePlacement();
                        String pkg = WindowManagerService.this.getForegroundPackage();
                        synchronized (WindowManagerService.this.mWindowMap) {
                            if (WindowManagerService.this.mDisplayFrozen) {
                                WindowManagerService.this.mInputMonitor.thawInputDispatchingLw();
                                if (pkg == null || pkg.length() > 91) {
                                    Slog.v(WindowManagerService.TAG, "set sys.app_freeze_timeout: getForegroundPackage failed!!!");
                                } else {
                                    SystemProperties.set(WindowManagerService.APP_FROZEN_TIMEOUT_PROP, pkg);
                                    Slog.v(WindowManagerService.TAG, "set sys.app_freeze_timeout: pkg=" + pkg);
                                }
                            }
                        }
                        try {
                            ActivityManagerNative.getDefault().reportJunkFromApp("WINDOW_FREEZE_TIMEOUT", pkg, "Window freeze timeout expired.", false);
                            break;
                        } catch (RemoteException e5) {
                            Slog.e(WindowManagerService.TAG, e5.toString());
                            break;
                        }
                    }
                case 13:
                    hashMap = WindowManagerService.this.mWindowMap;
                    synchronized (hashMap) {
                        if (!(!WindowManagerService.this.mAppTransition.isTransitionSet() && WindowManagerService.this.mOpeningApps.isEmpty() && WindowManagerService.this.mClosingApps.isEmpty())) {
                            Slog.v("WindowManager", "*** APP TRANSITION TIMEOUT. isTransitionSet()=" + WindowManagerService.this.mAppTransition.isTransitionSet() + " mOpeningApps.size()=" + WindowManagerService.this.mOpeningApps.size() + " mClosingApps.size()=" + WindowManagerService.this.mClosingApps.size());
                            WindowManagerService.this.mAppTransition.setTimeout();
                            Writer sw = new StringWriter();
                            PrintWriter fastPrintWriter = new FastPrintWriter(sw, false, 128);
                            WindowManagerService.this.mAppTransition.dump(fastPrintWriter, IElsaManager.EMPTY_PACKAGE);
                            try {
                                ActivityManagerNative.getDefault().reportJunkFromApp("APP_TRANSITION_TIMEOUT", "N/A", sw.toString(), false);
                            } catch (RemoteException e52) {
                                Slog.e(WindowManagerService.TAG, e52.toString());
                            }
                            fastPrintWriter.close();
                            WindowManagerService.this.mWindowPlacerLocked.performSurfacePlacement();
                            break;
                        }
                    }
                case 14:
                    Global.putFloat(WindowManagerService.this.mContext.getContentResolver(), "window_animation_scale", WindowManagerService.this.mWindowAnimationScaleSetting);
                    Global.putFloat(WindowManagerService.this.mContext.getContentResolver(), "transition_animation_scale", WindowManagerService.this.mTransitionAnimationScaleSetting);
                    Global.putFloat(WindowManagerService.this.mContext.getContentResolver(), "animator_duration_scale", WindowManagerService.this.mAnimatorDurationScaleSetting);
                    break;
                case 15:
                    synchronized (WindowManagerService.this.mWindowMap) {
                        if (WindowManagerService.this.mAnimator.isAnimating() || WindowManagerService.this.mAnimationScheduled) {
                            sendEmptyMessageDelayed(15, 2000);
                            return;
                        } else if (WindowManagerService.this.mDisplayFrozen) {
                            return;
                        }
                    }
                    break;
                case 16:
                    WindowManagerService.this.performEnableScreen();
                    break;
                case 17:
                    hashMap = WindowManagerService.this.mWindowMap;
                    synchronized (hashMap) {
                        Slog.w("WindowManager", "App freeze timeout expired.");
                        if (!WindowManagerService.this.mSimulateWindowFreezing) {
                            WindowManagerService.this.mWindowsFreezingScreen = 2;
                        }
                        int numStacks = WindowManagerService.this.mStackIdToStack.size();
                        for (int stackNdx = 0; stackNdx < numStacks; stackNdx++) {
                            ArrayList<Task> tasks = ((TaskStack) WindowManagerService.this.mStackIdToStack.valueAt(stackNdx)).getTasks();
                            for (int taskNdx = tasks.size() - 1; taskNdx >= 0; taskNdx--) {
                                AppTokenList tokens = ((Task) tasks.get(taskNdx)).mAppTokens;
                                for (int tokenNdx = tokens.size() - 1; tokenNdx >= 0; tokenNdx--) {
                                    AppWindowToken tok = (AppWindowToken) tokens.get(tokenNdx);
                                    if (tok.mAppAnimator.freezingScreen) {
                                        Slog.w("WindowManager", "Force clearing freeze: " + tok);
                                        WindowManagerService.this.unsetAppFreezingScreenLocked(tok, true, true);
                                    }
                                }
                            }
                        }
                        break;
                    }
                case 18:
                    removeMessages(18);
                    WindowManagerService.this.sendNewConfiguration();
                    break;
                case 19:
                    if (WindowManagerService.this.mWindowsChanged) {
                        synchronized (WindowManagerService.this.mWindowMap) {
                            WindowManagerService.this.mWindowsChanged = false;
                        }
                        WindowManagerService.this.notifyWindowsChanged();
                        break;
                    }
                    break;
                case 20:
                    win = msg.obj;
                    if (WindowManagerDebugConfig.DEBUG_DRAG) {
                        Slog.w("WindowManager", "Timeout starting drag by win " + win);
                    }
                    hashMap = WindowManagerService.this.mWindowMap;
                    synchronized (hashMap) {
                        if (WindowManagerService.this.mDragState != null) {
                            WindowManagerService.this.mDragState.unregister();
                            WindowManagerService.this.mInputMonitor.updateInputWindowsLw(true);
                            WindowManagerService.this.mDragState.reset();
                            WindowManagerService.this.mDragState = null;
                            break;
                        }
                    }
                    break;
                case 21:
                    win = (IBinder) msg.obj;
                    if (WindowManagerDebugConfig.DEBUG_DRAG) {
                        Slog.w("WindowManager", "Timeout ending drag to win " + win);
                    }
                    hashMap = WindowManagerService.this.mWindowMap;
                    synchronized (hashMap) {
                        if (WindowManagerService.this.mDragState != null) {
                            WindowManagerService.this.mDragState.mDragResult = false;
                            WindowManagerService.this.mDragState.endDragLw();
                            break;
                        }
                    }
                    break;
                case 22:
                    WindowManagerService.this.notifyHardKeyboardStatusChange();
                    break;
                case 23:
                    WindowManagerService.this.performBootTimeout();
                    break;
                case 24:
                    synchronized (WindowManagerService.this.mWindowMap) {
                        Slog.w("WindowManager", "Timeout waiting for drawn: undrawn=" + WindowManagerService.this.mWaitingForDrawn);
                        WindowManagerService.this.mWaitingForDrawn.clear();
                        callback = WindowManagerService.this.mWaitingForDrawnCallback;
                        WindowManagerService.this.mWaitingForDrawnCallback = null;
                        dockedCallback = WindowManagerService.this.mDockedForDrawnCallback;
                        WindowManagerService.this.mDockedForDrawnCallback = null;
                        WindowManagerService.this.blockSurfaceFlinger = false;
                        if (WindowManagerDebugConfig.DEBUG_ANIM) {
                            Trace.asyncTraceEnd(32, "wait windows drawn", 0);
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
                case DO_ANIMATION_CALLBACK /*26*/:
                    try {
                        ((IRemoteCallback) msg.obj).sendResult(null);
                        break;
                    } catch (RemoteException e6) {
                        break;
                    }
                case 27:
                    WindowManagerService.this.handleDisplayAdded(msg.arg1);
                    break;
                case DO_DISPLAY_REMOVED /*28*/:
                    hashMap = WindowManagerService.this.mWindowMap;
                    synchronized (hashMap) {
                        WindowManagerService.this.handleDisplayRemovedLocked(msg.arg1);
                        break;
                    }
                case 29:
                    hashMap = WindowManagerService.this.mWindowMap;
                    synchronized (hashMap) {
                        WindowManagerService.this.handleDisplayChangedLocked(msg.arg1);
                        break;
                    }
                case 30:
                    hashMap = WindowManagerService.this.mWindowMap;
                    synchronized (hashMap) {
                        if (WindowManagerService.this.mClientFreezingScreen) {
                            WindowManagerService.this.mClientFreezingScreen = false;
                            WindowManagerService.this.mLastFinishedFreezeSource = "client-timeout";
                            WindowManagerService.this.stopFreezingDisplayLocked();
                            break;
                        }
                    }
                    break;
                case 31:
                    WindowManagerService.this.handleTapOutsideTask((DisplayContent) msg.obj, msg.arg1, msg.arg2);
                    break;
                case 32:
                    try {
                        WindowManagerService.this.mActivityManager.notifyActivityDrawn((IBinder) msg.obj);
                        break;
                    } catch (RemoteException e7) {
                        break;
                    }
                case 33:
                    synchronized (WindowManagerService.this.mWindowMap) {
                        callback = WindowManagerService.this.mWaitingForDrawnCallback;
                        WindowManagerService.this.mWaitingForDrawnCallback = null;
                        dockedCallback = WindowManagerService.this.mDockedForDrawnCallback;
                        WindowManagerService.this.mDockedForDrawnCallback = null;
                        WindowManagerService.this.mH.removeMessages(1000);
                        WindowManagerService.this.blockSurfaceFlinger = false;
                        if (WindowManagerDebugConfig.DEBUG_ANIM) {
                            Trace.asyncTraceEnd(32, "wait windows drawn", 0);
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
                case 34:
                    break;
                case 35:
                    WindowManagerService.this.showCircularMask(msg.arg1 == 1);
                    break;
                case 36:
                    WindowManagerService.this.showEmulatorDisplayOverlay();
                    break;
                case 37:
                    boolean bootAnimationComplete;
                    synchronized (WindowManagerService.this.mWindowMap) {
                        if (WindowManagerDebugConfig.DEBUG_BOOT) {
                            Slog.i("WindowManager", "CHECK_IF_BOOT_ANIMATION_FINISHED:");
                        }
                        bootAnimationComplete = WindowManagerService.this.checkBootAnimationCompleteLocked();
                    }
                    if (bootAnimationComplete) {
                        WindowManagerService.this.performEnableScreen();
                        break;
                    }
                    break;
                case 38:
                    hashMap = WindowManagerService.this.mWindowMap;
                    synchronized (hashMap) {
                        WindowManagerService.this.mLastANRState = null;
                        break;
                    }
                case 39:
                    hashMap = WindowManagerService.this.mWindowMap;
                    synchronized (hashMap) {
                        if (WindowManagerService.this.mWallpaperControllerLocked.processWallpaperDrawPendingTimeout()) {
                            WindowManagerService.this.mWindowPlacerLocked.performSurfacePlacement();
                            break;
                        }
                    }
                    break;
                case 40:
                    WindowManagerService.this.finishPositioning();
                    break;
                case 41:
                    hashMap = WindowManagerService.this.mWindowMap;
                    synchronized (hashMap) {
                        DisplayContent displayContent = WindowManagerService.this.getDefaultDisplayContentLocked();
                        displayContent.getDockedDividerController().reevaluateVisibility(false);
                        WindowManagerService.this.adjustForImeIfNeeded(displayContent);
                        break;
                    }
                case 42:
                    try {
                        WindowManagerService.this.mActivityManager.resizeStack(msg.arg1, (Rect) msg.obj, msg.arg2 == 1, false, false, -1);
                        break;
                    } catch (RemoteException e8) {
                        break;
                    }
                case 43:
                    try {
                        WindowManagerService.this.mActivityManager.resizeTask(msg.arg1, (Rect) msg.obj, msg.arg2);
                        break;
                    } catch (RemoteException e9) {
                        break;
                    }
                case 44:
                    WindowManagerService.this.startScrollingTask((DisplayContent) msg.obj, msg.arg1, msg.arg2);
                    break;
                case WINDOW_REPLACEMENT_TIMEOUT /*46*/:
                    hashMap = WindowManagerService.this.mWindowMap;
                    synchronized (hashMap) {
                        for (i = WindowManagerService.this.mReplacingWindowTimeouts.size() - 1; i >= 0; i--) {
                            ((AppWindowToken) WindowManagerService.this.mReplacingWindowTimeouts.get(i)).clearTimedoutReplacesLocked();
                        }
                        WindowManagerService.this.mReplacingWindowTimeouts.clear();
                        break;
                    }
                case 47:
                    WindowManagerService.this.mAmInternal.notifyAppTransitionStarting(msg.arg1);
                    break;
                case 48:
                    WindowManagerService.this.mAmInternal.notifyAppTransitionCancelled();
                    break;
                case 49:
                    WindowManagerService.this.mAmInternal.notifyAppTransitionFinished();
                    break;
                case 50:
                    WindowManagerService.this.mAmInternal.notifyStartingWindowDrawn();
                    break;
                case 51:
                    switch (msg.arg1) {
                        case 0:
                            WindowManagerService.this.mWindowAnimationScaleSetting = Global.getFloat(WindowManagerService.this.mContext.getContentResolver(), "window_animation_scale", WindowManagerService.this.mWindowAnimationScaleSetting);
                            break;
                        case 1:
                            WindowManagerService.this.mTransitionAnimationScaleSetting = Global.getFloat(WindowManagerService.this.mContext.getContentResolver(), "transition_animation_scale", WindowManagerService.this.mTransitionAnimationScaleSetting);
                            break;
                        case 2:
                            WindowManagerService.this.mAnimatorDurationScaleSetting = Global.getFloat(WindowManagerService.this.mContext.getContentResolver(), "animator_duration_scale", WindowManagerService.this.mAnimatorDurationScaleSetting);
                            WindowManagerService.this.dispatchNewAnimatorScaleLocked(null);
                            break;
                    }
                    break;
                case 52:
                    WindowState window = msg.obj;
                    hashMap = WindowManagerService.this.mWindowMap;
                    synchronized (hashMap) {
                        LayoutParams layoutParams = window.mAttrs;
                        layoutParams.flags &= -129;
                        window.hidePermanentlyLw();
                        window.setDisplayLayoutNeeded();
                        WindowManagerService.this.mWindowPlacerLocked.performSurfacePlacement();
                        break;
                    }
                case 53:
                    WindowManagerService.this.mAmInternal.notifyDockedStackMinimizedChanged(msg.arg1 == 1);
                    break;
                case 54:
                    hashMap = WindowManagerService.this.mWindowMap;
                    synchronized (hashMap) {
                        windows = WindowManagerService.this.getDefaultDisplayContentLocked().getWindowList();
                        boolean layoutNeeded = false;
                        for (i = windows.size() - 1; i >= 0; i--) {
                            w = (WindowState) windows.get(i);
                            if (w.mSeamlesslyRotated) {
                                layoutNeeded = true;
                                w.setDisplayLayoutNeeded();
                                WindowManagerService.this.markForSeamlessRotation(w, false);
                            }
                        }
                        if (layoutNeeded) {
                            WindowManagerService.this.mWindowPlacerLocked.performSurfacePlacement();
                            break;
                        }
                    }
                    break;
                case 55:
                    WindowManagerService.this.mIsUpdateIpoRotation = true;
                    WindowManagerService.this.updateRotation(false, false);
                    break;
                case 56:
                    try {
                        AppWindowToken atoken = msg.obj;
                        if (!(!WindowManagerService.this.isFastStartingWindowSupport() || !WindowManagerService.this.isCacheStartingWindow() || atoken.startingWindow == null || atoken.startingWindow.mWinAnimator == null || atoken.startingView == null || atoken.startingWindow.isFastStartingWindow() || atoken.reportedDrawn || WindowManagerService.this.hasBitmapByToken(atoken.token))) {
                            atoken.startingWindow.mWinAnimator.doCacheBitmap(atoken.startingView);
                            break;
                        }
                    } catch (Exception e10) {
                        Slog.d(WindowManagerService.TAG, "Fast starting window doesn't catch.");
                        break;
                    }
                case 1000:
                    hashMap = WindowManagerService.this.mWindowMap;
                    synchronized (hashMap) {
                        WindowManagerService.this.blockSurfaceFlinger = true;
                        if (WindowManagerDebugConfig.DEBUG_ANIM) {
                            Trace.asyncTraceBegin(32, "wait windows drawn", 0);
                            break;
                        }
                    }
                    break;
                case 1001:
                    hashMap = WindowManagerService.this.mWindowMap;
                    synchronized (hashMap) {
                        WindowManagerService.this.blockSurfaceFlinger = false;
                        if (WindowManagerDebugConfig.DEBUG_ANIM) {
                            Trace.asyncTraceEnd(32, "wait windows drawn", 0);
                            break;
                        }
                    }
                    break;
                case 1002:
                    WindowManagerService.this.mAppTransition.handleAppTransitionSpecsFromFutureTimeout();
                    break;
            }
            i++;
            if (WindowManagerDebugConfig.DEBUG_WINDOW_TRACE) {
                Slog.v("WindowManager", "handleMessage: exit");
            }
            return;
            if (WindowManagerDebugConfig.DEBUG_WINDOW_TRACE) {
            }
            return;
        }
    }

    final class HandlerFloatWindow extends Handler {
        private static final String ACTION_PERMISSION_PROTECT_NOTIFY = "com.oppo.permissionprotect.notify";
        public static final int ADD_FLOAT_WINDOW_PACKAGE = 1;
        public static final int HEARTBEAT = 2;
        public static final int HEARTBEAT_TEN_MINUTES = 600000;
        private static final int MIN_FLOATWINDOW_SIZE = 3;
        private static final int NOTIFY_PERMISSION_DENIED = 3;
        private static final String PERMISSION_FLOAT_WINDOW = "android.permission.SYSTEM_ALERT_WINDOW";
        private static final long PERMISSION_TOAST_INTERVAL = 2000;
        public static final int POPUP_NOTIFY = 4;
        public static final String TAG_TYPE = "Type";
        public static final String TYPE_CHILD_WINDOW = "ChildWindow";
        private final String ACTION = "oppo.action.FLOAT_WINDOW_DATA_COLLECTION";
        private List<String> mListFloatWindow = new ArrayList();

        public HandlerFloatWindow(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    String[] pkgNameArray = WindowManagerService.this.mContext.getPackageManager().getPackagesForUid(msg.obj.mUid);
                    if (pkgNameArray != null) {
                        String pkgName = pkgNameArray[0];
                        this.mListFloatWindow.add(pkgName);
                        if (this.mListFloatWindow.size() >= 3) {
                            sendAndClear();
                        }
                        Message m = Message.obtain();
                        m.what = 3;
                        m.obj = pkgName;
                        WindowManagerService.this.mHandlerFloatWindow.removeMessages(3);
                        WindowManagerService.this.mHandlerFloatWindow.sendMessageDelayed(m, PERMISSION_TOAST_INTERVAL);
                        return;
                    }
                    return;
                case 2:
                    sendAndClear();
                    sendEmptyMessageDelayed(2, LocationFudger.FASTEST_INTERVAL_MS);
                    return;
                case 3:
                    Intent intent = new Intent(ACTION_PERMISSION_PROTECT_NOTIFY);
                    intent.putExtra("PackageName", msg.obj.toString());
                    intent.putExtra("Permission", PERMISSION_FLOAT_WINDOW);
                    WindowManagerService.this.mContext.sendBroadcast(intent);
                    return;
                case 4:
                    WindowManagerService.this.sendPopupWinBroadcast(msg.obj);
                    return;
                default:
                    return;
            }
        }

        private void sendAndClear() {
            if (this.mListFloatWindow.size() > 0) {
                Intent intent = new Intent("oppo.action.FLOAT_WINDOW_DATA_COLLECTION");
                intent.putStringArrayListExtra("pkgName", new ArrayList(this.mListFloatWindow));
                WindowManagerService.this.mContext.sendBroadcast(intent);
                this.mListFloatWindow.clear();
            }
        }
    }

    private static final class HideNavInputConsumer extends InputConsumerImpl implements InputConsumer {
        private final InputEventReceiver mInputEventReceiver;

        HideNavInputConsumer(WindowManagerService service, Looper looper, Factory inputEventReceiverFactory) {
            super(service, "input consumer", null);
            this.mInputEventReceiver = inputEventReceiverFactory.createInputEventReceiver(this.mClientChannel, looper);
        }

        public void dismiss() {
            if (this.mService.removeInputConsumer()) {
                synchronized (this.mService.mWindowMap) {
                    this.mInputEventReceiver.dispose();
                    disposeChannelsLw();
                }
            }
        }
    }

    private final class LocalService extends WindowManagerInternal {
        /* synthetic */ LocalService(WindowManagerService this$0, LocalService localService) {
            this();
        }

        private LocalService() {
        }

        public void requestTraversalFromDisplayManager() {
            WindowManagerService.this.requestTraversal();
        }

        public void setMagnificationSpec(MagnificationSpec spec) {
            synchronized (WindowManagerService.this.mWindowMap) {
                if (WindowManagerService.this.mAccessibilityController != null) {
                    WindowManagerService.this.mAccessibilityController.setMagnificationSpecLocked(spec);
                } else {
                    throw new IllegalStateException("Magnification callbacks not set!");
                }
            }
            if (Binder.getCallingPid() != Process.myPid()) {
                spec.recycle();
            }
        }

        public void getMagnificationRegion(Region magnificationRegion) {
            synchronized (WindowManagerService.this.mWindowMap) {
                if (WindowManagerService.this.mAccessibilityController != null) {
                    WindowManagerService.this.mAccessibilityController.getMagnificationRegionLocked(magnificationRegion);
                } else {
                    throw new IllegalStateException("Magnification callbacks not set!");
                }
            }
        }

        public MagnificationSpec getCompatibleMagnificationSpecForWindow(IBinder windowToken) {
            synchronized (WindowManagerService.this.mWindowMap) {
                WindowState windowState = (WindowState) WindowManagerService.this.mWindowMap.get(windowToken);
                if (windowState == null) {
                    return null;
                }
                MagnificationSpec spec = null;
                if (WindowManagerService.this.mAccessibilityController != null) {
                    spec = WindowManagerService.this.mAccessibilityController.getMagnificationSpecForWindowLocked(windowState);
                }
                if ((spec == null || spec.isNop()) && windowState.mGlobalScale == 1.0f) {
                    return null;
                }
                spec = spec == null ? MagnificationSpec.obtain() : MagnificationSpec.obtain(spec);
                spec.scale *= windowState.mGlobalScale;
                return spec;
            }
        }

        public void setMagnificationCallbacks(MagnificationCallbacks callbacks) {
            synchronized (WindowManagerService.this.mWindowMap) {
                if (WindowManagerService.this.mAccessibilityController == null) {
                    WindowManagerService.this.mAccessibilityController = new AccessibilityController(WindowManagerService.this);
                }
                WindowManagerService.this.mAccessibilityController.setMagnificationCallbacksLocked(callbacks);
                if (!WindowManagerService.this.mAccessibilityController.hasCallbacksLocked()) {
                    WindowManagerService.this.mAccessibilityController = null;
                }
            }
        }

        public void setWindowsForAccessibilityCallback(WindowsForAccessibilityCallback callback) {
            synchronized (WindowManagerService.this.mWindowMap) {
                if (WindowManagerService.this.mAccessibilityController == null) {
                    WindowManagerService.this.mAccessibilityController = new AccessibilityController(WindowManagerService.this);
                }
                WindowManagerService.this.mAccessibilityController.setWindowsForAccessibilityCallback(callback);
                if (!WindowManagerService.this.mAccessibilityController.hasCallbacksLocked()) {
                    WindowManagerService.this.mAccessibilityController = null;
                }
            }
        }

        public void setInputFilter(IInputFilter filter) {
            WindowManagerService.this.mInputManager.setInputFilter(filter);
        }

        public IBinder getFocusedWindowToken() {
            synchronized (WindowManagerService.this.mWindowMap) {
                WindowState windowState = WindowManagerService.this.getFocusedWindowLocked();
                if (windowState != null) {
                    IBinder asBinder = windowState.mClient.asBinder();
                    return asBinder;
                }
                return null;
            }
        }

        public boolean isKeyguardLocked() {
            return WindowManagerService.this.isKeyguardLocked();
        }

        public void showGlobalActions() {
            WindowManagerService.this.showGlobalActions();
        }

        public void getWindowFrame(IBinder token, Rect outBounds) {
            synchronized (WindowManagerService.this.mWindowMap) {
                WindowState windowState = (WindowState) WindowManagerService.this.mWindowMap.get(token);
                if (windowState != null) {
                    outBounds.set(windowState.mFrame);
                } else {
                    outBounds.setEmpty();
                }
            }
        }

        public void waitForAllWindowsDrawn(Runnable callback, long timeout) {
            boolean allWindowsDrawn = false;
            synchronized (WindowManagerService.this.mWindowMap) {
                WindowManagerService.this.mWaitingForDrawnCallback = callback;
                WindowList windows = WindowManagerService.this.getDefaultWindowListLocked();
                boolean shouldWaitForKeyguard = true;
                WindowState keyguard = null;
                for (int winNdx = windows.size() - 1; winNdx >= 0; winNdx--) {
                    WindowState win = (WindowState) windows.get(winNdx);
                    boolean isForceHiding = WindowManagerService.this.mPolicy.isForceHiding(win.mAttrs);
                    if (win.mViewVisibility != 8 && win.isVisibleLw() && (win.mAppToken != null || isForceHiding)) {
                        win.mWinAnimator.mDrawState = 1;
                        win.mLastContentInsets.set(-1, -1, -1, -1);
                        WindowManagerService.this.mWaitingForDrawn.add(win);
                        if (isForceHiding) {
                            break;
                        }
                    }
                    if (shouldWaitForKeyguard) {
                        if (!(win.toString() == null || win.getAttrs() == null)) {
                            int flags = win.getAttrs().flags;
                            boolean isOppoLeather = win.toString().contains("OppoLeather");
                            boolean isShowWhenLocked = (DumpState.DUMP_FROZEN & flags) != 0;
                            boolean isDismissKeyguard = (4194304 & flags) != 0;
                            if (WindowManagerDebugConfig.DEBUG_SCREEN_ON) {
                                Log.d(WindowManagerService.TAG, "waitForAllWindowsDrawn, win = " + win + ", isOppoLeather = " + isOppoLeather + ", isShowWhenLocked = " + isShowWhenLocked + ", isDismissKeyguard = " + isDismissKeyguard + ", win.isVisibleLw() = " + win.isVisibleLw());
                            }
                            shouldWaitForKeyguard = (win.isVisibleLw() && (isOppoLeather || isShowWhenLocked || isDismissKeyguard)) ? false : true;
                        }
                    }
                    if (WindowManagerService.this.mPolicy.isKeyguardHostWindow(win.mAttrs)) {
                        keyguard = win;
                    }
                    if (WindowManagerDebugConfig.DEBUG_SCREEN_ON) {
                        Log.d(WindowManagerService.TAG, "waitForAllWindowsDrawn, shouldWaitForKeyguard = " + shouldWaitForKeyguard);
                    }
                }
                if (shouldWaitForKeyguard && keyguard != null && WindowManagerService.this.mPolicy.isKeyguardLocked()) {
                    keyguard.mWinAnimator.mDrawState = 1;
                    keyguard.mLastContentInsets.set(-1, -1, -1, -1);
                    WindowManagerService.this.mWaitingForDrawn.add(keyguard);
                    Log.d(WindowManagerService.TAG, "waitForAllWindowsDrawn, add keyguard ");
                }
                WindowManagerService.this.mWindowPlacerLocked.requestTraversal();
                WindowManagerService.this.mH.removeMessages(24);
                if (WindowManagerService.this.mWaitingForDrawn.isEmpty()) {
                    allWindowsDrawn = true;
                } else {
                    WindowManagerService.this.mH.sendEmptyMessageDelayed(24, timeout);
                    WindowManagerService.this.checkDrawnWindowsLocked();
                }
            }
            if (!WindowManagerService.this.mWaitingForDrawn.isEmpty()) {
                WindowManagerService.this.mH.removeMessages(1000);
                WindowManagerService.this.mH.sendEmptyMessageDelayed(1000, 20);
                WindowManagerService.this.mH.sendEmptyMessageDelayed(1001, 200);
            }
            if (allWindowsDrawn) {
                callback.run();
            }
        }

        public void addWindowToken(IBinder token, int type) {
            WindowManagerService.this.addWindowToken(token, type);
        }

        public void removeWindowToken(IBinder token, boolean removeWindows) {
            synchronized (WindowManagerService.this.mWindowMap) {
                if (removeWindows) {
                    WindowToken wtoken = (WindowToken) WindowManagerService.this.mTokenMap.remove(token);
                    if (wtoken != null) {
                        wtoken.removeAllWindows();
                    }
                }
                WindowManagerService.this.removeWindowToken(token);
            }
        }

        public void registerAppTransitionListener(AppTransitionListener listener) {
            synchronized (WindowManagerService.this.mWindowMap) {
                WindowManagerService.this.mAppTransition.registerListenerLocked(listener);
            }
        }

        public int getInputMethodWindowVisibleHeight() {
            int inputMethodWindowVisibleHeightLw;
            synchronized (WindowManagerService.this.mWindowMap) {
                inputMethodWindowVisibleHeightLw = WindowManagerService.this.mPolicy.getInputMethodWindowVisibleHeightLw();
            }
            return inputMethodWindowVisibleHeightLw;
        }

        public void saveLastInputMethodWindowForTransition() {
            synchronized (WindowManagerService.this.mWindowMap) {
                if (WindowManagerService.this.mInputMethodWindow != null) {
                    WindowManagerService.this.mPolicy.setLastInputMethodWindowLw(WindowManagerService.this.mInputMethodWindow, WindowManagerService.this.mInputMethodTarget);
                }
            }
        }

        public void clearLastInputMethodWindowForTransition() {
            synchronized (WindowManagerService.this.mWindowMap) {
                WindowManagerService.this.mPolicy.setLastInputMethodWindowLw(null, null);
            }
        }

        public boolean isHardKeyboardAvailable() {
            boolean z;
            synchronized (WindowManagerService.this.mWindowMap) {
                z = WindowManagerService.this.mHardKeyboardAvailable;
            }
            return z;
        }

        public void setOnHardKeyboardStatusChangeListener(OnHardKeyboardStatusChangeListener listener) {
            synchronized (WindowManagerService.this.mWindowMap) {
                WindowManagerService.this.mHardKeyboardStatusChangeListener = listener;
            }
        }

        public boolean isStackVisible(int stackId) {
            boolean isStackVisibleLocked;
            synchronized (WindowManagerService.this.mWindowMap) {
                isStackVisibleLocked = WindowManagerService.this.isStackVisibleLocked(stackId);
            }
            return isStackVisibleLocked;
        }

        public boolean isDockedDividerResizing() {
            boolean isResizing;
            synchronized (WindowManagerService.this.mWindowMap) {
                isResizing = WindowManagerService.this.getDefaultDisplayContentLocked().getDockedDividerController().isResizing();
            }
            return isResizing;
        }
    }

    private static class MousePositionTracker implements PointerEventListener {
        private boolean mLatestEventWasMouse;
        private float mLatestMouseX;
        private float mLatestMouseY;

        /* synthetic */ MousePositionTracker(MousePositionTracker mousePositionTracker) {
            this();
        }

        private MousePositionTracker() {
        }

        void updatePosition(float x, float y) {
            synchronized (this) {
                this.mLatestEventWasMouse = true;
                this.mLatestMouseX = x;
                this.mLatestMouseY = y;
            }
        }

        public void onPointerEvent(MotionEvent motionEvent) {
            if (motionEvent.isFromSource(8194)) {
                updatePosition(motionEvent.getRawX(), motionEvent.getRawY());
                return;
            }
            synchronized (this) {
                this.mLatestEventWasMouse = false;
            }
        }
    }

    private class PopupInfo {
        int action;
        String pkg;
        int type;
        int uid;

        public PopupInfo(int action, String pkg, int uid, int type) {
            this.action = action;
            this.pkg = pkg;
            this.uid = uid;
            this.type = type;
        }

        public String toString() {
            return "action=" + this.action + " pkg=" + this.pkg + " uid=" + this.uid + " type=" + this.type;
        }
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

    class RotationWatcher {
        DeathRecipient deathRecipient;
        IRotationWatcher watcher;

        RotationWatcher(IRotationWatcher w, DeathRecipient d) {
            this.watcher = w;
            this.deathRecipient = d;
        }
    }

    private final class SettingsObserver extends ContentObserver {
        private final Uri mAnimationDurationScaleUri = Global.getUriFor("animator_duration_scale");
        private final Uri mDisplayInversionEnabledUri = Secure.getUriFor("accessibility_display_inversion_enabled");
        private final Uri mDisplayMagnificationEnabledUri = Secure.getUriFor("accessibility_display_magnification_enabled");
        private final Uri mTransitionAnimationScaleUri = Global.getUriFor("transition_animation_scale");
        private final Uri mWindowAnimationScaleUri = Global.getUriFor("window_animation_scale");

        public SettingsObserver() {
            super(new Handler());
            ContentResolver resolver = WindowManagerService.this.mContext.getContentResolver();
            resolver.registerContentObserver(this.mDisplayInversionEnabledUri, false, this, -1);
            resolver.registerContentObserver(this.mWindowAnimationScaleUri, false, this, -1);
            resolver.registerContentObserver(this.mTransitionAnimationScaleUri, false, this, -1);
            resolver.registerContentObserver(this.mAnimationDurationScaleUri, false, this, -1);
            resolver.registerContentObserver(this.mDisplayMagnificationEnabledUri, false, this, -1);
        }

        public void onChange(boolean selfChange, Uri uri) {
            if (uri != null) {
                if (this.mDisplayInversionEnabledUri.equals(uri)) {
                    WindowManagerService.this.updateCircularDisplayMaskIfNeeded();
                } else if (!this.mDisplayMagnificationEnabledUri.equals(uri)) {
                    int mode;
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
                } else if (WindowManagerService.this.mDisplayMagnificationEnabled) {
                    WindowManagerService.this.mDisplayMagnificationEnabled = false;
                    WindowManagerService.this.mMagnificationBorderDisappearCnt = 30;
                } else {
                    WindowManagerService.this.mDisplayMagnificationEnabled = true;
                }
            }
        }
    }

    @IntDef({0, 1, 2})
    @Retention(RetentionPolicy.SOURCE)
    private @interface UpdateAnimationScaleMode {
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.wm.WindowManagerService.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.wm.WindowManagerService.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wm.WindowManagerService.<clinit>():void");
    }

    int getDragLayerLocked() {
        return (this.mPolicy.windowTypeToLayerLw(2016) * 10000) + 1000;
    }

    public void hideKeyguardByFingerprint(boolean isHide) {
        synchronized (this.mWindowMap) {
            Log.d(TAG, "hideKeyguardByFingerprint  begin");
            hideKeyguardLocked(isHide, true);
            Log.d(TAG, "hideKeyguardByFingerprint  end");
        }
    }

    private void hideKeyguardLocked(boolean hide, boolean hideAnimator) {
        this.mH.removeCallbacks(this.mHideKeyguardTimeoutRunnable);
        if (this.mKeyguard != null) {
            if (hide) {
                this.mH.postDelayed(this.mHideKeyguardTimeoutRunnable, 2000);
            }
            Slog.i(TAG, "hideKeyguardByFingerprint, hide = " + hide + ", childCount = " + this.mKeyguard.mChildWindows.size());
            this.mKeyguard.hideByFingerPrint(hide);
            this.mIsKeyguardWindowHide = hide;
            return;
        }
        Log.w(TAG, "mKeyguard == null");
    }

    private void forceRefresh() {
        Log.d(TAG, "forceRefresh");
        try {
            IBinder flinger = ServiceManager.getService("SurfaceFlinger");
            if (flinger != null) {
                Parcel data = Parcel.obtain();
                data.writeInterfaceToken("android.ui.ISurfaceComposer");
                flinger.transact(ANRManager.RENAME_TRACE_FILES_MSG, data, null, 0);
                data.recycle();
            }
        } catch (RemoteException ex) {
            Log.e(TAG, "Failed to refresh surface", ex);
        }
        Log.d(TAG, "forceRefresh  end");
    }

    public static WindowManagerService main(Context context, InputManagerService im, boolean haveInputMethods, boolean showBootMsgs, boolean onlyCore) {
        final WindowManagerService[] holder = new WindowManagerService[1];
        final Context context2 = context;
        final InputManagerService inputManagerService = im;
        final boolean z = haveInputMethods;
        final boolean z2 = showBootMsgs;
        final boolean z3 = onlyCore;
        DisplayThread.getHandler().runWithScissors(new Runnable() {
            @OppoHook(level = OppoHookType.CHANGE_CODE, note = "JianHui.Yu@Plf.SDK, 2016-12-25 : Modify for ColorOS Service", property = OppoRomType.ROM)
            public void run() {
                holder[0] = new OppoWindowManagerService(context2, inputManagerService, z, z2, z3);
            }
        }, 0);
        return holder[0];
    }

    private void initPolicy() {
        UiThread.getHandler().runWithScissors(new Runnable() {
            public void run() {
                WindowManagerPolicyThread.set(Thread.currentThread(), Looper.myLooper());
                if ("eng".equals(Build.TYPE)) {
                    Looper.myLooper().setMessageLogging(ANRAppManager.getDefault(new ANRAppFrameworks()).newMessageLogger(false, Thread.currentThread().getName()));
                }
                WindowManagerService.this.mPolicy.init(WindowManagerService.this.mContext, WindowManagerService.this, WindowManagerService.this);
            }
        }, 0);
    }

    @OppoHook(level = OppoHookType.CHANGE_ACCESS, note = "JianHui.Yu@Plf.SDK, 2016-12-25 : [-private] Modify for ColorOS Service", property = OppoRomType.ROM)
    WindowManagerService(Context context, InputManagerService inputManager, boolean haveInputMethods, boolean showBootMsgs, boolean onlyCore) {
        this.mHyp = null;
        this.mSplitFormBack = false;
        this.mRotationChanged = false;
        this.mBroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if ("android.app.action.DEVICE_POLICY_MANAGER_STATE_CHANGED".equals(action)) {
                    WindowManagerService.this.mKeyguardDisableHandler.sendEmptyMessage(3);
                } else if ("android.intent.action.ACTION_BOOT_IPO".equals(action)) {
                    WindowManagerService.this.mDisplayEnabled = false;
                    SystemProperties.set("service.bootanim.exit", LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON);
                    Slog.v(WindowManagerService.TAG, "set 'service.bootanim.exit' = 1");
                    WindowManagerService.this.mIsAlarmBooting = WindowManagerService.this.isAlarmBoot();
                    if (WindowManagerService.this.mIsAlarmBooting) {
                        Slog.v(WindowManagerService.TAG, "Alarm boot is running");
                        WindowManagerService.this.mInputMonitor.setEventDispatchingLw(true);
                    } else {
                        Slog.v(WindowManagerService.TAG, "Alarm boot is not running");
                        WindowManagerService.this.mH.sendMessage(WindowManagerService.this.mH.obtainMessage(16));
                    }
                    if (WindowManagerService.this.mIsUpdateIpoRotation) {
                        Slog.v(WindowManagerService.TAG, "Update IPO rotation is done");
                        while (!LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(SystemProperties.get("service.bootanim.exit", "0"))) {
                            Slog.v(WindowManagerService.TAG, "service.bootanim.exit = " + SystemProperties.get("service.bootanim.exit", "0"));
                            SystemClock.sleep(100);
                        }
                        SystemClock.sleep(100);
                        WindowManagerService.this.mIsUpdateIpoRotation = false;
                        if (WindowManagerService.this.mIpoRotation != -1) {
                            WindowManagerService.this.freezeRotation(WindowManagerService.this.mIpoRotation);
                            WindowManagerService.this.mIpoRotation = -1;
                            return;
                        }
                        WindowManagerService.this.thawRotation();
                    }
                } else if ("android.intent.action.ACTION_PREBOOT_IPO".equals(intent.getAction())) {
                    WindowManagerService.this.closeSystemDialogs();
                    WindowManagerService.this.mH.sendMessage(WindowManagerService.this.mH.obtainMessage(55));
                    Slog.v(WindowManagerService.TAG, "UPDATE_IPO_ROTATION");
                } else if (WindowManagerService.ALARM_BOOT_DONE.equals(action)) {
                    WindowManagerService.this.mIsAlarmBooting = false;
                    if (WindowManagerService.this.mRotation != 0) {
                        WindowManagerService.this.mIsUpdateAlarmBootRotation = true;
                    }
                    Slog.v(WindowManagerService.TAG, "Alarm boot is done");
                    WindowManagerService.this.mBootAnimationStopped = false;
                    WindowManagerService.this.mH.sendMessage(WindowManagerService.this.mH.obtainMessage(16));
                } else if ("android.intent.action.ACTION_SHUTDOWN_IPO".equals(action)) {
                    WindowManagerService.this.mHasReceiveIPO = true;
                    Slog.v(WindowManagerService.TAG, "IPO_ENABLE, setEventDispatching false");
                    WindowManagerService.this.mInputMonitor.setEventDispatchingLw(false);
                    if (WindowManagerService.this.isRotationFrozen()) {
                        WindowManagerService.this.mIpoRotation = WindowManagerService.this.getRotation();
                    }
                } else if ("android.intent.action.SCREEN_OFF".equals(action)) {
                    if (WindowManagerService.this.mHasReceiveIPO) {
                        WindowManagerService.this.mRotation = 0;
                        WindowManagerService.this.mPolicy.setRotationLw(WindowManagerService.this.mRotation);
                        Slog.v(WindowManagerService.TAG, "Re-initialize the rotation value to " + WindowManagerService.this.mRotation);
                        WindowManagerService.this.mHasReceiveIPO = false;
                    }
                } else if ("android.intent.action.CONFIGURATION_CHANGED".equals(action)) {
                    Slog.d(WindowManagerService.TAG, "Configuration changed, remove fast starting window catch");
                    WindowManagerService.this.mBitmaps.evictAll();
                } else if ("android.intent.action.PACKAGE_REMOVED".equals(action)) {
                    String packageName = intent.getData().getEncodedSchemeSpecificPart();
                    if (packageName != null && WindowManagerService.this.mCheckedFloatWindowSet.contains(packageName)) {
                        WindowManagerService.this.mCheckedFloatWindowSet.remove(packageName);
                    }
                }
            }
        };
        this.mCurrentProfileIds = new int[0];
        this.mPolicy = new OppoPhoneWindowManager();
        this.mSessions = new ArraySet();
        this.mWindowMap = new HashMap();
        this.mTokenMap = new HashMap();
        this.mFinishedStarting = new ArrayList();
        this.mFinishedEarlyAnim = new ArrayList();
        this.mReplacingWindowTimeouts = new ArrayList();
        this.mResizingWindows = new ArrayList();
        this.mPendingRemove = new ArrayList();
        this.mPendingRemoveTmp = new WindowState[20];
        this.mDestroySurface = new ArrayList();
        this.mDestroyPreservedSurface = new ArrayList();
        this.mLosingFocus = new ArrayList();
        this.mForceRemoves = new ArrayList();
        this.mHidingNonSystemOverlayWindows = new ArrayList();
        this.mWaitingForDrawn = new ArrayList();
        this.blockSurfaceFlinger = false;
        this.mRebuildTmp = new WindowState[20];
        this.mScreenCaptureDisabled = new SparseArray();
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
        this.mLastWakeLockHoldingWindow = null;
        this.mLastWakeLockObscuringWindow = null;
        this.mDisplayContents = new SparseArray(2);
        this.mRotation = 0;
        this.mLastOrientation = -1;
        this.mAltOrientation = false;
        this.mDockedStackCreateMode = 0;
        this.mTmpTaskIds = new SparseIntArray();
        this.mChangedStackList = new ArrayList();
        this.mForceResizableTasks = false;
        this.mRotationWatchers = new ArrayList();
        this.mSystemDecorLayer = 0;
        this.mScreenRect = new Rect();
        this.mDisplayFrozen = false;
        this.mDisplayFreezeTime = 0;
        this.mLastDisplayFreezeDuration = 0;
        this.mLastFinishedFreezeSource = null;
        this.mWaitingForConfig = false;
        this.mDisableFrozenBySecuritypermission = false;
        this.mDisableFrozenBySecurityTimeoutRunnable = new Runnable() {
            public void run() {
                WindowManagerService.this.mDisableFrozenBySecuritypermission = false;
            }
        };
        this.mWindowsFreezingScreen = 0;
        this.mClientFreezingScreen = false;
        this.mAppsFreezingScreen = 0;
        this.mLastWindowForcedOrientation = -1;
        this.mLastKeyguardForcedOrientation = -1;
        this.mLayoutSeq = 0;
        this.mLastStatusBarVisibility = 0;
        this.mLastDispatchedSystemUiVisibility = 0;
        this.mCurConfiguration = new Configuration();
        this.mSkipAppTransitionAnimation = false;
        this.mOpeningApps = new ArraySet();
        this.mClosingApps = new ArraySet();
        this.mDisplayMetrics = new DisplayMetrics();
        this.mRealDisplayMetrics = new DisplayMetrics();
        this.mTmpDisplayMetrics = new DisplayMetrics();
        this.mCompatDisplayMetrics = new DisplayMetrics();
        this.mH = new H();
        this.mChoreographer = Choreographer.getInstance();
        this.mCurrentFocus = null;
        this.mLastFocus = null;
        this.mInputMethodTarget = null;
        this.mInputMethodWindow = null;
        this.mInputMethodDialogs = new ArrayList();
        this.mTmpWindows = new ArrayList();
        this.mSeamlessRotationCount = 0;
        this.mDisplayMagnificationEnabled = false;
        this.mMagnificationBorderDisappearCnt = 0;
        this.MAGNIFICATION_DISAPPEAR_CNT_LIMIT = 30;
        this.mFocusedApp = null;
        this.mWindowAnimationScaleSetting = 1.0f;
        this.mTransitionAnimationScaleSetting = 1.0f;
        this.mAnimatorDurationScaleSetting = 1.0f;
        this.mAnimationsDisabled = false;
        this.mDragState = null;
        this.mTaskIdToTask = new SparseArray();
        this.mStackIdToStack = new SparseArray();
        this.mWindowChangeListeners = new ArrayList();
        this.mWindowsChanged = false;
        this.mTempConfiguration = new Configuration();
        this.mIsKeyguardWindowHide = false;
        this.mHideKeyguardTimeoutRunnable = new Runnable() {
            public void run() {
                synchronized (WindowManagerService.this.mWindowMap) {
                    Log.d(WindowManagerService.TAG, "run");
                    WindowManagerService.this.hideKeyguardLocked(false, false);
                }
            }
        };
        this.mCheckedFloatWindowSet = new HashSet();
        this.mNoAnimationNotifyOnTransitionFinished = new ArrayList();
        this.mReconfigureOnConfigurationChanged = new DisplayContentList();
        this.mFreeingChange = false;
        this.mWinRequestTurnScreenOnWhenRelayout = null;
        this.mSimulateWindowFreezing = SystemProperties.getBoolean("persist.simulatewmsfrozen", false);
        this.mActivityManagerAppTransitionNotifier = new AppTransitionListener() {
            public void onAppTransitionCancelledLocked() {
                WindowManagerService.this.mH.sendEmptyMessage(48);
            }

            public void onAppTransitionFinishedLocked(IBinder token) {
                WindowManagerService.this.mH.sendEmptyMessage(49);
                AppWindowToken atoken = WindowManagerService.this.findAppWindowToken(token);
                if (atoken != null) {
                    if (atoken.mLaunchTaskBehind) {
                        try {
                            WindowManagerService.this.mActivityManager.notifyLaunchTaskBehindComplete(atoken.token);
                        } catch (RemoteException e) {
                        }
                        atoken.mLaunchTaskBehind = false;
                    } else {
                        atoken.updateReportedVisibilityLocked();
                        if (atoken.mEnteringAnimation) {
                            atoken.mEnteringAnimation = false;
                            try {
                                WindowManagerService.this.mActivityManager.notifyEnterAnimationComplete(atoken.token);
                            } catch (RemoteException e2) {
                            }
                        }
                    }
                }
            }
        };
        this.mActivityChangedListener = new ActivityChangedListener() {
            public void onActivityChanged(String prePkg, String nextPkg) {
                if (WindowManagerService.DEBUG_DETAIL) {
                    Slog.v(WindowManagerService.TAG, "onActivityChanged, prePkg : " + prePkg + " , nextPkg : " + nextPkg);
                }
                if (prePkg != null && nextPkg != null && !prePkg.equals(nextPkg) && WindowManagerService.this.mCheckedFloatWindowSet != null) {
                    if (prePkg.equals("com.coloros.recents") || prePkg.isEmpty() || nextPkg.isEmpty()) {
                        WindowManagerService.this.updateAppOpsState();
                        return;
                    }
                    if (WindowManagerService.this.mCheckedFloatWindowSet.contains(prePkg)) {
                        if (OppoToastHelper.getToastAppMap().containsKey(prePkg) && ((String) OppoToastHelper.getToastAppMap().get(prePkg)).equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON)) {
                            WindowManagerService.this.mCheckedFloatWindowSet.remove(prePkg);
                        } else {
                            WindowManagerService.this.updateAppOpsState(prePkg, Boolean.valueOf(false));
                        }
                    }
                    if (WindowManagerService.this.mCheckedFloatWindowSet.contains(nextPkg)) {
                        WindowManagerService.this.updateAppOpsState(nextPkg, Boolean.valueOf(true));
                    }
                }
            }
        };
        this.mInputMonitor = new InputMonitor(this);
        this.mMousePositionTracker = new MousePositionTracker();
        this.mIsPerfBoostEnable = false;
        this.mPerfService = null;
        this.mHasReceiveIPO = false;
        this.mIsAlarmBooting = false;
        this.mIsUpdateAlarmBootRotation = false;
        this.mIsUpdateIpoRotation = false;
        this.mIpoRotation = -1;
        this.mCacheBehavior = 0;
        this.mFastStartingWindowSupport = 1 == SystemProperties.getInt("ro.mtk_perf_fast_start_win", 0);
        this.mDisableFastStartingWindow = 1 == SystemProperties.getInt("debug.disable_fast_start_win", 0);
        this.mOppoEnableFastStartingWin = 1 == SystemProperties.getInt("persist.sys.fast_start_win", 0);
        this.mBitmaps = new LruCache(6);
        this.mFreeformStackListeners = new RemoteCallbackList();
        this.mIsRestoreButtonVisible = false;
        this.mDisableStatusBar = false;
        this.dismissDockedStackFromHome = false;
        this.mSystemReady = false;
        this.mContext = context;
        this.mHaveInputMethods = haveInputMethods;
        this.mAllowBootMessages = showBootMsgs;
        this.mOnlyCore = onlyCore;
        this.mLimitedAlphaCompositing = context.getResources().getBoolean(17956875);
        this.mHasPermanentDpad = context.getResources().getBoolean(17956997);
        this.mInTouchMode = context.getResources().getBoolean(17957025);
        this.mDrawLockTimeoutMillis = (long) context.getResources().getInteger(17694869);
        this.mAllowAnimationsInLowPowerMode = context.getResources().getBoolean(17957027);
        this.mInputManager = inputManager;
        this.mDisplayManagerInternal = (DisplayManagerInternal) LocalServices.getService(DisplayManagerInternal.class);
        this.mDisplaySettings = new DisplaySettings();
        this.mDisplaySettings.readSettingsLocked();
        this.mStatusBarHeight = context.getResources().getDimensionPixelSize(17104921);
        this.mWallpaperControllerLocked = new WallpaperController(this);
        this.mWindowPlacerLocked = new WindowSurfacePlacer(this);
        this.mLayersController = new WindowLayersController(this);
        LocalServices.addService(WindowManagerPolicy.class, this.mPolicy);
        this.mPointerEventDispatcher = new PointerEventDispatcher(this.mInputManager.monitorInput("WindowManager"));
        this.mFxSession = new SurfaceSession();
        this.mDisplayManager = (DisplayManager) context.getSystemService("display");
        this.mDisplays = this.mDisplayManager.getDisplays();
        for (Display display : this.mDisplays) {
            createDisplayContentLocked(display);
        }
        this.mKeyguardDisableHandler = new KeyguardDisableHandler(this.mContext, this.mPolicy);
        this.mPowerManager = (PowerManager) context.getSystemService("power");
        this.mPowerManagerInternal = (PowerManagerInternal) LocalServices.getService(PowerManagerInternal.class);
        this.mPowerManagerInternal.registerLowPowerModeObserver(new LowPowerModeListener() {
            public void onLowPowerModeChanged(boolean enabled) {
                synchronized (WindowManagerService.this.mWindowMap) {
                    if (!(WindowManagerService.this.mAnimationsDisabled == enabled || WindowManagerService.this.mAllowAnimationsInLowPowerMode)) {
                        WindowManagerService.this.mAnimationsDisabled = enabled;
                        WindowManagerService.this.dispatchNewAnimatorScaleLocked(null);
                    }
                }
            }
        });
        this.mAnimationsDisabled = this.mPowerManagerInternal.getLowPowerModeEnabled();
        this.mScreenFrozenLock = this.mPowerManager.newWakeLock(1, "SCREEN_FROZEN");
        this.mScreenFrozenLock.setReferenceCounted(false);
        this.mAppTransition = new OppoAppTransition(context, this);
        this.mAppTransition.registerListenerLocked(this.mActivityManagerAppTransitionNotifier);
        this.mBoundsAnimationController = new BoundsAnimationController(this.mAppTransition, UiThread.getHandler());
        this.mActivityManager = ActivityManagerNative.getDefault();
        this.mAmInternal = (ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class);
        this.mAppOps = (AppOpsManager) context.getSystemService("appops");
        OnOpChangedInternalListener opListener = new OnOpChangedInternalListener() {
            public void onOpChanged(int op, String packageName) {
                WindowManagerService.this.updateAppOpsState();
            }
        };
        this.mAppOps.startWatchingMode(24, null, opListener);
        this.mAppOps.startWatchingMode(45, null, opListener);
        this.mWindowAnimationScaleSetting = Global.getFloat(context.getContentResolver(), "window_animation_scale", this.mWindowAnimationScaleSetting);
        this.mTransitionAnimationScaleSetting = Global.getFloat(context.getContentResolver(), "transition_animation_scale", this.mTransitionAnimationScaleSetting);
        setAnimatorDurationScale(Global.getFloat(context.getContentResolver(), "animator_duration_scale", this.mAnimatorDurationScaleSetting));
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.app.action.DEVICE_POLICY_MANAGER_STATE_CHANGED");
        filter.addAction("android.intent.action.ACTION_BOOT_IPO");
        filter.addAction("android.intent.action.ACTION_PREBOOT_IPO");
        filter.addAction("android.intent.action.ACTION_SHUTDOWN_IPO");
        filter.addAction(ALARM_BOOT_DONE);
        filter.addAction("android.intent.action.SCREEN_OFF");
        if (isFastStartingWindowSupport()) {
            filter.addAction("android.intent.action.CONFIGURATION_CHANGED");
        }
        filter.addAction("android.intent.action.PACKAGE_REMOVED");
        this.mContext.registerReceiver(this.mBroadcastReceiver, filter);
        this.mSettingsObserver = new SettingsObserver();
        this.mColorNavigationBarUtil = new ColorNavigationBarUtil(context);
        this.mHoldingScreenWakeLock = this.mPowerManager.newWakeLock(536870922, "WindowManager");
        this.mHoldingScreenWakeLock.setReferenceCounted(false);
        this.mAnimator = new WindowAnimator(this);
        this.mAllowTheaterModeWakeFromLayout = context.getResources().getBoolean(17956913);
        LocalServices.addService(WindowManagerInternal.class, new LocalService(this, null));
        initPolicy();
        Watchdog.getInstance().addMonitor(this);
        SurfaceControl.openTransaction();
        try {
            boolean z;
            createWatermarkInTransaction();
            showEmulatorDisplayOverlayIfNeeded();
            this.mThreadFloatWindow = new HandlerThread("ThreadFloatWindow");
            this.mThreadFloatWindow.start();
            this.mHandlerFloatWindow = new HandlerFloatWindow(this.mThreadFloatWindow.getLooper());
            this.mHandlerFloatWindow.sendEmptyMessageDelayed(2, LocationFudger.FASTEST_INTERVAL_MS);
            this.mIsAlarmBooting = isAlarmBoot();
            Log.v(TAG, "mIsAlarmBooting = " + this.mIsAlarmBooting);
            if (this.mIsAlarmBooting) {
                this.mInputMonitor.setEventDispatchingLw(true);
            }
            OppoToastHelper.setWMService(this);
            OppoProtectEyeManagerService.setActivityChangedListener(this.mActivityChangedListener);
            WmsFrozenStateWatch wmsFrozenWatch = new WmsFrozenStateWatch();
            wmsFrozenWatch.setWmsInstance(this);
            CheckBlockedException.getInstance().addStateWatch(wmsFrozenWatch);
            if (Secure.getInt(this.mContext.getContentResolver(), "accessibility_display_magnification_enabled", 0) == 1) {
                z = true;
            } else {
                z = false;
            }
            this.mDisplayMagnificationEnabled = z;
        } finally {
            SurfaceControl.closeTransaction();
        }
    }

    public InputMonitor getInputMonitor() {
        return this.mInputMonitor;
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

    private void placeWindowAfter(WindowState pos, WindowState window) {
        WindowList windows = pos.getWindowList();
        int i = windows.indexOf(pos);
        if (DEBUG_WMS || WindowManagerDebugConfig.DEBUG_FOCUS || WindowManagerDebugConfig.DEBUG_WINDOW_MOVEMENT || WindowManagerDebugConfig.DEBUG_ADD_REMOVE) {
            Slog.v("WindowManager", "Adding window " + window + " at " + (i + 1) + " of " + windows.size() + " (after " + pos + ")");
        }
        windows.add(i + 1, window);
        this.mWindowsChanged = true;
    }

    private void placeWindowBefore(WindowState pos, WindowState window) {
        WindowList windows = pos.getWindowList();
        int i = windows.indexOf(pos);
        if (DEBUG_WMS || WindowManagerDebugConfig.DEBUG_FOCUS || WindowManagerDebugConfig.DEBUG_WINDOW_MOVEMENT || WindowManagerDebugConfig.DEBUG_ADD_REMOVE) {
            Slog.v("WindowManager", "Adding window " + window + " at " + i + " of " + windows.size() + " (before " + pos + ")");
        }
        if (i < 0) {
            Slog.w("WindowManager", "placeWindowBefore: Unable to find " + pos + " in " + windows);
            i = 0;
        }
        windows.add(i, window);
        this.mWindowsChanged = true;
    }

    private int findIdxBasedOnAppTokens(WindowState win) {
        WindowList windows = win.getWindowList();
        for (int j = windows.size() - 1; j >= 0; j--) {
            if (((WindowState) windows.get(j)).mAppToken == win.mAppToken) {
                return j;
            }
        }
        return -1;
    }

    private WindowList getTokenWindowsOnDisplay(WindowToken token, DisplayContent displayContent) {
        WindowList windowList = new WindowList();
        int count = token.windows.size();
        for (int i = 0; i < count; i++) {
            WindowState win = (WindowState) token.windows.get(i);
            if (win.getDisplayContent() == displayContent) {
                windowList.add(win);
            }
        }
        return windowList;
    }

    private int indexOfWinInWindowList(WindowState targetWin, WindowList windows) {
        for (int i = windows.size() - 1; i >= 0; i--) {
            WindowState w = (WindowState) windows.get(i);
            if (w == targetWin) {
                return i;
            }
            if (!w.mChildWindows.isEmpty() && indexOfWinInWindowList(targetWin, w.mChildWindows) >= 0) {
                return i;
            }
        }
        return -1;
    }

    /* JADX WARNING: Removed duplicated region for block: B:39:0x0120 A:{LOOP_END, LOOP:0: B:12:0x007b->B:39:0x0120} */
    /* JADX WARNING: Removed duplicated region for block: B:76:0x00bf A:{SYNTHETIC, EDGE_INSN: B:76:0x00bf->B:23:0x00bf ?: BREAK  } */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int addAppWindowToListLocked(WindowState win) {
        DisplayContent displayContent = win.getDisplayContent();
        if (displayContent == null) {
            return 0;
        }
        IWindow client = win.mClient;
        WindowToken token = win.mToken;
        WindowList windows = displayContent.getWindowList();
        WindowList tokenWindowList = getTokenWindowsOnDisplay(token, displayContent);
        if (!tokenWindowList.isEmpty()) {
            return addAppWindowToTokenListLocked(win, token, windows, tokenWindowList);
        }
        AppTokenList tokens;
        if (localLOGV) {
            Slog.v("WindowManager", "Figuring out where to add app window " + client.asBinder() + " (token=" + token + ")");
        }
        WindowState pos = null;
        ArrayList<Task> tasks = displayContent.getTasks();
        int tokenNdx = -1;
        int taskNdx = tasks.size() - 1;
        while (taskNdx >= 0) {
            tokens = ((Task) tasks.get(taskNdx)).mAppTokens;
            tokenNdx = tokens.size() - 1;
            while (tokenNdx >= 0) {
                WindowToken t = (AppWindowToken) tokens.get(tokenNdx);
                if (t == token) {
                    tokenNdx--;
                    if (tokenNdx < 0) {
                        taskNdx--;
                        if (taskNdx >= 0) {
                            tokenNdx = ((Task) tasks.get(taskNdx)).mAppTokens.size() - 1;
                        }
                    }
                    if (tokenNdx < 0) {
                        break;
                    }
                    taskNdx--;
                } else {
                    tokenWindowList = getTokenWindowsOnDisplay(t, displayContent);
                    if (!t.sendingToBottom && tokenWindowList.size() > 0) {
                        pos = (WindowState) tokenWindowList.get(0);
                    }
                    tokenNdx--;
                }
            }
            if (tokenNdx < 0) {
            }
        }
        WindowToken atoken;
        if (pos != null) {
            atoken = (WindowToken) this.mTokenMap.get(pos.mClient.asBinder());
            if (atoken != null) {
                tokenWindowList = getTokenWindowsOnDisplay(atoken, displayContent);
                if (tokenWindowList.size() > 0) {
                    WindowState bottom = (WindowState) tokenWindowList.get(0);
                    if (bottom.mSubLayer < 0) {
                        pos = bottom;
                    }
                }
            }
            placeWindowBefore(pos, win);
            return 0;
        }
        for (taskNdx = 
/*
Method generation error in method: com.android.server.wm.WindowManagerService.addAppWindowToListLocked(com.android.server.wm.WindowState):int, dex: 
jadx.core.utils.exceptions.CodegenException: Error generate insn: PHI: (r15_5 'taskNdx' int) = (r15_3 'taskNdx' int), (r15_1 'taskNdx' int) binds: {(r15_3 'taskNdx' int)=B:76:0x00bf, (r15_1 'taskNdx' int)=B:75:0x00bf} in method: com.android.server.wm.WindowManagerService.addAppWindowToListLocked(com.android.server.wm.WindowState):int, dex: 
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:228)
	at jadx.core.codegen.RegionGen.makeLoop(RegionGen.java:183)
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
	... 27 more

*/

    private int addAppWindowToTokenListLocked(WindowState win, WindowToken token, WindowList windows, WindowList tokenWindowList) {
        if (win.mAttrs.type == 1) {
            WindowState lowestWindow = (WindowState) tokenWindowList.get(0);
            placeWindowBefore(lowestWindow, win);
            return indexOfWinInWindowList(lowestWindow, token.windows);
        }
        AppWindowToken atoken = win.mAppToken;
        WindowState lastWindow = (WindowState) tokenWindowList.get(tokenWindowList.size() - 1);
        if (atoken == null || lastWindow != atoken.startingWindow) {
            int tokenWindowsPos;
            int newIdx = findIdxBasedOnAppTokens(win);
            if (WindowManagerDebugConfig.DEBUG_FOCUS || WindowManagerDebugConfig.DEBUG_WINDOW_MOVEMENT || WindowManagerDebugConfig.DEBUG_ADD_REMOVE) {
                Slog.v("WindowManager", "not Base app: Adding window " + win + " at " + (newIdx + 1) + " of " + windows.size());
            }
            windows.add(newIdx + 1, win);
            if (newIdx < 0) {
                tokenWindowsPos = 0;
            } else {
                tokenWindowsPos = indexOfWinInWindowList((WindowState) windows.get(newIdx), token.windows) + 1;
            }
            this.mWindowsChanged = true;
            return tokenWindowsPos;
        }
        placeWindowBefore(lastWindow, win);
        return indexOfWinInWindowList(lastWindow, token.windows);
    }

    private void addFreeWindowToListLocked(WindowState win) {
        WindowList windows = win.getWindowList();
        int windowType = win.getBaseType();
        int myLayer = win.mBaseLayer;
        int i = windows.size() - 1;
        while (i >= 0) {
            WindowState otherWin = (WindowState) windows.get(i);
            if ((windowType == 2004 && otherWin.mBaseLayer <= myLayer) || (otherWin.getBaseType() != 2013 && otherWin.mBaseLayer <= myLayer)) {
                break;
            }
            i--;
        }
        i++;
        if (WindowManagerDebugConfig.DEBUG_FOCUS || WindowManagerDebugConfig.DEBUG_WINDOW_MOVEMENT || WindowManagerDebugConfig.DEBUG_ADD_REMOVE) {
            Slog.v("WindowManager", "Free window: Adding window " + win + " at " + i + " of " + windows.size());
        }
        windows.add(i, win);
        this.mWindowsChanged = true;
    }

    /* JADX WARNING: Removed duplicated region for block: B:19:0x0063  */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x0063  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void addAttachedWindowToListLocked(WindowState win, boolean addToToken) {
        WindowToken token = win.mToken;
        DisplayContent displayContent = win.getDisplayContent();
        if (displayContent != null) {
            int i;
            WindowState attached = win.mAttachedWindow;
            WindowList tokenWindowList = getTokenWindowsOnDisplay(token, displayContent);
            int NA = tokenWindowList.size();
            int sublayer = win.mSubLayer;
            int largestSublayer = Integer.MIN_VALUE;
            WindowState windowWithLargestSublayer = null;
            for (i = 0; i < NA; i++) {
                WindowState w = (WindowState) tokenWindowList.get(i);
                int wSublayer = w.mSubLayer;
                if (wSublayer >= largestSublayer) {
                    largestSublayer = wSublayer;
                    windowWithLargestSublayer = w;
                }
                if (sublayer < 0) {
                    if (wSublayer >= sublayer) {
                        if (addToToken) {
                            if (WindowManagerDebugConfig.DEBUG_ADD_REMOVE) {
                                Slog.v("WindowManager", "Adding " + win + " to " + token);
                            }
                            token.windows.add(i, win);
                        }
                        if (wSublayer >= 0) {
                            w = attached;
                        }
                        placeWindowBefore(w, win);
                        if (i >= NA) {
                            if (addToToken) {
                                if (WindowManagerDebugConfig.DEBUG_ADD_REMOVE) {
                                    Slog.v("WindowManager", "Adding " + win + " to " + token);
                                }
                                token.windows.add(win);
                            }
                            if (sublayer < 0) {
                                placeWindowBefore(attached, win);
                            } else {
                                if (largestSublayer < 0) {
                                    windowWithLargestSublayer = attached;
                                }
                                placeWindowAfter(windowWithLargestSublayer, win);
                            }
                        }
                    }
                } else if (wSublayer > sublayer) {
                    if (addToToken) {
                        if (WindowManagerDebugConfig.DEBUG_ADD_REMOVE) {
                            Slog.v("WindowManager", "Adding " + win + " to " + token);
                        }
                        token.windows.add(i, win);
                    }
                    placeWindowBefore(w, win);
                    if (i >= NA) {
                    }
                }
            }
            if (i >= NA) {
            }
        }
    }

    private void addWindowToListInOrderLocked(WindowState win, boolean addToToken) {
        if (WindowManagerDebugConfig.DEBUG_FOCUS) {
            Slog.d("WindowManager", "addWindowToListInOrderLocked: win=" + win + " Callers=" + Debug.getCallers(4));
        }
        if (win.mAttachedWindow == null) {
            WindowToken token = win.mToken;
            int tokenWindowsPos = 0;
            if (token.appWindowToken != null) {
                tokenWindowsPos = addAppWindowToListLocked(win);
            } else {
                addFreeWindowToListLocked(win);
            }
            if (addToToken) {
                if (WindowManagerDebugConfig.DEBUG_ADD_REMOVE) {
                    Slog.v("WindowManager", "Adding " + win + " to " + token);
                }
                token.windows.add(tokenWindowsPos, win);
            }
        } else {
            addAttachedWindowToListLocked(win, addToToken);
        }
        AppWindowToken appToken = win.mAppToken;
        if (appToken != null && addToToken) {
            appToken.addWindow(win);
        }
    }

    static boolean canBeImeTarget(WindowState w) {
        int fl = w.mAttrs.flags & 131080;
        int type = w.mAttrs.type;
        if (fl != 0 && fl != 131080 && type != 3) {
            return false;
        }
        if (WindowManagerDebugConfig.DEBUG_INPUT_METHOD) {
            Slog.i("WindowManager", "isVisibleOrAdding " + w + ": " + w.isVisibleOrAdding());
            if (!w.isVisibleOrAdding()) {
                Slog.i("WindowManager", "  mSurfaceController=" + w.mWinAnimator.mSurfaceController + " relayoutCalled=" + w.mRelayoutCalled + " viewVis=" + w.mViewVisibility + " policyVis=" + w.mPolicyVisibility + " policyVisAfterAnim=" + w.mPolicyVisibilityAfterAnim + " attachHid=" + w.mAttachedHidden + " exiting=" + w.mAnimatingExit + " destroying=" + w.mDestroying);
                if (w.mAppToken != null) {
                    Slog.i("WindowManager", "  mAppToken.hiddenRequested=" + w.mAppToken.hiddenRequested);
                }
            }
        }
        return w.isVisibleOrAdding();
    }

    /* JADX WARNING: Removed duplicated region for block: B:47:0x014e  */
    /* JADX WARNING: Removed duplicated region for block: B:69:0x0238  */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x017f  */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x0182  */
    /* JADX WARNING: Removed duplicated region for block: B:113:0x037a  */
    /* JADX WARNING: Removed duplicated region for block: B:84:0x0299  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    int findDesiredInputMethodWindowIndexLocked(boolean willMove) {
        WindowState curTarget;
        AppWindowToken token;
        WindowList windows = getDefaultWindowListLocked();
        WindowState w = null;
        WindowState stickyWin = null;
        int i = windows.size() - 1;
        while (i >= 0) {
            WindowState win = (WindowState) windows.get(i);
            if (WindowManagerDebugConfig.DEBUG_INPUT_METHOD && willMove) {
                Slog.i("WindowManager", "Checking window @" + i + " " + win + " fl=0x" + Integer.toHexString(win.mAttrs.flags));
            }
            if (MultiWindowManager.isSupported() && stickyWin == null && isStickyByMtk(win)) {
                stickyWin = win;
                if (this.mFocusedApp != win.mAppToken) {
                    Slog.v(TAG, "[BMW]Sticky " + win + " is not a focus window." + " Therefore, it can't be ime target");
                    i--;
                }
            }
            if (canBeImeTarget(win)) {
                w = win;
                if (!willMove && win.mAttrs.type == 3 && i > 0) {
                    WindowState wb = (WindowState) windows.get(i - 1);
                    if (wb.mAppToken == win.mAppToken && canBeImeTarget(wb)) {
                        i--;
                        w = wb;
                    }
                }
                if (WindowManagerDebugConfig.DEBUG_INPUT_METHOD && willMove) {
                    Slog.v("WindowManager", "Proposed new IME target: " + w);
                }
                curTarget = this.mInputMethodTarget;
                if (curTarget == null && curTarget.isDisplayedLw() && curTarget.isClosing() && (w == null || curTarget.mWinAnimator.mAnimLayer > w.mWinAnimator.mAnimLayer)) {
                    if (WindowManagerDebugConfig.DEBUG_INPUT_METHOD) {
                        Slog.v("WindowManager", "Current target higher, not changing");
                    }
                    return windows.indexOf(curTarget) + 1;
                }
                if (WindowManagerDebugConfig.DEBUG_INPUT_METHOD) {
                    Slog.v("WindowManager", "Desired input method target=" + w + " willMove=" + willMove);
                }
                if (willMove && w != null) {
                    token = curTarget != null ? null : curTarget.mAppToken;
                    if (token != null) {
                        WindowState highestTarget = null;
                        int highestPos = 0;
                        if (token.mAppAnimator.animating || token.mAppAnimator.animation != null) {
                            WindowList curWindows = curTarget.getWindowList();
                            for (int pos = curWindows.indexOf(curTarget); pos >= 0; pos--) {
                                win = (WindowState) curWindows.get(pos);
                                if (win.mAppToken != token) {
                                    break;
                                }
                                if (!win.mRemoved && (highestTarget == null || win.mWinAnimator.mAnimLayer > highestTarget.mWinAnimator.mAnimLayer)) {
                                    highestTarget = win;
                                    highestPos = pos;
                                }
                            }
                        }
                        if (highestTarget != null) {
                            if (WindowManagerDebugConfig.DEBUG_INPUT_METHOD) {
                                Slog.v("WindowManager", this.mAppTransition + " " + highestTarget + " animating=" + highestTarget.mWinAnimator.isAnimationSet() + " layer=" + highestTarget.mWinAnimator.mAnimLayer + " new layer=" + w.mWinAnimator.mAnimLayer);
                            }
                            if (this.mAppTransition.isTransitionSet()) {
                                this.mInputMethodTargetWaitingAnim = true;
                                this.mInputMethodTarget = highestTarget;
                                return highestPos + 1;
                            } else if (highestTarget.mWinAnimator.isAnimationSet() && highestTarget.mWinAnimator.mAnimLayer > w.mWinAnimator.mAnimLayer) {
                                this.mInputMethodTargetWaitingAnim = true;
                                this.mInputMethodTarget = highestTarget;
                                return highestPos + 1;
                            }
                        }
                    }
                }
                if (w == null) {
                    if (willMove) {
                        if (WindowManagerDebugConfig.DEBUG_INPUT_METHOD) {
                            Slog.w("WindowManager", "Moving IM target from " + curTarget + " to " + w + (WindowManagerDebugConfig.SHOW_STACK_CRAWLS ? " Callers=" + Debug.getCallers(4) : IElsaManager.EMPTY_PACKAGE));
                        }
                        this.mInputMethodTarget = w;
                        this.mInputMethodTargetWaitingAnim = false;
                        if (w.mAppToken != null) {
                            this.mLayersController.setInputMethodAnimLayerAdjustment(w.mAppToken.mAppAnimator.animLayerAdjustment);
                        } else {
                            this.mLayersController.setInputMethodAnimLayerAdjustment(0);
                        }
                    }
                    WindowState dockedDivider = w.mDisplayContent.mDividerControllerLocked.getWindow();
                    if (dockedDivider != null && dockedDivider.isVisibleLw()) {
                        int dividerIndex = windows.indexOf(dockedDivider);
                        if (dividerIndex > 0 && dividerIndex > i) {
                            return dividerIndex + 1;
                        }
                    }
                    if (!MultiWindowManager.isSupported() || stickyWin == null || stickyWin == w) {
                        return i + 1;
                    }
                    Slog.v(TAG, "[BMW]Because of sticky window, move ime over the stick window");
                    return windows.indexOf(stickyWin) + 1;
                }
                if (willMove) {
                    if (WindowManagerDebugConfig.DEBUG_INPUT_METHOD) {
                        Slog.w("WindowManager", "Moving IM target from " + curTarget + " to null." + (WindowManagerDebugConfig.SHOW_STACK_CRAWLS ? " Callers=" + Debug.getCallers(4) : IElsaManager.EMPTY_PACKAGE));
                    }
                    this.mInputMethodTarget = null;
                    this.mLayersController.setInputMethodAnimLayerAdjustment(0);
                }
                return -1;
            }
            i--;
        }
        Slog.v("WindowManager", "Proposed new IME target: " + w);
        curTarget = this.mInputMethodTarget;
        if (curTarget == null) {
        }
        if (WindowManagerDebugConfig.DEBUG_INPUT_METHOD) {
        }
        if (curTarget != null) {
        }
        if (token != null) {
        }
        if (w == null) {
        }
    }

    void addInputMethodWindowToListLocked(WindowState win) {
        int pos = findDesiredInputMethodWindowIndexLocked(true);
        if (pos >= 0) {
            win.mTargetAppToken = this.mInputMethodTarget.mAppToken;
            if (WindowManagerDebugConfig.DEBUG_WINDOW_MOVEMENT || WindowManagerDebugConfig.DEBUG_ADD_REMOVE) {
                Slog.v("WindowManager", "Adding input method window " + win + " at " + pos);
            }
            getDefaultWindowListLocked().add(pos, win);
            this.mWindowsChanged = true;
            moveInputMethodDialogsLocked(pos + 1);
            return;
        }
        win.mTargetAppToken = null;
        addWindowToListInOrderLocked(win, true);
        moveInputMethodDialogsLocked(pos);
    }

    private int tmpRemoveWindowLocked(int interestingPos, WindowState win) {
        WindowList windows = win.getWindowList();
        int wpos = windows.indexOf(win);
        if (wpos >= 0) {
            if (wpos < interestingPos) {
                interestingPos--;
            }
            if (WindowManagerDebugConfig.DEBUG_WINDOW_MOVEMENT) {
                Slog.v("WindowManager", "Temp removing at " + wpos + ": " + win);
            }
            windows.remove(wpos);
            this.mWindowsChanged = true;
            int NC = win.mChildWindows.size();
            while (NC > 0) {
                NC--;
                WindowState cw = (WindowState) win.mChildWindows.get(NC);
                int cpos = windows.indexOf(cw);
                if (cpos >= 0) {
                    if (cpos < interestingPos) {
                        interestingPos--;
                    }
                    if (WindowManagerDebugConfig.DEBUG_WINDOW_MOVEMENT) {
                        Slog.v("WindowManager", "Temp removing child at " + cpos + ": " + cw);
                    }
                    windows.remove(cpos);
                }
            }
        }
        return interestingPos;
    }

    private void reAddWindowToListInOrderLocked(WindowState win) {
        addWindowToListInOrderLocked(win, false);
        WindowList windows = win.getWindowList();
        int wpos = windows.indexOf(win);
        if (wpos >= 0) {
            if (WindowManagerDebugConfig.DEBUG_WINDOW_MOVEMENT) {
                Slog.v("WindowManager", "ReAdd removing from " + wpos + ": " + win);
            }
            windows.remove(wpos);
            this.mWindowsChanged = true;
            reAddWindowLocked(wpos, win);
        }
    }

    void logWindowList(WindowList windows, String prefix) {
        int N = windows.size();
        while (N > 0) {
            N--;
            Slog.v("WindowManager", prefix + "#" + N + ": " + windows.get(N));
        }
    }

    void moveInputMethodDialogsLocked(int pos) {
        int i;
        ArrayList<WindowState> dialogs = this.mInputMethodDialogs;
        WindowList windows = getDefaultWindowListLocked();
        int N = dialogs.size();
        if (WindowManagerDebugConfig.DEBUG_INPUT_METHOD) {
            Slog.v("WindowManager", "Removing " + N + " dialogs w/pos=" + pos);
        }
        for (i = 0; i < N; i++) {
            pos = tmpRemoveWindowLocked(pos, (WindowState) dialogs.get(i));
        }
        if (WindowManagerDebugConfig.DEBUG_INPUT_METHOD) {
            Slog.v("WindowManager", "Window list w/pos=" + pos);
            logWindowList(windows, "  ");
        }
        WindowState win;
        if (pos >= 0) {
            AppWindowToken targetAppToken = this.mInputMethodTarget.mAppToken;
            if (this.mInputMethodWindow != null) {
                while (pos < windows.size()) {
                    WindowState wp = (WindowState) windows.get(pos);
                    if (wp != this.mInputMethodWindow && wp.mAttachedWindow != this.mInputMethodWindow) {
                        break;
                    }
                    pos++;
                }
            }
            if (WindowManagerDebugConfig.DEBUG_INPUT_METHOD) {
                Slog.v("WindowManager", "Adding " + N + " dialogs at pos=" + pos);
            }
            for (i = 0; i < N; i++) {
                win = (WindowState) dialogs.get(i);
                win.mTargetAppToken = targetAppToken;
                pos = reAddWindowLocked(pos, win);
            }
            if (WindowManagerDebugConfig.DEBUG_INPUT_METHOD) {
                Slog.v("WindowManager", "Final window list:");
                logWindowList(windows, "  ");
            }
            return;
        }
        for (i = 0; i < N; i++) {
            win = (WindowState) dialogs.get(i);
            win.mTargetAppToken = null;
            reAddWindowToListInOrderLocked(win);
            if (WindowManagerDebugConfig.DEBUG_INPUT_METHOD) {
                Slog.v("WindowManager", "No IM target, final list:");
                logWindowList(windows, "  ");
            }
        }
    }

    boolean moveInputMethodWindowsIfNeededLocked(boolean needAssignLayers) {
        WindowState imWin = this.mInputMethodWindow;
        int DN = this.mInputMethodDialogs.size();
        if (imWin == null && DN == 0) {
            return false;
        }
        WindowList windows = getDefaultWindowListLocked();
        int imPos = findDesiredInputMethodWindowIndexLocked(true);
        if (imPos >= 0) {
            WindowState baseImWin;
            int N = windows.size();
            WindowState firstImWin = imPos < N ? (WindowState) windows.get(imPos) : null;
            if (imWin != null) {
                baseImWin = imWin;
            } else {
                baseImWin = (WindowState) this.mInputMethodDialogs.get(0);
            }
            if (baseImWin.mChildWindows.size() > 0) {
                WindowState cw = (WindowState) baseImWin.mChildWindows.get(0);
                if (cw.mSubLayer < 0) {
                    baseImWin = cw;
                }
            }
            if (firstImWin == baseImWin) {
                int pos = imPos + 1;
                while (pos < N && ((WindowState) windows.get(pos)).mIsImWindow) {
                    pos++;
                }
                pos++;
                while (pos < N && !((WindowState) windows.get(pos)).mIsImWindow) {
                    pos++;
                }
                if (pos >= N) {
                    if (imWin != null) {
                        imWin.mTargetAppToken = this.mInputMethodTarget.mAppToken;
                    }
                    return false;
                }
            }
            if (imWin != null) {
                if (WindowManagerDebugConfig.DEBUG_INPUT_METHOD) {
                    Slog.v("WindowManager", "Moving IM from " + imPos);
                    logWindowList(windows, "  ");
                }
                imPos = tmpRemoveWindowLocked(imPos, imWin);
                if (WindowManagerDebugConfig.DEBUG_INPUT_METHOD) {
                    Slog.v("WindowManager", "List after removing with new pos " + imPos + ":");
                    logWindowList(windows, "  ");
                }
                imWin.mTargetAppToken = this.mInputMethodTarget.mAppToken;
                reAddWindowLocked(imPos, imWin);
                if (WindowManagerDebugConfig.DEBUG_INPUT_METHOD) {
                    Slog.v("WindowManager", "List after moving IM to " + imPos + ":");
                    logWindowList(windows, "  ");
                }
                if (DN > 0) {
                    moveInputMethodDialogsLocked(imPos + 1);
                }
            } else {
                moveInputMethodDialogsLocked(imPos);
            }
        } else if (imWin != null) {
            if (WindowManagerDebugConfig.DEBUG_INPUT_METHOD) {
                Slog.v("WindowManager", "Moving IM from " + imPos);
            }
            tmpRemoveWindowLocked(0, imWin);
            imWin.mTargetAppToken = null;
            reAddWindowToListInOrderLocked(imWin);
            if (WindowManagerDebugConfig.DEBUG_INPUT_METHOD) {
                Slog.v("WindowManager", "List with no IM target:");
                logWindowList(windows, "  ");
            }
            if (DN > 0) {
                moveInputMethodDialogsLocked(-1);
            }
        } else {
            moveInputMethodDialogsLocked(-1);
        }
        if (needAssignLayers) {
            this.mLayersController.assignLayersLocked(windows);
        }
        return true;
    }

    private static boolean excludeWindowTypeFromTapOutTask(int windowType) {
        switch (windowType) {
            case 2000:
            case 2012:
            case 2019:
                return true;
            default:
                return false;
        }
    }

    /* JADX WARNING: Missing block: B:348:0x078f, code:
            if (r56.mCurrentFocus.mOwnerUid == r35) goto L_0x0704;
     */
    /* JADX WARNING: Missing block: B:430:0x09cf, code:
            if (r50 == false) goto L_0x09d4;
     */
    /* JADX WARNING: Missing block: B:431:0x09d1, code:
            sendNewConfiguration();
     */
    /* JADX WARNING: Missing block: B:432:0x09d4, code:
            android.os.Binder.restoreCallingIdentity(r48);
     */
    /* JADX WARNING: Missing block: B:433:0x09d7, code:
            return r51;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int addWindow(Session session, IWindow client, int seq, LayoutParams attrs, int viewVisibility, int displayId, Rect outContentInsets, Rect outStableInsets, Rect outOutsets, InputChannel outInputChannel) {
        int[] appOp = new int[1];
        int res = this.mPolicy.checkAddPermission(attrs, appOp);
        if (res != 0) {
            return res;
        }
        String topAppName = IElsaManager.EMPTY_PACKAGE;
        Boolean isUserToast = Boolean.valueOf(false);
        try {
            if (this.mActivityManager.getTopAppName() != null) {
                topAppName = this.mActivityManager.getTopAppName().getPackageName();
            }
            if (appOp[0] == 45 && client.isUserDefinedToast()) {
                isUserToast = Boolean.valueOf(true);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        boolean reportNewConfig = false;
        WindowState attachedWindow = null;
        int callingUid = Binder.getCallingUid();
        int type = attrs.type;
        synchronized (this.mWindowMap) {
            if (this.mDisplayReady) {
                DisplayContent displayContent = getDisplayContentLocked(displayId);
                if (displayContent == null) {
                    Slog.w("WindowManager", "Attempted to add window to a display that does not exist: " + displayId + ".  Aborting.");
                    return -9;
                } else if (!displayContent.hasAccess(session.mUid)) {
                    Slog.w("WindowManager", "Attempted to add window to a display for which the application does not have access: " + displayId + ".  Aborting.");
                    return -9;
                } else if (this.mWindowMap.containsKey(client.asBinder())) {
                    Slog.w("WindowManager", "Window " + client + " is already added");
                    return -5;
                } else {
                    if (type >= 1000 && type <= 1999) {
                        attachedWindow = windowForClientLocked(null, attrs.token, false);
                        if (attachedWindow == null) {
                            Slog.w("WindowManager", "Attempted to add window with token that is not a window: " + attrs.token + ".  Aborting.");
                            return -2;
                        } else if (attachedWindow.mAttrs.type >= 1000 && attachedWindow.mAttrs.type <= 1999) {
                            Slog.w("WindowManager", "Attempted to add window with token that is a sub-window: " + attrs.token + ".  Aborting.");
                            return -2;
                        }
                    }
                    if (type == 2030) {
                        if (!displayContent.isPrivate()) {
                            Slog.w("WindowManager", "Attempted to add private presentation window to a non-private display.  Aborting.");
                            return -8;
                        }
                    }
                    boolean addToken = false;
                    WindowToken token = (WindowToken) this.mTokenMap.get(attrs.token);
                    AppWindowToken atoken = null;
                    boolean addToastWindowRequiresToken = false;
                    if (token == null) {
                        if (type >= 1 && type <= 99) {
                            Slog.w("WindowManager", "Attempted to add application window with unknown token " + attrs.token + ".  Aborting.");
                            return -1;
                        } else if (type == 2011) {
                            Slog.w("WindowManager", "Attempted to add input method window with unknown token " + attrs.token + ".  Aborting.");
                            return -1;
                        } else if (type == 2031) {
                            Slog.w("WindowManager", "Attempted to add voice interaction window with unknown token " + attrs.token + ".  Aborting.");
                            return -1;
                        } else if (type == 2013) {
                            Slog.w("WindowManager", "Attempted to add wallpaper window with unknown token " + attrs.token + ".  Aborting.");
                            return -1;
                        } else if (type == 2023) {
                            Slog.w("WindowManager", "Attempted to add Dream window with unknown token " + attrs.token + ".  Aborting.");
                            return -1;
                        } else if (type == 2035) {
                            Slog.w("WindowManager", "Attempted to add QS dialog window with unknown token " + attrs.token + ".  Aborting.");
                            return -1;
                        } else if (type == 2032) {
                            Slog.w("WindowManager", "Attempted to add Accessibility overlay window with unknown token " + attrs.token + ".  Aborting.");
                            return -1;
                        } else {
                            if (type == 2005) {
                                if (doesAddToastWindowRequireToken(attrs.packageName, callingUid, attachedWindow)) {
                                    Slog.w("WindowManager", "Attempted to add a toast window with unknown token " + attrs.token + ".  Aborting.");
                                    return -1;
                                }
                            }
                            token = new WindowToken(this, attrs.token, -1, false);
                            addToken = true;
                        }
                    } else if (type >= 1 && type <= 99) {
                        atoken = token.appWindowToken;
                        if (atoken == null) {
                            Slog.w("WindowManager", "Attempted to add window with non-application token " + token + ".  Aborting.");
                            return -3;
                        } else if (atoken.removed) {
                            Slog.w("WindowManager", "Attempted to add window with exiting application token " + token + ".  Aborting.");
                            return -4;
                        } else if (type == 3) {
                            if (atoken.firstWindowDrawn) {
                                if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW || localLOGV) {
                                    Slog.v("WindowManager", "**** NO NEED TO START: " + attrs.getTitle());
                                }
                                return -6;
                            }
                        }
                    } else if (type == 2011) {
                        if (token.windowType != 2011) {
                            Slog.w("WindowManager", "Attempted to add input method window with bad token " + attrs.token + ".  Aborting.");
                            return -1;
                        }
                    } else if (type == 2031) {
                        if (token.windowType != 2031) {
                            Slog.w("WindowManager", "Attempted to add voice interaction window with bad token " + attrs.token + ".  Aborting.");
                            return -1;
                        }
                    } else if (type == 2013) {
                        if (token.windowType != 2013) {
                            Slog.w("WindowManager", "Attempted to add wallpaper window with bad token " + attrs.token + ".  Aborting.");
                            return -1;
                        }
                    } else if (type == 2023) {
                        if (token.windowType != 2023) {
                            Slog.w("WindowManager", "Attempted to add Dream window with bad token " + attrs.token + ".  Aborting.");
                            return -1;
                        }
                    } else if (type == 2032) {
                        if (token.windowType != 2032) {
                            Slog.w("WindowManager", "Attempted to add Accessibility overlay window with bad token " + attrs.token + ".  Aborting.");
                            return -1;
                        }
                    } else if (type == 2005) {
                        addToastWindowRequiresToken = doesAddToastWindowRequireToken(attrs.packageName, callingUid, attachedWindow);
                        if (addToastWindowRequiresToken && token.windowType != 2005) {
                            Slog.w("WindowManager", "Attempted to add a toast window with bad token " + attrs.token + ".  Aborting.");
                            return -1;
                        }
                    } else if (type == 2035) {
                        if (token.windowType != 2035) {
                            Slog.w("WindowManager", "Attempted to add QS dialog window with bad token " + attrs.token + ".  Aborting.");
                            return -1;
                        }
                    } else if (token.appWindowToken != null) {
                        Slog.w("WindowManager", "Non-null appWindowToken for system window of type=" + type);
                        attrs.token = null;
                        token = new WindowToken(this, null, -1, false);
                        addToken = true;
                    }
                    WindowState win = new WindowState(this, session, client, token, attachedWindow, appOp[0], seq, attrs, viewVisibility, displayContent);
                    if (WindowManagerDebugConfig.DEBUG_VISIBILITY || WindowManagerDebugConfig.DEBUG_ADD_REMOVE) {
                        Slog.v(TAG, "addWindow: callingPid " + Binder.getCallingPid() + " callingUid " + callingUid + " win " + win);
                    }
                    if (win.mDeathRecipient == null) {
                        Slog.w("WindowManager", "Adding window client " + client.asBinder() + " that is dead, aborting.");
                        return -4;
                    } else if (win.getDisplayContent() == null) {
                        Slog.w("WindowManager", "Adding window to Display that has been removed.");
                        return -9;
                    } else {
                        if (WindowManagerDebugConfig.DEBUG_ADD_REMOVE) {
                            Slog.d(TAG, "add window for clien:" + client + ", token:" + token);
                        }
                        this.mPolicy.adjustWindowParamsLw(win.mAttrs);
                        win.setShowToOwnerOnlyLocked(this.mPolicy.checkShowToOwnerOnly(attrs));
                        res = this.mPolicy.prepareAddWindowLw(win, attrs);
                        if (res != 0) {
                            return res;
                        }
                        boolean openInputChannels = outInputChannel != null ? (attrs.inputFeatures & 2) == 0 : false;
                        if (openInputChannels) {
                            try {
                                win.openInputChannel(outInputChannel);
                            } catch (Throwable e2) {
                                Slog.w(TAG, "handle Input channel erorr", e2);
                                return -11;
                            }
                        }
                        if (type == 2005) {
                            ApplicationInfo aInfo = getApplicationInfo(attrs.packageName, callingUid);
                            boolean nonSystemAppBeforeNougat = (aInfo == null || aInfo.targetSdkVersion >= 24) ? false : !aInfo.isSystemApp();
                            boolean appInWhiteList = aInfo != null ? (aInfo.privateFlags & DumpState.DUMP_INSTALLS) != 0 : false;
                            if (appInWhiteList) {
                                Slog.w("WindowManager", "Skip toast check for application in whitelist.");
                            } else {
                                if (!getDefaultDisplayContentLocked().canAddToastWindowForUid(callingUid, !nonSystemAppBeforeNougat)) {
                                    Slog.w("WindowManager", "Adding more than one toast window for UID at a time.");
                                    return -5;
                                } else if (nonSystemAppBeforeNougat) {
                                    Slog.w("WindowManager", "Skip focus check for application before nougat.");
                                } else {
                                    if (!addToastWindowRequiresToken) {
                                        if ((attrs.flags & 8) != 0) {
                                            if (this.mCurrentFocus != null) {
                                            }
                                        }
                                    }
                                    this.mH.sendMessageDelayed(this.mH.obtainMessage(52, win), win.mAttrs.hideTimeoutMilliseconds);
                                }
                            }
                        }
                        res = 0;
                        boolean inputWindow = false;
                        if (!(win.mAttachedWindow == null || win.mAttachedWindow.mAttrs == null || win.mAttachedWindow.mAttrs.type != 2011)) {
                            inputWindow = true;
                        }
                        if (excludeWindowTypeFromTapOutTask(type) || inputWindow || COLOROS_FLOAT.equals(win.mAttrs.getTitle()) || (type == 2014 && TICKER_PANEL.equals(win.mAttrs.getTitle()))) {
                            displayContent.mTapExcludedWindows.add(win);
                        }
                        long origId = Binder.clearCallingIdentity();
                        if (addToken) {
                            this.mTokenMap.put(attrs.token, token);
                        }
                        win.attach();
                        if (win.isAttachSuccess()) {
                            this.mWindowMap.put(client.asBinder(), win);
                            boolean isWinVisible = true;
                            if (win.mAppOp != -1) {
                                int startOpResult = this.mAppOps.startOpNoThrow(win.mAppOp, win.getOwningUid(), win.getOwningPackage());
                                if (startOpResult == 0 || startOpResult == 3) {
                                    if (45 == win.mAppOp && isUserToast.booleanValue() && OppoToastHelper.shouldCloseToast(this.mContext, win.getOwningPackage())) {
                                        if (!topAppName.equals(win.getOwningPackage())) {
                                            win.setAppOpVisibilityLw(false);
                                            isWinVisible = false;
                                        }
                                        this.mCheckedFloatWindowSet.add(win.getOwningPackage());
                                    }
                                } else if (24 != win.mAppOp) {
                                    win.setAppOpVisibilityLw(false);
                                    isWinVisible = false;
                                } else if (SystemProperties.getBoolean("persist.sys.permission.enable", false)) {
                                    if (!topAppName.equals(win.getOwningPackage())) {
                                        win.setAppOpVisibilityLw(false);
                                        this.mHandlerFloatWindow.sendMessage(this.mHandlerFloatWindow.obtainMessage(1, session));
                                        isWinVisible = false;
                                    }
                                    this.mCheckedFloatWindowSet.add(win.getOwningPackage());
                                }
                            } else if (win.isChildWindow()) {
                                WindowState parentWindow = win.mAttachedWindow;
                                if (!(parentWindow == null || parentWindow.mAppOp != 45 || parentWindow.mAppOpVisibility)) {
                                    win.setAppOpVisibilityLw(false);
                                }
                            }
                            if (NOTIFY_POPUP && isWinVisible) {
                                this.mHandlerFloatWindow.sendMessage(this.mHandlerFloatWindow.obtainMessage(4, new PopupInfo(1, win.getOwningPackage(), win.getOwningUid(), type)));
                            }
                            win.setForceHideNonSystemOverlayWindowIfNeeded(!this.mHidingNonSystemOverlayWindows.isEmpty());
                            if (type == 3 && token.appWindowToken != null) {
                                token.appWindowToken.startingWindow = win;
                                if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
                                    Slog.v("WindowManager", "addWindow: " + token.appWindowToken + " startingWindow=" + win);
                                }
                            }
                            boolean imMayMove = true;
                            if (type == 2004) {
                                Log.d(TAG, "add Keyguard Window");
                                this.mKeyguard = win;
                            }
                            if (type == 2011) {
                                win.mGivenInsetsPending = true;
                                this.mInputMethodWindow = win;
                                addInputMethodWindowToListLocked(win);
                                imMayMove = false;
                            } else if (type == 2012) {
                                this.mInputMethodDialogs.add(win);
                                addWindowToListInOrderLocked(win, true);
                                moveInputMethodDialogsLocked(findDesiredInputMethodWindowIndexLocked(true));
                                imMayMove = false;
                            } else {
                                addWindowToListInOrderLocked(win, true);
                                if (win.mAttrs != null && "com.coloros.securitypermission".equals(win.mAttrs.packageName)) {
                                    this.mDisableFrozenBySecuritypermission = true;
                                    this.mH.postDelayed(this.mDisableFrozenBySecurityTimeoutRunnable, 5000);
                                }
                                if (type == 2013) {
                                    this.mWallpaperControllerLocked.clearLastWallpaperTimeoutTime();
                                    displayContent.pendingLayoutChanges |= 4;
                                } else if ((attrs.flags & DumpState.DUMP_DEXOPT) != 0) {
                                    displayContent.pendingLayoutChanges |= 4;
                                } else if (this.mWallpaperControllerLocked.isBelowWallpaperTarget(win)) {
                                    displayContent.pendingLayoutChanges |= 4;
                                }
                            }
                            win.applyScrollIfNeeded();
                            win.applyAdjustForImeIfNeeded();
                            if (type == 2034) {
                                getDefaultDisplayContentLocked().getDockedDividerController().setWindow(win);
                            }
                            WindowStateAnimator winAnimator = win.mWinAnimator;
                            winAnimator.mEnterAnimationPending = true;
                            winAnimator.mEnteringAnimation = true;
                            if (!(atoken == null || prepareWindowReplacementTransition(atoken))) {
                                prepareNoneTransitionForRelaunching(atoken);
                            }
                            if (displayContent.isDefaultDisplay) {
                                Rect taskBounds;
                                DisplayInfo displayInfo = displayContent.getDisplayInfo();
                                if (atoken == null || atoken.mTask == null) {
                                    taskBounds = null;
                                } else {
                                    taskBounds = this.mTmpRect;
                                    atoken.mTask.getBounds(this.mTmpRect);
                                }
                                if (this.mPolicy.getInsetHintLw(win.mAttrs, taskBounds, this.mRotation, displayInfo.logicalWidth, displayInfo.logicalHeight, outContentInsets, outStableInsets, outOutsets)) {
                                    res = 4;
                                }
                            } else {
                                outContentInsets.setEmpty();
                                outStableInsets.setEmpty();
                            }
                            if (this.mInTouchMode) {
                                res |= 1;
                            }
                            if (win.mAppToken == null || !win.mAppToken.clientHidden) {
                                res |= 2;
                            }
                            this.mInputMonitor.setUpdateInputWindowsNeededLw();
                            boolean focusChanged = false;
                            if (win.canReceiveKeys()) {
                                focusChanged = updateFocusedWindowLocked(1, false);
                                if (focusChanged) {
                                    imMayMove = false;
                                }
                            }
                            if (imMayMove) {
                                moveInputMethodWindowsIfNeededLocked(false);
                            }
                            this.mLayersController.assignLayersLocked(displayContent.getWindowList());
                            if (focusChanged) {
                                this.mInputMonitor.setInputFocusLw(this.mCurrentFocus, false);
                            }
                            this.mInputMonitor.updateInputWindowsLw(false);
                            if (localLOGV || WindowManagerDebugConfig.DEBUG_ADD_REMOVE) {
                                Slog.v("WindowManager", "addWindow: New client " + client.asBinder() + ": window=" + win + " Callers=" + Debug.getCallers(5));
                            }
                            if (win.isVisibleOrAdding() && updateOrientationFromAppTokensLocked(false)) {
                                reportNewConfig = true;
                            }
                        } else {
                            Slog.e(TAG, "This win has not finish attach, should not add to map, aborting!");
                            if (addToken) {
                                this.mTokenMap.remove(attrs.token);
                            }
                            return -4;
                        }
                    }
                }
            }
            throw new IllegalStateException("Display has not been initialialized");
        }
    }

    private boolean doesAddToastWindowRequireToken(String packageName, int callingUid, WindowState attachedWindow) {
        boolean z = true;
        if (attachedWindow != null) {
            if (attachedWindow.mAppToken == null) {
                z = false;
            } else if (attachedWindow.mAppToken.targetSdk <= 25) {
                z = false;
            }
            return z;
        }
        try {
            ApplicationInfo appInfo = this.mContext.getPackageManager().getApplicationInfoAsUser(packageName, 0, UserHandle.getUserId(callingUid));
            if (appInfo.uid == callingUid) {
                return appInfo.targetSdkVersion > 25;
            } else {
                throw new SecurityException("Package " + packageName + " not in UID " + callingUid);
            }
        } catch (NameNotFoundException e) {
        }
    }

    private ApplicationInfo getApplicationInfo(String packageName, int callingUid) {
        try {
            return this.mContext.getPackageManager().getApplicationInfoAsUser(packageName, 0, UserHandle.getUserId(callingUid));
        } catch (NameNotFoundException e) {
            return null;
        }
    }

    private boolean prepareWindowReplacementTransition(AppWindowToken atoken) {
        atoken.clearAllDrawn();
        WindowState replacedWindow = null;
        for (int i = atoken.windows.size() - 1; i >= 0 && replacedWindow == null; i--) {
            WindowState candidate = (WindowState) atoken.windows.get(i);
            if (candidate.mAnimatingExit && candidate.mWillReplaceWindow && candidate.mAnimateReplacingWindow) {
                replacedWindow = candidate;
            }
        }
        if (replacedWindow == null) {
            return false;
        }
        Rect frame = replacedWindow.mVisibleFrame;
        this.mOpeningApps.add(atoken);
        prepareAppTransition(18, true);
        this.mAppTransition.overridePendingAppTransitionClipReveal(frame.left, frame.top, frame.width(), frame.height());
        executeAppTransition();
        return true;
    }

    private void prepareNoneTransitionForRelaunching(AppWindowToken atoken) {
        if (this.mDisplayFrozen && !this.mOpeningApps.contains(atoken) && atoken.isRelaunching()) {
            this.mOpeningApps.add(atoken);
            prepareAppTransition(0, false);
            executeAppTransition();
        }
    }

    boolean isScreenCaptureDisabledLocked(int userId) {
        Boolean disabled = (Boolean) this.mScreenCaptureDisabled.get(userId);
        if (disabled == null) {
            return false;
        }
        return disabled.booleanValue();
    }

    boolean isSecureLocked(WindowState w) {
        return (w.mAttrs.flags & DumpState.DUMP_PREFERRED_XML) != 0 || isScreenCaptureDisabledLocked(UserHandle.getUserId(w.mOwnerUid));
    }

    public void setScreenCaptureDisabled(int userId, boolean disabled) {
        if (Binder.getCallingUid() != 1000) {
            throw new SecurityException("Only system can call setScreenCaptureDisabled.");
        }
        boolean mCustomize = false;
        PackageManager mPackageManager = this.mContext.getPackageManager();
        if (mPackageManager != null && mPackageManager.hasSystemFeature("oppo.customize.function.control_capture")) {
            mCustomize = true;
        }
        synchronized (this.mWindowMap) {
            this.mScreenCaptureDisabled.put(userId, Boolean.valueOf(disabled));
            if (disabled && mCustomize) {
                SystemProperties.set("persist.sys.customize.forbcap", "true");
            } else {
                SystemProperties.set("persist.sys.customize.forbcap", "false");
            }
            for (int displayNdx = this.mDisplayContents.size() - 1; displayNdx >= 0; displayNdx--) {
                WindowList windows = ((DisplayContent) this.mDisplayContents.valueAt(displayNdx)).getWindowList();
                for (int winNdx = windows.size() - 1; winNdx >= 0; winNdx--) {
                    WindowState win = (WindowState) windows.get(winNdx);
                    if (win.mHasSurface && userId == UserHandle.getUserId(win.mOwnerUid)) {
                        win.mWinAnimator.setSecureLocked(disabled);
                    }
                }
            }
        }
    }

    private void setupWindowForRemoveOnExit(WindowState win) {
        win.mRemoveOnExit = true;
        win.setDisplayLayoutNeeded();
        boolean focusChanged = updateFocusedWindowLocked(3, false);
        this.mWindowPlacerLocked.performSurfacePlacement();
        if (focusChanged) {
            this.mInputMonitor.updateInputWindowsLw(false);
        }
    }

    public void removeWindow(Session session, IWindow client) {
        synchronized (this.mWindowMap) {
            WindowState win = windowForClientLocked(session, client, false);
            if (win == null) {
                return;
            }
            removeWindowLocked(win);
        }
    }

    void removeWindowLocked(WindowState win) {
        removeWindowLocked(win, false);
    }

    void removeWindowLocked(WindowState win, boolean keepVisibleDeadWindow) {
        win.mWindowRemovalAllowed = true;
        if (WindowManagerDebugConfig.DEBUG_ADD_REMOVE) {
            Slog.v(TAG, "removeWindowLocked: " + win + " callers=" + Debug.getCallers(4));
        }
        boolean startingWindow = win.mAttrs.type == 3;
        if (startingWindow && WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
            Slog.d("WindowManager", "Starting window removed " + win);
        }
        if (localLOGV || WindowManagerDebugConfig.DEBUG_FOCUS || (WindowManagerDebugConfig.DEBUG_FOCUS_LIGHT && win == this.mCurrentFocus)) {
            Slog.v("WindowManager", "Remove " + win + " client=" + Integer.toHexString(System.identityHashCode(win.mClient.asBinder())) + ", surfaceController=" + win.mWinAnimator.mSurfaceController + " Callers=" + Debug.getCallers(4));
        }
        long origId = Binder.clearCallingIdentity();
        win.disposeInputChannel();
        if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
            Object obj;
            boolean z;
            String str = "WindowManager";
            StringBuilder append = new StringBuilder().append("Remove ").append(win).append(": mSurfaceController=").append(win.mWinAnimator.mSurfaceController).append(" mAnimatingExit=").append(win.mAnimatingExit).append(" mRemoveOnExit=").append(win.mRemoveOnExit).append(" mHasSurface=").append(win.mHasSurface).append(" surfaceShowing=").append(win.mWinAnimator.getShown()).append(" isAnimationSet=").append(win.mWinAnimator.isAnimationSet()).append(" app-animation=");
            if (win.mAppToken != null) {
                obj = win.mAppToken.mAppAnimator.animation;
            } else {
                obj = null;
            }
            append = append.append(obj).append(" mWillReplaceWindow=").append(win.mWillReplaceWindow).append(" inPendingTransaction=");
            if (win.mAppToken != null) {
                z = win.mAppToken.inPendingTransaction;
            } else {
                z = false;
            }
            Slog.v(str, append.append(z).append(" mDisplayFrozen=").append(this.mDisplayFrozen).append(" callers=").append(Debug.getCallers(6)).toString());
        }
        boolean wasVisible = false;
        if (win.mHasSurface && okToDisplay()) {
            AppWindowToken appToken = win.mAppToken;
            if (win.mWillReplaceWindow) {
                if (WindowManagerDebugConfig.DEBUG_ADD_REMOVE) {
                    Slog.v("WindowManager", "Preserving " + win + " until the new one is " + "added");
                }
                win.mAnimatingExit = true;
                win.mReplacingRemoveRequested = true;
                Binder.restoreCallingIdentity(origId);
                return;
            } else if (!win.isAnimatingWithSavedSurface() || appToken.allDrawnExcludingSaved) {
                wasVisible = win.isWinVisibleLw();
                if (keepVisibleDeadWindow) {
                    if (WindowManagerDebugConfig.DEBUG_ADD_REMOVE) {
                        Slog.v("WindowManager", "Not removing " + win + " because app died while it's visible");
                    }
                    win.mAppDied = true;
                    win.setDisplayLayoutNeeded();
                    this.mWindowPlacerLocked.performSurfacePlacement();
                    win.openInputChannel(null);
                    this.mInputMonitor.updateInputWindowsLw(true);
                    Binder.restoreCallingIdentity(origId);
                    return;
                }
                WindowStateAnimator winAnimator = win.mWinAnimator;
                if (wasVisible) {
                    int transit = !startingWindow ? 2 : 5;
                    if (winAnimator.applyAnimationLocked(transit, false)) {
                        win.mAnimatingExit = true;
                    }
                    if (this.mAccessibilityController != null && win.getDisplayId() == 0) {
                        this.mAccessibilityController.onWindowTransitionLocked(win, transit);
                    }
                }
                boolean isAnimating = winAnimator.isAnimationSet() && !winAnimator.isDummyAnimation();
                boolean lastWindowIsStartingWindow = (!startingWindow || appToken == null) ? false : appToken.allAppWindows.size() == 1;
                if (winAnimator.getShown() && win.mAnimatingExit && (!lastWindowIsStartingWindow || isAnimating)) {
                    if (WindowManagerDebugConfig.DEBUG_ADD_REMOVE) {
                        Slog.v("WindowManager", "Not removing " + win + " due to exit animation ");
                    }
                    setupWindowForRemoveOnExit(win);
                    if (appToken != null) {
                        appToken.updateReportedVisibilityLocked();
                    }
                    Binder.restoreCallingIdentity(origId);
                    return;
                }
            } else {
                if (WindowManagerDebugConfig.DEBUG_ADD_REMOVE) {
                    Slog.d("WindowManager", "removeWindowLocked: delay removal of " + win + " due to early animation");
                }
                setupWindowForRemoveOnExit(win);
                Binder.restoreCallingIdentity(origId);
                return;
            }
        }
        removeWindowInnerLocked(win);
        if (wasVisible && updateOrientationFromAppTokensLocked(false)) {
            this.mH.sendEmptyMessage(18);
        }
        updateFocusedWindowLocked(0, true);
        Binder.restoreCallingIdentity(origId);
    }

    void removeWindowInnerLocked(WindowState win) {
        if (win.mRemoved) {
            if (WindowManagerDebugConfig.DEBUG_ADD_REMOVE) {
                Slog.v("WindowManager", "removeWindowInnerLocked: " + win + " Already removed...");
            }
            return;
        }
        while (win.mChildWindows.size() > 0) {
            WindowState cwin = (WindowState) win.mChildWindows.get(win.mChildWindows.size() - 1);
            Slog.w("WindowManager", "Force-removing child win " + cwin + " from container " + win);
            removeWindowInnerLocked(cwin);
        }
        win.mRemoved = true;
        if (this.mInputMethodTarget == win) {
            moveInputMethodWindowsIfNeededLocked(false);
        }
        int type = win.mAttrs.type;
        boolean inputWindow = false;
        if (!(win.mAttachedWindow == null || win.mAttachedWindow.mAttrs == null || win.mAttachedWindow.mAttrs.type != 2011)) {
            inputWindow = true;
        }
        if (excludeWindowTypeFromTapOutTask(type) || inputWindow || COLOROS_FLOAT.equals(win.mAttrs.getTitle()) || (type == 2014 && TICKER_PANEL.equals(win.mAttrs.getTitle()))) {
            win.getDisplayContent().mTapExcludedWindows.remove(win);
        }
        this.mPolicy.removeWindowLw(win);
        win.removeLocked();
        if (WindowManagerDebugConfig.DEBUG_ADD_REMOVE) {
            Slog.v("WindowManager", "removeWindowInnerLocked: " + win);
        }
        this.mWindowMap.remove(win.mClient.asBinder());
        if (win.mAppOp != -1) {
            this.mAppOps.finishOp(win.mAppOp, win.getOwningUid(), win.getOwningPackage());
        }
        this.mPendingRemove.remove(win);
        this.mResizingWindows.remove(win);
        updateNonSystemOverlayWindowsVisibilityIfNeeded(win, false);
        this.mWindowsChanged = true;
        if (WindowManagerDebugConfig.DEBUG_WINDOW_MOVEMENT) {
            Slog.v("WindowManager", "Final remove of window: " + win);
        }
        if (win.mAttrs != null && "com.coloros.securitypermission".equals(win.mAttrs.packageName)) {
            this.mDisableFrozenBySecuritypermission = false;
        }
        if (this.mInputMethodWindow == win) {
            this.mInputMethodWindow = null;
        } else if (win.mAttrs.type == 2012) {
            this.mInputMethodDialogs.remove(win);
        }
        WindowToken token = win.mToken;
        AppWindowToken atoken = win.mAppToken;
        if (WindowManagerDebugConfig.DEBUG_ADD_REMOVE) {
            Slog.v("WindowManager", "Removing " + win + " from " + token);
        }
        token.windows.remove(win);
        if (atoken != null) {
            atoken.allAppWindows.remove(win);
        }
        if (localLOGV) {
            Slog.v("WindowManager", "**** Removing window " + win + ": count=" + token.windows.size());
        }
        if (token.windows.size() == 0) {
            if (!token.explicit) {
                this.mTokenMap.remove(token.token);
            } else if (atoken != null) {
                atoken.firstWindowDrawn = false;
                atoken.clearAllDrawn();
            }
        }
        if (atoken != null) {
            if (atoken.startingWindow == win) {
                if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
                    Slog.v("WindowManager", "Notify removed startingWindow " + win);
                }
                scheduleRemoveStartingWindowLocked(atoken);
            } else if (atoken.allAppWindows.size() == 0 && atoken.startingData != null) {
                if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
                    Slog.v("WindowManager", "Nulling last startingWindow");
                }
                atoken.startingData = null;
            } else if (atoken.allAppWindows.size() == 1 && atoken.startingView != null) {
                scheduleRemoveStartingWindowLocked(atoken);
            }
        }
        DisplayContent defaultDisplayContentLocked;
        if (type == 2013) {
            this.mWallpaperControllerLocked.clearLastWallpaperTimeoutTime();
            defaultDisplayContentLocked = getDefaultDisplayContentLocked();
            defaultDisplayContentLocked.pendingLayoutChanges |= 4;
        } else if ((win.mAttrs.flags & DumpState.DUMP_DEXOPT) != 0) {
            defaultDisplayContentLocked = getDefaultDisplayContentLocked();
            defaultDisplayContentLocked.pendingLayoutChanges |= 4;
        }
        WindowList windows = win.getWindowList();
        if (windows != null) {
            windows.remove(win);
            if (NOTIFY_POPUP && win.mAppOpVisibility) {
                this.mHandlerFloatWindow.sendMessage(this.mHandlerFloatWindow.obtainMessage(4, new PopupInfo(0, win.getOwningPackage(), win.getOwningUid(), win.mAttrs.type)));
            }
            if (!this.mWindowPlacerLocked.isInLayout()) {
                this.mLayersController.assignLayersLocked(windows);
                win.setDisplayLayoutNeeded();
                if (this.mCurrentFocus == win) {
                    this.mFocusMayChange = true;
                }
                this.mWindowPlacerLocked.performSurfacePlacement();
                if (win.mAppToken != null) {
                    win.mAppToken.updateReportedVisibilityLocked();
                }
            }
        }
        this.mInputMonitor.updateInputWindowsLw(true);
        if (win.mAttrs.type == 2004) {
            Log.d(TAG, "remove Keyguard Window");
            this.mKeyguard = null;
        }
    }

    public void updateAppOpsState() {
        String topAppName = IElsaManager.EMPTY_PACKAGE;
        try {
            if (this.mActivityManager.getTopAppName() != null) {
                topAppName = this.mActivityManager.getTopAppName().getPackageName();
            }
        } catch (RemoteException e) {
        }
        synchronized (this.mWindowMap) {
            int numDisplays = this.mDisplayContents.size();
            boolean permission = SystemProperties.getBoolean("persist.sys.permission.enable", false);
            for (int displayNdx = 0; displayNdx < numDisplays; displayNdx++) {
                WindowList windows = ((DisplayContent) this.mDisplayContents.valueAt(displayNdx)).getWindowList();
                int numWindows = windows.size();
                for (int winNdx = 0; winNdx < numWindows; winNdx++) {
                    WindowState win = (WindowState) windows.get(winNdx);
                    if (win.mAppOp != -1) {
                        int mode = this.mAppOps.checkOpNoThrow(win.mAppOp, win.getOwningUid(), win.getOwningPackage());
                        boolean state;
                        if (24 == win.mAppOp) {
                            if (mode == 0 || mode == 3 || !permission) {
                                state = true;
                            } else {
                                state = topAppName.equals(win.getOwningPackage());
                            }
                            win.setAppOpVisibilityLw(state);
                            updateChildWindowState(win, state);
                            if ((mode == 0 || mode == 3) && this.mCheckedFloatWindowSet.contains(win.getOwningPackage())) {
                                this.mCheckedFloatWindowSet.remove(win.getOwningPackage());
                            } else if (!(mode == 0 || mode == 3 || this.mCheckedFloatWindowSet.contains(win.getOwningPackage()))) {
                                this.mCheckedFloatWindowSet.add(win.getOwningPackage());
                            }
                        } else if (45 == win.mAppOp) {
                            boolean isPkgToastClosed = OppoToastHelper.isPackageToastClosed(win.getOwningPackage());
                            if (mode == 0 || mode == 3) {
                                state = isPkgToastClosed ? topAppName.equals(win.getOwningPackage()) : true;
                            } else {
                                state = false;
                            }
                            win.setAppOpVisibilityLw(state);
                            updateChildWindowState(win, state);
                            if (!isPkgToastClosed && this.mCheckedFloatWindowSet.contains(win.getOwningPackage())) {
                                this.mCheckedFloatWindowSet.remove(win.getOwningPackage());
                            } else if (isPkgToastClosed && !this.mCheckedFloatWindowSet.contains(win.getOwningPackage())) {
                                this.mCheckedFloatWindowSet.add(win.getOwningPackage());
                            }
                        } else {
                            boolean z = mode != 0 ? mode == 3 : true;
                            win.setAppOpVisibilityLw(z);
                        }
                    }
                }
            }
        }
    }

    public void updateAppOpsState(String packageName, Boolean state) {
        synchronized (this.mWindowMap) {
            int numDisplays = this.mDisplayContents.size();
            boolean permission = SystemProperties.getBoolean("persist.sys.permission.enable", false);
            for (int displayNdx = 0; displayNdx < numDisplays; displayNdx++) {
                WindowList windows = ((DisplayContent) this.mDisplayContents.valueAt(displayNdx)).getWindowList();
                int numWindows = windows.size();
                for (int winNdx = 0; winNdx < numWindows; winNdx++) {
                    WindowState win = (WindowState) windows.get(winNdx);
                    if (!(win.getOwningPackage() == null || packageName == null || !win.getOwningPackage().equals(packageName) || win.mAppOp == -1)) {
                        boolean z;
                        if (24 == win.mAppOp) {
                            if (state.booleanValue() || !permission) {
                                z = true;
                            } else {
                                z = false;
                            }
                            win.setAppOpVisibilityLw(z);
                            Slog.d(TAG, "updateAppOpsState, packageName : " + packageName + " , state : " + state);
                        } else if (45 == win.mAppOp) {
                            if (state.booleanValue() || !permission) {
                                z = true;
                            } else {
                                z = false;
                            }
                            win.setAppOpVisibilityLw(z);
                            Slog.d(TAG, "updateAppOpsState, packageName : " + packageName + " , state : " + state);
                        }
                        if (state.booleanValue() || !permission) {
                            z = true;
                        } else {
                            z = false;
                        }
                        updateChildWindowState(win, z);
                    }
                }
            }
        }
    }

    private void updateChildWindowState(WindowState win, boolean state) {
        if (win.mChildWindows != null && !win.mChildWindows.isEmpty()) {
            WindowList childWindows = win.mChildWindows;
            int numChildWindows = childWindows.size();
            int i = 0;
            while (i < numChildWindows) {
                try {
                    ((WindowState) childWindows.get(i)).setAppOpVisibilityLw(state);
                    i++;
                } catch (Exception e) {
                    e.printStackTrace();
                    Slog.e(TAG, "updateAppOpsState error!");
                    return;
                }
            }
            final String packageName = win.getOwningPackage();
            if (!state) {
                this.mHandlerFloatWindow.post(new Runnable() {
                    public void run() {
                        Intent intent = new Intent("com.oppo.permissionprotect.notify");
                        intent.putExtra("PackageName", packageName);
                        intent.putExtra("Permission", "android.permission.SYSTEM_ALERT_WINDOW");
                        intent.putExtra(HandlerFloatWindow.TAG_TYPE, HandlerFloatWindow.TYPE_CHILD_WINDOW);
                        WindowManagerService.this.mContext.sendBroadcast(intent);
                    }
                });
            }
            Slog.d(TAG, "child win of : " + packageName + " , state " + state);
        }
    }

    static void logSurface(WindowState w, String msg, boolean withStackTrace) {
        String str = "  SURFACE " + msg + ": " + w;
        if (withStackTrace) {
            logWithStack(TAG, str);
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

    void setTransparentRegionWindow(Session session, IWindow client, Region region) {
        long origId = Binder.clearCallingIdentity();
        try {
            synchronized (this.mWindowMap) {
                WindowState w = windowForClientLocked(session, client, false);
                if (WindowManagerDebugConfig.SHOW_TRANSACTIONS) {
                    logSurface(w, "transparentRegionHint=" + region, false);
                }
                if (w != null && w.mHasSurface) {
                    w.mWinAnimator.setTransparentRegionHintLocked(region);
                }
            }
        } finally {
            Binder.restoreCallingIdentity(origId);
        }
    }

    void setInsetsWindow(Session session, IWindow client, int touchableInsets, Rect contentInsets, Rect visibleInsets, Region touchableRegion) {
        long origId = Binder.clearCallingIdentity();
        try {
            synchronized (this.mWindowMap) {
                WindowState w = windowForClientLocked(session, client, false);
                if (w != null) {
                    if (WindowManagerDebugConfig.DEBUG_LAYOUT) {
                        Slog.d(TAG, "setInsetsWindow " + w + ", contentInsets=" + w.mGivenContentInsets + " -> " + contentInsets + ", visibleInsets=" + w.mGivenVisibleInsets + " -> " + visibleInsets + ", touchableRegion=" + w.mGivenTouchableRegion + " -> " + touchableRegion + ", touchableInsets " + w.mTouchableInsets + " -> " + touchableInsets);
                    }
                    w.mGivenInsetsPending = false;
                    w.mGivenContentInsets.set(contentInsets);
                    w.mGivenVisibleInsets.set(visibleInsets);
                    w.mGivenTouchableRegion.set(touchableRegion);
                    w.mTouchableInsets = touchableInsets;
                    if (w.mGlobalScale != 1.0f) {
                        w.mGivenContentInsets.scale(w.mGlobalScale);
                        w.mGivenVisibleInsets.scale(w.mGlobalScale);
                        w.mGivenTouchableRegion.scale(w.mGlobalScale);
                    }
                    w.setDisplayLayoutNeeded();
                    this.mWindowPlacerLocked.performSurfacePlacement();
                } else if (WindowManagerDebugConfig.DEBUG_LAYOUT) {
                    Slog.d(TAG, "setInsetsWindow session =" + session + ", client = " + client);
                }
            }
        } finally {
            Binder.restoreCallingIdentity(origId);
        }
    }

    public void getWindowDisplayFrame(Session session, IWindow client, Rect outDisplayFrame) {
        synchronized (this.mWindowMap) {
            WindowState win = windowForClientLocked(session, client, false);
            if (win == null) {
                outDisplayFrame.setEmpty();
                return;
            }
            outDisplayFrame.set(win.mDisplayFrame);
        }
    }

    public void onRectangleOnScreenRequested(IBinder token, Rect rectangle) {
        synchronized (this.mWindowMap) {
            if (this.mAccessibilityController != null) {
                WindowState window = (WindowState) this.mWindowMap.get(token);
                if (window != null && window.getDisplayId() == 0) {
                    this.mAccessibilityController.onRectangleOnScreenRequestedLocked(rectangle);
                }
            }
        }
    }

    public IWindowId getWindowId(IBinder token) {
        IWindowId iWindowId = null;
        synchronized (this.mWindowMap) {
            WindowState window = (WindowState) this.mWindowMap.get(token);
            if (window != null) {
                iWindowId = window.mWindowId;
            }
        }
        return iWindowId;
    }

    public void pokeDrawLock(Session session, IBinder token) {
        synchronized (this.mWindowMap) {
            WindowState window = windowForClientLocked(session, token, false);
            if (window != null) {
                window.pokeDrawLockLw(this.mDrawLockTimeoutMillis);
            }
        }
    }

    void repositionChild(Session session, IWindow client, int left, int top, int right, int bottom, long frameNumber, Rect outFrame) {
        Trace.traceBegin(32, "repositionChild");
        long origId = Binder.clearCallingIdentity();
        try {
            synchronized (this.mWindowMap) {
                WindowState win = windowForClientLocked(session, client, false);
                if (win == null) {
                    Binder.restoreCallingIdentity(origId);
                    Trace.traceEnd(32);
                } else if (win.mAttachedWindow == null) {
                    throw new IllegalArgumentException("repositionChild called but window is notattached to a parent win=" + win);
                } else {
                    win.mAttrs.x = left;
                    win.mAttrs.y = top;
                    win.mAttrs.width = right - left;
                    win.mAttrs.height = bottom - top;
                    win.setWindowScale(win.mRequestedWidth, win.mRequestedHeight);
                    if (win.mHasSurface) {
                        if (WindowManagerDebugConfig.SHOW_TRANSACTIONS) {
                            Slog.i("WindowManager", ">>> OPEN TRANSACTION repositionChild");
                        }
                        SurfaceControl.openTransaction();
                        win.applyGravityAndUpdateFrame(win.mContainingFrame, win.mDisplayFrame);
                        win.mWinAnimator.computeShownFrameLocked();
                        win.mWinAnimator.setSurfaceBoundariesLocked(false);
                        if (frameNumber > 0) {
                            win.mWinAnimator.deferTransactionUntilParentFrame(frameNumber);
                        }
                        SurfaceControl.closeTransaction();
                        if (WindowManagerDebugConfig.SHOW_TRANSACTIONS) {
                            Slog.i("WindowManager", "<<< CLOSE TRANSACTION repositionChild");
                        }
                    }
                    outFrame = win.mCompatFrame;
                    Binder.restoreCallingIdentity(origId);
                    Trace.traceEnd(32);
                }
            }
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(origId);
            Trace.traceEnd(32);
        }
    }

    /*  JADX ERROR: JadxRuntimeException in pass: RegionMakerVisitor
        jadx.core.utils.exceptions.JadxRuntimeException: Exception block dominator not found, method:com.android.server.wm.WindowManagerService.relayoutWindow(com.android.server.wm.Session, android.view.IWindow, int, android.view.WindowManager$LayoutParams, int, int, int, int, android.graphics.Rect, android.graphics.Rect, android.graphics.Rect, android.graphics.Rect, android.graphics.Rect, android.graphics.Rect, android.graphics.Rect, android.content.res.Configuration, android.view.Surface):int, dom blocks: [B:200:0x0560, B:230:0x0655]
        	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.searchTryCatchDominators(ProcessTryCatchRegions.java:89)
        	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.process(ProcessTryCatchRegions.java:45)
        	at jadx.core.dex.visitors.regions.RegionMakerVisitor.postProcessRegions(RegionMakerVisitor.java:63)
        	at jadx.core.dex.visitors.regions.RegionMakerVisitor.visit(RegionMakerVisitor.java:58)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:27)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$1(DepthTraversal.java:14)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public int relayoutWindow(com.android.server.wm.Session r31, android.view.IWindow r32, int r33, android.view.WindowManager.LayoutParams r34, int r35, int r36, int r37, int r38, android.graphics.Rect r39, android.graphics.Rect r40, android.graphics.Rect r41, android.graphics.Rect r42, android.graphics.Rect r43, android.graphics.Rect r44, android.graphics.Rect r45, android.content.res.Configuration r46, android.view.Surface r47) {
        /*
        r30 = this;
        r6 = 0;
        r0 = r30;
        r4 = r0.mContext;
        r5 = "android.permission.STATUS_BAR";
        r4 = r4.checkCallingOrSelfPermission(r5);
        if (r4 != 0) goto L_0x0042;
    L_0x000e:
        r16 = 1;
    L_0x0010:
        r20 = android.os.Binder.clearCallingIdentity();
        r4 = com.android.server.wm.OppoInterceptWindow.getInstance();
        r0 = r30;
        r5 = r0.mContext;
        r0 = r30;
        r0 = r0.mPolicy;
        r26 = r0;
        r0 = r31;
        r1 = r34;
        r2 = r26;
        r4.getRunningAppList(r5, r0, r1, r2);
        r0 = r30;
        r0 = r0.mWindowMap;
        r26 = r0;
        monitor-enter(r26);
        r4 = 0;
        r0 = r30;	 Catch:{ all -> 0x0103 }
        r1 = r31;	 Catch:{ all -> 0x0103 }
        r2 = r32;	 Catch:{ all -> 0x0103 }
        r7 = r0.windowForClientLocked(r1, r2, r4);	 Catch:{ all -> 0x0103 }
        if (r7 != 0) goto L_0x0045;
    L_0x003f:
        r4 = 0;
        monitor-exit(r26);
        return r4;
    L_0x0042:
        r16 = 0;
        goto L_0x0010;
    L_0x0045:
        r8 = r7.mWinAnimator;	 Catch:{ all -> 0x0103 }
        r4 = 8;	 Catch:{ all -> 0x0103 }
        r0 = r37;	 Catch:{ all -> 0x0103 }
        if (r0 == r4) goto L_0x0054;	 Catch:{ all -> 0x0103 }
    L_0x004d:
        r0 = r35;	 Catch:{ all -> 0x0103 }
        r1 = r36;	 Catch:{ all -> 0x0103 }
        r7.setRequestedSize(r0, r1);	 Catch:{ all -> 0x0103 }
    L_0x0054:
        r9 = 0;	 Catch:{ all -> 0x0103 }
        r14 = 0;	 Catch:{ all -> 0x0103 }
        if (r34 == 0) goto L_0x014b;	 Catch:{ all -> 0x0103 }
    L_0x0058:
        r0 = r30;	 Catch:{ all -> 0x0103 }
        r4 = r0.mPolicy;	 Catch:{ all -> 0x0103 }
        r0 = r34;	 Catch:{ all -> 0x0103 }
        r4.adjustWindowParamsLw(r0);	 Catch:{ all -> 0x0103 }
        r4 = r7.mSeq;	 Catch:{ all -> 0x0103 }
        r0 = r33;	 Catch:{ all -> 0x0103 }
        if (r0 != r4) goto L_0x0082;	 Catch:{ all -> 0x0103 }
    L_0x0067:
        r0 = r34;	 Catch:{ all -> 0x0103 }
        r4 = r0.systemUiVisibility;	 Catch:{ all -> 0x0103 }
        r0 = r34;	 Catch:{ all -> 0x0103 }
        r5 = r0.subtreeSystemUiVisibility;	 Catch:{ all -> 0x0103 }
        r22 = r4 | r5;	 Catch:{ all -> 0x0103 }
        r4 = 67043328; // 0x3ff0000 float:1.4987553E-36 double:3.3123805E-316;	 Catch:{ all -> 0x0103 }
        r4 = r4 & r22;	 Catch:{ all -> 0x0103 }
        if (r4 == 0) goto L_0x007e;	 Catch:{ all -> 0x0103 }
    L_0x0077:
        if (r16 != 0) goto L_0x007e;	 Catch:{ all -> 0x0103 }
    L_0x0079:
        r4 = -67043329; // 0xfffffffffc00ffff float:-2.679225E36 double:NaN;	 Catch:{ all -> 0x0103 }
        r22 = r22 & r4;	 Catch:{ all -> 0x0103 }
    L_0x007e:
        r0 = r22;	 Catch:{ all -> 0x0103 }
        r7.mSystemUiVisibility = r0;	 Catch:{ all -> 0x0103 }
    L_0x0082:
        r4 = r7.mAttrs;	 Catch:{ all -> 0x0103 }
        r4 = r4.type;	 Catch:{ all -> 0x0103 }
        r0 = r34;	 Catch:{ all -> 0x0103 }
        r5 = r0.type;	 Catch:{ all -> 0x0103 }
        if (r4 == r5) goto L_0x0106;	 Catch:{ all -> 0x0103 }
    L_0x008c:
        r4 = TAG;	 Catch:{ all -> 0x0103 }
        r5 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0103 }
        r5.<init>();	 Catch:{ all -> 0x0103 }
        r27 = "Window : ";	 Catch:{ all -> 0x0103 }
        r0 = r27;	 Catch:{ all -> 0x0103 }
        r5 = r5.append(r0);	 Catch:{ all -> 0x0103 }
        r5 = r5.append(r7);	 Catch:{ all -> 0x0103 }
        r27 = "changes the window type!!";	 Catch:{ all -> 0x0103 }
        r0 = r27;	 Catch:{ all -> 0x0103 }
        r5 = r5.append(r0);	 Catch:{ all -> 0x0103 }
        r5 = r5.toString();	 Catch:{ all -> 0x0103 }
        android.util.Slog.e(r4, r5);	 Catch:{ all -> 0x0103 }
        r4 = TAG;	 Catch:{ all -> 0x0103 }
        r5 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0103 }
        r5.<init>();	 Catch:{ all -> 0x0103 }
        r27 = "Original type : ";	 Catch:{ all -> 0x0103 }
        r0 = r27;	 Catch:{ all -> 0x0103 }
        r5 = r5.append(r0);	 Catch:{ all -> 0x0103 }
        r0 = r7.mAttrs;	 Catch:{ all -> 0x0103 }
        r27 = r0;	 Catch:{ all -> 0x0103 }
        r0 = r27;	 Catch:{ all -> 0x0103 }
        r0 = r0.type;	 Catch:{ all -> 0x0103 }
        r27 = r0;	 Catch:{ all -> 0x0103 }
        r0 = r27;	 Catch:{ all -> 0x0103 }
        r5 = r5.append(r0);	 Catch:{ all -> 0x0103 }
        r5 = r5.toString();	 Catch:{ all -> 0x0103 }
        android.util.Slog.e(r4, r5);	 Catch:{ all -> 0x0103 }
        r4 = TAG;	 Catch:{ all -> 0x0103 }
        r5 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0103 }
        r5.<init>();	 Catch:{ all -> 0x0103 }
        r27 = "Changed type : ";	 Catch:{ all -> 0x0103 }
        r0 = r27;	 Catch:{ all -> 0x0103 }
        r5 = r5.append(r0);	 Catch:{ all -> 0x0103 }
        r0 = r34;	 Catch:{ all -> 0x0103 }
        r0 = r0.type;	 Catch:{ all -> 0x0103 }
        r27 = r0;	 Catch:{ all -> 0x0103 }
        r0 = r27;	 Catch:{ all -> 0x0103 }
        r5 = r5.append(r0);	 Catch:{ all -> 0x0103 }
        r5 = r5.toString();	 Catch:{ all -> 0x0103 }
        android.util.Slog.e(r4, r5);	 Catch:{ all -> 0x0103 }
        r4 = new java.lang.IllegalArgumentException;	 Catch:{ all -> 0x0103 }
        r5 = "Window type can not be changed after the window is added.";	 Catch:{ all -> 0x0103 }
        r4.<init>(r5);	 Catch:{ all -> 0x0103 }
        throw r4;	 Catch:{ all -> 0x0103 }
    L_0x0103:
        r4 = move-exception;
        monitor-exit(r26);
        throw r4;
    L_0x0106:
        r0 = r34;	 Catch:{ all -> 0x0103 }
        r4 = r0.privateFlags;	 Catch:{ all -> 0x0103 }
        r4 = r4 & 8192;	 Catch:{ all -> 0x0103 }
        if (r4 == 0) goto L_0x012e;	 Catch:{ all -> 0x0103 }
    L_0x010e:
        r4 = r7.mAttrs;	 Catch:{ all -> 0x0103 }
        r4 = r4.x;	 Catch:{ all -> 0x0103 }
        r0 = r34;	 Catch:{ all -> 0x0103 }
        r0.x = r4;	 Catch:{ all -> 0x0103 }
        r4 = r7.mAttrs;	 Catch:{ all -> 0x0103 }
        r4 = r4.y;	 Catch:{ all -> 0x0103 }
        r0 = r34;	 Catch:{ all -> 0x0103 }
        r0.y = r4;	 Catch:{ all -> 0x0103 }
        r4 = r7.mAttrs;	 Catch:{ all -> 0x0103 }
        r4 = r4.width;	 Catch:{ all -> 0x0103 }
        r0 = r34;	 Catch:{ all -> 0x0103 }
        r0.width = r4;	 Catch:{ all -> 0x0103 }
        r4 = r7.mAttrs;	 Catch:{ all -> 0x0103 }
        r4 = r4.height;	 Catch:{ all -> 0x0103 }
        r0 = r34;	 Catch:{ all -> 0x0103 }
        r0.height = r4;	 Catch:{ all -> 0x0103 }
    L_0x012e:
        r4 = r7.mAttrs;	 Catch:{ all -> 0x0103 }
        r5 = r4.flags;	 Catch:{ all -> 0x0103 }
        r0 = r34;	 Catch:{ all -> 0x0103 }
        r0 = r0.flags;	 Catch:{ all -> 0x0103 }
        r27 = r0;	 Catch:{ all -> 0x0103 }
        r14 = r5 ^ r27;	 Catch:{ all -> 0x0103 }
        r4.flags = r14;	 Catch:{ all -> 0x0103 }
        r4 = r7.mAttrs;	 Catch:{ all -> 0x0103 }
        r0 = r34;	 Catch:{ all -> 0x0103 }
        r9 = r4.copyFrom(r0);	 Catch:{ all -> 0x0103 }
        r4 = r9 & 16385;	 Catch:{ all -> 0x0103 }
        if (r4 == 0) goto L_0x014b;	 Catch:{ all -> 0x0103 }
    L_0x0148:
        r4 = 1;	 Catch:{ all -> 0x0103 }
        r7.mLayoutNeeded = r4;	 Catch:{ all -> 0x0103 }
    L_0x014b:
        r4 = com.android.server.wm.WindowManagerDebugConfig.DEBUG_LAYOUT;	 Catch:{ all -> 0x0103 }
        if (r4 != 0) goto L_0x0153;	 Catch:{ all -> 0x0103 }
    L_0x014f:
        r4 = DEBUG_WMS;	 Catch:{ all -> 0x0103 }
        if (r4 == 0) goto L_0x01af;	 Catch:{ all -> 0x0103 }
    L_0x0153:
        r4 = "WindowManager";	 Catch:{ all -> 0x0103 }
        r5 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0103 }
        r5.<init>();	 Catch:{ all -> 0x0103 }
        r27 = "Relayout ";	 Catch:{ all -> 0x0103 }
        r0 = r27;	 Catch:{ all -> 0x0103 }
        r5 = r5.append(r0);	 Catch:{ all -> 0x0103 }
        r5 = r5.append(r7);	 Catch:{ all -> 0x0103 }
        r27 = ": viewVisibility=";	 Catch:{ all -> 0x0103 }
        r0 = r27;	 Catch:{ all -> 0x0103 }
        r5 = r5.append(r0);	 Catch:{ all -> 0x0103 }
        r0 = r37;	 Catch:{ all -> 0x0103 }
        r5 = r5.append(r0);	 Catch:{ all -> 0x0103 }
        r27 = " req=";	 Catch:{ all -> 0x0103 }
        r0 = r27;	 Catch:{ all -> 0x0103 }
        r5 = r5.append(r0);	 Catch:{ all -> 0x0103 }
        r0 = r35;	 Catch:{ all -> 0x0103 }
        r5 = r5.append(r0);	 Catch:{ all -> 0x0103 }
        r27 = "x";	 Catch:{ all -> 0x0103 }
        r0 = r27;	 Catch:{ all -> 0x0103 }
        r5 = r5.append(r0);	 Catch:{ all -> 0x0103 }
        r0 = r36;	 Catch:{ all -> 0x0103 }
        r5 = r5.append(r0);	 Catch:{ all -> 0x0103 }
        r27 = " ";	 Catch:{ all -> 0x0103 }
        r0 = r27;	 Catch:{ all -> 0x0103 }
        r5 = r5.append(r0);	 Catch:{ all -> 0x0103 }
        r0 = r7.mAttrs;	 Catch:{ all -> 0x0103 }
        r27 = r0;	 Catch:{ all -> 0x0103 }
        r0 = r27;	 Catch:{ all -> 0x0103 }
        r5 = r5.append(r0);	 Catch:{ all -> 0x0103 }
        r5 = r5.toString();	 Catch:{ all -> 0x0103 }
        android.util.Slog.v(r4, r5);	 Catch:{ all -> 0x0103 }
    L_0x01af:
        r4 = com.android.server.wm.WindowManagerDebugConfig.DEBUG_LAYOUT;	 Catch:{ all -> 0x0103 }
        if (r4 == 0) goto L_0x01d0;	 Catch:{ all -> 0x0103 }
    L_0x01b3:
        r4 = TAG;	 Catch:{ all -> 0x0103 }
        r5 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0103 }
        r5.<init>();	 Catch:{ all -> 0x0103 }
        r27 = "Input attr :";	 Catch:{ all -> 0x0103 }
        r0 = r27;	 Catch:{ all -> 0x0103 }
        r5 = r5.append(r0);	 Catch:{ all -> 0x0103 }
        r0 = r34;	 Catch:{ all -> 0x0103 }
        r5 = r5.append(r0);	 Catch:{ all -> 0x0103 }
        r5 = r5.toString();	 Catch:{ all -> 0x0103 }
        android.util.Slog.v(r4, r5);	 Catch:{ all -> 0x0103 }
    L_0x01d0:
        r4 = r38 & 2;	 Catch:{ all -> 0x0103 }
        if (r4 == 0) goto L_0x04e8;	 Catch:{ all -> 0x0103 }
    L_0x01d4:
        r4 = 1;	 Catch:{ all -> 0x0103 }
    L_0x01d5:
        r8.mSurfaceDestroyDeferred = r4;	 Catch:{ all -> 0x0103 }
        r4 = r38 & 2;	 Catch:{ all -> 0x0103 }
        if (r4 == 0) goto L_0x04eb;	 Catch:{ all -> 0x0103 }
    L_0x01db:
        r4 = 1;	 Catch:{ all -> 0x0103 }
    L_0x01dc:
        r8.mDestroyDeferredFlag = r4;	 Catch:{ all -> 0x0103 }
        r4 = r7.mAttrs;	 Catch:{ all -> 0x0103 }
        r4 = r4.privateFlags;	 Catch:{ all -> 0x0103 }
        r4 = r4 & 128;	 Catch:{ all -> 0x0103 }
        if (r4 == 0) goto L_0x04ee;	 Catch:{ all -> 0x0103 }
    L_0x01e6:
        r4 = 1;	 Catch:{ all -> 0x0103 }
    L_0x01e7:
        r7.mEnforceSizeCompat = r4;	 Catch:{ all -> 0x0103 }
        r4 = r9 & 128;	 Catch:{ all -> 0x0103 }
        if (r4 == 0) goto L_0x0244;	 Catch:{ all -> 0x0103 }
    L_0x01ed:
        r4 = r8.isKeyguard();	 Catch:{ all -> 0x0103 }
        if (r4 == 0) goto L_0x023e;	 Catch:{ all -> 0x0103 }
    L_0x01f3:
        r4 = com.android.server.lights.LightsManager.class;	 Catch:{ all -> 0x0103 }
        r19 = com.android.server.LocalServices.getService(r4);	 Catch:{ all -> 0x0103 }
        r19 = (com.android.server.lights.LightsManager) r19;	 Catch:{ all -> 0x0103 }
        r0 = r34;	 Catch:{ all -> 0x0103 }
        r4 = r0.alpha;	 Catch:{ all -> 0x0103 }
        r0 = r19;	 Catch:{ all -> 0x0103 }
        r0.setKeyguardWindowAlpha(r4);	 Catch:{ all -> 0x0103 }
        r4 = com.android.server.wm.WindowManagerDebugConfig.DEBUG_LAYOUT;	 Catch:{ all -> 0x0103 }
        if (r4 == 0) goto L_0x023e;	 Catch:{ all -> 0x0103 }
    L_0x0208:
        r4 = TAG;	 Catch:{ all -> 0x0103 }
        r5 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0103 }
        r5.<init>();	 Catch:{ all -> 0x0103 }
        r27 = "set_Alpha current mAlpha = ";	 Catch:{ all -> 0x0103 }
        r0 = r27;	 Catch:{ all -> 0x0103 }
        r5 = r5.append(r0);	 Catch:{ all -> 0x0103 }
        r0 = r8.mAlpha;	 Catch:{ all -> 0x0103 }
        r27 = r0;	 Catch:{ all -> 0x0103 }
        r0 = r27;	 Catch:{ all -> 0x0103 }
        r5 = r5.append(r0);	 Catch:{ all -> 0x0103 }
        r27 = ", new mAlpha = ";	 Catch:{ all -> 0x0103 }
        r0 = r27;	 Catch:{ all -> 0x0103 }
        r5 = r5.append(r0);	 Catch:{ all -> 0x0103 }
        r0 = r34;	 Catch:{ all -> 0x0103 }
        r0 = r0.alpha;	 Catch:{ all -> 0x0103 }
        r27 = r0;	 Catch:{ all -> 0x0103 }
        r0 = r27;	 Catch:{ all -> 0x0103 }
        r5 = r5.append(r0);	 Catch:{ all -> 0x0103 }
        r5 = r5.toString();	 Catch:{ all -> 0x0103 }
        android.util.Slog.d(r4, r5);	 Catch:{ all -> 0x0103 }
    L_0x023e:
        r0 = r34;	 Catch:{ all -> 0x0103 }
        r4 = r0.alpha;	 Catch:{ all -> 0x0103 }
        r8.mAlpha = r4;	 Catch:{ all -> 0x0103 }
    L_0x0244:
        r4 = r7.mRequestedWidth;	 Catch:{ all -> 0x0103 }
        r5 = r7.mRequestedHeight;	 Catch:{ all -> 0x0103 }
        r7.setWindowScale(r4, r5);	 Catch:{ all -> 0x0103 }
        r4 = r7.mAttrs;	 Catch:{ all -> 0x0103 }
        r4 = r4.surfaceInsets;	 Catch:{ all -> 0x0103 }
        r4 = r4.left;	 Catch:{ all -> 0x0103 }
        if (r4 != 0) goto L_0x025b;	 Catch:{ all -> 0x0103 }
    L_0x0253:
        r4 = r7.mAttrs;	 Catch:{ all -> 0x0103 }
        r4 = r4.surfaceInsets;	 Catch:{ all -> 0x0103 }
        r4 = r4.top;	 Catch:{ all -> 0x0103 }
        if (r4 == 0) goto L_0x04f1;	 Catch:{ all -> 0x0103 }
    L_0x025b:
        r4 = 0;	 Catch:{ all -> 0x0103 }
        r8.setOpaqueLocked(r4);	 Catch:{ all -> 0x0103 }
    L_0x025f:
        r4 = 131080; // 0x20008 float:1.83682E-40 double:6.4762E-319;	 Catch:{ all -> 0x0103 }
        r4 = r4 & r14;	 Catch:{ all -> 0x0103 }
        if (r4 == 0) goto L_0x0503;	 Catch:{ all -> 0x0103 }
    L_0x0265:
        r17 = 1;	 Catch:{ all -> 0x0103 }
    L_0x0267:
        r18 = r7.isDefaultDisplay();	 Catch:{ all -> 0x0103 }
        if (r18 == 0) goto L_0x0511;	 Catch:{ all -> 0x0103 }
    L_0x026d:
        r4 = r7.mViewVisibility;	 Catch:{ all -> 0x0103 }
        r0 = r37;	 Catch:{ all -> 0x0103 }
        if (r4 != r0) goto L_0x0277;	 Catch:{ all -> 0x0103 }
    L_0x0273:
        r4 = r14 & 8;	 Catch:{ all -> 0x0103 }
        if (r4 == 0) goto L_0x0507;	 Catch:{ all -> 0x0103 }
    L_0x0277:
        r15 = 1;	 Catch:{ all -> 0x0103 }
    L_0x0278:
        r4 = r7.mViewVisibility;	 Catch:{ all -> 0x0103 }
        r0 = r37;	 Catch:{ all -> 0x0103 }
        if (r4 == r0) goto L_0x0518;	 Catch:{ all -> 0x0103 }
    L_0x027e:
        r4 = r7.mAttrs;	 Catch:{ all -> 0x0103 }
        r4 = r4.flags;	 Catch:{ all -> 0x0103 }
        r5 = 1048576; // 0x100000 float:1.469368E-39 double:5.180654E-318;	 Catch:{ all -> 0x0103 }
        r4 = r4 & r5;	 Catch:{ all -> 0x0103 }
        if (r4 == 0) goto L_0x0514;	 Catch:{ all -> 0x0103 }
    L_0x0287:
        r25 = 1;	 Catch:{ all -> 0x0103 }
    L_0x0289:
        r4 = 1048576; // 0x100000 float:1.469368E-39 double:5.180654E-318;	 Catch:{ all -> 0x0103 }
        r4 = r4 & r14;	 Catch:{ all -> 0x0103 }
        if (r4 == 0) goto L_0x051c;	 Catch:{ all -> 0x0103 }
    L_0x028e:
        r4 = 1;	 Catch:{ all -> 0x0103 }
    L_0x028f:
        r25 = r25 | r4;	 Catch:{ all -> 0x0103 }
        r4 = r14 & 8192;	 Catch:{ all -> 0x0103 }
        if (r4 == 0) goto L_0x02a4;	 Catch:{ all -> 0x0103 }
    L_0x0295:
        r4 = r8.mSurfaceController;	 Catch:{ all -> 0x0103 }
        if (r4 == 0) goto L_0x02a4;	 Catch:{ all -> 0x0103 }
    L_0x0299:
        r4 = r8.mSurfaceController;	 Catch:{ all -> 0x0103 }
        r0 = r30;	 Catch:{ all -> 0x0103 }
        r5 = r0.isSecureLocked(r7);	 Catch:{ all -> 0x0103 }
        r4.setSecure(r5);	 Catch:{ all -> 0x0103 }
    L_0x02a4:
        r4 = 1;	 Catch:{ all -> 0x0103 }
        r7.mRelayoutCalled = r4;	 Catch:{ all -> 0x0103 }
        r4 = 1;	 Catch:{ all -> 0x0103 }
        r7.mInRelayout = r4;	 Catch:{ all -> 0x0103 }
        r10 = r7.mViewVisibility;	 Catch:{ all -> 0x0103 }
        r0 = r37;	 Catch:{ all -> 0x0103 }
        r7.mViewVisibility = r0;	 Catch:{ all -> 0x0103 }
        if (r37 != 0) goto L_0x02b8;	 Catch:{ all -> 0x0103 }
    L_0x02b2:
        if (r10 == 0) goto L_0x02b8;	 Catch:{ all -> 0x0103 }
    L_0x02b4:
        r4 = IS_USER_BUILD;	 Catch:{ all -> 0x0103 }
        if (r4 == 0) goto L_0x051f;	 Catch:{ all -> 0x0103 }
    L_0x02b8:
        if (r37 != 0) goto L_0x02c4;	 Catch:{ all -> 0x0103 }
    L_0x02ba:
        r4 = r7.mAppToken;	 Catch:{ all -> 0x0103 }
        if (r4 == 0) goto L_0x0558;	 Catch:{ all -> 0x0103 }
    L_0x02be:
        r4 = r7.mAppToken;	 Catch:{ all -> 0x0103 }
        r4 = r4.clientHidden;	 Catch:{ all -> 0x0103 }
        if (r4 == 0) goto L_0x0558;	 Catch:{ all -> 0x0103 }
    L_0x02c4:
        r4 = 0;	 Catch:{ all -> 0x0103 }
        r8.mEnterAnimationPending = r4;	 Catch:{ all -> 0x0103 }
        r4 = 0;	 Catch:{ all -> 0x0103 }
        r8.mEnteringAnimation = r4;	 Catch:{ all -> 0x0103 }
        if (r10 == 0) goto L_0x05ce;	 Catch:{ all -> 0x0103 }
    L_0x02cc:
        r24 = r7.isAnimatingWithSavedSurface();	 Catch:{ all -> 0x0103 }
    L_0x02d0:
        r4 = com.android.server.wm.WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS;	 Catch:{ all -> 0x0103 }
        if (r4 != 0) goto L_0x02d8;	 Catch:{ all -> 0x0103 }
    L_0x02d4:
        r4 = com.android.server.wm.WindowManagerDebugConfig.DEBUG_ANIM;	 Catch:{ all -> 0x0103 }
        if (r4 == 0) goto L_0x02e2;	 Catch:{ all -> 0x0103 }
    L_0x02d8:
        r4 = r8.hasSurface();	 Catch:{ all -> 0x0103 }
        if (r4 == 0) goto L_0x02e2;	 Catch:{ all -> 0x0103 }
    L_0x02de:
        r4 = r7.mAnimatingExit;	 Catch:{ all -> 0x0103 }
        if (r4 == 0) goto L_0x05d2;	 Catch:{ all -> 0x0103 }
    L_0x02e2:
        r4 = r8.hasSurface();	 Catch:{ all -> 0x0103 }
        if (r4 == 0) goto L_0x02ec;	 Catch:{ all -> 0x0103 }
    L_0x02e8:
        r4 = r7.mAnimatingExit;	 Catch:{ all -> 0x0103 }
        if (r4 == 0) goto L_0x05f1;	 Catch:{ all -> 0x0103 }
    L_0x02ec:
        if (r37 != 0) goto L_0x0635;	 Catch:{ all -> 0x0103 }
    L_0x02ee:
        r4 = r8.hasSurface();	 Catch:{ all -> 0x0103 }
        if (r4 == 0) goto L_0x0635;	 Catch:{ all -> 0x0103 }
    L_0x02f4:
        r4 = r8.mSurfaceController;	 Catch:{ all -> 0x0103 }
        r0 = r47;	 Catch:{ all -> 0x0103 }
        r4.getSurface(r0);	 Catch:{ all -> 0x0103 }
    L_0x02fb:
        if (r15 == 0) goto L_0x0309;	 Catch:{ all -> 0x0103 }
    L_0x02fd:
        r4 = 3;	 Catch:{ all -> 0x0103 }
        r5 = 0;	 Catch:{ all -> 0x0103 }
        r0 = r30;	 Catch:{ all -> 0x0103 }
        r4 = r0.updateFocusedWindowLocked(r4, r5);	 Catch:{ all -> 0x0103 }
        if (r4 == 0) goto L_0x0309;	 Catch:{ all -> 0x0103 }
    L_0x0307:
        r17 = 0;	 Catch:{ all -> 0x0103 }
    L_0x0309:
        r4 = r6 & 2;	 Catch:{ all -> 0x0103 }
        if (r4 == 0) goto L_0x0687;	 Catch:{ all -> 0x0103 }
    L_0x030d:
        r23 = 1;	 Catch:{ all -> 0x0103 }
    L_0x030f:
        if (r17 == 0) goto L_0x0327;	 Catch:{ all -> 0x0103 }
    L_0x0311:
        r4 = 0;	 Catch:{ all -> 0x0103 }
        r0 = r30;	 Catch:{ all -> 0x0103 }
        r4 = r0.moveInputMethodWindowsIfNeededLocked(r4);	 Catch:{ all -> 0x0103 }
        if (r4 != 0) goto L_0x031c;	 Catch:{ all -> 0x0103 }
    L_0x031a:
        if (r23 == 0) goto L_0x0327;	 Catch:{ all -> 0x0103 }
    L_0x031c:
        r0 = r30;	 Catch:{ all -> 0x0103 }
        r4 = r0.mLayersController;	 Catch:{ all -> 0x0103 }
        r5 = r7.getWindowList();	 Catch:{ all -> 0x0103 }
        r4.assignLayersLocked(r5);	 Catch:{ all -> 0x0103 }
    L_0x0327:
        if (r25 == 0) goto L_0x0333;	 Catch:{ all -> 0x0103 }
    L_0x0329:
        r4 = r30.getDefaultDisplayContentLocked();	 Catch:{ all -> 0x0103 }
        r5 = r4.pendingLayoutChanges;	 Catch:{ all -> 0x0103 }
        r5 = r5 | 4;	 Catch:{ all -> 0x0103 }
        r4.pendingLayoutChanges = r5;	 Catch:{ all -> 0x0103 }
    L_0x0333:
        r7.setDisplayLayoutNeeded();	 Catch:{ all -> 0x0103 }
        r4 = r38 & 1;	 Catch:{ all -> 0x0103 }
        if (r4 == 0) goto L_0x068b;	 Catch:{ all -> 0x0103 }
    L_0x033a:
        r4 = 1;	 Catch:{ all -> 0x0103 }
    L_0x033b:
        r7.mGivenInsetsPending = r4;	 Catch:{ all -> 0x0103 }
        r4 = 0;	 Catch:{ all -> 0x0103 }
        r0 = r30;	 Catch:{ all -> 0x0103 }
        r11 = r0.updateOrientationFromAppTokensLocked(r4);	 Catch:{ all -> 0x0103 }
        r0 = r30;	 Catch:{ all -> 0x0103 }
        r4 = r0.mWindowPlacerLocked;	 Catch:{ all -> 0x0103 }
        r5 = 0;	 Catch:{ all -> 0x0103 }
        r4.mLayoutCalled = r5;	 Catch:{ all -> 0x0103 }
        r0 = r30;	 Catch:{ all -> 0x0103 }
        r4 = r0.mWindowPlacerLocked;	 Catch:{ all -> 0x0103 }
        r4.performSurfacePlacement();	 Catch:{ all -> 0x0103 }
        r0 = r30;	 Catch:{ all -> 0x0103 }
        r4 = r0.mWindowPlacerLocked;	 Catch:{ all -> 0x0103 }
        r4 = r4.mLayoutCalled;	 Catch:{ all -> 0x0103 }
        if (r4 != 0) goto L_0x037d;	 Catch:{ all -> 0x0103 }
    L_0x035a:
        r4 = com.android.server.wm.WindowManagerDebugConfig.DEBUG_LAYOUT;	 Catch:{ all -> 0x0103 }
        if (r4 == 0) goto L_0x037a;	 Catch:{ all -> 0x0103 }
    L_0x035e:
        r4 = "WindowManager";	 Catch:{ all -> 0x0103 }
        r5 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0103 }
        r5.<init>();	 Catch:{ all -> 0x0103 }
        r27 = "mLayoutCalled fail, set RELAYOUT_NEED_RETRY flag, win:";	 Catch:{ all -> 0x0103 }
        r0 = r27;	 Catch:{ all -> 0x0103 }
        r5 = r5.append(r0);	 Catch:{ all -> 0x0103 }
        r5 = r5.append(r7);	 Catch:{ all -> 0x0103 }
        r5 = r5.toString();	 Catch:{ all -> 0x0103 }
        android.util.Slog.v(r4, r5);	 Catch:{ all -> 0x0103 }
    L_0x037a:
        r4 = 8388608; // 0x800000 float:1.17549435E-38 double:4.144523E-317;	 Catch:{ all -> 0x0103 }
        r6 = r6 | r4;	 Catch:{ all -> 0x0103 }
    L_0x037d:
        if (r23 == 0) goto L_0x039a;	 Catch:{ all -> 0x0103 }
    L_0x037f:
        r4 = r7.mIsWallpaper;	 Catch:{ all -> 0x0103 }
        if (r4 == 0) goto L_0x039a;	 Catch:{ all -> 0x0103 }
    L_0x0383:
        r12 = r30.getDefaultDisplayInfoLocked();	 Catch:{ all -> 0x0103 }
        r0 = r30;	 Catch:{ all -> 0x0103 }
        r4 = r0.mWallpaperControllerLocked;	 Catch:{ all -> 0x0103 }
        r5 = r12.logicalWidth;	 Catch:{ all -> 0x0103 }
        r0 = r12.logicalHeight;	 Catch:{ all -> 0x0103 }
        r27 = r0;	 Catch:{ all -> 0x0103 }
        r28 = 0;	 Catch:{ all -> 0x0103 }
        r0 = r27;	 Catch:{ all -> 0x0103 }
        r1 = r28;	 Catch:{ all -> 0x0103 }
        r4.updateWallpaperOffset(r7, r5, r0, r1);	 Catch:{ all -> 0x0103 }
    L_0x039a:
        r4 = r7.mAppToken;	 Catch:{ all -> 0x0103 }
        if (r4 == 0) goto L_0x03a3;	 Catch:{ all -> 0x0103 }
    L_0x039e:
        r4 = r7.mAppToken;	 Catch:{ all -> 0x0103 }
        r4.updateReportedVisibilityLocked();	 Catch:{ all -> 0x0103 }
    L_0x03a3:
        r4 = r8.mReportSurfaceResized;	 Catch:{ all -> 0x0103 }
        if (r4 == 0) goto L_0x03ac;	 Catch:{ all -> 0x0103 }
    L_0x03a7:
        r4 = 0;	 Catch:{ all -> 0x0103 }
        r8.mReportSurfaceResized = r4;	 Catch:{ all -> 0x0103 }
        r6 = r6 | 32;	 Catch:{ all -> 0x0103 }
    L_0x03ac:
        r0 = r30;	 Catch:{ all -> 0x0103 }
        r4 = r0.mPolicy;	 Catch:{ all -> 0x0103 }
        r4 = r4.isNavBarForcedShownLw(r7);	 Catch:{ all -> 0x0103 }
        if (r4 == 0) goto L_0x03b8;	 Catch:{ all -> 0x0103 }
    L_0x03b6:
        r6 = r6 | 64;	 Catch:{ all -> 0x0103 }
    L_0x03b8:
        r4 = r7.isGoneForLayoutLw();	 Catch:{ all -> 0x0103 }
        if (r4 != 0) goto L_0x03c1;	 Catch:{ all -> 0x0103 }
    L_0x03be:
        r4 = 0;	 Catch:{ all -> 0x0103 }
        r7.mResizedWhileGone = r4;	 Catch:{ all -> 0x0103 }
    L_0x03c1:
        r4 = r7.mCompatFrame;	 Catch:{ all -> 0x0103 }
        r0 = r39;	 Catch:{ all -> 0x0103 }
        r0.set(r4);	 Catch:{ all -> 0x0103 }
        r4 = r7.mOverscanInsets;	 Catch:{ all -> 0x0103 }
        r0 = r40;	 Catch:{ all -> 0x0103 }
        r0.set(r4);	 Catch:{ all -> 0x0103 }
        r4 = r7.mContentInsets;	 Catch:{ all -> 0x0103 }
        r0 = r41;	 Catch:{ all -> 0x0103 }
        r0.set(r4);	 Catch:{ all -> 0x0103 }
        r4 = r7.mVisibleInsets;	 Catch:{ all -> 0x0103 }
        r0 = r42;	 Catch:{ all -> 0x0103 }
        r0.set(r4);	 Catch:{ all -> 0x0103 }
        r4 = r7.mStableInsets;	 Catch:{ all -> 0x0103 }
        r0 = r43;	 Catch:{ all -> 0x0103 }
        r0.set(r4);	 Catch:{ all -> 0x0103 }
        r4 = r7.mOutsets;	 Catch:{ all -> 0x0103 }
        r0 = r44;	 Catch:{ all -> 0x0103 }
        r0.set(r4);	 Catch:{ all -> 0x0103 }
        r4 = r7.mFrame;	 Catch:{ all -> 0x0103 }
        r4 = r7.getBackdropFrame(r4);	 Catch:{ all -> 0x0103 }
        r0 = r45;	 Catch:{ all -> 0x0103 }
        r0.set(r4);	 Catch:{ all -> 0x0103 }
        r4 = localLOGV;	 Catch:{ all -> 0x0103 }
        if (r4 == 0) goto L_0x0467;	 Catch:{ all -> 0x0103 }
    L_0x03fa:
        r4 = "WindowManager";	 Catch:{ all -> 0x0103 }
        r5 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0103 }
        r5.<init>();	 Catch:{ all -> 0x0103 }
        r27 = "Relayout given client ";	 Catch:{ all -> 0x0103 }
        r0 = r27;	 Catch:{ all -> 0x0103 }
        r5 = r5.append(r0);	 Catch:{ all -> 0x0103 }
        r27 = r32.asBinder();	 Catch:{ all -> 0x0103 }
        r0 = r27;	 Catch:{ all -> 0x0103 }
        r5 = r5.append(r0);	 Catch:{ all -> 0x0103 }
        r27 = ", requestedWidth=";	 Catch:{ all -> 0x0103 }
        r0 = r27;	 Catch:{ all -> 0x0103 }
        r5 = r5.append(r0);	 Catch:{ all -> 0x0103 }
        r0 = r35;	 Catch:{ all -> 0x0103 }
        r5 = r5.append(r0);	 Catch:{ all -> 0x0103 }
        r27 = ", requestedHeight=";	 Catch:{ all -> 0x0103 }
        r0 = r27;	 Catch:{ all -> 0x0103 }
        r5 = r5.append(r0);	 Catch:{ all -> 0x0103 }
        r0 = r36;	 Catch:{ all -> 0x0103 }
        r5 = r5.append(r0);	 Catch:{ all -> 0x0103 }
        r27 = ", viewVisibility=";	 Catch:{ all -> 0x0103 }
        r0 = r27;	 Catch:{ all -> 0x0103 }
        r5 = r5.append(r0);	 Catch:{ all -> 0x0103 }
        r0 = r37;	 Catch:{ all -> 0x0103 }
        r5 = r5.append(r0);	 Catch:{ all -> 0x0103 }
        r27 = "\nRelayout returning frame=";	 Catch:{ all -> 0x0103 }
        r0 = r27;	 Catch:{ all -> 0x0103 }
        r5 = r5.append(r0);	 Catch:{ all -> 0x0103 }
        r0 = r39;	 Catch:{ all -> 0x0103 }
        r5 = r5.append(r0);	 Catch:{ all -> 0x0103 }
        r27 = ", surface=";	 Catch:{ all -> 0x0103 }
        r0 = r27;	 Catch:{ all -> 0x0103 }
        r5 = r5.append(r0);	 Catch:{ all -> 0x0103 }
        r0 = r47;	 Catch:{ all -> 0x0103 }
        r5 = r5.append(r0);	 Catch:{ all -> 0x0103 }
        r5 = r5.toString();	 Catch:{ all -> 0x0103 }
        android.util.Slog.v(r4, r5);	 Catch:{ all -> 0x0103 }
    L_0x0467:
        r4 = localLOGV;	 Catch:{ all -> 0x0103 }
        if (r4 != 0) goto L_0x046f;	 Catch:{ all -> 0x0103 }
    L_0x046b:
        r4 = com.android.server.wm.WindowManagerDebugConfig.DEBUG_FOCUS;	 Catch:{ all -> 0x0103 }
        if (r4 == 0) goto L_0x0498;	 Catch:{ all -> 0x0103 }
    L_0x046f:
        r4 = "WindowManager";	 Catch:{ all -> 0x0103 }
        r5 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0103 }
        r5.<init>();	 Catch:{ all -> 0x0103 }
        r27 = "Relayout of ";	 Catch:{ all -> 0x0103 }
        r0 = r27;	 Catch:{ all -> 0x0103 }
        r5 = r5.append(r0);	 Catch:{ all -> 0x0103 }
        r5 = r5.append(r7);	 Catch:{ all -> 0x0103 }
        r27 = ": focusMayChange=";	 Catch:{ all -> 0x0103 }
        r0 = r27;	 Catch:{ all -> 0x0103 }
        r5 = r5.append(r0);	 Catch:{ all -> 0x0103 }
        r5 = r5.append(r15);	 Catch:{ all -> 0x0103 }
        r5 = r5.toString();	 Catch:{ all -> 0x0103 }
        android.util.Slog.v(r4, r5);	 Catch:{ all -> 0x0103 }
    L_0x0498:
        r0 = r30;	 Catch:{ all -> 0x0103 }
        r4 = r0.mInTouchMode;	 Catch:{ all -> 0x0103 }
        if (r4 == 0) goto L_0x068e;	 Catch:{ all -> 0x0103 }
    L_0x049e:
        r4 = 1;	 Catch:{ all -> 0x0103 }
    L_0x049f:
        r6 = r6 | r4;	 Catch:{ all -> 0x0103 }
        r0 = r30;	 Catch:{ all -> 0x0103 }
        r4 = r0.mInputMonitor;	 Catch:{ all -> 0x0103 }
        r5 = 1;	 Catch:{ all -> 0x0103 }
        r4.updateInputWindowsLw(r5);	 Catch:{ all -> 0x0103 }
        r4 = com.android.server.wm.WindowManagerDebugConfig.DEBUG_LAYOUT;	 Catch:{ all -> 0x0103 }
        if (r4 == 0) goto L_0x04db;	 Catch:{ all -> 0x0103 }
    L_0x04ac:
        r4 = "WindowManager";	 Catch:{ all -> 0x0103 }
        r5 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0103 }
        r5.<init>();	 Catch:{ all -> 0x0103 }
        r27 = "Relayout complete ";	 Catch:{ all -> 0x0103 }
        r0 = r27;	 Catch:{ all -> 0x0103 }
        r5 = r5.append(r0);	 Catch:{ all -> 0x0103 }
        r5 = r5.append(r7);	 Catch:{ all -> 0x0103 }
        r27 = ": outFrame=";	 Catch:{ all -> 0x0103 }
        r0 = r27;	 Catch:{ all -> 0x0103 }
        r5 = r5.append(r0);	 Catch:{ all -> 0x0103 }
        r27 = r39.toShortString();	 Catch:{ all -> 0x0103 }
        r0 = r27;	 Catch:{ all -> 0x0103 }
        r5 = r5.append(r0);	 Catch:{ all -> 0x0103 }
        r5 = r5.toString();	 Catch:{ all -> 0x0103 }
        android.util.Slog.v(r4, r5);	 Catch:{ all -> 0x0103 }
    L_0x04db:
        r4 = 0;	 Catch:{ all -> 0x0103 }
        r7.mInRelayout = r4;	 Catch:{ all -> 0x0103 }
        monitor-exit(r26);
        if (r11 == 0) goto L_0x04e4;
    L_0x04e1:
        r30.sendNewConfiguration();
    L_0x04e4:
        android.os.Binder.restoreCallingIdentity(r20);
        return r6;
    L_0x04e8:
        r4 = 0;
        goto L_0x01d5;
    L_0x04eb:
        r4 = 0;
        goto L_0x01dc;
    L_0x04ee:
        r4 = 0;
        goto L_0x01e7;
    L_0x04f1:
        r4 = r7.mAttrs;	 Catch:{ all -> 0x0103 }
        r4 = r4.surfaceInsets;	 Catch:{ all -> 0x0103 }
        r4 = r4.right;	 Catch:{ all -> 0x0103 }
        if (r4 != 0) goto L_0x025b;	 Catch:{ all -> 0x0103 }
    L_0x04f9:
        r4 = r7.mAttrs;	 Catch:{ all -> 0x0103 }
        r4 = r4.surfaceInsets;	 Catch:{ all -> 0x0103 }
        r4 = r4.bottom;	 Catch:{ all -> 0x0103 }
        if (r4 == 0) goto L_0x025f;	 Catch:{ all -> 0x0103 }
    L_0x0501:
        goto L_0x025b;	 Catch:{ all -> 0x0103 }
    L_0x0503:
        r17 = 0;	 Catch:{ all -> 0x0103 }
        goto L_0x0267;	 Catch:{ all -> 0x0103 }
    L_0x0507:
        r4 = r7.mRelayoutCalled;	 Catch:{ all -> 0x0103 }
        if (r4 == 0) goto L_0x050e;	 Catch:{ all -> 0x0103 }
    L_0x050b:
        r15 = 0;	 Catch:{ all -> 0x0103 }
        goto L_0x0278;	 Catch:{ all -> 0x0103 }
    L_0x050e:
        r15 = 1;	 Catch:{ all -> 0x0103 }
        goto L_0x0278;	 Catch:{ all -> 0x0103 }
    L_0x0511:
        r15 = 0;	 Catch:{ all -> 0x0103 }
        goto L_0x0278;	 Catch:{ all -> 0x0103 }
    L_0x0514:
        r25 = 0;	 Catch:{ all -> 0x0103 }
        goto L_0x0289;	 Catch:{ all -> 0x0103 }
    L_0x0518:
        r25 = 0;	 Catch:{ all -> 0x0103 }
        goto L_0x0289;	 Catch:{ all -> 0x0103 }
    L_0x051c:
        r4 = 0;	 Catch:{ all -> 0x0103 }
        goto L_0x028f;	 Catch:{ all -> 0x0103 }
    L_0x051f:
        r4 = TAG;	 Catch:{ all -> 0x0103 }
        r5 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0103 }
        r5.<init>();	 Catch:{ all -> 0x0103 }
        r27 = "Relayout ";	 Catch:{ all -> 0x0103 }
        r0 = r27;	 Catch:{ all -> 0x0103 }
        r5 = r5.append(r0);	 Catch:{ all -> 0x0103 }
        r5 = r5.append(r7);	 Catch:{ all -> 0x0103 }
        r27 = ": oldVis=";	 Catch:{ all -> 0x0103 }
        r0 = r27;	 Catch:{ all -> 0x0103 }
        r5 = r5.append(r0);	 Catch:{ all -> 0x0103 }
        r5 = r5.append(r10);	 Catch:{ all -> 0x0103 }
        r27 = " newVis=";	 Catch:{ all -> 0x0103 }
        r0 = r27;	 Catch:{ all -> 0x0103 }
        r5 = r5.append(r0);	 Catch:{ all -> 0x0103 }
        r0 = r37;	 Catch:{ all -> 0x0103 }
        r5 = r5.append(r0);	 Catch:{ all -> 0x0103 }
        r5 = r5.toString();	 Catch:{ all -> 0x0103 }
        android.util.Slog.i(r4, r5);	 Catch:{ all -> 0x0103 }
        goto L_0x02b8;	 Catch:{ all -> 0x0103 }
    L_0x0558:
        r4 = r30;	 Catch:{ all -> 0x0103 }
        r5 = r46;	 Catch:{ all -> 0x0103 }
        r6 = r4.relayoutVisibleWindow(r5, r6, r7, r8, r9, r10);	 Catch:{ all -> 0x0103 }
        r0 = r30;	 Catch:{ Exception -> 0x0581 }
        r1 = r47;	 Catch:{ Exception -> 0x0581 }
        r6 = r0.createSurfaceControl(r1, r6, r7, r8);	 Catch:{ Exception -> 0x0581 }
        r4 = r6 & 2;
        if (r4 == 0) goto L_0x056e;
    L_0x056c:
        r15 = r18;
    L_0x056e:
        r4 = r7.mAttrs;	 Catch:{ all -> 0x0103 }
        r4 = r4.type;	 Catch:{ all -> 0x0103 }
        r5 = 2011; // 0x7db float:2.818E-42 double:9.936E-321;	 Catch:{ all -> 0x0103 }
        if (r4 != r5) goto L_0x057c;	 Catch:{ all -> 0x0103 }
    L_0x0576:
        r0 = r30;	 Catch:{ all -> 0x0103 }
        r0.mInputMethodWindow = r7;	 Catch:{ all -> 0x0103 }
        r17 = 1;	 Catch:{ all -> 0x0103 }
    L_0x057c:
        r7.adjustStartingWindowFlags();	 Catch:{ all -> 0x0103 }
        goto L_0x02fb;	 Catch:{ all -> 0x0103 }
    L_0x0581:
        r13 = move-exception;	 Catch:{ all -> 0x0103 }
        r0 = r30;	 Catch:{ all -> 0x0103 }
        r4 = r0.mInputMonitor;	 Catch:{ all -> 0x0103 }
        r5 = 1;	 Catch:{ all -> 0x0103 }
        r4.updateInputWindowsLw(r5);	 Catch:{ all -> 0x0103 }
        r4 = "WindowManager";	 Catch:{ all -> 0x0103 }
        r5 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0103 }
        r5.<init>();	 Catch:{ all -> 0x0103 }
        r27 = "Exception thrown when creating surface for client ";	 Catch:{ all -> 0x0103 }
        r0 = r27;	 Catch:{ all -> 0x0103 }
        r5 = r5.append(r0);	 Catch:{ all -> 0x0103 }
        r0 = r32;	 Catch:{ all -> 0x0103 }
        r5 = r5.append(r0);	 Catch:{ all -> 0x0103 }
        r27 = " (";	 Catch:{ all -> 0x0103 }
        r0 = r27;	 Catch:{ all -> 0x0103 }
        r5 = r5.append(r0);	 Catch:{ all -> 0x0103 }
        r0 = r7.mAttrs;	 Catch:{ all -> 0x0103 }
        r27 = r0;	 Catch:{ all -> 0x0103 }
        r27 = r27.getTitle();	 Catch:{ all -> 0x0103 }
        r0 = r27;	 Catch:{ all -> 0x0103 }
        r5 = r5.append(r0);	 Catch:{ all -> 0x0103 }
        r27 = ")";	 Catch:{ all -> 0x0103 }
        r0 = r27;	 Catch:{ all -> 0x0103 }
        r5 = r5.append(r0);	 Catch:{ all -> 0x0103 }
        r5 = r5.toString();	 Catch:{ all -> 0x0103 }
        android.util.Slog.w(r4, r5, r13);	 Catch:{ all -> 0x0103 }
        android.os.Binder.restoreCallingIdentity(r20);	 Catch:{ all -> 0x0103 }
        r4 = 0;
        monitor-exit(r26);
        return r4;
    L_0x05ce:
        r24 = 0;
        goto L_0x02d0;
    L_0x05d2:
        if (r24 == 0) goto L_0x02e2;
    L_0x05d4:
        r4 = TAG;	 Catch:{ all -> 0x0103 }
        r5 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0103 }
        r5.<init>();	 Catch:{ all -> 0x0103 }
        r27 = "Ignoring layout to invisible when using saved surface ";	 Catch:{ all -> 0x0103 }
        r0 = r27;	 Catch:{ all -> 0x0103 }
        r5 = r5.append(r0);	 Catch:{ all -> 0x0103 }
        r5 = r5.append(r7);	 Catch:{ all -> 0x0103 }
        r5 = r5.toString();	 Catch:{ all -> 0x0103 }
        android.util.Slog.d(r4, r5);	 Catch:{ all -> 0x0103 }
        goto L_0x02e2;	 Catch:{ all -> 0x0103 }
    L_0x05f1:
        if (r24 != 0) goto L_0x02ec;	 Catch:{ all -> 0x0103 }
    L_0x05f3:
        r4 = com.android.server.wm.WindowManagerDebugConfig.DEBUG_VISIBILITY;	 Catch:{ all -> 0x0103 }
        if (r4 == 0) goto L_0x0626;	 Catch:{ all -> 0x0103 }
    L_0x05f7:
        r4 = "WindowManager";	 Catch:{ all -> 0x0103 }
        r5 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0103 }
        r5.<init>();	 Catch:{ all -> 0x0103 }
        r27 = "Relayout invis ";	 Catch:{ all -> 0x0103 }
        r0 = r27;	 Catch:{ all -> 0x0103 }
        r5 = r5.append(r0);	 Catch:{ all -> 0x0103 }
        r5 = r5.append(r7);	 Catch:{ all -> 0x0103 }
        r27 = ": mAnimatingExit=";	 Catch:{ all -> 0x0103 }
        r0 = r27;	 Catch:{ all -> 0x0103 }
        r5 = r5.append(r0);	 Catch:{ all -> 0x0103 }
        r0 = r7.mAnimatingExit;	 Catch:{ all -> 0x0103 }
        r27 = r0;	 Catch:{ all -> 0x0103 }
        r0 = r27;	 Catch:{ all -> 0x0103 }
        r5 = r5.append(r0);	 Catch:{ all -> 0x0103 }
        r5 = r5.toString();	 Catch:{ all -> 0x0103 }
        android.util.Slog.i(r4, r5);	 Catch:{ all -> 0x0103 }
    L_0x0626:
        r4 = r7.mWillReplaceWindow;	 Catch:{ all -> 0x0103 }
        if (r4 != 0) goto L_0x0632;	 Catch:{ all -> 0x0103 }
    L_0x062a:
        r0 = r30;	 Catch:{ all -> 0x0103 }
        r1 = r18;	 Catch:{ all -> 0x0103 }
        r15 = r0.tryStartExitingAnimation(r7, r8, r1, r15);	 Catch:{ all -> 0x0103 }
    L_0x0632:
        r6 = 4;	 Catch:{ all -> 0x0103 }
        goto L_0x02ec;	 Catch:{ all -> 0x0103 }
    L_0x0635:
        r4 = com.android.server.wm.WindowManagerDebugConfig.DEBUG_VISIBILITY;	 Catch:{ all -> 0x0103 }
        if (r4 == 0) goto L_0x0655;	 Catch:{ all -> 0x0103 }
    L_0x0639:
        r4 = "WindowManager";	 Catch:{ all -> 0x0103 }
        r5 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0103 }
        r5.<init>();	 Catch:{ all -> 0x0103 }
        r27 = "Releasing surface in: ";	 Catch:{ all -> 0x0103 }
        r0 = r27;	 Catch:{ all -> 0x0103 }
        r5 = r5.append(r0);	 Catch:{ all -> 0x0103 }
        r5 = r5.append(r7);	 Catch:{ all -> 0x0103 }
        r5 = r5.toString();	 Catch:{ all -> 0x0103 }
        android.util.Slog.i(r4, r5);	 Catch:{ all -> 0x0103 }
    L_0x0655:
        r4 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0680 }
        r4.<init>();	 Catch:{ all -> 0x0680 }
        r5 = "wmReleaseOutSurface_";	 Catch:{ all -> 0x0680 }
        r4 = r4.append(r5);	 Catch:{ all -> 0x0680 }
        r5 = r7.mAttrs;	 Catch:{ all -> 0x0680 }
        r5 = r5.getTitle();	 Catch:{ all -> 0x0680 }
        r4 = r4.append(r5);	 Catch:{ all -> 0x0680 }
        r4 = r4.toString();	 Catch:{ all -> 0x0680 }
        r28 = 32;	 Catch:{ all -> 0x0680 }
        r0 = r28;	 Catch:{ all -> 0x0680 }
        android.os.Trace.traceBegin(r0, r4);	 Catch:{ all -> 0x0680 }
        r47.release();	 Catch:{ all -> 0x0680 }
        r4 = 32;
        android.os.Trace.traceEnd(r4);	 Catch:{ all -> 0x0103 }
        goto L_0x02fb;	 Catch:{ all -> 0x0103 }
    L_0x0680:
        r4 = move-exception;	 Catch:{ all -> 0x0103 }
        r28 = 32;	 Catch:{ all -> 0x0103 }
        android.os.Trace.traceEnd(r28);	 Catch:{ all -> 0x0103 }
        throw r4;	 Catch:{ all -> 0x0103 }
    L_0x0687:
        r23 = 0;
        goto L_0x030f;
    L_0x068b:
        r4 = 0;
        goto L_0x033b;
    L_0x068e:
        r4 = 0;
        goto L_0x049f;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wm.WindowManagerService.relayoutWindow(com.android.server.wm.Session, android.view.IWindow, int, android.view.WindowManager$LayoutParams, int, int, int, int, android.graphics.Rect, android.graphics.Rect, android.graphics.Rect, android.graphics.Rect, android.graphics.Rect, android.graphics.Rect, android.graphics.Rect, android.content.res.Configuration, android.view.Surface):int");
    }

    private boolean tryStartExitingAnimation(WindowState win, WindowStateAnimator winAnimator, boolean isDefaultDisplay, boolean focusMayChange) {
        int transit = 2;
        if (win.mAttrs.type == 3) {
            transit = 5;
        }
        if (win.isWinVisibleLw() && winAnimator.applyAnimationLocked(transit, false)) {
            focusMayChange = isDefaultDisplay;
            win.mAnimatingExit = true;
            win.mWinAnimator.mAnimating = true;
        } else if (win.mWinAnimator.isAnimationSet()) {
            win.mAnimatingExit = true;
            win.mWinAnimator.mAnimating = true;
        } else if (this.mWallpaperControllerLocked.isWallpaperTarget(win)) {
            win.mAnimatingExit = true;
            win.mWinAnimator.mAnimating = true;
        } else {
            if (this.mInputMethodWindow == win) {
                this.mInputMethodWindow = null;
            }
            win.destroyOrSaveSurface();
        }
        if (this.mAccessibilityController != null && win.getDisplayId() == 0) {
            this.mAccessibilityController.onWindowTransitionLocked(win, transit);
        }
        return focusMayChange;
    }

    private int createSurfaceControl(Surface outSurface, int result, WindowState win, WindowStateAnimator winAnimator) {
        if (!win.mHasSurface) {
            result |= 4;
        }
        WindowSurfaceController surfaceController = winAnimator.createSurfaceLocked();
        if (surfaceController != null) {
            surfaceController.getSurface(outSurface);
            if (WindowManagerDebugConfig.SHOW_TRANSACTIONS) {
                Slog.i("WindowManager", "  OUT SURFACE " + outSurface + ": copied");
            }
        } else {
            Slog.w("WindowManager", "Failed to create surface control for " + win);
            outSurface.release();
        }
        return result;
    }

    private int relayoutVisibleWindow(Configuration outConfig, int result, WindowState win, WindowStateAnimator winAnimator, int attrChanges, int oldVisibility) {
        int i;
        int i2 = 0;
        if (win.isVisibleLw()) {
            i = 0;
        } else {
            i = 2;
        }
        result |= i;
        if (win.mAnimatingExit) {
            Slog.d(TAG, "relayoutVisibleWindow: " + win + " mAnimatingExit=true, mRemoveOnExit=" + win.mRemoveOnExit + ", mDestroying=" + win.mDestroying);
            winAnimator.cancelExitAnimationForNextAnimationLocked();
            win.mAnimatingExit = false;
        }
        if (win.mDestroying) {
            win.mDestroying = false;
            this.mDestroySurface.remove(win);
        }
        if (oldVisibility == 8) {
            winAnimator.mEnterAnimationPending = true;
        }
        win.mLastVisibleLayoutRotation = this.mRotation;
        winAnimator.mEnteringAnimation = true;
        if ((result & 2) != 0) {
            win.prepareWindowToDisplayDuringRelayout(outConfig);
        }
        if (!((attrChanges & 8) == 0 || winAnimator.tryChangeFormatInPlaceLocked())) {
            winAnimator.preserveSurfaceLocked();
            result |= 6;
        }
        if (win.isDragResizeChanged() || win.isResizedWhileNotDragResizing()) {
            win.setDragResizing();
            win.setResizedWhileNotDragResizing(false);
            if (win.mHasSurface && win.mAttachedWindow == null) {
                winAnimator.preserveSurfaceLocked();
                result |= 2;
            }
        }
        boolean freeformResizing = win.isDragResizing() ? win.getResizeMode() == 0 : false;
        boolean dockedResizing = win.isDragResizing() ? win.getResizeMode() == 1 : false;
        if (freeformResizing) {
            i = 16;
        } else {
            i = 0;
        }
        result |= i;
        if (dockedResizing) {
            i2 = 8;
        }
        result |= i2;
        if (win.isAnimatingWithSavedSurface()) {
            return result | 2;
        }
        return result;
    }

    public void performDeferredDestroyWindow(Session session, IWindow client) {
        long origId = Binder.clearCallingIdentity();
        try {
            synchronized (this.mWindowMap) {
                WindowState win = windowForClientLocked(session, client, false);
                if (win != null && !win.mWillReplaceWindow) {
                    win.mWinAnimator.destroyDeferredSurfaceLocked();
                    Binder.restoreCallingIdentity(origId);
                }
            }
        } finally {
            Binder.restoreCallingIdentity(origId);
        }
    }

    public boolean outOfMemoryWindow(Session session, IWindow client) {
        boolean z = false;
        long origId = Binder.clearCallingIdentity();
        try {
            synchronized (this.mWindowMap) {
                WindowState win = windowForClientLocked(session, client, false);
                if (win != null) {
                    z = "from-client";
                    boolean reclaimSomeSurfaceMemoryLocked = reclaimSomeSurfaceMemoryLocked(win.mWinAnimator, z, false);
                    Binder.restoreCallingIdentity(origId);
                    return reclaimSomeSurfaceMemoryLocked;
                }
            }
            return z;
        } finally {
            Binder.restoreCallingIdentity(origId);
        }
    }

    public void finishDrawingWindow(Session session, IWindow client) {
        long origId = Binder.clearCallingIdentity();
        try {
            synchronized (this.mWindowMap) {
                WindowState win = windowForClientLocked(session, client, false);
                if (WindowManagerDebugConfig.DEBUG_ADD_REMOVE) {
                    Slog.d("WindowManager", "finishDrawingWindow: " + win + " mDrawState=" + (win != null ? win.mWinAnimator.drawStateToString() : "null"));
                }
                if (win != null && win.mWinAnimator.finishDrawingLocked()) {
                    if ((win.mAttrs.flags & DumpState.DUMP_DEXOPT) != 0) {
                        DisplayContent defaultDisplayContentLocked = getDefaultDisplayContentLocked();
                        defaultDisplayContentLocked.pendingLayoutChanges |= 4;
                    }
                    win.setDisplayLayoutNeeded();
                    this.mWindowPlacerLocked.requestTraversal();
                }
            }
        } finally {
            Binder.restoreCallingIdentity(origId);
        }
    }

    private boolean applyAnimationLocked(AppWindowToken atoken, LayoutParams lp, int transit, boolean enter, boolean isVoiceInteraction) {
        Trace.traceBegin(32, "WM#applyAnimationLocked");
        if (okToDisplay()) {
            Animation a;
            DisplayInfo displayInfo = getDefaultDisplayInfoLocked();
            int width = displayInfo.appWidth;
            int height = displayInfo.appHeight;
            if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS || WindowManagerDebugConfig.DEBUG_ANIM) {
                Slog.v("WindowManager", "applyAnimation: atoken=" + atoken);
            }
            WindowState win = atoken.findMainWindow();
            Rect frame = new Rect(0, 0, width, height);
            Rect displayFrame = new Rect(0, 0, displayInfo.logicalWidth, displayInfo.logicalHeight);
            Rect insets = new Rect();
            Rect surfaceInsets = null;
            boolean freeform = win != null ? win.inFreeformWorkspace() : false;
            if (win != null) {
                if (freeform) {
                    frame.set(win.mFrame);
                } else {
                    frame.set(win.mContainingFrame);
                }
                surfaceInsets = win.getAttrs().surfaceInsets;
                insets.set(win.mContentInsets);
            }
            if (atoken.mLaunchTaskBehind) {
                enter = false;
            }
            if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                Slog.d("WindowManager", "Loading animation for app transition. transit=" + AppTransition.appTransitionToString(transit) + " enter=" + enter + " frame=" + frame + " insets=" + insets + " surfaceInsets=" + surfaceInsets);
            }
            if (!this.dismissDockedStackFromHome || enter) {
                a = this.mAppTransition.loadAnimation(lp, transit, enter, this.mCurConfiguration.uiMode, this.mCurConfiguration.orientation, frame, displayFrame, insets, surfaceInsets, isVoiceInteraction, freeform, atoken.mTask.mTaskId);
            } else if (atoken.stringName.contains(RECENT_ACTVITIY_NAME)) {
                a = this.mAppTransition.loadAnimationRes(lp, 201982991);
            } else if (win == null) {
                a = this.mAppTransition.loadAnimationRes(lp, 201982990);
                Slog.w("WindowManager", "Loading animation null when win is null for " + atoken.stringName);
            } else {
                if (isLeftOrTopDockedStack(win.mVisibleFrame)) {
                    a = this.mAppTransition.loadAnimationRes(lp, 201982990);
                } else {
                    a = this.mAppTransition.loadAnimationRes(lp, 201982991);
                }
            }
            if (a != null) {
                if (WindowManagerDebugConfig.DEBUG_ANIM) {
                    logWithStack(TAG, "Loaded animation " + a + " for " + atoken);
                }
                atoken.mAppAnimator.setAnimation(a, frame.width(), frame.height(), this.mAppTransition.canSkipFirstFrame(), this.mAppTransition.getAppStackClipMode());
            }
        } else {
            atoken.mAppAnimator.clearAnimation();
        }
        Trace.traceEnd(32);
        if (atoken.mAppAnimator.animation != null) {
            return true;
        }
        return false;
    }

    /* JADX WARNING: Missing block: B:39:0x0135, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void validateAppTokens(int stackId, List<TaskGroup> tasks) {
        synchronized (this.mWindowMap) {
            int t = tasks.size() - 1;
            if (t < 0) {
                Slog.w("WindowManager", "validateAppTokens: empty task list");
                return;
            }
            int taskId = ((TaskGroup) tasks.get(0)).taskId;
            DisplayContent displayContent = ((Task) this.mTaskIdToTask.get(taskId)).getDisplayContent();
            if (displayContent == null) {
                Slog.w("WindowManager", "validateAppTokens: no Display for taskId=" + taskId);
                return;
            }
            ArrayList<Task> localTasks = ((TaskStack) this.mStackIdToStack.get(stackId)).getTasks();
            int taskNdx = localTasks.size() - 1;
            while (taskNdx >= 0 && t >= 0) {
                AppTokenList localTokens = ((Task) localTasks.get(taskNdx)).mAppTokens;
                TaskGroup task = (TaskGroup) tasks.get(t);
                List<IApplicationToken> tokens = task.tokens;
                DisplayContent lastDisplayContent = displayContent;
                displayContent = ((Task) this.mTaskIdToTask.get(taskId)).getDisplayContent();
                if (displayContent == lastDisplayContent) {
                    int tokenNdx = localTokens.size() - 1;
                    int v = task.tokens.size() - 1;
                    while (tokenNdx >= 0 && v >= 0) {
                        AppWindowToken atoken = (AppWindowToken) localTokens.get(tokenNdx);
                        if (atoken.removed) {
                            tokenNdx--;
                        } else if (tokens.get(v) != atoken.token) {
                            break;
                        } else {
                            tokenNdx--;
                            v--;
                        }
                    }
                    if (tokenNdx >= 0 || v >= 0) {
                        break;
                    }
                    taskNdx--;
                    t--;
                } else {
                    Slog.w("WindowManager", "validateAppTokens: displayContent changed in TaskGroup list!");
                    return;
                }
            }
            if (taskNdx >= 0 || t >= 0) {
                Slog.w("WindowManager", "validateAppTokens: Mismatch! ActivityManager=" + tasks);
                Slog.w("WindowManager", "validateAppTokens: Mismatch! WindowManager=" + localTasks);
                Slog.w("WindowManager", "validateAppTokens: Mismatch! Callers=" + Debug.getCallers(4));
            }
        }
    }

    public void validateStackOrder(Integer[] remoteStackIds) {
    }

    private boolean checkCallingPermission(String permission, String func) {
        if (Binder.getCallingPid() == Process.myPid() || this.mContext.checkCallingPermission(permission) == 0) {
            return true;
        }
        Slog.w("WindowManager", "Permission Denial: " + func + " from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " requires " + permission);
        return false;
    }

    boolean okToDisplay() {
        return (this.mDisplayFrozen || !this.mDisplayEnabled) ? false : this.mPolicy.isScreenOn();
    }

    AppWindowToken findAppWindowToken(IBinder token) {
        WindowToken wtoken = (WindowToken) this.mTokenMap.get(token);
        if (wtoken == null) {
            return null;
        }
        return wtoken.appWindowToken;
    }

    /* JADX WARNING: Missing block: B:17:0x0053, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void addWindowToken(IBinder token, int type) {
        if (checkCallingPermission("android.permission.MANAGE_APP_TOKENS", "addWindowToken()")) {
            synchronized (this.mWindowMap) {
                if (((WindowToken) this.mTokenMap.get(token)) != null) {
                    Slog.w("WindowManager", "Attempted to add existing input method token: " + token);
                    return;
                }
                WindowToken wtoken = new WindowToken(this, token, type, true);
                this.mTokenMap.put(token, wtoken);
                if (type == 2013) {
                    this.mWallpaperControllerLocked.addWallpaperToken(wtoken);
                }
            }
        } else {
            throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
        }
    }

    public void removeWindowToken(IBinder token) {
        if (checkCallingPermission("android.permission.MANAGE_APP_TOKENS", "removeWindowToken()")) {
            long origId = Binder.clearCallingIdentity();
            synchronized (this.mWindowMap) {
                DisplayContent displayContent = null;
                WindowToken wtoken = (WindowToken) this.mTokenMap.remove(token);
                if (wtoken != null) {
                    boolean delayed = false;
                    if (!wtoken.hidden) {
                        int N = wtoken.windows.size();
                        boolean changed = false;
                        for (int i = 0; i < N; i++) {
                            WindowState win = (WindowState) wtoken.windows.get(i);
                            displayContent = win.getDisplayContent();
                            if (win.mWinAnimator.isAnimationSet()) {
                                delayed = true;
                            }
                            if (win.isVisibleNow()) {
                                win.mWinAnimator.applyAnimationLocked(2, false);
                                if (this.mAccessibilityController != null && win.isDefaultDisplay()) {
                                    this.mAccessibilityController.onWindowTransitionLocked(win, 2);
                                }
                                changed = true;
                                if (displayContent != null) {
                                    displayContent.layoutNeeded = true;
                                }
                            }
                        }
                        wtoken.hidden = true;
                        if (changed) {
                            this.mWindowPlacerLocked.performSurfacePlacement();
                            updateFocusedWindowLocked(0, false);
                        }
                        if (delayed && displayContent != null) {
                            displayContent.mExitingTokens.add(wtoken);
                        } else if (wtoken.windowType == 2013) {
                            this.mWallpaperControllerLocked.removeWallpaperToken(wtoken);
                        }
                    } else if (wtoken.windowType == 2013) {
                        this.mWallpaperControllerLocked.removeWallpaperToken(wtoken);
                    }
                    this.mInputMonitor.updateInputWindowsLw(true);
                } else {
                    Slog.w("WindowManager", "Attempted to remove non-existing token: " + token);
                }
            }
            Binder.restoreCallingIdentity(origId);
            return;
        }
        throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
    }

    private Task createTaskLocked(int taskId, int stackId, int userId, AppWindowToken atoken, Rect bounds, Configuration config) {
        if (WindowManagerDebugConfig.DEBUG_STACK) {
            Slog.i("WindowManager", "createTaskLocked: taskId=" + taskId + " stackId=" + stackId + " atoken=" + atoken + " bounds=" + bounds);
        }
        TaskStack stack = (TaskStack) this.mStackIdToStack.get(stackId);
        if (stack == null) {
            throw new IllegalArgumentException("addAppToken: invalid stackId=" + stackId);
        }
        boolean z;
        Object[] objArr = new Object[2];
        objArr[0] = Integer.valueOf(taskId);
        objArr[1] = Integer.valueOf(stackId);
        EventLog.writeEvent(EventLogTags.WM_TASK_CREATED, objArr);
        Task task = new Task(taskId, stack, userId, this, bounds, config);
        this.mTaskIdToTask.put(taskId, task);
        if (atoken.mLaunchTaskBehind) {
            z = false;
        } else {
            z = true;
        }
        stack.addTask(task, z, atoken.showForAllUsers);
        return task;
    }

    /* JADX WARNING: Missing block: B:19:0x00af, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void addAppToken(int addPos, IApplicationToken token, int taskId, int stackId, int requestedOrientation, boolean fullscreen, boolean showForAllUsers, int userId, int configChanges, boolean voiceInteraction, boolean launchTaskBehind, Rect taskBounds, Configuration config, int taskResizeMode, boolean alwaysFocusable, boolean homeTask, int targetSdkVersion, int rotationAnimationHint) {
        if (checkCallingPermission("android.permission.MANAGE_APP_TOKENS", "addAppToken()")) {
            long inputDispatchingTimeoutNanos;
            try {
                inputDispatchingTimeoutNanos = token.getKeyDispatchingTimeout() * 1000000;
            } catch (RemoteException ex) {
                Slog.w("WindowManager", "Could not get dispatching timeout.", ex);
                inputDispatchingTimeoutNanos = DEFAULT_INPUT_DISPATCHING_TIMEOUT_NANOS;
            }
            synchronized (this.mWindowMap) {
                AppWindowToken atoken = findAppWindowToken(token.asBinder());
                Task task;
                if (atoken != null) {
                    Slog.w("WindowManager", "Attempted to add existing app token: " + token);
                    task = (Task) this.mTaskIdToTask.get(taskId);
                    if (task == null) {
                        task = createTaskLocked(taskId, stackId, userId, atoken, taskBounds, config);
                    }
                    if (!task.mAppTokens.contains(atoken)) {
                        Slog.v("WindowManager", "existing app token add: " + atoken + " to stack=" + stackId + " task=" + task + " at " + addPos);
                        task.addAppToken(addPos, atoken, taskResizeMode, homeTask);
                    }
                } else {
                    atoken = new AppWindowToken(this, token, voiceInteraction);
                    atoken.inputDispatchingTimeoutNanos = inputDispatchingTimeoutNanos;
                    atoken.appFullscreen = fullscreen;
                    atoken.showForAllUsers = showForAllUsers;
                    atoken.targetSdk = targetSdkVersion;
                    atoken.requestedOrientation = requestedOrientation;
                    atoken.layoutConfigChanges = (configChanges & 1152) != 0;
                    atoken.mLaunchTaskBehind = launchTaskBehind;
                    atoken.mAlwaysFocusable = alwaysFocusable;
                    if (WindowManagerDebugConfig.DEBUG_TOKEN_MOVEMENT || WindowManagerDebugConfig.DEBUG_ADD_REMOVE) {
                        Slog.v("WindowManager", "addAppToken: " + atoken + " to stack=" + stackId + " task=" + taskId + " at " + addPos);
                    }
                    atoken.mRotationAnimationHint = rotationAnimationHint;
                    task = (Task) this.mTaskIdToTask.get(taskId);
                    if (task == null) {
                        task = createTaskLocked(taskId, stackId, userId, atoken, taskBounds, config);
                    }
                    task.addAppToken(addPos, atoken, taskResizeMode, homeTask);
                    this.mTokenMap.put(token.asBinder(), atoken);
                    atoken.hidden = true;
                    atoken.hiddenRequested = true;
                    return;
                }
            }
        }
        throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
    }

    public void setAppTask(IBinder token, int taskId, int stackId, Rect taskBounds, Configuration config, int taskResizeMode, boolean homeTask) {
        if (checkCallingPermission("android.permission.MANAGE_APP_TOKENS", "setAppTask()")) {
            synchronized (this.mWindowMap) {
                AppWindowToken atoken = findAppWindowToken(token);
                if (atoken == null) {
                    Slog.w("WindowManager", "Attempted to set task id of non-existing app token: " + token);
                    return;
                }
                Task oldTask = atoken.mTask;
                oldTask.removeAppToken(atoken);
                Task newTask = (Task) this.mTaskIdToTask.get(taskId);
                if (newTask == null) {
                    newTask = createTaskLocked(taskId, stackId, oldTask.mUserId, atoken, taskBounds, config);
                }
                newTask.addAppToken(Integer.MAX_VALUE, atoken, taskResizeMode, homeTask);
                return;
            }
        }
        throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
    }

    public int getOrientationLocked() {
        AppWindowToken appShowWhenLocked = null;
        if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
            Slog.v(TAG, "Checking window orientation: mDisplayFrozen=" + this.mDisplayFrozen + ", mLastWindowForcedOrientation=" + this.mLastWindowForcedOrientation + ", mLastKeyguardForcedOrientation=" + this.mLastKeyguardForcedOrientation);
        }
        if (!this.mDisplayFrozen) {
            int req;
            WindowList windows = getDefaultWindowListLocked();
            for (int pos = windows.size() - 1; pos >= 0; pos--) {
                WindowState win = (WindowState) windows.get(pos);
                if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                    Slog.v(TAG, win + " screenOrientation=" + win.mAttrs.screenOrientation + ", appToken=" + win.mAppToken + ", visibility=" + win.isVisibleLw() + ", mPolicyVisibilityAfterAnim=" + win.mPolicyVisibilityAfterAnim + ", mPolicyVisibility=" + win.mPolicyVisibility + ", mAttachedHidden=" + win.mAttachedHidden + ", destroying=" + win.mDestroying);
                }
                if (win.mAppToken != null) {
                    break;
                }
                if (win.isVisibleLw() && win.mPolicyVisibilityAfterAnim) {
                    req = win.mAttrs.screenOrientation;
                    if (!(req == -1 || req == 3)) {
                        if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                            Slog.v("WindowManager", win + " forcing orientation to " + req);
                        }
                        if (this.mPolicy.isKeyguardHostWindow(win.mAttrs)) {
                            this.mLastKeyguardForcedOrientation = req;
                        }
                        this.mLastWindowForcedOrientation = req;
                        return req;
                    }
                }
            }
            this.mLastWindowForcedOrientation = -1;
            if (this.mPolicy.isKeyguardLocked()) {
                WindowState winShowWhenLocked = (WindowState) this.mPolicy.getWinShowWhenLockedLw();
                if (winShowWhenLocked != null) {
                    appShowWhenLocked = winShowWhenLocked.mAppToken;
                }
                if (appShowWhenLocked != null) {
                    req = appShowWhenLocked.requestedOrientation;
                    if (req == 3) {
                        req = this.mLastKeyguardForcedOrientation;
                    }
                    if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                        Slog.v("WindowManager", "Done at " + appShowWhenLocked + " -- show when locked, return " + req);
                    }
                    return req;
                }
                if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                    Slog.v("WindowManager", "No one is requesting an orientation when the screen is locked");
                }
                return this.mLastKeyguardForcedOrientation;
            }
        } else if (this.mLastWindowForcedOrientation != -1) {
            if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                Slog.v("WindowManager", "Display is frozen, return " + this.mLastWindowForcedOrientation);
            }
            return this.mLastWindowForcedOrientation;
        } else if (this.mPolicy.isKeyguardLocked()) {
            if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                Slog.v("WindowManager", "Display is frozen while keyguard locked, return " + this.mLastOrientation);
            }
            return this.mLastOrientation;
        }
        return getAppSpecifiedOrientation();
    }

    private int getAppSpecifiedOrientation() {
        boolean inMultiWindow;
        int lastOrientation = -1;
        boolean findingBehind = false;
        boolean lastFullscreen = false;
        ArrayList<Task> tasks = getDefaultDisplayContentLocked().getTasks();
        if (isStackVisibleLocked(3)) {
            inMultiWindow = true;
        } else {
            inMultiWindow = isStackVisibleLocked(2);
        }
        boolean dockMinimized = getDefaultDisplayContentLocked().mDividerControllerLocked.isMinimizedDock();
        for (int taskNdx = tasks.size() - 1; taskNdx >= 0; taskNdx--) {
            AppTokenList tokens = ((Task) tasks.get(taskNdx)).mAppTokens;
            int firstToken = tokens.size() - 1;
            for (int tokenNdx = firstToken; tokenNdx >= 0; tokenNdx--) {
                AppWindowToken atoken = (AppWindowToken) tokens.get(tokenNdx);
                if (WindowManagerDebugConfig.DEBUG_APP_ORIENTATION) {
                    Slog.v("WindowManager", "Checking app orientation: " + atoken);
                }
                if (findingBehind || atoken.hidden || !atoken.hiddenRequested) {
                    if (tokenNdx == firstToken && lastOrientation != 3 && lastFullscreen) {
                        if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                            Slog.v("WindowManager", "Done at " + atoken + " -- end of group, return " + lastOrientation);
                        }
                        return lastOrientation;
                    } else if (atoken.hiddenRequested) {
                        if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                            Slog.v("WindowManager", "Skipping " + atoken + " -- hidden on top");
                        }
                    } else if (!inMultiWindow || (atoken.mTask.isHomeTask() && dockMinimized)) {
                        if (tokenNdx == 0) {
                            lastOrientation = atoken.requestedOrientation;
                        }
                        int or = atoken.requestedOrientation;
                        lastFullscreen = atoken.appFullscreen;
                        if (lastFullscreen && or != 3) {
                            if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                                Slog.v("WindowManager", "Done at " + atoken + " -- full screen, return " + or);
                            }
                            return or;
                        } else if (or == -1 || or == 3) {
                            findingBehind |= or == 3 ? 1 : 0;
                        } else {
                            if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                                Slog.v("WindowManager", "Done at " + atoken + " -- explicitly set, return " + or);
                            }
                            return or;
                        }
                    }
                } else if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                    Slog.v("WindowManager", "Skipping " + atoken + " -- going to hide");
                }
            }
        }
        if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
            Slog.v("WindowManager", "No app is requesting an orientation, return " + this.mLastOrientation);
        }
        return inMultiWindow ? -1 : this.mLastOrientation;
    }

    public Configuration updateOrientationFromAppTokens(Configuration currentConfig, IBinder freezeThisOneIfNeeded) {
        if (checkCallingPermission("android.permission.MANAGE_APP_TOKENS", "updateOrientationFromAppTokens()")) {
            Configuration config;
            long ident = Binder.clearCallingIdentity();
            synchronized (this.mWindowMap) {
                config = updateOrientationFromAppTokensLocked(currentConfig, freezeThisOneIfNeeded);
            }
            Binder.restoreCallingIdentity(ident);
            return config;
        }
        throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
    }

    private Configuration updateOrientationFromAppTokensLocked(Configuration currentConfig, IBinder freezeThisOneIfNeeded) {
        if (!this.mDisplayReady) {
            return null;
        }
        Configuration config = null;
        if (updateOrientationFromAppTokensLocked(false)) {
            if (!(freezeThisOneIfNeeded == null || this.mWindowPlacerLocked.mOrientationChangeComplete)) {
                AppWindowToken atoken = findAppWindowToken(freezeThisOneIfNeeded);
                if (atoken != null) {
                    startAppFreezingScreenLocked(atoken);
                }
            }
            config = computeNewConfigurationLocked();
        } else if (currentConfig != null) {
            this.mTempConfiguration.setToDefaults();
            this.mTempConfiguration.updateFrom(currentConfig);
            computeScreenConfigurationLocked(this.mTempConfiguration);
            if (currentConfig.diff(this.mTempConfiguration) != 0) {
                this.mWaitingForConfig = true;
                DisplayContent displayContent = getDefaultDisplayContentLocked();
                displayContent.layoutNeeded = true;
                int[] anim = new int[2];
                if (displayContent.isDimming()) {
                    anim[1] = 0;
                    anim[0] = 0;
                } else {
                    this.mPolicy.selectRotationAnimationLw(anim);
                }
                startFreezingDisplayLocked(false, anim[0], anim[1]);
                config = new Configuration(this.mTempConfiguration);
            }
        }
        return config;
    }

    boolean updateOrientationFromAppTokensLocked(boolean inTransaction) {
        long ident = Binder.clearCallingIdentity();
        try {
            int req = getOrientationLocked();
            if (req != this.mLastOrientation) {
                if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                    Slog.v(TAG, "updateOrientation: req= " + req + ", mLastOrientation= " + this.mLastOrientation, new Throwable("updateOrientation"));
                }
                this.mLastOrientation = req;
                this.mPolicy.setCurrentOrientationLw(req);
                if (updateRotationUncheckedLocked(inTransaction)) {
                    return true;
                }
            }
            Binder.restoreCallingIdentity(ident);
            return false;
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    boolean rotationNeedsUpdateLocked() {
        int rotation = this.mPolicy.rotationForOrientationLw(this.mLastOrientation, this.mRotation);
        boolean altOrientation = !this.mPolicy.rotationHasCompatibleMetricsLw(this.mLastOrientation, rotation);
        if (this.mRotation == rotation && this.mAltOrientation == altOrientation) {
            return false;
        }
        return true;
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "Changwei.Li@Plf.SDK : Modify for oppo tp limit (narrow screen frame)", property = OppoRomType.ROM)
    public int[] setNewConfiguration(Configuration config) {
        if (checkCallingPermission("android.permission.MANAGE_APP_TOKENS", "setNewConfiguration()")) {
            final File file = new File("/proc/touchpanel/oppo_tp_limit_enable");
            if (this.mContext.getPackageManager().hasSystemFeature("oppo.tp.limit.support") && this.mCurConfiguration.orientation != config.orientation && file.exists()) {
                final String enabled = config.orientation == 2 ? "0" : LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON;
                new Thread() {
                    public void run() {
                        try {
                            FileWriter oppo_tp_limit = new FileWriter(file);
                            oppo_tp_limit.write(enabled);
                            oppo_tp_limit.close();
                        } catch (IOException e) {
                            Slog.w(WindowManagerService.TAG, "Failed to write to oppo_tp_limit_enable:" + e.getMessage());
                        }
                    }
                }.start();
            }
            synchronized (this.mWindowMap) {
                if (this.mWaitingForConfig) {
                    this.mWaitingForConfig = false;
                    this.mLastFinishedFreezeSource = "new-config";
                }
                if ((this.mCurConfiguration.diff(config) != 0) || this.mRotationChanged) {
                    this.mRotationChanged = false;
                    prepareFreezingAllTaskBounds();
                    this.mCurConfiguration = new Configuration(config);
                    int[] onConfigurationChanged = onConfigurationChanged();
                    return onConfigurationChanged;
                }
                return null;
            }
        }
        throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
    }

    public Rect getBoundsForNewConfiguration(int stackId) {
        Rect outBounds;
        synchronized (this.mWindowMap) {
            TaskStack stack = (TaskStack) this.mStackIdToStack.get(stackId);
            outBounds = new Rect();
            stack.getBoundsForNewConfiguration(outBounds);
        }
        return outBounds;
    }

    private void prepareFreezingAllTaskBounds() {
        for (int i = this.mDisplayContents.size() - 1; i >= 0; i--) {
            ArrayList<TaskStack> stacks = ((DisplayContent) this.mDisplayContents.valueAt(i)).getStacks();
            for (int stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
                ((TaskStack) stacks.get(stackNdx)).prepareFreezingTaskBounds();
            }
        }
    }

    private int[] onConfigurationChanged() {
        this.mPolicy.onConfigurationChanged();
        DisplayContent defaultDisplayContent = getDefaultDisplayContentLocked();
        if (!this.mReconfigureOnConfigurationChanged.contains(defaultDisplayContent)) {
            this.mReconfigureOnConfigurationChanged.add(defaultDisplayContent);
        }
        for (int i = this.mReconfigureOnConfigurationChanged.size() - 1; i >= 0; i--) {
            reconfigureDisplayLocked((DisplayContent) this.mReconfigureOnConfigurationChanged.remove(i));
        }
        defaultDisplayContent.getDockedDividerController().onConfigurationChanged();
        this.mChangedStackList.clear();
        for (int stackNdx = this.mStackIdToStack.size() - 1; stackNdx >= 0; stackNdx--) {
            TaskStack stack = (TaskStack) this.mStackIdToStack.valueAt(stackNdx);
            if (stack.onConfigurationChanged()) {
                this.mChangedStackList.add(Integer.valueOf(stack.mStackId));
            }
        }
        return this.mChangedStackList.isEmpty() ? null : ArrayUtils.convertToIntArray(this.mChangedStackList);
    }

    /* JADX WARNING: Missing block: B:17:0x0069, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setAppOrientation(IApplicationToken token, int requestedOrientation) {
        if (checkCallingPermission("android.permission.MANAGE_APP_TOKENS", "setAppOrientation()")) {
            synchronized (this.mWindowMap) {
                AppWindowToken atoken = findAppWindowToken(token.asBinder());
                if (atoken == null) {
                    Slog.w("WindowManager", "Attempted to set orientation of non-existing app token: " + token);
                    return;
                }
                atoken.requestedOrientation = requestedOrientation;
                if (!IS_USER_BUILD) {
                    Slog.d(TAG, "setAppOrientation to " + requestedOrientation + ", app:" + atoken);
                }
            }
        } else {
            throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
        }
    }

    public int getAppOrientation(IApplicationToken token) {
        synchronized (this.mWindowMap) {
            AppWindowToken wtoken = findAppWindowToken(token.asBinder());
            if (wtoken == null) {
                return -1;
            }
            int i = wtoken.requestedOrientation;
            return i;
        }
    }

    void setFocusTaskRegionLocked() {
        if (this.mFocusedApp != null) {
            Task task = this.mFocusedApp.mTask;
            DisplayContent displayContent = task.getDisplayContent();
            if (displayContent != null) {
                displayContent.setTouchExcludeRegion(task);
            }
        }
    }

    public void setFocusedApp(IBinder token, boolean moveFocusNow) {
        if (checkCallingPermission("android.permission.MANAGE_APP_TOKENS", "setFocusedApp()")) {
            synchronized (this.mWindowMap) {
                AppWindowToken newFocus;
                if (token == null) {
                    if (WindowManagerDebugConfig.DEBUG_FOCUS_LIGHT) {
                        Slog.v("WindowManager", "Clearing focused app, was " + this.mFocusedApp);
                    }
                    newFocus = null;
                } else {
                    newFocus = findAppWindowToken(token);
                    if (newFocus == null) {
                        Slog.w("WindowManager", "Attempted to set focus to non-existing app token: " + token);
                    }
                    if (WindowManagerDebugConfig.DEBUG_FOCUS_LIGHT) {
                        Slog.v("WindowManager", "Set focused app to: " + newFocus + " old focus=" + this.mFocusedApp + " moveFocusNow=" + moveFocusNow);
                    }
                }
                boolean changed = this.mFocusedApp != newFocus;
                if (changed) {
                    if (!"0".equals(SystemProperties.get(APP_FROZEN_TIMEOUT_PROP))) {
                        SystemProperties.set(APP_FROZEN_TIMEOUT_PROP, "0");
                    }
                    this.mFocusedApp = newFocus;
                    this.mInputMonitor.setFocusedAppLw(newFocus);
                    setFocusTaskRegionLocked();
                } else if (newFocus != null) {
                    this.mFocusedApp = newFocus;
                    this.mInputMonitor.setFocusedAppLw(newFocus);
                }
                if (moveFocusNow && changed) {
                    long origId = Binder.clearCallingIdentity();
                    updateFocusedWindowLocked(0, true);
                    Binder.restoreCallingIdentity(origId);
                }
            }
            return;
        }
        throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
    }

    public void prepareAppTransition(int transit, boolean alwaysKeepCurrent) {
        if (checkCallingPermission("android.permission.MANAGE_APP_TOKENS", "prepareAppTransition()")) {
            synchronized (this.mWindowMap) {
                if (this.mAppTransition.prepareAppTransitionLocked(transit, alwaysKeepCurrent) && okToDisplay()) {
                    this.mSkipAppTransitionAnimation = false;
                }
            }
            return;
        }
        throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
    }

    public int getPendingAppTransition() {
        return this.mAppTransition.getAppTransition();
    }

    public void overridePendingAppTransition(String packageName, int enterAnim, int exitAnim, IRemoteCallback startedCallback) {
        synchronized (this.mWindowMap) {
            this.mAppTransition.overridePendingAppTransition(packageName, enterAnim, exitAnim, startedCallback);
        }
    }

    public void overridePendingAppTransitionScaleUp(int startX, int startY, int startWidth, int startHeight) {
        synchronized (this.mWindowMap) {
            this.mAppTransition.overridePendingAppTransitionScaleUp(startX, startY, startWidth, startHeight);
        }
    }

    public void overridePendingAppTransitionClipReveal(int startX, int startY, int startWidth, int startHeight) {
        synchronized (this.mWindowMap) {
            this.mAppTransition.overridePendingAppTransitionClipReveal(startX, startY, startWidth, startHeight);
        }
    }

    public void overridePendingAppTransitionThumb(Bitmap srcThumb, int startX, int startY, IRemoteCallback startedCallback, boolean scaleUp) {
        synchronized (this.mWindowMap) {
            this.mAppTransition.overridePendingAppTransitionThumb(srcThumb, startX, startY, startedCallback, scaleUp);
        }
    }

    public void overridePendingAppTransitionAspectScaledThumb(Bitmap srcThumb, int startX, int startY, int targetWidth, int targetHeight, IRemoteCallback startedCallback, boolean scaleUp) {
        synchronized (this.mWindowMap) {
            this.mAppTransition.overridePendingAppTransitionAspectScaledThumb(srcThumb, startX, startY, targetWidth, targetHeight, startedCallback, scaleUp);
        }
    }

    public void overridePendingAppTransitionMultiThumb(AppTransitionAnimationSpec[] specs, IRemoteCallback onAnimationStartedCallback, IRemoteCallback onAnimationFinishedCallback, boolean scaleUp) {
        synchronized (this.mWindowMap) {
            this.mAppTransition.overridePendingAppTransitionMultiThumb(specs, onAnimationStartedCallback, onAnimationFinishedCallback, scaleUp);
            prolongAnimationsFromSpecs(specs, scaleUp);
        }
    }

    void prolongAnimationsFromSpecs(AppTransitionAnimationSpec[] specs, boolean scaleUp) {
        this.mTmpTaskIds.clear();
        for (int i = specs.length - 1; i >= 0; i--) {
            this.mTmpTaskIds.put(specs[i].taskId, 0);
        }
        for (WindowState win : this.mWindowMap.values()) {
            Task task = win.getTask();
            if (!(task == null || this.mTmpTaskIds.get(task.mTaskId, -1) == -1 || !task.inFreeformWorkspace())) {
                AppWindowToken appToken = win.mAppToken;
                if (!(appToken == null || appToken.mAppAnimator == null)) {
                    appToken.mAppAnimator.startProlongAnimation(scaleUp ? 2 : 1);
                }
            }
        }
    }

    public void overridePendingAppTransitionInPlace(String packageName, int anim) {
        synchronized (this.mWindowMap) {
            this.mAppTransition.overrideInPlaceAppTransition(packageName, anim);
        }
    }

    public void overridePendingAppTransitionMultiThumbFuture(IAppTransitionAnimationSpecsFuture specsFuture, IRemoteCallback callback, boolean scaleUp) {
        synchronized (this.mWindowMap) {
            this.mAppTransition.overridePendingAppTransitionMultiThumbFuture(specsFuture, callback, scaleUp);
        }
    }

    public void endProlongedAnimations() {
        synchronized (this.mWindowMap) {
            for (WindowState win : this.mWindowMap.values()) {
                AppWindowToken appToken = win.mAppToken;
                if (!(appToken == null || appToken.mAppAnimator == null)) {
                    appToken.mAppAnimator.endProlongedAnimation();
                }
            }
            this.mAppTransition.notifyProlongedAnimationsEnded();
        }
    }

    public void executeAppTransition() {
        if (checkCallingPermission("android.permission.MANAGE_APP_TOKENS", "executeAppTransition()")) {
            synchronized (this.mWindowMap) {
                if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS || DEBUG_WMS) {
                    Slog.w("WindowManager", "Execute app transition: " + this.mAppTransition + " Callers=" + Debug.getCallers(5));
                }
                if (this.mAppTransition.isTransitionSet()) {
                    this.mAppTransition.setReady();
                    long origId = Binder.clearCallingIdentity();
                    try {
                        this.mWindowPlacerLocked.performSurfacePlacement();
                        Binder.restoreCallingIdentity(origId);
                    } catch (Throwable th) {
                        Binder.restoreCallingIdentity(origId);
                    }
                }
            }
            return;
        }
        throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
    }

    public boolean setAppStartingWindow(IBinder token, String pkg, int theme, CompatibilityInfo compatInfo, CharSequence nonLocalizedLabel, int labelRes, int icon, int logo, int windowFlags, IBinder transferFrom, boolean createIfNeeded) {
        if (checkCallingPermission("android.permission.MANAGE_APP_TOKENS", "setAppStartingWindow()")) {
            synchronized (this.mWindowMap) {
                if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
                    Slog.v("WindowManager", "setAppStartingWindow: token=" + token + " pkg=" + pkg + " transferFrom=" + transferFrom);
                }
                AppWindowToken wtoken = findAppWindowToken(token);
                if (wtoken == null) {
                    Slog.w("WindowManager", "Attempted to set icon of non-existing app token: " + token);
                    return false;
                } else if (!okToDisplay()) {
                    return false;
                } else if (wtoken.startingData != null) {
                    return false;
                } else {
                    if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
                        Slog.v("WindowManager", "Checking theme of starting window: 0x" + Integer.toHexString(theme));
                    }
                    if (theme != 0) {
                        Entry ent = AttributeCache.instance().get(pkg, theme, R.styleable.Window, this.mCurrentUserId);
                        if (ent == null) {
                            return false;
                        }
                        boolean windowIsTranslucent = ent.array.getBoolean(5, false);
                        boolean windowIsFloating = ent.array.getBoolean(4, false);
                        boolean windowShowWallpaper = ent.array.getBoolean(14, false);
                        boolean windowDisableStarting = ent.array.getBoolean(12, false);
                        if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
                            Slog.v("WindowManager", "Translucent=" + windowIsTranslucent + " Floating=" + windowIsFloating + " ShowWallpaper=" + windowShowWallpaper);
                        }
                        if (windowIsTranslucent) {
                            return false;
                        } else if (windowIsFloating || windowDisableStarting) {
                            return false;
                        } else if (windowShowWallpaper) {
                            if (this.mWallpaperControllerLocked.getWallpaperTarget() == null) {
                                windowFlags |= DumpState.DUMP_DEXOPT;
                            } else {
                                return false;
                            }
                        }
                    }
                    if (transferStartingWindow(transferFrom, wtoken)) {
                        return true;
                    } else if (createIfNeeded) {
                        if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
                            Slog.v("WindowManager", "Creating StartingData");
                        }
                        wtoken.startingData = new StartingData(pkg, theme, compatInfo, nonLocalizedLabel, labelRes, icon, logo, windowFlags);
                        Message m = this.mH.obtainMessage(5, wtoken);
                        if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
                            Slog.v("WindowManager", "Enqueueing ADD_STARTING");
                        }
                        this.mH.sendMessageAtFrontOfQueue(m);
                        return true;
                    } else {
                        return false;
                    }
                }
            }
        }
        throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
    }

    private boolean transferStartingWindow(IBinder transferFrom, AppWindowToken wtoken) {
        if (transferFrom == null) {
            return false;
        }
        AppWindowToken ttoken = findAppWindowToken(transferFrom);
        if (ttoken == null) {
            return false;
        }
        WindowState startingWindow = ttoken.startingWindow;
        if (startingWindow != null && ttoken.startingView != null) {
            this.mSkipAppTransitionAnimation = true;
            if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
                Slog.v("WindowManager", "Moving existing starting " + startingWindow + " from " + ttoken + " to " + wtoken);
            }
            long origId = Binder.clearCallingIdentity();
            wtoken.startingData = ttoken.startingData;
            wtoken.startingView = ttoken.startingView;
            wtoken.startingDisplayed = ttoken.startingDisplayed;
            ttoken.startingDisplayed = false;
            wtoken.startingWindow = startingWindow;
            wtoken.reportedVisible = ttoken.reportedVisible;
            ttoken.startingData = null;
            ttoken.startingView = null;
            ttoken.startingWindow = null;
            ttoken.startingMoved = true;
            startingWindow.mToken = wtoken;
            startingWindow.mRootToken = wtoken;
            startingWindow.mAppToken = wtoken;
            if (WindowManagerDebugConfig.DEBUG_WINDOW_MOVEMENT || WindowManagerDebugConfig.DEBUG_ADD_REMOVE || WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
                Slog.v("WindowManager", "Removing starting window: " + startingWindow);
            }
            startingWindow.getWindowList().remove(startingWindow);
            this.mWindowsChanged = true;
            if (WindowManagerDebugConfig.DEBUG_ADD_REMOVE) {
                Slog.v("WindowManager", "Removing starting " + startingWindow + " from " + ttoken);
            }
            ttoken.windows.remove(startingWindow);
            ttoken.allAppWindows.remove(startingWindow);
            addWindowToListInOrderLocked(startingWindow, true);
            if (ttoken.allDrawn) {
                wtoken.allDrawn = true;
                wtoken.deferClearAllDrawn = ttoken.deferClearAllDrawn;
            }
            if (ttoken.firstWindowDrawn) {
                wtoken.firstWindowDrawn = true;
            }
            if (!ttoken.hidden) {
                wtoken.hidden = false;
                wtoken.hiddenRequested = false;
            }
            if (wtoken.clientHidden != ttoken.clientHidden) {
                wtoken.clientHidden = ttoken.clientHidden;
                wtoken.sendAppVisibilityToClients();
            }
            ttoken.mAppAnimator.transferCurrentAnimation(wtoken.mAppAnimator, startingWindow.mWinAnimator);
            updateFocusedWindowLocked(3, true);
            getDefaultDisplayContentLocked().layoutNeeded = true;
            this.mWindowPlacerLocked.performSurfacePlacement();
            Binder.restoreCallingIdentity(origId);
            return true;
        } else if (ttoken.startingData != null) {
            if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
                Slog.v("WindowManager", "Moving pending starting from " + ttoken + " to " + wtoken);
            }
            wtoken.startingData = ttoken.startingData;
            ttoken.startingData = null;
            ttoken.startingMoved = true;
            this.mH.sendMessageAtFrontOfQueue(this.mH.obtainMessage(5, wtoken));
            return true;
        } else {
            AppWindowAnimator tAppAnimator = ttoken.mAppAnimator;
            AppWindowAnimator wAppAnimator = wtoken.mAppAnimator;
            if (tAppAnimator.thumbnail != null) {
                if (wAppAnimator.thumbnail != null) {
                    wAppAnimator.thumbnail.destroy();
                }
                wAppAnimator.thumbnail = tAppAnimator.thumbnail;
                wAppAnimator.thumbnailLayer = tAppAnimator.thumbnailLayer;
                wAppAnimator.thumbnailAnimation = tAppAnimator.thumbnailAnimation;
                tAppAnimator.thumbnail = null;
            }
            return false;
        }
    }

    public void removeAppStartingWindow(IBinder token) {
        synchronized (this.mWindowMap) {
            scheduleRemoveStartingWindowLocked(((WindowToken) this.mTokenMap.get(token)).appWindowToken);
        }
    }

    public void setAppFullscreen(IBinder token, boolean toOpaque) {
        synchronized (this.mWindowMap) {
            AppWindowToken atoken = findAppWindowToken(token);
            if (atoken != null) {
                atoken.appFullscreen = toOpaque;
                setWindowOpaqueLocked(token, toOpaque);
                this.mWindowPlacerLocked.requestTraversal();
            }
        }
    }

    public void setWindowOpaque(IBinder token, boolean isOpaque) {
        synchronized (this.mWindowMap) {
            setWindowOpaqueLocked(token, isOpaque);
        }
    }

    public void setWindowOpaqueLocked(IBinder token, boolean isOpaque) {
        AppWindowToken wtoken = findAppWindowToken(token);
        if (wtoken != null) {
            WindowState win = wtoken.findMainWindow();
            if (win != null) {
                win.mWinAnimator.setOpaqueLocked(isOpaque);
            }
        }
    }

    boolean setTokenVisibilityLocked(AppWindowToken wtoken, LayoutParams lp, boolean visible, int transit, boolean performLayout, boolean isVoiceInteraction) {
        int i;
        boolean delayed = false;
        if (wtoken.clientHidden == visible) {
            wtoken.clientHidden = !visible;
            wtoken.sendAppVisibilityToClients();
        }
        boolean visibilityChanged = false;
        if (wtoken.hidden == visible || ((wtoken.hidden && wtoken.mIsExiting) || (visible && wtoken.waitingForReplacement()))) {
            boolean changed = false;
            if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                Slog.v("WindowManager", "Changing app " + wtoken + " hidden=" + wtoken.hidden + " performLayout=" + performLayout);
            }
            boolean runningAppAnimation = false;
            if (transit != -1) {
                if (wtoken.mAppAnimator.animation == AppWindowAnimator.sDummyAnimation) {
                    wtoken.mAppAnimator.setNullAnimation();
                }
                if (applyAnimationLocked(wtoken, lp, transit, visible, isVoiceInteraction)) {
                    runningAppAnimation = true;
                    delayed = true;
                }
                WindowState window = wtoken.findMainWindow();
                if (!(window == null || this.mAccessibilityController == null || window.getDisplayId() != 0)) {
                    this.mAccessibilityController.onAppWindowTransitionLocked(window, transit);
                }
                changed = true;
            }
            int windowsCount = wtoken.allAppWindows.size();
            for (i = 0; i < windowsCount; i++) {
                WindowState win = (WindowState) wtoken.allAppWindows.get(i);
                if (win == wtoken.startingWindow) {
                    if (!visible && win.isVisibleNow() && wtoken.mAppAnimator.isAnimating()) {
                        win.mAnimatingExit = true;
                        win.mRemoveOnExit = true;
                        win.mWindowRemovalAllowed = true;
                    }
                } else if (visible) {
                    if (!win.isVisibleNow()) {
                        if (!runningAppAnimation) {
                            win.mWinAnimator.applyAnimationLocked(1, true);
                            if (this.mAccessibilityController != null && win.getDisplayId() == 0) {
                                this.mAccessibilityController.onWindowTransitionLocked(win, 1);
                            }
                        }
                        changed = true;
                        win.setDisplayLayoutNeeded();
                    }
                } else if (win.isVisibleNow()) {
                    if (!runningAppAnimation) {
                        win.mWinAnimator.applyAnimationLocked(2, false);
                        if (this.mAccessibilityController != null && win.getDisplayId() == 0) {
                            this.mAccessibilityController.onWindowTransitionLocked(win, 2);
                        }
                    }
                    changed = true;
                    win.setDisplayLayoutNeeded();
                }
            }
            boolean z = !visible;
            wtoken.hiddenRequested = z;
            wtoken.hidden = z;
            visibilityChanged = true;
            if (visible) {
                WindowState swin = wtoken.startingWindow;
                if (!(swin == null || swin.isDrawnLw())) {
                    swin.mPolicyVisibility = false;
                    swin.mPolicyVisibilityAfterAnim = false;
                }
            } else {
                unsetAppFreezingScreenLocked(wtoken, true, true);
            }
            if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                Slog.v("WindowManager", "setTokenVisibilityLocked: " + wtoken + ": hidden=" + wtoken.hidden + " hiddenRequested=" + wtoken.hiddenRequested);
            }
            if (changed) {
                this.mInputMonitor.setUpdateInputWindowsNeededLw();
                if (performLayout) {
                    updateFocusedWindowLocked(3, false);
                    this.mWindowPlacerLocked.performSurfacePlacement();
                }
                this.mInputMonitor.updateInputWindowsLw(false);
            }
        }
        if (wtoken.mAppAnimator.animation != null) {
            delayed = true;
        }
        for (i = wtoken.allAppWindows.size() - 1; i >= 0 && !delayed; i--) {
            if (((WindowState) wtoken.allAppWindows.get(i)).mWinAnimator.isWindowAnimationSet()) {
                delayed = true;
            }
        }
        if (visibilityChanged) {
            if (visible && !delayed) {
                wtoken.mEnteringAnimation = true;
                this.mActivityManagerAppTransitionNotifier.onAppTransitionFinishedLocked(wtoken.token);
            }
            if (!(this.mClosingApps.contains(wtoken) || this.mOpeningApps.contains(wtoken))) {
                getDefaultDisplayContentLocked().getDockedDividerController().notifyAppVisibilityChanged();
            }
        }
        return delayed;
    }

    void updateTokenInPlaceLocked(AppWindowToken wtoken, int transit) {
        if (transit != -1) {
            if (wtoken.mAppAnimator.animation == AppWindowAnimator.sDummyAnimation) {
                wtoken.mAppAnimator.setNullAnimation();
            }
            applyAnimationLocked(wtoken, null, transit, false, false);
        }
    }

    public void notifyAppResumed(IBinder token, boolean wasStopped, boolean allowSavedSurface) {
        if (checkCallingPermission("android.permission.MANAGE_APP_TOKENS", "notifyAppResumed()")) {
            synchronized (this.mWindowMap) {
                AppWindowToken wtoken = findAppWindowToken(token);
                if (wtoken == null) {
                    Slog.w("WindowManager", "Attempted to notify resumed of non-existing app token: " + token);
                    return;
                }
                wtoken.notifyAppResumed(wasStopped, allowSavedSurface);
                return;
            }
        }
        throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
    }

    public void notifyAppStopped(IBinder token) {
        if (checkCallingPermission("android.permission.MANAGE_APP_TOKENS", "notifyAppStopped()")) {
            synchronized (this.mWindowMap) {
                AppWindowToken wtoken = findAppWindowToken(token);
                if (wtoken == null) {
                    Slog.w("WindowManager", "Attempted to notify stopped of non-existing app token: " + token);
                    return;
                }
                wtoken.notifyAppStopped();
                return;
            }
        }
        throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
    }

    /* JADX WARNING: Missing block: B:60:0x0180, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setAppVisibility(IBinder token, boolean visible) {
        boolean z = false;
        if (checkCallingPermission("android.permission.MANAGE_APP_TOKENS", "setAppVisibility()")) {
            synchronized (this.mWindowMap) {
                AppWindowToken wtoken = findAppWindowToken(token);
                if (wtoken == null) {
                    Slog.w("WindowManager", "Attempted to set visibility of non-existing app token: " + token);
                    return;
                }
                if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS || WindowManagerDebugConfig.DEBUG_ORIENTATION || !IS_USER_BUILD || DEBUG_WMS) {
                    Slog.v("WindowManager", "setAppVisibility(" + token + ", visible=" + visible + "): " + this.mAppTransition + " hidden=" + wtoken.hidden + " hiddenRequested=" + wtoken.hiddenRequested + " Callers=" + Debug.getCallers(6));
                }
                this.mOpeningApps.remove(wtoken);
                this.mClosingApps.remove(wtoken);
                wtoken.waitingToShow = false;
                if (!visible) {
                    z = true;
                }
                wtoken.hiddenRequested = z;
                if (!visible) {
                    wtoken.removeAllDeadWindows();
                    wtoken.setVisibleBeforeClientHidden();
                } else if (visible) {
                    if (!this.mAppTransition.isTransitionSet() && this.mAppTransition.isReady()) {
                        this.mOpeningApps.add(wtoken);
                    }
                    wtoken.startingMoved = false;
                    if (wtoken.hidden || wtoken.mAppStopped) {
                        wtoken.clearAllDrawn();
                        if (wtoken.hidden) {
                            wtoken.waitingToShow = true;
                        }
                        if (wtoken.clientHidden) {
                            wtoken.clientHidden = false;
                            wtoken.sendAppVisibilityToClients();
                        }
                    }
                    wtoken.requestUpdateWallpaperIfNeeded();
                    if (WindowManagerDebugConfig.DEBUG_ADD_REMOVE) {
                        Slog.v("WindowManager", "No longer Stopped: " + wtoken);
                    }
                    wtoken.mAppStopped = false;
                }
                if (okToDisplay() && this.mAppTransition.isTransitionSet()) {
                    if (wtoken.mAppAnimator.usingTransferredAnimation && wtoken.mAppAnimator.animation == null) {
                        Slog.wtf("WindowManager", "Will NOT set dummy animation on: " + wtoken + ", using null transfered animation!");
                    }
                    if (!wtoken.mAppAnimator.usingTransferredAnimation && (!wtoken.startingDisplayed || this.mSkipAppTransitionAnimation)) {
                        if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                            Slog.v("WindowManager", "Setting dummy animation on: " + wtoken);
                        }
                        wtoken.mAppAnimator.setDummyAnimation();
                    }
                    wtoken.inPendingTransaction = true;
                    if (visible) {
                        this.mOpeningApps.add(wtoken);
                        wtoken.mEnteringAnimation = true;
                    } else {
                        this.mClosingApps.add(wtoken);
                        wtoken.mEnteringAnimation = false;
                    }
                    if (this.mAppTransition.getAppTransition() == 16) {
                        WindowState win = findFocusedWindowLocked(getDefaultDisplayContentLocked());
                        if (win != null) {
                            AppWindowToken focusedToken = win.mAppToken;
                            if (focusedToken != null) {
                                if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                                    Slog.d("WindowManager", "TRANSIT_TASK_OPEN_BEHIND,  adding " + focusedToken + " to mOpeningApps");
                                }
                                focusedToken.hidden = true;
                                this.mOpeningApps.add(focusedToken);
                            }
                        }
                    }
                } else {
                    long origId = Binder.clearCallingIdentity();
                    wtoken.inPendingTransaction = false;
                    setTokenVisibilityLocked(wtoken, null, visible, -1, true, wtoken.voiceInteraction);
                    wtoken.updateReportedVisibilityLocked();
                    Binder.restoreCallingIdentity(origId);
                    return;
                }
            }
        }
        throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
    }

    void unsetAppFreezingScreenLocked(AppWindowToken wtoken, boolean unfreezeSurfaceNow, boolean force) {
        if (wtoken.mAppAnimator.freezingScreen) {
            if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                Slog.v("WindowManager", "Clear freezing of " + wtoken + " force=" + force);
            }
            int N = wtoken.allAppWindows.size();
            boolean unfrozeWindows = false;
            for (int i = 0; i < N; i++) {
                WindowState w = (WindowState) wtoken.allAppWindows.get(i);
                if (w.mAppFreezing) {
                    w.mAppFreezing = false;
                    if (!(!w.mHasSurface || w.mOrientationChanging || this.mWindowsFreezingScreen == 2)) {
                        if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                            Slog.v("WindowManager", "set mOrientationChanging of " + w);
                        }
                        w.mOrientationChanging = true;
                        this.mWindowPlacerLocked.mOrientationChangeComplete = false;
                    }
                    w.mLastFreezeDuration = 0;
                    unfrozeWindows = true;
                    w.setDisplayLayoutNeeded();
                }
            }
            if (force || unfrozeWindows) {
                if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                    Slog.v("WindowManager", "No longer freezing: " + wtoken);
                }
                wtoken.mAppAnimator.freezingScreen = false;
                wtoken.mAppAnimator.lastFreezeDuration = (int) (SystemClock.elapsedRealtime() - this.mDisplayFreezeTime);
                this.mAppsFreezingScreen--;
                this.mLastFinishedFreezeSource = wtoken;
            }
            if (unfreezeSurfaceNow) {
                if (unfrozeWindows) {
                    this.mWindowPlacerLocked.performSurfacePlacement();
                }
                stopFreezingDisplayLocked();
            }
        }
    }

    private void startAppFreezingScreenLocked(AppWindowToken wtoken) {
        if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
            logWithStack(TAG, "Set freezing of " + wtoken.appToken + ": hidden=" + wtoken.hidden + " freezing=" + wtoken.mAppAnimator.freezingScreen);
        }
        if (!wtoken.hiddenRequested) {
            if (!wtoken.mAppAnimator.freezingScreen) {
                wtoken.mAppAnimator.freezingScreen = true;
                wtoken.mAppAnimator.lastFreezeDuration = 0;
                this.mAppsFreezingScreen++;
                if (this.mAppsFreezingScreen == 1) {
                    startFreezingDisplayLocked(false, 0, 0);
                    this.mH.removeMessages(17);
                    this.mH.sendEmptyMessageDelayed(17, 2000);
                }
            }
            int N = wtoken.allAppWindows.size();
            for (int i = 0; i < N; i++) {
                ((WindowState) wtoken.allAppWindows.get(i)).mAppFreezing = true;
            }
        }
    }

    /* JADX WARNING: Missing block: B:9:0x001e, code:
            if (okToDisplay() == false) goto L_0x0020;
     */
    /* JADX WARNING: Missing block: B:16:0x0048, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void startAppFreezingScreen(IBinder token, int configChanges) {
        if (checkCallingPermission("android.permission.MANAGE_APP_TOKENS", "setAppFreezingScreen()")) {
            synchronized (this.mWindowMap) {
                if (configChanges == 0) {
                }
                if (!SystemService.isRunning(BOOT_ANIMATION_SERVICE)) {
                    AppWindowToken wtoken = findAppWindowToken(token);
                    if (wtoken == null || wtoken.appToken == null) {
                        Slog.w("WindowManager", "Attempted to freeze screen with non-existing app token: " + wtoken);
                        return;
                    }
                    long origId = Binder.clearCallingIdentity();
                    startAppFreezingScreenLocked(wtoken);
                    Binder.restoreCallingIdentity(origId);
                    return;
                }
                if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                    Slog.v("WindowManager", "Skipping set freeze of " + token);
                }
            }
        } else {
            throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
        }
    }

    /* JADX WARNING: Missing block: B:12:0x0023, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void stopAppFreezingScreen(IBinder token, boolean force) {
        if (checkCallingPermission("android.permission.MANAGE_APP_TOKENS", "setAppFreezingScreen()")) {
            synchronized (this.mWindowMap) {
                AppWindowToken wtoken = findAppWindowToken(token);
                if (wtoken == null || wtoken.appToken == null) {
                } else {
                    long origId = Binder.clearCallingIdentity();
                    if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                        Slog.v("WindowManager", "Clear freezing of " + token + ": hidden=" + wtoken.hidden + " freezing=" + wtoken.mAppAnimator.freezingScreen);
                    }
                    unsetAppFreezingScreenLocked(wtoken, true, force);
                    Binder.restoreCallingIdentity(origId);
                    return;
                }
            }
        }
        throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
    }

    public void removeAppToken(IBinder token) {
        if (checkCallingPermission("android.permission.MANAGE_APP_TOKENS", "removeAppToken()")) {
            AppWindowToken appWindowToken = null;
            AppWindowToken startingToken = null;
            boolean delayed = false;
            long origId = Binder.clearCallingIdentity();
            synchronized (this.mWindowMap) {
                WindowToken basewtoken = (WindowToken) this.mTokenMap.remove(token);
                if (basewtoken != null) {
                    appWindowToken = basewtoken.appWindowToken;
                    if (appWindowToken != null) {
                        if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                            Slog.v("WindowManager", "Removing app token: " + appWindowToken);
                        }
                        delayed = setTokenVisibilityLocked(appWindowToken, null, false, -1, true, appWindowToken.voiceInteraction);
                        appWindowToken.inPendingTransaction = false;
                        this.mOpeningApps.remove(appWindowToken);
                        appWindowToken.waitingToShow = false;
                        if (this.mClosingApps.contains(appWindowToken)) {
                            delayed = true;
                        } else if (this.mAppTransition.isTransitionSet()) {
                            this.mClosingApps.add(appWindowToken);
                            delayed = true;
                        }
                        if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                            Slog.v("WindowManager", "Removing app " + appWindowToken + " delayed=" + delayed + " animation=" + appWindowToken.mAppAnimator.animation + " animating=" + appWindowToken.mAppAnimator.animating);
                        }
                        if (WindowManagerDebugConfig.DEBUG_ADD_REMOVE || WindowManagerDebugConfig.DEBUG_TOKEN_MOVEMENT) {
                            Slog.v("WindowManager", "removeAppToken: " + appWindowToken + " delayed=" + delayed + " Callers=" + Debug.getCallers(4));
                        }
                        TaskStack stack = appWindowToken.mTask.mStack;
                        if (!delayed || appWindowToken.allAppWindows.isEmpty()) {
                            appWindowToken.mAppAnimator.clearAnimation();
                            appWindowToken.mAppAnimator.animating = false;
                            appWindowToken.removeAppFromTaskLocked();
                        } else {
                            if (WindowManagerDebugConfig.DEBUG_ADD_REMOVE || WindowManagerDebugConfig.DEBUG_TOKEN_MOVEMENT) {
                                Slog.v("WindowManager", "removeAppToken make exiting: " + appWindowToken);
                            }
                            stack.mExitingAppTokens.add(appWindowToken);
                            appWindowToken.mIsExiting = true;
                        }
                        appWindowToken.removed = true;
                        if (appWindowToken.startingData != null) {
                            startingToken = appWindowToken;
                        }
                        unsetAppFreezingScreenLocked(appWindowToken, true, true);
                        if (this.mFocusedApp == appWindowToken) {
                            if (WindowManagerDebugConfig.DEBUG_FOCUS_LIGHT) {
                                Slog.v("WindowManager", "Removing focused app token:" + appWindowToken);
                            }
                            this.mFocusedApp = null;
                            updateFocusedWindowLocked(0, true);
                            this.mInputMonitor.setFocusedAppLw(null);
                        }
                        if (!(delayed || appWindowToken == null)) {
                            appWindowToken.updateReportedVisibilityLocked();
                        }
                        scheduleRemoveStartingWindowLocked(startingToken);
                    }
                }
                Slog.w("WindowManager", "Attempted to remove non-existing app token: " + token);
                appWindowToken.updateReportedVisibilityLocked();
                scheduleRemoveStartingWindowLocked(startingToken);
            }
            Binder.restoreCallingIdentity(origId);
            return;
        }
        throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
    }

    void scheduleRemoveStartingWindowLocked(AppWindowToken wtoken) {
        if (wtoken != null && !this.mH.hasMessages(6, wtoken)) {
            if (wtoken.startingWindow == null) {
                if (wtoken.startingData != null) {
                    if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
                        Slog.v("WindowManager", "Clearing startingData for token=" + wtoken);
                    }
                    wtoken.startingData = null;
                }
                return;
            }
            if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
                Slog.v("WindowManager", Debug.getCallers(1) + ": Schedule remove starting " + wtoken + (wtoken != null ? " startingWindow=" + wtoken.startingWindow : IElsaManager.EMPTY_PACKAGE));
            }
            this.mH.sendMessage(this.mH.obtainMessage(6, wtoken));
        }
    }

    void dumpAppTokensLocked() {
        int numStacks = this.mStackIdToStack.size();
        for (int stackNdx = 0; stackNdx < numStacks; stackNdx++) {
            TaskStack stack = (TaskStack) this.mStackIdToStack.valueAt(stackNdx);
            Slog.v("WindowManager", "  Stack #" + stack.mStackId + " tasks from bottom to top:");
            ArrayList<Task> tasks = stack.getTasks();
            int numTasks = tasks.size();
            for (int taskNdx = 0; taskNdx < numTasks; taskNdx++) {
                Task task = (Task) tasks.get(taskNdx);
                Slog.v("WindowManager", "    Task #" + task.mTaskId + " activities from bottom to top:");
                AppTokenList tokens = task.mAppTokens;
                int numTokens = tokens.size();
                for (int tokenNdx = 0; tokenNdx < numTokens; tokenNdx++) {
                    Slog.v("WindowManager", "      activity #" + tokenNdx + ": " + ((AppWindowToken) tokens.get(tokenNdx)).token);
                }
            }
        }
    }

    void dumpWindowsLocked() {
        int numDisplays = this.mDisplayContents.size();
        for (int displayNdx = 0; displayNdx < numDisplays; displayNdx++) {
            DisplayContent displayContent = (DisplayContent) this.mDisplayContents.valueAt(displayNdx);
            Slog.v("WindowManager", " Display #" + displayContent.getDisplayId());
            WindowList windows = displayContent.getWindowList();
            for (int winNdx = windows.size() - 1; winNdx >= 0; winNdx--) {
                Slog.v("WindowManager", "  #" + winNdx + ": " + windows.get(winNdx));
            }
        }
    }

    private final int reAddWindowLocked(int index, WindowState win) {
        WindowList windows = win.getWindowList();
        int NCW = win.mChildWindows.size();
        boolean winAdded = false;
        for (int j = 0; j < NCW; j++) {
            WindowState cwin = (WindowState) win.mChildWindows.get(j);
            if (!winAdded && cwin.mSubLayer >= 0) {
                if (WindowManagerDebugConfig.DEBUG_WINDOW_MOVEMENT) {
                    Slog.v("WindowManager", "Re-adding child window at " + index + ": " + cwin);
                }
                win.mRebuilding = false;
                windows.add(index, win);
                index++;
                winAdded = true;
            }
            if (WindowManagerDebugConfig.DEBUG_WINDOW_MOVEMENT) {
                Slog.v("WindowManager", "Re-adding window at " + index + ": " + cwin);
            }
            cwin.mRebuilding = false;
            windows.add(index, cwin);
            index++;
        }
        if (!winAdded) {
            if (WindowManagerDebugConfig.DEBUG_WINDOW_MOVEMENT) {
                Slog.v("WindowManager", "Re-adding window at " + index + ": " + win);
            }
            win.mRebuilding = false;
            windows.add(index, win);
            index++;
        }
        this.mWindowsChanged = true;
        return index;
    }

    private final int reAddAppWindowsLocked(DisplayContent displayContent, int index, WindowToken token) {
        int NW = token.windows.size();
        for (int i = 0; i < NW; i++) {
            WindowState win = (WindowState) token.windows.get(i);
            DisplayContent winDisplayContent = win.getDisplayContent();
            if (winDisplayContent == displayContent || winDisplayContent == null) {
                win.mDisplayContent = displayContent;
                index = reAddWindowLocked(index, win);
            }
        }
        return index;
    }

    /* JADX WARNING: Removed duplicated region for block: B:26:0x007b A:{LOOP_END, LOOP:0: B:1:0x001a->B:26:0x007b} */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x004e A:{SYNTHETIC} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void moveStackWindowsLocked(DisplayContent displayContent) {
        WindowList windows = displayContent.getWindowList();
        this.mTmpWindows.addAll(windows);
        rebuildAppWindowListLocked(displayContent);
        int tmpSize = this.mTmpWindows.size();
        int winSize = windows.size();
        int tmpNdx = 0;
        int winNdx = 0;
        while (tmpNdx < tmpSize && winNdx < winSize) {
            int tmpNdx2;
            WindowState tmp;
            int winNdx2;
            WindowState win;
            while (true) {
                tmpNdx2 = tmpNdx + 1;
                tmp = (WindowState) this.mTmpWindows.get(tmpNdx);
                if (tmpNdx2 >= tmpSize || tmp.mAppToken == null || !tmp.mAppToken.mIsExiting) {
                    while (true) {
                        winNdx2 = winNdx + 1;
                        win = (WindowState) windows.get(winNdx);
                        winNdx = winNdx2;
                    }
                } else {
                    tmpNdx = tmpNdx2;
                }
            }
            while (true) {
                winNdx2 = winNdx + 1;
                win = (WindowState) windows.get(winNdx);
                if (winNdx2 < winSize && win.mAppToken != null && win.mAppToken.mIsExiting) {
                    winNdx = winNdx2;
                } else if (tmp == win) {
                    displayContent.layoutNeeded = true;
                    winNdx = winNdx2;
                    tmpNdx = tmpNdx2;
                    break;
                } else {
                    winNdx = winNdx2;
                    tmpNdx = tmpNdx2;
                }
            }
            if (tmp == win) {
            }
        }
        if (tmpNdx != winNdx) {
            displayContent.layoutNeeded = true;
        }
        this.mTmpWindows.clear();
        if (!updateFocusedWindowLocked(3, false)) {
            this.mLayersController.assignLayersLocked(displayContent.getWindowList());
        }
        this.mInputMonitor.setUpdateInputWindowsNeededLw();
        this.mWindowPlacerLocked.performSurfacePlacement();
        this.mInputMonitor.updateInputWindowsLw(false);
    }

    public void moveTaskToTop(int taskId) {
        long origId = Binder.clearCallingIdentity();
        try {
            synchronized (this.mWindowMap) {
                Task task = (Task) this.mTaskIdToTask.get(taskId);
                if (task != null) {
                    TaskStack stack = task.mStack;
                    DisplayContent displayContent = task.getDisplayContent();
                    if (displayContent != null) {
                        displayContent.moveStack(stack, true);
                        if (displayContent.isDefaultDisplay) {
                            TaskStack homeStack = displayContent.getHomeStack();
                            if (homeStack != stack) {
                                displayContent.moveStack(homeStack, false);
                            }
                        }
                        stack.moveTaskToTop(task);
                        if (this.mAppTransition.isTransitionSet()) {
                            task.setSendingToBottom(false);
                        }
                        moveStackWindowsLocked(displayContent);
                    } else {
                        RuntimeException exce = new RuntimeException("here");
                        exce.fillInStackTrace();
                        Slog.e(TAG, "moveTaskToTop failed at displayContent null!", exce);
                    }
                    Binder.restoreCallingIdentity(origId);
                }
            }
        } finally {
            Binder.restoreCallingIdentity(origId);
        }
    }

    public void moveTaskToBottom(int taskId) {
        long origId = Binder.clearCallingIdentity();
        try {
            synchronized (this.mWindowMap) {
                Task task = (Task) this.mTaskIdToTask.get(taskId);
                if (task == null) {
                    Slog.e("WindowManager", "moveTaskToBottom: taskId=" + taskId + " not found in mTaskIdToTask");
                } else {
                    TaskStack stack = task.mStack;
                    stack.moveTaskToBottom(task);
                    if (this.mAppTransition.isTransitionSet()) {
                        task.setSendingToBottom(true);
                    }
                    moveStackWindowsLocked(stack.getDisplayContent());
                    Binder.restoreCallingIdentity(origId);
                }
            }
        } finally {
            Binder.restoreCallingIdentity(origId);
        }
    }

    boolean isStackVisibleLocked(int stackId) {
        TaskStack stack = (TaskStack) this.mStackIdToStack.get(stackId);
        return stack != null ? stack.isVisibleLocked() : false;
    }

    public void setDockedStackCreateState(int mode, Rect bounds) {
        synchronized (this.mWindowMap) {
            setDockedStackCreateStateLocked(mode, bounds);
        }
    }

    void setDockedStackCreateStateLocked(int mode, Rect bounds) {
        this.mDockedStackCreateMode = mode;
        this.mDockedStackCreateBounds = bounds;
    }

    public Rect attachStack(int stackId, int displayId, boolean onTop) {
        long origId = Binder.clearCallingIdentity();
        try {
            synchronized (this.mWindowMap) {
                DisplayContent displayContent = (DisplayContent) this.mDisplayContents.get(displayId);
                boolean attachedToDisplay = false;
                if (displayContent != null) {
                    TaskStack stack = (TaskStack) this.mStackIdToStack.get(stackId);
                    if (stack == null) {
                        if (WindowManagerDebugConfig.DEBUG_STACK) {
                            Slog.d("WindowManager", "attachStack: stackId=" + stackId);
                        }
                        stack = displayContent.getStackById(stackId);
                        if (stack != null) {
                            displayContent.detachStack(stack);
                            stack.mDeferDetach = false;
                            attachedToDisplay = true;
                        } else {
                            stack = new TaskStack(this, stackId);
                        }
                        this.mStackIdToStack.put(stackId, stack);
                        if (stackId == 3) {
                            getDefaultDisplayContentLocked().mDividerControllerLocked.notifyDockedStackExistsChanged(true);
                        }
                    }
                    if (!attachedToDisplay) {
                        stack.attachDisplayContent(displayContent);
                    }
                    displayContent.attachStack(stack, onTop);
                    if (!stack.getRawFullscreen()) {
                        Rect bounds = new Rect();
                        stack.getRawBounds(bounds);
                        Binder.restoreCallingIdentity(origId);
                        return bounds;
                    }
                } else {
                    Binder.restoreCallingIdentity(origId);
                    return null;
                }
            }
            return null;
        } finally {
            Binder.restoreCallingIdentity(origId);
        }
    }

    void detachStackLocked(DisplayContent displayContent, TaskStack stack) {
        displayContent.detachStack(stack);
        stack.detachDisplay();
        if (stack.mStackId == 3) {
            getDefaultDisplayContentLocked().mDividerControllerLocked.notifyDockedStackExistsChanged(false);
        }
    }

    /* JADX WARNING: Missing block: B:15:0x0022, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void detachStack(int stackId) {
        synchronized (this.mWindowMap) {
            TaskStack stack = (TaskStack) this.mStackIdToStack.get(stackId);
            if (stack != null) {
                DisplayContent displayContent = stack.getDisplayContent();
                if (displayContent != null) {
                    if (stack.isAnimating()) {
                        stack.mDeferDetach = true;
                        return;
                    }
                    detachStackLocked(displayContent, stack);
                }
            }
        }
    }

    public void removeStack(int stackId) {
        synchronized (this.mWindowMap) {
            this.mStackIdToStack.remove(stackId);
        }
    }

    /* JADX WARNING: Missing block: B:9:0x002c, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void removeTask(int taskId) {
        synchronized (this.mWindowMap) {
            Task task = (Task) this.mTaskIdToTask.get(taskId);
            if (task != null) {
                task.removeLocked();
            } else if (WindowManagerDebugConfig.DEBUG_STACK) {
                Slog.i("WindowManager", "removeTask: could not find taskId=" + taskId);
            }
        }
    }

    public void cancelTaskWindowTransition(int taskId) {
        synchronized (this.mWindowMap) {
            Task task = (Task) this.mTaskIdToTask.get(taskId);
            if (task != null) {
                task.cancelTaskWindowTransition();
            }
        }
    }

    public void cancelTaskThumbnailTransition(int taskId) {
        synchronized (this.mWindowMap) {
            Task task = (Task) this.mTaskIdToTask.get(taskId);
            if (task != null) {
                task.cancelTaskThumbnailTransition();
            }
        }
    }

    /* JADX WARNING: Missing block: B:15:0x005a, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void addTask(int taskId, int stackId, boolean toTop) {
        synchronized (this.mWindowMap) {
            if (WindowManagerDebugConfig.DEBUG_STACK) {
                Slog.i("WindowManager", "addTask: adding taskId=" + taskId + " to " + (toTop ? "top" : "bottom"));
            }
            Task task = (Task) this.mTaskIdToTask.get(taskId);
            if (task != null) {
                TaskStack stack = (TaskStack) this.mStackIdToStack.get(stackId);
                stack.addTask(task, toTop);
                stack.getDisplayContent().layoutNeeded = true;
                this.mWindowPlacerLocked.performSurfacePlacement();
            } else if (WindowManagerDebugConfig.DEBUG_STACK) {
                Slog.i("WindowManager", "addTask: could not find taskId=" + taskId);
            }
        }
    }

    /* JADX WARNING: Missing block: B:15:0x0065, code:
            return;
     */
    /* JADX WARNING: Missing block: B:24:0x0093, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void moveTaskToStack(int taskId, int stackId, boolean toTop) {
        synchronized (this.mWindowMap) {
            if (WindowManagerDebugConfig.DEBUG_STACK) {
                Slog.i("WindowManager", "moveTaskToStack: moving taskId=" + taskId + " to stackId=" + stackId + " at " + (toTop ? "top" : "bottom"));
            }
            Task task = (Task) this.mTaskIdToTask.get(taskId);
            if (task != null) {
                TaskStack stack = (TaskStack) this.mStackIdToStack.get(stackId);
                if (stack != null) {
                    task.moveTaskToStack(stack, toTop);
                    stack.getDisplayContent().layoutNeeded = true;
                    this.mWindowPlacerLocked.performSurfacePlacement();
                } else if (WindowManagerDebugConfig.DEBUG_STACK) {
                    Slog.i("WindowManager", "moveTaskToStack: could not find stackId=" + stackId);
                }
            } else if (WindowManagerDebugConfig.DEBUG_STACK) {
                Slog.i("WindowManager", "moveTaskToStack: could not find taskId=" + taskId);
            }
        }
    }

    public void getStackDockedModeBounds(int stackId, Rect bounds, boolean ignoreVisibility) {
        synchronized (this.mWindowMap) {
            TaskStack stack = (TaskStack) this.mStackIdToStack.get(stackId);
            if (stack != null) {
                stack.getStackDockedModeBoundsLocked(bounds, ignoreVisibility);
                return;
            }
            bounds.setEmpty();
        }
    }

    public void getStackBounds(int stackId, Rect bounds) {
        synchronized (this.mWindowMap) {
            TaskStack stack = (TaskStack) this.mStackIdToStack.get(stackId);
            if (stack != null) {
                stack.getBounds(bounds);
                return;
            }
            bounds.setEmpty();
        }
    }

    public void overridePlayingAppAnimationsLw(Animation a) {
        getDefaultDisplayContentLocked().overridePlayingAppAnimationsLw(a);
    }

    public boolean resizeStack(int stackId, Rect bounds, SparseArray<Configuration> configs, SparseArray<Rect> taskBounds, SparseArray<Rect> taskTempInsetBounds) {
        boolean rawFullscreen;
        synchronized (this.mWindowMap) {
            TaskStack stack = (TaskStack) this.mStackIdToStack.get(stackId);
            if (stack == null) {
                throw new IllegalArgumentException("resizeStack: stackId " + stackId + " not found.");
            }
            if (stack.setBounds(bounds, configs, taskBounds, taskTempInsetBounds) && stack.isVisibleLocked()) {
                stack.getDisplayContent().layoutNeeded = true;
                this.mWindowPlacerLocked.performSurfacePlacement();
            }
            rawFullscreen = stack.getRawFullscreen();
        }
        return rawFullscreen;
    }

    public void prepareFreezingTaskBounds(int stackId) {
        synchronized (this.mWindowMap) {
            TaskStack stack = (TaskStack) this.mStackIdToStack.get(stackId);
            if (stack == null) {
                throw new IllegalArgumentException("prepareFreezingTaskBounds: stackId " + stackId + " not found.");
            }
            stack.prepareFreezingTaskBounds();
        }
    }

    /* JADX WARNING: Missing block: B:12:0x0060, code:
            return;
     */
    /* JADX WARNING: Missing block: B:20:0x008a, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void positionTaskInStack(int taskId, int stackId, int position, Rect bounds, Configuration config) {
        synchronized (this.mWindowMap) {
            if (WindowManagerDebugConfig.DEBUG_STACK) {
                Slog.i("WindowManager", "positionTaskInStack: positioning taskId=" + taskId + " in stackId=" + stackId + " at " + position);
            }
            Task task = (Task) this.mTaskIdToTask.get(taskId);
            if (task != null) {
                TaskStack stack = (TaskStack) this.mStackIdToStack.get(stackId);
                if (stack != null) {
                    task.positionTaskInStack(stack, position, bounds, config);
                    stack.getDisplayContent().layoutNeeded = true;
                    this.mWindowPlacerLocked.performSurfacePlacement();
                } else if (WindowManagerDebugConfig.DEBUG_STACK) {
                    Slog.i("WindowManager", "positionTaskInStack: could not find stackId=" + stackId);
                }
            } else if (WindowManagerDebugConfig.DEBUG_STACK) {
                Slog.i("WindowManager", "positionTaskInStack: could not find taskId=" + taskId);
            }
        }
    }

    public void resizeTask(int taskId, Rect bounds, Configuration configuration, boolean relayout, boolean forced) {
        synchronized (this.mWindowMap) {
            Task task = (Task) this.mTaskIdToTask.get(taskId);
            if (task == null) {
                throw new IllegalArgumentException("resizeTask: taskId " + taskId + " not found.");
            }
            if (task.resizeLocked(bounds, configuration, forced) && relayout) {
                task.getDisplayContent().layoutNeeded = true;
                this.mWindowPlacerLocked.performSurfacePlacement();
            }
        }
    }

    public void setTaskDockedResizing(int taskId, boolean resizing) {
        synchronized (this.mWindowMap) {
            Task task = (Task) this.mTaskIdToTask.get(taskId);
            if (task == null) {
                Slog.w(TAG, "setTaskDockedResizing: taskId " + taskId + " not found.");
                return;
            }
            task.setDragResizing(resizing, 1);
        }
    }

    public void scrollTask(int taskId, Rect bounds) {
        synchronized (this.mWindowMap) {
            Task task = (Task) this.mTaskIdToTask.get(taskId);
            if (task == null) {
                throw new IllegalArgumentException("scrollTask: taskId " + taskId + " not found.");
            }
            if (task.scrollLocked(bounds)) {
                task.getDisplayContent().layoutNeeded = true;
                this.mInputMonitor.setUpdateInputWindowsNeededLw();
                this.mWindowPlacerLocked.performSurfacePlacement();
            }
        }
    }

    public void deferSurfaceLayout() {
        synchronized (this.mWindowMap) {
            this.mWindowPlacerLocked.deferLayout();
        }
    }

    public void continueSurfaceLayout() {
        synchronized (this.mWindowMap) {
            this.mWindowPlacerLocked.continueLayout();
        }
    }

    public void getTaskBounds(int taskId, Rect bounds) {
        synchronized (this.mWindowMap) {
            Task task = (Task) this.mTaskIdToTask.get(taskId);
            if (task != null) {
                task.getBounds(bounds);
                return;
            }
            bounds.setEmpty();
        }
    }

    public boolean isValidTaskId(int taskId) {
        boolean z;
        synchronized (this.mWindowMap) {
            z = this.mTaskIdToTask.get(taskId) != null;
        }
        return z;
    }

    public void startFreezingScreen(int exitAnim, int enterAnim) {
        if (checkCallingPermission("android.permission.FREEZE_SCREEN", "startFreezingScreen()")) {
            synchronized (this.mWindowMap) {
                if (!this.mClientFreezingScreen) {
                    this.mClientFreezingScreen = true;
                    long origId = Binder.clearCallingIdentity();
                    try {
                        startFreezingDisplayLocked(false, exitAnim, enterAnim);
                        this.mH.removeMessages(30);
                        long timeout = (long) (this.mFreeingChange ? 500 : 5000);
                        this.mFreeingChange = false;
                        this.mH.sendEmptyMessageDelayed(30, timeout);
                        Binder.restoreCallingIdentity(origId);
                    } catch (Throwable th) {
                        Binder.restoreCallingIdentity(origId);
                    }
                }
            }
            return;
        }
        throw new SecurityException("Requires FREEZE_SCREEN permission");
    }

    public void stopFreezingScreen() {
        if (checkCallingPermission("android.permission.FREEZE_SCREEN", "stopFreezingScreen()")) {
            synchronized (this.mWindowMap) {
                if (this.mClientFreezingScreen) {
                    this.mClientFreezingScreen = false;
                    this.mLastFinishedFreezeSource = "client";
                    long origId = Binder.clearCallingIdentity();
                    try {
                        stopFreezingDisplayLocked();
                        Binder.restoreCallingIdentity(origId);
                    } catch (Throwable th) {
                        Binder.restoreCallingIdentity(origId);
                    }
                }
            }
            return;
        }
        throw new SecurityException("Requires FREEZE_SCREEN permission");
    }

    public void disableKeyguard(IBinder token, String tag) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.DISABLE_KEYGUARD") != 0) {
            throw new SecurityException("Requires DISABLE_KEYGUARD permission");
        } else if (Binder.getCallingUid() != 1000 && isKeyguardSecure()) {
            Log.d("WindowManager", "current mode is SecurityMode, ignore disableKeyguard");
        } else if (Binder.getCallingUserHandle().getIdentifier() != this.mCurrentUserId) {
            Log.d("WindowManager", "non-current user, ignore disableKeyguard");
        } else if (token == null) {
            throw new IllegalArgumentException("token == null");
        } else {
            this.mKeyguardDisableHandler.sendMessage(this.mKeyguardDisableHandler.obtainMessage(1, new Pair(token, tag)));
        }
    }

    public void reenableKeyguard(IBinder token) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.DISABLE_KEYGUARD") != 0) {
            throw new SecurityException("Requires DISABLE_KEYGUARD permission");
        } else if (token == null) {
            throw new IllegalArgumentException("token == null");
        } else {
            this.mKeyguardDisableHandler.sendMessage(this.mKeyguardDisableHandler.obtainMessage(2, token));
        }
    }

    public void exitKeyguardSecurely(final IOnKeyguardExitResult callback) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.DISABLE_KEYGUARD") != 0) {
            throw new SecurityException("Requires DISABLE_KEYGUARD permission");
        } else if (callback == null) {
            throw new IllegalArgumentException("callback == null");
        } else {
            this.mPolicy.exitKeyguardSecurely(new OnKeyguardExitResult() {
                public void onKeyguardExitResult(boolean success) {
                    try {
                        callback.onKeyguardExitResult(success);
                    } catch (RemoteException e) {
                    }
                }
            });
        }
    }

    public boolean inKeyguardRestrictedInputMode() {
        return this.mPolicy.inKeyguardRestrictedKeyInputMode();
    }

    public boolean isKeyguardLocked() {
        return this.mPolicy.isKeyguardLocked();
    }

    public boolean isKeyguardSecure() {
        int userId = UserHandle.getCallingUserId();
        long origId = Binder.clearCallingIdentity();
        try {
            boolean isKeyguardSecure = this.mPolicy.isKeyguardSecure(userId);
            return isKeyguardSecure;
        } finally {
            Binder.restoreCallingIdentity(origId);
        }
    }

    public void dismissKeyguard() {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.DISABLE_KEYGUARD") != 0) {
            throw new SecurityException("Requires DISABLE_KEYGUARD permission");
        }
        synchronized (this.mWindowMap) {
            this.mPolicy.dismissKeyguardLw();
        }
    }

    public void keyguardGoingAway(int flags) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.DISABLE_KEYGUARD") != 0) {
            throw new SecurityException("Requires DISABLE_KEYGUARD permission");
        }
        if (WindowManagerDebugConfig.DEBUG_KEYGUARD) {
            Slog.d("WindowManager", "keyguardGoingAway: flags=0x" + Integer.toHexString(flags));
        }
        synchronized (this.mWindowMap) {
            this.mAnimator.mKeyguardGoingAway = true;
            this.mAnimator.mKeyguardGoingAwayFlags = flags;
            this.mWindowPlacerLocked.requestTraversal();
        }
    }

    public void keyguardWaitingForActivityDrawn() {
        if (WindowManagerDebugConfig.DEBUG_KEYGUARD) {
            Slog.d("WindowManager", "keyguardWaitingForActivityDrawn");
        }
        synchronized (this.mWindowMap) {
            this.mKeyguardWaitingForActivityDrawn = true;
        }
    }

    public void notifyActivityDrawnForKeyguard() {
        if (WindowManagerDebugConfig.DEBUG_KEYGUARD) {
            Slog.d("WindowManager", "notifyActivityDrawnForKeyguard: waiting=" + this.mKeyguardWaitingForActivityDrawn + " Callers=" + Debug.getCallers(5));
        }
        synchronized (this.mWindowMap) {
            if (this.mKeyguardWaitingForActivityDrawn) {
                this.mPolicy.notifyActivityDrawnForKeyguardLw();
                this.mKeyguardWaitingForActivityDrawn = false;
            }
        }
    }

    void showGlobalActions() {
        this.mPolicy.showGlobalActions();
    }

    public void closeSystemDialogs(String reason) {
        synchronized (this.mWindowMap) {
            int numDisplays = this.mDisplayContents.size();
            for (int displayNdx = 0; displayNdx < numDisplays; displayNdx++) {
                WindowList windows = ((DisplayContent) this.mDisplayContents.valueAt(displayNdx)).getWindowList();
                int numWindows = windows.size();
                for (int winNdx = 0; winNdx < numWindows; winNdx++) {
                    WindowState w = (WindowState) windows.get(winNdx);
                    if (w.mHasSurface) {
                        try {
                            w.mClient.closeSystemDialogs(reason);
                        } catch (RemoteException e) {
                        }
                    }
                }
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
            scale = fixScale(scale);
            switch (which) {
                case 0:
                    this.mWindowAnimationScaleSetting = scale;
                    break;
                case 1:
                    this.mTransitionAnimationScaleSetting = scale;
                    break;
                case 2:
                    this.mAnimatorDurationScaleSetting = scale;
                    break;
            }
            if (1 == System.getIntForUser(this.mContext.getContentResolver(), EYEPROTECT_ENABLE, 0, -2) && 1 == System.getInt(this.mContext.getContentResolver(), EYEPROTECT_INVERSE_ENABLE, 0)) {
                this.mWindowAnimationScaleSetting = OppoBrightUtils.MIN_LUX_LIMITI;
                this.mTransitionAnimationScaleSetting = OppoBrightUtils.MIN_LUX_LIMITI;
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
        switch (which) {
            case 0:
                return this.mWindowAnimationScaleSetting;
            case 1:
                return this.mTransitionAnimationScaleSetting;
            case 2:
                return this.mAnimatorDurationScaleSetting;
            default:
                return OppoBrightUtils.MIN_LUX_LIMITI;
        }
    }

    public float[] getAnimationScales() {
        float[] fArr = new float[3];
        fArr[0] = this.mWindowAnimationScaleSetting;
        fArr[1] = this.mTransitionAnimationScaleSetting;
        fArr[2] = this.mAnimatorDurationScaleSetting;
        return fArr;
    }

    public float getCurrentAnimatorScale() {
        float f;
        synchronized (this.mWindowMap) {
            f = this.mAnimationsDisabled ? OppoBrightUtils.MIN_LUX_LIMITI : this.mAnimatorDurationScaleSetting;
        }
        return f;
    }

    void dispatchNewAnimatorScaleLocked(Session session) {
        this.mH.obtainMessage(34, session).sendToTarget();
    }

    public void registerPointerEventListener(PointerEventListener listener) {
        this.mPointerEventDispatcher.registerInputEventListener(listener);
    }

    public void unregisterPointerEventListener(PointerEventListener listener) {
        this.mPointerEventDispatcher.unregisterInputEventListener(listener);
    }

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

    public void lockDeviceNow() {
        lockNow(null);
    }

    public int getCameraLensCoverState() {
        int sw = this.mInputManager.getSwitchState(-1, -256, 9);
        if (sw > 0) {
            return 1;
        }
        return sw == 0 ? 0 : -1;
    }

    public void switchInputMethod(boolean forwardDirection) {
        InputMethodManagerInternal inputMethodManagerInternal = (InputMethodManagerInternal) LocalServices.getService(InputMethodManagerInternal.class);
        if (inputMethodManagerInternal != null) {
            inputMethodManagerInternal.switchInputMethod(forwardDirection);
        }
    }

    public void shutdown(boolean confirm) {
        ShutdownThread.shutdown(this.mContext, "userrequested", confirm);
    }

    public void reboot(boolean confirm) {
        ShutdownThread.reboot(this.mContext, "userrequested", confirm);
    }

    public void rebootSafeMode(boolean confirm) {
        ShutdownThread.rebootSafeMode(this.mContext, confirm);
    }

    public void setCurrentProfileIds(int[] currentProfileIds) {
        synchronized (this.mWindowMap) {
            this.mCurrentProfileIds = currentProfileIds;
        }
    }

    public void setCurrentUser(int newUserId, int[] currentProfileIds) {
        synchronized (this.mWindowMap) {
            DisplayContent displayContent;
            this.mCurrentUserId = newUserId;
            this.mCurrentProfileIds = currentProfileIds;
            this.mAppTransition.setCurrentUser(newUserId);
            this.mPolicy.setCurrentUserLw(newUserId);
            this.mPolicy.enableKeyguard(true);
            int numDisplays = this.mDisplayContents.size();
            for (int displayNdx = 0; displayNdx < numDisplays; displayNdx++) {
                displayContent = (DisplayContent) this.mDisplayContents.valueAt(displayNdx);
                displayContent.switchUserStacks();
                rebuildAppWindowListLocked(displayContent);
            }
            this.mWindowPlacerLocked.performSurfacePlacement();
            displayContent = getDefaultDisplayContentLocked();
            displayContent.mDividerControllerLocked.notifyDockedStackExistsChanged(hasDockedTasksForUser(newUserId));
            if (this.mDisplayReady) {
                int targetDensity;
                int forcedDensity = getForcedDisplayDensityForUserLocked(newUserId);
                if (forcedDensity != 0) {
                    targetDensity = forcedDensity;
                } else {
                    targetDensity = displayContent.mInitialDisplayDensity;
                }
                setForcedDisplayDensityLocked(displayContent, targetDensity);
            }
        }
    }

    boolean hasDockedTasksForUser(int userId) {
        TaskStack stack = (TaskStack) this.mStackIdToStack.get(3);
        if (stack == null) {
            return false;
        }
        ArrayList<Task> tasks = stack.getTasks();
        boolean hasUserTask = false;
        for (int i = tasks.size() - 1; i >= 0 && !hasUserTask; i--) {
            hasUserTask = ((Task) tasks.get(i)).mUserId == userId;
        }
        return hasUserTask;
    }

    boolean isCurrentProfileLocked(int userId) {
        if (userId == this.mCurrentUserId || OppoMultiAppManager.getInstance().isCurrentProfile(userId)) {
            return true;
        }
        for (int i : this.mCurrentProfileIds) {
            if (i == userId) {
                return true;
            }
        }
        return false;
    }

    public void enableScreenAfterBoot() {
        synchronized (this.mWindowMap) {
            if (WindowManagerDebugConfig.DEBUG_BOOT) {
                RuntimeException here = new RuntimeException("here");
                here.fillInStackTrace();
                Slog.i("WindowManager", "enableScreenAfterBoot: mDisplayEnabled=" + this.mDisplayEnabled + " mForceDisplayEnabled=" + this.mForceDisplayEnabled + " mShowingBootMessages=" + this.mShowingBootMessages + " mSystemBooted=" + this.mSystemBooted, here);
            }
            if (this.mSystemBooted) {
                return;
            }
            this.mSystemBooted = true;
            hideBootMessagesLocked();
            this.mH.sendEmptyMessageDelayed(23, 30000);
            this.mPolicy.systemBooted();
            performEnableScreen();
        }
    }

    public void enableScreenIfNeeded() {
        synchronized (this.mWindowMap) {
            enableScreenIfNeededLocked();
        }
    }

    void enableScreenIfNeededLocked() {
        if (!this.mDisplayEnabled) {
            if (this.mSystemBooted || this.mShowingBootMessages) {
                this.mH.sendEmptyMessage(16);
            }
        }
    }

    public void performBootTimeout() {
        synchronized (this.mWindowMap) {
            if (this.mDisplayEnabled) {
                return;
            }
            Slog.w("WindowManager", "***** BOOT TIMEOUT: forcing display enabled");
            this.mForceDisplayEnabled = true;
            performEnableScreen();
        }
    }

    private boolean checkWaitingForWindowsLocked() {
        boolean haveBootMsg = false;
        boolean haveApp = false;
        boolean haveWallpaper = false;
        boolean wallpaperEnabled = this.mContext.getResources().getBoolean(17956942) ? !this.mOnlyCore : false;
        boolean haveKeyguard = true;
        WindowList windows = getDefaultWindowListLocked();
        int N = windows.size();
        for (int i = 0; i < N; i++) {
            WindowState w = (WindowState) windows.get(i);
            if (w.isVisibleLw() && !w.mObscured && !w.isDrawnLw()) {
                return true;
            }
            if (w.isDrawnLw()) {
                if (w.mAttrs.type == 2021) {
                    haveBootMsg = true;
                } else if (w.mAttrs.type == 2 || w.mAttrs.type == 4) {
                    haveApp = true;
                } else if (w.mAttrs.type == 2013) {
                    haveWallpaper = true;
                } else if (w.mAttrs.type == 2000) {
                    haveKeyguard = this.mPolicy.isKeyguardDrawnLw();
                }
            }
        }
        if (WindowManagerDebugConfig.DEBUG_SCREEN_ON || WindowManagerDebugConfig.DEBUG_BOOT) {
            Slog.i("WindowManager", "******** booted=" + this.mSystemBooted + " msg=" + this.mShowingBootMessages + " haveBoot=" + haveBootMsg + " haveApp=" + haveApp + " haveWall=" + haveWallpaper + " wallEnabled=" + wallpaperEnabled + " haveKeyguard=" + haveKeyguard);
        }
        if (!this.mSystemBooted && !haveBootMsg) {
            return true;
        }
        if (!this.mSystemBooted || ((haveApp || haveKeyguard) && (!wallpaperEnabled || haveWallpaper))) {
            return false;
        }
        return true;
    }

    /* JADX WARNING: Missing block: B:59:?, code:
            r10.mActivityManager.bootAnimationComplete();
     */
    /* JADX WARNING: Missing block: B:69:0x013e, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void performEnableScreen() {
        synchronized (this.mWindowMap) {
            if (WindowManagerDebugConfig.DEBUG_BOOT || !IS_USER_BUILD) {
                Slog.i("WindowManager", "performEnableScreen: mDisplayEnabled=" + this.mDisplayEnabled + " mForceDisplayEnabled=" + this.mForceDisplayEnabled + " mShowingBootMessages=" + this.mShowingBootMessages + " mSystemBooted=" + this.mSystemBooted + " mOnlyCore=" + this.mOnlyCore + " mIsAlarmBooting=" + this.mIsAlarmBooting + " mBootAnimationStopped=" + this.mBootAnimationStopped, new RuntimeException("here").fillInStackTrace());
            }
            if (this.mDisplayEnabled) {
                return;
            } else if (!this.mSystemBooted && !this.mShowingBootMessages) {
                return;
            } else if (!this.mForceDisplayEnabled && checkWaitingForWindowsLocked()) {
                return;
            } else if (this.mIsAlarmBooting) {
                return;
            } else {
                if (!this.mBootAnimationStopped) {
                    Trace.asyncTraceBegin(32, "Stop bootanim", 0);
                    try {
                        IBinder surfaceFlinger = ServiceManager.getService("SurfaceFlinger");
                        if (surfaceFlinger != null) {
                            Parcel data = Parcel.obtain();
                            data.writeInterfaceToken("android.ui.ISurfaceComposer");
                            surfaceFlinger.transact(1, data, null, 0);
                            data.recycle();
                            if (!IS_USER_BUILD) {
                                Slog.d(TAG, "Tell SurfaceFlinger finish boot animation");
                            }
                        }
                    } catch (RemoteException e) {
                        Slog.e("WindowManager", "Boot completed: SurfaceFlinger is dead!");
                    }
                    this.mBootAnimationStopped = true;
                }
                if (this.mForceDisplayEnabled || checkBootAnimationCompleteLocked()) {
                    EventLog.writeEvent(EventLogTags.WM_BOOT_ANIMATION_DONE, SystemClock.uptimeMillis());
                    Trace.asyncTraceEnd(32, "Stop bootanim", 0);
                    this.mDisplayEnabled = true;
                    if (WindowManagerDebugConfig.DEBUG_SCREEN_ON || WindowManagerDebugConfig.DEBUG_BOOT) {
                        Slog.i("WindowManager", "******************** ENABLING SCREEN!");
                    }
                    this.mInputMonitor.setEventDispatchingLw(true);
                } else if (WindowManagerDebugConfig.DEBUG_BOOT) {
                    Slog.i("WindowManager", "performEnableScreen: Waiting for anim complete");
                }
            }
        }
        this.mPolicy.enableScreenAfterBoot();
        updateRotationUnchecked(false, false);
    }

    private boolean checkBootAnimationCompleteLocked() {
        if (SystemService.isRunning(BOOT_ANIMATION_SERVICE)) {
            this.mH.removeMessages(37);
            this.mH.sendEmptyMessageDelayed(37, 200);
            if (WindowManagerDebugConfig.DEBUG_BOOT) {
                Slog.i("WindowManager", "checkBootAnimationComplete: Waiting for anim complete");
            }
            return false;
        }
        if (WindowManagerDebugConfig.DEBUG_BOOT) {
            Slog.i("WindowManager", "checkBootAnimationComplete: Animation complete!");
        }
        return true;
    }

    /* JADX WARNING: Missing block: B:26:0x007d, code:
            if (r0 == false) goto L_0x0082;
     */
    /* JADX WARNING: Missing block: B:27:0x007f, code:
            performEnableScreen();
     */
    /* JADX WARNING: Missing block: B:28:0x0082, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void showBootMessage(CharSequence msg, boolean always) {
        boolean first = false;
        synchronized (this.mWindowMap) {
            if (WindowManagerDebugConfig.DEBUG_BOOT) {
                RuntimeException here = new RuntimeException("here");
                here.fillInStackTrace();
                Slog.i("WindowManager", "showBootMessage: msg=" + msg + " always=" + always + " mAllowBootMessages=" + this.mAllowBootMessages + " mShowingBootMessages=" + this.mShowingBootMessages + " mSystemBooted=" + this.mSystemBooted, here);
            }
            if (this.mAllowBootMessages) {
                if (!this.mShowingBootMessages) {
                    if (always) {
                        first = true;
                    } else {
                        return;
                    }
                }
                if (this.mSystemBooted) {
                } else {
                    this.mShowingBootMessages = true;
                    this.mPolicy.showBootMessage(msg, always);
                }
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
        synchronized (this.mWindowMap) {
            this.mInTouchMode = mode;
        }
    }

    private void updateCircularDisplayMaskIfNeeded() {
        if (this.mContext.getResources().getConfiguration().isScreenRound() && this.mContext.getResources().getBoolean(17957000)) {
            int currentUserId;
            synchronized (this.mWindowMap) {
                currentUserId = this.mCurrentUserId;
            }
            int showMask = Secure.getIntForUser(this.mContext.getContentResolver(), "accessibility_display_inversion_enabled", 0, currentUserId) == 1 ? 0 : 1;
            Message m = this.mH.obtainMessage(35);
            m.arg1 = showMask;
            this.mH.sendMessage(m);
        }
    }

    public void showEmulatorDisplayOverlayIfNeeded() {
        if (this.mContext.getResources().getBoolean(17957001) && SystemProperties.getBoolean(PROPERTY_EMULATOR_CIRCULAR, false) && Build.IS_EMULATOR) {
            this.mH.sendMessage(this.mH.obtainMessage(36));
        }
    }

    public void showCircularMask(boolean visible) {
        synchronized (this.mWindowMap) {
            if (WindowManagerDebugConfig.SHOW_LIGHT_TRANSACTIONS) {
                Slog.i("WindowManager", ">>> OPEN TRANSACTION showCircularMask(visible=" + visible + ")");
            }
            SurfaceControl.openTransaction();
            if (visible) {
                try {
                    if (this.mCircularDisplayMask == null) {
                        this.mCircularDisplayMask = new CircularDisplayMask(getDefaultDisplayContentLocked().getDisplay(), this.mFxSession, (this.mPolicy.windowTypeToLayerLw(2018) * 10000) + 10, this.mContext.getResources().getInteger(17694868), this.mContext.getResources().getDimensionPixelSize(17105059));
                    }
                    this.mCircularDisplayMask.setVisibility(true);
                } catch (Throwable th) {
                    SurfaceControl.closeTransaction();
                    if (WindowManagerDebugConfig.SHOW_LIGHT_TRANSACTIONS) {
                        Slog.i("WindowManager", "<<< CLOSE TRANSACTION showCircularMask(visible=" + visible + ")");
                    }
                }
            } else if (this.mCircularDisplayMask != null) {
                this.mCircularDisplayMask.setVisibility(false);
                this.mCircularDisplayMask = null;
            }
            SurfaceControl.closeTransaction();
            if (WindowManagerDebugConfig.SHOW_LIGHT_TRANSACTIONS) {
                Slog.i("WindowManager", "<<< CLOSE TRANSACTION showCircularMask(visible=" + visible + ")");
            }
        }
    }

    public void showEmulatorDisplayOverlay() {
        synchronized (this.mWindowMap) {
            if (WindowManagerDebugConfig.SHOW_LIGHT_TRANSACTIONS) {
                Slog.i("WindowManager", ">>> OPEN TRANSACTION showEmulatorDisplayOverlay");
            }
            SurfaceControl.openTransaction();
            try {
                if (this.mEmulatorDisplayOverlay == null) {
                    this.mEmulatorDisplayOverlay = new EmulatorDisplayOverlay(this.mContext, getDefaultDisplayContentLocked().getDisplay(), this.mFxSession, (this.mPolicy.windowTypeToLayerLw(2018) * 10000) + 10);
                }
                this.mEmulatorDisplayOverlay.setVisibility(true);
                SurfaceControl.closeTransaction();
                if (WindowManagerDebugConfig.SHOW_LIGHT_TRANSACTIONS) {
                    Slog.i("WindowManager", "<<< CLOSE TRANSACTION showEmulatorDisplayOverlay");
                }
            } catch (Throwable th) {
                SurfaceControl.closeTransaction();
                if (WindowManagerDebugConfig.SHOW_LIGHT_TRANSACTIONS) {
                    Slog.i("WindowManager", "<<< CLOSE TRANSACTION showEmulatorDisplayOverlay");
                }
            }
        }
    }

    public void showStrictModeViolation(boolean on) {
        this.mH.sendMessage(this.mH.obtainMessage(25, on ? 1 : 0, Binder.getCallingPid()));
    }

    /* JADX WARNING: Missing block: B:37:0x007f, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void showStrictModeViolation(int arg, int pid) {
        boolean on = arg != 0;
        synchronized (this.mWindowMap) {
            if (on) {
                boolean isVisible = false;
                int numDisplays = this.mDisplayContents.size();
                for (int displayNdx = 0; displayNdx < numDisplays; displayNdx++) {
                    WindowList windows = ((DisplayContent) this.mDisplayContents.valueAt(displayNdx)).getWindowList();
                    int numWindows = windows.size();
                    for (int winNdx = 0; winNdx < numWindows; winNdx++) {
                        WindowState ws = (WindowState) windows.get(winNdx);
                        if (ws.mSession.mPid == pid && ws.isVisibleLw()) {
                            isVisible = true;
                            break;
                        }
                    }
                }
                if (!isVisible) {
                    return;
                }
            }
            if (WindowManagerDebugConfig.SHOW_VERBOSE_TRANSACTIONS) {
                Slog.i("WindowManager", ">>> OPEN TRANSACTION showStrictModeViolation");
            }
            SurfaceControl.openTransaction();
            try {
                if (this.mStrictModeFlash == null) {
                    this.mStrictModeFlash = new StrictModeFlash(getDefaultDisplayContentLocked().getDisplay(), this.mFxSession);
                }
                this.mStrictModeFlash.setVisibility(on);
                SurfaceControl.closeTransaction();
                if (WindowManagerDebugConfig.SHOW_VERBOSE_TRANSACTIONS) {
                    Slog.i("WindowManager", "<<< CLOSE TRANSACTION showStrictModeViolation");
                }
            } catch (Throwable th) {
                SurfaceControl.closeTransaction();
                if (WindowManagerDebugConfig.SHOW_VERBOSE_TRANSACTIONS) {
                    Slog.i("WindowManager", "<<< CLOSE TRANSACTION showStrictModeViolation");
                }
            }
        }
    }

    public void setStrictModeVisualIndicatorPreference(String value) {
        SystemProperties.set("persist.sys.strictmode.visual", value);
    }

    private static void convertCropForSurfaceFlinger(Rect crop, int rot, int dw, int dh) {
        int tmp;
        if (rot == 1) {
            tmp = crop.top;
            crop.top = dw - crop.right;
            crop.right = crop.bottom;
            crop.bottom = dw - crop.left;
            crop.left = tmp;
        } else if (rot == 2) {
            tmp = crop.top;
            crop.top = dh - crop.bottom;
            crop.bottom = dh - tmp;
            tmp = crop.right;
            crop.right = dw - crop.left;
            crop.left = dw - tmp;
        } else if (rot == 3) {
            tmp = crop.top;
            crop.top = crop.left;
            crop.left = dh - crop.bottom;
            crop.bottom = crop.right;
            crop.right = dh - tmp;
        }
    }

    public Bitmap screenshotWallpaper() {
        if (checkCallingPermission("android.permission.READ_FRAME_BUFFER", "screenshotWallpaper()")) {
            try {
                Trace.traceBegin(32, "screenshotWallpaper");
                Bitmap screenshotApplicationsInner = screenshotApplicationsInner(null, 0, -1, -1, true, 1.0f, Config.ARGB_8888, true);
                return screenshotApplicationsInner;
            } finally {
                Trace.traceEnd(32);
            }
        } else {
            throw new SecurityException("Requires READ_FRAME_BUFFER permission");
        }
    }

    public boolean requestAssistScreenshot(final IAssistScreenshotReceiver receiver) {
        if (checkCallingPermission("android.permission.READ_FRAME_BUFFER", "requestAssistScreenshot()")) {
            FgThread.getHandler().post(new Runnable() {
                public void run() {
                    try {
                        receiver.send(WindowManagerService.this.screenshotApplicationsInner(null, 0, -1, -1, true, 1.0f, Config.ARGB_8888, false));
                    } catch (RemoteException e) {
                    }
                }
            });
            return true;
        }
        throw new SecurityException("Requires READ_FRAME_BUFFER permission");
    }

    public Bitmap screenshotApplications(IBinder appToken, int displayId, int width, int height, float frameScale) {
        if (checkCallingPermission("android.permission.READ_FRAME_BUFFER", "screenshotApplications()")) {
            try {
                Trace.traceBegin(32, "screenshotApplications");
                Bitmap screenshotApplicationsInner = screenshotApplicationsInner(appToken, displayId, width, height, false, frameScale, Config.RGB_565, false);
                return screenshotApplicationsInner;
            } finally {
                Trace.traceEnd(32);
            }
        } else {
            throw new SecurityException("Requires READ_FRAME_BUFFER permission");
        }
    }

    /* JADX WARNING: Missing block: B:12:0x0044, code:
            r35 = r34.getDisplayInfo();
            r14 = r35.logicalWidth;
            r15 = r35.logicalHeight;
     */
    /* JADX WARNING: Missing block: B:13:0x0050, code:
            if (r14 == 0) goto L_0x0054;
     */
    /* JADX WARNING: Missing block: B:14:0x0052, code:
            if (r15 != 0) goto L_0x008f;
     */
    /* JADX WARNING: Missing block: B:16:0x0056, code:
            if (com.android.server.wm.WindowManagerDebugConfig.DEBUG_SCREENSHOT == false) goto L_0x008a;
     */
    /* JADX WARNING: Missing block: B:17:0x0058, code:
            android.util.Slog.i("WindowManager", "Screenshot of " + r56 + ": returning null. logical widthxheight=" + r14 + "x" + r15);
     */
    /* JADX WARNING: Missing block: B:19:0x008b, code:
            return null;
     */
    /* JADX WARNING: Missing block: B:23:0x008f, code:
            r10 = 0;
            r38 = new android.graphics.Rect();
            r48 = new android.graphics.Rect();
     */
    /* JADX WARNING: Missing block: B:24:0x009c, code:
            if (r56 != null) goto L_0x00a0;
     */
    /* JADX WARNING: Missing block: B:25:0x009e, code:
            if (r63 == false) goto L_0x0114;
     */
    /* JADX WARNING: Missing block: B:26:0x00a0, code:
            r47 = false;
            r9 = Integer.MAX_VALUE;
     */
    /* JADX WARNING: Missing block: B:27:0x00a5, code:
            r8 = r55.mWindowMap;
     */
    /* JADX WARNING: Missing block: B:28:0x00ab, code:
            monitor-enter(r8);
     */
    /* JADX WARNING: Missing block: B:31:0x00b0, code:
            if (r55.mInputMethodTarget == null) goto L_0x0118;
     */
    /* JADX WARNING: Missing block: B:32:0x00b2, code:
            r40 = r55.mInputMethodTarget.mAppToken;
     */
    /* JADX WARNING: Missing block: B:33:0x00ba, code:
            if (r40 == null) goto L_0x011e;
     */
    /* JADX WARNING: Missing block: B:35:0x00c0, code:
            if (r40.appToken == null) goto L_0x011e;
     */
    /* JADX WARNING: Missing block: B:37:0x00cc, code:
            if (r40.appToken.asBinder() != r56) goto L_0x011e;
     */
    /* JADX WARNING: Missing block: B:39:0x00d6, code:
            if (r55.mInputMethodTarget.isInMultiWindowMode() == false) goto L_0x011b;
     */
    /* JADX WARNING: Missing block: B:40:0x00d8, code:
            r41 = false;
     */
    /* JADX WARNING: Missing block: B:41:0x00da, code:
            monitor-exit(r8);
     */
    /* JADX WARNING: Missing block: B:42:0x00db, code:
            r24 = ((r55.mPolicy.windowTypeToLayerLw(2) + 1) * 10000) + 1000;
            r20 = r55.mWindowMap;
     */
    /* JADX WARNING: Missing block: B:43:0x00f2, code:
            monitor-enter(r20);
     */
    /* JADX WARNING: Missing block: B:44:0x00f3, code:
            r26 = null;
     */
    /* JADX WARNING: Missing block: B:46:?, code:
            r53 = r34.getWindowList();
            r39 = r53.size() - 1;
     */
    /* JADX WARNING: Missing block: B:47:0x00ff, code:
            if (r39 < 0) goto L_0x018a;
     */
    /* JADX WARNING: Missing block: B:48:0x0101, code:
            r54 = (com.android.server.wm.WindowState) r53.get(r39);
     */
    /* JADX WARNING: Missing block: B:49:0x010f, code:
            if (r54.mHasSurface != false) goto L_0x0124;
     */
    /* JADX WARNING: Missing block: B:50:0x0111, code:
            r39 = r39 - 1;
     */
    /* JADX WARNING: Missing block: B:51:0x0114, code:
            r47 = true;
            r9 = 0;
     */
    /* JADX WARNING: Missing block: B:52:0x0118, code:
            r40 = null;
     */
    /* JADX WARNING: Missing block: B:53:0x011b, code:
            r41 = true;
     */
    /* JADX WARNING: Missing block: B:54:0x011e, code:
            r41 = false;
     */
    /* JADX WARNING: Missing block: B:60:0x012a, code:
            if (r54.mLayer >= r24) goto L_0x0111;
     */
    /* JADX WARNING: Missing block: B:61:0x012c, code:
            if (r63 == false) goto L_0x0134;
     */
    /* JADX WARNING: Missing block: B:63:0x0132, code:
            if (r54.mIsWallpaper == false) goto L_0x0111;
     */
    /* JADX WARNING: Missing block: B:65:0x0138, code:
            if (r54.mIsImWindow == false) goto L_0x01b1;
     */
    /* JADX WARNING: Missing block: B:66:0x013a, code:
            if (r41 == false) goto L_0x0111;
     */
    /* JADX WARNING: Missing block: B:67:0x013c, code:
            r52 = r54.mWinAnimator;
            r42 = r52.mSurfaceController.getLayer();
     */
    /* JADX WARNING: Missing block: B:68:0x014c, code:
            if (r10 >= r42) goto L_0x0150;
     */
    /* JADX WARNING: Missing block: B:69:0x014e, code:
            r10 = r42;
     */
    /* JADX WARNING: Missing block: B:71:0x0152, code:
            if (r9 <= r42) goto L_0x0156;
     */
    /* JADX WARNING: Missing block: B:72:0x0154, code:
            r9 = r42;
     */
    /* JADX WARNING: Missing block: B:73:0x0156, code:
            if (r60 != false) goto L_0x015e;
     */
    /* JADX WARNING: Missing block: B:75:0x015c, code:
            if (r54.mIsWallpaper == false) goto L_0x01d5;
     */
    /* JADX WARNING: Missing block: B:77:0x0162, code:
            if (r54.mAppToken == null) goto L_0x022f;
     */
    /* JADX WARNING: Missing block: B:79:0x016c, code:
            if (r54.mAppToken.token != r56) goto L_0x022f;
     */
    /* JADX WARNING: Missing block: B:80:0x016e, code:
            r37 = true;
     */
    /* JADX WARNING: Missing block: B:81:0x0170, code:
            if (r37 == false) goto L_0x0180;
     */
    /* JADX WARNING: Missing block: B:83:0x0176, code:
            if (r54.isDisplayedLw() == false) goto L_0x0180;
     */
    /* JADX WARNING: Missing block: B:85:0x017c, code:
            if (r52.getShown() == false) goto L_0x0180;
     */
    /* JADX WARNING: Missing block: B:86:0x017e, code:
            r47 = true;
     */
    /* JADX WARNING: Missing block: B:88:0x0188, code:
            if (r54.isObscuringFullscreen(r35) == false) goto L_0x0111;
     */
    /* JADX WARNING: Missing block: B:89:0x018a, code:
            if (r56 == null) goto L_0x0239;
     */
    /* JADX WARNING: Missing block: B:90:0x018c, code:
            if (r26 != null) goto L_0x0239;
     */
    /* JADX WARNING: Missing block: B:92:0x0190, code:
            if (com.android.server.wm.WindowManagerDebugConfig.DEBUG_SCREENSHOT == false) goto L_0x01ae;
     */
    /* JADX WARNING: Missing block: B:93:0x0192, code:
            android.util.Slog.i("WindowManager", "Screenshot: Couldn't find a surface matching " + r56);
     */
    /* JADX WARNING: Missing block: B:95:0x01af, code:
            monitor-exit(r20);
     */
    /* JADX WARNING: Missing block: B:96:0x01b0, code:
            return null;
     */
    /* JADX WARNING: Missing block: B:99:0x01b5, code:
            if (r54.mIsWallpaper == false) goto L_0x01bf;
     */
    /* JADX WARNING: Missing block: B:100:0x01b7, code:
            if (r63 == false) goto L_0x01bb;
     */
    /* JADX WARNING: Missing block: B:101:0x01b9, code:
            r26 = r54;
     */
    /* JADX WARNING: Missing block: B:102:0x01bb, code:
            if (r26 != null) goto L_0x013c;
     */
    /* JADX WARNING: Missing block: B:103:0x01bf, code:
            if (r56 == null) goto L_0x013c;
     */
    /* JADX WARNING: Missing block: B:105:0x01c5, code:
            if (r54.mAppToken == null) goto L_0x0111;
     */
    /* JADX WARNING: Missing block: B:107:0x01cf, code:
            if (r54.mAppToken.token != r56) goto L_0x0111;
     */
    /* JADX WARNING: Missing block: B:108:0x01d1, code:
            r26 = r54;
     */
    /* JADX WARNING: Missing block: B:109:0x01d5, code:
            r50 = r54.mFrame;
            r31 = r54.mContentInsets;
            r38.union(r50.left + r31.left, r50.top + r31.top, r50.right - r31.right, r50.bottom - r31.bottom);
            r54.getVisibleBounds(r48);
     */
    /* JADX WARNING: Missing block: B:110:0x0225, code:
            if (android.graphics.Rect.intersects(r38, r48) != false) goto L_0x015e;
     */
    /* JADX WARNING: Missing block: B:111:0x0227, code:
            r38.setEmpty();
     */
    /* JADX WARNING: Missing block: B:115:0x022f, code:
            if (r26 == null) goto L_0x0235;
     */
    /* JADX WARNING: Missing block: B:116:0x0231, code:
            r37 = r63;
     */
    /* JADX WARNING: Missing block: B:117:0x0235, code:
            r37 = false;
     */
    /* JADX WARNING: Missing block: B:118:0x0239, code:
            if (r47 != false) goto L_0x0295;
     */
    /* JADX WARNING: Missing block: B:120:?, code:
            r8 = "WindowManager";
            r13 = new java.lang.StringBuilder().append("Failed to capture screenshot of ").append(r56).append(" appWin=");
     */
    /* JADX WARNING: Missing block: B:121:0x0257, code:
            if (r26 != null) goto L_0x026a;
     */
    /* JADX WARNING: Missing block: B:122:0x0259, code:
            r7 = "null";
     */
    /* JADX WARNING: Missing block: B:123:0x025c, code:
            android.util.Slog.i(r8, r13.append(r7).toString());
     */
    /* JADX WARNING: Missing block: B:125:0x0268, code:
            monitor-exit(r20);
     */
    /* JADX WARNING: Missing block: B:126:0x0269, code:
            return null;
     */
    /* JADX WARNING: Missing block: B:128:?, code:
            r7 = r26 + " drawState=" + r26.mWinAnimator.mDrawState;
     */
    /* JADX WARNING: Missing block: B:129:0x0295, code:
            if (r10 != 0) goto L_0x02c5;
     */
    /* JADX WARNING: Missing block: B:131:0x0299, code:
            if (com.android.server.wm.WindowManagerDebugConfig.DEBUG_SCREENSHOT == false) goto L_0x02c2;
     */
    /* JADX WARNING: Missing block: B:132:0x029b, code:
            android.util.Slog.i("WindowManager", "Screenshot of " + r56 + ": returning null maxLayer=" + r10);
     */
    /* JADX WARNING: Missing block: B:134:0x02c3, code:
            monitor-exit(r20);
     */
    /* JADX WARNING: Missing block: B:135:0x02c4, code:
            return null;
     */
    /* JADX WARNING: Missing block: B:136:0x02c5, code:
            if (r60 != false) goto L_0x02dd;
     */
    /* JADX WARNING: Missing block: B:140:0x02cf, code:
            if (r38.intersect(0, 0, r14, r15) != false) goto L_0x02d4;
     */
    /* JADX WARNING: Missing block: B:141:0x02d1, code:
            r38.setEmpty();
     */
    /* JADX WARNING: Missing block: B:143:0x02d8, code:
            if (r38.isEmpty() == false) goto L_0x02e5;
     */
    /* JADX WARNING: Missing block: B:145:0x02db, code:
            monitor-exit(r20);
     */
    /* JADX WARNING: Missing block: B:146:0x02dc, code:
            return null;
     */
    /* JADX WARNING: Missing block: B:149:?, code:
            r38.set(0, 0, r14, r15);
     */
    /* JADX WARNING: Missing block: B:150:0x02e5, code:
            if (r58 >= 0) goto L_0x02f1;
     */
    /* JADX WARNING: Missing block: B:151:0x02e7, code:
            r58 = (int) (((float) r38.width()) * r61);
     */
    /* JADX WARNING: Missing block: B:152:0x02f1, code:
            if (r59 >= 0) goto L_0x02fd;
     */
    /* JADX WARNING: Missing block: B:153:0x02f3, code:
            r59 = (int) (((float) r38.height()) * r61);
     */
    /* JADX WARNING: Missing block: B:154:0x02fd, code:
            r6 = new android.graphics.Rect(r38);
     */
    /* JADX WARNING: Missing block: B:155:0x0318, code:
            if ((((float) r58) / ((float) r38.width())) >= (((float) r59) / ((float) r38.height()))) goto L_0x03f9;
     */
    /* JADX WARNING: Missing block: B:156:0x031a, code:
            r6.right = r6.left + ((int) ((((float) r58) / ((float) r59)) * ((float) r38.height())));
     */
    /* JADX WARNING: Missing block: B:157:0x0330, code:
            r12 = getDefaultDisplayContentLocked().getDisplay().getRotation();
     */
    /* JADX WARNING: Missing block: B:158:0x033d, code:
            if (r12 == 1) goto L_0x0342;
     */
    /* JADX WARNING: Missing block: B:160:0x0340, code:
            if (r12 != 3) goto L_0x0346;
     */
    /* JADX WARNING: Missing block: B:162:0x0343, code:
            if (r12 != 1) goto L_0x0411;
     */
    /* JADX WARNING: Missing block: B:163:0x0345, code:
            r12 = 3;
     */
    /* JADX WARNING: Missing block: B:164:0x0346, code:
            convertCropForSurfaceFlinger(r6, r12, r14, r15);
     */
    /* JADX WARNING: Missing block: B:165:0x034b, code:
            if (com.android.server.wm.WindowManagerDebugConfig.DEBUG_SCREENSHOT == false) goto L_0x041d;
     */
    /* JADX WARNING: Missing block: B:166:0x034d, code:
            android.util.Slog.i("WindowManager", "Screenshot: " + r14 + "x" + r15 + " from " + r9 + " to " + r10 + " appToken=" + r56);
            r39 = 0;
     */
    /* JADX WARNING: Missing block: B:168:0x039d, code:
            if (r39 >= r53.size()) goto L_0x041d;
     */
    /* JADX WARNING: Missing block: B:169:0x039f, code:
            r51 = (com.android.server.wm.WindowState) r53.get(r39);
            r30 = r51.mWinAnimator.mSurfaceController;
            r8 = "WindowManager";
            r13 = new java.lang.StringBuilder().append(r51).append(": ").append(r51.mLayer).append(" animLayer=").append(r51.mWinAnimator.mAnimLayer).append(" surfaceLayer=");
     */
    /* JADX WARNING: Missing block: B:170:0x03e6, code:
            if (r30 != null) goto L_0x0414;
     */
    /* JADX WARNING: Missing block: B:171:0x03e8, code:
            r7 = "null";
     */
    /* JADX WARNING: Missing block: B:172:0x03eb, code:
            android.util.Slog.i(r8, r13.append(r7).toString());
            r39 = r39 + 1;
     */
    /* JADX WARNING: Missing block: B:173:0x03f9, code:
            r6.bottom = r6.top + ((int) ((((float) r59) / ((float) r58)) * ((float) r38.width())));
     */
    /* JADX WARNING: Missing block: B:174:0x0411, code:
            r12 = 1;
     */
    /* JADX WARNING: Missing block: B:175:0x0414, code:
            r7 = java.lang.Integer.valueOf(r30.getLayer());
     */
    /* JADX WARNING: Missing block: B:176:0x041d, code:
            r46 = r55.mAnimator.getScreenRotationAnimationLocked(0);
     */
    /* JADX WARNING: Missing block: B:177:0x0426, code:
            if (r46 == null) goto L_0x048d;
     */
    /* JADX WARNING: Missing block: B:178:0x0428, code:
            r11 = r46.isAnimating();
     */
    /* JADX WARNING: Missing block: B:180:0x042e, code:
            if (com.android.server.wm.WindowManagerDebugConfig.DEBUG_SCREENSHOT == false) goto L_0x043b;
     */
    /* JADX WARNING: Missing block: B:181:0x0430, code:
            if (r11 == false) goto L_0x043b;
     */
    /* JADX WARNING: Missing block: B:182:0x0432, code:
            android.util.Slog.v("WindowManager", "Taking screenshot while rotating");
     */
    /* JADX WARNING: Missing block: B:183:0x043b, code:
            android.view.SurfaceControl.openTransaction();
            android.view.SurfaceControl.closeTransactionSync();
            android.os.Trace.traceBegin(32, "wmScreenshot");
            r27 = android.view.SurfaceControl.screenshot(r6, r58, r59, r9, r10, r11, r12);
            android.os.Trace.traceEnd(32);
     */
    /* JADX WARNING: Missing block: B:184:0x0458, code:
            if (r27 != null) goto L_0x048f;
     */
    /* JADX WARNING: Missing block: B:185:0x045a, code:
            android.util.Slog.w("WindowManager", "Screenshot failure taking screenshot for (" + r14 + "x" + r15 + ") to layer " + r10);
     */
    /* JADX WARNING: Missing block: B:187:0x048b, code:
            monitor-exit(r20);
     */
    /* JADX WARNING: Missing block: B:188:0x048c, code:
            return null;
     */
    /* JADX WARNING: Missing block: B:189:0x048d, code:
            r11 = false;
     */
    /* JADX WARNING: Missing block: B:192:0x0493, code:
            if (isFastStartingWindowSupport() == false) goto L_0x04c9;
     */
    /* JADX WARNING: Missing block: B:194:0x0499, code:
            if (isCacheLastFrame() == false) goto L_0x04c9;
     */
    /* JADX WARNING: Missing block: B:195:0x049b, code:
            r28 = android.view.SurfaceControl.screenshot(new android.graphics.Rect(), r14, r15, r9, r10, false, 0);
            setBitmapByToken(r26.mWinAnimator.mWin.mToken.token, r28.copy(r28.getConfig(), true));
            r28.recycle();
     */
    /* JADX WARNING: Missing block: B:196:0x04c9, code:
            monitor-exit(r20);
     */
    /* JADX WARNING: Missing block: B:198:0x04cc, code:
            if (com.android.server.wm.WindowManagerDebugConfig.DEBUG_SCREENSHOT == false) goto L_0x0566;
     */
    /* JADX WARNING: Missing block: B:199:0x04ce, code:
            r17 = new int[(r27.getWidth() * r27.getHeight())];
            r27.getPixels(r17, 0, r27.getWidth(), 0, 0, r27.getWidth(), r27.getHeight());
            r25 = true;
            r36 = r17[0];
            r39 = 0;
     */
    /* JADX WARNING: Missing block: B:201:0x04fe, code:
            if (r39 >= r17.length) goto L_0x0508;
     */
    /* JADX WARNING: Missing block: B:203:0x0504, code:
            if (r17[r39] == r36) goto L_0x0572;
     */
    /* JADX WARNING: Missing block: B:204:0x0506, code:
            r25 = false;
     */
    /* JADX WARNING: Missing block: B:205:0x0508, code:
            if (r25 == false) goto L_0x0566;
     */
    /* JADX WARNING: Missing block: B:206:0x050a, code:
            r8 = "WindowManager";
            r13 = new java.lang.StringBuilder().append("Screenshot ").append(r26).append(" was monochrome(").append(java.lang.Integer.toHexString(r36)).append(")! mSurfaceLayer=");
     */
    /* JADX WARNING: Missing block: B:207:0x0535, code:
            if (r26 == null) goto L_0x0575;
     */
    /* JADX WARNING: Missing block: B:208:0x0537, code:
            r7 = java.lang.Integer.valueOf(r26.mWinAnimator.mSurfaceController.getLayer());
     */
    /* JADX WARNING: Missing block: B:209:0x0545, code:
            android.util.Slog.i(r8, r13.append(r7).append(" minLayer=").append(r9).append(" maxLayer=").append(r10).toString());
     */
    /* JADX WARNING: Missing block: B:210:0x0566, code:
            r44 = r27.createAshmemBitmap(r62);
            r27.recycle();
     */
    /* JADX WARNING: Missing block: B:211:0x0571, code:
            return r44;
     */
    /* JADX WARNING: Missing block: B:212:0x0572, code:
            r39 = r39 + 1;
     */
    /* JADX WARNING: Missing block: B:213:0x0575, code:
            r7 = "null";
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    Bitmap screenshotApplicationsInner(IBinder appToken, int displayId, int width, int height, boolean includeFullDisplay, float frameScale, Config config, boolean wallpaperOnly) {
        synchronized (this.mWindowMap) {
            DisplayContent displayContent = getDisplayContentLocked(displayId);
            if (displayContent == null) {
                if (WindowManagerDebugConfig.DEBUG_SCREENSHOT) {
                    Slog.i("WindowManager", "Screenshot of " + appToken + ": returning null. No Display for displayId=" + displayId);
                }
                return null;
            }
        }
    }

    public void freezeRotation(int rotation) {
        if (!checkCallingPermission("android.permission.SET_ORIENTATION", "freezeRotation()")) {
            throw new SecurityException("Requires SET_ORIENTATION permission");
        } else if (rotation < -1 || rotation > 3) {
            throw new IllegalArgumentException("Rotation argument must be -1 or a valid rotation constant.");
        } else {
            if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                Slog.v("WindowManager", "freezeRotation: mRotation=" + this.mRotation);
            }
            long origId = Binder.clearCallingIdentity();
            try {
                WindowManagerPolicy windowManagerPolicy = this.mPolicy;
                if (rotation == -1) {
                    rotation = this.mRotation;
                }
                windowManagerPolicy.setUserRotationMode(1, rotation);
                updateRotationUnchecked(false, false);
            } finally {
                Binder.restoreCallingIdentity(origId);
            }
        }
    }

    public void thawRotation() {
        if (checkCallingPermission("android.permission.SET_ORIENTATION", "thawRotation()")) {
            if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                Slog.v("WindowManager", "thawRotation: mRotation=" + this.mRotation);
            }
            long origId = Binder.clearCallingIdentity();
            try {
                this.mPolicy.setUserRotationMode(0, 777);
                updateRotationUnchecked(false, false);
            } finally {
                Binder.restoreCallingIdentity(origId);
            }
        } else {
            throw new SecurityException("Requires SET_ORIENTATION permission");
        }
    }

    public void updateRotation(boolean alwaysSendConfiguration, boolean forceRelayout) {
        updateRotationUnchecked(alwaysSendConfiguration, forceRelayout);
    }

    void pauseRotationLocked() {
        this.mDeferredRotationPauseCount++;
    }

    void resumeRotationLocked() {
        if (this.mDeferredRotationPauseCount > 0) {
            this.mDeferredRotationPauseCount--;
            if (this.mDeferredRotationPauseCount == 0 && updateRotationUncheckedLocked(false)) {
                this.mH.sendEmptyMessage(18);
            }
        }
    }

    public void updateRotationUnchecked(boolean alwaysSendConfiguration, boolean forceRelayout) {
        boolean changed;
        if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
            Slog.v("WindowManager", "updateRotationUnchecked(alwaysSendConfiguration=" + alwaysSendConfiguration + ")");
        }
        if (this.mHyp == null) {
            this.mHyp = new Hypnus();
        }
        if (this.mHyp != null) {
            this.mHyp.hypnusSetAction(12, 1000);
        }
        long origId = Binder.clearCallingIdentity();
        synchronized (this.mWindowMap) {
            changed = updateRotationUncheckedLocked(false);
            if (!changed || forceRelayout) {
                getDefaultDisplayContentLocked().layoutNeeded = true;
                this.mWindowPlacerLocked.performSurfacePlacement();
            }
        }
        if (changed || alwaysSendConfiguration) {
            sendNewConfiguration();
        }
        Binder.restoreCallingIdentity(origId);
    }

    public boolean updateRotationUncheckedLocked(boolean inTransaction) {
        if (this.mDeferredRotationPauseCount > 0) {
            if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                Slog.v("WindowManager", "Deferring rotation, rotation is paused.");
            }
            return false;
        }
        ScreenRotationAnimation screenRotationAnimation = this.mAnimator.getScreenRotationAnimationLocked(0);
        if (screenRotationAnimation != null && screenRotationAnimation.isAnimating()) {
            if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                Slog.v("WindowManager", "Deferring rotation, animation in progress.");
            }
            return false;
        } else if (this.mDisplayFrozen) {
            if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                Slog.v("WindowManager", "Deferring rotation, still finishing previous rotation");
            }
            return false;
        } else if (this.mDisplayEnabled || this.mIsUpdateIpoRotation) {
            int rotation;
            int i;
            WindowState w;
            DisplayContent displayContent = getDefaultDisplayContentLocked();
            WindowList windows = displayContent.getWindowList();
            int oldRotation = this.mRotation;
            if (this.mIsUpdateIpoRotation || this.mIsUpdateAlarmBootRotation) {
                rotation = 0;
            } else {
                rotation = this.mPolicy.rotationForOrientationLw(this.mLastOrientation, this.mRotation);
            }
            boolean rotateSeamlessly = this.mPolicy.shouldRotateSeamlessly(oldRotation, rotation);
            if (rotateSeamlessly) {
                for (i = windows.size() - 1; i >= 0; i--) {
                    w = (WindowState) windows.get(i);
                    if (w.mSeamlesslyRotated) {
                        return false;
                    }
                    if (!((w.isChildWindow() & w.isVisibleNow()) == 0 || w.mWinAnimator.mSurfaceController.getTransformToDisplayInverse())) {
                        rotateSeamlessly = false;
                    }
                }
            }
            boolean altOrientation = !this.mPolicy.rotationHasCompatibleMetricsLw(this.mLastOrientation, rotation);
            if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                Slog.v("WindowManager", "Selected orientation " + this.mLastOrientation + ", got rotation " + rotation + " which has " + (altOrientation ? "incompatible" : "compatible") + " metrics");
            }
            if (this.mRotation == rotation && this.mAltOrientation == altOrientation) {
                return false;
            }
            if (this.mRotation != rotation) {
                this.mRotationChanged = true;
            } else {
                this.mRotationChanged = false;
            }
            Slog.v("WindowManager", "Rotation changed to " + rotation + (altOrientation ? " (alt)" : IElsaManager.EMPTY_PACKAGE) + " from " + this.mRotation + (this.mAltOrientation ? " (alt)" : IElsaManager.EMPTY_PACKAGE) + ", lastOrientation=" + this.mLastOrientation);
            this.mRotation = rotation;
            this.mAltOrientation = altOrientation;
            this.mPolicy.setRotationLw(this.mRotation);
            this.mWindowsFreezingScreen = 1;
            int override_window_freeze_timeout = -1;
            if (!(this.mFocusedApp == null || this.mFocusedApp.toString() == null || !this.mFocusedApp.toString().contains("com.jb.gosms"))) {
                override_window_freeze_timeout = 6000;
            }
            this.mH.removeMessages(11);
            H h = this.mH;
            if (override_window_freeze_timeout <= 0) {
                override_window_freeze_timeout = 2000;
            }
            h.sendEmptyMessageDelayed(11, (long) override_window_freeze_timeout);
            this.mWaitingForConfig = true;
            displayContent.layoutNeeded = true;
            int[] anim = new int[2];
            if (displayContent.isDimming()) {
                anim[1] = 0;
                anim[0] = 0;
            } else {
                this.mPolicy.selectRotationAnimationLw(anim);
            }
            if (rotateSeamlessly) {
                screenRotationAnimation = null;
                this.mSeamlessRotationCount = 0;
            } else {
                startFreezingDisplayLocked(inTransaction, anim[0], anim[1]);
                screenRotationAnimation = this.mAnimator.getScreenRotationAnimationLocked(0);
            }
            updateDisplayAndOrientationLocked(this.mCurConfiguration.uiMode);
            DisplayInfo displayInfo = displayContent.getDisplayInfo();
            if (!inTransaction) {
                if (WindowManagerDebugConfig.SHOW_TRANSACTIONS) {
                    Slog.i("WindowManager", ">>> OPEN TRANSACTION setRotationUnchecked");
                }
                SurfaceControl.openTransaction();
            }
            if (screenRotationAnimation != null) {
                try {
                    if (screenRotationAnimation.hasScreenshot() && screenRotationAnimation.setRotationInTransaction(rotation, this.mFxSession, 10000, getTransitionAnimationScaleLocked(), displayInfo.logicalWidth, displayInfo.logicalHeight)) {
                        scheduleAnimationLocked();
                    }
                } catch (Throwable th) {
                    if (!inTransaction) {
                        SurfaceControl.closeTransaction();
                        if (WindowManagerDebugConfig.SHOW_LIGHT_TRANSACTIONS) {
                            Slog.i("WindowManager", "<<< CLOSE TRANSACTION setRotationUnchecked");
                        }
                    }
                }
            }
            if (rotateSeamlessly) {
                for (i = windows.size() - 1; i >= 0; i--) {
                    ((WindowState) windows.get(i)).mWinAnimator.seamlesslyRotateWindow(oldRotation, this.mRotation);
                }
            }
            this.mDisplayManagerInternal.performTraversalInTransactionFromWindowManager();
            if (!inTransaction) {
                SurfaceControl.closeTransaction();
                if (WindowManagerDebugConfig.SHOW_LIGHT_TRANSACTIONS) {
                    Slog.i("WindowManager", "<<< CLOSE TRANSACTION setRotationUnchecked");
                }
            }
            for (i = windows.size() - 1; i >= 0; i--) {
                w = (WindowState) windows.get(i);
                if (w.mAppToken != null) {
                    w.mAppToken.destroySavedSurfaces();
                }
                if (w.mHasSurface && !rotateSeamlessly) {
                    if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                        Slog.v("WindowManager", "Set mOrientationChanging of " + w);
                    }
                    w.mOrientationChanging = true;
                    this.mWindowPlacerLocked.mOrientationChangeComplete = false;
                    w.mLastFreezeDuration = 0;
                }
            }
            if (rotateSeamlessly) {
                this.mH.removeMessages(54);
                this.mH.sendEmptyMessageDelayed(54, 2000);
            }
            for (i = this.mRotationWatchers.size() - 1; i >= 0; i--) {
                try {
                    ((RotationWatcher) this.mRotationWatchers.get(i)).watcher.onRotationChanged(rotation);
                } catch (RemoteException e) {
                }
            }
            if (screenRotationAnimation == null && this.mAccessibilityController != null && displayContent.getDisplayId() == 0) {
                this.mAccessibilityController.onRotationChangedLocked(getDefaultDisplayContentLocked(), rotation);
            }
            if (this.mIsUpdateAlarmBootRotation) {
                if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                    Slog.v(TAG, "Update power-off alarm Boot rotation is done");
                }
                this.mIsUpdateAlarmBootRotation = false;
            }
            return true;
        } else {
            if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                Slog.v("WindowManager", "Deferring rotation, display is not enabled.");
            }
            return false;
        }
    }

    public int getRotation() {
        return this.mRotation;
    }

    public boolean isRotationFrozen() {
        return this.mPolicy.getUserRotationMode() == 1;
    }

    public int watchRotation(IRotationWatcher watcher) {
        int i;
        final IBinder watcherBinder = watcher.asBinder();
        DeathRecipient dr = new DeathRecipient() {
            public void binderDied() {
                synchronized (WindowManagerService.this.mWindowMap) {
                    int i = 0;
                    while (i < WindowManagerService.this.mRotationWatchers.size()) {
                        if (watcherBinder == ((RotationWatcher) WindowManagerService.this.mRotationWatchers.get(i)).watcher.asBinder()) {
                            IBinder binder = ((RotationWatcher) WindowManagerService.this.mRotationWatchers.remove(i)).watcher.asBinder();
                            if (binder != null) {
                                binder.unlinkToDeath(this, 0);
                            }
                            i--;
                        }
                        i++;
                    }
                }
            }
        };
        synchronized (this.mWindowMap) {
            try {
                watcher.asBinder().linkToDeath(dr, 0);
                this.mRotationWatchers.add(new RotationWatcher(watcher, dr));
            } catch (RemoteException e) {
            }
            i = this.mRotation;
        }
        return i;
    }

    public void removeRotationWatcher(IRotationWatcher watcher) {
        IBinder watcherBinder = watcher.asBinder();
        synchronized (this.mWindowMap) {
            int i = 0;
            while (i < this.mRotationWatchers.size()) {
                if (watcherBinder == ((RotationWatcher) this.mRotationWatchers.get(i)).watcher.asBinder()) {
                    RotationWatcher removed = (RotationWatcher) this.mRotationWatchers.remove(i);
                    IBinder binder = removed.watcher.asBinder();
                    if (binder != null) {
                        binder.unlinkToDeath(removed.deathRecipient, 0);
                    }
                    i--;
                }
                i++;
            }
        }
    }

    public int getPreferredOptionsPanelGravity() {
        synchronized (this.mWindowMap) {
            int rotation = getRotation();
            DisplayContent displayContent = getDefaultDisplayContentLocked();
            if (displayContent.mInitialDisplayWidth < displayContent.mInitialDisplayHeight) {
                switch (rotation) {
                    case 1:
                        return 85;
                    case 2:
                        return 81;
                    case 3:
                        return 8388691;
                    default:
                        return 81;
                }
            }
            switch (rotation) {
                case 1:
                    return 81;
                case 2:
                    return 8388691;
                case 3:
                    return 81;
                default:
                    return 85;
            }
        }
    }

    public boolean startViewServer(int port) {
        if (isSystemSecure() || !checkCallingPermission("android.permission.DUMP", "startViewServer") || port < 1024) {
            return false;
        }
        if (this.mViewServer != null) {
            if (!this.mViewServer.isRunning()) {
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
        if (LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(SystemProperties.get(SYSTEM_SECURE, LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON))) {
            return "0".equals(SystemProperties.get(SYSTEM_DEBUGGABLE, "0"));
        }
        return false;
    }

    public boolean stopViewServer() {
        if (isSystemSecure() || !checkCallingPermission("android.permission.DUMP", "stopViewServer") || this.mViewServer == null) {
            return false;
        }
        return this.mViewServer.stop();
    }

    public boolean isViewServerRunning() {
        boolean z = false;
        if (isSystemSecure() || !checkCallingPermission("android.permission.DUMP", "isViewServerRunning")) {
            return false;
        }
        if (this.mViewServer != null) {
            z = this.mViewServer.isRunning();
        }
        return z;
    }

    /* JADX WARNING: Removed duplicated region for block: B:35:0x0086 A:{SYNTHETIC, Splitter: B:35:0x0086} */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x0090 A:{SYNTHETIC, Splitter: B:41:0x0090} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    boolean viewServerListWindows(Socket client) {
        Throwable th;
        if (isSystemSecure()) {
            return false;
        }
        boolean result = true;
        WindowList windows = new WindowList();
        synchronized (this.mWindowMap) {
            int numDisplays = this.mDisplayContents.size();
            for (int displayNdx = 0; displayNdx < numDisplays; displayNdx++) {
                windows.addAll(((DisplayContent) this.mDisplayContents.valueAt(displayNdx)).getWindowList());
            }
        }
        BufferedWriter out = null;
        try {
            BufferedWriter out2 = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()), DumpState.DUMP_PREFERRED_XML);
            try {
                int count = windows.size();
                for (int i = 0; i < count; i++) {
                    WindowState w = (WindowState) windows.get(i);
                    out2.write(Integer.toHexString(System.identityHashCode(w)));
                    out2.write(32);
                    out2.append(w.mAttrs.getTitle());
                    out2.write(10);
                }
                out2.write("DONE.\n");
                out2.flush();
                if (out2 != null) {
                    try {
                        out2.close();
                    } catch (IOException e) {
                        result = false;
                    }
                }
                out = out2;
            } catch (Exception e2) {
                out = out2;
                result = false;
                if (out != null) {
                }
                return result;
            } catch (Throwable th2) {
                th = th2;
                out = out2;
                if (out != null) {
                }
                throw th;
            }
        } catch (Exception e3) {
            result = false;
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e4) {
                    result = false;
                }
            }
            return result;
        } catch (Throwable th3) {
            th = th3;
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e5) {
                }
            }
            throw th;
        }
        return result;
    }

    /* JADX WARNING: Removed duplicated region for block: B:21:0x004f A:{SYNTHETIC, Splitter: B:21:0x004f} */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x0059 A:{SYNTHETIC, Splitter: B:27:0x0059} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    boolean viewServerGetFocusedWindow(Socket client) {
        Throwable th;
        if (isSystemSecure()) {
            return false;
        }
        boolean result = true;
        WindowState focusedWindow = getFocusedWindow();
        BufferedWriter out = null;
        try {
            BufferedWriter out2 = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()), DumpState.DUMP_PREFERRED_XML);
            if (focusedWindow != null) {
                try {
                    out2.write(Integer.toHexString(System.identityHashCode(focusedWindow)));
                    out2.write(32);
                    out2.append(focusedWindow.mAttrs.getTitle());
                } catch (Exception e) {
                    out = out2;
                    result = false;
                    if (out != null) {
                    }
                    return result;
                } catch (Throwable th2) {
                    th = th2;
                    out = out2;
                    if (out != null) {
                    }
                    throw th;
                }
            }
            out2.write(10);
            out2.flush();
            if (out2 != null) {
                try {
                    out2.close();
                } catch (IOException e2) {
                    result = false;
                }
            }
            out = out2;
        } catch (Exception e3) {
            result = false;
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e4) {
                    result = false;
                }
            }
            return result;
        } catch (Throwable th3) {
            th = th3;
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e5) {
                }
            }
            throw th;
        }
        return result;
    }

    /* JADX WARNING: Removed duplicated region for block: B:46:0x00e7  */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x00ec  */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x00f1 A:{SYNTHETIC, Splitter: B:50:0x00f1} */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x00d4  */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x00d9  */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x00de A:{SYNTHETIC, Splitter: B:41:0x00de} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    boolean viewServerWindowCommand(Socket client, String command, String parameters) {
        Exception e;
        Throwable th;
        if (isSystemSecure()) {
            return false;
        }
        boolean success = true;
        Parcel parcel = null;
        Parcel parcel2 = null;
        BufferedWriter out = null;
        try {
            int index = parameters.indexOf(32);
            if (index == -1) {
                index = parameters.length();
            }
            int hashCode = (int) Long.parseLong(parameters.substring(0, index), 16);
            if (index < parameters.length()) {
                parameters = parameters.substring(index + 1);
            } else {
                parameters = IElsaManager.EMPTY_PACKAGE;
            }
            WindowState window = findWindow(hashCode);
            if (window == null) {
                return false;
            }
            parcel = Parcel.obtain();
            parcel.writeInterfaceToken("android.view.IWindow");
            parcel.writeString(command);
            parcel.writeString(parameters);
            parcel.writeInt(1);
            ParcelFileDescriptor.fromSocket(client).writeToParcel(parcel, 0);
            parcel2 = Parcel.obtain();
            window.mClient.asBinder().transact(1, parcel, parcel2, 0);
            parcel2.readException();
            if (!client.isOutputShutdown()) {
                BufferedWriter out2 = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
                try {
                    out2.write("DONE\n");
                    out2.flush();
                    out = out2;
                } catch (Exception e2) {
                    e = e2;
                    out = out2;
                    try {
                        Slog.w("WindowManager", "Could not send command " + command + " with parameters " + parameters, e);
                        success = false;
                        if (parcel != null) {
                            parcel.recycle();
                        }
                        if (parcel2 != null) {
                            parcel2.recycle();
                        }
                        if (out != null) {
                            try {
                                out.close();
                            } catch (IOException e3) {
                            }
                        }
                        return success;
                    } catch (Throwable th2) {
                        th = th2;
                        if (parcel != null) {
                            parcel.recycle();
                        }
                        if (parcel2 != null) {
                            parcel2.recycle();
                        }
                        if (out != null) {
                            try {
                                out.close();
                            } catch (IOException e4) {
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    out = out2;
                    if (parcel != null) {
                    }
                    if (parcel2 != null) {
                    }
                    if (out != null) {
                    }
                    throw th;
                }
            }
            if (parcel != null) {
                parcel.recycle();
            }
            if (parcel2 != null) {
                parcel2.recycle();
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e5) {
                }
            }
            return success;
        } catch (Exception e6) {
            e = e6;
            Slog.w("WindowManager", "Could not send command " + command + " with parameters " + parameters, e);
            success = false;
            if (parcel != null) {
            }
            if (parcel2 != null) {
            }
            if (out != null) {
            }
            return success;
        }
    }

    public void addWindowChangeListener(WindowChangeListener listener) {
        synchronized (this.mWindowMap) {
            this.mWindowChangeListeners.add(listener);
        }
    }

    public void removeWindowChangeListener(WindowChangeListener listener) {
        synchronized (this.mWindowMap) {
            this.mWindowChangeListeners.remove(listener);
        }
    }

    /* JADX WARNING: Missing block: B:10:0x001e, code:
            r0 = r2.length;
            r1 = 0;
     */
    /* JADX WARNING: Missing block: B:11:0x0020, code:
            if (r1 >= r0) goto L_0x002d;
     */
    /* JADX WARNING: Missing block: B:12:0x0022, code:
            r2[r1].windowsChanged();
            r1 = r1 + 1;
     */
    /* JADX WARNING: Missing block: B:16:0x002d, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void notifyWindowsChanged() {
        synchronized (this.mWindowMap) {
            if (this.mWindowChangeListeners.isEmpty()) {
            } else {
                WindowChangeListener[] windowChangeListeners = (WindowChangeListener[]) this.mWindowChangeListeners.toArray(new WindowChangeListener[this.mWindowChangeListeners.size()]);
            }
        }
    }

    /* JADX WARNING: Missing block: B:10:0x001e, code:
            r0 = r2.length;
            r1 = 0;
     */
    /* JADX WARNING: Missing block: B:11:0x0020, code:
            if (r1 >= r0) goto L_0x002d;
     */
    /* JADX WARNING: Missing block: B:12:0x0022, code:
            r2[r1].focusChanged();
            r1 = r1 + 1;
     */
    /* JADX WARNING: Missing block: B:16:0x002d, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void notifyFocusChanged() {
        synchronized (this.mWindowMap) {
            if (this.mWindowChangeListeners.isEmpty()) {
            } else {
                WindowChangeListener[] windowChangeListeners = (WindowChangeListener[]) this.mWindowChangeListeners.toArray(new WindowChangeListener[this.mWindowChangeListeners.size()]);
            }
        }
    }

    private WindowState findWindow(int hashCode) {
        if (hashCode == -1) {
            return getFocusedWindow();
        }
        synchronized (this.mWindowMap) {
            int numDisplays = this.mDisplayContents.size();
            for (int displayNdx = 0; displayNdx < numDisplays; displayNdx++) {
                WindowList windows = ((DisplayContent) this.mDisplayContents.valueAt(displayNdx)).getWindowList();
                int numWindows = windows.size();
                for (int winNdx = 0; winNdx < numWindows; winNdx++) {
                    WindowState w = (WindowState) windows.get(winNdx);
                    if (System.identityHashCode(w) == hashCode) {
                        return w;
                    }
                }
            }
            return null;
        }
    }

    void sendNewConfiguration() {
        try {
            this.mActivityManager.updateConfiguration(null);
        } catch (RemoteException e) {
        }
    }

    public Configuration computeNewConfiguration() {
        Configuration computeNewConfigurationLocked;
        synchronized (this.mWindowMap) {
            computeNewConfigurationLocked = computeNewConfigurationLocked();
        }
        return computeNewConfigurationLocked;
    }

    private Configuration computeNewConfigurationLocked() {
        if (!this.mDisplayReady) {
            return null;
        }
        Configuration config = new Configuration();
        config.fontScale = OppoBrightUtils.MIN_LUX_LIMITI;
        computeScreenConfigurationLocked(config);
        return config;
    }

    private void adjustDisplaySizeRanges(DisplayInfo displayInfo, int rotation, int uiMode, int dw, int dh) {
        int width = this.mPolicy.getConfigDisplayWidth(dw, dh, rotation, uiMode);
        if (width < displayInfo.smallestNominalAppWidth) {
            displayInfo.smallestNominalAppWidth = width;
        }
        if (width > displayInfo.largestNominalAppWidth) {
            displayInfo.largestNominalAppWidth = width;
        }
        int height = this.mPolicy.getConfigDisplayHeight(dw, dh, rotation, uiMode);
        if (height < displayInfo.smallestNominalAppHeight) {
            displayInfo.smallestNominalAppHeight = height;
        }
        if (height > displayInfo.largestNominalAppHeight) {
            displayInfo.largestNominalAppHeight = height;
        }
    }

    private int reduceConfigLayout(int curLayout, int rotation, float density, int dw, int dh, int uiMode) {
        int w = this.mPolicy.getNonDecorDisplayWidth(dw, dh, rotation, uiMode);
        int h = this.mPolicy.getNonDecorDisplayHeight(dw, dh, rotation, uiMode);
        int longSize = w;
        int shortSize = h;
        if (w < h) {
            int tmp = w;
            longSize = h;
            shortSize = w;
        }
        return Configuration.reduceScreenLayout(curLayout, (int) (((float) longSize) / density), (int) (((float) shortSize) / density));
    }

    private void computeSizeRangesAndScreenLayout(DisplayInfo displayInfo, boolean rotated, int uiMode, int dw, int dh, float density, Configuration outConfig) {
        int unrotDw;
        int unrotDh;
        if (rotated) {
            unrotDw = dh;
            unrotDh = dw;
        } else {
            unrotDw = dw;
            unrotDh = dh;
        }
        displayInfo.smallestNominalAppWidth = 1073741824;
        displayInfo.smallestNominalAppHeight = 1073741824;
        displayInfo.largestNominalAppWidth = 0;
        displayInfo.largestNominalAppHeight = 0;
        adjustDisplaySizeRanges(displayInfo, 0, uiMode, unrotDw, unrotDh);
        adjustDisplaySizeRanges(displayInfo, 1, uiMode, unrotDh, unrotDw);
        adjustDisplaySizeRanges(displayInfo, 2, uiMode, unrotDw, unrotDh);
        adjustDisplaySizeRanges(displayInfo, 3, uiMode, unrotDh, unrotDw);
        int sl = reduceConfigLayout(reduceConfigLayout(reduceConfigLayout(reduceConfigLayout(Configuration.resetScreenLayout(outConfig.screenLayout), 0, density, unrotDw, unrotDh, uiMode), 1, density, unrotDh, unrotDw, uiMode), 2, density, unrotDw, unrotDh, uiMode), 3, density, unrotDh, unrotDw, uiMode);
        outConfig.smallestScreenWidthDp = (int) (((float) displayInfo.smallestNominalAppWidth) / density);
        outConfig.screenLayout = sl;
    }

    private int reduceCompatConfigWidthSize(int curSize, int rotation, int uiMode, DisplayMetrics dm, int dw, int dh) {
        dm.noncompatWidthPixels = this.mPolicy.getNonDecorDisplayWidth(dw, dh, rotation, uiMode);
        dm.noncompatHeightPixels = this.mPolicy.getNonDecorDisplayHeight(dw, dh, rotation, uiMode);
        int size = (int) (((((float) dm.noncompatWidthPixels) / CompatibilityInfo.computeCompatibleScaling(dm, null)) / dm.density) + 0.5f);
        if (curSize == 0 || size < curSize) {
            return size;
        }
        return curSize;
    }

    private int computeCompatSmallestWidth(boolean rotated, int uiMode, DisplayMetrics dm, int dw, int dh) {
        int unrotDw;
        int unrotDh;
        this.mTmpDisplayMetrics.setTo(dm);
        DisplayMetrics tmpDm = this.mTmpDisplayMetrics;
        if (rotated) {
            unrotDw = dh;
            unrotDh = dw;
        } else {
            unrotDw = dw;
            unrotDh = dh;
        }
        return reduceCompatConfigWidthSize(reduceCompatConfigWidthSize(reduceCompatConfigWidthSize(reduceCompatConfigWidthSize(0, 0, uiMode, tmpDm, unrotDw, unrotDh), 1, uiMode, tmpDm, unrotDh, unrotDw), 2, uiMode, tmpDm, unrotDw, unrotDh), 3, uiMode, tmpDm, unrotDh, unrotDw);
    }

    DisplayInfo updateDisplayAndOrientationLocked(int uiMode) {
        DisplayContent displayContent = getDefaultDisplayContentLocked();
        boolean rotated = this.mRotation != 1 ? this.mRotation == 3 : true;
        int realdw = rotated ? displayContent.mBaseDisplayHeight : displayContent.mBaseDisplayWidth;
        int realdh = rotated ? displayContent.mBaseDisplayWidth : displayContent.mBaseDisplayHeight;
        int dw = realdw;
        int dh = realdh;
        if (this.mAltOrientation) {
            if (realdw > realdh) {
                int maxw = (int) (((float) realdh) / 1.3f);
                if (maxw < realdw) {
                    dw = maxw;
                }
            } else {
                int maxh = (int) (((float) realdw) / 1.3f);
                if (maxh < realdh) {
                    dh = maxh;
                }
            }
        }
        int appWidth = this.mPolicy.getNonDecorDisplayWidth(dw, dh, this.mRotation, uiMode);
        int appHeight = this.mPolicy.getNonDecorDisplayHeight(dw, dh, this.mRotation, uiMode);
        DisplayInfo displayInfo = displayContent.getDisplayInfo();
        displayInfo.rotation = this.mRotation;
        displayInfo.logicalWidth = dw;
        displayInfo.logicalHeight = dh;
        displayInfo.logicalDensityDpi = displayContent.mBaseDisplayDensity;
        displayInfo.appWidth = appWidth;
        displayInfo.appHeight = appHeight;
        displayInfo.getLogicalMetrics(this.mRealDisplayMetrics, CompatibilityInfo.DEFAULT_COMPATIBILITY_INFO, null);
        displayInfo.getAppMetrics(this.mDisplayMetrics);
        if (displayContent.mDisplayScalingDisabled) {
            displayInfo.flags |= 1073741824;
        } else {
            displayInfo.flags &= -1073741825;
        }
        this.mDisplayManagerInternal.setDisplayInfoOverrideFromWindowManager(displayContent.getDisplayId(), displayInfo);
        displayContent.mBaseDisplayRect.set(0, 0, dw, dh);
        this.mCompatibleScreenScale = CompatibilityInfo.computeCompatibleScaling(this.mDisplayMetrics, this.mCompatDisplayMetrics);
        return displayInfo;
    }

    void computeScreenConfigurationLocked(Configuration config) {
        int i;
        DisplayInfo displayInfo = updateDisplayAndOrientationLocked(config.uiMode);
        int dw = displayInfo.logicalWidth;
        int dh = displayInfo.logicalHeight;
        if (dw <= dh) {
            i = 1;
        } else {
            i = 2;
        }
        config.orientation = i;
        config.screenWidthDp = (int) (((float) this.mPolicy.getConfigDisplayWidth(dw, dh, this.mRotation, config.uiMode)) / this.mDisplayMetrics.density);
        config.screenHeightDp = (int) (((float) this.mPolicy.getConfigDisplayHeight(dw, dh, this.mRotation, config.uiMode)) / this.mDisplayMetrics.density);
        boolean rotated = this.mRotation != 1 ? this.mRotation == 3 : true;
        computeSizeRangesAndScreenLayout(displayInfo, rotated, config.uiMode, dw, dh, this.mDisplayMetrics.density, config);
        int i2 = config.screenLayout & -769;
        if ((displayInfo.flags & 16) != 0) {
            i = 512;
        } else {
            i = 256;
        }
        config.screenLayout = i | i2;
        config.compatScreenWidthDp = (int) (((float) config.screenWidthDp) / this.mCompatibleScreenScale);
        config.compatScreenHeightDp = (int) (((float) config.screenHeightDp) / this.mCompatibleScreenScale);
        config.compatSmallestScreenWidthDp = computeCompatSmallestWidth(rotated, config.uiMode, this.mDisplayMetrics, dw, dh);
        config.densityDpi = displayInfo.logicalDensityDpi;
        config.touchscreen = 1;
        config.keyboard = 1;
        config.navigation = 1;
        int keyboardPresence = 0;
        int navigationPresence = 0;
        for (InputDevice device : this.mInputManager.getInputDevices()) {
            if (!device.isVirtual()) {
                int presenceFlag;
                int sources = device.getSources();
                if (device.isExternal()) {
                    presenceFlag = 2;
                } else {
                    presenceFlag = 1;
                }
                if (!this.mIsTouchDevice) {
                    config.touchscreen = 1;
                } else if ((sources & 4098) == 4098) {
                    config.touchscreen = 3;
                }
                if ((65540 & sources) == 65540) {
                    config.navigation = 3;
                    navigationPresence |= presenceFlag;
                } else if ((sources & 513) == 513 && config.navigation == 1) {
                    config.navigation = 2;
                    navigationPresence |= presenceFlag;
                }
                if (device.getKeyboardType() == 2) {
                    config.keyboard = 2;
                    keyboardPresence |= presenceFlag;
                }
            }
        }
        if (config.navigation == 1 && this.mHasPermanentDpad) {
            config.navigation = 2;
            navigationPresence |= 1;
        }
        boolean hardKeyboardAvailable = config.keyboard != 1;
        if (hardKeyboardAvailable != this.mHardKeyboardAvailable) {
            this.mHardKeyboardAvailable = hardKeyboardAvailable;
            this.mH.removeMessages(22);
            this.mH.sendEmptyMessage(22);
        }
        config.keyboardHidden = 1;
        config.hardKeyboardHidden = 1;
        config.navigationHidden = 1;
        this.mPolicy.adjustConfigurationLw(config, keyboardPresence, navigationPresence);
    }

    void notifyHardKeyboardStatusChange() {
        OnHardKeyboardStatusChangeListener listener;
        boolean available;
        synchronized (this.mWindowMap) {
            listener = this.mHardKeyboardStatusChangeListener;
            available = this.mHardKeyboardAvailable;
        }
        if (listener != null) {
            listener.onHardKeyboardStatusChange(available);
        }
    }

    /* JADX WARNING: Missing block: B:10:?, code:
            r6.mActivityManager.setFocusedTask(r1.getTask().mTaskId);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    boolean startMovingTask(IWindow window, float startX, float startY) {
        synchronized (this.mWindowMap) {
            WindowState win = windowForClientLocked(null, window, false);
            if (!startPositioningLocked(win, false, startX, startY)) {
                return false;
            }
        }
        return true;
    }

    /* JADX WARNING: Missing block: B:16:?, code:
            r9.mActivityManager.setFocusedTask(r2.mTaskId);
     */
    /* JADX WARNING: Missing block: B:19:0x0063, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void startScrollingTask(DisplayContent displayContent, int startX, int startY) {
        if (WindowManagerDebugConfig.DEBUG_TASK_POSITIONING) {
            Slog.d("WindowManager", "startScrollingTask: {" + startX + ", " + startY + "}");
        }
        Task task = null;
        synchronized (this.mWindowMap) {
            int taskId = displayContent.taskIdFromPoint(startX, startY);
            if (taskId >= 0) {
                task = (Task) this.mTaskIdToTask.get(taskId);
            }
            if (!(task != null && task.isDockedInEffect() && startPositioningLocked(task.getTopVisibleAppMainWindow(), false, (float) startX, (float) startY))) {
            }
        }
    }

    /* JADX WARNING: Missing block: B:12:0x001c, code:
            if (r2 < 0) goto L_0x0023;
     */
    /* JADX WARNING: Missing block: B:14:?, code:
            r8.mActivityManager.setFocusedTask(r2);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void handleTapOutsideTask(DisplayContent displayContent, int x, int y) {
        synchronized (this.mWindowMap) {
            Task task = displayContent.findTaskForControlPoint(x, y);
            int taskId;
            if (task == null) {
                taskId = displayContent.taskIdFromPoint(x, y, true);
            } else if (startPositioningLocked(task.getTopVisibleAppMainWindow(), true, (float) x, (float) y)) {
                taskId = task.mTaskId;
            }
        }
    }

    private boolean startPositioningLocked(WindowState win, boolean resize, float startX, float startY) {
        if (WindowManagerDebugConfig.DEBUG_TASK_POSITIONING) {
            Slog.d("WindowManager", "startPositioningLocked: win=" + win + ", resize=" + resize + ", {" + startX + ", " + startY + "}");
        }
        if (win == null || win.getAppToken() == null) {
            Slog.w("WindowManager", "startPositioningLocked: Bad window " + win);
            return false;
        } else if (win.mInputChannel == null) {
            Slog.wtf("WindowManager", "startPositioningLocked: " + win + " has no input channel, " + " probably being removed");
            return false;
        } else {
            DisplayContent displayContent = win.getDisplayContent();
            if (displayContent == null) {
                Slog.w("WindowManager", "startPositioningLocked: Invalid display content " + win);
                return false;
            }
            Display display = displayContent.getDisplay();
            this.mTaskPositioner = new TaskPositioner(this);
            this.mTaskPositioner.register(display);
            this.mInputMonitor.updateInputWindowsLw(true);
            WindowState transferFocusFromWin = win;
            if (!(this.mCurrentFocus == null || this.mCurrentFocus == win || this.mCurrentFocus.mAppToken != win.mAppToken)) {
                transferFocusFromWin = this.mCurrentFocus;
            }
            if (this.mInputManager.transferTouchFocus(transferFocusFromWin.mInputChannel, this.mTaskPositioner.mServerChannel)) {
                this.mTaskPositioner.startDragLocked(win, resize, startX, startY);
                return true;
            }
            Slog.e("WindowManager", "startPositioningLocked: Unable to transfer touch focus");
            this.mTaskPositioner.unregister();
            this.mTaskPositioner = null;
            this.mInputMonitor.updateInputWindowsLw(true);
            return false;
        }
    }

    private void finishPositioning() {
        if (WindowManagerDebugConfig.DEBUG_TASK_POSITIONING) {
            Slog.d("WindowManager", "finishPositioning");
        }
        synchronized (this.mWindowMap) {
            if (this.mTaskPositioner != null) {
                this.mTaskPositioner.unregister();
                this.mTaskPositioner = null;
                this.mInputMonitor.updateInputWindowsLw(true);
            }
        }
    }

    void adjustForImeIfNeeded(DisplayContent displayContent) {
        WindowState imeWin = this.mInputMethodWindow;
        boolean imeVisible = (imeWin != null && imeWin.isVisibleLw() && imeWin.isDisplayedLw()) ? !displayContent.mDividerControllerLocked.isImeHideRequested() : false;
        boolean dockVisible = isStackVisibleLocked(3);
        TaskStack imeTargetStack = getImeFocusStackLocked();
        int imeDockSide = (!dockVisible || imeTargetStack == null) ? -1 : imeTargetStack.getDockSide();
        boolean imeOnTop = imeDockSide == 2;
        boolean imeOnBottom = imeDockSide == 4;
        boolean dockMinimized = displayContent.mDividerControllerLocked.isMinimizedDock();
        int imeHeight = this.mPolicy.getInputMethodWindowVisibleHeightLw();
        boolean imeHeightChanged = imeVisible ? imeHeight != displayContent.mDividerControllerLocked.getImeHeightAdjustedFor() : false;
        ArrayList<TaskStack> stacks;
        int i;
        if (imeVisible && dockVisible && ((imeOnTop || imeOnBottom) && !dockMinimized)) {
            stacks = displayContent.getStacks();
            for (i = stacks.size() - 1; i >= 0; i--) {
                TaskStack stack = (TaskStack) stacks.get(i);
                boolean isDockedOnBottom = stack.getDockSide() == 4;
                if (stack.isVisibleLocked() && (imeOnBottom || isDockedOnBottom)) {
                    stack.setAdjustedForIme(imeWin, imeOnBottom ? imeHeightChanged : false);
                } else {
                    stack.resetAdjustedForIme(false);
                }
            }
            displayContent.mDividerControllerLocked.setAdjustedForIme(imeOnBottom, true, true, imeWin, imeHeight);
            return;
        }
        stacks = displayContent.getStacks();
        for (i = stacks.size() - 1; i >= 0; i--) {
            ((TaskStack) stacks.get(i)).resetAdjustedForIme(!dockVisible);
        }
        displayContent.mDividerControllerLocked.setAdjustedForIme(false, false, dockVisible, imeWin, imeHeight);
    }

    /* JADX WARNING: Removed duplicated region for block: B:34:0x0161 A:{Catch:{ all -> 0x016e }} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    IBinder prepareDragSurface(IWindow window, SurfaceSession session, int flags, int width, int height, Surface outSurface) {
        OutOfResourcesException e;
        Throwable th;
        if (WindowManagerDebugConfig.DEBUG_DRAG) {
            Slog.d("WindowManager", "prepare drag surface: w=" + width + " h=" + height + " flags=" + Integer.toHexString(flags) + " win=" + window + " asbinder=" + window.asBinder());
        }
        int callerPid = Binder.getCallingPid();
        int callerUid = Binder.getCallingUid();
        long origId = Binder.clearCallingIdentity();
        IBinder token = null;
        IBinder token2;
        try {
            synchronized (this.mWindowMap) {
                try {
                    if (this.mDragState == null) {
                        Display display = getDefaultDisplayContentLocked().getDisplay();
                        SurfaceControl surface = new SurfaceControl(session, "drag surface", width, height, -3, 4);
                        surface.setLayerStack(display.getLayerStack());
                        float alpha = 1.0f;
                        if ((flags & 512) == 0) {
                            alpha = DRAG_SHADOW_ALPHA_TRANSPARENT;
                        }
                        surface.setAlpha(alpha);
                        if (WindowManagerDebugConfig.SHOW_TRANSACTIONS) {
                            Slog.i("WindowManager", "  DRAG " + surface + ": CREATE");
                        }
                        outSurface.copyFrom(surface);
                        IBinder winBinder = window.asBinder();
                        token2 = new Binder();
                        try {
                            this.mDragState = new DragState(this, token2, surface, flags, winBinder);
                            this.mDragState.mPid = callerPid;
                            this.mDragState.mUid = callerUid;
                            this.mDragState.mOriginalAlpha = alpha;
                            token = new Binder();
                            this.mDragState.mToken = token;
                            this.mH.removeMessages(20, winBinder);
                            this.mH.sendMessageDelayed(this.mH.obtainMessage(20, winBinder), 5000);
                            token2 = token;
                        } catch (OutOfResourcesException e2) {
                            e = e2;
                            try {
                                Slog.e("WindowManager", "Can't allocate drag surface w=" + width + " h=" + height, e);
                                if (this.mDragState != null) {
                                }
                                Binder.restoreCallingIdentity(origId);
                                return token2;
                            } catch (Throwable th2) {
                                th = th2;
                                throw th;
                            }
                        }
                        try {
                            Binder.restoreCallingIdentity(origId);
                            return token2;
                        } catch (Throwable th3) {
                            th = th3;
                            Binder.restoreCallingIdentity(origId);
                            throw th;
                        }
                    }
                    Slog.w("WindowManager", "Drag already in progress");
                    token2 = null;
                    Binder.restoreCallingIdentity(origId);
                    return token2;
                } catch (OutOfResourcesException e3) {
                    e = e3;
                    token2 = token;
                    Slog.e("WindowManager", "Can't allocate drag surface w=" + width + " h=" + height, e);
                    if (this.mDragState != null) {
                        this.mDragState.reset();
                        this.mDragState = null;
                    }
                    Binder.restoreCallingIdentity(origId);
                    return token2;
                } catch (Throwable th4) {
                    th = th4;
                    token2 = token;
                    throw th;
                }
            }
        } catch (Throwable th5) {
            th = th5;
            token2 = null;
            Binder.restoreCallingIdentity(origId);
            throw th;
        }
    }

    public void pauseKeyDispatching(IBinder _token) {
        if (checkCallingPermission("android.permission.MANAGE_APP_TOKENS", "pauseKeyDispatching()")) {
            synchronized (this.mWindowMap) {
                WindowToken token = (WindowToken) this.mTokenMap.get(_token);
                if (token != null) {
                    this.mInputMonitor.pauseDispatchingLw(token);
                }
            }
            return;
        }
        throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
    }

    public void resumeKeyDispatching(IBinder _token) {
        if (checkCallingPermission("android.permission.MANAGE_APP_TOKENS", "resumeKeyDispatching()")) {
            synchronized (this.mWindowMap) {
                WindowToken token = (WindowToken) this.mTokenMap.get(_token);
                if (token != null) {
                    this.mInputMonitor.resumeDispatchingLw(token);
                }
            }
            return;
        }
        throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
    }

    public void setEventDispatching(boolean enabled) {
        if (checkCallingPermission("android.permission.MANAGE_APP_TOKENS", "setEventDispatching()")) {
            synchronized (this.mWindowMap) {
                this.mEventDispatchingEnabled = enabled;
                if (this.mDisplayEnabled) {
                    this.mInputMonitor.setEventDispatchingLw(enabled);
                }
            }
            return;
        }
        throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
    }

    private WindowState getFocusedWindow() {
        WindowState focusedWindowLocked;
        synchronized (this.mWindowMap) {
            focusedWindowLocked = getFocusedWindowLocked();
        }
        return focusedWindowLocked;
    }

    private WindowState getFocusedWindowLocked() {
        return this.mCurrentFocus;
    }

    TaskStack getImeFocusStackLocked() {
        if (this.mFocusedApp == null || this.mFocusedApp.mTask == null) {
            return null;
        }
        return this.mFocusedApp.mTask.mStack;
    }

    private void showAuditSafeModeNotification() {
        PendingIntent pendingIntent = PendingIntent.getActivity(this.mContext, 0, new Intent("android.intent.action.VIEW", Uri.parse("https://support.google.com/nexus/answer/2852139")), 0);
        String title = this.mContext.getString(17040907);
        ((NotificationManager) this.mContext.getSystemService(NotificationManagerService.NOTIFICATON_TITLE_NAME)).notifyAsUser(null, 17040907, new Builder(this.mContext).setSmallIcon(17301642).setWhen(0).setOngoing(true).setTicker(title).setLocalOnly(true).setPriority(1).setVisibility(1).setColor(this.mContext.getColor(17170523)).setContentTitle(title).setContentText(this.mContext.getString(17040908)).setContentIntent(pendingIntent).build(), UserHandle.ALL);
    }

    public boolean detectSafeMode() {
        if (!this.mInputMonitor.waitForInputDevicesReady(1000)) {
            Slog.w("WindowManager", "Devices still not ready after waiting 1000 milliseconds before attempting to detect safe mode.");
        }
        if (Global.getInt(this.mContext.getContentResolver(), "safe_boot_disallowed", 0) != 0) {
            return false;
        }
        int menuState = this.mInputManager.getKeyCodeState(-1, -256, 82);
        int sState = this.mInputManager.getKeyCodeState(-1, -256, 47);
        int dpadState = this.mInputManager.getKeyCodeState(-1, 513, 23);
        int trackballState = this.mInputManager.getScanCodeState(-1, 65540, InputManagerService.BTN_MOUSE);
        int volumeDownState = this.mInputManager.getKeyCodeState(-1, -256, 25);
        this.mSafeMode = false;
        try {
            if (!(SystemProperties.getInt(ShutdownThread.REBOOT_SAFEMODE_PROPERTY, 0) == 0 && SystemProperties.getInt(ShutdownThread.RO_SAFEMODE_PROPERTY, 0) == 0)) {
                int auditSafeMode = SystemProperties.getInt(ShutdownThread.AUDIT_SAFEMODE_PROPERTY, 0);
                if (auditSafeMode == 0) {
                    this.mSafeMode = true;
                    SystemProperties.set(ShutdownThread.REBOOT_SAFEMODE_PROPERTY, IElsaManager.EMPTY_PACKAGE);
                } else if (auditSafeMode >= SystemProperties.getInt(PROPERTY_BUILD_DATE_UTC, 0)) {
                    this.mSafeMode = true;
                    showAuditSafeModeNotification();
                } else {
                    SystemProperties.set(ShutdownThread.REBOOT_SAFEMODE_PROPERTY, IElsaManager.EMPTY_PACKAGE);
                    SystemProperties.set(ShutdownThread.AUDIT_SAFEMODE_PROPERTY, IElsaManager.EMPTY_PACKAGE);
                }
            }
        } catch (IllegalArgumentException e) {
        }
        if (this.mSafeMode) {
            Log.i("WindowManager", "SAFE MODE ENABLED (menu=" + menuState + " s=" + sState + " dpad=" + dpadState + " trackball=" + trackballState + ")");
            SystemProperties.set(ShutdownThread.RO_SAFEMODE_PROPERTY, LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON);
        } else {
            Log.i("WindowManager", "SAFE MODE not enabled");
        }
        this.mPolicy.setSafeMode(this.mSafeMode);
        return this.mSafeMode;
    }

    public void displayReady() {
        for (Display display : this.mDisplays) {
            displayReady(display.getDisplayId());
        }
        synchronized (this.mWindowMap) {
            readForcedDisplayPropertiesLocked(getDefaultDisplayContentLocked());
            this.mDisplayReady = true;
        }
        try {
            this.mActivityManager.updateConfiguration(null);
        } catch (RemoteException e) {
        }
        synchronized (this.mWindowMap) {
            this.mIsTouchDevice = this.mContext.getPackageManager().hasSystemFeature("android.hardware.touchscreen");
            configureDisplayPolicyLocked(getDefaultDisplayContentLocked());
        }
        try {
            this.mActivityManager.updateConfiguration(null);
        } catch (RemoteException e2) {
        }
        updateCircularDisplayMaskIfNeeded();
    }

    private void displayReady(int displayId) {
        synchronized (this.mWindowMap) {
            DisplayContent displayContent = getDisplayContentLocked(displayId);
            if (displayContent != null) {
                this.mAnimator.addDisplayLocked(displayId);
                displayContent.initializeDisplayBaseInfo();
                if (displayContent.mTapDetector != null) {
                    displayContent.mTapDetector.init();
                }
            }
        }
    }

    public void systemReady() {
        this.mPolicy.systemReady();
        this.mSystemReady = true;
    }

    void destroyPreservedSurfaceLocked() {
        for (int i = this.mDestroyPreservedSurface.size() - 1; i >= 0; i--) {
            ((WindowState) this.mDestroyPreservedSurface.get(i)).mWinAnimator.destroyPreservedSurfaceLocked();
        }
        this.mDestroyPreservedSurface.clear();
    }

    void stopUsingSavedSurfaceLocked() {
        for (int i = this.mFinishedEarlyAnim.size() - 1; i >= 0; i--) {
            ((AppWindowToken) this.mFinishedEarlyAnim.get(i)).stopUsingSavedSurfaceLocked();
        }
        this.mFinishedEarlyAnim.clear();
    }

    public IWindowSession openSession(IWindowSessionCallback callback, IInputMethodClient client, IInputContext inputContext) {
        if (client == null) {
            throw new IllegalArgumentException("null client");
        } else if (inputContext != null) {
            return new Session(this, callback, client, inputContext);
        } else {
            throw new IllegalArgumentException("null inputContext");
        }
    }

    /* JADX WARNING: Missing block: B:42:0x014e, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean inputMethodClientHasFocus(IInputMethodClient client) {
        synchronized (this.mWindowMap) {
            int idx = findDesiredInputMethodWindowIndexLocked(false);
            if (idx > 0) {
                WindowState imFocus = (WindowState) getDefaultWindowListLocked().get(idx - 1);
                if (WindowManagerDebugConfig.DEBUG_INPUT_METHOD) {
                    Slog.i("WindowManager", "Desired input method target: " + imFocus);
                    Slog.i("WindowManager", "Current focus: " + this.mCurrentFocus);
                    Slog.i("WindowManager", "Last focus: " + this.mLastFocus);
                }
                if (imFocus != null) {
                    if (imFocus.mAttrs.type == 3 && imFocus.mAppToken != null) {
                        for (int i = 0; i < imFocus.mAppToken.windows.size(); i++) {
                            WindowState w = (WindowState) imFocus.mAppToken.windows.get(i);
                            if (w != imFocus) {
                                Log.i("WindowManager", "Switching to real app window: " + w);
                                imFocus = w;
                                break;
                            }
                        }
                    }
                    if (WindowManagerDebugConfig.DEBUG_INPUT_METHOD) {
                        Slog.i("WindowManager", "IM target client: " + imFocus.mSession.mClient);
                        if (imFocus.mSession.mClient != null) {
                            Slog.i("WindowManager", "IM target client binder: " + imFocus.mSession.mClient.asBinder());
                            Slog.i("WindowManager", "Requesting client binder: " + client.asBinder());
                        }
                    }
                    if (imFocus.mSession.mClient != null && imFocus.mSession.mClient.asBinder() == client.asBinder()) {
                        return true;
                    }
                }
            }
            if (this.mCurrentFocus == null || this.mCurrentFocus.mSession.mClient == null || this.mCurrentFocus.mSession.mClient.asBinder() != client.asBinder()) {
            } else {
                return true;
            }
        }
    }

    public void getInitialDisplaySize(int displayId, Point size) {
        synchronized (this.mWindowMap) {
            DisplayContent displayContent = getDisplayContentLocked(displayId);
            if (displayContent != null && displayContent.hasAccess(Binder.getCallingUid())) {
                size.x = displayContent.mInitialDisplayWidth;
                size.y = displayContent.mInitialDisplayHeight;
            }
        }
    }

    public void getBaseDisplaySize(int displayId, Point size) {
        synchronized (this.mWindowMap) {
            DisplayContent displayContent = getDisplayContentLocked(displayId);
            if (displayContent != null && displayContent.hasAccess(Binder.getCallingUid())) {
                size.x = displayContent.mBaseDisplayWidth;
                size.y = displayContent.mBaseDisplayHeight;
            }
        }
    }

    public void setForcedDisplaySize(int displayId, int width, int height) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.WRITE_SECURE_SETTINGS") != 0) {
            throw new SecurityException("Must hold permission android.permission.WRITE_SECURE_SETTINGS");
        } else if (displayId != 0) {
            throw new IllegalArgumentException("Can only set the default display");
        } else {
            long ident = Binder.clearCallingIdentity();
            try {
                synchronized (this.mWindowMap) {
                    DisplayContent displayContent = getDisplayContentLocked(displayId);
                    if (displayContent != null) {
                        width = Math.min(Math.max(width, 200), displayContent.mInitialDisplayWidth * 2);
                        height = Math.min(Math.max(height, 200), displayContent.mInitialDisplayHeight * 2);
                        setForcedDisplaySizeLocked(displayContent, width, height);
                        Global.putString(this.mContext.getContentResolver(), "display_size_forced", width + "," + height);
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    public void setForcedDisplayScalingMode(int displayId, int mode) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.WRITE_SECURE_SETTINGS") != 0) {
            throw new SecurityException("Must hold permission android.permission.WRITE_SECURE_SETTINGS");
        } else if (displayId != 0) {
            throw new IllegalArgumentException("Can only set the default display");
        } else {
            long ident = Binder.clearCallingIdentity();
            try {
                synchronized (this.mWindowMap) {
                    DisplayContent displayContent = getDisplayContentLocked(displayId);
                    if (displayContent != null) {
                        if (mode < 0 || mode > 1) {
                            mode = 0;
                        }
                        setForcedDisplayScalingModeLocked(displayContent, mode);
                        Global.putInt(this.mContext.getContentResolver(), "display_scaling_force", mode);
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    private void setForcedDisplayScalingModeLocked(DisplayContent displayContent, int mode) {
        boolean z;
        Slog.i("WindowManager", "Using display scaling mode: " + (mode == 0 ? "auto" : "off"));
        if (mode != 0) {
            z = true;
        } else {
            z = false;
        }
        displayContent.mDisplayScalingDisabled = z;
        reconfigureDisplayLocked(displayContent);
    }

    private void readForcedDisplayPropertiesLocked(DisplayContent displayContent) {
        String sizeStr = Global.getString(this.mContext.getContentResolver(), "display_size_forced");
        if (sizeStr == null || sizeStr.length() == 0) {
            sizeStr = SystemProperties.get(SIZE_OVERRIDE, null);
        }
        if (sizeStr != null && sizeStr.length() > 0) {
            int pos = sizeStr.indexOf(44);
            if (pos > 0 && sizeStr.lastIndexOf(44) == pos) {
                try {
                    int width = Integer.parseInt(sizeStr.substring(0, pos));
                    int height = Integer.parseInt(sizeStr.substring(pos + 1));
                    if (!(displayContent.mBaseDisplayWidth == width && displayContent.mBaseDisplayHeight == height)) {
                        Slog.i("WindowManager", "FORCED DISPLAY SIZE: " + width + "x" + height);
                        displayContent.mBaseDisplayWidth = width;
                        displayContent.mBaseDisplayHeight = height;
                    }
                } catch (NumberFormatException e) {
                }
            }
        }
        int density = getForcedDisplayDensityForUserLocked(this.mCurrentUserId);
        if (density != 0) {
            displayContent.mBaseDisplayDensity = density;
        }
        if (Global.getInt(this.mContext.getContentResolver(), "display_scaling_force", 0) != 0) {
            Slog.i("WindowManager", "FORCED DISPLAY SCALING DISABLED");
            displayContent.mDisplayScalingDisabled = true;
        }
    }

    private void setForcedDisplaySizeLocked(DisplayContent displayContent, int width, int height) {
        Slog.i("WindowManager", "Using new display size: " + width + "x" + height);
        displayContent.mBaseDisplayWidth = width;
        displayContent.mBaseDisplayHeight = height;
        reconfigureDisplayLocked(displayContent);
    }

    public void clearForcedDisplaySize(int displayId) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.WRITE_SECURE_SETTINGS") != 0) {
            throw new SecurityException("Must hold permission android.permission.WRITE_SECURE_SETTINGS");
        } else if (displayId != 0) {
            throw new IllegalArgumentException("Can only set the default display");
        } else {
            long ident = Binder.clearCallingIdentity();
            try {
                synchronized (this.mWindowMap) {
                    DisplayContent displayContent = getDisplayContentLocked(displayId);
                    if (displayContent != null) {
                        setForcedDisplaySizeLocked(displayContent, displayContent.mInitialDisplayWidth, displayContent.mInitialDisplayHeight);
                        Global.putString(this.mContext.getContentResolver(), "display_size_forced", IElsaManager.EMPTY_PACKAGE);
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    /* JADX WARNING: Missing block: B:12:0x0019, code:
            return -1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int getInitialDisplayDensity(int displayId) {
        synchronized (this.mWindowMap) {
            DisplayContent displayContent = getDisplayContentLocked(displayId);
            if (displayContent == null || !displayContent.hasAccess(Binder.getCallingUid())) {
            } else {
                int i = displayContent.mInitialDisplayDensity;
                return i;
            }
        }
    }

    /* JADX WARNING: Missing block: B:12:0x0019, code:
            return -1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int getBaseDisplayDensity(int displayId) {
        synchronized (this.mWindowMap) {
            DisplayContent displayContent = getDisplayContentLocked(displayId);
            if (displayContent == null || !displayContent.hasAccess(Binder.getCallingUid())) {
            } else {
                int i = displayContent.mBaseDisplayDensity;
                return i;
            }
        }
    }

    public void setForcedDisplayDensityForUser(int displayId, int density, int userId) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.WRITE_SECURE_SETTINGS") != 0) {
            throw new SecurityException("Must hold permission android.permission.WRITE_SECURE_SETTINGS");
        } else if (displayId != 0) {
            throw new IllegalArgumentException("Can only set the default display");
        } else {
            int targetUserId = ActivityManager.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, false, true, "setForcedDisplayDensityForUser", null);
            long ident = Binder.clearCallingIdentity();
            try {
                synchronized (this.mWindowMap) {
                    DisplayContent displayContent = getDisplayContentLocked(displayId);
                    if (displayContent != null && this.mCurrentUserId == targetUserId) {
                        setForcedDisplayDensityLocked(displayContent, density);
                    }
                    Secure.putStringForUser(this.mContext.getContentResolver(), "display_density_forced", Integer.toString(density), targetUserId);
                }
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    public void clearForcedDisplayDensityForUser(int displayId, int userId) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.WRITE_SECURE_SETTINGS") != 0) {
            throw new SecurityException("Must hold permission android.permission.WRITE_SECURE_SETTINGS");
        } else if (displayId != 0) {
            throw new IllegalArgumentException("Can only set the default display");
        } else {
            int callingUserId = ActivityManager.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, false, true, "clearForcedDisplayDensityForUser", null);
            long ident = Binder.clearCallingIdentity();
            try {
                synchronized (this.mWindowMap) {
                    DisplayContent displayContent = getDisplayContentLocked(displayId);
                    if (displayContent != null && this.mCurrentUserId == callingUserId) {
                        setForcedDisplayDensityLocked(displayContent, displayContent.mInitialDisplayDensity);
                    }
                    Secure.putStringForUser(this.mContext.getContentResolver(), "display_density_forced", IElsaManager.EMPTY_PACKAGE, callingUserId);
                }
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    private int getForcedDisplayDensityForUserLocked(int userId) {
        String densityStr = Secure.getStringForUser(this.mContext.getContentResolver(), "display_density_forced", userId);
        if (densityStr == null || densityStr.length() == 0) {
            densityStr = SystemProperties.get(DENSITY_OVERRIDE, null);
        }
        if (densityStr != null && densityStr.length() > 0) {
            try {
                return Integer.parseInt(densityStr);
            } catch (NumberFormatException e) {
            }
        }
        return 0;
    }

    private void setForcedDisplayDensityLocked(DisplayContent displayContent, int density) {
        displayContent.mBaseDisplayDensity = density;
        reconfigureDisplayLocked(displayContent);
    }

    private void reconfigureDisplayLocked(DisplayContent displayContent) {
        if (this.mDisplayReady) {
            configureDisplayPolicyLocked(displayContent);
            displayContent.layoutNeeded = true;
            boolean configChanged = updateOrientationFromAppTokensLocked(false);
            this.mTempConfiguration.setToDefaults();
            this.mTempConfiguration.updateFrom(this.mCurConfiguration);
            computeScreenConfigurationLocked(this.mTempConfiguration);
            if (configChanged | (this.mCurConfiguration.diff(this.mTempConfiguration) != 0 ? 1 : 0)) {
                this.mWaitingForConfig = true;
                startFreezingDisplayLocked(false, 0, 0);
                this.mH.sendEmptyMessage(18);
                if (!this.mReconfigureOnConfigurationChanged.contains(displayContent)) {
                    this.mReconfigureOnConfigurationChanged.add(displayContent);
                }
            }
            this.mWindowPlacerLocked.performSurfacePlacement();
        }
    }

    private void configureDisplayPolicyLocked(DisplayContent displayContent) {
        this.mPolicy.setInitialDisplaySize(displayContent.getDisplay(), displayContent.mBaseDisplayWidth, displayContent.mBaseDisplayHeight, displayContent.mBaseDisplayDensity);
        DisplayInfo displayInfo = displayContent.getDisplayInfo();
        this.mPolicy.setDisplayOverscan(displayContent.getDisplay(), displayInfo.overscanLeft, displayInfo.overscanTop, displayInfo.overscanRight, displayInfo.overscanBottom);
    }

    public void setOverscan(int displayId, int left, int top, int right, int bottom) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.WRITE_SECURE_SETTINGS") != 0) {
            throw new SecurityException("Must hold permission android.permission.WRITE_SECURE_SETTINGS");
        }
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mWindowMap) {
                DisplayContent displayContent = getDisplayContentLocked(displayId);
                if (displayContent != null) {
                    setOverscanLocked(displayContent, left, top, right, bottom);
                }
            }
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    private void setOverscanLocked(DisplayContent displayContent, int left, int top, int right, int bottom) {
        DisplayInfo displayInfo = displayContent.getDisplayInfo();
        displayInfo.overscanLeft = left;
        displayInfo.overscanTop = top;
        displayInfo.overscanRight = right;
        displayInfo.overscanBottom = bottom;
        this.mDisplaySettings.setOverscanLocked(displayInfo.uniqueId, displayInfo.name, left, top, right, bottom);
        this.mDisplaySettings.writeSettingsLocked();
        reconfigureDisplayLocked(displayContent);
    }

    final WindowState windowForClientLocked(Session session, IWindow client, boolean throwOnError) {
        return windowForClientLocked(session, client.asBinder(), throwOnError);
    }

    final WindowState windowForClientLocked(Session session, IBinder client, boolean throwOnError) {
        WindowState win = (WindowState) this.mWindowMap.get(client);
        if (localLOGV) {
            Slog.v("WindowManager", "Looking up client " + client + ": " + win);
        }
        RuntimeException ex;
        if (win == null) {
            ex = new IllegalArgumentException("Requested window " + client + " does not exist");
            if (throwOnError) {
                throw ex;
            }
            Slog.w("WindowManager", "Failed looking up window", ex);
            return null;
        } else if (session == null || win.mSession == session) {
            return win;
        } else {
            ex = new IllegalArgumentException("Requested window " + client + " is in session " + win.mSession + ", not " + session);
            if (throwOnError) {
                throw ex;
            }
            Slog.w("WindowManager", "Failed looking up window", ex);
            return null;
        }
    }

    final void rebuildAppWindowListLocked() {
        rebuildAppWindowListLocked(getDefaultDisplayContentLocked());
    }

    /* JADX WARNING: Removed duplicated region for block: B:27:0x00d7  */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x0120  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void rebuildAppWindowListLocked(DisplayContent displayContent) {
        int stackNdx;
        int numbAdd;
        WindowList windows = displayContent.getWindowList();
        int NW = windows.size();
        int lastBelow = -1;
        int numRemoved = 0;
        int numReAdd = 0;
        int inputMethodIndex = -1;
        boolean isMultiWindowMode = !isStackVisibleLocked(3) ? isStackVisibleLocked(2) : true;
        if (this.mRebuildTmp.length < NW) {
            this.mRebuildTmp = new WindowState[(NW + 10)];
        }
        WindowState lanucherWin = null;
        int i = 0;
        while (i < NW) {
            WindowState w = (WindowState) windows.get(i);
            if (w.mAppToken != null) {
                boolean tempIsAboveInputMethod;
                WindowState win;
                w.mAppToken.mIsAboveInputMethod = false;
                if (inputMethodIndex != -1) {
                    try {
                        if (w.mAppToken.appToken == null || !(w.mAppToken.appToken.isHomeActivity() || w.mAppToken.appToken.isRecentsActivity())) {
                            tempIsAboveInputMethod = true;
                            if (!w.mAppToken.mVisitBeforeInputMethod || w.mAttrs.type < 1000) {
                                w.mAppToken.mIsAboveInputMethod = tempIsAboveInputMethod;
                            }
                            if (!w.mAppToken.mIsAboveInputMethod) {
                                w.mAppToken.mVisitBeforeInputMethod = true;
                            }
                            win = (WindowState) windows.remove(i);
                            win.mRebuilding = true;
                            this.mRebuildTmp[numRemoved] = win;
                            this.mWindowsChanged = true;
                            if (!(win == null || win.getOwningPackage() == null || !win.getOwningPackage().contains(ActivityManagerService.OPPO_LAUNCHER))) {
                                lanucherWin = win;
                            }
                            if (WindowManagerDebugConfig.DEBUG_WINDOW_MOVEMENT) {
                                Slog.v("WindowManager", "Rebuild removing window: " + win);
                            }
                            NW--;
                            numRemoved++;
                        }
                    } catch (RemoteException e) {
                    }
                }
                tempIsAboveInputMethod = false;
                w.mAppToken.mIsAboveInputMethod = tempIsAboveInputMethod;
                if (w.mAppToken.mIsAboveInputMethod) {
                }
                win = (WindowState) windows.remove(i);
                win.mRebuilding = true;
                this.mRebuildTmp[numRemoved] = win;
                this.mWindowsChanged = true;
                lanucherWin = win;
                if (WindowManagerDebugConfig.DEBUG_WINDOW_MOVEMENT) {
                }
                NW--;
                numRemoved++;
            } else {
                if (lastBelow == i - 1 && w.mAttrs.type == 2013) {
                    lastBelow = i;
                }
                if (!isMultiWindowMode && ((w.mAttrs.type == 2011 || w.mAttrs.type == 2012) && (w.mAttrs.flags & DumpState.DUMP_PREFERRED_XML) == 0)) {
                    inputMethodIndex = i;
                }
                i++;
            }
        }
        i = lastBelow + 1;
        inputMethodIndex++;
        ArrayList<TaskStack> stacks = displayContent.getStacks();
        int numStacks = stacks.size();
        for (stackNdx = 0; stackNdx < numStacks; stackNdx++) {
            AppTokenList exitingAppTokens = ((TaskStack) stacks.get(stackNdx)).mExitingAppTokens;
            int NT = exitingAppTokens.size();
            for (int j = 0; j < NT; j++) {
                numbAdd = i;
                i = reAddAppWindowsLocked(displayContent, i, (WindowToken) exitingAppTokens.get(j));
                numbAdd = i - numbAdd;
                inputMethodIndex += numbAdd;
                numReAdd += numbAdd;
            }
        }
        for (stackNdx = 0; stackNdx < numStacks; stackNdx++) {
            ArrayList<Task> tasks = ((TaskStack) stacks.get(stackNdx)).getTasks();
            int numTasks = tasks.size();
            for (int taskNdx = 0; taskNdx < numTasks; taskNdx++) {
                AppTokenList tokens = ((Task) tasks.get(taskNdx)).mAppTokens;
                int numTokens = tokens.size();
                for (int tokenNdx = 0; tokenNdx < numTokens; tokenNdx++) {
                    WindowToken wtoken = (AppWindowToken) tokens.get(tokenNdx);
                    wtoken.mVisitBeforeInputMethod = false;
                    if (!wtoken.mIsExiting || wtoken.waitingForReplacement()) {
                        if (wtoken.mIsAboveInputMethod) {
                            numbAdd = inputMethodIndex;
                            inputMethodIndex = reAddAppWindowsLocked(displayContent, inputMethodIndex, wtoken);
                            numbAdd = inputMethodIndex - numbAdd;
                        } else {
                            numbAdd = i;
                            i = reAddAppWindowsLocked(displayContent, i, wtoken);
                            numbAdd = i - numbAdd;
                            inputMethodIndex += numbAdd;
                        }
                        numReAdd += numbAdd;
                    }
                }
            }
        }
        adjustLanucherLayerLocked(displayContent, lanucherWin);
        if (numReAdd != numRemoved) {
            displayContent.layoutNeeded = true;
            Slog.w("WindowManager", "On display=" + displayContent.getDisplayId() + " Rebuild removed " + numRemoved + " windows but added " + numReAdd + " rebuildAppWindowListLocked() " + " callers=" + Debug.getCallers(10));
            for (i = 0; i < numRemoved; i++) {
                WindowState ws = this.mRebuildTmp[i];
                if (ws.mRebuilding) {
                    Writer sw = new StringWriter();
                    PrintWriter fastPrintWriter = new FastPrintWriter(sw, false, 1024);
                    ws.dump(fastPrintWriter, IElsaManager.EMPTY_PACKAGE, true);
                    fastPrintWriter.flush();
                    Slog.w("WindowManager", "This window was lost: " + ws);
                    Slog.w("WindowManager", sw.toString());
                    ws.mWinAnimator.destroySurfaceLocked();
                }
            }
            Slog.w("WindowManager", "Current app token list:");
            dumpAppTokensLocked();
            Slog.w("WindowManager", "Final window list:");
            dumpWindowsLocked();
        }
        Arrays.fill(this.mRebuildTmp, null);
    }

    void makeWindowFreezingScreenIfNeededLocked(WindowState w) {
        if (!okToDisplay() && this.mWindowsFreezingScreen != 2) {
            if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                Slog.v("WindowManager", "Changing surface while display frozen: " + w);
            }
            w.mOrientationChanging = true;
            w.mLastFreezeDuration = 0;
            this.mWindowPlacerLocked.mOrientationChangeComplete = false;
            if (this.mWindowsFreezingScreen == 0) {
                this.mWindowsFreezingScreen = 1;
                int override_window_freeze_timeout = -1;
                if (!(this.mFocusedApp == null || this.mFocusedApp.toString() == null || !this.mFocusedApp.toString().contains("com.jb.gosms"))) {
                    override_window_freeze_timeout = 6000;
                }
                this.mH.removeMessages(11);
                H h = this.mH;
                if (override_window_freeze_timeout <= 0) {
                    override_window_freeze_timeout = 2000;
                }
                h.sendEmptyMessageDelayed(11, (long) override_window_freeze_timeout);
            }
        }
    }

    int handleAnimatingStoppedAndTransitionLocked() {
        this.mAppTransition.setIdle();
        this.dismissDockedStackFromHome = false;
        for (int i = this.mNoAnimationNotifyOnTransitionFinished.size() - 1; i >= 0; i--) {
            this.mAppTransition.notifyAppTransitionFinishedLocked((IBinder) this.mNoAnimationNotifyOnTransitionFinished.get(i));
        }
        this.mNoAnimationNotifyOnTransitionFinished.clear();
        this.mWallpaperControllerLocked.hideDeferredWallpapersIfNeeded();
        ArrayList<TaskStack> stacks = getDefaultDisplayContentLocked().getStacks();
        for (int stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
            ArrayList<Task> tasks = ((TaskStack) stacks.get(stackNdx)).getTasks();
            for (int taskNdx = tasks.size() - 1; taskNdx >= 0; taskNdx--) {
                AppTokenList tokens = ((Task) tasks.get(taskNdx)).mAppTokens;
                for (int tokenNdx = tokens.size() - 1; tokenNdx >= 0; tokenNdx--) {
                    ((AppWindowToken) tokens.get(tokenNdx)).sendingToBottom = false;
                }
            }
        }
        rebuildAppWindowListLocked();
        if (WindowManagerDebugConfig.DEBUG_WALLPAPER_LIGHT) {
            Slog.v("WindowManager", "Wallpaper layer changed: assigning layers + relayout");
        }
        moveInputMethodWindowsIfNeededLocked(true);
        this.mWindowPlacerLocked.mWallpaperMayChange = true;
        this.mFocusMayChange = true;
        return 1;
    }

    void updateResizingWindows(WindowState w) {
        WindowStateAnimator winAnimator = w.mWinAnimator;
        if (WindowManagerDebugConfig.DEBUG_RESIZE || WindowManagerDebugConfig.DEBUG_ORIENTATION) {
            Slog.v("WindowManager", "Update resizing " + w + ":mHasSurface=" + w.mHasSurface + " frame=" + w.mFrame + " w.isVisibleLw() " + w.isVisibleLw());
        }
        if (w.mAttrs.type == 2004 && !w.isVisibleLw() && w.isConfigChanged()) {
            if (WindowManagerDebugConfig.DEBUG_RESIZE || WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                Slog.v("WindowManager", "win " + w + " is not visible ,skip it");
            }
            return;
        }
        if (w.mAttrs.type == 2004) {
            boolean rotated;
            if (this.mRotation == 1) {
                rotated = true;
            } else if (this.mRotation == 3) {
                rotated = true;
            } else {
                rotated = false;
            }
            if (rotated && w.mFrame != null && w.mFrame.bottom < w.mFrame.right && w.mFrame.bottom > 0) {
                Slog.v("WindowManager", "win " + w + " screen rotation ,skip it");
                return;
            }
        }
        if (w.mHasSurface && w.mLayoutSeq == this.mLayoutSeq && !w.isGoneForLayoutLw()) {
            Task task = w.getTask();
            if (task == null || !task.mStack.getBoundsAnimating()) {
                w.setReportResizeHints();
                boolean configChanged = w.isConfigChanged();
                if (WindowManagerDebugConfig.DEBUG_CONFIGURATION && configChanged) {
                    Slog.v("WindowManager", "Win " + w + " config changed: " + this.mCurConfiguration);
                }
                boolean dragResizingChanged = w.isDragResizeChanged() ? !w.isDragResizingChangeReported() : false;
                if (localLOGV || WindowManagerDebugConfig.DEBUG_RESIZE) {
                    Slog.v("WindowManager", "Resizing " + w + ": configChanged=" + configChanged + " dragResizingChanged=" + dragResizingChanged + " last=" + w.mLastFrame + " frame=" + w.mFrame);
                }
                w.mLastFrame.set(w.mFrame);
                if (w.mContentInsetsChanged || w.mVisibleInsetsChanged || winAnimator.mSurfaceResized || w.mOutsetsChanged || w.mFrameSizeChanged || configChanged || dragResizingChanged || !w.isResizedWhileNotDragResizingReported()) {
                    if (WindowManagerDebugConfig.DEBUG_RESIZE || WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                        Slog.v("WindowManager", "Resize reasons for w=" + w + ": " + " contentInsetsChanged=" + w.mContentInsetsChanged + " " + w.mContentInsets.toShortString() + " visibleInsetsChanged=" + w.mVisibleInsetsChanged + " " + w.mVisibleInsets.toShortString() + " stableInsetsChanged=" + w.mStableInsetsChanged + " " + w.mStableInsets.toShortString() + " outsetsChanged=" + w.mOutsetsChanged + " " + w.mOutsets.toShortString() + " surfaceResized=" + winAnimator.mSurfaceResized + " configChanged=" + configChanged + " dragResizingChanged=" + dragResizingChanged + " resizedWhileNotDragResizingReported=" + w.isResizedWhileNotDragResizingReported() + " contentInsets=" + w.mContentInsets + " visibleInsets=" + w.mVisibleInsets);
                    }
                    if (w.mAppToken == null || !w.mAppDied) {
                        w.mLastOverscanInsets.set(w.mOverscanInsets);
                        w.mLastContentInsets.set(w.mContentInsets);
                        w.mLastVisibleInsets.set(w.mVisibleInsets);
                        w.mLastStableInsets.set(w.mStableInsets);
                        w.mLastOutsets.set(w.mOutsets);
                        makeWindowFreezingScreenIfNeededLocked(w);
                        if (w.mOrientationChanging || dragResizingChanged || w.isResizedWhileNotDragResizing()) {
                            if (WindowManagerDebugConfig.DEBUG_SURFACE_TRACE || WindowManagerDebugConfig.DEBUG_ANIM || WindowManagerDebugConfig.DEBUG_ORIENTATION || WindowManagerDebugConfig.DEBUG_RESIZE) {
                                Slog.v("WindowManager", "Orientation or resize start waiting for draw, mDrawState=DRAW_PENDING in " + w + ", surfaceController " + winAnimator.mSurfaceController);
                            }
                            winAnimator.mDrawState = 1;
                            if (w.mAppToken != null) {
                                w.mAppToken.clearAllDrawn();
                            }
                        }
                        if (!this.mResizingWindows.contains(w)) {
                            if (WindowManagerDebugConfig.DEBUG_RESIZE || WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                                Slog.v("WindowManager", "Resizing window " + w);
                            }
                            this.mResizingWindows.add(w);
                        }
                    } else {
                        w.mAppToken.removeAllDeadWindows();
                    }
                } else if (w.mOrientationChanging && w.isDrawnLw()) {
                    if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                        Slog.v("WindowManager", "Orientation not waiting for draw in " + w + ", surfaceController " + winAnimator.mSurfaceController);
                    }
                    w.mOrientationChanging = false;
                    w.mLastFreezeDuration = (int) (SystemClock.elapsedRealtime() - this.mDisplayFreezeTime);
                }
            }
        }
    }

    void checkDrawnWindowsLocked() {
        if (!this.mWaitingForDrawn.isEmpty() && (this.mWaitingForDrawnCallback != null || this.mDockedForDrawnCallback != null)) {
            for (int j = this.mWaitingForDrawn.size() - 1; j >= 0; j--) {
                WindowState win = (WindowState) this.mWaitingForDrawn.get(j);
                if (WindowManagerDebugConfig.DEBUG_SCREEN_ON) {
                    Slog.i("WindowManager", "Waiting for drawn " + win + ": removed=" + win.mRemoved + " visible=" + win.isVisibleLw() + " mHasSurface=" + win.mHasSurface + " drawState=" + win.mWinAnimator.mDrawState);
                }
                if (!this.mPolicy.isKeyguardHostWindow(win.mAttrs)) {
                    if (win.mRemoved || !win.mHasSurface || !win.mPolicyVisibility) {
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

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "YuHao@Plf.DesktopApp.Keyguard, 2015/01/27: BUG 576326 BUG 600338", property = OppoRomType.ROM)
    private boolean releaseWakeLockWhenKeyguardShownForcibly() {
        if (this.mHoldingScreenWakeLock == null || this.mPolicy == null || !this.mPolicy.isKeyguardLocked() || this.mKeyguard == null || !this.mKeyguard.isVisibleNow()) {
            return false;
        }
        if (this.mHoldingScreenWakeLock.isHeld()) {
            this.mLastWakeLockHoldingWindow = null;
            this.mLastWakeLockObscuringWindow = this.mWindowPlacerLocked.mObsuringWindow;
            this.mPolicy.keepScreenOnStoppedLw();
            this.mHoldingScreenWakeLock.release();
        }
        return true;
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "YuHao@Plf.DesktopApp.Keyguard, 2015/01/27: BUG 576326", property = OppoRomType.ROM)
    void setHoldScreenLocked(Session newHoldScreen) {
        boolean hold = newHoldScreen != null;
        if (hold && this.mHoldingScreenOn != newHoldScreen) {
            this.mHoldingScreenWakeLock.setWorkSource(new WorkSource(newHoldScreen.mUid));
        }
        this.mHoldingScreenOn = newHoldScreen;
        boolean state = this.mHoldingScreenWakeLock.isHeld();
        if (!(releaseWakeLockWhenKeyguardShownForcibly() || hold == state)) {
            if (hold) {
                if (WindowManagerDebugConfig.DEBUG_KEEP_SCREEN_ON) {
                    Slog.d("DebugKeepScreenOn", "Acquiring screen wakelock due to " + this.mWindowPlacerLocked.mHoldScreenWindow);
                }
                this.mLastWakeLockHoldingWindow = this.mWindowPlacerLocked.mHoldScreenWindow;
                this.mLastWakeLockObscuringWindow = null;
                this.mHoldingScreenWakeLock.acquire();
                this.mPolicy.keepScreenOnStartedLw();
            } else {
                if (WindowManagerDebugConfig.DEBUG_KEEP_SCREEN_ON) {
                    Slog.d("DebugKeepScreenOn", "Releasing screen wakelock, obscured by " + this.mWindowPlacerLocked.mObsuringWindow);
                }
                this.mLastWakeLockHoldingWindow = null;
                this.mLastWakeLockObscuringWindow = this.mWindowPlacerLocked.mObsuringWindow;
                this.mPolicy.keepScreenOnStoppedLw();
                this.mHoldingScreenWakeLock.release();
            }
        }
    }

    void requestTraversal() {
        synchronized (this.mWindowMap) {
            this.mWindowPlacerLocked.requestTraversal();
        }
    }

    void scheduleAnimationLocked() {
        if (!this.mAnimationScheduled) {
            this.mAnimationScheduled = true;
            this.mChoreographer.postFrameCallback(this.mAnimator.mAnimationFrameCallback);
        }
    }

    boolean needsLayout() {
        int numDisplays = this.mDisplayContents.size();
        for (int displayNdx = 0; displayNdx < numDisplays; displayNdx++) {
            if (((DisplayContent) this.mDisplayContents.valueAt(displayNdx)).layoutNeeded) {
                return true;
            }
        }
        return false;
    }

    int adjustAnimationBackground(WindowStateAnimator winAnimator) {
        WindowList windows = winAnimator.mWin.getWindowList();
        for (int i = windows.size() - 1; i >= 0; i--) {
            WindowState testWin = (WindowState) windows.get(i);
            if (testWin.mIsWallpaper && testWin.isVisibleNow()) {
                return testWin.mWinAnimator.mAnimLayer;
            }
        }
        return winAnimator.mAnimLayer;
    }

    boolean reclaimSomeSurfaceMemoryLocked(WindowStateAnimator winAnimator, String operation, boolean secure) {
        WindowSurfaceController surfaceController = winAnimator.mSurfaceController;
        boolean leakedSurface = false;
        boolean killedApps = false;
        String[] strArr = new Object[3];
        strArr[0] = winAnimator.mWin.toString();
        strArr[1] = Integer.valueOf(winAnimator.mSession.mPid);
        strArr[2] = operation;
        EventLog.writeEvent(EventLogTags.WM_NO_SURFACE_MEMORY, strArr);
        long callingIdentity = Binder.clearCallingIdentity();
        try {
            int displayNdx;
            WindowList windows;
            int numWindows;
            int winNdx;
            WindowState ws;
            WindowStateAnimator wsa;
            Slog.i("WindowManager", "Out of memory for surface!  Looking for leaks...");
            int numDisplays = this.mDisplayContents.size();
            for (displayNdx = 0; displayNdx < numDisplays; displayNdx++) {
                windows = ((DisplayContent) this.mDisplayContents.valueAt(displayNdx)).getWindowList();
                numWindows = windows.size();
                for (winNdx = 0; winNdx < numWindows; winNdx++) {
                    ws = (WindowState) windows.get(winNdx);
                    wsa = ws.mWinAnimator;
                    if (wsa.mSurfaceController != null) {
                        if (!this.mSessions.contains(wsa.mSession)) {
                            Slog.w("WindowManager", "LEAKED SURFACE (session doesn't exist): " + ws + " surface=" + wsa.mSurfaceController + " token=" + ws.mToken + " pid=" + ws.mSession.mPid + " uid=" + ws.mSession.mUid);
                            wsa.destroySurface();
                            this.mForceRemoves.add(ws);
                            leakedSurface = true;
                        } else if (ws.mAppToken != null && ws.mAppToken.clientHidden) {
                            Slog.w("WindowManager", "LEAKED SURFACE (app token hidden): " + ws + " surface=" + wsa.mSurfaceController + " token=" + ws.mAppToken + " saved=" + ws.hasSavedSurface());
                            if (WindowManagerDebugConfig.SHOW_TRANSACTIONS) {
                                logSurface(ws, "LEAK DESTROY", false);
                            }
                            wsa.destroySurface();
                            leakedSurface = true;
                        }
                    }
                }
            }
            if (!leakedSurface) {
                Slog.w("WindowManager", "No leaked surfaces; killing applicatons!");
                SparseIntArray pidCandidates = new SparseIntArray();
                for (displayNdx = 0; displayNdx < numDisplays; displayNdx++) {
                    windows = ((DisplayContent) this.mDisplayContents.valueAt(displayNdx)).getWindowList();
                    numWindows = windows.size();
                    for (winNdx = 0; winNdx < numWindows; winNdx++) {
                        ws = (WindowState) windows.get(winNdx);
                        if (!this.mForceRemoves.contains(ws)) {
                            wsa = ws.mWinAnimator;
                            if (wsa.mSurfaceController != null) {
                                pidCandidates.append(wsa.mSession.mPid, wsa.mSession.mPid);
                            }
                        }
                    }
                    if (pidCandidates.size() > 0) {
                        int[] pids = new int[pidCandidates.size()];
                        for (int i = 0; i < pids.length; i++) {
                            pids[i] = pidCandidates.keyAt(i);
                        }
                        try {
                            if (this.mActivityManager.killPids(pids, "Free memory", secure)) {
                                killedApps = true;
                            }
                        } catch (RemoteException e) {
                        }
                    }
                }
            }
            if (leakedSurface || killedApps) {
                Slog.w("WindowManager", "Looks like we have reclaimed some memory, clearing surface for retry.");
                if (surfaceController != null) {
                    if (WindowManagerDebugConfig.SHOW_TRANSACTIONS || WindowManagerDebugConfig.SHOW_SURFACE_ALLOC) {
                        logSurface(winAnimator.mWin, "RECOVER DESTROY", false);
                    }
                    winAnimator.destroySurface();
                    scheduleRemoveStartingWindowLocked(winAnimator.mWin.mAppToken);
                }
                try {
                    winAnimator.mWin.mClient.dispatchGetNewSurface();
                } catch (RemoteException e2) {
                }
            }
            Binder.restoreCallingIdentity(callingIdentity);
            return !leakedSurface ? killedApps : true;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(callingIdentity);
        }
    }

    boolean updateFocusedWindowLocked(int mode, boolean updateInputWindows) {
        WindowState newFocus = computeFocusedWindowLocked();
        if (this.mCurrentFocus == newFocus) {
            return false;
        }
        Trace.traceBegin(32, "wmUpdateFocus");
        this.mH.removeMessages(2);
        this.mH.sendEmptyMessage(2);
        DisplayContent displayContent = getDefaultDisplayContentLocked();
        boolean z = mode != 1 ? mode != 3 : false;
        boolean imWindowChanged = moveInputMethodWindowsIfNeededLocked(z);
        if (imWindowChanged) {
            displayContent.layoutNeeded = true;
            newFocus = computeFocusedWindowLocked();
        }
        if (WindowManagerDebugConfig.DEBUG_FOCUS_LIGHT || localLOGV) {
            Slog.v("WindowManager", "Changing focus from " + this.mCurrentFocus + " to " + newFocus + " Callers=" + Debug.getCallers(4));
        } else {
            Slog.v(TAG, "Changing focus from " + this.mCurrentFocus + " to " + newFocus);
        }
        WindowState oldFocus = this.mCurrentFocus;
        this.mCurrentFocus = newFocus;
        this.mLosingFocus.remove(newFocus);
        if (newFocus == null || (newFocus.getAttrs().isDisableStatusBar != 1 && (newFocus.getAttrs().memoryType & DumpState.DUMP_DEXOPT) == 0)) {
            if (this.mDisableStatusBar) {
                this.mDisableStatusBar = false;
                disableStatusBar(false);
            }
        } else if (!this.mDisableStatusBar) {
            disableStatusBar(true);
            this.mDisableStatusBar = true;
        }
        int focusChanged = this.mPolicy.focusChangedLw(oldFocus, newFocus);
        if (imWindowChanged && oldFocus != this.mInputMethodWindow) {
            if (mode == 2) {
                this.mWindowPlacerLocked.performLayoutLockedInner(displayContent, true, updateInputWindows);
                focusChanged &= -2;
            } else if (mode == 3) {
                this.mLayersController.assignLayersLocked(displayContent.getWindowList());
            }
        }
        if ((focusChanged & 1) != 0) {
            displayContent.layoutNeeded = true;
            if (mode == 2) {
                this.mWindowPlacerLocked.performLayoutLockedInner(displayContent, true, updateInputWindows);
            }
        }
        if (mode != 1) {
            this.mInputMonitor.setInputFocusLw(this.mCurrentFocus, updateInputWindows);
        }
        adjustForImeIfNeeded(displayContent);
        boolean appInWhiteList = false;
        boolean nonSystemAppBeforeNougat = false;
        if (oldFocus != null) {
            ApplicationInfo aInfo = getApplicationInfo(oldFocus.getAttrs().packageName, oldFocus.mOwnerUid);
            appInWhiteList = aInfo != null ? (aInfo.privateFlags & DumpState.DUMP_INSTALLS) != 0 : false;
            nonSystemAppBeforeNougat = (aInfo == null || aInfo.targetSdkVersion >= 24) ? false : !aInfo.isSystemApp();
        }
        if (appInWhiteList) {
            Slog.w("WindowManager", "Skip toast check for application in whitelist.");
        } else if (nonSystemAppBeforeNougat) {
            Slog.w("WindowManager", "Skip focus check for application before nougat.");
        } else {
            getDefaultDisplayContentLocked().scheduleToastWindowsTimeoutIfNeededLocked(oldFocus, newFocus);
        }
        Trace.traceEnd(32);
        return true;
    }

    private WindowState computeFocusedWindowLocked() {
        int displayCount = this.mDisplayContents.size();
        for (int i = 0; i < displayCount; i++) {
            WindowState win = findFocusedWindowLocked((DisplayContent) this.mDisplayContents.valueAt(i));
            if (win != null) {
                return win;
            }
        }
        return null;
    }

    WindowState findFocusedWindowLocked(DisplayContent displayContent) {
        WindowList windows = displayContent.getWindowList();
        for (int i = windows.size() - 1; i >= 0; i--) {
            WindowState win = (WindowState) windows.get(i);
            if (localLOGV || WindowManagerDebugConfig.DEBUG_FOCUS) {
                Slog.v("WindowManager", "Looking for focus: " + i + " = " + win + ", flags=" + win.mAttrs.flags + ", canReceive=" + win.canReceiveKeys());
            }
            if (win.canReceiveKeys()) {
                if (MultiWindowManager.isSupported() && isStickyByMtk(win) && this.mFocusedApp != win.mAppToken) {
                    Slog.v(TAG, "[BMW] Skipping " + win.mAppToken + " because it belongs to stick stack");
                } else {
                    AppWindowToken wtoken = win.mAppToken;
                    if (wtoken == null || !(wtoken.removed || wtoken.sendingToBottom)) {
                        if (wtoken != null && win.mAttrs.type != 3 && this.mFocusedApp != null) {
                            ArrayList<Task> tasks = displayContent.getTasks();
                            for (int taskNdx = tasks.size() - 1; taskNdx >= 0; taskNdx--) {
                                AppTokenList tokens = ((Task) tasks.get(taskNdx)).mAppTokens;
                                int tokenNdx = tokens.size() - 1;
                                while (tokenNdx >= 0) {
                                    AppWindowToken token = (AppWindowToken) tokens.get(tokenNdx);
                                    if (wtoken == token) {
                                        break;
                                    } else if (this.mFocusedApp == token && token.windowsAreFocusable()) {
                                        if (localLOGV || WindowManagerDebugConfig.DEBUG_FOCUS_LIGHT) {
                                            Slog.v("WindowManager", "findFocusedWindow: Reached focused app=" + this.mFocusedApp + " target=" + wtoken);
                                        }
                                        return null;
                                    } else {
                                        tokenNdx--;
                                    }
                                }
                                if (tokenNdx >= 0) {
                                    break;
                                }
                            }
                        }
                        if (WindowManagerDebugConfig.DEBUG_FOCUS) {
                            Slog.v("WindowManager", "findFocusedWindow: Found new focus @ " + i + " = " + win);
                        }
                        return win;
                    } else if (WindowManagerDebugConfig.DEBUG_FOCUS) {
                        Slog.v("WindowManager", "Skipping " + wtoken + " because " + (wtoken.removed ? "removed" : "sendingToBottom"));
                    }
                }
            }
        }
        if (WindowManagerDebugConfig.DEBUG_FOCUS_LIGHT) {
            Slog.v("WindowManager", "findFocusedWindow: No focusable windows.");
        }
        return null;
    }

    private void startFreezingDisplayLocked(boolean inTransaction, int exitAnim, int enterAnim) {
        if (!this.mDisplayFrozen && this.mDisplayReady && this.mPolicy.isScreenOn()) {
            if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                Slog.d("WindowManager", "startFreezingDisplayLocked: inTransaction=" + inTransaction + " exitAnim=" + exitAnim + " enterAnim=" + enterAnim + " called by " + Debug.getCallers(8));
            }
            this.mScreenFrozenLock.acquire();
            this.mDisplayFrozen = true;
            this.mDisplayFreezeTime = SystemClock.elapsedRealtime();
            this.mLastFinishedFreezeSource = null;
            this.mInputMonitor.freezeInputDispatchingLw();
            this.mPolicy.setLastInputMethodWindowLw(null, null);
            if (this.mAppTransition.isTransitionSet()) {
                this.mAppTransition.freeze();
            }
            boolean agingTest = LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(SystemProperties.get("SPECIAL_OPPO_CONFIG"));
            if (PROFILE_ORIENTATION && !agingTest) {
                Debug.startMethodTracing(new File("/data/system/frozen").toString(), 8388608);
            }
            this.mExitAnimId = exitAnim;
            this.mEnterAnimId = enterAnim;
            DisplayContent displayContent = getDefaultDisplayContentLocked();
            int displayId = displayContent.getDisplayId();
            ScreenRotationAnimation screenRotationAnimation = this.mAnimator.getScreenRotationAnimationLocked(displayId);
            if (screenRotationAnimation != null) {
                screenRotationAnimation.kill();
            }
            boolean isSecure = false;
            WindowList windows = getDefaultWindowListLocked();
            int N = windows.size();
            for (int i = 0; i < N; i++) {
                WindowState ws = (WindowState) windows.get(i);
                if (ws.isOnScreen() && (ws.mAttrs.flags & DumpState.DUMP_PREFERRED_XML) != 0) {
                    isSecure = true;
                    break;
                }
            }
            displayContent.updateDisplayInfo();
            this.mAnimator.setScreenRotationAnimationLocked(displayId, new ScreenRotationAnimation(this.mContext, displayContent, this.mFxSession, inTransaction, this.mPolicy.isDefaultOrientationForced(), isSecure));
        }
    }

    void stopFreezingDisplayLocked() {
        if (!this.mDisplayFrozen) {
            return;
        }
        if ((this.mWaitingForConfig || this.mAppsFreezingScreen > 0 || this.mWindowsFreezingScreen == 1 || this.mClientFreezingScreen || !this.mOpeningApps.isEmpty()) && !this.mDisableFrozenBySecuritypermission) {
            if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                Slog.d("WindowManager", "stopFreezingDisplayLocked: Returning mWaitingForConfig=" + this.mWaitingForConfig + ", mAppsFreezingScreen=" + this.mAppsFreezingScreen + ", mWindowsFreezingScreen=" + this.mWindowsFreezingScreen + ", mClientFreezingScreen=" + this.mClientFreezingScreen + ", mOpeningApps.size()=" + this.mOpeningApps.size());
            }
            return;
        }
        if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
            Slog.d("WindowManager", "stopFreezingDisplayLocked: Unfreezing now");
        }
        this.mDisplayFrozen = false;
        this.mLastDisplayFreezeDuration = (int) (SystemClock.elapsedRealtime() - this.mDisplayFreezeTime);
        StringBuilder stringBuilder = new StringBuilder(128);
        stringBuilder.append("Screen frozen for ");
        TimeUtils.formatDuration((long) this.mLastDisplayFreezeDuration, stringBuilder);
        if (this.mLastFinishedFreezeSource != null) {
            stringBuilder.append(" due to ");
            stringBuilder.append(this.mLastFinishedFreezeSource);
        }
        Slog.i("WindowManager", stringBuilder.toString());
        try {
            ActivityManagerNative.getDefault().reportJunkFromApp("WINDOW_FREEZE", "N/A", stringBuilder.toString(), false);
        } catch (RemoteException e) {
            Slog.e(TAG, e.toString());
        }
        this.mH.removeMessages(17);
        this.mH.removeMessages(30);
        boolean agingTest = LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(SystemProperties.get("SPECIAL_OPPO_CONFIG"));
        if (PROFILE_ORIENTATION && !agingTest) {
            Debug.stopMethodTracing();
        }
        boolean updateRotation = false;
        DisplayContent displayContent = getDefaultDisplayContentLocked();
        int displayId = displayContent.getDisplayId();
        ScreenRotationAnimation screenRotationAnimation = this.mAnimator.getScreenRotationAnimationLocked(displayId);
        if (screenRotationAnimation == null || !screenRotationAnimation.hasScreenshot()) {
            if (screenRotationAnimation != null) {
                screenRotationAnimation.kill();
                this.mAnimator.setScreenRotationAnimationLocked(displayId, null);
            }
            updateRotation = true;
        } else {
            if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                Slog.i("WindowManager", "**** Dismissing screen rotation animation");
            }
            DisplayInfo displayInfo = displayContent.getDisplayInfo();
            if (!this.mPolicy.validateRotationAnimationLw(this.mExitAnimId, this.mEnterAnimId, displayContent.isDimming())) {
                this.mEnterAnimId = 0;
                this.mExitAnimId = 0;
            }
            if (screenRotationAnimation.dismiss(this.mFxSession, 10000, getTransitionAnimationScaleLocked(), displayInfo.logicalWidth, displayInfo.logicalHeight, this.mExitAnimId, this.mEnterAnimId)) {
                scheduleAnimationLocked();
            } else {
                screenRotationAnimation.kill();
                this.mAnimator.setScreenRotationAnimationLocked(displayId, null);
                updateRotation = true;
            }
        }
        this.mInputMonitor.thawInputDispatchingLw();
        if (!"0".equals(SystemProperties.get(APP_FROZEN_TIMEOUT_PROP))) {
            SystemProperties.set(APP_FROZEN_TIMEOUT_PROP, "0");
        }
        boolean configChanged = updateOrientationFromAppTokensLocked(false);
        this.mH.removeMessages(15);
        this.mH.sendEmptyMessageDelayed(15, 2000);
        this.mScreenFrozenLock.release();
        if (updateRotation) {
            if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                Slog.d("WindowManager", "Performing post-rotate rotation");
            }
            configChanged |= updateRotationUncheckedLocked(false);
        }
        if (configChanged) {
            this.mH.sendEmptyMessage(18);
        }
    }

    static int getPropertyInt(String[] tokens, int index, int defUnits, int defDps, DisplayMetrics dm) {
        if (index < tokens.length) {
            String str = tokens[index];
            if (str != null && str.length() > 0) {
                try {
                    return Integer.parseInt(str);
                } catch (Exception e) {
                }
            }
        }
        if (defUnits == 0) {
            return defDps;
        }
        return (int) TypedValue.applyDimension(defUnits, (float) defDps, dm);
    }

    /* JADX WARNING: Removed duplicated region for block: B:40:0x006a  */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x0064 A:{SYNTHETIC, Splitter: B:37:0x0064} */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x0059  */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x0053 A:{SYNTHETIC, Splitter: B:28:0x0053} */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x007b  */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x0075 A:{SYNTHETIC, Splitter: B:46:0x0075} */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x0064 A:{SYNTHETIC, Splitter: B:37:0x0064} */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x006a  */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x0053 A:{SYNTHETIC, Splitter: B:28:0x0053} */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x0059  */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x0075 A:{SYNTHETIC, Splitter: B:46:0x0075} */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x007b  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void createWatermarkInTransaction() {
        Throwable th;
        if (this.mWatermark == null) {
            FileInputStream in = null;
            DataInputStream ind = null;
            try {
                FileInputStream in2 = new FileInputStream(new File("/system/etc/setup.conf"));
                try {
                    DataInputStream ind2 = new DataInputStream(in2);
                    try {
                        String line = ind2.readLine();
                        if (line != null) {
                            String[] toks = line.split("%");
                            if (toks != null && toks.length > 0) {
                                this.mWatermark = new Watermark(getDefaultDisplayContentLocked().getDisplay(), this.mRealDisplayMetrics, this.mFxSession, toks);
                            }
                        }
                        if (ind2 != null) {
                            try {
                                ind2.close();
                            } catch (IOException e) {
                            }
                        } else if (in2 != null) {
                            try {
                                in2.close();
                            } catch (IOException e2) {
                            }
                        }
                        in = in2;
                    } catch (FileNotFoundException e3) {
                        ind = ind2;
                        in = in2;
                        if (ind != null) {
                        }
                    } catch (IOException e4) {
                        ind = ind2;
                        in = in2;
                        if (ind != null) {
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        ind = ind2;
                        in = in2;
                        if (ind != null) {
                        }
                        throw th;
                    }
                } catch (FileNotFoundException e5) {
                    in = in2;
                    if (ind != null) {
                    }
                } catch (IOException e6) {
                    in = in2;
                    if (ind != null) {
                    }
                } catch (Throwable th3) {
                    th = th3;
                    in = in2;
                    if (ind != null) {
                    }
                    throw th;
                }
            } catch (FileNotFoundException e7) {
                if (ind != null) {
                    try {
                        ind.close();
                    } catch (IOException e8) {
                    }
                } else if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e9) {
                    }
                }
            } catch (IOException e10) {
                if (ind != null) {
                    try {
                        ind.close();
                    } catch (IOException e11) {
                    }
                } else if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e12) {
                    }
                }
            } catch (Throwable th4) {
                th = th4;
                if (ind != null) {
                    try {
                        ind.close();
                    } catch (IOException e13) {
                    }
                } else if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e14) {
                    }
                }
                throw th;
            }
        }
    }

    public void setRecentsVisibility(boolean visible) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.STATUS_BAR") != 0) {
            throw new SecurityException("Caller does not hold permission android.permission.STATUS_BAR");
        }
        synchronized (this.mWindowMap) {
            this.mPolicy.setRecentsVisibilityLw(visible);
        }
    }

    public void setTvPipVisibility(boolean visible) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.STATUS_BAR") != 0) {
            throw new SecurityException("Caller does not hold permission android.permission.STATUS_BAR");
        }
        synchronized (this.mWindowMap) {
            this.mPolicy.setTvPipVisibilityLw(visible);
        }
    }

    public void statusBarVisibilityChanged(int visibility) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.STATUS_BAR") != 0) {
            throw new SecurityException("Caller does not hold permission android.permission.STATUS_BAR");
        }
        synchronized (this.mWindowMap) {
            this.mLastStatusBarVisibility = visibility;
            updateStatusBarVisibilityLocked(this.mPolicy.adjustSystemUiVisibilityLw(visibility));
        }
    }

    boolean updateStatusBarVisibilityLocked(int visibility) {
        if (this.mLastDispatchedSystemUiVisibility == visibility) {
            return false;
        }
        int globalDiff = ((this.mLastDispatchedSystemUiVisibility ^ visibility) & 7) & (~visibility);
        this.mLastDispatchedSystemUiVisibility = visibility;
        this.mInputManager.setSystemUiVisibility(visibility);
        WindowList windows = getDefaultWindowListLocked();
        int N = windows.size();
        int i = 0;
        while (i < N) {
            WindowState ws = (WindowState) windows.get(i);
            try {
                int curValue = ws.mSystemUiVisibility;
                int diff = (curValue ^ visibility) & globalDiff;
                int newValue = ((~diff) & curValue) | (visibility & diff);
                if (newValue != curValue) {
                    ws.mSeq++;
                    ws.mSystemUiVisibility = newValue;
                }
                if (newValue != curValue || ws.mAttrs.hasSystemUiListeners) {
                    ws.mClient.dispatchSystemUiVisibilityChanged(ws.mSeq, visibility, newValue, diff);
                    i++;
                } else {
                    i++;
                }
            } catch (RemoteException e) {
            }
        }
        return true;
    }

    public void reevaluateStatusBarVisibility() {
        synchronized (this.mWindowMap) {
            if (updateStatusBarVisibilityLocked(this.mPolicy.adjustSystemUiVisibilityLw(this.mLastStatusBarVisibility))) {
                this.mWindowPlacerLocked.requestTraversal();
            }
        }
    }

    public InputConsumer addInputConsumer(Looper looper, Factory inputEventReceiverFactory) {
        HideNavInputConsumer inputConsumerImpl;
        synchronized (this.mWindowMap) {
            inputConsumerImpl = new HideNavInputConsumer(this, looper, inputEventReceiverFactory);
            this.mInputConsumer = inputConsumerImpl;
            this.mInputMonitor.updateInputWindowsLw(true);
        }
        return inputConsumerImpl;
    }

    boolean removeInputConsumer() {
        synchronized (this.mWindowMap) {
            if (this.mInputConsumer != null) {
                this.mInputConsumer = null;
                this.mInputMonitor.updateInputWindowsLw(true);
                return true;
            }
            return false;
        }
    }

    public void createWallpaperInputConsumer(InputChannel inputChannel) {
        synchronized (this.mWindowMap) {
            this.mWallpaperInputConsumer = new InputConsumerImpl(this, "wallpaper input", inputChannel);
            this.mWallpaperInputConsumer.mWindowHandle.hasWallpaper = true;
            this.mInputMonitor.updateInputWindowsLw(true);
        }
    }

    public void removeWallpaperInputConsumer() {
        synchronized (this.mWindowMap) {
            if (this.mWallpaperInputConsumer != null) {
                this.mWallpaperInputConsumer.disposeChannelsLw();
                this.mWallpaperInputConsumer = null;
                this.mInputMonitor.updateInputWindowsLw(true);
            }
        }
    }

    public boolean hasNavigationBar() {
        return this.mPolicy.hasNavigationBar();
    }

    public void lockNow(Bundle options) {
        this.mPolicy.lockNow(options);
    }

    public void showRecentApps(boolean fromHome) {
        this.mPolicy.showRecentApps(fromHome);
    }

    public boolean isSafeModeEnabled() {
        return this.mSafeMode;
    }

    public boolean clearWindowContentFrameStats(IBinder token) {
        if (checkCallingPermission("android.permission.FRAME_STATS", "clearWindowContentFrameStats()")) {
            synchronized (this.mWindowMap) {
                WindowState windowState = (WindowState) this.mWindowMap.get(token);
                if (windowState == null) {
                    return false;
                }
                WindowSurfaceController surfaceController = windowState.mWinAnimator.mSurfaceController;
                if (surfaceController == null) {
                    return false;
                }
                boolean clearWindowContentFrameStats = surfaceController.clearWindowContentFrameStats();
                return clearWindowContentFrameStats;
            }
        }
        throw new SecurityException("Requires FRAME_STATS permission");
    }

    public WindowContentFrameStats getWindowContentFrameStats(IBinder token) {
        if (checkCallingPermission("android.permission.FRAME_STATS", "getWindowContentFrameStats()")) {
            synchronized (this.mWindowMap) {
                WindowState windowState = (WindowState) this.mWindowMap.get(token);
                if (windowState == null) {
                    return null;
                }
                WindowSurfaceController surfaceController = windowState.mWinAnimator.mSurfaceController;
                if (surfaceController == null) {
                    return null;
                }
                if (this.mTempWindowRenderStats == null) {
                    this.mTempWindowRenderStats = new WindowContentFrameStats();
                }
                WindowContentFrameStats stats = this.mTempWindowRenderStats;
                if (surfaceController.getWindowContentFrameStats(stats)) {
                    return stats;
                }
                return null;
            }
        }
        throw new SecurityException("Requires FRAME_STATS permission");
    }

    public void notifyAppRelaunching(IBinder token) {
        synchronized (this.mWindowMap) {
            AppWindowToken appWindow = findAppWindowToken(token);
            if (appWindow != null) {
                appWindow.startRelaunching();
            }
        }
    }

    public void notifyAppRelaunchingFinished(IBinder token) {
        synchronized (this.mWindowMap) {
            AppWindowToken appWindow = findAppWindowToken(token);
            if (appWindow != null) {
                appWindow.finishRelaunching();
            }
        }
    }

    public void notifyAppRelaunchesCleared(IBinder token) {
        synchronized (this.mWindowMap) {
            AppWindowToken appWindow = findAppWindowToken(token);
            if (appWindow != null) {
                appWindow.clearRelaunching();
            }
        }
    }

    public int getDockedDividerInsetsLw() {
        return getDefaultDisplayContentLocked().getDockedDividerController().getContentInsets();
    }

    void dumpPolicyLocked(PrintWriter pw, String[] args, boolean dumpAll) {
        pw.println("WINDOW MANAGER POLICY STATE (dumpsys window policy)");
        this.mPolicy.dump("    ", pw, args);
    }

    void dumpAnimatorLocked(PrintWriter pw, String[] args, boolean dumpAll) {
        pw.println("WINDOW MANAGER ANIMATOR STATE (dumpsys window animator)");
        this.mAnimator.dumpLocked(pw, "    ", dumpAll);
    }

    void dumpTokensLocked(PrintWriter pw, boolean dumpAll) {
        WindowToken token;
        pw.println("WINDOW MANAGER TOKENS (dumpsys window tokens)");
        if (!this.mTokenMap.isEmpty()) {
            pw.println("  All tokens:");
            for (WindowToken token2 : this.mTokenMap.values()) {
                pw.print("  ");
                pw.print(token2);
                if (dumpAll) {
                    pw.println(':');
                    token2.dump(pw, "    ");
                } else {
                    pw.println();
                }
            }
        }
        this.mWallpaperControllerLocked.dumpTokens(pw, "  ", dumpAll);
        if (!this.mFinishedStarting.isEmpty()) {
            pw.println();
            pw.println("  Finishing start of application tokens:");
            for (int i = this.mFinishedStarting.size() - 1; i >= 0; i--) {
                token2 = (WindowToken) this.mFinishedStarting.get(i);
                pw.print("  Finished Starting #");
                pw.print(i);
                pw.print(' ');
                pw.print(token2);
                if (dumpAll) {
                    pw.println(':');
                    token2.dump(pw, "    ");
                } else {
                    pw.println();
                }
            }
        }
        if (!this.mOpeningApps.isEmpty() || !this.mClosingApps.isEmpty()) {
            pw.println();
            if (this.mOpeningApps.size() > 0) {
                pw.print("  mOpeningApps=");
                pw.println(this.mOpeningApps);
            }
            if (this.mClosingApps.size() > 0) {
                pw.print("  mClosingApps=");
                pw.println(this.mClosingApps);
            }
        }
    }

    void dumpSessionsLocked(PrintWriter pw, boolean dumpAll) {
        pw.println("WINDOW MANAGER SESSIONS (dumpsys window sessions)");
        for (int i = 0; i < this.mSessions.size(); i++) {
            Session s = (Session) this.mSessions.valueAt(i);
            pw.print("  Session ");
            pw.print(s);
            pw.println(':');
            s.dump(pw, "    ");
        }
    }

    void dumpDisplayContentsLocked(PrintWriter pw, boolean dumpAll) {
        pw.println("WINDOW MANAGER DISPLAY CONTENTS (dumpsys window displays)");
        if (this.mDisplayReady) {
            int numDisplays = this.mDisplayContents.size();
            for (int displayNdx = 0; displayNdx < numDisplays; displayNdx++) {
                ((DisplayContent) this.mDisplayContents.valueAt(displayNdx)).dump("  ", pw);
            }
            return;
        }
        pw.println("  NO DISPLAY");
    }

    void dumpWindowsLocked(PrintWriter pw, boolean dumpAll, ArrayList<WindowState> windows) {
        pw.println("WINDOW MANAGER WINDOWS (dumpsys window windows)");
        dumpWindowsNoHeaderLocked(pw, dumpAll, windows);
    }

    void dumpWindowsNoHeaderLocked(PrintWriter pw, boolean dumpAll, ArrayList<WindowState> windows) {
        int displayNdx;
        WindowState w;
        int i;
        int numDisplays = this.mDisplayContents.size();
        for (displayNdx = 0; displayNdx < numDisplays; displayNdx++) {
            WindowList windowList = ((DisplayContent) this.mDisplayContents.valueAt(displayNdx)).getWindowList();
            for (int winNdx = windowList.size() - 1; winNdx >= 0; winNdx--) {
                w = (WindowState) windowList.get(winNdx);
                if (windows == null || windows.contains(w)) {
                    boolean z;
                    pw.print("  Window #");
                    pw.print(winNdx);
                    pw.print(' ');
                    pw.print(w);
                    pw.println(":");
                    String str = "    ";
                    if (dumpAll || windows != null) {
                        z = true;
                    } else {
                        z = false;
                    }
                    w.dump(pw, str, z);
                }
            }
        }
        if (this.mInputMethodDialogs.size() > 0) {
            pw.println();
            pw.println("  Input method dialogs:");
            for (i = this.mInputMethodDialogs.size() - 1; i >= 0; i--) {
                w = (WindowState) this.mInputMethodDialogs.get(i);
                if (windows == null || windows.contains(w)) {
                    pw.print("  IM Dialog #");
                    pw.print(i);
                    pw.print(": ");
                    pw.println(w);
                }
            }
        }
        if (this.mPendingRemove.size() > 0) {
            pw.println();
            pw.println("  Remove pending for:");
            for (i = this.mPendingRemove.size() - 1; i >= 0; i--) {
                w = (WindowState) this.mPendingRemove.get(i);
                if (windows == null || windows.contains(w)) {
                    pw.print("  Remove #");
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
        }
        if (this.mForceRemoves != null && this.mForceRemoves.size() > 0) {
            pw.println();
            pw.println("  Windows force removing:");
            for (i = this.mForceRemoves.size() - 1; i >= 0; i--) {
                w = (WindowState) this.mForceRemoves.get(i);
                pw.print("  Removing #");
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
        if (this.mDestroySurface.size() > 0) {
            pw.println();
            pw.println("  Windows waiting to destroy their surface:");
            for (i = this.mDestroySurface.size() - 1; i >= 0; i--) {
                w = (WindowState) this.mDestroySurface.get(i);
                if (windows == null || windows.contains(w)) {
                    pw.print("  Destroy #");
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
        }
        if (this.mLosingFocus.size() > 0) {
            pw.println();
            pw.println("  Windows losing focus:");
            for (i = this.mLosingFocus.size() - 1; i >= 0; i--) {
                w = (WindowState) this.mLosingFocus.get(i);
                if (windows == null || windows.contains(w)) {
                    pw.print("  Losing #");
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
        }
        if (this.mResizingWindows.size() > 0) {
            pw.println();
            pw.println("  Windows waiting to resize:");
            for (i = this.mResizingWindows.size() - 1; i >= 0; i--) {
                w = (WindowState) this.mResizingWindows.get(i);
                if (windows == null || windows.contains(w)) {
                    pw.print("  Resizing #");
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
        }
        if (this.mWaitingForDrawn.size() > 0) {
            pw.println();
            pw.println("  Clients waiting for these windows to be drawn:");
            for (i = this.mWaitingForDrawn.size() - 1; i >= 0; i--) {
                WindowState win = (WindowState) this.mWaitingForDrawn.get(i);
                pw.print("  Waiting #");
                pw.print(i);
                pw.print(' ');
                pw.print(win);
            }
        }
        pw.println();
        pw.print("  mCurConfiguration=");
        pw.println(this.mCurConfiguration);
        pw.print("  mHasPermanentDpad=");
        pw.println(this.mHasPermanentDpad);
        pw.print("  mCurrentFocus=");
        pw.println(this.mCurrentFocus);
        if (this.mLastFocus != this.mCurrentFocus) {
            pw.print("  mLastFocus=");
            pw.println(this.mLastFocus);
        }
        pw.print("  mFocusedApp=");
        pw.println(this.mFocusedApp);
        if (this.mInputMethodTarget != null) {
            pw.print("  mInputMethodTarget=");
            pw.println(this.mInputMethodTarget);
        }
        pw.print("  mInTouchMode=");
        pw.print(this.mInTouchMode);
        pw.print(" mLayoutSeq=");
        pw.println(this.mLayoutSeq);
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
        this.mInputMonitor.dump(pw, "  ");
        if (dumpAll) {
            pw.print("  mSystemDecorLayer=");
            pw.print(this.mSystemDecorLayer);
            pw.print(" mScreenRect=");
            pw.println(this.mScreenRect.toShortString());
            if (this.mLastStatusBarVisibility != 0) {
                pw.print("  mLastStatusBarVisibility=0x");
                pw.println(Integer.toHexString(this.mLastStatusBarVisibility));
            }
            if (this.mInputMethodWindow != null) {
                pw.print("  mInputMethodWindow=");
                pw.println(this.mInputMethodWindow);
            }
            this.mWindowPlacerLocked.dump(pw, "  ");
            this.mWallpaperControllerLocked.dump(pw, "  ");
            this.mLayersController.dump(pw, "  ");
            pw.print("  mSystemBooted=");
            pw.print(this.mSystemBooted);
            pw.print(" mDisplayEnabled=");
            pw.println(this.mDisplayEnabled);
            if (needsLayout()) {
                pw.print("  layoutNeeded on displays=");
                for (displayNdx = 0; displayNdx < numDisplays; displayNdx++) {
                    DisplayContent displayContent = (DisplayContent) this.mDisplayContents.valueAt(displayNdx);
                    if (displayContent.layoutNeeded) {
                        pw.print(displayContent.getDisplayId());
                    }
                }
                pw.println();
            }
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
            pw.print(" waitingForConfig=");
            pw.println(this.mWaitingForConfig);
            pw.print("  mRotation=");
            pw.print(this.mRotation);
            pw.print(" mAltOrientation=");
            pw.println(this.mAltOrientation);
            pw.print("  mLastWindowForcedOrientation=");
            pw.print(this.mLastWindowForcedOrientation);
            pw.print(" mLastOrientation=");
            pw.println(this.mLastOrientation);
            pw.print("  mDeferredRotationPauseCount=");
            pw.println(this.mDeferredRotationPauseCount);
            pw.print("  Animation settings: disabled=");
            pw.print(this.mAnimationsDisabled);
            pw.print(" window=");
            pw.print(this.mWindowAnimationScaleSetting);
            pw.print(" transition=");
            pw.print(this.mTransitionAnimationScaleSetting);
            pw.print(" animator=");
            pw.println(this.mAnimatorDurationScaleSetting);
            pw.print(" mSkipAppTransitionAnimation=");
            pw.println(this.mSkipAppTransitionAnimation);
            pw.println("  mLayoutToAnim:");
            this.mAppTransition.dump(pw, "    ");
        }
    }

    boolean dumpWindows(PrintWriter pw, String name, String[] args, int opti, boolean dumpAll) {
        WindowList windows = new WindowList();
        HashMap hashMap;
        int numDisplays;
        int displayNdx;
        WindowList windowList;
        int winNdx;
        WindowState w;
        if ("apps".equals(name) || "visible".equals(name) || "visible-apps".equals(name)) {
            boolean appsOnly = name.contains("apps");
            boolean visibleOnly = name.contains("visible");
            hashMap = this.mWindowMap;
            synchronized (hashMap) {
                if (appsOnly) {
                    dumpDisplayContentsLocked(pw, true);
                }
                numDisplays = this.mDisplayContents.size();
                for (displayNdx = 0; displayNdx < numDisplays; displayNdx++) {
                    windowList = ((DisplayContent) this.mDisplayContents.valueAt(displayNdx)).getWindowList();
                    for (winNdx = windowList.size() - 1; winNdx >= 0; winNdx--) {
                        w = (WindowState) windowList.get(winNdx);
                        if ((!visibleOnly || w.mWinAnimator.getShown()) && !(appsOnly && w.mAppToken == null)) {
                            windows.add(w);
                        }
                    }
                }
            }
        } else {
            CharSequence name2;
            int objectId = 0;
            try {
                objectId = Integer.parseInt(name2, 16);
                name2 = null;
            } catch (RuntimeException e) {
            }
            hashMap = this.mWindowMap;
            synchronized (hashMap) {
                numDisplays = this.mDisplayContents.size();
                for (displayNdx = 0; displayNdx < numDisplays; displayNdx++) {
                    windowList = ((DisplayContent) this.mDisplayContents.valueAt(displayNdx)).getWindowList();
                    for (winNdx = windowList.size() - 1; winNdx >= 0; winNdx--) {
                        w = (WindowState) windowList.get(winNdx);
                        if (name2 != null) {
                            if (w.mAttrs.getTitle().toString().contains(name2)) {
                                windows.add(w);
                            }
                        } else if (System.identityHashCode(w) == objectId) {
                            windows.add(w);
                        }
                    }
                }
            }
        }
        if (windows.size() <= 0) {
            return false;
        }
        synchronized (this.mWindowMap) {
            dumpWindowsLocked(pw, dumpAll, windows);
        }
        return true;
    }

    void dumpLastANRLocked(PrintWriter pw) {
        pw.println("WINDOW MANAGER LAST ANR (dumpsys window lastanr)");
        if (this.mLastANRState == null) {
            pw.println("  <no ANR has occurred since boot>");
        } else {
            pw.println(this.mLastANRState);
        }
    }

    public void saveANRStateLocked(AppWindowToken appWindowToken, WindowState windowState, String reason) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new FastPrintWriter(sw, false, 1024);
        pw.println("  ANR time: " + DateFormat.getInstance().format(new Date()));
        if (appWindowToken != null) {
            pw.println("  Application at fault: " + appWindowToken.stringName);
        }
        if (windowState != null) {
            pw.println("  Window at fault: " + windowState.mAttrs.getTitle());
        }
        if (reason != null) {
            pw.println("  Reason: " + reason);
        }
        pw.println();
        dumpWindowsNoHeaderLocked(pw, true, null);
        pw.println();
        pw.println("Last ANR continued");
        dumpDisplayContentsLocked(pw, true);
        pw.close();
        this.mLastANRState = sw.toString();
        this.mH.removeMessages(38);
        this.mH.sendEmptyMessageDelayed(38, 7200000);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        String str = null;
        if (this.mContext.checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
            pw.println("Permission Denial: can't dump WindowManager from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
            return;
        }
        boolean dumpAll = false;
        int opti = 0;
        while (opti < args.length) {
            String opt = args[opti];
            if (opt == null || opt.length() <= 0 || opt.charAt(0) != '-') {
                break;
            }
            opti++;
            if ("-a".equals(opt)) {
                dumpAll = true;
            } else if ("-h".equals(opt)) {
                pw.println("Window manager dump options:");
                pw.println("  [-a] [-h] [cmd] ...");
                pw.println("  cmd may be one of:");
                pw.println("    i[input]: input subsystem state");
                pw.println("    p[policy]: policy state");
                pw.println("    s[essions]: active sessions");
                pw.println("    surfaces: active surfaces (debugging enabled only)");
                pw.println("    d[isplays]: active display contents");
                pw.println("    t[okens]: token list");
                pw.println("    w[indows]: window list");
                pw.println("  cmd may also be a NAME to dump windows.  NAME may");
                pw.println("    be a partial substring in a window name, a");
                pw.println("    Window hex object identifier, or");
                pw.println("    \"all\" for all windows, or");
                pw.println("    \"visible\" for the visible windows.");
                pw.println("    \"visible-apps\" for the visible app windows.");
                pw.println("  -a: include all available server state.");
                return;
            } else if ("-d".equals(opt)) {
                runDebug(pw, args, opti);
                return;
            } else {
                pw.println("Unknown argument: " + opt + "; use -h for help");
            }
        }
        pw.println("Dump time : " + new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS").format(new Date()));
        if (opti < args.length) {
            String cmd = args[opti];
            opti++;
            if ("lastanr".equals(cmd) || "l".equals(cmd)) {
                synchronized (this.mWindowMap) {
                    dumpLastANRLocked(pw);
                }
                return;
            } else if ("policy".equals(cmd) || OppoCrashClearManager.CRASH_CLEAR_NAME.equals(cmd)) {
                synchronized (this.mWindowMap) {
                    dumpPolicyLocked(pw, args, true);
                }
                return;
            } else if ("animator".equals(cmd) || "a".equals(cmd)) {
                synchronized (this.mWindowMap) {
                    dumpAnimatorLocked(pw, args, true);
                }
                return;
            } else if ("sessions".equals(cmd) || "s".equals(cmd)) {
                synchronized (this.mWindowMap) {
                    dumpSessionsLocked(pw, true);
                }
                return;
            } else if ("surfaces".equals(cmd)) {
                synchronized (this.mWindowMap) {
                    SurfaceTrace.dumpAllSurfaces(pw, null);
                }
                return;
            } else if ("displays".equals(cmd) || "d".equals(cmd)) {
                synchronized (this.mWindowMap) {
                    dumpDisplayContentsLocked(pw, true);
                }
                return;
            } else if ("tokens".equals(cmd) || "t".equals(cmd)) {
                synchronized (this.mWindowMap) {
                    dumpTokensLocked(pw, true);
                }
                return;
            } else if ("windows".equals(cmd) || "w".equals(cmd)) {
                synchronized (this.mWindowMap) {
                    dumpWindowsLocked(pw, true, null);
                }
                return;
            } else if ("all".equals(cmd) || "a".equals(cmd)) {
                synchronized (this.mWindowMap) {
                    dumpWindowsLocked(pw, true, null);
                }
                return;
            } else if ("oppo-log".equals(cmd)) {
                dynamicallyConfigLogTag(pw, args, opti);
                return;
            } else if ("debug_switch".equals(cmd)) {
                dumpDynamicallyLogSwitch(pw, args, opti);
                return;
            } else if ("get_value".equals(cmd)) {
                dynamicGetValue(pw, args);
                return;
            } else {
                if (!dumpWindows(pw, cmd, args, opti, dumpAll)) {
                    pw.println("Bad window command, or no windows match: " + cmd);
                    pw.println("Use -h for help.");
                }
                return;
            }
        }
        synchronized (this.mWindowMap) {
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
                str = "-------------------------------------------------------------------------------";
            }
            SurfaceTrace.dumpAllSurfaces(pw, str);
            pw.println();
            if (dumpAll) {
                pw.println("-------------------------------------------------------------------------------");
            }
            dumpDisplayContentsLocked(pw, dumpAll);
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
        }
    }

    private void runDebug(PrintWriter pw, String[] args, int opti) {
        int mode;
        String cmd = "help";
        if (opti < args.length) {
            cmd = args[opti];
            opti++;
        }
        if ("help".equals(cmd)) {
            mode = 0;
            pw.println("Window manager debug options:");
            pw.println("  -d enable <zone zone ...> : enable the debug zone");
            pw.println("  -d disable <zone zone ...> : disable the debug zone");
            pw.println("zone may be some of:");
            pw.println("  a[all]");
        } else if ("enable".equals(cmd)) {
            mode = 1;
        } else if ("disable".equals(cmd)) {
            mode = 2;
        } else {
            pw.println("Unknown debug argument: " + cmd + "; use \"-d help\" for help");
            return;
        }
        boolean setAll = false;
        Field[] fields = WindowManagerDebugConfig.class.getDeclaredFields();
        Field[] fieldsPolicy = PhoneWindowManager.class.getDeclaredFields();
        while (!setAll && (mode == 0 || opti < args.length)) {
            if (opti < args.length) {
                cmd = args[opti];
                opti++;
            }
            setAll = (mode == 0 || "all".equals(cmd)) ? true : "a".equals(cmd);
            for (int i = 0; i < fields.length; i++) {
                String name = fields[i].getName();
                if (name != null && (name.contains("DEBUG") || name.contains("SHOW") || name.equals("localLOGV"))) {
                    if (!setAll) {
                        try {
                            if (!name.equals(cmd)) {
                            }
                        } catch (IllegalAccessException e) {
                            Slog.e(TAG, name + " setBoolean failed", e);
                        }
                    }
                    if (mode != 0) {
                        fields[i].setBoolean(null, mode == 1);
                        if (name.equals("localLOGV")) {
                            localLOGV = mode == 1;
                        }
                        int j = 0;
                        while (j < fieldsPolicy.length) {
                            if (fieldsPolicy[j].getName().equals(name)) {
                                fieldsPolicy[j].setAccessible(true);
                                fieldsPolicy[j].setBoolean(null, mode == 1);
                            } else {
                                j++;
                            }
                        }
                    }
                    Object[] objArr = new Object[2];
                    objArr[0] = name;
                    objArr[1] = Boolean.valueOf(fields[i].getBoolean(null));
                    pw.println(String.format("  %s = %b", objArr));
                }
            }
        }
    }

    public void monitor() {
        synchronized (this.mWindowMap) {
        }
    }

    private DisplayContent newDisplayContentLocked(Display display) {
        DisplayContent displayContent = new DisplayContent(display, this);
        int displayId = display.getDisplayId();
        if (WindowManagerDebugConfig.DEBUG_DISPLAY) {
            Slog.v("WindowManager", "Adding display=" + display);
        }
        this.mDisplayContents.put(displayId, displayContent);
        DisplayInfo displayInfo = displayContent.getDisplayInfo();
        Rect rect = new Rect();
        this.mDisplaySettings.getOverscanLocked(displayInfo.name, displayInfo.uniqueId, rect);
        displayInfo.overscanLeft = rect.left;
        displayInfo.overscanTop = rect.top;
        displayInfo.overscanRight = rect.right;
        displayInfo.overscanBottom = rect.bottom;
        this.mDisplayManagerInternal.setDisplayInfoOverrideFromWindowManager(displayId, displayInfo);
        configureDisplayPolicyLocked(displayContent);
        if (displayId == 0) {
            displayContent.mTapDetector = new TaskTapPointerEventListener(this, displayContent);
            registerPointerEventListener(displayContent.mTapDetector);
            registerPointerEventListener(this.mMousePositionTracker);
        }
        return displayContent;
    }

    public void createDisplayContentLocked(Display display) {
        if (display == null) {
            throw new IllegalArgumentException("getDisplayContent: display must not be null");
        }
        getDisplayContentLocked(display.getDisplayId());
    }

    public DisplayContent getDisplayContentLocked(int displayId) {
        DisplayContent displayContent = (DisplayContent) this.mDisplayContents.get(displayId);
        if (displayContent != null) {
            return displayContent;
        }
        Display display = this.mDisplayManager.getDisplay(displayId);
        if (display != null) {
            return newDisplayContentLocked(display);
        }
        return displayContent;
    }

    public DisplayContent getDefaultDisplayContentLocked() {
        return getDisplayContentLocked(0);
    }

    public WindowList getDefaultWindowListLocked() {
        return getDefaultDisplayContentLocked().getWindowList();
    }

    public DisplayInfo getDefaultDisplayInfoLocked() {
        return getDefaultDisplayContentLocked().getDisplayInfo();
    }

    public WindowList getWindowListLocked(Display display) {
        return getWindowListLocked(display.getDisplayId());
    }

    public WindowList getWindowListLocked(int displayId) {
        DisplayContent displayContent = getDisplayContentLocked(displayId);
        if (displayContent != null) {
            return displayContent.getWindowList();
        }
        return null;
    }

    public void onDisplayAdded(int displayId) {
        this.mH.sendMessage(this.mH.obtainMessage(27, displayId, 0));
    }

    public void handleDisplayAdded(int displayId) {
        synchronized (this.mWindowMap) {
            Display display = this.mDisplayManager.getDisplay(displayId);
            if (display != null) {
                createDisplayContentLocked(display);
                displayReady(displayId);
            }
            this.mWindowPlacerLocked.requestTraversal();
        }
    }

    public void onDisplayRemoved(int displayId) {
        Slog.v("WindowManager", "onDisplayRemoved id = " + displayId + " Callers=" + Debug.getCallers(3));
        this.mH.sendMessage(this.mH.obtainMessage(28, displayId, 0));
    }

    private void handleDisplayRemovedLocked(int displayId) {
        DisplayContent displayContent = getDisplayContentLocked(displayId);
        if (displayContent != null) {
            if (displayContent.isAnimating()) {
                displayContent.mDeferredRemoval = true;
                return;
            }
            if (WindowManagerDebugConfig.DEBUG_DISPLAY) {
                Slog.v("WindowManager", "Removing display=" + displayContent);
            }
            this.mDisplayContents.delete(displayId);
            displayContent.close();
            if (displayId == 0) {
                unregisterPointerEventListener(displayContent.mTapDetector);
                unregisterPointerEventListener(this.mMousePositionTracker);
            }
        }
        this.mAnimator.removeDisplayLocked(displayId);
        this.mWindowPlacerLocked.requestTraversal();
    }

    public void onDisplayChanged(int displayId) {
        this.mH.sendMessage(this.mH.obtainMessage(29, displayId, 0));
    }

    private void handleDisplayChangedLocked(int displayId) {
        DisplayContent displayContent = getDisplayContentLocked(displayId);
        if (displayContent != null) {
            displayContent.updateDisplayInfo();
        }
        this.mWindowPlacerLocked.requestTraversal();
    }

    public Object getWindowManagerLock() {
        return this.mWindowMap;
    }

    public void setReplacingWindow(IBinder token, boolean animate) {
        synchronized (this.mWindowMap) {
            AppWindowToken appWindowToken = findAppWindowToken(token);
            if (appWindowToken == null || !appWindowToken.isVisible()) {
                Slog.w("WindowManager", "Attempted to set replacing window on non-existing app token " + token);
                return;
            }
            appWindowToken.setReplacingWindows(animate);
        }
    }

    public void setReplacingWindows(IBinder token, boolean childrenOnly) {
        synchronized (this.mWindowMap) {
            AppWindowToken appWindowToken = findAppWindowToken(token);
            if (appWindowToken == null || !appWindowToken.isVisible()) {
                Slog.w("WindowManager", "Attempted to set replacing window on non-existing app token " + token);
                return;
            }
            if (childrenOnly) {
                appWindowToken.setReplacingChildren();
            } else {
                appWindowToken.setReplacingWindows(false);
            }
            scheduleClearReplacingWindowIfNeeded(token, true);
        }
    }

    /* JADX WARNING: Missing block: B:12:0x002c, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void scheduleClearReplacingWindowIfNeeded(IBinder token, boolean replacing) {
        synchronized (this.mWindowMap) {
            AppWindowToken appWindowToken = findAppWindowToken(token);
            if (appWindowToken == null) {
                Slog.w("WindowManager", "Attempted to reset replacing window on non-existing app token " + token);
            } else if (replacing) {
                scheduleReplacingWindowTimeouts(appWindowToken);
            } else {
                appWindowToken.resetReplacingWindows();
            }
        }
    }

    void scheduleReplacingWindowTimeouts(AppWindowToken appWindowToken) {
        if (!this.mReplacingWindowTimeouts.contains(appWindowToken)) {
            this.mReplacingWindowTimeouts.add(appWindowToken);
        }
        this.mH.removeMessages(46);
        this.mH.sendEmptyMessageDelayed(46, 2000);
    }

    public int getDockedStackSide() {
        int dockSide;
        synchronized (this.mWindowMap) {
            TaskStack dockedStack = getDefaultDisplayContentLocked().getDockedStackVisibleForUserLocked();
            dockSide = dockedStack == null ? -1 : dockedStack.getDockSide();
        }
        return dockSide;
    }

    public void setDockedStackResizing(boolean resizing) {
        synchronized (this.mWindowMap) {
            getDefaultDisplayContentLocked().getDockedDividerController().setResizing(resizing);
            requestTraversal();
        }
    }

    public void setDockedStackDividerTouchRegion(Rect touchRegion) {
        synchronized (this.mWindowMap) {
            getDefaultDisplayContentLocked().getDockedDividerController().setTouchRegion(touchRegion);
            setFocusTaskRegionLocked();
        }
    }

    public void setResizeDimLayer(boolean visible, int targetStackId, float alpha) {
        synchronized (this.mWindowMap) {
            getDefaultDisplayContentLocked().getDockedDividerController().setResizeDimLayer(visible, targetStackId, alpha);
        }
    }

    public void animateResizePinnedStack(Rect bounds, int animationDuration) {
        synchronized (this.mWindowMap) {
            final TaskStack stack = (TaskStack) this.mStackIdToStack.get(4);
            if (stack == null) {
                Slog.w(TAG, "animateResizePinnedStack: stackId 4 not found.");
                return;
            }
            final Rect originalBounds = new Rect();
            stack.getBounds(originalBounds);
            final Rect rect = bounds;
            final int i = animationDuration;
            UiThread.getHandler().post(new Runnable() {
                public void run() {
                    WindowManagerService.this.mBoundsAnimationController.animateBounds(stack, originalBounds, rect, i);
                }
            });
        }
    }

    public void setTaskResizeable(int taskId, int resizeMode) {
        synchronized (this.mWindowMap) {
            Task task = (Task) this.mTaskIdToTask.get(taskId);
            if (task != null) {
                task.setResizeable(resizeMode);
            }
        }
    }

    public void setForceResizableTasks(boolean forceResizableTasks) {
        synchronized (this.mWindowMap) {
            this.mForceResizableTasks = forceResizableTasks;
        }
    }

    static int dipToPixel(int dip, DisplayMetrics displayMetrics) {
        return (int) TypedValue.applyDimension(1, (float) dip, displayMetrics);
    }

    public void registerDockedStackListener(IDockedStackListener listener) {
        if (checkCallingPermission("android.permission.REGISTER_WINDOW_MANAGER_LISTENERS", "registerDockedStackListener()")) {
            getDefaultDisplayContentLocked().mDividerControllerLocked.registerDockedStackListener(listener);
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

    public void getStableInsets(Rect outInsets) throws RemoteException {
        synchronized (this.mWindowMap) {
            getStableInsetsLocked(outInsets);
        }
    }

    void getStableInsetsLocked(Rect outInsets) {
        DisplayInfo di = getDefaultDisplayInfoLocked();
        this.mPolicy.getStableInsetsLw(di.rotation, di.logicalWidth, di.logicalHeight, outInsets);
    }

    private void getNonDecorInsetsLocked(Rect outInsets) {
        DisplayInfo di = getDefaultDisplayInfoLocked();
        this.mPolicy.getNonDecorInsetsLw(di.rotation, di.logicalWidth, di.logicalHeight, outInsets);
    }

    public void subtractStableInsets(Rect inOutBounds) {
        synchronized (this.mWindowMap) {
            getStableInsetsLocked(this.mTmpRect2);
            DisplayInfo di = getDefaultDisplayInfoLocked();
            this.mTmpRect.set(0, 0, di.logicalWidth, di.logicalHeight);
            subtractInsets(this.mTmpRect, this.mTmpRect2, inOutBounds);
        }
    }

    public void subtractNonDecorInsets(Rect inOutBounds) {
        synchronized (this.mWindowMap) {
            getNonDecorInsetsLocked(this.mTmpRect2);
            DisplayInfo di = getDefaultDisplayInfoLocked();
            this.mTmpRect.set(0, 0, di.logicalWidth, di.logicalHeight);
            subtractInsets(this.mTmpRect, this.mTmpRect2, inOutBounds);
        }
    }

    void subtractInsets(Rect display, Rect insets, Rect inOutBounds) {
        this.mTmpRect3.set(display);
        this.mTmpRect3.inset(insets);
        inOutBounds.intersect(this.mTmpRect3);
    }

    public int getSmallestWidthForTaskBounds(Rect bounds) {
        int smallestWidthDpForBounds;
        synchronized (this.mWindowMap) {
            smallestWidthDpForBounds = getDefaultDisplayContentLocked().getDockedDividerController().getSmallestWidthDpForBounds(bounds);
        }
        return smallestWidthDpForBounds;
    }

    /* JADX WARNING: Missing block: B:10:0x001a, code:
            r7 = r10.mWindowMap;
     */
    /* JADX WARNING: Missing block: B:11:0x001c, code:
            monitor-enter(r7);
     */
    /* JADX WARNING: Missing block: B:14:0x001f, code:
            if (r10.mDragState == null) goto L_0x0026;
     */
    /* JADX WARNING: Missing block: B:15:0x0021, code:
            monitor-exit(r7);
     */
    /* JADX WARNING: Missing block: B:16:0x0022, code:
            return;
     */
    /* JADX WARNING: Missing block: B:22:?, code:
            r0 = windowForClientLocked(null, r11, false);
     */
    /* JADX WARNING: Missing block: B:23:0x002c, code:
            if (r0 != null) goto L_0x004a;
     */
    /* JADX WARNING: Missing block: B:24:0x002e, code:
            android.util.Slog.w("WindowManager", "Bad requesting window " + r11);
     */
    /* JADX WARNING: Missing block: B:25:0x0048, code:
            monitor-exit(r7);
     */
    /* JADX WARNING: Missing block: B:26:0x0049, code:
            return;
     */
    /* JADX WARNING: Missing block: B:28:?, code:
            r1 = r0.getDisplayContent();
     */
    /* JADX WARNING: Missing block: B:29:0x004e, code:
            if (r1 != null) goto L_0x0052;
     */
    /* JADX WARNING: Missing block: B:30:0x0050, code:
            monitor-exit(r7);
     */
    /* JADX WARNING: Missing block: B:31:0x0051, code:
            return;
     */
    /* JADX WARNING: Missing block: B:33:?, code:
            r5 = r1.getTouchableWinAtPointLocked(r3, r4);
     */
    /* JADX WARNING: Missing block: B:34:0x0056, code:
            if (r5 == r0) goto L_0x005a;
     */
    /* JADX WARNING: Missing block: B:35:0x0058, code:
            monitor-exit(r7);
     */
    /* JADX WARNING: Missing block: B:36:0x0059, code:
            return;
     */
    /* JADX WARNING: Missing block: B:38:?, code:
            r5.mClient.updatePointerIcon(r5.translateToWindowX(r3), r5.translateToWindowY(r4));
     */
    /* JADX WARNING: Missing block: B:43:?, code:
            android.util.Slog.w("WindowManager", "unable to update pointer icon");
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void updatePointerIcon(IWindow client) {
        synchronized (this.mMousePositionTracker) {
            if (this.mMousePositionTracker.mLatestEventWasMouse) {
                float mouseX = this.mMousePositionTracker.mLatestMouseX;
                float mouseY = this.mMousePositionTracker.mLatestMouseY;
            } else {
                return;
            }
        }
    }

    void restorePointerIconLocked(DisplayContent displayContent, float latestX, float latestY) {
        this.mMousePositionTracker.updatePosition(latestX, latestY);
        WindowState windowUnderPointer = displayContent.getTouchableWinAtPointLocked(latestX, latestY);
        if (windowUnderPointer != null) {
            try {
                windowUnderPointer.mClient.updatePointerIcon(windowUnderPointer.translateToWindowX(latestX), windowUnderPointer.translateToWindowY(latestY));
                return;
            } catch (RemoteException e) {
                Slog.w("WindowManager", "unable to restore pointer icon");
                return;
            }
        }
        InputManager.getInstance().setPointerIconType(1000);
    }

    public void registerShortcutKey(long shortcutCode, IShortcutService shortcutKeyReceiver) throws RemoteException {
        if (checkCallingPermission("android.permission.REGISTER_WINDOW_MANAGER_LISTENERS", "registerShortcutKey")) {
            this.mPolicy.registerShortcutKey(shortcutCode, shortcutKeyReceiver);
            return;
        }
        throw new SecurityException("Requires REGISTER_WINDOW_MANAGER_LISTENERS permission");
    }

    void markForSeamlessRotation(WindowState w, boolean seamlesslyRotated) {
        if (seamlesslyRotated != w.mSeamlesslyRotated) {
            w.mSeamlesslyRotated = seamlesslyRotated;
            if (seamlesslyRotated) {
                this.mSeamlessRotationCount++;
            } else {
                this.mSeamlessRotationCount--;
            }
            if (this.mSeamlessRotationCount == 0) {
                if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                    Slog.i(TAG, "Performing post-rotate rotation after seamless rotation");
                }
                if (updateRotationUncheckedLocked(false)) {
                    this.mH.sendEmptyMessage(18);
                }
            }
        }
    }

    void updateNonSystemOverlayWindowsVisibilityIfNeeded(WindowState win, boolean surfaceShown) {
        if (win.hideNonSystemOverlayWindowsWhenVisible()) {
            boolean systemAlertWindowsHidden = !this.mHidingNonSystemOverlayWindows.isEmpty();
            if (!surfaceShown) {
                this.mHidingNonSystemOverlayWindows.remove(win);
            } else if (!this.mHidingNonSystemOverlayWindows.contains(win)) {
                this.mHidingNonSystemOverlayWindows.add(win);
            }
            boolean hideSystemAlertWindows = !this.mHidingNonSystemOverlayWindows.isEmpty();
            if (systemAlertWindowsHidden != hideSystemAlertWindows) {
                int numDisplays = this.mDisplayContents.size();
                for (int displayNdx = 0; displayNdx < numDisplays; displayNdx++) {
                    WindowList windows = ((DisplayContent) this.mDisplayContents.valueAt(displayNdx)).getWindowList();
                    int numWindows = windows.size();
                    for (int winNdx = 0; winNdx < numWindows; winNdx++) {
                        ((WindowState) windows.get(winNdx)).setForceHideNonSystemOverlayWindowIfNeeded(hideSystemAlertWindows);
                    }
                }
            }
        }
    }

    boolean isAlarmBoot() {
        String bootReason = SystemProperties.get("sys.boot.reason");
        return bootReason != null && bootReason.equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON);
    }

    private void closeSystemDialogs() {
        WindowList windows = getDefaultWindowListLocked();
        synchronized (this.mWindowMap) {
            for (int i = windows.size() - 1; i >= 0; i--) {
                WindowState w = (WindowState) windows.get(i);
                if (w.mHasSurface && (w.mAttrs.type == 2008 || w.mAttrs.type == 2010 || w.mAttrs.type == 2003 || w.mAttrs.type == 2009)) {
                    removeWindow(w.mSession, w.mClient);
                }
            }
        }
    }

    public boolean isCacheStartingWindow() {
        return true;
    }

    public boolean isCacheFirstFrame() {
        return false;
    }

    public boolean isCacheLastFrame() {
        return false;
    }

    public boolean isFastStartingWindowSupport() {
        return (!this.mOppoEnableFastStartingWin || this.mDisableFastStartingWindow) ? false : this.mFastStartingWindowSupport;
    }

    public boolean hasBitmapByToken(IBinder token) {
        return getBitmapByToken(token) != null;
    }

    public Bitmap getBitmapByToken(IBinder token) {
        if (getRotation() == 0) {
            if (token != null) {
                try {
                    ComponentName activityName = this.mActivityManager.getActivityClassForToken(token);
                    String str = activityName == null ? null : activityName.toString();
                    if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
                        Slog.v(TAG, "getBitmapByToken ok token =" + token + ", name = " + str);
                    }
                    if (str != null) {
                        return (Bitmap) this.mBitmaps.get(str);
                    }
                } catch (RemoteException e) {
                    Slog.d(TAG, "getBitmapByToken failed", e);
                }
            } else if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
                Slog.v(TAG, "getBitmapByToken null " + token);
            }
        } else if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
            Slog.v(TAG, "getBitmapByToken rot null " + token);
        }
        return null;
    }

    public void setBitmapByToken(IBinder token, Bitmap bitmap) {
        if (getRotation() == 0) {
            if (token != null && bitmap != null) {
                try {
                    ComponentName activityName = this.mActivityManager.getActivityClassForToken(token);
                    String str = activityName == null ? null : activityName.toString();
                    if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
                        Slog.v(TAG, "setBitmapByToken ok, token =" + token + ", name = " + str);
                    }
                    if (str != null) {
                        this.mBitmaps.put(str, bitmap);
                    }
                } catch (RemoteException e) {
                    Slog.d(TAG, "setBitmapByToken failed", e);
                }
            } else if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
                Slog.v(TAG, "setBitmapByToken null " + token);
            }
        } else if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
            Slog.v(TAG, "setBitmapByToken rot null " + token);
        }
    }

    public void cacheStartingWindow(AppWindowToken appToken) {
        Message m = this.mH.obtainMessage(56, appToken);
        this.mH.removeMessages(56);
        this.mH.sendMessage(m);
    }

    public void setRotationBoost(boolean enable) {
        if (this.mPerfService == null) {
            this.mPerfService = new PerfServiceWrapper(null);
        }
        if (enable && !this.mIsPerfBoostEnable) {
            this.mPerfService.boostEnable(5);
        } else if (!enable && this.mIsPerfBoostEnable) {
            this.mPerfService.boostDisable(5);
        }
        this.mIsPerfBoostEnable = enable;
    }

    /* JADX WARNING: Missing block: B:12:0x0060, code:
            return;
     */
    /* JADX WARNING: Missing block: B:20:0x008a, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void stickWindow(int stackId, int taskId, boolean isSticky) {
        synchronized (this.mWindowMap) {
            if (MultiWindowManager.DEBUG) {
                Slog.d("WindowManager", "stickWindow, stackId = " + stackId + ", taskId = " + taskId + ", isSticky = " + isSticky);
            }
            Task task = (Task) this.mTaskIdToTask.get(taskId);
            if (task == null) {
                if (MultiWindowManager.DEBUG) {
                    Slog.d("WindowManager", "positionTaskInStack: could not find taskId=" + taskId);
                }
            } else if (((TaskStack) this.mStackIdToStack.get(stackId)) != null) {
                task.mSticky = isSticky;
            } else if (MultiWindowManager.DEBUG) {
                Slog.d("WindowManager", "positionTaskInStack: could not find stackId=" + stackId);
            }
        }
    }

    private boolean isStickyByMtk(WindowState win) {
        boolean z = false;
        if (win == null || win.mAppToken == null || win.getStack() == null || win.getTask() == null) {
            return false;
        }
        if (win.getStack().mStackId == 2) {
            z = win.getTask().mSticky;
        }
        return z;
    }

    public void registerFreeformStackListener(IFreeformStackListener listener) {
        this.mFreeformStackListeners.register(listener);
    }

    void showOrHideRestoreButton(WindowState win) {
        boolean isShown = (win != null && win.mFrame != null && win.mFrame.left == 0 && win.mFrame.top == 0 && win.mFrame.width() == this.mDisplayMetrics.widthPixels && win.getTask().isResizeable()) ? win.getStack().mStackId == 1 : false;
        if (isShown != this.mIsRestoreButtonVisible) {
            this.mIsRestoreButtonVisible = isShown;
            try {
                int size = this.mFreeformStackListeners.beginBroadcast();
                for (int i = 0; i < size; i++) {
                    try {
                        ((IFreeformStackListener) this.mFreeformStackListeners.getBroadcastItem(i)).onShowRestoreButtonChanged(isShown);
                    } catch (RemoteException e) {
                        Slog.e("WindowManager", "Error delivering show restore button changed event.", e);
                    }
                }
                this.mFreeformStackListeners.finishBroadcast();
            } catch (Exception e2) {
                Slog.e("WindowManager", "Error delivering show restore button changed event.", e2);
            }
        }
    }

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "XiaoKang.Feng@Plf.SDK, 2016-09-20 : Add for Surface detection", property = OppoRomType.ROM)
    public void setLastSurfaceAppName(String name) {
        if (name != null) {
            SystemProperties.set("debug.surface.package.name", name);
        }
    }

    private void sendPopupWinBroadcast(PopupInfo popupInfo) {
        if (this.mContext != null) {
            int type = popupInfo.type;
            if (type == 2 || type == 2002 || type == 2003 || type == 2005 || type == 2008 || type == 2010) {
                Intent intent = new Intent("action.oppo.popup.notify");
                intent.putExtra("action", popupInfo.action);
                intent.putExtra("pkg", popupInfo.pkg);
                intent.putExtra(PackageProcessList.KEY_UID, popupInfo.uid);
                intent.putExtra(SoundModelContract.KEY_TYPE, type);
                this.mContext.sendBroadcast(intent, "android.permission.RETRIEVE_WINDOW_CONTENT");
                Slog.d("popnotify", popupInfo.toString());
            }
        }
    }

    private String getForegroundPackage() {
        ComponentName cn;
        try {
            cn = new OppoActivityManager().getTopActivityComponentName();
        } catch (Exception e) {
            Log.w(TAG, "getTopActivityComponentName exception");
            cn = null;
        }
        if (cn != null) {
            return cn.getPackageName();
        }
        return null;
    }

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "ZhiYong.Lin@Plf.Framework, add for BPM", property = OppoRomType.ROM)
    public InputManagerService getInputManagerService() {
        return this.mInputManager;
    }

    public String getFocusedWindowPkg() {
        String result = IElsaManager.EMPTY_PACKAGE;
        WindowState window = getFocusedWindow();
        if (window != null) {
            return window.getOwningPackage();
        }
        return result;
    }

    private void disableStatusBar(boolean disable) {
        if (this.mContext != null) {
            StatusBarManager mStatusBar = (StatusBarManager) this.mContext.getSystemService("statusbar");
            int state = 0;
            if (disable) {
                state = DumpState.DUMP_INSTALLS;
            }
            Slog.v(TAG, "disableStatusBar state: " + state + " , disable: " + disable);
            mStatusBar.disable(state);
        }
    }

    private void adjustLanucherLayerLocked(DisplayContent displayContent, WindowState win) {
        if (win != null) {
            WindowList windows = displayContent.getWindowList();
            int NW = windows.size();
            int pos = windows.indexOf(win);
            int moveIndex = -1;
            if (pos <= NW && pos >= 2) {
                for (int i = 1; i < pos; i++) {
                    WindowState tmp = (WindowState) windows.get(i);
                    if (!(tmp == null || !tmp.mHasSurface || tmp.mIsImWindow || tmp.mIsWallpaper)) {
                        boolean isOpaque = true;
                        if (!(tmp.mAppToken == null || tmp.mAppToken.appFullscreen)) {
                            isOpaque = false;
                        }
                        if (!isOpaque) {
                            moveIndex = i;
                            Slog.v(TAG, "Found launcher layer @" + moveIndex);
                            break;
                        }
                    }
                }
                if (moveIndex < pos && moveIndex >= 0) {
                    windows.remove(pos);
                    windows.add(moveIndex, win);
                }
            }
        }
    }

    public WindowManagerPolicy getPolicy() {
        return this.mPolicy;
    }

    public boolean getInputShow() {
        if (this.mInputMethodWindow != null) {
            return this.mInputMethodWindow.mHasSurface;
        }
        return false;
    }

    public float getWindowGlobalScale(IWindow client) {
        synchronized (this.mWindowMap) {
            WindowState win = windowForClientLocked(null, client, false);
            if (win != null) {
                float f = win.mGlobalScale;
                return f;
            }
            return -1.0f;
        }
    }

    public void notifyDismissDockedStatckFromHome() {
        if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
            Slog.d("WindowManager", "onDismissDockedStackFromHome()");
        }
        this.dismissDockedStackFromHome = true;
    }

    private boolean isLeftOrTopDockedStack(Rect frame) {
        if (frame.top > this.mStatusBarHeight || frame.left > this.mStatusBarHeight) {
            return false;
        }
        return true;
    }

    public boolean isGoneForDismissDockedStack(WindowState win) {
        if (this.dismissDockedStackFromHome && win.mAppToken != null && win.mAppToken.stringName.contains(RECENT_ACTVITIY_NAME)) {
            return true;
        }
        return false;
    }

    public boolean isActivityNeedPalette(String pkg, String activityName) {
        if (this.mSystemReady) {
            return this.mColorNavigationBarUtil.isActivityNeedPalette(pkg, activityName);
        }
        return false;
    }

    public int getNavBarColorFromAdaptation(String pkg, String activityName) {
        if (this.mSystemReady) {
            return this.mColorNavigationBarUtil.getNavBarColorFromAdaptation(pkg, activityName);
        }
        return 0;
    }

    public int getStatusBarColorFromAdaptation(String pkg, String activityName) {
        if (this.mSystemReady) {
            return this.mColorNavigationBarUtil.getStatusBarColorFromAdaptation(pkg, activityName);
        }
        return 0;
    }

    public void setNavigationBarState(int state) {
        switch (state) {
            case 0:
                this.mPolicy.transientNavigatioinBar();
                return;
            default:
                return;
        }
    }

    public boolean GetDisplayFrozen() {
        return this.mDisplayFrozen;
    }

    public boolean killNotDrawnAppsWhenFrozen() {
        if (!this.mDisplayFrozen) {
            return false;
        }
        boolean killapp = false;
        synchronized (this.mWindowMap) {
            WindowList windows = getDefaultWindowListLocked();
            int i = windows.size();
            while (i > 0) {
                i--;
                WindowState w = (WindowState) windows.get(i);
                if (!(w.hasDrawnLw() || w.mSession == null || Process.myPid() == w.mSession.mPid)) {
                    Process.killProcess(w.mSession.mPid);
                    Log.i(TAG, "killNotDrawnAppsWhenFrozen kill w.mSession.mPid:" + w.mSession.mPid);
                    killapp = true;
                }
            }
        }
        return killapp;
    }

    protected void dynamicGetValue(PrintWriter pw, String[] args) {
        if (args.length == 1 || args.length == 2) {
            new DumpObject().dumpValue(pw, this, args.length == 2 ? args[1] : IElsaManager.EMPTY_PACKAGE);
            return;
        }
        pw.println("get_value usage:");
        pw.println("dumpsys window get_value");
        pw.println("or");
        pw.println("dumpsys window get_value variable");
    }

    protected void dynamicallyConfigLogTag(PrintWriter pw, String[] args, int opti) {
        pw.println("dynamicallyConfigLogTag, opti:" + opti + ", args.length:" + args.length);
        for (int index = 0; index < args.length; index++) {
            pw.println("dynamicallyConfigLogTag, args[" + index + "]:" + args[index]);
        }
        if (args.length != 3) {
            pw.println("********** Invalid argument! Get detail help as bellow: **********");
            logoutTagConfigHelp(pw);
            return;
        }
        String tag = args[1];
        boolean on = LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(args[2]);
        pw.println("dynamicallyConfigLogTag, tag:" + tag + ", on:" + on);
        if ("window".equals(tag)) {
            WindowManagerDebugConfig.DEBUG_ADD_REMOVE = on;
            WindowManagerDebugConfig.DEBUG_FOCUS = on;
            WindowManagerDebugConfig.DEBUG_FOCUS_LIGHT = on;
            WindowManagerDebugConfig.DEBUG_WINDOW_MOVEMENT = on;
            WindowManagerDebugConfig.DEBUG_TASK_MOVEMENT = on;
            WindowManagerDebugConfig.DEBUG_STARTING_WINDOW = on;
            WindowStateAnimator.DEBUG_STARTING_WINDOW = on;
            WindowManagerDebugConfig.DEBUG_STACK = on;
            WindowManagerDebugConfig.DEBUG_DIM_LAYER = on;
        } else if ("fresh".equals(tag)) {
            WindowManagerDebugConfig.DEBUG_LAYOUT = on;
            WindowManagerDebugConfig.DEBUG_RESIZE = on;
            WindowManagerDebugConfig.DEBUG_VISIBILITY = on;
            WindowStateAnimator.DEBUG_VISIBILITY = on;
            WindowState.DEBUG_VISIBILITY = on;
            WindowState.DEBUG_LAYOUT = on;
        } else if ("anim".equals(tag)) {
            WindowManagerDebugConfig.DEBUG_ANIM = on;
            WindowStateAnimator.DEBUG_ANIM = on;
        } else if ("input".equals(tag)) {
            WindowManagerDebugConfig.DEBUG_INPUT = on;
            WindowManagerDebugConfig.DEBUG_INPUT_METHOD = on;
            WindowManagerDebugConfig.DEBUG_DRAG = on;
        } else if ("screen".equals(tag)) {
            WindowManagerDebugConfig.DEBUG_SCREEN_ON = on;
            WindowManagerDebugConfig.DEBUG_SCREENSHOT = on;
            WindowManagerDebugConfig.DEBUG_BOOT = on;
        } else if ("apptoken".equals(tag)) {
            WindowManagerDebugConfig.DEBUG_TOKEN_MOVEMENT = on;
            WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS = on;
            WindowManagerDebugConfig.DEBUG_APP_ORIENTATION = on;
            if (on) {
                SystemProperties.set("sys.view.root.log", "true");
            } else {
                SystemProperties.set("sys.view.root.log", "false");
            }
            WindowList defaultWindows = getDefaultDisplayContentLocked().getWindowList();
            for (int i = defaultWindows.size() - 1; i >= 0; i--) {
                WindowState w = (WindowState) defaultWindows.get(i);
                if (!(w == null || w.mClient == null)) {
                    try {
                        w.mClient.enableLogLight(on);
                    } catch (RemoteException e) {
                        Slog.v(TAG, "RemoteException " + e + " w " + w);
                    } catch (Exception e2) {
                        Slog.e(TAG, "Exception when enable log light!", e2);
                    }
                }
            }
        } else if ("wallpaper".equals(tag)) {
            WindowManagerDebugConfig.DEBUG_WALLPAPER = on;
            WindowManagerDebugConfig.DEBUG_WALLPAPER_LIGHT = on;
        } else if ("config".equals(tag)) {
            WindowManagerDebugConfig.DEBUG_ORIENTATION = on;
            WindowManagerDebugConfig.DEBUG_APP_ORIENTATION = on;
            WindowManagerDebugConfig.DEBUG_CONFIGURATION = on;
            PROFILE_ORIENTATION = on;
            WindowStateAnimator.DEBUG_ORIENTATION = on;
        } else if ("trace".equals(tag)) {
            WindowManagerDebugConfig.DEBUG_SURFACE_TRACE = on;
            WindowManagerDebugConfig.DEBUG_WINDOW_TRACE = on;
            WindowStateAnimator.DEBUG_SURFACE_TRACE = on;
        } else if ("surface".equals(tag)) {
            WindowManagerDebugConfig.SHOW_SURFACE_ALLOC = on;
            WindowManagerDebugConfig.SHOW_TRANSACTIONS = on;
            WindowManagerDebugConfig.SHOW_LIGHT_TRANSACTIONS = on;
            WindowStateAnimator.SHOW_TRANSACTIONS = on;
            WindowStateAnimator.SHOW_LIGHT_TRANSACTIONS = on;
            WindowStateAnimator.SHOW_SURFACE_ALLOC = on;
        } else if ("layer".equals(tag)) {
            WindowManagerDebugConfig.DEBUG_LAYERS = on;
            WindowStateAnimator.DEBUG_LAYERS = on;
        } else if ("policy".equals(tag)) {
            DEBUG_POLICY = on;
            this.mPolicy.dump("debuglog", pw, args);
        } else if ("local".equals(tag)) {
            localLOGV = on;
            WindowStateAnimator.localLOGV = on;
        } else if ("systembar".equals(tag)) {
            WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR = on;
        } else {
            pw.println("Failed! Invalid argument! Type cmd for help: dumpsys window log");
        }
    }

    protected void logoutTagConfigHelp(PrintWriter pw) {
        pw.println("********************** Help begin:**********************");
        pw.println("1 Window add or remove:DEBUG_ADD_REMOVE | DEBUG_FOCUS | DEBUG_STARTING_WINDOW | DEBUG_WINDOW_MOVEMENT | DEBUG_FOCUS_LIGHT | DEBUG_TASK_MOVEMENT | DEBUG_STACK");
        pw.println("cmd: dumpsys window oppo-log window 0/1");
        pw.println("----------------------------------");
        pw.println("2 Window fresh: DEBUG_LAYOUT | DEBUG_RESIZE | DEBUG_VISIBILITY");
        pw.println("cmd: dumpsys window oppo-log fresh 0/1");
        pw.println("----------------------------------");
        pw.println("3 Animation:DEBUG_ANIM");
        pw.println("cmd: dumpsys window oppo-log anim 0/1");
        pw.println("----------------------------------");
        pw.println("4 Input envent:DEBUG_INPUT | DEBUG_INPUT_METHOD | DEBUG_DRAG");
        pw.println("cmd: dumpsys window oppo-log input 0/1");
        pw.println("----------------------------------");
        pw.println("5 Screen status change:DEBUG_SCREEN_ON | DEBUG_SCREENSHOT | DEBUG_BOOT");
        pw.println("cmd: dumpsys window oppo-log screen 0/1");
        pw.println("----------------------------------");
        pw.println("6 App token:DEBUG_TOKEN_MOVEMENT | DEBUG_APP_TRANSITIONS | DEBUG_APP_ORIENTATION");
        pw.println("cmd: dumpsys window oppo-log apptoken 0/1");
        pw.println("----------------------------------");
        pw.println("7 Wallpaper change:DEBUG_WALLPAPER | DEBUG_WALLPAPER_LIGH");
        pw.println("cmd: dumpsys window oppo-log wallpaper 0/1");
        pw.println("----------------------------------");
        pw.println("8 Config change:DEBUG_ORIENTATION | DEBUG_APP_ORIENTATION | DEBUG_CONFIGURATION | PROFILE_ORIENTATION");
        pw.println("cmd: dumpsys window oppo-log config 0/1");
        pw.println("----------------------------------");
        pw.println("9 Trace surface and window:DEBUG_SURFACE_TRACE | DEBUG_WINDOW_TRACE");
        pw.println("cmd: dumpsys window oppo-log trace 0/1");
        pw.println("----------------------------------");
        pw.println("10 Surface show change:SHOW_SURFACE_ALLOC | SHOW_TRANSACTIONS | SHOW_LIGHT_TRANSACTIONS");
        pw.println("cmd: dumpsys window oppo-log surface 0/1");
        pw.println("----------------------------------");
        pw.println("11 Layer change:DEBUG_LAYERS");
        pw.println("cmd: dumpsys window oppo-log layer 0/1");
        pw.println("----------------------------------");
        pw.println("12 PhoneWindowManager log:All PhoneWindowManager debug log switch");
        pw.println("cmd: dumpsys window oppo-log policy 0/1");
        pw.println("----------------------------------");
        pw.println("13 local log:localLOGV");
        pw.println("cmd: dumpsys window oppo-log local 0/1");
        pw.println("----------------------------------");
        pw.println("********************** Help end.  **********************");
    }

    protected void dumpDynamicallyLogSwitch(PrintWriter pw, String[] args, int opti) {
        boolean z = false;
        StringBuilder append;
        if (args.length == 1) {
            boolean z2;
            StringBuilder append2 = new StringBuilder().append("  window=");
            if (WindowManagerDebugConfig.DEBUG_ADD_REMOVE && WindowManagerDebugConfig.DEBUG_FOCUS && WindowManagerDebugConfig.DEBUG_FOCUS_LIGHT && WindowManagerDebugConfig.DEBUG_WINDOW_MOVEMENT && WindowManagerDebugConfig.DEBUG_TASK_MOVEMENT && WindowManagerDebugConfig.DEBUG_STARTING_WINDOW && WindowStateAnimator.DEBUG_STARTING_WINDOW) {
                z2 = WindowManagerDebugConfig.DEBUG_STACK;
            } else {
                z2 = false;
            }
            append2 = append2.append(z2).append("  fresh=");
            if (WindowManagerDebugConfig.DEBUG_LAYOUT && WindowManagerDebugConfig.DEBUG_RESIZE && WindowManagerDebugConfig.DEBUG_VISIBILITY && WindowStateAnimator.DEBUG_VISIBILITY && WindowState.DEBUG_VISIBILITY) {
                z2 = WindowState.DEBUG_LAYOUT;
            } else {
                z2 = false;
            }
            append2 = append2.append(z2).append("  anim=");
            if (WindowManagerDebugConfig.DEBUG_ANIM) {
                z2 = WindowStateAnimator.DEBUG_ANIM;
            } else {
                z2 = false;
            }
            append2 = append2.append(z2).append("  input=");
            if (WindowManagerDebugConfig.DEBUG_INPUT && WindowManagerDebugConfig.DEBUG_INPUT_METHOD) {
                z2 = WindowManagerDebugConfig.DEBUG_DRAG;
            } else {
                z2 = false;
            }
            append2 = append2.append(z2).append("  screen=");
            if (WindowManagerDebugConfig.DEBUG_SCREEN_ON && WindowManagerDebugConfig.DEBUG_SCREENSHOT) {
                z2 = WindowManagerDebugConfig.DEBUG_BOOT;
            } else {
                z2 = false;
            }
            append2 = append2.append(z2).append("  apptoken=");
            if (WindowManagerDebugConfig.DEBUG_TOKEN_MOVEMENT && WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                z2 = WindowManagerDebugConfig.DEBUG_APP_ORIENTATION;
            } else {
                z2 = false;
            }
            append2 = append2.append(z2).append("  wallpaper=");
            if (WindowManagerDebugConfig.DEBUG_WALLPAPER) {
                z2 = WindowManagerDebugConfig.DEBUG_WALLPAPER_LIGHT;
            } else {
                z2 = false;
            }
            append2 = append2.append(z2).append("  config=");
            if (WindowManagerDebugConfig.DEBUG_ORIENTATION && WindowManagerDebugConfig.DEBUG_APP_ORIENTATION && WindowManagerDebugConfig.DEBUG_CONFIGURATION && PROFILE_ORIENTATION) {
                z2 = WindowStateAnimator.DEBUG_ORIENTATION;
            } else {
                z2 = false;
            }
            append2 = append2.append(z2).append("  trace=");
            if (WindowManagerDebugConfig.DEBUG_SURFACE_TRACE && WindowManagerDebugConfig.DEBUG_WINDOW_TRACE) {
                z2 = WindowStateAnimator.DEBUG_SURFACE_TRACE;
            } else {
                z2 = false;
            }
            append2 = append2.append(z2).append("  surface=");
            if (WindowManagerDebugConfig.SHOW_SURFACE_ALLOC && WindowManagerDebugConfig.SHOW_TRANSACTIONS && WindowManagerDebugConfig.SHOW_LIGHT_TRANSACTIONS && WindowStateAnimator.SHOW_TRANSACTIONS && WindowStateAnimator.SHOW_LIGHT_TRANSACTIONS) {
                z2 = WindowStateAnimator.SHOW_SURFACE_ALLOC;
            } else {
                z2 = false;
            }
            append2 = append2.append(z2).append("  layer=");
            if (WindowManagerDebugConfig.DEBUG_LAYERS) {
                z2 = WindowStateAnimator.DEBUG_LAYERS;
            } else {
                z2 = false;
            }
            append = append2.append(z2).append("  policy=").append(DEBUG_POLICY).append("  local=");
            if (localLOGV) {
                z = WindowStateAnimator.localLOGV;
            }
            pw.println(append.append(z).toString());
        } else if (args.length == 2) {
            String tag = args[1];
            if ("window".equals(tag)) {
                append = new StringBuilder().append("  window=");
                if (WindowManagerDebugConfig.DEBUG_ADD_REMOVE && WindowManagerDebugConfig.DEBUG_FOCUS && WindowManagerDebugConfig.DEBUG_FOCUS_LIGHT && WindowManagerDebugConfig.DEBUG_WINDOW_MOVEMENT && WindowManagerDebugConfig.DEBUG_TASK_MOVEMENT && WindowManagerDebugConfig.DEBUG_STARTING_WINDOW && WindowStateAnimator.DEBUG_STARTING_WINDOW) {
                    z = WindowManagerDebugConfig.DEBUG_STACK;
                }
                pw.println(append.append(z).toString());
            } else if ("fresh".equals(tag)) {
                append = new StringBuilder().append("  fresh=");
                if (WindowManagerDebugConfig.DEBUG_LAYOUT && WindowManagerDebugConfig.DEBUG_RESIZE && WindowManagerDebugConfig.DEBUG_VISIBILITY && WindowStateAnimator.DEBUG_VISIBILITY && WindowState.DEBUG_VISIBILITY) {
                    z = WindowState.DEBUG_LAYOUT;
                }
                pw.println(append.append(z).toString());
            } else if ("anim".equals(tag)) {
                append = new StringBuilder().append("  anim=");
                if (WindowManagerDebugConfig.DEBUG_ANIM) {
                    z = WindowStateAnimator.DEBUG_ANIM;
                }
                pw.println(append.append(z).toString());
            } else if ("input".equals(tag)) {
                append = new StringBuilder().append("  input=");
                if (WindowManagerDebugConfig.DEBUG_INPUT && WindowManagerDebugConfig.DEBUG_INPUT_METHOD) {
                    z = WindowManagerDebugConfig.DEBUG_DRAG;
                }
                pw.println(append.append(z).toString());
            } else if ("screen".equals(tag)) {
                append = new StringBuilder().append("  screen=");
                if (WindowManagerDebugConfig.DEBUG_SCREEN_ON && WindowManagerDebugConfig.DEBUG_SCREENSHOT) {
                    z = WindowManagerDebugConfig.DEBUG_BOOT;
                }
                pw.println(append.append(z).toString());
            } else if ("apptoken".equals(tag)) {
                append = new StringBuilder().append("  apptoken=");
                if (WindowManagerDebugConfig.DEBUG_TOKEN_MOVEMENT && WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                    z = WindowManagerDebugConfig.DEBUG_APP_ORIENTATION;
                }
                pw.println(append.append(z).toString());
            } else if ("wallpaper".equals(tag)) {
                append = new StringBuilder().append("  wallpaper=");
                if (WindowManagerDebugConfig.DEBUG_WALLPAPER) {
                    z = WindowManagerDebugConfig.DEBUG_WALLPAPER_LIGHT;
                }
                pw.println(append.append(z).toString());
            } else if ("config".equals(tag)) {
                append = new StringBuilder().append("  config=");
                if (WindowManagerDebugConfig.DEBUG_ORIENTATION && WindowManagerDebugConfig.DEBUG_APP_ORIENTATION && WindowManagerDebugConfig.DEBUG_CONFIGURATION && PROFILE_ORIENTATION) {
                    z = WindowStateAnimator.DEBUG_ORIENTATION;
                }
                pw.println(append.append(z).toString());
            } else if ("trace".equals(tag)) {
                append = new StringBuilder().append("  trace=");
                if (WindowManagerDebugConfig.DEBUG_SURFACE_TRACE && WindowManagerDebugConfig.DEBUG_WINDOW_TRACE) {
                    z = WindowStateAnimator.DEBUG_SURFACE_TRACE;
                }
                pw.println(append.append(z).toString());
            } else if ("surface".equals(tag)) {
                append = new StringBuilder().append("  surface=");
                if (WindowManagerDebugConfig.SHOW_SURFACE_ALLOC && WindowManagerDebugConfig.SHOW_TRANSACTIONS && WindowManagerDebugConfig.SHOW_LIGHT_TRANSACTIONS && WindowStateAnimator.SHOW_TRANSACTIONS && WindowStateAnimator.SHOW_LIGHT_TRANSACTIONS) {
                    z = WindowStateAnimator.SHOW_SURFACE_ALLOC;
                }
                pw.println(append.append(z).toString());
            } else if ("layer".equals(tag)) {
                append = new StringBuilder().append("  layer=");
                if (WindowManagerDebugConfig.DEBUG_LAYERS) {
                    z = WindowStateAnimator.DEBUG_LAYERS;
                }
                pw.println(append.append(z).toString());
            } else if ("policy".equals(tag)) {
                pw.println("  policy=" + DEBUG_POLICY);
            } else if ("local".equals(tag)) {
                append = new StringBuilder().append("  local=");
                if (localLOGV) {
                    z = WindowStateAnimator.localLOGV;
                }
                pw.println(append.append(z).toString());
            } else {
                pw.println("Failed! Invalid argument! Type cmd for help: dumpsys window log");
            }
        }
    }

    public boolean isKeyguardShown() {
        return this.mPolicy.isKeyguardShown();
    }

    public void setFreeingChange(boolean change) {
        this.mFreeingChange = change;
    }

    public void setSplitFromBack(boolean change) {
        this.mSplitFormBack = change;
    }

    public boolean getSplitFromBack() {
        return this.mSplitFormBack;
    }

    boolean isWindowsFreezingScreenTimeout() {
        return this.mWindowsFreezingScreen == 2;
    }
}
