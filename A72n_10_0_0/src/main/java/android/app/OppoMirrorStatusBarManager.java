package android.app;

import com.oppo.reflect.MethodParams;
import com.oppo.reflect.RefClass;
import com.oppo.reflect.RefMethod;

public class OppoMirrorStatusBarManager {
    public static Class<?> TYPE = RefClass.load(OppoMirrorStatusBarManager.class, StatusBarManager.class);
    @MethodParams({int.class})
    public static RefMethod<Void> setShortcutsPanelState;
}
