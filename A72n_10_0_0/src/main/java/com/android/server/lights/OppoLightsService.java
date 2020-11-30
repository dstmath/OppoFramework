package com.android.server.lights;

import android.common.OppoFeatureCache;
import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Log;
import android.util.Slog;
import android.view.SurfaceControl;
import com.android.internal.os.BackgroundThread;
import com.android.server.display.IOppoBrightness;
import com.android.server.display.IPswFeatureReduceBrightness;
import com.android.server.display.ManualSharedConstants;
import com.android.server.display.OppoBrightUtils;
import com.android.server.display.color.DisplayTransformManager;
import com.android.server.lights.LightsService;

public class OppoLightsService extends LightsService {
    public static final String BUTTON_LIGHT_MODE = "button_light_mode";
    public static final String BUTTON_LIGHT_TIMEOUT = "button_light_timeout";
    public static boolean DEBUG = false;
    private static final String SF_COMPOSER_TOKEN = "android.ui.ISurfaceComposer";
    private static final String SF_SERVICE_NAME = "SurfaceFlinger";
    public static final int SF_TRANSACTION_DC_CHANGED_CODE = 21010;
    public static final int SF_TRANSACTION_SWITCH_MODE_CODE = 21009;
    public static final String SURFACE_FLINGER = "SurfaceFlinger";
    private static final String TAG = "OppoLightsService";
    private static IBinder mDisplayToken = null;
    public static IBinder mFlinger = null;
    public static final int mFlingerCriticalPoint = 200;
    public static int mFlingerLight = 0;
    public static final int mFlingerTransactionCode = 21005;
    public static int mScreenBrightness = 127;
    public boolean bootcompleted = false;
    private int mBtExtendSupported = 0;
    public Context mContext;
    private int mMaxBrightness = OppoBrightUtils.ELEVEN_BITS_MAXBRIGHTNESS;
    private int mMode = -1;
    private int mReduBnMax = OppoBrightUtils.ELEVEN_BITS_MAXBRIGHTNESS;
    private int mReduBnThreshold = DisplayTransformManager.LEVEL_COLOR_MATRIX_SATURATION;
    private final boolean mSoftwareBnSupport;

    public OppoLightsService(Context context) {
        super(context);
        this.mContext = context;
        mDisplayToken = SurfaceControl.getInternalDisplayToken();
        mFlinger = ServiceManager.getService("SurfaceFlinger");
        this.mSoftwareBnSupport = SurfaceControl.getDisplayBrightnessSupport(mDisplayToken);
        this.mBtExtendSupported = SystemProperties.getInt("persist.sys.extend.brightness", 0);
        if (this.mBtExtendSupported == 1) {
            this.mReduBnThreshold = 1830;
            this.mReduBnMax = OppoFeatureCache.getOrCreate(IOppoBrightness.DEFAULT, new Object[0]).getHBM_SW_EXT_BTN_MAX_4095_5119();
            return;
        }
        this.mReduBnThreshold = DisplayTransformManager.LEVEL_COLOR_MATRIX_SATURATION;
        this.mReduBnMax = OppoBrightUtils.ELEVEN_BITS_MAXBRIGHTNESS;
    }

    public void systemReady() {
        setLight(2, new ButtonLight(2));
    }

    /* access modifiers changed from: package-private */
    public void setLight(int id, LightsService.LightImpl light) {
        this.mLights[id] = light;
    }

    public class ButtonLight extends LightsService.LightImpl {
        public static final int BRIGHTNESS_DEFAULT = 100;
        private static final int MESSAGE_TURN_LIGHT_OFF = 1000;
        public static final int MODE_ALWAYS_OFF = 2;
        public static final int MODE_ALWAYS_ON = 1;
        public static final int MODE_AUTO_SENSOR = 4;
        public static final int MODE_AUTO_TIMEOUT = 3;
        public static final long TIMEOUT_DEFAULT = 6000;
        private int mBrightnessMode;
        private int mButtonLightMode;
        private long mButtonLightTimeout;
        private int mColor;
        private MyHandler mHandler;
        private boolean mHasEnabled;
        private int mMode;
        private int mOffMS;
        private int mOnMS;

