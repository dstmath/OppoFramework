package com.mediatek.matrixeffect;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Surface;
import java.lang.ref.WeakReference;

public class MatrixEffect {
    private static final int MSG_EFFECT_DONE = 100;
    private static final String TAG = "MatrixEffect_Framework";
    private static MatrixEffect sMatrixEffect;
    private EffectsCallback mEffectsListener;
    private EventHandler mEventHandler;

    public interface EffectsCallback {
        void onEffectsDone();
    }

    private native void native_displayEffect(byte[] bArr, int i);

    private native void native_initializeEffect(int i, int i2, int i3, int i4);

    private native void native_processEffect(byte[] bArr, int[] iArr);

    private native void native_registerEffectBuffers(int i, int i2, byte[][] bArr);

    private native void native_releaseEffect();

    private native void native_setSurfaceToNative(Surface surface, int i);

    private native void native_setup(Object obj);

    MatrixEffect() {
        Looper looper = Looper.myLooper();
        if (looper != null) {
            this.mEventHandler = new EventHandler(looper);
        } else {
            this.mEventHandler = new EventHandler(Looper.getMainLooper());
        }
        native_setup(new WeakReference(this));
    }

    public static MatrixEffect getInstance() {
        if (sMatrixEffect == null) {
            System.loadLibrary("jni_lomoeffect");
            sMatrixEffect = new MatrixEffect();
        }
        return sMatrixEffect;
    }

    public void setCallback(EffectsCallback listener) {
        this.mEffectsListener = listener;
    }

    public void setSurface(Surface surface, int surfaceNumber) {
        native_setSurfaceToNative(surface, surfaceNumber);
    }

    public void initialize(int previewWidth, int previewHeight, int effectNumOfPage, int format) {
        native_initializeEffect(previewWidth, previewHeight, effectNumOfPage, format);
    }

    public void setBuffers(int bufferWidth, int bufferHeight, byte[][] buffers) {
        native_registerEffectBuffers(bufferWidth, bufferHeight, buffers);
    }

    public void process(byte[] previewData, int[] effectId) {
        native_processEffect(previewData, effectId);
    }

    public void release() {
        native_releaseEffect();
    }

    private class EventHandler extends Handler {
        public EventHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            Log.i(MatrixEffect.TAG, "handleMessage:" + msg);
            if (msg.what == MatrixEffect.MSG_EFFECT_DONE && MatrixEffect.this.mEffectsListener != null) {
                MatrixEffect.this.mEffectsListener.onEffectsDone();
            }
        }
    }

    private static void postEventFromNative(Object matrixeffect_pref, int what) {
        ((MatrixEffect) ((WeakReference) matrixeffect_pref).get()).mEventHandler.obtainMessage(what).sendToTarget();
    }
}
