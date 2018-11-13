package com.android.server.display;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.display.DisplayManagerInternal.DisplayPowerCallbacks;
import android.hardware.display.DisplayManagerInternal.DisplayPowerRequest;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManagerInternal;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.Trace;
import android.provider.Settings.System;
import android.util.MathUtils;
import android.util.Slog;
import android.util.Spline;
import android.util.TimeUtils;
import android.view.WindowManagerPolicy;
import android.view.WindowManagerPolicy.ScreenOnListener;
import com.android.internal.app.IBatteryStats;
import com.android.server.LocalServices;
import com.android.server.am.BatteryStatsService;
import com.android.server.display.RampAnimator.Listener;
import com.android.server.lights.LightsService;
import com.android.server.oppo.ScreenOnCpuBoostHelper;
import com.oppo.FaceHook;
import com.oppo.FingerprintHook;
import java.io.PrintWriter;
import java.util.Timer;
import java.util.TimerTask;

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
final class DisplayPowerController implements Callbacks {
    /* renamed from: -assertionsDisabled */
    static final /* synthetic */ boolean f11-assertionsDisabled = false;
    private static final int ALWAYSON_SENSOR_RATE_US = 500000;
    public static int BRIGHTNESS_RAMP_RATE_FAST = 0;
    public static int BRIGHTNESS_RAMP_RATE_SCREENON = 0;
    public static int BRIGHTNESS_RAMP_RATE_SLOW = 0;
    private static final int COLOR_FADE_OFF_ANIMATION_DURATION_MILLIS = 100;
    private static final int COLOR_FADE_ON_ANIMATION_DURATION_MILLIS = 250;
    static boolean DEBUG = false;
    static boolean DEBUG_PANIC = false;
    private static final boolean DEBUG_PRETEND_PROXIMITY_SENSOR_ABSENT = false;
    private static final long LCD_HIGH_BRIGHTNESS_STATE_DELAY = 2000;
    private static final long LOW_DIMMING_PROTECTION_DURATION = 5000;
    private static final boolean LOW_DIMMING_PROTECTION_SUPPORT = false;
    private static final int LOW_DIMMING_PROTECTION_THRESHOLD = 40;
    private static final int MSG_PROTECT_LOW_DIMMING = 100;
    private static final int MSG_PROXIMITY_SENSOR_DEBOUNCED = 2;
    private static final int MSG_SCREEN_ON_BRIGHTNESS_BOOST = 5;
    private static final int MSG_SCREEN_ON_UNBLOCKED = 3;
    private static final int MSG_SCREEN_ON_UNBLOCKED_BY_FINGERPRINT = 4;
    private static final int MSG_UPDATE_HIGH_BRIGHTNESS_STATE = 1;
    private static final int MSG_UPDATE_POWER_STATE = 1;
    private static final String MTK_AAL_LOW_DIMMING_PROTECTION_TRIGGERED = "com.mediatek.aal.low_dimming_protection_triggered";
    public static final boolean MTK_AAL_RUNTIME_TUNING_SUPPORT = false;
    public static final boolean MTK_AAL_SUPPORT = false;
    private static final String MTK_AAL_UPDATE_CONFIG_ACTION = "com.mediatek.aal.update_config";
    public static final boolean MTK_ULTRA_DIMMING_SUPPORT = false;
    private static final long OLED_HIGH_BRIGHTNESS_STATE_DELAY = 5000;
    private static final int PROXIMITY_NEGATIVE = 0;
    private static final int PROXIMITY_POSITIVE = 1;
    private static final int PROXIMITY_SENSOR_NEGATIVE_DEBOUNCE_DELAY = 0;
    private static final int PROXIMITY_SENSOR_POSITIVE_DEBOUNCE_DELAY = 0;
    private static final int PROXIMITY_UNKNOWN = -1;
    private static final int REPORTED_TO_POLICY_SCREEN_OFF = 0;
    private static final int REPORTED_TO_POLICY_SCREEN_ON = 2;
    private static final int REPORTED_TO_POLICY_SCREEN_TURNING_ON = 1;
    private static final int SCREEN_DIM_MINIMUM_REDUCTION = 10;
    private static final String SCREEN_ON_BLOCKED_BY_FP_TRACE_NAME = "Screen on blocked by fp";
    private static final String SCREEN_ON_BLOCKED_TRACE_NAME = "Screen on blocked";
    private static final String TAG = "DisplayPowerController";
    private static final String TAG_FP = "Fingerprint_DEBUG";
    private static final int TUNING_ALI2BLI_CURVE = 1;
    private static final int TUNING_ALI2BLI_CURVE_LENGTH = 0;
    private static final int TUNING_BLI_RAMP_RATE_BRIGHTEN = 2;
    private static final int TUNING_BLI_RAMP_RATE_DARKEN = 3;
    private static final float TYPICAL_PROXIMITY_THRESHOLD = 5.0f;
    private static final boolean USE_COLOR_FADE_ON_ANIMATION = false;
    private static int mHighBrightnessMode;
    private static float mLux;
    private static OppoBrightUtils mOppoBrightUtils;
    private static int mPreHighBrightnessMode;
    public static boolean mQuickDarkToBright;
    public static boolean mScreenDimQuicklyDark;
    private static boolean mStartTimer;
    private int BRIGHTNESS_RAMP_RATE_BRIGHTEN;
    private int BRIGHTNESS_RAMP_RATE_DARKEN;
    private final boolean mAllowAutoBrightnessWhileDozingConfig;
    private final AnimatorListener mAnimatorListener;
    private boolean mAppliedAutoBrightness;
    private boolean mAppliedDimming;
    private boolean mAppliedLowPower;
    private AutomaticBrightnessController mAutomaticBrightnessController;
    private final IBatteryStats mBatteryStats;
    private final DisplayBlanker mBlanker;
    private final DisplayPowerCallbacks mCallbacks;
    private final Runnable mCleanListener;
    private boolean mColorFadeFadesConfig;
    private ObjectAnimator mColorFadeOffAnimator;
    private ObjectAnimator mColorFadeOnAnimator;
    private final Context mContext;
    private boolean mDisplayReadyLocked;
    private FaceHook mFaceHook;
    private FingerprintHook mFingerprintHook;
    private final DisplayControllerHandler mHandler;
    private Sensor mLightSensor;
    private boolean mLightSensorAlwaysOn;
    private final SensorEventListener mLightSensorAlwaysOnListener;
    private final Object mLock;
    private boolean mLowDimmingProtectionEnabled;
    private int mLowDimmingProtectionTriggerBrightness;
    private final Runnable mOnProximityNegativeRunnable;
    private final Runnable mOnProximityNegativeSuspendRunnable;
    private final Runnable mOnProximityPositiveRunnable;
    private final Runnable mOnProximityPositiveSuspendRunnable;
    private final Runnable mOnStateChangedRunnable;
    private boolean mPendingDisplayReadyBlocker;
    private int mPendingProximity;
    private long mPendingProximityDebounceTime;
    private boolean mPendingRequestChangedLocked;
    private DisplayPowerRequest mPendingRequestLocked;
    private boolean mPendingScreenOff;
    private ScreenOnUnblocker mPendingScreenOnUnblocker;
    private ScreenOnUnblockerByFingerPrint mPendingScreenOnUnblockerFromFingerPrint;
    private boolean mPendingUpdatePowerStateLocked;
    private boolean mPendingWaitForNegativeProximityLocked;
    private final PowerManagerInternal mPowerManagerInternal;
    private DisplayPowerRequest mPowerRequest;
    private DisplayPowerState mPowerState;
    private int mProximity;
    private boolean mProximityEventHandled;
    private Sensor mProximitySensor;
    private boolean mProximitySensorEnabled;
    private final SensorEventListener mProximitySensorListener;
    private float mProximityThreshold;
    private final Listener mRampAnimatorListener;
    private int mReportedScreenStateToPolicy;
    private int mScreenBrightnessDarkConfig;
    private int mScreenBrightnessDimConfig;
    private int mScreenBrightnessDozeConfig;
    private RampAnimator<DisplayPowerState> mScreenBrightnessRampAnimator;
    private final int mScreenBrightnessRangeMaximum;
    private final int mScreenBrightnessRangeMinimum;
    private boolean mScreenOffBecauseOfProximity;
    private long mScreenOnBlockStartRealTime;
    private boolean mScreenOnBlockedByFace;
    private ScreenOnCpuBoostHelper mScreenOnCpuBoostHelper;
    private int mScreenState;
    private final SensorManager mSensorManager;
    private TimerTask mTask;
    private Handler mTimehandler;
    private Timer mTimer;
    private int mTuningAli2BliSerial;
    private int mTuningBliBrightenSerial;
    private int mTuningBliDarkenSerial;
    private int[] mTuningInitCurve;
    private boolean mTuningQuicklyApply;
    private boolean mUnfinishedBusiness;
    private boolean mUseSoftwareAutoBrightnessConfig;
    private boolean mWaitingForNegativeProximity;
    private final WindowManagerPolicy mWindowManagerPolicy;
    private boolean useProximityForceSuspend;