        @Override // com.android.server.lights.Light, com.android.server.lights.LightsService.LightImpl
        public /* bridge */ /* synthetic */ void pulse() {
            super.pulse();
        }

        @Override // com.android.server.lights.Light, com.android.server.lights.LightsService.LightImpl
        public /* bridge */ /* synthetic */ void pulse(int i, int i2) {
            super.pulse(i, i2);
        }

        @Override // com.android.server.lights.Light, com.android.server.lights.LightsService.LightImpl
        public /* bridge */ /* synthetic */ void setBrightness(int i) {
            super.setBrightness(i);
        }

        @Override // com.android.server.lights.Light, com.android.server.lights.LightsService.LightImpl
        public /* bridge */ /* synthetic */ void setBrightness(int i, int i2) {
            super.setBrightness(i, i2);
        }

        @Override // com.android.server.lights.Light, com.android.server.lights.LightsService.LightImpl
        public /* bridge */ /* synthetic */ void setColor(int i) {
            super.setColor(i);
        }

        @Override // com.android.server.lights.Light, com.android.server.lights.LightsService.LightImpl
        public /* bridge */ /* synthetic */ void setFlashing(int i, int i2, int i3, int i4) {
            super.setFlashing(i, i2, i3, i4);
        }

        @Override // com.android.server.lights.Light, com.android.server.lights.LightsService.LightImpl
        public /* bridge */ /* synthetic */ void setVrMode(boolean z) {
            super.setVrMode(z);
        }

        @Override // com.android.server.lights.Light, com.android.server.lights.LightsService.LightImpl
        public /* bridge */ /* synthetic */ void turnOff() {
            super.turnOff();
        }

        /* access modifiers changed from: package-private */
        public class MyHandler extends Handler {
            MyHandler(Looper looper) {
                super(looper);
            }

            public void handleMessage(Message msg) {
                if (msg.what == 1000) {
                    ButtonLight.this.turnButtonLightOff();
                }
            }
        }

        class SettingsObserver extends ContentObserver {
            SettingsObserver(Handler handler) {
                super(handler);
            }

            /* access modifiers changed from: package-private */
            public void observe() {
                ContentResolver resolver = OppoLightsService.this.mContext.getContentResolver();
                resolver.registerContentObserver(Settings.System.getUriFor(OppoLightsService.BUTTON_LIGHT_MODE), false, this);
                resolver.registerContentObserver(Settings.System.getUriFor(OppoLightsService.BUTTON_LIGHT_TIMEOUT), false, this);
                update();
            }

            public void onChange(boolean selfChange) {
                update();
            }

            public void update() {
                ContentResolver resolver = OppoLightsService.this.mContext.getContentResolver();
                int buttonLightMode = Settings.System.getInt(resolver, OppoLightsService.BUTTON_LIGHT_MODE, 3);
                ButtonLight.this.mButtonLightMode = buttonLightMode;
                if (OppoLightsService.DEBUG) {
                    Log.d(OppoLightsService.TAG, "+++++++SettingsObserver update buttonLightMode=" + buttonLightMode + ", tId=" + Thread.currentThread().getId());
                }
                long buttonLightTimeout = Settings.System.getLong(resolver, OppoLightsService.BUTTON_LIGHT_TIMEOUT, ButtonLight.TIMEOUT_DEFAULT);
                ButtonLight.this.mButtonLightTimeout = buttonLightTimeout;
                Log.d(OppoLightsService.TAG, "+++++++SettingsObserver update buttonLightTimeout=" + buttonLightTimeout);
                if (ButtonLight.this.mHasEnabled) {
                    synchronized (this) {
                        ButtonLight.this.setLightLocked(ButtonLight.this.mColor, ButtonLight.this.mMode, ButtonLight.this.mOnMS, ButtonLight.this.mOffMS, ButtonLight.this.mBrightnessMode);
                    }
                }
            }
        }

