package com.coloros.deepthinker.sdk.aidl.platform;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IAlgorithmBinderPool extends IInterface {
    IBinder queryBinder(int i) throws RemoteException;

    public static class Default implements IAlgorithmBinderPool {
        @Override // com.coloros.deepthinker.sdk.aidl.platform.IAlgorithmBinderPool
        public IBinder queryBinder(int binderCode) throws RemoteException {
            return null;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IAlgorithmBinderPool {
        private static final String DESCRIPTOR = "com.coloros.deepthinker.sdk.aidl.platform.IAlgorithmBinderPool";
        static final int TRANSACTION_queryBinder = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IAlgorithmBinderPool asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IAlgorithmBinderPool)) {
                return new Proxy(obj);
            }
            return (IAlgorithmBinderPool) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode != 1) {
                return null;
            }
            return "queryBinder";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                IBinder _result = queryBinder(data.readInt());
                reply.writeNoException();
                reply.writeStrongBinder(_result);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IAlgorithmBinderPool {
            public static IAlgorithmBinderPool sDefaultImpl;
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

            @Override // com.coloros.deepthinker.sdk.aidl.platform.IAlgorithmBinderPool
            public IBinder queryBinder(int binderCode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(binderCode);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().queryBinder(binderCode);
                    }
                    _reply.readException();
                    IBinder _result = _reply.readStrongBinder();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IAlgorithmBinderPool impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IAlgorithmBinderPool getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
