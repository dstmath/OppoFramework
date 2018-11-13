package com.android.server.display;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.EventLog;
import android.util.MathUtils;
import android.util.Slog;
import android.util.Spline;
import android.util.TimeUtils;
import com.android.server.EventLogTags;
import com.android.server.LocalServices;
import com.android.server.twilight.TwilightListener;
import com.android.server.twilight.TwilightManager;
import com.android.server.twilight.TwilightState;
import java.util.Timer;
import java.util.TimerTask;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
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
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
class AutomaticBrightnessController {
    private static final long AMBIENT_LIGHT_PREDICTION_TIME_MILLIS = 100;
    private static final float BRIGHTENING_LIGHT_HYSTERESIS = 0.0f;
    private static final int BRIGHTNESS_ADJUSTMENT_SAMPLE_DEBOUNCE_MILLIS = 10000;
    private static final float DARKENING_LIGHT_HYSTERESIS = 0.0f;
    static boolean DEBUG = false;
    private static final boolean DEBUG_PRETEND_LIGHT_SENSOR_ABSENT = false;
    private static final int MSG_BRIGHTNESS_ADJUSTMENT_SAMPLE = 2;
    private static final int MSG_UPDATE_AMBIENT_LUX = 1;
    private static final int MSG_UPDATE_BRIGHTNESS_AFTER_PROXIMITY = 5;
    private static final int MSG_UPDATE_RUNTIME_CONFIG = 3;
    private static final long MTK_AAL_AMBIENT_LIGHT_HORIZON = 500;
    private static final boolean MTK_DEBUG = false;
    private static final String TAG = "AutomaticBrightnessController";
    private static final float TWILIGHT_ADJUSTMENT_MAX_GAMMA = 1.0f;
    private static final boolean USE_SCREEN_AUTO_BRIGHTNESS_ADJUSTMENT = true;
    private static int mCurrent_Lbr_Level;
    private static int mLast_Lbr_Level;
    private static OppoBrightUtils mOppoBrightUtils;
    public static boolean mProximityNear;
    private float lux_beforemax;
    private float lux_beforemin;
    private float lux_max;
    private float lux_min;
    private final int mAmbientLightHorizon;
    private AmbientLightRingBuffer mAmbientLightRingBuffer;
    private float mAmbientLux;
    private boolean mAmbientLuxValid;
    public int mAutoRate;
    private int mBacklightStatus;
    private final long mBrighteningLightDebounceConfig;
    private float mBrighteningLuxThreshold;
    private float mBrightnessAdjustmentSampleOldAdjustment;
    private int mBrightnessAdjustmentSampleOldBrightness;
    private float mBrightnessAdjustmentSampleOldGamma;
    private float mBrightnessAdjustmentSampleOldLux;
    private boolean mBrightnessAdjustmentSamplePending;
    private final Callbacks mCallbacks;
    private Context mContext;
    private final long mDarkeningLightDebounceConfig;
    private float mDarkeningLuxThreshold;
    private float mDeltaLux;
    private final float mDozeScaleFactor;
    private boolean mDozing;
    private AutomaticBrightnessHandler mHandler;
    private AmbientLightRingBuffer mInitialHorizonAmbientLightRingBuffer;
    private float mLastObservedLux;
    private long mLastObservedLuxTime;
    private float mLastScreenAutoBrightnessGamma;
    private final Sensor mLightSensor;
    private long mLightSensorEnableTime;
    private boolean mLightSensorEnabled;
    private final SensorEventListener mLightSensorListener;
    private final int mLightSensorRate;
    private int mLightSensorWarmUpTimeConfig;
    private boolean mManulBrightnessSlide;
    private final Sensor mProximitySensor;
    private long mProximitySensorChangeTime;
    private final SensorEventListener mProximitySensorListener;
    private int mRecentLightSamples;
    private final boolean mResetAmbientLuxAfterWarmUpConfig;
    private final RuntimeConfig mRuntimeConfig;
    public int mScreenAutoBrightness;
    private float mScreenAutoBrightnessAdjustment;
    private float mScreenAutoBrightnessAdjustmentMaxGamma;
    private Spline mScreenAutoBrightnessSpline;
    private final int mScreenBrightnessRangeMaximum;
    private final int mScreenBrightnessRangeMinimum;
    private final SensorManager mSensorManager;
    private boolean mStartManual;
    private final TwilightManager mTwilight;
    private final TwilightListener mTwilightListener;
    private boolean mUseTwilight;
    private final int mWeightingIntercept;
    private long mZeroStartTime;
    private TimerTask mZeroTask;
    private Timer mZeroTimer;
    private int marea;
    private int mareabefore;
    private boolean mbStartTimer;
    private Handler zeroHandler;

    private static final class AmbientLightRingBuffer {
        private static final float BUFFER_SLACK = 1.5f;
        private int mCapacity;
        private int mCount;
        private int mEnd;
        private float[] mRingLux = new float[this.mCapacity];
        private long[] mRingTime = new long[this.mCapacity];
        private int mStart;

        public AmbientLightRingBuffer(long lightSensorRate, int ambientLightHorizon) {
            this.mCapacity = (int) Math.ceil((double) ((((float) ambientLightHorizon) * BUFFER_SLACK) / ((float) lightSensorRate)));
        }

        public float getLux(int index) {
            return this.mRingLux[offsetOf(index)];
        }

        public long getTime(int index) {
            return this.mRingTime[offsetOf(index)];
        }

        public void push(long time, float lux) {
            int next = this.mEnd;
            if (this.mCount == this.mCapacity) {
                int newSize = this.mCapacity * 2;
                float[] newRingLux = new float[newSize];
                long[] newRingTime = new long[newSize];
                int length = this.mCapacity - this.mStart;
                System.arraycopy(this.mRingLux, this.mStart, newRingLux, 0, length);
                System.arraycopy(this.mRingTime, this.mStart, newRingTime, 0, length);
                if (this.mStart != 0) {
                    System.arraycopy(this.mRingLux, 0, newRingLux, length, this.mStart);
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
                    if (next >= this.mCapacity) {
                        next -= this.mCapacity;
                    }
                    if (this.mRingTime[next] > horizon) {
                        break;
                    }
                    this.mStart = next;
                    this.mCount--;
                }
                if (this.mRingTime[this.mStart] < horizon) {
                    this.mRingTime[this.mStart] = horizon;
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
            for (int i = 0; i < this.mCount; i++) {
                long next = i + 1 < this.mCount ? getTime(i + 1) : SystemClock.uptimeMillis();
                if (i != 0) {
                    buf.append(", ");
                }
                buf.append(getLux(i));
                buf.append(" / ");
                buf.append(next - getTime(i));
                buf.append("ms");
            }
            buf.append(']');
            return buf.toString();
        }

        private int offsetOf(int index) {
            if (index >= this.mCount || index < 0) {
                throw new ArrayIndexOutOfBoundsException(index);
            }
            index += this.mStart;
            if (index >= this.mCapacity) {
                return index - this.mCapacity;
            }
            return index;
        }
    }

    private final class AutomaticBrightnessHandler extends Handler {
        public AutomaticBrightnessHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    AutomaticBrightnessController.this.updateAmbientLux();
                    return;
                case 2:
                    AutomaticBrightnessController.this.collectBrightnessAdjustmentSample();
                    return;
                case 3:
                    AutomaticBrightnessController.this.updateRuntimeConfigInternal();
                    return;
                case 5:
                    AutomaticBrightnessController.this.mCallbacks.updateBrightness();
                    return;
                default:
                    return;
            }
        }
    }