        private ButtonLight(int nId) {
            super(OppoLightsService.this.mContext, nId);
            this.mColor = 0;
            this.mMode = 0;
            this.mOnMS = 0;
            this.mOffMS = 0;
            this.mBrightnessMode = 0;
            this.mHasEnabled = false;
            this.mHandler = new MyHandler(BackgroundThread.getHandler().getLooper());
            new SettingsObserver(this.mHandler).observe();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void turnButtonLightOff() {
            if (OppoLightsService.DEBUG) {
                Log.d(OppoLightsService.TAG, "turnButtonLightOff: tId=" + Thread.currentThread().getId());
            }
            synchronized (this) {
                super.setLightLocked(0, 0, 0, 0, 0);
            }
        }

        /* access modifiers changed from: package-private */
        @Override // com.android.server.lights.LightsService.LightImpl
        public void setLightLocked(int color, int mode, int onMS, int offMS, int brightnessMode) {
            if (OppoLightsService.DEBUG) {
                RuntimeException stk = new RuntimeException();
                stk.fillInStackTrace();
                Slog.d(OppoLightsService.TAG, "+++++++ please ignore debug setLightLocked in", stk);
            }
            this.mHasEnabled = true;
            this.mHandler.removeMessages(1000);
            int i = this.mButtonLightMode;
            if (i == 1) {
                super.setLightLocked(color, mode, onMS, offMS, brightnessMode);
                if (OppoLightsService.DEBUG) {
                    Log.d(OppoLightsService.TAG, "+++++++setLightLocked mButtonLightMode = MODE_ALWAYS_ON");
                }
            } else if (i == 2) {
                super.setLightLocked(0, 0, 0, 0, 0);
                if (OppoLightsService.DEBUG) {
                    Log.d(OppoLightsService.TAG, "+++++++setLightLocked mButtonLightMode = MODE_ALWAYS_OFF");
                }
            } else if (i == 3) {
                super.setLightLocked(color, mode, onMS, offMS, brightnessMode);
                MyHandler myHandler = this.mHandler;
                myHandler.sendMessageDelayed(myHandler.obtainMessage(1000), this.mButtonLightTimeout);
                if (OppoLightsService.DEBUG) {
                    Log.d(OppoLightsService.TAG, "+++++++setLightLocked mButtonLightMode = MODE_AUTO_TIMEOUT");
                }
            } else if (i != 4) {
                super.setLightLocked(color, mode, onMS, offMS, brightnessMode);
            }
            this.mColor = color;
            this.mMode = mode;
            this.mOnMS = onMS;
            this.mOffMS = offMS;
            this.mBrightnessMode = brightnessMode;
        }
    }

    @Override // com.android.server.SystemService, com.android.server.lights.LightsService
    public void onBootPhase(int phase) {
        super.onBootPhase(phase);
        if (phase == 1000) {
            Settings.System.putIntForUser(this.mContext.getContentResolver(), "real_screen_brightness", getIntForBrightness(this.mContext.getContentResolver(), "screen_brightness", 0, -2), -2);
            this.bootcompleted = true;
            this.mMaxBrightness = OppoFeatureCache.getOrCreate(IOppoBrightness.DEFAULT, new Object[0]).getFinalMaxBrightness();
        }
    }

    public void notifyFlingerLight(int color) {
        if (color >= 200 && mFlingerLight >= 200) {
            return;
        }
        if (color >= 200 || mFlingerLight >= 200) {
            if (mFlinger == null) {
                mFlinger = ServiceManager.getService("SurfaceFlinger");
            }
            try {
                if (mFlinger != null) {
                    Parcel data = Parcel.obtain();
                    data.writeInterfaceToken(SF_COMPOSER_TOKEN);
                    data.writeInt(color);
                    mFlinger.transact(mFlingerTransactionCode, data, null, 0);
                    data.recycle();
                    mFlingerLight = color;
                    Slog.d(TAG, "notifyFlingerLight color=" + color + "code=" + mFlingerTransactionCode);
                    return;
                }
                Slog.d(TAG, "notifyFlingerLight null");
            } catch (RemoteException e) {
                Slog.d(TAG, "get SurfaceFlinger Service failed");
            } catch (SecurityException e2) {
                Slog.i(TAG, "SurfaceFlinger transaction exception ");
            }
        }
    }

