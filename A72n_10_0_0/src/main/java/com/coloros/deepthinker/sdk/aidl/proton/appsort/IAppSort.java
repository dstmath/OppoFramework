package com.coloros.deepthinker.sdk.aidl.proton.appsort;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface IAppSort extends IInterface {
    List<String> getAppQueueSortedByComplex() throws RemoteException;

    List<String> getAppQueueSortedByCount() throws RemoteException;

    List<String> getAppQueueSortedByTime() throws RemoteException;

    public static class Default implements IAppSort {
        @Override // com.coloros.deepthinker.sdk.aidl.proton.appsort.IAppSort
        public List<String> getAppQueueSortedByTime() throws RemoteException {
            return null;
        }

        @Override // com.coloros.deepthinker.sdk.aidl.proton.appsort.IAppSort
        public List<String> getAppQueueSortedByCount() throws RemoteException {
            return null;
        }

        @Override // com.coloros.deepthinker.sdk.aidl.proton.appsort.IAppSort
        public List<String> getAppQueueSortedByComplex() throws RemoteException {
            return null;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IAppSort {
        private static final String DESCRIPTOR = "com.coloros.deepthinker.sdk.aidl.proton.appsort.IAppSort";
        static final int TRANSACTION_getAppQueueSortedByComplex = 3;
        static final int TRANSACTION_getAppQueueSortedByCount = 2;
        static final int TRANSACTION_getAppQueueSortedByTime = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IAppSort asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IAppSort)) {
                return new Proxy(obj);
            }
            return (IAppSort) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "getAppQueueSortedByTime";
            }
            if (transactionCode == 2) {
                return "getAppQueueSortedByCount";
            }
            if (transactionCode != 3) {
                return null;
            }
            return "getAppQueueSortedByComplex";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                List<String> _result = getAppQueueSortedByTime();
                reply.writeNoException();
                reply.writeStringList(_result);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                List<String> _result2 = getAppQueueSortedByCount();
                reply.writeNoException();
                reply.writeStringList(_result2);
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                List<String> _result3 = getAppQueueSortedByComplex();
                reply.writeNoException();
                reply.writeStringList(_result3);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IAppSort {
            public static IAppSort sDefaultImpl;
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

            @Override // com.coloros.deepthinker.sdk.aidl.proton.appsort.IAppSort
            public List<String> getAppQueueSortedByTime() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAppQueueSortedByTime();
                    }
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.coloros.deepthinker.sdk.aidl.proton.appsort.IAppSort
            public List<String> getAppQueueSortedByCount() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAppQueueSortedByCount();
                    }
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.coloros.deepthinker.sdk.aidl.proton.appsort.IAppSort
            public List<String> getAppQueueSortedByComplex() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAppQueueSortedByComplex();
                    }
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IAppSort impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IAppSort getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
