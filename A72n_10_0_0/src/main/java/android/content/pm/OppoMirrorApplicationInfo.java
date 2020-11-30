package android.content.pm;

import com.oppo.reflect.RefClass;
import com.oppo.reflect.RefInt;
import com.oppo.reflect.RefObject;

public class OppoMirrorApplicationInfo {
    public static Class<?> TYPE = RefClass.load(OppoMirrorApplicationInfo.class, ApplicationInfo.class);
    public static RefObject<OppoApplicationInfoEx> mOppoApplicationInfoEx;
    public static RefInt oppoPrivateFlags;
}
