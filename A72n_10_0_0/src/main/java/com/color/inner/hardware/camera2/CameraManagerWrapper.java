package com.color.inner.hardware.camera2;

import android.hardware.camera2.CameraManager;
import android.os.IBinder;
import android.util.Log;

public class CameraManagerWrapper {
    private static final String TAG = "CameraManagerWrapper";

    private CameraManagerWrapper() {
    }

    public static void addAuthResultInfo(CameraManager cameraManager, int uid, int pid, int permBits, String packageName) {
        Log.i(TAG, "addAuthResultInfo.");
        try {
            cameraManager.getClass().getMethod("addAuthResultInfo", Integer.TYPE, Integer.TYPE, Integer.TYPE, String.class).invoke(cameraManager, Integer.valueOf(uid), Integer.valueOf(pid), Integer.valueOf(permBits), packageName);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
        }
    }

    public static void setDeathRecipient(CameraManager cameraManager, IBinder client) {
        Log.i(TAG, "setDeathRecipient.");
        try {
            cameraManager.getClass().getMethod("setDeathRecipient", IBinder.class).invoke(cameraManager, client);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
        }
    }
}
