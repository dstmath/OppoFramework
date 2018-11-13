package com.qualcomm.uimremoteserver;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IUimRemoteServerServiceCallback extends IInterface {

    public static abstract class Stub extends Binder implements IUimRemoteServerServiceCallback {
        private static final String DESCRIPTOR = "com.qualcomm.uimremoteserver.IUimRemoteServerServiceCallback";
        static final int TRANSACTION_uimRemoteServerApduResp = 3;
        static final int TRANSACTION_uimRemoteServerConnectResp = 1;
        static final int TRANSACTION_uimRemoteServerDisconnectInd = 7;
        static final int TRANSACTION_uimRemoteServerDisconnectResp = 2;
        static final int TRANSACTION_uimRemoteServerPowerResp = 5;
        static final int TRANSACTION_uimRemoteServerResetSimResp = 6;
        static final int TRANSACTION_uimRemoteServerStatusInd = 8;
        static final int TRANSACTION_uimRemoteServerTransferAtrResp = 4;

        private static class Proxy implements IUimRemoteServerServiceCallback {
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

            public void uimRemoteServerConnectResp(int slot, int connStatus, int maxMessageSize) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slot);
                    _data.writeInt(connStatus);
                    _data.writeInt(maxMessageSize);
                    this.mRemote.transact(Stub.TRANSACTION_uimRemoteServerConnectResp, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void uimRemoteServerDisconnectResp(int slot, int disconnStatus) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slot);
                    _data.writeInt(disconnStatus);
                    this.mRemote.transact(Stub.TRANSACTION_uimRemoteServerDisconnectResp, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void uimRemoteServerApduResp(int slot, int respStatus, byte[] respData) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slot);
                    _data.writeInt(respStatus);
                    _data.writeByteArray(respData);
                    this.mRemote.transact(Stub.TRANSACTION_uimRemoteServerApduResp, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void uimRemoteServerTransferAtrResp(int slot, int transferStatus, byte[] atr) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slot);
                    _data.writeInt(transferStatus);
                    _data.writeByteArray(atr);
                    this.mRemote.transact(Stub.TRANSACTION_uimRemoteServerTransferAtrResp, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void uimRemoteServerPowerResp(int slot, int powerStatus) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slot);
                    _data.writeInt(powerStatus);
                    this.mRemote.transact(Stub.TRANSACTION_uimRemoteServerPowerResp, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void uimRemoteServerResetSimResp(int slot, int resetStatus) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slot);
                    _data.writeInt(resetStatus);
                    this.mRemote.transact(Stub.TRANSACTION_uimRemoteServerResetSimResp, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void uimRemoteServerDisconnectInd(int slot, int disconnectType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slot);
                    _data.writeInt(disconnectType);
                    this.mRemote.transact(Stub.TRANSACTION_uimRemoteServerDisconnectInd, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void uimRemoteServerStatusInd(int slot, int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slot);
                    _data.writeInt(status);
                    this.mRemote.transact(Stub.TRANSACTION_uimRemoteServerStatusInd, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IUimRemoteServerServiceCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IUimRemoteServerServiceCallback)) {
                return new Proxy(obj);
            }
            return (IUimRemoteServerServiceCallback) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case TRANSACTION_uimRemoteServerConnectResp /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    uimRemoteServerConnectResp(data.readInt(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_uimRemoteServerDisconnectResp /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    uimRemoteServerDisconnectResp(data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_uimRemoteServerApduResp /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    uimRemoteServerApduResp(data.readInt(), data.readInt(), data.createByteArray());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_uimRemoteServerTransferAtrResp /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    uimRemoteServerTransferAtrResp(data.readInt(), data.readInt(), data.createByteArray());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_uimRemoteServerPowerResp /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    uimRemoteServerPowerResp(data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_uimRemoteServerResetSimResp /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    uimRemoteServerResetSimResp(data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_uimRemoteServerDisconnectInd /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    uimRemoteServerDisconnectInd(data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_uimRemoteServerStatusInd /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    uimRemoteServerStatusInd(data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void uimRemoteServerApduResp(int i, int i2, byte[] bArr) throws RemoteException;

    void uimRemoteServerConnectResp(int i, int i2, int i3) throws RemoteException;

    void uimRemoteServerDisconnectInd(int i, int i2) throws RemoteException;

    void uimRemoteServerDisconnectResp(int i, int i2) throws RemoteException;

    void uimRemoteServerPowerResp(int i, int i2) throws RemoteException;

    void uimRemoteServerResetSimResp(int i, int i2) throws RemoteException;

    void uimRemoteServerStatusInd(int i, int i2) throws RemoteException;

    void uimRemoteServerTransferAtrResp(int i, int i2, byte[] bArr) throws RemoteException;
}
