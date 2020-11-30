package com.color.favorite;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IColorFavoriteQueryCallback extends IInterface {
    void onQueryResult(ColorFavoriteQueryResult colorFavoriteQueryResult) throws RemoteException;

    public static class Default implements IColorFavoriteQueryCallback {
        @Override // com.color.favorite.IColorFavoriteQueryCallback
        public void onQueryResult(ColorFavoriteQueryResult result) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IColorFavoriteQueryCallback {
        private static final String DESCRIPTOR = "com.color.favorite.IColorFavoriteQueryCallback";
        static final int TRANSACTION_onQueryResult = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IColorFavoriteQueryCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IColorFavoriteQueryCallback)) {
                return new Proxy(obj);
            }
            return (IColorFavoriteQueryCallback) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode != 1) {
                return null;
            }
            return "onQueryResult";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            ColorFavoriteQueryResult _arg0;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = ColorFavoriteQueryResult.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                onQueryResult(_arg0);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IColorFavoriteQueryCallback {
            public static IColorFavoriteQueryCallback sDefaultImpl;
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

            @Override // com.color.favorite.IColorFavoriteQueryCallback
            public void onQueryResult(ColorFavoriteQueryResult result) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (result != null) {
                        _data.writeInt(1);
                        result.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onQueryResult(result);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IColorFavoriteQueryCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IColorFavoriteQueryCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
