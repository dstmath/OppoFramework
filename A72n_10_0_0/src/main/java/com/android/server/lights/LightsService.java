package com.android.server.lights;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.Trace;
import android.provider.Settings;
import android.util.Slog;
import android.view.SurfaceControl;
import com.android.server.LocalServices;
import com.android.server.SystemService;
import com.android.server.biometrics.fingerprint.power.FingerprintInternal;
import com.android.server.lights.OppoLightsServiceUtils;

public class LightsService extends SystemService {
    static final boolean DEBUG = true;
    static final String TAG = "LightsService";
    private FingerprintInternal mFingerprintInternal;
    private OppoLightsServiceUtils.FingerprintInternalCallback mFpCallback = new OppoLightsServiceUtils.FingerprintInternalCallback() {
        /* class com.android.server.lights.LightsService.AnonymousClass3 */

        @Override // com.android.server.lights.OppoLightsServiceUtils.FingerprintInternalCallback
        public void notifyonLightScreenOnFinish() {
            if (LightsService.this.getFingerprintInternal() != null) {
                LightsService.this.getFingerprintInternal().onLightScreenOnFinish();
            }
        }
    };
    private Handler mH = new Handler() {
        /* class com.android.server.lights.LightsService.AnonymousClass2 */

        public void handleMessage(Message msg) {
            ((LightImpl) msg.obj).stopFlashing();
        }
    };
    LightImpl[] mLights = new LightImpl[8];
    private OppoLightsServiceUtils mOppoLightsServiceUtils = null;
    final LightsManager mService = new OppoLightsManager() {
        /* class com.android.server.lights.LightsService.AnonymousClass1 */

        @Override // com.android.server.lights.LightsManager
        public Light getLight(int id) {
            if (id < 0 || id >= 8) {
                return null;
            }
            return LightsService.this.mLights[id];
        }

        @Override // com.android.server.lights.OppoLightsManager, com.android.server.lights.LightsManager
        public void setKeyguardWindowAlpha(float alpha) {
            LightsService.this.mOppoLightsServiceUtils.setKeyguardWindowAlpha(alpha);
        }

        @Override // com.android.server.lights.OppoLightsManager, com.android.server.lights.LightsManager
        public boolean getLightState(int id) {
            return LightsService.this.mLights[id].mColor != 0;
        }
    };

    static native void setLight_native(int i, int i2, int i3, int i4, int i5, int i6);

    /* access modifiers changed from: package-private */
    public class LightImpl extends Light {
        private int mBrightnessMode;
        private int mColor;
        private final IBinder mDisplayToken = SurfaceControl.getInternalDisplayToken();
        private boolean mFlashing;
        private int mId;
        private boolean mInitialized;
        private int mLastBrightnessMode;
        private int mLastColor;
        private int mMode;
        private int mOffMS;
        private int mOnMS;
        private final int mSurfaceControlMaximumBrightness;
        private boolean mUseLowPersistenceForVR;
        private boolean mVrModeEnabled;

        LightImpl(Context context, int id) {
            PowerManager pm;
            this.mId = id;
            boolean brightnessSupport = SurfaceControl.getDisplayBrightnessSupport(this.mDisplayToken);
            Slog.d(LightsService.TAG, "Display brightness support: " + brightnessSupport);
            int maximumBrightness = 0;
            if (brightnessSupport && (pm = (PowerManager) context.getSystemService(PowerManager.class)) != null) {
                maximumBrightness = pm.getMaximumScreenBrightnessSetting();
            }
            this.mSurfaceControlMaximumBrightness = maximumBrightness;
        }

        @Override // com.android.server.lights.Light
        public void setBrightness(int brightness) {
            setBrightness(brightness, 0);
        }

