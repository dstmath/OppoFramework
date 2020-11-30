package com.color.keylayout;

import android.app.ColorActivityTaskManager;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.Log;

public class ColorKeyLayoutManager {
    private static final boolean DEBUG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private static final String TAG = "ColorKeyLayoutManager";
    private static volatile ColorKeyLayoutManager sInstance;
    private ColorActivityTaskManager mOAms = new ColorActivityTaskManager();

    public static ColorKeyLayoutManager getInstance() {
        if (sInstance == null) {
            synchronized (ColorKeyLayoutManager.class) {
                if (sInstance == null) {
                    sInstance = new ColorKeyLayoutManager();
                }
            }
        }
        return sInstance;
    }

    private ColorKeyLayoutManager() {
    }

    public void setGimbalLaunchPkg(String pkgName) {
        if (DEBUG) {
            Log.i(TAG, "setGimbalLaunchPkg, pkgName: " + pkgName);
        }
        try {
            if (this.mOAms != null) {
                this.mOAms.setGimbalLaunchPkg(pkgName);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "setGimbalLaunchPkg remoteException " + e);
            e.printStackTrace();
        }
    }
}
