package com.android.server.policy;

import android.app.ActivityManagerNative;
import android.app.ColorStatusBarManager;
import android.app.UiModeManager;
import android.common.OppoFeatureCache;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.IContentProvider;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.SystemSensorManager;
import android.media.AudioManager;
import android.media.AudioSystem;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.os.UserHandle;
import android.os.Vibrator;
import android.provider.Settings;
import android.telecom.TelecomManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.util.OppoSafeDbReader;
import android.util.Slog;
import android.view.ColorBaseLayoutParams;
import android.view.IWindowManager;
import android.view.KeyEvent;
import android.view.OppoScreenDragUtil;
import android.view.OppoScreenShotUtil;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;
import com.android.internal.graphics.ColorUtils;
import com.android.internal.policy.IKeyguardDismissCallback;
import com.android.internal.policy.PhoneWindow;
import com.android.internal.statusbar.IStatusBarService;
import com.android.internal.util.ScreenshotHelper;
import com.android.server.LocalServices;
import com.android.server.am.ColorHansRestriction;
import com.android.server.am.ColorKeyEventUtil;
import com.android.server.am.ColorKeyLayoutManagerUtil;
import com.android.server.am.ColorMultiAppManagerService;
import com.android.server.am.IColorGameSpaceManager;
import com.android.server.coloros.OppoListManager;
import com.android.server.coloros.OppoSysStateManager;
import com.android.server.display.OppoDisplayManagerInternal;
import com.android.server.display.ai.utils.BrightnessConstants;
import com.android.server.display.ai.utils.ColorAILog;
import com.android.server.policy.OppoBasePhoneWindowManager;
import com.android.server.policy.WindowManagerPolicy;
import com.android.server.wm.ColorAppSwitchManager;
import com.android.server.wm.ColorInputMethodKeyboardPositionManager;
import com.android.server.wm.DisplayPolicy;
import com.android.server.wm.IColorStartingWindowManager;
import com.android.server.wm.OppoBaseDisplayPolicy;
import com.android.server.wm.OppoDisplayPolicy;
import com.android.server.wm.startingwindow.ColorStartingWindowRUSHelper;
import com.android.server.wm.startingwindow.ColorStartingWindowUtils;
import com.color.app.ColorBootMessageDialog;
import com.color.util.ColorTypeCastingHelper;
import com.oppo.os.LinearmotorVibrator;
import com.oppo.os.WaveformEffect;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class OppoPhoneWindowManager extends PhoneWindowManager {
    private static final String ACTION_BOOT_IPO = "android.intent.action.ACTION_BOOT_IPO";
    private static final String ACTION_DISABLE_BOTTOM_KEY_MODE = "com.oppo.intent.action.DISABLE_BOTTOM_KEY_MODE";
    private static final String ACTION_END_CALL = "android.intent.action.END_CALL";
    private static final String ACTION_KEY_LOCK = "com.oppo.intent.action.KEY_LOCK_MODE";
    private static final String ACTION_POWER_BUTTON_ENDS_ALARMCLOCK = "oppo.intent.action.POWER_BUTTON_ENDS_ALARMCLOCK";
    private static final String ACTION_SCREEN_SHOT = "oppo.intent.action.SCREEN_SHOT";
    private static final String ACTION_SHUTDOWN_IPO = "android.intent.action.ACTION_SHUTDOWN_IPO";
    private static final int ARGS_LENGTH = 3;
    private static final long BOTTOM_KEY_CLICK_INTERVAL = 2500;
    private static final String BREENO_DISABLE_KEY = "breeno_disable_key";
    private static final String CAMERA_LAUNCH_TYPE = "android_camera_launch_type";
    private static final String CAMERA_PKG = "com.oppo.camera";
    private static final String CAPTURE_GUIDE = "com.oppo.gestureguide.activity.CaptureActivity";
    private static final String CTS_PROJECTION_TOUCH = "com.android.cts.verifier.projection.touch.ProjectionTouchActivity";
    private static final long DEFALUT_LONG_VIBRATION_DURING_TIME = 300;
    private static final long DEFALUT_TOUCH_FEEDBACK_10 = 10;
    private static final long DEFALUT_TOUCH_FEEDBACK_20 = 20;
    private static final long DEFALUT_TOUCH_FEEDBACK_30 = 30;
    private static final long DEFAULT_LONG_PRESS_TIME_FOR_SPEECH = 500;
    private static final int DELAY_REG_PHONESTATE = 500;
    private static final String DISABLE_GOOGLE_ASSSIST_POWER_WAKEUP = "disable_google_asssist_power_wakeup";
    private static final int DURATION_START_SPEECH = 750;
    private static final int DURATION_START_SPLIT_SCREEN = 750;
    private static final long FADE_OFF_ANIMATION_DURATION = 400;
    private static final String FEATURE_CHILDREN_MODE = "oppo.childspace.support";
    private static final String FULL_WINDOW = "fullscreen";
    private static final String GET_PID_TAG = "get_pid";
    private static final String GIMBAL_LAUNCH_CAMERA = "gimbal_launch_from_framework";
    private static boolean INDIA_SOS = false;
    private static final int KEY_LOCK_MODE_BACK_HOME = 4;
    private static final int KEY_LOCK_MODE_HOME = 3;
    private static final int KEY_LOCK_MODE_NORMAL = 0;
    private static final int KEY_LOCK_MODE_POWER = 1;
    private static final int KEY_LOCK_MODE_POWER_HOME = 2;
    private static final String KEY_OPEN_REQUEST_IS_LOCKSCREEN = "OPEN_REQUEST_IS_LOCKSCREEN";
    private static final String KEY_OPEN_REQUEST_PACKAGENAME = "OPEN_REQUEST_PACKAGENAME";
    private static final Uri KEY_OPEN_WALLET_CP_URI = Uri.parse("content://finshell.wallet.quickstart.flag.provider.open/CARD_BAG_FLAG");
    private static final String LAYER_WALLPAPER = "LAYER_WALLPAPER";
    private static final long LID_WAKELOCK_TIMEOUT = 2000;
    private static final int MAX_COUNT_REG_PHONESTATE = 40;
    private static final int MAX_WAIT_TIME = 1000;
    private static final int MSG_ISCAMERAMODE = 17;
    private static final int MSG_ISHOMEMODE = 16;
    private static final int MSG_ISINGESTUREGUIDE = 19;
    private static final long MSG_ISINGESTUREGUIDE_AND_ISCAMERAMODE = 50;
    private static final int MSG_POWER_KEY_SLEEP_DELAY = 1020;
    private static final int MSG_POWER_LONG_PRESS_FOR_SPEECH = 1011;
    private static final int MSG_SET_WALLPAPER_LAYER = 18;
    private static final int MSG_WALLET_CHECK = 1015;
    private static final String MTK_DEVICE_NAME = "mtk-kpd";
    private static final String MULTI_TOUCH_GUIDE = "com.oppo.gestureguide.activity.StartMultiTouchOpenCamera";
    private static final String OPPO_ENTERPRISE_DEVELOPMENT_FEATURE = "oppo.business.custom";
    private static final String OPPO_IGNORE_DRIVE_MODE = "com.oppo.drivemode";
    private static final String OPPO_INCALL_SCREEN = "com.android.incallui/com.android.incallui.OppoInCallActivity";
    private static final String OPPO_SETTINGS_FORBID_GLOBALACTION = "forbid_globalaction_by_power";
    private static final String PERMISSION_OPPO_COMPONENT_SAFE = "oppo.permission.OPPO_COMPONENT_SAFE";
    private static final int PERSONAL_SOURCE_FROM_HOME = 2;
    private static final int PERSONAL_SOURCE_FROM_POWER_VOLUME = 1;
    private static final long POWER_KEY_SLEEP_DELAY = 220;
    private static final float PROXIMITY_THRESHOLD = 2.0f;
    private static final String QCOM_DEVICE_NAME = "gpio-keys";
    private static final String QUICK_LAUNCH_CAMERA = "quick_launch_from_framework";
    private static final String QUICK_LAUNCH_CAMERA_KEY = "com.oppo.camera quick launch";
    private static final int QUICK_LAUNCH_CAMERA_OFF = 0;
    private static final int QUICK_LAUNCH_CAMERA_ON = 1;
    private static final int QUICK_LAUNCH_CAMERA_WAKE_DELAY = 350;
    private static final long QUICK_SHOT_DELAY_MILLIS_SCREEN_OFF = 300;
    private static final long QUICK_SHOT_DELAY_MILLIS_SCREEN_ON = 200;
    private static final String SETTING_CHILDREN_MODE = "children_mode_on";
    private static final String SHORTCUT_WINDOW = "ShortcutsPanel";
    private static final int SOS_DISABLED = 0;
    private static final int SOS_ENABLED = 1;
    private static final String SOS_LICENSE = "oppo_comm_soshelper_license";
    private static final int SOS_MAX_LAUNCH_WAIT_COUNT = 3;
    private static int SOS_POWER_KEY_DOWN_COUNT = 5;
    private static long SOS_POWER_KEY_DOWN_INTERVAL = BOTTOM_KEY_CLICK_INTERVAL;
    private static final long SOS_POWER_KEY_MAX_INTERVAL = 500;
    private static final String SOURCE_REQUEST_FRAMWORK_QUICKSTART = "request_pkg_framework_quick_start";
    private static final String SPECIAL_PKG = "com.taiwanmobile.wali";
    public static final String SPEECH_ACTION_NAME = "heytap.intent.action.ACTIVATE_SPEECH_ASSIST";
    public static final String SPEECH_PACKAGE_NAME = "com.heytap.speechassist";
    private static final String SPEECH_POWER_KEY_ENABLED = "speech_assist_power_wakeup";
    public static final String SPEECH_SERVICE_NAME = "com.heytap.speechassist.core.SpeechService";
    public static final int SPEECH_START_TYPE_VALUE = 1024;
    public static final String START_SPEECH_DISABLE = "com.oppo.intent.action.START_SPEECH_DISABLE";
    public static final String START_SPEECH_ENABLE = "com.oppo.intent.action.START_SPEECH_ENABLE";
    private static final String STR_INCALL_POWER_BUTTON_HANGUP = "oppo_comm_phone_power_button_ends_call";
    private static final String STR_POWER_BUTTON_ENDS_ALARMCLOCK = "oppo_power_button_ends_alarm_clock";
    private static final String STR_UP_GESTURE_MODE = "hide_navigationbar_enable";
    private static final String SUPER_POWERSAVE_MODE_STATE = "super_powersave_mode_state";
    private static final String TAG = "OppoPhoneWindowManager";
    private static final Uri URI_SETTINGS_UP_GESTURE_MODE = Settings.Secure.getUriFor(STR_UP_GESTURE_MODE);
    private static final String USE_POWER_BTN = "oppo_comm_soshelper_use_power_btn";
    private static final int VALUE_DISABLE_GOOGLE_SPEECH_BY_POWER = 1;
    private static final int VALUE_ENABLE_GOOGLE_SPEECH_BY_POWER = 0;
    private static final int VALUE_SPEECH_DISABLE = 0;
    private static final int VALUE_SPEECH_ENABLE = 1;
    private static final int VOLUME_MAX_DELAY_TIME = 500;
    private static final long VOLUME_UP_DELAY_ONCE = 50;
    private static final long VOLUME_UP_DOUBLE_CLIICK_DELAY = 300;
    private static final long VOLUME_UP_LONG_PRESS_DELAY = 300;
    public static final String WALLET_ACTION_NAME = "finshell.wallet.intent.action.OPEN";
    private static long WALLET_INIT_TIMEOUT = 15000;
    public static final String WALLET_PACKAGE_NAME = "com.finshell.wallet";
    private static long WALLET_POWER_KEY_CHECK_AFTER_UP = QUICK_SHOT_DELAY_MILLIS_SCREEN_ON;
    private static long WALLET_POWER_KEY_MAX_TIME = 300;
    private static final int WINDOW_LAYER_10 = 10;
    private static final int WINDOW_LAYER_100 = 100;
    private static final int WINDOW_LAYER_101 = 101;
    private static final int WINDOW_LAYER_102 = 102;
    private static final int WINDOW_LAYER_103 = 103;
    private static final int WINDOW_LAYER_104 = 104;
    private static final int WINDOW_LAYER_105 = 105;
    private static final int WINDOW_LAYER_106 = 106;
    private static final int WINDOW_LAYER_107 = 107;
    private static final int WINDOW_LAYER_108 = 108;
    private static final int WINDOW_LAYER_109 = 109;
    private static final int WINDOW_LAYER_110 = 110;
    private static final int WINDOW_LAYER_111 = 111;
    private static final int WINDOW_LAYER_112 = 112;
    private static final int WINDOW_LAYER_113 = 113;
    private static final int WINDOW_LAYER_114 = 114;
    private static final int WINDOW_LAYER_115 = 115;
    private static final int WINDOW_LAYER_116 = 116;
    private static final int WINDOW_LAYER_117 = 117;
    private static final int WINDOW_LAYER_14 = 14;
    private static final int WINDOW_LAYER_17 = 16;
    private static final int WINDOW_LAYER_2 = 2;
    private static final int WINDOW_LAYER_3 = 3;
    private static final int WINDOW_LAYER_55 = 55;
    private static final int mKeycodeRecentTask = 187;
    private final String EXTRA_ASSIST_MANAGER_LAUNCH_MODE = "assistant_launch_mode";
    private final int MAX_POWER_KEY_COUNT_OPPO_DEFINED = Math.max(2, SOS_POWER_KEY_DOWN_COUNT);
    private final int WALLET_POWER_KEY_DOWN_COUNT = 2;
    private AudioManager mAudioManager;
    private boolean mBackKeyUpIgnore;
    private BroadcastReceiver mBootShutdownReceiver = new BroadcastReceiver() {
        /* class com.android.server.policy.OppoPhoneWindowManager.AnonymousClass3 */

        public void onReceive(Context context, Intent intent) {
            intent.getAction();
        }
    };
    private volatile boolean mClearPowerKeyDownRecord = false;
    private int mCntPhoneStateReg;
    private ColorPowerKeyHandler mColorPowerKeyHandler = null;
    private long mConsumedVolumeUpDownTime = 0;
    private Context mContext;
    private volatile int mCurrentKeyMode = 0;
    private int mCurrentUserId;
    private int mDisableBottomKeyMode = 0;
    private DisableBottomKeyModeReceiver mDisableBottomKeyModeReceiver = new DisableBottomKeyModeReceiver();
    private int mFirstUserLocked = 3;
    WindowManagerPolicy.WindowState mFocusedWindow = null;
    private long mForceResumForChangingTheme = ColorAppSwitchManager.INTERVAL;
    private OppoScreenOffGestureManager mGestureManager = null;
    private boolean mGotSwitchError = true;
    boolean mHideNavigationBar = false;
    private Toast mHitToast = null;
    private boolean mHomeConsumed;
    private final Runnable mHomeKeyLongPress = new Runnable() {
        /* class com.android.server.policy.OppoPhoneWindowManager.AnonymousClass12 */

        public void run() {
            OppoPhoneWindowManager.this.mHomeConsumed = true;
            OppoPhoneWindowManager.this.startPersonalAssistant(2);
        }
    };
    private boolean mIgnoreHomeAndMenu = false;
    private int mIncallPowerButtonHangup;
    private boolean mIsAnimatingKeyDown = false;
    private boolean mIsBusinessCustom = false;
    private boolean mIsCameraShow = false;
    private boolean mIsCustomBackDownIgnored = false;
    private boolean mIsCustomHomeDownIgnored = false;
    private boolean mIsCustomTaskDownIgnored = false;
    private boolean mIsExpVersion = false;
    private boolean mIsHomeKeyDown;
    private boolean mIsInGestureGuide = false;
    private boolean mIsMenuKeyDown;
    public AtomicBoolean mIsMute = new AtomicBoolean(false);
    private boolean mIsUpGestureMode = false;
    private ContentObserver mIsUpGestureModeSettingObserver = new ContentObserver(this.mHandler) {
        /* class com.android.server.policy.OppoPhoneWindowManager.AnonymousClass10 */

        public void onChange(boolean selfChange, Uri uri) {
            OppoPhoneWindowManager oppoPhoneWindowManager = OppoPhoneWindowManager.this;
            boolean z = false;
            if (Settings.Secure.getInt(oppoPhoneWindowManager.mContext.getContentResolver(), OppoPhoneWindowManager.STR_UP_GESTURE_MODE, 0) == 2) {
                z = true;
            }
            oppoPhoneWindowManager.mIsUpGestureMode = z;
        }
    };
    private boolean mIsVolumeUpKeyUp = false;
    private boolean mIsVolumeUpLongPressed = false;
    private final ArrayList<String> mKeyLockIntentProcess = new ArrayList<>();
    private KeyLockModeReceiver mKeyLockModeReceiver = new KeyLockModeReceiver();
    private int mLastAction = -1;
    private int mLastDownKeyCode = -1;
    private long mLastDownKeyTime;
    private long mLastVolumeDownTime;
    private long mLastVolumeUpTime;
    private final Map<Integer, Integer> mLaunchCameraSettingMap = new ArrayMap();
    private final ContentObserver mLaunchCameraSettingObserver = new ContentObserver(this.mHandler) {
        /* class com.android.server.policy.OppoPhoneWindowManager.AnonymousClass1 */

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange, Uri uri, int userId) {
            synchronized (OppoPhoneWindowManager.this.mLaunchCameraSettingMap) {
                if (OppoPhoneWindowManager.this.mOppoDebug) {
                    Log.d(OppoPhoneWindowManager.TAG, "quick launch camera setting changed, current setting: " + OppoPhoneWindowManager.this.mLaunchCameraSettingMap.getOrDefault(Integer.valueOf(userId), -1) + ", userId: " + userId);
                }
                OppoPhoneWindowManager.this.mLaunchCameraSettingMap.put(Integer.valueOf(userId), Integer.valueOf(Settings.Secure.getIntForUser(OppoPhoneWindowManager.this.mContext.getContentResolver(), OppoPhoneWindowManager.QUICK_LAUNCH_CAMERA_KEY, 0, userId)));
            }
        }
    };
    private volatile int mLaunchSosWaitCount = -1;
    private long mLaunchSosWaitStartTime = 0;
    private LinearmotorVibrator mLinearmotorVibrator = null;
    private boolean mMenuKeyUpIgnore;
    private Handler mMyHandler = new Handler() {
        /* class com.android.server.policy.OppoPhoneWindowManager.AnonymousClass2 */

        public void handleMessage(Message msg) {
            boolean isInGestureGuide = false;
            switch (msg.what) {
                case ColorHansRestriction.HANS_RESTRICTION_BLOCK_ALARM /* 16 */:
                    boolean isHomeOrLock = msg.arg1 == 1;
                    if (msg.arg2 == 1) {
                        isInGestureGuide = true;
                    }
                    OppoPhoneWindowManager.this.sendIsHomeModeIntent(isHomeOrLock, isInGestureGuide);
                    return;
                case OppoPhoneWindowManager.MSG_ISCAMERAMODE /* 17 */:
                    if (msg.arg1 == 1) {
                        isInGestureGuide = true;
                    }
                    OppoPhoneWindowManager.this.sendIsCameraModeIntent(isInGestureGuide);
                    return;
                case OppoPhoneWindowManager.MSG_SET_WALLPAPER_LAYER /* 18 */:
                default:
                    return;
                case OppoPhoneWindowManager.MSG_ISINGESTUREGUIDE /* 19 */:
                    if (msg.arg1 == 1) {
                        isInGestureGuide = true;
                    }
                    OppoPhoneWindowManager.this.sendIsInGestureGuideIntent(isInGestureGuide);
                    return;
            }
        }
    };
    PowerManager.WakeLock mNotifyLidWakeLock;
    private Object mObject = new Object();
    private OppoAppFrozen mOppoAppFrozen;
    BroadcastReceiver mOppoBaseReceiver = new BroadcastReceiver() {
        /* class com.android.server.policy.OppoPhoneWindowManager.AnonymousClass4 */

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (OppoPhoneWindowManager.ACTION_END_CALL.equals(action)) {
                OppoPhoneWindowManager.this.mIsMute.set(false);
                if (OppoPhoneWindowManager.this.mOppoDebug) {
                    Log.d(OppoPhoneWindowManager.TAG, "ACTION_END_CALL");
                }
            } else if (OppoPhoneWindowManager.START_SPEECH_ENABLE.equals(action)) {
                if (OppoPhoneWindowManager.this.mOppoDebug) {
                    Log.i(OppoPhoneWindowManager.TAG, "receive START_SPEECH_ENABLE");
                }
                OppoPhoneWindowManager.this.mStartSpeechEnabled = true;
            } else if (OppoPhoneWindowManager.START_SPEECH_DISABLE.equals(action)) {
                if (OppoPhoneWindowManager.this.mOppoDebug) {
                    Log.i(OppoPhoneWindowManager.TAG, "receive START_SPEECH_DISABLE");
                }
                OppoPhoneWindowManager.this.mStartSpeechEnabled = false;
            } else if (OppoPhoneWindowManager.ACTION_SCREEN_SHOT.equals(action)) {
                if (OppoPhoneWindowManager.this.mOppoDebug) {
                    Log.i(OppoPhoneWindowManager.TAG, "ACTION_SCREEN_SHOT");
                }
                if (OppoPhoneWindowManager.this.mDefaultDisplayPolicy instanceof OppoDisplayPolicy) {
                    OppoPhoneWindowManager.this.mDefaultDisplayPolicy.takeScreenshot(intent);
                }
            }
        }
    };
    private boolean mOppoDebug = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, false);
    private OppoDisplayManagerInternal mOppoDisplayManagerInternal;
    boolean mOppoEarlyPowerKeyHandle = false;
    private OppoSafeDbReader mOppoSafeDbReader;
    private boolean mPauseForChangingTheme = false;
    private boolean mPhoneStateRegSucess = false;
    PhoneStateListener mPhoneStatelistener = new PhoneStateListener() {
        /* class com.android.server.policy.OppoPhoneWindowManager.AnonymousClass15 */

        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            if (!OppoPhoneWindowManager.this.mPhoneStateRegSucess) {
                OppoPhoneWindowManager.this.mPhoneStateRegSucess = true;
                OppoPhoneWindowManager.this.mHandler.removeCallbacks(OppoPhoneWindowManager.this.mRunPhoneStateRegister);
                Log.d(OppoPhoneWindowManager.TAG, "onCallStateChanged: set mPhoneStateRegSucess true");
            }
            if (state == 0) {
                OppoPhoneWindowManager.this.mIsMute.set(false);
                if (OppoPhoneWindowManager.this.mOppoDebug) {
                    Log.d(OppoPhoneWindowManager.TAG, "onCallStateChanged: CALL_STATE_IDLE!");
                }
            } else if (state != 1) {
                if (state != 2) {
                }
            } else {
                OppoPhoneWindowManager.this.mIsMute.set(false);
                if (OppoPhoneWindowManager.this.mOppoDebug) {
                    Log.d(OppoPhoneWindowManager.TAG, "onCallStateChanged: CALL_STATE_RINGING!");
                }
            }
        }
    };
    private int mPowerButtonEndsAlarmclock;
    private boolean mPowerConsumedByWakeup = false;
    private ArrayList<Long> mPowerKeyDownTimes = new ArrayList<>();
    private volatile boolean mPowerKeyForbidden = false;
    SensorEventListener mProximityListener = new SensorEventListener() {
        /* class com.android.server.policy.OppoPhoneWindowManager.AnonymousClass14 */

        public void onSensorChanged(SensorEvent event) {
            boolean z = false;
            if (OppoPhoneWindowManager.this.mOppoDebug) {
                Log.d(OppoPhoneWindowManager.TAG, "mProximityListener.onSensorChanged values[0]:" + event.values[0]);
            }
            synchronized (OppoPhoneWindowManager.this.mObject) {
                float distance = event.values[0];
                OppoPhoneWindowManager oppoPhoneWindowManager = OppoPhoneWindowManager.this;
                if (((double) distance) >= 0.0d && distance < OppoPhoneWindowManager.PROXIMITY_THRESHOLD && distance < OppoPhoneWindowManager.this.mProximitySensor.getMaximumRange()) {
                    z = true;
                }
                oppoPhoneWindowManager.mProximitySensorActive = z;
                if (OppoPhoneWindowManager.this.mOppoDebug) {
                    Log.d(OppoPhoneWindowManager.TAG, "mProximityListener.onSensorChanged active: " + OppoPhoneWindowManager.this.mProximitySensorActive);
                }
                OppoPhoneWindowManager.this.mObject.notifyAll();
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    private Sensor mProximitySensor;
    private boolean mProximitySensorActive = true;
    private boolean mProximitySensorEnabled = false;
    private BroadcastReceiver mReceiverBootComplete = new BroadcastReceiver() {
        /* class com.android.server.policy.OppoPhoneWindowManager.AnonymousClass5 */

        public void onReceive(Context context, Intent intent) {
            if (BrightnessConstants.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
                OppoPhoneWindowManager.this.mHandler.postDelayed(OppoPhoneWindowManager.this.mRunPhoneStateRegister, 500);
                TelephonyManager.from(OppoPhoneWindowManager.this.mContext).listen(OppoPhoneWindowManager.this.mPhoneStatelistener, 32);
                Slog.d(OppoPhoneWindowManager.TAG, "ACTION_BOOT_COMPLETED: telephoneManager.listen.");
                if ("file".equals(SystemProperties.get("ro.crypto.type"))) {
                    OppoPhoneWindowManager oppoPhoneWindowManager = OppoPhoneWindowManager.this;
                    oppoPhoneWindowManager.mOppoSafeDbReader = OppoSafeDbReader.getInstance(oppoPhoneWindowManager.mContext);
                    OppoPhoneWindowManager.this.mOppoSafeDbReader.startThread();
                }
            }
        }
    };
    private AtomicBoolean mRecentsLongPressDetected = new AtomicBoolean(false);
    private final Runnable mRecentsStartSplitSreen = new Runnable() {
        /* class com.android.server.policy.OppoPhoneWindowManager.AnonymousClass23 */

        public void run() {
            if (!OppoPhoneWindowManager.this.keyguardOn()) {
                OppoPhoneWindowManager.this.mRecentsLongPressDetected.set(true);
                if (!OppoPhoneWindowManager.this.stopLockTaskMode()) {
                    OppoPhoneWindowManager.this.toggleSplitScreen();
                }
            }
        }
    };
    Runnable mResumeForChangingTheme = new Runnable() {
        /* class com.android.server.policy.OppoPhoneWindowManager.AnonymousClass11 */

        public void run() {
            if (OppoPhoneWindowManager.this.mPauseForChangingTheme) {
                OppoPhoneWindowManager.this.enableScreenAfterBoot();
                OppoPhoneWindowManager.this.mPauseForChangingTheme = false;
            }
            Intent intent = new Intent(OppoPhoneWindowManager.ACTION_KEY_LOCK);
            intent.putExtra("KeyLockMode", 0);
            intent.putExtra("ProcessName", "com.android.individuationSettings:individuationSetting");
            OppoPhoneWindowManager.this.mContext.sendBroadcast(intent);
        }
    };
    private final Runnable mRunPhoneStateRegister = new Runnable() {
        /* class com.android.server.policy.OppoPhoneWindowManager.AnonymousClass6 */

        public void run() {
            Slog.d(OppoPhoneWindowManager.TAG, "Runnable: mPhoneStateRegSucess = " + OppoPhoneWindowManager.this.mPhoneStateRegSucess);
            if (!OppoPhoneWindowManager.this.mPhoneStateRegSucess) {
                if (OppoPhoneWindowManager.access$908(OppoPhoneWindowManager.this) < OppoPhoneWindowManager.MAX_COUNT_REG_PHONESTATE) {
                    OppoPhoneWindowManager.this.mHandler.postDelayed(OppoPhoneWindowManager.this.mRunPhoneStateRegister, 500);
                }
                TelephonyManager.from(OppoPhoneWindowManager.this.mContext).listen(OppoPhoneWindowManager.this.mPhoneStatelistener, 32);
                Slog.d(OppoPhoneWindowManager.TAG, "Runnable: telephoneManager.listen. mCntPhoneStateReg=" + OppoPhoneWindowManager.this.mCntPhoneStateReg);
            }
        }
    };
    private int mSameBottomKeyClickCount = 0;
    ScreenshotHelper mScreenshotHelper;
    private SensorManager mSensorManager;
    private volatile boolean mSosPowerKeyEnabled = false;
    private volatile int mSpeechEnabled = -1;
    private volatile boolean mSpeechLongPressHandled = false;
    private boolean mStartSpeechEnabled = false;
    private Drawable mStartingWindowDrawable;
    private boolean mSuperPowerSaveMode = false;
    private boolean mSupportNfc = false;
    private boolean mSupportVolumeUpDoubleClick = false;
    private boolean mSupportVolumeUpLongPress = false;
    private Binder mToken = new Binder();
    BroadcastReceiver mUnlockReceiver = new BroadcastReceiver() {
        /* class com.android.server.policy.OppoPhoneWindowManager.AnonymousClass21 */

        public void onReceive(Context context, Intent intent) {
            OppoPhoneWindowManager.this.mColorPowerKeyHandler.post(OppoPhoneWindowManager.this.mUserUnlockRunnable);
        }
    };
    Runnable mUserUnlockRunnable = new Runnable() {
        /* class com.android.server.policy.OppoPhoneWindowManager.AnonymousClass22 */

        public void run() {
            boolean oldValue = OppoPhoneWindowManager.this.mWalletPowerKeyEnabled;
            OppoPhoneWindowManager oppoPhoneWindowManager = OppoPhoneWindowManager.this;
            oppoPhoneWindowManager.mWalletPowerKeyEnabled = oppoPhoneWindowManager.checkSupportFlag(oppoPhoneWindowManager.mContext);
            if (OppoPhoneWindowManager.this.mGotSwitchError) {
                OppoPhoneWindowManager.access$3310(OppoPhoneWindowManager.this);
                if (OppoPhoneWindowManager.this.mFirstUserLocked == 0 && OppoPhoneWindowManager.this.mUnlockReceiver != null) {
                    OppoPhoneWindowManager.this.mContext.unregisterReceiver(OppoPhoneWindowManager.this.mUnlockReceiver);
                    OppoPhoneWindowManager.this.mUnlockReceiver = null;
                    return;
                }
                return;
            }
            if (OppoPhoneWindowManager.this.mUnlockReceiver != null) {
                OppoPhoneWindowManager.this.mContext.unregisterReceiver(OppoPhoneWindowManager.this.mUnlockReceiver);
                OppoPhoneWindowManager.this.mUnlockReceiver = null;
            }
            if (oldValue != OppoPhoneWindowManager.this.mWalletPowerKeyEnabled) {
                if (!oldValue) {
                    OppoPhoneWindowManager.this.updateFadeOffDuration(OppoPhoneWindowManager.FADE_OFF_ANIMATION_DURATION);
                } else {
                    OppoPhoneWindowManager.this.updateFadeOffDuration(-1);
                }
            }
            try {
                OppoPhoneWindowManager.this.mContext.getContentResolver().registerContentObserver(OppoPhoneWindowManager.KEY_OPEN_WALLET_CP_URI, true, new WalletContentObserver(OppoPhoneWindowManager.this.mColorPowerKeyHandler));
                Slog.d(OppoPhoneWindowManager.TAG, "register content observer for wallet");
            } catch (Throwable th) {
                Slog.d(OppoPhoneWindowManager.TAG, "cat not register content observer for wallet");
            }
        }
    };
    private Vibrator mVibrator = null;
    private boolean mVolumeUpDownDispatched;
    private long mVolumeUpDownTime = 0;
    private boolean mVolumeUpDownqueueed;
    private final Runnable mVolumeUpLongPress = new Runnable() {
        /* class com.android.server.policy.OppoPhoneWindowManager.AnonymousClass18 */

        public void run() {
            OppoPhoneWindowManager.this.sendIntercomAcquireRelaseUp(true);
            OppoPhoneWindowManager.this.mIsVolumeUpLongPressed = true;
        }
    };
    private PowerManager.WakeLock mWakeLockVolumeUpLongPress;
    private volatile boolean mWalletPowerKeyEnabled = false;
    private int mWallpaperLayer = -1;

    static /* synthetic */ int access$3310(OppoPhoneWindowManager x0) {
        int i = x0.mFirstUserLocked;
        x0.mFirstUserLocked = i - 1;
        return i;
    }

    static /* synthetic */ int access$908(OppoPhoneWindowManager x0) {
        int i = x0.mCntPhoneStateReg;
        x0.mCntPhoneStateReg = i + 1;
        return i;
    }

    private static boolean isExpROM() {
        return !SystemProperties.get("persist.sys.oppo.region", "CN").equalsIgnoreCase("CN");
    }

    public void init(Context context, IWindowManager windowManager, WindowManagerPolicy.WindowManagerFuncs windowManagerFuncs) {
        this.mContext = context;
        OppoPhoneWindowManager.super.init(context, windowManager, windowManagerFuncs);
        this.mOppoDisplayManagerInternal = (OppoDisplayManagerInternal) LocalServices.getService(OppoDisplayManagerInternal.class);
        IntentFilter keyModeFilter = new IntentFilter();
        keyModeFilter.addAction(ACTION_KEY_LOCK);
        context.registerReceiverAsUser(this.mKeyLockModeReceiver, UserHandle.ALL, keyModeFilter, "oppo.permission.OPPO_COMPONENT_SAFE", null);
        IntentFilter disableBottomKeyModeFilter = new IntentFilter();
        disableBottomKeyModeFilter.addAction(ACTION_DISABLE_BOTTOM_KEY_MODE);
        context.registerReceiver(this.mDisableBottomKeyModeReceiver, disableBottomKeyModeFilter, "oppo.permission.OPPO_COMPONENT_SAFE", null);
        context.getContentResolver().registerContentObserver(URI_SETTINGS_UP_GESTURE_MODE, false, this.mIsUpGestureModeSettingObserver);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_END_CALL);
        filter.addAction(START_SPEECH_ENABLE);
        filter.addAction(START_SPEECH_DISABLE);
        filter.addAction(ACTION_SCREEN_SHOT);
        context.registerReceiverAsUser(this.mOppoBaseReceiver, UserHandle.ALL, filter, "oppo.permission.OPPO_COMPONENT_SAFE", null);
        IntentFilter f = new IntentFilter();
        f.addAction(ACTION_BOOT_IPO);
        f.addAction(ACTION_SHUTDOWN_IPO);
        f.addAction("android.intent.action.ACTION_SHUTDOWN");
        context.registerReceiver(this.mBootShutdownReceiver, f);
        this.mGestureManager = new OppoScreenOffGestureManager(this.mContext, this.mHandler, this.mKeyguardDelegate, this.mBroadcastWakeLock);
        OppoScreenDragUtil.resetState();
        this.mNotifyLidWakeLock = this.mPowerManager.newWakeLock(1, "notifyLidSwitchWakeLock");
        this.mOppoAppFrozen = new OppoAppFrozen(context, this);
        if (!"file".equals(SystemProperties.get("ro.crypto.type"))) {
            this.mOppoSafeDbReader = OppoSafeDbReader.getInstance(this.mContext);
            this.mOppoSafeDbReader.startThread();
        }
        IntentFilter fBootComplete = new IntentFilter();
        fBootComplete.addAction(BrightnessConstants.ACTION_BOOT_COMPLETED);
        fBootComplete.setPriority(1000);
        this.mContext.registerReceiver(this.mReceiverBootComplete, fBootComplete);
        registerSettingsForOppoLocked(context.getContentResolver(), this.mSettingsObserver);
        this.mColorPowerKeyHandler = new ColorPowerKeyHandler(this.mHandler.getLooper());
        context.getContentResolver().registerContentObserver(Settings.Global.getUriFor(STR_INCALL_POWER_BUTTON_HANGUP), false, this.mSettingsObserver, -1);
        context.getContentResolver().registerContentObserver(Settings.Secure.getUriFor(STR_POWER_BUTTON_ENDS_ALARMCLOCK), false, this.mSettingsObserver, -1);
        if (this.mContext.getPackageManager().hasSystemFeature("coloros.customize.phone.voluemup.doubleclick")) {
            this.mSupportVolumeUpDoubleClick = true;
        }
        if (this.mContext.getPackageManager().hasSystemFeature("coloros.customize.phone.voluemup.longpress")) {
            this.mSupportVolumeUpLongPress = true;
        }
        this.mWakeLockVolumeUpLongPress = this.mPowerManager.newWakeLock(1, "VolumeUpLongPress");
        Log.d(TAG, "supportVolumeUpDoubleClick = " + this.mSupportVolumeUpDoubleClick + "supportVolumeUpLongPress = " + this.mSupportVolumeUpLongPress);
        context.getContentResolver().registerContentObserver(Settings.Secure.getUriFor(ColorInputMethodKeyboardPositionManager.KEYBOARD_POSITION), false, this.mSettingsObserver, -1);
        this.mScreenshotHelper = new ScreenshotHelper(this.mContext);
        context.getContentResolver().registerContentObserver(Settings.Secure.getUriFor(QUICK_LAUNCH_CAMERA_KEY), false, this.mLaunchCameraSettingObserver, -1);
        this.mAudioManager = (AudioManager) this.mContext.getSystemService("audio");
        this.mIsBusinessCustom = this.mContext.getPackageManager().hasSystemFeature(OPPO_ENTERPRISE_DEVELOPMENT_FEATURE);
    }

    public int interceptKeyBeforeQueueing(KeyEvent event, int policyFlags) {
        int i;
        int launchCameraSetting;
        String deviceName;
        if (!this.mSystemBooted) {
            return 0;
        }
        ColorKeyEventUtil.getInstance().onKeyEvent(event);
        boolean interactive = (536870912 & policyFlags) != 0;
        boolean down = event.getAction() == 0;
        int keyCode = event.getKeyCode();
        if (customIgnoreHomeTaskKey(keyCode, down)) {
            if (this.mOppoDebug) {
                Log.d(TAG, "interceptKeyBeforeQueueing: custom ignore " + event);
            }
            return 0;
        } else if (4 == this.mCurrentKeyMode && 4 == keyCode) {
            if (this.mOppoDebug) {
                Log.d(TAG, "interceptKeyBeforeQueueing: keylockmode ignore " + event);
            }
            return 0;
        } else {
            int repeatCount = event.getRepeatCount();
            if (3 == keyCode && (event.getFlags() & SPEECH_START_TYPE_VALUE) == 0) {
                this.mIsHomeKeyDown = down;
            } else if (mKeycodeRecentTask == keyCode) {
                this.mIsMenuKeyDown = down;
            }
            if (4 == keyCode && down && repeatCount == 0) {
                this.mBackKeyUpIgnore = false;
            }
            if (mKeycodeRecentTask == keyCode && down && repeatCount == 0) {
                this.mMenuKeyUpIgnore = false;
            }
            if (this.mIsHomeKeyDown) {
                if (4 != keyCode || !down) {
                    if (this.mIsMenuKeyDown) {
                        this.mMenuKeyUpIgnore = true;
                        if (this.mOppoDebug) {
                            Log.d(TAG, "interceptKeyBeforeQueueing: Home and Menu all Pressed, set mMenuKeyUpIgnore true!");
                        }
                    }
                    if (mKeycodeRecentTask == keyCode) {
                        if (!down) {
                            this.mMenuKeyUpIgnore = false;
                        }
                        if (this.mOppoDebug) {
                            Log.d(TAG, "interceptKeyBeforeQueueing: Home Pressed, ignore RecentTask. down=" + down);
                        }
                        return 0;
                    }
                } else {
                    if (repeatCount == 0) {
                        this.mBackKeyUpIgnore = true;
                    }
                    if (this.mOppoDebug) {
                        Log.d(TAG, "interceptKeyBeforeQueueing: Home Pressed, ignore KEYCODE_BACK DOWN!");
                    }
                    return 0;
                }
            }
            if (4 == keyCode && !down && this.mBackKeyUpIgnore) {
                this.mBackKeyUpIgnore = false;
                if (this.mOppoDebug) {
                    Log.d(TAG, "interceptKeyBeforeQueueing: KEYCODE_BACK DOWN has been ignored, ignore KEYCODE_BACK UP now!");
                }
                return 0;
            } else if (mKeycodeRecentTask != keyCode || down || !this.mMenuKeyUpIgnore) {
                if (keyCode == mKeycodeRecentTask && !down) {
                    this.mHandler.removeCallbacks(this.mRecentsStartSplitSreen);
                }
                if (keyCode == 3 && !down) {
                    this.mHandler.removeCallbacks(this.mHomeKeyLongPress);
                }
                if ((25 == keyCode || 24 == keyCode || 164 == keyCode) && down) {
                    this.mContext.sendBroadcastAsUser(new Intent("SILENCE_ACTION_FOR_OPPO_SPEECH"), UserHandle.CURRENT);
                    if (AudioSystem.isStreamActive(5, 0)) {
                        disableNotificationAlert();
                    }
                }
                if ((25 == keyCode || 24 == keyCode) && down) {
                    long eventTime = event.getEventTime();
                    synchronized (this.mLaunchCameraSettingMap) {
                        if (this.mLaunchCameraSettingMap.containsKey(Integer.valueOf(this.mCurrentUserId))) {
                            launchCameraSetting = this.mLaunchCameraSettingMap.get(Integer.valueOf(this.mCurrentUserId)).intValue();
                        } else {
                            int launchCameraSetting2 = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), QUICK_LAUNCH_CAMERA_KEY, 0, this.mCurrentUserId);
                            this.mLaunchCameraSettingMap.put(Integer.valueOf(this.mCurrentUserId), Integer.valueOf(launchCameraSetting2));
                            launchCameraSetting = launchCameraSetting2;
                        }
                    }
                    if (event.getDevice() == null || event.getDevice().getName() == null) {
                        deviceName = "";
                    } else {
                        deviceName = event.getDevice().getName();
                    }
                    if (launchCameraSetting == 1 && !isScreenOn() && event.getDevice() != null) {
                        if (!event.getDevice().isExternal() && repeatCount == 0 && !this.mSuperPowerSaveMode) {
                            if (QCOM_DEVICE_NAME.equals(deviceName) || MTK_DEVICE_NAME.equals(deviceName)) {
                                if (25 == keyCode) {
                                    this.mLastVolumeUpTime = 0;
                                    if (eventTime - this.mLastVolumeDownTime <= 500) {
                                        String audioPids = this.mAudioManager.getParameters(GET_PID_TAG);
                                        TelecomManager telecomManager = getTelecommService();
                                        if ((audioPids == null || audioPids.isEmpty()) && (telecomManager == null || !telecomManager.isInCall())) {
                                            quickLaunchCamera(eventTime);
                                        }
                                        this.mLastVolumeDownTime = 0;
                                    } else {
                                        this.mLastVolumeDownTime = eventTime;
                                    }
                                } else if (24 == keyCode) {
                                    this.mLastVolumeDownTime = 0;
                                    if (eventTime - this.mLastVolumeUpTime <= 500) {
                                        String audioPids2 = this.mAudioManager.getParameters(GET_PID_TAG);
                                        TelecomManager telecomManager2 = getTelecommService();
                                        if ((audioPids2 == null || audioPids2.isEmpty()) && (telecomManager2 == null || !telecomManager2.isInCall())) {
                                            quickLaunchCamera(eventTime);
                                        }
                                        this.mLastVolumeUpTime = 0;
                                    } else {
                                        this.mLastVolumeUpTime = eventTime;
                                    }
                                }
                            }
                        }
                    }
                    this.mLastVolumeDownTime = 0;
                    this.mLastVolumeUpTime = 0;
                } else if (!(25 == keyCode || 24 == keyCode)) {
                    this.mLastVolumeDownTime = 0;
                    this.mLastVolumeUpTime = 0;
                }
                int policyFlags2 = handleScreenoffGesture(event, policyFlags, interactive);
                if (this.mOppoDebug) {
                    Log.d(TAG, "mIsUpGestureMode = " + this.mIsUpGestureMode + ",  mDisableBottomKeyMode = " + this.mDisableBottomKeyMode + ",  policyFlags = " + policyFlags2 + ",  interactive = " + interactive + ",  canceled = " + event.isCanceled());
                }
                if (this.mIsUpGestureMode || 1 != this.mDisableBottomKeyMode) {
                    i = 0;
                } else {
                    if (down) {
                        if (keyCode != this.mLastDownKeyCode || repeatCount != 0) {
                            if (keyCode == mKeycodeRecentTask || keyCode == 3 || keyCode == 4) {
                                this.mHandler.post(new Runnable() {
                                    /* class com.android.server.policy.OppoPhoneWindowManager.AnonymousClass8 */

                                    public void run() {
                                        if (OppoPhoneWindowManager.this.mHitToast != null) {
                                            OppoPhoneWindowManager.this.mHitToast.cancel();
                                        }
                                        OppoPhoneWindowManager oppoPhoneWindowManager = OppoPhoneWindowManager.this;
                                        oppoPhoneWindowManager.mHitToast = Toast.makeText(oppoPhoneWindowManager.mContext, OppoPhoneWindowManager.this.mContext.getString(201590152, 1), 0);
                                        OppoPhoneWindowManager.this.mHitToast.show();
                                    }
                                });
                            }
                            this.mSameBottomKeyClickCount = 0;
                        } else if (keyCode == mKeycodeRecentTask || keyCode == 3 || keyCode == 4) {
                            if (BOTTOM_KEY_CLICK_INTERVAL > SystemClock.uptimeMillis() - this.mLastDownKeyTime) {
                                this.mSameBottomKeyClickCount++;
                            } else {
                                this.mHandler.post(new Runnable() {
                                    /* class com.android.server.policy.OppoPhoneWindowManager.AnonymousClass7 */

                                    public void run() {
                                        if (OppoPhoneWindowManager.this.mHitToast != null) {
                                            OppoPhoneWindowManager.this.mHitToast.cancel();
                                        }
                                        OppoPhoneWindowManager oppoPhoneWindowManager = OppoPhoneWindowManager.this;
                                        oppoPhoneWindowManager.mHitToast = Toast.makeText(oppoPhoneWindowManager.mContext, OppoPhoneWindowManager.this.mContext.getString(201590152, 1), 0);
                                        OppoPhoneWindowManager.this.mHitToast.show();
                                    }
                                });
                                this.mSameBottomKeyClickCount = 0;
                            }
                        }
                        this.mLastDownKeyCode = keyCode;
                        this.mLastDownKeyTime = event.getDownTime();
                    } else if (this.mSameBottomKeyClickCount == 1) {
                        this.mDisableBottomKeyMode = 0;
                        this.mSameBottomKeyClickCount = 0;
                        this.mLastDownKeyCode = -1;
                        Settings.System.putInt(this.mContext.getContentResolver(), "disable_bottom_key_mode", 0);
                        this.mHandler.post(new Runnable() {
                            /* class com.android.server.policy.OppoPhoneWindowManager.AnonymousClass9 */

                            public void run() {
                                if (OppoPhoneWindowManager.this.mHitToast != null) {
                                    OppoPhoneWindowManager.this.mHitToast.cancel();
                                }
                                OppoPhoneWindowManager oppoPhoneWindowManager = OppoPhoneWindowManager.this;
                                oppoPhoneWindowManager.mHitToast = Toast.makeText(oppoPhoneWindowManager.mContext, OppoPhoneWindowManager.this.mContext.getString(201590132), 0);
                                OppoPhoneWindowManager.this.mHitToast.show();
                            }
                        });
                    }
                    if (keyCode == mKeycodeRecentTask || keyCode == 3 || keyCode == 4) {
                        return 0;
                    }
                    i = 0;
                }
                volumeUpLongPressBeforeQueueing(event);
                if (volumeUpDoubleClickBeforeQueueing(event)) {
                    return i;
                }
                KeyEvent newEvent = adjustKey(event);
                if (this.mOppoDebug && isLogKey(keyCode)) {
                    Log.d(TAG, "interceptKeyBeforeQueueing:" + newEvent.toString());
                }
                int newKeyCode = newEvent.getKeyCode();
                boolean launchSos = false;
                if (newKeyCode == 26 && down) {
                    enqueuePowerKeyDownEvent(event.getDownTime());
                    launchSos = handlePowerKeyDownEventForSosEarly(event.getDownTime());
                }
                if (OppoFeatureCache.get(IColorGameSpaceManager.DEFAULT).isInterceptKeyBeforeQueueing(newEvent, policyFlags2)) {
                    return 0;
                }
                int result = OppoPhoneWindowManager.super.interceptKeyBeforeQueueing(newEvent, policyFlags2);
                if (repeatCount == 0 && down && event.getKeyCode() == 717) {
                    result = 1;
                }
                if (newKeyCode == 26) {
                    if (down) {
                        handlePowerKeyDownEventForSosLate(launchSos);
                    }
                    if (!this.mOppoEarlyPowerKeyHandle && down) {
                        if (!interactive && this.mPowerKeyHandled) {
                            this.mPowerConsumedByWakeup = true;
                        }
                        sendSpeechMessage(event);
                    }
                }
                if (!event.isCanceled() && newKeyCode == 3 && !down && (result & 1) != 0) {
                    this.mOppoAppFrozen.sendHomeDispatchTimeoutMsg();
                }
                return result;
            } else {
                this.mMenuKeyUpIgnore = false;
                if (this.mOppoDebug) {
                    Log.d(TAG, "interceptKeyBeforeQueueing: RecentTask DOWN has been ignored, ignore RecentTask UP now!");
                }
                return 0;
            }
        }
    }

    public long interceptKeyBeforeDispatching(WindowManagerPolicy.WindowState win, KeyEvent event, int policyFlags) {
        boolean down1 = event.getAction() == 0;
        boolean down = event.getAction() == 0;
        int keyCode = event.getKeyCode();
        event.getFlags();
        boolean canceled = event.isCanceled();
        int repeatCount = event.getRepeatCount();
        boolean keydown = event.getAction() == 0;
        boolean keyup = event.getAction() == 1;
        if (keyCode == 4 && keydown && repeatCount == 0) {
            SystemProperties.set("debug.sys.oppo.keydowntime", Long.toString(System.currentTimeMillis()));
        } else if (keyCode == 4 && keyup) {
            SystemProperties.set("debug.sys.oppo.keyuptime", Long.toString(System.currentTimeMillis()));
        } else if (keyCode == 3 && keydown) {
            SystemProperties.set("debug.sys.oppo.keydowntime", Long.toString(Long.MAX_VALUE));
        }
        if (keyCode == 3 && !down) {
            this.mOppoAppFrozen.clearHomeDispatchTimeoutMsg();
            this.mOppoAppFrozen.appFrozenHandle();
            this.mHandler.removeCallbacks(this.mHomeKeyLongPress);
        }
        if (keyCode == mKeycodeRecentTask && !down && this.mHandler.hasCallbacks(this.mRecentsStartSplitSreen)) {
            this.mHandler.removeCallbacks(this.mRecentsStartSplitSreen);
        }
        if (keyCode == mKeycodeRecentTask && this.mContext.getPackageManager().hasSystemFeature(FEATURE_CHILDREN_MODE) && Settings.Global.getInt(this.mContext.getContentResolver(), SETTING_CHILDREN_MODE, 0) == 1) {
            Log.d(TAG, "ignore menu in cdm");
            return -1;
        }
        if (win != null) {
            ColorBaseLayoutParams baseLp = typeCasting(win.getAttrs());
            if (!(win.getAttrs() == null || baseLp == null)) {
                if (this.mIgnoreHomeAndMenu && down) {
                    this.mIgnoreHomeAndMenu = false;
                }
                if (baseLp.ignoreHomeMenuKey == 1 || (win.getAttrs().memoryType & 16777216) != 0) {
                    if (keyCode == mKeycodeRecentTask || keyCode == 3 || keyCode == 82) {
                        if (down) {
                            this.mIgnoreHomeAndMenu = true;
                        } else {
                            this.mIgnoreHomeAndMenu = false;
                        }
                        Log.v(TAG, "ignoreHomeMenuKey ignore KEYCODE_MENU and KEYCODE_HOME win: " + win);
                        return -1;
                    }
                } else if (baseLp.ignoreHomeMenuKey == 2 || (win.getAttrs().memoryType & 33554432) != 0) {
                    if (keyCode == 3) {
                        if (down) {
                            this.mIgnoreHomeAndMenu = true;
                        } else {
                            this.mIgnoreHomeAndMenu = false;
                        }
                        Log.v(TAG, "ignoreHomeMenuKey ignore KEYCODE_HOME win: " + win);
                        return -1;
                    }
                } else if ((baseLp.ignoreHomeMenuKey == 3 || (win.getAttrs().memoryType & ColorMultiAppManagerService.FLAG_MULTI_APP) != 0) && (keyCode == mKeycodeRecentTask || keyCode == 82)) {
                    if (down) {
                        this.mIgnoreHomeAndMenu = true;
                    } else {
                        this.mIgnoreHomeAndMenu = false;
                    }
                    Log.v(TAG, "ignoreHomeMenuKey ignore KEYCODE_MENU win: " + win);
                    return -1;
                }
                if (!down && this.mIgnoreHomeAndMenu) {
                    Log.v(TAG, "ignoreHomeMenuKey ignore  mignoreHomeAndMenu: " + this.mIgnoreHomeAndMenu);
                    return -1;
                }
            }
        }
        long retValUp = volumeUpLongPressBeforeDispatching(event);
        if (retValUp != 0) {
            return retValUp;
        }
        long retValUp2 = volumeUpDoubleClickBeforeDispatching(event);
        if (retValUp2 != 0) {
            return retValUp2;
        }
        if (this.mOppoDebug && isLogKey(keyCode)) {
            Log.d(TAG, "interceptKeyBeforeDispatching key: win=" + win + "  event = " + event.toString());
        }
        KeyEvent newEvent = adjustKey(event);
        if (this.mOppoDebug && isLogKey(keyCode)) {
            Log.d(TAG, "interceptKeyBeforeDispatching newEvent keyCode = " + newEvent.getKeyCode());
        }
        if (newEvent.getKeyCode() != 3) {
            if (newEvent.getKeyCode() == mKeycodeRecentTask) {
                if (canceled) {
                    Log.i(TAG, "Ignoring RecentApps key; event canceled.");
                    return -1;
                } else if (!keyguardOn() && down && repeatCount == 0) {
                    this.mRecentsLongPressDetected.set(false);
                    if (!isInLockTaskMode()) {
                        this.mHandler.postDelayed(this.mRecentsStartSplitSreen, 750);
                    }
                }
            }
            if (repeatCount == 0 && down && event.getKeyCode() == 717) {
                String gimbalLaunchPkg = ColorKeyLayoutManagerUtil.getInstance().getGimbalLaunchPkg();
                if (win == null || win.getOwningPackage() == null) {
                    if (this.mOppoDebug) {
                        Log.d(TAG, "Current top package: null");
                    }
                    interceptGimbalPowerKey(gimbalLaunchPkg);
                } else if (!win.getOwningPackage().equals(gimbalLaunchPkg)) {
                    if (this.mOppoDebug) {
                        Log.d(TAG, "Current top package: " + win.getOwningPackage());
                    }
                    interceptGimbalPowerKey(gimbalLaunchPkg);
                }
            }
            return OppoPhoneWindowManager.super.interceptKeyBeforeDispatching(win, newEvent, policyFlags);
        } else if (down || this.mHomeConsumed) {
            if (!isExpROM() && down && repeatCount == 0) {
                this.mHandler.postDelayed(this.mHomeKeyLongPress, 750);
                if (this.mOppoDebug) {
                    Log.i(TAG, "interceptKeyBeforeDispatching: postDelayed speech");
                }
            }
            return OppoPhoneWindowManager.super.interceptKeyBeforeDispatching(win, event, policyFlags);
        } else {
            this.mHomeConsumed = false;
            getInner().cancelPreloadRecentApps();
            if (!canceled) {
                if (win == null || win.getAttrs() == null || win.getAttrs().getTitle() == null || !win.getAttrs().getTitle().toString().equals(OPPO_INCALL_SCREEN) || !isIncomingRing()) {
                    launchHomeFromHotKey(0);
                    return -1;
                }
                hideIncommingCallUi();
                return -1;
            } else if (!this.mOppoDebug) {
                return -1;
            } else {
                Log.i(TAG, "Ignoring HOME; event canceled.");
                return -1;
            }
        }
    }

    private void hideIncommingCallUi() {
        if (this.mOppoDebug) {
            Log.d(TAG, "hideIncommingCallUi.");
        }
        Intent intent = new Intent();
        intent.setAction("oppo.intent.action.incallui.HIDE_INCOMING_FULL_UI");
        intent.setPackage("com.android.incallui");
        try {
            this.mContext.sendBroadcastAsUser(intent, UserHandle.CURRENT);
        } catch (Exception e) {
            Log.w(TAG, "hideIncommingCallUi: ", e);
        }
    }

    private boolean isIncomingRing() {
        boolean incomingRinging = false;
        TelecomManager telecomManager = (TelecomManager) this.mContext.getSystemService("telecom");
        if (telecomManager != null) {
            incomingRinging = telecomManager.isRinging();
        }
        if (this.mOppoDebug) {
            Log.d(TAG, "isIncomingRing: " + incomingRinging);
        }
        return incomingRinging;
    }

    private class KeyLockModeReceiver extends BroadcastReceiver {
        private KeyLockModeReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            int nMode = intent.getIntExtra("KeyLockMode", -1);
            String processName = intent.getStringExtra("ProcessName");
            if (-1 == nMode || processName == null) {
                Log.i(OppoPhoneWindowManager.TAG, "KeyLockModeReceiver: processName=" + processName + ", nMode=" + nMode + ", do nothing!");
                return;
            }
            Log.i(OppoPhoneWindowManager.TAG, "KeyLockModeReceiver KeyLockMode: " + nMode + " ProcessName: " + processName);
            synchronized (OppoPhoneWindowManager.this.mKeyLockIntentProcess) {
                if (nMode != 0) {
                    OppoPhoneWindowManager.this.setVideoMode(nMode);
                    if (!OppoPhoneWindowManager.this.mKeyLockIntentProcess.contains(processName)) {
                        OppoPhoneWindowManager.this.mKeyLockIntentProcess.add(processName);
                    }
                    if ("com.android.individuationSettings:individuationSetting".equals(processName)) {
                        OppoPhoneWindowManager.this.mPauseForChangingTheme = true;
                        OppoPhoneWindowManager.this.mHandler.postDelayed(OppoPhoneWindowManager.this.mResumeForChangingTheme, OppoPhoneWindowManager.this.mForceResumForChangingTheme);
                    }
                } else if (OppoPhoneWindowManager.this.mKeyLockIntentProcess.contains(processName)) {
                    OppoPhoneWindowManager.this.setVideoMode(nMode);
                    OppoPhoneWindowManager.this.mKeyLockIntentProcess.remove(processName);
                    if ("com.android.individuationSettings:individuationSetting".equals(processName)) {
                        OppoPhoneWindowManager.this.mHandler.removeCallbacks(OppoPhoneWindowManager.this.mResumeForChangingTheme);
                        if (OppoPhoneWindowManager.this.mPauseForChangingTheme) {
                            OppoPhoneWindowManager.this.mPauseForChangingTheme = false;
                        }
                    }
                }
            }
        }
    }

    private class DisableBottomKeyModeReceiver extends BroadcastReceiver {
        private DisableBottomKeyModeReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            int mode = intent.getIntExtra("DisableBottomKeyMode", -1);
            if (-1 == mode) {
                Log.i(OppoPhoneWindowManager.TAG, "mode=" + mode + ", do nothing!");
                return;
            }
            Log.i(OppoPhoneWindowManager.TAG, "DisableBottomMode: " + mode);
            if (mode == 0) {
                OppoPhoneWindowManager.this.mDisableBottomKeyMode = 0;
                Settings.System.putInt(OppoPhoneWindowManager.this.mContext.getContentResolver(), "disable_bottom_key_mode", 0);
            } else if (1 == mode) {
                OppoPhoneWindowManager.this.mDisableBottomKeyMode = 1;
                Settings.System.putInt(OppoPhoneWindowManager.this.mContext.getContentResolver(), "disable_bottom_key_mode", 1);
            }
        }
    }

    public int hookWindowLayerFromTypeLw(int type, boolean canAddInternalSystemWindow) {
        int layer = getWindowLayerFromType(type, canAddInternalSystemWindow);
        if (layer > 0) {
            return layer;
        }
        return 0;
    }

    private int getWindowLayerFromType(int type, boolean canAddInternalSystemWindow) {
        if (type == 2004) {
            return WINDOW_LAYER_14;
        }
        if (type != 2010) {
            if (type == 2032) {
                return WINDOW_LAYER_112;
            }
            if (type == 2036) {
                return WINDOW_LAYER_107;
            }
            if (type == 2100) {
                return 2;
            }
            if (type == 2300) {
                return WINDOW_LAYER_114;
            }
            if (type == 2305) {
                return WINDOW_LAYER_113;
            }
            if (type == 2023) {
                return 25;
            }
            if (type != 2024) {
                if (type == 2026) {
                    return WINDOW_LAYER_110;
                }
                if (type == 2027) {
                    return WINDOW_LAYER_109;
                }
                if (type == 2302) {
                    return 100;
                }
                if (type == 2303) {
                    return 102;
                }
                switch (type) {
                    case 2014:
                        return 103;
                    case 2015:
                        return WINDOW_LAYER_115;
                    case 2016:
                        return WINDOW_LAYER_111;
                    default:
                        switch (type) {
                            case 2018:
                                return WINDOW_LAYER_117;
                            case 2019:
                                if (hasNavigationBar()) {
                                    return WINDOW_LAYER_105;
                                }
                                return 0;
                            case 2020:
                                return 104;
                            case 2021:
                                return WINDOW_LAYER_116;
                            default:
                                switch (type) {
                                    case 2309:
                                        return 102;
                                    case 2310:
                                        return WINDOW_LAYER_113;
                                    case 2311:
                                        return WINDOW_LAYER_107;
                                    case 2312:
                                        return 3;
                                    case 2313:
                                        return WINDOW_LAYER_55;
                                    case 2314:
                                        return 16;
                                    case 2315:
                                        return WINDOW_LAYER_115;
                                    case 2316:
                                        return 4;
                                    case 2317:
                                        return 101;
                                    case 2318:
                                        return 10;
                                    default:
                                        return 0;
                                }
                        }
                }
            } else if (hasNavigationBar()) {
                return WINDOW_LAYER_106;
            } else {
                return 0;
            }
        } else if (canAddInternalSystemWindow) {
            return WINDOW_LAYER_108;
        } else {
            return 10;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setVideoMode(int mode) {
        this.mCurrentKeyMode = mode;
    }

    private boolean customIgnoreHomeTaskKey(int keyCode, boolean down) {
        if (!this.mIsBusinessCustom) {
            return false;
        }
        if (3 == keyCode) {
            if (down) {
                if (SystemProperties.getBoolean("persist.sys.custom_home_disable", false)) {
                    this.mIsCustomHomeDownIgnored = true;
                    return true;
                }
            } else if (this.mIsCustomHomeDownIgnored) {
                this.mIsCustomHomeDownIgnored = false;
                return true;
            }
        } else if (mKeycodeRecentTask == keyCode) {
            if (down) {
                if (SystemProperties.getBoolean("persist.sys.custom_task_disable", false)) {
                    this.mIsCustomTaskDownIgnored = true;
                    return true;
                }
            } else if (this.mIsCustomTaskDownIgnored) {
                this.mIsCustomTaskDownIgnored = false;
                return true;
            }
        } else if (4 == keyCode) {
            if (down) {
                if (SystemProperties.getBoolean("persist.sys.custom_back_disable", false)) {
                    this.mIsCustomBackDownIgnored = true;
                    return true;
                }
            } else if (this.mIsCustomBackDownIgnored) {
                this.mIsCustomBackDownIgnored = false;
                return true;
            }
        }
        return false;
    }

    private KeyEvent adjustKey(KeyEvent event) {
        int keyCode = event.getKeyCode();
        int currentKeyMode = getColorKeyMode();
        if (1 == currentKeyMode) {
            if (keyCode != 26) {
                return event;
            }
            return offsetKey(event);
        } else if (2 == currentKeyMode) {
            if (keyCode == 3 || keyCode == 26 || keyCode == 82 || keyCode == mKeycodeRecentTask) {
                return offsetKey(event);
            }
            return event;
        } else if (3 == currentKeyMode) {
            if (keyCode == 3 || keyCode == 82 || keyCode == mKeycodeRecentTask) {
                return offsetKey(event);
            }
            return event;
        } else if (4 != this.mCurrentKeyMode) {
            return event;
        } else {
            if (keyCode == 3 || keyCode == 82 || keyCode == mKeycodeRecentTask) {
                return offsetKey(event);
            }
            return event;
        }
    }

    private KeyEvent offsetKey(KeyEvent event) {
        return KeyEvent.obtain(event.getDownTime(), event.getEventTime(), event.getAction(), event.getKeyCode() + 800, event.getRepeatCount(), event.getMetaState(), event.getDeviceId(), event.getScanCode(), event.getFlags(), event.getSource(), event.getCharacters());
    }

    /* access modifiers changed from: protected */
    public synchronized boolean startPersonalAssistant(int source) {
        if (source == 1) {
            if (isExpROM()) {
                return false;
            }
        }
        if (OppoSysStateManager.getInstance().isScreenOff()) {
            if (this.mOppoDebug) {
                Log.d(TAG, "startPersonalAssistant: is screen off. ignore");
            }
            return false;
        } else if (keyguardOn()) {
            if (this.mOppoDebug) {
                Log.d(TAG, "startPersonalAssistant: is keyguardOn. ignore");
            }
            return false;
        } else if (this.mContext.getPackageManager().hasSystemFeature(FEATURE_CHILDREN_MODE) && Settings.Global.getInt(this.mContext.getContentResolver(), SETTING_CHILDREN_MODE, 0) == 1) {
            Log.d(TAG, "startPersonalAssistant: is children space. ignore");
            return false;
        } else if (!isUserSetupComplete()) {
            Log.d(TAG, "startPersonalAssistant: isUserSetupComplete fail. ignore");
            return false;
        } else if (this.mSuperPowerSaveMode) {
            Log.d(TAG, "startPersonalAssistant: failed. super power save ignore");
            return false;
        } else {
            List<String> targetPkgList = new ArrayList<>();
            targetPkgList.add("com.coloros.personalassistant");
            OppoListManager.getInstance().addBackgroundRestrictedInfo("android", targetPkgList);
            Intent intent = new Intent();
            intent.setAction("colors.intent.action.PERSONAL_ASSISTANT_SERVICE");
            intent.setPackage("com.coloros.personalassistant");
            intent.putExtra("startBy", String.valueOf(source));
            try {
                this.mContext.startServiceAsUser(intent, UserHandle.CURRENT);
            } catch (Exception e) {
                Log.w(TAG, "startPersonalAssistant: ", e);
            }
            if (this.mOppoDebug) {
                Log.d(TAG, "startPersonalAssistant");
            }
            return true;
        }
    }

    public void systemReady() {
        OppoPhoneWindowManager.super.systemReady();
        this.mSensorManager = new SystemSensorManager(this.mContext, this.mHandler.getLooper());
        this.mProximitySensor = this.mSensorManager.getDefaultSensor(8);
        boolean z = false;
        this.mDisableBottomKeyMode = 0;
        Settings.System.putInt(this.mContext.getContentResolver(), "disable_bottom_key_mode", 0);
        if (Settings.Secure.getInt(this.mContext.getContentResolver(), STR_UP_GESTURE_MODE, 0) == 2) {
            z = true;
        }
        this.mIsUpGestureMode = z;
    }

    public void screenTurnedOff() {
        OppoPhoneWindowManager.super.screenTurnedOff();
        OppoScreenOffGestureManager oppoScreenOffGestureManager = this.mGestureManager;
        if (oppoScreenOffGestureManager != null) {
            oppoScreenOffGestureManager.screenTurnedOff();
        }
        OppoScreenShotUtil.resumeDeliverPointerEvent();
        this.mHandler.removeCallbacks(this.mHomeKeyLongPress);
        new Thread(new Runnable() {
            /* class com.android.server.policy.OppoPhoneWindowManager.AnonymousClass13 */

            public void run() {
                OppoPhoneWindowManager.this.mContext.sendBroadcast(new Intent("android.intent.action.OPPO_SCREEN_OFF"), "oppo.permission.OPPO_COMPONENT_SAFE");
            }
        }).start();
        this.mHandler.removeCallbacks(this.mRecentsStartSplitSreen);
        this.mColorPowerKeyHandler.removeMessages(MSG_POWER_LONG_PRESS_FOR_SPEECH);
    }

    public void screenTurningOn(WindowManagerPolicy.ScreenOnListener screenOnListener) {
        OppoPhoneWindowManager.super.screenTurningOn(screenOnListener);
        OppoScreenOffGestureManager oppoScreenOffGestureManager = this.mGestureManager;
        if (oppoScreenOffGestureManager != null) {
            oppoScreenOffGestureManager.screenTurningOn();
        }
    }

    private void enableProximitySensor() {
        if (this.mOppoDebug) {
            Log.d(TAG, "enableProximitySensor");
        }
        if (!this.mProximitySensorEnabled) {
            long identity = Binder.clearCallingIdentity();
            try {
                this.mSensorManager.registerListener(this.mProximityListener, this.mProximitySensor, 0);
                this.mProximitySensorEnabled = true;
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }
    }

    /* JADX INFO: finally extract failed */
    private void disableProximitySensor() {
        if (this.mOppoDebug) {
            Log.d(TAG, "disableProximitySensor");
        }
        if (this.mProximitySensorEnabled) {
            long identity = Binder.clearCallingIdentity();
            try {
                this.mSensorManager.unregisterListener(this.mProximityListener);
                this.mProximitySensorEnabled = false;
                Binder.restoreCallingIdentity(identity);
                if (this.mProximitySensorActive) {
                    this.mProximitySensorActive = true;
                }
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
                throw th;
            }
        }
    }

    private int handleScreenoffGesture(KeyEvent event, int policyFlags, boolean interactive) {
        boolean down = event.getAction() == 0;
        if (!this.mGestureManager.isScreenoffGestureKey(event.getKeyCode()) || !down) {
            return policyFlags;
        }
        boolean proximityDetectCanceled = this.mContext.getPackageManager().hasSystemFeature("oppo.black.gesture.proximitysensor.support");
        Log.d(TAG, "proximityDetectCanceled = " + proximityDetectCanceled);
        this.mGestureManager.updateGestureInfo();
        if (!proximityDetectCanceled) {
            synchronized (this.mObject) {
                enableProximitySensor();
                try {
                    this.mObject.wait(1000);
                } catch (InterruptedException e) {
                }
                if (!this.mProximitySensorActive) {
                    SystemProperties.set("oppo.dt.wakeupnum", String.valueOf(SystemProperties.getInt("oppo.dt.wakeupnum", 0) + 1));
                }
                Log.d(TAG, "mProximitySensorActive = " + this.mProximitySensorActive);
                if (!this.mProximitySensorActive || (this.mWindowManagerFuncs.getLidState() == 0 && this.mGestureManager.isGestureDoubleTap())) {
                    policyFlags = this.mGestureManager.dealScreenOffGesture(event, policyFlags, interactive);
                }
                disableProximitySensor();
            }
            return policyFlags;
        } else if (this.mWindowManagerFuncs.getLidState() != 0) {
            return this.mGestureManager.dealScreenOffGesture(event, policyFlags, interactive);
        } else {
            if (this.mGestureManager.isGestureDoubleTap()) {
                return this.mGestureManager.dealScreenOffGesture(event, policyFlags, interactive);
            }
            return policyFlags;
        }
    }

    private boolean isLogKey(int keyCode) {
        return 26 == keyCode || 25 == keyCode || 24 == keyCode || 164 == keyCode || mKeycodeRecentTask == keyCode || 82 == keyCode || 3 == keyCode || 79 == keyCode || 4 == keyCode;
    }

    private void dumpWindowState(WindowManagerPolicy.WindowState win) {
        Log.d(TAG, win + "====getOwningPackage :" + win.getOwningPackage());
        StringBuilder sb = new StringBuilder();
        sb.append(" getAttrs :");
        sb.append(win.getAttrs());
        Log.d(TAG, sb.toString());
        Log.d(TAG, "getSurfaceLayer :" + win.getSurfaceLayer());
        Log.d(TAG, "hasAppShownWindows :" + win.hasAppShownWindows());
        Log.d(TAG, "isVisibleLw :" + win.isVisibleLw());
        Log.d(TAG, "isDisplayedLw :" + win.isDisplayedLw());
        Log.d(TAG, "isAnimatingLw :" + win.isAnimatingLw());
        Log.d(TAG, "isGoneForLayoutLw :" + win.isGoneForLayoutLw());
        Log.d(TAG, "hasDrawnLw :" + win.hasDrawnLw());
        Log.d(TAG, "isAlive :" + win.isAlive());
    }

    /* access modifiers changed from: package-private */
    public final void sendIsHomeModeIntent(boolean isHomeMode, boolean isSemipermeable) {
        if (!this.mSystemReady) {
            Log.d(TAG, "sendIsHomeModeIntent:isHomeMode = " + isHomeMode + ", can't send broadcast before boot completed!");
            return;
        }
        Intent intent = new Intent("android.intent.action.HOME_MODE_CHANGE");
        intent.putExtra("ishomemode", isHomeMode ? 1 : 0);
        intent.putExtra("isSemipermeable", isSemipermeable ? 1 : 0);
        Context context = this.mContext;
        if (context != null) {
            context.sendBroadcastAsUser(intent, UserHandle.CURRENT);
        }
    }

    /* access modifiers changed from: package-private */
    public final void sendIsCameraModeIntent(boolean isCameraMode) {
        if (this.mOppoDebug) {
            Log.d(TAG, "===sendIsCameraModeIntent==" + isCameraMode);
        }
        if (!this.mSystemReady) {
            Log.d(TAG, "sendIsCameraModeIntent:isCameraMode = " + isCameraMode + ", can't send broadcast before boot completed!");
            return;
        }
        Intent intent = new Intent("android.intent.action.CAMERA_MODE_CHANGE");
        intent.putExtra("iscameramode", isCameraMode ? 1 : 0);
        Context context = this.mContext;
        if (context != null) {
            context.sendBroadcastAsUser(intent, UserHandle.CURRENT);
        }
    }

    /* access modifiers changed from: package-private */
    public final void sendIsInGestureGuideIntent(boolean isInGestureGuideMode) {
        if (!this.mSystemReady) {
            Log.d(TAG, "GestureGuideIntent can't send broadcast before boot completed!");
            return;
        }
        Intent intent = new Intent("oppo.intent.action.GESTUREGUIDE_MODE_CHANGE");
        intent.putExtra("isInGestureGuideMode", isInGestureGuideMode ? 1 : 0);
        Context context = this.mContext;
        if (context != null) {
            context.sendBroadcastAsUser(intent, UserHandle.CURRENT);
        }
    }

    public int getColorKeyMode() {
        if (this.mOppoDebug && this.mCurrentKeyMode != 0) {
            ArrayList<String> keyLockProcess = new ArrayList<>(this.mKeyLockIntentProcess);
            Log.d(TAG, "In KeyLock:" + this.mCurrentKeyMode);
            for (int i = 0; i < keyLockProcess.size(); i++) {
                Log.d(TAG, "KeyLockProcess:" + keyLockProcess.get(i));
            }
        }
        return this.mCurrentKeyMode;
    }

    public boolean performHapticFeedback(int uid, String packageName, int effectId, boolean always, String reason) {
        if ((Settings.System.getIntForUser(this.mContext.getContentResolver(), "haptic_feedback_enabled", 0, -2) == 0) && !always) {
            return false;
        }
        if (this.mVibrator == null) {
            this.mVibrator = (Vibrator) this.mContext.getSystemService("vibrator");
        }
        if (this.mLinearmotorVibrator == null) {
            this.mLinearmotorVibrator = (LinearmotorVibrator) this.mContext.getSystemService("linearmotor");
        }
        boolean hasPerformed = false;
        if (effectId != 0) {
            if (effectId != 1) {
                if (effectId != 10001) {
                    switch (effectId) {
                        case 3:
                        case ColorStartingWindowRUSHelper.TASK_SNAPSHOT_BLACK_TOKEN_START_FROM_LAUNCHER /* 7 */:
                        case 8:
                        case ColorStartingWindowRUSHelper.SNAPSHOT_FORCE_CLEAR_WHEN_DIFF_ORIENTATION /* 11 */:
                        case ColorStartingWindowRUSHelper.USE_TRANSLUCENT_DRAWABLE_FOR_SPLASH_WINDOW /* 12 */:
                        case 13:
                        case 15:
                        case ColorHansRestriction.HANS_RESTRICTION_BLOCK_ALARM /* 16 */:
                            if (this.mLinearmotorVibrator != null) {
                                this.mLinearmotorVibrator.vibrate(new WaveformEffect.Builder().setEffectType(1).setAsynchronous(true).build());
                                hasPerformed = true;
                                break;
                            }
                            break;
                        case 4:
                        case 6:
                        case ColorStartingWindowRUSHelper.FORCE_USE_COLOR_DRAWABLE_WHEN_SPLASH_WINDOW_TRANSLUCENT /* 9 */:
                            if (this.mLinearmotorVibrator != null) {
                                this.mLinearmotorVibrator.vibrate(new WaveformEffect.Builder().setEffectType(0).setAsynchronous(true).build());
                                hasPerformed = true;
                                break;
                            }
                            break;
                        case 5:
                            if (this.mLinearmotorVibrator != null) {
                                this.mLinearmotorVibrator.vibrate(new WaveformEffect.Builder().setEffectType(2).setAsynchronous(true).build());
                                hasPerformed = true;
                                break;
                            }
                            break;
                        case 10:
                        case WINDOW_LAYER_14 /* 14 */:
                            if (this.mLinearmotorVibrator != null) {
                                this.mLinearmotorVibrator.vibrate(new WaveformEffect.Builder().setEffectType(68).setAsynchronous(true).build());
                                hasPerformed = true;
                                break;
                            }
                            break;
                        case MSG_ISCAMERAMODE /* 17 */:
                            if (this.mLinearmotorVibrator != null) {
                                this.mLinearmotorVibrator.vibrate(new WaveformEffect.Builder().setEffectType(3).setAsynchronous(true).build());
                                hasPerformed = true;
                                break;
                            }
                            break;
                        default:
                            switch (effectId) {
                                case 300:
                                    this.mVibrator.vibrate(new long[]{0, 65, 50, 65}, -1);
                                    hasPerformed = true;
                                    break;
                                case 301:
                                    this.mVibrator.vibrate(new long[]{0, 40}, -1);
                                    hasPerformed = true;
                                    break;
                                case 302:
                                    if (this.mLinearmotorVibrator != null) {
                                        this.mLinearmotorVibrator.vibrate(new WaveformEffect.Builder().setEffectType(0).setAsynchronous(true).build());
                                        hasPerformed = true;
                                        break;
                                    }
                                    break;
                                case 303:
                                    if (this.mLinearmotorVibrator != null) {
                                        this.mLinearmotorVibrator.vibrate(new WaveformEffect.Builder().setEffectType(1).setAsynchronous(true).build());
                                        hasPerformed = true;
                                        break;
                                    }
                                    break;
                                case 304:
                                    if (this.mLinearmotorVibrator != null) {
                                        this.mLinearmotorVibrator.vibrate(new WaveformEffect.Builder().setEffectType(3).setAsynchronous(true).build());
                                        hasPerformed = true;
                                        break;
                                    }
                                    break;
                            }
                    }
                } else if (this.mLinearmotorVibrator != null) {
                    this.mLinearmotorVibrator.vibrate(new WaveformEffect.Builder().setEffectType(70).setAsynchronous(true).build());
                    hasPerformed = true;
                }
            } else if (this.mLinearmotorVibrator != null) {
                this.mLinearmotorVibrator.vibrate(new WaveformEffect.Builder().setEffectType(1).setAsynchronous(true).build());
                hasPerformed = true;
            } else {
                Vibrator vibrator = this.mVibrator;
                if (vibrator != null) {
                    vibrator.vibrate(new long[]{0, 65}, -1);
                    hasPerformed = true;
                }
            }
        } else if (this.mLinearmotorVibrator != null) {
            this.mLinearmotorVibrator.vibrate(new WaveformEffect.Builder().setEffectType(1).setAsynchronous(true).build());
            hasPerformed = true;
        } else {
            Vibrator vibrator2 = this.mVibrator;
            if (vibrator2 != null) {
                vibrator2.vibrate(new long[]{0, 65}, -1);
                hasPerformed = true;
            }
        }
        if (hasPerformed) {
            return hasPerformed;
        }
        return OppoPhoneWindowManager.super.performHapticFeedback(uid, packageName, effectId, always, reason);
    }

    public void showBootMessage(final CharSequence msg, boolean always) {
        this.mHandler.post(new Runnable() {
            /* class com.android.server.policy.OppoPhoneWindowManager.AnonymousClass16 */

            public void run() {
                if (OppoPhoneWindowManager.this.mBootMsgDialog == null) {
                    OppoPhoneWindowManager oppoPhoneWindowManager = OppoPhoneWindowManager.this;
                    oppoPhoneWindowManager.mBootMsgDialog = ColorBootMessageDialog.create(oppoPhoneWindowManager.mContext);
                    OppoPhoneWindowManager.this.mBootMsgDialog.show();
                }
                OppoPhoneWindowManager.this.mBootMsgDialog.setMessage(msg);
            }
        });
    }

    /* access modifiers changed from: package-private */
    public void showGlobalActionsInternal() {
        sendMessageBeforeGlobalAction();
        try {
            if (!(this.mContext.getPackageManager() == null || !this.mContext.getPackageManager().hasSystemFeature(OPPO_ENTERPRISE_DEVELOPMENT_FEATURE) || Settings.Secure.getInt(this.mContext.getContentResolver(), OPPO_SETTINGS_FORBID_GLOBALACTION, 0) == 0)) {
                return;
            }
        } catch (Exception e) {
            Log.e(TAG, "Enterprise development platform: showGlobalActionsInternal exception");
        }
        if (this.mGlobalActions == null) {
            this.mGlobalActions = new OppoGlobalActions(this.mContext, this.mWindowManagerFuncs);
        }
        if (this.mDefaultDisplayPolicy != null && (this.mDefaultDisplayPolicy instanceof OppoDisplayPolicy)) {
            this.mGlobalActions.setSystemUiVisibility(this.mDefaultDisplayPolicy.isSystemUiVisibility());
        }
        OppoPhoneWindowManager.super.showGlobalActionsInternal();
    }

    /* access modifiers changed from: package-private */
    public void launchHomeFromHotKey(int displayId) {
        if (this.mGlobalActions != null) {
            this.mGlobalActions.removePowerView();
        }
        OppoPhoneWindowManager.super.launchHomeFromHotKey(displayId);
    }

    public boolean isGlobalActionVisible() {
        return this.mGlobalActions != null && this.mGlobalActions.isShowing();
    }

    public int checkAddPermission(WindowManager.LayoutParams attrs, int[] outAppOp) {
        if ("SecurityInputMethodDialog".equals(attrs.getTitle())) {
            return 0;
        }
        return OppoPhoneWindowManager.super.checkAddPermission(attrs, outAppOp);
    }

    public void updateSettings() {
        OppoPhoneWindowManager.super.updateSettings();
        synchronized (getInner().getLock()) {
            updateSettingsForOppoLocked(this.mContext.getContentResolver());
            this.mIncallPowerButtonHangup = Settings.Global.getInt(this.mContext.getContentResolver(), STR_INCALL_POWER_BUTTON_HANGUP, 0);
            Log.d(TAG, "updateSettings: incallPowerButtonHangup = " + this.mIncallPowerButtonHangup);
            this.mPowerButtonEndsAlarmclock = Settings.Secure.getInt(this.mContext.getContentResolver(), STR_POWER_BUTTON_ENDS_ALARMCLOCK, 0);
            Log.d(TAG, "updateSettings: powerButtonEndsAlarmclock = " + this.mPowerButtonEndsAlarmclock);
            if (this.mDefaultDisplayPolicy instanceof OppoDisplayPolicy) {
                this.mDefaultDisplayPolicy.updateKeyboardPosition();
            }
            boolean z = true;
            if (Settings.System.getIntForUser(this.mContext.getContentResolver(), SUPER_POWERSAVE_MODE_STATE, 0, 0) != 1) {
                z = false;
            }
            this.mSuperPowerSaveMode = z;
        }
    }

    /* access modifiers changed from: package-private */
    public void extraWorkInCancelPendingPowerKeyAction() {
        this.mColorPowerKeyHandler.removeMessages(MSG_POWER_LONG_PRESS_FOR_SPEECH);
    }

    /* access modifiers changed from: package-private */
    public void colorInterceptPowerKeyDown(KeyEvent event, boolean interactive) {
        if (interactive && getPowerKeyForbiddenState()) {
            if (this.mOppoDebug) {
                Slog.d(TAG, "sos forbid power key down");
            }
            this.mPowerKeyHandled |= true;
        }
        this.mOppoEarlyPowerKeyHandle = this.mPowerKeyHandled;
    }

    /* access modifiers changed from: package-private */
    public boolean getSpeechLongPressHandle() {
        return this.mSpeechLongPressHandled;
    }

    /* access modifiers changed from: package-private */
    public void handlePowerKeyUpForWallet(boolean handled) {
        if (!handled) {
            sendCheckWalletMessage();
            return;
        }
        if (this.mOppoDebug) {
            Slog.d(TAG, "handlePowerKeyUpForWallet, handled!");
        }
        if (!this.mPowerConsumedByWakeup) {
            this.mClearPowerKeyDownRecord = true;
        }
    }

    /* access modifiers changed from: package-private */
    public void colorInterceptPowerKeyUp(KeyEvent event, boolean interactive, boolean canceld) {
        if (!getPowerKeyForbiddenState()) {
            notifyPowerKeyUpForSystemApp();
        }
    }

    /* access modifiers changed from: package-private */
    public void colorInterceptPowerKeyUpForWallet(KeyEvent event, boolean interactive, int powerKeyPressCounter) {
        sendGoToSleepDelayMessage(event, interactive);
    }

    public void onDefaultDisplayFocusChangedLw(WindowManagerPolicy.WindowState newFocus) {
        OppoPhoneWindowManager.super.onDefaultDisplayFocusChangedLw(newFocus);
        this.mFocusedWindow = newFocus;
        clearSosWaitCount();
    }

    public void systemBooted() {
        OppoPhoneWindowManager.super.systemBooted();
        this.mHandler.postDelayed(new Runnable() {
            /* class com.android.server.policy.OppoPhoneWindowManager.AnonymousClass17 */

            public void run() {
                OppoPhoneWindowManager.this.initForWallet();
            }
        }, WALLET_INIT_TIMEOUT);
    }

    /* access modifiers changed from: private */
    public class ColorPowerKeyHandler extends Handler {
        public ColorPowerKeyHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            int i = msg.what;
            boolean fromInteractive = true;
            if (i == OppoPhoneWindowManager.MSG_POWER_LONG_PRESS_FOR_SPEECH) {
                OppoPhoneWindowManager.this.mClearPowerKeyDownRecord = true;
                try {
                    OppoPhoneWindowManager.this.startSpeech((KeyEvent) msg.obj);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (i == OppoPhoneWindowManager.MSG_WALLET_CHECK) {
                removeMessages(OppoPhoneWindowManager.MSG_POWER_KEY_SLEEP_DELAY);
                OppoPhoneWindowManager.this.mClearPowerKeyDownRecord = true;
                OppoPhoneWindowManager.this.startWallet();
            } else if (i == OppoPhoneWindowManager.MSG_POWER_KEY_SLEEP_DELAY) {
                try {
                    if (!OppoPhoneWindowManager.this.isPowerKeyDownTimeMatchedSos()) {
                        KeyEvent event = (KeyEvent) msg.obj;
                        if (msg.arg1 != 1) {
                            fromInteractive = false;
                        }
                        boolean canceled = event.isCanceled();
                        long eventTime = event.getDownTime();
                        if (fromInteractive && !OppoPhoneWindowManager.this.mBeganFromNonInteractive && !canceled && !OppoPhoneWindowManager.this.mPowerKeyWakeLock.isHeld()) {
                            OppoPhoneWindowManager.this.getInner().powerPress(eventTime, fromInteractive, 0);
                        }
                    }
                } catch (Exception e2) {
                }
            }
        }
    }

    private boolean volumeUpDoubleClickBeforeQueueing(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if (!this.mSupportVolumeUpDoubleClick || keyCode != 24 || (event.getFlags() & SPEECH_START_TYPE_VALUE) != 0) {
            return false;
        }
        if (event.getAction() == 0) {
            long downTime = event.getDownTime();
            long lastDownTime = this.mVolumeUpDownTime;
            this.mVolumeUpDownTime = downTime;
            if (downTime - lastDownTime >= 300) {
                return false;
            }
            doubleClickVolumeUpAction();
            this.mConsumedVolumeUpDownTime = lastDownTime;
            this.mVolumeUpDownqueueed = false;
            this.mVolumeUpDownTime = 0;
            if (this.mOppoDebug) {
                Log.d(TAG, "interceptKeyBeforeQueueing ignore " + event);
            }
            return true;
        } else if (this.mVolumeUpDownqueueed) {
            return false;
        } else {
            this.mVolumeUpDownqueueed = true;
            if (this.mOppoDebug) {
                Log.d(TAG, "interceptKeyBeforeQueueing ignore " + event);
            }
            return true;
        }
    }

    private long volumeUpDoubleClickBeforeDispatching(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if (!this.mSupportVolumeUpDoubleClick || keyCode != 24 || (event.getFlags() & SPEECH_START_TYPE_VALUE) != 0) {
            return 0;
        }
        if (event.getAction() == 0) {
            if (this.mConsumedVolumeUpDownTime == event.getDownTime()) {
                this.mVolumeUpDownDispatched = false;
                if (this.mOppoDebug) {
                    Log.d(TAG, "interceptKeyBeforeDispatching double click consumed. consumedDownTime=" + this.mConsumedVolumeUpDownTime + ", ignore " + event);
                }
                return -1;
            }
            long now = SystemClock.uptimeMillis();
            long timeoutTime = this.mVolumeUpDownTime + 300;
            if (now < timeoutTime) {
                return timeoutTime - now;
            }
        } else if (!this.mVolumeUpDownDispatched) {
            this.mVolumeUpDownDispatched = true;
            if (this.mOppoDebug) {
                Log.d(TAG, "interceptKeyBeforeDispatching double click consumed. ignore " + event);
            }
            return -1;
        }
        return 0;
    }

    private void doubleClickVolumeUpAction() {
        Intent intent = new Intent();
        intent.setAction("oppo.intent.action.LAUNCH_INTERCOM_CUSTOM");
        try {
            this.mContext.sendBroadcastAsUser(intent, UserHandle.CURRENT);
        } catch (Exception e) {
            Log.w(TAG, "doubleClickVolumeUpAction: ", e);
        }
        Log.d(TAG, "doubleClickVolumeUpAction");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendIntercomAcquireRelaseUp(boolean acquire) {
        Intent intent = new Intent();
        intent.setAction("oppo.intent.action.LAUNCH_INTERCOM_CUSTOM");
        if (acquire) {
            intent.putExtra("action", "acquire");
        } else {
            intent.putExtra("action", "release");
        }
        try {
            this.mContext.sendBroadcastAsUser(intent, UserHandle.CURRENT);
        } catch (Exception e) {
            Log.w(TAG, "longPressVolumeUpAction: ", e);
        }
        Log.d(TAG, "longPressVolumeUpAction: acquire=" + acquire);
    }

    private void volumeUpLongPressBeforeQueueing(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if (this.mSupportVolumeUpLongPress && keyCode == 24 && (event.getFlags() & SPEECH_START_TYPE_VALUE) == 0) {
            if (event.getAction() == 0) {
                this.mIsVolumeUpKeyUp = false;
                long timeoutTime = event.getDownTime() + 300;
                long delay = timeoutTime - SystemClock.uptimeMillis();
                if (delay <= 0 || delay > 300) {
                    Log.d(TAG, "interceptKeyBeforeQueueing delay=" + delay + ", timeoutTime=" + timeoutTime + ", now=" + SystemClock.uptimeMillis());
                    return;
                }
                this.mHandler.postDelayed(this.mVolumeUpLongPress, delay);
                this.mWakeLockVolumeUpLongPress.acquire(300);
                return;
            }
            this.mIsVolumeUpKeyUp = true;
            this.mHandler.removeCallbacks(this.mVolumeUpLongPress);
            if (this.mIsVolumeUpLongPressed) {
                sendIntercomAcquireRelaseUp(false);
                this.mIsVolumeUpLongPressed = false;
            }
            if (this.mWakeLockVolumeUpLongPress.isHeld()) {
                this.mWakeLockVolumeUpLongPress.release();
            }
        }
    }

    private long volumeUpLongPressBeforeDispatching(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if (!this.mSupportVolumeUpLongPress || keyCode != 24 || (event.getFlags() & SPEECH_START_TYPE_VALUE) != 0) {
            return 0;
        }
        if ((event.getAction() == 0) && !this.mIsVolumeUpKeyUp) {
            long timeoutTime = event.getDownTime() + 300;
            long delay = timeoutTime - SystemClock.uptimeMillis();
            if (delay <= 0 || delay > 300) {
                if (delay <= 0) {
                    return -1;
                }
                Log.d(TAG, "interceptKeyBeforeDispatching delay=" + delay + ", timeoutTime=" + timeoutTime + ", now=" + SystemClock.uptimeMillis());
            } else if (delay < 50) {
                return delay;
            } else {
                return 50;
            }
        }
        return 0;
    }

    private void updateSettingsForOppoLocked(ContentResolver resolver) {
        try {
            boolean z = false;
            int isAgreeValue = INDIA_SOS ? 1 : Settings.Global.getInt(resolver, SOS_LICENSE, 0);
            int usePowerBtnValue = Settings.Global.getInt(resolver, USE_POWER_BTN, 1);
            Slog.i(TAG, "updateSettingsForOppoLocked isAgreeValue = " + isAgreeValue + ",usePowerBtnValue = " + usePowerBtnValue);
            if (usePowerBtnValue == 1 && isAgreeValue == 1) {
                z = true;
            }
            this.mSosPowerKeyEnabled = z;
        } catch (Exception e) {
            Slog.e(TAG, "isUsePowerBtnSwitchOn has an error.", e);
        }
        try {
            if (this.mIsExpVersion) {
                this.mSpeechEnabled = Settings.Secure.getInt(resolver, DISABLE_GOOGLE_ASSSIST_POWER_WAKEUP, -1);
            } else {
                this.mSpeechEnabled = Settings.Secure.getInt(resolver, SPEECH_POWER_KEY_ENABLED, 1);
            }
        } catch (Exception e2) {
            Slog.e(TAG, "got speech enable has an error.", e2);
        }
        Slog.i(TAG, "mSosPowerKeyEnabled = " + this.mSosPowerKeyEnabled + ",mSpeechEnabled = " + this.mSpeechEnabled + ",mIsExpVersion = " + this.mIsExpVersion + ", mWalletPowerKeyEnabled = " + this.mWalletPowerKeyEnabled + ",INDIA_SOS = " + INDIA_SOS);
        OppoBaseDisplayPolicy baseDp = typeCasting(this.mDefaultDisplayPolicy);
        if (baseDp != null) {
            baseDp.getDisplayPolicyEx().updateGestureStatus();
        }
    }

    private void registerSettingsForOppoLocked(ContentResolver resolver, ContentObserver observer) {
        this.mIsExpVersion = this.mContext.getPackageManager().hasSystemFeature("oppo.version.exp");
        INDIA_SOS = this.mContext.getPackageManager().hasSystemFeature("oppo.common_center.india.sos.special.function");
        if (INDIA_SOS) {
            SOS_POWER_KEY_DOWN_COUNT = 3;
            SOS_POWER_KEY_DOWN_INTERVAL = 1500;
        }
        resolver.registerContentObserver(Settings.Global.getUriFor(USE_POWER_BTN), false, observer, -1);
        resolver.registerContentObserver(Settings.Global.getUriFor(SOS_LICENSE), false, observer, -1);
        if (this.mIsExpVersion) {
            resolver.registerContentObserver(Settings.Secure.getUriFor(DISABLE_GOOGLE_ASSSIST_POWER_WAKEUP), false, observer, -1);
        } else {
            resolver.registerContentObserver(Settings.Secure.getUriFor(SPEECH_POWER_KEY_ENABLED), false, observer, -1);
        }
        resolver.registerContentObserver(Settings.Secure.getUriFor(STR_UP_GESTURE_MODE), false, observer, -1);
        resolver.registerContentObserver(Settings.Secure.getUriFor("manual_hide_navigationbar"), false, observer, -1);
        resolver.registerContentObserver(Settings.Secure.getUriFor("nav_bar_immersive"), false, observer, -1);
        resolver.registerContentObserver(Settings.System.getUriFor(SUPER_POWERSAVE_MODE_STATE), false, observer, 0);
        if (this.mContext.getPackageManager().hasSystemFeature("coloros.gesture.range.pinning.support")) {
            resolver.registerContentObserver(Settings.Secure.getUriFor("follow_rotation_gesture_bar_enable"), false, observer, -1);
        }
    }

    private void enqueuePowerKeyDownEvent(long downTime) {
        if (this.mSosPowerKeyEnabled || this.mWalletPowerKeyEnabled) {
            this.mColorPowerKeyHandler.removeMessages(MSG_POWER_KEY_SLEEP_DELAY);
            this.mColorPowerKeyHandler.removeMessages(MSG_WALLET_CHECK);
            synchronized (this.mPowerKeyDownTimes) {
                if (this.mClearPowerKeyDownRecord) {
                    if (this.mOppoDebug) {
                        Slog.d(TAG, "enqueuePowerKeyDownEvent clear");
                    }
                    this.mPowerKeyDownTimes.clear();
                    this.mClearPowerKeyDownRecord = false;
                }
                if (!getPowerKeyForbiddenState()) {
                    correctPowerKeyEventLocked(downTime);
                    if (this.mPowerKeyDownTimes.size() < SOS_POWER_KEY_DOWN_COUNT) {
                        this.mPowerKeyDownTimes.add(Long.valueOf(downTime));
                    } else {
                        this.mPowerKeyDownTimes.remove(0);
                        this.mPowerKeyDownTimes.add(Long.valueOf(downTime));
                    }
                } else if (this.mOppoDebug) {
                    Slog.d(TAG, "enqueuePowerKeyDownEvent met sos forbid");
                }
            }
        }
    }

    private void correctPowerKeyEventLocked(long downTime) {
        if (this.mPowerKeyDownTimes.size() > 0) {
            ArrayList<Long> arrayList = this.mPowerKeyDownTimes;
            if (downTime - arrayList.get(arrayList.size() - 1).longValue() >= 500) {
                this.mPowerKeyDownTimes.clear();
            }
        }
    }

    private boolean handlePowerKeyDownEventForSosEarly(long now) {
        boolean result = false;
        if (!this.mSosPowerKeyEnabled) {
            this.mPowerKeyForbidden = false;
            return false;
        }
        boolean timeRule = isPowerKeyDownTimeMatchedSos();
        if (timeRule) {
            this.mClearPowerKeyDownRecord = true;
        }
        if (now - this.mLaunchSosWaitStartTime > SOS_POWER_KEY_DOWN_INTERVAL) {
            this.mLaunchSosWaitCount = 0;
        }
        CharSequence tag = getTopApp();
        boolean isSosTopApp = tag != null && tag.toString().contains("com.oppo.sos.ui.EmergencyCallActivity");
        boolean focusRule = !isSosTopApp;
        if (isSosTopApp) {
            this.mPowerKeyForbidden = true;
        } else if (this.mLaunchSosWaitCount > 0) {
            this.mPowerKeyForbidden = true;
        } else {
            this.mPowerKeyForbidden = false;
        }
        if (this.mOppoDebug) {
            Slog.d(TAG, "timeRule = " + timeRule + ",focusRule = " + focusRule + ", mLaunchSosWaitCount = " + this.mLaunchSosWaitCount);
        }
        if (timeRule && focusRule && this.mLaunchSosWaitCount <= 0) {
            result = true;
        }
        if (this.mLaunchSosWaitCount > 0) {
            this.mLaunchSosWaitCount--;
        }
        return result;
    }

    private void handlePowerKeyDownEventForSosLate(boolean launchSos) {
        if (launchSos) {
            this.mClearPowerKeyDownRecord = true;
            this.mPowerKeyHandled = false;
            getInner().cancelPendingPowerKeyAction();
            try {
                this.mContext.startActivityAsUser(new Intent("oppo.intent.action.LAUNCH_SOS_HELPER"), UserHandle.CURRENT);
                this.mPowerKeyForbidden = true;
                this.mLaunchSosWaitCount = 3;
                this.mLaunchSosWaitStartTime = SystemClock.elapsedRealtime();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private CharSequence getTopApp() {
        WindowManagerPolicy.WindowState windowState = this.mFocusedWindow;
        if (windowState == null) {
            return null;
        }
        CharSequence tag = windowState.getAttrs().getTitle();
        if (tag == null || tag.length() <= 0) {
            return this.mFocusedWindow.getAttrs().packageName;
        }
        return tag;
    }

    private boolean getPowerKeyForbiddenState() {
        return this.mPowerKeyForbidden;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isPowerKeyDownTimeMatchedSos() {
        boolean result = false;
        synchronized (this.mPowerKeyDownTimes) {
            if (this.mPowerKeyDownTimes.size() == SOS_POWER_KEY_DOWN_COUNT) {
                result = true;
            }
        }
        return result;
    }

    private void clearSosWaitCount() {
        if (this.mSosPowerKeyEnabled && this.mLaunchSosWaitCount > 0) {
            CharSequence tag = getTopApp();
            if (tag != null && tag.toString().contains("com.oppo.sos.ui.EmergencyCallActivity")) {
                this.mLaunchSosWaitCount = 0;
            }
        } else if (this.mPowerKeyForbidden) {
            this.mPowerKeyForbidden = false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startSpeech(final KeyEvent event) {
        if (this.mOppoDebug) {
            Slog.d(TAG, "startSpeech mSpeechLongPressHandled=" + this.mSpeechLongPressHandled);
        }
        if (!this.mSpeechLongPressHandled) {
            this.mSpeechLongPressHandled = true;
            if (isInChildSpace()) {
                Log.d(TAG, "startSpeech: is children space. ignore");
            } else if (!isUserSetupComplete()) {
                Log.d(TAG, "startSpeech: isUserSetupComplete fail. ignore");
            } else if (this.mSuperPowerSaveMode) {
                Log.d(TAG, "startSpeech: failed. super power save ignore");
            } else if (!this.mIsExpVersion) {
                Intent intent = new Intent(SPEECH_ACTION_NAME);
                intent.setComponent(new ComponentName(SPEECH_PACKAGE_NAME, SPEECH_SERVICE_NAME));
                intent.addFlags(268435456);
                intent.putExtra("caller_package", "android");
                intent.putExtra("start_type", SPEECH_START_TYPE_VALUE);
                try {
                    this.mContext.startForegroundServiceAsUser(intent, UserHandle.CURRENT);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                this.mClearPowerKeyDownRecord = true;
            } else if (keyguardOn()) {
                if (this.mSpeechEnabled != -1) {
                    try {
                        performHapticFeedback(Process.myUid(), this.mContext.getOpPackageName(), 0, true, "Speech - Long Press");
                        IWindowManager.Stub.asInterface(ServiceManager.getService("window")).dismissKeyguard(new IKeyguardDismissCallback.Stub() {
                            /* class com.android.server.policy.OppoPhoneWindowManager.AnonymousClass19 */

                            public void onDismissError() throws RemoteException {
                            }

                            public void onDismissSucceeded() throws RemoteException {
                                OppoPhoneWindowManager.this.mColorPowerKeyHandler.post(new Runnable() {
                                    /* class com.android.server.policy.OppoPhoneWindowManager.AnonymousClass19.AnonymousClass1 */

                                    public void run() {
                                        OppoPhoneWindowManager.this.getInner().launchAssistAction((String) null, event.getDeviceId(), OppoBasePhoneWindowManager.AssistManagerLaunchMode.DEFAULT);
                                    }
                                });
                            }

                            public void onDismissCancelled() throws RemoteException {
                            }
                        }, "");
                    } catch (Exception e2) {
                    }
                }
            } else if (this.mSpeechEnabled == -1) {
                Intent intent2 = new Intent("oppo.intent.action.ASSSIST_GUIDE");
                intent2.setPackage("com.android.settings");
                intent2.addFlags(268435456);
                try {
                    this.mContext.startActivityAsUser(intent2, UserHandle.CURRENT);
                    performHapticFeedback(Process.myUid(), this.mContext.getOpPackageName(), 0, true, "Speech - Long Press");
                } catch (Exception e3) {
                    e3.printStackTrace();
                }
            } else {
                performHapticFeedback(Process.myUid(), this.mContext.getOpPackageName(), 0, true, "Speech - Long Press");
                getInner().launchAssistAction((String) null, event.getDeviceId(), OppoBasePhoneWindowManager.AssistManagerLaunchMode.DEFAULT);
            }
        } else {
            Slog.w(TAG, "speech is already start up");
        }
    }

    private boolean isSpeechDisabled() {
        if (!this.mIsExpVersion || this.mSpeechEnabled != 1) {
            return false;
        }
        return true;
    }

    private void sendSpeechMessage(KeyEvent event) {
        if (this.mOppoDebug) {
            Slog.d(TAG, "sendSpeechMessage mSpeechEnabled =" + isSpeechDisabled());
        }
        if (isUserSetupComplete() && Settings.Global.getInt(this.mContext.getContentResolver(), "oppo_show_device_locked", 0) == 0 && Settings.Secure.getInt(this.mContext.getContentResolver(), BREENO_DISABLE_KEY, 0) == 0 && !isSpeechDisabled()) {
            this.mColorPowerKeyHandler.removeMessages(MSG_POWER_LONG_PRESS_FOR_SPEECH);
            Message msg = this.mColorPowerKeyHandler.obtainMessage(MSG_POWER_LONG_PRESS_FOR_SPEECH);
            msg.obj = new KeyEvent(event);
            msg.setAsynchronous(true);
            this.mColorPowerKeyHandler.sendMessageDelayed(msg, 500);
            this.mSpeechLongPressHandled = false;
        }
    }

    private void notifyPowerKeyUpForSystemApp() {
        if (this.mOppoDebug) {
            Slog.d(TAG, "notifyPowerKeyUpForSystemApp mSpeechLongPressHandled=" + this.mSpeechLongPressHandled);
        }
        if (this.mIsExpVersion) {
            this.mSpeechLongPressHandled = false;
            return;
        }
        if (this.mSpeechLongPressHandled) {
            Settings.Secure.putLong(this.mContext.getContentResolver(), "power_key_up_time", SystemClock.elapsedRealtime());
        } else {
            this.mColorPowerKeyHandler.removeMessages(MSG_POWER_LONG_PRESS_FOR_SPEECH);
        }
        this.mSpeechLongPressHandled = false;
    }

    private void sendMessageBeforeGlobalAction() {
        Slog.d(TAG, "sendMessageBeforeGlobalAction");
        Intent intent = new Intent("com.oppo.poweroff.screen.show");
        intent.setFlags(268435456);
        this.mContext.sendBroadcast(intent, "oppo.permission.OPPO_COMPONENT_SAFE");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0028, code lost:
        r4.mColorPowerKeyHandler.sendMessageDelayed(r4.mColorPowerKeyHandler.obtainMessage(com.android.server.policy.OppoPhoneWindowManager.MSG_WALLET_CHECK), com.android.server.policy.OppoPhoneWindowManager.WALLET_POWER_KEY_CHECK_AFTER_UP);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0039, code lost:
        if (r4.mOppoDebug == false) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x003b, code lost:
        android.util.Slog.d(com.android.server.policy.OppoPhoneWindowManager.TAG, "send wallet check message");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:?, code lost:
        return;
     */
    private void sendCheckWalletMessage() {
        if (this.mIsExpVersion || !this.mSupportNfc) {
            if (this.mOppoDebug) {
                Slog.d(TAG, "send check wallet message failed by exp version or not support nfc");
            }
        } else if (this.mWalletPowerKeyEnabled) {
            synchronized (this.mPowerKeyDownTimes) {
                if (this.mPowerKeyDownTimes.size() != 2) {
                }
            }
        } else if (this.mOppoDebug) {
            Slog.d(TAG, "send check wallet message failed by switch close");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startWallet() {
        this.mPowerKeyHandled = false;
        getInner().cancelPendingPowerKeyAction();
        Intent intent = new Intent(WALLET_ACTION_NAME);
        intent.putExtra(KEY_OPEN_REQUEST_PACKAGENAME, SOURCE_REQUEST_FRAMWORK_QUICKSTART);
        intent.putExtra(KEY_OPEN_REQUEST_IS_LOCKSCREEN, keyguardOn());
        intent.setPackage(WALLET_PACKAGE_NAME);
        intent.addFlags(268435456);
        try {
            if (this.mOppoDebug) {
                Slog.d(TAG, "start wallet!");
            }
            this.mContext.startActivityAsUser(intent, UserHandle.CURRENT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendGoToSleepDelayMessage(KeyEvent event, boolean interactive) {
        if (this.mIsExpVersion || !this.mSupportNfc || (!this.mSosPowerKeyEnabled && !this.mWalletPowerKeyEnabled)) {
            getInner().powerPress(event.getDownTime(), interactive, this.mPowerKeyPressCounter);
            return;
        }
        Message msg = this.mColorPowerKeyHandler.obtainMessage(MSG_POWER_KEY_SLEEP_DELAY);
        msg.arg1 = interactive ? 1 : 0;
        msg.obj = new KeyEvent(event);
        this.mColorPowerKeyHandler.sendMessageDelayed(msg, POWER_KEY_SLEEP_DELAY);
        if (this.mOppoDebug) {
            Slog.d(TAG, "send msg sleep delay");
        }
    }

    /* access modifiers changed from: private */
    public class WalletContentObserver extends ContentObserver {
        public WalletContentObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange, Uri uri) {
            boolean oldValue = OppoPhoneWindowManager.this.mWalletPowerKeyEnabled;
            OppoPhoneWindowManager oppoPhoneWindowManager = OppoPhoneWindowManager.this;
            oppoPhoneWindowManager.mWalletPowerKeyEnabled = oppoPhoneWindowManager.checkSupportFlag(oppoPhoneWindowManager.mContext);
            if (oldValue == OppoPhoneWindowManager.this.mWalletPowerKeyEnabled) {
                return;
            }
            if (!oldValue) {
                OppoPhoneWindowManager.this.updateFadeOffDuration(OppoPhoneWindowManager.FADE_OFF_ANIMATION_DURATION);
            } else {
                OppoPhoneWindowManager.this.updateFadeOffDuration(-1);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x007c, code lost:
        if (r8 != null) goto L_0x008b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0089, code lost:
        if (0 == 0) goto L_0x008f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x008b, code lost:
        r8.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x008f, code lost:
        return false;
     */
    public boolean checkSupportFlag(Context context) {
        String[] projection = {"SUPPORT_QUICK_START"};
        Cursor c = null;
        try {
            ContentResolver resolver = context.getContentResolver();
            IContentProvider provider = resolver.acquireUnstableProvider(KEY_OPEN_WALLET_CP_URI);
            Slog.d(TAG, "got provider " + provider);
            if (provider == null) {
                if (0 != 0) {
                    c.close();
                }
                return false;
            }
            c = resolver.query(KEY_OPEN_WALLET_CP_URI, projection, null, null, null);
            Slog.d(TAG, "got something from wallet");
            this.mGotSwitchError = false;
            if (c != null && c.getCount() > 0) {
                c.moveToFirst();
                String support = c.getString(c.getColumnIndex(projection[0]));
                Slog.d(TAG, "get switch from wallet " + support);
                boolean equals = TextUtils.equals("true", support);
                c.close();
                return equals;
            }
        } catch (Throwable th) {
            if (0 != 0) {
                c.close();
            }
            throw th;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initForWallet() {
        if (!this.mIsExpVersion) {
            this.mSupportNfc = this.mContext.getPackageManager().hasSystemFeature("android.hardware.nfc");
            if (this.mSupportNfc) {
                if (!SystemProperties.getBoolean("persist.sys.pkwallet.support", false)) {
                    SystemProperties.set("persist.sys.pkwallet.support", "true");
                }
                boolean oldValue = this.mWalletPowerKeyEnabled;
                this.mWalletPowerKeyEnabled = checkSupportFlag(this.mContext);
                if (this.mOppoDebug) {
                    Slog.d(TAG, "mWalletPowerKeyEnabled=" + this.mWalletPowerKeyEnabled + " , mGotSwitchError = " + this.mGotSwitchError);
                }
                IntentFilter filter = new IntentFilter();
                filter.addAction("android.intent.action.PACKAGE_DATA_CLEARED");
                filter.addDataScheme(BrightnessConstants.AppSplineXml.TAG_PACKAGE);
                this.mContext.registerReceiverAsUser(new BroadcastReceiver() {
                    /* class com.android.server.policy.OppoPhoneWindowManager.AnonymousClass20 */

                    public void onReceive(Context context, Intent intent) {
                        if (intent != null && intent.getData() != null && TextUtils.equals(intent.getData().getSchemeSpecificPart(), OppoPhoneWindowManager.WALLET_PACKAGE_NAME)) {
                            if (OppoPhoneWindowManager.this.mOppoDebug) {
                                Slog.d(OppoPhoneWindowManager.TAG, "receive broadcast for clear wallet");
                            }
                            boolean oldValue = OppoPhoneWindowManager.this.mWalletPowerKeyEnabled;
                            OppoPhoneWindowManager.this.mWalletPowerKeyEnabled = false;
                            if (oldValue) {
                                OppoPhoneWindowManager.this.updateFadeOffDuration(-1);
                            }
                        }
                    }
                }, UserHandle.ALL, filter, null, this.mColorPowerKeyHandler);
                if (this.mGotSwitchError) {
                    if (this.mFirstUserLocked == 3 && this.mUnlockReceiver != null) {
                        IntentFilter unLockFilter = new IntentFilter();
                        unLockFilter.addAction("android.intent.action.USER_PRESENT");
                        this.mContext.registerReceiverAsUser(this.mUnlockReceiver, UserHandle.ALL, unLockFilter, null, this.mColorPowerKeyHandler);
                    }
                    this.mFirstUserLocked--;
                    return;
                }
                if (oldValue != this.mWalletPowerKeyEnabled) {
                    if (!oldValue) {
                        updateFadeOffDuration(FADE_OFF_ANIMATION_DURATION);
                    } else {
                        updateFadeOffDuration(-1);
                    }
                }
                try {
                    this.mContext.getContentResolver().registerContentObserver(KEY_OPEN_WALLET_CP_URI, true, new WalletContentObserver(this.mColorPowerKeyHandler));
                    Slog.d(TAG, "register content observer for wallet");
                } catch (Throwable th) {
                    Slog.d(TAG, "cat not register content observer for wallet");
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateFadeOffDuration(long duration) {
    }

    private boolean isInChildSpace() {
        if (!this.mContext.getPackageManager().hasSystemFeature(FEATURE_CHILDREN_MODE) || Settings.Global.getInt(this.mContext.getContentResolver(), SETTING_CHILDREN_MODE, 0) != 1) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean colorInterceptPowerKeyForTelephone(KeyEvent event, boolean interactive) {
        boolean hungUp = false;
        TelecomManager telecomManager = getTelecommService();
        if (telecomManager != null) {
            if (telecomManager.isRinging()) {
                boolean inSilence = ((AudioManager) this.mContext.getSystemService("audio")).getStreamVolume(2) == 0;
                boolean isNeverVibrate = Settings.System.getInt(this.mContext.getContentResolver(), "call_vibrate_method", 1) == 1;
                Log.d(TAG, "inSilence::" + inSilence + ", isNeverVibrate::" + isNeverVibrate + " mIsMute=" + this.mIsMute.get());
                if (this.mIsMute.get() || (inSilence && isNeverVibrate)) {
                    hungUp = telecomManager.endCall();
                    this.mIsMute.set(false);
                } else {
                    this.mIsMute.set(true);
                    hungUp = true;
                    telecomManager.silenceRinger();
                }
                this.mContext.sendBroadcastAsUser(new Intent("SILENCE_ACTION_FOR_OPPO_SPEECH"), UserHandle.CURRENT);
                if (!interactive) {
                    getInner().wakeUpFromPowerKey(event.getDownTime());
                }
            } else if ((this.mIncallPowerBehavior & 2) != 0 && telecomManager.isInCall()) {
                hungUp = telecomManager.endCall();
                this.mIsMute.set(false);
                Log.d(TAG, "interceptPowerKeyForTelephone: telecomManager.endCall()==> " + hungUp);
                this.mContext.sendBroadcastAsUser(new Intent("SILENCE_ACTION_FOR_OPPO_SPEECH"), UserHandle.CURRENT);
                if (!interactive) {
                    getInner().wakeUpFromPowerKey(event.getDownTime());
                }
            } else if (this.mIncallPowerButtonHangup == 1 && telecomManager.isInCall() && interactive) {
                hungUp = telecomManager.endCall();
                Log.d(TAG, "interceptPowerKeyForTelephone: 222 power key endCall()==> " + hungUp);
                this.mContext.sendBroadcastAsUser(new Intent("SILENCE_ACTION_FOR_OPPO_SPEECH"), UserHandle.CURRENT);
            }
        }
        if (AudioSystem.isStreamActive(5, 0)) {
            disableNotificationAlert();
        }
        return hungUp;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void toggleSplitScreen() {
        Log.i(TAG, "toggleSplitScreen()");
        try {
            new ColorStatusBarManager().toggleSplitScreen(2);
        } catch (RemoteException e) {
            Log.e(TAG, " toggleSplitScreen error e = " + e);
        }
    }

    /* access modifiers changed from: protected */
    public boolean isMenuLongPressed() {
        return this.mRecentsLongPressDetected.get();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean stopLockTaskMode() {
        return false;
    }

    private boolean isInLockTaskMode() {
        try {
            if (ActivityManagerNative.getDefault().isInLockTaskMode()) {
                return true;
            }
            return false;
        } catch (RemoteException e) {
            return false;
        }
    }

    public void disableNotificationAlert() {
        try {
            IStatusBarService svc = getStatusBarService();
            if (svc != null) {
                svc.disable(262144, this.mToken, this.mContext.getPackageName());
                svc.disable(0, this.mToken, this.mContext.getPackageName());
            }
        } catch (Exception e) {
            Slog.d(TAG, "disableNotificationAlert failed, e:" + e.toString());
        }
    }

    /* access modifiers changed from: package-private */
    public void colorInterceptPowerKeyForAlarm() {
        if (this.mPowerButtonEndsAlarmclock == 1) {
            Intent intent = new Intent(ACTION_POWER_BUTTON_ENDS_ALARMCLOCK);
            intent.setPackage("com.coloros.alarmclock");
            this.mContext.sendBroadcast(intent, "oppo.permission.OPPO_COMPONENT_SAFE");
            if (this.mOppoDebug) {
                Slog.d(TAG, "interceptPowerKeyDown: send ends alarmclock");
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean colorInterceptLongPowerPress() {
        if (!this.mDefaultDisplayPolicy.isAwake()) {
            Log.i(TAG, "LONG_PRESS_POWER_GLOBAL_ACTIONS. screen is not on!");
            return true;
        }
        performHapticFeedback(Process.myUid(), this.mContext.getOpPackageName(), 5, true, "Power - Long Press - Global Actions");
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean colorInterceptLongHomePress() {
        if (!isInChildSpace()) {
            return false;
        }
        Log.d(TAG, "handleLongPressOnHome: is children space. ignore");
        return true;
    }

    /* access modifiers changed from: package-private */
    public int colorUpdateConfigurationDependentBehaviors(int oldValue) {
        if (isExpROM()) {
            return 2;
        }
        return oldValue;
    }

    /* access modifiers changed from: package-private */
    public boolean colorInterceptAppSwitchEventBeforeQueueing(KeyEvent event, boolean oldValue) {
        int keyCode = event.getKeyCode();
        if ((event.getAction() == 0) && event.getRepeatCount() == 0) {
            oldValue &= true;
        }
        OppoDisplayManagerInternal oppoDisplayManagerInternal = this.mOppoDisplayManagerInternal;
        if (oppoDisplayManagerInternal != null && oppoDisplayManagerInternal.isBlockScreenOnByBiometrics()) {
            oldValue &= false;
        }
        if (event.getDownTime() - event.getEventTime() == ((long) keyCode)) {
            return oldValue & false;
        }
        return oldValue;
    }

    /* access modifiers changed from: package-private */
    public void oppoHandleAssistLaunchMode(OppoBasePhoneWindowManager.AssistManagerLaunchMode launchMode, Bundle args) {
        if (launchMode != null) {
            if (args == null) {
                args = new Bundle();
            }
            if (this.mOppoDebug) {
                Slog.d(TAG, "launch assist action with " + launchMode);
            }
            args.putInt("assistant_launch_mode", launchMode.ordinal());
        }
    }

    public void requestKeyguard(String command) {
        if (this.mKeyguardDelegate != null && OppoMirrorKeyguardServiceDelegate.requestKeyguard != null) {
            OppoMirrorKeyguardServiceDelegate.requestKeyguard.call(this.mKeyguardDelegate, new Object[]{command});
        }
    }

    public void dump(String prefix, PrintWriter pw, String[] args) {
        if (args.length == 3 && "log".equals(args[1])) {
            if ("on".equals(args[2])) {
                this.mOppoDebug = true;
                pw.println("OppoPhoneWindowManager log on!");
            } else if ("off".equals(args[2])) {
                this.mOppoDebug = false;
                pw.println("OppoPhoneWindowManager log off!");
            }
        }
        synchronized (this.mKeyLockIntentProcess) {
            if (this.mKeyLockIntentProcess.size() == 0) {
                pw.println("no KeyLockProcess.");
            } else {
                for (int i = 0; i < this.mKeyLockIntentProcess.size(); i++) {
                    pw.print("KeyLockProcess : ");
                    pw.println(this.mKeyLockIntentProcess.get(i));
                }
            }
        }
        pw.print("mDisableBottomKeyMode : ");
        pw.println(this.mDisableBottomKeyMode);
        OppoPhoneWindowManager.super.dump(prefix, pw, args);
    }

    private static ColorBaseLayoutParams typeCasting(WindowManager.LayoutParams lp) {
        if (lp != null) {
            return (ColorBaseLayoutParams) ColorTypeCastingHelper.typeCasting(ColorBaseLayoutParams.class, lp);
        }
        return null;
    }

    private static OppoBaseDisplayPolicy typeCasting(DisplayPolicy dp) {
        if (dp != null) {
            return (OppoBaseDisplayPolicy) ColorTypeCastingHelper.typeCasting(OppoBaseDisplayPolicy.class, dp);
        }
        return null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private IOppoPhoneWindowManagerInner getInner() {
        OppoBasePhoneWindowManager basePhoneWindowManager = (OppoBasePhoneWindowManager) ColorTypeCastingHelper.typeCasting(OppoBasePhoneWindowManager.class, this);
        if (basePhoneWindowManager == null || basePhoneWindowManager.mInner == null) {
            return IOppoPhoneWindowManagerInner.DEFAULT;
        }
        return basePhoneWindowManager.mInner;
    }

    /* access modifiers changed from: package-private */
    public boolean checkStartingWindowDrawable(Drawable originDrawable, boolean windowIsTranslucent) {
        boolean hasWindowBackground = originDrawable != null;
        if (this.mOppoDebug) {
            Slog.d(TAG, "checkStartingWindowDrawable: hasWindowBackground =:" + hasWindowBackground + ",windowIsTranslucent =:" + windowIsTranslucent + ",originDrawable =: " + originDrawable);
        }
        if (hasWindowBackground) {
            if (originDrawable instanceof ColorDrawable) {
                int colorAlpha = originDrawable.getAlpha();
                if (this.mOppoDebug) {
                    Slog.d(TAG, "addSplashScreen: colorAlpha =:" + colorAlpha);
                }
                if (colorAlpha != 0) {
                    if (isNightMode(this.mContext)) {
                        int color = ((ColorDrawable) originDrawable).getColor();
                        if (this.mOppoDebug) {
                            Slog.d(TAG, "in night mode, we should handle windowbackgroud");
                        }
                        this.mStartingWindowDrawable = new ColorDrawable(makeDark(color));
                    } else {
                        this.mStartingWindowDrawable = originDrawable;
                    }
                }
            } else {
                this.mStartingWindowDrawable = originDrawable;
            }
        }
        return hasWindowBackground;
    }

    /* access modifiers changed from: package-private */
    public void handleStartingWindow(PhoneWindow window) {
        Drawable curDrawable;
        if (this.mOppoDebug) {
            Trace.traceBegin(32, "handleStartingWindow");
        }
        long startTime = System.currentTimeMillis();
        WindowManager.LayoutParams params = window.getAttributes();
        if (this.mStartingWindowDrawable == null || ColorStartingWindowUtils.forUseColorDrawableForSplashWindow(params.packageName)) {
            if ((params.flags & 1048576) != 0 || ColorStartingWindowUtils.useTranslucentDrawableForSplashWindow(params.packageName)) {
                this.mStartingWindowDrawable = new ColorDrawable(0);
            } else {
                this.mStartingWindowDrawable = isNightMode(this.mContext) ? new ColorDrawable(-16777216) : new ColorDrawable(-1);
            }
        }
        boolean transparentSystemBar = false;
        Drawable drawable = this.mStartingWindowDrawable;
        if ((drawable instanceof ColorDrawable) || (drawable instanceof StateListDrawable)) {
            window.requestFeature(1);
            params.flags |= 512;
            transparentSystemBar = true;
            BitmapDrawable appSnapshot = OppoFeatureCache.get(IColorStartingWindowManager.DEFAULT).getAppSnapshotBitmapDrawable(window);
            if (this.mOppoDebug) {
                Slog.d(TAG, "handleStartingWindow ==> appSnapshot =: " + appSnapshot);
            }
            if (appSnapshot != null) {
                this.mStartingWindowDrawable = appSnapshot;
            }
        } else if (drawable instanceof BitmapDrawable) {
            transparentSystemBar = true;
            OppoFeatureCache.get(IColorStartingWindowManager.DEFAULT).setAllowAppSnapshot(false);
        } else {
            OppoFeatureCache.get(IColorStartingWindowManager.DEFAULT).setAllowAppSnapshot(false);
        }
        if (transparentSystemBar) {
            window.setStatusBarColor(0);
            window.setNavigationBarColor(0);
        }
        if (this.mOppoDebug) {
            Slog.d(TAG, "handleStartingWindow ==> mStartingWindowDrawable =: " + this.mStartingWindowDrawable);
            Drawable drawable2 = this.mStartingWindowDrawable;
            if (drawable2 != null) {
                if (drawable2 instanceof ColorDrawable) {
                    Slog.d(TAG, "handleStartingWindow ==> mStartingWindowDrawable.color =: " + Integer.toHexString(((ColorDrawable) this.mStartingWindowDrawable).getColor()));
                }
                Drawable drawable3 = this.mStartingWindowDrawable;
                if ((drawable3 instanceof StateListDrawable) && (curDrawable = ((StateListDrawable) drawable3).getCurrent()) != null && (curDrawable instanceof ColorDrawable)) {
                    Slog.d(TAG, "handleStartingWindow ==> curDrawable.color =: " + Integer.toHexString(((ColorDrawable) curDrawable).getColor()));
                }
            }
        }
        window.setBackgroundDrawable(this.mStartingWindowDrawable);
        this.mStartingWindowDrawable = null;
        Slog.d(TAG, "handleStartingWindow spend time =: " + (System.currentTimeMillis() - startTime));
        if (this.mOppoDebug) {
            Trace.traceEnd(32);
        }
    }

    /* access modifiers changed from: package-private */
    public void handleStartingWindowAttrs(PhoneWindow window) {
        if (window != null) {
            boolean windowIsFloating = window.isFloating();
            if (this.mOppoDebug) {
                Slog.d(TAG, "handleStartingWindowAttrs windowIsFloating =:" + windowIsFloating);
            }
            if (windowIsFloating) {
                WindowManager.LayoutParams params = window.getAttributes();
                params.flags |= 256;
                params.flags |= Integer.MIN_VALUE;
                window.setLayout(-1, -1);
            }
            handleStatusbarForStartingWindow(window.getDecorView());
        }
    }

    /* access modifiers changed from: package-private */
    public void handleStatusbarForStartingWindow(View view) {
        int vis;
        if (view != null) {
            boolean isNightMode = isNightMode(view.getContext());
            if (this.mOppoDebug) {
                Slog.d(TAG, "handleStatusbarForStartingWindow isNightMode =: " + isNightMode);
            }
            int vis2 = view.getSystemUiVisibility();
            if (!isNightMode) {
                vis = vis2 | 8192;
            } else {
                vis = vis2 & -8193;
            }
            view.setSystemUiVisibility(vis);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isNightMode(Context context) {
        UiModeManager uiModeManager;
        if (context == null || (uiModeManager = (UiModeManager) context.getSystemService("uimode")) == null || uiModeManager.getNightMode() != 2) {
            return false;
        }
        return true;
    }

    public static int makeDark(int color) {
        double[] lab = new double[3];
        ColorUtils.colorToLAB(color, lab);
        double newL = 100.0d - lab[0];
        if (newL >= lab[0]) {
            return color;
        }
        lab[0] = newL;
        int newColor = ColorUtils.LABToColor(lab[0], lab[1], lab[2]);
        return Color.argb(Color.alpha(color), Color.red(newColor), Color.green(newColor), Color.blue(newColor));
    }

    public void onWakeUp(String wakeUpReason) {
        Slog.d(TAG, "onWakeUp for:" + wakeUpReason);
        if (this.mKeyguardDelegate == null) {
            Slog.e(TAG, "onWakeUp failed without KeyguardDelegate for:" + wakeUpReason);
        } else if (OppoMirrorKeyguardServiceDelegate.onWakeUp != null) {
            OppoMirrorKeyguardServiceDelegate.onWakeUp.call(this.mKeyguardDelegate, new Object[]{wakeUpReason});
        }
    }

    public void setCurrentUserLw(int newUserId) {
        OppoPhoneWindowManager.super.setCurrentUserLw(newUserId);
        if (this.mOppoDebug) {
            Log.d(TAG, "userId changed, oldUserId: " + this.mCurrentUserId + ", newUserId: " + newUserId);
        }
        this.mCurrentUserId = newUserId;
    }

    private void quickLaunchCamera(long wakeTime) {
        if (this.mOppoDebug) {
            Log.d(TAG, "Quick launch camera");
        }
        Intent intent = new Intent();
        intent.setPackage(CAMERA_PKG);
        if (!isKeyguardSecure(this.mCurrentUserId) || !isKeyguardLocked()) {
            intent.setAction("android.media.action.STILL_IMAGE_CAMERA");
        } else {
            intent.setAction("android.media.action.STILL_IMAGE_CAMERA_SECURE");
        }
        intent.addFlags(872415232);
        intent.putExtra(CAMERA_LAUNCH_TYPE, QUICK_LAUNCH_CAMERA);
        if (this.mSystemReady) {
            enableProximitySensor();
        }
        this.mHandler.postDelayed(new Runnable(intent) {
            /* class com.android.server.policy.$$Lambda$OppoPhoneWindowManager$vYgbE6BYTqV1mB1xerYHE8qauE */
            private final /* synthetic */ Intent f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                OppoPhoneWindowManager.this.lambda$quickLaunchCamera$0$OppoPhoneWindowManager(this.f$1);
            }
        }, 350);
    }

    public /* synthetic */ void lambda$quickLaunchCamera$0$OppoPhoneWindowManager(Intent intent) {
        if (!this.mProximitySensorActive) {
            this.mContext.startActivityAsUser(intent, UserHandle.CURRENT);
        }
        disableProximitySensor();
    }

    private void interceptGimbalPowerKey(String launchPkg) {
        if (this.mOppoDebug) {
            Log.d(TAG, "gimbal launch package name: " + launchPkg);
        }
        Intent intent = new Intent();
        intent.setPackage(launchPkg);
        intent.setAction("android.media.action.STILL_IMAGE_CAMERA");
        intent.addFlags(872415232);
        intent.putExtra(CAMERA_LAUNCH_TYPE, GIMBAL_LAUNCH_CAMERA);
        try {
            this.mContext.startActivityAsUser(intent, UserHandle.CURRENT);
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "interceptGimbalPowerKey failed " + e);
        }
    }
}