        @Override // com.android.server.lights.Light
        public void setBrightness(int brightness, int brightnessMode) {
            synchronized (this) {
                if (brightnessMode == 2) {
                    Slog.w(LightsService.TAG, "setBrightness with LOW_PERSISTENCE unexpected #" + this.mId + ": brightness=0x" + Integer.toHexString(brightness));
                    return;
                }
                LightsService.this.onSetLight(this.mId, brightness, brightnessMode);
            }
        }

        @Override // com.android.server.lights.Light
        public void setColor(int color) {
            synchronized (this) {
                setLightLocked(color, 0, 0, 0, 0);
            }
        }

        @Override // com.android.server.lights.Light
        public void setFlashing(int color, int mode, int onMS, int offMS) {
            synchronized (this) {
                setLightLocked(color, mode, onMS, offMS, 0);
            }
        }

        @Override // com.android.server.lights.Light
        public void pulse() {
            pulse(16777215, 7);
        }

        @Override // com.android.server.lights.Light
        public void pulse(int color, int onMS) {
            synchronized (this) {
                if (this.mColor == 0 && !this.mFlashing) {
                    setLightLocked(color, 2, onMS, 1000, 0);
                    this.mColor = 0;
                    LightsService.this.mH.sendMessageDelayed(Message.obtain(LightsService.this.mH, 1, this), (long) onMS);
                }
            }
        }

        @Override // com.android.server.lights.Light
        public void turnOff() {
            synchronized (this) {
                setLightLocked(0, 0, 0, 0, 0);
            }
        }

        @Override // com.android.server.lights.Light
        public void setVrMode(boolean enabled) {
            synchronized (this) {
                if (this.mVrModeEnabled != enabled) {
                    this.mVrModeEnabled = enabled;
                    this.mUseLowPersistenceForVR = LightsService.this.getVrDisplayMode() == 0;
                    if (shouldBeInLowPersistenceMode()) {
                        this.mLastBrightnessMode = this.mBrightnessMode;
                    }
                }
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void stopFlashing() {
            synchronized (this) {
                setLightLocked(this.mColor, 0, 0, 0, 0);
            }
        }

        /* access modifiers changed from: package-private */
        public void setLightLocked(int color, int mode, int onMS, int offMS, int brightnessMode) {
            if (shouldBeInLowPersistenceMode()) {
                brightnessMode = 2;
            } else if (brightnessMode == 2) {
                brightnessMode = this.mLastBrightnessMode;
            }
            if (!this.mInitialized || color != this.mColor || mode != this.mMode || onMS != this.mOnMS || offMS != this.mOffMS || this.mBrightnessMode != brightnessMode) {
                Slog.v(LightsService.TAG, "setLight #" + this.mId + ": color=#" + Integer.toHexString(color) + ": brightnessMode=" + brightnessMode);
                LightsService.this.mOppoLightsServiceUtils.onLightScreenOnFinish(this.mId, color, LightsService.this.mFpCallback);
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
            return this.mVrModeEnabled && this.mUseLowPersistenceForVR;
        }
    }

    public LightsService(Context context) {
        super(context);
        for (int i = 0; i < 8; i++) {
            this.mLights[i] = new LightImpl(context, i);
        }
    }

    @Override // com.android.server.SystemService
    public void onStart() {
        publishLocalService(LightsManager.class, this.mService);
        this.mOppoLightsServiceUtils = new OppoLightsServiceUtils(this.mService);
    }

    @Override // com.android.server.SystemService
    public void onBootPhase(int phase) {
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getVrDisplayMode() {
        return Settings.Secure.getIntForUser(getContext().getContentResolver(), "vr_display_mode", 0, ActivityManager.getCurrentUser());
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onSetLight(int id, int brightness, int brightnessMode) {
        ((OppoLightsService) this).onSetLight(id, brightness, brightnessMode);
    }

    public FingerprintInternal getFingerprintInternal() {
        if (this.mFingerprintInternal == null) {
            this.mFingerprintInternal = (FingerprintInternal) LocalServices.getService(FingerprintInternal.class);
        }
        return this.mFingerprintInternal;
    }
}
