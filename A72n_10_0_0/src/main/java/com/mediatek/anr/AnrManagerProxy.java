package com.mediatek.anr;

import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;

class AnrManagerProxy implements IAnrManager {
    private IBinder mRemote;

    public AnrManagerProxy(IBinder remote) {
        this.mRemote = remote;
    }

    public IBinder asBinder() {
        return this.mRemote;
    }

    @Override // com.mediatek.anr.IAnrManager
    public void informMessageDump(String msgInfo, int pid) throws RemoteException {
        Parcel data = Parcel.obtain();
        data.writeInterfaceToken(IAnrManager.descriptor);
        data.writeString(msgInfo);
        data.writeInt(pid);
        this.mRemote.transact(2, data, null, 1);
        data.recycle();
    }
}
