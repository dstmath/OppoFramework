package com.android.server;

import android.common.IOppoCommonFeature;
import android.os.Parcel;
import android.os.RemoteException;

public interface IOppoCommonManagerServiceEx extends IOppoCommonFeature {
    default void onStart() {
    }

    default void systemReady() {
    }

    default boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        return false;
    }
}