    public static void applyDCMode(int mode) {
        if (mFlinger != null) {
            Parcel data = Parcel.obtain();
            data.writeInterfaceToken(SF_COMPOSER_TOKEN);
            data.writeStrongBinder(mDisplayToken);
            data.writeInt(mode);
            try {
                mFlinger.transact(SF_TRANSACTION_DC_CHANGED_CODE, data, null, 0);
            } catch (RemoteException ex) {
                Slog.e(TAG, "Failed to set DC mode", ex);
            } catch (SecurityException e) {
                Slog.i(TAG, "SurfaceFlinger transaction exception ");
            } catch (Throwable th) {
                data.recycle();
                throw th;
            }
            data.recycle();
            return;
        }
        Slog.e(TAG, "applyDCMode SF is null mode=" + mode);
    }

    public static void notifySFSwitchMode(int mode) {
        if (mFlinger != null) {
            Parcel data = Parcel.obtain();
            data.writeInterfaceToken(SF_COMPOSER_TOKEN);
            data.writeStrongBinder(mDisplayToken);
            data.writeInt(mode);
            try {
                mFlinger.transact(SF_TRANSACTION_SWITCH_MODE_CODE, data, null, 0);
                Slog.d(TAG, "notifySFSwitchMode mode=" + mode + " code=" + SF_TRANSACTION_SWITCH_MODE_CODE);
            } catch (RemoteException ex) {
                Slog.e(TAG, "Failed to set switch mode", ex);
            } catch (SecurityException e) {
                Slog.i(TAG, "SurfaceFlinger transaction exception ");
            } catch (Throwable th) {
                data.recycle();
                throw th;
            }
            data.recycle();
            return;
        }
        Slog.e(TAG, "notifySFSwitchMode SF is null mode=" + mode);
    }

