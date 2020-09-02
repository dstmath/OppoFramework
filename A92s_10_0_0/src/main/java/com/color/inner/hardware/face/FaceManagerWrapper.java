package com.color.inner.hardware.face;

import android.content.Context;
import android.hardware.face.FaceManager;
import android.hardware.face.OppoMirrorFaceManager;

public class FaceManagerWrapper {
    public static String TAG = "FaceManagerWrapper";
    private FaceManager mFaceManager;

    private boolean checkFaceManagerNotNull(Context context) {
        if (this.mFaceManager != null) {
            return true;
        }
        this.mFaceManager = (FaceManager) context.getSystemService("face");
        if (this.mFaceManager == null) {
            return false;
        }
        return true;
    }

    public long getLockoutAttemptDeadline(Context context) {
        if (!checkFaceManagerNotNull(context) || OppoMirrorFaceManager.getLockoutAttemptDeadline == null) {
            return -1;
        }
        return ((Long) OppoMirrorFaceManager.getLockoutAttemptDeadline.call(this.mFaceManager, new Object[0])).longValue();
    }

    public int getFailedAttempts(Context context) {
        if (!checkFaceManagerNotNull(context) || OppoMirrorFaceManager.getFailedAttempts == null) {
            return -1;
        }
        return ((Integer) OppoMirrorFaceManager.getFailedAttempts.call(this.mFaceManager, new Object[0])).intValue();
    }
}
