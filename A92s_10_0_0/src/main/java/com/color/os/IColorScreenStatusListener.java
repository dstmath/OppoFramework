package com.color.os;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IColorScreenStatusListener extends IInterface {
    void onScreenOff() throws RemoteException;

    void onScreenOn() throws RemoteException;

    public static class Default implements IColorScreenStatusListener {
        @Override // com.color.os.IColorScreenStatusListener
        public void onScreenOff() throws RemoteException {
        }

        @Override // com.color.os.IColorScreenStatusListener
        public void onScreenOn() throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IColorScreenStatusListener {
        private static final String DESCRIPTOR = "com.color.os.IColorScreenStatusListener";
        static final int TRANSACTION_onScreenOff = 1;
        static final int TRANSACTION_onScreenOn = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IColorScreenStatusListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IColorScreenStatusListener)) {
                return new Proxy(obj);
            }
            return (IColorScreenStatusListener) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "onScreenOff";
            }
            if (transactionCode != 2) {
                return null;
            }
            return "onScreenOn";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onScreenOff();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                onScreenOn();
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IColorScreenStatusListener {
            public static IColorScreenStatusListener sDefaultImpl;
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            @Override // com.color.os.IColorScreenStatusListener
            public void onScreenOff() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onScreenOff();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.color.os.IColorScreenStatusListener
            public void onScreenOn() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onScreenOn();
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IColorScreenStatusListener impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IColorScreenStatusListener getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