    interface Callbacks {
        void updateBrightness();
    }

    private class RuntimeConfig {
        public Spline mScreenAutoBrightnessSpline = null;
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.display.AutomaticBrightnessController.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.display.AutomaticBrightnessController.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.display.AutomaticBrightnessController.<clinit>():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.display.AutomaticBrightnessController.dump(java.io.PrintWriter):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void dump(java.io.PrintWriter r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.display.AutomaticBrightnessController.dump(java.io.PrintWriter):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.display.AutomaticBrightnessController.dump(java.io.PrintWriter):void");
    }

    private void startZeroTimer() {
        synchronized (this) {
            if (this.mZeroTimer == null) {
                this.mZeroTimer = new Timer();
            }
            if (this.mZeroTask == null) {
                this.mZeroTask = new TimerTask() {
                    public void run() {
                        Message msg = new Message();
                        msg.what = 1;
                        AutomaticBrightnessController.this.zeroHandler.sendMessage(msg);
                    }
                };
            }
            if (!(this.mZeroTimer == null || this.mZeroTask == null)) {
                this.mZeroTimer.schedule(this.mZeroTask, 5000, this.mDarkeningLightDebounceConfig);
            }
        }
    }

    private void stopZeroTimer() {
        if (this.mbStartTimer) {
            synchronized (this) {
                try {
                    this.mbStartTimer = false;
                    if (this.mZeroTimer != null) {
                        this.mZeroTimer.cancel();
                        this.mZeroTimer = null;
                    }
                    if (this.mZeroTask != null) {
                        this.mZeroTask.cancel();
                        this.mZeroTask = null;
                    }
                } catch (NullPointerException e) {
                    Slog.i(TAG, "stopZeroTimer null pointer", e);
                }
            }
            return;
        }
        return;
    }

    public void resetLightParamsScreenOff() {
        this.mAmbientLuxValid = false;
    }

