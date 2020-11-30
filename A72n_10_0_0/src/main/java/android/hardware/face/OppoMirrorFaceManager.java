package android.hardware.face;

import com.oppo.reflect.RefClass;
import com.oppo.reflect.RefMethod;

public class OppoMirrorFaceManager {
    public static Class<?> TYPE = RefClass.load(OppoMirrorFaceManager.class, FaceManager.class);
    public static RefMethod<Integer> getFailedAttempts;
    public static RefMethod<Long> getLockoutAttemptDeadline;
}
