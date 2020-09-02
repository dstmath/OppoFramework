package com.android.server.display;

import android.common.OppoFeatureCache;
import android.content.Context;
import android.util.Log;
import android.util.Slog;
import com.color.app.ColorAppEnterInfo;
import com.color.app.ColorAppExitInfo;
import com.color.app.ColorAppSwitchConfig;
import com.color.app.ColorAppSwitchManager;
import com.color.util.ColorTypeCastingHelper;

public class PswFeatureReduceBrightness implements IPswFeatureReduceBrightness {
    private static final String TAG = "PswFeatureReduceBrightness";
    public static int mBrightness;
    private static Context mContext = null;
    private static DisplayPowerController mDpc = null;
    /* access modifiers changed from: private */
    public static OppoBaseDisplayPowerController2 mDpcBase;
    public static int mRate;
    private static PswFeatureReduceBrightness sInstance = null;
    private ColorAppSwitchManager.OnAppSwitchObserver mDynamicObserver = new ColorAppSwitchManager.OnAppSwitchObserver() {
        /* class com.android.server.display.PswFeatureReduceBrightness.AnonymousClass1 */

        public void onAppEnter(ColorAppEnterInfo info) {
            Slog.d(PswFeatureReduceBrightness.TAG, "OnAppSwitchObserver: onAppEnter , info = " + info);
            OppoBaseDisplayPowerController2 unused = PswFeatureReduceBrightness.mDpcBase;
            OppoBaseDisplayPowerController2.mReduceBrightnessMode = 1;
            OppoBaseDisplayPowerController2 unused2 = PswFeatureReduceBrightness.mDpcBase;
            OppoBaseDisplayPowerController2.mShouldAdjustRate = 2;
            OppoBaseDisplayPowerController2 unused3 = PswFeatureReduceBrightness.mDpcBase;
            OppoBaseDisplayPowerController2.mReduceBrightnessAnimating = true;
            PswFeatureReduceBrightness.this.mInner.sendUpdatePowerStateInner();
        }

        public void onAppExit(ColorAppExitInfo info) {
            Slog.d(PswFeatureReduceBrightness.TAG, "OnAppSwitchObserver: onAppExit , info = " + info);
            OppoBaseDisplayPowerController2 unused = PswFeatureReduceBrightness.mDpcBase;
            OppoBaseDisplayPowerController2.mReduceBrightnessMode = 0;
            OppoBaseDisplayPowerController2 unused2 = PswFeatureReduceBrightness.mDpcBase;
            OppoBaseDisplayPowerController2.mShouldAdjustRate = 3;
            OppoBaseDisplayPowerController2 unused3 = PswFeatureReduceBrightness.mDpcBase;
            OppoBaseDisplayPowerController2.mReduceBrightnessAnimating = true;
            PswFeatureReduceBrightness.this.mInner.sendUpdatePowerStateInner();
        }

        public void onActivityEnter(ColorAppEnterInfo info) {
        }

        public void onActivityExit(ColorAppExitInfo info) {
        }
    };
    /* access modifiers changed from: private */
    public IPswDisplayPowerControllerReduceBrightnessInner mInner;
    public boolean mRegistAppSwitch = true;

    public static PswFeatureReduceBrightness getInstance(Object... vars) {
        Log.d(TAG, "DisplayPowerController getInstance");
        mContext = (Context) vars[0];
        mDpc = (DisplayPowerController) vars[1];
        mDpcBase = (OppoBaseDisplayPowerController2) ColorTypeCastingHelper.typeCasting(OppoBaseDisplayPowerController2.class, mDpc);
        if (sInstance == null) {
            sInstance = new PswFeatureReduceBrightness();
        }
        return sInstance;
    }

    public static PswFeatureReduceBrightness getMethod() {
        if (sInstance == null) {
            sInstance = new PswFeatureReduceBrightness();
        }
        return sInstance;
    }

    public PswFeatureReduceBrightness() {
        Log.i(TAG, "create");
    }

    public void registerByNewImpl() {
        Slog.d(TAG, "registerByNewImpl");
        this.mInner.registerByNewImpl();
        ColorAppSwitchConfig config = new ColorAppSwitchConfig();
        config.addAppConfig(2, this.mInner.GetReduceBrightnessPackage());
        ColorAppSwitchManager.getInstance().registerAppSwitchObserver(mContext, this.mDynamicObserver, config);
        this.mRegistAppSwitch = true;
    }

