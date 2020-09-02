package android.app;

import com.oppo.reflect.RefBoolean;
import com.oppo.reflect.RefClass;
import com.oppo.reflect.RefMethod;

public class OppoMirrorActivityThread {
    public static RefBoolean DEBUG_BROADCAST;
    public static RefBoolean DEBUG_BROADCAST_LIGHT;
    public static RefBoolean DEBUG_CONFIGURATION;
    public static RefBoolean DEBUG_MEMORY_TRIM;
    public static RefBoolean DEBUG_MESSAGES;
    public static RefBoolean DEBUG_PROVIDER;
    public static RefBoolean DEBUG_RESOLVER;
    public static RefBoolean DEBUG_SERVICE;
    public static Class<?> TYPE = RefClass.load(OppoMirrorActivityThread.class, ActivityThread.class);
    public static RefMethod<Boolean> inCptWhiteList;
    public static RefBoolean localLOGV;

    public static void setBooleanValue(RefBoolean refBoolean, boolean value) {
        if (refBoolean != null) {
            refBoolean.set(null, value);
        }
    }
}
