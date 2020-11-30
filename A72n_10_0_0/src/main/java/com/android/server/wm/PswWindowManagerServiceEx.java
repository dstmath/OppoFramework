package com.android.server.wm;

import android.content.Context;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Slog;
import com.android.server.PswServiceRegistry;

public class PswWindowManagerServiceEx extends OppoDummyWindowManagerServiceEx implements IPswWindowManagerServiceEx {
    private static final String TAG = "PswWindowManagerServiceEx";

    public PswWindowManagerServiceEx(Context context, WindowManagerService wms) {
        super(context, wms);
        Slog.i(TAG, "create");
        init(context);
    }

    public void onStart() {
        PswWindowManagerServiceEx.super.onStart();
    }

    public void systemReady() {
        Slog.i(TAG, "systemReady");
        PswServiceRegistry.getInstance().serviceReady(25);
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        return PswWindowManagerServiceEx.super.onTransact(code, data, reply, flags);
    }

    private void init(Context context) {
        PswServiceRegistry.getInstance().serviceInit(5, this);
    }
}
