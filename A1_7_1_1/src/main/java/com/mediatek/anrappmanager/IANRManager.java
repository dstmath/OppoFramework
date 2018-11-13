package com.mediatek.anrappmanager;

import android.os.IInterface;
import android.os.RemoteException;

public interface IANRManager extends IInterface {
    public static final int INFORM_MESSAGE_DUMP_TRANSACTION = 3;
    public static final int NOTIFY_LIGHTWEIGHT_ANR_TRANSACTION = 2;
    public static final String descriptor = "android.app.IANRManager";

    void informMessageDump(String str, int i) throws RemoteException;

    void notifyLightWeightANR(int i, String str, int i2) throws RemoteException;
}
