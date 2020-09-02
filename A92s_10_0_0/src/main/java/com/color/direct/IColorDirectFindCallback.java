package com.color.direct;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IColorDirectFindCallback extends IInterface {
    void onDirectInfoFound(ColorDirectFindResult colorDirectFindResult) throws RemoteException;

    public static class Default implements IColorDirectFindCallback {
        @Override // com.color.direct.IColorDirectFindCallback
        public void onDirectInfoFound(ColorDirectFindResult result) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IColorDirectFindCallback {
        private static final String DESCRIPTOR = "com.color.direct.IColorDirectFindCallback";
        static final int TRANSACTION_onDirectInfoFound = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IColorDirectFindCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IColorDirectFindCallback)) {
                return new Proxy(obj);
            }
            return (IColorDirectFindCallback) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode != 1) {
                return null;
            }
            return "onDirectInfoFound";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            ColorDirectFindResult _arg0;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = ColorDirectFindResult.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                onDirectInfoFound(_arg0);
                reply.writeNoException();
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IColorDirectFindCallback {
            public static IColorDirectFindCallback sDefaultImpl;
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

            @Override // com.color.direct.IColorDirectFindCallback
            public void onDirectInfoFound(ColorDirectFindResult result) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (result != null) {
                        _data.writeInt(1);
                        result.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onDirectInfoFound(result);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IColorDirectFindCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IColorDirectFindCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
