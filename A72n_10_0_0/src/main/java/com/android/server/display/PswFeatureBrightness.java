package com.android.server.display;

import android.common.OppoFeatureCache;
import android.content.Context;
import android.hardware.display.DisplayManagerInternal;
import android.os.OppoBasePowerManager;
import android.provider.Settings;
import android.util.Log;
import android.util.Slog;
import com.color.util.ColorTypeCastingHelper;

public class PswFeatureBrightness implements IPswFeatureBrightness {
    static boolean DEBUG = true;
    static boolean DEBUG_PANIC = true;
    private static final String TAG = "PswFeatureBrightness";
    private static Context mContext = null;
    private static DisplayPowerController mDpc = null;
    private static OppoBaseDisplayPowerController2 mDpcBase;
    private static PswFeatureBrightness sInstance = null;
    private int mCameraMode = -1;
    private IPswDisplayPowerControllerAutoBrightnessInner mInner;
    private int mbrightness;
    private int mrate;

    public static PswFeatureBrightness getInstance(Object... vars) {
        Log.d(TAG, "DisplayPowerController getInstance");
        mContext = (Context) vars[0];
        mDpc = (DisplayPowerController) vars[1];
        mDpcBase = (OppoBaseDisplayPowerController2) ColorTypeCastingHelper.typeCasting(OppoBaseDisplayPowerController2.class, mDpc);
        if (sInstance == null) {
            sInstance = new PswFeatureBrightness();
        }
        return sInstance;
    }

    public PswFeatureBrightness() {
        Log.i(TAG, "create");
    }

    public void init(IPswDisplayPowerControllerAutoBrightnessInner inner) {
        this.mInner = inner;
    }

    public void systemReady() {
        mDpcBase.mScreenState = -1;
        OppoBaseDisplayPowerController2.mQuickDarkToBright = false;
        this.mInner.setmScreenBrightnessRangeMinimum(OppoFeatureCache.getOrCreate(IOppoBrightness.DEFAULT, new Object[0]).getMinimumScreenBrightnessSetting());
        if (!OppoFeatureCache.getOrCreate(IOppoBrightness.DEFAULT, new Object[0]).getmScreenGlobalHBMSupport()) {
            this.mInner.setmScreenBrightnessRangeMaximum(OppoBasePowerManager.BRIGHTNESS_MULTIBITS_ON);
        } else if (OppoFeatureCache.getOrCreate(IOppoBrightness.DEFAULT, new Object[0]).getHBM_SW_EXT_BTN_MAX_4095_5119_Support()) {
            this.mInner.setmScreenBrightnessRangeMaximum(OppoFeatureCache.getOrCreate(IOppoBrightness.DEFAULT, new Object[0]).getHBM_SW_EXT_BTN_MAX_4095_5119());
        } else {
            this.mInner.setmScreenBrightnessRangeMaximum(OppoFeatureCache.getOrCreate(IOppoBrightness.DEFAULT, new Object[0]).getHBM_EXTEND_MAXBRIGHTNESS());
        }
        this.mInner.setmScreenBrightnessDefault(OppoFeatureCache.getOrCreate(IOppoBrightness.DEFAULT, new Object[0]).getDefaultScreenBrightnessSetting());
        if (mContext.getPackageManager().hasSystemFeature("oppo.hardware.fingerprint.optical.support")) {
            OppoBaseDisplayPowerController2 oppoBaseDisplayPowerController2 = mDpcBase;
            OppoBaseDisplayPowerController2.mFingerprintOpticalSupport = true;
            StringBuilder sb = new StringBuilder();
            sb.append("mFingerprintOpticalSupport = ");
            OppoBaseDisplayPowerController2 oppoBaseDisplayPowerController22 = mDpcBase;
            sb.append(OppoBaseDisplayPowerController2.mFingerprintOpticalSupport);
            Slog.d(TAG, sb.toString());
        }
    }

    public void setQuickDarkToBrightStatus(DisplayManagerInternal.DisplayPowerRequest powerrequest) {
        if (powerrequest.policy == 2 && mDpcBase.mScreenState == 3) {
            OppoBaseDisplayPowerController2 oppoBaseDisplayPowerController2 = mDpcBase;
            OppoBaseDisplayPowerController2.mQuickDarkToBright = true;
        }
        mDpcBase.mScreenState = powerrequest.policy;
    }

