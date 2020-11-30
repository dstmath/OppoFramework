package com.mediatek.dm;

import android.content.Context;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import com.mediatek.dm.IDmService;

public final class DmManager {
    private static final String TAG = "DmManager";
    private static DmManager mDmManager = null;
    private static IDmService mService;
    private final Context mContext;

    public static DmManager getDefaultDmManager(Context context) {
        if (context != null) {
            synchronized (DmManager.class) {
                if (mDmManager == null) {
                    IBinder b = ServiceManager.getService("GbaDmService");
                    if (b == null) {
                        Log.i("debug", "[getDefaultDmManager]The binder is null");
                        return null;
                    }
                    mService = IDmService.Stub.asInterface(b);
                    mDmManager = new DmManager(context);
                }
                return mDmManager;
            }
        }
        throw new IllegalArgumentException("context cannot be null");
    }

    DmManager(Context context) {
        this.mContext = context;
    }

    public int getDmSupported() {
        try {
            return mService.getDmSupported();
        } catch (RemoteException e) {
            return 0;
        }
    }

    public boolean getImcProvision(int phoneId, int feature) {
        Log.d(TAG, "DmManager getImcProvision for feature=" + feature);
        try {
            return mService.getImcProvision(phoneId, feature);
        } catch (RemoteException e) {
            return true;
        }
    }

    public boolean setImcProvision(int phoneId, int feature, int pvs_en) {
        Log.d(TAG, "DmManager setImcProvision for feature=" + feature + ", en = " + pvs_en);
        try {
            return mService.setImcProvision(phoneId, feature, pvs_en);
        } catch (RemoteException e) {
            return false;
        }
    }
}
