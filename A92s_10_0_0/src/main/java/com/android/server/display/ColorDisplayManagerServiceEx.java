package com.android.server.display;

import android.content.Context;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Slog;
import com.android.server.ColorServiceRegistry;

public final class ColorDisplayManagerServiceEx extends ColorDummyDisplayManagerServiceEx {
    private static final String TAG = "ColorDisplayManagerServiceEx";
    private ColorDisplayManagerTransactionHelper mTransactionHelper;

    public ColorDisplayManagerServiceEx(Context context, DisplayManagerService dms) {
        super(context, dms);
        init(context, dms);
    }

    public void systemReady() {
        Slog.i(TAG, "systemReady");
    }

    public void onStart() {
        Slog.i(TAG, "onStart");
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        ColorDisplayManagerTransactionHelper colorDisplayManagerTransactionHelper = this.mTransactionHelper;
        if (colorDisplayManagerTransactionHelper != null) {
            return colorDisplayManagerTransactionHelper.onTransact(code, data, reply, flags);
        }
        return ColorDisplayManagerServiceEx.super.onTransact(code, data, reply, flags);
    }

    private void init(Context context, DisplayManagerService dms) {
        this.mTransactionHelper = new ColorDisplayManagerTransactionHelper(context, dms);
        ColorServiceRegistry.getInstance().serviceInit(11, this);
    }
}
