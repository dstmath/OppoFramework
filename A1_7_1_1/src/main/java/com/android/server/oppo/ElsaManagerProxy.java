package com.android.server.oppo;

import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;

public class ElsaManagerProxy implements IElsaManager {
    final String TAG = "ElsaManagerProxy";
    private IBinder mRemote;

    public ElsaManagerProxy(IBinder remote) {
        this.mRemote = remote;
    }

    public String getInterfaceDescriptor() {
        return IElsaManager.DESCRIPTOR;
    }

    public IBinder asBinder() {
        return this.mRemote;
    }

    public int pingTest() throws RemoteException {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        int result = 0;
        try {
            _data.writeInterfaceToken(IElsaManager.DESCRIPTOR);
            this.mRemote.transact(1, _data, _reply, 0);
            _reply.readException();
            result = _reply.readInt();
            return result;
        } finally {
            _reply.recycle();
            _data.recycle();
        }
    }

    public int elsaSetCpuLoadLimit(int uid, String pkgname, int limit, int timeout, int flag) throws RemoteException {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        int result = 0;
        try {
            _data.writeInterfaceToken(IElsaManager.DESCRIPTOR);
            _data.writeInt(uid);
            _data.writeString(pkgname);
            _data.writeInt(limit);
            _data.writeInt(timeout);
            _data.writeInt(flag);
            this.mRemote.transact(101, _data, _reply, 0);
            _reply.readException();
            result = _reply.readInt();
            return result;
        } finally {
            _reply.recycle();
            _data.recycle();
        }
    }

    public int elsaGetCpuLoadLimit(int uid, int flag) throws RemoteException {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        int result = 0;
        try {
            _data.writeInterfaceToken(IElsaManager.DESCRIPTOR);
            _data.writeInt(uid);
            _data.writeInt(flag);
            this.mRemote.transact(102, _data, _reply, 0);
            result = _reply.readInt();
            return result;
        } finally {
            _reply.recycle();
            _data.recycle();
        }
    }

    public int elsaSetCoreLimit(int uid, String pkgname, int coreMask, int timeout, int flag) throws RemoteException {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        int result = 0;
        try {
            _data.writeInterfaceToken(IElsaManager.DESCRIPTOR);
            _data.writeInt(uid);
            _data.writeString(pkgname);
            _data.writeInt(coreMask);
            _data.writeInt(timeout);
            _data.writeInt(flag);
            this.mRemote.transact(103, _data, _reply, 0);
            _reply.readException();
            result = _reply.readInt();
            return result;
        } finally {
            _reply.recycle();
            _data.recycle();
        }
    }

    public int elsaGetCoreLimit(int uid, int flag) throws RemoteException {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        int result = 0;
        try {
            _data.writeInterfaceToken(IElsaManager.DESCRIPTOR);
            _data.writeInt(uid);
            _data.writeInt(flag);
            this.mRemote.transact(104, _data, _reply, 0);
            result = _reply.readInt();
            return result;
        } finally {
            _reply.recycle();
            _data.recycle();
        }
    }

    public int elsaSetPackageFreezing(int uid, String pkgname, int level, int flag) throws RemoteException {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        int result = 0;
        try {
            _data.writeInterfaceToken(IElsaManager.DESCRIPTOR);
            _data.writeInt(uid);
            _data.writeString(pkgname);
            _data.writeInt(level);
            _data.writeInt(flag);
            this.mRemote.transact(105, _data, _reply, 0);
            _reply.readException();
            result = _reply.readInt();
            return result;
        } finally {
            _reply.recycle();
            _data.recycle();
        }
    }

    public int elsaGetPackageFreezing(int uid, int flag) throws RemoteException {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        int result = 0;
        try {
            _data.writeInterfaceToken(IElsaManager.DESCRIPTOR);
            _data.writeInt(uid);
            _data.writeInt(flag);
            this.mRemote.transact(106, _data, _reply, 0);
            result = _reply.readInt();
            return result;
        } finally {
            _reply.recycle();
            _data.recycle();
        }
    }

    public int elsaSetPackagePriority(int uid, String pkgname, int pri, int flag) throws RemoteException {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        int result = 0;
        try {
            _data.writeInterfaceToken(IElsaManager.DESCRIPTOR);
            _data.writeInt(uid);
            _data.writeString(pkgname);
            _data.writeInt(pri);
            _data.writeInt(flag);
            this.mRemote.transact(107, _data, _reply, 0);
            _reply.readException();
            result = _reply.readInt();
            return result;
        } finally {
            _reply.recycle();
            _data.recycle();
        }
    }
}
