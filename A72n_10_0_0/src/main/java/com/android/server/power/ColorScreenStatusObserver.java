package com.android.server.power;

import android.app.ActivityManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import com.android.server.pm.OppoPackageManagerHelper;
import com.color.os.IColorScreenStatusListener;

public class ColorScreenStatusObserver extends IColorScreenStatusListener.Stub {
    private final int mCallingPid = Binder.getCallingPid();
    private final IColorScreenStatusListener mListener;
    private final String mReceiver = OppoPackageManagerHelper.getProcessNameByPid(this.mCallingPid);

    public ColorScreenStatusObserver(IColorScreenStatusListener listener, ActivityManager am) {
        this.mListener = listener;
    }

    public IBinder asBinder() {
        return this.mListener.asBinder();
    }

    public void onScreenOff() throws RemoteException {
        this.mListener.onScreenOff();
    }

    public void onScreenOn() throws RemoteException {
        this.mListener.onScreenOn();
    }

    public String getCaller() {
        return this.mCallingPid + "@" + this.mReceiver;
    }
}
