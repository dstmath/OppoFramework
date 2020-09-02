package com.color.antivirus;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IColorAntiViruStateChangeCallback extends IInterface {
    void onAntiVirusStateChange(boolean z) throws RemoteException;

    public static class Default implements IColorAntiViruStateChangeCallback {
        @Override // com.color.antivirus.IColorAntiViruStateChangeCallback
        public void onAntiVirusStateChange(boolean state) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IColorAntiViruStateChangeCallback {
        private static final String DESCRIPTOR = "com.color.antivirus.IColorAntiViruStateChangeCallback";
        static final int TRANSACTION_onAntiVirusStateChange = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IColorAntiViruStateChangeCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IColorAntiViruStateChangeCallback)) {
                return new Proxy(obj);
            }
            return (IColorAntiViruStateChangeCallback) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode != 1) {
                return null;
            }
            return "onAntiVirusStateChange";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onAntiVirusStateChange(data.readInt() != 0);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IColorAntiViruStateChangeCallback {
            public static IColorAntiViruStateChangeCallback sDefaultImpl;
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

            @Override // com.color.antivirus.IColorAntiViruStateChangeCallback
            public void onAntiVirusStateChange(boolean state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state ? 1 : 0);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onAntiVirusStateChange(state);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IColorAntiViruStateChangeCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IColorAntiViruStateChangeCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
