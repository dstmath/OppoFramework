package com.android.server.display;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ParceledListSlice;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.display.AmbientBrightnessDayStats;
import android.hardware.display.BrightnessChangeEvent;
import android.hardware.display.BrightnessConfiguration;
import android.hardware.display.DisplayManagerInternal;
import android.metrics.LogMaker;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.MathUtils;
import android.util.Slog;
import android.util.TimeUtils;
import android.view.Display;
import com.android.internal.app.IBatteryStats;
import com.android.internal.logging.MetricsLogger;
import com.android.server.LocalServices;
import com.android.server.UiModeManagerService;
import com.android.server.am.BatteryStatsService;
import com.android.server.biometrics.fingerprint.FingerprintService;
import com.android.server.display.OppoAutomaticBrightnessController;
import com.android.server.display.OppoRampAnimator;
import com.android.server.display.whitebalance.DisplayWhiteBalanceController;
import com.android.server.display.whitebalance.DisplayWhiteBalanceFactory;
import com.android.server.display.whitebalance.DisplayWhiteBalanceSettings;
import com.android.server.oppo.OppoAppScaleHelper;
import com.android.server.policy.WindowManagerPolicy;
import com.color.app.ColorAppEnterInfo;
import com.color.app.ColorAppExitInfo;
import com.color.app.ColorAppSwitchConfig;
import com.color.app.ColorAppSwitchManager;
import java.io.PrintWriter;
import java.util.List;

