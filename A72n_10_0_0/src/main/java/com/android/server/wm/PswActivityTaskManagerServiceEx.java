package com.android.server.wm;

import android.content.Context;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Slog;
import com.android.server.PswServiceRegistry;

public class PswActivityTaskManagerServiceEx extends OppoDummyActivityTaskManagerServiceEx implements IPswActivityTaskManagerServiceEx {
    private static final String TAG = "PswActivityTaskManagerServiceEx";

    public PswActivityTaskManagerServiceEx(Context context, ActivityTaskManagerService atms) {
        super(context, atms);
        Slog.i(TAG, "create");
        init(context);
    }

    public void onStart() {
        PswActivityTaskManagerServiceEx.super.onStart();
    }

    public void systemReady() {
        Slog.i(TAG, "systemReady");
        PswServiceRegistry.getInstance().serviceReady(21);
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        return PswActivityTaskManagerServiceEx.super.onTransact(code, data, reply, flags);
    }

    private void init(Context context) {
        PswServiceRegistry.getInstance().serviceInit(1, this);
    }
}
