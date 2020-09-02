package com.color.antivirus;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.color.antivirus.IColorAntiViruStateChangeCallback;

public interface IColorAntiVirusManagerService extends IInterface {
    void registerStateChangeCallback(IColorAntiViruStateChangeCallback iColorAntiViruStateChangeCallback) throws RemoteException;

    public static class Default implements IColorAntiVirusManagerService {
        @Override // com.color.antivirus.IColorAntiVirusManagerService
        public void registerStateChangeCallback(IColorAntiViruStateChangeCallback callback) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IColorAntiVirusManagerService {
        private static final String DESCRIPTOR = "com.color.antivirus.IColorAntiVirusManagerService";
        static final int TRANSACTION_registerStateChangeCallback = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IColorAntiVirusManagerService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IColorAntiVirusManagerService)) {
                return new Proxy(obj);
            }
            return (IColorAntiVirusManagerService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode != 1) {
                return null;
            }
            return "registerStateChangeCallback";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                registerStateChangeCallback(IColorAntiViruStateChangeCallback.Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IColorAntiVirusManagerService {
            public static IColorAntiVirusManagerService sDefaultImpl;
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

            @Override // com.color.antivirus.IColorAntiVirusManagerService
            public void registerStateChangeCallback(IColorAntiViruStateChangeCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerStateChangeCallback(callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IColorAntiVirusManagerService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IColorAntiVirusManagerService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
