package com.color.util;

import android.app.OppoActivityManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Log;

public class ColorCommonConfig {
    private static final String TAG = "ColorCommonConfig";
    public static final int TO_AMS = 1;
    public static final int TO_PMS = 2;
    private static volatile ColorCommonConfig sConfig = null;
    private OppoActivityManager mOppoAm;

    private ColorCommonConfig() {
        this.mOppoAm = null;
        this.mOppoAm = new OppoActivityManager();
    }

    public static ColorCommonConfig getInstance() {
        if (sConfig == null) {
            synchronized (ColorCommonConfig.class) {
                if (sConfig == null) {
                    sConfig = new ColorCommonConfig();
                }
            }
        }
        return sConfig;
    }

    public boolean putConfigInfo(String configName, Bundle bundle, int flag) {
        if (this.mOppoAm == null) {
            this.mOppoAm = new OppoActivityManager();
        }
        OppoActivityManager oppoActivityManager = this.mOppoAm;
        if (oppoActivityManager == null) {
            return false;
        }
        try {
            return oppoActivityManager.putConfigInfo(configName, bundle, flag, UserHandle.myUserId());
        } catch (RemoteException e) {
            Log.e(TAG, "putConfigInfo " + configName + " failed!");
            return false;
        }
    }

    public boolean putConfigInfoAsUser(String configName, Bundle bundle, int flag, int userId) {
        if (this.mOppoAm == null) {
            this.mOppoAm = new OppoActivityManager();
        }
        OppoActivityManager oppoActivityManager = this.mOppoAm;
        if (oppoActivityManager == null) {
            return false;
        }
        try {
            return oppoActivityManager.putConfigInfo(configName, bundle, flag, userId);
        } catch (RemoteException e) {
            Log.e(TAG, "putConfigInfoAsUser " + configName + " failed!");
            return false;
        }
    }

    public Bundle getConfigInfo(String configName, int flag) {
        if (this.mOppoAm == null) {
            this.mOppoAm = new OppoActivityManager();
        }
        OppoActivityManager oppoActivityManager = this.mOppoAm;
        if (oppoActivityManager == null) {
            return null;
        }
        try {
            return oppoActivityManager.getConfigInfo(configName, flag, UserHandle.myUserId());
        } catch (RemoteException e) {
            Log.e(TAG, "getConfigInfo " + configName + " failed!");
            return null;
        }
    }

    public Bundle getConfigInfoAsUser(String configName, int flag, int userId) {
        if (this.mOppoAm == null) {
            this.mOppoAm = new OppoActivityManager();
        }
        OppoActivityManager oppoActivityManager = this.mOppoAm;
        if (oppoActivityManager == null) {
            return null;
        }
        try {
            return oppoActivityManager.getConfigInfo(configName, flag, userId);
        } catch (RemoteException e) {
            Log.e(TAG, "getConfigInfoAsUser " + configName + " failed!");
            return null;
        }
    }
}
