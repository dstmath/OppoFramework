package com.android.server.display;

import android.content.ContentResolver;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.display.DisplayManagerInternal;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Slog;
import android.util.Spline;
import com.android.server.lights.OppoLightsService;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class OppoDisplayPowerControlBrightnessHelper {
    private static final int ALWAYSON_SENSOR_RATE_US = 500000;
    /* access modifiers changed from: private */
    public static boolean DEBUG = true;
    private static final long LCD_HIGH_BRIGHTNESS_STATE_DELAY = 2000;
    private static final int MSG_FIRST_INDEX = 100;
    private static final int MSG_GAMESPACE_IN_OUT_TIMEOUT = 102;
    private static final int MSG_REPORT_MANUL_BRIGHTNESS = 201;
    private static final int MSG_SCREEN_ON_BRIGHTNESS_BOOST = 101;
    private static final int MSG_UPDATE_HIGH_BRIGHTNESS_STATE = 200;
    private static final long OLED_HIGH_BRIGHTNESS_STATE_DELAY = 5000;
    private static final String TAG = "OppoDisplayPowerControlBrightnessHelper";
    /* access modifiers changed from: private */
    public static int mHighBrightnessMode = 0;
    /* access modifiers changed from: private */
    public static float mLux = OppoBrightUtils.MIN_LUX_LIMITI;
    /* access modifiers changed from: private */
    public static OppoBrightUtils mOppoBrightUtils;
    /* access modifiers changed from: private */
    public static int mPreHighBrightnessMode = 0;
    public static boolean mQuickDarkToBright = false;
    public static boolean mScreenDimQuicklyDark = false;
    private static boolean mStartTimer = false;
    /* access modifiers changed from: private */
    public static long manulDurationTime = System.currentTimeMillis();
    private static long manulTime = System.currentTimeMillis();
    /* access modifiers changed from: private */
    public OppoAutomaticBrightnessController mAutomaticBrightnessController;
    private Context mContext;
    /* access modifiers changed from: private */
    public DisplayPowerController mDpc = null;
    private IBinder mFlinger = null;
    /* access modifiers changed from: private */
    public DisplayBrightnessHandler mHandler = null;
    /* access modifiers changed from: private */
    public float mLastManulBrightness = 127.0f;
    /* access modifiers changed from: private */
    public Sensor mLightSensor;
    /* access modifiers changed from: private */
    public boolean mLightSensorAlwaysOn = false;
    /* access modifiers changed from: private */
    public final SensorEventListener mLightSensorAlwaysOnListener = new SensorEventListener() {
        /* class com.android.server.display.OppoDisplayPowerControlBrightnessHelper.AnonymousClass2 */
        private float mPrevLux = OppoBrightUtils.MIN_LUX_LIMITI;
        private long mPrevTime = 0;

        /* JADX WARNING: Code restructure failed: missing block: B:44:0x0128, code lost:
            if (com.android.server.display.OppoBrightUtils.mCameraMode == 1) goto L_0x012a;
         */
        public void onSensorChanged(SensorEvent event) {
            if (OppoDisplayPowerControlBrightnessHelper.this.mLightSensorAlwaysOn && !OppoDisplayPowerControlBrightnessHelper.this.mDpc.mScreenBrightnessRampAnimator.isAnimating()) {
                float lLux = event.values[0];
                float unused = OppoDisplayPowerControlBrightnessHelper.mLux = lLux;
                long time = SystemClock.uptimeMillis();
                long tempTime = time - this.mPrevTime;
                int modeSource = -1;
                if (OppoBrightUtils.mScreenGlobalHBMSupport && OppoBrightUtils.sHbmLevels.size() != 0) {
                    OppoBrightUtils.HBM_LUX_LEVEL_FOUR = OppoBrightUtils.sHbmLevels.get(0).floatValue();
                }
                if (OppoDisplayPowerControlBrightnessHelper.DEBUG) {
                    Slog.d(OppoDisplayPowerControlBrightnessHelper.TAG, "PowerMS LHbm-Sensor Changed lux=" + lLux + " limit = " + OppoBrightUtils.HBM_LUX_LEVEL_FOUR);
                }
                if (OppoBrightUtils.mHighBrightnessModeSupport) {
                    if (lLux >= OppoBrightUtils.HBM_LUX_LEVEL_FOUR) {
                        if (OppoBrightUtils.mScreenGlobalHBMSupport) {
                            int size = OppoBrightUtils.sHbmLevels.size();
                            int i = 1;
                            while (true) {
                                if (i > size) {
                                    break;
                                } else if (lLux >= OppoBrightUtils.sHbmLevels.get(size - i).floatValue()) {
                                    int unused2 = OppoDisplayPowerControlBrightnessHelper.mHighBrightnessMode = OppoBrightUtils.brighness_int_config[OppoBrightUtils.BRIGHTNESS_STEPS - i];
                                    modeSource = 1;
                                    break;
                                } else {
                                    i++;
                                }
                            }
                            if (!OppoDisplayPowerControlBrightnessHelper.this.mAutomaticBrightnessController.mLightSensorEnabled) {
                                int watchSize = OppoBrightUtils.sManuLuxWatchPointTotalTimes.size();
                                if (watchSize == size) {
                                    for (int i2 = 0; i2 < watchSize; i2++) {
                                        if (lLux >= OppoBrightUtils.sHbmLevels.get(i2).floatValue() && this.mPrevLux >= OppoBrightUtils.sHbmLevels.get(i2).floatValue() && tempTime > 0 && tempTime <= 1000) {
                                            OppoBrightUtils.sManuLuxWatchPointTotalTimes.set(i2, Long.valueOf(OppoBrightUtils.sManuLuxWatchPointTotalTimes.get(i2).longValue() + tempTime));
                                        }
                                    }
                                } else {
                                    Slog.e(OppoDisplayPowerControlBrightnessHelper.TAG, "onSensorChanged watchSize must be equals size=" + size + " watchSize=" + watchSize);
                                }
                            }
                        } else {
                            OppoBrightUtils unused3 = OppoDisplayPowerControlBrightnessHelper.mOppoBrightUtils;
                            if (OppoBrightUtils.mGalleryMode != 1) {
                                OppoBrightUtils unused4 = OppoDisplayPowerControlBrightnessHelper.mOppoBrightUtils;
                            }
                            int i3 = OppoLightsService.mScreenBrightness;
                            OppoBrightUtils unused5 = OppoDisplayPowerControlBrightnessHelper.mOppoBrightUtils;
                            if (i3 == OppoBrightUtils.mMaxBrightness) {
                                int unused6 = OppoDisplayPowerControlBrightnessHelper.mHighBrightnessMode = 8;
                                modeSource = 2;
                            }
                        }
                    } else if (!OppoBrightUtils.mScreenGlobalHBMSupport) {
                        int unused7 = OppoDisplayPowerControlBrightnessHelper.mHighBrightnessMode = 0;
                        modeSource = 5;
                    } else if (OppoDisplayPowerControlBrightnessHelper.this.mAutomaticBrightnessController.mLightSensorEnabled) {
                        int unused8 = OppoDisplayPowerControlBrightnessHelper.mHighBrightnessMode = OppoDisplayPowerControlBrightnessHelper.this.mAutomaticBrightnessController.getAutomaticScreenBrightness();
                        modeSource = 3;
                    } else {
                        int unused9 = OppoDisplayPowerControlBrightnessHelper.mHighBrightnessMode = OppoDisplayPowerControlBrightnessHelper.this.getScreenBrightnessSetting();
                        int unused10 = OppoDisplayPowerControlBrightnessHelper.mHighBrightnessMode = OppoDisplayPowerControlBrightnessHelper.mHighBrightnessMode > OppoBrightUtils.mMaxBrightness ? OppoBrightUtils.mMaxBrightness : OppoDisplayPowerControlBrightnessHelper.mHighBrightnessMode;
                        modeSource = 4;
                    }
                } else if (lLux >= OppoBrightUtils.HBM_LUX_LEVEL_FOUR) {
                    int unused11 = OppoDisplayPowerControlBrightnessHelper.mHighBrightnessMode = 1;
                    modeSource = 6;
                } else {
                    int unused12 = OppoDisplayPowerControlBrightnessHelper.mHighBrightnessMode = 0;
                    modeSource = 7;
                }
                if (OppoDisplayPowerControlBrightnessHelper.DEBUG) {
                    Slog.d(OppoDisplayPowerControlBrightnessHelper.TAG, "PowerMS LHbm-Sensor Changed preMode=" + OppoDisplayPowerControlBrightnessHelper.mPreHighBrightnessMode + " mode = " + OppoDisplayPowerControlBrightnessHelper.mHighBrightnessMode + " source=" + modeSource);
                }
                if (OppoDisplayPowerControlBrightnessHelper.mOppoBrightUtils.isAIBrightnessOpen()) {
                    OppoDisplayPowerControlBrightnessHelper.mOppoBrightUtils.setAIBrightnessLux(lLux);
                    if (OppoDisplayPowerControlBrightnessHelper.DEBUG) {
                        Slog.d(OppoDisplayPowerControlBrightnessHelper.TAG, "Set AI Brightness Lux:lux=" + lLux);
                    }
                } else if (OppoDisplayPowerControlBrightnessHelper.mHighBrightnessMode != OppoDisplayPowerControlBrightnessHelper.mPreHighBrightnessMode) {
                    int unused13 = OppoDisplayPowerControlBrightnessHelper.mPreHighBrightnessMode = OppoDisplayPowerControlBrightnessHelper.mHighBrightnessMode;
                    OppoDisplayPowerControlBrightnessHelper.this.stopTimer();
                    OppoDisplayPowerControlBrightnessHelper.this.startTimer();
                }
                this.mPrevTime = time;
                this.mPrevLux = lLux;
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    /* access modifiers changed from: private */
    public HashMap<String, String> mReportMap;
    private int mScreenState;
    /* access modifiers changed from: private */
    public SensorManager mSensorManager = null;
    private TimerTask mTask;
    /* access modifiers changed from: private */
    public Handler mTimehandler = new Handler() {
        /* class com.android.server.display.OppoDisplayPowerControlBrightnessHelper.AnonymousClass3 */

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what != 200) {
                return;
            }
            if (!OppoDisplayPowerControlBrightnessHelper.this.mDpc.mScreenBrightnessRampAnimator.isAnimating()) {
                if (OppoBrightUtils.mScreenGlobalHBMSupport) {
                    if (OppoDisplayPowerControlBrightnessHelper.mHighBrightnessMode == OppoDisplayPowerControlBrightnessHelper.mPreHighBrightnessMode || OppoDisplayPowerControlBrightnessHelper.mHighBrightnessMode < OppoBrightUtils.mMaxBrightness) {
                        if (OppoDisplayPowerControlBrightnessHelper.this.getScreenBrightnessSetting() >= OppoDisplayPowerControlBrightnessHelper.mHighBrightnessMode || OppoDisplayPowerControlBrightnessHelper.mHighBrightnessMode > OppoBrightUtils.mMaxBrightness) {
                            Slog.d(OppoDisplayPowerControlBrightnessHelper.TAG, "handleMessage set HBM to mHighBrightnessMode=" + OppoDisplayPowerControlBrightnessHelper.mHighBrightnessMode);
                        } else {
                            int unused = OppoDisplayPowerControlBrightnessHelper.mHighBrightnessMode = OppoDisplayPowerControlBrightnessHelper.this.getScreenBrightnessSetting();
                            int unused2 = OppoDisplayPowerControlBrightnessHelper.mPreHighBrightnessMode = OppoDisplayPowerControlBrightnessHelper.mHighBrightnessMode;
                            Slog.d(OppoDisplayPowerControlBrightnessHelper.TAG, "handleMessage:already exit HBM mode, cur brightness=" + OppoDisplayPowerControlBrightnessHelper.mHighBrightnessMode);
                        }
                    }
                } else if (OppoDisplayPowerControlBrightnessHelper.mHighBrightnessMode == OppoDisplayPowerControlBrightnessHelper.mPreHighBrightnessMode) {
                    OppoDisplayPowerControlBrightnessHelper.mOppoBrightUtils.setHighBrightness(OppoDisplayPowerControlBrightnessHelper.mHighBrightnessMode);
                }
                OppoDisplayPowerControlBrightnessHelper.this.stopTimer();
            } else if (!OppoDisplayPowerControlBrightnessHelper.mOppoBrightUtils.isAIBrightnessOpen()) {
                OppoDisplayPowerControlBrightnessHelper.this.stopTimer();
                OppoDisplayPowerControlBrightnessHelper.this.startTimer();
            }
        }
    };
    private Timer mTimer;

    public OppoDisplayPowerControlBrightnessHelper(DisplayPowerController controller, OppoAutomaticBrightnessController automaticBrightnessController, SensorManager sensorManager, Context context, Handler handler) {
        this.mDpc = controller;
        this.mContext = context;
        this.mSensorManager = sensorManager;
        this.mHandler = new DisplayBrightnessHandler(handler.getLooper());
        this.mAutomaticBrightnessController = automaticBrightnessController;
        this.mScreenState = -1;
        mQuickDarkToBright = false;
        mOppoBrightUtils = OppoBrightUtils.getInstance();
        OppoBrightUtils oppoBrightUtils = mOppoBrightUtils;
        DEBUG = OppoBrightUtils.DEBUG;
        this.mLightSensor = this.mSensorManager.getDefaultSensor(5);
    }

    /* access modifiers changed from: private */
    public int getScreenBrightnessSetting() {
        return Settings.System.getIntForBrightness(this.mContext.getContentResolver(), "screen_brightness", mOppoBrightUtils.getDefaultScreenBrightnessSetting(), -2);
    }

    public void setLightSensorAlwaysOn(boolean enable) {
        if (enable) {
            if (OppoBrightUtils.mScreenGlobalHBMSupport) {
                int i = OppoLightsService.mScreenBrightness;
                OppoBrightUtils oppoBrightUtils = mOppoBrightUtils;
                if (i < OppoBrightUtils.mMaxBrightness) {
                    return;
                }
            }
            if (!this.mLightSensorAlwaysOn) {
                this.mLightSensorAlwaysOn = true;
                Slog.d(TAG, "setLightSensorAlwaysOn = " + enable);
                Thread thread = new Thread(new Runnable() {
                    /* class com.android.server.display.OppoDisplayPowerControlBrightnessHelper.AnonymousClass1 */

                    public void run() {
                        OppoDisplayPowerControlBrightnessHelper.this.mSensorManager.registerListener(OppoDisplayPowerControlBrightnessHelper.this.mLightSensorAlwaysOnListener, OppoDisplayPowerControlBrightnessHelper.this.mLightSensor, OppoDisplayPowerControlBrightnessHelper.ALWAYSON_SENSOR_RATE_US, OppoDisplayPowerControlBrightnessHelper.this.mHandler);
                    }
                }, "LightSensorAlwaysOnThread");
                thread.setPriority(10);
                thread.start();
            }
        } else if (this.mLightSensorAlwaysOn && !enable) {
            this.mLightSensorAlwaysOn = false;
            Slog.d(TAG, "setLightSensorAlwaysOn = " + enable);
            this.mSensorManager.unregisterListener(this.mLightSensorAlwaysOnListener);
            if (OppoBrightUtils.mScreenGlobalHBMSupport) {
                mLux = OppoBrightUtils.MIN_LUX_LIMITI;
                stopTimer();
                int i2 = mHighBrightnessMode;
                OppoBrightUtils oppoBrightUtils2 = mOppoBrightUtils;
                if (i2 > OppoBrightUtils.mMaxBrightness) {
                    if (this.mAutomaticBrightnessController.mLightSensorEnabled) {
                        mHighBrightnessMode = this.mAutomaticBrightnessController.getAutomaticScreenBrightness();
                        Slog.d(TAG, "Auto mLightSensorAlwaysOnListener close mHighBrightnessMode=" + mHighBrightnessMode);
                    } else {
                        mHighBrightnessMode = getScreenBrightnessSetting();
                        Slog.d(TAG, "Manual mLightSensorAlwaysOnListener close mHighBrightnessMode=" + mHighBrightnessMode);
                    }
                    mPreHighBrightnessMode = mHighBrightnessMode;
                    Message msg = new Message();
                    msg.what = 200;
                    this.mTimehandler.sendMessage(msg);
                    return;
                }
                return;
            }
            mHighBrightnessMode = 0;
            mPreHighBrightnessMode = 0;
            mLux = OppoBrightUtils.MIN_LUX_LIMITI;
            stopTimer();
            mOppoBrightUtils.setHighBrightness(mHighBrightnessMode);
        }
    }

    public void setTemporaryAutoBrightnessAdjustment(float adjustment) {
        if (adjustment == (((float) PowerManager.BRIGHTNESS_MULTIBITS_ON) * 300.0f) / 255.0f || adjustment == (((float) PowerManager.BRIGHTNESS_MULTIBITS_ON) * 301.0f) / 255.0f) {
            Slog.d(TAG, "adjustment == " + adjustment);
            if (adjustment == (((float) PowerManager.BRIGHTNESS_MULTIBITS_ON) * 300.0f) / 255.0f) {
                Slog.d(TAG, "Camera(boot gallery from camera)");
                OppoBrightUtils oppoBrightUtils = mOppoBrightUtils;
                OppoBrightUtils.mCameraBacklight = true;
            } else {
                Slog.d(TAG, "boot gallery from desk or file system");
                OppoBrightUtils oppoBrightUtils2 = mOppoBrightUtils;
                OppoBrightUtils.mGalleryBacklight = true;
            }
            OppoBrightUtils oppoBrightUtils3 = mOppoBrightUtils;
            OppoBrightUtils.mCameraMode = 1;
            OppoBrightUtils oppoBrightUtils4 = mOppoBrightUtils;
            OppoBrightUtils.mCameraUseAdjustmentSetting = true;
            setOutdoorMode(true);
        } else if (adjustment == (((float) PowerManager.BRIGHTNESS_MULTIBITS_ON) * 500.0f) / 255.0f) {
            Slog.d(TAG, "adjustment == " + adjustment);
            OppoBrightUtils oppoBrightUtils5 = mOppoBrightUtils;
            OppoBrightUtils.mCameraBacklight = false;
            OppoBrightUtils oppoBrightUtils6 = mOppoBrightUtils;
            OppoBrightUtils.mGalleryBacklight = false;
            OppoBrightUtils oppoBrightUtils7 = mOppoBrightUtils;
            OppoBrightUtils.mCameraMode = 0;
            OppoBrightUtils oppoBrightUtils8 = mOppoBrightUtils;
            OppoBrightUtils.mCameraUseAdjustmentSetting = true;
            setOutdoorMode(false);
            if (Settings.System.getIntForUser(this.mContext.getContentResolver(), "screen_brightness_mode", 0, -2) == 0) {
                OppoBrightUtils oppoBrightUtils9 = mOppoBrightUtils;
                OppoBrightUtils.mCameraMode = -1;
            }
        } else if (Math.round(adjustment) == 1930286 || Math.round(adjustment) == 1930287) {
            if (Math.round(adjustment) == 1930286) {
                OppoBrightUtils oppoBrightUtils10 = mOppoBrightUtils;
                OppoBrightUtils.mSpecialBrightnessFlag |= 1;
                Slog.d(TAG, "setTemporaryAutoBrightnessAdjustment special color from gamespace IN");
            } else {
                this.mHandler.removeMessages(102);
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(102), 1200);
                Slog.d(TAG, "setTemporaryAutoBrightnessAdjustment special color from gamespace OUT");
            }
        }
        if (adjustment == 16384.0f) {
            OppoBrightUtils oppoBrightUtils11 = mOppoBrightUtils;
            OppoBrightUtils.mInverseMode = 1;
        } else if (adjustment == 32768.0f) {
            OppoBrightUtils oppoBrightUtils12 = mOppoBrightUtils;
            OppoBrightUtils.mInverseMode = 0;
        }
        if (adjustment == 16385.0f) {
            OppoBrightUtils oppoBrightUtils13 = mOppoBrightUtils;
            OppoBrightUtils.mGalleryMode = 1;
            OppoBrightUtils oppoBrightUtils14 = mOppoBrightUtils;
            OppoBrightUtils.mShouldAdjustRate = 1;
            setOutdoorMode(true);
        } else if (adjustment == 32769.0f) {
            OppoBrightUtils oppoBrightUtils15 = mOppoBrightUtils;
            OppoBrightUtils.mGalleryMode = 0;
            OppoBrightUtils oppoBrightUtils16 = mOppoBrightUtils;
            OppoBrightUtils.mShouldAdjustRate = 1;
            setOutdoorMode(false);
        }
        if (adjustment == 16386.0f) {
            OppoBrightUtils oppoBrightUtils17 = mOppoBrightUtils;
            OppoBrightUtils.mVideoMode = 1;
            OppoBrightUtils oppoBrightUtils18 = mOppoBrightUtils;
            OppoBrightUtils.mShouldAdjustRate = 1;
        } else if (adjustment == 32770.0f) {
            OppoBrightUtils oppoBrightUtils19 = mOppoBrightUtils;
            OppoBrightUtils.mVideoMode = 0;
            OppoBrightUtils oppoBrightUtils20 = mOppoBrightUtils;
            OppoBrightUtils.mShouldAdjustRate = 1;
        }
        if (mOppoBrightUtils.isSpecialAdj(this.mDpc.mTemporaryAutoBrightnessAdjustment)) {
            OppoBrightUtils oppoBrightUtils21 = mOppoBrightUtils;
            OppoBrightUtils.mShouldFastRate = true;
        }
        DisplayPowerController displayPowerController = this.mDpc;
        if (DisplayPowerController.DEBUG_PANIC || DEBUG) {
            Slog.d(TAG, "setTemporaryAutoBrightnessAdjustment = " + adjustment);
        }
    }

    public void setOutdoorMode(boolean enable) {
        OppoBrightUtils oppoBrightUtils = mOppoBrightUtils;
        if (OppoBrightUtils.mScreenGlobalHBMSupport || !OppoBrightUtils.mHighBrightnessModeSupport) {
            return;
        }
        if (enable) {
            setLightSensorAlwaysOn(true);
        } else {
            setLightSensorAlwaysOn(false);
        }
    }

    /* access modifiers changed from: private */
    public void startTimer() {
        if (!mStartTimer) {
            synchronized (this) {
                mStartTimer = true;
                if (this.mTimer == null) {
                    this.mTimer = new Timer();
                }
                if (this.mTask == null) {
                    this.mTask = new TimerTask() {
                        /* class com.android.server.display.OppoDisplayPowerControlBrightnessHelper.AnonymousClass4 */

                        public void run() {
                            Message msg = new Message();
                            msg.what = 200;
                            OppoDisplayPowerControlBrightnessHelper.this.mTimehandler.sendMessage(msg);
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

    /* access modifiers changed from: private */
    public void stopTimer() {
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
        }
    }

    public static void setManulReportTime() {
        manulDurationTime = System.currentTimeMillis();
        manulTime = System.currentTimeMillis();
    }

    public void setQuickDarkToBrightStatus(DisplayManagerInternal.DisplayPowerRequest powerrequest) {
        if (powerrequest.policy == 2 && this.mScreenState == 3) {
            mQuickDarkToBright = true;
        }
        this.mScreenState = powerrequest.policy;
    }

    public void setDimQuickDarkStatus() {
        mScreenDimQuicklyDark = true;
        OppoBrightUtils oppoBrightUtils = mOppoBrightUtils;
        OppoBrightUtils.mManualSetAutoBrightness = false;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:6:0x0011, code lost:
        if (com.android.server.display.OppoBrightUtils.mBrightnessBoost == 2) goto L_0x0013;
     */
    public int caculateRate(boolean slowChange, boolean autoBrightnessEnabled) {
        int rate = OppoBrightUtils.BRIGHTNESS_RAMP_RATE_SLOW;
        OppoAutomaticBrightnessController oppoAutomaticBrightnessController = this.mAutomaticBrightnessController;
        if (oppoAutomaticBrightnessController != null && autoBrightnessEnabled) {
            rate = oppoAutomaticBrightnessController.mAutoRate;
        }
        if (slowChange) {
            OppoBrightUtils oppoBrightUtils = mOppoBrightUtils;
        }
        rate = OppoBrightUtils.BRIGHTNESS_RAMP_RATE_FAST;
        OppoBrightUtils oppoBrightUtils2 = mOppoBrightUtils;
        if (OppoBrightUtils.mBrightnessBoost == 3) {
            rate = OppoBrightUtils.BRIGHTNESS_RAMP_RATE_FAST;
            this.mHandler.removeMessages(101);
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(101), 600);
        }
        if (mQuickDarkToBright) {
            mQuickDarkToBright = false;
            rate = OppoBrightUtils.BRIGHTNESS_RAMP_RATE_SCREENON;
        }
        OppoBrightUtils oppoBrightUtils3 = mOppoBrightUtils;
        if (OppoBrightUtils.mPocketRingingState) {
            mOppoBrightUtils.setPocketRingingState(false);
            rate = OppoBrightUtils.BRIGHTNESS_RAMP_RATE_FAST;
        }
        OppoBrightUtils oppoBrightUtils4 = mOppoBrightUtils;
        if (OppoBrightUtils.mShouldAdjustRate != 1) {
            return rate;
        }
        OppoBrightUtils oppoBrightUtils5 = mOppoBrightUtils;
        OppoBrightUtils.mShouldAdjustRate = 0;
        return 70;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0045, code lost:
        if (com.android.server.display.OppoBrightUtils.mVideoMode == 1) goto L_0x0047;
     */
    public int caculateBrightness(int brightness, boolean autoBrightnessEnabled) {
        if (!autoBrightnessEnabled) {
            OppoAutomaticBrightnessController oppoAutomaticBrightnessController = this.mAutomaticBrightnessController;
            if (oppoAutomaticBrightnessController != null) {
                oppoAutomaticBrightnessController.mScreenAutoBrightness = brightness;
            }
            if (System.currentTimeMillis() - manulTime > LCD_HIGH_BRIGHTNESS_STATE_DELAY) {
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(MSG_REPORT_MANUL_BRIGHTNESS), LCD_HIGH_BRIGHTNESS_STATE_DELAY);
                manulTime = System.currentTimeMillis();
            }
        }
        OppoBrightUtils oppoBrightUtils = mOppoBrightUtils;
        if (OppoBrightUtils.mInverseMode == 1) {
            brightness = mOppoBrightUtils.adjustInverseModeBrightness(brightness);
        }
        OppoBrightUtils oppoBrightUtils2 = mOppoBrightUtils;
        if (OppoBrightUtils.mCameraMode != 1) {
            OppoBrightUtils oppoBrightUtils3 = mOppoBrightUtils;
            if (OppoBrightUtils.mGalleryMode != 1) {
                OppoBrightUtils oppoBrightUtils4 = mOppoBrightUtils;
            }
        }
        if (brightness >= 250) {
            return brightness;
        }
        if (brightness > 2) {
            return (brightness * 93) / 100;
        }
        return brightness;
    }

    public void doFirstSetScreenStateAction(int state, DisplayManagerInternal.DisplayPowerRequest powerrequest) {
        if (state == 2) {
            OppoBrightUtils oppoBrightUtils = mOppoBrightUtils;
            if (OppoBrightUtils.mFirstSetScreenState) {
                if (powerrequest.useAutoBrightness) {
                    OppoBrightUtils oppoBrightUtils2 = mOppoBrightUtils;
                    OppoBrightUtils.mBrightnessBoost = 1;
                    this.mHandler.removeMessages(101);
                    this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(101), 4000);
                    OppoBrightUtils oppoBrightUtils3 = mOppoBrightUtils;
                    OppoBrightUtils.mDisplayStateOn = true;
                    OppoBrightUtils oppoBrightUtils4 = mOppoBrightUtils;
                    OppoBrightUtils.mFirstSetScreenState = false;
                    OppoBrightUtils oppoBrightUtils5 = mOppoBrightUtils;
                    OppoBrightUtils.mManualBrightnessBackup = (int) Settings.System.getFloatForUser(this.mContext.getContentResolver(), "screen_auto_brightness_adj", OppoBrightUtils.MIN_LUX_LIMITI, -2);
                    OppoBrightUtils oppoBrightUtils6 = mOppoBrightUtils;
                    OppoBrightUtils.mManualAmbientLuxBackup = Settings.System.getFloatForUser(this.mContext.getContentResolver(), "autobrightness_manul_ambient", OppoBrightUtils.MIN_LUX_LIMITI, -2);
                    if (DEBUG) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("first setScreenState mManulLux = ");
                        OppoBrightUtils oppoBrightUtils7 = mOppoBrightUtils;
                        sb.append(OppoBrightUtils.mManulAtAmbientLux);
                        sb.append(" mManuColor = ");
                        OppoBrightUtils oppoBrightUtils8 = mOppoBrightUtils;
                        sb.append(OppoBrightUtils.mManualBrightness);
                        sb.append(" mManuLuxBak = ");
                        OppoBrightUtils oppoBrightUtils9 = mOppoBrightUtils;
                        sb.append(OppoBrightUtils.mManualAmbientLuxBackup);
                        sb.append(" mManuColorBak = ");
                        OppoBrightUtils oppoBrightUtils10 = mOppoBrightUtils;
                        sb.append(OppoBrightUtils.mManualBrightnessBackup);
                        Slog.d(TAG, sb.toString());
                    }
                } else {
                    OppoBrightUtils oppoBrightUtils11 = mOppoBrightUtils;
                    OppoBrightUtils.mFirstSetScreenState = false;
                }
                if (OppoBrightUtils.mOutdoorBrightnessSupport || (OppoBrightUtils.mScreenGlobalHBMSupport && OppoBrightUtils.mHighBrightnessModeSupport)) {
                    setLightSensorAlwaysOn(true);
                }
            }
        }
    }

    public void doSetScreenStateAction(int state, DisplayPowerState PowerState, DisplayManagerInternal.DisplayPowerRequest powerrequest) {
        if (state == 1 && PowerState.getScreenState() == 2) {
            OppoBrightUtils oppoBrightUtils = mOppoBrightUtils;
            if (!OppoBrightUtils.mSaveBrightnessByShutdown) {
                OppoBrightUtils oppoBrightUtils2 = mOppoBrightUtils;
                OppoBrightUtils.mManualBrightnessBackup = OppoBrightUtils.mManualBrightness;
                OppoBrightUtils oppoBrightUtils3 = mOppoBrightUtils;
                OppoBrightUtils.mManualAmbientLuxBackup = OppoBrightUtils.mManulAtAmbientLux;
                if (DEBUG) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Display.STATE_OFF mManualBrightness = ");
                    OppoBrightUtils oppoBrightUtils4 = mOppoBrightUtils;
                    sb.append(OppoBrightUtils.mManualBrightness);
                    sb.append(" mManulAtAmbientLux = ");
                    OppoBrightUtils oppoBrightUtils5 = mOppoBrightUtils;
                    sb.append(OppoBrightUtils.mManulAtAmbientLux);
                    Slog.d(TAG, sb.toString());
                }
                ContentResolver contentResolver = this.mContext.getContentResolver();
                OppoBrightUtils oppoBrightUtils6 = mOppoBrightUtils;
                Settings.System.putFloatForUser(contentResolver, "screen_auto_brightness_adj", (float) OppoBrightUtils.mManualBrightnessBackup, -2);
                ContentResolver contentResolver2 = this.mContext.getContentResolver();
                OppoBrightUtils oppoBrightUtils7 = mOppoBrightUtils;
                Settings.System.putFloatForUser(contentResolver2, "autobrightness_manul_ambient", OppoBrightUtils.mManulAtAmbientLux, -2);
                OppoBrightUtils oppoBrightUtils8 = mOppoBrightUtils;
                OppoBrightUtils.mManualBrightness = 0;
                OppoBrightUtils oppoBrightUtils9 = mOppoBrightUtils;
                OppoBrightUtils.mManulAtAmbientLux = OppoBrightUtils.MIN_LUX_LIMITI;
            }
            OppoBrightUtils oppoBrightUtils10 = mOppoBrightUtils;
            OppoBrightUtils.mSaveBrightnessByShutdown = false;
            if (OppoAutomaticBrightnessController.mProximityNear && !OppoBrightUtils.DEBUG_PRETEND_PROX_SENSOR_ABSENT) {
                this.mDpc.mScreenBrightnessRampAnimator.updateCurrentToTarget();
            }
            if (OppoBrightUtils.mOutdoorBrightnessSupport || OppoBrightUtils.mHighBrightnessModeSupport) {
                setLightSensorAlwaysOn(false);
            }
            OppoAutomaticBrightnessController oppoAutomaticBrightnessController = this.mAutomaticBrightnessController;
            if (oppoAutomaticBrightnessController != null) {
                oppoAutomaticBrightnessController.resetLightParamsScreenOff();
            }
        } else if (state == 2) {
            if (powerrequest.useAutoBrightness) {
                OppoBrightUtils oppoBrightUtils11 = mOppoBrightUtils;
                OppoBrightUtils.mBrightnessBoost = 1;
                this.mHandler.removeMessages(101);
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(101), 4000);
            }
            OppoBrightUtils oppoBrightUtils12 = mOppoBrightUtils;
            OppoBrightUtils.mDisplayStateOn = true;
            OppoBrightUtils oppoBrightUtils13 = mOppoBrightUtils;
            OppoBrightUtils.mManualBrightnessBackup = (int) Settings.System.getFloatForUser(this.mContext.getContentResolver(), "screen_auto_brightness_adj", OppoBrightUtils.MIN_LUX_LIMITI, -2);
            OppoBrightUtils oppoBrightUtils14 = mOppoBrightUtils;
            OppoBrightUtils.mManualAmbientLuxBackup = Settings.System.getFloatForUser(this.mContext.getContentResolver(), "autobrightness_manul_ambient", OppoBrightUtils.MIN_LUX_LIMITI, -2);
            OppoBrightUtils oppoBrightUtils15 = mOppoBrightUtils;
            if (OppoBrightUtils.mManualBrightnessBackup != 0) {
                OppoBrightUtils oppoBrightUtils16 = mOppoBrightUtils;
                OppoBrightUtils.mManualBrightness = OppoBrightUtils.mManualBrightnessBackup;
                OppoBrightUtils oppoBrightUtils17 = mOppoBrightUtils;
                OppoBrightUtils.mManulAtAmbientLux = OppoBrightUtils.mManualAmbientLuxBackup;
            }
            if (DEBUG) {
                StringBuilder sb2 = new StringBuilder();
                sb2.append("Display.STATE_ON mManualBrightness ");
                OppoBrightUtils oppoBrightUtils18 = mOppoBrightUtils;
                sb2.append(OppoBrightUtils.mManualBrightness);
                Slog.d(TAG, sb2.toString());
            }
            if (OppoBrightUtils.mOutdoorBrightnessSupport || (OppoBrightUtils.mScreenGlobalHBMSupport && OppoBrightUtils.mHighBrightnessModeSupport)) {
                setLightSensorAlwaysOn(true);
            }
        }
    }

    public void putBrightnessTodatabase(int target) {
        OppoBrightUtils oppoBrightUtils = mOppoBrightUtils;
        if (OppoBrightUtils.mSetBrihgtnessSlide) {
            Settings.System.putIntForBrightness(this.mContext.getContentResolver(), "screen_brightness", target, -2);
            OppoBrightUtils oppoBrightUtils2 = mOppoBrightUtils;
            OppoBrightUtils.mSetBrihgtnessSlide = false;
        }
    }

    /* access modifiers changed from: private */
    public class DisplayBrightnessHandler extends Handler {
        public DisplayBrightnessHandler(Looper looper) {
            super(looper);
            Slog.d(OppoDisplayPowerControlBrightnessHelper.TAG, "DisplayBrightnessHandler init");
        }

        public void handleMessage(Message msg) {
            Slog.d(OppoDisplayPowerControlBrightnessHelper.TAG, "handleMessage:" + msg.what);
            int i = msg.what;
            if (i == 101) {
                OppoBrightUtils unused = OppoDisplayPowerControlBrightnessHelper.mOppoBrightUtils;
                OppoBrightUtils.mBrightnessBoost = 4;
            } else if (i == OppoDisplayPowerControlBrightnessHelper.MSG_REPORT_MANUL_BRIGHTNESS) {
                if (OppoDisplayPowerControlBrightnessHelper.this.mReportMap == null) {
                    HashMap unused2 = OppoDisplayPowerControlBrightnessHelper.this.mReportMap = new HashMap();
                }
                OppoDisplayPowerControlBrightnessHelper.this.mReportMap.put("ManulBrightness", String.valueOf(OppoDisplayPowerControlBrightnessHelper.this.mLastManulBrightness));
                OppoDisplayPowerControlBrightnessHelper.this.mReportMap.put("AmbientLux", String.valueOf(OppoDisplayPowerControlBrightnessHelper.mOppoBrightUtils.getManulAmbientLux()));
                OppoDisplayPowerControlBrightnessHelper.this.mReportMap.put("ManulDurationTime", OppoDisplayPowerControlBrightnessHelper.mOppoBrightUtils.getDurationTime(System.currentTimeMillis() - OppoDisplayPowerControlBrightnessHelper.manulDurationTime));
                OppoDisplayPowerControlBrightnessHelper.this.mReportMap.put("PackageName", OppoDisplayPowerControlBrightnessHelper.mOppoBrightUtils.getTopPackageName());
                OppoDisplayPowerControlBrightnessHelper.this.mReportMap.put("SystemTime", OppoDisplayPowerControlBrightnessHelper.mOppoBrightUtils.getSystemTime());
                OppoDisplayPowerControlBrightnessHelper.mOppoBrightUtils.autoBackLightReport("20180004", OppoDisplayPowerControlBrightnessHelper.this.mReportMap);
                long unused3 = OppoDisplayPowerControlBrightnessHelper.manulDurationTime = System.currentTimeMillis();
                if (OppoDisplayPowerControlBrightnessHelper.this.mAutomaticBrightnessController != null) {
                    OppoDisplayPowerControlBrightnessHelper oppoDisplayPowerControlBrightnessHelper = OppoDisplayPowerControlBrightnessHelper.this;
                    float unused4 = oppoDisplayPowerControlBrightnessHelper.mLastManulBrightness = (float) oppoDisplayPowerControlBrightnessHelper.mAutomaticBrightnessController.mScreenAutoBrightness;
                }
                OppoDisplayPowerControlBrightnessHelper.this.mReportMap.clear();
            }
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
            if (OppoBrightUtils.mScreenUnderLightSensorSupport) {
                x[0] = 5.0f;
                Slog.w(TAG, "ScreenUnderLightSensorSupport = true, so used first value 5.");
            }
            y[0] = OppoBrightUtils.normalizeAbsoluteBrightness(brightness[0]);
            for (int i = 1; i < n; i++) {
                x[i] = (float) lux[i - 1];
                y[i] = OppoBrightUtils.normalizeAbsoluteBrightness(brightness[i]);
            }
            Spline spline = Spline.createSpline(x, y);
            if (DEBUG) {
                Slog.d(TAG, "Auto-brightness spline: " + spline);
                for (float v = 1.0f; v < ((float) lux[lux.length - 1]) * 1.25f; v *= 1.25f) {
                    Slog.d(TAG, String.format("  %7.1f: %7.1f", Float.valueOf(v), Float.valueOf(spline.interpolate(v))));
                }
            }
            return spline;
        } catch (IllegalArgumentException ex) {
            Slog.e(TAG, "Could not create auto-brightness spline.", ex);
            return null;
        }
    }
}
