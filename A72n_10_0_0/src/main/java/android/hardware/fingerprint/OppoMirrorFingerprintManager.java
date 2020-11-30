package android.hardware.fingerprint;

import com.oppo.reflect.RefClass;
import com.oppo.reflect.RefMethod;

public class OppoMirrorFingerprintManager {
    public static Class<?> TYPE = RefClass.load(OppoMirrorFingerprintManager.class, FingerprintManager.class);
    public static RefMethod<Integer> getFailedAttempts;
    public static RefMethod<Long> getLockoutAttemptDeadline;
    public static RefMethod<Void> hideFingerprintIcon;
    public static RefMethod<Void> showFingerprintIcon;
}
