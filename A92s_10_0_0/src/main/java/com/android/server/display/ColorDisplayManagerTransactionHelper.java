package com.android.server.display;

import android.common.OppoFeatureCache;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Slog;

public final class ColorDisplayManagerTransactionHelper extends ColorDisplayManagerCommonHelper {
    private static final String TAG = "ColorDisplayManagerTransactionHelper";

    public ColorDisplayManagerTransactionHelper(Context context, DisplayManagerService dms) {
        super(context, dms);
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        if (DEBUG) {
            Slog.i(TAG, "onTransact code = " + code + " flags = " + flags);
        }
        if (code != 10111) {
            Slog.i(TAG, "onTransact unknown code.");
            return false;
        }
        data.enforceInterface("android.hardware.display.IDisplayManager");
        boolean result = setStateChanged(data.readInt(), data.readBundle());
        reply.writeNoException();
        reply.writeString(String.valueOf(result));
        return true;
    }

    public boolean setStateChanged(int msgId, Bundle extraData) throws RemoteException {
        return OppoFeatureCache.get(IColorAIBrightManager.DEFAULT).setStateChanged(msgId, extraData);
    }
}
