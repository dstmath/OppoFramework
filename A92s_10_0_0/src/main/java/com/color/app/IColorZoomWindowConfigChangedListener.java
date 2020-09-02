package com.color.app;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IColorZoomWindowConfigChangedListener extends IInterface {
    void onConfigSwitchChanged(boolean z) throws RemoteException;

    void onConfigTypeChanged(int i) throws RemoteException;

    public static class Default implements IColorZoomWindowConfigChangedListener {
        @Override // com.color.app.IColorZoomWindowConfigChangedListener
        public void onConfigTypeChanged(int type) throws RemoteException {
        }

        @Override // com.color.app.IColorZoomWindowConfigChangedListener
        public void onConfigSwitchChanged(boolean enable) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IColorZoomWindowConfigChangedListener {
        private static final String DESCRIPTOR = "com.color.app.IColorZoomWindowConfigChangedListener";
        static final int TRANSACTION_onConfigSwitchChanged = 2;
        static final int TRANSACTION_onConfigTypeChanged = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IColorZoomWindowConfigChangedListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IColorZoomWindowConfigChangedListener)) {
                return new Proxy(obj);
            }
            return (IColorZoomWindowConfigChangedListener) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "onConfigTypeChanged";
            }
            if (transactionCode != 2) {
                return null;
            }
            return "onConfigSwitchChanged";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onConfigTypeChanged(data.readInt());
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                onConfigSwitchChanged(data.readInt() != 0);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IColorZoomWindowConfigChangedListener {
            public static IColorZoomWindowConfigChangedListener sDefaultImpl;
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

            @Override // com.color.app.IColorZoomWindowConfigChangedListener
            public void onConfigTypeChanged(int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onConfigTypeChanged(type);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.color.app.IColorZoomWindowConfigChangedListener
            public void onConfigSwitchChanged(boolean enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enable ? 1 : 0);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onConfigSwitchChanged(enable);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IColorZoomWindowConfigChangedListener impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IColorZoomWindowConfigChangedListener getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
