package com.android.server.display;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.app.ActivityManager;
import android.content.Context;
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
import android.os.SystemProperties;
import android.os.Trace;
import android.provider.Settings.System;
import android.util.MathUtils;
import android.util.Slog;
import android.util.Spline;
import android.util.TimeUtils;
import android.view.Display;
import android.view.WindowManagerPolicy;
import android.view.WindowManagerPolicy.ScreenOffListener;
import android.view.WindowManagerPolicy.ScreenOnListener;
import com.android.internal.app.IBatteryStats;
import com.android.server.LocalServices;
import com.android.server.am.BatteryStatsService;
import com.android.server.biometrics.BiometricsManagerInternal;
import com.android.server.display.RampAnimator.Listener;
import com.android.server.lights.LightsService;
import com.android.server.oppo.ScreenOnCpuBoostHelper;
import com.android.server.power.PowerManagerService;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

final class DisplayPowerController implements Callbacks {
    /* renamed from: -assertionsDisabled */
    static final /* synthetic */ boolean f32-assertionsDisabled = (DisplayPowerController.class.desiredAssertionStatus() ^ 1);
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
    private static final int MSG_PROXIMITY_SENSOR_DEBOUNCED = 2;
    private static final int MSG_SCREEN_OFF_UNBLOCKED = 4;
    private static final int MSG_SCREEN_ON_BRIGHTNESS_BOOST = 5;
    private static final int MSG_SCREEN_ON_UNBLOCKED = 3;
    private static final int MSG_SCREEN_ON_UNBLOCKED_BY_BIOMETRICS = 6;
    private static final int MSG_UPDATE_HIGH_BRIGHTNESS_STATE = 1;
    private static final int MSG_UPDATE_POWER_STATE = 1;
    private static final long OLED_HIGH_BRIGHTNESS_STATE_DELAY = 5000;
    private static final int PROXIMITY_NEGATIVE = 0;
    private static final int PROXIMITY_POSITIVE = 1;
    private static final int PROXIMITY_SENSOR_NEGATIVE_DEBOUNCE_DELAY = 0;
    private static final int PROXIMITY_SENSOR_POSITIVE_DEBOUNCE_DELAY = 0;
    private static final int PROXIMITY_UNKNOWN = -1;
    private static final int RAMP_STATE_SKIP_AUTOBRIGHT = 2;
    private static final int RAMP_STATE_SKIP_INITIAL = 1;
    private static final int RAMP_STATE_SKIP_NONE = 0;
    private static final int REPORTED_TO_POLICY_SCREEN_OFF = 0;
    private static final int REPORTED_TO_POLICY_SCREEN_ON = 2;
    private static final int REPORTED_TO_POLICY_SCREEN_TURNING_OFF = 3;
    private static final int REPORTED_TO_POLICY_SCREEN_TURNING_ON = 1;
    private static final int SCREEN_DIM_MINIMUM_REDUCTION = 10;
    private static final String SCREEN_OFF_BLOCKED_TRACE_NAME = "Screen off blocked";
    private static final String SCREEN_ON_BLOCKED_BY_BIOMETRICS_TRACE_NAME = "Screen on blocked by biometrics";
    private static final String SCREEN_ON_BLOCKED_TRACE_NAME = "Screen on blocked";
    private static final String TAG = "DisplayPowerController";
    private static final String TAG_BIOMETRICS = "Biometrics_DEBUG";
    private static final float TYPICAL_PROXIMITY_THRESHOLD = 5.0f;
    private static final boolean USE_COLOR_FADE_ON_ANIMATION = false;
    private static int mHighBrightnessMode = 0;
    private static float mLux = OppoBrightUtils.MIN_LUX_LIMITI;
    private static OppoBrightUtils mOppoBrightUtils;
    private static int mPreHighBrightnessMode = 0;
    public static boolean mQuickDarkToBright = false;
    public static boolean mScreenDimQuicklyDark = false;
    private static boolean mStartTimer = false;
    private final boolean mAllowAutoBrightnessWhileDozingConfig;
    private final AnimatorListener mAnimatorListener = new AnimatorListener() {
        public void onAnimationStart(Animator animation) {
        }

        public void onAnimationEnd(Animator animation) {
            DisplayPowerController.this.sendUpdatePowerState();
            if (DisplayPowerController.this.mBiometricsManager != null) {
                DisplayPowerController.this.mBiometricsManager.onGoToSleepFinish();
            }
        }

        public void onAnimationRepeat(Animator animation) {
        }

        public void onAnimationCancel(Animator animation) {
        }
    };
    private boolean mAppliedAutoBrightness;
    private boolean mAppliedDimming;
    private boolean mAppliedLowPower;
    private AutomaticBrightnessController mAutomaticBrightnessController;
    private final IBatteryStats mBatteryStats;
    private BiometricsManagerInternal mBiometricsManager;
    private final DisplayBlanker mBlanker;
    private final ArrayList<String> mBlockReasonList = new ArrayList();
    private boolean mBrightnessBucketsInDozeConfig;
    private final int mBrightnessRampRateSlow;
    private final DisplayPowerCallbacks mCallbacks;
    private final Runnable mCleanListener = new Runnable() {
        public void run() {
            DisplayPowerController.this.sendUpdatePowerState();
        }
    };
    private final boolean mColorFadeEnabled;
    private boolean mColorFadeFadesConfig;
    private ObjectAnimator mColorFadeOffAnimator;
    private ObjectAnimator mColorFadeOnAnimator;
    private final Context mContext;
    private boolean mDisplayBlanksAfterDozeConfig;
    private boolean mDisplayReadyLocked;
    private boolean mDozing;
    private final DisplayControllerHandler mHandler;
    private int mInitialAutoBrightness;
    private Sensor mLightSensor;
    private boolean mLightSensorAlwaysOn = false;
    private final SensorEventListener mLightSensorAlwaysOnListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
            if (DisplayPowerController.this.mLightSensorAlwaysOn && (DisplayPowerController.this.mScreenBrightnessRampAnimator.isAnimating() ^ 1) != 0) {
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
    private final Object mLock = new Object();
    private final Runnable mOnProximityNegativeRunnable = new Runnable() {
        public void run() {
            DisplayPowerController.this.mCallbacks.onProximityNegative();
            DisplayPowerController.this.mCallbacks.releaseSuspendBlocker();
        }
    };
    private final Runnable mOnProximityNegativeSuspendRunnable = new Runnable() {
        public void run() {
            DisplayPowerController.this.mCallbacks.onProximityNegativeForceSuspend();
            DisplayPowerController.this.mCallbacks.releaseSuspendBlocker();
        }
    };
    private final Runnable mOnProximityPositiveRunnable = new Runnable() {
        public void run() {
            DisplayPowerController.this.mCallbacks.onProximityPositive();
            DisplayPowerController.this.mCallbacks.releaseSuspendBlocker();
        }
    };
    private final Runnable mOnProximityPositiveSuspendRunnable = new Runnable() {
        public void run() {
            DisplayPowerController.this.mCallbacks.onProximityPositiveForceSuspend();
            DisplayPowerController.this.mCallbacks.releaseSuspendBlocker();
        }
    };
    private final Runnable mOnStateChangedRunnable = new Runnable() {
        public void run() {
            DisplayPowerController.this.mCallbacks.onStateChanged();
            DisplayPowerController.this.mCallbacks.releaseSuspendBlocker();
        }
    };
    private boolean mPendingDisplayReadyBlocker = false;
    private int mPendingProximity = -1;
    private long mPendingProximityDebounceTime = -1;
    private boolean mPendingRequestChangedLocked;
    private DisplayPowerRequest mPendingRequestLocked;
    private boolean mPendingScreenOff;
    private ScreenOffUnblocker mPendingScreenOffUnblocker;
    private ScreenOnUnblocker mPendingScreenOnUnblocker;
    private ScreenOnUnblockerByBiometrics mPendingScreenOnUnblockerFromBiometrics;
    private boolean mPendingUpdatePowerStateLocked;
    private boolean mPendingWaitForNegativeProximityLocked;
    private final PowerManagerInternal mPowerManagerInternal;
    private DisplayPowerRequest mPowerRequest;
    private DisplayPowerState mPowerState;
    private int mProximity = -1;
    private boolean mProximityEventHandled = true;
    private Sensor mProximitySensor;
    private boolean mProximitySensorEnabled;
    private final SensorEventListener mProximitySensorListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
            if (DisplayPowerController.this.mProximitySensorEnabled) {
                long time = SystemClock.uptimeMillis();
                float distance = event.values[0];
                boolean positive = distance >= OppoBrightUtils.MIN_LUX_LIMITI && distance < DisplayPowerController.this.mProximityThreshold;
                if (DisplayPowerController.DEBUG_PANIC) {
                    Slog.d(DisplayPowerController.TAG, "P-Sensor Changed: distance = " + distance + ", positive = " + positive);
                }
                DisplayPowerController.this.handleProximitySensorEvent(time, positive);
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    private float mProximityThreshold;
    private final Listener mRampAnimatorListener = new Listener() {
        public void onAnimationEnd() {
            DisplayPowerController.this.sendUpdatePowerState();
        }
    };
    private int mReportedScreenStateToPolicy;
    private int mScreenBrightnessDarkConfig;
    private int mScreenBrightnessDimConfig;
    private int mScreenBrightnessDozeConfig;
    private RampAnimator<DisplayPowerState> mScreenBrightnessRampAnimator;
    private final int mScreenBrightnessRangeMaximum;
    private final int mScreenBrightnessRangeMinimum;
    private boolean mScreenOffBecauseOfProximity;
    private long mScreenOffBlockStartRealTime;
    private long mScreenOnBlockStartRealTime;
    private boolean mScreenOnBlockedByFace = false;
    private ScreenOnCpuBoostHelper mScreenOnCpuBoostHelper;
    private int mScreenState;
    private final SensorManager mSensorManager;
    private int mSkipRampState = 0;
    private final boolean mSkipScreenOnBrightnessRamp;
    private TimerTask mTask;
    private Handler mTimehandler = new Handler() {
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
    private Timer mTimer;
    private boolean mUnfinishedBusiness;
    private boolean mUseSoftwareAutoBrightnessConfig;
    private boolean mWaitingForNegativeProximity;
    private final WindowManagerPolicy mWindowManagerPolicy;
    private boolean useProximityForceSuspend = false;

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
                    if (DisplayPowerController.this.mPendingScreenOffUnblocker == msg.obj) {
                        DisplayPowerController.this.unblockScreenOff();
                        DisplayPowerController.this.updatePowerState();
                        return;
                    }
                    return;
                case 5:
                    DisplayPowerController.mOppoBrightUtils;
                    OppoBrightUtils.mBrightnessBoost = 4;
                    return;
                case 6:
                    if (DisplayPowerController.this.mPendingScreenOnUnblockerFromBiometrics == msg.obj) {
                        DisplayPowerController.this.unblockScreenOnByBiometrics("MSG_SCREEN_ON_UNBLOCKED_BY_BIOMETRICS");
                        DisplayPowerController.this.updatePowerState();
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    private final class ScreenOffUnblocker implements ScreenOffListener {
        /* synthetic */ ScreenOffUnblocker(DisplayPowerController this$0, ScreenOffUnblocker -this1) {
            this();
        }

        private ScreenOffUnblocker() {
        }

        public void onScreenOff() {
            Message msg = DisplayPowerController.this.mHandler.obtainMessage(4, this);
            msg.setAsynchronous(true);
            DisplayPowerController.this.mHandler.sendMessage(msg);
        }
    }

    private final class ScreenOnUnblocker implements ScreenOnListener {
        /* synthetic */ ScreenOnUnblocker(DisplayPowerController this$0, ScreenOnUnblocker -this1) {
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

    private final class ScreenOnUnblockerByBiometrics implements ScreenOnListener {
        /* synthetic */ ScreenOnUnblockerByBiometrics(DisplayPowerController this$0, ScreenOnUnblockerByBiometrics -this1) {
            this();
        }

        private ScreenOnUnblockerByBiometrics() {
        }

        public void onScreenOn() {
            Message msg = DisplayPowerController.this.mHandler.obtainMessage(6, this);
            msg.setAsynchronous(true);
            DisplayPowerController.this.mHandler.sendMessage(msg);
        }
    }

    public DisplayPowerController(Context context, DisplayPowerCallbacks callbacks, Handler handler, SensorManager sensorManager, DisplayBlanker blanker) {
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
        int screenBrightnessSettingMinimum = clampAbsoluteBrightness(resources.getInteger(17694851));
        this.mScreenBrightnessDozeConfig = clampAbsoluteBrightness(resources.getInteger(17694845));
        this.mScreenBrightnessDimConfig = clampAbsoluteBrightness(resources.getInteger(17694844));
        this.mScreenBrightnessDarkConfig = clampAbsoluteBrightness(resources.getInteger(17694843));
        if (this.mScreenBrightnessDarkConfig > this.mScreenBrightnessDimConfig) {
            Slog.w(TAG, "Expected config_screenBrightnessDark (" + this.mScreenBrightnessDarkConfig + ") to be less than or equal to " + "config_screenBrightnessDim (" + this.mScreenBrightnessDimConfig + ").");
        }
        if (this.mScreenBrightnessDarkConfig > screenBrightnessSettingMinimum) {
            Slog.w(TAG, "Expected config_screenBrightnessDark (" + this.mScreenBrightnessDarkConfig + ") to be less than or equal to " + "config_screenBrightnessSettingMinimum (" + screenBrightnessSettingMinimum + ").");
        }
        int screenBrightnessRangeMinimum = Math.min(Math.min(screenBrightnessSettingMinimum, this.mScreenBrightnessDimConfig), this.mScreenBrightnessDarkConfig);
        screenBrightnessRangeMinimum = mOppoBrightUtils.getMinimumScreenBrightnessSetting();
        this.mScreenBrightnessRangeMaximum = PowerManager.BRIGHTNESS_MULTIBITS_ON;
        this.mUseSoftwareAutoBrightnessConfig = resources.getBoolean(17956894);
        this.mAllowAutoBrightnessWhileDozingConfig = resources.getBoolean(17956872);
        this.mBrightnessRampRateSlow = resources.getInteger(17694745);
        this.mSkipScreenOnBrightnessRamp = resources.getBoolean(17957016);
        int lightSensorRate = resources.getInteger(17694734);
        int initialLightSensorRate = resources.getInteger(17694733);
        if (initialLightSensorRate == -1) {
            initialLightSensorRate = lightSensorRate;
        } else if (initialLightSensorRate > lightSensorRate) {
            Slog.w(TAG, "Expected config_autoBrightnessInitialLightSensorRate (" + initialLightSensorRate + ") to be less than or equal to " + "config_autoBrightnessLightSensorRate (" + lightSensorRate + ").");
        }
        long brighteningLightDebounce = (long) resources.getInteger(17694731);
        long darkeningLightDebounce = (long) resources.getInteger(17694732);
        if (OppoBrightUtils.mBrightnessBitsConfig == 3) {
            darkeningLightDebounce = 1000;
        }
        boolean autoBrightnessResetAmbientLuxAfterWarmUp = resources.getBoolean(17956890);
        int ambientLightHorizon = resources.getInteger(17694730);
        float autoBrightnessAdjustmentMaxGamma = resources.getFraction(18022400, 1, 1);
        HysteresisLevels hysteresisLevels = new HysteresisLevels(resources.getIntArray(17236003), resources.getIntArray(17236004), resources.getIntArray(17236005));
        if (this.mUseSoftwareAutoBrightnessConfig) {
            int[] lux = mOppoBrightUtils.readAutoBrightnessLuxConfig();
            int[] screenBrightness = mOppoBrightUtils.readAutoBrightnessConfig();
            int lightSensorWarmUpTimeConfig = resources.getInteger(17694798);
            float dozeScaleFactor = resources.getFraction(18022403, 1, 1);
            Spline screenAutoBrightnessSpline = createAutoBrightnessSpline(lux, screenBrightness);
            if (screenAutoBrightnessSpline == null) {
                Slog.e(TAG, "Error in config.xml.  config_autoBrightnessLcdBacklightValues (size " + screenBrightness.length + ") " + "must be monotic and have exactly one more entry than " + "config_autoBrightnessLevels (size " + lux.length + ") " + "which must be strictly increasing.  " + "Auto-brightness will be disabled.");
                this.mUseSoftwareAutoBrightnessConfig = false;
            } else {
                int bottom = clampAbsoluteBrightness(screenBrightness[0]);
                if (this.mScreenBrightnessDarkConfig > bottom) {
                    Slog.w(TAG, "config_screenBrightnessDark (" + this.mScreenBrightnessDarkConfig + ") should be less than or equal to the first value of " + "config_autoBrightnessLcdBacklightValues (" + bottom + ").");
                }
                if (bottom < screenBrightnessRangeMinimum) {
                    screenBrightnessRangeMinimum = bottom;
                }
                this.mAutomaticBrightnessController = new AutomaticBrightnessController(this, handler.getLooper(), this.mContext, sensorManager, screenAutoBrightnessSpline, lightSensorWarmUpTimeConfig, screenBrightnessRangeMinimum, this.mScreenBrightnessRangeMaximum, dozeScaleFactor, lightSensorRate, initialLightSensorRate, brighteningLightDebounce, darkeningLightDebounce, autoBrightnessResetAmbientLuxAfterWarmUp, ambientLightHorizon, autoBrightnessAdjustmentMaxGamma, hysteresisLevels);
            }
            this.mScreenBrightnessDozeConfig = screenBrightness[0];
            this.mScreenBrightnessDimConfig = screenBrightness[0];
            this.mScreenBrightnessDarkConfig = screenBrightness[0];
            DEBUG_PANIC = SystemProperties.getBoolean("persist.sys.assert.panic", false);
        }
        this.mScreenBrightnessRangeMinimum = screenBrightnessRangeMinimum;
        this.mColorFadeEnabled = ActivityManager.isLowRamDeviceStatic() ^ 1;
        this.mColorFadeFadesConfig = resources.getBoolean(17956887);
        this.mDisplayBlanksAfterDozeConfig = resources.getBoolean(17956928);
        this.mBrightnessBucketsInDozeConfig = resources.getBoolean(17956929);
        this.mProximitySensor = this.mSensorManager.getDefaultSensor(8);
        if (this.mProximitySensor != null) {
            this.mProximityThreshold = Math.min(this.mProximitySensor.getMaximumRange(), 5.0f);
        }
        this.mBiometricsManager = (BiometricsManagerInternal) LocalServices.getService(BiometricsManagerInternal.class);
    }

    public boolean isProximitySensorAvailable() {
        return this.mProximitySensor != null;
    }

    public void setUseProximityForceSuspend(boolean enable) {
        if (!this.useProximityForceSuspend) {
            this.useProximityForceSuspend = enable;
        }
    }

    public boolean requestPowerState(DisplayPowerRequest request, boolean waitForNegativeProximity) {
        boolean z;
        if (DEBUG) {
            Slog.d(TAG, "requestPowerState: " + request + ", waitForNegativeProximity=" + waitForNegativeProximity);
        }
        synchronized (this.mLock) {
            boolean changed = false;
            if (waitForNegativeProximity) {
                if ((this.mPendingWaitForNegativeProximityLocked ^ 1) != 0) {
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
            if (changed && (this.mPendingRequestChangedLocked ^ 1) != 0) {
                this.mPendingRequestChangedLocked = true;
                sendUpdatePowerStateLocked();
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
        this.mPowerState = new DisplayPowerState(this.mBlanker, this.mColorFadeEnabled ? new ColorFade(0) : null);
        if (this.mColorFadeEnabled) {
            this.mColorFadeOnAnimator = ObjectAnimator.ofFloat(this.mPowerState, DisplayPowerState.COLOR_FADE_LEVEL, new float[]{OppoBrightUtils.MIN_LUX_LIMITI, 1.0f});
            this.mColorFadeOnAnimator.setDuration(250);
            this.mColorFadeOnAnimator.addListener(this.mAnimatorListener);
            this.mColorFadeOffAnimator = ObjectAnimator.ofFloat(this.mPowerState, DisplayPowerState.COLOR_FADE_LEVEL, new float[]{1.0f, OppoBrightUtils.MIN_LUX_LIMITI});
            this.mColorFadeOffAnimator.setDuration(100);
            this.mColorFadeOffAnimator.addListener(this.mAnimatorListener);
        }
        this.mScreenBrightnessRampAnimator = new RampAnimator(this.mPowerState, DisplayPowerState.SCREEN_BRIGHTNESS);
        this.mScreenBrightnessRampAnimator.setListener(this.mRampAnimatorListener);
        try {
            this.mBatteryStats.noteScreenState(this.mPowerState.getScreenState());
            this.mBatteryStats.noteScreenBrightness(this.mPowerState.getScreenBrightness());
        } catch (RemoteException e) {
        }
    }

    /* JADX WARNING: Missing block: B:15:0x005e, code:
            if (r11 == false) goto L_0x0063;
     */
    /* JADX WARNING: Missing block: B:16:0x0060, code:
            initialize();
     */
    /* JADX WARNING: Missing block: B:18:0x0075, code:
            if (r24.mPowerRequest.policy != 2) goto L_0x0089;
     */
    /* JADX WARNING: Missing block: B:20:0x0083, code:
            if (r24.mScreenState != 3) goto L_0x0089;
     */
    /* JADX WARNING: Missing block: B:21:0x0085, code:
            mQuickDarkToBright = true;
     */
    /* JADX WARNING: Missing block: B:22:0x0089, code:
            r24.mScreenState = r24.mPowerRequest.policy;
            r7 = -1;
            r14 = false;
     */
    /* JADX WARNING: Missing block: B:23:0x00a9, code:
            switch(r24.mPowerRequest.policy) {
                case 0: goto L_0x0125;
                case 1: goto L_0x0129;
                case 2: goto L_0x00ac;
                case 3: goto L_0x00ac;
                case 4: goto L_0x015a;
                default: goto L_0x00ac;
            };
     */
    /* JADX WARNING: Missing block: B:24:0x00ac, code:
            r18 = 2;
     */
    /* JADX WARNING: Missing block: B:26:0x00b0, code:
            if (-assertionsDisabled != false) goto L_0x015e;
     */
    /* JADX WARNING: Missing block: B:27:0x00b2, code:
            if (r18 != 0) goto L_0x015e;
     */
    /* JADX WARNING: Missing block: B:29:0x00b9, code:
            throw new java.lang.AssertionError();
     */
    /* JADX WARNING: Missing block: B:41:0x0125, code:
            r18 = 1;
            r14 = true;
     */
    /* JADX WARNING: Missing block: B:43:0x0135, code:
            if (r24.mPowerRequest.dozeScreenState == 0) goto L_0x0157;
     */
    /* JADX WARNING: Missing block: B:44:0x0137, code:
            r18 = r24.mPowerRequest.dozeScreenState;
     */
    /* JADX WARNING: Missing block: B:46:0x0149, code:
            if (r24.mAllowAutoBrightnessWhileDozingConfig != false) goto L_0x00ae;
     */
    /* JADX WARNING: Missing block: B:47:0x014b, code:
            r7 = r24.mPowerRequest.dozeScreenBrightness;
     */
    /* JADX WARNING: Missing block: B:48:0x0157, code:
            r18 = 3;
     */
    /* JADX WARNING: Missing block: B:49:0x015a, code:
            r18 = 5;
     */
    /* JADX WARNING: Missing block: B:51:0x0164, code:
            if (r24.mProximitySensor == null) goto L_0x090d;
     */
    /* JADX WARNING: Missing block: B:53:0x016c, code:
            if (r24.useProximityForceSuspend == false) goto L_0x0848;
     */
    /* JADX WARNING: Missing block: B:55:0x017a, code:
            if (r24.mPowerRequest.useProximitySensor == false) goto L_0x0761;
     */
    /* JADX WARNING: Missing block: B:56:0x017c, code:
            setProximitySensorEnabled(true);
     */
    /* JADX WARNING: Missing block: B:57:0x018b, code:
            if (r24.mProximityEventHandled != false) goto L_0x0752;
     */
    /* JADX WARNING: Missing block: B:58:0x018d, code:
            android.util.Slog.i(TAG, "mProximity = " + proximityToString(r24.mProximity));
     */
    /* JADX WARNING: Missing block: B:59:0x01bd, code:
            if (r24.mProximity != 1) goto L_0x0745;
     */
    /* JADX WARNING: Missing block: B:60:0x01bf, code:
            sendOnProximityPositiveSuspendWithWakelock();
     */
    /* JADX WARNING: Missing block: B:61:0x01c2, code:
            r24.mProximityEventHandled = true;
     */
    /* JADX WARNING: Missing block: B:63:0x01d0, code:
            if (r24.useProximityForceSuspend != false) goto L_0x01dc;
     */
    /* JADX WARNING: Missing block: B:65:0x01d8, code:
            if (r24.mScreenOffBecauseOfProximity == false) goto L_0x01dc;
     */
    /* JADX WARNING: Missing block: B:66:0x01da, code:
            r18 = 1;
     */
    /* JADX WARNING: Missing block: B:67:0x01dc, code:
            r13 = r24.mPowerState.getScreenState();
            animateScreenStateChange(r18, r14);
            r18 = r24.mPowerState.getScreenState();
     */
    /* JADX WARNING: Missing block: B:68:0x01fd, code:
            if (r18 != 1) goto L_0x0200;
     */
    /* JADX WARNING: Missing block: B:69:0x01ff, code:
            r7 = 0;
     */
    /* JADX WARNING: Missing block: B:70:0x0200, code:
            r5 = false;
     */
    /* JADX WARNING: Missing block: B:71:0x0207, code:
            if (r24.mAutomaticBrightnessController == null) goto L_0x0272;
     */
    /* JADX WARNING: Missing block: B:73:0x020f, code:
            if (r24.mAllowAutoBrightnessWhileDozingConfig == false) goto L_0x091a;
     */
    /* JADX WARNING: Missing block: B:75:0x0217, code:
            if (r18 == 3) goto L_0x0221;
     */
    /* JADX WARNING: Missing block: B:77:0x021f, code:
            if (r18 != 4) goto L_0x0917;
     */
    /* JADX WARNING: Missing block: B:78:0x0221, code:
            r6 = true;
     */
    /* JADX WARNING: Missing block: B:80:0x022e, code:
            if (r24.mPowerRequest.useAutoBrightness == false) goto L_0x0920;
     */
    /* JADX WARNING: Missing block: B:82:0x0236, code:
            if (r18 == 2) goto L_0x023a;
     */
    /* JADX WARNING: Missing block: B:83:0x0238, code:
            if (r6 == false) goto L_0x0920;
     */
    /* JADX WARNING: Missing block: B:84:0x023a, code:
            if (r7 >= 0) goto L_0x091d;
     */
    /* JADX WARNING: Missing block: B:85:0x023c, code:
            r5 = true;
     */
    /* JADX WARNING: Missing block: B:86:0x023d, code:
            if (r4 == false) goto L_0x0923;
     */
    /* JADX WARNING: Missing block: B:87:0x023f, code:
            r19 = r24.mPowerRequest.brightnessSetByUser;
     */
    /* JADX WARNING: Missing block: B:88:0x024b, code:
            r22 = r24.mAutomaticBrightnessController;
            r23 = r24.mPowerRequest.screenAutoBrightnessAdjustment;
     */
    /* JADX WARNING: Missing block: B:89:0x0263, code:
            if (r18 == 2) goto L_0x0927;
     */
    /* JADX WARNING: Missing block: B:90:0x0265, code:
            r21 = true;
     */
    /* JADX WARNING: Missing block: B:91:0x0267, code:
            r22.configure(r5, r23, r21, r19);
     */
    /* JADX WARNING: Missing block: B:93:0x027e, code:
            if (r24.mPowerRequest.boostScreenBrightness == false) goto L_0x0284;
     */
    /* JADX WARNING: Missing block: B:94:0x0280, code:
            if (r7 == 0) goto L_0x0284;
     */
    /* JADX WARNING: Missing block: B:95:0x0282, code:
            r7 = android.os.PowerManager.BRIGHTNESS_MULTIBITS_ON;
     */
    /* JADX WARNING: Missing block: B:96:0x0284, code:
            r17 = false;
     */
    /* JADX WARNING: Missing block: B:97:0x0286, code:
            if (r7 >= 0) goto L_0x0935;
     */
    /* JADX WARNING: Missing block: B:98:0x0288, code:
            if (r5 == false) goto L_0x02b4;
     */
    /* JADX WARNING: Missing block: B:99:0x028a, code:
            r7 = r24.mAutomaticBrightnessController.getAutomaticScreenBrightness();
     */
    /* JADX WARNING: Missing block: B:100:0x0296, code:
            if (DEBUG == false) goto L_0x02b4;
     */
    /* JADX WARNING: Missing block: B:101:0x0298, code:
            android.util.Slog.i(TAG, "Auto brightness ---002--- brightness = " + r7);
     */
    /* JADX WARNING: Missing block: B:102:0x02b4, code:
            if (r7 < 0) goto L_0x092b;
     */
    /* JADX WARNING: Missing block: B:103:0x02b6, code:
            r7 = clampScreenBrightness(r7);
     */
    /* JADX WARNING: Missing block: B:104:0x02be, code:
            if (DEBUG == false) goto L_0x02dc;
     */
    /* JADX WARNING: Missing block: B:105:0x02c0, code:
            android.util.Slog.i(TAG, "Auto brightness ---003--- brightness = " + r7);
     */
    /* JADX WARNING: Missing block: B:107:0x02e2, code:
            if (r24.mAppliedAutoBrightness == false) goto L_0x02ea;
     */
    /* JADX WARNING: Missing block: B:109:0x02e6, code:
            if ((r4 ^ 1) == 0) goto L_0x02ea;
     */
    /* JADX WARNING: Missing block: B:110:0x02e8, code:
            r17 = true;
     */
    /* JADX WARNING: Missing block: B:111:0x02ea, code:
            r21 = mOppoBrightUtils;
     */
    /* JADX WARNING: Missing block: B:112:0x02f4, code:
            if (com.android.server.display.OppoBrightUtils.mBrightnessBoost != 2) goto L_0x02f8;
     */
    /* JADX WARNING: Missing block: B:113:0x02f6, code:
            r17 = false;
     */
    /* JADX WARNING: Missing block: B:114:0x02f8, code:
            r24.mAppliedAutoBrightness = true;
     */
    /* JADX WARNING: Missing block: B:115:0x0300, code:
            if (r7 >= 0) goto L_0x0316;
     */
    /* JADX WARNING: Missing block: B:117:0x0308, code:
            if (r18 == 3) goto L_0x0312;
     */
    /* JADX WARNING: Missing block: B:119:0x0310, code:
            if (r18 != 4) goto L_0x0316;
     */
    /* JADX WARNING: Missing block: B:120:0x0312, code:
            r7 = r24.mScreenBrightnessDozeConfig;
     */
    /* JADX WARNING: Missing block: B:121:0x0316, code:
            if (r7 >= 0) goto L_0x034c;
     */
    /* JADX WARNING: Missing block: B:122:0x0318, code:
            r7 = clampScreenBrightness(r24.mPowerRequest.screenBrightness);
     */
    /* JADX WARNING: Missing block: B:123:0x032e, code:
            if (DEBUG == false) goto L_0x034c;
     */
    /* JADX WARNING: Missing block: B:124:0x0330, code:
            android.util.Slog.i(TAG, "Auto brightness ---005--- brightness = " + r7);
     */
    /* JADX WARNING: Missing block: B:126:0x035e, code:
            if (r24.mPowerRequest.policy != 2) goto L_0x0943;
     */
    /* JADX WARNING: Missing block: B:128:0x0368, code:
            if (r7 <= r24.mScreenBrightnessRangeMinimum) goto L_0x03a6;
     */
    /* JADX WARNING: Missing block: B:130:0x036c, code:
            if ((r7 - 10) <= 0) goto L_0x093f;
     */
    /* JADX WARNING: Missing block: B:131:0x036e, code:
            r7 = r7 - 10;
     */
    /* JADX WARNING: Missing block: B:132:0x0370, code:
            r7 = java.lang.Math.max(java.lang.Math.min(r7, r24.mScreenBrightnessDimConfig), r24.mScreenBrightnessRangeMinimum);
     */
    /* JADX WARNING: Missing block: B:133:0x0388, code:
            if (DEBUG == false) goto L_0x03a6;
     */
    /* JADX WARNING: Missing block: B:134:0x038a, code:
            android.util.Slog.i(TAG, "Auto brightness ---006--- brightness = " + r7);
     */
    /* JADX WARNING: Missing block: B:136:0x03ac, code:
            if (r24.mAppliedDimming != false) goto L_0x03b0;
     */
    /* JADX WARNING: Missing block: B:137:0x03ae, code:
            r17 = false;
     */
    /* JADX WARNING: Missing block: B:138:0x03b0, code:
            r24.mAppliedDimming = true;
            mScreenDimQuicklyDark = true;
            r21 = mOppoBrightUtils;
            com.android.server.display.OppoBrightUtils.mManualSetAutoBrightness = false;
     */
    /* JADX WARNING: Missing block: B:140:0x03ce, code:
            if (r24.mPowerRequest.lowPowerMode == false) goto L_0x0957;
     */
    /* JADX WARNING: Missing block: B:142:0x03d8, code:
            if (r7 <= r24.mScreenBrightnessRangeMinimum) goto L_0x03e6;
     */
    /* JADX WARNING: Missing block: B:143:0x03da, code:
            r7 = java.lang.Math.max(r7 / 2, r24.mScreenBrightnessRangeMinimum);
     */
    /* JADX WARNING: Missing block: B:145:0x03ec, code:
            if (r24.mAppliedLowPower != false) goto L_0x03f0;
     */
    /* JADX WARNING: Missing block: B:146:0x03ee, code:
            r17 = false;
     */
    /* JADX WARNING: Missing block: B:147:0x03f0, code:
            r24.mAppliedLowPower = true;
     */
    /* JADX WARNING: Missing block: B:149:0x03fa, code:
            if (DEBUG_PANIC == false) goto L_0x043c;
     */
    /* JADX WARNING: Missing block: B:150:0x03fc, code:
            android.util.Slog.d(TAG, "updatePowerState: state = " + r18 + ", brightness = " + r7 + "mColorFadeOffAnimator.isStarted() : " + r24.mColorFadeOffAnimator.isStarted());
     */
    /* JADX WARNING: Missing block: B:152:0x0442, code:
            if (r24.mPendingScreenOff != false) goto L_0x0600;
     */
    /* JADX WARNING: Missing block: B:154:0x044a, code:
            if (r24.mSkipScreenOnBrightnessRamp == false) goto L_0x0470;
     */
    /* JADX WARNING: Missing block: B:156:0x0452, code:
            if (r18 != 2) goto L_0x09ad;
     */
    /* JADX WARNING: Missing block: B:158:0x045a, code:
            if (r24.mSkipRampState != 0) goto L_0x096b;
     */
    /* JADX WARNING: Missing block: B:160:0x0462, code:
            if (r24.mDozing == false) goto L_0x096b;
     */
    /* JADX WARNING: Missing block: B:161:0x0464, code:
            r24.mInitialAutoBrightness = r7;
            r24.mSkipRampState = 1;
     */
    /* JADX WARNING: Missing block: B:163:0x0476, code:
            if (r18 == 5) goto L_0x047e;
     */
    /* JADX WARNING: Missing block: B:165:0x047c, code:
            if (r13 != 5) goto L_0x09b7;
     */
    /* JADX WARNING: Missing block: B:166:0x047e, code:
            r20 = true;
     */
    /* JADX WARNING: Missing block: B:168:0x0486, code:
            if (r18 != 2) goto L_0x09bb;
     */
    /* JADX WARNING: Missing block: B:170:0x048e, code:
            if (r24.mSkipRampState != 0) goto L_0x09bb;
     */
    /* JADX WARNING: Missing block: B:172:0x0492, code:
            if ((r20 ^ 1) == 0) goto L_0x0600;
     */
    /* JADX WARNING: Missing block: B:173:0x0494, code:
            r15 = BRIGHTNESS_RAMP_RATE_SLOW;
     */
    /* JADX WARNING: Missing block: B:174:0x0496, code:
            if (r5 == false) goto L_0x09cf;
     */
    /* JADX WARNING: Missing block: B:175:0x0498, code:
            r15 = r24.mAutomaticBrightnessController.mAutoRate;
     */
    /* JADX WARNING: Missing block: B:176:0x04a4, code:
            if (DEBUG == false) goto L_0x04c2;
     */
    /* JADX WARNING: Missing block: B:177:0x04a6, code:
            android.util.Slog.i(TAG, "Auto brightness ---001--- rate = " + r15);
     */
    /* JADX WARNING: Missing block: B:178:0x04c2, code:
            if (r17 == false) goto L_0x04d0;
     */
    /* JADX WARNING: Missing block: B:179:0x04c4, code:
            r21 = mOppoBrightUtils;
     */
    /* JADX WARNING: Missing block: B:180:0x04ce, code:
            if (com.android.server.display.OppoBrightUtils.mBrightnessBoost != 2) goto L_0x04f2;
     */
    /* JADX WARNING: Missing block: B:181:0x04d0, code:
            r15 = BRIGHTNESS_RAMP_RATE_FAST;
     */
    /* JADX WARNING: Missing block: B:182:0x04d4, code:
            if (DEBUG == false) goto L_0x04f2;
     */
    /* JADX WARNING: Missing block: B:183:0x04d6, code:
            android.util.Slog.i(TAG, "Auto brightness ---002--- rate = " + r15);
     */
    /* JADX WARNING: Missing block: B:184:0x04f2, code:
            r21 = mOppoBrightUtils;
     */
    /* JADX WARNING: Missing block: B:185:0x04fc, code:
            if (com.android.server.display.OppoBrightUtils.mBrightnessBoost != 3) goto L_0x0546;
     */
    /* JADX WARNING: Missing block: B:186:0x04fe, code:
            r15 = BRIGHTNESS_RAMP_RATE_FAST;
     */
    /* JADX WARNING: Missing block: B:187:0x0502, code:
            if (DEBUG == false) goto L_0x0520;
     */
    /* JADX WARNING: Missing block: B:188:0x0504, code:
            android.util.Slog.i(TAG, "Auto brightness ---003--- rate = " + r15);
     */
    /* JADX WARNING: Missing block: B:189:0x0520, code:
            r24.mHandler.removeMessages(5);
            r24.mHandler.sendMessageDelayed(r24.mHandler.obtainMessage(5), com.android.server.display.OppoBrightUtils.BRIGHTNESS_BOOST_SWITCHON_TIMEOUT);
     */
    /* JADX WARNING: Missing block: B:191:0x0548, code:
            if (mQuickDarkToBright == false) goto L_0x0570;
     */
    /* JADX WARNING: Missing block: B:192:0x054a, code:
            mQuickDarkToBright = false;
            r15 = BRIGHTNESS_RAMP_RATE_SCREENON;
     */
    /* JADX WARNING: Missing block: B:193:0x0552, code:
            if (DEBUG == false) goto L_0x0570;
     */
    /* JADX WARNING: Missing block: B:194:0x0554, code:
            android.util.Slog.i(TAG, "Auto brightness ---004--- rate = " + r15);
     */
    /* JADX WARNING: Missing block: B:195:0x0570, code:
            r21 = mOppoBrightUtils;
     */
    /* JADX WARNING: Missing block: B:196:0x0574, code:
            if (com.android.server.display.OppoBrightUtils.mPocketRingingState == false) goto L_0x059f;
     */
    /* JADX WARNING: Missing block: B:197:0x0576, code:
            mOppoBrightUtils.setPocketRingingState(false);
            r15 = BRIGHTNESS_RAMP_RATE_FAST;
     */
    /* JADX WARNING: Missing block: B:198:0x0581, code:
            if (DEBUG == false) goto L_0x059f;
     */
    /* JADX WARNING: Missing block: B:199:0x0583, code:
            android.util.Slog.i(TAG, "Auto brightness ---005--- rate = " + r15);
     */
    /* JADX WARNING: Missing block: B:200:0x059f, code:
            r21 = mOppoBrightUtils;
     */
    /* JADX WARNING: Missing block: B:201:0x05a9, code:
            if (com.android.server.display.OppoBrightUtils.mInverseMode != 1) goto L_0x05b3;
     */
    /* JADX WARNING: Missing block: B:202:0x05ab, code:
            r7 = mOppoBrightUtils.adjustInverseModeBrightness(r7);
     */
    /* JADX WARNING: Missing block: B:203:0x05b3, code:
            r21 = mOppoBrightUtils;
     */
    /* JADX WARNING: Missing block: B:204:0x05b7, code:
            if (com.android.server.display.OppoBrightUtils.mHighBrightnessModeSupport == false) goto L_0x05fb;
     */
    /* JADX WARNING: Missing block: B:205:0x05b9, code:
            r21 = mOppoBrightUtils;
     */
    /* JADX WARNING: Missing block: B:206:0x05c3, code:
            if (com.android.server.display.OppoBrightUtils.mCameraMode == 1) goto L_0x09db;
     */
    /* JADX WARNING: Missing block: B:207:0x05c5, code:
            r21 = mOppoBrightUtils;
     */
    /* JADX WARNING: Missing block: B:208:0x05cf, code:
            if (com.android.server.display.OppoBrightUtils.mGalleryMode == 1) goto L_0x09db;
     */
    /* JADX WARNING: Missing block: B:209:0x05d1, code:
            r21 = mOppoBrightUtils;
     */
    /* JADX WARNING: Missing block: B:210:0x05db, code:
            if (com.android.server.display.OppoBrightUtils.mVideoMode == 1) goto L_0x09db;
     */
    /* JADX WARNING: Missing block: B:212:0x05e1, code:
            if (r7 <= 2) goto L_0x05e7;
     */
    /* JADX WARNING: Missing block: B:213:0x05e3, code:
            r7 = (r7 * 93) / 100;
     */
    /* JADX WARNING: Missing block: B:214:0x05e7, code:
            r21 = mOppoBrightUtils;
     */
    /* JADX WARNING: Missing block: B:215:0x05f1, code:
            if (com.android.server.display.OppoBrightUtils.mShouldAdjustRate != 1) goto L_0x05fb;
     */
    /* JADX WARNING: Missing block: B:216:0x05f3, code:
            r21 = mOppoBrightUtils;
            com.android.server.display.OppoBrightUtils.mShouldAdjustRate = 0;
            r15 = 70;
     */
    /* JADX WARNING: Missing block: B:217:0x05fb, code:
            animateScreenBrightness(r7, r15);
     */
    /* JADX WARNING: Missing block: B:219:0x0606, code:
            if (r24.mPendingScreenOnUnblocker != null) goto L_0x09e3;
     */
    /* JADX WARNING: Missing block: B:221:0x060e, code:
            if (r24.mColorFadeEnabled == false) goto L_0x062a;
     */
    /* JADX WARNING: Missing block: B:223:0x061a, code:
            if (r24.mColorFadeOnAnimator.isStarted() != false) goto L_0x09e3;
     */
    /* JADX WARNING: Missing block: B:225:0x0628, code:
            if ((r24.mColorFadeOffAnimator.isStarted() ^ 1) == 0) goto L_0x09e3;
     */
    /* JADX WARNING: Missing block: B:227:0x0632, code:
            if ((r24.mPendingDisplayReadyBlocker ^ 1) == 0) goto L_0x09e3;
     */
    /* JADX WARNING: Missing block: B:228:0x0634, code:
            r16 = r24.mPowerState.waitUntilClean(r24.mCleanListener);
     */
    /* JADX WARNING: Missing block: B:229:0x0644, code:
            if (r16 == false) goto L_0x09e7;
     */
    /* JADX WARNING: Missing block: B:230:0x0646, code:
            r8 = r24.mScreenBrightnessRampAnimator.isAnimating() ^ 1;
     */
    /* JADX WARNING: Missing block: B:231:0x0652, code:
            if (r16 == false) goto L_0x067c;
     */
    /* JADX WARNING: Missing block: B:233:0x065a, code:
            if (r18 == 1) goto L_0x067c;
     */
    /* JADX WARNING: Missing block: B:235:0x0668, code:
            if (r24.mReportedScreenStateToPolicy != 1) goto L_0x067c;
     */
    /* JADX WARNING: Missing block: B:236:0x066a, code:
            setReportedScreenState(2);
            r24.mWindowManagerPolicy.screenTurnedOn();
     */
    /* JADX WARNING: Missing block: B:237:0x067c, code:
            if (r8 != 0) goto L_0x06a6;
     */
    /* JADX WARNING: Missing block: B:239:0x0686, code:
            if ((r24.mUnfinishedBusiness ^ 1) == 0) goto L_0x06a6;
     */
    /* JADX WARNING: Missing block: B:241:0x068a, code:
            if (DEBUG == false) goto L_0x0695;
     */
    /* JADX WARNING: Missing block: B:242:0x068c, code:
            android.util.Slog.d(TAG, "Unfinished business...");
     */
    /* JADX WARNING: Missing block: B:243:0x0695, code:
            r24.mCallbacks.acquireSuspendBlocker();
            r24.mUnfinishedBusiness = true;
     */
    /* JADX WARNING: Missing block: B:244:0x06a6, code:
            if (r16 == false) goto L_0x070c;
     */
    /* JADX WARNING: Missing block: B:245:0x06a8, code:
            if (r12 == false) goto L_0x070c;
     */
    /* JADX WARNING: Missing block: B:247:0x06b0, code:
            if (r18 == 1) goto L_0x06ba;
     */
    /* JADX WARNING: Missing block: B:249:0x06b8, code:
            if (r18 != 3) goto L_0x09ea;
     */
    /* JADX WARNING: Missing block: B:250:0x06ba, code:
            r9 = true;
     */
    /* JADX WARNING: Missing block: B:252:0x06c1, code:
            if (r24.mBiometricsManager == null) goto L_0x06e0;
     */
    /* JADX WARNING: Missing block: B:254:0x06c5, code:
            if ((r9 ^ 1) == 0) goto L_0x06e0;
     */
    /* JADX WARNING: Missing block: B:256:0x06d3, code:
            if (r24.mPowerRequest.policy == 0) goto L_0x06e0;
     */
    /* JADX WARNING: Missing block: B:257:0x06d5, code:
            r24.mBiometricsManager.onAnimateScreenBrightness(r7);
     */
    /* JADX WARNING: Missing block: B:258:0x06e0, code:
            r22 = r24.mLock;
     */
    /* JADX WARNING: Missing block: B:259:0x06e6, code:
            monitor-enter(r22);
     */
    /* JADX WARNING: Missing block: B:262:0x06ed, code:
            if (r24.mPendingRequestChangedLocked != false) goto L_0x0708;
     */
    /* JADX WARNING: Missing block: B:263:0x06ef, code:
            r24.mDisplayReadyLocked = true;
     */
    /* JADX WARNING: Missing block: B:264:0x06f9, code:
            if (DEBUG == false) goto L_0x0708;
     */
    /* JADX WARNING: Missing block: B:265:0x06fb, code:
            android.util.Slog.d(TAG, "Display ready!");
     */
    /* JADX WARNING: Missing block: B:266:0x0708, code:
            monitor-exit(r22);
     */
    /* JADX WARNING: Missing block: B:267:0x0709, code:
            sendOnStateChangedWithWakelock();
     */
    /* JADX WARNING: Missing block: B:268:0x070c, code:
            if (r8 == 0) goto L_0x0734;
     */
    /* JADX WARNING: Missing block: B:270:0x0714, code:
            if (r24.mUnfinishedBusiness == false) goto L_0x0734;
     */
    /* JADX WARNING: Missing block: B:272:0x0718, code:
            if (DEBUG == false) goto L_0x0723;
     */
    /* JADX WARNING: Missing block: B:273:0x071a, code:
            android.util.Slog.d(TAG, "Finished business...");
     */
    /* JADX WARNING: Missing block: B:274:0x0723, code:
            r24.mUnfinishedBusiness = false;
            r24.mCallbacks.releaseSuspendBlocker();
     */
    /* JADX WARNING: Missing block: B:276:0x073a, code:
            if (r18 == 2) goto L_0x09f8;
     */
    /* JADX WARNING: Missing block: B:277:0x073c, code:
            r21 = true;
     */
    /* JADX WARNING: Missing block: B:278:0x073e, code:
            r24.mDozing = r21;
     */
    /* JADX WARNING: Missing block: B:279:0x0744, code:
            return;
     */
    /* JADX WARNING: Missing block: B:281:0x074b, code:
            if (r24.mProximity != 0) goto L_0x01c2;
     */
    /* JADX WARNING: Missing block: B:282:0x074d, code:
            sendOnProximityNegativeSuspendWithWakelock();
     */
    /* JADX WARNING: Missing block: B:284:0x0754, code:
            if (DEBUG == false) goto L_0x01ca;
     */
    /* JADX WARNING: Missing block: B:285:0x0756, code:
            android.util.Slog.i(TAG, "the last proximity event has been handled");
     */
    /* JADX WARNING: Missing block: B:287:0x0767, code:
            if (r24.mProximitySensorEnabled == false) goto L_0x01ca;
     */
    /* JADX WARNING: Missing block: B:288:0x0769, code:
            android.util.Slog.i(TAG, "mPowerRequest.useProximitySensor = " + r24.mPowerRequest.useProximitySensor + ", mWaitingForNegativeProximity = " + r24.mWaitingForNegativeProximity + ", state = " + r18);
     */
    /* JADX WARNING: Missing block: B:289:0x07b5, code:
            if (r24.mWaitingForNegativeProximity == false) goto L_0x07f4;
     */
    /* JADX WARNING: Missing block: B:291:0x07c3, code:
            if (r24.mProximity != 1) goto L_0x07f4;
     */
    /* JADX WARNING: Missing block: B:293:0x07cb, code:
            if (r18 == 1) goto L_0x07d5;
     */
    /* JADX WARNING: Missing block: B:295:0x07d3, code:
            if (r18 != 3) goto L_0x07e0;
     */
    /* JADX WARNING: Missing block: B:296:0x07d5, code:
            setProximitySensorEnabled(true);
     */
    /* JADX WARNING: Missing block: B:298:0x07e6, code:
            if (r18 == 4) goto L_0x07d5;
     */
    /* JADX WARNING: Missing block: B:300:0x07f2, code:
            if (getScreenState() != 1) goto L_0x07d5;
     */
    /* JADX WARNING: Missing block: B:301:0x07f4, code:
            setProximitySensorEnabled(false);
     */
    /* JADX WARNING: Missing block: B:302:0x0803, code:
            if (r18 == 1) goto L_0x080d;
     */
    /* JADX WARNING: Missing block: B:304:0x080b, code:
            if (r18 != 3) goto L_0x0833;
     */
    /* JADX WARNING: Missing block: B:305:0x080d, code:
            android.util.Slog.i(TAG, "turn on lcd light due to proximity released");
            sendOnProximityNegativeSuspendWithWakelock();
     */
    /* JADX WARNING: Missing block: B:306:0x0819, code:
            r24.mScreenOffBecauseOfProximity = false;
            r24.mWaitingForNegativeProximity = false;
            r24.mProximityEventHandled = true;
     */
    /* JADX WARNING: Missing block: B:308:0x0839, code:
            if (r18 == 4) goto L_0x080d;
     */
    /* JADX WARNING: Missing block: B:310:0x0845, code:
            if (getScreenState() == 1) goto L_0x0819;
     */
    /* JADX WARNING: Missing block: B:312:0x0854, code:
            if (r24.mPowerRequest.useProximitySensor == false) goto L_0x08bb;
     */
    /* JADX WARNING: Missing block: B:314:0x085c, code:
            if (r18 == 1) goto L_0x08bb;
     */
    /* JADX WARNING: Missing block: B:316:0x0864, code:
            if (r18 == 3) goto L_0x08bb;
     */
    /* JADX WARNING: Missing block: B:318:0x086c, code:
            if (r18 == 4) goto L_0x08bb;
     */
    /* JADX WARNING: Missing block: B:319:0x086e, code:
            setProximitySensorEnabled(true);
     */
    /* JADX WARNING: Missing block: B:320:0x087d, code:
            if (r24.mScreenOffBecauseOfProximity != false) goto L_0x0898;
     */
    /* JADX WARNING: Missing block: B:322:0x088b, code:
            if (r24.mProximity != 1) goto L_0x0898;
     */
    /* JADX WARNING: Missing block: B:323:0x088d, code:
            r24.mScreenOffBecauseOfProximity = true;
            sendOnProximityPositiveWithWakelock();
     */
    /* JADX WARNING: Missing block: B:325:0x089e, code:
            if (r24.mScreenOffBecauseOfProximity == false) goto L_0x01ca;
     */
    /* JADX WARNING: Missing block: B:327:0x08ac, code:
            if (r24.mProximity == 1) goto L_0x01ca;
     */
    /* JADX WARNING: Missing block: B:328:0x08ae, code:
            r24.mScreenOffBecauseOfProximity = false;
            sendOnProximityNegativeWithWakelock();
     */
    /* JADX WARNING: Missing block: B:330:0x08c1, code:
            if (r24.mWaitingForNegativeProximity == false) goto L_0x08fb;
     */
    /* JADX WARNING: Missing block: B:332:0x08c9, code:
            if (r24.mScreenOffBecauseOfProximity == false) goto L_0x08fb;
     */
    /* JADX WARNING: Missing block: B:334:0x08d7, code:
            if (r24.mProximity != 1) goto L_0x08fb;
     */
    /* JADX WARNING: Missing block: B:336:0x08df, code:
            if (r18 == 1) goto L_0x08fb;
     */
    /* JADX WARNING: Missing block: B:338:0x08e7, code:
            if (r18 == 3) goto L_0x08fb;
     */
    /* JADX WARNING: Missing block: B:340:0x08ef, code:
            if (r18 == 4) goto L_0x08fb;
     */
    /* JADX WARNING: Missing block: B:341:0x08f1, code:
            setProximitySensorEnabled(true);
     */
    /* JADX WARNING: Missing block: B:342:0x08fb, code:
            setProximitySensorEnabled(false);
            r24.mWaitingForNegativeProximity = false;
     */
    /* JADX WARNING: Missing block: B:343:0x090d, code:
            r24.mWaitingForNegativeProximity = false;
     */
    /* JADX WARNING: Missing block: B:344:0x0917, code:
            r6 = false;
     */
    /* JADX WARNING: Missing block: B:345:0x091a, code:
            r6 = false;
     */
    /* JADX WARNING: Missing block: B:346:0x091d, code:
            r5 = false;
     */
    /* JADX WARNING: Missing block: B:347:0x0920, code:
            r5 = false;
     */
    /* JADX WARNING: Missing block: B:348:0x0923, code:
            r19 = false;
     */
    /* JADX WARNING: Missing block: B:349:0x0927, code:
            r21 = false;
     */
    /* JADX WARNING: Missing block: B:350:0x092b, code:
            r24.mAppliedAutoBrightness = false;
     */
    /* JADX WARNING: Missing block: B:351:0x0935, code:
            r24.mAppliedAutoBrightness = false;
     */
    /* JADX WARNING: Missing block: B:352:0x093f, code:
            r7 = r7 / 2;
     */
    /* JADX WARNING: Missing block: B:354:0x0949, code:
            if (r24.mAppliedDimming == false) goto L_0x03c2;
     */
    /* JADX WARNING: Missing block: B:355:0x094b, code:
            r17 = false;
            r24.mAppliedDimming = false;
     */
    /* JADX WARNING: Missing block: B:357:0x095d, code:
            if (r24.mAppliedLowPower == false) goto L_0x03f8;
     */
    /* JADX WARNING: Missing block: B:358:0x095f, code:
            r17 = false;
            r24.mAppliedLowPower = false;
     */
    /* JADX WARNING: Missing block: B:360:0x0977, code:
            if (r24.mSkipRampState != 1) goto L_0x0995;
     */
    /* JADX WARNING: Missing block: B:362:0x097f, code:
            if (r24.mUseSoftwareAutoBrightnessConfig == false) goto L_0x0995;
     */
    /* JADX WARNING: Missing block: B:364:0x0989, code:
            if (r7 == r24.mInitialAutoBrightness) goto L_0x0995;
     */
    /* JADX WARNING: Missing block: B:365:0x098b, code:
            r24.mSkipRampState = 2;
     */
    /* JADX WARNING: Missing block: B:367:0x09a1, code:
            if (r24.mSkipRampState != 2) goto L_0x0470;
     */
    /* JADX WARNING: Missing block: B:368:0x09a3, code:
            r24.mSkipRampState = 0;
     */
    /* JADX WARNING: Missing block: B:369:0x09ad, code:
            r24.mSkipRampState = 0;
     */
    /* JADX WARNING: Missing block: B:370:0x09b7, code:
            r20 = false;
     */
    /* JADX WARNING: Missing block: B:372:0x09c1, code:
            if (r18 != 3) goto L_0x0600;
     */
    /* JADX WARNING: Missing block: B:374:0x09cb, code:
            if ((r24.mBrightnessBucketsInDozeConfig ^ 1) == 0) goto L_0x0600;
     */
    /* JADX WARNING: Missing block: B:375:0x09cf, code:
            r24.mAutomaticBrightnessController.mScreenAutoBrightness = r7;
     */
    /* JADX WARNING: Missing block: B:377:0x09df, code:
            if (r7 >= COLOR_FADE_ON_ANIMATION_DURATION_MILLIS) goto L_0x05e7;
     */
    /* JADX WARNING: Missing block: B:378:0x09e3, code:
            r16 = false;
     */
    /* JADX WARNING: Missing block: B:379:0x09e7, code:
            r8 = 0;
     */
    /* JADX WARNING: Missing block: B:381:0x09f0, code:
            if (r18 == 4) goto L_0x06ba;
     */
    /* JADX WARNING: Missing block: B:382:0x09f2, code:
            r9 = false;
     */
    /* JADX WARNING: Missing block: B:386:0x09f8, code:
            r21 = false;
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
            boolean mustNotify = this.mDisplayReadyLocked ^ 1;
        }
    }

    public void updateBrightness() {
        sendUpdatePowerState();
    }

    public void blockScreenOnByBiometrics(String reason) {
        if (DEBUG_PANIC) {
            Slog.d(TAG_BIOMETRICS, "blockScreenOnByBiometrics, mPendingScreenOnUnblockerFromBiometrics = " + this.mPendingScreenOnUnblockerFromBiometrics);
        }
        if (this.mPendingScreenOnUnblockerFromBiometrics == null) {
            Trace.asyncTraceBegin(131072, SCREEN_ON_BLOCKED_BY_BIOMETRICS_TRACE_NAME, 0);
            this.mPendingScreenOnUnblockerFromBiometrics = new ScreenOnUnblockerByBiometrics(this, null);
        }
        this.mPendingDisplayReadyBlocker = true;
        this.mBlockReasonList.add(reason);
    }

    public void unblockScreenOnByBiometrics(String reason) {
        if (DEBUG_PANIC) {
            Slog.d(TAG_BIOMETRICS, "unblockScreen(reason = " + reason + ") , mPendingScreenOnUnblockerFromBiometrics = " + this.mPendingScreenOnUnblockerFromBiometrics);
        }
        if (this.mPendingScreenOnUnblockerFromBiometrics != null) {
            this.mPendingScreenOnUnblockerFromBiometrics = null;
            Trace.asyncTraceEnd(131072, SCREEN_ON_BLOCKED_BY_BIOMETRICS_TRACE_NAME, 0);
        }
        this.mPendingDisplayReadyBlocker = false;
        if (!PowerManagerService.UNBLOCK_REASON_GO_TO_SLEEP.equals(reason)) {
            sendUpdatePowerState();
        }
        this.mBlockReasonList.clear();
    }

    private void unblockDisplayReady() {
        if (this.mPowerManagerInternal != null) {
            if (this.mPowerManagerInternal.isStartGoToSleep()) {
                this.mPendingDisplayReadyBlocker = false;
            }
            if (DEBUG_PANIC) {
                Slog.d(TAG_BIOMETRICS, "unblockDisplayReady, mPendingDisplayReadyBlocker = " + this.mPendingDisplayReadyBlocker);
            }
        }
    }

    public boolean isBlockScreenOnByBiometrics() {
        return this.mPendingScreenOnUnblockerFromBiometrics != null;
    }

    public boolean isBlockDisplayByBiometrics() {
        return this.mPendingDisplayReadyBlocker;
    }

    public boolean hasBiometricsBlockedReason(String reason) {
        return this.mBlockReasonList.contains(reason);
    }

    public int getScreenState() {
        if (DEBUG_PANIC) {
            boolean z;
            String str = TAG_BIOMETRICS;
            StringBuilder append = new StringBuilder().append("ScreenState = ").append(this.mPowerState.getScreenState()).append(", fingerPrint block = ");
            if (this.mPendingScreenOnUnblockerFromBiometrics != null) {
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
        return (this.mPowerState.getScreenState() == 2 && this.mPendingScreenOnUnblockerFromBiometrics == null && this.mPendingScreenOnUnblocker == null) ? 1 : 0;
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
            Trace.asyncTraceBegin(131072, SCREEN_ON_BLOCKED_TRACE_NAME, 0);
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
            Trace.asyncTraceEnd(131072, SCREEN_ON_BLOCKED_TRACE_NAME, 0);
        }
    }

    private void blockScreenOff() {
        if (this.mPendingScreenOffUnblocker == null) {
            Trace.asyncTraceBegin(131072, SCREEN_OFF_BLOCKED_TRACE_NAME, 0);
            this.mPendingScreenOffUnblocker = new ScreenOffUnblocker(this, null);
            this.mScreenOffBlockStartRealTime = SystemClock.elapsedRealtime();
            Slog.i(TAG, "Blocking screen off");
        }
    }

    private void unblockScreenOff() {
        if (this.mPendingScreenOffUnblocker != null) {
            this.mPendingScreenOffUnblocker = null;
            Slog.i(TAG, "Unblocked screen off after " + (SystemClock.elapsedRealtime() - this.mScreenOffBlockStartRealTime) + " ms");
            Trace.asyncTraceEnd(131072, SCREEN_OFF_BLOCKED_TRACE_NAME, 0);
        }
    }

    private boolean setScreenState(int state) {
        return setScreenState(state, false);
    }

    private boolean setScreenState(int state, boolean reportOnly) {
        OppoBrightUtils oppoBrightUtils;
        String str;
        StringBuilder append;
        OppoBrightUtils oppoBrightUtils2;
        boolean z = true;
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
            }
        }
        boolean isOff = state == 1;
        if (this.mPowerState.getScreenState() != state) {
            if (isOff && (this.mScreenOffBecauseOfProximity ^ 1) != 0) {
                if (this.mReportedScreenStateToPolicy == 2) {
                    setReportedScreenState(3);
                    blockScreenOff();
                    this.mWindowManagerPolicy.screenTurningOff(this.mPendingScreenOffUnblocker);
                    unblockScreenOff();
                } else if (this.mPendingScreenOffUnblocker != null) {
                    return false;
                }
            }
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
                if (AutomaticBrightnessController.mProximityNear && (OppoBrightUtils.DEBUG_PRETEND_PROX_SENSOR_ABSENT ^ 1) != 0) {
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
            if (!reportOnly) {
                this.mPowerState.setScreenState(state);
                try {
                    if (DEBUG_PANIC) {
                        Slog.d(TAG, "mBatteryStats.noteScreenState +++");
                    }
                    this.mBatteryStats.noteScreenState(state);
                    if (DEBUG_PANIC) {
                        Slog.d(TAG, "mBatteryStats.noteScreenState ---");
                    }
                } catch (RemoteException e) {
                }
            }
        }
        if (isOff && this.mReportedScreenStateToPolicy != 0 && (this.mScreenOffBecauseOfProximity ^ 1) != 0) {
            setReportedScreenState(0);
            unblockScreenOn();
            unblockDisplayReady();
            this.mWindowManagerPolicy.screenTurnedOff();
        } else if (!isOff && this.mReportedScreenStateToPolicy == 3) {
            unblockScreenOff();
            this.mWindowManagerPolicy.screenTurnedOff();
            setReportedScreenState(0);
        }
        if (!isOff && this.mReportedScreenStateToPolicy == 0) {
            setReportedScreenState(1);
            if (this.mPowerState.getColorFadeLevel() == OppoBrightUtils.MIN_LUX_LIMITI) {
                blockScreenOn();
            } else {
                unblockScreenOn();
            }
            this.mWindowManagerPolicy.screenTurningOn(this.mPendingScreenOnUnblocker);
        }
        if (!(this.mPendingScreenOnUnblocker == null && this.mPendingScreenOnUnblockerFromBiometrics == null)) {
            z = false;
        }
        return z;
    }

    private void setReportedScreenState(int state) {
        Trace.traceCounter(131072, "ReportedScreenStateToPolicy", state);
        this.mReportedScreenStateToPolicy = state;
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
            Trace.traceCounter(131072, "TargetScreenBrightness", target);
            try {
                this.mBatteryStats.noteScreenBrightness(target);
            } catch (RemoteException e) {
            }
        }
    }

    private void animateScreenStateChange(int target, boolean performScreenOffTransition) {
        int i = 2;
        if (!this.mColorFadeEnabled || (!this.mColorFadeOnAnimator.isStarted() && !this.mColorFadeOffAnimator.isStarted())) {
            if (this.mDisplayBlanksAfterDozeConfig && Display.isDozeState(this.mPowerState.getScreenState()) && (Display.isDozeState(target) ^ 1) != 0) {
                boolean z;
                this.mPowerState.prepareColorFade(this.mContext, this.mColorFadeFadesConfig ? 2 : 0);
                if (this.mColorFadeOffAnimator != null) {
                    this.mColorFadeOffAnimator.end();
                }
                if (target != 1) {
                    z = true;
                } else {
                    z = false;
                }
                setScreenState(1, z);
            }
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
            } else if (target == 5) {
                if (!(this.mScreenBrightnessRampAnimator.isAnimating() && this.mPowerState.getScreenState() == 2) && setScreenState(5)) {
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
                if (!this.mColorFadeEnabled) {
                    this.mPowerState.setColorFadeLevel(OppoBrightUtils.MIN_LUX_LIMITI);
                }
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
                if (DEBUG_PANIC) {
                    Slog.d(TAG, "setProximitySensorEnabled : True");
                }
                this.mProximitySensorEnabled = true;
                this.mSensorManager.registerListener(this.mProximitySensorListener, this.mProximitySensor, 3, this.mHandler);
            }
        } else if (this.mProximitySensorEnabled) {
            if (DEBUG_PANIC) {
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
        if (this.mProximitySensorEnabled && (this.mPendingProximity != 0 || (positive ^ 1) == 0)) {
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
        pw.println("  mScreenBrightnessRampAnimator.isAnimating()=" + this.mScreenBrightnessRampAnimator.isAnimating());
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

    private static Spline createAutoBrightnessSpline(int[] lux, int[] brightness) {
        if (lux == null || lux.length == 0 || brightness == null || brightness.length == 0) {
            Slog.e(TAG, "Could not create auto-brightness spline.");
            return null;
        }
        try {
            int n = brightness.length;
            float[] x = new float[n];
            float[] y = new float[n];
            y[0] = normalizeAbsoluteBrightness(brightness[0]);
            for (int i = 1; i < n; i++) {
                x[i] = (float) lux[i - 1];
                y[i] = normalizeAbsoluteBrightness(brightness[i]);
            }
            Spline spline = Spline.createSpline(x, y);
            if (DEBUG) {
                Slog.d(TAG, "Auto-brightness spline: " + spline);
                for (float v = 1.0f; v < ((float) lux[lux.length - 1]) * 1.25f; v *= 1.25f) {
                    Slog.d(TAG, String.format("  %7.1f: %7.1f", new Object[]{Float.valueOf(v), Float.valueOf(spline.interpolate(v))}));
                }
            }
            return spline;
        } catch (IllegalArgumentException ex) {
            Slog.e(TAG, "Could not create auto-brightness spline.", ex);
            return null;
        }
    }

    private static float normalizeAbsoluteBrightness(int value) {
        return ((float) clampAbsoluteBrightness(value)) / ((float) PowerManager.BRIGHTNESS_MULTIBITS_ON);
    }

    private static int clampAbsoluteBrightness(int value) {
        return MathUtils.constrain(value, 0, PowerManager.BRIGHTNESS_MULTIBITS_ON);
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
