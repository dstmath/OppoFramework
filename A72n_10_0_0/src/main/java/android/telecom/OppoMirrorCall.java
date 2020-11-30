package android.telecom;

import com.oppo.reflect.RefClass;
import com.oppo.reflect.RefMethod;

public class OppoMirrorCall {
    public static Class<?> TYPE = RefClass.load(OppoMirrorCall.class, Call.class);
    public static RefMethod<String> internalGetCallId;
}
