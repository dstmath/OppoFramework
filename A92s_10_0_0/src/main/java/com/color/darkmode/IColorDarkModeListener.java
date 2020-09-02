package com.color.darkmode;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IColorDarkModeListener extends IInterface {
    void onUiModeConfigurationChangeFinish() throws RemoteException;

    public static class Default implements IColorDarkModeListener {
        @Override // com.color.darkmode.IColorDarkModeListener
        public void onUiModeConfigurationChangeFinish() throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IColorDarkModeListener {
        private static final String DESCRIPTOR = "com.color.darkmode.IColorDarkModeListener";
        static final int TRANSACTION_onUiModeConfigurationChangeFinish = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IColorDarkModeListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IColorDarkModeListener)) {
                return new Proxy(obj);
            }
            return (IColorDarkModeListener) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode != 1) {
                return null;
            }
            return "onUiModeConfigurationChangeFinish";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onUiModeConfigurationChangeFinish();
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IColorDarkModeListener {
            public static IColorDarkModeListener sDefaultImpl;
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

            @Override // com.color.darkmode.IColorDarkModeListener
            public void onUiModeConfigurationChangeFinish() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onUiModeConfigurationChangeFinish();
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IColorDarkModeListener impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IColorDarkModeListener getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
