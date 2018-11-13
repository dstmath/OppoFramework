package com.qualcomm.wfd.service;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IHIDEventListener extends IInterface {

    public static abstract class Stub extends Binder implements IHIDEventListener {
        private static final String DESCRIPTOR = "com.qualcomm.wfd.service.IHIDEventListener";
        static final int TRANSACTION_onHIDReprtDescRcv = 1;
        static final int TRANSACTION_onHIDReprtRcv = 2;

        private static class Proxy implements IHIDEventListener {
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

            public void onHIDReprtDescRcv(byte[] HIDRD) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(HIDRD);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onHIDReprtRcv(byte[] HIDRep) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(HIDRep);
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHIDEventListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHIDEventListener)) {
                return new Proxy(obj);
            }
            return (IHIDEventListener) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    onHIDReprtDescRcv(data.createByteArray());
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    onHIDReprtRcv(data.createByteArray());
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void onHIDReprtDescRcv(byte[] bArr) throws RemoteException;

    void onHIDReprtRcv(byte[] bArr) throws RemoteException;
}
