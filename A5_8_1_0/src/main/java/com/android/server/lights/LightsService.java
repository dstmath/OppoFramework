package com.android.server.lights;

import android.app.ActivityManager;
import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.PowerManagerInternal;
import android.os.PowerManagerInternal.LowPowerModeListener;
import android.os.PowerSaveState;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.Trace;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.util.Slog;
import com.android.server.LocalServices;
import com.android.server.SystemService;
import com.android.server.display.OppoBrightUtils;
import com.android.server.fingerprint.power.FingerprintInternal;
import com.android.server.usb.UsbAudioDevice;
import java.util.concurrent.atomic.AtomicBoolean;

public class LightsService extends SystemService {
    public static boolean DEBUG = false;
    private static final String KEY_POWER_SAVE_SUB_BACKLIGHT = "power_save_backlight_state";
    public static final int MSG_DELAY_SETLIGHT = 1;
    public static final int SETLIGHT_WAITING_TIME = 32;
    static final String TAG = "LightsService";
    public static int mScreenBrightness = 127;
    private int mBrightnessModeSkiped;
    private AtomicBoolean mColorOsLowPowerModeEnabled = new AtomicBoolean(false);
    private int mColorSkiped;
    private final Context mContext;
    FingerprintInternal mFingerprintInternal;
    private Handler mH = new Handler() {
        public void handleMessage(Message msg) {
            msg.obj.stopFlashing();
        }
    };
    private MyHandler mHandler;
    private HandlerThread mHandlerThread;
    private boolean mHasNotifiedScreenOn = false;
    private boolean mIsSubSwitchReged = false;
    private float mKeyguardAlphaInWms = 255.0f;
    LightImpl[] mLights = new LightImpl[8];
    private Looper mLooper;
    private int mMinAdjBrightness = 12;
    private boolean mNeedReSetLight = false;
    private OppoBrightUtils mOppoBrightUtils;
    private PowerManagerInternal mPowerManagerInternal;
    private AtomicBoolean mPowerSaveSubEnabled = new AtomicBoolean(false);
    private volatile int mScreenBrightnessBakup;
    final LightsManager mService = new LightsManager() {
        public Light getLight(int id) {
            if (id < 0 || id >= 8) {
                return null;
            }
            return LightsService.this.mLights[id];
        }

        public void setKeyguardWindowAlpha(float alpha) {
            LightsService.this.mKeyguardAlphaInWms = alpha;
            if (LightsService.DEBUG) {
                Slog.v(LightsService.TAG, "mKeyguardAlphaInWms = " + LightsService.this.mKeyguardAlphaInWms);
            }
        }

        public boolean getLightState(int id) {
            return LightsService.this.mLights[id].mColor != 0;
        }
    };

    class LightImpl extends Light {
        private int mBrightnessMode;
        private int mColor;
        private boolean mFlashing;
        private int mId;
        private boolean mInitialized;
        private int mLastBrightnessMode;
        private int mLastColor;
        private int mMode;
        private int mOffMS;
        private int mOnMS;
        private boolean mUseLowPersistenceForVR;
        private boolean mVrModeEnabled;

        LightImpl(int id) {
            this.mId = id;
        }

        public void setBrightness(int brightness) {
            setBrightness(brightness, 0);
        }

