package com.android.server;

import android.os.SystemClock;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public final class ColorTraceMonitor {
    private static final String TAG = "ColorTraceMonitor";
    private static ColorFeatureStatistics sInstance = null;

    public static <T> T getTraceMonitor(T real, Class<?>[] cls, String name) {
        sInstance = ColorFeatureStatistics.getInstance();
        if (!ColorFeatureManager.isTracing(name)) {
            return real;
        }
        InvocationHandler handler = new DynamicProxy(real, name);
        return (T) Proxy.newProxyInstance(handler.getClass().getClassLoader(), cls, handler);
    }

    private static class DynamicProxy implements InvocationHandler {
        private String feature;
        private Object realObj;

        public DynamicProxy(Object object, String f) {
            this.realObj = object;
            this.feature = f;
        }

        @Override // java.lang.reflect.InvocationHandler
        public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
            ColorTraceMonitor.sInstance.startExecution(this.feature, method.getName(), SystemClock.elapsedRealtime());
            Object result = method.invoke(this.realObj, objects);
            ColorTraceMonitor.sInstance.finishExecution(this.feature, method.getName(), SystemClock.elapsedRealtime());
            return result;
        }
    }
}
