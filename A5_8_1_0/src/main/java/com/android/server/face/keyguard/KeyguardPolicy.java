package com.android.server.face.keyguard;

import android.content.Context;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings.System;
import com.android.server.face.utils.LogUtil;

public class KeyguardPolicy {
    public static final String TAG = "FaceService.KeyguardPolicy";
    private static Object sMutex = new Object();
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

    public boolean isDefaultKeyguardInSettings() {
        String whichPkg = System.getString(this.mContext.getContentResolver(), "oppo_unlock_change_pkg");
        if (whichPkg == null || "".equals(whichPkg) || "com.android.keyguard".equals(whichPkg)) {
            return true;
        }
        return false;
    }

    public int setKeyguardVisibility(boolean isVisible) {
        int result;
        int visible = isVisible ? 1 : 0;
        int retry = 0;
        do {
            result = setKeyguardVisibility(visible);
            LogUtil.d(TAG, "setKeyguardVisibility: " + visible + " result:" + result);
            retry++;
            if (retry >= 3) {
                break;
            }
        } while (result != 1);
        return result;
    }

    private int setKeyguardVisibility(int visible) {
        try {
            IBinder flinger = ServiceManager.getService("SurfaceFlinger");
            if (flinger != null) {
                Parcel data = Parcel.obtain();
                Parcel reply = Parcel.obtain();
                data.writeInterfaceToken("android.ui.ISurfaceComposer");
                data.writeInt(visible);
                flinger.transact(20001, data, reply, 0);
                int result = reply.readInt();
                data.recycle();
                reply.recycle();
                return result;
            }
        } catch (RemoteException e) {
        }
        return -1;
    }
}
