package com.mediatek.mmsdk;

import android.os.Handler;
import android.view.Surface;
import com.mediatek.mmsdk.CameraEffectSession;
import java.util.List;

public abstract class CameraEffect implements AutoCloseable {
    @Override // java.lang.AutoCloseable
    public abstract void close();

    public abstract void closeEffect();

    public abstract CameraEffectSession createCaptureSession(List<Surface> list, List<BaseParameters> list2, CameraEffectSession.SessionStateCallback sessionStateCallback, Handler handler) throws CameraEffectHalException;

    public abstract List<BaseParameters> getCaputreRequirement(BaseParameters baseParameters);

    public abstract List<Surface> getInputSurface();

    public abstract void setParamters(BaseParameters baseParameters);

    public static abstract class StateCallback {
        public static final int ERROR_EFFECT_DEVICE = 4;
        public static final int ERROR_EFFECT_DISABLED = 3;
        public static final int ERROR_EFFECT_HAL_IN_USE = 1;
        public static final int ERROR_EFFECT_LISTENER = 6;

        public abstract void onDisconnected(CameraEffect cameraEffect);

        public abstract void onError(CameraEffect cameraEffect, int i);

        public void onClosed(CameraEffect effect) {
        }
    }
}
