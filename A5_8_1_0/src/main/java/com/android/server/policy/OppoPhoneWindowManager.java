package com.android.server.policy;

import android.app.IColorKeyguardSessionCallback;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.SystemSensorManager;
import android.media.AudioSystem;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.Vibrator;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.telecom.TelecomManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.OppoSafeDbReader;
import android.util.Slog;
import android.view.IOppoWindowManagerImpl;
import android.view.IWindowManager;
import android.view.KeyEvent;
import android.view.OppoScreenDragUtil;
import android.view.OppoScreenShotUtil;
import android.view.OppoWindowManagerPolicy;
import android.view.WindowManager.LayoutParams;
import android.view.WindowManagerPolicy.ScreenOnListener;
import android.view.WindowManagerPolicy.WindowManagerFuncs;
import android.view.WindowManagerPolicy.WindowState;
import android.widget.Toast;
import com.android.internal.statusbar.IStatusBarService;
import com.android.server.am.OppoPhoneStateReceiver;
import com.android.server.coloros.OppoSysStateManager;
import com.color.app.ColorBootMessageDialog;
import com.color.view.ColorWindowManager;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;

public class OppoPhoneWindowManager extends PhoneWindowManager implements OppoWindowManagerPolicy {
    private static final String ACTION_BOOT_IPO = "android.intent.action.ACTION_BOOT_IPO";
    private static final String ACTION_DISABLE_BOTTOM_KEY_MODE = "com.oppo.intent.action.DISABLE_BOTTOM_KEY_MODE";
    private static final String ACTION_END_CALL = "android.intent.action.END_CALL";
    private static final String ACTION_KEY_LOCK = "com.oppo.intent.action.KEY_LOCK_MODE";
    private static final String ACTION_SCREEN_SHOT = "oppo.intent.action.SCREEN_SHOT";
    private static final String ACTION_SHUTDOWN_IPO = "android.intent.action.ACTION_SHUTDOWN_IPO";
    private static final int ARGS_LENGTH = 3;
    private static final long BOTTOM_KEY_CLICK_INTERVAL = 2500;
    private static final String CAMERA_PKG = "com.oppo.camera";
    private static final String CAPTURE_GUIDE = "com.oppo.gestureguide.activity.CaptureActivity";
    private static final String CTS_PROJECTION_TOUCH = "com.android.cts.verifier.projection.touch.ProjectionTouchActivity";
    private static final long DEFALUT_LONG_VIBRATION_DURING_TIME = 300;
    private static final long DEFALUT_TOUCH_FEEDBACK_10 = 10;
    private static final long DEFALUT_TOUCH_FEEDBACK_20 = 20;
    private static final long DEFALUT_TOUCH_FEEDBACK_30 = 30;
    private static final int DELAY_REG_PHONESTATE = 500;
    private static final int DURATION_START_SPEECH = 750;
    private static final String FEATURE_CHILDREN_MODE = "oppo.childspace.support";
    private static final String KEY_EXIT_DRAG_WINDOW = "com.oppo.intent.action.KEY_EXIT_DRAG_WINDOW";
    private static final int KEY_LOCK_MODE_HOME = 3;
    private static final int KEY_LOCK_MODE_NORMAL = 0;
    private static final int KEY_LOCK_MODE_POWER = 1;
    private static final int KEY_LOCK_MODE_POWER_HOME = 2;
    static final int KEY_OFFSET_VALUE = 800;
    private static final String LAYER_WALLPAPER = "LAYER_WALLPAPER";
    private static final long LID_WAKELOCK_TIMEOUT = 2000;
    private static final int MAX_COUNT_REG_PHONESTATE = 40;
    private static final int MAX_WAIT_TIME = 1000;
    private static final int MSG_ISCAMERAMODE = 17;
    private static final int MSG_ISHOMEMODE = 16;
    private static final int MSG_ISINGESTUREGUIDE = 19;
    private static final long MSG_ISINGESTUREGUIDE_AND_ISCAMERAMODE = 50;
    private static final int MSG_SET_WALLPAPER_LAYER = 18;
    private static final String MULTI_TOUCH_GUIDE = "com.oppo.gestureguide.activity.StartMultiTouchOpenCamera";
    private static final String OPPO_IGNORE_DRIVE_MODE = "com.oppo.drivemode";
    private static final String OPPO_IGNORE_INCALL_SCREEN = "com.android.incallui/com.android.incallui.OppoSlidingDrawerInCallActivity";
    private static final String OPPO_IGNORE_SPEECH_ASSIST = "com.coloros.speechassist";
    private static final String OPPO_INCALL_SCREEN = "com.android.incallui/com.android.incallui.OppoInCallActivity";
    private static final String PERMISSION_OPPO_COMPONENT_SAFE = "oppo.permission.OPPO_COMPONENT_SAFE";
    private static final float PROXIMITY_THRESHOLD = 2.0f;
    private static final long QUICK_SHOT_DELAY_MILLIS_SCREEN_OFF = 300;
    private static final long QUICK_SHOT_DELAY_MILLIS_SCREEN_ON = 200;
    private static final String SETTING_CHILDREN_MODE = "children_mode_on";
    private static final String SETTING_HALL_ENABLE = "oppo_hall_device_enabled";
    private static final String SPECIAL_PKG = "com.taiwanmobile.wali";
    public static final String START_SPEECH_DISABLE = "com.oppo.intent.action.START_SPEECH_DISABLE";
    public static final String START_SPEECH_ENABLE = "com.oppo.intent.action.START_SPEECH_ENABLE";
    private static final String STR_UP_GESTURE_MODE = "hide_navigationbar_enable";
    private static final String TAG = "OppoPhoneWindowManager";
    private static final Uri URI_SETTINGS_UP_GESTURE_MODE = Secure.getUriFor(STR_UP_GESTURE_MODE);
    private static final long WAIT_FOR_START_TIME = 2000;
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
    private static final int WINDOW_LAYER_14 = 14;
    private static final int WINDOW_LAYER_15 = 15;
    private static final int WINDOW_LAYER_2 = 2;
    private static final int mKeycodeRecentTask = 187;
    ArrayList<WindowState> mApkLockScreens = new ArrayList();
    private boolean mBackKeyUpIgnore;
    private BroadcastReceiver mBootShutdownReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
        }
    };
    private int mCntPhoneStateReg;
    private Context mContext;
    private volatile int mCurrentKeyMode = 0;
    private int mDisableBottomKeyMode = 0;
    private DisableBottomKeyModeReceiver mDisableBottomKeyModeReceiver = new DisableBottomKeyModeReceiver(this, null);
    private long mForceResumForChangingTheme = 30000;
    private OppoScreenOffGestureManager mGestureManager = null;
    private HallEnableObserver mHallEnableObserver;
    private Toast mHitToast = null;
    private final Runnable mHomeKeyLongPress = new Runnable() {
        public void run() {
            OppoPhoneWindowManager.this.mHomeConsumed = true;
            OppoPhoneWindowManager.this.startSpeechAssist();
        }
    };
    private IOppoWindowManagerImpl mIOppoWindowManagerImpl = null;
    private boolean mIgnoreHomeAndMenu = false;
    private boolean mIsAnimatingKeyDown = false;
    private boolean mIsCameraShow = false;
    private boolean mIsHomeKeyDown;
    private boolean mIsInGestureGuide = false;
    private boolean mIsMenuKeyDown;
    private boolean mIsUpGestureMode = false;
    private ContentObserver mIsUpGestureModeSettingObserver = new ContentObserver(this.mHandler) {
        public void onChange(boolean selfChange, Uri uri) {
            boolean z = false;
            OppoPhoneWindowManager oppoPhoneWindowManager = OppoPhoneWindowManager.this;
            if (Secure.getInt(OppoPhoneWindowManager.this.mContext.getContentResolver(), OppoPhoneWindowManager.STR_UP_GESTURE_MODE, 0) == 2) {
                z = true;
            }
            oppoPhoneWindowManager.mIsUpGestureMode = z;
        }
    };
    private final ArrayList<String> mKeyLockIntentProcess = new ArrayList();
    private KeyLockModeReceiver mKeyLockModeReceiver = new KeyLockModeReceiver(this, null);
    private int mLastAction = -1;
    private WindowState mLastAppWin = null;
    private int mLastDownKeyCode = -1;
    private long mLastDownKeyTime;
    private WindowState mLauncherWin = null;
    private ColorLongshotPolicy mLongshotPolicy = new ColorLongshotPolicy();
    private boolean mMenuKeyUpIgnore;
    private Handler mMyHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 16:
                    OppoPhoneWindowManager.this.sendIsHomeModeIntent(msg.arg1 == 1, msg.arg2 == 1);
                    return;
                case 17:
                    OppoPhoneWindowManager.this.sendIsCameraModeIntent(msg.arg1 == 1);
                    return;
                case 18:
                    OppoPhoneWindowManager.this.setWallpaperLayer();
                    return;
                case 19:
                    OppoPhoneWindowManager.this.sendIsInGestureGuideIntent(msg.arg1 == 1);
                    return;
                default:
                    return;
            }
        }
    };
    WakeLock mNotifyLidWakeLock;
    private Object mObject = new Object();
    private OppoAppFrozen mOppoAppFrozen;
    BroadcastReceiver mOppoBaseReceiver = new BroadcastReceiver() {
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
                OppoPhoneWindowManager.this.mShotScreenHelper.shotScreen(OppoPhoneWindowManager.this.mStatusBar, OppoPhoneWindowManager.this.mNavigationBar, intent.getIntExtra("direction", 1), OppoPhoneWindowManager.this.isGlobalActionVisible(), OppoPhoneWindowManager.this.isLandscape());
            } else if (OppoPhoneStateReceiver.ACTION_PHONE_STATE_CHANGED.equals(action)) {
                String state = intent.getStringExtra("state");
                if (TelephonyManager.EXTRA_STATE_IDLE.equals(state)) {
                    OppoPhoneWindowManager.this.mRingingTime = 0;
                } else if (TelephonyManager.EXTRA_STATE_RINGING.equals(state)) {
                    OppoPhoneWindowManager.this.mRingingTime = SystemClock.uptimeMillis();
                } else if (TelephonyManager.EXTRA_STATE_OFFHOOK.equals(state)) {
                    OppoPhoneWindowManager.this.mRingingTime = 0;
                }
            }
        }
    };
    private boolean mOppoDebug = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private int mOppoLastLidState = -1;
    private OppoSafeDbReader mOppoSafeDbReader;
    private boolean mPauseForChangingTheme = false;
    private boolean mPhoneStateRegSucess = false;
    PhoneStateListener mPhoneStatelistener = new PhoneStateListener() {
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            if (!OppoPhoneWindowManager.this.mPhoneStateRegSucess) {
                OppoPhoneWindowManager.this.mPhoneStateRegSucess = true;
                OppoPhoneWindowManager.this.mHandler.removeCallbacks(OppoPhoneWindowManager.this.mRunPhoneStateRegister);
                Log.d(OppoPhoneWindowManager.TAG, "onCallStateChanged: set mPhoneStateRegSucess true");
            }
            switch (state) {
                case 0:
                    OppoPhoneWindowManager.this.mIsMute.set(false);
                    if (OppoPhoneWindowManager.this.mOppoDebug) {
                        Log.d(OppoPhoneWindowManager.TAG, "onCallStateChanged: CALL_STATE_IDLE!");
                        return;
                    }
                    return;
                case 1:
                    OppoPhoneWindowManager.this.mIsMute.set(false);
                    if (OppoPhoneWindowManager.this.mOppoDebug) {
                        Log.d(OppoPhoneWindowManager.TAG, "onCallStateChanged: CALL_STATE_RINGING!");
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    };
    SensorEventListener mProximityListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
            boolean z = false;
            if (OppoPhoneWindowManager.this.mOppoDebug) {
                Log.d(OppoPhoneWindowManager.TAG, "mProximityListener.onSensorChanged values[0]:" + event.values[0]);
            }
            synchronized (OppoPhoneWindowManager.this.mObject) {
                float distance = event.values[0];
                OppoPhoneWindowManager oppoPhoneWindowManager = OppoPhoneWindowManager.this;
                if (((double) distance) >= 0.0d && distance < 2.0f && distance < OppoPhoneWindowManager.this.mProximitySensor.getMaximumRange()) {
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
    BroadcastReceiver mReceiverBootComplete = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
                OppoPhoneWindowManager.this.mHandler.postDelayed(OppoPhoneWindowManager.this.mRunPhoneStateRegister, 500);
                TelephonyManager.from(OppoPhoneWindowManager.this.mContext).listen(OppoPhoneWindowManager.this.mPhoneStatelistener, 32);
                Slog.d(OppoPhoneWindowManager.TAG, "ACTION_BOOT_COMPLETED: telephoneManager.listen.");
            }
        }
    };
    Runnable mResumeForChangingTheme = new Runnable() {
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
    private long mRingingTime = 0;
    private final Runnable mRunPhoneStateRegister = new Runnable() {
        public void run() {
            Slog.d(OppoPhoneWindowManager.TAG, "Runnable: mPhoneStateRegSucess = " + OppoPhoneWindowManager.this.mPhoneStateRegSucess);
            if (!OppoPhoneWindowManager.this.mPhoneStateRegSucess) {
                OppoPhoneWindowManager oppoPhoneWindowManager = OppoPhoneWindowManager.this;
                int -get0 = oppoPhoneWindowManager.mCntPhoneStateReg;
                oppoPhoneWindowManager.mCntPhoneStateReg = -get0 + 1;
                if (-get0 < 40) {
                    OppoPhoneWindowManager.this.mHandler.postDelayed(OppoPhoneWindowManager.this.mRunPhoneStateRegister, 500);
                }
                TelephonyManager.from(OppoPhoneWindowManager.this.mContext).listen(OppoPhoneWindowManager.this.mPhoneStatelistener, 32);
                Slog.d(OppoPhoneWindowManager.TAG, "Runnable: telephoneManager.listen. mCntPhoneStateReg=" + OppoPhoneWindowManager.this.mCntPhoneStateReg);
            }
        }
    };
    private BroadcastReceiver mSaleReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(OppoPhoneWindowManager.TAG, "mSaleReceiver: action=" + action);
            if ("com.oppo.screensaver.on".equals(action) && context.getPackageManager().hasSystemFeature("oppo.version.exp")) {
                Log.d("OppoScreenSaverCtrl", "mSaleReceiver: action=" + action);
            }
        }
    };
    private int mSameBottomKeyClickCount = 0;
    private SensorManager mSensorManager;
    OppoShotScreenHelper mShotScreenHelper = new ColorShotScreenHelper();
    private OppoSkyGestureHelper mSkyGestureHelper = null;
    private boolean mStartSpeechEnabled = false;
    private boolean mSupportQuickShot = false;
    private WindowState mTopAppWin = null;
    final Runnable mUpDownRunnable = new Runnable() {
        public void run() {
            Intent intent = new Intent();
            long uptime = SystemClock.uptimeMillis();
            intent.setAction("action.com.oppo.oclick.single_click");
            intent.putExtra("upTime", uptime);
            if (OppoPhoneWindowManager.this.mOppoDebug) {
                Log.i(OppoPhoneWindowManager.TAG, "sendBroadcast uptime = " + uptime);
            }
            OppoPhoneWindowManager.this.mContext.sendBroadcast(intent);
        }
    };
    private boolean mVolumeDownKeyConsumedByUpDownChord;
    private long mVolumeDownKeyTime;
    private boolean mVolumeDownKeyTriggered;
    private boolean mVolumeUpKeyConsumedByUpDownChord;
    private long mVolumeUpKeyTime;
    private boolean mVolumeUpKeyTriggered;
    private int mWallpaperLayer = -1;
    private WindowState mWallpaperWin = null;

    private class DisableBottomKeyModeReceiver extends BroadcastReceiver {
        /* synthetic */ DisableBottomKeyModeReceiver(OppoPhoneWindowManager this$0, DisableBottomKeyModeReceiver -this1) {
            this();
        }

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
                System.putInt(OppoPhoneWindowManager.this.mContext.getContentResolver(), "disable_bottom_key_mode", 0);
            } else if (1 == mode) {
                OppoPhoneWindowManager.this.mDisableBottomKeyMode = 1;
                System.putInt(OppoPhoneWindowManager.this.mContext.getContentResolver(), "disable_bottom_key_mode", 1);
            }
        }
    }

    private class HallEnableObserver extends ContentObserver {
        HallEnableObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            OppoPhoneWindowManager.this.updateLidControlSleep();
        }
    }

    private class KeyLockModeReceiver extends BroadcastReceiver {
        /* synthetic */ KeyLockModeReceiver(OppoPhoneWindowManager this$0, KeyLockModeReceiver -this1) {
            this();
        }

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

    public void beginPostLayoutPolicyLw(int displayWidth, int displayHeight) {
        super.beginPostLayoutPolicyLw(displayWidth, displayHeight);
        this.mWallpaperWin = null;
        this.mLauncherWin = null;
        this.mTopAppWin = null;
    }

    public void applyPostLayoutPolicyLw(WindowState win, LayoutParams attrs, WindowState attached, WindowState imeTarget) {
        super.applyPostLayoutPolicyLw(win, attrs, attached, imeTarget);
        if (win.getAttrs().type <= 99 && win.isVisibleLw()) {
            if (this.mTopAppWin == null) {
                this.mTopAppWin = win;
            } else if (this.mTopAppWin.getSurfaceLayer() < win.getSurfaceLayer()) {
                this.mTopAppWin = win;
            }
        }
        if (win.toString().contains("launcher")) {
            this.mLauncherWin = win;
        } else if (win.getAttrs().type == 2013) {
            this.mWallpaperWin = win;
        }
    }

    private static boolean isExpROM() {
        return SystemProperties.get("persist.sys.oppo.region", "CN").equalsIgnoreCase("CN") ^ 1;
    }

    private void updateLidControlSleep() {
        boolean isHallEnable;
        try {
            isHallEnable = System.getInt(this.mContext.getContentResolver(), SETTING_HALL_ENABLE) != 0;
        } catch (SettingNotFoundException e) {
            isHallEnable = true;
            Log.d(TAG, "updateLidControlSleep: oppo_hall_device_enabled is not found.");
        }
        boolean disWindowLeather = this.mContext.getPackageManager().hasSystemFeature("oppo.disable.small.window.leather");
        if (isHallEnable && disWindowLeather) {
            this.mLidControlsSleep = true;
        } else {
            this.mLidControlsSleep = false;
        }
        Log.d(TAG, "updateLidControlSleep: isHallEnable=" + isHallEnable + ", disWindowLeather=" + disWindowLeather + ", mLidControlsSleep=" + this.mLidControlsSleep);
    }

    public void init(Context context, IWindowManager windowManager, WindowManagerFuncs windowManagerFuncs) {
        super.init(context, windowManager, windowManagerFuncs);
        IntentFilter keyModeFilter = new IntentFilter();
        keyModeFilter.addAction(ACTION_KEY_LOCK);
        context.registerReceiver(this.mKeyLockModeReceiver, keyModeFilter, PERMISSION_OPPO_COMPONENT_SAFE, null);
        IntentFilter disableBottomKeyModeFilter = new IntentFilter();
        disableBottomKeyModeFilter.addAction(ACTION_DISABLE_BOTTOM_KEY_MODE);
        context.registerReceiver(this.mDisableBottomKeyModeReceiver, disableBottomKeyModeFilter, PERMISSION_OPPO_COMPONENT_SAFE, null);
        context.getContentResolver().registerContentObserver(URI_SETTINGS_UP_GESTURE_MODE, false, this.mIsUpGestureModeSettingObserver);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_END_CALL);
        filter.addAction(START_SPEECH_ENABLE);
        filter.addAction(START_SPEECH_DISABLE);
        filter.addAction(ACTION_SCREEN_SHOT);
        filter.addAction(OppoPhoneStateReceiver.ACTION_PHONE_STATE_CHANGED);
        context.registerReceiver(this.mOppoBaseReceiver, filter);
        IntentFilter f = new IntentFilter();
        f.addAction(ACTION_BOOT_IPO);
        f.addAction(ACTION_SHUTDOWN_IPO);
        f.addAction("android.intent.action.ACTION_SHUTDOWN");
        context.registerReceiver(this.mBootShutdownReceiver, f);
        if (context.getPackageManager().hasSystemFeature("oppo.version.exp")) {
            IntentFilter fsale = new IntentFilter();
            fsale.addAction("com.oppo.screensaver.on");
            context.registerReceiver(this.mSaleReceiver, fsale);
        }
        this.mContext = context;
        this.mShotScreenHelper.init(this.mHandler, this.mContext);
        this.mGestureManager = new OppoScreenOffGestureManager(this.mContext, this.mHandler, this.mKeyguardDelegate, this.mBroadcastWakeLock);
        updateLidControlSleep();
        this.mHallEnableObserver = new HallEnableObserver(this.mHandler);
        this.mContext.getContentResolver().registerContentObserver(System.getUriFor(SETTING_HALL_ENABLE), false, this.mHallEnableObserver);
        if (this.mContext.getPackageManager().hasSystemFeature("oppo.quick.shot.support")) {
            this.mSupportQuickShot = true;
        }
        this.mSkyGestureHelper = new OppoSkyGestureHelper(this.mHandler, this.mContext, this.mPowerManager);
        OppoScreenDragUtil.resetState();
        this.mNotifyLidWakeLock = this.mPowerManager.newWakeLock(1, "notifyLidSwitchWakeLock");
        this.mOppoAppFrozen = new OppoAppFrozen(context, this);
        this.mOppoSafeDbReader = OppoSafeDbReader.getInstance(this.mContext);
        this.mOppoSafeDbReader.startThread();
        IntentFilter fBootComplete = new IntentFilter();
        fBootComplete.addAction("android.intent.action.BOOT_COMPLETED");
        fBootComplete.setPriority(1000);
        this.mContext.registerReceiver(this.mReceiverBootComplete, fBootComplete);
        this.mLongshotPolicy.init(this.mContext);
    }

    public int interceptKeyBeforeQueueing(KeyEvent event, int policyFlags) {
        if (!this.mSystemBooted) {
            return 0;
        }
        boolean interactive = (536870912 & policyFlags) != 0;
        boolean down = event.getAction() == 0;
        int keyCode = event.getKeyCode();
        int repeatCount = event.getRepeatCount();
        if (3 == keyCode) {
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
            if (4 == keyCode && down) {
                if (repeatCount == 0) {
                    this.mBackKeyUpIgnore = true;
                }
                if (this.mOppoDebug) {
                    Log.d(TAG, "interceptKeyBeforeQueueing: Home Pressed, ignore KEYCODE_BACK DOWN!");
                }
                return 0;
            }
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
        }
        if (4 == keyCode && (down ^ 1) != 0 && this.mBackKeyUpIgnore) {
            this.mBackKeyUpIgnore = false;
            if (this.mOppoDebug) {
                Log.d(TAG, "interceptKeyBeforeQueueing: KEYCODE_BACK DOWN has been ignored, ignore KEYCODE_BACK UP now!");
            }
            return 0;
        } else if (mKeycodeRecentTask == keyCode && (down ^ 1) != 0 && this.mMenuKeyUpIgnore) {
            this.mMenuKeyUpIgnore = false;
            if (this.mOppoDebug) {
                Log.d(TAG, "interceptKeyBeforeQueueing: RecentTask DOWN has been ignored, ignore RecentTask UP now!");
            }
            return 0;
        } else {
            if (keyCode == 3 && (down ^ 1) != 0) {
                this.mHandler.removeCallbacks(this.mHomeKeyLongPress);
            }
            OppoGlobalActions oppoGlobalActions = this.mGlobalActions;
            if (OppoGlobalActions.isPRStatus) {
                Log.d(TAG, "OppoPhoneWindowManager block key at shutdown/restart sequence");
                return 0;
            }
            if (25 == keyCode || 24 == keyCode || 164 == keyCode) {
                if (down) {
                    this.mContext.sendBroadcast(new Intent("SILENCE_ACTION_FOR_OPPO_SPEECH"));
                    if (AudioSystem.isStreamActive(5, 0)) {
                        disableNotificationAlert();
                    }
                }
                if (this.mContext.getPackageManager().hasSystemFeature("oppo.sky.gesture.support")) {
                    if (down && (interactive ^ 1) != 0) {
                        this.mSkyGestureHelper.dealSkyGestureDown();
                    }
                    if (!down && this.mSkyGestureHelper.isVolumeLongPress()) {
                        this.mSkyGestureHelper.dealSkyGestureUp();
                    }
                }
            }
            policyFlags = handleScreenoffGesture(event, policyFlags, interactive);
            if (this.mOppoDebug) {
                Log.d(TAG, "mIsUpGestureMode = " + this.mIsUpGestureMode + "  mDisableBottomKeyMode = " + this.mDisableBottomKeyMode);
            }
            if (!this.mIsUpGestureMode && 1 == this.mDisableBottomKeyMode) {
                if (down) {
                    if (keyCode != this.mLastDownKeyCode || repeatCount != 0) {
                        if (keyCode == mKeycodeRecentTask || keyCode == 3 || keyCode == 4) {
                            this.mHandler.post(new Runnable() {
                                public void run() {
                                    if (OppoPhoneWindowManager.this.mHitToast != null) {
                                        OppoPhoneWindowManager.this.mHitToast.cancel();
                                    }
                                    OppoPhoneWindowManager.this.mHitToast = Toast.makeText(OppoPhoneWindowManager.this.mContext, OppoPhoneWindowManager.this.mContext.getString(201590152, new Object[]{Integer.valueOf(1)}), 0);
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
                                public void run() {
                                    if (OppoPhoneWindowManager.this.mHitToast != null) {
                                        OppoPhoneWindowManager.this.mHitToast.cancel();
                                    }
                                    OppoPhoneWindowManager.this.mHitToast = Toast.makeText(OppoPhoneWindowManager.this.mContext, OppoPhoneWindowManager.this.mContext.getString(201590152, new Object[]{Integer.valueOf(1)}), 0);
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
                    System.putInt(this.mContext.getContentResolver(), "disable_bottom_key_mode", 0);
                    this.mHandler.post(new Runnable() {
                        public void run() {
                            try {
                                IStatusBarService statusbar = OppoPhoneWindowManager.this.getStatusBarService();
                                if (statusbar != null) {
                                    statusbar.setShortcutsPanelState(2);
                                }
                            } catch (RemoteException e) {
                                Slog.e(OppoPhoneWindowManager.TAG, "RemoteException when setShortcutsPanelState: ", e);
                                OppoPhoneWindowManager.this.mStatusBarService = null;
                            }
                            if (OppoPhoneWindowManager.this.mHitToast != null) {
                                OppoPhoneWindowManager.this.mHitToast.cancel();
                            }
                            OppoPhoneWindowManager.this.mHitToast = Toast.makeText(OppoPhoneWindowManager.this.mContext, OppoPhoneWindowManager.this.mContext.getString(201590132), 0);
                            OppoPhoneWindowManager.this.mHitToast.show();
                        }
                    });
                }
                if (keyCode == mKeycodeRecentTask || keyCode == 3 || keyCode == 4) {
                    return 0;
                }
            }
            KeyEvent newEvent = adjustKey(event);
            if (this.mOppoDebug && isLogKey(keyCode)) {
                Log.d(TAG, "interceptKeyBeforeQueueing:" + newEvent.toString());
            }
            if (this.mSupportQuickShot) {
                if (keyCode == 25) {
                    if (!down) {
                        this.mVolumeDownKeyTriggered = false;
                    } else if (!this.mVolumeDownKeyTriggered && (event.getFlags() & 1024) == 0) {
                        this.mVolumeDownKeyTriggered = true;
                        this.mVolumeDownKeyTime = event.getDownTime();
                        this.mVolumeDownKeyConsumedByUpDownChord = false;
                        interceptUpDownChord();
                    }
                }
                if (keyCode == 24) {
                    if (!down) {
                        this.mVolumeUpKeyTriggered = false;
                    } else if (!this.mVolumeUpKeyTriggered && (event.getFlags() & 1024) == 0) {
                        this.mVolumeUpKeyTriggered = true;
                        this.mVolumeUpKeyTime = event.getDownTime();
                        this.mVolumeDownKeyConsumedByUpDownChord = false;
                        interceptUpDownChord();
                    }
                }
            }
            int result = super.interceptKeyBeforeQueueing(newEvent, policyFlags);
            if (!(event.isCanceled() || keyCode != 3 || (down ^ 1) == 0 || (result & 1) == 0)) {
                this.mOppoAppFrozen.sendHomeDispatchTimeoutMsg();
            }
            return result;
        }
    }

    /* JADX WARNING: Missing block: B:233:0x065e, code:
            if (r29.getAttrs().getTitle().toString().contains(OPPO_IGNORE_INCALL_SCREEN) == false) goto L_0x0660;
     */
    /* JADX WARNING: Missing block: B:235:0x066c, code:
            if ((r16 - r28.mRingingTime) < 2000) goto L_0x066e;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public long interceptKeyBeforeDispatching(WindowState win, KeyEvent event, int policyFlags) {
        if (event.getAction() == 0) {
        }
        boolean down = event.getAction() == 0;
        int keyCode = event.getKeyCode();
        int flags = event.getFlags();
        boolean canceled = event.isCanceled();
        int repeatCount = event.getRepeatCount();
        if (keyCode == 3 && (down ^ 1) != 0) {
            this.mOppoAppFrozen.clearHomeDispatchTimeoutMsg();
            this.mOppoAppFrozen.appFrozenHandle();
            this.mHandler.removeCallbacks(this.mHomeKeyLongPress);
        }
        if (this.mIOppoWindowManagerImpl == null) {
            this.mIOppoWindowManagerImpl = new IOppoWindowManagerImpl();
        }
        if (keyCode == mKeycodeRecentTask) {
            if (this.mContext.getPackageManager().hasSystemFeature(FEATURE_CHILDREN_MODE)) {
                if (Global.getInt(this.mContext.getContentResolver(), SETTING_CHILDREN_MODE, 0) == 1) {
                    Log.d(TAG, "ignore menu in cdm");
                    return -1;
                }
            }
        }
        if (win != null) {
            try {
                if (win.getAttrs() != null) {
                    if (this.mIgnoreHomeAndMenu && down) {
                        this.mIgnoreHomeAndMenu = false;
                    }
                    if (win.getAttrs().ignoreHomeMenuKey == 1 || (win.getAttrs().memoryType & 16777216) != 0) {
                        if (keyCode == mKeycodeRecentTask || keyCode == 3 || keyCode == 82) {
                            if (down) {
                                this.mIgnoreHomeAndMenu = true;
                            } else {
                                this.mIgnoreHomeAndMenu = false;
                            }
                            Log.v(TAG, "ignoreHomeMenuKey ignore KEYCODE_MENU and KEYCODE_HOME win: " + win);
                            return -1;
                        }
                    } else if (win.getAttrs().ignoreHomeMenuKey == 2 || (win.getAttrs().memoryType & PhoneWindowManager.SYSTEM_UI_FLAG_APP_CUSTOM_NAVIGATION_BAR) != 0) {
                        if (keyCode == 3) {
                            if (down) {
                                this.mIgnoreHomeAndMenu = true;
                            } else {
                                this.mIgnoreHomeAndMenu = false;
                            }
                            Log.v(TAG, "ignoreHomeMenuKey ignore KEYCODE_HOME win: " + win);
                            return -1;
                        }
                    } else if ((win.getAttrs().ignoreHomeMenuKey == 3 || (win.getAttrs().memoryType & 67108864) != 0) && (keyCode == mKeycodeRecentTask || keyCode == 82)) {
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
                if (!(this.mContext == null || (this.mContext.getPackageManager().isClosedSuperFirewall() ^ 1) == 0)) {
                    if (keyCode == 4) {
                        if (event.getAction() == 0) {
                            if (repeatCount == 0) {
                                this.mIsAnimatingKeyDown = false;
                                if ((win.isAnimatingLw() && win.isNotDummyAnimation()) || this.mIOppoWindowManagerImpl.isRotatingLw()) {
                                    if (this.mOppoDebug) {
                                        Log.i(TAG, "do not shotscreen when animating or window rotating!-KeyEvent.ACTION_DOWN-1, win: " + win + ",animating = " + win.isAnimatingLw() + ", NotDummy= " + win.isNotDummyAnimation());
                                    }
                                    this.mIsAnimatingKeyDown = true;
                                    return -1;
                                }
                            }
                            if (this.mIsAnimatingKeyDown) {
                                if (this.mOppoDebug) {
                                    Log.i(TAG, "do not shotscreen when animating or window rotating!-KeyEvent.ACTION_DOWN-2, win: " + win);
                                }
                                return -1;
                            }
                        }
                        if (this.mIsAnimatingKeyDown && event.getAction() == 1) {
                            if (this.mOppoDebug) {
                                Log.i(TAG, "do not shotscreen when animating or window rotating!-KeyEvent.ACTION_UP-3, win: " + win + ",animating = " + win.isAnimatingLw() + ", NotDummy= " + win.isNotDummyAnimation());
                            }
                            this.mIsAnimatingKeyDown = false;
                            return -1;
                        }
                    }
                    if (repeatCount == 0 && ((keyCode == 3 || keyCode == mKeycodeRecentTask) && ((win.isAnimatingLw() && win.isNotDummyAnimation()) || this.mIOppoWindowManagerImpl.isRotatingLw()))) {
                        if (this.mOppoDebug) {
                            Log.i(TAG, "do not shotscreen when animating or window rotating!-KeyEvent.KEYCODE_HOME or RecentTask keycode, win: " + win + ",animating = " + win.isAnimatingLw() + ", NotDummy= " + win.isNotDummyAnimation());
                        }
                        if (this.mHomeConsumed && event.getAction() == 1 && keyCode == 3) {
                            this.mHomeConsumed = false;
                        }
                        return -1;
                    } else if (event.isLongPress() && keyCode == 3 && ((win.isAnimatingLw() && win.isNotDummyAnimation()) || this.mIOppoWindowManagerImpl.isRotatingLw())) {
                        if (this.mOppoDebug) {
                            Log.i(TAG, "do not shotscreen when isCanceled and animating or window rotating, win: " + win + ",animating = " + win.isAnimatingLw() + ", NotDummy= " + win.isNotDummyAnimation());
                        }
                        if (this.mHomeConsumed && event.getAction() == 1 && keyCode == 3) {
                            this.mHomeConsumed = false;
                        }
                        return -1;
                    }
                }
            } catch (RemoteException e) {
            }
        }
        if (this.mOppoDebug && isLogKey(keyCode)) {
            Log.d(TAG, "interceptKeyBeforeDispatching key: win=" + win + "  event = " + event.toString());
        }
        if (this.mSupportQuickShot && (flags & 1024) == 0) {
            long now;
            long timeoutTime;
            if (this.mVolumeDownKeyTriggered && (this.mVolumeUpKeyTriggered ^ 1) != 0) {
                now = SystemClock.uptimeMillis();
                if (this.mPowerManager.isScreenOn()) {
                    timeoutTime = this.mVolumeDownKeyTime + QUICK_SHOT_DELAY_MILLIS_SCREEN_ON;
                } else {
                    timeoutTime = this.mVolumeDownKeyTime + 300;
                }
                if (now < timeoutTime) {
                    return timeoutTime - now;
                }
            }
            if (this.mVolumeUpKeyTriggered && (this.mVolumeDownKeyTriggered ^ 1) != 0) {
                now = SystemClock.uptimeMillis();
                if (this.mPowerManager.isScreenOn()) {
                    timeoutTime = this.mVolumeUpKeyTime + QUICK_SHOT_DELAY_MILLIS_SCREEN_ON;
                } else {
                    timeoutTime = this.mVolumeUpKeyTime + 300;
                }
                if (now < timeoutTime) {
                    return timeoutTime - now;
                }
            }
            if (keyCode == 25 && this.mVolumeDownKeyConsumedByUpDownChord) {
                if (!down) {
                    this.mVolumeDownKeyConsumedByUpDownChord = false;
                }
                return -1;
            } else if (keyCode == 24 && this.mVolumeUpKeyConsumedByUpDownChord) {
                if (!down) {
                    this.mVolumeUpKeyConsumedByUpDownChord = false;
                }
                return -1;
            }
        }
        KeyEvent newEvent = adjustKey(event);
        if (this.mOppoDebug && isLogKey(keyCode)) {
            Log.d(TAG, "interceptKeyBeforeDispatching newEvent keyCode = " + newEvent.getKeyCode() + ", mHomeConsumed = " + this.mHomeConsumed);
        }
        if (newEvent.getKeyCode() != 3) {
            return super.interceptKeyBeforeDispatching(win, newEvent, policyFlags);
        }
        if (down || this.mHomeConsumed) {
            if (!isExpROM() && this.mStartSpeechEnabled && down && repeatCount == 0) {
                boolean keyguardOn = keyguardOn();
                TelecomManager telephoneManager = getTelecommService();
                if (!(keyguardOn || win == null)) {
                    if ((OPPO_IGNORE_SPEECH_ASSIST.equals(win.getAttrs().packageName) ^ 1) != 0) {
                        if ((OPPO_IGNORE_DRIVE_MODE.equals(win.getAttrs().packageName) ^ 1) != 0 && telephoneManager.getCallState() == 0) {
                            this.mHandler.postDelayed(this.mHomeKeyLongPress, 750);
                        }
                    }
                }
            }
            return super.interceptKeyBeforeDispatching(win, event, policyFlags);
        }
        this.mHomePressed = false;
        this.mHomeConsumed = false;
        cancelPreloadRecentApps();
        this.mHomePressed = false;
        if (!canceled) {
            boolean incomingRinging = false;
            TelecomManager telecomManager = getTelecommService();
            if (telecomManager != null) {
                incomingRinging = telecomManager.isRinging();
            }
            long nowTime = SystemClock.uptimeMillis();
            if (incomingRinging) {
                if (!(win == null || win.getAttrs() == null || win.getAttrs().getTitle() == null)) {
                    if (!win.getAttrs().getTitle().toString().contains(OPPO_INCALL_SCREEN)) {
                    }
                    if (this.mOppoDebug) {
                        Log.i(TAG, "Ignoring HOME; there's a ringing incoming call.");
                    }
                }
            }
            launchHomeFromHotKey();
        } else if (this.mOppoDebug) {
            Log.i(TAG, "Ignoring HOME; event canceled.");
        }
        return -1;
    }

    private void setVideoMode(int mode) {
        this.mCurrentKeyMode = mode;
    }

    private KeyEvent adjustKey(KeyEvent event) {
        KeyEvent newEvent = event;
        int keyCode = event.getKeyCode();
        if (1 == this.mCurrentKeyMode) {
            switch (keyCode) {
                case H.DO_ANIMATION_CALLBACK /*26*/:
                    return offsetKey(event);
                default:
                    return newEvent;
            }
        } else if (2 == this.mCurrentKeyMode) {
            switch (keyCode) {
                case 3:
                case H.DO_ANIMATION_CALLBACK /*26*/:
                case HdmiCecKeycode.CEC_KEYCODE_VIDEO_ON_DEMAND /*82*/:
                case mKeycodeRecentTask /*187*/:
                    return offsetKey(event);
                default:
                    return newEvent;
            }
        } else if (3 != this.mCurrentKeyMode) {
            return newEvent;
        } else {
            switch (keyCode) {
                case 3:
                case HdmiCecKeycode.CEC_KEYCODE_VIDEO_ON_DEMAND /*82*/:
                case mKeycodeRecentTask /*187*/:
                    return offsetKey(event);
                default:
                    return newEvent;
            }
        }
    }

    private KeyEvent offsetKey(KeyEvent event) {
        return KeyEvent.obtain(event.getDownTime(), event.getEventTime(), event.getAction(), event.getKeyCode() + KEY_OFFSET_VALUE, event.getRepeatCount(), event.getMetaState(), event.getDeviceId(), event.getScanCode(), event.getFlags(), event.getSource(), event.getCharacters());
    }

    private void startSpeechAssist() {
        Intent intentOppoAssist = new Intent();
        intentOppoAssist.setFlags(268435456);
        intentOppoAssist.setAction("com.coloros.speechassist.start_action");
        if (OppoSysStateManager.getInstance().isScreenOff()) {
            if (this.mOppoDebug) {
                Log.i(TAG, "startSpeechAssist: is screen off. do not start speechassist");
            }
            return;
        }
        if (this.mOppoDebug) {
            Log.i(TAG, "startSpeechAssist:send com.coloros.speechassist.start_action");
        }
        try {
            this.mContext.startActivity(intentOppoAssist);
        } catch (ActivityNotFoundException e) {
            Log.w(TAG, "startSpeechAssist: ", e);
        }
    }

    public void systemReady() {
        boolean z = false;
        super.systemReady();
        this.mSensorManager = new SystemSensorManager(this.mContext, this.mHandler.getLooper());
        this.mProximitySensor = this.mSensorManager.getDefaultSensor(8);
        this.mDisableBottomKeyMode = 0;
        System.putInt(this.mContext.getContentResolver(), "disable_bottom_key_mode", 0);
        if (Secure.getInt(this.mContext.getContentResolver(), STR_UP_GESTURE_MODE, 0) == 2) {
            z = true;
        }
        this.mIsUpGestureMode = z;
    }

    public void screenTurnedOff() {
        super.screenTurnedOff();
        if (this.mGestureManager != null) {
            this.mGestureManager.screenTurnedOff();
        }
        OppoScreenShotUtil.resumeDeliverPointerEvent();
        this.mHandler.removeCallbacks(this.mHomeKeyLongPress);
        this.mContext.sendBroadcast(new Intent("android.intent.action.OPPO_SCREEN_OFF"), PERMISSION_OPPO_COMPONENT_SAFE);
    }

    public void screenTurningOn(ScreenOnListener screenOnListener) {
        super.screenTurningOn(screenOnListener);
        if (this.mGestureManager != null) {
            this.mGestureManager.screenTurningOn();
        }
    }

    public int getColorKeyMode() {
        return this.mCurrentKeyMode;
    }

    public boolean isIncomingRingingIngoreHomeKey(WindowState currentFocus) {
        boolean incomingRinging = false;
        TelecomManager telecomManager = getTelecommService();
        if (telecomManager != null) {
            incomingRinging = telecomManager.isRinging();
        }
        long nowTime = SystemClock.uptimeMillis();
        if (!incomingRinging || ((currentFocus == null || currentFocus.getAttrs() == null || currentFocus.getAttrs().getTitle() == null || (!currentFocus.getAttrs().getTitle().toString().contains(OPPO_INCALL_SCREEN) && !currentFocus.getAttrs().getTitle().toString().contains(OPPO_IGNORE_INCALL_SCREEN))) && nowTime - this.mRingingTime >= 2000)) {
            return false;
        }
        if (this.mOppoDebug) {
            Log.i(TAG, "isIncomingRingingIngoreKey  true.");
        }
        return true;
    }

    public boolean isStatusBarVisible() {
        return this.mStatusBar.isVisibleLw();
    }

    public boolean performHapticFeedbackLw(WindowState win, int effectId, boolean always) {
        boolean handle = super.performHapticFeedbackLw(win, effectId, always);
        if (!handle) {
            if (this.mVibrator == null) {
                this.mVibrator = (Vibrator) this.mContext.getSystemService("vibrator");
            }
            switch (effectId) {
                case 300:
                    this.mVibrator.vibrate(new long[]{0, 300}, -1);
                    return true;
                case 301:
                    this.mVibrator.vibrate(new long[]{0, DEFALUT_TOUCH_FEEDBACK_10, DEFALUT_TOUCH_FEEDBACK_20, DEFALUT_TOUCH_FEEDBACK_30}, -1);
                    return true;
            }
        }
        return handle;
    }

    public int finishPostLayoutPolicyLw() {
        int i = 1;
        boolean isCameraShow = false;
        if (!(this.mTopAppWin == null || this.mTopAppWin.getOwningPackage() == null || !this.mTopAppWin.getOwningPackage().contains(CAMERA_PKG))) {
            isCameraShow = true;
        }
        if (isCameraShow != this.mIsCameraShow) {
            int i2;
            this.mIsCameraShow = isCameraShow;
            this.mMyHandler.removeMessages(17);
            Handler handler = this.mMyHandler;
            if (this.mIsCameraShow) {
                i2 = 1;
            } else {
                i2 = 0;
            }
            this.mMyHandler.sendMessageDelayed(Message.obtain(handler, 17, i2, 0), MSG_ISINGESTUREGUIDE_AND_ISCAMERAMODE);
        }
        Object pkgName = null;
        Object appName = null;
        boolean isInGestureGuide = false;
        if (!(this.mTopAppWin == null || this.mTopAppWin.getAttrs() == null || this.mTopAppWin.getAttrs().getTitle() == null)) {
            String[] subString = this.mTopAppWin.getAttrs().getTitle().toString().split("/");
            if (subString.length == 2) {
                pkgName = subString[0];
                appName = subString[1];
            }
            if (CAPTURE_GUIDE.equals(appName) || MULTI_TOUCH_GUIDE.equals(appName) || CTS_PROJECTION_TOUCH.equals(appName) || SPECIAL_PKG.equals(pkgName)) {
                isInGestureGuide = true;
            }
        }
        if (isInGestureGuide != this.mIsInGestureGuide) {
            this.mIsInGestureGuide = isInGestureGuide;
            this.mMyHandler.removeMessages(19);
            Handler handler2 = this.mMyHandler;
            if (!this.mIsInGestureGuide) {
                i = 0;
            }
            this.mMyHandler.sendMessageDelayed(Message.obtain(handler2, 19, i, 0), MSG_ISINGESTUREGUIDE_AND_ISCAMERAMODE);
        }
        int changes = super.finishPostLayoutPolicyLw() | 0;
        checkLiveWallpaperLayer();
        return changes;
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

    public void notifyLidSwitchChanged(long whenNanos, boolean lidOpen) {
        if (this.mContext.getPackageManager().hasSystemFeature("oppo.disable.small.window.leather")) {
            super.notifyLidSwitchChanged(whenNanos, lidOpen);
            return;
        }
        int newLidState = lidOpen ? 1 : 0;
        if (newLidState == this.mOppoLastLidState) {
            if (this.mOppoDebug) {
                Log.i(TAG, "notifyLidSwitchChanged newLidState == mLidState");
            }
            return;
        }
        if (this.mSystemReady && isLeatherModeEnabled(this.mContext)) {
            Intent intent = new Intent("com.oppo.intent.action.LID_STATE_CHANGED");
            String str = "lid_state";
            int i = this.mOppoLastLidState == -1 ? newLidState == 1 ? 0 : 1 : this.mOppoLastLidState;
            intent.putExtra(str, i);
            if (newLidState == 1) {
                this.mNotifyLidWakeLock.acquire(2000);
            }
            this.mContext.sendBroadcast(intent);
            if (this.mOppoDebug) {
                Log.i(TAG, "OppoPhoneWindowManager notifyLidSwitchChanged sendBroadcast com.oppo.intent.action.LID_STATE_CHANGED, newLidState=" + newLidState);
            }
            if (OppoScreenDragUtil.isOffsetState()) {
                this.mContext.sendBroadcast(new Intent(KEY_EXIT_DRAG_WINDOW));
            }
        }
        super.notifyLidSwitchChanged(whenNanos, lidOpen);
        this.mOppoLastLidState = newLidState;
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

    private void disableProximitySensor() {
        if (this.mOppoDebug) {
            Log.d(TAG, "disableProximitySensor");
        }
        if (this.mProximitySensorEnabled) {
            long identity = Binder.clearCallingIdentity();
            try {
                this.mSensorManager.unregisterListener(this.mProximityListener);
                this.mProximitySensorEnabled = false;
                if (this.mProximitySensorActive) {
                    this.mProximitySensorActive = true;
                }
            } finally {
                Binder.restoreCallingIdentity(identity);
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
                if (!this.mProximitySensorActive || (this.mLidState == 0 && this.mGestureManager.isGestureDoubleTap())) {
                    policyFlags = this.mGestureManager.dealScreenOffGesture(event, policyFlags, interactive);
                }
                disableProximitySensor();
            }
            return policyFlags;
        } else if (this.mLidState == 0) {
            return this.mGestureManager.isGestureDoubleTap() ? this.mGestureManager.dealScreenOffGesture(event, policyFlags, interactive) : policyFlags;
        } else {
            return this.mGestureManager.dealScreenOffGesture(event, policyFlags, interactive);
        }
    }

    private boolean isLogKey(int keyCode) {
        if (26 == keyCode || 25 == keyCode || 24 == keyCode || 164 == keyCode || mKeycodeRecentTask == keyCode || 82 == keyCode || 3 == keyCode || 79 == keyCode || 4 == keyCode) {
            return true;
        }
        return false;
    }

    private void dumpWindowState(WindowState win) {
        Log.d(TAG, win + "====getOwningPackage :" + win.getOwningPackage());
        Log.d(TAG, " getAttrs :" + win.getAttrs());
        Log.d(TAG, "getSurfaceLayer :" + win.getSurfaceLayer());
        Log.d(TAG, "hasAppShownWindows :" + win.hasAppShownWindows());
        Log.d(TAG, "isVisibleLw :" + win.isVisibleLw());
        Log.d(TAG, "isDisplayedLw :" + win.isDisplayedLw());
        Log.d(TAG, "isAnimatingLw :" + win.isAnimatingLw());
        Log.d(TAG, "isGoneForLayoutLw :" + win.isGoneForLayoutLw());
        Log.d(TAG, "hasDrawnLw :" + win.hasDrawnLw());
        Log.d(TAG, "isAlive :" + win.isAlive());
    }

    final void sendIsHomeModeIntent(boolean isHomeMode, boolean isSemipermeable) {
        int i = 1;
        if (this.mSystemReady) {
            Intent intent = new Intent("android.intent.action.HOME_MODE_CHANGE");
            intent.putExtra("ishomemode", isHomeMode ? 1 : 0);
            String str = "isSemipermeable";
            if (!isSemipermeable) {
                i = 0;
            }
            intent.putExtra(str, i);
            if (this.mContext != null) {
                this.mContext.sendBroadcastAsUser(intent, UserHandle.CURRENT);
            }
            return;
        }
        Log.d(TAG, "sendIsHomeModeIntent:isHomeMode = " + isHomeMode + ", can't send broadcast before boot completed!");
    }

    final void sendIsCameraModeIntent(boolean isCameraMode) {
        if (this.mOppoDebug) {
            Log.d(TAG, "===sendIsCameraModeIntent==" + isCameraMode);
        }
        if (this.mSystemReady) {
            Intent intent = new Intent("android.intent.action.CAMERA_MODE_CHANGE");
            intent.putExtra("iscameramode", isCameraMode ? 1 : 0);
            if (this.mContext != null) {
                this.mContext.sendBroadcastAsUser(intent, UserHandle.CURRENT);
            }
            return;
        }
        Log.d(TAG, "sendIsCameraModeIntent:isCameraMode = " + isCameraMode + ", can't send broadcast before boot completed!");
    }

    final void sendIsInGestureGuideIntent(boolean isInGestureGuideMode) {
        if (this.mSystemReady) {
            Intent intent = new Intent("oppo.intent.action.GESTUREGUIDE_MODE_CHANGE");
            intent.putExtra("isInGestureGuideMode", isInGestureGuideMode ? 1 : 0);
            if (this.mContext != null) {
                this.mContext.sendBroadcastAsUser(intent, UserHandle.CURRENT);
            }
            return;
        }
        Log.d(TAG, "GestureGuideIntent can't send broadcast before boot completed!");
    }

    public void showBootMessage(final CharSequence msg, boolean always) {
        this.mHandler.post(new Runnable() {
            public void run() {
                if (OppoPhoneWindowManager.this.mBootMsgDialog == null) {
                    OppoPhoneWindowManager.this.mBootMsgDialog = ColorBootMessageDialog.create(OppoPhoneWindowManager.this.mContext);
                    OppoPhoneWindowManager.this.mBootMsgDialog.show();
                }
                OppoPhoneWindowManager.this.mBootMsgDialog.setMessage(msg);
            }
        });
    }

    private void interceptUpDownChord() {
        if (this.mVolumeDownKeyTriggered && this.mVolumeUpKeyTriggered) {
            Log.i(TAG, "enter interceptUpDownChord");
            long now = SystemClock.uptimeMillis();
            if (this.mPowerManager.isScreenOn()) {
                if (now <= this.mVolumeDownKeyTime + QUICK_SHOT_DELAY_MILLIS_SCREEN_ON && now <= this.mVolumeUpKeyTime + QUICK_SHOT_DELAY_MILLIS_SCREEN_ON) {
                    this.mVolumeDownKeyConsumedByUpDownChord = true;
                    this.mVolumeUpKeyConsumedByUpDownChord = true;
                    this.mHandler.post(this.mUpDownRunnable);
                }
            } else if (now <= this.mVolumeDownKeyTime + 300 && now <= this.mVolumeUpKeyTime + 300) {
                this.mVolumeDownKeyConsumedByUpDownChord = true;
                this.mVolumeUpKeyConsumedByUpDownChord = true;
                this.mHandler.post(this.mUpDownRunnable);
            }
        }
    }

    private void setWallpaperLayer() {
        System.putInt(this.mContext.getContentResolver(), LAYER_WALLPAPER, this.mWallpaperLayer);
    }

    public boolean isKeyguardHostWindow(LayoutParams attrs) {
        return attrs.type == 2004;
    }

    public int prepareAddWindowLw(WindowState win, LayoutParams attrs) {
        int result = this.mLongshotPolicy.prepareAddWindowLw(win, attrs);
        if (result != 0) {
            return result;
        }
        return super.prepareAddWindowLw(win, attrs);
    }

    public void removeWindowLw(WindowState win) {
        this.mLongshotPolicy.removeWindowLw(win);
        super.removeWindowLw(win);
    }

    public static Object getFieldName(Object instance, String variableName) {
        try {
            Field field = instance.getClass().getDeclaredField(variableName);
            field.setAccessible(true);
            return field.get(instance);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private int getWindowLayerFromType(int type, boolean canAddInternalSystemWindow) {
        switch (type) {
            case 2004:
                return 14;
            case 2010:
                return canAddInternalSystemWindow ? 107 : 10;
            case 2014:
                return 102;
            case 2015:
                return 114;
            case 2016:
                return 110;
            case 2018:
                return 116;
            case 2019:
                if (hasNavigationBar()) {
                    return 104;
                }
                break;
            case 2020:
                return 103;
            case 2021:
                return 115;
            case 2024:
                if (hasNavigationBar()) {
                    return 105;
                }
                break;
            case 2026:
                return 109;
            case 2027:
                return 108;
            case 2032:
                return 111;
            case 2036:
                return 106;
            case 2100:
                return 2;
            case 2300:
                return 113;
            case 2302:
                return 100;
            case 2303:
            case 2305:
            case 2309:
                return 101;
            case 2310:
                return 112;
            case 2311:
                return 106;
            case 2312:
                return 15;
        }
        return 0;
    }

    public int hookWindowLayerFromTypeLw(int type, boolean canAddInternalSystemWindow) {
        int layer = getWindowLayerFromType(type, canAddInternalSystemWindow);
        if (layer > 0) {
            return layer;
        }
        return super.hookWindowLayerFromTypeLw(type, canAddInternalSystemWindow);
    }

    private void checkLiveWallpaperLayer() {
        int wallpaperLayer = -1;
        if (!(this.mLauncherWin == null || this.mWallpaperWin == null)) {
            wallpaperLayer = this.mWallpaperWin.getSurfaceLayer();
        }
        if (this.mWallpaperLayer != wallpaperLayer && wallpaperLayer > 0) {
            this.mWallpaperLayer = wallpaperLayer;
            this.mMyHandler.sendMessage(this.mMyHandler.obtainMessage(18));
        }
    }

    public boolean doesNeedWaitingKeyguard() {
        return false;
    }

    public void requestDismissKeyguard() {
    }

    public void requestKeyguard(final String command) {
        if (this.mKeyguardDelegate != null) {
            String finalCommand = command;
            this.mHandler.post(new Runnable() {
                public void run() {
                    OppoPhoneWindowManager.this.mKeyguardDelegate.requestKeyguard(command);
                }
            });
        }
    }

    public boolean openKeyguardSession(IColorKeyguardSessionCallback callback, IBinder token, String module) {
        if (this.mKeyguardDelegate != null) {
            return this.mKeyguardDelegate.openKeyguardSession(callback, token, module);
        }
        return false;
    }

    public boolean isNavigationBarVisible() {
        if (this.mNavigationBar != null) {
            return this.mNavigationBar.isVisibleLw();
        }
        return false;
    }

    void showGlobalActionsInternal() {
        if (this.mGlobalActions == null) {
            this.mGlobalActions = new OppoGlobalActions(this.mContext, this.mWindowManagerFuncs);
        }
        this.mGlobalActions.setSystemUiVisibility(this.mStatusBar != null ? this.mStatusBar.isVisibleLw() : false);
        super.showGlobalActionsInternal();
    }

    void launchHomeFromHotKey() {
        if (this.mGlobalActions != null) {
            this.mGlobalActions.removePowerView();
        }
        super.launchHomeFromHotKey();
    }

    public boolean canMagnifyWindow(int windowType) {
        if (OppoScreenDragUtil.isDragState()) {
            switch (windowType) {
                case 2011:
                case 2012:
                case 2019:
                    return true;
                case 2301:
                case 2302:
                    return false;
            }
        }
        return super.canMagnifyWindow(windowType);
    }

    public boolean isShortcutsPanelShow() {
        return this.mLongshotPolicy.isShortcutsPanelShow();
    }

    private boolean isLandscape() {
        return 2 == this.mContext.getResources().getConfiguration().orientation;
    }

    private boolean isGlobalActionVisible() {
        return this.mGlobalActions != null ? this.mGlobalActions.isShowing() : false;
    }

    void takeScreenshot(int screenshotType) {
        boolean z = false;
        synchronized (this.mScreenshotLock) {
            Bundle extras = new Bundle();
            extras.putString("screenshot_source", "KeyPress");
            extras.putBoolean("statusbar_visible", this.mStatusBar != null ? this.mStatusBar.isVisibleLw() : false);
            String str = "navigationbar_visible";
            if (this.mNavigationBar != null) {
                z = this.mNavigationBar.isVisibleLw();
            }
            extras.putBoolean(str, z);
            extras.putBoolean("global_action_visible", isGlobalActionVisible());
            extras.putBoolean("screenshot_orientation", isLandscape());
            PhoneWindowManager.takeScreenshot(this.mContext, extras);
        }
    }

    void adjustOppoWindowFrameFullScreen(Rect pf, Rect df, Rect of, Rect cf, Rect dcf, LayoutParams attrs) {
        super.adjustOppoWindowFrameFullScreen(pf, df, of, cf, dcf, attrs);
        int right;
        int bottom;
        if (ColorWindowManager.LayoutParams.isForceFullScreen(attrs.type)) {
            pf.left = this.mOverscanScreenLeft;
            df.left = this.mOverscanScreenLeft;
            of.left = this.mOverscanScreenLeft;
            cf.left = this.mOverscanScreenLeft;
            dcf.left = this.mOverscanScreenLeft;
            pf.top = this.mOverscanScreenTop;
            df.top = this.mOverscanScreenTop;
            of.top = this.mOverscanScreenTop;
            cf.top = this.mOverscanScreenTop;
            dcf.top = this.mOverscanScreenTop;
            right = this.mOverscanScreenLeft + this.mOverscanScreenWidth;
            pf.right = right;
            df.right = right;
            of.right = right;
            cf.right = right;
            dcf.right = right;
            bottom = this.mOverscanScreenTop + this.mOverscanScreenHeight;
            pf.bottom = bottom;
            df.bottom = bottom;
            of.bottom = bottom;
            cf.bottom = bottom;
            dcf.bottom = bottom;
        } else if (attrs.type >= 300) {
            boolean hasStatusBar = attrs.mColorLayoutParams.hasStatusBar();
            boolean hasNavigationBar = attrs.mColorLayoutParams.hasNavigationBar();
            if (hasStatusBar || hasNavigationBar) {
                pf.left = this.mOverscanScreenLeft;
                df.left = this.mOverscanScreenLeft;
                of.left = this.mOverscanScreenLeft;
                dcf.left = this.mOverscanScreenLeft;
                pf.top = this.mOverscanScreenTop;
                df.top = this.mOverscanScreenTop;
                of.top = this.mOverscanScreenTop;
                dcf.top = this.mOverscanScreenTop;
                right = this.mOverscanScreenLeft + this.mOverscanScreenWidth;
                pf.right = right;
                df.right = right;
                of.right = right;
                dcf.right = right;
                bottom = this.mOverscanScreenTop + this.mOverscanScreenHeight;
                pf.bottom = bottom;
                df.bottom = bottom;
                of.bottom = bottom;
                dcf.bottom = bottom;
                cf.left = this.mContentLeft;
                cf.top = hasStatusBar ? this.mContentTop : pf.top;
                cf.right = this.mContentRight;
                cf.bottom = hasNavigationBar ? this.mContentBottom : pf.bottom;
            }
        }
    }

    public boolean isKeyguardShowingAndNotOccludedComp() {
        return isKeyguardShowingAndNotOccluded();
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
                    pw.println((String) this.mKeyLockIntentProcess.get(i));
                }
            }
        }
        pw.print("mDisableBottomKeyMode : ");
        pw.println(this.mDisableBottomKeyMode);
        super.dump(prefix, pw, args);
    }
}
