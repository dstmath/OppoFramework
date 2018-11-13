package com.qualcomm.qti.remoteSimlock;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IUimRemoteSimlockService extends IInterface {

    public static abstract class Stub extends Binder implements IUimRemoteSimlockService {
        private static final String DESCRIPTOR = "com.qualcomm.qti.remoteSimlock.IUimRemoteSimlockService";
        static final int TRANSACTION_deregisterCallback = 2;
        static final int TRANSACTION_registerCallback = 1;
        static final int TRANSACTION_uimRemoteSimlockGenerateHMAC = 5;
        static final int TRANSACTION_uimRemoteSimlockGetSharedKey = 4;
        static final int TRANSACTION_uimRemoteSimlockGetSimlockStatus = 7;
        static final int TRANSACTION_uimRemoteSimlockGetVersion = 6;
        static final int TRANSACTION_uimRemoteSimlockProcessSimlockData = 3;

        private static class Proxy implements IUimRemoteSimlockService {
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

            public int registerCallback(IUimRemoteSimlockServiceCallback cb) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (cb != null) {
                        iBinder = cb.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_registerCallback, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int deregisterCallback(IUimRemoteSimlockServiceCallback cb) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (cb != null) {
                        iBinder = cb.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_deregisterCallback, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int uimRemoteSimlockProcessSimlockData(int token, byte[] simlockData) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(token);
                    _data.writeByteArray(simlockData);
                    this.mRemote.transact(Stub.TRANSACTION_uimRemoteSimlockProcessSimlockData, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int uimRemoteSimlockGetSharedKey(int token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(token);
                    this.mRemote.transact(Stub.TRANSACTION_uimRemoteSimlockGetSharedKey, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int uimRemoteSimlockGenerateHMAC(int token, byte[] data) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(token);
                    _data.writeByteArray(data);
                    this.mRemote.transact(Stub.TRANSACTION_uimRemoteSimlockGenerateHMAC, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int uimRemoteSimlockGetVersion(int token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(token);
                    this.mRemote.transact(Stub.TRANSACTION_uimRemoteSimlockGetVersion, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int uimRemoteSimlockGetSimlockStatus(int token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(token);
                    this.mRemote.transact(Stub.TRANSACTION_uimRemoteSimlockGetSimlockStatus, _data, _reply, 0);
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

        public static IUimRemoteSimlockService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IUimRemoteSimlockService)) {
                return new Proxy(obj);
            }
            return (IUimRemoteSimlockService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int _result;
            switch (code) {
                case TRANSACTION_registerCallback /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = registerCallback(com.qualcomm.qti.remoteSimlock.IUimRemoteSimlockServiceCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_deregisterCallback /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = deregisterCallback(com.qualcomm.qti.remoteSimlock.IUimRemoteSimlockServiceCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_uimRemoteSimlockProcessSimlockData /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = uimRemoteSimlockProcessSimlockData(data.readInt(), data.createByteArray());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_uimRemoteSimlockGetSharedKey /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = uimRemoteSimlockGetSharedKey(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_uimRemoteSimlockGenerateHMAC /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = uimRemoteSimlockGenerateHMAC(data.readInt(), data.createByteArray());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_uimRemoteSimlockGetVersion /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = uimRemoteSimlockGetVersion(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_uimRemoteSimlockGetSimlockStatus /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = uimRemoteSimlockGetSimlockStatus(data.readInt());
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

    int deregisterCallback(IUimRemoteSimlockServiceCallback iUimRemoteSimlockServiceCallback) throws RemoteException;

    int registerCallback(IUimRemoteSimlockServiceCallback iUimRemoteSimlockServiceCallback) throws RemoteException;

    int uimRemoteSimlockGenerateHMAC(int i, byte[] bArr) throws RemoteException;

    int uimRemoteSimlockGetSharedKey(int i) throws RemoteException;

    int uimRemoteSimlockGetSimlockStatus(int i) throws RemoteException;

    int uimRemoteSimlockGetVersion(int i) throws RemoteException;

    int uimRemoteSimlockProcessSimlockData(int i, byte[] bArr) throws RemoteException;
}
