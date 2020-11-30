package com.mediatek.mmsdk;

import android.os.Handler;

public abstract class CameraEffectSession implements AutoCloseable {

    public static abstract class CaptureCallback {
        public abstract void onCaptureFailed(CameraEffectSession cameraEffectSession, BaseParameters baseParameters);

        public abstract void onCaptureSequenceAborted(CameraEffectSession cameraEffectSession, BaseParameters baseParameters);

        public abstract void onCaptureSequenceCompleted(CameraEffectSession cameraEffectSession, BaseParameters baseParameters, long j);

        public abstract void onInputFrameProcessed(CameraEffectSession cameraEffectSession, BaseParameters baseParameters, BaseParameters baseParameters2);

        public abstract void onOutputFrameProcessed(CameraEffectSession cameraEffectSession, BaseParameters baseParameters, BaseParameters baseParameters2);
    }

    public static abstract class SessionStateCallback {
        public abstract void onClosed(CameraEffectSession cameraEffectSession);

        public abstract void onConfigureFailed(CameraEffectSession cameraEffectSession);

        public abstract void onConfigured(CameraEffectSession cameraEffectSession);

        public abstract void onPrepared(CameraEffectSession cameraEffectSession);
    }

    @Override // java.lang.AutoCloseable
    public abstract void close();

    public abstract void closeSession();

    public abstract boolean getFrameSyncMode(boolean z, int i);

    public abstract void setFrameParameters(boolean z, int i, BaseParameters baseParameters, long j, boolean z2);

    public abstract int setFrameSyncMode(boolean z, int i, boolean z2);

    public abstract void startCapture(CaptureCallback captureCallback, Handler handler);

    public abstract void stopCapture(BaseParameters baseParameters);
}
