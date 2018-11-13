package com.android.server.policy;

import android.annotation.OppoHook;
import android.annotation.OppoHook.OppoHookType;
import android.annotation.OppoHook.OppoRomType;
import android.app.ActivityManager;
import android.app.ActivityManager.StackId;
import android.app.ActivityManagerInternal;
import android.app.ActivityManagerInternal.SleepToken;
import android.app.ActivityManagerNative;
import android.app.AppOpsManager;
import android.app.IActivityManager;
import android.app.IUiModeManager;
import android.app.IUiModeManager.Stub;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.app.StatusBarManager;
import android.app.UiModeManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.display.DisplayManagerInternal;
import android.hardware.fingerprint.FingerprintInternal;
import android.hardware.hdmi.HdmiControlManager;
import android.hardware.hdmi.HdmiPlaybackClient;
import android.hardware.hdmi.HdmiPlaybackClient.OneTouchPlayCallback;
import android.hardware.input.InputManager;
import android.hardware.input.InputManagerInternal;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.AudioSystem;
import android.media.IAudioService;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.session.MediaSessionLegacyHelper;
import android.os.Binder;
import android.os.Bundle;
import android.os.Debug;
import android.os.FactoryTest;
import android.os.Handler;
import android.os.IBinder;
import android.os.IDeviceIdleController;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.PowerManagerInternal;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UEventObserver;
import android.os.UEventObserver.UEvent;
import android.os.UserHandle;
import android.os.Vibrator;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.service.dreams.DreamManagerInternal;
import android.service.dreams.IDreamManager;
import android.telecom.TelecomManager;
import android.util.DisplayMetrics;
import android.util.EventLog;
import android.util.Log;
import android.util.LongSparseArray;
import android.util.MutableBoolean;
import android.util.Slog;
import android.util.SparseArray;
import android.view.Display;
import android.view.IApplicationToken;
import android.view.IWindowManager;
import android.view.InputChannel;
import android.view.InputEvent;
import android.view.InputEventReceiver;
import android.view.InputEventReceiver.Factory;
import android.view.KeyCharacterMap;
import android.view.KeyCharacterMap.FallbackAction;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.view.WindowManager.BadTokenException;
import android.view.WindowManager.LayoutParams;
import android.view.WindowManagerGlobal;
import android.view.WindowManagerInternal;
import android.view.WindowManagerPolicy;
import android.view.WindowManagerPolicy.InputConsumer;
import android.view.WindowManagerPolicy.OnKeyguardExitResult;
import android.view.WindowManagerPolicy.ScreenOnListener;
import android.view.WindowManagerPolicy.WindowManagerFuncs;
import android.view.WindowManagerPolicy.WindowState;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import com.android.internal.R;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.policy.IShortcutService;
import com.android.internal.policy.PhoneWindow;
import com.android.internal.statusbar.IStatusBarService;
import com.android.internal.util.ScreenShapeHelper;
import com.android.internal.widget.PointerLocationView;
import com.android.server.LocalServices;
import com.android.server.LocationManagerService;
import com.android.server.ServiceThread;
import com.android.server.am.OppoProcessManager;
import com.android.server.oppo.IElsaManager;
import com.android.server.oppo.ScreenOnCpuBoostHelper;
import com.android.server.policy.keyguard.KeyguardServiceDelegate;
import com.android.server.policy.keyguard.KeyguardServiceDelegate.DrawnListener;
import com.android.server.policy.keyguard.KeyguardStateMonitor.OnShowingStateChangedCallback;
import com.android.server.statusbar.StatusBarManagerInternal;
import com.android.server.wm.WindowManagerDebugConfig;
import com.color.view.ColorWindowManager;
import com.oppo.debug.InputLog;
import com.oppo.debug.ProcessCpuTrackerRunnable;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

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
public class PhoneWindowManager implements WindowManagerPolicy {
    private static final String ACTION_DISABLE_LIDCONTROLSSLEEP = "oppo.intent.action.DISABLE_LIDCONTROLSSLEEP";
    private static final String ACTION_ENABLE_LIDCONTROLSSLEEP = "oppo.intent.action.ENABLE_LIDCONTROLSSLEEP";
    static final boolean ALTERNATE_CAR_MODE_NAV_SIZE = false;
    static final int APPLICATION_ABOVE_SUB_PANEL_SUBLAYER = 3;
    static final int APPLICATION_MEDIA_OVERLAY_SUBLAYER = -1;
    static final int APPLICATION_MEDIA_SUBLAYER = -2;
    static final int APPLICATION_PANEL_SUBLAYER = 1;
    static final int APPLICATION_SUB_PANEL_SUBLAYER = 2;
    private static final int BRIGHTNESS_STEPS = 10;
    static boolean DEBUG = false;
    static boolean DEBUG_INPUT = false;
    static boolean DEBUG_KEYGUARD = false;
    static boolean DEBUG_LAYOUT = false;
    static boolean DEBUG_ORIENTATION = false;
    static boolean DEBUG_STARTING_WINDOW = false;
    static boolean DEBUG_WAKEUP = false;
    static final int DEFAULT_LONG_PRESS_POWERON_DISPLAY_TIME = 2500;
    protected static final int DISMISS_KEYGUARD_CONTINUE = 2;
    protected static final int DISMISS_KEYGUARD_NONE = 0;
    protected static final int DISMISS_KEYGUARD_START = 1;
    private static final int DISMISS_SCREEN_PINNING_KEY_CODE = 4;
    static final int DOUBLE_TAP_HOME_NOTHING = 0;
    static final int DOUBLE_TAP_HOME_RECENT_SYSTEM_UI = 1;
    private static final int DURATION_START_SPLIT_SCREEN = 750;
    static final boolean ENABLE_DESK_DOCK_HOME_CAPTURE = false;
    private static final String HIDE_NAVIGATIONBAR_ENABLE = "hide_navigationbar_enable";
    public static final String IPO_DISABLE = "android.intent.action.ACTION_BOOT_IPO";
    public static final String IPO_ENABLE = "android.intent.action.ACTION_SHUTDOWN_IPO";
    static final boolean IS_USER_BUILD = false;
    private static final float KEYGUARD_SCREENSHOT_CHORD_DELAY_MULTIPLIER = 2.5f;
    static final int KEY_DISPATCH_MODE_ALL_DISABLE = 1;
    static final int KEY_DISPATCH_MODE_ALL_ENABLE = 0;
    static final int KEY_DISPATCH_MODE_HOME_DISABLE = 2;
    static final int KEY_OFFSET_VALUE = 800;
    static final int LAST_LONG_PRESS_HOME_BEHAVIOR = 2;
    static final int LONG_PRESS_BACK_GO_TO_VOICE_ASSIST = 1;
    static final int LONG_PRESS_BACK_NOTHING = 0;
    static final int LONG_PRESS_HOME_ASSIST = 2;
    static final int LONG_PRESS_HOME_NOTHING = 0;
    static final int LONG_PRESS_HOME_RECENT_SYSTEM_UI = 1;
    static final int LONG_PRESS_POWER_GLOBAL_ACTIONS = 1;
    static final int LONG_PRESS_POWER_NOTHING = 0;
    static final int LONG_PRESS_POWER_SHUT_OFF = 2;
    static final int LONG_PRESS_POWER_SHUT_OFF_NO_CONFIRM = 3;
    public static final int MODE_NAVIGATIONBAR_GESTURE = 2;
    private static final int MSG_BACK_LONG_PRESS = 18;
    private static final int MSG_DISABLE_POINTER_LOCATION = 2;
    private static final int MSG_DISPATCH_MEDIA_KEY_REPEAT_WITH_WAKE_LOCK = 4;
    private static final int MSG_DISPATCH_MEDIA_KEY_WITH_WAKE_LOCK = 3;
    private static final int MSG_DISPATCH_SHOW_GLOBAL_ACTIONS = 10;
    private static final int MSG_DISPATCH_SHOW_RECENTS = 9;
    private static final int MSG_DISPOSE_INPUT_CONSUMER = 19;
    private static final int MSG_ENABLE_POINTER_LOCATION = 1;
    private static final int MSG_HIDE_BOOT_MESSAGE = 11;
    private static final int MSG_KEYGUARD_DRAWN_COMPLETE = 5;
    private static final int MSG_KEYGUARD_DRAWN_TIMEOUT = 6;
    private static final int MSG_LAUNCH_VOICE_ASSIST_WITH_WAKE_LOCK = 12;
    private static final int MSG_MULTI_WINDOW_FOCUS_CHANGED = 21;
    private static final int MSG_POWER_DELAYED_PRESS = 13;
    private static final int MSG_POWER_KEY_WAKE_LOCK_TIME_OUT = 255;
    private static final int MSG_POWER_LONG_PRESS = 14;
    private static final int MSG_POWER_LONG_PRESS_HARDWARE_SHUTDOWN = 20;
    private static final int MSG_REQUEST_TRANSIENT_BARS = 16;
    private static final int MSG_REQUEST_TRANSIENT_BARS_ARG_NAVIGATION = 1;
    private static final int MSG_REQUEST_TRANSIENT_BARS_ARG_STATUS = 0;
    private static final int MSG_SHOW_TV_PICTURE_IN_PICTURE_MENU = 17;
    private static final int MSG_UPDATE_DREAMING_SLEEP_TOKEN = 15;
    private static final int MSG_WINDOW_MANAGER_DRAWN_COMPLETE = 7;
    static final int MULTI_PRESS_POWER_BRIGHTNESS_BOOST = 2;
    static final int MULTI_PRESS_POWER_NOTHING = 0;
    static final int MULTI_PRESS_POWER_THEATER_MODE = 1;
    private static final int NAV_BAR_BOTTOM = 0;
    private static final int NAV_BAR_LEFT = 2;
    static final int NAV_BAR_OPAQUE_WHEN_FREEFORM_OR_DOCKED = 0;
    private static final int NAV_BAR_RIGHT = 1;
    static final int NAV_BAR_TRANSLUCENT_WHEN_FREEFORM_OPAQUE_OTHERWISE = 1;
    private static final String NORMAL_BOOT_ACTION = "android.intent.action.normal.boot";
    private static final String NORMAL_SHUTDOWN_ACTION = "android.intent.action.normal.shutdown";
    private static final long PANIC_GESTURE_EXPIRATION = 30000;
    static final int PENDING_KEY_NULL = -1;
    private static final long POWER_KEY_WAKE_LOCK_TIME_OUT = 120000;
    static final boolean PRINT_ANIM = false;
    private static final long SCREENSHOT_CHORD_DEBOUNCE_DELAY_MILLIS = 150;
    private static final String SHORTCUT_WINDOW = "ShortcutsPanel";
    static final int SHORT_PRESS_POWER_GO_HOME = 4;
    static final int SHORT_PRESS_POWER_GO_TO_SLEEP = 1;
    static final int SHORT_PRESS_POWER_NOTHING = 0;
    static final int SHORT_PRESS_POWER_REALLY_GO_TO_SLEEP = 2;
    static final int SHORT_PRESS_POWER_REALLY_GO_TO_SLEEP_AND_GO_HOME = 3;
    static final int SHORT_PRESS_SLEEP_GO_TO_SLEEP = 0;
    static final int SHORT_PRESS_SLEEP_GO_TO_SLEEP_AND_GO_HOME = 1;
    static final int SHORT_PRESS_WINDOW_NOTHING = 0;
    static final int SHORT_PRESS_WINDOW_PICTURE_IN_PICTURE = 1;
    static final boolean SHOW_STARTING_ANIMATIONS = true;
    public static final String STK_USERACTIVITY = "android.intent.action.stk.USER_ACTIVITY";
    public static final String STK_USERACTIVITY_ENABLE = "android.intent.action.stk.USER_ACTIVITY.enable";
    public static final String SYSTEM_DIALOG_REASON_ASSIST = "assist";
    public static final String SYSTEM_DIALOG_REASON_GLOBAL_ACTIONS = "globalactions";
    public static final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
    public static final String SYSTEM_DIALOG_REASON_KEY = "reason";
    public static final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";
    static final int SYSTEM_UI_CHANGING_LAYOUT = -1073709010;
    public static final int SYSTEM_UI_FLAG_APP_LIGHT_NAVIGATION_BAR = 128;
    public static final int SYSTEM_UI_FLAG_FOCUS_TOP_OR_LEFT = 64;
    public static final int SYSTEM_UI_FLAG_OPPO_NAVIGATION_BAR_IME_TOP = 16384;
    public static final int SYSTEM_UI_FLAG_OPPO_NAVIGATION_BAR_SHOW_IME = 32768;
    private static final String SYSUI_PACKAGE = "com.android.systemui";
    private static final String SYSUI_SCREENSHOT_ERROR_RECEIVER = "com.android.systemui.screenshot.ScreenshotServiceErrorReceiver";
    private static final String SYSUI_SCREENSHOT_SERVICE = "com.android.systemui.screenshot.TakeScreenshotService";
    static final String TAG = "WindowManager";
    public static final int TOAST_WINDOW_TIMEOUT = 3500;
    private static final AudioAttributes VIBRATION_ATTRIBUTES = null;
    static final int WAITING_FOR_DRAWN_TIMEOUT = 1000;
    private static final int[] WINDOW_TYPES_WHERE_HOME_DOESNT_WORK = null;
    static boolean localLOGV;
    static final Rect mTmpContentFrame = null;
    static final Rect mTmpDecorFrame = null;
    static final Rect mTmpDisplayFrame = null;
    static final Rect mTmpNavigationFrame = null;
    static final Rect mTmpOutsetFrame = null;
    static final Rect mTmpOverscanFrame = null;
    static final Rect mTmpParentFrame = null;
    private static final Rect mTmpRect = null;
    static final Rect mTmpStableFrame = null;
    static final Rect mTmpVisibleFrame = null;
    static SparseArray<String> sApplicationLaunchKeyCategories;
    @OppoHook(level = OppoHookType.NEW_FIELD, note = "oukun@ROM.SysApp, 2015.03.21:add for pull down status bar when full screen", property = OppoRomType.ROM)
    boolean bForceShowStatusBar;
    boolean isTouchFingerPrintSensor;
    boolean isUspEnable;
    boolean mAccelerometerDefault;
    AccessibilityManager mAccessibilityManager;
    IActivityManager mActivityManager;
    ActivityManagerInternal mActivityManagerInternal;
    int mAllowAllRotations;
    boolean mAllowLockscreenWhenOn;
    private boolean mAllowTheaterModeWakeFromCameraLens;
    private boolean mAllowTheaterModeWakeFromKey;
    private boolean mAllowTheaterModeWakeFromLidSwitch;
    private boolean mAllowTheaterModeWakeFromMotion;
    private boolean mAllowTheaterModeWakeFromMotionWhenNotDreaming;
    private boolean mAllowTheaterModeWakeFromPowerKey;
    private boolean mAllowTheaterModeWakeFromWakeGesture;
    boolean mAppLaunchTimeEnabled;
    AppOpsManager mAppOpsManager;
    HashSet<IApplicationToken> mAppsThatDismissKeyguard;
    HashSet<IApplicationToken> mAppsToBeHidden;
    boolean mAssistKeyLongPressed;
    boolean mAwake;
    volatile boolean mBackKeyHandled;
    volatile boolean mBeganFromNonInteractive;
    boolean mBootMessageNeedsHiding;
    ProgressDialog mBootMsgDialog;
    WakeLock mBroadcastWakeLock;
    BurnInProtectionHelper mBurnInProtectionHelper;
    long[] mCalendarDateVibePattern;
    volatile boolean mCameraGestureTriggeredDuringGoingToSleep;
    int mCameraLensCoverState;
    boolean mCarDockEnablesAccelerometer;
    Intent mCarDockIntent;
    int mCarDockRotation;
    private final Runnable mClearHideNavigationFlag;
    long[] mClockTickVibePattern;
    boolean mConsumeSearchKeyUp;
    int mContentBottom;
    int mContentLeft;
    int mContentRight;
    int mContentTop;
    Context mContext;
    long[] mContextClickVibePattern;
    int mCurBottom;
    int mCurLeft;
    int mCurRight;
    int mCurTop;
    int mCurrentAppOrientation;
    protected int mCurrentUserId;
    @GuardedBy("Lw")
    private boolean mCurrentlyDismissingKeyguard;
    private boolean mDeferBindKeyguard;
    int mDemoHdmiRotation;
    boolean mDemoHdmiRotationLock;
    int mDemoRotation;
    boolean mDemoRotationLock;
    boolean mDeskDockEnablesAccelerometer;
    Intent mDeskDockIntent;
    int mDeskDockRotation;
    int mDismissKeyguard;
    Display mDisplay;
    DisplayManagerInternal mDisplayManagerInternal;
    private int mDisplayRotation;
    int mDockBottom;
    int mDockLayer;
    int mDockLeft;
    int mDockMode;
    BroadcastReceiver mDockReceiver;
    int mDockRight;
    int mDockTop;
    final Rect mDockedStackBounds;
    int mDoublePressOnPowerBehavior;
    private int mDoubleTapOnHomeBehavior;
    DreamManagerInternal mDreamManagerInternal;
    BroadcastReceiver mDreamReceiver;
    boolean mDreamingLockscreen;
    SleepToken mDreamingSleepToken;
    boolean mDreamingSleepTokenNeeded;
    private boolean mEnableCarDockHomeCapture;
    boolean mEnableShiftMenuBugReports;
    volatile boolean mEndCallKeyHandled;
    private final Runnable mEndCallLongPress;
    int mEndcallBehavior;
    BroadcastReceiver mEngineerModeHandleReceiver;
    private boolean mExpendBar;
    private final SparseArray<FallbackAction> mFallbackActions;
    FingerprintInternal mFingerprintInternal;
    IApplicationToken mFocusedApp;
    WindowState mFocusedWindow;
    int mForceClearedSystemUiFlags;
    private boolean mForceDefaultOrientation;
    boolean mForceShowSystemBars;
    boolean mForceStatusBar;
    boolean mForceStatusBarFromKeyguard;
    private boolean mForceStatusBarTransparent;
    boolean mForcingShowNavBar;
    int mForcingShowNavBarLayer;
    @OppoHook(level = OppoHookType.CHANGE_BASE_CLASS, note = "Xiaokang.Feng@Plf.SDK, modify for Oppo global actions", property = OppoRomType.ROM)
    OppoGlobalActions mGlobalActions;
    private GlobalKeyManager mGlobalKeyManager;
    private boolean mGoToSleepOnButtonPressTheaterMode;
    volatile boolean mGoingToSleep;
    private UEventObserver mHDMIObserver;
    Handler mHandler;
    private boolean mHasFeatureWatch;
    boolean mHasNavigationBar;
    boolean mHasSoftInput;
    boolean mHaveBuiltInKeyboard;
    boolean mHavePendingMediaKeyRepeatWithWakeLock;
    HdmiControl mHdmiControl;
    boolean mHdmiPlugged;
    private final Runnable mHiddenNavPanic;
    boolean mHideLockScreen;
    final Factory mHideNavInputEventReceiverFactory;
    boolean mHideNavigationBar;
    boolean mHideStatusBarWhenLockScreen;
    boolean mHomeConsumed;
    boolean mHomeDoubleTapPending;
    private final Runnable mHomeDoubleTapTimeoutRunnable;
    Intent mHomeIntent;
    boolean mHomePressed;
    int mIPOUserRotation;
    private ImmersiveModeConfirmation mImmersiveModeConfirmation;
    int mIncallPowerBehavior;
    int mInitialMetaState;
    InputConsumer mInputConsumer;
    InputManagerInternal mInputManagerInternal;
    BroadcastReceiver mIpoEventReceiver;
    private boolean mIsAlarmBoot;
    boolean mIsEnableKeyguardHide;
    private boolean mIsIpoShutDown;
    public AtomicBoolean mIsMute;
    private boolean mIsShutDown;
    boolean mIsStkUserActivityEnabled;
    int mKeyDispatcMode;
    final Object mKeyDispatchLock;
    private long mKeyRemappingSendFakeKeyDownTime;
    private Runnable mKeyRemappingVolumeDownLongPress;
    private Runnable mKeyRemappingVolumeDownLongPress_Test;
    private boolean mKeyRemappingVolumeDownLongPressed;
    private Runnable mKeyRemappingVolumeUpLongPress;
    private boolean mKeyRemappingVolumeUpLongPressed;
    long[] mKeyboardTapVibePattern;
    WindowState mKeyguard;
    KeyguardServiceDelegate mKeyguardDelegate;
    @OppoHook(level = OppoHookType.NEW_FIELD, note = "YuHao@Plf.DesktopApp.Keyguard, 2014/10/24, Add for Avoid Keyguard blink When activity showing on Keyguard exchange.", property = OppoRomType.ROM)
    protected boolean mKeyguardDoShowLw;
    boolean mKeyguardDrawComplete;
    final DrawnListener mKeyguardDrawnCallback;
    private boolean mKeyguardDrawnOnce;
    protected boolean mKeyguardHidden;
    volatile boolean mKeyguardOccluded;
    private WindowState mKeyguardScrim;
    boolean mKeyguardSecure;
    boolean mKeyguardSecureIncludingHidden;
    int mLandscapeRotation;
    boolean mLanguageSwitchKeyPressed;
    private int mLastDisplayRotation;
    final Rect mLastDockedStackBounds;
    int mLastDockedStackSysUiFlags;
    private boolean mLastExpand;
    private int mLastExpendBarColor;
    boolean mLastFocusNeedsMenu;
    int mLastFullscreenStackSysUiFlags;
    private long mLastHomeDownTimeDuringPF;
    WindowState mLastInputMethodTargetWindow;
    WindowState mLastInputMethodWindow;
    private int mLastNavVisibility;
    private int mLastNavigationBarColor;
    final Rect mLastNonDockedStackBounds;
    int mLastSystemUiFlags;
    private int mLastValidNavigationBarTint;
    private int mLastValidStatusBarTint;
    int mLastWindowFocusFlags;
    boolean mLidControlsScreenLock;
    boolean mLidControlsSleep;
    int mLidKeyboardAccessibility;
    int mLidNavigationAccessibility;
    int mLidOpenRotation;
    int mLidState;
    private final Object mLock;
    int mLockScreenTimeout;
    boolean mLockScreenTimerActive;
    private final LogDecelerateInterpolator mLogDecelerateInterpolator;
    int mLongPressOnBackBehavior;
    private int mLongPressOnHomeBehavior;
    int mLongPressOnPowerBehavior;
    long[] mLongPressVibePattern;
    @OppoHook(level = OppoHookType.NEW_FIELD, note = "JianHui.Yu@Plf.SDK, 2015-01-06 : Add for Longshot", property = OppoRomType.ROM)
    private Runnable mLongshotRunnable;
    int mMetaState;
    BroadcastReceiver mMultiuserReceiver;
    private int mNaturalHeight;
    private int mNaturalWidth;
    int mNavBarOpacityMode;
    WindowState mNavigationBar;
    boolean mNavigationBarCanMove;
    private final BarController mNavigationBarController;
    private int mNavigationBarEnableStatus;
    int[] mNavigationBarHeightForRotationDefault;
    int[] mNavigationBarHeightForRotationInCarMode;
    int mNavigationBarPosition;
    int[] mNavigationBarWidthForRotationDefault;
    int[] mNavigationBarWidthForRotationInCarMode;
    final Rect mNonDockedStackBounds;
    Runnable mNotifyStk;
    MyOrientationListener mOrientationListener;
    boolean mOrientationSensorEnabled;
    int mOverscanBottom;
    int mOverscanLeft;
    int mOverscanRight;
    int mOverscanScreenHeight;
    int mOverscanScreenLeft;
    int mOverscanScreenTop;
    int mOverscanScreenWidth;
    int mOverscanTop;
    boolean mPendingCapsLockToggle;
    boolean mPendingMetaAction;
    private long mPendingPanicGestureUptime;
    volatile int mPendingWakeKey;
    private PhoneWinHandler mPhoneWinHandler;
    private ServiceThread mPhoneWinHandlerThread;
    int mPointerLocationMode;
    PointerLocationView mPointerLocationView;
    int mPortraitRotation;
    volatile boolean mPowerKeyHandled;
    volatile int mPowerKeyPressCounter;
    WakeLock mPowerKeyWakeLock;
    PowerManager mPowerManager;
    PowerManagerInternal mPowerManagerInternal;
    BroadcastReceiver mPoweroffAlarmReceiver;
    boolean mPreloadedRecentApps;
    private ProcessCpuTrackerRunnable mProcessCpuTrackerRunnable;
    int mRecentAppsHeldModifiers;
    private boolean mRecentsLongPressDetected;
    private final Runnable mRecentsStartSplitSreen;
    volatile boolean mRecentsVisible;
    int mResettingSystemUiFlags;
    int mRestrictedOverscanScreenHeight;
    int mRestrictedOverscanScreenLeft;
    int mRestrictedOverscanScreenTop;
    int mRestrictedOverscanScreenWidth;
    int mRestrictedScreenHeight;
    int mRestrictedScreenLeft;
    int mRestrictedScreenTop;
    int mRestrictedScreenWidth;
    boolean mSafeMode;
    long[] mSafeModeDisabledVibePattern;
    long[] mSafeModeEnabledVibePattern;
    ScreenLockTimeout mScreenLockTimeout;
    int mScreenOffReason;
    SleepToken mScreenOffSleepToken;
    ScreenOnCpuBoostHelper mScreenOnCpuBoostHelper;
    boolean mScreenOnEarly;
    boolean mScreenOnFully;
    ScreenOnListener mScreenOnListener;
    private boolean mScreenshotChordEnabled;
    private long mScreenshotChordPowerKeyTime;
    private boolean mScreenshotChordPowerKeyTriggered;
    private boolean mScreenshotChordVolumeDownKeyConsumed;
    private long mScreenshotChordVolumeDownKeyTime;
    private boolean mScreenshotChordVolumeDownKeyTriggered;
    @OppoHook(level = OppoHookType.NEW_FIELD, note = "JianHui.Yu@Plf.SDK, 2016-05-20 : Add for Longshot", property = OppoRomType.ROM)
    private boolean mScreenshotChordVolumeUpKeyConsumed;
    @OppoHook(level = OppoHookType.NEW_FIELD, note = "JianHui.Yu@Plf.SDK, 2016-05-20 : Add for Longshot", property = OppoRomType.ROM)
    private long mScreenshotChordVolumeUpKeyTime;
    private boolean mScreenshotChordVolumeUpKeyTriggered;
    ServiceConnection mScreenshotConnection;
    final Object mScreenshotLock;
    private final ScreenshotRunnable mScreenshotRunnable;
    final Runnable mScreenshotTimeout;
    boolean mSearchKeyShortcutPending;
    SearchManager mSearchManager;
    int mSeascapeRotation;
    private boolean mSecureDismissingKeyguard;
    final Object mServiceAquireLock;
    SettingsObserver mSettingsObserver;
    int mShortPressOnPowerBehavior;
    int mShortPressOnSleepBehavior;
    int mShortPressWindowBehavior;
    private LongSparseArray<IShortcutService> mShortcutKeyServices;
    ShortcutManager mShortcutManager;
    boolean mShowingDream;
    boolean mShowingLockscreen;
    boolean mShuttingDown;
    int mStableBottom;
    int mStableFullscreenBottom;
    int mStableFullscreenLeft;
    int mStableFullscreenRight;
    int mStableFullscreenTop;
    int mStableLeft;
    int mStableRight;
    int mStableTop;
    WindowState mStatusBar;
    private final StatusBarController mStatusBarController;
    int mStatusBarHeight;
    int mStatusBarLayer;
    StatusBarManagerInternal mStatusBarManagerInternal;
    IStatusBarService mStatusBarService;
    private Object mStkLock;
    BroadcastReceiver mStkUserActivityEnReceiver;
    boolean mSupportAutoRotation;
    private boolean mSupportLongPressPowerWhenNonInteractive;
    boolean mSystemBooted;
    int mSystemBottom;
    private SystemGesturesPointerEventListener mSystemGestures;
    int mSystemLeft;
    boolean mSystemReady;
    int mSystemRight;
    int mSystemTop;
    private final MutableBoolean mTmpBoolean;
    WindowState mTopDockedOpaqueOrDimmingWindowState;
    WindowState mTopDockedOpaqueWindowState;
    WindowState mTopDockedWindowState;
    WindowState mTopFullscreenOpaqueOrDimmingWindowState;
    int mTopFullscreenOpaqueWindowNavBarColor;
    WindowState mTopFullscreenOpaqueWindowState;
    boolean mTopIsFullscreen;
    boolean mTranslucentDecorEnabled;
    int mTriplePressOnPowerBehavior;
    volatile boolean mTvPictureInPictureVisible;
    int mUiMode;
    IUiModeManager mUiModeManager;
    int mUndockedHdmiRotation;
    int mUnrestrictedScreenHeight;
    int mUnrestrictedScreenLeft;
    int mUnrestrictedScreenTop;
    int mUnrestrictedScreenWidth;
    int mUpsideDownRotation;
    boolean mUseTvRouting;
    int mUserRotation;
    int mUserRotationMode;
    Vibrator mVibrator;
    long[] mVirtualKeyVibePattern;
    int mVoiceContentBottom;
    int mVoiceContentLeft;
    int mVoiceContentRight;
    int mVoiceContentTop;
    boolean mWakeGestureEnabledSetting;
    MyWakeGestureListener mWakeGestureListener;
    private WindowState mWinDismissingKeyguard;
    private WindowState mWinShowWhenLocked;
    IWindowManager mWindowManager;
    final Runnable mWindowManagerDrawCallback;
    boolean mWindowManagerDrawComplete;
    WindowManagerFuncs mWindowManagerFuncs;
    WindowManagerInternal mWindowManagerInternal;

    final /* synthetic */ class -int_finishPostLayoutPolicyLw__LambdaImpl0 implements Runnable {
        private /* synthetic */ boolean val$trusted;

        public /* synthetic */ -int_finishPostLayoutPolicyLw__LambdaImpl0(boolean z) {
            this.val$trusted = z;
        }

        public void run() {
            PhoneWindowManager.this.m10-com_android_server_policy_PhoneWindowManager_lambda$1(this.val$trusted);
        }
    }

    final /* synthetic */ class -void_systemReady__LambdaImpl0 implements OnShowingStateChangedCallback {
        public void onShowingStateChanged(boolean arg0) {
            PhoneWindowManager.this.-com_android_server_policy_PhoneWindowManager-mthref-0(arg0);
        }
    }

    private static class HdmiControl {
        private final HdmiPlaybackClient mClient;

        /* synthetic */ HdmiControl(HdmiPlaybackClient client, HdmiControl hdmiControl) {
            this(client);
        }

        private HdmiControl(HdmiPlaybackClient client) {
            this.mClient = client;
        }

        public void turnOnTv() {
            if (this.mClient != null) {
                this.mClient.oneTouchPlay(new OneTouchPlayCallback() {
                    public void onComplete(int result) {
                        if (result != 0) {
                            Log.w(PhoneWindowManager.TAG, "One touch play failed: " + result);
                        }
                    }
                });
            }
        }
    }

    final class HideNavInputEventReceiver extends InputEventReceiver {
        public HideNavInputEventReceiver(InputChannel inputChannel, Looper looper) {
            super(inputChannel, looper);
        }

        /* JADX WARNING: Missing block: B:25:0x0062, code:
            if (r2 == false) goto L_0x006b;
     */
        /* JADX WARNING: Missing block: B:26:0x0064, code:
            r12.this$0.mWindowManagerFuncs.reevaluateStatusBarVisibility();
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onInputEvent(InputEvent event) {
            try {
                if ((event instanceof MotionEvent) && (event.getSource() & 2) != 0 && ((MotionEvent) event).getAction() == 0) {
                    boolean changed = false;
                    synchronized (PhoneWindowManager.this.mWindowManagerFuncs.getWindowManagerLock()) {
                        if (PhoneWindowManager.this.mInputConsumer != null) {
                            int newVal = ((PhoneWindowManager.this.mResettingSystemUiFlags | 2) | 1) | 4;
                            if (PhoneWindowManager.this.mResettingSystemUiFlags != newVal) {
                                PhoneWindowManager.this.mResettingSystemUiFlags = newVal;
                                changed = true;
                            }
                            newVal = PhoneWindowManager.this.mForceClearedSystemUiFlags | 2;
                            if (PhoneWindowManager.this.mForceClearedSystemUiFlags != newVal) {
                                PhoneWindowManager.this.mForceClearedSystemUiFlags = newVal;
                                changed = true;
                                PhoneWindowManager.this.mHandler.postDelayed(PhoneWindowManager.this.mClearHideNavigationFlag, 1000);
                            }
                        }
                    }
                }
                finishInputEvent(event, false);
            } finally {
                finishInputEvent(event, false);
            }
        }
    }

    @OppoHook(level = OppoHookType.NEW_CLASS, note = "JianHui.Yu@Plf.SDK, 2015-01-06 : Add for Longshot", property = OppoRomType.ROM)
    private class LongshotRunnable implements Runnable {
        /* synthetic */ LongshotRunnable(PhoneWindowManager this$0, LongshotRunnable longshotRunnable) {
            this();
        }

        private LongshotRunnable() {
        }

        public void run() {
            PhoneWindowManager.this.takeLongshot();
        }
    }

    class MyOrientationListener extends WindowOrientationListener {
        private final Runnable mUpdateRotationRunnable = new Runnable() {
            public void run() {
                PhoneWindowManager.this.mPowerManagerInternal.powerHint(2, 0);
                PhoneWindowManager.this.updateRotation(false);
            }
        };

        MyOrientationListener(Context context, Handler handler) {
            super(context, handler);
        }

        public void onProposedRotationChanged(int rotation) {
            if (PhoneWindowManager.localLOGV) {
                Slog.v(PhoneWindowManager.TAG, "onProposedRotationChanged, rotation=" + rotation);
            }
            PhoneWindowManager.this.mHandler.post(this.mUpdateRotationRunnable);
        }
    }

    class MyWakeGestureListener extends WakeGestureListener {
        MyWakeGestureListener(Context context, Handler handler) {
            super(context, handler);
        }

        public void onWakeUp() {
            synchronized (PhoneWindowManager.this.mLock) {
                if (PhoneWindowManager.this.shouldEnableWakeGestureLp()) {
                    PhoneWindowManager.this.performHapticFeedbackLw(null, 1, false);
                    PhoneWindowManager.this.wakeUp(SystemClock.uptimeMillis(), PhoneWindowManager.this.mAllowTheaterModeWakeFromWakeGesture, "android.policy:GESTURE");
                }
            }
        }
    }

    private class PhoneWinHandler extends Handler {
        public PhoneWinHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 5:
                    if (PhoneWindowManager.DEBUG_WAKEUP) {
                        Slog.w(PhoneWindowManager.TAG, "Setting mKeyguardDrawComplete");
                    }
                    PhoneWindowManager.this.finishKeyguardDrawn();
                    return;
                case 6:
                    Slog.w(PhoneWindowManager.TAG, "Keyguard drawn timeout. Setting mKeyguardDrawComplete");
                    PhoneWindowManager.this.finishKeyguardDrawn();
                    return;
                case 7:
                    if (PhoneWindowManager.DEBUG_WAKEUP) {
                        Slog.w(PhoneWindowManager.TAG, "Setting mWindowManagerDrawComplete");
                    }
                    PhoneWindowManager.this.finishWindowsDrawn();
                    return;
                default:
                    return;
            }
        }
    }

    private class PolicyHandler extends Handler {
        /* synthetic */ PolicyHandler(PhoneWindowManager this$0, PolicyHandler policyHandler) {
            this();
        }

        private PolicyHandler() {
        }

        public void handleMessage(Message msg) {
            boolean z = true;
            PhoneWindowManager phoneWindowManager;
            switch (msg.what) {
                case 1:
                    PhoneWindowManager.this.enablePointerLocation();
                    return;
                case 2:
                    PhoneWindowManager.this.disablePointerLocation();
                    return;
                case 3:
                    PhoneWindowManager.this.dispatchMediaKeyWithWakeLock((KeyEvent) msg.obj);
                    return;
                case 4:
                    PhoneWindowManager.this.dispatchMediaKeyRepeatWithWakeLock((KeyEvent) msg.obj);
                    return;
                case 9:
                    PhoneWindowManager.this.showRecentApps(false, msg.arg1 != 0);
                    return;
                case 10:
                    PhoneWindowManager.this.showGlobalActionsInternal();
                    return;
                case 11:
                    PhoneWindowManager.this.handleHideBootMessage();
                    return;
                case 12:
                    phoneWindowManager = PhoneWindowManager.this;
                    if (msg.arg1 == 0) {
                        z = false;
                    }
                    phoneWindowManager.launchVoiceAssistWithWakeLock(z);
                    return;
                case 13:
                    PhoneWindowManager phoneWindowManager2 = PhoneWindowManager.this;
                    long longValue = ((Long) msg.obj).longValue();
                    if (msg.arg1 == 0) {
                        z = false;
                    }
                    phoneWindowManager2.powerPress(longValue, z, msg.arg2);
                    PhoneWindowManager.this.finishPowerKeyPress();
                    return;
                case 14:
                    PhoneWindowManager.this.powerLongPress();
                    return;
                case 15:
                    phoneWindowManager = PhoneWindowManager.this;
                    if (msg.arg1 == 0) {
                        z = false;
                    }
                    phoneWindowManager.updateDreamingSleepToken(z);
                    return;
                case 16:
                    WindowState targetBar = msg.arg1 == 0 ? PhoneWindowManager.this.mStatusBar : PhoneWindowManager.this.mNavigationBar;
                    if (targetBar != null) {
                        PhoneWindowManager.this.requestTransientBars(targetBar);
                        return;
                    }
                    return;
                case 17:
                    PhoneWindowManager.this.showTvPictureInPictureMenuInternal();
                    return;
                case 18:
                    PhoneWindowManager.this.backLongPress();
                    return;
                case 19:
                    PhoneWindowManager.this.disposeInputConsumer((InputConsumer) msg.obj);
                    return;
                case 20:
                    synchronized (PhoneWindowManager.this.mLock) {
                        HwShutdownRecord.getInstance().recordHwShutdownFlag();
                    }
                    return;
                case 21:
                    PhoneWindowManager.this.notifyMultiWindowFocusChanged(msg.arg1);
                    return;
                case 255:
                    if (PhoneWindowManager.this.mPowerKeyWakeLock.isHeld()) {
                        PhoneWindowManager.this.mPowerKeyWakeLock.release();
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    class ScreenLockTimeout implements Runnable {
        Bundle options;

        ScreenLockTimeout() {
        }

        public void run() {
            synchronized (this) {
                if (PhoneWindowManager.localLOGV) {
                    Log.v(PhoneWindowManager.TAG, "mScreenLockTimeout activating keyguard");
                }
                if (PhoneWindowManager.this.mKeyguardDelegate != null) {
                    PhoneWindowManager.this.mKeyguardDelegate.doKeyguardTimeout(this.options);
                }
                PhoneWindowManager.this.mLockScreenTimerActive = false;
                this.options = null;
            }
        }

        public void setLockOptions(Bundle options) {
            this.options = options;
        }
    }

    private class ScreenshotRunnable implements Runnable {
        private int mScreenshotType;

        /* synthetic */ ScreenshotRunnable(PhoneWindowManager this$0, ScreenshotRunnable screenshotRunnable) {
            this();
        }

        private ScreenshotRunnable() {
            this.mScreenshotType = 1;
        }

        public void setScreenshotType(int screenshotType) {
            this.mScreenshotType = screenshotType;
        }

        public void run() {
            PhoneWindowManager.this.takeScreenshot(this.mScreenshotType);
            SystemProperties.set("ctl.start", "gettpinfo");
        }
    }

    class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            ContentResolver resolver = PhoneWindowManager.this.mContext.getContentResolver();
            resolver.registerContentObserver(System.getUriFor("end_button_behavior"), false, this, -1);
            resolver.registerContentObserver(Secure.getUriFor("incall_power_button_behavior"), false, this, -1);
            resolver.registerContentObserver(Secure.getUriFor("wake_gesture_enabled"), false, this, -1);
            resolver.registerContentObserver(System.getUriFor("accelerometer_rotation"), false, this, -1);
            resolver.registerContentObserver(System.getUriFor("user_rotation"), false, this, -1);
            resolver.registerContentObserver(System.getUriFor("screen_off_timeout"), false, this, -1);
            resolver.registerContentObserver(System.getUriFor("pointer_location"), false, this, -1);
            resolver.registerContentObserver(Secure.getUriFor("default_input_method"), false, this, -1);
            resolver.registerContentObserver(Secure.getUriFor("immersive_mode_confirmations"), false, this, -1);
            resolver.registerContentObserver(Global.getUriFor("policy_control"), false, this, -1);
            resolver.registerContentObserver(Secure.getUriFor(PhoneWindowManager.HIDE_NAVIGATIONBAR_ENABLE), false, this, -1);
            PhoneWindowManager.this.updateSettings();
        }

        public void onChange(boolean selfChange) {
            PhoneWindowManager.this.updateSettings();
            PhoneWindowManager.this.updateRotation(false);
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.policy.PhoneWindowManager.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.policy.PhoneWindowManager.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.policy.PhoneWindowManager.<clinit>():void");
    }

    public PhoneWindowManager() {
        boolean z = true;
        this.mKeyguardDoShowLw = true;
        this.mHideStatusBarWhenLockScreen = false;
        this.mLock = new Object();
        this.isTouchFingerPrintSensor = false;
        this.mIsEnableKeyguardHide = false;
        this.mShuttingDown = false;
        this.mScreenOnCpuBoostHelper = null;
        this.mServiceAquireLock = new Object();
        this.mEnableShiftMenuBugReports = false;
        this.mKeyguard = null;
        this.mHideNavigationBar = false;
        this.mStatusBar = null;
        this.mNavigationBar = null;
        this.mHasNavigationBar = false;
        this.mNavigationBarCanMove = false;
        this.mNavigationBarPosition = 0;
        this.mNavigationBarHeightForRotationDefault = new int[4];
        this.mNavigationBarWidthForRotationDefault = new int[4];
        this.mNavigationBarHeightForRotationInCarMode = new int[4];
        this.mNavigationBarWidthForRotationInCarMode = new int[4];
        this.mShortcutKeyServices = new LongSparseArray();
        this.mEnableCarDockHomeCapture = true;
        this.mWindowManagerDrawCallback = new Runnable() {
            public void run() {
                if (PhoneWindowManager.DEBUG_WAKEUP) {
                    Slog.i(PhoneWindowManager.TAG, "All windows ready for display!");
                }
                PhoneWindowManager.this.mPhoneWinHandler.sendEmptyMessage(7);
            }
        };
        this.mKeyguardDrawnCallback = new DrawnListener() {
            public void onDrawn() {
                if (PhoneWindowManager.DEBUG_WAKEUP) {
                    Slog.d(PhoneWindowManager.TAG, "mKeyguardDelegate.ShowListener.onDrawn.");
                }
                PhoneWindowManager.this.mPhoneWinHandler.sendEmptyMessage(5);
            }
        };
        this.mLastInputMethodWindow = null;
        this.mLastInputMethodTargetWindow = null;
        this.mPendingWakeKey = -1;
        this.mLidState = -1;
        this.mCameraLensCoverState = -1;
        this.mDockMode = 0;
        this.mForceDefaultOrientation = false;
        this.mUserRotationMode = 0;
        this.mUserRotation = 0;
        this.mAllowAllRotations = -1;
        this.mOrientationSensorEnabled = false;
        this.mCurrentAppOrientation = -1;
        this.mHasSoftInput = false;
        this.mTranslucentDecorEnabled = true;
        this.mPointerLocationMode = 0;
        this.mResettingSystemUiFlags = 0;
        this.mForceClearedSystemUiFlags = 0;
        this.mNonDockedStackBounds = new Rect();
        this.mDockedStackBounds = new Rect();
        this.mLastNonDockedStackBounds = new Rect();
        this.mLastDockedStackBounds = new Rect();
        this.mLastFocusNeedsMenu = false;
        this.mInputConsumer = null;
        this.mAppsToBeHidden = new HashSet();
        this.mAppsThatDismissKeyguard = new HashSet();
        this.mNavBarOpacityMode = 0;
        this.mDismissKeyguard = 0;
        this.mLastExpand = this.mExpendBar;
        this.mLandscapeRotation = 0;
        this.mSeascapeRotation = 0;
        this.mPortraitRotation = 0;
        this.mUpsideDownRotation = 0;
        this.mOverscanLeft = 0;
        this.mOverscanTop = 0;
        this.mOverscanRight = 0;
        this.mOverscanBottom = 0;
        this.mFallbackActions = new SparseArray();
        this.mLogDecelerateInterpolator = new LogDecelerateInterpolator(100, 0);
        this.mTmpBoolean = new MutableBoolean(false);
        this.mLastHomeDownTimeDuringPF = 0;
        this.mNavigationBarEnableStatus = 0;
        this.mHDMIObserver = new UEventObserver() {
            public void onUEvent(UEvent event) {
                PhoneWindowManager.this.setHdmiPlugged(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(event.get("SWITCH_STATE")));
            }
        };
        this.mStatusBarController = new StatusBarController();
        this.mNavigationBarController = new BarController("NavigationBar", 134217728, 536870912, Integer.MIN_VALUE, 2, 134217728, 32768);
        this.mEndCallLongPress = new Runnable() {
            public void run() {
                PhoneWindowManager.this.mEndCallKeyHandled = true;
                if (!PhoneWindowManager.this.performHapticFeedbackLw(null, 0, false)) {
                    PhoneWindowManager.this.performAuditoryFeedbackForAccessibilityIfNeed();
                }
                PhoneWindowManager.this.showGlobalActionsInternal();
            }
        };
        this.mScreenshotRunnable = new ScreenshotRunnable(this, null);
        this.mHomeDoubleTapTimeoutRunnable = new Runnable() {
            public void run() {
                if (PhoneWindowManager.this.mHomeDoubleTapPending) {
                    PhoneWindowManager.this.mHomeDoubleTapPending = false;
                    PhoneWindowManager.this.handleShortPressOnHome();
                }
            }
        };
        this.bForceShowStatusBar = false;
        this.mClearHideNavigationFlag = new Runnable() {
            public void run() {
                synchronized (PhoneWindowManager.this.mWindowManagerFuncs.getWindowManagerLock()) {
                    PhoneWindowManager phoneWindowManager = PhoneWindowManager.this;
                    phoneWindowManager.mForceClearedSystemUiFlags &= -3;
                }
                PhoneWindowManager.this.mWindowManagerFuncs.reevaluateStatusBarVisibility();
            }
        };
        this.mHideNavInputEventReceiverFactory = new Factory() {
            public InputEventReceiver createInputEventReceiver(InputChannel inputChannel, Looper looper) {
                return new HideNavInputEventReceiver(inputChannel, looper);
            }
        };
        this.mScreenshotLock = new Object();
        this.mScreenshotConnection = null;
        this.mScreenshotTimeout = new Runnable() {
            public void run() {
                synchronized (PhoneWindowManager.this.mScreenshotLock) {
                    if (PhoneWindowManager.this.mScreenshotConnection != null) {
                        PhoneWindowManager.this.mContext.unbindService(PhoneWindowManager.this.mScreenshotConnection);
                        PhoneWindowManager.this.mScreenshotConnection = null;
                        PhoneWindowManager.this.notifyScreenshotError();
                    }
                }
            }
        };
        this.mProcessCpuTrackerRunnable = new ProcessCpuTrackerRunnable();
        this.mDockReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if ("android.intent.action.DOCK_EVENT".equals(intent.getAction())) {
                    PhoneWindowManager.this.mDockMode = intent.getIntExtra("android.intent.extra.DOCK_STATE", 0);
                } else {
                    try {
                        IUiModeManager uiModeService = Stub.asInterface(ServiceManager.getService("uimode"));
                        PhoneWindowManager.this.mUiMode = uiModeService.getCurrentModeType();
                    } catch (RemoteException e) {
                    }
                }
                PhoneWindowManager.this.updateRotation(true);
                synchronized (PhoneWindowManager.this.mLock) {
                    PhoneWindowManager.this.updateOrientationListenerLp();
                }
            }
        };
        this.mDreamReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if ("android.intent.action.DREAMING_STARTED".equals(intent.getAction())) {
                    Slog.v(PhoneWindowManager.TAG, "*** onDreamingStarted");
                    if (PhoneWindowManager.this.mKeyguardDelegate != null) {
                        PhoneWindowManager.this.mKeyguardDelegate.onDreamingStarted();
                    }
                } else if ("android.intent.action.DREAMING_STOPPED".equals(intent.getAction())) {
                    Slog.v(PhoneWindowManager.TAG, "*** onDreamingStopped");
                    if (PhoneWindowManager.this.mKeyguardDelegate != null) {
                        PhoneWindowManager.this.mKeyguardDelegate.onDreamingStopped();
                    }
                }
            }
        };
        this.mMultiuserReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if ("android.intent.action.USER_SWITCHED".equals(intent.getAction())) {
                    PhoneWindowManager.this.mSettingsObserver.onChange(false);
                    synchronized (PhoneWindowManager.this.mWindowManagerFuncs.getWindowManagerLock()) {
                        PhoneWindowManager.this.mLastSystemUiFlags = 0;
                        PhoneWindowManager.this.updateSystemUiVisibilityLw();
                    }
                }
            }
        };
        this.mEngineerModeHandleReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                Log.v(PhoneWindowManager.TAG, "mEngineerModeHandleReceiver -- onReceive -- entry");
                String action = intent.getAction();
                if (action.equals(PhoneWindowManager.ACTION_DISABLE_LIDCONTROLSSLEEP)) {
                    PhoneWindowManager.this.mLidControlsSleep = false;
                    Log.v(PhoneWindowManager.TAG, "Receive ACTION_DISABLE_LIDCONTROLSSLEEP");
                } else if (action.equals(PhoneWindowManager.ACTION_ENABLE_LIDCONTROLSSLEEP)) {
                    PhoneWindowManager.this.mLidControlsSleep = true;
                    Log.v(PhoneWindowManager.TAG, "Receive ACTION_ENABLE_LIDCONTROLSSLEEP");
                } else {
                    Log.v(PhoneWindowManager.TAG, "mEngineerModeHandleReceiver Receive invalid Intent");
                }
            }
        };
        this.mHiddenNavPanic = new Runnable() {
            /* JADX WARNING: Missing block: B:12:0x0030, code:
            return;
     */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void run() {
                synchronized (PhoneWindowManager.this.mWindowManagerFuncs.getWindowManagerLock()) {
                    if (PhoneWindowManager.this.isUserSetupComplete()) {
                        PhoneWindowManager.this.mPendingPanicGestureUptime = SystemClock.uptimeMillis();
                        if (!PhoneWindowManager.isNavBarEmpty(PhoneWindowManager.this.mLastSystemUiFlags)) {
                            PhoneWindowManager.this.mNavigationBarController.showTransient();
                        }
                    }
                }
            }
        };
        this.mBootMsgDialog = null;
        this.mScreenLockTimeout = new ScreenLockTimeout();
        this.mLastValidStatusBarTint = 0;
        this.mLastValidNavigationBarTint = 0;
        this.mLastNavigationBarColor = 0;
        this.mLastNavVisibility = 0;
        this.mIsAlarmBoot = isAlarmBoot();
        this.mIsShutDown = false;
        this.mPoweroffAlarmReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                Log.v(PhoneWindowManager.TAG, "mIpoEventReceiver -- onReceive -- entry");
                String action = intent.getAction();
                SystemProperties.set("sys.boot.reason", "0");
                PhoneWindowManager.this.mIsAlarmBoot = false;
                if (action.equals(PhoneWindowManager.NORMAL_SHUTDOWN_ACTION)) {
                    Log.v(PhoneWindowManager.TAG, "Receive NORMAL_SHUTDOWN_ACTION");
                    PhoneWindowManager.this.mIsShutDown = true;
                } else if (PhoneWindowManager.NORMAL_BOOT_ACTION.equals(action)) {
                    Log.v(PhoneWindowManager.TAG, "Receive NORMAL_BOOT_ACTION");
                    SystemProperties.set("service.bootanim.exit", "0");
                    SystemProperties.set("ctl.start", "bootanim");
                }
            }
        };
        this.mKeyDispatchLock = new Object();
        this.mIPOUserRotation = 0;
        this.mIsIpoShutDown = false;
        this.mIpoEventReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                Log.v(PhoneWindowManager.TAG, "mIpoEventReceiver -- onReceive -- entry");
                String action = intent.getAction();
                if (action.equals("android.intent.action.ACTION_SHUTDOWN_IPO")) {
                    Log.v(PhoneWindowManager.TAG, "Receive IPO_ENABLE");
                    PhoneWindowManager.this.mIsIpoShutDown = true;
                    PhoneWindowManager.this.ipoSystemShutdown();
                } else if (action.equals("android.intent.action.ACTION_BOOT_IPO")) {
                    Log.v(PhoneWindowManager.TAG, "Receive IPO_DISABLE");
                    PhoneWindowManager.this.ipoSystemBooted();
                    PhoneWindowManager.this.mIsIpoShutDown = false;
                } else {
                    Log.v(PhoneWindowManager.TAG, "Receive Fake Intent");
                }
            }
        };
        this.mIsStkUserActivityEnabled = false;
        this.mStkLock = new Object();
        this.mStkUserActivityEnReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Log.v(PhoneWindowManager.TAG, "mStkUserActivityEnReceiver -- onReceive -- entry");
                synchronized (PhoneWindowManager.this.mStkLock) {
                    if (action.equals(PhoneWindowManager.STK_USERACTIVITY_ENABLE)) {
                        if (PhoneWindowManager.DEBUG_INPUT) {
                            Log.v(PhoneWindowManager.TAG, "Receive STK_ENABLE");
                        }
                        boolean enabled = intent.getBooleanExtra("state", false);
                        if (enabled != PhoneWindowManager.this.mIsStkUserActivityEnabled) {
                            PhoneWindowManager.this.mIsStkUserActivityEnabled = enabled;
                        }
                    } else if (PhoneWindowManager.DEBUG_INPUT) {
                        Log.e(PhoneWindowManager.TAG, "Receive Fake Intent");
                    }
                    if (PhoneWindowManager.DEBUG_INPUT) {
                        Log.v(PhoneWindowManager.TAG, "mStkUserActivityEnReceiver -- onReceive -- exist " + PhoneWindowManager.this.mIsStkUserActivityEnabled);
                    }
                }
            }
        };
        this.mNotifyStk = new Runnable() {
            public void run() {
                PhoneWindowManager.this.mContext.sendBroadcast(new Intent(PhoneWindowManager.STK_USERACTIVITY));
            }
        };
        this.mScreenOffReason = -1;
        this.mKeyDispatcMode = 0;
        this.mKeyRemappingVolumeDownLongPress_Test = new Runnable() {
            public void run() {
                KeyEvent keyEvent = new KeyEvent(1, 4);
                InputManager inputManager = (InputManager) PhoneWindowManager.this.mContext.getSystemService("input");
                Log.d(PhoneWindowManager.TAG, ">>>>>>>> InjectEvent Start");
                inputManager.injectInputEvent(keyEvent, 2);
                try {
                    Log.d(PhoneWindowManager.TAG, "***** Sleeping.");
                    Thread.sleep(10000);
                    Log.d(PhoneWindowManager.TAG, "***** Waking up.");
                } catch (IllegalArgumentException e) {
                    Log.d(PhoneWindowManager.TAG, "IllegalArgumentException: ", e);
                } catch (SecurityException e2) {
                    Log.d(PhoneWindowManager.TAG, "SecurityException: ", e2);
                } catch (InterruptedException e3) {
                    Log.d(PhoneWindowManager.TAG, "InterruptedException: ", e3);
                }
                Log.d(PhoneWindowManager.TAG, "<<<<<<<< InjectEvent End");
            }
        };
        this.mKeyRemappingVolumeUpLongPress = new Runnable() {
            public void run() {
                PhoneWindowManager.this.showRecentApps(false);
                PhoneWindowManager.this.mKeyRemappingVolumeUpLongPressed = true;
            }
        };
        this.mKeyRemappingVolumeDownLongPress = new Runnable() {
            public void run() {
                PhoneWindowManager.this.keyRemappingSendFakeKeyEvent(0, 82);
                PhoneWindowManager.this.keyRemappingSendFakeKeyEvent(1, 82);
                PhoneWindowManager.this.mKeyRemappingVolumeDownLongPressed = true;
            }
        };
        this.mAppLaunchTimeEnabled = 1 == SystemProperties.getInt("ro.mtk_perf_response_time", 0);
        if ("no".equals(SystemProperties.get("ro.mtk_carrierexpress_pack", "no"))) {
            z = false;
        }
        this.isUspEnable = z;
        this.mLongshotRunnable = new LongshotRunnable(this, null);
        this.mIsMute = new AtomicBoolean(false);
        this.mRecentsLongPressDetected = false;
        this.mRecentsStartSplitSreen = new Runnable() {
            public void run() {
                if (!PhoneWindowManager.this.keyguardOn() && !PhoneWindowManager.this.stopLockTaskMode()) {
                    PhoneWindowManager.this.toggleSplitScreen();
                }
            }
        };
    }

    IStatusBarService getStatusBarService() {
        IStatusBarService iStatusBarService;
        synchronized (this.mServiceAquireLock) {
            if (this.mStatusBarService == null) {
                this.mStatusBarService = IStatusBarService.Stub.asInterface(ServiceManager.getService("statusbar"));
            }
            iStatusBarService = this.mStatusBarService;
        }
        return iStatusBarService;
    }

    StatusBarManagerInternal getStatusBarManagerInternal() {
        StatusBarManagerInternal statusBarManagerInternal;
        synchronized (this.mServiceAquireLock) {
            if (this.mStatusBarManagerInternal == null) {
                this.mStatusBarManagerInternal = (StatusBarManagerInternal) LocalServices.getService(StatusBarManagerInternal.class);
            }
            statusBarManagerInternal = this.mStatusBarManagerInternal;
        }
        return statusBarManagerInternal;
    }

    boolean needSensorRunningLp() {
        if (this.mSupportAutoRotation && (this.mCurrentAppOrientation == 4 || this.mCurrentAppOrientation == 10 || this.mCurrentAppOrientation == 7 || this.mCurrentAppOrientation == 6)) {
            return true;
        }
        if ((this.mCarDockEnablesAccelerometer && this.mDockMode == 2) || (this.mDeskDockEnablesAccelerometer && (this.mDockMode == 1 || this.mDockMode == 3 || this.mDockMode == 4))) {
            return true;
        }
        boolean isInSpecial = false;
        boolean isEnableDock = isDockable();
        try {
            String topApp = this.mActivityManager.getTopAppName().getClassName();
            if ("com.oppo.camera.Camera".equals(topApp) || "com.oppo.photoeditor.PhotoEditorActivity".equals(topApp) || "jp.naver.line.android.activity.main.MainActivity".equals(topApp) || "jp.naver.line.android.activity.SplashActivity".equals(topApp)) {
                Log.v(TAG, " InSpecial topApp = " + topApp);
                isInSpecial = true;
            }
        } catch (Exception e) {
            Slog.v(TAG, " getTopAppName exception e = " + e.toString());
        }
        if (this.mUserRotationMode == 0 && (this.mCurrentAppOrientation == -1 || this.mCurrentAppOrientation == 2 || this.mCurrentAppOrientation == 13 || isInSpecial)) {
            return true;
        }
        return isEnableDock && this.mUserRotationMode != 1;
    }

    public boolean isDockable() {
        boolean ret = false;
        try {
            return ActivityManagerNative.getDefault().isTopRunningTaskDockable();
        } catch (Exception e) {
            Slog.v(TAG, "isTopRunningTaskDockable error : " + e.toString());
            return ret;
        }
    }

    void updateOrientationListenerLp() {
        if (this.mOrientationListener.canDetectOrientation()) {
            if (localLOGV) {
                Slog.v(TAG, "mScreenOnEarly=" + this.mScreenOnEarly + ", mAwake=" + this.mAwake + ", mCurrentAppOrientation=" + this.mCurrentAppOrientation + ", mOrientationSensorEnabled=" + this.mOrientationSensorEnabled + ", mKeyguardDrawComplete=" + this.mKeyguardDrawComplete + ", mWindowManagerDrawComplete=" + this.mWindowManagerDrawComplete);
            }
            boolean disable = true;
            if (this.mScreenOnEarly && this.mAwake && this.mKeyguardDrawComplete && this.mWindowManagerDrawComplete && needSensorRunningLp()) {
                disable = false;
                if (!this.mOrientationSensorEnabled) {
                    this.mOrientationListener.enable();
                    if (localLOGV) {
                        Slog.v(TAG, "Enabling listeners");
                    }
                    this.mOrientationSensorEnabled = true;
                }
            }
            if (disable && this.mOrientationSensorEnabled) {
                this.mOrientationListener.disable();
                if (localLOGV) {
                    Slog.v(TAG, "Disabling listeners");
                }
                this.mOrientationSensorEnabled = false;
            }
        }
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "JianHui.Yu@Plf.SDK, 2016-05-20 : Modify for Longshot", property = OppoRomType.ROM)
    private void interceptPowerKeyDown(KeyEvent event, boolean interactive) {
        boolean z;
        if (DEBUG_WAKEUP) {
            Slog.i(TAG, "interceptPowerKeyDown");
        }
        if (!this.mPowerKeyWakeLock.isHeld()) {
            this.mPowerKeyWakeLock.acquire();
            this.mHandler.removeMessages(255);
            this.mHandler.sendEmptyMessageDelayed(255, 120000);
        }
        if (this.mPowerKeyPressCounter != 0) {
            this.mHandler.removeMessages(13);
        }
        if (this.mImmersiveModeConfirmation.onPowerKeyDown(interactive, SystemClock.elapsedRealtime(), isImmersiveMode(this.mLastSystemUiFlags), isNavBarEmpty(this.mLastSystemUiFlags))) {
            this.mHandler.post(this.mHiddenNavPanic);
        }
        if (DEBUG_WAKEUP) {
            Slog.i(TAG, "interceptPowerKeyDown Detect panic done");
        }
        if (interactive && !this.mScreenshotChordPowerKeyTriggered && (event.getFlags() & 1024) == 0) {
            this.mScreenshotChordPowerKeyTriggered = true;
            this.mScreenshotChordPowerKeyTime = event.getDownTime();
            interceptScreenshotChord();
            interceptLongshotChord();
        }
        if (DEBUG_WAKEUP) {
            Slog.i(TAG, "interceptPowerKeyDown detect screenshot chord done");
        }
        TelecomManager telecomManager = getTelecommService();
        if (DEBUG_WAKEUP) {
            Slog.i(TAG, "interceptPowerKeyDown getTelecommService done");
        }
        boolean hungUp = interceptPowerKeyForTelephone(event, interactive);
        if (DEBUG_WAKEUP) {
            Slog.i(TAG, "interceptPowerKeyDown detect hangup done");
        }
        if (AudioSystem.isStreamActive(5, 0)) {
            disableNotificationAlert();
        }
        if (DEBUG_WAKEUP) {
            Slog.i(TAG, "interceptPowerKeyDown disableNotificationAlert done");
        }
        if (hungUp || this.mScreenshotChordVolumeDownKeyTriggered || this.mScreenshotChordVolumeUpKeyTriggered) {
            z = true;
        } else {
            z = false;
        }
        this.mPowerKeyHandled = z;
        Message msg;
        if (this.mPowerKeyHandled) {
            Log.d(TAG, "interceptPowerKeyDown,  mPowerKeyHandled = " + this.mPowerKeyHandled + ", hungUp = " + hungUp + ", mScreenshotChordVolumeDownKeyTriggered  = " + this.mScreenshotChordVolumeDownKeyTriggered + ", mScreenshotChordVolumeUpKeyTriggered = " + this.mScreenshotChordVolumeUpKeyTriggered + ", currentKeyCode = " + event.getKeyCode());
        } else if (!interactive) {
            wakeUpFromPowerKey(event.getDownTime());
            if (this.mSupportLongPressPowerWhenNonInteractive && hasLongPressOnPowerBehavior()) {
                msg = this.mHandler.obtainMessage(14);
                msg.setAsynchronous(true);
                this.mHandler.sendMessageDelayed(msg, ViewConfiguration.get(this.mContext).getDeviceGlobalActionKeyTimeout());
                this.mBeganFromNonInteractive = true;
            } else if (getMaxMultiPressPowerCount() <= 1) {
                this.mPowerKeyHandled = true;
            } else {
                this.mBeganFromNonInteractive = true;
            }
        } else if (hasLongPressOnPowerBehavior()) {
            msg = this.mHandler.obtainMessage(14);
            msg.setAsynchronous(true);
            this.mHandler.sendMessageDelayed(msg, 2500);
        }
        synchronized (this.mLock) {
            startHwShutdownDectect();
        }
    }

    private void interceptPowerKeyUp(KeyEvent event, boolean interactive, boolean canceled) {
        int i = 0;
        boolean handled = !canceled ? this.mPowerKeyHandled : true;
        this.mScreenshotChordPowerKeyTriggered = false;
        cancelPendingScreenshotChordAction();
        cancelPendingPowerKeyAction();
        synchronized (this.mLock) {
            clearHwShutdownDectect();
        }
        if (!handled) {
            this.mPowerKeyPressCounter++;
            int maxCount = getMaxMultiPressPowerCount();
            long eventTime = event.getDownTime();
            if (this.mPowerKeyPressCounter < maxCount) {
                Handler handler = this.mHandler;
                if (interactive) {
                    i = 1;
                }
                Message msg = handler.obtainMessage(13, i, this.mPowerKeyPressCounter, Long.valueOf(eventTime));
                msg.setAsynchronous(true);
                this.mHandler.sendMessageDelayed(msg, (long) ViewConfiguration.getDoubleTapTimeout());
                return;
            }
            powerPress(eventTime, interactive, this.mPowerKeyPressCounter);
        }
        finishPowerKeyPress();
    }

    private void finishPowerKeyPress() {
        this.mBeganFromNonInteractive = false;
        this.mPowerKeyPressCounter = 0;
        this.mHandler.removeMessages(255);
        if (this.mPowerKeyWakeLock.isHeld()) {
            this.mPowerKeyWakeLock.release();
        }
    }

    private void cancelPendingPowerKeyAction() {
        if (!this.mPowerKeyHandled) {
            this.mPowerKeyHandled = true;
            this.mHandler.removeMessages(14);
        }
    }

    private void cancelPendingBackKeyAction() {
        if (!this.mBackKeyHandled) {
            this.mBackKeyHandled = true;
            this.mHandler.removeMessages(18);
        }
    }

    private void powerPress(long eventTime, boolean interactive, int count) {
        if (!this.mScreenOnEarly || this.mScreenOnFully) {
            if (count != 2) {
                if (count != 3) {
                    if (interactive && !this.mBeganFromNonInteractive) {
                        this.mScreenOnCpuBoostHelper.acquireCpuBoost(1000);
                        switch (this.mShortPressOnPowerBehavior) {
                            case 1:
                                this.mPowerManager.goToSleep(eventTime, 4, 0);
                                break;
                            case 2:
                                this.mPowerManager.goToSleep(eventTime, 4, 1);
                                break;
                            case 3:
                                this.mPowerManager.goToSleep(eventTime, 4, 1);
                                launchHomeFromHotKey();
                                break;
                            case 4:
                                launchHomeFromHotKey(true, false);
                                break;
                        }
                    }
                }
                powerMultiPressAction(eventTime, interactive, this.mTriplePressOnPowerBehavior);
            } else {
                powerMultiPressAction(eventTime, interactive, this.mDoublePressOnPowerBehavior);
            }
            return;
        }
        Slog.i(TAG, "Suppressed redundant power key press while already in the process of turning the screen on.");
    }

    private void powerMultiPressAction(long eventTime, boolean interactive, int behavior) {
        switch (behavior) {
            case 1:
                if (!isUserSetupComplete()) {
                    Slog.i(TAG, "Ignoring toggling theater mode - device not setup.");
                    return;
                } else if (isTheaterModeEnabled()) {
                    Slog.i(TAG, "Toggling theater mode off.");
                    Global.putInt(this.mContext.getContentResolver(), "theater_mode_on", 0);
                    if (!interactive) {
                        wakeUpFromPowerKey(eventTime);
                        return;
                    }
                    return;
                } else {
                    Slog.i(TAG, "Toggling theater mode on.");
                    Global.putInt(this.mContext.getContentResolver(), "theater_mode_on", 1);
                    if (this.mGoToSleepOnButtonPressTheaterMode && interactive) {
                        this.mPowerManager.goToSleep(eventTime, 4, 0);
                        return;
                    }
                    return;
                }
            case 2:
                Slog.i(TAG, "Starting brightness boost.");
                if (!interactive) {
                    wakeUpFromPowerKey(eventTime);
                }
                this.mPowerManager.boostScreenBrightness(eventTime);
                return;
            default:
                return;
        }
    }

    private int getMaxMultiPressPowerCount() {
        if (this.mTriplePressOnPowerBehavior != 0) {
            return 3;
        }
        if (this.mDoublePressOnPowerBehavior != 0) {
            return 2;
        }
        return 1;
    }

    private void powerLongPress() {
        boolean z = true;
        int behavior = getResolvedLongPressOnPowerBehavior();
        switch (behavior) {
            case 1:
                this.mPowerKeyHandled = true;
                if (!this.mScreenOnFully) {
                    Log.i(TAG, "LONG_PRESS_POWER_GLOBAL_ACTIONS. screen is not on!");
                    break;
                }
                if (!performHapticFeedbackLw(null, 0, false)) {
                    performAuditoryFeedbackForAccessibilityIfNeed();
                }
                if (this.mContext.getPackageManager().hasSystemFeature("oppo.disable.small.window.leather") || !isLeatherModeEnabled(this.mContext) || this.mLidState != 0) {
                    Log.i(TAG, "showGlobalActionsInternal");
                    showGlobalActionsInternal();
                    break;
                }
                sendCloseSystemWindows(SYSTEM_DIALOG_REASON_GLOBAL_ACTIONS);
                Log.i(TAG, "mLidState == LID_CLOSED! sendCloseSystemWindows and Ignore showGlobalActionsDialog!");
                return;
                break;
            case 2:
            case 3:
                this.mPowerKeyHandled = true;
                performHapticFeedbackLw(null, 0, false);
                sendCloseSystemWindows(SYSTEM_DIALOG_REASON_GLOBAL_ACTIONS);
                WindowManagerFuncs windowManagerFuncs = this.mWindowManagerFuncs;
                if (behavior != 2) {
                    z = false;
                }
                windowManagerFuncs.shutdown(z);
                break;
        }
    }

    private void backLongPress() {
        this.mBackKeyHandled = true;
        switch (this.mLongPressOnBackBehavior) {
            case 1:
                startActivityAsUser(new Intent("android.intent.action.VOICE_ASSIST"), UserHandle.CURRENT_OR_SELF);
                return;
            default:
                return;
        }
    }

    private void disposeInputConsumer(InputConsumer inputConsumer) {
        if (inputConsumer != null) {
            inputConsumer.dismiss();
        }
    }

    private void sleepPress(long eventTime) {
        if (this.mShortPressOnSleepBehavior == 1) {
            launchHomeFromHotKey(false, true);
        }
    }

    private void sleepRelease(long eventTime) {
        switch (this.mShortPressOnSleepBehavior) {
            case 0:
            case 1:
                Slog.i(TAG, "sleepRelease() calling goToSleep(GO_TO_SLEEP_REASON_SLEEP_BUTTON)");
                this.mPowerManager.goToSleep(eventTime, 6, 0);
                return;
            default:
                return;
        }
    }

    private int getResolvedLongPressOnPowerBehavior() {
        if (FactoryTest.isLongPressOnPowerOffEnabled()) {
            return 3;
        }
        return this.mLongPressOnPowerBehavior;
    }

    private boolean hasLongPressOnPowerBehavior() {
        return getResolvedLongPressOnPowerBehavior() != 0;
    }

    private boolean hasLongPressOnBackBehavior() {
        return this.mLongPressOnBackBehavior != 0;
    }

    private void interceptScreenshotChord() {
        if (this.mScreenshotChordEnabled && this.mScreenshotChordVolumeDownKeyTriggered && this.mScreenshotChordPowerKeyTriggered && !this.mScreenshotChordVolumeUpKeyTriggered) {
            long now = SystemClock.uptimeMillis();
            if (now <= this.mScreenshotChordVolumeDownKeyTime + SCREENSHOT_CHORD_DEBOUNCE_DELAY_MILLIS && now <= this.mScreenshotChordPowerKeyTime + SCREENSHOT_CHORD_DEBOUNCE_DELAY_MILLIS) {
                this.mScreenshotChordVolumeDownKeyConsumed = true;
                cancelPendingPowerKeyAction();
                this.mScreenshotRunnable.setScreenshotType(1);
                this.mHandler.postDelayed(this.mScreenshotRunnable, getScreenshotChordLongPressDelay());
            }
        }
    }

    private long getScreenshotChordLongPressDelay() {
        if (this.mKeyguardDelegate.isShowing()) {
            return (long) (((float) ViewConfiguration.get(this.mContext).getDeviceGlobalActionKeyTimeout()) * KEYGUARD_SCREENSHOT_CHORD_DELAY_MULTIPLIER);
        }
        return ViewConfiguration.get(this.mContext).getDeviceGlobalActionKeyTimeout();
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "JianHui.Yu@Plf.SDK, 2015-01-06 : Modify for Longshot", property = OppoRomType.ROM)
    private void cancelPendingScreenshotChordAction() {
        this.mHandler.removeCallbacks(this.mScreenshotRunnable);
        cancelPendingLongshotChordAction();
    }

    public void showGlobalActions() {
        this.mHandler.removeMessages(10);
        this.mHandler.sendEmptyMessage(10);
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "Xiaokang.Feng@Plf.SDK, use oppo global actions", property = OppoRomType.ROM)
    void showGlobalActionsInternal() {
        sendCloseSystemWindows(SYSTEM_DIALOG_REASON_GLOBAL_ACTIONS);
        if (this.mGlobalActions == null) {
            this.mGlobalActions = new OppoGlobalActions(this.mContext, this.mWindowManagerFuncs);
        }
        boolean keyguardShowing = isKeyguardShowingAndNotOccluded();
        this.mGlobalActions.showDialog(keyguardShowing, isDeviceProvisioned());
        if (keyguardShowing) {
            this.mPowerManager.userActivity(SystemClock.uptimeMillis(), false);
        }
    }

    boolean isDeviceProvisioned() {
        if (Global.getInt(this.mContext.getContentResolver(), "device_provisioned", 0) != 0) {
            return true;
        }
        return false;
    }

    boolean isUserSetupComplete() {
        return Secure.getIntForUser(this.mContext.getContentResolver(), "user_setup_complete", 0, -2) != 0;
    }

    private void handleShortPressOnHome() {
        getHdmiControl().turnOnTv();
        if (this.mDreamManagerInternal == null || !this.mDreamManagerInternal.isDreaming()) {
            launchHomeFromHotKey();
        } else {
            this.mDreamManagerInternal.stopDream(false);
        }
    }

    private HdmiControl getHdmiControl() {
        if (this.mHdmiControl == null) {
            HdmiControlManager manager = (HdmiControlManager) this.mContext.getSystemService("hdmi_control");
            HdmiPlaybackClient client = null;
            if (manager != null) {
                client = manager.getPlaybackClient();
            }
            this.mHdmiControl = new HdmiControl(client, null);
        }
        return this.mHdmiControl;
    }

    private void handleLongPressOnHome(int deviceId) {
        if (this.mLongPressOnHomeBehavior != 0) {
            this.mHomeConsumed = true;
            performHapticFeedbackLw(null, 0, false);
            switch (this.mLongPressOnHomeBehavior) {
                case 1:
                    toggleRecentApps();
                    break;
                case 2:
                    launchAssistAction(null, deviceId);
                    break;
                default:
                    Log.w(TAG, "Undefined home long press behavior: " + this.mLongPressOnHomeBehavior);
                    break;
            }
        }
    }

    private void handleDoubleTapOnHome() {
        if (this.mDoubleTapOnHomeBehavior == 1) {
            this.mHomeConsumed = true;
            toggleRecentApps();
        }
    }

    private void showTvPictureInPictureMenu(KeyEvent event) {
        if (DEBUG_INPUT) {
            Log.d(TAG, "showTvPictureInPictureMenu event=" + event);
        }
        this.mHandler.removeMessages(17);
        Message msg = this.mHandler.obtainMessage(17);
        msg.setAsynchronous(true);
        msg.sendToTarget();
    }

    private void showTvPictureInPictureMenuInternal() {
        StatusBarManagerInternal statusbar = getStatusBarManagerInternal();
        if (statusbar != null) {
            statusbar.showTvPictureInPictureMenu();
        }
    }

    private boolean isRoundWindow() {
        return this.mContext.getResources().getConfiguration().isScreenRound();
    }

    public void init(Context context, IWindowManager windowManager, WindowManagerFuncs windowManagerFuncs) {
        boolean z;
        this.mContext = context;
        this.mWindowManager = windowManager;
        this.mWindowManagerFuncs = windowManagerFuncs;
        this.mWindowManagerInternal = (WindowManagerInternal) LocalServices.getService(WindowManagerInternal.class);
        this.mActivityManagerInternal = (ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class);
        this.mInputManagerInternal = (InputManagerInternal) LocalServices.getService(InputManagerInternal.class);
        this.mDreamManagerInternal = (DreamManagerInternal) LocalServices.getService(DreamManagerInternal.class);
        this.mDisplayManagerInternal = (DisplayManagerInternal) LocalServices.getService(DisplayManagerInternal.class);
        this.isTouchFingerPrintSensor = context.getPackageManager().hasSystemFeature("oppo.front.touch.fingerprint.sensor");
        this.mIsEnableKeyguardHide = this.mContext.getPackageManager().hasSystemFeature("oppo.fingerprint.enable.keyguard.hide");
        this.mScreenOnCpuBoostHelper = new ScreenOnCpuBoostHelper();
        this.mPowerManagerInternal = (PowerManagerInternal) LocalServices.getService(PowerManagerInternal.class);
        this.mAppOpsManager = (AppOpsManager) this.mContext.getSystemService("appops");
        this.mHasFeatureWatch = this.mContext.getPackageManager().hasSystemFeature("android.hardware.type.watch");
        this.mActivityManager = ActivityManagerNative.getDefault();
        boolean burnInProtectionEnabled = context.getResources().getBoolean(17957026);
        boolean burnInProtectionDevMode = SystemProperties.getBoolean("persist.debug.force_burn_in", false);
        if (burnInProtectionEnabled || burnInProtectionDevMode) {
            int minHorizontal;
            int maxHorizontal;
            int minVertical;
            int maxVertical;
            int maxRadius;
            if (burnInProtectionDevMode) {
                minHorizontal = -8;
                maxHorizontal = 8;
                minVertical = -8;
                maxVertical = -4;
                if (isRoundWindow()) {
                    maxRadius = 6;
                } else {
                    maxRadius = -1;
                }
            } else {
                Resources resources = context.getResources();
                minHorizontal = resources.getInteger(17694875);
                maxHorizontal = resources.getInteger(17694876);
                minVertical = resources.getInteger(17694877);
                maxVertical = resources.getInteger(17694878);
                maxRadius = resources.getInteger(17694874);
            }
            this.mBurnInProtectionHelper = new BurnInProtectionHelper(context, minHorizontal, maxHorizontal, minVertical, maxVertical, maxRadius);
        }
        this.mHandler = new PolicyHandler(this, null);
        this.mPhoneWinHandlerThread = new ServiceThread("PhoneWinHandlerThread", -4, false);
        this.mPhoneWinHandlerThread.start();
        this.mPhoneWinHandler = new PhoneWinHandler(this.mPhoneWinHandlerThread.getLooper());
        this.mWakeGestureListener = new MyWakeGestureListener(this.mContext, this.mHandler);
        this.mOrientationListener = new MyOrientationListener(this.mContext, this.mHandler);
        try {
            this.mOrientationListener.setCurrentRotation(windowManager.getRotation());
        } catch (RemoteException e) {
        }
        this.mSettingsObserver = new SettingsObserver(this.mHandler);
        this.mSettingsObserver.observe();
        this.mShortcutManager = new ShortcutManager(context);
        this.mUiMode = context.getResources().getInteger(17694793);
        this.mHomeIntent = new Intent("android.intent.action.MAIN", null);
        this.mHomeIntent.addCategory("android.intent.category.HOME");
        this.mHomeIntent.addFlags(270532608);
        this.mEnableCarDockHomeCapture = context.getResources().getBoolean(17956923);
        this.mCarDockIntent = new Intent("android.intent.action.MAIN", null);
        this.mCarDockIntent.addCategory("android.intent.category.CAR_DOCK");
        this.mCarDockIntent.addFlags(270532608);
        this.mDeskDockIntent = new Intent("android.intent.action.MAIN", null);
        this.mDeskDockIntent.addCategory("android.intent.category.DESK_DOCK");
        this.mDeskDockIntent.addFlags(270532608);
        this.mPowerManager = (PowerManager) context.getSystemService("power");
        this.mBroadcastWakeLock = this.mPowerManager.newWakeLock(1, "PhoneWindowManager.mBroadcastWakeLock");
        this.mPowerKeyWakeLock = this.mPowerManager.newWakeLock(1, "PhoneWindowManager.mPowerKeyWakeLock");
        this.mEnableShiftMenuBugReports = LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(SystemProperties.get("ro.debuggable"));
        this.mSupportAutoRotation = this.mContext.getResources().getBoolean(17956916);
        this.mLidOpenRotation = readRotation(17694785);
        this.mCarDockRotation = readRotation(17694790);
        this.mDeskDockRotation = readRotation(17694788);
        this.mUndockedHdmiRotation = readRotation(17694792);
        this.mCarDockEnablesAccelerometer = this.mContext.getResources().getBoolean(17956922);
        this.mDeskDockEnablesAccelerometer = this.mContext.getResources().getBoolean(17956921);
        this.mLidKeyboardAccessibility = this.mContext.getResources().getInteger(17694786);
        this.mLidNavigationAccessibility = this.mContext.getResources().getInteger(17694787);
        this.mLidControlsScreenLock = this.mContext.getResources().getBoolean(17956919);
        this.mLidControlsSleep = this.mContext.getResources().getBoolean(17956920);
        this.mTranslucentDecorEnabled = this.mContext.getResources().getBoolean(17956935);
        this.mAllowTheaterModeWakeFromKey = this.mContext.getResources().getBoolean(17956908);
        if (this.mAllowTheaterModeWakeFromKey) {
            z = true;
        } else {
            z = this.mContext.getResources().getBoolean(17956907);
        }
        this.mAllowTheaterModeWakeFromPowerKey = z;
        this.mAllowTheaterModeWakeFromMotion = this.mContext.getResources().getBoolean(17956909);
        this.mAllowTheaterModeWakeFromMotionWhenNotDreaming = this.mContext.getResources().getBoolean(17956910);
        this.mAllowTheaterModeWakeFromCameraLens = this.mContext.getResources().getBoolean(17956906);
        this.mAllowTheaterModeWakeFromLidSwitch = this.mContext.getResources().getBoolean(17956911);
        this.mAllowTheaterModeWakeFromWakeGesture = this.mContext.getResources().getBoolean(17956905);
        this.mGoToSleepOnButtonPressTheaterMode = this.mContext.getResources().getBoolean(17956914);
        this.mSupportLongPressPowerWhenNonInteractive = this.mContext.getResources().getBoolean(17956915);
        this.mLongPressOnBackBehavior = this.mContext.getResources().getInteger(17694799);
        this.mShortPressOnPowerBehavior = this.mContext.getResources().getInteger(17694800);
        this.mLongPressOnPowerBehavior = this.mContext.getResources().getInteger(17694798);
        this.mDoublePressOnPowerBehavior = this.mContext.getResources().getInteger(17694801);
        this.mTriplePressOnPowerBehavior = this.mContext.getResources().getInteger(17694802);
        this.mShortPressOnSleepBehavior = this.mContext.getResources().getInteger(17694803);
        this.mUseTvRouting = AudioSystem.getPlatformType(this.mContext) == 2;
        readConfigurationDependentBehaviors();
        this.mAccessibilityManager = (AccessibilityManager) context.getSystemService("accessibility");
        IntentFilter filter = new IntentFilter();
        filter.addAction(UiModeManager.ACTION_ENTER_CAR_MODE);
        filter.addAction(UiModeManager.ACTION_EXIT_CAR_MODE);
        filter.addAction(UiModeManager.ACTION_ENTER_DESK_MODE);
        filter.addAction(UiModeManager.ACTION_EXIT_DESK_MODE);
        filter.addAction("android.intent.action.DOCK_EVENT");
        Intent intent = context.registerReceiver(this.mDockReceiver, filter);
        if (intent != null) {
            this.mDockMode = intent.getIntExtra("android.intent.extra.DOCK_STATE", 0);
        }
        IntentFilter ipoEventFilter = new IntentFilter();
        ipoEventFilter.addAction("android.intent.action.ACTION_SHUTDOWN_IPO");
        ipoEventFilter.addAction("android.intent.action.ACTION_BOOT_IPO");
        context.registerReceiver(this.mIpoEventReceiver, ipoEventFilter);
        IntentFilter poweroffAlarmFilter = new IntentFilter();
        poweroffAlarmFilter.addAction(NORMAL_SHUTDOWN_ACTION);
        poweroffAlarmFilter.addAction(NORMAL_BOOT_ACTION);
        context.registerReceiver(this.mPoweroffAlarmReceiver, poweroffAlarmFilter);
        IntentFilter stkUserActivityFilter = new IntentFilter();
        stkUserActivityFilter.addAction(STK_USERACTIVITY_ENABLE);
        context.registerReceiver(this.mStkUserActivityEnReceiver, stkUserActivityFilter);
        filter = new IntentFilter();
        filter.addAction("android.intent.action.DREAMING_STARTED");
        filter.addAction("android.intent.action.DREAMING_STOPPED");
        context.registerReceiver(this.mDreamReceiver, filter);
        context.registerReceiver(this.mMultiuserReceiver, new IntentFilter("android.intent.action.USER_SWITCHED"));
        IntentFilter engineermodeFilter = new IntentFilter();
        engineermodeFilter.addAction(ACTION_DISABLE_LIDCONTROLSSLEEP);
        engineermodeFilter.addAction(ACTION_ENABLE_LIDCONTROLSSLEEP);
        context.registerReceiver(this.mEngineerModeHandleReceiver, engineermodeFilter);
        this.mSystemGestures = new SystemGesturesPointerEventListener(context, new Callbacks() {
            public void onSwipeFromTop() {
                if (!(isGestureIsolated() || PhoneWindowManager.this.mStatusBar == null)) {
                    PhoneWindowManager.this.requestTransientBars(PhoneWindowManager.this.mStatusBar);
                }
            }

            public void onSwipeFromBottom() {
                if (!(isGestureIsolated() || PhoneWindowManager.this.mNavigationBar == null || PhoneWindowManager.this.mNavigationBarPosition != 0)) {
                    PhoneWindowManager.this.requestTransientBars(PhoneWindowManager.this.mNavigationBar);
                }
            }

            public void onSwipeFromRight() {
                if (!(isGestureIsolated() || PhoneWindowManager.this.mNavigationBar == null || PhoneWindowManager.this.mNavigationBarPosition != 1)) {
                    PhoneWindowManager.this.requestTransientBars(PhoneWindowManager.this.mNavigationBar);
                }
            }

            public void onSwipeFromLeft() {
                if (!(isGestureIsolated() || PhoneWindowManager.this.mNavigationBar == null || PhoneWindowManager.this.mNavigationBarPosition != 2)) {
                    PhoneWindowManager.this.requestTransientBars(PhoneWindowManager.this.mNavigationBar);
                }
            }

            public void onFling(int duration) {
                if (PhoneWindowManager.this.mPowerManagerInternal != null) {
                    PhoneWindowManager.this.mPowerManagerInternal.powerHint(2, duration);
                }
            }

            public void onDebug() {
            }

            public void onDown() {
                PhoneWindowManager.this.mOrientationListener.onTouchStart();
            }

            public void onUpOrCancel() {
                PhoneWindowManager.this.mOrientationListener.onTouchEnd();
            }

            public void onMouseHoverAtTop() {
                if (!isGestureIsolated()) {
                    PhoneWindowManager.this.mHandler.removeMessages(16);
                    Message msg = PhoneWindowManager.this.mHandler.obtainMessage(16);
                    msg.arg1 = 0;
                    PhoneWindowManager.this.mHandler.sendMessageDelayed(msg, 500);
                }
            }

            public void onMouseHoverAtBottom() {
                if (!isGestureIsolated()) {
                    PhoneWindowManager.this.mHandler.removeMessages(16);
                    Message msg = PhoneWindowManager.this.mHandler.obtainMessage(16);
                    msg.arg1 = 1;
                    PhoneWindowManager.this.mHandler.sendMessageDelayed(msg, 500);
                }
            }

            public void onMouseLeaveFromEdge() {
                PhoneWindowManager.this.mHandler.removeMessages(16);
            }

            private boolean isGestureIsolated() {
                WindowState win = PhoneWindowManager.this.mFocusedWindow != null ? PhoneWindowManager.this.mFocusedWindow : PhoneWindowManager.this.mTopFullscreenOpaqueWindowState;
                if (win == null || (win.getSystemUiVisibility() & 16777216) == 0) {
                    return false;
                }
                return true;
            }
        });
        this.mImmersiveModeConfirmation = new ImmersiveModeConfirmation(this.mContext);
        this.mWindowManagerFuncs.registerPointerEventListener(this.mSystemGestures);
        this.mVibrator = (Vibrator) context.getSystemService("vibrator");
        this.mLongPressVibePattern = getLongIntArray(this.mContext.getResources(), 17236002);
        this.mVirtualKeyVibePattern = getLongIntArray(this.mContext.getResources(), 17236003);
        this.mKeyboardTapVibePattern = getLongIntArray(this.mContext.getResources(), 17236004);
        this.mClockTickVibePattern = getLongIntArray(this.mContext.getResources(), 17236005);
        this.mCalendarDateVibePattern = getLongIntArray(this.mContext.getResources(), 17236006);
        this.mSafeModeDisabledVibePattern = getLongIntArray(this.mContext.getResources(), 17236007);
        this.mSafeModeEnabledVibePattern = getLongIntArray(this.mContext.getResources(), 17236008);
        this.mContextClickVibePattern = getLongIntArray(this.mContext.getResources(), 17236010);
        this.mScreenshotChordEnabled = this.mContext.getResources().getBoolean(17956903);
        this.mGlobalKeyManager = new GlobalKeyManager(this.mContext);
        initializeHdmiState();
        if (!this.mPowerManager.isInteractive()) {
            startedGoingToSleep(2);
            finishedGoingToSleep(2);
        }
        this.mWindowManagerInternal.registerAppTransitionListener(this.mStatusBarController.getAppTransitionListener());
        DEBUG_WAKEUP = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    }

    private void readConfigurationDependentBehaviors() {
        Resources res = this.mContext.getResources();
        this.mLongPressOnHomeBehavior = res.getInteger(17694816);
        if (isExpROM()) {
            this.mLongPressOnHomeBehavior = 2;
        }
        if (this.mLongPressOnHomeBehavior < 0 || this.mLongPressOnHomeBehavior > 2) {
            this.mLongPressOnHomeBehavior = 0;
        }
        this.mDoubleTapOnHomeBehavior = res.getInteger(17694817);
        if (this.mDoubleTapOnHomeBehavior < 0 || this.mDoubleTapOnHomeBehavior > 1) {
            this.mDoubleTapOnHomeBehavior = 0;
        }
        this.mShortPressWindowBehavior = 0;
        if (this.mContext.getPackageManager().hasSystemFeature("android.software.picture_in_picture")) {
            this.mShortPressWindowBehavior = 1;
        }
        this.mNavBarOpacityMode = res.getInteger(17694884);
    }

    private static boolean isExpROM() {
        return !SystemProperties.get("persist.sys.oppo.region", "CN").equalsIgnoreCase("CN");
    }

    public void setInitialDisplaySize(Display display, int width, int height, int density) {
        if (this.mContext != null && display.getDisplayId() == 0) {
            int shortSize;
            int longSize;
            this.mDisplay = display;
            Resources res = this.mContext.getResources();
            if (width > height) {
                shortSize = height;
                longSize = width;
                this.mLandscapeRotation = 0;
                this.mSeascapeRotation = 2;
                if (res.getBoolean(17956918)) {
                    this.mPortraitRotation = 1;
                    this.mUpsideDownRotation = 3;
                } else {
                    this.mPortraitRotation = 3;
                    this.mUpsideDownRotation = 1;
                }
            } else {
                shortSize = width;
                longSize = height;
                this.mPortraitRotation = 0;
                this.mUpsideDownRotation = 2;
                if (res.getBoolean(17956918)) {
                    this.mLandscapeRotation = 3;
                    this.mSeascapeRotation = 1;
                } else {
                    this.mLandscapeRotation = 1;
                    this.mSeascapeRotation = 3;
                }
            }
            this.mNaturalHeight = longSize;
            this.mNaturalWidth = shortSize;
            int shortSizeDp = (shortSize * 160) / density;
            int longSizeDp = (longSize * 160) / density;
            boolean z = width != height && shortSizeDp < 600;
            this.mNavigationBarCanMove = z;
            this.mHasNavigationBar = res.getBoolean(17956968);
            String navBarOverride = SystemProperties.get("persist.sys.oppo.hasNav");
            if (LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(navBarOverride)) {
                this.mHasNavigationBar = false;
            } else if ("0".equals(navBarOverride)) {
                this.mHasNavigationBar = true;
            }
            if (this.mHasNavigationBar) {
                SystemProperties.set("qemu.hw.mainkeys", "0");
            } else {
                SystemProperties.set("qemu.hw.mainkeys", LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON);
            }
            if ("portrait".equals(SystemProperties.get("persist.demo.hdmirotation"))) {
                this.mDemoHdmiRotation = this.mPortraitRotation;
            } else {
                this.mDemoHdmiRotation = this.mLandscapeRotation;
            }
            this.mDemoHdmiRotationLock = SystemProperties.getBoolean("persist.demo.hdmirotationlock", false);
            if ("portrait".equals(SystemProperties.get("persist.demo.remoterotation"))) {
                this.mDemoRotation = this.mPortraitRotation;
            } else {
                this.mDemoRotation = this.mLandscapeRotation;
            }
            this.mDemoRotationLock = SystemProperties.getBoolean("persist.demo.rotationlock", false);
            z = (longSizeDp < 960 || shortSizeDp < 720 || !res.getBoolean(17956995)) ? false : !"true".equals(SystemProperties.get("config.override_forced_orient"));
            this.mForceDefaultOrientation = z;
        }
    }

    private boolean canHideNavigationBar() {
        return this.mHasNavigationBar;
    }

    public boolean isDefaultOrientationForced() {
        return this.mForceDefaultOrientation;
    }

    public void setDisplayOverscan(Display display, int left, int top, int right, int bottom) {
        if (display.getDisplayId() == 0) {
            this.mOverscanLeft = left;
            this.mOverscanTop = top;
            this.mOverscanRight = right;
            this.mOverscanBottom = bottom;
        }
    }

    public void updateSettings() {
        int i = 2;
        ContentResolver resolver = this.mContext.getContentResolver();
        boolean updateRotation = false;
        synchronized (this.mLock) {
            int userRotationMode;
            this.mEndcallBehavior = System.getIntForUser(resolver, "end_button_behavior", 2, -2);
            this.mIncallPowerBehavior = Secure.getIntForUser(resolver, "incall_power_button_behavior", 1, -2);
            boolean wakeGestureEnabledSetting = Secure.getIntForUser(resolver, "wake_gesture_enabled", 0, -2) != 0;
            if (this.mWakeGestureEnabledSetting != wakeGestureEnabledSetting) {
                this.mWakeGestureEnabledSetting = wakeGestureEnabledSetting;
                updateWakeGestureListenerLp();
            }
            int userRotation = System.getIntForUser(resolver, "user_rotation", 0, -2);
            if (this.mUserRotation != userRotation) {
                this.mUserRotation = userRotation;
                updateRotation = true;
            }
            if (System.getIntForUser(resolver, "accelerometer_rotation", 0, -2) != 0) {
                userRotationMode = 0;
            } else {
                userRotationMode = 1;
            }
            if (this.mUserRotationMode != userRotationMode) {
                this.mUserRotationMode = userRotationMode;
                updateRotation = true;
                updateOrientationListenerLp();
            }
            if (this.mSystemReady) {
                int pointerLocation = System.getIntForUser(resolver, "pointer_location", 0, -2);
                if (this.mPointerLocationMode != pointerLocation) {
                    this.mPointerLocationMode = pointerLocation;
                    Handler handler = this.mHandler;
                    if (pointerLocation != 0) {
                        i = 1;
                    }
                    handler.sendEmptyMessage(i);
                }
            }
            this.mLockScreenTimeout = System.getIntForUser(resolver, "screen_off_timeout", 0, -2);
            String imId = Secure.getStringForUser(resolver, "default_input_method", -2);
            boolean hasSoftInput = imId != null && imId.length() > 0;
            if (this.mHasSoftInput != hasSoftInput) {
                this.mHasSoftInput = hasSoftInput;
                updateRotation = true;
            }
            if (this.mImmersiveModeConfirmation != null) {
                this.mImmersiveModeConfirmation.loadSetting(this.mCurrentUserId);
            }
            this.mNavigationBarEnableStatus = Secure.getInt(this.mContext.getContentResolver(), HIDE_NAVIGATIONBAR_ENABLE, 0);
        }
        synchronized (this.mWindowManagerFuncs.getWindowManagerLock()) {
            PolicyControl.reloadFromSetting(this.mContext);
        }
        if (updateRotation) {
            updateRotation(true);
        }
    }

    private void updateWakeGestureListenerLp() {
        if (shouldEnableWakeGestureLp()) {
            this.mWakeGestureListener.requestWakeUpTrigger();
        } else {
            this.mWakeGestureListener.cancelWakeUpTrigger();
        }
    }

    private boolean shouldEnableWakeGestureLp() {
        if (!this.mWakeGestureEnabledSetting || this.mAwake) {
            return false;
        }
        if ((this.mLidControlsSleep && this.mLidState == 0) || !this.mWakeGestureListener.isSupported() || this.mIsIpoShutDown) {
            return false;
        }
        return true;
    }

    private void enablePointerLocation() {
        if (this.mPointerLocationView == null) {
            this.mPointerLocationView = new PointerLocationView(this.mContext);
            this.mPointerLocationView.setPrintCoords(false);
            LayoutParams lp = new LayoutParams(-1, -1);
            lp.type = 2015;
            lp.flags = 1304;
            if (ActivityManager.isHighEndGfx()) {
                lp.flags |= 16777216;
                lp.privateFlags |= 2;
            }
            lp.format = -3;
            lp.setTitle("PointerLocation");
            WindowManager wm = (WindowManager) this.mContext.getSystemService("window");
            lp.inputFeatures |= 2;
            wm.addView(this.mPointerLocationView, lp);
            this.mWindowManagerFuncs.registerPointerEventListener(this.mPointerLocationView);
        }
    }

    private void disablePointerLocation() {
        if (this.mPointerLocationView != null) {
            this.mWindowManagerFuncs.unregisterPointerEventListener(this.mPointerLocationView);
            ((WindowManager) this.mContext.getSystemService("window")).removeView(this.mPointerLocationView);
            this.mPointerLocationView = null;
        }
    }

    private int readRotation(int resID) {
        try {
            switch (this.mContext.getResources().getInteger(resID)) {
                case 0:
                    return 0;
                case 90:
                    return 1;
                case OppoProcessManager.MSG_UPLOAD /*180*/:
                    return 2;
                case 270:
                    return 3;
            }
        } catch (NotFoundException e) {
        }
        return -1;
    }

    public int checkAddPermission(LayoutParams attrs, int[] outAppOp) {
        int type = attrs.type;
        outAppOp[0] = -1;
        if ((type < 1 || type > 99) && ((type < 1000 || type > 1999) && (type < 2000 || type > 2999))) {
            return -10;
        }
        if (type < 2000 || type > 2999) {
            return 0;
        }
        String permission = null;
        switch (type) {
            case 2002:
            case 2003:
            case 2006:
            case 2007:
            case 2010:
                permission = "android.permission.SYSTEM_ALERT_WINDOW";
                outAppOp[0] = 24;
                break;
            case 2005:
                outAppOp[0] = 45;
                break;
            case 2011:
            case 2013:
            case 2023:
            case 2030:
            case 2031:
            case 2032:
            case 2035:
            case 2037:
                break;
            default:
                permission = "android.permission.INTERNAL_SYSTEM_WINDOW";
                break;
        }
        if (permission != null) {
            if ("android.permission.SYSTEM_ALERT_WINDOW".equals(permission)) {
                int callingUid = Binder.getCallingUid();
                if (callingUid == 1000) {
                    return 0;
                }
                switch (this.mAppOpsManager.checkOpNoThrow(outAppOp[0], callingUid, attrs.packageName)) {
                    case 0:
                    case 1:
                        return 0;
                    case 2:
                        try {
                            return this.mContext.getPackageManager().getApplicationInfo(attrs.packageName, UserHandle.getUserId(callingUid)).targetSdkVersion < 23 ? 0 : -8;
                        } catch (NameNotFoundException e) {
                        }
                    default:
                        return this.mContext.checkCallingPermission(permission) != 0 ? -8 : 0;
                }
            } else if (this.mContext.checkCallingOrSelfPermission(permission) != 0) {
                return -8;
            }
        }
        return 0;
    }

    public boolean checkShowToOwnerOnly(LayoutParams attrs) {
        boolean z = true;
        switch (attrs.type) {
            case 3:
            case 2000:
            case 2001:
            case 2002:
            case 2007:
            case 2008:
            case 2009:
            case 2014:
            case 2017:
            case 2018:
            case 2019:
            case 2020:
            case 2021:
            case 2022:
            case 2024:
            case 2026:
            case 2027:
            case 2029:
            case 2030:
            case 2034:
                break;
            default:
                if ((attrs.privateFlags & 16) == 0) {
                    return true;
                }
                break;
        }
        if (this.mContext.checkCallingOrSelfPermission("android.permission.INTERNAL_SYSTEM_WINDOW") == 0) {
            z = false;
        }
        return z;
    }

    public void adjustWindowParamsLw(LayoutParams attrs) {
        switch (attrs.type) {
            case 2005:
                if (attrs.hideTimeoutMilliseconds < 0 || attrs.hideTimeoutMilliseconds > 3500) {
                    attrs.hideTimeoutMilliseconds = 3500;
                }
                attrs.windowAnimations = 16973828;
                break;
            case 2006:
            case 2015:
                attrs.flags |= 24;
                attrs.flags &= -262145;
                break;
            case 2036:
                attrs.flags |= 8;
                break;
        }
        if (attrs.type != 2004) {
            attrs.privateFlags &= -1025;
        }
        if (ActivityManager.isHighEndGfx()) {
            if ((attrs.flags & Integer.MIN_VALUE) != 0) {
                attrs.subtreeSystemUiVisibility |= 512;
            }
            boolean forceWindowDrawsStatusBarBackground = (attrs.privateFlags & DumpState.DUMP_INTENT_FILTER_VERIFIERS) != 0;
            if ((attrs.flags & Integer.MIN_VALUE) != 0 || (forceWindowDrawsStatusBarBackground && attrs.height == -1 && attrs.width == -1)) {
                attrs.subtreeSystemUiVisibility |= 1024;
            }
        }
    }

    void readLidState() {
        this.mLidState = this.mWindowManagerFuncs.getLidState();
    }

    private void readCameraLensCoverState() {
        this.mCameraLensCoverState = this.mWindowManagerFuncs.getCameraLensCoverState();
    }

    private boolean isHidden(int accessibilityMode) {
        boolean z = true;
        switch (accessibilityMode) {
            case 1:
                if (this.mLidState != 0) {
                    z = false;
                }
                return z;
            case 2:
                if (this.mLidState != 1) {
                    z = false;
                }
                return z;
            default:
                return false;
        }
    }

    public void adjustConfigurationLw(Configuration config, int keyboardPresence, int navigationPresence) {
        boolean z = false;
        if ((keyboardPresence & 1) != 0) {
            z = true;
        }
        this.mHaveBuiltInKeyboard = z;
        readConfigurationDependentBehaviors();
        readLidState();
        if (config.keyboard == 1 || (keyboardPresence == 1 && isHidden(this.mLidKeyboardAccessibility))) {
            config.hardKeyboardHidden = 2;
            if (!this.mHasSoftInput) {
                config.keyboardHidden = 2;
            }
        }
        if (config.navigation == 1 || (navigationPresence == 1 && isHidden(this.mLidNavigationAccessibility))) {
            config.navigationHidden = 2;
        }
    }

    public void onConfigurationChanged() {
        Resources res = this.mContext.getResources();
        this.mStatusBarHeight = res.getDimensionPixelSize(17104921);
        int[] iArr = this.mNavigationBarHeightForRotationDefault;
        int i = this.mPortraitRotation;
        int dimensionPixelSize = res.getDimensionPixelSize(17104922);
        this.mNavigationBarHeightForRotationDefault[this.mUpsideDownRotation] = dimensionPixelSize;
        iArr[i] = dimensionPixelSize;
        iArr = this.mNavigationBarHeightForRotationDefault;
        i = this.mLandscapeRotation;
        dimensionPixelSize = res.getDimensionPixelSize(17104923);
        this.mNavigationBarHeightForRotationDefault[this.mSeascapeRotation] = dimensionPixelSize;
        iArr[i] = dimensionPixelSize;
        iArr = this.mNavigationBarWidthForRotationDefault;
        i = this.mPortraitRotation;
        dimensionPixelSize = res.getDimensionPixelSize(17104924);
        this.mNavigationBarWidthForRotationDefault[this.mSeascapeRotation] = dimensionPixelSize;
        this.mNavigationBarWidthForRotationDefault[this.mLandscapeRotation] = dimensionPixelSize;
        this.mNavigationBarWidthForRotationDefault[this.mUpsideDownRotation] = dimensionPixelSize;
        iArr[i] = dimensionPixelSize;
    }

    public int windowTypeToLayerLw(int type) {
        if (type >= 1 && type <= 99) {
            return 2;
        }
        switch (type) {
            case 2000:
                return 16;
            case 2001:
            case 2033:
                return 4;
            case 2002:
                return 3;
            case 2003:
                return 11;
            case 2005:
                return 8;
            case 2006:
                return 20;
            case 2007:
                return 9;
            case 2008:
                return 7;
            case 2009:
                return 18;
            case 2010:
                return 24;
            case 2011:
                return 12;
            case 2012:
                return 13;
            case 2013:
                return 2;
            case 2014:
                return 17;
            case 2015:
                return 29;
            case 2016:
                return 27;
            case 2017:
                return 15;
            case 2018:
                return 31;
            case 2019:
                return 21;
            case 2020:
                return 19;
            case 2021:
                return 30;
            case 2022:
                return 6;
            case 2023:
                return 10;
            case 2024:
                return 22;
            case 2026:
                return 26;
            case 2027:
                return 25;
            case 2029:
                return 14;
            case 2030:
                return 2;
            case 2031:
                return 5;
            case 2032:
                return 28;
            case 2034:
                return 2;
            case 2035:
                return 2;
            case 2036:
                return 23;
            case 2037:
                return 32;
            case 2301:
                return 2;
            case 2302:
                return 2;
            default:
                Log.e(TAG, "Unknown window type: " + type);
                return 2;
        }
    }

    public int subWindowTypeToLayerLw(int type) {
        switch (type) {
            case 1000:
            case 1003:
                return 1;
            case 1001:
                return -2;
            case 1002:
                return 2;
            case 1004:
                return -1;
            case 1005:
                return 3;
            default:
                Log.e(TAG, "Unknown sub-window type: " + type);
                return 0;
        }
    }

    public int getMaxWallpaperLayer() {
        return windowTypeToLayerLw(2000);
    }

    private int getNavigationBarWidth(int rotation, int uiMode) {
        return this.mNavigationBarWidthForRotationDefault[rotation];
    }

    public int getNonDecorDisplayWidth(int fullWidth, int fullHeight, int rotation, int uiMode) {
        if (this.mHasNavigationBar && this.mNavigationBarCanMove && fullWidth > fullHeight) {
            return fullWidth - getNavigationBarWidth(rotation, uiMode);
        }
        return fullWidth;
    }

    private int getNavigationBarHeight(int rotation, int uiMode) {
        return this.mNavigationBarHeightForRotationDefault[rotation];
    }

    public int getNonDecorDisplayHeight(int fullWidth, int fullHeight, int rotation, int uiMode) {
        if (!this.mHasNavigationBar || (this.mNavigationBarCanMove && fullWidth >= fullHeight)) {
            return fullHeight;
        }
        return fullHeight - getNavigationBarHeight(rotation, uiMode);
    }

    public int getConfigDisplayWidth(int fullWidth, int fullHeight, int rotation, int uiMode) {
        return getNonDecorDisplayWidth(fullWidth, fullHeight, rotation, uiMode);
    }

    public int getConfigDisplayHeight(int fullWidth, int fullHeight, int rotation, int uiMode) {
        return getNonDecorDisplayHeight(fullWidth, fullHeight, rotation, uiMode) - this.mStatusBarHeight;
    }

    public boolean isForceHiding(WindowState win) {
        return false;
    }

    public boolean isForceHiding(LayoutParams attrs) {
        if ((attrs.privateFlags & 1024) == 0) {
            return (isKeyguardHostWindow(attrs) && isKeyguardShowingAndNotOccluded()) || attrs.type == 2029;
        } else {
            return true;
        }
    }

    public boolean isKeyguardHostWindow(LayoutParams attrs) {
        return attrs.type == 2000;
    }

    public boolean canBeForceHidden(WindowState win, LayoutParams attrs) {
        boolean z = false;
        switch (attrs.type) {
            case 2000:
            case 2013:
            case 2019:
            case 2023:
            case 2029:
                return false;
            default:
                if (windowTypeToLayerLw(win.getBaseType()) < windowTypeToLayerLw(2000)) {
                    z = true;
                }
                return z;
        }
    }

    public WindowState getWinShowWhenLockedLw() {
        return this.mWinShowWhenLocked;
    }

    /* JADX WARNING: Missing block: B:72:0x0233, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public View addStartingWindow(IBinder appToken, String packageName, int theme, CompatibilityInfo compatInfo, CharSequence nonLocalizedLabel, int labelRes, int icon, int logo, int windowFlags, Configuration overrideConfig) {
        if (packageName == null) {
            return null;
        }
        WindowManager wm = null;
        View view = null;
        try {
            View view2;
            Context context = this.mContext;
            if (DEBUG_STARTING_WINDOW) {
                Slog.d(TAG, "addStartingWindow " + packageName + ": nonLocalizedLabel=" + nonLocalizedLabel + " theme=" + Integer.toHexString(theme));
            }
            if (!(theme == context.getThemeResId() && labelRes == 0)) {
                try {
                    context = context.createPackageContext(packageName, 0);
                    context.setTheme(theme);
                } catch (NameNotFoundException e) {
                }
            }
            if (overrideConfig != null) {
                if (overrideConfig != Configuration.EMPTY) {
                    if (DEBUG_STARTING_WINDOW) {
                        Slog.d(TAG, "addStartingWindow: creating context based on overrideConfig" + overrideConfig + " for starting window");
                    }
                    Context overrideContext = context.createConfigurationContext(overrideConfig);
                    overrideContext.setTheme(theme);
                    int resId = overrideContext.obtainStyledAttributes(R.styleable.Window).getResourceId(1, 0);
                    if (!(resId == 0 || overrideContext.getDrawable(resId) == null)) {
                        if (DEBUG_STARTING_WINDOW) {
                            Slog.d(TAG, "addStartingWindow: apply overrideConfig" + overrideConfig + " to starting window resId=" + resId);
                        }
                        context = overrideContext;
                    }
                }
            }
            PhoneWindow win = new PhoneWindow(context);
            win.setIsStartingWindow(true);
            CharSequence label = context.getResources().getText(labelRes, null);
            if (label != null) {
                win.setTitle(label, true);
            } else {
                win.setTitle(nonLocalizedLabel, false);
            }
            win.setType(3);
            synchronized (this.mWindowManagerFuncs.getWindowManagerLock()) {
                if (this.mKeyguardHidden) {
                    windowFlags |= DumpState.DUMP_FROZEN;
                }
            }
            win.setFlags(((windowFlags | 16) | 8) | DumpState.DUMP_INTENT_FILTER_VERIFIERS, ((windowFlags | 16) | 8) | DumpState.DUMP_INTENT_FILTER_VERIFIERS);
            win.setStatusBarColor(0);
            win.setDefaultIcon(icon);
            win.setDefaultLogo(logo);
            win.setLayout(-1, -1);
            LayoutParams params = win.getAttributes();
            params.token = appToken;
            params.packageName = packageName;
            params.windowAnimations = win.getWindowStyle().getResourceId(8, 0);
            params.privateFlags |= 1;
            params.privateFlags |= 16;
            if (!compatInfo.supportsScreen()) {
                params.privateFlags |= 128;
            }
            params.setTitle("Starting " + packageName);
            wm = (WindowManager) context.getSystemService("window");
            view = win.getDecorView();
            if (DEBUG_STARTING_WINDOW) {
                Slog.d(TAG, "Adding starting window for " + packageName + " / " + appToken + ": " + (view.getParent() != null ? view : null));
            }
            wm.addView(view, params);
            if (this.mAppLaunchTimeEnabled) {
                WindowManagerGlobal.getInstance().doTraversal(view, true);
            }
            if (view.getParent() != null) {
                view2 = view;
            } else {
                view2 = null;
            }
            if (view != null && view.getParent() == null) {
                Log.w(TAG, "view not successfully added to wm, removing view");
                wm.removeViewImmediate(view);
            }
            return view2;
        } catch (BadTokenException e2) {
            Log.w(TAG, appToken + " already running, starting window not displayed. " + e2.getMessage());
            if (view != null && view.getParent() == null) {
                Log.w(TAG, "view not successfully added to wm, removing view");
                wm.removeViewImmediate(view);
            }
        } catch (RuntimeException e3) {
            Log.w(TAG, appToken + " failed creating starting window", e3);
            if (view != null && view.getParent() == null) {
                Log.w(TAG, "view not successfully added to wm, removing view");
                wm.removeViewImmediate(view);
            }
        } catch (Throwable th) {
            if (view != null && view.getParent() == null) {
                Log.w(TAG, "view not successfully added to wm, removing view");
                wm.removeViewImmediate(view);
            }
        }
    }

    public void removeStartingWindow(IBinder appToken, View window) {
        if (DEBUG_STARTING_WINDOW) {
            Slog.v(TAG, "Removing starting window for " + appToken + ": " + window + " Callers=" + Debug.getCallers(4));
        }
        if (window != null) {
            ((WindowManager) this.mContext.getSystemService("window")).removeView(window);
        }
    }

    public int prepareAddWindowLw(WindowState win, LayoutParams attrs) {
        switch (attrs.type) {
            case 2000:
                this.mContext.enforceCallingOrSelfPermission("android.permission.STATUS_BAR_SERVICE", "PhoneWindowManager");
                if (this.mStatusBar == null || !this.mStatusBar.isAlive()) {
                    this.mStatusBar = win;
                    this.mStatusBarController.setWindow(win);
                    break;
                }
                return -7;
            case 2014:
            case 2017:
            case 2024:
            case 2033:
                this.mContext.enforceCallingOrSelfPermission("android.permission.STATUS_BAR_SERVICE", "PhoneWindowManager");
                break;
            case 2019:
                this.mContext.enforceCallingOrSelfPermission("android.permission.STATUS_BAR_SERVICE", "PhoneWindowManager");
                if (this.mNavigationBar == null || !this.mNavigationBar.isAlive()) {
                    this.mNavigationBar = win;
                    this.mNavigationBarController.setWindow(win);
                    if (DEBUG_LAYOUT) {
                        Slog.i(TAG, "NAVIGATION BAR: " + this.mNavigationBar);
                        break;
                    }
                }
                return -7;
                break;
            case 2029:
                if (this.mKeyguardScrim == null) {
                    this.mKeyguardScrim = win;
                    break;
                }
                return -7;
        }
        String pkgName = win.getOwningPackage();
        if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
            Slog.d(TAG, "prepareAddWindowLw pkgName:" + pkgName + " mTopFullscreenOpaqueWindowNavBarColor:" + Integer.toHexString(this.mTopFullscreenOpaqueWindowNavBarColor) + " mLastNavigationBarColor:" + Integer.toHexString(this.mLastNavigationBarColor));
        }
        if (!win.toString().contains("Starting ")) {
            boolean isAppInAutomaticAdapationList = false;
            try {
                isAppInAutomaticAdapationList = this.mWindowManager.isActivityNeedPalette(pkgName, IElsaManager.EMPTY_PACKAGE);
            } catch (RemoteException e) {
            }
            if (isAppInAutomaticAdapationList) {
                win.updateWindowNavigationBarColor(this.mTopFullscreenOpaqueWindowNavBarColor);
            }
        }
        return 0;
    }

    public void removeWindowLw(WindowState win) {
        if (this.mStatusBar == win) {
            this.mStatusBar = null;
            this.mStatusBarController.setWindow(null);
        } else if (this.mKeyguardScrim == win) {
            Log.v(TAG, "Removing keyguard scrim");
            this.mKeyguardScrim = null;
        }
        if (this.mNavigationBar == win) {
            this.mNavigationBar = null;
            this.mNavigationBarController.setWindow(null);
        }
    }

    public int selectAnimationLw(WindowState win, int transit) {
        if (win == this.mStatusBar) {
            boolean isKeyguard = this.mKeyguardDelegate == null ? false : this.mKeyguardDelegate.isShowing();
            if (transit == 2 || transit == 4) {
                return isKeyguard ? -1 : 17432619;
            } else if (transit == 1 || transit == 3) {
                return isKeyguard ? -1 : 17432618;
            }
        } else if (win == this.mNavigationBar) {
            if (win.getAttrs().windowAnimations != 0) {
                return 0;
            }
            if (this.mNavigationBarPosition == 0) {
                if (transit == 2 || transit == 4) {
                    if (isKeyguardShowingAndNotOccluded()) {
                        return 17432613;
                    }
                    return 17432612;
                } else if (transit == 1 || transit == 3) {
                    return 17432611;
                }
            } else if (this.mNavigationBarPosition == 1) {
                if (transit == 2 || transit == 4) {
                    return 17432617;
                }
                if (transit == 1 || transit == 3) {
                    return 17432616;
                }
            } else if (this.mNavigationBarPosition == 2) {
                if (transit == 2 || transit == 4) {
                    return 17432615;
                }
                if (transit == 1 || transit == 3) {
                    return 17432614;
                }
            }
        } else if (win.getAttrs().type == 2034) {
            return selectDockedDividerAnimationLw(win, transit);
        }
        if (transit == 5) {
            if (win.hasAppShownWindows()) {
                return 17432593;
            }
        } else if (win.getAttrs().type == 2023 && this.mDreamingLockscreen && transit == 1) {
            return -1;
        }
        return 0;
    }

    private int selectDockedDividerAnimationLw(WindowState win, int transit) {
        boolean landscape;
        int insets = this.mWindowManagerFuncs.getDockedDividerInsetsLw();
        Rect frame = win.getFrameLw();
        boolean behindNavBar = this.mNavigationBar != null ? ((this.mNavigationBarPosition != 0 || frame.top + insets < this.mNavigationBar.getFrameLw().top) && (this.mNavigationBarPosition != 1 || frame.left + insets < this.mNavigationBar.getFrameLw().left)) ? this.mNavigationBarPosition == 2 ? frame.right - insets <= this.mNavigationBar.getFrameLw().right : false : true : false;
        if (frame.height() > frame.width()) {
            landscape = true;
        } else {
            landscape = false;
        }
        boolean offscreenLandscape = landscape ? frame.right - insets > 0 ? frame.left + insets >= win.getDisplayFrameLw().right : true : false;
        boolean offscreenPortrait = !landscape ? frame.top - insets > 0 ? frame.bottom + insets >= win.getDisplayFrameLw().bottom : true : false;
        boolean offscreen = !offscreenLandscape ? offscreenPortrait : true;
        if (behindNavBar || offscreen) {
            return 0;
        }
        if (transit == 1 || transit == 3) {
            return 17432576;
        }
        return transit == 2 ? 0 : 0;
    }

    public void selectRotationAnimationLw(int[] anim) {
        if (this.mTopFullscreenOpaqueWindowState != null) {
            int animationHint = this.mTopFullscreenOpaqueWindowState.getRotationAnimationHint();
            if (animationHint < 0 && this.mTopIsFullscreen) {
                animationHint = this.mTopFullscreenOpaqueWindowState.getAttrs().rotationAnimation;
            }
            switch (animationHint) {
                case 1:
                case 3:
                    anim[0] = 17432686;
                    anim[1] = 17432684;
                    return;
                case 2:
                    anim[0] = 17432685;
                    anim[1] = 17432684;
                    return;
                default:
                    anim[1] = 0;
                    anim[0] = 0;
                    return;
            }
        }
        anim[1] = 0;
        anim[0] = 0;
    }

    public boolean validateRotationAnimationLw(int exitAnimId, int enterAnimId, boolean forceDefault) {
        boolean z = true;
        switch (exitAnimId) {
            case 17432685:
            case 17432686:
                if (forceDefault) {
                    return false;
                }
                int[] anim = new int[2];
                selectRotationAnimationLw(anim);
                if (!(exitAnimId == anim[0] && enterAnimId == anim[1])) {
                    z = false;
                }
                return z;
            default:
                return true;
        }
    }

    public Animation createForceHideEnterAnimation(boolean onWallpaper, boolean goingToNotificationShade) {
        if (goingToNotificationShade) {
            return AnimationUtils.loadAnimation(this.mContext, 17432661);
        }
        int i;
        Context context = this.mContext;
        if (onWallpaper) {
            i = 17432662;
        } else {
            i = 17432660;
        }
        AnimationSet set = (AnimationSet) AnimationUtils.loadAnimation(context, i);
        List<Animation> animations = set.getAnimations();
        for (int i2 = animations.size() - 1; i2 >= 0; i2--) {
            ((Animation) animations.get(i2)).setInterpolator(this.mLogDecelerateInterpolator);
        }
        return set;
    }

    public Animation createForceHideWallpaperExitAnimation(boolean goingToNotificationShade) {
        if (goingToNotificationShade) {
            return null;
        }
        return AnimationUtils.loadAnimation(this.mContext, 17432665);
    }

    private static void awakenDreams() {
        IDreamManager dreamManager = getDreamManager();
        if (dreamManager != null) {
            try {
                dreamManager.awaken();
            } catch (RemoteException e) {
            }
        }
    }

    static IDreamManager getDreamManager() {
        return IDreamManager.Stub.asInterface(ServiceManager.checkService("dreams"));
    }

    TelecomManager getTelecommService() {
        return (TelecomManager) this.mContext.getSystemService("telecom");
    }

    static IAudioService getAudioService() {
        IAudioService audioService = IAudioService.Stub.asInterface(ServiceManager.checkService("audio"));
        if (audioService == null) {
            Log.w(TAG, "Unable to find IAudioService interface.");
        }
        return audioService;
    }

    boolean keyguardOn() {
        return !isKeyguardShowingAndNotOccluded() ? inKeyguardRestrictedKeyInputMode() : true;
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "JianHui.Yu@Plf.SDK, 2016-06-14 : Modify for Longshot", property = OppoRomType.ROM)
    public long interceptKeyBeforeDispatching(WindowState win, KeyEvent event, int policyFlags) {
        boolean keyguardOn = keyguardOn();
        int keyCode = event.getKeyCode();
        int repeatCount = event.getRepeatCount();
        int metaState = event.getMetaState();
        int flags = event.getFlags();
        boolean down = event.getAction() == 0;
        boolean canceled = event.isCanceled();
        if (!IS_USER_BUILD || DEBUG_INPUT || InputLog.DEBUG) {
            Log.d(TAG, "interceptKeyTi keyCode=" + keyCode + " down=" + down + " repeatCount=" + repeatCount + " keyguardOn=" + keyguardOn + " mHomePressed=" + this.mHomePressed + " canceled=" + canceled + " metaState:" + metaState);
        }
        boolean keyup = event.getAction() == 1;
        if (keyCode == 4 && keyup) {
            SystemProperties.set("debug.sys.oppo.keytime", Long.toString(System.currentTimeMillis()));
        } else if (keyCode == 3 && keyup) {
            SystemProperties.set("debug.sys.oppo.keytime", "0");
        }
        if (keyCode == 187 && !down && this.mHandler.hasCallbacks(this.mRecentsStartSplitSreen)) {
            this.mHandler.removeCallbacks(this.mRecentsStartSplitSreen);
        }
        if (this.mScreenshotChordEnabled && (flags & 1024) == 0) {
            long now;
            long timeoutTime;
            if (this.mScreenshotChordVolumeDownKeyTriggered && !this.mScreenshotChordPowerKeyTriggered) {
                now = SystemClock.uptimeMillis();
                timeoutTime = this.mScreenshotChordVolumeDownKeyTime + SCREENSHOT_CHORD_DEBOUNCE_DELAY_MILLIS;
                if (now < timeoutTime) {
                    return timeoutTime - now;
                }
            }
            if (keyCode == 25 && this.mScreenshotChordVolumeDownKeyConsumed) {
                if (!down) {
                    this.mScreenshotChordVolumeDownKeyConsumed = false;
                }
                return -1;
            }
            if (this.mScreenshotChordVolumeUpKeyTriggered && !this.mScreenshotChordPowerKeyTriggered) {
                now = SystemClock.uptimeMillis();
                timeoutTime = this.mScreenshotChordVolumeUpKeyTime + SCREENSHOT_CHORD_DEBOUNCE_DELAY_MILLIS;
                if (now < timeoutTime) {
                    return timeoutTime - now;
                }
            }
            if (keyCode == 24 && this.mScreenshotChordVolumeUpKeyConsumed) {
                if (!down) {
                    this.mScreenshotChordVolumeUpKeyConsumed = false;
                }
                return -1;
            }
        }
        if (!this.mHasNavigationBar && (flags & 1024) == 0 && keyCode == 4 && down && repeatCount == 1) {
            interceptDismissPinningChord();
        }
        if (this.mPendingMetaAction && !KeyEvent.isMetaKey(keyCode)) {
            this.mPendingMetaAction = false;
        }
        if (!(!this.mPendingCapsLockToggle || KeyEvent.isMetaKey(keyCode) || KeyEvent.isAltKey(keyCode))) {
            this.mPendingCapsLockToggle = false;
        }
        int type;
        if (keyCode != 3) {
            if (keyCode == 82) {
                if (down && repeatCount == 0 && this.mEnableShiftMenuBugReports && (metaState & 1) == 1) {
                    this.mContext.sendOrderedBroadcastAsUser(new Intent("android.intent.action.BUG_REPORT"), UserHandle.CURRENT, null, null, null, 0, null, null);
                    return -1;
                }
            } else if (keyCode == 84) {
                if (!down) {
                    this.mSearchKeyShortcutPending = false;
                    if (this.mConsumeSearchKeyUp) {
                        this.mConsumeSearchKeyUp = false;
                        return -1;
                    }
                } else if (repeatCount == 0) {
                    this.mSearchKeyShortcutPending = true;
                    this.mConsumeSearchKeyUp = false;
                }
                return 0;
            } else if (keyCode == 187) {
                if (canceled) {
                    Log.i(TAG, "Ignoring RecentApps key; event canceled.");
                    return -1;
                }
                if ((event.getFlags() & 128) != 0) {
                    this.mRecentsLongPressDetected = true;
                }
                if (!keyguardOn) {
                    if (down && repeatCount == 0) {
                        preloadRecentApps();
                        this.mRecentsLongPressDetected = false;
                        this.mHandler.postDelayed(this.mRecentsStartSplitSreen, 750);
                    } else if (!(down || this.mRecentsLongPressDetected)) {
                        toggleRecentApps();
                    }
                }
                return -1;
            } else if (keyCode == 42 && event.isMetaPressed()) {
                if (down) {
                    IStatusBarService service = getStatusBarService();
                    if (service != null) {
                        try {
                            service.expandNotificationsPanel();
                        } catch (RemoteException e) {
                        }
                    }
                }
            } else if (keyCode == 47 && event.isMetaPressed() && event.isCtrlPressed()) {
                if (down && repeatCount == 0) {
                    if (event.isShiftPressed()) {
                        type = 2;
                    } else {
                        type = 1;
                    }
                    this.mScreenshotRunnable.setScreenshotType(type);
                    this.mHandler.post(this.mScreenshotRunnable);
                    return -1;
                }
            } else if (keyCode == 76 && event.isMetaPressed()) {
                if (down && repeatCount == 0 && !isKeyguardLocked()) {
                    toggleKeyboardShortcutsMenu(event.getDeviceId());
                }
            } else if (keyCode == 219) {
                if (down) {
                    if (repeatCount == 0) {
                        this.mAssistKeyLongPressed = false;
                    } else if (repeatCount == 1) {
                        this.mAssistKeyLongPressed = true;
                        if (!keyguardOn) {
                            launchAssistLongPressAction();
                        }
                    }
                } else if (this.mAssistKeyLongPressed) {
                    this.mAssistKeyLongPressed = false;
                } else if (!keyguardOn) {
                    launchAssistAction(null, event.getDeviceId());
                }
                return -1;
            } else if (keyCode == 231) {
                if (!down) {
                    Intent intent;
                    if (keyguardOn) {
                        IDeviceIdleController dic = IDeviceIdleController.Stub.asInterface(ServiceManager.getService("deviceidle"));
                        if (dic != null) {
                            try {
                                dic.exitIdle("voice-search");
                            } catch (RemoteException e2) {
                            }
                        }
                        intent = new Intent("android.speech.action.VOICE_SEARCH_HANDS_FREE");
                        intent.putExtra("android.speech.extras.EXTRA_SECURE", true);
                    } else {
                        intent = new Intent("android.speech.action.WEB_SEARCH");
                    }
                    startActivityAsUser(voiceIntent, UserHandle.CURRENT_OR_SELF);
                }
            } else if (keyCode == 120) {
                if (down && repeatCount == 0) {
                    this.mScreenshotRunnable.setScreenshotType(1);
                    this.mHandler.post(this.mScreenshotRunnable);
                }
                return -1;
            } else if (keyCode == 221 || keyCode == 220) {
                if (down) {
                    int direction = keyCode == 221 ? 1 : -1;
                    if (System.getIntForUser(this.mContext.getContentResolver(), "screen_brightness_mode", 0, -3) != 0) {
                        System.putIntForUser(this.mContext.getContentResolver(), "screen_brightness_mode", 0, -3);
                    }
                    int min = this.mPowerManager.getMinimumScreenBrightnessSetting();
                    int max = this.mPowerManager.getMaximumScreenBrightnessSetting();
                    System.putIntForUser(this.mContext.getContentResolver(), "screen_brightness", Math.max(min, Math.min(max, System.getIntForUser(this.mContext.getContentResolver(), "screen_brightness", this.mPowerManager.getDefaultScreenBrightnessSetting(), -3) + (((((max - min) + 10) - 1) / 10) * direction))), -3);
                    startActivityAsUser(new Intent("android.intent.action.SHOW_BRIGHTNESS_DIALOG"), UserHandle.CURRENT_OR_SELF);
                }
                return -1;
            } else if ((keyCode == 24 || keyCode == 25 || keyCode == 164) && this.mUseTvRouting) {
                dispatchDirectAudioEvent(event);
                return -1;
            }
            boolean actionTriggered = false;
            if (KeyEvent.isModifierKey(keyCode)) {
                if (!this.mPendingCapsLockToggle) {
                    this.mInitialMetaState = this.mMetaState;
                    this.mPendingCapsLockToggle = true;
                } else if (event.getAction() == 1) {
                    int altOnMask = this.mMetaState & 50;
                    int metaOnMask = this.mMetaState & 458752;
                    if (!(metaOnMask == 0 || altOnMask == 0 || this.mInitialMetaState != (this.mMetaState ^ (altOnMask | metaOnMask)))) {
                        this.mInputManagerInternal.toggleCapsLock(event.getDeviceId());
                        actionTriggered = true;
                    }
                    this.mPendingCapsLockToggle = false;
                }
            }
            this.mMetaState = metaState;
            if (actionTriggered) {
                return -1;
            }
            if (KeyEvent.isMetaKey(keyCode)) {
                if (down) {
                    this.mPendingMetaAction = true;
                } else if (this.mPendingMetaAction) {
                    launchAssistAction("android.intent.extra.ASSIST_INPUT_HINT_KEYBOARD", event.getDeviceId());
                }
                return -1;
            }
            KeyCharacterMap kcm;
            Intent shortcutIntent;
            if (this.mSearchKeyShortcutPending) {
                kcm = event.getKeyCharacterMap();
                if (kcm.isPrintingKey(keyCode)) {
                    this.mConsumeSearchKeyUp = true;
                    this.mSearchKeyShortcutPending = false;
                    if (down && repeatCount == 0 && !keyguardOn) {
                        shortcutIntent = this.mShortcutManager.getIntent(kcm, keyCode, metaState);
                        if (shortcutIntent != null) {
                            shortcutIntent.addFlags(268435456);
                            try {
                                startActivityAsUser(shortcutIntent, UserHandle.CURRENT);
                                dismissKeyboardShortcutsMenu();
                            } catch (Throwable ex) {
                                Slog.w(TAG, "Dropping shortcut key combination because the activity to which it is registered was not found: SEARCH+" + KeyEvent.keyCodeToString(keyCode), ex);
                            }
                        } else {
                            Slog.i(TAG, "Dropping unregistered shortcut key combination: SEARCH+" + KeyEvent.keyCodeToString(keyCode));
                        }
                    }
                    return -1;
                }
            }
            if (down && repeatCount == 0 && !keyguardOn && (DumpState.DUMP_INSTALLS & metaState) != 0) {
                kcm = event.getKeyCharacterMap();
                if (kcm.isPrintingKey(keyCode)) {
                    shortcutIntent = this.mShortcutManager.getIntent(kcm, keyCode, -458753 & metaState);
                    if (shortcutIntent != null) {
                        shortcutIntent.addFlags(268435456);
                        try {
                            startActivityAsUser(shortcutIntent, UserHandle.CURRENT);
                            dismissKeyboardShortcutsMenu();
                        } catch (Throwable ex2) {
                            Slog.w(TAG, "Dropping shortcut key combination because the activity to which it is registered was not found: META+" + KeyEvent.keyCodeToString(keyCode), ex2);
                        }
                        return -1;
                    }
                }
            }
            if (down && repeatCount == 0 && !keyguardOn) {
                String category = (String) sApplicationLaunchKeyCategories.get(keyCode);
                if (category != null) {
                    Intent intent2 = Intent.makeMainSelectorActivity("android.intent.action.MAIN", category);
                    intent2.setFlags(268435456);
                    try {
                        startActivityAsUser(intent2, UserHandle.CURRENT);
                        dismissKeyboardShortcutsMenu();
                    } catch (Throwable ex22) {
                        Slog.w(TAG, "Dropping application launch key because the activity to which it is registered was not found: keyCode=" + keyCode + ", category=" + category, ex22);
                    }
                    return -1;
                }
            }
            if (down && repeatCount == 0 && keyCode == 61) {
                if (this.mRecentAppsHeldModifiers == 0 && !keyguardOn && isUserSetupComplete()) {
                    int shiftlessModifiers = event.getModifiers() & -194;
                    if (KeyEvent.metaStateHasModifiers(shiftlessModifiers, 2)) {
                        this.mRecentAppsHeldModifiers = shiftlessModifiers;
                        showRecentApps(true, false);
                        return -1;
                    }
                }
            } else if (!(down || this.mRecentAppsHeldModifiers == 0 || (this.mRecentAppsHeldModifiers & metaState) != 0)) {
                this.mRecentAppsHeldModifiers = 0;
                hideRecentApps(true, false);
            }
            if (down && repeatCount == 0 && (keyCode == 204 || (keyCode == 62 && (458752 & metaState) != 0))) {
                this.mWindowManagerFuncs.switchInputMethod((metaState & HdmiCecKeycode.UI_SOUND_PRESENTATION_TREBLE_STEP_PLUS) == 0);
                return -1;
            } else if (this.mLanguageSwitchKeyPressed && !down && (keyCode == 204 || keyCode == 62)) {
                this.mLanguageSwitchKeyPressed = false;
                return -1;
            } else if (isValidGlobalKey(keyCode) && this.mGlobalKeyManager.handleGlobalKey(this.mContext, keyCode, event)) {
                return -1;
            } else {
                if (down) {
                    long shortcutCode = (long) keyCode;
                    if (event.isCtrlPressed()) {
                        shortcutCode |= 17592186044416L;
                    }
                    if (event.isAltPressed()) {
                        shortcutCode |= 8589934592L;
                    }
                    if (event.isShiftPressed()) {
                        shortcutCode |= 4294967296L;
                    }
                    if (event.isMetaPressed()) {
                        shortcutCode |= 281474976710656L;
                    }
                    IShortcutService shortcutService = (IShortcutService) this.mShortcutKeyServices.get(shortcutCode);
                    if (shortcutService != null) {
                        try {
                            if (isUserSetupComplete()) {
                                shortcutService.notifyShortcutKeyPressed(shortcutCode);
                            }
                        } catch (RemoteException e3) {
                            this.mShortcutKeyServices.delete(shortcutCode);
                        }
                        return -1;
                    }
                }
                if ((DumpState.DUMP_INSTALLS & metaState) != 0) {
                    return -1;
                }
                return 0;
            }
        } else if (down) {
            LayoutParams attrs = win != null ? win.getAttrs() : null;
            if (attrs != null) {
                type = attrs.type;
                if (type == 2029 || type == 2009 || (attrs.privateFlags & 1024) != 0) {
                    return 0;
                }
                for (int i : WINDOW_TYPES_WHERE_HOME_DOESNT_WORK) {
                    if (type == i) {
                        return -1;
                    }
                }
            }
            if (repeatCount == 0) {
                this.mHomePressed = true;
                if (this.mHomeDoubleTapPending) {
                    this.mHomeDoubleTapPending = false;
                    this.mHandler.removeCallbacks(this.mHomeDoubleTapTimeoutRunnable);
                    handleDoubleTapOnHome();
                } else if (this.mLongPressOnHomeBehavior == 1 || this.mDoubleTapOnHomeBehavior == 1) {
                    preloadRecentApps();
                }
            } else if (!((event.getFlags() & 128) == 0 || keyguardOn)) {
                handleLongPressOnHome(event.getDeviceId());
            }
            return -1;
        } else {
            cancelPreloadRecentApps();
            this.mHomePressed = false;
            if (this.mHomeConsumed) {
                this.mHomeConsumed = false;
                return -1;
            } else if (canceled) {
                Log.i(TAG, "Ignoring HOME; event canceled.");
                return -1;
            } else if (this.mDoubleTapOnHomeBehavior != 0) {
                this.mHandler.removeCallbacks(this.mHomeDoubleTapTimeoutRunnable);
                this.mHomeDoubleTapPending = true;
                this.mHandler.postDelayed(this.mHomeDoubleTapTimeoutRunnable, (long) ViewConfiguration.getDoubleTapTimeout());
                return -1;
            } else {
                handleShortPressOnHome();
                return -1;
            }
        }
    }

    public KeyEvent dispatchUnhandledKey(WindowState win, KeyEvent event, int policyFlags) {
        if (DEBUG_INPUT) {
            Slog.d(TAG, "Unhandled key: win=" + win + ", action=" + event.getAction() + ", flags=" + event.getFlags() + ", keyCode=" + event.getKeyCode() + ", scanCode=" + event.getScanCode() + ", metaState=" + event.getMetaState() + ", repeatCount=" + event.getRepeatCount() + ", policyFlags=" + policyFlags);
        }
        KeyEvent fallbackEvent = null;
        if ((event.getFlags() & 1024) == 0) {
            FallbackAction fallbackAction;
            KeyCharacterMap kcm = event.getKeyCharacterMap();
            int keyCode = event.getKeyCode();
            int metaState = event.getMetaState();
            boolean initialDown = event.getAction() == 0 ? event.getRepeatCount() == 0 : false;
            if (initialDown) {
                fallbackAction = kcm.getFallbackAction(keyCode, metaState);
            } else {
                fallbackAction = (FallbackAction) this.mFallbackActions.get(keyCode);
            }
            if (fallbackAction != null) {
                if (DEBUG_INPUT) {
                    Slog.d(TAG, "Fallback: keyCode=" + fallbackAction.keyCode + " metaState=" + Integer.toHexString(fallbackAction.metaState));
                }
                fallbackEvent = KeyEvent.obtain(event.getDownTime(), event.getEventTime(), event.getAction(), fallbackAction.keyCode, event.getRepeatCount(), fallbackAction.metaState, event.getDeviceId(), event.getScanCode(), event.getFlags() | 1024, event.getSource(), null);
                if (!interceptFallback(win, fallbackEvent, policyFlags)) {
                    fallbackEvent.recycle();
                    fallbackEvent = null;
                }
                if (initialDown) {
                    this.mFallbackActions.put(keyCode, fallbackAction);
                } else if (event.getAction() == 1) {
                    this.mFallbackActions.remove(keyCode);
                    fallbackAction.recycle();
                }
            }
        }
        if (DEBUG_INPUT) {
            if (fallbackEvent == null) {
                Slog.d(TAG, "No fallback.");
            } else {
                Slog.d(TAG, "Performing fallback: " + fallbackEvent);
            }
        }
        return fallbackEvent;
    }

    private boolean interceptFallback(WindowState win, KeyEvent fallbackEvent, int policyFlags) {
        if ((interceptKeyBeforeQueueing(fallbackEvent, policyFlags) & 1) == 0 || interceptKeyBeforeDispatching(win, fallbackEvent, policyFlags) != 0) {
            return false;
        }
        return true;
    }

    public void registerShortcutKey(long shortcutCode, IShortcutService shortcutService) throws RemoteException {
        synchronized (this.mLock) {
            IShortcutService service = (IShortcutService) this.mShortcutKeyServices.get(shortcutCode);
            if (service == null || !service.asBinder().pingBinder()) {
                this.mShortcutKeyServices.put(shortcutCode, shortcutService);
            } else {
                throw new RemoteException("Key already exists.");
            }
        }
    }

    public boolean canShowDismissingWindowWhileLockedLw() {
        if (this.mKeyguardDelegate == null || !this.mKeyguardDelegate.isTrusted()) {
            return false;
        }
        return this.mCurrentlyDismissingKeyguard;
    }

    private void launchAssistLongPressAction() {
        performHapticFeedbackLw(null, 0, false);
        sendCloseSystemWindows(SYSTEM_DIALOG_REASON_ASSIST);
        Intent intent = new Intent("android.intent.action.SEARCH_LONG_PRESS");
        intent.setFlags(268435456);
        try {
            SearchManager searchManager = getSearchManager();
            if (searchManager != null) {
                searchManager.stopSearch();
            }
            startActivityAsUser(intent, UserHandle.CURRENT);
        } catch (ActivityNotFoundException e) {
            Slog.w(TAG, "No activity to handle assist long press action.", e);
        }
    }

    private void launchAssistAction(String hint, int deviceId) {
        sendCloseSystemWindows(SYSTEM_DIALOG_REASON_ASSIST);
        if (isUserSetupComplete()) {
            Bundle args = null;
            if (deviceId > Integer.MIN_VALUE) {
                args = new Bundle();
                args.putInt("android.intent.extra.ASSIST_INPUT_DEVICE_ID", deviceId);
            }
            if ((this.mContext.getResources().getConfiguration().uiMode & 15) == 4) {
                ((SearchManager) this.mContext.getSystemService("search")).launchLegacyAssist(hint, UserHandle.myUserId(), args);
            } else {
                if (hint != null) {
                    if (args == null) {
                        args = new Bundle();
                    }
                    args.putBoolean(hint, true);
                }
                StatusBarManagerInternal statusbar = getStatusBarManagerInternal();
                if (statusbar != null) {
                    statusbar.startAssist(args);
                }
            }
        }
    }

    private void startActivityAsUser(Intent intent, UserHandle handle) {
        if (isUserSetupComplete()) {
            this.mContext.startActivityAsUser(intent, handle);
        } else {
            Slog.i(TAG, "Not starting activity because user setup is in progress: " + intent);
        }
    }

    private SearchManager getSearchManager() {
        if (this.mSearchManager == null) {
            this.mSearchManager = (SearchManager) this.mContext.getSystemService("search");
        }
        return this.mSearchManager;
    }

    private void preloadRecentApps() {
        this.mPreloadedRecentApps = true;
        StatusBarManagerInternal statusbar = getStatusBarManagerInternal();
        if (statusbar != null) {
            statusbar.preloadRecentApps();
        }
    }

    protected void cancelPreloadRecentApps() {
        if (this.mPreloadedRecentApps) {
            this.mPreloadedRecentApps = false;
            StatusBarManagerInternal statusbar = getStatusBarManagerInternal();
            if (statusbar != null) {
                statusbar.cancelPreloadRecentApps();
            }
        }
    }

    private void toggleRecentApps() {
        this.mPreloadedRecentApps = false;
        StatusBarManagerInternal statusbar = getStatusBarManagerInternal();
        if (statusbar != null) {
            statusbar.toggleRecentApps();
        }
    }

    public void showRecentApps(boolean fromHome) {
        int i;
        this.mHandler.removeMessages(9);
        Handler handler = this.mHandler;
        if (fromHome) {
            i = 1;
        } else {
            i = 0;
        }
        handler.obtainMessage(9, i, 0).sendToTarget();
    }

    private void showRecentApps(boolean triggeredFromAltTab, boolean fromHome) {
        this.mPreloadedRecentApps = false;
        StatusBarManagerInternal statusbar = getStatusBarManagerInternal();
        if (statusbar != null) {
            statusbar.showRecentApps(triggeredFromAltTab, fromHome);
        }
    }

    private void toggleKeyboardShortcutsMenu(int deviceId) {
        StatusBarManagerInternal statusbar = getStatusBarManagerInternal();
        if (statusbar != null) {
            statusbar.toggleKeyboardShortcutsMenu(deviceId);
        }
    }

    private void dismissKeyboardShortcutsMenu() {
        StatusBarManagerInternal statusbar = getStatusBarManagerInternal();
        if (statusbar != null) {
            statusbar.dismissKeyboardShortcutsMenu();
        }
    }

    private void hideRecentApps(boolean triggeredFromAltTab, boolean triggeredFromHome) {
        this.mPreloadedRecentApps = false;
        StatusBarManagerInternal statusbar = getStatusBarManagerInternal();
        if (statusbar != null) {
            statusbar.hideRecentApps(triggeredFromAltTab, triggeredFromHome);
        }
    }

    void launchHomeFromHotKey() {
        launchHomeFromHotKey(true, true);
    }

    void launchHomeFromHotKey(final boolean awakenFromDreams, boolean respectKeyguard) {
        if (respectKeyguard) {
            if (!isKeyguardShowingAndNotOccluded()) {
                if (!this.mHideLockScreen && this.mKeyguardDelegate.isInputRestricted()) {
                    this.mKeyguardDelegate.verifyUnlock(new OnKeyguardExitResult() {
                        public void onKeyguardExitResult(boolean success) {
                            if (success) {
                                try {
                                    ActivityManagerNative.getDefault().stopAppSwitches();
                                } catch (RemoteException e) {
                                }
                                PhoneWindowManager.this.sendCloseSystemWindows(PhoneWindowManager.SYSTEM_DIALOG_REASON_HOME_KEY);
                                PhoneWindowManager.this.startDockOrHome(true, awakenFromDreams);
                            }
                        }
                    });
                    return;
                }
            }
            return;
        }
        try {
            ActivityManagerNative.getDefault().stopAppSwitches();
        } catch (RemoteException e) {
        }
        if (this.mRecentsVisible) {
            if (awakenFromDreams) {
                awakenDreams();
            }
            hideRecentApps(false, true);
        } else {
            sendCloseSystemWindows(SYSTEM_DIALOG_REASON_HOME_KEY);
            startDockOrHome(true, awakenFromDreams);
        }
    }

    public void setRecentsVisibilityLw(boolean visible) {
        this.mRecentsVisible = visible;
    }

    public void setTvPipVisibilityLw(boolean visible) {
        this.mTvPictureInPictureVisible = visible;
    }

    public int adjustSystemUiVisibilityLw(int visibility) {
        this.mStatusBarController.adjustSystemUiVisibilityLw(this.mLastSystemUiFlags, visibility);
        this.mNavigationBarController.adjustSystemUiVisibilityLw(this.mLastSystemUiFlags, visibility);
        this.mResettingSystemUiFlags &= visibility;
        return ((~this.mResettingSystemUiFlags) & visibility) & (~this.mForceClearedSystemUiFlags);
    }

    public boolean getInsetHintLw(LayoutParams attrs, Rect taskBounds, int displayRotation, int displayWidth, int displayHeight, Rect outContentInsets, Rect outStableInsets, Rect outOutsets) {
        int fl = PolicyControl.getWindowFlags(null, attrs);
        int systemUiVisibility = PolicyControl.getSystemUiVisibility(null, attrs) | attrs.subtreeSystemUiVisibility;
        if (outOutsets != null ? shouldUseOutsets(attrs, fl) : false) {
            int outset = ScreenShapeHelper.getWindowOutsetBottomPx(this.mContext.getResources());
            if (outset > 0) {
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
        }
        if ((65792 & fl) == 65792) {
            int availRight;
            int availBottom;
            if (!canHideNavigationBar() || (systemUiVisibility & 512) == 0) {
                availRight = this.mRestrictedScreenLeft + this.mRestrictedScreenWidth;
                availBottom = this.mRestrictedScreenTop + this.mRestrictedScreenHeight;
            } else {
                availRight = this.mUnrestrictedScreenLeft + this.mUnrestrictedScreenWidth;
                availBottom = this.mUnrestrictedScreenTop + this.mUnrestrictedScreenHeight;
            }
            if ((systemUiVisibility & 256) != 0) {
                if ((fl & 1024) != 0) {
                    outContentInsets.set(this.mStableFullscreenLeft, this.mStableFullscreenTop, availRight - this.mStableFullscreenRight, availBottom - this.mStableFullscreenBottom);
                } else {
                    outContentInsets.set(this.mStableLeft, this.mStableTop, availRight - this.mStableRight, availBottom - this.mStableBottom);
                }
            } else if ((fl & 1024) != 0 || (33554432 & fl) != 0) {
                outContentInsets.setEmpty();
            } else if ((systemUiVisibility & 1028) == 0) {
                outContentInsets.set(this.mCurLeft, this.mCurTop, availRight - this.mCurRight, availBottom - this.mCurBottom);
            } else {
                outContentInsets.set(this.mCurLeft, this.mCurTop, availRight - this.mCurRight, availBottom - this.mCurBottom);
            }
            outStableInsets.set(this.mStableLeft, this.mStableTop, availRight - this.mStableRight, availBottom - this.mStableBottom);
            if (taskBounds != null) {
                calculateRelevantTaskInsets(taskBounds, outContentInsets, displayWidth, displayHeight);
                calculateRelevantTaskInsets(taskBounds, outStableInsets, displayWidth, displayHeight);
            }
            return this.mForceShowSystemBars;
        }
        outContentInsets.setEmpty();
        outStableInsets.setEmpty();
        return this.mForceShowSystemBars;
    }

    private void calculateRelevantTaskInsets(Rect taskBounds, Rect inOutInsets, int displayWidth, int displayHeight) {
        mTmpRect.set(0, 0, displayWidth, displayHeight);
        mTmpRect.inset(inOutInsets);
        mTmpRect.intersect(taskBounds);
        inOutInsets.set(mTmpRect.left - taskBounds.left, mTmpRect.top - taskBounds.top, taskBounds.right - mTmpRect.right, taskBounds.bottom - mTmpRect.bottom);
    }

    private boolean shouldUseOutsets(LayoutParams attrs, int fl) {
        return attrs.type == 2013 || (33555456 & fl) != 0;
    }

    public void beginLayoutLw(boolean isDefaultDisplay, int displayWidth, int displayHeight, int displayRotation, int uiMode) {
        int navigationBarSize;
        int overscanLeft;
        int overscanTop;
        int overscanRight;
        int overscanBottom;
        int i;
        this.mDisplayRotation = displayRotation;
        int naturalHeight = this.mNaturalHeight;
        int compatHeight = (this.mNaturalWidth * 16) / 9;
        int navigationBarHeight = this.mNavigationBarHeightForRotationDefault[displayRotation];
        int navigatinoBarWidth = this.mNavigationBarWidthForRotationDefault[displayRotation];
        if (navigationBarHeight < navigatinoBarWidth) {
            navigationBarSize = navigationBarHeight;
        } else {
            navigationBarSize = navigatinoBarWidth;
        }
        int emptySize = (naturalHeight - compatHeight) - navigationBarSize;
        boolean isCompatNavBar = false;
        boolean isCompatWindow = false;
        boolean isIgnoreNavBar = false;
        if (displayRotation == 0 || displayRotation == 2) {
            if (displayHeight == -1) {
                isCompatNavBar = true;
                displayHeight = naturalHeight;
                this.mExpendBar = false;
            }
            if (displayHeight == -3) {
                isCompatWindow = true;
                displayHeight = compatHeight;
                this.mExpendBar = true;
            }
            if (displayHeight == -2) {
                isIgnoreNavBar = true;
                displayHeight = naturalHeight;
                this.mExpendBar = false;
            }
        } else if (displayRotation == 1 || displayRotation == 3) {
            if (displayWidth == -1) {
                isCompatNavBar = true;
                displayWidth = naturalHeight;
                this.mExpendBar = false;
            }
            if (displayWidth == -3) {
                isCompatWindow = true;
                displayWidth = compatHeight;
                this.mExpendBar = true;
            }
            if (displayWidth == -2) {
                isIgnoreNavBar = true;
                displayWidth = naturalHeight;
                this.mExpendBar = false;
            }
        }
        if (isDefaultDisplay) {
            switch (displayRotation) {
                case 1:
                    overscanLeft = this.mOverscanTop;
                    overscanTop = this.mOverscanRight;
                    overscanRight = this.mOverscanBottom;
                    overscanBottom = this.mOverscanLeft;
                    break;
                case 2:
                    overscanLeft = this.mOverscanRight;
                    overscanTop = this.mOverscanBottom;
                    overscanRight = this.mOverscanLeft;
                    overscanBottom = this.mOverscanTop;
                    break;
                case 3:
                    overscanLeft = this.mOverscanBottom;
                    overscanTop = this.mOverscanLeft;
                    overscanRight = this.mOverscanTop;
                    overscanBottom = this.mOverscanRight;
                    break;
                default:
                    overscanLeft = this.mOverscanLeft;
                    overscanTop = this.mOverscanTop;
                    overscanRight = this.mOverscanRight;
                    overscanBottom = this.mOverscanBottom;
                    break;
            }
        }
        overscanLeft = 0;
        overscanTop = 0;
        overscanRight = 0;
        overscanBottom = 0;
        if ((displayRotation == 0 || displayRotation == 2) && isCompatWindow) {
            this.mRestrictedOverscanScreenLeft = 0;
            this.mOverscanScreenLeft = 0;
            this.mRestrictedOverscanScreenTop = emptySize;
            this.mOverscanScreenTop = emptySize;
            this.mRestrictedOverscanScreenWidth = displayWidth;
            this.mOverscanScreenWidth = displayWidth;
            this.mRestrictedOverscanScreenHeight = displayHeight;
            this.mOverscanScreenHeight = displayHeight;
            this.mSystemLeft = 0;
            this.mSystemTop = emptySize;
            this.mSystemRight = displayWidth;
            this.mSystemBottom = displayHeight + emptySize;
            this.mUnrestrictedScreenLeft = overscanLeft;
            this.mUnrestrictedScreenTop = overscanTop + emptySize;
            this.mUnrestrictedScreenWidth = (displayWidth - overscanLeft) - overscanRight;
            this.mUnrestrictedScreenHeight = (displayHeight - overscanTop) - overscanBottom;
            this.mRestrictedScreenLeft = this.mUnrestrictedScreenLeft;
            this.mRestrictedScreenTop = this.mUnrestrictedScreenTop;
            i = this.mUnrestrictedScreenWidth;
            this.mSystemGestures.screenWidth = i;
            this.mRestrictedScreenWidth = i;
            i = this.mUnrestrictedScreenHeight;
            this.mSystemGestures.screenHeight = i;
            this.mRestrictedScreenHeight = i;
            i = this.mUnrestrictedScreenLeft;
            this.mCurLeft = i;
            this.mStableFullscreenLeft = i;
            this.mStableLeft = i;
            this.mVoiceContentLeft = i;
            this.mContentLeft = i;
            this.mDockLeft = i;
            i = this.mUnrestrictedScreenTop;
            this.mCurTop = i;
            this.mStableFullscreenTop = i;
            this.mStableTop = i;
            this.mVoiceContentTop = i;
            this.mContentTop = i;
            this.mDockTop = i;
            i = displayWidth - overscanRight;
            this.mCurRight = i;
            this.mStableFullscreenRight = i;
            this.mStableRight = i;
            this.mVoiceContentRight = i;
            this.mContentRight = i;
            this.mDockRight = i;
            i = (displayHeight + emptySize) - overscanBottom;
            this.mCurBottom = i;
            this.mStableFullscreenBottom = i;
            this.mStableBottom = i;
            this.mVoiceContentBottom = i;
            this.mContentBottom = i;
            this.mDockBottom = i;
        } else if (displayRotation == 1 && isCompatWindow) {
            this.mRestrictedOverscanScreenLeft = emptySize;
            this.mOverscanScreenLeft = emptySize;
            this.mRestrictedOverscanScreenTop = 0;
            this.mOverscanScreenTop = 0;
            this.mRestrictedOverscanScreenWidth = displayWidth;
            this.mOverscanScreenWidth = displayWidth;
            this.mRestrictedOverscanScreenHeight = displayHeight;
            this.mOverscanScreenHeight = displayHeight;
            this.mSystemLeft = emptySize;
            this.mSystemTop = 0;
            this.mSystemRight = displayWidth + emptySize;
            this.mSystemBottom = displayHeight;
            this.mUnrestrictedScreenLeft = overscanLeft + emptySize;
            this.mUnrestrictedScreenTop = overscanTop;
            this.mUnrestrictedScreenWidth = (displayWidth - overscanLeft) - overscanRight;
            this.mUnrestrictedScreenHeight = (displayHeight - overscanTop) - overscanBottom;
            this.mRestrictedScreenLeft = this.mUnrestrictedScreenLeft;
            this.mRestrictedScreenTop = this.mUnrestrictedScreenTop;
            i = this.mUnrestrictedScreenWidth;
            this.mSystemGestures.screenWidth = i;
            this.mRestrictedScreenWidth = i;
            i = this.mUnrestrictedScreenHeight;
            this.mSystemGestures.screenHeight = i;
            this.mRestrictedScreenHeight = i;
            i = this.mUnrestrictedScreenLeft;
            this.mCurLeft = i;
            this.mStableFullscreenLeft = i;
            this.mStableLeft = i;
            this.mVoiceContentLeft = i;
            this.mContentLeft = i;
            this.mDockLeft = i;
            i = this.mUnrestrictedScreenTop;
            this.mCurTop = i;
            this.mStableFullscreenTop = i;
            this.mStableTop = i;
            this.mVoiceContentTop = i;
            this.mContentTop = i;
            this.mDockTop = i;
            i = (displayWidth + emptySize) - overscanRight;
            this.mCurRight = i;
            this.mStableFullscreenRight = i;
            this.mStableRight = i;
            this.mVoiceContentRight = i;
            this.mContentRight = i;
            this.mDockRight = i;
            i = displayHeight - overscanBottom;
            this.mCurBottom = i;
            this.mStableFullscreenBottom = i;
            this.mStableBottom = i;
            this.mVoiceContentBottom = i;
            this.mContentBottom = i;
            this.mDockBottom = i;
        } else if (displayRotation == 3 && isCompatWindow) {
            this.mRestrictedOverscanScreenLeft = navigationBarSize;
            this.mOverscanScreenLeft = navigationBarSize;
            this.mRestrictedOverscanScreenTop = 0;
            this.mOverscanScreenTop = 0;
            this.mRestrictedOverscanScreenWidth = displayWidth;
            this.mOverscanScreenWidth = displayWidth;
            this.mRestrictedOverscanScreenHeight = displayHeight;
            this.mOverscanScreenHeight = displayHeight;
            this.mSystemLeft = navigationBarSize;
            this.mSystemTop = 0;
            this.mSystemRight = displayWidth + navigationBarSize;
            this.mSystemBottom = displayHeight;
            this.mUnrestrictedScreenLeft = overscanLeft + navigationBarSize;
            this.mUnrestrictedScreenTop = overscanTop;
            this.mUnrestrictedScreenWidth = (displayWidth - overscanLeft) - overscanRight;
            this.mUnrestrictedScreenHeight = (displayHeight - overscanTop) - overscanBottom;
            this.mRestrictedScreenLeft = this.mUnrestrictedScreenLeft;
            this.mRestrictedScreenTop = this.mUnrestrictedScreenTop;
            i = this.mUnrestrictedScreenWidth;
            this.mSystemGestures.screenWidth = i;
            this.mRestrictedScreenWidth = i;
            i = this.mUnrestrictedScreenHeight;
            this.mSystemGestures.screenHeight = i;
            this.mRestrictedScreenHeight = i;
            i = this.mUnrestrictedScreenLeft;
            this.mCurLeft = i;
            this.mStableFullscreenLeft = i;
            this.mStableLeft = i;
            this.mVoiceContentLeft = i;
            this.mContentLeft = i;
            this.mDockLeft = i;
            i = this.mUnrestrictedScreenTop;
            this.mCurTop = i;
            this.mStableFullscreenTop = i;
            this.mStableTop = i;
            this.mVoiceContentTop = i;
            this.mContentTop = i;
            this.mDockTop = i;
            i = (displayWidth + navigationBarSize) - overscanRight;
            this.mCurRight = i;
            this.mStableFullscreenRight = i;
            this.mStableRight = i;
            this.mVoiceContentRight = i;
            this.mContentRight = i;
            this.mDockRight = i;
            i = displayHeight - overscanBottom;
            this.mCurBottom = i;
            this.mStableFullscreenBottom = i;
            this.mStableBottom = i;
            this.mVoiceContentBottom = i;
            this.mContentBottom = i;
            this.mDockBottom = i;
        } else {
            this.mRestrictedOverscanScreenLeft = 0;
            this.mOverscanScreenLeft = 0;
            this.mRestrictedOverscanScreenTop = 0;
            this.mOverscanScreenTop = 0;
            this.mRestrictedOverscanScreenWidth = displayWidth;
            this.mOverscanScreenWidth = displayWidth;
            this.mRestrictedOverscanScreenHeight = displayHeight;
            this.mOverscanScreenHeight = displayHeight;
            this.mSystemLeft = 0;
            this.mSystemTop = 0;
            this.mSystemRight = displayWidth;
            this.mSystemBottom = displayHeight;
            this.mUnrestrictedScreenLeft = overscanLeft;
            this.mUnrestrictedScreenTop = overscanTop;
            this.mUnrestrictedScreenWidth = (displayWidth - overscanLeft) - overscanRight;
            this.mUnrestrictedScreenHeight = (displayHeight - overscanTop) - overscanBottom;
            this.mRestrictedScreenLeft = this.mUnrestrictedScreenLeft;
            this.mRestrictedScreenTop = this.mUnrestrictedScreenTop;
            i = this.mUnrestrictedScreenWidth;
            this.mSystemGestures.screenWidth = i;
            this.mRestrictedScreenWidth = i;
            i = this.mUnrestrictedScreenHeight;
            this.mSystemGestures.screenHeight = i;
            this.mRestrictedScreenHeight = i;
            i = this.mUnrestrictedScreenLeft;
            this.mCurLeft = i;
            this.mStableFullscreenLeft = i;
            this.mStableLeft = i;
            this.mVoiceContentLeft = i;
            this.mContentLeft = i;
            this.mDockLeft = i;
            i = this.mUnrestrictedScreenTop;
            this.mCurTop = i;
            this.mStableFullscreenTop = i;
            this.mStableTop = i;
            this.mVoiceContentTop = i;
            this.mContentTop = i;
            this.mDockTop = i;
            i = displayWidth - overscanRight;
            this.mCurRight = i;
            this.mStableFullscreenRight = i;
            this.mStableRight = i;
            this.mVoiceContentRight = i;
            this.mContentRight = i;
            this.mDockRight = i;
            i = displayHeight - overscanBottom;
            this.mCurBottom = i;
            this.mStableFullscreenBottom = i;
            this.mStableBottom = i;
            this.mVoiceContentBottom = i;
            this.mContentBottom = i;
            this.mDockBottom = i;
        }
        this.mDockLayer = 268435456;
        this.mStatusBarLayer = -1;
        Rect pf = mTmpParentFrame;
        Rect df = mTmpDisplayFrame;
        Rect of = mTmpOverscanFrame;
        Rect vf = mTmpVisibleFrame;
        Rect dcf = mTmpDecorFrame;
        i = this.mDockLeft;
        vf.left = i;
        of.left = i;
        df.left = i;
        pf.left = i;
        i = this.mDockTop;
        vf.top = i;
        of.top = i;
        df.top = i;
        pf.top = i;
        i = this.mDockRight;
        vf.right = i;
        of.right = i;
        df.right = i;
        pf.right = i;
        i = this.mDockBottom;
        vf.bottom = i;
        of.bottom = i;
        df.bottom = i;
        pf.bottom = i;
        dcf.setEmpty();
        if (isDefaultDisplay) {
            int sysui = this.mLastSystemUiFlags;
            boolean navVisible = (sysui & 2) == 0;
            boolean navTranslucent = (-2147450880 & sysui) != 0;
            boolean immersive = (sysui & 2048) != 0;
            boolean immersiveSticky = (sysui & 4096) != 0;
            boolean navAllowedHidden = !immersive ? immersiveSticky : true;
            navTranslucent &= immersiveSticky ? 0 : 1;
            boolean isKeyguardShowing = isStatusBarKeyguard() && !this.mHideLockScreen;
            if (!isKeyguardShowing) {
                navTranslucent &= areTranslucentBarsAllowed();
            }
            boolean statusBarExpandedNotKeyguard = (isKeyguardShowing || this.mStatusBar == null || this.mStatusBar.getAttrs().height != -1) ? false : this.mStatusBar.getAttrs().width == -1;
            if (navVisible || navAllowedHidden) {
                if (this.mInputConsumer != null) {
                    this.mHandler.sendMessage(this.mHandler.obtainMessage(19, this.mInputConsumer));
                    this.mInputConsumer = null;
                }
            } else if (this.mInputConsumer == null) {
                this.mInputConsumer = this.mWindowManagerFuncs.addInputConsumer(this.mHandler.getLooper(), this.mHideNavInputEventReceiverFactory);
            }
            navVisible |= canHideNavigationBar() ? 0 : 1;
            if (isCompatNavBar) {
                if (displayRotation == 0 || displayRotation == 2) {
                    displayHeight = -1;
                } else if (displayRotation == 1 || displayRotation == 3) {
                    displayWidth = -1;
                }
            }
            if (isCompatWindow) {
                if (displayRotation == 0 || displayRotation == 2) {
                    displayHeight = -3;
                } else if (displayRotation == 1 || displayRotation == 3) {
                    displayWidth = -3;
                }
            }
            if (isIgnoreNavBar) {
                if (displayRotation == 0 || displayRotation == 2) {
                    displayHeight = -2;
                } else if (displayRotation == 1 || displayRotation == 3) {
                    displayWidth = -2;
                }
            }
            boolean updateSysUiVisibility = layoutNavigationBar(displayWidth, displayHeight, displayRotation, uiMode, overscanLeft, overscanRight, overscanBottom, dcf, navVisible, navTranslucent, navAllowedHidden, statusBarExpandedNotKeyguard);
            if (DEBUG_LAYOUT) {
                String str = TAG;
                Object[] objArr = new Object[4];
                objArr[0] = Integer.valueOf(this.mDockLeft);
                objArr[1] = Integer.valueOf(this.mDockTop);
                objArr[2] = Integer.valueOf(this.mDockRight);
                objArr[3] = Integer.valueOf(this.mDockBottom);
                Slog.i(str, String.format("mDock rect: (%d,%d - %d,%d)", objArr));
            }
            if (!isCompatWindow && (updateSysUiVisibility | layoutStatusBar(pf, df, of, vf, dcf, sysui, isKeyguardShowing))) {
                updateSystemUiVisibilityLw();
            }
        }
    }

    private boolean layoutStatusBarForCompat(Rect pf, Rect df, Rect of, Rect vf, Rect dcf, int sysui, boolean isKeyguardShowing, int displayRotation, boolean isCompatWindow) {
        if (this.mStatusBar != null) {
            this.mStatusBarLayer = this.mStatusBar.getSurfaceLayer();
            this.mStableTop = this.mUnrestrictedScreenTop + this.mStatusBarHeight;
            boolean statusBarTransient = (67108864 & sysui) != 0;
            boolean statusBarTranslucent = (1073741832 & sysui) != 0;
            if (!isKeyguardShowing) {
                statusBarTranslucent &= areTranslucentBarsAllowed();
            }
            if (this.mStatusBar.isVisibleLw() && !statusBarTransient) {
                this.mDockTop = this.mUnrestrictedScreenTop + this.mStatusBarHeight;
                int i = this.mDockTop;
                this.mCurTop = i;
                this.mVoiceContentTop = i;
                this.mContentTop = i;
                i = this.mDockBottom;
                this.mCurBottom = i;
                this.mVoiceContentBottom = i;
                this.mContentBottom = i;
                i = this.mDockLeft;
                this.mCurLeft = i;
                this.mVoiceContentLeft = i;
                this.mContentLeft = i;
                i = this.mDockRight;
                this.mCurRight = i;
                this.mVoiceContentRight = i;
                this.mContentRight = i;
                if (DEBUG_LAYOUT) {
                    String str = TAG;
                    StringBuilder append = new StringBuilder().append("Status bar: ");
                    Object[] objArr = new Object[12];
                    objArr[0] = Integer.valueOf(this.mDockLeft);
                    objArr[1] = Integer.valueOf(this.mDockTop);
                    objArr[2] = Integer.valueOf(this.mDockRight);
                    objArr[3] = Integer.valueOf(this.mDockBottom);
                    objArr[4] = Integer.valueOf(this.mContentLeft);
                    objArr[5] = Integer.valueOf(this.mContentTop);
                    objArr[6] = Integer.valueOf(this.mContentRight);
                    objArr[7] = Integer.valueOf(this.mContentBottom);
                    objArr[8] = Integer.valueOf(this.mCurLeft);
                    objArr[9] = Integer.valueOf(this.mCurTop);
                    objArr[10] = Integer.valueOf(this.mCurRight);
                    objArr[11] = Integer.valueOf(this.mCurBottom);
                    Slog.v(str, append.append(String.format("dock=[%d,%d][%d,%d] content=[%d,%d][%d,%d] cur=[%d,%d][%d,%d]", objArr)).toString());
                }
            }
            if (!(!this.mStatusBar.isVisibleLw() || this.mStatusBar.isAnimatingLw() || statusBarTransient || statusBarTranslucent || this.mStatusBarController.wasRecentlyTranslucent())) {
                this.mSystemTop = this.mUnrestrictedScreenTop + this.mStatusBarHeight;
            }
            if (this.mStatusBarController.checkHiddenLw()) {
                return true;
            }
        }
        return false;
    }

    private boolean layoutStatusBar(Rect pf, Rect df, Rect of, Rect vf, Rect dcf, int sysui, boolean isKeyguardShowing) {
        if (this.mStatusBar != null) {
            int i = this.mUnrestrictedScreenLeft;
            of.left = i;
            df.left = i;
            pf.left = i;
            i = this.mUnrestrictedScreenTop;
            of.top = i;
            df.top = i;
            pf.top = i;
            i = this.mUnrestrictedScreenWidth + this.mUnrestrictedScreenLeft;
            of.right = i;
            df.right = i;
            pf.right = i;
            i = this.mUnrestrictedScreenHeight + this.mUnrestrictedScreenTop;
            of.bottom = i;
            df.bottom = i;
            pf.bottom = i;
            vf.left = this.mStableLeft;
            vf.top = this.mStableTop;
            vf.right = this.mStableRight;
            vf.bottom = this.mStableBottom;
            this.mStatusBarLayer = this.mStatusBar.getSurfaceLayer();
            this.mStatusBar.computeFrameLw(pf, df, vf, vf, vf, dcf, vf, vf);
            this.mStableTop = this.mUnrestrictedScreenTop + this.mStatusBarHeight;
            boolean statusBarTransient = (67108864 & sysui) != 0;
            boolean statusBarTranslucent = (1073741832 & sysui) != 0;
            if (!isKeyguardShowing) {
                statusBarTranslucent &= areTranslucentBarsAllowed();
            }
            if (this.mStatusBar.isVisibleLw() && !statusBarTransient) {
                this.mDockTop = this.mUnrestrictedScreenTop + this.mStatusBarHeight;
                i = this.mDockTop;
                this.mCurTop = i;
                this.mVoiceContentTop = i;
                this.mContentTop = i;
                i = this.mDockBottom;
                this.mCurBottom = i;
                this.mVoiceContentBottom = i;
                this.mContentBottom = i;
                i = this.mDockLeft;
                this.mCurLeft = i;
                this.mVoiceContentLeft = i;
                this.mContentLeft = i;
                i = this.mDockRight;
                this.mCurRight = i;
                this.mVoiceContentRight = i;
                this.mContentRight = i;
                if (DEBUG_LAYOUT) {
                    String str = TAG;
                    StringBuilder append = new StringBuilder().append("Status bar: ");
                    Object[] objArr = new Object[12];
                    objArr[0] = Integer.valueOf(this.mDockLeft);
                    objArr[1] = Integer.valueOf(this.mDockTop);
                    objArr[2] = Integer.valueOf(this.mDockRight);
                    objArr[3] = Integer.valueOf(this.mDockBottom);
                    objArr[4] = Integer.valueOf(this.mContentLeft);
                    objArr[5] = Integer.valueOf(this.mContentTop);
                    objArr[6] = Integer.valueOf(this.mContentRight);
                    objArr[7] = Integer.valueOf(this.mContentBottom);
                    objArr[8] = Integer.valueOf(this.mCurLeft);
                    objArr[9] = Integer.valueOf(this.mCurTop);
                    objArr[10] = Integer.valueOf(this.mCurRight);
                    objArr[11] = Integer.valueOf(this.mCurBottom);
                    Slog.v(str, append.append(String.format("dock=[%d,%d][%d,%d] content=[%d,%d][%d,%d] cur=[%d,%d][%d,%d]", objArr)).toString());
                }
            }
            if (!(!this.mStatusBar.isVisibleLw() || this.mStatusBar.isAnimatingLw() || statusBarTransient || statusBarTranslucent || this.mStatusBarController.wasRecentlyTranslucent())) {
                this.mSystemTop = this.mUnrestrictedScreenTop + this.mStatusBarHeight;
            }
            if (this.mStatusBarController.checkHiddenLw()) {
                return true;
            }
        }
        return false;
    }

    private boolean layoutNavigationBar(int displayWidth, int displayHeight, int displayRotation, int uiMode, int overscanLeft, int overscanRight, int overscanBottom, Rect dcf, boolean navVisible, boolean navTranslucent, boolean navAllowedHidden, boolean statusBarExpandedNotKeyguard) {
        int naturalHeight = this.mNaturalHeight;
        boolean isCompatNavBar = false;
        boolean isIgnoreComputeFrame = false;
        boolean isNomWindow = false;
        if (displayRotation == 0 || displayRotation == 2) {
            if (displayHeight == -1) {
                displayHeight = naturalHeight;
                isCompatNavBar = true;
            }
            if (displayHeight == -3) {
                displayHeight = naturalHeight;
                isIgnoreComputeFrame = true;
            }
            if (displayHeight == -2) {
                displayHeight = naturalHeight;
                isNomWindow = true;
            }
        } else if (displayRotation == 1 || displayRotation == 3) {
            if (displayWidth == -1) {
                displayWidth = naturalHeight;
                isCompatNavBar = true;
            }
            if (displayWidth == -3) {
                displayWidth = naturalHeight;
                isIgnoreComputeFrame = true;
            }
            if (displayWidth == -2) {
                displayWidth = naturalHeight;
                isNomWindow = true;
            }
        }
        if (this.mNavigationBar != null) {
            int i;
            boolean transientNavBarShowing = this.mNavigationBarController.isTransientShowing();
            this.mHideNavigationBar = 1 == SystemProperties.getInt("oppo.hide.navigationbar", 0);
            this.mNavigationBarPosition = navigationBarPosition(displayWidth, displayHeight, displayRotation);
            if (this.mNavigationBarPosition == 0) {
                int top;
                int tmpTop;
                if (isCompatNavBar || isIgnoreComputeFrame) {
                    top = (displayHeight - overscanBottom) - getNavigationBarHeight(displayRotation, uiMode);
                    tmpTop = (displayHeight - overscanBottom) - getNavigationBarHeight(displayRotation, uiMode);
                } else {
                    top = (displayHeight - overscanBottom) - getNavigationBarHeight(displayRotation, uiMode);
                    tmpTop = (displayHeight - overscanBottom) - getNavigationBarHeight(displayRotation, uiMode);
                }
                if (this.mHasNavigationBar && this.mHideNavigationBar && !isIgnoreComputeFrame) {
                    mTmpNavigationFrame.set(0, displayHeight, displayWidth, this.mNavigationBarHeightForRotationDefault[0] + displayHeight);
                } else {
                    mTmpNavigationFrame.set(0, top, displayWidth, displayHeight - overscanBottom);
                }
                i = mTmpNavigationFrame.top;
                this.mStableFullscreenBottom = i;
                this.mStableBottom = i;
                if (transientNavBarShowing) {
                    this.mNavigationBarController.setBarShowingLw(true);
                } else if (!navVisible) {
                    this.mNavigationBarController.setBarShowingLw(statusBarExpandedNotKeyguard);
                } else if (!(this.mIsAlarmBoot || this.mIsShutDown)) {
                    this.mNavigationBarController.setBarShowingLw(true);
                    this.mDockBottom = mTmpNavigationFrame.top;
                    this.mRestrictedScreenHeight = this.mDockBottom - this.mRestrictedScreenTop;
                    this.mRestrictedOverscanScreenHeight = this.mDockBottom - this.mRestrictedOverscanScreenTop;
                }
                if (!(!navVisible || navTranslucent || navAllowedHidden || this.mNavigationBar.isAnimatingLw() || this.mNavigationBarController.wasRecentlyTranslucent())) {
                    this.mSystemBottom = mTmpNavigationFrame.top;
                }
            } else if (this.mNavigationBarPosition == 1) {
                int left;
                int tmpleft;
                if (isCompatNavBar || isIgnoreComputeFrame) {
                    left = (displayWidth - overscanRight) - getNavigationBarWidth(displayRotation, uiMode);
                    tmpleft = (displayWidth - overscanRight) - getNavigationBarWidth(displayRotation, uiMode);
                } else {
                    left = (displayWidth - overscanRight) - getNavigationBarWidth(displayRotation, uiMode);
                    tmpleft = (displayWidth - overscanRight) - getNavigationBarWidth(displayRotation, uiMode);
                }
                if (this.mHasNavigationBar && this.mHideNavigationBar && !isIgnoreComputeFrame) {
                    mTmpNavigationFrame.set(displayWidth, 0, this.mNavigationBarHeightForRotationDefault[0] + displayWidth, displayHeight);
                } else {
                    mTmpNavigationFrame.set(left, 0, displayWidth - overscanRight, displayHeight);
                }
                i = mTmpNavigationFrame.left;
                this.mStableFullscreenRight = i;
                this.mStableRight = i;
                if (transientNavBarShowing) {
                    this.mNavigationBarController.setBarShowingLw(true);
                } else if (!navVisible) {
                    this.mNavigationBarController.setBarShowingLw(statusBarExpandedNotKeyguard);
                } else if (!(this.mIsAlarmBoot || this.mIsShutDown)) {
                    this.mNavigationBarController.setBarShowingLw(true);
                    this.mDockRight = mTmpNavigationFrame.left;
                    this.mRestrictedScreenWidth = this.mDockRight - this.mRestrictedScreenLeft;
                    this.mRestrictedOverscanScreenWidth = this.mDockRight - this.mRestrictedOverscanScreenLeft;
                }
                if (!(!navVisible || navTranslucent || navAllowedHidden || this.mNavigationBar.isAnimatingLw() || this.mNavigationBarController.wasRecentlyTranslucent())) {
                    this.mSystemRight = mTmpNavigationFrame.left;
                }
            } else if (this.mNavigationBarPosition == 2) {
                int right;
                int tmpRight;
                if (isCompatNavBar || isIgnoreComputeFrame) {
                    right = overscanLeft + getNavigationBarWidth(displayRotation, uiMode);
                    tmpRight = overscanLeft + getNavigationBarWidth(displayRotation, uiMode);
                } else {
                    right = overscanLeft + getNavigationBarWidth(displayRotation, uiMode);
                    tmpRight = overscanLeft + getNavigationBarWidth(displayRotation, uiMode);
                }
                if (this.mHasNavigationBar && this.mHideNavigationBar && !isIgnoreComputeFrame) {
                    mTmpNavigationFrame.set(overscanLeft - this.mNavigationBarHeightForRotationDefault[0], 0, overscanLeft, displayHeight);
                } else {
                    mTmpNavigationFrame.set(overscanLeft, 0, right, displayHeight);
                }
                i = mTmpNavigationFrame.right;
                this.mStableFullscreenLeft = i;
                this.mStableLeft = i;
                if (transientNavBarShowing) {
                    this.mNavigationBarController.setBarShowingLw(true);
                } else if (!navVisible) {
                    this.mNavigationBarController.setBarShowingLw(statusBarExpandedNotKeyguard);
                } else if (!(this.mIsAlarmBoot || this.mIsShutDown)) {
                    this.mNavigationBarController.setBarShowingLw(true);
                    this.mDockLeft = mTmpNavigationFrame.right;
                    i = this.mDockLeft;
                    this.mRestrictedOverscanScreenLeft = i;
                    this.mRestrictedScreenLeft = i;
                    this.mRestrictedScreenWidth = this.mDockRight - this.mRestrictedScreenLeft;
                    this.mRestrictedOverscanScreenWidth = this.mDockRight - this.mRestrictedOverscanScreenLeft;
                }
                if (!(!navVisible || navTranslucent || navAllowedHidden || this.mNavigationBar.isAnimatingLw() || this.mNavigationBarController.wasRecentlyTranslucent())) {
                    this.mSystemLeft = mTmpNavigationFrame.right;
                }
            }
            i = this.mDockTop;
            this.mCurTop = i;
            this.mVoiceContentTop = i;
            this.mContentTop = i;
            i = this.mDockBottom;
            this.mCurBottom = i;
            this.mVoiceContentBottom = i;
            this.mContentBottom = i;
            i = this.mDockLeft;
            this.mCurLeft = i;
            this.mVoiceContentLeft = i;
            this.mContentLeft = i;
            i = this.mDockRight;
            this.mCurRight = i;
            this.mVoiceContentRight = i;
            this.mContentRight = i;
            this.mStatusBarLayer = this.mNavigationBar.getSurfaceLayer();
            if (!(isIgnoreComputeFrame || isNomWindow)) {
                this.mNavigationBar.computeFrameLw(mTmpNavigationFrame, mTmpNavigationFrame, mTmpNavigationFrame, mTmpNavigationFrame, mTmpNavigationFrame, dcf, mTmpNavigationFrame, mTmpNavigationFrame);
            }
            if (DEBUG_LAYOUT) {
                Slog.i(TAG, "mNavigationBar frame: " + mTmpNavigationFrame);
            }
            if (this.mNavigationBarController.checkHiddenLw()) {
                return true;
            }
        }
        return false;
    }

    private int navigationBarPosition(int displayWidth, int displayHeight, int displayRotation) {
        if (!this.mNavigationBarCanMove || displayWidth <= displayHeight) {
            return 0;
        }
        if (displayRotation == 3) {
            return 2;
        }
        return 1;
    }

    public int getSystemDecorLayerLw() {
        if (this.mStatusBar != null && this.mStatusBar.isVisibleLw()) {
            return this.mStatusBar.getSurfaceLayer();
        }
        if (this.mNavigationBar == null || !this.mNavigationBar.isVisibleLw()) {
            return 0;
        }
        return this.mNavigationBar.getSurfaceLayer();
    }

    public void getContentRectLw(Rect r) {
        r.set(this.mContentLeft, this.mContentTop, this.mContentRight, this.mContentBottom);
    }

    void setAttachedWindowFrames(WindowState win, int fl, int adjust, WindowState attached, boolean insetDecors, Rect pf, Rect df, Rect of, Rect cf, Rect vf) {
        if (win.getSurfaceLayer() <= this.mDockLayer || attached.getSurfaceLayer() >= this.mDockLayer) {
            Rect displayFrameLw;
            if (adjust != 16) {
                cf.set((1073741824 & fl) != 0 ? attached.getContentFrameLw() : attached.getOverscanFrameLw());
            } else {
                cf.set(attached.getContentFrameLw());
                if (attached.isVoiceInteraction()) {
                    if (cf.left < this.mVoiceContentLeft) {
                        cf.left = this.mVoiceContentLeft;
                    }
                    if (cf.top < this.mVoiceContentTop) {
                        cf.top = this.mVoiceContentTop;
                    }
                    if (cf.right > this.mVoiceContentRight) {
                        cf.right = this.mVoiceContentRight;
                    }
                    if (cf.bottom > this.mVoiceContentBottom) {
                        cf.bottom = this.mVoiceContentBottom;
                    }
                } else if (attached.getSurfaceLayer() < this.mDockLayer) {
                    if (cf.left < this.mContentLeft) {
                        cf.left = this.mContentLeft;
                    }
                    if (cf.top < this.mContentTop) {
                        cf.top = this.mContentTop;
                    }
                    if (cf.right > this.mContentRight) {
                        cf.right = this.mContentRight;
                    }
                    if (cf.bottom > this.mContentBottom) {
                        cf.bottom = this.mContentBottom;
                    }
                }
            }
            if (insetDecors) {
                displayFrameLw = attached.getDisplayFrameLw();
            } else {
                displayFrameLw = cf;
            }
            df.set(displayFrameLw);
            if (insetDecors) {
                cf = attached.getOverscanFrameLw();
            }
            of.set(cf);
            vf.set(attached.getVisibleFrameLw());
        } else {
            int i = this.mDockLeft;
            vf.left = i;
            cf.left = i;
            of.left = i;
            df.left = i;
            i = this.mDockTop;
            vf.top = i;
            cf.top = i;
            of.top = i;
            df.top = i;
            i = this.mDockRight;
            vf.right = i;
            cf.right = i;
            of.right = i;
            df.right = i;
            i = this.mDockBottom;
            vf.bottom = i;
            cf.bottom = i;
            of.bottom = i;
            df.bottom = i;
        }
        if ((fl & 256) == 0) {
            df = attached.getFrameLw();
        }
        pf.set(df);
    }

    private void applyStableConstraints(int sysui, int fl, Rect r) {
        if ((sysui & 256) == 0) {
            return;
        }
        if ((fl & 1024) != 0) {
            if (r.left < this.mStableFullscreenLeft) {
                r.left = this.mStableFullscreenLeft;
            }
            if (r.top < this.mStableFullscreenTop) {
                r.top = this.mStableFullscreenTop;
            }
            if (r.right > this.mStableFullscreenRight) {
                r.right = this.mStableFullscreenRight;
            }
            if (r.bottom > this.mStableFullscreenBottom) {
                r.bottom = this.mStableFullscreenBottom;
                return;
            }
            return;
        }
        if (r.left < this.mStableLeft) {
            r.left = this.mStableLeft;
        }
        if (r.top < this.mStableTop) {
            r.top = this.mStableTop;
        }
        if (r.right > this.mStableRight) {
            r.right = this.mStableRight;
        }
        if (r.bottom > this.mStableBottom) {
            r.bottom = this.mStableBottom;
        }
    }

    private boolean canReceiveInput(WindowState win) {
        boolean altFocusableIm;
        boolean notFocusable = (win.getAttrs().flags & 8) != 0;
        if ((win.getAttrs().flags & DumpState.DUMP_INTENT_FILTER_VERIFIERS) != 0) {
            altFocusableIm = true;
        } else {
            altFocusableIm = false;
        }
        if (notFocusable ^ altFocusableIm) {
            return false;
        }
        return true;
    }

    public void reLayoutInputMethod(WindowState win, WindowState attached) {
        if ((win != this.mStatusBar || canReceiveInput(win)) && win != this.mNavigationBar) {
            LayoutParams attrs = win.getAttrs();
            boolean isDefaultDisplay = win.isDefaultDisplay();
            if (attrs.type == 2011 && win.isVisibleOrBehindKeyguardLw() && win.isDisplayedLw() && !win.getGivenInsetsPendingLw()) {
                setLastInputMethodWindowLw(null, null);
                offsetInputMethodWindowLw(win);
            }
        }
    }

    public void layoutWindowLw(WindowState win, WindowState attached) {
        if ((win != this.mStatusBar || canReceiveInput(win)) && win != this.mNavigationBar) {
            boolean hasNavBar;
            String str;
            Rect osf;
            LayoutParams attrs = win.getAttrs();
            boolean isDefaultDisplay = win.isDefaultDisplay();
            boolean needsToOffsetInputMethodTarget = isDefaultDisplay ? win == this.mLastInputMethodTargetWindow && this.mLastInputMethodWindow != null : false;
            if (needsToOffsetInputMethodTarget) {
                if (DEBUG_LAYOUT) {
                    Slog.i(TAG, "Offset ime target window by the last ime window state");
                }
                offsetInputMethodWindowLw(this.mLastInputMethodWindow);
            }
            int fl = PolicyControl.getWindowFlags(win, attrs);
            int pfl = attrs.privateFlags;
            int sim = attrs.softInputMode;
            int sysUiFl = PolicyControl.getSystemUiVisibility(win, null);
            Rect pf = mTmpParentFrame;
            Rect df = mTmpDisplayFrame;
            Rect of = mTmpOverscanFrame;
            Rect cf = mTmpContentFrame;
            Rect vf = mTmpVisibleFrame;
            Rect dcf = mTmpDecorFrame;
            Rect sf = mTmpStableFrame;
            dcf.setEmpty();
            if (isDefaultDisplay && this.mHasNavigationBar && this.mNavigationBar != null) {
                hasNavBar = this.mNavigationBar.isVisibleLw();
            } else {
                hasNavBar = false;
            }
            int adjust = sim & 240;
            if (isDefaultDisplay) {
                sf.set(this.mStableLeft, this.mStableTop, this.mStableRight, this.mStableBottom);
            } else {
                sf.set(this.mOverscanLeft, this.mOverscanTop, this.mOverscanRight, this.mOverscanBottom);
            }
            int i;
            if (isDefaultDisplay) {
                if (attrs.type == 2011) {
                    i = this.mDockLeft;
                    vf.left = i;
                    cf.left = i;
                    of.left = i;
                    df.left = i;
                    pf.left = i;
                    i = this.mDockTop;
                    vf.top = i;
                    cf.top = i;
                    of.top = i;
                    df.top = i;
                    pf.top = i;
                    i = this.mDockRight;
                    vf.right = i;
                    cf.right = i;
                    of.right = i;
                    df.right = i;
                    pf.right = i;
                    if (this.mHideNavigationBar) {
                        i = this.mUnrestrictedScreenTop + this.mUnrestrictedScreenHeight;
                    } else {
                        i = this.mRestrictedScreenTop + this.mRestrictedScreenHeight;
                    }
                    of.bottom = i;
                    df.bottom = i;
                    pf.bottom = i;
                    i = this.mStableBottom;
                    vf.bottom = i;
                    cf.bottom = i;
                    if (this.mStatusBar != null && this.mFocusedWindow == this.mStatusBar) {
                        if (canReceiveInput(this.mStatusBar)) {
                            if (this.mNavigationBarPosition == 1) {
                                i = this.mStableRight;
                                vf.right = i;
                                cf.right = i;
                                of.right = i;
                                df.right = i;
                                pf.right = i;
                            } else if (this.mNavigationBarPosition == 2) {
                                i = this.mStableLeft;
                                vf.left = i;
                                cf.left = i;
                                of.left = i;
                                df.left = i;
                                pf.left = i;
                            }
                        }
                    }
                    attrs.gravity = 80;
                    this.mDockLayer = win.getSurfaceLayer();
                } else if (attrs.type == 2031) {
                    i = this.mUnrestrictedScreenLeft;
                    of.left = i;
                    df.left = i;
                    pf.left = i;
                    i = this.mUnrestrictedScreenTop;
                    of.top = i;
                    df.top = i;
                    pf.top = i;
                    i = this.mUnrestrictedScreenLeft + this.mUnrestrictedScreenWidth;
                    of.right = i;
                    df.right = i;
                    pf.right = i;
                    i = this.mUnrestrictedScreenTop + this.mUnrestrictedScreenHeight;
                    of.bottom = i;
                    df.bottom = i;
                    pf.bottom = i;
                    if (adjust != 16) {
                        cf.left = this.mDockLeft;
                        cf.top = this.mDockTop;
                        cf.right = this.mDockRight;
                        cf.bottom = this.mDockBottom;
                    } else {
                        cf.left = this.mContentLeft;
                        cf.top = this.mContentTop;
                        cf.right = this.mContentRight;
                        cf.bottom = this.mContentBottom;
                    }
                    if (adjust != 48) {
                        vf.left = this.mCurLeft;
                        vf.top = this.mCurTop;
                        vf.right = this.mCurRight;
                        vf.bottom = this.mCurBottom;
                    } else {
                        vf.set(cf);
                    }
                } else if (attrs.type == 2013) {
                    layoutWallpaper(win, pf, df, of, cf);
                } else if (win == this.mStatusBar || ((win == this.mKeyguard && (attrs.privateFlags & 1024) != 0) || attrs.type == 2009)) {
                    i = this.mUnrestrictedScreenLeft;
                    of.left = i;
                    df.left = i;
                    pf.left = i;
                    i = this.mUnrestrictedScreenTop;
                    of.top = i;
                    df.top = i;
                    pf.top = i;
                    i = this.mUnrestrictedScreenWidth + this.mUnrestrictedScreenLeft;
                    of.right = i;
                    df.right = i;
                    pf.right = i;
                    i = this.mUnrestrictedScreenHeight + this.mUnrestrictedScreenTop;
                    of.bottom = i;
                    df.bottom = i;
                    pf.bottom = i;
                    i = this.mStableLeft;
                    vf.left = i;
                    cf.left = i;
                    i = this.mStableTop;
                    vf.top = i;
                    cf.top = i;
                    i = this.mStableRight;
                    vf.right = i;
                    cf.right = i;
                    vf.bottom = this.mStableBottom;
                    if (adjust == 16) {
                        cf.bottom = this.mContentBottom;
                    } else {
                        cf.bottom = this.mDockBottom;
                        vf.bottom = this.mContentBottom;
                    }
                } else {
                    dcf.left = this.mSystemLeft;
                    dcf.top = this.mSystemTop;
                    dcf.right = this.mSystemRight;
                    dcf.bottom = this.mSystemBottom;
                    boolean inheritTranslucentDecor = (attrs.privateFlags & 512) != 0;
                    boolean isAppWindow = attrs.type >= 1 ? attrs.type <= 99 : false;
                    if (win != this.mTopFullscreenOpaqueWindowState || win.isAnimatingLw()) {
                    }
                    if (isAppWindow && !inheritTranslucentDecor) {
                        if ((sysUiFl & 4) == 0 && (fl & 1024) == 0 && (67108864 & fl) == 0 && (Integer.MIN_VALUE & fl) == 0 && (DumpState.DUMP_INTENT_FILTER_VERIFIERS & pfl) == 0) {
                            dcf.top = this.mStableTop;
                        }
                        if ((134217728 & fl) == 0 && (sysUiFl & 2) == 0 && ((Integer.MIN_VALUE & fl) == 0 || (attrs.navigationBarVisibility & 536870912) != 0)) {
                            dcf.bottom = this.mStableBottom;
                            dcf.right = this.mStableRight;
                        }
                    }
                    Object[] objArr;
                    if ((65792 & fl) == 65792) {
                        if (DEBUG_LAYOUT) {
                            Slog.v(TAG, "layoutWindowLw(" + attrs.getTitle() + "): IN_SCREEN, INSET_DECOR, sim=#" + Integer.toHexString(adjust) + ", type=" + attrs.type + ", flag=" + fl + ", canHideNavigationBar=" + canHideNavigationBar() + ", sysUiFl=" + sysUiFl);
                        }
                        if (attached != null) {
                            setAttachedWindowFrames(win, fl, adjust, attached, true, pf, df, of, cf, vf);
                        } else {
                            if (attrs.type == 2014 || attrs.type == 2017) {
                                i = hasNavBar ? this.mDockLeft : this.mUnrestrictedScreenLeft;
                                of.left = i;
                                df.left = i;
                                pf.left = i;
                                i = this.mUnrestrictedScreenTop;
                                of.top = i;
                                df.top = i;
                                pf.top = i;
                                if (hasNavBar) {
                                    i = this.mRestrictedScreenLeft + this.mRestrictedScreenWidth;
                                } else {
                                    i = this.mUnrestrictedScreenLeft + this.mUnrestrictedScreenWidth;
                                }
                                of.right = i;
                                df.right = i;
                                pf.right = i;
                                if (hasNavBar) {
                                    i = this.mRestrictedScreenTop + this.mRestrictedScreenHeight;
                                } else {
                                    i = this.mUnrestrictedScreenTop + this.mUnrestrictedScreenHeight;
                                }
                                of.bottom = i;
                                df.bottom = i;
                                pf.bottom = i;
                                if (DEBUG_LAYOUT) {
                                    str = TAG;
                                    objArr = new Object[4];
                                    objArr[0] = Integer.valueOf(pf.left);
                                    objArr[1] = Integer.valueOf(pf.top);
                                    objArr[2] = Integer.valueOf(pf.right);
                                    objArr[3] = Integer.valueOf(pf.bottom);
                                    Slog.v(str, String.format("Laying out status bar window: (%d,%d - %d,%d)", objArr));
                                }
                            } else if ((33554432 & fl) != 0 && attrs.type >= 1 && attrs.type <= 1999) {
                                i = this.mOverscanScreenLeft;
                                of.left = i;
                                df.left = i;
                                pf.left = i;
                                i = this.mOverscanScreenTop;
                                of.top = i;
                                df.top = i;
                                pf.top = i;
                                i = this.mOverscanScreenLeft + this.mOverscanScreenWidth;
                                of.right = i;
                                df.right = i;
                                pf.right = i;
                                i = this.mOverscanScreenTop + this.mOverscanScreenHeight;
                                of.bottom = i;
                                df.bottom = i;
                                pf.bottom = i;
                            } else if (!canHideNavigationBar() || (sysUiFl & 512) == 0 || (attrs.type != 2037 && (attrs.type < 1 || attrs.type > 1999))) {
                                i = this.mRestrictedOverscanScreenLeft;
                                df.left = i;
                                pf.left = i;
                                i = this.mRestrictedOverscanScreenTop;
                                df.top = i;
                                pf.top = i;
                                i = this.mRestrictedOverscanScreenLeft + this.mRestrictedOverscanScreenWidth;
                                df.right = i;
                                pf.right = i;
                                i = this.mRestrictedOverscanScreenTop + this.mRestrictedOverscanScreenHeight;
                                df.bottom = i;
                                pf.bottom = i;
                                of.left = this.mUnrestrictedScreenLeft;
                                of.top = this.mUnrestrictedScreenTop;
                                of.right = this.mUnrestrictedScreenLeft + this.mUnrestrictedScreenWidth;
                                of.bottom = this.mUnrestrictedScreenTop + this.mUnrestrictedScreenHeight;
                            } else {
                                i = this.mOverscanScreenLeft;
                                df.left = i;
                                pf.left = i;
                                i = this.mOverscanScreenTop;
                                df.top = i;
                                pf.top = i;
                                i = this.mOverscanScreenLeft + this.mOverscanScreenWidth;
                                df.right = i;
                                pf.right = i;
                                i = this.mOverscanScreenTop + this.mOverscanScreenHeight;
                                df.bottom = i;
                                pf.bottom = i;
                                of.left = this.mUnrestrictedScreenLeft;
                                of.top = this.mUnrestrictedScreenTop;
                                of.right = this.mUnrestrictedScreenLeft + this.mUnrestrictedScreenWidth;
                                of.bottom = this.mUnrestrictedScreenTop + this.mUnrestrictedScreenHeight;
                            }
                            if ((fl & 1024) != 0) {
                                cf.left = this.mRestrictedScreenLeft;
                                cf.top = this.mRestrictedScreenTop;
                                cf.right = this.mRestrictedScreenLeft + this.mRestrictedScreenWidth;
                                cf.bottom = this.mRestrictedScreenTop + this.mRestrictedScreenHeight;
                            } else if (win.isVoiceInteraction()) {
                                cf.left = this.mVoiceContentLeft;
                                cf.top = this.mVoiceContentTop;
                                cf.right = this.mVoiceContentRight;
                                cf.bottom = this.mVoiceContentBottom;
                            } else if (adjust != 16) {
                                cf.left = this.mDockLeft;
                                cf.top = this.mDockTop;
                                cf.right = this.mDockRight;
                                cf.bottom = this.mDockBottom;
                            } else {
                                cf.left = this.mContentLeft;
                                cf.top = this.mContentTop;
                                cf.right = this.mContentRight;
                                cf.bottom = this.mContentBottom;
                            }
                            applyStableConstraints(sysUiFl, fl, cf);
                            if (adjust != 48) {
                                vf.left = this.mCurLeft;
                                vf.top = this.mCurTop;
                                vf.right = this.mCurRight;
                                vf.bottom = this.mCurBottom;
                            } else {
                                vf.set(cf);
                            }
                        }
                    } else if ((fl & 256) != 0 || (sysUiFl & 1536) != 0) {
                        if (DEBUG_LAYOUT) {
                            Slog.v(TAG, "layoutWindowLw(" + attrs.getTitle() + "): IN_SCREEN, type=" + attrs.type + ", flag=" + fl + ", canHideNavigationBar=" + canHideNavigationBar() + ", sysUiFl=" + sysUiFl);
                        }
                        if (attrs.type == 2014 || attrs.type == 2017 || attrs.type == 2020) {
                            i = hasNavBar ? this.mDockLeft : this.mUnrestrictedScreenLeft;
                            cf.left = i;
                            of.left = i;
                            df.left = i;
                            pf.left = i;
                            i = this.mUnrestrictedScreenTop;
                            cf.top = i;
                            of.top = i;
                            df.top = i;
                            pf.top = i;
                            if (hasNavBar) {
                                i = this.mRestrictedScreenLeft + this.mRestrictedScreenWidth;
                            } else {
                                i = this.mUnrestrictedScreenLeft + this.mUnrestrictedScreenWidth;
                            }
                            cf.right = i;
                            of.right = i;
                            df.right = i;
                            pf.right = i;
                            if (hasNavBar) {
                                i = this.mRestrictedScreenTop + this.mRestrictedScreenHeight;
                            } else {
                                i = this.mUnrestrictedScreenTop + this.mUnrestrictedScreenHeight;
                            }
                            cf.bottom = i;
                            of.bottom = i;
                            df.bottom = i;
                            pf.bottom = i;
                            if (DEBUG_LAYOUT) {
                                str = TAG;
                                objArr = new Object[4];
                                objArr[0] = Integer.valueOf(pf.left);
                                objArr[1] = Integer.valueOf(pf.top);
                                objArr[2] = Integer.valueOf(pf.right);
                                objArr[3] = Integer.valueOf(pf.bottom);
                                Slog.v(str, String.format("Laying out IN_SCREEN status bar window: (%d,%d - %d,%d)", objArr));
                            }
                        } else if (attrs.type == 2019 || attrs.type == 2024) {
                            i = this.mUnrestrictedScreenLeft;
                            of.left = i;
                            df.left = i;
                            pf.left = i;
                            i = this.mUnrestrictedScreenTop;
                            of.top = i;
                            df.top = i;
                            pf.top = i;
                            i = this.mUnrestrictedScreenLeft + this.mUnrestrictedScreenWidth;
                            of.right = i;
                            df.right = i;
                            pf.right = i;
                            i = this.mUnrestrictedScreenTop + this.mUnrestrictedScreenHeight;
                            of.bottom = i;
                            df.bottom = i;
                            pf.bottom = i;
                            if (DEBUG_LAYOUT) {
                                str = TAG;
                                objArr = new Object[4];
                                objArr[0] = Integer.valueOf(pf.left);
                                objArr[1] = Integer.valueOf(pf.top);
                                objArr[2] = Integer.valueOf(pf.right);
                                objArr[3] = Integer.valueOf(pf.bottom);
                                Slog.v(str, String.format("Laying out navigation bar window: (%d,%d - %d,%d)", objArr));
                            }
                        } else if ((attrs.type == 2015 || attrs.type == 2021 || attrs.type == 2036) && (fl & 1024) != 0) {
                            i = this.mOverscanScreenLeft;
                            cf.left = i;
                            of.left = i;
                            df.left = i;
                            pf.left = i;
                            i = this.mOverscanScreenTop;
                            cf.top = i;
                            of.top = i;
                            df.top = i;
                            pf.top = i;
                            i = this.mOverscanScreenLeft + this.mOverscanScreenWidth;
                            cf.right = i;
                            of.right = i;
                            df.right = i;
                            pf.right = i;
                            i = this.mOverscanScreenTop + this.mOverscanScreenHeight;
                            cf.bottom = i;
                            of.bottom = i;
                            df.bottom = i;
                            pf.bottom = i;
                        } else if (attrs.type == 2021) {
                            i = this.mOverscanScreenLeft;
                            cf.left = i;
                            of.left = i;
                            df.left = i;
                            pf.left = i;
                            i = this.mOverscanScreenTop;
                            cf.top = i;
                            of.top = i;
                            df.top = i;
                            pf.top = i;
                            i = this.mOverscanScreenLeft + this.mOverscanScreenWidth;
                            cf.right = i;
                            of.right = i;
                            df.right = i;
                            pf.right = i;
                            i = this.mOverscanScreenTop + this.mOverscanScreenHeight;
                            cf.bottom = i;
                            of.bottom = i;
                            df.bottom = i;
                            pf.bottom = i;
                        } else if ((33554432 & fl) != 0 && attrs.type >= 1 && attrs.type <= 1999) {
                            i = this.mOverscanScreenLeft;
                            cf.left = i;
                            of.left = i;
                            df.left = i;
                            pf.left = i;
                            i = this.mOverscanScreenTop;
                            cf.top = i;
                            of.top = i;
                            df.top = i;
                            pf.top = i;
                            i = this.mOverscanScreenLeft + this.mOverscanScreenWidth;
                            cf.right = i;
                            of.right = i;
                            df.right = i;
                            pf.right = i;
                            i = this.mOverscanScreenTop + this.mOverscanScreenHeight;
                            cf.bottom = i;
                            of.bottom = i;
                            df.bottom = i;
                            pf.bottom = i;
                        } else if (canHideNavigationBar() && (sysUiFl & 512) != 0 && (attrs.type == 2000 || attrs.type == 2005 || attrs.type == 2034 || attrs.type == 2033 || (attrs.type >= 1 && attrs.type <= 1999))) {
                            i = this.mUnrestrictedScreenLeft;
                            cf.left = i;
                            of.left = i;
                            df.left = i;
                            pf.left = i;
                            i = this.mUnrestrictedScreenTop;
                            cf.top = i;
                            of.top = i;
                            df.top = i;
                            pf.top = i;
                            i = this.mUnrestrictedScreenLeft + this.mUnrestrictedScreenWidth;
                            cf.right = i;
                            of.right = i;
                            df.right = i;
                            pf.right = i;
                            i = this.mUnrestrictedScreenTop + this.mUnrestrictedScreenHeight;
                            cf.bottom = i;
                            of.bottom = i;
                            df.bottom = i;
                            pf.bottom = i;
                        } else if ((sysUiFl & 1024) != 0) {
                            i = this.mRestrictedScreenLeft;
                            of.left = i;
                            df.left = i;
                            pf.left = i;
                            i = this.mRestrictedScreenTop;
                            of.top = i;
                            df.top = i;
                            pf.top = i;
                            i = this.mRestrictedScreenLeft + this.mRestrictedScreenWidth;
                            of.right = i;
                            df.right = i;
                            pf.right = i;
                            i = this.mRestrictedScreenTop + this.mRestrictedScreenHeight;
                            of.bottom = i;
                            df.bottom = i;
                            pf.bottom = i;
                            if (adjust != 16) {
                                cf.left = this.mDockLeft;
                                cf.top = this.mDockTop;
                                cf.right = this.mDockRight;
                                cf.bottom = this.mDockBottom;
                            } else {
                                cf.left = this.mContentLeft;
                                cf.top = this.mContentTop;
                                cf.right = this.mContentRight;
                                cf.bottom = this.mContentBottom;
                            }
                        } else {
                            i = this.mRestrictedScreenLeft;
                            cf.left = i;
                            of.left = i;
                            df.left = i;
                            pf.left = i;
                            i = this.mRestrictedScreenTop;
                            cf.top = i;
                            of.top = i;
                            df.top = i;
                            pf.top = i;
                            i = this.mRestrictedScreenLeft + this.mRestrictedScreenWidth;
                            cf.right = i;
                            of.right = i;
                            df.right = i;
                            pf.right = i;
                            i = this.mRestrictedScreenTop + this.mRestrictedScreenHeight;
                            cf.bottom = i;
                            of.bottom = i;
                            df.bottom = i;
                            pf.bottom = i;
                        }
                        applyStableConstraints(sysUiFl, fl, cf);
                        if (adjust != 48) {
                            vf.left = this.mCurLeft;
                            vf.top = this.mCurTop;
                            vf.right = this.mCurRight;
                            vf.bottom = this.mCurBottom;
                        } else {
                            vf.set(cf);
                        }
                    } else if (attached != null) {
                        if (DEBUG_LAYOUT) {
                            Slog.v(TAG, "layoutWindowLw(" + attrs.getTitle() + "): attached to " + attached);
                        }
                        setAttachedWindowFrames(win, fl, adjust, attached, false, pf, df, of, cf, vf);
                    } else {
                        if (DEBUG_LAYOUT) {
                            Slog.v(TAG, "layoutWindowLw(" + attrs.getTitle() + "): normal window");
                        }
                        if (attrs.type == 2014 || attrs.type == 2020) {
                            i = this.mRestrictedScreenLeft;
                            cf.left = i;
                            of.left = i;
                            df.left = i;
                            pf.left = i;
                            i = this.mRestrictedScreenTop;
                            cf.top = i;
                            of.top = i;
                            df.top = i;
                            pf.top = i;
                            i = this.mRestrictedScreenLeft + this.mRestrictedScreenWidth;
                            cf.right = i;
                            of.right = i;
                            df.right = i;
                            pf.right = i;
                            i = this.mRestrictedScreenTop + this.mRestrictedScreenHeight;
                            cf.bottom = i;
                            of.bottom = i;
                            df.bottom = i;
                            pf.bottom = i;
                        } else if (attrs.type == 2005 || attrs.type == 2003) {
                            i = this.mStableLeft;
                            cf.left = i;
                            of.left = i;
                            df.left = i;
                            pf.left = i;
                            i = this.mStableTop;
                            cf.top = i;
                            of.top = i;
                            df.top = i;
                            pf.top = i;
                            i = this.mStableRight;
                            cf.right = i;
                            of.right = i;
                            df.right = i;
                            pf.right = i;
                            i = this.mStableBottom;
                            cf.bottom = i;
                            of.bottom = i;
                            df.bottom = i;
                            pf.bottom = i;
                        } else {
                            pf.left = this.mContentLeft;
                            pf.top = this.mContentTop;
                            pf.right = this.mContentRight;
                            pf.bottom = this.mContentBottom;
                            if (win.isVoiceInteraction()) {
                                i = this.mVoiceContentLeft;
                                cf.left = i;
                                of.left = i;
                                df.left = i;
                                i = this.mVoiceContentTop;
                                cf.top = i;
                                of.top = i;
                                df.top = i;
                                i = this.mVoiceContentRight;
                                cf.right = i;
                                of.right = i;
                                df.right = i;
                                i = this.mVoiceContentBottom;
                                cf.bottom = i;
                                of.bottom = i;
                                df.bottom = i;
                            } else if (adjust != 16) {
                                i = this.mDockLeft;
                                cf.left = i;
                                of.left = i;
                                df.left = i;
                                i = this.mDockTop;
                                cf.top = i;
                                of.top = i;
                                df.top = i;
                                i = this.mDockRight;
                                cf.right = i;
                                of.right = i;
                                df.right = i;
                                i = this.mDockBottom;
                                cf.bottom = i;
                                of.bottom = i;
                                df.bottom = i;
                            } else {
                                i = this.mContentLeft;
                                cf.left = i;
                                of.left = i;
                                df.left = i;
                                i = this.mContentTop;
                                cf.top = i;
                                of.top = i;
                                df.top = i;
                                i = this.mContentRight;
                                cf.right = i;
                                of.right = i;
                                df.right = i;
                                i = this.mContentBottom;
                                cf.bottom = i;
                                of.bottom = i;
                                df.bottom = i;
                            }
                            if (adjust != 48) {
                                vf.left = this.mCurLeft;
                                vf.top = this.mCurTop;
                                vf.right = this.mCurRight;
                                vf.bottom = this.mCurBottom;
                            } else {
                                vf.set(cf);
                            }
                        }
                    }
                }
            } else if (attached != null) {
                setAttachedWindowFrames(win, fl, adjust, attached, true, pf, df, of, cf, vf);
            } else {
                i = this.mOverscanScreenLeft;
                cf.left = i;
                of.left = i;
                df.left = i;
                pf.left = i;
                i = this.mOverscanScreenTop;
                cf.top = i;
                of.top = i;
                df.top = i;
                pf.top = i;
                i = this.mOverscanScreenLeft + this.mOverscanScreenWidth;
                cf.right = i;
                of.right = i;
                df.right = i;
                pf.right = i;
                i = this.mOverscanScreenTop + this.mOverscanScreenHeight;
                cf.bottom = i;
                of.bottom = i;
                df.bottom = i;
                pf.bottom = i;
            }
            if (!((fl & 512) == 0 || attrs.type == 2010 || win.isInMultiWindowMode())) {
                df.top = -10000;
                df.left = -10000;
                df.bottom = 10000;
                df.right = 10000;
                if (attrs.type != 2013) {
                    vf.top = -10000;
                    vf.left = -10000;
                    cf.top = -10000;
                    cf.left = -10000;
                    of.top = -10000;
                    of.left = -10000;
                    vf.bottom = 10000;
                    vf.right = 10000;
                    cf.bottom = 10000;
                    cf.right = 10000;
                    of.bottom = 10000;
                    of.right = 10000;
                }
            }
            adjustOppoWindowFrame(pf, df, of, cf, dcf, attrs);
            boolean useOutsets = shouldUseOutsets(attrs, fl);
            if (!isDefaultDisplay) {
                osf = null;
            } else if (useOutsets) {
                osf = mTmpOutsetFrame;
                osf.set(cf.left, cf.top, cf.right, cf.bottom);
                int outset = ScreenShapeHelper.getWindowOutsetBottomPx(this.mContext.getResources());
                if (outset > 0) {
                    int rotation = this.mDisplayRotation;
                    if (rotation == 0) {
                        osf.bottom += outset;
                    } else if (rotation == 1) {
                        osf.right += outset;
                    } else if (rotation == 2) {
                        osf.top -= outset;
                    } else if (rotation == 3) {
                        osf.left -= outset;
                    }
                    if (DEBUG_LAYOUT) {
                        Slog.v(TAG, "applying bottom outset of " + outset + " with rotation " + rotation + ", result: " + osf);
                    }
                }
            } else {
                osf = null;
            }
            if (DEBUG_LAYOUT) {
                String str2 = TAG;
                StringBuilder append = new StringBuilder().append("Compute frame ").append(attrs.getTitle()).append(": sim=#").append(Integer.toHexString(sim)).append(" attach=").append(attached).append(" type=").append(attrs.type);
                Object[] objArr2 = new Object[1];
                objArr2[0] = Integer.valueOf(fl);
                StringBuilder append2 = append.append(String.format(" flags=0x%08x", objArr2)).append(" pf=").append(pf.toShortString()).append(" df=").append(df.toShortString()).append(" of=").append(of.toShortString()).append(" cf=").append(cf.toShortString()).append(" vf=").append(vf.toShortString()).append(" dcf=").append(dcf.toShortString()).append(" sf=").append(sf.toShortString()).append(" osf=");
                if (osf == null) {
                    str = "null";
                } else {
                    str = osf.toShortString();
                }
                Slog.v(str2, append2.append(str).toString());
            }
            win.computeFrameLw(pf, df, of, cf, vf, dcf, sf, osf);
            if (attrs.type == 2011 && win.isVisibleOrBehindKeyguardLw() && win.isDisplayedLw() && !win.getGivenInsetsPendingLw()) {
                setLastInputMethodWindowLw(null, null);
                offsetInputMethodWindowLw(win);
            }
            if (attrs.type == 2031 && win.isVisibleOrBehindKeyguardLw() && !win.getGivenInsetsPendingLw()) {
                offsetVoiceInputWindowLw(win);
            }
        }
    }

    private void layoutWallpaper(WindowState win, Rect pf, Rect df, Rect of, Rect cf) {
        int i = this.mOverscanScreenLeft;
        df.left = i;
        pf.left = i;
        i = this.mOverscanScreenTop;
        df.top = i;
        pf.top = i;
        i = this.mOverscanScreenLeft + this.mOverscanScreenWidth;
        df.right = i;
        pf.right = i;
        i = this.mOverscanScreenTop + this.mOverscanScreenHeight;
        df.bottom = i;
        pf.bottom = i;
        i = this.mUnrestrictedScreenLeft;
        cf.left = i;
        of.left = i;
        i = this.mUnrestrictedScreenTop;
        cf.top = i;
        of.top = i;
        i = this.mUnrestrictedScreenLeft + this.mUnrestrictedScreenWidth;
        cf.right = i;
        of.right = i;
        i = this.mUnrestrictedScreenTop + this.mUnrestrictedScreenHeight;
        cf.bottom = i;
        of.bottom = i;
    }

    private void offsetInputMethodWindowLw(WindowState win) {
        int top = Math.max(win.getDisplayFrameLw().top, win.getContentFrameLw().top) + win.getGivenContentInsetsLw().top;
        if (this.mContentBottom > top) {
            this.mContentBottom = top;
        }
        if (this.mVoiceContentBottom > top) {
            this.mVoiceContentBottom = top;
        }
        top = win.getVisibleFrameLw().top + win.getGivenVisibleInsetsLw().top;
        if (this.mCurBottom > top) {
            this.mCurBottom = top;
        }
        if (DEBUG_LAYOUT) {
            Slog.v(TAG, "Input method: mDockBottom=" + this.mDockBottom + " mContentBottom=" + this.mContentBottom + " mCurBottom=" + this.mCurBottom);
        }
    }

    private void offsetVoiceInputWindowLw(WindowState win) {
        int top = Math.max(win.getDisplayFrameLw().top, win.getContentFrameLw().top) + win.getGivenContentInsetsLw().top;
        if (this.mVoiceContentBottom > top) {
            this.mVoiceContentBottom = top;
        }
    }

    public void finishLayoutLw() {
    }

    public void beginPostLayoutPolicyLw(int displayWidth, int displayHeight) {
        boolean z = false;
        this.mTopFullscreenOpaqueWindowState = null;
        this.mTopFullscreenOpaqueOrDimmingWindowState = null;
        this.mTopDockedOpaqueWindowState = null;
        this.mTopDockedWindowState = null;
        this.mTopDockedOpaqueOrDimmingWindowState = null;
        this.mAppsToBeHidden.clear();
        this.mAppsThatDismissKeyguard.clear();
        this.mForceStatusBar = false;
        this.mForceStatusBarFromKeyguard = false;
        this.mForceStatusBarTransparent = false;
        this.mForcingShowNavBar = false;
        this.mForcingShowNavBarLayer = -1;
        this.mHideLockScreen = false;
        this.mAllowLockscreenWhenOn = false;
        this.mDismissKeyguard = 0;
        this.mShowingLockscreen = false;
        this.mShowingDream = false;
        this.mWinShowWhenLocked = null;
        this.mKeyguardSecure = isKeyguardSecure(this.mCurrentUserId);
        if (this.mKeyguardSecure && this.mKeyguardDelegate != null) {
            z = this.mKeyguardDelegate.isShowing();
        }
        this.mKeyguardSecureIncludingHidden = z;
        if (this.mDreamManagerInternal == null) {
            this.mDreamManagerInternal = (DreamManagerInternal) LocalServices.getService(DreamManagerInternal.class);
        }
    }

    public void applyPostLayoutPolicyLw(WindowState win, LayoutParams attrs, WindowState attached) {
        if (DEBUG_LAYOUT) {
            Slog.i(TAG, "applyPostLayoutPolicyLw Win " + win + ": isVisibleOrBehindKeyguardLw=" + win.isVisibleOrBehindKeyguardLw() + ", win.isVisibleLw()=" + win.isVisibleLw() + ", win.hasDrawnLw()=" + win.hasDrawnLw() + ", win.isDrawnLw()=" + win.isDrawnLw() + ", attrs.type=" + attrs.type + ", attrs.privateFlags=#" + Integer.toHexString(attrs.privateFlags) + ", fl=#" + Integer.toHexString(PolicyControl.getWindowFlags(win, attrs)) + ", stackId=" + win.getStackId() + ", mTopFullscreenOpaqueWindowState=" + this.mTopFullscreenOpaqueWindowState + ", win.isVisibleOrBehindKeyguardLw()=" + win.isVisibleOrBehindKeyguardLw() + ", win.isGoneForLayoutLw()=" + win.isGoneForLayoutLw() + ", attached=" + attached + ", isFullscreen=" + isFullscreen(attrs) + ", normallyFullscreenWindows=" + StackId.normallyFullscreenWindows(win.getStackId()) + ", mDreamingLockscreen=" + this.mDreamingLockscreen + ", mShowingDream=" + this.mShowingDream);
        }
        int fl = PolicyControl.getWindowFlags(win, attrs);
        if (this.mTopFullscreenOpaqueWindowState == null && win.isVisibleLw() && attrs.type == 2011) {
            this.mForcingShowNavBar = true;
            this.mForcingShowNavBarLayer = win.getSurfaceLayer();
        }
        if (attrs.type == 2004 && (attrs.privateFlags & 1024) != 0) {
            this.mForceStatusBarFromKeyguard = true;
            this.mShowingLockscreen = true;
        }
        if (attrs.type == 2000 && (attrs.privateFlags & 4096) != 0) {
            this.mForceStatusBarTransparent = true;
        }
        boolean appWindow = attrs.type >= 1 ? attrs.type < 2000 : false;
        boolean startingwindow = attrs.getTitle().toString().contains("Starting ");
        boolean showWhenLocked = (startingwindow || (DumpState.DUMP_FROZEN & fl) == 0) ? false : true;
        boolean dismissKeyguard = (startingwindow || (4194304 & fl) == 0) ? false : true;
        int stackId = win.getStackId();
        if (this.mTopFullscreenOpaqueWindowState == null && win.isVisibleOrBehindKeyguardLw() && !win.isGoneForLayoutLw()) {
            if ((fl & 2048) != 0) {
                if ((attrs.privateFlags & 1024) != 0) {
                    this.mForceStatusBarFromKeyguard = true;
                } else {
                    this.mForceStatusBar = true;
                }
            }
            if (isApkLockScreen(win)) {
                appWindow = false;
            }
            if (attrs.type == 2023 && (!this.mDreamingLockscreen || (win.isVisibleLw() && win.hasDrawnLw()))) {
                this.mShowingDream = true;
                appWindow = true;
            }
            IApplicationToken appToken = win.getAppToken();
            if (appWindow && attached == null) {
                if (showWhenLocked) {
                    this.mAppsToBeHidden.remove(appToken);
                    this.mAppsThatDismissKeyguard.remove(appToken);
                    if (this.mAppsToBeHidden.isEmpty()) {
                        if (dismissKeyguard && !this.mKeyguardSecure) {
                            this.mAppsThatDismissKeyguard.add(appToken);
                        } else if (win.isDrawnLw() || win.hasAppShownWindows()) {
                            if (DEBUG_KEYGUARD) {
                                Slog.v(TAG, "ShowWhenLocked: " + win);
                            }
                            this.mWinShowWhenLocked = win;
                            this.mHideLockScreen = true;
                            this.mForceStatusBarFromKeyguard = false;
                        }
                    }
                } else if (dismissKeyguard) {
                    if (this.mKeyguardSecure) {
                        this.mAppsToBeHidden.add(appToken);
                    } else {
                        this.mAppsToBeHidden.remove(appToken);
                    }
                    this.mAppsThatDismissKeyguard.add(appToken);
                } else {
                    this.mAppsToBeHidden.add(appToken);
                }
                if (isFullscreen(attrs) && StackId.normallyFullscreenWindows(stackId)) {
                    if (DEBUG_LAYOUT) {
                        Slog.v(TAG, "Fullscreen window: " + win);
                    }
                    this.mTopFullscreenOpaqueWindowState = win;
                    if (this.mTopFullscreenOpaqueOrDimmingWindowState == null) {
                        this.mTopFullscreenOpaqueOrDimmingWindowState = win;
                    }
                    if (!this.mAppsThatDismissKeyguard.isEmpty() && this.mDismissKeyguard == 0) {
                        if (DEBUG_LAYOUT) {
                            Slog.v(TAG, "Setting mDismissKeyguard true by win " + win);
                        }
                        int i = (this.mWinDismissingKeyguard == win && this.mSecureDismissingKeyguard == this.mKeyguardSecure) ? 2 : 1;
                        this.mDismissKeyguard = i;
                        this.mWinDismissingKeyguard = win;
                        this.mSecureDismissingKeyguard = this.mKeyguardSecure;
                        this.mForceStatusBarFromKeyguard = this.mShowingLockscreen ? this.mKeyguardSecure : false;
                    } else if (this.mAppsToBeHidden.isEmpty() && showWhenLocked && (win.isDrawnLw() || win.hasAppShownWindows())) {
                        if (DEBUG_LAYOUT) {
                            Slog.v(TAG, "Setting mHideLockScreen to true by win " + win);
                        }
                        this.mHideLockScreen = true;
                        this.mForceStatusBarFromKeyguard = false;
                    }
                    if ((fl & 1) != 0) {
                        this.mAllowLockscreenWhenOn = true;
                    }
                }
                if (!(this.mKeyguardHidden || this.mWinShowWhenLocked == null || this.mWinShowWhenLocked.getAppToken() == win.getAppToken() || (attrs.flags & DumpState.DUMP_FROZEN) != 0 || !isStatusBarKeyguard())) {
                    win.hideLw(false);
                }
            }
        } else if (this.mTopFullscreenOpaqueWindowState == null && this.mWinShowWhenLocked == null && win.isAnimatingLw() && appWindow && showWhenLocked && this.mKeyguardHidden) {
            if (DEBUG_KEYGUARD) {
                Slog.v(TAG, "ShowWhenLocked no top: " + win);
            }
            this.mWinShowWhenLocked = win;
        }
        boolean reallyVisible = win.isVisibleOrBehindKeyguardLw() && !win.isGoneForLayoutLw();
        if (reallyVisible && win.getAttrs().type == 2031) {
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
            if (this.mTopDockedWindowState == null) {
                this.mTopDockedWindowState = win;
            }
        }
        if (this.mTopFullscreenOpaqueOrDimmingWindowState == null && reallyVisible && win.isDimming() && StackId.normallyFullscreenWindows(stackId)) {
            this.mTopFullscreenOpaqueOrDimmingWindowState = win;
        }
        if (this.mTopDockedOpaqueWindowState == null && reallyVisible && appWindow && attached == null && isFullscreen(attrs) && stackId == 3) {
            this.mTopDockedOpaqueWindowState = win;
            if (this.mTopDockedOpaqueOrDimmingWindowState == null) {
                this.mTopDockedOpaqueOrDimmingWindowState = win;
            }
        }
        if (this.mTopDockedWindowState == null && reallyVisible && appWindow && attached == null && stackId == 3) {
            this.mTopDockedWindowState = win;
        }
        if (this.mTopDockedOpaqueOrDimmingWindowState == null && reallyVisible && win.isDimming() && stackId == 3) {
            this.mTopDockedOpaqueOrDimmingWindowState = win;
        }
    }

    private boolean isFullscreen(LayoutParams attrs) {
        if (attrs.x == 0 && attrs.y == 0 && attrs.width == -1 && attrs.height == -1) {
            return true;
        }
        return false;
    }

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "YuHao@Plf.DesktopApp.Keyguard add for ColorOS Keyguard", property = OppoRomType.OPPO)
    public int finishPostLayoutPolicyLwKeyguard(int changes) {
        return changes;
    }

    public boolean isApkLockScreen(WindowState win) {
        return false;
    }

    public int finishPostLayoutPolicyLw() {
        LayoutParams lp;
        if (!(this.mWinShowWhenLocked == null || this.mTopFullscreenOpaqueWindowState == null || this.mWinShowWhenLocked.getAppToken() == this.mTopFullscreenOpaqueWindowState.getAppToken() || !isKeyguardLocked())) {
            LayoutParams attrs = this.mWinShowWhenLocked.getAttrs();
            attrs.flags |= DumpState.DUMP_DEXOPT;
            if (this.mTopFullscreenOpaqueWindowState != null) {
                this.mTopFullscreenOpaqueWindowState.hideLw(false);
            }
            this.mTopFullscreenOpaqueWindowState = this.mWinShowWhenLocked;
        }
        int changes = 0;
        boolean topIsFullscreen = false;
        if (this.mTopFullscreenOpaqueWindowState != null) {
            lp = this.mTopFullscreenOpaqueWindowState.getAttrs();
        } else {
            lp = null;
        }
        if (!this.mShowingDream) {
            this.mDreamingLockscreen = this.mShowingLockscreen;
            if (this.mDreamingSleepTokenNeeded) {
                this.mDreamingSleepTokenNeeded = false;
                this.mHandler.obtainMessage(15, 0, 1).sendToTarget();
            }
        } else if (!this.mDreamingSleepTokenNeeded) {
            this.mDreamingSleepTokenNeeded = true;
            this.mHandler.obtainMessage(15, 1, 1).sendToTarget();
        }
        if (this.mStatusBar != null) {
            boolean shouldBeTransparent;
            if (DEBUG_LAYOUT) {
                Slog.i(TAG, "force=" + this.mForceStatusBar + " forcefkg=" + this.mForceStatusBarFromKeyguard + " top=" + this.mTopFullscreenOpaqueWindowState + " dream=" + (this.mDreamManagerInternal != null ? Boolean.valueOf(this.mDreamManagerInternal.isDreaming()) : "null"));
            }
            if (!this.mForceStatusBarTransparent || this.mForceStatusBar) {
                shouldBeTransparent = false;
            } else {
                shouldBeTransparent = !this.mForceStatusBarFromKeyguard;
            }
            if (!shouldBeTransparent) {
                this.mStatusBarController.setShowTransparent(false);
            } else if (!this.mStatusBar.isVisibleLw()) {
                this.mStatusBarController.setShowTransparent(true);
            }
            LayoutParams statusBarAttrs = this.mStatusBar.getAttrs();
            boolean statusBarExpanded = statusBarAttrs.height == -1 ? statusBarAttrs.width == -1 : false;
            if (this.mDreamManagerInternal != null && this.mDreamManagerInternal.isDreaming()) {
                if (DEBUG_LAYOUT) {
                    Slog.v(TAG, "** HIDING status bar: dreaming");
                }
                if (this.mStatusBarController.setBarShowingLw(false)) {
                    changes = 1;
                }
            } else if (this.mForceStatusBar || ((this.mForceStatusBarFromKeyguard && !this.mHideStatusBarWhenLockScreen) || this.mForceStatusBarTransparent || statusBarExpanded)) {
                if (DEBUG_LAYOUT) {
                    Slog.v(TAG, "Showing status bar: forced");
                }
                if (this.mStatusBarController.setBarShowingLw(true)) {
                    changes = 1;
                }
                topIsFullscreen = this.mTopIsFullscreen ? this.mStatusBar.isAnimatingLw() : false;
                if (this.mForceStatusBarFromKeyguard && this.mStatusBarController.isTransientShowing() && !this.mHideStatusBarWhenLockScreen) {
                    this.mStatusBarController.updateVisibilityLw(false, this.mLastSystemUiFlags, this.mLastSystemUiFlags);
                }
                if (statusBarExpanded && this.mNavigationBar != null && this.mNavigationBarController.setBarShowingLw(true)) {
                    changes |= 1;
                }
            } else if (this.mTopFullscreenOpaqueWindowState != null) {
                int fl = PolicyControl.getWindowFlags(null, lp);
                if (localLOGV) {
                    Slog.d(TAG, "frame: " + this.mTopFullscreenOpaqueWindowState.getFrameLw() + " shown position: " + this.mTopFullscreenOpaqueWindowState.getShownPositionLw());
                    Slog.d(TAG, "attr: " + this.mTopFullscreenOpaqueWindowState.getAttrs() + " lp.flags=0x" + Integer.toHexString(fl));
                }
                topIsFullscreen = ((fl & 1024) != 0 || this.mHideStatusBarWhenLockScreen) ? true : (this.mLastSystemUiFlags & 4) != 0;
                if (this.mStatusBarController.isTransientShowing()) {
                    if (this.mStatusBarController.setBarShowingLw(true)) {
                        changes = 1;
                    }
                } else if (!topIsFullscreen || this.mWindowManagerInternal.isStackVisible(2) || this.mWindowManagerInternal.isStackVisible(3)) {
                    if (DEBUG_LAYOUT) {
                        Slog.v(TAG, "** SHOWING status bar: top is not fullscreen");
                    }
                    if (this.mStatusBarController.setBarShowingLw(true)) {
                        changes = 1;
                    }
                } else {
                    if (DEBUG_LAYOUT) {
                        Slog.v(TAG, "** HIDING status bar");
                    }
                    if (this.mStatusBarController.setBarShowingLw(false)) {
                        changes = 1;
                    } else if (DEBUG_LAYOUT) {
                        Slog.v(TAG, "Status bar already hiding");
                    }
                }
            }
        }
        if (this.mTopIsFullscreen != topIsFullscreen) {
            if (!topIsFullscreen) {
                changes |= 1;
            }
            try {
                IStatusBarService statusbar = getStatusBarService();
                Slog.i(TAG, "finishPostLayoutPolicyLw (statusbar != null ) " + (statusbar != null) + " topIsFullscreen " + topIsFullscreen + " top = " + this.mTopFullscreenOpaqueWindowState);
                if (statusbar != null) {
                    statusbar.topIsFullscreen(topIsFullscreen);
                }
            } catch (RemoteException e) {
                Slog.e(TAG, "RemoteException when  notify topIsFullscreen", e);
                this.mStatusBarService = null;
            }
            this.mTopIsFullscreen = topIsFullscreen;
        }
        changes |= finishPostLayoutPolicyLwKeyguard(changes);
        if (!(this.mKeyguardDelegate == null || this.mKeyguard == null)) {
            if (localLOGV) {
                Slog.v(TAG, "finishPostLayoutPolicyLw: mHideKeyguard=" + this.mHideLockScreen + " mDismissKeyguard=" + this.mDismissKeyguard + " mKeyguardDelegate.isSecure()= " + this.mKeyguardDelegate.isSecure(this.mCurrentUserId));
            }
            if (this.mDismissKeyguard != 0 && !this.mKeyguardSecure) {
                this.mKeyguardHidden = true;
                if (this.mKeyguard.hideLw(false)) {
                    changes |= 7;
                }
                setKeyguardOccludedLw(true);
                if (this.mKeyguardDelegate.isShowing()) {
                    this.mHandler.post(new Runnable() {
                        public void run() {
                            PhoneWindowManager.this.mKeyguardDelegate.keyguardDone(false, false);
                        }
                    });
                }
            } else if (this.mHideLockScreen) {
                this.mKeyguardHidden = true;
                this.mWinDismissingKeyguard = null;
                if (this.mKeyguard.hideLw(false)) {
                    changes |= 7;
                }
                setKeyguardOccludedLw(true);
            } else if (this.mDismissKeyguard != 0) {
                this.mKeyguardHidden = false;
                boolean dismissKeyguard = false;
                boolean trusted = this.mKeyguardDelegate.isTrusted();
                if (this.mDismissKeyguard == 1) {
                    boolean willDismiss;
                    if (trusted && this.mKeyguardOccluded && this.mKeyguardDelegate != null) {
                        willDismiss = this.mKeyguardDelegate.isShowing();
                    } else {
                        willDismiss = false;
                    }
                    if (willDismiss) {
                        this.mCurrentlyDismissingKeyguard = true;
                    }
                    dismissKeyguard = true;
                }
                if (!this.mCurrentlyDismissingKeyguard) {
                    if (this.mKeyguard.showLw(false)) {
                        changes |= 7;
                    }
                    setKeyguardOccludedLw(false);
                }
                if (dismissKeyguard) {
                    this.mHandler.post(new -int_finishPostLayoutPolicyLw__LambdaImpl0(trusted));
                }
            } else {
                this.mWinDismissingKeyguard = null;
                this.mSecureDismissingKeyguard = false;
                this.mKeyguardHidden = false;
                if (this.mKeyguardDoShowLw) {
                    if (this.mKeyguard.showLw(false)) {
                        changes |= 7;
                    }
                    setKeyguardOccludedLw(false);
                }
            }
        }
        if (this.mTopFullscreenOpaqueWindowState != null) {
            this.mTopFullscreenOpaqueWindowNavBarColor = this.mTopFullscreenOpaqueWindowState.getAttrs().navigationBarColor;
            if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
                Log.d(TAG, "set top full window nav bar color " + Integer.toHexString(this.mTopFullscreenOpaqueWindowNavBarColor) + " @" + this.mTopFullscreenOpaqueWindowState);
            }
        }
        if ((updateSystemUiVisibilityLw() & SYSTEM_UI_CHANGING_LAYOUT) != 0) {
            changes |= 1;
        }
        updateLockScreenTimeout();
        return changes;
    }

    /* renamed from: -com_android_server_policy_PhoneWindowManager_lambda$1 */
    /* synthetic */ void m10-com_android_server_policy_PhoneWindowManager_lambda$1(boolean trusted) {
        this.mKeyguardDelegate.dismiss(trusted);
    }

    boolean setKeyguardOccludedLw(boolean isOccluded) {
        boolean wasOccluded = this.mKeyguardOccluded;
        boolean showing = this.mKeyguardDelegate.isShowing();
        if (wasOccluded && !isOccluded && showing) {
            this.mKeyguardOccluded = false;
            this.mKeyguardDelegate.setOccluded(false, true);
            return true;
        } else if (!wasOccluded && isOccluded && showing) {
            this.mKeyguardOccluded = true;
            this.mKeyguardDelegate.setOccluded(true, false);
            return true;
        } else {
            if (!(!wasOccluded || isOccluded || showing)) {
                this.mKeyguardOccluded = false;
                this.mKeyguardDelegate.setOccluded(false, true);
            }
            return false;
        }
    }

    private void onKeyguardShowingStateChanged(boolean showing) {
        if (!showing) {
            synchronized (this.mWindowManagerFuncs.getWindowManagerLock()) {
                this.mCurrentlyDismissingKeyguard = false;
            }
        }
    }

    private boolean isStatusBarKeyguard() {
        if (this.mKeyguard == null || (this.mKeyguard.getAttrs().privateFlags & 1024) == 0) {
            return false;
        }
        return true;
    }

    public boolean allowAppAnimationsLw() {
        if (isStatusBarKeyguard() || this.mShowingDream) {
            return false;
        }
        return true;
    }

    public int focusChangedLw(WindowState lastFocus, WindowState newFocus) {
        this.mFocusedWindow = newFocus;
        if ((updateSystemUiVisibilityLw() & SYSTEM_UI_CHANGING_LAYOUT) != 0) {
            return 1;
        }
        return 0;
    }

    public void notifyLidSwitchChanged(long whenNanos, boolean lidOpen) {
        Intent intent = new Intent("android.intent.action.LID_SWITCH");
        intent.putExtra("lidOpen", lidOpen);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        int newLidState = lidOpen ? 1 : 0;
        if (newLidState != this.mLidState) {
            this.mLidState = newLidState;
            applyLidSwitchState();
            updateRotation(true);
            if (lidOpen) {
                if (this.mLidControlsSleep) {
                    wakeUp(SystemClock.uptimeMillis(), this.mAllowTheaterModeWakeFromLidSwitch, "android.policy:LID");
                }
            } else if (!this.mLidControlsSleep) {
                this.mPowerManager.userActivity(SystemClock.uptimeMillis(), false);
            }
        }
    }

    public void notifyCameraLensCoverSwitchChanged(long whenNanos, boolean lensCovered) {
        boolean keyguardActive = false;
        int lensCoverState = lensCovered ? 1 : 0;
        if (this.mCameraLensCoverState != lensCoverState) {
            if (this.mCameraLensCoverState == 1 && lensCoverState == 0) {
                Intent intent;
                if (this.mKeyguardDelegate != null) {
                    keyguardActive = this.mKeyguardDelegate.isShowing();
                }
                if (keyguardActive) {
                    intent = new Intent("android.media.action.STILL_IMAGE_CAMERA_SECURE");
                } else {
                    intent = new Intent("android.media.action.STILL_IMAGE_CAMERA");
                }
                wakeUp(whenNanos / 1000000, this.mAllowTheaterModeWakeFromCameraLens, "android.policy:CAMERA_COVER");
                startActivityAsUser(intent, UserHandle.CURRENT_OR_SELF);
            }
            this.mCameraLensCoverState = lensCoverState;
        }
    }

    void setHdmiPlugged(boolean plugged) {
        if (this.mHdmiPlugged != plugged) {
            this.mHdmiPlugged = plugged;
            updateRotation(true, true);
            Intent intent = new Intent("android.intent.action.HDMI_PLUGGED");
            intent.addFlags(67108864);
            intent.putExtra("state", plugged);
            this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:43:0x00a3  */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x0045  */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x00a5  */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x0045  */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x00a3  */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x00a5  */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x0094 A:{SYNTHETIC, Splitter: B:34:0x0094} */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x00a3  */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x0045  */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x00a5  */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x0071 A:{SYNTHETIC, Splitter: B:27:0x0071} */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x0045  */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x00a3  */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x00a5  */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x009d A:{SYNTHETIC, Splitter: B:39:0x009d} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void initializeHdmiState() {
        IOException ex;
        boolean z;
        NumberFormatException ex2;
        Throwable th;
        boolean z2 = false;
        boolean plugged = false;
        if (new File("/sys/devices/virtual/switch/hdmi/state").exists()) {
            this.mHDMIObserver.startObserving("DEVPATH=/devices/virtual/switch/hdmi");
            String filename = "/sys/class/switch/hdmi/state";
            FileReader reader = null;
            try {
                FileReader reader2 = new FileReader("/sys/class/switch/hdmi/state");
                try {
                    char[] buf = new char[15];
                    int n = reader2.read(buf);
                    if (n > 1) {
                        plugged = Integer.parseInt(new String(buf, 0, n + -1)) != 0;
                    }
                    if (reader2 != null) {
                        try {
                            reader2.close();
                        } catch (IOException e) {
                        }
                    }
                } catch (IOException e2) {
                    ex = e2;
                    reader = reader2;
                    Slog.w(TAG, "Couldn't read hdmi state from /sys/class/switch/hdmi/state: " + ex);
                    if (reader != null) {
                    }
                    if (plugged) {
                    }
                    this.mHdmiPlugged = z;
                    if (!this.mHdmiPlugged) {
                    }
                    setHdmiPlugged(z2);
                } catch (NumberFormatException e3) {
                    ex2 = e3;
                    reader = reader2;
                    try {
                        Slog.w(TAG, "Couldn't read hdmi state from /sys/class/switch/hdmi/state: " + ex2);
                        if (reader != null) {
                        }
                        if (plugged) {
                        }
                        this.mHdmiPlugged = z;
                        if (this.mHdmiPlugged) {
                        }
                        setHdmiPlugged(z2);
                    } catch (Throwable th2) {
                        th = th2;
                        if (reader != null) {
                            try {
                                reader.close();
                            } catch (IOException e4) {
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    reader = reader2;
                    if (reader != null) {
                    }
                    throw th;
                }
            } catch (IOException e5) {
                ex = e5;
                Slog.w(TAG, "Couldn't read hdmi state from /sys/class/switch/hdmi/state: " + ex);
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e6) {
                    }
                }
                if (plugged) {
                }
                this.mHdmiPlugged = z;
                if (this.mHdmiPlugged) {
                }
                setHdmiPlugged(z2);
            } catch (NumberFormatException e7) {
                ex2 = e7;
                Slog.w(TAG, "Couldn't read hdmi state from /sys/class/switch/hdmi/state: " + ex2);
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e8) {
                    }
                }
                if (plugged) {
                }
                this.mHdmiPlugged = z;
                if (this.mHdmiPlugged) {
                }
                setHdmiPlugged(z2);
            }
        }
        if (plugged) {
            z = false;
        } else {
            z = true;
        }
        this.mHdmiPlugged = z;
        if (this.mHdmiPlugged) {
            z2 = true;
        }
        setHdmiPlugged(z2);
    }

    /* JADX WARNING: Missing block: B:12:0x004a, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void takeScreenshot(final int screenshotType) {
        synchronized (this.mScreenshotLock) {
            if (this.mScreenshotConnection != null) {
                return;
            }
            ComponentName serviceComponent = new ComponentName(SYSUI_PACKAGE, SYSUI_SCREENSHOT_SERVICE);
            Intent serviceIntent = new Intent();
            serviceIntent.setComponent(serviceComponent);
            ServiceConnection conn = new ServiceConnection() {
                public void onServiceConnected(ComponentName name, IBinder service) {
                    synchronized (PhoneWindowManager.this.mScreenshotLock) {
                        if (PhoneWindowManager.this.mScreenshotConnection != this) {
                            return;
                        }
                        Messenger messenger = new Messenger(service);
                        Message msg = Message.obtain(null, screenshotType);
                        msg.replyTo = new Messenger(new Handler(PhoneWindowManager.this.mHandler.getLooper()) {
                            public void handleMessage(Message msg) {
                                synchronized (PhoneWindowManager.this.mScreenshotLock) {
                                    if (PhoneWindowManager.this.mScreenshotConnection == this) {
                                        PhoneWindowManager.this.mContext.unbindService(PhoneWindowManager.this.mScreenshotConnection);
                                        PhoneWindowManager.this.mScreenshotConnection = null;
                                        PhoneWindowManager.this.mHandler.removeCallbacks(PhoneWindowManager.this.mScreenshotTimeout);
                                    }
                                }
                            }
                        });
                        msg.arg2 = 0;
                        msg.arg1 = 0;
                        if (PhoneWindowManager.this.mStatusBar != null && PhoneWindowManager.this.mStatusBar.isVisibleLw()) {
                            msg.arg1 = 1;
                        }
                        if (PhoneWindowManager.this.mNavigationBar != null && PhoneWindowManager.this.mNavigationBar.isVisibleLw()) {
                            msg.arg2 = 1;
                        }
                        try {
                            messenger.send(msg);
                        } catch (RemoteException e) {
                        }
                    }
                }

                public void onServiceDisconnected(ComponentName name) {
                    synchronized (PhoneWindowManager.this.mScreenshotLock) {
                        if (PhoneWindowManager.this.mScreenshotConnection != null) {
                            PhoneWindowManager.this.mContext.unbindService(PhoneWindowManager.this.mScreenshotConnection);
                            PhoneWindowManager.this.mScreenshotConnection = null;
                            PhoneWindowManager.this.mHandler.removeCallbacks(PhoneWindowManager.this.mScreenshotTimeout);
                            PhoneWindowManager.this.notifyScreenshotError();
                        }
                    }
                }
            };
            if (this.mContext.bindServiceAsUser(serviceIntent, conn, 33554433, UserHandle.CURRENT)) {
                this.mScreenshotConnection = conn;
                this.mHandler.postDelayed(this.mScreenshotTimeout, 10000);
                this.mHandler.removeCallbacks(this.mProcessCpuTrackerRunnable);
                this.mHandler.postDelayed(this.mProcessCpuTrackerRunnable, 2000);
            }
        }
    }

    public void shutdown() {
        this.mShuttingDown = true;
    }

    private void notifyScreenshotError() {
        ComponentName errorComponent = new ComponentName(SYSUI_PACKAGE, SYSUI_SCREENSHOT_ERROR_RECEIVER);
        Intent errorIntent = new Intent("android.intent.action.USER_PRESENT");
        errorIntent.setComponent(errorComponent);
        errorIntent.addFlags(335544320);
        this.mContext.sendBroadcastAsUser(errorIntent, UserHandle.CURRENT);
    }

    /* JADX WARNING: Missing block: B:25:0x0049, code:
            if ((536870912 & r28) == 0) goto L_0x00c8;
     */
    /* JADX WARNING: Missing block: B:26:0x004b, code:
            r12 = true;
     */
    /* JADX WARNING: Missing block: B:28:0x0050, code:
            if (r27.getAction() != 0) goto L_0x00ca;
     */
    /* JADX WARNING: Missing block: B:29:0x0052, code:
            r8 = true;
     */
    /* JADX WARNING: Missing block: B:30:0x0053, code:
            r7 = r27.isCanceled();
            r15 = r27.getKeyCode();
     */
    /* JADX WARNING: Missing block: B:31:0x005f, code:
            if ((16777216 & r28) == 0) goto L_0x00cc;
     */
    /* JADX WARNING: Missing block: B:32:0x0061, code:
            r13 = true;
     */
    /* JADX WARNING: Missing block: B:34:0x0068, code:
            if (r26.mKeyguardDelegate != null) goto L_0x00ce;
     */
    /* JADX WARNING: Missing block: B:35:0x006a, code:
            r16 = false;
     */
    /* JADX WARNING: Missing block: B:37:0x0070, code:
            if (r15 != 187) goto L_0x0074;
     */
    /* JADX WARNING: Missing block: B:38:0x0072, code:
            if (r8 == false) goto L_0x00e0;
     */
    /* JADX WARNING: Missing block: B:40:0x0076, code:
            if ((r28 & 1) != 0) goto L_0x00f0;
     */
    /* JADX WARNING: Missing block: B:41:0x0078, code:
            r14 = r27.isWakeKey();
     */
    /* JADX WARNING: Missing block: B:42:0x007c, code:
            if (r12 != false) goto L_0x00f2;
     */
    /* JADX WARNING: Missing block: B:43:0x007e, code:
            if (r13 == false) goto L_0x0082;
     */
    /* JADX WARNING: Missing block: B:44:0x0080, code:
            if (r14 == false) goto L_0x00f2;
     */
    /* JADX WARNING: Missing block: B:45:0x0082, code:
            if (r12 != false) goto L_0x010f;
     */
    /* JADX WARNING: Missing block: B:47:0x0088, code:
            if (shouldDispatchInputWhenNonInteractive(r27) == false) goto L_0x010f;
     */
    /* JADX WARNING: Missing block: B:48:0x008a, code:
            r18 = 1;
            r26.mPendingWakeKey = -1;
     */
    /* JADX WARNING: Missing block: B:50:0x0098, code:
            if (isValidGlobalKey(r15) == false) goto L_0x0129;
     */
    /* JADX WARNING: Missing block: B:52:0x00a8, code:
            if (r26.mGlobalKeyManager.shouldHandleGlobalKey(r15, r27) == false) goto L_0x0129;
     */
    /* JADX WARNING: Missing block: B:53:0x00aa, code:
            if (r14 == false) goto L_0x00c4;
     */
    /* JADX WARNING: Missing block: B:54:0x00ac, code:
            wakeUp(r27.getEventTime(), r26.mAllowTheaterModeWakeFromKey, "android.policy:KEY");
     */
    /* JADX WARNING: Missing block: B:55:0x00c4, code:
            return r18;
     */
    /* JADX WARNING: Missing block: B:59:0x00c8, code:
            r12 = false;
     */
    /* JADX WARNING: Missing block: B:60:0x00ca, code:
            r8 = false;
     */
    /* JADX WARNING: Missing block: B:61:0x00cc, code:
            r13 = false;
     */
    /* JADX WARNING: Missing block: B:62:0x00ce, code:
            if (r12 == false) goto L_0x00d5;
     */
    /* JADX WARNING: Missing block: B:63:0x00d0, code:
            r16 = isKeyguardShowingAndNotOccluded();
     */
    /* JADX WARNING: Missing block: B:64:0x00d5, code:
            r16 = r26.mKeyguardDelegate.isShowing();
     */
    /* JADX WARNING: Missing block: B:65:0x00e0, code:
            r26.mHandler.removeCallbacks(r26.mRecentsStartSplitSreen);
     */
    /* JADX WARNING: Missing block: B:66:0x00f0, code:
            r14 = true;
     */
    /* JADX WARNING: Missing block: B:67:0x00f2, code:
            r18 = 1;
            r14 = false;
     */
    /* JADX WARNING: Missing block: B:68:0x00f5, code:
            if (r12 == false) goto L_0x0094;
     */
    /* JADX WARNING: Missing block: B:70:0x00ff, code:
            if (r15 != r26.mPendingWakeKey) goto L_0x0103;
     */
    /* JADX WARNING: Missing block: B:71:0x0101, code:
            if (r8 == false) goto L_0x010c;
     */
    /* JADX WARNING: Missing block: B:72:0x0103, code:
            r26.mPendingWakeKey = -1;
     */
    /* JADX WARNING: Missing block: B:73:0x010c, code:
            r18 = 0;
     */
    /* JADX WARNING: Missing block: B:74:0x010f, code:
            r18 = 0;
     */
    /* JADX WARNING: Missing block: B:75:0x0111, code:
            if (r14 == false) goto L_0x011d;
     */
    /* JADX WARNING: Missing block: B:76:0x0113, code:
            if (r8 == false) goto L_0x0127;
     */
    /* JADX WARNING: Missing block: B:78:0x011b, code:
            if (isWakeKeyWhenScreenOff(r15) == false) goto L_0x0127;
     */
    /* JADX WARNING: Missing block: B:79:0x011d, code:
            if (r14 == false) goto L_0x0094;
     */
    /* JADX WARNING: Missing block: B:80:0x011f, code:
            if (r8 == false) goto L_0x0094;
     */
    /* JADX WARNING: Missing block: B:81:0x0121, code:
            r26.mPendingWakeKey = r15;
     */
    /* JADX WARNING: Missing block: B:82:0x0127, code:
            r14 = false;
     */
    /* JADX WARNING: Missing block: B:83:0x0129, code:
            if (r8 == false) goto L_0x021a;
     */
    /* JADX WARNING: Missing block: B:85:0x012d, code:
            if ((r28 & 2) == 0) goto L_0x021a;
     */
    /* JADX WARNING: Missing block: B:87:0x0133, code:
            if (r27.getRepeatCount() != 0) goto L_0x0216;
     */
    /* JADX WARNING: Missing block: B:88:0x0135, code:
            r20 = true;
     */
    /* JADX WARNING: Missing block: B:90:0x0139, code:
            if (IS_USER_BUILD == false) goto L_0x013f;
     */
    /* JADX WARNING: Missing block: B:92:0x013d, code:
            if (com.oppo.debug.InputLog.DEBUG == false) goto L_0x01fa;
     */
    /* JADX WARNING: Missing block: B:93:0x013f, code:
            android.util.Log.d(TAG, "interceptKeyTq keycode=" + r15 + " interactive=" + r12 + " keyguardActive=" + r16 + " policyFlags=" + java.lang.Integer.toHexString(r28) + " down =" + r8 + " canceled = " + r7 + " isWakeKey=" + r14 + " mVolumeDownKeyTriggered =" + r26.mScreenshotChordVolumeDownKeyTriggered + " mVolumeUpKeyTriggered =" + r26.mScreenshotChordVolumeUpKeyTriggered + " result = " + r18 + " useHapticFeedback = " + r20 + " isInjected = " + r13);
     */
    /* JADX WARNING: Missing block: B:94:0x01fa, code:
            switch(r15) {
                case 3: goto L_0x029e;
                case 4: goto L_0x0237;
                case 5: goto L_0x05cc;
                case 6: goto L_0x0493;
                case 24: goto L_0x0357;
                case 25: goto L_0x0357;
                case com.android.server.wm.WindowManagerService.H.DO_ANIMATION_CALLBACK :int: goto L_0x0524;
                case com.android.server.hdmi.HdmiCecKeycode.CEC_KEYCODE_RESERVED :int: goto L_0x0584;
                case com.android.server.hdmi.HdmiCecKeycode.CEC_KEYCODE_VIDEO_ON_DEMAND :int: goto L_0x021e;
                case com.android.server.hdmi.HdmiCecKeycode.CEC_KEYCODE_INITIAL_CONFIGURATION :int: goto L_0x0584;
                case com.android.server.hdmi.HdmiCecKeycode.CEC_KEYCODE_SELECT_BROADCAST_TYPE :int: goto L_0x0584;
                case com.android.server.hdmi.HdmiCecKeycode.CEC_KEYCODE_SELECT_SOUND_PRESENTATION :int: goto L_0x0584;
                case 88: goto L_0x0584;
                case 89: goto L_0x0584;
                case 90: goto L_0x0584;
                case 91: goto L_0x0584;
                case 126: goto L_0x0584;
                case 127: goto L_0x0584;
                case 130: goto L_0x0584;
                case 164: goto L_0x0357;
                case 171: goto L_0x0624;
                case 187: goto L_0x0643;
                case com.android.server.NetworkManagementService.NetdResponseCode.DnsProxyQueryResult :int: goto L_0x0584;
                case com.android.server.NetworkManagementService.NetdResponseCode.ClatdStatusResult :int: goto L_0x0542;
                case 224: goto L_0x057f;
                case 231: goto L_0x05ea;
                case 276: goto L_0x056f;
                case 280: goto L_0x053b;
                case 281: goto L_0x053b;
                case 282: goto L_0x053b;
                case 283: goto L_0x053b;
                case 803: goto L_0x029e;
                case 882: goto L_0x021e;
                case 987: goto L_0x0643;
                default: goto L_0x01fd;
            };
     */
    /* JADX WARNING: Missing block: B:95:0x01fd, code:
            if (r20 == false) goto L_0x0207;
     */
    /* JADX WARNING: Missing block: B:97:0x0205, code:
            if (r26.mShuttingDown == false) goto L_0x065d;
     */
    /* JADX WARNING: Missing block: B:98:0x0207, code:
            if (r14 == false) goto L_0x0215;
     */
    /* JADX WARNING: Missing block: B:100:0x0213, code:
            if (r26.mDisplayManagerInternal.isBlockScreenOnByFingerPrint() == false) goto L_0x0670;
     */
    /* JADX WARNING: Missing block: B:101:0x0215, code:
            return r18;
     */
    /* JADX WARNING: Missing block: B:102:0x0216, code:
            r20 = false;
     */
    /* JADX WARNING: Missing block: B:103:0x021a, code:
            r20 = false;
     */
    /* JADX WARNING: Missing block: B:104:0x021e, code:
            if (r8 == false) goto L_0x0228;
     */
    /* JADX WARNING: Missing block: B:106:0x0224, code:
            if (r27.getRepeatCount() != 0) goto L_0x0228;
     */
    /* JADX WARNING: Missing block: B:107:0x0226, code:
            r20 = true;
     */
    /* JADX WARNING: Missing block: B:109:0x0232, code:
            if (r26.mDisplayManagerInternal.isBlockScreenOnByFingerPrint() == false) goto L_0x01fd;
     */
    /* JADX WARNING: Missing block: B:110:0x0234, code:
            r20 = false;
     */
    /* JADX WARNING: Missing block: B:111:0x0237, code:
            if (r8 == false) goto L_0x0291;
     */
    /* JADX WARNING: Missing block: B:113:0x023d, code:
            if (r27.getRepeatCount() != 0) goto L_0x0241;
     */
    /* JADX WARNING: Missing block: B:114:0x023f, code:
            r20 = true;
     */
    /* JADX WARNING: Missing block: B:116:0x024b, code:
            if (r26.mDisplayManagerInternal.isBlockScreenOnByFingerPrint() == false) goto L_0x024f;
     */
    /* JADX WARNING: Missing block: B:117:0x024d, code:
            r20 = false;
     */
    /* JADX WARNING: Missing block: B:118:0x024f, code:
            r26.mBackKeyHandled = false;
     */
    /* JADX WARNING: Missing block: B:119:0x025b, code:
            if (hasLongPressOnBackBehavior() == false) goto L_0x01fd;
     */
    /* JADX WARNING: Missing block: B:120:0x025d, code:
            r17 = r26.mHandler.obtainMessage(18);
            r17.setAsynchronous(true);
            r26.mHandler.sendMessageDelayed(r17, android.view.ViewConfiguration.get(r26.mContext).getDeviceGlobalActionKeyTimeout());
     */
    /* JADX WARNING: Missing block: B:121:0x0291, code:
            r10 = r26.mBackKeyHandled;
            cancelPendingBackKeyAction();
     */
    /* JADX WARNING: Missing block: B:122:0x0298, code:
            if (r10 == false) goto L_0x01fd;
     */
    /* JADX WARNING: Missing block: B:123:0x029a, code:
            r18 = r18 & -2;
     */
    /* JADX WARNING: Missing block: B:124:0x029e, code:
            if (r8 == false) goto L_0x02d4;
     */
    /* JADX WARNING: Missing block: B:125:0x02a0, code:
            if (r12 != false) goto L_0x02b2;
     */
    /* JADX WARNING: Missing block: B:127:0x02a6, code:
            if (r27.getRepeatCount() != 0) goto L_0x02b2;
     */
    /* JADX WARNING: Missing block: B:128:0x02a8, code:
            r26.mLastHomeDownTimeDuringPF = r27.getDownTime();
     */
    /* JADX WARNING: Missing block: B:129:0x02b2, code:
            if (r12 == false) goto L_0x02c4;
     */
    /* JADX WARNING: Missing block: B:131:0x02b8, code:
            if (r27.getRepeatCount() != 0) goto L_0x02c4;
     */
    /* JADX WARNING: Missing block: B:133:0x02c0, code:
            if (r26.isTouchFingerPrintSensor == false) goto L_0x02c4;
     */
    /* JADX WARNING: Missing block: B:134:0x02c2, code:
            r20 = true;
     */
    /* JADX WARNING: Missing block: B:136:0x02c8, code:
            if (getFingerprintInternal() == null) goto L_0x02d1;
     */
    /* JADX WARNING: Missing block: B:137:0x02ca, code:
            getFingerprintInternal().onHomeKeyDown();
     */
    /* JADX WARNING: Missing block: B:138:0x02d1, code:
            r14 = false;
     */
    /* JADX WARNING: Missing block: B:140:0x02d8, code:
            if (getFingerprintInternal() == null) goto L_0x02e1;
     */
    /* JADX WARNING: Missing block: B:141:0x02da, code:
            getFingerprintInternal().onHomeKeyUp();
     */
    /* JADX WARNING: Missing block: B:143:0x02eb, code:
            if (r26.mLastHomeDownTimeDuringPF == 0) goto L_0x030f;
     */
    /* JADX WARNING: Missing block: B:145:0x02f9, code:
            if (r27.getDownTime() != r26.mLastHomeDownTimeDuringPF) goto L_0x030f;
     */
    /* JADX WARNING: Missing block: B:146:0x02fb, code:
            android.util.Slog.d(TAG, "Skip HOME_KEY Up when screen off due to finger print");
            r26.mLastHomeDownTimeDuringPF = 0;
     */
    /* JADX WARNING: Missing block: B:147:0x030e, code:
            return 0;
     */
    /* JADX WARNING: Missing block: B:149:0x0328, code:
            if (android.provider.Settings.System.getInt(r26.mContext.getContentResolver(), "drop_home_key_when_use_fingerprint", 0) != 1) goto L_0x034b;
     */
    /* JADX WARNING: Missing block: B:150:0x032a, code:
            r9 = true;
     */
    /* JADX WARNING: Missing block: B:151:0x032b, code:
            if (r9 == false) goto L_0x034d;
     */
    /* JADX WARNING: Missing block: B:152:0x032d, code:
            android.util.Slog.d(TAG, "drop HOME_KEY Up when screen on due to finger print");
            android.provider.Settings.System.putInt(r26.mContext.getContentResolver(), "drop_home_key_when_use_fingerprint", 0);
     */
    /* JADX WARNING: Missing block: B:153:0x034a, code:
            return 0;
     */
    /* JADX WARNING: Missing block: B:154:0x034b, code:
            r9 = false;
     */
    /* JADX WARNING: Missing block: B:155:0x034d, code:
            r26.mLastHomeDownTimeDuringPF = 0;
     */
    /* JADX WARNING: Missing block: B:157:0x035b, code:
            if (r15 != 25) goto L_0x03cf;
     */
    /* JADX WARNING: Missing block: B:158:0x035d, code:
            if (r8 == false) goto L_0x03c3;
     */
    /* JADX WARNING: Missing block: B:159:0x035f, code:
            if (r12 == false) goto L_0x0369;
     */
    /* JADX WARNING: Missing block: B:161:0x0367, code:
            if (r26.mScreenshotChordVolumeDownKeyTriggered == false) goto L_0x0396;
     */
    /* JADX WARNING: Missing block: B:162:0x0369, code:
            if (r8 == false) goto L_0x046c;
     */
    /* JADX WARNING: Missing block: B:163:0x036b, code:
            r19 = getTelecommService();
            r6 = (android.media.AudioManager) r26.mContext.getSystemService("audio");
     */
    /* JADX WARNING: Missing block: B:164:0x037e, code:
            if (r19 == null) goto L_0x046c;
     */
    /* JADX WARNING: Missing block: B:166:0x0384, code:
            if (r19.isRinging() == false) goto L_0x043b;
     */
    /* JADX WARNING: Missing block: B:167:0x0386, code:
            android.util.Log.i(TAG, "interceptKeyBeforeQueueing: VOLUME key-down while ringing: Silence ringer!");
            r19.silenceRinger();
            r18 = r18 & -2;
     */
    /* JADX WARNING: Missing block: B:169:0x03a0, code:
            if ((r27.getFlags() & 1024) != 0) goto L_0x0369;
     */
    /* JADX WARNING: Missing block: B:170:0x03a2, code:
            r26.mScreenshotChordVolumeDownKeyTriggered = true;
            r26.mScreenshotChordVolumeDownKeyTime = r27.getDownTime();
            r26.mScreenshotChordVolumeDownKeyConsumed = false;
            cancelPendingPowerKeyAction();
            interceptScreenshotChord();
     */
    /* JADX WARNING: Missing block: B:171:0x03c3, code:
            r26.mScreenshotChordVolumeDownKeyTriggered = false;
            cancelPendingScreenshotChordAction();
     */
    /* JADX WARNING: Missing block: B:173:0x03d3, code:
            if (r15 != 24) goto L_0x0369;
     */
    /* JADX WARNING: Missing block: B:175:0x03d7, code:
            if (IS_USER_BUILD != false) goto L_0x0400;
     */
    /* JADX WARNING: Missing block: B:177:0x03e7, code:
            if (android.os.SystemProperties.get("persist.sys.anr_sys_key").equals(com.android.server.LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON) == false) goto L_0x0400;
     */
    /* JADX WARNING: Missing block: B:178:0x03e9, code:
            r26.mHandler.postDelayed(r26.mKeyRemappingVolumeDownLongPress_Test, 0);
     */
    /* JADX WARNING: Missing block: B:179:0x0400, code:
            if (r8 == false) goto L_0x042e;
     */
    /* JADX WARNING: Missing block: B:180:0x0402, code:
            if (r12 == false) goto L_0x0369;
     */
    /* JADX WARNING: Missing block: B:182:0x040a, code:
            if (r26.mScreenshotChordVolumeUpKeyTriggered != false) goto L_0x0369;
     */
    /* JADX WARNING: Missing block: B:184:0x0416, code:
            if ((r27.getFlags() & 1024) != 0) goto L_0x0369;
     */
    /* JADX WARNING: Missing block: B:185:0x0418, code:
            r26.mScreenshotChordVolumeUpKeyTriggered = true;
            cancelPendingPowerKeyAction();
            cancelPendingScreenshotChordAction();
            handleVolumeUpKeyDownEvent(r27);
            interceptLongshotChord();
     */
    /* JADX WARNING: Missing block: B:186:0x042e, code:
            r26.mScreenshotChordVolumeUpKeyTriggered = false;
            cancelPendingScreenshotChordAction();
     */
    /* JADX WARNING: Missing block: B:188:0x043f, code:
            if (r19.isInCall() != false) goto L_0x044d;
     */
    /* JADX WARNING: Missing block: B:190:0x044b, code:
            if (r6.getMode() != 3) goto L_0x046c;
     */
    /* JADX WARNING: Missing block: B:192:0x044f, code:
            if ((r18 & 1) == 0) goto L_0x0455;
     */
    /* JADX WARNING: Missing block: B:193:0x0451, code:
            if (r12 == false) goto L_0x046c;
     */
    /* JADX WARNING: Missing block: B:194:0x0453, code:
            if (r16 == false) goto L_0x046c;
     */
    /* JADX WARNING: Missing block: B:195:0x0455, code:
            android.media.session.MediaSessionLegacyHelper.getHelper(r26.mContext).sendVolumeKeyEvent(r27, false);
     */
    /* JADX WARNING: Missing block: B:197:0x0472, code:
            if (r26.mUseTvRouting == false) goto L_0x0478;
     */
    /* JADX WARNING: Missing block: B:198:0x0474, code:
            r18 = r18 | 1;
     */
    /* JADX WARNING: Missing block: B:200:0x047a, code:
            if ((r18 & 1) != 0) goto L_0x01fd;
     */
    /* JADX WARNING: Missing block: B:201:0x047c, code:
            android.media.session.MediaSessionLegacyHelper.getHelper(r26.mContext).sendVolumeKeyEvent(r27, true);
     */
    /* JADX WARNING: Missing block: B:202:0x0493, code:
            r18 = r18 & -2;
     */
    /* JADX WARNING: Missing block: B:203:0x0495, code:
            if (r8 == false) goto L_0x04dd;
     */
    /* JADX WARNING: Missing block: B:204:0x0497, code:
            r19 = getTelecommService();
            r11 = false;
     */
    /* JADX WARNING: Missing block: B:205:0x049c, code:
            if (r19 == null) goto L_0x04a2;
     */
    /* JADX WARNING: Missing block: B:206:0x049e, code:
            r11 = r19.endCall();
     */
    /* JADX WARNING: Missing block: B:207:0x04a2, code:
            if (r12 == false) goto L_0x04a6;
     */
    /* JADX WARNING: Missing block: B:208:0x04a4, code:
            if (r11 == false) goto L_0x04b0;
     */
    /* JADX WARNING: Missing block: B:209:0x04a6, code:
            r26.mEndCallKeyHandled = true;
     */
    /* JADX WARNING: Missing block: B:210:0x04b0, code:
            r26.mEndCallKeyHandled = false;
            r26.mHandler.postDelayed(r26.mEndCallLongPress, android.view.ViewConfiguration.get(r26.mContext).getDeviceGlobalActionKeyTimeout());
     */
    /* JADX WARNING: Missing block: B:212:0x04e3, code:
            if (r26.mEndCallKeyHandled != false) goto L_0x01fd;
     */
    /* JADX WARNING: Missing block: B:213:0x04e5, code:
            r26.mHandler.removeCallbacks(r26.mEndCallLongPress);
     */
    /* JADX WARNING: Missing block: B:214:0x04f4, code:
            if (r7 != false) goto L_0x01fd;
     */
    /* JADX WARNING: Missing block: B:216:0x04fe, code:
            if ((r26.mEndcallBehavior & 1) == 0) goto L_0x0506;
     */
    /* JADX WARNING: Missing block: B:218:0x0504, code:
            if (goHome() != false) goto L_0x01fd;
     */
    /* JADX WARNING: Missing block: B:220:0x050e, code:
            if ((r26.mEndcallBehavior & 2) == 0) goto L_0x01fd;
     */
    /* JADX WARNING: Missing block: B:221:0x0510, code:
            r26.mPowerManager.goToSleep(r27.getEventTime(), 4, 0);
            r14 = false;
     */
    /* JADX WARNING: Missing block: B:222:0x0524, code:
            r18 = r18 & -2;
            r14 = false;
     */
    /* JADX WARNING: Missing block: B:223:0x0527, code:
            if (r8 == false) goto L_0x0532;
     */
    /* JADX WARNING: Missing block: B:224:0x0529, code:
            interceptPowerKeyDown(r27, r12);
     */
    /* JADX WARNING: Missing block: B:225:0x0532, code:
            interceptPowerKeyUp(r27, r12, r7);
     */
    /* JADX WARNING: Missing block: B:226:0x053b, code:
            r18 = r18 & -2;
            interceptSystemNavigationKey(r27);
     */
    /* JADX WARNING: Missing block: B:227:0x0542, code:
            r18 = r18 & -2;
            r14 = false;
     */
    /* JADX WARNING: Missing block: B:228:0x054f, code:
            if (r26.mPowerManager.isInteractive() != false) goto L_0x0553;
     */
    /* JADX WARNING: Missing block: B:229:0x0551, code:
            r20 = false;
     */
    /* JADX WARNING: Missing block: B:230:0x0553, code:
            if (r8 == false) goto L_0x0562;
     */
    /* JADX WARNING: Missing block: B:231:0x0555, code:
            sleepPress(r27.getEventTime());
     */
    /* JADX WARNING: Missing block: B:232:0x0562, code:
            sleepRelease(r27.getEventTime());
     */
    /* JADX WARNING: Missing block: B:233:0x056f, code:
            r18 = r18 & -2;
            r14 = false;
     */
    /* JADX WARNING: Missing block: B:234:0x0572, code:
            if (r8 != false) goto L_0x01fd;
     */
    /* JADX WARNING: Missing block: B:235:0x0574, code:
            r26.mPowerManagerInternal.setUserInactiveOverrideFromWindowManager();
     */
    /* JADX WARNING: Missing block: B:236:0x057f, code:
            r18 = r18 & -2;
            r14 = true;
     */
    /* JADX WARNING: Missing block: B:238:0x0592, code:
            if (android.media.session.MediaSessionLegacyHelper.getHelper(r26.mContext).isGlobalPriorityActive() == false) goto L_0x0596;
     */
    /* JADX WARNING: Missing block: B:239:0x0594, code:
            r18 = r18 & -2;
     */
    /* JADX WARNING: Missing block: B:241:0x0598, code:
            if ((r18 & 1) != 0) goto L_0x01fd;
     */
    /* JADX WARNING: Missing block: B:242:0x059a, code:
            r26.mBroadcastWakeLock.acquire();
            r17 = r26.mHandler.obtainMessage(3, new android.view.KeyEvent(r27));
            r17.setAsynchronous(true);
            r17.sendToTarget();
     */
    /* JADX WARNING: Missing block: B:243:0x05cc, code:
            if (r8 == false) goto L_0x01fd;
     */
    /* JADX WARNING: Missing block: B:244:0x05ce, code:
            r19 = getTelecommService();
     */
    /* JADX WARNING: Missing block: B:245:0x05d2, code:
            if (r19 == null) goto L_0x01fd;
     */
    /* JADX WARNING: Missing block: B:247:0x05d8, code:
            if (r19.isRinging() == false) goto L_0x01fd;
     */
    /* JADX WARNING: Missing block: B:248:0x05da, code:
            android.util.Log.i(TAG, "interceptKeyBeforeQueueing: CALL key-down while ringing: Answer the call!");
            r19.acceptRingingCall();
            r18 = r18 & -2;
     */
    /* JADX WARNING: Missing block: B:250:0x05ec, code:
            if ((r18 & 1) != 0) goto L_0x01fd;
     */
    /* JADX WARNING: Missing block: B:251:0x05ee, code:
            if (r8 != false) goto L_0x01fd;
     */
    /* JADX WARNING: Missing block: B:252:0x05f0, code:
            r26.mBroadcastWakeLock.acquire();
            r22 = r26.mHandler;
     */
    /* JADX WARNING: Missing block: B:253:0x05ff, code:
            if (r16 == false) goto L_0x0621;
     */
    /* JADX WARNING: Missing block: B:254:0x0601, code:
            r21 = 1;
     */
    /* JADX WARNING: Missing block: B:255:0x0603, code:
            r17 = r22.obtainMessage(12, r21, 0);
            r17.setAsynchronous(true);
            r17.sendToTarget();
     */
    /* JADX WARNING: Missing block: B:256:0x0621, code:
            r21 = 0;
     */
    /* JADX WARNING: Missing block: B:258:0x0630, code:
            if (r26.mShortPressWindowBehavior != 1) goto L_0x01fd;
     */
    /* JADX WARNING: Missing block: B:260:0x0638, code:
            if (r26.mTvPictureInPictureVisible == false) goto L_0x01fd;
     */
    /* JADX WARNING: Missing block: B:261:0x063a, code:
            if (r8 != false) goto L_0x063f;
     */
    /* JADX WARNING: Missing block: B:262:0x063c, code:
            showTvPictureInPictureMenu(r27);
     */
    /* JADX WARNING: Missing block: B:263:0x063f, code:
            r18 = r18 & -2;
     */
    /* JADX WARNING: Missing block: B:264:0x0643, code:
            if (r8 == false) goto L_0x064d;
     */
    /* JADX WARNING: Missing block: B:266:0x0649, code:
            if (r27.getRepeatCount() != 0) goto L_0x064d;
     */
    /* JADX WARNING: Missing block: B:267:0x064b, code:
            r20 = true;
     */
    /* JADX WARNING: Missing block: B:269:0x0657, code:
            if (r26.mDisplayManagerInternal.isBlockScreenOnByFingerPrint() == false) goto L_0x01fd;
     */
    /* JADX WARNING: Missing block: B:270:0x0659, code:
            r20 = false;
     */
    /* JADX WARNING: Missing block: B:271:0x065d, code:
            performHapticFeedbackLw(null, 1, false);
     */
    /* JADX WARNING: Missing block: B:272:0x0670, code:
            wakeUp(r27.getEventTime(), r26.mAllowTheaterModeWakeFromKey, "android.policy:KEY");
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "JianHui.Yu@Plf.SDK, 2015-01-06 : Modify for Longshot", property = OppoRomType.ROM)
    public int interceptKeyBeforeQueueing(KeyEvent event, int policyFlags) {
        if (!this.mSystemBooted) {
            return 0;
        }
        if (interceptKeyBeforeHandling(event)) {
            return 0;
        }
        if (26 == event.getKeyCode() && this.mIsAlarmBoot) {
            return 0;
        }
        synchronized (this.mKeyDispatchLock) {
            if (1 == this.mKeyDispatcMode) {
                return 0;
            }
        }
    }

    private void interceptSystemNavigationKey(KeyEvent event) {
        if (event.getAction() == 1 && areSystemNavigationKeysEnabled()) {
            IStatusBarService sbar = getStatusBarService();
            if (sbar != null) {
                try {
                    sbar.handleSystemNavigationKey(event.getKeyCode());
                } catch (RemoteException e) {
                }
            }
        }
    }

    private static boolean isValidGlobalKey(int keyCode) {
        switch (keyCode) {
            case H.DO_ANIMATION_CALLBACK /*26*/:
            case NetdResponseCode.ClatdStatusResult /*223*/:
            case 224:
                return false;
            default:
                return true;
        }
    }

    private boolean isWakeKeyWhenScreenOff(int keyCode) {
        boolean z = true;
        switch (keyCode) {
            case 24:
            case 25:
            case 164:
                if (this.mDockMode == 0) {
                    z = false;
                }
                return z;
            case 27:
            case HdmiCecKeycode.CEC_KEYCODE_RESERVED /*79*/:
            case HdmiCecKeycode.CEC_KEYCODE_INITIAL_CONFIGURATION /*85*/:
            case HdmiCecKeycode.CEC_KEYCODE_SELECT_BROADCAST_TYPE /*86*/:
            case HdmiCecKeycode.CEC_KEYCODE_SELECT_SOUND_PRESENTATION /*87*/:
            case 88:
            case 89:
            case 90:
            case 91:
            case 126:
            case 127:
            case 130:
            case NetdResponseCode.DnsProxyQueryResult /*222*/:
                return false;
            default:
                return true;
        }
    }

    public int interceptMotionBeforeQueueingNonInteractive(long whenNanos, int policyFlags) {
        if ((policyFlags & 1) != 0 && wakeUp(whenNanos / 1000000, this.mAllowTheaterModeWakeFromMotion, "android.policy:MOTION")) {
            return 0;
        }
        if (shouldDispatchInputWhenNonInteractive(null)) {
            return 1;
        }
        if (isTheaterModeEnabled() && (policyFlags & 1) != 0) {
            wakeUp(whenNanos / 1000000, this.mAllowTheaterModeWakeFromMotionWhenNotDreaming, "android.policy:MOTION");
        }
        return 0;
    }

    private boolean shouldDispatchInputWhenNonInteractive(KeyEvent event) {
        if (this.mDisplayManagerInternal.isBlockScreenOnByFingerPrint()) {
            return false;
        }
        boolean displayOff;
        if (this.mDisplay == null || this.mDisplay.getState() == 1) {
            displayOff = true;
        } else {
            displayOff = false;
        }
        if (displayOff && !this.mHasFeatureWatch) {
            return false;
        }
        if (isKeyguardShowingAndNotOccluded() && !displayOff) {
            return true;
        }
        if (this.mHasFeatureWatch && event != null && (event.getKeyCode() == 4 || event.getKeyCode() == DhcpPacket.MIN_PACKET_LENGTH_L3)) {
            return false;
        }
        IDreamManager dreamManager = getDreamManager();
        if (dreamManager != null) {
            try {
                if (dreamManager.isDreaming()) {
                    return true;
                }
            } catch (RemoteException e) {
                Slog.e(TAG, "RemoteException when checking if dreaming", e);
            }
        }
        return false;
    }

    private void dispatchDirectAudioEvent(KeyEvent event) {
        if (event.getAction() == 0) {
            int keyCode = event.getKeyCode();
            String pkgName = this.mContext.getOpPackageName();
            switch (keyCode) {
                case 24:
                    try {
                        getAudioService().adjustSuggestedStreamVolume(1, Integer.MIN_VALUE, 4101, pkgName, TAG);
                        break;
                    } catch (RemoteException e) {
                        Log.e(TAG, "Error dispatching volume up in dispatchTvAudioEvent.", e);
                        break;
                    }
                case 25:
                    try {
                        getAudioService().adjustSuggestedStreamVolume(-1, Integer.MIN_VALUE, 4101, pkgName, TAG);
                        break;
                    } catch (RemoteException e2) {
                        Log.e(TAG, "Error dispatching volume down in dispatchTvAudioEvent.", e2);
                        break;
                    }
                case 164:
                    try {
                        if (event.getRepeatCount() == 0) {
                            getAudioService().adjustSuggestedStreamVolume(101, Integer.MIN_VALUE, 4101, pkgName, TAG);
                            break;
                        }
                    } catch (RemoteException e22) {
                        Log.e(TAG, "Error dispatching mute in dispatchTvAudioEvent.", e22);
                        break;
                    }
                    break;
            }
        }
    }

    void dispatchMediaKeyWithWakeLock(KeyEvent event) {
        if (DEBUG_INPUT) {
            Slog.d(TAG, "dispatchMediaKeyWithWakeLock: " + event);
        }
        if (this.mHavePendingMediaKeyRepeatWithWakeLock) {
            if (DEBUG_INPUT) {
                Slog.d(TAG, "dispatchMediaKeyWithWakeLock: canceled repeat");
            }
            this.mHandler.removeMessages(4);
            this.mHavePendingMediaKeyRepeatWithWakeLock = false;
            this.mBroadcastWakeLock.release();
        }
        dispatchMediaKeyWithWakeLockToAudioService(event);
        if (event.getAction() == 0 && event.getRepeatCount() == 0) {
            this.mHavePendingMediaKeyRepeatWithWakeLock = true;
            Message msg = this.mHandler.obtainMessage(4, event);
            msg.setAsynchronous(true);
            this.mHandler.sendMessageDelayed(msg, (long) ViewConfiguration.getKeyRepeatTimeout());
            return;
        }
        this.mBroadcastWakeLock.release();
    }

    void dispatchMediaKeyRepeatWithWakeLock(KeyEvent event) {
        this.mHavePendingMediaKeyRepeatWithWakeLock = false;
        KeyEvent repeatEvent = KeyEvent.changeTimeRepeat(event, SystemClock.uptimeMillis(), 1, event.getFlags() | 128);
        if (DEBUG_INPUT) {
            Slog.d(TAG, "dispatchMediaKeyRepeatWithWakeLock: " + repeatEvent);
        }
        dispatchMediaKeyWithWakeLockToAudioService(repeatEvent);
        this.mBroadcastWakeLock.release();
    }

    void dispatchMediaKeyWithWakeLockToAudioService(KeyEvent event) {
        if (ActivityManagerNative.isSystemReady()) {
            MediaSessionLegacyHelper.getHelper(this.mContext).sendMediaButtonEvent(event, true);
        }
    }

    void launchVoiceAssistWithWakeLock(boolean keyguardActive) {
        IDeviceIdleController dic = IDeviceIdleController.Stub.asInterface(ServiceManager.getService("deviceidle"));
        if (dic != null) {
            try {
                dic.exitIdle("voice-search");
            } catch (RemoteException e) {
            }
        }
        Intent voiceIntent = new Intent("android.speech.action.VOICE_SEARCH_HANDS_FREE");
        voiceIntent.putExtra("android.speech.extras.EXTRA_SECURE", keyguardActive);
        startActivityAsUser(voiceIntent, UserHandle.CURRENT_OR_SELF);
        this.mBroadcastWakeLock.release();
    }

    /* JADX WARNING: Missing block: B:16:0x0026, code:
            return;
     */
    /* JADX WARNING: Missing block: B:32:0x0056, code:
            return;
     */
    /* JADX WARNING: Missing block: B:44:0x0071, code:
            return;
     */
    /* JADX WARNING: Missing block: B:53:0x0088, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void requestTransientBars(WindowState swipeTarget) {
        synchronized (this.mWindowManagerFuncs.getWindowManagerLock()) {
            if (!isUserSetupComplete()) {
            } else if (swipeTarget != this.mNavigationBar || this.mNavigationBarEnableStatus != 2) {
                boolean sb = this.mStatusBarController.checkShowTransientBarLw();
                boolean nb = this.mNavigationBarController.checkShowTransientBarLw() ? !isNavBarEmpty(this.mLastSystemUiFlags) : false;
                if (sb || nb) {
                    if (nb || swipeTarget != this.mNavigationBar) {
                        if (!this.mHideStatusBarWhenLockScreen || this.mNavigationBar != swipeTarget) {
                            if (sb) {
                                this.mStatusBarController.showTransient();
                            }
                            if (nb && !this.mHideStatusBarWhenLockScreen) {
                                this.mNavigationBarController.showTransient();
                            }
                            this.mImmersiveModeConfirmation.confirmCurrentPrompt();
                            updateSystemUiVisibilityLw();
                        } else if (DEBUG) {
                            Slog.d(TAG, "Not showing transient bar, force hide status bar for Keyguard");
                        }
                    } else if (DEBUG) {
                        Slog.d(TAG, "Not showing transient bar, wrong swipe target");
                    }
                }
            } else if (DEBUG) {
                Slog.d(TAG, "NAVIGATIONBAR GESTURE Mode Not showing Navigation bar");
            }
        }
    }

    public void startedGoingToSleep(int why) {
        if (DEBUG_WAKEUP) {
            Slog.i(TAG, "Started going to sleep... (why=" + why + ")");
        }
        this.mCameraGestureTriggeredDuringGoingToSleep = false;
        this.mGoingToSleep = true;
        if (this.mKeyguardDelegate != null) {
            this.mKeyguardDelegate.onStartedGoingToSleep(why);
        }
    }

    public void finishedGoingToSleep(int why) {
        EventLog.writeEvent(70000, 0);
        if (DEBUG_WAKEUP) {
            Slog.i(TAG, "Finished going to sleep... (why=" + why + ")");
        }
        MetricsLogger.histogram(this.mContext, "screen_timeout", this.mLockScreenTimeout / 1000);
        this.mGoingToSleep = false;
        synchronized (this.mLock) {
            this.mAwake = false;
            updateWakeGestureListenerLp();
            updateOrientationListenerLp();
            updateLockScreenTimeout();
            this.mScreenshotChordVolumeUpKeyTriggered = false;
            this.mScreenshotChordVolumeDownKeyTriggered = false;
        }
        if (this.mKeyguardDelegate != null) {
            this.mKeyguardDelegate.onFinishedGoingToSleep(why, this.mCameraGestureTriggeredDuringGoingToSleep);
        }
        this.mCameraGestureTriggeredDuringGoingToSleep = false;
    }

    private FingerprintInternal getFingerprintInternal() {
        if (this.mFingerprintInternal == null) {
            this.mFingerprintInternal = (FingerprintInternal) LocalServices.getService(FingerprintInternal.class);
        }
        return this.mFingerprintInternal;
    }

    public void startedWakingUp() {
        EventLog.writeEvent(70000, 1);
        if (DEBUG_WAKEUP) {
            Slog.i(TAG, "Started waking up...");
        }
        synchronized (this.mLock) {
            this.mAwake = true;
            updateWakeGestureListenerLp();
            updateOrientationListenerLp();
            updateLockScreenTimeout();
        }
        if (this.mKeyguardDelegate != null) {
            this.mKeyguardDelegate.onStartedWakingUp();
        }
    }

    public void finishedWakingUp() {
        if (DEBUG_WAKEUP) {
            Slog.i(TAG, "Finished waking up...");
        }
    }

    private void wakeUpFromPowerKey(long eventTime) {
        this.mScreenOnCpuBoostHelper.acquireCpuBoost(1000);
        if (getFingerprintInternal() != null) {
            getFingerprintInternal().notifyPowerKeyPressed();
        }
        wakeUp(eventTime, this.mAllowTheaterModeWakeFromPowerKey, "android.policy:POWER");
    }

    private boolean wakeUp(long wakeTime, boolean wakeInTheaterMode, String reason) {
        boolean theaterModeEnabled = isTheaterModeEnabled();
        if (!wakeInTheaterMode && theaterModeEnabled) {
            return false;
        }
        if (theaterModeEnabled) {
            Global.putInt(this.mContext.getContentResolver(), "theater_mode_on", 0);
        }
        this.mPowerManager.wakeUp(wakeTime, reason);
        return true;
    }

    /* JADX WARNING: Missing block: B:8:0x000c, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void finishKeyguardDrawn() {
        synchronized (this.mLock) {
            if (!this.mScreenOnEarly || this.mKeyguardDrawComplete) {
            } else {
                this.mKeyguardDrawComplete = true;
                if (this.mKeyguardDelegate != null) {
                    this.mPhoneWinHandler.removeMessages(6);
                }
                this.mWindowManagerDrawComplete = false;
                this.mWindowManagerInternal.waitForAllWindowsDrawn(this.mWindowManagerDrawCallback, 1000);
            }
        }
    }

    public void screenTurnedOff() {
        if (DEBUG_WAKEUP) {
            Slog.i(TAG, "Screen turned off...");
        }
        updateScreenOffSleepToken(true);
        synchronized (this.mLock) {
            this.mScreenOnEarly = false;
            this.mScreenOnFully = false;
            this.mKeyguardDrawComplete = false;
            this.mWindowManagerDrawComplete = false;
            this.mScreenOnListener = null;
            updateOrientationListenerLp();
            if (this.mKeyguardDelegate != null) {
                this.mKeyguardDelegate.onScreenTurnedOff();
            }
        }
        this.mHandler.removeCallbacks(this.mRecentsStartSplitSreen);
    }

    public void screenTurningOn(ScreenOnListener screenOnListener) {
        if (DEBUG_WAKEUP) {
            Slog.i(TAG, "Screen turning on...");
        }
        updateScreenOffSleepToken(false);
        synchronized (this.mLock) {
            this.mScreenOnEarly = true;
            this.mScreenOnFully = false;
            this.mKeyguardDrawComplete = false;
            this.mWindowManagerDrawComplete = false;
            this.mScreenOnListener = screenOnListener;
            if (this.mKeyguardDelegate != null) {
                this.mPhoneWinHandler.removeMessages(6);
                this.mPhoneWinHandler.sendEmptyMessageDelayed(6, 1000);
                this.mKeyguardDelegate.onScreenTurningOn(this.mKeyguardDrawnCallback);
            } else {
                if (DEBUG_WAKEUP) {
                    Slog.d(TAG, "null mKeyguardDelegate: setting mKeyguardDrawComplete.");
                }
                finishKeyguardDrawn();
            }
        }
    }

    public void screenTurnedOn() {
        if (DEBUG_WAKEUP) {
            Slog.i(TAG, "Screen turned on...");
        }
        synchronized (this.mLock) {
            if (this.mKeyguardDelegate != null) {
                this.mKeyguardDelegate.onScreenTurnedOn();
            }
        }
    }

    /* JADX WARNING: Missing block: B:8:0x000c, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void finishWindowsDrawn() {
        synchronized (this.mLock) {
            if (!this.mScreenOnEarly || this.mWindowManagerDrawComplete) {
            } else {
                this.mWindowManagerDrawComplete = true;
                finishScreenTurningOn();
            }
        }
    }

    /* JADX WARNING: Missing block: B:32:0x009e, code:
            if (r1 == null) goto L_0x00a3;
     */
    /* JADX WARNING: Missing block: B:33:0x00a0, code:
            r1.onScreenOn();
     */
    /* JADX WARNING: Missing block: B:34:0x00a3, code:
            if (r0 == false) goto L_0x00aa;
     */
    /* JADX WARNING: Missing block: B:36:?, code:
            r7.mWindowManager.enableScreenIfNeeded();
     */
    /* JADX WARNING: Missing block: B:43:0x00b2, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void finishScreenTurningOn() {
        synchronized (this.mLock) {
            updateOrientationListenerLp();
        }
        synchronized (this.mLock) {
            if (DEBUG_WAKEUP) {
                Slog.d(TAG, "finishScreenTurningOn: mAwake=" + this.mAwake + ", mScreenOnEarly=" + this.mScreenOnEarly + ", mScreenOnFully=" + this.mScreenOnFully + ", mKeyguardDrawComplete=" + this.mKeyguardDrawComplete + ", mWindowManagerDrawComplete=" + this.mWindowManagerDrawComplete);
            }
            if (this.mScreenOnFully || !this.mScreenOnEarly || !this.mWindowManagerDrawComplete || (this.mAwake && !this.mKeyguardDrawComplete)) {
            } else {
                if (DEBUG_WAKEUP) {
                    Slog.i(TAG, "Finished screen turning on...");
                }
                ScreenOnListener listener = this.mScreenOnListener;
                this.mScreenOnListener = null;
                this.mScreenOnFully = true;
                boolean enableScreen;
                if (this.mKeyguardDrawnOnce || !this.mAwake) {
                    enableScreen = false;
                } else {
                    this.mKeyguardDrawnOnce = true;
                    enableScreen = true;
                    if (this.mBootMessageNeedsHiding) {
                        this.mBootMessageNeedsHiding = false;
                        hideBootMessages();
                    }
                }
            }
        }
        handleScreenTurningOn();
    }

    /* JADX WARNING: Missing block: B:10:0x0010, code:
            if (r3.mBootMsgDialog == null) goto L_0x0026;
     */
    /* JADX WARNING: Missing block: B:12:0x0014, code:
            if (DEBUG_WAKEUP == false) goto L_0x001f;
     */
    /* JADX WARNING: Missing block: B:13:0x0016, code:
            android.util.Slog.d(TAG, "handleHideBootMessage: dismissing");
     */
    /* JADX WARNING: Missing block: B:14:0x001f, code:
            r3.mBootMsgDialog.dismiss();
            r3.mBootMsgDialog = null;
     */
    /* JADX WARNING: Missing block: B:15:0x0026, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void handleHideBootMessage() {
        synchronized (this.mLock) {
            if (!this.mKeyguardDrawnOnce) {
                this.mBootMessageNeedsHiding = true;
            }
        }
    }

    public boolean isScreenOn() {
        return this.mScreenOnFully;
    }

    public void enableKeyguard(boolean enabled) {
        if (this.mKeyguardDelegate != null) {
            this.mKeyguardDelegate.setKeyguardEnabled(enabled);
        }
    }

    public void exitKeyguardSecurely(OnKeyguardExitResult callback) {
        if (this.mKeyguardDelegate != null) {
            this.mKeyguardDelegate.verifyUnlock(callback);
        }
    }

    private boolean isKeyguardShowingAndNotOccluded() {
        boolean z = false;
        if (this.mKeyguardDelegate == null) {
            return false;
        }
        if (this.mKeyguardDelegate.isShowing() && !this.mKeyguardOccluded) {
            z = true;
        }
        return z;
    }

    public boolean isKeyguardLocked() {
        return keyguardOn();
    }

    public boolean isKeyguardSecure(int userId) {
        if (this.mKeyguardDelegate == null) {
            return false;
        }
        return this.mKeyguardDelegate.isSecure(userId);
    }

    public boolean isKeyguardShowingOrOccluded() {
        return this.mKeyguardDelegate == null ? false : this.mKeyguardDelegate.isShowing();
    }

    public boolean inKeyguardRestrictedKeyInputMode() {
        if (this.mKeyguardDelegate == null) {
            return false;
        }
        return this.mKeyguardDelegate.isInputRestricted();
    }

    public void dismissKeyguardLw() {
        if (this.mKeyguardDelegate != null && this.mKeyguardDelegate.isShowing()) {
            if (DEBUG_KEYGUARD) {
                Slog.d(TAG, "PWM.dismissKeyguardLw");
            }
            this.mHandler.post(new Runnable() {
                public void run() {
                    PhoneWindowManager.this.mKeyguardDelegate.dismiss(false);
                }
            });
        }
    }

    public void notifyActivityDrawnForKeyguardLw() {
        if (this.mKeyguardDelegate != null) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    PhoneWindowManager.this.mKeyguardDelegate.onActivityDrawn();
                }
            });
        }
    }

    public boolean isKeyguardDrawnLw() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mKeyguardDrawnOnce;
        }
        return z;
    }

    public void dispatchWakeUp(boolean isWakeUpByFingerprint) {
        Log.d(TAG, "dispatchWakeUp, isWakeUpByFingerprint = " + isWakeUpByFingerprint);
        if (this.mIsEnableKeyguardHide) {
            this.mWindowManagerFuncs.hideKeyguardByFingerprint(isWakeUpByFingerprint);
        } else {
            this.mKeyguardDelegate.dispatchWakeUp(isWakeUpByFingerprint);
        }
    }

    public void onWakeUp(String wakeUpReason) {
        Log.d(TAG, "onWakeUp, wakeUpReason = " + wakeUpReason);
        this.mKeyguardDelegate.onWakeUp(wakeUpReason);
    }

    public void startKeyguardExitAnimation(long startTime, long fadeoutDuration) {
        if (this.mKeyguardDelegate != null) {
            if (DEBUG_KEYGUARD) {
                Slog.d(TAG, "PWM.startKeyguardExitAnimation");
            }
            this.mKeyguardDelegate.startKeyguardExitAnimation(startTime, fadeoutDuration);
        }
    }

    public void getStableInsetsLw(int displayRotation, int displayWidth, int displayHeight, Rect outInsets) {
        outInsets.setEmpty();
        getNonDecorInsetsLw(displayRotation, displayWidth, displayHeight, outInsets);
        if (this.mStatusBar != null) {
            outInsets.top = this.mStatusBarHeight;
        }
    }

    public void getNonDecorInsetsLw(int displayRotation, int displayWidth, int displayHeight, Rect outInsets) {
        outInsets.setEmpty();
        if (this.mNavigationBar != null) {
            int position = navigationBarPosition(displayWidth, displayHeight, displayRotation);
            if (position == 0) {
                outInsets.bottom = getNavigationBarHeight(displayRotation, this.mUiMode);
            } else if (position == 1) {
                outInsets.right = getNavigationBarWidth(displayRotation, this.mUiMode);
            } else if (position == 2) {
                outInsets.left = getNavigationBarWidth(displayRotation, this.mUiMode);
            }
        }
    }

    public boolean isNavBarForcedShownLw(WindowState windowState) {
        return this.mForceShowSystemBars;
    }

    public boolean isDockSideAllowed(int dockSide) {
        boolean z = true;
        if (this.mNavigationBarCanMove) {
            if (!(dockSide == 2 || dockSide == 1)) {
                z = false;
            }
            return z;
        }
        if (!(dockSide == 2 || dockSide == 1 || dockSide == 3)) {
            z = false;
        }
        return z;
    }

    void sendCloseSystemWindows() {
        PhoneWindow.sendCloseSystemWindows(this.mContext, null);
    }

    void sendCloseSystemWindows(String reason) {
        PhoneWindow.sendCloseSystemWindows(this.mContext, reason);
    }

    public int rotationForOrientationLw(int orientation, int lastRotation) {
        if (this.mForceDefaultOrientation) {
            return 0;
        }
        synchronized (this.mLock) {
            int preferredRotation;
            int i;
            int sensorRotation = this.mOrientationListener.getProposedRotation();
            if (sensorRotation < 0) {
                sensorRotation = lastRotation;
            }
            if (this.mLidState == 1 && this.mLidOpenRotation >= 0) {
                preferredRotation = this.mLidOpenRotation;
            } else if (this.mDockMode == 2 && (this.mCarDockEnablesAccelerometer || this.mCarDockRotation >= 0)) {
                preferredRotation = this.mCarDockEnablesAccelerometer ? sensorRotation : this.mCarDockRotation;
            } else if ((this.mDockMode == 1 || this.mDockMode == 3 || this.mDockMode == 4) && (this.mDeskDockEnablesAccelerometer || this.mDeskDockRotation >= 0)) {
                preferredRotation = this.mDeskDockEnablesAccelerometer ? sensorRotation : this.mDeskDockRotation;
            } else if (this.mHdmiPlugged && this.mDemoHdmiRotationLock) {
                preferredRotation = this.mDemoHdmiRotation;
            } else if (this.mHdmiPlugged && this.mDockMode == 0 && this.mUndockedHdmiRotation >= 0) {
                preferredRotation = this.mUndockedHdmiRotation;
            } else if (this.mDemoRotationLock) {
                preferredRotation = this.mDemoRotation;
            } else if (orientation == 14) {
                preferredRotation = lastRotation;
            } else if (!this.mSupportAutoRotation) {
                preferredRotation = -1;
            } else if ((this.mUserRotationMode == 0 && (orientation == 2 || orientation == -1 || orientation == 11 || orientation == 12 || orientation == 13)) || orientation == 4 || orientation == 10 || orientation == 6 || orientation == 7) {
                if (this.mAllowAllRotations < 0) {
                    if (this.mContext.getResources().getBoolean(17956917)) {
                        i = 1;
                    } else {
                        i = 0;
                    }
                    this.mAllowAllRotations = i;
                }
                preferredRotation = (sensorRotation != 2 || this.mAllowAllRotations == 1 || orientation == 10 || orientation == 13) ? sensorRotation : lastRotation;
            } else {
                preferredRotation = (this.mUserRotationMode != 1 || orientation == 5) ? -1 : this.mUserRotation;
            }
            if (DEBUG_ORIENTATION) {
                Slog.v(TAG, "rotationForOrientationLw(appReqQrientation = " + orientation + ", lastOrientation = " + lastRotation + ", sensorRotation = " + sensorRotation + ", UserRotation = " + this.mUserRotation + ", LidState = " + this.mLidState + ", DockMode = " + this.mDockMode + ", DeskDockEnable = " + this.mDeskDockEnablesAccelerometer + ", CarDockEnable = " + this.mCarDockEnablesAccelerometer + ", HdmiPlugged = " + this.mHdmiPlugged + ", Accelerometer = " + this.mAccelerometerDefault + ", AllowAllRotations = " + this.mAllowAllRotations + ")");
            }
            switch (orientation) {
                case 0:
                    if (isLandscapeOrSeascape(preferredRotation)) {
                        return preferredRotation;
                    }
                    i = this.mLandscapeRotation;
                    return i;
                case 1:
                    if (isAnyPortrait(preferredRotation)) {
                        return preferredRotation;
                    }
                    i = this.mPortraitRotation;
                    return i;
                case 6:
                case 11:
                    if (isLandscapeOrSeascape(preferredRotation)) {
                        return preferredRotation;
                    } else if (isLandscapeOrSeascape(lastRotation)) {
                        return lastRotation;
                    } else {
                        i = this.mLandscapeRotation;
                        return i;
                    }
                case 7:
                case 12:
                    if (isAnyPortrait(preferredRotation)) {
                        return preferredRotation;
                    } else if (isAnyPortrait(lastRotation)) {
                        return lastRotation;
                    } else {
                        i = this.mPortraitRotation;
                        return i;
                    }
                case 8:
                    if (isLandscapeOrSeascape(preferredRotation)) {
                        return preferredRotation;
                    }
                    i = this.mSeascapeRotation;
                    return i;
                case 9:
                    if (isAnyPortrait(preferredRotation)) {
                        return preferredRotation;
                    }
                    i = this.mUpsideDownRotation;
                    return i;
                default:
                    if (preferredRotation >= 0) {
                        return preferredRotation;
                    }
                    return 0;
            }
        }
    }

    public boolean rotationHasCompatibleMetricsLw(int orientation, int rotation) {
        switch (orientation) {
            case 0:
            case 6:
            case 8:
                return isLandscapeOrSeascape(rotation);
            case 1:
            case 7:
            case 9:
                return isAnyPortrait(rotation);
            default:
                return true;
        }
    }

    public void setRotationLw(int rotation) {
        this.mOrientationListener.setCurrentRotation(rotation);
        try {
            IStatusBarService statusbar = getStatusBarService();
            if (statusbar != null) {
                statusbar.setNavigationBarColor(-1, -1, this.mExpendBar);
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "setRotationLw  setNavigationBarColor ", e);
            this.mStatusBarService = null;
        }
    }

    private boolean isLandscapeOrSeascape(int rotation) {
        return rotation == this.mLandscapeRotation || rotation == this.mSeascapeRotation;
    }

    private boolean isAnyPortrait(int rotation) {
        return rotation == this.mPortraitRotation || rotation == this.mUpsideDownRotation;
    }

    public int getUserRotationMode() {
        if (System.getIntForUser(this.mContext.getContentResolver(), "accelerometer_rotation", 0, -2) != 0) {
            return 0;
        }
        return 1;
    }

    public void setUserRotationMode(int mode, int rot) {
        ContentResolver res = this.mContext.getContentResolver();
        if (mode == 1) {
            System.putIntForUser(res, "user_rotation", rot, -2);
            System.putIntForUser(res, "accelerometer_rotation", 0, -2);
            return;
        }
        System.putIntForUser(res, "accelerometer_rotation", 1, -2);
    }

    public void setSafeMode(boolean safeMode) {
        int i;
        this.mSafeMode = safeMode;
        if (safeMode) {
            i = OppoScreenOffGestureManager.MSG_SCREEN_TURNED_OFF;
        } else {
            i = 10000;
        }
        performHapticFeedbackLw(null, i, true);
    }

    static long[] getLongIntArray(Resources r, int resid) {
        int[] ar = r.getIntArray(resid);
        if (ar == null) {
            return null;
        }
        long[] out = new long[ar.length];
        for (int i = 0; i < ar.length; i++) {
            out[i] = (long) ar[i];
        }
        return out;
    }

    public void systemReady() {
        boolean bindKeyguardNow;
        this.mKeyguardDelegate = new KeyguardServiceDelegate(this.mContext, new -void_systemReady__LambdaImpl0());
        this.mKeyguardDelegate.onSystemReady();
        readCameraLensCoverState();
        updateUiMode();
        synchronized (this.mLock) {
            updateOrientationListenerLp();
            this.mSystemReady = true;
            this.mHandler.post(new Runnable() {
                public void run() {
                    PhoneWindowManager.this.updateSettings();
                }
            });
            bindKeyguardNow = this.mDeferBindKeyguard;
            if (bindKeyguardNow) {
                this.mDeferBindKeyguard = false;
            }
        }
        if (bindKeyguardNow) {
            this.mKeyguardDelegate.onBootCompleted();
        }
        this.mSystemGestures.systemReady();
        this.mImmersiveModeConfirmation.systemReady();
    }

    public void systemBooted() {
        boolean bindKeyguardNow = false;
        synchronized (this.mLock) {
            if (this.mKeyguardDelegate != null) {
                bindKeyguardNow = true;
            } else {
                this.mDeferBindKeyguard = true;
            }
        }
        if (bindKeyguardNow) {
            this.mKeyguardDelegate.onBootCompleted();
        }
        synchronized (this.mLock) {
            this.mSystemBooted = true;
        }
        startedWakingUp();
        screenTurningOn(null);
        screenTurnedOn();
    }

    public void showBootMessage(final CharSequence msg, boolean always) {
        this.mHandler.post(new Runnable() {
            public void run() {
                if (PhoneWindowManager.this.mBootMsgDialog == null) {
                    int theme;
                    if (PhoneWindowManager.this.mContext.getPackageManager().hasSystemFeature("android.hardware.type.television")) {
                        theme = 16975020;
                    } else {
                        theme = 0;
                    }
                    PhoneWindowManager.this.mBootMsgDialog = new ProgressDialog(PhoneWindowManager.this.mContext, theme) {
                        public boolean dispatchKeyEvent(KeyEvent event) {
                            return true;
                        }

                        public boolean dispatchKeyShortcutEvent(KeyEvent event) {
                            return true;
                        }

                        public boolean dispatchTouchEvent(MotionEvent ev) {
                            return true;
                        }

                        public boolean dispatchTrackballEvent(MotionEvent ev) {
                            return true;
                        }

                        public boolean dispatchGenericMotionEvent(MotionEvent ev) {
                            return true;
                        }

                        public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
                            return true;
                        }
                    };
                    if (PhoneWindowManager.this.mContext.getPackageManager().isUpgrade()) {
                        PhoneWindowManager.this.mBootMsgDialog.setTitle(17040310);
                    } else {
                        PhoneWindowManager.this.mBootMsgDialog.setTitle(17040311);
                    }
                    PhoneWindowManager.this.mBootMsgDialog.setProgressStyle(0);
                    PhoneWindowManager.this.mBootMsgDialog.setIndeterminate(true);
                    PhoneWindowManager.this.mBootMsgDialog.getWindow().setType(2021);
                    PhoneWindowManager.this.mBootMsgDialog.getWindow().addFlags(258);
                    PhoneWindowManager.this.mBootMsgDialog.getWindow().setDimAmount(1.0f);
                    LayoutParams lp = PhoneWindowManager.this.mBootMsgDialog.getWindow().getAttributes();
                    lp.screenOrientation = 5;
                    PhoneWindowManager.this.mBootMsgDialog.getWindow().setAttributes(lp);
                    PhoneWindowManager.this.mBootMsgDialog.setCancelable(false);
                    PhoneWindowManager.this.mBootMsgDialog.show();
                }
                PhoneWindowManager.this.mBootMsgDialog.setMessage(msg);
            }
        });
    }

    public void hideBootMessages() {
        this.mHandler.sendEmptyMessage(11);
    }

    public void userActivity() {
        synchronized (this.mStkLock) {
            if (this.mIsStkUserActivityEnabled) {
                this.mHandler.post(this.mNotifyStk);
            }
        }
        synchronized (this.mScreenLockTimeout) {
            if (this.mLockScreenTimerActive) {
                this.mHandler.removeCallbacks(this.mScreenLockTimeout);
                this.mHandler.postDelayed(this.mScreenLockTimeout, (long) this.mLockScreenTimeout);
            }
        }
    }

    public void lockNow(Bundle options) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
        this.mHandler.removeCallbacks(this.mScreenLockTimeout);
        if (options != null) {
            this.mScreenLockTimeout.setLockOptions(options);
        }
        this.mHandler.post(this.mScreenLockTimeout);
    }

    private void updateLockScreenTimeout() {
        synchronized (this.mScreenLockTimeout) {
            boolean enable;
            if (this.mAllowLockscreenWhenOn && this.mAwake && this.mKeyguardDelegate != null) {
                enable = this.mKeyguardDelegate.isSecure(this.mCurrentUserId);
            } else {
                enable = false;
            }
            if (this.mLockScreenTimerActive != enable) {
                if (enable) {
                    if (localLOGV) {
                        Log.v(TAG, "setting lockscreen timer");
                    }
                    this.mHandler.removeCallbacks(this.mScreenLockTimeout);
                    this.mHandler.postDelayed(this.mScreenLockTimeout, (long) this.mLockScreenTimeout);
                } else {
                    if (localLOGV) {
                        Log.v(TAG, "clearing lockscreen timer");
                    }
                    this.mHandler.removeCallbacks(this.mScreenLockTimeout);
                }
                this.mLockScreenTimerActive = enable;
            }
        }
    }

    private void updateDreamingSleepToken(boolean acquire) {
        if (acquire) {
            if (this.mDreamingSleepToken == null) {
                this.mDreamingSleepToken = this.mActivityManagerInternal.acquireSleepToken("Dream");
            }
        } else if (this.mDreamingSleepToken != null) {
            this.mDreamingSleepToken.release();
            this.mDreamingSleepToken = null;
        }
    }

    private void updateScreenOffSleepToken(boolean acquire) {
        if (acquire) {
            if (this.mScreenOffSleepToken == null) {
                this.mScreenOffSleepToken = this.mActivityManagerInternal.acquireSleepToken("ScreenOff");
            }
        } else if (this.mScreenOffSleepToken != null) {
            this.mScreenOffSleepToken.release();
            this.mScreenOffSleepToken = null;
        }
    }

    public void enableScreenAfterBoot() {
        readLidState();
        applyLidSwitchState();
        updateRotation(true);
    }

    private void applyLidSwitchState() {
        if (this.mLidState == 0 && this.mLidControlsSleep) {
            this.mPowerManager.goToSleep(SystemClock.uptimeMillis(), 3, 1);
        } else if (this.mLidState == 0 && this.mLidControlsScreenLock) {
            this.mWindowManagerFuncs.lockDeviceNow();
        }
        synchronized (this.mLock) {
            updateWakeGestureListenerLp();
        }
    }

    void updateUiMode() {
        if (this.mUiModeManager == null) {
            this.mUiModeManager = Stub.asInterface(ServiceManager.getService("uimode"));
        }
        try {
            this.mUiMode = this.mUiModeManager.getCurrentModeType();
        } catch (RemoteException e) {
        }
    }

    void updateRotation(boolean alwaysSendConfiguration) {
        try {
            this.mWindowManager.updateRotation(alwaysSendConfiguration, false);
        } catch (RemoteException e) {
        }
    }

    void updateRotation(boolean alwaysSendConfiguration, boolean forceRelayout) {
        try {
            this.mWindowManager.updateRotation(alwaysSendConfiguration, forceRelayout);
        } catch (RemoteException e) {
        }
    }

    Intent createHomeDockIntent() {
        Intent intent;
        if (this.mUiMode == 3) {
            if (this.mEnableCarDockHomeCapture) {
                intent = this.mCarDockIntent;
            }
            intent = null;
        } else {
            if (this.mUiMode != 2) {
                if (this.mUiMode == 6 && (this.mDockMode == 1 || this.mDockMode == 4 || this.mDockMode == 3)) {
                    intent = this.mDeskDockIntent;
                } else {
                    intent = null;
                }
            }
            intent = null;
        }
        if (intent == null) {
            return null;
        }
        ActivityInfo ai = null;
        ResolveInfo info = this.mContext.getPackageManager().resolveActivityAsUser(intent, 65664, this.mCurrentUserId);
        if (info != null) {
            ai = info.activityInfo;
        }
        if (ai == null || ai.metaData == null || !ai.metaData.getBoolean("android.dock_home")) {
            return null;
        }
        Intent intent2 = new Intent(intent);
        intent2.setClassName(ai.packageName, ai.name);
        return intent2;
    }

    void startDockOrHome(boolean fromHomeKey, boolean awakenFromDreams) {
        Intent intent;
        if (awakenFromDreams) {
            awakenDreams();
        }
        Intent dock = createHomeDockIntent();
        if (dock != null) {
            if (fromHomeKey) {
                try {
                    dock.putExtra("android.intent.extra.FROM_HOME_KEY", fromHomeKey);
                } catch (ActivityNotFoundException e) {
                }
            }
            startActivityAsUser(dock, UserHandle.CURRENT);
            return;
        }
        if (fromHomeKey) {
            intent = new Intent(this.mHomeIntent);
            intent.putExtra("android.intent.extra.FROM_HOME_KEY", fromHomeKey);
        } else {
            intent = this.mHomeIntent;
        }
        startActivityAsUser(intent, UserHandle.CURRENT);
        InputLog.e(TAG, " startDockOrHome ok");
    }

    boolean goHome() {
        if (isUserSetupComplete()) {
            try {
                if (SystemProperties.getInt("persist.sys.uts-test-mode", 0) == 1) {
                    Log.d(TAG, "UTS-TEST-MODE");
                } else {
                    ActivityManagerNative.getDefault().stopAppSwitches();
                    sendCloseSystemWindows();
                    Intent dock = createHomeDockIntent();
                    if (dock != null && ActivityManagerNative.getDefault().startActivityAsUser(null, null, dock, dock.resolveTypeIfNeeded(this.mContext.getContentResolver()), null, null, 0, 1, null, null, -2) == 1) {
                        return false;
                    }
                }
                if (ActivityManagerNative.getDefault().startActivityAsUser(null, null, this.mHomeIntent, this.mHomeIntent.resolveTypeIfNeeded(this.mContext.getContentResolver()), null, null, 0, 1, null, null, -2) == 1) {
                    return false;
                }
            } catch (RemoteException e) {
            }
            return true;
        }
        Slog.i(TAG, "Not going home because user setup is in progress.");
        return false;
    }

    public void setCurrentOrientationLw(int newOrientation) {
        synchronized (this.mLock) {
            if (newOrientation != this.mCurrentAppOrientation) {
                this.mCurrentAppOrientation = newOrientation;
                updateOrientationListenerLp();
            }
        }
    }

    private void performAuditoryFeedbackForAccessibilityIfNeed() {
        if (isGlobalAccessibilityGestureEnabled() && !((AudioManager) this.mContext.getSystemService("audio")).isSilentMode()) {
            Ringtone ringTone = RingtoneManager.getRingtone(this.mContext, System.DEFAULT_NOTIFICATION_URI);
            ringTone.setStreamType(3);
            ringTone.play();
        }
    }

    private boolean isTheaterModeEnabled() {
        return Global.getInt(this.mContext.getContentResolver(), "theater_mode_on", 0) == 1;
    }

    private boolean isGlobalAccessibilityGestureEnabled() {
        return Global.getInt(this.mContext.getContentResolver(), "enable_accessibility_global_gesture_enabled", 0) == 1;
    }

    private boolean areSystemNavigationKeysEnabled() {
        return Secure.getIntForUser(this.mContext.getContentResolver(), "system_navigation_keys_enabled", 0, -2) == 1;
    }

    public boolean performHapticFeedbackLw(WindowState win, int effectId, boolean always) {
        if (this.mVibrator.hasVibrator()) {
            boolean hapticsDisabled;
            if (System.getIntForUser(this.mContext.getContentResolver(), "haptic_feedback_enabled", 0, -2) == 0) {
                hapticsDisabled = true;
            } else {
                hapticsDisabled = false;
            }
            if (!hapticsDisabled || always) {
                long[] pattern;
                int owningUid;
                String owningPackage;
                switch (effectId) {
                    case 0:
                        pattern = this.mLongPressVibePattern;
                        break;
                    case 1:
                        pattern = this.mVirtualKeyVibePattern;
                        break;
                    case 3:
                        pattern = this.mKeyboardTapVibePattern;
                        break;
                    case 4:
                        pattern = this.mClockTickVibePattern;
                        break;
                    case 5:
                        pattern = this.mCalendarDateVibePattern;
                        break;
                    case 6:
                        pattern = this.mContextClickVibePattern;
                        break;
                    case 10000:
                        pattern = this.mSafeModeDisabledVibePattern;
                        break;
                    case OppoScreenOffGestureManager.MSG_SCREEN_TURNED_OFF /*10001*/:
                        pattern = this.mSafeModeEnabledVibePattern;
                        break;
                    default:
                        return false;
                }
                if (win != null) {
                    owningUid = win.getOwningUid();
                    owningPackage = win.getOwningPackage();
                } else {
                    owningUid = Process.myUid();
                    owningPackage = this.mContext.getOpPackageName();
                }
                if (pattern.length == 1) {
                    this.mVibrator.vibrate(owningUid, owningPackage, pattern[0], VIBRATION_ATTRIBUTES);
                } else {
                    this.mVibrator.vibrate(owningUid, owningPackage, pattern, -1, VIBRATION_ATTRIBUTES);
                }
                return true;
            }
            if (InputLog.DEBUG) {
                Slog.w(TAG, "Vibrator: hapticsDisabled=" + hapticsDisabled);
            }
            return false;
        }
        if (InputLog.DEBUG) {
            Slog.w(TAG, "Vibrator: mVibrator.hasVibrator() false");
        }
        return false;
    }

    public void keepScreenOnStartedLw() {
    }

    public void keepScreenOnStoppedLw() {
        if (isKeyguardShowingAndNotOccluded()) {
            this.mPowerManager.userActivity(SystemClock.uptimeMillis(), false);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:96:0x035f  */
    /* JADX WARNING: Removed duplicated region for block: B:99:0x037b  */
    /* JADX WARNING: Removed duplicated region for block: B:110:0x03a2  */
    /* JADX WARNING: Removed duplicated region for block: B:114:0x03b1  */
    /* JADX WARNING: Removed duplicated region for block: B:121:0x03cc  */
    /* JADX WARNING: Removed duplicated region for block: B:136:0x04a4  */
    /* JADX WARNING: Removed duplicated region for block: B:139:0x04b5  */
    /* JADX WARNING: Removed duplicated region for block: B:362:0x0c3c  */
    /* JADX WARNING: Missing block: B:349:0x0bc5, code:
            if (isImmersiveNavbar(r26.mTopFullscreenOpaqueWindowState) == false) goto L_0x0bc7;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int updateSystemUiVisibilityLw() {
        WindowState winCandidate;
        if (this.mFocusedWindow != null) {
            winCandidate = this.mFocusedWindow;
        } else {
            winCandidate = this.mTopFullscreenOpaqueWindowState;
        }
        if (winCandidate == null) {
            return 0;
        }
        if (winCandidate.getAttrs().token == this.mImmersiveModeConfirmation.getWindowToken()) {
            winCandidate = isStatusBarKeyguard() ? this.mStatusBar : this.mTopFullscreenOpaqueWindowState;
            if (winCandidate == null) {
                return 0;
            }
        }
        WindowState win = winCandidate;
        if ((winCandidate.getAttrs().privateFlags & 1024) != 0 && this.mHideLockScreen) {
            return 0;
        }
        if (win.getAttrs().type == 2004 && !win.isVisibleLw()) {
            return 0;
        }
        int visibility;
        int diff;
        int fullscreenDiff;
        int dockedDiff;
        boolean needsMenu;
        int windowFocusFlags;
        int windowFocusDiff;
        Rect fullscreenStackBounds;
        Rect dockedStackBounds;
        StatusBarManagerInternal statusbar;
        if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
            Slog.d(TAG, "updateSystemUiVisibilityLw: mFocusedWindow=" + this.mFocusedWindow + ", mTopFullscreenOpaqueWindowState=" + this.mTopFullscreenOpaqueWindowState + ", mTopDockedOpaqueWindowState=" + this.mTopDockedOpaqueWindowState + ", mTopDockedWindowState=" + this.mTopDockedWindowState + ", winCandidate=" + winCandidate + ", win=" + win + ", win.getAppToken()=" + win.getAppToken() + ", mFocusedApp=" + this.mFocusedApp);
        }
        int tmpVisibility = (PolicyControl.getSystemUiVisibility(win, null) & (~this.mResettingSystemUiFlags)) & (~this.mForceClearedSystemUiFlags);
        if (this.mForcingShowNavBar && win.getSurfaceLayer() < this.mForcingShowNavBarLayer) {
            tmpVisibility &= ~PolicyControl.adjustClearableFlags(win, 7);
        }
        int fullscreenVisibility = updateLightStatusBarLw(0, this.mTopFullscreenOpaqueWindowState, this.mTopFullscreenOpaqueOrDimmingWindowState);
        int dockedVisibility = updateLightStatusBarLw(0, this.mTopDockedOpaqueWindowState, this.mTopDockedOpaqueOrDimmingWindowState);
        this.mWindowManagerFuncs.getStackBounds(0, this.mNonDockedStackBounds);
        this.mWindowManagerFuncs.getStackBounds(3, this.mDockedStackBounds);
        if (this.mTopFullscreenOpaqueWindowState == null) {
            fullscreenVisibility = this.mLastFullscreenStackSysUiFlags;
        }
        if (this.mTopDockedOpaqueWindowState == null) {
            dockedVisibility = this.mLastDockedStackSysUiFlags;
        }
        if (win.getAttrs().type == 2004 && win.isVisibleLw() && (tmpVisibility & 32) != 0) {
            this.mHideStatusBarWhenLockScreen = true;
        } else {
            this.mHideStatusBarWhenLockScreen = false;
        }
        int navVisibility = 0;
        int navigationBarColor = win.getAttrs().navigationBarColor;
        if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
            String str;
            String str2 = TAG;
            StringBuilder append = new StringBuilder().append("updateSystemUiVisibilityLw win=").append(win).append(" tmpVisibility=").append(Integer.toHexString(tmpVisibility)).append(" win.getSystemUiVisibility()").append(Integer.toHexString(win.getSystemUiVisibility())).append(" mLastValidStatusBarTint=").append(Integer.toHexString(this.mLastValidStatusBarTint)).append(" mLastValidNavigationBarTint=").append(Integer.toHexString(this.mLastValidNavigationBarTint)).append(" navigationBarVisibility=");
            if (win == null) {
                str = "null";
            } else {
                str = Integer.toHexString(win.getAttrs().navigationBarVisibility);
            }
            append = append.append(str).append(" navigationBarColor=");
            if (win == null) {
                str = "null";
            } else {
                str = Integer.toHexString(win.getAttrs().navigationBarColor);
            }
            Slog.d(str2, append.append(str).append(" mExpandBar=").append(this.mExpendBar).toString());
        }
        if (win != null && win.toString().contains("Starting ")) {
            if ((this.mLastSystemUiFlags & 8208) != 0) {
                tmpVisibility |= 8208;
            }
            if (win.getAttrs().navigationBarColor != 0) {
                if (lightNavigationBar(win)) {
                    tmpVisibility |= 64;
                }
                this.mLastValidNavigationBarTint = tmpVisibility & 64;
            } else if ((this.mLastSystemUiFlags & 64) != 0) {
                tmpVisibility |= 64;
            }
        } else if (win != null && (win.toString().contains("com.tencent.mm/com.tencent.mm.plugin.sns.ui.SnsOnlineVideoActivity") || win.toString().contains("com.tencent.mm/com.tencent.mm.ui.chatting.gallery.ImageGalleryUI") || (((this.mDisplayRotation == 1 || this.mDisplayRotation == 3) && win.toString().contains("com.qihoo.browser/com.qihoo.browser.BrowserActivity")) || win.toString().contains("com.tencent.mm/com.tencent.mm.plugin.sns.ui.SnsBrowseUI")))) {
            tmpVisibility &= ~64;
            this.mLastValidNavigationBarTint = tmpVisibility & 64;
        } else if (win != null && ("com.doc88.reader".equals(win.getOwningPackage()) || win.toString().contains("com.google.android.googlequicksearchbox/com.google.android.apps.gsa.searchnow.SearchNow") || win.toString().contains("io.moreless.tide/io.moreless.tide.about.AboutActivity") || win.toString().contains("com.ztrk.goldfishfinance.release/com.ztrk.goldfishfinance.activity.LiveDetailActivity") || win.toString().contains("com.lemi.lvr.superlvr/com.lemi.lvr.superlvr.ui.activity.VideoDetailActivity") || win.toString().contains("io.github.jackzrliu.wificonsultant/io.github.jackzrliu.wificonsultant.view.activity.MainActivity") || win.toString().contains("com.iodev.flashalerts/com.iodev.flashalert.view.MainSettingsActivity") || win.toString().contains("com.iodev.flashalerts/com.iodev.flashalert.view.AllAppsActivity") || win.toString().contains("com.tencent.reading/com.tencent.reading.ui.NewsDetailActivity") || win.toString().contains("com.tencent.reading/com.tencent.reading.ui.ImageDetailActivity") || win.toString().contains("com.hjw.videoparent/com.hjw.videoparent.ui.LoginActivity"))) {
            tmpVisibility |= 64;
            this.mLastValidNavigationBarTint = tmpVisibility & 64;
        } else if (this.mTopFullscreenOpaqueWindowState == null) {
            if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
                Slog.d(TAG, "topfull is null, keep system bar last light flag.");
            }
            if (this.mLastValidStatusBarTint != 0) {
                tmpVisibility |= 8208;
            } else {
                tmpVisibility &= ~8208;
            }
            if ((this.mLastValidNavigationBarTint & 64) != 0) {
                tmpVisibility |= 64;
            } else {
                tmpVisibility &= ~64;
            }
        } else if (win != this.mTopFullscreenOpaqueWindowState) {
            int topVisibility = this.mTopFullscreenOpaqueWindowState.getSystemUiVisibility();
            if (lightNavigationBar(this.mTopFullscreenOpaqueWindowState)) {
                if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
                    Slog.d(TAG, "top win is light navigation bar flag @" + this.mTopFullscreenOpaqueWindowState);
                }
                topVisibility |= 64;
            }
            if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
                Slog.d(TAG, "updateSystemUiVisibilityLw: topVisibility=" + Integer.toHexString(topVisibility));
            }
            if ((topVisibility & 8208) != 0) {
                if (win.getAttrs().type != 2004) {
                    if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
                        Slog.d(TAG, "top fullscreen window is light statusbar");
                    }
                    tmpVisibility |= 8208;
                    this.mLastValidStatusBarTint = tmpVisibility & 8208;
                } else {
                    this.mLastValidStatusBarTint = win.getSystemUiVisibility() & 8208;
                }
            } else if (!((tmpVisibility & 8208) == 0 || win.getAttrs().type == 2004 || this.mTopFullscreenOpaqueWindowState.toString().contains("Starting "))) {
                tmpVisibility &= ~8208;
            }
            if ((topVisibility & 64) != 0) {
                if (win.getAttrs().type != 2004) {
                    if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
                        Slog.d(TAG, "top fullscreen window is light navigation bar");
                    }
                    tmpVisibility |= 64;
                    this.mLastValidNavigationBarTint |= 64;
                } else if (lightNavigationBar(win)) {
                    this.mLastValidNavigationBarTint |= 64;
                } else {
                    this.mLastValidNavigationBarTint &= ~64;
                }
            } else if (lightNavigationBar(win)) {
                if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
                    Slog.d(TAG, "win is light navigation bar");
                }
                if (win.getAttrs().type == 2004 || this.mTopFullscreenOpaqueWindowState.toString().contains("Starting ")) {
                    tmpVisibility |= 64;
                } else {
                    tmpVisibility &= ~64;
                }
            }
        } else {
            this.mLastValidStatusBarTint = win.getSystemUiVisibility() & 8208;
            if (lightNavigationBar(win)) {
                tmpVisibility |= 64;
            }
            this.mLastValidNavigationBarTint = tmpVisibility & 64;
            if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
                Slog.d(TAG, "win is top full screen winVis=" + Integer.toHexString(win.getSystemUiVisibility()) + ", tmpVisibility=" + Integer.toHexString(tmpVisibility) + ", mLastValidStatusBarTint:" + Integer.toHexString(this.mLastValidStatusBarTint) + ", mLastValidNavigationBarTint=" + Integer.toHexString(this.mLastValidNavigationBarTint));
            }
        }
        if ((win.getAttrs().navigationBarVisibility & 134217728) != 0) {
            if ((win.getAttrs().navigationBarVisibility & 67108864) != 0) {
                tmpVisibility |= 8208;
            } else {
                tmpVisibility &= ~8208;
            }
            this.mLastValidStatusBarTint = tmpVisibility & 8208;
        }
        if (!(this.mTopFullscreenOpaqueWindowState == null || win != this.mTopFullscreenOpaqueWindowState || (win.getAttrs().flags & 4194304) == 0 || isImmersiveNavbar(win))) {
            if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
                Slog.d(TAG, "win has FLAG_DISMISS_KEYGUARD, draw background.");
            }
            navVisibility = 128;
            navigationBarColor = -263173;
            this.mLastNavigationBarColor = -263173;
            tmpVisibility &= ~8208;
        }
        if ((win.getSystemUiVisibility() & 16384) != 0) {
            this.mExpendBar = true;
        } else {
            this.mExpendBar = false;
        }
        if (!(win == this.mStatusBar || win.getAttrs().type == 2306)) {
            if (windowTypeToLayerLw(win.getAttrs().type) <= windowTypeToLayerLw(2019)) {
                if (!isFullscreen(win.getAttrs())) {
                    if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
                        Slog.d(TAG, "win is not full screen mLastNavVisibility:" + Integer.toHexString(this.mLastNavVisibility) + ", mLastNavigationBarColor:" + Integer.toHexString(this.mLastNavigationBarColor));
                    }
                    if (this.mFocusedWindow == null || this.mTopFullscreenOpaqueWindowState == null || this.mFocusedWindow == this.mTopFullscreenOpaqueWindowState) {
                        if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
                            Slog.d(TAG, "use last status, mTopFullscreenOpaqueWindowState:" + this.mTopFullscreenOpaqueWindowState);
                        }
                        this.mExpendBar = this.mLastExpand;
                        navVisibility = this.mLastNavVisibility;
                        navigationBarColor = this.mLastNavigationBarColor;
                    } else {
                        if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
                            Slog.d(TAG, "focus window isn't top fullscreen window. mTopFullscreenOpaqueWindowState:" + this.mTopFullscreenOpaqueWindowState);
                        }
                        if (!isImmersiveNavbar(this.mTopFullscreenOpaqueWindowState)) {
                            navVisibility |= 128;
                            if (this.mFocusedWindow != this.mStatusBar) {
                                navigationBarColor = this.mTopFullscreenOpaqueWindowState.getAttrs().navigationBarColor;
                                this.mLastNavigationBarColor = navigationBarColor;
                                if ((this.mTopFullscreenOpaqueWindowState.getSystemUiVisibility() & 16384) != 0) {
                                    this.mExpendBar = true;
                                } else {
                                    this.mExpendBar = false;
                                }
                            }
                        }
                    }
                } else if (this.mTopFullscreenOpaqueWindowState == null) {
                    if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
                        Slog.d(TAG, "mTopFullscreenOpaqueWindowState is null, mLastNavVisibility:" + Integer.toHexString(this.mLastNavVisibility) + ", mLastNavigationBarColor:" + Integer.toHexString(this.mLastNavigationBarColor) + ", mLastExpand:" + this.mLastExpand);
                    }
                    this.mExpendBar = this.mLastExpand;
                    navVisibility = this.mLastNavVisibility;
                    navigationBarColor = this.mLastNavigationBarColor;
                    if (!(this.mFocusedWindow == null || this.mTopFullscreenOpaqueWindowState == null || this.mFocusedWindow == this.mTopFullscreenOpaqueWindowState)) {
                        if (!isImmersiveNavbar(this.mTopFullscreenOpaqueWindowState)) {
                            navVisibility |= 128;
                            if (this.mFocusedWindow != this.mStatusBar) {
                                navigationBarColor = this.mTopFullscreenOpaqueWindowState.getAttrs().navigationBarColor;
                                this.mLastNavigationBarColor = navigationBarColor;
                                if ((this.mTopFullscreenOpaqueWindowState.getSystemUiVisibility() & 16384) != 0) {
                                    this.mExpendBar = true;
                                } else {
                                    this.mExpendBar = false;
                                }
                            }
                        }
                    }
                } else {
                    if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
                        Slog.d(TAG, "win is full screen & mTopFullscreenOpaqueWindowState isn't null! navigationBarVisibility:" + Integer.toHexString(win.getAttrs().navigationBarVisibility) + " navigationBarColor:" + Integer.toHexString(win.getAttrs().navigationBarColor) + " sysVis:" + win.getSystemUiVisibility() + " mExpendBar" + this.mExpendBar);
                    }
                    if ((win.getAttrs().navigationBarVisibility & 1073741824) != 0) {
                        if ((win.getSystemUiVisibility() & 4098) != 4098) {
                            navVisibility |= 128;
                            navigationBarColor = win.getAttrs().navigationBarColor;
                            this.mLastNavigationBarColor = navigationBarColor;
                        }
                    } else if (this.mFocusedWindow == null && (this.mLastNavVisibility & 128) != 0) {
                        if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
                            Slog.d(TAG, "focus is null & last is draw background");
                        }
                        navVisibility |= 128;
                        navigationBarColor = this.mLastNavigationBarColor;
                    } else if (win != this.mTopFullscreenOpaqueWindowState) {
                        if (isImmersiveNavbar(win)) {
                        }
                        if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
                            Slog.d(TAG, "win is not top full screen, draw background");
                        }
                        navVisibility |= 128;
                        navigationBarColor = win.getAttrs().navigationBarColor;
                        this.mLastNavigationBarColor = navigationBarColor;
                    } else if (!isImmersiveNavbar(win)) {
                        if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
                            Slog.d(TAG, "win is top full screen, draw background");
                        }
                        navVisibility |= 128;
                        navigationBarColor = win.getAttrs().navigationBarColor;
                        this.mLastNavigationBarColor = navigationBarColor;
                    }
                }
                if ("com.mxxcvn.fun.sea".equals(win.getOwningPackage())) {
                    tmpVisibility &= ~64;
                    this.mLastValidNavigationBarTint = tmpVisibility & 64;
                    navVisibility &= -129;
                }
                if (win.toString().contains("cn.com.nd.s")) {
                    navigationBarColor = -263173;
                }
                if (this.mForcingShowNavBar && win.getSurfaceLayer() < this.mForcingShowNavBarLayer && this.mDisplayRotation != 1 && this.mDisplayRotation != 3) {
                    if ((navVisibility & 128) != 0) {
                        navigationBarColor = -263173;
                    }
                    navVisibility |= 16384;
                }
                if (this.mForcingShowNavBar) {
                    navVisibility |= 32768;
                }
                if (!(this.mDisplayRotation == this.mLastDisplayRotation && navigationBarColor == this.mLastExpendBarColor && this.mExpendBar == this.mLastExpand && navVisibility == this.mLastNavVisibility)) {
                    if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
                        Slog.d(TAG, "setNavigationBarColorToStatusBar mDisplayRotation:" + this.mDisplayRotation + ",navigationBarColor:" + Integer.toHexString(navigationBarColor) + ",navVisibility:" + Integer.toHexString(navVisibility) + ",mExpendBar:" + this.mExpendBar);
                    }
                    this.mLastDisplayRotation = this.mDisplayRotation;
                    this.mLastNavVisibility = navVisibility;
                    setNavigationBarColorToStatusBar(navVisibility, navigationBarColor, this.mExpendBar);
                }
                visibility = updateSystemBarsLw(win, this.mLastSystemUiFlags, tmpVisibility);
                diff = visibility ^ this.mLastSystemUiFlags;
                fullscreenDiff = fullscreenVisibility ^ this.mLastFullscreenStackSysUiFlags;
                dockedDiff = dockedVisibility ^ this.mLastDockedStackSysUiFlags;
                needsMenu = win.getNeedsMenuLw(this.mTopFullscreenOpaqueWindowState);
                if (this.mFocusedWindow != null || this.mFocusedApp == null || this.mTopDockedWindowState == null || this.mFocusedApp != this.mFocusedWindow.getAppToken()) {
                    windowFocusFlags = this.mLastWindowFocusFlags;
                } else if (this.mFocusedWindow == this.mTopDockedWindowState) {
                    windowFocusFlags = 0;
                } else {
                    windowFocusFlags = 64;
                }
                windowFocusDiff = windowFocusFlags ^ this.mLastWindowFocusFlags;
                if (windowFocusDiff != 0) {
                    handleMultiWindowFocusChanged(windowFocusFlags);
                    this.mLastWindowFocusFlags = windowFocusFlags;
                }
                if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
                    Slog.d(TAG, "updateSystemUiVisibilityLw: diff=" + Integer.toHexString(diff) + ", mLastValidNavigationBarTint=" + Integer.toHexString(this.mLastValidNavigationBarTint) + ", fullscreenDiff=" + Integer.toHexString(fullscreenDiff) + ", dockedDiff=" + Integer.toHexString(dockedDiff) + ", windowFocusDiff=" + Integer.toHexString(windowFocusDiff));
                }
                if (diff != 0 && fullscreenDiff == 0 && dockedDiff == 0 && this.mLastFocusNeedsMenu == needsMenu && this.mFocusedApp == win.getAppToken() && this.mLastNonDockedStackBounds.equals(this.mNonDockedStackBounds) && this.mLastDockedStackBounds.equals(this.mDockedStackBounds)) {
                    return 0;
                }
                this.mLastSystemUiFlags = visibility;
                this.mLastFullscreenStackSysUiFlags = fullscreenVisibility;
                this.mLastDockedStackSysUiFlags = dockedVisibility;
                this.mLastFocusNeedsMenu = needsMenu;
                this.mFocusedApp = win.getAppToken();
                fullscreenStackBounds = new Rect(this.mNonDockedStackBounds);
                dockedStackBounds = new Rect(this.mDockedStackBounds);
                statusbar = getStatusBarManagerInternal();
                if (statusbar != null) {
                    if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
                        Slog.d(TAG, "setSystemUiVisibility vis:" + Integer.toHexString(visibility));
                    }
                    statusbar.setSystemUiVisibility(visibility, fullscreenVisibility, dockedVisibility, -1, fullscreenStackBounds, dockedStackBounds, win.toString());
                    statusbar.topAppWindowChanged(needsMenu);
                }
                return diff;
            }
        }
        if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
            Slog.d(TAG, "win is status bar or above navigation bar! type:" + win.getAttrs().type);
        }
        this.mExpendBar = this.mLastExpand;
        if ((this.mLastNavVisibility & 128) != 0) {
            navVisibility |= 128;
            navigationBarColor = this.mLastNavigationBarColor;
        }
        if ("com.mxxcvn.fun.sea".equals(win.getOwningPackage())) {
        }
        if (win.toString().contains("cn.com.nd.s")) {
        }
        if ((navVisibility & 128) != 0) {
        }
        navVisibility |= 16384;
        if (this.mForcingShowNavBar) {
        }
        if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
        }
        this.mLastDisplayRotation = this.mDisplayRotation;
        this.mLastNavVisibility = navVisibility;
        setNavigationBarColorToStatusBar(navVisibility, navigationBarColor, this.mExpendBar);
        visibility = updateSystemBarsLw(win, this.mLastSystemUiFlags, tmpVisibility);
        diff = visibility ^ this.mLastSystemUiFlags;
        fullscreenDiff = fullscreenVisibility ^ this.mLastFullscreenStackSysUiFlags;
        dockedDiff = dockedVisibility ^ this.mLastDockedStackSysUiFlags;
        needsMenu = win.getNeedsMenuLw(this.mTopFullscreenOpaqueWindowState);
        if (this.mFocusedWindow != null) {
        }
        windowFocusFlags = this.mLastWindowFocusFlags;
        windowFocusDiff = windowFocusFlags ^ this.mLastWindowFocusFlags;
        if (windowFocusDiff != 0) {
        }
        if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
        }
        if (diff != 0) {
        }
        this.mLastSystemUiFlags = visibility;
        this.mLastFullscreenStackSysUiFlags = fullscreenVisibility;
        this.mLastDockedStackSysUiFlags = dockedVisibility;
        this.mLastFocusNeedsMenu = needsMenu;
        this.mFocusedApp = win.getAppToken();
        fullscreenStackBounds = new Rect(this.mNonDockedStackBounds);
        dockedStackBounds = new Rect(this.mDockedStackBounds);
        statusbar = getStatusBarManagerInternal();
        if (statusbar != null) {
        }
        return diff;
    }

    private boolean isImmersiveNavbar(WindowState win) {
        if (win != null) {
            if (win.toString().contains("com.android.quicksearchbox.SearchActivity") || win.toString().contains("com.tencent.mm/com.tencent.mm.plugin.sns.ui.SnsBrowseUI") || win.toString().contains("com.tencent.mm/com.tencent.mm.ui.chatting.gallery.ImageGalleryUI") || win.toString().contains("com.tencent.mtt/com.tencent.mtt.BlockActivity") || win.toString().contains("com.android.packageinstaller/com.android.packageinstaller.PackageInstallerActivity") || "com.coloros.gallery3d".equals(win.getOwningPackage())) {
                return true;
            }
            if (drawsSystemBarBackground(win) && win.toString().contains("Starting ") && (win.getAttrs().navigationBarVisibility & 536870912) == 0) {
                return true;
            }
            Rect contentRect = win.getContentFrameLw();
            Point realSize = new Point(0, 0);
            this.mDisplay.getRealSize(realSize);
            int rotation = this.mDisplayRotation;
            if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
                Slog.d(TAG, "isImmersiveNavbar: win=" + win + ",contentRect" + contentRect + ", realSize=" + realSize + ", rotation=" + rotation + ", navigationBarVisibility:" + Integer.toHexString(win.getAttrs().navigationBarVisibility));
            }
            if ((rotation == 0 && realSize.y == contentRect.bottom) || ((rotation == 1 && realSize.x == contentRect.right) || ((rotation == 3 && contentRect.left == 0) || (win.getAttrs().navigationBarVisibility & 268435456) != 0))) {
                return true;
            }
        }
        if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
            Slog.d(TAG, "isImmersiveNavbar: win=" + win + " isn't immersive nav bar!");
        }
        return false;
    }

    private int updateLightStatusBarLw(int vis, WindowState opaque, WindowState opaqueOrDimming) {
        WindowState statusColorWin;
        if (!isStatusBarKeyguard() || this.mHideLockScreen) {
            statusColorWin = opaqueOrDimming;
        } else {
            statusColorWin = this.mStatusBar;
        }
        if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
            Slog.d(TAG, "updateLightStatusBarLw: opaque=" + opaque + ", opaqueOrDimming" + opaqueOrDimming + ", mStatusBar=" + this.mStatusBar + ", statusColorWin=" + statusColorWin);
        }
        if (statusColorWin == null) {
            return vis;
        }
        if (statusColorWin == opaque) {
            vis = (((vis & -8193) & -17) | (PolicyControl.getSystemUiVisibility(statusColorWin, null) & 16)) | (PolicyControl.getSystemUiVisibility(statusColorWin, null) & DumpState.DUMP_PREFERRED_XML);
            if (!WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
                return vis;
            }
            Slog.d(TAG, "updateLightStatusBarLw: windowVis=" + Integer.toHexString(PolicyControl.getSystemUiVisibility(statusColorWin, null)) + ", vis=" + Integer.toHexString(vis));
            return vis;
        } else if (statusColorWin == null || !statusColorWin.isDimming()) {
            return vis;
        } else {
            return (vis & -8193) & -17;
        }
    }

    private boolean lightNavigationBar(WindowState win) {
        if (win == null) {
            return true;
        }
        if (((win.getAttrs().navigationBarVisibility & Integer.MIN_VALUE) == 0 || ((win.getSystemUiVisibility() & Integer.MIN_VALUE) != 0 && (win.getAttrs().navigationBarVisibility & 268435456) == 0)) && (win.getSystemUiVisibility() & 128) == 0) {
            return false;
        }
        return true;
    }

    private boolean drawsSystemBarBackground(WindowState win) {
        return win == null || (win.getAttrs().flags & Integer.MIN_VALUE) != 0;
    }

    private boolean forcesDrawStatusBarBackground(WindowState win) {
        return win == null || (win.getAttrs().privateFlags & DumpState.DUMP_INTENT_FILTER_VERIFIERS) != 0;
    }

    private int updateSystemBarsLw(WindowState win, int oldVis, int vis) {
        WindowState fullscreenTransWin;
        boolean fullscreenDrawsStatusBarBackground;
        boolean dockedDrawsStatusBarBackground;
        boolean transientNavBarAllowed;
        boolean denyTransientStatus;
        boolean dockedStackVisible = this.mWindowManagerInternal.isStackVisible(3);
        boolean freeformStackVisible = this.mWindowManagerInternal.isStackVisible(2);
        boolean resizing = this.mWindowManagerInternal.isDockedDividerResizing();
        boolean z = (dockedStackVisible || freeformStackVisible) ? true : resizing;
        this.mForceShowSystemBars = z;
        boolean forceOpaqueStatusBar = this.mForceShowSystemBars && !this.mForceStatusBarFromKeyguard;
        if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
            Slog.d(TAG, "updateSystemBarsLw() win=" + win + ", oldVis=" + Integer.toHexString(oldVis) + ", vis=" + Integer.toHexString(vis) + ", dockedStackVisible=" + dockedStackVisible + ", freeformStackVisible=" + freeformStackVisible + ", resizing=" + resizing + ", forceOpaqueStatusBar=" + forceOpaqueStatusBar);
        }
        if (!isStatusBarKeyguard() || this.mHideLockScreen) {
            fullscreenTransWin = this.mTopFullscreenOpaqueWindowState;
        } else {
            fullscreenTransWin = this.mStatusBar;
        }
        vis = this.mNavigationBarController.applyTranslucentFlagLw(fullscreenTransWin, this.mStatusBarController.applyTranslucentFlagLw(fullscreenTransWin, vis, oldVis), oldVis);
        int dockedVis = this.mStatusBarController.applyTranslucentFlagLw(this.mTopDockedOpaqueWindowState, 0, 0);
        if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
            Slog.d(TAG, "updateSystemBarsLw() applyTranslucentFlagLw vis=" + Integer.toHexString(vis) + ", fullscreenTransWin=" + fullscreenTransWin + ", mStatusBar=" + this.mStatusBar + ", mTopFullscreenOpaqueWindowState=" + this.mTopFullscreenOpaqueWindowState + ", mTopDockedOpaqueWindowState=" + this.mTopDockedOpaqueWindowState + ", dockedVis=" + Integer.toHexString(dockedVis) + ", flags=" + Integer.toHexString(win.getAttrs().flags & Integer.MIN_VALUE));
        }
        if (drawsSystemBarBackground(this.mTopFullscreenOpaqueWindowState)) {
            fullscreenDrawsStatusBarBackground = true;
        } else {
            fullscreenDrawsStatusBarBackground = forcesDrawStatusBarBackground(this.mTopFullscreenOpaqueWindowState);
        }
        if (drawsSystemBarBackground(this.mTopDockedOpaqueWindowState)) {
            dockedDrawsStatusBarBackground = true;
        } else {
            dockedDrawsStatusBarBackground = forcesDrawStatusBarBackground(this.mTopDockedOpaqueWindowState);
        }
        boolean statusBarHasFocus = win.getAttrs().type == 2000;
        if (statusBarHasFocus && !isStatusBarKeyguard()) {
            int flags = 14342;
            if (this.mHideLockScreen) {
                flags = -1073727482;
            }
            vis = ((~flags) & vis) | (oldVis & flags);
        }
        if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
            Slog.d(TAG, "updateSystemBarsLw() fullscreenDrawsStatusBarBackground=" + fullscreenDrawsStatusBarBackground + ", dockedDrawsStatusBarBackground=" + dockedDrawsStatusBarBackground + ", vis=" + Integer.toHexString(vis));
        }
        if (fullscreenDrawsStatusBarBackground && dockedDrawsStatusBarBackground) {
            vis = (vis | 8) & -1073741825;
        } else if (!(areTranslucentBarsAllowed() || fullscreenTransWin == this.mStatusBar) || forceOpaqueStatusBar) {
            vis &= -1073741833;
        }
        vis = configureNavBarOpacity(vis, dockedStackVisible, freeformStackVisible, resizing);
        boolean immersiveSticky = (vis & 4096) != 0;
        boolean hideStatusBarWM = this.mTopFullscreenOpaqueWindowState != null ? (PolicyControl.getWindowFlags(this.mTopFullscreenOpaqueWindowState, null) & 1024) != 0 : false;
        boolean hideStatusBarSysui = (vis & 4) != 0;
        boolean hideNavBarSysui = (vis & 2) != 0;
        boolean transientStatusBarAllowed = this.mStatusBar != null ? !statusBarHasFocus ? !this.mForceShowSystemBars ? (hideStatusBarWM || (hideStatusBarSysui && immersiveSticky)) ? true : this.mHideStatusBarWhenLockScreen : false : true : false;
        if (this.mNavigationBar == null || this.mForceShowSystemBars || !hideNavBarSysui) {
            transientNavBarAllowed = false;
        } else {
            transientNavBarAllowed = immersiveSticky;
        }
        boolean pendingPanic = this.mPendingPanicGestureUptime != 0 ? SystemClock.uptimeMillis() - this.mPendingPanicGestureUptime <= PANIC_GESTURE_EXPIRATION : false;
        if (pendingPanic && hideNavBarSysui && !isStatusBarKeyguard() && this.mKeyguardDrawComplete) {
            this.mPendingPanicGestureUptime = 0;
            this.mStatusBarController.showTransient();
            if (!isNavBarEmpty(vis)) {
                this.mNavigationBarController.showTransient();
            }
        }
        if (!this.mStatusBarController.isTransientShowRequested() || transientStatusBarAllowed) {
            denyTransientStatus = false;
        } else {
            denyTransientStatus = hideStatusBarSysui;
        }
        boolean denyTransientNav = this.mNavigationBarController.isTransientShowRequested() ? !transientNavBarAllowed : false;
        if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
            Slog.d(TAG, "updateSystemBarsLw() denyTransientStatus=" + denyTransientStatus + ", denyTransientNav=" + denyTransientNav + ", mForceShowSystemBars=" + this.mForceShowSystemBars);
        }
        if (denyTransientStatus || denyTransientNav || this.mForceShowSystemBars) {
            clearClearableFlagsLw();
            vis &= -8;
        }
        boolean navAllowedHidden = !((vis & 2048) != 0) ? (vis & 4096) != 0 : true;
        if (hideNavBarSysui && !navAllowedHidden && windowTypeToLayerLw(win.getBaseType()) > windowTypeToLayerLw(2022)) {
            vis &= -3;
        }
        vis = this.mStatusBarController.updateVisibilityLw(transientStatusBarAllowed, oldVis, vis);
        if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
            Slog.d(TAG, "updateSystemBarsLw() update status bar vis=" + Integer.toHexString(vis));
        }
        vis = this.mNavigationBarController.updateVisibilityLw(transientNavBarAllowed, oldVis, vis);
        if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
            Slog.d(TAG, "updateSystemBarsLw() update navigation bar vis=" + Integer.toHexString(vis));
        }
        return vis;
    }

    private int configureNavBarOpacity(int visibility, boolean dockedStackVisible, boolean freeformStackVisible, boolean isDockedDividerResizing) {
        if (this.mNavBarOpacityMode == 0) {
            if (dockedStackVisible || freeformStackVisible || isDockedDividerResizing) {
                visibility = setNavBarOpaqueFlag(visibility);
            }
        } else if (this.mNavBarOpacityMode == 1) {
            if (isDockedDividerResizing) {
                visibility = setNavBarOpaqueFlag(visibility);
            } else if (freeformStackVisible) {
                visibility = setNavBarTranslucentFlag(visibility);
            } else {
                visibility = setNavBarOpaqueFlag(visibility);
            }
        }
        if (areTranslucentBarsAllowed()) {
            return visibility;
        }
        return visibility & Integer.MAX_VALUE;
    }

    private int setNavBarOpaqueFlag(int visibility) {
        return visibility & 2147450879;
    }

    private int setNavBarTranslucentFlag(int visibility) {
        return (visibility & -32769) | Integer.MIN_VALUE;
    }

    private void clearClearableFlagsLw() {
        int newVal = this.mResettingSystemUiFlags | 7;
        if (newVal != this.mResettingSystemUiFlags) {
            this.mResettingSystemUiFlags = newVal;
            this.mWindowManagerFuncs.reevaluateStatusBarVisibility();
        }
    }

    private boolean isImmersiveMode(int vis) {
        if (this.mNavigationBar == null || (vis & 2) == 0 || (vis & 6144) == 0) {
            return false;
        }
        return canHideNavigationBar();
    }

    private static boolean isNavBarEmpty(int systemUiFlags) {
        return (systemUiFlags & 23068672) == 23068672;
    }

    private boolean areTranslucentBarsAllowed() {
        return this.mTranslucentDecorEnabled;
    }

    public boolean hasNavigationBar() {
        return this.mHasNavigationBar;
    }

    public void setLastInputMethodWindowLw(WindowState ime, WindowState target) {
        this.mLastInputMethodWindow = ime;
        this.mLastInputMethodTargetWindow = target;
    }

    public int getInputMethodWindowVisibleHeightLw() {
        return this.mDockBottom - this.mCurBottom;
    }

    public void setCurrentUserLw(int newUserId) {
        this.mCurrentUserId = newUserId;
        if (this.mKeyguardDelegate != null) {
            this.mKeyguardDelegate.setCurrentUser(newUserId);
        }
        StatusBarManagerInternal statusBar = getStatusBarManagerInternal();
        if (statusBar != null) {
            statusBar.setCurrentUser(newUserId);
        }
        setLastInputMethodWindowLw(null, null);
    }

    public boolean canMagnifyWindow(int windowType) {
        switch (windowType) {
            case 2011:
            case 2012:
            case 2019:
            case 2027:
                return false;
            default:
                return true;
        }
    }

    public boolean isTopLevelWindow(int windowType) {
        boolean z = true;
        if (windowType < 1000 || windowType > 1999) {
            return true;
        }
        if (windowType != 1003) {
            z = false;
        }
        return z;
    }

    public boolean shouldRotateSeamlessly(int oldRotation, int newRotation) {
        if (oldRotation == this.mUpsideDownRotation || newRotation == this.mUpsideDownRotation) {
            return false;
        }
        int delta = newRotation - oldRotation;
        if (delta < 0) {
            delta += 4;
        }
        if (delta == 2) {
            return false;
        }
        WindowState w = this.mTopFullscreenOpaqueWindowState;
        if (w == this.mFocusedWindow && w != null && !w.isAnimatingLw() && (w.getAttrs().rotationAnimation == 2 || w.getAttrs().rotationAnimation == 3)) {
            return true;
        }
        return false;
    }

    public void dump(String prefix, PrintWriter pw, String[] args) {
        pw.print(prefix);
        pw.print("mIsAlarmBoot=");
        pw.print(this.mIsAlarmBoot);
        pw.print(" mIPOUserRotation=");
        pw.print(this.mIPOUserRotation);
        pw.print(" mIsShutDown=");
        pw.print(this.mIsShutDown);
        pw.print(" mScreenOffReason=");
        pw.print(this.mScreenOffReason);
        pw.print(" mIsAlarmBoot=");
        pw.print(this.mIsAlarmBoot);
        synchronized (this.mKeyDispatchLock) {
            pw.print(" mKeyDispatcMode=");
            pw.println(this.mKeyDispatcMode);
        }
        pw.print(prefix);
        pw.print("mSafeMode=");
        pw.print(this.mSafeMode);
        pw.print(" mSystemReady=");
        pw.print(this.mSystemReady);
        pw.print(" mSystemBooted=");
        pw.println(this.mSystemBooted);
        pw.print(prefix);
        pw.print("mLidState=");
        pw.print(this.mLidState);
        pw.print(" mLidOpenRotation=");
        pw.print(this.mLidOpenRotation);
        pw.print(" mCameraLensCoverState=");
        pw.print(this.mCameraLensCoverState);
        pw.print(" mHdmiPlugged=");
        pw.println(this.mHdmiPlugged);
        if (!(this.mLastSystemUiFlags == 0 && this.mResettingSystemUiFlags == 0 && this.mForceClearedSystemUiFlags == 0)) {
            pw.print(prefix);
            pw.print("mLastSystemUiFlags=0x");
            pw.print(Integer.toHexString(this.mLastSystemUiFlags));
            pw.print(" mResettingSystemUiFlags=0x");
            pw.print(Integer.toHexString(this.mResettingSystemUiFlags));
            pw.print(" mForceClearedSystemUiFlags=0x");
            pw.println(Integer.toHexString(this.mForceClearedSystemUiFlags));
        }
        if (this.mLastFocusNeedsMenu) {
            pw.print(prefix);
            pw.print("mLastFocusNeedsMenu=");
            pw.println(this.mLastFocusNeedsMenu);
        }
        pw.print(prefix);
        pw.print("mWakeGestureEnabledSetting=");
        pw.println(this.mWakeGestureEnabledSetting);
        pw.print(prefix);
        pw.print("mSupportAutoRotation=");
        pw.println(this.mSupportAutoRotation);
        pw.print(prefix);
        pw.print("mUiMode=");
        pw.print(this.mUiMode);
        pw.print(" mDockMode=");
        pw.print(this.mDockMode);
        pw.print(" mEnableCarDockHomeCapture=");
        pw.print(this.mEnableCarDockHomeCapture);
        pw.print(" mCarDockRotation=");
        pw.print(this.mCarDockRotation);
        pw.print(" mDeskDockRotation=");
        pw.println(this.mDeskDockRotation);
        pw.print(prefix);
        pw.print("mUserRotationMode=");
        pw.print(this.mUserRotationMode);
        pw.print(" mUserRotation=");
        pw.print(this.mUserRotation);
        pw.print(" mAllowAllRotations=");
        pw.println(this.mAllowAllRotations);
        pw.print(prefix);
        pw.print("mCurrentAppOrientation=");
        pw.println(this.mCurrentAppOrientation);
        pw.print(prefix);
        pw.print("mCarDockEnablesAccelerometer=");
        pw.print(this.mCarDockEnablesAccelerometer);
        pw.print(" mDeskDockEnablesAccelerometer=");
        pw.println(this.mDeskDockEnablesAccelerometer);
        pw.print(prefix);
        pw.print("mLidKeyboardAccessibility=");
        pw.print(this.mLidKeyboardAccessibility);
        pw.print(" mLidNavigationAccessibility=");
        pw.print(this.mLidNavigationAccessibility);
        pw.print(" mLidControlsScreenLock=");
        pw.println(this.mLidControlsScreenLock);
        pw.print(" mLidControlsSleep=");
        pw.println(this.mLidControlsSleep);
        pw.print(prefix);
        pw.print(" mLongPressOnBackBehavior=");
        pw.println(this.mLongPressOnBackBehavior);
        pw.print(prefix);
        pw.print("mShortPressOnPowerBehavior=");
        pw.print(this.mShortPressOnPowerBehavior);
        pw.print(" mLongPressOnPowerBehavior=");
        pw.println(this.mLongPressOnPowerBehavior);
        pw.print(prefix);
        pw.print("mDoublePressOnPowerBehavior=");
        pw.print(this.mDoublePressOnPowerBehavior);
        pw.print(" mTriplePressOnPowerBehavior=");
        pw.println(this.mTriplePressOnPowerBehavior);
        pw.print(prefix);
        pw.print("mHasSoftInput=");
        pw.println(this.mHasSoftInput);
        pw.print(prefix);
        pw.print("mAwake=");
        pw.println(this.mAwake);
        pw.print(prefix);
        pw.print("mScreenOnEarly=");
        pw.print(this.mScreenOnEarly);
        pw.print(" mScreenOnFully=");
        pw.println(this.mScreenOnFully);
        pw.print(prefix);
        pw.print("mKeyguardDrawComplete=");
        pw.print(this.mKeyguardDrawComplete);
        pw.print(" mWindowManagerDrawComplete=");
        pw.println(this.mWindowManagerDrawComplete);
        pw.print(prefix);
        pw.print("mOrientationSensorEnabled=");
        pw.println(this.mOrientationSensorEnabled);
        pw.print(prefix);
        pw.print("mOverscanScreen=(");
        pw.print(this.mOverscanScreenLeft);
        pw.print(",");
        pw.print(this.mOverscanScreenTop);
        pw.print(") ");
        pw.print(this.mOverscanScreenWidth);
        pw.print("x");
        pw.println(this.mOverscanScreenHeight);
        if (!(this.mOverscanLeft == 0 && this.mOverscanTop == 0 && this.mOverscanRight == 0 && this.mOverscanBottom == 0)) {
            pw.print(prefix);
            pw.print("mOverscan left=");
            pw.print(this.mOverscanLeft);
            pw.print(" top=");
            pw.print(this.mOverscanTop);
            pw.print(" right=");
            pw.print(this.mOverscanRight);
            pw.print(" bottom=");
            pw.println(this.mOverscanBottom);
        }
        pw.print(prefix);
        pw.print("mRestrictedOverscanScreen=(");
        pw.print(this.mRestrictedOverscanScreenLeft);
        pw.print(",");
        pw.print(this.mRestrictedOverscanScreenTop);
        pw.print(") ");
        pw.print(this.mRestrictedOverscanScreenWidth);
        pw.print("x");
        pw.println(this.mRestrictedOverscanScreenHeight);
        pw.print(prefix);
        pw.print("mUnrestrictedScreen=(");
        pw.print(this.mUnrestrictedScreenLeft);
        pw.print(",");
        pw.print(this.mUnrestrictedScreenTop);
        pw.print(") ");
        pw.print(this.mUnrestrictedScreenWidth);
        pw.print("x");
        pw.println(this.mUnrestrictedScreenHeight);
        pw.print(prefix);
        pw.print("mRestrictedScreen=(");
        pw.print(this.mRestrictedScreenLeft);
        pw.print(",");
        pw.print(this.mRestrictedScreenTop);
        pw.print(") ");
        pw.print(this.mRestrictedScreenWidth);
        pw.print("x");
        pw.println(this.mRestrictedScreenHeight);
        pw.print(prefix);
        pw.print("mStableFullscreen=(");
        pw.print(this.mStableFullscreenLeft);
        pw.print(",");
        pw.print(this.mStableFullscreenTop);
        pw.print(")-(");
        pw.print(this.mStableFullscreenRight);
        pw.print(",");
        pw.print(this.mStableFullscreenBottom);
        pw.println(")");
        pw.print(prefix);
        pw.print("mStable=(");
        pw.print(this.mStableLeft);
        pw.print(",");
        pw.print(this.mStableTop);
        pw.print(")-(");
        pw.print(this.mStableRight);
        pw.print(",");
        pw.print(this.mStableBottom);
        pw.println(")");
        pw.print(prefix);
        pw.print("mSystem=(");
        pw.print(this.mSystemLeft);
        pw.print(",");
        pw.print(this.mSystemTop);
        pw.print(")-(");
        pw.print(this.mSystemRight);
        pw.print(",");
        pw.print(this.mSystemBottom);
        pw.println(")");
        pw.print(prefix);
        pw.print("mCur=(");
        pw.print(this.mCurLeft);
        pw.print(",");
        pw.print(this.mCurTop);
        pw.print(")-(");
        pw.print(this.mCurRight);
        pw.print(",");
        pw.print(this.mCurBottom);
        pw.println(")");
        pw.print(prefix);
        pw.print("mContent=(");
        pw.print(this.mContentLeft);
        pw.print(",");
        pw.print(this.mContentTop);
        pw.print(")-(");
        pw.print(this.mContentRight);
        pw.print(",");
        pw.print(this.mContentBottom);
        pw.println(")");
        pw.print(prefix);
        pw.print("mVoiceContent=(");
        pw.print(this.mVoiceContentLeft);
        pw.print(",");
        pw.print(this.mVoiceContentTop);
        pw.print(")-(");
        pw.print(this.mVoiceContentRight);
        pw.print(",");
        pw.print(this.mVoiceContentBottom);
        pw.println(")");
        pw.print(prefix);
        pw.print("mDock=(");
        pw.print(this.mDockLeft);
        pw.print(",");
        pw.print(this.mDockTop);
        pw.print(")-(");
        pw.print(this.mDockRight);
        pw.print(",");
        pw.print(this.mDockBottom);
        pw.println(")");
        pw.print(prefix);
        pw.print("mDockLayer=");
        pw.print(this.mDockLayer);
        pw.print(" mStatusBarLayer=");
        pw.println(this.mStatusBarLayer);
        pw.print(prefix);
        pw.print("mShowingLockscreen=");
        pw.print(this.mShowingLockscreen);
        pw.print(" mShowingDream=");
        pw.print(this.mShowingDream);
        pw.print(" mDreamingLockscreen=");
        pw.print(this.mDreamingLockscreen);
        pw.print(" mDreamingSleepToken=");
        pw.println(this.mDreamingSleepToken);
        if (this.mLastInputMethodWindow != null) {
            pw.print(prefix);
            pw.print("mLastInputMethodWindow=");
            pw.println(this.mLastInputMethodWindow);
        }
        if (this.mLastInputMethodTargetWindow != null) {
            pw.print(prefix);
            pw.print("mLastInputMethodTargetWindow=");
            pw.println(this.mLastInputMethodTargetWindow);
        }
        if (this.mStatusBar != null) {
            pw.print(prefix);
            pw.print("mStatusBar=");
            pw.print(this.mStatusBar);
            pw.print(" isStatusBarKeyguard=");
            pw.println(isStatusBarKeyguard());
        }
        if (this.mNavigationBar != null) {
            pw.print(prefix);
            pw.print("mNavigationBar=");
            pw.println(this.mNavigationBar);
        }
        if (this.mFocusedWindow != null) {
            pw.print(prefix);
            pw.print("mFocusedWindow=");
            pw.println(this.mFocusedWindow);
        }
        if (this.mFocusedApp != null) {
            pw.print(prefix);
            pw.print("mFocusedApp=");
            pw.println(this.mFocusedApp);
        }
        if (this.mWinDismissingKeyguard != null) {
            pw.print(prefix);
            pw.print("mWinDismissingKeyguard=");
            pw.println(this.mWinDismissingKeyguard);
        }
        if (this.mTopFullscreenOpaqueWindowState != null) {
            pw.print(prefix);
            pw.print("mTopFullscreenOpaqueWindowState=");
            pw.println(this.mTopFullscreenOpaqueWindowState);
        }
        if (this.mTopFullscreenOpaqueOrDimmingWindowState != null) {
            pw.print(prefix);
            pw.print("mTopFullscreenOpaqueOrDimmingWindowState=");
            pw.println(this.mTopFullscreenOpaqueOrDimmingWindowState);
        }
        if (this.mForcingShowNavBar) {
            pw.print(prefix);
            pw.print("mForcingShowNavBar=");
            pw.println(this.mForcingShowNavBar);
            pw.print("mForcingShowNavBarLayer=");
            pw.println(this.mForcingShowNavBarLayer);
        }
        pw.print(prefix);
        pw.print("mTopIsFullscreen=");
        pw.print(this.mTopIsFullscreen);
        pw.print(" mHideLockScreen=");
        pw.println(this.mHideLockScreen);
        pw.print(prefix);
        pw.print("mForceStatusBar=");
        pw.print(this.mForceStatusBar);
        pw.print(" mForceStatusBarFromKeyguard=");
        pw.println(this.mForceStatusBarFromKeyguard);
        pw.print(prefix);
        pw.print("mDismissKeyguard=");
        pw.print(this.mDismissKeyguard);
        pw.print(" mCurrentlyDismissingKeyguard=");
        pw.println(this.mCurrentlyDismissingKeyguard);
        pw.print(" mWinDismissingKeyguard=");
        pw.print(this.mWinDismissingKeyguard);
        pw.print(" mHomePressed=");
        pw.println(this.mHomePressed);
        pw.print(prefix);
        pw.print("mAllowLockscreenWhenOn=");
        pw.print(this.mAllowLockscreenWhenOn);
        pw.print(" mLockScreenTimeout=");
        pw.print(this.mLockScreenTimeout);
        pw.print(" mLockScreenTimerActive=");
        pw.println(this.mLockScreenTimerActive);
        pw.print(prefix);
        pw.print("mEndcallBehavior=");
        pw.print(this.mEndcallBehavior);
        pw.print(" mIncallPowerBehavior=");
        pw.print(this.mIncallPowerBehavior);
        pw.print(" mLongPressOnHomeBehavior=");
        pw.println(this.mLongPressOnHomeBehavior);
        pw.print(prefix);
        pw.print("mLandscapeRotation=");
        pw.print(this.mLandscapeRotation);
        pw.print(" mSeascapeRotation=");
        pw.println(this.mSeascapeRotation);
        pw.print(prefix);
        pw.print("mPortraitRotation=");
        pw.print(this.mPortraitRotation);
        pw.print(" mUpsideDownRotation=");
        pw.println(this.mUpsideDownRotation);
        pw.print(prefix);
        pw.print("mDemoHdmiRotation=");
        pw.print(this.mDemoHdmiRotation);
        pw.print(" mDemoHdmiRotationLock=");
        pw.println(this.mDemoHdmiRotationLock);
        pw.print(prefix);
        pw.print("mUndockedHdmiRotation=");
        pw.println(this.mUndockedHdmiRotation);
        this.mGlobalKeyManager.dump(prefix, pw);
        this.mStatusBarController.dump(pw, prefix);
        this.mNavigationBarController.dump(pw, prefix);
        PolicyControl.dump(prefix, pw);
        if (this.mWakeGestureListener != null) {
            this.mWakeGestureListener.dump(pw, prefix);
        }
        if (this.mOrientationListener != null) {
            this.mOrientationListener.dump(pw, prefix);
        }
        if (this.mBurnInProtectionHelper != null) {
            this.mBurnInProtectionHelper.dump(prefix, pw);
        }
        if (this.mKeyguardDelegate != null) {
            this.mKeyguardDelegate.dump(prefix, pw);
        }
    }

    private boolean isAlarmBoot() {
        String bootReason = SystemProperties.get("sys.boot.reason");
        if (bootReason == null || !bootReason.equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON)) {
            return false;
        }
        return true;
    }

    private void ipoSystemBooted() {
        this.mIsAlarmBoot = isAlarmBoot();
        this.mIsShutDown = false;
        this.mHideLockScreen = false;
        this.mScreenshotChordVolumeDownKeyTriggered = false;
        this.mScreenshotChordVolumeUpKeyTriggered = false;
        synchronized (this.mKeyDispatchLock) {
            this.mKeyDispatcMode = 0;
            if (DEBUG_INPUT) {
                Log.v(TAG, "mIpoEventReceiver=" + this.mKeyDispatcMode);
            }
        }
        if (this.mIPOUserRotation != 0) {
            this.mUserRotation = this.mIPOUserRotation;
            this.mIPOUserRotation = 0;
        }
    }

    private void ipoSystemShutdown() {
        synchronized (this.mKeyDispatchLock) {
            this.mKeyDispatcMode = 1;
            if (DEBUG_INPUT) {
                Log.v(TAG, "mIpoEventReceiver=" + this.mKeyDispatcMode);
            }
        }
        if (this.mUserRotationMode == 1 && this.mUserRotation != 0) {
            this.mIPOUserRotation = this.mUserRotation;
            this.mUserRotation = 0;
        }
    }

    private void keyRemappingSendFakeKeyEvent(int action, int keyCode) {
        long eventTime = SystemClock.uptimeMillis();
        if (action == 0) {
            this.mKeyRemappingSendFakeKeyDownTime = eventTime;
        }
        ((InputManager) this.mContext.getSystemService("input")).injectInputEvent(new KeyEvent(this.mKeyRemappingSendFakeKeyDownTime, eventTime, action, keyCode, 0), 0);
    }

    private void interceptDismissPinningChord() {
        IActivityManager activityManager = ActivityManagerNative.asInterface(ServiceManager.checkService("activity"));
        try {
            if (activityManager.isInLockTaskMode()) {
                activityManager.stopLockTaskMode();
            }
        } catch (RemoteException e) {
        }
    }

    /* JADX WARNING: Missing block: B:38:0x0138, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public View addFastStartingWindow(IBinder appToken, String packageName, int theme, CompatibilityInfo compatInfo, CharSequence nonLocalizedLabel, int labelRes, int icon, int logo, int windowFlags, Bitmap bitmap) {
        if (packageName == null) {
            return null;
        }
        WindowManager windowManager = null;
        View view = new View(this.mContext);
        try {
            View view2;
            Context context = this.mContext;
            if (DEBUG_STARTING_WINDOW) {
                Slog.d(TAG, "addFastStartingWindow " + packageName + ": nonLocalizedLabel=" + nonLocalizedLabel + " theme=" + Integer.toHexString(theme));
            }
            LayoutParams params = new LayoutParams(-1, -1);
            params.type = 3;
            params.flags = ((((windowFlags | 16) | 8) | DumpState.DUMP_INTENT_FILTER_VERIFIERS) | Integer.MIN_VALUE) | 256;
            params.windowAnimations = this.mContext.obtainStyledAttributes(R.styleable.Window).getResourceId(8, 0);
            params.token = appToken;
            params.packageName = packageName;
            params.privateFlags |= 1;
            params.privateFlags |= 16;
            if (!compatInfo.supportsScreen()) {
                params.privateFlags |= 128;
            }
            params.setTitle("FastStarting");
            windowManager = (WindowManager) context.getSystemService("window");
            if (DEBUG_STARTING_WINDOW) {
                Slog.d(TAG, "Adding starting window for " + packageName + " / " + appToken + ": " + (view.getParent() != null ? view : null));
            }
            windowManager.addView(view, params);
            if (this.mAppLaunchTimeEnabled) {
                WindowManagerGlobal.getInstance().doTraversal(view, true);
            }
            if (view.getParent() != null) {
                view2 = view;
            } else {
                view2 = null;
            }
            if (view != null && view.getParent() == null) {
                Log.w(TAG, "view not successfully added to wm, removing view");
                windowManager.removeViewImmediate(view);
            }
            return view2;
        } catch (BadTokenException e) {
            Log.w(TAG, appToken + " already running, starting window not displayed. " + e.getMessage());
            if (view != null && view.getParent() == null) {
                Log.w(TAG, "view not successfully added to wm, removing view");
                windowManager.removeViewImmediate(view);
            }
        } catch (RuntimeException e2) {
            Log.w(TAG, appToken + " failed creating starting window", e2);
            if (view != null && view.getParent() == null) {
                Log.w(TAG, "view not successfully added to wm, removing view");
                windowManager.removeViewImmediate(view);
            }
        } catch (Throwable th) {
            if (view != null && view.getParent() == null) {
                Log.w(TAG, "view not successfully added to wm, removing view");
                windowManager.removeViewImmediate(view);
            }
        }
    }

    private boolean interceptKeyBeforeHandling(KeyEvent event) {
        if (this.isUspEnable && 26 == event.getKeyCode() && (SystemProperties.getInt("persist.mtk_usp_cfg_ctrl", 0) & 4) == 4) {
            return true;
        }
        return false;
    }

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "JianHui.Yu@Plf.SDK, 2016-05-20 : Add for Longshot", property = OppoRomType.ROM)
    private boolean canTakeLongshot() {
        if (this.mGlobalActions != null && this.mGlobalActions.isShowing()) {
            return false;
        }
        WindowManager wm = (WindowManager) this.mContext.getSystemService("window");
        if (wm != null) {
            DisplayMetrics dm = new DisplayMetrics();
            wm.getDefaultDisplay().getMetrics(dm);
            if (dm.widthPixels > dm.heightPixels) {
                return false;
            }
        }
        return true;
    }

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "JianHui.Yu@Plf.SDK, 2015-01-06 : Add for Longshot", property = OppoRomType.ROM)
    private void interceptLongshotChord() {
        if (this.mScreenshotChordEnabled && this.mScreenshotChordVolumeUpKeyTriggered && this.mScreenshotChordPowerKeyTriggered && !this.mScreenshotChordVolumeDownKeyTriggered) {
            long now = SystemClock.uptimeMillis();
            if (now <= this.mScreenshotChordVolumeUpKeyTime + SCREENSHOT_CHORD_DEBOUNCE_DELAY_MILLIS && now <= this.mScreenshotChordPowerKeyTime + SCREENSHOT_CHORD_DEBOUNCE_DELAY_MILLIS) {
                this.mScreenshotChordVolumeUpKeyConsumed = true;
                cancelPendingPowerKeyAction();
                if (canTakeLongshot()) {
                    this.mHandler.postDelayed(this.mLongshotRunnable, getScreenshotChordLongPressDelay());
                }
            }
        }
    }

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "JianHui.Yu@Plf.SDK, 2015-01-06 : Add for Longshot", property = OppoRomType.ROM)
    private void cancelPendingLongshotChordAction() {
        this.mHandler.removeCallbacks(this.mLongshotRunnable);
    }

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "JianHui.Yu@Plf.SDK, 2015-01-06 : Add for Longshot", property = OppoRomType.ROM)
    private void takeLongshot() {
        PhoneWindowManager.takeLongshot(this.mContext, this.mStatusBar != null ? this.mStatusBar.isVisibleLw() : false, this.mNavigationBar != null ? this.mNavigationBar.isVisibleLw() : false);
    }

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "JianHui.Yu@Plf.SDK, 2016-05-20 : Add for Longshot", property = OppoRomType.ROM)
    private void handleVolumeUpKeyDownEvent(KeyEvent event) {
        this.mScreenshotChordVolumeUpKeyTime = event.getDownTime();
        this.mScreenshotChordVolumeUpKeyConsumed = false;
    }

    private boolean stopLockTaskMode() {
        try {
            if (!ActivityManagerNative.getDefault().isInLockTaskMode()) {
                return false;
            }
            if (DEBUG_INPUT) {
                Log.d(TAG, "------------stopLockTaskMode  ------------");
            }
            ActivityManagerNative.getDefault().stopLockTaskMode();
            return true;
        } catch (RemoteException e) {
            return false;
        }
    }

    private void toggleSplitScreen() {
        Log.i(TAG, "toggleSplitScreen()");
        StatusBarManagerInternal statusbar = getStatusBarManagerInternal();
        if (statusbar != null) {
            statusbar.toggleSplitScreen();
        }
    }

    private void handleMultiWindowFocusChanged(int state) {
        if (this.mHandler.hasMessages(21)) {
            this.mHandler.removeMessages(21);
        }
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(21, state, 0), 200);
    }

    private void notifyMultiWindowFocusChanged(int state) {
        IStatusBarService sbar = getStatusBarService();
        if (sbar != null) {
            try {
                sbar.notifyMultiWindowFocusChanged(state);
            } catch (RemoteException e) {
            }
        }
    }

    public void notifyMultiWindowModeChanged(boolean isInMultiWindow) {
        if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
            Slog.d(TAG, "notifyMultiWindowModeChanged: isInMultiWindow=" + isInMultiWindow);
        }
        IStatusBarService sbar = getStatusBarService();
        if (sbar != null) {
            try {
                sbar.notifyMultiWindowModeChanged(isInMultiWindow);
            } catch (RemoteException e) {
            }
        }
    }

    private void setNavigationBarColorToStatusBar(int visibility, int color, boolean expandNavBar) {
        this.mLastExpand = expandNavBar;
        this.mLastExpendBarColor = color;
        IStatusBarService sbar = getStatusBarService();
        if (sbar != null) {
            try {
                sbar.setNavigationBarColor(visibility, color, expandNavBar);
            } catch (RemoteException e) {
            }
        }
    }

    public void exitSplitScreen(int state) {
        IStatusBarService sbar = getStatusBarService();
        if (sbar != null) {
            try {
                sbar.exitSplitScreen(state);
            } catch (RemoteException e) {
            }
        }
    }

    private void startHwShutdownDectect() {
        Message msg = this.mHandler.obtainMessage(20);
        msg.setAsynchronous(true);
        this.mHandler.sendMessageDelayed(msg, 3000);
    }

    private void clearHwShutdownDectect() {
        this.mHandler.removeMessages(20);
        HwShutdownRecord.getInstance().cancelHwShutdownFlag();
    }

    public void disableNotificationAlert() {
        StatusBarManager statusBarManager = (StatusBarManager) this.mContext.getSystemService("statusbar");
        statusBarManager.disable(DumpState.DUMP_DOMAIN_PREFERRED);
        statusBarManager.disable(0);
    }

    private boolean interceptPowerKeyForTelephone(KeyEvent event, boolean interactive) {
        boolean hungUp = false;
        TelecomManager telecomManager = getTelecommService();
        if (telecomManager != null) {
            if (telecomManager.isRinging()) {
                boolean inSilence = ((AudioManager) this.mContext.getSystemService("audio")).getStreamVolume(2) == 0;
                boolean isNeverVibrate = System.getInt(this.mContext.getContentResolver(), "call_vibrate_method", 1) == 1;
                Log.d(TAG, "inSilence::" + inSilence + ", isNeverVibrate::" + isNeverVibrate + " isMute=" + this.mIsMute.get());
                if (this.mIsMute.get() || (inSilence && isNeverVibrate)) {
                    hungUp = telecomManager.endCall();
                    this.mIsMute.set(false);
                } else {
                    this.mIsMute.set(true);
                    hungUp = true;
                    telecomManager.silenceRinger();
                }
                this.mContext.sendBroadcast(new Intent("SILENCE_ACTION_FOR_OPPO_SPEECH"));
                if (!interactive) {
                    wakeUpFromPowerKey(event.getDownTime());
                }
            } else if ((this.mIncallPowerBehavior & 2) != 0 && telecomManager.isInCall()) {
                hungUp = telecomManager.endCall();
                this.mIsMute.set(false);
                Log.d(TAG, "interceptPowerKeyForTelephone: telecomManager.endCall()==> " + hungUp);
                this.mContext.sendBroadcast(new Intent("SILENCE_ACTION_FOR_OPPO_SPEECH"));
                if (!interactive) {
                    wakeUpFromPowerKey(event.getDownTime());
                }
            }
        }
        return hungUp;
    }

    private boolean isLeatherModeEnabled(Context context) {
        boolean leatherEnable = false;
        try {
            leatherEnable = System.getInt(context.getContentResolver(), "oppo_leather_mode_enabled") == 1;
        } catch (SettingNotFoundException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "isLeatherModeEnabled: leatherEnable = " + leatherEnable);
        return leatherEnable;
    }

    private void adjustOppoWindowFrame(Rect pf, Rect df, Rect of, Rect cf, Rect dcf, LayoutParams attrs) {
        if (canHideNavigationBar() && ColorWindowManager.LayoutParams.isForceFullScreen(attrs.type)) {
            int i = this.mOverscanScreenLeft;
            dcf.left = i;
            cf.left = i;
            of.left = i;
            df.left = i;
            pf.left = i;
            i = this.mOverscanScreenTop;
            dcf.top = i;
            cf.top = i;
            of.top = i;
            df.top = i;
            pf.top = i;
            i = this.mOverscanScreenLeft + this.mOverscanScreenWidth;
            dcf.right = i;
            cf.right = i;
            of.right = i;
            df.right = i;
            pf.right = i;
            i = this.mOverscanScreenTop + this.mOverscanScreenHeight;
            dcf.bottom = i;
            cf.bottom = i;
            of.bottom = i;
            df.bottom = i;
            pf.bottom = i;
        }
    }

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "Peng.Jin@ROM.SysApp.ScreenOffGestrue, 2016/10/09, Add for gesture service show gesture anim.", property = OppoRomType.ROM)
    public void handleScreenTurningOn() {
    }

    public boolean isKeyguardShown() {
        return !this.mKeyguardHidden;
    }

    public int getNavigationBarStatus() {
        return this.mNavigationBarEnableStatus;
    }

    public void transientNavigatioinBar() {
        if (this.mNavigationBar != null) {
            requestTransientBars(this.mNavigationBar);
        }
    }
}
