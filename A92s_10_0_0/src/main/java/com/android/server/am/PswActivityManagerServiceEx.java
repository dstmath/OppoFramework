package com.android.server.am;

import android.content.Context;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Slog;
import com.android.server.PswServiceRegistry;

public class PswActivityManagerServiceEx extends OppoDummyActivityManagerServiceEx implements IPswActivityManagerServiceEx {
    private static final String TAG = "PswActivityManagerServiceEx";

    public PswActivityManagerServiceEx(Context context, ActivityManagerService ams) {
        super(context, ams);
        Slog.i(TAG, "create");
        init(context);
    }

    public void onStart() {
        PswActivityManagerServiceEx.super.onStart();
    }

    public void systemReady() {
        Slog.i(TAG, "systemReady");
        PswServiceRegistry.getInstance().serviceReady(22);
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        return PswActivityManagerServiceEx.super.onTransact(code, data, reply, flags);
    }

    private void init(Context context) {
        PswServiceRegistry.getInstance().serviceInit(2, this);
    }
}