    private final class DisplayControllerHandler extends Handler {
        public DisplayControllerHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    DisplayPowerController.this.updatePowerState();
                    return;
                case 2:
                    DisplayPowerController.this.debounceProximitySensor();
                    return;
                case 3:
                    if (DisplayPowerController.this.mPendingScreenOnUnblocker == msg.obj) {
                        DisplayPowerController.this.unblockScreenOn();
                        DisplayPowerController.this.updatePowerState();
                        return;
                    }
                    return;
                case 4:
                    if (DisplayPowerController.this.mPendingScreenOnUnblockerFromFingerPrint == msg.obj) {
                        DisplayPowerController.this.unblockScreenOnByFingerPrint(true);
                        DisplayPowerController.this.updatePowerState();
                        return;
                    }
                    return;
                case 5:
                    DisplayPowerController.mOppoBrightUtils;
                    OppoBrightUtils.mBrightnessBoost = 4;
                    return;
                case 100:
                    DisplayPowerController.this.handleProtectLowDimming();
                    return;
                default:
                    return;
            }
        }
    }

    private final class ScreenOnUnblocker implements ScreenOnListener {
        /* synthetic */ ScreenOnUnblocker(DisplayPowerController this$0, ScreenOnUnblocker screenOnUnblocker) {
            this();
        }

        private ScreenOnUnblocker() {
        }

        public void onScreenOn() {
            Message msg = DisplayPowerController.this.mHandler.obtainMessage(3, this);
            msg.setAsynchronous(true);
            Slog.d(DisplayPowerController.TAG, "ScreenOnUnblocker, onScreenOn");
            if (DisplayPowerController.this.mScreenOnBlockedByFace) {
                DisplayPowerController.this.mHandler.sendMessageDelayed(msg, 1000);
            } else {
                DisplayPowerController.this.mHandler.sendMessage(msg);
            }
        }
    }

    private final class ScreenOnUnblockerByFingerPrint implements ScreenOnListener {
        /* synthetic */ ScreenOnUnblockerByFingerPrint(DisplayPowerController this$0, ScreenOnUnblockerByFingerPrint screenOnUnblockerByFingerPrint) {
            this();
        }

        private ScreenOnUnblockerByFingerPrint() {
        }

        public void onScreenOn() {
            Message msg = DisplayPowerController.this.mHandler.obtainMessage(4, this);
            msg.setAsynchronous(true);
            DisplayPowerController.this.mHandler.sendMessage(msg);
        }
    }

    private class UpdateConfigReceiver extends BroadcastReceiver {
        private UpdateConfigReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            Slog.d(DisplayPowerController.TAG, "AALRuntimeTuning: UpdateConfigReceiver received an intent.");
            DisplayPowerController.this.sendUpdatePowerState();
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.display.DisplayPowerController.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.display.DisplayPowerController.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.display.DisplayPowerController.<clinit>():void");
    }

    private static native int nativeGetTuningInt(int i);

    private static native void nativeGetTuningIntArray(int i, int[] iArr);

    private static native int nativeGetTuningSerial(int i);

    private static native boolean nativeRuntimeTuningIsSupported();

    public static native void nativeSetDebouncedAmbientLight(int i);

    private static native int nativeSetTuningInt(int i, int i2);

    private static native int nativeSetTuningIntArray(int i, int[] iArr);

    public DisplayPowerController(Context context, DisplayPowerCallbacks callbacks, Handler handler, SensorManager sensorManager, DisplayBlanker blanker) {
        this.BRIGHTNESS_RAMP_RATE_BRIGHTEN = BRIGHTNESS_RAMP_RATE_SLOW;
        this.BRIGHTNESS_RAMP_RATE_DARKEN = BRIGHTNESS_RAMP_RATE_SLOW;
        this.mLock = new Object();
        this.mProximity = -1;
        this.mPendingProximity = -1;
        this.mPendingProximityDebounceTime = -1;
        this.mProximityEventHandled = true;
        this.useProximityForceSuspend = false;
        this.mLightSensorAlwaysOn = false;
        this.mAnimatorListener = new AnimatorListener() {
            public void onAnimationStart(Animator animation) {
            }

            public void onAnimationEnd(Animator animation) {
                DisplayPowerController.this.sendUpdatePowerState();
                if (DisplayPowerController.this.mFingerprintHook != null) {
                    DisplayPowerController.this.mFingerprintHook.onGoToSleepFinish();
                }
                if (DisplayPowerController.this.mFaceHook != null) {
                    DisplayPowerController.this.mFaceHook.onGoToSleepFinish();
                }
            }

            public void onAnimationRepeat(Animator animation) {
            }

            public void onAnimationCancel(Animator animation) {
            }
        };
        this.mRampAnimatorListener = new Listener() {
            public void onAnimationEnd() {
                DisplayPowerController.this.mTuningQuicklyApply = false;
                DisplayPowerController.this.sendUpdatePowerState();
            }
        };
        this.mPendingDisplayReadyBlocker = false;
        this.mScreenOnBlockedByFace = false;
        this.mCleanListener = new Runnable() {
            public void run() {
                DisplayPowerController.this.sendUpdatePowerState();
            }
        };
        this.mOnStateChangedRunnable = new Runnable() {
            public void run() {
                DisplayPowerController.this.mCallbacks.onStateChanged();
                DisplayPowerController.this.mCallbacks.releaseSuspendBlocker();
            }
        };
        this.mOnProximityPositiveSuspendRunnable = new Runnable() {
            public void run() {
                DisplayPowerController.this.mCallbacks.onProximityPositiveForceSuspend();
                DisplayPowerController.this.mCallbacks.releaseSuspendBlocker();
            }
        };
        this.mOnProximityNegativeSuspendRunnable = new Runnable() {
            public void run() {
                DisplayPowerController.this.mCallbacks.onProximityNegativeForceSuspend();
                DisplayPowerController.this.mCallbacks.releaseSuspendBlocker();
            }
        };
        this.mOnProximityPositiveRunnable = new Runnable() {
            public void run() {
                DisplayPowerController.this.mCallbacks.onProximityPositive();
                DisplayPowerController.this.mCallbacks.releaseSuspendBlocker();
            }
        };
        this.mOnProximityNegativeRunnable = new Runnable() {
            public void run() {
                DisplayPowerController.this.mCallbacks.onProximityNegative();
                DisplayPowerController.this.mCallbacks.releaseSuspendBlocker();
            }
        };
        this.mProximitySensorListener = new SensorEventListener() {
            public void onSensorChanged(SensorEvent event) {
                if (DisplayPowerController.this.mProximitySensorEnabled) {
                    long time = SystemClock.uptimeMillis();
                    float distance = event.values[0];
                    boolean positive = distance >= OppoBrightUtils.MIN_LUX_LIMITI && distance < DisplayPowerController.this.mProximityThreshold;
                    DisplayPowerController.this.handleProximitySensorEvent(time, positive);
                }
            }

            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };
        this.mTuningInitCurve = null;
        this.mTuningAli2BliSerial = 0;
        this.mTuningBliBrightenSerial = -1;
        this.mTuningBliDarkenSerial = -1;
        this.mTuningQuicklyApply = false;
        this.mLowDimmingProtectionEnabled = false;
        this.mLowDimmingProtectionTriggerBrightness = 0;
        this.mLightSensorAlwaysOnListener = new SensorEventListener() {
            public void onSensorChanged(SensorEvent event) {
                if (DisplayPowerController.this.mLightSensorAlwaysOn && !DisplayPowerController.this.mScreenBrightnessRampAnimator.isAnimating()) {
                    float lLux = event.values[0];
                    DisplayPowerController.mLux = lLux;
                    if (OppoBrightUtils.mHighBrightnessModeSupport) {
                        DisplayPowerController.mOppoBrightUtils;
                        if (OppoBrightUtils.mCameraMode == 1 && lLux >= 10000.0f) {
                            int i = LightsService.mScreenBrightness;
                            DisplayPowerController.mOppoBrightUtils;
                            if (i == OppoBrightUtils.mMaxBrightness) {
                                DisplayPowerController.mHighBrightnessMode = 8;
                            }
                        }
                        DisplayPowerController.mHighBrightnessMode = 0;
                    } else if (lLux >= 10000.0f) {
                        DisplayPowerController.mHighBrightnessMode = 1;
                    } else {
                        DisplayPowerController.mHighBrightnessMode = 0;
                    }
                    if (DisplayPowerController.mHighBrightnessMode != DisplayPowerController.mPreHighBrightnessMode) {
                        DisplayPowerController.mPreHighBrightnessMode = DisplayPowerController.mHighBrightnessMode;
                        DisplayPowerController.this.stopTimer();
                        DisplayPowerController.this.startTimer();
                    }
                }
            }

            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };
        this.mTimehandler = new Handler() {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what != 1) {
                    return;
                }
                if (DisplayPowerController.this.mScreenBrightnessRampAnimator.isAnimating()) {
                    DisplayPowerController.this.stopTimer();
                    DisplayPowerController.this.startTimer();
                    return;
                }
                if (DisplayPowerController.mHighBrightnessMode == DisplayPowerController.mPreHighBrightnessMode) {
                    DisplayPowerController.mOppoBrightUtils.setHighBrightness(DisplayPowerController.mHighBrightnessMode);
                }
                DisplayPowerController.this.stopTimer();
            }
        };
        this.mHandler = new DisplayControllerHandler(handler.getLooper());
        this.mCallbacks = callbacks;
        this.mBatteryStats = BatteryStatsService.getService();
        this.mSensorManager = sensorManager;
        this.mWindowManagerPolicy = (WindowManagerPolicy) LocalServices.getService(WindowManagerPolicy.class);
        this.mPowerManagerInternal = (PowerManagerInternal) LocalServices.getService(PowerManagerInternal.class);
        this.mScreenOnCpuBoostHelper = new ScreenOnCpuBoostHelper();
        this.mBlanker = blanker;
        this.mContext = context;
        this.mScreenState = -1;
        mQuickDarkToBright = false;
        mOppoBrightUtils = OppoBrightUtils.getInstance();
        mOppoBrightUtils.isSpecialSensor();
        mOppoBrightUtils.configAutoBrightness();
        OppoBrightUtils oppoBrightUtils = mOppoBrightUtils;
        DEBUG = OppoBrightUtils.DEBUG;
        this.mLightSensor = this.mSensorManager.getDefaultSensor(5);
        Resources resources = context.getResources();
        int screenBrightnessSettingMinimum = clampAbsoluteBrightness(resources.getInteger(17694818), false);
        this.mScreenBrightnessDozeConfig = clampAbsoluteBrightness(resources.getInteger(17694821), false);
        this.mScreenBrightnessDimConfig = clampAbsoluteBrightness(resources.getInteger(17694826), false);
        this.mScreenBrightnessDarkConfig = clampAbsoluteBrightness(resources.getInteger(17694827), false);
        if (this.mScreenBrightnessDarkConfig > this.mScreenBrightnessDimConfig) {
            Slog.w(TAG, "Expected config_screenBrightnessDark (" + this.mScreenBrightnessDarkConfig + ") to be less than or equal to " + "config_screenBrightnessDim (" + this.mScreenBrightnessDimConfig + ").");
        }
        if (this.mScreenBrightnessDarkConfig > this.mScreenBrightnessDimConfig) {
            Slog.w(TAG, "Expected config_screenBrightnessDark (" + this.mScreenBrightnessDarkConfig + ") to be less than or equal to " + "config_screenBrightnessSettingMinimum (" + screenBrightnessSettingMinimum + ").");
        }
        int screenBrightnessRangeMinimum = Math.min(Math.min(screenBrightnessSettingMinimum, this.mScreenBrightnessDimConfig), this.mScreenBrightnessDarkConfig);
        screenBrightnessRangeMinimum = mOppoBrightUtils.getMinimumScreenBrightnessSetting();
        this.mScreenBrightnessRangeMaximum = PowerManager.BRIGHTNESS_MULTIBITS_ON;
        this.mUseSoftwareAutoBrightnessConfig = resources.getBoolean(17956897);
        this.mAllowAutoBrightnessWhileDozingConfig = resources.getBoolean(17956939);
        int lightSensorRate = resources.getInteger(17694824);
        long brighteningLightDebounce = (long) resources.getInteger(17694822);
        long darkeningLightDebounce = (long) resources.getInteger(17694823);
        if (OppoBrightUtils.mBrightnessBitsConfig == 3) {
            darkeningLightDebounce = 1000;
        }
        boolean autoBrightnessResetAmbientLuxAfterWarmUp = resources.getBoolean(17956940);
        int ambientLightHorizon = resources.getInteger(17694825);
        float autoBrightnessAdjustmentMaxGamma = resources.getFraction(18022401, 1, 1);
        if (this.mUseSoftwareAutoBrightnessConfig) {
            int[] lux = mOppoBrightUtils.readAutoBrightnessLuxConfig();
            int[] screenBrightness = mOppoBrightUtils.readAutoBrightnessConfig();
            int lightSensorWarmUpTimeConfig = resources.getInteger(17694828);
            float dozeScaleFactor = resources.getFraction(18022402, 1, 1);
            Spline screenAutoBrightnessSpline = createAutoBrightnessSpline(lux, screenBrightness, false);
            if (screenAutoBrightnessSpline == null) {
                Slog.e(TAG, "Error in config.xml.  config_autoBrightnessLcdBacklightValues (size " + screenBrightness.length + ") " + "must be monotic and have exactly one more entry than " + "config_autoBrightnessLevels (size " + lux.length + ") " + "which must be strictly increasing.  " + "Auto-brightness will be disabled.");
                this.mUseSoftwareAutoBrightnessConfig = false;
            } else {
                int bottom = clampAbsoluteBrightness(screenBrightness[0], false);
                if (this.mScreenBrightnessDarkConfig > bottom) {
                    Slog.w(TAG, "config_screenBrightnessDark (" + this.mScreenBrightnessDarkConfig + ") should be less than or equal to the first value of " + "config_autoBrightnessLcdBacklightValues (" + bottom + ").");
                }
                if (bottom < screenBrightnessRangeMinimum) {
                    screenBrightnessRangeMinimum = bottom;
                }
                this.mAutomaticBrightnessController = new AutomaticBrightnessController(this, handler.getLooper(), this.mContext, sensorManager, screenAutoBrightnessSpline, lightSensorWarmUpTimeConfig, screenBrightnessRangeMinimum, this.mScreenBrightnessRangeMaximum, dozeScaleFactor, lightSensorRate, brighteningLightDebounce, darkeningLightDebounce, autoBrightnessResetAmbientLuxAfterWarmUp, ambientLightHorizon, autoBrightnessAdjustmentMaxGamma);
            }
            this.mScreenBrightnessDozeConfig = screenBrightness[0];
            this.mScreenBrightnessDimConfig = screenBrightness[0];
            this.mScreenBrightnessDarkConfig = screenBrightness[0];
        }
        this.mScreenBrightnessRangeMinimum = screenBrightnessRangeMinimum;
        this.mColorFadeFadesConfig = resources.getBoolean(17956902);
        this.mProximitySensor = this.mSensorManager.getDefaultSensor(8);
        if (this.mProximitySensor != null) {
            this.mProximityThreshold = Math.min(this.mProximitySensor.getMaximumRange(), TYPICAL_PROXIMITY_THRESHOLD);
        }
        this.mFingerprintHook = FingerprintHook.getInstance();
        this.mFaceHook = FaceHook.getInstance();
    }

    public boolean isProximitySensorAvailable() {
        return this.mProximitySensor != null;
    }

    public void setIPOScreenOnDelay(int msec) {
        this.mPowerState.setIPOScreenOnDelay(msec);
    }

    public void setUseProximityForceSuspend(boolean enable) {
        if (!this.useProximityForceSuspend) {
            this.useProximityForceSuspend = enable;
        }
    }

    public boolean requestPowerState(DisplayPowerRequest request, boolean waitForNegativeProximity) {
        boolean z;
        synchronized (this.mLock) {
            boolean changed = false;
            if (waitForNegativeProximity) {
                if (!this.mPendingWaitForNegativeProximityLocked) {
                    this.mPendingWaitForNegativeProximityLocked = true;
                    changed = true;
                }
            }
            if (this.mPendingRequestLocked == null) {
                this.mPendingRequestLocked = new DisplayPowerRequest(request);
                changed = true;
            } else if (!this.mPendingRequestLocked.equals(request)) {
                this.mPendingRequestLocked.copyFrom(request);
                changed = true;
            }
            if (changed) {
                this.mDisplayReadyLocked = false;
            }
            if (changed && !this.mPendingRequestChangedLocked) {
                this.mPendingRequestChangedLocked = true;
                sendUpdatePowerStateLocked();
            }
            if (DEBUG && changed) {
                Slog.d(TAG, "requestPowerState: " + request + ", waitForNegativeProximity=" + waitForNegativeProximity + ", changed=" + changed);
            }
            z = this.mDisplayReadyLocked;
        }
        return z;
    }

    private void sendUpdatePowerState() {
        synchronized (this.mLock) {
            sendUpdatePowerStateLocked();
        }
    }

    private void sendUpdatePowerStateLocked() {
        if (!this.mPendingUpdatePowerStateLocked) {
            this.mPendingUpdatePowerStateLocked = true;
            Message msg = this.mHandler.obtainMessage(1);
            msg.setAsynchronous(true);
            this.mHandler.sendMessage(msg);
        }
    }

    private void initialize() {
        this.mPowerState = new DisplayPowerState(this.mBlanker, new ColorFade(0));
        this.mColorFadeOnAnimator = ObjectAnimator.ofFloat(this.mPowerState, DisplayPowerState.COLOR_FADE_LEVEL, new float[]{OppoBrightUtils.MIN_LUX_LIMITI, 1.0f});
        this.mColorFadeOnAnimator.setDuration(250);
        this.mColorFadeOnAnimator.addListener(this.mAnimatorListener);
        this.mColorFadeOffAnimator = ObjectAnimator.ofFloat(this.mPowerState, DisplayPowerState.COLOR_FADE_LEVEL, new float[]{1.0f, OppoBrightUtils.MIN_LUX_LIMITI});
        this.mColorFadeOffAnimator.setDuration(100);
        this.mColorFadeOffAnimator.addListener(this.mAnimatorListener);
        this.mScreenBrightnessRampAnimator = new RampAnimator(this.mPowerState, DisplayPowerState.SCREEN_BRIGHTNESS);
        this.mScreenBrightnessRampAnimator.setListener(this.mRampAnimatorListener);
        try {
            this.mBatteryStats.noteScreenState(this.mPowerState.getScreenState());
            this.mBatteryStats.noteScreenBrightness(this.mPowerState.getScreenBrightness());
        } catch (RemoteException e) {
        }
    }

    /* JADX WARNING: Missing block: B:19:0x008e, code:
            if (r16 == false) goto L_0x0093;
     */
    /* JADX WARNING: Missing block: B:20:0x0090, code:
            initialize();
     */
    /* JADX WARNING: Missing block: B:22:0x009a, code:
            if (r23.mPowerRequest.policy != 2) goto L_0x00a6;
     */
    /* JADX WARNING: Missing block: B:24:0x00a1, code:
            if (r23.mScreenState != 3) goto L_0x00a6;
     */
    /* JADX WARNING: Missing block: B:25:0x00a3, code:
            mQuickDarkToBright = true;
     */
    /* JADX WARNING: Missing block: B:26:0x00a6, code:
            r23.mScreenState = r23.mPowerRequest.policy;
            r13 = -1;
            r18 = false;
     */
    /* JADX WARNING: Missing block: B:27:0x00b9, code:
            switch(r23.mPowerRequest.policy) {
                case 0: goto L_0x0116;
                case 1: goto L_0x011b;
                default: goto L_0x00bc;
            };
     */
    /* JADX WARNING: Missing block: B:28:0x00bc, code:
            r22 = 2;
     */
    /* JADX WARNING: Missing block: B:30:0x00c0, code:
            if (-assertionsDisabled != false) goto L_0x013d;
     */
    /* JADX WARNING: Missing block: B:31:0x00c2, code:
            if (r22 == 0) goto L_0x013b;
     */
    /* JADX WARNING: Missing block: B:32:0x00c4, code:
            r4 = 1;
     */
    /* JADX WARNING: Missing block: B:33:0x00c5, code:
            if (r4 != null) goto L_0x013d;
     */
    /* JADX WARNING: Missing block: B:35:0x00cc, code:
            throw new java.lang.AssertionError();
     */
    /* JADX WARNING: Missing block: B:48:0x0116, code:
            r22 = 1;
            r18 = true;
     */
    /* JADX WARNING: Missing block: B:50:0x0121, code:
            if (r23.mPowerRequest.dozeScreenState == 0) goto L_0x0138;
     */
    /* JADX WARNING: Missing block: B:51:0x0123, code:
            r22 = r23.mPowerRequest.dozeScreenState;
     */
    /* JADX WARNING: Missing block: B:53:0x012f, code:
            if (r23.mAllowAutoBrightnessWhileDozingConfig != false) goto L_0x00be;
     */
    /* JADX WARNING: Missing block: B:54:0x0131, code:
            r13 = r23.mPowerRequest.dozeScreenBrightness;
     */
    /* JADX WARNING: Missing block: B:55:0x0138, code:
            r22 = 3;
     */
    /* JADX WARNING: Missing block: B:56:0x013b, code:
            r4 = null;
     */
    /* JADX WARNING: Missing block: B:58:0x0141, code:
            if (r23.mProximitySensor == null) goto L_0x0599;
     */
    /* JADX WARNING: Missing block: B:60:0x0147, code:
            if (r23.useProximityForceSuspend == false) goto L_0x052f;
     */
    /* JADX WARNING: Missing block: B:62:0x014f, code:
            if (r23.mPowerRequest.useProximitySensor == false) goto L_0x0492;
     */
    /* JADX WARNING: Missing block: B:63:0x0151, code:
            setProximitySensorEnabled(true);
     */
    /* JADX WARNING: Missing block: B:64:0x015b, code:
            if (r23.mProximityEventHandled != false) goto L_0x0483;
     */
    /* JADX WARNING: Missing block: B:65:0x015d, code:
            android.util.Slog.i(TAG, "mProximity = " + proximityToString(r23.mProximity));
     */
    /* JADX WARNING: Missing block: B:66:0x0184, code:
            if (r23.mProximity != 1) goto L_0x0478;
     */
    /* JADX WARNING: Missing block: B:67:0x0186, code:
            sendOnProximityPositiveSuspendWithWakelock();
     */
    /* JADX WARNING: Missing block: B:68:0x0189, code:
            r23.mProximityEventHandled = true;
     */
    /* JADX WARNING: Missing block: B:70:0x0192, code:
            if (r23.useProximityForceSuspend != false) goto L_0x019c;
     */
    /* JADX WARNING: Missing block: B:72:0x0198, code:
            if (r23.mScreenOffBecauseOfProximity == false) goto L_0x019c;
     */
    /* JADX WARNING: Missing block: B:73:0x019a, code:
            r22 = 1;
     */
    /* JADX WARNING: Missing block: B:74:0x019c, code:
            animateScreenStateChange(r22, r18);
            r22 = r23.mPowerState.getScreenState();
     */
    /* JADX WARNING: Missing block: B:75:0x01b0, code:
            if (r22 != 1) goto L_0x01b3;
     */
    /* JADX WARNING: Missing block: B:76:0x01b2, code:
            r13 = 0;
     */
    /* JADX WARNING: Missing block: B:77:0x01b3, code:
            r11 = false;
     */
    /* JADX WARNING: Missing block: B:78:0x01b8, code:
            if (r23.mAutomaticBrightnessController == null) goto L_0x025e;
     */
    /* JADX WARNING: Missing block: B:80:0x01be, code:
            if (r23.mAllowAutoBrightnessWhileDozingConfig == false) goto L_0x05a3;
     */
    /* JADX WARNING: Missing block: B:82:0x01c3, code:
            if (r22 == 3) goto L_0x01ca;
     */
    /* JADX WARNING: Missing block: B:84:0x01c8, code:
            if (r22 != 4) goto L_0x05a0;
     */
    /* JADX WARNING: Missing block: B:85:0x01ca, code:
            r12 = true;
     */
    /* JADX WARNING: Missing block: B:87:0x01d1, code:
            if (r23.mPowerRequest.useAutoBrightness == false) goto L_0x05a9;
     */
    /* JADX WARNING: Missing block: B:89:0x01d6, code:
            if (r22 == 2) goto L_0x01da;
     */
    /* JADX WARNING: Missing block: B:90:0x01d8, code:
            if (r12 == false) goto L_0x05a9;
     */
    /* JADX WARNING: Missing block: B:91:0x01da, code:
            if (r13 >= 0) goto L_0x05a6;
     */
    /* JADX WARNING: Missing block: B:92:0x01dc, code:
            r11 = true;
     */
    /* JADX WARNING: Missing block: B:93:0x01dd, code:
            if (r10 == false) goto L_0x05ac;
     */
    /* JADX WARNING: Missing block: B:94:0x01df, code:
            r8 = r23.mPowerRequest.brightnessSetByUser;
     */
    /* JADX WARNING: Missing block: B:96:0x01e7, code:
            if (DEBUG == false) goto L_0x023f;
     */
    /* JADX WARNING: Missing block: B:97:0x01e9, code:
            android.util.Slog.d(TAG, "ABC configure: enabledInDoze=" + r23.mAllowAutoBrightnessWhileDozingConfig + ", lowDimmingProtectionEnabled=" + r23.mLowDimmingProtectionEnabled + ", adjustment=" + r23.mPowerRequest.screenAutoBrightnessAdjustment + ", state=" + r22 + ", brightness=" + r13);
     */
    /* JADX WARNING: Missing block: B:98:0x023f, code:
            r4 = r23.mAutomaticBrightnessController;
     */
    /* JADX WARNING: Missing block: B:99:0x0243, code:
            if (r11 != false) goto L_0x05af;
     */
    /* JADX WARNING: Missing block: B:100:0x0245, code:
            r5 = r23.mLowDimmingProtectionEnabled;
     */
    /* JADX WARNING: Missing block: B:101:0x0249, code:
            r6 = r23.mPowerRequest.screenAutoBrightnessAdjustment;
     */
    /* JADX WARNING: Missing block: B:102:0x0252, code:
            if (r22 == 2) goto L_0x05b2;
     */
    /* JADX WARNING: Missing block: B:103:0x0254, code:
            r7 = true;
     */
    /* JADX WARNING: Missing block: B:104:0x0255, code:
            r4.configure(r5, r6, r7, r8, r23.mPowerRequest.useTwilight);
     */
    /* JADX WARNING: Missing block: B:106:0x0264, code:
            if (r23.mPowerRequest.boostScreenBrightness == false) goto L_0x026a;
     */
    /* JADX WARNING: Missing block: B:107:0x0266, code:
            if (r13 == 0) goto L_0x026a;
     */
    /* JADX WARNING: Missing block: B:108:0x0268, code:
            r13 = android.os.PowerManager.BRIGHTNESS_MULTIBITS_ON;
     */
    /* JADX WARNING: Missing block: B:109:0x026a, code:
            r21 = false;
     */
    /* JADX WARNING: Missing block: B:110:0x026c, code:
            if (r13 >= 0) goto L_0x05c0;
     */
    /* JADX WARNING: Missing block: B:111:0x026e, code:
            if (r11 == false) goto L_0x0278;
     */
    /* JADX WARNING: Missing block: B:112:0x0270, code:
            r13 = r23.mAutomaticBrightnessController.getAutomaticScreenBrightness();
     */
    /* JADX WARNING: Missing block: B:113:0x0278, code:
            if (r13 < 0) goto L_0x05b9;
     */
    /* JADX WARNING: Missing block: B:114:0x027a, code:
            r13 = clampScreenBrightness(r13);
     */
    /* JADX WARNING: Missing block: B:115:0x0284, code:
            if (r23.mAppliedAutoBrightness == false) goto L_0x0288;
     */
    /* JADX WARNING: Missing block: B:116:0x0286, code:
            if (r10 == false) goto L_0x05b5;
     */
    /* JADX WARNING: Missing block: B:117:0x0288, code:
            r4 = mOppoBrightUtils;
     */
    /* JADX WARNING: Missing block: B:118:0x028d, code:
            if (com.android.server.display.OppoBrightUtils.mBrightnessBoost != 2) goto L_0x0291;
     */
    /* JADX WARNING: Missing block: B:119:0x028f, code:
            r21 = false;
     */
    /* JADX WARNING: Missing block: B:120:0x0291, code:
            r23.mAppliedAutoBrightness = true;
     */
    /* JADX WARNING: Missing block: B:121:0x0296, code:
            if (r13 >= 0) goto L_0x02a6;
     */
    /* JADX WARNING: Missing block: B:123:0x029b, code:
            if (r22 == 3) goto L_0x02a2;
     */
    /* JADX WARNING: Missing block: B:125:0x02a0, code:
            if (r22 != 4) goto L_0x02a6;
     */
    /* JADX WARNING: Missing block: B:126:0x02a2, code:
            r13 = r23.mScreenBrightnessDozeConfig;
     */
    /* JADX WARNING: Missing block: B:127:0x02a6, code:
            if (r13 >= 0) goto L_0x02b4;
     */
    /* JADX WARNING: Missing block: B:128:0x02a8, code:
            r13 = clampScreenBrightness(r23.mPowerRequest.screenBrightness);
     */
    /* JADX WARNING: Missing block: B:130:0x02bb, code:
            if (r23.mPowerRequest.policy != 2) goto L_0x05cb;
     */
    /* JADX WARNING: Missing block: B:132:0x02c1, code:
            if (r13 <= r23.mScreenBrightnessRangeMinimum) goto L_0x02d9;
     */
    /* JADX WARNING: Missing block: B:134:0x02c5, code:
            if ((r13 - 10) <= 0) goto L_0x05c7;
     */
    /* JADX WARNING: Missing block: B:135:0x02c7, code:
            r13 = r13 - 10;
     */
    /* JADX WARNING: Missing block: B:136:0x02c9, code:
            r13 = java.lang.Math.max(java.lang.Math.min(r13, r23.mScreenBrightnessDimConfig), r23.mScreenBrightnessRangeMinimum);
     */
    /* JADX WARNING: Missing block: B:138:0x02dd, code:
            if (r23.mAppliedDimming != false) goto L_0x02e1;
     */
    /* JADX WARNING: Missing block: B:139:0x02df, code:
            r21 = false;
     */
    /* JADX WARNING: Missing block: B:140:0x02e1, code:
            r23.mAppliedDimming = true;
            mScreenDimQuicklyDark = true;
            r4 = mOppoBrightUtils;
            com.android.server.display.OppoBrightUtils.mManualSetAutoBrightness = false;
     */
    /* JADX WARNING: Missing block: B:142:0x02f4, code:
            if (r23.mPowerRequest.lowPowerMode == false) goto L_0x05da;
     */
    /* JADX WARNING: Missing block: B:144:0x02fa, code:
            if (r13 <= r23.mScreenBrightnessRangeMinimum) goto L_0x0306;
     */
    /* JADX WARNING: Missing block: B:145:0x02fc, code:
            r13 = java.lang.Math.max(r13 / 2, r23.mScreenBrightnessRangeMinimum);
     */
    /* JADX WARNING: Missing block: B:147:0x030a, code:
            if (r23.mAppliedLowPower != false) goto L_0x030e;
     */
    /* JADX WARNING: Missing block: B:148:0x030c, code:
            r21 = false;
     */
    /* JADX WARNING: Missing block: B:149:0x030e, code:
            r23.mAppliedLowPower = true;
     */
    /* JADX WARNING: Missing block: B:151:0x0315, code:
            if (DEBUG_PANIC == false) goto L_0x0351;
     */
    /* JADX WARNING: Missing block: B:152:0x0317, code:
            android.util.Slog.d(TAG, "updatePowerState: state = " + r22 + ", brightness = " + r13 + "mColorFadeOffAnimator.isStarted() : " + r23.mColorFadeOffAnimator.isStarted());
     */
    /* JADX WARNING: Missing block: B:154:0x0355, code:
            if (r23.mPendingScreenOff != false) goto L_0x03c6;
     */
    /* JADX WARNING: Missing block: B:156:0x035a, code:
            if (r22 == 2) goto L_0x0361;
     */
    /* JADX WARNING: Missing block: B:158:0x035f, code:
            if (r22 != 3) goto L_0x03c6;
     */
    /* JADX WARNING: Missing block: B:159:0x0361, code:
            r19 = BRIGHTNESS_RAMP_RATE_SLOW;
     */
    /* JADX WARNING: Missing block: B:160:0x0363, code:
            if (r11 == false) goto L_0x05e9;
     */
    /* JADX WARNING: Missing block: B:161:0x0365, code:
            r19 = r23.mAutomaticBrightnessController.mAutoRate;
     */
    /* JADX WARNING: Missing block: B:162:0x036d, code:
            if (r21 == false) goto L_0x0376;
     */
    /* JADX WARNING: Missing block: B:163:0x036f, code:
            r4 = mOppoBrightUtils;
     */
    /* JADX WARNING: Missing block: B:164:0x0374, code:
            if (com.android.server.display.OppoBrightUtils.mBrightnessBoost != 2) goto L_0x0378;
     */
    /* JADX WARNING: Missing block: B:165:0x0376, code:
            r19 = BRIGHTNESS_RAMP_RATE_FAST;
     */
    /* JADX WARNING: Missing block: B:166:0x0378, code:
            r4 = mOppoBrightUtils;
     */
    /* JADX WARNING: Missing block: B:167:0x037d, code:
            if (com.android.server.display.OppoBrightUtils.mBrightnessBoost != 3) goto L_0x039b;
     */
    /* JADX WARNING: Missing block: B:168:0x037f, code:
            r19 = BRIGHTNESS_RAMP_RATE_FAST;
            r23.mHandler.removeMessages(5);
            r23.mHandler.sendMessageDelayed(r23.mHandler.obtainMessage(5), 600);
     */
    /* JADX WARNING: Missing block: B:170:0x039d, code:
            if (mQuickDarkToBright == false) goto L_0x03a4;
     */
    /* JADX WARNING: Missing block: B:171:0x039f, code:
            mQuickDarkToBright = false;
            r19 = BRIGHTNESS_RAMP_RATE_SCREENON;
     */
    /* JADX WARNING: Missing block: B:172:0x03a4, code:
            r4 = mOppoBrightUtils;
     */
    /* JADX WARNING: Missing block: B:173:0x03a8, code:
            if (com.android.server.display.OppoBrightUtils.mPocketRingingState == false) goto L_0x03b2;
     */
    /* JADX WARNING: Missing block: B:174:0x03aa, code:
            mOppoBrightUtils.setPocketRingingState(false);
            r19 = BRIGHTNESS_RAMP_RATE_FAST;
     */
    /* JADX WARNING: Missing block: B:175:0x03b2, code:
            r4 = mOppoBrightUtils;
     */
    /* JADX WARNING: Missing block: B:176:0x03b7, code:
            if (com.android.server.display.OppoBrightUtils.mInverseMode != 1) goto L_0x03bf;
     */
    /* JADX WARNING: Missing block: B:177:0x03b9, code:
            r13 = mOppoBrightUtils.adjustInverseModeBrightness(r13);
     */
    /* JADX WARNING: Missing block: B:178:0x03bf, code:
            animateScreenBrightness(r13, r19);
     */
    /* JADX WARNING: Missing block: B:180:0x03ca, code:
            if (r23.mPendingScreenOnUnblocker != null) goto L_0x03d2;
     */
    /* JADX WARNING: Missing block: B:182:0x03d0, code:
            if (r23.mPendingDisplayReadyBlocker == false) goto L_0x05f1;
     */
    /* JADX WARNING: Missing block: B:183:0x03d2, code:
            r20 = false;
     */
    /* JADX WARNING: Missing block: B:184:0x03d4, code:
            if (r20 == false) goto L_0x0616;
     */
    /* JADX WARNING: Missing block: B:186:0x03de, code:
            if (r23.mScreenBrightnessRampAnimator.isAnimating() == false) goto L_0x0613;
     */
    /* JADX WARNING: Missing block: B:187:0x03e0, code:
            r14 = false;
     */
    /* JADX WARNING: Missing block: B:188:0x03e1, code:
            if (r20 == false) goto L_0x03fb;
     */
    /* JADX WARNING: Missing block: B:190:0x03e6, code:
            if (r22 == 1) goto L_0x03fb;
     */
    /* JADX WARNING: Missing block: B:192:0x03ed, code:
            if (r23.mReportedScreenStateToPolicy != 1) goto L_0x03fb;
     */
    /* JADX WARNING: Missing block: B:193:0x03ef, code:
            r23.mReportedScreenStateToPolicy = 2;
            r23.mWindowManagerPolicy.screenTurnedOn();
     */
    /* JADX WARNING: Missing block: B:194:0x03fb, code:
            if (r14 != false) goto L_0x0403;
     */
    /* JADX WARNING: Missing block: B:196:0x0401, code:
            if (r23.mUnfinishedBusiness == false) goto L_0x0619;
     */
    /* JADX WARNING: Missing block: B:197:0x0403, code:
            if (r20 == false) goto L_0x0456;
     */
    /* JADX WARNING: Missing block: B:198:0x0405, code:
            if (r17 == false) goto L_0x0456;
     */
    /* JADX WARNING: Missing block: B:200:0x040b, code:
            if (r23.mFingerprintHook == null) goto L_0x041e;
     */
    /* JADX WARNING: Missing block: B:202:0x0410, code:
            if (r22 == 2) goto L_0x0417;
     */
    /* JADX WARNING: Missing block: B:204:0x0415, code:
            if (r22 != 3) goto L_0x041e;
     */
    /* JADX WARNING: Missing block: B:205:0x0417, code:
            r23.mFingerprintHook.onAnimateScreenBrightness(r13);
     */
    /* JADX WARNING: Missing block: B:207:0x0422, code:
            if (r23.mFaceHook == null) goto L_0x0435;
     */
    /* JADX WARNING: Missing block: B:209:0x0427, code:
            if (r22 == 2) goto L_0x042e;
     */
    /* JADX WARNING: Missing block: B:211:0x042c, code:
            if (r22 != 3) goto L_0x0435;
     */
    /* JADX WARNING: Missing block: B:212:0x042e, code:
            r23.mFaceHook.onAnimateScreenBrightness(r13);
     */
    /* JADX WARNING: Missing block: B:213:0x0435, code:
            r5 = r23.mLock;
     */
    /* JADX WARNING: Missing block: B:214:0x0439, code:
            monitor-enter(r5);
     */
    /* JADX WARNING: Missing block: B:217:0x043e, code:
            if (r23.mPendingRequestChangedLocked != false) goto L_0x0452;
     */
    /* JADX WARNING: Missing block: B:218:0x0440, code:
            r23.mDisplayReadyLocked = true;
     */
    /* JADX WARNING: Missing block: B:219:0x0447, code:
            if (DEBUG == false) goto L_0x0452;
     */
    /* JADX WARNING: Missing block: B:220:0x0449, code:
            android.util.Slog.d(TAG, "Display ready!");
     */
    /* JADX WARNING: Missing block: B:221:0x0452, code:
            monitor-exit(r5);
     */
    /* JADX WARNING: Missing block: B:222:0x0453, code:
            sendOnStateChangedWithWakelock();
     */
    /* JADX WARNING: Missing block: B:223:0x0456, code:
            if (r14 == false) goto L_0x0477;
     */
    /* JADX WARNING: Missing block: B:225:0x045c, code:
            if (r23.mUnfinishedBusiness == false) goto L_0x0477;
     */
    /* JADX WARNING: Missing block: B:227:0x0460, code:
            if (DEBUG == false) goto L_0x046b;
     */
    /* JADX WARNING: Missing block: B:228:0x0462, code:
            android.util.Slog.d(TAG, "Finished business...");
     */
    /* JADX WARNING: Missing block: B:229:0x046b, code:
            r23.mUnfinishedBusiness = false;
            r23.mCallbacks.releaseSuspendBlocker();
     */
    /* JADX WARNING: Missing block: B:230:0x0477, code:
            return;
     */
    /* JADX WARNING: Missing block: B:232:0x047c, code:
            if (r23.mProximity != 0) goto L_0x0189;
     */
    /* JADX WARNING: Missing block: B:233:0x047e, code:
            sendOnProximityNegativeSuspendWithWakelock();
     */
    /* JADX WARNING: Missing block: B:235:0x0485, code:
            if (DEBUG == false) goto L_0x018e;
     */
    /* JADX WARNING: Missing block: B:236:0x0487, code:
            android.util.Slog.i(TAG, "the last proximity event has been handled");
     */
    /* JADX WARNING: Missing block: B:238:0x0496, code:
            if (r23.mProximitySensorEnabled == false) goto L_0x018e;
     */
    /* JADX WARNING: Missing block: B:239:0x0498, code:
            android.util.Slog.i(TAG, "mPowerRequest.useProximitySensor = " + r23.mPowerRequest.useProximitySensor + ", mWaitingForNegativeProximity = " + r23.mWaitingForNegativeProximity + ", state = " + r22);
     */
    /* JADX WARNING: Missing block: B:240:0x04d8, code:
            if (r23.mWaitingForNegativeProximity == false) goto L_0x04fa;
     */
    /* JADX WARNING: Missing block: B:242:0x04df, code:
            if (r23.mProximity != 1) goto L_0x04fa;
     */
    /* JADX WARNING: Missing block: B:244:0x04e4, code:
            if (r22 == 1) goto L_0x04eb;
     */
    /* JADX WARNING: Missing block: B:246:0x04e9, code:
            if (r22 != 3) goto L_0x04f3;
     */
    /* JADX WARNING: Missing block: B:247:0x04eb, code:
            setProximitySensorEnabled(true);
     */
    /* JADX WARNING: Missing block: B:249:0x04f8, code:
            if (getScreenState() != 1) goto L_0x04eb;
     */
    /* JADX WARNING: Missing block: B:250:0x04fa, code:
            setProximitySensorEnabled(false);
     */
    /* JADX WARNING: Missing block: B:251:0x0503, code:
            if (r22 == 1) goto L_0x050a;
     */
    /* JADX WARNING: Missing block: B:253:0x0508, code:
            if (r22 != 3) goto L_0x0527;
     */
    /* JADX WARNING: Missing block: B:254:0x050a, code:
            android.util.Slog.i(TAG, "turn on lcd light due to proximity released");
            sendOnProximityNegativeSuspendWithWakelock();
     */
    /* JADX WARNING: Missing block: B:255:0x0516, code:
            r23.mScreenOffBecauseOfProximity = false;
            r23.mWaitingForNegativeProximity = false;
            r23.mProximityEventHandled = true;
     */
    /* JADX WARNING: Missing block: B:257:0x052c, code:
            if (getScreenState() == 1) goto L_0x0516;
     */
    /* JADX WARNING: Missing block: B:259:0x0535, code:
            if (r23.mPowerRequest.useProximitySensor == false) goto L_0x056e;
     */
    /* JADX WARNING: Missing block: B:261:0x053a, code:
            if (r22 == 1) goto L_0x056e;
     */
    /* JADX WARNING: Missing block: B:262:0x053c, code:
            setProximitySensorEnabled(true);
     */
    /* JADX WARNING: Missing block: B:263:0x0546, code:
            if (r23.mScreenOffBecauseOfProximity != false) goto L_0x0557;
     */
    /* JADX WARNING: Missing block: B:265:0x054d, code:
            if (r23.mProximity != 1) goto L_0x0557;
     */
    /* JADX WARNING: Missing block: B:266:0x054f, code:
            r23.mScreenOffBecauseOfProximity = true;
            sendOnProximityPositiveWithWakelock();
     */
    /* JADX WARNING: Missing block: B:268:0x055b, code:
            if (r23.mScreenOffBecauseOfProximity == false) goto L_0x018e;
     */
    /* JADX WARNING: Missing block: B:270:0x0562, code:
            if (r23.mProximity == 1) goto L_0x018e;
     */
    /* JADX WARNING: Missing block: B:271:0x0564, code:
            r23.mScreenOffBecauseOfProximity = false;
            sendOnProximityNegativeWithWakelock();
     */
    /* JADX WARNING: Missing block: B:273:0x0572, code:
            if (r23.mWaitingForNegativeProximity == false) goto L_0x058d;
     */
    /* JADX WARNING: Missing block: B:275:0x0578, code:
            if (r23.mScreenOffBecauseOfProximity == false) goto L_0x058d;
     */
    /* JADX WARNING: Missing block: B:277:0x057f, code:
            if (r23.mProximity != 1) goto L_0x058d;
     */
    /* JADX WARNING: Missing block: B:279:0x0584, code:
            if (r22 == 1) goto L_0x058d;
     */
    /* JADX WARNING: Missing block: B:280:0x0586, code:
            setProximitySensorEnabled(true);
     */
    /* JADX WARNING: Missing block: B:281:0x058d, code:
            setProximitySensorEnabled(false);
            r23.mWaitingForNegativeProximity = false;
     */
    /* JADX WARNING: Missing block: B:282:0x0599, code:
            r23.mWaitingForNegativeProximity = false;
     */
    /* JADX WARNING: Missing block: B:283:0x05a0, code:
            r12 = false;
     */
    /* JADX WARNING: Missing block: B:284:0x05a3, code:
            r12 = false;
     */
    /* JADX WARNING: Missing block: B:285:0x05a6, code:
            r11 = false;
     */
    /* JADX WARNING: Missing block: B:286:0x05a9, code:
            r11 = false;
     */
    /* JADX WARNING: Missing block: B:287:0x05ac, code:
            r8 = false;
     */
    /* JADX WARNING: Missing block: B:288:0x05af, code:
            r5 = true;
     */
    /* JADX WARNING: Missing block: B:289:0x05b2, code:
            r7 = false;
     */
    /* JADX WARNING: Missing block: B:290:0x05b5, code:
            r21 = true;
     */
    /* JADX WARNING: Missing block: B:291:0x05b9, code:
            r23.mAppliedAutoBrightness = false;
     */
    /* JADX WARNING: Missing block: B:292:0x05c0, code:
            r23.mAppliedAutoBrightness = false;
     */
    /* JADX WARNING: Missing block: B:293:0x05c7, code:
            r13 = r13 / 2;
     */
    /* JADX WARNING: Missing block: B:295:0x05cf, code:
            if (r23.mAppliedDimming == false) goto L_0x02ee;
     */
    /* JADX WARNING: Missing block: B:296:0x05d1, code:
            r21 = false;
            r23.mAppliedDimming = false;
     */
    /* JADX WARNING: Missing block: B:298:0x05de, code:
            if (r23.mAppliedLowPower == false) goto L_0x0313;
     */
    /* JADX WARNING: Missing block: B:299:0x05e0, code:
            r21 = false;
            r23.mAppliedLowPower = false;
     */
    /* JADX WARNING: Missing block: B:300:0x05e9, code:
            r23.mAutomaticBrightnessController.mScreenAutoBrightness = r13;
     */
    /* JADX WARNING: Missing block: B:302:0x05f9, code:
            if (r23.mColorFadeOnAnimator.isStarted() != false) goto L_0x03d2;
     */
    /* JADX WARNING: Missing block: B:304:0x0603, code:
            if (r23.mColorFadeOffAnimator.isStarted() != false) goto L_0x03d2;
     */
    /* JADX WARNING: Missing block: B:305:0x0605, code:
            r20 = r23.mPowerState.waitUntilClean(r23.mCleanListener);
     */
    /* JADX WARNING: Missing block: B:306:0x0613, code:
            r14 = true;
     */
    /* JADX WARNING: Missing block: B:307:0x0616, code:
            r14 = false;
     */
    /* JADX WARNING: Missing block: B:309:0x061b, code:
            if (DEBUG == false) goto L_0x0626;
     */
    /* JADX WARNING: Missing block: B:310:0x061d, code:
            android.util.Slog.d(TAG, "Unfinished business...");
     */
    /* JADX WARNING: Missing block: B:311:0x0626, code:
            r23.mCallbacks.acquireSuspendBlocker();
            r23.mUnfinishedBusiness = true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void updatePowerState() {
        boolean mustInitialize = false;
        boolean autoBrightnessAdjustmentChanged = false;
        synchronized (this.mLock) {
            this.mPendingUpdatePowerStateLocked = false;
            if (this.mPendingRequestLocked == null) {
                return;
            }
            if (this.mPowerRequest == null) {
                this.mPowerRequest = new DisplayPowerRequest(this.mPendingRequestLocked);
                this.mWaitingForNegativeProximity = this.mPendingWaitForNegativeProximityLocked;
                this.mPendingWaitForNegativeProximityLocked = false;
                this.mPendingRequestChangedLocked = false;
                mustInitialize = true;
            } else if (this.mPendingRequestChangedLocked) {
                autoBrightnessAdjustmentChanged = this.mPowerRequest.screenAutoBrightnessAdjustment != this.mPendingRequestLocked.screenAutoBrightnessAdjustment;
                this.mPowerRequest.copyFrom(this.mPendingRequestLocked);
                this.mWaitingForNegativeProximity |= this.mPendingWaitForNegativeProximityLocked;
                this.mPendingWaitForNegativeProximityLocked = false;
                this.mPendingRequestChangedLocked = false;
                this.mDisplayReadyLocked = false;
            }
            boolean mustNotify = !this.mDisplayReadyLocked;
            if (DEBUG) {
                Slog.d(TAG, "updatePowerState: " + this.mPowerRequest + ", mWaitingForNegativeProximity=" + this.mWaitingForNegativeProximity + ", autoBrightnessAdjustmentChanged=" + autoBrightnessAdjustmentChanged + ", mustNotify=" + mustNotify);
            }
        }
    }

    public void updateBrightness() {
        sendUpdatePowerState();
    }

    public void blockScreenOnByFingerPrint() {
        if (DEBUG_PANIC) {
            Slog.d(TAG_FP, "blockScreenOnByFingerPrint, mPendingScreenOnUnblockerFromFingerPrint = " + this.mPendingScreenOnUnblockerFromFingerPrint);
        }
        if (this.mPendingScreenOnUnblockerFromFingerPrint == null) {
            Trace.asyncTraceBegin(524288, SCREEN_ON_BLOCKED_BY_FP_TRACE_NAME, 0);
            this.mPendingScreenOnUnblockerFromFingerPrint = new ScreenOnUnblockerByFingerPrint(this, null);
        }
        this.mPendingDisplayReadyBlocker = true;
    }

    public void unblockScreenOnByFingerPrint(boolean type) {
        if (DEBUG_PANIC) {
            Slog.d(TAG_FP, "unblockScreen(type = " + type + ") , mPendingScreenOnUnblockerFromFingerPrint = " + this.mPendingScreenOnUnblockerFromFingerPrint);
        }
        if (this.mPendingScreenOnUnblockerFromFingerPrint != null) {
            this.mPendingScreenOnUnblockerFromFingerPrint = null;
            Trace.asyncTraceEnd(524288, SCREEN_ON_BLOCKED_BY_FP_TRACE_NAME, 0);
        }
        this.mPendingDisplayReadyBlocker = false;
        if (type) {
            sendUpdatePowerState();
        }
    }

    private void unblockDisplayReady() {
        if (this.mPowerManagerInternal != null) {
            if (this.mPowerManagerInternal.isStartGoToSleep()) {
                this.mPendingDisplayReadyBlocker = false;
            }
            if (DEBUG_PANIC) {
                Slog.d(TAG_FP, "unblockDisplayReady, mPendingDisplayReadyBlocker = " + this.mPendingDisplayReadyBlocker);
            }
        }
    }

    public boolean isBlockScreenOnByFingerPrint() {
        return this.mPendingScreenOnUnblockerFromFingerPrint != null;
    }

    public boolean isBlockDisplayByFingerPrint() {
        return this.mPendingDisplayReadyBlocker;
    }

    public int getScreenState() {
        if (DEBUG_PANIC) {
            boolean z;
            String str = TAG_FP;
            StringBuilder append = new StringBuilder().append("ScreenState = ").append(this.mPowerState.getScreenState()).append(", FingerPrint block = ");
            if (this.mPendingScreenOnUnblockerFromFingerPrint != null) {
                z = true;
            } else {
                z = false;
            }
            append = append.append(z).append(", keyguard block = ");
            if (this.mPendingScreenOnUnblocker != null) {
                z = true;
            } else {
                z = false;
            }
            Slog.d(str, append.append(z).toString());
        }
        return (this.mPowerState.getScreenState() == 2 && this.mPendingScreenOnUnblockerFromFingerPrint == null && this.mPendingScreenOnUnblocker == null) ? 1 : 0;
    }

    public void updateScreenOnBlockedState(boolean isBlockedScreenOn) {
        this.mScreenOnBlockedByFace = isBlockedScreenOn;
        if (DEBUG_PANIC) {
            Slog.d(TAG, "updateScreenOnBlockedState, isBlockedScreenOn = " + isBlockedScreenOn);
        }
        if (!isBlockedScreenOn && this.mHandler.hasMessages(3)) {
            this.mHandler.removeMessages(3);
            Message msg = this.mHandler.obtainMessage(3, this.mPendingScreenOnUnblocker);
            msg.setAsynchronous(true);
            this.mHandler.sendMessage(msg);
            Slog.d(TAG, "MSG_SCREEN_ON_UNBLOCKED sended");
        }
    }

    private void blockScreenOn() {
        this.mHandler.removeMessages(3);
        if (this.mPendingScreenOnUnblocker == null) {
            Trace.asyncTraceBegin(524288, SCREEN_ON_BLOCKED_TRACE_NAME, 0);
            this.mPendingScreenOnUnblocker = new ScreenOnUnblocker(this, null);
            this.mScreenOnBlockStartRealTime = SystemClock.elapsedRealtime();
            Slog.i(TAG, "Blocking screen on until initial contents have been drawn.");
        }
    }

    private void unblockScreenOn() {
        this.mHandler.removeMessages(3);
        if (this.mPendingScreenOnUnblocker != null) {
            this.mPendingScreenOnUnblocker = null;
            Slog.i(TAG, "Unblocked screen on after " + (SystemClock.elapsedRealtime() - this.mScreenOnBlockStartRealTime) + " ms");
            Trace.asyncTraceEnd(524288, SCREEN_ON_BLOCKED_TRACE_NAME, 0);
        }
    }

    private boolean setScreenState(int state) {
        OppoBrightUtils oppoBrightUtils;
        String str;
        StringBuilder append;
        OppoBrightUtils oppoBrightUtils2;
        boolean isOff;
        if (state == 2) {
            oppoBrightUtils = mOppoBrightUtils;
            if (OppoBrightUtils.mFirstSetScreenState) {
                if (this.mPowerRequest.useAutoBrightness) {
                    oppoBrightUtils = mOppoBrightUtils;
                    OppoBrightUtils.mBrightnessBoost = 1;
                    this.mHandler.removeMessages(5);
                    this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(5), 4000);
                    oppoBrightUtils = mOppoBrightUtils;
                    OppoBrightUtils.mDisplayStateOn = true;
                    oppoBrightUtils = mOppoBrightUtils;
                    OppoBrightUtils.mFirstSetScreenState = false;
                    oppoBrightUtils = mOppoBrightUtils;
                    OppoBrightUtils.mManualBrightnessBackup = (int) System.getFloatForUser(this.mContext.getContentResolver(), "screen_auto_brightness_adj", OppoBrightUtils.MIN_LUX_LIMITI, -2);
                    oppoBrightUtils = mOppoBrightUtils;
                    OppoBrightUtils.mManualAmbientLuxBackup = System.getFloatForUser(this.mContext.getContentResolver(), "autobrightness_manul_ambient", OppoBrightUtils.MIN_LUX_LIMITI, -2);
                    if (DEBUG) {
                        str = TAG;
                        append = new StringBuilder().append(" mManulAtAmbientLux = ");
                        oppoBrightUtils2 = mOppoBrightUtils;
                        Slog.d(str, append.append(OppoBrightUtils.mManulAtAmbientLux).toString());
                    }
                } else {
                    oppoBrightUtils = mOppoBrightUtils;
                    OppoBrightUtils.mFirstSetScreenState = false;
                }
                if (OppoBrightUtils.mOutdoorBrightnessSupport) {
                    setLightSensorAlwaysOn(true);
                }
            }
        }
        if (this.mPowerState.getScreenState() != state) {
            if (state == 1) {
                oppoBrightUtils = mOppoBrightUtils;
                if (!OppoBrightUtils.mSaveBrightnessByShutdown) {
                    oppoBrightUtils = mOppoBrightUtils;
                    oppoBrightUtils = mOppoBrightUtils;
                    OppoBrightUtils.mManualBrightnessBackup = OppoBrightUtils.mManualBrightness;
                    oppoBrightUtils = mOppoBrightUtils;
                    oppoBrightUtils = mOppoBrightUtils;
                    OppoBrightUtils.mManualAmbientLuxBackup = OppoBrightUtils.mManulAtAmbientLux;
                    if (DEBUG) {
                        str = TAG;
                        append = new StringBuilder().append("Display.STATE_OFF mManualBrightness = ");
                        oppoBrightUtils2 = mOppoBrightUtils;
                        append = append.append(OppoBrightUtils.mManualBrightness).append(" mManulAtAmbientLux = ");
                        oppoBrightUtils2 = mOppoBrightUtils;
                        Slog.d(str, append.append(OppoBrightUtils.mManulAtAmbientLux).toString());
                    }
                    oppoBrightUtils2 = mOppoBrightUtils;
                    System.putFloatForUser(this.mContext.getContentResolver(), "screen_auto_brightness_adj", (float) OppoBrightUtils.mManualBrightnessBackup, -2);
                    oppoBrightUtils2 = mOppoBrightUtils;
                    System.putFloatForUser(this.mContext.getContentResolver(), "autobrightness_manul_ambient", OppoBrightUtils.mManulAtAmbientLux, -2);
                    oppoBrightUtils = mOppoBrightUtils;
                    OppoBrightUtils.mManualBrightness = 0;
                    oppoBrightUtils = mOppoBrightUtils;
                    OppoBrightUtils.mManulAtAmbientLux = OppoBrightUtils.MIN_LUX_LIMITI;
                }
                oppoBrightUtils = mOppoBrightUtils;
                OppoBrightUtils.mSaveBrightnessByShutdown = false;
                if (AutomaticBrightnessController.mProximityNear && !OppoBrightUtils.DEBUG_PRETEND_PROX_SENSOR_ABSENT) {
                    this.mScreenBrightnessRampAnimator.updateCurrentToTarget();
                }
                if (OppoBrightUtils.mHighBrightnessModeSupport || OppoBrightUtils.mOutdoorBrightnessSupport) {
                    setLightSensorAlwaysOn(false);
                }
                this.mAutomaticBrightnessController.resetLightParamsScreenOff();
            } else if (state == 2) {
                if (this.mPowerRequest.useAutoBrightness) {
                    oppoBrightUtils = mOppoBrightUtils;
                    OppoBrightUtils.mBrightnessBoost = 1;
                    this.mHandler.removeMessages(5);
                    this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(5), 4000);
                }
                oppoBrightUtils = mOppoBrightUtils;
                OppoBrightUtils.mDisplayStateOn = true;
                oppoBrightUtils = mOppoBrightUtils;
                OppoBrightUtils.mManualBrightnessBackup = (int) System.getFloatForUser(this.mContext.getContentResolver(), "screen_auto_brightness_adj", OppoBrightUtils.MIN_LUX_LIMITI, -2);
                oppoBrightUtils = mOppoBrightUtils;
                if (OppoBrightUtils.mManualBrightnessBackup != 0) {
                    oppoBrightUtils = mOppoBrightUtils;
                    oppoBrightUtils = mOppoBrightUtils;
                    OppoBrightUtils.mManualBrightness = OppoBrightUtils.mManualBrightnessBackup;
                }
                if (DEBUG) {
                    str = TAG;
                    append = new StringBuilder().append("Display.STATE_ON mManualBrightness ");
                    oppoBrightUtils2 = mOppoBrightUtils;
                    Slog.d(str, append.append(OppoBrightUtils.mManualBrightness).toString());
                }
                if (OppoBrightUtils.mOutdoorBrightnessSupport) {
                    setLightSensorAlwaysOn(true);
                }
            }
        }
        if (this.mPowerState.getScreenState() != state) {
            if (this.mPowerState.getScreenState() != 1) {
            }
            this.mPowerState.setScreenState(state);
            try {
                this.mBatteryStats.noteScreenState(state);
            } catch (RemoteException e) {
            }
        }
        if (state == 1) {
            isOff = true;
        } else {
            isOff = false;
        }
        if (isOff && this.mReportedScreenStateToPolicy != 0 && !this.mScreenOffBecauseOfProximity) {
            this.mReportedScreenStateToPolicy = 0;
            unblockScreenOn();
            unblockDisplayReady();
            this.mWindowManagerPolicy.screenTurnedOff();
        } else if (!isOff && this.mReportedScreenStateToPolicy == 0) {
            this.mReportedScreenStateToPolicy = 1;
            if (this.mPowerState.getColorFadeLevel() == OppoBrightUtils.MIN_LUX_LIMITI) {
                blockScreenOn();
            } else {
                unblockScreenOn();
            }
            this.mWindowManagerPolicy.screenTurningOn(this.mPendingScreenOnUnblocker);
        }
        if (this.mPendingScreenOnUnblocker == null && this.mPendingScreenOnUnblockerFromFingerPrint == null) {
            return true;
        }
        return false;
    }

    private int clampScreenBrightness(int value) {
        return MathUtils.constrain(value, this.mScreenBrightnessRangeMinimum, this.mScreenBrightnessRangeMaximum);
    }

    private void animateScreenBrightness(int target, int rate) {
        if (DEBUG) {
            Slog.d(TAG, "Animating brightness: target=" + target + ", rate=" + rate);
        }
        OppoBrightUtils oppoBrightUtils = mOppoBrightUtils;
        if (OppoBrightUtils.mSetBrihgtnessSlide) {
            System.putIntForBrightness(this.mContext.getContentResolver(), "screen_brightness", target, -2);
            oppoBrightUtils = mOppoBrightUtils;
            OppoBrightUtils.mSetBrihgtnessSlide = false;
        }
        if (this.mScreenBrightnessRampAnimator.animateTo(target, rate)) {
            try {
                this.mBatteryStats.noteScreenBrightness(target);
            } catch (RemoteException e) {
            }
        }
    }

    private void animateScreenStateChange(int target, boolean performScreenOffTransition) {
        int i = 2;
        if (!this.mColorFadeOnAnimator.isStarted() && !this.mColorFadeOffAnimator.isStarted()) {
            if (this.mPendingScreenOff && target != 1) {
                setScreenState(1);
                this.mPendingScreenOff = false;
                this.mPowerState.dismissColorFadeResources();
            }
            if (target == 2) {
                if (setScreenState(2)) {
                    this.mPowerState.setColorFadeLevel(1.0f);
                    this.mPowerState.dismissColorFade();
                }
            } else if (target == 3) {
                if (!(this.mScreenBrightnessRampAnimator.isAnimating() && this.mPowerState.getScreenState() == 2) && setScreenState(3)) {
                    this.mPowerState.setColorFadeLevel(1.0f);
                    this.mPowerState.dismissColorFade();
                }
            } else if (target != 4) {
                this.mPendingScreenOff = true;
                if (this.mPowerState.getColorFadeLevel() == OppoBrightUtils.MIN_LUX_LIMITI) {
                    setScreenState(1);
                    this.mPendingScreenOff = false;
                    this.mPowerState.dismissColorFadeResources();
                } else {
                    if (performScreenOffTransition) {
                        DisplayPowerState displayPowerState = this.mPowerState;
                        Context context = this.mContext;
                        if (!this.mColorFadeFadesConfig) {
                            i = 1;
                        }
                        if (displayPowerState.prepareColorFade(context, i) && this.mPowerState.getScreenState() != 1) {
                            this.mColorFadeOffAnimator.start();
                        }
                    }
                    this.mColorFadeOffAnimator.end();
                }
            } else if (!this.mScreenBrightnessRampAnimator.isAnimating() || this.mPowerState.getScreenState() == 4) {
                if (this.mPowerState.getScreenState() != 4) {
                    if (setScreenState(3)) {
                        setScreenState(4);
                    } else {
                        return;
                    }
                }
                this.mPowerState.setColorFadeLevel(1.0f);
                this.mPowerState.dismissColorFade();
            }
        }
    }

    private void setProximitySensorEnabled(boolean enable) {
        if (enable) {
            if (!this.mProximitySensorEnabled) {
                if (DEBUG) {
                    Slog.d(TAG, "setProximitySensorEnabled : True");
                }
                this.mProximitySensorEnabled = true;
                this.mSensorManager.registerListener(this.mProximitySensorListener, this.mProximitySensor, 3, this.mHandler);
            }
        } else if (this.mProximitySensorEnabled) {
            if (DEBUG) {
                Slog.d(TAG, "setProximitySensorEnabled : False");
            }
            this.mProximitySensorEnabled = false;
            this.useProximityForceSuspend = false;
            this.mProximity = -1;
            this.mPendingProximity = -1;
            this.mHandler.removeMessages(2);
            this.mSensorManager.unregisterListener(this.mProximitySensorListener);
            clearPendingProximityDebounceTime();
        }
    }

    private void handleProximitySensorEvent(long time, boolean positive) {
        if (this.mProximitySensorEnabled && (this.mPendingProximity != 0 || positive)) {
            if (this.mPendingProximity != 1 || !positive) {
                this.mHandler.removeMessages(2);
                if (positive) {
                    this.mPendingProximity = 1;
                    setPendingProximityDebounceTime(time + 0);
                } else {
                    this.mPendingProximity = 0;
                    setPendingProximityDebounceTime(time + 0);
                }
                debounceProximitySensor();
            }
        }
    }

    private void debounceProximitySensor() {
        if (this.mProximitySensorEnabled && this.mPendingProximity != -1 && this.mPendingProximityDebounceTime >= 0) {
            if (this.mPendingProximityDebounceTime <= SystemClock.uptimeMillis()) {
                this.mProximity = this.mPendingProximity;
                this.mProximityEventHandled = false;
                this.mScreenOnCpuBoostHelper.acquireCpuBoost(500);
                updatePowerState();
                clearPendingProximityDebounceTime();
                return;
            }
            Message msg = this.mHandler.obtainMessage(2);
            msg.setAsynchronous(true);
            this.mHandler.sendMessageAtTime(msg, this.mPendingProximityDebounceTime);
        }
    }

    private void clearPendingProximityDebounceTime() {
        if (this.mPendingProximityDebounceTime >= 0) {
            this.mPendingProximityDebounceTime = -1;
            this.mCallbacks.releaseSuspendBlocker();
        }
    }

    private void setPendingProximityDebounceTime(long debounceTime) {
        if (this.mPendingProximityDebounceTime < 0) {
            this.mCallbacks.acquireSuspendBlocker();
        }
        this.mPendingProximityDebounceTime = debounceTime;
    }

    private void sendOnStateChangedWithWakelock() {
        this.mCallbacks.acquireSuspendBlocker();
        this.mHandler.post(this.mOnStateChangedRunnable);
    }

    private void sendOnProximityPositiveSuspendWithWakelock() {
        this.mCallbacks.acquireSuspendBlocker();
        this.mHandler.post(this.mOnProximityPositiveSuspendRunnable);
    }

    private void sendOnProximityNegativeSuspendWithWakelock() {
        this.mCallbacks.acquireSuspendBlocker();
        this.mHandler.post(this.mOnProximityNegativeSuspendRunnable);
    }

    private void sendOnProximityPositiveWithWakelock() {
        this.mCallbacks.acquireSuspendBlocker();
        this.mHandler.post(this.mOnProximityPositiveRunnable);
    }

    private void sendOnProximityNegativeWithWakelock() {
        this.mCallbacks.acquireSuspendBlocker();
        this.mHandler.post(this.mOnProximityNegativeRunnable);
    }

    public void dump(final PrintWriter pw) {
        synchronized (this.mLock) {
            pw.println();
            pw.println("Display Power Controller Locked State:");
            pw.println("  mDisplayReadyLocked=" + this.mDisplayReadyLocked);
            pw.println("  mPendingRequestLocked=" + this.mPendingRequestLocked);
            pw.println("  mPendingRequestChangedLocked=" + this.mPendingRequestChangedLocked);
            pw.println("  mPendingWaitForNegativeProximityLocked=" + this.mPendingWaitForNegativeProximityLocked);
            pw.println("  mPendingUpdatePowerStateLocked=" + this.mPendingUpdatePowerStateLocked);
        }
        pw.println();
        pw.println("Display Power Controller Configuration:");
        pw.println("  mScreenBrightnessDozeConfig=" + this.mScreenBrightnessDozeConfig);
        pw.println("  mScreenBrightnessDimConfig=" + this.mScreenBrightnessDimConfig);
        pw.println("  mScreenBrightnessDarkConfig=" + this.mScreenBrightnessDarkConfig);
        pw.println("  mScreenBrightnessRangeMinimum=" + this.mScreenBrightnessRangeMinimum);
        pw.println("  mScreenBrightnessRangeMaximum=" + this.mScreenBrightnessRangeMaximum);
        pw.println("  mUseSoftwareAutoBrightnessConfig=" + this.mUseSoftwareAutoBrightnessConfig);
        pw.println("  mAllowAutoBrightnessWhileDozingConfig=" + this.mAllowAutoBrightnessWhileDozingConfig);
        pw.println("  mColorFadeFadesConfig=" + this.mColorFadeFadesConfig);
        this.mHandler.runWithScissors(new Runnable() {
            public void run() {
                DisplayPowerController.this.dumpLocal(pw);
            }
        }, 1000);
    }

    private void dumpLocal(PrintWriter pw) {
        pw.println();
        pw.println("Display Power Controller Thread State:");
        pw.println("  mPowerRequest=" + this.mPowerRequest);
        pw.println("  mWaitingForNegativeProximity=" + this.mWaitingForNegativeProximity);
        pw.println("  mProximitySensor=" + this.mProximitySensor);
        pw.println("  mProximitySensorEnabled=" + this.mProximitySensorEnabled);
        pw.println("  mProximityThreshold=" + this.mProximityThreshold);
        pw.println("  mProximity=" + proximityToString(this.mProximity));
        pw.println("  mPendingProximity=" + proximityToString(this.mPendingProximity));
        pw.println("  mPendingProximityDebounceTime=" + TimeUtils.formatUptime(this.mPendingProximityDebounceTime));
        pw.println("  mScreenOffBecauseOfProximity=" + this.mScreenOffBecauseOfProximity);
        pw.println("  mAppliedAutoBrightness=" + this.mAppliedAutoBrightness);
        pw.println("  mAppliedDimming=" + this.mAppliedDimming);
        pw.println("  mAppliedLowPower=" + this.mAppliedLowPower);
        pw.println("  mPendingScreenOnUnblocker=" + this.mPendingScreenOnUnblocker);
        pw.println("  mPendingScreenOff=" + this.mPendingScreenOff);
        pw.println("  mReportedToPolicy=" + reportedToPolicyToString(this.mReportedScreenStateToPolicy));
        if (this.mScreenBrightnessRampAnimator != null) {
            pw.println("  mScreenBrightnessRampAnimator.isAnimating()=" + this.mScreenBrightnessRampAnimator.isAnimating());
        }
        if (this.mColorFadeOnAnimator != null) {
            pw.println("  mColorFadeOnAnimator.isStarted()=" + this.mColorFadeOnAnimator.isStarted());
        }
        if (this.mColorFadeOffAnimator != null) {
            pw.println("  mColorFadeOffAnimator.isStarted()=" + this.mColorFadeOffAnimator.isStarted());
        }
        if (this.mPowerState != null) {
            this.mPowerState.dump(pw);
        }
        if (this.mAutomaticBrightnessController != null) {
            this.mAutomaticBrightnessController.dump(pw);
        }
    }

    private static String proximityToString(int state) {
        switch (state) {
            case -1:
                return "Unknown";
            case 0:
                return "Negative";
            case 1:
                return "Positive";
            default:
                return Integer.toString(state);
        }
    }

    private static String reportedToPolicyToString(int state) {
        switch (state) {
            case 0:
                return "REPORTED_TO_POLICY_SCREEN_OFF";
            case 1:
                return "REPORTED_TO_POLICY_SCREEN_TURNING_ON";
            case 2:
                return "REPORTED_TO_POLICY_SCREEN_ON";
            default:
                return Integer.toString(state);
        }
    }

    private static Spline createAutoBrightnessSpline(int[] lux, int[] brightness, boolean virtualValues) {
        if (lux == null || lux.length == 0 || brightness == null || brightness.length == 0) {
            Slog.e(TAG, "Could not create auto-brightness spline.");
            return null;
        }
        try {
            int n = brightness.length;
            float[] x = new float[n];
            float[] y = new float[n];
            y[0] = normalizeAbsoluteBrightness(brightness[0], virtualValues);
            for (int i = 1; i < n; i++) {
                x[i] = (float) lux[i - 1];
                y[i] = normalizeAbsoluteBrightness(brightness[i], virtualValues);
            }
            Spline spline = Spline.createSpline(x, y);
            if (DEBUG) {
                Slog.d(TAG, "Auto-brightness spline: " + spline);
                for (float v = 1.0f; v < ((float) lux[lux.length - 1]) * 1.25f; v *= 1.25f) {
                    String str = TAG;
                    Object[] objArr = new Object[2];
                    objArr[0] = Float.valueOf(v);
                    objArr[1] = Float.valueOf(spline.interpolate(v));
                    Slog.d(str, String.format("  %7.1f: %7.1f", objArr));
                }
            }
            return spline;
        } catch (IllegalArgumentException ex) {
            Slog.e(TAG, "Could not create auto-brightness spline.", ex);
            return null;
        }
    }

    private static float normalizeAbsoluteBrightness(int value, boolean virtualValue) {
        return ((float) clampAbsoluteBrightness(value, virtualValue)) / ((float) PowerManager.BRIGHTNESS_MULTIBITS_ON);
    }

    private static int clampAbsoluteBrightness(int value, boolean virtualValue) {
        if (!virtualValue) {
            value = PowerManager.dimmingPhysicalToVirtual(value);
        }
        return MathUtils.constrain(value, 0, PowerManager.BRIGHTNESS_MULTIBITS_ON);
    }

    private void writeInitConfig() {
        synchronized (this.mLock) {
            if (this.mTuningInitCurve != null) {
                this.mTuningAli2BliSerial = nativeSetTuningIntArray(1, this.mTuningInitCurve);
                this.mTuningInitCurve = null;
            }
            if (this.mTuningBliBrightenSerial == -1) {
                this.mTuningBliBrightenSerial = nativeSetTuningInt(2, this.BRIGHTNESS_RAMP_RATE_BRIGHTEN);
            }
            if (this.mTuningBliDarkenSerial == -1) {
                this.mTuningBliDarkenSerial = nativeSetTuningInt(3, this.BRIGHTNESS_RAMP_RATE_DARKEN);
            }
        }
    }

    private void updateRuntimeConfig() {
        synchronized (this.mLock) {
            boolean updated = false;
            int serial = nativeGetTuningSerial(1);
            if (serial != this.mTuningAli2BliSerial) {
                int length = nativeGetTuningInt(0);
                if (length > 0) {
                    int[] curve = new int[(length * 2)];
                    nativeGetTuningIntArray(1, curve);
                    if (curve[0] != 0) {
                        Slog.w(TAG, "AALRuntimeTuning: lux[0] is not 0: " + curve[0]);
                    }
                    int[] lux = new int[(length - 1)];
                    int[] brightness = new int[length];
                    System.arraycopy(curve, 1, lux, 0, length - 1);
                    System.arraycopy(curve, length, brightness, 0, length);
                    Spline spline = createAutoBrightnessSpline(lux, brightness, false);
                    if (spline != null) {
                        this.mAutomaticBrightnessController.setScreenAutoBrightnessSpline(spline);
                        this.mTuningAli2BliSerial = serial;
                        updated = true;
                        Slog.d(TAG, "AALRuntimeTuning: curve updated. Length = " + length + ", serial = " + serial);
                        Slog.d(TAG, "AALRuntimeTuning: lux = " + curve[0] + "-" + lux[lux.length - 1] + " brightness = " + brightness[0] + "-" + brightness[brightness.length - 1]);
                    } else {
                        Slog.e(TAG, "AALRuntimeTuning: invalid curve is given.");
                        StringBuffer buffer = new StringBuffer();
                        buffer.append("AALRuntimeTuning: curve = ");
                        for (int i = 0; i < length; i++) {
                            buffer.append(curve[i]);
                            buffer.append(":");
                            buffer.append(curve[length + i]);
                            buffer.append(" ");
                        }
                        Slog.e(TAG, buffer.toString());
                    }
                } else {
                    Slog.e(TAG, "AALRuntimeTuning: invalid curve length: " + length);
                }
            }
            if (updated) {
                this.mAutomaticBrightnessController.updateRuntimeConfig();
                this.mTuningQuicklyApply = true;
            }
            serial = nativeGetTuningSerial(2);
            if (serial != this.mTuningBliBrightenSerial) {
                this.BRIGHTNESS_RAMP_RATE_BRIGHTEN = nativeGetTuningInt(2);
                if (this.BRIGHTNESS_RAMP_RATE_BRIGHTEN <= 0) {
                    this.BRIGHTNESS_RAMP_RATE_BRIGHTEN = BRIGHTNESS_RAMP_RATE_SLOW;
                }
                this.mTuningBliBrightenSerial = serial;
                Slog.d(TAG, "AALRuntimeTuning: brighten = " + this.BRIGHTNESS_RAMP_RATE_BRIGHTEN + ", serial = " + serial);
            }
            serial = nativeGetTuningSerial(3);
            if (serial != this.mTuningBliDarkenSerial) {
                this.BRIGHTNESS_RAMP_RATE_DARKEN = nativeGetTuningInt(3);
                if (this.BRIGHTNESS_RAMP_RATE_DARKEN <= 0) {
                    this.BRIGHTNESS_RAMP_RATE_DARKEN = BRIGHTNESS_RAMP_RATE_SLOW;
                }
                this.mTuningBliDarkenSerial = serial;
                Slog.d(TAG, "AALRuntimeTuning: darken = " + this.BRIGHTNESS_RAMP_RATE_DARKEN + ", serial = " + serial);
            }
        }
    }

    private boolean isScreenStateBright() {
        return this.mPowerRequest.policy == 3;
    }

    private int protectedMinimumBrightness() {
        return this.mAutomaticBrightnessController.getAutomaticScreenBrightness() < 80 ? 1 : 40;
    }

    private int getBrightnessSetting() {
        return System.getIntForUser(this.mContext.getContentResolver(), "screen_brightness", this.mPowerRequest.screenBrightness, -2);
    }

    private void handleProtectLowDimming() {
        if (!this.mAppliedAutoBrightness) {
            int minBrightness = protectedMinimumBrightness();
            if (isScreenStateBright() && !this.mPowerRequest.useAutoBrightness && getBrightnessSetting() == this.mPowerRequest.screenBrightness && this.mPowerRequest.screenBrightness < 40 && this.mPowerRequest.screenBrightness < minBrightness) {
                Slog.d(TAG, "Low dimming protection : " + this.mPowerRequest.screenBrightness + " -> " + minBrightness + ", auto = " + this.mAutomaticBrightnessController.getAutomaticScreenBrightness());
                System.putIntForUser(this.mContext.getContentResolver(), "screen_brightness", minBrightness, -2);
                Intent intent = new Intent(MTK_AAL_LOW_DIMMING_PROTECTION_TRIGGERED);
                intent.addFlags(67108864);
                this.mContext.sendBroadcast(intent);
            }
        }
        this.mLowDimmingProtectionTriggerBrightness = 0;
    }

    private void setLightSensorAlwaysOn(boolean enable) {
        if (enable) {
            if (!this.mLightSensorAlwaysOn) {
                this.mLightSensorAlwaysOn = true;
                Thread thread = new Thread(new Runnable() {
                    public void run() {
                        DisplayPowerController.this.mSensorManager.registerListener(DisplayPowerController.this.mLightSensorAlwaysOnListener, DisplayPowerController.this.mLightSensor, DisplayPowerController.ALWAYSON_SENSOR_RATE_US, DisplayPowerController.this.mHandler);
                    }
                }, "LightSensorAlwaysOnThread");
                thread.setPriority(10);
                thread.start();
            }
        } else if (this.mLightSensorAlwaysOn) {
            this.mLightSensorAlwaysOn = false;
            this.mSensorManager.unregisterListener(this.mLightSensorAlwaysOnListener);
            mHighBrightnessMode = 0;
            mPreHighBrightnessMode = 0;
            mLux = OppoBrightUtils.MIN_LUX_LIMITI;
            stopTimer();
            mOppoBrightUtils.setHighBrightness(mHighBrightnessMode);
        }
    }

    public void setOutdoorMode(boolean enable) {
        if (!OppoBrightUtils.mHighBrightnessModeSupport) {
            return;
        }
        if (enable) {
            setLightSensorAlwaysOn(true);
        } else {
            setLightSensorAlwaysOn(false);
        }
    }

    private void startTimer() {
        if (!mStartTimer) {
            synchronized (this) {
                mStartTimer = true;
                if (this.mTimer == null) {
                    this.mTimer = new Timer();
                }
                if (this.mTask == null) {
                    this.mTask = new TimerTask() {
                        public void run() {
                            Message msg = new Message();
                            msg.what = 1;
                            DisplayPowerController.this.mTimehandler.sendMessage(msg);
                        }
                    };
                }
                if (!(this.mTimer == null || this.mTask == null)) {
                    if (OppoBrightUtils.mHighBrightnessModeSupport) {
                        this.mTimer.schedule(this.mTask, 5000, 5000);
                    } else {
                        this.mTimer.schedule(this.mTask, LCD_HIGH_BRIGHTNESS_STATE_DELAY, LCD_HIGH_BRIGHTNESS_STATE_DELAY);
                    }
                }
            }
        }
    }

    private void stopTimer() {
        if (mStartTimer) {
            synchronized (this) {
                try {
                    mStartTimer = false;
                    if (this.mTimer != null) {
                        this.mTimer.cancel();
                        this.mTimer = null;
                    }
                    if (this.mTask != null) {
                        this.mTask.cancel();
                        this.mTask = null;
                    }
                } catch (NullPointerException e) {
                    Slog.i(TAG, "stopTimer null pointer", e);
                }
            }
            return;
        }
        return;
    }
}
