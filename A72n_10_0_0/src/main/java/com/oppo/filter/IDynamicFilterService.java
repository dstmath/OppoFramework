package com.oppo.filter;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IDynamicFilterService extends IInterface {
    void addToFilter(String str, String str2, String str3) throws RemoteException;

    String getFilterTagValue(String str, String str2) throws RemoteException;

    boolean hasFilter(String str) throws RemoteException;

    boolean inFilter(String str, String str2) throws RemoteException;

    void removeFromFilter(String str, String str2) throws RemoteException;

    public static class Default implements IDynamicFilterService {
        @Override // com.oppo.filter.IDynamicFilterService
        public boolean hasFilter(String name) throws RemoteException {
            return false;
        }

        @Override // com.oppo.filter.IDynamicFilterService
        public boolean inFilter(String name, String tag) throws RemoteException {
            return false;
        }

        @Override // com.oppo.filter.IDynamicFilterService
        public void addToFilter(String name, String tag, String value) throws RemoteException {
        }

        @Override // com.oppo.filter.IDynamicFilterService
        public void removeFromFilter(String name, String tag) throws RemoteException {
        }

        @Override // com.oppo.filter.IDynamicFilterService
        public String getFilterTagValue(String name, String tag) throws RemoteException {
            return null;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IDynamicFilterService {
        private static final String DESCRIPTOR = "com.oppo.filter.IDynamicFilterService";
        static final int TRANSACTION_addToFilter = 3;
        static final int TRANSACTION_getFilterTagValue = 5;
        static final int TRANSACTION_hasFilter = 1;
        static final int TRANSACTION_inFilter = 2;
        static final int TRANSACTION_removeFromFilter = 4;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IDynamicFilterService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IDynamicFilterService)) {
                return new Proxy(obj);
            }
            return (IDynamicFilterService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "hasFilter";
            }
            if (transactionCode == 2) {
                return "inFilter";
            }
            if (transactionCode == 3) {
                return "addToFilter";
            }
            if (transactionCode == 4) {
                return "removeFromFilter";
            }
            if (transactionCode != 5) {
                return null;
            }
            return "getFilterTagValue";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                boolean hasFilter = hasFilter(data.readString());
                reply.writeNoException();
                reply.writeInt(hasFilter ? 1 : 0);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                boolean inFilter = inFilter(data.readString(), data.readString());
                reply.writeNoException();
                reply.writeInt(inFilter ? 1 : 0);
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                addToFilter(data.readString(), data.readString(), data.readString());
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                removeFromFilter(data.readString(), data.readString());
                return true;
            } else if (code == 5) {
                data.enforceInterface(DESCRIPTOR);
                String _result = getFilterTagValue(data.readString(), data.readString());
                reply.writeNoException();
                reply.writeString(_result);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IDynamicFilterService {
            public static IDynamicFilterService sDefaultImpl;
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

            @Override // com.oppo.filter.IDynamicFilterService
            public boolean hasFilter(String name) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    boolean _result = false;
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().hasFilter(name);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.filter.IDynamicFilterService
            public boolean inFilter(String name, String tag) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    _data.writeString(tag);
                    boolean _result = false;
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().inFilter(name, tag);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.filter.IDynamicFilterService
            public void addToFilter(String name, String tag, String value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    _data.writeString(tag);
                    _data.writeString(value);
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().addToFilter(name, tag, value);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.oppo.filter.IDynamicFilterService
            public void removeFromFilter(String name, String tag) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    _data.writeString(tag);
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().removeFromFilter(name, tag);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.oppo.filter.IDynamicFilterService
            public String getFilterTagValue(String name, String tag) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    _data.writeString(tag);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getFilterTagValue(name, tag);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IDynamicFilterService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IDynamicFilterService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
