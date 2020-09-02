package com.android.server.policy;

import android.app.ActivityManager;
import android.app.ActivityManagerInternal;
import android.app.ActivityTaskManager;
import android.app.AppOpsManager;
import android.app.IApplicationThread;
import android.app.IUiModeManager;
import android.app.ProfilerInfo;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.app.UiModeManager;
import android.common.OppoFeatureCache;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.ContentObserver;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.hardware.display.DisplayManager;
import android.hardware.display.DisplayManagerInternal;
import android.hardware.hdmi.HdmiAudioSystemClient;
import android.hardware.hdmi.HdmiControlManager;
import android.hardware.hdmi.HdmiPlaybackClient;
import android.hardware.input.InputManagerInternal;
import android.media.AudioAttributes;
import android.media.AudioManagerInternal;
import android.media.AudioSystem;
import android.media.IAudioService;
import android.media.session.MediaSessionLegacyHelper;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.FactoryTest;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.IDeviceIdleController;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManagerInternal;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.StrictMode;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UEventObserver;
import android.os.UserHandle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.service.dreams.DreamManagerInternal;
import android.service.dreams.IDreamManager;
import android.service.vr.IPersistentVrStateCallbacks;
import android.telecom.TelecomManager;
import android.util.Log;
import android.util.LongSparseArray;
import android.util.MutableBoolean;
import android.util.PrintWriterPrinter;
import android.util.Slog;
import android.util.SparseArray;
import android.util.proto.ProtoOutputStream;
import android.view.Display;
import android.view.IDisplayFoldListener;
import android.view.IWindowManager;
import android.view.InputDevice;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.view.WindowManagerPolicyConstants;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.autofill.AutofillManagerInternal;
import com.android.internal.R;
import com.android.internal.accessibility.AccessibilityShortcutController;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.os.RoSystemProperties;
import com.android.internal.policy.IKeyguardDismissCallback;
import com.android.internal.policy.IShortcutService;
import com.android.internal.policy.PhoneWindow;
import com.android.internal.statusbar.IStatusBarService;
import com.android.internal.util.ArrayUtils;
import com.android.server.ExtconStateObserver;
import com.android.server.ExtconUEventObserver;
import com.android.server.LocalServices;
import com.android.server.ServiceThread;
import com.android.server.SystemServiceManager;
import com.android.server.biometrics.fingerprint.power.FingerprintInternal;
import com.android.server.biometrics.fingerprint.util.SupportUtil;
import com.android.server.display.OppoBaseDisplayPowerController2;
import com.android.server.hdmi.HdmiCecKeycode;
import com.android.server.inputmethod.InputMethodManagerInternal;
import com.android.server.job.controllers.JobStatus;
import com.android.server.operator.OppoOperatorManagerService;
import com.android.server.pm.DumpState;
import com.android.server.policy.OppoBasePhoneWindowManager;
import com.android.server.policy.PhoneWindowManager;
import com.android.server.policy.WindowManagerPolicy;
import com.android.server.policy.keyguard.KeyguardServiceDelegate;
import com.android.server.policy.keyguard.KeyguardStateMonitor;
import com.android.server.statusbar.StatusBarManagerInternal;
import com.android.server.usb.descriptors.UsbDescriptor;
import com.android.server.vr.VrManagerInternal;
import com.android.server.wm.ActivityTaskManagerInternal;
import com.android.server.wm.AppTransition;
import com.android.server.wm.DisplayPolicy;
import com.android.server.wm.DisplayRotation;
import com.android.server.wm.OppoWindowManagerInternal;
import com.android.server.wm.WindowManagerInternal;
import com.color.darkmode.IColorDarkModeManager;
import com.mediatek.server.MtkSystemServiceFactory;
import com.mediatek.server.wm.WindowManagerDebugger;
import com.oppo.hypnus.HypnusManager;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class PhoneWindowManager extends OppoBasePhoneWindowManager implements WindowManagerPolicy {
    private static final int BRIGHTNESS_STEPS = 10;
    private static final long BUGREPORT_TV_GESTURE_TIMEOUT_MILLIS = 1000;
    static boolean DEBUG_INPUT = false;
    static boolean DEBUG_KEYGUARD = false;
    static boolean DEBUG_SPLASH_SCREEN = false;
    static boolean DEBUG_WAKEUP = false;
    static final int DEFAULT_LONG_PRESS_POWERON_DISPLAY_TIME = 2500;
    static final int DOUBLE_TAP_HOME_NOTHING = 0;
    static final int DOUBLE_TAP_HOME_RECENT_SYSTEM_UI = 1;
    static final boolean ENABLE_DESK_DOCK_HOME_CAPTURE = false;
    static final boolean ENABLE_VR_HEADSET_HOME_CAPTURE = true;
    private static final float KEYGUARD_SCREENSHOT_CHORD_DELAY_MULTIPLIER = 2.5f;
    static final int LAST_LONG_PRESS_HOME_BEHAVIOR = 2;
    static final int LONG_PRESS_BACK_GO_TO_VOICE_ASSIST = 1;
    static final int LONG_PRESS_BACK_NOTHING = 0;
    static final int LONG_PRESS_HOME_ALL_APPS = 1;
    static final int LONG_PRESS_HOME_ASSIST = 2;
    static final int LONG_PRESS_HOME_NOTHING = 0;
    static final int LONG_PRESS_POWER_ASSISTANT = 5;
    static final int LONG_PRESS_POWER_GLOBAL_ACTIONS = 1;
    static final int LONG_PRESS_POWER_GO_TO_VOICE_ASSIST = 4;
    static final int LONG_PRESS_POWER_NOTHING = 0;
    static final int LONG_PRESS_POWER_SHUT_OFF = 2;
    static final int LONG_PRESS_POWER_SHUT_OFF_NO_CONFIRM = 3;
    private static final long MOVING_DISPLAY_TO_TOP_DURATION_MILLIS = 10;
    private static final int MSG_ACCESSIBILITY_SHORTCUT = 17;
    private static final int MSG_ACCESSIBILITY_TV = 19;
    private static final int MSG_BACK_LONG_PRESS = 16;
    private static final int MSG_BUGREPORT_TV = 18;
    private static final int MSG_DISPATCH_BACK_KEY_TO_AUTOFILL = 20;
    private static final int MSG_DISPATCH_MEDIA_KEY_REPEAT_WITH_WAKE_LOCK = 4;
    private static final int MSG_DISPATCH_MEDIA_KEY_WITH_WAKE_LOCK = 3;
    private static final int MSG_DISPATCH_SHOW_GLOBAL_ACTIONS = 10;
    private static final int MSG_DISPATCH_SHOW_RECENTS = 9;
    private static final int MSG_HANDLE_ALL_APPS = 22;
    private static final int MSG_HIDE_BOOT_MESSAGE = 11;
    private static final int MSG_KEYGUARD_DRAWN_COMPLETE = 5;
    private static final int MSG_KEYGUARD_DRAWN_TIMEOUT = 6;
    private static final int MSG_LAUNCH_ASSIST = 23;
    private static final int MSG_LAUNCH_ASSIST_LONG_PRESS = 24;
    private static final int MSG_LAUNCH_VOICE_ASSIST_WITH_WAKE_LOCK = 12;
    private static final int MSG_MOVE_DISPLAY_TO_TOP = 28;
    private static final int MSG_NOTIFY_USER_ACTIVITY = 26;
    private static final int MSG_POWER_DELAYED_PRESS = 13;
    private static final int MSG_POWER_LONG_PRESS = 14;
    private static final int MSG_POWER_VERY_LONG_PRESS = 25;
    private static final int MSG_RINGER_TOGGLE_CHORD = 27;
    private static final int MSG_SHOW_PICTURE_IN_PICTURE_MENU = 15;
    private static final int MSG_SYSTEM_KEY_PRESS = 21;
    private static final int MSG_WINDOW_MANAGER_DRAWN_COMPLETE = 7;
    static final int MULTI_PRESS_POWER_BRIGHTNESS_BOOST = 2;
    static final int MULTI_PRESS_POWER_NOTHING = 0;
    static final int MULTI_PRESS_POWER_THEATER_MODE = 1;
    static final int PENDING_KEY_NULL = -1;
    private static final int POWER_BUTTON_SUPPRESSION_DELAY_DEFAULT_MILLIS = 800;
    private static final long SCREENSHOT_CHORD_DEBOUNCE_DELAY_MILLIS = 150;
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
    static boolean SHOW_SPLASH_SCREENS = true;
    public static final String SYSTEM_DIALOG_REASON_ASSIST = "assist";
    public static final String SYSTEM_DIALOG_REASON_GLOBAL_ACTIONS = "globalactions";
    public static final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
    public static final String SYSTEM_DIALOG_REASON_KEY = "reason";
    public static final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";
    public static final String SYSTEM_DIALOG_REASON_SCREENSHOT = "screenshot";
    static final String TAG = "WindowManager";
    public static final int TOAST_WINDOW_TIMEOUT = 3500;
    private static final int USER_ACTIVITY_NOTIFICATION_DELAY = 200;
    static final int VERY_LONG_PRESS_POWER_GLOBAL_ACTIONS = 1;
    static final int VERY_LONG_PRESS_POWER_NOTHING = 0;
    private static final AudioAttributes VIBRATION_ATTRIBUTES = new AudioAttributes.Builder().setContentType(4).setUsage(13).build();
    static final int WAITING_FOR_DRAWN_TIMEOUT = 1000;
    /* access modifiers changed from: private */
    public static final int[] WINDOW_TYPES_WHERE_HOME_DOESNT_WORK = {2003, 2010};
    static boolean localLOGV = false;
    static SparseArray<String> sApplicationLaunchKeyCategories = new SparseArray<>();
    HwShutdownRecord hwShutdownRecord = null;
    boolean isTouchFingerPrintSensor = false;
    private boolean mA11yShortcutChordVolumeUpKeyConsumed;
    private long mA11yShortcutChordVolumeUpKeyTime;
    private boolean mA11yShortcutChordVolumeUpKeyTriggered;
    AccessibilityManager mAccessibilityManager;
    /* access modifiers changed from: private */
    public AccessibilityShortcutController mAccessibilityShortcutController;
    private boolean mAccessibilityTvKey1Pressed;
    private boolean mAccessibilityTvKey2Pressed;
    private boolean mAccessibilityTvScheduled;
    ActivityManagerInternal mActivityManagerInternal;
    ActivityTaskManagerInternal mActivityTaskManagerInternal;
    private HashSet<Integer> mAllowLockscreenWhenOnDisplays = new HashSet<>();
    boolean mAllowStartActivityForLongPressOnPowerDuringSetup;
    private boolean mAllowTheaterModeWakeFromCameraLens;
    private boolean mAllowTheaterModeWakeFromKey;
    private boolean mAllowTheaterModeWakeFromLidSwitch;
    private boolean mAllowTheaterModeWakeFromMotion;
    private boolean mAllowTheaterModeWakeFromMotionWhenNotDreaming;
    private boolean mAllowTheaterModeWakeFromPowerKey;
    /* access modifiers changed from: private */
    public boolean mAllowTheaterModeWakeFromWakeGesture;
    private boolean mAodShowing;
    AppOpsManager mAppOpsManager;
    AudioManagerInternal mAudioManagerInternal;
    AutofillManagerInternal mAutofillManagerInternal;
    volatile boolean mBackKeyHandled;
    volatile boolean mBeganFromNonInteractive;
    boolean mBootMessageNeedsHiding;
    ProgressDialog mBootMsgDialog = null;
    PowerManager.WakeLock mBroadcastWakeLock;
    private boolean mBugreportTvKey1Pressed;
    private boolean mBugreportTvKey2Pressed;
    private boolean mBugreportTvScheduled;
    BurnInProtectionHelper mBurnInProtectionHelper;
    long[] mCalendarDateVibePattern;
    volatile boolean mCameraGestureTriggeredDuringGoingToSleep;
    int mCameraLensCoverState = -1;
    Intent mCarDockIntent;
    boolean mConsumeSearchKeyUp;
    Context mContext;
    private int mCurrentUserId;
    Display mDefaultDisplay;
    DisplayPolicy mDefaultDisplayPolicy;
    DisplayRotation mDefaultDisplayRotation;
    Intent mDeskDockIntent;
    private volatile boolean mDismissImeOnBackKeyPressed;
    private DisplayFoldController mDisplayFoldController;
    private final SparseArray<DisplayHomeButtonHandler> mDisplayHomeButtonHandlers = new SparseArray<>();
    DisplayManager mDisplayManager;
    DisplayManagerInternal mDisplayManagerInternal;
    BroadcastReceiver mDockReceiver = new BroadcastReceiver() {
        /* class com.android.server.policy.PhoneWindowManager.AnonymousClass10 */

        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.DOCK_EVENT".equals(intent.getAction())) {
                PhoneWindowManager.this.mDefaultDisplayPolicy.setDockMode(intent.getIntExtra("android.intent.extra.DOCK_STATE", 0));
            } else {
                try {
                    IUiModeManager uiModeService = IUiModeManager.Stub.asInterface(ServiceManager.getService("uimode"));
                    PhoneWindowManager.this.mUiMode = uiModeService.getCurrentModeType();
                } catch (RemoteException e) {
                }
            }
            PhoneWindowManager.this.updateRotation(true);
            PhoneWindowManager.this.mDefaultDisplayRotation.updateOrientationListener();
        }
    };
    int mDoublePressOnPowerBehavior;
    /* access modifiers changed from: private */
    public int mDoubleTapOnHomeBehavior;
    DreamManagerInternal mDreamManagerInternal;
    BroadcastReceiver mDreamReceiver = new BroadcastReceiver() {
        /* class com.android.server.policy.PhoneWindowManager.AnonymousClass11 */

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
    private boolean mEnableCarDockHomeCapture = true;
    boolean mEnableShiftMenuBugReports = false;
    volatile boolean mEndCallKeyHandled;
    private final Runnable mEndCallLongPress = new Runnable() {
        /* class com.android.server.policy.PhoneWindowManager.AnonymousClass6 */

        public void run() {
            PhoneWindowManager phoneWindowManager = PhoneWindowManager.this;
            phoneWindowManager.mEndCallKeyHandled = true;
            boolean unused = phoneWindowManager.performHapticFeedback(0, false, "End Call - Long Press - Show Global Actions");
            PhoneWindowManager.this.showGlobalActionsInternal();
        }
    };
    int mEndcallBehavior;
    private final SparseArray<KeyCharacterMap.FallbackAction> mFallbackActions = new SparseArray<>();
    FingerprintInternal mFingerprintInternal;
    GlobalActions mGlobalActions;
    private GlobalKeyManager mGlobalKeyManager;
    private boolean mGoToSleepOnButtonPressTheaterMode;
    volatile boolean mGoingToSleep;
    private UEventObserver mHDMIObserver = new UEventObserver() {
        /* class com.android.server.policy.PhoneWindowManager.AnonymousClass3 */

        public void onUEvent(UEventObserver.UEvent event) {
            PhoneWindowManager.this.mDefaultDisplayPolicy.setHdmiPlugged("1".equals(event.get("SWITCH_STATE")));
        }
    };
    private boolean mHandleVolumeKeysInWM;
    Handler mHandler;
    boolean mHapticTextHandleEnabled;
    private boolean mHasFeatureHdmiCec;
    private boolean mHasFeatureLeanback;
    private boolean mHasFeatureWatch;
    boolean mHasSoftInput = false;
    boolean mHaveBuiltInKeyboard;
    boolean mHavePendingMediaKeyRepeatWithWakeLock;
    HdmiControl mHdmiControl;
    Intent mHomeIntent;
    private HypnusManager mHypnusManager;
    int mIncallBackBehavior;
    int mIncallPowerBehavior;
    int mInitialMetaState;
    InputManagerInternal mInputManagerInternal;
    InputMethodManagerInternal mInputMethodManagerInternal;
    boolean mIsEnableKeyguardHide = false;
    private boolean mKeyguardBound;
    private WindowManagerPolicy.WindowState mKeyguardCandidate = null;
    KeyguardServiceDelegate mKeyguardDelegate;
    final KeyguardServiceDelegate.DrawnListener mKeyguardDrawnCallback = new KeyguardServiceDelegate.DrawnListener() {
        /* class com.android.server.policy.PhoneWindowManager.AnonymousClass2 */

        @Override // com.android.server.policy.keyguard.KeyguardServiceDelegate.DrawnListener
        public void onDrawn() {
            if (PhoneWindowManager.DEBUG_WAKEUP) {
                Slog.d("WindowManager", "mKeyguardDelegate.ShowListener.onDrawn.");
            }
            PhoneWindowManager.this.mPhoneWinHandler.sendEmptyMessage(5);
        }
    };
    private boolean mKeyguardDrawnOnce;
    volatile boolean mKeyguardOccluded;
    private boolean mKeyguardOccludedChanged;
    boolean mLanguageSwitchKeyPressed;
    private long mLastHomeDownTimeDuringPF = 0;
    private boolean mLidControlsDisplayFold;
    int mLidKeyboardAccessibility;
    int mLidNavigationAccessibility;
    /* access modifiers changed from: private */
    public final Object mLock = new Object();
    int mLockScreenTimeout;
    boolean mLockScreenTimerActive;
    private final LogDecelerateInterpolator mLogDecelerateInterpolator = new LogDecelerateInterpolator(100, 0);
    MetricsLogger mLogger;
    int mLongPressOnBackBehavior;
    /* access modifiers changed from: private */
    public int mLongPressOnHomeBehavior;
    int mLongPressOnPowerBehavior;
    long[] mLongPressVibePattern;
    int mMetaState;
    private volatile long mMovingDisplayToTopKeyTime;
    /* access modifiers changed from: private */
    public volatile boolean mMovingDisplayToTopKeyTriggered;
    BroadcastReceiver mMultiuserReceiver = new BroadcastReceiver() {
        /* class com.android.server.policy.PhoneWindowManager.AnonymousClass12 */

        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.USER_SWITCHED".equals(intent.getAction())) {
                PhoneWindowManager.this.mSettingsObserver.onChange(false);
                PhoneWindowManager.this.mDefaultDisplayRotation.onUserSwitch();
                PhoneWindowManager.this.mWindowManagerFuncs.onUserSwitched();
            }
        }
    };
    volatile boolean mNavBarVirtualKeyHapticFeedbackEnabled = true;
    private boolean mNotifyUserActivity;
    private OppoEngineerPhoneWindowManagerHelper mOppoEngineerHelper;
    BroadcastReceiver mOppoScreencapReceiver = new BroadcastReceiver() {
        /* class com.android.server.policy.PhoneWindowManager.AnonymousClass13 */

        public void onReceive(Context context, Intent intent) {
            if ("oppo.intent.action.SCREEN_SHOT".equals(intent.getAction())) {
                SystemProperties.set("ctl.start", "gettpinfo");
            }
        }
    };
    OppoWindowManagerInternal mOppoWindowManagerInternal;
    boolean mPendingCapsLockToggle;
    private boolean mPendingKeyguardOccluded;
    boolean mPendingMetaAction;
    volatile int mPendingWakeKey = -1;
    private boolean mPerDisplayFocusEnabled = false;
    final IPersistentVrStateCallbacks mPersistentVrModeListener = new IPersistentVrStateCallbacks.Stub() {
        /* class com.android.server.policy.PhoneWindowManager.AnonymousClass4 */

        public void onPersistentVrStateChanged(boolean enabled) {
            PhoneWindowManager.this.mDefaultDisplayPolicy.setPersistentVrModeEnabled(enabled);
        }
    };
    /* access modifiers changed from: private */
    public PhoneWinHandler mPhoneWinHandler;
    private ServiceThread mPhoneWinHandlerThread;
    volatile boolean mPictureInPictureVisible;
    private Runnable mPossibleVeryLongPressReboot = new Runnable() {
        /* class com.android.server.policy.PhoneWindowManager.AnonymousClass5 */

        public void run() {
            PhoneWindowManager.this.mActivityManagerInternal.prepareForPossibleShutdown();
        }
    };
    private int mPowerButtonSuppressionDelayMillis = POWER_BUTTON_SUPPRESSION_DELAY_DEFAULT_MILLIS;
    volatile boolean mPowerKeyHandled;
    volatile int mPowerKeyPressCounter;
    PowerManager.WakeLock mPowerKeyWakeLock;
    PowerManager mPowerManager;
    PowerManagerInternal mPowerManagerInternal;
    boolean mPreloadedRecentApps;
    int mRecentAppsHeldModifiers;
    volatile boolean mRecentsVisible;
    volatile boolean mRequestedOrGoingToSleep;
    private int mRingerToggleChord = 0;
    boolean mSafeMode;
    long[] mSafeModeEnabledVibePattern;
    ScreenLockTimeout mScreenLockTimeout = new ScreenLockTimeout();
    ActivityTaskManagerInternal.SleepToken mScreenOffSleepToken;
    private boolean mScreenshotChordEnabled;
    private long mScreenshotChordPowerKeyTime;
    private boolean mScreenshotChordPowerKeyTriggered;
    private boolean mScreenshotChordVolumeDownKeyConsumed;
    private long mScreenshotChordVolumeDownKeyTime;
    private boolean mScreenshotChordVolumeDownKeyTriggered;
    private final ScreenshotRunnable mScreenshotRunnable = new ScreenshotRunnable();
    boolean mSearchKeyShortcutPending;
    SearchManager mSearchManager;
    final Object mServiceAquireLock = new Object();
    SettingsObserver mSettingsObserver;
    int mShortPressOnPowerBehavior;
    int mShortPressOnSleepBehavior;
    int mShortPressOnWindowBehavior;
    private LongSparseArray<IShortcutService> mShortcutKeyServices = new LongSparseArray<>();
    ShortcutManager mShortcutManager;
    StatusBarManagerInternal mStatusBarManagerInternal;
    IStatusBarService mStatusBarService;
    private boolean mSupportLongPressPowerWhenNonInteractive;
    boolean mSystemBooted;
    boolean mSystemNavigationKeysEnabled;
    boolean mSystemReady;
    private final MutableBoolean mTmpBoolean = new MutableBoolean(false);
    private volatile int mTopFocusedDisplayId = -1;
    int mTriplePressOnPowerBehavior;
    int mUiMode;
    IUiModeManager mUiModeManager;
    boolean mUseTvRouting;
    int mVeryLongPressOnPowerBehavior;
    int mVeryLongPressTimeout;
    Vibrator mVibrator;
    Intent mVrHeadsetHomeIntent;
    volatile VrManagerInternal mVrManagerInternal;
    boolean mWakeGestureEnabledSetting;
    MyWakeGestureListener mWakeGestureListener;
    IWindowManager mWindowManager;
    private WindowManagerDebugger mWindowManagerDebugger = MtkSystemServiceFactory.getInstance().makeWindowManagerDebugger();
    final Runnable mWindowManagerDrawCallback = new Runnable() {
        /* class com.android.server.policy.PhoneWindowManager.AnonymousClass1 */

        public void run() {
            if (PhoneWindowManager.DEBUG_WAKEUP) {
                Slog.i("WindowManager", "All windows ready for display!");
            }
            PhoneWindowManager.this.mPhoneWinHandler.sendEmptyMessage(7);
        }
    };
    WindowManagerPolicy.WindowManagerFuncs mWindowManagerFuncs;
    WindowManagerInternal mWindowManagerInternal;

    static {
        sApplicationLaunchKeyCategories.append(64, "android.intent.category.APP_BROWSER");
        sApplicationLaunchKeyCategories.append(65, "android.intent.category.APP_EMAIL");
        sApplicationLaunchKeyCategories.append(207, "android.intent.category.APP_CONTACTS");
        sApplicationLaunchKeyCategories.append(208, "android.intent.category.APP_CALENDAR");
        sApplicationLaunchKeyCategories.append(209, "android.intent.category.APP_MUSIC");
        sApplicationLaunchKeyCategories.append(210, "android.intent.category.APP_CALCULATOR");
    }

    private class PolicyHandler extends Handler {
        private PolicyHandler() {
        }

        public void handleMessage(Message msg) {
            OppoBasePhoneWindowManager.AssistManagerLaunchMode assistManagerLaunchMode;
            int i = msg.what;
            if (i == 3) {
                PhoneWindowManager.this.dispatchMediaKeyWithWakeLock((KeyEvent) msg.obj);
            } else if (i != 4) {
                boolean z = true;
                switch (i) {
                    case 9:
                        PhoneWindowManager.this.showRecentApps(false);
                        return;
                    case 10:
                        PhoneWindowManager.this.showGlobalActionsInternal();
                        return;
                    case 11:
                        PhoneWindowManager.this.handleHideBootMessage();
                        return;
                    case 12:
                        PhoneWindowManager.this.launchVoiceAssistWithWakeLock();
                        return;
                    case 13:
                        PhoneWindowManager phoneWindowManager = PhoneWindowManager.this;
                        long longValue = ((Long) msg.obj).longValue();
                        if (msg.arg1 == 0) {
                            z = false;
                        }
                        phoneWindowManager.powerPress(longValue, z, msg.arg2);
                        PhoneWindowManager.this.finishPowerKeyPress();
                        return;
                    case 14:
                        PhoneWindowManager.this.powerLongPress();
                        return;
                    case 15:
                        PhoneWindowManager.this.showPictureInPictureMenuInternal();
                        return;
                    case 16:
                        PhoneWindowManager.this.backLongPress();
                        return;
                    case 17:
                        PhoneWindowManager.this.accessibilityShortcutActivated();
                        return;
                    case 18:
                        PhoneWindowManager.this.requestFullBugreport();
                        return;
                    case 19:
                        if (PhoneWindowManager.this.mAccessibilityShortcutController.isAccessibilityShortcutAvailable(false)) {
                            PhoneWindowManager.this.accessibilityShortcutActivated();
                            return;
                        }
                        return;
                    case 20:
                        PhoneWindowManager.this.mAutofillManagerInternal.onBackKeyPressed();
                        return;
                    case 21:
                        PhoneWindowManager.this.sendSystemKeyToStatusBar(msg.arg1);
                        return;
                    case 22:
                        PhoneWindowManager.this.launchAllAppsAction();
                        return;
                    case 23:
                        int deviceId = msg.arg1;
                        String hint = (String) msg.obj;
                        Bundle data = msg.getData();
                        OppoBasePhoneWindowManager.AssistManagerLaunchMode launchMode = null;
                        if (data != null) {
                            try {
                                assistManagerLaunchMode = (OppoBasePhoneWindowManager.AssistManagerLaunchMode) data.getSerializable("launchMode");
                            } catch (Exception e) {
                            }
                        } else {
                            assistManagerLaunchMode = null;
                        }
                        launchMode = assistManagerLaunchMode;
                        PhoneWindowManager.this.launchAssistAction(hint, deviceId, launchMode);
                        return;
                    case 24:
                        PhoneWindowManager.this.launchAssistLongPressAction();
                        return;
                    case 25:
                        PhoneWindowManager.this.powerVeryLongPress();
                        return;
                    case 26:
                        removeMessages(26);
                        Intent intent = new Intent("android.intent.action.USER_ACTIVITY_NOTIFICATION");
                        intent.addFlags(1073741824);
                        PhoneWindowManager.this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, "android.permission.USER_ACTIVITY");
                        return;
                    case 27:
                        if (!PhoneWindowManager.this.startPersonalAssistant(1)) {
                            PhoneWindowManager.this.handleRingerChordGesture();
                            return;
                        }
                        return;
                    case 28:
                        PhoneWindowManager.this.mWindowManagerFuncs.moveDisplayToTop(msg.arg1);
                        boolean unused = PhoneWindowManager.this.mMovingDisplayToTopKeyTriggered = false;
                        return;
                    default:
                        return;
                }
            } else {
                PhoneWindowManager.this.dispatchMediaKeyRepeatWithWakeLock((KeyEvent) msg.obj);
            }
        }
    }

    /* access modifiers changed from: private */
    public class PhoneWinHandler extends Handler {
        public PhoneWinHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 5) {
                if (PhoneWindowManager.DEBUG_WAKEUP) {
                    Slog.w("WindowManager", "Setting mKeyguardDrawComplete");
                }
                PhoneWindowManager.this.finishKeyguardDrawn();
            } else if (i == 6) {
                Slog.w("WindowManager", "Keyguard drawn timeout. Setting mKeyguardDrawComplete");
                PhoneWindowManager.this.finishKeyguardDrawn();
            } else if (i == 7) {
                if (PhoneWindowManager.DEBUG_WAKEUP) {
                    Slog.w("WindowManager", "Setting mWindowManagerDrawComplete");
                }
                PhoneWindowManager.this.finishWindowsDrawn();
            }
        }
    }

    class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
        }

        /* access modifiers changed from: package-private */
        public void observe() {
            ContentResolver resolver = PhoneWindowManager.this.mContext.getContentResolver();
            resolver.registerContentObserver(Settings.System.getUriFor("end_button_behavior"), false, this, -1);
            resolver.registerContentObserver(Settings.Secure.getUriFor("incall_power_button_behavior"), false, this, -1);
            resolver.registerContentObserver(Settings.Secure.getUriFor("incall_back_button_behavior"), false, this, -1);
            resolver.registerContentObserver(Settings.Secure.getUriFor("wake_gesture_enabled"), false, this, -1);
            resolver.registerContentObserver(Settings.System.getUriFor("screen_off_timeout"), false, this, -1);
            resolver.registerContentObserver(Settings.Secure.getUriFor("default_input_method"), false, this, -1);
            resolver.registerContentObserver(Settings.Secure.getUriFor("volume_hush_gesture"), false, this, -1);
            resolver.registerContentObserver(Settings.Secure.getUriFor("system_navigation_keys_enabled"), false, this, -1);
            resolver.registerContentObserver(Settings.Global.getUriFor("power_button_long_press"), false, this, -1);
            resolver.registerContentObserver(Settings.Global.getUriFor("power_button_very_long_press"), false, this, -1);
            resolver.registerContentObserver(Settings.Global.getUriFor("power_button_suppression_delay_after_gesture_wake"), false, this, -1);
            PhoneWindowManager.this.updateSettings();
        }

        public void onChange(boolean selfChange) {
            PhoneWindowManager.this.updateSettings();
            PhoneWindowManager.this.updateRotation(false);
        }
    }

    class MyWakeGestureListener extends WakeGestureListener {
        MyWakeGestureListener(Context context, Handler handler) {
            super(context, handler);
        }

        @Override // com.android.server.policy.WakeGestureListener
        public void onWakeUp() {
            synchronized (PhoneWindowManager.this.mLock) {
                if (PhoneWindowManager.this.shouldEnableWakeGestureLp()) {
                    boolean unused = PhoneWindowManager.this.performHapticFeedback(1, false, "Wake Up");
                    boolean unused2 = PhoneWindowManager.this.wakeUp(SystemClock.uptimeMillis(), PhoneWindowManager.this.mAllowTheaterModeWakeFromWakeGesture, 4, "android.policy:GESTURE");
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleRingerChordGesture() {
        if (this.mRingerToggleChord != 0) {
            getAudioManagerInternal();
            this.mAudioManagerInternal.silenceRingerModeInternal("volume_hush");
            Settings.Secure.putInt(this.mContext.getContentResolver(), "hush_gesture_used", 1);
            this.mLogger.action(1440, this.mRingerToggleChord);
        }
    }

    /* access modifiers changed from: package-private */
    public IStatusBarService getStatusBarService() {
        IStatusBarService iStatusBarService;
        synchronized (this.mServiceAquireLock) {
            if (this.mStatusBarService == null) {
                this.mStatusBarService = IStatusBarService.Stub.asInterface(ServiceManager.getService("statusbar"));
            }
            iStatusBarService = this.mStatusBarService;
        }
        return iStatusBarService;
    }

    /* access modifiers changed from: package-private */
    public StatusBarManagerInternal getStatusBarManagerInternal() {
        StatusBarManagerInternal statusBarManagerInternal;
        synchronized (this.mServiceAquireLock) {
            if (this.mStatusBarManagerInternal == null) {
                this.mStatusBarManagerInternal = (StatusBarManagerInternal) LocalServices.getService(StatusBarManagerInternal.class);
            }
            statusBarManagerInternal = this.mStatusBarManagerInternal;
        }
        return statusBarManagerInternal;
    }

    /* access modifiers changed from: package-private */
    public AudioManagerInternal getAudioManagerInternal() {
        AudioManagerInternal audioManagerInternal;
        synchronized (this.mServiceAquireLock) {
            if (this.mAudioManagerInternal == null) {
                this.mAudioManagerInternal = (AudioManagerInternal) LocalServices.getService(AudioManagerInternal.class);
            }
            audioManagerInternal = this.mAudioManagerInternal;
        }
        return audioManagerInternal;
    }

    private void interceptBackKeyDown() {
        this.mLogger.count("key_back_down", 1);
        this.mBackKeyHandled = false;
        if (hasLongPressOnBackBehavior()) {
            Message msg = this.mHandler.obtainMessage(16);
            msg.setAsynchronous(true);
            this.mHandler.sendMessageDelayed(msg, ViewConfiguration.get(this.mContext).getDeviceGlobalActionKeyTimeout());
        }
    }

    private boolean interceptBackKeyUp(KeyEvent event) {
        TelecomManager telecomManager;
        this.mLogger.count("key_back_up", 1);
        boolean handled = this.mBackKeyHandled;
        cancelPendingBackKeyAction();
        if (this.mHasFeatureWatch && (telecomManager = getTelecommService()) != null) {
            if (telecomManager.isRinging()) {
                telecomManager.silenceRinger();
                return false;
            } else if ((1 & this.mIncallBackBehavior) != 0 && telecomManager.isInCall()) {
                return telecomManager.endCall();
            }
        }
        if (this.mAutofillManagerInternal != null && event.getKeyCode() == 4) {
            Handler handler = this.mHandler;
            handler.sendMessage(handler.obtainMessage(20));
        }
        return handled;
    }

    private void interceptPowerKeyDown(KeyEvent event, boolean interactive) {
        if (!this.mPowerKeyWakeLock.isHeld()) {
            this.mPowerKeyWakeLock.acquire(JobStatus.DEFAULT_TRIGGER_MAX_DELAY);
        }
        if (this.mPowerKeyPressCounter != 0) {
            this.mHandler.removeMessages(13);
        }
        this.mWindowManagerFuncs.onPowerKeyDown(interactive);
        OppoEngineerPhoneWindowManagerHelper oppoEngineerPhoneWindowManagerHelper = this.mOppoEngineerHelper;
        if (oppoEngineerPhoneWindowManagerHelper != null) {
            oppoEngineerPhoneWindowManagerHelper.onPwkPressed();
        }
        if (interactive && !this.mScreenshotChordPowerKeyTriggered && (event.getFlags() & 1024) == 0) {
            this.mScreenshotChordPowerKeyTriggered = true;
            this.mScreenshotChordPowerKeyTime = event.getDownTime();
            interceptScreenshotChord();
            interceptRingerToggleChord();
        }
        getTelecommService();
        boolean hungUp = colorInterceptPowerKeyForTelephone(event, interactive);
        colorInterceptPowerKeyForAlarm();
        sendSystemKeyToStatusBarAsync(event.getKeyCode());
        schedulePossibleVeryLongPressReboot();
        this.mPowerKeyHandled = hungUp || this.mScreenshotChordVolumeDownKeyTriggered || this.mA11yShortcutChordVolumeUpKeyTriggered || 0 != 0;
        colorInterceptPowerKeyDown(event, interactive);
        if (!this.mPowerKeyHandled) {
            if (!interactive) {
                wakeUpFromPowerKey(event.getDownTime());
                if (this.mSupportLongPressPowerWhenNonInteractive && hasLongPressOnPowerBehavior()) {
                    if ((event.getFlags() & 128) != 0) {
                        powerLongPress();
                    } else {
                        Message msg = this.mHandler.obtainMessage(14);
                        msg.setAsynchronous(true);
                        this.mHandler.sendMessageDelayed(msg, ViewConfiguration.get(this.mContext).getDeviceGlobalActionKeyTimeout());
                        if (hasVeryLongPressOnPowerBehavior()) {
                            Message longMsg = this.mHandler.obtainMessage(25);
                            longMsg.setAsynchronous(true);
                            this.mHandler.sendMessageDelayed(longMsg, (long) this.mVeryLongPressTimeout);
                        }
                    }
                    this.mBeganFromNonInteractive = true;
                } else if (getMaxMultiPressPowerCount() <= 1) {
                    this.mPowerKeyHandled = true;
                } else {
                    this.mBeganFromNonInteractive = true;
                }
            } else if (hasLongPressOnPowerBehavior()) {
                if ((event.getFlags() & 128) != 0) {
                    powerLongPress();
                } else {
                    Message msg2 = this.mHandler.obtainMessage(14);
                    msg2.setAsynchronous(true);
                    this.mHandler.sendMessageDelayed(msg2, 2500);
                    if (hasVeryLongPressOnPowerBehavior()) {
                        Message longMsg2 = this.mHandler.obtainMessage(25);
                        longMsg2.setAsynchronous(true);
                        this.mHandler.sendMessageDelayed(longMsg2, (long) this.mVeryLongPressTimeout);
                    }
                }
            }
        }
        this.hwShutdownRecord.startHwShutdownDectect();
    }

    private void interceptPowerKeyUp(KeyEvent event, boolean interactive, boolean canceled) {
        boolean handled = canceled || this.mPowerKeyHandled || getSpeechLongPressHandle();
        this.mScreenshotChordPowerKeyTriggered = false;
        cancelPendingScreenshotChordAction();
        cancelPendingPowerKeyAction();
        this.hwShutdownRecord.clearHwShutdownDectect();
        OppoEngineerPhoneWindowManagerHelper oppoEngineerPhoneWindowManagerHelper = this.mOppoEngineerHelper;
        if (oppoEngineerPhoneWindowManagerHelper != null) {
            oppoEngineerPhoneWindowManagerHelper.onPwkReleased();
        }
        handlePowerKeyUpForWallet(handled);
        colorInterceptPowerKeyUp(event, interactive, canceled);
        if (!handled) {
            if ((event.getFlags() & 128) == 0) {
                Handler handler = this.mHandler;
                WindowManagerPolicy.WindowManagerFuncs windowManagerFuncs = this.mWindowManagerFuncs;
                Objects.requireNonNull(windowManagerFuncs);
                handler.post(new Runnable() {
                    /* class com.android.server.policy.$$Lambda$oXa0y3A00RiQs6KTPBgpkGtgw */

                    public final void run() {
                        WindowManagerPolicy.WindowManagerFuncs.this.triggerAnimationFailsafe();
                    }
                });
            }
            this.mPowerKeyPressCounter++;
            int maxCount = getMaxMultiPressPowerCount();
            long eventTime = event.getDownTime();
            if (this.mPowerKeyPressCounter < maxCount) {
                Message msg = this.mHandler.obtainMessage(13, interactive ? 1 : 0, this.mPowerKeyPressCounter, Long.valueOf(eventTime));
                msg.setAsynchronous(true);
                this.mHandler.sendMessageDelayed(msg, (long) ViewConfiguration.getMultiPressTimeout());
                return;
            }
            colorInterceptPowerKeyUpForWallet(event, interactive, this.mPowerKeyPressCounter);
        }
        finishPowerKeyPress();
    }

    /* access modifiers changed from: private */
    public void finishPowerKeyPress() {
        this.mBeganFromNonInteractive = false;
        this.mPowerKeyPressCounter = 0;
        if (this.mPowerKeyWakeLock.isHeld()) {
            this.mPowerKeyWakeLock.release();
        }
    }

    /* access modifiers changed from: private */
    public void cancelPendingPowerKeyAction() {
        if (!this.mPowerKeyHandled) {
            this.mPowerKeyHandled = true;
            this.mHandler.removeMessages(14);
        }
        if (hasVeryLongPressOnPowerBehavior()) {
            this.mHandler.removeMessages(25);
        }
        cancelPossibleVeryLongPressReboot();
        extraWorkInCancelPendingPowerKeyAction();
    }

    private void cancelPendingBackKeyAction() {
        if (!this.mBackKeyHandled) {
            this.mBackKeyHandled = true;
            this.mHandler.removeMessages(16);
        }
    }

    /* access modifiers changed from: private */
    public void powerPress(long eventTime, boolean interactive, int count) {
        int i;
        if (!this.mDefaultDisplayPolicy.isScreenOnEarly() || this.mDefaultDisplayPolicy.isScreenOnFully()) {
            Slog.d("WindowManager", "powerPress: eventTime=" + eventTime + " interactive=" + interactive + " count=" + count + " beganFromNonInteractive=" + this.mBeganFromNonInteractive + " mShortPressOnPowerBehavior=" + this.mShortPressOnPowerBehavior);
            if (count == 2) {
                powerMultiPressAction(eventTime, interactive, this.mDoublePressOnPowerBehavior);
            } else if (count == 3) {
                powerMultiPressAction(eventTime, interactive, this.mTriplePressOnPowerBehavior);
            } else if (interactive && !this.mBeganFromNonInteractive && (i = this.mShortPressOnPowerBehavior) != 0) {
                if (i == 1) {
                    goToSleepFromPowerButton(eventTime, 0);
                } else if (i == 2) {
                    goToSleepFromPowerButton(eventTime, 1);
                } else if (i != 3) {
                    if (i == 4) {
                        shortPressPowerGoHome();
                    } else if (i == 5) {
                        if (this.mDismissImeOnBackKeyPressed) {
                            if (this.mInputMethodManagerInternal == null) {
                                this.mInputMethodManagerInternal = (InputMethodManagerInternal) LocalServices.getService(InputMethodManagerInternal.class);
                            }
                            InputMethodManagerInternal inputMethodManagerInternal = this.mInputMethodManagerInternal;
                            if (inputMethodManagerInternal != null) {
                                inputMethodManagerInternal.hideCurrentInputMethod();
                                return;
                            }
                            return;
                        }
                        shortPressPowerGoHome();
                    }
                } else if (goToSleepFromPowerButton(eventTime, 1)) {
                    launchHomeFromHotKey(0);
                }
            }
        } else {
            Slog.i("WindowManager", "Suppressed redundant power key press while already in the process of turning the screen on.");
        }
    }

    private boolean goToSleepFromPowerButton(long eventTime, int flags) {
        PowerManager.WakeData lastWakeUp = this.mPowerManagerInternal.getLastWakeup();
        if (lastWakeUp != null && lastWakeUp.wakeReason == 4) {
            Settings.Global.getInt(this.mContext.getContentResolver(), "power_button_suppression_delay_after_gesture_wake", POWER_BUTTON_SUPPRESSION_DELAY_DEFAULT_MILLIS);
            long now = SystemClock.uptimeMillis();
            if (this.mPowerButtonSuppressionDelayMillis > 0 && now < lastWakeUp.wakeTime + ((long) this.mPowerButtonSuppressionDelayMillis)) {
                Slog.i("WindowManager", "Sleep from power button suppressed. Time since gesture: " + (now - lastWakeUp.wakeTime) + "ms");
                return false;
            }
        }
        goToSleep(eventTime, 4, flags);
        return true;
    }

    private void goToSleep(long eventTime, int reason, int flags) {
        this.mRequestedOrGoingToSleep = true;
        this.mPowerManager.goToSleep(eventTime, reason, flags);
    }

    private void shortPressPowerGoHome() {
        launchHomeFromHotKey(0, true, false);
        if (isKeyguardShowingAndNotOccluded()) {
            this.mKeyguardDelegate.onShortPowerPressedGoHome();
        }
    }

    private void powerMultiPressAction(long eventTime, boolean interactive, int behavior) {
        if (behavior == 0) {
            return;
        }
        if (behavior != 1) {
            if (behavior == 2) {
                Slog.i("WindowManager", "Starting brightness boost.");
                if (!interactive) {
                    wakeUpFromPowerKey(eventTime);
                }
                this.mPowerManager.boostScreenBrightness(eventTime);
            }
        } else if (!isUserSetupComplete()) {
            Slog.i("WindowManager", "Ignoring toggling theater mode - device not setup.");
        } else if (isTheaterModeEnabled()) {
            Slog.i("WindowManager", "Toggling theater mode off.");
            Settings.Global.putInt(this.mContext.getContentResolver(), "theater_mode_on", 0);
            if (!interactive) {
                wakeUpFromPowerKey(eventTime);
            }
        } else {
            Slog.i("WindowManager", "Toggling theater mode on.");
            Settings.Global.putInt(this.mContext.getContentResolver(), "theater_mode_on", 1);
            if (this.mGoToSleepOnButtonPressTheaterMode && interactive) {
                goToSleep(eventTime, 4, 0);
            }
        }
    }

    private int getLidBehavior() {
        return Settings.Global.getInt(this.mContext.getContentResolver(), "lid_behavior", 0);
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

    /* access modifiers changed from: private */
    public void powerLongPress() {
        int behavior = getResolvedLongPressOnPowerBehavior();
        if (behavior != 0) {
            boolean z = true;
            if (behavior == 1) {
                this.mPowerKeyHandled = true;
                if (!colorInterceptLongPowerPress()) {
                    showGlobalActionsInternal();
                }
            } else if (behavior == 2 || behavior == 3) {
                this.mPowerKeyHandled = true;
                performHapticFeedback(0, false, "Power - Long Press - Shut Off");
                sendCloseSystemWindows(SYSTEM_DIALOG_REASON_GLOBAL_ACTIONS);
                WindowManagerPolicy.WindowManagerFuncs windowManagerFuncs = this.mWindowManagerFuncs;
                if (behavior != 2) {
                    z = false;
                }
                windowManagerFuncs.shutdown(z);
            } else if (behavior == 4) {
                this.mPowerKeyHandled = true;
                performHapticFeedback(0, false, "Power - Long Press - Go To Voice Assist");
                launchVoiceAssist(this.mAllowStartActivityForLongPressOnPowerDuringSetup);
            } else if (behavior == 5) {
                this.mPowerKeyHandled = true;
                performHapticFeedback(0, false, "Power - Long Press - Go To Assistant");
                launchAssistAction(null, Integer.MIN_VALUE, OppoBasePhoneWindowManager.AssistManagerLaunchMode.DEFAULT);
            }
        }
    }

    /* access modifiers changed from: private */
    public void powerVeryLongPress() {
        int i = this.mVeryLongPressOnPowerBehavior;
        if (i != 0 && i == 1) {
            this.mPowerKeyHandled = true;
            performHapticFeedback(0, false, "Power - Very Long Press - Show Global Actions");
            showGlobalActionsInternal();
        }
    }

    /* access modifiers changed from: private */
    public void backLongPress() {
        this.mBackKeyHandled = true;
        int i = this.mLongPressOnBackBehavior;
        if (i != 0 && i == 1) {
            launchVoiceAssist(false);
        }
    }

    /* access modifiers changed from: private */
    public void accessibilityShortcutActivated() {
        this.mAccessibilityShortcutController.performAccessibilityShortcut();
    }

    private void sleepPress() {
        if (this.mShortPressOnSleepBehavior == 1) {
            launchHomeFromHotKey(0, false, true);
        }
    }

    private void sleepRelease(long eventTime) {
        int i = this.mShortPressOnSleepBehavior;
        if (i == 0 || i == 1) {
            Slog.i("WindowManager", "sleepRelease() calling goToSleep(GO_TO_SLEEP_REASON_SLEEP_BUTTON)");
            goToSleep(eventTime, 6, 0);
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

    private boolean hasVeryLongPressOnPowerBehavior() {
        return this.mVeryLongPressOnPowerBehavior != 0;
    }

    private boolean hasLongPressOnBackBehavior() {
        return this.mLongPressOnBackBehavior != 0;
    }

    private void interceptScreenshotChord() {
        if (this.mScreenshotChordEnabled && this.mScreenshotChordVolumeDownKeyTriggered && this.mScreenshotChordPowerKeyTriggered && !this.mA11yShortcutChordVolumeUpKeyTriggered) {
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
        if (this.mAccessibilityShortcutController.isAccessibilityShortcutAvailable(isKeyguardLocked()) && this.mScreenshotChordVolumeDownKeyTriggered && this.mA11yShortcutChordVolumeUpKeyTriggered && !this.mScreenshotChordPowerKeyTriggered) {
            long now = SystemClock.uptimeMillis();
            if (now <= this.mScreenshotChordVolumeDownKeyTime + SCREENSHOT_CHORD_DEBOUNCE_DELAY_MILLIS && now <= this.mA11yShortcutChordVolumeUpKeyTime + SCREENSHOT_CHORD_DEBOUNCE_DELAY_MILLIS) {
                this.mScreenshotChordVolumeDownKeyConsumed = true;
                this.mA11yShortcutChordVolumeUpKeyConsumed = true;
                Handler handler = this.mHandler;
                handler.sendMessageDelayed(handler.obtainMessage(17), getAccessibilityShortcutTimeout());
            }
        }
    }

    private void interceptRingerToggleChord() {
        if (this.mRingerToggleChord != 0 && this.mScreenshotChordPowerKeyTriggered && this.mA11yShortcutChordVolumeUpKeyTriggered) {
            long now = SystemClock.uptimeMillis();
            if (now <= this.mA11yShortcutChordVolumeUpKeyTime + SCREENSHOT_CHORD_DEBOUNCE_DELAY_MILLIS && now <= this.mScreenshotChordPowerKeyTime + SCREENSHOT_CHORD_DEBOUNCE_DELAY_MILLIS) {
                this.mA11yShortcutChordVolumeUpKeyConsumed = true;
                cancelPendingPowerKeyAction();
                Handler handler = this.mHandler;
                handler.sendMessageDelayed(handler.obtainMessage(27), getRingerToggleChordDelay());
            }
        }
    }

    private long getAccessibilityShortcutTimeout() {
        ViewConfiguration config = ViewConfiguration.get(this.mContext);
        if (Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "accessibility_shortcut_dialog_shown", 0, this.mCurrentUserId) == 0) {
            return config.getAccessibilityShortcutKeyTimeout();
        }
        return config.getAccessibilityShortcutKeyTimeoutAfterConfirmation();
    }

    private long getScreenshotChordLongPressDelay() {
        return 0;
    }

    private long getRingerToggleChordDelay() {
        return (long) ViewConfiguration.getTapTimeout();
    }

    private void cancelPendingScreenshotChordAction() {
        this.mHandler.removeCallbacks(this.mScreenshotRunnable);
    }

    private void cancelPendingAccessibilityShortcutAction() {
        this.mHandler.removeMessages(17);
    }

    private void cancelPendingRingerToggleChordAction() {
        this.mHandler.removeMessages(27);
    }

    private class ScreenshotRunnable implements Runnable {
        private int mScreenshotType;

        private ScreenshotRunnable() {
            this.mScreenshotType = 1;
        }

        public void setScreenshotType(int screenshotType) {
            this.mScreenshotType = screenshotType;
        }

        public void run() {
            PhoneWindowManager.this.mDefaultDisplayPolicy.takeScreenshot(this.mScreenshotType);
            SystemProperties.set("ctl.start", "gettpinfo");
        }
    }

    @Override // com.android.server.policy.WindowManagerPolicy
    public void showGlobalActions() {
        this.mHandler.removeMessages(10);
        this.mHandler.sendEmptyMessage(10);
    }

    /* access modifiers changed from: package-private */
    public void showGlobalActionsInternal() {
        if (this.mGlobalActions == null) {
            this.mGlobalActions = new GlobalActions(this.mContext, this.mWindowManagerFuncs);
        }
        this.mGlobalActions.showDialog(isKeyguardShowingAndNotOccluded(), isDeviceProvisioned());
        this.mPowerManager.userActivity(SystemClock.uptimeMillis(), false);
    }

    /* access modifiers changed from: package-private */
    public boolean isDeviceProvisioned() {
        return Settings.Global.getInt(this.mContext.getContentResolver(), "device_provisioned", 0) != 0;
    }

    @Override // com.android.server.policy.WindowManagerPolicy
    public boolean isUserSetupComplete() {
        boolean isSetupComplete = false;
        if (Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "user_setup_complete", 0, -2) != 0) {
            isSetupComplete = true;
        }
        if (this.mHasFeatureLeanback) {
            return isSetupComplete & isTvUserSetupComplete();
        }
        return isSetupComplete;
    }

    private boolean isTvUserSetupComplete() {
        return Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "tv_user_setup_complete", 0, -2) != 0;
    }

    /* access modifiers changed from: private */
    public void handleShortPressOnHome(int displayId) {
        HdmiControl hdmiControl = getHdmiControl();
        if (hdmiControl != null) {
            hdmiControl.turnOnTv();
        }
        DreamManagerInternal dreamManagerInternal = this.mDreamManagerInternal;
        if (dreamManagerInternal == null || !dreamManagerInternal.isDreaming()) {
            launchHomeFromHotKey(displayId);
        } else {
            this.mDreamManagerInternal.stopDream(false);
        }
    }

    private HdmiControl getHdmiControl() {
        if (this.mHdmiControl == null) {
            if (!this.mHasFeatureHdmiCec) {
                return null;
            }
            HdmiControlManager manager = (HdmiControlManager) this.mContext.getSystemService("hdmi_control");
            HdmiPlaybackClient client = null;
            if (manager != null) {
                client = manager.getPlaybackClient();
            }
            this.mHdmiControl = new HdmiControl(client);
        }
        return this.mHdmiControl;
    }

    private static class HdmiControl {
        private final HdmiPlaybackClient mClient;

        private HdmiControl(HdmiPlaybackClient client) {
            this.mClient = client;
        }

        public void turnOnTv() {
            HdmiPlaybackClient hdmiPlaybackClient = this.mClient;
            if (hdmiPlaybackClient != null) {
                hdmiPlaybackClient.oneTouchPlay(new HdmiPlaybackClient.OneTouchPlayCallback() {
                    /* class com.android.server.policy.PhoneWindowManager.HdmiControl.AnonymousClass1 */

                    public void onComplete(int result) {
                        if (result != 0) {
                            Log.w("WindowManager", "One touch play failed: " + result);
                        }
                    }
                });
            }
        }
    }

    /* access modifiers changed from: private */
    public void launchAllAppsAction() {
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

    private void showPictureInPictureMenu(KeyEvent event) {
        if (DEBUG_INPUT) {
            Log.d("WindowManager", "showPictureInPictureMenu event=" + event);
        }
        this.mHandler.removeMessages(15);
        Message msg = this.mHandler.obtainMessage(15);
        msg.setAsynchronous(true);
        msg.sendToTarget();
    }

    /* access modifiers changed from: private */
    public void showPictureInPictureMenuInternal() {
        StatusBarManagerInternal statusbar = getStatusBarManagerInternal();
        if (statusbar != null) {
            statusbar.showPictureInPictureMenu();
        }
    }

    /* access modifiers changed from: private */
    public class DisplayHomeButtonHandler {
        /* access modifiers changed from: private */
        public final int mDisplayId;
        private boolean mHomeConsumed;
        /* access modifiers changed from: private */
        public boolean mHomeDoubleTapPending;
        private final Runnable mHomeDoubleTapTimeoutRunnable = new Runnable() {
            /* class com.android.server.policy.PhoneWindowManager.DisplayHomeButtonHandler.AnonymousClass1 */

            public void run() {
                if (DisplayHomeButtonHandler.this.mHomeDoubleTapPending) {
                    boolean unused = DisplayHomeButtonHandler.this.mHomeDoubleTapPending = false;
                    PhoneWindowManager.this.handleShortPressOnHome(DisplayHomeButtonHandler.this.mDisplayId);
                }
            }
        };
        private boolean mHomePressed;

        DisplayHomeButtonHandler(int displayId) {
            this.mDisplayId = displayId;
        }

        /* access modifiers changed from: package-private */
        public int handleHomeButton(WindowManagerPolicy.WindowState win, KeyEvent event) {
            boolean keyguardOn = PhoneWindowManager.this.keyguardOn();
            int repeatCount = event.getRepeatCount();
            boolean down = event.getAction() == 0;
            boolean canceled = event.isCanceled();
            if (PhoneWindowManager.DEBUG_INPUT) {
                Log.d("WindowManager", String.format("handleHomeButton in display#%d mHomePressed = %b", Integer.valueOf(this.mDisplayId), Boolean.valueOf(this.mHomePressed)));
            }
            if (!down) {
                if (this.mDisplayId == 0) {
                    PhoneWindowManager.this.cancelPreloadRecentApps();
                }
                this.mHomePressed = false;
                if (this.mHomeConsumed) {
                    this.mHomeConsumed = false;
                    return -1;
                } else if (canceled) {
                    Log.i("WindowManager", "Ignoring HOME; event canceled.");
                    return -1;
                } else if (PhoneWindowManager.this.mDoubleTapOnHomeBehavior != 0) {
                    PhoneWindowManager.this.mHandler.removeCallbacks(this.mHomeDoubleTapTimeoutRunnable);
                    this.mHomeDoubleTapPending = true;
                    PhoneWindowManager.this.mHandler.postDelayed(this.mHomeDoubleTapTimeoutRunnable, (long) ViewConfiguration.getDoubleTapTimeout());
                    return -1;
                } else {
                    PhoneWindowManager.this.mHandler.post(new Runnable() {
                        /* class com.android.server.policy.$$Lambda$PhoneWindowManager$DisplayHomeButtonHandler$ljCIzo7y96OZCYYMVaAi6LAwRAE */

                        public final void run() {
                            PhoneWindowManager.DisplayHomeButtonHandler.this.lambda$handleHomeButton$0$PhoneWindowManager$DisplayHomeButtonHandler();
                        }
                    });
                    return -1;
                }
            } else {
                WindowManager.LayoutParams attrs = win != null ? win.getAttrs() : null;
                if (attrs != null) {
                    int type = attrs.type;
                    if (type == 2009 || (attrs.privateFlags & 1024) != 0) {
                        return 0;
                    }
                    for (int t : PhoneWindowManager.WINDOW_TYPES_WHERE_HOME_DOESNT_WORK) {
                        if (type == t) {
                            return -1;
                        }
                    }
                }
                if (repeatCount == 0) {
                    this.mHomePressed = true;
                    if (this.mHomeDoubleTapPending) {
                        this.mHomeDoubleTapPending = false;
                        PhoneWindowManager.this.mHandler.removeCallbacks(this.mHomeDoubleTapTimeoutRunnable);
                        handleDoubleTapOnHome();
                    } else if (PhoneWindowManager.this.mDoubleTapOnHomeBehavior == 1 && this.mDisplayId == 0) {
                        PhoneWindowManager.this.preloadRecentApps();
                    }
                } else if ((event.getFlags() & 128) != 0 && !keyguardOn) {
                    PhoneWindowManager.this.mHandler.post(new Runnable(event) {
                        /* class com.android.server.policy.$$Lambda$PhoneWindowManager$DisplayHomeButtonHandler$mDqq2TX5_l1ydQz3e0WFhnBNreI */
                        private final /* synthetic */ KeyEvent f$1;

                        {
                            this.f$1 = r2;
                        }

                        public final void run() {
                            PhoneWindowManager.DisplayHomeButtonHandler.this.lambda$handleHomeButton$1$PhoneWindowManager$DisplayHomeButtonHandler(this.f$1);
                        }
                    });
                }
                return -1;
            }
        }

        public /* synthetic */ void lambda$handleHomeButton$0$PhoneWindowManager$DisplayHomeButtonHandler() {
            PhoneWindowManager.this.handleShortPressOnHome(this.mDisplayId);
        }

        public /* synthetic */ void lambda$handleHomeButton$1$PhoneWindowManager$DisplayHomeButtonHandler(KeyEvent event) {
            handleLongPressOnHome(event.getDeviceId());
        }

        private void handleDoubleTapOnHome() {
            if (PhoneWindowManager.this.mDoubleTapOnHomeBehavior == 1) {
                this.mHomeConsumed = true;
                PhoneWindowManager.this.toggleRecentApps();
            }
        }

        private void handleLongPressOnHome(int deviceId) {
            if (PhoneWindowManager.this.mLongPressOnHomeBehavior != 0 && !PhoneWindowManager.this.colorInterceptLongHomePress()) {
                this.mHomeConsumed = true;
                boolean unused = PhoneWindowManager.this.performHapticFeedback(0, false, "Home - Long Press");
                int access$3300 = PhoneWindowManager.this.mLongPressOnHomeBehavior;
                if (access$3300 == 1) {
                    PhoneWindowManager.this.launchAllAppsAction();
                } else if (access$3300 != 2) {
                    Log.w("WindowManager", "Undefined home long press behavior: " + PhoneWindowManager.this.mLongPressOnHomeBehavior);
                } else {
                    PhoneWindowManager.this.launchAssistAction(null, deviceId, OppoBasePhoneWindowManager.AssistManagerLaunchMode.DEFAULT);
                }
            }
        }

        public String toString() {
            return String.format("mDisplayId = %d, mHomePressed = %b", Integer.valueOf(this.mDisplayId), Boolean.valueOf(this.mHomePressed));
        }
    }

    private boolean isRoundWindow() {
        return this.mContext.getResources().getConfiguration().isScreenRound();
    }

    @Override // com.android.server.policy.WindowManagerPolicy
    public void setDefaultDisplay(WindowManagerPolicy.DisplayContentInfo displayContentInfo) {
        this.mDefaultDisplay = displayContentInfo.getDisplay();
        this.mDefaultDisplayRotation = displayContentInfo.getDisplayRotation();
        this.mDefaultDisplayPolicy = this.mDefaultDisplayRotation.getDisplayPolicy();
    }

    @Override // com.android.server.policy.WindowManagerPolicy
    public void init(Context context, IWindowManager windowManager, WindowManagerPolicy.WindowManagerFuncs windowManagerFuncs) {
        int maxRadius;
        int maxVertical;
        int minVertical;
        int maxHorizontal;
        int minHorizontal;
        this.mContext = context;
        this.mWindowManager = windowManager;
        this.mWindowManagerFuncs = windowManagerFuncs;
        this.mWindowManagerInternal = (WindowManagerInternal) LocalServices.getService(WindowManagerInternal.class);
        this.mActivityManagerInternal = (ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class);
        this.mActivityTaskManagerInternal = (ActivityTaskManagerInternal) LocalServices.getService(ActivityTaskManagerInternal.class);
        this.mInputManagerInternal = (InputManagerInternal) LocalServices.getService(InputManagerInternal.class);
        this.mDreamManagerInternal = (DreamManagerInternal) LocalServices.getService(DreamManagerInternal.class);
        this.mDisplayManagerInternal = (DisplayManagerInternal) LocalServices.getService(DisplayManagerInternal.class);
        this.isTouchFingerPrintSensor = context.getPackageManager().hasSystemFeature(SupportUtil.FRONT_TOUCH_SENSOR);
        this.mIsEnableKeyguardHide = this.mContext.getPackageManager().hasSystemFeature("oppo.fingerprint.enable.keyguard.hide");
        this.mPowerManagerInternal = (PowerManagerInternal) LocalServices.getService(PowerManagerInternal.class);
        this.mAppOpsManager = (AppOpsManager) this.mContext.getSystemService(AppOpsManager.class);
        this.mDisplayManager = (DisplayManager) this.mContext.getSystemService(DisplayManager.class);
        this.mHasFeatureWatch = this.mContext.getPackageManager().hasSystemFeature("android.hardware.type.watch");
        this.mHasFeatureLeanback = this.mContext.getPackageManager().hasSystemFeature("android.software.leanback");
        this.mHasFeatureHdmiCec = this.mContext.getPackageManager().hasSystemFeature("android.hardware.hdmi.cec");
        this.mAccessibilityShortcutController = new AccessibilityShortcutController(this.mContext, new Handler(), this.mCurrentUserId);
        this.mLogger = new MetricsLogger();
        boolean burnInProtectionEnabled = context.getResources().getBoolean(17891434);
        boolean burnInProtectionDevMode = SystemProperties.getBoolean("persist.debug.force_burn_in", false);
        if (burnInProtectionEnabled || burnInProtectionDevMode) {
            if (burnInProtectionDevMode) {
                minHorizontal = -8;
                maxHorizontal = 8;
                minVertical = -8;
                maxVertical = -4;
                maxRadius = isRoundWindow() ? 6 : -1;
            } else {
                Resources resources = context.getResources();
                int minHorizontal2 = resources.getInteger(17694756);
                int maxHorizontal2 = resources.getInteger(17694753);
                int minVertical2 = resources.getInteger(17694757);
                int maxVertical2 = resources.getInteger(17694755);
                maxRadius = resources.getInteger(17694754);
                minHorizontal = minHorizontal2;
                maxHorizontal = maxHorizontal2;
                minVertical = minVertical2;
                maxVertical = maxVertical2;
            }
            this.mBurnInProtectionHelper = new BurnInProtectionHelper(context, minHorizontal, maxHorizontal, minVertical, maxVertical, maxRadius);
        }
        this.mHandler = new PolicyHandler();
        this.mPhoneWinHandlerThread = new ServiceThread("PhoneWinHandlerThread", -4, false);
        this.mPhoneWinHandlerThread.start();
        this.mPhoneWinHandler = new PhoneWinHandler(this.mPhoneWinHandlerThread.getLooper());
        this.mOppoEngineerHelper = new OppoEngineerPhoneWindowManagerHelper();
        this.mWakeGestureListener = new MyWakeGestureListener(this.mContext, this.mHandler);
        this.mSettingsObserver = new SettingsObserver(this.mHandler);
        this.mSettingsObserver.observe();
        this.mShortcutManager = new ShortcutManager(context);
        this.mUiMode = context.getResources().getInteger(17694782);
        this.mHomeIntent = new Intent("android.intent.action.MAIN", (Uri) null);
        this.mHomeIntent.addCategory("android.intent.category.HOME");
        this.mHomeIntent.addFlags(270532608);
        this.mEnableCarDockHomeCapture = context.getResources().getBoolean(17891435);
        this.mCarDockIntent = new Intent("android.intent.action.MAIN", (Uri) null);
        this.mCarDockIntent.addCategory("android.intent.category.CAR_DOCK");
        this.mCarDockIntent.addFlags(270532608);
        this.mDeskDockIntent = new Intent("android.intent.action.MAIN", (Uri) null);
        this.mDeskDockIntent.addCategory("android.intent.category.DESK_DOCK");
        this.mDeskDockIntent.addFlags(270532608);
        this.mVrHeadsetHomeIntent = new Intent("android.intent.action.MAIN", (Uri) null);
        this.mVrHeadsetHomeIntent.addCategory("android.intent.category.VR_HOME");
        this.mVrHeadsetHomeIntent.addFlags(270532608);
        this.mPowerManager = (PowerManager) context.getSystemService("power");
        boolean z = true;
        this.mBroadcastWakeLock = this.mPowerManager.newWakeLock(1, "PhoneWindowManager.mBroadcastWakeLock");
        this.mPowerKeyWakeLock = this.mPowerManager.newWakeLock(1, "PhoneWindowManager.mPowerKeyWakeLock");
        this.mEnableShiftMenuBugReports = "1".equals(SystemProperties.get("ro.debuggable"));
        this.mLidKeyboardAccessibility = this.mContext.getResources().getInteger(17694819);
        this.mLidNavigationAccessibility = this.mContext.getResources().getInteger(17694820);
        this.mLidControlsDisplayFold = this.mContext.getResources().getBoolean(17891471);
        this.mAllowTheaterModeWakeFromKey = this.mContext.getResources().getBoolean(17891351);
        this.mAllowTheaterModeWakeFromPowerKey = this.mAllowTheaterModeWakeFromKey || this.mContext.getResources().getBoolean(17891355);
        this.mAllowTheaterModeWakeFromMotion = this.mContext.getResources().getBoolean(17891353);
        this.mAllowTheaterModeWakeFromMotionWhenNotDreaming = this.mContext.getResources().getBoolean(17891354);
        this.mAllowTheaterModeWakeFromCameraLens = this.mContext.getResources().getBoolean(17891348);
        this.mAllowTheaterModeWakeFromLidSwitch = this.mContext.getResources().getBoolean(17891352);
        this.mAllowTheaterModeWakeFromWakeGesture = this.mContext.getResources().getBoolean(17891350);
        this.mGoToSleepOnButtonPressTheaterMode = this.mContext.getResources().getBoolean(17891461);
        this.mSupportLongPressPowerWhenNonInteractive = this.mContext.getResources().getBoolean(17891533);
        this.mLongPressOnBackBehavior = this.mContext.getResources().getInteger(17694824);
        this.mShortPressOnPowerBehavior = this.mContext.getResources().getInteger(17694892);
        this.mLongPressOnPowerBehavior = this.mContext.getResources().getInteger(17694826);
        this.mVeryLongPressOnPowerBehavior = this.mContext.getResources().getInteger(17694908);
        this.mDoublePressOnPowerBehavior = this.mContext.getResources().getInteger(17694796);
        this.mTriplePressOnPowerBehavior = this.mContext.getResources().getInteger(17694905);
        this.mShortPressOnSleepBehavior = this.mContext.getResources().getInteger(17694893);
        this.mVeryLongPressTimeout = this.mContext.getResources().getInteger(17694909);
        this.mAllowStartActivityForLongPressOnPowerDuringSetup = this.mContext.getResources().getBoolean(17891347);
        this.mHapticTextHandleEnabled = this.mContext.getResources().getBoolean(17891440);
        if (AudioSystem.getPlatformType(this.mContext) != 2) {
            z = false;
        }
        this.mUseTvRouting = z;
        this.mHandleVolumeKeysInWM = this.mContext.getResources().getBoolean(17891463);
        this.mPerDisplayFocusEnabled = this.mContext.getResources().getBoolean(17891332);
        readConfigurationDependentBehaviors();
        if (this.mLidControlsDisplayFold) {
            this.mDisplayFoldController = DisplayFoldController.create(context, 0);
        } else if (SystemProperties.getBoolean("persist.debug.force_foldable", false)) {
            this.mDisplayFoldController = DisplayFoldController.createWithProxSensor(context, 0);
        }
        this.mAccessibilityManager = (AccessibilityManager) context.getSystemService("accessibility");
        IntentFilter filter = new IntentFilter();
        filter.addAction(UiModeManager.ACTION_ENTER_CAR_MODE);
        filter.addAction(UiModeManager.ACTION_EXIT_CAR_MODE);
        filter.addAction(UiModeManager.ACTION_ENTER_DESK_MODE);
        filter.addAction(UiModeManager.ACTION_EXIT_DESK_MODE);
        filter.addAction("android.intent.action.DOCK_EVENT");
        Intent intent = context.registerReceiver(this.mDockReceiver, filter);
        if (intent != null) {
            this.mDefaultDisplayPolicy.setDockMode(intent.getIntExtra("android.intent.extra.DOCK_STATE", 0));
        }
        IntentFilter filter2 = new IntentFilter();
        filter2.addAction("android.intent.action.DREAMING_STARTED");
        filter2.addAction("android.intent.action.DREAMING_STOPPED");
        context.registerReceiver(this.mDreamReceiver, filter2);
        context.registerReceiver(this.mMultiuserReceiver, new IntentFilter("android.intent.action.USER_SWITCHED"));
        context.registerReceiver(this.mOppoScreencapReceiver, new IntentFilter("oppo.intent.action.SCREEN_SHOT"));
        this.mVibrator = (Vibrator) context.getSystemService("vibrator");
        this.mLongPressVibePattern = getLongIntArray(this.mContext.getResources(), 17236042);
        this.mCalendarDateVibePattern = getLongIntArray(this.mContext.getResources(), 17235998);
        this.mSafeModeEnabledVibePattern = getLongIntArray(this.mContext.getResources(), 17236061);
        this.mScreenshotChordEnabled = this.mContext.getResources().getBoolean(17891448);
        this.mGlobalKeyManager = new GlobalKeyManager(this.mContext);
        initializeHdmiState();
        if (!this.mPowerManager.isInteractive()) {
            startedGoingToSleep(2);
            finishedGoingToSleep(2);
        }
        this.mWindowManagerInternal.registerAppTransitionListener(new WindowManagerInternal.AppTransitionListener() {
            /* class com.android.server.policy.PhoneWindowManager.AnonymousClass7 */

            @Override // com.android.server.wm.WindowManagerInternal.AppTransitionListener
            public int onAppTransitionStartingLocked(int transit, long duration, long statusBarAnimationStartTime, long statusBarAnimationDuration) {
                return PhoneWindowManager.this.handleStartTransitionForKeyguardLw(transit, duration);
            }

            @Override // com.android.server.wm.WindowManagerInternal.AppTransitionListener
            public void onAppTransitionCancelledLocked(int transit) {
                int unused = PhoneWindowManager.this.handleStartTransitionForKeyguardLw(transit, 0);
            }
        });
        this.mKeyguardDelegate = new KeyguardServiceDelegate(this.mContext, new KeyguardStateMonitor.StateCallback() {
            /* class com.android.server.policy.PhoneWindowManager.AnonymousClass8 */

            @Override // com.android.server.policy.keyguard.KeyguardStateMonitor.StateCallback
            public void onTrustedChanged() {
                PhoneWindowManager.this.mWindowManagerFuncs.notifyKeyguardTrustedChanged();
            }

            @Override // com.android.server.policy.keyguard.KeyguardStateMonitor.StateCallback
            public void onShowingChanged() {
                PhoneWindowManager.this.mWindowManagerFuncs.onKeyguardShowingAndNotOccludedChanged();
            }
        });
        HandlerThread mHandlerThread = new HandlerThread("hwShutDownRecord");
        mHandlerThread.start();
        this.hwShutdownRecord = new HwShutdownRecord(new Handler(mHandlerThread.getLooper()));
        this.mInner = new OppoPhoneWindowManagerInner();
    }

    private void readConfigurationDependentBehaviors() {
        Resources res = this.mContext.getResources();
        this.mLongPressOnHomeBehavior = res.getInteger(17694825);
        this.mLongPressOnHomeBehavior = colorUpdateConfigurationDependentBehaviors(this.mLongPressOnHomeBehavior);
        int i = this.mLongPressOnHomeBehavior;
        if (i < 0 || i > 2) {
            this.mLongPressOnHomeBehavior = 0;
        }
        this.mDoubleTapOnHomeBehavior = res.getInteger(17694797);
        int i2 = this.mDoubleTapOnHomeBehavior;
        if (i2 < 0 || i2 > 1) {
            this.mDoubleTapOnHomeBehavior = 0;
        }
        this.mShortPressOnWindowBehavior = 0;
        if (this.mContext.getPackageManager().hasSystemFeature("android.software.picture_in_picture")) {
            this.mShortPressOnWindowBehavior = 1;
        }
    }

    public void updateSettings() {
        ContentResolver resolver = this.mContext.getContentResolver();
        boolean updateRotation = false;
        synchronized (this.mLock) {
            this.mEndcallBehavior = Settings.System.getIntForUser(resolver, "end_button_behavior", 2, -2);
            this.mIncallPowerBehavior = Settings.Secure.getIntForUser(resolver, "incall_power_button_behavior", 1, -2);
            boolean hasSoftInput = false;
            this.mIncallBackBehavior = Settings.Secure.getIntForUser(resolver, "incall_back_button_behavior", 0, -2);
            this.mSystemNavigationKeysEnabled = Settings.Secure.getIntForUser(resolver, "system_navigation_keys_enabled", 0, -2) == 1;
            this.mRingerToggleChord = Settings.Secure.getIntForUser(resolver, "volume_hush_gesture", 0, -2);
            this.mPowerButtonSuppressionDelayMillis = Settings.Global.getInt(resolver, "power_button_suppression_delay_after_gesture_wake", POWER_BUTTON_SUPPRESSION_DELAY_DEFAULT_MILLIS);
            if (!this.mContext.getResources().getBoolean(17891572)) {
                this.mRingerToggleChord = 0;
            }
            boolean wakeGestureEnabledSetting = Settings.Secure.getIntForUser(resolver, "wake_gesture_enabled", 0, -2) != 0;
            if (this.mWakeGestureEnabledSetting != wakeGestureEnabledSetting) {
                this.mWakeGestureEnabledSetting = wakeGestureEnabledSetting;
                updateWakeGestureListenerLp();
            }
            this.mLockScreenTimeout = Settings.System.getIntForUser(resolver, "screen_off_timeout", 0, -2);
            String imId = Settings.Secure.getStringForUser(resolver, "default_input_method", -2);
            if (imId != null && imId.length() > 0) {
                hasSoftInput = true;
            }
            if (this.mHasSoftInput != hasSoftInput) {
                this.mHasSoftInput = hasSoftInput;
                updateRotation = true;
            }
            this.mLongPressOnPowerBehavior = Settings.Global.getInt(resolver, "power_button_long_press", this.mContext.getResources().getInteger(17694826));
            this.mVeryLongPressOnPowerBehavior = Settings.Global.getInt(resolver, "power_button_very_long_press", this.mContext.getResources().getInteger(17694908));
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

    /* access modifiers changed from: private */
    public boolean shouldEnableWakeGestureLp() {
        if (!this.mWakeGestureEnabledSetting || this.mDefaultDisplayPolicy.isAwake() || ((getLidBehavior() == 1 && this.mDefaultDisplayPolicy.getLidState() == 0) || !this.mWakeGestureListener.isSupported())) {
            return false;
        }
        return true;
    }

    @Override // com.android.server.policy.WindowManagerPolicy
    public int checkAddPermission(WindowManager.LayoutParams attrs, int[] outAppOp) {
        ApplicationInfo appInfo;
        int type = attrs.type;
        if (((attrs.privateFlags & DumpState.DUMP_DEXOPT) != 0) && this.mContext.checkCallingOrSelfPermission("android.permission.INTERNAL_SYSTEM_WINDOW") != 0) {
            return -8;
        }
        outAppOp[0] = -1;
        if ((type < 1 || type > 99) && ((type < 1000 || type > 1999) && (type < 2000 || type > 2999))) {
            return -10;
        }
        if (type < 2000 || type > 2999) {
            return 0;
        }
        if (WindowManager.LayoutParams.isSystemAlertWindowType(type)) {
            outAppOp[0] = 24;
            int callingUid = Binder.getCallingUid();
            if (UserHandle.getAppId(callingUid) == 1000) {
                return 0;
            }
            try {
                appInfo = this.mContext.getPackageManager().getApplicationInfoAsUser(attrs.packageName, 0, UserHandle.getUserId(callingUid));
            } catch (PackageManager.NameNotFoundException e) {
                appInfo = null;
            }
            if (appInfo != null && (type == 2038 || appInfo.targetSdkVersion < 26)) {
                int mode = this.mAppOpsManager.noteOpNoThrow(outAppOp[0], callingUid, attrs.packageName);
                if (mode == 0 || mode == 1) {
                    return 0;
                }
                if (mode != 2) {
                    if (this.mContext.checkCallingOrSelfPermission("android.permission.SYSTEM_ALERT_WINDOW") == 0) {
                        return 0;
                    }
                    return -8;
                } else if (appInfo.targetSdkVersion < 23) {
                    return 0;
                } else {
                    return -8;
                }
            } else if (this.mContext.checkCallingOrSelfPermission("android.permission.INTERNAL_SYSTEM_WINDOW") == 0) {
                return 0;
            } else {
                return -8;
            }
        } else if (type != 2005) {
            if (!(type == 2011 || type == 2013 || type == 2023 || type == 2035 || type == 2037)) {
                switch (type) {
                    case 2030:
                    case 2031:
                    case 2032:
                        break;
                    default:
                        if (this.mContext.checkCallingOrSelfPermission("android.permission.INTERNAL_SYSTEM_WINDOW") == 0) {
                            return 0;
                        }
                        return -8;
                }
            }
            return 0;
        } else {
            outAppOp[0] = 45;
            return 0;
        }
    }

    @Override // com.android.server.policy.WindowManagerPolicy
    public boolean checkShowToOwnerOnly(WindowManager.LayoutParams attrs) {
        int i = attrs.type;
        if (!(i == 3 || i == 2014 || i == 2024 || i == 2030 || i == 2034 || i == 2037 || i == 2026 || i == 2027)) {
            switch (i) {
                case 2000:
                case 2001:
                case 2002:
                    break;
                default:
                    switch (i) {
                        case 2007:
                        case 2008:
                        case 2009:
                            break;
                        default:
                            switch (i) {
                                case 2017:
                                case 2018:
                                case 2019:
                                case OppoBaseDisplayPowerController2.MSG_INIT_RUS_OBJECT:
                                case OppoOperatorManagerService.H.MSG_PERSIST:
                                case 2022:
                                    break;
                                default:
                                    if ((attrs.privateFlags & 16) == 0) {
                                        return true;
                                    }
                                    break;
                            }
                    }
            }
        }
        if (this.mContext.checkCallingOrSelfPermission("android.permission.INTERNAL_SYSTEM_WINDOW") != 0) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void readLidState() {
        this.mDefaultDisplayPolicy.setLidState(this.mWindowManagerFuncs.getLidState());
    }

    private void readCameraLensCoverState() {
        this.mCameraLensCoverState = this.mWindowManagerFuncs.getCameraLensCoverState();
    }

    private boolean isHidden(int accessibilityMode) {
        int lidState = this.mDefaultDisplayPolicy.getLidState();
        return accessibilityMode != 1 ? accessibilityMode == 2 && lidState == 1 : lidState == 0;
    }

    @Override // com.android.server.policy.WindowManagerPolicy
    public void adjustConfigurationLw(Configuration config, int keyboardPresence, int navigationPresence) {
        this.mHaveBuiltInKeyboard = (keyboardPresence & 1) != 0;
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

    @Override // com.android.server.policy.WindowManagerPolicy
    public int getMaxWallpaperLayer() {
        return getWindowLayerFromTypeLw(2000);
    }

    @Override // com.android.server.policy.WindowManagerPolicy
    public boolean isKeyguardHostWindow(WindowManager.LayoutParams attrs) {
        return attrs.type == 2000;
    }

    @Override // com.android.server.policy.WindowManagerPolicy
    public boolean canBeHiddenByKeyguardLw(WindowManagerPolicy.WindowState win) {
        int i;
        if (win.getAppToken() != null || (i = win.getAttrs().type) == 2000 || i == 2013 || i == 2019 || i == 2023 || getWindowLayerLw(win) >= getWindowLayerFromTypeLw(2000)) {
            return false;
        }
        return true;
    }

    private boolean shouldBeHiddenByKeyguard(WindowManagerPolicy.WindowState win, WindowManagerPolicy.WindowState imeTarget) {
        WindowManager.LayoutParams attrs = win.getAttrs();
        if (attrs.type == 2034 && !this.mWindowManagerInternal.isStackVisibleLw(3)) {
            return true;
        }
        if (win.isInputMethodWindow() && (this.mAodShowing || !this.mDefaultDisplayPolicy.isWindowManagerDrawComplete())) {
            return true;
        }
        boolean allowWhenLocked = win.isInputMethodWindow() && (imeTarget != null && imeTarget.isVisibleLw() && (imeTarget.canShowWhenLocked() || !canBeHiddenByKeyguardLw(imeTarget)));
        boolean isKeyguardShowing = this.mKeyguardDelegate.isShowing();
        if (isKeyguardShowing && isKeyguardOccluded()) {
            allowWhenLocked |= win.canShowWhenLocked() || (attrs.privateFlags & 256) != 0;
        }
        return isKeyguardShowing && !allowWhenLocked && win.getDisplayId() == 0;
    }

    /* JADX WARNING: Removed duplicated region for block: B:62:0x014b A[Catch:{ BadTokenException -> 0x02e5, RuntimeException -> 0x02db, all -> 0x02d0 }] */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x0150 A[Catch:{ BadTokenException -> 0x02e5, RuntimeException -> 0x02db, all -> 0x02d0 }] */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x015f  */
    @Override // com.android.server.policy.WindowManagerPolicy
    public WindowManagerPolicy.StartingSurface addSplashScreen(IBinder appToken, String packageName, int theme, CompatibilityInfo compatInfo, CharSequence nonLocalizedLabel, int labelRes, int icon, int logo, int windowFlags, Configuration overrideConfig, int displayId) {
        WindowManager wm;
        WindowManager windowManager;
        WindowManager windowManager2;
        WindowManager windowManager3;
        WindowManager wm2;
        CharSequence label;
        Object windowManagerLock;
        Object obj;
        int windowFlags2;
        if (!SHOW_SPLASH_SCREENS || packageName == null) {
            return null;
        }
        WindowManager wm3 = null;
        View view = null;
        try {
            Context context = this.mContext;
            if (DEBUG_SPLASH_SCREEN) {
                try {
                    Slog.d("WindowManager", "addSplashScreen " + packageName + ": nonLocalizedLabel=" + ((Object) nonLocalizedLabel) + " theme=" + Integer.toHexString(theme));
                } catch (WindowManager.BadTokenException e) {
                    e = e;
                    wm = wm3;
                } catch (RuntimeException e2) {
                    e = e2;
                    wm = wm3;
                    try {
                        Log.w("WindowManager", appToken + " failed creating starting window", e);
                        return null;
                    } catch (Throwable th) {
                        th = th;
                    }
                } catch (Throwable th2) {
                    th = th2;
                    wm = wm3;
                    Log.w("WindowManager", "view not successfully added to wm, removing view");
                    wm.removeViewImmediate(view);
                    throw th;
                }
            }
            Context displayContext = getDisplayContext(context, displayId);
            if (displayContext == null) {
                if (view != null && view.getParent() == null) {
                    Log.w("WindowManager", "view not successfully added to wm, removing view");
                    wm3.removeViewImmediate(view);
                }
                return null;
            }
            Context context2 = displayContext;
            if (!(theme == context2.getThemeResId() && labelRes == 0)) {
                try {
                    context2 = context2.createPackageContext(packageName, 4);
                    context2.setTheme(theme);
                } catch (PackageManager.NameNotFoundException e3) {
                }
            }
            if (overrideConfig != null) {
                try {
                    if (!overrideConfig.equals(Configuration.EMPTY)) {
                        if (DEBUG_SPLASH_SCREEN) {
                            Slog.d("WindowManager", "addSplashScreen: creating context based on overrideConfig" + overrideConfig + " for splash screen");
                        }
                        Context overrideContext = context2.createConfigurationContext(overrideConfig);
                        overrideContext.setTheme(theme);
                        TypedArray typedArray = overrideContext.obtainStyledAttributes(R.styleable.Window);
                        int resId = typedArray.getResourceId(1, 0);
                        boolean windowIsTranslucent = typedArray.getBoolean(5, false);
                        if (resId == 0 || !checkStartingWindowDrawable(overrideContext.getDrawable(resId), windowIsTranslucent)) {
                            wm2 = wm3;
                        } else {
                            if (DEBUG_SPLASH_SCREEN) {
                                StringBuilder sb = new StringBuilder();
                                wm2 = wm3;
                                try {
                                    sb.append("addSplashScreen: apply overrideConfig");
                                    sb.append(overrideConfig);
                                    sb.append(" to starting window resId=");
                                    sb.append(resId);
                                    Slog.d("WindowManager", sb.toString());
                                } catch (WindowManager.BadTokenException e4) {
                                    e = e4;
                                    wm = wm2;
                                    Log.w("WindowManager", appToken + " already running, starting window not displayed. " + e.getMessage());
                                    if (view == null || view.getParent() != null) {
                                        return null;
                                    }
                                    Log.w("WindowManager", "view not successfully added to wm, removing view");
                                    wm.removeViewImmediate(view);
                                    return null;
                                } catch (RuntimeException e5) {
                                    e = e5;
                                    wm = wm2;
                                    Log.w("WindowManager", appToken + " failed creating starting window", e);
                                    if (view == null || view.getParent() != null) {
                                        return null;
                                    }
                                    Log.w("WindowManager", "view not successfully added to wm, removing view");
                                    wm.removeViewImmediate(view);
                                    return null;
                                } catch (Throwable th3) {
                                    th = th3;
                                    wm = wm2;
                                    if (view != null && view.getParent() == null) {
                                        Log.w("WindowManager", "view not successfully added to wm, removing view");
                                        wm.removeViewImmediate(view);
                                    }
                                    throw th;
                                }
                            } else {
                                wm2 = wm3;
                            }
                            context2 = overrideContext;
                        }
                        typedArray.recycle();
                        PhoneWindow win = new PhoneWindow(context2);
                        win.setIsStartingWindow(true);
                        label = context2.getResources().getText(labelRes, null);
                        if (label == null) {
                            win.setTitle(label, true);
                        } else {
                            win.setTitle(nonLocalizedLabel, false);
                        }
                        win.setType(3);
                        windowManagerLock = this.mWindowManagerFuncs.getWindowManagerLock();
                        synchronized (windowManagerLock) {
                            if (displayId == 0) {
                                try {
                                    if (this.mKeyguardOccluded) {
                                        windowFlags2 = windowFlags | DumpState.DUMP_FROZEN;
                                    }
                                } catch (Throwable th4) {
                                    th = th4;
                                    obj = windowManagerLock;
                                    while (true) {
                                        try {
                                            break;
                                        } catch (Throwable th5) {
                                            th = th5;
                                        }
                                    }
                                    throw th;
                                }
                            }
                            windowFlags2 = windowFlags;
                            try {
                            } catch (Throwable th6) {
                                th = th6;
                                obj = windowManagerLock;
                                while (true) {
                                    break;
                                }
                                throw th;
                            }
                        }
                        try {
                            win.setFlags(windowFlags2 | 16 | 8 | DumpState.DUMP_INTENT_FILTER_VERIFIERS, windowFlags2 | 16 | 8 | DumpState.DUMP_INTENT_FILTER_VERIFIERS);
                            try {
                                win.setDefaultIcon(icon);
                                try {
                                    win.setDefaultLogo(logo);
                                    win.setLayout(-1, -1);
                                    WindowManager.LayoutParams params = win.getAttributes();
                                    params.token = appToken;
                                    params.packageName = packageName;
                                    params.windowAnimations = win.getWindowStyle().getResourceId(8, 0);
                                    if (packageName.contains("com.kasikorn.retail.mbanking.wap")) {
                                        win.setFlags((int) DumpState.DUMP_SERVICE_PERMISSIONS, (int) DumpState.DUMP_SERVICE_PERMISSIONS);
                                    } else {
                                        params.privateFlags |= 1;
                                    }
                                    params.privateFlags |= 16;
                                    if (!compatInfo.supportsScreen()) {
                                        params.privateFlags |= 128;
                                    }
                                    params.setTitle("Splash Screen " + packageName);
                                    handleStartingWindow(win);
                                    addSplashscreenContent(win, context2);
                                    wm = (WindowManager) context2.getSystemService("window");
                                    try {
                                        View view2 = win.getDecorView();
                                        try {
                                            handleStartingWindowAttrs(win);
                                            handleStatusbarForStartingWindow(view2);
                                            OppoFeatureCache.getOrCreate(IColorDarkModeManager.DEFAULT, new Object[0]).handleStartingWindow(this.mContext, appToken != null ? appToken.toString() : "", win, win.getDecorView());
                                            if (DEBUG_SPLASH_SCREEN) {
                                                StringBuilder sb2 = new StringBuilder();
                                                sb2.append("Adding splash screen window for ");
                                                sb2.append(packageName);
                                                sb2.append(" / ");
                                                sb2.append(appToken);
                                                sb2.append(": ");
                                                sb2.append(view2.getParent() != null ? view2 : null);
                                                Slog.d("WindowManager", sb2.toString());
                                            }
                                            wm.addView(view2, params);
                                            SplashScreenSurface splashScreenSurface = view2.getParent() != null ? new SplashScreenSurface(view2, appToken) : null;
                                            if (view2.getParent() == null) {
                                                Log.w("WindowManager", "view not successfully added to wm, removing view");
                                                wm.removeViewImmediate(view2);
                                            }
                                            return splashScreenSurface;
                                        } catch (WindowManager.BadTokenException e6) {
                                            e = e6;
                                            view = view2;
                                            Log.w("WindowManager", appToken + " already running, starting window not displayed. " + e.getMessage());
                                            return null;
                                        } catch (RuntimeException e7) {
                                            e = e7;
                                            view = view2;
                                            Log.w("WindowManager", appToken + " failed creating starting window", e);
                                            return null;
                                        } catch (Throwable th7) {
                                            th = th7;
                                            view = view2;
                                            Log.w("WindowManager", "view not successfully added to wm, removing view");
                                            wm.removeViewImmediate(view);
                                            throw th;
                                        }
                                    } catch (WindowManager.BadTokenException e8) {
                                        e = e8;
                                        Log.w("WindowManager", appToken + " already running, starting window not displayed. " + e.getMessage());
                                        return null;
                                    } catch (RuntimeException e9) {
                                        e = e9;
                                        Log.w("WindowManager", appToken + " failed creating starting window", e);
                                        return null;
                                    }
                                } catch (WindowManager.BadTokenException e10) {
                                    e = e10;
                                    wm = wm2;
                                    Log.w("WindowManager", appToken + " already running, starting window not displayed. " + e.getMessage());
                                    return null;
                                } catch (RuntimeException e11) {
                                    e = e11;
                                    wm = wm2;
                                    Log.w("WindowManager", appToken + " failed creating starting window", e);
                                    return null;
                                } catch (Throwable th8) {
                                    th = th8;
                                    wm = wm2;
                                    Log.w("WindowManager", "view not successfully added to wm, removing view");
                                    wm.removeViewImmediate(view);
                                    throw th;
                                }
                            } catch (WindowManager.BadTokenException e12) {
                                e = e12;
                                wm = wm2;
                                Log.w("WindowManager", appToken + " already running, starting window not displayed. " + e.getMessage());
                                return null;
                            } catch (RuntimeException e13) {
                                e = e13;
                                wm = wm2;
                                Log.w("WindowManager", appToken + " failed creating starting window", e);
                                return null;
                            } catch (Throwable th9) {
                                th = th9;
                                wm = wm2;
                                Log.w("WindowManager", "view not successfully added to wm, removing view");
                                wm.removeViewImmediate(view);
                                throw th;
                            }
                        } catch (WindowManager.BadTokenException e14) {
                            e = e14;
                            wm = wm2;
                            Log.w("WindowManager", appToken + " already running, starting window not displayed. " + e.getMessage());
                            return null;
                        } catch (RuntimeException e15) {
                            e = e15;
                            wm = wm2;
                            Log.w("WindowManager", appToken + " failed creating starting window", e);
                            return null;
                        } catch (Throwable th10) {
                            th = th10;
                            wm = wm2;
                            Log.w("WindowManager", "view not successfully added to wm, removing view");
                            wm.removeViewImmediate(view);
                            throw th;
                        }
                    }
                } catch (WindowManager.BadTokenException e16) {
                    e = e16;
                    windowManager = wm3;
                    wm = windowManager;
                    Log.w("WindowManager", appToken + " already running, starting window not displayed. " + e.getMessage());
                    return null;
                } catch (RuntimeException e17) {
                    e = e17;
                    windowManager2 = wm3;
                    wm = windowManager2;
                    Log.w("WindowManager", appToken + " failed creating starting window", e);
                    return null;
                } catch (Throwable th11) {
                    th = th11;
                    windowManager3 = wm3;
                    wm = windowManager3;
                    Log.w("WindowManager", "view not successfully added to wm, removing view");
                    wm.removeViewImmediate(view);
                    throw th;
                }
            }
            wm2 = wm3;
            PhoneWindow win2 = new PhoneWindow(context2);
            win2.setIsStartingWindow(true);
            label = context2.getResources().getText(labelRes, null);
            if (label == null) {
            }
            win2.setType(3);
            windowManagerLock = this.mWindowManagerFuncs.getWindowManagerLock();
            synchronized (windowManagerLock) {
            }
        } catch (WindowManager.BadTokenException e18) {
            e = e18;
            windowManager = wm3;
            wm = windowManager;
            Log.w("WindowManager", appToken + " already running, starting window not displayed. " + e.getMessage());
            return null;
        } catch (RuntimeException e19) {
            e = e19;
            windowManager2 = wm3;
            wm = windowManager2;
            Log.w("WindowManager", appToken + " failed creating starting window", e);
            return null;
        } catch (Throwable th12) {
            th = th12;
            windowManager3 = wm3;
            wm = windowManager3;
            Log.w("WindowManager", "view not successfully added to wm, removing view");
            wm.removeViewImmediate(view);
            throw th;
        }
    }

    private void addSplashscreenContent(PhoneWindow win, Context ctx) {
        Drawable drawable;
        TypedArray a = ctx.obtainStyledAttributes(R.styleable.Window);
        int resId = a.getResourceId(48, 0);
        a.recycle();
        if (resId != 0 && (drawable = ctx.getDrawable(resId)) != null) {
            View v = new View(ctx);
            v.setBackground(drawable);
            win.setContentView(v);
        }
    }

    private Context getDisplayContext(Context context, int displayId) {
        if (displayId == 0) {
            return context;
        }
        Display targetDisplay = this.mDisplayManager.getDisplay(displayId);
        if (targetDisplay == null) {
            return null;
        }
        return context.createDisplayContext(targetDisplay);
    }

    @Override // com.android.server.policy.WindowManagerPolicy
    public Animation createHiddenByKeyguardExit(boolean onWallpaper, boolean goingToNotificationShade) {
        int i;
        if (goingToNotificationShade) {
            return AnimationUtils.loadAnimation(this.mContext, 17432819);
        }
        Context context = this.mContext;
        if (onWallpaper) {
            i = 17432820;
        } else {
            i = 17432818;
        }
        AnimationSet set = (AnimationSet) AnimationUtils.loadAnimation(context, i);
        List<Animation> animations = set.getAnimations();
        for (int i2 = animations.size() - 1; i2 >= 0; i2--) {
            animations.get(i2).setInterpolator(this.mLogDecelerateInterpolator);
        }
        return set;
    }

    @Override // com.android.server.policy.WindowManagerPolicy
    public Animation createKeyguardWallpaperExit(boolean goingToNotificationShade) {
        if (goingToNotificationShade) {
            return null;
        }
        return AnimationUtils.loadAnimation(this.mContext, 17432823);
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

    /* access modifiers changed from: package-private */
    public TelecomManager getTelecommService() {
        return (TelecomManager) this.mContext.getSystemService("telecom");
    }

    static IAudioService getAudioService() {
        IAudioService audioService = IAudioService.Stub.asInterface(ServiceManager.checkService("audio"));
        if (audioService == null) {
            Log.w("WindowManager", "Unable to find IAudioService interface.");
        }
        return audioService;
    }

    /* access modifiers changed from: package-private */
    public boolean keyguardOn() {
        return isKeyguardShowingAndNotOccluded() || inKeyguardRestrictedKeyInputMode();
    }

    @Override // com.android.server.policy.WindowManagerPolicy
    public long interceptKeyBeforeDispatching(WindowManagerPolicy.WindowState win, KeyEvent event, int policyFlags) {
        long result = interceptKeyBeforeDispatchingInner(win, event, policyFlags);
        int eventDisplayId = event.getDisplayId();
        if (result != 0 || this.mPerDisplayFocusEnabled || eventDisplayId == -1 || eventDisplayId == this.mTopFocusedDisplayId) {
            return result;
        }
        long eventDownTime = event.getDownTime();
        if (this.mMovingDisplayToTopKeyTime < eventDownTime) {
            this.mMovingDisplayToTopKeyTime = eventDownTime;
            this.mMovingDisplayToTopKeyTriggered = true;
            Handler handler = this.mHandler;
            handler.sendMessage(handler.obtainMessage(28, eventDisplayId, 0));
            return MOVING_DISPLAY_TO_TOP_DURATION_MILLIS;
        } else if (this.mMovingDisplayToTopKeyTriggered) {
            return MOVING_DISPLAY_TO_TOP_DURATION_MILLIS;
        } else {
            Slog.w("WindowManager", "Dropping key targeting non-focused display #" + eventDisplayId + " keyCode=" + KeyEvent.keyCodeToString(event.getKeyCode()));
            return -1;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:7:0x0035, code lost:
        if (com.mediatek.server.wm.WindowManagerDebugger.WMS_DEBUG_ENG != false) goto L_0x0037;
     */
    /* JADX WARNING: Removed duplicated region for block: B:249:0x033d A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:250:0x033e  */
    private long interceptKeyBeforeDispatchingInner(WindowManagerPolicy.WindowState win, KeyEvent event, int policyFlags) {
        boolean down;
        boolean actionTriggered;
        int i;
        String category;
        Intent shortcutIntent;
        InputDevice d;
        int type;
        IStatusBarService service;
        long j;
        boolean keyguardOn = keyguardOn();
        int keyCode = event.getKeyCode();
        int repeatCount = event.getRepeatCount();
        int metaState = event.getMetaState();
        int flags = event.getFlags();
        boolean down2 = event.getAction() == 0;
        boolean canceled = event.isCanceled();
        int displayId = event.getDisplayId();
        if (!DEBUG_INPUT) {
            WindowManagerDebugger windowManagerDebugger = this.mWindowManagerDebugger;
        }
        Log.d("WindowManager", "interceptKeyTi keyCode=" + keyCode + " down=" + down2 + " repeatCount=" + repeatCount + " keyguardOn=" + keyguardOn + " canceled=" + canceled + " metaState:" + metaState);
        boolean z = event.getAction() == 0;
        boolean keyup = event.getAction() == 1;
        if (keyCode == 4 && keyup) {
            if (this.mHypnusManager == null) {
                this.mHypnusManager = HypnusManager.getHypnusManager();
            }
            HypnusManager hypnusManager = this.mHypnusManager;
            if (hypnusManager != null) {
                hypnusManager.hypnusSetAction(12, 300);
            }
        }
        if (this.mScreenshotChordEnabled && (flags & 1024) == 0) {
            if (this.mScreenshotChordVolumeDownKeyTriggered && !this.mScreenshotChordPowerKeyTriggered) {
                long now = SystemClock.uptimeMillis();
                long timeoutTime = this.mScreenshotChordVolumeDownKeyTime + SCREENSHOT_CHORD_DEBOUNCE_DELAY_MILLIS;
                if (now < timeoutTime) {
                    return timeoutTime - now;
                }
            }
            if (keyCode == 25 && this.mScreenshotChordVolumeDownKeyConsumed) {
                if (!down2) {
                    this.mScreenshotChordVolumeDownKeyConsumed = false;
                }
                return -1;
            }
        }
        if (!this.mAccessibilityShortcutController.isAccessibilityShortcutAvailable(false) || (flags & 1024) != 0) {
            down = down2;
        } else {
            if (this.mA11yShortcutChordVolumeUpKeyTriggered ^ this.mScreenshotChordVolumeDownKeyTriggered) {
                long now2 = SystemClock.uptimeMillis();
                if (this.mScreenshotChordVolumeDownKeyTriggered) {
                    down = down2;
                    j = this.mScreenshotChordVolumeDownKeyTime;
                } else {
                    down = down2;
                    j = this.mA11yShortcutChordVolumeUpKeyTime;
                }
                long timeoutTime2 = j + SCREENSHOT_CHORD_DEBOUNCE_DELAY_MILLIS;
                if (now2 < timeoutTime2) {
                    return timeoutTime2 - now2;
                }
            } else {
                down = down2;
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
        if (this.mRingerToggleChord != 0 && (flags & 1024) == 0) {
            if (this.mA11yShortcutChordVolumeUpKeyTriggered && !this.mScreenshotChordPowerKeyTriggered) {
                long now3 = SystemClock.uptimeMillis();
                long timeoutTime3 = this.mA11yShortcutChordVolumeUpKeyTime + SCREENSHOT_CHORD_DEBOUNCE_DELAY_MILLIS;
                if (now3 < timeoutTime3) {
                    return timeoutTime3 - now3;
                }
            }
            if (keyCode == 24 && this.mA11yShortcutChordVolumeUpKeyConsumed) {
                if (!down) {
                    this.mA11yShortcutChordVolumeUpKeyConsumed = false;
                }
                return -1;
            }
        }
        if (this.mPendingMetaAction && !KeyEvent.isMetaKey(keyCode)) {
            this.mPendingMetaAction = false;
        }
        if (this.mPendingCapsLockToggle && !KeyEvent.isMetaKey(keyCode) && !KeyEvent.isAltKey(keyCode)) {
            this.mPendingCapsLockToggle = false;
        }
        if (keyCode == 3) {
            DisplayHomeButtonHandler handler = this.mDisplayHomeButtonHandlers.get(displayId);
            if (handler == null) {
                handler = new DisplayHomeButtonHandler(displayId);
                this.mDisplayHomeButtonHandlers.put(displayId, handler);
            }
            return (long) handler.handleHomeButton(win, event);
        }
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
            if (!keyguardOn) {
                if (down && repeatCount == 0) {
                    preloadRecentApps();
                } else if (!down && !isMenuLongPressed()) {
                    toggleRecentApps();
                }
            }
            return -1;
        } else if (keyCode != 42 || !event.isMetaPressed()) {
            if (keyCode != 47 || !event.isMetaPressed() || !event.isCtrlPressed()) {
                if (keyCode != 76 || !event.isMetaPressed()) {
                    if (keyCode == 219) {
                        Slog.wtf("WindowManager", "KEYCODE_ASSIST should be handled in interceptKeyBeforeQueueing");
                        return -1;
                    } else if (keyCode == 231) {
                        Slog.wtf("WindowManager", "KEYCODE_VOICE_ASSIST should be handled in interceptKeyBeforeQueueing");
                        return -1;
                    } else if (keyCode == 120) {
                        if (down && repeatCount == 0) {
                            this.mScreenshotRunnable.setScreenshotType(1);
                            this.mHandler.post(this.mScreenshotRunnable);
                        }
                        return -1;
                    } else {
                        if (keyCode != 221) {
                            if (keyCode != 220) {
                                if (keyCode == 24 || keyCode == 25 || keyCode == 164) {
                                    if (!this.mUseTvRouting) {
                                        if (!this.mHandleVolumeKeysInWM) {
                                            if (this.mDefaultDisplayPolicy.isPersistentVrModeEnabled() && (d = event.getDevice()) != null && !d.isExternal()) {
                                                return -1;
                                            }
                                        }
                                    }
                                    dispatchDirectAudioEvent(event);
                                    return -1;
                                } else if (keyCode == 61 && event.isMetaPressed()) {
                                    return 0;
                                } else {
                                    if (this.mHasFeatureLeanback && interceptBugreportGestureTv(keyCode, down)) {
                                        return -1;
                                    }
                                    if (keyCode == 284) {
                                        if (!down) {
                                            this.mHandler.removeMessages(22);
                                            Message msg = this.mHandler.obtainMessage(22);
                                            msg.setAsynchronous(true);
                                            msg.sendToTarget();
                                        }
                                        return -1;
                                    }
                                }
                            }
                        }
                        if (down) {
                            int direction = keyCode == 221 ? 1 : -1;
                            if (Settings.System.getIntForUser(this.mContext.getContentResolver(), "screen_brightness_mode", 0, -3) != 0) {
                                Settings.System.putIntForUser(this.mContext.getContentResolver(), "screen_brightness_mode", 0, -3);
                            }
                            int min = this.mPowerManager.getMinimumScreenBrightnessSetting();
                            int max = this.mPowerManager.getMaximumScreenBrightnessSetting();
                            Settings.System.putIntForUser(this.mContext.getContentResolver(), "screen_brightness", Math.max(min, Math.min(max, Settings.System.getIntForUser(this.mContext.getContentResolver(), "screen_brightness", this.mPowerManager.getDefaultScreenBrightnessSetting(), -3) + (((((max - min) + 10) - 1) / 10) * direction))), -3);
                            startActivityAsUser(new Intent("com.android.intent.action.SHOW_BRIGHTNESS_DIALOG"), UserHandle.CURRENT_OR_SELF);
                        }
                        return -1;
                    }
                } else if (down && repeatCount == 0 && !isKeyguardLocked()) {
                    toggleKeyboardShortcutsMenu(event.getDeviceId());
                }
            } else if (down && repeatCount == 0) {
                if (event.isShiftPressed()) {
                    type = 2;
                } else {
                    type = 1;
                }
                this.mScreenshotRunnable.setScreenshotType(type);
                this.mHandler.post(this.mScreenshotRunnable);
                return -1;
            }
        } else if (down && (service = getStatusBarService()) != null) {
            try {
                service.expandNotificationsPanel();
            } catch (RemoteException e) {
            }
        }
        boolean actionTriggered2 = false;
        if (KeyEvent.isModifierKey(keyCode)) {
            if (!this.mPendingCapsLockToggle) {
                this.mInitialMetaState = this.mMetaState;
                this.mPendingCapsLockToggle = true;
            } else if (event.getAction() == 1) {
                int i2 = this.mMetaState;
                int altOnMask = i2 & 50;
                int metaOnMask = i2 & 458752;
                if (!(metaOnMask == 0 || altOnMask == 0 || this.mInitialMetaState != (i2 ^ (altOnMask | metaOnMask)))) {
                    this.mInputManagerInternal.toggleCapsLock(event.getDeviceId());
                    actionTriggered2 = true;
                }
                this.mPendingCapsLockToggle = false;
                actionTriggered = actionTriggered2;
                this.mMetaState = metaState;
                if (!actionTriggered) {
                    return -1;
                }
                if (KeyEvent.isMetaKey(keyCode)) {
                    if (down) {
                        this.mPendingMetaAction = true;
                    } else if (this.mPendingMetaAction) {
                        launchAssistAction("android.intent.extra.ASSIST_INPUT_HINT_KEYBOARD", event.getDeviceId(), OppoBasePhoneWindowManager.AssistManagerLaunchMode.DEFAULT);
                    }
                    return -1;
                }
                if (this.mSearchKeyShortcutPending) {
                    KeyCharacterMap kcm = event.getKeyCharacterMap();
                    if (kcm.isPrintingKey(keyCode)) {
                        this.mConsumeSearchKeyUp = true;
                        this.mSearchKeyShortcutPending = false;
                        if (down && repeatCount == 0 && !keyguardOn) {
                            Intent shortcutIntent2 = this.mShortcutManager.getIntent(kcm, keyCode, metaState);
                            if (shortcutIntent2 != null) {
                                shortcutIntent2.addFlags(268435456);
                                try {
                                    startActivityAsUser(shortcutIntent2, UserHandle.CURRENT);
                                    dismissKeyboardShortcutsMenu();
                                } catch (ActivityNotFoundException ex) {
                                    Slog.w("WindowManager", "Dropping shortcut key combination because the activity to which it is registered was not found: SEARCH+" + KeyEvent.keyCodeToString(keyCode), ex);
                                }
                            } else {
                                Slog.i("WindowManager", "Dropping unregistered shortcut key combination: SEARCH+" + KeyEvent.keyCodeToString(keyCode));
                            }
                        }
                        return -1;
                    }
                }
                if (down && repeatCount == 0 && !keyguardOn && (metaState & 65536) != 0) {
                    KeyCharacterMap kcm2 = event.getKeyCharacterMap();
                    if (kcm2.isPrintingKey(keyCode) && (shortcutIntent = this.mShortcutManager.getIntent(kcm2, keyCode, -458753 & metaState)) != null) {
                        shortcutIntent.addFlags(268435456);
                        try {
                            startActivityAsUser(shortcutIntent, UserHandle.CURRENT);
                            dismissKeyboardShortcutsMenu();
                        } catch (ActivityNotFoundException ex2) {
                            Slog.w("WindowManager", "Dropping shortcut key combination because the activity to which it is registered was not found: META+" + KeyEvent.keyCodeToString(keyCode), ex2);
                        }
                        return -1;
                    }
                }
                if (!down || repeatCount != 0 || keyguardOn || (category = sApplicationLaunchKeyCategories.get(keyCode)) == null) {
                    if (down && repeatCount == 0 && keyCode == 61) {
                        if (this.mRecentAppsHeldModifiers == 0 && !keyguardOn && isUserSetupComplete()) {
                            int shiftlessModifiers = event.getModifiers() & -194;
                            if (KeyEvent.metaStateHasModifiers(shiftlessModifiers, 2)) {
                                this.mRecentAppsHeldModifiers = shiftlessModifiers;
                                showRecentApps(true);
                                return -1;
                            }
                        }
                    } else if (!down && (i = this.mRecentAppsHeldModifiers) != 0 && (i & metaState) == 0) {
                        this.mRecentAppsHeldModifiers = 0;
                        hideRecentApps(true, false);
                    }
                    boolean isCtrlOrMetaSpace = keyCode == 62 && (487424 & metaState) != 0;
                    if (down && repeatCount == 0 && (keyCode == 204 || isCtrlOrMetaSpace)) {
                        this.mWindowManagerFuncs.switchKeyboardLayout(event.getDeviceId(), (metaState & HdmiCecKeycode.UI_SOUND_PRESENTATION_TREBLE_STEP_PLUS) != 0 ? -1 : 1);
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
                            IShortcutService shortcutService = this.mShortcutKeyServices.get(shortcutCode);
                            if (shortcutService != null) {
                                try {
                                    if (isUserSetupComplete()) {
                                        shortcutService.notifyShortcutKeyPressed(shortcutCode);
                                    }
                                } catch (RemoteException e2) {
                                    this.mShortcutKeyServices.delete(shortcutCode);
                                }
                                return -1;
                            }
                        }
                        if ((metaState & 65536) != 0) {
                            return -1;
                        }
                        return 0;
                    }
                } else {
                    Intent intent = Intent.makeMainSelectorActivity("android.intent.action.MAIN", category);
                    intent.setFlags(268435456);
                    try {
                        startActivityAsUser(intent, UserHandle.CURRENT);
                        dismissKeyboardShortcutsMenu();
                    } catch (ActivityNotFoundException ex3) {
                        Slog.w("WindowManager", "Dropping application launch key because the activity to which it is registered was not found: keyCode=" + keyCode + ", category=" + category, ex3);
                    }
                    return -1;
                }
            }
        }
        actionTriggered = false;
        this.mMetaState = metaState;
        if (!actionTriggered) {
        }
    }

    private boolean interceptBugreportGestureTv(int keyCode, boolean down) {
        if (keyCode == 23) {
            this.mBugreportTvKey1Pressed = down;
        } else if (keyCode == 4) {
            this.mBugreportTvKey2Pressed = down;
        }
        if (!this.mBugreportTvKey1Pressed || !this.mBugreportTvKey2Pressed) {
            if (this.mBugreportTvScheduled) {
                this.mHandler.removeMessages(18);
                this.mBugreportTvScheduled = false;
            }
        } else if (!this.mBugreportTvScheduled) {
            this.mBugreportTvScheduled = true;
            Message msg = Message.obtain(this.mHandler, 18);
            msg.setAsynchronous(true);
            this.mHandler.sendMessageDelayed(msg, 1000);
        }
        return this.mBugreportTvScheduled;
    }

    private boolean interceptAccessibilityGestureTv(int keyCode, boolean down) {
        if (keyCode == 4) {
            this.mAccessibilityTvKey1Pressed = down;
        } else if (keyCode == 20) {
            this.mAccessibilityTvKey2Pressed = down;
        }
        if (!this.mAccessibilityTvKey1Pressed || !this.mAccessibilityTvKey2Pressed) {
            if (this.mAccessibilityTvScheduled) {
                this.mHandler.removeMessages(19);
                this.mAccessibilityTvScheduled = false;
            }
        } else if (!this.mAccessibilityTvScheduled) {
            this.mAccessibilityTvScheduled = true;
            Message msg = Message.obtain(this.mHandler, 19);
            msg.setAsynchronous(true);
            this.mHandler.sendMessageDelayed(msg, getAccessibilityShortcutTimeout());
        }
        return this.mAccessibilityTvScheduled;
    }

    /* access modifiers changed from: private */
    public void requestFullBugreport() {
        if ("1".equals(SystemProperties.get("ro.debuggable")) || Settings.Global.getInt(this.mContext.getContentResolver(), "development_settings_enabled", 0) == 1) {
            try {
                ActivityManager.getService().requestBugReport(0);
            } catch (RemoteException e) {
                Slog.e("WindowManager", "Error taking bugreport", e);
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:31:0x0133  */
    @Override // com.android.server.policy.WindowManagerPolicy
    public KeyEvent dispatchUnhandledKey(WindowManagerPolicy.WindowState win, KeyEvent event, int policyFlags) {
        KeyEvent fallbackEvent;
        KeyEvent fallbackEvent2;
        KeyCharacterMap.FallbackAction fallbackAction;
        if (DEBUG_INPUT) {
            Slog.d("WindowManager", "Unhandled key: win=" + win + ", action=" + event.getAction() + ", flags=" + event.getFlags() + ", keyCode=" + event.getKeyCode() + ", scanCode=" + event.getScanCode() + ", metaState=" + event.getMetaState() + ", repeatCount=" + event.getRepeatCount() + ", policyFlags=" + policyFlags);
        }
        if ((event.getFlags() & 1024) == 0) {
            KeyCharacterMap kcm = event.getKeyCharacterMap();
            int keyCode = event.getKeyCode();
            int metaState = event.getMetaState();
            boolean initialDown = event.getAction() == 0 && event.getRepeatCount() == 0;
            if (initialDown) {
                fallbackAction = kcm.getFallbackAction(keyCode, metaState);
            } else {
                fallbackAction = this.mFallbackActions.get(keyCode);
            }
            if (fallbackAction != null) {
                if (DEBUG_INPUT) {
                    Slog.d("WindowManager", "Fallback: keyCode=" + fallbackAction.keyCode + " metaState=" + Integer.toHexString(fallbackAction.metaState));
                }
                fallbackEvent = KeyEvent.obtain(event.getDownTime(), event.getEventTime(), event.getAction(), fallbackAction.keyCode, event.getRepeatCount(), fallbackAction.metaState, event.getDeviceId(), event.getScanCode(), event.getFlags() | 1024, event.getSource(), event.getDisplayId(), null);
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
                if (DEBUG_INPUT) {
                    if (fallbackEvent == null) {
                        Slog.d("WindowManager", "No fallback.");
                    } else {
                        Slog.d("WindowManager", "Performing fallback: " + fallbackEvent);
                    }
                }
                return fallbackEvent;
            }
            fallbackEvent2 = null;
        } else {
            fallbackEvent2 = null;
        }
        fallbackEvent = fallbackEvent2;
        if (DEBUG_INPUT) {
        }
        return fallbackEvent;
    }

    private boolean interceptFallback(WindowManagerPolicy.WindowState win, KeyEvent fallbackEvent, int policyFlags) {
        if ((interceptKeyBeforeQueueing(fallbackEvent, policyFlags) & 1) == 0 || interceptKeyBeforeDispatching(win, fallbackEvent, policyFlags) != 0) {
            return false;
        }
        return true;
    }

    @Override // com.android.server.policy.WindowManagerPolicy
    public void setTopFocusedDisplay(int displayId) {
        this.mTopFocusedDisplayId = displayId;
    }

    @Override // com.android.server.policy.WindowManagerPolicy
    public void registerDisplayFoldListener(IDisplayFoldListener listener) {
        DisplayFoldController displayFoldController = this.mDisplayFoldController;
        if (displayFoldController != null) {
            displayFoldController.registerDisplayFoldListener(listener);
        }
    }

    @Override // com.android.server.policy.WindowManagerPolicy
    public void unregisterDisplayFoldListener(IDisplayFoldListener listener) {
        DisplayFoldController displayFoldController = this.mDisplayFoldController;
        if (displayFoldController != null) {
            displayFoldController.unregisterDisplayFoldListener(listener);
        }
    }

    @Override // com.android.server.policy.WindowManagerPolicy
    public void setOverrideFoldedArea(Rect area) {
        DisplayFoldController displayFoldController = this.mDisplayFoldController;
        if (displayFoldController != null) {
            displayFoldController.setOverrideFoldedArea(area);
        }
    }

    @Override // com.android.server.policy.WindowManagerPolicy
    public Rect getFoldedArea() {
        DisplayFoldController displayFoldController = this.mDisplayFoldController;
        if (displayFoldController != null) {
            return displayFoldController.getFoldedArea();
        }
        return new Rect();
    }

    @Override // com.android.server.policy.WindowManagerPolicy
    public void onDefaultDisplayFocusChangedLw(WindowManagerPolicy.WindowState newFocus) {
        DisplayFoldController displayFoldController = this.mDisplayFoldController;
        if (displayFoldController != null) {
            displayFoldController.onDefaultDisplayFocusChanged(newFocus != null ? newFocus.getOwningPackage() : null);
        }
    }

    @Override // com.android.server.policy.WindowManagerPolicy
    public void registerShortcutKey(long shortcutCode, IShortcutService shortcutService) throws RemoteException {
        synchronized (this.mLock) {
            IShortcutService service = this.mShortcutKeyServices.get(shortcutCode);
            if (service != null) {
                if (service.asBinder().pingBinder()) {
                    throw new RemoteException("Key already exists.");
                }
            }
            this.mShortcutKeyServices.put(shortcutCode, shortcutService);
        }
    }

    @Override // com.android.server.policy.WindowManagerPolicy
    public void onKeyguardOccludedChangedLw(boolean occluded) {
        KeyguardServiceDelegate keyguardServiceDelegate = this.mKeyguardDelegate;
        if (keyguardServiceDelegate == null || !keyguardServiceDelegate.isShowing()) {
            if (this.mKeyguardOccludedChanged) {
                this.mKeyguardOccludedChanged = false;
            }
            setKeyguardOccludedLw(occluded, false);
            return;
        }
        this.mPendingKeyguardOccluded = occluded;
        this.mKeyguardOccludedChanged = true;
    }

    /* access modifiers changed from: private */
    public int handleStartTransitionForKeyguardLw(int transit, long duration) {
        if (this.mKeyguardOccludedChanged) {
            if (DEBUG_KEYGUARD) {
                Slog.d("WindowManager", "transition/occluded changed occluded=" + this.mPendingKeyguardOccluded);
            }
            this.mKeyguardOccludedChanged = false;
            if (setKeyguardOccludedLw(this.mPendingKeyguardOccluded, false)) {
                return 5;
            }
        }
        if (AppTransition.isKeyguardGoingAwayTransit(transit)) {
            if (DEBUG_KEYGUARD) {
                Slog.d("WindowManager", "Starting keyguard exit animation");
            }
            startKeyguardExitAnimation(SystemClock.uptimeMillis(), duration);
        }
        return 0;
    }

    /* access modifiers changed from: private */
    public void launchAssistLongPressAction() {
        performHapticFeedback(0, false, "Assist - Long Press");
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
            Slog.w("WindowManager", "No activity to handle assist long press action.", e);
        }
    }

    /* access modifiers changed from: package-private */
    public void launchAssistAction(String hint, int deviceId, OppoBasePhoneWindowManager.AssistManagerLaunchMode launchMode) {
        sendCloseSystemWindows(SYSTEM_DIALOG_REASON_ASSIST);
        if (isUserSetupComplete()) {
            Bundle args = null;
            if (deviceId > Integer.MIN_VALUE) {
                args = new Bundle();
                args.putInt("android.intent.extra.ASSIST_INPUT_DEVICE_ID", deviceId);
            }
            if ((this.mContext.getResources().getConfiguration().uiMode & 15) == 4) {
                ((SearchManager) this.mContext.getSystemService("search")).launchLegacyAssist(hint, UserHandle.myUserId(), args);
                return;
            }
            if (hint != null) {
                if (args == null) {
                    args = new Bundle();
                }
                args.putBoolean(hint, true);
            }
            oppoHandleAssistLaunchMode(launchMode, args);
            StatusBarManagerInternal statusbar = getStatusBarManagerInternal();
            if (statusbar != null) {
                statusbar.startAssist(args);
            }
        }
    }

    private void launchVoiceAssist(boolean allowDuringSetup) {
        boolean keyguardActive;
        KeyguardServiceDelegate keyguardServiceDelegate = this.mKeyguardDelegate;
        if (keyguardServiceDelegate == null) {
            keyguardActive = false;
        } else {
            keyguardActive = keyguardServiceDelegate.isShowing();
        }
        if (!keyguardActive) {
            startActivityAsUser(new Intent("android.intent.action.VOICE_ASSIST"), null, UserHandle.CURRENT_OR_SELF, allowDuringSetup);
        }
    }

    private void startActivityAsUser(Intent intent, UserHandle handle) {
        startActivityAsUser(intent, null, handle);
    }

    private void startActivityAsUser(Intent intent, Bundle bundle, UserHandle handle) {
        startActivityAsUser(intent, bundle, handle, false);
    }

    private void startActivityAsUser(Intent intent, Bundle bundle, UserHandle handle, boolean allowDuringSetup) {
        if (allowDuringSetup || isUserSetupComplete()) {
            this.mContext.startActivityAsUser(intent, bundle, handle);
            return;
        }
        Slog.i("WindowManager", "Not starting activity because user setup is in progress: " + intent);
    }

    private SearchManager getSearchManager() {
        if (this.mSearchManager == null) {
            this.mSearchManager = (SearchManager) this.mContext.getSystemService("search");
        }
        return this.mSearchManager;
    }

    /* access modifiers changed from: private */
    public void preloadRecentApps() {
        this.mPreloadedRecentApps = true;
        StatusBarManagerInternal statusbar = getStatusBarManagerInternal();
        if (statusbar != null) {
            statusbar.preloadRecentApps();
        }
    }

    /* access modifiers changed from: protected */
    public void cancelPreloadRecentApps() {
        if (this.mPreloadedRecentApps) {
            this.mPreloadedRecentApps = false;
            StatusBarManagerInternal statusbar = getStatusBarManagerInternal();
            if (statusbar != null) {
                statusbar.cancelPreloadRecentApps();
            }
        }
    }

    /* access modifiers changed from: private */
    public void toggleRecentApps() {
        this.mPreloadedRecentApps = false;
        StatusBarManagerInternal statusbar = getStatusBarManagerInternal();
        if (statusbar != null) {
            statusbar.toggleRecentApps();
        }
    }

    @Override // com.android.server.policy.WindowManagerPolicy
    public void showRecentApps() {
        this.mHandler.removeMessages(9);
        this.mHandler.obtainMessage(9).sendToTarget();
    }

    /* access modifiers changed from: private */
    public void showRecentApps(boolean triggeredFromAltTab) {
        this.mPreloadedRecentApps = false;
        StatusBarManagerInternal statusbar = getStatusBarManagerInternal();
        if (statusbar != null) {
            statusbar.showRecentApps(triggeredFromAltTab);
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

    /* access modifiers changed from: package-private */
    public void launchHomeFromHotKey(int displayId) {
        launchHomeFromHotKey(displayId, true, true);
    }

    /* access modifiers changed from: package-private */
    public void launchHomeFromHotKey(final int displayId, final boolean awakenFromDreams, boolean respectKeyguard) {
        if (respectKeyguard) {
            if (!isKeyguardShowingAndNotOccluded()) {
                if (!this.mKeyguardOccluded && this.mKeyguardDelegate.isInputRestricted()) {
                    this.mKeyguardDelegate.verifyUnlock(new WindowManagerPolicy.OnKeyguardExitResult() {
                        /* class com.android.server.policy.PhoneWindowManager.AnonymousClass9 */

                        @Override // com.android.server.policy.WindowManagerPolicy.OnKeyguardExitResult
                        public void onKeyguardExitResult(boolean success) {
                            if (success) {
                                PhoneWindowManager.this.startDockOrHome(displayId, true, awakenFromDreams);
                            }
                        }
                    });
                    return;
                }
            } else {
                return;
            }
        }
        if (this.mRecentsVisible) {
            try {
                ActivityManager.getService().stopAppSwitches();
            } catch (RemoteException e) {
            }
            if (awakenFromDreams) {
                awakenDreams();
            }
            hideRecentApps(false, true);
            return;
        }
        startDockOrHome(displayId, true, awakenFromDreams);
    }

    @Override // com.android.server.policy.WindowManagerPolicy
    public void setRecentsVisibilityLw(boolean visible) {
        this.mRecentsVisible = visible;
    }

    @Override // com.android.server.policy.WindowManagerPolicy
    public void setPipVisibilityLw(boolean visible) {
        this.mPictureInPictureVisible = visible;
    }

    @Override // com.android.server.policy.WindowManagerPolicy
    public void setNavBarVirtualKeyHapticFeedbackEnabledLw(boolean enabled) {
        this.mNavBarVirtualKeyHapticFeedbackEnabled = enabled;
    }

    @Override // com.android.server.policy.WindowManagerPolicy
    public void applyKeyguardPolicyLw(WindowManagerPolicy.WindowState win, WindowManagerPolicy.WindowState imeTarget) {
        if (!canBeHiddenByKeyguardLw(win)) {
            return;
        }
        if (shouldBeHiddenByKeyguard(win, imeTarget)) {
            win.hideLw(false);
        } else {
            win.showLw(false);
        }
    }

    @Override // com.android.server.policy.WindowManagerPolicy
    public void setKeyguardCandidateLw(WindowManagerPolicy.WindowState win) {
        this.mKeyguardCandidate = win;
        setKeyguardOccludedLw(this.mKeyguardOccluded, true);
    }

    private boolean setKeyguardOccludedLw(boolean isOccluded, boolean force) {
        if (DEBUG_KEYGUARD) {
            Slog.d("WindowManager", "setKeyguardOccluded occluded=" + isOccluded);
        }
        boolean wasOccluded = this.mKeyguardOccluded;
        boolean showing = this.mKeyguardDelegate.isShowing();
        boolean changed = wasOccluded != isOccluded || force;
        if (!isOccluded && changed && showing) {
            this.mKeyguardOccluded = false;
            this.mKeyguardDelegate.setOccluded(false, true);
            WindowManagerPolicy.WindowState windowState = this.mKeyguardCandidate;
            if (windowState != null) {
                windowState.getAttrs().privateFlags |= 1024;
                if (!this.mKeyguardDelegate.hasLockscreenWallpaper()) {
                    this.mKeyguardCandidate.getAttrs().flags |= DumpState.DUMP_DEXOPT;
                }
            }
            return true;
        } else if (isOccluded && changed && showing) {
            this.mKeyguardOccluded = true;
            this.mKeyguardDelegate.setOccluded(true, false);
            WindowManagerPolicy.WindowState windowState2 = this.mKeyguardCandidate;
            if (windowState2 != null) {
                windowState2.getAttrs().privateFlags &= -1025;
                this.mKeyguardCandidate.getAttrs().flags &= -1048577;
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

    @Override // com.android.server.policy.WindowManagerPolicy
    public void notifyLidSwitchChanged(long whenNanos, boolean lidOpen) {
        if (lidOpen != this.mDefaultDisplayPolicy.getLidState()) {
            this.mDefaultDisplayPolicy.setLidState(lidOpen ? 1 : 0);
            applyLidSwitchState();
            updateRotation(true);
            if (lidOpen) {
                wakeUp(SystemClock.uptimeMillis(), this.mAllowTheaterModeWakeFromLidSwitch, 9, "android.policy:LID");
            } else if (getLidBehavior() != 1) {
                this.mPowerManager.userActivity(SystemClock.uptimeMillis(), false);
            }
        }
    }

    @Override // com.android.server.policy.WindowManagerPolicy
    public void notifyCameraLensCoverSwitchChanged(long whenNanos, boolean lensCovered) {
        boolean keyguardActive;
        Intent intent;
        int i = this.mCameraLensCoverState;
        if (i != lensCovered) {
            if (i == 1 && !lensCovered) {
                KeyguardServiceDelegate keyguardServiceDelegate = this.mKeyguardDelegate;
                if (keyguardServiceDelegate == null) {
                    keyguardActive = false;
                } else {
                    keyguardActive = keyguardServiceDelegate.isShowing();
                }
                if (keyguardActive) {
                    intent = new Intent("android.media.action.STILL_IMAGE_CAMERA_SECURE");
                } else {
                    intent = new Intent("android.media.action.STILL_IMAGE_CAMERA");
                }
                wakeUp(whenNanos / 1000000, this.mAllowTheaterModeWakeFromCameraLens, 5, "android.policy:CAMERA_COVER");
                startActivityAsUser(intent, UserHandle.CURRENT_OR_SELF);
            }
            this.mCameraLensCoverState = lensCovered ? 1 : 0;
        }
    }

    /* access modifiers changed from: package-private */
    public void initializeHdmiState() {
        int oldMask = StrictMode.allowThreadDiskReadsMask();
        try {
            initializeHdmiStateInternal();
        } finally {
            StrictMode.setThreadPolicyMask(oldMask);
        }
    }

    /* access modifiers changed from: package-private */
    public void initializeHdmiStateInternal() {
        boolean plugged = false;
        if (new File("/sys/devices/virtual/switch/hdmi/state").exists()) {
            this.mHDMIObserver.startObserving("DEVPATH=/devices/virtual/switch/hdmi");
            FileReader reader = null;
            try {
                FileReader reader2 = new FileReader("/sys/class/switch/hdmi/state");
                char[] buf = new char[15];
                int n = reader2.read(buf);
                if (n > 1) {
                    boolean z = false;
                    if (Integer.parseInt(new String(buf, 0, n - 1)) != 0) {
                        z = true;
                    }
                    plugged = z;
                }
                try {
                    reader2.close();
                } catch (IOException e) {
                }
            } catch (IOException ex) {
                Slog.w("WindowManager", "Couldn't read hdmi state from /sys/class/switch/hdmi/state: " + ex);
                if (reader != null) {
                    reader.close();
                }
            } catch (NumberFormatException ex2) {
                Slog.w("WindowManager", "Couldn't read hdmi state from /sys/class/switch/hdmi/state: " + ex2);
                if (reader != null) {
                    reader.close();
                }
            } catch (Throwable th) {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e2) {
                    }
                }
                throw th;
            }
        } else if (ExtconUEventObserver.extconExists() && ExtconUEventObserver.namedExtconDirExists("hdmi")) {
            HdmiVideoExtconUEventObserver observer = new HdmiVideoExtconUEventObserver();
            plugged = observer.init();
            this.mHDMIObserver = observer;
        } else if (localLOGV) {
            Slog.v("WindowManager", "Not observing HDMI plug state because HDMI was not found.");
        }
        this.mDefaultDisplayPolicy.setHdmiPlugged(plugged, true);
    }

    /* JADX WARNING: Removed duplicated region for block: B:232:0x03a8  */
    /* JADX WARNING: Removed duplicated region for block: B:235:0x03ae  */
    @Override // com.android.server.policy.WindowManagerPolicy
    public int interceptKeyBeforeQueueing(KeyEvent event, int policyFlags) {
        boolean keyguardActive;
        int result;
        boolean isWakeKey;
        boolean down;
        int keyCode;
        boolean canceled;
        boolean interactive;
        boolean down2;
        boolean useHapticFeedback;
        if (!this.mSystemBooted) {
            return 0;
        }
        boolean interactive2 = (policyFlags & 536870912) != 0;
        boolean down3 = event.getAction() == 0;
        boolean canceled2 = event.isCanceled();
        int keyCode2 = event.getKeyCode();
        int displayId = event.getDisplayId();
        boolean isInjected = (policyFlags & DumpState.DUMP_SERVICE_PERMISSIONS) != 0;
        KeyguardServiceDelegate keyguardServiceDelegate = this.mKeyguardDelegate;
        if (keyguardServiceDelegate == null) {
            keyguardActive = false;
        } else if (interactive2) {
            keyguardActive = isKeyguardShowingAndNotOccluded();
        } else {
            keyguardActive = keyguardServiceDelegate.isShowing();
        }
        if (DEBUG_INPUT) {
            Log.d("WindowManager", "interceptKeyTq keycode=" + keyCode2 + " interactive=" + interactive2 + " keyguardActive=" + keyguardActive + " policyFlags=" + Integer.toHexString(policyFlags));
        }
        boolean isWakeKey2 = (policyFlags & 1) != 0 || event.isWakeKey();
        if (interactive2 || (isInjected && !isWakeKey2)) {
            int result2 = 1;
            result2 = 1;
            if (interactive2) {
                if (keyCode2 == this.mPendingWakeKey && !down3) {
                    result2 = 0;
                }
                this.mPendingWakeKey = -1;
                isWakeKey = false;
                result = result2;
            } else {
                isWakeKey = false;
                result = 1;
            }
        } else if (interactive2 || !shouldDispatchInputWhenNonInteractive(displayId, keyCode2)) {
            if (isWakeKey2 && (!down3 || !isWakeKeyWhenScreenOff(keyCode2))) {
                isWakeKey2 = false;
            }
            if (isWakeKey2 && down3) {
                this.mPendingWakeKey = keyCode2;
            }
            isWakeKey = isWakeKey2;
            result = 0;
        } else {
            this.mPendingWakeKey = -1;
            isWakeKey = isWakeKey2;
            result = 1;
        }
        if (!isValidGlobalKey(keyCode2) || !this.mGlobalKeyManager.shouldHandleGlobalKey(keyCode2, event)) {
            boolean useHapticFeedback2 = down3 && (policyFlags & 2) != 0 && (!((event.getFlags() & 64) != 0) || this.mNavBarVirtualKeyHapticFeedbackEnabled) && event.getRepeatCount() == 0;
            WindowManagerDebugger windowManagerDebugger = this.mWindowManagerDebugger;
            if (WindowManagerDebugger.WMS_DEBUG_ENG) {
                keyCode = keyCode2;
                canceled = canceled2;
                down = down3;
                interactive = interactive2;
                this.mWindowManagerDebugger.debugInterceptKeyBeforeQueueing("WindowManager", keyCode2, interactive2, keyguardActive, policyFlags, down, canceled, isWakeKey, this.mScreenshotChordVolumeDownKeyTriggered, result, useHapticFeedback2, isInjected);
            } else {
                keyCode = keyCode2;
                canceled = canceled2;
                down = down3;
                interactive = interactive2;
            }
            if (keyCode == 3) {
                down2 = down;
                if (down2 && event.getDownTime() - event.getEventTime() == ((long) keyCode)) {
                    useHapticFeedback2 = false;
                }
                if (down2) {
                    if (!interactive && event.getRepeatCount() == 0) {
                        this.mLastHomeDownTimeDuringPF = event.getDownTime();
                    }
                    if (interactive && event.getRepeatCount() == 0 && this.isTouchFingerPrintSensor) {
                        useHapticFeedback2 = true;
                    }
                    if (getFingerprintInternal() != null) {
                        getFingerprintInternal().onHomeKeyDown();
                    }
                } else {
                    if (getFingerprintInternal() != null) {
                        getFingerprintInternal().onHomeKeyUp();
                    }
                    if (this.mLastHomeDownTimeDuringPF == 0 || event.getDownTime() != this.mLastHomeDownTimeDuringPF) {
                        if (Settings.System.getInt(this.mContext.getContentResolver(), "drop_home_key_when_use_fingerprint", 0) == 1) {
                            Slog.d("WindowManager", "drop HOME_KEY Up when screen on due to finger print");
                            Settings.System.putInt(this.mContext.getContentResolver(), "drop_home_key_when_use_fingerprint", 0);
                            return 0;
                        }
                        this.mLastHomeDownTimeDuringPF = 0;
                    } else {
                        Slog.d("WindowManager", "Skip HOME_KEY Up when screen off due to finger print");
                        this.mLastHomeDownTimeDuringPF = 0;
                        return 0;
                    }
                }
                isWakeKey = false;
                useHapticFeedback = useHapticFeedback2;
            } else if (keyCode != 4) {
                if (keyCode != 5) {
                    if (keyCode != 6) {
                        if (keyCode != 79) {
                            if (keyCode == 82) {
                                down2 = down;
                                if (down2 && event.getRepeatCount() == 0) {
                                    useHapticFeedback2 = true;
                                }
                                useHapticFeedback = this.mDisplayManagerInternal.isBlockScreenOnByBiometrics() ? false : useHapticFeedback2;
                            } else if (keyCode != 130) {
                                if (keyCode != 164) {
                                    if (keyCode != 171) {
                                        if (keyCode != 187) {
                                            if (keyCode == 219) {
                                                down2 = down;
                                                boolean longPressed = event.getRepeatCount() > 0;
                                                if (down2 && longPressed) {
                                                    Message msg = this.mHandler.obtainMessage(24);
                                                    msg.setAsynchronous(true);
                                                    msg.sendToTarget();
                                                }
                                                if (!down2 && !longPressed) {
                                                    Message msg2 = this.mHandler.obtainMessage(23, event.getDeviceId(), 0, null);
                                                    Bundle bundle = new Bundle();
                                                    bundle.putSerializable("launchMode", OppoBasePhoneWindowManager.AssistManagerLaunchMode.DEFAULT);
                                                    msg2.setData(bundle);
                                                    msg2.setAsynchronous(true);
                                                    msg2.sendToTarget();
                                                }
                                                result &= -2;
                                                useHapticFeedback = useHapticFeedback2;
                                            } else if (keyCode == 231) {
                                                down2 = down;
                                                if (!down2) {
                                                    this.mBroadcastWakeLock.acquire();
                                                    Message msg3 = this.mHandler.obtainMessage(12);
                                                    msg3.setAsynchronous(true);
                                                    msg3.sendToTarget();
                                                }
                                                result &= -2;
                                                useHapticFeedback = useHapticFeedback2;
                                            } else if (keyCode != 276) {
                                                if (keyCode != 987) {
                                                    if (!(keyCode == 126 || keyCode == 127)) {
                                                        switch (keyCode) {
                                                            case 24:
                                                            case 25:
                                                                break;
                                                            case 26:
                                                                down2 = down;
                                                                EventLogTags.writeInterceptPower(KeyEvent.actionToString(event.getAction()), this.mPowerKeyHandled ? 1 : 0, this.mPowerKeyPressCounter);
                                                                cancelPendingAccessibilityShortcutAction();
                                                                result &= -2;
                                                                isWakeKey = false;
                                                                isWakeKey = false;
                                                                if (!down2) {
                                                                    interceptPowerKeyUp(event, interactive, canceled);
                                                                    break;
                                                                } else {
                                                                    if (interactive) {
                                                                        if (this.mHypnusManager == null) {
                                                                            this.mHypnusManager = HypnusManager.getHypnusManager();
                                                                        }
                                                                        HypnusManager hypnusManager = this.mHypnusManager;
                                                                        if (hypnusManager != null) {
                                                                            hypnusManager.hypnusSetAction(15, 250);
                                                                        }
                                                                    }
                                                                    interceptPowerKeyDown(event, interactive);
                                                                    break;
                                                                }
                                                            default:
                                                                switch (keyCode) {
                                                                    default:
                                                                        switch (keyCode) {
                                                                            case 222:
                                                                                break;
                                                                            case 223:
                                                                                result &= -2;
                                                                                isWakeKey = false;
                                                                                isWakeKey = false;
                                                                                if (!this.mPowerManager.isInteractive()) {
                                                                                    useHapticFeedback2 = false;
                                                                                }
                                                                                down2 = down;
                                                                                if (!down2) {
                                                                                    sleepRelease(event.getEventTime());
                                                                                    break;
                                                                                } else {
                                                                                    sleepPress();
                                                                                    break;
                                                                                }
                                                                            case UsbDescriptor.CLASSID_WIRELESS /*{ENCODED_INT: 224}*/:
                                                                                result &= -2;
                                                                                isWakeKey = true;
                                                                                useHapticFeedback = useHapticFeedback2;
                                                                                down2 = down;
                                                                                break;
                                                                            default:
                                                                                switch (keyCode) {
                                                                                    case 280:
                                                                                    case 281:
                                                                                    case 282:
                                                                                    case 283:
                                                                                        result &= -2;
                                                                                        interceptSystemNavigationKey(event);
                                                                                        useHapticFeedback = useHapticFeedback2;
                                                                                        down2 = down;
                                                                                        break;
                                                                                    default:
                                                                                        down2 = down;
                                                                                        break;
                                                                                }
                                                                        }
                                                                    case HdmiCecKeycode.CEC_KEYCODE_INITIAL_CONFIGURATION:
                                                                    case HdmiCecKeycode.CEC_KEYCODE_SELECT_BROADCAST_TYPE:
                                                                    case HdmiCecKeycode.CEC_KEYCODE_SELECT_SOUND_PRESENTATION:
                                                                    case 88:
                                                                    case 89:
                                                                    case 90:
                                                                    case 91:
                                                                        down2 = down;
                                                                        if (MediaSessionLegacyHelper.getHelper(this.mContext).isGlobalPriorityActive()) {
                                                                            result &= -2;
                                                                        }
                                                                        if ((result & 1) == 0) {
                                                                            this.mBroadcastWakeLock.acquire();
                                                                            Message msg4 = this.mHandler.obtainMessage(3, new KeyEvent(event));
                                                                            msg4.setAsynchronous(true);
                                                                            msg4.sendToTarget();
                                                                            break;
                                                                        }
                                                                        break;
                                                                }
                                                        }
                                                    }
                                                }
                                            } else {
                                                down2 = down;
                                                result &= -2;
                                                isWakeKey = false;
                                                isWakeKey = false;
                                                if (!down2) {
                                                    this.mPowerManagerInternal.setUserInactiveOverrideFromWindowManager();
                                                }
                                            }
                                        }
                                        down2 = down;
                                        useHapticFeedback = colorInterceptAppSwitchEventBeforeQueueing(event, useHapticFeedback2);
                                    } else {
                                        down2 = down;
                                        if (this.mShortPressOnWindowBehavior == 1 && this.mPictureInPictureVisible) {
                                            if (!down2) {
                                                showPictureInPictureMenu(event);
                                            }
                                            result &= -2;
                                            useHapticFeedback = useHapticFeedback2;
                                        }
                                    }
                                }
                                down2 = down;
                                if (keyCode == 25) {
                                    if (down2) {
                                        cancelPendingRingerToggleChordAction();
                                        if (interactive && !this.mScreenshotChordVolumeDownKeyTriggered && (event.getFlags() & 1024) == 0) {
                                            this.mScreenshotChordVolumeDownKeyTriggered = true;
                                            this.mScreenshotChordVolumeDownKeyTime = event.getDownTime();
                                            this.mScreenshotChordVolumeDownKeyConsumed = false;
                                            cancelPendingPowerKeyAction();
                                            interceptScreenshotChord();
                                            interceptAccessibilityShortcutChord();
                                        }
                                    } else {
                                        this.mScreenshotChordVolumeDownKeyTriggered = false;
                                        cancelPendingScreenshotChordAction();
                                        cancelPendingAccessibilityShortcutAction();
                                    }
                                } else if (keyCode == 24) {
                                    if (!down2) {
                                        this.mA11yShortcutChordVolumeUpKeyTriggered = false;
                                        cancelPendingScreenshotChordAction();
                                        cancelPendingAccessibilityShortcutAction();
                                        cancelPendingRingerToggleChordAction();
                                    } else if (interactive && !this.mA11yShortcutChordVolumeUpKeyTriggered && (event.getFlags() & 1024) == 0) {
                                        this.mA11yShortcutChordVolumeUpKeyTriggered = true;
                                        this.mA11yShortcutChordVolumeUpKeyTime = event.getDownTime();
                                        this.mA11yShortcutChordVolumeUpKeyConsumed = false;
                                        cancelPendingPowerKeyAction();
                                        cancelPendingScreenshotChordAction();
                                        cancelPendingRingerToggleChordAction();
                                        interceptAccessibilityShortcutChord();
                                        interceptRingerToggleChord();
                                    }
                                }
                                if (down2) {
                                    sendSystemKeyToStatusBarAsync(event.getKeyCode());
                                    TelecomManager telecomManager = getTelecommService();
                                    if (telecomManager == null || this.mHandleVolumeKeysInWM || !telecomManager.isRinging()) {
                                        int audioMode = 0;
                                        try {
                                            audioMode = getAudioService().getMode();
                                        } catch (Exception e) {
                                            Log.e("WindowManager", "Error getting AudioService in interceptKeyBeforeQueueing.", e);
                                        }
                                        if (((telecomManager != null && telecomManager.isInCall()) || audioMode == 3) && (result & 1) == 0) {
                                            MediaSessionLegacyHelper.getHelper(this.mContext).sendVolumeKeyEvent(event, Integer.MIN_VALUE, false);
                                        }
                                    } else {
                                        Log.i("WindowManager", "interceptKeyBeforeQueueing: VOLUME key-down while ringing: Silence ringer!");
                                        telecomManager.silenceRinger();
                                        result &= -2;
                                        useHapticFeedback = useHapticFeedback2;
                                    }
                                }
                                if (this.mUseTvRouting || this.mHandleVolumeKeysInWM) {
                                    result |= 1;
                                    useHapticFeedback = useHapticFeedback2;
                                } else if ((result & 1) == 0) {
                                    MediaSessionLegacyHelper.getHelper(this.mContext).sendVolumeKeyEvent(event, Integer.MIN_VALUE, true);
                                }
                            }
                        }
                        down2 = down;
                        if (MediaSessionLegacyHelper.getHelper(this.mContext).isGlobalPriorityActive()) {
                        }
                        if ((result & 1) == 0) {
                        }
                    } else {
                        down2 = down;
                        result &= -2;
                        if (down2) {
                            TelecomManager telecomManager2 = getTelecommService();
                            boolean hungUp = false;
                            if (telecomManager2 != null) {
                                hungUp = telecomManager2.endCall();
                            }
                            if (!interactive || hungUp) {
                                this.mEndCallKeyHandled = true;
                            } else {
                                this.mEndCallKeyHandled = false;
                                this.mHandler.postDelayed(this.mEndCallLongPress, ViewConfiguration.get(this.mContext).getDeviceGlobalActionKeyTimeout());
                            }
                        } else if (!this.mEndCallKeyHandled) {
                            this.mHandler.removeCallbacks(this.mEndCallLongPress);
                            if (!canceled && (((this.mEndcallBehavior & 1) == 0 || !goHome()) && (this.mEndcallBehavior & 2) != 0)) {
                                goToSleep(event.getEventTime(), 4, 0);
                                isWakeKey = false;
                                useHapticFeedback = useHapticFeedback2;
                            }
                        }
                    }
                    useHapticFeedback = useHapticFeedback2;
                } else {
                    down2 = down;
                    if (down2) {
                        TelecomManager telecomManager3 = getTelecommService();
                        if (telecomManager3 != null && telecomManager3.isRinging()) {
                            Log.i("WindowManager", "interceptKeyBeforeQueueing: CALL key-down while ringing: Answer the call!");
                            telecomManager3.acceptRingingCall();
                            result &= -2;
                        }
                        useHapticFeedback = useHapticFeedback2;
                    }
                }
                useHapticFeedback = useHapticFeedback2;
            } else {
                down2 = down;
                if (down2) {
                    interceptBackKeyDown();
                    if (event.getDownTime() - event.getEventTime() == ((long) keyCode)) {
                        useHapticFeedback2 = false;
                    }
                    useHapticFeedback = this.mDisplayManagerInternal.isBlockScreenOnByBiometrics() ? false : useHapticFeedback2;
                } else {
                    if (interceptBackKeyUp(event)) {
                        result &= -2;
                    }
                    useHapticFeedback = useHapticFeedback2;
                }
            }
            if (this.mHasFeatureLeanback && ((keyCode == 4 || keyCode == 20) && interceptAccessibilityGestureTv(keyCode, down2))) {
                result &= -2;
            }
            if (this.mAccessibilityShortcutController.isAccessibilityShortcutAvailable(isKeyguardLocked()) && keyCode == 54 && down2 && event.isCtrlPressed() && event.isAltPressed()) {
                Handler handler = this.mHandler;
                handler.sendMessage(handler.obtainMessage(17));
                result &= -2;
            }
            if (useHapticFeedback) {
                performHapticFeedback(1, false, "Virtual Key - Press");
            }
            if (isWakeKey && !this.mDisplayManagerInternal.isBlockScreenOnByBiometrics()) {
                wakeUp(event.getEventTime(), this.mAllowTheaterModeWakeFromKey, 6, "android.policy:KEY");
            }
            return result;
        }
        if (isWakeKey) {
            wakeUp(event.getEventTime(), this.mAllowTheaterModeWakeFromKey, 6, "android.policy:KEY");
        }
        return result;
    }

    private void interceptSystemNavigationKey(KeyEvent event) {
        if (event.getAction() != 1) {
            return;
        }
        if ((!this.mAccessibilityManager.isEnabled() || !this.mAccessibilityManager.sendFingerprintGesture(event.getKeyCode())) && this.mSystemNavigationKeysEnabled) {
            sendSystemKeyToStatusBarAsync(event.getKeyCode());
        }
    }

    /* access modifiers changed from: private */
    public void sendSystemKeyToStatusBar(int keyCode) {
        IStatusBarService statusBar = getStatusBarService();
        if (statusBar != null) {
            try {
                statusBar.handleSystemKey(keyCode);
            } catch (RemoteException e) {
            }
        }
    }

    private void sendSystemKeyToStatusBarAsync(int keyCode) {
        Message message = this.mHandler.obtainMessage(21, keyCode, 0);
        message.setAsynchronous(true);
        this.mHandler.sendMessage(message);
    }

    private static boolean isValidGlobalKey(int keyCode) {
        if (keyCode == 26 || keyCode == 223 || keyCode == 224) {
            return false;
        }
        return true;
    }

    private boolean isWakeKeyWhenScreenOff(int keyCode) {
        if (!(keyCode == 24 || keyCode == 25)) {
            if (!(keyCode == 27 || keyCode == 79 || keyCode == 130)) {
                if (keyCode != 164) {
                    if (!(keyCode == 222 || keyCode == 126 || keyCode == 127)) {
                        switch (keyCode) {
                            case HdmiCecKeycode.CEC_KEYCODE_INITIAL_CONFIGURATION:
                            case HdmiCecKeycode.CEC_KEYCODE_SELECT_BROADCAST_TYPE:
                            case HdmiCecKeycode.CEC_KEYCODE_SELECT_SOUND_PRESENTATION:
                            case 88:
                            case 89:
                            case 90:
                            case 91:
                                break;
                            default:
                                return true;
                        }
                    }
                }
            }
            return false;
        }
        return this.mDefaultDisplayPolicy.getDockMode() != 0;
    }

    @Override // com.android.server.policy.WindowManagerPolicy
    public int interceptMotionBeforeQueueingNonInteractive(int displayId, long whenNanos, int policyFlags) {
        if ((policyFlags & 1) != 0 && wakeUp(whenNanos / 1000000, this.mAllowTheaterModeWakeFromMotion, 7, "android.policy:MOTION")) {
            return 0;
        }
        if (shouldDispatchInputWhenNonInteractive(displayId, 0)) {
            return 1;
        }
        if (isTheaterModeEnabled() && (policyFlags & 1) != 0) {
            wakeUp(whenNanos / 1000000, this.mAllowTheaterModeWakeFromMotionWhenNotDreaming, 7, "android.policy:MOTION");
        }
        return 0;
    }

    private boolean shouldDispatchInputWhenNonInteractive(int displayId, int keyCode) {
        Display display;
        IDreamManager dreamManager;
        if (this.mDisplayManagerInternal.isBlockScreenOnByBiometrics()) {
            return false;
        }
        boolean isDefaultDisplay = displayId == 0 || displayId == -1;
        if (isDefaultDisplay) {
            display = this.mDefaultDisplay;
        } else {
            display = this.mDisplayManager.getDisplay(displayId);
        }
        boolean displayOff = display == null || display.getState() == 1;
        if (displayOff && !this.mHasFeatureWatch) {
            return false;
        }
        if (isKeyguardShowingAndNotOccluded() && !displayOff) {
            return true;
        }
        if ((!this.mHasFeatureWatch || !(keyCode == 4 || keyCode == 264)) && isDefaultDisplay && (dreamManager = getDreamManager()) != null) {
            try {
                if (dreamManager.isDreaming()) {
                    return true;
                }
            } catch (RemoteException e) {
                Slog.e("WindowManager", "RemoteException when checking if dreaming", e);
            }
        }
        return false;
    }

    private void dispatchDirectAudioEvent(KeyEvent event) {
        HdmiAudioSystemClient audioSystemClient;
        HdmiControlManager hdmiControlManager = getHdmiControlManager();
        if (hdmiControlManager != null && !hdmiControlManager.getSystemAudioMode() && shouldCecAudioDeviceForwardVolumeKeysSystemAudioModeOff() && (audioSystemClient = hdmiControlManager.getAudioSystemClient()) != null) {
            audioSystemClient.sendKeyEvent(event.getKeyCode(), event.getAction() == 0);
        } else if (event.getAction() == 0) {
            int keyCode = event.getKeyCode();
            String pkgName = this.mContext.getOpPackageName();
            if (keyCode == 24) {
                try {
                    getAudioService().adjustSuggestedStreamVolume(1, Integer.MIN_VALUE, 4101, pkgName, "WindowManager");
                } catch (Exception e) {
                    Log.e("WindowManager", "Error dispatching volume up in dispatchTvAudioEvent.", e);
                }
            } else if (keyCode == 25) {
                try {
                    getAudioService().adjustSuggestedStreamVolume(-1, Integer.MIN_VALUE, 4101, pkgName, "WindowManager");
                } catch (Exception e2) {
                    Log.e("WindowManager", "Error dispatching volume down in dispatchTvAudioEvent.", e2);
                }
            } else if (keyCode == 164) {
                try {
                    if (event.getRepeatCount() == 0) {
                        getAudioService().adjustSuggestedStreamVolume(101, Integer.MIN_VALUE, 4101, pkgName, "WindowManager");
                    }
                } catch (Exception e3) {
                    Log.e("WindowManager", "Error dispatching mute in dispatchTvAudioEvent.", e3);
                }
            }
        }
    }

    private HdmiControlManager getHdmiControlManager() {
        if (!this.mHasFeatureHdmiCec) {
            return null;
        }
        return (HdmiControlManager) this.mContext.getSystemService(HdmiControlManager.class);
    }

    private boolean shouldCecAudioDeviceForwardVolumeKeysSystemAudioModeOff() {
        return RoSystemProperties.CEC_AUDIO_DEVICE_FORWARD_VOLUME_KEYS_SYSTEM_AUDIO_MODE_OFF;
    }

    /* access modifiers changed from: package-private */
    public void dispatchMediaKeyWithWakeLock(KeyEvent event) {
        if (DEBUG_INPUT) {
            Slog.d("WindowManager", "dispatchMediaKeyWithWakeLock: " + event);
        }
        if (this.mHavePendingMediaKeyRepeatWithWakeLock) {
            if (DEBUG_INPUT) {
                Slog.d("WindowManager", "dispatchMediaKeyWithWakeLock: canceled repeat");
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

    /* access modifiers changed from: package-private */
    public void dispatchMediaKeyRepeatWithWakeLock(KeyEvent event) {
        this.mHavePendingMediaKeyRepeatWithWakeLock = false;
        KeyEvent repeatEvent = KeyEvent.changeTimeRepeat(event, SystemClock.uptimeMillis(), 1, event.getFlags() | 128);
        if (DEBUG_INPUT) {
            Slog.d("WindowManager", "dispatchMediaKeyRepeatWithWakeLock: " + repeatEvent);
        }
        dispatchMediaKeyWithWakeLockToAudioService(repeatEvent);
        this.mBroadcastWakeLock.release();
    }

    /* access modifiers changed from: package-private */
    public void dispatchMediaKeyWithWakeLockToAudioService(KeyEvent event) {
        if (this.mActivityManagerInternal.isSystemReady()) {
            MediaSessionLegacyHelper.getHelper(this.mContext).sendMediaButtonEvent(event, true);
        }
    }

    /* access modifiers changed from: package-private */
    public void launchVoiceAssistWithWakeLock() {
        IDeviceIdleController dic;
        sendCloseSystemWindows(SYSTEM_DIALOG_REASON_ASSIST);
        if (!keyguardOn()) {
            dic = new Intent("android.speech.action.WEB_SEARCH");
        } else {
            IDeviceIdleController dic2 = IDeviceIdleController.Stub.asInterface(ServiceManager.getService("deviceidle"));
            if (dic2 != null) {
                try {
                    dic2.exitIdle("voice-search");
                } catch (RemoteException e) {
                }
            }
            IDeviceIdleController voiceIntent = new Intent("android.speech.action.VOICE_SEARCH_HANDS_FREE");
            voiceIntent.putExtra("android.speech.extras.EXTRA_SECURE", true);
            dic = voiceIntent;
        }
        startActivityAsUser(dic, UserHandle.CURRENT_OR_SELF);
        this.mBroadcastWakeLock.release();
    }

    @Override // com.android.server.policy.WindowManagerPolicy
    public void startedGoingToSleep(int why) {
        if (DEBUG_WAKEUP) {
            Slog.i("WindowManager", "Started going to sleep... (why=" + WindowManagerPolicyConstants.offReasonToString(why) + ")");
        }
        this.mGoingToSleep = true;
        this.mRequestedOrGoingToSleep = true;
        KeyguardServiceDelegate keyguardServiceDelegate = this.mKeyguardDelegate;
        if (keyguardServiceDelegate != null) {
            keyguardServiceDelegate.onStartedGoingToSleep(why);
            if (why != 4) {
                try {
                    if (this.mWindowManager != null && !this.mKeyguardDelegate.isShowing()) {
                        Bundle bundle = new Bundle();
                        bundle.putParcelable(SYSTEM_DIALOG_REASON_SCREENSHOT, this.mWindowManager.captureLayersForKgd());
                        this.mKeyguardDelegate.doKeyguardTimeout(bundle);
                    }
                } catch (Exception e) {
                }
            }
        }
    }

    @Override // com.android.server.policy.WindowManagerPolicy
    public void finishedGoingToSleep(int why) {
        EventLogTags.writeScreenToggled(0);
        if (DEBUG_WAKEUP) {
            Slog.i("WindowManager", "Finished going to sleep... (why=" + WindowManagerPolicyConstants.offReasonToString(why) + ")");
        }
        MetricsLogger.histogram(this.mContext, "screen_timeout", this.mLockScreenTimeout / 1000);
        this.mGoingToSleep = false;
        this.mRequestedOrGoingToSleep = false;
        this.mDefaultDisplayPolicy.setAwake(false);
        synchronized (this.mLock) {
            updateWakeGestureListenerLp();
            updateLockScreenTimeout();
        }
        this.mDefaultDisplayRotation.updateOrientationListener();
        KeyguardServiceDelegate keyguardServiceDelegate = this.mKeyguardDelegate;
        if (keyguardServiceDelegate != null) {
            keyguardServiceDelegate.onFinishedGoingToSleep(why, this.mCameraGestureTriggeredDuringGoingToSleep);
            if (why != 4) {
                try {
                    if (this.mWindowManager != null && !this.mKeyguardDelegate.isShowing()) {
                        Bundle bundle = new Bundle();
                        bundle.putParcelable(SYSTEM_DIALOG_REASON_SCREENSHOT, this.mWindowManager.captureLayersForKgd());
                        this.mKeyguardDelegate.doKeyguardTimeout(bundle);
                    }
                } catch (Exception e) {
                }
            }
        }
        DisplayFoldController displayFoldController = this.mDisplayFoldController;
        if (displayFoldController != null) {
            displayFoldController.finishedGoingToSleep();
        }
        this.mCameraGestureTriggeredDuringGoingToSleep = false;
    }

    private FingerprintInternal getFingerprintInternal() {
        if (this.mFingerprintInternal == null) {
            this.mFingerprintInternal = (FingerprintInternal) LocalServices.getService(FingerprintInternal.class);
        }
        return this.mFingerprintInternal;
    }

    @Override // com.android.server.policy.WindowManagerPolicy
    public void startedWakingUp(int why) {
        EventLogTags.writeScreenToggled(1);
        if (DEBUG_WAKEUP) {
            Slog.i("WindowManager", "Started waking up... (why=" + WindowManagerPolicyConstants.onReasonToString(why) + ")");
        }
        this.mDefaultDisplayPolicy.setAwake(true);
        synchronized (this.mLock) {
            updateWakeGestureListenerLp();
            updateLockScreenTimeout();
        }
        this.mDefaultDisplayRotation.updateOrientationListener();
        KeyguardServiceDelegate keyguardServiceDelegate = this.mKeyguardDelegate;
        if (keyguardServiceDelegate != null) {
            keyguardServiceDelegate.onStartedWakingUp();
        }
    }

    @Override // com.android.server.policy.WindowManagerPolicy
    public void finishedWakingUp(int why) {
        if (DEBUG_WAKEUP) {
            Slog.i("WindowManager", "Finished waking up... (why=" + WindowManagerPolicyConstants.onReasonToString(why) + ")");
        }
        KeyguardServiceDelegate keyguardServiceDelegate = this.mKeyguardDelegate;
        if (keyguardServiceDelegate != null) {
            keyguardServiceDelegate.onFinishedWakingUp();
        }
        DisplayFoldController displayFoldController = this.mDisplayFoldController;
        if (displayFoldController != null) {
            displayFoldController.finishedWakingUp();
        }
    }

    /* access modifiers changed from: private */
    public void wakeUpFromPowerKey(long eventTime) {
        if (getFingerprintInternal() != null) {
            getFingerprintInternal().notifyPowerKeyPressed();
        }
        wakeUp(eventTime, this.mAllowTheaterModeWakeFromPowerKey, 1, "android.policy:POWER");
    }

    /* access modifiers changed from: private */
    public boolean wakeUp(long wakeTime, boolean wakeInTheaterMode, int reason, String details) {
        boolean theaterModeEnabled = isTheaterModeEnabled();
        if (!wakeInTheaterMode && theaterModeEnabled) {
            return false;
        }
        if (theaterModeEnabled) {
            Settings.Global.putInt(this.mContext.getContentResolver(), "theater_mode_on", 0);
        }
        this.mPowerManager.wakeUp(wakeTime, reason, details);
        return true;
    }

    /* access modifiers changed from: private */
    public void finishKeyguardDrawn() {
        if (this.mDefaultDisplayPolicy.finishKeyguardDrawn()) {
            synchronized (this.mLock) {
                if (this.mKeyguardDelegate != null) {
                    this.mPhoneWinHandler.removeMessages(6);
                }
            }
            this.mWindowManagerInternal.waitForAllWindowsDrawn(this.mWindowManagerDrawCallback, 1000);
        }
    }

    @Override // com.android.server.policy.WindowManagerPolicy
    public void screenTurnedOff() {
        if (DEBUG_WAKEUP) {
            Slog.i("WindowManager", "Screen turned off...");
        }
        updateScreenOffSleepToken(true);
        this.mDefaultDisplayPolicy.screenTurnedOff();
        synchronized (this.mLock) {
            if (this.mKeyguardDelegate != null) {
                this.mKeyguardDelegate.onScreenTurnedOff();
            }
        }
        this.mDefaultDisplayRotation.updateOrientationListener();
        reportScreenStateToVrManager(false);
    }

    private long getKeyguardDrawnTimeout() {
        return ((SystemServiceManager) LocalServices.getService(SystemServiceManager.class)).isBootCompleted() ? 1000 : 5000;
    }

    @Override // com.android.server.policy.WindowManagerPolicy
    public void screenTurningOn(WindowManagerPolicy.ScreenOnListener screenOnListener) {
        if (DEBUG_WAKEUP) {
            Slog.i("WindowManager", "Screen turning on...");
        }
        updateScreenOffSleepToken(false);
        this.mDefaultDisplayPolicy.screenTurnedOn(screenOnListener);
        synchronized (this.mLock) {
            if (this.mKeyguardDelegate == null || !this.mKeyguardDelegate.hasKeyguard()) {
                if (DEBUG_WAKEUP) {
                    Slog.d("WindowManager", "null mKeyguardDelegate: setting mKeyguardDrawComplete.");
                }
                this.mHandler.sendEmptyMessage(5);
            } else {
                this.mPhoneWinHandler.removeMessages(6);
                this.mPhoneWinHandler.sendEmptyMessageDelayed(6, getKeyguardDrawnTimeout());
                this.mKeyguardDelegate.onScreenTurningOn(this.mKeyguardDrawnCallback);
            }
        }
    }

    @Override // com.android.server.policy.WindowManagerPolicy
    public void screenTurnedOn() {
        synchronized (this.mLock) {
            if (this.mKeyguardDelegate != null) {
                this.mKeyguardDelegate.onScreenTurnedOn();
            }
        }
        reportScreenStateToVrManager(true);
    }

    @Override // com.android.server.policy.WindowManagerPolicy
    public void screenTurningOff(WindowManagerPolicy.ScreenOffListener screenOffListener) {
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

    /* access modifiers changed from: private */
    public void finishWindowsDrawn() {
        if (this.mDefaultDisplayPolicy.finishWindowsDrawn()) {
            finishScreenTurningOn();
        }
    }

    private void finishScreenTurningOn() {
        boolean enableScreen;
        this.mDefaultDisplayRotation.updateOrientationListener();
        WindowManagerPolicy.ScreenOnListener listener = this.mDefaultDisplayPolicy.getScreenOnListener();
        if (this.mDefaultDisplayPolicy.finishScreenTurningOn()) {
            boolean awake = this.mDefaultDisplayPolicy.isAwake();
            synchronized (this.mLock) {
                if (this.mKeyguardDrawnOnce || !awake) {
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
            if (listener != null) {
                listener.onScreenOn();
            }
            if (enableScreen) {
                try {
                    this.mWindowManager.enableScreenIfNeeded();
                } catch (RemoteException e) {
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:10:0x000f, code lost:
        if (r2.mBootMsgDialog == null) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0013, code lost:
        if (com.android.server.policy.PhoneWindowManager.DEBUG_WAKEUP == false) goto L_0x001c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0015, code lost:
        android.util.Slog.d("WindowManager", "handleHideBootMessage: dismissing");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x001c, code lost:
        r2.mBootMsgDialog.dismiss();
        r2.mBootMsgDialog = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:?, code lost:
        return;
     */
    public void handleHideBootMessage() {
        synchronized (this.mLock) {
            if (!this.mKeyguardDrawnOnce) {
                this.mBootMessageNeedsHiding = true;
            }
        }
    }

    @Override // com.android.server.policy.WindowManagerPolicy
    public boolean isScreenOn() {
        return this.mDefaultDisplayPolicy.isScreenOnEarly();
    }

    @Override // com.android.server.policy.WindowManagerPolicy
    public boolean okToAnimate() {
        return this.mDefaultDisplayPolicy.isAwake() && !this.mGoingToSleep;
    }

    @Override // com.android.server.policy.WindowManagerPolicy
    public void enableKeyguard(boolean enabled) {
        KeyguardServiceDelegate keyguardServiceDelegate = this.mKeyguardDelegate;
        if (keyguardServiceDelegate != null) {
            keyguardServiceDelegate.setKeyguardEnabled(enabled);
        }
    }

    @Override // com.android.server.policy.WindowManagerPolicy
    public void exitKeyguardSecurely(WindowManagerPolicy.OnKeyguardExitResult callback) {
        KeyguardServiceDelegate keyguardServiceDelegate = this.mKeyguardDelegate;
        if (keyguardServiceDelegate != null) {
            keyguardServiceDelegate.verifyUnlock(callback);
        }
    }

    @Override // com.android.server.policy.WindowManagerPolicy
    public boolean isKeyguardShowingAndNotOccluded() {
        KeyguardServiceDelegate keyguardServiceDelegate = this.mKeyguardDelegate;
        if (keyguardServiceDelegate != null && keyguardServiceDelegate.isShowing() && !this.mKeyguardOccluded) {
            return true;
        }
        return false;
    }

    @Override // com.android.server.policy.WindowManagerPolicy
    public boolean isKeyguardTrustedLw() {
        KeyguardServiceDelegate keyguardServiceDelegate = this.mKeyguardDelegate;
        if (keyguardServiceDelegate == null) {
            return false;
        }
        return keyguardServiceDelegate.isTrusted();
    }

    @Override // com.android.server.policy.WindowManagerPolicy
    public boolean isKeyguardLocked() {
        return keyguardOn();
    }

    @Override // com.android.server.policy.WindowManagerPolicy
    public boolean isKeyguardSecure(int userId) {
        KeyguardServiceDelegate keyguardServiceDelegate = this.mKeyguardDelegate;
        if (keyguardServiceDelegate == null) {
            return false;
        }
        return keyguardServiceDelegate.isSecure(userId);
    }

    @Override // com.android.server.policy.WindowManagerPolicy
    public boolean isKeyguardOccluded() {
        if (this.mKeyguardDelegate == null) {
            return false;
        }
        return this.mKeyguardOccluded;
    }

    @Override // com.android.server.policy.WindowManagerPolicy
    public boolean inKeyguardRestrictedKeyInputMode() {
        KeyguardServiceDelegate keyguardServiceDelegate = this.mKeyguardDelegate;
        if (keyguardServiceDelegate == null) {
            return false;
        }
        return keyguardServiceDelegate.isInputRestricted();
    }

    @Override // com.android.server.policy.WindowManagerPolicy
    public void dismissKeyguardLw(IKeyguardDismissCallback callback, CharSequence message) {
        KeyguardServiceDelegate keyguardServiceDelegate = this.mKeyguardDelegate;
        if (keyguardServiceDelegate != null && keyguardServiceDelegate.isShowing()) {
            if (DEBUG_KEYGUARD) {
                Slog.d("WindowManager", "PWM.dismissKeyguardLw");
            }
            this.mKeyguardDelegate.dismiss(callback, message);
        } else if (callback != null) {
            try {
                callback.onDismissError();
            } catch (RemoteException e) {
                Slog.w("WindowManager", "Failed to call callback", e);
            }
        }
    }

    @Override // com.android.server.policy.WindowManagerPolicy
    public boolean isKeyguardDrawnLw() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mKeyguardDrawnOnce;
        }
        return z;
    }

    @Override // com.android.server.policy.WindowManagerPolicy
    public void startKeyguardExitAnimation(long startTime, long fadeoutDuration) {
        if (this.mKeyguardDelegate != null) {
            if (DEBUG_KEYGUARD) {
                Slog.d("WindowManager", "PWM.startKeyguardExitAnimation");
            }
            this.mKeyguardDelegate.startKeyguardExitAnimation(startTime, fadeoutDuration);
        }
    }

    /* access modifiers changed from: package-private */
    public void sendCloseSystemWindows() {
        PhoneWindow.sendCloseSystemWindows(this.mContext, (String) null);
    }

    /* access modifiers changed from: package-private */
    public void sendCloseSystemWindows(String reason) {
        PhoneWindow.sendCloseSystemWindows(this.mContext, reason);
    }

    @Override // com.android.server.policy.WindowManagerPolicy
    public void setSafeMode(boolean safeMode) {
        this.mSafeMode = safeMode;
        if (safeMode) {
            performHapticFeedback(OppoScreenOffGestureManager.MSG_SCREEN_TURNED_OFF, true, "Safe Mode Enabled");
        }
    }

    static long[] getLongIntArray(Resources r, int resid) {
        return ArrayUtils.convertToLongArray(r.getIntArray(resid));
    }

    private void bindKeyguard() {
        synchronized (this.mLock) {
            if (!this.mKeyguardBound) {
                this.mKeyguardBound = true;
                this.mKeyguardDelegate.bindService(this.mContext);
            }
        }
    }

    @Override // com.android.server.policy.WindowManagerPolicy
    public void onSystemUiStarted() {
        bindKeyguard();
    }

    @Override // com.android.server.policy.WindowManagerPolicy
    public void systemReady() {
        this.mKeyguardDelegate.onSystemReady();
        this.mVrManagerInternal = (VrManagerInternal) LocalServices.getService(VrManagerInternal.class);
        if (this.mVrManagerInternal != null) {
            this.mVrManagerInternal.addPersistentVrModeStateListener(this.mPersistentVrModeListener);
        }
        readCameraLensCoverState();
        updateUiMode();
        this.mDefaultDisplayRotation.updateOrientationListener();
        synchronized (this.mLock) {
            this.mSystemReady = true;
            this.mHandler.post(new Runnable() {
                /* class com.android.server.policy.PhoneWindowManager.AnonymousClass14 */

                public void run() {
                    PhoneWindowManager.this.updateSettings();
                }
            });
            if (this.mSystemBooted) {
                this.mKeyguardDelegate.onBootCompleted();
            }
        }
        this.mAutofillManagerInternal = (AutofillManagerInternal) LocalServices.getService(AutofillManagerInternal.class);
    }

    @Override // com.android.server.policy.WindowManagerPolicy
    public void systemBooted() {
        bindKeyguard();
        synchronized (this.mLock) {
            this.mSystemBooted = true;
            if (this.mSystemReady) {
                this.mKeyguardDelegate.onBootCompleted();
            }
        }
        startedWakingUp(3);
        finishedWakingUp(3);
        screenTurningOn(null);
        screenTurnedOn();
    }

    @Override // com.android.server.policy.WindowManagerPolicy
    public boolean canDismissBootAnimation() {
        return this.mDefaultDisplayPolicy.isKeyguardDrawComplete();
    }

    @Override // com.android.server.policy.WindowManagerPolicy
    public void showBootMessage(final CharSequence msg, boolean always) {
        this.mHandler.post(new Runnable() {
            /* class com.android.server.policy.PhoneWindowManager.AnonymousClass15 */

            public void run() {
                int theme;
                if (PhoneWindowManager.this.mBootMsgDialog == null) {
                    if (PhoneWindowManager.this.mContext.getPackageManager().hasSystemFeature("android.software.leanback")) {
                        theme = 16974873;
                    } else {
                        theme = 0;
                    }
                    PhoneWindowManager phoneWindowManager = PhoneWindowManager.this;
                    phoneWindowManager.mBootMsgDialog = new ProgressDialog(phoneWindowManager.mContext, theme) {
                        /* class com.android.server.policy.PhoneWindowManager.AnonymousClass15.AnonymousClass1 */

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
                    if (PhoneWindowManager.this.mContext.getPackageManager().isDeviceUpgrading()) {
                        PhoneWindowManager.this.mBootMsgDialog.setTitle(17039496);
                    } else {
                        PhoneWindowManager.this.mBootMsgDialog.setTitle(17039489);
                    }
                    PhoneWindowManager.this.mBootMsgDialog.setProgressStyle(0);
                    PhoneWindowManager.this.mBootMsgDialog.setIndeterminate(true);
                    PhoneWindowManager.this.mBootMsgDialog.getWindow().setType(OppoOperatorManagerService.H.MSG_PERSIST);
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

    @Override // com.android.server.policy.WindowManagerPolicy
    public void hideBootMessages() {
        this.mHandler.sendEmptyMessage(11);
    }

    @Override // com.android.server.policy.WindowManagerPolicy
    public void requestUserActivityNotification() {
        if (!this.mNotifyUserActivity && !this.mHandler.hasMessages(26)) {
            this.mNotifyUserActivity = true;
        }
    }

    @Override // com.android.server.policy.WindowManagerPolicy
    public void userActivity() {
        synchronized (this.mScreenLockTimeout) {
            if (this.mLockScreenTimerActive) {
                this.mHandler.removeCallbacks(this.mScreenLockTimeout);
                this.mHandler.postDelayed(this.mScreenLockTimeout, (long) this.mLockScreenTimeout);
            }
        }
        if (this.mDefaultDisplayPolicy.isAwake() && this.mNotifyUserActivity) {
            this.mHandler.sendEmptyMessageDelayed(26, 200);
            this.mNotifyUserActivity = false;
        }
    }

    class ScreenLockTimeout implements Runnable {
        Bundle options;

        ScreenLockTimeout() {
        }

        public void run() {
            synchronized (this) {
                if (PhoneWindowManager.localLOGV) {
                    Log.v("WindowManager", "mScreenLockTimeout activating keyguard");
                }
                if (PhoneWindowManager.this.mKeyguardDelegate != null) {
                    PhoneWindowManager.this.mKeyguardDelegate.doKeyguardTimeout(this.options);
                }
                PhoneWindowManager.this.mLockScreenTimerActive = false;
                this.options = null;
            }
        }

        public void setLockOptions(Bundle options2) {
            this.options = options2;
        }
    }

    @Override // com.android.server.policy.WindowManagerPolicy
    public void lockNow(Bundle options) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
        this.mHandler.removeCallbacks(this.mScreenLockTimeout);
        if (options != null) {
            this.mScreenLockTimeout.setLockOptions(options);
        }
        this.mHandler.post(this.mScreenLockTimeout);
    }

    @Override // com.android.server.policy.WindowManagerPolicy
    public void setAllowLockscreenWhenOn(int displayId, boolean allow) {
        if (allow) {
            this.mAllowLockscreenWhenOnDisplays.add(Integer.valueOf(displayId));
        } else {
            this.mAllowLockscreenWhenOnDisplays.remove(Integer.valueOf(displayId));
        }
        updateLockScreenTimeout();
    }

    private void updateLockScreenTimeout() {
        synchronized (this.mScreenLockTimeout) {
            boolean enable = !this.mAllowLockscreenWhenOnDisplays.isEmpty() && this.mDefaultDisplayPolicy.isAwake() && this.mKeyguardDelegate != null && this.mKeyguardDelegate.isSecure(this.mCurrentUserId);
            if (this.mLockScreenTimerActive != enable) {
                if (enable) {
                    if (localLOGV) {
                        Log.v("WindowManager", "setting lockscreen timer");
                    }
                    this.mHandler.removeCallbacks(this.mScreenLockTimeout);
                    this.mHandler.postDelayed(this.mScreenLockTimeout, (long) this.mLockScreenTimeout);
                } else {
                    if (localLOGV) {
                        Log.v("WindowManager", "clearing lockscreen timer");
                    }
                    this.mHandler.removeCallbacks(this.mScreenLockTimeout);
                }
                this.mLockScreenTimerActive = enable;
            }
        }
    }

    private void schedulePossibleVeryLongPressReboot() {
        this.mHandler.removeCallbacks(this.mPossibleVeryLongPressReboot);
        this.mHandler.postDelayed(this.mPossibleVeryLongPressReboot, (long) this.mVeryLongPressTimeout);
    }

    private void cancelPossibleVeryLongPressReboot() {
        this.mHandler.removeCallbacks(this.mPossibleVeryLongPressReboot);
    }

    private void updateScreenOffSleepToken(boolean acquire) {
        if (!acquire) {
            ActivityTaskManagerInternal.SleepToken sleepToken = this.mScreenOffSleepToken;
            if (sleepToken != null) {
                sleepToken.release();
                this.mScreenOffSleepToken = null;
            }
        } else if (this.mScreenOffSleepToken == null) {
            this.mScreenOffSleepToken = this.mActivityTaskManagerInternal.acquireSleepToken("ScreenOff", 0);
        }
    }

    @Override // com.android.server.policy.WindowManagerPolicy
    public void enableScreenAfterBoot() {
        readLidState();
        applyLidSwitchState();
        updateRotation(true);
    }

    private void applyLidSwitchState() {
        DisplayFoldController displayFoldController;
        int lidState = this.mDefaultDisplayPolicy.getLidState();
        boolean z = true;
        if (this.mLidControlsDisplayFold && (displayFoldController = this.mDisplayFoldController) != null) {
            if (lidState != 0) {
                z = false;
            }
            displayFoldController.requestDeviceFolded(z);
        } else if (lidState == 0) {
            int lidBehavior = getLidBehavior();
            if (lidBehavior == 1) {
                goToSleep(SystemClock.uptimeMillis(), 3, 1);
            } else if (lidBehavior == 2) {
                this.mWindowManagerFuncs.lockDeviceNow();
            }
        }
        synchronized (this.mLock) {
            updateWakeGestureListenerLp();
        }
    }

    /* access modifiers changed from: package-private */
    public void updateUiMode() {
        if (this.mUiModeManager == null) {
            this.mUiModeManager = IUiModeManager.Stub.asInterface(ServiceManager.getService("uimode"));
        }
        try {
            this.mUiMode = this.mUiModeManager.getCurrentModeType();
        } catch (RemoteException e) {
        }
    }

    @Override // com.android.server.policy.WindowManagerPolicy
    public int getUiMode() {
        return this.mUiMode;
    }

    /* access modifiers changed from: package-private */
    public void updateRotation(boolean alwaysSendConfiguration) {
        try {
            this.mWindowManager.updateRotation(alwaysSendConfiguration, false);
        } catch (RemoteException e) {
        }
    }

    /* access modifiers changed from: package-private */
    public Intent createHomeDockIntent() {
        Intent intent = null;
        int i = this.mUiMode;
        if (i == 3) {
            if (this.mEnableCarDockHomeCapture) {
                intent = this.mCarDockIntent;
            }
        } else if (i != 2) {
            if (i == 6) {
                int dockMode = this.mDefaultDisplayPolicy.getDockMode();
                if (dockMode == 1 || dockMode == 4 || dockMode == 3) {
                    intent = this.mDeskDockIntent;
                }
            } else if (i == 7) {
                intent = this.mVrHeadsetHomeIntent;
            }
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

    /* access modifiers changed from: package-private */
    public void startDockOrHome(int displayId, boolean fromHomeKey, boolean awakenFromDreams) {
        try {
            ActivityManager.getService().stopAppSwitches();
        } catch (RemoteException e) {
        }
        sendCloseSystemWindows(SYSTEM_DIALOG_REASON_HOME_KEY);
        if (awakenFromDreams) {
            awakenDreams();
        }
        if (!isUserSetupComplete()) {
            Slog.i("WindowManager", "Not going home because user setup is in progress.");
            return;
        }
        Intent dock = createHomeDockIntent();
        if (dock != null) {
            if (fromHomeKey) {
                try {
                    dock.putExtra("android.intent.extra.FROM_HOME_KEY", fromHomeKey);
                } catch (ActivityNotFoundException e2) {
                }
            }
            startActivityAsUser(dock, UserHandle.CURRENT);
            return;
        }
        this.mActivityTaskManagerInternal.startHomeOnDisplay(this.mCurrentUserId, "startDockOrHome", displayId, true, fromHomeKey);
    }

    /* access modifiers changed from: package-private */
    public boolean goHome() {
        if (!isUserSetupComplete()) {
            Slog.i("WindowManager", "Not going home because user setup is in progress.");
            return false;
        }
        try {
            if (SystemProperties.getInt("persist.sys.uts-test-mode", 0) == 1) {
                Log.d("WindowManager", "UTS-TEST-MODE");
            } else {
                ActivityManager.getService().stopAppSwitches();
                sendCloseSystemWindows();
                Intent dock = createHomeDockIntent();
                if (dock != null && ActivityTaskManager.getService().startActivityAsUser((IApplicationThread) null, (String) null, dock, dock.resolveTypeIfNeeded(this.mContext.getContentResolver()), (IBinder) null, (String) null, 0, 1, (ProfilerInfo) null, (Bundle) null, -2) == 1) {
                    return false;
                }
            }
            if (ActivityTaskManager.getService().startActivityAsUser((IApplicationThread) null, (String) null, this.mHomeIntent, this.mHomeIntent.resolveTypeIfNeeded(this.mContext.getContentResolver()), (IBinder) null, (String) null, 0, 1, (ProfilerInfo) null, (Bundle) null, -2) == 1) {
                return false;
            }
            return true;
        } catch (RemoteException e) {
        }
    }

    private boolean isTheaterModeEnabled() {
        return Settings.Global.getInt(this.mContext.getContentResolver(), "theater_mode_on", 0) == 1;
    }

    /* access modifiers changed from: private */
    public boolean performHapticFeedback(int effectId, boolean always, String reason) {
        return performHapticFeedback(Process.myUid(), this.mContext.getOpPackageName(), effectId, always, reason);
    }

    @Override // com.android.server.policy.WindowManagerPolicy
    public boolean performHapticFeedback(int uid, String packageName, int effectId, boolean always, String reason) {
        VibrationEffect effect;
        if (!this.mVibrator.hasVibrator()) {
            return false;
        }
        if (((Settings.System.getIntForUser(this.mContext.getContentResolver(), "haptic_feedback_enabled", 0, -2) == 0) && !always) || (effect = getVibrationEffect(effectId)) == null) {
            return false;
        }
        this.mVibrator.vibrate(uid, packageName, effect, reason, VIBRATION_ATTRIBUTES);
        return true;
    }

    private VibrationEffect getVibrationEffect(int effectId) {
        long[] pattern;
        if (effectId != 0) {
            if (effectId != 1) {
                if (effectId != 10001) {
                    switch (effectId) {
                        case 3:
                        case 12:
                        case 15:
                        case 16:
                            break;
                        case 4:
                            return VibrationEffect.get(21);
                        case 5:
                            pattern = this.mCalendarDateVibePattern;
                            break;
                        case 6:
                            return VibrationEffect.get(2);
                        case 7:
                        case 8:
                        case 10:
                        case 11:
                        case 13:
                            return VibrationEffect.get(2, false);
                        case 9:
                            if (!this.mHapticTextHandleEnabled) {
                                return null;
                            }
                            return VibrationEffect.get(21);
                        case 14:
                            break;
                        case 17:
                            return VibrationEffect.get(1);
                        default:
                            return null;
                    }
                } else {
                    pattern = this.mSafeModeEnabledVibePattern;
                }
                if (pattern.length == 0) {
                    return null;
                }
                if (pattern.length == 1) {
                    return VibrationEffect.createOneShot(pattern[0], -1);
                }
                return VibrationEffect.createWaveform(pattern, -1);
            }
            return VibrationEffect.get(0);
        }
        return VibrationEffect.get(5);
    }

    @Override // com.android.server.policy.WindowManagerPolicy
    public void keepScreenOnStartedLw() {
    }

    @Override // com.android.server.policy.WindowManagerPolicy
    public void keepScreenOnStoppedLw() {
        if (isKeyguardShowingAndNotOccluded()) {
            this.mPowerManager.userActivity(SystemClock.uptimeMillis(), false);
        }
    }

    @Override // com.android.server.policy.WindowManagerPolicy
    public boolean hasNavigationBar() {
        return this.mDefaultDisplayPolicy.hasNavigationBar();
    }

    @Override // com.android.server.policy.WindowManagerPolicy
    public void setDismissImeOnBackKeyPressed(boolean newValue) {
        this.mDismissImeOnBackKeyPressed = newValue;
    }

    @Override // com.android.server.policy.WindowManagerPolicy
    public void setCurrentUserLw(int newUserId) {
        this.mCurrentUserId = newUserId;
        KeyguardServiceDelegate keyguardServiceDelegate = this.mKeyguardDelegate;
        if (keyguardServiceDelegate != null) {
            keyguardServiceDelegate.setCurrentUser(newUserId);
        }
        AccessibilityShortcutController accessibilityShortcutController = this.mAccessibilityShortcutController;
        if (accessibilityShortcutController != null) {
            accessibilityShortcutController.setCurrentUser(newUserId);
        }
        StatusBarManagerInternal statusBar = getStatusBarManagerInternal();
        if (statusBar != null) {
            statusBar.setCurrentUser(newUserId);
        }
    }

    @Override // com.android.server.policy.WindowManagerPolicy
    public void setSwitchingUser(boolean switching) {
        this.mKeyguardDelegate.setSwitchingUser(switching);
    }

    @Override // com.android.server.policy.WindowManagerPolicy
    public boolean isTopLevelWindow(int windowType) {
        if (windowType < 1000 || windowType > 1999 || windowType == 1003) {
            return true;
        }
        return false;
    }

    @Override // com.android.server.policy.WindowManagerPolicy
    public void writeToProto(ProtoOutputStream proto, long fieldId) {
        long token = proto.start(fieldId);
        proto.write(1159641169922L, this.mDefaultDisplayRotation.getUserRotationMode());
        proto.write(1159641169923L, this.mDefaultDisplayRotation.getUserRotation());
        proto.write(1159641169924L, this.mDefaultDisplayRotation.getCurrentAppOrientation());
        proto.write(1133871366149L, this.mDefaultDisplayPolicy.isScreenOnFully());
        proto.write(1133871366150L, this.mDefaultDisplayPolicy.isKeyguardDrawComplete());
        proto.write(1133871366151L, this.mDefaultDisplayPolicy.isWindowManagerDrawComplete());
        proto.write(1133871366156L, this.mKeyguardOccluded);
        proto.write(1133871366157L, this.mKeyguardOccludedChanged);
        proto.write(1133871366158L, this.mPendingKeyguardOccluded);
        KeyguardServiceDelegate keyguardServiceDelegate = this.mKeyguardDelegate;
        if (keyguardServiceDelegate != null) {
            keyguardServiceDelegate.writeToProto(proto, 1146756268052L);
        }
        proto.end(token);
    }

    @Override // com.android.server.policy.WindowManagerPolicy
    public void dump(String prefix, PrintWriter pw, String[] args) {
        if (args != null && args.length > 2) {
            boolean on = "1".equals(args[2]);
            pw.println("dynamicallyConfigLogTag PhoneWindowManager,, on:" + on);
            DEBUG_INPUT = on;
        }
        pw.print(prefix);
        pw.print("mSafeMode=");
        pw.print(this.mSafeMode);
        pw.print(" mSystemReady=");
        pw.print(this.mSystemReady);
        pw.print(" mSystemBooted=");
        pw.println(this.mSystemBooted);
        pw.print(prefix);
        pw.print("mCameraLensCoverState=");
        pw.println(WindowManagerPolicy.WindowManagerFuncs.cameraLensStateToString(this.mCameraLensCoverState));
        pw.print(prefix);
        pw.print("mWakeGestureEnabledSetting=");
        pw.println(this.mWakeGestureEnabledSetting);
        pw.print(prefix);
        pw.print("mUiMode=");
        pw.print(Configuration.uiModeToString(this.mUiMode));
        pw.print("mEnableCarDockHomeCapture=");
        pw.println(this.mEnableCarDockHomeCapture);
        pw.print(prefix);
        pw.print("mLidKeyboardAccessibility=");
        pw.print(this.mLidKeyboardAccessibility);
        pw.print(" mLidNavigationAccessibility=");
        pw.print(this.mLidNavigationAccessibility);
        pw.print(" getLidBehavior=");
        pw.println(lidBehaviorToString(getLidBehavior()));
        pw.print(prefix);
        pw.print("mLongPressOnBackBehavior=");
        pw.println(longPressOnBackBehaviorToString(this.mLongPressOnBackBehavior));
        pw.print(prefix);
        pw.print("mLongPressOnHomeBehavior=");
        pw.println(longPressOnHomeBehaviorToString(this.mLongPressOnHomeBehavior));
        pw.print(prefix);
        pw.print("mDoubleTapOnHomeBehavior=");
        pw.println(doubleTapOnHomeBehaviorToString(this.mDoubleTapOnHomeBehavior));
        pw.print(prefix);
        pw.print("mShortPressOnPowerBehavior=");
        pw.println(shortPressOnPowerBehaviorToString(this.mShortPressOnPowerBehavior));
        pw.print(prefix);
        pw.print("mLongPressOnPowerBehavior=");
        pw.println(longPressOnPowerBehaviorToString(this.mLongPressOnPowerBehavior));
        pw.print(prefix);
        pw.print("mVeryLongPressOnPowerBehavior=");
        pw.println(veryLongPressOnPowerBehaviorToString(this.mVeryLongPressOnPowerBehavior));
        pw.print(prefix);
        pw.print("mDoublePressOnPowerBehavior=");
        pw.println(multiPressOnPowerBehaviorToString(this.mDoublePressOnPowerBehavior));
        pw.print(prefix);
        pw.print("mTriplePressOnPowerBehavior=");
        pw.println(multiPressOnPowerBehaviorToString(this.mTriplePressOnPowerBehavior));
        pw.print(prefix);
        pw.print("mShortPressOnSleepBehavior=");
        pw.println(shortPressOnSleepBehaviorToString(this.mShortPressOnSleepBehavior));
        pw.print(prefix);
        pw.print("mShortPressOnWindowBehavior=");
        pw.println(shortPressOnWindowBehaviorToString(this.mShortPressOnWindowBehavior));
        pw.print(prefix);
        pw.print("mAllowStartActivityForLongPressOnPowerDuringSetup=");
        pw.println(this.mAllowStartActivityForLongPressOnPowerDuringSetup);
        pw.print(prefix);
        pw.print("mHasSoftInput=");
        pw.print(this.mHasSoftInput);
        pw.print(" mHapticTextHandleEnabled=");
        pw.println(this.mHapticTextHandleEnabled);
        pw.print(prefix);
        pw.print("mDismissImeOnBackKeyPressed=");
        pw.print(this.mDismissImeOnBackKeyPressed);
        pw.print(" mIncallPowerBehavior=");
        pw.println(incallPowerBehaviorToString(this.mIncallPowerBehavior));
        pw.print(prefix);
        pw.print("mIncallBackBehavior=");
        pw.print(incallBackBehaviorToString(this.mIncallBackBehavior));
        pw.print(" mEndcallBehavior=");
        pw.println(endcallBehaviorToString(this.mEndcallBehavior));
        pw.print(prefix);
        pw.print("mDisplayHomeButtonHandlers=");
        for (int i = 0; i < this.mDisplayHomeButtonHandlers.size(); i++) {
            pw.println(this.mDisplayHomeButtonHandlers.get(this.mDisplayHomeButtonHandlers.keyAt(i)));
        }
        pw.print(prefix);
        pw.print("mKeyguardOccluded=");
        pw.print(this.mKeyguardOccluded);
        pw.print(" mKeyguardOccludedChanged=");
        pw.print(this.mKeyguardOccludedChanged);
        pw.print(" mPendingKeyguardOccluded=");
        pw.println(this.mPendingKeyguardOccluded);
        pw.print(prefix);
        pw.print("mAllowLockscreenWhenOnDisplays=");
        pw.print(!this.mAllowLockscreenWhenOnDisplays.isEmpty());
        pw.print(" mLockScreenTimeout=");
        pw.print(this.mLockScreenTimeout);
        pw.print(" mLockScreenTimerActive=");
        pw.println(this.mLockScreenTimerActive);
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
        MyWakeGestureListener myWakeGestureListener = this.mWakeGestureListener;
        if (myWakeGestureListener != null) {
            myWakeGestureListener.dump(pw, prefix);
        }
        BurnInProtectionHelper burnInProtectionHelper = this.mBurnInProtectionHelper;
        if (burnInProtectionHelper != null) {
            burnInProtectionHelper.dump(prefix, pw);
        }
        KeyguardServiceDelegate keyguardServiceDelegate = this.mKeyguardDelegate;
        if (keyguardServiceDelegate != null) {
            keyguardServiceDelegate.dump(prefix, pw);
        }
        pw.print(prefix);
        pw.println("Looper state:");
        Looper looper = this.mHandler.getLooper();
        PrintWriterPrinter printWriterPrinter = new PrintWriterPrinter(pw);
        looper.dump(printWriterPrinter, prefix + "  ");
    }

    private static String endcallBehaviorToString(int behavior) {
        StringBuilder sb = new StringBuilder();
        if ((behavior & 1) != 0) {
            sb.append("home|");
        }
        if ((behavior & 2) != 0) {
            sb.append("sleep|");
        }
        int N = sb.length();
        if (N == 0) {
            return "<nothing>";
        }
        return sb.substring(0, N - 1);
    }

    private static String incallPowerBehaviorToString(int behavior) {
        if ((behavior & 2) != 0) {
            return "hangup";
        }
        return "sleep";
    }

    private static String incallBackBehaviorToString(int behavior) {
        if ((behavior & 1) != 0) {
            return "hangup";
        }
        return "<nothing>";
    }

    private static String longPressOnBackBehaviorToString(int behavior) {
        if (behavior == 0) {
            return "LONG_PRESS_BACK_NOTHING";
        }
        if (behavior != 1) {
            return Integer.toString(behavior);
        }
        return "LONG_PRESS_BACK_GO_TO_VOICE_ASSIST";
    }

    private static String longPressOnHomeBehaviorToString(int behavior) {
        if (behavior == 0) {
            return "LONG_PRESS_HOME_NOTHING";
        }
        if (behavior == 1) {
            return "LONG_PRESS_HOME_ALL_APPS";
        }
        if (behavior != 2) {
            return Integer.toString(behavior);
        }
        return "LONG_PRESS_HOME_ASSIST";
    }

    private static String doubleTapOnHomeBehaviorToString(int behavior) {
        if (behavior == 0) {
            return "DOUBLE_TAP_HOME_NOTHING";
        }
        if (behavior != 1) {
            return Integer.toString(behavior);
        }
        return "DOUBLE_TAP_HOME_RECENT_SYSTEM_UI";
    }

    private static String shortPressOnPowerBehaviorToString(int behavior) {
        if (behavior == 0) {
            return "SHORT_PRESS_POWER_NOTHING";
        }
        if (behavior == 1) {
            return "SHORT_PRESS_POWER_GO_TO_SLEEP";
        }
        if (behavior == 2) {
            return "SHORT_PRESS_POWER_REALLY_GO_TO_SLEEP";
        }
        if (behavior == 3) {
            return "SHORT_PRESS_POWER_REALLY_GO_TO_SLEEP_AND_GO_HOME";
        }
        if (behavior == 4) {
            return "SHORT_PRESS_POWER_GO_HOME";
        }
        if (behavior != 5) {
            return Integer.toString(behavior);
        }
        return "SHORT_PRESS_POWER_CLOSE_IME_OR_GO_HOME";
    }

    private static String longPressOnPowerBehaviorToString(int behavior) {
        if (behavior == 0) {
            return "LONG_PRESS_POWER_NOTHING";
        }
        if (behavior == 1) {
            return "LONG_PRESS_POWER_GLOBAL_ACTIONS";
        }
        if (behavior == 2) {
            return "LONG_PRESS_POWER_SHUT_OFF";
        }
        if (behavior == 3) {
            return "LONG_PRESS_POWER_SHUT_OFF_NO_CONFIRM";
        }
        if (behavior == 4) {
            return "LONG_PRESS_POWER_GO_TO_VOICE_ASSIST";
        }
        if (behavior != 5) {
            return Integer.toString(behavior);
        }
        return "LONG_PRESS_POWER_ASSISTANT";
    }

    private static String veryLongPressOnPowerBehaviorToString(int behavior) {
        if (behavior == 0) {
            return "VERY_LONG_PRESS_POWER_NOTHING";
        }
        if (behavior != 1) {
            return Integer.toString(behavior);
        }
        return "VERY_LONG_PRESS_POWER_GLOBAL_ACTIONS";
    }

    private static String multiPressOnPowerBehaviorToString(int behavior) {
        if (behavior == 0) {
            return "MULTI_PRESS_POWER_NOTHING";
        }
        if (behavior == 1) {
            return "MULTI_PRESS_POWER_THEATER_MODE";
        }
        if (behavior != 2) {
            return Integer.toString(behavior);
        }
        return "MULTI_PRESS_POWER_BRIGHTNESS_BOOST";
    }

    private static String shortPressOnSleepBehaviorToString(int behavior) {
        if (behavior == 0) {
            return "SHORT_PRESS_SLEEP_GO_TO_SLEEP";
        }
        if (behavior != 1) {
            return Integer.toString(behavior);
        }
        return "SHORT_PRESS_SLEEP_GO_TO_SLEEP_AND_GO_HOME";
    }

    private static String shortPressOnWindowBehaviorToString(int behavior) {
        if (behavior == 0) {
            return "SHORT_PRESS_WINDOW_NOTHING";
        }
        if (behavior != 1) {
            return Integer.toString(behavior);
        }
        return "SHORT_PRESS_WINDOW_PICTURE_IN_PICTURE";
    }

    private static String lidBehaviorToString(int behavior) {
        if (behavior == 0) {
            return "LID_BEHAVIOR_NONE";
        }
        if (behavior == 1) {
            return "LID_BEHAVIOR_SLEEP";
        }
        if (behavior != 2) {
            return Integer.toString(behavior);
        }
        return "LID_BEHAVIOR_LOCK";
    }

    @Override // com.android.server.policy.WindowManagerPolicy
    public boolean setAodShowing(boolean aodShowing) {
        if (this.mAodShowing == aodShowing) {
            return false;
        }
        this.mAodShowing = aodShowing;
        return true;
    }

    private class HdmiVideoExtconUEventObserver extends ExtconStateObserver<Boolean> {
        private static final String HDMI_EXIST = "HDMI=1";
        private static final String NAME = "hdmi";
        private final ExtconUEventObserver.ExtconInfo mHdmi;

        private HdmiVideoExtconUEventObserver() {
            this.mHdmi = new ExtconUEventObserver.ExtconInfo(NAME);
        }

        /* access modifiers changed from: private */
        public boolean init() {
            boolean plugged = false;
            try {
                plugged = ((Boolean) parseStateFromFile(this.mHdmi)).booleanValue();
            } catch (FileNotFoundException e) {
                Slog.w("WindowManager", this.mHdmi.getStatePath() + " not found while attempting to determine initial state", e);
            } catch (IOException e2) {
                Slog.e("WindowManager", "Error reading " + this.mHdmi.getStatePath() + " while attempting to determine initial state", e2);
            }
            startObserving(this.mHdmi);
            return plugged;
        }

        public void updateState(ExtconUEventObserver.ExtconInfo extconInfo, String eventName, Boolean state) {
            PhoneWindowManager.this.mDefaultDisplayPolicy.setHdmiPlugged(state.booleanValue());
        }

        @Override // com.android.server.ExtconStateObserver
        public Boolean parseState(ExtconUEventObserver.ExtconInfo extconIfno, String state) {
            return Boolean.valueOf(state.contains(HDMI_EXIST));
        }
    }

    public void setDynamicalLogEnable(boolean on) {
        DEBUG_INPUT = on;
        DEBUG_SPLASH_SCREEN = on;
        DEBUG_KEYGUARD = on;
        DEBUG_WAKEUP = on;
    }

    /* access modifiers changed from: package-private */
    public Object getLock() {
        return this.mLock;
    }

    @Override // com.android.server.policy.WindowManagerPolicy
    public int getNavBarHeight(int rotation, int uiMode) {
        return this.mDefaultDisplayPolicy.getNavigationBarHeight(rotation, uiMode);
    }

    /* access modifiers changed from: package-private */
    public void colorCancelPendingPowerKeyAction() {
        cancelPendingPowerKeyAction();
    }

    /* access modifiers changed from: package-private */
    public void colorInterceptPowerKeyUpForWallet(KeyEvent event, boolean interactive, int powerKeyPressCounter) {
        powerPress(event.getDownTime(), interactive, powerKeyPressCounter);
    }

    /* access modifiers changed from: package-private */
    public void colorPowerPress(long eventTime, boolean interactive, int powerKeyPressCounter) {
        powerPress(eventTime, interactive, powerKeyPressCounter);
    }

    /* access modifiers changed from: package-private */
    public void colorGotoSleep(long eventTime, int reason, int flags) {
        goToSleep(eventTime, reason, flags);
    }

    /* access modifiers changed from: package-private */
    public void colorWakeUpFromPowerKey(long eventTime) {
        wakeUpFromPowerKey(eventTime);
    }

    class OppoPhoneWindowManagerInner implements IOppoPhoneWindowManagerInner {
        OppoPhoneWindowManagerInner() {
        }

        @Override // com.android.server.policy.IOppoPhoneWindowManagerInner
        public void cancelPreloadRecentApps() {
            PhoneWindowManager.this.cancelPreloadRecentApps();
        }

        @Override // com.android.server.policy.IOppoPhoneWindowManagerInner
        public Object getLock() {
            return PhoneWindowManager.this.mLock;
        }

        @Override // com.android.server.policy.IOppoPhoneWindowManagerInner
        public void powerPress(long eventTime, boolean interactive, int count) {
            PhoneWindowManager.this.powerPress(eventTime, interactive, count);
        }

        @Override // com.android.server.policy.IOppoPhoneWindowManagerInner
        public void cancelPendingPowerKeyAction() {
            PhoneWindowManager.this.cancelPendingPowerKeyAction();
        }

        @Override // com.android.server.policy.IOppoPhoneWindowManagerInner
        public void launchAssistAction(String hint, int deviceId, OppoBasePhoneWindowManager.AssistManagerLaunchMode launchMode) {
            PhoneWindowManager.this.launchAssistAction(hint, deviceId, launchMode);
        }

        @Override // com.android.server.policy.IOppoPhoneWindowManagerInner
        public void wakeUpFromPowerKey(long eventTime) {
            PhoneWindowManager.this.wakeUpFromPowerKey(eventTime);
        }
    }
}
