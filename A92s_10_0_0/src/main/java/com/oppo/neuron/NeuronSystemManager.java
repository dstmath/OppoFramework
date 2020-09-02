package com.oppo.neuron;

import android.content.ContentValues;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.util.Slog;
import com.oppo.neuron.INeuronSystemService;
import java.util.List;

public final class NeuronSystemManager {
    public static final int DEFAULT_PROP = 25;
    public static final boolean LOG_ON = SystemProperties.getBoolean("persist.sys.ns_logon", false);
    public static final int NS_APP_PRELOAD = 16;
    public static final int NS_EVENT_PUBLISH = 8;
    public static final int NS_ON = 1;
    public static final int NS_UPLOAD_DB = 4;
    public static final int NS_WRITE_DB = 2;
    private static final String TAG = "NeuronSystem";
    private static NeuronSystemManager sNeuronSystemManager = null;
    public static int sNsProp = SystemProperties.getInt("persist.sys.neuron_system", 25);
    private INeuronSystemService mService;

    private NeuronSystemManager() {
        this.mService = null;
        this.mService = INeuronSystemService.Stub.asInterface(ServiceManager.getService("neuronsystem"));
        if (this.mService == null) {
            Slog.d(TAG, "can not get service neuronsystem");
        }
    }

    public static NeuronSystemManager getInstance() {
        if (sNeuronSystemManager == null) {
            synchronized (NeuronSystemManager.class) {
                if (sNeuronSystemManager == null) {
                    sNeuronSystemManager = new NeuronSystemManager();
                }
            }
        }
        return sNeuronSystemManager;
    }

    public static boolean isEnable() {
        return (sNsProp & 1) != 0;
    }

    public void publishEvent(int type, ContentValues contentValues) {
        INeuronSystemService iNeuronSystemService = this.mService;
        if (iNeuronSystemService != null) {
            try {
                iNeuronSystemService.publishEvent(type, contentValues);
            } catch (Exception e) {
                Slog.d(TAG, "NeuronSystemManager publishEvent err: " + e);
            }
        }
    }

    public List<String> getRecommendedApps(int topK) {
        INeuronSystemService iNeuronSystemService = this.mService;
        if (iNeuronSystemService == null) {
            Slog.d(TAG, "NeuronSystemManager getRecommendedApps can not get service neuronsystem");
            return null;
        }
        try {
            return iNeuronSystemService.getRecommendedApps(topK);
        } catch (RemoteException e) {
            Slog.e(TAG, "Exception happend while getRecommendedApps", e);
            return null;
        }
    }
}