    public void caculateBrightness(boolean slowChange, boolean autoBrightnessEnabled, int brightness, IColorAutomaticBrightnessController mAutomaticBrightnessController) {
        int rate = OppoFeatureCache.getOrCreate(IOppoBrightness.DEFAULT, new Object[0]).getBRIGHTNESS_RAMP_RATE_SLOW();
        if (autoBrightnessEnabled && mAutomaticBrightnessController != null) {
            rate = mAutomaticBrightnessController.getAutoRate();
            if (DEBUG) {
                Slog.i(TAG, "Auto brightness ---001--- rate = " + rate);
            }
        } else if (mAutomaticBrightnessController != null) {
            mAutomaticBrightnessController.setAutomaticScreenBrightness(brightness);
        } else {
            Slog.d(TAG, "mAutomaticBrightnessController is null");
        }
        if (mAutomaticBrightnessController != null) {
            if (!slowChange || mAutomaticBrightnessController.getBrightnessBoost() == 2 || OppoFeatureCache.getOrCreate(IOppoBrightness.DEFAULT, new Object[0]).getmShouldFastRate() || mAutomaticBrightnessController.getCameraMode() != this.mCameraMode) {
                this.mCameraMode = mAutomaticBrightnessController.getCameraMode();
                rate = OppoFeatureCache.getOrCreate(IOppoBrightness.DEFAULT, new Object[0]).getBRIGHTNESS_RAMP_RATE_FAST();
                if (DEBUG) {
                    Slog.i(TAG, "Auto brightness ---002--- rate = " + rate);
                }
            }
            if (mAutomaticBrightnessController.getBrightnessBoost() == 3) {
                rate = OppoFeatureCache.getOrCreate(IOppoBrightness.DEFAULT, new Object[0]).getBRIGHTNESS_RAMP_RATE_FAST();
                if (DEBUG) {
                    Slog.i(TAG, "Auto brightness ---003--- rate = " + rate);
                }
                this.mInner.BRIGHTNESS_BOOST_Delayed();
            }
        }
        OppoBaseDisplayPowerController2 oppoBaseDisplayPowerController2 = mDpcBase;
        if (OppoBaseDisplayPowerController2.mQuickDarkToBright) {
            OppoBaseDisplayPowerController2 oppoBaseDisplayPowerController22 = mDpcBase;
            OppoBaseDisplayPowerController2.mQuickDarkToBright = false;
            rate = OppoFeatureCache.getOrCreate(IOppoBrightness.DEFAULT, new Object[0]).getBRIGHTNESS_RAMP_RATE_SCREENON();
            if (DEBUG) {
                Slog.i(TAG, "Auto brightness ---004--- rate = " + rate);
            }
        }
        if (mAutomaticBrightnessController != null && mAutomaticBrightnessController.isPocketRingingState()) {
            mAutomaticBrightnessController.setPocketRingingState(false);
            rate = OppoFeatureCache.getOrCreate(IOppoBrightness.DEFAULT, new Object[0]).getBRIGHTNESS_RAMP_RATE_FAST();
            if (DEBUG) {
                Slog.i(TAG, "Auto brightness ---005--- rate = " + rate);
            }
        }
        if (OppoFeatureCache.getOrCreate(IOppoBrightness.DEFAULT, new Object[0]).getmInverseMode() == OppoFeatureCache.getOrCreate(IOppoBrightness.DEFAULT, new Object[0]).getINVERSE_ON()) {
            brightness = OppoFeatureCache.getOrCreate(IOppoBrightness.DEFAULT, new Object[0]).adjustInverseModeBrightness(brightness);
            mDpcBase.mBrightnessSource = 13;
        }
        OppoFeatureCache.getOrCreate(IOppoBrightness.DEFAULT, new Object[0]).setmShouldFastRate(false);
        this.mbrightness = brightness;
        this.mrate = rate;
    }

    public float caculateautoBrightnessAdjustment(float autoBrightnessAdjustment) {
        if (autoBrightnessAdjustment <= 0.0f || OppoFeatureCache.getOrCreate(IOppoBrightness.DEFAULT, new Object[0]).isSpecialAdj(autoBrightnessAdjustment)) {
            return autoBrightnessAdjustment;
        }
        return Math.max(Math.min(autoBrightnessAdjustment, (float) this.mInner.getmScreenBrightnessRangeMaximum()), (float) this.mInner.getmScreenBrightnessRangeMinimum());
    }

