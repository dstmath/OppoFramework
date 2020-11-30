package com.mediatek.anr;

import android.os.IInterface;
import android.os.RemoteException;

public interface IAnrManager extends IInterface {
    public static final int INFORM_MESSAGE_DUMP_TRANSACTION = 2;
    public static final String descriptor = "android.app.IAnrManager";

    void informMessageDump(String str, int i) throws RemoteException;
}