    public void onSetLightHelper(int id, int brightness, int brightnessMode) {
        int i;
        if (id == 0) {
            if (brightness >= 0) {
                mScreenBrightness = brightness;
            } else {
                return;
            }
        }
        int brightness2 = OppoFeatureCache.getOrCreate(IOppoBrightness.DEFAULT, new Object[0]).changeMinBrightness(brightness);
        int color = brightness2 & 255;
        int color2 = color | -16777216 | (color << 16) | (color << 8);
        if (id == 0) {
            color2 = brightness2;
            OppoFeatureCache.getOrCreate(IOppoBrightness.DEFAULT, new Object[0]).setBrightnessNoAnimation(false);
            if (OppoFeatureCache.getOrCreate(IOppoBrightness.DEFAULT, new Object[0]).enableDebug()) {
                Slog.d(TAG, "brightness color = " + color2);
            }
        }
        if (this.mBtExtendSupported == 1 && id == 0 && mDisplayToken != null) {
            int swExtBtnMax = OppoFeatureCache.getOrCreate(IOppoBrightness.DEFAULT, new Object[0]).get_SW_EXT_BTN_MAX_4095();
            int hbmSWExtBtnMax = OppoFeatureCache.getOrCreate(IOppoBrightness.DEFAULT, new Object[0]).getHBM_SW_EXT_BTN_MAX_4095_5119();
            int hbmExtBtnMax = OppoFeatureCache.getOrCreate(IOppoBrightness.DEFAULT, new Object[0]).getHBM_EXTEND_MAXBRIGHTNESS();
            if (color2 == 1 || color2 == 0 || color2 > swExtBtnMax) {
                if (this.bootcompleted) {
                    if (color2 > swExtBtnMax) {
                        if (this.mMode != 3) {
                            notifySFSwitchMode(3);
                            this.mMode = 3;
                        }
                        color2 -= hbmSWExtBtnMax - hbmExtBtnMax;
                    } else if (color2 == 1 && this.mMode != 2) {
                        notifySFSwitchMode(2);
                        this.mMode = 2;
                    } else if (color2 == 0 && this.mMode != 4) {
                        notifySFSwitchMode(4);
                        this.mMode = 4;
                    }
                }
                this.mLights[id].setLightLocked(color2, 0, 0, 0, brightnessMode);
            } else {
                if (this.bootcompleted && this.mMode != 0) {
                    notifySFSwitchMode(0);
                }
                SurfaceControl.setDisplayBrightness(mDisplayToken, ((float) color2) / ((float) swExtBtnMax));
                this.mMode = 0;
            }
        } else {
            this.mLights[id].setLightLocked(color2, 0, 0, 0, brightnessMode);
        }
        if (id == 0 && OppoFeatureCache.getOrCreate(IOppoBrightness.DEFAULT, new Object[0]).enableDebug()) {
            Slog.d(TAG, "Iris DC color= " + color2 + " mBtExtendSupported=" + this.mBtExtendSupported + " mode=" + this.mMode);
        }
        notifyFlingerLight(color2);
        float temp1 = ((float) this.mReduBnThreshold) / OppoFeatureCache.getOrCreate(IPswFeatureReduceBrightness.DEFAULT, new Object[0]).getReduceBrightnessRate();
        float temp2 = ((float) this.mReduBnThreshold) * OppoFeatureCache.getOrCreate(IPswFeatureReduceBrightness.DEFAULT, new Object[0]).getReduceBrightnessRate();
        if (OppoFeatureCache.getOrCreate(IPswFeatureReduceBrightness.DEFAULT, new Object[0]).getReduceBrightnessMode() == 1 && OppoFeatureCache.getOrCreate(IPswFeatureReduceBrightness.DEFAULT, new Object[0]).getReduceBrightnessRate() != 1.0f && ((float) brightness2) >= temp2) {
            brightness2 = (((float) brightness2) < temp2 || brightness2 > (i = this.mReduBnThreshold)) ? (int) Math.min((double) this.mReduBnMax, Math.ceil((double) (((float) brightness2) / OppoFeatureCache.getOrCreate(IPswFeatureReduceBrightness.DEFAULT, new Object[0]).getReduceBrightnessRate()))) : (int) (((((float) brightness2) * (temp1 - temp2)) / (((float) i) - temp2)) - (((temp1 - ((float) i)) * temp2) / (((float) i) - temp2)));
        }
        if (OppoFeatureCache.getOrCreate(IOppoBrightness.DEFAULT, new Object[0]).isLowPowerMode()) {
            brightness2 = Math.min(this.mReduBnMax, brightness2 * 2);
        }
        if (OppoFeatureCache.getOrCreate(IOppoBrightness.DEFAULT, new Object[0]).isFingerprintOpticalSupport() && this.bootcompleted && id == 0) {
            Settings.System.putIntForUser(this.mContext.getContentResolver(), "real_screen_brightness", brightness2, -2);
        }
        if (ManualSharedConstants.sUseAutoBrightness && id == 0 && !ManualSharedConstants.sManualSetAutoBrightness && !OppoFeatureCache.getOrCreate(IPswFeatureReduceBrightness.DEFAULT, new Object[0]).isReduceBrightnessAnimating()) {
            putIntForBrightness(this.mContext.getContentResolver(), "screen_brightness", brightness2, -2);
        }
        if (brightness2 < 260) {
            OppoFeatureCache.getOrCreate(IOppoBrightness.DEFAULT, new Object[0]).notifySfRepaintEverything();
        }
    }

