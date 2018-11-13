package com.android.server.audio;

import android.content.Context;
import android.hardware.display.DisplayManager;
import android.hardware.display.DisplayManager.DisplayListener;
import android.media.AudioSystem;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;
import com.android.server.am.OppoProcessManager;

class RotationHelper {
    private static final String TAG = "AudioService.RotationHelper";
    private static Context sContext;
    private static int sDeviceRotation = 0;
    private static AudioDisplayListener sDisplayListener;
    private static Handler sHandler;
    private static final Object sRotationLock = new Object();

    static final class AudioDisplayListener implements DisplayListener {
        AudioDisplayListener() {
        }

        public void onDisplayAdded(int displayId) {
        }

        public void onDisplayRemoved(int displayId) {
        }

        public void onDisplayChanged(int displayId) {
            RotationHelper.updateOrientation();
        }
    }

    RotationHelper() {
    }

    static void init(Context context, Handler handler) {
        if (context == null) {
            throw new IllegalArgumentException("Invalid null context");
        }
        sContext = context;
        sHandler = handler;
        sDisplayListener = new AudioDisplayListener();
        enable();
    }

    static void enable() {
        ((DisplayManager) sContext.getSystemService("display")).registerDisplayListener(sDisplayListener, sHandler);
        updateOrientation();
    }

    static void disable() {
        ((DisplayManager) sContext.getSystemService("display")).unregisterDisplayListener(sDisplayListener);
    }

    static void updateOrientation() {
        int newRotation = ((WindowManager) sContext.getSystemService(OppoProcessManager.RESUME_REASON_VISIBLE_WINDOW_STR)).getDefaultDisplay().getRotation();
        synchronized (sRotationLock) {
            if (newRotation != sDeviceRotation) {
                sDeviceRotation = newRotation;
                publishRotation(sDeviceRotation);
            }
        }
    }

    private static void publishRotation(int rotation) {
        Log.v(TAG, "publishing device rotation =" + rotation + " (x90deg)");
        switch (rotation) {
            case 0:
                AudioSystem.setParameters("rotation=0");
                return;
            case 1:
                AudioSystem.setParameters("rotation=90");
                return;
            case 2:
                AudioSystem.setParameters("rotation=180");
                return;
            case 3:
                AudioSystem.setParameters("rotation=270");
                return;
            default:
                Log.e(TAG, "Unknown device rotation");
                return;
        }
    }
}
