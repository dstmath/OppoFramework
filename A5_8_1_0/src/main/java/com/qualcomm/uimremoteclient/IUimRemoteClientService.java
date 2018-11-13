package com.qualcomm.uimremoteclient;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IUimRemoteClientService extends IInterface {

    public static abstract class Stub extends Binder implements IUimRemoteClientService {
        private static final String DESCRIPTOR = "com.qualcomm.uimremoteclient.IUimRemoteClientService";
        static final int TRANSACTION_deregisterCallback = 2;
        static final int TRANSACTION_registerCallback = 1;
        static final int TRANSACTION_uimRemoteApdu = 4;
        static final int TRANSACTION_uimRemoteEvent = 3;

        private static class Proxy implements IUimRemoteClientService {
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

            public int registerCallback(IUimRemoteClientServiceCallback cb) throws RemoteException {
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

            public int deregisterCallback(IUimRemoteClientServiceCallback cb) throws RemoteException {
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

            public int uimRemoteEvent(int slot, int event, byte[] atr, int errCode, boolean has_transport, int transport, boolean has_usage, int usage, boolean has_apdu_timeout, int apdu_timeout, boolean has_disable_all_polling, int disable_all_polling, boolean has_poll_timer, int poll_timer) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slot);
                    _data.writeInt(event);
                    _data.writeByteArray(atr);
                    _data.writeInt(errCode);
                    _data.writeInt(has_transport ? Stub.TRANSACTION_registerCallback : 0);
                    _data.writeInt(transport);
                    _data.writeInt(has_usage ? Stub.TRANSACTION_registerCallback : 0);
                    _data.writeInt(usage);
                    _data.writeInt(has_apdu_timeout ? Stub.TRANSACTION_registerCallback : 0);
                    _data.writeInt(apdu_timeout);
                    _data.writeInt(has_disable_all_polling ? Stub.TRANSACTION_registerCallback : 0);
                    _data.writeInt(disable_all_polling);
                    _data.writeInt(has_poll_timer ? Stub.TRANSACTION_registerCallback : 0);
                    _data.writeInt(poll_timer);
                    this.mRemote.transact(Stub.TRANSACTION_uimRemoteEvent, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int uimRemoteApdu(int slot, int apduStatus, byte[] apduResp) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slot);
                    _data.writeInt(apduStatus);
                    _data.writeByteArray(apduResp);
                    this.mRemote.transact(Stub.TRANSACTION_uimRemoteApdu, _data, _reply, 0);
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

        public static IUimRemoteClientService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IUimRemoteClientService)) {
                return new Proxy(obj);
            }
            return (IUimRemoteClientService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int _result;
            switch (code) {
                case TRANSACTION_registerCallback /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = registerCallback(com.qualcomm.uimremoteclient.IUimRemoteClientServiceCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_deregisterCallback /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = deregisterCallback(com.qualcomm.uimremoteclient.IUimRemoteClientServiceCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_uimRemoteEvent /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = uimRemoteEvent(data.readInt(), data.readInt(), data.createByteArray(), data.readInt(), data.readInt() != 0, data.readInt(), data.readInt() != 0, data.readInt(), data.readInt() != 0, data.readInt(), data.readInt() != 0, data.readInt(), data.readInt() != 0, data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_uimRemoteApdu /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = uimRemoteApdu(data.readInt(), data.readInt(), data.createByteArray());
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

    int deregisterCallback(IUimRemoteClientServiceCallback iUimRemoteClientServiceCallback) throws RemoteException;

    int registerCallback(IUimRemoteClientServiceCallback iUimRemoteClientServiceCallback) throws RemoteException;

    int uimRemoteApdu(int i, int i2, byte[] bArr) throws RemoteException;

    int uimRemoteEvent(int i, int i2, byte[] bArr, int i3, boolean z, int i4, boolean z2, int i5, boolean z3, int i6, boolean z4, int i7, boolean z5, int i8) throws RemoteException;
}
