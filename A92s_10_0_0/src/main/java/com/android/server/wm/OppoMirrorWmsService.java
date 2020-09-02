package com.android.server.wm;

import com.oppo.reflect.RefBoolean;
import com.oppo.reflect.RefClass;

public class OppoMirrorWmsService {
    public static RefBoolean PROFILE_ORIENTATION;
    public static Class<?> TYPE = RefClass.load(OppoMirrorWmsService.class, WindowManagerService.class);
    public static RefBoolean localLOGV;

    public static void setBooleanValue(RefBoolean refBoolean, boolean value) {
        if (refBoolean != null) {
            refBoolean.set((Object) null, value);
        }
    }
}
