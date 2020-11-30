package com.color.inner.hardware.fingerprint;

import android.hardware.fingerprint.FingerprintManager;
import android.hardware.fingerprint.IOppoFingerprintManagerEx;
import android.hardware.fingerprint.OppoMirrorFingerprintManager;
import android.util.Log;
import com.color.util.ColorTypeCastingHelper;

public class FingerprintManagerWrapper {
    private static final String TAG = "FingerprintManagerWrapper";

    public interface OpticalFingerprintListenerCallbackWrapper {
        void onOpticalFingerprintUpdate(int i);
    }

    private FingerprintManagerWrapper() {
    }

    public static void regsiterOpticalFingerprintListener(FingerprintManager fp, final OpticalFingerprintListenerCallbackWrapper callback) {
        try {
            IOppoFingerprintManagerEx oppoFingerprintManagerEx = typeCasting(fp);
            if (callback != null && oppoFingerprintManagerEx != null) {
                oppoFingerprintManagerEx.regsiterOpticalFingerprintListener(new IOppoFingerprintManagerEx.OpticalFingerprintListener() {
                    /* class com.color.inner.hardware.fingerprint.FingerprintManagerWrapper.AnonymousClass1 */

                    public void onOpticalFingerprintUpdate(int status) {
                        OpticalFingerprintListenerCallbackWrapper.this.onOpticalFingerprintUpdate(status);
                    }
                });
            }
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
        }
    }

    public static void showFingerprintIcon(FingerprintManager fp) {
        if (OppoMirrorFingerprintManager.showFingerprintIcon != null) {
            OppoMirrorFingerprintManager.showFingerprintIcon.call(fp, new Object[0]);
        }
    }

    public static void hideFingerprintIcon(FingerprintManager fp) {
        if (OppoMirrorFingerprintManager.hideFingerprintIcon != null) {
            OppoMirrorFingerprintManager.hideFingerprintIcon.call(fp, new Object[0]);
        }
    }

    public static int getFailedAttempts(FingerprintManager fp) {
        if (OppoMirrorFingerprintManager.getFailedAttempts != null) {
            return ((Integer) OppoMirrorFingerprintManager.getFailedAttempts.call(fp, new Object[0])).intValue();
        }
        return -1;
    }

    public static long getLockoutAttemptDeadline(FingerprintManager fp) {
        if (OppoMirrorFingerprintManager.getLockoutAttemptDeadline != null) {
            return ((Long) OppoMirrorFingerprintManager.getLockoutAttemptDeadline.call(fp, new Object[0])).longValue();
        }
        return -1;
    }

    private static IOppoFingerprintManagerEx typeCasting(FingerprintManager fp) {
        return (IOppoFingerprintManagerEx) ColorTypeCastingHelper.typeCasting(IOppoFingerprintManagerEx.class, fp);
    }
}
