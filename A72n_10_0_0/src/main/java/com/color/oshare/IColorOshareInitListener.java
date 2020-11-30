package com.color.oshare;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IColorOshareInitListener extends IInterface {
    void onShareInit() throws RemoteException;

    void onShareUninit() throws RemoteException;

    public static class Default implements IColorOshareInitListener {
        @Override // com.color.oshare.IColorOshareInitListener
        public void onShareInit() throws RemoteException {
        }

        @Override // com.color.oshare.IColorOshareInitListener
        public void onShareUninit() throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IColorOshareInitListener {
        private static final String DESCRIPTOR = "com.color.oshare.IColorOshareInitListener";
        static final int TRANSACTION_onShareInit = 1;
        static final int TRANSACTION_onShareUninit = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IColorOshareInitListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IColorOshareInitListener)) {
                return new Proxy(obj);
            }
            return (IColorOshareInitListener) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "onShareInit";
            }
            if (transactionCode != 2) {
                return null;
            }
            return "onShareUninit";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onShareInit();
                reply.writeNoException();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                onShareUninit();
                reply.writeNoException();
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IColorOshareInitListener {
            public static IColorOshareInitListener sDefaultImpl;
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

            @Override // com.color.oshare.IColorOshareInitListener
            public void onShareInit() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onShareInit();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.color.oshare.IColorOshareInitListener
            public void onShareUninit() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onShareUninit();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IColorOshareInitListener impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IColorOshareInitListener getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
