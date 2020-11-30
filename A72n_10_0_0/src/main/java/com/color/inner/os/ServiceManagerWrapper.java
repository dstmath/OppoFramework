package com.color.inner.os;

import android.os.IBinder;
import android.os.ServiceManager;
import android.util.Log;

public class ServiceManagerWrapper {
    private static final String TAG = "ServiceManagerWrapper";

    public static IBinder getService(String name) {
        try {
            return ServiceManager.getService(name);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }

    public static IBinder checkService(String name) {
        try {
            return ServiceManager.checkService(name);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }

    public static void addService(String name, IBinder service) {
        ServiceManager.addService(name, service);
    }
}
