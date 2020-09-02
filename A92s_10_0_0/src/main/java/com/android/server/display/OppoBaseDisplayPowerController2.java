package com.android.server.display;

import android.common.OppoFeatureCache;
import android.hardware.Sensor;
import android.os.OppoBasePowerManager;
import android.util.MathUtils;
import java.util.Timer;
import java.util.TimerTask;

abstract class OppoBaseDisplayPowerController2 {
    protected static final int ALWAYSON_SENSOR_RATE_US = 500000;
    public static int APP_REDUCE_BRIGHTNESS_RATE_IN = 10;
    public static int APP_REDUCE_BRIGHTNESS_RATE_OUT = 10;
    public static final long BRIGHTNESS_BOOST_SWITCHON_TIMEOUT = 600;
    protected static final long LCD_HIGH_BRIGHTNESS_STATE_DELAY = 2000;
    protected static final long MANUAL_DATA_REPORT_EVENT_ID = 608;
    protected static final long MANUAL_DATA_REPORT_PERID = 1000;
    public static final int MSG_INIT_RUS_OBJECT = 2020;
    public static final int MSG_SCREEN_ON_BRIGHTNESS_BOOST = 8;
    protected static final int MSG_UPDATE_HIGH_BRIGHTNESS_STATE = 1;
    protected static final long OLED_HIGH_BRIGHTNESS_STATE_DELAY = 5000;
    protected static final long SWITCH_BRIGHTNESS_EVENT_ID = 605;
    protected static final long SWITCH_BRIGHTNESS_REPORT_PERID = 7200000;
    public static boolean mFingerprintOpticalSupport = false;
    protected static float mLux = OppoBrightUtils.MIN_LUX_LIMITI;
    public static boolean mQuickDarkToBright = false;
    public static boolean mReduceBrightnessAnimating = false;
    public static int mReduceBrightnessMode = 0;
    public static float mReduceBrightnessRate = 1.0f;
    public static boolean mScreenDimQuicklyDark = false;
    public static int mShouldAdjustRate = 0;
    protected static boolean mStartTimer = false;
    protected IColorAutomaticBrightnessController mAutomaticBrightnessController;
    public int mBrightnessSource = -1;
    protected int mCtsBrightness = -1;
    protected long mLastManualStaticsDataReportTime;
    protected int mLastScreenBrightness;
    protected long mLastSwitchBrightReportTime;
    protected Sensor mLightSensor;
    public long mManualBacklightTmpTime = 0;
    public long mManualBacklightTotalTime = 0;
    protected long mManualLightLuxWatchPointOneTotalTime = 0;
    protected long mManualLightLuxWatchPointThreeTotalTime = 0;
    protected long mManualLightLuxWatchPointTwoTotalTime = 0;
    protected long mManualLightSensorEnableTime = 0;
    public long mManualMaxBacklightTmpTime = 0;
    public long mManualMaxBacklightTotalTime = 0;
    public int mScreenAnimBightnessRate;
    public int mScreenAnimBightnessTarget;
    public int mScreenState;
    protected long mSwitchBrightCount;
    protected TimerTask mTask;
    protected Timer mTimer;

    OppoBaseDisplayPowerController2() {
    }

    public static float normalizeAbsoluteBrightness(int value) {
        if (OppoFeatureCache.getOrCreate(IOppoBrightness.DEFAULT, new Object[0]).getmScreenGlobalHBMSupport()) {
            return ((float) clampAbsoluteBrightness(value)) / ((float) OppoFeatureCache.getOrCreate(IOppoBrightness.DEFAULT, new Object[0]).getHBM_EXTEND_MAXBRIGHTNESS());
        }
        return ((float) clampAbsoluteBrightness(value)) / ((float) OppoBasePowerManager.BRIGHTNESS_MULTIBITS_ON);
    }

    public static int clampAbsoluteBrightness(int value) {
        if (OppoFeatureCache.getOrCreate(IOppoBrightness.DEFAULT, new Object[0]).getmScreenGlobalHBMSupport()) {
            return MathUtils.constrain(value, 0, OppoFeatureCache.getOrCreate(IOppoBrightness.DEFAULT, new Object[0]).getHBM_EXTEND_MAXBRIGHTNESS());
        }
        return MathUtils.constrain(value, 0, OppoBasePowerManager.BRIGHTNESS_MULTIBITS_ON);
    }

    public static float clampAutoBrightnessAdjustment(float value) {
        if (OppoFeatureCache.getOrCreate(IOppoBrightness.DEFAULT, new Object[0]).getmScreenGlobalHBMSupport()) {
            return MathUtils.constrain(value, (float) OppoBrightUtils.MIN_LUX_LIMITI, (float) OppoFeatureCache.getOrCreate(IOppoBrightness.DEFAULT, new Object[0]).getHBM_EXTEND_MAXBRIGHTNESS());
        }
        return MathUtils.constrain(value, (float) OppoBrightUtils.MIN_LUX_LIMITI, (float) OppoBasePowerManager.BRIGHTNESS_MULTIBITS_ON);
    }
}
