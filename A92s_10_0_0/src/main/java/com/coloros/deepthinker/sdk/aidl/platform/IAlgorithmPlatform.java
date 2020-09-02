package com.coloros.deepthinker.sdk.aidl.platform;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IAlgorithmPlatform extends IInterface {
    int getAlgorithmPlatformVersion() throws RemoteException;

    public static class Default implements IAlgorithmPlatform {
        @Override // com.coloros.deepthinker.sdk.aidl.platform.IAlgorithmPlatform
        public int getAlgorithmPlatformVersion() throws RemoteException {
            return 0;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IAlgorithmPlatform {
        private static final String DESCRIPTOR = "com.coloros.deepthinker.sdk.aidl.platform.IAlgorithmPlatform";
        static final int TRANSACTION_getAlgorithmPlatformVersion = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IAlgorithmPlatform asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IAlgorithmPlatform)) {
                return new Proxy(obj);
            }
            return (IAlgorithmPlatform) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode != 1) {
                return null;
            }
            return "getAlgorithmPlatformVersion";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                int _result = getAlgorithmPlatformVersion();
                reply.writeNoException();
                reply.writeInt(_result);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IAlgorithmPlatform {
            public static IAlgorithmPlatform sDefaultImpl;
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

            @Override // com.coloros.deepthinker.sdk.aidl.platform.IAlgorithmPlatform
            public int getAlgorithmPlatformVersion() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAlgorithmPlatformVersion();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IAlgorithmPlatform impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IAlgorithmPlatform getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
