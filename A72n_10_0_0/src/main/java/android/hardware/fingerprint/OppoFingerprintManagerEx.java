package android.hardware.fingerprint;

import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;

public class OppoFingerprintManagerEx {
    private static final String TAG = "OppoFingerprintManagerEx";
    private Context mContext;
    private FingerprintManager mFingerprintManager = null;

    public static abstract class FingerprintInputCallbackEx implements FingerprintManager.FingerprintInputCallback {
    }

    public static abstract class MonitorEventCallbackEx implements FingerprintManager.MonitorEventCallback {
    }

    public OppoFingerprintManagerEx(Context context) {
        this.mContext = context;
        this.mFingerprintManager = (FingerprintManager) context.getSystemService(Context.FINGERPRINT_SERVICE);
    }

    public static class FingerprintImageInfoBase {
        public int mQuality;
        public int mScore;
        public int mType;

        public FingerprintImageInfoBase(int type, int quality, int matchScore) {
            this.mType = type;
            this.mQuality = quality;
            this.mScore = matchScore;
        }
    }

    public long getLockoutAttemptDeadline() {
        FingerprintManager fingerprintManager = this.mFingerprintManager;
        if (fingerprintManager != null) {
            return fingerprintManager.getLockoutAttemptDeadline();
        }
        return -1;
    }

    public int getFailedAttempts() {
        FingerprintManager fingerprintManager = this.mFingerprintManager;
        if (fingerprintManager != null) {
            return fingerprintManager.getFailedAttempts();
        }
        return -1;
    }

    public int getCurrentIconStatus() {
        FingerprintManager fingerprintManager = this.mFingerprintManager;
        if (fingerprintManager != null) {
            return fingerprintManager.getCurrentIconStatus();
        }
        return -1;
    }

    public byte[] alipayInvokeCommand(byte[] inbuf) {
        FingerprintManager fingerprintManager = this.mFingerprintManager;
        if (fingerprintManager != null) {
            return fingerprintManager.alipayInvokeCommand(inbuf);
        }
        return null;
    }

    public byte[] getFingerprintAuthToken() {
        FingerprintManager fingerprintManager = this.mFingerprintManager;
        if (fingerprintManager != null) {
            return fingerprintManager.getFingerprintAuthToken();
        }
        return null;
    }

    public int touchDown() {
        FingerprintManager fingerprintManager = this.mFingerprintManager;
        if (fingerprintManager != null) {
            return fingerprintManager.touchDown();
        }
        return -1;
    }

    public int touchUp() {
        FingerprintManager fingerprintManager = this.mFingerprintManager;
        if (fingerprintManager != null) {
            return fingerprintManager.touchUp();
        }
        return -1;
    }

    public int sendFingerprintCmd(int cmdId, byte[] inbuf) {
        FingerprintManager fingerprintManager = this.mFingerprintManager;
        if (fingerprintManager != null) {
            return fingerprintManager.sendFingerprintCmd(cmdId, inbuf);
        }
        return -1;
    }

    public boolean pauseEnroll() {
        FingerprintManager fingerprintManager = this.mFingerprintManager;
        if (fingerprintManager != null) {
            return fingerprintManager.pauseEnroll();
        }
        return true;
    }

    public boolean continueEnroll() {
        FingerprintManager fingerprintManager = this.mFingerprintManager;
        if (fingerprintManager != null) {
            return fingerprintManager.continueEnroll();
        }
        return true;
    }

    public int getEnrollmentTotalTimes() {
        FingerprintManager fingerprintManager = this.mFingerprintManager;
        if (fingerprintManager != null) {
            return fingerprintManager.getEnrollmentTotalTimes();
        }
        return 0;
    }

    public void pauseIdentify() {
        FingerprintManager fingerprintManager = this.mFingerprintManager;
        if (fingerprintManager != null) {
            fingerprintManager.pauseIdentify();
        }
    }

    public void continueIdentify() {
        FingerprintManager fingerprintManager = this.mFingerprintManager;
        if (fingerprintManager != null) {
            fingerprintManager.continueIdentify();
        }
    }

    public int getAlikeyStatus() {
        FingerprintManager fingerprintManager = this.mFingerprintManager;
        if (fingerprintManager == null) {
            return -1;
        }
        fingerprintManager.getAlikeyStatus();
        return -1;
    }
}