    public void unregisterByNewImpl() {
        Slog.d(TAG, "unregisterByNewImpl");
        ColorAppSwitchManager.getInstance().unregisterAppSwitchObserver(mContext, this.mDynamicObserver);
        this.mRegistAppSwitch = false;
    }

    public void reducebrightness(int brightness, int rate) {
        OppoBaseDisplayPowerController2 oppoBaseDisplayPowerController2 = mDpcBase;
        float temp1 = 150.0f / OppoBaseDisplayPowerController2.mReduceBrightnessRate;
        OppoBaseDisplayPowerController2 oppoBaseDisplayPowerController22 = mDpcBase;
        float temp2 = OppoBaseDisplayPowerController2.mReduceBrightnessRate * 150.0f;
        OppoBaseDisplayPowerController2 oppoBaseDisplayPowerController23 = mDpcBase;
        if (OppoBaseDisplayPowerController2.mReduceBrightnessMode == 1) {
            OppoBaseDisplayPowerController2 oppoBaseDisplayPowerController24 = mDpcBase;
            if (OppoBaseDisplayPowerController2.mReduceBrightnessRate != 1.0f && ((float) brightness) >= temp2 && brightness <= OppoFeatureCache.getOrCreate(IOppoBrightness.DEFAULT, new Object[0]).getMaximumScreenBrightnessSetting() && brightness > 2) {
                if (((float) brightness) > temp1 || ((float) brightness) < temp2) {
                    OppoBaseDisplayPowerController2 oppoBaseDisplayPowerController25 = mDpcBase;
                    brightness = (int) (((float) brightness) * OppoBaseDisplayPowerController2.mReduceBrightnessRate);
                    mDpcBase.mBrightnessSource = 15;
                } else {
                    brightness = (int) ((((150.0f - temp2) * ((float) brightness)) / (temp1 - temp2)) + (((temp1 - 150.0f) * temp2) / (temp1 - temp2)));
                    mDpcBase.mBrightnessSource = 14;
                }
            }
        }
        OppoBaseDisplayPowerController2 oppoBaseDisplayPowerController26 = mDpcBase;
        if (OppoBaseDisplayPowerController2.mShouldAdjustRate == 2) {
            OppoBaseDisplayPowerController2 oppoBaseDisplayPowerController27 = mDpcBase;
            OppoBaseDisplayPowerController2.mShouldAdjustRate = 0;
            OppoBaseDisplayPowerController2 oppoBaseDisplayPowerController28 = mDpcBase;
            rate = OppoBaseDisplayPowerController2.APP_REDUCE_BRIGHTNESS_RATE_IN;
        }
        OppoBaseDisplayPowerController2 oppoBaseDisplayPowerController29 = mDpcBase;
        if (OppoBaseDisplayPowerController2.mShouldAdjustRate == 3) {
            OppoBaseDisplayPowerController2 oppoBaseDisplayPowerController210 = mDpcBase;
            OppoBaseDisplayPowerController2.mShouldAdjustRate = 0;
            OppoBaseDisplayPowerController2 oppoBaseDisplayPowerController211 = mDpcBase;
            rate = OppoBaseDisplayPowerController2.APP_REDUCE_BRIGHTNESS_RATE_OUT;
        }
        if (mDpcBase.mScreenAnimBightnessTarget == brightness) {
            rate = mDpcBase.mScreenAnimBightnessRate;
        }
        mBrightness = brightness;
        mRate = rate;
    }

    public int getbrightness() {
        return mBrightness;
    }

    public int getrate() {
        return mRate;
    }

    public boolean getmRegistAppSwitch() {
        return this.mRegistAppSwitch;
    }

    public void init(IPswDisplayPowerControllerReduceBrightnessInner inner) {
        this.mInner = inner;
    }

    public void init() {
        this.mInner.init();
    }

    public int getReduceBrightnessMode() {
        OppoBaseDisplayPowerController2 oppoBaseDisplayPowerController2 = mDpcBase;
        return OppoBaseDisplayPowerController2.mReduceBrightnessMode;
    }

    public float getReduceBrightnessRate() {
        OppoBaseDisplayPowerController2 oppoBaseDisplayPowerController2 = mDpcBase;
        return OppoBaseDisplayPowerController2.mReduceBrightnessRate;
    }

    public boolean isReduceBrightnessAnimating() {
        OppoBaseDisplayPowerController2 oppoBaseDisplayPowerController2 = mDpcBase;
        return OppoBaseDisplayPowerController2.mReduceBrightnessAnimating;
    }
}
