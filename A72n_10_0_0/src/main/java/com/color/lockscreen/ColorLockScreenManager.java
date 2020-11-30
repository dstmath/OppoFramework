package com.color.lockscreen;

import android.app.OppoActivityManager;
import android.os.RemoteException;
import android.util.Log;
import java.util.Map;

public class ColorLockScreenManager {
    private static final String TAG = "ColorLockScreenManager";
    private static volatile ColorLockScreenManager sInstance;
    private OppoActivityManager mOAms = new OppoActivityManager();

    public static ColorLockScreenManager getInstance() {
        if (sInstance == null) {
            synchronized (ColorLockScreenManager.class) {
                if (sInstance == null) {
                    sInstance = new ColorLockScreenManager();
                }
            }
        }
        return sInstance;
    }

    private ColorLockScreenManager() {
    }

    public void setPackagesState(Map<String, Integer> packageMap) {
        try {
            this.mOAms.setPackagesState(packageMap);
        } catch (RemoteException e) {
            Log.e(TAG, "setPackagesState remoteException ");
            e.printStackTrace();
        }
    }

    public boolean registerLockScreenCallback(IColorLockScreenCallback callback) {
        try {
            return this.mOAms.registerLockScreenCallback(callback);
        } catch (RemoteException e) {
            Log.e(TAG, "registerLockScreenCallback remoteException ");
            e.printStackTrace();
            return false;
        }
    }

    public boolean unregisterLockScreenCallback(IColorLockScreenCallback callback) {
        try {
            return this.mOAms.unregisterLockScreenCallback(callback);
        } catch (RemoteException e) {
            Log.e(TAG, "unRegisterLockScreenCallback remoteException ");
            e.printStackTrace();
            return false;
        }
    }
}
