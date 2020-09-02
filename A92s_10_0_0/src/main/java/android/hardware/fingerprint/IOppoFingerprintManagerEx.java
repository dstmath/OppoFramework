package android.hardware.fingerprint;

public interface IOppoFingerprintManagerEx {

    public static abstract class OpticalFingerprintListener {
        public void onOpticalFingerprintUpdate(int status) {
        }
    }

    default int regsiterOpticalFingerprintListener(OpticalFingerprintListener listener) {
        return -1;
    }

    default void showFingerprintIcon() {
    }

    default long getLockoutAttemptDeadline() {
        return -1;
    }

    default void hideFingerprintIcon() {
    }

    default int getFailedAttempts() {
        return -1;
    }
}
