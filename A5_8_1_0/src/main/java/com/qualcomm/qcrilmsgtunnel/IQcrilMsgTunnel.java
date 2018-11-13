package com.qualcomm.qcrilmsgtunnel;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.qualcomm.qcrilhook.IOemHookCallback;

public interface IQcrilMsgTunnel extends IInterface {

    public static abstract class Stub extends Binder implements IQcrilMsgTunnel {
        private static final String DESCRIPTOR = "com.qualcomm.qcrilmsgtunnel.IQcrilMsgTunnel";
        static final int TRANSACTION_sendOemRilRequestRaw = 1;
        static final int TRANSACTION_sendOemRilRequestRawAsync = 2;

        private static class Proxy implements IQcrilMsgTunnel {
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

            public int sendOemRilRequestRaw(byte[] request, byte[] response, int sub) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(request);
                    if (response == null) {
                        _data.writeInt(-1);
                    } else {
                        _data.writeInt(response.length);
                    }
                    _data.writeInt(sub);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.readByteArray(response);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void sendOemRilRequestRawAsync(byte[] request, IOemHookCallback oemHookCb, int sub) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(request);
                    if (oemHookCb != null) {
                        iBinder = oemHookCb.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(sub);
                    this.mRemote.transact(2, _data, _reply, 0);
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

        public static IQcrilMsgTunnel asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IQcrilMsgTunnel)) {
                return new Proxy(obj);
            }
            return (IQcrilMsgTunnel) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1:
                    byte[] _arg1;
                    data.enforceInterface(DESCRIPTOR);
                    byte[] _arg0 = data.createByteArray();
                    int _arg1_length = data.readInt();
                    if (_arg1_length < 0) {
                        _arg1 = null;
                    } else {
                        _arg1 = new byte[_arg1_length];
                    }
                    int _result = sendOemRilRequestRaw(_arg0, _arg1, data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    reply.writeByteArray(_arg1);
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    sendOemRilRequestRawAsync(data.createByteArray(), com.qualcomm.qcrilhook.IOemHookCallback.Stub.asInterface(data.readStrongBinder()), data.readInt());
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

    int sendOemRilRequestRaw(byte[] bArr, byte[] bArr2, int i) throws RemoteException;

    void sendOemRilRequestRawAsync(byte[] bArr, IOemHookCallback iOemHookCallback, int i) throws RemoteException;
}
