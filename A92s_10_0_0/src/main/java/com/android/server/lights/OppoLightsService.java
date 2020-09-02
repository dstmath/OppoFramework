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
import android.provider.Settings;
import android.util.Log;
import android.util.Slog;
import com.android.internal.os.BackgroundThread;
import com.android.server.display.IOppoBrightness;
import com.android.server.display.IPswFeatureReduceBrightness;
import com.android.server.display.ManualSharedConstants;
import com.android.server.display.OppoBrightUtils;
import com.android.server.lights.LightsService;

public class OppoLightsService extends LightsService {
    public static final String BUTTON_LIGHT_MODE = "button_light_mode";
    public static final String BUTTON_LIGHT_TIMEOUT = "button_light_timeout";
    public static boolean DEBUG = false;
    private static final String SF_COMPOSER_TOKEN = "android.ui.ISurfaceComposer";
    private static final String SF_SERVICE_NAME = "SurfaceFlinger";
    private static final String TAG = "OppoLightsService";
    public static IBinder mFlinger = null;
    public static final int mFlingerCriticalPoint = 200;
    public static int mFlingerLight = 0;
    public static final int mFlingerTransactionCode = 21005;
    public static int mScreenBrightness = 127;
    public boolean bootcompleted = false;
    public Context mContext;

    public OppoLightsService(Context context) {
        super(context);
        this.mContext = context;
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
        /* access modifiers changed from: private */
        public int mBrightnessMode;
        /* access modifiers changed from: private */
        public int mButtonLightMode;
        /* access modifiers changed from: private */
        public long mButtonLightTimeout;
        /* access modifiers changed from: private */
        public int mColor;
        private MyHandler mHandler;
        /* access modifiers changed from: private */
        public boolean mHasEnabled;
        /* access modifiers changed from: private */
        public int mMode;
        /* access modifiers changed from: private */
        public int mOffMS;
        /* access modifiers changed from: private */
        public int mOnMS;

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

        class MyHandler extends Handler {
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
                int unused = ButtonLight.this.mButtonLightMode = buttonLightMode;
                if (OppoLightsService.DEBUG) {
                    Log.d(OppoLightsService.TAG, "+++++++SettingsObserver update buttonLightMode=" + buttonLightMode + ", tId=" + Thread.currentThread().getId());
                }
                long buttonLightTimeout = Settings.System.getLong(resolver, OppoLightsService.BUTTON_LIGHT_TIMEOUT, ButtonLight.TIMEOUT_DEFAULT);
                long unused2 = ButtonLight.this.mButtonLightTimeout = buttonLightTimeout;
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
        public void turnButtonLightOff() {
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
        }
    }

    public void notifyFlingerLight(int color) {
        if (color >= 200 && mFlingerLight >= 200) {
            return;
        }
        if (color >= 200 || mFlingerLight >= 200) {
            if (mFlinger == null) {
                mFlinger = ServiceManager.getService(SF_SERVICE_NAME);
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

    public void onSetLightHelper(int id, int brightness, int brightnessMode) {
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
                Slog.d(TAG, "brightness after color = " + brightness2);
            }
        }
        this.mLights[id].setLightLocked(color2, 0, 0, 0, brightnessMode);
        notifyFlingerLight(color2);
        float temp1 = 150.0f / OppoFeatureCache.getOrCreate(IPswFeatureReduceBrightness.DEFAULT, new Object[0]).getReduceBrightnessRate();
        float temp2 = OppoFeatureCache.getOrCreate(IPswFeatureReduceBrightness.DEFAULT, new Object[0]).getReduceBrightnessRate() * 150.0f;
        if (OppoFeatureCache.getOrCreate(IPswFeatureReduceBrightness.DEFAULT, new Object[0]).getReduceBrightnessMode() == 1 && OppoFeatureCache.getOrCreate(IPswFeatureReduceBrightness.DEFAULT, new Object[0]).getReduceBrightnessRate() != 1.0f && ((float) brightness2) >= temp2) {
            brightness2 = (((float) brightness2) < temp2 || brightness2 > 150) ? (int) Math.min(2047.0d, Math.ceil((double) (((float) brightness2) / OppoFeatureCache.getOrCreate(IPswFeatureReduceBrightness.DEFAULT, new Object[0]).getReduceBrightnessRate()))) : (int) (((((float) brightness2) * (temp1 - temp2)) / (150.0f - temp2)) - (((temp1 - 150.0f) * temp2) / (150.0f - temp2)));
        }
        if (OppoFeatureCache.getOrCreate(IOppoBrightness.DEFAULT, new Object[0]).isLowPowerMode()) {
            brightness2 = Math.min((int) OppoBrightUtils.ELEVEN_BITS_MAXBRIGHTNESS, brightness2 * 2);
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

    @Override // com.android.server.lights.LightsService
    public void onSetLight(int id, int brightness, int brightnessMode) {
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
            brightness2 = Math.min((int) OppoBrightUtils.ELEVEN_BITS_MAXBRIGHTNESS, brightness2 * 2);
        }
        if (OppoFeatureCache.getOrCreate(IOppoBrightness.DEFAULT, new Object[0]).isFingerprintOpticalSupport() && this.bootcompleted && id == 0) {
            Settings.System.putIntForUser(this.mContext.getContentResolver(), "real_screen_brightness", brightness2, -2);
        }
        if (OppoFeatureCache.getOrCreate(IOppoBrightness.DEFAULT, new Object[0]).isUseAutoBrightness() && id == 0 && !OppoFeatureCache.getOrCreate(IOppoBrightness.DEFAULT, new Object[0]).isManualSetAutoBrightness() && !OppoFeatureCache.getOrCreate(IOppoBrightness.DEFAULT, new Object[0]).isReduceBrightnessAnimating()) {
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
