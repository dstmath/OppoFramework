package com.android.server.face;

import com.oppo.hypnus.HypnusManager;

public class FaceHypnus {
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
        if (sHypnus == null) {
            sHypnus = new HypnusManager();
        }
        if (sHypnus != null) {
            sHypnus.hypnusSetAction(15, timeMillis);
        }
    }
}
