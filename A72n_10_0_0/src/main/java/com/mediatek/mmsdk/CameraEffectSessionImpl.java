package com.mediatek.mmsdk;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.android.internal.util.Preconditions;
import com.mediatek.mmsdk.CameraEffectImpl;
import com.mediatek.mmsdk.CameraEffectSession;

public class CameraEffectSessionImpl extends CameraEffectSession {
    private static final String TAG = "CameraEffectSessionImpl";
    private static final boolean VERBOSE = true;
    private volatile boolean mAborting;
    private CameraEffectImpl mCameraMmEffectImpl;
    private boolean mClosed = false;
    private final Runnable mConfiguredFailRunnable = new Runnable() {
        /* class com.mediatek.mmsdk.CameraEffectSessionImpl.AnonymousClass4 */

        public void run() {
            Log.e(CameraEffectSessionImpl.TAG, "[mConfiguredFailRunnable]Failed to create capture session: configuration failed");
            CameraEffectSessionImpl.this.mStateCallback.onConfigureFailed(CameraEffectSessionImpl.this);
        }
    };
    private final Runnable mConfiguredRunnable = new Runnable() {
        /* class com.mediatek.mmsdk.CameraEffectSessionImpl.AnonymousClass3 */

        public void run() {
            Log.v(CameraEffectSessionImpl.TAG, "[mConfiguredRunnable] Created session successfully");
            CameraEffectSessionImpl.this.mStateCallback.onConfigured(CameraEffectSessionImpl.this);
        }
    };
    private final Handler mDeviceHandler;
    private final CameraEffectSession.SessionStateCallback mStateCallback;
    private final Handler mStateHandler;

    public CameraEffectSessionImpl(CameraEffectSession.SessionStateCallback callback, Handler effectStateHandler, CameraEffectImpl effectImpl, Handler deviceStateHandler, boolean configureSuccess) {
        Log.i(TAG, "[CameraEffectHalSessionImpl]++++ configureSuccess = " + configureSuccess);
        if (callback != null) {
            this.mStateCallback = callback;
            this.mStateHandler = checkHandler(effectStateHandler);
            this.mDeviceHandler = (Handler) Preconditions.checkNotNull(deviceStateHandler, "deviceStateHandler must not be null");
            this.mCameraMmEffectImpl = (CameraEffectImpl) Preconditions.checkNotNull(effectImpl, "deviceImpl must not be null");
            if (configureSuccess) {
                this.mStateHandler.post(this.mConfiguredRunnable);
            } else {
                this.mStateHandler.post(this.mConfiguredFailRunnable);
            }
        } else {
            throw new IllegalArgumentException("callback must not be null");
        }
    }

    @Override // com.mediatek.mmsdk.CameraEffectSession
    public void startCapture(CameraEffectSession.CaptureCallback callback, Handler handler) {
        Log.v(TAG, "[" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]++++ callback " + callback + " handler " + handler);
        checkNotClosed();
        this.mCameraMmEffectImpl.startEffectHal(this.mDeviceHandler, createCaptureCallback(checkHandler(handler, callback), callback));
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append(Thread.currentThread().getStackTrace()[2].getMethodName());
        sb.append("]----");
        Log.v(TAG, sb.toString());
    }

    @Override // com.mediatek.mmsdk.CameraEffectSession
    public void setFrameParameters(boolean isInput, int index, BaseParameters baseParameters, long timestamp, boolean repeating) {
        if (baseParameters != null) {
            this.mCameraMmEffectImpl.setFrameParameters(isInput, index, baseParameters, timestamp, repeating);
            return;
        }
        throw new IllegalArgumentException("[addInputFrameParameter] parameters is null");
    }

    @Override // com.mediatek.mmsdk.CameraEffectSession
    public int setFrameSyncMode(boolean isInput, int index, boolean sync) {
        int status_t = this.mCameraMmEffectImpl.setFrameSyncMode(isInput, index, sync);
        Log.i(TAG, "[setInputsyncMode] status_t = " + status_t);
        return status_t;
    }

    @Override // com.mediatek.mmsdk.CameraEffectSession
    public boolean getFrameSyncMode(boolean isInputSync, int index) {
        boolean value = this.mCameraMmEffectImpl.getFrameSyncMode(isInputSync, index);
        Log.i(TAG, "[getInputsyncMode] value = " + value);
        return value;
    }

    @Override // com.mediatek.mmsdk.CameraEffectSession
    public void stopCapture(BaseParameters baseParameters) {
        Log.v(TAG, "[abort]baseParameters " + baseParameters);
        this.mCameraMmEffectImpl.abortCapture(baseParameters);
    }

    @Override // com.mediatek.mmsdk.CameraEffectSession
    public void closeSession() {
        this.mCameraMmEffectImpl.abortCapture(null);
    }

    @Override // com.mediatek.mmsdk.CameraEffectSession, java.lang.AutoCloseable
    public void close() {
        if (this.mClosed) {
            Log.i(TAG, "[close],current session is closed,so return");
            return;
        }
        Log.i(TAG, "[close] on going");
        this.mClosed = VERBOSE;
    }

    /* access modifiers changed from: package-private */
    public void replaceSessionClose() {
        Log.i(TAG, "[replaceSessionClose]");
        close();
    }

