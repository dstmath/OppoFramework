package com.color.inner.hardware.camera2;

import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.params.InputConfiguration;
import android.hardware.camera2.params.OutputConfiguration;
import android.os.Handler;
import android.util.Log;
import java.util.List;

public class CameraDeviceWrapper {
    private static final String TAG = "CameraDeviceWrapper";

    private CameraDeviceWrapper() {
    }

    public static void createCustomCaptureSession(CameraDevice cameraDevice, InputConfiguration inputConfig, List<OutputConfiguration> outputs, int operatingMode, CameraCaptureSession.StateCallback callback, Handler handler) {
        try {
            cameraDevice.createCustomCaptureSession(inputConfig, outputs, operatingMode, callback, handler);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
        }
    }
}