    public AutomaticBrightnessController(Callbacks callbacks, Looper looper, Context context, SensorManager sensorManager, Spline autoBrightnessSpline, int lightSensorWarmUpTime, int brightnessMin, int brightnessMax, float dozeScaleFactor, int lightSensorRate, long brighteningLightDebounceConfig, long darkeningLightDebounceConfig, boolean resetAmbientLuxAfterWarmUpConfig, int ambientLightHorizon, float autoBrightnessAdjustmentMaxGamma) {
        this.mAmbientLux = 320.0f;
        this.mScreenAutoBrightness = -1;
        this.mScreenAutoBrightnessAdjustment = OppoBrightUtils.MIN_LUX_LIMITI;
        this.mLastScreenAutoBrightnessGamma = TWILIGHT_ADJUSTMENT_MAX_GAMMA;
        this.mBacklightStatus = 0;
        this.mareabefore = -1;
        this.marea = -1;
        this.lux_beforemin = OppoBrightUtils.MIN_LUX_LIMITI;
        this.lux_beforemax = OppoBrightUtils.MIN_LUX_LIMITI;
        this.mProximitySensorChangeTime = 0;
        this.mManulBrightnessSlide = false;
        this.mStartManual = false;
        this.mAutoRate = 0;
        this.mDeltaLux = OppoBrightUtils.MIN_LUX_LIMITI;
        this.lux_min = OppoBrightUtils.MIN_LUX_LIMITI;
        this.lux_max = OppoBrightUtils.MIN_LUX_LIMITI;
        this.mbStartTimer = false;
        this.mZeroStartTime = 0;
        this.zeroHandler = new Handler() {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 1) {
                    long time = SystemClock.uptimeMillis();
                    AutomaticBrightnessController automaticBrightnessController = AutomaticBrightnessController.this;
                    AutomaticBrightnessController.mOppoBrightUtils;
                    automaticBrightnessController.lux_min = OppoBrightUtils.mAutoBrightnessLuxMinLimit[0];
                    automaticBrightnessController = AutomaticBrightnessController.this;
                    AutomaticBrightnessController.mOppoBrightUtils;
                    automaticBrightnessController.lux_max = OppoBrightUtils.mAutoBrightnessLuxMaxLimit[0];
                    AutomaticBrightnessController.this.mDeltaLux = AutomaticBrightnessController.this.mLastObservedLux;
                    AutomaticBrightnessController.this.mAutoRate = AutomaticBrightnessController.mOppoBrightUtils.caclurateRate(OppoBrightUtils.MIN_LUX_LIMITI, AutomaticBrightnessController.this.mLastObservedLux);
                    AutomaticBrightnessController.this.mLastObservedLux = OppoBrightUtils.MIN_LUX_LIMITI;
                    AutomaticBrightnessController.this.handleLightSensorEvent(time, OppoBrightUtils.MIN_LUX_LIMITI);
                }
                if (AutomaticBrightnessController.this.mbStartTimer) {
                    AutomaticBrightnessController.this.stopZeroTimer();
                }
            }
        };
        this.mLightSensorListener = new SensorEventListener() {
            private long mPrevTime = 0;

            /* JADX WARNING: Removed duplicated region for block: B:32:0x00ba  */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void onSensorChanged(SensorEvent event) {
                if (AutomaticBrightnessController.this.mLightSensorEnabled) {
                    long time = SystemClock.uptimeMillis();
                    AutomaticBrightnessController.mOppoBrightUtils;
                    if (OppoBrightUtils.mBrightnessBoost == 1) {
                        this.mPrevTime = 0;
                        AutomaticBrightnessController.mOppoBrightUtils;
                        OppoBrightUtils.mBrightnessBoost = 2;
                    }
                    if (event.values[0] <= OppoBrightUtils.MIN_LUX_LIMITI || event.values[0] >= 31000.0f || time - this.mPrevTime >= ((long) (AutomaticBrightnessController.this.mLightSensorRate / 4))) {
                        this.mPrevTime = time;
                        float lux = event.values[0];
                        AutomaticBrightnessController.mOppoBrightUtils;
                        if (OppoBrightUtils.mBrightnessBoost == 2 || !AutomaticBrightnessController.mProximityNear || OppoBrightUtils.DEBUG_PRETEND_PROX_SENSOR_ABSENT || lux >= AutomaticBrightnessController.this.lux_max || AutomaticBrightnessController.this.mBacklightStatus != 0) {
                            AutomaticBrightnessController.mOppoBrightUtils;
                            if (OppoBrightUtils.mSeedLbrModeSupport) {
                                if (lux >= 15000.0f) {
                                    AutomaticBrightnessController.mOppoBrightUtils;
                                    if (OppoBrightUtils.mCameraMode == 1) {
                                        AutomaticBrightnessController.mCurrent_Lbr_Level = (int) ((lux - 15000.0f) / 235.0f);
                                        if (AutomaticBrightnessController.mCurrent_Lbr_Level > 63) {
                                            AutomaticBrightnessController.mCurrent_Lbr_Level = 63;
                                        }
                                        if (AutomaticBrightnessController.mCurrent_Lbr_Level != AutomaticBrightnessController.mLast_Lbr_Level) {
                                            AutomaticBrightnessController.mLast_Lbr_Level = AutomaticBrightnessController.mCurrent_Lbr_Level;
                                            if (AutomaticBrightnessController.DEBUG) {
                                                Slog.d(AutomaticBrightnessController.TAG, "go to OppoBrightUtils SEED_LBR_NODE write " + AutomaticBrightnessController.mCurrent_Lbr_Level);
                                            }
                                            AutomaticBrightnessController.mOppoBrightUtils.writeSeedLbrNodeValue(AutomaticBrightnessController.mCurrent_Lbr_Level);
                                        }
                                    }
                                }
                                AutomaticBrightnessController.mCurrent_Lbr_Level = 0;
                                if (AutomaticBrightnessController.mCurrent_Lbr_Level != AutomaticBrightnessController.mLast_Lbr_Level) {
                                }
                            }
                            if (AutomaticBrightnessController.DEBUG) {
                                Slog.d(AutomaticBrightnessController.TAG, "PowerMS L-Sensor Changed:lux=" + lux + ",lux_min = " + AutomaticBrightnessController.this.lux_min + ",lux_max = " + AutomaticBrightnessController.this.lux_max);
                            }
                            if (!(lux != OppoBrightUtils.MIN_LUX_LIMITI || AutomaticBrightnessController.this.lux_min == OppoBrightUtils.MIN_LUX_LIMITI || AutomaticBrightnessController.this.mbStartTimer)) {
                                AutomaticBrightnessController.mOppoBrightUtils;
                                if (OppoBrightUtils.mBrightnessBoost != 2) {
                                    AutomaticBrightnessController.this.mZeroStartTime = time;
                                    AutomaticBrightnessController.this.mbStartTimer = true;
                                    AutomaticBrightnessController.this.startZeroTimer();
                                    if (AutomaticBrightnessController.DEBUG) {
                                        Slog.d(AutomaticBrightnessController.TAG, "onSensorChanged: first received lux = 0");
                                    }
                                    return;
                                }
                            }
                            if (AutomaticBrightnessController.this.mbStartTimer) {
                                if (lux != OppoBrightUtils.MIN_LUX_LIMITI) {
                                    AutomaticBrightnessController.this.stopZeroTimer();
                                    if (AutomaticBrightnessController.DEBUG) {
                                        Slog.d(AutomaticBrightnessController.TAG, "received 0lux at" + AutomaticBrightnessController.this.mZeroStartTime + "now received lux=" + lux);
                                    }
                                } else {
                                    if (AutomaticBrightnessController.DEBUG) {
                                        Slog.d(AutomaticBrightnessController.TAG, "it will not go here");
                                    }
                                    return;
                                }
                            }
                            if (AutomaticBrightnessController.DEBUG) {
                                Slog.d(AutomaticBrightnessController.TAG, "mBacklightStatus=" + AutomaticBrightnessController.this.mBacklightStatus + " marea=" + AutomaticBrightnessController.this.marea + " mareabefore=" + AutomaticBrightnessController.this.mareabefore);
                            }
                            if (AutomaticBrightnessController.this.mBacklightStatus != 0 || lux < AutomaticBrightnessController.this.lux_min || lux >= AutomaticBrightnessController.this.lux_max) {
                                if (AutomaticBrightnessController.this.mBacklightStatus == 1) {
                                    if (lux >= AutomaticBrightnessController.this.lux_beforemin && lux < AutomaticBrightnessController.this.lux_beforemax) {
                                        AutomaticBrightnessController.this.mBacklightStatus = 2;
                                        AutomaticBrightnessController.this.handleLightSensorEvent(time, AutomaticBrightnessController.mOppoBrightUtils.findAmbientLux(AutomaticBrightnessController.this.mareabefore));
                                        AutomaticBrightnessController.this.mBacklightStatus = 0;
                                        AutomaticBrightnessController.this.lux_min = AutomaticBrightnessController.this.lux_beforemin;
                                        AutomaticBrightnessController.this.lux_max = AutomaticBrightnessController.this.lux_beforemax;
                                        AutomaticBrightnessController.this.marea = AutomaticBrightnessController.this.mareabefore;
                                        if (AutomaticBrightnessController.DEBUG) {
                                            Slog.d(AutomaticBrightnessController.TAG, "not stable to stable status and return.");
                                        }
                                        return;
                                    } else if (lux >= AutomaticBrightnessController.this.lux_min && lux < AutomaticBrightnessController.this.lux_max) {
                                        if (AutomaticBrightnessController.DEBUG) {
                                            Slog.d(AutomaticBrightnessController.TAG, "not stable range and return.");
                                        }
                                        return;
                                    }
                                }
                                AutomaticBrightnessController.mOppoBrightUtils;
                                if (OppoBrightUtils.mBrightnessBitsConfig == 1) {
                                    AutomaticBrightnessController.this.mAutoRate = AutomaticBrightnessController.mOppoBrightUtils.caclurateRate(lux, AutomaticBrightnessController.this.mLastObservedLux);
                                } else {
                                    AutomaticBrightnessController.this.mDeltaLux = lux - AutomaticBrightnessController.this.mLastObservedLux;
                                }
                                if (AutomaticBrightnessController.DEBUG) {
                                    Slog.d(AutomaticBrightnessController.TAG, "lux=" + lux + " ,mLastObservedLux=" + AutomaticBrightnessController.this.mLastObservedLux + " ,mAutoRate=" + AutomaticBrightnessController.this.mAutoRate);
                                }
                                AutomaticBrightnessController.this.mLastObservedLux = lux;
                                int ii = 0;
                                while (true) {
                                    AutomaticBrightnessController.mOppoBrightUtils;
                                    if (ii >= OppoBrightUtils.BRIGHTNESS_STEPS) {
                                        break;
                                    }
                                    AutomaticBrightnessController.mOppoBrightUtils;
                                    AutomaticBrightnessController automaticBrightnessController;
                                    if (lux <= OppoBrightUtils.mAutoBrightnessLux[ii]) {
                                        AutomaticBrightnessController.mOppoBrightUtils;
                                        lux = OppoBrightUtils.mAutoBrightnessLux[ii];
                                        automaticBrightnessController = AutomaticBrightnessController.this;
                                        AutomaticBrightnessController.mOppoBrightUtils;
                                        automaticBrightnessController.lux_min = OppoBrightUtils.mAutoBrightnessLuxMinLimit[ii];
                                        automaticBrightnessController = AutomaticBrightnessController.this;
                                        AutomaticBrightnessController.mOppoBrightUtils;
                                        automaticBrightnessController.lux_max = OppoBrightUtils.mAutoBrightnessLuxMaxLimit[ii];
                                        AutomaticBrightnessController.this.marea = ii;
                                        break;
                                    }
                                    AutomaticBrightnessController.mOppoBrightUtils;
                                    float[] fArr = OppoBrightUtils.mAutoBrightnessLux;
                                    AutomaticBrightnessController.mOppoBrightUtils;
                                    if (lux > fArr[OppoBrightUtils.BRIGHTNESS_STEPS - 1]) {
                                        AutomaticBrightnessController.mOppoBrightUtils;
                                        fArr = OppoBrightUtils.mAutoBrightnessLux;
                                        AutomaticBrightnessController.mOppoBrightUtils;
                                        lux = fArr[OppoBrightUtils.BRIGHTNESS_STEPS - 1];
                                        automaticBrightnessController = AutomaticBrightnessController.this;
                                        AutomaticBrightnessController.mOppoBrightUtils;
                                        float[] fArr2 = OppoBrightUtils.mAutoBrightnessLuxMinLimit;
                                        AutomaticBrightnessController.mOppoBrightUtils;
                                        automaticBrightnessController.lux_min = fArr2[OppoBrightUtils.BRIGHTNESS_STEPS - 1];
                                        automaticBrightnessController = AutomaticBrightnessController.this;
                                        AutomaticBrightnessController.mOppoBrightUtils;
                                        fArr2 = OppoBrightUtils.mAutoBrightnessLuxMaxLimit;
                                        AutomaticBrightnessController.mOppoBrightUtils;
                                        automaticBrightnessController.lux_max = fArr2[OppoBrightUtils.BRIGHTNESS_STEPS - 1];
                                        automaticBrightnessController = AutomaticBrightnessController.this;
                                        AutomaticBrightnessController.mOppoBrightUtils;
                                        automaticBrightnessController.marea = OppoBrightUtils.BRIGHTNESS_STEPS - 1;
                                        break;
                                    }
                                    ii++;
                                }
                                AutomaticBrightnessController.this.mBacklightStatus = 1;
                                AutomaticBrightnessController.this.handleLightSensorEvent(time, lux);
                            } else {
                                if (AutomaticBrightnessController.DEBUG) {
                                    Slog.d(AutomaticBrightnessController.TAG, "in stable status and return.");
                                }
                                return;
                            }
                        }
                        if (AutomaticBrightnessController.DEBUG) {
                            Slog.d(AutomaticBrightnessController.TAG, "DEBUG_PRETEND_PROX_SENSOR_ABSENT=" + OppoBrightUtils.DEBUG_PRETEND_PROX_SENSOR_ABSENT);
                        }
                        return;
                    }
                    if (AutomaticBrightnessController.DEBUG) {
                        Slog.d(AutomaticBrightnessController.TAG, "Skip onSensorChanaged, pre time = " + this.mPrevTime + ", now = " + time);
                    }
                }
            }

            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };
        this.mProximitySensorListener = new SensorEventListener() {
            private boolean mPrevProximityNear = false;

            public void onSensorChanged(SensorEvent event) {
                long time = SystemClock.uptimeMillis();
                if (((double) event.values[0]) == 0.0d) {
                    AutomaticBrightnessController.mProximityNear = true;
                    if (AutomaticBrightnessController.DEBUG) {
                        Slog.d(AutomaticBrightnessController.TAG, "Proximity is near");
                    }
                } else {
                    if (AutomaticBrightnessController.mProximityNear) {
                        if (!OppoBrightUtils.DEBUG_PRETEND_PROX_SENSOR_ABSENT) {
                            AutomaticBrightnessController.mOppoBrightUtils;
                            if (!OppoBrightUtils.mManualSetAutoBrightness) {
                                AutomaticBrightnessController.this.mRecentLightSamples = 0;
                                AutomaticBrightnessController.this.mAmbientLightRingBuffer.clear();
                                AutomaticBrightnessController.this.mHandler.removeMessages(1);
                                AutomaticBrightnessController.this.lux_min = OppoBrightUtils.MIN_LUX_LIMITI;
                                AutomaticBrightnessController.this.lux_max = OppoBrightUtils.MIN_LUX_LIMITI;
                            }
                        }
                        if (AutomaticBrightnessController.DEBUG) {
                            Slog.d(AutomaticBrightnessController.TAG, "Proximity is far");
                        }
                        AutomaticBrightnessController.this.mHandler.removeMessages(5);
                        AutomaticBrightnessController.this.mHandler.sendEmptyMessageDelayed(5, AutomaticBrightnessController.this.mDarkeningLightDebounceConfig);
                    }
                    AutomaticBrightnessController.mProximityNear = false;
                }
                if (this.mPrevProximityNear && !AutomaticBrightnessController.mProximityNear) {
                    AutomaticBrightnessController.this.mProximitySensorChangeTime = time;
                }
                this.mPrevProximityNear = AutomaticBrightnessController.mProximityNear;
            }

            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };
        this.mTwilightListener = new TwilightListener() {
            public void onTwilightStateChanged(TwilightState state) {
                AutomaticBrightnessController.this.updateAutoBrightness(true);
            }
        };
        this.mRuntimeConfig = new RuntimeConfig();
        this.mCallbacks = callbacks;
        this.mTwilight = (TwilightManager) LocalServices.getService(TwilightManager.class);
        this.mSensorManager = sensorManager;
        this.mScreenAutoBrightnessSpline = autoBrightnessSpline;
        this.mScreenBrightnessRangeMinimum = brightnessMin;
        this.mScreenBrightnessRangeMaximum = brightnessMax;
        this.mLightSensorWarmUpTimeConfig = lightSensorWarmUpTime;
        this.mDozeScaleFactor = dozeScaleFactor;
        this.mLightSensorRate = lightSensorRate;
        this.mBrighteningLightDebounceConfig = brighteningLightDebounceConfig;
        this.mDarkeningLightDebounceConfig = darkeningLightDebounceConfig;
        this.mResetAmbientLuxAfterWarmUpConfig = resetAmbientLuxAfterWarmUpConfig;
        this.mAmbientLightHorizon = ambientLightHorizon;
        this.mWeightingIntercept = ambientLightHorizon;
        this.mScreenAutoBrightnessAdjustmentMaxGamma = autoBrightnessAdjustmentMaxGamma;
        this.mHandler = new AutomaticBrightnessHandler(looper);
        this.mAmbientLightRingBuffer = new AmbientLightRingBuffer((long) this.mLightSensorRate, this.mAmbientLightHorizon);
        this.mInitialHorizonAmbientLightRingBuffer = new AmbientLightRingBuffer((long) this.mLightSensorRate, this.mAmbientLightHorizon);
        this.mLightSensor = this.mSensorManager.getDefaultSensor(5);
        mOppoBrightUtils = OppoBrightUtils.getInstance();
        mOppoBrightUtils.readFeatureProperty();
        OppoBrightUtils oppoBrightUtils = mOppoBrightUtils;
        DEBUG = OppoBrightUtils.DEBUG;
        this.mContext = context;
        this.mProximitySensor = this.mSensorManager.getDefaultSensor(8);
        this.mbStartTimer = false;
    }

    public int getAutomaticScreenBrightness() {
        if (DEBUG) {
            Slog.d(TAG, "getAutomaticScreenBrightness: brightness=" + this.mScreenAutoBrightness + ", dozing=" + this.mDozing + ", factor=" + this.mDozeScaleFactor);
        }
        if (this.mDozing) {
            return (int) (((float) this.mScreenAutoBrightness) * this.mDozeScaleFactor);
        }
        return this.mScreenAutoBrightness;
    }

    public void configure(boolean enable, float adjustment, boolean dozing, boolean userInitiatedChange, boolean useTwilight) {
        this.mDozing = dozing;
        boolean z = enable && !dozing;
        if ((setLightSensorEnabled(z) | setScreenAutoBrightnessAdjustment(adjustment)) | setUseTwilight(useTwilight)) {
            updateAutoBrightness(false);
        }
        if (enable && !dozing && userInitiatedChange) {
            prepareBrightnessAdjustmentSample();
        }
    }

    private boolean setUseTwilight(boolean useTwilight) {
        if (this.mUseTwilight == useTwilight) {
            return false;
        }
        if (useTwilight) {
            this.mTwilight.registerListener(this.mTwilightListener, this.mHandler);
        } else {
            this.mTwilight.unregisterListener(this.mTwilightListener);
        }
        this.mUseTwilight = useTwilight;
        return true;
    }

    /* JADX WARNING: Missing block: B:18:0x00b2, code:
            if (com.android.server.display.OppoBrightUtils.mBrightnessBoost == 2) goto L_0x00a7;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean setLightSensorEnabled(boolean enable) {
        if (enable != this.mLightSensorEnabled) {
            Slog.d(TAG, "setLightSensorEnabled: enable=" + enable + ", mLightSensorEnabled=" + this.mLightSensorEnabled + ", mAmbientLuxValid=" + this.mAmbientLuxValid + ", mResetAmbientLuxAfterWarmUpConfig=" + this.mResetAmbientLuxAfterWarmUpConfig);
        }
        DisplayPowerController.mScreenDimQuicklyDark = false;
        OppoBrightUtils oppoBrightUtils;
        if (enable) {
            if (!this.mLightSensorEnabled) {
                this.mLightSensorEnabled = true;
                this.mLightSensorEnableTime = SystemClock.uptimeMillis();
                oppoBrightUtils = mOppoBrightUtils;
                OppoBrightUtils.mUseAutoBrightness = true;
                oppoBrightUtils = mOppoBrightUtils;
                if (OppoBrightUtils.mBrightnessBoost == 1) {
                    Thread thread = new Thread(new Runnable() {
                        public void run() {
                            AutomaticBrightnessController.this.mSensorManager.registerListener(AutomaticBrightnessController.this.mLightSensorListener, AutomaticBrightnessController.this.mLightSensor, AutomaticBrightnessController.this.mLightSensorRate * 1000, AutomaticBrightnessController.this.mHandler);
                            AutomaticBrightnessController.this.mSensorManager.registerListener(AutomaticBrightnessController.this.mProximitySensorListener, AutomaticBrightnessController.this.mProximitySensor, AutomaticBrightnessController.this.mLightSensorRate * 1000, AutomaticBrightnessController.this.mHandler);
                        }
                    }, "LightSensorEnableThread");
                    thread.setPriority(10);
                    thread.start();
                } else {
                    this.mSensorManager.registerListener(this.mLightSensorListener, this.mLightSensor, this.mLightSensorRate * 1000, this.mHandler);
                    this.mSensorManager.registerListener(this.mProximitySensorListener, this.mProximitySensor, this.mLightSensorRate * 1000, this.mHandler);
                    oppoBrightUtils = mOppoBrightUtils;
                    if (OppoBrightUtils.mBrightnessBoost != 4) {
                        oppoBrightUtils = mOppoBrightUtils;
                        if (OppoBrightUtils.mBrightnessBoost != 0) {
                            oppoBrightUtils = mOppoBrightUtils;
                        }
                    }
                    oppoBrightUtils = mOppoBrightUtils;
                    OppoBrightUtils.mBrightnessBoost = 3;
                }
                this.lux_min = OppoBrightUtils.MIN_LUX_LIMITI;
                this.lux_max = OppoBrightUtils.MIN_LUX_LIMITI;
                return true;
            }
        } else if (this.mLightSensorEnabled) {
            this.mLightSensorEnabled = false;
            this.mAmbientLuxValid = !this.mResetAmbientLuxAfterWarmUpConfig;
            this.mRecentLightSamples = 0;
            this.mAmbientLightRingBuffer.clear();
            this.mInitialHorizonAmbientLightRingBuffer.clear();
            this.mHandler.removeMessages(1);
            this.mSensorManager.unregisterListener(this.mLightSensorListener);
            this.mManulBrightnessSlide = false;
            oppoBrightUtils = mOppoBrightUtils;
            OppoBrightUtils.mManualBrightness = 0;
            this.mStartManual = false;
            oppoBrightUtils = mOppoBrightUtils;
            OppoBrightUtils.mUseAutoBrightness = false;
            oppoBrightUtils = mOppoBrightUtils;
            OppoBrightUtils.mManualSetAutoBrightness = false;
            mProximityNear = false;
            this.mSensorManager.unregisterListener(this.mProximitySensorListener);
            if (this.mbStartTimer) {
                stopZeroTimer();
            }
            DisplayPowerController.mQuickDarkToBright = false;
            this.mBacklightStatus = 0;
            oppoBrightUtils = mOppoBrightUtils;
            if (OppoBrightUtils.mSeedLbrModeSupport && mCurrent_Lbr_Level > 0) {
                mCurrent_Lbr_Level = 0;
                mLast_Lbr_Level = 0;
                if (DEBUG) {
                    Slog.d(TAG, "go to OppoBrightUtils SEED_LBR_NODE write " + mCurrent_Lbr_Level);
                }
                mOppoBrightUtils.writeSeedLbrNodeValue(mCurrent_Lbr_Level);
            }
        }
        return false;
    }

    private void handleLightSensorEvent(long time, float lux) {
        this.mHandler.removeMessages(1);
        applyLightSensorMeasurement(time, lux);
        updateAmbientLux(time);
    }

    private void applyLightSensorMeasurement(long time, float lux) {
        this.mRecentLightSamples++;
        if (time <= this.mLightSensorEnableTime + ((long) this.mAmbientLightHorizon)) {
            this.mInitialHorizonAmbientLightRingBuffer.push(time, lux);
        }
        this.mAmbientLightRingBuffer.prune(time - ((long) this.mAmbientLightHorizon));
        this.mAmbientLightRingBuffer.push(time, lux);
    }

    private boolean setScreenAutoBrightnessAdjustment(float adjustment) {
        if (adjustment == this.mScreenAutoBrightnessAdjustment) {
            return false;
        }
        this.mScreenAutoBrightnessAdjustment = adjustment;
        if (!mOppoBrightUtils.isSpecialAdj(this.mScreenAutoBrightnessAdjustment) && this.mLightSensorEnabled && this.mAmbientLuxValid) {
            this.mManulBrightnessSlide = true;
            OppoBrightUtils oppoBrightUtils = mOppoBrightUtils;
            OppoBrightUtils.mManualBrightness = Math.round(adjustment);
        }
        return true;
    }

    private void setAmbientLux(float lux) {
        this.mAmbientLux = lux;
        this.mBrighteningLuxThreshold = this.mAmbientLux * TWILIGHT_ADJUSTMENT_MAX_GAMMA;
        this.mDarkeningLuxThreshold = this.mAmbientLux * TWILIGHT_ADJUSTMENT_MAX_GAMMA;
        DisplayPowerController.nativeSetDebouncedAmbientLight((int) lux);
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
            float lux = this.mAmbientLightRingBuffer.getLux(i);
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
        return this.mBrighteningLightDebounceConfig + earliestValidTime;
    }

    private long nextAmbientLightDarkeningTransition(long time) {
        long earliestValidTime = time;
        int i = this.mAmbientLightRingBuffer.size() - 1;
        while (i >= 0 && Math.round(this.mAmbientLightRingBuffer.getLux(i)) < Math.round(this.mDarkeningLuxThreshold)) {
            earliestValidTime = this.mAmbientLightRingBuffer.getTime(i);
            i--;
        }
        return this.mDarkeningLightDebounceConfig + earliestValidTime;
    }

    private void updateAmbientLux() {
        long time = SystemClock.uptimeMillis();
        this.mAmbientLightRingBuffer.prune(time - ((long) this.mAmbientLightHorizon));
        updateAmbientLux(time);
    }

    private void updateAmbientLux(long time) {
        if (!this.mAmbientLuxValid) {
            long timeWhenSensorWarmedUp = ((long) this.mLightSensorWarmUpTimeConfig) + this.mLightSensorEnableTime;
            if (time < timeWhenSensorWarmedUp) {
                if (MTK_DEBUG) {
                    Slog.d(TAG, "updateAmbientLux: Sensor not  ready yet: time=" + time + ", timeWhenSensorWarmedUp=" + timeWhenSensorWarmedUp);
                }
                this.mHandler.sendEmptyMessageAtTime(1, timeWhenSensorWarmedUp);
                return;
            }
            this.mBacklightStatus = 0;
            this.lux_beforemin = this.lux_min;
            this.lux_beforemax = this.lux_max;
            this.mareabefore = this.marea;
            setAmbientLux(calculateAmbientLux(time));
            this.mAmbientLuxValid = true;
            if (MTK_DEBUG) {
                Slog.d(TAG, "updateAmbientLux: Initializing: mAmbientLightRingBuffer=" + this.mAmbientLightRingBuffer + ", mAmbientLux=" + this.mAmbientLux);
            }
            updateAutoBrightness(true);
        }
        long nextBrightenTransition = nextAmbientLightBrighteningTransition(time);
        long nextDarkenTransition = nextAmbientLightDarkeningTransition(time);
        float ambientLux = calculateAmbientLux(time);
        if (this.mBacklightStatus == 2) {
            time = 0;
        }
        boolean screenOn = false;
        if (time - this.mLightSensorEnableTime <= 4000) {
            screenOn = true;
        }
        boolean pocketRinging = false;
        mOppoBrightUtils.setPocketRingingState(false);
        if (time - this.mProximitySensorChangeTime <= 4000 && mOppoBrightUtils.getPhoneState() == 1) {
            mOppoBrightUtils.setPocketRingingState(true);
            pocketRinging = true;
        }
        OppoBrightUtils oppoBrightUtils = mOppoBrightUtils;
        if (!OppoBrightUtils.mIsOledHighBrightness && ((Math.round(ambientLux) >= Math.round(this.mBrighteningLuxThreshold) && this.mBacklightStatus == 1 && (nextBrightenTransition <= time || screenOn || pocketRinging)) || ((Math.round(ambientLux) <= Math.round(this.mDarkeningLuxThreshold) && this.mBacklightStatus == 1 && nextDarkenTransition <= time) || (Math.round(ambientLux) == 0 && this.mbStartTimer)))) {
            this.mBacklightStatus = 0;
            this.lux_beforemin = this.lux_min;
            this.lux_beforemax = this.lux_max;
            this.mareabefore = this.marea;
            if (DEBUG) {
                Slog.d(TAG, "ambientLux = " + Math.round(ambientLux) + " LuxThreshold = " + Math.round(this.mBrighteningLuxThreshold));
            }
            setAmbientLux(ambientLux);
            updateAutoBrightness(true);
            nextBrightenTransition = nextAmbientLightBrighteningTransition(time);
            nextDarkenTransition = nextAmbientLightDarkeningTransition(time);
        }
        long nextTransitionTime = Math.min(nextDarkenTransition, nextBrightenTransition);
        if (nextTransitionTime <= time) {
            nextTransitionTime = time + ((long) this.mLightSensorRate);
        }
        if (DEBUG) {
            Slog.d(TAG, "updateAmbientLux: Scheduling ambient lux update for " + nextTransitionTime + TimeUtils.formatUptime(nextTransitionTime));
        }
        this.mHandler.sendEmptyMessageAtTime(1, nextTransitionTime);
    }

    private void resetAutoBrightness(float ambientLux, float manulAtAmbientLux, int manulBrightness) {
        int lNowInterval = mOppoBrightUtils.resetAmbientLux(ambientLux);
        int i = manulBrightness;
        this.mScreenAutoBrightness = mOppoBrightUtils.calDragBrightness(clampScreenBrightness(Math.round(((float) PowerManager.BRIGHTNESS_MULTIBITS_ON) * this.mScreenAutoBrightnessSpline.interpolate(manulAtAmbientLux))), i, lNowInterval, mOppoBrightUtils.resetAmbientLux(manulAtAmbientLux), this.mScreenAutoBrightness);
    }

    /* JADX WARNING: Missing block: B:59:0x01c7, code:
            if (com.android.server.display.OppoBrightUtils.mCameraUseAdjustmentSetting != false) goto L_0x0087;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void updateAutoBrightness(boolean sendUpdate) {
        if (this.mAmbientLuxValid || this.mManulBrightnessSlide) {
            float value = this.mScreenAutoBrightnessSpline.interpolate(this.mAmbientLux);
            String str;
            StringBuilder append;
            OppoBrightUtils oppoBrightUtils;
            OppoBrightUtils oppoBrightUtils2;
            if (this.mManulBrightnessSlide) {
                if (DEBUG) {
                    str = TAG;
                    append = new StringBuilder().append("mOppoBrightUtils.mManualBrightness = ");
                    oppoBrightUtils = mOppoBrightUtils;
                    Slog.d(str, append.append(OppoBrightUtils.mManualBrightness).append(" mAmbientLux = ").append(this.mAmbientLux).toString());
                }
                oppoBrightUtils2 = mOppoBrightUtils;
                OppoBrightUtils.mManulAtAmbientLux = this.mAmbientLux;
                this.mStartManual = true;
                this.mManulBrightnessSlide = false;
                oppoBrightUtils2 = mOppoBrightUtils;
                OppoBrightUtils.mManualSetAutoBrightness = true;
                oppoBrightUtils2 = mOppoBrightUtils;
                this.mScreenAutoBrightness = OppoBrightUtils.mManualBrightness;
                this.mCallbacks.updateBrightness();
            } else {
                if (sendUpdate) {
                    oppoBrightUtils2 = mOppoBrightUtils;
                    OppoBrightUtils.mManualSetAutoBrightness = false;
                }
                int newScreenAutoBrightness = clampScreenBrightness(Math.round(((float) PowerManager.BRIGHTNESS_MULTIBITS_ON) * value));
                oppoBrightUtils2 = mOppoBrightUtils;
                OppoBrightUtils oppoBrightUtils3 = mOppoBrightUtils;
                newScreenAutoBrightness = oppoBrightUtils2.findNightBrightness(OppoBrightUtils.mDisplayStateOn, newScreenAutoBrightness);
                if (this.mScreenAutoBrightness == newScreenAutoBrightness) {
                    oppoBrightUtils2 = mOppoBrightUtils;
                    if (OppoBrightUtils.mCameraMode != 1) {
                        oppoBrightUtils2 = mOppoBrightUtils;
                    }
                }
                if (DEBUG) {
                    str = TAG;
                    append = new StringBuilder().append("mScreenAutoBrightness = ").append(this.mScreenAutoBrightness).append(" newScreenAutoBrightness = ").append(newScreenAutoBrightness).append(" mOppoBrightUtils.mManualBrightness = ");
                    oppoBrightUtils = mOppoBrightUtils;
                    append = append.append(OppoBrightUtils.mManualBrightness).append(" mStartManual = ").append(this.mStartManual).append(" mOppoBrightUtils.mManualBrightnessBackup = ");
                    oppoBrightUtils = mOppoBrightUtils;
                    append = append.append(OppoBrightUtils.mManualBrightnessBackup).append(" mOppoBrightUtils.mDisplayStateOn = ");
                    oppoBrightUtils = mOppoBrightUtils;
                    append = append.append(OppoBrightUtils.mDisplayStateOn).append(" mBrightnessOverride = ");
                    oppoBrightUtils = mOppoBrightUtils;
                    Slog.d(str, append.append(OppoBrightUtils.mBrightnessOverride).toString());
                }
                int autoBrightness = this.mScreenAutoBrightness;
                this.mScreenAutoBrightness = newScreenAutoBrightness;
                this.mLastScreenAutoBrightnessGamma = TWILIGHT_ADJUSTMENT_MAX_GAMMA;
                oppoBrightUtils2 = mOppoBrightUtils;
                if (OppoBrightUtils.mDisplayStateOn) {
                    oppoBrightUtils2 = mOppoBrightUtils;
                    if (OppoBrightUtils.mManualBrightnessBackup != 0) {
                        this.mStartManual = true;
                        oppoBrightUtils2 = mOppoBrightUtils;
                        oppoBrightUtils2 = mOppoBrightUtils;
                        OppoBrightUtils.mManualBrightness = OppoBrightUtils.mManualBrightnessBackup;
                        oppoBrightUtils2 = mOppoBrightUtils;
                        oppoBrightUtils2 = mOppoBrightUtils;
                        OppoBrightUtils.mManulAtAmbientLux = OppoBrightUtils.mManualAmbientLuxBackup;
                        oppoBrightUtils2 = mOppoBrightUtils;
                        OppoBrightUtils.mManualBrightnessBackup = 0;
                        oppoBrightUtils2 = mOppoBrightUtils;
                        OppoBrightUtils.mManualAmbientLuxBackup = OppoBrightUtils.MIN_LUX_LIMITI;
                    }
                }
                oppoBrightUtils2 = mOppoBrightUtils;
                if (OppoBrightUtils.mBrightnessOverride == 0) {
                    oppoBrightUtils2 = mOppoBrightUtils;
                    if (OppoBrightUtils.mBrightnessOverrideAdj != 0 && this.mLightSensorEnabled) {
                        this.mStartManual = true;
                        oppoBrightUtils2 = mOppoBrightUtils;
                        oppoBrightUtils2 = mOppoBrightUtils;
                        OppoBrightUtils.mManualBrightness = OppoBrightUtils.mBrightnessOverrideAdj;
                        oppoBrightUtils2 = mOppoBrightUtils;
                        oppoBrightUtils2 = mOppoBrightUtils;
                        OppoBrightUtils.mManulAtAmbientLux = OppoBrightUtils.mBrightnessOverrideAmbientLux;
                        oppoBrightUtils2 = mOppoBrightUtils;
                        OppoBrightUtils.mBrightnessOverride = -1;
                        oppoBrightUtils2 = mOppoBrightUtils;
                        OppoBrightUtils.mBrightnessOverrideAdj = 0;
                        oppoBrightUtils2 = mOppoBrightUtils;
                        OppoBrightUtils.mBrightnessOverrideAmbientLux = OppoBrightUtils.MIN_LUX_LIMITI;
                    }
                }
                if (this.mStartManual && this.mLightSensorEnabled) {
                    float f = this.mAmbientLux;
                    oppoBrightUtils3 = mOppoBrightUtils;
                    if (f == OppoBrightUtils.mManulAtAmbientLux) {
                        oppoBrightUtils2 = mOppoBrightUtils;
                        this.mScreenAutoBrightness = OppoBrightUtils.mManualBrightness;
                    } else {
                        f = this.mAmbientLux;
                        oppoBrightUtils3 = mOppoBrightUtils;
                        float f2 = OppoBrightUtils.mManulAtAmbientLux;
                        oppoBrightUtils = mOppoBrightUtils;
                        resetAutoBrightness(f, f2, OppoBrightUtils.mManualBrightness);
                        f = this.mAmbientLux;
                        oppoBrightUtils3 = mOppoBrightUtils;
                        int i;
                        if (f > OppoBrightUtils.mManulAtAmbientLux) {
                            i = this.mScreenAutoBrightness;
                            oppoBrightUtils3 = mOppoBrightUtils;
                            this.mScreenAutoBrightness = Math.max(i, OppoBrightUtils.mManualBrightness);
                        } else {
                            f = this.mAmbientLux;
                            oppoBrightUtils3 = mOppoBrightUtils;
                            if (f < OppoBrightUtils.mManulAtAmbientLux) {
                                i = this.mScreenAutoBrightness;
                                oppoBrightUtils3 = mOppoBrightUtils;
                                this.mScreenAutoBrightness = Math.min(i, OppoBrightUtils.mManualBrightness);
                            }
                        }
                    }
                }
                oppoBrightUtils2 = mOppoBrightUtils;
                if (OppoBrightUtils.mCameraMode == 1) {
                    oppoBrightUtils2 = mOppoBrightUtils;
                    if (OppoBrightUtils.mCameraBacklight) {
                        this.mScreenAutoBrightness = mOppoBrightUtils.adjustCameraBrightness(this.mScreenAutoBrightness);
                    } else {
                        oppoBrightUtils2 = mOppoBrightUtils;
                        if (OppoBrightUtils.mGalleryBacklight) {
                            Slog.d(TAG, "There is no request in Gallery, do nothing");
                        }
                    }
                }
                oppoBrightUtils2 = mOppoBrightUtils;
                if (OppoBrightUtils.mBrightnessBitsConfig != 1) {
                    this.mAutoRate = mOppoBrightUtils.caclurateRateForIndex(this.mDeltaLux, autoBrightness, this.mScreenAutoBrightness);
                }
                if (sendUpdate) {
                    this.mCallbacks.updateBrightness();
                }
                oppoBrightUtils2 = mOppoBrightUtils;
                if (OppoBrightUtils.mDisplayStateOn && this.mLightSensorEnabled) {
                    oppoBrightUtils2 = mOppoBrightUtils;
                    if (OppoBrightUtils.mManualBrightnessBackup != 0) {
                        this.mStartManual = true;
                        oppoBrightUtils2 = mOppoBrightUtils;
                        oppoBrightUtils2 = mOppoBrightUtils;
                        OppoBrightUtils.mManualBrightness = OppoBrightUtils.mManualBrightnessBackup;
                        oppoBrightUtils2 = mOppoBrightUtils;
                        OppoBrightUtils.mManualBrightnessBackup = 0;
                    }
                }
                oppoBrightUtils2 = mOppoBrightUtils;
                OppoBrightUtils.mDisplayStateOn = false;
            }
        }
    }

    private int clampScreenBrightness(int value) {
        return MathUtils.constrain(value, this.mScreenBrightnessRangeMinimum, this.mScreenBrightnessRangeMaximum);
    }

    private void prepareBrightnessAdjustmentSample() {
        if (this.mBrightnessAdjustmentSamplePending) {
            this.mHandler.removeMessages(2);
        } else {
            this.mBrightnessAdjustmentSamplePending = true;
            this.mBrightnessAdjustmentSampleOldAdjustment = this.mScreenAutoBrightnessAdjustment;
            this.mBrightnessAdjustmentSampleOldLux = this.mAmbientLuxValid ? this.mAmbientLux : -1.0f;
            this.mBrightnessAdjustmentSampleOldBrightness = this.mScreenAutoBrightness;
            this.mBrightnessAdjustmentSampleOldGamma = this.mLastScreenAutoBrightnessGamma;
        }
        this.mHandler.sendEmptyMessageDelayed(2, 10000);
    }

    private void cancelBrightnessAdjustmentSample() {
        if (this.mBrightnessAdjustmentSamplePending) {
            this.mBrightnessAdjustmentSamplePending = false;
            this.mHandler.removeMessages(2);
        }
    }

    private void collectBrightnessAdjustmentSample() {
        if (this.mBrightnessAdjustmentSamplePending) {
            this.mBrightnessAdjustmentSamplePending = false;
            if (this.mAmbientLuxValid && this.mScreenAutoBrightness >= 0) {
                if (DEBUG) {
                    Slog.d(TAG, "Auto-brightness adjustment changed by user: adj=" + this.mScreenAutoBrightnessAdjustment + ", lux=" + this.mAmbientLux + ", brightness=" + this.mScreenAutoBrightness + ", gamma=" + this.mLastScreenAutoBrightnessGamma + ", ring=" + this.mAmbientLightRingBuffer);
                }
                Object[] objArr = new Object[8];
                objArr[0] = Float.valueOf(this.mBrightnessAdjustmentSampleOldAdjustment);
                objArr[1] = Float.valueOf(this.mBrightnessAdjustmentSampleOldLux);
                objArr[2] = Integer.valueOf(this.mBrightnessAdjustmentSampleOldBrightness);
                objArr[3] = Float.valueOf(this.mBrightnessAdjustmentSampleOldGamma);
                objArr[4] = Float.valueOf(this.mScreenAutoBrightnessAdjustment);
                objArr[5] = Float.valueOf(this.mAmbientLux);
                objArr[6] = Integer.valueOf(this.mScreenAutoBrightness);
                objArr[7] = Float.valueOf(this.mLastScreenAutoBrightnessGamma);
                EventLog.writeEvent(EventLogTags.AUTO_BRIGHTNESS_ADJ, objArr);
            }
        }
    }

    public void setScreenAutoBrightnessSpline(Spline spline) {
        this.mRuntimeConfig.mScreenAutoBrightnessSpline = spline;
    }

    public void updateRuntimeConfig() {
        this.mHandler.sendEmptyMessage(3);
    }

    private void updateRuntimeConfigInternal() {
        this.mScreenAutoBrightnessSpline = this.mRuntimeConfig.mScreenAutoBrightnessSpline;
        updateAutoBrightness(true);
    }
}