    public int putBrightnessTodatabase(int target) {
        int tenBitsMaxBt;
        if (OppoFeatureCache.getOrCreate(IOppoBrightness.DEFAULT, new Object[0]).getHBM_SW_EXT_BTN_MAX_4095_5119_Support()) {
            tenBitsMaxBt = OppoFeatureCache.getOrCreate(IOppoBrightness.DEFAULT, new Object[0]).get_SW_EXT_BTN_MAX_4095();
        } else {
            tenBitsMaxBt = OppoFeatureCache.getOrCreate(IOppoBrightness.DEFAULT, new Object[0]).getTEN_BITS_MAXBRIGHTNESS();
        }
        if (OppoFeatureCache.getOrCreate(IOppoBrightness.DEFAULT, new Object[0]).getmScreenGlobalHBMSupport() && OppoFeatureCache.getOrCreate(IOppoBrightness.DEFAULT, new Object[0]).getsGlobalHbmSellMode() == 1) {
            target = target > tenBitsMaxBt ? tenBitsMaxBt : target;
            Slog.e(TAG, "animateScreenBrightness tartet=" + target + " mode=" + OppoFeatureCache.getOrCreate(IOppoBrightness.DEFAULT, new Object[0]).getsGlobalHbmSellMode());
        }
        if (OppoFeatureCache.getOrCreate(IOppoBrightness.DEFAULT, new Object[0]).getmSetBrihgtnessSlide()) {
            if ((OppoFeatureCache.getOrCreate(IOppoBrightness.DEFAULT, new Object[0]).getmScreenGlobalHBMSupport() && target <= tenBitsMaxBt) || !OppoFeatureCache.getOrCreate(IOppoBrightness.DEFAULT, new Object[0]).getmScreenGlobalHBMSupport()) {
                this.mInner.putIntForBrightness(target);
            }
            OppoFeatureCache.getOrCreate(IOppoBrightness.DEFAULT, new Object[0]).setmSetBrihgtnessSlide(false);
        }
        return target;
    }

    public int getGlobalHbmSellMode() {
        return Settings.Secure.getIntForUser(mContext.getContentResolver(), OppoFeatureCache.getOrCreate(IOppoBrightness.DEFAULT, new Object[0]).getGLOBAL_HBM_SELL_MODE(), 0, -2);
    }

    public int applydimmingbrightness(int brightness, int SCREEN_DIM_MINIMUM_REDUCTION) {
        int brightness2;
        if (brightness - SCREEN_DIM_MINIMUM_REDUCTION > 0) {
            brightness2 = brightness - SCREEN_DIM_MINIMUM_REDUCTION;
        } else {
            brightness2 = brightness / 2;
        }
        return Math.max(Math.min(brightness2, this.mInner.getmScreenBrightnessDimConfig()), this.mInner.getmScreenBrightnessRangeMinimum());
    }

    public boolean notifyBrightnessSetting(IColorAutomaticBrightnessController mAutomaticBrightnessController, int brightness, boolean mAppliedTemporaryBrightness, boolean mAppliedTemporaryAutoBrightnessAdjustment, int mCurrentScreenBrightnessSetting, boolean slowChange) {
        if (mAutomaticBrightnessController != null && mAutomaticBrightnessController.getBrightnessBoost() == 2) {
            slowChange = false;
        }
        boolean brightnessIsTmp = mAppliedTemporaryBrightness || mAppliedTemporaryAutoBrightnessAdjustment;
        if (brightness == mCurrentScreenBrightnessSetting || brightnessIsTmp || mCurrentScreenBrightnessSetting <= 0) {
            mDpcBase.mCtsBrightness = -1;
        } else {
            mDpcBase.mCtsBrightness = mCurrentScreenBrightnessSetting;
        }
        OppoFeatureCache.getOrCreate(IOppoBrightness.DEFAULT, new Object[0]).setsHbmAutoBrightness(true);
        return slowChange;
    }

    public int getrate() {
        return this.mrate;
    }

    public int getbrightness() {
        return this.mbrightness;
    }
}