        /* JADX WARNING: Missing block: B:47:0x0115, code:
            if (com.android.server.display.OppoBrightUtils.mVideoMode != 1) goto L_0x0117;
     */
        /* JADX WARNING: Missing block: B:48:0x0117, code:
            com.android.server.lights.LightsService.-get8(r8.this$0);
            r9 = (int) java.lang.Math.min((double) com.android.server.display.OppoBrightUtils.mMaxBrightness, java.lang.Math.ceil((double) ((((float) r9) * 100.0f) / 93.0f)));
     */
        /* JADX WARNING: Missing block: B:51:0x0142, code:
            return;
     */
        /* JADX WARNING: Missing block: B:53:0x0145, code:
            if (r9 < 250) goto L_0x0117;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void setBrightness(int brightness, int brightnessMode) {
            synchronized (this) {
                if (brightnessMode == 2) {
                    Slog.w(LightsService.TAG, "setBrightness with LOW_PERSISTENCE unexpected #" + this.mId + ": brightness=0x" + Integer.toHexString(brightness));
                    return;
                }
                if (this.mId == 0) {
                    if (brightness < 0) {
                        return;
                    }
                    LightsService.mScreenBrightness = brightness;
                }
                brightness = LightsService.this.mOppoBrightUtils.changeMinBrightness(brightness);
                int color = brightness & 255;
                color |= ((color << 16) | UsbAudioDevice.kAudioDeviceMetaMask) | (color << 8);
                if (this.mId == 0) {
                    LightsService.this.mScreenBrightnessBakup = color;
                    if (LightsService.this.mColorOsLowPowerModeEnabled.get() && color > LightsService.this.mMinAdjBrightness) {
                        int backlight = color / 2;
                        if (backlight < LightsService.this.mMinAdjBrightness) {
                            backlight = LightsService.this.mMinAdjBrightness;
                        }
                        if (LightsService.DEBUG) {
                            Slog.d(LightsService.TAG, "PoweSaveMode. brightness from " + color + " to " + backlight);
                        }
                        color = backlight;
                    }
                }
                if (this.mId == 0) {
                    color = brightness;
                    LightsService.this.mOppoBrightUtils;
                    OppoBrightUtils.mBrightnessNoAnimation = false;
                    if (OppoBrightUtils.DEBUG) {
                        Slog.d(LightsService.TAG, "brightness color = " + brightness);
                    }
                }
                setLightLocked(color, 0, 0, 0, brightnessMode);
                LightsService.this.mOppoBrightUtils;
                if (OppoBrightUtils.mUseAutoBrightness && this.mId == 0) {
                    LightsService.this.mOppoBrightUtils;
                    if ((OppoBrightUtils.mManualSetAutoBrightness ^ 1) != 0) {
                        LightsService.this.mOppoBrightUtils;
                        if (OppoBrightUtils.mHighBrightnessModeSupport) {
                            LightsService.this.mOppoBrightUtils;
                            if (OppoBrightUtils.mCameraMode != 1) {
                                LightsService.this.mOppoBrightUtils;
                                if (OppoBrightUtils.mGalleryMode != 1) {
                                    LightsService.this.mOppoBrightUtils;
                                }
                            }
                        }
                        System.putIntForBrightness(LightsService.this.mContext.getContentResolver(), "screen_brightness", brightness, -2);
                    }
                }
            }
        }

        public void setColor(int color) {
            synchronized (this) {
                setLightLocked(color, 0, 0, 0, 0);
            }
        }

        public void setFlashing(int color, int mode, int onMS, int offMS) {
            synchronized (this) {
                setLightLocked(color, mode, onMS, offMS, 0);
            }
        }

        public void pulse() {
            pulse(UsbAudioDevice.kAudioDeviceClassMask, 7);
        }

        public void pulse(int color, int onMS) {
            synchronized (this) {
                if (this.mColor == 0 && (this.mFlashing ^ 1) != 0) {
                    setLightLocked(color, 2, onMS, 1000, 0);
                    this.mColor = 0;
                    LightsService.this.mH.sendMessageDelayed(Message.obtain(LightsService.this.mH, 1, this), (long) onMS);
                }
            }
        }

        public void turnOff() {
            synchronized (this) {
                setLightLocked(0, 0, 0, 0, 0);
            }
        }

        public void setVrMode(boolean enabled) {
            boolean z = false;
            synchronized (this) {
                if (this.mVrModeEnabled != enabled) {
                    this.mVrModeEnabled = enabled;
                    if (LightsService.this.getVrDisplayMode() == 0) {
                        z = true;
                    }
                    this.mUseLowPersistenceForVR = z;
                    if (shouldBeInLowPersistenceMode()) {
                        this.mLastBrightnessMode = this.mBrightnessMode;
                    }
                }
            }
        }

        private void stopFlashing() {
            synchronized (this) {
                setLightLocked(this.mColor, 0, 0, 0, 0);
            }
        }

        void setLightLocked(int color, int mode, int onMS, int offMS, int brightnessMode) {
            if (this.mId == 0) {
                LightsService.this.mScreenBrightnessBakup = color;
                if (LightsService.this.mColorOsLowPowerModeEnabled.get() && LightsService.this.mPowerSaveSubEnabled.get() && color > LightsService.this.mMinAdjBrightness) {
                    int backlight = color / 2;
                    if (backlight < LightsService.this.mMinAdjBrightness) {
                        backlight = LightsService.this.mMinAdjBrightness;
                    }
                    if (LightsService.DEBUG) {
                        Slog.d(LightsService.TAG, "PoweSaveMode. brightness from " + color + " to " + backlight);
                    }
                    color = backlight;
                }
            }
            if (shouldBeInLowPersistenceMode()) {
                brightnessMode = 2;
            } else if (brightnessMode == 2) {
                brightnessMode = this.mLastBrightnessMode;
            }
            if (!this.mInitialized || color != this.mColor || mode != this.mMode || onMS != this.mOnMS || offMS != this.mOffMS || this.mBrightnessMode != brightnessMode) {
                if (LightsService.DEBUG) {
                    Slog.v(LightsService.TAG, "setLight #" + this.mId + ": color=#" + Integer.toHexString(color) + ": brightnessMode=" + brightnessMode);
                }
                if (LightsService.this.getFingerprintInternal() != null && this.mId == 0 && color != 0 && (LightsService.this.mHasNotifiedScreenOn ^ 1) != 0) {
                    if (LightsService.DEBUG) {
                        Slog.v(LightsService.TAG, "onLightScreenOnFinish mHasNotifiedScreenOn = " + LightsService.this.mHasNotifiedScreenOn);
                    }
                    LightsService.this.mHasNotifiedScreenOn = true;
                    LightsService.this.getFingerprintInternal().onLightScreenOnFinish();
                } else if (this.mId == 0 && color == 0) {
                    LightsService.this.mHasNotifiedScreenOn = false;
                }
                this.mInitialized = true;
                this.mLastColor = this.mColor;
                this.mColor = color;
                this.mMode = mode;
                this.mOnMS = onMS;
                this.mOffMS = offMS;
                this.mBrightnessMode = brightnessMode;
                Trace.traceBegin(131072, "setLight(" + this.mId + ", 0x" + Integer.toHexString(color) + ")");
                try {
                    LightsService.setLight_native(this.mId, color, mode, onMS, offMS, brightnessMode);
                } finally {
                    Trace.traceEnd(131072);
                }
            }
        }

        private boolean shouldBeInLowPersistenceMode() {
            return this.mVrModeEnabled ? this.mUseLowPersistenceForVR : false;
        }
    }

    private class MyHandler extends Handler {
        /* synthetic */ MyHandler(LightsService this$0, Looper looper, MyHandler -this2) {
            this(looper);
        }