    public void onSetLightHelper2(int id, int brightness, int brightnessMode) {
        if (id == 0) {
            if (brightness >= 0) {
                mScreenBrightness = brightness;
            } else {
                return;
            }
        }
        int brightness2 = OppoFeatureCache.getOrCreate(IOppoBrightness.DEFAULT, new Object[0]).changeMinBrightness(brightness);
        int color = brightness2 & 255;
        int color2 = color | -16777216 | (color << 16) | (color << 8);
        if (id == 0) {
            color2 = brightness2;
            OppoFeatureCache.getOrCreate(IOppoBrightness.DEFAULT, new Object[0]).setBrightnessNoAnimation(false);
            if (OppoFeatureCache.getOrCreate(IOppoBrightness.DEFAULT, new Object[0]).enableDebug()) {
                Slog.d(TAG, "brightness color = " + color2);
            }
        }
        if (this.mBtExtendSupported == 1 && id == 0 && mDisplayToken != null) {
            int swExtBtnMax = OppoFeatureCache.getOrCreate(IOppoBrightness.DEFAULT, new Object[0]).get_SW_EXT_BTN_MAX_4095();
            int hbmSWExtBtnMax = OppoFeatureCache.getOrCreate(IOppoBrightness.DEFAULT, new Object[0]).getHBM_SW_EXT_BTN_MAX_4095_5119();
            int hbmExtBtnMax = OppoFeatureCache.getOrCreate(IOppoBrightness.DEFAULT, new Object[0]).getHBM_EXTEND_MAXBRIGHTNESS();
            if (color2 == 1 || color2 == 0 || color2 > swExtBtnMax) {
                if (this.bootcompleted) {
                    if (color2 > swExtBtnMax) {
                        if (this.mMode != 3) {
                            notifySFSwitchMode(3);
                            this.mMode = 3;
                        }
                        color2 -= hbmSWExtBtnMax - hbmExtBtnMax;
                    } else if (color2 == 1 && this.mMode != 2) {
                        notifySFSwitchMode(2);
                        this.mMode = 2;
                    } else if (color2 == 0 && this.mMode != 4) {
                        notifySFSwitchMode(4);
                        this.mMode = 4;
                    }
                }
                this.mLights[id].setLightLocked(color2, 0, 0, 0, brightnessMode);
            } else {
                if (this.bootcompleted && this.mMode != 0) {
                    notifySFSwitchMode(0);
                }
                SurfaceControl.setDisplayBrightness(mDisplayToken, ((float) color2) / ((float) swExtBtnMax));
                this.mMode = 0;
            }
        } else {
            this.mLights[id].setLightLocked(color2, 0, 0, 0, brightnessMode);
        }
        if (id == 0 && OppoFeatureCache.getOrCreate(IOppoBrightness.DEFAULT, new Object[0]).enableDebug()) {
            Slog.d(TAG, "Iris DC color= " + color2 + " mBtExtendSupported=" + this.mBtExtendSupported + " mode=" + this.mMode);
        }
        notifyFlingerLight(color2);
        if (OppoFeatureCache.getOrCreate(IOppoBrightness.DEFAULT, new Object[0]).isLowPowerMode()) {
            brightness2 = Math.min(this.mReduBnMax, brightness2 * 2);
        }
        if (OppoFeatureCache.getOrCreate(IOppoBrightness.DEFAULT, new Object[0]).isFingerprintOpticalSupport() && this.bootcompleted && id == 0) {
            Settings.System.putIntForUser(this.mContext.getContentResolver(), "real_screen_brightness", brightness2, -2);
        }
        if (ManualSharedConstants.sUseAutoBrightness && id == 0 && !ManualSharedConstants.sManualSetAutoBrightness && !OppoFeatureCache.getOrCreate(IPswFeatureReduceBrightness.DEFAULT, new Object[0]).isReduceBrightnessAnimating()) {
            putIntForBrightness(this.mContext.getContentResolver(), "screen_brightness", brightness2, -2);
        }
        if (brightness2 < 260) {
            OppoFeatureCache.getOrCreate(IOppoBrightness.DEFAULT, new Object[0]).notifySfRepaintEverything();
        }
    }

