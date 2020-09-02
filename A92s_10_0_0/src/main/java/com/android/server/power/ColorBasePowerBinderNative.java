package com.android.server.power;

import android.content.Context;
import android.os.IPowerManager;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Slog;

public abstract class ColorBasePowerBinderNative extends IPowerManager.Stub {
    private static final String TAG = "ColorBasePowerBinderNative";
    protected final Context mContext;
    protected final PowerManagerService mService;

    public ColorBasePowerBinderNative(Context context, PowerManagerService service) {
        this.mContext = context;
        this.mService = service;
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        return ColorBasePowerBinderNative.super.onTransact(code, data, reply, flags);
    }

    private void warn(String methodName) {
        Slog.w(TAG, methodName + " not implemented");
    }
}
