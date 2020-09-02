package com.color.app;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IColorAccessControlObserver extends IInterface {
    void onAccessControlStateChange(ColorAccessControlInfo colorAccessControlInfo) throws RemoteException;

    public static class Default implements IColorAccessControlObserver {
        @Override // com.color.app.IColorAccessControlObserver
        public void onAccessControlStateChange(ColorAccessControlInfo info) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IColorAccessControlObserver {
        private static final String DESCRIPTOR = "com.color.app.IColorAccessControlObserver";
        static final int TRANSACTION_onAccessControlStateChange = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IColorAccessControlObserver asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IColorAccessControlObserver)) {
                return new Proxy(obj);
            }
            return (IColorAccessControlObserver) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode != 1) {
                return null;
            }
            return "onAccessControlStateChange";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            ColorAccessControlInfo _arg0;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = ColorAccessControlInfo.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                onAccessControlStateChange(_arg0);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IColorAccessControlObserver {
            public static IColorAccessControlObserver sDefaultImpl;
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

            @Override // com.color.app.IColorAccessControlObserver
            public void onAccessControlStateChange(ColorAccessControlInfo info) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (info != null) {
                        _data.writeInt(1);
                        info.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onAccessControlStateChange(info);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IColorAccessControlObserver impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IColorAccessControlObserver getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
