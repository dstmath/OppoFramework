package com.qualcomm.qti;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IPerfManager extends IInterface {

    public static abstract class Stub extends Binder implements IPerfManager {
        private static final String DESCRIPTOR = "com.qualcomm.qti.IPerfManager";
        static final int TRANSACTION_perfHint = 3;
        static final int TRANSACTION_perfLockAcquire = 4;
        static final int TRANSACTION_perfLockRelease = 1;
        static final int TRANSACTION_perfLockReleaseHandler = 2;

        private static class Proxy implements IPerfManager {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            public int perfLockRelease() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_perfLockRelease, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int perfLockReleaseHandler(int handle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(handle);
                    this.mRemote.transact(Stub.TRANSACTION_perfLockReleaseHandler, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int perfHint(int hint, String userDataStr, int userData1, int userData2) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(hint);
                    _data.writeString(userDataStr);
                    _data.writeInt(userData1);
                    _data.writeInt(userData2);
                    this.mRemote.transact(Stub.TRANSACTION_perfHint, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int perfLockAcquire(int duration, int[] list) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(duration);
                    _data.writeIntArray(list);
                    this.mRemote.transact(Stub.TRANSACTION_perfLockAcquire, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IPerfManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IPerfManager)) {
                return new Proxy(obj);
            }
            return (IPerfManager) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int _result;
            switch (code) {
                case TRANSACTION_perfLockRelease /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = perfLockRelease();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_perfLockReleaseHandler /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = perfLockReleaseHandler(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_perfHint /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = perfHint(data.readInt(), data.readString(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_perfLockAcquire /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = perfLockAcquire(data.readInt(), data.createIntArray());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    int perfHint(int i, String str, int i2, int i3) throws RemoteException;

    int perfLockAcquire(int i, int[] iArr) throws RemoteException;

    int perfLockRelease() throws RemoteException;

    int perfLockReleaseHandler(int i) throws RemoteException;
}
