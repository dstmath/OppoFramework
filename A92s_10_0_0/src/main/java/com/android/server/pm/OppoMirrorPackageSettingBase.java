package com.android.server.pm;

import com.oppo.reflect.MethodParams;
import com.oppo.reflect.RefClass;
import com.oppo.reflect.RefMethod;

public class OppoMirrorPackageSettingBase {
    public static Class<?> TYPE = RefClass.load(OppoMirrorPackageSettingBase.class, PackageSettingBase.class);
    @MethodParams({int.class})
    public static RefMethod<Integer> getOppoFreezeFlag;
    @MethodParams({int.class})
    public static RefMethod<Integer> getOppoFreezeState;
    @MethodParams({int.class, int.class})
    public static RefMethod<Void> setOppoFreezeFlag;
    @MethodParams({int.class, int.class})
    public static RefMethod<Void> setOppoFreezeState;
}
