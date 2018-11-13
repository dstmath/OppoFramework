package com.qualcomm.uimremoteserver;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IUimRemoteServerService extends IInterface {

    public static abstract class Stub extends Binder implements IUimRemoteServerService {
        private static final String DESCRIPTOR = "com.qualcomm.uimremoteserver.IUimRemoteServerService";
        static final int TRANSACTION_deregisterCallback = 2;
        static final int TRANSACTION_registerCallback = 1;
        static final int TRANSACTION_uimRemoteServerApduReq = 5;
        static final int TRANSACTION_uimRemoteServerConnectReq = 3;
        static final int TRANSACTION_uimRemoteServerDisconnectReq = 4;
        static final int TRANSACTION_uimRemoteServerPowerReq = 7;
        static final int TRANSACTION_uimRemoteServerResetSimReq = 8;
        static final int TRANSACTION_uimRemoteServerTransferAtrReq = 6;

        private static class Proxy implements IUimRemoteServerService {
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

            public int registerCallback(IUimRemoteServerServiceCallback cb) throws RemoteException {
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

            public int deregisterCallback(IUimRemoteServerServiceCallback cb) throws RemoteException {
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

            public int uimRemoteServerConnectReq(int slot, int maxMessageSize) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slot);
                    _data.writeInt(maxMessageSize);
                    this.mRemote.transact(Stub.TRANSACTION_uimRemoteServerConnectReq, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int uimRemoteServerDisconnectReq(int slot) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slot);
                    this.mRemote.transact(Stub.TRANSACTION_uimRemoteServerDisconnectReq, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int uimRemoteServerApduReq(int slot, byte[] cmd) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slot);
                    _data.writeByteArray(cmd);
                    this.mRemote.transact(Stub.TRANSACTION_uimRemoteServerApduReq, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int uimRemoteServerTransferAtrReq(int slot) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slot);
                    this.mRemote.transact(Stub.TRANSACTION_uimRemoteServerTransferAtrReq, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int uimRemoteServerPowerReq(int slot, boolean state) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slot);
                    if (state) {
                        i = Stub.TRANSACTION_registerCallback;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_uimRemoteServerPowerReq, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int uimRemoteServerResetSimReq(int slot) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slot);
                    this.mRemote.transact(Stub.TRANSACTION_uimRemoteServerResetSimReq, _data, _reply, 0);
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

        public static IUimRemoteServerService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IUimRemoteServerService)) {
                return new Proxy(obj);
            }
            return (IUimRemoteServerService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int _result;
            switch (code) {
                case TRANSACTION_registerCallback /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = registerCallback(com.qualcomm.uimremoteserver.IUimRemoteServerServiceCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_deregisterCallback /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = deregisterCallback(com.qualcomm.uimremoteserver.IUimRemoteServerServiceCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_uimRemoteServerConnectReq /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = uimRemoteServerConnectReq(data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_uimRemoteServerDisconnectReq /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = uimRemoteServerDisconnectReq(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_uimRemoteServerApduReq /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = uimRemoteServerApduReq(data.readInt(), data.createByteArray());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_uimRemoteServerTransferAtrReq /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = uimRemoteServerTransferAtrReq(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_uimRemoteServerPowerReq /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = uimRemoteServerPowerReq(data.readInt(), data.readInt() != 0);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_uimRemoteServerResetSimReq /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = uimRemoteServerResetSimReq(data.readInt());
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

    int deregisterCallback(IUimRemoteServerServiceCallback iUimRemoteServerServiceCallback) throws RemoteException;

    int registerCallback(IUimRemoteServerServiceCallback iUimRemoteServerServiceCallback) throws RemoteException;

    int uimRemoteServerApduReq(int i, byte[] bArr) throws RemoteException;

    int uimRemoteServerConnectReq(int i, int i2) throws RemoteException;

    int uimRemoteServerDisconnectReq(int i) throws RemoteException;

    int uimRemoteServerPowerReq(int i, boolean z) throws RemoteException;

    int uimRemoteServerResetSimReq(int i) throws RemoteException;

    int uimRemoteServerTransferAtrReq(int i) throws RemoteException;
}