    private void checkNotClosed() {
        if (this.mClosed) {
            throw new IllegalStateException("Session has been closed; further changes are illegal.");
        }
    }

    private static <T> Handler checkHandler(Handler handler, T callback) {
        if (callback != null) {
            return checkHandler(handler);
        }
        return handler;
    }

    private static Handler checkHandler(Handler handler) {
        if (handler != null) {
            return handler;
        }
        Looper looper = Looper.myLooper();
        if (looper != null) {
            return new Handler(looper);
        }
        throw new IllegalArgumentException("No handler given, and current thread has no looper!");
    }

    /* access modifiers changed from: package-private */
    public CameraEffectImpl.DeviceStateCallback getDeviceStateCallback() {
        return new CameraEffectImpl.DeviceStateCallback() {
            /* class com.mediatek.mmsdk.CameraEffectSessionImpl.AnonymousClass1 */
            private boolean mActive = false;
            private boolean mBusy = false;

            @Override // com.mediatek.mmsdk.CameraEffect.StateCallback
            public void onDisconnected(CameraEffect effect) {
                Log.v(CameraEffectSessionImpl.TAG, "[" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                CameraEffectSessionImpl.this.close();
            }

            @Override // com.mediatek.mmsdk.CameraEffectImpl.DeviceStateCallback
            public void onUnconfigured(CameraEffect effect) {
                Log.v(CameraEffectSessionImpl.TAG, "[" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
            }

            @Override // com.mediatek.mmsdk.CameraEffectImpl.DeviceStateCallback
            public void onActive(CameraEffect effect) {
                this.mActive = CameraEffectSessionImpl.VERBOSE;
                Log.v(CameraEffectSessionImpl.TAG, "[" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
            }

            @Override // com.mediatek.mmsdk.CameraEffectImpl.DeviceStateCallback
            public void onBusy(CameraEffect effect) {
                this.mBusy = CameraEffectSessionImpl.VERBOSE;
                Log.v(CameraEffectSessionImpl.TAG, "[" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
            }

            @Override // com.mediatek.mmsdk.CameraEffectImpl.DeviceStateCallback
            public void onIdle(CameraEffect effect) {
                boolean isAborting;
                Log.v(CameraEffectSessionImpl.TAG, "[" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                synchronized (this) {
                    isAborting = CameraEffectSessionImpl.this.mAborting;
                }
                if (this.mBusy && isAborting) {
                    CameraEffectSessionImpl.this.mAborting = false;
                }
                this.mBusy = false;
                this.mActive = false;
            }

            @Override // com.mediatek.mmsdk.CameraEffect.StateCallback
            public void onError(CameraEffect effect, int error) {
                Log.wtf(CameraEffectSessionImpl.TAG, "Got device error " + error);
            }
        };
    }

    private CameraEffectImpl.CaptureCallback createCaptureCallback(Handler handler, final CameraEffectSession.CaptureCallback callback) {
        return new CameraEffectImpl.CaptureCallback() {
            /* class com.mediatek.mmsdk.CameraEffectSessionImpl.AnonymousClass2 */

            @Override // com.mediatek.mmsdk.CameraEffectImpl.CaptureCallback
            public void onInputFrameProcessed(CameraEffectSession session, BaseParameters parameter, BaseParameters partialResult) {
                Log.i(CameraEffectSessionImpl.TAG, "[" + Thread.currentThread().getStackTrace()[2].getMethodName() + "],callback = " + callback);
                callback.onInputFrameProcessed(session, parameter, partialResult);
            }

            @Override // com.mediatek.mmsdk.CameraEffectImpl.CaptureCallback
            public void onOutputFrameProcessed(CameraEffectSession session, BaseParameters parameter, BaseParameters partialResult) {
                Log.i(CameraEffectSessionImpl.TAG, "[" + Thread.currentThread().getStackTrace()[2].getMethodName() + "],callback = " + callback);
                callback.onOutputFrameProcessed(session, parameter, partialResult);
            }

            @Override // com.mediatek.mmsdk.CameraEffectImpl.CaptureCallback
            public void onCaptureSequenceCompleted(CameraEffectSession session, BaseParameters result, long uid) {
                Log.i(CameraEffectSessionImpl.TAG, "[" + Thread.currentThread().getStackTrace()[2].getMethodName() + "],callback = " + callback);
                callback.onCaptureSequenceCompleted(session, result, uid);
            }

            @Override // com.mediatek.mmsdk.CameraEffectImpl.CaptureCallback
            public void onCaptureSequenceAborted(CameraEffectSession session, BaseParameters result) {
                Log.i(CameraEffectSessionImpl.TAG, "[" + Thread.currentThread().getStackTrace()[2].getMethodName() + "],callback = " + callback);
                callback.onCaptureSequenceAborted(session, result);
            }

            @Override // com.mediatek.mmsdk.CameraEffectImpl.CaptureCallback
            public void onCaptureFailed(CameraEffectSession session, BaseParameters result) {
                Log.i(CameraEffectSessionImpl.TAG, "[" + Thread.currentThread().getStackTrace()[2].getMethodName() + "],callback = " + callback);
                callback.onCaptureFailed(session, result);
            }
        };
    }
}
