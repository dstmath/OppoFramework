package com.android.internal.telephony.common;

import android.content.Context;
import com.android.internal.telephony.common.OppoFeatureList;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;

public class OppoFeatureManager {
    private static final String TAG = "OppoFeatureManager";
    private static final boolean[] mFeatureDisable = new boolean[OppoFeatureList.OppoIndex.End.ordinal()];
    private static final boolean[] mFeatureTraing = new boolean[OppoFeatureList.OppoIndex.End.ordinal()];
    private static final HashMap<String, Boolean> sFeatureSwitchMap = new HashMap<>();
    private static final HashMap<String, Boolean> sFeatureTraceMap = new HashMap<>();
    private static OppoFeatureManager sInstance = null;

    public OppoFeatureManager getInstance() {
        if (sInstance == null) {
            synchronized (OppoFeatureManager.class) {
                if (sInstance == null) {
                    sInstance = new OppoFeatureManager();
                }
            }
        }
        return sInstance;
    }

    public static void init(Context context) {
    }

    public static <T extends IOppoCommonFeature> boolean isSupport(T def) {
        int index = getIndex(def);
        boolean disable = mFeatureDisable[index];
        if (disable) {
            synchronized (def.getDefault()) {
                disable = mFeatureDisable[index];
            }
        }
        return !disable;
    }

    public static <T extends IOppoCommonFeature> boolean isTracing(T def) {
        int index = getIndex(def);
        boolean enable = mFeatureTraing[index];
        if (enable) {
            synchronized (def.getDefault()) {
                enable = mFeatureTraing[index];
            }
        }
        return enable;
    }

    public static boolean isSupport(String name) {
        synchronized (sFeatureSwitchMap) {
        }
        return true;
    }

    public static boolean isTracing(String name) {
        synchronized (sFeatureTraceMap) {
        }
        return false;
    }

    public static <T extends IOppoCommonFeature> T getTraceMonitor(T real) {
        if (real != null) {
            OppoFeatureList.OppoIndex index = real.index();
            if (!isTracing(real)) {
                return real;
            }
            InvocationHandler handler = new DynamicProxy(real, index.name());
            return (T) ((IOppoCommonFeature) Proxy.newProxyInstance(handler.getClass().getClassLoader(), new Class[]{(Class) real.getDefault().getClass().getGenericInterfaces()[0]}, handler));
        }
        throw new IllegalArgumentException("params must not be null");
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
            return method.invoke(this.realObj, objects);
        }
    }

    private static <T extends IOppoCommonFeature> int getIndex(T service) {
        if (service != null) {
            int index = service.index().ordinal();
            if (index < mFeatureDisable.length) {
                return index;
            }
            throw new IllegalAccessError("index = " + index + " size = " + mFeatureDisable.length);
        }
        throw new IllegalArgumentException("dummy must not be null");
    }
}
