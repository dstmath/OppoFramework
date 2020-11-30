package com.color.app;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IColorFreeformConfigChangedListener extends IInterface {
    void onConfigSwitchChanged(boolean z) throws RemoteException;

    void onConfigTypeChanged(int i) throws RemoteException;

    public static class Default implements IColorFreeformConfigChangedListener {
        @Override // com.color.app.IColorFreeformConfigChangedListener
        public void onConfigTypeChanged(int type) throws RemoteException {
        }

        @Override // com.color.app.IColorFreeformConfigChangedListener
        public void onConfigSwitchChanged(boolean enable) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IColorFreeformConfigChangedListener {
        private static final String DESCRIPTOR = "com.color.app.IColorFreeformConfigChangedListener";
        static final int TRANSACTION_onConfigSwitchChanged = 2;
        static final int TRANSACTION_onConfigTypeChanged = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IColorFreeformConfigChangedListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IColorFreeformConfigChangedListener)) {
                return new Proxy(obj);
            }
            return (IColorFreeformConfigChangedListener) iin;
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

        /* access modifiers changed from: private */
        public static class Proxy implements IColorFreeformConfigChangedListener {
            public static IColorFreeformConfigChangedListener sDefaultImpl;
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

            @Override // com.color.app.IColorFreeformConfigChangedListener
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

            @Override // com.color.app.IColorFreeformConfigChangedListener
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

        public static boolean setDefaultImpl(IColorFreeformConfigChangedListener impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IColorFreeformConfigChangedListener getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
