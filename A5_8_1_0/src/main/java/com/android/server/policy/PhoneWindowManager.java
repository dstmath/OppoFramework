package com.android.server.policy;

import android.annotation.OppoHook;
import android.annotation.OppoHook.OppoHookType;
import android.annotation.OppoHook.OppoRomType;
import android.app.ActivityManager;
import android.app.ActivityManager.StackId;
import android.app.ActivityManagerInternal;
import android.app.ActivityManagerInternal.SleepToken;
import android.app.ActivityManagerNative;
import android.app.ActivityThread;
import android.app.AlertDialog;
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
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.content.res.TypedArray;
import android.database.ContentObserver;
import android.engineer.OppoEngineerManager;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.hardware.display.DisplayManager;
import android.hardware.display.DisplayManagerInternal;
import android.hardware.hdmi.HdmiControlManager;
import android.hardware.hdmi.HdmiPlaybackClient;
import android.hardware.hdmi.HdmiPlaybackClient.OneTouchPlayCallback;
import android.hardware.input.InputManager;
import android.hardware.input.InputManagerInternal;
import android.media.AudioAttributes;
import android.media.AudioAttributes.Builder;
import android.media.AudioManager;
import android.media.AudioSystem;
import android.media.IAudioService;
import android.media.session.MediaSessionLegacyHelper;
import android.net.arp.OppoArpPeer;
import android.net.dhcp.DhcpPacket;
import android.os.Binder;
import android.os.Bundle;
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
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.service.dreams.DreamManagerInternal;
import android.service.dreams.IDreamManager;
import android.service.vr.IPersistentVrStateCallbacks;
import android.telecom.TelecomManager;
import android.util.BoostFramework;
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
import android.view.KeyCharacterMap;
import android.view.KeyCharacterMap.FallbackAction;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.BadTokenException;
import android.view.WindowManagerInternal;
import android.view.WindowManagerInternal.AppTransitionListener;
import android.view.WindowManagerPolicy;
import android.view.WindowManagerPolicy.InputConsumer;
import android.view.WindowManagerPolicy.OnKeyguardExitResult;
import android.view.WindowManagerPolicy.ScreenOffListener;
import android.view.WindowManagerPolicy.ScreenOnListener;
import android.view.WindowManagerPolicy.StartingSurface;
import android.view.WindowManagerPolicy.WindowManagerFuncs;
import android.view.WindowManagerPolicy.WindowState;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.autofill.AutofillManagerInternal;
import android.view.inputmethod.InputMethodManagerInternal;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import com.android.internal.R;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.policy.IKeyguardDismissCallback;
import com.android.internal.policy.IShortcutService;
import com.android.internal.policy.PhoneWindow;
import com.android.internal.statusbar.IStatusBarService;
import com.android.internal.util.ScreenShapeHelper;
import com.android.internal.widget.PointerLocationView;
import com.android.server.LocalServices;
import com.android.server.LocationManagerService;
import com.android.server.ServiceThread;
import com.android.server.SystemServiceManager;
import com.android.server.am.OppoAppStartupManager;
import com.android.server.am.OppoProcessManager;
import com.android.server.display.OppoBrightUtils;
import com.android.server.fingerprint.power.FingerprintInternal;
import com.android.server.fingerprint.util.SupportUtil;
import com.android.server.job.controllers.JobStatus;
import com.android.server.oppo.ScreenOnCpuBoostHelper;
import com.android.server.policy.keyguard.KeyguardServiceDelegate;
import com.android.server.policy.keyguard.KeyguardServiceDelegate.DrawnListener;
import com.android.server.policy.keyguard.KeyguardStateMonitor.StateCallback;
import com.android.server.statusbar.StatusBarManagerInternal;
import com.android.server.usb.UsbAudioDevice;
import com.android.server.usb.descriptors.UsbACInterface;
import com.android.server.usb.descriptors.UsbTerminalTypes;
import com.android.server.vr.VrManagerInternal;
import com.android.server.wm.AppTransition;
import com.android.server.wm.WindowManagerDebugConfig;
import com.color.util.ColorDisplayCompatUtils;
import com.color.view.ColorWindowManager;
import com.color.widget.ColorSagView;
import com.oppo.debug.InputLog;
import com.oppo.debug.ProcessCpuTrackerRunnable;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class PhoneWindowManager implements WindowManagerPolicy {
    private static final String ACTION_DISABLE_LIDCONTROLSSLEEP = "oppo.intent.action.DISABLE_LIDCONTROLSSLEEP";
    private static final String ACTION_ENABLE_LIDCONTROLSSLEEP = "oppo.intent.action.ENABLE_LIDCONTROLSSLEEP";
    private static final String ACTION_WIFI_DISPLAY_VIDEO = "org.codeaurora.intent.action.WIFI_DISPLAY_VIDEO";
    static final boolean ALTERNATE_CAR_MODE_NAV_SIZE = false;
    private static final int BRIGHTNESS_STEPS = 10;
    private static final long BUGREPORT_TV_GESTURE_TIMEOUT_MILLIS = 1000;
    static final boolean DEBUG = false;
    static final boolean DEBUG_INPUT = false;
    static final boolean DEBUG_KEYGUARD = false;
    static final boolean DEBUG_LAYOUT = false;
    static boolean DEBUG_ORIENT = false;
    public static boolean DEBUG_PANIC = false;
    static final boolean DEBUG_SPLASH_SCREEN = false;
    static boolean DEBUG_WAKEUP = false;
    static final int DEFAULT_LONG_PRESS_POWERON_DISPLAY_TIME = 2500;
    static final int DOUBLE_TAP_HOME_NOTHING = 0;
    static final int DOUBLE_TAP_HOME_RECENT_SYSTEM_UI = 1;
    private static final int DURATION_START_SPLIT_SCREEN = 750;
    static final boolean ENABLE_DESK_DOCK_HOME_CAPTURE = false;
    static final boolean ENABLE_VR_HEADSET_HOME_CAPTURE = true;
    private static final String FULL_WINDOW = "fullscreen";
    private static final int HARDWARE_RESET_RECORD_FLAG_INDEX = 77;
    private static final String HIDE_NAVIGATIONBAR_ENABLE = "hide_navigationbar_enable";
    private static final float KEYGUARD_SCREENSHOT_CHORD_DELAY_MULTIPLIER = 2.5f;
    static final int LAST_LONG_PRESS_HOME_BEHAVIOR = 2;
    static final int LONG_PRESS_BACK_GO_TO_VOICE_ASSIST = 1;
    static final int LONG_PRESS_BACK_NOTHING = 0;
    static final int LONG_PRESS_HOME_ALL_APPS = 1;
    static final int LONG_PRESS_HOME_ASSIST = 2;
    static final int LONG_PRESS_HOME_NOTHING = 0;
    static final int LONG_PRESS_POWER_GLOBAL_ACTIONS = 1;
    static final int LONG_PRESS_POWER_NOTHING = 0;
    static final int LONG_PRESS_POWER_SHUT_OFF = 2;
    static final int LONG_PRESS_POWER_SHUT_OFF_NO_CONFIRM = 3;
    public static final int MODE_HOVER_NAVIGATIONBAR_HIDE = 1;
    public static final int MODE_HOVER_NAVIGATIONBAR_SHOW_PLACEHOLDER = 2;
    public static final int MODE_HOVER_NAVIGATIONBAR_SHOW_SUSPENDED = 0;
    public static final int MODE_NAVIGATIONBAR_GESTURE = 2;
    private static final int MSG_ACCESSIBILITY_SHORTCUT = 21;
    private static final int MSG_ACCESSIBILITY_TV = 23;
    private static final int MSG_BACK_DELAYED_PRESS = 20;
    private static final int MSG_BACK_LONG_PRESS = 18;
    private static final int MSG_BUGREPORT_TV = 22;
    private static final int MSG_DISABLE_POINTER_LOCATION = 2;
    private static final int MSG_DISPATCH_BACK_KEY_TO_AUTOFILL = 24;
    private static final int MSG_DISPATCH_MEDIA_KEY_REPEAT_WITH_WAKE_LOCK = 4;
    private static final int MSG_DISPATCH_MEDIA_KEY_WITH_WAKE_LOCK = 3;
    private static final int MSG_DISPATCH_SHOW_GLOBAL_ACTIONS = 10;
    private static final int MSG_DISPATCH_SHOW_RECENTS = 9;
    private static final int MSG_DISPOSE_INPUT_CONSUMER = 19;
    private static final int MSG_ENABLE_POINTER_LOCATION = 1;
    private static final int MSG_HANDLE_ALL_APPS = 26;
    private static final int MSG_HIDE_BOOT_MESSAGE = 11;
    private static final int MSG_KEYGUARD_DRAWN_COMPLETE = 5;
    private static final int MSG_KEYGUARD_DRAWN_TIMEOUT = 6;
    private static final int MSG_LAUNCH_VOICE_ASSIST_WITH_WAKE_LOCK = 12;
    private static final int MSG_MULTI_WINDOW_FOCUS_CHANGED = 27;
    private static final int MSG_OPPO_BASE = 1000;
    private static final int MSG_OPPO_RELAUNCHER_DISPLAY_FULLSCREEN = 10111;
    private static final int MSG_POWER_DELAYED_PRESS = 13;
    private static final int MSG_POWER_KEY_WAKE_LOCK_TIME_OUT = 255;
    private static final int MSG_POWER_LONG_PRESS = 14;
    private static final int MSG_POWER_LONG_PRESS_HARDWARE_SHUTDOWN = 1001;
    private static final int MSG_REQUEST_TRANSIENT_BARS = 16;
    private static final int MSG_REQUEST_TRANSIENT_BARS_ARG_NAVIGATION = 1;
    private static final int MSG_REQUEST_TRANSIENT_BARS_ARG_STATUS = 0;
    private static final int MSG_SHOW_PICTURE_IN_PICTURE_MENU = 17;
    private static final int MSG_SYSTEM_KEY_PRESS = 25;
    private static final int MSG_UPDATE_DREAMING_SLEEP_TOKEN = 15;
    private static final int MSG_WINDOW_MANAGER_DRAWN_COMPLETE = 7;
    static final int MULTI_PRESS_POWER_BRIGHTNESS_BOOST = 2;
    static final int MULTI_PRESS_POWER_NOTHING = 0;
    static final int MULTI_PRESS_POWER_THEATER_MODE = 1;
    static final int NAV_BAR_OPAQUE_WHEN_FREEFORM_OR_DOCKED = 0;
    static final int NAV_BAR_TRANSLUCENT_WHEN_FREEFORM_OPAQUE_OTHERWISE = 1;
    private static final long PANIC_GESTURE_EXPIRATION = 30000;
    static final int PANIC_PRESS_BACK_COUNT = 4;
    static final int PANIC_PRESS_BACK_HOME = 1;
    static final int PANIC_PRESS_BACK_NOTHING = 0;
    static final int PENDING_KEY_NULL = -1;
    private static final long POWER_KEY_WAKE_LOCK_TIME_OUT = 120000;
    static final boolean PRINT_ANIM = false;
    private static final long SCREENSHOT_CHORD_DEBOUNCE_DELAY_MILLIS = 150;
    private static final String SETTINGS_HOVER_NAVIGATIONBAR_ENABLE_STRING = "hide_gesture_bar_enable";
    private static final String SHORTCUT_WINDOW = "ShortcutsPanel";
    static final int SHORT_PRESS_POWER_CLOSE_IME_OR_GO_HOME = 5;
    static final int SHORT_PRESS_POWER_GO_HOME = 4;
    static final int SHORT_PRESS_POWER_GO_TO_SLEEP = 1;
    static final int SHORT_PRESS_POWER_NOTHING = 0;
    static final int SHORT_PRESS_POWER_REALLY_GO_TO_SLEEP = 2;
    static final int SHORT_PRESS_POWER_REALLY_GO_TO_SLEEP_AND_GO_HOME = 3;
    static final int SHORT_PRESS_SLEEP_GO_TO_SLEEP = 0;
    static final int SHORT_PRESS_SLEEP_GO_TO_SLEEP_AND_GO_HOME = 1;
    static final int SHORT_PRESS_WINDOW_NOTHING = 0;
    static final int SHORT_PRESS_WINDOW_PICTURE_IN_PICTURE = 1;
    static final boolean SHOW_SPLASH_SCREENS = true;
    public static final String SYSTEM_DIALOG_REASON_ASSIST = "assist";
    public static final String SYSTEM_DIALOG_REASON_GLOBAL_ACTIONS = "globalactions";
    public static final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
    public static final String SYSTEM_DIALOG_REASON_KEY = "reason";
    public static final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";
    public static final String SYSTEM_DIALOG_REASON_SCREENSHOT = "screenshot";
    static final int SYSTEM_UI_CHANGING_LAYOUT = -1073709042;
    public static final int SYSTEM_UI_FLAG_APP_CUSTOM_NAVIGATION_BAR = 33554432;
    public static final int SYSTEM_UI_FLAG_APP_LIGHT_NAVIGATION_BAR = 128;
    public static final int SYSTEM_UI_FLAG_FOCUS_TOP_OR_LEFT = 64;
    public static final int SYSTEM_UI_FLAG_OPPO_NAVIGATION_BAR_IME_TOP = 16384;
    public static final int SYSTEM_UI_FLAG_OPPO_NAVIGATION_BAR_SHOW_IME = 32768;
    public static final int SYSTEM_UI_FLAG_OPPO_STATUS_BAR_TOP = 8192;
    private static final String SYSUI_PACKAGE = "com.android.systemui";
    private static final String SYSUI_SCREENSHOT_ERROR_RECEIVER = "com.android.systemui.screenshot.ScreenshotServiceErrorReceiver";
    private static final String SYSUI_SCREENSHOT_SERVICE = "com.android.systemui.screenshot.TakeScreenshotService";
    static final String TAG = "WindowManager";
    public static final int TOAST_WINDOW_TIMEOUT = 3500;
    private static final AudioAttributes VIBRATION_ATTRIBUTES = new Builder().setContentType(4).setUsage(13).build();
    static final int WAITING_FOR_DRAWN_TIMEOUT = 1000;
    private static final int[] WINDOW_TYPES_WHERE_HOME_DOESNT_WORK = new int[]{2003, 2010};
    static final boolean localLOGV = false;
    private static List<String> mAutoHideApp = new ArrayList();
    private static final String[] mAutoHideVideoPackageName = new String[]{"com.baidu.video", "com.telecom.video.ikan4g", "com.ss.android.article.video", "com.duowan.kiwi", "com.miui.video", "cn.cntv", "com.smile.gifmaker", "com.ss.android.ugc.live", "com.meelive.ingkee", "com.huajiao", "tv.pps.mobile", "com.ss.android.ugc.aweme", "com.qihoo.video", "com.molitv.android", "com.le123.ysdq"};
    private static ColorDisplayCompatUtils mDisplayCompatUtils = null;
    static final Rect mTmpContentFrame = new Rect();
    static final Rect mTmpDecorFrame = new Rect();
    static final Rect mTmpDisplayFrame = new Rect();
    static final Rect mTmpNavigationFrame = new Rect();
    static final Rect mTmpNavigationFrameForGesture = new Rect();
    static final Rect mTmpOutsetFrame = new Rect();
    static final Rect mTmpOverscanFrame = new Rect();
    static final Rect mTmpParentFrame = new Rect();
    private static final Rect mTmpRect = new Rect();
    static final Rect mTmpStableFrame = new Rect();
    static final Rect mTmpVisibleFrame = new Rect();
    static SparseArray<String> sApplicationLaunchKeyCategories = new SparseArray();
    boolean isTouchFingerPrintSensor = false;
    private boolean mA11yShortcutChordVolumeUpKeyConsumed;
    private long mA11yShortcutChordVolumeUpKeyTime;
    private boolean mA11yShortcutChordVolumeUpKeyTriggered;
    boolean mAccelerometerDefault;
    AccessibilityManager mAccessibilityManager;
    private AccessibilityShortcutController mAccessibilityShortcutController;
    private boolean mAccessibilityTvKey1Pressed;
    private boolean mAccessibilityTvKey2Pressed;
    private boolean mAccessibilityTvScheduled;
    private final Runnable mAcquireSleepTokenRunnable = new -$Lambda$pV_TcBBXJOcgD8CpVRVZuDc_ff8((byte) 1, this);
    IActivityManager mActivityManager;
    ActivityManagerInternal mActivityManagerInternal;
    int mAllowAllRotations = -1;
    boolean mAllowLockscreenWhenOn;
    private boolean mAllowTheaterModeWakeFromCameraLens;
    private boolean mAllowTheaterModeWakeFromKey;
    private boolean mAllowTheaterModeWakeFromLidSwitch;
    private boolean mAllowTheaterModeWakeFromMotion;
    private boolean mAllowTheaterModeWakeFromMotionWhenNotDreaming;
    private boolean mAllowTheaterModeWakeFromPowerKey;
    private boolean mAllowTheaterModeWakeFromWakeGesture;
    AppOpsManager mAppOpsManager;
    boolean mAssistKeyLongPressed;
    AutofillManagerInternal mAutofillManagerInternal;
    volatile boolean mAwake;
    volatile boolean mBackKeyHandled;
    volatile int mBackKeyPressCounter;
    volatile boolean mBeganFromNonInteractive;
    boolean mBootMessageNeedsHiding;
    ProgressDialog mBootMsgDialog = null;
    WakeLock mBroadcastWakeLock;
    private boolean mBugreportTvKey1Pressed;
    private boolean mBugreportTvKey2Pressed;
    private boolean mBugreportTvScheduled;
    BurnInProtectionHelper mBurnInProtectionHelper;
    long[] mCalendarDateVibePattern;
    volatile boolean mCameraGestureTriggeredDuringGoingToSleep;
    int mCameraLensCoverState = -1;
    boolean mCarDockEnablesAccelerometer;
    Intent mCarDockIntent;
    int mCarDockRotation;
    private final Runnable mClearHideNavigationFlag = new Runnable() {
        public void run() {
            synchronized (PhoneWindowManager.this.mWindowManagerFuncs.getWindowManagerLock()) {
                PhoneWindowManager phoneWindowManager = PhoneWindowManager.this;
                phoneWindowManager.mForceClearedSystemUiFlags &= -3;
            }
            PhoneWindowManager.this.mWindowManagerFuncs.reevaluateStatusBarVisibility();
        }
    };
    WindowState mColorFullScreenDisplay = null;
    WindowState mColorFullScreenDisplayLand = null;
    private View mColorFullScreenWindow = null;
    private View mColorFullScreenWindowLand = null;
    WindowState mColorOSRVB = null;
    WindowState mColorOSRVL = null;
    WindowState mColorOSRVR = null;
    WindowState mColorOSRVT = null;
    private View mColorSagAreaView = null;
    WindowState mColorSagAreaWindow = null;
    boolean mConsumeSearchKeyUp;
    int mContentBottom;
    int mContentLeft;
    int mContentRight;
    int mContentTop;
    Context mContext;
    int mCurBottom;
    int mCurLeft;
    int mCurRight;
    int mCurTop;
    int mCurrentAppOrientation = -1;
    private int mCurrentUserId;
    int mDemoHdmiRotation;
    boolean mDemoHdmiRotationLock;
    int mDemoRotation;
    boolean mDemoRotationLock;
    boolean mDeskDockEnablesAccelerometer;
    Intent mDeskDockIntent;
    int mDeskDockRotation;
    private volatile boolean mDismissImeOnBackKeyPressed;
    Display mDisplay;
    DisplayManagerInternal mDisplayManagerInternal;
    private int mDisplayRotation;
    int mDockBottom;
    int mDockLayer;
    int mDockLeft;
    int mDockMode = 0;
    BroadcastReceiver mDockReceiver = new BroadcastReceiver() {
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
    int mDockRight;
    int mDockTop;
    final Rect mDockedStackBounds = new Rect();
    int mDoublePressOnPowerBehavior;
    private int mDoubleTapOnHomeBehavior;
    DreamManagerInternal mDreamManagerInternal;
    BroadcastReceiver mDreamReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.DREAMING_STARTED".equals(intent.getAction())) {
                if (PhoneWindowManager.this.mKeyguardDelegate != null) {
                    PhoneWindowManager.this.mKeyguardDelegate.onDreamingStarted();
                }
            } else if ("android.intent.action.DREAMING_STOPPED".equals(intent.getAction()) && PhoneWindowManager.this.mKeyguardDelegate != null) {
                PhoneWindowManager.this.mKeyguardDelegate.onDreamingStopped();
            }
        }
    };
    boolean mDreamingLockscreen;
    SleepToken mDreamingSleepToken;
    boolean mDreamingSleepTokenNeeded;
    private boolean mEnableCarDockHomeCapture = true;
    boolean mEnableShiftMenuBugReports = false;
    volatile boolean mEndCallKeyHandled;
    private final Runnable mEndCallLongPress = new Runnable() {
        public void run() {
            PhoneWindowManager.this.mEndCallKeyHandled = true;
            PhoneWindowManager.this.performHapticFeedbackLw(null, 0, false);
            PhoneWindowManager.this.showGlobalActionsInternal();
        }
    };
    int mEndcallBehavior;
    BroadcastReceiver mEngineerModeHandleReceiver = new BroadcastReceiver() {
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
    private boolean mExpendBar;
    private UEventObserver mExtEventObserver = new UEventObserver() {
        public void onUEvent(UEvent event) {
            if (event.get("status") != null) {
                PhoneWindowManager.this.setHdmiPlugged("connected".equals(event.get("status")));
            }
        }
    };
    private final SparseArray<FallbackAction> mFallbackActions = new SparseArray();
    FingerprintInternal mFingerprintInternal;
    IApplicationToken mFocusedApp;
    WindowState mFocusedWindow;
    int mForceClearedSystemUiFlags = 0;
    private boolean mForceDefaultOrientation = false;
    boolean mForceShowSystemBars;
    boolean mForceStatusBar;
    boolean mForceStatusBarFromKeyguard;
    private boolean mForceStatusBarTransparent;
    boolean mForcingShowNavBar;
    int mForcingShowNavBarLayer;
    private String mFoucsPackage = null;
    private AlertDialog mFullscreenDialog;
    private AlertDialog mFullscreenDialogLand;
    BroadcastReceiver mFullscreenDialogReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.CLOSE_SYSTEM_DIALOGS".equals(intent.getAction())) {
                if (PhoneWindowManager.this.mFullscreenDialogLand != null) {
                    PhoneWindowManager.this.mFullscreenDialogLand.dismiss();
                    PhoneWindowManager.this.mFullscreenDialogLand = null;
                }
                if (PhoneWindowManager.this.mFullscreenDialog != null) {
                    PhoneWindowManager.this.mFullscreenDialog.dismiss();
                    PhoneWindowManager.this.mFullscreenDialog = null;
                }
            }
        }
    };
    private boolean mGestureAnimSupport = false;
    @OppoHook(level = OppoHookType.CHANGE_BASE_CLASS, note = "Xiaokang.Feng@ROM.SDK, modify for Oppo global actions", property = OppoRomType.ROM)
    OppoGlobalActions mGlobalActions;
    private GlobalKeyManager mGlobalKeyManager;
    private boolean mGoToSleepOnButtonPressTheaterMode;
    volatile boolean mGoingToSleep;
    private UEventObserver mHDMIObserver = new UEventObserver() {
        public void onUEvent(UEvent event) {
            PhoneWindowManager.this.setHdmiPlugged(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(event.get("SWITCH_STATE")));
        }
    };
    private int mHHHRotation = 0;
    private boolean mHandleVolumeKeysInWM;
    Handler mHandler;
    private boolean mHasFeatureLeanback;
    private boolean mHasFeatureWatch;
    private boolean mHasHeteromorphismFeature = false;
    boolean mHasNavigationBar = false;
    boolean mHasSoftInput = false;
    boolean mHaveBuiltInKeyboard;
    boolean mHavePendingMediaKeyRepeatWithWakeLock;
    private View mHchildLeft = null;
    private View mHchildRight = null;
    HdmiControl mHdmiControl;
    boolean mHdmiPlugged;
    private final Runnable mHiddenNavPanic = new Runnable() {
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
    boolean mHideNavigationBar = false;
    boolean mHideStatusBarWhenLockScreen = false;
    boolean mHomeConsumed;
    boolean mHomeDoubleTapPending;
    private final Runnable mHomeDoubleTapTimeoutRunnable = new Runnable() {
        public void run() {
            if (PhoneWindowManager.this.mHomeDoubleTapPending) {
                PhoneWindowManager.this.mHomeDoubleTapPending = false;
                PhoneWindowManager.this.handleShortPressOnHome();
            }
        }
    };
    Intent mHomeIntent;
    boolean mHomePressed;
    private int mHoverNavigationBarStatus = 0;
    private int mHwShutdownCount = -1;
    private ImmersiveModeConfirmation mImmersiveModeConfirmation;
    int mIncallBackBehavior;
    int mIncallPowerBehavior;
    int mInitialMetaState;
    InputConsumer mInputConsumer = null;
    InputManagerInternal mInputManagerInternal;
    InputMethodManagerInternal mInputMethodManagerInternal;
    boolean mIsEnableKeyguardHide = false;
    public AtomicBoolean mIsMute = new AtomicBoolean(false);
    private boolean mIsVisibility = false;
    private boolean mKeyguardBound;
    KeyguardServiceDelegate mKeyguardDelegate;
    boolean mKeyguardDrawComplete;
    final DrawnListener mKeyguardDrawnCallback = new DrawnListener() {
        public void onDrawn() {
            if (PhoneWindowManager.DEBUG_WAKEUP) {
                Slog.d(PhoneWindowManager.TAG, "mKeyguardDelegate.ShowListener.onDrawn.");
            }
            PhoneWindowManager.this.mPhoneWinHandler.sendEmptyMessage(5);
        }
    };
    private boolean mKeyguardDrawnOnce;
    volatile boolean mKeyguardOccluded;
    private boolean mKeyguardOccludedChanged;
    int mLandscapeRotation = 0;
    boolean mLanguageSwitchKeyPressed;
    private int mLastDisplayRotation;
    private boolean mLastDockedDrawsStatusBarBackground;
    final Rect mLastDockedStackBounds = new Rect();
    int mLastDockedStackSysUiFlags;
    private boolean mLastExpand = this.mExpendBar;
    private int mLastExpendBarColor;
    boolean mLastFocusNeedsMenu = false;
    private boolean mLastFullscreenDrawsStatusBarBackground;
    int mLastFullscreenStackSysUiFlags;
    private long mLastHomeDownTimeDuringPF = 0;
    WindowState mLastInputMethodTargetWindow = null;
    WindowState mLastInputMethodWindow = null;
    private int mLastNavVisibility = 0;
    private int mLastNavigationBarColor = 0;
    private int mLastNavigationBarState = 0;
    final Rect mLastNonDockedStackBounds = new Rect();
    private boolean mLastShowingDream;
    int mLastSystemUiFlags;
    private int mLastValidNavigationBarTint = 0;
    private int mLastValidStatusBarTint = 0;
    int mLastWindowFocusFlags;
    private boolean mLastWindowSleepTokenNeeded;
    private LayoutParams mLayoutParams = null;
    boolean mLidControlsScreenLock;
    boolean mLidControlsSleep;
    int mLidKeyboardAccessibility;
    int mLidNavigationAccessibility;
    int mLidOpenRotation;
    int mLidState = -1;
    private final Object mLock = new Object();
    int mLockScreenTimeout;
    boolean mLockScreenTimerActive;
    private final LogDecelerateInterpolator mLogDecelerateInterpolator = new LogDecelerateInterpolator(100, 0);
    int mLongPressOnBackBehavior;
    private int mLongPressOnHomeBehavior;
    int mLongPressOnPowerBehavior;
    long[] mLongPressVibePattern;
    int mMetaState;
    BroadcastReceiver mMultiuserReceiver = new BroadcastReceiver() {
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
    private int mNaturalHeight;
    private int mNaturalWidth;
    int mNavBarOpacityMode = 0;
    private final OnBarVisibilityChangedListener mNavBarVisibilityListener = new OnBarVisibilityChangedListener() {
        public void onBarVisibilityChanged(boolean visible) {
            PhoneWindowManager.this.mAccessibilityManager.notifyAccessibilityButtonVisibilityChanged(visible);
        }
    };
    WindowState mNavigationBar = null;
    boolean mNavigationBarCanMove = false;
    private final BarController mNavigationBarController = new BarController("NavigationBar", 134217728, 536870912, Integer.MIN_VALUE, 2, 134217728, 32768);
    private int mNavigationBarEnableStatus = 0;
    int[] mNavigationBarHeightForRotationDefault = new int[4];
    int[] mNavigationBarHeightForRotationGestrue = new int[4];
    int[] mNavigationBarHeightForRotationInCarMode = new int[4];
    int mNavigationBarPosition = 4;
    int[] mNavigationBarWidthForRotationDefault = new int[4];
    int[] mNavigationBarWidthForRotationInCarMode = new int[4];
    final Rect mNonDockedStackBounds = new Rect();
    MyOrientationListener mOrientationListener;
    boolean mOrientationSensorEnabled = false;
    int mOverscanBottom = 0;
    int mOverscanLeft = 0;
    int mOverscanRight = 0;
    int mOverscanScreenHeight;
    int mOverscanScreenLeft;
    int mOverscanScreenTop;
    int mOverscanScreenWidth;
    int mOverscanTop = 0;
    int mPanicPressOnBackBehavior;
    boolean mPendingCapsLockToggle;
    private boolean mPendingKeyguardOccluded;
    boolean mPendingMetaAction;
    private long mPendingPanicGestureUptime;
    volatile int mPendingWakeKey = -1;
    BoostFramework mPerfBoost = null;
    private volatile boolean mPersistentVrModeEnabled;
    final IPersistentVrStateCallbacks mPersistentVrModeListener = new IPersistentVrStateCallbacks.Stub() {
        public void onPersistentVrStateChanged(boolean enabled) {
            PhoneWindowManager.this.mPersistentVrModeEnabled = enabled;
        }
    };
    private PhoneWinHandler mPhoneWinHandler;
    private ServiceThread mPhoneWinHandlerThread;
    volatile boolean mPictureInPictureVisible;
    private PowerManager mPm = null;
    int mPointerLocationMode = 0;
    PointerLocationView mPointerLocationView;
    int mPortraitRotation = 0;
    volatile boolean mPowerKeyHandled;
    volatile int mPowerKeyPressCounter;
    WakeLock mPowerKeyWakeLock;
    PowerManager mPowerManager;
    PowerManagerInternal mPowerManagerInternal;
    boolean mPreloadedRecentApps;
    private ProcessCpuTrackerRunnable mProcessCpuTrackerRunnable = new ProcessCpuTrackerRunnable();
    int mRecentAppsHeldModifiers;
    private AtomicBoolean mRecentsLongPressDetected = new AtomicBoolean(false);
    private final Runnable mRecentsStartSplitSreen = new Runnable() {
        public void run() {
            if (!PhoneWindowManager.this.keyguardOn()) {
                PhoneWindowManager.this.mRecentsLongPressDetected.set(true);
                if (!PhoneWindowManager.this.stopLockTaskMode()) {
                    PhoneWindowManager.this.toggleSplitScreen();
                }
            }
        }
    };
    volatile boolean mRecentsVisible;
    private final Runnable mReleaseSleepTokenRunnable = new -$Lambda$pV_TcBBXJOcgD8CpVRVZuDc_ff8((byte) 2, this);
    volatile boolean mRequestedOrGoingToSleep;
    int mResettingSystemUiFlags = 0;
    int mRestrictedOverscanScreenHeight;
    int mRestrictedOverscanScreenLeft;
    int mRestrictedOverscanScreenTop;
    int mRestrictedOverscanScreenWidth;
    int mRestrictedScreenHeight;
    int mRestrictedScreenLeft;
    int mRestrictedScreenTop;
    int mRestrictedScreenWidth;
    private int mRotation = 0;
    boolean mSafeMode;
    long[] mSafeModeEnabledVibePattern;
    WindowState mScreenAssistant = null;
    ScreenLockTimeout mScreenLockTimeout = new ScreenLockTimeout();
    SleepToken mScreenOffSleepToken;
    ScreenOnCpuBoostHelper mScreenOnCpuBoostHelper = null;
    boolean mScreenOnEarly;
    boolean mScreenOnFully;
    ScreenOnListener mScreenOnListener;
    private boolean mScreenshotChordEnabled;
    private long mScreenshotChordPowerKeyTime;
    private boolean mScreenshotChordPowerKeyTriggered;
    private boolean mScreenshotChordVolumeDownKeyConsumed;
    private long mScreenshotChordVolumeDownKeyTime;
    private boolean mScreenshotChordVolumeDownKeyTriggered;
    ServiceConnection mScreenshotConnection = null;
    final Object mScreenshotLock = new Object();
    private final ScreenshotRunnable mScreenshotRunnable = new ScreenshotRunnable(this, null);
    final Runnable mScreenshotTimeout = new Runnable() {
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
    boolean mSearchKeyShortcutPending;
    SearchManager mSearchManager;
    int mSeascapeRotation = 0;
    final Object mServiceAquireLock = new Object();
    SettingsObserver mSettingsObserver;
    int mShortPressOnPowerBehavior;
    int mShortPressOnSleepBehavior;
    int mShortPressWindowBehavior;
    private LongSparseArray<IShortcutService> mShortcutKeyServices = new LongSparseArray();
    ShortcutManager mShortcutManager;
    boolean mShowingDream;
    private String[] mSpecialApp = new String[]{"com.oppo.camera.Camera", "com.oppo.photoeditor.PhotoEditorActivity", "com.coloros.filemanager.view.browser.FileBrowserActivity", "com.coloros.filemanager.Main"};
    int mStableBottom;
    int mStableFullscreenBottom;
    int mStableFullscreenLeft;
    int mStableFullscreenRight;
    int mStableFullscreenTop;
    int mStableLeft;
    int mStableRight;
    int mStableTop;
    WindowState mStatusBar = null;
    private final StatusBarController mStatusBarController = new StatusBarController();
    int mStatusBarHeight;
    int mStatusBarLayer;
    StatusBarManagerInternal mStatusBarManagerInternal;
    IStatusBarService mStatusBarService;
    boolean mSupportAutoRotation;
    private boolean mSupportLongPressPowerWhenNonInteractive;
    boolean mSystemBooted;
    int mSystemBottom;
    private SystemGesturesPointerEventListener mSystemGestures;
    int mSystemLeft;
    boolean mSystemReady;
    int mSystemRight;
    int mSystemTop;
    private final MutableBoolean mTmpBoolean = new MutableBoolean(false);
    private boolean mTopActivityIsFullscreen = false;
    WindowState mTopDockedOpaqueOrDimmingWindowState;
    WindowState mTopDockedOpaqueWindowState;
    WindowState mTopDockedWindowState;
    WindowState mTopFullscreenOpaqueOrDimmingWindowState;
    WindowState mTopFullscreenOpaqueWindowState;
    boolean mTopIsFullscreen;
    boolean mTranslucentDecorEnabled = true;
    int mTriplePressOnPowerBehavior;
    int mUiMode;
    IUiModeManager mUiModeManager;
    int mUndockedHdmiRotation;
    int mUnrestrictedScreenHeight;
    int mUnrestrictedScreenLeft;
    int mUnrestrictedScreenTop;
    int mUnrestrictedScreenWidth;
    int mUpsideDownRotation = 0;
    boolean mUseTvRouting;
    int mUserRotation = 0;
    int mUserRotationMode = 0;
    private View mVchild = null;
    Vibrator mVibrator;
    int mVoiceContentBottom;
    int mVoiceContentLeft;
    int mVoiceContentRight;
    int mVoiceContentTop;
    Intent mVrHeadsetHomeIntent;
    volatile VrManagerInternal mVrManagerInternal;
    boolean mWakeGestureEnabledSetting;
    MyWakeGestureListener mWakeGestureListener;
    private boolean mWifiDisplayConnected = false;
    private int mWifiDisplayCustomRotation = -1;
    BroadcastReceiver mWifiDisplayReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(PhoneWindowManager.ACTION_WIFI_DISPLAY_VIDEO)) {
                if (intent.getIntExtra("state", 0) == 1) {
                    PhoneWindowManager.this.mWifiDisplayConnected = true;
                } else {
                    PhoneWindowManager.this.mWifiDisplayConnected = false;
                }
                PhoneWindowManager.this.mWifiDisplayCustomRotation = intent.getIntExtra("wfd_UIBC_rot", -1);
                PhoneWindowManager.this.updateRotation(true);
            }
        }
    };
    IWindowManager mWindowManager;
    final Runnable mWindowManagerDrawCallback = new Runnable() {
        public void run() {
            if (PhoneWindowManager.DEBUG_WAKEUP) {
                Slog.i(PhoneWindowManager.TAG, "All windows ready for display!");
            }
            PhoneWindowManager.this.mPhoneWinHandler.sendEmptyMessage(7);
        }
    };
    boolean mWindowManagerDrawComplete;
    WindowManagerFuncs mWindowManagerFuncs;
    WindowManagerInternal mWindowManagerInternal;
    @GuardedBy("mHandler")
    private SleepToken mWindowSleepToken;
    private boolean mWindowSleepTokenNeeded;

    private static class HdmiControl {
        private final HdmiPlaybackClient mClient;

        /* synthetic */ HdmiControl(HdmiPlaybackClient client, HdmiControl -this1) {
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
        public void onInputEvent(InputEvent event, int displayId) {
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
        /* synthetic */ PolicyHandler(PhoneWindowManager this$0, PolicyHandler -this1) {
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
                    PhoneWindowManager.this.showPictureInPictureMenuInternal();
                    return;
                case 18:
                    PhoneWindowManager.this.backLongPress();
                    PhoneWindowManager.this.finishBackKeyPress();
                    return;
                case 19:
                    PhoneWindowManager.this.disposeInputConsumer((InputConsumer) msg.obj);
                    return;
                case 20:
                    PhoneWindowManager.this.backMultiPressAction(((Long) msg.obj).longValue(), msg.arg1);
                    PhoneWindowManager.this.finishBackKeyPress();
                    return;
                case 21:
                    PhoneWindowManager.this.accessibilityShortcutActivated();
                    return;
                case 22:
                    PhoneWindowManager.this.takeBugreport();
                    return;
                case 23:
                    if (PhoneWindowManager.this.mAccessibilityShortcutController.isAccessibilityShortcutAvailable(false)) {
                        PhoneWindowManager.this.accessibilityShortcutActivated();
                        return;
                    }
                    return;
                case 24:
                    PhoneWindowManager.this.mAutofillManagerInternal.onBackKeyPressed();
                    return;
                case 25:
                    PhoneWindowManager.this.sendSystemKeyToStatusBar(msg.arg1);
                    return;
                case 26:
                    PhoneWindowManager.this.launchAllAppsAction();
                    return;
                case 27:
                    PhoneWindowManager.this.notifyMultiWindowFocusChanged(msg.arg1);
                    return;
                case 255:
                    if (PhoneWindowManager.this.mPowerKeyWakeLock.isHeld()) {
                        PhoneWindowManager.this.mPowerKeyWakeLock.release();
                        return;
                    }
                    return;
                case 1001:
                    synchronized (PhoneWindowManager.this.mLock) {
                        HwShutdownRecord.getInstance().recordHwShutdownFlag();
                        Slog.i(PhoneWindowManager.TAG, "OppoEngineerManager.setProductLineTestResult recordHwShutdownFlag mHwShutdownCount=" + PhoneWindowManager.this.mHwShutdownCount);
                        byte[] result = OppoEngineerManager.getProductLineTestResult();
                        if (result != null && result.length > 77) {
                            PhoneWindowManager.this.mHwShutdownCount = result[77];
                            OppoEngineerManager.setProductLineTestResult(77, PhoneWindowManager.this.mHwShutdownCount + 1);
                        }
                    }
                    return;
                case PhoneWindowManager.MSG_OPPO_RELAUNCHER_DISPLAY_FULLSCREEN /*10111*/:
                    PhoneWindowManager.this.luncherFullScreenApp((String) msg.obj);
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

        /* synthetic */ ScreenshotRunnable(PhoneWindowManager this$0, ScreenshotRunnable -this1) {
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
            resolver.registerContentObserver(Secure.getUriFor("incall_back_button_behavior"), false, this, -1);
            resolver.registerContentObserver(Secure.getUriFor("wake_gesture_enabled"), false, this, -1);
            resolver.registerContentObserver(System.getUriFor("accelerometer_rotation"), false, this, -1);
            resolver.registerContentObserver(System.getUriFor("user_rotation"), false, this, -1);
            resolver.registerContentObserver(System.getUriFor("screen_off_timeout"), false, this, -1);
            resolver.registerContentObserver(System.getUriFor("pointer_location"), false, this, -1);
            resolver.registerContentObserver(Secure.getUriFor("default_input_method"), false, this, -1);
            resolver.registerContentObserver(Secure.getUriFor("immersive_mode_confirmations"), false, this, -1);
            resolver.registerContentObserver(Global.getUriFor("policy_control"), false, this, -1);
            resolver.registerContentObserver(Secure.getUriFor(PhoneWindowManager.HIDE_NAVIGATIONBAR_ENABLE), false, this, -1);
            resolver.registerContentObserver(Secure.getUriFor(PhoneWindowManager.SETTINGS_HOVER_NAVIGATIONBAR_ENABLE_STRING), false, this, -1);
            PhoneWindowManager.this.updateSettings();
        }

        public void onChange(boolean selfChange) {
            PhoneWindowManager.this.updateSettings();
            PhoneWindowManager.this.updateRotation(false);
        }
    }

    static {
        sApplicationLaunchKeyCategories.append(64, "android.intent.category.APP_BROWSER");
        sApplicationLaunchKeyCategories.append(65, "android.intent.category.APP_EMAIL");
        sApplicationLaunchKeyCategories.append(207, "android.intent.category.APP_CONTACTS");
        sApplicationLaunchKeyCategories.append(208, "android.intent.category.APP_CALENDAR");
        sApplicationLaunchKeyCategories.append(209, "android.intent.category.APP_MUSIC");
        sApplicationLaunchKeyCategories.append(210, "android.intent.category.APP_CALCULATOR");
        for (Object add : mAutoHideVideoPackageName) {
            mAutoHideApp.add(add);
        }
    }

    /* renamed from: lambda$-com_android_server_policy_PhoneWindowManager_65786 */
    /* synthetic */ void m39lambda$-com_android_server_policy_PhoneWindowManager_65786() {
        if (this.mWindowSleepToken == null) {
            this.mWindowSleepToken = this.mActivityManagerInternal.acquireSleepToken("WindowSleepToken", 0);
        }
    }

    /* renamed from: lambda$-com_android_server_policy_PhoneWindowManager_66054 */
    /* synthetic */ void m40lambda$-com_android_server_policy_PhoneWindowManager_66054() {
        if (this.mWindowSleepToken != null) {
            this.mWindowSleepToken.release();
            this.mWindowSleepToken = null;
        }
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
        if (this.mUserRotationMode == 1) {
            return false;
        }
        try {
            ComponentName cn = this.mActivityManager.getTopAppName();
            if (cn == null) {
                Slog.v(TAG, "cn is null");
                return this.mSupportAutoRotation;
            }
            String topApp = cn.getClassName();
            for (String app : this.mSpecialApp) {
                if (app.equals(topApp)) {
                    Slog.v(TAG, " InSpecial topApp = " + topApp);
                    return this.mSupportAutoRotation;
                }
            }
            if (this.mCurrentAppOrientation == 0 || this.mCurrentAppOrientation == 1 || this.mCurrentAppOrientation == 5 || this.mCurrentAppOrientation == 14) {
                return false;
            }
            return this.mSupportAutoRotation;
        } catch (Exception e) {
            Slog.v(TAG, " InSpecial getTopAppName exception e = " + e.toString());
        }
    }

    void updateOrientationListenerLp() {
        if (this.mOrientationListener.canDetectOrientation()) {
            boolean disable = true;
            if (this.mScreenOnEarly && this.mAwake && this.mKeyguardDrawComplete && this.mWindowManagerDrawComplete && needSensorRunningLp()) {
                disable = false;
                if (!this.mOrientationSensorEnabled) {
                    this.mOrientationListener.enable(true);
                    this.mOrientationSensorEnabled = true;
                }
            }
            if (disable && this.mOrientationSensorEnabled) {
                this.mOrientationListener.disable();
                this.mOrientationSensorEnabled = false;
            }
        }
    }

    private void interceptBackKeyDown() {
        MetricsLogger.count(this.mContext, "key_back_down", 1);
        this.mBackKeyHandled = false;
        if (hasPanicPressOnBackBehavior() && this.mBackKeyPressCounter != 0 && this.mBackKeyPressCounter < 4) {
            this.mHandler.removeMessages(20);
        }
        if (hasLongPressOnBackBehavior()) {
            Message msg = this.mHandler.obtainMessage(18);
            msg.setAsynchronous(true);
            this.mHandler.sendMessageDelayed(msg, ViewConfiguration.get(this.mContext).getDeviceGlobalActionKeyTimeout());
        }
    }

    private boolean interceptBackKeyUp(KeyEvent event) {
        boolean handled = this.mBackKeyHandled;
        if (hasPanicPressOnBackBehavior()) {
            this.mBackKeyPressCounter++;
            long eventTime = event.getDownTime();
            if (this.mBackKeyPressCounter <= 4) {
                Message msg = this.mHandler.obtainMessage(20, this.mBackKeyPressCounter, 0, Long.valueOf(eventTime));
                msg.setAsynchronous(true);
                this.mHandler.sendMessageDelayed(msg, (long) ViewConfiguration.getMultiPressTimeout());
            }
        }
        cancelPendingBackKeyAction();
        if (this.mHasFeatureWatch) {
            TelecomManager telecomManager = getTelecommService();
            if (telecomManager != null) {
                if (telecomManager.isRinging()) {
                    telecomManager.silenceRinger();
                    return false;
                } else if ((this.mIncallBackBehavior & 1) != 0 && telecomManager.isInCall()) {
                    return telecomManager.endCall();
                }
            }
        }
        if (this.mAutofillManagerInternal != null && event.getKeyCode() == 4) {
            this.mHandler.sendMessage(this.mHandler.obtainMessage(24));
        }
        return handled;
    }

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
        if (interactive && (this.mScreenshotChordPowerKeyTriggered ^ 1) != 0 && (event.getFlags() & 1024) == 0) {
            this.mScreenshotChordPowerKeyTriggered = true;
            this.mScreenshotChordPowerKeyTime = event.getDownTime();
            interceptScreenshotChord();
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
        sendSystemKeyToStatusBarAsync(event.getKeyCode());
        if (hungUp || this.mScreenshotChordVolumeDownKeyTriggered || this.mA11yShortcutChordVolumeUpKeyTriggered) {
            z = true;
        } else {
            z = false;
        }
        this.mPowerKeyHandled = z;
        Message msg;
        if (this.mPowerKeyHandled) {
            Log.d(TAG, "interceptPowerKeyDown,  mPowerKeyHandled = " + this.mPowerKeyHandled + ", hungUp = " + hungUp + ", mScreenshotChordVolumeDownKeyTriggered  = " + this.mScreenshotChordVolumeDownKeyTriggered + ", mA11yShortcutChordVolumeUpKeyTriggered = " + this.mA11yShortcutChordVolumeUpKeyTriggered + ", currentKeyCode = " + event.getKeyCode());
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
                this.mHandler.sendMessageDelayed(msg, (long) ViewConfiguration.getMultiPressTimeout());
                return;
            }
            powerPress(eventTime, interactive, this.mPowerKeyPressCounter);
        }
        finishPowerKeyPress();
    }

    private void startHwShutdownDectect() {
        Message msg = this.mHandler.obtainMessage(1001);
        msg.setAsynchronous(true);
        this.mHandler.sendMessageDelayed(msg, 3000);
    }

    private void clearHwShutdownDectect() {
        this.mHandler.removeMessages(1001);
        HwShutdownRecord.getInstance().cancelHwShutdownFlag();
        if (this.mHwShutdownCount != -1) {
            Slog.i(TAG, "OppoEngineerManager.setProductLineTestResult mHwShutdownCount=" + this.mHwShutdownCount);
            OppoEngineerManager.setProductLineTestResult(77, this.mHwShutdownCount);
        }
    }

    private void finishPowerKeyPress() {
        this.mBeganFromNonInteractive = false;
        this.mPowerKeyPressCounter = 0;
        this.mHandler.removeMessages(255);
        if (this.mPowerKeyWakeLock.isHeld()) {
            this.mPowerKeyWakeLock.release();
        }
    }

    private void finishBackKeyPress() {
        this.mBackKeyPressCounter = 0;
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

    private void backMultiPressAction(long eventTime, int count) {
        if (count >= 4) {
            switch (this.mPanicPressOnBackBehavior) {
                case 1:
                    launchHomeFromHotKey();
                    return;
                default:
                    return;
            }
        }
    }

    private void powerPress(long eventTime, boolean interactive, int count) {
        if (!this.mScreenOnEarly || (this.mScreenOnFully ^ 1) == 0) {
            if (count != 2) {
                if (count != 3) {
                    if (interactive && (this.mBeganFromNonInteractive ^ 1) != 0) {
                        this.mScreenOnCpuBoostHelper.acquireCpuBoost(1000);
                        switch (this.mShortPressOnPowerBehavior) {
                            case 1:
                                goToSleep(eventTime, 4, 0);
                                break;
                            case 2:
                                goToSleep(eventTime, 4, 1);
                                break;
                            case 3:
                                goToSleep(eventTime, 4, 1);
                                launchHomeFromHotKey();
                                break;
                            case 4:
                                shortPressPowerGoHome();
                                break;
                            case 5:
                                if (!this.mDismissImeOnBackKeyPressed) {
                                    shortPressPowerGoHome();
                                    break;
                                }
                                if (this.mInputMethodManagerInternal == null) {
                                    this.mInputMethodManagerInternal = (InputMethodManagerInternal) LocalServices.getService(InputMethodManagerInternal.class);
                                }
                                if (this.mInputMethodManagerInternal != null) {
                                    this.mInputMethodManagerInternal.hideCurrentInputMethod();
                                    break;
                                }
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

    private void goToSleep(long eventTime, int reason, int flags) {
        this.mRequestedOrGoingToSleep = true;
        this.mPowerManager.goToSleep(eventTime, reason, flags);
    }

    private void shortPressPowerGoHome() {
        launchHomeFromHotKey(true, false);
        if (isKeyguardShowingAndNotOccluded()) {
            this.mKeyguardDelegate.onShortPowerPressedGoHome();
        }
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
                        goToSleep(eventTime, 4, 0);
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
                performHapticFeedbackLw(null, 0, false);
                if (this.mContext.getPackageManager().hasSystemFeature("oppo.disable.small.window.leather") || !isLeatherModeEnabled(this.mContext) || this.mLidState != 0) {
                    Log.i(TAG, "showGlobalActionsInternal");
                    showGlobalActionsInternal();
                    break;
                }
                sendCloseSystemWindows(SYSTEM_DIALOG_REASON_GLOBAL_ACTIONS);
                Log.i(TAG, "mLidState == LID_CLOSED! sendCloseSystemWindows and Ignore showGlobalActionsDialog!");
                return;
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
                boolean keyguardActive;
                if (this.mKeyguardDelegate == null) {
                    keyguardActive = false;
                } else {
                    keyguardActive = this.mKeyguardDelegate.isShowing();
                }
                if (!keyguardActive) {
                    startActivityAsUser(new Intent("android.intent.action.VOICE_ASSIST"), UserHandle.CURRENT_OR_SELF);
                    return;
                }
                return;
            default:
                return;
        }
    }

    private void accessibilityShortcutActivated() {
        this.mAccessibilityShortcutController.performAccessibilityShortcut();
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
                goToSleep(eventTime, 6, 0);
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

    private boolean hasPanicPressOnBackBehavior() {
        return this.mPanicPressOnBackBehavior != 0;
    }

    private void interceptScreenshotChord() {
        if (this.mScreenshotChordEnabled && this.mScreenshotChordVolumeDownKeyTriggered && this.mScreenshotChordPowerKeyTriggered && (this.mA11yShortcutChordVolumeUpKeyTriggered ^ 1) != 0) {
            long now = SystemClock.uptimeMillis();
            if (now <= this.mScreenshotChordVolumeDownKeyTime + SCREENSHOT_CHORD_DEBOUNCE_DELAY_MILLIS && now <= this.mScreenshotChordPowerKeyTime + SCREENSHOT_CHORD_DEBOUNCE_DELAY_MILLIS) {
                this.mScreenshotChordVolumeDownKeyConsumed = true;
                cancelPendingPowerKeyAction();
                this.mScreenshotRunnable.setScreenshotType(1);
                this.mHandler.postDelayed(this.mScreenshotRunnable, getScreenshotChordLongPressDelay());
            }
        }
    }

    private void interceptAccessibilityShortcutChord() {
        if (this.mAccessibilityShortcutController.isAccessibilityShortcutAvailable(isKeyguardLocked()) && this.mScreenshotChordVolumeDownKeyTriggered && this.mA11yShortcutChordVolumeUpKeyTriggered && (this.mScreenshotChordPowerKeyTriggered ^ 1) != 0) {
            long now = SystemClock.uptimeMillis();
            if (now <= this.mScreenshotChordVolumeDownKeyTime + SCREENSHOT_CHORD_DEBOUNCE_DELAY_MILLIS && now <= this.mA11yShortcutChordVolumeUpKeyTime + SCREENSHOT_CHORD_DEBOUNCE_DELAY_MILLIS) {
                this.mScreenshotChordVolumeDownKeyConsumed = true;
                this.mA11yShortcutChordVolumeUpKeyConsumed = true;
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(21), ViewConfiguration.get(this.mContext).getAccessibilityShortcutKeyTimeout());
            }
        }
    }

    private long getScreenshotChordLongPressDelay() {
        if (this.mKeyguardDelegate.isShowing()) {
            return (long) (((float) ViewConfiguration.get(this.mContext).getDeviceGlobalActionKeyTimeout()) * KEYGUARD_SCREENSHOT_CHORD_DELAY_MULTIPLIER);
        }
        return ViewConfiguration.get(this.mContext).getDeviceGlobalActionKeyTimeout();
    }

    private void cancelPendingScreenshotChordAction() {
        this.mHandler.removeCallbacks(this.mScreenshotRunnable);
    }

    private void cancelPendingAccessibilityShortcutAction() {
        this.mHandler.removeMessages(21);
    }

    public void showGlobalActions() {
        this.mHandler.removeMessages(10);
        this.mHandler.sendEmptyMessage(10);
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "Xiaokang.Feng@ROM.SDK, use oppo global actions", property = OppoRomType.ROM)
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
        boolean isSetupComplete = Secure.getIntForUser(this.mContext.getContentResolver(), "user_setup_complete", 0, -2) != 0;
        if (this.mHasFeatureLeanback) {
            return isSetupComplete & isTvUserSetupComplete();
        }
        return isSetupComplete;
    }

    private boolean isTvUserSetupComplete() {
        return Secure.getIntForUser(this.mContext.getContentResolver(), "tv_user_setup_complete", 0, -2) != 0;
    }

    private void handleShortPressOnHome() {
        HdmiControl hdmiControl = getHdmiControl();
        if (hdmiControl != null) {
            hdmiControl.turnOnTv();
        }
        if (this.mDreamManagerInternal == null || !this.mDreamManagerInternal.isDreaming()) {
            launchHomeFromHotKey();
        } else {
            this.mDreamManagerInternal.stopDream(false);
        }
    }

    private HdmiControl getHdmiControl() {
        if (this.mHdmiControl == null) {
            if (!this.mContext.getPackageManager().hasSystemFeature("android.hardware.hdmi.cec")) {
                return null;
            }
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
                    launchAllAppsAction();
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

    private void launchAllAppsAction() {
        Intent intent = new Intent("android.intent.action.ALL_APPS");
        if (this.mHasFeatureLeanback) {
            PackageManager pm = this.mContext.getPackageManager();
            Intent intentLauncher = new Intent("android.intent.action.MAIN");
            intentLauncher.addCategory("android.intent.category.HOME");
            ResolveInfo resolveInfo = pm.resolveActivityAsUser(intentLauncher, DumpState.DUMP_DEXOPT, this.mCurrentUserId);
            if (resolveInfo != null) {
                intent.setPackage(resolveInfo.activityInfo.packageName);
            }
        }
        startActivityAsUser(intent, UserHandle.CURRENT);
    }

    private void handleDoubleTapOnHome() {
        if (this.mDoubleTapOnHomeBehavior == 1) {
            this.mHomeConsumed = true;
            toggleRecentApps();
        }
    }

    private void showPictureInPictureMenu(KeyEvent event) {
        this.mHandler.removeMessages(17);
        Message msg = this.mHandler.obtainMessage(17);
        msg.setAsynchronous(true);
        msg.sendToTarget();
    }

    private void showPictureInPictureMenuInternal() {
        StatusBarManagerInternal statusbar = getStatusBarManagerInternal();
        if (statusbar != null) {
            statusbar.showPictureInPictureMenu();
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
        this.mStatusBarController.setWindowManagerInternal(this.mWindowManagerInternal);
        DEBUG_PANIC = SystemProperties.getBoolean("persist.sys.assert.panic", false);
        this.mDisplayManagerInternal = (DisplayManagerInternal) LocalServices.getService(DisplayManagerInternal.class);
        this.isTouchFingerPrintSensor = context.getPackageManager().hasSystemFeature(SupportUtil.FRONT_TOUCH_SENSOR);
        this.mIsEnableKeyguardHide = this.mContext.getPackageManager().hasSystemFeature("oppo.fingerprint.enable.keyguard.hide");
        this.mScreenOnCpuBoostHelper = new ScreenOnCpuBoostHelper();
        this.mActivityManager = ActivityManagerNative.getDefault();
        this.mPowerManagerInternal = (PowerManagerInternal) LocalServices.getService(PowerManagerInternal.class);
        this.mAppOpsManager = (AppOpsManager) this.mContext.getSystemService("appops");
        this.mHasFeatureWatch = this.mContext.getPackageManager().hasSystemFeature("android.hardware.type.watch");
        this.mHasFeatureLeanback = this.mContext.getPackageManager().hasSystemFeature("android.software.leanback");
        this.mAccessibilityShortcutController = new AccessibilityShortcutController(this.mContext, new Handler(), this.mCurrentUserId);
        boolean burnInProtectionEnabled = context.getResources().getBoolean(17956947);
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
                minHorizontal = resources.getInteger(17694749);
                maxHorizontal = resources.getInteger(17694746);
                minVertical = resources.getInteger(17694750);
                maxVertical = resources.getInteger(17694748);
                maxRadius = resources.getInteger(17694747);
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
            this.mOrientationListener.setCurrentRotation(windowManager.getDefaultDisplayRotation());
        } catch (RemoteException e) {
        }
        this.mSettingsObserver = new SettingsObserver(this.mHandler);
        this.mSettingsObserver.observe();
        this.mShortcutManager = new ShortcutManager(context);
        this.mUiMode = context.getResources().getInteger(17694770);
        this.mHomeIntent = new Intent("android.intent.action.MAIN", null);
        this.mHomeIntent.addCategory("android.intent.category.HOME");
        this.mHomeIntent.addFlags(270532608);
        this.mEnableCarDockHomeCapture = context.getResources().getBoolean(17956948);
        this.mCarDockIntent = new Intent("android.intent.action.MAIN", null);
        this.mCarDockIntent.addCategory("android.intent.category.CAR_DOCK");
        this.mCarDockIntent.addFlags(270532608);
        this.mDeskDockIntent = new Intent("android.intent.action.MAIN", null);
        this.mDeskDockIntent.addCategory("android.intent.category.DESK_DOCK");
        this.mDeskDockIntent.addFlags(270532608);
        this.mVrHeadsetHomeIntent = new Intent("android.intent.action.MAIN", null);
        this.mVrHeadsetHomeIntent.addCategory("android.intent.category.VR_HOME");
        this.mVrHeadsetHomeIntent.addFlags(270532608);
        this.mPowerManager = (PowerManager) context.getSystemService("power");
        this.mBroadcastWakeLock = this.mPowerManager.newWakeLock(1, "PhoneWindowManager.mBroadcastWakeLock");
        this.mPowerKeyWakeLock = this.mPowerManager.newWakeLock(1, "PhoneWindowManager.mPowerKeyWakeLock");
        this.mEnableShiftMenuBugReports = LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(SystemProperties.get("ro.debuggable"));
        this.mSupportAutoRotation = this.mContext.getResources().getBoolean(17957027);
        this.mLidOpenRotation = readRotation(17694797);
        this.mCarDockRotation = readRotation(17694754);
        this.mDeskDockRotation = readRotation(17694773);
        this.mUndockedHdmiRotation = readRotation(17694868);
        this.mCarDockEnablesAccelerometer = this.mContext.getResources().getBoolean(17956908);
        this.mDeskDockEnablesAccelerometer = this.mContext.getResources().getBoolean(17956920);
        this.mLidKeyboardAccessibility = this.mContext.getResources().getInteger(17694795);
        this.mLidNavigationAccessibility = this.mContext.getResources().getInteger(17694796);
        this.mLidControlsScreenLock = this.mContext.getResources().getBoolean(17956979);
        this.mLidControlsSleep = this.mContext.getResources().getBoolean(17956980);
        this.mTranslucentDecorEnabled = this.mContext.getResources().getBoolean(17956962);
        this.mAllowTheaterModeWakeFromKey = this.mContext.getResources().getBoolean(17956879);
        if (this.mAllowTheaterModeWakeFromKey) {
            z = true;
        } else {
            z = this.mContext.getResources().getBoolean(17956883);
        }
        this.mAllowTheaterModeWakeFromPowerKey = z;
        this.mAllowTheaterModeWakeFromMotion = this.mContext.getResources().getBoolean(17956881);
        this.mAllowTheaterModeWakeFromMotionWhenNotDreaming = this.mContext.getResources().getBoolean(17956882);
        this.mAllowTheaterModeWakeFromCameraLens = this.mContext.getResources().getBoolean(17956876);
        this.mAllowTheaterModeWakeFromLidSwitch = this.mContext.getResources().getBoolean(17956880);
        this.mAllowTheaterModeWakeFromWakeGesture = this.mContext.getResources().getBoolean(17956878);
        this.mGoToSleepOnButtonPressTheaterMode = this.mContext.getResources().getBoolean(17956972);
        this.mSupportLongPressPowerWhenNonInteractive = this.mContext.getResources().getBoolean(17957029);
        this.mLongPressOnBackBehavior = this.mContext.getResources().getInteger(17694800);
        this.mPanicPressOnBackBehavior = this.mContext.getResources().getInteger(17694737);
        this.mShortPressOnPowerBehavior = this.mContext.getResources().getInteger(17694853);
        this.mLongPressOnPowerBehavior = this.mContext.getResources().getInteger(17694802);
        this.mDoublePressOnPowerBehavior = this.mContext.getResources().getInteger(17694775);
        this.mTriplePressOnPowerBehavior = this.mContext.getResources().getInteger(17694867);
        this.mShortPressOnSleepBehavior = this.mContext.getResources().getInteger(17694854);
        this.mUseTvRouting = AudioSystem.getPlatformType(this.mContext) == 2;
        this.mHandleVolumeKeysInWM = this.mContext.getResources().getBoolean(17956974);
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
        filter = new IntentFilter();
        filter.addAction("android.intent.action.DREAMING_STARTED");
        filter.addAction("android.intent.action.DREAMING_STOPPED");
        context.registerReceiver(this.mDreamReceiver, filter);
        context.registerReceiver(this.mMultiuserReceiver, new IntentFilter("android.intent.action.USER_SWITCHED"));
        IntentFilter engineermodeFilter = new IntentFilter();
        engineermodeFilter.addAction(ACTION_DISABLE_LIDCONTROLSSLEEP);
        engineermodeFilter.addAction(ACTION_ENABLE_LIDCONTROLSSLEEP);
        context.registerReceiver(this.mEngineerModeHandleReceiver, engineermodeFilter);
        context.registerReceiver(this.mFullscreenDialogReceiver, new IntentFilter("android.intent.action.CLOSE_SYSTEM_DIALOGS"));
        this.mSystemGestures = new SystemGesturesPointerEventListener(context, new Callbacks() {
            public void onSwipeFromTop() {
                if (PhoneWindowManager.this.mStatusBar != null) {
                    PhoneWindowManager.this.requestTransientBars(PhoneWindowManager.this.mStatusBar);
                }
            }

            public void onSwipeFromBottom() {
                if (PhoneWindowManager.this.mNavigationBar != null && PhoneWindowManager.this.mNavigationBarPosition == 4) {
                    PhoneWindowManager.this.requestTransientBars(PhoneWindowManager.this.mNavigationBar);
                }
            }

            public void onSwipeFromRight() {
                if (PhoneWindowManager.this.mNavigationBar != null && PhoneWindowManager.this.mNavigationBarPosition == 2) {
                    PhoneWindowManager.this.requestTransientBars(PhoneWindowManager.this.mNavigationBar);
                }
            }

            public void onSwipeFromLeft() {
                if (PhoneWindowManager.this.mNavigationBar != null && PhoneWindowManager.this.mNavigationBarPosition == 1) {
                    PhoneWindowManager.this.requestTransientBars(PhoneWindowManager.this.mNavigationBar);
                }
            }

            public void onFling(int duration) {
                if (PhoneWindowManager.this.mPowerManagerInternal != null) {
                    PhoneWindowManager.this.mPowerManagerInternal.powerHint(2, duration);
                }
            }

            public void onScroll(boolean started) {
                if (PhoneWindowManager.this.mPerfBoost == null) {
                    PhoneWindowManager.this.mPerfBoost = new BoostFramework();
                }
                if (PhoneWindowManager.this.mPerfBoost == null) {
                    Slog.e(PhoneWindowManager.TAG, "Error: boost object null");
                    return;
                }
                if (started) {
                    PhoneWindowManager.this.mPerfBoost.perfHint(4231, "", -1, 1);
                } else {
                    PhoneWindowManager.this.mPerfBoost.perfLockRelease();
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
                PhoneWindowManager.this.mHandler.removeMessages(16);
                Message msg = PhoneWindowManager.this.mHandler.obtainMessage(16);
                msg.arg1 = 0;
                PhoneWindowManager.this.mHandler.sendMessageDelayed(msg, 500);
            }

            public void onMouseHoverAtBottom() {
                PhoneWindowManager.this.mHandler.removeMessages(16);
                Message msg = PhoneWindowManager.this.mHandler.obtainMessage(16);
                msg.arg1 = 1;
                PhoneWindowManager.this.mHandler.sendMessageDelayed(msg, 500);
            }

            public void onMouseLeaveFromEdge() {
                PhoneWindowManager.this.mHandler.removeMessages(16);
            }
        });
        this.mImmersiveModeConfirmation = new ImmersiveModeConfirmation(this.mContext);
        this.mWindowManagerFuncs.registerPointerEventListener(this.mSystemGestures);
        this.mVibrator = (Vibrator) context.getSystemService("vibrator");
        Intent wifidisplayIntent = context.registerReceiver(this.mWifiDisplayReceiver, new IntentFilter(ACTION_WIFI_DISPLAY_VIDEO));
        this.mLongPressVibePattern = getLongIntArray(this.mContext.getResources(), 17236017);
        this.mCalendarDateVibePattern = getLongIntArray(this.mContext.getResources(), 17235989);
        this.mSafeModeEnabledVibePattern = getLongIntArray(this.mContext.getResources(), 17236029);
        this.mScreenshotChordEnabled = this.mContext.getResources().getBoolean(17956961);
        this.mGlobalKeyManager = new GlobalKeyManager(this.mContext);
        initializeHdmiState();
        if (!this.mPowerManager.isInteractive()) {
            startedGoingToSleep(2);
            finishedGoingToSleep(2);
        }
        this.mWindowManagerInternal.registerAppTransitionListener(this.mStatusBarController.getAppTransitionListener());
        this.mWindowManagerInternal.registerAppTransitionListener(new AppTransitionListener() {
            public int onAppTransitionStartingLocked(int transit, IBinder openToken, IBinder closeToken, Animation openAnimation, Animation closeAnimation) {
                return PhoneWindowManager.this.handleStartTransitionForKeyguardLw(transit, openAnimation);
            }

            public void onAppTransitionCancelledLocked(int transit) {
                PhoneWindowManager.this.handleStartTransitionForKeyguardLw(transit, null);
            }
        });
        this.mKeyguardDelegate = new KeyguardServiceDelegate(this.mContext, new StateCallback() {
            public void onTrustedChanged() {
                PhoneWindowManager.this.mWindowManagerFuncs.notifyKeyguardTrustedChanged();
            }
        });
        DEBUG_WAKEUP = SystemProperties.getBoolean("persist.sys.assert.panic", false);
        DEBUG_ORIENT = DEBUG_WAKEUP;
        if (!(this.mContext == null || this.mContext.getPackageManager() == null || !this.mContext.getPackageManager().hasSystemFeature("com.oppo.feature.screen.heteromorphism"))) {
            this.mHasHeteromorphismFeature = true;
        }
        if (this.mContext.getPackageManager().hasSystemFeature("oppo.systemui.disable.gesturenavigation")) {
            z = false;
        } else {
            z = this.mContext.getPackageManager().hasSystemFeature("oppo.common.gesture.anim.support");
        }
        this.mGestureAnimSupport = z;
    }

    private void readConfigurationDependentBehaviors() {
        Resources res = this.mContext.getResources();
        this.mLongPressOnHomeBehavior = res.getInteger(17694801);
        if (isExpROM()) {
            this.mLongPressOnHomeBehavior = 2;
        }
        if (this.mLongPressOnHomeBehavior < 0 || this.mLongPressOnHomeBehavior > 2) {
            this.mLongPressOnHomeBehavior = 0;
        }
        this.mDoubleTapOnHomeBehavior = res.getInteger(17694776);
        if (this.mDoubleTapOnHomeBehavior < 0 || this.mDoubleTapOnHomeBehavior > 1) {
            this.mDoubleTapOnHomeBehavior = 0;
        }
        this.mShortPressWindowBehavior = 0;
        if (this.mContext.getPackageManager().hasSystemFeature("android.software.picture_in_picture")) {
            this.mShortPressWindowBehavior = 1;
        }
        this.mNavBarOpacityMode = res.getInteger(17694816);
    }

    private static boolean isExpROM() {
        return SystemProperties.get("persist.sys.oppo.region", "CN").equalsIgnoreCase("CN") ^ 1;
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
                if (res.getBoolean(17957002)) {
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
                if (res.getBoolean(17957002)) {
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
            this.mHasNavigationBar = res.getBoolean(17957011);
            String navBarOverride = SystemProperties.get("qemu.hw.mainkeys");
            if (LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(navBarOverride)) {
                this.mHasNavigationBar = false;
            } else if ("0".equals(navBarOverride)) {
                this.mHasNavigationBar = true;
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
            z = (longSizeDp < 960 || shortSizeDp < 720 || !res.getBoolean(17956969)) ? false : "true".equals(SystemProperties.get("config.override_forced_orient")) ^ 1;
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
            this.mIncallBackBehavior = Secure.getIntForUser(resolver, "incall_back_button_behavior", 0, -2);
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
            this.mHoverNavigationBarStatus = Secure.getInt(this.mContext.getContentResolver(), SETTINGS_HOVER_NAVIGATIONBAR_ENABLE_STRING, this.mHoverNavigationBarStatus);
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
        if (!this.mWakeGestureEnabledSetting || (this.mAwake ^ 1) == 0) {
            return false;
        }
        if (this.mLidControlsSleep && this.mLidState == 0) {
            return false;
        }
        return this.mWakeGestureListener.isSupported();
    }

    private void enablePointerLocation() {
        if (this.mPointerLocationView == null) {
            this.mPointerLocationView = new PointerLocationView(this.mContext);
            this.mPointerLocationView.setPrintCoords(false);
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams(-1, -1);
            lp.type = 2015;
            lp.flags = 1304;
            if (ActivityManager.isHighEndGfx()) {
                lp.flags |= 16777216;
                lp.privateFlags |= 2;
            }
            lp.format = -3;
            lp.setTitle("PointerLocation");
            WindowManager wm = (WindowManager) this.mContext.getSystemService(OppoProcessManager.RESUME_REASON_VISIBLE_WINDOW_STR);
            lp.inputFeatures |= 2;
            wm.addView(this.mPointerLocationView, lp);
            this.mWindowManagerFuncs.registerPointerEventListener(this.mPointerLocationView);
        }
    }

    private void disablePointerLocation() {
        if (this.mPointerLocationView != null) {
            this.mWindowManagerFuncs.unregisterPointerEventListener(this.mPointerLocationView);
            ((WindowManager) this.mContext.getSystemService(OppoProcessManager.RESUME_REASON_VISIBLE_WINDOW_STR)).removeView(this.mPointerLocationView);
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

    public int checkAddPermission(WindowManager.LayoutParams attrs, int[] outAppOp) {
        int i = 0;
        int type = attrs.type;
        outAppOp[0] = -1;
        if ((type < 1 || type > 99) && ((type < 1000 || type > 1999) && (type < OppoArpPeer.ARP_FIRST_RESPONSE_TIMEOUT || type > 2999))) {
            return -10;
        }
        if (type < OppoArpPeer.ARP_FIRST_RESPONSE_TIMEOUT || type > 2999) {
            return 0;
        }
        if (WindowManager.LayoutParams.isSystemAlertWindowType(type)) {
            outAppOp[0] = 24;
            int callingUid = Binder.getCallingUid();
            if (UserHandle.getAppId(callingUid) == 1000) {
                return 0;
            }
            ApplicationInfo appInfo;
            try {
                appInfo = this.mContext.getPackageManager().getApplicationInfoAsUser(attrs.packageName, 0, UserHandle.getUserId(callingUid));
            } catch (NameNotFoundException e) {
                appInfo = null;
            }
            if (appInfo == null || (type != 2038 && appInfo.targetSdkVersion >= 26)) {
                if (this.mContext.checkCallingOrSelfPermission("android.permission.INTERNAL_SYSTEM_WINDOW") != 0) {
                    i = -8;
                }
                return i;
            }
            switch (this.mAppOpsManager.checkOpNoThrow(outAppOp[0], callingUid, attrs.packageName)) {
                case 0:
                case 1:
                    return 0;
                case 2:
                    return appInfo.targetSdkVersion < 23 ? 0 : -8;
                default:
                    if (this.mContext.checkCallingOrSelfPermission("android.permission.SYSTEM_ALERT_WINDOW") != 0) {
                        i = -8;
                    }
                    return i;
            }
        }
        switch (type) {
            case 2005:
                outAppOp[0] = 45;
                return 0;
            case 2011:
            case 2013:
            case 2023:
            case 2030:
            case 2031:
            case 2032:
            case 2035:
            case 2037:
                return 0;
            default:
                if (this.mContext.checkCallingOrSelfPermission("android.permission.INTERNAL_SYSTEM_WINDOW") != 0) {
                    i = -8;
                }
                return i;
        }
    }

    public boolean checkShowToOwnerOnly(WindowManager.LayoutParams attrs) {
        boolean z = true;
        switch (attrs.type) {
            case 3:
            case OppoArpPeer.ARP_FIRST_RESPONSE_TIMEOUT /*2000*/:
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
            case 2030:
            case 2034:
            case 2037:
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

    public void adjustWindowParamsLw(WindowManager.LayoutParams attrs) {
        switch (attrs.type) {
            case OppoArpPeer.ARP_FIRST_RESPONSE_TIMEOUT /*2000*/:
                if (this.mKeyguardOccluded) {
                    attrs.flags &= -1048577;
                    attrs.privateFlags &= -1025;
                    break;
                }
                break;
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
        if (attrs.type != OppoArpPeer.ARP_FIRST_RESPONSE_TIMEOUT) {
            attrs.privateFlags &= -1025;
        }
        if ((attrs.flags & Integer.MIN_VALUE) != 0) {
            attrs.subtreeSystemUiVisibility |= 512;
        }
        boolean forceWindowDrawsStatusBarBackground = (attrs.privateFlags & DumpState.DUMP_INTENT_FILTER_VERIFIERS) != 0;
        if ((attrs.flags & Integer.MIN_VALUE) != 0 || (forceWindowDrawsStatusBarBackground && attrs.height == -1 && attrs.width == -1)) {
            attrs.subtreeSystemUiVisibility |= 1024;
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
        Resources res = ActivityThread.currentActivityThread().getSystemUiContext().getResources();
        this.mStatusBarHeight = res.getDimensionPixelSize(17105258);
        int[] iArr = this.mNavigationBarHeightForRotationDefault;
        int i = this.mPortraitRotation;
        int dimensionPixelSize = res.getDimensionPixelSize(17105141);
        this.mNavigationBarHeightForRotationDefault[this.mUpsideDownRotation] = dimensionPixelSize;
        iArr[i] = dimensionPixelSize;
        iArr = this.mNavigationBarHeightForRotationDefault;
        i = this.mLandscapeRotation;
        dimensionPixelSize = res.getDimensionPixelSize(17105143);
        this.mNavigationBarHeightForRotationDefault[this.mSeascapeRotation] = dimensionPixelSize;
        iArr[i] = dimensionPixelSize;
        iArr = this.mNavigationBarWidthForRotationDefault;
        i = this.mPortraitRotation;
        dimensionPixelSize = res.getDimensionPixelSize(17105146);
        this.mNavigationBarWidthForRotationDefault[this.mSeascapeRotation] = dimensionPixelSize;
        this.mNavigationBarWidthForRotationDefault[this.mLandscapeRotation] = dimensionPixelSize;
        this.mNavigationBarWidthForRotationDefault[this.mUpsideDownRotation] = dimensionPixelSize;
        iArr[i] = dimensionPixelSize;
        iArr = this.mNavigationBarHeightForRotationGestrue;
        i = this.mPortraitRotation;
        dimensionPixelSize = res.getDimensionPixelSize(201654487);
        this.mNavigationBarHeightForRotationGestrue[this.mSeascapeRotation] = dimensionPixelSize;
        this.mNavigationBarHeightForRotationGestrue[this.mLandscapeRotation] = dimensionPixelSize;
        this.mNavigationBarHeightForRotationGestrue[this.mUpsideDownRotation] = dimensionPixelSize;
        iArr[i] = dimensionPixelSize;
    }

    public int getMaxWallpaperLayer() {
        return getWindowLayerFromTypeLw(OppoArpPeer.ARP_FIRST_RESPONSE_TIMEOUT);
    }

    private int getNavigationBarWidth(int rotation, int uiMode) {
        return this.mNavigationBarWidthForRotationDefault[rotation];
    }

    public int getNonDecorDisplayWidth(int fullWidth, int fullHeight, int rotation, int uiMode, int displayId) {
        if (displayId == 0 && this.mHasNavigationBar && this.mNavigationBarCanMove && fullWidth > fullHeight) {
            return fullWidth - getNavigationBarWidth(rotation, uiMode);
        }
        return fullWidth;
    }

    private int getNavigationBarHeight(int rotation, int uiMode) {
        return this.mNavigationBarHeightForRotationDefault[rotation];
    }

    public int getNonDecorDisplayHeight(int fullWidth, int fullHeight, int rotation, int uiMode, int displayId) {
        if (displayId == 0 && this.mHasNavigationBar && (!this.mNavigationBarCanMove || fullWidth < fullHeight)) {
            return fullHeight - getNavigationBarHeight(rotation, uiMode);
        }
        return fullHeight;
    }

    public int getConfigDisplayWidth(int fullWidth, int fullHeight, int rotation, int uiMode, int displayId) {
        return getNonDecorDisplayWidth(fullWidth, fullHeight, rotation, uiMode, displayId);
    }

    public int getConfigDisplayHeight(int fullWidth, int fullHeight, int rotation, int uiMode, int displayId) {
        if (displayId == 0) {
            return getNonDecorDisplayHeight(fullWidth, fullHeight, rotation, uiMode, displayId) - this.mStatusBarHeight;
        }
        return fullHeight;
    }

    public boolean isKeyguardHostWindow(WindowManager.LayoutParams attrs) {
        return attrs.type == OppoArpPeer.ARP_FIRST_RESPONSE_TIMEOUT;
    }

    public boolean canBeHiddenByKeyguardLw(WindowState win) {
        boolean z = false;
        switch (win.getAttrs().type) {
            case OppoArpPeer.ARP_FIRST_RESPONSE_TIMEOUT /*2000*/:
            case 2013:
            case 2019:
            case 2023:
                return false;
            default:
                if (getWindowLayerLw(win) < getWindowLayerFromTypeLw(OppoArpPeer.ARP_FIRST_RESPONSE_TIMEOUT)) {
                    z = true;
                }
                return z;
        }
    }

    private boolean shouldBeHiddenByKeyguard(WindowState win, WindowState imeTarget) {
        boolean z = true;
        int i = 0;
        if (win.getAppToken() != null) {
            return false;
        }
        int showImeOverKeyguard;
        int allowWhenLocked;
        boolean hideDockDivider;
        WindowManager.LayoutParams attrs = win.getAttrs();
        if (imeTarget == null || !imeTarget.isVisibleLw()) {
            showImeOverKeyguard = 0;
        } else if ((imeTarget.getAttrs().flags & DumpState.DUMP_FROZEN) == 0) {
            showImeOverKeyguard = canBeHiddenByKeyguardLw(imeTarget) ^ 1;
        } else {
            showImeOverKeyguard = 1;
        }
        if (win.isInputMethodWindow() || imeTarget == this) {
            allowWhenLocked = showImeOverKeyguard;
        } else {
            allowWhenLocked = 0;
        }
        if (isKeyguardLocked() && isKeyguardOccluded()) {
            if ((attrs.flags & DumpState.DUMP_FROZEN) != 0) {
                i = 1;
            } else if ((attrs.privateFlags & 256) != 0) {
                i = 1;
            }
            allowWhenLocked |= i;
        }
        boolean keyguardLocked = isKeyguardLocked();
        if (attrs.type == 2034) {
            hideDockDivider = this.mWindowManagerInternal.isStackVisible(3) ^ 1;
        } else {
            hideDockDivider = false;
        }
        if (!(keyguardLocked && (allowWhenLocked ^ 1) != 0 && win.getDisplayId() == 0)) {
            z = hideDockDivider;
        }
        return z;
    }

    public void luncherFullScreenAPPForLand(String packageName) {
        PackageManager pm = this.mContext.getPackageManager();
        Intent intent = new Intent("com.coloros.performance.RotateActivity");
        intent.putExtra("packageName", packageName);
        intent.addFlags(268435456);
        try {
            this.mContext.startActivity(intent);
            Slog.i(TAG, "launchActivity land package=" + packageName);
            if (mDisplayCompatUtils != null) {
                mDisplayCompatUtils.updateLocalAppsListForPkg(packageName);
            }
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "catch ActivityNotFoundException land", e);
            luncherFullScreenApp(packageName);
        }
    }

    public void luncherFullScreenApp(String packageName) {
        PackageManager pm = this.mContext.getPackageManager();
        Intent intent = new Intent("android.intent.action.MAIN", null);
        intent.setPackage(packageName);
        intent.addCategory("android.intent.category.LAUNCHER");
        List<ResolveInfo> apps = pm.queryIntentActivities(intent, 0);
        if (apps == null || apps.size() <= 0) {
            Slog.i(TAG, "launchActivity cannot found activity to start for package=" + packageName);
        } else {
            try {
                Intent it = new Intent("android.intent.action.MAIN");
                it.addCategory("android.intent.category.LAUNCHER");
                it.setComponent(new ComponentName(((ResolveInfo) apps.get(0)).activityInfo.packageName, ((ResolveInfo) apps.get(0)).activityInfo.name));
                it.addFlags(268435456);
                this.mContext.startActivity(it);
                Slog.i(TAG, "launchActivity package=" + packageName + ",it=" + it);
            } catch (ActivityNotFoundException e) {
                Log.e(TAG, "catch ActivityNotFoundException", e);
            } catch (Exception e2) {
                Log.e(TAG, "catch Exception", e2);
            }
        }
        if (mDisplayCompatUtils != null) {
            mDisplayCompatUtils.updateLocalAppsListForPkg(packageName);
        }
    }

    public void killFoucsPackage() {
        ((ActivityManager) this.mContext.getSystemService(OppoAppStartupManager.TYPE_ACTIVITY)).forceStopPackage(this.mFoucsPackage);
    }

    public AlertDialog createDialog(final boolean isLandscape) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.mContext, 201523207);
        builder.setTitle(201590167).setView(201917592).setPositiveButton(201590173, new OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                PhoneWindowManager.this.killFoucsPackage();
                if (isLandscape) {
                    if (PhoneWindowManager.this.mColorFullScreenWindowLand != null) {
                        PhoneWindowManager.this.mColorFullScreenWindowLand.setVisibility(8);
                    }
                    PhoneWindowManager.this.luncherFullScreenAPPForLand(PhoneWindowManager.this.mFoucsPackage);
                } else {
                    if (PhoneWindowManager.this.mColorFullScreenWindow != null) {
                        PhoneWindowManager.this.mColorFullScreenWindow.setVisibility(8);
                    }
                    PhoneWindowManager.this.luncherFullScreenApp(PhoneWindowManager.this.mFoucsPackage);
                }
                if (PhoneWindowManager.mDisplayCompatUtils != null) {
                    PhoneWindowManager.mDisplayCompatUtils.updateLocalShowDialogListForPkg(PhoneWindowManager.this.mFoucsPackage);
                }
            }
        }).setNegativeButton(201590174, null);
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.getWindow().getAttributes().type = 2003;
        dialog.show();
        return dialog;
    }

    private void addSagAreaWindow() {
        if (this.mColorSagAreaView == null) {
            this.mColorSagAreaView = new ColorSagView(this.mContext);
            this.mColorSagAreaView.setBackgroundColor(UsbAudioDevice.kAudioDeviceMetaMask);
        }
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.setTitle("SagAreaWindow");
        params.type = 2100;
        params.format = 1;
        params.flags = 8;
        params.flags |= 67108864;
        params.width = -1;
        params.height = -1;
        params.x = 0;
        params.y = 0;
        this.mColorSagAreaView.setVisibility(8);
        ((WindowManager) this.mContext.getSystemService(OppoProcessManager.RESUME_REASON_VISIBLE_WINDOW_STR)).addView(this.mColorSagAreaView, params);
    }

    public void reAddSagAreaWindow() {
        if (this.mColorSagAreaView != null) {
            this.mColorSagAreaView.setVisibility(0);
            this.mColorSagAreaView.invalidate();
        }
    }

    public void removeSagAreaWindow() {
        if (this.mColorSagAreaView != null) {
            this.mColorSagAreaView.setVisibility(8);
            this.mColorSagAreaView.invalidate();
        }
    }

    public void addDisplayFullScreenWindow() {
        this.mPm = (PowerManager) this.mContext.getSystemService("power");
        try {
            Context context = this.mContext;
            if (this.mColorFullScreenWindow == null) {
                this.mColorFullScreenWindow = new LinearLayout(context);
            }
            this.mColorFullScreenWindowLand = new LinearLayout(context);
            View view = this.mColorFullScreenWindow;
            this.mVchild = View.inflate(context, 201917590, null);
            this.mVchild.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (PhoneWindowManager.mDisplayCompatUtils == null || !PhoneWindowManager.mDisplayCompatUtils.shouldShowFullscreenDialogForPkg(PhoneWindowManager.this.mFoucsPackage)) {
                        PhoneWindowManager.this.killFoucsPackage();
                        PhoneWindowManager.this.mColorFullScreenWindow.setVisibility(8);
                        PhoneWindowManager.this.luncherFullScreenApp(PhoneWindowManager.this.mFoucsPackage);
                        return;
                    }
                    if (PhoneWindowManager.this.mFullscreenDialog != null) {
                        PhoneWindowManager.this.mFullscreenDialog.dismiss();
                        PhoneWindowManager.this.mFullscreenDialog = null;
                    }
                    PhoneWindowManager.this.mFullscreenDialog = PhoneWindowManager.this.createDialog(false);
                }
            });
            view = this.mColorFullScreenWindow;
            this.mHchildLeft = View.inflate(context, 201917591, null);
            this.mHchildLeft.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (PhoneWindowManager.mDisplayCompatUtils == null || !PhoneWindowManager.mDisplayCompatUtils.shouldShowFullscreenDialogForPkg(PhoneWindowManager.this.mFoucsPackage)) {
                        PhoneWindowManager.this.killFoucsPackage();
                        PhoneWindowManager.this.mColorFullScreenWindow.setVisibility(8);
                        PhoneWindowManager.this.luncherFullScreenAPPForLand(PhoneWindowManager.this.mFoucsPackage);
                        return;
                    }
                    if (PhoneWindowManager.this.mFullscreenDialogLand != null) {
                        PhoneWindowManager.this.mFullscreenDialogLand.dismiss();
                        PhoneWindowManager.this.mFullscreenDialogLand = null;
                    }
                    PhoneWindowManager.this.mFullscreenDialogLand = PhoneWindowManager.this.createDialog(true);
                }
            });
            view = this.mColorFullScreenWindow;
            this.mHchildRight = View.inflate(context, 201917593, null);
            this.mHchildRight.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (PhoneWindowManager.mDisplayCompatUtils == null || !PhoneWindowManager.mDisplayCompatUtils.shouldShowFullscreenDialogForPkg(PhoneWindowManager.this.mFoucsPackage)) {
                        PhoneWindowManager.this.killFoucsPackage();
                        PhoneWindowManager.this.mColorFullScreenWindowLand.setVisibility(8);
                        PhoneWindowManager.this.luncherFullScreenAPPForLand(PhoneWindowManager.this.mFoucsPackage);
                        return;
                    }
                    if (PhoneWindowManager.this.mFullscreenDialogLand != null) {
                        PhoneWindowManager.this.mFullscreenDialogLand.dismiss();
                        PhoneWindowManager.this.mFullscreenDialogLand = null;
                    }
                    PhoneWindowManager.this.mFullscreenDialogLand = PhoneWindowManager.this.createDialog(true);
                }
            });
            this.mLayoutParams = new LayoutParams(-2, -2, OppoBrightUtils.MIN_LUX_LIMITI);
            ((LinearLayout) this.mColorFullScreenWindow).setGravity(49);
            ((LinearLayout) this.mColorFullScreenWindow).addView(this.mVchild, this.mLayoutParams);
            ((LinearLayout) this.mColorFullScreenWindowLand).addView(this.mHchildLeft, this.mLayoutParams);
            this.mColorFullScreenWindow.setBackgroundColor(UsbAudioDevice.kAudioDeviceMetaMask);
            this.mColorFullScreenWindow.setVisibility(8);
            this.mColorFullScreenWindowLand.setBackgroundColor(UsbAudioDevice.kAudioDeviceMetaMask);
            this.mColorFullScreenWindowLand.setVisibility(8);
            WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            params.setTitle("VColorFullScreenDisplay");
            params.type = 2100;
            params.format = 1;
            params.flags = 8;
            params.flags |= 67108864;
            params.width = -1;
            params.height = -1;
            params.x = 0;
            params.y = 0;
            WindowManager wm = (WindowManager) context.getSystemService(OppoProcessManager.RESUME_REASON_VISIBLE_WINDOW_STR);
            wm.addView(this.mColorFullScreenWindow, params);
            params.setTitle("HColorFullScreenDisplay");
            wm.addView(this.mColorFullScreenWindowLand, params);
            this.mIsVisibility = false;
            addSagAreaWindow();
        } catch (BadTokenException e) {
            Log.e(TAG, "BadTokenException", e);
        } catch (RuntimeException e2) {
            Log.e(TAG, "RuntimeException", e2);
        }
    }

    public void configChangeDisplayFullScreen(int rotation) {
        if (this.mColorSagAreaView != null) {
            this.mColorSagAreaView.requestLayout();
        }
    }

    public void resetDisplayFullScreenWindow() {
        if (this.mColorFullScreenWindow != null && this.mColorFullScreenWindowLand != null) {
            this.mColorFullScreenWindow.setVisibility(8);
            this.mColorFullScreenWindowLand.setVisibility(8);
            this.mColorFullScreenWindow.invalidate();
            this.mColorFullScreenWindowLand.invalidate();
            this.mIsVisibility = false;
            this.mRotation = 0;
            this.mFoucsPackage = null;
            this.mHHHRotation = 0;
        }
    }

    public void reLayoutDisplayFullScreenWindow(boolean visibility, Object packageName, int rotation, boolean needHide) {
        if (!((this.mPm != null && (this.mPm.isScreenOn() ^ 1) != 0) || this.mColorFullScreenWindow == null || this.mColorFullScreenWindowLand == null)) {
            if (this.mIsVisibility == visibility) {
                if (this.mIsVisibility && this.mHHHRotation != rotation) {
                    this.mHHHRotation = rotation;
                    switch (rotation) {
                        case 0:
                            this.mColorFullScreenWindow.setVisibility(0);
                            this.mColorFullScreenWindowLand.setVisibility(8);
                            break;
                        case 1:
                            this.mColorFullScreenWindow.setVisibility(8);
                            this.mColorFullScreenWindowLand.setVisibility(0);
                            break;
                        case 2:
                            this.mColorFullScreenWindow.setVisibility(0);
                            this.mColorFullScreenWindowLand.setVisibility(8);
                            break;
                        case 3:
                            this.mColorFullScreenWindow.setVisibility(8);
                            this.mColorFullScreenWindowLand.setVisibility(0);
                            break;
                    }
                    updateChildVisibility((ViewGroup) this.mColorFullScreenWindow, needHide);
                    updateChildVisibility((ViewGroup) this.mColorFullScreenWindowLand, needHide);
                }
                return;
            }
            this.mIsVisibility = visibility;
            if (this.mIsVisibility) {
                switch (rotation) {
                    case 0:
                        this.mColorFullScreenWindow.setVisibility(0);
                        this.mColorFullScreenWindowLand.setVisibility(8);
                        break;
                    case 1:
                        this.mColorFullScreenWindow.setVisibility(8);
                        this.mColorFullScreenWindowLand.setVisibility(0);
                        break;
                    case 2:
                        this.mColorFullScreenWindow.setVisibility(0);
                        this.mColorFullScreenWindowLand.setVisibility(8);
                        break;
                    case 3:
                        this.mColorFullScreenWindow.setVisibility(8);
                        this.mColorFullScreenWindowLand.setVisibility(0);
                        break;
                }
                this.mFoucsPackage = (String) packageName;
            } else {
                this.mColorFullScreenWindow.setVisibility(8);
                this.mColorFullScreenWindowLand.setVisibility(8);
                this.mFoucsPackage = null;
            }
            updateChildVisibility((ViewGroup) this.mColorFullScreenWindow, needHide);
            updateChildVisibility((ViewGroup) this.mColorFullScreenWindowLand, needHide);
            this.mColorFullScreenWindow.invalidate();
            this.mColorFullScreenWindowLand.invalidate();
        }
    }

    public void updateDisplayFullScreenContent(int displayRotation) {
        if (this.mRotation != displayRotation) {
            this.mRotation = displayRotation;
            if (this.mColorFullScreenWindow != null) {
                switch (displayRotation) {
                    case 0:
                        if (!(this.mVchild == null || this.mLayoutParams == null)) {
                            ((ViewGroup) this.mColorFullScreenWindow).removeAllViews();
                            ((LinearLayout) this.mColorFullScreenWindow).addView(this.mVchild, this.mLayoutParams);
                            break;
                        }
                    case 1:
                        if (!(this.mHchildLeft == null || this.mLayoutParams == null)) {
                            ((LinearLayout) this.mColorFullScreenWindowLand).setGravity(19);
                            ((ViewGroup) this.mColorFullScreenWindowLand).removeAllViews();
                            ((LinearLayout) this.mColorFullScreenWindowLand).addView(this.mHchildLeft, this.mLayoutParams);
                            break;
                        }
                    case 2:
                        if (!(this.mVchild == null || this.mLayoutParams == null)) {
                            ((ViewGroup) this.mColorFullScreenWindow).removeAllViews();
                            ((LinearLayout) this.mColorFullScreenWindow).addView(this.mVchild, this.mLayoutParams);
                            break;
                        }
                    case 3:
                        if (!(this.mHchildRight == null || this.mLayoutParams == null)) {
                            ((LinearLayout) this.mColorFullScreenWindowLand).setGravity(21);
                            ((ViewGroup) this.mColorFullScreenWindowLand).removeAllViews();
                            ((LinearLayout) this.mColorFullScreenWindowLand).addView(this.mHchildRight, this.mLayoutParams);
                            break;
                        }
                }
                this.mColorFullScreenWindow.invalidate();
                this.mColorFullScreenWindowLand.invalidate();
            }
        }
    }

    private void updateChildVisibility(ViewGroup viewParent, boolean needHide) {
        if (viewParent != null && viewParent.getVisibility() == 0) {
            int childCount = viewParent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = viewParent.getChildAt(i);
                if (child != null) {
                    int i2;
                    if (needHide) {
                        i2 = 8;
                    } else {
                        i2 = 0;
                    }
                    child.setVisibility(i2);
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:59:0x018b, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public StartingSurface addSplashScreen(IBinder appToken, String packageName, int theme, CompatibilityInfo compatInfo, CharSequence nonLocalizedLabel, int labelRes, int icon, int logo, int windowFlags, Configuration overrideConfig, int displayId) {
        if (packageName == null) {
            return null;
        }
        WindowManager wm = null;
        View view = null;
        try {
            Context displayContext = getDisplayContext(this.mContext, displayId);
            if (displayContext == null) {
                return null;
            }
            StartingSurface splashScreenSurface;
            Context context = displayContext;
            if (!(theme == displayContext.getThemeResId() && labelRes == 0)) {
                try {
                    context = displayContext.createPackageContext(packageName, 4);
                    context.setTheme(theme);
                } catch (NameNotFoundException e) {
                }
            }
            if (overrideConfig != null) {
                if ((overrideConfig.equals(Configuration.EMPTY) ^ 1) != 0) {
                    Context overrideContext = context.createConfigurationContext(overrideConfig);
                    overrideContext.setTheme(theme);
                    TypedArray typedArray = overrideContext.obtainStyledAttributes(R.styleable.Window);
                    int resId = typedArray.getResourceId(1, 0);
                    if (!(resId == 0 || overrideContext.getDrawable(resId) == null)) {
                        context = overrideContext;
                    }
                    typedArray.recycle();
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
                if (this.mKeyguardOccluded) {
                    windowFlags |= DumpState.DUMP_FROZEN;
                }
            }
            win.setFlags(((windowFlags | 16) | 8) | DumpState.DUMP_INTENT_FILTER_VERIFIERS, ((windowFlags | 16) | 8) | DumpState.DUMP_INTENT_FILTER_VERIFIERS);
            win.setStatusBarColor(0);
            win.setDefaultIcon(icon);
            win.setDefaultLogo(logo);
            win.setLayout(-1, -1);
            WindowManager.LayoutParams params = win.getAttributes();
            params.token = appToken;
            params.packageName = packageName;
            params.windowAnimations = win.getWindowStyle().getResourceId(8, 0);
            params.privateFlags |= 1;
            params.privateFlags |= 16;
            if (!compatInfo.supportsScreen()) {
                params.privateFlags |= 128;
            }
            params.setTitle("Splash Screen " + packageName);
            addSplashscreenContent(win, context);
            wm = (WindowManager) context.getSystemService(OppoProcessManager.RESUME_REASON_VISIBLE_WINDOW_STR);
            view = win.getDecorView();
            wm.addView(view, params);
            if (view.getParent() != null) {
                splashScreenSurface = new SplashScreenSurface(view, appToken);
            } else {
                splashScreenSurface = null;
            }
            if (view != null && view.getParent() == null) {
                Log.w(TAG, "view not successfully added to wm, removing view");
                wm.removeViewImmediate(view);
            }
            return splashScreenSurface;
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

    private void addSplashscreenContent(PhoneWindow win, Context ctx) {
        TypedArray a = ctx.obtainStyledAttributes(R.styleable.Window);
        int resId = a.getResourceId(48, 0);
        a.recycle();
        if (resId != 0) {
            Drawable drawable = ctx.getDrawable(resId);
            if (drawable != null) {
                View v = new View(ctx);
                v.setBackground(drawable);
                win.setContentView(v);
            }
        }
    }

    private Context getDisplayContext(Context context, int displayId) {
        if (displayId == 0) {
            return context;
        }
        Display targetDisplay = ((DisplayManager) context.getSystemService("display")).getDisplay(displayId);
        if (targetDisplay == null) {
            return null;
        }
        return context.createDisplayContext(targetDisplay);
    }

    public int prepareAddWindowLw(WindowState win, WindowManager.LayoutParams attrs) {
        switch (attrs.type) {
            case OppoArpPeer.ARP_FIRST_RESPONSE_TIMEOUT /*2000*/:
                this.mContext.enforceCallingOrSelfPermission("android.permission.STATUS_BAR_SERVICE", "PhoneWindowManager");
                if (this.mStatusBar == null || !this.mStatusBar.isAlive()) {
                    this.mStatusBar = win;
                    this.mStatusBarController.setWindow(win);
                    setKeyguardOccludedLw(this.mKeyguardOccluded, true);
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
                    this.mNavigationBarController.setOnBarVisibilityChangedListener(this.mNavBarVisibilityListener, true);
                    break;
                }
                return -7;
        }
        if (win.toString().contains("OPPORCViewRight")) {
            this.mColorOSRVR = win;
        } else if (win.toString().contains("OPPORCViewLeft")) {
            this.mColorOSRVL = win;
        } else if (win.toString().contains("OPPORCViewBottom")) {
            this.mColorOSRVB = win;
        } else if (win.toString().contains("OPPORCViewTop")) {
            this.mColorOSRVT = win;
        } else if (win.getAttrs() != null && "SystemUIScreenAssistant".equals(win.getAttrs().getTitle())) {
            this.mScreenAssistant = win;
        } else if (win.toString().contains("VColorFullScreenDisplay")) {
            this.mColorFullScreenDisplay = win;
        } else if (win.toString().contains("HColorFullScreenDisplay")) {
            this.mColorFullScreenDisplayLand = win;
        } else if (win.toString().contains("SagAreaWindow")) {
            this.mColorSagAreaWindow = win;
        }
        return 0;
    }

    public void removeWindowLw(WindowState win) {
        if (this.mStatusBar == win) {
            this.mStatusBar = null;
            this.mStatusBarController.setWindow(null);
        } else if (this.mNavigationBar == win) {
            this.mNavigationBar = null;
            this.mNavigationBarController.setWindow(null);
        }
    }

    public int selectAnimationLw(WindowState win, int transit) {
        if (win == this.mStatusBar) {
            boolean isKeyguard = (win.getAttrs().privateFlags & 1024) != 0;
            boolean expanded = win.getAttrs().height == -1 ? win.getAttrs().width == -1 : false;
            if (isKeyguard || expanded) {
                return -1;
            }
            if (transit == 2 || transit == 4) {
                return 17432619;
            }
            if (transit == 1 || transit == 3) {
                return 17432618;
            }
        } else if (win == this.mNavigationBar) {
            if (win.getAttrs().windowAnimations != 0) {
                return 0;
            }
            updateNavigationBarHideState();
            if (this.mHideNavigationBar) {
                return 0;
            }
            if (this.mNavigationBarPosition == 4) {
                if (transit == 2 || transit == 4) {
                    if (isKeyguardShowingAndNotOccluded()) {
                        return 0;
                    }
                    return 17432612;
                } else if (transit == 1 || transit == 3) {
                    return 17432611;
                }
            } else if (this.mNavigationBarPosition == 2) {
                if (transit == 2 || transit == 4) {
                    return 17432617;
                }
                if (transit == 1 || transit == 3) {
                    return 17432616;
                }
            } else if (this.mNavigationBarPosition == 1) {
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
        if (transit != 5) {
            return (win.getAttrs().type == 2023 && this.mDreamingLockscreen && transit == 1) ? -1 : 0;
        } else {
            if (win.hasAppShownWindows()) {
                return 17432593;
            }
        }
    }

    private int selectDockedDividerAnimationLw(WindowState win, int transit) {
        int insets = this.mWindowManagerFuncs.getDockedDividerInsetsLw();
        Rect frame = win.getFrameLw();
        boolean behindNavBar = this.mNavigationBar != null ? ((this.mNavigationBarPosition != 4 || frame.top + insets < this.mNavigationBar.getFrameLw().top) && (this.mNavigationBarPosition != 2 || frame.left + insets < this.mNavigationBar.getFrameLw().left)) ? this.mNavigationBarPosition == 1 ? frame.right - insets <= this.mNavigationBar.getFrameLw().right : false : true : false;
        boolean landscape = frame.height() > frame.width();
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
        if ((this.mScreenOnFully ? okToAnimate() ^ 1 : 1) != 0) {
            anim[0] = 17432685;
            anim[1] = 17432684;
            return;
        }
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
                    break;
                case 2:
                    anim[0] = 17432685;
                    anim[1] = 17432684;
                    break;
                default:
                    anim[1] = 0;
                    anim[0] = 0;
                    break;
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

    public Animation createHiddenByKeyguardExit(boolean onWallpaper, boolean goingToNotificationShade) {
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
        if (!onWallpaper) {
            set.setDuration(0);
        }
        List<Animation> animations = set.getAnimations();
        for (int i2 = animations.size() - 1; i2 >= 0; i2--) {
            ((Animation) animations.get(i2)).setInterpolator(this.mLogDecelerateInterpolator);
        }
        return set;
    }

    public Animation createKeyguardWallpaperExit(boolean goingToNotificationShade) {
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

    public long interceptKeyBeforeDispatching(WindowState win, KeyEvent event, int policyFlags) {
        long now;
        long timeoutTime;
        boolean keyguardOn = keyguardOn();
        int keyCode = event.getKeyCode();
        int repeatCount = event.getRepeatCount();
        int metaState = event.getMetaState();
        int flags = event.getFlags();
        boolean down = event.getAction() == 0;
        boolean canceled = event.isCanceled();
        if (InputLog.DEBUG) {
            Log.d(TAG, "interceptKeyTi keyCode=" + keyCode + " down=" + down + " repeatCount=" + repeatCount + " keyguardOn=" + keyguardOn + " mHomePressed=" + this.mHomePressed + " canceled=" + canceled);
        }
        boolean keydown = event.getAction() == 0;
        boolean keyup = event.getAction() == 1;
        if (keyCode == 4 && keydown && repeatCount == 0) {
            SystemProperties.set("debug.sys.oppo.keydowntime", Long.toString(System.currentTimeMillis()));
        } else if (keyCode == 4 && keyup) {
            SystemProperties.set("debug.sys.oppo.keyuptime", Long.toString(System.currentTimeMillis()));
        } else if (keyCode == 3 && keydown) {
            SystemProperties.set("debug.sys.oppo.keydowntime", Long.toString(JobStatus.NO_LATEST_RUNTIME));
        }
        if (keyCode == 187 && (down ^ 1) != 0 && this.mHandler.hasCallbacks(this.mRecentsStartSplitSreen)) {
            this.mHandler.removeCallbacks(this.mRecentsStartSplitSreen);
        }
        if (this.mScreenshotChordEnabled && (flags & 1024) == 0) {
            if (this.mScreenshotChordVolumeDownKeyTriggered && (this.mScreenshotChordPowerKeyTriggered ^ 1) != 0) {
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
        }
        if (this.mAccessibilityShortcutController.isAccessibilityShortcutAvailable(false) && (flags & 1024) == 0) {
            if ((this.mScreenshotChordVolumeDownKeyTriggered ^ this.mA11yShortcutChordVolumeUpKeyTriggered) != 0) {
                now = SystemClock.uptimeMillis();
                timeoutTime = (this.mScreenshotChordVolumeDownKeyTriggered ? this.mScreenshotChordVolumeDownKeyTime : this.mA11yShortcutChordVolumeUpKeyTime) + SCREENSHOT_CHORD_DEBOUNCE_DELAY_MILLIS;
                if (now < timeoutTime) {
                    return timeoutTime - now;
                }
            }
            if (keyCode == 25 && this.mScreenshotChordVolumeDownKeyConsumed) {
                if (!down) {
                    this.mScreenshotChordVolumeDownKeyConsumed = false;
                }
                return -1;
            } else if (keyCode == 24 && this.mA11yShortcutChordVolumeUpKeyConsumed) {
                if (!down) {
                    this.mA11yShortcutChordVolumeUpKeyConsumed = false;
                }
                return -1;
            }
        }
        if (this.mPendingMetaAction && (KeyEvent.isMetaKey(keyCode) ^ 1) != 0) {
            this.mPendingMetaAction = false;
        }
        if (!(!this.mPendingCapsLockToggle || (KeyEvent.isMetaKey(keyCode) ^ 1) == 0 || (KeyEvent.isAltKey(keyCode) ^ 1) == 0)) {
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
                if (!keyguardOn) {
                    if (down && repeatCount == 0) {
                        preloadRecentApps();
                        this.mRecentsLongPressDetected.set(false);
                        this.mHandler.postDelayed(this.mRecentsStartSplitSreen, 750);
                    } else if (!(down || (this.mRecentsLongPressDetected.get() ^ 1) == 0)) {
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
                if (down && repeatCount == 0 && (isKeyguardLocked() ^ 1) != 0) {
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
                    startActivityAsUser(new Intent("com.android.intent.action.SHOW_BRIGHTNESS_DIALOG"), UserHandle.CURRENT_OR_SELF);
                }
                return -1;
            } else if (keyCode == 24 || keyCode == 25 || keyCode == 164) {
                if (this.mUseTvRouting || this.mHandleVolumeKeysInWM) {
                    dispatchDirectAudioEvent(event);
                    return -1;
                } else if (this.mPersistentVrModeEnabled) {
                    return -1;
                }
            } else if (keyCode == 61 && event.isMetaPressed()) {
                return 0;
            } else {
                if (this.mHasFeatureLeanback && interceptBugreportGestureTv(keyCode, down)) {
                    return -1;
                }
                if (this.mHasFeatureLeanback && interceptAccessibilityGestureTv(keyCode, down)) {
                    return -1;
                }
                if (keyCode == 284) {
                    if (!down) {
                        this.mHandler.removeMessages(26);
                        Message msg = this.mHandler.obtainMessage(26);
                        msg.setAsynchronous(true);
                        msg.sendToTarget();
                    }
                    return -1;
                }
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
                    if (down && repeatCount == 0 && (keyguardOn ^ 1) != 0) {
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
            if (down && repeatCount == 0 && (keyguardOn ^ 1) != 0 && (65536 & metaState) != 0) {
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
            if (down && repeatCount == 0 && (keyguardOn ^ 1) != 0) {
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
                if (this.mRecentAppsHeldModifiers == 0 && (keyguardOn ^ 1) != 0 && isUserSetupComplete()) {
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
            } else if (this.mLanguageSwitchKeyPressed && (down ^ 1) != 0 && (keyCode == 204 || keyCode == 62)) {
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
                if ((65536 & metaState) != 0) {
                    return -1;
                }
                return 0;
            }
        } else if (down) {
            WindowManager.LayoutParams attrs = win != null ? win.getAttrs() : null;
            if (attrs != null) {
                type = attrs.type;
                if (type == 2009 || (attrs.privateFlags & 1024) != 0) {
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
                } else if (this.mDoubleTapOnHomeBehavior == 1) {
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

    private boolean interceptBugreportGestureTv(int keyCode, boolean down) {
        if (keyCode == 23) {
            this.mBugreportTvKey1Pressed = down;
        } else if (keyCode == 4) {
            this.mBugreportTvKey2Pressed = down;
        }
        if (this.mBugreportTvKey1Pressed && this.mBugreportTvKey2Pressed) {
            if (!this.mBugreportTvScheduled) {
                this.mBugreportTvScheduled = true;
                Message msg = Message.obtain(this.mHandler, 22);
                msg.setAsynchronous(true);
                this.mHandler.sendMessageDelayed(msg, 1000);
            }
        } else if (this.mBugreportTvScheduled) {
            this.mHandler.removeMessages(22);
            this.mBugreportTvScheduled = false;
        }
        return this.mBugreportTvScheduled;
    }

    private boolean interceptAccessibilityGestureTv(int keyCode, boolean down) {
        if (keyCode == 4) {
            this.mAccessibilityTvKey1Pressed = down;
        } else if (keyCode == 20) {
            this.mAccessibilityTvKey2Pressed = down;
        }
        if (this.mAccessibilityTvKey1Pressed && this.mAccessibilityTvKey2Pressed) {
            if (!this.mAccessibilityTvScheduled) {
                this.mAccessibilityTvScheduled = true;
                Message msg = Message.obtain(this.mHandler, 23);
                msg.setAsynchronous(true);
                this.mHandler.sendMessageDelayed(msg, ViewConfiguration.get(this.mContext).getAccessibilityShortcutKeyTimeout());
            }
        } else if (this.mAccessibilityTvScheduled) {
            this.mHandler.removeMessages(23);
            this.mAccessibilityTvScheduled = false;
        }
        return this.mAccessibilityTvScheduled;
    }

    private void takeBugreport() {
        if (LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(SystemProperties.get("ro.debuggable")) || Global.getInt(this.mContext.getContentResolver(), "development_settings_enabled", 0) == 1) {
            try {
                ActivityManager.getService().requestBugReport(1);
            } catch (RemoteException e) {
                Slog.e(TAG, "Error taking bugreport", e);
            }
        }
    }

    public KeyEvent dispatchUnhandledKey(WindowState win, KeyEvent event, int policyFlags) {
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

    public void onKeyguardOccludedChangedLw(boolean occluded) {
        if (this.mKeyguardDelegate == null || !this.mKeyguardDelegate.isShowing()) {
            if (this.mKeyguardOccludedChanged) {
                this.mKeyguardOccludedChanged = false;
            }
            setKeyguardOccludedLw(occluded, false);
            return;
        }
        this.mPendingKeyguardOccluded = occluded;
        this.mKeyguardOccludedChanged = true;
    }

    private int handleStartTransitionForKeyguardLw(int transit, Animation anim) {
        if (this.mKeyguardOccludedChanged) {
            this.mKeyguardOccludedChanged = false;
            if (setKeyguardOccludedLw(this.mPendingKeyguardOccluded, false)) {
                return 5;
            }
        }
        if (AppTransition.isKeyguardGoingAwayTransit(transit)) {
            long startTime;
            long duration;
            if (anim != null) {
                startTime = SystemClock.uptimeMillis() + anim.getStartOffset();
            } else {
                startTime = SystemClock.uptimeMillis();
            }
            if (anim != null) {
                duration = anim.getDuration();
            } else {
                duration = 0;
            }
            startKeyguardExitAnimation(startTime, duration);
        }
        return 0;
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
        if (this.mActivityManagerInternal.notifyToFullscreen()) {
            preloadRecentApps();
        }
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
                if (!this.mKeyguardOccluded && this.mKeyguardDelegate.isInputRestricted()) {
                    this.mKeyguardDelegate.verifyUnlock(new OnKeyguardExitResult() {
                        public void onKeyguardExitResult(boolean success) {
                            if (success) {
                                try {
                                    ActivityManager.getService().stopAppSwitches();
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
            ActivityManager.getService().stopAppSwitches();
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

    public void setPipVisibilityLw(boolean visible) {
        this.mPictureInPictureVisible = visible;
    }

    public int adjustSystemUiVisibilityLw(int visibility) {
        this.mStatusBarController.adjustSystemUiVisibilityLw(this.mLastSystemUiFlags, visibility);
        this.mNavigationBarController.adjustSystemUiVisibilityLw(this.mLastSystemUiFlags, visibility);
        this.mResettingSystemUiFlags &= visibility;
        return ((~this.mResettingSystemUiFlags) & visibility) & (~this.mForceClearedSystemUiFlags);
    }

    public boolean getInsetHintLw(WindowManager.LayoutParams attrs, Rect taskBounds, int displayRotation, int displayWidth, int displayHeight, Rect outContentInsets, Rect outStableInsets, Rect outOutsets) {
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
                if ((fl & 1024) != 0 || isForceHidesStatusBar()) {
                    outContentInsets.set(this.mStableFullscreenLeft, this.mStableFullscreenTop, availRight - this.mStableFullscreenRight, availBottom - this.mStableFullscreenBottom);
                } else {
                    outContentInsets.set(this.mStableLeft, this.mStableTop, availRight - this.mStableRight, availBottom - this.mStableBottom);
                }
            } else if ((fl & 1024) != 0 || isForceHidesStatusBar() || (SYSTEM_UI_FLAG_APP_CUSTOM_NAVIGATION_BAR & fl) != 0) {
                outContentInsets.setEmpty();
            } else if ((systemUiVisibility & UsbTerminalTypes.TERMINAL_BIDIR_SKRPHONE_SUPRESS) == 0) {
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

    private boolean shouldUseOutsets(WindowManager.LayoutParams attrs, int fl) {
        if (attrs.type == 2013 || (33555456 & fl) != 0) {
            return true;
        }
        return isForceHidesStatusBar();
    }

    public boolean hasHeteromorphismFeature() {
        return this.mHasHeteromorphismFeature;
    }

    public void layoutDisplayFullScreenWindow(boolean isDefaultDisplay, int displayWidth, int displayHeight, int displayRotation, int uiMode) {
        if (isDefaultDisplay) {
            if (this.mColorFullScreenDisplay != null) {
                layoutLwForColorFullScreen(isDefaultDisplay, displayWidth, displayHeight, displayRotation);
            }
            if (this.mColorFullScreenDisplayLand != null) {
                layoutLwForColorFullScreeLand(isDefaultDisplay, displayWidth, displayHeight, displayRotation);
            }
            if (this.mColorSagAreaWindow != null) {
                layoutLwForSagAreaWindow(isDefaultDisplay, displayWidth, displayHeight, displayRotation);
            }
        }
    }

    public void beginLayoutLwForColorOSRV(boolean isDefaultDisplay, int displayWidth, int displayHeight, int displayRotation, int uiMode) {
        if (this.mHasHeteromorphismFeature) {
            int overscanLeft;
            int overscanTop;
            int overscanRight;
            int overscanBottom;
            this.mDisplayRotation = displayRotation;
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
            int i = this.mUnrestrictedScreenWidth;
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
                if (this.mColorOSRVL != null) {
                    this.mColorOSRVL.computeFrameLw(pf, pf, pf, pf, pf, pf, pf, pf);
                }
                if (this.mColorOSRVR != null) {
                    this.mColorOSRVR.computeFrameLw(pf, pf, pf, pf, pf, pf, pf, pf);
                }
                if (this.mColorOSRVB != null) {
                    this.mColorOSRVB.computeFrameLw(pf, pf, pf, pf, pf, pf, pf, pf);
                }
                if (this.mColorOSRVT != null) {
                    this.mColorOSRVT.computeFrameLw(pf, pf, pf, pf, pf, pf, pf, pf);
                }
                if (this.mScreenAssistant != null) {
                    this.mScreenAssistant.computeFrameLw(pf, pf, pf, pf, pf, pf, pf, pf);
                }
            }
        }
    }

    public void layoutLwForSagAreaWindow(boolean isDefaultDisplay, int displayWidth, int displayHeight, int displayRotation) {
        Rect df = new Rect();
        switch (displayRotation) {
            case 1:
                if (this.mHasHeteromorphismFeature) {
                    df.set(0, 0, 53, 720);
                    break;
                }
                break;
            case 3:
                if (this.mHasHeteromorphismFeature) {
                    df.set(1467, 0, 1520, 720);
                    break;
                }
                break;
            default:
                if (this.mHasHeteromorphismFeature) {
                    df.set(0, 0, 720, 53);
                    break;
                }
                break;
        }
        if (this.mColorSagAreaWindow != null) {
            this.mColorSagAreaWindow.computeFrameLw(df, df, df, df, df, df, df, df);
        }
    }

    public void layoutLwForColorFullScreen(boolean isDefaultDisplay, int displayWidth, int displayHeight, int displayRotation) {
        Rect df = new Rect();
        switch (displayRotation) {
            case 0:
                if (!this.mHasHeteromorphismFeature) {
                    df.set(0, 1280, 720, 1520);
                    break;
                } else {
                    df.set(0, 1333, 720, 1520);
                    break;
                }
            case 2:
                if (!this.mHasHeteromorphismFeature) {
                    df.set(0, 1280, 720, 1520);
                    break;
                } else {
                    df.set(0, 1333, 720, 1520);
                    break;
                }
            default:
                if (!this.mHasHeteromorphismFeature) {
                    df.set(0, 1280, 720, 1520);
                    break;
                } else {
                    df.set(0, 1333, 720, 1520);
                    break;
                }
        }
        if (this.mColorFullScreenDisplay != null) {
            this.mColorFullScreenDisplay.computeFrameLw(df, df, df, df, df, df, df, df);
        }
    }

    public void layoutLwForColorFullScreeLand(boolean isDefaultDisplay, int displayWidth, int displayHeight, int displayRotation) {
        Rect df = new Rect();
        switch (displayRotation) {
            case 1:
                if (!this.mHasHeteromorphismFeature) {
                    df.set(1280, 0, 1520, 720);
                    break;
                } else {
                    df.set(1333, 0, 1520, 720);
                    break;
                }
            case 3:
                if (!this.mHasHeteromorphismFeature) {
                    df.set(0, 0, 187, 720);
                    break;
                } else {
                    df.set(0, 0, 187, 720);
                    break;
                }
            default:
                if (!this.mHasHeteromorphismFeature) {
                    df.set(0, 0, 187, 720);
                    break;
                } else {
                    df.set(0, 0, 187, 720);
                    break;
                }
        }
        if (this.mColorFullScreenDisplayLand != null) {
            this.mColorFullScreenDisplayLand.computeFrameLw(df, df, df, df, df, df, df, df);
        }
    }

    public WindowState getTopFullscreenOpaqueWindowState() {
        return this.mTopFullscreenOpaqueWindowState;
    }

    public WindowState getFullScreenDisplayWindow() {
        return this.mColorFullScreenDisplay;
    }

    public WindowState getFullScreenDisplayWindowLand() {
        return this.mColorFullScreenDisplayLand;
    }

    public void beginLayoutLwForCompat(boolean isDefaultDisplay, int displayWidth, int displayHeight, int displayRotation, int uiMode, boolean isFullScreen, boolean is169, WindowState w) {
        int overscanLeft;
        int overscanTop;
        int overscanRight;
        int overscanBottom;
        int i;
        this.mDisplayRotation = displayRotation;
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
        if (this.mHasHeteromorphismFeature) {
        }
        if (this.mHasHeteromorphismFeature) {
        }
        if (isShowHoverNavigationBar()) {
        }
        int fullScreenBarAndNavigationBar = this.mHasHeteromorphismFeature ? 280 : 240;
        if (this.mHasHeteromorphismFeature) {
        }
        if (this.mHasHeteromorphismFeature) {
        }
        if (displayRotation == 0 || displayRotation == 2) {
            if (isFullScreen && (is169 ^ 1) != 0) {
                this.mRestrictedOverscanScreenLeft = 0;
                this.mOverscanScreenLeft = 0;
                this.mRestrictedOverscanScreenTop = 53;
                this.mOverscanScreenTop = 53;
                this.mRestrictedOverscanScreenWidth = displayWidth;
                this.mOverscanScreenWidth = displayWidth;
                i = displayHeight - 53;
                this.mRestrictedOverscanScreenHeight = i;
                this.mOverscanScreenHeight = i;
                this.mSystemLeft = 0;
                this.mSystemTop = 53;
                this.mSystemRight = displayWidth;
                this.mSystemBottom = displayHeight;
                this.mUnrestrictedScreenLeft = overscanLeft;
                this.mUnrestrictedScreenTop = overscanTop + 53;
                this.mUnrestrictedScreenWidth = (displayWidth - overscanLeft) - overscanRight;
                this.mUnrestrictedScreenHeight = ((displayHeight - overscanTop) - overscanBottom) - 53;
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
            } else if (isFullScreen && is169) {
                this.mRestrictedOverscanScreenLeft = 0;
                this.mOverscanScreenLeft = 0;
                this.mRestrictedOverscanScreenTop = 53;
                this.mOverscanScreenTop = 53;
                this.mRestrictedOverscanScreenWidth = displayWidth;
                this.mOverscanScreenWidth = displayWidth;
                i = (displayHeight - 187) - 53;
                this.mRestrictedOverscanScreenHeight = i;
                this.mOverscanScreenHeight = i;
                this.mSystemLeft = 0;
                this.mSystemTop = 53;
                this.mSystemRight = displayWidth;
                this.mSystemBottom = displayHeight - 187;
                this.mUnrestrictedScreenLeft = overscanLeft;
                this.mUnrestrictedScreenTop = overscanTop + 53;
                this.mUnrestrictedScreenWidth = (displayWidth - overscanLeft) - overscanRight;
                this.mUnrestrictedScreenHeight = ((displayHeight - overscanTop) - overscanBottom) - 53;
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
                i = (displayHeight - overscanBottom) - 187;
                this.mCurBottom = i;
                this.mStableFullscreenBottom = i;
                this.mStableBottom = i;
                this.mVoiceContentBottom = i;
                this.mContentBottom = i;
                this.mDockBottom = i;
            } else if (isFullScreen || !is169) {
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
            } else {
                this.mRestrictedOverscanScreenLeft = 0;
                this.mOverscanScreenLeft = 0;
                this.mRestrictedOverscanScreenTop = 0;
                this.mOverscanScreenTop = 0;
                this.mRestrictedOverscanScreenWidth = displayWidth;
                this.mOverscanScreenWidth = displayWidth;
                i = displayHeight - 187;
                this.mRestrictedOverscanScreenHeight = i;
                this.mOverscanScreenHeight = i;
                this.mSystemLeft = 0;
                this.mSystemTop = 0;
                this.mSystemRight = displayWidth;
                this.mSystemBottom = displayHeight - 187;
                this.mUnrestrictedScreenLeft = overscanLeft;
                this.mUnrestrictedScreenTop = overscanTop;
                this.mUnrestrictedScreenWidth = (displayWidth - overscanLeft) - overscanRight;
                this.mUnrestrictedScreenHeight = ((displayHeight - overscanTop) - overscanBottom) - 187;
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
                i = (displayHeight - overscanBottom) - 187;
                this.mCurBottom = i;
                this.mStableFullscreenBottom = i;
                this.mStableBottom = i;
                this.mVoiceContentBottom = i;
                this.mContentBottom = i;
                this.mDockBottom = i;
            }
        } else if (displayRotation == 1) {
            if (is169 && isFullScreen) {
                this.mRestrictedOverscanScreenLeft = 53;
                this.mOverscanScreenLeft = 53;
                this.mRestrictedOverscanScreenTop = 0;
                this.mOverscanScreenTop = 0;
                i = (displayWidth - 187) - 53;
                this.mRestrictedOverscanScreenWidth = i;
                this.mOverscanScreenWidth = i;
                this.mRestrictedOverscanScreenHeight = displayHeight;
                this.mOverscanScreenHeight = displayHeight;
                this.mSystemLeft = 53;
                this.mSystemTop = 0;
                this.mSystemRight = displayWidth - 187;
                this.mSystemBottom = displayHeight;
                this.mUnrestrictedScreenLeft = overscanLeft + 53;
                this.mUnrestrictedScreenTop = overscanTop;
                this.mUnrestrictedScreenWidth = (((displayWidth - overscanLeft) - overscanRight) - 53) - 187;
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
                i = (displayWidth - overscanRight) - 187;
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
            } else if (!is169 && isFullScreen) {
                this.mRestrictedOverscanScreenLeft = 53;
                this.mOverscanScreenLeft = 53;
                this.mRestrictedOverscanScreenTop = 0;
                this.mOverscanScreenTop = 0;
                i = displayWidth - 53;
                this.mRestrictedOverscanScreenWidth = i;
                this.mOverscanScreenWidth = i;
                this.mRestrictedOverscanScreenHeight = displayHeight;
                this.mOverscanScreenHeight = displayHeight;
                this.mSystemLeft = 53;
                this.mSystemTop = 0;
                this.mSystemRight = displayWidth;
                this.mSystemBottom = displayHeight;
                this.mUnrestrictedScreenLeft = overscanLeft + 53;
                this.mUnrestrictedScreenTop = overscanTop;
                this.mUnrestrictedScreenWidth = ((displayWidth - overscanLeft) - overscanRight) - 53;
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
            } else if (!is169 || (isFullScreen ^ 1) == 0) {
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
                this.mSystemRight = displayWidth - fullScreenBarAndNavigationBar;
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
                i = (displayWidth - overscanRight) - fullScreenBarAndNavigationBar;
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
                i = displayWidth - 187;
                this.mRestrictedOverscanScreenWidth = i;
                this.mOverscanScreenWidth = i;
                this.mRestrictedOverscanScreenHeight = displayHeight;
                this.mOverscanScreenHeight = displayHeight;
                this.mSystemLeft = 0;
                this.mSystemTop = 0;
                this.mSystemRight = displayWidth - 187;
                this.mSystemBottom = displayHeight;
                this.mUnrestrictedScreenLeft = overscanLeft;
                this.mUnrestrictedScreenTop = overscanTop;
                this.mUnrestrictedScreenWidth = ((displayWidth - overscanLeft) - overscanRight) - 187;
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
                i = (displayWidth - overscanRight) - 187;
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
        } else if (displayRotation == 3) {
            if (is169 && isFullScreen) {
                this.mRestrictedOverscanScreenLeft = 187;
                this.mOverscanScreenLeft = 187;
                this.mRestrictedOverscanScreenTop = 0;
                this.mOverscanScreenTop = 0;
                i = (displayWidth - 187) - 53;
                this.mRestrictedOverscanScreenWidth = i;
                this.mOverscanScreenWidth = i;
                this.mRestrictedOverscanScreenHeight = displayHeight;
                this.mOverscanScreenHeight = displayHeight;
                this.mSystemLeft = 187;
                this.mSystemTop = 0;
                this.mSystemRight = displayWidth - 53;
                this.mSystemBottom = displayHeight;
                this.mUnrestrictedScreenLeft = overscanLeft + 187;
                this.mUnrestrictedScreenTop = overscanTop;
                this.mUnrestrictedScreenWidth = (((displayWidth - overscanLeft) - overscanRight) - 187) - 53;
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
                i = displayWidth - 53;
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
            } else if (is169 && (isFullScreen ^ 1) != 0) {
                this.mRestrictedOverscanScreenLeft = 187;
                this.mOverscanScreenLeft = 187;
                this.mRestrictedOverscanScreenTop = 0;
                this.mOverscanScreenTop = 0;
                i = displayWidth - 187;
                this.mRestrictedOverscanScreenWidth = i;
                this.mOverscanScreenWidth = i;
                this.mRestrictedOverscanScreenHeight = displayHeight;
                this.mOverscanScreenHeight = displayHeight;
                this.mSystemLeft = 187;
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
            } else if (is169 || !isFullScreen) {
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
            } else {
                this.mRestrictedOverscanScreenLeft = 0;
                this.mOverscanScreenLeft = 0;
                this.mRestrictedOverscanScreenTop = 0;
                this.mOverscanScreenTop = 0;
                i = displayWidth - 53;
                this.mRestrictedOverscanScreenWidth = i;
                this.mOverscanScreenWidth = i;
                this.mRestrictedOverscanScreenHeight = displayHeight;
                this.mOverscanScreenHeight = displayHeight;
                this.mSystemLeft = 0;
                this.mSystemTop = 0;
                this.mSystemRight = displayWidth - 53;
                this.mSystemBottom = displayHeight;
                this.mUnrestrictedScreenLeft = overscanLeft;
                this.mUnrestrictedScreenTop = overscanTop;
                this.mUnrestrictedScreenWidth = ((displayWidth - overscanLeft) - overscanRight) - 53;
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
                i = (displayWidth - 53) - overscanRight;
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
            navTranslucent &= immersiveSticky ^ 1;
            boolean isKeyguardShowing = isStatusBarKeyguard() ? this.mKeyguardOccluded ^ 1 : false;
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
                this.mInputConsumer = this.mWindowManagerFuncs.createInputConsumer(this.mHandler.getLooper(), "nav_input_consumer", new -$Lambda$Nd7e3Murb8x7RqelLk3bI3c3rfY((byte) 1, this));
                InputManager.getInstance().setPointerIconType(0);
            }
            if (layoutNavigationBarForCompat(displayWidth, displayHeight, displayRotation, uiMode, overscanLeft, overscanRight, overscanBottom, dcf, navVisible | (canHideNavigationBar() ^ 1), navTranslucent, navAllowedHidden, statusBarExpandedNotKeyguard, is169, isFullScreen) | layoutStatusBarForCompat(pf, df, of, vf, dcf, sysui, isKeyguardShowing, isFullScreen)) {
                updateSystemUiVisibilityLw();
            }
        }
    }

    /* renamed from: lambda$-com_android_server_policy_PhoneWindowManager_293161 */
    /* synthetic */ InputEventReceiver m37lambda$-com_android_server_policy_PhoneWindowManager_293161(InputChannel channel, Looper looper) {
        return new HideNavInputEventReceiver(channel, looper);
    }

    public void beginLayoutLw(boolean isDefaultDisplay, int displayWidth, int displayHeight, int displayRotation, int uiMode) {
        int overscanLeft;
        int overscanTop;
        int overscanRight;
        int overscanBottom;
        int i;
        this.mDisplayRotation = displayRotation;
        int naturalHeight = this.mNaturalHeight;
        int compatHeight = ((this.mNaturalWidth * 16) / 9) + 53;
        int navigationBarHeight = this.mNavigationBarHeightForRotationDefault[displayRotation];
        int navigatinoBarWidth = this.mNavigationBarWidthForRotationDefault[displayRotation];
        int emptySize = (naturalHeight - compatHeight) - (navigationBarHeight < navigatinoBarWidth ? navigationBarHeight : navigatinoBarWidth);
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
            this.mRestrictedOverscanScreenTop = 53;
            this.mOverscanScreenTop = 53;
            this.mRestrictedOverscanScreenWidth = displayWidth;
            this.mOverscanScreenWidth = displayWidth;
            this.mRestrictedOverscanScreenHeight = displayHeight;
            this.mOverscanScreenHeight = displayHeight;
            this.mSystemLeft = 0;
            this.mSystemTop = 53;
            this.mSystemRight = displayWidth;
            this.mSystemBottom = displayHeight + 53;
            this.mUnrestrictedScreenLeft = overscanLeft;
            this.mUnrestrictedScreenTop = overscanTop + 53;
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
            i = (displayHeight + 53) - overscanBottom;
            this.mCurBottom = i;
            this.mStableFullscreenBottom = i;
            this.mStableBottom = i;
            this.mVoiceContentBottom = i;
            this.mContentBottom = i;
            this.mDockBottom = i;
        } else if (displayRotation == 1 && isCompatWindow) {
            this.mRestrictedOverscanScreenLeft = 53;
            this.mOverscanScreenLeft = 53;
            this.mRestrictedOverscanScreenTop = 0;
            this.mOverscanScreenTop = 0;
            this.mRestrictedOverscanScreenWidth = displayWidth;
            this.mOverscanScreenWidth = displayWidth;
            this.mRestrictedOverscanScreenHeight = displayHeight;
            this.mOverscanScreenHeight = displayHeight;
            this.mSystemLeft = 53;
            this.mSystemTop = 0;
            this.mSystemRight = displayWidth + 53;
            this.mSystemBottom = displayHeight;
            this.mUnrestrictedScreenLeft = overscanLeft + 53;
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
            i = (displayWidth + 53) - overscanRight;
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
            this.mRestrictedOverscanScreenLeft = 144;
            this.mOverscanScreenLeft = 144;
            this.mRestrictedOverscanScreenTop = 0;
            this.mOverscanScreenTop = 0;
            this.mRestrictedOverscanScreenWidth = displayWidth;
            this.mOverscanScreenWidth = displayWidth;
            this.mRestrictedOverscanScreenHeight = displayHeight;
            this.mOverscanScreenHeight = displayHeight;
            this.mSystemLeft = 144;
            this.mSystemTop = 0;
            this.mSystemRight = displayWidth + 144;
            this.mSystemBottom = displayHeight;
            this.mUnrestrictedScreenLeft = overscanLeft + 144;
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
            i = (displayWidth + 144) - overscanRight;
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
            navTranslucent &= immersiveSticky ^ 1;
            boolean isKeyguardShowing = isStatusBarKeyguard() ? this.mKeyguardOccluded ^ 1 : false;
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
                this.mInputConsumer = this.mWindowManagerFuncs.createInputConsumer(this.mHandler.getLooper(), "nav_input_consumer", new -$Lambda$Nd7e3Murb8x7RqelLk3bI3c3rfY((byte) 0, this));
                InputManager.getInstance().setPointerIconType(0);
            }
            navVisible |= canHideNavigationBar() ^ 1;
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
            if (!isCompatWindow && (updateSysUiVisibility | layoutStatusBar(pf, df, of, vf, dcf, sysui, isKeyguardShowing))) {
                updateSystemUiVisibilityLw();
            }
        }
    }

    /* renamed from: lambda$-com_android_server_policy_PhoneWindowManager_309504 */
    /* synthetic */ InputEventReceiver m38lambda$-com_android_server_policy_PhoneWindowManager_309504(InputChannel channel, Looper looper) {
        return new HideNavInputEventReceiver(channel, looper);
    }

    private boolean layoutStatusBarForCompat(Rect pf, Rect df, Rect of, Rect vf, Rect dcf, int sysui, boolean isKeyguardShowing, int displayRotation, boolean isCompatWindow) {
        if (this.mStatusBar != null) {
            this.mStatusBarLayer = this.mStatusBar.getSurfaceLayer();
            this.mStableTop = this.mUnrestrictedScreenTop + this.mStatusBarHeight;
            boolean statusBarTransient = (67108864 & sysui) != 0;
            int statusBarTranslucent = (1073741832 & sysui) != 0 ? 1 : 0;
            if (!isKeyguardShowing) {
                statusBarTranslucent &= areTranslucentBarsAllowed();
            }
            if (this.mStatusBar.isVisibleLw() && (statusBarTransient ^ 1) != 0) {
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
            }
            if (!(!this.mStatusBar.isVisibleLw() || (this.mStatusBar.isAnimatingLw() ^ 1) == 0 || (statusBarTransient ^ 1) == 0 || (statusBarTranslucent ^ 1) == 0 || (this.mStatusBarController.wasRecentlyTranslucent() ^ 1) == 0)) {
                this.mSystemTop = this.mUnrestrictedScreenTop + this.mStatusBarHeight;
            }
            if (this.mStatusBarController.checkHiddenLw()) {
                return true;
            }
        }
        return false;
    }

    private boolean layoutStatusBarForCompat(Rect pf, Rect df, Rect of, Rect vf, Rect dcf, int sysui, boolean isKeyguardShowing, boolean isFullScreen) {
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
            if (!isFullScreen) {
                this.mStableTop = this.mUnrestrictedScreenTop + this.mStatusBarHeight;
            }
            boolean statusBarTransient = (67108864 & sysui) != 0;
            int statusBarTranslucent = (1073741832 & sysui) != 0 ? 1 : 0;
            if (!isKeyguardShowing) {
                statusBarTranslucent &= areTranslucentBarsAllowed();
            }
            if (!(!this.mStatusBar.isVisibleLw() || (statusBarTransient ^ 1) == 0 || (isForceHidesStatusBar() ^ 1) == 0)) {
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
            }
            if (!(!this.mStatusBar.isVisibleLw() || (this.mStatusBar.isAnimatingLw() ^ 1) == 0 || (isForceHidesStatusBar() ^ 1) == 0 || (statusBarTransient ^ 1) == 0 || (statusBarTranslucent ^ 1) == 0 || (this.mStatusBarController.wasRecentlyTranslucent() ^ 1) == 0 || isFullScreen)) {
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
            int statusBarTranslucent = (1073741832 & sysui) != 0 ? 1 : 0;
            if (!isKeyguardShowing) {
                statusBarTranslucent &= areTranslucentBarsAllowed();
            }
            if (!(!this.mStatusBar.isVisibleLw() || (statusBarTransient ^ 1) == 0 || (isForceHidesStatusBar() ^ 1) == 0)) {
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
            }
            if (!(!this.mStatusBar.isVisibleLw() || (this.mStatusBar.isAnimatingLw() ^ 1) == 0 || (isForceHidesStatusBar() ^ 1) == 0 || (statusBarTransient ^ 1) == 0 || (statusBarTranslucent ^ 1) == 0 || (this.mStatusBarController.wasRecentlyTranslucent() ^ 1) == 0)) {
                this.mSystemTop = this.mUnrestrictedScreenTop + this.mStatusBarHeight;
            }
            if (this.mStatusBarController.checkHiddenLw()) {
                return true;
            }
        }
        return false;
    }

    private boolean layoutNavigationBarForCompat(int displayWidth, int displayHeight, int displayRotation, int uiMode, int overscanLeft, int overscanRight, int overscanBottom, Rect dcf, boolean navVisible, boolean navTranslucent, boolean navAllowedHidden, boolean statusBarExpandedNotKeyguard, boolean is169, boolean isFullScreen) {
        int fullScreenBarAndNavigationBar = this.mHasHeteromorphismFeature ? 187 : 240;
        if (this.mNavigationBar != null) {
            int i;
            boolean transientNavBarShowing = this.mNavigationBarController.isTransientShowing();
            updateNavigationBarHideState();
            this.mNavigationBarPosition = navigationBarPosition(displayWidth, displayHeight, displayRotation);
            if (this.mNavigationBarPosition == 4) {
                int top = (displayHeight - overscanBottom) - getNavigationBarHeightForCompat(displayRotation, uiMode);
                if (is169) {
                    top = (displayHeight - overscanBottom) - fullScreenBarAndNavigationBar;
                }
                if (is169) {
                    mTmpNavigationFrame.set(0, top, displayWidth, displayHeight - overscanBottom);
                } else {
                    mTmpNavigationFrame.set(0, top, displayWidth, displayHeight - overscanBottom);
                }
                i = mTmpNavigationFrame.top;
                this.mStableFullscreenBottom = i;
                this.mStableBottom = i;
                if (transientNavBarShowing) {
                    this.mNavigationBarController.setBarShowingLw(true);
                } else if (navVisible) {
                    this.mNavigationBarController.setBarShowingLw(true);
                    this.mDockBottom = mTmpNavigationFrame.top;
                    this.mRestrictedScreenHeight = this.mDockBottom - this.mRestrictedScreenTop;
                    this.mRestrictedOverscanScreenHeight = this.mDockBottom - this.mRestrictedOverscanScreenTop;
                } else {
                    this.mNavigationBarController.setBarShowingLw(statusBarExpandedNotKeyguard);
                }
                if (!(!navVisible || (navTranslucent ^ 1) == 0 || (navAllowedHidden ^ 1) == 0 || (this.mNavigationBar.isAnimatingLw() ^ 1) == 0 || (this.mNavigationBarController.wasRecentlyTranslucent() ^ 1) == 0)) {
                    this.mSystemBottom = mTmpNavigationFrame.top;
                }
            } else if (this.mNavigationBarPosition == 2) {
                int left = (displayWidth - overscanRight) - getNavigationBarWidthForCompat(displayRotation, uiMode);
                if (is169) {
                    left = (displayWidth - overscanRight) - fullScreenBarAndNavigationBar;
                }
                if (is169) {
                    mTmpNavigationFrame.set(left, 0, displayWidth - overscanRight, displayHeight);
                } else {
                    mTmpNavigationFrame.set(left, 0, displayWidth - overscanRight, displayHeight);
                }
                i = mTmpNavigationFrame.left;
                this.mStableFullscreenRight = i;
                this.mStableRight = i;
                if (transientNavBarShowing) {
                    this.mNavigationBarController.setBarShowingLw(true);
                } else if (navVisible) {
                    this.mNavigationBarController.setBarShowingLw(true);
                    this.mDockRight = mTmpNavigationFrame.left;
                    this.mRestrictedScreenWidth = this.mDockRight - this.mRestrictedScreenLeft;
                    this.mRestrictedOverscanScreenWidth = this.mDockRight - this.mRestrictedOverscanScreenLeft;
                } else {
                    this.mNavigationBarController.setBarShowingLw(statusBarExpandedNotKeyguard);
                }
                if (!(!navVisible || (navTranslucent ^ 1) == 0 || (navAllowedHidden ^ 1) == 0 || (this.mNavigationBar.isAnimatingLw() ^ 1) == 0 || (this.mNavigationBarController.wasRecentlyTranslucent() ^ 1) == 0)) {
                    this.mSystemRight = mTmpNavigationFrame.left;
                }
            } else if (this.mNavigationBarPosition == 1) {
                int right = overscanLeft + getNavigationBarWidthForCompat(displayRotation, uiMode);
                if (is169) {
                    right = overscanLeft + fullScreenBarAndNavigationBar;
                }
                if (is169) {
                    mTmpNavigationFrame.set(overscanLeft, 0, right, displayHeight);
                } else {
                    mTmpNavigationFrame.set(overscanLeft, 0, right, displayHeight);
                }
                i = mTmpNavigationFrame.right;
                this.mStableFullscreenLeft = i;
                this.mStableLeft = i;
                if (transientNavBarShowing) {
                    this.mNavigationBarController.setBarShowingLw(true);
                } else if (navVisible) {
                    this.mNavigationBarController.setBarShowingLw(true);
                    this.mDockLeft = mTmpNavigationFrame.right;
                    i = this.mDockLeft;
                    this.mRestrictedOverscanScreenLeft = i;
                    this.mRestrictedScreenLeft = i;
                    this.mRestrictedScreenWidth = this.mDockRight - this.mRestrictedScreenLeft;
                    this.mRestrictedOverscanScreenWidth = this.mDockRight - this.mRestrictedOverscanScreenLeft;
                } else {
                    this.mNavigationBarController.setBarShowingLw(statusBarExpandedNotKeyguard);
                }
                if (!(!navVisible || (navTranslucent ^ 1) == 0 || (navAllowedHidden ^ 1) == 0 || (this.mNavigationBar.isAnimatingLw() ^ 1) == 0 || (this.mNavigationBarController.wasRecentlyTranslucent() ^ 1) == 0)) {
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
            if (this.mNavigationBarController.checkHiddenLw()) {
                return true;
            }
        }
        return false;
    }

    private int getNavigationBarWidthForLayout(int rotation, int uiMode) {
        if (this.mNavigationBarEnableStatus == 2) {
            return this.mNavigationBarHeightForRotationGestrue[rotation];
        }
        return this.mNavigationBarWidthForRotationDefault[rotation];
    }

    private int getNavigationBarHeightForLayout(int rotation, int uiMode) {
        if (this.mNavigationBarEnableStatus == 2) {
            return this.mNavigationBarHeightForRotationGestrue[rotation];
        }
        return this.mNavigationBarHeightForRotationDefault[rotation];
    }

    private int getNavigationBarWidthForCompat(int rotation, int uiMode) {
        if (this.mHideNavigationBar) {
            return 0;
        }
        if (isShowGestureBarPlaceholder()) {
            return this.mNavigationBarHeightForRotationGestrue[rotation];
        }
        return this.mNavigationBarWidthForRotationDefault[rotation];
    }

    private int getNavigationBarHeightForCompat(int rotation, int uiMode) {
        if (this.mHideNavigationBar) {
            return 0;
        }
        if (isShowGestureBarPlaceholder()) {
            return this.mNavigationBarHeightForRotationGestrue[rotation];
        }
        return this.mNavigationBarHeightForRotationDefault[rotation];
    }

    private void updateNavigationBarHideState() {
        boolean z = true;
        if (1 != SystemProperties.getInt("oppo.hide.navigationbar", 0)) {
            z = false;
        }
        this.mHideNavigationBar = z;
        if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
            Slog.i(TAG, "updateNavigationBarHideState mNavigationBarEnableStatus: " + this.mNavigationBarEnableStatus + " mHoverNavigationBarStatus:" + this.mHoverNavigationBarStatus + " mHideNavigationBar:" + this.mHideNavigationBar);
        }
    }

    private boolean layoutNavigationBar(int displayWidth, int displayHeight, int displayRotation, int uiMode, int overscanLeft, int overscanRight, int overscanBottom, Rect dcf, boolean navVisible, boolean navTranslucent, boolean navAllowedHidden, boolean statusBarExpandedNotKeyguard) {
        int naturalHeight = this.mNaturalHeight;
        boolean isIgnoreComputeFrame = false;
        boolean isNomWindow = false;
        if (displayRotation == 0 || displayRotation == 2) {
            if (displayHeight == -1) {
                displayHeight = naturalHeight;
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
            updateNavigationBarHideState();
            this.mNavigationBarPosition = navigationBarPosition(displayWidth, displayHeight, displayRotation);
            if (this.mNavigationBarPosition == 4) {
                int top = (displayHeight - overscanBottom) - getNavigationBarHeight(displayRotation, uiMode);
                int layoutTop = (displayHeight - overscanBottom) - getNavigationBarHeightForLayout(displayRotation, uiMode);
                mTmpNavigationFrameForGesture.set(0, layoutTop, displayWidth, displayHeight - overscanBottom);
                if (this.mHasNavigationBar && isShowGestureBarPlaceholder()) {
                    top = layoutTop;
                } else if (this.mHasNavigationBar && this.mHideNavigationBar) {
                    top = displayHeight - overscanBottom;
                }
                mTmpNavigationFrame.set(0, top, displayWidth, displayHeight - overscanBottom);
                i = mTmpNavigationFrame.top;
                this.mStableFullscreenBottom = i;
                this.mStableBottom = i;
                if (transientNavBarShowing) {
                    this.mNavigationBarController.setBarShowingLw(true);
                } else if (navVisible) {
                    this.mNavigationBarController.setBarShowingLw(true);
                    this.mDockBottom = mTmpNavigationFrame.top;
                    this.mRestrictedScreenHeight = this.mDockBottom - this.mRestrictedScreenTop;
                    this.mRestrictedOverscanScreenHeight = this.mDockBottom - this.mRestrictedOverscanScreenTop;
                } else {
                    this.mNavigationBarController.setBarShowingLw(statusBarExpandedNotKeyguard);
                }
                if (!(!navVisible || (navTranslucent ^ 1) == 0 || (navAllowedHidden ^ 1) == 0 || (this.mNavigationBar.isAnimatingLw() ^ 1) == 0 || (this.mNavigationBarController.wasRecentlyTranslucent() ^ 1) == 0)) {
                    this.mSystemBottom = mTmpNavigationFrame.top;
                }
            } else if (this.mNavigationBarPosition == 2) {
                int left = (displayWidth - overscanRight) - getNavigationBarWidth(displayRotation, uiMode);
                int layoutLeft = (displayWidth - overscanRight) - getNavigationBarWidthForLayout(displayRotation, uiMode);
                mTmpNavigationFrameForGesture.set(layoutLeft, 0, displayWidth - overscanRight, displayHeight);
                if (this.mHasNavigationBar && isShowGestureBarPlaceholder()) {
                    left = layoutLeft;
                } else if (this.mHasNavigationBar && this.mHideNavigationBar) {
                    left = displayWidth - overscanRight;
                }
                mTmpNavigationFrame.set(left, 0, displayWidth - overscanRight, displayHeight);
                i = mTmpNavigationFrame.left;
                this.mStableFullscreenRight = i;
                this.mStableRight = i;
                if (transientNavBarShowing) {
                    this.mNavigationBarController.setBarShowingLw(true);
                } else if (navVisible) {
                    this.mNavigationBarController.setBarShowingLw(true);
                    this.mDockRight = mTmpNavigationFrame.left;
                    this.mRestrictedScreenWidth = this.mDockRight - this.mRestrictedScreenLeft;
                    this.mRestrictedOverscanScreenWidth = this.mDockRight - this.mRestrictedOverscanScreenLeft;
                } else {
                    this.mNavigationBarController.setBarShowingLw(statusBarExpandedNotKeyguard);
                }
                if (!(!navVisible || (navTranslucent ^ 1) == 0 || (navAllowedHidden ^ 1) == 0 || (this.mNavigationBar.isAnimatingLw() ^ 1) == 0 || (this.mNavigationBarController.wasRecentlyTranslucent() ^ 1) == 0)) {
                    this.mSystemRight = mTmpNavigationFrame.left;
                }
            } else if (this.mNavigationBarPosition == 1) {
                int right = overscanLeft + getNavigationBarWidth(displayRotation, uiMode);
                int layoutRight = overscanLeft + getNavigationBarWidthForLayout(displayRotation, uiMode);
                mTmpNavigationFrameForGesture.set(overscanLeft, 0, layoutRight, displayHeight);
                if (this.mHasNavigationBar && isShowGestureBarPlaceholder()) {
                    right = layoutRight;
                } else if (this.mHasNavigationBar && this.mHideNavigationBar) {
                    right = overscanLeft;
                }
                mTmpNavigationFrame.set(overscanLeft, 0, right, displayHeight);
                i = mTmpNavigationFrame.right;
                this.mStableFullscreenLeft = i;
                this.mStableLeft = i;
                if (transientNavBarShowing) {
                    this.mNavigationBarController.setBarShowingLw(true);
                } else if (navVisible) {
                    this.mNavigationBarController.setBarShowingLw(true);
                    this.mDockLeft = mTmpNavigationFrame.right;
                    i = this.mDockLeft;
                    this.mRestrictedOverscanScreenLeft = i;
                    this.mRestrictedScreenLeft = i;
                    this.mRestrictedScreenWidth = this.mDockRight - this.mRestrictedScreenLeft;
                    this.mRestrictedOverscanScreenWidth = this.mDockRight - this.mRestrictedOverscanScreenLeft;
                } else {
                    this.mNavigationBarController.setBarShowingLw(statusBarExpandedNotKeyguard);
                }
                if (!(!navVisible || (navTranslucent ^ 1) == 0 || (navAllowedHidden ^ 1) == 0 || (this.mNavigationBar.isAnimatingLw() ^ 1) == 0 || (this.mNavigationBarController.wasRecentlyTranslucent() ^ 1) == 0)) {
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
            if (!(isIgnoreComputeFrame || (isNomWindow ^ 1) == 0)) {
                this.mNavigationBar.computeFrameLw(mTmpNavigationFrameForGesture, mTmpNavigationFrameForGesture, mTmpNavigationFrameForGesture, mTmpNavigationFrameForGesture, mTmpNavigationFrameForGesture, dcf, mTmpNavigationFrameForGesture, mTmpNavigationFrameForGesture);
                if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
                    Slog.i(TAG, "mNavigationBar frame: " + mTmpNavigationFrame + " mTmpNavigationFrameForGesture:" + mTmpNavigationFrameForGesture);
                }
            }
            if (this.mNavigationBarController.checkHiddenLw()) {
                return true;
            }
        }
        return false;
    }

    private int navigationBarPosition(int displayWidth, int displayHeight, int displayRotation) {
        if (!this.mNavigationBarCanMove || displayWidth <= displayHeight) {
            return 4;
        }
        if (displayRotation == 3) {
            return 1;
        }
        return 2;
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
        if ((fl & 1024) != 0 || isForceHidesStatusBar()) {
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
        return (((win.getAttrs().flags & 8) != 0) ^ ((win.getAttrs().flags & DumpState.DUMP_INTENT_FILTER_VERIFIERS) != 0)) ^ 1;
    }

    public void reLayoutInputMethod(WindowState win, WindowState attached) {
        if ((win != this.mStatusBar || (canReceiveInput(win) ^ 1) == 0) && win != this.mNavigationBar) {
            WindowManager.LayoutParams attrs = win.getAttrs();
            boolean isDefaultDisplay = win.isDefaultDisplay();
            if (attrs.type == 2011 && win.isVisibleLw() && win.isDisplayedLw() && (win.getGivenInsetsPendingLw() ^ 1) != 0) {
                setLastInputMethodWindowLw(null, null);
                offsetInputMethodWindowLw(win);
            }
        }
    }

    public void layoutWindowLw(WindowState win, WindowState attached) {
        if ((win != this.mStatusBar || (canReceiveInput(win) ^ 1) == 0) && win != this.mNavigationBar) {
            boolean hasNavBar;
            Rect osf;
            WindowManager.LayoutParams attrs = win.getAttrs();
            boolean isDefaultDisplay = win.isDefaultDisplay();
            boolean needsToOffsetInputMethodTarget = isDefaultDisplay ? win == this.mLastInputMethodTargetWindow && this.mLastInputMethodWindow != null : false;
            if (needsToOffsetInputMethodTarget) {
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
                            if (this.mNavigationBarPosition == 2) {
                                i = this.mStableRight;
                                vf.right = i;
                                cf.right = i;
                                of.right = i;
                                df.right = i;
                                pf.right = i;
                            } else if (this.mNavigationBarPosition == 1) {
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
                } else if (win == this.mStatusBar) {
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
                    if (win == this.mTopFullscreenOpaqueWindowState) {
                        int isAnimatingLw = win.isAnimatingLw() ^ 1;
                    }
                    if (isAppWindow && (inheritTranslucentDecor ^ 1) != 0) {
                        if ((sysUiFl & 4) == 0 && (isForceHidesStatusBar() ^ 1) != 0 && (fl & 1024) == 0 && (67108864 & fl) == 0 && (Integer.MIN_VALUE & fl) == 0 && (DumpState.DUMP_INTENT_FILTER_VERIFIERS & pfl) == 0) {
                            dcf.top = this.mStableTop;
                        }
                        if ((134217728 & fl) == 0 && (sysUiFl & 2) == 0 && ((Integer.MIN_VALUE & fl) == 0 || (attrs.navigationBarVisibility & 536870912) != 0)) {
                            dcf.bottom = this.mStableBottom;
                            dcf.right = this.mStableRight;
                        }
                    }
                    if ((65792 & fl) == 65792) {
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
                            } else if ((SYSTEM_UI_FLAG_APP_CUSTOM_NAVIGATION_BAR & fl) != 0 && attrs.type >= 1 && attrs.type <= 1999) {
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
                            } else if (!canHideNavigationBar() || (sysUiFl & 512) == 0 || attrs.type < 1 || attrs.type > 1999) {
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
                    } else if ((fl & 256) != 0 || (sysUiFl & UsbTerminalTypes.TERMINAL_EXTERN_UNDEFINED) != 0) {
                        if (attrs.type == 2014 || attrs.type == 2017) {
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
                        } else if (attrs.type == 2020) {
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
                        } else if (attrs.type == 2019 || attrs.type == 2024 || attrs.type == 2312 || attrs.type == 2311) {
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
                        } else if ((SYSTEM_UI_FLAG_APP_CUSTOM_NAVIGATION_BAR & fl) != 0 && attrs.type >= 1 && attrs.type <= 1999) {
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
                        } else if (canHideNavigationBar() && (sysUiFl & 512) != 0 && (attrs.type == OppoArpPeer.ARP_FIRST_RESPONSE_TIMEOUT || attrs.type == 2005 || attrs.type == 2034 || attrs.type == 2033 || (attrs.type >= 1 && attrs.type <= 1999))) {
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
                        setAttachedWindowFrames(win, fl, adjust, attached, false, pf, df, of, cf, vf);
                    } else if (attrs.type == 2014 || attrs.type == 2020) {
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
            if (!((fl & 512) == 0 || attrs.type == 2010 || (win.isInMultiWindowMode() ^ 1) == 0)) {
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
                }
            } else {
                osf = null;
            }
            win.computeFrameLw(pf, df, of, cf, vf, dcf, sf, osf);
            if (attrs.type == 2011 && win.isVisibleLw() && (win.getGivenInsetsPendingLw() ^ 1) != 0) {
                setLastInputMethodWindowLw(null, null);
                offsetInputMethodWindowLw(win);
            }
            if (attrs.type == 2031 && win.isVisibleLw() && (win.getGivenInsetsPendingLw() ^ 1) != 0) {
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
        this.mTopFullscreenOpaqueWindowState = null;
        this.mTopFullscreenOpaqueOrDimmingWindowState = null;
        this.mTopDockedOpaqueWindowState = null;
        this.mTopDockedWindowState = null;
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

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "JianHui.Yu@ROM.SDK, 2017-12-10 : Modify for Longshot 5.0", property = OppoRomType.ROM)
    public void applyPostLayoutPolicyLw(WindowState win, WindowManager.LayoutParams attrs, WindowState attached, WindowState imeTarget) {
        boolean affectsSystemUi = win.canAffectSystemUiFlags();
        applyKeyguardPolicyLw(win, imeTarget);
        int fl = PolicyControl.getWindowFlags(win, attrs);
        if (this.mTopFullscreenOpaqueWindowState == null && affectsSystemUi && attrs.type == 2011) {
            this.mForcingShowNavBar = true;
            this.mForcingShowNavBarLayer = win.getSurfaceLayer();
        }
        if (attrs.type == OppoArpPeer.ARP_FIRST_RESPONSE_TIMEOUT) {
            if ((attrs.privateFlags & 1024) != 0) {
                this.mForceStatusBarFromKeyguard = true;
            }
            if ((attrs.privateFlags & 4096) != 0) {
                this.mForceStatusBarTransparent = true;
            }
        }
        boolean appWindow = attrs.type >= 1 ? attrs.type < OppoArpPeer.ARP_FIRST_RESPONSE_TIMEOUT : false;
        if (ColorWindowManager.LayoutParams.isSpecialAppWindow(attrs)) {
            appWindow = true;
            if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
                Log.d(TAG, "set SpecialAppWindow " + attrs);
            }
        }
        int stackId = win.getStackId();
        if (this.mTopFullscreenOpaqueWindowState == null && affectsSystemUi) {
            if ((fl & 2048) != 0) {
                this.mForceStatusBar = true;
            }
            if (attrs.type == 2023 && (!this.mDreamingLockscreen || (win.isVisibleLw() && win.hasDrawnLw()))) {
                this.mShowingDream = true;
                appWindow = true;
            }
            if (appWindow && attached == null && attrs.isFullscreen() && StackId.normallyFullscreenWindows(stackId)) {
                this.mTopFullscreenOpaqueWindowState = win;
                if (this.mTopFullscreenOpaqueOrDimmingWindowState == null) {
                    this.mTopFullscreenOpaqueOrDimmingWindowState = win;
                }
                if ((fl & 1) != 0) {
                    this.mAllowLockscreenWhenOn = true;
                }
            }
        }
        if (affectsSystemUi && win.getAttrs().type == 2031) {
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
        if (this.mTopFullscreenOpaqueOrDimmingWindowState == null && affectsSystemUi && win.isDimming() && StackId.normallyFullscreenWindows(stackId)) {
            this.mTopFullscreenOpaqueOrDimmingWindowState = win;
        }
        if (this.mTopDockedOpaqueWindowState == null && affectsSystemUi && appWindow && attached == null && attrs.isFullscreen() && stackId == 3) {
            this.mTopDockedOpaqueWindowState = win;
            if (this.mTopDockedOpaqueOrDimmingWindowState == null) {
                this.mTopDockedOpaqueOrDimmingWindowState = win;
            }
        }
        if (this.mTopDockedWindowState == null && affectsSystemUi && appWindow && attached == null && stackId == 3) {
            this.mTopDockedWindowState = win;
        }
        if (this.mTopDockedOpaqueOrDimmingWindowState == null && affectsSystemUi && win.isDimming() && stackId == 3) {
            this.mTopDockedOpaqueOrDimmingWindowState = win;
        }
        if (win.isVisibleLw() && (attrs.privateFlags & DumpState.DUMP_COMPILER_STATS) != 0 && win.canAcquireSleepToken()) {
            this.mWindowSleepTokenNeeded = true;
        }
    }

    private void applyKeyguardPolicyLw(WindowState win, WindowState imeTarget) {
        if (!canBeHiddenByKeyguardLw(win)) {
            return;
        }
        if (shouldBeHiddenByKeyguard(win, imeTarget)) {
            win.hideLw(false);
        } else {
            win.showLw(false);
        }
    }

    public int finishPostLayoutPolicyLw() {
        IStatusBarService statusbar;
        int changes = 0;
        boolean topIsFullscreen = false;
        boolean topActivityIsFullscreen = false;
        if (this.mTopFullscreenOpaqueWindowState != null) {
            WindowManager.LayoutParams attrs = this.mTopFullscreenOpaqueWindowState.getAttrs();
        }
        if (!this.mShowingDream) {
            this.mDreamingLockscreen = isKeyguardShowingAndNotOccluded();
            if (this.mDreamingSleepTokenNeeded) {
                this.mDreamingSleepTokenNeeded = false;
                this.mHandler.obtainMessage(15, 0, 1).sendToTarget();
            }
        } else if (!this.mDreamingSleepTokenNeeded) {
            this.mDreamingSleepTokenNeeded = true;
            this.mHandler.obtainMessage(15, 1, 1).sendToTarget();
        }
        if (this.mStatusBar != null) {
            int shouldBeTransparent;
            if (!this.mForceStatusBarTransparent || (this.mForceStatusBar ^ 1) == 0) {
                shouldBeTransparent = 0;
            } else {
                shouldBeTransparent = this.mForceStatusBarFromKeyguard ^ 1;
            }
            if (shouldBeTransparent == 0) {
                this.mStatusBarController.setShowTransparent(false);
            } else if (!this.mStatusBar.isVisibleLw()) {
                this.mStatusBarController.setShowTransparent(true);
            }
            WindowManager.LayoutParams statusBarAttrs = this.mStatusBar.getAttrs();
            boolean statusBarExpanded = statusBarAttrs.height == -1 ? statusBarAttrs.width == -1 : false;
            boolean topAppHidesStatusBar = topAppHidesStatusBar();
            if ((this.mForceStatusBar && (isForceHidesStatusBar() ^ 1) != 0) || this.mForceStatusBarFromKeyguard || this.mForceStatusBarTransparent || statusBarExpanded) {
                if (this.mStatusBarController.setBarShowingLw(true)) {
                    changes = 1;
                }
                topIsFullscreen = this.mTopIsFullscreen ? this.mStatusBar.isAnimatingLw() : false;
                if ((this.mForceStatusBarFromKeyguard || statusBarExpanded) && this.mStatusBarController.isTransientShowing()) {
                    this.mStatusBarController.updateVisibilityLw(false, this.mLastSystemUiFlags, this.mLastSystemUiFlags);
                }
                if (statusBarExpanded && this.mNavigationBar != null && this.mNavigationBarController.setBarShowingLw(true)) {
                    changes |= 1;
                }
            } else if (this.mTopFullscreenOpaqueWindowState != null) {
                topIsFullscreen = topAppHidesStatusBar;
                if (this.mStatusBarController.isTransientShowing()) {
                    if (this.mStatusBarController.setBarShowingLw(true)) {
                        changes = 1;
                    }
                } else if (!topAppHidesStatusBar || (this.mWindowManagerInternal.isStackVisible(3) ^ 1) == 0) {
                    if (isForceHidesStatusBar()) {
                        if (this.mStatusBarController.setBarShowingLw(false)) {
                            changes = 1;
                        } else if (DEBUG_PANIC) {
                            Slog.v(TAG, "Status bar already hiding");
                        }
                        topAppHidesStatusBar = true;
                    } else {
                        if (this.mStatusBarController.setBarShowingLw(true)) {
                            changes = 1;
                        }
                        topAppHidesStatusBar = false;
                    }
                } else if (this.mStatusBarController.setBarShowingLw(false)) {
                    changes = 1;
                } else if (DEBUG_PANIC) {
                    Slog.v(TAG, "Status bar already hiding");
                }
            }
            this.mStatusBarController.setTopAppHidesStatusBar(topAppHidesStatusBar);
            if (statusBarExpanded) {
                topActivityIsFullscreen = topAppHidesStatusBar;
            } else {
                topActivityIsFullscreen = topIsFullscreen;
            }
        }
        if (this.mTopActivityIsFullscreen != topActivityIsFullscreen) {
            try {
                statusbar = getStatusBarService();
                Slog.i(TAG, "TopFullScreen status bar state changed, topIsFullscreen:" + topIsFullscreen + " topWin:" + this.mTopFullscreenOpaqueWindowState + " topActivityIsFullscreen:" + topActivityIsFullscreen);
                if (statusbar != null) {
                    statusbar.topIsFullscreen(topActivityIsFullscreen);
                }
            } catch (RemoteException e) {
                Slog.e(TAG, "RemoteException when notify topIsFullscreen" + e);
                this.mStatusBarService = null;
            }
            this.mTopActivityIsFullscreen = topActivityIsFullscreen;
        }
        if (this.mTopIsFullscreen != topIsFullscreen) {
            if (!topIsFullscreen) {
                changes |= 1;
            }
            try {
                statusbar = getStatusBarService();
                Slog.i(TAG, "finishPostLayoutPolicyLw (statusbar != null ) " + (statusbar != null) + " topIsFullscreen " + topIsFullscreen + " top = " + this.mTopFullscreenOpaqueWindowState + " topActivityIsFullscreen " + topActivityIsFullscreen);
                if (statusbar != null) {
                    statusbar.topIsFullscreen(topActivityIsFullscreen);
                }
                this.mTopActivityIsFullscreen = topActivityIsFullscreen;
            } catch (RemoteException e2) {
                Slog.e(TAG, "RemoteException when  notify topIsFullscreen", e2);
                this.mStatusBarService = null;
            }
            Bundle result = new Bundle();
            result.putBoolean(FULL_WINDOW, topIsFullscreen);
            this.mWindowManagerInternal.notifyWindowStateChange(result);
            this.mTopIsFullscreen = topIsFullscreen;
        }
        if ((updateSystemUiVisibilityLw() & SYSTEM_UI_CHANGING_LAYOUT) != 0) {
            changes |= 1;
        }
        if (this.mShowingDream != this.mLastShowingDream) {
            this.mLastShowingDream = this.mShowingDream;
            this.mWindowManagerFuncs.notifyShowingDreamChanged();
        }
        updateWindowSleepToken();
        updateLockScreenTimeout();
        return changes;
    }

    private void updateWindowSleepToken() {
        if (this.mWindowSleepTokenNeeded && (this.mLastWindowSleepTokenNeeded ^ 1) != 0) {
            this.mHandler.removeCallbacks(this.mReleaseSleepTokenRunnable);
            this.mHandler.post(this.mAcquireSleepTokenRunnable);
        } else if (!this.mWindowSleepTokenNeeded && this.mLastWindowSleepTokenNeeded) {
            this.mHandler.removeCallbacks(this.mAcquireSleepTokenRunnable);
            this.mHandler.post(this.mReleaseSleepTokenRunnable);
        }
        this.mLastWindowSleepTokenNeeded = this.mWindowSleepTokenNeeded;
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "JianHui.Yu@ROM.SDK, 2018-04-28 : Modify for Longshot 5.0", property = OppoRomType.ROM)
    private boolean topAppHidesStatusBar() {
        boolean z = true;
        if (this.mTopFullscreenOpaqueWindowState == null) {
            return false;
        }
        if (ColorWindowManager.updateSpecialSystemBar(this.mTopFullscreenOpaqueWindowState.getAttrs())) {
            return this.mTopIsFullscreen;
        }
        int fl = PolicyControl.getWindowFlags(null, this.mTopFullscreenOpaqueWindowState.getAttrs());
        if (DEBUG_PANIC) {
            String str = TAG;
            StringBuilder append = new StringBuilder().append("topAppHidesStatusBar win:").append(this.mTopFullscreenOpaqueWindowState).append(" attr: ").append(this.mTopFullscreenOpaqueWindowState.getAttrs()).append(" mLastSystemUiFlags:").append(Integer.toHexString(this.mLastSystemUiFlags)).append(" fl:").append(Integer.toHexString(fl)).append(" isTopAppHideStatusBar:");
            boolean z2 = (fl & 1024) == 0 ? (this.mLastSystemUiFlags & 4) != 0 : true;
            Slog.v(str, append.append(z2).toString());
        }
        if ((fl & 1024) == 0 && (this.mLastSystemUiFlags & 4) == 0) {
            z = false;
        }
        return z;
    }

    private boolean setKeyguardOccludedLw(boolean isOccluded, boolean force) {
        boolean wasOccluded = this.mKeyguardOccluded;
        boolean showing = this.mKeyguardDelegate.isShowing();
        boolean changed = wasOccluded == isOccluded ? force : true;
        WindowManager.LayoutParams attrs;
        if (!isOccluded && changed && showing) {
            this.mKeyguardOccluded = false;
            this.mKeyguardDelegate.setOccluded(false, true);
            if (this.mStatusBar != null) {
                attrs = this.mStatusBar.getAttrs();
                attrs.privateFlags |= 1024;
                if (!this.mKeyguardDelegate.hasLockscreenWallpaper()) {
                    attrs = this.mStatusBar.getAttrs();
                    attrs.flags |= DumpState.DUMP_DEXOPT;
                }
            }
            return true;
        } else if (isOccluded && changed && showing) {
            this.mKeyguardOccluded = true;
            this.mKeyguardDelegate.setOccluded(true, false);
            if (this.mStatusBar != null) {
                attrs = this.mStatusBar.getAttrs();
                attrs.privateFlags &= -1025;
                attrs = this.mStatusBar.getAttrs();
                attrs.flags &= -1048577;
            }
            return true;
        } else if (!changed) {
            return false;
        } else {
            this.mKeyguardOccluded = isOccluded;
            this.mKeyguardDelegate.setOccluded(isOccluded, false);
            return false;
        }
    }

    private boolean isStatusBarKeyguard() {
        if (this.mStatusBar == null || (this.mStatusBar.getAttrs().privateFlags & 1024) == 0) {
            return false;
        }
        return true;
    }

    public boolean allowAppAnimationsLw() {
        if (this.mShowingDream) {
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
        int lensCoverState = lensCovered ? 1 : 0;
        if (this.mCameraLensCoverState != lensCoverState) {
            if (this.mCameraLensCoverState == 1 && lensCoverState == 0) {
                boolean keyguardActive;
                Intent intent;
                if (this.mKeyguardDelegate == null) {
                    keyguardActive = false;
                } else {
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
            Slog.v(TAG, "Broadcasting Intent for HDMI HPD :" + plugged);
            this.mHdmiPlugged = plugged;
            updateRotation(true, true);
            Intent intent = new Intent("android.intent.action.HDMI_PLUGGED");
            intent.addFlags(67108864);
            intent.putExtra("state", plugged);
            this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:30:0x009a A:{SYNTHETIC, Splitter: B:30:0x009a} */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x0077 A:{SYNTHETIC, Splitter: B:23:0x0077} */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x00a3 A:{SYNTHETIC, Splitter: B:35:0x00a3} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void initializeHdmiState() {
        IOException ex;
        NumberFormatException ex2;
        Throwable th;
        boolean plugged = false;
        this.mExtEventObserver.startObserving("mdss_mdp/drm/card");
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
                    this.mHdmiPlugged = plugged ^ 1;
                    setHdmiPlugged(this.mHdmiPlugged ^ 1);
                } catch (NumberFormatException e3) {
                    ex2 = e3;
                    reader = reader2;
                    try {
                        Slog.w(TAG, "Couldn't read hdmi state from /sys/class/switch/hdmi/state: " + ex2);
                        if (reader != null) {
                        }
                        this.mHdmiPlugged = plugged ^ 1;
                        setHdmiPlugged(this.mHdmiPlugged ^ 1);
                    } catch (Throwable th2) {
                        th = th2;
                        if (reader != null) {
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    reader = reader2;
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e4) {
                        }
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
                this.mHdmiPlugged = plugged ^ 1;
                setHdmiPlugged(this.mHdmiPlugged ^ 1);
            } catch (NumberFormatException e7) {
                ex2 = e7;
                Slog.w(TAG, "Couldn't read hdmi state from /sys/class/switch/hdmi/state: " + ex2);
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e8) {
                    }
                }
                this.mHdmiPlugged = plugged ^ 1;
                setHdmiPlugged(this.mHdmiPlugged ^ 1);
            }
        }
        this.mHdmiPlugged = plugged ^ 1;
        setHdmiPlugged(this.mHdmiPlugged ^ 1);
    }

    /* JADX WARNING: Missing block: B:12:0x004a, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    @OppoHook(level = OppoHookType.CHANGE_ACCESS, note = "JianHui.Yu@ROM.SDK, 2017-10-16 : [-private] Modify for Longshot 5.0", property = OppoRomType.ROM)
    void takeScreenshot(final int screenshotType) {
        synchronized (this.mScreenshotLock) {
            if (this.mScreenshotConnection != null) {
                return;
            }
            ComponentName serviceComponent = new ComponentName("com.android.systemui", SYSUI_SCREENSHOT_SERVICE);
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

    private void notifyScreenshotError() {
        ComponentName errorComponent = new ComponentName("com.android.systemui", SYSUI_SCREENSHOT_ERROR_RECEIVER);
        Intent errorIntent = new Intent("android.intent.action.USER_PRESENT");
        errorIntent.setComponent(errorComponent);
        errorIntent.addFlags(335544320);
        this.mContext.sendBroadcastAsUser(errorIntent, UserHandle.CURRENT);
    }

    public int interceptKeyBeforeQueueing(KeyEvent event, int policyFlags) {
        if (!this.mSystemBooted) {
            return 0;
        }
        boolean keyguardActive;
        int isWakeKey;
        int result;
        boolean interactive = (536870912 & policyFlags) != 0;
        boolean down = event.getAction() == 0;
        boolean canceled = event.isCanceled();
        int keyCode = event.getKeyCode();
        boolean isInjected = (16777216 & policyFlags) != 0;
        if (this.mKeyguardDelegate == null) {
            keyguardActive = false;
        } else if (interactive) {
            keyguardActive = isKeyguardShowingAndNotOccluded();
        } else {
            keyguardActive = this.mKeyguardDelegate.isShowing();
        }
        if (keyCode == 187 && (down ^ 1) != 0) {
            this.mHandler.removeCallbacks(this.mRecentsStartSplitSreen);
        }
        if ((policyFlags & 1) == 0) {
            isWakeKey = event.isWakeKey();
        } else {
            isWakeKey = 1;
        }
        if (interactive || (isInjected && (isWakeKey ^ 1) != 0)) {
            result = 1;
            isWakeKey = 0;
            if (interactive) {
                if (keyCode == this.mPendingWakeKey && (down ^ 1) != 0) {
                    result = 0;
                }
                this.mPendingWakeKey = -1;
            }
        } else if (interactive || !shouldDispatchInputWhenNonInteractive(event)) {
            result = 0;
            if (!(isWakeKey == 0 || (down && (isWakeKeyWhenScreenOff(keyCode) ^ 1) == 0))) {
                isWakeKey = 0;
            }
            if (isWakeKey != 0 && down) {
                this.mPendingWakeKey = keyCode;
            }
        } else {
            result = 1;
            this.mPendingWakeKey = -1;
        }
        if (isValidGlobalKey(keyCode) && this.mGlobalKeyManager.shouldHandleGlobalKey(keyCode, event)) {
            if (isWakeKey != 0) {
                wakeUp(event.getEventTime(), this.mAllowTheaterModeWakeFromKey, "android.policy:KEY");
            }
            return result;
        }
        boolean useHapticFeedback = (!down || (policyFlags & 2) == 0) ? false : event.getRepeatCount() == 0;
        if (InputLog.DEBUG) {
            Log.d(TAG, "interceptKeyTq keycode=" + keyCode + " interactive=" + interactive + " keyguardActive=" + keyguardActive + " policyFlags=" + Integer.toHexString(policyFlags) + " down =" + down + " canceled = " + canceled + " isWakeKey=" + isWakeKey + " mVolumeDownKeyTriggered =" + this.mScreenshotChordVolumeDownKeyTriggered + " mVolumeUpKeyTriggered =" + this.mA11yShortcutChordVolumeUpKeyTriggered + " result = " + result + " useHapticFeedback = " + useHapticFeedback + " isInjected = " + isInjected);
        }
        TelecomManager telecomManager;
        Message msg;
        switch (keyCode) {
            case 3:
            case 803:
                if (down && event.getDownTime() - event.getEventTime() == ((long) keyCode)) {
                    useHapticFeedback = false;
                }
                if (down) {
                    if (!interactive && event.getRepeatCount() == 0) {
                        this.mLastHomeDownTimeDuringPF = event.getDownTime();
                    }
                    if (interactive && event.getRepeatCount() == 0 && this.isTouchFingerPrintSensor) {
                        useHapticFeedback = true;
                    }
                    if (getFingerprintInternal() != null) {
                        getFingerprintInternal().onHomeKeyDown();
                    }
                } else {
                    if (getFingerprintInternal() != null) {
                        getFingerprintInternal().onHomeKeyUp();
                    }
                    if (this.mLastHomeDownTimeDuringPF == 0 || event.getDownTime() != this.mLastHomeDownTimeDuringPF) {
                        if (System.getInt(this.mContext.getContentResolver(), "drop_home_key_when_use_fingerprint", 0) == 1) {
                            Slog.d(TAG, "drop HOME_KEY Up when screen on due to finger print");
                            System.putInt(this.mContext.getContentResolver(), "drop_home_key_when_use_fingerprint", 0);
                            return 0;
                        }
                        this.mLastHomeDownTimeDuringPF = 0;
                    } else {
                        Slog.d(TAG, "Skip HOME_KEY Up when screen off due to finger print");
                        this.mLastHomeDownTimeDuringPF = 0;
                        return 0;
                    }
                }
                isWakeKey = 0;
                break;
            case 4:
                if (!down) {
                    if (interceptBackKeyUp(event)) {
                        result &= -2;
                        break;
                    }
                }
                interceptBackKeyDown();
                if (event.getDownTime() - event.getEventTime() == ((long) keyCode)) {
                    useHapticFeedback = false;
                }
                if (this.mDisplayManagerInternal.isBlockScreenOnByBiometrics()) {
                    useHapticFeedback = false;
                    break;
                }
                break;
            case 5:
                if (down) {
                    telecomManager = getTelecommService();
                    if (telecomManager != null && telecomManager.isRinging()) {
                        Log.i(TAG, "interceptKeyBeforeQueueing: CALL key-down while ringing: Answer the call!");
                        telecomManager.acceptRingingCall();
                        result &= -2;
                        break;
                    }
                }
                break;
            case 6:
                result &= -2;
                if (!down) {
                    if (!this.mEndCallKeyHandled) {
                        this.mHandler.removeCallbacks(this.mEndCallLongPress);
                        if (!canceled && (((this.mEndcallBehavior & 1) == 0 || !goHome()) && (this.mEndcallBehavior & 2) != 0)) {
                            goToSleep(event.getEventTime(), 4, 0);
                            isWakeKey = 0;
                            break;
                        }
                    }
                }
                telecomManager = getTelecommService();
                int hungUp = 0;
                if (telecomManager != null) {
                    hungUp = telecomManager.endCall();
                }
                if (interactive && (hungUp ^ 1) != 0) {
                    this.mEndCallKeyHandled = false;
                    this.mHandler.postDelayed(this.mEndCallLongPress, ViewConfiguration.get(this.mContext).getDeviceGlobalActionKeyTimeout());
                    break;
                }
                this.mEndCallKeyHandled = true;
                break;
                break;
            case 24:
            case 25:
            case 164:
                if (keyCode == 25) {
                    if (!down) {
                        this.mScreenshotChordVolumeDownKeyTriggered = false;
                        cancelPendingScreenshotChordAction();
                        cancelPendingAccessibilityShortcutAction();
                    } else if (interactive && (this.mScreenshotChordVolumeDownKeyTriggered ^ 1) != 0 && (event.getFlags() & 1024) == 0) {
                        this.mScreenshotChordVolumeDownKeyTriggered = true;
                        this.mScreenshotChordVolumeDownKeyTime = event.getDownTime();
                        this.mScreenshotChordVolumeDownKeyConsumed = false;
                        cancelPendingPowerKeyAction();
                        interceptScreenshotChord();
                        interceptAccessibilityShortcutChord();
                    }
                } else if (keyCode == 24) {
                    if (!down) {
                        this.mA11yShortcutChordVolumeUpKeyTriggered = false;
                        cancelPendingScreenshotChordAction();
                        cancelPendingAccessibilityShortcutAction();
                    } else if (interactive && (this.mA11yShortcutChordVolumeUpKeyTriggered ^ 1) != 0 && (event.getFlags() & 1024) == 0) {
                        this.mA11yShortcutChordVolumeUpKeyTriggered = true;
                        this.mA11yShortcutChordVolumeUpKeyTime = event.getDownTime();
                        this.mA11yShortcutChordVolumeUpKeyConsumed = false;
                        cancelPendingPowerKeyAction();
                        cancelPendingScreenshotChordAction();
                        interceptAccessibilityShortcutChord();
                    }
                }
                if (down) {
                    sendSystemKeyToStatusBarAsync(event.getKeyCode());
                    telecomManager = getTelecommService();
                    if (telecomManager != null && telecomManager.isRinging()) {
                        Log.i(TAG, "interceptKeyBeforeQueueing: VOLUME key-down while ringing: Silence ringer!");
                        telecomManager.silenceRinger();
                        result &= -2;
                        break;
                    }
                    int audioMode = 0;
                    try {
                        audioMode = getAudioService().getMode();
                    } catch (Exception e) {
                        Log.e(TAG, "Error getting AudioService in interceptKeyBeforeQueueing.", e);
                    }
                    boolean isInCall = (telecomManager == null || !telecomManager.isInCall()) ? audioMode == 3 : true;
                    if (isInCall && (result & 1) == 0) {
                        MediaSessionLegacyHelper.getHelper(this.mContext).sendVolumeKeyEvent(event, Integer.MIN_VALUE, false);
                        break;
                    }
                }
                if (!this.mUseTvRouting && !this.mHandleVolumeKeysInWM) {
                    if ((result & 1) == 0) {
                        MediaSessionLegacyHelper.getHelper(this.mContext).sendVolumeKeyEvent(event, Integer.MIN_VALUE, true);
                        break;
                    }
                }
                result |= 1;
                break;
                break;
            case 26:
                cancelPendingAccessibilityShortcutAction();
                result &= -2;
                isWakeKey = 0;
                if (!down) {
                    interceptPowerKeyUp(event, interactive, canceled);
                    break;
                }
                interceptPowerKeyDown(event, interactive);
                break;
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
                if (MediaSessionLegacyHelper.getHelper(this.mContext).isGlobalPriorityActive()) {
                    result &= -2;
                }
                if ((result & 1) == 0) {
                    this.mBroadcastWakeLock.acquire();
                    msg = this.mHandler.obtainMessage(3, new KeyEvent(event));
                    msg.setAsynchronous(true);
                    msg.sendToTarget();
                    break;
                }
                break;
            case HdmiCecKeycode.CEC_KEYCODE_VIDEO_ON_DEMAND /*82*/:
            case 882:
                if (down && event.getRepeatCount() == 0) {
                    useHapticFeedback = true;
                }
                if (this.mDisplayManagerInternal.isBlockScreenOnByBiometrics()) {
                    useHapticFeedback = false;
                    break;
                }
                break;
            case 171:
                if (this.mShortPressWindowBehavior == 1 && this.mPictureInPictureVisible) {
                    if (!down) {
                        showPictureInPictureMenu(event);
                    }
                    result &= -2;
                    break;
                }
            case 187:
            case 987:
                if (down && event.getRepeatCount() == 0) {
                    useHapticFeedback = true;
                }
                if (this.mDisplayManagerInternal.isBlockScreenOnByBiometrics()) {
                    useHapticFeedback = false;
                    break;
                }
                break;
            case NetdResponseCode.ClatdStatusResult /*223*/:
                result &= -2;
                isWakeKey = 0;
                if (!this.mPowerManager.isInteractive()) {
                    useHapticFeedback = false;
                }
                if (!down) {
                    sleepRelease(event.getEventTime());
                    break;
                }
                sleepPress(event.getEventTime());
                break;
            case 224:
                result &= -2;
                isWakeKey = 1;
                break;
            case 231:
                if ((result & 1) == 0 && (down ^ 1) != 0) {
                    this.mBroadcastWakeLock.acquire();
                    msg = this.mHandler.obtainMessage(12, keyguardActive ? 1 : 0, 0);
                    msg.setAsynchronous(true);
                    msg.sendToTarget();
                    break;
                }
            case 276:
                result &= -2;
                isWakeKey = 0;
                if (!down) {
                    this.mPowerManagerInternal.setUserInactiveOverrideFromWindowManager();
                    break;
                }
                break;
            case 280:
            case 281:
            case 282:
            case 283:
                result &= -2;
                interceptSystemNavigationKey(event);
                break;
        }
        if (useHapticFeedback) {
            performHapticFeedbackLw(null, 1, false);
        }
        if (!(isWakeKey == 0 || (this.mDisplayManagerInternal.isBlockScreenOnByBiometrics() ^ 1) == 0)) {
            wakeUp(event.getEventTime(), this.mAllowTheaterModeWakeFromKey, "android.policy:KEY");
        }
        return result;
    }

    private void interceptSystemNavigationKey(KeyEvent event) {
        if (event.getAction() != 1) {
            return;
        }
        if (!(this.mAccessibilityManager.isEnabled() && (this.mAccessibilityManager.sendFingerprintGesture(event.getKeyCode()) ^ 1) == 0) && areSystemNavigationKeysEnabled()) {
            sendSystemKeyToStatusBarAsync(event.getKeyCode());
        }
    }

    private void sendSystemKeyToStatusBar(int keyCode) {
        IStatusBarService statusBar = getStatusBarService();
        if (statusBar != null) {
            try {
                statusBar.handleSystemKey(keyCode);
            } catch (RemoteException e) {
            }
        }
    }

    private void sendSystemKeyToStatusBarAsync(int keyCode) {
        Message message = this.mHandler.obtainMessage(25, keyCode, 0);
        message.setAsynchronous(true);
        this.mHandler.sendMessage(message);
    }

    private static boolean isValidGlobalKey(int keyCode) {
        switch (keyCode) {
            case 26:
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
        if (this.mDisplayManagerInternal.isBlockScreenOnByBiometrics()) {
            return false;
        }
        boolean displayOff = this.mDisplay == null || this.mDisplay.getState() == 1;
        if (displayOff && (this.mHasFeatureWatch ^ 1) != 0) {
            return false;
        }
        if (isKeyguardShowingAndNotOccluded() && (displayOff ^ 1) != 0) {
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
                    } catch (Exception e) {
                        Log.e(TAG, "Error dispatching volume up in dispatchTvAudioEvent.", e);
                        break;
                    }
                case 25:
                    try {
                        getAudioService().adjustSuggestedStreamVolume(-1, Integer.MIN_VALUE, 4101, pkgName, TAG);
                        break;
                    } catch (Exception e2) {
                        Log.e(TAG, "Error dispatching volume down in dispatchTvAudioEvent.", e2);
                        break;
                    }
                case 164:
                    try {
                        if (event.getRepeatCount() == 0) {
                            getAudioService().adjustSuggestedStreamVolume(101, Integer.MIN_VALUE, 4101, pkgName, TAG);
                            break;
                        }
                    } catch (Exception e22) {
                        Log.e(TAG, "Error dispatching mute in dispatchTvAudioEvent.", e22);
                        break;
                    }
                    break;
            }
        }
    }

    void dispatchMediaKeyWithWakeLock(KeyEvent event) {
        if (this.mHavePendingMediaKeyRepeatWithWakeLock) {
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
        dispatchMediaKeyWithWakeLockToAudioService(KeyEvent.changeTimeRepeat(event, SystemClock.uptimeMillis(), 1, event.getFlags() | 128));
        this.mBroadcastWakeLock.release();
    }

    void dispatchMediaKeyWithWakeLockToAudioService(KeyEvent event) {
        if (this.mActivityManagerInternal.isSystemReady()) {
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

    /* JADX WARNING: Missing block: B:36:0x005a, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void requestTransientBars(WindowState swipeTarget) {
        synchronized (this.mWindowManagerFuncs.getWindowManagerLock()) {
            if (!isUserSetupComplete()) {
            } else if (swipeTarget == this.mNavigationBar && this.mNavigationBarEnableStatus == 2 && this.mHoverNavigationBarStatus == 1) {
            } else {
                int nb;
                boolean sb = this.mStatusBarController.checkShowTransientBarLw();
                if (this.mNavigationBarController.checkShowTransientBarLw()) {
                    nb = isNavBarEmpty(this.mLastSystemUiFlags) ^ 1;
                } else {
                    nb = 0;
                }
                if (sb || nb != 0) {
                    if (nb == 0 && swipeTarget == this.mNavigationBar) {
                        return;
                    }
                    if (sb) {
                        this.mStatusBarController.showTransient();
                    }
                    if (nb != 0) {
                        this.mNavigationBarController.showTransient();
                    }
                    this.mImmersiveModeConfirmation.confirmCurrentPrompt();
                    updateSystemUiVisibilityLw();
                }
            }
        }
    }

    public void startedGoingToSleep(int why) {
        if (DEBUG_WAKEUP) {
            Slog.i(TAG, "Started going to sleep... (why=" + why + ")");
        }
        this.mGoingToSleep = true;
        this.mRequestedOrGoingToSleep = true;
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
        this.mRequestedOrGoingToSleep = false;
        synchronized (this.mLock) {
            this.mAwake = false;
            updateWakeGestureListenerLp();
            updateOrientationListenerLp();
            updateLockScreenTimeout();
            this.mA11yShortcutChordVolumeUpKeyTriggered = false;
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
        if (this.mKeyguardDelegate != null) {
            this.mKeyguardDelegate.onFinishedWakingUp();
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
        reportScreenStateToVrManager(false);
        this.mHandler.removeCallbacks(this.mRecentsStartSplitSreen);
    }

    private long getKeyguardDrawnTimeout() {
        return (long) (((SystemServiceManager) LocalServices.getService(SystemServiceManager.class)).isBootCompleted() ? 1000 : OppoArpPeer.ARP_DUP_RESPONSE_TIMEOUT);
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
                this.mPhoneWinHandler.sendEmptyMessageDelayed(6, getKeyguardDrawnTimeout());
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
        synchronized (this.mLock) {
            if (this.mKeyguardDelegate != null) {
                this.mKeyguardDelegate.onScreenTurnedOn();
            }
        }
        reportScreenStateToVrManager(true);
    }

    public void screenTurningOff(ScreenOffListener screenOffListener) {
        this.mWindowManagerFuncs.screenTurningOff(screenOffListener);
        synchronized (this.mLock) {
            if (this.mKeyguardDelegate != null) {
                this.mKeyguardDelegate.onScreenTurningOff();
            }
        }
    }

    private void reportScreenStateToVrManager(boolean isScreenOn) {
        if (this.mVrManagerInternal != null) {
            this.mVrManagerInternal.onScreenStateChanged(isScreenOn);
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

    /* JADX WARNING: Missing block: B:23:0x0079, code:
            return;
     */
    /* JADX WARNING: Missing block: B:39:0x00a9, code:
            if (r1 == null) goto L_0x00ae;
     */
    /* JADX WARNING: Missing block: B:40:0x00ab, code:
            r1.onScreenOn();
     */
    /* JADX WARNING: Missing block: B:41:0x00ae, code:
            if (r0 == false) goto L_0x00b5;
     */
    /* JADX WARNING: Missing block: B:43:?, code:
            r7.mWindowManager.enableScreenIfNeeded();
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
            if (this.mScreenOnFully || (this.mScreenOnEarly ^ 1) != 0 || (this.mWindowManagerDrawComplete ^ 1) != 0 || (this.mAwake && (this.mKeyguardDrawComplete ^ 1) != 0)) {
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
        boolean z;
        synchronized (this.mLock) {
            z = this.mScreenOnEarly;
        }
        return z;
    }

    public boolean okToAnimate() {
        return this.mAwake ? this.mGoingToSleep ^ 1 : false;
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

    public boolean isKeyguardShowingAndNotOccluded() {
        boolean z = false;
        if (this.mKeyguardDelegate == null) {
            return false;
        }
        if (this.mKeyguardDelegate.isShowing()) {
            z = this.mKeyguardOccluded ^ 1;
        }
        return z;
    }

    public boolean isKeyguardTrustedLw() {
        if (this.mKeyguardDelegate == null) {
            return false;
        }
        return this.mKeyguardDelegate.isTrusted();
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

    public boolean isKeyguardOccluded() {
        if (this.mKeyguardDelegate == null) {
            return false;
        }
        return this.mKeyguardOccluded;
    }

    public boolean inKeyguardRestrictedKeyInputMode() {
        if (this.mKeyguardDelegate == null) {
            return false;
        }
        return this.mKeyguardDelegate.isInputRestricted();
    }

    public void dismissKeyguardLw(IKeyguardDismissCallback callback) {
        if (this.mKeyguardDelegate != null && this.mKeyguardDelegate.isShowing()) {
            this.mKeyguardDelegate.dismiss(callback);
        } else if (callback != null) {
            try {
                callback.onDismissError();
            } catch (RemoteException e) {
                Slog.w(TAG, "Failed to call callback", e);
            }
        }
    }

    public boolean isKeyguardDrawnLw() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mKeyguardDrawnOnce;
        }
        return z;
    }

    public void onWakeUp(String wakeUpReason) {
        Log.d(TAG, "onWakeUp, wakeUpReason = " + wakeUpReason);
        this.mKeyguardDelegate.onWakeUp(wakeUpReason);
    }

    public boolean isShowingDreamLw() {
        return this.mShowingDream;
    }

    public void startKeyguardExitAnimation(long startTime, long fadeoutDuration) {
        if (this.mKeyguardDelegate != null) {
            this.mKeyguardDelegate.startKeyguardExitAnimation(startTime, fadeoutDuration);
        }
    }

    public void getStableInsetsLw(int displayRotation, int displayWidth, int displayHeight, Rect outInsets) {
        outInsets.setEmpty();
        getNonDecorInsetsLw(displayRotation, displayWidth, displayHeight, outInsets);
        outInsets.top = this.mStatusBarHeight;
    }

    public void getNonDecorInsetsLw(int displayRotation, int displayWidth, int displayHeight, Rect outInsets) {
        outInsets.setEmpty();
        if (this.mHasNavigationBar) {
            int position = navigationBarPosition(displayWidth, displayHeight, displayRotation);
            if (position == 4) {
                outInsets.bottom = getNavigationBarHeight(displayRotation, this.mUiMode);
            } else if (position == 2) {
                outInsets.right = getNavigationBarWidth(displayRotation, this.mUiMode);
            } else if (position == 1) {
                outInsets.left = getNavigationBarWidth(displayRotation, this.mUiMode);
            }
        }
    }

    public boolean isNavBarForcedShownLw(WindowState windowState) {
        return this.mForceShowSystemBars;
    }

    public int getNavBarPosition() {
        return this.mNavigationBarPosition;
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
            } else if ((this.mHdmiPlugged || this.mWifiDisplayConnected) && this.mDemoHdmiRotationLock) {
                preferredRotation = this.mDemoHdmiRotation;
            } else if (this.mWifiDisplayConnected && this.mWifiDisplayCustomRotation > -1) {
                preferredRotation = this.mWifiDisplayCustomRotation;
            } else if (this.mHdmiPlugged && this.mDockMode == 0 && this.mUndockedHdmiRotation >= 0) {
                preferredRotation = this.mUndockedHdmiRotation;
            } else if (this.mDemoRotationLock) {
                preferredRotation = this.mDemoRotation;
            } else if (this.mPersistentVrModeEnabled) {
                preferredRotation = this.mPortraitRotation;
            } else if (orientation == 14) {
                preferredRotation = lastRotation;
            } else if (!this.mSupportAutoRotation) {
                preferredRotation = -1;
            } else if ((this.mUserRotationMode == 0 && (orientation == 2 || orientation == -1 || orientation == 11 || orientation == 12 || orientation == 13)) || orientation == 4 || orientation == 10 || orientation == 6 || orientation == 7) {
                if (this.mAllowAllRotations < 0) {
                    if (this.mContext.getResources().getBoolean(17956870)) {
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
            if (DEBUG_ORIENT) {
                Slog.v(TAG, "orientation " + orientation + " preferredRotation " + preferredRotation + " sensorRotation " + sensorRotation);
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
        setNavigationBarColorToStatusBar(-1, rotation, this.mExpendBar);
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
        this.mSafeMode = safeMode;
        if (safeMode) {
            performHapticFeedbackLw(null, OppoScreenOffGestureManager.MSG_SCREEN_TURNED_OFF, true);
        }
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

    private void bindKeyguard() {
        synchronized (this.mLock) {
            if (this.mKeyguardBound) {
                return;
            }
            this.mKeyguardBound = true;
            this.mKeyguardDelegate.bindService(this.mContext);
        }
    }

    public void onSystemUiStarted() {
        bindKeyguard();
    }

    public void systemReady() {
        this.mKeyguardDelegate.onSystemReady();
        this.mVrManagerInternal = (VrManagerInternal) LocalServices.getService(VrManagerInternal.class);
        if (this.mVrManagerInternal != null) {
            this.mVrManagerInternal.addPersistentVrModeStateListener(this.mPersistentVrModeListener);
        }
        readCameraLensCoverState();
        updateUiMode();
        synchronized (this.mLock) {
            updateOrientationListenerLp();
            this.mSystemReady = true;
            this.mHoverNavigationBarStatus = Secure.getInt(this.mContext.getContentResolver(), SETTINGS_HOVER_NAVIGATIONBAR_ENABLE_STRING, 0);
            if (this.mHoverNavigationBarStatus == 2) {
                Secure.putInt(this.mContext.getContentResolver(), SETTINGS_HOVER_NAVIGATIONBAR_ENABLE_STRING, 0);
            }
            this.mHandler.post(new Runnable() {
                public void run() {
                    PhoneWindowManager.this.updateSettings();
                }
            });
            if (this.mSystemBooted) {
                this.mKeyguardDelegate.onBootCompleted();
            }
        }
        this.mSystemGestures.systemReady();
        this.mImmersiveModeConfirmation.systemReady();
        this.mAutofillManagerInternal = (AutofillManagerInternal) LocalServices.getService(AutofillManagerInternal.class);
        mDisplayCompatUtils = ColorDisplayCompatUtils.getInstance();
    }

    public void systemBooted() {
        bindKeyguard();
        synchronized (this.mLock) {
            this.mSystemBooted = true;
            if (this.mSystemReady) {
                this.mKeyguardDelegate.onBootCompleted();
            }
        }
        startedWakingUp();
        screenTurningOn(null);
        screenTurnedOn();
    }

    public boolean canDismissBootAnimation() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mKeyguardDrawComplete;
        }
        return z;
    }

    public void showBootMessage(final CharSequence msg, boolean always) {
        this.mHandler.post(new Runnable() {
            public void run() {
                if (PhoneWindowManager.this.mBootMsgDialog == null) {
                    int theme;
                    if (PhoneWindowManager.this.mContext.getPackageManager().hasSystemFeature("android.software.leanback")) {
                        theme = 16974820;
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
                        PhoneWindowManager.this.mBootMsgDialog.setTitle(17039490);
                    } else {
                        PhoneWindowManager.this.mBootMsgDialog.setTitle(17039482);
                    }
                    PhoneWindowManager.this.mBootMsgDialog.setProgressStyle(0);
                    PhoneWindowManager.this.mBootMsgDialog.setIndeterminate(true);
                    PhoneWindowManager.this.mBootMsgDialog.getWindow().setType(2021);
                    PhoneWindowManager.this.mBootMsgDialog.getWindow().addFlags(258);
                    PhoneWindowManager.this.mBootMsgDialog.getWindow().setDimAmount(1.0f);
                    WindowManager.LayoutParams lp = PhoneWindowManager.this.mBootMsgDialog.getWindow().getAttributes();
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
                    this.mHandler.removeCallbacks(this.mScreenLockTimeout);
                    this.mHandler.postDelayed(this.mScreenLockTimeout, (long) this.mLockScreenTimeout);
                } else {
                    this.mHandler.removeCallbacks(this.mScreenLockTimeout);
                }
                this.mLockScreenTimerActive = enable;
            }
        }
    }

    private void updateDreamingSleepToken(boolean acquire) {
        if (acquire) {
            if (this.mDreamingSleepToken == null) {
                this.mDreamingSleepToken = this.mActivityManagerInternal.acquireSleepToken("Dream", 0);
            }
        } else if (this.mDreamingSleepToken != null) {
            this.mDreamingSleepToken.release();
            this.mDreamingSleepToken = null;
        }
    }

    private void updateScreenOffSleepToken(boolean acquire) {
        if (acquire) {
            if (this.mScreenOffSleepToken == null) {
                this.mScreenOffSleepToken = this.mActivityManagerInternal.acquireSleepToken("ScreenOff", 0);
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
            goToSleep(SystemClock.uptimeMillis(), 3, 1);
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
                } else if (this.mUiMode == 7) {
                    intent = this.mVrHeadsetHomeIntent;
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
                    ActivityManager.getService().stopAppSwitches();
                    sendCloseSystemWindows();
                    Intent dock = createHomeDockIntent();
                    if (dock != null && ActivityManager.getService().startActivityAsUser(null, null, dock, dock.resolveTypeIfNeeded(this.mContext.getContentResolver()), null, null, 0, 1, null, null, -2) == 1) {
                        return false;
                    }
                }
                if (ActivityManager.getService().startActivityAsUser(null, null, this.mHomeIntent, this.mHomeIntent.resolveTypeIfNeeded(this.mContext.getContentResolver()), null, null, 0, 1, null, null, -2) == 1) {
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

    private boolean isTheaterModeEnabled() {
        return Global.getInt(this.mContext.getContentResolver(), "theater_mode_on", 0) == 1;
    }

    private boolean areSystemNavigationKeysEnabled() {
        return Secure.getIntForUser(this.mContext.getContentResolver(), "system_navigation_keys_enabled", 0, -2) == 1;
    }

    public boolean performHapticFeedbackLw(WindowState win, int effectId, boolean always) {
        if (this.mVibrator.hasVibrator()) {
            boolean hapticsDisabled = System.getIntForUser(this.mContext.getContentResolver(), "haptic_feedback_enabled", 0, -2) == 0;
            if (!hapticsDisabled || (always ^ 1) == 0) {
                VibrationEffect effect = getVibrationEffect(effectId);
                if (effect == null) {
                    return false;
                }
                int owningUid;
                String owningPackage;
                if (win != null) {
                    owningUid = win.getOwningUid();
                    owningPackage = win.getOwningPackage();
                } else {
                    owningUid = Process.myUid();
                    owningPackage = this.mContext.getOpPackageName();
                }
                this.mVibrator.vibrate(owningUid, owningPackage, effect, VIBRATION_ATTRIBUTES);
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

    private VibrationEffect getVibrationEffect(int effectId) {
        long[] pattern;
        switch (effectId) {
            case 0:
                pattern = this.mLongPressVibePattern;
                break;
            case 1:
                return VibrationEffect.get(0);
            case 3:
                return VibrationEffect.get(0);
            case 4:
                return VibrationEffect.get(2);
            case 5:
                pattern = this.mCalendarDateVibePattern;
                break;
            case 6:
                return VibrationEffect.get(2);
            case 7:
                return VibrationEffect.get(2, false);
            case 8:
                return VibrationEffect.get(2, false);
            case 9:
                return VibrationEffect.get(2, false);
            case OppoScreenOffGestureManager.MSG_SCREEN_TURNED_OFF /*10001*/:
                pattern = this.mSafeModeEnabledVibePattern;
                break;
            default:
                return null;
        }
        if (pattern.length == 0) {
            return null;
        }
        if (pattern.length == 1) {
            return VibrationEffect.createOneShot(pattern[0], -1);
        }
        return VibrationEffect.createWaveform(pattern, -1);
    }

    public void keepScreenOnStartedLw() {
    }

    public void keepScreenOnStoppedLw() {
        if (isKeyguardShowingAndNotOccluded()) {
            this.mPowerManager.userActivity(SystemClock.uptimeMillis(), false);
        }
    }

    /* JADX WARNING: Missing block: B:290:0x0a4b, code:
            if (isWindowTitleEquals(r25, "com.wb.gc.ljfk/com.nearme.game.sdk.component.proxy.ProxyActivity") != false) goto L_0x0a4d;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "JianHui.Yu@ROM.SDK, 2017-12-10 : Modify for Longshot 5.0", property = OppoRomType.ROM)
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
            if (isStatusBarKeyguard()) {
                winCandidate = this.mStatusBar;
            } else {
                winCandidate = this.mTopFullscreenOpaqueWindowState;
            }
            if (winCandidate == null) {
                return 0;
            }
        }
        WindowState win = winCandidate;
        if ((winCandidate.getAttrs().privateFlags & 1024) != 0 && this.mKeyguardOccluded) {
            return 0;
        }
        if (ColorWindowManager.skipSystemUiVisibility(win.getAttrs())) {
            if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
                Slog.d(TAG, "updateSystemUiVisibilityLw : skip");
            }
            return 0;
        }
        int windowFocusFlags;
        if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
            Slog.d(TAG, "updateSystemUiVisibilityLw: mFocusedWindow=" + this.mFocusedWindow + ", mTopFullscreenOpaqueWindowState=" + this.mTopFullscreenOpaqueWindowState + ", mTopDockedOpaqueWindowState=" + this.mTopDockedOpaqueWindowState + ", mTopDockedWindowState=" + this.mTopDockedWindowState + ", winCandidate=" + winCandidate + ", win=" + win + ", win.getAppToken()=" + win.getAppToken() + ", mFocusedApp=" + this.mFocusedApp);
        }
        WindowState referWin = win;
        if (win.getStackId() == 2 && this.mTopFullscreenOpaqueWindowState != null) {
            win = this.mTopFullscreenOpaqueWindowState;
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
        int navVisibility = 0;
        int navigationBarColor = win.getAttrs().navigationBarColor;
        boolean isKeyguardShow = isKeyguardShowingAndNotOccluded();
        if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
            Slog.d(TAG, "updateSystemUiVisibilityLw win=" + win + " tmpVisibility=" + Integer.toHexString(tmpVisibility) + " win.getAttrs()" + win.getAttrs() + " mLastValidStatusBarTint=" + Integer.toHexString(this.mLastValidStatusBarTint) + " mLastValidNavigationBarTint=" + Integer.toHexString(this.mLastValidNavigationBarTint) + " keyguardOn()=" + keyguardOn() + " isKeyguardShow:" + isKeyguardShow + " mExpandBar=" + this.mExpendBar);
        }
        if (win != null && win.toString().contains("Splash Screen ")) {
            if ((this.mLastSystemUiFlags & 8192) != 0) {
                tmpVisibility |= 8192;
            }
            if (win.getAttrs().navigationBarColor != 0) {
                if (lightNavigationBar(win)) {
                    tmpVisibility |= 16;
                }
                this.mLastValidNavigationBarTint = tmpVisibility & 16;
            } else if ((this.mLastSystemUiFlags & 16) != 0) {
                tmpVisibility |= 16;
            }
        } else if (ColorWindowManager.updateSpecialSystemBar(win.getAttrs())) {
            tmpVisibility &= ~16;
            this.mLastValidNavigationBarTint = tmpVisibility & 16;
            tmpVisibility &= ~8192;
            Slog.d(TAG, "updateDarkNavigationBar : mLastValidNavigationBarTint=" + this.mLastValidNavigationBarTint);
            if ((this.mLastValidStatusBarTint & 8192) != 0) {
                tmpVisibility |= 8192;
                if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
                    Slog.d(TAG, "updateDarkNavigationBar : tmpVisibility |= FLAG_STATUS_BAR_LIGHT");
                }
            } else {
                tmpVisibility &= ~8192;
                if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
                    Slog.d(TAG, "updateDarkNavigationBar : tmpVisibility &= ~FLAG_STATUS_BAR_LIGHT");
                }
            }
        } else if (this.mTopFullscreenOpaqueWindowState == null) {
            if (isKeyguardShow) {
                if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
                    Slog.d(TAG, "top window is keyguard vis:" + Integer.toHexString(win.getSystemUiVisibility()));
                }
                this.mLastValidStatusBarTint = win.getSystemUiVisibility() & 8192;
            } else {
                if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
                    Slog.d(TAG, "topfull is null, keep system bar last light flag.");
                }
                if (this.mLastValidStatusBarTint != 0) {
                    tmpVisibility |= 8192;
                } else {
                    tmpVisibility &= ~8192;
                }
            }
            tmpVisibility = (this.mLastValidNavigationBarTint & 16) != 0 ? tmpVisibility | 16 : tmpVisibility & (~16);
        } else if (win != this.mTopFullscreenOpaqueWindowState) {
            int topVisibility = this.mTopFullscreenOpaqueWindowState.getSystemUiVisibility();
            if (lightNavigationBar(this.mTopFullscreenOpaqueWindowState)) {
                if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
                    Slog.d(TAG, "top win is light navigation bar flag @" + this.mTopFullscreenOpaqueWindowState.getAttrs());
                }
                topVisibility |= 16;
            } else if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
                Slog.d(TAG, "top win is dark navigation bar flag @" + this.mTopFullscreenOpaqueWindowState.getAttrs());
            }
            if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
                Slog.d(TAG, "updateSystemUiVisibilityLw: topVis=" + Integer.toHexString(topVisibility));
            }
            if ((topVisibility & 8192) != 0) {
                if (isKeyguardShow) {
                    if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
                        Slog.d(TAG, "top window is keyguard vis:" + Integer.toHexString(win.getSystemUiVisibility()));
                    }
                    this.mLastValidStatusBarTint = win.getSystemUiVisibility() & 8192;
                } else {
                    if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
                        Slog.d(TAG, "top fullscreen window is light statusbar");
                    }
                    tmpVisibility |= 8192;
                    this.mLastValidStatusBarTint = tmpVisibility & 8192;
                }
            } else if (!((tmpVisibility & 8192) == 0 || isKeyguardShow || (this.mTopFullscreenOpaqueWindowState.toString().contains("Splash Screen ") ^ 1) == 0)) {
                tmpVisibility &= ~8192;
            }
            if ((topVisibility & 16) != 0) {
                if (!isKeyguardShow) {
                    if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
                        Slog.d(TAG, "top fullscreen window is light navigation bar");
                    }
                    tmpVisibility |= 16;
                    this.mLastValidNavigationBarTint |= 16;
                } else if (lightNavigationBar(win)) {
                    this.mLastValidNavigationBarTint |= 16;
                } else {
                    this.mLastValidNavigationBarTint &= ~16;
                }
            } else if (lightNavigationBar(win)) {
                if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
                    Slog.d(TAG, "win is light navigation bar");
                }
                if (!(isKeyguardShow || (this.mTopFullscreenOpaqueWindowState.toString().contains("Splash Screen ") ^ 1) == 0)) {
                    tmpVisibility &= ~16;
                }
            }
        } else {
            this.mLastValidStatusBarTint = win.getSystemUiVisibility() & 8192;
            if (lightNavigationBar(win)) {
                tmpVisibility |= 16;
            } else {
                tmpVisibility &= ~16;
            }
            this.mLastValidNavigationBarTint = tmpVisibility & 16;
            if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
                Slog.d(TAG, "win is top full screen winVis=" + Integer.toHexString(win.getSystemUiVisibility()) + ", tmpVisibility=" + Integer.toHexString(tmpVisibility) + ", mLastValidStatusBarTint:" + Integer.toHexString(this.mLastValidStatusBarTint) + ", mLastValidNavigationBarTint=" + Integer.toHexString(this.mLastValidNavigationBarTint));
            }
        }
        if ((win.getAttrs().navigationBarVisibility & 134217728) != 0) {
            if ((win.getAttrs().navigationBarVisibility & 67108864) != 0) {
                tmpVisibility |= 8192;
            } else {
                tmpVisibility &= ~8192;
            }
            this.mLastValidStatusBarTint = tmpVisibility & 8192;
        }
        if (!(this.mTopFullscreenOpaqueWindowState == null || win != this.mTopFullscreenOpaqueWindowState || (win.getAttrs().flags & DumpState.DUMP_CHANGES) == 0 || (isImmersiveNavbar(win) ^ 1) == 0)) {
            if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
                Slog.d(TAG, "win has FLAG_DISMISS_KEYGUARD, draw background.");
            }
            navVisibility = 128;
            navigationBarColor = -263173;
            this.mLastNavigationBarColor = -263173;
            tmpVisibility &= ~8192;
        }
        if (win.isDisplayCompat()) {
            this.mExpendBar = true;
        } else {
            this.mExpendBar = false;
        }
        if (ColorWindowManager.isUseLastStatusBarTint(win.getAttrs())) {
            if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
                Slog.d(TAG, "isUseLastStatusBarTint! type:" + win.getAttrs().type + ", navVisibility:" + Integer.toHexString(navVisibility) + ", navigationBarColor:" + Integer.toHexString(navigationBarColor));
            }
            navVisibility &= -129;
        } else if (win == this.mStatusBar || win.getAttrs().type == 2306 || getWindowLayerLw(win) > getWindowLayerFromTypeLw(2019)) {
            this.mExpendBar = this.mLastExpand;
            if ((this.mLastNavVisibility & 128) != 0) {
                navVisibility |= 128;
                navigationBarColor = this.mLastNavigationBarColor;
            }
            if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
                Slog.d(TAG, "win is status bar or above navigation bar! type:" + win.getAttrs().type + ", navVisibility:" + Integer.toHexString(navVisibility) + ", navigationBarColor:" + Integer.toHexString(navigationBarColor));
            }
        } else if (!win.getAttrs().isFullscreen()) {
            if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
                Slog.d(TAG, "win is not full screen mLastNavVisibility:" + Integer.toHexString(this.mLastNavVisibility) + ", mLastNavigationBarColor:" + Integer.toHexString(this.mLastNavigationBarColor));
            }
            if (this.mFocusedWindow == null || this.mTopFullscreenOpaqueWindowState == null || this.mFocusedWindow == this.mTopFullscreenOpaqueWindowState) {
                this.mExpendBar = this.mLastExpand;
                navVisibility = this.mLastNavVisibility;
                navigationBarColor = this.mLastNavigationBarColor;
                if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
                    Slog.d(TAG, "use last state, mTopFullscreenOpaqueWindowState:" + this.mTopFullscreenOpaqueWindowState + ", navVisibility:" + Integer.toHexString(navVisibility) + ", navigationBarColor:" + Integer.toHexString(navigationBarColor));
                }
            } else {
                if (isImmersiveNavbar(this.mTopFullscreenOpaqueWindowState)) {
                }
                navVisibility |= 128;
                if (this.mFocusedWindow != this.mStatusBar) {
                    if (win.toString().contains("PopupWindow:")) {
                        navigationBarColor = this.mLastNavigationBarColor;
                    } else {
                        navigationBarColor = win.getAttrs().navigationBarColor;
                        if (navigationBarColor == 0) {
                            navigationBarColor = this.mLastNavigationBarColor;
                        }
                    }
                    this.mLastNavigationBarColor = navigationBarColor;
                    if (this.mTopFullscreenOpaqueWindowState.isDisplayCompat()) {
                        this.mExpendBar = true;
                    } else {
                        this.mExpendBar = false;
                    }
                }
                if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
                    Slog.d(TAG, "focus window isn't top fullscreen window. mTopFullscreenOpaqueWindowState:" + this.mTopFullscreenOpaqueWindowState + ", navVisibility:" + Integer.toHexString(navVisibility) + ", navigationBarColor:" + Integer.toHexString(navigationBarColor));
                }
            }
        } else if (this.mTopFullscreenOpaqueWindowState == null) {
            if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
                Slog.d(TAG, "mTopFullscreenOpaqueWindowState is null, mLastNavVisibility:" + Integer.toHexString(this.mLastNavVisibility) + ", mLastNavigationBarColor:" + Integer.toHexString(this.mLastNavigationBarColor) + ", mLastExpand:" + this.mLastExpand);
            }
            this.mExpendBar = this.mLastExpand;
            navVisibility = this.mLastNavVisibility;
            navigationBarColor = this.mLastNavigationBarColor;
        } else {
            if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
                Slog.d(TAG, "win is full screen & mTopFullscreenOpaqueWindowState isn't null! navigationBarVisibility:" + Integer.toHexString(win.getAttrs().navigationBarVisibility) + " navigationBarColor:" + Integer.toHexString(win.getAttrs().navigationBarColor) + " mLastNavVisibility:" + Integer.toHexString(this.mLastNavVisibility) + " mLastNavigationBarColor:" + Integer.toHexString(this.mLastNavigationBarColor) + " mExpendBar" + this.mExpendBar);
            }
            if (this.mFocusedWindow == null && (this.mLastNavVisibility & 128) != 0) {
                navVisibility |= 128;
                navigationBarColor = this.mLastNavigationBarColor;
                this.mExpendBar = this.mLastExpand;
                if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
                    Slog.d(TAG, "focus is null & last is draw background color:" + Integer.toHexString(navigationBarColor));
                }
            } else if (win == this.mTopFullscreenOpaqueWindowState) {
                if (!isImmersiveNavbar(win)) {
                    navVisibility |= 128;
                    navigationBarColor = win.getAttrs().navigationBarColor;
                    this.mLastNavigationBarColor = navigationBarColor;
                    if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
                        Slog.d(TAG, "win is top full screen, draw background color:" + Integer.toHexString(navigationBarColor));
                    }
                }
            } else if (win.toString().contains("PopupWindow:")) {
                navVisibility = this.mLastNavVisibility;
                navigationBarColor = this.mLastNavigationBarColor;
                if (this.mTopFullscreenOpaqueWindowState.isDisplayCompat()) {
                    this.mExpendBar = true;
                } else {
                    this.mExpendBar = false;
                }
                if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
                    Slog.d(TAG, "the focus window is popupwindow navigationBarColor:" + Integer.toHexString(navigationBarColor));
                }
            } else if (isImmersiveNavbar(win)) {
                if (!isImmersiveNavbar(this.mTopFullscreenOpaqueWindowState)) {
                    if (this.mTopFullscreenOpaqueWindowState.isDisplayCompat()) {
                        this.mExpendBar = true;
                    } else {
                        this.mExpendBar = false;
                    }
                    navVisibility |= 128;
                    navigationBarColor = win.getAttrs().navigationBarColor;
                    this.mLastNavigationBarColor = navigationBarColor;
                    if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
                        Slog.d(TAG, "TopFullscreenWindow is not immersive nav bar, draw background color:" + Integer.toHexString(navigationBarColor));
                    }
                }
            } else {
                navVisibility |= 128;
                navigationBarColor = win.getAttrs().navigationBarColor;
                this.mLastNavigationBarColor = navigationBarColor;
                if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
                    Slog.d(TAG, "win is not immersive nav bar, draw background color:" + Integer.toHexString(navigationBarColor));
                }
            }
        }
        if ("com.mxxcvn.fun.sea".equals(win.getOwningPackage())) {
            tmpVisibility &= ~16;
            this.mLastValidNavigationBarTint = tmpVisibility & 16;
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
        if (isKeyguardShow && win == this.mStatusBar) {
            navVisibility |= 8192;
        } else {
            navVisibility &= -8193;
        }
        if ((win.getAttrs().navigationBarVisibility & SYSTEM_UI_FLAG_APP_CUSTOM_NAVIGATION_BAR) != 0) {
            navVisibility |= SYSTEM_UI_FLAG_APP_CUSTOM_NAVIGATION_BAR;
        } else {
            navVisibility &= -33554433;
        }
        if (!(this.mDisplayRotation == this.mLastDisplayRotation && navigationBarColor == this.mLastExpendBarColor && this.mExpendBar == this.mLastExpand && navVisibility == this.mLastNavVisibility)) {
            if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
                Slog.d(TAG, "setNavigationBarColorToStatusBar mDisplayRotation:" + this.mDisplayRotation + ",navigationBarColor:" + Integer.toHexString(navigationBarColor) + ",navVisibility:" + Integer.toHexString(navVisibility) + ",mExpendBar:" + this.mExpendBar);
            }
            this.mLastDisplayRotation = this.mDisplayRotation;
            this.mLastNavVisibility = navVisibility;
            setNavigationBarColorToStatusBar(navVisibility, navigationBarColor, this.mExpendBar);
        }
        if ((this.mDisplayRotation == 1 || this.mDisplayRotation == 3) && mAutoHideApp != null && mAutoHideApp.contains(win.getOwningPackage())) {
            tmpVisibility |= UsbACInterface.FORMAT_II_AC3;
            if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
                Slog.d(TAG, "after upadte win=" + win + " add hide flag! vis:" + Integer.toHexString(tmpVisibility));
            }
        }
        int visibility = updateSystemBarsLw(win, this.mLastSystemUiFlags, tmpVisibility);
        int state = 0;
        if ((visibility & 2) != 0) {
            state = 2;
        }
        if (this.mLastNavigationBarState != state) {
            this.mLastNavigationBarState = state;
            Bundle data = new Bundle();
            data.putInt(BarController.SYSTEM_BAR_ID, 2);
            data.putInt(BarController.SYSTEM_BAR_STATE, state);
            this.mWindowManagerInternal.notifyWindowStateChange(data);
        }
        int diff = visibility ^ this.mLastSystemUiFlags;
        int fullscreenDiff = fullscreenVisibility ^ this.mLastFullscreenStackSysUiFlags;
        int dockedDiff = dockedVisibility ^ this.mLastDockedStackSysUiFlags;
        boolean needsMenu = referWin.getNeedsMenuLw(this.mTopFullscreenOpaqueWindowState);
        if (this.mFocusedWindow == null || this.mFocusedApp == null || this.mTopDockedWindowState == null || this.mFocusedApp != this.mFocusedWindow.getAppToken()) {
            windowFocusFlags = this.mLastWindowFocusFlags;
        } else if (this.mFocusedWindow == this.mTopDockedWindowState) {
            windowFocusFlags = 0;
        } else {
            windowFocusFlags = 64;
        }
        int windowFocusDiff = windowFocusFlags ^ this.mLastWindowFocusFlags;
        if (windowFocusDiff != 0) {
            handleMultiWindowFocusChanged(windowFocusFlags);
            this.mLastWindowFocusFlags = windowFocusFlags;
        }
        if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
            Slog.d(TAG, "updateSystemUiVisibilityLw: diff=" + Integer.toHexString(diff) + ", mLastValidNavigationBarTint=" + Integer.toHexString(this.mLastValidNavigationBarTint) + ", fullscreenDiff=" + Integer.toHexString(fullscreenDiff) + ", dockedDiff=" + Integer.toHexString(dockedDiff) + ", windowFocusDiff=" + Integer.toHexString(windowFocusDiff));
        }
        if (diff == 0 && fullscreenDiff == 0 && dockedDiff == 0 && this.mLastFocusNeedsMenu == needsMenu && this.mFocusedApp == referWin.getAppToken() && this.mLastNonDockedStackBounds.equals(this.mNonDockedStackBounds) && this.mLastDockedStackBounds.equals(this.mDockedStackBounds)) {
            return 0;
        }
        this.mLastSystemUiFlags = visibility;
        this.mLastFullscreenStackSysUiFlags = fullscreenVisibility;
        this.mLastDockedStackSysUiFlags = dockedVisibility;
        this.mLastFocusNeedsMenu = needsMenu;
        this.mFocusedApp = referWin.getAppToken();
        Rect fullscreenStackBounds = new Rect(this.mNonDockedStackBounds);
        Rect dockedStackBounds = new Rect(this.mDockedStackBounds);
        StatusBarManagerInternal statusbar = getStatusBarManagerInternal();
        if (statusbar != null) {
            if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
                Slog.d(TAG, "setSystemUiVisibility vis:" + Integer.toHexString(visibility));
            }
            statusbar.setSystemUiVisibility(visibility, fullscreenVisibility, dockedVisibility, -1, fullscreenStackBounds, dockedStackBounds, win.toString());
            statusbar.topAppWindowChanged(needsMenu);
        }
        return diff;
    }

    private boolean isWindowTitleEquals(WindowState win, String title) {
        return win != null ? win.getAttrs().getTitle().equals(title) : false;
    }

    private boolean isImmersiveNavbar(WindowState win) {
        if (win != null) {
            if (isWindowTitleEquals(win, "com.tencent.mm/com.tencent.mm.plugin.sns.ui.SnsBrowseUI") || isWindowTitleEquals(win, "com.tencent.mm/com.tencent.mm.ui.chatting.gallery.ImageGalleryUI") || isWindowTitleEquals(win, "com.android.packageinstaller/com.android.packageinstaller.PackageInstallerActivity") || isWindowTitleEquals(win, "com.snapchat.android/com.snapchat.android.LandingPageActivity")) {
                return true;
            }
            if (isWindowTitleEquals(win, "com.qiyi.video/com.qiyi.video.WelcomeActivity") || isWindowTitleEquals(win, "com.UCMobile/com.uc.browser.office.OfficeActivity") || isWindowTitleEquals(win, "tc.everphoto/everphoto.ui.feature.main.MainActivity")) {
                if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
                    Slog.d(TAG, "isImmersiveNavbar: win=" + win + " isn't immersive nav bar!");
                }
                return false;
            } else if (drawsSystemBarBackground(win)) {
                if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
                    Slog.d(TAG, "isImmersiveNavbar: win=" + win + " is immersive nav bar for drawsSystemBarBackground");
                }
                return true;
            } else if ((win.getAttrs().flags & 512) != 0) {
                Point realSize = new Point(0, 0);
                this.mDisplay.getRealSize(realSize);
                int screenHeight = Math.max(realSize.x, realSize.y);
                Rect contentRect = win.getContentFrameLw();
                if (Math.max(Math.max(contentRect.right, contentRect.bottom), contentRect.left) == screenHeight) {
                    if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
                        Slog.d(TAG, "isImmersiveNavbar: win=" + win + " is immersive nav bar for FLAG_LAYOUT_NO_LIMITS");
                    }
                    return true;
                }
            }
        }
        if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
            Slog.d(TAG, "isImmersiveNavbar: win=" + win + " isn't immersive nav bar!");
        }
        return false;
    }

    private boolean lightNavigationBar(WindowState win) {
        boolean z = true;
        if (win != null && (isWindowTitleEquals(win, "com.tencent.mm/com.tencent.mm.plugin.sns.ui.SnsOnlineVideoActivity") || isWindowTitleEquals(win, "com.tencent.mm/com.tencent.mm.ui.chatting.gallery.ImageGalleryUI") || (((this.mDisplayRotation == 1 || this.mDisplayRotation == 3) && (isWindowTitleEquals(win, "com.qihoo.browser/com.qihoo.browser.BrowserActivity") || isWindowTitleEquals(win, "cn.mucang.xiaomi.android.wz/cn.mucang.android.qichetoutiao.lib.detail.VideoNewsActivity") || isWindowTitleEquals(win, "com.jiongji.andriod.card/com.baicizhan.main.activity.lookup.LookupWordActivity") || isWindowTitleEquals(win, "com.jb.go.musicplayer.mp3player/com.jiubang.go.music.webview.MusicWebViewActivity"))) || isWindowTitleEquals(win, "com.buzzpia.aqua.launcher/com.buzzpia.aqua.launcher.LauncherActivity") || "wan.ke.ji".equals(win.getOwningPackage()) || "com.netease.h28na.google".equals(win.getOwningPackage()) || "com.ksmobile.launcher".equals(win.getOwningPackage()) || isWindowTitleEquals(win, "com.tencent.mm/com.tencent.mm.plugin.sns.ui.SnsBrowseUI")))) {
            return false;
        }
        if (win != null && ("com.doc88.reader".equals(win.getOwningPackage()) || "com.jm.fxw".equals(win.getOwningPackage()) || "com.joom".equals(win.getOwningPackage()) || isWindowTitleEquals(win, "com.google.android.googlequicksearchbox/com.google.android.apps.gsa.searchnow.SearchNow") || isWindowTitleEquals(win, "io.moreless.tide/io.moreless.tide.about.AboutActivity") || isWindowTitleEquals(win, "com.ztrk.goldfishfinance.release/com.ztrk.goldfishfinance.activity.LiveDetailActivity") || isWindowTitleEquals(win, "com.lemi.lvr.superlvr/com.lemi.lvr.superlvr.ui.activity.VideoDetailActivity") || isWindowTitleEquals(win, "io.github.jackzrliu.wificonsultant/io.github.jackzrliu.wificonsultant.view.activity.MainActivity") || isWindowTitleEquals(win, "com.iodev.flashalerts/com.iodev.flashalert.view.MainSettingsActivity") || isWindowTitleEquals(win, "com.iodev.flashalerts/com.iodev.flashalert.view.AllAppsActivity") || isWindowTitleEquals(win, "com.tencent.reading/com.tencent.reading.ui.NewsDetailActivity") || isWindowTitleEquals(win, "com.tencent.reading/com.tencent.reading.ui.ImageDetailActivity") || isWindowTitleEquals(win, "com.hjw.videoparent/com.hjw.videoparent.ui.LoginActivity") || isWindowTitleEquals(win, "com.dianping.v1/com.dianping.v1.NovaMainActivity") || isWindowTitleEquals(win, "com.haodou.recipe/com.haodou.recipe.MainActivity") || isWindowTitleEquals(win, "com.leixun.haitao/com.leixun.haitao.ui.activity.MainTabActivity") || isWindowTitleEquals(win, "com.jingdong.app.mall/com.jingdong.app.mall.MainFrameActivity") || isWindowTitleEquals(win, "com.kingsoft.android.cat/com.kingsoft.android.cat.ui.activity.AddAcountActivity"))) {
            return true;
        }
        boolean isLight = true;
        if (!(win == null || (isShowGestureBarSuspended() ^ 1) == 0)) {
            isLight = (win.getAttrs().navigationBarVisibility & 268435456) != 0;
        }
        if (win != null && (((win.getAttrs().navigationBarVisibility & Integer.MIN_VALUE) == 0 || !isLight) && (win.getSystemUiVisibility() & 128) == 0 && (win.getSystemUiVisibility() & 16) == 0)) {
            z = false;
        }
        return z;
    }

    private int updateLightStatusBarLw(int vis, WindowState opaque, WindowState opaqueOrDimming) {
        int onKeyguard = isStatusBarKeyguard() ? this.mKeyguardOccluded ^ 1 : 0;
        WindowState statusColorWin = onKeyguard != 0 ? this.mStatusBar : opaqueOrDimming;
        if (statusColorWin != null && (statusColorWin == opaque || onKeyguard != 0)) {
            return (vis & -8193) | (PolicyControl.getSystemUiVisibility(statusColorWin, null) & 8192);
        }
        if (statusColorWin == null || !statusColorWin.isDimming()) {
            return vis;
        }
        return vis & -8193;
    }

    private int updateLightNavigationBarLw(int vis, WindowState opaque, WindowState opaqueOrDimming) {
        WindowState navColorWin;
        WindowState imeWin = this.mWindowManagerFuncs.getInputMethodWindowLw();
        if (imeWin != null && imeWin.isVisibleLw() && this.mNavigationBarPosition == 4) {
            navColorWin = imeWin;
        } else {
            navColorWin = opaqueOrDimming;
        }
        if (navColorWin == null) {
            return vis;
        }
        if (navColorWin == opaque) {
            return (vis & -17) | (PolicyControl.getSystemUiVisibility(navColorWin, null) & 16);
        }
        if (navColorWin.isDimming() || navColorWin == imeWin) {
            return vis & -17;
        }
        return vis;
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
        boolean transientStatusBarAllowed;
        boolean transientNavBarAllowed;
        boolean denyTransientStatus;
        boolean dockedStackVisible = this.mWindowManagerInternal.isStackVisible(3);
        boolean resizing = this.mWindowManagerInternal.isDockedDividerResizing();
        boolean forceHideSystemBars = false;
        if (isForceHidesStatusBar()) {
            this.mForceShowSystemBars = false;
            forceHideSystemBars = true;
        } else {
            this.mForceShowSystemBars = !dockedStackVisible ? resizing : true;
        }
        int forceOpaqueStatusBar = this.mForceShowSystemBars ? this.mForceStatusBarFromKeyguard ^ 1 : 0;
        if (!isStatusBarKeyguard() || (this.mKeyguardOccluded ^ 1) == 0) {
            fullscreenTransWin = this.mTopFullscreenOpaqueWindowState;
        } else {
            fullscreenTransWin = this.mStatusBar;
        }
        vis = this.mNavigationBarController.applyTranslucentFlagLw(fullscreenTransWin, this.mStatusBarController.applyTranslucentFlagLw(fullscreenTransWin, vis, oldVis), oldVis);
        int dockedVis = this.mStatusBarController.applyTranslucentFlagLw(this.mTopDockedOpaqueWindowState, 0, 0);
        if ("com.newsdog".equals(win.getOwningPackage())) {
            if (drawsSystemBarBackground(this.mTopFullscreenOpaqueWindowState) && (1073741824 & vis) == 0) {
                fullscreenDrawsStatusBarBackground = true;
            } else {
                fullscreenDrawsStatusBarBackground = forcesDrawStatusBarBackground(this.mTopFullscreenOpaqueWindowState);
            }
        } else {
            if (drawsSystemBarBackground(this.mTopFullscreenOpaqueWindowState)) {
                fullscreenDrawsStatusBarBackground = true;
            } else {
                fullscreenDrawsStatusBarBackground = forcesDrawStatusBarBackground(this.mTopFullscreenOpaqueWindowState);
            }
        }
        if (drawsSystemBarBackground(this.mTopDockedOpaqueWindowState)) {
            dockedDrawsStatusBarBackground = true;
        } else {
            dockedDrawsStatusBarBackground = forcesDrawStatusBarBackground(this.mTopDockedOpaqueWindowState);
        }
        if (this.mTopFullscreenOpaqueWindowState != null) {
            this.mLastFullscreenDrawsStatusBarBackground = fullscreenDrawsStatusBarBackground;
        } else {
            fullscreenDrawsStatusBarBackground = this.mLastFullscreenDrawsStatusBarBackground;
        }
        if (dockedStackVisible) {
            if (this.mTopDockedOpaqueWindowState != null) {
                this.mLastDockedDrawsStatusBarBackground = dockedDrawsStatusBarBackground;
            } else {
                dockedDrawsStatusBarBackground = this.mLastDockedDrawsStatusBarBackground;
            }
        }
        boolean statusBarHasFocus = win.getAttrs().type == 2000;
        if (statusBarHasFocus && (isStatusBarKeyguard() ^ 1) != 0) {
            int flags = 14342;
            if (this.mKeyguardOccluded) {
                flags = -1073727482;
            }
            vis = ((~flags) & vis) | (oldVis & flags);
        }
        if (fullscreenDrawsStatusBarBackground && dockedDrawsStatusBarBackground) {
            vis = (vis | 8) & -1073741825;
        } else if (!((areTranslucentBarsAllowed() || fullscreenTransWin == this.mStatusBar) && forceOpaqueStatusBar == 0)) {
            vis &= -1073741833;
        }
        vis = configureNavBarOpacity(vis, dockedStackVisible, false, resizing);
        boolean immersiveSticky = (vis & 4096) != 0;
        boolean hideStatusBarWM = this.mTopFullscreenOpaqueWindowState != null ? (PolicyControl.getWindowFlags(this.mTopFullscreenOpaqueWindowState, null) & 1024) != 0 : false;
        boolean hideStatusBarSysui = (vis & 4) != 0;
        boolean hideNavBarSysui = (vis & 2) != 0;
        if (this.mStatusBar == null) {
            transientStatusBarAllowed = false;
        } else if (statusBarHasFocus || (!this.mForceShowSystemBars && (hideStatusBarWM || (hideStatusBarSysui && immersiveSticky)))) {
            transientStatusBarAllowed = true;
        } else {
            transientStatusBarAllowed = forceHideSystemBars;
        }
        if (this.mNavigationBar == null || (this.mForceShowSystemBars ^ 1) == 0 || !hideNavBarSysui) {
            transientNavBarAllowed = false;
        } else {
            transientNavBarAllowed = immersiveSticky;
        }
        boolean pendingPanic = this.mPendingPanicGestureUptime != 0 ? SystemClock.uptimeMillis() - this.mPendingPanicGestureUptime <= 30000 : false;
        if (pendingPanic && hideNavBarSysui && (isStatusBarKeyguard() ^ 1) != 0 && this.mKeyguardDrawComplete) {
            this.mPendingPanicGestureUptime = 0;
            this.mStatusBarController.showTransient();
            if (!isNavBarEmpty(vis)) {
                this.mNavigationBarController.showTransient();
            }
        }
        if (!this.mStatusBarController.isTransientShowRequested() || (transientStatusBarAllowed ^ 1) == 0) {
            denyTransientStatus = false;
        } else {
            denyTransientStatus = hideStatusBarSysui;
        }
        int denyTransientNav;
        if (this.mNavigationBarController.isTransientShowRequested()) {
            denyTransientNav = transientNavBarAllowed ^ 1;
        } else {
            denyTransientNav = 0;
        }
        if (denyTransientStatus || denyTransientNav != 0 || this.mForceShowSystemBars) {
            clearClearableFlagsLw();
            vis &= -8;
        }
        int navAllowedHidden = !((vis & 2048) != 0) ? (vis & 4096) != 0 : 1;
        if (hideNavBarSysui && (navAllowedHidden ^ 1) != 0 && getWindowLayerLw(win) > getWindowLayerFromTypeLw(2022)) {
            vis &= -3;
        }
        vis = this.mStatusBarController.updateVisibilityLw(transientStatusBarAllowed, oldVis, vis);
        if (WindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
            Slog.d(TAG, "updateSystemBarsLw() update status bar vis:" + Integer.toHexString(vis));
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

    public void setDismissImeOnBackKeyPressed(boolean newValue) {
        this.mDismissImeOnBackKeyPressed = newValue;
    }

    public int getInputMethodWindowVisibleHeightLw() {
        return this.mDockBottom - this.mCurBottom;
    }

    public void setCurrentUserLw(int newUserId) {
        this.mCurrentUserId = newUserId;
        if (this.mKeyguardDelegate != null) {
            this.mKeyguardDelegate.setCurrentUser(newUserId);
        }
        if (this.mAccessibilityShortcutController != null) {
            this.mAccessibilityShortcutController.setCurrentUser(newUserId);
        }
        StatusBarManagerInternal statusBar = getStatusBarManagerInternal();
        if (statusBar != null) {
            statusBar.setCurrentUser(newUserId);
        }
        setLastInputMethodWindowLw(null, null);
    }

    public void setSwitchingUser(boolean switching) {
        this.mKeyguardDelegate.setSwitchingUser(switching);
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

    /* JADX WARNING: Missing block: B:4:0x000a, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean shouldRotateSeamlessly(int oldRotation, int newRotation) {
        if (oldRotation == this.mUpsideDownRotation || newRotation == this.mUpsideDownRotation || !this.mNavigationBarCanMove) {
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
        if (w == this.mFocusedWindow && w != null && (w.isAnimatingLw() ^ 1) != 0 && (w.getAttrs().rotationAnimation == 2 || w.getAttrs().rotationAnimation == 3)) {
            return true;
        }
        return false;
    }

    public void dump(String prefix, PrintWriter pw, String[] args) {
        if (!"debuglog_pointer".equals(prefix)) {
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
            pw.print("mShowingDream=");
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
            pw.print(prefix);
            pw.print("mDismissImeOnBackKeyPressed=");
            pw.println(this.mDismissImeOnBackKeyPressed);
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
            pw.print(" mKeyguardOccluded=");
            pw.println(this.mKeyguardOccluded);
            pw.print(" mKeyguardOccludedChanged=");
            pw.println(this.mKeyguardOccludedChanged);
            pw.print(" mPendingKeyguardOccluded=");
            pw.println(this.mPendingKeyguardOccluded);
            pw.print(prefix);
            pw.print("mForceStatusBar=");
            pw.print(this.mForceStatusBar);
            pw.print(" mForceStatusBarFromKeyguard=");
            pw.println(this.mForceStatusBarFromKeyguard);
            pw.print(prefix);
            pw.print("mHomePressed=");
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
            pw.print(" mIncallBackBehavior=");
            pw.print(this.mIncallBackBehavior);
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
            if (this.mHasFeatureLeanback) {
                pw.print(prefix);
                pw.print("mAccessibilityTvKey1Pressed=");
                pw.println(this.mAccessibilityTvKey1Pressed);
                pw.print(prefix);
                pw.print("mAccessibilityTvKey2Pressed=");
                pw.println(this.mAccessibilityTvKey2Pressed);
                pw.print(prefix);
                pw.print("mAccessibilityTvScheduled=");
                pw.println(this.mAccessibilityTvScheduled);
            }
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
        } else if (args.length != 3) {
            pw.println("dumpsys window log pointer 0/1  close/open pointer log");
        } else {
            String tag = args[1];
            boolean on = LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(args[2]);
            if (this.mPointerLocationView != null) {
                pw.println("PointerLocationView setPrintCoords on =" + on);
                this.mPointerLocationView.setPrintCoords(on);
            }
        }
    }

    private boolean stopLockTaskMode() {
        try {
            if (!ActivityManager.getService().isInLockTaskMode()) {
                return false;
            }
            ActivityManager.getService().stopLockTaskMode();
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
        if (this.mHandler.hasMessages(27)) {
            this.mHandler.removeMessages(27);
        }
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(27, state, 0), 200);
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
    }

    private void setNavigationBarColorToStatusBar(int visibility, int color, boolean expandNavBar) {
        this.mLastExpand = expandNavBar;
        this.mLastExpendBarColor = color;
        IStatusBarService sbar = getStatusBarService();
        if (sbar != null) {
            try {
                sbar.setNavigationBarColor(visibility, color, expandNavBar);
            } catch (RemoteException e) {
                Log.w(TAG, "setNavigationBarColorToStatusBar e1:" + e);
            }
        }
    }

    public void handleScreenTurningOn() {
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
                Log.d(TAG, "inSilence::" + inSilence + ", isNeverVibrate::" + isNeverVibrate + " mIsMute=" + this.mIsMute.get());
                if (this.mIsMute.get() || (inSilence && (isNeverVibrate ^ 1) == 0)) {
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

    private void adjustOppoWindowFrame(Rect pf, Rect df, Rect of, Rect cf, Rect dcf, WindowManager.LayoutParams attrs) {
        if (canHideNavigationBar()) {
            adjustOppoWindowFrameFullScreen(pf, df, of, cf, dcf, attrs);
        }
    }

    void adjustOppoWindowFrameFullScreen(Rect pf, Rect df, Rect of, Rect cf, Rect dcf, WindowManager.LayoutParams attrs) {
    }

    public boolean isKeyguardShown() {
        return this.mKeyguardOccluded ^ 1;
    }

    public void handleOpeningSpecialApp(int state, String pkg) {
        try {
            IStatusBarService statusbar = getStatusBarService();
            Slog.i(TAG, "handleOpeningSpecialApp (statusbar != null ) " + (statusbar != null) + " pkg: " + pkg + " start: " + state);
            if (statusbar != null) {
                statusbar.handleOpeningSpecialApp(state, pkg);
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "RemoteException when  handleOpeningSpecialApp ", e);
            this.mStatusBarService = null;
        }
    }

    public boolean isGestureAnimSupport() {
        return this.mGestureAnimSupport;
    }

    public int getColorKeyMode() {
        return -1;
    }

    public boolean isIncomingRingingIngoreHomeKey(WindowState currentFocus) {
        return false;
    }

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "JianHui.Yu@ROM.SDK, 2017-10-27 : Add for hook window layer from type for ColorOS", property = OppoRomType.ROM)
    public int hookWindowLayerFromTypeLw(int type, boolean canAddInternalSystemWindow) {
        return 0;
    }

    public int getNavigationBarStatus() {
        return this.mNavigationBarEnableStatus;
    }

    public void transientNavigatioinBar() {
        if (this.mNavigationBar != null) {
            requestTransientBars(this.mNavigationBar);
        }
    }

    private boolean isShowHoverNavigationBar() {
        if (this.mNavigationBarEnableStatus == 2) {
            return this.mHoverNavigationBarStatus == 2 || this.mHoverNavigationBarStatus == 0;
        } else {
            return false;
        }
    }

    private boolean isShowGestureBarPlaceholder() {
        if (this.mNavigationBarEnableStatus == 2 && this.mHoverNavigationBarStatus == 2) {
            return true;
        }
        return false;
    }

    private boolean isShowGestureBarSuspended() {
        if (this.mNavigationBarEnableStatus == 2 && this.mHoverNavigationBarStatus == 0) {
            return true;
        }
        return false;
    }

    private boolean isForceHidesStatusBar() {
        if (this.mWindowManagerInternal.isStackVisible(3)) {
            return this.mDisplayRotation == 1 || this.mDisplayRotation == 3;
        } else {
            return false;
        }
    }
}
