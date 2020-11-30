package android.location;

import com.oppo.reflect.MethodParams;
import com.oppo.reflect.RefClass;
import com.oppo.reflect.RefMethod;

public class OppoMirrorLocationManager {
    public static Class<?> TYPE = RefClass.load(OppoMirrorLocationManager.class, LocationManager.class);
    @MethodParams({int.class, LocAppsOp.class})
    public static RefMethod<Void> getLocAppsOp;
    @MethodParams({int.class, LocAppsOp.class})
    public static RefMethod<Void> setLocAppsOp;
}
