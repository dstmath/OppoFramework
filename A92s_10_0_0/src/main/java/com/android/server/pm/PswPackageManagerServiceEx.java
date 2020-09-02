package com.android.server.pm;

import android.content.Context;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Slog;
import com.android.server.PswServiceRegistry;

public class PswPackageManagerServiceEx extends OppoDummyPackageManagerServiceEx implements IPswPackageManagerServiceEx {
    private static final String TAG = "PswPackageManagerServiceEx";

    public PswPackageManagerServiceEx(Context context, PackageManagerService pms) {
        super(context, pms);
        Slog.i(TAG, "create");
        init(context);
    }

    public void onStart() {
        PswPackageManagerServiceEx.super.onStart();
    }

    public void systemReady() {
        Slog.i(TAG, "systemReady");
        PswServiceRegistry.getInstance().serviceReady(24);
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        return PswPackageManagerServiceEx.super.onTransact(code, data, reply, flags);
    }

    private void init(Context context) {
        PswServiceRegistry.getInstance().serviceInit(4, this);
    }
}