/* access modifiers changed from: package-private */
public final class DisplayPowerController extends OppoBaseDisplayPowerController implements OppoAutomaticBrightnessController.Callbacks, DisplayWhiteBalanceController.Callbacks {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final int ALWAYSON_SENSOR_RATE_US = 500000;
    private static int APP_REDUCE_BRIGHTNESS_RATE_IN = 10;
    private static int APP_REDUCE_BRIGHTNESS_RATE_OUT = 10;
    private static final int COLOR_FADE_OFF_ANIMATION_DURATION_MILLIS = 100;
    private static final int COLOR_FADE_ON_ANIMATION_DURATION_MILLIS = 250;
    static boolean DEBUG = false;
    static boolean DEBUG_PANIC = false;
    private static final boolean DEBUG_PRETEND_PROXIMITY_SENSOR_ABSENT = false;
    private static final long LCD_HIGH_BRIGHTNESS_STATE_DELAY = 2000;
    private static final long MANUAL_DATA_REPORT_EVENT_ID = 608;
    private static final long MANUAL_DATA_REPORT_PERID = 1000;
    private static final int MSG_CONFIGURE_BRIGHTNESS = 5;
    private static final int MSG_GAMESPACE_IN_OUT_TIMEOUT = 2019;
    private static final int MSG_INIT_RUS_OBJECT = 2020;
    private static final int MSG_PROXIMITY_SENSOR_DEBOUNCED = 2;
    private static final int MSG_SCREEN_OFF_UNBLOCKED = 4;
    private static final int MSG_SCREEN_ON_BRIGHTNESS_BOOST = 8;
    private static final int MSG_SCREEN_ON_UNBLOCKED = 3;
    private static final int MSG_SET_TEMPORARY_AUTO_BRIGHTNESS_ADJUSTMENT = 7;
    private static final int MSG_SET_TEMPORARY_BRIGHTNESS = 6;
    private static final int MSG_UPDATE_HIGH_BRIGHTNESS_STATE = 1;
    private static final int MSG_UPDATE_POWER_STATE = 1;
    private static final boolean MTK_DEBUG = "eng".equals(Build.TYPE);
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
    private static final String SCREEN_ON_BLOCKED_TRACE_NAME = "Screen on blocked";
    private static final long SWITCH_BRIGHTNESS_EVENT_ID = 605;
    private static final long SWITCH_BRIGHTNESS_REPORT_PERID = 7200000;
    private static final String TAG = "DisplayPowerController";
    private static final float TYPICAL_PROXIMITY_THRESHOLD = 5.0f;
    private static final boolean USE_COLOR_FADE_ON_ANIMATION = false;
    private static int mHighBrightnessMode = 0;
    private static float mLux = OppoBrightUtils.MIN_LUX_LIMITI;
    static OppoAppScaleHelper mOppoAppScaleHelper = null;
    /* access modifiers changed from: private */
    public static OppoBrightUtils mOppoBrightUtils;
    private static int mPreHighBrightnessMode = 0;
    public static boolean mQuickDarkToBright = false;
    public static boolean mScreenDimQuicklyDark = false;
    private static boolean mStartTimer = false;
    private final boolean mAllowAutoBrightnessWhileDozingConfig;
    private final Animator.AnimatorListener mAnimatorListener = new Animator.AnimatorListener() {
        /* class com.android.server.display.DisplayPowerController.AnonymousClass2 */

        public void onAnimationStart(Animator animation) {
        }

        public void onAnimationEnd(Animator animation) {
            DisplayPowerController.this.sendUpdatePowerState();
            DisplayPowerController.this.mOppoDisplayPowerHelper.onAnimationChanged(animation, 2);
        }

        public void onAnimationRepeat(Animator animation) {
        }

        public void onAnimationCancel(Animator animation) {
        }
    };
    private boolean mAppliedAutoBrightness;
    private boolean mAppliedBrightnessBoost;
    private boolean mAppliedDimming;
    private boolean mAppliedLowPower;
    private boolean mAppliedScreenBrightnessOverride;
    private boolean mAppliedTemporaryAutoBrightnessAdjustment;
    private boolean mAppliedTemporaryBrightness;
    private float mAutoBrightnessAdjustment;
    private OppoAutomaticBrightnessController mAutomaticBrightnessController;
    private final IBatteryStats mBatteryStats;
    private final DisplayBlanker mBlanker;
    private boolean mBrightnessBucketsInDozeConfig;
    /* access modifiers changed from: private */
    public BrightnessConfiguration mBrightnessConfiguration;
    private BrightnessMappingStrategy mBrightnessMapper;
    private final int mBrightnessRampRateFast;
    private final int mBrightnessRampRateSlow;
    private BrightnessReason mBrightnessReason = new BrightnessReason();
    private BrightnessReason mBrightnessReasonTemp = new BrightnessReason();
    private int mBrightnessSource = -1;
    private final BrightnessTracker mBrightnessTracker;
    /* access modifiers changed from: private */
    public final DisplayManagerInternal.DisplayPowerCallbacks mCallbacks;
    private final Runnable mCleanListener = new Runnable() {
        /* class com.android.server.display.DisplayPowerController.AnonymousClass4 */

        public void run() {
            DisplayPowerController.this.sendUpdatePowerState();
        }
    };
    private final boolean mColorFadeEnabled;
    private boolean mColorFadeFadesConfig;
    private ObjectAnimator mColorFadeOffAnimator;
    private ObjectAnimator mColorFadeOnAnimator;
    /* access modifiers changed from: private */
    public final Context mContext;
    private int mCtsBrightness = -1;
    private int mCurrentScreenBrightnessSetting;
    private boolean mDisplayBlanksAfterDozeConfig;
    private boolean mDisplayReadyLocked;
    private final DisplayWhiteBalanceController mDisplayWhiteBalanceController;
    private final DisplayWhiteBalanceSettings mDisplayWhiteBalanceSettings;
    private final int mDozeBrightnessConfig = 0;
    private boolean mDozing;
    private ColorAppSwitchManager.OnAppSwitchObserver mDynamicObserver = new ColorAppSwitchManager.OnAppSwitchObserver() {
        /* class com.android.server.display.DisplayPowerController.AnonymousClass1 */

        public void onAppEnter(ColorAppEnterInfo info) {
            Slog.d(DisplayPowerController.TAG, "OnAppSwitchObserver: onAppEnter , info = " + info);
            OppoBrightUtils unused = DisplayPowerController.mOppoBrightUtils;
            OppoBrightUtils.mReduceBrightnessMode = 1;
            OppoBrightUtils unused2 = DisplayPowerController.mOppoBrightUtils;
            OppoBrightUtils.mShouldAdjustRate = 2;
            OppoBrightUtils unused3 = DisplayPowerController.mOppoBrightUtils;
            OppoBrightUtils.mReduceBrightnessAnimating = true;
            DisplayPowerController.this.sendUpdatePowerState();
        }

        public void onAppExit(ColorAppExitInfo info) {
            Slog.d(DisplayPowerController.TAG, "OnAppSwitchObserver: onAppExit , info = " + info);
            OppoBrightUtils unused = DisplayPowerController.mOppoBrightUtils;
            OppoBrightUtils.mReduceBrightnessMode = 0;
            OppoBrightUtils unused2 = DisplayPowerController.mOppoBrightUtils;
            OppoBrightUtils.mShouldAdjustRate = 3;
            OppoBrightUtils unused3 = DisplayPowerController.mOppoBrightUtils;
            OppoBrightUtils.mReduceBrightnessAnimating = true;
            DisplayPowerController.this.sendUpdatePowerState();
        }

        public void onActivityEnter(ColorAppEnterInfo info) {
        }

        public void onActivityExit(ColorAppExitInfo info) {
        }
    };
    private boolean mFingerprintOpticalSupport = false;
    /* access modifiers changed from: private */
    public final DisplayControllerHandler mHandler;
    private int mInitialAutoBrightness;
    private long mLastManualStaticsDataReportTime;
    private int mLastScreenBrightness;
    private long mLastSwitchBrightReportTime;
    private int mLastUserSetScreenBrightness;
    private Sensor mLightSensor;
    private boolean mLightSensorAlwaysOn = false;
    private final Object mLock = new Object();
    public long mManualBacklightTmpTime = 0;
    public long mManualBacklightTotalTime = 0;
    private long mManualLightLuxWatchPointOneTotalTime = 0;
    private long mManualLightLuxWatchPointThreeTotalTime = 0;
    private long mManualLightLuxWatchPointTwoTotalTime = 0;
    private boolean mManualLightSensorEnable = false;
    private long mManualLightSensorEnableTime = 0;
    public long mManualMaxBacklightTmpTime = 0;
    public long mManualMaxBacklightTotalTime = 0;
    private final Runnable mOnProximityNegativeRunnable = new Runnable() {
        /* class com.android.server.display.DisplayPowerController.AnonymousClass7 */

        public void run() {
            DisplayPowerController.this.mCallbacks.onProximityNegative();
            DisplayPowerController.this.mCallbacks.releaseSuspendBlocker();
        }
    };
    private final Runnable mOnProximityPositiveRunnable = new Runnable() {
        /* class com.android.server.display.DisplayPowerController.AnonymousClass6 */

        public void run() {
            DisplayPowerController.this.mCallbacks.onProximityPositive();
            DisplayPowerController.this.mCallbacks.releaseSuspendBlocker();
        }
    };
    private final Runnable mOnStateChangedRunnable = new Runnable() {
        /* class com.android.server.display.DisplayPowerController.AnonymousClass5 */

        public void run() {
            DisplayPowerController.this.mCallbacks.onStateChanged();
            DisplayPowerController.this.mCallbacks.releaseSuspendBlocker();
        }
    };
    private float mPendingAutoBrightnessAdjustment;
    private int mPendingProximity = -1;
    private long mPendingProximityDebounceTime = -1;
    private boolean mPendingRequestChangedLocked;
    private DisplayManagerInternal.DisplayPowerRequest mPendingRequestLocked;
    private int mPendingScreenBrightnessSetting;
    private boolean mPendingScreenOff;
    /* access modifiers changed from: private */
    public ScreenOffUnblocker mPendingScreenOffUnblocker;
    /* access modifiers changed from: private */
    public ScreenOnUnblocker mPendingScreenOnUnblocker;
    private boolean mPendingUpdatePowerStateLocked;
    private boolean mPendingWaitForNegativeProximityLocked;
    private DisplayManagerInternal.DisplayPowerRequest mPowerRequest;
    private DisplayPowerState mPowerState;
    private int mProximity = -1;
    private Sensor mProximitySensor;
    /* access modifiers changed from: private */
    public boolean mProximitySensorEnabled;
    private final SensorEventListener mProximitySensorListener = new SensorEventListener() {
        /* class com.android.server.display.DisplayPowerController.AnonymousClass9 */

        public void onSensorChanged(SensorEvent event) {
            if (DisplayPowerController.this.mProximitySensorEnabled) {
                long time = SystemClock.uptimeMillis();
                boolean positive = false;
                float distance = event.values[0];
                if (distance >= OppoBrightUtils.MIN_LUX_LIMITI && distance < DisplayPowerController.this.mProximityThreshold) {
                    positive = true;
                }
                DisplayPowerController.this.handleProximitySensorEvent(time, positive);
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    /* access modifiers changed from: private */
    public float mProximityThreshold;
    private final OppoRampAnimator.Listener mRampAnimatorListener = new OppoRampAnimator.Listener() {
        /* class com.android.server.display.DisplayPowerController.AnonymousClass3 */

        @Override // com.android.server.display.OppoRampAnimator.Listener
        public void onAnimationEnd() {
            DisplayPowerController.this.sendUpdatePowerState();
            OppoBrightUtils unused = DisplayPowerController.mOppoBrightUtils;
            OppoBrightUtils.mReduceBrightnessAnimating = false;
        }
    };
    public boolean mRegistAppSwitch = true;
    private int mReportedScreenStateToPolicy;
    private int mScreenAnimBightnessRate;
    private int mScreenAnimBightnessTarget;
    private final int mScreenBrightnessDefault;
    private int mScreenBrightnessDimConfig;
    private int mScreenBrightnessDozeConfig;
    private int mScreenBrightnessForVr;
    private final int mScreenBrightnessForVrDefault;
    private final int mScreenBrightnessForVrRangeMaximum;
    private final int mScreenBrightnessForVrRangeMinimum;
    public OppoRampAnimator<DisplayPowerState> mScreenBrightnessRampAnimator;
    private final int mScreenBrightnessRangeMaximum;
    private final int mScreenBrightnessRangeMinimum;
    private boolean mScreenOffBecauseOfProximity;
    private long mScreenOffBlockStartRealTime;
    private long mScreenOnBlockStartRealTime;
    private int mScreenState;
    private final SensorManager mSensorManager;
    private final SettingsObserver mSettingsObserver;
    private int mSkipRampState = 0;
    private final boolean mSkipScreenOnBrightnessRamp;
    private long mSwitchBrightCount;
    public float mTemporaryAutoBrightnessAdjustment;
    /* access modifiers changed from: private */
    public int mTemporaryScreenBrightness;
    private boolean mUnfinishedBusiness;
    private FadeOffDurationRunnable mUpdateFadeOffDurationRunnable = new FadeOffDurationRunnable();
    private boolean mUseSoftwareAutoBrightnessConfig;
    private boolean mWaitingForNegativeProximity;
    private final WindowManagerPolicy mWindowManagerPolicy;
    private boolean mblackGestureWakeUp = false;

    public void registerByNewImpl() {
        Slog.d(TAG, "registerByNewImpl");
        OppoAppScaleHelper oppoAppScaleHelper = mOppoAppScaleHelper;
        if (oppoAppScaleHelper != null) {
            OppoBrightUtils oppoBrightUtils = mOppoBrightUtils;
            OppoBrightUtils.mReduceBrightnessRate = oppoAppScaleHelper.GetReduceBrightnessRate();
            ColorAppSwitchConfig config = new ColorAppSwitchConfig();
            config.addAppConfig(2, mOppoAppScaleHelper.GetReduceBrightnessPackage());
            ColorAppSwitchManager.getInstance().registerAppSwitchObserver(this.mContext, this.mDynamicObserver, config);
            this.mRegistAppSwitch = true;
        }
    }

    public void unregisterByNewImpl() {
        Slog.d(TAG, "unregisterByNewImpl");
        ColorAppSwitchManager.getInstance().unregisterAppSwitchObserver(this.mContext, this.mDynamicObserver);
        this.mRegistAppSwitch = false;
    }

    /* JADX DEBUG: Additional 3 move instruction added to help type inference */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX INFO: Multiple debug info for r13v2 long: [D('screenDarkeningThresholds' int[]), D('brighteningLightDebounce' long)] */
    /* JADX INFO: Multiple debug info for r13v3 long: [D('brighteningLightDebounce' long), D('darkeningLightDebounce' long)] */
    /* JADX WARN: Type inference failed for: r1v15 */
    /* JADX WARN: Type inference failed for: r1v32 */
    /* JADX WARN: Type inference failed for: r1v33 */
    /* JADX WARNING: Removed duplicated region for block: B:65:0x03d4  */
    /* JADX WARNING: Unknown variable types count: 1 */
    public DisplayPowerController(Context context, DisplayManagerInternal.DisplayPowerCallbacks callbacks, Handler handler, SensorManager sensorManager, DisplayBlanker blanker) {
        Resources resources;
        DisplayPowerController displayPowerController;
        boolean z;
        Sensor sensor;
        DisplayWhiteBalanceSettings displayWhiteBalanceSettings;
        DisplayWhiteBalanceController displayWhiteBalanceController;
        DisplayWhiteBalanceController displayWhiteBalanceController2;
        DisplayWhiteBalanceSettings displayWhiteBalanceSettings2;
        int initialLightSensorRate;
        ? r1;
        String str;
        boolean z2;
        int[] ambientDarkeningThresholds;
        this.mHandler = new DisplayControllerHandler(handler.getLooper());
        this.mBrightnessTracker = new BrightnessTracker(context, null);
        this.mSettingsObserver = new SettingsObserver(this.mHandler);
        this.mCallbacks = callbacks;
        this.mBatteryStats = BatteryStatsService.getService();
        this.mSensorManager = sensorManager;
        this.mWindowManagerPolicy = (WindowManagerPolicy) LocalServices.getService(WindowManagerPolicy.class);
        this.mBlanker = blanker;
        this.mContext = context;
        this.mScreenState = -1;
        mQuickDarkToBright = false;
        mOppoBrightUtils = OppoBrightUtils.getInstance();
        this.mLightSensor = this.mSensorManager.getDefaultSensor(5);
        if ("user".equals(SystemProperties.get("ro.build.type", UiModeManagerService.Shell.NIGHT_MODE_STR_UNKNOWN))) {
            DEBUG = false;
        } else {
            OppoBrightUtils oppoBrightUtils = mOppoBrightUtils;
            DEBUG = OppoBrightUtils.DEBUG;
        }
        Resources resources2 = context.getResources();
        clampAbsoluteBrightness(resources2.getInteger(17694889));
        this.mScreenBrightnessDozeConfig = clampAbsoluteBrightness(resources2.getInteger(17694883));
        this.mScreenBrightnessDimConfig = clampAbsoluteBrightness(resources2.getInteger(17694882));
        this.mScreenBrightnessRangeMinimum = mOppoBrightUtils.getMinimumScreenBrightnessSetting();
        if (OppoBrightUtils.mScreenGlobalHBMSupport) {
            this.mScreenBrightnessRangeMaximum = OppoBrightUtils.HBM_EXTEND_MAXBRIGHTNESS;
        } else {
            this.mScreenBrightnessRangeMaximum = PowerManager.BRIGHTNESS_MULTIBITS_ON;
        }
        this.mScreenBrightnessDefault = mOppoBrightUtils.getDefaultScreenBrightnessSetting();
        this.mBrightnessRampRateFast = OppoBrightUtils.BRIGHTNESS_RAMP_RATE_FAST;
        this.mBrightnessRampRateSlow = OppoBrightUtils.BRIGHTNESS_RAMP_RATE_SLOW;
        this.mScreenBrightnessForVrRangeMinimum = clampAbsoluteBrightness(resources2.getInteger(17694886));
        this.mScreenBrightnessForVrRangeMaximum = clampAbsoluteBrightness(resources2.getInteger(17694885));
        this.mScreenBrightnessForVrDefault = clampAbsoluteBrightness(resources2.getInteger(17694884));
        boolean hasSystemFeature = this.mContext.getPackageManager().hasSystemFeature(FingerprintService.OPTICAL_FINGERPRINT_FEATURE);
        String str2 = TAG;
        if (hasSystemFeature) {
            this.mFingerprintOpticalSupport = true;
            Slog.d(str2, "mFingerprintOpticalSupport = " + this.mFingerprintOpticalSupport);
        }
        this.mUseSoftwareAutoBrightnessConfig = resources2.getBoolean(17891367);
        this.mAllowAutoBrightnessWhileDozingConfig = resources2.getBoolean(17891342);
        this.mSkipScreenOnBrightnessRamp = resources2.getBoolean(17891519);
        if (this.mUseSoftwareAutoBrightnessConfig) {
            float dozeScaleFactor = resources2.getFraction(18022406, 1, 1);
            HysteresisLevels ambientBrightnessThresholds = new HysteresisLevels(resources2.getIntArray(17235982), resources2.getIntArray(17235983), resources2.getIntArray(17235984));
            HysteresisLevels screenBrightnessThresholds = new HysteresisLevels(resources2.getIntArray(17236062), resources2.getIntArray(17236065), resources2.getIntArray(17236066));
            long brighteningLightDebounce = (long) resources2.getInteger(17694736);
            long darkeningLightDebounce = (long) resources2.getInteger(17694737);
            boolean autoBrightnessResetAmbientLuxAfterWarmUp = resources2.getBoolean(17891362);
            int lightSensorWarmUpTimeConfig = resources2.getInteger(17694822);
            int lightSensorRate = resources2.getInteger(17694739);
            int initialLightSensorRate2 = resources2.getInteger(17694738);
            if (initialLightSensorRate2 == -1) {
                initialLightSensorRate = lightSensorRate;
            } else {
                if (initialLightSensorRate2 > lightSensorRate) {
                    Slog.w(str2, "Expected config_autoBrightnessInitialLightSensorRate (" + initialLightSensorRate2 + ") to be less than or equal to config_autoBrightnessLightSensorRate (" + lightSensorRate + ").");
                }
                initialLightSensorRate = initialLightSensorRate2;
            }
            int shortTermModelTimeout = resources2.getInteger(17694740);
            try {
                Sensor lightSensor = findDisplayLightSensor(resources2.getString(17039720));
                try {
                    this.mBrightnessMapper = BrightnessMappingStrategy.create(resources2);
                    if (this.mBrightnessMapper != null) {
                        try {
                        } catch (Exception e) {
                            str = str2;
                            resources = resources2;
                            displayPowerController = this;
                            z2 = false;
                            str2 = str;
                            Slog.i(str2, "get light senor faile. set mUseSoftwareAutoBrightnessConfig false.");
                            displayPowerController.mUseSoftwareAutoBrightnessConfig = z2;
                            r1 = z2;
                            displayPowerController.mScreenBrightnessDozeConfig = mOppoBrightUtils.readAutoBrightnessConfig()[r1];
                            displayPowerController.mScreenBrightnessDimConfig = mOppoBrightUtils.readAutoBrightnessConfig()[r1];
                            z = r1;
                            displayPowerController.mColorFadeEnabled = !ActivityManager.isLowRamDeviceStatic();
                            displayPowerController.mColorFadeFadesConfig = resources.getBoolean(17891359);
                            displayPowerController.mDisplayBlanksAfterDozeConfig = resources.getBoolean(17891410);
                            displayPowerController.mBrightnessBucketsInDozeConfig = resources.getBoolean(17891411);
                            displayPowerController.mProximitySensor = displayPowerController.mSensorManager.getDefaultSensor(8);
                            sensor = displayPowerController.mProximitySensor;
                            if (sensor != null) {
                            }
                            displayPowerController.mCurrentScreenBrightnessSetting = getScreenBrightnessSetting();
                            displayPowerController.mScreenBrightnessForVr = getScreenBrightnessForVrSetting();
                            displayPowerController.mAutoBrightnessAdjustment = getAutoBrightnessAdjustmentSetting();
                            displayPowerController.mTemporaryScreenBrightness = -1;
                            displayPowerController.mPendingScreenBrightnessSetting = -1;
                            displayPowerController.mTemporaryAutoBrightnessAdjustment = Float.NaN;
                            displayPowerController.mPendingAutoBrightnessAdjustment = Float.NaN;
                            displayWhiteBalanceSettings = null;
                            displayWhiteBalanceController = null;
                            displayWhiteBalanceSettings = new DisplayWhiteBalanceSettings(displayPowerController.mContext, displayPowerController.mHandler);
                            displayWhiteBalanceController = DisplayWhiteBalanceFactory.create(displayPowerController.mHandler, displayPowerController.mSensorManager, resources);
                            displayWhiteBalanceSettings.setCallbacks(displayPowerController);
                            displayWhiteBalanceController.setCallbacks(displayPowerController);
                            displayWhiteBalanceSettings2 = displayWhiteBalanceSettings;
                            displayWhiteBalanceController2 = displayWhiteBalanceController;
                            displayPowerController.mDisplayWhiteBalanceSettings = displayWhiteBalanceSettings2;
                            displayPowerController.mDisplayWhiteBalanceController = displayWhiteBalanceController2;
                            DEBUG_PANIC = SystemProperties.getBoolean("persist.sys.assert.panic", z);
                            displayPowerController.mOppoDisplayPowerHelper = new OppoDisplayPowerHelper(this, displayPowerController.mContext, displayPowerController.mLock, handler, displayPowerController.mCallbacks);
                            displayPowerController.mOppoDisplayBrightnessHelper = new OppoDisplayPowerControlBrightnessHelper(this, displayPowerController.mAutomaticBrightnessController, displayPowerController.mSensorManager, displayPowerController.mContext, handler);
                            displayPowerController.mBrightnessBucketsInDozeConfig = true;
                            displayPowerController.mLastScreenBrightness = -1;
                            displayPowerController.mSwitchBrightCount = 0;
                            displayPowerController.mLastSwitchBrightReportTime = 0;
                            displayPowerController.mHandler.sendMessage(displayPowerController.mHandler.obtainMessage(2020));
                        }
                        try {
                        } catch (Exception e2) {
                            str = str2;
                            resources = resources2;
                            displayPowerController = this;
                            z2 = false;
                            str2 = str;
                            Slog.i(str2, "get light senor faile. set mUseSoftwareAutoBrightnessConfig false.");
                            displayPowerController.mUseSoftwareAutoBrightnessConfig = z2;
                            r1 = z2;
                            displayPowerController.mScreenBrightnessDozeConfig = mOppoBrightUtils.readAutoBrightnessConfig()[r1];
                            displayPowerController.mScreenBrightnessDimConfig = mOppoBrightUtils.readAutoBrightnessConfig()[r1];
                            z = r1;
                            displayPowerController.mColorFadeEnabled = !ActivityManager.isLowRamDeviceStatic();
                            displayPowerController.mColorFadeFadesConfig = resources.getBoolean(17891359);
                            displayPowerController.mDisplayBlanksAfterDozeConfig = resources.getBoolean(17891410);
                            displayPowerController.mBrightnessBucketsInDozeConfig = resources.getBoolean(17891411);
                            displayPowerController.mProximitySensor = displayPowerController.mSensorManager.getDefaultSensor(8);
                            sensor = displayPowerController.mProximitySensor;
                            if (sensor != null) {
                            }
                            displayPowerController.mCurrentScreenBrightnessSetting = getScreenBrightnessSetting();
                            displayPowerController.mScreenBrightnessForVr = getScreenBrightnessForVrSetting();
                            displayPowerController.mAutoBrightnessAdjustment = getAutoBrightnessAdjustmentSetting();
                            displayPowerController.mTemporaryScreenBrightness = -1;
                            displayPowerController.mPendingScreenBrightnessSetting = -1;
                            displayPowerController.mTemporaryAutoBrightnessAdjustment = Float.NaN;
                            displayPowerController.mPendingAutoBrightnessAdjustment = Float.NaN;
                            displayWhiteBalanceSettings = null;
                            displayWhiteBalanceController = null;
                            displayWhiteBalanceSettings = new DisplayWhiteBalanceSettings(displayPowerController.mContext, displayPowerController.mHandler);
                            displayWhiteBalanceController = DisplayWhiteBalanceFactory.create(displayPowerController.mHandler, displayPowerController.mSensorManager, resources);
                            displayWhiteBalanceSettings.setCallbacks(displayPowerController);
                            displayWhiteBalanceController.setCallbacks(displayPowerController);
                            displayWhiteBalanceSettings2 = displayWhiteBalanceSettings;
                            displayWhiteBalanceController2 = displayWhiteBalanceController;
                            displayPowerController.mDisplayWhiteBalanceSettings = displayWhiteBalanceSettings2;
                            displayPowerController.mDisplayWhiteBalanceController = displayWhiteBalanceController2;
                            DEBUG_PANIC = SystemProperties.getBoolean("persist.sys.assert.panic", z);
                            displayPowerController.mOppoDisplayPowerHelper = new OppoDisplayPowerHelper(this, displayPowerController.mContext, displayPowerController.mLock, handler, displayPowerController.mCallbacks);
                            displayPowerController.mOppoDisplayBrightnessHelper = new OppoDisplayPowerControlBrightnessHelper(this, displayPowerController.mAutomaticBrightnessController, displayPowerController.mSensorManager, displayPowerController.mContext, handler);
                            displayPowerController.mBrightnessBucketsInDozeConfig = true;
                            displayPowerController.mLastScreenBrightness = -1;
                            displayPowerController.mSwitchBrightCount = 0;
                            displayPowerController.mLastSwitchBrightReportTime = 0;
                            displayPowerController.mHandler.sendMessage(displayPowerController.mHandler.obtainMessage(2020));
                        }
                        try {
                            str = str2;
                            resources = resources2;
                        } catch (Exception e3) {
                            str = str2;
                            resources = resources2;
                            displayPowerController = this;
                            z2 = false;
                            str2 = str;
                            Slog.i(str2, "get light senor faile. set mUseSoftwareAutoBrightnessConfig false.");
                            displayPowerController.mUseSoftwareAutoBrightnessConfig = z2;
                            r1 = z2;
                            displayPowerController.mScreenBrightnessDozeConfig = mOppoBrightUtils.readAutoBrightnessConfig()[r1];
                            displayPowerController.mScreenBrightnessDimConfig = mOppoBrightUtils.readAutoBrightnessConfig()[r1];
                            z = r1;
                            displayPowerController.mColorFadeEnabled = !ActivityManager.isLowRamDeviceStatic();
                            displayPowerController.mColorFadeFadesConfig = resources.getBoolean(17891359);
                            displayPowerController.mDisplayBlanksAfterDozeConfig = resources.getBoolean(17891410);
                            displayPowerController.mBrightnessBucketsInDozeConfig = resources.getBoolean(17891411);
                            displayPowerController.mProximitySensor = displayPowerController.mSensorManager.getDefaultSensor(8);
                            sensor = displayPowerController.mProximitySensor;
                            if (sensor != null) {
                            }
                            displayPowerController.mCurrentScreenBrightnessSetting = getScreenBrightnessSetting();
                            displayPowerController.mScreenBrightnessForVr = getScreenBrightnessForVrSetting();
                            displayPowerController.mAutoBrightnessAdjustment = getAutoBrightnessAdjustmentSetting();
                            displayPowerController.mTemporaryScreenBrightness = -1;
                            displayPowerController.mPendingScreenBrightnessSetting = -1;
                            displayPowerController.mTemporaryAutoBrightnessAdjustment = Float.NaN;
                            displayPowerController.mPendingAutoBrightnessAdjustment = Float.NaN;
                            displayWhiteBalanceSettings = null;
                            displayWhiteBalanceController = null;
                            displayWhiteBalanceSettings = new DisplayWhiteBalanceSettings(displayPowerController.mContext, displayPowerController.mHandler);
                            displayWhiteBalanceController = DisplayWhiteBalanceFactory.create(displayPowerController.mHandler, displayPowerController.mSensorManager, resources);
                            displayWhiteBalanceSettings.setCallbacks(displayPowerController);
                            displayWhiteBalanceController.setCallbacks(displayPowerController);
                            displayWhiteBalanceSettings2 = displayWhiteBalanceSettings;
                            displayWhiteBalanceController2 = displayWhiteBalanceController;
                            displayPowerController.mDisplayWhiteBalanceSettings = displayWhiteBalanceSettings2;
                            displayPowerController.mDisplayWhiteBalanceController = displayWhiteBalanceController2;
                            DEBUG_PANIC = SystemProperties.getBoolean("persist.sys.assert.panic", z);
                            displayPowerController.mOppoDisplayPowerHelper = new OppoDisplayPowerHelper(this, displayPowerController.mContext, displayPowerController.mLock, handler, displayPowerController.mCallbacks);
                            displayPowerController.mOppoDisplayBrightnessHelper = new OppoDisplayPowerControlBrightnessHelper(this, displayPowerController.mAutomaticBrightnessController, displayPowerController.mSensorManager, displayPowerController.mContext, handler);
                            displayPowerController.mBrightnessBucketsInDozeConfig = true;
                            displayPowerController.mLastScreenBrightness = -1;
                            displayPowerController.mSwitchBrightCount = 0;
                            displayPowerController.mLastSwitchBrightReportTime = 0;
                            displayPowerController.mHandler.sendMessage(displayPowerController.mHandler.obtainMessage(2020));
                        }
                        try {
                            displayPowerController = this;
                            try {
                                displayPowerController.mAutomaticBrightnessController = new OppoAutomaticBrightnessController(this, handler.getLooper(), sensorManager, lightSensor, this.mBrightnessMapper, lightSensorWarmUpTimeConfig, this.mScreenBrightnessRangeMinimum, this.mScreenBrightnessRangeMaximum, dozeScaleFactor, lightSensorRate, initialLightSensorRate, brighteningLightDebounce, darkeningLightDebounce, autoBrightnessResetAmbientLuxAfterWarmUp, ambientBrightnessThresholds, screenBrightnessThresholds, (long) shortTermModelTimeout, context.getPackageManager());
                                displayPowerController.mAutomaticBrightnessController.init(displayPowerController.mContext);
                                ambientDarkeningThresholds = null;
                            } catch (Exception e4) {
                                z2 = false;
                                str2 = str;
                                Slog.i(str2, "get light senor faile. set mUseSoftwareAutoBrightnessConfig false.");
                                displayPowerController.mUseSoftwareAutoBrightnessConfig = z2;
                                r1 = z2;
                                displayPowerController.mScreenBrightnessDozeConfig = mOppoBrightUtils.readAutoBrightnessConfig()[r1];
                                displayPowerController.mScreenBrightnessDimConfig = mOppoBrightUtils.readAutoBrightnessConfig()[r1];
                                z = r1;
                                displayPowerController.mColorFadeEnabled = !ActivityManager.isLowRamDeviceStatic();
                                displayPowerController.mColorFadeFadesConfig = resources.getBoolean(17891359);
                                displayPowerController.mDisplayBlanksAfterDozeConfig = resources.getBoolean(17891410);
                                displayPowerController.mBrightnessBucketsInDozeConfig = resources.getBoolean(17891411);
                                displayPowerController.mProximitySensor = displayPowerController.mSensorManager.getDefaultSensor(8);
                                sensor = displayPowerController.mProximitySensor;
                                if (sensor != null) {
                                }
                                displayPowerController.mCurrentScreenBrightnessSetting = getScreenBrightnessSetting();
                                displayPowerController.mScreenBrightnessForVr = getScreenBrightnessForVrSetting();
                                displayPowerController.mAutoBrightnessAdjustment = getAutoBrightnessAdjustmentSetting();
                                displayPowerController.mTemporaryScreenBrightness = -1;
                                displayPowerController.mPendingScreenBrightnessSetting = -1;
                                displayPowerController.mTemporaryAutoBrightnessAdjustment = Float.NaN;
                                displayPowerController.mPendingAutoBrightnessAdjustment = Float.NaN;
                                displayWhiteBalanceSettings = null;
                                displayWhiteBalanceController = null;
                                displayWhiteBalanceSettings = new DisplayWhiteBalanceSettings(displayPowerController.mContext, displayPowerController.mHandler);
                                displayWhiteBalanceController = DisplayWhiteBalanceFactory.create(displayPowerController.mHandler, displayPowerController.mSensorManager, resources);
                                displayWhiteBalanceSettings.setCallbacks(displayPowerController);
                                displayWhiteBalanceController.setCallbacks(displayPowerController);
                                displayWhiteBalanceSettings2 = displayWhiteBalanceSettings;
                                displayWhiteBalanceController2 = displayWhiteBalanceController;
                                displayPowerController.mDisplayWhiteBalanceSettings = displayWhiteBalanceSettings2;
                                displayPowerController.mDisplayWhiteBalanceController = displayWhiteBalanceController2;
                                DEBUG_PANIC = SystemProperties.getBoolean("persist.sys.assert.panic", z);
                                displayPowerController.mOppoDisplayPowerHelper = new OppoDisplayPowerHelper(this, displayPowerController.mContext, displayPowerController.mLock, handler, displayPowerController.mCallbacks);
                                displayPowerController.mOppoDisplayBrightnessHelper = new OppoDisplayPowerControlBrightnessHelper(this, displayPowerController.mAutomaticBrightnessController, displayPowerController.mSensorManager, displayPowerController.mContext, handler);
                                displayPowerController.mBrightnessBucketsInDozeConfig = true;
                                displayPowerController.mLastScreenBrightness = -1;
                                displayPowerController.mSwitchBrightCount = 0;
                                displayPowerController.mLastSwitchBrightReportTime = 0;
                                displayPowerController.mHandler.sendMessage(displayPowerController.mHandler.obtainMessage(2020));
                            }
                        } catch (Exception e5) {
                            displayPowerController = this;
                            z2 = false;
                            str2 = str;
                            Slog.i(str2, "get light senor faile. set mUseSoftwareAutoBrightnessConfig false.");
                            displayPowerController.mUseSoftwareAutoBrightnessConfig = z2;
                            r1 = z2;
                            displayPowerController.mScreenBrightnessDozeConfig = mOppoBrightUtils.readAutoBrightnessConfig()[r1];
                            displayPowerController.mScreenBrightnessDimConfig = mOppoBrightUtils.readAutoBrightnessConfig()[r1];
                            z = r1;
                            displayPowerController.mColorFadeEnabled = !ActivityManager.isLowRamDeviceStatic();
                            displayPowerController.mColorFadeFadesConfig = resources.getBoolean(17891359);
                            displayPowerController.mDisplayBlanksAfterDozeConfig = resources.getBoolean(17891410);
                            displayPowerController.mBrightnessBucketsInDozeConfig = resources.getBoolean(17891411);
                            displayPowerController.mProximitySensor = displayPowerController.mSensorManager.getDefaultSensor(8);
                            sensor = displayPowerController.mProximitySensor;
                            if (sensor != null) {
                            }
                            displayPowerController.mCurrentScreenBrightnessSetting = getScreenBrightnessSetting();
                            displayPowerController.mScreenBrightnessForVr = getScreenBrightnessForVrSetting();
                            displayPowerController.mAutoBrightnessAdjustment = getAutoBrightnessAdjustmentSetting();
                            displayPowerController.mTemporaryScreenBrightness = -1;
                            displayPowerController.mPendingScreenBrightnessSetting = -1;
                            displayPowerController.mTemporaryAutoBrightnessAdjustment = Float.NaN;
                            displayPowerController.mPendingAutoBrightnessAdjustment = Float.NaN;
                            displayWhiteBalanceSettings = null;
                            displayWhiteBalanceController = null;
                            displayWhiteBalanceSettings = new DisplayWhiteBalanceSettings(displayPowerController.mContext, displayPowerController.mHandler);
                            displayWhiteBalanceController = DisplayWhiteBalanceFactory.create(displayPowerController.mHandler, displayPowerController.mSensorManager, resources);
                            displayWhiteBalanceSettings.setCallbacks(displayPowerController);
                            displayWhiteBalanceController.setCallbacks(displayPowerController);
                            displayWhiteBalanceSettings2 = displayWhiteBalanceSettings;
                            displayWhiteBalanceController2 = displayWhiteBalanceController;
                            displayPowerController.mDisplayWhiteBalanceSettings = displayWhiteBalanceSettings2;
                            displayPowerController.mDisplayWhiteBalanceController = displayWhiteBalanceController2;
                            DEBUG_PANIC = SystemProperties.getBoolean("persist.sys.assert.panic", z);
                            displayPowerController.mOppoDisplayPowerHelper = new OppoDisplayPowerHelper(this, displayPowerController.mContext, displayPowerController.mLock, handler, displayPowerController.mCallbacks);
                            displayPowerController.mOppoDisplayBrightnessHelper = new OppoDisplayPowerControlBrightnessHelper(this, displayPowerController.mAutomaticBrightnessController, displayPowerController.mSensorManager, displayPowerController.mContext, handler);
                            displayPowerController.mBrightnessBucketsInDozeConfig = true;
                            displayPowerController.mLastScreenBrightness = -1;
                            displayPowerController.mSwitchBrightCount = 0;
                            displayPowerController.mLastSwitchBrightReportTime = 0;
                            displayPowerController.mHandler.sendMessage(displayPowerController.mHandler.obtainMessage(2020));
                        }
                    } else {
                        str = str2;
                        resources = resources2;
                        displayPowerController = this;
                        z2 = false;
                        ambientDarkeningThresholds = null;
                        try {
                            displayPowerController.mUseSoftwareAutoBrightnessConfig = false;
                        } catch (Exception e6) {
                            str2 = str;
                            Slog.i(str2, "get light senor faile. set mUseSoftwareAutoBrightnessConfig false.");
                            displayPowerController.mUseSoftwareAutoBrightnessConfig = z2;
                            r1 = z2;
                            displayPowerController.mScreenBrightnessDozeConfig = mOppoBrightUtils.readAutoBrightnessConfig()[r1];
                            displayPowerController.mScreenBrightnessDimConfig = mOppoBrightUtils.readAutoBrightnessConfig()[r1];
                            z = r1;
                            displayPowerController.mColorFadeEnabled = !ActivityManager.isLowRamDeviceStatic();
                            displayPowerController.mColorFadeFadesConfig = resources.getBoolean(17891359);
                            displayPowerController.mDisplayBlanksAfterDozeConfig = resources.getBoolean(17891410);
                            displayPowerController.mBrightnessBucketsInDozeConfig = resources.getBoolean(17891411);
                            displayPowerController.mProximitySensor = displayPowerController.mSensorManager.getDefaultSensor(8);
                            sensor = displayPowerController.mProximitySensor;
                            if (sensor != null) {
                            }
                            displayPowerController.mCurrentScreenBrightnessSetting = getScreenBrightnessSetting();
                            displayPowerController.mScreenBrightnessForVr = getScreenBrightnessForVrSetting();
                            displayPowerController.mAutoBrightnessAdjustment = getAutoBrightnessAdjustmentSetting();
                            displayPowerController.mTemporaryScreenBrightness = -1;
                            displayPowerController.mPendingScreenBrightnessSetting = -1;
                            displayPowerController.mTemporaryAutoBrightnessAdjustment = Float.NaN;
                            displayPowerController.mPendingAutoBrightnessAdjustment = Float.NaN;
                            displayWhiteBalanceSettings = null;
                            displayWhiteBalanceController = null;
                            displayWhiteBalanceSettings = new DisplayWhiteBalanceSettings(displayPowerController.mContext, displayPowerController.mHandler);
                            displayWhiteBalanceController = DisplayWhiteBalanceFactory.create(displayPowerController.mHandler, displayPowerController.mSensorManager, resources);
                            displayWhiteBalanceSettings.setCallbacks(displayPowerController);
                            displayWhiteBalanceController.setCallbacks(displayPowerController);
                            displayWhiteBalanceSettings2 = displayWhiteBalanceSettings;
                            displayWhiteBalanceController2 = displayWhiteBalanceController;
                            displayPowerController.mDisplayWhiteBalanceSettings = displayWhiteBalanceSettings2;
                            displayPowerController.mDisplayWhiteBalanceController = displayWhiteBalanceController2;
                            DEBUG_PANIC = SystemProperties.getBoolean("persist.sys.assert.panic", z);
                            displayPowerController.mOppoDisplayPowerHelper = new OppoDisplayPowerHelper(this, displayPowerController.mContext, displayPowerController.mLock, handler, displayPowerController.mCallbacks);
                            displayPowerController.mOppoDisplayBrightnessHelper = new OppoDisplayPowerControlBrightnessHelper(this, displayPowerController.mAutomaticBrightnessController, displayPowerController.mSensorManager, displayPowerController.mContext, handler);
                            displayPowerController.mBrightnessBucketsInDozeConfig = true;
                            displayPowerController.mLastScreenBrightness = -1;
                            displayPowerController.mSwitchBrightCount = 0;
                            displayPowerController.mLastSwitchBrightReportTime = 0;
                            displayPowerController.mHandler.sendMessage(displayPowerController.mHandler.obtainMessage(2020));
                        }
                    }
                    str2 = str;
                    r1 = ambientDarkeningThresholds;
                } catch (Exception e7) {
                    str = str2;
                    resources = resources2;
                    displayPowerController = this;
                    z2 = false;
                    str2 = str;
                    Slog.i(str2, "get light senor faile. set mUseSoftwareAutoBrightnessConfig false.");
                    displayPowerController.mUseSoftwareAutoBrightnessConfig = z2;
                    r1 = z2;
                    displayPowerController.mScreenBrightnessDozeConfig = mOppoBrightUtils.readAutoBrightnessConfig()[r1];
                    displayPowerController.mScreenBrightnessDimConfig = mOppoBrightUtils.readAutoBrightnessConfig()[r1];
                    z = r1;
                    displayPowerController.mColorFadeEnabled = !ActivityManager.isLowRamDeviceStatic();
                    displayPowerController.mColorFadeFadesConfig = resources.getBoolean(17891359);
                    displayPowerController.mDisplayBlanksAfterDozeConfig = resources.getBoolean(17891410);
                    displayPowerController.mBrightnessBucketsInDozeConfig = resources.getBoolean(17891411);
                    displayPowerController.mProximitySensor = displayPowerController.mSensorManager.getDefaultSensor(8);
                    sensor = displayPowerController.mProximitySensor;
                    if (sensor != null) {
                    }
                    displayPowerController.mCurrentScreenBrightnessSetting = getScreenBrightnessSetting();
                    displayPowerController.mScreenBrightnessForVr = getScreenBrightnessForVrSetting();
                    displayPowerController.mAutoBrightnessAdjustment = getAutoBrightnessAdjustmentSetting();
                    displayPowerController.mTemporaryScreenBrightness = -1;
                    displayPowerController.mPendingScreenBrightnessSetting = -1;
                    displayPowerController.mTemporaryAutoBrightnessAdjustment = Float.NaN;
                    displayPowerController.mPendingAutoBrightnessAdjustment = Float.NaN;
                    displayWhiteBalanceSettings = null;
                    displayWhiteBalanceController = null;
                    displayWhiteBalanceSettings = new DisplayWhiteBalanceSettings(displayPowerController.mContext, displayPowerController.mHandler);
                    displayWhiteBalanceController = DisplayWhiteBalanceFactory.create(displayPowerController.mHandler, displayPowerController.mSensorManager, resources);
                    displayWhiteBalanceSettings.setCallbacks(displayPowerController);
                    displayWhiteBalanceController.setCallbacks(displayPowerController);
                    displayWhiteBalanceSettings2 = displayWhiteBalanceSettings;
                    displayWhiteBalanceController2 = displayWhiteBalanceController;
                    displayPowerController.mDisplayWhiteBalanceSettings = displayWhiteBalanceSettings2;
                    displayPowerController.mDisplayWhiteBalanceController = displayWhiteBalanceController2;
                    DEBUG_PANIC = SystemProperties.getBoolean("persist.sys.assert.panic", z);
                    displayPowerController.mOppoDisplayPowerHelper = new OppoDisplayPowerHelper(this, displayPowerController.mContext, displayPowerController.mLock, handler, displayPowerController.mCallbacks);
                    displayPowerController.mOppoDisplayBrightnessHelper = new OppoDisplayPowerControlBrightnessHelper(this, displayPowerController.mAutomaticBrightnessController, displayPowerController.mSensorManager, displayPowerController.mContext, handler);
                    displayPowerController.mBrightnessBucketsInDozeConfig = true;
                    displayPowerController.mLastScreenBrightness = -1;
                    displayPowerController.mSwitchBrightCount = 0;
                    displayPowerController.mLastSwitchBrightReportTime = 0;
                    displayPowerController.mHandler.sendMessage(displayPowerController.mHandler.obtainMessage(2020));
                }
            } catch (Exception e8) {
                str = str2;
                resources = resources2;
                displayPowerController = this;
                z2 = false;
                str2 = str;
                Slog.i(str2, "get light senor faile. set mUseSoftwareAutoBrightnessConfig false.");
                displayPowerController.mUseSoftwareAutoBrightnessConfig = z2;
                r1 = z2;
                displayPowerController.mScreenBrightnessDozeConfig = mOppoBrightUtils.readAutoBrightnessConfig()[r1];
                displayPowerController.mScreenBrightnessDimConfig = mOppoBrightUtils.readAutoBrightnessConfig()[r1];
                z = r1;
                displayPowerController.mColorFadeEnabled = !ActivityManager.isLowRamDeviceStatic();
                displayPowerController.mColorFadeFadesConfig = resources.getBoolean(17891359);
                displayPowerController.mDisplayBlanksAfterDozeConfig = resources.getBoolean(17891410);
                displayPowerController.mBrightnessBucketsInDozeConfig = resources.getBoolean(17891411);
                displayPowerController.mProximitySensor = displayPowerController.mSensorManager.getDefaultSensor(8);
                sensor = displayPowerController.mProximitySensor;
                if (sensor != null) {
                }
                displayPowerController.mCurrentScreenBrightnessSetting = getScreenBrightnessSetting();
                displayPowerController.mScreenBrightnessForVr = getScreenBrightnessForVrSetting();
                displayPowerController.mAutoBrightnessAdjustment = getAutoBrightnessAdjustmentSetting();
                displayPowerController.mTemporaryScreenBrightness = -1;
                displayPowerController.mPendingScreenBrightnessSetting = -1;
                displayPowerController.mTemporaryAutoBrightnessAdjustment = Float.NaN;
                displayPowerController.mPendingAutoBrightnessAdjustment = Float.NaN;
                displayWhiteBalanceSettings = null;
                displayWhiteBalanceController = null;
                displayWhiteBalanceSettings = new DisplayWhiteBalanceSettings(displayPowerController.mContext, displayPowerController.mHandler);
                displayWhiteBalanceController = DisplayWhiteBalanceFactory.create(displayPowerController.mHandler, displayPowerController.mSensorManager, resources);
                displayWhiteBalanceSettings.setCallbacks(displayPowerController);
                displayWhiteBalanceController.setCallbacks(displayPowerController);
                displayWhiteBalanceSettings2 = displayWhiteBalanceSettings;
                displayWhiteBalanceController2 = displayWhiteBalanceController;
                displayPowerController.mDisplayWhiteBalanceSettings = displayWhiteBalanceSettings2;
                displayPowerController.mDisplayWhiteBalanceController = displayWhiteBalanceController2;
                DEBUG_PANIC = SystemProperties.getBoolean("persist.sys.assert.panic", z);
                displayPowerController.mOppoDisplayPowerHelper = new OppoDisplayPowerHelper(this, displayPowerController.mContext, displayPowerController.mLock, handler, displayPowerController.mCallbacks);
                displayPowerController.mOppoDisplayBrightnessHelper = new OppoDisplayPowerControlBrightnessHelper(this, displayPowerController.mAutomaticBrightnessController, displayPowerController.mSensorManager, displayPowerController.mContext, handler);
                displayPowerController.mBrightnessBucketsInDozeConfig = true;
                displayPowerController.mLastScreenBrightness = -1;
                displayPowerController.mSwitchBrightCount = 0;
                displayPowerController.mLastSwitchBrightReportTime = 0;
                displayPowerController.mHandler.sendMessage(displayPowerController.mHandler.obtainMessage(2020));
            }
            displayPowerController.mScreenBrightnessDozeConfig = mOppoBrightUtils.readAutoBrightnessConfig()[r1];
            displayPowerController.mScreenBrightnessDimConfig = mOppoBrightUtils.readAutoBrightnessConfig()[r1];
            z = r1;
        } else {
            resources = resources2;
            z = false;
            displayPowerController = this;
        }
        displayPowerController.mColorFadeEnabled = !ActivityManager.isLowRamDeviceStatic();
        displayPowerController.mColorFadeFadesConfig = resources.getBoolean(17891359);
        displayPowerController.mDisplayBlanksAfterDozeConfig = resources.getBoolean(17891410);
        displayPowerController.mBrightnessBucketsInDozeConfig = resources.getBoolean(17891411);
        displayPowerController.mProximitySensor = displayPowerController.mSensorManager.getDefaultSensor(8);
        sensor = displayPowerController.mProximitySensor;
        if (sensor != null) {
            displayPowerController.mProximityThreshold = Math.min(sensor.getMaximumRange(), (float) TYPICAL_PROXIMITY_THRESHOLD);
        }
        displayPowerController.mCurrentScreenBrightnessSetting = getScreenBrightnessSetting();
        displayPowerController.mScreenBrightnessForVr = getScreenBrightnessForVrSetting();
        displayPowerController.mAutoBrightnessAdjustment = getAutoBrightnessAdjustmentSetting();
        displayPowerController.mTemporaryScreenBrightness = -1;
        displayPowerController.mPendingScreenBrightnessSetting = -1;
        displayPowerController.mTemporaryAutoBrightnessAdjustment = Float.NaN;
        displayPowerController.mPendingAutoBrightnessAdjustment = Float.NaN;
        displayWhiteBalanceSettings = null;
        displayWhiteBalanceController = null;
        try {
            displayWhiteBalanceSettings = new DisplayWhiteBalanceSettings(displayPowerController.mContext, displayPowerController.mHandler);
            displayWhiteBalanceController = DisplayWhiteBalanceFactory.create(displayPowerController.mHandler, displayPowerController.mSensorManager, resources);
            displayWhiteBalanceSettings.setCallbacks(displayPowerController);
            displayWhiteBalanceController.setCallbacks(displayPowerController);
            displayWhiteBalanceSettings2 = displayWhiteBalanceSettings;
            displayWhiteBalanceController2 = displayWhiteBalanceController;
        } catch (Exception e9) {
            Slog.e(str2, "failed to set up display white-balance: " + e9);
            displayWhiteBalanceSettings2 = displayWhiteBalanceSettings;
            displayWhiteBalanceController2 = displayWhiteBalanceController;
        }
        displayPowerController.mDisplayWhiteBalanceSettings = displayWhiteBalanceSettings2;
        displayPowerController.mDisplayWhiteBalanceController = displayWhiteBalanceController2;
        DEBUG_PANIC = SystemProperties.getBoolean("persist.sys.assert.panic", z);
        displayPowerController.mOppoDisplayPowerHelper = new OppoDisplayPowerHelper(this, displayPowerController.mContext, displayPowerController.mLock, handler, displayPowerController.mCallbacks);
        displayPowerController.mOppoDisplayBrightnessHelper = new OppoDisplayPowerControlBrightnessHelper(this, displayPowerController.mAutomaticBrightnessController, displayPowerController.mSensorManager, displayPowerController.mContext, handler);
        displayPowerController.mBrightnessBucketsInDozeConfig = true;
        displayPowerController.mLastScreenBrightness = -1;
        displayPowerController.mSwitchBrightCount = 0;
        displayPowerController.mLastSwitchBrightReportTime = 0;
        displayPowerController.mHandler.sendMessage(displayPowerController.mHandler.obtainMessage(2020));
    }

    private Sensor findDisplayLightSensor(String sensorType) {
        if (!TextUtils.isEmpty(sensorType)) {
            List<Sensor> sensors = this.mSensorManager.getSensorList(-1);
            for (int i = 0; i < sensors.size(); i++) {
                Sensor sensor = sensors.get(i);
                if (sensorType.equals(sensor.getStringType())) {
                    return sensor;
                }
            }
        }
        return this.mSensorManager.getDefaultSensor(5);
    }

    public boolean isProximitySensorAvailable() {
        return this.mProximitySensor != null;
    }

    public void updateFadeOffDuration(long duration) {
        if (!this.mColorFadeOffAnimator.isStarted()) {
            Slog.d(TAG, " update fade off duration " + duration);
            this.mHandler.removeCallbacks(this.mUpdateFadeOffDurationRunnable);
            this.mColorFadeOffAnimator.setDuration(duration < 0 ? 100 : duration);
            return;
        }
        Slog.d(TAG, " update fade off duration failed for animation is running");
        this.mHandler.removeCallbacks(this.mUpdateFadeOffDurationRunnable);
        this.mUpdateFadeOffDurationRunnable.setDuration(duration);
        this.mHandler.postDelayed(this.mUpdateFadeOffDurationRunnable, 1000);
    }

    private class FadeOffDurationRunnable implements Runnable {
        private long duration;

        private FadeOffDurationRunnable() {
            this.duration = 0;
        }

        public void setDuration(long value) {
            this.duration = value;
        }

        public void run() {
            DisplayPowerController.this.updateFadeOffDuration(this.duration);
        }
    }

    public ParceledListSlice<BrightnessChangeEvent> getBrightnessEvents(int userId, boolean includePackage) {
        return this.mBrightnessTracker.getEvents(userId, includePackage);
    }

    public void onSwitchUser(int newUserId) {
        int brightness = Settings.System.getIntForBrightness(this.mContext.getContentResolver(), "screen_brightness", -1, -2);
        float mManualBrightnessBackup = Settings.System.getFloatForUser(this.mContext.getContentResolver(), "screen_auto_brightness_adj", OppoBrightUtils.MIN_LUX_LIMITI, -2);
        if (brightness == -1 && ((int) mManualBrightnessBackup) == 0) {
            Settings.System.putIntForBrightness(this.mContext.getContentResolver(), "screen_brightness", this.mCurrentScreenBrightnessSetting, -2);
        }
        if (((int) mManualBrightnessBackup) != 0) {
            Settings.System.putIntForBrightness(this.mContext.getContentResolver(), "screen_brightness", (int) mManualBrightnessBackup, -2);
        }
        handleSettingsChange(true);
        this.mBrightnessTracker.onSwitchUser(newUserId);
    }

    public ParceledListSlice<AmbientBrightnessDayStats> getAmbientBrightnessStats(int userId) {
        return this.mBrightnessTracker.getAmbientBrightnessStats(userId);
    }

    public void persistBrightnessTrackerState() {
        this.mBrightnessTracker.persistBrightnessTrackerState();
    }

    public boolean requestPowerState(DisplayManagerInternal.DisplayPowerRequest request, boolean waitForNegativeProximity) {
        boolean z;
        if (DEBUG) {
            Slog.d(TAG, "requestPowerState: " + request + ", waitForNegativeProximity=" + waitForNegativeProximity);
        }
        synchronized (this.mLock) {
            boolean changed = false;
            if (waitForNegativeProximity) {
                if (!this.mPendingWaitForNegativeProximityLocked) {
                    this.mPendingWaitForNegativeProximityLocked = true;
                    changed = true;
                }
            }
            if (this.mPendingRequestLocked == null) {
                this.mPendingRequestLocked = new DisplayManagerInternal.DisplayPowerRequest(request);
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
            if ((MTK_DEBUG && changed) || DEBUG) {
                Slog.d(TAG, "requestPowerState: " + request + ", waitForNegativeProximity=" + waitForNegativeProximity + ", changed=" + changed);
            }
            z = this.mDisplayReadyLocked;
        }
        return z;
    }

    public BrightnessConfiguration getDefaultBrightnessConfiguration() {
        OppoAutomaticBrightnessController oppoAutomaticBrightnessController = this.mAutomaticBrightnessController;
        if (oppoAutomaticBrightnessController == null) {
            return null;
        }
        return oppoAutomaticBrightnessController.getDefaultConfig();
    }

    /* access modifiers changed from: private */
    public void sendUpdatePowerState() {
        synchronized (this.mLock) {
            sendUpdatePowerStateLocked();
        }
    }

    private void sendUpdatePowerStateLocked() {
        if (!this.mPendingUpdatePowerStateLocked) {
            this.mPendingUpdatePowerStateLocked = true;
            this.mHandler.sendMessage(this.mHandler.obtainMessage(1));
        }
    }

    private void initialize() {
        this.mPowerState = new DisplayPowerState(this.mBlanker, this.mColorFadeEnabled ? new ColorFade(0) : null, this.mContext);
        if (this.mColorFadeEnabled) {
            this.mColorFadeOnAnimator = ObjectAnimator.ofFloat(this.mPowerState, DisplayPowerState.COLOR_FADE_LEVEL, OppoBrightUtils.MIN_LUX_LIMITI, 1.0f);
            this.mColorFadeOnAnimator.setDuration(250L);
            this.mColorFadeOnAnimator.addListener(this.mAnimatorListener);
            this.mColorFadeOffAnimator = ObjectAnimator.ofFloat(this.mPowerState, DisplayPowerState.COLOR_FADE_LEVEL, 1.0f, OppoBrightUtils.MIN_LUX_LIMITI);
            this.mColorFadeOffAnimator.setDuration(100L);
            this.mColorFadeOffAnimator.addListener(this.mAnimatorListener);
        }
        this.mScreenBrightnessRampAnimator = new OppoRampAnimator<>(this.mPowerState, DisplayPowerState.SCREEN_BRIGHTNESS);
        this.mScreenBrightnessRampAnimator.setListener(this.mRampAnimatorListener);
        try {
            this.mBatteryStats.noteScreenState(this.mPowerState.getScreenState());
            this.mBatteryStats.noteScreenBrightness(this.mPowerState.getScreenBrightness());
        } catch (RemoteException e) {
        }
        float brightness = convertToNits(this.mPowerState.getScreenBrightness());
        if (brightness >= OppoBrightUtils.MIN_LUX_LIMITI) {
            this.mBrightnessTracker.start(brightness);
        }
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("screen_brightness"), false, this.mSettingsObserver, -1);
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("screen_brightness_for_vr"), false, this.mSettingsObserver, -1);
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("screen_auto_brightness_adj"), false, this.mSettingsObserver, -1);
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor(OppoBrightUtils.GLOBAL_HBM_SELL_MODE), false, this.mSettingsObserver, -1);
    }

