package com.android.server.display;

import android.app.ActivityManager;
import android.app.ActivityTaskManager;
import android.app.IActivityTaskManager;
import android.app.TaskStackListener;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.display.BrightnessConfiguration;
import android.hardware.display.DisplayManagerInternal;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.EventLog;
import android.util.MathUtils;
import android.util.Slog;
import android.util.Spline;
import android.util.TimeUtils;
import android.view.WindowManager;
import com.android.internal.os.BackgroundThread;
import com.android.server.EventLogTags;
import com.android.server.display.AIBrightnessHelper;
import com.android.server.pm.DumpState;
import com.android.server.usage.AppStandbyController;
import java.io.PrintWriter;
import java.util.HashMap;

/* access modifiers changed from: package-private */
public class OppoAutomaticBrightnessController {
    private static final int AMBIENT_LIGHT_LONG_HORIZON_MILLIS = 10000;
    private static final long AMBIENT_LIGHT_PREDICTION_TIME_MILLIS = 100;
    private static final int AMBIENT_LIGHT_SHORT_HORIZON_MILLIS = 2000;
    private static final float BRIGHTENING_LIGHT_HYSTERESIS = 0.0f;
    private static final int BRIGHTNESS_ADJUSTMENT_SAMPLE_DEBOUNCE_MILLIS = 10000;
    private static final float DARKENING_LIGHT_HYSTERESIS = 0.0f;
    public static boolean DEBUG = false;
    private static final boolean DEBUG_PRETEND_LIGHT_SENSOR_ABSENT = false;
    public static final float LIGHT_LUX_WATCH_POINT_ONE = 10000.0f;
    public static final float LIGHT_LUX_WATCH_POINT_THREE = 30000.0f;
    public static final float LIGHT_LUX_WATCH_POINT_TWO = 20000.0f;
    private static final int MSG_BRIGHTNESS_ADJUSTMENT_SAMPLE = 2;
    private static final int MSG_INVALIDATE_SHORT_TERM_MODEL = 3;
    private static final int MSG_REPORT_AUTO_BRIGHTNESS = 10;
    private static final int MSG_REPORT_AUTO_MANUL_BRIGHTNESS = 11;
    private static final int MSG_REPORT_MANUL_BRIGHTNESS = 12;
    private static final int MSG_ROTATE_STATE_TIMEOUT = 13;
    private static final int MSG_UPDATE_AMBIENT_LUX = 1;
    private static final int MSG_UPDATE_BRIGHTNESS_AFTER_PROXIMITY = 6;
    private static final int MSG_UPDATE_FOREGROUND_APP = 4;
    private static final int MSG_UPDATE_FOREGROUND_APP_SYNC = 5;
    private static final long SENSOR_LUX_BRIGHTNESS_EVENT_ID = 607;
    public static long SENSOR_LUX_BRIGHTNESS_REPORT_PERID = AppStandbyController.SettingsObserver.DEFAULT_SYSTEM_UPDATE_TIMEOUT;
    private static final int SHORT_TERM_MODEL_TIMEOUT_MILLIS = 30000;
    private static final String TAG = "AutomaticBrightnessController";
    private static final boolean USE_SCREEN_AUTO_BRIGHTNESS_ADJUSTMENT = true;
    /* access modifiers changed from: private */
    public static boolean mDeviceLandScape = false;
    /* access modifiers changed from: private */
    public static OppoBrightUtils mOppoBrightUtils;
    public static boolean mProximityNear = false;
    /* access modifiers changed from: private */
    public static boolean mScreenLandScape = false;
    private static long manulDurationTime = -1;
    private static long manulTime = -1;
    private static boolean mbPreProximityNear = false;
    private final long DEBOUNCE_THRESHOLD = 1000;
    private float SHORT_TERM_MODEL_THRESHOLD_RATIO = 0.6f;
    private boolean forceUpdate = false;
    /* access modifiers changed from: private */
    public float lux_beforemax = OppoBrightUtils.MIN_LUX_LIMITI;
    /* access modifiers changed from: private */
    public float lux_beforemin = OppoBrightUtils.MIN_LUX_LIMITI;
    /* access modifiers changed from: private */
    public float lux_max = OppoBrightUtils.MIN_LUX_LIMITI;
    /* access modifiers changed from: private */
    public float lux_min = OppoBrightUtils.MIN_LUX_LIMITI;
    /* access modifiers changed from: private */
    public IActivityTaskManager mActivityTaskManager;
    private float mAmbientBrighteningThreshold;
    private final HysteresisLevels mAmbientBrightnessThresholds;
    private float mAmbientDarkeningThreshold;
    private final int mAmbientLightHorizon;
    /* access modifiers changed from: private */
    public AmbientLightRingBuffer mAmbientLightRingBuffer;
    /* access modifiers changed from: private */
    public float mAmbientLux = 320.0f;
    private boolean mAmbientLuxValid;
    /* access modifiers changed from: private */
    public float mAutoAmbientLuxValue = OppoBrightUtils.MIN_LUX_LIMITI;
    private long mAutoDurationTime = -1;
    private long mAutoManulDurationTime = -1;
    private long mAutoManulTime = -1;
    public int mAutoRate = 0;
    private long mAutoTime = -1;
    private OppoAutoBrightControllerHelper mAutomaticHelper;
    /* access modifiers changed from: private */
    public int mBacklightStatus = 0;
    private final long mBrighteningLightDebounceConfig;
    private float mBrighteningLuxThreshold;
    private int mBrightnessAdjustmentSampleOldBrightness;
    private float mBrightnessAdjustmentSampleOldLux;
    private boolean mBrightnessAdjustmentSamplePending;
    private final BrightnessMappingStrategy mBrightnessMapper;
    /* access modifiers changed from: private */
    public final Callbacks mCallbacks;
    private BroadcastReceiver mConfigureChangeReceiver = new BroadcastReceiver() {
        /* class com.android.server.display.OppoAutomaticBrightnessController.AnonymousClass7 */
        private int mOrientation = 0;
        private boolean mPrevScreenLandeScape = false;

        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.CONFIGURATION_CHANGED".equals(intent.getAction())) {
                int tempOrient = ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getRotation();
                Slog.d(OppoAutomaticBrightnessController.TAG, "mConfigureChangeReceiver:tempOrient= " + tempOrient + ",mOrientation=" + this.mOrientation + ",mScreenLandScape=" + OppoAutomaticBrightnessController.mScreenLandScape);
                if (tempOrient != 4) {
                    long time = SystemClock.uptimeMillis();
                    if (tempOrient != this.mOrientation) {
                        this.mOrientation = tempOrient;
                        int i = this.mOrientation;
                        if (i == 1 || i == 3) {
                            boolean unused = OppoAutomaticBrightnessController.mScreenLandScape = true;
                        } else {
                            boolean unused2 = OppoAutomaticBrightnessController.mScreenLandScape = false;
                        }
                        if (this.mPrevScreenLandeScape && !OppoAutomaticBrightnessController.mScreenLandScape) {
                            long unused3 = OppoAutomaticBrightnessController.this.mScreenLandScapeTime = time;
                        }
                        if (this.mPrevScreenLandeScape != OppoAutomaticBrightnessController.mScreenLandScape) {
                            this.mPrevScreenLandeScape = OppoAutomaticBrightnessController.mScreenLandScape;
                            OppoAutomaticBrightnessController.this.proximityApply();
                        }
                    }
                }
            }
        }
    };
    private Context mContext;
    private int mCurrentLightSensorRate;
    private int mCurrentUser;
    /* access modifiers changed from: private */
    public final long mDarkeningLightDebounceConfig;
    private float mDarkeningLuxThreshold;
    /* access modifiers changed from: private */
    public float mDeltaLux = OppoBrightUtils.MIN_LUX_LIMITI;
    /* access modifiers changed from: private */
    public long mDeviceLandScapeTime = -1;
    /* access modifiers changed from: private */
    public final SensorEventListener mDeviceOriListener = new SensorEventListener() {
        /* class com.android.server.display.OppoAutomaticBrightnessController.AnonymousClass6 */
        private int mDeviceOritation = 0;
        private boolean mPrevDeviceLandScape = false;

        public void onSensorChanged(SensorEvent event) {
            Slog.d(OppoAutomaticBrightnessController.TAG, "mDeviceOriListener change : " + event.values[0] + ", mDeviceLandScape=" + OppoAutomaticBrightnessController.mDeviceLandScape);
            int tempOrition = (int) event.values[0];
            if (tempOrition != 4) {
                long time = SystemClock.uptimeMillis();
                if (tempOrition != this.mDeviceOritation) {
                    this.mDeviceOritation = tempOrition;
                    int i = this.mDeviceOritation;
                    if (i == 1 || i == 3) {
                        boolean unused = OppoAutomaticBrightnessController.mDeviceLandScape = true;
                    } else {
                        boolean unused2 = OppoAutomaticBrightnessController.mDeviceLandScape = false;
                    }
                    if (this.mPrevDeviceLandScape && !OppoAutomaticBrightnessController.mDeviceLandScape) {
                        long unused3 = OppoAutomaticBrightnessController.this.mDeviceLandScapeTime = time;
                    }
                    if (this.mPrevDeviceLandScape != OppoAutomaticBrightnessController.mDeviceLandScape) {
                        this.mPrevDeviceLandScape = OppoAutomaticBrightnessController.mDeviceLandScape;
                        OppoAutomaticBrightnessController.this.proximityApply();
                    }
                }
            }
        }

        public void onAccuracyChanged(Sensor arg0, int arg1) {
        }
    };
    /* access modifiers changed from: private */
    public Sensor mDeviceOriSensor;
    private int mDisplayPolicy = 0;
    private final float mDozeScaleFactor;
    private boolean mDozing;
    private int mForegroundAppCategory;
    /* access modifiers changed from: private */
    public String mForegroundAppPackageName;
    /* access modifiers changed from: private */
    public AutomaticBrightnessHandler mHandler;
    private final int mInitialLightSensorRate;
    private boolean mIsUsingFusionLight = SystemProperties.getBoolean("persist.sys.oppo.fusionlight", false);
    private boolean mIsUsingFusionLightNaruto = SystemProperties.getBoolean("persist.sys.oppo.fusionlight.naruto", false);
    private float mLastAutoBrightness = -1.0f;
    private float mLastAutoManualBrightness = -1.0f;
    private float mLastManulBrightness = -1.0f;
    /* access modifiers changed from: private */
    public float mLastObservedLux;
    private long mLastObservedLuxTime;
    private float mLastScreenAutoBrightnessGamma = 1.0f;
    private long mLastSensorLuxBrightnessReportTime = 0;
    public long mLightLuxWatchPointOneTotalTime = 0;
    public long mLightLuxWatchPointThreeTotalTime = 0;
    public long mLightLuxWatchPointTwoTotalTime = 0;
    /* access modifiers changed from: private */
    public final Sensor mLightSensor;
    private long mLightSensorEnableTime;
    public long mLightSensorEnableTotalTime;
    public boolean mLightSensorEnabled;
    /* access modifiers changed from: private */
    public final SensorEventListener mLightSensorListener = new SensorEventListener() {
        /* class com.android.server.display.OppoAutomaticBrightnessController.AnonymousClass3 */
        private float mPrevLux = OppoBrightUtils.MIN_LUX_LIMITI;
        private long mPrevTime = 0;

        public void onSensorChanged(SensorEvent event) {
            if (OppoAutomaticBrightnessController.this.mLightSensorEnabled) {
                long time = SystemClock.uptimeMillis();
                OppoBrightUtils unused = OppoAutomaticBrightnessController.mOppoBrightUtils;
                if (OppoBrightUtils.mBrightnessBoost == 1) {
                    this.mPrevTime = 0;
                    OppoBrightUtils unused2 = OppoAutomaticBrightnessController.mOppoBrightUtils;
                    OppoBrightUtils.mBrightnessBoost = 2;
                }
                if (event.values[0] <= OppoBrightUtils.MIN_LUX_LIMITI || event.values[0] >= 31000.0f || time - this.mPrevTime >= ((long) (OppoAutomaticBrightnessController.this.mNormalLightSensorRate / 4))) {
                    float lux = event.values[0];
                    if (lux >= 10000.0f && this.mPrevLux >= 10000.0f) {
                        long j = this.mPrevTime;
                        if (time >= j && time - j <= 1000) {
                            OppoAutomaticBrightnessController.this.mLightLuxWatchPointOneTotalTime += time - this.mPrevTime;
                        }
                    }
                    if (lux >= 20000.0f && this.mPrevLux >= 20000.0f) {
                        long j2 = this.mPrevTime;
                        if (time >= j2 && time - j2 <= 1000) {
                            OppoAutomaticBrightnessController.this.mLightLuxWatchPointTwoTotalTime += time - this.mPrevTime;
                        }
                    }
                    if (lux >= 30000.0f && this.mPrevLux >= 30000.0f) {
                        long j3 = this.mPrevTime;
                        if (time >= j3 && time - j3 <= 1000) {
                            OppoAutomaticBrightnessController.this.mLightLuxWatchPointThreeTotalTime += time - this.mPrevTime;
                        }
                    }
                    this.mPrevTime = time;
                    this.mPrevLux = lux;
                    OppoBrightUtils unused3 = OppoAutomaticBrightnessController.mOppoBrightUtils;
                    if (OppoBrightUtils.mBrightnessBoost == 2 || !OppoAutomaticBrightnessController.mProximityNear || OppoBrightUtils.DEBUG_PRETEND_PROX_SENSOR_ABSENT || lux >= OppoAutomaticBrightnessController.this.lux_max || OppoAutomaticBrightnessController.this.mBacklightStatus != 0) {
                        OppoBrightUtils unused4 = OppoAutomaticBrightnessController.mOppoBrightUtils;
                        if ((OppoBrightUtils.mSpecialBrightnessFlag & 1) != 0) {
                            OppoBrightUtils unused5 = OppoAutomaticBrightnessController.mOppoBrightUtils;
                            if (OppoBrightUtils.mScreenUnderLightSensorSupport && OppoAutomaticBrightnessController.this.mRotateStart) {
                                OppoAutomaticBrightnessController.access$2408(OppoAutomaticBrightnessController.this);
                                StringBuilder sb = new StringBuilder();
                                sb.append("wa game space in out skip lux =");
                                sb.append(lux);
                                sb.append("  count = ");
                                sb.append((int) OppoAutomaticBrightnessController.this.mSkipCount);
                                sb.append(" mRotateStart = ");
                                sb.append(OppoAutomaticBrightnessController.this.mRotateStart);
                                sb.append(" flag = ");
                                OppoBrightUtils unused6 = OppoAutomaticBrightnessController.mOppoBrightUtils;
                                sb.append(OppoBrightUtils.mSpecialBrightnessFlag);
                                Slog.d(OppoAutomaticBrightnessController.TAG, sb.toString());
                                return;
                            }
                        }
                        short unused7 = OppoAutomaticBrightnessController.this.mSkipCount = 0;
                        OppoBrightUtils unused8 = OppoAutomaticBrightnessController.mOppoBrightUtils;
                        OppoBrightUtils.mSpecialBrightnessFlag &= 16777214;
                        if (OppoAutomaticBrightnessController.mOppoBrightUtils.isAIBrightnessOpen()) {
                            OppoAutomaticBrightnessController.mOppoBrightUtils.setAIBrightnessLux(lux);
                            float unused9 = OppoAutomaticBrightnessController.this.mAmbientLux = lux;
                            if (OppoAutomaticBrightnessController.DEBUG) {
                                Slog.d(OppoAutomaticBrightnessController.TAG, "Set AI Brightness Lux:lux=" + lux);
                                return;
                            }
                            return;
                        }
                        if (OppoAutomaticBrightnessController.DEBUG) {
                            Slog.d(OppoAutomaticBrightnessController.TAG, "PowerMS L-Sensor Changed:lux=" + lux + ",lux_min = " + OppoAutomaticBrightnessController.this.lux_min + ",lux_max = " + OppoAutomaticBrightnessController.this.lux_max);
                        }
                        if (OppoAutomaticBrightnessController.DEBUG) {
                            Slog.d(OppoAutomaticBrightnessController.TAG, "mBacklightStatus=" + OppoAutomaticBrightnessController.this.mBacklightStatus + " marea=" + OppoAutomaticBrightnessController.this.marea + " mareabefore=" + OppoAutomaticBrightnessController.this.mareabefore);
                        }
                        if (OppoAutomaticBrightnessController.this.mBacklightStatus != 0 || lux < OppoAutomaticBrightnessController.this.lux_min || lux >= OppoAutomaticBrightnessController.this.lux_max) {
                            if (OppoAutomaticBrightnessController.this.mBacklightStatus == 1) {
                                if (lux >= OppoAutomaticBrightnessController.this.lux_beforemin && lux < OppoAutomaticBrightnessController.this.lux_beforemax) {
                                    int unused10 = OppoAutomaticBrightnessController.this.mBacklightStatus = 2;
                                    float unused11 = OppoAutomaticBrightnessController.this.mSensorLux = lux;
                                    OppoAutomaticBrightnessController.this.mAmbientLightRingBuffer.clear();
                                    OppoAutomaticBrightnessController.this.handleLightSensorEvent(time, OppoAutomaticBrightnessController.mOppoBrightUtils.findAmbientLux(OppoAutomaticBrightnessController.this.mareabefore));
                                    int unused12 = OppoAutomaticBrightnessController.this.mBacklightStatus = 0;
                                    OppoAutomaticBrightnessController oppoAutomaticBrightnessController = OppoAutomaticBrightnessController.this;
                                    float unused13 = oppoAutomaticBrightnessController.lux_min = oppoAutomaticBrightnessController.lux_beforemin;
                                    OppoAutomaticBrightnessController oppoAutomaticBrightnessController2 = OppoAutomaticBrightnessController.this;
                                    float unused14 = oppoAutomaticBrightnessController2.lux_max = oppoAutomaticBrightnessController2.lux_beforemax;
                                    OppoAutomaticBrightnessController oppoAutomaticBrightnessController3 = OppoAutomaticBrightnessController.this;
                                    int unused15 = oppoAutomaticBrightnessController3.marea = oppoAutomaticBrightnessController3.mareabefore;
                                    float unused16 = OppoAutomaticBrightnessController.this.mLastObservedLux = lux;
                                    if (OppoAutomaticBrightnessController.DEBUG) {
                                        Slog.d(OppoAutomaticBrightnessController.TAG, "lux area  lux_min = " + OppoAutomaticBrightnessController.this.lux_min + "  lux_max = " + OppoAutomaticBrightnessController.this.lux_max + "  sLux = " + OppoAutomaticBrightnessController.this.mSensorLux + "  mBacklightStatus = " + OppoAutomaticBrightnessController.this.mBacklightStatus + "  not stable to stable status and return.");
                                        return;
                                    }
                                    return;
                                } else if (lux >= OppoAutomaticBrightnessController.this.lux_min && lux < OppoAutomaticBrightnessController.this.lux_max) {
                                    if (OppoAutomaticBrightnessController.DEBUG) {
                                        Slog.d(OppoAutomaticBrightnessController.TAG, "not stable range and return.");
                                        return;
                                    }
                                    return;
                                }
                            }
                            OppoBrightUtils unused17 = OppoAutomaticBrightnessController.mOppoBrightUtils;
                            int i = OppoBrightUtils.mBrightnessBitsConfig;
                            OppoBrightUtils unused18 = OppoAutomaticBrightnessController.mOppoBrightUtils;
                            if (i == 1) {
                                OppoAutomaticBrightnessController.this.mAutoRate = OppoAutomaticBrightnessController.mOppoBrightUtils.caclurateRate(lux, OppoAutomaticBrightnessController.this.mLastObservedLux);
                            } else {
                                OppoAutomaticBrightnessController oppoAutomaticBrightnessController4 = OppoAutomaticBrightnessController.this;
                                float unused19 = oppoAutomaticBrightnessController4.mDeltaLux = lux - oppoAutomaticBrightnessController4.mLastObservedLux;
                            }
                            if (OppoAutomaticBrightnessController.DEBUG) {
                                Slog.d(OppoAutomaticBrightnessController.TAG, "lux=" + lux + " ,mLastObservedLux=" + OppoAutomaticBrightnessController.this.mLastObservedLux + " ,mAutoRate=" + OppoAutomaticBrightnessController.this.mAutoRate);
                            }
                            OppoAutomaticBrightnessController oppoAutomaticBrightnessController5 = OppoAutomaticBrightnessController.this;
                            float unused20 = oppoAutomaticBrightnessController5.mAutoAmbientLuxValue = oppoAutomaticBrightnessController5.mLastObservedLux;
                            float unused21 = OppoAutomaticBrightnessController.this.mLastObservedLux = lux;
                            float unused22 = OppoAutomaticBrightnessController.this.mSensorLux = lux;
                            int ii = 0;
                            while (true) {
                                OppoBrightUtils unused23 = OppoAutomaticBrightnessController.mOppoBrightUtils;
                                if (ii >= OppoBrightUtils.BRIGHTNESS_STEPS) {
                                    break;
                                }
                                OppoBrightUtils unused24 = OppoAutomaticBrightnessController.mOppoBrightUtils;
                                if (lux <= OppoBrightUtils.mAutoBrightnessLux[ii]) {
                                    OppoBrightUtils unused25 = OppoAutomaticBrightnessController.mOppoBrightUtils;
                                    lux = OppoBrightUtils.mAutoBrightnessLux[ii];
                                    OppoAutomaticBrightnessController oppoAutomaticBrightnessController6 = OppoAutomaticBrightnessController.this;
                                    OppoBrightUtils unused26 = OppoAutomaticBrightnessController.mOppoBrightUtils;
                                    float unused27 = oppoAutomaticBrightnessController6.lux_min = OppoBrightUtils.mAutoBrightnessLuxMinLimit[ii];
                                    OppoAutomaticBrightnessController oppoAutomaticBrightnessController7 = OppoAutomaticBrightnessController.this;
                                    OppoBrightUtils unused28 = OppoAutomaticBrightnessController.mOppoBrightUtils;
                                    float unused29 = oppoAutomaticBrightnessController7.lux_max = OppoBrightUtils.mAutoBrightnessLuxMaxLimit[ii];
                                    int unused30 = OppoAutomaticBrightnessController.this.marea = ii;
                                    break;
                                }
                                OppoBrightUtils unused31 = OppoAutomaticBrightnessController.mOppoBrightUtils;
                                float[] fArr = OppoBrightUtils.mAutoBrightnessLux;
                                OppoBrightUtils unused32 = OppoAutomaticBrightnessController.mOppoBrightUtils;
                                if (lux > fArr[OppoBrightUtils.BRIGHTNESS_STEPS - 1]) {
                                    OppoBrightUtils unused33 = OppoAutomaticBrightnessController.mOppoBrightUtils;
                                    float[] fArr2 = OppoBrightUtils.mAutoBrightnessLux;
                                    OppoBrightUtils unused34 = OppoAutomaticBrightnessController.mOppoBrightUtils;
                                    lux = fArr2[OppoBrightUtils.BRIGHTNESS_STEPS - 1];
                                    OppoAutomaticBrightnessController oppoAutomaticBrightnessController8 = OppoAutomaticBrightnessController.this;
                                    OppoBrightUtils unused35 = OppoAutomaticBrightnessController.mOppoBrightUtils;
                                    float[] fArr3 = OppoBrightUtils.mAutoBrightnessLuxMinLimit;
                                    OppoBrightUtils unused36 = OppoAutomaticBrightnessController.mOppoBrightUtils;
                                    float unused37 = oppoAutomaticBrightnessController8.lux_min = fArr3[OppoBrightUtils.BRIGHTNESS_STEPS - 1];
                                    OppoAutomaticBrightnessController oppoAutomaticBrightnessController9 = OppoAutomaticBrightnessController.this;
                                    OppoBrightUtils unused38 = OppoAutomaticBrightnessController.mOppoBrightUtils;
                                    float[] fArr4 = OppoBrightUtils.mAutoBrightnessLuxMaxLimit;
                                    OppoBrightUtils unused39 = OppoAutomaticBrightnessController.mOppoBrightUtils;
                                    float unused40 = oppoAutomaticBrightnessController9.lux_max = fArr4[OppoBrightUtils.BRIGHTNESS_STEPS - 1];
                                    OppoAutomaticBrightnessController oppoAutomaticBrightnessController10 = OppoAutomaticBrightnessController.this;
                                    OppoBrightUtils unused41 = OppoAutomaticBrightnessController.mOppoBrightUtils;
                                    int unused42 = oppoAutomaticBrightnessController10.marea = OppoBrightUtils.BRIGHTNESS_STEPS - 1;
                                    break;
                                }
                                ii++;
                            }
                            if (OppoAutomaticBrightnessController.DEBUG) {
                                Slog.d(OppoAutomaticBrightnessController.TAG, "lux area  lux_min = " + OppoAutomaticBrightnessController.this.lux_min + "  lux_max = " + OppoAutomaticBrightnessController.this.lux_max + "  mBacklightStatus = " + OppoAutomaticBrightnessController.this.mBacklightStatus + " handleLightSensorEvent lux = " + lux);
                            }
                            int unused43 = OppoAutomaticBrightnessController.this.mBacklightStatus = 1;
                            OppoAutomaticBrightnessController.this.handleLightSensorEvent(time, lux);
                        } else if (OppoAutomaticBrightnessController.DEBUG) {
                            Slog.d(OppoAutomaticBrightnessController.TAG, "in stable status and return.");
                        }
                    } else if (OppoAutomaticBrightnessController.DEBUG) {
                        Slog.d(OppoAutomaticBrightnessController.TAG, "DEBUG_PRETEND_PROX_SENSOR_ABSENT=" + OppoBrightUtils.DEBUG_PRETEND_PROX_SENSOR_ABSENT);
                    }
                } else if (OppoAutomaticBrightnessController.DEBUG) {
                    Slog.d(OppoAutomaticBrightnessController.TAG, "Skip onSensorChanaged, pre time = " + this.mPrevTime + ", now = " + time);
                }
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    private int mLightSensorWarmUpTimeConfig;
    private final Object mLock = new Object();
    private boolean mLoggingEnabled;
    private boolean mManulBrightnessSlide = false;
    private int mNewstate;
    /* access modifiers changed from: private */
    public final int mNormalLightSensorRate;
    private int mOldstate;
    /* access modifiers changed from: private */
    public PackageManager mPackageManager;
    /* access modifiers changed from: private */
    public int mPendingForegroundAppCategory;
    /* access modifiers changed from: private */
    public String mPendingForegroundAppPackageName;
    PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        /* class com.android.server.display.OppoAutomaticBrightnessController.AnonymousClass8 */

        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            Slog.d(OppoAutomaticBrightnessController.TAG, "PhoneState is changed state = " + state);
            if (state == 0) {
                OppoAutomaticBrightnessController.this.unRegisterProxSensorListener();
            } else if (state != 1) {
                if (state != 2) {
                }
            } else {
                OppoAutomaticBrightnessController.this.registerProxSensorListener();
            }
        }
    };
    /* access modifiers changed from: private */
    public Sensor mProximitySensor = null;
    /* access modifiers changed from: private */
    public long mProximitySensorChangeTime = 0;
    /* access modifiers changed from: private */
    public final SensorEventListener mProximitySensorListener = new SensorEventListener() {
        /* class com.android.server.display.OppoAutomaticBrightnessController.AnonymousClass4 */
        private boolean mPrevProximityNear = false;

        public void onSensorChanged(SensorEvent event) {
            long time = SystemClock.uptimeMillis();
            if (((double) event.values[0]) == 0.0d) {
                OppoAutomaticBrightnessController.mProximityNear = true;
                if (OppoAutomaticBrightnessController.DEBUG) {
                    Slog.d(OppoAutomaticBrightnessController.TAG, "Proximity is near");
                }
            } else {
                if (OppoAutomaticBrightnessController.mProximityNear) {
                    if (!OppoBrightUtils.DEBUG_PRETEND_PROX_SENSOR_ABSENT) {
                        OppoBrightUtils unused = OppoAutomaticBrightnessController.mOppoBrightUtils;
                        if (!OppoBrightUtils.mManualSetAutoBrightness) {
                            int unused2 = OppoAutomaticBrightnessController.this.mRecentLightSamples = 0;
                            OppoAutomaticBrightnessController.this.mAmbientLightRingBuffer.clear();
                            OppoAutomaticBrightnessController.this.mHandler.removeMessages(1);
                            float unused3 = OppoAutomaticBrightnessController.this.lux_min = OppoBrightUtils.MIN_LUX_LIMITI;
                            float unused4 = OppoAutomaticBrightnessController.this.lux_max = OppoBrightUtils.MIN_LUX_LIMITI;
                        }
                    }
                    if (OppoAutomaticBrightnessController.DEBUG) {
                        Slog.d(OppoAutomaticBrightnessController.TAG, "Proximity is far");
                    }
                    OppoAutomaticBrightnessController.this.mHandler.removeMessages(6);
                    OppoAutomaticBrightnessController.this.mHandler.sendEmptyMessageDelayed(6, OppoAutomaticBrightnessController.this.mDarkeningLightDebounceConfig);
                }
                OppoAutomaticBrightnessController.mProximityNear = false;
            }
            if (this.mPrevProximityNear && !OppoAutomaticBrightnessController.mProximityNear) {
                long unused5 = OppoAutomaticBrightnessController.this.mProximitySensorChangeTime = time;
            }
            this.mPrevProximityNear = OppoAutomaticBrightnessController.mProximityNear;
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    /* access modifiers changed from: private */
    public int mRecentLightSamples;
    private HashMap<String, String> mReportMap;
    private final boolean mResetAmbientLuxAfterWarmUpConfig;
    /* access modifiers changed from: private */
    public boolean mRotateStart;
    public int mScreenAutoBrightness = -1;
    private float mScreenAutoBrightnessAdjustment = OppoBrightUtils.MIN_LUX_LIMITI;
    private Spline mScreenAutoBrightnessSpline;
    private float mScreenBrighteningThreshold;
    private final int mScreenBrightnessRangeMaximum;
    private final int mScreenBrightnessRangeMinimum;
    private final HysteresisLevels mScreenBrightnessThresholds;
    private float mScreenDarkeningThreshold;
    /* access modifiers changed from: private */
    public long mScreenLandScapeTime = -1;
    /* access modifiers changed from: private */
    public float mSensorLux = OppoBrightUtils.MIN_LUX_LIMITI;
    /* access modifiers changed from: private */
    public final SensorManager mSensorManager;
    private float mShortTermModelAnchor;
    private long mShortTermModelTimeout;
    private boolean mShortTermModelValid;
    /* access modifiers changed from: private */
    public short mSkipCount = 0;
    private long mSleepStartTime = -1;
    private long mSleepTime = -1;
    private boolean mStartManual = false;
    private TaskStackListenerImpl mTaskStackListener;
    /* access modifiers changed from: private */
    public final SensorEventListener mTelePhoneProximityListener = new SensorEventListener() {
        /* class com.android.server.display.OppoAutomaticBrightnessController.AnonymousClass9 */
        private boolean prevNear = false;
        private boolean proxNear = false;

        public void onSensorChanged(SensorEvent sensorEvent) {
            long time = SystemClock.uptimeMillis();
            if (((double) sensorEvent.values[0]) == 0.0d) {
                this.proxNear = true;
                if (OppoAutomaticBrightnessController.DEBUG) {
                    Slog.d(OppoAutomaticBrightnessController.TAG, "telePhone Proximity is near");
                }
            } else {
                this.proxNear = false;
                if (OppoAutomaticBrightnessController.DEBUG) {
                    Slog.d(OppoAutomaticBrightnessController.TAG, "telePhone Proximity is far");
                }
            }
            if (this.prevNear && !this.proxNear) {
                long unused = OppoAutomaticBrightnessController.this.mVirProxSensorChangeTime = time;
            }
            this.prevNear = this.proxNear;
        }

        public void onAccuracyChanged(Sensor sensor, int i) {
        }
    };
    private long mUserSwitchTime = -1;
    /* access modifiers changed from: private */
    public long mVirProxSensorChangeTime = 0;
    private int mWAManualBrightness = -1;
    private int mWASensorLux = -1;
    private long mWASleepTime = -1;
    private boolean mWAState = false;
    private boolean mWAisOpen = false;
    private final int mWeightingIntercept;
    /* access modifiers changed from: private */
    public int marea = -1;
    /* access modifiers changed from: private */
    public int mareabefore = -1;

    /* access modifiers changed from: package-private */
    public interface Callbacks {
        void updateBrightness();
    }

    static /* synthetic */ short access$2408(OppoAutomaticBrightnessController x0) {
        short s = x0.mSkipCount;
        x0.mSkipCount = (short) (s + 1);
        return s;
    }

    public OppoAutomaticBrightnessController(Callbacks callbacks, Looper looper, SensorManager sensorManager, Sensor lightSensor, BrightnessMappingStrategy mapper, int lightSensorWarmUpTime, int brightnessMin, int brightnessMax, float dozeScaleFactor, int lightSensorRate, int initialLightSensorRate, long brighteningLightDebounceConfig, long darkeningLightDebounceConfig, boolean resetAmbientLuxAfterWarmUpConfig, HysteresisLevels ambientBrightnessThresholds, HysteresisLevels screenBrightnessThresholds, long shortTermModelTimeout, PackageManager packageManager) {
        this.mCallbacks = callbacks;
        this.mSensorManager = sensorManager;
        this.mBrightnessMapper = mapper;
        this.mScreenBrightnessRangeMinimum = brightnessMin;
        this.mScreenBrightnessRangeMaximum = brightnessMax;
        this.mLightSensorWarmUpTimeConfig = lightSensorWarmUpTime;
        this.mDozeScaleFactor = dozeScaleFactor;
        this.mNormalLightSensorRate = lightSensorRate;
        this.mInitialLightSensorRate = initialLightSensorRate;
        this.mCurrentLightSensorRate = -1;
        this.mBrighteningLightDebounceConfig = brighteningLightDebounceConfig;
        this.mDarkeningLightDebounceConfig = darkeningLightDebounceConfig;
        this.mResetAmbientLuxAfterWarmUpConfig = resetAmbientLuxAfterWarmUpConfig;
        this.mAmbientLightHorizon = 10000;
        this.mWeightingIntercept = 10000;
        this.mAmbientBrightnessThresholds = ambientBrightnessThresholds;
        this.mScreenBrightnessThresholds = screenBrightnessThresholds;
        this.mShortTermModelTimeout = shortTermModelTimeout;
        this.mShortTermModelValid = true;
        this.mShortTermModelAnchor = -1.0f;
        this.mHandler = new AutomaticBrightnessHandler(looper);
        this.mAmbientLightRingBuffer = new AmbientLightRingBuffer((long) this.mNormalLightSensorRate, this.mAmbientLightHorizon);
        if (!this.mIsUsingFusionLight || !this.mIsUsingFusionLightNaruto) {
            this.mLightSensor = lightSensor;
        } else {
            this.mLightSensor = this.mSensorManager.getDefaultSensor(33171099);
        }
        this.mActivityTaskManager = ActivityTaskManager.getService();
        this.mPackageManager = packageManager;
        this.mTaskStackListener = new TaskStackListenerImpl();
        this.mForegroundAppPackageName = null;
        this.mPendingForegroundAppPackageName = null;
        this.mForegroundAppCategory = -1;
        this.mPendingForegroundAppCategory = -1;
        this.mCurrentUser = ActivityManager.getCurrentUser();
    }

    public void init(Context context) {
        mOppoBrightUtils = OppoBrightUtils.getInstance();
        OppoBrightUtils oppoBrightUtils = mOppoBrightUtils;
        DEBUG = OppoBrightUtils.DEBUG;
        this.mContext = context;
        this.mProximitySensor = this.mSensorManager.getDefaultSensor(8);
        this.mAutomaticHelper = new OppoAutoBrightControllerHelper(this, context, this.mHandler);
        this.mScreenAutoBrightnessSpline = OppoBrightUtils.createAutoBrightnessSpline(mOppoBrightUtils.readAutoBrightnessLuxConfig(), mOppoBrightUtils.readAutoBrightnessConfig());
        OppoBrightUtils oppoBrightUtils2 = mOppoBrightUtils;
        if (OppoBrightUtils.sUseRotateSupport) {
            this.mDeviceOriSensor = this.mSensorManager.getDefaultSensor(27);
            registerRotationStateReceiver();
            registerPhoneStateListener();
        } else {
            OppoBrightUtils oppoBrightUtils3 = mOppoBrightUtils;
            if (OppoBrightUtils.sPSensorNotSupport) {
                registerPhoneStateListener();
            }
        }
        setAIBrightnessListener();
    }

    public void setProximityNearPara() {
        if (!OppoBrightUtils.DEBUG_PRETEND_PROX_SENSOR_ABSENT) {
            OppoBrightUtils oppoBrightUtils = mOppoBrightUtils;
            if (!OppoBrightUtils.mManualSetAutoBrightness) {
                this.mAmbientLuxValid = false;
                this.mRecentLightSamples = 0;
                this.mAmbientLightRingBuffer.clear();
                this.mHandler.removeMessages(1);
                this.lux_min = OppoBrightUtils.MIN_LUX_LIMITI;
                this.lux_max = OppoBrightUtils.MIN_LUX_LIMITI;
                this.mBacklightStatus = 2;
            }
        }
        if (DEBUG) {
            Slog.d(TAG, "Proximity is far");
        }
    }

    public long getDarkeningLightDebounceConfigTime() {
        return this.mDarkeningLightDebounceConfig;
    }

    public void callbackUpdateBrightness() {
        this.mCallbacks.updateBrightness();
    }

    public void zeroHandlerSetPara() {
        long time = SystemClock.uptimeMillis();
        OppoBrightUtils oppoBrightUtils = mOppoBrightUtils;
        this.lux_min = OppoBrightUtils.mAutoBrightnessLuxMinLimit[0];
        OppoBrightUtils oppoBrightUtils2 = mOppoBrightUtils;
        this.lux_max = OppoBrightUtils.mAutoBrightnessLuxMaxLimit[0];
        float f = this.mLastObservedLux;
        this.mDeltaLux = f;
        this.mAutoRate = mOppoBrightUtils.caclurateRate(OppoBrightUtils.MIN_LUX_LIMITI, f);
        this.mLastObservedLux = OppoBrightUtils.MIN_LUX_LIMITI;
        handleLightSensorEvent(time, OppoBrightUtils.MIN_LUX_LIMITI);
    }

    public void resetLightParamsScreenOff() {
        this.mAmbientLuxValid = false;
    }

    public boolean setLoggingEnabled(boolean loggingEnabled) {
        if (this.mLoggingEnabled == loggingEnabled) {
            return false;
        }
        this.mBrightnessMapper.setLoggingEnabled(loggingEnabled);
        this.mLoggingEnabled = loggingEnabled;
        return true;
    }

    public int getAutomaticScreenBrightness() {
        if (this.mDisplayPolicy == 1) {
            return (int) (((float) this.mScreenAutoBrightness) * this.mDozeScaleFactor);
        }
        return this.mScreenAutoBrightness;
    }

    public boolean hasValidAmbientLux() {
        return this.mAmbientLuxValid;
    }

    public float getAutomaticScreenBrightnessAdjustment() {
        return this.mBrightnessMapper.getAutoBrightnessAdjustment();
    }

    public void configure(boolean enable, float adjustment, boolean dozing, boolean userInitiatedChange) {
        this.mDozing = dozing;
        boolean changed = setLightSensorEnabled(enable && !dozing);
        if (enable && !dozing && userInitiatedChange) {
            prepareBrightnessAdjustmentSample();
        }
        if ((changed || setScreenAutoBrightnessAdjustment(adjustment)) || this.forceUpdate) {
            updateAutoBrightness(false);
            this.forceUpdate = false;
        }
    }

    public boolean hasUserDataPoints() {
        return this.mBrightnessMapper.hasUserDataPoints();
    }

    public boolean isDefaultConfig() {
        return this.mBrightnessMapper.isDefaultConfig();
    }

    public BrightnessConfiguration getDefaultConfig() {
        return this.mBrightnessMapper.getDefaultConfig();
    }

    private boolean setDisplayPolicy(int policy) {
        if (this.mDisplayPolicy == policy) {
            return false;
        }
        int oldPolicy = this.mDisplayPolicy;
        this.mDisplayPolicy = policy;
        if (DEBUG) {
            Slog.d(TAG, "Display policy transitioning from " + oldPolicy + " to " + policy);
        }
        if (!isInteractivePolicy(policy) && isInteractivePolicy(oldPolicy)) {
            this.mHandler.sendEmptyMessageDelayed(3, 30000);
            return true;
        } else if (!isInteractivePolicy(policy) || isInteractivePolicy(oldPolicy)) {
            return true;
        } else {
            this.mHandler.removeMessages(3);
            return true;
        }
    }

    private static boolean isInteractivePolicy(int policy) {
        return policy == 3 || policy == 2 || policy == 4;
    }

    private boolean setScreenBrightnessByUser(float brightness) {
        if (!this.mAmbientLuxValid) {
            return false;
        }
        this.mBrightnessMapper.addUserDataPoint(this.mAmbientLux, brightness);
        this.mShortTermModelValid = true;
        this.mShortTermModelAnchor = this.mAmbientLux;
        if (DEBUG) {
            Slog.d(TAG, "ShortTermModel: anchor=" + this.mShortTermModelAnchor);
        }
        return true;
    }

    public void resetShortTermModel() {
        this.mBrightnessMapper.clearUserDataPoints();
        this.mShortTermModelValid = true;
        this.mShortTermModelAnchor = -1.0f;
    }

    /* access modifiers changed from: private */
    public void invalidateShortTermModel() {
        if (DEBUG) {
            Slog.d(TAG, "ShortTermModel: invalidate user data");
        }
        this.mShortTermModelValid = false;
    }

    public boolean setBrightnessConfiguration(BrightnessConfiguration configuration) {
        if (!this.mBrightnessMapper.setBrightnessConfiguration(configuration)) {
            return false;
        }
        resetShortTermModel();
        return true;
    }

    public void dump(PrintWriter pw) {
        pw.println();
        pw.println("Automatic Brightness Controller Configuration:");
        pw.println("  mScreenBrightnessRangeMinimum=" + this.mScreenBrightnessRangeMinimum);
        pw.println("  mScreenBrightnessRangeMaximum=" + this.mScreenBrightnessRangeMaximum);
        pw.println("  mDozeScaleFactor=" + this.mDozeScaleFactor);
        pw.println("  mInitialLightSensorRate=" + this.mInitialLightSensorRate);
        pw.println("  mNormalLightSensorRate=" + this.mNormalLightSensorRate);
        pw.println("  mLightSensorWarmUpTimeConfig=" + this.mLightSensorWarmUpTimeConfig);
        pw.println("  mBrighteningLightDebounceConfig=" + this.mBrighteningLightDebounceConfig);
        pw.println("  mDarkeningLightDebounceConfig=" + this.mDarkeningLightDebounceConfig);
        pw.println("  mResetAmbientLuxAfterWarmUpConfig=" + this.mResetAmbientLuxAfterWarmUpConfig);
        pw.println("  mAmbientLightHorizon=" + this.mAmbientLightHorizon);
        pw.println("  mWeightingIntercept=" + this.mWeightingIntercept);
        pw.println();
        pw.println("Automatic Brightness Controller State:");
        pw.println("  mLightSensor=" + this.mLightSensor);
        pw.println("  mLightSensorEnabled=" + this.mLightSensorEnabled);
        pw.println("  mLightSensorEnableTime=" + TimeUtils.formatUptime(this.mLightSensorEnableTime));
        pw.println("  mCurrentLightSensorRate=" + this.mCurrentLightSensorRate);
        pw.println("  mAmbientLux=" + this.mAmbientLux);
        pw.println("  mAmbientLuxValid=" + this.mAmbientLuxValid);
        pw.println("  mAmbientBrighteningThreshold=" + this.mAmbientBrighteningThreshold);
        pw.println("  mAmbientDarkeningThreshold=" + this.mAmbientDarkeningThreshold);
        pw.println("  mScreenBrighteningThreshold=" + this.mScreenBrighteningThreshold);
        pw.println("  mScreenDarkeningThreshold=" + this.mScreenDarkeningThreshold);
        pw.println("  mLastObservedLux=" + this.mLastObservedLux);
        pw.println("  mLastObservedLuxTime=" + TimeUtils.formatUptime(this.mLastObservedLuxTime));
        pw.println("  mRecentLightSamples=" + this.mRecentLightSamples);
        pw.println("  mAmbientLightRingBuffer=" + this.mAmbientLightRingBuffer);
        pw.println("  mScreenAutoBrightness=" + this.mScreenAutoBrightness);
        pw.println("  mDisplayPolicy=" + DisplayManagerInternal.DisplayPowerRequest.policyToString(this.mDisplayPolicy));
        pw.println("  mShortTermModelTimeout=" + this.mShortTermModelTimeout);
        pw.println("  mShortTermModelAnchor=" + this.mShortTermModelAnchor);
        pw.println("  mShortTermModelValid=" + this.mShortTermModelValid);
        pw.println("  mBrightnessAdjustmentSamplePending=" + this.mBrightnessAdjustmentSamplePending);
        pw.println("  mBrightnessAdjustmentSampleOldLux=" + this.mBrightnessAdjustmentSampleOldLux);
        pw.println("  mBrightnessAdjustmentSampleOldBrightness=" + this.mBrightnessAdjustmentSampleOldBrightness);
        pw.println("  mForegroundAppPackageName=" + this.mForegroundAppPackageName);
        pw.println("  mPendingForegroundAppPackageName=" + this.mPendingForegroundAppPackageName);
        pw.println("  mForegroundAppCategory=" + this.mForegroundAppCategory);
        pw.println("  mPendingForegroundAppCategory=" + this.mPendingForegroundAppCategory);
        pw.println();
        this.mBrightnessMapper.dump(pw);
        pw.println();
        this.mAmbientBrightnessThresholds.dump(pw);
        this.mScreenBrightnessThresholds.dump(pw);
        DEBUG = OppoBrightUtils.DEBUG;
        if (mOppoBrightUtils != null) {
            pw.println();
            mOppoBrightUtils.dump(pw);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0117, code lost:
        if (com.android.server.display.OppoBrightUtils.mBrightnessBoost == 2) goto L_0x0119;
     */
    private boolean setLightSensorEnabled(boolean enable) {
        if (enable != this.mLightSensorEnabled) {
            Slog.d(TAG, "setLightSensorEnabled: enable=" + enable + ", mLightSensorEnabled=" + this.mLightSensorEnabled + ", mAmbientLuxValid=" + this.mAmbientLuxValid + ", mResetAmbientLuxAfterWarmUpConfig=" + this.mResetAmbientLuxAfterWarmUpConfig);
        }
        DisplayPowerController.mScreenDimQuicklyDark = false;
        if (enable) {
            OppoBrightUtils oppoBrightUtils = mOppoBrightUtils;
            if (OppoBrightUtils.mClearManualBritness) {
                OppoBrightUtils oppoBrightUtils2 = mOppoBrightUtils;
                OppoBrightUtils.mClearManualBritness = false;
                this.mManulBrightnessSlide = false;
                OppoBrightUtils oppoBrightUtils3 = mOppoBrightUtils;
                OppoBrightUtils.mManualBrightness = 0;
                this.mStartManual = false;
                OppoBrightUtils oppoBrightUtils4 = mOppoBrightUtils;
                OppoBrightUtils.mManualSetAutoBrightness = false;
                OppoBrightUtils oppoBrightUtils5 = mOppoBrightUtils;
                OppoBrightUtils.mManulAtAmbientLux = OppoBrightUtils.MIN_LUX_LIMITI;
                OppoBrightUtils oppoBrightUtils6 = mOppoBrightUtils;
                OppoBrightUtils.mManualBrightnessBackup = 0;
                OppoBrightUtils oppoBrightUtils7 = mOppoBrightUtils;
                OppoBrightUtils.mManualAmbientLuxBackup = OppoBrightUtils.MIN_LUX_LIMITI;
                OppoBrightUtils oppoBrightUtils8 = mOppoBrightUtils;
                OppoBrightUtils.mManualBrightness = 0;
                OppoBrightUtils oppoBrightUtils9 = mOppoBrightUtils;
                OppoBrightUtils.mManulAtAmbientLux = OppoBrightUtils.MIN_LUX_LIMITI;
                OppoBrightUtils oppoBrightUtils10 = mOppoBrightUtils;
                OppoBrightUtils.mRefManualToAutoBritness = 0;
            }
            Settings.System.putFloatForUser(this.mContext.getContentResolver(), "screen_auto_brightness_adj", OppoBrightUtils.MIN_LUX_LIMITI, -2);
            if (!this.mLightSensorEnabled) {
                this.mLightSensorEnabled = true;
                this.mLightSensorEnableTime = SystemClock.uptimeMillis();
                this.mCurrentLightSensorRate = this.mInitialLightSensorRate;
                registerForegroundAppUpdater();
                OppoBrightUtils oppoBrightUtils11 = mOppoBrightUtils;
                OppoBrightUtils.mUseAutoBrightness = true;
                OppoBrightUtils oppoBrightUtils12 = mOppoBrightUtils;
                if (OppoBrightUtils.mBrightnessBoost == 1) {
                    Thread thread = new Thread(new Runnable() {
                        /* class com.android.server.display.OppoAutomaticBrightnessController.AnonymousClass1 */

                        public void run() {
                            OppoAutomaticBrightnessController.this.mSensorManager.registerListener(OppoAutomaticBrightnessController.this.mLightSensorListener, OppoAutomaticBrightnessController.this.mLightSensor, OppoAutomaticBrightnessController.this.mNormalLightSensorRate * 1000, OppoAutomaticBrightnessController.this.mHandler);
                            OppoBrightUtils unused = OppoAutomaticBrightnessController.mOppoBrightUtils;
                            if (OppoBrightUtils.sUseRotateSupport) {
                                OppoAutomaticBrightnessController.this.mSensorManager.registerListener(OppoAutomaticBrightnessController.this.mDeviceOriListener, OppoAutomaticBrightnessController.this.mDeviceOriSensor, 1, OppoAutomaticBrightnessController.this.mHandler);
                                return;
                            }
                            OppoBrightUtils unused2 = OppoAutomaticBrightnessController.mOppoBrightUtils;
                            if (!OppoBrightUtils.sPSensorNotSupport) {
                                OppoAutomaticBrightnessController.this.mSensorManager.registerListener(OppoAutomaticBrightnessController.this.mProximitySensorListener, OppoAutomaticBrightnessController.this.mProximitySensor, OppoAutomaticBrightnessController.this.mNormalLightSensorRate * 1000, OppoAutomaticBrightnessController.this.mHandler);
                            }
                        }
                    }, "LightSensorEnableThread");
                    thread.setPriority(10);
                    thread.start();
                } else {
                    this.mSensorManager.registerListener(this.mLightSensorListener, this.mLightSensor, this.mNormalLightSensorRate * 1000, this.mHandler);
                    OppoBrightUtils oppoBrightUtils13 = mOppoBrightUtils;
                    if (OppoBrightUtils.sUseRotateSupport) {
                        this.mSensorManager.registerListener(this.mDeviceOriListener, this.mDeviceOriSensor, 1, this.mHandler);
                    } else {
                        OppoBrightUtils oppoBrightUtils14 = mOppoBrightUtils;
                        if (!OppoBrightUtils.sPSensorNotSupport) {
                            this.mSensorManager.registerListener(this.mProximitySensorListener, this.mProximitySensor, this.mNormalLightSensorRate * 1000, this.mHandler);
                        }
                    }
                    if (DEBUG) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("setLightSensorEnabled     mBrightnessBoost = ");
                        OppoBrightUtils oppoBrightUtils15 = mOppoBrightUtils;
                        sb.append(OppoBrightUtils.mBrightnessBoost);
                        Slog.e(TAG, sb.toString());
                    }
                    OppoBrightUtils oppoBrightUtils16 = mOppoBrightUtils;
                    if (OppoBrightUtils.mBrightnessBoost != 4) {
                        OppoBrightUtils oppoBrightUtils17 = mOppoBrightUtils;
                        if (OppoBrightUtils.mBrightnessBoost != 0) {
                            OppoBrightUtils oppoBrightUtils18 = mOppoBrightUtils;
                        }
                    }
                    OppoBrightUtils oppoBrightUtils19 = mOppoBrightUtils;
                    OppoBrightUtils.mBrightnessBoost = 3;
                }
                this.lux_min = OppoBrightUtils.MIN_LUX_LIMITI;
                this.lux_max = OppoBrightUtils.MIN_LUX_LIMITI;
                this.lux_beforemin = OppoBrightUtils.MIN_LUX_LIMITI;
                this.lux_beforemax = OppoBrightUtils.MIN_LUX_LIMITI;
                this.mBacklightStatus = 1;
                setAIBrightnessListener();
                if (OppoBrightUtils.REPORT_FEATURE_ON) {
                    if (this.mOldstate == 2 && this.mNewstate == 2) {
                        OppoBrightUtils oppoBrightUtils20 = mOppoBrightUtils;
                        if (OppoBrightUtils.DEBUG_REPORT) {
                            Slog.d(TAG, "on to on report manul brightness");
                        }
                        manulDurationTime = System.currentTimeMillis() - manulTime;
                        this.mLastManulBrightness = (float) this.mScreenAutoBrightness;
                        if (manulDurationTime > 10000) {
                            Slog.d(TAG, "on to on report manul brightness, send message,manulDurationTime=" + manulDurationTime);
                            this.mHandler.sendMessage(this.mHandler.obtainMessage(12));
                        }
                    }
                    this.mAutoTime = System.currentTimeMillis();
                    this.mAutoAmbientLuxValue = this.mLastObservedLux;
                    manulTime = 0;
                    OppoBrightUtils oppoBrightUtils21 = mOppoBrightUtils;
                    if (OppoBrightUtils.DEBUG_REPORT) {
                        Slog.d(TAG, "mAutoTime=" + this.mAutoTime);
                    }
                }
                return true;
            }
        } else if (this.mLightSensorEnabled) {
            if (Settings.System.getIntForUser(this.mContext.getContentResolver(), "screen_brightness_mode", 0, -2) == 0) {
                OppoBrightUtils oppoBrightUtils22 = mOppoBrightUtils;
                OppoBrightUtils.mManualBrightnessBackup = 0;
                OppoBrightUtils oppoBrightUtils23 = mOppoBrightUtils;
                OppoBrightUtils.mManualAmbientLuxBackup = OppoBrightUtils.MIN_LUX_LIMITI;
                OppoBrightUtils oppoBrightUtils24 = mOppoBrightUtils;
                OppoBrightUtils.mManualBrightness = 0;
                OppoBrightUtils oppoBrightUtils25 = mOppoBrightUtils;
                OppoBrightUtils.mManulAtAmbientLux = OppoBrightUtils.MIN_LUX_LIMITI;
            }
            this.mLightSensorEnabled = false;
            this.mAmbientLuxValid = !this.mResetAmbientLuxAfterWarmUpConfig;
            this.mRecentLightSamples = 0;
            this.mAmbientLightRingBuffer.clear();
            this.mCurrentLightSensorRate = -1;
            this.mHandler.removeMessages(1);
            unregisterForegroundAppUpdater();
            this.mSensorManager.unregisterListener(this.mLightSensorListener);
            OppoBrightUtils oppoBrightUtils26 = mOppoBrightUtils;
            if (OppoBrightUtils.sUseRotateSupport) {
                this.mSensorManager.unregisterListener(this.mDeviceOriListener);
            }
            this.mManulBrightnessSlide = false;
            OppoBrightUtils oppoBrightUtils27 = mOppoBrightUtils;
            OppoBrightUtils.mManualBrightness = 0;
            this.mStartManual = false;
            OppoBrightUtils oppoBrightUtils28 = mOppoBrightUtils;
            OppoBrightUtils.mUseAutoBrightness = false;
            OppoBrightUtils oppoBrightUtils29 = mOppoBrightUtils;
            OppoBrightUtils.mManualSetAutoBrightness = false;
            mProximityNear = false;
            OppoBrightUtils oppoBrightUtils30 = mOppoBrightUtils;
            OppoBrightUtils.mManulAtAmbientLux = OppoBrightUtils.MIN_LUX_LIMITI;
            OppoBrightUtils oppoBrightUtils31 = mOppoBrightUtils;
            if (OppoBrightUtils.sUseRotateSupport) {
                this.mSensorManager.unregisterListener(this.mDeviceOriListener);
            } else {
                OppoBrightUtils oppoBrightUtils32 = mOppoBrightUtils;
                if (!OppoBrightUtils.sPSensorNotSupport) {
                    this.mSensorManager.unregisterListener(this.mProximitySensorListener);
                }
            }
            DisplayPowerController.mQuickDarkToBright = false;
            this.mBacklightStatus = 0;
            if (OppoBrightUtils.REPORT_FEATURE_ON) {
                if (this.mOldstate == 2 && this.mNewstate == 2) {
                    OppoBrightUtils oppoBrightUtils33 = mOppoBrightUtils;
                    if (OppoBrightUtils.DEBUG_REPORT) {
                        Slog.d(TAG, "on to on and report auto, begin manual");
                    }
                    manulTime = System.currentTimeMillis();
                }
                this.mAutoDurationTime = System.currentTimeMillis() - this.mAutoTime;
                this.mLastAutoBrightness = (float) this.mScreenAutoBrightness;
                if (this.mAutoDurationTime > 10000) {
                    OppoBrightUtils oppoBrightUtils34 = mOppoBrightUtils;
                    if (OppoBrightUtils.DEBUG_REPORT) {
                        Slog.d(TAG, "on to on and report auto, begin manual, mAutoDurationTime=" + this.mAutoDurationTime);
                    }
                    this.mHandler.sendMessage(this.mHandler.obtainMessage(10));
                }
                this.mAutoTime = 0;
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    public void handleLightSensorEvent(long time, float lux) {
        Trace.traceCounter(131072, "ALS", (int) lux);
        this.mHandler.removeMessages(1);
        applyLightSensorMeasurement(time, lux);
        updateAmbientLux(time);
    }

    private void applyLightSensorMeasurement(long time, float lux) {
        this.mRecentLightSamples++;
        this.mAmbientLightRingBuffer.prune(time - ((long) this.mAmbientLightHorizon));
        this.mAmbientLightRingBuffer.push(time, lux);
    }

    private void adjustLightSensorRate(int lightSensorRate) {
        if (lightSensorRate != this.mCurrentLightSensorRate) {
            if (DEBUG) {
                Slog.d(TAG, "adjustLightSensorRate: previousRate=" + this.mCurrentLightSensorRate + ", currentRate=" + lightSensorRate);
            }
            this.mCurrentLightSensorRate = lightSensorRate;
            this.mSensorManager.unregisterListener(this.mLightSensorListener);
            this.mSensorManager.registerListener(this.mLightSensorListener, this.mLightSensor, lightSensorRate * 1000, this.mHandler);
        }
    }

    private boolean setAutoBrightnessAdjustment(float adjustment) {
        return this.mBrightnessMapper.setAutoBrightnessAdjustment(adjustment);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:23:0x004e, code lost:
        return true;
     */
    private boolean setScreenAutoBrightnessAdjustment(float adjustment) {
        long now = System.currentTimeMillis();
        if (adjustment != this.mScreenAutoBrightnessAdjustment) {
            synchronized (this.mLock) {
                if (this.mUserSwitchTime <= 0 || now - this.mUserSwitchTime >= 1000) {
                    this.mScreenAutoBrightnessAdjustment = adjustment;
                    if (mOppoBrightUtils.isAIBrightnessFeatureOpen) {
                        this.mAmbientLuxValid = true;
                    }
                    if (!mOppoBrightUtils.isSpecialAdj(this.mScreenAutoBrightnessAdjustment) && this.mLightSensorEnabled && this.mAmbientLuxValid) {
                        this.mManulBrightnessSlide = true;
                        OppoBrightUtils oppoBrightUtils = mOppoBrightUtils;
                        OppoBrightUtils.mManualBrightness = Math.round(adjustment);
                    }
                } else {
                    this.mUserSwitchTime = -1;
                    return false;
                }
            }
        } else {
            if (mOppoBrightUtils.isAIBrightnessFeatureOpen) {
                this.mAmbientLuxValid = true;
            }
            return false;
        }
    }

    private void setAmbientLux(float lux) {
        if (DEBUG) {
            Slog.d(TAG, "setAmbientLux(" + lux + ")");
        }
        if (lux < OppoBrightUtils.MIN_LUX_LIMITI) {
            Slog.w(TAG, "Ambient lux was negative, ignoring and setting to 0");
            lux = OppoBrightUtils.MIN_LUX_LIMITI;
        }
        this.mAmbientLux = lux;
        float f = this.mAmbientLux;
        this.mBrighteningLuxThreshold = f * 1.0f;
        this.mDarkeningLuxThreshold = f * 1.0f;
    }

    /* JADX INFO: Multiple debug info for r9v3 long: [D('endIndex' int), D('startTime' long)] */
    private float calculateAmbientLux(long now, long horizon) {
        OppoAutomaticBrightnessController oppoAutomaticBrightnessController = this;
        long j = now;
        if (DEBUG) {
            Slog.d(TAG, "calculateAmbientLux(" + j + ", " + horizon + ")");
        }
        int N = oppoAutomaticBrightnessController.mAmbientLightRingBuffer.size();
        if (N == 0) {
            Slog.e(TAG, "calculateAmbientLux: No ambient light readings available");
            return -1.0f;
        }
        int endIndex = 0;
        long horizonStartTime = j - horizon;
        int i = 0;
        while (i < N - 1 && oppoAutomaticBrightnessController.mAmbientLightRingBuffer.getTime(i + 1) <= horizonStartTime) {
            endIndex++;
            i++;
        }
        if (DEBUG) {
            Slog.d(TAG, "calculateAmbientLux: selected endIndex=" + endIndex + ", point=(" + oppoAutomaticBrightnessController.mAmbientLightRingBuffer.getTime(endIndex) + ", " + oppoAutomaticBrightnessController.mAmbientLightRingBuffer.getLux(endIndex) + ")");
        }
        float sum = OppoBrightUtils.MIN_LUX_LIMITI;
        float totalWeight = OppoBrightUtils.MIN_LUX_LIMITI;
        long endTime = AMBIENT_LIGHT_PREDICTION_TIME_MILLIS;
        int i2 = N - 1;
        while (i2 >= endIndex) {
            long eventTime = oppoAutomaticBrightnessController.mAmbientLightRingBuffer.getTime(i2);
            if (i2 == endIndex && eventTime < horizonStartTime) {
                eventTime = horizonStartTime;
            }
            long startTime = eventTime - j;
            float weight = oppoAutomaticBrightnessController.calculateWeight(startTime, endTime);
            float lux = oppoAutomaticBrightnessController.mAmbientLightRingBuffer.getLux(i2);
            if (DEBUG) {
                Slog.d(TAG, "calculateAmbientLux: [" + startTime + ", " + endTime + "]: lux=" + lux + ", weight=" + weight);
            }
            totalWeight += weight;
            sum += lux * weight;
            endTime = startTime;
            i2--;
            oppoAutomaticBrightnessController = this;
            j = now;
            endIndex = endIndex;
            horizonStartTime = horizonStartTime;
        }
        if (DEBUG) {
            Slog.d(TAG, "calculateAmbientLux: totalWeight=" + totalWeight + ", newAmbientLux=" + (sum / totalWeight));
        }
        return sum / totalWeight;
    }

    private float calculateAmbientLux(long now) {
        int N = this.mAmbientLightRingBuffer.size();
        if (N == 0) {
            Slog.e(TAG, "calculateAmbientLux: No ambient light readings available");
            return -1.0f;
        }
        float sum = OppoBrightUtils.MIN_LUX_LIMITI;
        float totalWeight = OppoBrightUtils.MIN_LUX_LIMITI;
        long endTime = AMBIENT_LIGHT_PREDICTION_TIME_MILLIS;
        for (int i = N - 1; i >= 0; i--) {
            long startTime = this.mAmbientLightRingBuffer.getTime(i) - now;
            if (startTime < 0) {
                startTime = 0;
            }
            float weight = calculateWeight(startTime, endTime);
            this.mAmbientLightRingBuffer.getLux(i);
            totalWeight += weight;
            sum += this.mAmbientLightRingBuffer.getLux(i) * weight;
            endTime = startTime;
        }
        if (DEBUG) {
            Slog.d(TAG, "calculateAmbientLux: totalWeight=" + totalWeight + ", newAmbientLux=" + (sum / totalWeight));
        }
        return sum / totalWeight;
    }

    private float calculateWeight(long startDelta, long endDelta) {
        return weightIntegral(endDelta) - weightIntegral(startDelta);
    }

    private float weightIntegral(long x) {
        return ((float) x) * ((((float) x) * 0.5f) + ((float) this.mWeightingIntercept));
    }

    private long nextAmbientLightBrighteningTransition(long time) {
        long earliestValidTime = time;
        int i = this.mAmbientLightRingBuffer.size() - 1;
        while (i >= 0 && Math.round(this.mAmbientLightRingBuffer.getLux(i)) > Math.round(this.mBrighteningLuxThreshold)) {
            earliestValidTime = this.mAmbientLightRingBuffer.getTime(i);
            i--;
        }
        if (mOppoBrightUtils.isAIBrightnessOpen()) {
            return earliestValidTime;
        }
        return this.mBrighteningLightDebounceConfig + earliestValidTime;
    }

    private long nextAmbientLightDarkeningTransition(long time) {
        long earliestValidTime = time;
        int i = this.mAmbientLightRingBuffer.size() - 1;
        while (i >= 0 && Math.round(this.mAmbientLightRingBuffer.getLux(i)) < Math.round(this.mDarkeningLuxThreshold)) {
            earliestValidTime = this.mAmbientLightRingBuffer.getTime(i);
            i--;
        }
        if (mOppoBrightUtils.isAIBrightnessOpen()) {
            return earliestValidTime;
        }
        return this.mDarkeningLightDebounceConfig + earliestValidTime;
    }

    /* access modifiers changed from: private */
    public void updateAmbientLux() {
        long time = SystemClock.uptimeMillis();
        this.mAmbientLightRingBuffer.prune(time - ((long) this.mAmbientLightHorizon));
        updateAmbientLux(time);
    }

    private void updateAmbientLux(long time) {
        long time2 = time;
        if (!this.mAmbientLuxValid) {
            long timeWhenSensorWarmedUp = ((long) this.mLightSensorWarmUpTimeConfig) + this.mLightSensorEnableTime;
            if (time2 < timeWhenSensorWarmedUp) {
                if (DEBUG) {
                    Slog.d(TAG, "updateAmbientLux: Sensor not  ready yet: time=" + time2 + ", timeWhenSensorWarmedUp=" + timeWhenSensorWarmedUp);
                }
                this.mHandler.sendEmptyMessageAtTime(1, timeWhenSensorWarmedUp);
                return;
            }
            this.mBacklightStatus = 0;
            this.lux_beforemin = this.lux_min;
            this.lux_beforemax = this.lux_max;
            this.mareabefore = this.marea;
            float firstAmbientLux = calculateAmbientLux(time);
            OppoBrightUtils oppoBrightUtils = mOppoBrightUtils;
            if (!OppoBrightUtils.mScreenUnderLightSensorSupport) {
                this.mWAState = false;
            } else {
                this.mWAManualBrightness = mOppoBrightUtils.getWAManualBrightness();
                this.mWASensorLux = mOppoBrightUtils.getWASensorLux();
                this.mWASleepTime = mOppoBrightUtils.getWASleepTime();
                this.mWAisOpen = mOppoBrightUtils.isOpen();
                if (this.mSleepStartTime > 0) {
                    this.mSleepTime = SystemClock.elapsedRealtime() - this.mSleepStartTime;
                }
                if (DEBUG) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("updateAmbientLux  mSleepStartTime = ");
                    sb.append(this.mSleepStartTime);
                    sb.append("  mSleepTime = ");
                    sb.append(this.mSleepTime);
                    sb.append("  waManu = ");
                    sb.append(this.mWAManualBrightness);
                    sb.append(" waLux=");
                    sb.append(this.mWASensorLux);
                    sb.append(" waTime = ");
                    sb.append(this.mWASleepTime);
                    sb.append(" manu = ");
                    OppoBrightUtils oppoBrightUtils2 = mOppoBrightUtils;
                    sb.append(OppoBrightUtils.mManualBrightness);
                    sb.append(" mSensorLux = ");
                    sb.append(this.mSensorLux);
                    sb.append(" waOpen = ");
                    sb.append(this.mWAisOpen);
                    Slog.d(TAG, sb.toString());
                }
                if (this.mSleepTime >= this.mWASleepTime) {
                    this.mStartManual = false;
                    OppoBrightUtils oppoBrightUtils3 = mOppoBrightUtils;
                    OppoBrightUtils.mBrightnessOverride = -1;
                    OppoBrightUtils oppoBrightUtils4 = mOppoBrightUtils;
                    OppoBrightUtils.mManualBrightnessBackup = 0;
                    OppoBrightUtils oppoBrightUtils5 = mOppoBrightUtils;
                    OppoBrightUtils.mManualAmbientLuxBackup = OppoBrightUtils.MIN_LUX_LIMITI;
                    OppoBrightUtils oppoBrightUtils6 = mOppoBrightUtils;
                    OppoBrightUtils.mManualBrightness = 0;
                    OppoBrightUtils oppoBrightUtils7 = mOppoBrightUtils;
                    OppoBrightUtils.mManulAtAmbientLux = OppoBrightUtils.MIN_LUX_LIMITI;
                    Slog.d(TAG, "updateAmbientLux clear clear clear");
                }
                this.mSleepStartTime = -1;
                OppoBrightUtils oppoBrightUtils8 = mOppoBrightUtils;
                if (OppoBrightUtils.mManualBrightness < this.mWAManualBrightness || this.mSensorLux > ((float) this.mWASensorLux)) {
                    this.mWAState = false;
                    if (DEBUG) {
                        Slog.d(TAG, "updateAmbientLux --02--");
                    }
                } else {
                    this.mWAState = true;
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append("updateAmbientLux --01--manulux = ");
                    OppoBrightUtils oppoBrightUtils9 = mOppoBrightUtils;
                    sb2.append(OppoBrightUtils.mManulAtAmbientLux);
                    sb2.append("  lux = ");
                    sb2.append(firstAmbientLux);
                    Slog.d(TAG, sb2.toString());
                    OppoBrightUtils oppoBrightUtils10 = mOppoBrightUtils;
                    if (firstAmbientLux <= OppoBrightUtils.mManulAtAmbientLux) {
                        OppoBrightUtils oppoBrightUtils11 = mOppoBrightUtils;
                        firstAmbientLux = OppoBrightUtils.mManulAtAmbientLux;
                    }
                }
            }
            setAmbientLux(firstAmbientLux);
            this.mAmbientLuxValid = true;
            if (DEBUG) {
                Slog.d(TAG, "updateAmbientLux: Initializing: mAmbientLightRingBuffer=" + this.mAmbientLightRingBuffer + ", mAmbientLux=" + this.mAmbientLux);
            }
            updateAutoBrightness(true);
        }
        long nextBrightenTransition = nextAmbientLightBrighteningTransition(time);
        long nextDarkenTransition = nextAmbientLightDarkeningTransition(time);
        float ambientLux = calculateAmbientLux(time);
        if (this.mBacklightStatus == 2) {
            time2 = 0;
        }
        boolean screenOn = false;
        if (time2 - this.mLightSensorEnableTime <= 4000) {
            screenOn = true;
        }
        boolean pocketRinging = false;
        mOppoBrightUtils.setPocketRingingState(false);
        if ((time2 - this.mProximitySensorChangeTime <= 4000 || time2 - this.mVirProxSensorChangeTime <= 4000) && mOppoBrightUtils.getPhoneState() == 1) {
            mOppoBrightUtils.setPocketRingingState(true);
            pocketRinging = true;
        }
        OppoBrightUtils oppoBrightUtils12 = mOppoBrightUtils;
        if (!OppoBrightUtils.mIsOledHighBrightness && ((this.mBacklightStatus == 1 && Math.round(ambientLux) >= Math.round(this.mBrighteningLuxThreshold) && (nextBrightenTransition <= time2 || screenOn || pocketRinging)) || (Math.round(ambientLux) <= Math.round(this.mDarkeningLuxThreshold) && this.mBacklightStatus == 1 && nextDarkenTransition <= time2))) {
            this.mBacklightStatus = 0;
            this.lux_beforemin = this.lux_min;
            this.lux_beforemax = this.lux_max;
            this.mareabefore = this.marea;
            if (DEBUG) {
                Slog.d(TAG, "ambientLux = " + Math.round(ambientLux) + " LuxThreshold = " + Math.round(this.mBrighteningLuxThreshold));
            }
            OppoBrightUtils oppoBrightUtils13 = mOppoBrightUtils;
            if (!OppoBrightUtils.mScreenUnderLightSensorSupport) {
                this.mWAState = false;
            } else {
                OppoBrightUtils oppoBrightUtils14 = mOppoBrightUtils;
                if (OppoBrightUtils.mManualBrightness < this.mWAManualBrightness || this.mSensorLux > ((float) this.mWASensorLux)) {
                    this.mWAState = false;
                    if (DEBUG) {
                        Slog.d(TAG, "updateAmbientLux --04--");
                    }
                } else {
                    this.mWAState = true;
                    StringBuilder sb3 = new StringBuilder();
                    sb3.append("updateAmbientLux --03--manulux = ");
                    OppoBrightUtils oppoBrightUtils15 = mOppoBrightUtils;
                    sb3.append(OppoBrightUtils.mManulAtAmbientLux);
                    sb3.append("  lux = ");
                    sb3.append(ambientLux);
                    Slog.d(TAG, sb3.toString());
                    OppoBrightUtils oppoBrightUtils16 = mOppoBrightUtils;
                    if (ambientLux <= OppoBrightUtils.mManulAtAmbientLux) {
                        OppoBrightUtils oppoBrightUtils17 = mOppoBrightUtils;
                        ambientLux = OppoBrightUtils.mManulAtAmbientLux;
                    }
                }
            }
            setAmbientLux(ambientLux);
            updateAutoBrightness(true);
            nextBrightenTransition = nextAmbientLightBrighteningTransition(time2);
            nextDarkenTransition = nextAmbientLightDarkeningTransition(time2);
        }
        long nextTransitionTime = Math.min(nextDarkenTransition, nextBrightenTransition);
        long nextTransitionTime2 = nextTransitionTime > time2 ? nextTransitionTime : ((long) this.mNormalLightSensorRate) + time2;
        if (DEBUG) {
            Slog.d(TAG, "updateAmbientLux: Scheduling ambient lux update for " + nextTransitionTime2 + TimeUtils.formatUptime(nextTransitionTime2));
        }
        this.mHandler.sendEmptyMessageAtTime(1, nextTransitionTime2);
    }

    private void resetAutoBrightness(float ambientLux, float manulAtAmbientLux, int manulBrightness) {
        int lNowInterval = mOppoBrightUtils.resetAmbientLux(ambientLux);
        int lManulInterval = mOppoBrightUtils.resetAmbientLux(manulAtAmbientLux);
        this.mScreenAutoBrightness = mOppoBrightUtils.calDragBrightness(clampScreenBrightness(Math.round((float) mOppoBrightUtils.getBrightness(lManulInterval, manulAtAmbientLux))), manulBrightness, lNowInterval, lManulInterval, this.mScreenAutoBrightness);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:49:0x0148, code lost:
        if (com.android.server.display.OppoBrightUtils.mCameraUseAdjustmentSetting != false) goto L_0x014a;
     */
    private void updateAutoBrightness(boolean sendUpdate) {
        if (this.mAmbientLuxValid || this.mManulBrightnessSlide) {
            float value = (float) mOppoBrightUtils.getBrightness(this.marea, this.mAmbientLux);
            if (this.mManulBrightnessSlide) {
                if (DEBUG) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("mOppoBrightUtils.mManualBrightness = ");
                    OppoBrightUtils oppoBrightUtils = mOppoBrightUtils;
                    sb.append(OppoBrightUtils.mManualBrightness);
                    sb.append(" mAmbientLux = ");
                    sb.append(this.mAmbientLux);
                    Slog.d(TAG, sb.toString());
                }
                OppoBrightUtils oppoBrightUtils2 = mOppoBrightUtils;
                if (OppoBrightUtils.mManualBrightness != 0) {
                    OppoBrightUtils oppoBrightUtils3 = mOppoBrightUtils;
                    OppoBrightUtils.mManulAtAmbientLux = this.mAmbientLux;
                    if (OppoBrightUtils.REPORT_FEATURE_ON) {
                        this.mAutoAmbientLuxValue = this.mLastObservedLux;
                        OppoBrightUtils oppoBrightUtils4 = mOppoBrightUtils;
                        if (!OppoBrightUtils.mManualSetAutoBrightness) {
                            this.mAutoDurationTime = System.currentTimeMillis() - this.mAutoTime;
                            this.mLastAutoBrightness = (float) this.mScreenAutoBrightness;
                            OppoBrightUtils oppoBrightUtils5 = mOppoBrightUtils;
                            if (OppoBrightUtils.DEBUG_REPORT) {
                                Slog.d(TAG, "first manual, mAutoDurationTime=" + this.mAutoDurationTime);
                            }
                            if (this.mAutoDurationTime > 10000) {
                                this.mHandler.sendMessage(this.mHandler.obtainMessage(10));
                            }
                        } else {
                            OppoBrightUtils oppoBrightUtils6 = mOppoBrightUtils;
                            if (OppoBrightUtils.mManualSetAutoBrightness) {
                                this.mAutoManulDurationTime = System.currentTimeMillis() - this.mAutoManulTime;
                                if (this.mAutoManulDurationTime > 10000) {
                                    this.mHandler.sendMessage(this.mHandler.obtainMessage(11));
                                    this.mAutoManulTime = System.currentTimeMillis();
                                    this.mLastAutoManualBrightness = (float) this.mScreenAutoBrightness;
                                }
                            }
                        }
                        this.mAutoManulTime = System.currentTimeMillis();
                        this.mAutoTime = this.mAutoManulTime;
                    }
                    this.mStartManual = true;
                    this.mManulBrightnessSlide = false;
                    OppoBrightUtils oppoBrightUtils7 = mOppoBrightUtils;
                    OppoBrightUtils.mManualSetAutoBrightness = true;
                    OppoBrightUtils oppoBrightUtils8 = mOppoBrightUtils;
                    this.mScreenAutoBrightness = OppoBrightUtils.mManualBrightness;
                    this.mCallbacks.updateBrightness();
                    if (mOppoBrightUtils.isAIBrightnessOpen()) {
                        mOppoBrightUtils.setBrightnessByUser(this.mScreenAutoBrightness);
                        mOppoBrightUtils.mAIBrightness = this.mScreenAutoBrightness;
                        return;
                    }
                    return;
                }
                return;
            }
            boolean reportAutoMaunalBrightness = false;
            OppoBrightUtils oppoBrightUtils9 = mOppoBrightUtils;
            if (OppoBrightUtils.mManualSetAutoBrightness && OppoBrightUtils.REPORT_FEATURE_ON) {
                reportAutoMaunalBrightness = true;
            }
            if (sendUpdate) {
                OppoBrightUtils oppoBrightUtils10 = mOppoBrightUtils;
                OppoBrightUtils.mManualSetAutoBrightness = false;
            }
            int autoBrightness = this.mScreenAutoBrightness;
            int newScreenAutoBrightness = clampScreenBrightness(Math.round(value));
            if (mOppoBrightUtils.isAIBrightnessOpen() && mOppoBrightUtils.mAIBrightness != 0) {
                newScreenAutoBrightness = mOppoBrightUtils.mAIBrightness;
            }
            int newScreenAutoBrightness2 = mOppoBrightUtils.findNightBrightness(OppoBrightUtils.mDisplayStateOn, newScreenAutoBrightness);
            if (this.mScreenAutoBrightness == newScreenAutoBrightness2) {
                OppoBrightUtils oppoBrightUtils11 = mOppoBrightUtils;
                if (OppoBrightUtils.mCameraMode != 1) {
                    OppoBrightUtils oppoBrightUtils12 = mOppoBrightUtils;
                }
            }
            if (DEBUG) {
                StringBuilder sb2 = new StringBuilder();
                sb2.append("mScreenAutoBrightness = ");
                sb2.append(this.mScreenAutoBrightness);
                sb2.append(" newScreenAutoBrightness = ");
                sb2.append(newScreenAutoBrightness2);
                sb2.append(" mOppoBrightUtils.mManualBrightness = ");
                OppoBrightUtils oppoBrightUtils13 = mOppoBrightUtils;
                sb2.append(OppoBrightUtils.mManualBrightness);
                sb2.append(" mStartManual = ");
                sb2.append(this.mStartManual);
                sb2.append(" mOppoBrightUtils.mManualBrightnessBackup = ");
                OppoBrightUtils oppoBrightUtils14 = mOppoBrightUtils;
                sb2.append(OppoBrightUtils.mManualBrightnessBackup);
                sb2.append(" mOppoBrightUtils.mDisplayStateOn = ");
                OppoBrightUtils oppoBrightUtils15 = mOppoBrightUtils;
                sb2.append(OppoBrightUtils.mDisplayStateOn);
                sb2.append(" mBrightnessOverride = ");
                OppoBrightUtils oppoBrightUtils16 = mOppoBrightUtils;
                sb2.append(OppoBrightUtils.mBrightnessOverride);
                Slog.d(TAG, sb2.toString());
            }
            this.mScreenAutoBrightness = newScreenAutoBrightness2;
            this.mLastScreenAutoBrightnessGamma = 1.0f;
            OppoBrightUtils oppoBrightUtils17 = mOppoBrightUtils;
            if (OppoBrightUtils.mDisplayStateOn) {
                OppoBrightUtils oppoBrightUtils18 = mOppoBrightUtils;
                if (OppoBrightUtils.mManualBrightnessBackup != 0) {
                    this.mStartManual = true;
                    OppoBrightUtils oppoBrightUtils19 = mOppoBrightUtils;
                    OppoBrightUtils.mManualBrightness = OppoBrightUtils.mManualBrightnessBackup;
                    OppoBrightUtils oppoBrightUtils20 = mOppoBrightUtils;
                    OppoBrightUtils.mManulAtAmbientLux = OppoBrightUtils.mManualAmbientLuxBackup;
                    OppoBrightUtils oppoBrightUtils21 = mOppoBrightUtils;
                    OppoBrightUtils.mManualBrightnessBackup = 0;
                    OppoBrightUtils oppoBrightUtils22 = mOppoBrightUtils;
                    OppoBrightUtils.mManualAmbientLuxBackup = OppoBrightUtils.MIN_LUX_LIMITI;
                }
            }
            OppoBrightUtils oppoBrightUtils23 = mOppoBrightUtils;
            if (OppoBrightUtils.mBrightnessOverride == 0) {
                OppoBrightUtils oppoBrightUtils24 = mOppoBrightUtils;
                if (OppoBrightUtils.mBrightnessOverrideAdj != 0 && this.mLightSensorEnabled) {
                    this.mStartManual = true;
                    OppoBrightUtils oppoBrightUtils25 = mOppoBrightUtils;
                    OppoBrightUtils.mManualBrightness = OppoBrightUtils.mBrightnessOverrideAdj;
                    OppoBrightUtils oppoBrightUtils26 = mOppoBrightUtils;
                    OppoBrightUtils.mManulAtAmbientLux = OppoBrightUtils.mBrightnessOverrideAmbientLux;
                    OppoBrightUtils oppoBrightUtils27 = mOppoBrightUtils;
                    OppoBrightUtils.mBrightnessOverride = -1;
                    OppoBrightUtils oppoBrightUtils28 = mOppoBrightUtils;
                    OppoBrightUtils.mBrightnessOverrideAdj = 0;
                    OppoBrightUtils oppoBrightUtils29 = mOppoBrightUtils;
                    OppoBrightUtils.mBrightnessOverrideAmbientLux = OppoBrightUtils.MIN_LUX_LIMITI;
                }
            }
            if (!mOppoBrightUtils.isAIBrightnessOpen() && this.mStartManual && this.mLightSensorEnabled) {
                float f = this.mAmbientLux;
                OppoBrightUtils oppoBrightUtils30 = mOppoBrightUtils;
                if (f == OppoBrightUtils.mManulAtAmbientLux) {
                    OppoBrightUtils oppoBrightUtils31 = mOppoBrightUtils;
                    this.mScreenAutoBrightness = OppoBrightUtils.mManualBrightness;
                } else {
                    float f2 = this.mAmbientLux;
                    OppoBrightUtils oppoBrightUtils32 = mOppoBrightUtils;
                    float f3 = OppoBrightUtils.mManulAtAmbientLux;
                    OppoBrightUtils oppoBrightUtils33 = mOppoBrightUtils;
                    resetAutoBrightness(f2, f3, OppoBrightUtils.mManualBrightness);
                    float f4 = this.mAmbientLux;
                    OppoBrightUtils oppoBrightUtils34 = mOppoBrightUtils;
                    if (f4 > OppoBrightUtils.mManulAtAmbientLux) {
                        int i = this.mScreenAutoBrightness;
                        OppoBrightUtils oppoBrightUtils35 = mOppoBrightUtils;
                        this.mScreenAutoBrightness = Math.max(i, OppoBrightUtils.mManualBrightness);
                    } else {
                        float f5 = this.mAmbientLux;
                        OppoBrightUtils oppoBrightUtils36 = mOppoBrightUtils;
                        if (f5 < OppoBrightUtils.mManulAtAmbientLux) {
                            int i2 = this.mScreenAutoBrightness;
                            OppoBrightUtils oppoBrightUtils37 = mOppoBrightUtils;
                            this.mScreenAutoBrightness = Math.min(i2, OppoBrightUtils.mManualBrightness);
                        }
                    }
                }
            }
            OppoBrightUtils oppoBrightUtils38 = mOppoBrightUtils;
            if (OppoBrightUtils.mCameraMode == 1) {
                OppoBrightUtils oppoBrightUtils39 = mOppoBrightUtils;
                if (OppoBrightUtils.mCameraBacklight) {
                    this.mScreenAutoBrightness = mOppoBrightUtils.adjustCameraBrightness(this.mScreenAutoBrightness);
                    Slog.d(TAG, "updateBrightness from CAMERA color = " + this.mScreenAutoBrightness);
                } else {
                    OppoBrightUtils oppoBrightUtils40 = mOppoBrightUtils;
                    if (OppoBrightUtils.mGalleryBacklight) {
                        Slog.d(TAG, "There is no request in Gallery, do nothing");
                    }
                }
            }
            OppoBrightUtils oppoBrightUtils41 = mOppoBrightUtils;
            int i3 = OppoBrightUtils.mBrightnessBitsConfig;
            OppoBrightUtils oppoBrightUtils42 = mOppoBrightUtils;
            if (i3 != 1) {
                this.mAutoRate = oppoBrightUtils42.caclurateRateForIndex(this.mDeltaLux, autoBrightness, this.mScreenAutoBrightness);
            }
            if (sendUpdate) {
                OppoBrightUtils oppoBrightUtils43 = mOppoBrightUtils;
                if (!OppoBrightUtils.sUseRotateSupport || !mProximityNear || this.mScreenAutoBrightness >= autoBrightness) {
                    this.mCallbacks.updateBrightness();
                }
                if (sendUpdate) {
                    OppoBrightUtils oppoBrightUtils44 = mOppoBrightUtils;
                    OppoBrightUtils.mReduceBrightnessAnimating = false;
                }
                if (OppoBrightUtils.REPORT_FEATURE_ON) {
                    if (reportAutoMaunalBrightness) {
                        OppoBrightUtils oppoBrightUtils45 = mOppoBrightUtils;
                        if (OppoBrightUtils.DEBUG_REPORT) {
                            Slog.d(TAG, "reportAutoMaunalBrightness");
                        }
                        this.mAutoManulDurationTime = System.currentTimeMillis() - this.mAutoManulTime;
                        if (this.mAutoManulDurationTime > 10000) {
                            this.mHandler.sendMessage(this.mHandler.obtainMessage(11));
                        }
                        this.mLastAutoManualBrightness = (float) autoBrightness;
                        this.mAutoManulTime = System.currentTimeMillis();
                    } else {
                        this.mAutoDurationTime = System.currentTimeMillis() - this.mAutoTime;
                        this.mLastAutoBrightness = (float) autoBrightness;
                        OppoBrightUtils oppoBrightUtils46 = mOppoBrightUtils;
                        if (OppoBrightUtils.DEBUG_REPORT) {
                            Slog.d(TAG, "reportAutoBrightness,mAutoDurationTime=" + this.mAutoDurationTime + ",mAutoTime=" + this.mAutoTime + ",mAutoAmbientLuxValue=" + this.mAutoAmbientLuxValue);
                        }
                        if (this.mAutoDurationTime > 10000) {
                            this.mHandler.sendMessage(this.mHandler.obtainMessage(10));
                        }
                    }
                    this.mAutoTime = System.currentTimeMillis();
                }
            }
            OppoBrightUtils oppoBrightUtils47 = mOppoBrightUtils;
            if (OppoBrightUtils.mDisplayStateOn && this.mLightSensorEnabled) {
                OppoBrightUtils oppoBrightUtils48 = mOppoBrightUtils;
                if (OppoBrightUtils.mManualBrightnessBackup != 0) {
                    this.mStartManual = true;
                    OppoBrightUtils oppoBrightUtils49 = mOppoBrightUtils;
                    OppoBrightUtils.mManualBrightness = OppoBrightUtils.mManualBrightnessBackup;
                    OppoBrightUtils oppoBrightUtils50 = mOppoBrightUtils;
                    OppoBrightUtils.mManulAtAmbientLux = OppoBrightUtils.mManualAmbientLuxBackup;
                    OppoBrightUtils oppoBrightUtils51 = mOppoBrightUtils;
                    OppoBrightUtils.mManualBrightnessBackup = 0;
                    OppoBrightUtils oppoBrightUtils52 = mOppoBrightUtils;
                    OppoBrightUtils.mManualAmbientLuxBackup = OppoBrightUtils.MIN_LUX_LIMITI;
                }
            }
            OppoBrightUtils oppoBrightUtils53 = mOppoBrightUtils;
            OppoBrightUtils.mDisplayStateOn = false;
        }
    }

    private int clampScreenBrightness(int value) {
        return MathUtils.constrain(value, this.mScreenBrightnessRangeMinimum, this.mScreenBrightnessRangeMaximum);
    }

    private void prepareBrightnessAdjustmentSample() {
        if (!this.mBrightnessAdjustmentSamplePending) {
            this.mBrightnessAdjustmentSamplePending = true;
            this.mBrightnessAdjustmentSampleOldLux = this.mAmbientLuxValid ? this.mAmbientLux : -1.0f;
            this.mBrightnessAdjustmentSampleOldBrightness = this.mScreenAutoBrightness;
        } else {
            this.mHandler.removeMessages(2);
        }
        this.mHandler.sendEmptyMessageDelayed(2, 10000);
    }

    private void cancelBrightnessAdjustmentSample() {
        if (this.mBrightnessAdjustmentSamplePending) {
            this.mBrightnessAdjustmentSamplePending = false;
            this.mHandler.removeMessages(2);
        }
    }

    /* access modifiers changed from: private */
    public void collectBrightnessAdjustmentSample() {
        if (this.mBrightnessAdjustmentSamplePending) {
            this.mBrightnessAdjustmentSamplePending = false;
            if (this.mAmbientLuxValid && this.mScreenAutoBrightness >= 0) {
                if (DEBUG) {
                    Slog.d(TAG, "Auto-brightness adjustment changed by user: lux=" + this.mAmbientLux + ", brightness=" + this.mScreenAutoBrightness + ", ring=" + this.mAmbientLightRingBuffer);
                }
                EventLog.writeEvent((int) EventLogTags.AUTO_BRIGHTNESS_ADJ, Float.valueOf(this.mBrightnessAdjustmentSampleOldLux), Integer.valueOf(this.mBrightnessAdjustmentSampleOldBrightness), Float.valueOf(this.mAmbientLux), Integer.valueOf(this.mScreenAutoBrightness));
            }
        }
    }

    private void registerForegroundAppUpdater() {
        try {
            this.mActivityTaskManager.registerTaskStackListener(this.mTaskStackListener);
            updateForegroundApp();
        } catch (RemoteException e) {
            if (this.mLoggingEnabled) {
                Slog.e(TAG, "Failed to register foreground app updater: " + e);
            }
        }
    }

    private void unregisterForegroundAppUpdater() {
        try {
            this.mActivityTaskManager.unregisterTaskStackListener(this.mTaskStackListener);
        } catch (RemoteException e) {
        }
        this.mForegroundAppPackageName = null;
        this.mForegroundAppCategory = -1;
    }

    /* access modifiers changed from: private */
    public void updateForegroundApp() {
        if (this.mLoggingEnabled) {
            Slog.d(TAG, "Attempting to update foreground app");
        }
        BackgroundThread.getHandler().post(new Runnable() {
            /* class com.android.server.display.OppoAutomaticBrightnessController.AnonymousClass2 */

            public void run() {
                try {
                    ActivityManager.StackInfo info = OppoAutomaticBrightnessController.this.mActivityTaskManager.getFocusedStackInfo();
                    if (info == null) {
                        return;
                    }
                    if (info.topActivity != null) {
                        String packageName = info.topActivity.getPackageName();
                        if (OppoAutomaticBrightnessController.this.mForegroundAppPackageName == null || !OppoAutomaticBrightnessController.this.mForegroundAppPackageName.equals(packageName)) {
                            String unused = OppoAutomaticBrightnessController.this.mPendingForegroundAppPackageName = packageName;
                            int unused2 = OppoAutomaticBrightnessController.this.mPendingForegroundAppCategory = -1;
                            try {
                                int unused3 = OppoAutomaticBrightnessController.this.mPendingForegroundAppCategory = OppoAutomaticBrightnessController.this.mPackageManager.getApplicationInfo(packageName, DumpState.DUMP_CHANGES).category;
                            } catch (PackageManager.NameNotFoundException e) {
                            }
                            OppoAutomaticBrightnessController.this.mHandler.sendEmptyMessage(5);
                        }
                    }
                } catch (RemoteException e2) {
                }
            }
        });
    }

    /* access modifiers changed from: private */
    public void updateForegroundAppSync() {
        if (this.mLoggingEnabled) {
            Slog.d(TAG, "Updating foreground app: packageName=" + this.mPendingForegroundAppPackageName + ", category=" + this.mPendingForegroundAppCategory);
        }
        this.mForegroundAppPackageName = this.mPendingForegroundAppPackageName;
        this.mPendingForegroundAppPackageName = null;
        this.mForegroundAppCategory = this.mPendingForegroundAppCategory;
        this.mPendingForegroundAppCategory = -1;
        updateAutoBrightness(true);
    }

    /* access modifiers changed from: private */
    public final class AutomaticBrightnessHandler extends Handler {
        public AutomaticBrightnessHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i != 13) {
                switch (i) {
                    case 1:
                        OppoAutomaticBrightnessController.this.updateAmbientLux();
                        return;
                    case 2:
                        OppoAutomaticBrightnessController.this.collectBrightnessAdjustmentSample();
                        return;
                    case 3:
                        OppoAutomaticBrightnessController.this.invalidateShortTermModel();
                        return;
                    case 4:
                        OppoAutomaticBrightnessController.this.updateForegroundApp();
                        return;
                    case 5:
                        OppoAutomaticBrightnessController.this.updateForegroundAppSync();
                        return;
                    case 6:
                        OppoAutomaticBrightnessController.this.mCallbacks.updateBrightness();
                        return;
                    default:
                        return;
                }
            } else {
                boolean unused = OppoAutomaticBrightnessController.this.mRotateStart = false;
            }
        }
    }

    public void setSleepStartTime(long sleepTime) {
        this.mSleepStartTime = sleepTime;
    }

    public long getSleepStartTime() {
        return this.mSleepStartTime;
    }

    public int getRealAutoBrightness() {
        return clampScreenBrightness(Math.round(this.mScreenAutoBrightnessSpline.interpolate(this.mAmbientLux)));
    }

    class TaskStackListenerImpl extends TaskStackListener {
        TaskStackListenerImpl() {
        }

        public void onTaskStackChanged() {
            OppoAutomaticBrightnessController.this.mHandler.sendEmptyMessage(4);
        }
    }

    private void setAIBrightnessListener() {
        mOppoBrightUtils.getAIBrightnessHelper().setBrightnessChangeListener(new AIBrightnessHelper.BrightnessChangeListener() {
            /* class com.android.server.display.OppoAutomaticBrightnessController.AnonymousClass5 */

            @Override // com.android.server.display.AIBrightnessHelper.BrightnessChangeListener
            public void onBrightnessChanged(float brightness) {
            }

            @Override // com.android.server.display.AIBrightnessHelper.BrightnessChangeListener
            public void onTargetBrightnessChanged(float brightness) {
                OppoAutomaticBrightnessController.mOppoBrightUtils.mAIBrightness = (int) brightness;
                OppoAutomaticBrightnessController.this.updateAIAutoBrightness();
                if (OppoAutomaticBrightnessController.DEBUG) {
                    Slog.d(OppoAutomaticBrightnessController.TAG, "onTargetBrightnessChanged mAIBrightness=" + ((int) brightness));
                }
            }

            @Override // com.android.server.display.AIBrightnessHelper.BrightnessChangeListener
            public void onSwitchChanged(boolean isOpen) {
                OppoAutomaticBrightnessController.mOppoBrightUtils.isAIBrightnessFeatureOpen = isOpen;
            }
        });
    }

    /* access modifiers changed from: private */
    public void updateAIAutoBrightness() {
        if (!this.mManulBrightnessSlide) {
            this.mAmbientLuxValid = true;
            updateAutoBrightness(true);
        }
    }

    /* access modifiers changed from: private */
    public static final class AmbientLightRingBuffer {
        private static final float BUFFER_SLACK = 1.5f;
        private int mCapacity;
        private int mCount;
        private int mEnd;
        private float[] mRingLux;
        private long[] mRingTime;
        private int mStart;

        public AmbientLightRingBuffer(long lightSensorRate, int ambientLightHorizon) {
            this.mCapacity = (int) Math.ceil((double) ((((float) ambientLightHorizon) * 1.5f) / ((float) lightSensorRate)));
            int i = this.mCapacity;
            this.mRingLux = new float[i];
            this.mRingTime = new long[i];
        }

        public float getLux(int index) {
            return this.mRingLux[offsetOf(index)];
        }

        public long getTime(int index) {
            return this.mRingTime[offsetOf(index)];
        }

        public void push(long time, float lux) {
            int next = this.mEnd;
            int i = this.mCount;
            int i2 = this.mCapacity;
            if (i == i2) {
                int newSize = i2 * 2;
                float[] newRingLux = new float[newSize];
                long[] newRingTime = new long[newSize];
                int i3 = this.mStart;
                int length = i2 - i3;
                System.arraycopy(this.mRingLux, i3, newRingLux, 0, length);
                System.arraycopy(this.mRingTime, this.mStart, newRingTime, 0, length);
                int i4 = this.mStart;
                if (i4 != 0) {
                    System.arraycopy(this.mRingLux, 0, newRingLux, length, i4);
                    System.arraycopy(this.mRingTime, 0, newRingTime, length, this.mStart);
                }
                this.mRingLux = newRingLux;
                this.mRingTime = newRingTime;
                next = this.mCapacity;
                this.mCapacity = newSize;
                this.mStart = 0;
            }
            this.mRingTime[next] = time;
            this.mRingLux[next] = lux;
            this.mEnd = next + 1;
            if (this.mEnd == this.mCapacity) {
                this.mEnd = 0;
            }
            this.mCount++;
        }

        public void prune(long horizon) {
            if (this.mCount != 0) {
                while (this.mCount > 1) {
                    int next = this.mStart + 1;
                    int i = this.mCapacity;
                    if (next >= i) {
                        next -= i;
                    }
                    if (this.mRingTime[next] > horizon) {
                        break;
                    }
                    this.mStart = next;
                    this.mCount--;
                }
                long[] jArr = this.mRingTime;
                int i2 = this.mStart;
                if (jArr[i2] < horizon) {
                    jArr[i2] = horizon;
                }
            }
        }

        public int size() {
            return this.mCount;
        }

        public void clear() {
            this.mStart = 0;
            this.mEnd = 0;
            this.mCount = 0;
        }

        public String toString() {
            StringBuffer buf = new StringBuffer();
            buf.append('[');
            int i = 0;
            while (true) {
                int i2 = this.mCount;
                if (i < i2) {
                    long next = i + 1 < i2 ? getTime(i + 1) : SystemClock.uptimeMillis();
                    if (i != 0) {
                        buf.append(", ");
                    }
                    buf.append(getLux(i));
                    buf.append(" / ");
                    buf.append(next - getTime(i));
                    buf.append("ms");
                    i++;
                } else {
                    buf.append(']');
                    return buf.toString();
                }
            }
        }

        private int offsetOf(int index) {
            if (index >= this.mCount || index < 0) {
                throw new ArrayIndexOutOfBoundsException(index);
            }
            int index2 = index + this.mStart;
            int i = this.mCapacity;
            if (index2 >= i) {
                return index2 - i;
            }
            return index2;
        }
    }

    public void setPowerState(int oldstate, int newstate) {
        this.mOldstate = oldstate;
        this.mNewstate = newstate;
    }

    /* access modifiers changed from: private */
    public void proximityApply() {
        Slog.d(TAG, "proximityApply:mProximityNear=" + mProximityNear + ",mScreenLandScape=" + mScreenLandScape + ",mDeviceLandScape=" + mDeviceLandScape);
        boolean z = mProximityNear;
        if (z != mScreenLandScape || z != mDeviceLandScape) {
            if (mScreenLandScape || mDeviceLandScape) {
                mProximityNear = true;
                Slog.d(TAG, "proximityApply:Proximity is near");
            } else {
                if (mProximityNear) {
                    if (!OppoBrightUtils.DEBUG_PRETEND_PROX_SENSOR_ABSENT) {
                        OppoBrightUtils oppoBrightUtils = mOppoBrightUtils;
                        if (!OppoBrightUtils.mManualSetAutoBrightness) {
                            this.mRecentLightSamples = 0;
                            this.mAmbientLightRingBuffer.clear();
                            this.mHandler.removeMessages(1);
                            this.lux_min = OppoBrightUtils.MIN_LUX_LIMITI;
                            this.lux_max = OppoBrightUtils.MIN_LUX_LIMITI;
                            this.mBacklightStatus = 2;
                        }
                    }
                    Slog.d(TAG, "proximityApply:Proximity is far");
                    this.mHandler.removeMessages(6);
                    this.mHandler.sendEmptyMessageDelayed(6, this.mDarkeningLightDebounceConfig);
                }
                mProximityNear = false;
            }
            if (mbPreProximityNear && !mProximityNear) {
                long j = this.mScreenLandScapeTime;
                long j2 = this.mDeviceLandScapeTime;
                if (j <= j2) {
                    j = j2;
                }
                this.mProximitySensorChangeTime = j;
            }
            mbPreProximityNear = mProximityNear;
        }
    }

    private void registerRotationStateReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.CONFIGURATION_CHANGED");
        this.mContext.registerReceiver(this.mConfigureChangeReceiver, intentFilter);
    }

    private void unregisterRotationStateReceiver() {
        this.mContext.unregisterReceiver(this.mConfigureChangeReceiver);
    }

    /* access modifiers changed from: private */
    public void registerProxSensorListener() {
        Thread thread = new Thread(new Runnable() {
            /* class com.android.server.display.OppoAutomaticBrightnessController.AnonymousClass10 */

            public void run() {
                OppoAutomaticBrightnessController.this.mSensorManager.registerListener(OppoAutomaticBrightnessController.this.mTelePhoneProximityListener, OppoAutomaticBrightnessController.this.mProximitySensor, OppoAutomaticBrightnessController.this.mNormalLightSensorRate * 1000, OppoAutomaticBrightnessController.this.mHandler);
            }
        }, "ProxSensorOnlyUseForTelePhone");
        thread.setPriority(10);
        thread.start();
    }

    /* access modifiers changed from: private */
    public void unRegisterProxSensorListener() {
        this.mSensorManager.unregisterListener(this.mTelePhoneProximityListener);
    }

    private void registerPhoneStateListener() {
        ((TelephonyManager) this.mContext.getSystemService("phone")).listen(this.mPhoneStateListener, 32);
    }

    public void setRotateState(boolean isStart) {
        this.mRotateStart = isStart;
        if (isStart) {
            this.mHandler.removeMessages(13);
            this.mHandler.sendEmptyMessageDelayed(13, 2500);
        }
        Slog.d(TAG, "update rotate state = " + this.mRotateStart);
    }

    public void onSwitchUser(int userID) {
        synchronized (this.mLock) {
            this.mUserSwitchTime = System.currentTimeMillis();
            this.mScreenAutoBrightnessAdjustment = Settings.System.getFloatForUser(this.mContext.getContentResolver(), "screen_auto_brightness_adj", OppoBrightUtils.MIN_LUX_LIMITI, userID);
            OppoBrightUtils oppoBrightUtils = mOppoBrightUtils;
            OppoBrightUtils.mManualBrightnessBackup = (int) Settings.System.getFloatForUser(this.mContext.getContentResolver(), "screen_auto_brightness_adj", OppoBrightUtils.MIN_LUX_LIMITI, userID);
            OppoBrightUtils oppoBrightUtils2 = mOppoBrightUtils;
            OppoBrightUtils.mManualAmbientLuxBackup = Settings.System.getFloatForUser(this.mContext.getContentResolver(), "autobrightness_manul_ambient", OppoBrightUtils.MIN_LUX_LIMITI, userID);
            ContentResolver contentResolver = this.mContext.getContentResolver();
            OppoBrightUtils oppoBrightUtils3 = mOppoBrightUtils;
            Settings.System.putFloatForUser(contentResolver, "screen_auto_brightness_adj", (float) OppoBrightUtils.mManualBrightness, this.mCurrentUser);
            ContentResolver contentResolver2 = this.mContext.getContentResolver();
            OppoBrightUtils oppoBrightUtils4 = mOppoBrightUtils;
            Settings.System.putFloatForUser(contentResolver2, "autobrightness_manul_ambient", OppoBrightUtils.mManulAtAmbientLux, this.mCurrentUser);
            OppoBrightUtils oppoBrightUtils5 = mOppoBrightUtils;
            OppoBrightUtils.mManualBrightness = 0;
            OppoBrightUtils oppoBrightUtils6 = mOppoBrightUtils;
            OppoBrightUtils.mManulAtAmbientLux = OppoBrightUtils.MIN_LUX_LIMITI;
            OppoBrightUtils oppoBrightUtils7 = mOppoBrightUtils;
            if (OppoBrightUtils.mManualBrightnessBackup != 0) {
                OppoBrightUtils oppoBrightUtils8 = mOppoBrightUtils;
                OppoBrightUtils oppoBrightUtils9 = mOppoBrightUtils;
                OppoBrightUtils.mManualBrightness = OppoBrightUtils.mManualBrightnessBackup;
                OppoBrightUtils oppoBrightUtils10 = mOppoBrightUtils;
                OppoBrightUtils oppoBrightUtils11 = mOppoBrightUtils;
                OppoBrightUtils.mManulAtAmbientLux = OppoBrightUtils.mManualAmbientLuxBackup;
            }
            OppoBrightUtils oppoBrightUtils12 = mOppoBrightUtils;
            OppoBrightUtils.mDisplayStateOn = true;
            this.mScreenAutoBrightness = -1;
            this.mCurrentUser = userID;
            this.forceUpdate = true;
            updateAutoBrightness(true);
        }
        if (DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("switch user mScreenAutoBrightnessAdjustment: ");
            sb.append(this.mScreenAutoBrightnessAdjustment);
            sb.append(" mManualBrightnessBackup: ");
            OppoBrightUtils oppoBrightUtils13 = mOppoBrightUtils;
            sb.append(OppoBrightUtils.mManualBrightnessBackup);
            sb.append(" mManualAmbientLuxBackup: ");
            OppoBrightUtils oppoBrightUtils14 = mOppoBrightUtils;
            sb.append(OppoBrightUtils.mManualAmbientLuxBackup);
            sb.append(" mManualBrightness: ");
            OppoBrightUtils oppoBrightUtils15 = mOppoBrightUtils;
            sb.append(OppoBrightUtils.mManualBrightness);
            sb.append(" mManulAtAmbientLux: ");
            OppoBrightUtils oppoBrightUtils16 = mOppoBrightUtils;
            sb.append(OppoBrightUtils.mManulAtAmbientLux);
            sb.append(" current user: ");
            sb.append(userID);
            Slog.i(TAG, sb.toString());
        }
    }
}
