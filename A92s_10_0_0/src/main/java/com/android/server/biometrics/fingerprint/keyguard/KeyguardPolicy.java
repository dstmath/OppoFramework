package com.android.server.biometrics.fingerprint.keyguard;

import android.content.Context;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import com.android.server.biometrics.fingerprint.util.LogUtil;
import com.android.server.oppo.OppoUsageService;

public class KeyguardPolicy {
    public static final String TAG = "FingerprintService.KeyguardPolicy";
    private static final int sHasKeyguardLayer = 20000;
    private static Object sMutex = new Object();
    private static final int sSetKeyguardVisibility = 20001;
    private static KeyguardPolicy sSingleInstance;
    private Context mContext;

    public KeyguardPolicy(Context context) {
        this.mContext = context;
    }

    public static KeyguardPolicy getKeyguardPolicy(Context c) {
        synchronized (sMutex) {
            if (sSingleInstance == null) {
                sSingleInstance = new KeyguardPolicy(c);
            }
        }
        return sSingleInstance;
    }

    public static KeyguardPolicy getKeyguardPolicy() {
        return sSingleInstance;
    }

    public int hasKeyguard() {
        try {
            IBinder flinger = ServiceManager.getService("SurfaceFlinger");
            if (flinger == null) {
                return -1;
            }
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            data.writeInterfaceToken("android.ui.ISurfaceComposer");
            flinger.transact(sHasKeyguardLayer, data, reply, 0);
            int result = reply.readInt();
            data.recycle();
            reply.recycle();
            return result;
        } catch (RemoteException e) {
            return -1;
        }
    }

    public int setKeyguardVisibility(boolean isVisible) {
        int ret;
        int retry = 0;
        do {
            ret = setKeyguardVisibility(isVisible ? 1 : 0);
            LogUtil.d(TAG, "setKeyguardVisibility: " + ((int) isVisible) + " ret:" + ret);
            retry++;
            if (retry >= 3) {
                break;
            }
        } while (ret != 1);
        return ret;
    }

    private int setKeyguardVisibility(int visible) {
        try {
            IBinder flinger = ServiceManager.getService("SurfaceFlinger");
            if (flinger == null) {
                return -1;
            }
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            data.writeInterfaceToken("android.ui.ISurfaceComposer");
            data.writeInt(visible);
            flinger.transact(sSetKeyguardVisibility, data, reply, 0);
            int result = reply.readInt();
            data.recycle();
            reply.recycle();
            return result;
        } catch (RemoteException e) {
            return -1;
        }
    }

    private void calculateTime(String mode, long interval) {
        LogUtil.d(TAG, "TimeConsuming, " + mode + " :" + interval);
    }

    public void forceRefresh() {
        try {
            IBinder flinger = ServiceManager.getService("SurfaceFlinger");
            if (flinger != null) {
                Parcel data = Parcel.obtain();
                Parcel reply = Parcel.obtain();
                data.writeInterfaceToken("android.ui.ISurfaceComposer");
                flinger.transact(OppoUsageService.IntergrateReserveManager.WRITE_OPPORESEVE2_TYPE_PHOENIX, data, reply, 0);
                data.recycle();
            }
        } catch (RemoteException ex) {
            LogUtil.e(TAG, "Failed to refresh surface", ex);
        }
    }
}
