package com.android.server.display;

import android.common.OppoFeatureCache;
import android.content.Context;
import android.os.SystemProperties;
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
    private static OppoBaseDisplayPowerController2 mDpcBase;
    public static int mRate;
    private static PswFeatureReduceBrightness sInstance = null;
    private float Linearprecent;
    private int brightnessbackup;
    private int mBtExtendSupported = 0;
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
            OppoBaseDisplayPowerController2 unused4 = PswFeatureReduceBrightness.mDpcBase;
            OppoBaseDisplayPowerController2.mExitState = true;
            PswFeatureReduceBrightness.this.mInner.EXIT_STATE_Delayed();
            PswFeatureReduceBrightness.this.mInner.sendUpdatePowerStateInner();
        }

        public void onActivityEnter(ColorAppEnterInfo info) {
        }

        public void onActivityExit(ColorAppExitInfo info) {
        }
    };
    private IPswDisplayPowerControllerReduceBrightnessInner mInner;
    private int mReduBnMax = 2047;
    private int mReduBnThreshold = 150;
    public boolean mRegistAppSwitch = true;
    private boolean mShouldSlowRate = false;
    private int target;
    private int tempLimit;
    private float y = 1.0f;

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
        this.mBtExtendSupported = SystemProperties.getInt("persist.sys.extend.brightness", 0);
        if (this.mBtExtendSupported == 1) {
            this.mReduBnThreshold = 1830;
            this.mReduBnMax = OppoFeatureCache.getOrCreate(IOppoBrightness.DEFAULT, new Object[0]).getHBM_SW_EXT_BTN_MAX_4095_5119();
            OppoBaseDisplayPowerController2.APP_REDUCE_BRIGHTNESS_RATE_IN *= 4;
            OppoBaseDisplayPowerController2.APP_REDUCE_BRIGHTNESS_RATE_OUT *= 4;
            return;
        }
        this.mReduBnThreshold = 150;
        this.mReduBnMax = 2047;
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
        float temp1;
        float temp4;
        float temp3;
        float temp2;
        if (this.mBtExtendSupported == 1) {
            OppoBaseDisplayPowerController2 oppoBaseDisplayPowerController2 = mDpcBase;
            temp1 = ((float) this.mReduBnThreshold) / OppoBaseDisplayPowerController2.mReduceBrightnessRate;
            OppoBaseDisplayPowerController2 oppoBaseDisplayPowerController22 = mDpcBase;
            temp2 = ((float) this.mReduBnThreshold) * OppoBaseDisplayPowerController2.mReduceBrightnessRate;
            OppoBaseDisplayPowerController2 oppoBaseDisplayPowerController23 = mDpcBase;
            temp3 = ((float) this.mReduBnMax) / OppoBaseDisplayPowerController2.mReduceBrightnessRate;
            OppoBaseDisplayPowerController2 oppoBaseDisplayPowerController24 = mDpcBase;
            temp4 = ((float) this.mReduBnMax) * OppoBaseDisplayPowerController2.mReduceBrightnessRate;
        } else {
            OppoBaseDisplayPowerController2 oppoBaseDisplayPowerController25 = mDpcBase;
            OppoBaseDisplayPowerController2 oppoBaseDisplayPowerController26 = mDpcBase;
            temp1 = ((float) OppoBaseDisplayPowerController2.mLowBrightnessThresholdLimit) / OppoBaseDisplayPowerController2.mReduceBrightnessRate;
            OppoBaseDisplayPowerController2 oppoBaseDisplayPowerController27 = mDpcBase;
            OppoBaseDisplayPowerController2 oppoBaseDisplayPowerController28 = mDpcBase;
            temp2 = ((float) OppoBaseDisplayPowerController2.mLowBrightnessThresholdLimit) * OppoBaseDisplayPowerController2.mReduceBrightnessRate;
            OppoBaseDisplayPowerController2 oppoBaseDisplayPowerController29 = mDpcBase;
            OppoBaseDisplayPowerController2 oppoBaseDisplayPowerController210 = mDpcBase;
            temp3 = ((float) OppoBaseDisplayPowerController2.mHghBrightnessThresholdLimit) / OppoBaseDisplayPowerController2.mReduceBrightnessRate;
            OppoBaseDisplayPowerController2 oppoBaseDisplayPowerController211 = mDpcBase;
            OppoBaseDisplayPowerController2 oppoBaseDisplayPowerController212 = mDpcBase;
            temp4 = ((float) OppoBaseDisplayPowerController2.mHghBrightnessThresholdLimit) * OppoBaseDisplayPowerController2.mReduceBrightnessRate;
        }
        OppoBaseDisplayPowerController2 oppoBaseDisplayPowerController213 = mDpcBase;
        if (OppoBaseDisplayPowerController2.mReduceBrightnessMode == 1) {
            OppoBaseDisplayPowerController2 oppoBaseDisplayPowerController214 = mDpcBase;
            if (OppoBaseDisplayPowerController2.mReduceBrightnessRate != 1.0f && ((float) brightness) >= temp2 && ((float) brightness) <= temp3 && brightness > 2) {
                if (((float) brightness) <= temp1 && ((float) brightness) >= temp2) {
                    OppoBaseDisplayPowerController2 oppoBaseDisplayPowerController215 = mDpcBase;
                    float f = ((((float) OppoBaseDisplayPowerController2.mLowBrightnessThresholdLimit) - temp2) * ((float) brightness)) / (temp1 - temp2);
                    OppoBaseDisplayPowerController2 oppoBaseDisplayPowerController216 = mDpcBase;
                    brightness = (int) (f + (((temp1 - ((float) OppoBaseDisplayPowerController2.mLowBrightnessThresholdLimit)) * temp2) / (temp1 - temp2)));
                } else if (((float) brightness) > temp3 || ((float) brightness) < temp4) {
                    OppoBaseDisplayPowerController2 oppoBaseDisplayPowerController217 = mDpcBase;
                    brightness = (int) (((float) brightness) * OppoBaseDisplayPowerController2.mReduceBrightnessRate);
                } else {
                    OppoBaseDisplayPowerController2 oppoBaseDisplayPowerController218 = mDpcBase;
                    OppoBaseDisplayPowerController2 oppoBaseDisplayPowerController219 = mDpcBase;
                    brightness = (int) ((((temp3 - (OppoBaseDisplayPowerController2.mReduceBrightnessRate * temp4)) * ((float) brightness)) / (temp3 - temp4)) - (((temp3 * temp4) * (1.0f - OppoBaseDisplayPowerController2.mReduceBrightnessRate)) / (temp3 - temp4)));
                }
            }
        }
        OppoBaseDisplayPowerController2 oppoBaseDisplayPowerController220 = mDpcBase;
        if (OppoBaseDisplayPowerController2.mShouldAdjustRate == 2) {
            OppoBaseDisplayPowerController2 oppoBaseDisplayPowerController221 = mDpcBase;
            OppoBaseDisplayPowerController2.mShouldAdjustRate = 0;
            OppoBaseDisplayPowerController2 oppoBaseDisplayPowerController222 = mDpcBase;
            rate = OppoBaseDisplayPowerController2.APP_REDUCE_BRIGHTNESS_RATE_IN;
        }
        OppoBaseDisplayPowerController2 oppoBaseDisplayPowerController223 = mDpcBase;
        if (OppoBaseDisplayPowerController2.mShouldAdjustRate == 3) {
            OppoBaseDisplayPowerController2 oppoBaseDisplayPowerController224 = mDpcBase;
            OppoBaseDisplayPowerController2.mShouldAdjustRate = 0;
            OppoBaseDisplayPowerController2 oppoBaseDisplayPowerController225 = mDpcBase;
            rate = OppoBaseDisplayPowerController2.APP_REDUCE_BRIGHTNESS_RATE_OUT;
        }
        if (mDpcBase.mScreenAnimBightnessTarget == brightness) {
            rate = mDpcBase.mScreenAnimBightnessRate;
        }
        mBrightness = brightness;
        mRate = rate;
    }

    public void reducebrightness2(int brightness, int rate) {
        OppoBaseDisplayPowerController2 oppoBaseDisplayPowerController2 = mDpcBase;
        if (OppoBaseDisplayPowerController2.mReduceBrightnessMode == 1 && mDpcBase.useReduceBrightness != 0 && brightness > mDpcBase.temp5 && brightness <= mDpcBase.temp4) {
            this.brightnessbackup = brightness;
            if (brightness < mDpcBase.temp1) {
                this.Linearprecent = (((float) (brightness - mDpcBase.temp5)) / ((float) (mDpcBase.temp1 - mDpcBase.temp5))) * (mDpcBase.y3 - mDpcBase.y2);
                brightness = (int) ((this.y - this.Linearprecent) * ((float) brightness));
                mDpcBase.mBrightnessSource = 14;
            } else if (brightness < mDpcBase.temp2 && brightness >= mDpcBase.temp1) {
                brightness = (int) ((this.y - mDpcBase.y3) * ((float) brightness));
                mDpcBase.mBrightnessSource = 15;
            } else if (brightness >= mDpcBase.temp3 || brightness < mDpcBase.temp2) {
                brightness = (int) ((this.y - mDpcBase.y2) * ((float) brightness));
                mDpcBase.mBrightnessSource = 17;
            } else {
                this.Linearprecent = mDpcBase.y3 - ((((float) (brightness - mDpcBase.temp2)) / ((float) (mDpcBase.temp3 - mDpcBase.temp2))) * (mDpcBase.y3 - mDpcBase.y2));
                brightness = (int) ((this.y - this.Linearprecent) * ((float) brightness));
                mDpcBase.mBrightnessSource = 16;
            }
            if ("com.ss.android.ugc.aweme" == OppoFeatureCache.getOrCreate(IOppoBrightness.DEFAULT, new Object[0]).getTopPackageName() || "com.smile.gifmaker" == OppoFeatureCache.getOrCreate(IOppoBrightness.DEFAULT, new Object[0]).getTopPackageName()) {
                if (this.brightnessbackup < mDpcBase.temp1) {
                    this.Linearprecent = mDpcBase.y5 * (((float) this.brightnessbackup) / ((float) mDpcBase.temp1));
                    brightness = (int) ((this.y - this.Linearprecent) * ((float) this.brightnessbackup));
                    mDpcBase.mBrightnessSource = 18;
                } else if (this.brightnessbackup < mDpcBase.temp2 && this.brightnessbackup >= mDpcBase.temp1) {
                    brightness = (int) ((this.y - mDpcBase.y5) * ((float) this.brightnessbackup));
                    mDpcBase.mBrightnessSource = 19;
                } else if (this.brightnessbackup < mDpcBase.temp3 && this.brightnessbackup >= mDpcBase.temp2) {
                    this.Linearprecent = mDpcBase.y5 - ((((float) (this.brightnessbackup - mDpcBase.temp2)) / ((float) (mDpcBase.temp3 - mDpcBase.temp2))) * (mDpcBase.y5 - mDpcBase.y2));
                    brightness = (int) ((this.y - this.Linearprecent) * ((float) this.brightnessbackup));
                    mDpcBase.mBrightnessSource = 20;
                }
            }
        }
        OppoBaseDisplayPowerController2 oppoBaseDisplayPowerController22 = mDpcBase;
        if (OppoBaseDisplayPowerController2.mShouldAdjustRate == 2) {
            OppoBaseDisplayPowerController2 oppoBaseDisplayPowerController23 = mDpcBase;
            OppoBaseDisplayPowerController2.mShouldAdjustRate = 0;
            rate = (this.brightnessbackup * 3) / 100;
        }
        OppoBaseDisplayPowerController2 oppoBaseDisplayPowerController24 = mDpcBase;
        if (OppoBaseDisplayPowerController2.mShouldAdjustRate == 3) {
            OppoBaseDisplayPowerController2 oppoBaseDisplayPowerController25 = mDpcBase;
            OppoBaseDisplayPowerController2.mShouldAdjustRate = 0;
            rate = (brightness * 2) / 100;
        }
        if (mDpcBase.mScreenAnimBightnessTarget == brightness) {
            rate = mDpcBase.mScreenAnimBightnessRate;
        }
        this.target = brightness;
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

    public int getuseReduceBrightness() {
        return mDpcBase.useReduceBrightness;
    }

    public int getTempLimit() {
        return this.tempLimit;
    }

    public int getbrightnessbackup() {
        return this.brightnessbackup;
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
