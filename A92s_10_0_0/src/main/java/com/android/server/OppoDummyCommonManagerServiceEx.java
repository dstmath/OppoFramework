package com.android.server;

import android.content.Context;
import android.os.Parcel;
import android.os.RemoteException;

public class OppoDummyCommonManagerServiceEx implements IOppoCommonManagerServiceEx {
    protected final Context mContext;

    public OppoDummyCommonManagerServiceEx(Context context) {
        this.mContext = context;
    }

    @Override // com.android.server.IOppoCommonManagerServiceEx
    public void onStart() {
    }

    @Override // com.android.server.IOppoCommonManagerServiceEx
    public void systemReady() {
    }

    @Override // com.android.server.IOppoCommonManagerServiceEx
    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        return false;
    }
}
