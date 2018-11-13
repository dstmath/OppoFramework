package com.qualcomm.uimremoteclient;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IUimRemoteClientServiceCallback extends IInterface {

    public static abstract class Stub extends Binder implements IUimRemoteClientServiceCallback {
        private static final String DESCRIPTOR = "com.qualcomm.uimremoteclient.IUimRemoteClientServiceCallback";
        static final int TRANSACTION_uimRemoteApduIndication = 3;
        static final int TRANSACTION_uimRemoteApduResponse = 2;
        static final int TRANSACTION_uimRemoteConnectIndication = 4;
        static final int TRANSACTION_uimRemoteDisconnectIndication = 5;
        static final int TRANSACTION_uimRemoteEventResponse = 1;
        static final int TRANSACTION_uimRemotePowerDownIndication = 7;
        static final int TRANSACTION_uimRemotePowerUpIndication = 6;
        static final int TRANSACTION_uimRemoteRadioStateIndication = 9;
        static final int TRANSACTION_uimRemoteResetIndication = 8;

        private static class Proxy implements IUimRemoteClientServiceCallback {
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

            public void uimRemoteEventResponse(int slot, int responseCode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slot);
                    _data.writeInt(responseCode);
                    this.mRemote.transact(Stub.TRANSACTION_uimRemoteEventResponse, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void uimRemoteApduResponse(int slot, int responseCode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slot);
                    _data.writeInt(responseCode);
                    this.mRemote.transact(Stub.TRANSACTION_uimRemoteApduResponse, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void uimRemoteApduIndication(int slot, byte[] apduCmd) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slot);
                    _data.writeByteArray(apduCmd);
                    this.mRemote.transact(Stub.TRANSACTION_uimRemoteApduIndication, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void uimRemoteConnectIndication(int slot) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slot);
                    this.mRemote.transact(Stub.TRANSACTION_uimRemoteConnectIndication, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void uimRemoteDisconnectIndication(int slot) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slot);
                    this.mRemote.transact(Stub.TRANSACTION_uimRemoteDisconnectIndication, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void uimRemotePowerUpIndication(int slot) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slot);
                    this.mRemote.transact(Stub.TRANSACTION_uimRemotePowerUpIndication, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void uimRemotePowerDownIndication(int slot) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slot);
                    this.mRemote.transact(Stub.TRANSACTION_uimRemotePowerDownIndication, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void uimRemoteResetIndication(int slot) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slot);
                    this.mRemote.transact(Stub.TRANSACTION_uimRemoteResetIndication, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void uimRemoteRadioStateIndication(int slot, int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slot);
                    _data.writeInt(state);
                    this.mRemote.transact(Stub.TRANSACTION_uimRemoteRadioStateIndication, _data, _reply, 0);
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

        public static IUimRemoteClientServiceCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IUimRemoteClientServiceCallback)) {
                return new Proxy(obj);
            }
            return (IUimRemoteClientServiceCallback) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case TRANSACTION_uimRemoteEventResponse /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    uimRemoteEventResponse(data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_uimRemoteApduResponse /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    uimRemoteApduResponse(data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_uimRemoteApduIndication /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    uimRemoteApduIndication(data.readInt(), data.createByteArray());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_uimRemoteConnectIndication /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    uimRemoteConnectIndication(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_uimRemoteDisconnectIndication /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    uimRemoteDisconnectIndication(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_uimRemotePowerUpIndication /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    uimRemotePowerUpIndication(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_uimRemotePowerDownIndication /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    uimRemotePowerDownIndication(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_uimRemoteResetIndication /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    uimRemoteResetIndication(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_uimRemoteRadioStateIndication /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    uimRemoteRadioStateIndication(data.readInt(), data.readInt());
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

    void uimRemoteApduIndication(int i, byte[] bArr) throws RemoteException;

    void uimRemoteApduResponse(int i, int i2) throws RemoteException;

    void uimRemoteConnectIndication(int i) throws RemoteException;

    void uimRemoteDisconnectIndication(int i) throws RemoteException;

    void uimRemoteEventResponse(int i, int i2) throws RemoteException;

    void uimRemotePowerDownIndication(int i) throws RemoteException;

    void uimRemotePowerUpIndication(int i) throws RemoteException;

    void uimRemoteRadioStateIndication(int i, int i2) throws RemoteException;

    void uimRemoteResetIndication(int i) throws RemoteException;
}
