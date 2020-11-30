package com.color.antivirus.qihoo;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IUidSetChangeCallbackInterface extends IInterface {
    void onUidSetChanged(int[] iArr) throws RemoteException;

    public static class Default implements IUidSetChangeCallbackInterface {
        @Override // com.color.antivirus.qihoo.IUidSetChangeCallbackInterface
        public void onUidSetChanged(int[] uidArray) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IUidSetChangeCallbackInterface {
        private static final String DESCRIPTOR = "com.color.antivirus.qihoo.IUidSetChangeCallbackInterface";
        static final int TRANSACTION_onUidSetChanged = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IUidSetChangeCallbackInterface asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IUidSetChangeCallbackInterface)) {
                return new Proxy(obj);
            }
            return (IUidSetChangeCallbackInterface) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode != 1) {
                return null;
            }
            return "onUidSetChanged";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onUidSetChanged(data.createIntArray());
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IUidSetChangeCallbackInterface {
            public static IUidSetChangeCallbackInterface sDefaultImpl;
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

            @Override // com.color.antivirus.qihoo.IUidSetChangeCallbackInterface
            public void onUidSetChanged(int[] uidArray) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeIntArray(uidArray);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onUidSetChanged(uidArray);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IUidSetChangeCallbackInterface impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IUidSetChangeCallbackInterface getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
