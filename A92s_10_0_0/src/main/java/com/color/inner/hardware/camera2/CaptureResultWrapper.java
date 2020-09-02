package com.color.inner.hardware.camera2;

import android.hardware.camera2.CaptureResult;
import android.util.Log;

public class CaptureResultWrapper {
    private static final String TAG = "CaptureResultWrapper";

    private CaptureResultWrapper() {
    }

    public static <T> CaptureResult.Key<T> captureResultKey(String name, Class<T> type, long vendorId) {
        try {
            return new CaptureResult.Key<>(name, type, vendorId);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }

    public static <T> CaptureResult.Key<T> captureResultKey(String name, String fallbackName, Class<T> type) {
        try {
            return new CaptureResult.Key<>(name, fallbackName, type);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }

    public static <T> CaptureResult.Key<T> captureResultKey(String name, Class<T> type) {
        try {
            return new CaptureResult.Key<>(name, type);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }
}