        private MyHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if (LightsService.DEBUG) {
                        Slog.v(LightsService.TAG, "setLight is delayed 32ms because surfaceFlinger is Blocked end");
                    }
                    LightsService.this.mService.getLight(0).setBrightness(LightsService.this.mColorSkiped, LightsService.this.mBrightnessModeSkiped);
                    return;
                default:
                    Slog.w(LightsService.TAG, "Unknown message:" + msg.what);
                    return;
            }
        }
    }

    static native void setLight_native(int i, int i2, int i3, int i4, int i5, int i6);

    private FingerprintInternal getFingerprintInternal() {
        if (this.mFingerprintInternal == null) {
            this.mFingerprintInternal = (FingerprintInternal) LocalServices.getService(FingerprintInternal.class);
        }
        return this.mFingerprintInternal;
    }

    public int hasKeyguard() {
        try {
            IBinder flinger = ServiceManager.getService("SurfaceFlinger");
            if (flinger != null) {
                Parcel data = Parcel.obtain();
                Parcel reply = Parcel.obtain();
                data.writeInterfaceToken("android.ui.ISurfaceComposer");
                flinger.transact(20000, data, reply, 0);
                int result = reply.readInt();
                data.recycle();
                reply.recycle();
                return result;
            }
        } catch (RemoteException e) {
        }
        return -1;
    }

    public LightsService(Context context) {
        super(context);
        this.mContext = context;
        this.mOppoBrightUtils = OppoBrightUtils.getInstance();
        for (int i = 0; i < 8; i++) {
            this.mLights[i] = new LightImpl(i);
        }
        this.mHandlerThread = new HandlerThread("LightService thread");
        this.mHandlerThread.start();
        this.mLooper = this.mHandlerThread.getLooper();
        if (this.mLooper == null) {
            Slog.e(TAG, "mLooper null");
        }
        this.mHandler = new MyHandler(this, this.mLooper, null);
    }

    public void onStart() {
        publishLocalService(LightsManager.class, this.mService);
    }

    public void onBootPhase(int phase) {
        if (phase == 500) {
            boolean hasFeature = this.mContext.getPackageManager().hasSystemFeature("oppo.system.cmcc.powersave");
            Slog.d(TAG, "onBootPhase: hasFeature= " + hasFeature);
            if (!hasFeature) {
                this.mPowerManagerInternal = (PowerManagerInternal) LocalServices.getService(PowerManagerInternal.class);
                this.mPowerManagerInternal.registerColorOsLowPowerModeObserver(new LowPowerModeListener() {
                    public int getServiceType() {
                        return 0;
                    }

                    public void onLowPowerModeChanged(PowerSaveState state) {
                        LightsService.this.onLowPowerModeChangedInternal(state.batterySaverEnabled);
                    }
                });
                this.mColorOsLowPowerModeEnabled.set(this.mPowerManagerInternal.getColorOsLowPowerModeEnabled());
            }
        }
    }

    private void onLowPowerModeChangedInternal(boolean enabled) {
        this.mMinAdjBrightness = (SystemProperties.getInt("sys.oppo.multibrightness", 255) * 5) / 100;
        this.mColorOsLowPowerModeEnabled.set(enabled);
        setSubSwitchEnable();
        setScreenBrigntness(this.mScreenBrightnessBakup);
        regPowerSaveSubSwitch();
        Slog.d(TAG, "lowPowerModeChanged: enabled=" + enabled + ", screenBrightnessBakup=" + this.mScreenBrightnessBakup + ", minAdjBrightness=" + this.mMinAdjBrightness + ", subEnable=" + this.mPowerSaveSubEnabled.get());
    }

    private void setScreenBrigntness(int brightness) {
        this.mLights[0].setBrightness(brightness);
    }

    private void setSubSwitchEnable() {
        boolean z = true;
        int ret = System.getInt(this.mContext.getContentResolver(), KEY_POWER_SAVE_SUB_BACKLIGHT, 1);
        AtomicBoolean atomicBoolean = this.mPowerSaveSubEnabled;
        if (ret == 0) {
            z = false;
        }
        atomicBoolean.set(z);
        Slog.d(TAG, "setSubSwitchEnable: ret=" + this.mPowerSaveSubEnabled.get());
    }

    private void regPowerSaveSubSwitch() {
        if (!this.mIsSubSwitchReged) {
            this.mIsSubSwitchReged = true;
            this.mContext.getContentResolver().registerContentObserver(System.getUriFor(KEY_POWER_SAVE_SUB_BACKLIGHT), false, new ContentObserver(new Handler()) {
                public void onChange(boolean selfChange) {
                    Slog.d(LightsService.TAG, "Power Save backlight sub change.");
                    LightsService.this.setSubSwitchEnable();
                    LightsService.this.setScreenBrigntness(LightsService.this.mScreenBrightnessBakup);
                }
            });
        }
    }

    private int getVrDisplayMode() {
        return Secure.getIntForUser(getContext().getContentResolver(), "vr_display_mode", 0, ActivityManager.getCurrentUser());
    }
}
