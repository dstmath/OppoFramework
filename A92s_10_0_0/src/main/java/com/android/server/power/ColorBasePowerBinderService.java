package com.android.server.power;

import android.content.Context;
import android.os.Parcel;
import android.os.RemoteException;

public abstract class ColorBasePowerBinderService extends ColorBasePowerBinderNative {
    private ColorCommonPowerHelper mCommonHelper = null;

    public ColorBasePowerBinderService(Context context, PowerManagerService service) {
        super(context, service);
        initInterface(context);
        initFunction(context);
    }

    @Override // com.android.server.power.ColorBasePowerBinderNative
    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        if (this.mCommonHelper.onTransact(code, data, reply, flags)) {
            return true;
        }
        return super.onTransact(code, data, reply, flags);
    }

    private void initInterface(Context context) {
        this.mCommonHelper = new ColorCommonPowerHelper(context, this.mService);
    }

    private void initFunction(Context context) {
    }
}
