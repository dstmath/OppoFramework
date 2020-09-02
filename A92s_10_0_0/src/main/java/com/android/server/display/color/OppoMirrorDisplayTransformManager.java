package com.android.server.display.color;

import android.util.SparseArray;
import com.oppo.reflect.MethodParams;
import com.oppo.reflect.RefClass;
import com.oppo.reflect.RefObject;
import com.oppo.reflect.RefStaticMethod;

public class OppoMirrorDisplayTransformManager {
    public static Class<?> TYPE = RefClass.load(OppoMirrorDisplayTransformManager.class, DisplayTransformManager.class);
    @MethodParams({float[].class})
    public static RefStaticMethod<Void> applyColorMatrix;
    public static RefObject<SparseArray<float[]>> mColorMatrix;
}
