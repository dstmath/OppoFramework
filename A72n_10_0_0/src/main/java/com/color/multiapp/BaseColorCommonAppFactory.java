package com.color.multiapp;

import android.util.Log;

public abstract class BaseColorCommonAppFactory {
    private static final String TAG = "BaseColorCommonAppFactory";

    /* access modifiers changed from: package-private */
    public abstract IColorMultiApp getColorMultiApp();

    protected static Object newInstance(String className) throws Exception {
        return Class.forName(className).getConstructor(new Class[0]).newInstance(new Object[0]);
    }

    /* access modifiers changed from: protected */
    public void warn(String methodName) {
        Log.w(TAG, methodName);
    }
}