    public void onSetLight(int id, int brightness, int brightnessMode) {
        boolean isBrightnessAnimating = OppoFeatureCache.getOrCreate(IOppoBrightness.DEFAULT, new Object[0]).isReduceBrightnessAnimating();
        if (id == 0) {
            if (brightness >= 0) {
                mScreenBrightness = brightness;
            } else {
                return;
            }
        }
        int brightness2 = OppoFeatureCache.getOrCreate(IOppoBrightness.DEFAULT, new Object[0]).changeMinBrightness(brightness);
        int color = brightness2 & 255;
        int color2 = color | -16777216 | (color << 16) | (color << 8);
        if (id == 0) {
            color2 = brightness2;
            OppoFeatureCache.getOrCreate(IOppoBrightness.DEFAULT, new Object[0]).setBrightnessNoAnimation(false);
            if (OppoFeatureCache.getOrCreate(IOppoBrightness.DEFAULT, new Object[0]).enableDebug()) {
                Slog.d(TAG, "brightness color = " + color2);
            }
        }
        this.mLights[id].setLightLocked(color2, 0, 0, 0, brightnessMode);
        notifyFlingerLight(color2);
        float temp1 = 150.0f / OppoFeatureCache.getOrCreate(IOppoBrightness.DEFAULT, new Object[0]).getReduceBrightnessRate();
        float temp2 = OppoFeatureCache.getOrCreate(IOppoBrightness.DEFAULT, new Object[0]).getReduceBrightnessRate() * 150.0f;
        if (OppoFeatureCache.getOrCreate(IOppoBrightness.DEFAULT, new Object[0]).getReduceBrightnessMode() == 1 && OppoFeatureCache.getOrCreate(IOppoBrightness.DEFAULT, new Object[0]).getReduceBrightnessRate() != 1.0f && ((float) brightness2) >= temp2) {
            brightness2 = (((float) brightness2) < temp2 || brightness2 > 150) ? (int) Math.min(2047.0d, Math.ceil((double) (((float) brightness2) / OppoFeatureCache.getOrCreate(IOppoBrightness.DEFAULT, new Object[0]).getReduceBrightnessRate()))) : (int) (((((float) brightness2) * (temp1 - temp2)) / (150.0f - temp2)) - (((temp1 - 150.0f) * temp2) / (150.0f - temp2)));
        }
        if (OppoFeatureCache.getOrCreate(IOppoBrightness.DEFAULT, new Object[0]).isLowPowerMode()) {
            brightness2 = Math.min(this.mMaxBrightness, brightness2 * 2);
        }
        if (OppoFeatureCache.getOrCreate(IOppoBrightness.DEFAULT, new Object[0]).isFingerprintOpticalSupport() && this.bootcompleted && id == 0) {
            Settings.System.putIntForUser(this.mContext.getContentResolver(), "real_screen_brightness", brightness2, -2);
        }
        if (OppoFeatureCache.getOrCreate(IOppoBrightness.DEFAULT, new Object[0]).isUseAutoBrightness() && id == 0 && !OppoFeatureCache.getOrCreate(IOppoBrightness.DEFAULT, new Object[0]).isManualSetAutoBrightness() && !isBrightnessAnimating) {
            putIntForBrightness(this.mContext.getContentResolver(), "screen_brightness", brightness2, -2);
        }
        if (brightness2 < 260) {
            OppoFeatureCache.getOrCreate(IOppoBrightness.DEFAULT, new Object[0]).notifySfRepaintEverything();
        }
    }

    private int getIntForBrightness(ContentResolver cr, String name, int def, int userHandle) {
        String v = Settings.System.getStringForUser(cr, name, userHandle);
        if (v == null) {
            return def;
        }
        try {
            return Integer.parseInt(v);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    public static int getIntForBrightness(ContentResolver cr, String name, int userHandle) throws Settings.SettingNotFoundException {
        try {
            return Integer.parseInt(Settings.System.getStringForUser(cr, name, userHandle));
        } catch (NumberFormatException e) {
            throw new Settings.SettingNotFoundException(name);
        }
    }

    public static boolean putIntForBrightness(ContentResolver cr, String name, int value, int userHandle) {
        return Settings.System.putStringForUser(cr, name, Integer.toString(value), userHandle);
    }
}
