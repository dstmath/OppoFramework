package com.android.server.policy;

import com.android.server.policy.keyguard.KeyguardServiceDelegate;
import com.oppo.reflect.MethodParams;
import com.oppo.reflect.RefClass;
import com.oppo.reflect.RefMethod;

public class OppoMirrorKeyguardServiceDelegate {
    public static Class<?> TYPE = RefClass.load(OppoMirrorKeyguardServiceDelegate.class, KeyguardServiceDelegate.class);
    public static RefMethod<Void> onWakeUp;
    @MethodParams({String.class})
    public static RefMethod<Void> requestKeyguard;
}
