package com.android.server.biometrics.face;

import android.os.SystemProperties;
import com.android.server.biometrics.face.utils.LogUtil;
import com.oppo.hypnus.HypnusManager;

public class FaceHypnus {
    private static final String PROP_FACE_PERFORMANCE_HIDE = "persist.sys.face.performance.hide";
    private static final String TAG = "FaceService.FaceHypnus";
    private static HypnusManager sHypnus = null;
    private static FaceHypnus sInstance;
    private static final Object sInstanceLock = new Object();

    public static FaceHypnus getInstance() {
        synchronized (sInstanceLock) {
            if (sInstance == null) {
                sInstance = new FaceHypnus();
            }
        }
        return sInstance;
    }

    private FaceHypnus() {
    }

    public void hypnusSpeedUp(int timeMillis) {
        if ("hypnus".equals(SystemProperties.get(PROP_FACE_PERFORMANCE_HIDE, ""))) {
            LogUtil.d(TAG, "PROP_FACE_PERFORMANCE_HIDE = " + SystemProperties.get(PROP_FACE_PERFORMANCE_HIDE, ""));
            return;
        }
        if (sHypnus == null) {
            sHypnus = new HypnusManager();
        }
        HypnusManager hypnusManager = sHypnus;
        if (hypnusManager != null) {
            hypnusManager.hypnusSetAction(15, timeMillis);
        }
    }
}
