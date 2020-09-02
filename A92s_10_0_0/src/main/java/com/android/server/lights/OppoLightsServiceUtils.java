package com.android.server.lights;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.util.Slog;

public class OppoLightsServiceUtils {
    /* access modifiers changed from: private */
    public static boolean DEBUG = false;
    public static final int MSG_DELAY_SETLIGHT = 1;
    public static final int SETLIGHT_WAITING_TIME = 32;
    static final String TAG = "OppoLightsServiceUtils";
    /* access modifiers changed from: private */
    public int mBrightnessModeSkiped;
    /* access modifiers changed from: private */
    public int mColorSkiped;
    private MyHandler mHandler;
    private HandlerThread mHandlerThread;
    private boolean mHasNotifiedScreenOn = false;
    private float mKeyguardAlphaInWms = 255.0f;
    public LightsManager mLightsService = null;
    private Looper mLooper;
    private boolean mNeedReSetLight = false;

    public interface FingerprintInternalCallback {
        void notifyonLightScreenOnFinish();
    }

    public OppoLightsServiceUtils(LightsManager mService) {
        this.mLightsService = mService;
        this.mHandlerThread = new HandlerThread("LightService thread");
        this.mHandlerThread.start();
        this.mLooper = this.mHandlerThread.getLooper();
        if (this.mLooper == null) {
            Slog.e(TAG, "mLooper null");
        }
        DEBUG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
        Looper looper = this.mLooper;
        if (looper != null) {
            this.mHandler = new MyHandler(looper);
        }
    }

    public int hasKeyguard() {
        try {
            IBinder flinger = ServiceManager.getService("SurfaceFlinger");
            if (flinger == null) {
                return -1;
            }
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            data.writeInterfaceToken("android.ui.ISurfaceComposer");
            flinger.transact(20000, data, reply, 0);
            int result = reply.readInt();
            data.recycle();
            reply.recycle();
            return result;
        } catch (RemoteException e) {
            return -1;
        }
    }

    public void onLightScreenOnFinish(int mId, int color, FingerprintInternalCallback callback) {
        if (callback != null && mId == 0 && color != 0 && !this.mHasNotifiedScreenOn) {
            if (DEBUG) {
                Slog.v(TAG, "onLightScreenOnFinish color=#" + Integer.toHexString(color) + " mHasNotifiedScreenOn = " + this.mHasNotifiedScreenOn);
            }
            this.mHasNotifiedScreenOn = true;
            callback.notifyonLightScreenOnFinish();
        } else if (mId == 0 && color == 0) {
            this.mHasNotifiedScreenOn = false;
        }
    }

    public void setKeyguardWindowAlpha(float alpha) {
        this.mKeyguardAlphaInWms = alpha;
        if (DEBUG) {
            Slog.v(TAG, "mKeyguardAlphaInWms = " + this.mKeyguardAlphaInWms);
        }
    }

    private class MyHandler extends Handler {
        private MyHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (msg.what != 1) {
                Slog.w(OppoLightsServiceUtils.TAG, "Unknown message:" + msg.what);
                return;
            }
            if (OppoLightsServiceUtils.DEBUG) {
                Slog.v(OppoLightsServiceUtils.TAG, "setLight is delayed 32ms because surfaceFlinger is Blocked end");
            }
            if (OppoLightsServiceUtils.this.mLightsService != null) {
                OppoLightsServiceUtils.this.mLightsService.getLight(0).setBrightness(OppoLightsServiceUtils.this.mColorSkiped, OppoLightsServiceUtils.this.mBrightnessModeSkiped);
            }
        }
    }
}
