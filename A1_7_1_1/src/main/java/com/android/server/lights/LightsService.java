package com.android.server.lights;

import android.app.ActivityManager;
import android.content.Context;
import android.hardware.fingerprint.FingerprintInternal;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.PowerManagerInternal;
import android.os.PowerManagerInternal.LowPowerModeListener;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.Trace;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.service.vr.IVrManager;
import android.service.vr.IVrStateCallbacks;
import android.service.vr.IVrStateCallbacks.Stub;
import android.util.Slog;
import com.android.server.LocalServices;
import com.android.server.SystemService;
import com.android.server.display.OppoBrightUtils;
import com.android.server.usb.UsbAudioDevice;
import com.android.server.vr.VrManagerService;
import java.util.concurrent.atomic.AtomicBoolean;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
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
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class LightsService extends SystemService {
    public static boolean DEBUG = false;
    public static final int MSG_DELAY_SETLIGHT = 1;
    public static final int SETLIGHT_WAITING_TIME = 32;
    static final String TAG = "LightsService";
    public static int mScreenBrightness;
    private int mBrightnessModeSkiped;
    private AtomicBoolean mColorOsLowPowerModeEnabled;
    private int mColorSkiped;
    private final Context mContext;
    FingerprintInternal mFingerprintInternal;
    private Handler mH;
    private MyHandler mHandler;
    private HandlerThread mHandlerThread;
    private boolean mHasNotifiedScreenOn;
    private float mKeyguardAlphaInWms;
    LightImpl[] mLights;
    private Looper mLooper;
    private int mMinAdjBrightness;
    private long mNativePointer;
    private boolean mNeedReSetLight;
    private OppoBrightUtils mOppoBrightUtils;
    private PowerManagerInternal mPowerManagerInternal;
    private volatile int mScreenBrightnessBakup;
    final LightsManager mService;
    private boolean mVrModeEnabled;
    private final IVrStateCallbacks mVrStateCallbacks;

    class LightImpl extends Light {
        private int mBrightnessMode;
        private int mColor;
        private boolean mFlashing;
        private int mId;
        private int mLastBrightnessMode;
        private int mLastColor;
        private boolean mLocked;
        private int mMode;
        private int mOffMS;
        private int mOnMS;

        LightImpl(int id) {
            this.mId = id;
        }

        public void setBrightness(int brightness) {
            setBrightness(brightness, 0);
        }

        /* JADX WARNING: Missing block: B:48:0x00f7, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void setBrightness(int brightness, int brightnessMode) {
            synchronized (this) {
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
                    color = brightness;
                    LightsService.this.mOppoBrightUtils;
                    OppoBrightUtils.mBrightnessNoAnimation = false;
                    if (OppoBrightUtils.DEBUG) {
                        Slog.d(LightsService.TAG, "brightness color = " + brightness);
                    }
                }
                if (this.mId == 0 && this.mColor == 0 && color != 0 && !LightsService.this.mNeedReSetLight) {
                    int keyguardLayerAlpha = LightsService.this.hasKeyguard();
                    if (LightsService.this.mKeyguardAlphaInWms == OppoBrightUtils.MIN_LUX_LIMITI && keyguardLayerAlpha == 1) {
                        if (LightsService.DEBUG) {
                            Slog.v(LightsService.TAG, "setLight is delayed 32ms, because surfaceFlinger is Blocked, mKeyguardAlphaInWms = " + LightsService.this.mKeyguardAlphaInWms + ", keyguardLayerAlpha = " + keyguardLayerAlpha);
                        }
                        LightsService.this.mNeedReSetLight = true;
                        LightsService.this.mColorSkiped = color;
                        LightsService.this.mBrightnessModeSkiped = brightnessMode;
                        LightsService.this.mHandler.sendMessageDelayed(LightsService.this.mHandler.obtainMessage(1), 32);
                        return;
                    }
                } else if (this.mId == 0 && color == 0) {
                    LightsService.this.mNeedReSetLight = false;
                    if (LightsService.this.mHandler.hasMessages(1)) {
                        LightsService.this.mHandler.removeMessages(1);
                    }
                }
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
                setLightLocked(color, 0, 0, 0, brightnessMode);
                LightsService.this.mOppoBrightUtils;
                if (OppoBrightUtils.mUseAutoBrightness && this.mId == 0) {
                    LightsService.this.mOppoBrightUtils;
                    if (!OppoBrightUtils.mManualSetAutoBrightness && brightness > 0) {
                        if (LightsService.DEBUG) {
                            Slog.i(LightsService.TAG, "elevenbits setBrightness color=" + color + ", brightness=" + brightness);
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

        /* JADX WARNING: Missing block: B:13:0x0011, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void pulse(int color, int onMS) {
            synchronized (this) {
                if (this.mBrightnessMode == 2) {
                } else if (this.mColor == 0 && !this.mFlashing) {
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

        void enableLowPersistence() {
            synchronized (this) {
                setLightLocked(0, 0, 0, 0, 2);
                this.mLocked = true;
            }
        }

        void disableLowPersistence() {
            synchronized (this) {
                this.mLocked = false;
                setLightLocked(this.mLastColor, 0, 0, 0, this.mLastBrightnessMode);
            }
        }

        private void stopFlashing() {
            synchronized (this) {
                setLightLocked(this.mColor, 0, 0, 0, 0);
            }
        }

        void setLightLocked(int color, int mode, int onMS, int offMS, int brightnessMode) {
            if (!this.mLocked) {
                if (color != this.mColor || mode != this.mMode || onMS != this.mOnMS || offMS != this.mOffMS || this.mBrightnessMode != brightnessMode) {
                    if (LightsService.DEBUG) {
                        Slog.v(LightsService.TAG, "setLight #" + this.mId + ": color=#" + Integer.toHexString(color) + ": brightnessMode=" + brightnessMode);
                    }
                    if (LightsService.this.getFingerprintInternal() != null && this.mId == 0 && color != 0 && !LightsService.this.mHasNotifiedScreenOn) {
                        if (LightsService.DEBUG) {
                            Slog.v(LightsService.TAG, "onLightScreenOnFinish mHasNotifiedScreenOn = " + LightsService.this.mHasNotifiedScreenOn);
                        }
                        LightsService.this.mHasNotifiedScreenOn = true;
                        LightsService.this.getFingerprintInternal().onLightScreenOnFinish();
                    } else if (this.mId == 0 && color == 0) {
                        LightsService.this.mHasNotifiedScreenOn = false;
                    }
                    this.mLastColor = this.mColor;
                    this.mColor = color;
                    this.mMode = mode;
                    this.mOnMS = onMS;
                    this.mOffMS = offMS;
                    this.mLastBrightnessMode = this.mBrightnessMode;
                    this.mBrightnessMode = brightnessMode;
                    Trace.traceBegin(524288, "setLight(" + this.mId + ", 0x" + Integer.toHexString(color) + ")");
                    try {
                        LightsService.setLight_native(LightsService.this.mNativePointer, this.mId, color, mode, onMS, offMS, brightnessMode);
                    } finally {
                        Trace.traceEnd(524288);
                    }
                }
            }
        }
    }

    private class MyHandler extends Handler {
        /* synthetic */ MyHandler(LightsService this$0, Looper looper, MyHandler myHandler) {
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

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.lights.LightsService.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.lights.LightsService.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.lights.LightsService.<clinit>():void");
    }

    private static native void finalize_native(long j);

    private static native long init_native();

    static native void setLight_native(long j, int i, int i2, int i3, int i4, int i5, int i6);

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
                flinger.transact(10000, data, reply, 0);
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
        this.mLights = new LightImpl[8];
        this.mHasNotifiedScreenOn = false;
        this.mKeyguardAlphaInWms = 255.0f;
        this.mNeedReSetLight = false;
        this.mColorOsLowPowerModeEnabled = new AtomicBoolean(false);
        this.mMinAdjBrightness = 12;
        this.mVrStateCallbacks = new Stub() {
            public void onVrStateChanged(boolean enabled) throws RemoteException {
                LightImpl l = LightsService.this.mLights[0];
                int vrDisplayMode = LightsService.this.getVrDisplayMode();
                if (enabled && vrDisplayMode == 0) {
                    if (!LightsService.this.mVrModeEnabled) {
                        if (LightsService.DEBUG) {
                            Slog.v(LightsService.TAG, "VR mode enabled, setting brightness to low persistence");
                        }
                        l.enableLowPersistence();
                        LightsService.this.mVrModeEnabled = true;
                    }
                } else if (LightsService.this.mVrModeEnabled) {
                    if (LightsService.DEBUG) {
                        Slog.v(LightsService.TAG, "VR mode disabled, resetting brightnes");
                    }
                    l.disableLowPersistence();
                    LightsService.this.mVrModeEnabled = false;
                }
            }
        };
        this.mService = new LightsManager() {
            public Light getLight(int id) {
                if (id < 8) {
                    return LightsService.this.mLights[id];
                }
                return null;
            }

            public boolean getLightState(int id) {
                return LightsService.this.mLights[id].mColor != 0;
            }

            public void setKeyguardWindowAlpha(float alpha) {
                LightsService.this.mKeyguardAlphaInWms = alpha;
                if (LightsService.DEBUG) {
                    Slog.v(LightsService.TAG, "mKeyguardAlphaInWms = " + LightsService.this.mKeyguardAlphaInWms);
                }
            }
        };
        this.mH = new Handler() {
            public void handleMessage(Message msg) {
                msg.obj.stopFlashing();
            }
        };
        this.mContext = context;
        this.mOppoBrightUtils = OppoBrightUtils.getInstance();
        this.mNativePointer = init_native();
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
            try {
                ((IVrManager) getBinderService(VrManagerService.VR_MANAGER_BINDER_SERVICE)).registerListener(this.mVrStateCallbacks);
            } catch (RemoteException e) {
                Slog.e(TAG, "Failed to register VR mode state listener: " + e);
            }
            boolean hasFeature = this.mContext.getPackageManager().hasSystemFeature("oppo.system.cmcc.powersave");
            Slog.d(TAG, "onBootPhase: hasFeature= " + hasFeature);
            if (!hasFeature) {
                this.mPowerManagerInternal = (PowerManagerInternal) LocalServices.getService(PowerManagerInternal.class);
                this.mPowerManagerInternal.registerColorOsLowPowerModeObserver(new LowPowerModeListener() {
                    public void onLowPowerModeChanged(boolean enabled) {
                        LightsService.this.onLowPowerModeChangedInternal(enabled);
                    }
                });
                this.mColorOsLowPowerModeEnabled.set(this.mPowerManagerInternal.getColorOsLowPowerModeEnabled());
            }
        }
    }

    private void onLowPowerModeChangedInternal(boolean enabled) {
        this.mMinAdjBrightness = (SystemProperties.getInt("sys.oppo.multibrightness", 255) * 5) / 100;
        this.mColorOsLowPowerModeEnabled.set(enabled);
        setScreenBrigntness(this.mScreenBrightnessBakup);
        Slog.d(TAG, "onLowPowerModeChanged: enabled=" + enabled + ", mScreenBrightnessBakup=" + this.mScreenBrightnessBakup + ", mMinAdjBrightness=" + this.mMinAdjBrightness);
    }

    private void setScreenBrigntness(int brightness) {
        this.mLights[0].setBrightness(brightness);
    }

    private int getVrDisplayMode() {
        return Secure.getIntForUser(getContext().getContentResolver(), "vr_display_mode", 0, ActivityManager.getCurrentUser());
    }

    protected void finalize() throws Throwable {
        finalize_native(this.mNativePointer);
        super.finalize();
    }
}
