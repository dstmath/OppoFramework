package com.android.server.display;

import android.common.OppoFeatureCache;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.display.BrightnessConfiguration;
import android.hardware.display.DisplayManagerInternal;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.MathUtils;
import android.util.Slog;
import com.android.server.display.IColorAutomaticBrightnessController;
import com.android.server.display.ai.AIBrightnessModel;
import com.android.server.display.ai.bean.ModelConfig;
import com.android.server.display.ai.utils.ColorAILog;
import com.android.server.wm.ColorFreeformManagerService;
import display.DevicePropertyHelper;
import java.io.PrintWriter;

public class ColorAutomaticBrightnessController extends ColorDummyAutomaticBrightnessController {
    static final boolean DEBUG = true;
    private static final boolean DEBUG_PRETEND_LIGHT_SENSOR_ABSENT = false;
    private static final float MAX_LUX_LIMITI = 31000.0f;
    private static final float MIN_LUX_LIMITI = 0.0f;
    static final String TAG = "ColorAutomaticBrightnessController";
    private final int MSG_ON_PRINT_INTERVAL = 101;
    private final int MSG_ON_UPGRADE_MODEL = 100;
    private int mAIBrightness;
    /* access modifiers changed from: private */
    public AIBrightnessModel mAIBrightnessModel;
    private AIBrightnessModelUpgradeReceiver mAIBrightnessModelUpgradeReceiver;
    /* access modifiers changed from: private */
    public AnimatorRateDelegate mAnimatorRateDelegate;
    /* access modifiers changed from: private */
    public BrightBoostDelegate mBrightBoostDelegate;
    private ColorBaseBrightnessMappingStrategy mBrightnessMapper;
    /* access modifiers changed from: private */
    public IColorAutomaticBrightnessController.Callbacks mCallbacks;
    private CameraDelegate mCameraDelegate;
    private Context mContext;
    /* access modifiers changed from: private */
    public long mDarkeningLightDebounceConfig;
    private int mDisplayPolicy = 0;
    private float mDozeScaleFactor;
    /* access modifiers changed from: private */
    public AutomaticBrightnessHandler mHandler;
    /* access modifiers changed from: private */
    public float mLastObservedLux;
    /* access modifiers changed from: private */
    public Sensor mLightSensor;
    /* access modifiers changed from: private */
    public boolean mLightSensorEnabled;
    /* access modifiers changed from: private */
    public final SensorEventListener mLightSensorListener = new SensorEventListener() {
        /* class com.android.server.display.ColorAutomaticBrightnessController.AnonymousClass2 */
        private long mPrevTime = 0;

        public void onSensorChanged(SensorEvent event) {
            if (ColorAutomaticBrightnessController.this.mLightSensorEnabled) {
                long time = SystemClock.uptimeMillis();
                if (ColorAutomaticBrightnessController.this.mBrightBoostDelegate.getBrightnessBoost() == 1) {
                    this.mPrevTime = 0;
                    ColorAutomaticBrightnessController.this.mBrightBoostDelegate.setBrightnessBoost(2);
                }
                if (event.values[0] <= ColorAutomaticBrightnessController.MIN_LUX_LIMITI || event.values[0] >= ColorAutomaticBrightnessController.MAX_LUX_LIMITI || time - this.mPrevTime >= ((long) (ColorAutomaticBrightnessController.this.mNormalLightSensorRate / 4))) {
                    float lux = event.values[0];
                    this.mPrevTime = time;
                    ColorAutomaticBrightnessController.this.mAIBrightnessModel.setLux(lux);
                    if (Math.abs(lux - ColorAutomaticBrightnessController.this.mLastObservedLux) > 100.0f) {
                        ColorAutomaticBrightnessController.this.mAnimatorRateDelegate.calculateRate(lux, ColorAutomaticBrightnessController.this.mLastObservedLux);
                        float unused = ColorAutomaticBrightnessController.this.mLastObservedLux = lux;
                    }
                    if (time - ColorAutomaticBrightnessController.this.mProximitySensorChangeTime <= OppoFeatureCache.getOrCreate(IOppoBrightness.DEFAULT, new Object[0]).getPOCKET_RIGNING_STATE_TIMEOUT() && OppoFeatureCache.getOrCreate(IOppoBrightness.DEFAULT, new Object[0]).getPhoneState() == OppoFeatureCache.getOrCreate(IOppoBrightness.DEFAULT, new Object[0]).getPOCKET_RINGING_STATE()) {
                        ColorAutomaticBrightnessController.this.setPocketRingingState(true);
                        return;
                    }
                    return;
                }
                Slog.d(ColorAutomaticBrightnessController.TAG, "Skip onSensorChanaged, pre time = " + this.mPrevTime + ", now = " + time);
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    private boolean mLoggingEnabled;
    private boolean mManualBrightnessSlide = DEBUG_PRETEND_LIGHT_SENSOR_ABSENT;
    private ModelConfig mModelConfig;
    /* access modifiers changed from: private */
    public boolean mNeedUpgradeModel = DEBUG_PRETEND_LIGHT_SENSOR_ABSENT;
    /* access modifiers changed from: private */
    public int mNormalLightSensorRate;
    /* access modifiers changed from: private */
    public boolean mProximityNear = DEBUG_PRETEND_LIGHT_SENSOR_ABSENT;
    private Sensor mProximitySensor = null;
    /* access modifiers changed from: private */
    public long mProximitySensorChangeTime = 0;
    private final SensorEventListener mProximitySensorListener = new SensorEventListener() {
        /* class com.android.server.display.ColorAutomaticBrightnessController.AnonymousClass4 */
        private boolean mPrevProximityNear = ColorAutomaticBrightnessController.DEBUG_PRETEND_LIGHT_SENSOR_ABSENT;

        public void onSensorChanged(SensorEvent event) {
            long time = SystemClock.uptimeMillis();
            if (((double) event.values[0]) == 0.0d) {
                boolean unused = ColorAutomaticBrightnessController.this.mProximityNear = true;
                Slog.d(ColorAutomaticBrightnessController.TAG, "Proximity is near");
            } else {
                if (ColorAutomaticBrightnessController.this.mProximityNear) {
                    Slog.d(ColorAutomaticBrightnessController.TAG, "Proximity is far");
                    ColorAutomaticBrightnessController.this.mHandler.removeMessages(2);
                    ColorAutomaticBrightnessController.this.mHandler.sendEmptyMessageDelayed(2, ColorAutomaticBrightnessController.this.mDarkeningLightDebounceConfig);
                }
                boolean unused2 = ColorAutomaticBrightnessController.this.mProximityNear = ColorAutomaticBrightnessController.DEBUG_PRETEND_LIGHT_SENSOR_ABSENT;
            }
            if (this.mPrevProximityNear && !ColorAutomaticBrightnessController.this.mProximityNear) {
                long unused3 = ColorAutomaticBrightnessController.this.mProximitySensorChangeTime = time;
            }
            this.mPrevProximityNear = ColorAutomaticBrightnessController.this.mProximityNear;
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    private int mScreenAutoBrightness = -1;
    private float mScreenAutoBrightnessAdjustment = MIN_LUX_LIMITI;
    private int mScreenBrightnessRangeMaximum;
    private int mScreenBrightnessRangeMinimum;
    /* access modifiers changed from: private */
    public SensorManager mSensorManager;

    public ColorAutomaticBrightnessController(Context context) {
        this.mContext = context;
    }

    public void init(IColorAutomaticBrightnessController.Callbacks callbacks, Looper looper, SensorManager sensorManager, Sensor lightSensor, ColorBaseBrightnessMappingStrategy mapper, int brightnessMin, int brightnessMax, float dozeScaleFactor, int lightSensorRate, ModelConfig modelConfig, long darkeningLightDebounce) {
        this.mCallbacks = callbacks;
        this.mSensorManager = sensorManager;
        this.mBrightnessMapper = mapper;
        this.mScreenBrightnessRangeMinimum = brightnessMin;
        this.mScreenBrightnessRangeMaximum = brightnessMax;
        this.mDozeScaleFactor = dozeScaleFactor;
        this.mNormalLightSensorRate = lightSensorRate;
        this.mModelConfig = modelConfig;
        this.mDarkeningLightDebounceConfig = darkeningLightDebounce;
        this.mHandler = new AutomaticBrightnessHandler(looper);
        this.mLightSensor = getLightSensor(lightSensor);
        this.mAIBrightnessModel = new AIBrightnessModel(this.mContext, modelConfig);
        this.mCameraDelegate = new CameraDelegate();
        this.mCameraDelegate.readLbrSwitch(this.mContext);
        this.mBrightBoostDelegate = new BrightBoostDelegate();
        this.mAnimatorRateDelegate = new AnimatorRateDelegate(this.mContext);
        this.mAnimatorRateDelegate.isSpecialSensor();
        this.mProximitySensor = this.mSensorManager.getDefaultSensor(8);
        DevicePropertyHelper.getScreenAutoBrightnessConfig();
    }

    public int getmAIBrightness() {
        return this.mAIBrightness;
    }

    public boolean getmLightSensorEnabled() {
        return this.mLightSensorEnabled;
    }

    public boolean getmProximityNear() {
        return this.mProximityNear;
    }

    public int getAutomaticScreenBrightness() {
        if (this.mDisplayPolicy == 1) {
            return (int) (((float) this.mScreenAutoBrightness) * this.mDozeScaleFactor);
        }
        return this.mScreenAutoBrightness;
    }

    public void setAutomaticScreenBrightness(int automaticScreenBrightness) {
        this.mScreenAutoBrightness = automaticScreenBrightness;
    }

    public float getAutomaticScreenBrightnessAdjustment() {
        return this.mBrightnessMapper.getAutoBrightnessAdjustment();
    }

    public void configure(boolean enable, float adjustment, boolean dozing, boolean userInitiatedChange) {
        if (setLightSensorEnabled(enable && !dozing) || setScreenAutoBrightnessAdjustment(adjustment)) {
            updateAutoBrightness(DEBUG_PRETEND_LIGHT_SENSOR_ABSENT);
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

    public void resetShortTermModel() {
        this.mBrightnessMapper.clearUserDataPoints();
    }

    public boolean setBrightnessConfiguration(BrightnessConfiguration configuration) {
        if (!this.mBrightnessMapper.setBrightnessConfiguration(configuration)) {
            return DEBUG_PRETEND_LIGHT_SENSOR_ABSENT;
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
        pw.println("  mNormalLightSensorRate=" + this.mNormalLightSensorRate);
        pw.println("  mDarkeningLightDebounceConfig=" + this.mDarkeningLightDebounceConfig);
        pw.println();
        pw.println("Automatic Brightness Controller State:");
        pw.println("  mLightSensor=" + this.mLightSensor);
        pw.println("  mLightSensorEnabled=" + this.mLightSensorEnabled);
        pw.println("  mLastObservedLux=" + this.mLastObservedLux);
        pw.println("  mScreenAutoBrightness=" + this.mScreenAutoBrightness);
        pw.println("  mDisplayPolicy=" + DisplayManagerInternal.DisplayPowerRequest.policyToString(this.mDisplayPolicy));
        this.mAIBrightnessModel.dump(pw);
        pw.println();
        this.mBrightnessMapper.dump(pw);
    }

    private boolean setLightSensorEnabled(boolean enable) {
        if (enable != this.mLightSensorEnabled) {
            Slog.d(TAG, "setLightSensorEnabled: enable=" + enable + ", mLightSensorEnabled=" + this.mLightSensorEnabled);
        }
        OppoBaseDisplayPowerController2.mScreenDimQuicklyDark = DEBUG_PRETEND_LIGHT_SENSOR_ABSENT;
        if (enable) {
            Settings.System.putFloatForUser(this.mContext.getContentResolver(), "screen_auto_brightness_adj", MIN_LUX_LIMITI, -2);
            if (!this.mLightSensorEnabled) {
                this.mLightSensorEnabled = true;
                ManualSharedConstants.sUseAutoBrightness = true;
                if (this.mBrightBoostDelegate.getBrightnessBoost() == 1) {
                    Thread thread = new Thread(new Runnable() {
                        /* class com.android.server.display.ColorAutomaticBrightnessController.AnonymousClass1 */

                        public void run() {
                            ColorAutomaticBrightnessController.this.mSensorManager.registerListener(ColorAutomaticBrightnessController.this.mLightSensorListener, ColorAutomaticBrightnessController.this.mLightSensor, ColorAutomaticBrightnessController.this.mNormalLightSensorRate * ColorFreeformManagerService.FREEFORM_CALLER_UID, ColorAutomaticBrightnessController.this.mHandler);
                        }
                    }, "LightSensorEnableThread");
                    thread.setPriority(10);
                    thread.start();
                } else {
                    this.mSensorManager.registerListener(this.mLightSensorListener, this.mLightSensor, this.mNormalLightSensorRate * ColorFreeformManagerService.FREEFORM_CALLER_UID, this.mHandler);
                    this.mBrightBoostDelegate.turnOnBoost();
                }
                setAIBrightnessCallback();
                return true;
            }
        } else if (this.mLightSensorEnabled) {
            this.mLightSensorEnabled = DEBUG_PRETEND_LIGHT_SENSOR_ABSENT;
            this.mSensorManager.unregisterListener(this.mLightSensorListener);
            ManualSharedConstants.sUseAutoBrightness = DEBUG_PRETEND_LIGHT_SENSOR_ABSENT;
            ManualSharedConstants.sManualSetAutoBrightness = DEBUG_PRETEND_LIGHT_SENSOR_ABSENT;
            this.mProximityNear = DEBUG_PRETEND_LIGHT_SENSOR_ABSENT;
            OppoBaseDisplayPowerController2.mQuickDarkToBright = DEBUG_PRETEND_LIGHT_SENSOR_ABSENT;
            this.mScreenAutoBrightness = -1;
            this.mManualBrightnessSlide = DEBUG_PRETEND_LIGHT_SENSOR_ABSENT;
            ManualSharedConstants.sManualBrightness = 0;
        }
        return DEBUG_PRETEND_LIGHT_SENSOR_ABSENT;
    }

    /* access modifiers changed from: package-private */
    public void updateAutoBrightness(boolean sendUpdate) {
        if (this.mManualBrightnessSlide) {
            this.mManualBrightnessSlide = DEBUG_PRETEND_LIGHT_SENSOR_ABSENT;
            if (ManualSharedConstants.sManualBrightness != 0) {
                ManualSharedConstants.sManualSetAutoBrightness = true;
                this.mScreenAutoBrightness = ManualSharedConstants.sManualBrightness;
                this.mAIBrightness = ManualSharedConstants.sManualBrightness;
                Slog.i(TAG, "updateAutoBrightness: Manual\n, sendUpdate = " + sendUpdate + ", AIBrightness = " + this.mAIBrightness + ", adjustment = " + this.mScreenAutoBrightnessAdjustment + ", screenAutoBrightness = " + this.mScreenAutoBrightness);
                this.mCallbacks.updateBrightness();
                this.mAIBrightnessModel.setBrightnessByUser((float) this.mScreenAutoBrightness);
                return;
            }
            return;
        }
        Slog.d(TAG, "enter Auto");
        if (sendUpdate) {
            ManualSharedConstants.sManualSetAutoBrightness = DEBUG_PRETEND_LIGHT_SENSOR_ABSENT;
        }
        int newScreenAutoBrightness = clampScreenBrightness(Math.round(0.5f));
        if (this.mAIBrightness != 0) {
            newScreenAutoBrightness = this.mAIBrightness;
        }
        Slog.d(TAG, "mCameraMode = " + this.mCameraDelegate.getCameraMode());
        int newScreenAutoBrightness2 = this.mCameraDelegate.adjustCameraBright(newScreenAutoBrightness);
        if (this.mScreenAutoBrightness != newScreenAutoBrightness2 || this.mCameraDelegate.getCameraMode() == 1 || this.mCameraDelegate.getCameraUseAdjustmentSetting()) {
            Slog.i(TAG, "updateAutoBrightness: Auto\n, sendUpdate = " + sendUpdate + ", adjustment = " + this.mScreenAutoBrightnessAdjustment + ", AIBrightness = " + this.mAIBrightness + ", adjustManualBright = " + newScreenAutoBrightness2);
            this.mAnimatorRateDelegate.calculateRateForIndex(this.mScreenAutoBrightness, newScreenAutoBrightness2);
            this.mScreenAutoBrightness = newScreenAutoBrightness2;
            if (sendUpdate) {
                this.mCallbacks.updateBrightness();
            }
            if (sendUpdate) {
                OppoBaseDisplayPowerController2.mReduceBrightnessAnimating = DEBUG_PRETEND_LIGHT_SENSOR_ABSENT;
            }
        }
    }

    private int clampScreenBrightness(int value) {
        return MathUtils.constrain(value, this.mScreenBrightnessRangeMinimum, this.mScreenBrightnessRangeMaximum);
    }

    /* access modifiers changed from: private */
    public final class AutomaticBrightnessHandler extends Handler {
        AutomaticBrightnessHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 1) {
                ColorAutomaticBrightnessController.this.onTargetBrightnessChanged(((Float) msg.obj).floatValue());
            } else if (i == 2) {
                ColorAutomaticBrightnessController.this.mCallbacks.updateBrightness();
            } else if (i == 3) {
                boolean unused = ColorAutomaticBrightnessController.this.mNeedUpgradeModel = true;
                Slog.d(ColorAutomaticBrightnessController.TAG, "MSG_ON_RUS_CHANGE");
            } else if (i == 4) {
                Slog.d(ColorAutomaticBrightnessController.TAG, "MSG_ON_SCREEN_OFF");
                if (ColorAutomaticBrightnessController.this.mNeedUpgradeModel) {
                    ColorAutomaticBrightnessController.this.upgradeAIBrightnessModel();
                }
            } else if (i == 100) {
                boolean unused2 = ColorAutomaticBrightnessController.this.mNeedUpgradeModel = true;
                Slog.d(ColorAutomaticBrightnessController.TAG, "MSG_ON_UPGRADE_MODEL");
            } else if (i == 101) {
                ColorAutomaticBrightnessController.this.mAIBrightnessModel.printAutoLuxInterval();
                Slog.d(ColorAutomaticBrightnessController.TAG, "MSG_ON_PRINT_INTERVAL");
            }
        }
    }

    /* access modifiers changed from: private */
    public void upgradeAIBrightnessModel() {
        Slog.d(TAG, "upgradeAIBrightnessModel");
        AIBrightnessModel aIBrightnessModel = this.mAIBrightnessModel;
        if (aIBrightnessModel != null) {
            aIBrightnessModel.release();
        }
        this.mAIBrightnessModel = new AIBrightnessModel(this.mContext, this.mModelConfig);
        this.mNeedUpgradeModel = DEBUG_PRETEND_LIGHT_SENSOR_ABSENT;
    }

    /* access modifiers changed from: private */
    public void onTargetBrightnessChanged(float brightness) {
        this.mAIBrightness = (int) brightness;
        if (!this.mManualBrightnessSlide) {
            updateAutoBrightness(true);
        }
        Slog.d(TAG, "onTargetBrightnessChanged mAIBrightness=" + ((int) brightness));
    }

    private Sensor getLightSensor(Sensor lightSensor) {
        boolean isUsingFusionLight = SystemProperties.getBoolean("persist.sys.oppo.fusionlight", (boolean) DEBUG_PRETEND_LIGHT_SENSOR_ABSENT);
        boolean isUsingFusionLightNaruto = SystemProperties.getBoolean("persist.sys.oppo.fusionlight.naruto", (boolean) DEBUG_PRETEND_LIGHT_SENSOR_ABSENT);
        if (!isUsingFusionLight || !isUsingFusionLightNaruto) {
            return lightSensor;
        }
        return this.mSensorManager.getDefaultSensor(33171099);
    }

    private boolean setScreenAutoBrightnessAdjustment(float adjustment) {
        if (adjustment == this.mScreenAutoBrightnessAdjustment) {
            return DEBUG_PRETEND_LIGHT_SENSOR_ABSENT;
        }
        this.mScreenAutoBrightnessAdjustment = adjustment;
        if (this.mLightSensorEnabled && !this.mCameraDelegate.isSpecialAdj(adjustment)) {
            this.mManualBrightnessSlide = true;
            ManualSharedConstants.sManualBrightness = Math.round(adjustment);
        }
        return true;
    }

    public boolean setLoggingEnabled(boolean loggingEnabled) {
        if (this.mLoggingEnabled == loggingEnabled) {
            return DEBUG_PRETEND_LIGHT_SENSOR_ABSENT;
        }
        this.mBrightnessMapper.setLoggingEnabled(loggingEnabled);
        this.mLoggingEnabled = loggingEnabled;
        ColorAILog.sIsLogOn = loggingEnabled;
        if (!Build.IS_USERDEBUG) {
            return true;
        }
        if (loggingEnabled) {
            this.mHandler.post(new Runnable() {
                /* class com.android.server.display.$$Lambda$ColorAutomaticBrightnessController$urPKeBu8ODmrSQadQIrdBcPU0 */

                public final void run() {
                    ColorAutomaticBrightnessController.this.lambda$setLoggingEnabled$0$ColorAutomaticBrightnessController();
                }
            });
            return true;
        }
        this.mHandler.post(new Runnable() {
            /* class com.android.server.display.$$Lambda$ColorAutomaticBrightnessController$RFWShn5peMfpRlfKQmSgkfFDptU */

            public final void run() {
                ColorAutomaticBrightnessController.this.lambda$setLoggingEnabled$1$ColorAutomaticBrightnessController();
            }
        });
        return true;
    }

    public /* synthetic */ void lambda$setLoggingEnabled$0$ColorAutomaticBrightnessController() {
        if (this.mAIBrightnessModelUpgradeReceiver == null) {
            this.mAIBrightnessModelUpgradeReceiver = new AIBrightnessModelUpgradeReceiver();
        }
        this.mAIBrightnessModelUpgradeReceiver.register(this.mContext);
    }

    public /* synthetic */ void lambda$setLoggingEnabled$1$ColorAutomaticBrightnessController() {
        AIBrightnessModelUpgradeReceiver aIBrightnessModelUpgradeReceiver = this.mAIBrightnessModelUpgradeReceiver;
        if (aIBrightnessModelUpgradeReceiver != null) {
            aIBrightnessModelUpgradeReceiver.unregister(this.mContext);
        }
    }

    private void setAIBrightnessCallback() {
        this.mAIBrightnessModel.setCallbackHandler(this.mHandler);
    }

    public int getAutoRate() {
        return this.mAnimatorRateDelegate.getAutoRate();
    }

    public float getNextChange(int targetY, float currY, float timeDelta) {
        return this.mAIBrightnessModel.getNextChange(targetY, currY, timeDelta);
    }

    /* access modifiers changed from: package-private */
    public void updateBrightnessAsync() {
        this.mHandler.post(new Runnable() {
            /* class com.android.server.display.ColorAutomaticBrightnessController.AnonymousClass3 */

            public void run() {
                ColorAutomaticBrightnessController.this.updateAutoBrightness(true);
            }
        });
    }

    public void setStateChanged(int msgId, Bundle extraData) {
        this.mAIBrightnessModel.setStateChanged(msgId, extraData);
    }

    public void printAutoLuxInterval() {
        this.mAIBrightnessModel.printAutoLuxInterval();
    }

    public void setLux(float lux) {
        this.mAIBrightnessModel.setLux(lux);
    }

    public boolean isPocketRingingState() {
        return this.mBrightBoostDelegate.isPocketRingingState();
    }

    public void setPocketRingingState(boolean state) {
        this.mBrightBoostDelegate.setPocketRingingState(state);
    }

    public int getCameraMode() {
        return this.mCameraDelegate.getCameraMode();
    }

    public void setCameraMode(int mode) {
        this.mCameraDelegate.setCameraMode(mode);
    }

    public void setCameraUseAdjustmentSetting(boolean enable) {
        this.mCameraDelegate.setCameraUseAdjustmentSetting(enable);
    }

    public void setCameraBacklight(boolean enable) {
        this.mCameraDelegate.setCameraBacklight(enable);
    }

    public void setGalleryBacklight(boolean enable) {
        this.mCameraDelegate.setGalleryBacklight(enable);
    }

    public int getBrightnessBoost() {
        return this.mBrightBoostDelegate.getBrightnessBoost();
    }

    public void setBrightnessBoost(int mode) {
        this.mBrightBoostDelegate.setBrightnessBoost(mode);
    }

    public class AIBrightnessModelUpgradeReceiver extends BroadcastReceiver {
        private static final String AUTO_INTERVAL_ACTION = "oppo.intent.action.AI_BBRIGHT_AUTO_INTERVAL_PRINT";
        private static final String TAG = "AIBrightnessModelUpgradeReceiver";
        private static final String UPGRADE_ACTION = "oppo.intent.action.AI_BBRIGHT_MODEL_UPGRADE";
        private boolean mHasRegistered;
        private IntentFilter mIntentFilter;

        private AIBrightnessModelUpgradeReceiver() {
            this.mHasRegistered = ColorAutomaticBrightnessController.DEBUG_PRETEND_LIGHT_SENSOR_ABSENT;
            this.mIntentFilter = new IntentFilter(UPGRADE_ACTION);
            this.mIntentFilter = new IntentFilter(AUTO_INTERVAL_ACTION);
        }

        /* access modifiers changed from: private */
        public void register(Context context) {
            if (!this.mHasRegistered) {
                try {
                    context.getApplicationContext().registerReceiver(this, this.mIntentFilter, "oppo.permission.OPPO_COMPONENT_SAFE", null);
                    this.mHasRegistered = true;
                } catch (Exception e) {
                    Slog.e(TAG, "register exception " + e.getMessage());
                }
            }
        }

        /* access modifiers changed from: private */
        public void unregister(Context context) {
            try {
                context.getApplicationContext().unregisterReceiver(this);
                this.mHasRegistered = ColorAutomaticBrightnessController.DEBUG_PRETEND_LIGHT_SENSOR_ABSENT;
            } catch (Exception e) {
                Slog.e(TAG, "unregister exception " + e.getMessage());
            }
        }

        public void onReceive(Context context, Intent intent) {
            if (intent != null && UPGRADE_ACTION.equals(intent.getAction())) {
                Slog.d(TAG, "onReceive : oppo.intent.action.AI_BBRIGHT_MODEL_UPGRADE");
                Handler handler = ColorAutomaticBrightnessController.this.mHandler;
                if (handler != null) {
                    handler.sendEmptyMessage(100);
                }
            } else if (intent != null && AUTO_INTERVAL_ACTION.equals(intent.getAction())) {
                Slog.d(TAG, "onReceive : oppo.intent.action.AI_BBRIGHT_AUTO_INTERVAL_PRINT");
                Handler handler2 = ColorAutomaticBrightnessController.this.mHandler;
                if (handler2 != null) {
                    handler2.sendEmptyMessage(101);
                }
            }
        }
    }
}