    /* JADX INFO: Multiple debug info for r3v77 boolean: [D('brightnessAdjustmentFlags' int), D('userInitiatedChange' boolean)] */
    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:100:0x01b2, code lost:
        r14 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:101:0x01b4, code lost:
        r14 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:102:0x01b5, code lost:
        r15 = updateUserSetScreenBrightness();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:103:0x01bd, code lost:
        if (r27.mTemporaryScreenBrightness <= 0) goto L_0x01d1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:105:0x01c1, code lost:
        if (r27.mAppliedScreenBrightnessOverride != false) goto L_0x01d1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:106:0x01c3, code lost:
        r4 = r27.mTemporaryScreenBrightness;
        r27.mAppliedTemporaryBrightness = true;
        r27.mBrightnessReasonTemp.setReason(8);
        r27.mBrightnessSource = 4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:107:0x01d1, code lost:
        r27.mAppliedTemporaryBrightness = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:108:0x01d3, code lost:
        r8 = updateAutoBrightnessAdjustment();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:109:0x01d7, code lost:
        if (r8 == false) goto L_0x01dd;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:110:0x01d9, code lost:
        r27.mTemporaryAutoBrightnessAdjustment = Float.NaN;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:112:0x01e3, code lost:
        if (java.lang.Float.isNaN(r27.mTemporaryAutoBrightnessAdjustment) != false) goto L_0x01ec;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:113:0x01e5, code lost:
        r13 = r27.mTemporaryAutoBrightnessAdjustment;
        r3 = 1;
        r27.mAppliedTemporaryAutoBrightnessAdjustment = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:114:0x01ec, code lost:
        r13 = r27.mAutoBrightnessAdjustment;
        r3 = 2;
        r27.mAppliedTemporaryAutoBrightnessAdjustment = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:116:0x01f5, code lost:
        if (r13 <= com.android.server.display.OppoBrightUtils.MIN_LUX_LIMITI) goto L_0x0213;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:118:0x01fd, code lost:
        if (com.android.server.display.DisplayPowerController.mOppoBrightUtils.isSpecialAdj(r13) != false) goto L_0x0210;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:119:0x01ff, code lost:
        r13 = java.lang.Math.max(java.lang.Math.min(r13, (float) r27.mScreenBrightnessRangeMaximum), (float) r27.mScreenBrightnessRangeMinimum);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:123:0x021b, code lost:
        if (r27.mPowerRequest.boostScreenBrightness == false) goto L_0x022d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:124:0x021d, code lost:
        if (r4 == 0) goto L_0x022d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:125:0x021f, code lost:
        r4 = android.os.PowerManager.BRIGHTNESS_MULTIBITS_ON;
        r27.mBrightnessSource = 5;
        r27.mBrightnessReasonTemp.setReason(9);
        r27.mAppliedBrightnessBoost = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:126:0x022d, code lost:
        r27.mAppliedBrightnessBoost = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:127:0x0230, code lost:
        if (r4 >= 0) goto L_0x0238;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:128:0x0232, code lost:
        if (r8 != false) goto L_0x0236;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:129:0x0234, code lost:
        if (r15 == false) goto L_0x0238;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:130:0x0236, code lost:
        r0 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:131:0x0238, code lost:
        r0 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:132:0x0239, code lost:
        r0 = r27.mAutomaticBrightnessController;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:133:0x023f, code lost:
        if (r0 == null) goto L_0x024f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:135:0x0242, code lost:
        if (r9 == 2) goto L_0x0246;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:136:0x0244, code lost:
        r2 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:137:0x0246, code lost:
        r2 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:138:0x0247, code lost:
        r25 = r3;
        r3 = r0;
        r0.configure(r14, r13, r2, r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:139:0x024f, code lost:
        r25 = r3;
        r3 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:141:0x0254, code lost:
        if (r4 >= 0) goto L_0x02cb;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:142:0x0256, code lost:
        r2 = r13;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:143:0x0257, code lost:
        if (r14 == false) goto L_0x0268;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:144:0x0259, code lost:
        r22 = false;
        r4 = r27.mAutomaticBrightnessController.getAutomaticScreenBrightness();
        r2 = r27.mAutomaticBrightnessController.getAutomaticScreenBrightnessAdjustment();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:145:0x0268, code lost:
        r22 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:146:0x026a, code lost:
        if (r4 < 0) goto L_0x02bb;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:147:0x026c, code lost:
        r0 = clampScreenBrightness(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:148:0x0272, code lost:
        if (r27.mAppliedAutoBrightness == false) goto L_0x0278;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:149:0x0274, code lost:
        if (r8 != false) goto L_0x0278;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:150:0x0276, code lost:
        r4 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:151:0x0278, code lost:
        r4 = r22;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:152:0x027a, code lost:
        r22 = com.android.server.display.DisplayPowerController.mOppoBrightUtils;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:153:0x0283, code lost:
        if (com.android.server.display.OppoBrightUtils.mBrightnessBoost != 2) goto L_0x0287;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:154:0x0285, code lost:
        r4 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:155:0x0287, code lost:
        r4 = r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:157:0x028b, code lost:
        if (r27.mAppliedTemporaryBrightness != false) goto L_0x0294;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:159:0x028f, code lost:
        if (r27.mAppliedTemporaryAutoBrightnessAdjustment == false) goto L_0x0292;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:160:0x0292, code lost:
        r3 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:161:0x0294, code lost:
        r3 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:162:0x0295, code lost:
        r4 = r27.mCurrentScreenBrightnessSetting;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:163:0x0299, code lost:
        if (r0 == r4) goto L_0x02a2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:164:0x029b, code lost:
        if (r3 != false) goto L_0x02a2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:165:0x029d, code lost:
        if (r4 <= 0) goto L_0x02a2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:166:0x029f, code lost:
        r27.mCtsBrightness = r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:167:0x02a2, code lost:
        r27.mCtsBrightness = -1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:168:0x02a5, code lost:
        com.android.server.display.OppoBrightUtils.sHbmAutoBrightness = true;
        r27.mBrightnessSource = 6;
        r27.mAppliedAutoBrightness = true;
        r27.mBrightnessReasonTemp.setReason(4);
        r4 = r0;
        r0 = r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:169:0x02bb, code lost:
        r27.mAppliedAutoBrightness = false;
        r0 = r22;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:171:0x02c4, code lost:
        if (r13 == r2) goto L_0x02c9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:172:0x02c6, code lost:
        r3 = r25;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:173:0x02c9, code lost:
        r3 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:174:0x02cb, code lost:
        r27.mAppliedAutoBrightness = false;
        r3 = 0;
        r0 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:175:0x02d5, code lost:
        r16 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:176:0x02db, code lost:
        if (com.android.server.display.DisplayPowerController.mOppoBrightUtils.isAIBrightnessFeatureOpen == false) goto L_0x0302;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:177:0x02dd, code lost:
        if (r4 >= 0) goto L_0x0302;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:179:0x02e3, code lost:
        if (r27.mCurrentScreenBrightnessSetting < com.android.server.display.OppoBrightUtils.mMaxBrightness) goto L_0x0302;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:180:0x02e5, code lost:
        r0 = com.android.server.display.DisplayPowerController.mOppoBrightUtils.mAIBrightness;
        r2 = com.android.server.display.DisplayPowerController.mOppoBrightUtils;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:181:0x02ed, code lost:
        if (r0 < com.android.server.display.OppoBrightUtils.mMaxBrightness) goto L_0x02f4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:182:0x02ef, code lost:
        r4 = clampScreenBrightness(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:183:0x02f4, code lost:
        r27.mBrightnessSource = 99999;
        r27.mBrightnessReasonTemp.setReason(10);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:184:0x0302, code lost:
        if (r4 >= 0) goto L_0x0320;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:186:0x0308, code lost:
        if (android.view.Display.isDozeState(r9) == false) goto L_0x0320;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:187:0x030a, code lost:
        r4 = 0;
        r27.mBrightnessSource = 7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:188:0x0310, code lost:
        if (com.android.server.display.DisplayPowerController.DEBUG == false) goto L_0x031a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:189:0x0312, code lost:
        android.util.Slog.i(com.android.server.display.DisplayPowerController.TAG, "mDozeBrightnessConfig = 0");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:190:0x031a, code lost:
        r27.mBrightnessReasonTemp.setReason(3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:191:0x0320, code lost:
        if (r4 >= 0) goto L_0x0340;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:193:0x0324, code lost:
        if (com.android.server.display.DisplayPowerController.DEBUG == false) goto L_0x032d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:194:0x0326, code lost:
        android.util.Slog.i(com.android.server.display.DisplayPowerController.TAG, "brightness < 0, need to set slowChange = false");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:195:0x032d, code lost:
        r4 = clampScreenBrightness(r27.mCurrentScreenBrightnessSetting);
        r16 = false;
        r27.mBrightnessReasonTemp.setReason(1);
        r27.mBrightnessSource = 8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:197:0x0345, code lost:
        if (r27.mPowerRequest.policy != 2) goto L_0x037d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:199:0x0349, code lost:
        if (r4 <= r27.mScreenBrightnessRangeMinimum) goto L_0x036b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:201:0x034d, code lost:
        if ((r4 - 10) <= 0) goto L_0x0352;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:202:0x034f, code lost:
        r4 = r4 - 10;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:203:0x0352, code lost:
        r4 = r4 / 2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:204:0x0354, code lost:
        r0 = java.lang.Math.max(java.lang.Math.min(r4, r27.mScreenBrightnessDimConfig), r27.mScreenBrightnessRangeMinimum);
        r27.mBrightnessReasonTemp.addModifier(1);
        r27.mBrightnessSource = 9;
        r4 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:206:0x036d, code lost:
        if (r27.mAppliedDimming != false) goto L_0x0372;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:207:0x036f, code lost:
        r16 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:208:0x0372, code lost:
        r27.mAppliedDimming = true;
        com.android.server.display.DisplayPowerController.mScreenDimQuicklyDark = true;
        r0 = com.android.server.display.DisplayPowerController.mOppoBrightUtils;
        com.android.server.display.OppoBrightUtils.mManualSetAutoBrightness = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:210:0x0380, code lost:
        if (r27.mAppliedDimming == false) goto L_0x0386;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:211:0x0382, code lost:
        r16 = false;
        r27.mAppliedDimming = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:213:0x038a, code lost:
        if (r27.mPowerRequest.lowPowerMode == false) goto L_0x03b1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:214:0x038c, code lost:
        r0 = r27.mScreenBrightnessRangeMinimum;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:215:0x038e, code lost:
        if (r4 <= r0) goto L_0x03a1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:216:0x0390, code lost:
        r0 = java.lang.Math.max(r4 / 2, r0);
        r27.mBrightnessReasonTemp.addModifier(2);
        r27.mBrightnessSource = 10;
        r4 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:218:0x03a3, code lost:
        if (r27.mAppliedLowPower != false) goto L_0x03ad;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:219:0x03a5, code lost:
        r16 = false;
        r0 = com.android.server.display.DisplayPowerController.mOppoBrightUtils;
        r2 = true;
        com.android.server.display.OppoBrightUtils.mReduceBrightnessAnimating = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0058, code lost:
        if (r2 == false) goto L_0x005d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:220:0x03ad, code lost:
        r2 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:221:0x03ae, code lost:
        r27.mAppliedLowPower = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:223:0x03b4, code lost:
        if (r27.mAppliedLowPower == false) goto L_0x03bf;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:224:0x03b6, code lost:
        r16 = false;
        r27.mAppliedLowPower = false;
        r0 = com.android.server.display.DisplayPowerController.mOppoBrightUtils;
        com.android.server.display.OppoBrightUtils.mReduceBrightnessAnimating = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:225:0x03bf, code lost:
        r0 = com.android.server.display.DisplayPowerController.mOppoBrightUtils;
        com.android.server.display.OppoBrightUtils.mAppliedLowPower = r27.mAppliedLowPower;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:226:0x03c7, code lost:
        if (com.android.server.display.DisplayPowerController.DEBUG_PANIC == false) goto L_0x03f6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:227:0x03c9, code lost:
        android.util.Slog.d(com.android.server.display.DisplayPowerController.TAG, "updatePowerState: state = " + r9 + ", brightness = " + r4 + ", mColorFadeOffAnimator.isStarted() : " + r27.mColorFadeOffAnimator.isStarted());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:229:0x03f8, code lost:
        if (r27.mPendingScreenOff != false) goto L_0x0631;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x005a, code lost:
        initialize();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:231:0x03fc, code lost:
        if (r27.mSkipScreenOnBrightnessRamp == false) goto L_0x042e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:233:0x03ff, code lost:
        if (r9 != 2) goto L_0x042b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:235:0x0403, code lost:
        if (r27.mSkipRampState != 0) goto L_0x040f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:237:0x0407, code lost:
        if (r27.mDozing == false) goto L_0x040f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:238:0x0409, code lost:
        r27.mInitialAutoBrightness = r4;
        r27.mSkipRampState = 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:240:0x0412, code lost:
        if (r27.mSkipRampState != 1) goto L_0x0420;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:242:0x0416, code lost:
        if (r27.mUseSoftwareAutoBrightnessConfig == false) goto L_0x0420;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:244:0x041a, code lost:
        if (r4 == r27.mInitialAutoBrightness) goto L_0x0420;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:245:0x041c, code lost:
        r27.mSkipRampState = 2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:247:0x0423, code lost:
        if (r27.mSkipRampState != 2) goto L_0x0429;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:248:0x0425, code lost:
        r27.mSkipRampState = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0063, code lost:
        if (r27.mPowerRequest.policy != 2) goto L_0x006b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:250:0x042b, code lost:
        r27.mSkipRampState = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:252:0x042f, code lost:
        if (r9 == 5) goto L_0x0436;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:253:0x0431, code lost:
        if (r7 != 5) goto L_0x0434;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:254:0x0434, code lost:
        r0 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:255:0x0436, code lost:
        r0 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:257:0x0438, code lost:
        if (r9 != 2) goto L_0x043e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:259:0x043c, code lost:
        if (r27.mSkipRampState == 0) goto L_0x0445;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:261:0x043f, code lost:
        if (r9 != 3) goto L_0x0628;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:263:0x0443, code lost:
        if (r27.mBrightnessBucketsInDozeConfig != false) goto L_0x0628;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:264:0x0445, code lost:
        if (r0 != false) goto L_0x0628;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:265:0x0447, code lost:
        r2 = com.android.server.display.OppoBrightUtils.BRIGHTNESS_RAMP_RATE_SLOW;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:266:0x0449, code lost:
        if (r14 == false) goto L_0x0471;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:267:0x044b, code lost:
        r2 = r27.mAutomaticBrightnessController.mAutoRate;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:268:0x0453, code lost:
        if (com.android.server.display.DisplayPowerController.DEBUG == false) goto L_0x046e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:269:0x0455, code lost:
        android.util.Slog.i(com.android.server.display.DisplayPowerController.TAG, "Auto brightness ---001--- rate = " + r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0067, code lost:
        if (r27.mScreenState != 3) goto L_0x006b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:271:0x0471, code lost:
        r0 = r27.mAutomaticBrightnessController;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:272:0x0477, code lost:
        if (r0 == null) goto L_0x047c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:273:0x0479, code lost:
        r0.mScreenAutoBrightness = r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:274:0x047c, code lost:
        android.util.Slog.d(com.android.server.display.DisplayPowerController.TAG, "mAutomaticBrightnessController is null");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:275:0x0484, code lost:
        if (r16 == false) goto L_0x0493;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:276:0x0486, code lost:
        r0 = com.android.server.display.DisplayPowerController.mOppoBrightUtils;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:277:0x048b, code lost:
        if (com.android.server.display.OppoBrightUtils.mBrightnessBoost == 2) goto L_0x0493;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:278:0x048d, code lost:
        r0 = com.android.server.display.DisplayPowerController.mOppoBrightUtils;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:279:0x0491, code lost:
        if (com.android.server.display.OppoBrightUtils.mShouldFastRate == false) goto L_0x04af;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0069, code lost:
        com.android.server.display.DisplayPowerController.mQuickDarkToBright = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:280:0x0493, code lost:
        r2 = com.android.server.display.OppoBrightUtils.BRIGHTNESS_RAMP_RATE_FAST;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:281:0x0497, code lost:
        if (com.android.server.display.DisplayPowerController.DEBUG == false) goto L_0x04af;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:282:0x0499, code lost:
        android.util.Slog.i(com.android.server.display.DisplayPowerController.TAG, "Auto brightness ---002--- rate = " + r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:283:0x04af, code lost:
        r0 = com.android.server.display.DisplayPowerController.mOppoBrightUtils;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:284:0x04b4, code lost:
        if (com.android.server.display.OppoBrightUtils.mBrightnessBoost != 3) goto L_0x04eb;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:285:0x04b6, code lost:
        r2 = com.android.server.display.OppoBrightUtils.BRIGHTNESS_RAMP_RATE_FAST;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:286:0x04ba, code lost:
        if (com.android.server.display.DisplayPowerController.DEBUG == false) goto L_0x04d2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:287:0x04bc, code lost:
        android.util.Slog.i(com.android.server.display.DisplayPowerController.TAG, "Auto brightness ---003--- rate = " + r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:288:0x04d2, code lost:
        r27.mHandler.removeMessages(8);
        r27.mHandler.sendMessageDelayed(r27.mHandler.obtainMessage(8), 600);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x006b, code lost:
        r27.mScreenState = r27.mPowerRequest.policy;
        r4 = -1;
        r10 = false;
        r11 = r27.mPowerRequest.policy;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:291:0x04f1, code lost:
        if (com.android.server.display.DisplayPowerController.mQuickDarkToBright == false) goto L_0x0512;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:292:0x04f3, code lost:
        com.android.server.display.DisplayPowerController.mQuickDarkToBright = false;
        r2 = com.android.server.display.OppoBrightUtils.BRIGHTNESS_RAMP_RATE_SCREENON;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:293:0x04fa, code lost:
        if (com.android.server.display.DisplayPowerController.DEBUG == false) goto L_0x0512;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:294:0x04fc, code lost:
        android.util.Slog.i(com.android.server.display.DisplayPowerController.TAG, "Auto brightness ---004--- rate = " + r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:295:0x0512, code lost:
        r0 = com.android.server.display.DisplayPowerController.mOppoBrightUtils;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:296:0x0516, code lost:
        if (com.android.server.display.OppoBrightUtils.mPocketRingingState == false) goto L_0x053a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:297:0x0518, code lost:
        com.android.server.display.DisplayPowerController.mOppoBrightUtils.setPocketRingingState(false);
        r2 = com.android.server.display.OppoBrightUtils.BRIGHTNESS_RAMP_RATE_FAST;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:298:0x0522, code lost:
        if (com.android.server.display.DisplayPowerController.DEBUG == false) goto L_0x053a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:299:0x0524, code lost:
        android.util.Slog.i(com.android.server.display.DisplayPowerController.TAG, "Auto brightness ---005--- rate = " + r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0078, code lost:
        if (r11 == 0) goto L_0x009e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:300:0x053a, code lost:
        r0 = com.android.server.display.DisplayPowerController.mOppoBrightUtils;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:301:0x053f, code lost:
        if (com.android.server.display.OppoBrightUtils.mInverseMode != 1) goto L_0x054b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:302:0x0541, code lost:
        r4 = com.android.server.display.DisplayPowerController.mOppoBrightUtils.adjustInverseModeBrightness(r4);
        r27.mBrightnessSource = 11;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:303:0x054b, code lost:
        r0 = com.android.server.display.DisplayPowerController.mOppoBrightUtils;
        com.android.server.display.OppoBrightUtils.mShouldFastRate = false;
        r0 = com.android.server.display.DisplayPowerController.mOppoBrightUtils;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:304:0x0554, code lost:
        if (com.android.server.display.OppoBrightUtils.mScreenGlobalHBMSupport == false) goto L_0x056a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:305:0x0556, code lost:
        r0 = com.android.server.display.DisplayPowerController.mOppoBrightUtils;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:306:0x055a, code lost:
        if (r4 < com.android.server.display.OppoBrightUtils.mMaxBrightness) goto L_0x0563;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:307:0x055c, code lost:
        r27.mOppoDisplayBrightnessHelper.setLightSensorAlwaysOn(true);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:308:0x0563, code lost:
        r27.mOppoDisplayBrightnessHelper.setLightSensorAlwaysOn(false);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:309:0x056a, code lost:
        r27.mOppoDisplayBrightnessHelper.setLightSensorAlwaysOn(false);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x007a, code lost:
        if (r11 == 1) goto L_0x0082;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:310:0x0570, code lost:
        r0 = com.android.server.display.DisplayPowerController.mOppoBrightUtils;
        r0 = 200.0f / com.android.server.display.OppoBrightUtils.mReduceBrightnessRate;
        r10 = com.android.server.display.DisplayPowerController.mOppoBrightUtils;
        r10 = com.android.server.display.OppoBrightUtils.mReduceBrightnessRate * 200.0f;
        r11 = com.android.server.display.DisplayPowerController.mOppoBrightUtils;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:311:0x0582, code lost:
        if (com.android.server.display.OppoBrightUtils.mReduceBrightnessMode != 1) goto L_0x05c4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:312:0x0584, code lost:
        r3 = com.android.server.display.DisplayPowerController.mOppoBrightUtils;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:313:0x058c, code lost:
        if (com.android.server.display.OppoBrightUtils.mReduceBrightnessRate == 1.0f) goto L_0x05c4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:315:0x0591, code lost:
        if (((float) r4) < r10) goto L_0x05c4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:316:0x0593, code lost:
        r3 = com.android.server.display.DisplayPowerController.mOppoBrightUtils;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:317:0x0597, code lost:
        if (r4 > com.android.server.display.OppoBrightUtils.mMaxBrightness) goto L_0x05c4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:319:0x059a, code lost:
        if (r4 <= 2) goto L_0x05c4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x007c, code lost:
        if (r11 == 4) goto L_0x0080;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:321:0x059f, code lost:
        if (((float) r4) > r0) goto L_0x05bc;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:323:0x05a4, code lost:
        if (((float) r4) < r10) goto L_0x05bc;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:324:0x05a6, code lost:
        r4 = (int) ((((200.0f - r10) * ((float) r4)) / (r0 - r10)) + (((r0 - 200.0f) * r10) / (r0 - r10)));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:325:0x05bc, code lost:
        r11 = com.android.server.display.DisplayPowerController.mOppoBrightUtils;
        r4 = (int) (((float) r4) * com.android.server.display.OppoBrightUtils.mReduceBrightnessRate);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:326:0x05c4, code lost:
        r3 = com.android.server.display.DisplayPowerController.mOppoBrightUtils;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:327:0x05c9, code lost:
        if (com.android.server.display.OppoBrightUtils.mShouldAdjustRate != 2) goto L_0x05d2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:328:0x05cb, code lost:
        r3 = com.android.server.display.DisplayPowerController.mOppoBrightUtils;
        com.android.server.display.OppoBrightUtils.mShouldAdjustRate = 0;
        r2 = com.android.server.display.DisplayPowerController.APP_REDUCE_BRIGHTNESS_RATE_IN;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:329:0x05d2, code lost:
        r3 = com.android.server.display.DisplayPowerController.mOppoBrightUtils;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x007e, code lost:
        r11 = 2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:330:0x05d7, code lost:
        if (com.android.server.display.OppoBrightUtils.mShouldAdjustRate != 3) goto L_0x05e0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:331:0x05d9, code lost:
        r3 = com.android.server.display.DisplayPowerController.mOppoBrightUtils;
        com.android.server.display.OppoBrightUtils.mShouldAdjustRate = 0;
        r2 = com.android.server.display.DisplayPowerController.APP_REDUCE_BRIGHTNESS_RATE_OUT;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:333:0x05e2, code lost:
        if (r27.mScreenAnimBightnessTarget != r4) goto L_0x05e6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:334:0x05e4, code lost:
        r2 = r27.mScreenAnimBightnessRate;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:336:0x05ea, code lost:
        if (com.android.server.display.DisplayPowerController.mOppoBrightUtils.isAIBrightnessFeatureOpen == false) goto L_0x05f2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:337:0x05ec, code lost:
        animateScreenBrightness(r4, r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:339:0x05f4, code lost:
        if (com.android.server.display.OppoBrightUtils.mScreenGlobalHBMSupport == false) goto L_0x0600;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0080, code lost:
        r11 = 5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:341:0x05f8, code lost:
        if (com.android.server.display.OppoBrightUtils.sHbmAutoBrightness == false) goto L_0x0600;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:343:0x05fc, code lost:
        if (r4 <= com.android.server.display.OppoBrightUtils.mMaxBrightness) goto L_0x0600;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:344:0x05fe, code lost:
        r3 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:345:0x0600, code lost:
        r3 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:346:0x0601, code lost:
        if (r3 == false) goto L_0x061f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:348:0x0605, code lost:
        if (r4 < com.android.server.display.OppoBrightUtils.mMaxBrightness) goto L_0x060e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:350:0x0609, code lost:
        if (r27.mLightSensorAlwaysOn != false) goto L_0x060e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:353:0x0610, code lost:
        if (com.android.server.display.DisplayPowerController.DEBUG == false) goto L_0x061c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:354:0x0612, code lost:
        android.util.Slog.d(com.android.server.display.DisplayPowerController.TAG, "auto hbm brightness not setting");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:357:0x0621, code lost:
        animateScreenBrightness(r4, r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:358:0x0624, code lost:
        com.android.server.display.OppoBrightUtils.sHbmAutoBrightness = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0086, code lost:
        if (r27.mPowerRequest.dozeScreenState == 0) goto L_0x008d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:362:0x0639, code lost:
        if (r27.mAutomaticBrightnessController == null) goto L_0x066d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:364:0x063c, code lost:
        if (r9 != 1) goto L_0x064a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:366:0x063f, code lost:
        if (r7 != 2) goto L_0x064a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:367:0x0641, code lost:
        r27.mAutomaticBrightnessController.setSleepStartTime(android.os.SystemClock.elapsedRealtime());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:369:0x064c, code lost:
        if (com.android.server.display.DisplayPowerController.DEBUG == false) goto L_0x066d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0088, code lost:
        r11 = r27.mPowerRequest.dozeScreenState;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:370:0x064e, code lost:
        android.util.Slog.d(com.android.server.display.DisplayPowerController.TAG, "upatePowerState state = " + r9 + "  brightness = " + r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:372:0x066f, code lost:
        if (r27.mDisplayWhiteBalanceController == null) goto L_0x068e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:374:0x0672, code lost:
        if (r9 != 2) goto L_0x0688;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:376:0x067a, code lost:
        if (r27.mDisplayWhiteBalanceSettings.isEnabled() == false) goto L_0x0688;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:377:0x067c, code lost:
        r27.mDisplayWhiteBalanceController.setEnabled(true);
        r27.mDisplayWhiteBalanceController.updateDisplayColorTemperature();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:378:0x0688, code lost:
        r27.mDisplayWhiteBalanceController.setEnabled(false);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x008d, code lost:
        r11 = 3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:380:0x0690, code lost:
        if (r27.mPendingScreenOnUnblocker != null) goto L_0x06b8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:382:0x0694, code lost:
        if (r27.mColorFadeEnabled == false) goto L_0x06a6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:384:0x069c, code lost:
        if (r27.mColorFadeOnAnimator.isStarted() != false) goto L_0x06b8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:386:0x06a4, code lost:
        if (r27.mColorFadeOffAnimator.isStarted() != false) goto L_0x06b8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:388:0x06aa, code lost:
        if (isBlockDisplayByBiometrics() != false) goto L_0x06b8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:390:0x06b4, code lost:
        if (r27.mPowerState.waitUntilClean(r27.mCleanListener) == false) goto L_0x06b8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:391:0x06b6, code lost:
        r0 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:392:0x06b8, code lost:
        r0 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:394:0x06ba, code lost:
        if (r0 == false) goto L_0x06c6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:396:0x06c2, code lost:
        if (r27.mScreenBrightnessRampAnimator.isAnimating() != false) goto L_0x06c6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:397:0x06c4, code lost:
        r0 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:398:0x06c6, code lost:
        r0 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x0090, code lost:
        if (r27.mAllowAutoBrightnessWhileDozingConfig != false) goto L_0x00a1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:400:0x06c8, code lost:
        if (r0 == false) goto L_0x06e0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:402:0x06cb, code lost:
        if (r9 == 1) goto L_0x06e0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:404:0x06ce, code lost:
        if (r9 == 3) goto L_0x06e0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:406:0x06d1, code lost:
        if (r9 == 4) goto L_0x06e0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:408:0x06d5, code lost:
        if (r27.mReportedScreenStateToPolicy != 1) goto L_0x06e0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:409:0x06d7, code lost:
        setReportedScreenState(2);
        r27.mWindowManagerPolicy.screenTurnedOn();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x0092, code lost:
        r4 = r27.mPowerRequest.dozeScreenBrightness;
        r27.mBrightnessReasonTemp.setReason(2);
        r27.mBrightnessSource = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:410:0x06e0, code lost:
        if (r0 != false) goto L_0x06f9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:412:0x06e4, code lost:
        if (r27.mUnfinishedBusiness != false) goto L_0x06f9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:414:0x06e8, code lost:
        if (com.android.server.display.DisplayPowerController.DEBUG == false) goto L_0x06f1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:415:0x06ea, code lost:
        android.util.Slog.d(com.android.server.display.DisplayPowerController.TAG, "Unfinished business...");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:416:0x06f1, code lost:
        r27.mCallbacks.acquireSuspendBlocker();
        r27.mUnfinishedBusiness = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:417:0x06f9, code lost:
        if (r0 == false) goto L_0x0723;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:418:0x06fb, code lost:
        if (r6 == false) goto L_0x0723;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:419:0x06fd, code lost:
        r27.mOppoDisplayPowerHelper.onUpdatePowerState(r9, r27.mPowerRequest.policy, r4);
        r10 = r27.mLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x009e, code lost:
        r11 = 1;
        r10 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:420:0x0708, code lost:
        monitor-enter(r10);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:423:0x070b, code lost:
        if (r27.mPendingRequestChangedLocked != false) goto L_0x071b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:424:0x070d, code lost:
        r27.mDisplayReadyLocked = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:425:0x0712, code lost:
        if (com.android.server.display.DisplayPowerController.DEBUG == false) goto L_0x071b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:426:0x0714, code lost:
        android.util.Slog.d(com.android.server.display.DisplayPowerController.TAG, "Display ready!");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:427:0x071b, code lost:
        monitor-exit(r10);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:428:0x071c, code lost:
        sendOnStateChangedWithWakelock();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:432:0x0723, code lost:
        if (r0 == false) goto L_0x073d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:434:0x0727, code lost:
        if (r27.mUnfinishedBusiness == false) goto L_0x073d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:436:0x072b, code lost:
        if (com.android.server.display.DisplayPowerController.DEBUG == false) goto L_0x0734;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:437:0x072d, code lost:
        android.util.Slog.d(com.android.server.display.DisplayPowerController.TAG, "Finished business...");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:438:0x0734, code lost:
        r0 = false;
        r27.mUnfinishedBusiness = false;
        r27.mCallbacks.releaseSuspendBlocker();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:439:0x073d, code lost:
        r0 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x00a4, code lost:
        if (r27.mProximitySensor == null) goto L_0x00be;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:441:0x073f, code lost:
        if (r9 == 2) goto L_0x0742;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:442:0x0741, code lost:
        r0 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:443:0x0742, code lost:
        r27.mDozing = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:444:0x0748, code lost:
        if (r5 == r27.mPowerRequest.policy) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:445:0x074a, code lost:
        logDisplayPolicyChanged(r27.mPowerRequest.policy);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x00a6, code lost:
        r27.mOppoDisplayPowerHelper.applyOppoProximitySensorLocked(r27.mPowerRequest, r27.mProximity, r27.mProximitySensorEnabled, r27.mWaitingForNegativeProximity, r27.mScreenOffBecauseOfProximity, r11);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:451:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:452:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x00be, code lost:
        r27.mWaitingForNegativeProximity = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x00c4, code lost:
        if (getUseProximityForceSuspendState() != false) goto L_0x00cb;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x00c8, code lost:
        if (r27.mScreenOffBecauseOfProximity == false) goto L_0x00cb;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x00ca, code lost:
        r11 = 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x00cb, code lost:
        r7 = r27.mPowerState.getScreenState();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x00d3, code lost:
        if (r27.mFingerprintOpticalSupport == false) goto L_0x00db;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x00d5, code lost:
        if (r7 == 3) goto L_0x00da;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x00d8, code lost:
        if (r7 != 4) goto L_0x00db;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x00da, code lost:
        r10 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x00db, code lost:
        animateScreenStateChange(r11, r10);
        r9 = r27.mPowerState.getScreenState();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x00e6, code lost:
        if (r9 != 1) goto L_0x00f0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x00e8, code lost:
        r4 = 0;
        r27.mBrightnessReasonTemp.setReason(5);
        r27.mBrightnessSource = 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x00f1, code lost:
        if (r9 != 5) goto L_0x00fd;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x00f3, code lost:
        r4 = r27.mScreenBrightnessForVr;
        r27.mBrightnessReasonTemp.setReason(6);
        r27.mBrightnessSource = 2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x00fe, code lost:
        if (r4 >= 0) goto L_0x018b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:66:0x0104, code lost:
        if (r27.mPowerRequest.screenBrightnessOverride <= 0) goto L_0x018b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:0x0106, code lost:
        r14 = com.android.server.display.DisplayPowerController.mOppoBrightUtils.getMaximumScreenBrightnessSetting();
        r15 = com.android.server.display.DisplayPowerController.mOppoBrightUtils.getMinimumScreenBrightnessSetting();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x0116, code lost:
        if (r27.mPowerRequest.screenBrightnessOverride <= r14) goto L_0x013a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:70:0x011a, code lost:
        if (com.android.server.display.DisplayPowerController.DEBUG == false) goto L_0x0138;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:71:0x011c, code lost:
        android.util.Slog.i(com.android.server.display.DisplayPowerController.TAG, "screenBrightnessOverride requested has over the maxBrightness(" + r14 + ")\n");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:72:0x0138, code lost:
        r4 = r14;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:74:0x013e, code lost:
        if (r27.mPowerRequest.screenBrightnessOverride >= r15) goto L_0x0162;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:76:0x0142, code lost:
        if (com.android.server.display.DisplayPowerController.DEBUG == false) goto L_0x0160;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:77:0x0144, code lost:
        android.util.Slog.i(com.android.server.display.DisplayPowerController.TAG, "screenBrightnessOverride requested has under the miniBrightness(" + r15 + ")\n");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:78:0x0160, code lost:
        r4 = r15;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:79:0x0162, code lost:
        r4 = r27.mPowerRequest.screenBrightnessOverride;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:80:0x0166, code lost:
        r27.mBrightnessSource = 3;
        r27.mBrightnessReasonTemp.setReason(7);
        r27.mAppliedScreenBrightnessOverride = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:81:0x0172, code lost:
        if (com.android.server.display.DisplayPowerController.DEBUG == false) goto L_0x018d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:82:0x0174, code lost:
        android.util.Slog.i(com.android.server.display.DisplayPowerController.TAG, "Auto brightness has override maybe from window brightness = " + r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:83:0x018b, code lost:
        r27.mAppliedScreenBrightnessOverride = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:84:0x018d, code lost:
        r11 = getmScreenBrightnessModeSetting();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:85:0x0193, code lost:
        if (r27.mAllowAutoBrightnessWhileDozingConfig == false) goto L_0x019d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:87:0x0199, code lost:
        if (android.view.Display.isDozeState(r9) == false) goto L_0x019d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:88:0x019b, code lost:
        r12 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:89:0x019d, code lost:
        r12 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:91:0x01a2, code lost:
        if (r27.mPowerRequest.useAutoBrightness == false) goto L_0x01b4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:93:0x01a5, code lost:
        if (r9 == 2) goto L_0x01a9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:94:0x01a7, code lost:
        if (r12 == false) goto L_0x01b4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:95:0x01a9, code lost:
        if (r4 >= 0) goto L_0x01b4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:97:0x01ad, code lost:
        if (r27.mAutomaticBrightnessController == null) goto L_0x01b4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:99:0x01b0, code lost:
        if (r11 != 1) goto L_0x01b4;
     */
    public void updatePowerState() {
        int previousPolicy;
        boolean mustInitialize = false;
        this.mBrightnessReasonTemp.set(null);
        synchronized (this.mLock) {
            try {
                this.mPendingUpdatePowerStateLocked = false;
                if (this.mPendingRequestLocked != null) {
                    if (this.mPowerRequest == null) {
                        this.mPowerRequest = new DisplayManagerInternal.DisplayPowerRequest(this.mPendingRequestLocked);
                        this.mWaitingForNegativeProximity = this.mPendingWaitForNegativeProximityLocked;
                        this.mPendingWaitForNegativeProximityLocked = false;
                        this.mPendingRequestChangedLocked = false;
                        mustInitialize = true;
                        previousPolicy = 3;
                    } else if (this.mPendingRequestChangedLocked) {
                        previousPolicy = this.mPowerRequest.policy;
                        this.mPowerRequest.copyFrom(this.mPendingRequestLocked);
                        this.mWaitingForNegativeProximity |= this.mPendingWaitForNegativeProximityLocked;
                        this.mPendingWaitForNegativeProximityLocked = false;
                        this.mPendingRequestChangedLocked = false;
                        this.mDisplayReadyLocked = false;
                    } else {
                        previousPolicy = this.mPowerRequest.policy;
                    }
                    try {
                        boolean mustNotify = !this.mDisplayReadyLocked;
                    } catch (Throwable th) {
                        th = th;
                        throw th;
                    }
                }
            } catch (Throwable th2) {
                th = th2;
                throw th;
            }
        }
    }

    @Override // com.android.server.display.OppoAutomaticBrightnessController.Callbacks
    public void updateBrightness() {
        sendUpdatePowerState();
    }

    public void setBrightnessConfiguration(BrightnessConfiguration c) {
        this.mHandler.obtainMessage(5, c).sendToTarget();
    }

    public void setRotateState(boolean isStart) {
        this.mAutomaticBrightnessController.setRotateState(isStart);
    }

    public void setTemporaryBrightness(int brightness) {
        if (DEBUG_PANIC || DEBUG) {
            Slog.d(TAG, "setTemporaryBrightness = " + brightness);
        }
        this.mHandler.obtainMessage(6, brightness, 0).sendToTarget();
    }

    public void setTemporaryAutoBrightnessAdjustment(float adjustment) {
        this.mOppoDisplayBrightnessHelper.setTemporaryAutoBrightnessAdjustment(adjustment);
        this.mHandler.obtainMessage(7, Float.floatToIntBits(adjustment), 0).sendToTarget();
    }

    private void blockScreenOn() {
        this.mHandler.removeMessages(3);
        if (this.mPendingScreenOnUnblocker == null) {
            Trace.asyncTraceBegin(131072, SCREEN_ON_BLOCKED_TRACE_NAME, 0);
            this.mPendingScreenOnUnblocker = new ScreenOnUnblocker();
            this.mScreenOnBlockStartRealTime = SystemClock.elapsedRealtime();
            Slog.i(TAG, "Blocking screen on until initial contents have been drawn.");
        }
    }

    /* access modifiers changed from: private */
    public void unblockScreenOn() {
        this.mHandler.removeMessages(3);
        if (this.mPendingScreenOnUnblocker != null) {
            this.mPendingScreenOnUnblocker = null;
            long delay = SystemClock.elapsedRealtime() - this.mScreenOnBlockStartRealTime;
            Slog.i(TAG, "Unblocked screen on after " + delay + " ms");
            Trace.asyncTraceEnd(131072, SCREEN_ON_BLOCKED_TRACE_NAME, 0);
        }
    }

    private void blockScreenOff() {
        if (this.mPendingScreenOffUnblocker == null) {
            Trace.asyncTraceBegin(131072, SCREEN_OFF_BLOCKED_TRACE_NAME, 0);
            this.mPendingScreenOffUnblocker = new ScreenOffUnblocker();
            this.mScreenOffBlockStartRealTime = SystemClock.elapsedRealtime();
            Slog.i(TAG, "Blocking screen off");
        }
    }

    /* access modifiers changed from: private */
    public void unblockScreenOff() {
        if (this.mPendingScreenOffUnblocker != null) {
            this.mPendingScreenOffUnblocker = null;
            long delay = SystemClock.elapsedRealtime() - this.mScreenOffBlockStartRealTime;
            Slog.i(TAG, "Unblocked screen off after " + delay + " ms");
            Trace.asyncTraceEnd(131072, SCREEN_OFF_BLOCKED_TRACE_NAME, 0);
        }
    }

    private boolean setScreenState(int state) {
        return setScreenState(state, false);
    }

    private boolean setScreenState(int state, boolean reportOnly) {
        this.mOppoDisplayBrightnessHelper.doFirstSetScreenStateAction(state, this.mPowerRequest);
        boolean isOff = state == 1 || state == 3 || state == 4;
        if (this.mPowerState.getScreenState() != state) {
            if (isOff && !this.mScreenOffBecauseOfProximity) {
                if (this.mReportedScreenStateToPolicy == 2) {
                    setReportedScreenState(3);
                    blockScreenOff();
                    this.mWindowManagerPolicy.screenTurningOff(this.mPendingScreenOffUnblocker);
                    unblockScreenOff();
                } else if (this.mPendingScreenOffUnblocker != null) {
                    return false;
                }
            }
            this.mOppoDisplayBrightnessHelper.doSetScreenStateAction(state, this.mPowerState, this.mPowerRequest);
            if (!reportOnly) {
                Trace.traceCounter(131072, "ScreenState", state);
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
        if (isOff && this.mReportedScreenStateToPolicy != 0 && !this.mScreenOffBecauseOfProximity) {
            setReportedScreenState(0);
            unblockScreenOn();
            this.mOppoDisplayPowerHelper.unblockDisplayReady();
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
        return this.mPendingScreenOnUnblocker == null && !isBlockScreenOnByBiometrics();
    }

    private void setReportedScreenState(int state) {
        Trace.traceCounter(131072, "ReportedScreenStateToPolicy", state);
        this.mReportedScreenStateToPolicy = state;
    }

    private int clampScreenBrightnessForVr(int value) {
        return MathUtils.constrain(value, this.mScreenBrightnessForVrRangeMinimum, this.mScreenBrightnessForVrRangeMaximum);
    }

    private int clampScreenBrightness(int value) {
        return MathUtils.constrain(value, this.mScreenBrightnessRangeMinimum, this.mScreenBrightnessRangeMaximum);
    }

    public void animateScreenBrightness(int target, int rate) {
        if (DEBUG || DEBUG_PANIC) {
            Slog.d(TAG, "Animating brightness: target=" + target + ", rate=" + rate + " mode=" + OppoBrightUtils.sGlobalHbmSellMode + " source=" + this.mBrightnessSource);
        }
        if (OppoBrightUtils.mScreenGlobalHBMSupport && OppoBrightUtils.sGlobalHbmSellMode == 1) {
            target = target > 1023 ? 1023 : target;
            Slog.e(TAG, "  tartet=" + target + " mode=" + OppoBrightUtils.sGlobalHbmSellMode + " source=" + this.mBrightnessSource);
        }
        OppoBrightUtils oppoBrightUtils = mOppoBrightUtils;
        if (OppoBrightUtils.mSetBrihgtnessSlide) {
            if ((OppoBrightUtils.mScreenGlobalHBMSupport && target <= 1023) || !OppoBrightUtils.mScreenGlobalHBMSupport) {
                Settings.System.putIntForBrightness(this.mContext.getContentResolver(), "screen_brightness", target, -2);
            }
            OppoBrightUtils oppoBrightUtils2 = mOppoBrightUtils;
            OppoBrightUtils.mSetBrihgtnessSlide = false;
        }
        this.mScreenAnimBightnessTarget = target;
        this.mScreenAnimBightnessRate = rate;
        if (this.mScreenBrightnessRampAnimator.animateTo(target, rate)) {
            Trace.traceCounter(131072, "TargetScreenBrightness", target);
            try {
                this.mBatteryStats.noteScreenBrightness(target);
            } catch (RemoteException e) {
            }
        }
    }

    private void animateScreenStateChange(int target, boolean performScreenOffTransition) {
        if (!this.mColorFadeEnabled || (!this.mColorFadeOnAnimator.isStarted() && !this.mColorFadeOffAnimator.isStarted())) {
            int i = 2;
            if (this.mDisplayBlanksAfterDozeConfig && Display.isDozeState(this.mPowerState.getScreenState()) && !Display.isDozeState(target) && target != 2) {
                this.mPowerState.prepareColorFade(this.mContext, this.mColorFadeFadesConfig ? 2 : 0);
                ObjectAnimator objectAnimator = this.mColorFadeOffAnimator;
                if (objectAnimator != null) {
                    objectAnimator.end();
                }
                setScreenState(1, target != 1);
            }
            if (this.mblackGestureWakeUp && target == 2 && this.mPowerState.getScreenState() != 2) {
                performScreenOffTransition = true;
                this.mPowerState.prepareColorFade(this.mContext, this.mColorFadeFadesConfig ? 2 : 0);
                ObjectAnimator objectAnimator2 = this.mColorFadeOffAnimator;
                if (objectAnimator2 != null) {
                    objectAnimator2.end();
                }
                setScreenState(1, target != 1);
                this.mblackGestureWakeUp = false;
                Slog.d(TAG, "unblack screen set ColorFadeOffAnimator");
            }
            if (this.mPendingScreenOff && target != 1) {
                setScreenState(1);
                this.mPendingScreenOff = false;
                this.mPowerState.dismissColorFadeResources();
            }
            if (target == 2) {
                if (setScreenState(2)) {
                    this.mPowerState.setAodStatus(false);
                    this.mPowerState.setColorFadeLevel(1.0f);
                    this.mPowerState.dismissColorFade();
                }
            } else if (target == 5) {
                if ((!this.mScreenBrightnessRampAnimator.isAnimating() || this.mPowerState.getScreenState() != 2) && setScreenState(5)) {
                    this.mPowerState.setAodStatus(false);
                    this.mPowerState.setColorFadeLevel(1.0f);
                    this.mPowerState.dismissColorFade();
                }
            } else if (target == 3) {
                if ((!this.mScreenBrightnessRampAnimator.isAnimating() || this.mPowerState.getScreenState() != 2) && setScreenState(3)) {
                    this.mPowerState.setAodStatus(true);
                    if (this.mFingerprintOpticalSupport) {
                        this.mPowerState.setColorFadeLevel(1.0f);
                        this.mPowerState.dismissColorFade();
                    }
                    this.mPowerState.setColorFadeLevel(1.0f);
                    this.mPowerState.dismissColorFade();
                }
            } else if (target == 4) {
                if (!this.mScreenBrightnessRampAnimator.isAnimating() || this.mPowerState.getScreenState() == 4) {
                    if (this.mPowerState.getScreenState() != 4) {
                        setScreenState(4);
                    }
                    this.mPowerState.setAodStatus(true);
                    if (this.mFingerprintOpticalSupport) {
                        this.mPowerState.setColorFadeLevel(1.0f);
                        this.mPowerState.dismissColorFade();
                    }
                    this.mPowerState.setColorFadeLevel(1.0f);
                    this.mPowerState.dismissColorFade();
                }
            } else if (target != 6) {
                this.mPendingScreenOff = true;
                this.mPowerState.setAodStatus(false);
                if (!this.mColorFadeEnabled) {
                    this.mPowerState.setColorFadeLevel(OppoBrightUtils.MIN_LUX_LIMITI);
                }
                if (this.mPowerState.getColorFadeLevel() == OppoBrightUtils.MIN_LUX_LIMITI) {
                    setScreenState(1);
                    this.mPendingScreenOff = false;
                    this.mPowerState.dismissColorFadeResources();
                    return;
                }
                if (performScreenOffTransition) {
                    DisplayPowerState displayPowerState = this.mPowerState;
                    Context context = this.mContext;
                    if (!this.mColorFadeFadesConfig) {
                        i = 1;
                    }
                    if (displayPowerState.prepareColorFade(context, i) && this.mPowerState.getScreenState() != 1) {
                        this.mColorFadeOffAnimator.start();
                        return;
                    }
                }
                this.mColorFadeOffAnimator.end();
            } else if (!this.mScreenBrightnessRampAnimator.isAnimating() || this.mPowerState.getScreenState() == 6) {
                if (this.mPowerState.getScreenState() != 6) {
                    if (setScreenState(2)) {
                        setScreenState(6);
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
            setUseProximityForceSuspend(false);
            this.mProximity = -1;
            this.mPendingProximity = -1;
            this.mHandler.removeMessages(2);
            this.mSensorManager.unregisterListener(this.mProximitySensorListener);
            clearPendingProximityDebounceTime();
        }
    }

    /* access modifiers changed from: private */
    public void handleProximitySensorEvent(long time, boolean positive) {
        if (!this.mProximitySensorEnabled) {
            return;
        }
        if (this.mPendingProximity == 0 && !positive) {
            return;
        }
        if (this.mPendingProximity != 1 || !positive) {
            this.mHandler.removeMessages(2);
            if (positive) {
                this.mPendingProximity = 1;
                setPendingProximityDebounceTime(0 + time);
            } else {
                this.mPendingProximity = 0;
                setPendingProximityDebounceTime(0 + time);
            }
            debounceProximitySensor();
        }
    }

    /* access modifiers changed from: private */
    public void debounceProximitySensor() {
        if (this.mProximitySensorEnabled && this.mPendingProximity != -1 && this.mPendingProximityDebounceTime >= 0) {
            if (this.mPendingProximityDebounceTime <= SystemClock.uptimeMillis()) {
                this.mProximity = this.mPendingProximity;
                onProximityDebounceTimeArrived();
                updatePowerState();
                clearPendingProximityDebounceTime();
                return;
            }
            this.mHandler.sendMessageAtTime(this.mHandler.obtainMessage(2), this.mPendingProximityDebounceTime);
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

    private void logDisplayPolicyChanged(int newPolicy) {
        LogMaker log = new LogMaker(1696);
        log.setType(6);
        log.setSubtype(newPolicy);
        MetricsLogger.action(log);
    }

    /* access modifiers changed from: private */
    public void handleSettingsChange(boolean userSwitch) {
        this.mPendingScreenBrightnessSetting = getScreenBrightnessSetting();
        if (userSwitch) {
            this.mCurrentScreenBrightnessSetting = this.mPendingScreenBrightnessSetting;
            OppoAutomaticBrightnessController oppoAutomaticBrightnessController = this.mAutomaticBrightnessController;
            if (oppoAutomaticBrightnessController != null) {
                oppoAutomaticBrightnessController.resetShortTermModel();
            }
        }
        this.mPendingAutoBrightnessAdjustment = getAutoBrightnessAdjustmentSetting();
        this.mScreenBrightnessForVr = getScreenBrightnessForVrSetting();
        OppoBrightUtils.sGlobalHbmSellMode = getGlobalHbmSellMode();
        sendUpdatePowerState();
    }

    private int getGlobalHbmSellMode() {
        return Settings.Secure.getIntForUser(this.mContext.getContentResolver(), OppoBrightUtils.GLOBAL_HBM_SELL_MODE, 0, -2);
    }

    private float getAutoBrightnessAdjustmentSetting() {
        float adj = Settings.System.getFloatForUser(this.mContext.getContentResolver(), "screen_auto_brightness_adj", OppoBrightUtils.MIN_LUX_LIMITI, -2);
        if (Float.isNaN(adj)) {
            return OppoBrightUtils.MIN_LUX_LIMITI;
        }
        return clampAutoBrightnessAdjustment(adj);
    }

    private int getScreenBrightnessSetting() {
        return clampAbsoluteBrightness(Settings.System.getIntForBrightness(this.mContext.getContentResolver(), "screen_brightness", this.mScreenBrightnessDefault, -2));
    }

    private int getScreenBrightnessForVrSetting() {
        return clampScreenBrightnessForVr(Settings.System.getIntForUser(this.mContext.getContentResolver(), "screen_brightness_for_vr", this.mScreenBrightnessForVrDefault, -2));
    }

    public void putScreenBrightnessSetting(int brightness) {
        this.mCurrentScreenBrightnessSetting = brightness;
        if ((OppoBrightUtils.mScreenGlobalHBMSupport && brightness <= 1023) || !OppoBrightUtils.mScreenGlobalHBMSupport) {
            Settings.System.putIntForBrightness(this.mContext.getContentResolver(), "screen_brightness", brightness, -2);
        }
    }

    private void putAutoBrightnessAdjustmentSetting(float adjustment) {
        this.mAutoBrightnessAdjustment = adjustment;
        Settings.System.putFloatForUser(this.mContext.getContentResolver(), "screen_auto_brightness_adj", adjustment, -2);
    }

    private boolean updateAutoBrightnessAdjustment() {
        if (mOppoBrightUtils.isSpecialAdj(this.mTemporaryAutoBrightnessAdjustment)) {
            this.mAutoBrightnessAdjustment = this.mTemporaryAutoBrightnessAdjustment;
            this.mPendingAutoBrightnessAdjustment = Float.NaN;
            this.mTemporaryAutoBrightnessAdjustment = Float.NaN;
            return true;
        } else if (Float.isNaN(this.mPendingAutoBrightnessAdjustment)) {
            return false;
        } else {
            float f = this.mAutoBrightnessAdjustment;
            if (f == this.mPendingAutoBrightnessAdjustment || mOppoBrightUtils.isSpecialAdj(f)) {
                this.mPendingAutoBrightnessAdjustment = Float.NaN;
                return false;
            }
            float f2 = this.mPendingAutoBrightnessAdjustment;
            if (f2 == OppoBrightUtils.MIN_LUX_LIMITI) {
                this.mPendingAutoBrightnessAdjustment = Float.NaN;
                return false;
            }
            this.mAutoBrightnessAdjustment = f2;
            this.mPendingAutoBrightnessAdjustment = Float.NaN;
            return true;
        }
    }

    private boolean updateUserSetScreenBrightness() {
        int i = this.mPendingScreenBrightnessSetting;
        if (i < 0) {
            if (this.mCurrentScreenBrightnessSetting == this.mTemporaryScreenBrightness) {
                this.mTemporaryScreenBrightness = -1;
            }
            return false;
        } else if (this.mCurrentScreenBrightnessSetting == i) {
            this.mPendingScreenBrightnessSetting = -1;
            this.mTemporaryScreenBrightness = -1;
            return false;
        } else {
            this.mCurrentScreenBrightnessSetting = i;
            this.mLastUserSetScreenBrightness = i;
            this.mPendingScreenBrightnessSetting = -1;
            this.mTemporaryScreenBrightness = -1;
            return true;
        }
    }

    private void notifyBrightnessChanged(int brightness, boolean userInitiated, boolean hadUserDataPoint) {
        float powerFactor;
        float brightnessInNits = convertToNits(brightness);
        if (this.mPowerRequest.useAutoBrightness && brightnessInNits >= OppoBrightUtils.MIN_LUX_LIMITI && this.mAutomaticBrightnessController != null) {
            if (this.mPowerRequest.lowPowerMode) {
                powerFactor = this.mPowerRequest.screenLowPowerBrightnessFactor;
            } else {
                powerFactor = 1.0f;
            }
            this.mBrightnessTracker.notifyBrightnessChanged(brightnessInNits, userInitiated, powerFactor, hadUserDataPoint, this.mAutomaticBrightnessController.isDefaultConfig());
        }
    }

    private float convertToNits(int backlight) {
        BrightnessMappingStrategy brightnessMappingStrategy = this.mBrightnessMapper;
        if (brightnessMappingStrategy != null) {
            return brightnessMappingStrategy.convertToNits(backlight);
        }
        return -1.0f;
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
        pw.println("  mScreenBrightnessRangeMinimum=" + this.mScreenBrightnessRangeMinimum);
        pw.println("  mScreenBrightnessRangeMaximum=" + this.mScreenBrightnessRangeMaximum);
        pw.println("  mScreenBrightnessDefault=" + this.mScreenBrightnessDefault);
        pw.println("  mScreenBrightnessForVrRangeMinimum=" + this.mScreenBrightnessForVrRangeMinimum);
        pw.println("  mScreenBrightnessForVrRangeMaximum=" + this.mScreenBrightnessForVrRangeMaximum);
        pw.println("  mScreenBrightnessForVrDefault=" + this.mScreenBrightnessForVrDefault);
        pw.println("  mUseSoftwareAutoBrightnessConfig=" + this.mUseSoftwareAutoBrightnessConfig);
        pw.println("  mAllowAutoBrightnessWhileDozingConfig=" + this.mAllowAutoBrightnessWhileDozingConfig);
        pw.println("  mBrightnessRampRateFast=" + this.mBrightnessRampRateFast);
        pw.println("  mBrightnessRampRateSlow=" + this.mBrightnessRampRateSlow);
        pw.println("  mSkipScreenOnBrightnessRamp=" + this.mSkipScreenOnBrightnessRamp);
        pw.println("  mColorFadeFadesConfig=" + this.mColorFadeFadesConfig);
        pw.println("  mColorFadeEnabled=" + this.mColorFadeEnabled);
        pw.println("  mDisplayBlanksAfterDozeConfig=" + this.mDisplayBlanksAfterDozeConfig);
        pw.println("  mBrightnessBucketsInDozeConfig=" + this.mBrightnessBucketsInDozeConfig);
        this.mHandler.runWithScissors(new Runnable() {
            /* class com.android.server.display.DisplayPowerController.AnonymousClass8 */

            public void run() {
                DisplayPowerController.this.dumpLocal(pw);
            }
        }, 1000);
    }

    /* access modifiers changed from: private */
    public void dumpLocal(PrintWriter pw) {
        pw.println();
        pw.println("Display Power Controller Thread State:");
        pw.println("  mPowerRequest=" + this.mPowerRequest);
        pw.println("  mUnfinishedBusiness=" + this.mUnfinishedBusiness);
        pw.println("  mWaitingForNegativeProximity=" + this.mWaitingForNegativeProximity);
        pw.println("  mProximitySensor=" + this.mProximitySensor);
        pw.println("  mProximitySensorEnabled=" + this.mProximitySensorEnabled);
        pw.println("  mProximityThreshold=" + this.mProximityThreshold);
        pw.println("  mProximity=" + proximityToString(this.mProximity));
        pw.println("  mPendingProximity=" + proximityToString(this.mPendingProximity));
        pw.println("  mPendingProximityDebounceTime=" + TimeUtils.formatUptime(this.mPendingProximityDebounceTime));
        pw.println("  mScreenOffBecauseOfProximity=" + this.mScreenOffBecauseOfProximity);
        pw.println("  mLastUserSetScreenBrightness=" + this.mLastUserSetScreenBrightness);
        pw.println("  mCurrentScreenBrightnessSetting=" + this.mCurrentScreenBrightnessSetting);
        pw.println("  mPendingScreenBrightnessSetting=" + this.mPendingScreenBrightnessSetting);
        pw.println("  mTemporaryScreenBrightness=" + this.mTemporaryScreenBrightness);
        pw.println("  mAutoBrightnessAdjustment=" + this.mAutoBrightnessAdjustment);
        pw.println("  mBrightnessReason=" + this.mBrightnessReason);
        pw.println("  mTemporaryAutoBrightnessAdjustment=" + this.mTemporaryAutoBrightnessAdjustment);
        pw.println("  mPendingAutoBrightnessAdjustment=" + this.mPendingAutoBrightnessAdjustment);
        pw.println("  mScreenBrightnessForVr=" + this.mScreenBrightnessForVr);
        pw.println("  mAppliedAutoBrightness=" + this.mAppliedAutoBrightness);
        pw.println("  mAppliedDimming=" + this.mAppliedDimming);
        pw.println("  mAppliedLowPower=" + this.mAppliedLowPower);
        pw.println("  mAppliedScreenBrightnessOverride=" + this.mAppliedScreenBrightnessOverride);
        pw.println("  mAppliedTemporaryBrightness=" + this.mAppliedTemporaryBrightness);
        pw.println("  mDozing=" + this.mDozing);
        pw.println("  mSkipRampState=" + skipRampStateToString(this.mSkipRampState));
        pw.println("  mInitialAutoBrightness=" + this.mInitialAutoBrightness);
        pw.println("  mScreenOnBlockStartRealTime=" + this.mScreenOnBlockStartRealTime);
        pw.println("  mScreenOffBlockStartRealTime=" + this.mScreenOffBlockStartRealTime);
        pw.println("  mPendingScreenOnUnblocker=" + this.mPendingScreenOnUnblocker);
        pw.println("  mPendingScreenOffUnblocker=" + this.mPendingScreenOffUnblocker);
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
        DisplayPowerState displayPowerState = this.mPowerState;
        if (displayPowerState != null) {
            displayPowerState.dump(pw);
        }
        OppoAutomaticBrightnessController oppoAutomaticBrightnessController = this.mAutomaticBrightnessController;
        if (oppoAutomaticBrightnessController != null) {
            oppoAutomaticBrightnessController.dump(pw);
        }
        if (this.mBrightnessTracker != null) {
            pw.println();
            this.mBrightnessTracker.dump(pw);
        }
        pw.println();
        DisplayWhiteBalanceController displayWhiteBalanceController = this.mDisplayWhiteBalanceController;
        if (displayWhiteBalanceController != null) {
            displayWhiteBalanceController.dump(pw);
            this.mDisplayWhiteBalanceSettings.dump(pw);
        }
        this.mOppoDisplayPowerHelper.dump(pw);
    }

    private static String proximityToString(int state) {
        if (state == -1) {
            return "Unknown";
        }
        if (state == 0) {
            return "Negative";
        }
        if (state != 1) {
            return Integer.toString(state);
        }
        return "Positive";
    }

    private static String reportedToPolicyToString(int state) {
        if (state == 0) {
            return "REPORTED_TO_POLICY_SCREEN_OFF";
        }
        if (state == 1) {
            return "REPORTED_TO_POLICY_SCREEN_TURNING_ON";
        }
        if (state != 2) {
            return Integer.toString(state);
        }
        return "REPORTED_TO_POLICY_SCREEN_ON";
    }

    private static String skipRampStateToString(int state) {
        if (state == 0) {
            return "RAMP_STATE_SKIP_NONE";
        }
        if (state == 1) {
            return "RAMP_STATE_SKIP_INITIAL";
        }
        if (state != 2) {
            return Integer.toString(state);
        }
        return "RAMP_STATE_SKIP_AUTOBRIGHT";
    }

    private static float normalizeAbsoluteBrightness(int value) {
        if (OppoBrightUtils.mScreenGlobalHBMSupport) {
            return ((float) clampAbsoluteBrightness(value)) / ((float) OppoBrightUtils.HBM_EXTEND_MAXBRIGHTNESS);
        }
        return ((float) clampAbsoluteBrightness(value)) / ((float) PowerManager.BRIGHTNESS_MULTIBITS_ON);
    }

    private static int clampAbsoluteBrightness(int value) {
        if (OppoBrightUtils.mScreenGlobalHBMSupport) {
            return MathUtils.constrain(value, 0, OppoBrightUtils.HBM_EXTEND_MAXBRIGHTNESS);
        }
        return MathUtils.constrain(value, 0, PowerManager.BRIGHTNESS_MULTIBITS_ON);
    }

    private static float clampAutoBrightnessAdjustment(float value) {
        if (OppoBrightUtils.mScreenGlobalHBMSupport) {
            return MathUtils.constrain(value, (float) OppoBrightUtils.MIN_LUX_LIMITI, (float) OppoBrightUtils.HBM_EXTEND_MAXBRIGHTNESS);
        }
        return MathUtils.constrain(value, (float) OppoBrightUtils.MIN_LUX_LIMITI, (float) PowerManager.BRIGHTNESS_MULTIBITS_ON);
    }

    /* access modifiers changed from: private */
    public final class DisplayControllerHandler extends Handler {
        public DisplayControllerHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == DisplayPowerController.MSG_GAMESPACE_IN_OUT_TIMEOUT) {
                OppoBrightUtils unused = DisplayPowerController.mOppoBrightUtils;
                OppoBrightUtils.mSpecialBrightnessFlag &= 16777214;
            } else if (i != 2020) {
                switch (i) {
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
                        BrightnessConfiguration unused2 = DisplayPowerController.this.mBrightnessConfiguration = (BrightnessConfiguration) msg.obj;
                        DisplayPowerController.this.updatePowerState();
                        return;
                    case 6:
                        int unused3 = DisplayPowerController.this.mTemporaryScreenBrightness = msg.arg1;
                        DisplayPowerController.this.updatePowerState();
                        return;
                    case 7:
                        DisplayPowerController.this.mTemporaryAutoBrightnessAdjustment = Float.intBitsToFloat(msg.arg1);
                        DisplayPowerController.this.updatePowerState();
                        return;
                    case 8:
                        OppoBrightUtils unused4 = DisplayPowerController.mOppoBrightUtils;
                        OppoBrightUtils.mBrightnessBoost = 4;
                        return;
                    default:
                        return;
                }
            } else {
                DisplayPowerController.mOppoAppScaleHelper = new OppoAppScaleHelper(DisplayPowerController.this.mContext);
                DisplayPowerController.mOppoAppScaleHelper.initUpdateBroadcastReceiver();
            }
        }
    }

    private final class SettingsObserver extends ContentObserver {
        public SettingsObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange, Uri uri) {
            DisplayPowerController.this.handleSettingsChange(false);
        }
    }

    /* access modifiers changed from: private */
    public final class ScreenOnUnblocker implements WindowManagerPolicy.ScreenOnListener {
        private ScreenOnUnblocker() {
        }

        @Override // com.android.server.policy.WindowManagerPolicy.ScreenOnListener
        public void onScreenOn() {
            Message msg = DisplayPowerController.this.mHandler.obtainMessage(3, this);
            Slog.d(DisplayPowerController.TAG, "ScreenOnUnblocker, onScreenOn");
            if (DisplayPowerController.this.isScreenOnBlockedByFace()) {
                DisplayPowerController.this.mHandler.sendMessageDelayed(msg, 1000);
            } else {
                DisplayPowerController.this.mHandler.sendMessage(msg);
            }
        }
    }

    /* access modifiers changed from: private */
    public final class ScreenOffUnblocker implements WindowManagerPolicy.ScreenOffListener {
        private ScreenOffUnblocker() {
        }

        @Override // com.android.server.policy.WindowManagerPolicy.ScreenOffListener
        public void onScreenOff() {
            DisplayPowerController.this.mHandler.sendMessage(DisplayPowerController.this.mHandler.obtainMessage(4, this));
        }
    }

    /* access modifiers changed from: package-private */
    public void setAutoBrightnessLoggingEnabled(boolean enabled) {
        OppoAutomaticBrightnessController oppoAutomaticBrightnessController = this.mAutomaticBrightnessController;
        if (oppoAutomaticBrightnessController != null) {
            oppoAutomaticBrightnessController.setLoggingEnabled(enabled);
        }
    }

    @Override // com.android.server.display.whitebalance.DisplayWhiteBalanceController.Callbacks
    public void updateWhiteBalance() {
        sendUpdatePowerState();
    }

    /* access modifiers changed from: package-private */
    public void setDisplayWhiteBalanceLoggingEnabled(boolean enabled) {
        DisplayWhiteBalanceController displayWhiteBalanceController = this.mDisplayWhiteBalanceController;
        if (displayWhiteBalanceController != null) {
            displayWhiteBalanceController.setLoggingEnabled(enabled);
            this.mDisplayWhiteBalanceSettings.setLoggingEnabled(enabled);
        }
    }

    /* access modifiers changed from: package-private */
    public void setAmbientColorTemperatureOverride(float cct) {
        DisplayWhiteBalanceController displayWhiteBalanceController = this.mDisplayWhiteBalanceController;
        if (displayWhiteBalanceController != null) {
            displayWhiteBalanceController.setAmbientColorTemperatureOverride(cct);
            sendUpdatePowerState();
        }
    }

    /* access modifiers changed from: package-private */
    public void notifySendUpdatePowerState() {
        sendUpdatePowerState();
    }

    /* access modifiers changed from: package-private */
    public void callUpdatePowerState() {
        updatePowerState();
    }

    /* access modifiers changed from: package-private */
    public void setScreenOffBecauseOfProximityState(boolean state) {
        this.mScreenOffBecauseOfProximity = state;
    }

    /* access modifiers changed from: package-private */
    public void setWaitingForNegativeProximityState(boolean state) {
        this.mWaitingForNegativeProximity = state;
    }

    /* access modifiers changed from: package-private */
    public void sendOnProximityStateChangedWithWakelock(boolean positive) {
        if (!positive) {
            sendOnProximityNegativeWithWakelock();
        } else {
            sendOnProximityPositiveWithWakelock();
        }
    }

    /* access modifiers changed from: package-private */
    public void setOppoProximitySensorEnabled(boolean enable) {
        setProximitySensorEnabled(enable);
    }

    /* access modifiers changed from: package-private */
    public ScreenOnUnblocker getPendingScreenOnUnblocker() {
        return this.mPendingScreenOnUnblocker;
    }

    /* access modifiers changed from: package-private */
    public void sendMsgUnblockScreenOn(boolean needBlockedScreenOn) {
        if (!needBlockedScreenOn && this.mHandler.hasMessages(3)) {
            this.mHandler.removeMessages(3);
            Message msg = this.mHandler.obtainMessage(3, this.mPendingScreenOnUnblocker);
            msg.setAsynchronous(true);
            this.mHandler.sendMessage(msg);
            Slog.d(TAG, "MSG_SCREEN_ON_UNBLOCKED sended");
        }
    }

    /* access modifiers changed from: package-private */
    public void setBlackGestureWakeUp(boolean value) {
        this.mblackGestureWakeUp = value;
    }

    private int getmScreenBrightnessModeSetting() {
        return Settings.System.getIntForUser(this.mContext.getContentResolver(), "screen_brightness_mode", 0, -2);
    }

    private final class BrightnessReason {
        static final int ADJUSTMENT_AUTO = 2;
        static final int ADJUSTMENT_AUTO_TEMP = 1;
        static final int MODIFIER_DIMMED = 1;
        static final int MODIFIER_LOW_POWER = 2;
        static final int MODIFIER_MASK = 3;
        static final int REASON_AI_HBM = 10;
        static final int REASON_AUTOMATIC = 4;
        static final int REASON_BOOST = 9;
        static final int REASON_DOZE = 2;
        static final int REASON_DOZE_DEFAULT = 3;
        static final int REASON_MANUAL = 1;
        static final int REASON_MAX = 10;
        static final int REASON_OVERRIDE = 7;
        static final int REASON_SCREEN_OFF = 5;
        static final int REASON_TEMPORARY = 8;
        static final int REASON_UNKNOWN = 0;
        static final int REASON_VR = 6;
        public int modifier;
        public int reason;

        private BrightnessReason() {
        }

        public void set(BrightnessReason other) {
            int i = 0;
            setReason(other == null ? 0 : other.reason);
            if (other != null) {
                i = other.modifier;
            }
            setModifier(i);
        }

        public void setReason(int reason2) {
            if (reason2 < 0 || reason2 > 10) {
                Slog.w(DisplayPowerController.TAG, "brightness reason out of bounds: " + reason2);
                return;
            }
            this.reason = reason2;
            if (!DisplayPowerController.DEBUG) {
                boolean z = DisplayPowerController.DEBUG_PANIC;
            }
        }

        public void setModifier(int modifier2) {
            if ((modifier2 & -4) != 0) {
                Slog.w(DisplayPowerController.TAG, "brightness modifier out of bounds: 0x" + Integer.toHexString(modifier2));
                return;
            }
            this.modifier = modifier2;
        }

        public void addModifier(int modifier2) {
            setModifier(this.modifier | modifier2);
        }

        public boolean equals(Object obj) {
            if (obj == null || !(obj instanceof BrightnessReason)) {
                return false;
            }
            BrightnessReason other = (BrightnessReason) obj;
            if (other.reason == this.reason && other.modifier == this.modifier) {
                return true;
            }
            return false;
        }

        public String toString() {
            return toString(0);
        }

        public String toString(int adjustments) {
            StringBuilder sb = new StringBuilder();
            sb.append(reasonToString(this.reason));
            sb.append(" [");
            if ((adjustments & 1) != 0) {
                sb.append(" temp_adj");
            }
            if ((adjustments & 2) != 0) {
                sb.append(" auto_adj");
            }
            if ((this.modifier & 2) != 0) {
                sb.append(" low_pwr");
            }
            if ((this.modifier & 1) != 0) {
                sb.append(" dim");
            }
            int strlen = sb.length();
            if (sb.charAt(strlen - 1) == '[') {
                sb.setLength(strlen - 2);
            } else {
                sb.append(" ]");
            }
            return sb.toString();
        }

        private String reasonToString(int reason2) {
            switch (reason2) {
                case 1:
                    return "manual";
                case 2:
                    return "doze";
                case 3:
                    return "doze_default";
                case 4:
                    return "automatic";
                case 5:
                    return "screen_off";
                case 6:
                    return "vr";
                case 7:
                    return "override";
                case 8:
                    return "temporary";
                case 9:
                    return "boost";
                default:
                    return Integer.toString(reason2);
            }
        }
    }
}
