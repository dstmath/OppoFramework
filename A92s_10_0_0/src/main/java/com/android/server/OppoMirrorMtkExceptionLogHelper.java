package com.android.server;

import com.oppo.reflect.RefClass;
import com.oppo.reflect.RefMethod;

public class OppoMirrorMtkExceptionLogHelper {
    public static Class<?> TYPE = RefClass.load(OppoMirrorMtkExceptionLogHelper.class, "com.android.server.MtkExceptionLogHelper");
    public static RefMethod<Void> generateSystemCrashLog;
}
